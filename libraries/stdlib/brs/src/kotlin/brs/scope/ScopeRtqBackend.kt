/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs.scope

import kotlin.brs.BrsInline
import kotlin.brs.brsName
import kotlin.brs.roku.RoAssociativeArray
import kotlin.brs.roku.RoSGNode
import kotlin.coroutines.pump.PumpScheduler
import kotlin.coroutines.pump.RoRenderThreadQueue

/**
 * The roRenderThreadQueue FAST-PATH CARRIER for the ScopeHandle protocol
 * (design §7), mirroring the PumpScheduler backend pattern: feature-detect
 * once per app session via CreateObject-returns-invalid (roRenderThreadQueue
 * exists on Roku OS 15.0+), cache on the global node under a dedicated field
 * (NEVER the pump's), one per-instance channel per component (NEVER the
 * pump's channel). The envelopes are the field carrier's exactly
 * (ScopeWire.kt) — only the transport and the reply-address FORM differ:
 *
 * - owner advertisement: `"rtq:<ownerChannelId>"` (the ad doubles as the
 *   routing address) instead of `"field"`;
 * - child reply address: its own channel id (`replyToChannel`) instead of a
 *   node ref (`replyTo`).
 *
 * Mixed-mode law (the local force hooks construct such pairs): transport TO a
 * peer always follows the PEER's declaration — the owner's ad says how to
 * post requests/cancels; the request's reply key says how to post outcomes.
 * A component's own backend decides only what it declares for itself, so a
 * field-backend child interoperates with an rtq owner and vice versa.
 */

/** Session-wide backend cache field on the global node (NEVER the pump's). */
internal const val SCOPE_BACKEND_CACHE_FIELD: String = "__kotlinScopeBackend"

/** The rtq backend name ([SCOPE_AD_FIELD_BACKEND] doubles as the floor's). */
internal const val SCOPE_BACKEND_RTQ: String = "rtq"

/**
 * Per-component carrier state (`object` = GetGlobalAA identity domain, so
 * each component instance resolves and registers independently — the
 * PumpScheduler/ComponentMailbox identity model).
 */
internal object ScopeCarrier {
    /** "" until resolved; then "rtq" or "field" for THIS component. */
    internal var backend: String = ""

    /** This component's queue handle (posting and registration). */
    internal var rtq: RoRenderThreadQueue? = null

    /** This component's own scope channel id ("" until allocated). */
    internal var channelId: String = ""

    /** The channel handler is registered (registration precedes every use). */
    internal var registered: Boolean = false
}

// File-private splices (BrsInline helpers stay private to the file that uses
// them — ComponentMailbox precedent).
@BrsInline("return CreateObject(\"roRenderThreadQueue\")")
private external fun scopeRenderThreadQueueOrInvalid(): RoRenderThreadQueue?

@BrsInline("return CreateObject(\"roDeviceInfo\").GetRandomUUID()")
private external fun scopeChannelUuid(): String

/**
 * Resolves THIS component's scope carrier, once (PumpScheduler.resolveBackend
 * verbatim): session cache on the global node first; otherwise feature-detect
 * and publish the result for every other component. The global comes from the
 * pump scheduler's attach state — non-null in every context that reaches
 * this (exposeScope attaches explicitly; the child wire path is gated on the
 * ambient top, which implies attach ran). A null global (unattachable
 * context) skips only the session-wide cache: the component still
 * feature-detects and memoizes the result per-component
 * ([ScopeCarrier.backend]).
 */
internal fun resolveScopeBackend(): String {
    if (ScopeCarrier.backend != "") {
        return ScopeCarrier.backend
    }
    val g = PumpScheduler.hostGlobalOrNull()
    var resolved = ""
    if (g != null) {
        val cached = g.getField(SCOPE_BACKEND_CACHE_FIELD)
        if (cached != null) {
            resolved = "$cached"
        }
    }
    if (resolved == "") {
        val q = scopeRenderThreadQueueOrInvalid()
        resolved = if (q == null) SCOPE_AD_FIELD_BACKEND else SCOPE_BACKEND_RTQ
        if (q != null) {
            ScopeCarrier.rtq = q
        }
        if (g != null) {
            g.addField(SCOPE_BACKEND_CACHE_FIELD, "string", false)
            g.setField(SCOPE_BACKEND_CACHE_FIELD, resolved)
        }
    }
    ScopeCarrier.backend = resolved
    return resolved
}

/**
 * Registers this component's scope message handler on its own per-instance
 * channel (`"kotlin.scope.<uuid>"`), once, and returns the channel id (""
 * when the queue is unavailable — callers fall back to the field floor).
 * Posts to unregistered channels are DROPPED by the platform (spike-pinned),
 * so this always runs STRICTLY BEFORE the channel id is advertised (owner) or
 * used as a reply address (child) — the registration-before-advertisement
 * law, both sides.
 */
