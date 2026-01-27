function CombinedContext_create_CoroutineContext_Element_k_(left as Object, element as Object) as Object
    this = {}
    this.__type = "CombinedContext"
    this.__proto = ["CombinedContext", "CoroutineContext"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.get_Key_k_ = CombinedContext_get_Key_k_
    this.fold_AnyN_Function2Element_k_ = CombinedContext_fold_AnyN_Function2Element_k_
    this.minusKey_Key_k_ = CombinedContext_minusKey_Key_k_
    this.equals_AnyN_k_ = CombinedContext_equals_AnyN_k_
    this.equals = CombinedContext_equals_AnyN_k_
    this.hashCode_k_ = CombinedContext_hashCode_k_
    this.hashCode = CombinedContext_hashCode_k_
    this.toString_k_ = CombinedContext_toString_k_
    this.toString = CombinedContext_toString_k_
    this.size_k_ = CombinedContext_size_k_
    this.containsAll_CombinedContext_k_ = CombinedContext_containsAll_CombinedContext_k_
    this.plus_CoroutineContext_k_ = CombinedContext_plus_CoroutineContext_k_
    this.__get_left = CombinedContext___get_left_k_
    this.__get_element = CombinedContext___get_element_k_
    this.left = left
    this.element = element
    return this
end function

function CombinedContext_get_Key_k_(key as Object) as Dynamic
    cur = m
    while true
        tmp0_safe_receiver = cur.__get_element().get_Key_k_(key)
        if tmp0_safe_receiver = invalid then

        else if true then
            tmp0 = tmp0_safe_receiver
            tmp_ret_0 = invalid
            while true
                this = tmp0
                tmp0 = this
                it = tmp0
                return it

                tmp_ret_0 = invalid
                exit while
            end while
        end if

        next_ = cur.__get_left()
        if __kotlin_isInstanceOf(next_, "CombinedContext") then
            cur = next_
        else if true then
            return next_.get_Key_k_(key)
        end if
    end while
end function

function CombinedContext_fold_AnyN_Function2Element_k_(initial as Dynamic, operation as Object) as Dynamic
    return operation.invoke_AnyN_AnyN_k_(m.__get_left().fold_AnyN_Function2Element_k_(initial, operation), m.__get_element())
end function

function CombinedContext_minusKey_Key_k_(key as Object) as Object
    tmp0_safe_receiver = m.__get_element().get_Key_k_(key)
    if tmp0_safe_receiver = invalid then

    else if true then
        tmp0 = tmp0_safe_receiver
        tmp_ret_0 = invalid
        while true
            this = tmp0
            tmp0 = this
            it = tmp0
            return m.__get_left()

            tmp_ret_0 = invalid
            exit while
        end while
    end if

    newLeft = m.__get_left().minusKey_Key_k_(key)
    __when_tmp0 = invalid
    if __kotlin_identityEquals(newLeft, m.__get_left()) then
        __when_tmp0 = m
    else if __kotlin_identityEquals(newLeft, EmptyCoroutineContext_getInstance()) then
        __when_tmp0 = m.__get_element()
    else if true then
        __when_tmp0 = CombinedContext_create_CoroutineContext_Element_k_(newLeft, m.__get_element())
    end if
    return __when_tmp0

end function

function CombinedContext_equals_AnyN_k_(other as Dynamic) as Boolean
    return __kotlin_identityEquals(m, other) or ((__kotlin_isInstanceOf(other, "CombinedContext") and (m.size_k_() = other.size_k_())) and m.containsAll_CombinedContext_k_(other))
end function

function CombinedContext_hashCode_k_() as Integer
    return m.__get_left().hashCode() + m.__get_element().hashCode()
end function

function CombinedContext_toString_k_() as String
    return ("[" + m.fold_AnyN_Function2Element_k_("", CombinedContext_toString_lambda_create_k_())) + "]"
end function

function CombinedContext_size_k_() as Integer
    cur = m
    size = 2
    while true
        __safeCast_tmp198 = cur.__get_left()
        tmp0_elvis_lhs = (function(__kotlin_isInstanceOf, __safeCast_tmp198)
            if __kotlin_isInstanceOf(__safeCast_tmp198, "CombinedContext") then return __safeCast_tmp198 else return invalid
        end function)(__kotlin_isInstanceOf, __safeCast_tmp198)
        __when_tmp2 = invalid
        if tmp0_elvis_lhs = invalid then
            return size
        else if true then
            __when_tmp2 = tmp0_elvis_lhs
        end if
        cur = __when_tmp2
        size = (size + 1)
    end while
end function

function CombinedContext_containsAll_CombinedContext_k_(context as Object) as Boolean
    cur = context
    while true
        if cur.__get_element().get_Key_k_(cur.__get_element().__get_key()) = invalid then
            return false
        end if
        next_ = cur.__get_left()
        if __kotlin_isInstanceOf(next_, "CombinedContext") then
            cur = next_
        else if __kotlin_isInstanceOf(next_, "CoroutineContext_Element") then
            return next_.get_Key_k_(next_.__get_key()) <> invalid
        else if true then
            return true
        end if
    end while
end function

function CombinedContext_plus_CoroutineContext_k_(context as Object) as Object
    __when_tmp6 = invalid
    if __kotlin_identityEquals(context, EmptyCoroutineContext_getInstance()) then
        __when_tmp6 = m
    else if true then
        __when_tmp6 = context.fold_AnyN_Function2Element_k_(m, CoroutineContext_plus_lambda_create_k_())
    end if
    return __when_tmp6

end function

function CombinedContext___get_left_k_() as Object
    return m.left
end function

function CombinedContext___get_element_k_() as Object
    return m.element
end function

function CombinedContext_toString_lambda_create_k_() as Object
    this = {}
    this.__type = "CombinedContext_toString_lambda"
    this.__proto = ["CombinedContext_toString_lambda", "Function2", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_AnyN_k_ = CombinedContext_toString_lambda_invoke_AnyN_AnyN_k_
    return this
end function

function CombinedContext_toString_lambda_invoke_AnyN_AnyN_k_(acc as String, element as Object) as String
    __when_tmp1 = invalid
    if isEmpty_rStr_k_(acc) then
        __when_tmp1 = toString_AnyN_k_(element)
    else if true then
        __when_tmp1 = ((acc + ", ") + element.toString())
    end if
    return __when_tmp1

end function

function CoroutineContext_plus_lambda_create_k_() as Object
    this = {}
    this.__type = "CoroutineContext_plus_lambda"
    this.__proto = ["CoroutineContext_plus_lambda", "Function2", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_AnyN_k_ = CoroutineContext_plus_lambda_invoke_AnyN_AnyN_k_
    return this
end function

function CoroutineContext_plus_lambda_invoke_AnyN_AnyN_k_(acc as Object, element as Object) as Object
    removed = acc.minusKey_Key_k_(element.__get_key())
    __when_tmp5 = invalid
    if __kotlin_identityEquals(removed, EmptyCoroutineContext_getInstance()) then
        __when_tmp5 = element
    else if true then
        interceptor = removed.get_Key_k_(ContinuationInterceptor_Key_getInstance())
        __when_tmp4 = invalid
        if interceptor = invalid then
            __when_tmp4 = CombinedContext_create_CoroutineContext_Element_k_(removed, element)
        else if true then
            left = removed.minusKey_Key_k_(ContinuationInterceptor_Key_getInstance())
            __when_tmp3 = invalid
            if __kotlin_identityEquals(left, EmptyCoroutineContext_getInstance()) then
                __when_tmp3 = CombinedContext_create_CoroutineContext_Element_k_(element, interceptor)
            else if true then
                __when_tmp3 = CombinedContext_create_CoroutineContext_Element_k_(CombinedContext_create_CoroutineContext_Element_k_(left, element), interceptor)
            end if
            __when_tmp4 = __when_tmp3
        end if
        __when_tmp5 = __when_tmp4
    end if
    return __when_tmp5

end function
