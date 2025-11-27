/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("UNUSED_PARAMETER")

package kotlin.brs.roku

import kotlin.brs.*

/**
 * Dialog node - standard dialog with buttons.
 */
@BrsExternal
public external class Dialog : Group {
    public var title: String
    public var message: String
    public var buttons: RoArray
    public var buttonGroup: Node?
    public var buttonSelected: Int
    public var buttonFocused: Int
    public var optionsDialog: Boolean
    public var iconUri: String
    public var backgroundUri: String
}

/**
 * StandardDialog - base for various dialog types.
 */
@BrsExternal
public external open class StandardDialog : Group {
    public var title: String
    public var message: String
    public var contentData: Node?
    public var buttons: RoArray
    public var buttonSelected: Int
    public var wasClosed: Boolean
    public var palette: Node?
}

/**
 * KeyboardDialog - text input dialog.
 */
@BrsExternal
public external class KeyboardDialog : StandardDialog {
    public var text: String
    public var keyboard: Node?
    public var textEditBox: Node?
}

/**
 * PinDialog - PIN entry dialog.
 */
@BrsExternal
public external class PinDialog : StandardDialog {
    public var pin: String
    public var pinLength: Int
    public var pinDisplay: String
    public var secureMode: Boolean
    public var showPinDisplay: Boolean
}

/**
 * ProgressDialog - shows progress.
 */
@BrsExternal
public external class ProgressDialog : StandardDialog

/**
 * Keyboard node - on-screen keyboard.
 */
@BrsExternal
public external class Keyboard : Group {
    public var text: String
    public var textEditBox: Node?
    public var showTextEditBox: Boolean
    public var lowercaseChars: String
    public var uppercaseChars: String
    public var symbols: String
    public var voiceEnabled: Boolean
    public var voiceButtonPresent: Boolean
}

/**
 * TextEditBox - text input field.
 */
@BrsExternal
public external class TextEditBox : Group {
    public var text: String
    public var hintText: String
    public var hintTextColor: String
    public var width: Float
    public var cursorPosition: Int
    public var maxTextLength: Int
    public var active: Boolean
    public var clearOnDownKey: Boolean
    public var textColor: String
    public var backgroundUri: String
}

/**
 * SimpleKeyboard - simpler keyboard variant.
 */
@BrsExternal
public external class SimpleKeyboard : Group {
    public var text: String
    public var lowercaseChars: String
    public var uppercaseChars: String
    public var showVKB: Boolean
    public var textEditBox: Node?
}

/**
 * MiniKeyboard - compact keyboard.
 */
@BrsExternal
public external class MiniKeyboard : Group {
    public var text: String
    public var focusedKeyBackground: String
    public var keyColor: String
    public var keyboardBitmapUri: String
    public var showTextEditBox: Boolean
    public var textEditBox: Node?
}

/**
 * Button node - standard button.
 */
@BrsExternal
public external class Button : Group {
    public var text: String
    public var textColor: String
    public var focusedTextColor: String
    public var textFont: RoInterface
    public var focusedTextFont: RoInterface
    public var iconUri: String
    public var focusedIconUri: String
    public var background: String
    public var focusedBackground: String
    public var backgroundBlendColor: String
    public var focusedBackgroundBlendColor: String
    public var focusBitmapBlendColor: String
    public var focusBitmapUri: String
    public var focusFootprintBlendColor: String
    public var focusFootprintBitmapUri: String
    public var height: Float
    public var maxWidth: Float
    public var minWidth: Float
    public var showFocusFootprint: Boolean
    public var buttonSelected: Boolean
}

/**
 * ButtonGroup node - group of buttons.
 */
@BrsExternal
public external class ButtonGroup : Group {
    public var buttons: RoArray
    public var focusButton: Int
    public var buttonSelected: Int
    public var buttonFocused: Int
    public var minWidth: Float
    public var maxWidth: Float
    public var textColor: String
    public var focusedTextColor: String
    public var textFont: RoInterface
    public var focusedTextFont: RoInterface
    public var iconUri: String
    public var focusedIconUri: String
}

/**
 * RadioButtonList - list of radio buttons.
 */
@BrsExternal
public external class RadioButtonList : Group {
    public var content: Node?
    public var checkedItem: Int
    public var checkedState: RoArray
    public var focusedItem: Int
    public var itemSize: RoArray
    public var itemSpacing: RoArray
    public var textColor: String
    public var focusedTextColor: String
    public var textFont: RoInterface
    public var focusedTextFont: RoInterface
    public var color: String
    public var focusedColor: String
    public var checkedIconUri: String
    public var focusedCheckedIconUri: String
    public var uncheckedIconUri: String
    public var focusedUncheckedIconUri: String
}

/**
 * CheckList - list with checkboxes.
 */
@BrsExternal
public external class CheckList : Group {
    public var content: Node?
    public var checkedItem: RoArray
    public var checkedState: RoArray
    public var focusedItem: Int
    public var itemSize: RoArray
    public var itemSpacing: RoArray
    public var textColor: String
    public var focusedTextColor: String
    public var textFont: RoInterface
    public var focusedTextFont: RoInterface
    public var color: String
    public var focusedColor: String
    public var checkedIconUri: String
    public var focusedCheckedIconUri: String
    public var uncheckedIconUri: String
    public var focusedUncheckedIconUri: String
}

/**
 * LabelList - scrolling list of text labels.
 */
@BrsExternal
public external class LabelList : Group {
    public var content: Node?
    public var itemSize: RoArray
    public var itemSpacing: RoArray
    public var numRows: Int
    public var focusRow: Float
    public var itemFocused: Int
    public var itemSelected: Int
    public var itemUnfocused: Int
    public var currFocusRow: Float
    public var currFocusSection: Int
    public var textColor: String
    public var focusedTextColor: String
    public var textFont: RoInterface
    public var focusedTextFont: RoInterface
    public var sectionDividerFont: RoInterface
    public var sectionDividerTextColor: String
    public var sectionDividerSpacing: Float
    public var sectionDividerHeight: Float
    public var sectionDividerMinWidth: Float
    public var sectionDividerLeftOffset: Float
    public var color: String
    public var focusedColor: String
    public var drawFocusFeedback: Boolean
    public var drawFocusFeedbackOnTop: Boolean
    public var wrap: Boolean
}

/**
 * PosterGrid - grid of poster images.
 */
@BrsExternal
public external class PosterGrid : Group {
    public var content: Node?
    public var itemSize: RoArray
    public var itemSpacing: RoArray
    public var numColumns: Int
    public var numRows: Int
    public var caption1Font: RoInterface
    public var caption1Color: String
    public var focusedCaption1Color: String
    public var caption2Font: RoInterface
    public var caption2Color: String
    public var focusedCaption2Color: String
    public var focusColumn: Int
    public var focusRow: Int
    public var itemFocused: Int
    public var itemSelected: Int
    public var itemUnfocused: Int
    public var currFocusColumn: Float
    public var currFocusRow: Float
    public var wrap: Boolean
}
