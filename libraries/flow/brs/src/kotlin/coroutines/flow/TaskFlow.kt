/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// The flowOn(Dispatchers.Task) lift's runtime half (flow-program spec §5).
// Generated basename (TaskFlowKt.brs) is unique across the stdlib and flow
// runtime JARs — the KGP merged-staging collision guard enforces this at
// packaging time.
//
// KEEP SIGNATURES IN SYNC with the task-lift lowering
// (compiler/ir/backend.brightscript/src/.../lower/BrsFlowTaskLiftLowering.kt):
// [taskFlowLifted] is its flowOn call-site rewrite target, and [driveFlowTask]
// + [readCapturesFrom] are what the synthesized component's run() calls — the
// lowering builds those calls against these exact shapes (the runTaskImpl /
// runLowered convention). The mangled names are pinned by the flowOnLift /
// spawnTaskLift goldens: a signature change here trips the golden gate loudly,
// which is the intended tripwire.
//
// THE CANCEL-MECHANISM CHOICE (spec §5, decision 8 — recorded here because the
// obvious template does not apply): TaskRunner's cancel path registers
// stdlib-INTERNAL cancel-request hooks (jobImplOf + invokeOnCancelRequest +
// registerCallerCancel) so protocol teardown runs before the park wakes. This
// klib is a separate user-mode module and cannot touch those internals, so the
// collector composes the same guarantees from PUBLIC surface instead:
//
//   - the drain never parks in FlowChannel's own (not cancellation-woken)
//     park; it polls the channel and parks on a fresh `Job()`'s `join()`,
//     which the stdlib promises is cancellation-aware mid-park (caller cancel
//     wakes it with CancellationException — Job.kt join / CLAUDE.md
//     cancellation promises);
//   - teardown (registry drop → observer disarm → `flowCancel=true` →
//     `control="STOP"`, the spec §5 sequence) runs in the collect body's
//     `finally`, i.e. AFTER the park wakes with the CE but strictly BEFORE the
//     CE reaches user code. The window between the cancel request and the
//     finally is harmless by construction: any envelope delivered in it lands
//     in a channel whose only drain is the already-cancelled collect, and the
//     registry drop + disarm then make every later delivery a no-op — the
//     same end state TaskRunner's pre-wake cleanup guarantees.
//
// The finally shape also unifies ALL early exits: caller cancellation,
// downstream aborts (first()/take() AbortFlowException unwinding through
// collector.emit), downstream throws, and withTimeout expiry all stop the
// task through the one teardown path.
package kotlin.coroutines.flow

import kotlin.brs.BrsInline
import kotlin.brs.Dynamic
import kotlin.brs.brsName
import kotlin.brs.roku.RoAssociativeArray
import kotlin.brs.roku.RoSGNode
import kotlin.brs.roku.RoSGNodeEvent
import kotlin.coroutines.CompletableJob
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.Job
import kotlin.coroutines.builders.startCoroutine
import kotlin.coroutines.ensureActive
import kotlin.coroutines.pump.kotlinAmbientTopOrNull
import kotlin.coroutines.task.FlowTaskCancellation
import kotlin.coroutines.task.FlowTaskThreadSlot
import kotlin.coroutines.task.TaskException
import kotlin.coroutines.task.flowCancelRequested

// FlowTaskComponent protocol field names (SceneComponent.kt declares them;
// `control`/`kotlinTaskId` come from the native Task node / TaskComponent).
private const val FLOW_CAPTURES_FIELD: String = "flowCaptures"
private const val FLOW_OUT_FIELD: String = "flowOut"
private const val FLOW_CANCEL_FIELD: String = "flowCancel"
private const val FLOW_TASK_ID_FIELD: String = "kotlinTaskId"
private const val FLOW_TASK_CONTROL_FIELD: String = "control"

