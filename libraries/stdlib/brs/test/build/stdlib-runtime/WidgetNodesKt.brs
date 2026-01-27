function ButtonEntry_create_152svm_k_(id as String, text = invalid, textFont = invalid, textColor = invalid, focusedTextColor = invalid, backgroundUri = invalid, focusBitmapUri = invalid, focusFootprint = invalid, minWidth = invalid, maxWidth = invalid, height = invalid, iconUri = invalid, focusedIconUri = invalid, showFocusFootprint = invalid, translation = invalid, rotation = invalid, scale = invalid, scaleRotateCenter = invalid, opacity = invalid, visible = invalid, inheritParentOpacity = invalid, inheritParentTransform = invalid, clippingRect = invalid, renderGroup = invalid, focusable = invalid, renderPass = invalid) as Object
    this = {}
    this.__type = "ButtonEntry"
    this.__proto = ["ButtonEntry", "NodeEntry", "CommonNodeDefaults", "CommonNodeProperties"]
    this.__id = __kotlin_nextObjectId()
    this.id = id
    this.text = text
    this.textFont = textFont
    this.textColor = textColor
    this.focusedTextColor = focusedTextColor
    this.backgroundUri = backgroundUri
    this.focusBitmapUri = focusBitmapUri
    this.focusFootprint = focusFootprint
    this.minWidth = minWidth
    this.maxWidth = maxWidth
    this.height = height
    this.iconUri = iconUri
    this.focusedIconUri = focusedIconUri
    this.showFocusFootprint = showFocusFootprint
    this.translation = translation
    this.rotation = rotation
    this.scale = scale
    this.scaleRotateCenter = scaleRotateCenter
    this.opacity = opacity
    this.visible = visible
    this.inheritParentOpacity = inheritParentOpacity
    this.inheritParentTransform = inheritParentTransform
    this.clippingRect = clippingRect
    this.renderGroup = renderGroup
    this.focusable = focusable
    this.renderPass = renderPass
    this.equals = ButtonEntry_equals
    this.hashCode = ButtonEntry_hashCode
    this.toString = ButtonEntry_toString
    this.copy = ButtonEntry_copy
    this.component1 = ButtonEntry_component1
    this.component2 = ButtonEntry_component2
    this.component3 = ButtonEntry_component3
    this.component4 = ButtonEntry_component4
    this.component5 = ButtonEntry_component5
    this.component6 = ButtonEntry_component6
    this.component7 = ButtonEntry_component7
    this.component8 = ButtonEntry_component8
    this.component9 = ButtonEntry_component9
    this.component10 = ButtonEntry_component10
    this.component11 = ButtonEntry_component11
    this.component12 = ButtonEntry_component12
    this.component13 = ButtonEntry_component13
    this.component14 = ButtonEntry_component14
    this.component15 = ButtonEntry_component15
    this.component16 = ButtonEntry_component16
    this.component17 = ButtonEntry_component17
    this.component18 = ButtonEntry_component18
    this.component19 = ButtonEntry_component19
    this.component20 = ButtonEntry_component20
    this.component21 = ButtonEntry_component21
    this.component22 = ButtonEntry_component22
    this.component23 = ButtonEntry_component23
    this.component24 = ButtonEntry_component24
    this.component25 = ButtonEntry_component25
    this.component26 = ButtonEntry_component26
    return this
end function

function ButtonEntry_equals(other as Object) as Boolean
    if Type(other) <> "roAssociativeArray" then
        return false
    end if
    if other.__type <> "ButtonEntry" then
        return false
    end if
    if m.id <> other.id then
        return false
    end if
    if m.text <> other.text then
        return false
    end if
    if m.textFont <> other.textFont then
        return false
    end if
    if m.textColor <> other.textColor then
        return false
    end if
    if m.focusedTextColor <> other.focusedTextColor then
        return false
    end if
    if m.backgroundUri <> other.backgroundUri then
        return false
    end if
    if m.focusBitmapUri <> other.focusBitmapUri then
        return false
    end if
    if m.focusFootprint <> other.focusFootprint then
        return false
    end if
    if m.minWidth <> other.minWidth then
        return false
    end if
    if m.maxWidth <> other.maxWidth then
        return false
    end if
    if m.height <> other.height then
        return false
    end if
    if m.iconUri <> other.iconUri then
        return false
    end if
    if m.focusedIconUri <> other.focusedIconUri then
        return false
    end if
    if m.showFocusFootprint <> other.showFocusFootprint then
        return false
    end if
    if m.translation <> other.translation then
        return false
    end if
    if m.rotation <> other.rotation then
        return false
    end if
    if m.scale <> other.scale then
        return false
    end if
    if m.scaleRotateCenter <> other.scaleRotateCenter then
        return false
    end if
    if m.opacity <> other.opacity then
        return false
    end if
    if m.visible <> other.visible then
        return false
    end if
    if m.inheritParentOpacity <> other.inheritParentOpacity then
        return false
    end if
    if m.inheritParentTransform <> other.inheritParentTransform then
        return false
    end if
    if m.clippingRect <> other.clippingRect then
        return false
    end if
    if m.renderGroup <> other.renderGroup then
        return false
    end if
    if m.focusable <> other.focusable then
        return false
    end if
    if m.renderPass <> other.renderPass then
        return false
    end if
    return true
end function

