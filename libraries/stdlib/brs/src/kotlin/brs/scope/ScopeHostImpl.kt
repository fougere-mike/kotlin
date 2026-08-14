/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs.scope

import kotlin.brs.BrsInline
import kotlin.brs.BrsStatic
import kotlin.brs.Dynamic
import kotlin.brs.ScopeClosedException
import kotlin.brs.ScopeHost
import kotlin.brs.ScopeRequestException
import kotlin.brs.roku.RoArray
import kotlin.brs.roku.RoAssociativeArray
import kotlin.brs.roku.RoSGNode
import kotlin.brs.roku.RoSGNodeEvent
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.CoroutineImpl
import kotlin.coroutines.CoroutineScope
import kotlin.coroutines.Continuation
import kotlin.coroutines.Job
import kotlin.coroutines.JobImpl
import kotlin.coroutines.ParkedContinuation
import kotlin.coroutines.ScopeBlockCompletion
import kotlin.coroutines.ScopeResultHolder
import kotlin.coroutines.builders.startCoroutine
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.intrinsics.intercepted
import kotlin.coroutines.jobImplOf
import kotlin.coroutines.registerCallerCancel
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * The OWNER half of the ScopeHandle protocol: per-component host state, the
 * owner inbox handler, request-job dispatch with the single completion egress,
 * and the same-component fast path.
 *
 * Dispatch mechanism note: the design prescribes "launch the handler as a
 * child job of the exposed scope". In stdlib source that cannot be the literal
 * `launch { result = handler(args) }` — stdlib compilation generates no
 * suspend state machines (BrsSuspendFunctionsLowering skips them when the
 * coroutine symbols are unavailable), so a stdlib suspend lambda with a
 * non-tail suspend call would miscompile. Instead the USER's handler (fully
 * state-machined in user code) is started directly via its generated
 * CoroutineImpl `create` factory with a [ScopeBlockCompletion] that settles a
 * child [JobImpl] of the exposed scope — the exact machinery `launch` +
 * `parkScopedBlock` are built from, with identical hierarchy, supervisor, and
 * egress semantics.
 */

/** Per-owner host state; one per component (GetGlobalAA identity domain). */
internal class ScopeOwnerState(
    internal val scope: CoroutineScope,
    internal val ownerTop: RoSGNode,
) {
    /** Registered request handlers by wire name. */
    internal val handlers = mutableMapOf<String, ScopeHandlerEntry>()

    /** In-flight request jobs by request key (for cancel envelopes). */
    internal val inFlight = mutableMapOf<String, Job>()

    /** Set by ScopeHost.close(); post-close requests get immediate "closed". */
    internal var closed: Boolean = false
}

/** The component's installed host state (one host per component). */
internal object ScopeHostHolder {
    internal var state: ScopeOwnerState? = null
}

/**
 * Per-component holder for the compiler-installed lowered run{}-block binding
 * table (request name `"<fileFq>#<n>"` → lifted function pointer). `object`
 * state lives on GetGlobalAA — per-COMPONENT-INSTANCE on the render thread —
 * so each owner reads its own table ([ScopeHostHolder] precedent).
 */
internal object ScopeBindingsHolder {
    internal var table: RoAssociativeArray? = null
}

/**
 * Called by compiler-generated component init() (components whose FILE calls
 * exposeScope): installs the lowered run{}-block binding table for this
 * component instance. The `__kotlinPumpAttach` idiom — injected init code
 * hands m-scope values to per-instance stdlib holders; dispatch reads the
 * holder, never ambient `m`.
 */
@BrsStatic
public fun __kotlinScopeBindingsInstall(table: RoAssociativeArray) {
    ScopeBindingsHolder.table = table
}

/** The installed binding for [name], or null (no table / no such block). */
internal fun scopeBindingFor(name: String): Dynamic? {
    val table = ScopeBindingsHolder.table ?: return null
    return table.lookup(name)
}

/** One registered handler: arity + the matching typed function slot. */
internal class ScopeHandlerEntry(
    internal val arity: Int,
    internal val fn0: (suspend () -> Any?)?,
    internal val fn1: (suspend (Any?) -> Any?)?,
    internal val fn2: (suspend (Any?, Any?) -> Any?)?,
)

internal class ScopeHostImpl(private val state: ScopeOwnerState) : ScopeHost {
    override val scope: CoroutineScope
        get() = state.scope

