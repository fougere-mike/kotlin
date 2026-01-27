function InterceptedCoroutine_create_k_() as Object
    this = {}
    this.__type = "InterceptedCoroutine"
    this.__proto = ["InterceptedCoroutine", "Continuation"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.intercepted_k_ = InterceptedCoroutine_intercepted_k_
    this.releaseIntercepted_k_ = InterceptedCoroutine_releaseIntercepted_k_
    this.__get__intercepted = InterceptedCoroutine___get__intercepted_k_
    this.__set__intercepted = InterceptedCoroutine___set__intercepted_ContinuationAnyNN_k_
    this._intercepted = invalid
    return this
end function

function InterceptedCoroutine_intercepted_k_() as Object
    result = m.__get__intercepted()
    if result = invalid then
        tmp0_safe_receiver = m.__get_context().get_Key_k_(ContinuationInterceptor_Key_getInstance())
        __when_tmp0 = invalid
        if tmp0_safe_receiver = invalid then
            __when_tmp0 = invalid
        else if true then
            __when_tmp0 = tmp0_safe_receiver.interceptContinuation_Continuation_k_(m)
        end if
        tmp1_elvis_lhs = __when_tmp0
        __when_tmp1 = invalid
        if tmp1_elvis_lhs = invalid then
            __when_tmp1 = m
        else if true then
            __when_tmp1 = tmp1_elvis_lhs
        end if
        result = __when_tmp1
        m.__set__intercepted(result)
    end if
    return result
end function

sub InterceptedCoroutine_releaseIntercepted_k_()
    intercepted = m.__get__intercepted()
    if (intercepted <> invalid) and not __kotlin_identityEquals(intercepted, m) then
        m.__get_context().get_Key_k_(ContinuationInterceptor_Key_getInstance()).releaseInterceptedContinuation_Continuation_k_(intercepted)
    end if
    m.__set__intercepted(CompletedContinuation_getInstance())
end sub

function InterceptedCoroutine___get__intercepted_k_() as Dynamic
    return m._intercepted
end function

sub InterceptedCoroutine___set__intercepted_ContinuationAnyNN_k_(value as Dynamic)
    m._intercepted = value
end sub

function CoroutineImpl_create_ContinuationAnyNN_k_(resultContinuation as Dynamic) as Object
    this = InterceptedCoroutine_create_k_()
    this._super = {}
    this._super.resumeWith_Result_k_ = this.resumeWith_Result_k_
    this.__proto = ["CoroutineImpl", "Continuation", this.__proto]
    this.__type = "CoroutineImpl"
    this.resumeWith_Result_k_ = CoroutineImpl_resumeWith_Result_k_
    this.doResume_k_ = CoroutineImpl_doResume_k_
    this.create_Continuation_k_ = CoroutineImpl_create_Continuation_k_
    this.create_AnyN_Continuation_k_ = CoroutineImpl_create_AnyN_Continuation_k_
    this.__get_resultContinuation = CoroutineImpl___get_resultContinuation_k_
    this.__get_state = CoroutineImpl___get_state_k_
    this.__set_state = CoroutineImpl___set_state_I_k_
    this.__get_exceptionState = CoroutineImpl___get_exceptionState_k_
    this.__set_exceptionState = CoroutineImpl___set_exceptionState_I_k_
    this.__get_result = CoroutineImpl___get_result_k_
    this.__set_result = CoroutineImpl___set_result_AnyN_k_
    this.__get_exception = CoroutineImpl___get_exception_k_
    this.__set_exception = CoroutineImpl___set_exception_ThrowableN_k_
    this.__get_finallyPath = CoroutineImpl___get_finallyPath_k_
    this.__set_finallyPath = CoroutineImpl___set_finallyPath_ArrN_k_
    this.__get__context = CoroutineImpl___get__context_k_
    this.__get_context = CoroutineImpl___get_context_k_
    this.resultContinuation = resultContinuation
    this.state = 0
    this.exceptionState = 0
    this.result = invalid
    this.exception = invalid
    this.finallyPath = invalid
    tmp0_safe_receiver = this.__get_resultContinuation()
    __when_tmp2 = invalid
    if tmp0_safe_receiver = invalid then
        __when_tmp2 = invalid
    else if true then
        __when_tmp2 = tmp0_safe_receiver.__get_context()
    end if
    this._context = __when_tmp2
    return this
end function

sub CoroutineImpl_resumeWith_Result_k_(result as Object)
    current = m
    currentResult = result.getOrNull_k_()
    currentException = result.exceptionOrNull_k_()
    while true
        if currentException = invalid then
            current.__set_result(currentResult)
        else if true then
            current.__set_state(current.__get_exceptionState())
            current.__set_exception(currentException)
        end if
                try
            outcome = current.doResume_k_()
            if __kotlin_identityEquals(outcome, __get_COROUTINE_SUSPENDED_k_()) then
                return
            end if
            currentResult = outcome
            currentException = invalid
        catch e
            currentResult = invalid
            currentException = e
        end try
        current.releaseIntercepted_k_()
        completion = current.__get_resultContinuation()
        if __kotlin_isInstanceOf(completion, "CoroutineImpl") then
            current = completion
        else if true then
            if currentException <> invalid then
                completion.resumeWith_Result_k_(Result_Companion_getInstance().failure_Throwable_k_(currentException))
            else if true then
                completion.resumeWith_Result_k_(Result_Companion_getInstance().success_AnyN_k_(currentResult))
            end if
            return
        end if
    end while
end sub

function CoroutineImpl_doResume_k_() as Dynamic
end function

function CoroutineImpl_create_Continuation_k_(completion as Object) as Object
    throw UnsupportedOperationException_create_StrN_k_("create(Continuation) has not been overridden")
end function

function CoroutineImpl_create_AnyN_Continuation_k_(value as Dynamic, completion as Object) as Object
    throw UnsupportedOperationException_create_StrN_k_("create(Any?;Continuation) has not been overridden")
end function

function CoroutineImpl___get_resultContinuation_k_() as Dynamic
    return m.resultContinuation
end function

function CoroutineImpl___get_state_k_() as Integer
    return m.state
end function

sub CoroutineImpl___set_state_I_k_(value as Integer)
    m.state = value
end sub

function CoroutineImpl___get_exceptionState_k_() as Integer
    return m.exceptionState
end function

sub CoroutineImpl___set_exceptionState_I_k_(value as Integer)
    m.exceptionState = value
end sub

function CoroutineImpl___get_result_k_() as Dynamic
    return m.result
end function

sub CoroutineImpl___set_result_AnyN_k_(value as Dynamic)
    m.result = value
end sub

function CoroutineImpl___get_exception_k_() as Dynamic
    return m.exception
end function

sub CoroutineImpl___set_exception_ThrowableN_k_(value as Dynamic)
    m.exception = value
end sub

function CoroutineImpl___get_finallyPath_k_() as Dynamic
    return m.finallyPath
end function

sub CoroutineImpl___set_finallyPath_ArrN_k_(value as Dynamic)
    m.finallyPath = value
end sub

function CoroutineImpl___get__context_k_() as Dynamic
    return m._context
end function

function CoroutineImpl___get_context_k_() as Object
    return m.__get__context()
end function

function CompletedContinuation_create_k_() as Object
    this = {}
    this.__type = "CompletedContinuation"
    this.__proto = ["CompletedContinuation", "Continuation"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.resumeWith_Result_k_ = CompletedContinuation_resumeWith_Result_k_
    this.toString_k_ = CompletedContinuation_toString_k_
    this.toString = CompletedContinuation_toString_k_
    this.__get_context = CompletedContinuation___get_context_k_
    return this
end function

function CompletedContinuation_getInstance() as Object
    if GetGlobalAA().CompletedContinuation_instance = invalid then
        GetGlobalAA().CompletedContinuation_instance = CompletedContinuation_create_k_()
    end if
    return GetGlobalAA().CompletedContinuation_instance
end function

sub CompletedContinuation_resumeWith_Result_k_(result as Object)
    error_Any_k_("This continuation is already complete")
end sub

function CompletedContinuation_toString_k_() as String
    return "This continuation is already complete"
end function

function CompletedContinuation___get_context_k_() as Object
    return error_Any_k_("This continuation is already complete")
end function