// Envelope kinds (the ScopeWire kind-tag convention: unknown kinds are ignored
// silently — forward compat, decision 9). Spawn outcomes reuse the same
// vocabulary: ONE `complete` envelope whose `value` key carries the result.
internal const val FLOW_KIND_EMIT: String = "emit"
internal const val FLOW_KIND_COMPLETE: String = "complete"
internal const val FLOW_KIND_ERROR: String = "error"

private const val FLOW_ON_CONTEXT_LAW: String =
    "flowOn(Dispatchers.Task) collection requires a render-thread component context (like runTask)"

// Spec §7 mandate: an escaped suspend in the lifted region must surface as a
// guided error at the collector, never a hang (the task thread has no pump —
// a genuine park there can never resume).
private const val ESCAPED_SUSPEND_LAW: String =
    "a suspend call parked inside a flowOn(Dispatchers.Task)/spawnTask region — only emit/emitAll " +
        "may suspend there (BRS_TASK_SUSPEND_IN_LIFTED); an @Suppress-escaped or FIR-hole call did this"

/** The lift's node factory: a fresh unparented task node per collection (runTask v1 law). */
@BrsInline("return CreateObject(\"roSGNode\", componentName)")
private external fun createFlowTaskNode(componentName: String): RoSGNode

/**
 * [RoSGNodeEvent.getRoSGNode] with an honest return type: on device it returns
 * invalid when the event's node was destroyed between the event being queued
 * and delivered (TaskRunner.taskEventNodeOrNull precedent).
 */
@BrsInline("return event.getRoSGNode()")
private external fun flowEventNodeOrNull(event: RoSGNodeEvent): RoSGNode?

// ---------------------------------------------------------------------------
// Envelope protocol.
//
// Envelope AAs: { kind: "emit"|"complete"|"error", seq: Int, then
// value (emit; also complete for spawn results) or message/number/backtrace
// (error) }. Collectors rely on per-write in-order delivery (device-pinned:
// FieldSemantics case 1; flowOut is alwaysNotify) — seq is carried for
// diagnostics, not reordering.
//
// Builders/parsers are PUBLIC because the stdlib device-test module has no
// friend wiring into this klib (the ScopeWire buildScopeErrorOutcome /
// scopeThrowableForOutcome precedent): the unit suite pins the round trip.
// ---------------------------------------------------------------------------

/** Builds an `emit` envelope carrying one upstream emission. */
public fun buildFlowEmitEnvelope(seq: Int, value: Any?): RoAssociativeArray {
    val aa = RoAssociativeArray.create()
    aa.addReplace("kind", FLOW_KIND_EMIT)
    aa.addReplace("seq", seq)
    aa.addReplace("value", value)
    return aa
}

/**
 * Builds a `complete` envelope. For a flowOn stream [value] is null (the kind
 * alone says "upstream finished"); for a spawnTask outcome [value] carries the
 * block's result — spawn's single outcome envelope IS a complete envelope.
 */
public fun buildFlowCompleteEnvelope(seq: Int, value: Any?): RoAssociativeArray {
    val aa = RoAssociativeArray.create()
    aa.addReplace("kind", FLOW_KIND_COMPLETE)
    aa.addReplace("seq", seq)
    aa.addReplace("value", value)
    return aa
}

/**
 * Builds an `error` envelope. Failures cross the thread boundary as DATA
 * (message/number/backtrace — the TaskException field set); exception TYPES
 * never cross (runTask precedent, spec §5 exceptions law).
 */
public fun buildFlowErrorEnvelope(seq: Int, message: String, number: Int, backtrace: Dynamic?): RoAssociativeArray {
    val aa = RoAssociativeArray.create()
    aa.addReplace("kind", FLOW_KIND_ERROR)
    aa.addReplace("seq", seq)
    aa.addReplace("message", message)
    aa.addReplace("number", number)
    aa.addReplace("backtrace", backtrace)
    return aa
}

