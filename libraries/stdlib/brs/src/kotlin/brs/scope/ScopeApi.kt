/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs

import kotlin.brs.roku.RoArray
import kotlin.brs.roku.RoAssociativeArray
import kotlin.brs.roku.RoSGNode
import kotlin.brs.scope.ComponentMailbox
import kotlin.brs.scope.SCOPE_AD_FIELD
import kotlin.brs.scope.SCOPE_AD_FIELD_BACKEND
import kotlin.brs.scope.SCOPE_INBOX_FIELD
import kotlin.brs.scope.ScopeHandlerEntry
import kotlin.brs.scope.ScopeHostHolder
import kotlin.brs.scope.ScopeHostImpl
import kotlin.brs.scope.ScopeOwnerState
import kotlin.brs.scope.onKotlinScopeInbox
import kotlin.brs.scope.postScopeRequestAndAwait
import kotlin.coroutines.CoroutineScope
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.pump.PumpScheduler

/**
 * ScopeHandle: cross-component scope borrowing (design of record:
 * docs/superpowers/plans/2026-08-12-scopehandle-design.md + Addendum A.1–A.6).
 *
 * An OWNER component exposes its coroutine scope once:
 *
 * ```kotlin
 * object RefreshWatchlist : ScopeRequest<Items>("RefreshWatchlist")
 *
 * class VmHost : RectangleComponent() {
 *     private val host = exposeScope {
 *         handle(RefreshWatchlist) { refreshInternal() }   // owner's code, owner's scope
 *     }
 *     fun onDismiss() = host.close()                       // THE teardown convention
 * }
 * ```
 *
 * Any child holding a [ScopeHandle] (minted from the owner's NODE — never a
 * component reference) awaits work that genuinely executes owner-side:
 *
 * ```kotlin
 * val items = owner.run(RefreshWatchlist)
 * ```
 *
 * v1 laws: render-thread component callers only; no timeouts in the API
 * (compose with `withTimeout`); teardown goes through [ScopeHost.close] —
 * a ~30s once-per-request console watchdog makes a missed close diagnosable.
 */

/**
 * Cancellation-flavored completion of a scope request: the owner's exposed
 * scope was closed ([ScopeHost.close]) before or during the request. Extends
 * [CancellationException], so an uncaught one winds the child coroutine down
 * quietly (TimeoutCancellationException precedent).
 */
public class ScopeClosedException internal constructor(message: String) : CancellationException(message)

/**
 * A scope request failed owner-side. Carries the ORIGINAL failure info as
 * data (TaskException precedent) — exception TYPES do not cross components;
 * domain failures that need typed handling belong in result values.
 */
public class ScopeRequestException(
    message: String,
    /** BrightScript runtime error number (0 when unavailable). */
    public val number: Int,
    /** BrightScript backtrace captured owner-side, if any. */
    public val backtrace: Dynamic?,
) : RuntimeException(message)

/**
 * A 0-arg request declaration. Declare as an object with an explicit wire
 * name: `object RefreshShelf : ScopeRequest<Int>("RefreshShelf")`. Names
 * containing '#' are rejected at registration ('#' is reserved for
 * compiler-lowered block names).
 */
public abstract class ScopeRequest<R>(public val name: String)

/** A 1-arg request declaration (arities 0–2; more args → one data holder). */
public abstract class ScopeRequest1<A1, R>(public val name: String)

/** A 2-arg request declaration. */
public abstract class ScopeRequest2<A1, A2, R>(public val name: String)

/**
 * Handle to an exposed scope, returned by [exposeScope]. `close()` is
 * idempotent and is THE teardown convention: whoever retires an owner node
 * calls it first; in-flight requests complete "closed", late requests are
 * answered "closed" while the node lives.
 */
public interface ScopeHost {
    public val scope: CoroutineScope
    public fun close()
}

/**
 * Registration surface of [exposeScope]: binds request names to owner-side
 * suspend handlers. Request names must be unique per owner and may not
 * contain '#'.
 */
public class ScopeHandlerRegistry internal constructor(
    private val handlers: MutableMap<String, ScopeHandlerEntry>,
) {
    public fun <R> handle(request: ScopeRequest<R>, handler: suspend () -> R) {
        @Suppress("UNCHECKED_CAST")
        register(request.name, ScopeHandlerEntry(0, handler as (suspend () -> Any?), null, null))
    }

    public fun <A1, R> handle(request: ScopeRequest1<A1, R>, handler: suspend (A1) -> R) {
        @Suppress("UNCHECKED_CAST")
        register(request.name, ScopeHandlerEntry(1, null, handler as (suspend (Any?) -> Any?), null))
    }

    public fun <A1, A2, R> handle(request: ScopeRequest2<A1, A2, R>, handler: suspend (A1, A2) -> R) {
        @Suppress("UNCHECKED_CAST")
        register(request.name, ScopeHandlerEntry(2, null, null, handler as (suspend (Any?, Any?) -> Any?)))
    }

    private fun register(name: String, entry: ScopeHandlerEntry) {
        if (name.contains("#")) {
            throw IllegalStateException(
                "ScopeHandle: request name '$name' may not contain '#' (reserved for compiler-generated blocks)"
            )
        }
        if (handlers.containsKey(name)) {
            throw IllegalStateException(
                "ScopeHandle: request name '$name' registered twice on this host — request names must be unique per owner"
            )
        }
        handlers[name] = entry
    }
}

