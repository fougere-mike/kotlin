/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs

import kotlin.brs.roku.RoAssociativeArray
import kotlin.brs.roku.RoSGNode
import kotlin.coroutines.pump.PumpScheduler

/**
 * SharedService: reference-shared classes across components (design of record:
 * docs/superpowers/plans/2026-08-14-shared-service-design.md).
 *
 * An OWNER component publishes a live instance onto a node it controls; any
 * component holding that node acquires THE SAME instance — genuine shared
 * identity over SetRef/GetRef (Roku OS 15.0+), never a copy, never a husk:
 *
 * ```kotlin
 * abstract class ViewModel : SharedService() { ... }    // app-owned vocabulary
 * class GuideVm(...) : ViewModel() { var selectedDay = 0 }
 *
 * // Owner screen:   shareOn(top, GuideVm(...))
 * // Any child:      val vm = sharedFrom<GuideVm>(screenNode)
 * // App services:   shareOn(top.getScene(), ApiClient(...)); sharedFrom via getScene()
 * ```
 *
 * The stash is one runtime-added AA field (`__kotlinShared`) per publishing
 * node, holding key → instance entries, written via SetRef ONLY (an ordinary
 * setField would copy). Keys default to the instance's runtime class name —
 * both ends are typed, so publish and acquire derive the same key with zero
 * ceremony; explicit keys cover two-instances-of-one-type.
 *
 * v1 laws: render-thread component callers only (like runTask/ScopeHandle);
 * OS 15.0+ floor — [shareOn] THROWS pre-15 (gate features with [canShare]);
 * republish under the same key replaces (the prior generation's [isLive]
 * goes false — pairs with the recreate-don't-reuse screen convention).
 */
public abstract class SharedService {
    /** Stash back-reference: the node this instance was last [shareOn]'d to (null until shared). */
    internal var __sharedNode: RoSGNode? = null

    /** Stash back-reference: the key this instance was last published under. */
    internal var __sharedKey: String = ""

    /** Generation stamp: which publish generation of its key this instance is (0 until shared). */
    internal var __sharedGen: Int = 0

    /**
     * Generation-based liveness: true iff THIS instance is the CURRENT
     * generation of the stash entry it was published to — its publish-time
     * stamp compared against the stash's per-key generation counter. Plain
     * Int reads through the GetRef proxy (pure data), so the verdict is
     * immune to per-access proxy identity.
     *
     * False when the instance was never [shareOn]'d, when a republish under
     * the same key bumped the generation, or when the stash is unreachable
     * (node destroyed, or read from a context where GetRef cannot succeed).
     * Defense-in-depth/debug — acquisition via [sharedFrom] returns GetRef
     * references, which are current-generation by construction.
     *
     * Residual: a same-generation instance that crossed a COPYING channel (a
     * husk) would still stamp-match. Acceptable by design: the copying
     * channels are compile-errors for SharedService types (the
     * BRS_SHARED_THROUGH_COPYING_CHANNEL FIR rule) — the same residual class
     * the design accepts for casts (spec §5 non-rules note).
     */
    public fun isLive(): Boolean {
        val node = __sharedNode
        if (node == null) {
            return false
        }
        // Destroyed-node/off-context guard: a failing GetRef answers false,
        // never crashes.
        if (!node.canGetRef(SHARED_STASH_FIELD)) {
            return false
        }
        val stash = node.getRef(SHARED_STASH_FIELD) as? RoAssociativeArray
        if (stash == null) {
            return false
        }
        val gens = stash.lookup(SHARED_GENS_KEY) as? RoAssociativeArray
        if (gens == null) {
            return false
        }
        // WHY generation stamps and not roUtils.IsSameObject (device fact,
        // 2026-08-14, Suite 9 runs 1-4): IsSameObject answered FALSE for a
        // nested stash entry reached through two SEPARATE GetRef accesses in
        // every run, while state sharing on the same entries was green in the
        // same runs. Two candidate sub-explanations, not yet discriminated:
        // per-access proxy identity blur (each getRef mints a distinct
        // wrapper over shared backing) vs IsSameObject semantics on
        // node-backed nested reads. Un-run discriminating probes (SPIKE
        // BAIT): (1) owner-side isLive() immediately post-publish (local
        // instance vs entry through one getRef); (2) consumer-side
        // IsSameObject of two back-to-back sharedFrom results. The scope-
        // handle spike's IsSameObject-true pin covered only the TOP-LEVEL
        // SetRef'd AA, never nested entries.
        // A missing counter reads 0, which no stamped instance carries
        // (generations start at 1) — false, never a crash.
        return (gens.lookup(__sharedKey) as? Int ?: 0) == __sharedGen
    }
}

