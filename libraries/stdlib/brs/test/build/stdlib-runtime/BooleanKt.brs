function Boolean_create_k_() as Object
    this = {}
    this.__type = "Boolean"
    this.__proto = ["Boolean", "Comparable"]
    this.__id = __kotlin_nextObjectId()
    this.not_k_ = Boolean_not_k_
    this.and_Z_k_ = Boolean_and_Z_k_
    this.or_Z_k_ = Boolean_or_Z_k_
    this.xor_Z_k_ = Boolean_xor_Z_k_
    this.compareTo_Z_k_ = Boolean_compareTo_Z_k_
    this.toString_k_ = Boolean_toString_k_
    this.toString = Boolean_toString_k_
    this.equals_AnyN_k_ = Boolean_equals_AnyN_k_
    this.equals = Boolean_equals_AnyN_k_
    this.hashCode_k_ = Boolean_hashCode_k_
    this.hashCode = Boolean_hashCode_k_
    return this
end function

function Boolean_not_k_() as Boolean
end function

function Boolean_and_Z_k_(other as Boolean) as Boolean
end function

function Boolean_or_Z_k_(other as Boolean) as Boolean
end function

function Boolean_xor_Z_k_(other as Boolean) as Boolean
end function

function Boolean_compareTo_Z_k_(other as Boolean) as Integer
end function

function Boolean_toString_k_() as String
end function

function Boolean_equals_AnyN_k_(other as Dynamic) as Boolean
end function

function Boolean_hashCode_k_() as Integer
end function

function Boolean_Companion_create_k_() as Object
    this = {}
    this.__type = "Boolean_Companion"
    this.__proto = ["Boolean_Companion"]
    this.__id = __kotlin_nextObjectId()
    return this
end function

function Boolean_Companion_getInstance() as Object
    if GetGlobalAA().Boolean_Companion_instance = invalid then
        GetGlobalAA().Boolean_Companion_instance = Boolean_Companion_create_k_()
    end if
    return GetGlobalAA().Boolean_Companion_instance
end function
