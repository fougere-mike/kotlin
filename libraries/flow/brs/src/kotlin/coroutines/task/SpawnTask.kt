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
// runLowered convention).

/**
 * COMPILER-TARGETED: what a `spawnTask { }` call site is rewritten to. Creates
 * a fresh [componentName] task node, sets [captures] on it, arms the outcome
 * observer, runs it, and suspends until the outcome envelope (rethrowing a
 * task-side throw as TaskException; cancellation-aware await).
 *
 * Real body lands in Task 8; until then any call that somehow reaches this
 * stub fails guided rather than silently running on the caller's thread.
 */
internal suspend fun <R> spawnTaskLifted(componentName: String, captures: RoAssociativeArray?): R =
    throw IllegalStateException("not yet implemented — Task 8")

/**
 * COMPILER-TARGETED: the task-thread driver a synthesized spawnTask
 * component's `run()` calls, handing it the REAL task node (`this.top` — the
 * lowering emits `m.top`; `this`/`m` alone is the m-scope AA and every field
 * access through it would silently miss the node, which is why this parameter
 * is node-typed) and the lifted block. Reads the captures off the node, runs
 * [block] with them, and writes the outcome envelope.
 *
 * Real body lands in Task 8.
 */
internal fun driveSpawnTask(node: RoSGNode, block: (RoAssociativeArray?) -> Any?): Unit =
    throw IllegalStateException("not yet implemented — Task 8")
