/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs

import kotlin.brs.roku.RoSGNode
import kotlin.brs.roku.RoSGNodeEvent
import kotlin.coroutines.Continuation
import kotlin.coroutines.DisposableHandle
import kotlin.coroutines.ParkedContinuation
import kotlin.coroutines.delay.DelayTracker
import kotlin.coroutines.ensureActive
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn
import kotlin.coroutines.pump.PumpScheduler
import kotlin.coroutines.registerCallerCancel

/**
 * Component lifecycle runtime (design of record:
 * docs/superpowers/plans/2026-09-04-component-lifecycle-design.md, §4).
 *
 * ONE file by the include-closure law: the compiler-injected attach call below
 * records the edge that pulls this file into every render component's closure,
 * and the name-registered observer handler added in the gate section must live
 * beside its registration.
 */

/**
 * The ready marker: one boolean XML field on every input-bearing component
 * TYPE (plan B declares it); absent on input-less types, which is how the
 * stdlib learns whether a class has inputs at all (spec §4).
 */
public const val LIFECYCLE_INPUTS_READY_FIELD: String = "__kotlinInputsReady"

/** Bare-named per-component entries the compiler generates (callFunc targets). */
internal const val LIFECYCLE_RETIRE_FUNCTION: String = "__kotlinRetire"
internal const val LIFECYCLE_REVIVE_FUNCTION: String = "__kotlinRevive"

/**
 * One declared dependency of a component (spec §13). Registered during init
 * (spec 2's `by sharedService` delegate is the first registrant); [resolve]
 * may run synchronously inside registration or later from any callback in
 * the component's context. [rearm] runs on every revive: drop cached state,
 * re-attempt resolution, arm for a later resolve on a miss.
 */
internal class DependencyTicket(
    internal val label: String,
    internal val rearm: () -> Unit,
) {
    internal var resolved: Boolean = false

    /** Idempotent. Wakes parked awaitReady callers when the whole gate is open. */
    internal fun resolve() {
        if (resolved) return
        resolved = true
        lifecycleWakeIfReady()
    }
}

/** Per-component-instance state (object singletons live on GetGlobalAA — per instance on the render thread). */
internal object LifecycleRegistry {
    /** Set by the compiler-injected attach as init()'s first statement. */
    internal var attached: Boolean = false
    internal var retired: Boolean = false
    internal var driverClaimed: Boolean = false
    /** Monotonic activation counter; bumps on retire. Watchdog callbacks compare against it. */
    internal var activation: Int = 0
    internal var markerObserved: Boolean = false
    internal var watchdogArmedFor: Int = -1
    internal var watchdogMs: Int = 30_000
    internal var watchdogFires: Int = 0
    internal val tickets: ArrayList<DependencyTicket> = ArrayList()
    internal val parked: ArrayList<ParkedContinuation> = ArrayList()
    internal val retireHooks: ArrayList<() -> Unit> = ArrayList()
}

/**
 * Component-init entry point, injected UNCONDITIONALLY as the first statement
 * of every render component's generated init() (task components excluded).
 * Performs the pump attach internally — this supersedes the coroutine-scan-
 * gated `__kotlinPumpAttach` injection and closes its helper-file hole. Cost:
 * one call, two ref stores; backend resolution and timers stay deferred to
 * the first real wakeup.
 */
@BrsStatic
public fun __kotlinComponentAttach(top: RoSGNode, global: RoSGNode) {
    PumpScheduler.attach(top, global)
    LifecycleRegistry.attached = true
}

// ---------------------------------------------------------------------------
// The gate
// ---------------------------------------------------------------------------

/** Inputs are ready when the class declares none (no marker field) or the marker is true. */
internal fun lifecycleInputsReady(top: RoSGNode): Boolean {
    if (!top.hasField(LIFECYCLE_INPUTS_READY_FIELD)) return true
    // `as? Boolean` (the stdlib getField idiom): a direct `Dynamic? == true`
    // is EQUALITY_NOT_APPLICABLE in K2 (unrelated interface vs final builtin).
    return (top.getField(LIFECYCLE_INPUTS_READY_FIELD) as? Boolean) == true
}

internal fun lifecycleGateOpen(top: RoSGNode): Boolean {
    val reg = LifecycleRegistry
    if (reg.retired) return false
    if (!lifecycleInputsReady(top)) return false
    for (ticket in reg.tickets) {
        if (!ticket.resolved) return false
    }
    return true
}

/** Re-checks the gate and drains every parked awaiter when it is open. */
internal fun lifecycleWakeIfReady() {
    val top = PumpScheduler.hostTopOrNull() ?: return
    if (!lifecycleGateOpen(top)) return
    val reg = LifecycleRegistry
    if (reg.parked.size == 0) return
    val waiters = ArrayList<ParkedContinuation>()
    waiters.addAll(reg.parked)
    reg.parked.clear()
    for (parked in waiters) {
        parked.tryResume(Unit)
    }
}

/**
 * Marker-field observer handler. Registered BY NAME below, so it lives in this
 * file (include-closure law); runs in the observing component's context and
 * therefore resolves that component's registry instance.
 */
internal fun onKotlinInputsReady(event: RoSGNodeEvent) {
    lifecycleWakeIfReady()
}

private fun lifecycleArmMarkerObserver(top: RoSGNode) {
    val reg = LifecycleRegistry
    if (reg.markerObserved) return
    if (!top.hasField(LIFECYCLE_INPUTS_READY_FIELD)) return
    reg.markerObserved = true
    top.observeFieldScoped(LIFECYCLE_INPUTS_READY_FIELD, brsName(::onKotlinInputsReady))
}

