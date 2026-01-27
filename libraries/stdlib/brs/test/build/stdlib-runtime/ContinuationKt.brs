sub Continuation_resumeWith_Result_k_(result as Object)
end sub

function Continuation___get_context_k_() as Object
end function

sub resume_rContinuation_AnyN_k_(m as Object, value as Dynamic)
    m.resumeWith_Result_k_(Result_Companion_getInstance().success_AnyN_k_(value))
    return

end sub

sub resumeWithException_rContinuation_Throwable_k_(m as Object, exception as Object)
    m.resumeWith_Result_k_(Result_Companion_getInstance().failure_Throwable_k_(exception))
    return

end sub

function suspendCoroutine_Function1ContinuationV_Continuation_k_(block as Object, _completion as Object) as Dynamic
    tmp_ret_2 = invalid
    while true
        tmp_ret_1 = invalid
        while true
            tmp0 = _completion
            tmp_ret_0 = invalid

            while true
                continuation = tmp0
                tmp0_safe_receiver = continuation.__get_context().get_Key_k_(ContinuationInterceptor_Key_getInstance())
                __when_tmp0 = invalid
                if tmp0_safe_receiver = invalid then
                    __when_tmp0 = invalid
                else if true then
                    __when_tmp0 = tmp0_safe_receiver.interceptContinuation_Continuation_k_(continuation)
                end if
                tmp1_elvis_lhs = __when_tmp0
                __when_tmp1 = invalid
                if tmp1_elvis_lhs = invalid then
                    __when_tmp1 = continuation
                else if true then
                    __when_tmp1 = tmp1_elvis_lhs
                end if
                intercepted = __when_tmp1
                block.invoke_AnyN_k_(intercepted)
                tmp_ret_0 = __get_COROUTINE_SUSPENDED_k_()
                exit while
            end while
            tmp_ret_1 = returnIfSuspended_AnyN_Continuation_k_(tmp_ret_0, _completion)
            exit while
        end while
        tmp_ret_2 = tmp_ret_1
        exit while
    end while
    return tmp_ret_2

end function
