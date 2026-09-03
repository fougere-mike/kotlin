/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.coroutines.flow

import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineScope
import kotlin.coroutines.Job
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.coroutineScope
import kotlin.coroutines.ensureActive
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.coroutines.builders.launch

// Concurrent operators over the internal FlowChannel primitive: upstream(s)
// collected in coroutineScope children feeding one channel; the downstream
// loop drains receiveOrClosed() in the collector's own coroutine. Mutable
// coordination state lives in class-instance holders, not captured vars.
//
// SINGLE-CONTEXT INTERLEAVING LAW (spec §4, pinned by the
// flatMapMergeNonSuspendingInnersDegenerateToConcat device test): children
// interleave only at suspension points. An inner flow that never suspends
// runs to completion synchronously when started, so flatMapMerge over
// non-suspending inners degenerates to concat order. Comparable to kotlinx
// on a single confined dispatcher — documented truth, not fought.
//
// Liveness discipline (see FlowChannel): every operator arranges close() via
// job completion handlers so the drain's park is always eventually woken —
// on normal completion, failure, and cancellation alike.

/** Sends every emission into [channel]; entry-checks the EMITTING coroutine's
 * job first, so a cancelled inner's late emissions are dropped at the source
 * (the *Latest cancel contract) rather than delivered from the buffer. */
private class ChannelSendCollector<T>(private val channel: FlowChannel<T>) : FlowCollector<T> {
    override suspend fun emit(value: T) {
        currentCoroutineContext().ensureActive()
        channel.send(value)
    }
}

/**
 * Drains [channel] into [downstream] until CLOSED, then rethrows a non-null
 * non-cancellation close cause (upstream/inner failures propagate to the
 * collector as themselves). A CancellationException cause is NOT rethrown
 * here: cancellation is owned by the receive entry-check and the enclosing
 * scope's completion.
 */
private suspend fun <R> drainTo(channel: FlowChannel<R>, downstream: FlowCollector<R>) {
    while (true) {
        val next = channel.receiveOrClosed()
        if (next === FlowChannel.CLOSED) break
        @Suppress("UNCHECKED_CAST")
        downstream.emit(next as R)
    }
    val cause = channel.closeCause
    if (cause != null && cause !is CancellationException) throw cause
}

/**
 * Transforms each upstream value into an inner flow and collects each inner
 * to COMPLETION before the next upstream value is taken — sequential
 * flattening, no concurrency (semantically `transform { emitAll(transform(it)) }`).
 */
public fun <T, R> Flow<T>.flatMapConcat(transform: suspend (T) -> Flow<R>): Flow<R> {
    val upstream = this
    return flow {
        val downstream = this
        flowCollectDispatch(upstream, FlowCollector { value ->
            downstream.emitAll(transform(value))
        })
    }
}

/**
 * Coordination state for [flatMapMerge]: the active-inner count doubles as
 * the concurrency gate (upstream collection parks in [acquireSlot] while the
 * limit is reached; each completed inner frees a slot and wakes it), and the
 * completion accounting closes the channel once upstream AND all inners are
 * done. The first non-cancellation failure is recorded as the close cause.
 */
private class MergeCoordinator(private val concurrency: Int) {
    var active = 0
    var upstreamDone = false
    var failure: Throwable? = null
    private var slotWaiter: Continuation<Unit>? = null

    /** Parks while the concurrency limit is reached, then claims a slot. */
    suspend fun acquireSlot() {
        while (active >= concurrency) {
            suspendCoroutine { cont -> slotWaiter = cont }
            // Woken by a slot release OR transitively during teardown (every
            // cancelled inner releases); honor cancellation before reclaiming.
            currentCoroutineContext().ensureActive()
        }
        active++
    }

    fun onInnerCompleted(channel: FlowChannel<*>, cause: Throwable?) {
        recordFailure(cause)
        active--
        val waiter = slotWaiter
        if (waiter != null) {
            slotWaiter = null
            waiter.resume(Unit)
        }
        maybeClose(channel)
    }

    fun onUpstreamCompleted(channel: FlowChannel<*>, cause: Throwable?) {
        recordFailure(cause)
        upstreamDone = true
        maybeClose(channel)
    }

    private fun recordFailure(cause: Throwable?) {
        if (cause != null && cause !is CancellationException && failure == null) {
            failure = cause
        }
    }