/**
 * Publishes [instance] on [node] under its default key — the instance's
 * runtime class name (the same name `is`-checks and [sharedFrom]'s reified
 * lowering use, so both ends derive identical keys with zero ceremony).
 *
 * See the keyed overload for the full contract.
 */
public fun shareOn(node: RoSGNode, instance: SharedService) {
    shareOn(node, instance, sharedServiceKeyOf(instance))
}

/**
 * Publishes [instance] on [node] under [key]: any component holding [node]
 * acquires THE SAME instance via [sharedFrom] — shared identity, not a copy.
 *
 * Every publish builds the next stash generation as a plain local AA —
 * carrying the prior generation's entries over via live GetRef reads — and
 * SetRefs it. (Device fact, 2026-08-14 Suite 9 run: GetRef-handle READS are
 * live, but INSERTS through the handle store a copy — so instances reach the
 * stash only via local-AA insert + SetRef, the identity-preserving
 * primitive.) The stash also carries a per-key generation counter (the
 * reserved '__gens' map): each publish stamps the instance and bumps the
 * counter — [SharedService.isLive]'s oracle. Keys starting with '__' are
 * reserved for that machinery and throw a guided [IllegalStateException].
 *
 * Republish under the same key REPLACES the entry: the prior generation's
 * [SharedService.isLive] goes false, and holders of stale references should
 * re-acquire (recreate-don't-reuse screen convention).
 *
 * Render-thread component context only (SetRef is render-thread-only), and
 * Roku OS 15.0+ only — throws a guided [IllegalStateException] otherwise;
 * gate optional features with [canShare].
 */
public fun shareOn(node: RoSGNode, instance: SharedService, key: String) {
    if (PumpScheduler.hostTopOrNull() == null) {
        throw IllegalStateException(
            "shareOn must be called from a render-thread component context (like runTask)"
        )
    }
    if (!canShare()) {
        throw IllegalStateException(
            "SharedService requires Roku OS 15.0+ (SetRef) — the MVVM-layer floor decision " +
                "(design A4); gate with canShare()"
        )
    }
    if (key.startsWith(SHARED_RESERVED_PREFIX)) {
        throw IllegalStateException(
            "shareOn key '$key' uses the reserved '__' prefix (stash machinery namespace, " +
                "e.g. '__gens') — choose a key without it"
        )
    }
    // EVERY publish builds the next stash generation as a plain LOCAL AA and
    // SetRefs it. Device fact (Suite 9 runs, 2026-08-14): READS through a
    // GetRef handle are live — they return the real entry objects — but
    // INSERTS through the handle store a slot-preserving intra-thread COPY,
    // which silently breaks shared identity (the owner's local instance would
    // not be the stash entry, and post-insert writes would never reach it).
    // The caller's instance therefore only ever reaches the stash via the
    // provably-safe primitive: plain local insert + SetRef.
    val next = RoAssociativeArray.create()
    val gens = RoAssociativeArray.create()
    var generation = 1
    if (node.canGetRef(SHARED_STASH_FIELD)) {
        val prior = node.getRef(SHARED_STASH_FIELD) as? RoAssociativeArray
        if (prior != null) {
            // Carry the prior generation's ENTRIES into the new container:
            // live reads + plain local inserts, so entries under OTHER keys
            // keep their identity across a republish. Machinery keys are
            // skipped — the gens map is rebuilt fresh below.
            val priorKeys = prior.keys()
            while (priorKeys.count() > 0) {
                val priorKey = "${priorKeys.shift()}"
                if (!priorKey.startsWith(SHARED_RESERVED_PREFIX)) {
                    next.addReplace(priorKey, prior.lookup(priorKey))
                }
            }
            // Carry the per-key generation counters (plain Ints — live reads
            // of pure data) into a fresh map, and bump THIS key's generation.
            val priorGens = prior.lookup(SHARED_GENS_KEY) as? RoAssociativeArray
            if (priorGens != null) {
                val genKeys = priorGens.keys()
                while (genKeys.count() > 0) {
                    val genKey = "${genKeys.shift()}"
                    gens.addReplace(genKey, priorGens.lookup(genKey) as? Int ?: 0)
                }
                generation = (priorGens.lookup(key) as? Int ?: 0) + 1
            }
        }
    }
    // Back-refs and the generation stamp BEFORE the instance enters the
    // stash: the SetRef'd container shares its entries by reference
    // (device-pinned by the sharedSameInstance E2E), so state the instance
    // carries at SetRef time is demonstrably on the shared entry.
    instance.__sharedNode = node
    instance.__sharedKey = key
    instance.__sharedGen = generation
    gens.addReplace(key, generation)
    next.addReplace(SHARED_GENS_KEY, gens)
    next.addReplace(key, instance)
    // Declare the field (a no-op when it already exists), then SetRef the new
    // generation.
    node.addField(SHARED_STASH_FIELD, "assocarray", false)
    node.setRef(SHARED_STASH_FIELD, next.asDynamic())
}

