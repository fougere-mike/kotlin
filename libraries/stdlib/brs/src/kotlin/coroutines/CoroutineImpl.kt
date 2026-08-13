/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.coroutines

import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED

/**
 * Base class for intercepted coroutines.
 * Provides caching of the intercepted continuation for efficiency.
 */
internal abstract class InterceptedCoroutine : Continuation<Any?> {
    private var _intercepted: Continuation<Any?>? = null

    /**
     * Returns the intercepted continuation, caching it for subsequent calls.
     */
    fun intercepted(): Continuation<Any?> {
        var result = _intercepted
        if (result == null) {
            result = context[ContinuationInterceptor]?.interceptContinuation(this) ?: this
            _intercepted = result
        }
        return result
    }

    /**
     * Releases the intercepted continuation when this coroutine completes.
     */
    protected fun releaseIntercepted() {
        val intercepted = _intercepted
        if (intercepted != null && intercepted !== this) {
            context[ContinuationInterceptor]!!.releaseInterceptedContinuation(intercepted)
        }
        this._intercepted = CompletedContinuation
    }
}

/**
 * Base class for coroutine state machine implementations in BrightScript.
 *
 * Generated coroutine classes extend this class and implement [doResume]
 * to handle their specific state machine logic.
 *
 * @property resultContinuation The continuation to call when this coroutine completes.
 */
@SinceKotlin("1.3")
internal abstract class CoroutineImpl(
    private val resultContinuation: Continuation<Any?>?
) : InterceptedCoroutine(), Continuation<Any?> {

    /**
     * Current state of the coroutine state machine.
     * Each suspension point corresponds to a state ID.
     */
    protected var state: Int = 0

    /**
     * State to jump to when an exception occurs.
     * Used for exception handling within the state machine.
     */
    protected var exceptionState: Int = 0

    /**
     * The result value from the last suspension point.
     * This is set when the coroutine is resumed with a value.
     */
    protected var result: Any? = null

    /**
     * The exception from the last suspension point.
     * This is set when the coroutine is resumed with an exception.
     */
    protected var exception: Throwable? = null

    /**
     * Path for finally blocks when jumping to them.
     */
    protected var finallyPath: Array<Int>? = null

    /**
     * Cached context from the result continuation.
     */
    private val _context: CoroutineContext? = resultContinuation?.context

    /**
     * The coroutine context for this coroutine.
     * Inherited from the completion continuation.
     */
    override val context: CoroutineContext get() = _context!!

    /**
     * Resume this coroutine with a result.
     * This is called when a suspended operation completes.
     *
     * The implementation unrolls recursion to maintain reasonable stack traces.
     */
    override fun resumeWith(result: Result<Any?>) {
        var current: CoroutineImpl = this
        var currentResult: Any? = result.getOrNull()
        var currentException: Throwable? = result.exceptionOrNull()

        // This loop unrolls recursion in current.resumeWith(param) to make saner and shorter stack traces on resume
        while (true) {
            // Set result and exception fields in the current continuation
            if (currentException == null) {
                current.result = currentResult
            } else {
                current.state = current.exceptionState
                current.exception = currentException
            }

            try {
                val outcome = current.doResume()
                if (outcome === COROUTINE_SUSPENDED) return
                currentResult = outcome
                currentException = null
            } catch (e: Throwable) {
                currentResult = null
                currentException = e
            }

            current.releaseIntercepted() // this state machine instance is terminating

            val completion = current.resultContinuation!!

            if (completion is CoroutineImpl) {
                // unrolling recursion via loop
                current = completion
            } else {
                // top-level completion reached -- invoke and return
                if (currentException != null) {
                    completion.resumeWith(Result.failure(currentException!!))
                } else {
                    completion.resumeWith(Result.success(currentResult))
                }
                return
            }
        }
    }

    /**
     * Execute the state machine logic.
     * Generated coroutine classes override this to implement their specific states.
     *
     * @return The result if the coroutine completed, or [COROUTINE_SUSPENDED] if it suspended.
     */
    protected abstract fun doResume(): Any?

    /**
     * Creates a new coroutine instance with the given completion continuation.
     * Used by suspend lambdas.
     */
    open fun create(completion: Continuation<*>): Continuation<Unit> {
        throw UnsupportedOperationException("create(Continuation) has not been overridden")
    }

    /**
     * Creates a new coroutine instance with a value and completion continuation.
     * Used by suspend lambdas with receiver.
     */
    open fun create(value: Any?, completion: Continuation<*>): Continuation<Unit> {
        throw UnsupportedOperationException("create(Any?;Continuation) has not been overridden")
    }

    /**
     * Creates a new coroutine instance with two values and a completion
     * continuation. Used by 2-parameter suspend lambdas (ScopeHandle's
     * 2-arg request handlers). Declaring this here makes the suspend-lambda
     * lowering type the generated `create` with erased (Any?) parameters and
     * mark it an override — the same mechanism the 1-parameter form relies on
     * (see AbstractSuspendFunctionsLowering's superCreateFunction handling).
     */
    open fun create(value1: Any?, value2: Any?, completion: Continuation<*>): Continuation<Unit> {
        throw UnsupportedOperationException("create(Any?;Any?;Continuation) has not been overridden")
    }
}

/**
 * Sentinel continuation indicating that a coroutine has already completed.
 */
internal object CompletedContinuation : Continuation<Any?> {
    override val context: CoroutineContext
        get() = error("This continuation is already complete")

    override fun resumeWith(result: Result<Any?>) {
        error("This continuation is already complete")
    }

    override fun toString(): String = "This continuation is already complete"
}
