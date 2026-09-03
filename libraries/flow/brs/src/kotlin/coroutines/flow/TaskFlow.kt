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
// runLowered convention).
package kotlin.coroutines.flow

import kotlin.brs.roku.RoAssociativeArray
import kotlin.brs.roku.RoSGNode

/**
 * COMPILER-TARGETED: what a `flowOn(Dispatchers.Task)` call site is rewritten
 * to. Returns the collector-side cold flow that, on each collection, creates a
 * fresh [componentName] task node, sets [captures] on it, arms the envelope
 * observer, runs it, and re-emits the task's envelope stream downstream
 * (runTask-shaped; arming-order law).
 *
 * Real body lands in Task 8; until then any call that somehow reaches this
 * stub fails guided rather than silently mis-collecting.
 */
internal fun <T> taskFlowLifted(componentName: String, captures: RoAssociativeArray?): Flow<T> =
    throw IllegalStateException("not yet implemented — Task 8")

/**
 * COMPILER-TARGETED: the task-thread driver a synthesized flowOn component's
 * `run()` calls, handing it the REAL task node (`this.top` — the lowering
 * emits `m.top`; `this`/`m` alone is the m-scope AA and every field access
 * through it would silently miss the node, which is why this parameter is
 * node-typed) and the rebuilt upstream chain. Evaluates the chain with the
 * envelope-writing shim collector (emit/complete/error, cancel checks).
 *
 * Real body lands in Task 8.
 */
internal fun driveFlowTask(node: RoSGNode, upstream: Flow<Any?>): Unit =
    throw IllegalStateException("not yet implemented — Task 8")

/**
 * COMPILER-TARGETED: reads the captures AA a collector-side [taskFlowLifted] /
 * spawnTaskLifted set on the task node, task-side. Node-typed for the same
 * this.top reason as [driveFlowTask].
 *
 * Real body lands in Task 8.
 */
internal fun readCapturesFrom(node: RoSGNode): RoAssociativeArray? =
    throw IllegalStateException("not yet implemented — Task 8")
