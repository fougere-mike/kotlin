/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs

import kotlin.brs.roku.RoSGNode
import kotlin.brs.roku.RoAssociativeArray

/**
 * Base class for all SceneGraph component types.
 *
 * Provides type-safe access to component scope variables and key event handling:
 * - [top] - The component's interface node (`m.top`)
 * - [global] - The global node (`m.global`)
 * - [m] - Direct access to the component scope for custom fields
 * - [onKeyEvent] - Override to handle key events (OK, Back, etc.)
 *
 * The `init { }` block in subclasses compiles to BrightScript's `sub init()`.
 *
 * @see GroupComponent For components extending Group
 * @see SceneComponent For components extending Scene
 * @see TaskComponent For background task components
 * @see LayoutComponent For auto-layout components (LayoutGroup)
 * @see ContentNodeComponent For data model components (ContentNode)
 * @see RectangleComponent For visual rectangle components
 */
public abstract class ComponentBase {
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

    /**
     * Handle remote key events.
     *
     * Override this method to handle key presses in your component.
     * Return `true` if you handled the event, `false` to let it propagate
     * to focused children.
     *
     * Compiles to:
     * ```brightscript
     * function onKeyEvent(key as String, press as Boolean) as Boolean
     *     return false  ' or your custom logic
     * end function
     * ```
     *
     * Common key values:
     * - "OK" - Select button
     * - "back" - Back button
     * - "up", "down", "left", "right" - D-pad
     * - "play", "pause", "rewind", "fastforward" - Media keys
     *
     * Example:
     * ```kotlin
     * class MyComponent : GroupComponent() {
     *     override fun onKeyEvent(key: String, press: Boolean): Boolean {
     *         if (press && key == "OK") {
     *             handleSelection()
     *             return true
     *         }
     *         return false  // Let event propagate to children
     *     }
     * }
     * ```
     *
     * @param key The key that was pressed (e.g., "OK", "back", "up")
     * @param press True if key was pressed, false if released
     * @return True if the key event was handled, false to let it propagate
     */
    protected open fun onKeyEvent(key: String, press: Boolean): Boolean = false

    /**
     * Lifecycle: fires once per ACTIVATION, on the render thread, as a child
     * coroutine of [componentScope], after init() has returned, every required
     * input is set, and every dependency registered during init has resolved.
     * Earliest: the first pump tick after init. Fires again after every
     * [revive]. The compiler launches it only for classes whose hierarchy
     * overrides it — a component that does not override pays no coroutine.
     * An uncaught failure prints the standard
     * `[kotlin.coroutines] Unhandled exception in coroutine` line; the
     * component stays alive (supervisor root). [retire] cancels an onStart
     * still running.
     *
     * Per-activation work belongs here; init {} is one-time structural setup.
     */
    protected open suspend fun onStart() {}

    /**
     * Lifecycle: runs synchronously inside [retire], BEFORE the component
     * scope is cancelled, so live state is still readable. Do not launch here —
     * the scope dies immediately after. Runs again on every later retire.
     */
    protected open fun onStop() {}
}

/**
 * Base class for SceneGraph components that extend Group.
 *
 * Use this for custom components that contain other nodes but don't need
 * automatic layout functionality.
 *
 * Example:
 * ```kotlin
 * class MyPanel : GroupComponent() {
 *     init {
 *         top.setFocus(true)
 *     }
 *
 *     override fun onKeyEvent(key: String, press: Boolean): Boolean {
 *         if (press && key == "OK") {
 *             // Handle OK press
 *             return true
 *         }
 *         return false
 *     }
 * }
 * ```
 */
@BrsSceneGraphComponent(extends = "Group")
public abstract class GroupComponent : ComponentBase()

