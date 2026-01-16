function HorizAlign_create_k_(__name as String, __ordinal as Integer) as Object
    this = {}
    this.__type = "HorizAlign"
    this.name = __name
    this.ordinal = __ordinal
    this.__proto = ["HorizAlign", "Comparable"]
    this.__id = __kotlin_nextObjectId()
    this.values_k_ = HorizAlign_values_k_
    this.valueOf_Str_k_ = HorizAlign_valueOf_Str_k_
    this.get_entries = HorizAlign_get_entries_k_
    return this
end function

sub HorizAlign_initEntries()
    if m.HorizAlign_entriesInitialized then
        return
    end if
    m.HorizAlign_entriesInitialized = true
    m.HorizAlign_left = HorizAlign_create_k_("left", 0)
    m.HorizAlign_center = HorizAlign_create_k_("center", 1)
    m.HorizAlign_right = HorizAlign_create_k_("right", 2)
end sub

function HorizAlign_values() as Object
    HorizAlign_initEntries()
    return [m.HorizAlign_left, m.HorizAlign_center, m.HorizAlign_right]
end function

function HorizAlign_valueOf(name as String) as Object
    HorizAlign_initEntries()
    if name = "left" then
        return m.HorizAlign_left
    else if name = "center" then
        return m.HorizAlign_center
    else if name = "right" then
        return m.HorizAlign_right
    else
        return invalid
    end if
end function

function VertAlign_create_k_(__name as String, __ordinal as Integer) as Object
    this = {}
    this.__type = "VertAlign"
    this.name = __name
    this.ordinal = __ordinal
    this.__proto = ["VertAlign", "Comparable"]
    this.__id = __kotlin_nextObjectId()
    this.values_k_ = VertAlign_values_k_
    this.valueOf_Str_k_ = VertAlign_valueOf_Str_k_
    this.get_entries = VertAlign_get_entries_k_
    return this
end function

sub VertAlign_initEntries()
    if m.VertAlign_entriesInitialized then
        return
    end if
    m.VertAlign_entriesInitialized = true
    m.VertAlign_top = VertAlign_create_k_("top", 0)
    m.VertAlign_center = VertAlign_create_k_("center", 1)
    m.VertAlign_bottom = VertAlign_create_k_("bottom", 2)
end sub

function VertAlign_values() as Object
    VertAlign_initEntries()
    return [m.VertAlign_top, m.VertAlign_center, m.VertAlign_bottom]
end function

function VertAlign_valueOf(name as String) as Object
    VertAlign_initEntries()
    if name = "top" then
        return m.VertAlign_top
    else if name = "center" then
        return m.VertAlign_center
    else if name = "bottom" then
        return m.VertAlign_bottom
    else
        return invalid
    end if
end function

function Truncation_create_k_(__name as String, __ordinal as Integer) as Object
    this = {}
    this.__type = "Truncation"
    this.name = __name
    this.ordinal = __ordinal
    this.__proto = ["Truncation", "Comparable"]
    this.__id = __kotlin_nextObjectId()
    this.values_k_ = Truncation_values_k_
    this.valueOf_Str_k_ = Truncation_valueOf_Str_k_
    this.get_entries = Truncation_get_entries_k_
    return this
end function

sub Truncation_initEntries()
    if m.Truncation_entriesInitialized then
        return
    end if
    m.Truncation_entriesInitialized = true
    m.Truncation_none = Truncation_create_k_("none", 0)
    m.Truncation_truncateEnd = Truncation_create_k_("truncateEnd", 1)
end sub

function Truncation_values() as Object
    Truncation_initEntries()
    return [m.Truncation_none, m.Truncation_truncateEnd]
end function

function Truncation_valueOf(name as String) as Object
    Truncation_initEntries()
    if name = "none" then
        return m.Truncation_none
    else if name = "truncateEnd" then
        return m.Truncation_truncateEnd
    else
        return invalid
    end if
end function

