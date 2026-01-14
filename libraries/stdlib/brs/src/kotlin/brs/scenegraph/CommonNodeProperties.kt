/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs.scenegraph

/**
 * Common properties inherited by all SceneGraph nodes.
 *
 * These map to the base Node class properties in Roku's SceneGraph framework.
 * All properties are optional (nullable) - only non-null values are emitted in XML.
 */
public interface CommonNodeProperties {
    /**
     * Position relative to the parent node's coordinate system.
     * XML format: `translation="[x, y]"`
     */
    val translation: Vector2D?

    /**
     * Rotation in degrees around the scaleRotateCenter point.
     * XML format: `rotation="45.0"`
     */
    val rotation: Float?

    /**
     * Scale factors for X and Y axes.
     * XML format: `scale="[1.5, 1.5]"`
     */
    val scale: Vector2D?

    /**
     * Center point for rotation and scaling operations.
     * XML format: `scaleRotateCenter="[50, 50]"`
     */
    val scaleRotateCenter: Vector2D?

    /**
     * Opacity from 0.0 (fully transparent) to 1.0 (fully opaque).
     * XML format: `opacity="0.5"`
     */
    val opacity: Float?

    /**
     * Whether this node and its children are rendered.
     * XML format: `visible="true"`
     */
    val visible: Boolean?

    /**
     * Whether this node inherits opacity from its parent.
     * XML format: `inheritParentOpacity="true"`
     */
    val inheritParentOpacity: Boolean?

    /**
     * Whether this node inherits transform (translation/rotation/scale) from parent.
     * XML format: `inheritParentTransform="true"`
     */
    val inheritParentTransform: Boolean?

    /**
     * Clipping rectangle that constrains rendering of this node and children.
     * XML format: `clippingRect="[0, 0, 100, 100]"`
     */
    val clippingRect: Vector4D?

    /**
     * Render group flag for optimization.
     * When true, node and children are rendered as a single bitmap.
     * XML format: `renderGroup="true"`
     */
    val renderGroup: Boolean?

    /**
     * Whether this node can receive focus.
     * XML format: `focusable="true"`
     */
    val focusable: Boolean?

    /**
     * Render pass assignment for multi-pass rendering.
     * XML format: `renderPass="1"`
     */
    val renderPass: Int?
}

/**
 * Provides default (null) values for all common node properties.
 *
 * Node entry data classes should implement this interface to get sensible defaults,
 * allowing users to only specify the properties they need.
 */
public interface CommonNodeDefaults : CommonNodeProperties {
    override val translation: Vector2D? get() = null
    override val rotation: Float? get() = null
    override val scale: Vector2D? get() = null
    override val scaleRotateCenter: Vector2D? get() = null
    override val opacity: Float? get() = null
    override val visible: Boolean? get() = null
    override val inheritParentOpacity: Boolean? get() = null
    override val inheritParentTransform: Boolean? get() = null
    override val clippingRect: Vector4D? get() = null
    override val renderGroup: Boolean? get() = null
    override val focusable: Boolean? get() = null
    override val renderPass: Int? get() = null
}