function ButtonEntry_hashCode() as Integer
    result = 1
    result = ((result * 31) + m.id)
    result = ((result * 31) + m.text)
    result = ((result * 31) + m.textFont)
    result = ((result * 31) + m.textColor)
    result = ((result * 31) + m.focusedTextColor)
    result = ((result * 31) + m.backgroundUri)
    result = ((result * 31) + m.focusBitmapUri)
    result = ((result * 31) + m.focusFootprint)
    result = ((result * 31) + m.minWidth)
    result = ((result * 31) + m.maxWidth)
    result = ((result * 31) + m.height)
    result = ((result * 31) + m.iconUri)
    result = ((result * 31) + m.focusedIconUri)
    result = ((result * 31) + m.showFocusFootprint)
    result = ((result * 31) + m.translation)
    result = ((result * 31) + m.rotation)
    result = ((result * 31) + m.scale)
    result = ((result * 31) + m.scaleRotateCenter)
    result = ((result * 31) + m.opacity)
    result = ((result * 31) + m.visible)
    result = ((result * 31) + m.inheritParentOpacity)
    result = ((result * 31) + m.inheritParentTransform)
    result = ((result * 31) + m.clippingRect)
    result = ((result * 31) + m.renderGroup)
    result = ((result * 31) + m.focusable)
    result = ((result * 31) + m.renderPass)
    return result
end function

function ButtonEntry_toString() as String
    return ((((((((((((((((((((((((((((((((((((((((((((((((((("ButtonEntry(id=" + m.id) + ", text=") + m.text) + ", textFont=") + m.textFont) + ", textColor=") + m.textColor) + ", focusedTextColor=") + m.focusedTextColor) + ", backgroundUri=") + m.backgroundUri) + ", focusBitmapUri=") + m.focusBitmapUri) + ", focusFootprint=") + m.focusFootprint) + ", minWidth=") + m.minWidth) + ", maxWidth=") + m.maxWidth) + ", height=") + m.height) + ", iconUri=") + m.iconUri) + ", focusedIconUri=") + m.focusedIconUri) + ", showFocusFootprint=") + m.showFocusFootprint) + ", translation=") + m.translation) + ", rotation=") + m.rotation) + ", scale=") + m.scale) + ", scaleRotateCenter=") + m.scaleRotateCenter) + ", opacity=") + m.opacity) + ", visible=") + m.visible) + ", inheritParentOpacity=") + m.inheritParentOpacity) + ", inheritParentTransform=") + m.inheritParentTransform) + ", clippingRect=") + m.clippingRect) + ", renderGroup=") + m.renderGroup) + ", focusable=") + m.focusable) + ", renderPass=") + m.renderPass) + ")"
end function

function ButtonEntry_copy(id = invalid, text = invalid, textFont = invalid, textColor = invalid, focusedTextColor = invalid, backgroundUri = invalid, focusBitmapUri = invalid, focusFootprint = invalid, minWidth = invalid, maxWidth = invalid, height = invalid, iconUri = invalid, focusedIconUri = invalid, showFocusFootprint = invalid, translation = invalid, rotation = invalid, scale = invalid, scaleRotateCenter = invalid, opacity = invalid, visible = invalid, inheritParentOpacity = invalid, inheritParentTransform = invalid, clippingRect = invalid, renderGroup = invalid, focusable = invalid, renderPass = invalid) as Object
    if id = invalid then
        id = m.id
    end if
    if text = invalid then
        text = m.text
    end if
    if textFont = invalid then
        textFont = m.textFont
    end if
    if textColor = invalid then
        textColor = m.textColor
    end if
    if focusedTextColor = invalid then
        focusedTextColor = m.focusedTextColor
    end if
    if backgroundUri = invalid then
        backgroundUri = m.backgroundUri
    end if
    if focusBitmapUri = invalid then
        focusBitmapUri = m.focusBitmapUri
    end if
    if focusFootprint = invalid then
        focusFootprint = m.focusFootprint
    end if
    if minWidth = invalid then
        minWidth = m.minWidth
    end if
    if maxWidth = invalid then
        maxWidth = m.maxWidth
    end if
    if height = invalid then
        height = m.height
    end if
    if iconUri = invalid then
        iconUri = m.iconUri
    end if
    if focusedIconUri = invalid then
        focusedIconUri = m.focusedIconUri
    end if
    if showFocusFootprint = invalid then
        showFocusFootprint = m.showFocusFootprint
    end if
    if translation = invalid then
        translation = m.translation
    end if
    if rotation = invalid then
        rotation = m.rotation
    end if
    if scale = invalid then
        scale = m.scale
    end if
    if scaleRotateCenter = invalid then
        scaleRotateCenter = m.scaleRotateCenter
    end if
    if opacity = invalid then
        opacity = m.opacity
    end if
    if visible = invalid then
        visible = m.visible
    end if
    if inheritParentOpacity = invalid then
        inheritParentOpacity = m.inheritParentOpacity
    end if
    if inheritParentTransform = invalid then
        inheritParentTransform = m.inheritParentTransform
    end if
    if clippingRect = invalid then
        clippingRect = m.clippingRect
    end if
    if renderGroup = invalid then
        renderGroup = m.renderGroup
    end if
    if focusable = invalid then
        focusable = m.focusable
    end if
    if renderPass = invalid then
        renderPass = m.renderPass
    end if
    return ButtonEntry_create_152svm_k_(id, text, textFont, textColor, focusedTextColor, backgroundUri, focusBitmapUri, focusFootprint, minWidth, maxWidth, height, iconUri, focusedIconUri, showFocusFootprint, translation, rotation, scale, scaleRotateCenter, opacity, visible, inheritParentOpacity, inheritParentTransform, clippingRect, renderGroup, focusable, renderPass)