function WordWrap_create_k_(__name as String, __ordinal as Integer) as Object
    this = {}
    this.__type = "WordWrap"
    this.name = __name
    this.ordinal = __ordinal
    this.__proto = ["WordWrap", "Comparable"]
    this.__id = __kotlin_nextObjectId()
    this.values_k_ = WordWrap_values_k_
    this.valueOf_Str_k_ = WordWrap_valueOf_Str_k_
    this.get_entries = WordWrap_get_entries_k_
    return this
end function

sub WordWrap_initEntries()
    if m.WordWrap_entriesInitialized then
        return
    end if
    m.WordWrap_entriesInitialized = true
    m.WordWrap_word = WordWrap_create_k_("word", 0)
    m.WordWrap_character = WordWrap_create_k_("character", 1)
end sub

function WordWrap_values() as Object
    WordWrap_initEntries()
    return [m.WordWrap_word, m.WordWrap_character]
end function

function WordWrap_valueOf(name as String) as Object
    WordWrap_initEntries()
    if name = "word" then
        return m.WordWrap_word
    else if name = "character" then
        return m.WordWrap_character
    else
        return invalid
    end if
end function

function LoadingBitmapStyle_create_k_(__name as String, __ordinal as Integer) as Object
    this = {}
    this.__type = "LoadingBitmapStyle"
    this.name = __name
    this.ordinal = __ordinal
    this.__proto = ["LoadingBitmapStyle", "Comparable"]
    this.__id = __kotlin_nextObjectId()
    this.values_k_ = LoadingBitmapStyle_values_k_
    this.valueOf_Str_k_ = LoadingBitmapStyle_valueOf_Str_k_
    this.get_entries = LoadingBitmapStyle_get_entries_k_
    return this
end function

sub LoadingBitmapStyle_initEntries()
    if m.LoadingBitmapStyle_entriesInitialized then
        return
    end if
    m.LoadingBitmapStyle_entriesInitialized = true
    m.LoadingBitmapStyle_noScale = LoadingBitmapStyle_create_k_("noScale", 0)
    m.LoadingBitmapStyle_scale = LoadingBitmapStyle_create_k_("scale", 1)
    m.LoadingBitmapStyle_center = LoadingBitmapStyle_create_k_("center", 2)
end sub

function LoadingBitmapStyle_values() as Object
    LoadingBitmapStyle_initEntries()
    return [m.LoadingBitmapStyle_noScale, m.LoadingBitmapStyle_scale, m.LoadingBitmapStyle_center]
end function

function LoadingBitmapStyle_valueOf(name as String) as Object
    LoadingBitmapStyle_initEntries()
    if name = "noScale" then
        return m.LoadingBitmapStyle_noScale
    else if name = "scale" then
        return m.LoadingBitmapStyle_scale
    else if name = "center" then
        return m.LoadingBitmapStyle_center
    else
        return invalid
    end if
end function

function BlendingMode_create_k_(__name as String, __ordinal as Integer) as Object
    this = {}
    this.__type = "BlendingMode"
    this.name = __name
    this.ordinal = __ordinal
    this.__proto = ["BlendingMode", "Comparable"]
    this.__id = __kotlin_nextObjectId()
    this.values_k_ = BlendingMode_values_k_
    this.valueOf_Str_k_ = BlendingMode_valueOf_Str_k_
    this.get_entries = BlendingMode_get_entries_k_
    return this
end function

sub BlendingMode_initEntries()
    if m.BlendingMode_entriesInitialized then
        return
    end if
    m.BlendingMode_entriesInitialized = true
    m.BlendingMode_normal = BlendingMode_create_k_("normal", 0)
    m.BlendingMode_add = BlendingMode_create_k_("add", 1)
    m.BlendingMode_multiply = BlendingMode_create_k_("multiply", 2)
end sub

function BlendingMode_values() as Object
    BlendingMode_initEntries()
    return [m.BlendingMode_normal, m.BlendingMode_add, m.BlendingMode_multiply]
end function

