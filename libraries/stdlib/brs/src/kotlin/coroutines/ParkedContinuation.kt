/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.coroutines

import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED

/**
 * One parked suspension. Owns the once-guard that resolves every
 * completion-vs-cancellation race, the disposal of the handles the utility
 * registered, and resumption through the ContinuationInterceptor (dispatcher)
 * when one is present.
 *
 * INLINE-RESUME SAFETY: in the no-interceptor regime (runBlocking/runPumping
 * contexts carry no dispatcher) a handler can fire while the enclosing
 * suspendCoroutineUninterceptedOrReturn block is STILL RUNNING (e.g.
 * coroutineScope starting a block that completes synchronously). Resuming the
 * continuation then would double-execute the caller. So a ParkedContinuation
 * starts UNARMED: settlements before [finish] are recorded, and [finish] —
 * which MUST be the block's last expression — either reports them as a
 * synchronous return/throw or arms the park and reports COROUTINE_SUSPENDED.
 */
internal class ParkedContinuation(
    private val continuation: Continuation<Any?>,
) {
    internal val handles = mutableListOf<DisposableHandle>()

    private var resumed = false
    private var armed = false
    private var syncSettled = false
    private var syncValue: Any? = null
    private var syncException: Throwable? = null

    fun tryResume(value: Any?) {
        if (resumed) return
        resumed = true
        disposeHandles()
        if (!armed) {
            syncSettled = true
            syncValue = value
            return
        }
        resumeTarget().resume(value)
    }

    fun tryResumeException(exception: Throwable) {
        if (resumed) return
        resumed = true
        disposeHandles()
        if (!armed) {
            syncSettled = true
            syncException = exception
            return
        }
        resumeTarget().resumeWithException(exception)
    }

    /** MUST be the last expression of the enclosing intrinsic block. */
    fun finish(): Any? {
        armed = true
        if (syncSettled) {
            val e = syncException
            if (e != null) throw e
            return syncValue
        }
        return COROUTINE_SUSPENDED
    }

    private fun disposeHandles() {
        for (handle in handles) {
            handle.dispose()
        }
        handles.clear()
    }

    private fun resumeTarget(): Continuation<Any?> {
        val interceptor = continuation.context[ContinuationInterceptor]
        if (interceptor != null) {
            return interceptor.interceptContinuation(continuation)
        }
        return continuation
    }
}

/**
 * Throws this job's cancellation cause if it was cancelled. The
 * cooperative-cancellation check: every stdlib suspend utility calls the
 * context form on entry; user loops can call it (or check [isActive]).
 */
public fun Job.ensureActive() {
    if (!isCancelled) return
    val impl = jobImplOf(this)
    val cause = impl?.completionCauseInternal
    if (cause is CancellationException) throw cause
    throw CancellationException("Job was cancelled", cause)
}

/** No-op when the context has no [Job] (main-thread driver coroutines). */
public fun CoroutineContext.ensureActive() {
    this[Job]?.ensureActive()
}

/** True while the scope's job is active; true for job-less scopes. */
public val CoroutineScope.isActive: Boolean
    get() {
        val job = coroutineContext[Job] ?: return true
        return job.isActive
    }

/**
 * Mid-park wakeup registration: cancelling the CALLER's job resumes this park
 * exceptionally with the cancellation cause. No-op for foreign Job types.
 */
internal fun registerCallerCancel(parked: ParkedContinuation, context: CoroutineContext) {
    val impl = jobImplOf(context[Job]) ?: return
    parked.handles.add(impl.invokeOnCancelRequest { cause ->
        val ce = if (cause is CancellationException) cause
        else CancellationException("Job was cancelled", cause)
        parked.tryResumeException(ce)
    })
}
