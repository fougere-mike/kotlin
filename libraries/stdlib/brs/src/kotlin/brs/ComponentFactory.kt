/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs

/**
 * Creates a new, unparented instance of the SceneGraph component [T].
 *
 * [T] must be a concrete component class (a user class extending
 * [GroupComponent], [SceneComponent], [TaskComponent], etc.). The returned
 * handle is the component's node: `@SG*Field` properties read and write
 * the node's interface fields directly.
 *
 * Compiles to:
 * ```brightscript
 * CreateObject("roSGNode", "<ComponentName>")
 * ```
 *
 * Example:
 * ```kotlin
 * val task = createComponent<FetchFeedTask>()
 * task.url = "https://example.com/feed"
 * ```
 */
public inline fun <reified T : ComponentBase> createComponent(): T = brsCreateComponent<T>()

/**
 * Backend intrinsic for [createComponent]. Calls are lowered to
 * `CreateObject("roSGNode", "<ComponentName>")` using the reified type
 * argument; this body only exists as a safety net for a missed lowering.
 */
@PublishedApi
internal fun <T : ComponentBase> brsCreateComponent(): T {
    error("brsCreateComponent should be lowered by the backend")
}
