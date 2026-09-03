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