    override fun close() {
        if (state.closed) {
            return
        }
        state.closed = true
        // In-flight request jobs are children of this job: the cancel cascades
        // to each, and each job's single egress posts its "closed" outcome.
        // The inbox stays armed — late requests get an immediate "closed".
        val job = state.scope.coroutineContext[Job]
        if (job != null) {
            job.cancel(ScopeClosedException("scope closed by owner"))
        }
    }
}

@BrsInline("return event.getRoSGNode()")
private external fun scopeInboxEventNodeOrNull(event: RoSGNodeEvent): RoSGNode?

/**
 * True when the host must answer "closed": close() ran, or the exposed scope's
 * job was cancelled some other way (a child job attached to an
 * already-cancelled parent would never be cancelled retroactively, so the
 * handler must not be started at all — the `launch`-on-cancelled-scope guard).
 */
internal fun scopeHostClosed(state: ScopeOwnerState): Boolean {
    if (state.closed) {
        return true
    }
    val job = state.scope.coroutineContext[Job]
    if (job != null) {
        if (job.isCancelled) {
            return true
        }
    }
    return false
}

/** The guided dispatch-miss message (immaculate-diagnostics mandate). */
internal fun scopeMissMessage(name: String, ownerSubtype: String): String {
    return "ScopeHandle: no handler for request '$name' on owner $ownerSubtype — " +
        "register it in exposeScope { handle(...) } (or, for run{ } blocks, declare the " +
        "operation in a file the owner includes — typically your VM class)"
}

/**
 * Observer for the OWNER's inbox (function-name observer form: runs in the
 * owner component's context by platform rule). Dispatches by envelope kind;
 * unknown kinds — including child-role "outcome" traffic on a node that is
 * both owner and child — are ignored silently (decision 9).
 */
internal fun onKotlinScopeInbox(event: RoSGNodeEvent) {
    // Destroyed-node guard (TaskRunner precedent).
    val node = scopeInboxEventNodeOrNull(event)
    if (node == null) {
        return
    }
    val payload = event.getData() as? RoAssociativeArray
    if (payload == null) {
        return
    }
    val env = parseScopeEnvelope(payload)
    if (env.kind == SCOPE_KIND_REQUEST) {
        handleScopeRequest(env)
        return
    }
    if (env.kind == SCOPE_KIND_CANCEL) {
        handleScopeCancel(env)
    }
}

private fun handleScopeRequest(env: ScopeEnvelope) {
    // No state means this node never exposed a scope — the advertisement was
    // never set, so a request here is a stray; drop it like any dead-receiver
    // post on this platform.
    val state = ScopeHostHolder.state ?: return
    val replyTo = env.replyTo ?: return
    if (scopeHostClosed(state)) {
        postScopeOutcome(replyTo, buildScopeClosedOutcome(env.key))
        return
    }
    // Dispatch order: hand-registered map → binding table → guided miss.
    val entry = state.handlers[env.name]
    if (entry != null) {
        // Wire-path args arrive already deep-copied by the field write itself.
        finishScopeRequestDispatch(state, env.key, replyTo, startScopeRequestJob(state, entry, env.args))
        return
    }
    // Compiler-lowered run{} blocks: the table installed by generated init()
    // maps the synthesized request name to the lifted block function.
    val binding = scopeBindingFor(env.name)
    if (binding == null) {
        postScopeOutcome(
            replyTo,
            buildScopeErrorOutcome(env.key, scopeMissMessage(env.name, state.ownerTop.subtype()), 0, null)
        )
        return
    }
    // Wire-path captures arrive already deep-copied by the field write itself.
    finishScopeRequestDispatch(state, env.key, replyTo, startScopeBindingJob(state, binding, env.captures))
}

/**
 * Shared wire-path egress for a started request job (hand-registered handler
 * or lowered-block binding). Single egress: one code path for value, failure,
 * and cancellation (close()'s ScopeClosedException included). Registered
 * after the start — if the job settled synchronously (a lowered block with no
 * suspension runs inline), invokeOnCompletion fires the handler immediately
 * (JobImpl terminal-registration behavior), after the inFlight put.
 */
private fun finishScopeRequestDispatch(
    state: ScopeOwnerState,
    key: String,
    replyTo: RoSGNode,
    started: ScopeRequestJob,
) {
    state.inFlight[key] = started.job
    started.job.invokeOnCompletion { cause ->
        state.inFlight.remove(key)
        if (cause == null) {
            postScopeOutcome(replyTo, buildScopeValueOutcome(key, started.holder.value))
        } else if (cause is CancellationException) {
            postScopeOutcome(replyTo, buildScopeClosedOutcome(key))
        } else {
            postScopeOutcome(replyTo, buildScopeFailureOutcome(key, cause))
        }
    }
}

