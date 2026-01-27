function Array_create_I_Function1I_k_(size as Integer, init as Object) as Object
    this = {}
    this.__type = "Array"
    this.__proto = ["Array"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.get_I_k_ = Array_get_I_k_
    this.set_I_AnyN_k_ = Array_set_I_AnyN_k_
    this.iterator_k_ = Array_iterator_k_
    this.__get_size = Array___get_size_k_
    return this
end function

function Array_get_I_k_(index as Integer) as Dynamic
end function

sub Array_set_I_AnyN_k_(index as Integer, value as Dynamic)
end sub

function Array_iterator_k_() as Object
end function

function Array___get_size_k_() as Integer
    return m.size
end function
