function LayoutDirection_create_k_(__name as String, __ordinal as Integer) as Object
    this = {}
    this.__type = "LayoutDirection"
    this.name = __name
    this.ordinal = __ordinal
    this.__proto = ["LayoutDirection", "Comparable"]
    this.__id = __kotlin_nextObjectId()
    this.values_k_ = LayoutDirection_values_k_
    this.valueOf_Str_k_ = LayoutDirection_valueOf_Str_k_
    this.get_entries = LayoutDirection_get_entries_k_
    return this
end function

sub LayoutDirection_initEntries()
    if m.LayoutDirection_entriesInitialized then
        return
    end if
    m.LayoutDirection_entriesInitialized = true
    m.LayoutDirection_horiz = LayoutDirection_create_k_("horiz", 0)
    m.LayoutDirection_vert = LayoutDirection_create_k_("vert", 1)
end sub

function LayoutDirection_values() as Object
    LayoutDirection_initEntries()
    return [m.LayoutDirection_horiz, m.LayoutDirection_vert]
end function

function LayoutDirection_valueOf(name as String) as Object
    LayoutDirection_initEntries()
    if name = "horiz" then
        return m.LayoutDirection_horiz
    else if name = "vert" then
        return m.LayoutDirection_vert
    else
        return invalid
    end if
end function

function HorizAlignment_create_k_(__name as String, __ordinal as Integer) as Object
    this = {}
    this.__type = "HorizAlignment"
    this.name = __name
    this.ordinal = __ordinal
    this.__proto = ["HorizAlignment", "Comparable"]
    this.__id = __kotlin_nextObjectId()
    this.values_k_ = HorizAlignment_values_k_
    this.valueOf_Str_k_ = HorizAlignment_valueOf_Str_k_
    this.get_entries = HorizAlignment_get_entries_k_
    return this
end function

sub HorizAlignment_initEntries()
    if m.HorizAlignment_entriesInitialized then
        return
    end if
    m.HorizAlignment_entriesInitialized = true
    m.HorizAlignment_left = HorizAlignment_create_k_("left", 0)
    m.HorizAlignment_center = HorizAlignment_create_k_("center", 1)
    m.HorizAlignment_right = HorizAlignment_create_k_("right", 2)
    m.HorizAlignment_custom = HorizAlignment_create_k_("custom", 3)
end sub

function HorizAlignment_values() as Object
    HorizAlignment_initEntries()
    return [m.HorizAlignment_left, m.HorizAlignment_center, m.HorizAlignment_right, m.HorizAlignment_custom]
end function

function HorizAlignment_valueOf(name as String) as Object
    HorizAlignment_initEntries()
    if name = "left" then
        return m.HorizAlignment_left
    else if name = "center" then
        return m.HorizAlignment_center
    else if name = "right" then
        return m.HorizAlignment_right
    else if name = "custom" then
        return m.HorizAlignment_custom
    else
        return invalid
    end if
end function

function VertAlignment_create_k_(__name as String, __ordinal as Integer) as Object
    this = {}
    this.__type = "VertAlignment"
    this.name = __name
    this.ordinal = __ordinal
    this.__proto = ["VertAlignment", "Comparable"]
    this.__id = __kotlin_nextObjectId()
    this.values_k_ = VertAlignment_values_k_
    this.valueOf_Str_k_ = VertAlignment_valueOf_Str_k_
    this.get_entries = VertAlignment_get_entries_k_
    return this
end function

sub VertAlignment_initEntries()
    if m.VertAlignment_entriesInitialized then
        return
    end if
    m.VertAlignment_entriesInitialized = true
    m.VertAlignment_top = VertAlignment_create_k_("top", 0)
    m.VertAlignment_center = VertAlignment_create_k_("center", 1)
    m.VertAlignment_bottom = VertAlignment_create_k_("bottom", 2)
    m.VertAlignment_custom = VertAlignment_create_k_("custom", 3)
end sub

function VertAlignment_values() as Object
    VertAlignment_initEntries()
    return [m.VertAlignment_top, m.VertAlignment_center, m.VertAlignment_bottom, m.VertAlignment_custom]
