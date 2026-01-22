/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.coroutines

import kotlin.coroutines.intrinsics.CoroutineSingletons
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED

/**
 * BrightScript actual implementation of SafeContinuation.
 *
 * This class provides double-resume protection for continuations used
 * with suspendCoroutine and similar functions.
 */
@PublishedApi
@SinceKotlin("1.3")
internal actual class SafeContinuation<in T>
internal actual constructor(
    private val delegate: Continuation<T>,
    initialResult: Any?
) : Continuation<T> {

    /**
     * Constructs a SafeContinuation with UNDECIDED initial state.
     * Used for asynchronous resumption scenarios.
     */
    @PublishedApi
    internal actual constructor(delegate: Continuation<T>) : this(delegate, CoroutineSingletons.UNDECIDED)

    /**
     * The coroutine context from the delegate continuation.
     */
    actual override val context: CoroutineContext
        get() = delegate.context

    /**
     * The current result state.
     * Can be: UNDECIDED, COROUTINE_SUSPENDED, RESUMED, or the actual result value.
     */
    private var result: Any? = initialResult

    /**
     * Resume the continuation with the given result.
     * Throws IllegalStateException if already resumed.
     */
    actual override fun resumeWith(result: Result<T>) {
        val cur = this.result
        when {
            cur === CoroutineSingletons.UNDECIDED -> {
                // First resume - store the result value
                // For success, store the value; for failure, wrap the exception
                this.result = result.fold(
                    onSuccess = { it },
                    onFailure = { ResultFailure(it) }
                )
            }
            cur === CoroutineSingletons.COROUTINE_SUSPENDED -> {
                // Already suspended, resume the delegate
                this.result = CoroutineSingletons.RESUMED
                delegate.resumeWith(result)
            }
            else -> throw IllegalStateException("Already resumed")
        }
    }

    /**
     * Gets the result or returns COROUTINE_SUSPENDED if resumption will be async.
     *
     * This is called after the user's block in suspendCoroutine returns.
     * If the continuation was already resumed synchronously, returns the result.
     * Otherwise, marks the state as COROUTINE_SUSPENDED and returns that marker.
     */
    @PublishedApi
    internal actual fun getOrThrow(): Any? {
        if (result === CoroutineSingletons.UNDECIDED) {
            result = CoroutineSingletons.COROUTINE_SUSPENDED
            return COROUTINE_SUSPENDED
        }
        val result = this.result
        return when {
            result === CoroutineSingletons.RESUMED -> COROUTINE_SUSPENDED // already called continuation, indicate COROUTINE_SUSPENDED upstream
            result is ResultFailure -> throw result.exception
            else -> result // either COROUTINE_SUSPENDED or data
        }
    }
}

/**
 * Internal wrapper for exceptions in Result.
 * Used to distinguish exceptional results from normal values.
 */
private class ResultFailure(val exception: Throwable)
