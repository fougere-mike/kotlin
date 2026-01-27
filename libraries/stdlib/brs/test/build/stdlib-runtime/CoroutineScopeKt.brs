function CoroutineScope___get_coroutineContext_k_() as Object
end function

function CoroutineScope_CoroutineContext_k_(context as Object) as Object
    return CoroutineScopeImpl_create_CoroutineContext_k_(context)
end function

function CoroutineScopeImpl_create_CoroutineContext_k_(coroutineContext as Object) as Object
    this = {}
    this.__type = "CoroutineScopeImpl"
    this.__proto = ["CoroutineScopeImpl", "CoroutineScope"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.__get_coroutineContext = CoroutineScopeImpl___get_coroutineContext_k_
    this.coroutineContext = coroutineContext
    return this
end function

function CoroutineScopeImpl___get_coroutineContext_k_() as Object
    return m.coroutineContext
end function

function coroutineScope_SuspendFunction1CoroutineScope_Continuation_k_(block as Object, _completion as Object) as Dynamic
    scope = CoroutineScope_CoroutineContext_k_(EmptyCoroutineContext_getInstance())
    return block.invoke_AnyN_Continuation_k_(scope, _completion)
end function