function BlendingMode_valueOf(name as String) as Object
    BlendingMode_initEntries()
    if name = "normal" then
        return m.BlendingMode_normal
    else if name = "add" then
        return m.BlendingMode_add
    else if name = "multiply" then
        return m.BlendingMode_multiply
    else
        return invalid
    end if
end function

function LabelEntry_create_hyi4mx_k_(id as String, text = invalid, font = invalid, color = invalid, horizAlign = invalid, vertAlign = invalid, width = invalid, height = invalid, maxWidth = invalid, maxLines = invalid, wrap = invalid, truncateOnDelimiter = invalid, lineSpacing = invalid, displayPartialLines = invalid, translation = invalid, rotation = invalid, scale = invalid, scaleRotateCenter = invalid, opacity = invalid, visible = invalid, inheritParentOpacity = invalid, inheritParentTransform = invalid, clippingRect = invalid, renderGroup = invalid, focusable = invalid, renderPass = invalid) as Object
    this = {}
    this.__type = "LabelEntry"
    this.__proto = ["LabelEntry", "NodeEntry", "CommonNodeDefaults", "CommonNodeProperties"]
    this.__id = __kotlin_nextObjectId()
    this.id = id
    this.text = text
    this.font = font
    this.color = color
    this.horizAlign = horizAlign
    this.vertAlign = vertAlign
    this.width = width
    this.height = height
    this.maxWidth = maxWidth
    this.maxLines = maxLines
    this.wrap = wrap
    this.truncateOnDelimiter = truncateOnDelimiter
    this.lineSpacing = lineSpacing
    this.displayPartialLines = displayPartialLines
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
    this.equals = LabelEntry_equals
    this.hashCode = LabelEntry_hashCode
    this.toString = LabelEntry_toString
    this.copy = LabelEntry_copy
    this.component1 = LabelEntry_component1
    this.component2 = LabelEntry_component2
    this.component3 = LabelEntry_component3
    this.component4 = LabelEntry_component4
    this.component5 = LabelEntry_component5
    this.component6 = LabelEntry_component6
    this.component7 = LabelEntry_component7
    this.component8 = LabelEntry_component8
    this.component9 = LabelEntry_component9
    this.component10 = LabelEntry_component10
    this.component11 = LabelEntry_component11
    this.component12 = LabelEntry_component12
    this.component13 = LabelEntry_component13
    this.component14 = LabelEntry_component14
    this.component15 = LabelEntry_component15
    this.component16 = LabelEntry_component16
    this.component17 = LabelEntry_component17
    this.component18 = LabelEntry_component18
    this.component19 = LabelEntry_component19
    this.component20 = LabelEntry_component20
    this.component21 = LabelEntry_component21
    this.component22 = LabelEntry_component22
    this.component23 = LabelEntry_component23
    this.component24 = LabelEntry_component24
    this.component25 = LabelEntry_component25
    this.component26 = LabelEntry_component26
    return this
end function

function LabelEntry_equals(other as Object) as Boolean
    if Type(other) <> "roAssociativeArray" then
        return false
    end if
    if other.__type <> "LabelEntry" then
        return false
    end if
    if m.id <> other.id then
        return false
    end if
    if m.text <> other.text then
        return false
    end if
    if m.font <> other.font then
        return false
    end if
    if m.color <> other.color then
        return false
    end if
    if m.horizAlign <> other.horizAlign then
        return false
    end if
    if m.vertAlign <> other.vertAlign then
        return false
    end if
    if m.width <> other.width then
        return false
    end if
    if m.height <> other.height then
        return false
    end if
    if m.maxWidth <> other.maxWidth then
        return false
    end if
    if m.maxLines <> other.maxLines then
        return false
    end if
    if m.wrap <> other.wrap then
        return false
    end if
    if m.truncateOnDelimiter <> other.truncateOnDelimiter then
        return false
    end if
    if m.lineSpacing <> other.lineSpacing then
        return false
    end if
    if m.displayPartialLines <> other.displayPartialLines then
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

