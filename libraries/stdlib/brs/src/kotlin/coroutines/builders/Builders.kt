/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.coroutines.builders

import kotlin.coroutines.*
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.dispatchers.processCoroutineQueue
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.intrinsics.createCoroutineUnintercepted
import kotlin.coroutines.intrinsics.intercepted
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn

/**
 * Launches a new coroutine without blocking the current thread and returns a reference
 * to the coroutine as a [Job]. The coroutine is cancelled when the resulting job is
 * [cancelled][Job.cancel].
 *
 * The new job attaches as a child of the scope's [Job] (from the merged
 * context): cancelling the scope cancels the coroutine, the parent does not
 * complete until the child finishes, and a failure propagates to a
 * non-supervisor parent. Launching on an already-cancelled scope returns a
 * dead (cancelled, completed) job without ever running [block].
 *
 * @param context additional context elements to the [CoroutineScope.coroutineContext] of the coroutine.
 * @param block the coroutine code which will be invoked.
 */
public fun CoroutineScope.launch(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> Unit
): Job {
    val newContext = coroutineContext + context
    val parent = newContext[Job]
    if (parent != null && parent.isCancelled) {
        // kotlinx parity: launching on a cancelled scope yields a dead job
        // and never runs the block.
        val dead = JobImpl(null)
        dead.cancel(CancellationException("Parent job was cancelled"))
        return dead
    }
    val job = JobImpl(parent, hasBody = true, reportsUnhandled = true)
    val newScope = CoroutineScope(newContext + job)
    val continuation = LaunchContinuation(newScope, block, job)
    continuation.start()
    return job
}

/**
 * Creates a coroutine and returns its future result as an implementation of [Deferred].
 * The running coroutine is cancelled when the resulting deferred is [cancelled][Job.cancel].
 *
 * The deferred attaches as a child of the scope's [Job] like [launch], but a
 * failure is held for [Deferred.await] rather than reported to the console.
 * async on an already-cancelled scope returns a dead (cancelled) deferred
 * without ever running [block].
 *
 * @param context additional context elements to the [CoroutineScope.coroutineContext] of the coroutine.
 * @param block the coroutine code.
 */
public fun <T> CoroutineScope.async(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> T
): Deferred<T> {
    val newContext = coroutineContext + context
    val parent = newContext[Job]
    if (parent != null && parent.isCancelled) {
        val dead = CompletableDeferredImpl<T>(null)
        dead.cancel(CancellationException("Parent job was cancelled"))
        return dead
    }
    val deferred = CompletableDeferredImpl<T>(parent, hasBody = true)
    val newScope = CoroutineScope(newContext + deferred)
    val continuation = AsyncContinuation(newScope, block, deferred)
    continuation.start()
    return deferred
}

/**
 * Runs a new coroutine and blocks the current thread interruptibly until its completion.
 *
 * This function should not be used from a coroutine. It is designed to bridge regular blocking code
 * to libraries that are written in suspending style, to be used in `main` functions and in tests.
 *
 * `runBlocking` includes its own event loop that processes:
 * - Dispatched coroutine work (via [processCoroutineQueue])
 * - Pending delays (via [processCoroutineDelays])
 *
 * This allows [delay], [yield], and [launch]/[async] to work correctly within `runBlocking`.
 *
 * @param context the context of the coroutine. The default value is an event loop on the current thread.
 * @param block the coroutine code.
 */
public fun <T> runBlocking(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> T
): T {
    // Supervisor root (kotlinx divergence, deliberate): a failed top-level
    // launch reports to the console instead of cancelling the whole
    // runBlocking scope — same philosophy as componentScope's root.
    val job = SupervisorJob()
    val scope = CoroutineScope(context + job)
    val deferred = CompletableDeferred<T>()

    // Create and start the coroutine
    val continuation = RunBlockingContinuation(scope, block, deferred)
    continuation.start()

    // Process the event loop until the coroutine completes.
    // This loop processes both dispatched work and pending delays.
    while (!deferred.isCompleted && !deferred.isCancelled) {
        // Process any dispatched coroutine work
        processCoroutineQueue()

        // Process any expired delays
        processCoroutineDelays()
    }

    // Return the result or throw the exception
    return deferred.getCompleted()
}

/**
 * Internal continuation for launch builder.
 */
private class LaunchContinuation(
    private val scope: CoroutineScope,
    private val block: suspend CoroutineScope.() -> Unit,
    private val job: CompletableJob
) : Continuation<Unit> {

    override val context: CoroutineContext get() = scope.coroutineContext

    fun start() {
        try {
            // Use block.startCoroutine to properly start the coroutine
            block.startCoroutine(scope, this)
        } catch (e: Throwable) {
            job.completeExceptionally(e)
        }
    }

    override fun resumeWith(result: Result<Unit>) {
        val exception = result.exceptionOrNull()
        if (exception != null) {
            job.completeExceptionally(exception)
        } else {
            job.complete()
        }
    }
}

/**
 * Internal continuation for async builder.
 */
private class AsyncContinuation<T>(
    private val scope: CoroutineScope,
    private val block: suspend CoroutineScope.() -> T,
    private val deferred: CompletableDeferred<T>
) : Continuation<T> {

    override val context: CoroutineContext get() = scope.coroutineContext

    fun start() {
        try {
            block.startCoroutine(scope, this)
        } catch (e: Throwable) {
            deferred.completeExceptionally(e)
        }
    }

    override fun resumeWith(result: Result<T>) {
        val exception = result.exceptionOrNull()
        if (exception != null) {
            deferred.completeExceptionally(exception)
        } else {
            deferred.complete(result.getOrThrow())
        }
    }
}

/**
 * Internal continuation for runBlocking builder.
 */
private class RunBlockingContinuation<T>(
    private val scope: CoroutineScope,
    private val block: suspend CoroutineScope.() -> T,
    private val deferred: CompletableDeferred<T>
) : Continuation<T> {

    override val context: CoroutineContext get() = scope.coroutineContext

    fun start() {
        try {
            block.startCoroutine(scope, this)
        } catch (e: Throwable) {
            deferred.completeExceptionally(e)
        }
    }

    override fun resumeWith(result: Result<T>) {
        val exception = result.exceptionOrNull()
        if (exception != null) {
            deferred.completeExceptionally(exception)
        } else {
            deferred.complete(result.getOrThrow())
        }
    }
}

/**
 * Starts a coroutine without a receiver and with result type [T].
 *
 * The coroutine is started by resuming the continuation returned by
 * [createCoroutineUnintercepted] with Unit.
 */
public fun <T> (suspend () -> T).startCoroutine(completion: Continuation<T>) {
    createCoroutineUnintercepted(completion).intercepted().resume(Unit)
}

/**
 * Starts a coroutine with receiver [R] and result type [T].
 */
public fun <R, T> (suspend R.() -> T).startCoroutine(receiver: R, completion: Continuation<T>) {
    createCoroutineUnintercepted(receiver, completion).intercepted().resume(Unit)
}
