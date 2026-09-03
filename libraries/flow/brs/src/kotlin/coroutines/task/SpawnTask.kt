/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// The one-shot task-lift entry point (flow-program spec §5, decision 3). Lives in
// the flow klib — the lift machinery ships with the flow program — but in the
// stdlib's kotlin.coroutines.task package, next to runTask, the sibling it
// supersedes withContext(Dispatchers.IO) alongside. Generated basename
// (SpawnTaskKt.brs) is unique across the stdlib and flow runtime JARs — the KGP
// merged-staging collision guard enforces this at packaging time.
package kotlin.coroutines.task

import kotlin.brs.roku.RoAssociativeArray
import kotlin.brs.roku.RoSGNode
import kotlin.coroutines.flow.FLOW_KIND_COMPLETE
import kotlin.coroutines.flow.FlowChannel
import kotlin.coroutines.flow.buildFlowCompleteEnvelope
import kotlin.coroutines.flow.flowErrorEnvelopeFrom
import kotlin.coroutines.flow.openFlowTaskSession

// FlowTaskComponent protocol field names (private duplicates, the
// TaskRunner-style per-file consts; the authoritative declarations are on
// SceneComponent.kt's FlowTaskComponent).
private const val SPAWN_CAPTURES_FIELD: String = "flowCaptures"
private const val SPAWN_OUT_FIELD: String = "flowOut"
private const val SPAWN_CANCEL_FIELD: String = "flowCancel"

private const val SPAWN_CONTEXT_LAW: String =
    "spawnTask requires a render-thread component context (like runTask)"

/**
 * COMPILER-LOWERED: runs [block] on a Roku Task thread and suspends the caller
 * until its outcome envelope arrives (the same per-call-site lift as
 * `flowOn(Dispatchers.Task)`, flow-program spec §5). The block is NON-suspend
 * by design — the platform name states the synchronous-block contract honestly
 * (spec decision 3): blocking I/O is the point of being there. Captures cross
 * by copy, marshallable-only; a task-side throw rethrows as `TaskException` at
 * the suspend point; the await is cancellation-aware.
 *
 * The lowering requires a LITERAL lambda at the call site (a stored function
 * value has no body to lift). This body is the runtime backstop for calls that
 * escaped the lowering: it throws a guided error rather than silently running
 * the block on the caller's thread.
 */
public suspend fun <R> spawnTask(block: () -> R): R =
    throw IllegalStateException(
        "spawnTask compiled without the task lift — this call must be compiler-lowered; " +
            "check the argument is a literal lambda"
    )

// KEEP SIGNATURES IN SYNC with the task-lift lowering
// (compiler/ir/backend.brightscript/src/.../lower/BrsFlowTaskLiftLowering.kt):
// [spawnTaskLifted] is its spawnTask call-site rewrite target, and
// [driveSpawnTask] is what the synthesized component's run() calls — the
// lowering builds those calls against these exact shapes (the runTaskImpl /
// runLowered convention). The mangled names are pinned by the spawnTaskLift
// golden: a signature change here trips the golden gate loudly, by design.

/**
 * Cooperative-cancellation unwind signal for lifted task bodies (spec §5,
 * Probe B6): thrown by the shim's per-emission check, [ensureTaskActive]
 * checkpoints, and caught by the task drivers as a CLEAN exit — task-side
 * try/finally on the unwind path RUNS (unlike a hard `control="STOP"` kill,
 * which skips everything — Probe B8). A plain RuntimeException, not a
 * CancellationException: there are no jobs on the task thread; this is pure
 * control flow between the shim/checkpoint and the driver. Task-side
 * `catch (e: Exception)` swallowing it breaks cooperative cancel exactly as
 * broadly-catching kotlinx code breaks `ensureActive` — same caveat, same
 * answer (catch narrowly or rethrow).
 */
internal class FlowTaskCancellation : RuntimeException("flow task cancelled")

/**
 * The task thread's stashed task node, resolved by [ensureTaskActive].
 *
 * A Kotlin `object` lives in GetGlobalAA scope — per-thread (per component
 * instance on the render thread; per task thread on a Task node). The drivers
 * stash the node at entry, so a lifted body's `ensureTaskActive()` finds the
 * node of ITS OWN run. Never cleared: the slot dies with the one-shot task
 * thread, and on the render thread (where no driver ever runs) it stays null
 * forever — which is what makes [ensureTaskActive] a safe no-op there.
 */
internal object FlowTaskThreadSlot {
    var node: RoSGNode? = null
}

/** Truthy read of the cooperative cancel field ([SPAWN_CANCEL_FIELD]) on [node]. */
internal fun flowCancelRequested(node: RoSGNode): Boolean {
    val cancelled = node.getField(SPAWN_CANCEL_FIELD) as? Boolean
    return cancelled == true
}

