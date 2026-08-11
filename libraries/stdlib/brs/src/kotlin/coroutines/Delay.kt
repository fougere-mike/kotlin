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
 * to the caller immediately.
 *
 * **In SceneGraph components** no wiring is needed: the self-scheduling pump
 * ([kotlin.coroutines.pump.PumpScheduler], attached automatically at component init) arms a
 * one-shot timer at the next deadline and resumes the coroutine — do NOT create pump timers
 * or call [processCoroutineDelays] from component code.
 *
 * **Main-thread drivers** (a `main()` port loop, test harnesses) own their run loop and must
 * call [processCoroutineDelays] each iteration — `runBlocking` and the kotlin.test device
 * driver's `runPumping` already do:
 *
 * ```kotlin
 * // Main thread event loop
 * while (true) {
 *     val msg = port.waitMessage(10)  // Short timeout for responsive delays
 *     processCoroutineQueue()
 *     processCoroutineDelays()  // Check and fire completed delays
 *     // ... handle messages
 * }
 * ```
 *
 * @param timeMillis time in milliseconds.
 */
public suspend fun delay(timeMillis: Long) {
    if (timeMillis <= 0) return

    return suspendCoroutineUninterceptedOrReturn { continuation ->
        continuation.context.ensureActive()
        @Suppress("UNCHECKED_CAST")
        val parked = ParkedContinuation(continuation as Continuation<Any?>)
        // DelayTracker has no deregistration: after a mid-park cancel wakeup
        // the deadline callback still fires and lands in the once-guard.
        DelayTracker.current.register(timeMillis) { parked.tryResume(Unit) }
        registerCallerCancel(parked, continuation.context)
        parked.finish()
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
    continuation.context.ensureActive()
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
 * **For run-loop OWNERS only** — main-thread drivers (`runBlocking`,
 * `runPumping`) call this each iteration. Component code must NOT call it:
 * the self-scheduling [kotlin.coroutines.pump.PumpScheduler] arms a one-shot
 * timer at the next deadline and ticks the tracker itself.
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

/**
 * Discards all pending delays on the current thread without firing them.
 *
 * Run-loop housekeeping counterpart of
 * [kotlin.coroutines.dispatchers.clearCoroutineQueue]: after a run is
 * abandoned, pending delay callbacks would resume coroutines that must never
 * execute again. This does not cancel those coroutines - it only prevents
 * their delayed resumptions from firing.
 *
 * @return The number of delays discarded.
 */
public fun clearCoroutineDelays(): Int {
    return DelayTracker.current.clear()
}
