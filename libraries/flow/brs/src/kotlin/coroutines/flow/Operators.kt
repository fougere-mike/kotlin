/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.coroutines.flow

import kotlin.coroutines.cancellation.CancellationException

// Simple intermediate operators, mirroring kotlinx.coroutines.flow. All are
// regular non-inline extensions over `flow {}` + `collect` (klib inline never
// inlines at user call sites on this backend; kotlinx's `inline` there is a JVM
// perf tactic, not semantics). Bodies capture the builder receiver into an
// explicit `downstream` local rather than relying on implicit-receiver capture
// through the nested collect lambdas.
//
// Every upstream collection routes through flowCollectDispatch with an explicit
// FlowCollector SAM (the internal-collect law, Doorbells.kt header): a bare
// `upstream.collect { }` lambda SAM-converts to the MEMBER collect (the member
// wins overload resolution), which is fn-slot dispatch — an operator applied to
// a shared-VM StateFlow would collect it cross-component on the disclaimed
// SetRef fn-ref path.

/** Returns a flow containing the results of applying [transform] to each upstream value. */
public fun <T, R> Flow<T>.map(transform: suspend (T) -> R): Flow<R> {
    val upstream = this
    return flow {
        val downstream = this
        flowCollectDispatch(upstream, FlowCollector { value ->
            downstream.emit(transform(value))
        })
    }
}

/** Returns a flow containing only upstream values matching [predicate]. */
public fun <T> Flow<T>.filter(predicate: suspend (T) -> Boolean): Flow<T> {
    val upstream = this
    return flow {
        val downstream = this
        flowCollectDispatch(upstream, FlowCollector { value ->
            if (predicate(value)) downstream.emit(value)
        })
    }
}

/** Returns a flow containing only the non-null upstream values. */
public fun <T : Any> Flow<T?>.filterNotNull(): Flow<T> {
    val upstream = this
    return flow {
        val downstream = this
        flowCollectDispatch(upstream, FlowCollector { value ->
            if (value != null) downstream.emit(value)
        })
    }
}

/**
 * Applies [block] to each upstream value with the downstream collector as
 * receiver: the block may emit zero or more values per input — the general
 * operator `map` and `filter` are special cases of.
 */
public fun <T, R> Flow<T>.transform(block: suspend FlowCollector<R>.(T) -> Unit): Flow<R> {
    val upstream = this
    return flow {
        val downstream = this
        flowCollectDispatch(upstream, FlowCollector { value ->
            downstream.block(value)
        })
    }
}

/**
 * Invokes [action] on each upstream value BEFORE it is emitted downstream;
 * values pass through unchanged.
 */
public fun <T> Flow<T>.onEach(action: suspend (T) -> Unit): Flow<T> {
    val upstream = this
    return flow {
        val downstream = this
        flowCollectDispatch(upstream, FlowCollector { value ->
            action(value)
            downstream.emit(value)
        })
    }
}

/**
 * Invokes [action] BEFORE the upstream flow starts being collected. The action
 * receives the downstream collector, so values it emits arrive ahead of every
 * upstream value.
 */
public fun <T> Flow<T>.onStart(action: suspend FlowCollector<T>.() -> Unit): Flow<T> {
    val upstream = this
    return flow {
        val downstream = this
        downstream.action()
        downstream.emitAll(upstream)
    }
}

/**
 * Receiver for an [onCompletion] action invoked after a failure: emitting into
 * a completed-exceptionally flow is illegal, so any emit rethrows the original
 * cause (kotlinx's ThrowingCollector).
 */
private class ThrowingCollector(private val cause: Throwable) : FlowCollector<Any?> {
    override suspend fun emit(value: Any?) {
        throw cause
    }
}

/**
 * Invokes [action] when upstream collection completes, with the completion
 * cause: `null` on normal completion, the exception on failure (upstream OR
 * downstream), and the CancellationException on cancellation — the full
 * doFinally matrix (flow-program spec decision 9). A non-null cause is
 * rethrown after the action runs — onCompletion never swallows.
 *
 * On normal completion the action may emit trailing values; after a failure
 * the receiver is a [ThrowingCollector], so emitting rethrows the cause.
 */
