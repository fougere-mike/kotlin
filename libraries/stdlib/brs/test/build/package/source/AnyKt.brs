function Any_create_k_() as Object
    this = {}
    this.__type = "Any"
    this.__proto = ["Any"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.equals_AnyN_k_ = Any_equals_AnyN_k_
    this.equals = Any_equals_AnyN_k_
    this.hashCode_k_ = Any_hashCode_k_
    this.hashCode = Any_hashCode_k_
    this.toString_k_ = Any_toString_k_
    this.toString = Any_toString_k_
    return this
end function

function Any_equals_AnyN_k_(other as Dynamic) as Boolean
    return __kotlin_identityEquals(m, other)
end function

function Any_hashCode_k_() as Integer
    return 0
end function

function Any_toString_k_() as String
    return "[object]"
end function
