/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs.scenegraph

import kotlin.brs.roku.RoSGNode

/**
 * Base class for generated layout accessors.
 *
 * The compiler generates a subclass with typed properties for each declared node.
 * Each property is lazily initialized using `findNode()` and cached for subsequent access.
 *
 * Example: For a layout with nodes "mainLayout" and "counterLabel",
 * the compiler generates BrightScript like:
 *
 * ```brightscript
 * function MainScreen_Layout_create(top)
 *     instance = {}
 *     instance._top = top
 *     instance._mainLayout = invalid
 *     instance._counterLabel = invalid
 *     return instance
 * end function
 *
 * function MainScreen_Layout_get_mainLayout(this)
 *     if this._mainLayout = invalid then
 *         this._mainLayout = this._top.findNode("mainLayout")
 *     end if
 *     return this._mainLayout
 * end function
 *
 * function MainScreen_Layout_get_counterLabel(this)
 *     if this._counterLabel = invalid then
 *         this._counterLabel = this._top.findNode("counterLabel")
 *     end if
 *     return this._counterLabel
 * end function
 * ```
 *
 * @param top The component's top node used for `findNode()` lookups.
 */
public abstract class SceneLayoutBase(protected val top: RoSGNode) {
    /**
     * Gets a node by ID dynamically (fallback for non-declared nodes).
     *
     * @param id The node ID to find.
     * @return The found node, or null if not found.
     */
    public operator fun get(id: String): RoSGNode? = top.findNode(id)
}

/**
 * Marker class returned by `sceneLayout { }` at compile time.
 *
 * The compiler processes `@SGLayout` properties and replaces this class
 * with the generated typed layout accessor class.
 *
 * At compile time, this holds the DSL-declared nodes for XML generation.
 * At runtime, users access the generated typed accessor instead of this class.
 *
 * @property nodes The list of top-level node entries declared in the DSL.
 */
public class SceneLayoutDefinition internal constructor(
    internal val nodes: List<NodeEntry>
)

/**
 * DSL entry point for defining component layout.
 *
 * Use this function with `@SGLayout` to declare the XML children and typed accessors
 * for a SceneGraph component.
 *
 * Example:
 * ```kotlin
 * class MainScreen : SceneNodeComponent() {
 *     @SGLayout
 *     val layout = sceneLayout {
 *         layoutGroup(
 *             id = "mainLayout",
 *             layoutDirection = LayoutDirection.horiz,
 *             itemSpacings = listOf(20)
 *         ) {
 *             button(id = "incrementButton", text = "Increment")
 *             label(id = "counterLabel", text = "0")
 *         }
 *     }
 *
 *     init {
 *         // Type-safe access to declared nodes
 *         layout.incrementButton.observeField("buttonSelected", "onButtonPressed")
 *     }
 *
 *     fun updateCounter(value: Int) {
 *         layout.counterLabel.setField("text", value.toString())
 *     }
 * }
 * ```
 *
 * The compiler processes this and generates:
 * 1. XML `<children>` section with the declared node hierarchy
 * 2. Typed layout accessor class with lazy-cached properties for each node
 *
 * @param init The DSL builder lambda that declares the component's children.
 * @return A SceneLayoutDefinition that the compiler transforms.
 */
@SGNodeDsl
public fun sceneLayout(init: LayoutBuilder.() -> Unit): SceneLayoutDefinition {
    val builder = LayoutBuilder()
    builder.init()
    return SceneLayoutDefinition(builder.build())
}
