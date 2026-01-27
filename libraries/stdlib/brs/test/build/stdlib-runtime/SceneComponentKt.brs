function ComponentBase_create_k_() as Object
    this = {}
    this.__type = "ComponentBase"
    this.__proto = ["ComponentBase"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.onKeyEvent_Str_Z_k_ = ComponentBase_onKeyEvent_Str_Z_k_
    this.__get_top = ComponentBase___get_top_k_
    this.__get_global = ComponentBase___get_global_k_
    this.__get_m = ComponentBase___get_m_k_
    return this
end function

function ComponentBase_onKeyEvent_Str_Z_k_(key as String, press as Boolean) as Boolean
    return false
end function

function ComponentBase___get_top_k_() as Object
    return __get_definedExternally_k_()
end function

function ComponentBase___get_global_k_() as Object
    return __get_definedExternally_k_()
end function

function ComponentBase___get_m_k_() as Object
    return __get_definedExternally_k_()
end function