end function

function ButtonEntry_component1() as String
    return m.id
end function

function ButtonEntry_component2() as Dynamic
    return m.text
end function

function ButtonEntry_component3() as Dynamic
    return m.textFont
end function

function ButtonEntry_component4() as Dynamic
    return m.textColor
end function

function ButtonEntry_component5() as Dynamic
    return m.focusedTextColor
end function

function ButtonEntry_component6() as Dynamic
    return m.backgroundUri
end function

function ButtonEntry_component7() as Dynamic
    return m.focusBitmapUri
end function

function ButtonEntry_component8() as Dynamic
    return m.focusFootprint
end function

function ButtonEntry_component9() as Dynamic
    return m.minWidth
end function

function ButtonEntry_component10() as Dynamic
    return m.maxWidth
end function

function ButtonEntry_component11() as Dynamic
    return m.height
end function

function ButtonEntry_component12() as Dynamic
    return m.iconUri
end function

function ButtonEntry_component13() as Dynamic
    return m.focusedIconUri
end function

function ButtonEntry_component14() as Dynamic
    return m.showFocusFootprint
end function

function ButtonEntry_component15() as Dynamic
    return m.translation
end function

function ButtonEntry_component16() as Dynamic
    return m.rotation
end function

function ButtonEntry_component17() as Dynamic
    return m.scale
end function

function ButtonEntry_component18() as Dynamic
    return m.scaleRotateCenter
end function

function ButtonEntry_component19() as Dynamic
    return m.opacity
end function

function ButtonEntry_component20() as Dynamic
    return m.visible
end function

function ButtonEntry_component21() as Dynamic
    return m.inheritParentOpacity
end function

function ButtonEntry_component22() as Dynamic
    return m.inheritParentTransform
end function

function ButtonEntry_component23() as Dynamic
    return m.clippingRect
end function

function ButtonEntry_component24() as Dynamic
    return m.renderGroup
end function

function ButtonEntry_component25() as Dynamic
    return m.focusable
end function

function ButtonEntry_component26() as Dynamic
    return m.renderPass
end function

function ButtonGroupEntry_create_aszxc9_k_(id as String, layoutDirection = invalid, itemSpacings = invalid, focusedButton = invalid, wrapDividerWidth = invalid, translation = invalid, rotation = invalid, scale = invalid, scaleRotateCenter = invalid, opacity = invalid, visible = invalid, inheritParentOpacity = invalid, inheritParentTransform = invalid, clippingRect = invalid, renderGroup = invalid, focusable = invalid, renderPass = invalid, children = emptyList_k_()) as Object
    this = {}
    this.__type = "ButtonGroupEntry"
    this.__proto = ["ButtonGroupEntry", "ContainerNodeEntry", "NodeEntry", "CommonNodeDefaults", "CommonNodeProperties"]
    this.__id = __kotlin_nextObjectId()
    this.id = id
    this.layoutDirection = layoutDirection
    this.itemSpacings = itemSpacings
    this.focusedButton = focusedButton
    this.wrapDividerWidth = wrapDividerWidth
    this.translation = translation
    this.rotation = rotation
    this.scale = scale
    this.scaleRotateCenter = scaleRotateCenter
    this.opacity = opacity
    this.visible = visible
    this.inheritParentOpacity = inheritParentOpacity
    this.inheritParentTransform = inheritParentTransform
    this.clippingRect = clippingRect
    this.renderGroup = renderGroup
    this.focusable = focusable
    this.renderPass = renderPass
    this.children = children
    this.equals = ButtonGroupEntry_equals
    this.hashCode = ButtonGroupEntry_hashCode
    this.toString = ButtonGroupEntry_toString
    this.copy = ButtonGroupEntry_copy
    this.component1 = ButtonGroupEntry_component1
    this.component2 = ButtonGroupEntry_component2
    this.component3 = ButtonGroupEntry_component3
    this.component4 = ButtonGroupEntry_component4
    this.component5 = ButtonGroupEntry_component5
    this.component6 = ButtonGroupEntry_component6
    this.component7 = ButtonGroupEntry_component7
    this.component8 = ButtonGroupEntry_component8
    this.component9 = ButtonGroupEntry_component9
    this.component10 = ButtonGroupEntry_component10
    this.component11 = ButtonGroupEntry_component11
    this.component12 = ButtonGroupEntry_component12
    this.component13 = ButtonGroupEntry_component13
    this.component14 = ButtonGroupEntry_component14
    this.component15 = ButtonGroupEntry_component15
    this.component16 = ButtonGroupEntry_component16
    this.component17 = ButtonGroupEntry_component17
    this.component18 = ButtonGroupEntry_component18
    return this
end function