function LabelEntry_hashCode() as Integer
    result = 1
    result = ((result * 31) + m.id)
    result = ((result * 31) + m.text)
    result = ((result * 31) + m.font)
    result = ((result * 31) + m.color)
    result = ((result * 31) + m.horizAlign)
    result = ((result * 31) + m.vertAlign)
    result = ((result * 31) + m.width)
    result = ((result * 31) + m.height)
    result = ((result * 31) + m.maxWidth)
    result = ((result * 31) + m.maxLines)
    result = ((result * 31) + m.wrap)
    result = ((result * 31) + m.truncateOnDelimiter)
    result = ((result * 31) + m.lineSpacing)
    result = ((result * 31) + m.displayPartialLines)
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

function LabelEntry_toString() as String
    return ((((((((((((((((((((((((((((((((((((((((((((((((((("LabelEntry(id=" + m.id) + ", text=") + m.text) + ", font=") + m.font) + ", color=") + m.color) + ", horizAlign=") + m.horizAlign) + ", vertAlign=") + m.vertAlign) + ", width=") + m.width) + ", height=") + m.height) + ", maxWidth=") + m.maxWidth) + ", maxLines=") + m.maxLines) + ", wrap=") + m.wrap) + ", truncateOnDelimiter=") + m.truncateOnDelimiter) + ", lineSpacing=") + m.lineSpacing) + ", displayPartialLines=") + m.displayPartialLines) + ", translation=") + m.translation) + ", rotation=") + m.rotation) + ", scale=") + m.scale) + ", scaleRotateCenter=") + m.scaleRotateCenter) + ", opacity=") + m.opacity) + ", visible=") + m.visible) + ", inheritParentOpacity=") + m.inheritParentOpacity) + ", inheritParentTransform=") + m.inheritParentTransform) + ", clippingRect=") + m.clippingRect) + ", renderGroup=") + m.renderGroup) + ", focusable=") + m.focusable) + ", renderPass=") + m.renderPass) + ")"
end function

function LabelEntry_copy(id = invalid, text = invalid, font = invalid, color = invalid, horizAlign = invalid, vertAlign = invalid, width = invalid, height = invalid, maxWidth = invalid, maxLines = invalid, wrap = invalid, truncateOnDelimiter = invalid, lineSpacing = invalid, displayPartialLines = invalid, translation = invalid, rotation = invalid, scale = invalid, scaleRotateCenter = invalid, opacity = invalid, visible = invalid, inheritParentOpacity = invalid, inheritParentTransform = invalid, clippingRect = invalid, renderGroup = invalid, focusable = invalid, renderPass = invalid) as Object
    if id = invalid then
        id = m.id
    end if
    if text = invalid then
        text = m.text
    end if
    if font = invalid then
        font = m.font
    end if
    if color = invalid then
        color = m.color
    end if
    if horizAlign = invalid then
        horizAlign = m.horizAlign
    end if
    if vertAlign = invalid then
        vertAlign = m.vertAlign
    end if
    if width = invalid then
        width = m.width
    end if
    if height = invalid then
        height = m.height
    end if
    if maxWidth = invalid then
        maxWidth = m.maxWidth
    end if
    if maxLines = invalid then
        maxLines = m.maxLines
    end if
    if wrap = invalid then
        wrap = m.wrap
    end if
    if truncateOnDelimiter = invalid then
        truncateOnDelimiter = m.truncateOnDelimiter
    end if
    if lineSpacing = invalid then
        lineSpacing = m.lineSpacing
    end if
    if displayPartialLines = invalid then
        displayPartialLines = m.displayPartialLines
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
    return LabelEntry_create_hyi4mx_k_(id, text, font, color, horizAlign, vertAlign, width, height, maxWidth, maxLines, wrap, truncateOnDelimiter, lineSpacing, displayPartialLines, translation, rotation, scale, scaleRotateCenter, opacity, visible, inheritParentOpacity, inheritParentTransform, clippingRect, renderGroup, focusable, renderPass)
end function

function LabelEntry_component1() as String
    return m.id
end function

function LabelEntry_component2() as Dynamic
    return m.text
end function

function LabelEntry_component3() as Dynamic
    return m.font
