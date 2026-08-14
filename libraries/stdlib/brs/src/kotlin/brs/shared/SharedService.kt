/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs

import kotlin.brs.roku.RoAssociativeArray
import kotlin.brs.roku.RoSGNode
import kotlin.brs.roku.RoUtils
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

    /**
     * Identity-based liveness: true iff THIS instance is the one currently in
     * the stash entry it was published to — `roUtils.IsSameObject` against
     * the live stash (a boolean flag could not discriminate: it would cross a
     * copying channel along with the rest of the husk).
     *
     * False when the instance was never [shareOn]'d, when a republish under
     * the same key replaced it, or when the stash is unreachable (node
     * destroyed, or read from a context where GetRef cannot succeed).
     * Defense-in-depth/debug — acquisition via [sharedFrom] returns GetRef
     * references, which are live by construction.
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
        val entry = stash.lookup(__sharedKey)
        if (entry == null) {
            return false
        }
        return RoUtils.create().isSameObject(this.asDynamic(), entry)
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
 * The stash entry crosses via SetRef only; the stash field is added on first
 * publish and read-modify-written through GetRef references afterwards
 * (reference semantics make the mutation live — no re-SetRef needed).
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
    var stash: RoAssociativeArray? = null
    if (node.canGetRef(SHARED_STASH_FIELD)) {
        stash = node.getRef(SHARED_STASH_FIELD) as? RoAssociativeArray
    }
    if (stash != null) {
        // Later publish: the GetRef reference IS the live stash — mutating it
        // is visible to every holder without another SetRef.
        stash.addReplace(key, instance)
    } else {
        // First publish on this node: declare the field (a no-op when it
        // already exists), then the one and only SetRef of a fresh stash.
        node.addField(SHARED_STASH_FIELD, "assocarray", false)
        val fresh = RoAssociativeArray.create()
        fresh.addReplace(key, instance)
        node.setRef(SHARED_STASH_FIELD, fresh.asDynamic())
    }
    instance.__sharedNode = node
    instance.__sharedKey = key
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
