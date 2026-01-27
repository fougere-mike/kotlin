function delay_J_ContinuationV_k_(timeMillis as LongInteger, _completion as Object) as Object
    if timeMillis <= 0 then
        return invalid
    end if
    tmp_ret_2 = invalid
    while true
        tmp_ret_1 = invalid
        while true
            tmp0 = _completion
            tmp_ret_0 = invalid

            while true
                continuation = tmp0
                DelayTracker_Companion_getInstance().__get_current().register_J_Function0V_k_(timeMillis, delay_lambda_create_ContinuationV_k_(continuation))
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

function yield_ContinuationV_k_(_completion as Object) as Object
    tmp_ret_2 = invalid
    while true
        tmp_ret_1 = invalid
        while true
            tmp0 = _completion
            tmp_ret_0 = invalid

            while true
                continuation = tmp0
                interceptor = continuation.__get_context().get_Key_k_(ContinuationInterceptor_Key_getInstance())
                if interceptor <> invalid then
                    intercepted = interceptor.interceptContinuation_Continuation_k_(continuation)
                    resume_rContinuation_AnyN_k_(intercepted, invalid)
                else if true then
                    resume_rContinuation_AnyN_k_(continuation, invalid)
                end if
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

function processCoroutineDelays_k_() as Integer
    return DelayTracker_Companion_getInstance().__get_current().tick_k_()
end function

function hasPendingDelays_k_() as Boolean
    return DelayTracker_Companion_getInstance().__get_current().hasPendingDelays_k_()
end function

function delay_lambda_create_ContinuationV_k_(_continuation as Object) as Object
    this = {}
    this.__type = "delay_lambda"
    this.__proto = ["delay_lambda", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = delay_lambda_invoke_k_
    this._continuation = _continuation
    return this
end function

sub delay_lambda_invoke_k_()
    interceptor = m._continuation.__get_context().get_Key_k_(ContinuationInterceptor_Key_getInstance())
    if interceptor <> invalid then
        intercepted = interceptor.interceptContinuation_Continuation_k_(m._continuation)
        resume_rContinuation_AnyN_k_(intercepted, invalid)
    else if true then
        resume_rContinuation_AnyN_k_(m._continuation, invalid)
    end if
end sub
