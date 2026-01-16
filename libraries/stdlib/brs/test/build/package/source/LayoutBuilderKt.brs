function LayoutBuilder_create_k_() as Object
    this = {}
    this.__type = "LayoutBuilder"
    this.__proto = ["LayoutBuilder"]
    this.__id = __kotlin_nextObjectId()
    this.build_k_ = LayoutBuilder_build_k_
    this.group_4ho41h_k_ = LayoutBuilder_group_4ho41h_k_
    this.layoutGroup_mkqx5j_k_ = LayoutBuilder_layoutGroup_mkqx5j_k_
    this.label_hyi4mx_k_ = LayoutBuilder_label_hyi4mx_k_
    this.poster_8kuq4f_k_ = LayoutBuilder_poster_8kuq4f_k_
    this.rectangle_ftlofj_k_ = LayoutBuilder_rectangle_ftlofj_k_
    this.button_152svm_k_ = LayoutBuilder_button_152svm_k_
    this.buttonGroup_3mf06j_k_ = LayoutBuilder_buttonGroup_3mf06j_k_
    this.textEditBox_qn9xve_k_ = LayoutBuilder_textEditBox_qn9xve_k_
    this.keyboard_j75j57_k_ = LayoutBuilder_keyboard_j75j57_k_
    this.get_nodes = LayoutBuilder_get_nodes_k_
    this.nodes = mutableListOf_k_()
    return this
end function

function LayoutBuilder_build_k_() as Object
    return toList_rIterable_k_(m.get_nodes())
end function

sub LayoutBuilder_group_4ho41h_k_(id as String, translation = invalid, rotation = invalid, scale = invalid, scaleRotateCenter = invalid, opacity = invalid, visible = invalid, inheritParentOpacity = invalid, inheritParentTransform = invalid, clippingRect = invalid, renderGroup = invalid, focusable = invalid, renderPass = invalid, init = {invoke: function(m as Object) as Void
    return
end function})
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
        init = {invoke: function(m as Object) as Void
            return
        end function}
    end if
    childBuilder = LayoutBuilder_create_k_()
    init.invoke(childBuilder)
    m.get_nodes().add_AnyN_k_(GroupEntry_create_5xq3b3_k_(id, translation, rotation, scale, scaleRotateCenter, opacity, visible, inheritParentOpacity, inheritParentTransform, clippingRect, renderGroup, focusable, renderPass, childBuilder.build_k_()))
end sub

sub LayoutBuilder_layoutGroup_mkqx5j_k_(id as String, layoutDirection = LayoutDirection_horiz, horizAlignment = invalid, vertAlignment = invalid, itemSpacings = invalid, addItemSpacingAfterChild = invalid, translation = invalid, rotation = invalid, scale = invalid, scaleRotateCenter = invalid, opacity = invalid, visible = invalid, inheritParentOpacity = invalid, inheritParentTransform = invalid, clippingRect = invalid, renderGroup = invalid, focusable = invalid, renderPass = invalid, init = {invoke: function(m as Object) as Void
    return
end function})
    if layoutDirection = invalid then
        layoutDirection = LayoutDirection_horiz
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
        init = {invoke: function(m as Object) as Void
            return
        end function}
    end if
    childBuilder = LayoutBuilder_create_k_()
    init.invoke(childBuilder)
    m.get_nodes().add_AnyN_k_(LayoutGroupEntry_create_3vjkej_k_(id, layoutDirection, horizAlignment, vertAlignment, itemSpacings, addItemSpacingAfterChild, translation, rotation, scale, scaleRotateCenter, opacity, visible, inheritParentOpacity, inheritParentTransform, clippingRect, renderGroup, focusable, renderPass, childBuilder.build_k_()))
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
    m.get_nodes().add_AnyN_k_(LabelEntry_create_hyi4mx_k_(id, text, font, color, horizAlign, vertAlign, width, height, maxWidth, maxLines, wrap, truncateOnDelimiter, lineSpacing, displayPartialLines, translation, rotation, scale, scaleRotateCenter, opacity, visible, inheritParentOpacity, inheritParentTransform, clippingRect, renderGroup, focusable, renderPass))
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
    m.get_nodes().add_AnyN_k_(PosterEntry_create_8kuq4f_k_(id, uri, width, height, loadingBitmapStyle, loadingBitmapUri, failedBitmapUri, blendingMode, blendColor, loadSync, maskUri, translation, rotation, scale, scaleRotateCenter, opacity, visible, inheritParentOpacity, inheritParentTransform, clippingRect, renderGroup, focusable, renderPass))
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
    m.get_nodes().add_AnyN_k_(RectangleEntry_create_ftlofj_k_(id, width, height, color, blendingMode, translation, rotation, scale, scaleRotateCenter, opacity, visible, inheritParentOpacity, inheritParentTransform, clippingRect, renderGroup, focusable, renderPass))
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
    m.get_nodes().add_AnyN_k_(ButtonEntry_create_152svm_k_(id, text, textFont, textColor, focusedTextColor, backgroundUri, focusBitmapUri, focusFootprint, minWidth, maxWidth, height, iconUri, focusedIconUri, showFocusFootprint, translation, rotation, scale, scaleRotateCenter, opacity, visible, inheritParentOpacity, inheritParentTransform, clippingRect, renderGroup, focusable, renderPass))
end sub

sub LayoutBuilder_buttonGroup_3mf06j_k_(id as String, layoutDirection = invalid, itemSpacings = invalid, focusedButton = invalid, wrapDividerWidth = invalid, translation = invalid, rotation = invalid, scale = invalid, scaleRotateCenter = invalid, opacity = invalid, visible = invalid, inheritParentOpacity = invalid, inheritParentTransform = invalid, clippingRect = invalid, renderGroup = invalid, focusable = invalid, renderPass = invalid, init = {invoke: function(m as Object) as Void
    return
end function})
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
        init = {invoke: function(m as Object) as Void
            return
        end function}
    end if
    childBuilder = LayoutBuilder_create_k_()
    init.invoke(childBuilder)
    m.get_nodes().add_AnyN_k_(ButtonGroupEntry_create_aszxc9_k_(id, layoutDirection, itemSpacings, focusedButton, wrapDividerWidth, translation, rotation, scale, scaleRotateCenter, opacity, visible, inheritParentOpacity, inheritParentTransform, clippingRect, renderGroup, focusable, renderPass, childBuilder.build_k_()))
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
    m.get_nodes().add_AnyN_k_(TextEditBoxEntry_create_qn9xve_k_(id, text, hint, textColor, hintTextColor, textFont, maxTextLength, width, secureMode, cursorPosition, translation, rotation, scale, scaleRotateCenter, opacity, visible, inheritParentOpacity, inheritParentTransform, clippingRect, renderGroup, focusable, renderPass))
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
    m.get_nodes().add_AnyN_k_(KeyboardEntry_create_j75j57_k_(id, text, keyboardType, lowercase, translation, rotation, scale, scaleRotateCenter, opacity, visible, inheritParentOpacity, inheritParentTransform, clippingRect, renderGroup, focusable, renderPass))
end sub

function LayoutBuilder_get_nodes_k_() as Object
    return m.nodes
end function