function ButtonGroupEntry_equals(other as Object) as Boolean
    if Type(other) <> "roAssociativeArray" then
        return false
    end if
    if other.__type <> "ButtonGroupEntry" then
        return false
    end if
    if m.id <> other.id then
        return false
    end if
    if m.layoutDirection <> other.layoutDirection then
        return false
    end if
    if m.itemSpacings <> other.itemSpacings then
        return false
    end if
    if m.focusedButton <> other.focusedButton then
        return false
    end if
    if m.wrapDividerWidth <> other.wrapDividerWidth then
        return false
    end if
    if m.translation <> other.translation then
        return false
    end if
    if m.rotation <> other.rotation then
        return false
    end if
    if m.scale <> other.scale then
        return false
    end if
    if m.scaleRotateCenter <> other.scaleRotateCenter then
        return false
    end if
    if m.opacity <> other.opacity then
        return false
    end if
    if m.visible <> other.visible then
        return false
    end if
    if m.inheritParentOpacity <> other.inheritParentOpacity then
        return false
    end if
    if m.inheritParentTransform <> other.inheritParentTransform then
        return false
    end if
    if m.clippingRect <> other.clippingRect then
        return false
    end if
    if m.renderGroup <> other.renderGroup then
        return false
    end if
    if m.focusable <> other.focusable then
        return false
    end if
    if m.renderPass <> other.renderPass then
        return false
    end if
    if m.children <> other.children then
        return false
    end if
    return true
end function

function ButtonGroupEntry_hashCode() as Integer
    result = 1
    result = ((result * 31) + m.id)
    result = ((result * 31) + m.layoutDirection)
    result = ((result * 31) + m.itemSpacings)
    result = ((result * 31) + m.focusedButton)
    result = ((result * 31) + m.wrapDividerWidth)
    result = ((result * 31) + m.translation)
    result = ((result * 31) + m.rotation)
    result = ((result * 31) + m.scale)
    result = ((result * 31) + m.scaleRotateCenter)
    result = ((result * 31) + m.opacity)
    result = ((result * 31) + m.visible)
    result = ((result * 31) + m.inheritParentOpacity)
    result = ((result * 31) + m.inheritParentTransform)
    result = ((result * 31) + m.clippingRect)
    result = ((result * 31) + m.renderGroup)
    result = ((result * 31) + m.focusable)
    result = ((result * 31) + m.renderPass)
    result = ((result * 31) + m.children)
    return result
end function

function ButtonGroupEntry_toString() as String
    return ((((((((((((((((((((((((((((((((((("ButtonGroupEntry(id=" + m.id) + ", layoutDirection=") + m.layoutDirection) + ", itemSpacings=") + m.itemSpacings) + ", focusedButton=") + m.focusedButton) + ", wrapDividerWidth=") + m.wrapDividerWidth) + ", translation=") + m.translation) + ", rotation=") + m.rotation) + ", scale=") + m.scale) + ", scaleRotateCenter=") + m.scaleRotateCenter) + ", opacity=") + m.opacity) + ", visible=") + m.visible) + ", inheritParentOpacity=") + m.inheritParentOpacity) + ", inheritParentTransform=") + m.inheritParentTransform) + ", clippingRect=") + m.clippingRect) + ", renderGroup=") + m.renderGroup) + ", focusable=") + m.focusable) + ", renderPass=") + m.renderPass) + ", children=") + m.children) + ")"
end function

function ButtonGroupEntry_copy(id = invalid, layoutDirection = invalid, itemSpacings = invalid, focusedButton = invalid, wrapDividerWidth = invalid, translation = invalid, rotation = invalid, scale = invalid, scaleRotateCenter = invalid, opacity = invalid, visible = invalid, inheritParentOpacity = invalid, inheritParentTransform = invalid, clippingRect = invalid, renderGroup = invalid, focusable = invalid, renderPass = invalid, children = invalid) as Object
    if id = invalid then
        id = m.id
    end if
    if layoutDirection = invalid then
        layoutDirection = m.layoutDirection
    end if
    if itemSpacings = invalid then
        itemSpacings = m.itemSpacings
    end if
    if focusedButton = invalid then
        focusedButton = m.focusedButton
    end if
    if wrapDividerWidth = invalid then
        wrapDividerWidth = m.wrapDividerWidth
    end if
    if translation = invalid then
        translation = m.translation
    end if
    if rotation = invalid then
        rotation = m.rotation
    end if
    if scale = invalid then
        scale = m.scale
    end if
    if scaleRotateCenter = invalid then
        scaleRotateCenter = m.scaleRotateCenter
    end if
    if opacity = invalid then
        opacity = m.opacity
    end if
    if visible = invalid then
        visible = m.visible
    end if
    if inheritParentOpacity = invalid then
        inheritParentOpacity = m.inheritParentOpacity
    end if
    if inheritParentTransform = invalid then
        inheritParentTransform = m.inheritParentTransform
    end if
    if clippingRect = invalid then
        clippingRect = m.clippingRect
    end if
    if renderGroup = invalid then
        renderGroup = m.renderGroup
    end if
    if focusable = invalid then
        focusable = m.focusable
    end if
    if renderPass = invalid then
        renderPass = m.renderPass
    end if
    if children = invalid then
        children = m.children
    end if
    return ButtonGroupEntry_create_aszxc9_k_(id, layoutDirection, itemSpacings, focusedButton, wrapDividerWidth, translation, rotation, scale, scaleRotateCenter, opacity, visible, inheritParentOpacity, inheritParentTransform, clippingRect, renderGroup, focusable, renderPass, children)
end function

function ButtonGroupEntry_component1() as String
    return m.id
end function

function ButtonGroupEntry_component2() as Dynamic
    return m.layoutDirection
end function

function ButtonGroupEntry_component3() as Dynamic
    return m.itemSpacings
end function

function ButtonGroupEntry_component4() as Dynamic
    return m.focusedButton
end function

function ButtonGroupEntry_component5() as Dynamic
    return m.wrapDividerWidth
end function

function ButtonGroupEntry_component6() as Dynamic
    return m.translation
end function

function ButtonGroupEntry_component7() as Dynamic
    return m.rotation
end function