end function

function VertAlignment_valueOf(name as String) as Object
    VertAlignment_initEntries()
    if name = "top" then
        return m.VertAlignment_top
    else if name = "center" then
        return m.VertAlignment_center
    else if name = "bottom" then
        return m.VertAlignment_bottom
    else if name = "custom" then
        return m.VertAlignment_custom
    else
        return invalid
    end if
end function

function ContainerNodeEntry_get_children_k_() as Object
end function

function GroupEntry_create_5xq3b3_k_(id as String, translation = invalid, rotation = invalid, scale = invalid, scaleRotateCenter = invalid, opacity = invalid, visible = invalid, inheritParentOpacity = invalid, inheritParentTransform = invalid, clippingRect = invalid, renderGroup = invalid, focusable = invalid, renderPass = invalid, children = emptyList_k_()) as Object
    this = {}
    this.__type = "GroupEntry"
    this.__proto = ["GroupEntry", "ContainerNodeEntry", "NodeEntry", "CommonNodeDefaults", "CommonNodeProperties"]
    this.__id = __kotlin_nextObjectId()
    this.id = id
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
    this.equals = GroupEntry_equals
    this.hashCode = GroupEntry_hashCode
    this.toString = GroupEntry_toString
    this.copy = GroupEntry_copy
    this.component1 = GroupEntry_component1
    this.component2 = GroupEntry_component2
    this.component3 = GroupEntry_component3
    this.component4 = GroupEntry_component4
    this.component5 = GroupEntry_component5
    this.component6 = GroupEntry_component6
    this.component7 = GroupEntry_component7
    this.component8 = GroupEntry_component8
    this.component9 = GroupEntry_component9
    this.component10 = GroupEntry_component10
    this.component11 = GroupEntry_component11
    this.component12 = GroupEntry_component12
    this.component13 = GroupEntry_component13
    this.component14 = GroupEntry_component14
    return this
end function

function GroupEntry_equals(other as Object) as Boolean
    if Type(other) <> "roAssociativeArray" then
        return false
    end if
    if other.__type <> "GroupEntry" then
        return false
    end if
    if m.id <> other.id then
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

function GroupEntry_hashCode() as Integer
    result = 1
    result = ((result * 31) + m.id)
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

function GroupEntry_toString() as String
    return ((((((((((((((((((((((((((("GroupEntry(id=" + m.id) + ", translation=") + m.translation) + ", rotation=") + m.rotation) + ", scale=") + m.scale) + ", scaleRotateCenter=") + m.scaleRotateCenter) + ", opacity=") + m.opacity) + ", visible=") + m.visible) + ", inheritParentOpacity=") + m.inheritParentOpacity) + ", inheritParentTransform=") + m.inheritParentTransform) + ", clippingRect=") + m.clippingRect) + ", renderGroup=") + m.renderGroup) + ", focusable=") + m.focusable) + ", renderPass=") + m.renderPass) + ", children=") + m.children) + ")"
end function

function GroupEntry_copy(id = invalid, translation = invalid, rotation = invalid, scale = invalid, scaleRotateCenter = invalid, opacity = invalid, visible = invalid, inheritParentOpacity = invalid, inheritParentTransform = invalid, clippingRect = invalid, renderGroup = invalid, focusable = invalid, renderPass = invalid, children = invalid) as Object
    if id = invalid then
        id = m.id
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
    return GroupEntry_create_5xq3b3_k_(id, translation, rotation, scale, scaleRotateCenter, opacity, visible, inheritParentOpacity, inheritParentTransform, clippingRect, renderGroup, focusable, renderPass, children)
end function

function GroupEntry_component1() as String
    return m.id
end function

function GroupEntry_component2() as Dynamic
    return m.translation
end function

function GroupEntry_component3() as Dynamic
    return m.rotation
end function

function GroupEntry_component4() as Dynamic
    return m.scale
end function

function GroupEntry_component5() as Dynamic
    return m.scaleRotateCenter
end function

function GroupEntry_component6() as Dynamic
    return m.opacity
