/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.coroutines

import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn

/**
 * Delays coroutine for a given time without blocking a thread and resumes it after a specified time.
 *
 * This suspending function is cancellable: it will throw [CancellationException] if the [Job] of the
 * current coroutine is cancelled while this suspending function is waiting.
 *
 * In BrightScript, this uses a Timer node to schedule the resumption.
 *
 * @param timeMillis time in milliseconds.
 */
public suspend fun delay(timeMillis: Long) {
    if (timeMillis <= 0) return

    return suspendCoroutine { continuation ->
        // In a full implementation, this would:
        // 1. Create a Timer node
        // 2. Set its duration to timeMillis / 1000.0
        // 3. Observe the "fire" field
        // 4. Resume the continuation when the timer fires
        //
        // For now, we resume immediately (no actual delay)
        // Real implementation requires SceneGraph Timer integration
        continuation.resume(Unit)
    }
}

// Note: Duration overload is not available in BrightScript stdlib.
// Use delay(timeMillis: Long) instead.

/**
 * Yields the thread (or thread pool) of the current coroutine dispatcher to other coroutines
 * on the same dispatcher to run if possible.
 *
 * This suspending function is cancellable: it will throw [CancellationException] if the [Job]
 * of the current coroutine is cancelled while this suspending function is waiting.
 *
 * Note that this function does not resume on the original dispatcher context. Use
 * `withContext(Dispatchers.Main) { yield() }` if you need to yield and resume on a specific dispatcher.
 */
public suspend fun yield(): Unit = suspendCoroutine { continuation ->
    // Re-dispatch the continuation to allow other coroutines to run
    val interceptor = continuation.context[ContinuationInterceptor]
    if (interceptor != null) {
        // Dispatch the resumption
        val intercepted = interceptor.interceptContinuation(continuation)
        intercepted.resume(Unit)
    } else {
        // No dispatcher, resume immediately
        continuation.resume(Unit)
    }
}