// `top`/`global` are protected on ComponentBase; extensions bridge them with
// splices (ComponentCoroutines precedent).
@BrsInline("return component.top")
private external fun scopeHostTopOf(component: ComponentBase): RoSGNode

@BrsInline("return component.global")
private external fun scopeHostGlobalOf(component: ComponentBase): RoSGNode

/**
 * Exposes this component's coroutine scope as a request host — owner-side,
 * typically once in `init`. Installs and arms the request inbox strictly
 * BEFORE the advertisement lands on the node (arming-order law), then runs
 * [register] against the registry, then advertises readiness.
 *
 * One host per component: a second call throws [IllegalStateException].
 */
public fun ComponentBase.exposeScope(
    scope: CoroutineScope = componentScope(),
    register: ScopeHandlerRegistry.() -> Unit = {},
): ScopeHost {
    if (ScopeHostHolder.state != null) {
        throw IllegalStateException(
            "ScopeHandle: exposeScope() called twice on this component — one scope host per " +
                "component; reuse the ScopeHost returned by the first call"
        )
    }
    val top = scopeHostTopOf(this)
    // Belt-and-braces pump attach (componentScope precedent): owner-side
    // request jobs dispatch through this component's queue even when a custom
    // scope was passed and no other coroutine machinery ran here yet.
    PumpScheduler.attach(top, scopeHostGlobalOf(this))
    val state = ScopeOwnerState(scope, top)
    ScopeHostHolder.state = state
    // Inbox armed BEFORE the advertisement can be observed by any child.
    top.addField(SCOPE_INBOX_FIELD, "assocarray", true)
    top.observeFieldScoped(SCOPE_INBOX_FIELD, brsName(::onKotlinScopeInbox))
    val registry = ScopeHandlerRegistry(state.handlers)
    registry.register()
    top.addField(SCOPE_AD_FIELD, "string", false)
    top.setField(SCOPE_AD_FIELD, SCOPE_AD_FIELD_BACKEND)
    return ScopeHostImpl(state)
}

/**
 * A borrowed reference to an owner's exposed scope. Construction-context-free:
 * it holds ONLY the owner's node identity — all caller-side machinery (child
 * inbox, park registry, request keys, watchdog) resolves from the AMBIENT
 * component at each [run] call, so a handle minted owner-side may be injected
 * into a plain-class VM and used from any child component.
 */
public class ScopeHandle internal constructor(internal val ownerNode: RoSGNode) {

    /** Runs a registered 0-arg request owner-side and returns its result. */
    public suspend fun <R> run(request: ScopeRequest<R>): R =
        postScopeRequestAndAwait(ownerNode, request.name, null, null)

    /** Runs a registered 1-arg request owner-side. Args cross BY COPY. */
    public suspend fun <A1, R> run(request: ScopeRequest1<A1, R>, a1: A1): R {
        val args = RoArray.create(0, true)
        args.push(a1)
        return postScopeRequestAndAwait(ownerNode, request.name, args, null)
    }

    /** Runs a registered 2-arg request owner-side. Args cross BY COPY. */
    public suspend fun <A1, A2, R> run(request: ScopeRequest2<A1, A2, R>, a1: A1, a2: A2): R {
        val args = RoArray.create(0, true)
        args.push(a1)
        args.push(a2)
        return postScopeRequestAndAwait(ownerNode, request.name, args, null)
    }

    /**
     * Runs a block owner-side (compiler-lowered surface). Call sites with a
     * literal lambda are rewritten by the compiler to the lowered entry point
     * (`BRS_SCOPE_BLOCK_NOT_LITERAL` guards everything else); this body is
     * the backstop for a call the lowering did not rewrite.
     */
    public suspend fun <R> run(block: suspend () -> R): R {
        throw IllegalStateException(
            "ScopeHandle.run(block) requires compiler lowering (BRS_SCOPE_BLOCK_NOT_LITERAL " +
                "guards call sites); this call was not lowered"
        )
    }
}

/**
 * Mints a handle to [owner]'s exposed scope. Takes a NODE, never a component
 * reference. The advertisement is read lazily at each [ScopeHandle.run]:
 * running against a node that never exposed a scope fails fast with
 * [IllegalStateException].
 */
public fun scopeHandleOf(owner: RoSGNode): ScopeHandle = ScopeHandle(owner)

/**
 * The lowered entry point targeted by the `owner.run { block }` compiler
 * lowering (Task 4): posts a compiler-synthesized request name plus the
 * block's BY-COPY captures.
 */
internal suspend fun <R> ScopeHandle.runLowered(name: String, captures: RoAssociativeArray?): R =
    postScopeRequestAndAwait(ownerNode, name, null, captures)

/**
 * TEST HOOK: overrides the ScopeHandle watchdog deadline for THIS component's
 * subsequent requests (default 30_000 ms). Lets a device test observe the
 * watchdog without a 30s wait.
 */
public fun kotlinScopeWatchdogMillis(ms: Int) {
    ComponentMailbox.watchdogMs = ms
}

/** TEST HOOK: times the ScopeHandle watchdog line printed in this component. */
public fun kotlinScopeWatchdogFires(): Int = ComponentMailbox.watchdogFires

/**
 * TEST HOOK: a fresh handler registry detached from any host — lets the
 * runBlocking unit suite exercise the registration guards (duplicate names,
 * reserved '#') without a component context.
 */
public fun kotlinScopeTestRegistry(): ScopeHandlerRegistry =
    ScopeHandlerRegistry(mutableMapOf())
