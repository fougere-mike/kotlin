function CoroutineContext_get_Key_k_(key as Object) as Dynamic
end function

function CoroutineContext_fold_AnyN_Function2Element_k_(initial as Dynamic, operation as Object) as Dynamic
end function

function CoroutineContext_plus_CoroutineContext_k_(context as Object) as Object
    __when_tmp3 = invalid
    if __kotlin_identityEquals(context, EmptyCoroutineContext_getInstance()) then
        __when_tmp3 = m
    else if true then
        __when_tmp3 = context.fold_AnyN_Function2Element_k_(m, CoroutineContext_plus_lambda_1_create_k_())
    end if
    return __when_tmp3

end function

function CoroutineContext_minusKey_Key_k_(key as Object) as Object
end function

function CoroutineContext_Element_get_Key_k_(key as Object) as Dynamic
    __when_tmp4 = invalid
    if brsStructuralEquals_AnyN_AnyN_k_(m.__get_key(), key) then
        __when_tmp4 = m
    else if true then
        __when_tmp4 = invalid
    end if
    return __when_tmp4

end function

function CoroutineContext_Element_fold_AnyN_Function2Element_k_(initial as Dynamic, operation as Object) as Dynamic
    return operation.invoke_AnyN_AnyN_k_(initial, m)
end function

function CoroutineContext_Element_minusKey_Key_k_(key as Object) as Object
    __when_tmp5 = invalid
    if brsStructuralEquals_AnyN_AnyN_k_(m.__get_key(), key) then
        __when_tmp5 = EmptyCoroutineContext_getInstance()
    else if true then
        __when_tmp5 = m
    end if
    return __when_tmp5

end function

function CoroutineContext_Element___get_key_k_() as Object
end function

function CoroutineContext_plus_lambda_1_create_k_() as Object
    this = {}
    this.__type = "CoroutineContext_plus_lambda_1"
    this.__proto = ["CoroutineContext_plus_lambda_1", "Function2", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_AnyN_k_ = CoroutineContext_plus_lambda_1_invoke_AnyN_AnyN_k_
    return this
end function

function CoroutineContext_plus_lambda_1_invoke_AnyN_AnyN_k_(acc as Object, element as Object) as Object
    removed = acc.minusKey_Key_k_(element.__get_key())
    __when_tmp2 = invalid
    if __kotlin_identityEquals(removed, EmptyCoroutineContext_getInstance()) then
        __when_tmp2 = element
    else if true then
        interceptor = removed.get_Key_k_(ContinuationInterceptor_Key_getInstance())
        __when_tmp1 = invalid
        if interceptor = invalid then
            __when_tmp1 = CombinedContext_create_CoroutineContext_Element_k_(removed, element)
        else if true then
            left = removed.minusKey_Key_k_(ContinuationInterceptor_Key_getInstance())
            __when_tmp0 = invalid
            if __kotlin_identityEquals(left, EmptyCoroutineContext_getInstance()) then
                __when_tmp0 = CombinedContext_create_CoroutineContext_Element_k_(element, interceptor)
            else if true then
                __when_tmp0 = CombinedContext_create_CoroutineContext_Element_k_(CombinedContext_create_CoroutineContext_Element_k_(left, element), interceptor)
            end if
            __when_tmp1 = __when_tmp0
        end if
        __when_tmp2 = __when_tmp1
    end if
    return __when_tmp2

end function
