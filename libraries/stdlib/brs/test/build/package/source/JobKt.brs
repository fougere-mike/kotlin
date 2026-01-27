sub Job_cancel_ThrowableN_k_(cause = invalid)
    if cause = invalid then
        cause = invalid
    end if
end sub

function Job_join_ContinuationV_k_(_completion as Object) as Object
end function

function Job_start_k_() as Boolean
end function

function Job___get_isActive_k_() as Boolean
end function

function Job___get_isCompleted_k_() as Boolean
end function

function Job___get_isCancelled_k_() as Boolean
end function

function Job_Key_create_k_() as Object
    this = {}
    this.__type = "Job_Key"
    this.__proto = ["Job_Key", "CoroutineContext_Key"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    return this
end function

function Job_Key_getInstance() as Object
    if GetGlobalAA().Job_Key_instance = invalid then
        GetGlobalAA().Job_Key_instance = Job_Key_create_k_()
    end if
    return GetGlobalAA().Job_Key_instance
end function

function CompletableJob_complete_k_() as Boolean
end function

function CompletableJob_completeExceptionally_Throwable_k_(exception as Object) as Boolean
end function

function Job_JobN_k_(parent = invalid) as Object
    if parent = invalid then
        parent = invalid
    end if
    return JobImpl_create_JobN_k_(parent)
end function

function JobImpl_create_JobN_k_(parent = invalid) as Object
    this = {}
    this.__type = "JobImpl"
    this.__proto = ["JobImpl", "CompletableJob", "Job", "CoroutineContext_Element", "CoroutineContext"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.cancel_ThrowableN_k_ = JobImpl_cancel_ThrowableN_k_
    this.join_ContinuationV_k_ = JobImpl_join_ContinuationV_k_
    this.start_k_ = JobImpl_start_k_
    this.complete_k_ = JobImpl_complete_k_
    this.completeExceptionally_Throwable_k_ = JobImpl_completeExceptionally_Throwable_k_
    this.get_Key_k_ = JobImpl_get_Key_k_
    this.fold_AnyN_Function2Element_k_ = JobImpl_fold_AnyN_Function2Element_k_
    this.minusKey_Key_k_ = JobImpl_minusKey_Key_k_
    this.plus_CoroutineContext_k_ = JobImpl_plus_CoroutineContext_k_
    this.__get_parent = JobImpl___get_parent_k_
    this.__get__isActive = JobImpl___get__isActive_k_
    this.__set__isActive = JobImpl___set__isActive_Z_k_
    this.__get__isCompleted = JobImpl___get__isCompleted_k_
    this.__set__isCompleted = JobImpl___set__isCompleted_Z_k_
    this.__get__isCancelled = JobImpl___get__isCancelled_k_
    this.__set__isCancelled = JobImpl___set__isCancelled_Z_k_
    this.__get_completionException = JobImpl___get_completionException_k_
    this.__set_completionException = JobImpl___set_completionException_ThrowableN_k_
    this.__get_key = JobImpl___get_key_k_
    this.__get_isActive = JobImpl___get_isActive_k_
    this.__get_isCompleted = JobImpl___get_isCompleted_k_
    this.__get_isCancelled = JobImpl___get_isCancelled_k_
    this.parent = parent
    this._isActive = true
    this._isCompleted = false
    this._isCancelled = false
    this.completionException = invalid
    return this
end function

sub JobImpl_cancel_ThrowableN_k_(cause as Dynamic)
    if m.__get__isCompleted() or m.__get__isCancelled() then
        return
    end if
    m.__set__isCancelled(true)
    m.__set__isActive(false)
    m.__set_completionException(cause)
end sub

function JobImpl_join_ContinuationV_k_(_completion as Object) as Object
    while not m.__get__isCompleted() and not m.__get__isCancelled()
    end while
    return invalid
end function

function JobImpl_start_k_() as Boolean
    if (m.__get__isActive() or m.__get__isCompleted()) or m.__get__isCancelled() then
        return false
    end if
    m.__set__isActive(true)
    return true
end function

function JobImpl_complete_k_() as Boolean
    if m.__get__isCompleted() or m.__get__isCancelled() then
        return false
    end if
    m.__set__isCompleted(true)
    m.__set__isActive(false)
    return true
end function

function JobImpl_completeExceptionally_Throwable_k_(exception as Object) as Boolean
    if m.__get__isCompleted() or m.__get__isCancelled() then
        return false
    end if
    m.__set__isCompleted(true)
    m.__set__isCancelled(true)
    m.__set__isActive(false)
    m.__set_completionException(exception)
    return true
end function

function JobImpl_get_Key_k_(key as Object) as Dynamic
    __when_tmp0 = invalid
    if brsStructuralEquals_AnyN_AnyN_k_(m.__get_key(), key) then
        __when_tmp0 = m
    else if true then
        __when_tmp0 = invalid
    end if
    return __when_tmp0

end function

function JobImpl_fold_AnyN_Function2Element_k_(initial as Dynamic, operation as Object) as Dynamic
    return operation.invoke_AnyN_AnyN_k_(initial, m)
end function

function JobImpl_minusKey_Key_k_(key as Object) as Object
    __when_tmp1 = invalid
    if brsStructuralEquals_AnyN_AnyN_k_(m.__get_key(), key) then
        __when_tmp1 = EmptyCoroutineContext_getInstance()
    else if true then
        __when_tmp1 = m
    end if
    return __when_tmp1

end function

function JobImpl_plus_CoroutineContext_k_(context as Object) as Object
    __when_tmp5 = invalid
    if __kotlin_identityEquals(context, EmptyCoroutineContext_getInstance()) then
        __when_tmp5 = m
    else if true then
        __when_tmp5 = context.fold_AnyN_Function2Element_k_(m, CoroutineContext_plus_lambda_2_create_k_())
    end if
    return __when_tmp5

end function

function JobImpl___get_parent_k_() as Dynamic
    return m.parent
end function

function JobImpl___get__isActive_k_() as Boolean
    return m._isActive
end function

sub JobImpl___set__isActive_Z_k_(value as Boolean)
    m._isActive = value
end sub

function JobImpl___get__isCompleted_k_() as Boolean
    return m._isCompleted
end function

sub JobImpl___set__isCompleted_Z_k_(value as Boolean)
    m._isCompleted = value
end sub

function JobImpl___get__isCancelled_k_() as Boolean
    return m._isCancelled
end function

sub JobImpl___set__isCancelled_Z_k_(value as Boolean)
    m._isCancelled = value
end sub

function JobImpl___get_completionException_k_() as Dynamic
    return m.completionException
end function

sub JobImpl___set_completionException_ThrowableN_k_(value as Dynamic)
    m.completionException = value
end sub

function JobImpl___get_key_k_() as Object
    return Job_Key_getInstance()
end function

function JobImpl___get_isActive_k_() as Boolean
    return (m.__get__isActive() and not m.__get__isCompleted()) and not m.__get__isCancelled()
end function

function JobImpl___get_isCompleted_k_() as Boolean
    return m.__get__isCompleted()
end function

function JobImpl___get_isCancelled_k_() as Boolean
    return m.__get__isCancelled()
end function

function Deferred_await_Continuation_k_(_completion as Object) as Dynamic
end function

function Deferred_getCompleted_k_() as Dynamic
end function

function CompletableDeferred_complete_AnyN_k_(value as Dynamic) as Boolean
end function

function CompletableDeferred_JobN_k_(parent = invalid) as Object
    if parent = invalid then
        parent = invalid
    end if
    return CompletableDeferredImpl_create_JobN_k_(parent)
end function

function CompletableDeferredImpl_create_JobN_k_(parent = invalid) as Object
    this = {}
    this.__type = "CompletableDeferredImpl"
    this.__proto = ["CompletableDeferredImpl", "CompletableDeferred", "Deferred", "Job", "CoroutineContext_Element", "CoroutineContext", "CompletableJob"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.cancel_ThrowableN_k_ = CompletableDeferredImpl_cancel_ThrowableN_k_
    this.join_ContinuationV_k_ = CompletableDeferredImpl_join_ContinuationV_k_
    this.start_k_ = CompletableDeferredImpl_start_k_
    this.complete_k_ = CompletableDeferredImpl_complete_k_
    this.completeExceptionally_Throwable_k_ = CompletableDeferredImpl_completeExceptionally_Throwable_k_
    this.complete_AnyN_k_ = CompletableDeferredImpl_complete_AnyN_k_
    this.await_Continuation_k_ = CompletableDeferredImpl_await_Continuation_k_
    this.getCompleted_k_ = CompletableDeferredImpl_getCompleted_k_
    this.get_Key_k_ = CompletableDeferredImpl_get_Key_k_
    this.fold_AnyN_Function2Element_k_ = CompletableDeferredImpl_fold_AnyN_Function2Element_k_
    this.minusKey_Key_k_ = CompletableDeferredImpl_minusKey_Key_k_
    this.plus_CoroutineContext_k_ = CompletableDeferredImpl_plus_CoroutineContext_k_
    this.__get_job = CompletableDeferredImpl___get_job_k_
    this.__get__value = CompletableDeferredImpl___get__value_k_
    this.__set__value = CompletableDeferredImpl___set__value_AnyN_k_
    this.__get__exception = CompletableDeferredImpl___get__exception_k_
    this.__set__exception = CompletableDeferredImpl___set__exception_ThrowableN_k_
    this.__get_key = CompletableDeferredImpl___get_key_k_
    this.__get_isActive = CompletableDeferredImpl___get_isActive_k_
    this.__get_isCompleted = CompletableDeferredImpl___get_isCompleted_k_
    this.__get_isCancelled = CompletableDeferredImpl___get_isCancelled_k_
    this.job = JobImpl_create_JobN_k_(parent)
    this._value = invalid
    this._exception = invalid
    return this
end function

sub CompletableDeferredImpl_cancel_ThrowableN_k_(cause as Dynamic)
    m.__get_job().cancel_ThrowableN_k_(cause)
    return

end sub

function CompletableDeferredImpl_join_ContinuationV_k_(_completion as Object) as Object
    return m.__get_job().join_ContinuationV_k_(_completion)
end function

function CompletableDeferredImpl_start_k_() as Boolean
    return m.__get_job().start_k_()
end function

function CompletableDeferredImpl_complete_k_() as Boolean
    return m.__get_job().complete_k_()
end function

function CompletableDeferredImpl_completeExceptionally_Throwable_k_(exception as Object) as Boolean
    m.__set__exception(exception)
    return m.__get_job().completeExceptionally_Throwable_k_(exception)
end function

function CompletableDeferredImpl_complete_AnyN_k_(value as Dynamic) as Boolean
    if m.__get_job().__get_isCompleted() or m.__get_job().__get_isCancelled() then
        return false
    end if
    m.__set__value(value)
    return m.__get_job().complete_k_()
end function

function CompletableDeferredImpl_await_Continuation_k_(_completion as Object) as Dynamic
    m.__get_job().join_ContinuationV_k_(_completion)
    tmp0_safe_receiver = m.__get__exception()
    if tmp0_safe_receiver = invalid then

    else if true then
        tmp0 = tmp0_safe_receiver
        tmp_ret_0 = invalid
        while true
            this = tmp0
            tmp0 = this
            it = tmp0
            throw it

            tmp_ret_0 = invalid
            exit while
        end while
    end if

    return m.__get__value()
end function

function CompletableDeferredImpl_getCompleted_k_() as Dynamic
    if not m.__get_job().__get_isCompleted() then
        throw IllegalStateException_create_StrN_k_("Deferred has not completed yet")
    end if
    tmp0_safe_receiver = m.__get__exception()
    if tmp0_safe_receiver = invalid then

    else if true then
        tmp0 = tmp0_safe_receiver
        tmp_ret_0 = invalid
        while true
            this = tmp0
            tmp0 = this
            it = tmp0
            throw it

            tmp_ret_0 = invalid
            exit while
        end while
    end if

    return m.__get__value()
end function

function CompletableDeferredImpl_get_Key_k_(key as Object) as Dynamic
    __when_tmp6 = invalid
    if brsStructuralEquals_AnyN_AnyN_k_(m.__get_key(), key) then
        __when_tmp6 = m
    else if true then
        __when_tmp6 = invalid
    end if
    return __when_tmp6

end function

function CompletableDeferredImpl_fold_AnyN_Function2Element_k_(initial as Dynamic, operation as Object) as Dynamic
    return operation.invoke_AnyN_AnyN_k_(initial, m)
end function

function CompletableDeferredImpl_minusKey_Key_k_(key as Object) as Object
    __when_tmp7 = invalid
    if brsStructuralEquals_AnyN_AnyN_k_(m.__get_key(), key) then
        __when_tmp7 = EmptyCoroutineContext_getInstance()
    else if true then
        __when_tmp7 = m
    end if
    return __when_tmp7

end function

function CompletableDeferredImpl_plus_CoroutineContext_k_(context as Object) as Object
    __when_tmp11 = invalid
    if __kotlin_identityEquals(context, EmptyCoroutineContext_getInstance()) then
        __when_tmp11 = m
    else if true then
        __when_tmp11 = context.fold_AnyN_Function2Element_k_(m, CoroutineContext_plus_lambda_3_create_k_())
    end if
    return __when_tmp11

end function

function CompletableDeferredImpl___get_job_k_() as Object
    return m.job
end function

function CompletableDeferredImpl___get__value_k_() as Dynamic
    return m._value
end function

sub CompletableDeferredImpl___set__value_AnyN_k_(value as Dynamic)
    m._value = value
end sub

function CompletableDeferredImpl___get__exception_k_() as Dynamic
    return m._exception
end function

sub CompletableDeferredImpl___set__exception_ThrowableN_k_(value as Dynamic)
    m._exception = value
end sub

function CompletableDeferredImpl___get_key_k_() as Object
    return Job_Key_getInstance()
end function

function CompletableDeferredImpl___get_isActive_k_() as Boolean
    return m.__get_job().__get_isActive()
end function

function CompletableDeferredImpl___get_isCompleted_k_() as Boolean
    return m.__get_job().__get_isCompleted()
end function

function CompletableDeferredImpl___get_isCancelled_k_() as Boolean
    return m.__get_job().__get_isCancelled()
end function

function CoroutineContext_plus_lambda_2_create_k_() as Object
    this = {}
    this.__type = "CoroutineContext_plus_lambda_2"
    this.__proto = ["CoroutineContext_plus_lambda_2", "Function2", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_AnyN_k_ = CoroutineContext_plus_lambda_2_invoke_AnyN_AnyN_k_
    return this
end function

function CoroutineContext_plus_lambda_2_invoke_AnyN_AnyN_k_(acc as Object, element as Object) as Object
    removed = acc.minusKey_Key_k_(element.__get_key())
    __when_tmp4 = invalid
    if __kotlin_identityEquals(removed, EmptyCoroutineContext_getInstance()) then
        __when_tmp4 = element
    else if true then
        interceptor = removed.get_Key_k_(ContinuationInterceptor_Key_getInstance())
        __when_tmp3 = invalid
        if interceptor = invalid then
            __when_tmp3 = CombinedContext_create_CoroutineContext_Element_k_(removed, element)
        else if true then
            left = removed.minusKey_Key_k_(ContinuationInterceptor_Key_getInstance())
            __when_tmp2 = invalid
            if __kotlin_identityEquals(left, EmptyCoroutineContext_getInstance()) then
                __when_tmp2 = CombinedContext_create_CoroutineContext_Element_k_(element, interceptor)
            else if true then
                __when_tmp2 = CombinedContext_create_CoroutineContext_Element_k_(CombinedContext_create_CoroutineContext_Element_k_(left, element), interceptor)
            end if
            __when_tmp3 = __when_tmp2
        end if
        __when_tmp4 = __when_tmp3
    end if
    return __when_tmp4

end function

function CoroutineContext_plus_lambda_3_create_k_() as Object
    this = {}
    this.__type = "CoroutineContext_plus_lambda_3"
    this.__proto = ["CoroutineContext_plus_lambda_3", "Function2", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_AnyN_k_ = CoroutineContext_plus_lambda_3_invoke_AnyN_AnyN_k_
    return this
end function

function CoroutineContext_plus_lambda_3_invoke_AnyN_AnyN_k_(acc as Object, element as Object) as Object
    removed = acc.minusKey_Key_k_(element.__get_key())
    __when_tmp10 = invalid
    if __kotlin_identityEquals(removed, EmptyCoroutineContext_getInstance()) then
        __when_tmp10 = element
    else if true then
        interceptor = removed.get_Key_k_(ContinuationInterceptor_Key_getInstance())
        __when_tmp9 = invalid
        if interceptor = invalid then
            __when_tmp9 = CombinedContext_create_CoroutineContext_Element_k_(removed, element)
        else if true then
            left = removed.minusKey_Key_k_(ContinuationInterceptor_Key_getInstance())
            __when_tmp8 = invalid
            if __kotlin_identityEquals(left, EmptyCoroutineContext_getInstance()) then
                __when_tmp8 = CombinedContext_create_CoroutineContext_Element_k_(element, interceptor)
            else if true then
                __when_tmp8 = CombinedContext_create_CoroutineContext_Element_k_(CombinedContext_create_CoroutineContext_Element_k_(left, element), interceptor)
            end if
            __when_tmp9 = __when_tmp8
        end if
        __when_tmp10 = __when_tmp9
    end if
    return __when_tmp10

end function
