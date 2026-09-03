/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// The StateFlow static access layer + global-node doorbell machinery
// (flow-program spec §6).
//
// ALL StateFlow behavior lives HERE as top-level statics, operating on
// StateFlowImpl's plain data. Interface-receiver member/accessor calls are
// fn-slot dispatch on this backend — they record no include-closure
// dependency, and on a shared-VM-held flow the slot fn-ref would cross the
// SetRef graph on the disclaimed path the SharedService program forbids. So:
//
//  - user call sites (`sf.value`, `msf.value = x`, member `collect`) are
//    REWRITTEN to these statics by BrsFlowAccessLowering;
//  - every flow-klib-internal upstream collection routes through
//    [flowCollectDispatch] instead of member collect (the cold-flow member
//    path survives only for local objects, and only inside this package —
//    the lowering's package guard documents why).
//
// INCLUDE-CLOSURE LAW: [onKotlinFlowDoorbell] is registered BY NAME
// (function-name observer form), so it must live in the SAME file as the
// registration call ([FlowDoorbells.register]) and the statics collectors
// call — one file, one static dependency edge, pulled transitively into every
// component that touches a StateFlow (the onKotlinFlowTaskOut precedent).
//
// THE CARRIER (Probe A pinned facts, spikes/flow-spike/FINDINGS.md): a
// runtime-addField integer field `__kotlinFlow_<uuid>` on the GLOBAL node,
// alwaysNotify=true, observed via observeFieldScoped. Every component reaches
// its own m.global natively, so no node handle ever crosses the SetRef graph;
// per-write in-order delivery, observer handlers run in the OBSERVER's
// context, observers stack, and observer-component death auto-detaches.
package kotlin.coroutines.flow

import kotlin.brs.BrsInline
import kotlin.brs.brsName
import kotlin.brs.roku.RoSGNode
import kotlin.brs.roku.RoSGNodeEvent
import kotlin.coroutines.CompletableJob
import kotlin.coroutines.Job
import kotlin.coroutines.pump.kotlinAmbientGlobalOrNull

private const val DOORBELL_FIELD_PREFIX: String = "__kotlinFlow_"

// The guided v1 context laws (spec §6). The emit message is pinned by the
// stdlib FlowState suite; keep the "render-thread" fragment stable.
private const val STATE_FLOW_EMIT_LAW: String =
    "StateFlow emits require a render-thread component context; reads work anywhere"

private const val STATE_FLOW_COLLECT_LAW: String =
    "StateFlow collection requires a render-thread component context (like runTask); reads work anywhere"

/** Doorbell identity mint (the ComponentMailbox/ScopeRtqBackend splice). */
@BrsInline("return CreateObject(\"roDeviceInfo\").GetRandomUUID()")
private external fun flowRandomUuid(): String

// ---------------------------------------------------------------------------
// The per-component doorbell registry (ComponentMailbox pattern): this object
// lives in GetGlobalAA scope, which on the render thread is per-COMPONENT-
// INSTANCE — the function-name observer below runs in the observing
// component's scope and resolves the SAME instance its collectors registered
// with. One entry per doorbell FIELD per component; the entry refcounts this
// component's collectors so the scoped observer is armed on the first and
// disarmed on the last (observeFieldScoped/unobserveFieldScoped are
// per-component, so the refcount's scope matches the observer's).
// ---------------------------------------------------------------------------

/** One doorbell field's collect state in THIS component: refcount + parked drains. */
internal class FlowDoorbellEntry {
    /** Registered collectors in this component (observer armed while > 0). */
    internal var collectors: Int = 0

    private val parked = ArrayList<CompletableJob>()

    /**
     * A fresh cancellation-aware park ticket (the FlowTaskEntry pattern:
     * `Job().join()` wakes promptly on caller cancel). Single-threaded
     * discipline: the collect loop only parks after comparing the live value
     * with no suspension in between, so an emit cannot slip into the gap.
     */
    fun parkTicket(): CompletableJob {
        val ticket = Job()
        parked.add(ticket)
        return ticket
    }

    /** Drops a ticket that was cancelled mid-park; no-op for signalled tickets. */
    fun dropTicket(ticket: CompletableJob) {
        parked.remove(ticket)
    }

    /** Wakes every parked collector (each reads the live value on resume). */
    fun signalAll() {
        while (parked.isNotEmpty()) {
            val ticket = parked.removeAt(parked.size - 1)
            ticket.complete()
        }
    }
}

internal object FlowDoorbells {
    private val entries = mutableMapOf<String, FlowDoorbellEntry>()

    /** Registers one collector; arms this component's scoped observer on the first. */
    fun register(field: String, global: RoSGNode): FlowDoorbellEntry {
        var entry = entries[field]
        if (entry == null) {
            entry = FlowDoorbellEntry()
            entries[field] = entry
        }
        entry.collectors = entry.collectors + 1
        if (entry.collectors == 1) {
            global.observeFieldScoped(field, brsName(::onKotlinFlowDoorbell))
        }
        return entry
    }

    /** Deregisters one collector; the last one disarms the scoped observer. */
    fun deregister(field: String, global: RoSGNode) {
        val entry = entries[field]
        if (entry == null) {
            return
        }
        entry.collectors = entry.collectors - 1
        if (entry.collectors <= 0) {
            entries.remove(field)
            global.unobserveFieldScoped(field)
        }
    }

    fun get(field: String): FlowDoorbellEntry? = entries[field]
}

/**
 * Observer callback for the doorbell fields. Top-level and in this file on
 * purpose (include-closure law, file header). A registry miss — every
 * collector in this component already deregistered, or a doorbell some OTHER
 * component's collectors registered — drops the delivery (each component's
 * observer resolves its own per-component registry instance).
 */