private fun handleScopeCancel(env: ScopeEnvelope) {
    val state = ScopeHostHolder.state ?: return
    val job = state.inFlight[env.key]
    // Miss = the request already settled — drop (best-effort by design).
    if (job != null) {
        job.cancel()
    }
}

private fun postScopeOutcome(replyTo: RoSGNode, outcome: RoAssociativeArray) {
    // Best-effort: a write to a dead/destroyed receiver is silently dropped
    // by the platform (spike-pinned); the child side handles absence via
    // registry miss + watchdog.
    replyTo.setField(SCOPE_INBOX_FIELD, outcome)
}

/** A started request job plus the slot its result lands in. */
internal class ScopeRequestJob(
    internal val job: JobImpl,
    internal val holder: ScopeResultHolder,
)

/**
 * Starts [entry]'s handler as a child job of the exposed scope, arity-
 * dispatched. The wire path passes the envelope's args (already a private
 * copy); the fast path passes a [deepCopyAA] copy. Args drain via shift() —
 * both callers own their array.
 */
internal fun startScopeRequestJob(
    state: ScopeOwnerState,
    entry: ScopeHandlerEntry,
    args: RoArray?,
): ScopeRequestJob {
    val parentContext = state.scope.coroutineContext
    // Child of the exposed scope's job: close() cascades here; a failure
    // reports to the deferred-style egress (reportsUnhandled=false) and, under
    // the exposed scope's own SupervisorJob (a dedicated child supervisor of
    // componentScope by default — the 2026-08-13 default-scope correction),
    // never poisons sibling requests.
    val job = JobImpl(parentContext[Job], hasBody = true)
    val jobContext = parentContext + job
    val holder = ScopeResultHolder()
    val completion = ScopeBlockCompletion<Any?>(jobContext, job, holder)
    if (entry.arity == 0) {
        startScopeHandler0(entry.fn0!!, completion)
    } else if (entry.arity == 1) {
        val a1 = scopeArgShift(args)
        startScopeHandler1(entry.fn1!!, a1, completion)
    } else {
        val a1 = scopeArgShift(args)
        val a2 = scopeArgShift(args)
        startScopeHandler2(entry.fn2!!, a1, a2, completion)
    }
    return ScopeRequestJob(job, holder)
}

private fun scopeArgShift(args: RoArray?): Any? {
    if (args == null) {
        return null
    }
    return args.shift()
}

private fun startScopeHandler0(fn: suspend () -> Any?, completion: Continuation<Any?>) {
    fn.startCoroutine(completion)
}

// No public starter exists for non-receiver 1/2-param suspend function values;
// these mirror IntrinsicsBrs.createCoroutineUnintercepted exactly: the
// compiled handler is a CoroutineImpl whose generated `create` override takes
// the lambda's parameters (erased to Any?) plus the completion.
private fun startScopeHandler1(
    fn: suspend (Any?) -> Any?,
    a1: Any?,
    completion: Continuation<Any?>,
) {
    @Suppress("UNCHECKED_CAST")
    val impl = fn as CoroutineImpl
    impl.create(a1, completion).intercepted().resume(Unit)
}

private fun startScopeHandler2(
    fn: suspend (Any?, Any?) -> Any?,
    a1: Any?,
    a2: Any?,
    completion: Continuation<Any?>,
) {
    @Suppress("UNCHECKED_CAST")
    val impl = fn as CoroutineImpl
    impl.create(a1, a2, completion).intercepted().resume(Unit)
}

// Raw invocation of a lifted block function pointer. Generated top-level
// suspend functions follow the startCoroutineUninterceptedOrReturn
// convention: `fn(captures, completion)` runs the body up to its first
// suspension and returns either the synchronous result (completion untouched)
// or COROUTINE_SUSPENDED (the block's state machine later settles through
// the completion).
@BrsInline("return binding(captures, completion)")
private external fun invokeScopeBinding(
    binding: Dynamic?,
    captures: RoAssociativeArray?,
    completion: Continuation<Any?>,
): Any?

