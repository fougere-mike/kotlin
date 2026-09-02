/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.coroutines.flow

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.ensureActive
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn

/**
 * The collecting coroutine's context, read off the current continuation.
 *
 * The BRS stdlib declares no `kotlin.coroutines.coroutineContext` intrinsic val
 * (and this backend never inlines dependency-klib inline functions, so a
 * stdlib-side one could not be intrinsified at flow-klib call sites either).
 * Instead the block passed to [suspendCoroutineUninterceptedOrReturn] returns
 * `continuation.context` directly — a synchronous return, never a suspension:
 * the stdlib's compiled intrinsic body hands the block this call's own
 * continuation, whose context IS the collecting coroutine's context.
 */
internal suspend fun currentCoroutineContext(): CoroutineContext =
    suspendCoroutineUninterceptedOrReturn { continuation -> continuation.context }

/**
 * Wraps the downstream collector for the [flow] builder: every emit first
 * entry-checks the collecting coroutine's job (the every-suspend-point law).
 * A cancelled collector therefore stops a producer loop at its next emit, and
 * the CancellationException unwinds through the producer body's try/finally
 * normally.
 */
private class SafeCollector<T>(private val downstream: FlowCollector<T>) : FlowCollector<T> {
    override suspend fun emit(value: T) {
        currentCoroutineContext().ensureActive()
        downstream.emit(value)
    }
}

/** The cold flow the [flow] builder returns: [block] re-executes per collect. */
private class SafeFlow<T>(private val block: suspend FlowCollector<T>.() -> Unit) : Flow<T> {
    override suspend fun collect(collector: FlowCollector<T>) {
        block(SafeCollector(collector))
    }
}

/**
 * Builds a cold [Flow]: [block] runs anew for every [Flow.collect] call, entirely
 * inside the collecting coroutine, emitting values into the downstream collector.
 */
// BRS_NAME_CASE_CLASH vs `interface Flow`: sound to suppress — see Flow.kt.
@Suppress("BRS_NAME_CASE_CLASH")
public fun <T> flow(block: suspend FlowCollector<T>.() -> Unit): Flow<T> = SafeFlow(block)

/**
 * A cold flow of the given values, in order. The values are snapshotted into a
 * list ONCE at construction — later mutation of a shared vararg array cannot
 * change what the flow emits.
 */
public fun <T> flowOf(vararg values: T): Flow<T> {
    val snapshot = values.toList()
    return flow {
        for (value in snapshot) {
            emit(value)
        }
    }
}

/** A cold flow that emits this iterable's elements, in iteration order, per collect. */
public fun <T> Iterable<T>.asFlow(): Flow<T> {
    val source = this
    return flow {
        for (value in source) {
            emit(value)
        }
    }
}