end function

function LabelEntry_component4() as Dynamic
    return m.color
end function

function LabelEntry_component5() as Dynamic
    return m.horizAlign
end function

function LabelEntry_component6() as Dynamic
    return m.vertAlign
end function

function LabelEntry_component7() as Dynamic
    return m.width
end function

function LabelEntry_component8() as Dynamic
    return m.height
end function

function LabelEntry_component9() as Dynamic
    return m.maxWidth
end function

function LabelEntry_component10() as Dynamic
    return m.maxLines
end function

function LabelEntry_component11() as Dynamic
    return m.wrap
end function

function LabelEntry_component12() as Dynamic
    return m.truncateOnDelimiter
end function

function LabelEntry_component13() as Dynamic
    return m.lineSpacing
end function

function LabelEntry_component14() as Dynamic
    return m.displayPartialLines
end function

function LabelEntry_component15() as Dynamic
    return m.translation
end function

function LabelEntry_component16() as Dynamic
    return m.rotation
end function

function LabelEntry_component17() as Dynamic
    return m.scale
end function

function LabelEntry_component18() as Dynamic
    return m.scaleRotateCenter
end function

function LabelEntry_component19() as Dynamic
    return m.opacity
end function

function LabelEntry_component20() as Dynamic
    return m.visible
end function

function LabelEntry_component21() as Dynamic
    return m.inheritParentOpacity
end function

function LabelEntry_component22() as Dynamic
    return m.inheritParentTransform
end function

function LabelEntry_component23() as Dynamic
    return m.clippingRect
end function

function LabelEntry_component24() as Dynamic
    return m.renderGroup
end function

function LabelEntry_component25() as Dynamic
    return m.focusable
end function

function LabelEntry_component26() as Dynamic
    return m.renderPass
end function

function PosterEntry_create_8kuq4f_k_(id as String, uri = invalid, width = invalid, height = invalid, loadingBitmapStyle = invalid, loadingBitmapUri = invalid, failedBitmapUri = invalid, blendingMode = invalid, blendColor = invalid, loadSync = invalid, maskUri = invalid, translation = invalid, rotation = invalid, scale = invalid, scaleRotateCenter = invalid, opacity = invalid, visible = invalid, inheritParentOpacity = invalid, inheritParentTransform = invalid, clippingRect = invalid, renderGroup = invalid, focusable = invalid, renderPass = invalid) as Object
    this = {}
    this.__type = "PosterEntry"
    this.__proto = ["PosterEntry", "NodeEntry", "CommonNodeDefaults", "CommonNodeProperties"]
    this.__id = __kotlin_nextObjectId()
    this.id = id
    this.uri = uri
    this.width = width
    this.height = height
    this.loadingBitmapStyle = loadingBitmapStyle
    this.loadingBitmapUri = loadingBitmapUri
    this.failedBitmapUri = failedBitmapUri
    this.blendingMode = blendingMode
    this.blendColor = blendColor
    this.loadSync = loadSync
    this.maskUri = maskUri
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
    this.equals = PosterEntry_equals
    this.hashCode = PosterEntry_hashCode
    this.toString = PosterEntry_toString
    this.copy = PosterEntry_copy
    this.component1 = PosterEntry_component1
    this.component2 = PosterEntry_component2
    this.component3 = PosterEntry_component3
    this.component4 = PosterEntry_component4
    this.component5 = PosterEntry_component5
    this.component6 = PosterEntry_component6
    this.component7 = PosterEntry_component7
    this.component8 = PosterEntry_component8
    this.component9 = PosterEntry_component9
    this.component10 = PosterEntry_component10
    this.component11 = PosterEntry_component11
    this.component12 = PosterEntry_component12
    this.component13 = PosterEntry_component13
    this.component14 = PosterEntry_component14
    this.component15 = PosterEntry_component15
    this.component16 = PosterEntry_component16
    this.component17 = PosterEntry_component17
    this.component18 = PosterEntry_component18
    this.component19 = PosterEntry_component19
    this.component20 = PosterEntry_component20
    this.component21 = PosterEntry_component21
    this.component22 = PosterEntry_component22
    this.component23 = PosterEntry_component23
    return this