function ButtonGroupEntry_component8() as Dynamic
    return m.scale
end function

function ButtonGroupEntry_component9() as Dynamic
    return m.scaleRotateCenter
end function

function ButtonGroupEntry_component10() as Dynamic
    return m.opacity
end function

function ButtonGroupEntry_component11() as Dynamic
    return m.visible
end function

function ButtonGroupEntry_component12() as Dynamic
    return m.inheritParentOpacity
end function

function ButtonGroupEntry_component13() as Dynamic
    return m.inheritParentTransform
end function

function ButtonGroupEntry_component14() as Dynamic
    return m.clippingRect
end function

function ButtonGroupEntry_component15() as Dynamic
    return m.renderGroup
end function

function ButtonGroupEntry_component16() as Dynamic
    return m.focusable
end function

function ButtonGroupEntry_component17() as Dynamic
    return m.renderPass
end function

function ButtonGroupEntry_component18() as Object
    return m.children
end function

function TextEditBoxEntry_create_qn9xve_k_(id as String, text = invalid, hint = invalid, textColor = invalid, hintTextColor = invalid, textFont = invalid, maxTextLength = invalid, width = invalid, secureMode = invalid, cursorPosition = invalid, translation = invalid, rotation = invalid, scale = invalid, scaleRotateCenter = invalid, opacity = invalid, visible = invalid, inheritParentOpacity = invalid, inheritParentTransform = invalid, clippingRect = invalid, renderGroup = invalid, focusable = invalid, renderPass = invalid) as Object
    this = {}
    this.__type = "TextEditBoxEntry"
    this.__proto = ["TextEditBoxEntry", "NodeEntry", "CommonNodeDefaults", "CommonNodeProperties"]
    this.__id = __kotlin_nextObjectId()
    this.id = id
    this.text = text
    this.hint = hint
    this.textColor = textColor
    this.hintTextColor = hintTextColor
    this.textFont = textFont
    this.maxTextLength = maxTextLength
    this.width = width
    this.secureMode = secureMode
    this.cursorPosition = cursorPosition
    this.translation = translation
    this.rotation = rotation
    this.scale = scale
    this.scaleRotateCenter = scaleRotateCenter
    this.opacity = opacity
    this.visible = visible
    this.inheritParentOpacity = inheritParentOpacity
    this.inheritParentTransform = inheritParentTransform
    this.clippingRect = clippingRect
    this.renderGroup = renderGroup
    this.focusable = focusable
    this.renderPass = renderPass
    this.equals = TextEditBoxEntry_equals
    this.hashCode = TextEditBoxEntry_hashCode
    this.toString = TextEditBoxEntry_toString
    this.copy = TextEditBoxEntry_copy
    this.component1 = TextEditBoxEntry_component1
    this.component2 = TextEditBoxEntry_component2
    this.component3 = TextEditBoxEntry_component3
    this.component4 = TextEditBoxEntry_component4
    this.component5 = TextEditBoxEntry_component5
    this.component6 = TextEditBoxEntry_component6
    this.component7 = TextEditBoxEntry_component7
    this.component8 = TextEditBoxEntry_component8
    this.component9 = TextEditBoxEntry_component9
    this.component10 = TextEditBoxEntry_component10
    this.component11 = TextEditBoxEntry_component11
    this.component12 = TextEditBoxEntry_component12
    this.component13 = TextEditBoxEntry_component13
    this.component14 = TextEditBoxEntry_component14
    this.component15 = TextEditBoxEntry_component15
    this.component16 = TextEditBoxEntry_component16
    this.component17 = TextEditBoxEntry_component17
    this.component18 = TextEditBoxEntry_component18
    this.component19 = TextEditBoxEntry_component19
    this.component20 = TextEditBoxEntry_component20
    this.component21 = TextEditBoxEntry_component21
    this.component22 = TextEditBoxEntry_component22
    return this
end function

function TextEditBoxEntry_equals(other as Object) as Boolean
    if Type(other) <> "roAssociativeArray" then
        return false
    end if
    if other.__type <> "TextEditBoxEntry" then
        return false
    end if
    if m.id <> other.id then
        return false
    end if
    if m.text <> other.text then
        return false
    end if
    if m.hint <> other.hint then
        return false
    end if
    if m.textColor <> other.textColor then
        return false
    end if
    if m.hintTextColor <> other.hintTextColor then
        return false
    end if
    if m.textFont <> other.textFont then
        return false
    end if
    if m.maxTextLength <> other.maxTextLength then
        return false
    end if
    if m.width <> other.width then
        return false
    end if
    if m.secureMode <> other.secureMode then
        return false
    end if
    if m.cursorPosition <> other.cursorPosition then
        return false
    end if
    if m.translation <> other.translation then
        return false
    end if
    if m.rotation <> other.rotation then
        return false
    end if
    if m.scale <> other.scale then
        return false
    end if
    if m.scaleRotateCenter <> other.scaleRotateCenter then
        return false
    end if
    if m.opacity <> other.opacity then
        return false
    end if
    if m.visible <> other.visible then
        return false
    end if
    if m.inheritParentOpacity <> other.inheritParentOpacity then
        return false
    end if
    if m.inheritParentTransform <> other.inheritParentTransform then
        return false
    end if
    if m.clippingRect <> other.clippingRect then
        return false
    end if
    if m.renderGroup <> other.renderGroup then
        return false
    end if
    if m.focusable <> other.focusable then
        return false
    end if
    if m.renderPass <> other.renderPass then
        return false
    end if
    return true