/**
 * Starts a compiler-lowered run{}-block [binding] as a child job of the
 * exposed scope — the binding-table twin of [startScopeRequestJob], sharing
 * its job/completion/egress machinery. Unlike the handler starters (deferred
 * entry via `create(...).intercepted().resume(Unit)`), a raw function pointer
 * has no create factory: the first segment runs INLINE and a block with no
 * suspension settles synchronously — both egress paths handle that (terminal
 * invokeOnCompletion registration; the fast path's sync-settle latch). A
 * synchronous throw (a non-suspending block body that fails) is routed into
 * the completion, exactly where a state-machined block's failure would land.
 *
 * Wire-path captures arrive already deep-copied by the field write itself;
 * the fast path passes a [deepCopyAA] copy.
 */
internal fun startScopeBindingJob(
    state: ScopeOwnerState,
    binding: Dynamic?,
    captures: RoAssociativeArray?,
): ScopeRequestJob {
    val parentContext = state.scope.coroutineContext
    val job = JobImpl(parentContext[Job], hasBody = true)
    val jobContext = parentContext + job
    val holder = ScopeResultHolder()
    val completion = ScopeBlockCompletion<Any?>(jobContext, job, holder)
    try {
        val result = invokeScopeBinding(binding, captures, completion)
        if (result !== COROUTINE_SUSPENDED) {
            completion.resume(result)
        }
    } catch (e: Throwable) {
        completion.resumeWithException(e)
    }
    return ScopeRequestJob(job, holder)
}

/**
 * Same-component fast path (design §6.2): the ambient component IS the owner,
 * so the request dispatches locally — no mailbox, no key, no watchdog — with
 * semantics identical to the wire path for BOTH surfaces: closed →
 * [ScopeClosedException]; hand-registered map → binding table → no match →
 * [ScopeRequestException] with the guided message; args AND captures cross by
 * copy ([deepCopyAA] — the wire's field write copies for it); the handler or
 * lowered block runs as a child job of the exposed scope; caller cancellation
 * best-effort cancels the request job. Settlements before the engine's
 * `parked.finish()` convert to synchronous return/throw via the park's
 * sync-settle latch.
 */
internal fun dispatchScopeRequestLocally(
    parked: ParkedContinuation,
    callerContext: CoroutineContext,
    ownerNode: RoSGNode,
    name: String,
    args: RoArray?,
    captures: RoAssociativeArray?,
) {
    val state = ScopeHostHolder.state
    if (state == null) {
        throw IllegalStateException(
            "node '${ownerNode.getField("id")}' has not exposed a scope: " +
                "call exposeScope() in the owner component before minting handles"
        )
    }
    if (scopeHostClosed(state)) {
        parked.tryResumeException(ScopeClosedException("owner scope closed"))
        return
    }
    val entry = state.handlers[name]
    if (entry != null) {
        val argsCopy = deepCopyAA(args) as? RoArray
        parkLocalScopeRequest(parked, callerContext, startScopeRequestJob(state, entry, argsCopy))
        return
    }
    val binding = scopeBindingFor(name)
    if (binding == null) {
        parked.tryResumeException(
            ScopeRequestException(scopeMissMessage(name, state.ownerTop.subtype()), 0, null)
        )
        return
    }
    val capturesCopy = deepCopyAA(captures) as? RoAssociativeArray
    parkLocalScopeRequest(parked, callerContext, startScopeBindingJob(state, binding, capturesCopy))
}

/**
 * Shared fast-path egress for a started request job (hand-registered handler
 * or lowered-block binding): single egress, local edition — the same outcome
 * mapping the wire's outcome envelope + onKotlinScopeOutcome would produce.
 */
private fun parkLocalScopeRequest(
    parked: ParkedContinuation,
    callerContext: CoroutineContext,
    started: ScopeRequestJob,
) {
    parked.handles.add(started.job.invokeOnCompletion { cause ->
        if (cause == null) {
            parked.tryResume(started.holder.value)
        } else if (cause is CancellationException) {
            parked.tryResumeException(ScopeClosedException("owner scope closed"))
        } else {
            val info = scopeErrorInfoFrom(cause)
            parked.tryResumeException(ScopeRequestException(info.message, info.number, info.backtrace))
        }
    })
    // Cleanup handle FIRST (cancel the request job — the wire path's cancel
    // envelope, minus the wire), then the caller-cancel wakeup.
    val callerJobImpl = jobImplOf(callerContext[Job])
    if (callerJobImpl != null) {
        parked.handles.add(callerJobImpl.invokeOnCancelRequest {
            started.job.cancel()
        })
    }
    registerCallerCancel(parked, callerContext)
}
