/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.coroutines

import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn

/**
 * A handle to a registration (completion / cancel-request handler) that can be
 * disposed to deregister it. Disposal after firing is a no-op.
 */
public interface DisposableHandle {
    public fun dispose()
}

internal object NoopHandle : DisposableHandle {
    override fun dispose() {}
}

public interface Job : CoroutineContext.Element {

    @Suppress("BRS_NAME_CASE_CLASH")
    public companion object Key : CoroutineContext.Key<Job>

    /** Started and neither terminal nor cancel-requested. */
    public val isActive: Boolean

    /** TERMINAL: body finished AND all children finished (any outcome). */
    public val isCompleted: Boolean

    /** True from the moment cancellation (or failure) is requested. */
    public val isCancelled: Boolean

    public fun cancel(cause: Throwable? = null)

    /**
     * Suspends until this job reaches its terminal state. Returns normally
     * even when the target was cancelled or failed — only the CALLER's own
     * cancellation makes join throw ([CancellationException]).
     */
    public suspend fun join()

    public fun start(): Boolean

    /**
     * Registers [handler] to run exactly once when this job reaches its
     * terminal state, with the terminal cause: null = success,
     * [CancellationException] = cancelled, anything else = failure.
     * Runs the handler synchronously at registration when already terminal.
     * The returned handle deregisters it (no-op after firing).
     */
    public fun invokeOnCompletion(handler: (Throwable?) -> Unit): DisposableHandle
}

public interface CompletableJob : Job {
    /** Marks the body finished successfully. False if already finished/cancelled. */
    public fun complete(): Boolean

    /** Marks the body failed. A [CancellationException] cancels quietly instead. */
    public fun completeExceptionally(exception: Throwable): Boolean
}

/** Creates a plain job (no coroutine body): cancel() finishes it immediately. */
public fun Job(parent: Job? = null): CompletableJob = JobImpl(parent)

/**
 * A job whose CHILDREN fail independently: a failed child never cancels this
 * job or its other children (the child reports its own failure instead).
 * Used as the root of [kotlin.brs.componentScope].
 */
public fun SupervisorJob(parent: Job? = null): CompletableJob =
    // hasBody stays explicit: the BRS backend emits supplied arguments
    // positionally with gaps compacted, so a named argument that skips a
    // defaulted parameter binds to the WRONG slot (isSupervisor=true used to
    // land on hasBody). Keep the argument list contiguous.
    JobImpl(parent, hasBody = false, isSupervisor = true)

/** Resolves the concrete JobImpl participating in the hierarchy, if any. */
internal fun jobImplOf(job: Job?): JobImpl? {
    if (job is JobImpl) return job
    if (job is CompletableDeferredImpl<*>) return job.innerJob
    return null
}

/**
 * Attaches [child] to [parent] and returns [parent]. Called from the
 * `parentImpl` property initializer INSTEAD of an `init` block: the BRS
 * backend currently drops init-block bodies from generated constructors,
 * so an attach placed there is silently never emitted.
 */
private fun attachToParent(child: JobImpl, parent: JobImpl?): JobImpl? {
    parent?.attachChild(child)
    return parent
}

/**
 * Job state machine. Single-threaded per component (GetGlobalAA scoping), so
 * plain booleans and lists are sound — no atomics, no locks.
 *
 * Lifecycle: Active -> Completing (body finished OR cancel requested, children
 * still winding down) -> terminal Completed/Cancelled. The terminal transition
 * fires ONLY when the body is done and the children list is empty; that is
 * what makes coroutineScope's "no child still running when it returns"
 * guarantee real.
 *
 * @param hasBody a coroutine writes the body outcome later (cancel must wait
 *   for it); plain Job()/latches have none (cancel finishes immediately).
 * @param isSupervisor children's failures are ignored (they self-report).
 * @param upcallsFailure scope jobs (coroutineScope/withContext/withTimeout)
 *   set false: their failure is DELIVERED to the parked caller (rethrow at
 *   the call site), never upcalled into the caller's job.
 * @param reportsUnhandled launch sets true: a failure that has no
 *   non-supervisor parent to propagate to is printed to the console.
 */