end function

function GroupEntry_component7() as Dynamic
    return m.visible
end function

function GroupEntry_component8() as Dynamic
    return m.inheritParentOpacity
end function

function GroupEntry_component9() as Dynamic
    return m.inheritParentTransform
end function

function GroupEntry_component10() as Dynamic
    return m.clippingRect
end function

function GroupEntry_component11() as Dynamic
    return m.renderGroup
end function

function GroupEntry_component12() as Dynamic
    return m.focusable
end function

function GroupEntry_component13() as Dynamic
    return m.renderPass
end function

function GroupEntry_component14() as Object
    return m.children
end function

function LayoutGroupEntry_create_3vjkej_k_(id as String, layoutDirection = LayoutDirection_horiz, horizAlignment = invalid, vertAlignment = invalid, itemSpacings = invalid, addItemSpacingAfterChild = invalid, translation = invalid, rotation = invalid, scale = invalid, scaleRotateCenter = invalid, opacity = invalid, visible = invalid, inheritParentOpacity = invalid, inheritParentTransform = invalid, clippingRect = invalid, renderGroup = invalid, focusable = invalid, renderPass = invalid, children = emptyList_k_()) as Object
    this = {}
    this.__type = "LayoutGroupEntry"
    this.__proto = ["LayoutGroupEntry", "ContainerNodeEntry", "NodeEntry", "CommonNodeDefaults", "CommonNodeProperties"]
    this.__id = __kotlin_nextObjectId()
    this.id = id
    this.layoutDirection = layoutDirection
    this.horizAlignment = horizAlignment
    this.vertAlignment = vertAlignment
    this.itemSpacings = itemSpacings
    this.addItemSpacingAfterChild = addItemSpacingAfterChild
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
    this.equals = LayoutGroupEntry_equals
    this.hashCode = LayoutGroupEntry_hashCode
    this.toString = LayoutGroupEntry_toString
    this.copy = LayoutGroupEntry_copy
    this.component1 = LayoutGroupEntry_component1
    this.component2 = LayoutGroupEntry_component2
    this.component3 = LayoutGroupEntry_component3
    this.component4 = LayoutGroupEntry_component4
    this.component5 = LayoutGroupEntry_component5
    this.component6 = LayoutGroupEntry_component6
    this.component7 = LayoutGroupEntry_component7
    this.component8 = LayoutGroupEntry_component8
    this.component9 = LayoutGroupEntry_component9
    this.component10 = LayoutGroupEntry_component10
    this.component11 = LayoutGroupEntry_component11
    this.component12 = LayoutGroupEntry_component12
    this.component13 = LayoutGroupEntry_component13
    this.component14 = LayoutGroupEntry_component14
    this.component15 = LayoutGroupEntry_component15
    this.component16 = LayoutGroupEntry_component16
    this.component17 = LayoutGroupEntry_component17
    this.component18 = LayoutGroupEntry_component18
    this.component19 = LayoutGroupEntry_component19
    return this
end function

function LayoutGroupEntry_equals(other as Object) as Boolean
    if Type(other) <> "roAssociativeArray" then
        return false
    end if
    if other.__type <> "LayoutGroupEntry" then
        return false
    end if
    if m.id <> other.id then
        return false
    end if
    if m.layoutDirection <> other.layoutDirection then
        return false
    end if
    if m.horizAlignment <> other.horizAlignment then
        return false
    end if
    if m.vertAlignment <> other.vertAlignment then
        return false
    end if
    if m.itemSpacings <> other.itemSpacings then
        return false
    end if
    if m.addItemSpacingAfterChild <> other.addItemSpacingAfterChild then
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

function LayoutGroupEntry_hashCode() as Integer
    result = 1
    result = ((result * 31) + m.id)
    result = ((result * 31) + m.layoutDirection)
    result = ((result * 31) + m.horizAlignment)
    result = ((result * 31) + m.vertAlignment)
    result = ((result * 31) + m.itemSpacings)
    result = ((result * 31) + m.addItemSpacingAfterChild)
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