internal fun ensureScopeRtqChannel(): String {
    if (ScopeCarrier.registered) {
        return ScopeCarrier.channelId
    }
    var q = ScopeCarrier.rtq
    if (q == null) {
        q = scopeRenderThreadQueueOrInvalid()
        ScopeCarrier.rtq = q
    }
    if (q == null) {
        return ""
    }
    if (ScopeCarrier.channelId == "") {
        ScopeCarrier.channelId = "kotlin.scope." + scopeChannelUuid()
    }
    q.addMessageHandler(ScopeCarrier.channelId, brsName(::onKotlinScopeRtqMessage))
    ScopeCarrier.registered = true
    return ScopeCarrier.channelId
}

/**
 * The single rtq scope handler (ifRenderThreadQueue shape: `sub Handler(data,
 * msgInfo)`). Runs in the REGISTERING component's own script context
 * (spike-pinned), so [ScopeHostHolder]/[ComponentMailbox] resolve to the
 * right instance — the same context guarantee as the field observers. ONE
 * channel per component serves BOTH roles: kind dispatch routes owner traffic
 * (request/cancel) and child traffic (outcome); unknown kinds are ignored
 * silently (decision 9). No stacked-observer double-dispatch hazard here
 * (single handler per channel), unlike the field carrier's role-filtered
 * observer pair.
 */
internal fun onKotlinScopeRtqMessage(data: Any?, msgInfo: Any?) {
    val payload = data as? RoAssociativeArray
    if (payload == null) {
        return
    }
    val env = parseScopeEnvelope(payload)
    if (env.kind == SCOPE_KIND_REQUEST) {
        handleScopeRequest(env)
        return
    }
    if (env.kind == SCOPE_KIND_CANCEL) {
        handleScopeCancel(env)
        return
    }
    if (env.kind == SCOPE_KIND_OUTCOME) {
        deliverScopeOutcome(env)
    }
}

/**
 * Posts [envelope] to a registered scope channel. PostMessage has MOVE
 * semantics (spike-pinned): the posted AA's top level is gutted sender-side —
 * safe here because every envelope is freshly built per post and never
 * retained or reused (the watchdog captures strings, not the envelope), and
 * nested values the sender still references are COPIED by the platform (the
 * move-vs-copy rule, spike `move.into`/`q1d.rtq.nested`), so the by-copy law
 * holds on this carrier with no explicit [deepCopyAA]. Posting needs no
 * registration SENDER-side, so a field-backend component posts to an rtq peer
 * just fine (mixed mode). Queue-unavailable is a silent drop — dead-receiver
 * parity with the field carrier, and unreachable in practice: a channel id
 * only exists where the queue does.
 */
internal fun postScopeEnvelopeToChannel(channel: String, envelope: RoAssociativeArray) {
    var q = ScopeCarrier.rtq
    if (q == null) {
        q = scopeRenderThreadQueueOrInvalid()
        ScopeCarrier.rtq = q
    }
    if (q == null) {
        return
    }
    q.postMessage(channel, envelope)
}

/**
 * Owner-directed post: transport per the owner's ADVERTISEMENT
 * (`"rtq:<id>"` → channel post; anything else → inbox field write). The
 * child's own backend decides only its REPLY-ADDRESS form; how to REACH the
 * owner is always the owner's declaration (mixed-mode law).
 */
internal fun postScopeEnvelopeToOwner(
    ownerNode: RoSGNode,
    ownerChannel: String,
    envelope: RoAssociativeArray,
) {
    if (ownerChannel != "") {
        postScopeEnvelopeToChannel(ownerChannel, envelope)
        return
    }
    ownerNode.setField(SCOPE_INBOX_FIELD, envelope)
}

/** Test-hook core: pin THIS component's carrier to the field backend. */
internal fun forceScopeFieldBackendLocally() {
    ScopeCarrier.backend = SCOPE_AD_FIELD_BACKEND
}

/**
 * Test-hook core: write the session-wide cache AND pin the calling
 * component's carrier (kotlinPumpForceTimerBackend shape). Components that
 * already resolved keep their backend.
 */
internal fun forceScopeFieldBackendSessionWide(global: RoSGNode) {
    global.addField(SCOPE_BACKEND_CACHE_FIELD, "string", false)
    global.setField(SCOPE_BACKEND_CACHE_FIELD, SCOPE_AD_FIELD_BACKEND)
    forceScopeFieldBackendLocally()
}

/** Test-hook core: "none" until resolved, then "rtq" or "field". */
internal fun scopeBackendNameOrNone(): String {
    if (ScopeCarrier.backend == "") {
        return "none"
    }
    return ScopeCarrier.backend
}
