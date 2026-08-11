/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.coroutines

import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn

/**
 * Awaits all [deferreds], returning their results in INPUT order.
 *
 * Resumes exceptionally with the FIRST failure as soon as it happens —
 * without waiting for, or cancelling, the remaining deferreds (kotlinx
 * parity). Throws [kotlin.coroutines.cancellation.CancellationException]
 * if the calling coroutine is cancelled (on entry or mid-park).
 */
public suspend fun <T> awaitAll(vararg deferreds: Deferred<T>): List<T> =
    deferreds.toList().awaitAll()

/** See [awaitAll]. */
public suspend fun <T> Collection<Deferred<T>>.awaitAll(): List<T> {
    if (isEmpty()) return emptyList()
    val list = this.toList()
    return suspendCoroutineUninterceptedOrReturn { continuation ->
        continuation.context.ensureActive()
        var pendingCount = 0
        var priorFailure: Throwable? = null
        for (d in list) {
            if (d.isCompleted) {
                if (priorFailure == null) {
                    priorFailure = d.getCompletionExceptionOrNull()
                }
            } else {
                pendingCount = pendingCount + 1
            }
        }
        val alreadyFailed = priorFailure
        if (alreadyFailed != null) throw alreadyFailed
        if (pendingCount == 0) {
            collectResults(list)
        } else {
            @Suppress("UNCHECKED_CAST")
            val parked = ParkedContinuation(continuation as Continuation<Any?>)
            // Mutable state lives in a class instance, NOT captured mutable
            // locals — stdlib closures must not need shared-box codegen.
            val state = CountdownState(pendingCount)
            for (d in list) {
                if (!d.isCompleted) {
                    parked.handles.add(d.invokeOnCompletion { cause ->
                        if (cause != null) {
                            parked.tryResumeException(cause)
                        } else {
                            state.remaining = state.remaining - 1
                            if (state.remaining == 0) {
                                parked.tryResume(collectResults(list))
                            }
                        }
                    })
                }
            }
            registerCallerCancel(parked, continuation.context)
            parked.finish()
        }
    }
}

/**
 * Suspends until all [jobs] are terminal, whatever their outcomes — target
 * failures/cancellations never throw here. Caller cancellation does.
 */
public suspend fun joinAll(vararg jobs: Job) {
    return jobs.toList().joinAll()
}

/** See [joinAll]. */
public suspend fun Collection<Job>.joinAll() {
    if (isEmpty()) return
    val list = this.toList()
    return suspendCoroutineUninterceptedOrReturn { continuation ->
        continuation.context.ensureActive()
        var pendingCount = 0
        for (job in list) {
            if (!job.isCompleted) pendingCount = pendingCount + 1
        }
        if (pendingCount == 0) {
            Unit
        } else {
            @Suppress("UNCHECKED_CAST")
            val parked = ParkedContinuation(continuation as Continuation<Any?>)
            val state = CountdownState(pendingCount)
            for (job in list) {
                if (!job.isCompleted) {
                    parked.handles.add(job.invokeOnCompletion {
                        state.remaining = state.remaining - 1
                        if (state.remaining == 0) {
                            parked.tryResume(Unit)
                        }
                    })
                }
            }
            registerCallerCancel(parked, continuation.context)
            parked.finish()
        }
    }
}

private class CountdownState(var remaining: Int)

private fun <T> collectResults(list: List<Deferred<T>>): List<T> {
    val results = ArrayList<T>(list.size)
    for (d in list) {
        results.add(d.getCompleted())
    }
    return results
}