internal open class JobImpl(
    parent: Job? = null,
    internal val hasBody: Boolean = false,
    internal val isSupervisor: Boolean = false,
    internal val upcallsFailure: Boolean = true,
    internal val reportsUnhandled: Boolean = false,
) : CompletableJob {

    @Suppress("LeakingThis")
    internal val parentImpl: JobImpl? = attachToParent(this, jobImplOf(parent))

    private var bodyCompleted: Boolean = false
    private var cancelRequested: Boolean = false
    private var terminal: Boolean = false

    /** Null iff completed successfully; CancellationException iff cancelled. */
    internal var completionCauseInternal: Throwable? = null
        private set

    private val children = mutableListOf<JobImpl>()
    private val completionHandlers = mutableListOf<HandlerEntry>()
    private val cancelHandlers = mutableListOf<HandlerEntry>()

    override val key: CoroutineContext.Key<*> get() = Job

    override val isActive: Boolean get() = !terminal && !cancelRequested
    override val isCompleted: Boolean get() = terminal
    override val isCancelled: Boolean get() = cancelRequested

    override fun start(): Boolean = false // jobs are active from creation

    private class HandlerEntry(
        private val owner: MutableList<HandlerEntry>,
        val handler: (Throwable?) -> Unit,
    ) : DisposableHandle {
        override fun dispose() {
            owner.remove(this)
        }
    }

    override fun invokeOnCompletion(handler: (Throwable?) -> Unit): DisposableHandle {
        if (terminal) {
            // CAUTION for stdlib suspend utilities: fires synchronously.
            // Fast-path the terminal case BEFORE registering (or rely on
            // ParkedContinuation's unarmed latch).
            invokeHandlerSafely(handler, completionCauseInternal)
            return NoopHandle
        }
        val entry = HandlerEntry(completionHandlers, handler)
        completionHandlers.add(entry)
        return entry
    }

    /**
     * Fires the moment cancellation is REQUESTED (before children unwind) —
     * the mid-park wakeup hook. Fires synchronously at registration when
     * already cancel-requested; never fires for a normally-completed job.
     */
    internal fun invokeOnCancelRequest(handler: (Throwable?) -> Unit): DisposableHandle {
        if (cancelRequested) {
            invokeHandlerSafely(handler, completionCauseInternal)
            return NoopHandle
        }
        if (terminal) return NoopHandle
        val entry = HandlerEntry(cancelHandlers, handler)
        cancelHandlers.add(entry)
        return entry
    }

    override fun complete(): Boolean {
        if (bodyCompleted || terminal) return false
        bodyCompleted = true
        val accepted = !cancelRequested
        tryFinish()
        return accepted
    }

    override fun completeExceptionally(exception: Throwable): Boolean {
        if (bodyCompleted || terminal) return false
        bodyCompleted = true
        if (cancelRequested) {
            // Body unwound after an earlier cancel — outcome already decided.
            tryFinish()
            return false
        }
        // Both failure and uncaught CancellationException move to cancelling;
        // the cause class decides failure-vs-quiet at the terminal transition.
        cancelRequested = true
        completionCauseInternal = exception
        fireCancelHandlers()
        cancelChildrenInternal(exception)
        tryFinish()
        return true
    }

    override fun cancel(cause: Throwable?) {
        if (terminal || cancelRequested) return
        cancelRequested = true
        completionCauseInternal = cause ?: CancellationException("Job was cancelled")
        fireCancelHandlers()
        cancelChildrenInternal(completionCauseInternal)
        tryFinish()
    }

    internal fun attachChild(child: JobImpl) {
        children.add(child)
    }

    internal fun detachChild(child: JobImpl) {
        children.remove(child)
        tryFinish()
    }

    /**
     * A child finished with a FAILURE (non-CancellationException cause).
     * First failure wins: cancels this job (and thereby the failed child's
     * siblings) with the child's exception as the cause.
     */
    internal fun childFailed(child: JobImpl, cause: Throwable) {
        if (!terminal && !cancelRequested) {
            cancelRequested = true
            completionCauseInternal = cause
            fireCancelHandlers()
            cancelChildrenInternal(cause)
        }
        detachChild(child)
    }

    private fun cancelChildrenInternal(cause: Throwable?) {
        if (children.isEmpty()) return
        val snapshot = children.toMutableList()
        val childCause: Throwable =
            if (cause is CancellationException) cause
            else CancellationException("Parent job was cancelled", cause)
        for (child in snapshot) {
            child.cancel(childCause)
        }
    }

    private fun tryFinish() {
        if (terminal) return
        if (children.isNotEmpty()) return
        if (!bodyCompleted) {
            // A coroutine-backed job must wait for its body to unwind; a
            // plain job (latch) finishes at the cancel request itself.
            if (!cancelRequested) return
            if (hasBody) return
        }
        terminal = true
        val cause = completionCauseInternal
        val isFailure = cause != null && cause !is CancellationException
        val parent = parentImpl
        if (parent != null) {
            if (isFailure && upcallsFailure && !parent.isSupervisor) {
                parent.childFailed(this, cause!!)
            } else {
                if (isFailure && reportsUnhandled) reportUnhandled(cause!!)
                parent.detachChild(this)
            }
        } else {
            if (isFailure && reportsUnhandled) reportUnhandled(cause!!)
        }
        fireCompletionHandlers(cause)
    }

    private fun fireCancelHandlers() {
        if (cancelHandlers.isEmpty()) return
        val snapshot = cancelHandlers.toMutableList()
        cancelHandlers.clear()
        for (entry in snapshot) {
            invokeHandlerSafely(entry.handler, completionCauseInternal)
        }
    }

    private fun fireCompletionHandlers(cause: Throwable?) {
        if (completionHandlers.isEmpty()) return
        val snapshot = completionHandlers.toMutableList()
        completionHandlers.clear()
        for (entry in snapshot) {
            invokeHandlerSafely(entry.handler, cause)
        }
    }

    private fun invokeHandlerSafely(handler: (Throwable?) -> Unit, cause: Throwable?) {
        try {
            handler(cause)
        } catch (e: Throwable) {
            println("[kotlin.coroutines] Completion handler threw: $e")
        }
    }

    private fun reportUnhandled(cause: Throwable) {
        println("[kotlin.coroutines] Unhandled exception in coroutine: $cause")
    }

    override suspend fun join() {
        return suspendCoroutineUninterceptedOrReturn { continuation ->
            continuation.context.ensureActive()
            if (terminal) {
                Unit
            } else {
                @Suppress("UNCHECKED_CAST")
                val parked = ParkedContinuation(continuation as Continuation<Any?>)
                // join() resumes normally whatever the target's outcome.
                parked.handles.add(invokeOnCompletion { parked.tryResume(Unit) })
                registerCallerCancel(parked, continuation.context)
                parked.finish()
            }
        }
    }
}

