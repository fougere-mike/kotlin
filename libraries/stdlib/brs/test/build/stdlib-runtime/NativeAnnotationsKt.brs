function NativeName_create_Str_k_(name as String) as Object
    this = {}
    this.__type = "NativeName"
    this.__proto = ["NativeName", "Annotation"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.__get_name = NativeName___get_name_k_
    this.name = name
    return this
end function

function NativeName___get_name_k_() as String
    return m.name
end function
