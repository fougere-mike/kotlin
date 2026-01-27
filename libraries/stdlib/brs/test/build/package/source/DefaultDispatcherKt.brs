function DefaultDispatcher_create_k_() as Object
    this = CoroutineDispatcher_create_k_()
    this._super = {}
    this._super.isDispatchNeeded_CoroutineContext_k_ = this.isDispatchNeeded_CoroutineContext_k_
    this._super.dispatch_CoroutineContext_Runnable_k_ = this.dispatch_CoroutineContext_Runnable_k_
    this._super.toString_k_ = this.toString_k_
    this.__proto = ["DefaultDispatcher", this.__proto]
    this.__type = "DefaultDispatcher"
    this.isDispatchNeeded_CoroutineContext_k_ = DefaultDispatcher_isDispatchNeeded_CoroutineContext_k_
    this.dispatch_CoroutineContext_Runnable_k_ = DefaultDispatcher_dispatch_CoroutineContext_Runnable_k_
    this.toString_k_ = DefaultDispatcher_toString_k_
    this.toString = DefaultDispatcher_toString_k_
    return this
end function

function DefaultDispatcher_getInstance() as Object
    if GetGlobalAA().DefaultDispatcher_instance = invalid then
        GetGlobalAA().DefaultDispatcher_instance = DefaultDispatcher_create_k_()
    end if
    return GetGlobalAA().DefaultDispatcher_instance
end function

function DefaultDispatcher_isDispatchNeeded_CoroutineContext_k_(context as Object) as Boolean
    return true
end function

sub DefaultDispatcher_dispatch_CoroutineContext_Runnable_k_(context as Object, block as Object)
    CoroutineQueue_getInstance().enqueue_Runnable_k_(block)
end sub

function DefaultDispatcher_toString_k_() as String
    return "Dispatchers.Default"
end function
