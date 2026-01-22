/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.coroutines

/**
 * A background job. Conceptually, a job is a cancellable thing with a life-cycle
 * that culminates in its completion.
 *
 * Jobs can be arranged into parent-child hierarchies where cancellation of a parent
 * leads to immediate cancellation of all its children recursively.
 *
 * The most basic instances of [Job] interface are created like this:
 *
 * * **Coroutine job** is created with [launch][CoroutineScope.launch] coroutine builder.
 *   It runs a specified block of code and completes on completion of this block.
 * * **[CompletableJob]** is created with a `Job()` factory function.
 *   It is completed by calling [CompletableJob.complete].
 *
 * Conceptually, an execution of a job does not produce a result value. Jobs are launched
 * solely for their side-effects.
 */
public interface Job : CoroutineContext.Element {

    /**
     * Key for [Job] instance in the coroutine context.
     */
    public companion object Key : CoroutineContext.Key<Job>

    /**
     * Returns `true` when this job is active -- it was already started and has not completed nor was cancelled yet.
     */
    public val isActive: Boolean

    /**
     * Returns `true` when this job has completed for any reason.
     */
    public val isCompleted: Boolean

    /**
     * Returns `true` if this job was cancelled for any reason.
     */
    public val isCancelled: Boolean

    /**
     * Cancels this job with an optional [cause].
     *
     * A [cause] can be used to specify an error message or to provide other details
     * for debugging purposes.
     */
    public fun cancel(cause: Throwable? = null)

    /**
     * Suspends the coroutine until this job is complete.
     *
     * This suspending function is cancellable and **always** checks for a cancellation
     * of the invoking coroutine's Job.
     */
    public suspend fun join()

    /**
     * Starts coroutine related to this job (if any) if it was not started yet.
     *
     * @return `true` if this invocation actually started the job.
     */
    public fun start(): Boolean
}

/**
 * A job that can be completed using [complete] function.
 */
public interface CompletableJob : Job {
    /**
     * Completes this job.
     *
     * The result of this function depends on the state of this job:
     * * If this job is already completed, returns `false`.
     * * If this job is active, moves it into completing state (only possible for uncompleted jobs),
     *   returns `true`.
     */
    public fun complete(): Boolean

    /**
     * Completes this job exceptionally with a given [exception].
     */
    public fun completeExceptionally(exception: Throwable): Boolean
}

/**
 * Creates a simple job.
 */
public fun Job(parent: Job? = null): CompletableJob = JobImpl(parent)

/**
 * Basic implementation of [CompletableJob].
 */
internal class JobImpl(private val parent: Job? = null) : CompletableJob {
    private var _isActive: Boolean = true
    private var _isCompleted: Boolean = false
    private var _isCancelled: Boolean = false
    private var completionException: Throwable? = null

    override val key: CoroutineContext.Key<*> get() = Job

    override val isActive: Boolean get() = _isActive && !_isCompleted && !_isCancelled

    override val isCompleted: Boolean get() = _isCompleted

    override val isCancelled: Boolean get() = _isCancelled

    override fun cancel(cause: Throwable?) {
        if (_isCompleted || _isCancelled) return
        _isCancelled = true
        _isActive = false
        completionException = cause
    }

    override suspend fun join() {
        // Simple busy-wait implementation - in real implementation would use suspendCoroutine
        while (!_isCompleted && !_isCancelled) {
            // yield() would go here in a real implementation
        }
    }

    override fun start(): Boolean {
        if (_isActive || _isCompleted || _isCancelled) return false
        _isActive = true
        return true
    }

    override fun complete(): Boolean {
        if (_isCompleted || _isCancelled) return false
        _isCompleted = true
        _isActive = false
        return true
    }

    override fun completeExceptionally(exception: Throwable): Boolean {
        if (_isCompleted || _isCancelled) return false
        _isCompleted = true
        _isCancelled = true
        _isActive = false
        completionException = exception
        return true
    }
}

/**
 * A deferred value is a non-blocking cancellable future &mdash; it is a [Job] with a result.
 *
 * It is created with the [async][CoroutineScope.async] coroutine builder or via
 * constructor of [CompletableDeferred] class.
 */
public interface Deferred<out T> : Job {
    /**
     * Awaits for completion of this value without blocking a thread and resumes when deferred computation
     * is complete, returning the resulting value or throwing the corresponding exception if the deferred
     * was cancelled.
     */
    public suspend fun await(): T

    /**
     * Returns the result immediately or throws [IllegalStateException] if this deferred value has not
     * completed yet.
     */
    public fun getCompleted(): T
}

/**
 * A [Deferred] that can be completed via public functions [complete] or [completeExceptionally].
 */
public interface CompletableDeferred<T> : Deferred<T>, CompletableJob {
    /**
     * Completes this deferred value with a given [value].
     */
    public fun complete(value: T): Boolean
}

/**
 * Creates a [CompletableDeferred] in an _active_ state.
 */
public fun <T> CompletableDeferred(parent: Job? = null): CompletableDeferred<T> = CompletableDeferredImpl(parent)

/**
 * Basic implementation of [CompletableDeferred].
 */
internal class CompletableDeferredImpl<T>(parent: Job? = null) : CompletableDeferred<T> {
    private val job = JobImpl(parent)
    private var _value: T? = null
    private var _exception: Throwable? = null

    override val key: CoroutineContext.Key<*> get() = Job

    override val isActive: Boolean get() = job.isActive
    override val isCompleted: Boolean get() = job.isCompleted
    override val isCancelled: Boolean get() = job.isCancelled

    override fun cancel(cause: Throwable?) = job.cancel(cause)

    override suspend fun join() = job.join()

    override fun start(): Boolean = job.start()

    override fun complete(): Boolean = job.complete()

    override fun completeExceptionally(exception: Throwable): Boolean {
        _exception = exception
        return job.completeExceptionally(exception)
    }

    override fun complete(value: T): Boolean {
        if (job.isCompleted || job.isCancelled) return false
        _value = value
        return job.complete()
    }

    override suspend fun await(): T {
        job.join()
        _exception?.let { throw it }
        @Suppress("UNCHECKED_CAST")
        return _value as T
    }

    override fun getCompleted(): T {
        if (!job.isCompleted) throw IllegalStateException("Deferred has not completed yet")
        _exception?.let { throw it }
        @Suppress("UNCHECKED_CAST")
        return _value as T
    }
}
