/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("UNUSED_PARAMETER")

package kotlin.brs.roku

import kotlin.brs.*
import kotlin.js.definedExternally

/**
 * Base class for all SceneGraph nodes.
 */
@BrsExternal
public external open class Node : RoInterface {
    /**
     * The node's unique ID.
     */
    public val id: String

    /**
     * The node's parent.
     */
    public val parent: Node?

    /**
     * Gets a child node by index.
     */
    public fun getChild(index: Int): Node?

    /**
     * Gets the number of children.
     */
    public fun getChildCount(): Int

    /**
     * Appends a child node.
     */
    public fun appendChild(child: Node)

    /**
     * Inserts a child at the specified index.
     */
    public fun insertChild(child: Node, index: Int)

    /**
     * Removes a child at the specified index.
     */
    public fun removeChild(index: Int)

    /**
     * Removes all children.
     */
    public fun removeChildren(index: Int, count: Int)

    /**
     * Removes all children.
     */
    public fun removeChildrenIndex(startIndex: Int, endIndex: Int)

    /**
     * Creates a child node of the specified type.
     */
    public fun createChild(nodeType: String): Node

    /**
     * Finds a node by ID in the subtree.
     */
    public fun findNode(id: String): Node?

    /**
     * Observes a field for changes.
     */
    public fun observeField(fieldName: String, callback: String)

    /**
     * Observes a field for changes (scoped to port).
     */
    public fun observeFieldScoped(fieldName: String, port: RoMessagePort)

    /**
     * Stops observing a field.
     */
    public fun unobserveField(fieldName: String)

    /**
     * Stops observing a field (scoped).
     */
    public fun unobserveFieldScoped(fieldName: String)

    /**
     * Checks if a field exists.
     */
    public fun hasField(fieldName: String): Boolean

    /**
     * Gets a field value.
     */
    public fun getField(fieldName: String): Any?

    /**
     * Sets a field value.
     */
    public fun setField(fieldName: String, value: Any?)

    /**
     * Sets multiple fields at once.
     */
    public fun setFields(fields: RoAssociativeArray)

    /**
     * Updates fields only if values differ.
     */
    public fun update(fields: RoAssociativeArray, addFields: Boolean = definedExternally)

    /**
     * Calls a function on this node.
     */
    public fun callFunc(funcName: String, vararg args: Any?): Any?

    /**
     * Gets all fields as an associative array.
     */
    public fun getFields(): RoAssociativeArray

    /**
     * Checks if this is the same node as another.
     */
    public fun isSameNode(other: Node): Boolean

    /**
     * Gets the subtype of this node.
     */
    public fun subtype(): String

    /**
     * Gets the parent subtype.
     */
    public fun parentSubtype(subtype: String): String

    /**
     * Checks if this node is a subtype of another.
     */
    public fun isSubtype(subtype: String): Boolean
}

/**
 * Group node - container for other nodes.
 */
@BrsExternal
public external open class Group : Node {
    public var visible: Boolean
    public var opacity: Float
    public var translation: RoArray // [x, y]
    public var rotation: Float
    public var scale: RoArray // [x, y]
    public var scaleRotateCenter: RoArray // [x, y]
    public var clippingRect: RoArray // [x, y, width, height]
    public var renderPass: Int
    public var muteAudioGuide: Boolean
    public var enableRenderTracking: Boolean
    public var renderTracking: String
}

/**
 * Rectangle node - draws a solid color rectangle.
 */
@BrsExternal
public external class Rectangle : Group {
    public var width: Float
    public var height: Float
    public var color: String
    public var blendingEnabled: Boolean
}

/**
 * Label node - displays text.
 */
@BrsExternal
public external class Label : Group {
    public var text: String
    public var color: String
    public var font: RoInterface // roFont
    public var horizAlign: String // "left", "center", "right"
    public var vertAlign: String // "top", "center", "bottom"
    public var width: Float
    public var height: Float
    public var numLines: Int
    public var maxLines: Int
    public var wrap: Boolean
    public var lineSpacing: Float
    public var truncateOnDelimiter: String
    public var ellipsizeOnBoundary: Boolean
    public var wordBreakChars: String
    public var ellipsisText: String
    public var isTextEllipsized: Boolean
}

/**
 * Poster node - displays an image.
 */
@BrsExternal
public external class Poster : Group {
    public var uri: String
    public var width: Float
    public var height: Float
    public var loadWidth: Float
    public var loadHeight: Float
    public var loadDisplayMode: String
    public var loadStatus: String
    public var loadingBitmapUri: String
    public var failedBitmapUri: String
    public var loadingBitmapOpacity: Float
    public var failedBitmapOpacity: Float
    public var blendColor: String
    public var audioGuideText: String
}