/**
 * Base class for SceneGraph components that extend LayoutGroup.
 *
 * LayoutGroup components automatically arrange their children based on
 * layout attributes like `layoutDirection`, `itemSpacings`, etc.
 *
 * Use this for components that need automatic layout of child nodes.
 *
 * Example:
 * ```kotlin
 * class MyMenu : LayoutComponent() {
 *     init {
 *         // Children will be laid out automatically
 *         top.setField("layoutDirection", "vert")
 *         top.setField("itemSpacings", listOf(10))
 *     }
 * }
 * ```
 *
 * @see GroupComponent For components without automatic layout
 */
@BrsSceneGraphComponent(extends = "LayoutGroup")
public abstract class LayoutComponent : ComponentBase()

/**
 * Base class for SceneGraph Scene components.
 *
 * Scene components represent the root of a SceneGraph application.
 * There is typically one Scene per application, created via
 * `RoSGScreen.createScene()`.
 *
 * Example:
 * ```kotlin
 * class MainScreen : SceneComponent() {
 *     init {
 *         top.setFocus(true)
 *         // Initialize the scene
 *     }
 * }
 * ```
 */
@BrsSceneGraphComponent(extends = "Scene")
public abstract class SceneComponent : ComponentBase()

/**
 * Base class for SceneGraph Task components.
 *
 * Task components run on a separate thread and are used for
 * background operations like network requests, file I/O, and
 * other long-running operations.
 *
 * Subclasses declare typed inputs and outputs as `@SG*Field` properties
 * and implement [run], which executes on the task thread (blocking calls
 * are fine there). The compiler wires the task entry point automatically:
 * it emits a `__kotlinTaskMain` wrapper that calls [run], reports success
 * or failure through the `kotlinTask*` protocol fields, and sets
 * `m.top.functionName` in the generated `init()`.
 *
 * Note: Task components do not support onKeyEvent as they run
 * on a separate thread and don't participate in the focus chain.
 *
 * Example:
 * ```kotlin
 * class FetchFeedTask : TaskComponent() {
 *     @SGStringField
 *     var url: String = ""            // input
 *
 *     @SGAssocArrayField
 *     var feed: RoAssociativeArray? = null  // output
 *
 *     override fun run() {
 *         feed = httpGetJson(url)     // runs on the task thread
 *     }
 * }
 * ```
 */
@BrsSceneGraphComponent(extends = "Task")
public abstract class TaskComponent : ComponentBase() {
    /**
     * Completion protocol field (internal — do not write from user code).
     *
     * "" while running, then "error" or "done". Written LAST by the generated
     * `__kotlinTaskMain` wrapper so observers see a fully populated node.
     */
    @SGStringField(alwaysNotify = true)
    public var kotlinTaskState: String = ""

    /**
     * Completion protocol field (internal — do not write from user code).
     *
     * On failure holds `{message, number, backtrace}` from the caught error;
     * written BEFORE [kotlinTaskState] is set to "error".
     */
    @SGAssocArrayField
    public var kotlinTaskError: RoAssociativeArray? = null

    /**
     * Correlation id assigned by the render-side runner (internal protocol).
     * Lets concurrent invocations of the same task type be told apart.
     */
    @SGIntegerField
    public var kotlinTaskId: Int = 0

    /**
     * The task body. Runs on the task thread when the node's `control`
     * field is set to "RUN"; blocking calls are allowed here.
     *
     * Only `@SG*Field` properties cross the thread boundary — writes to
     * un-annotated properties from this method are silently lost.
     */
    protected abstract fun run()
}

