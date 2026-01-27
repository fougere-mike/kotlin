/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.coroutines.intrinsics

import kotlin.coroutines.*

/**
 * Creates a coroutine without starting it.
 *
 * Returns a Continuation that, when resumed with Unit, starts the coroutine.
 *
 * @param completion The continuation to resume when the coroutine completes.
 * @return A continuation that starts the coroutine when resumed.
 */
@SinceKotlin("1.3")
@Suppress("UNCHECKED_CAST")
public fun <T> (suspend () -> T).createCoroutineUnintercepted(
    completion: Continuation<T>
): Continuation<Unit> {
    // The compiler transforms suspend lambdas to implement CoroutineImpl
    // which has a create method for this purpose
    val impl = this as CoroutineImpl
    return impl.create(completion)
}

/**
 * Creates a coroutine with receiver without starting it.
 *
 * @param receiver The receiver for the suspend function.
 * @param completion The continuation to resume when the coroutine completes.
 * @return A continuation that starts the coroutine when resumed.
 */
@SinceKotlin("1.3")
@Suppress("UNCHECKED_CAST")
public fun <R, T> (suspend R.() -> T).createCoroutineUnintercepted(
    receiver: R,
    completion: Continuation<T>
): Continuation<Unit> {
    val impl = this as CoroutineImpl
    return impl.create(receiver, completion)
}

/**
 * Intercepts a continuation with the coroutine context's interceptor.
 *
 * If the context has a ContinuationInterceptor, this wraps the continuation
 * to ensure it resumes on the correct dispatcher. Otherwise, returns the
 * continuation unchanged.
 *
 * @return The intercepted continuation, or the original if no interceptor.
 */
@SinceKotlin("1.3")
@Suppress("UNCHECKED_CAST")
public fun <T> Continuation<T>.intercepted(): Continuation<T> =
    (this as? InterceptedCoroutine)?.intercepted() as? Continuation<T> ?: this

/**
 * Starts a coroutine immediately and returns the result or COROUTINE_SUSPENDED.
 *
 * @param completion The continuation to resume when the coroutine completes.
 * @return The result if completed synchronously, or COROUTINE_SUSPENDED.
 */
@SinceKotlin("1.3")
@Suppress("UNCHECKED_CAST")
public fun <T> (suspend () -> T).startCoroutineUninterceptedOrReturn(
    completion: Continuation<T>
): Any? {
    val impl = this as CoroutineImpl
    impl.create(completion).resumeWith(Result.success(Unit))
    return COROUTINE_SUSPENDED // BrightScript always suspends initially
}

/**
 * Starts a coroutine with receiver immediately and returns the result or COROUTINE_SUSPENDED.
 *
 * @param receiver The receiver for the suspend function.
 * @param completion The continuation to resume when the coroutine completes.
 * @return The result if completed synchronously, or COROUTINE_SUSPENDED.
 */
@SinceKotlin("1.3")
@Suppress("UNCHECKED_CAST")
public fun <R, T> (suspend R.() -> T).startCoroutineUninterceptedOrReturn(
    receiver: R,
    completion: Continuation<T>
): Any? {
    val impl = this as CoroutineImpl
    impl.create(receiver, completion).resumeWith(Result.success(Unit))
    return COROUTINE_SUSPENDED
}

/**
 * Gets the current continuation from a suspend function.
 *
 * This is a compiler intrinsic - the compiler replaces calls to this function
 * with code that obtains the current continuation from the suspend function's context.
 *
 * Note: This is NOT a suspend function because the compiler handles it specially.
 * When inlined into a suspend function, the compiler replaces the call with code
 * that accesses the continuation parameter.
 */
@PublishedApi
internal fun <T> getContinuation(): Continuation<T> {
    // This is a compiler intrinsic - the compiler provides the implementation
    throw UnsupportedOperationException("getContinuation is a compiler intrinsic")
}

/**
 * Returns the coroutine context of the current coroutine.
 *
 * This is a compiler intrinsic function. Calls to this function are transformed
 * by the compiler to access the context from the current continuation.
 */
@PublishedApi
internal fun getCoroutineContext(): CoroutineContext {
    // This is a compiler intrinsic
    throw UnsupportedOperationException("getCoroutineContext is a compiler intrinsic")
}

/**
 * Returns the result if not suspended, otherwise suspends the coroutine.
 *
 * If [result] is [COROUTINE_SUSPENDED], the state machine handles the suspension.
 * Otherwise, it returns [result] casted to [T].
 *
 * @param result The result value or COROUTINE_SUSPENDED
 * @return The result value
 */
@Suppress("UNCHECKED_CAST")
@PublishedApi
internal suspend fun <T> returnIfSuspended(result: Any?): T {
    // Simply cast the result - the state machine handles COROUTINE_SUSPENDED
    return result as T
}

/**
 * BrightScript-specific implementation of suspendCoroutineUninterceptedOrReturn.
 *
 * This function is called in place of suspendCoroutineUninterceptedOrReturn.
 * It obtains the current continuation and passes it to the block.
 */
@PublishedApi
internal suspend inline fun <T> suspendCoroutineUninterceptedOrReturnBRS(
    crossinline block: (Continuation<T>) -> Any?
): T = returnIfSuspended<T>(block(getContinuation<T>()))

/**
 * Obtains the current continuation instance inside suspend functions and either suspends
 * currently running coroutine or returns result immediately without suspension.
 *
 * This inline function is the implementation of suspendCoroutineUninterceptedOrReturn intrinsic.
 * It obtains the current continuation and calls the given [block] with the continuation.
 * The [block] should return either the result value if it has completed synchronously,
 * or [COROUTINE_SUSPENDED] if the continuation will be resumed later.
 *
 * @param block The block that receives the continuation and returns the result or COROUTINE_SUSPENDED.
 * @return The result if completed synchronously, or suspends otherwise.
 */
@SinceKotlin("1.3")
public suspend inline fun <T> suspendCoroutineUninterceptedOrReturn(
    crossinline block: (Continuation<T>) -> Any?
): T = suspendCoroutineUninterceptedOrReturnBRS(block)
