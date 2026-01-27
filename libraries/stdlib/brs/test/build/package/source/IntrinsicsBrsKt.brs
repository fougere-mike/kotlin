function createCoroutineUnintercepted_rSuspendFunction0_Continuation_k_(m as Object, completion as Object) as Object
    impl = m
    return impl.create_Continuation_k_(completion)
end function

function createCoroutineUnintercepted_rSuspendFunction1_AnyN_Continuation_k_(m as Object, receiver as Dynamic, completion as Object) as Object
    impl = m
    return impl.create_AnyN_Continuation_k_(receiver, completion)
end function

function intercepted_rContinuation_k_(m as Object) as Object
    tmp0_safe_receiver = (function(__kotlin_isInstanceOf, m)
        if __kotlin_isInstanceOf(m, "InterceptedCoroutine") then return m else return invalid
    end function)(__kotlin_isInstanceOf, m)
    __when_tmp0 = invalid
    if tmp0_safe_receiver = invalid then
        __when_tmp0 = invalid
    else if true then
        __when_tmp0 = tmp0_safe_receiver.intercepted_k_()
    end if
    tmp1_elvis_lhs = (function(__kotlin_isInstanceOf, __when_tmp0)
        if __kotlin_isInstanceOf(__when_tmp0, "Continuation") then return __when_tmp0 else return invalid
    end function)(__kotlin_isInstanceOf, __when_tmp0)
    __when_tmp1 = invalid
    if tmp1_elvis_lhs = invalid then
        __when_tmp1 = m
    else if true then
        __when_tmp1 = tmp1_elvis_lhs
    end if
    return __when_tmp1

end function

function startCoroutineUninterceptedOrReturn_rSuspendFunction0_Continuation_k_(m as Object, completion as Object) as Dynamic
    impl = m
    impl.create_Continuation_k_(completion).resumeWith_Result_k_(Result_Companion_getInstance().success_AnyN_k_(invalid))
    return __get_COROUTINE_SUSPENDED_k_()
end function

function startCoroutineUninterceptedOrReturn_rSuspendFunction1_AnyN_Continuation_k_(m as Object, receiver as Dynamic, completion as Object) as Dynamic
    impl = m
    impl.create_AnyN_Continuation_k_(receiver, completion).resumeWith_Result_k_(Result_Companion_getInstance().success_AnyN_k_(invalid))
    return __get_COROUTINE_SUSPENDED_k_()
end function

function getContinuation_k_() as Object
    throw UnsupportedOperationException_create_StrN_k_("getContinuation is a compiler intrinsic")
end function

function getCoroutineContext_k_() as Object
    throw UnsupportedOperationException_create_StrN_k_("getCoroutineContext is a compiler intrinsic")
end function

function returnIfSuspended_AnyN_Continuation_k_(result as Dynamic, _completion as Object) as Dynamic
    return result
end function

function suspendCoroutineUninterceptedOrReturnBRS_Function1ContinuationAnyN_Continuation_k_(block as Object, _completion as Object) as Dynamic
    return returnIfSuspended_AnyN_Continuation_k_(block.invoke_AnyN_k_(_completion), _completion)
end function

function suspendCoroutineUninterceptedOrReturn_Function1ContinuationAnyN_Continuation_k_(block as Object, _completion as Object) as Dynamic
    tmp_ret_0 = invalid
    while true
        tmp_ret_0 = returnIfSuspended_AnyN_Continuation_k_(block.invoke_AnyN_k_(_completion), _completion)
        exit while
    end while
    return tmp_ret_0

end function
