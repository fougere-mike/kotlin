function ComponentBuilder_create_k_() as Object
    this = {}
    this.__type = "ComponentBuilder"
    this.__proto = ["ComponentBuilder"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.attr_Str_Str_k_ = ComponentBuilder_attr_Str_Str_k_
    this.attr_Str_Number_k_ = ComponentBuilder_attr_Str_Number_k_
    this.attr_Str_Z_k_ = ComponentBuilder_attr_Str_Z_k_
    this.attr_Str_Color_k_ = ComponentBuilder_attr_Str_Color_k_
    this.attr_Str_Vector2D_k_ = ComponentBuilder_attr_Str_Vector2D_k_
    this.attr_Str_Vector4D_k_ = ComponentBuilder_attr_Str_Vector4D_k_
    this.children_Function1LayoutBuilderV_k_ = ComponentBuilder_children_Function1LayoutBuilderV_k_
    this.__get_attributes = ComponentBuilder___get_attributes_k_
    this.__get_childBuilder = ComponentBuilder___get_childBuilder_k_
    this.attributes = mutableMapOf_k_()
    this.childBuilder = LayoutBuilder_create_k_()
    return this
end function

sub ComponentBuilder_attr_Str_Str_k_(name as String, value as String)
    set_rMutableMap_AnyN_AnyN_k_(m.__get_attributes(), name, value)
end sub

sub ComponentBuilder_attr_Str_Number_k_(name as String, value as Object)
    set_rMutableMap_AnyN_AnyN_k_(m.__get_attributes(), name, value.toString())
end sub

sub ComponentBuilder_attr_Str_Z_k_(name as String, value as Boolean)
    __when_tmp0 = invalid
    if value then
        __when_tmp0 = "true"
    else if true then
        __when_tmp0 = "false"
    end if
    set_rMutableMap_AnyN_AnyN_k_(m.__get_attributes(), name, __when_tmp0)

end sub

sub ComponentBuilder_attr_Str_Color_k_(name as String, value as Object)
    set_rMutableMap_AnyN_AnyN_k_(m.__get_attributes(), name, value.__get_hex())
end sub

sub ComponentBuilder_attr_Str_Vector2D_k_(name as String, value as Object)
    set_rMutableMap_AnyN_AnyN_k_(m.__get_attributes(), name, ((("[" + __kotlin_numToStr_F_k_(value.x)) + ", ") + __kotlin_numToStr_F_k_(value.y)) + "]")
end sub

sub ComponentBuilder_attr_Str_Vector4D_k_(name as String, value as Object)
    set_rMutableMap_AnyN_AnyN_k_(m.__get_attributes(), name, ((((((("[" + __kotlin_numToStr_F_k_(value.x)) + ", ") + __kotlin_numToStr_F_k_(value.y)) + ", ") + __kotlin_numToStr_F_k_(value.width)) + ", ") + __kotlin_numToStr_F_k_(value.height)) + "]")
end sub

sub ComponentBuilder_children_Function1LayoutBuilderV_k_(init as Object)
    init.invoke_AnyN_k_(m.__get_childBuilder())
end sub

function ComponentBuilder___get_attributes_k_() as Object
    return m.attributes
end function

function ComponentBuilder___get_childBuilder_k_() as Object
    return m.childBuilder
end function

function LayoutBuilder_create_k_() as Object
    this = {}
    this.__type = "LayoutBuilder"
    this.__proto = ["LayoutBuilder"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.build_k_ = LayoutBuilder_build_k_
    this.buildInterfaceFields_k_ = LayoutBuilder_buildInterfaceFields_k_
    this.interfaceField_Str_StrN_InterfaceFieldType_StrN_StrN_Z_k_ = LayoutBuilder_interfaceField_Str_StrN_InterfaceFieldType_StrN_StrN_Z_k_
    this.group_4ho41h_k_ = LayoutBuilder_group_4ho41h_k_
    this.layoutGroup_mkqx5j_k_ = LayoutBuilder_layoutGroup_mkqx5j_k_
    this.label_hyi4mx_k_ = LayoutBuilder_label_hyi4mx_k_
    this.poster_8kuq4f_k_ = LayoutBuilder_poster_8kuq4f_k_
    this.rectangle_ftlofj_k_ = LayoutBuilder_rectangle_ftlofj_k_
    this.button_152svm_k_ = LayoutBuilder_button_152svm_k_
    this.buttonGroup_3mf06j_k_ = LayoutBuilder_buttonGroup_3mf06j_k_
    this.textEditBox_qn9xve_k_ = LayoutBuilder_textEditBox_qn9xve_k_
    this.keyboard_j75j57_k_ = LayoutBuilder_keyboard_j75j57_k_
    this.component_ufap4w_k_ = LayoutBuilder_component_ufap4w_k_
    this.__get_nodes = LayoutBuilder___get_nodes_k_
    this.__get__interfaceFields = LayoutBuilder___get__interfaceFields_k_
    this.nodes = mutableListOf_k_()
    this._interfaceFields = mutableListOf_k_()
    return this
end function

function LayoutBuilder_build_k_() as Object
    return toList_rIterable_k_(m.__get_nodes())
end function

function LayoutBuilder_buildInterfaceFields_k_() as Object
    return toList_rIterable_k_(m.__get__interfaceFields())
end function

sub LayoutBuilder_interfaceField_Str_StrN_InterfaceFieldType_StrN_StrN_Z_k_(name as String, alias = invalid, type_ = [InterfaceFieldType_initEntries(), m.InterfaceFieldType_NODE][1], value = invalid, onChange = invalid, alwaysNotify = false)
    if alias = invalid then
        alias = invalid
    end if
    if type_ = invalid then
        type_ = [InterfaceFieldType_initEntries(), m.InterfaceFieldType_NODE][1]
    end if
    if value = invalid then
        value = invalid
    end if
    if onChange = invalid then
        onChange = invalid
    end if
    if alwaysNotify = invalid then
        alwaysNotify = false
    end if
    m.__get__interfaceFields().add_AnyN_k_(InterfaceFieldEntry_create_Str_StrN_InterfaceFieldType_StrN_StrN_Z_k_(name, alias, type_, value, onChange, alwaysNotify))
end sub

sub LayoutBuilder_group_4ho41h_k_(id as String, translation = invalid, rotation = invalid, scale = invalid, scaleRotateCenter = invalid, opacity = invalid, visible = invalid, inheritParentOpacity = invalid, inheritParentTransform = invalid, clippingRect = invalid, renderGroup = invalid, focusable = invalid, renderPass = invalid, init = LayoutBuilder_group_lambda_create_k_())
    if translation = invalid then
        translation = invalid
    end if
    if rotation = invalid then
        rotation = invalid
    end if
    if scale = invalid then
        scale = invalid
    end if
    if scaleRotateCenter = invalid then
        scaleRotateCenter = invalid
    end if
    if opacity = invalid then
        opacity = invalid
    end if
    if visible = invalid then
        visible = invalid
    end if
    if inheritParentOpacity = invalid then
        inheritParentOpacity = invalid
    end if
    if inheritParentTransform = invalid then
        inheritParentTransform = invalid
    end if
    if clippingRect = invalid then
        clippingRect = invalid
    end if
    if renderGroup = invalid then
        renderGroup = invalid
    end if
    if focusable = invalid then
        focusable = invalid
    end if
    if renderPass = invalid then
        renderPass = invalid
    end if
    if init = invalid then
        init = LayoutBuilder_group_lambda_create_k_()
    end if
    childBuilder = LayoutBuilder_create_k_()
    init.invoke_AnyN_k_(childBuilder)
    m.__get_nodes().add_AnyN_k_(GroupEntry_create_5xq3b3_k_(id, translation, rotation, scale, scaleRotateCenter, opacity, visible, inheritParentOpacity, inheritParentTransform, clippingRect, renderGroup, focusable, renderPass, childBuilder.build_k_()))
end sub

sub LayoutBuilder_layoutGroup_mkqx5j_k_(id as String, layoutDirection = [LayoutDirection_initEntries(), m.LayoutDirection_horiz][1], horizAlignment = invalid, vertAlignment = invalid, itemSpacings = invalid, addItemSpacingAfterChild = invalid, translation = invalid, rotation = invalid, scale = invalid, scaleRotateCenter = invalid, opacity = invalid, visible = invalid, inheritParentOpacity = invalid, inheritParentTransform = invalid, clippingRect = invalid, renderGroup = invalid, focusable = invalid, renderPass = invalid, init = LayoutBuilder_layoutGroup_lambda_create_k_())
    if layoutDirection = invalid then
        layoutDirection = [LayoutDirection_initEntries(), m.LayoutDirection_horiz][1]
    end if
    if horizAlignment = invalid then
        horizAlignment = invalid
    end if
    if vertAlignment = invalid then
        vertAlignment = invalid
    end if
    if itemSpacings = invalid then
        itemSpacings = invalid
    end if
    if addItemSpacingAfterChild = invalid then
        addItemSpacingAfterChild = invalid
    end if
    if translation = invalid then
        translation = invalid
    end if
    if rotation = invalid then
        rotation = invalid
    end if
    if scale = invalid then
        scale = invalid
    end if
    if scaleRotateCenter = invalid then
        scaleRotateCenter = invalid
    end if
    if opacity = invalid then
        opacity = invalid
    end if
    if visible = invalid then
        visible = invalid
    end if
    if inheritParentOpacity = invalid then
        inheritParentOpacity = invalid
    end if
    if inheritParentTransform = invalid then
        inheritParentTransform = invalid
    end if
    if clippingRect = invalid then
        clippingRect = invalid
    end if
    if renderGroup = invalid then
        renderGroup = invalid
    end if
    if focusable = invalid then
        focusable = invalid
    end if
    if renderPass = invalid then
        renderPass = invalid
    end if
    if init = invalid then
        init = LayoutBuilder_layoutGroup_lambda_create_k_()
    end if
    childBuilder = LayoutBuilder_create_k_()
    init.invoke_AnyN_k_(childBuilder)
    m.__get_nodes().add_AnyN_k_(LayoutGroupEntry_create_3vjkej_k_(id, layoutDirection, horizAlignment, vertAlignment, itemSpacings, addItemSpacingAfterChild, translation, rotation, scale, scaleRotateCenter, opacity, visible, inheritParentOpacity, inheritParentTransform, clippingRect, renderGroup, focusable, renderPass, childBuilder.build_k_()))
end sub

sub LayoutBuilder_label_hyi4mx_k_(id as String, text = invalid, font = invalid, color = invalid, horizAlign = invalid, vertAlign = invalid, width = invalid, height = invalid, maxWidth = invalid, maxLines = invalid, wrap = invalid, truncateOnDelimiter = invalid, lineSpacing = invalid, displayPartialLines = invalid, translation = invalid, rotation = invalid, scale = invalid, scaleRotateCenter = invalid, opacity = invalid, visible = invalid, inheritParentOpacity = invalid, inheritParentTransform = invalid, clippingRect = invalid, renderGroup = invalid, focusable = invalid, renderPass = invalid)
    if text = invalid then
        text = invalid
    end if
    if font = invalid then
        font = invalid
    end if
    if color = invalid then
        color = invalid
    end if
    if horizAlign = invalid then
        horizAlign = invalid
    end if
    if vertAlign = invalid then
        vertAlign = invalid
    end if
    if width = invalid then
        width = invalid
    end if
    if height = invalid then
        height = invalid
    end if
    if maxWidth = invalid then
        maxWidth = invalid
    end if
    if maxLines = invalid then
        maxLines = invalid
    end if
    if wrap = invalid then
        wrap = invalid
    end if
    if truncateOnDelimiter = invalid then
        truncateOnDelimiter = invalid
    end if
    if lineSpacing = invalid then
        lineSpacing = invalid
    end if
    if displayPartialLines = invalid then
        displayPartialLines = invalid
    end if
    if translation = invalid then
        translation = invalid
    end if
    if rotation = invalid then
        rotation = invalid
    end if
    if scale = invalid then
        scale = invalid
    end if
    if scaleRotateCenter = invalid then
        scaleRotateCenter = invalid
    end if
    if opacity = invalid then
        opacity = invalid
    end if
    if visible = invalid then
        visible = invalid
    end if
    if inheritParentOpacity = invalid then
        inheritParentOpacity = invalid
    end if
    if inheritParentTransform = invalid then
        inheritParentTransform = invalid
    end if
    if clippingRect = invalid then
        clippingRect = invalid
    end if
    if renderGroup = invalid then
        renderGroup = invalid
    end if
    if focusable = invalid then
        focusable = invalid
    end if
    if renderPass = invalid then
        renderPass = invalid
    end if
    m.__get_nodes().add_AnyN_k_(LabelEntry_create_hyi4mx_k_(id, text, font, color, horizAlign, vertAlign, width, height, maxWidth, maxLines, wrap, truncateOnDelimiter, lineSpacing, displayPartialLines, translation, rotation, scale, scaleRotateCenter, opacity, visible, inheritParentOpacity, inheritParentTransform, clippingRect, renderGroup, focusable, renderPass))
end sub

sub LayoutBuilder_poster_8kuq4f_k_(id as String, uri = invalid, width = invalid, height = invalid, loadingBitmapStyle = invalid, loadingBitmapUri = invalid, failedBitmapUri = invalid, blendingMode = invalid, blendColor = invalid, loadSync = invalid, maskUri = invalid, translation = invalid, rotation = invalid, scale = invalid, scaleRotateCenter = invalid, opacity = invalid, visible = invalid, inheritParentOpacity = invalid, inheritParentTransform = invalid, clippingRect = invalid, renderGroup = invalid, focusable = invalid, renderPass = invalid)
    if uri = invalid then
        uri = invalid
    end if
    if width = invalid then
        width = invalid
    end if
    if height = invalid then
        height = invalid
    end if
    if loadingBitmapStyle = invalid then
        loadingBitmapStyle = invalid
    end if
    if loadingBitmapUri = invalid then
        loadingBitmapUri = invalid
    end if
    if failedBitmapUri = invalid then
        failedBitmapUri = invalid
    end if
    if blendingMode = invalid then
        blendingMode = invalid
    end if
    if blendColor = invalid then
        blendColor = invalid
    end if
    if loadSync = invalid then
        loadSync = invalid
    end if
    if maskUri = invalid then
        maskUri = invalid
    end if
    if translation = invalid then
        translation = invalid
    end if
    if rotation = invalid then
        rotation = invalid
    end if
    if scale = invalid then
        scale = invalid
    end if
    if scaleRotateCenter = invalid then
        scaleRotateCenter = invalid
    end if
    if opacity = invalid then
        opacity = invalid
    end if
    if visible = invalid then
        visible = invalid
    end if
    if inheritParentOpacity = invalid then
        inheritParentOpacity = invalid
    end if
    if inheritParentTransform = invalid then
        inheritParentTransform = invalid
    end if
    if clippingRect = invalid then
        clippingRect = invalid
    end if
    if renderGroup = invalid then
        renderGroup = invalid
    end if
    if focusable = invalid then
        focusable = invalid
    end if
    if renderPass = invalid then
        renderPass = invalid
    end if
    m.__get_nodes().add_AnyN_k_(PosterEntry_create_8kuq4f_k_(id, uri, width, height, loadingBitmapStyle, loadingBitmapUri, failedBitmapUri, blendingMode, blendColor, loadSync, maskUri, translation, rotation, scale, scaleRotateCenter, opacity, visible, inheritParentOpacity, inheritParentTransform, clippingRect, renderGroup, focusable, renderPass))
end sub

sub LayoutBuilder_rectangle_ftlofj_k_(id as String, width = invalid, height = invalid, color = invalid, blendingMode = invalid, translation = invalid, rotation = invalid, scale = invalid, scaleRotateCenter = invalid, opacity = invalid, visible = invalid, inheritParentOpacity = invalid, inheritParentTransform = invalid, clippingRect = invalid, renderGroup = invalid, focusable = invalid, renderPass = invalid)
    if width = invalid then
        width = invalid
    end if
    if height = invalid then
        height = invalid
    end if
    if color = invalid then
        color = invalid
    end if
    if blendingMode = invalid then
        blendingMode = invalid
    end if
    if translation = invalid then
        translation = invalid
    end if
    if rotation = invalid then
        rotation = invalid
    end if
    if scale = invalid then
        scale = invalid
    end if
    if scaleRotateCenter = invalid then
        scaleRotateCenter = invalid
    end if
    if opacity = invalid then
        opacity = invalid
    end if
    if visible = invalid then
        visible = invalid
    end if
    if inheritParentOpacity = invalid then
        inheritParentOpacity = invalid
    end if
    if inheritParentTransform = invalid then
        inheritParentTransform = invalid
    end if
    if clippingRect = invalid then
        clippingRect = invalid
    end if
    if renderGroup = invalid then
        renderGroup = invalid
    end if
    if focusable = invalid then
        focusable = invalid
    end if
    if renderPass = invalid then
        renderPass = invalid
    end if
    m.__get_nodes().add_AnyN_k_(RectangleEntry_create_ftlofj_k_(id, width, height, color, blendingMode, translation, rotation, scale, scaleRotateCenter, opacity, visible, inheritParentOpacity, inheritParentTransform, clippingRect, renderGroup, focusable, renderPass))
end sub

sub LayoutBuilder_button_152svm_k_(id as String, text = invalid, textFont = invalid, textColor = invalid, focusedTextColor = invalid, backgroundUri = invalid, focusBitmapUri = invalid, focusFootprint = invalid, minWidth = invalid, maxWidth = invalid, height = invalid, iconUri = invalid, focusedIconUri = invalid, showFocusFootprint = invalid, translation = invalid, rotation = invalid, scale = invalid, scaleRotateCenter = invalid, opacity = invalid, visible = invalid, inheritParentOpacity = invalid, inheritParentTransform = invalid, clippingRect = invalid, renderGroup = invalid, focusable = invalid, renderPass = invalid)
    if text = invalid then
        text = invalid
    end if
    if textFont = invalid then
        textFont = invalid
    end if
    if textColor = invalid then
        textColor = invalid
    end if
    if focusedTextColor = invalid then
        focusedTextColor = invalid
    end if
    if backgroundUri = invalid then
        backgroundUri = invalid
    end if
    if focusBitmapUri = invalid then
        focusBitmapUri = invalid
    end if
    if focusFootprint = invalid then
        focusFootprint = invalid
    end if
    if minWidth = invalid then
        minWidth = invalid
    end if
    if maxWidth = invalid then
        maxWidth = invalid
    end if
    if height = invalid then
        height = invalid
    end if
    if iconUri = invalid then
        iconUri = invalid
    end if
    if focusedIconUri = invalid then
        focusedIconUri = invalid
    end if
    if showFocusFootprint = invalid then
        showFocusFootprint = invalid
    end if
    if translation = invalid then
        translation = invalid
    end if
    if rotation = invalid then
        rotation = invalid
    end if
    if scale = invalid then
        scale = invalid
    end if
    if scaleRotateCenter = invalid then
        scaleRotateCenter = invalid
    end if
    if opacity = invalid then
        opacity = invalid
    end if
    if visible = invalid then
        visible = invalid
    end if
    if inheritParentOpacity = invalid then
        inheritParentOpacity = invalid
    end if
    if inheritParentTransform = invalid then
        inheritParentTransform = invalid
    end if
    if clippingRect = invalid then
        clippingRect = invalid
    end if
    if renderGroup = invalid then
        renderGroup = invalid
    end if
    if focusable = invalid then
        focusable = invalid
    end if
    if renderPass = invalid then
        renderPass = invalid
    end if
    m.__get_nodes().add_AnyN_k_(ButtonEntry_create_152svm_k_(id, text, textFont, textColor, focusedTextColor, backgroundUri, focusBitmapUri, focusFootprint, minWidth, maxWidth, height, iconUri, focusedIconUri, showFocusFootprint, translation, rotation, scale, scaleRotateCenter, opacity, visible, inheritParentOpacity, inheritParentTransform, clippingRect, renderGroup, focusable, renderPass))
end sub

sub LayoutBuilder_buttonGroup_3mf06j_k_(id as String, layoutDirection = invalid, itemSpacings = invalid, focusedButton = invalid, wrapDividerWidth = invalid, translation = invalid, rotation = invalid, scale = invalid, scaleRotateCenter = invalid, opacity = invalid, visible = invalid, inheritParentOpacity = invalid, inheritParentTransform = invalid, clippingRect = invalid, renderGroup = invalid, focusable = invalid, renderPass = invalid, init = LayoutBuilder_buttonGroup_lambda_create_k_())
    if layoutDirection = invalid then
        layoutDirection = invalid
    end if
    if itemSpacings = invalid then
        itemSpacings = invalid
    end if
    if focusedButton = invalid then
        focusedButton = invalid
    end if
    if wrapDividerWidth = invalid then
        wrapDividerWidth = invalid
    end if
    if translation = invalid then
        translation = invalid
    end if
    if rotation = invalid then
        rotation = invalid
    end if
    if scale = invalid then
        scale = invalid
    end if
    if scaleRotateCenter = invalid then
        scaleRotateCenter = invalid
    end if
    if opacity = invalid then
        opacity = invalid
    end if
    if visible = invalid then
        visible = invalid
    end if
    if inheritParentOpacity = invalid then
        inheritParentOpacity = invalid
    end if
    if inheritParentTransform = invalid then
        inheritParentTransform = invalid
    end if
    if clippingRect = invalid then
        clippingRect = invalid
    end if
    if renderGroup = invalid then
        renderGroup = invalid
    end if
    if focusable = invalid then
        focusable = invalid
    end if
    if renderPass = invalid then
        renderPass = invalid
    end if
    if init = invalid then
        init = LayoutBuilder_buttonGroup_lambda_create_k_()
    end if
    childBuilder = LayoutBuilder_create_k_()
    init.invoke_AnyN_k_(childBuilder)
    m.__get_nodes().add_AnyN_k_(ButtonGroupEntry_create_aszxc9_k_(id, layoutDirection, itemSpacings, focusedButton, wrapDividerWidth, translation, rotation, scale, scaleRotateCenter, opacity, visible, inheritParentOpacity, inheritParentTransform, clippingRect, renderGroup, focusable, renderPass, childBuilder.build_k_()))
end sub

sub LayoutBuilder_textEditBox_qn9xve_k_(id as String, text = invalid, hint = invalid, textColor = invalid, hintTextColor = invalid, textFont = invalid, maxTextLength = invalid, width = invalid, secureMode = invalid, cursorPosition = invalid, translation = invalid, rotation = invalid, scale = invalid, scaleRotateCenter = invalid, opacity = invalid, visible = invalid, inheritParentOpacity = invalid, inheritParentTransform = invalid, clippingRect = invalid, renderGroup = invalid, focusable = invalid, renderPass = invalid)
    if text = invalid then
        text = invalid
    end if
    if hint = invalid then
        hint = invalid
    end if
    if textColor = invalid then
        textColor = invalid
    end if
    if hintTextColor = invalid then
        hintTextColor = invalid
    end if
    if textFont = invalid then
        textFont = invalid
    end if
    if maxTextLength = invalid then
        maxTextLength = invalid
    end if
    if width = invalid then
        width = invalid
    end if
    if secureMode = invalid then
        secureMode = invalid
    end if
    if cursorPosition = invalid then
        cursorPosition = invalid
    end if
    if translation = invalid then
        translation = invalid
    end if
    if rotation = invalid then
        rotation = invalid
    end if
    if scale = invalid then
        scale = invalid
    end if
    if scaleRotateCenter = invalid then
        scaleRotateCenter = invalid
    end if
    if opacity = invalid then
        opacity = invalid
    end if
    if visible = invalid then
        visible = invalid
    end if
    if inheritParentOpacity = invalid then
        inheritParentOpacity = invalid
    end if
    if inheritParentTransform = invalid then
        inheritParentTransform = invalid
    end if
    if clippingRect = invalid then
        clippingRect = invalid
    end if
    if renderGroup = invalid then
        renderGroup = invalid
    end if
    if focusable = invalid then
        focusable = invalid
    end if
    if renderPass = invalid then
        renderPass = invalid
    end if
    m.__get_nodes().add_AnyN_k_(TextEditBoxEntry_create_qn9xve_k_(id, text, hint, textColor, hintTextColor, textFont, maxTextLength, width, secureMode, cursorPosition, translation, rotation, scale, scaleRotateCenter, opacity, visible, inheritParentOpacity, inheritParentTransform, clippingRect, renderGroup, focusable, renderPass))
end sub

sub LayoutBuilder_keyboard_j75j57_k_(id as String, text = invalid, keyboardType = invalid, lowercase = invalid, translation = invalid, rotation = invalid, scale = invalid, scaleRotateCenter = invalid, opacity = invalid, visible = invalid, inheritParentOpacity = invalid, inheritParentTransform = invalid, clippingRect = invalid, renderGroup = invalid, focusable = invalid, renderPass = invalid)
    if text = invalid then
        text = invalid
    end if
    if keyboardType = invalid then
        keyboardType = invalid
    end if
    if lowercase = invalid then
        lowercase = invalid
    end if
    if translation = invalid then
        translation = invalid
    end if
    if rotation = invalid then
        rotation = invalid
    end if
    if scale = invalid then
        scale = invalid
    end if
    if scaleRotateCenter = invalid then
        scaleRotateCenter = invalid
    end if
    if opacity = invalid then
        opacity = invalid
    end if
    if visible = invalid then
        visible = invalid
    end if
    if inheritParentOpacity = invalid then
        inheritParentOpacity = invalid
    end if
    if inheritParentTransform = invalid then
        inheritParentTransform = invalid
    end if
    if clippingRect = invalid then
        clippingRect = invalid
    end if
    if renderGroup = invalid then
        renderGroup = invalid
    end if
    if focusable = invalid then
        focusable = invalid
    end if
    if renderPass = invalid then
        renderPass = invalid
    end if
    m.__get_nodes().add_AnyN_k_(KeyboardEntry_create_j75j57_k_(id, text, keyboardType, lowercase, translation, rotation, scale, scaleRotateCenter, opacity, visible, inheritParentOpacity, inheritParentTransform, clippingRect, renderGroup, focusable, renderPass))
end sub

sub LayoutBuilder_component_ufap4w_k_(componentType as String, id as String, translation = invalid, rotation = invalid, scale = invalid, scaleRotateCenter = invalid, opacity = invalid, visible = invalid, inheritParentOpacity = invalid, inheritParentTransform = invalid, clippingRect = invalid, renderGroup = invalid, focusable = invalid, renderPass = invalid, init = LayoutBuilder_component_lambda_create_k_())
    if translation = invalid then
        translation = invalid
    end if
    if rotation = invalid then
        rotation = invalid
    end if
    if scale = invalid then
        scale = invalid
    end if
    if scaleRotateCenter = invalid then
        scaleRotateCenter = invalid
    end if
    if opacity = invalid then
        opacity = invalid
    end if
    if visible = invalid then
        visible = invalid
    end if
    if inheritParentOpacity = invalid then
        inheritParentOpacity = invalid
    end if
    if inheritParentTransform = invalid then
        inheritParentTransform = invalid
    end if
    if clippingRect = invalid then
        clippingRect = invalid
    end if
    if renderGroup = invalid then
        renderGroup = invalid
    end if
    if focusable = invalid then
        focusable = invalid
    end if
    if renderPass = invalid then
        renderPass = invalid
    end if
    if init = invalid then
        init = LayoutBuilder_component_lambda_create_k_()
    end if
    builder = ComponentBuilder_create_k_()
    init.invoke_AnyN_k_(builder)
    m.__get_nodes().add_AnyN_k_(CustomComponentEntry_create_g4xuem_k_(id, componentType, toMap_rMap_k_(builder.__get_attributes()), translation, rotation, scale, scaleRotateCenter, opacity, visible, inheritParentOpacity, inheritParentTransform, clippingRect, renderGroup, focusable, renderPass, builder.__get_childBuilder().build_k_()))
end sub

function LayoutBuilder___get_nodes_k_() as Object
    return m.nodes
end function

function LayoutBuilder___get__interfaceFields_k_() as Object
    return m._interfaceFields
end function

function LayoutBuilder_group_lambda_create_k_() as Object
    this = {}
    this.__type = "LayoutBuilder_group_lambda"
    this.__proto = ["LayoutBuilder_group_lambda", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = LayoutBuilder_group_lambda_invoke_AnyN_k_
    return this
end function

sub LayoutBuilder_group_lambda_invoke_AnyN_k_(__this as Object)
    return
end sub

function LayoutBuilder_layoutGroup_lambda_create_k_() as Object
    this = {}
    this.__type = "LayoutBuilder_layoutGroup_lambda"
    this.__proto = ["LayoutBuilder_layoutGroup_lambda", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = LayoutBuilder_layoutGroup_lambda_invoke_AnyN_k_
    return this
end function

sub LayoutBuilder_layoutGroup_lambda_invoke_AnyN_k_(__this as Object)
    return
end sub

function LayoutBuilder_buttonGroup_lambda_create_k_() as Object
    this = {}
    this.__type = "LayoutBuilder_buttonGroup_lambda"
    this.__proto = ["LayoutBuilder_buttonGroup_lambda", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = LayoutBuilder_buttonGroup_lambda_invoke_AnyN_k_
    return this
end function

sub LayoutBuilder_buttonGroup_lambda_invoke_AnyN_k_(__this as Object)
    return
end sub

function LayoutBuilder_component_lambda_create_k_() as Object
    this = {}
    this.__type = "LayoutBuilder_component_lambda"
    this.__proto = ["LayoutBuilder_component_lambda", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = LayoutBuilder_component_lambda_invoke_AnyN_k_
    return this
end function

sub LayoutBuilder_component_lambda_invoke_AnyN_k_(__this as Object)
    return
end sub