end function

function TextEditBoxEntry_hashCode() as Integer
    result = 1
    result = ((result * 31) + m.id)
    result = ((result * 31) + m.text)
    result = ((result * 31) + m.hint)
    result = ((result * 31) + m.textColor)
    result = ((result * 31) + m.hintTextColor)
    result = ((result * 31) + m.textFont)
    result = ((result * 31) + m.maxTextLength)
    result = ((result * 31) + m.width)
    result = ((result * 31) + m.secureMode)
    result = ((result * 31) + m.cursorPosition)
    result = ((result * 31) + m.translation)
    result = ((result * 31) + m.rotation)
    result = ((result * 31) + m.scale)
    result = ((result * 31) + m.scaleRotateCenter)
    result = ((result * 31) + m.opacity)
    result = ((result * 31) + m.visible)
    result = ((result * 31) + m.inheritParentOpacity)
    result = ((result * 31) + m.inheritParentTransform)
    result = ((result * 31) + m.clippingRect)
    result = ((result * 31) + m.renderGroup)
    result = ((result * 31) + m.focusable)
    result = ((result * 31) + m.renderPass)
    return result
end function

function TextEditBoxEntry_toString() as String
    return ((((((((((((((((((((((((((((((((((((((((((("TextEditBoxEntry(id=" + m.id) + ", text=") + m.text) + ", hint=") + m.hint) + ", textColor=") + m.textColor) + ", hintTextColor=") + m.hintTextColor) + ", textFont=") + m.textFont) + ", maxTextLength=") + m.maxTextLength) + ", width=") + m.width) + ", secureMode=") + m.secureMode) + ", cursorPosition=") + m.cursorPosition) + ", translation=") + m.translation) + ", rotation=") + m.rotation) + ", scale=") + m.scale) + ", scaleRotateCenter=") + m.scaleRotateCenter) + ", opacity=") + m.opacity) + ", visible=") + m.visible) + ", inheritParentOpacity=") + m.inheritParentOpacity) + ", inheritParentTransform=") + m.inheritParentTransform) + ", clippingRect=") + m.clippingRect) + ", renderGroup=") + m.renderGroup) + ", focusable=") + m.focusable) + ", renderPass=") + m.renderPass) + ")"
end function

function TextEditBoxEntry_copy(id = invalid, text = invalid, hint = invalid, textColor = invalid, hintTextColor = invalid, textFont = invalid, maxTextLength = invalid, width = invalid, secureMode = invalid, cursorPosition = invalid, translation = invalid, rotation = invalid, scale = invalid, scaleRotateCenter = invalid, opacity = invalid, visible = invalid, inheritParentOpacity = invalid, inheritParentTransform = invalid, clippingRect = invalid, renderGroup = invalid, focusable = invalid, renderPass = invalid) as Object
    if id = invalid then
        id = m.id
    end if
    if text = invalid then
        text = m.text
    end if
    if hint = invalid then
        hint = m.hint
    end if
    if textColor = invalid then
        textColor = m.textColor
    end if
    if hintTextColor = invalid then
        hintTextColor = m.hintTextColor
    end if
    if textFont = invalid then
        textFont = m.textFont
    end if
    if maxTextLength = invalid then
        maxTextLength = m.maxTextLength
    end if
    if width = invalid then
        width = m.width
    end if
    if secureMode = invalid then
        secureMode = m.secureMode
    end if
    if cursorPosition = invalid then
        cursorPosition = m.cursorPosition
    end if
    if translation = invalid then
        translation = m.translation
    end if
    if rotation = invalid then
        rotation = m.rotation
    end if
    if scale = invalid then
        scale = m.scale
    end if
    if scaleRotateCenter = invalid then
        scaleRotateCenter = m.scaleRotateCenter
    end if
    if opacity = invalid then
        opacity = m.opacity
    end if
    if visible = invalid then
        visible = m.visible
    end if
    if inheritParentOpacity = invalid then
        inheritParentOpacity = m.inheritParentOpacity
    end if
    if inheritParentTransform = invalid then
        inheritParentTransform = m.inheritParentTransform
    end if
    if clippingRect = invalid then
        clippingRect = m.clippingRect
    end if
    if renderGroup = invalid then
        renderGroup = m.renderGroup
    end if
    if focusable = invalid then
        focusable = m.focusable
    end if
    if renderPass = invalid then
        renderPass = m.renderPass
    end if
    return TextEditBoxEntry_create_qn9xve_k_(id, text, hint, textColor, hintTextColor, textFont, maxTextLength, width, secureMode, cursorPosition, translation, rotation, scale, scaleRotateCenter, opacity, visible, inheritParentOpacity, inheritParentTransform, clippingRect, renderGroup, focusable, renderPass)
end function

function TextEditBoxEntry_component1() as String
    return m.id
end function

function TextEditBoxEntry_component2() as Dynamic
    return m.text
end function

function TextEditBoxEntry_component3() as Dynamic
    return m.hint
end function

function TextEditBoxEntry_component4() as Dynamic
    return m.textColor
end function

function TextEditBoxEntry_component5() as Dynamic
    return m.hintTextColor
end function

function TextEditBoxEntry_component6() as Dynamic
    return m.textFont
end function

function TextEditBoxEntry_component7() as Dynamic
    return m.maxTextLength
end function

function TextEditBoxEntry_component8() as Dynamic
    return m.width
end function

function TextEditBoxEntry_component9() as Dynamic
    return m.secureMode