internal fun onKotlinFlowDoorbell(event: RoSGNodeEvent) {
    val entry = FlowDoorbells.get(event.getField())
    if (entry == null) {
        return
    }
    entry.signalAll()
}

// ---------------------------------------------------------------------------
// The static access layer (the BrsFlowAccessLowering rewrite targets).
// ---------------------------------------------------------------------------

/** Unwraps a flow to its [StateFlowImpl], through the read-only view; null for cold flows and foreign impls. */
internal fun <T> stateFlowImplOf(flow: Flow<T>): StateFlowImpl<T>? {
    @Suppress("UNCHECKED_CAST")
    val impl = flow as? StateFlowImpl<T>
    if (impl != null) return impl
    @Suppress("UNCHECKED_CAST")
    val view = flow as? ReadonlyStateFlow<T>
    if (view != null) return view.source
    return null
}

/**
 * REWRITE TARGET for `StateFlow.value` GETS. A plain data read — works
 * anywhere (any thread, no component context), always the live value.
 */
internal fun <T> stateFlowGetValue(flow: StateFlow<T>): T {
    val impl = stateFlowImplOf(flow)
    if (impl == null) {
        // A foreign StateFlow implementation: interface accessor dispatch,
        // same-component-local by the cold-flows law. This member access is
        // exempt from the access rewrite (the lowering's package guard).
        return flow.value
    }
    return impl.stored
}

/**
 * REWRITE TARGET for `MutableStateFlow.value` SETS — the emit protocol
 * (spec §6, order is law): equality gate → render-context guard → store →
 * lazy doorbell bind → ring (setField of the incremented version).
 *
 * GUARD BEFORE STORE: an off-context emit throws the guided ISE and must NOT
 * mutate a value collectors were never told about (pinned by the stdlib
 * FlowState suite's unchanged-after-failed-emit test).
 */
internal fun <T> stateFlowSetValue(flow: MutableStateFlow<T>, value: T) {
    val impl = stateFlowImplOf(flow)
    if (impl == null) {
        // Foreign implementation: its own setter, same-component-local
        // (package-guard-exempt member access, as in stateFlowGetValue).
        flow.value = value
        return
    }
    if (value == impl.stored) {
        return
    }
    val global = kotlinAmbientGlobalOrNull()
    if (global == null) {
        throw IllegalStateException(STATE_FLOW_EMIT_LAW)
    }
    impl.stored = value
    bindDoorbell(impl, global)
    impl.version = impl.version + 1
    global.setField(DOORBELL_FIELD_PREFIX + impl.uuid, impl.version)
}

/**
 * REWRITE TARGET for interface-typed `Flow.collect(collector)` member calls,
 * and the ONLY path flow-klib internals use to collect a possibly-cross-
 * component upstream: a StateFlow routes to the doorbell protocol; anything
 * else takes the member path — safe, because cold/operator Flow objects are
 * always same-component-local (the cold-flows-never-cross law, Flow.kt).
 */
internal suspend fun <T> flowCollectDispatch(flow: Flow<T>, collector: FlowCollector<T>) {
    val impl = stateFlowImplOf(flow)
    if (impl != null) {
        stateFlowCollectData(impl, collector)
        return
    }
    flow.collect(collector)
}

/**
 * The StateFlow collect protocol (spec §6): guard → bind if unbound → deliver
 * the CURRENT value (late joiners see current state — kotlinx contract) →
 * loop { deliver the live value unless `==` last-delivered, else park until
 * the doorbell rings }.
 *
 * Conflation is correct by construction: a slow collector wakes and reads
 * whatever is newest (reading newer than the doorbell that fired is StateFlow
 * semantics, not a race), and the compare-before-park loop closes the
 * emit-while-delivering window — a value that changed while the collector was
 * busy is delivered on the next iteration WITHOUT parking. NEVER completes
 * normally (kotlinx contract): it ends only by cancellation, which wakes a
 * parked drain promptly (the ticket's `join` is cancellation-aware) and
 * propagates the CancellationException — `onCompletion`/`finally` in the
 * collector see it; teardown (ticket drop, deregister, last-collector
 * unobserve) runs in the finally on every exit.
 */
internal suspend fun <T> stateFlowCollectData(flow: StateFlowImpl<T>, collector: FlowCollector<T>) {
    val global = kotlinAmbientGlobalOrNull()
    if (global == null) {
        throw IllegalStateException(STATE_FLOW_COLLECT_LAW)
    }
    bindDoorbell(flow, global)
    val field = DOORBELL_FIELD_PREFIX + flow.uuid
    val entry = FlowDoorbells.register(field, global)
    var ticket: CompletableJob? = null
    try {
        var lastDelivered = flow.stored
        collector.emit(lastDelivered)
        while (true) {
            val current = flow.stored
            if (current != lastDelivered) {
                lastDelivered = current
                collector.emit(current)
            } else {
                val fresh = entry.parkTicket()
                ticket = fresh
                fresh.join()
                ticket = null
            }
        }
    } finally {
        val parked = ticket
        if (parked != null) {
            entry.dropTicket(parked)
        }
        FlowDoorbells.deregister(field, global)
    }
}

/**
 * Lazily binds the doorbell: mints the uuid and adds the global-node field
 * (`integer`, alwaysNotify=true — equal version writes still deliver,
 * Probe A). Idempotent; called under the render-context guard by emit and
 * collect alike, so an unbound flow binds on whichever happens first.
 */
private fun bindDoorbell(flow: StateFlowImpl<*>, global: RoSGNode) {
    if (flow.uuid != "") {
        return
    }
    val uuid = flowRandomUuid()
    flow.uuid = uuid
    global.addField(DOORBELL_FIELD_PREFIX + uuid, "integer", true)
}