end function

function PosterEntry_equals(other as Object) as Boolean
    if Type(other) <> "roAssociativeArray" then
        return false
    end if
    if other.__type <> "PosterEntry" then
        return false
    end if
    if m.id <> other.id then
        return false
    end if
    if m.uri <> other.uri then
        return false
    end if
    if m.width <> other.width then
        return false
    end if
    if m.height <> other.height then
        return false
    end if
    if m.loadingBitmapStyle <> other.loadingBitmapStyle then
        return false
    end if
    if m.loadingBitmapUri <> other.loadingBitmapUri then
        return false
    end if
    if m.failedBitmapUri <> other.failedBitmapUri then
        return false
    end if
    if m.blendingMode <> other.blendingMode then
        return false
    end if
    if m.blendColor <> other.blendColor then
        return false
    end if
    if m.loadSync <> other.loadSync then
        return false
    end if
    if m.maskUri <> other.maskUri then
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

function PosterEntry_hashCode() as Integer
    result = 1
    result = ((result * 31) + m.id)
    result = ((result * 31) + m.uri)
    result = ((result * 31) + m.width)
    result = ((result * 31) + m.height)
    result = ((result * 31) + m.loadingBitmapStyle)
    result = ((result * 31) + m.loadingBitmapUri)
    result = ((result * 31) + m.failedBitmapUri)
    result = ((result * 31) + m.blendingMode)
    result = ((result * 31) + m.blendColor)
    result = ((result * 31) + m.loadSync)
    result = ((result * 31) + m.maskUri)
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

function PosterEntry_toString() as String
    return ((((((((((((((((((((((((((((((((((((((((((((("PosterEntry(id=" + m.id) + ", uri=") + m.uri) + ", width=") + m.width) + ", height=") + m.height) + ", loadingBitmapStyle=") + m.loadingBitmapStyle) + ", loadingBitmapUri=") + m.loadingBitmapUri) + ", failedBitmapUri=") + m.failedBitmapUri) + ", blendingMode=") + m.blendingMode) + ", blendColor=") + m.blendColor) + ", loadSync=") + m.loadSync) + ", maskUri=") + m.maskUri) + ", translation=") + m.translation) + ", rotation=") + m.rotation) + ", scale=") + m.scale) + ", scaleRotateCenter=") + m.scaleRotateCenter) + ", opacity=") + m.opacity) + ", visible=") + m.visible) + ", inheritParentOpacity=") + m.inheritParentOpacity) + ", inheritParentTransform=") + m.inheritParentTransform) + ", clippingRect=") + m.clippingRect) + ", renderGroup=") + m.renderGroup) + ", focusable=") + m.focusable) + ", renderPass=") + m.renderPass) + ")"
end function

function PosterEntry_copy(id = invalid, uri = invalid, width = invalid, height = invalid, loadingBitmapStyle = invalid, loadingBitmapUri = invalid, failedBitmapUri = invalid, blendingMode = invalid, blendColor = invalid, loadSync = invalid, maskUri = invalid, translation = invalid, rotation = invalid, scale = invalid, scaleRotateCenter = invalid, opacity = invalid, visible = invalid, inheritParentOpacity = invalid, inheritParentTransform = invalid, clippingRect = invalid, renderGroup = invalid, focusable = invalid, renderPass = invalid) as Object
    if id = invalid then
        id = m.id
    end if
    if uri = invalid then
        uri = m.uri
    end if
    if width = invalid then
        width = m.width
    end if
    if height = invalid then
        height = m.height
    end if
    if loadingBitmapStyle = invalid then
        loadingBitmapStyle = m.loadingBitmapStyle
    end if
    if loadingBitmapUri = invalid then
        loadingBitmapUri = m.loadingBitmapUri
    end if
    if failedBitmapUri = invalid then
        failedBitmapUri = m.failedBitmapUri
    end if
    if blendingMode = invalid then
        blendingMode = m.blendingMode
    end if
    if blendColor = invalid then
        blendColor = m.blendColor
    end if
    if loadSync = invalid then
        loadSync = m.loadSync
    end if
    if maskUri = invalid then
        maskUri = m.maskUri
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
    return PosterEntry_create_8kuq4f_k_(id, uri, width, height, loadingBitmapStyle, loadingBitmapUri, failedBitmapUri, blendingMode, blendColor, loadSync, maskUri, translation, rotation, scale, scaleRotateCenter, opacity, visible, inheritParentOpacity, inheritParentTransform, clippingRect, renderGroup, focusable, renderPass)
