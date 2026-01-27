/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.coroutines

import kotlin.coroutines.delay.DelayTracker
import kotlin.coroutines.dispatchers.CoroutineQueue
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn

/**
 * Delays coroutine for a given time without blocking a thread and resumes it after a specified time.
 *
 * This suspending function is **non-blocking**: it suspends the coroutine and returns control
 * to the run loop immediately. The coroutine resumes when the delay expires and the run loop
 * calls [processCoroutineDelays].
 *
 * **Important:** Your run loop must call [processCoroutineDelays] on each iteration for delays
 * to fire:
 *
 * ```kotlin
 * // Main thread event loop
 * while (true) {
 *     val msg = port.getMessage()
 *     processCoroutineQueue()
 *     processCoroutineDelays()  // Check and fire completed delays
 *     // ... handle messages
 * }
 *
 * // Render thread - use Timer node
 * // In component init:
 * timer.observeField("fire", "onTimerTick")
 *
 * // In onTimerTick:
 * fun onTimerTick() {
 *     processCoroutineQueue()
 *     processCoroutineDelays()
 * }
 *
 * // Task thread event loop
 * while (running) {
 *     val msg = port.waitMessage(10)  // Short timeout for responsive delays
 *     processCoroutineQueue()
 *     processCoroutineDelays()
 *     // ... handle messages
 * }
 * ```
 *
 * @param timeMillis time in milliseconds.
 */
public suspend fun delay(timeMillis: Long) {
    if (timeMillis <= 0) return

    return suspendCoroutineUninterceptedOrReturn { continuation ->
        // Register a callback with the thread-local DelayTracker.
        // The callback will fire when tick() is called after the deadline passes.
        DelayTracker.current.register(timeMillis) {
            // Resume the coroutine when the delay expires.
            // We resume through the interceptor to ensure proper dispatching.
            val interceptor = continuation.context[ContinuationInterceptor]
            if (interceptor != null) {
                val intercepted = interceptor.interceptContinuation(continuation)
                intercepted.resume(Unit)
            } else {
                continuation.resume(Unit)
            }
        }
        // Return COROUTINE_SUSPENDED to indicate the coroutine is suspended.
        // Control returns to the run loop, which can process other work.
        COROUTINE_SUSPENDED
    }
}

// Note: Duration overload is not available in BrightScript stdlib.
// Use delay(timeMillis: Long) instead.

/**
 * Yields the thread (or thread pool) of the current coroutine dispatcher to other coroutines
 * on the same dispatcher to run if possible.
 *
 * This function suspends the current coroutine and re-dispatches it through the dispatcher,
 * allowing other pending coroutines to run first.
 *
 * **Important:** For `yield()` to work properly, your run loop must call [processCoroutineQueue]
 * on each iteration.
 *
 * Example:
 * ```kotlin
 * runBlocking {
 *     launch {
 *         repeat(3) { i ->
 *             println("A: $i")
 *             yield()
 *         }
 *     }
 *     launch {
 *         repeat(3) { i ->
 *             println("B: $i")
 *             yield()
 *         }
 *     }
 * }
 * // Output interleaves: A: 0, B: 0, A: 1, B: 1, ...
 * ```
 */
public suspend fun yield(): Unit = suspendCoroutineUninterceptedOrReturn { continuation ->
    // Re-dispatch the continuation through the interceptor to allow other coroutines to run.
    val interceptor = continuation.context[ContinuationInterceptor]
    if (interceptor != null) {
        val intercepted = interceptor.interceptContinuation(continuation)
        // The intercepted continuation will dispatch to the queue
        intercepted.resume(Unit)
    } else {
        // No dispatcher - just resume immediately (no-op yield)
        continuation.resume(Unit)
    }
    COROUTINE_SUSPENDED
}

/**
 * Processes pending delays on the current thread.
 *
 * Call this function from your run loop on each iteration to check and fire any delays
 * whose deadlines have passed.
 *
 * Delays are registered by the [delay] function and fire when their deadline passes and
 * this function is called.
 *
 * @return The number of delays that fired.
 */
public fun processCoroutineDelays(): Int {
    return DelayTracker.current.tick()
}

/**
 * Checks if there are any pending delays on the current thread.
 *
 * @return `true` if there are delays waiting to fire.
 */
public fun hasPendingDelays(): Boolean {
    return DelayTracker.current.hasPendingDelays()
}