/**
 * Builds an `error` envelope from a caught [Throwable] by reading the runtime
 * AA's data keys, never property getters (ScopeWire.scopeErrorInfoFrom
 * precedent): at runtime both Kotlin exception instances and native
 * BrightScript error objects are AAs carrying `message`/`number` (and
 * `backtrace` for native errors).
 */
public fun flowErrorEnvelopeFrom(seq: Int, cause: Throwable): RoAssociativeArray {
    var message = "flow task failed"
    var number = 0
    var backtrace: Dynamic? = null
    val aa = cause as? RoAssociativeArray
    if (aa != null) {
        val rawMessage = aa.lookup("message")
        if (rawMessage != null) {
            message = "$rawMessage"
        }
        val rawNumber = aa.lookup("number")
        if (rawNumber is Int) {
            number = rawNumber
        }
        backtrace = aa.lookup("backtrace")
    }
    return buildFlowErrorEnvelope(seq, message, number, backtrace)
}

/**
 * The collector-side mapping of an `error` envelope to the [TaskException]
 * thrown at the collect/spawn suspend point.
 */
public fun flowTaskExceptionFrom(envelope: RoAssociativeArray): TaskException {
    var message = "flow task failed"
    val rawMessage = envelope.lookup("message")
    if (rawMessage != null) {
        message = "$rawMessage"
    }
    var number = 0
    val rawNumber = envelope.lookup("number")
    if (rawNumber is Int) {
        number = rawNumber
    }
    return TaskException(message, number, envelope.lookup("backtrace"))
}

// ---------------------------------------------------------------------------
// Collector-side registry (ComponentMailbox pattern): this object lives in
// GetGlobalAA scope, which on the render thread is per-COMPONENT-INSTANCE —
// the function-name observer below runs in the observing component's scope
// and therefore resolves the SAME instance the collector registered with.
// Task ids are correlation keys within one component, exactly like
// TaskRunner's (whose allocateTaskId is stdlib-internal — hence this
// klib-local counter; sharing one counter would buy nothing, since the two
// registries never see each other's nodes).
// ---------------------------------------------------------------------------

/** One in-flight lifted collection: its envelope buffer plus the drain's park ticket. */
internal class FlowTaskEntry(internal val channel: FlowChannel<Any?>) {
    private var wake: CompletableJob? = null

    /**
     * A fresh park ticket for the drain to `join()` on. Single-threaded
     * discipline: the drain calls this only after [FlowChannel.poll] answered
     * EMPTY, with no suspension in between, so an envelope cannot slip into
     * the gap.
     */
    fun parkTicket(): CompletableJob {
        val ticket = Job()
        wake = ticket
        return ticket
    }

    /** Wakes a parked drain (once per ticket); no-op when none is parked. */
    fun signal() {
        val ticket = wake
        wake = null
        if (ticket != null) {
            ticket.complete()
        }
    }
}

internal object FlowTaskCollects {
    private val pending = mutableMapOf<Int, FlowTaskEntry>()
    private var lastTaskId = 0

    /** Allocates the next correlation id (ids start at 1; 0 = unassigned). */
    fun allocateId(): Int {
        lastTaskId = lastTaskId + 1
        return lastTaskId
    }

    fun register(taskId: Int, entry: FlowTaskEntry) {
        pending[taskId] = entry
    }

    fun get(taskId: Int): FlowTaskEntry? = pending[taskId]

    fun remove(taskId: Int) {
        pending.remove(taskId)
    }
}

/**
 * Observer callback for `flowOut`. Top-level and in this file on purpose: it
 * is registered by name (function-name observer form), so it must live in a
 * script included wherever collections start — call-dependency tracking
 * guarantees that, since both [taskFlowLifted] (this file) and
 * `spawnTaskLifted` (SpawnTask.kt, which calls into this file) are what
 * collectors call.
 *
 * Routing: `emit` envelopes enqueue; `complete` enqueues then closes clean
 * (the envelope itself still delivers — spawn reads its result off it);
 * `error` closes with the [TaskException]; unknown kinds are ignored silently
 * (forward compat, the ScopeWire decision-9 convention). A registry miss
 * (torn-down collection) drops the delivery — the TaskRunner shape.
 */
