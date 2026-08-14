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
 * The per-node stash field: an AA of key → instance entries, ALWAYS written
 * via SetRef (never ordinary setField — that would copy) and read via GetRef
 * (references, live by construction). Every publish SetRefs a freshly built
 * LOCAL container — device fact (2026-08-14): GetRef-handle reads are live,
 * but inserts through the handle copy. One field per publishing node.
 */
internal const val SHARED_STASH_FIELD: String = "__kotlinShared"

/**
 * Per-component-instance holder (`object` singletons live on GetGlobalAA,
 * which is per-component-instance on the render thread — ComponentMailbox
 * precedent): caches the OS 15.0 feature probe so [canShare] runs one
 * CreateObject per component, not per call.
 */
internal object SharedServiceState {
    /** The feature probe ran in this component. */
    internal var probed: Boolean = false

    /** Probe result: the OS 15.0 reference APIs (SetRef/roUtils) exist. */
    internal var setRefAvailable: Boolean = false
}

// File-private splices (ComponentMailbox precedent: BrsInline helpers stay
// private to the file that uses them; createRoUtilsOrInvalid is internal
// because canShare lives in SharedService.kt).

/**
 * Feature probe: `CreateObject("roUtils")` yields invalid pre-OS-15. A
 * separate splice because the @BrsCreateObject factory is typed non-null
 * (createRenderThreadQueueOrInvalid precedent).
 */
@BrsInline("return CreateObject(\"roUtils\")")
internal external fun createRoUtilsOrInvalid(): RoUtils?

/**
 * The `__proto` head: the value's runtime class name (constructors emit the
 * class's own name at proto index 0; verified against generated ctor output).
 */
@BrsInline("return instance.__proto[0]")
private external fun sharedProtoHead(instance: Dynamic): String

/**
 * The `is`-check machinery over data: walks the value's nested `__proto`
 * chain for [typeName]. The helper is unconditionally emitted into the stdlib
 * runtime (addRuntimeHelperFunctionsToManifest), so the splice always
 * resolves.
 */
@BrsInline("return __kotlin_isInstanceOf(entry, typeName)")
private external fun sharedEntryIsInstance(entry: Dynamic, typeName: String): Boolean

/**
 * The default stash key for [instance]: its runtime class name (the `__proto`
 * head — the SAME string the compiler emits for `is`-checks and for
 * [sharedFrom]'s lowered type name, so publish and acquire derive identical
 * keys with zero ceremony). Public for the unit suite; machinery otherwise.
 */
public fun sharedServiceKeyOf(instance: SharedService): String =
    sharedProtoHead(instance.asDynamic())

/**
 * Placeholder for [T]'s compiler-resolved class name. Call sites of
 * [sharedFrom]/[sharedFromOrNull] are rewritten by the backend
 * (BrsSharedFromCallLowering) before this can run; reaching this body means
 * a call site the lowering could not see the concrete type of (typically a
 * user-written reified forwarder — call sharedFrom directly instead).
 */
@PublishedApi
internal fun <T : SharedService> brsSharedServiceName(): String {
    error("brsSharedServiceName should be rewritten by the backend (sharedFrom lowering)")
}

/**
 * Implementation target of [sharedFrom]/[sharedFromOrNull]; the backend
 * rewrites reified call sites into
 * `sharedAcquire(node, key, "<ClassName>", orNull)` because klib inline
 * functions are never inlined at user call sites (BrsInlineFunctionResolver
 * limitation) — the rewrite performs exactly the one inline step the resolver
 * cannot, resolving the class name through the compiler's own naming (the
 * `__proto`/`is`-check source). Keep this signature in sync with
 * BrsSharedFromCallLowering.
 */
@PublishedApi
internal fun <T : SharedService> sharedAcquire(
    node: RoSGNode,
    key: String,
    typeName: String,
    orNull: Boolean,
): T? {
    if (PumpScheduler.hostTopOrNull() == null) {
        throw IllegalStateException(
            "sharedFrom must be called from a render-thread component context (like runTask)"
        )
    }
    // OS floor gate BEFORE any node call: canGetRef is itself an OS 15.0
    // reference API, so touching it pre-15 would crash on the missing member
    // function instead of failing guided. Pre-15, nothing can ever be shared:
    // orNull answers a truthful null; the throwing variant gets the same
    // guided floor error as shareOn. (This branch is unpinnable on the OS 15
    // test device — off-render calls throw the context ISE above first.)
    if (!canShare()) {
        if (orNull) {
            return null
        }
        throw IllegalStateException(
            "SharedService requires Roku OS 15.0+ (SetRef) — the MVVM-layer floor decision " +
                "(design A4); gate with canShare()"
        )
    }
    var stash: RoAssociativeArray? = null
    if (node.canGetRef(SHARED_STASH_FIELD)) {
        stash = node.getRef(SHARED_STASH_FIELD) as? RoAssociativeArray
    }
    if (stash == null) {
        if (orNull) {
            return null
        }
        throw IllegalStateException(
            "nothing shared on node '${node.getField("id")}' — shareOn(node, instance) " +
                "in the owner first, or use sharedFromOrNull"
        )
    }
    val entry = stash.lookup(key)
    if (entry == null) {
        if (orNull) {
            return null
        }
        throw IllegalStateException(
            "nothing shared under key '$key' on node '${node.getField("id")}' — " +
                "shareOn(node, instance) in the owner first, or use sharedFromOrNull"
        )
    }
    if (!sharedEntryIsInstance(entry, typeName)) {
        // A collision is a caller bug, not an absence: guided in BOTH the
        // throwing and orNull variants.
        throw IllegalStateException(
            "entry under key '$key' is a ${sharedProtoHead(entry)}, not $typeName — " +
                "key collision; use explicit keys"
        )
    }
    return entry.unsafeCast<T>()
}