function LayoutGroupEntry_toString() as String
    return ((((((((((((((((((((((((((((((((((((("LayoutGroupEntry(id=" + m.id) + ", layoutDirection=") + m.layoutDirection) + ", horizAlignment=") + m.horizAlignment) + ", vertAlignment=") + m.vertAlignment) + ", itemSpacings=") + m.itemSpacings) + ", addItemSpacingAfterChild=") + m.addItemSpacingAfterChild) + ", translation=") + m.translation) + ", rotation=") + m.rotation) + ", scale=") + m.scale) + ", scaleRotateCenter=") + m.scaleRotateCenter) + ", opacity=") + m.opacity) + ", visible=") + m.visible) + ", inheritParentOpacity=") + m.inheritParentOpacity) + ", inheritParentTransform=") + m.inheritParentTransform) + ", clippingRect=") + m.clippingRect) + ", renderGroup=") + m.renderGroup) + ", focusable=") + m.focusable) + ", renderPass=") + m.renderPass) + ", children=") + m.children) + ")"
end function

function LayoutGroupEntry_copy(id = invalid, layoutDirection = invalid, horizAlignment = invalid, vertAlignment = invalid, itemSpacings = invalid, addItemSpacingAfterChild = invalid, translation = invalid, rotation = invalid, scale = invalid, scaleRotateCenter = invalid, opacity = invalid, visible = invalid, inheritParentOpacity = invalid, inheritParentTransform = invalid, clippingRect = invalid, renderGroup = invalid, focusable = invalid, renderPass = invalid, children = invalid) as Object
    if id = invalid then
        id = m.id
    end if
    if layoutDirection = invalid then
        layoutDirection = m.layoutDirection
    end if
    if horizAlignment = invalid then
        horizAlignment = m.horizAlignment
    end if
    if vertAlignment = invalid then
        vertAlignment = m.vertAlignment
    end if
    if itemSpacings = invalid then
        itemSpacings = m.itemSpacings
    end if
    if addItemSpacingAfterChild = invalid then
        addItemSpacingAfterChild = m.addItemSpacingAfterChild
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
    return LayoutGroupEntry_create_3vjkej_k_(id, layoutDirection, horizAlignment, vertAlignment, itemSpacings, addItemSpacingAfterChild, translation, rotation, scale, scaleRotateCenter, opacity, visible, inheritParentOpacity, inheritParentTransform, clippingRect, renderGroup, focusable, renderPass, children)
end function

function LayoutGroupEntry_component1() as String
    return m.id
end function

function LayoutGroupEntry_component2() as Object
    return m.layoutDirection
end function

function LayoutGroupEntry_component3() as Dynamic
    return m.horizAlignment
end function

function LayoutGroupEntry_component4() as Dynamic
    return m.vertAlignment
end function

function LayoutGroupEntry_component5() as Dynamic
    return m.itemSpacings
end function

function LayoutGroupEntry_component6() as Dynamic
    return m.addItemSpacingAfterChild
end function

function LayoutGroupEntry_component7() as Dynamic
    return m.translation
end function

function LayoutGroupEntry_component8() as Dynamic
    return m.rotation
end function

function LayoutGroupEntry_component9() as Dynamic
    return m.scale
end function

function LayoutGroupEntry_component10() as Dynamic
    return m.scaleRotateCenter
end function

function LayoutGroupEntry_component11() as Dynamic
    return m.opacity
end function

function LayoutGroupEntry_component12() as Dynamic
    return m.visible
end function

function LayoutGroupEntry_component13() as Dynamic
    return m.inheritParentOpacity
end function

function LayoutGroupEntry_component14() as Dynamic
    return m.inheritParentTransform
end function

function LayoutGroupEntry_component15() as Dynamic
    return m.clippingRect
end function

function LayoutGroupEntry_component16() as Dynamic
    return m.renderGroup
end function

function LayoutGroupEntry_component17() as Dynamic
    return m.focusable
end function

function LayoutGroupEntry_component18() as Dynamic
    return m.renderPass
end function

function LayoutGroupEntry_component19() as Object
    return m.children
end function