    private fun maybeClose(channel: FlowChannel<*>) {
        if (upstreamDone && active == 0) {
            channel.close(failure)
        }
    }
}

/**
 * Transforms each upstream value into an inner flow and collects up to
 * [concurrency] inners CONCURRENTLY (as scope children), merging their
 * emissions; upstream collection suspends while the limit is reached.
 * `concurrency = 1` degrades to [flatMapConcat] ordering. A failure in the
 * upstream or any inner cancels the rest and propagates to the collector.
 *
 * Single-context interleaving (spec §4): inners interleave only at their
 * suspension points — non-suspending inners run to completion synchronously
 * when started, so their merged order degenerates to concat. Documented law;
 * see the file-header note.
 *
 * Throws [IllegalArgumentException] when [concurrency] is not positive, at
 * operator-construction time (kotlinx parity).
 */
public fun <T, R> Flow<T>.flatMapMerge(concurrency: Int = 16, transform: suspend (T) -> Flow<R>): Flow<R> {
    if (concurrency <= 0) throw IllegalArgumentException("Expected positive concurrency level, but had $concurrency")
    val upstream = this
    return flow {
        val downstream = this
        val channel = FlowChannel<R>()
        val sendCollector = ChannelSendCollector(channel)
        val coordinator = MergeCoordinator(concurrency)
        coroutineScope {
            val scope = this
            val upstreamJob = scope.launch {
                flowCollectDispatch(upstream, FlowCollector { value ->
                    // transform BEFORE claiming a slot: a throwing transform
                    // must not leave a claimed slot with no inner to release
                    // it (the close accounting would wedge the drain).
                    val inner = transform(value)
                    coordinator.acquireSlot()
                    val innerJob = scope.launch {
                        // Dispatch-routed (the internal-collect law): an inner
                        // flow may be a StateFlow.
                        flowCollectDispatch(inner, sendCollector)
                    }
                    // Safe if the inner already finished inline: a terminal
                    // job fires the handler synchronously at registration.
                    innerJob.invokeOnCompletion { cause -> coordinator.onInnerCompleted(channel, cause) }
                })
            }
            upstreamJob.invokeOnCompletion { cause -> coordinator.onUpstreamCompleted(channel, cause) }
            drainTo(channel, downstream)
        }
    }
}

/** Holder for the *Latest family's in-flight inner job (class-instance state,
 * not a captured var — the shared-box defensive convention). */
private class LatestState {
    var current: Job? = null
}

/**
 * Like [transform], but a NEW upstream value first CANCELS the in-flight
 * block and awaits its unwind (cancel + join: the superseded block's
 * `finally` completes before the new block's first emission), then runs
 * [block] for the new value. A cancelled block's late emissions are dropped.
 * The LAST value's block runs to completion. The Rx switchMap family's
 * engine; failures in the block or upstream propagate to the collector.
 */
public fun <T, R> Flow<T>.transformLatest(block: suspend FlowCollector<R>.(T) -> Unit): Flow<R> {
    val upstream = this
    return flow {
        val downstream = this
        val channel = FlowChannel<R>()
        val sendCollector = ChannelSendCollector(channel)
        coroutineScope {
            val scope = this
            val state = LatestState()
            val upstreamJob = scope.launch {
                flowCollectDispatch(upstream, FlowCollector { value ->
                    val previous = state.current
                    if (previous != null) {
                        previous.cancel()
                        previous.join()
                    }
                    state.current = scope.launch {
                        sendCollector.block(value)
                    }
                })
                // Upstream is done: let the last block finish naturally.
                val last = state.current
                if (last != null) {
                    last.join()
                }
            }
            upstreamJob.invokeOnCompletion { cause -> channel.close(cause) }
            drainTo(channel, downstream)
        }
    }
}

/**
 * Transforms each upstream value into an inner flow and mirrors the LATEST
 * one: a new upstream value cancels the in-flight inner's collection
 * (cancel + join — its `finally` runs, late emissions are dropped) and
 * switches to the new inner — the Rx switchMap contract.
 */
public fun <T, R> Flow<T>.flatMapLatest(transform: suspend (T) -> Flow<R>): Flow<R> =
    transformLatest { value -> emitAll(transform(value)) }

/**
 * Maps each upstream value through [transform], cancelling a still-running
 * transform when the next value arrives — only the latest value's result is
 * emitted ([transformLatest] with a single emit).
 */
