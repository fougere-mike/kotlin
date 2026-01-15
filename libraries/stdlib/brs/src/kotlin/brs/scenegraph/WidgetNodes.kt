/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs.scenegraph

/**
 * Button node - standard UI button with text.
 *
 * Button nodes support focus states and emit `buttonSelected` field changes
 * when pressed (OK button on remote).
 *
 * Example:
 * ```kotlin
 * button(
 *     id = "submitButton",
 *     text = "Submit",
 *     focusedTextColor = Color("0x00FF00FF"),
 *     focusBitmapUri = "pkg:/images/button_focus.9.png"
 * )
 * ```
 */
@SGNodeDsl
public data class ButtonEntry(
    override val id: String,
    /** Button label text */
    val text: String? = null,
    /** Font for the button text */
    val textFont: String? = null,
    /** Text color when not focused */
    val textColor: Color? = null,
    /** Text color when focused */
    val focusedTextColor: Color? = null,
    /** Background bitmap when not focused (9-patch supported) */
    val backgroundUri: String? = null,
    /** Background bitmap when focused (9-patch supported) */
    val focusBitmapUri: String? = null,
    /** Footer hint displayed below the button when focused */
    val focusFootprint: String? = null,
    /** Minimum button width */
    val minWidth: Float? = null,
    /** Maximum button width */
    val maxWidth: Float? = null,
    /** Button height */
    val height: Float? = null,
    /** Icon displayed alongside text */
    val iconUri: String? = null,
    /** Icon displayed when button is focused */
    val focusedIconUri: String? = null,
    /** Whether the button shows a checkmark */
    val showFocusFootprint: Boolean? = null,
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
    override val nodeType: String get() = "Button"
}

/**
 * ButtonGroup node - container that manages focus navigation among child buttons.
 *
 * ButtonGroup automatically handles focus movement between its Button children
 * and emits `buttonSelected` and `buttonFocused` field changes.
 *
 * Example:
 * ```kotlin
 * buttonGroup(id = "actionButtons") {
 *     button(id = "save", text = "Save")
 *     button(id = "cancel", text = "Cancel")
 *     button(id = "delete", text = "Delete")
 * }
 * ```
 */
@SGNodeDsl
public data class ButtonGroupEntry(
    override val id: String,
    /** Direction to arrange buttons (horiz or vert) */
    val layoutDirection: LayoutDirection? = null,
    /** Spacing between buttons */
    val itemSpacings: List<Int>? = null,
    /** Index of initially focused button */
    val focusedButton: Int? = null,
    /** Whether focus wraps around when at edges */
    val wrapDividerWidth: Int? = null,
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
    override val nodeType: String get() = "ButtonGroup"
}

/**
 * TextEditBox node - text input field for keyboard entry.
 *
 * Displays a text input field that shows the system keyboard when focused.
 *
 * Example:
 * ```kotlin
 * textEditBox(
 *     id = "searchInput",
 *     hint = "Search...",
 *     textColor = Color.White,
 *     maxTextLength = 100
 * )
 * ```
 */
@SGNodeDsl
public data class TextEditBoxEntry(
    override val id: String,
    /** Current text value */
    val text: String? = null,
    /** Placeholder text shown when empty */
    val hint: String? = null,
    /** Text color */
    val textColor: Color? = null,
    /** Hint text color */
    val hintTextColor: Color? = null,
    /** Text font */
    val textFont: String? = null,
    /** Maximum characters allowed */
    val maxTextLength: Int? = null,
    /** Width of the input field */
    val width: Float? = null,
    /** Whether this is a password field */
    val secureMode: Boolean? = null,
    /** Cursor position in the text */
    val cursorPosition: Int? = null,
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
    override val nodeType: String get() = "TextEditBox"
}

/**
 * Keyboard node - on-screen keyboard for text input.
 *
 * Example:
 * ```kotlin
 * keyboard(
 *     id = "searchKeyboard",
 *     keyboardType = KeyboardType.alphanumeric
 * )
 * ```
 */
public enum class KeyboardType {
    alphanumeric,
    email,
    url,
    password,
    numpad
}

@SGNodeDsl
public data class KeyboardEntry(
    override val id: String,
    /** Current text entered */
    val text: String? = null,
    /** Type of keyboard layout */
    val keyboardType: KeyboardType? = null,
    /** Whether keyboard shows lowercase letters initially */
    val lowercase: Boolean? = null,
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
    override val nodeType: String get() = "Keyboard"
}
