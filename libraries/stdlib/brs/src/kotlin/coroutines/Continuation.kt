/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.coroutines

/**
 * Interface representing a continuation after a suspension point that returns a value of type `T`.
 */
@SinceKotlin("1.3")
public interface Continuation<in T> {
    /**
     * The context of the coroutine that corresponds to this continuation.
     */
    public val context: CoroutineContext

    /**
     * Resumes the execution of the corresponding coroutine passing a successful or failed [result] as the
     * return value of the last suspension point.
     */
    public fun resumeWith(result: Result<T>)
}

/**
 * Resumes the execution of the corresponding coroutine passing [value] as the return value of the last suspension point.
 *
 * Note: For BrightScript, this is non-inline because klib inline function deserialization is not yet implemented.
 * This allows the function to be called from user code that depends on the stdlib klib.
 */
@SinceKotlin("1.3")
public fun <T> Continuation<T>.resume(value: T): Unit =
    resumeWith(Result.success(value))

/**
 * Resumes the execution of the corresponding coroutine so that the [exception] is re-thrown right after the
 * last suspension point.
 *
 * Note: For BrightScript, this is non-inline because klib inline function deserialization is not yet implemented.
 * This allows the function to be called from user code that depends on the stdlib klib.
 */
@SinceKotlin("1.3")
public fun <T> Continuation<T>.resumeWithException(exception: Throwable): Unit =
    resumeWith(Result.failure(exception))

/**
 * Suspends the coroutine like [suspendCoroutineUninterceptedOrReturn], but an intercepted continuation is
 * provided to the [block].
 *
 * This function is designed to convert callbacks into suspending functions.
 *
 * Example of usage:
 * ```
 * suspend fun awaitCallback(): T = suspendCoroutine { continuation ->
 *     callback { value ->
 *         continuation.resume(value)
 *     }
 * }
 * ```
 *
 * @param block A block that receives the continuation. The block may either call [Continuation.resume]
 *   or [Continuation.resumeWithException] on the continuation to resume coroutine with result or exception,
 *   or it can simply return if it is going to complete asynchronously via callback.
 * @return The value that was passed to [Continuation.resume] or throws the exception that was passed to
 *   [Continuation.resumeWithException].
 */
@SinceKotlin("1.3")
public suspend inline fun <T> suspendCoroutine(crossinline block: (Continuation<T>) -> Unit): T {
    return kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn { continuation ->
        // Intercept the continuation to handle dispatching
        val intercepted = continuation.context[ContinuationInterceptor]
            ?.interceptContinuation(continuation)
            ?: continuation

        // Call the block with the intercepted continuation
        block(intercepted)

        // Return COROUTINE_SUSPENDED to indicate that we're suspending
        kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
    }
}
