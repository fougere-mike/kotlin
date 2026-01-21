/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs.scenegraph

/**
 * Layout direction for LayoutGroup nodes.
 */
public enum class LayoutDirection {
    /** Arrange children horizontally (left to right) */
    horiz,
    /** Arrange children vertically (top to bottom) */
    vert
}

/**
 * Horizontal alignment options for LayoutGroup children.
 */
public enum class HorizAlignment {
    /** Align children to the left edge */
    left,
    /** Center children horizontally */
    center,
    /** Align children to the right edge */
    right,
    /** Custom horizontal alignment (use child translations) */
    custom
}

/**
 * Vertical alignment options for LayoutGroup children.
 */
public enum class VertAlignment {
    /** Align children to the top edge */
    top,
    /** Center children vertically */
    center,
    /** Align children to the bottom edge */
    bottom,
    /** Custom vertical alignment (use child translations) */
    custom
}

/**
 * Interface for nodes that can contain child nodes.
 */
public interface ContainerNodeEntry : NodeEntry, CommonNodeDefaults {
    /** List of child nodes within this container */
    val children: List<NodeEntry>
}

/**
 * Group node - basic container for organizing child nodes.
 *
 * A Group node doesn't affect the visual appearance or layout of its children
 * but provides a way to organize and manipulate multiple nodes together.
 *
 * Example:
 * ```kotlin
 * group(id = "myGroup", translation = Vector2D(100, 100)) {
 *     label(id = "label1", text = "First")
 *     label(id = "label2", text = "Second")
 * }
 * ```
 */
@SGNodeDsl
public data class GroupEntry(
    override val id: String,
    override val translation: Vector2D? = null,
    override val rotation: Float? = null,
    override val scale: Vector2D? = null,
    override val scaleRotateCenter: Vector2D? = null,
    override val opacity: Float? = null,
    override val visible: Boolean? = null,
    override val inheritParentOpacity: Boolean? = null,
    override val inheritParentTransform: Boolean? = null,
    override val clippingRect: Vector4D? = null,
    override val renderGroup: Boolean? = null,
    override val focusable: Boolean? = null,
    override val renderPass: Int? = null,
    override val children: List<NodeEntry> = emptyList()
) : ContainerNodeEntry {
    override val nodeType: String get() = "Group"
}

/**
 * LayoutGroup node - arranges children in a row or column with automatic spacing.
 *
 * LayoutGroup manages the position of its child nodes by arranging them
 * horizontally or vertically with configurable spacing and alignment.
 *
 * Example:
 * ```kotlin
 * layoutGroup(
 *     id = "toolbar",
 *     layoutDirection = LayoutDirection.horiz,
 *     itemSpacings = listOf(20),
 *     vertAlignment = VertAlignment.center
 * ) {
 *     button(id = "btn1", text = "Action 1")
 *     button(id = "btn2", text = "Action 2")
 * }
 * ```
 */
@SGNodeDsl
public data class LayoutGroupEntry(
    override val id: String,
    /**
     * Direction to arrange children.
     * - `horiz`: Arrange left to right
     * - `vert`: Arrange top to bottom
     */
    val layoutDirection: LayoutDirection = LayoutDirection.horiz,
    /**
     * Horizontal alignment of children when layoutDirection is `vert`.
     */
    val horizAlignment: HorizAlignment? = null,
    /**
     * Vertical alignment of children when layoutDirection is `horiz`.
     */
    val vertAlignment: VertAlignment? = null,
    /**
     * Spacing between children in pixels.
     *
     * Can be a single value applied uniformly, or multiple values
     * applied to each gap sequentially. The last value repeats for
     * any remaining gaps.
     *
     * Example: `listOf(10)` applies 10px between all children.
     * Example: `listOf(10, 20)` applies 10px after first child, 20px after the rest.
     */
    val itemSpacings: List<Int>? = null,
    /**
     * Whether spacing is added after (true) or before (false) each child.
     */
    val addItemSpacingAfterChild: Boolean? = null,
    override val translation: Vector2D? = null,
    override val rotation: Float? = null,
    override val scale: Vector2D? = null,
    override val scaleRotateCenter: Vector2D? = null,
    override val opacity: Float? = null,
    override val visible: Boolean? = null,
    override val inheritParentOpacity: Boolean? = null,
    override val inheritParentTransform: Boolean? = null,
    override val clippingRect: Vector4D? = null,
    override val renderGroup: Boolean? = null,
    override val focusable: Boolean? = null,
    override val renderPass: Int? = null,
    override val children: List<NodeEntry> = emptyList()
) : ContainerNodeEntry {
    override val nodeType: String get() = "LayoutGroup"
}

/**
 * Custom component node - embeds a user-defined Roku component with arbitrary attributes.
 *
 * Use this for embedding custom SceneGraph components that aren't built into the DSL.
 * Custom attributes can be set using the `attr()` method in the builder lambda.
 *
 * Example:
 * ```kotlin
 * component("MyCustomButton", id = "btn1") {
 *     attr("customField", "value")
 *     attr("iconSize", 48)
 * }
 *
 * component("CustomContainer", id = "container") {
 *     attr("padding", 20)
 *     children {
 *         label(id = "nested", text = "Inside container")
 *     }
 * }
 * ```
 */
@SGNodeDsl
public data class CustomComponentEntry(
    override val id: String,
    /** The name of the custom component type (e.g., "MyCustomButton") */
    val componentType: String,
    /** Custom attributes set via attr() calls */
    val customAttributes: Map<String, String> = emptyMap(),
    override val translation: Vector2D? = null,
    override val rotation: Float? = null,
    override val scale: Vector2D? = null,
    override val scaleRotateCenter: Vector2D? = null,
    override val opacity: Float? = null,
    override val visible: Boolean? = null,
    override val inheritParentOpacity: Boolean? = null,
    override val inheritParentTransform: Boolean? = null,
    override val clippingRect: Vector4D? = null,
    override val renderGroup: Boolean? = null,
    override val focusable: Boolean? = null,
    override val renderPass: Int? = null,
    override val children: List<NodeEntry> = emptyList()
) : ContainerNodeEntry {
    override val nodeType: String get() = componentType
}
