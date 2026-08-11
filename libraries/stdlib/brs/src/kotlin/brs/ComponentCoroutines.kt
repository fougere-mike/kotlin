/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs

import kotlin.brs.roku.RoSGNode
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.CoroutineScope
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.Job
import kotlin.coroutines.builders.launch
import kotlin.coroutines.dispatchers.Dispatchers
import kotlin.coroutines.pump.PumpScheduler

// `top`/`global` are protected on ComponentBase, so extensions bridge them
// with @BrsInline splices — a component `this` IS its m-scope AA at runtime,
// which carries `.top`/`.global` on every SceneGraph component.
@BrsInline("return component.top")
private external fun componentTopOf(component: ComponentBase): RoSGNode

@BrsInline("return component.global")
private external fun componentGlobalOf(component: ComponentBase): RoSGNode

// Per-component-instance holder: object singletons live on GetGlobalAA, which
// is per-component-instance on the render thread (render-thread-queue spike),
// so each component gets exactly one scope with no m-key bookkeeping.
private object ComponentScopeHolder {
    var scope: CoroutineScope? = null
}

/**
 * The component's coroutine scope (created on first use, one per component
 * instance). Context: [Dispatchers.Main] + a root [Job].
 *
 * Coroutines launched in this scope need NO pump wiring: attaching the scope
 * arms the self-scheduling pump, which wakes the render thread exactly when
 * work exists (roRenderThreadQueue post on Roku OS 15+, one-shot Timer
 * otherwise) and goes fully idle when it doesn't. Do not create pump timers or
 * call `processCoroutineQueue()`/`processCoroutineDelays()` from components.
 *
 * Render-thread components only — do not use from a [TaskComponent]'s `run()`
 * (task-thread work is synchronous by design; results cross back via typed
 * `@SG*Field` outputs).
 */
public fun ComponentBase.componentScope(): CoroutineScope {
    val existing = ComponentScopeHolder.scope
    if (existing != null) return existing
    // Belt-and-braces with the compiler-injected __kotlinPumpAttach: covers
    // components whose files escape the injection predicate (e.g. coroutine
    // use hidden entirely inside another file's helper).
    PumpScheduler.attach(componentTopOf(this), componentGlobalOf(this))
    val scope = CoroutineScope(Dispatchers.Main + Job())
    ComponentScopeHolder.scope = scope
    return scope
}

/**
 * Launches a coroutine in the component's [componentScope] — the canonical way
 * to run async work in a SceneGraph component:
 *
 * ```kotlin
 * class ShelfView : GroupComponent() {
 *     init {
 *         launch {
 *             val task = runTask<FetchShelfTask> { count = 5 }
 *             shelfItems = task.items
 *         }
 *     }
 * }
 * ```
 *
 * No imports, no dispatcher choice, no pump wiring. See [componentScope] for
 * the scope's semantics and threading constraints.
 */
public fun ComponentBase.launch(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> Unit
): Job = componentScope().launch(context, block)
