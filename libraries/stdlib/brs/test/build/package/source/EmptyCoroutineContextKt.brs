function EmptyCoroutineContext_create_k_() as Object
    this = {}
    this.__type = "EmptyCoroutineContext"
    this.__proto = ["EmptyCoroutineContext", "CoroutineContext"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.get_Key_k_ = EmptyCoroutineContext_get_Key_k_
    this.fold_AnyN_Function2Element_k_ = EmptyCoroutineContext_fold_AnyN_Function2Element_k_
    this.plus_CoroutineContext_k_ = EmptyCoroutineContext_plus_CoroutineContext_k_
    this.minusKey_Key_k_ = EmptyCoroutineContext_minusKey_Key_k_
    this.hashCode_k_ = EmptyCoroutineContext_hashCode_k_
    this.hashCode = EmptyCoroutineContext_hashCode_k_
    this.toString_k_ = EmptyCoroutineContext_toString_k_
    this.toString = EmptyCoroutineContext_toString_k_
    return this
end function

function EmptyCoroutineContext_getInstance() as Object
    if GetGlobalAA().EmptyCoroutineContext_instance = invalid then
        GetGlobalAA().EmptyCoroutineContext_instance = EmptyCoroutineContext_create_k_()
    end if
    return GetGlobalAA().EmptyCoroutineContext_instance
end function

function EmptyCoroutineContext_get_Key_k_(key as Object) as Dynamic
    return invalid
end function

function EmptyCoroutineContext_fold_AnyN_Function2Element_k_(initial as Dynamic, operation as Object) as Dynamic
    return initial
end function

function EmptyCoroutineContext_plus_CoroutineContext_k_(context as Object) as Object
    return context
end function

function EmptyCoroutineContext_minusKey_Key_k_(key as Object) as Object
    return m
end function

function EmptyCoroutineContext_hashCode_k_() as Integer
    return 0
end function

function EmptyCoroutineContext_toString_k_() as String
    return "EmptyCoroutineContext"
end function
