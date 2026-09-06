/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs.scope

import kotlin.brs.BrsInline
import kotlin.brs.brsName
import kotlin.brs.roku.RoArray
import kotlin.brs.roku.RoAssociativeArray
import kotlin.brs.roku.RoSGNode
import kotlin.brs.roku.RoSGNodeEvent
import kotlin.coroutines.Continuation
import kotlin.coroutines.Job
import kotlin.coroutines.ParkedContinuation
import kotlin.coroutines.delay.DelayTracker
import kotlin.coroutines.ensureActive
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn
import kotlin.coroutines.jobImplOf
import kotlin.coroutines.pump.PumpScheduler
import kotlin.coroutines.registerCallerCancel

/**
 * The CHILD half of the ScopeHandle protocol: the per-component "component
 * mailbox" layer (design decision 9 — the request/response protocol is its
 * first client; a future flow program adds envelope kinds without touching
 * carrier, detection, or arming).
 *
 * Everything here is per-component-instance state: `object` singletons live on
 * GetGlobalAA, which is per-COMPONENT-INSTANCE on the render thread
 * (spike-verified) — so each component gets its own key counter, park registry,
 * inbox arming flag, and watchdog bookkeeping, exactly like CoroutineQueue and
 * DelayTracker.
 */
internal object ComponentMailbox {
    /** The response inbox observer for THIS component is installed and armed. */
    internal var inboxInstalled: Boolean = false

    /** One UUID per caller component instance ("" until first allocation). */
    internal var callerUuid: String = ""

    /** Monotonic request ordinal, from 1 (0 = none allocated yet). */
    internal var lastRequestOrdinal: Int = 0

    /** Parked awaits keyed by request key; removed on settle/cancel/outcome. */
    internal val pending = mutableMapOf<String, ParkedContinuation>()

    /** Watchdog deadline for requests from this component (test hook shrinks). */
    internal var watchdogMs: Int = SCOPE_WATCHDOG_MS_DEFAULT

    /** Times the watchdog line printed in this component (test hook reads). */
    internal var watchdogFires: Int = 0
}

// File-private splices (TaskRunner/ComponentCoroutines precedent: BrsInline
// helpers stay private to the file that uses them).
@BrsInline("return event.getRoSGNode()")
private external fun scopeEventNodeOrNull(event: RoSGNodeEvent): RoSGNode?

@BrsInline("return CreateObject(\"roDeviceInfo\").GetRandomUUID()")
private external fun scopeRandomUuid(): String

/**
 * The AMBIENT component's `top` node, or null off-component. Sourced from the
 * pump scheduler's attach state, which received the literal `m.top` at
 * component init (compiler-injected `__kotlinComponentAttach`) or lazily via
 * `componentScope()`/`launch {}` — the only ways a coroutine (and therefore a
 * `ScopeHandle.run` call) can exist in a component. On the main thread the
 * scheduler is never attached, so this is null there — which is exactly the
 * v1 "render-thread components only" law.
 */
internal fun scopeAmbientTopOrNull(): RoSGNode? = PumpScheduler.hostTopOrNull()

/**
 * Allocates the next request key: `"<callerUuid>#<n>"`. Globally unique (two
 * children never collide) and embeds caller identity for the owner's in-flight
 * map and the watchdog line. Public for the unit suite; protocol machinery.
 */
public fun allocateScopeRequestKey(): String {
    if (ComponentMailbox.callerUuid == "") {
        ComponentMailbox.callerUuid = scopeRandomUuid()
    }
    ComponentMailbox.lastRequestOrdinal = ComponentMailbox.lastRequestOrdinal + 1
    return ComponentMailbox.callerUuid + "#" + ComponentMailbox.lastRequestOrdinal
}

/**
 * Installs and arms this component's FIELD-carrier response inbox, once (an
 * rtq-backend child replies on its channel instead — see ScopeRtqBackend.kt).
 * Armed strictly BEFORE the component's first request post can produce a
 * reply (arming-order law: an observer attached after a write never sees it).
 * A node that is also a FIELD-backend scope OWNER already has the field —
 * addField is then a no-op and the outcome observer stacks alongside the
 * owner's inbox observer (scoped observers on one field stack; each handler
 * ignores the other's kinds). An rtq-backend owner never declared the field,
 * so a field-forced child role installs it fresh here.
 */
internal fun ensureScopeChildInbox(top: RoSGNode) {
    if (ComponentMailbox.inboxInstalled) {
        return
    }
    top.addField(SCOPE_INBOX_FIELD, "assocarray", true)
    top.observeFieldScoped(SCOPE_INBOX_FIELD, brsName(::onKotlinScopeOutcome))
    ComponentMailbox.inboxInstalled = true
}

/**
 * Observer for outcome envelopes on the CHILD's inbox (function-name observer
 * form: runs in the registering — child — component's context).
 */
internal fun onKotlinScopeOutcome(event: RoSGNodeEvent) {
    // Destroyed-node guard (TaskRunner precedent): a delivery can outlive its
    // node; getRoSGNode() then returns invalid.
    val node = scopeEventNodeOrNull(event)
    if (node == null) {
        return
    }
    val payload = event.getData() as? RoAssociativeArray
    if (payload == null) {
        return
    }
    val env = parseScopeEnvelope(payload)
    if (env.kind != SCOPE_KIND_OUTCOME) {
        // Unknown kinds — and owner-role traffic on a dual-role node — are
        // someone else's or nobody's; ignore silently (decision 9).
        return
    }
    deliverScopeOutcome(env)
}