internal fun onKotlinFlowTaskOut(event: RoSGNodeEvent) {
    val node = flowEventNodeOrNull(event)
    if (node == null) {
        return
    }
    val taskId = node.getField(FLOW_TASK_ID_FIELD) as? Int
    if (taskId == null) {
        return
    }
    val entry = FlowTaskCollects.get(taskId)
    if (entry == null) {
        return
    }
    val envelope = event.getData() as? RoAssociativeArray
    if (envelope == null) {
        return
    }
    val kind = "${envelope.lookup("kind")}"
    if (kind == FLOW_KIND_EMIT) {
        entry.channel.send(envelope)
    } else if (kind == FLOW_KIND_COMPLETE) {
        entry.channel.send(envelope)
        entry.channel.close(null)
    } else if (kind == FLOW_KIND_ERROR) {
        entry.channel.close(flowTaskExceptionFrom(envelope))
    } else {
        return
    }
    entry.signal()
}

/**
 * One lifted collection's render-side protocol state. [openFlowTaskSession]
 * performs the runTask-shaped setup THROUGH the observer arm; the caller then
 * wraps `start()` + its drain loop in `try { } finally { teardown() }` so
 * every exit — completion, error, caller cancellation, downstream abort —
 * releases the task exactly once (see the cancel-mechanism note atop this
 * file).
 */
internal class FlowTaskSession(
    private val node: RoSGNode,
    private val taskId: Int,
    private val channel: FlowChannel<Any?>,
    private val entry: FlowTaskEntry,
) {
    /** Starts the task (`control="RUN"`); the observer is already armed (arming law). */
    fun start() {
        node.setField(FLOW_TASK_CONTROL_FIELD, "RUN")
    }

    /**
     * The next envelope AA, or [FlowChannel.CLOSED] after a clean close;
     * throws the close cause (the error-envelope [TaskException]) on a failed
     * close. Entry-checks the collecting coroutine's job every iteration and
     * parks cancellation-aware (a fresh ticket's `join()`), so caller cancel
     * wakes a parked drain promptly.
     */
    suspend fun nextEnvelope(): Any? {
        while (true) {
            currentCoroutineContext().ensureActive()
            val polled = channel.poll()
            if (polled === FlowChannel.CLOSED) {
                val cause = channel.closeCause
                if (cause != null) {
                    throw cause
                }
                return FlowChannel.CLOSED
            }
            if (polled !== FlowChannel.EMPTY) {
                return polled
            }
            entry.parkTicket().join()
        }
    }

    /**
     * The spec §5 cancellation sequence: drop the registry entry and queued
     * envelopes, disarm the observer, then — only when the task's own
     * terminal envelope has NOT already closed the channel — write the
     * cooperative cancel field and hard-stop the thread. Both writes are
     * safe/idempotent on an already-terminal node (Probe B7).
     */
    fun teardown() {
        FlowTaskCollects.remove(taskId)
        node.unobserveFieldScoped(FLOW_OUT_FIELD)
        if (!channel.isClosed) {
            node.setField(FLOW_CANCEL_FIELD, true)
            node.setField(FLOW_TASK_CONTROL_FIELD, "STOP")
        }
    }
}

/**
 * The runTask-shaped setup both lift surfaces share: render-context guard →
 * fresh node → captures (ONE deep-copied AA — the field write copies) → task
 * id → registry entry → `flowOut` observer armed. The observer is armed here,
 * strictly BEFORE the caller's `start()` writes `control="RUN"` (arming law:
 * a task-thread write only reaches observers attached when it happened).
 */