/**
 * Task-side cancellation checkpoint (kotlinx `ensureActive` parity for the
 * task world, spec §5 cooperative layer): inside a lifted
 * `flowOn(Dispatchers.Task)` / `spawnTask {}` region, throws the internal
 * cooperative-unwind signal when the collector has cancelled — giving long
 * NON-emitting compute a clean exit point (`finally` runs; Probe B6). Without
 * checkpoints, a cancelled region ends at its next `emit` — or at the hard
 * `control="STOP"` kill.
 *
 * No-op when no lifted-task node is stashed for this thread — in particular,
 * inside a plain `runTask` body (recorded deviation (c): plain TaskComponents
 * declare no cooperative-cancel field, so there is nothing to check; runTask
 * cancellation is the hard STOP alone) and anywhere on the render thread.
 */
public fun ensureTaskActive() {
    val node = FlowTaskThreadSlot.node
    if (node == null) {
        return
    }
    if (flowCancelRequested(node)) {
        throw FlowTaskCancellation()
    }
}

/**
 * COMPILER-TARGETED: what a `spawnTask { }` call site is rewritten to. Creates
 * a fresh [componentName] task node, sets [captures] on it, arms the outcome
 * observer, runs it, and suspends until the outcome envelope: the block's
 * result rides a single `complete` envelope's `value` key; a task-side throw
 * arrives as an `error` envelope and rethrows here as
 * [TaskException] (message/number/backtrace — failures cross as DATA). The
 * await is cancellation-aware and shares the flowOn drain machinery: caller
 * cancel wakes the park and the finally runs the spec §5 sequence
 * (disarm → drop → `flowCancel=true` → `control="STOP"`).
 *
 * v1 context law: render-thread component context only — guided
 * [IllegalStateException] otherwise (runTask precedent).
 */
internal suspend fun <R> spawnTaskLifted(componentName: String, captures: RoAssociativeArray?): R {
    val session = openFlowTaskSession(componentName, captures, SPAWN_CONTEXT_LAW)
    try {
        session.start()
        while (true) {
            val received = session.nextEnvelope()
            if (received === FlowChannel.CLOSED) {
                // Unreachable with a well-formed driver (close follows the
                // complete envelope; error closes with a cause that threw
                // above) — fail guided rather than spin.
                throw IllegalStateException(
                    "spawnTask completed without an outcome envelope — flow-task protocol violation"
                )
            }
            val envelope = received as RoAssociativeArray
            val kind = "${envelope.lookup("kind")}"
            if (kind == FLOW_KIND_COMPLETE) {
                @Suppress("UNCHECKED_CAST")
                return envelope.lookup("value") as R
            }
            // An emit envelope from a spawn component is protocol misuse;
            // skip it (unknown-kind tolerance).
        }
    } finally {
        session.teardown()
    }
}

/**
 * COMPILER-TARGETED: the task-thread driver a synthesized spawnTask
 * component's `run()` calls, handing it the REAL task node (`this.top` — the
 * lowering emits `m.top`; `this`/`m` alone is the m-scope AA and every field
 * access through it would silently miss the node, which is why this parameter
 * is node-typed) and the lifted block. Reads the captures off the node, runs
 * [block] with them, and writes the outcome envelope FIRST — the
 * kotlinTaskState protocol lands LAST (a rethrow reaches the generated
 * `__kotlinTaskMain` wrapper, which writes `kotlinTaskError` then
 * `kotlinTaskState="error"`; a clean return gets "done").
 *
 * [FlowTaskCancellation] (an [ensureTaskActive] checkpoint fired) is a CLEAN
 * exit: the collector is already gone — no envelope, plain return. No
 * escaped-suspension backstop here: the block is non-suspend by FIR law
 * (`BRS_TASK_SUSPEND_IN_LIFTED`), so there is no coroutine to park. Also
 * stashes the node in [FlowTaskThreadSlot] for checkpoint resolution (the
 * slot dies with the one-shot thread).
 */
internal fun driveSpawnTask(node: RoSGNode, block: (RoAssociativeArray?) -> Any?) {
    FlowTaskThreadSlot.node = node
    if (flowCancelRequested(node)) {
        // Cancelled before the thread even started the block: exit clean.
        return
    }
    val captures = node.getField(SPAWN_CAPTURES_FIELD) as? RoAssociativeArray
    val result: Any?
    try {
        result = block(captures)
    } catch (e: Throwable) {
        // Throwable + manual discrimination (the Terminals.kt convention):
        // `catch e` catches Kotlin exceptions AND native BrightScript error
        // objects alike; both cross as error-envelope DATA.
        if (e is FlowTaskCancellation) {
            return
        }
        node.setField(SPAWN_OUT_FIELD, flowErrorEnvelopeFrom(1, e))
        throw e
    }
    node.setField(SPAWN_OUT_FIELD, buildFlowCompleteEnvelope(1, result))
}
