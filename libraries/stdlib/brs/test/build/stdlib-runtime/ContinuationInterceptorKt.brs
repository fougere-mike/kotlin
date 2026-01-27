function ContinuationInterceptor_interceptContinuation_Continuation_k_(continuation as Object) as Object
end function

sub ContinuationInterceptor_releaseInterceptedContinuation_Continuation_k_(continuation as Object)
end sub

function ContinuationInterceptor_Key_create_k_() as Object
    this = {}
    this.__type = "ContinuationInterceptor_Key"
    this.__proto = ["ContinuationInterceptor_Key", "CoroutineContext_Key"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    return this
end function

function ContinuationInterceptor_Key_getInstance() as Object
    if GetGlobalAA().ContinuationInterceptor_Key_instance = invalid then
        GetGlobalAA().ContinuationInterceptor_Key_instance = ContinuationInterceptor_Key_create_k_()
    end if
    return GetGlobalAA().ContinuationInterceptor_Key_instance
end function