/**
 * Carrier-agnostic outcome delivery (the field observer above and the rtq
 * handler in ScopeRtqBackend.kt both land here). Late or duplicate deliveries
 * drop via registry miss; a park already settled by cancellation ignores the
 * resume (settle-once guard).
 */
internal fun deliverScopeOutcome(env: ScopeEnvelope) {
    val parked = ComponentMailbox.pending.remove(env.key)
    if (parked == null) {
        return
    }
    val failure = scopeThrowableForOutcome(env)
    if (failure == null) {
        parked.tryResume(env.value)
    } else {
        parked.tryResumeException(failure)
    }
}

/**
 * The child-side engine behind every `ScopeHandle.run(...)` overload: posts a
 * request envelope to [ownerNode] — over the carrier its advertisement
 * declares — and parks until the outcome arrives (or dispatches locally on
 * the same-component fast path). Tail-delegating suspension with the entry
 * check inside the intrinsic block (Job.join / TaskRunner.awaitCompletion
 * shape).
 */
internal suspend fun <R> postScopeRequestAndAwait(
    ownerNode: RoSGNode,
    name: String,
    args: RoArray?,
    captures: RoAssociativeArray?,
): R {
    return suspendCoroutineUninterceptedOrReturn { continuation ->
        continuation.context.ensureActive()
        val ambientTop = scopeAmbientTopOrNull()
        if (ambientTop == null) {
            throw IllegalStateException(
                "ScopeHandle.run must be called from a render-thread component context (like runTask)"
            )
        }
        @Suppress("UNCHECKED_CAST")
        val parked = ParkedContinuation(continuation as Continuation<Any?>)
        if (ambientTop.isSameNode(ownerNode)) {
            // Same-component fast path: local dispatch, NO mailbox round-trip.
            // Args and captures still cross by copy (deepCopyAA) for
            // wire-identical semantics on both surfaces.
            dispatchScopeRequestLocally(parked, continuation.context, ownerNode, name, args, captures)
        } else {
            val ad = ownerNode.getField(SCOPE_AD_FIELD)
            var adValue = ""
            if (ad != null) {
                adValue = "$ad"
            }
            if (adValue == "") {
                throw IllegalStateException(
                    "node '${ownerNode.getField("id")}' has not exposed a scope: " +
                        "call exposeScope() in the owner component before minting handles"
                )
            }
            // Reply-address duality: transport TO the owner comes from the
            // owner's ADVERTISEMENT; the reply address comes from THIS
            // component's resolved backend — so mixed-backend pairs (local
            // force hooks) interoperate in both directions.
            val ownerChannel = scopeAdChannelId(adValue)
            var replyChannel = ""
            if (resolveScopeBackend() == SCOPE_BACKEND_RTQ) {
                // Child channel registered STRICTLY before the first post
                // that could produce a reply (registration-before-
                // advertisement law, child side).
                replyChannel = ensureScopeRtqChannel()
            }
            if (replyChannel == "") {
                // Field floor (and the defensive queue-unavailable fallback):
                // child inbox armed before this first post can produce a reply.
                ensureScopeChildInbox(ambientTop)
            }
            val key = allocateScopeRequestKey()
            ComponentMailbox.pending[key] = parked
            // Cancel wiring: cleanup handle FIRST (registry remove +
            // best-effort cancel post), then the caller-cancel wakeup — so
            // protocol state is gone by the time the park wakes with the CE
            // and the late outcome finds nothing (TaskRunner Stage 1 shape).
            val jobImpl = jobImplOf(continuation.context[Job])
            if (jobImpl != null) {
                parked.handles.add(jobImpl.invokeOnCancelRequest {
                    ComponentMailbox.pending.remove(key)
                    postScopeEnvelopeToOwner(ownerNode, ownerChannel, buildScopeCancelEnvelope(key))
                })
            }
            registerCallerCancel(parked, continuation.context)
            // Once-per-request watchdog: DelayTracker has no deregistration,
            // so a settled request makes the deadline callback a registry-miss
            // no-op. Never re-arms.
            val watchdogMs = ComponentMailbox.watchdogMs
            val ownerSubtype = ownerNode.subtype()
            val ownerId = "${ownerNode.getField("id")}"
            val callerSubtype = ambientTop.subtype()
            DelayTracker.current.register(watchdogMs.toLong()) {
                if (ComponentMailbox.pending.containsKey(key)) {
                    println(
                        "[kotlin.coroutines] ScopeHandle request $key to owner " +
                            "$ownerSubtype(id=$ownerId) still pending after ${watchdogMs / 1000}s " +
                            "(caller: $callerSubtype) — owner torn down without close()?"
                    )
                    ComponentMailbox.watchdogFires = ComponentMailbox.watchdogFires + 1
                }
            }
            if (replyChannel != "") {
                postScopeEnvelopeToOwner(
                    ownerNode,
                    ownerChannel,
                    buildScopeChannelRequestEnvelope(key, replyChannel, name, args, captures)
                )
            } else {
                postScopeEnvelopeToOwner(
                    ownerNode,
                    ownerChannel,
                    buildScopeRequestEnvelope(key, ambientTop, name, args, captures)
                )
            }
        }
        parked.finish()
    }
}
