/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs.scenegraph

/**
 * Horizontal alignment options for text in Label nodes.
 */
public enum class HorizAlign {
    /** Align text to the left edge */
    left,
    /** Center text horizontally */
    center,
    /** Align text to the right edge */
    right
}

/**
 * Vertical alignment options for text in Label nodes.
 */
public enum class VertAlign {
    /** Align text to the top edge */
    top,
    /** Center text vertically */
    center,
    /** Align text to the bottom edge */
    bottom
}

/**
 * Truncation mode for Label text that overflows its bounds.
 */
public enum class Truncation {
    /** No truncation - text may overflow */
    none,
    /** Truncate at end and add ellipsis */
    truncateEnd
}

/**
 * Wrap mode for multiline text in Label nodes.
 */
public enum class WordWrap {
    /** Wrap at word boundaries */
    word,
    /** Wrap at character boundaries */
    character
}

/**
 * Content loading display mode for Poster nodes.
 */
public enum class LoadingBitmapStyle {
    /** No loading indicator */
    noScale,
    /** Scale loading indicator to fill bounds */
    scale,
    /** Center loading indicator */
    center
}

/**
 * Poster blending mode for compositing.
 */
public enum class BlendingMode {
    /** Normal alpha blending */
    normal,
    /** Additive blending */
    add,
    /** Multiply blending */
    multiply
}

/**
 * Label node - displays text with customizable font, color, and alignment.
 *
 * Example:
 * ```kotlin
 * label(
 *     id = "title",
 *     text = "Welcome",
 *     font = "font:LargeBoldSystemFont",
 *     color = Color("0xFFFFFFFF"),
 *     horizAlign = HorizAlign.center
 * )
 * ```
 */
@SGNodeDsl
public data class LabelEntry(
    override val id: String,
    /** Text to display */
    val text: String? = null,
    /**
     * Font specification.
     * Format: "font:FontName" for system fonts or "pkg:/fonts/custom.otf,size" for custom fonts.
     */
    val font: String? = null,
    /** Text color in RGBA hex format */
    val color: Color? = null,
    /** Horizontal text alignment within the label bounds */
    val horizAlign: HorizAlign? = null,
    /** Vertical text alignment within the label bounds */
    val vertAlign: VertAlign? = null,
    /** Width constraint for the label */
    val width: Float? = null,
    /** Height constraint for the label */
    val height: Float? = null,
    /** Maximum width before wrapping or truncation */
    val maxWidth: Float? = null,
    /** Maximum number of lines to display */
    val maxLines: Int? = null,
    /** Word wrap mode for multiline labels */
    val wrap: Boolean? = null,
    /** Truncation behavior when text exceeds bounds */
    val truncateOnDelimiter: String? = null,
    /** Line spacing multiplier (1.0 = normal) */
    val lineSpacing: Float? = null,
    /** Display mode for line breaks */
    val displayPartialLines: Boolean? = null,
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
    override val renderPass: Int? = null
) : NodeEntry, CommonNodeDefaults {
    override val nodeType: String get() = "Label"
}

/**
 * Poster node - displays images from URLs or local files.
 *
 * Example:
 * ```kotlin
 * poster(
 *     id = "heroImage",
 *     uri = "pkg:/images/hero.png",
 *     width = 1920f,
 *     height = 1080f
 * )
 * ```
 */
@SGNodeDsl
public data class PosterEntry(
    override val id: String,
    /**
     * Image URI.
     * Can be a local path (pkg:/images/foo.png) or a remote URL (https://...).
     */
    val uri: String? = null,
    /** Display width in pixels */
    val width: Float? = null,
    /** Display height in pixels */
    val height: Float? = null,
    /** How to display loading state */
    val loadingBitmapStyle: LoadingBitmapStyle? = null,
    /** URI for image to show while loading */
    val loadingBitmapUri: String? = null,
    /** URI for image to show on load failure */
    val failedBitmapUri: String? = null,
    /** Blending mode for compositing */
    val blendingMode: BlendingMode? = null,
    /** Tint color applied over the image */
    val blendColor: Color? = null,
    /** Whether to use hardware-accelerated loading */
    val loadSync: Boolean? = null,
    /** Optional mask bitmap */
    val maskUri: String? = null,
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
    override val renderPass: Int? = null
) : NodeEntry, CommonNodeDefaults {
    override val nodeType: String get() = "Poster"
}

/**
 * Rectangle node - renders a colored rectangle, optionally rounded.
 *
 * Example:
 * ```kotlin
 * rectangle(
 *     id = "background",
 *     width = 400f,
 *     height = 300f,
 *     color = Color("0x333333FF")
 * )
 * ```
 */
@SGNodeDsl
public data class RectangleEntry(
    override val id: String,
    /** Width in pixels */
    val width: Float? = null,
    /** Height in pixels */
    val height: Float? = null,
    /** Fill color */
    val color: Color? = null,
    /** Blending mode for compositing */
    val blendingMode: BlendingMode? = null,
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
    override val renderPass: Int? = null
) : NodeEntry, CommonNodeDefaults {
    override val nodeType: String get() = "Rectangle"
}