internal fun openFlowTaskSession(
    componentName: String,
    captures: RoAssociativeArray?,
    contextLawMessage: String,
): FlowTaskSession {
    if (kotlinAmbientTopOrNull() == null) {
        throw IllegalStateException(contextLawMessage)
    }
    val node = createFlowTaskNode(componentName)
    if (captures != null) {
        node.setField(FLOW_CAPTURES_FIELD, captures)
    }
    val taskId = FlowTaskCollects.allocateId()
    node.setField(FLOW_TASK_ID_FIELD, taskId)
    val channel = FlowChannel<Any?>()
    val entry = FlowTaskEntry(channel)
    FlowTaskCollects.register(taskId, entry)
    node.observeFieldScoped(FLOW_OUT_FIELD, brsName(::onKotlinFlowTaskOut))
    return FlowTaskSession(node, taskId, channel, entry)
}

/** The cold flow [taskFlowLifted] returns: a fresh task node per collection. */
private class TaskLiftedFlow<T>(
    private val componentName: String,
    private val captures: RoAssociativeArray?,
) : Flow<T> {
    override suspend fun collect(collector: FlowCollector<T>) {
        val session = openFlowTaskSession(componentName, captures, FLOW_ON_CONTEXT_LAW)
        try {
            session.start()
            while (true) {
                val received = session.nextEnvelope()
                if (received === FlowChannel.CLOSED) {
                    // Clean close without a complete envelope cannot happen
                    // today (the observer only closes clean AFTER enqueuing
                    // the complete envelope); tolerate it as completion.
                    return
                }
                val envelope = received as RoAssociativeArray
                val kind = "${envelope.lookup("kind")}"
                if (kind == FLOW_KIND_EMIT) {
                    @Suppress("UNCHECKED_CAST")
                    collector.emit(envelope.lookup("value") as T)
                } else if (kind == FLOW_KIND_COMPLETE) {
                    return
                }
                // Anything else was already filtered by the observer; skip.
            }
        } finally {
            session.teardown()
        }
    }
}

/**
 * COMPILER-TARGETED: what a `flowOn(Dispatchers.Task)` call site is rewritten
 * to. Returns the collector-side cold flow that, on each collection, creates a
 * fresh [componentName] task node, sets [captures] on it, arms the envelope
 * observer, runs it, and re-emits the task's envelope stream downstream
 * (runTask-shaped; arming-order law). An `error` envelope rethrows at the
 * collector as [TaskException]; cancellation and downstream aborts stop the
 * task via the spec §5 sequence (see [FlowTaskSession.teardown]).
 *
 * v1 context law: collection requires render-thread component context —
 * guided [IllegalStateException] otherwise (runTask precedent).
 */
internal fun <T> taskFlowLifted(componentName: String, captures: RoAssociativeArray?): Flow<T> =
    TaskLiftedFlow(componentName, captures)

// ---------------------------------------------------------------------------
// Task-thread half.
// ---------------------------------------------------------------------------

/** Outcome slot [FlowDriveCompletion] fills; read by [driveFlowTask] after startCoroutine returns. */
private class FlowDriveOutcome {
    var completed = false
    var cause: Throwable? = null
}

/**
 * Completion continuation for the task-side drive. Empty context: the task
 * thread has no interceptor, so every resumption is inline and a LEGAL lifted
 * body (only emit/emitAll suspend, and the shim never actually parks) runs to
 * completion synchronously inside `startCoroutine`. CoroutineImpl.resumeWith
 * catches everything its doResume throws — Kotlin exceptions and native
 * BrightScript errors both — and routes it here as the failure result.
 */
private class FlowDriveCompletion(private val outcome: FlowDriveOutcome) : Continuation<Unit> {
    override val context: CoroutineContext = EmptyCoroutineContext

    override fun resumeWith(result: Result<Unit>) {
        outcome.completed = true
        outcome.cause = result.exceptionOrNull()
    }
}

