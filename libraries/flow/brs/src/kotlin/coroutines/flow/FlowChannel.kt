/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.coroutines.flow

import kotlin.coroutines.Continuation
import kotlin.coroutines.ensureActive
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/** The value behind [FlowChannel.CLOSED]; a module-private object no user value can be. */
internal object FlowChannelClosed

/** The value behind [FlowChannel.EMPTY]; a module-private object no user value can be. */
internal object FlowChannelEmpty

/**
 * INTERNAL same-context queue: the concurrent operators' primitive (spec §4).
 * Sender coroutines [send] (non-suspending, unbounded) and one drain loop
 * [receiveOrClosed]s in the downstream collector's coroutine. NOT public API,
 * no thread safety, no cross-component reach — everything runs in the single
 * context the enclosing `collect` runs in.
 *
 * Discipline (all callers are in this file's module):
 * - ONE receiver at a time: the operators' single drain loop. A second
 *   concurrent receiver would silently overwrite the parked continuation.
 * - [close] is idempotent — the FIRST close wins, including its [closeCause].
 * - [send] after close is silently dropped: closes race sender teardown, and
 *   by then the drain no longer delivers (operator scope is unwinding).
 * - Liveness: an operator must guarantee close() once its senders are done
 *   (job completion handlers) — the drain's park is woken ONLY by send/close.
 *   Cancellation of the drain's coroutine is honored at the next receive
 *   entry-check; the park itself is not cancellation-woken, so a guaranteed
 *   close is what prevents a cancelled-but-parked drain from hanging.
 */
internal class FlowChannel<T>(private val conflated: Boolean = false) {
    private val buffer = ArrayList<Any?>()
    private var closed = false
    private var receiver: Continuation<Unit>? = null

    /** The cause [close] was called with; null before close and on normal completion. */
    internal var closeCause: Throwable? = null
        private set

    /**
     * Whether [close] has been called. The task-lift teardown reads this to
     * decide whether the producing task is already terminal (its own
     * complete/error envelope closed the channel) or must be stopped.
     */
    internal val isClosed: Boolean
        get() = closed

    /**
     * Enqueues [value] (conflated mode: replaces the not-yet-taken tail value)
     * and wakes a parked receiver. Non-suspending; dropped after [close].
     */
    fun send(value: T) {
        if (closed) return
        if (conflated && buffer.isNotEmpty()) {
            buffer[buffer.size - 1] = value
        } else {
            buffer.add(value)
        }
        wakeReceiver()
    }

    /** Closes the channel; first close wins. Buffered values stay receivable. */
    fun close(cause: Throwable? = null) {
        if (closed) return
        closed = true
        closeCause = cause
        wakeReceiver()
    }

    /**
     * The next buffered value, or [CLOSED] once the channel is closed and
     * drained. Entry-checks the receiving coroutine's job on every iteration
     * (the every-suspend-point law), so a cancelled drain stops at its next
     * receive even while values remain buffered.
     */
    suspend fun receiveOrClosed(): Any? {
        while (true) {
            currentCoroutineContext().ensureActive()
            if (buffer.isNotEmpty()) return buffer.removeAt(0)
            if (closed) return CLOSED
            // Parks until send/close wakes it; resume routes through the
            // ContinuationInterceptor when one is present (suspendCoroutine
            // intercepts), inline in the no-interceptor runBlocking regime.
            suspendCoroutine { cont -> receiver = cont }
        }
    }

    /**
     * Non-suspending poll: the next buffered value, [CLOSED] once the channel
     * is closed AND drained (buffered values stay receivable, matching
     * [receiveOrClosed]), or [EMPTY] when nothing is buffered yet.
     *
     * For drains that own their OWN park (the task-lift collector parks on a
     * cancellation-aware `Job().join()` instead of this channel's plain park,
     * so caller cancel wakes it mid-park — see TaskFlow.kt). [receiveOrClosed]
     * and [poll] must not be mixed on one channel: poll bypasses the parked
     * receiver, which only send/close wake.
     */
    fun poll(): Any? {
        if (buffer.isNotEmpty()) return buffer.removeAt(0)
        if (closed) return CLOSED
        return EMPTY
    }

    private fun wakeReceiver() {
        val parked = receiver
        if (parked != null) {
            receiver = null
            parked.resume(Unit)
        }
    }

    companion object {
        /** Sentinel [receiveOrClosed] returns when the channel is closed and drained. */
        val CLOSED: Any = FlowChannelClosed

        /** Sentinel [poll] returns when the channel is open with nothing buffered. */
        val EMPTY: Any = FlowChannelEmpty
    }
}
