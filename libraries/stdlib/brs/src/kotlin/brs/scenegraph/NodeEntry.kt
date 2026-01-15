/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs.scenegraph

import kotlin.jvm.JvmInline

/**
 * Marker annotation for SceneGraph DSL types.
 * Signals to the compiler that this class/function is part of the compile-time DSL.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
@DslMarker
public annotation class SGNodeDsl

/**
 * Marks a companion object function as a SceneGraph layout definition.
 *
 * Functions annotated with @SGLayout are processed by the compiler to:
 * 1. Generate the XML `<children>` section for the component
 * 2. Generate a nested `Layout` class on the outer class with typed node accessors
 *
 * The compiler inspects the DSL body to extract node IDs from calls like `button(id = "xyz")`.
 * For each node ID found, a property is generated in the `Layout` class that returns
 * an `RoSGNode` reference, lazily looked up via `findNode()`.
 *
 * Example:
 * ```kotlin
 * class MainScreen : SceneNodeComponent() {
 *     companion object {
 *         @SGLayout
 *         fun defineLayout() = sceneLayout {
 *             layoutGroup(id = "mainLayout") {
 *                 button(id = "myButton", text = "Click me")
 *                 label(id = "myLabel", text = "Hello")
 *             }
 *         }
 *     }
 *
 *     // Compiler generates: class Layout(top: RoSGNode) { val mainLayout, myButton, myLabel: RoSGNode }
 *     val layout = Layout(top)
 *
 *     init {
 *         // Type-safe access to declared nodes
 *         layout.myButton.observeField("buttonSelected", "onButtonSelected")
 *         layout.myLabel.setField("text", "Updated")
 *     }
 * }
 * ```
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
public annotation class SGLayout

/**
 * Base interface for all SceneGraph node entries in the DSL.
 *
 * Each node entry describes a SceneGraph node to be generated in the component XML.
 * All properties must be compile-time constants for the compiler to extract them.
 */
public interface NodeEntry {
    /** Unique identifier for this node within the component */
    val id: String

    /** The SceneGraph node type name (e.g., "Label", "LayoutGroup", "Button") */
    val nodeType: String
}

/**
 * 2D vector for translation, scale, and other coordinate properties.
 *
 * Renders to `[x, y]` format in XML attributes.
 *
 * Example:
 * ```kotlin
 * layoutGroup(id = "myGroup", translation = Vector2D(100, 200))
 * // Generates: <LayoutGroup id="myGroup" translation="[100.0, 200.0]" />
 * ```
 */
public data class Vector2D(val x: Float, val y: Float) {
    public constructor(x: Int, y: Int) : this(x.toFloat(), y.toFloat())

    /** Formats as XML attribute value */
    public fun toXmlValue(): String = "[$x, $y]"
}

/**
 * 4D vector for clipping rectangles and similar properties.
 *
 * Renders to `[x, y, width, height]` format in XML attributes.
 */
public data class Vector4D(val x: Float, val y: Float, val width: Float, val height: Float) {
    public constructor(x: Int, y: Int, width: Int, height: Int) :
            this(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat())

    /** Formats as XML attribute value */
    public fun toXmlValue(): String = "[$x, $y, $width, $height]"
}

/**
 * Color value for SceneGraph nodes.
 *
 * Accepts hex color strings in RGBA format (e.g., "0xFF0000FF" for red).
 */
@JvmInline
public value class Color(public val hex: String) {
    init {
        require(hex.startsWith("0x") || hex.startsWith("#")) {
            "Color must be in hex format (0xRRGGBBAA or #RRGGBBAA)"
        }
    }

    /** Formats as XML attribute value */
    public fun toXmlValue(): String = hex

    public companion object {
        public val White: Color = Color("0xFFFFFFFF")
        public val Black: Color = Color("0x000000FF")
        public val Red: Color = Color("0xFF0000FF")
        public val Green: Color = Color("0x00FF00FF")
        public val Blue: Color = Color("0x0000FFFF")
        public val Transparent: Color = Color("0x00000000")
    }
}
