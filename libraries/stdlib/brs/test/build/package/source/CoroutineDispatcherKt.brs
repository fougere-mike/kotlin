function CoroutineDispatcher_create_k_() as Object
    this = {}
    this.__type = "CoroutineDispatcher"
    this.__proto = ["CoroutineDispatcher", "ContinuationInterceptor", "CoroutineContext_Element", "CoroutineContext"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.dispatch_CoroutineContext_Runnable_k_ = CoroutineDispatcher_dispatch_CoroutineContext_Runnable_k_
    this.isDispatchNeeded_CoroutineContext_k_ = CoroutineDispatcher_isDispatchNeeded_CoroutineContext_k_
    this.interceptContinuation_Continuation_k_ = CoroutineDispatcher_interceptContinuation_Continuation_k_
    this.releaseInterceptedContinuation_Continuation_k_ = CoroutineDispatcher_releaseInterceptedContinuation_Continuation_k_
    this.__get_key = CoroutineDispatcher___get_key_k_
    return this
end function

sub CoroutineDispatcher_dispatch_CoroutineContext_Runnable_k_(context as Object, block as Object)
end sub

function CoroutineDispatcher_isDispatchNeeded_CoroutineContext_k_(context as Object) as Boolean
    return true
end function

function CoroutineDispatcher_interceptContinuation_Continuation_k_(continuation as Object) as Object
    return DispatchedContinuation_create_CoroutineDispatcher_Continuation_k_(m, continuation)
end function

sub CoroutineDispatcher_releaseInterceptedContinuation_Continuation_k_(continuation as Object)
end sub

function CoroutineDispatcher___get_key_k_() as Object
    return CoroutineDispatcher_Key_getInstance()
end function

function CoroutineDispatcher_Key_create_k_() as Object
    this = {}
    this.__type = "CoroutineDispatcher_Key"
    this.__proto = ["CoroutineDispatcher_Key", "CoroutineContext_Key"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    return this
end function

function CoroutineDispatcher_Key_getInstance() as Object
    if GetGlobalAA().CoroutineDispatcher_Key_instance = invalid then
        GetGlobalAA().CoroutineDispatcher_Key_instance = CoroutineDispatcher_Key_create_k_()
    end if
    return GetGlobalAA().CoroutineDispatcher_Key_instance
end function

function DispatchedContinuation_create_CoroutineDispatcher_Continuation_k_(dispatcher as Object, continuation as Object) as Object
    this = {}
    this.__type = "DispatchedContinuation"
    this.__proto = ["DispatchedContinuation", "Continuation"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.resumeWith_Result_k_ = DispatchedContinuation_resumeWith_Result_k_
    this.__get_dispatcher = DispatchedContinuation___get_dispatcher_k_
    this.__get_continuation = DispatchedContinuation___get_continuation_k_
    this.__get_context = DispatchedContinuation___get_context_k_
    this.dispatcher = dispatcher
    this.continuation = continuation
    return this
end function

sub DispatchedContinuation_resumeWith_Result_k_(result as Object)
    resumeBlock = DispatchedContinuation_resumeWith_lambda_create_DispatchedContinuation_Result_k_(m, result)
    if m.__get_dispatcher().isDispatchNeeded_CoroutineContext_k_(m.__get_context()) then
        m.__get_dispatcher().dispatch_CoroutineContext_Runnable_k_(m.__get_context(), resumeBlock)
    else if true then
        resumeBlock.run_k_()
    end if
end sub

function DispatchedContinuation___get_dispatcher_k_() as Object
    return m.dispatcher
end function

function DispatchedContinuation___get_continuation_k_() as Object
    return m.continuation
end function

function DispatchedContinuation___get_context_k_() as Object
    return m.__get_continuation().__get_context()
end function

function DispatchedContinuation_resumeWith_lambda_create_DispatchedContinuation_Result_k_(this_0 as Object, _result as Object) as Object
    this = {}
    this.__type = "DispatchedContinuation_resumeWith_lambda"
    this.__proto = ["DispatchedContinuation_resumeWith_lambda", "Runnable"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.run_k_ = DispatchedContinuation_resumeWith_lambda_run_k_
    this.this_0 = this_0
    this._result = _result
    return this
end function

sub DispatchedContinuation_resumeWith_lambda_run_k_()
    m.this_0.__get_continuation().resumeWith_Result_k_(m._result)
end sub