public fun <T> Flow<T>.onCompletion(action: suspend FlowCollector<T>.(Throwable?) -> Unit): Flow<T> {
    val upstream = this
    return flow {
        val downstream = this
        // The cause is recorded and handled AFTER the try: the action is a
        // suspend call, and suspend calls stay out of catch handlers here.
        var completionCause: Throwable? = null
        try {
            downstream.emitAll(upstream)
        } catch (e: Throwable) {
            completionCause = e
        }
        val cause = completionCause
        if (cause != null) {
            val throwing: FlowCollector<T> = ThrowingCollector(cause)
            throwing.action(cause)
            throw cause
        }
        downstream.action(null)
    }
}

/**
 * Catches UPSTREAM exceptions only: [action] receives the failure and may emit
 * fallback values. Exception transparency holds in both directions —
 * downstream (collector-block) exceptions pass through uncaught (identity
 * check), and CancellationException always rethrows, so a cancelled collector
 * and an in-flight flow-terminal abort both unwind untouched.
 */
public fun <T> Flow<T>.catch(action: suspend FlowCollector<T>.(Throwable) -> Unit): Flow<T> {
    val upstream = this
    return flow {
        val downstream = this
        var fromDownstream: Throwable? = null
        var upstreamFailure: Throwable? = null
        try {
            flowCollectDispatch(upstream, FlowCollector { value ->
                try {
                    downstream.emit(value)
                } catch (e: Throwable) {
                    fromDownstream = e
                    throw e
                }
            })
        } catch (e: Throwable) {
            // The CE guard MUST come first: CancellationException extends
            // IllegalStateException on this platform, so any handling ahead of
            // this line could swallow cancellation (or a take/first abort).
            if (e is CancellationException) throw e
            if (e === fromDownstream) throw e
            upstreamFailure = e
        }
        val cause = upstreamFailure
        if (cause != null) downstream.action(cause)
    }
}

/** Sentinel for "no previous value yet" in [distinctUntilChanged] (T may be nullable). */
private object DistinctNoValue

/**
 * Suppresses CONSECUTIVE repeats: a value equal (`==`) to the immediately
 * preceding emission is dropped. Non-consecutive repeats pass through.
 */
public fun <T> Flow<T>.distinctUntilChanged(): Flow<T> {
    val upstream = this
    return flow {
        val downstream = this
        var previous: Any? = DistinctNoValue
        flowCollectDispatch(upstream, FlowCollector { value ->
            if (previous === DistinctNoValue || previous != value) {
                previous = value
                downstream.emit(value)
            }
        })
    }
}

/**
 * Returns a flow of the first [count] upstream values, then aborts upstream
 * collection via [AbortFlowException] — the producer body never runs past the
 * count-th emit. The abort is caught here by owner identity and never leaks;
 * someone else's abort (e.g. a downstream `first()`) is rethrown to its own
 * catch site.
 *
 * Throws [IllegalArgumentException] when [count] is not positive, at
 * operator-construction time (kotlinx parity).
 */
public fun <T> Flow<T>.take(count: Int): Flow<T> {
    if (count <= 0) throw IllegalArgumentException("Requested element count $count should be positive")
    val upstream = this
    return flow {
        val downstream = this
        var consumed = 0
        try {
            flowCollectDispatch(upstream, FlowCollector { value ->
                consumed++
                downstream.emit(value)
                if (consumed >= count) throw AbortFlowException(downstream)
            })
        } catch (e: Throwable) {
            // Throwable + manual discrimination, NOT `catch (e: AbortFlowException)`:
            // the suspend state machine emits every typed catch clause as a
            // catch-all (BrsStateMachineBuilder.visitTry), so a typed clause here
            // would run the owner check on a foreign exception — a crash instead
            // of propagation (device-pinned: upstreamFailurePropagatesThroughTake).
            if (e !is AbortFlowException || e.owner !== downstream) throw e
        }
    }
}

/**
 * Skips the first [count] upstream values, then passes the rest through.
 *
 * Throws [IllegalArgumentException] when [count] is negative, at
 * operator-construction time (kotlinx parity).
 */
public fun <T> Flow<T>.drop(count: Int): Flow<T> {
    if (count < 0) throw IllegalArgumentException("Drop count should be non-negative, but had $count")
    val upstream = this
    return flow {
        val downstream = this
        var skipped = 0
        flowCollectDispatch(upstream, FlowCollector { value ->
            if (skipped >= count) downstream.emit(value) else skipped++
        })
    }
}