end function

function PosterEntry_component1() as String
    return m.id
end function

function PosterEntry_component2() as Dynamic
    return m.uri
end function

function PosterEntry_component3() as Dynamic
    return m.width
end function

function PosterEntry_component4() as Dynamic
    return m.height
end function

function PosterEntry_component5() as Dynamic
    return m.loadingBitmapStyle
end function

function PosterEntry_component6() as Dynamic
    return m.loadingBitmapUri
end function

function PosterEntry_component7() as Dynamic
    return m.failedBitmapUri
end function

function PosterEntry_component8() as Dynamic
    return m.blendingMode
end function

function PosterEntry_component9() as Dynamic
    return m.blendColor
end function

function PosterEntry_component10() as Dynamic
    return m.loadSync
end function

function PosterEntry_component11() as Dynamic
    return m.maskUri
end function

function PosterEntry_component12() as Dynamic
    return m.translation
end function

function PosterEntry_component13() as Dynamic
    return m.rotation
end function

function PosterEntry_component14() as Dynamic
    return m.scale
end function

function PosterEntry_component15() as Dynamic
    return m.scaleRotateCenter
end function

function PosterEntry_component16() as Dynamic
    return m.opacity
end function

function PosterEntry_component17() as Dynamic
    return m.visible
end function

function PosterEntry_component18() as Dynamic
    return m.inheritParentOpacity
end function

function PosterEntry_component19() as Dynamic
    return m.inheritParentTransform
end function

function PosterEntry_component20() as Dynamic
    return m.clippingRect
end function

function PosterEntry_component21() as Dynamic
    return m.renderGroup
end function

function PosterEntry_component22() as Dynamic
    return m.focusable
end function

function PosterEntry_component23() as Dynamic
    return m.renderPass
end function

function RectangleEntry_create_ftlofj_k_(id as String, width = invalid, height = invalid, color = invalid, blendingMode = invalid, translation = invalid, rotation = invalid, scale = invalid, scaleRotateCenter = invalid, opacity = invalid, visible = invalid, inheritParentOpacity = invalid, inheritParentTransform = invalid, clippingRect = invalid, renderGroup = invalid, focusable = invalid, renderPass = invalid) as Object
    this = {}
    this.__type = "RectangleEntry"
    this.__proto = ["RectangleEntry", "NodeEntry", "CommonNodeDefaults", "CommonNodeProperties"]
    this.__id = __kotlin_nextObjectId()
    this.id = id
    this.width = width
    this.height = height
    this.color = color
    this.blendingMode = blendingMode
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
    this.equals = RectangleEntry_equals
    this.hashCode = RectangleEntry_hashCode
    this.toString = RectangleEntry_toString
    this.copy = RectangleEntry_copy
    this.component1 = RectangleEntry_component1
    this.component2 = RectangleEntry_component2
    this.component3 = RectangleEntry_component3
    this.component4 = RectangleEntry_component4
    this.component5 = RectangleEntry_component5
    this.component6 = RectangleEntry_component6
    this.component7 = RectangleEntry_component7
    this.component8 = RectangleEntry_component8
    this.component9 = RectangleEntry_component9
    this.component10 = RectangleEntry_component10
    this.component11 = RectangleEntry_component11
    this.component12 = RectangleEntry_component12
    this.component13 = RectangleEntry_component13
    this.component14 = RectangleEntry_component14
    this.component15 = RectangleEntry_component15
    this.component16 = RectangleEntry_component16
    this.component17 = RectangleEntry_component17
    return this