public fun <T, R> Flow<T>.mapLatest(transform: suspend (T) -> R): Flow<R> =
    transformLatest { value -> emit(transform(value)) }

/**
 * Collects the flow, cancelling a still-running [action] when the next value
 * arrives and restarting it with the new value (kotlinx collectLatest). The
 * last value's action runs to completion before this returns.
 */
public suspend fun <T> Flow<T>.collectLatest(action: suspend (T) -> Unit) {
    flowCollectDispatch(transformLatest<T, Unit> { value -> action(value) }, FlowCollector { })
}

/** One side's update crossing a [combine] channel: which flow, what value. */
private class CombineUpdate(val index: Int, val value: Any?)

/** Sends [index]-tagged updates into the combine channel; entry-checks the
 * emitting coroutine's job like [ChannelSendCollector]. */
private class CombineSendCollector(
    private val channel: FlowChannel<CombineUpdate>,
    private val index: Int,
) : FlowCollector<Any?> {
    override suspend fun emit(value: Any?) {
        currentCoroutineContext().ensureActive()
        channel.send(CombineUpdate(index, value))
    }
}

/** Drain-side state for [combine]: each side's latest value, presence flags,
 * and the two-sender completion count that closes the channel. */
private class CombineCoordinator {
    var hasFirst = false
    var hasSecond = false
    var first: Any? = null
    var second: Any? = null
    var remaining = 2
    var failure: Throwable? = null

    fun onSenderCompleted(channel: FlowChannel<*>, cause: Throwable?) {
        if (cause != null && cause !is CancellationException && failure == null) {
            failure = cause
        }
        remaining--
        if (remaining == 0) {
            channel.close(failure)
        }
    }
}

/**
 * Combines the LATEST values of two flows: nothing is emitted until both
 * have produced a value; from then on every update from either side emits
 * `transform(latest1, latest2)`. Completes when BOTH flows complete; a
 * failure in either cancels the other and propagates.
 *
 * v1 divergence from kotlinx (KDoc'd, deliberate): a flow that completes
 * without EVER emitting does not short-circuit the combine — the other side
 * is still collected to completion (without emissions, since the has-both
 * gate never opens).
 */
public fun <T1, T2, R> combine(f1: Flow<T1>, f2: Flow<T2>, transform: suspend (T1, T2) -> R): Flow<R> {
    return flow {
        val downstream = this
        val channel = FlowChannel<CombineUpdate>()
        val coordinator = CombineCoordinator()
        coroutineScope {
            val scope = this
            val firstJob = scope.launch {
                flowCollectDispatch(f1, CombineSendCollector(channel, 0))
            }
            firstJob.invokeOnCompletion { cause -> coordinator.onSenderCompleted(channel, cause) }
            val secondJob = scope.launch {
                flowCollectDispatch(f2, CombineSendCollector(channel, 1))
            }
            secondJob.invokeOnCompletion { cause -> coordinator.onSenderCompleted(channel, cause) }
            while (true) {
                val next = channel.receiveOrClosed()
                if (next === FlowChannel.CLOSED) break
                val update = next as CombineUpdate
                if (update.index == 0) {
                    coordinator.first = update.value
                    coordinator.hasFirst = true
                } else {
                    coordinator.second = update.value
                    coordinator.hasSecond = true
                }
                if (coordinator.hasFirst && coordinator.hasSecond) {
                    @Suppress("UNCHECKED_CAST")
                    downstream.emit(transform(coordinator.first as T1, coordinator.second as T2))
                }
            }
            val cause = channel.closeCause
            if (cause != null && cause !is CancellationException) throw cause
        }
    }
}

/**
 * Conflates upstream emissions: the collector always receives the LATEST
 * value, and values produced while it is busy replace each other instead of
 * queueing (kotlinx buffer(CONFLATED)). A collector that keeps up sees every
 * value. Single-context shape: a fully synchronous upstream burst runs to
 * completion before the collector's first take — it sees only the last value.
 */
public fun <T> Flow<T>.conflate(): Flow<T> {
    val upstream = this
    return flow {
        val downstream = this
        val channel = FlowChannel<T>(conflated = true)
        val sendCollector = ChannelSendCollector(channel)
        coroutineScope {
            val scope = this
            val upstreamJob = scope.launch {
                flowCollectDispatch(upstream, sendCollector)
            }
            upstreamJob.invokeOnCompletion { cause -> channel.close(cause) }
            drainTo(channel, downstream)
        }
    }
}