/**
 * Whether sharing is available here: an ambient render-thread component
 * context AND the OS 15.0+ reference APIs (SetRef). Never throws — this is
 * the gate for apps that degrade features on older devices; [shareOn] throws
 * the guided floor error when called without it.
 *
 * The feature probe is `CreateObject("roUtils")`-returns-invalid (the
 * crash-free detection family — PumpScheduler precedent; probing SetRef
 * itself would crash pre-15 on the missing member function). roUtils ships
 * in the same OS 15.0 data-transfer feature set as SetRef. Probed once per
 * component instance.
 */
public fun canShare(): Boolean {
    if (PumpScheduler.hostTopOrNull() == null) {
        return false
    }
    if (!SharedServiceState.probed) {
        SharedServiceState.setRefAvailable = createRoUtilsOrInvalid() != null
        SharedServiceState.probed = true
    }
    return SharedServiceState.setRefAvailable
}

/**
 * Acquires the instance shared on [node] under [T]'s default key (its class
 * name). Returns the GetRef reference — live by construction; mutations are
 * visible to every holder. Nothing shared → guided [IllegalStateException]
 * ([sharedFromOrNull] answers null instead); an entry of the wrong type →
 * guided ISE naming the key collision (both variants — a collision is a
 * caller bug, not an absence). Pre-OS-15 → the same guided floor ISE as
 * [shareOn] (gate with [canShare]).
 */
public inline fun <reified T : SharedService> sharedFrom(node: RoSGNode): T =
    sharedAcquire<T>(node, brsSharedServiceName<T>(), brsSharedServiceName<T>(), false)!!

/** Acquires the instance shared on [node] under an explicit [key]. See [sharedFrom]. */
public inline fun <reified T : SharedService> sharedFrom(node: RoSGNode, key: String): T =
    sharedAcquire<T>(node, key, brsSharedServiceName<T>(), false)!!

/**
 * Like [sharedFrom], but answers null when nothing is shared under the key
 * (publish-order races, optional services) — including pre-OS-15, where
 * nothing can ever be shared (a truthful null; degrade-gracefully callers
 * need no [canShare] gate of their own). Caller bugs still throw: a wrong
 * calling context or a wrong-type entry is guided, never swallowed into null.
 */
public inline fun <reified T : SharedService> sharedFromOrNull(node: RoSGNode): T? =
    sharedAcquire<T>(node, brsSharedServiceName<T>(), brsSharedServiceName<T>(), true)

/** Like [sharedFromOrNull], under an explicit [key]. */
public inline fun <reified T : SharedService> sharedFromOrNull(node: RoSGNode, key: String): T? =
    sharedAcquire<T>(node, key, brsSharedServiceName<T>(), true)