end function

function TextEditBoxEntry_component10() as Dynamic
    return m.cursorPosition
end function

function TextEditBoxEntry_component11() as Dynamic
    return m.translation
end function

function TextEditBoxEntry_component12() as Dynamic
    return m.rotation
end function

function TextEditBoxEntry_component13() as Dynamic
    return m.scale
end function

function TextEditBoxEntry_component14() as Dynamic
    return m.scaleRotateCenter
end function

function TextEditBoxEntry_component15() as Dynamic
    return m.opacity
end function

function TextEditBoxEntry_component16() as Dynamic
    return m.visible
end function

function TextEditBoxEntry_component17() as Dynamic
    return m.inheritParentOpacity
end function

function TextEditBoxEntry_component18() as Dynamic
    return m.inheritParentTransform
end function

function TextEditBoxEntry_component19() as Dynamic
    return m.clippingRect
end function

function TextEditBoxEntry_component20() as Dynamic
    return m.renderGroup
end function

function TextEditBoxEntry_component21() as Dynamic
    return m.focusable
end function

function TextEditBoxEntry_component22() as Dynamic
    return m.renderPass
end function

function KeyboardType_create_k_(__name as String, __ordinal as Integer) as Object
    this = {}
    this.__type = "KeyboardType"
    this.name = __name
    this.ordinal = __ordinal
    this.__proto = ["KeyboardType", "Comparable"]
    this.__id = __kotlin_nextObjectId()
    return this
end function

sub KeyboardType_initEntries()
    if m.KeyboardType_entriesInitialized = true then
        return
    end if
    m.KeyboardType_entriesInitialized = true
    m.KeyboardType_alphanumeric = KeyboardType_create_k_("alphanumeric", 0)
    m.KeyboardType_email = KeyboardType_create_k_("email", 1)
    m.KeyboardType_url = KeyboardType_create_k_("url", 2)
    m.KeyboardType_password = KeyboardType_create_k_("password", 3)
    m.KeyboardType_numpad = KeyboardType_create_k_("numpad", 4)
end sub

function KeyboardType_values() as Object
    KeyboardType_initEntries()
    return [m.KeyboardType_alphanumeric, m.KeyboardType_email, m.KeyboardType_url, m.KeyboardType_password, m.KeyboardType_numpad]
end function

function KeyboardType_valueOf(name as String) as Object
    KeyboardType_initEntries()
    if name = "alphanumeric" then
        return m.KeyboardType_alphanumeric
    else if name = "email" then
        return m.KeyboardType_email
    else if name = "url" then
        return m.KeyboardType_url
    else if name = "password" then
        return m.KeyboardType_password
    else if name = "numpad" then
        return m.KeyboardType_numpad
    else
        return invalid
    end if
end function

function KeyboardEntry_create_j75j57_k_(id as String, text = invalid, keyboardType = invalid, lowercase = invalid, translation = invalid, rotation = invalid, scale = invalid, scaleRotateCenter = invalid, opacity = invalid, visible = invalid, inheritParentOpacity = invalid, inheritParentTransform = invalid, clippingRect = invalid, renderGroup = invalid, focusable = invalid, renderPass = invalid) as Object
    this = {}
    this.__type = "KeyboardEntry"
    this.__proto = ["KeyboardEntry", "NodeEntry", "CommonNodeDefaults", "CommonNodeProperties"]
    this.__id = __kotlin_nextObjectId()
    this.id = id
    this.text = text
    this.keyboardType = keyboardType
    this.lowercase = lowercase
    this.translation = translation
    this.rotation = rotation
    this.scale = scale
    this.scaleRotateCenter = scaleRotateCenter
    this.opacity = opacity
    this.visible = visible
    this.inheritParentOpacity = inheritParentOpacity
    this.inheritParentTransform = inheritParentTransform
    this.clippingRect = clippingRect
    this.renderGroup = renderGroup
    this.focusable = focusable
    this.renderPass = renderPass
    this.equals = KeyboardEntry_equals
    this.hashCode = KeyboardEntry_hashCode
    this.toString = KeyboardEntry_toString
    this.copy = KeyboardEntry_copy
    this.component1 = KeyboardEntry_component1
    this.component2 = KeyboardEntry_component2
    this.component3 = KeyboardEntry_component3
    this.component4 = KeyboardEntry_component4
    this.component5 = KeyboardEntry_component5
    this.component6 = KeyboardEntry_component6
    this.component7 = KeyboardEntry_component7
    this.component8 = KeyboardEntry_component8
    this.component9 = KeyboardEntry_component9
    this.component10 = KeyboardEntry_component10
    this.component11 = KeyboardEntry_component11
    this.component12 = KeyboardEntry_component12
    this.component13 = KeyboardEntry_component13
    this.component14 = KeyboardEntry_component14
    this.component15 = KeyboardEntry_component15
    this.component16 = KeyboardEntry_component16
    return this
end function

