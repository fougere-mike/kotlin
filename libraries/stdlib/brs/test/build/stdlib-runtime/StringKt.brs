function String_create_k_() as Object
    this = {}
    this.__type = "String"
    this.__proto = ["String", "Comparable", "CharSequence"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.plus_AnyN_k_ = String_plus_AnyN_k_
    this.get_I_k_ = String_get_I_k_
    this.subSequence_I_I_k_ = String_subSequence_I_I_k_
    this.compareTo_AnyN_k_ = String_compareTo_AnyN_k_
    this.equals_AnyN_k_ = String_equals_AnyN_k_
    this.equals = String_equals_AnyN_k_
    this.hashCode_k_ = String_hashCode_k_
    this.hashCode = String_hashCode_k_
    this.toString_k_ = String_toString_k_
    this.toString = String_toString_k_
    this.__get_length = String___get_length_k_
    return this
end function

function String_plus_AnyN_k_(other as Dynamic) as String
end function

function String_get_I_k_(index as Integer) as Object
end function

function String_subSequence_I_I_k_(startIndex as Integer, endIndex as Integer) as Object
end function

function String_compareTo_AnyN_k_(other as String) as Integer
end function

function String_equals_AnyN_k_(other as Dynamic) as Boolean
end function

function String_hashCode_k_() as Integer
end function

function String_toString_k_() as String
end function

function String___get_length_k_() as Integer
    return m.length
end function

function String_Companion_create_k_() as Object
    this = {}
    this.__type = "String_Companion"
    this.__proto = ["String_Companion"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    return this
end function

function String_Companion_getInstance() as Object
    if GetGlobalAA().String_Companion_instance = invalid then
        GetGlobalAA().String_Companion_instance = String_Companion_create_k_()
    end if
    return GetGlobalAA().String_Companion_instance
end function