// The DIRECT @BrsSceneGraphComponent annotation below is LOAD-BEARING twice:
// (1) isComponentBaseDeclaration (BrsIntrinsics — abstract + DIRECTLY
//     annotated) is the only thing that makes transformSceneGraphComponent
//     skip base codegen; unannotated, stdlib compilation would emit a
//     sub init() for this class into the SHARED SceneComponentKt.brs — a
//     device-wide duplicate-init collision.
// (2) getExtendsComponent (BrsComponentExtractor) resolves the synthesized
//     leaf's XML extends= from the direct superclass's annotation; unannotated
//     it would emit extends="FlowTaskComponent", a subtype no package defines —
//     node creation fails.
/**
 * Base of compiler-synthesized flowOn/spawnTask task components (flow-program
 * spec §5). Captures cross as ONE deep-copied AA; envelopes stream over
 * [flowOut] (alwaysNotify — every write must deliver); [flowCancel] is the
 * cooperative cancellation signal (FINDINGS Probe B6). Never subclass by hand.
 *
 * Extends [TaskComponent] DELIBERATELY: the leaves inherit the kotlinTask*
 * protocol fields AND all isTaskComponent machinery (the `__kotlinTaskMain`
 * wrapper, `functionName` init wiring, pump-attach exclusion).
 */
@BrsSceneGraphComponent(extends = "Task")
public abstract class FlowTaskComponent : TaskComponent() {
    /** The lifted region's captures, ONE deep-copied AA (render → task, set before RUN). */
    @SGAssocArrayField
    public var flowCaptures: RoAssociativeArray? = null

    /**
     * Kind-tagged envelope stream (task → render): emit/complete/error/outcome.
     * alwaysNotify is load-bearing — consecutive equal envelopes must each fire
     * the collector-side observer.
     */
    @SGAssocArrayField(alwaysNotify = true)
    public var flowOut: RoAssociativeArray? = null

    /** Cooperative cancellation signal (render → task); the shim checks it per emission. */
    @SGBooleanField
    public var flowCancel: Boolean = false
}

/**
 * Base class for SceneGraph ContentNode components.
 *
 * ContentNode components are used as data models for lists, grids,
 * and other data-driven UI components. They extend ContentNode and
 * can have custom fields for your data.
 *
 * Note: ContentNode components do not support onKeyEvent as they
 * are data containers, not visual components.
 *
 * Example:
 * ```kotlin
 * class VideoItem : ContentNodeComponent() {
 *     @SGStringField
 *     var title: String = ""
 *
 *     @SGStringField
 *     var thumbnailUrl: String = ""
 *
 *     @SGIntegerField
 *     var duration: Int = 0
 * }
 * ```
 *
 * Usage with RowList/MarkupGrid:
 * ```kotlin
 * val item = VideoItem()
 * item.title = "My Video"
 * item.thumbnailUrl = "pkg:/images/thumb.png"
 * rowList.content.appendChild(item)
 * ```
 */
@BrsSceneGraphComponent(extends = "ContentNode")
public abstract class ContentNodeComponent : ComponentBase()

/**
 * Base class for SceneGraph components that extend Rectangle.
 *
 * Rectangle components are visual nodes that render a colored rectangle.
 * They can have custom colors, dimensions, and can serve as backgrounds
 * or visual containers for other components.
 *
 * Rectangle nodes support these key properties:
 * - `color` - The fill color (hex format, e.g., "0xFF0000FF" for red)
 * - `width` / `height` - Dimensions in pixels
 * - `opacity` - Transparency (0.0 to 1.0)
 *
 * Example:
 * ```kotlin
 * class ColoredPanel : RectangleComponent() {
 *     init {
 *         top.setField("color", "0x0000FFFF")  // Blue
 *         top.setField("width", 200)
 *         top.setField("height", 100)
 *     }
 * }
 * ```
 *
 * @see GroupComponent For container components without rendering
 */
@BrsSceneGraphComponent(extends = "Rectangle")
public abstract class RectangleComponent : ComponentBase()

// ==================== Deprecated Type Aliases ====================
// These are provided for backwards compatibility during migration.
// They will be removed in a future version.

/**
 * @deprecated Use [SceneComponent] instead (now extends Scene, not Group).
 * Migration: If you need a Group component, use [GroupComponent].
 */
@Deprecated(
    message = "SceneNodeComponent has been renamed to SceneComponent",
    replaceWith = ReplaceWith("SceneComponent"),
    level = DeprecationLevel.WARNING
)
public typealias SceneNodeComponent = SceneComponent
