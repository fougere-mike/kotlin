/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.coroutines.flow

import kotlin.coroutines.CoroutineContext

/**
 * An asynchronous cold stream of values, mirroring `kotlinx.coroutines.flow.Flow`.
 *
 * This lives in the separate `kotlin-flow-brs` klib (NOT the stdlib) because flow
 * operator internals need real suspend state machines, and stdlib compilation
 * (`-Xstdlib-compilation`) generates none — kotlinx-style operator bodies would
 * miscompile silently there (flow-program spec decision 10). This klib compiles in
 * USER mode: `-Xallow-kotlin-package` only.
 *
 * **LAW: cold Flow objects must never cross a component boundary.** A Flow is a
 * class instance wrapping suspend lambdas; every ordinary SceneGraph channel
 * (node/global fields, callFunc args and returns, rtq PostMessage, observer
 * `getData()`) strips function slots silently — the received object is a husk
 * whose first `collect` crashes "Member function not found", while `as? Flow`
 * still passes. Collect a flow in the component that built it; only StateFlow
 * (the hot tier, a later task) is designed to cross components.
 */
// Suppression is sound: the kotlinx-parity pair Flow/flow is case-identical in
// BrightScript, but the interface emits no top-level runtime identifier (it only
// appears as __proto strings) and the builder function's emitted name is
// signature-mangled — they cannot collide. Stdlib precedent: UByteArray.kt.
@Suppress("BRS_NAME_CASE_CLASH")
public interface Flow<out T> {
    public suspend fun collect(collector: FlowCollector<T>)
}

/**
 * The consumer side of a [Flow]: producers emit values into it, mirroring
 * `kotlinx.coroutines.flow.FlowCollector`.
 */
public fun interface FlowCollector<in T> {
    public suspend fun emit(value: T)
}

/**
 * Collects [flow] into this collector: every value the flow emits is re-emitted
 * downstream. The standard way for a `flow {}` body to splice in another flow.
 *
 * Routed through [flowCollectDispatch] (the internal-collect law, Doorbells.kt
 * header): [flow] may be a StateFlow, and only the dispatch path serves those
 * without a cross-component slot call.
 */
public suspend fun <T> FlowCollector<T>.emitAll(flow: Flow<T>) {
    flowCollectDispatch(flow, this)
}

/**
 * COMPILER-LOWERED: moves the upstream chain onto a Roku Task thread (the task
 * lift, flow-program spec §5). At a call site whose argument is the literal
 * `Dispatchers.Task` token, the compiler lifts the ENTIRE upstream chain into a
 * per-call-site synthesized TaskComponent; downstream of `flowOn` collects the
 * task's emissions as an ordinary cold flow in the collector's context.
 *
 * [context] must be LITERALLY `Dispatchers.Task` at the call site, and
 * `Dispatchers.Task` is legal nowhere else — `BRS_FLOW_ON_INVALID_DISPATCHER`
 * enforces both directions (the lift is a compile-time lowering; a
 * runtime-chosen dispatcher cannot select it).
 *
 * This body is the runtime backstop for calls that escaped the lowering
 * (suppressed diagnostics, erased dispatch): it throws a guided error rather
 * than silently collecting upstream on the caller's thread.
 */
public fun <T> Flow<T>.flowOn(context: CoroutineContext): Flow<T> =
    throw IllegalStateException(
        "flowOn compiled without the task lift — this call must be compiler-lowered; " +
            "check the argument is the literal Dispatchers.Task"
    )