end function

function RectangleEntry_equals(other as Object) as Boolean
    if Type(other) <> "roAssociativeArray" then
        return false
    end if
    if other.__type <> "RectangleEntry" then
        return false
    end if
    if m.id <> other.id then
        return false
    end if
    if m.width <> other.width then
        return false
    end if
    if m.height <> other.height then
        return false
    end if
    if m.color <> other.color then
        return false
    end if
    if m.blendingMode <> other.blendingMode then
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

function RectangleEntry_hashCode() as Integer
    result = 1
    result = ((result * 31) + m.id)
    result = ((result * 31) + m.width)
    result = ((result * 31) + m.height)
    result = ((result * 31) + m.color)
    result = ((result * 31) + m.blendingMode)
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

function RectangleEntry_toString() as String
    return ((((((((((((((((((((((((((((((((("RectangleEntry(id=" + m.id) + ", width=") + m.width) + ", height=") + m.height) + ", color=") + m.color) + ", blendingMode=") + m.blendingMode) + ", translation=") + m.translation) + ", rotation=") + m.rotation) + ", scale=") + m.scale) + ", scaleRotateCenter=") + m.scaleRotateCenter) + ", opacity=") + m.opacity) + ", visible=") + m.visible) + ", inheritParentOpacity=") + m.inheritParentOpacity) + ", inheritParentTransform=") + m.inheritParentTransform) + ", clippingRect=") + m.clippingRect) + ", renderGroup=") + m.renderGroup) + ", focusable=") + m.focusable) + ", renderPass=") + m.renderPass) + ")"
end function

function RectangleEntry_copy(id = invalid, width = invalid, height = invalid, color = invalid, blendingMode = invalid, translation = invalid, rotation = invalid, scale = invalid, scaleRotateCenter = invalid, opacity = invalid, visible = invalid, inheritParentOpacity = invalid, inheritParentTransform = invalid, clippingRect = invalid, renderGroup = invalid, focusable = invalid, renderPass = invalid) as Object
    if id = invalid then
        id = m.id
    end if
    if width = invalid then
        width = m.width
    end if
    if height = invalid then
        height = m.height
    end if
    if color = invalid then
        color = m.color
    end if
    if blendingMode = invalid then
        blendingMode = m.blendingMode
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
    return RectangleEntry_create_ftlofj_k_(id, width, height, color, blendingMode, translation, rotation, scale, scaleRotateCenter, opacity, visible, inheritParentOpacity, inheritParentTransform, clippingRect, renderGroup, focusable, renderPass)
end function

function RectangleEntry_component1() as String
    return m.id
end function

function RectangleEntry_component2() as Dynamic
    return m.width
end function

function RectangleEntry_component3() as Dynamic
    return m.height
end function

function RectangleEntry_component4() as Dynamic
    return m.color
end function

function RectangleEntry_component5() as Dynamic
    return m.blendingMode
end function

function RectangleEntry_component6() as Dynamic
    return m.translation
end function

function RectangleEntry_component7() as Dynamic
    return m.rotation
end function

function RectangleEntry_component8() as Dynamic
    return m.scale
end function

function RectangleEntry_component9() as Dynamic
    return m.scaleRotateCenter
end function

function RectangleEntry_component10() as Dynamic
    return m.opacity
end function

function RectangleEntry_component11() as Dynamic
    return m.visible
end function

function RectangleEntry_component12() as Dynamic
    return m.inheritParentOpacity
end function

function RectangleEntry_component13() as Dynamic
    return m.inheritParentTransform
end function

function RectangleEntry_component14() as Dynamic
    return m.clippingRect
end function

function RectangleEntry_component15() as Dynamic
    return m.renderGroup
end function

function RectangleEntry_component16() as Dynamic
    return m.focusable
end function

function RectangleEntry_component17() as Dynamic
    return m.renderPass
end function
