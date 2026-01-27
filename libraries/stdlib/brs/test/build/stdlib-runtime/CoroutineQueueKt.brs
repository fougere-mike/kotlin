function CoroutineQueue_create_k_() as Object
    this = {}
    this.__type = "CoroutineQueue"
    this.__proto = ["CoroutineQueue"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.enqueue_Runnable_k_ = CoroutineQueue_enqueue_Runnable_k_
    this.processAll_k_ = CoroutineQueue_processAll_k_
    this.isEmpty_k_ = CoroutineQueue_isEmpty_k_
    this.size_k_ = CoroutineQueue_size_k_
    this.__get_queue = CoroutineQueue___get_queue_k_
    this.queue = mutableListOf_k_()
    return this
end function

function CoroutineQueue_getInstance() as Object
    if GetGlobalAA().CoroutineQueue_instance = invalid then
        GetGlobalAA().CoroutineQueue_instance = CoroutineQueue_create_k_()
    end if
    return GetGlobalAA().CoroutineQueue_instance
end function

sub CoroutineQueue_enqueue_Runnable_k_(block as Object)
    CoroutineQueue_getInstance().__get_queue().add_AnyN_k_(block)
end sub

function CoroutineQueue_processAll_k_() as Integer
    count = 0
    tmp0 = CoroutineQueue_getInstance().__get_queue()
    tmp_ret_0 = invalid

    while true
        this = tmp0
        tmp_ret_0 = not this.isEmpty_k_()
        exit while
    end while
    while true
        if not tmp_ret_0 then
            exit while
        end if
        block = CoroutineQueue_getInstance().__get_queue().removeAt_I_k_(0)
        block.run_k_()
        count = (count + 1)
        tmp0 = CoroutineQueue_getInstance().__get_queue()
        tmp_ret_0 = invalid

        while true
            this = tmp0
            tmp_ret_0 = not this.isEmpty_k_()
            exit while
        end while
    end while

    return count
end function

function CoroutineQueue_isEmpty_k_() as Boolean
    return CoroutineQueue_getInstance().__get_queue().isEmpty_k_()
end function

function CoroutineQueue_size_k_() as Integer
    return CoroutineQueue_getInstance().__get_queue().__get_size()
end function

function CoroutineQueue___get_queue_k_() as Object
    return m.queue
end function

function processCoroutineQueue_k_() as Integer
    return CoroutineQueue_getInstance().processAll_k_()
end function

function hasCoroutineWork_k_() as Boolean
    return not CoroutineQueue_getInstance().isEmpty_k_()
end function
