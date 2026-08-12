/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.coroutines

import kotlin.coroutines.builders.startCoroutine
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn

/**
 * Runs [block] in a child scope and suspends until the block AND every
 * coroutine launched in it complete. A child's failure cancels the scope's
 * other children (mid-park wakeup) and, once all have finished, rethrows the
 * ORIGINAL exception here — it is delivered to the caller, never upcalled
 * into the caller's job (kotlinx ScopeCoroutine parity). The caller's own
 * cancellation reaches the scope through the child cascade.
 *
 * The receiver scope is only valid inside [block]: its job completes when
 * this function returns, so launching on a receiver leaked out of the block
 * attaches to an already-terminal parent and is not supported.
 */
@Suppress("BRS_NAME_CASE_CLASH")
public suspend fun <R> coroutineScope(block: suspend CoroutineScope.() -> R): R {
    return suspendCoroutineUninterceptedOrReturn { continuation ->
        val outerContext = continuation.context
        outerContext.ensureActive()
        val scopeJob = JobImpl(outerContext[Job], hasBody = true, upcallsFailure = false)
        @Suppress("UNCHECKED_CAST")
        parkScopedBlock(
            continuation as Continuation<Any?>,
            outerContext + scopeJob,
            scopeJob,
            block
        ) { cause, value, parked ->
            if (cause != null) {
                parked.tryResumeException(cause)
            } else {
                parked.tryResume(value)
            }
        }
    }
}

internal class ScopeResultHolder {
    var value: Any? = null
}

/** Completion continuation for a scope's block: parks the value, settles the job. */
internal class ScopeBlockCompletion<R>(
    override val context: CoroutineContext,
    private val job: JobImpl,
    private val holder: ScopeResultHolder,
) : Continuation<R> {
    override fun resumeWith(result: Result<R>) {
        val exception = result.exceptionOrNull()
        if (exception != null) {
            job.completeExceptionally(exception)
        } else {
            holder.value = result.getOrNull()
            job.complete()
        }
    }
}

/**
 * Shared engine for coroutineScope/withContext/withTimeout: starts [block] as
 * a coroutine completing [scopeJob], and resumes [continuation] from the
 * scope job's TERMINAL handler via [onTerminal]. Returns parked.finish() —
 * the caller must return this from its intrinsic block, LAST.
 */
internal fun <R> parkScopedBlock(
    continuation: Continuation<Any?>,
    scopeContext: CoroutineContext,
    scopeJob: JobImpl,
    block: suspend CoroutineScope.() -> R,
    onTerminal: (cause: Throwable?, value: Any?, parked: ParkedContinuation) -> Unit,
): Any? {
    val parked = ParkedContinuation(continuation)
    val holder = ScopeResultHolder()
    parked.handles.add(scopeJob.invokeOnCompletion { cause ->
        onTerminal(cause, holder.value, parked)
    })
    val scope = CoroutineScope(scopeContext)
    block.startCoroutine(scope, ScopeBlockCompletion(scopeContext, scopeJob, holder))
    return parked.finish()
}