/**
 * Video node - plays video content.
 */
@BrsExternal
public external class Video : Group {
    public var content: Node?
    public var contentIsPlaylist: Boolean
    public var control: String // "play", "pause", "stop", "resume", etc.
    public var state: String
    public var position: Float
    public var duration: Float
    public var seek: Float
    public var seekMode: String
    public var loop: Boolean
    public var mute: Boolean
    public var trickPlayBar: Node?
    public var bufferingBar: Node?
    public var retrievingBar: Node?
    public var enableUI: Boolean
    public var enableTrickPlay: Boolean
    public var disableScreenSaver: Boolean
    public var width: Float
    public var height: Float
    public var videoFormat: String
    public var streamFormat: String
}

/**
 * Audio node - plays audio content.
 */
@BrsExternal
public external class Audio : Node {
    public var content: Node?
    public var contentIsPlaylist: Boolean
    public var control: String
    public var state: String
    public var loop: Boolean
    public var streamFormat: String
}

/**
 * Timer node - fires events at intervals.
 */
@BrsExternal
public external class Timer : Node {
    public var duration: Float
    public var repeat: Boolean
    public var control: String // "start", "stop"
    public var fire: Boolean
}

// Task node is defined in task.kt with full documentation

/**
 * ContentNode - holds content metadata.
 */
@BrsExternal
public external class ContentNode : Node {
    public var title: String
    public var description: String
    public var sdPosterUrl: String
    public var hdPosterUrl: String
    public var url: String
    public var streamFormat: String
    public var length: Int
    public var rating: String
    public var starRating: Int
    public var releaseDate: String
    public var genres: RoArray
    public var directors: RoArray
    public var actors: RoArray
}

/**
 * RowList node - displays scrolling rows.
 */
@BrsExternal
public external class RowList : Group {
    public var content: Node?
    public var itemSize: RoArray
    public var itemSpacing: RoArray
    public var rowItemSize: RoArray
    public var rowItemSpacing: RoArray
    public var numRows: Int
    public var focusRow: Int
    public var rowFocusAnimationStyle: String
    public var vertFocusAnimationStyle: String
    public var itemFocused: Int
    public var itemSelected: Int
    public var itemUnfocused: Int
    public var rowItemFocused: RoArray
    public var rowItemSelected: RoArray
    public var currFocusRow: Float
    public var currFocusSection: Int
    public var currFocusColumn: Float
}

/**
 * MarkupGrid node - displays a grid of items.
 */
@BrsExternal
public external class MarkupGrid : Group {
    public var content: Node?
    public var itemSize: RoArray
    public var itemSpacing: RoArray
    public var numColumns: Int
    public var numRows: Int
    public var focusRow: Int
    public var focusColumn: Int
    public var horizFocusAnimationStyle: String
    public var vertFocusAnimationStyle: String
    public var itemFocused: Int
    public var itemSelected: Int
    public var itemUnfocused: Int
    public var currFocusRow: Float
    public var currFocusColumn: Float
    public var drawFocusFeedback: Boolean
    public var drawFocusFeedbackOnTop: Boolean
    public var wrap: Boolean
    public var fixedLayout: Boolean
}

/**
 * BusySpinner node - displays a loading indicator.
 */
@BrsExternal
public external class BusySpinner : Group {
    public var poster: Node?
    public var control: String // "start", "stop"
}

/**
 * Animation node - animates field values.
 */
@BrsExternal
public external class Animation : Node {
    public var control: String // "start", "stop", "pause", "resume"
    public var state: String
    public var delay: Float
    public var duration: Float
    public var easeFunction: String
    public var repeat: Boolean
    public var optional: Boolean
}

/**
 * FloatFieldInterpolator - animates float fields.
 */
@BrsExternal
public external class FloatFieldInterpolator : Node {
    public var fieldToInterp: String
    public var key: RoArray
    public var keyValue: RoArray
}

/**
 * Vector2DFieldInterpolator - animates 2D vector fields.
 */
@BrsExternal
public external class Vector2DFieldInterpolator : Node {
    public var fieldToInterp: String
    public var key: RoArray
    public var keyValue: RoArray
}

/**
 * ColorFieldInterpolator - animates color fields.
 */
@BrsExternal
public external class ColorFieldInterpolator : Node {
    public var fieldToInterp: String
    public var key: RoArray
    public var keyValue: RoArray
}
