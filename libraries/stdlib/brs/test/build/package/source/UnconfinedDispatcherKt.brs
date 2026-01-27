function UnconfinedDispatcher_create_k_() as Object
    this = CoroutineDispatcher_create_k_()
    this._super = {}
    this._super.isDispatchNeeded_CoroutineContext_k_ = this.isDispatchNeeded_CoroutineContext_k_
    this._super.dispatch_CoroutineContext_Runnable_k_ = this.dispatch_CoroutineContext_Runnable_k_
    this._super.toString_k_ = this.toString_k_
    this.__proto = ["UnconfinedDispatcher", this.__proto]
    this.__type = "UnconfinedDispatcher"
    this.isDispatchNeeded_CoroutineContext_k_ = UnconfinedDispatcher_isDispatchNeeded_CoroutineContext_k_
    this.dispatch_CoroutineContext_Runnable_k_ = UnconfinedDispatcher_dispatch_CoroutineContext_Runnable_k_
    this.toString_k_ = UnconfinedDispatcher_toString_k_
    this.toString = UnconfinedDispatcher_toString_k_
    return this
end function

function UnconfinedDispatcher_getInstance() as Object
    if GetGlobalAA().UnconfinedDispatcher_instance = invalid then
        GetGlobalAA().UnconfinedDispatcher_instance = UnconfinedDispatcher_create_k_()
    end if
    return GetGlobalAA().UnconfinedDispatcher_instance
end function

function UnconfinedDispatcher_isDispatchNeeded_CoroutineContext_k_(context as Object) as Boolean
    return false
end function

sub UnconfinedDispatcher_dispatch_CoroutineContext_Runnable_k_(context as Object, block as Object)
    block.run_k_()
end sub

function UnconfinedDispatcher_toString_k_() as String
    return "Dispatchers.Unconfined"
end function