/**
 * The envelope-writing shim collector: each upstream emission first checks the
 * cooperative cancel field (truthy → [FlowTaskCancellation], the clean unwind
 * Probe B6 pins — task-side try/finally RUNS on this path), then writes one
 * `emit` envelope. Each `flowOut` write delivers in order cross-thread
 * (device-pinned); alwaysNotify makes equal envelopes deliver too.
 */
private class FlowEmitShim(private val node: RoSGNode) : FlowCollector<Any?> {
    private var seq = 0

    fun nextSeq(): Int {
        seq = seq + 1
        return seq
    }

    override suspend fun emit(value: Any?) {
        if (flowCancelRequested(node)) {
            throw FlowTaskCancellation()
        }
        node.setField(FLOW_OUT_FIELD, buildFlowEmitEnvelope(nextSeq(), value))
    }
}

/**
 * COMPILER-TARGETED: the task-thread driver a synthesized flowOn component's
 * `run()` calls, handing it the REAL task node (`this.top` — the lowering
 * emits `m.top`; `this`/`m` alone is the m-scope AA and every field access
 * through it would silently miss the node, which is why this parameter is
 * node-typed) and the rebuilt upstream chain. Field access is
 * node.setField/getField EXCLUSIVELY for the same reason.
 *
 * Drives `upstream.collect(shim)` synchronously via `startCoroutine` (no
 * interceptor task-side → inline resumption), then writes the terminal
 * envelope FIRST and lets the kotlinTaskState protocol land LAST (a throw
 * from here reaches the generated `__kotlinTaskMain` wrapper, which writes
 * `kotlinTaskError` then `kotlinTaskState="error"` — the TaskRunner order):
 * - completion → `complete` envelope, return (wrapper writes "done");
 * - [FlowTaskCancellation] → clean cancel unwind: the collector is already
 *   gone, so no envelope; return (wrapper writes "done");
 * - any other failure → `error` envelope from the cause, rethrow;
 * - `startCoroutine` returned WITHOUT the completion firing → a suspend call
 *   genuinely parked (spec §7 escaped-suspension backstop): the task thread
 *   has no pump, so it can never resume — write an `error` envelope naming
 *   the law so the collector throws instead of hanging, then throw it
 *   task-side too for the error protocol/console.
 *
 * Also stashes the node in [FlowTaskThreadSlot] (the task thread's OWN
 * GetGlobalAA-scope instance) so `ensureTaskActive()` checkpoints resolve it;
 * the slot dies with the one-shot task thread — no unstash needed.
 */
internal fun driveFlowTask(node: RoSGNode, upstream: Flow<Any?>) {
    FlowTaskThreadSlot.node = node
    if (flowCancelRequested(node)) {
        // Cancelled before the thread even started the chain: exit clean.
        return
    }
    val shim = FlowEmitShim(node)
    val outcome = FlowDriveOutcome()
    val block: suspend () -> Unit = { upstream.collect(shim) }
    block.startCoroutine(FlowDriveCompletion(outcome))
    if (!outcome.completed) {
        node.setField(FLOW_OUT_FIELD, buildFlowErrorEnvelope(shim.nextSeq(), ESCAPED_SUSPEND_LAW, 0, null))
        throw IllegalStateException(ESCAPED_SUSPEND_LAW)
    }
    val cause = outcome.cause
    if (cause == null) {
        node.setField(FLOW_OUT_FIELD, buildFlowCompleteEnvelope(shim.nextSeq(), null))
        return
    }
    if (cause is FlowTaskCancellation) {
        return
    }
    node.setField(FLOW_OUT_FIELD, flowErrorEnvelopeFrom(shim.nextSeq(), cause))
    throw cause
}

/**
 * COMPILER-TARGETED: reads the captures AA a collector-side [taskFlowLifted] /
 * spawnTaskLifted set on the task node, task-side. Node-typed for the same
 * this.top reason as [driveFlowTask].
 */
internal fun readCapturesFrom(node: RoSGNode): RoAssociativeArray? =
    node.getField(FLOW_CAPTURES_FIELD) as? RoAssociativeArray
