/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.coroutines.builders

import kotlin.coroutines.*
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.delay.DelayTracker
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn

/**
 * Thrown by [withTimeout] on expiry. Extends [CancellationException], so an
 * uncaught one CANCELS the coroutine instead of counting as a failure.
 */
public class TimeoutCancellationException internal constructor(
    message: String,
) : CancellationException(message)

/**
 * Runs [block] in a child scope with a deadline. On expiry the scope job is
 * cancelled with a [TimeoutCancellationException] (children get mid-park
 * wakeup); once they unwind, that TCE is rethrown here.
 *
 * Render-side only: work already handed to a task thread (runTask) is NOT
 * stopped — the waiting stops, and the task's late completion resumes nothing.
 *
 * The PumpScheduler arms a one-shot wakeup for DelayTracker deadlines, so
 * component timeouts fire promptly with zero wiring.
 */
public suspend fun <T> withTimeout(timeMillis: Long, block: suspend CoroutineScope.() -> T): T {
    return suspendCoroutineUninterceptedOrReturn { continuation ->
        val outerContext = continuation.context
        outerContext.ensureActive()
        if (timeMillis <= 0) {
            throw TimeoutCancellationException("Timed out immediately (timeMillis=$timeMillis)")
        }
        val scopeJob = JobImpl(outerContext[Job], hasBody = true, upcallsFailure = false)
        val timeout = TimeoutCancellationException("Timed out after ${timeMillis}ms")
        DelayTracker.current.register(timeMillis) {
            if (!scopeJob.isCompleted) {
                scopeJob.cancel(timeout)
            }
        }
        @Suppress("UNCHECKED_CAST")
        parkScopedBlock(
            continuation as Continuation<Any?>,
            outerContext + scopeJob,
            scopeJob,
            block
        ) { cause, value, parked ->
            if (cause != null) {
                parked.tryResumeException(cause)
            } else {
                parked.tryResume(value)
            }
        }
    }
}

/**
 * [withTimeout] that maps ITS OWN expiry to `null` instead of throwing.
 * A nested timeout's exception (different instance) still propagates.
 */
public suspend fun <T> withTimeoutOrNull(timeMillis: Long, block: suspend CoroutineScope.() -> T): T? {
    return suspendCoroutineUninterceptedOrReturn { continuation ->
        val outerContext = continuation.context
        outerContext.ensureActive()
        if (timeMillis <= 0) {
            null
        } else {
            val scopeJob = JobImpl(outerContext[Job], hasBody = true, upcallsFailure = false)
            val timeout = TimeoutCancellationException("Timed out after ${timeMillis}ms")
            DelayTracker.current.register(timeMillis) {
                if (!scopeJob.isCompleted) {
                    scopeJob.cancel(timeout)
                }
            }
            @Suppress("UNCHECKED_CAST")
            parkScopedBlock(
                continuation as Continuation<Any?>,
                outerContext + scopeJob,
                scopeJob,
                block
            ) { cause, value, parked ->
                if (cause === timeout) {
                    parked.tryResume(null)
                } else if (cause != null) {
                    parked.tryResumeException(cause)
                } else {
                    parked.tryResume(value)
                }
            }
        }
    }
}