/** Once per activation: DelayTracker has no deregistration, so the callback self-checks (registry-miss idiom). */
private fun lifecycleArmWatchdog(top: RoSGNode) {
    val reg = LifecycleRegistry
    if (reg.watchdogArmedFor == reg.activation) return
    val armedFor = reg.activation
    reg.watchdogArmedFor = armedFor
    val ms = reg.watchdogMs
    DelayTracker.current.register(ms.toLong()) {
        val stillCurrent = !reg.retired && reg.activation == armedFor
        if (stillCurrent) {
            if (!lifecycleGateOpen(top)) {
                var unresolved = ""
                for (ticket in reg.tickets) {
                    if (!ticket.resolved) unresolved = unresolved + ticket.label + " "
                }
                var inputs = "set"
                if (!lifecycleInputsReady(top)) {
                    inputs = "UNSET (created outside its Kotlin constructor?)"
                }
                println(
                    "[kotlin.lifecycle] ${top.subtype()}(id=${top.getField("id")}) not ready after " +
                        "${ms / 1000}s — inputs $inputs, unresolved: $unresolved"
                )
                reg.watchdogFires = reg.watchdogFires + 1
            }
        }
    }
}

/** Removes a parked awaiter on cancellation (the caller-cancel path resumes it exceptionally). */
private class LifecycleParkHandle(private val parked: ParkedContinuation) : DisposableHandle {
    override fun dispose() {
        LifecycleRegistry.parked.remove(parked)
    }
}

/**
 * Suspends until the current activation's gate is open: inputs set (or none
 * declared) AND every registered dependency resolved. Re-entrant; returns
 * immediately once open. The compiler-synthesized onStart driver awaits this;
 * a coroutine launched from an observer handler may await it too. Render-
 * thread component context only (guided ISE otherwise). Cancellation of the
 * calling job (retire cancels the scope) wakes a parked call with
 * CancellationException.
 */
public suspend fun ComponentBase.awaitReady() {
    return suspendCoroutineUninterceptedOrReturn { continuation ->
        val top = PumpScheduler.hostTopOrNull()
            ?: throw IllegalStateException(
                "awaitReady must be called from a render-thread component context (like runTask)"
            )
        continuation.context.ensureActive()
        if (lifecycleGateOpen(top)) {
            Unit
        } else {
            @Suppress("UNCHECKED_CAST")
            val parked = ParkedContinuation(continuation as Continuation<Any?>)
            LifecycleRegistry.parked.add(parked)
            parked.handles.add(LifecycleParkHandle(parked))
            lifecycleArmMarkerObserver(top)
            lifecycleArmWatchdog(top)
            registerCallerCancel(parked, continuation.context)
            parked.finish()
        }
    }
}

/**
 * Once-per-activation claim for the compiler-synthesized onStart driver: a
 * concrete base and its concrete subclass both emit a driver call from their
 * inits; the FIRST (the base's — base init runs first) claims and launches,
 * the second returns false. The launched body runs after the whole init
 * cascade, when the leaf's slot attachments are in place, so `onStart()`
 * resolves to the most-derived override. False while retired.
 */
@PublishedApi
internal fun kotlinLifecycleClaimDriver(): Boolean {
    val reg = LifecycleRegistry
    if (reg.retired) return false
    if (reg.driverClaimed) return false
    reg.driverClaimed = true
    return true
}

// ---------------------------------------------------------------------------
// Spec-2 interface (§13): dependency tickets, retire hooks
// ---------------------------------------------------------------------------

/** Init-time only: after the driver fired, a dependency nobody will wait for is a declaration bug. */
internal fun kotlinLifecycleRegisterDependency(label: String, rearm: () -> Unit): DependencyTicket {
    val reg = LifecycleRegistry
    if (reg.driverClaimed) {
        throw IllegalStateException(
            "lifecycle: dependency '$label' registered after onStart was driven — declare dependencies " +
                "during init (by sharedService { } as a property delegate)"
        )
    }
    val ticket = DependencyTicket(label, rearm)
    reg.tickets.add(ticket)
    return ticket
}

/** Runs inside retire after onStop and before scope cancellation (spec 2 disarms doorbell observers here). */
internal fun kotlinLifecycleOnRetire(hook: () -> Unit) {
    LifecycleRegistry.retireHooks.add(hook)
}

internal fun kotlinLifecycleIsRetired(): Boolean = LifecycleRegistry.retired

// ---------------------------------------------------------------------------
// Test hooks (kotlinScopeWatchdog* precedent)
// ---------------------------------------------------------------------------

/** Per-component count of watchdog lines printed. Assert the counter, not console text. */
public fun kotlinLifecycleWatchdogFires(): Int = LifecycleRegistry.watchdogFires

/** Per-component watchdog deadline override (default 30000). */
public fun kotlinLifecycleWatchdogMillis(ms: Int) {
    LifecycleRegistry.watchdogMs = ms
}

// Task 6 replaces these two with the real hop.
public fun retire(node: RoSGNode) {
    throw IllegalStateException("retire: node '${node.getField("id")}' (${node.subtype()}) is not a Kotlin render component — retire/revive target components compiled from ComponentBase subclasses")
}
public fun revive(node: RoSGNode) {
    throw IllegalStateException("revive: node '${node.getField("id")}' (${node.subtype()}) is not a Kotlin render component — retire/revive target components compiled from ComponentBase subclasses")
}
