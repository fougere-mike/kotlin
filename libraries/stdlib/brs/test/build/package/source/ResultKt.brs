function Result_create_AnyN_k_(value as Dynamic) as Object
    this = {}
    this.__type = "Result"
    this.__proto = ["Result"]
    this.__id = __kotlin_nextObjectId()
    this.getOrNull_k_ = Result_getOrNull_k_
    this.exceptionOrNull_k_ = Result_exceptionOrNull_k_
    this.getOrThrow_k_ = Result_getOrThrow_k_
    this.throwOnFailure_k_ = Result_throwOnFailure_k_
    this.toString_k_ = Result_toString_k_
    this.toString = Result_toString_k_
    this.hashCode_k_ = Result_hashCode_k_
    this.hashCode = Result_hashCode_k_
    this.equals_AnyN_k_ = Result_equals_AnyN_k_
    this.equals = Result_equals_AnyN_k_
    this.get_value = Result_get_value_k_
    this.get_isSuccess = Result_get_isSuccess_k_
    this.get_isFailure = Result_get_isFailure_k_
    this.value = value
    return this
end function

function Result_getOrNull_k_() as Dynamic
    __when_tmp0 = invalid
    if m.get_isFailure() then
        __when_tmp0 = invalid
    else if true then
        __when_tmp0 = m.get_value()
    end if
    return __when_tmp0

end function

function Result_exceptionOrNull_k_() as Dynamic
    tmp0_subject = m.get_value()
    __when_tmp1 = invalid
    if __kotlin_isInstanceOf(tmp0_subject, "Result_Failure") then
        __when_tmp1 = m.get_value().get_exception()
    else if true then
        __when_tmp1 = invalid
    end if
    return __when_tmp1

end function

function Result_getOrThrow_k_() as Dynamic
    m.throwOnFailure_k_()
    return m.get_value()
end function

sub Result_throwOnFailure_k_()
    if __kotlin_isInstanceOf(m.get_value(), "Result_Failure") then
        throw m.get_value().get_exception()
    end if
end sub

function Result_toString_k_() as String
    tmp0_subject = m.get_value()
    __when_tmp2 = invalid
    if __kotlin_isInstanceOf(tmp0_subject, "Result_Failure") then
        __when_tmp2 = (("Failure(" + m.get_value().get_exception().toString()) + ")")
    else if true then
        __when_tmp2 = (("Success(" + toString_AnyN_k_(m.get_value())) + ")")
    end if
    return __when_tmp2

end function

function Result_hashCode_k_() as Integer
    __when_tmp3 = invalid
    if m.value = invalid then
        __when_tmp3 = 0
    else if true then
        __when_tmp3 = m.value.hashCode()
    end if
    return __when_tmp3

end function

function Result_equals_AnyN_k_(other as Dynamic) as Boolean
    if not __kotlin_isInstanceOf(other, "Result") then
        return false
    end if
    tmp0_other_with_cast = other
    if not brsStructuralEquals_AnyN_AnyN_k_(m.value, tmp0_other_with_cast.value) then
        return false
    end if
    return true
end function

function Result_get_value_k_() as Dynamic
    return m.value
end function

function Result_get_isSuccess_k_() as Boolean
    return not __kotlin_isInstanceOf(m.get_value(), "Result_Failure")
end function

function Result_get_isFailure_k_() as Boolean
    return __kotlin_isInstanceOf(m.get_value(), "Result_Failure")
end function

function Result_Companion_create_k_() as Object
    this = {}
    this.__type = "Result_Companion"
    this.__proto = ["Result_Companion"]
    this.__id = __kotlin_nextObjectId()
    this.success_AnyN_k_ = Result_Companion_success_AnyN_k_
    this.failure_Throwable_k_ = Result_Companion_failure_Throwable_k_
    return this
end function

function Result_Companion_getInstance() as Object
    if GetGlobalAA().Result_Companion_instance = invalid then
        GetGlobalAA().Result_Companion_instance = Result_Companion_create_k_()
    end if
    return GetGlobalAA().Result_Companion_instance
end function

function Result_Companion_success_AnyN_k_(value as Dynamic) as Object
    return Result_create_AnyN_k_(value)
end function

function Result_Companion_failure_Throwable_k_(exception as Object) as Object
    return Result_create_AnyN_k_(Result_Failure_create_Throwable_k_(exception))
end function

function Result_Failure_create_Throwable_k_(exception as Object) as Object
    this = {}
    this.__type = "Result_Failure"
    this.__proto = ["Result_Failure"]
    this.__id = __kotlin_nextObjectId()
    this.get_exception = Result_Failure_get_exception_k_
    this.exception = exception
    return this
end function

function Result_Failure_get_exception_k_() as Object
    return m.exception
end function

function createFailure_Throwable_k_(exception as Object) as Object
    return Result_Failure_create_Throwable_k_(exception)
end function