/**
 * A deferred value: a [Job] with a result.
 */
public interface Deferred<out T> : Job {
    /**
     * Suspends until complete; returns the value or throws the completion
     * exception (a [CancellationException] when the deferred was cancelled).
     */
    public suspend fun await(): T

    /** The result now, or [IllegalStateException] if not complete. */
    public fun getCompleted(): T

    /**
     * The terminal exception (failure or [CancellationException]), null on
     * success; [IllegalStateException] if not complete.
     */
    public fun getCompletionExceptionOrNull(): Throwable?
}

public interface CompletableDeferred<T> : Deferred<T>, CompletableJob {
    public fun complete(value: T): Boolean
}

public fun <T> CompletableDeferred(parent: Job? = null): CompletableDeferred<T> =
    CompletableDeferredImpl(parent)

/**
 * Deferred over an inner [JobImpl] (composition): the inner job carries ALL
 * lifecycle/handler/hierarchy state; this wrapper adds only the value slot.
 */
internal class CompletableDeferredImpl<T>(
    parent: Job? = null,
    hasBody: Boolean = false,
) : CompletableDeferred<T> {

    // reportsUnhandled already defaults to false; naming it here would skip
    // two defaulted parameters, which the BRS backend miscompiles (see
    // SupervisorJob) — keep supplied arguments contiguous.
    internal val innerJob = JobImpl(parent, hasBody = hasBody)
    private var _value: T? = null

    override val key: CoroutineContext.Key<*> get() = Job

    override val isActive: Boolean get() = innerJob.isActive
    override val isCompleted: Boolean get() = innerJob.isCompleted
    override val isCancelled: Boolean get() = innerJob.isCancelled

    override fun cancel(cause: Throwable?) = innerJob.cancel(cause)

    override suspend fun join() = innerJob.join()

    override fun start(): Boolean = innerJob.start()

    override fun invokeOnCompletion(handler: (Throwable?) -> Unit): DisposableHandle =
        innerJob.invokeOnCompletion(handler)

    override fun complete(): Boolean = innerJob.complete()

    override fun completeExceptionally(exception: Throwable): Boolean =
        innerJob.completeExceptionally(exception)

    override fun complete(value: T): Boolean {
        if (innerJob.isCompleted) return false
        if (!innerJob.isCancelled) {
            // Value stored BEFORE complete() so handlers observe it.
            _value = value
        }
        // Records body completion even when cancel-requested (outcome stays
        // cancelled; the job can now reach terminal) — JobImpl.complete() parity.
        return innerJob.complete()
    }

    override fun getCompleted(): T {
        if (!innerJob.isCompleted) throw IllegalStateException("Deferred has not completed yet")
        val cause = innerJob.completionCauseInternal
        if (cause != null) throw cause
        @Suppress("UNCHECKED_CAST")
        return _value as T
    }

    override fun getCompletionExceptionOrNull(): Throwable? {
        if (!innerJob.isCompleted) throw IllegalStateException("Deferred has not completed yet")
        return innerJob.completionCauseInternal
    }

    override suspend fun await(): T {
        return suspendCoroutineUninterceptedOrReturn { continuation ->
            continuation.context.ensureActive()
            if (innerJob.isCompleted) {
                val cause = innerJob.completionCauseInternal
                if (cause != null) throw cause
                @Suppress("UNCHECKED_CAST")
                (_value as T)
            } else {
                @Suppress("UNCHECKED_CAST")
                val parked = ParkedContinuation(continuation as Continuation<Any?>)
                parked.handles.add(innerJob.invokeOnCompletion { cause ->
                    if (cause != null) {
                        parked.tryResumeException(cause)
                    } else {
                        parked.tryResume(_value)
                    }
                })
                registerCallerCancel(parked, continuation.context)
                parked.finish()
            }
        }
    }
}