function KeyboardEntry_equals(other as Object) as Boolean
    if Type(other) <> "roAssociativeArray" then
        return false
    end if
    if other.__type <> "KeyboardEntry" then
        return false
    end if
    if m.id <> other.id then
        return false
    end if
    if m.text <> other.text then
        return false
    end if
    if m.keyboardType <> other.keyboardType then
        return false
    end if
    if m.lowercase <> other.lowercase then
        return false
    end if
    if m.translation <> other.translation then
        return false
    end if
    if m.rotation <> other.rotation then
        return false
    end if
    if m.scale <> other.scale then
        return false
    end if
    if m.scaleRotateCenter <> other.scaleRotateCenter then
        return false
    end if
    if m.opacity <> other.opacity then
        return false
    end if
    if m.visible <> other.visible then
        return false
    end if
    if m.inheritParentOpacity <> other.inheritParentOpacity then
        return false
    end if
    if m.inheritParentTransform <> other.inheritParentTransform then
        return false
    end if
    if m.clippingRect <> other.clippingRect then
        return false
    end if
    if m.renderGroup <> other.renderGroup then
        return false
    end if
    if m.focusable <> other.focusable then
        return false
    end if
    if m.renderPass <> other.renderPass then
        return false
    end if
    return true
end function

function KeyboardEntry_hashCode() as Integer
    result = 1
    result = ((result * 31) + m.id)
    result = ((result * 31) + m.text)
    result = ((result * 31) + m.keyboardType)
    result = ((result * 31) + m.lowercase)
    result = ((result * 31) + m.translation)
    result = ((result * 31) + m.rotation)
    result = ((result * 31) + m.scale)
    result = ((result * 31) + m.scaleRotateCenter)
    result = ((result * 31) + m.opacity)
    result = ((result * 31) + m.visible)
    result = ((result * 31) + m.inheritParentOpacity)
    result = ((result * 31) + m.inheritParentTransform)
    result = ((result * 31) + m.clippingRect)
    result = ((result * 31) + m.renderGroup)
    result = ((result * 31) + m.focusable)
    result = ((result * 31) + m.renderPass)
    return result
end function

function KeyboardEntry_toString() as String
    return ((((((((((((((((((((((((((((((("KeyboardEntry(id=" + m.id) + ", text=") + m.text) + ", keyboardType=") + m.keyboardType) + ", lowercase=") + m.lowercase) + ", translation=") + m.translation) + ", rotation=") + m.rotation) + ", scale=") + m.scale) + ", scaleRotateCenter=") + m.scaleRotateCenter) + ", opacity=") + m.opacity) + ", visible=") + m.visible) + ", inheritParentOpacity=") + m.inheritParentOpacity) + ", inheritParentTransform=") + m.inheritParentTransform) + ", clippingRect=") + m.clippingRect) + ", renderGroup=") + m.renderGroup) + ", focusable=") + m.focusable) + ", renderPass=") + m.renderPass) + ")"
end function

function KeyboardEntry_copy(id = invalid, text = invalid, keyboardType = invalid, lowercase = invalid, translation = invalid, rotation = invalid, scale = invalid, scaleRotateCenter = invalid, opacity = invalid, visible = invalid, inheritParentOpacity = invalid, inheritParentTransform = invalid, clippingRect = invalid, renderGroup = invalid, focusable = invalid, renderPass = invalid) as Object
    if id = invalid then
        id = m.id
    end if
    if text = invalid then
        text = m.text
    end if
    if keyboardType = invalid then
        keyboardType = m.keyboardType
    end if
    if lowercase = invalid then
        lowercase = m.lowercase
    end if
    if translation = invalid then
        translation = m.translation
    end if
    if rotation = invalid then
        rotation = m.rotation
    end if
    if scale = invalid then
        scale = m.scale
    end if
    if scaleRotateCenter = invalid then
        scaleRotateCenter = m.scaleRotateCenter
    end if
    if opacity = invalid then
        opacity = m.opacity
    end if
    if visible = invalid then
        visible = m.visible
    end if
    if inheritParentOpacity = invalid then
        inheritParentOpacity = m.inheritParentOpacity
    end if
    if inheritParentTransform = invalid then
        inheritParentTransform = m.inheritParentTransform
    end if
    if clippingRect = invalid then
        clippingRect = m.clippingRect
    end if
    if renderGroup = invalid then
        renderGroup = m.renderGroup
    end if
    if focusable = invalid then
        focusable = m.focusable
    end if
    if renderPass = invalid then
        renderPass = m.renderPass
    end if
    return KeyboardEntry_create_j75j57_k_(id, text, keyboardType, lowercase, translation, rotation, scale, scaleRotateCenter, opacity, visible, inheritParentOpacity, inheritParentTransform, clippingRect, renderGroup, focusable, renderPass)
end function

function KeyboardEntry_component1() as String
    return m.id
end function

function KeyboardEntry_component2() as Dynamic
    return m.text
end function

function KeyboardEntry_component3() as Dynamic
    return m.keyboardType
end function

function KeyboardEntry_component4() as Dynamic
    return m.lowercase
end function

function KeyboardEntry_component5() as Dynamic
    return m.translation
end function

function KeyboardEntry_component6() as Dynamic
    return m.rotation
end function

function KeyboardEntry_component7() as Dynamic
    return m.scale
end function

function KeyboardEntry_component8() as Dynamic
    return m.scaleRotateCenter
end function

function KeyboardEntry_component9() as Dynamic
    return m.opacity
end function

function KeyboardEntry_component10() as Dynamic
    return m.visible
end function

function KeyboardEntry_component11() as Dynamic
    return m.inheritParentOpacity
end function

function KeyboardEntry_component12() as Dynamic
    return m.inheritParentTransform
end function

function KeyboardEntry_component13() as Dynamic
    return m.clippingRect
end function

function KeyboardEntry_component14() as Dynamic
    return m.renderGroup
end function

function KeyboardEntry_component15() as Dynamic
    return m.focusable
end function

function KeyboardEntry_component16() as Dynamic
    return m.renderPass
end function
