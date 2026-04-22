/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.coroutines.dispatchers

import kotlin.concurrent.Runnable
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.Continuation

/**
 * Base class for coroutine dispatchers.
 *
 * A coroutine dispatcher determines what thread or threads the corresponding
 * coroutine uses for its execution. The coroutine dispatcher can confine
 * coroutine execution to a specific thread, dispatch it to a thread pool,
 * or let it run unconfined.
 *
 * All implementations of [CoroutineDispatcher] must implement [dispatch]
 * which defines how coroutine resumption is scheduled.
 */
public abstract class CoroutineDispatcher : ContinuationInterceptor {

    /**
     * Key for [CoroutineDispatcher] in a coroutine context.
     */
    @Suppress("BRS_NAME_CASE_CLASH")
    public companion object Key : CoroutineContext.Key<CoroutineDispatcher>

    /**
     * Returns the key for this dispatcher element.
     */
    @Suppress("BRS_NAME_CASE_CLASH")
    override val key: CoroutineContext.Key<*> get() = CoroutineDispatcher

    /**
     * Dispatches execution of a runnable [block] onto another thread in the given [context].
     *
     * This method should guarantee that the given [block] will be eventually invoked,
     * otherwise the system may run out of resources.
     *
     * @param context The coroutine context of the coroutine that is being dispatched.
     * @param block The runnable to be dispatched.
     */
    public abstract fun dispatch(context: CoroutineContext, block: Runnable)

    /**
     * Returns `true` if execution shall be dispatched onto another thread.
     *
     * The default implementation returns `true`. Subclasses may override this to return
     * `false` when already executing in the right context (to avoid unnecessary dispatching).
     */
    public open fun isDispatchNeeded(context: CoroutineContext): Boolean = true

    /**
     * Intercepts continuation to add dispatcher logic.
     */
    override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> =
        DispatchedContinuation(this, continuation)

    /**
     * Releases the intercepted continuation.
     */
    override fun releaseInterceptedContinuation(continuation: Continuation<*>) {
        // Nothing to release by default
    }
}

/**
 * A continuation wrapper that dispatches resumption through a dispatcher.
 */
internal class DispatchedContinuation<T>(
    private val dispatcher: CoroutineDispatcher,
    private val continuation: Continuation<T>
) : Continuation<T> {

    override val context: CoroutineContext get() = continuation.context

    override fun resumeWith(result: Result<T>) {
        val resumeBlock = Runnable {
            continuation.resumeWith(result)
        }

        if (dispatcher.isDispatchNeeded(context)) {
            dispatcher.dispatch(context, resumeBlock)
        } else {
            resumeBlock.run()
        }
    }
}
