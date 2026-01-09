/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs

import kotlin.brs.roku.RoSGNode
import kotlin.brs.roku.RoAssociativeArray

/**
 * Base class for SceneGraph components that extend Group.
 *
 * Provides type-safe access to component scope variables:
 * - [top] - The component's interface node (`m.top`)
 * - [global] - The global node (`m.global`)
 * - [m] - Direct access to the component scope for custom fields
 *
 * The `init { }` block in subclasses compiles to BrightScript's `sub init()`.
 *
 * Example:
 * ```kotlin
 * class MyComponent : SceneComponent() {
 *     init {
 *         top.setFocus(true)
 *         global.setField("ready", true)
 *     }
 *
 *     fun handleButton() {
 *         println("Button pressed")
 *     }
 * }
 * ```
 *
 * Compiles to:
 * ```brightscript
 * sub init()
 *     m.top.setFocus(true)
 *     m.global.setField("ready", true)
 * end sub
 *
 * sub handleButton_k_()
 *     println_AnyN_k_("Button pressed")
 * end sub
 * ```
 */
@BrsSceneGraphComponent(extends = "Group")
public abstract class SceneComponent {
    /**
     * The component's interface node.
     *
     * Compiles to: `m.top`
     *
     * This node contains all the fields defined in the component's XML
     * interface and provides access to the component's SceneGraph properties.
     */
    protected val top: RoSGNode
        get() = definedExternally

    /**
     * The global node for sharing data across all components.
     *
     * Compiles to: `m.global`
     *
     * The global node is shared across the entire SceneGraph application
     * and is commonly used for app-wide state, authentication tokens,
     * feature flags, and other shared data.
     */
    protected val global: RoSGNode
        get() = definedExternally

    /**
     * Direct access to the component scope.
     *
     * Compiles to: `m`
     *
     * Use this for storing component-local state that doesn't need to be
     * exposed in the component's interface. For example:
     * ```kotlin
     * m["_internalCounter"] = 0
     * ```
     */
    protected val m: RoAssociativeArray
        get() = definedExternally
}

/**
 * Base class for SceneGraph Task components.
 *
 * Task components run on a separate thread and are used for
 * background operations like network requests, file I/O, and
 * other long-running operations.
 *
 * Example:
 * ```kotlin
 * class FetchDataTask : TaskComponent() {
 *     init {
 *         top.functionName = "fetchData"
 *     }
 *
 *     fun fetchData() {
 *         // Perform network request
 *         val result = doNetworkCall()
 *         top.setField("result", result)
 *     }
 * }
 * ```
 */
@BrsSceneGraphComponent(extends = "Task")
public abstract class TaskComponent : SceneComponent()

/**
 * Base class for SceneGraph Scene components.
 *
 * Scene components represent the root of a SceneGraph application.
 * There is typically one Scene per application, created via
 * `RoSGScreen.createScene()`.
 *
 * Example:
 * ```kotlin
 * class MainScene : SceneNodeComponent() {
 *     init {
 *         top.setFocus(true)
 *         // Initialize the scene
 *     }
 * }
 * ```
 */
@BrsSceneGraphComponent(extends = "Scene")
public abstract class SceneNodeComponent : SceneComponent()
