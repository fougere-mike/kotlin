function launch_rCoroutineScope_CoroutineContext_SuspendFunction1CoroutineScopeV_k_(m as Object, context = EmptyCoroutineContext_getInstance(), block = invalid) as Object
    if context = invalid then
        context = EmptyCoroutineContext_getInstance()
    end if
    newContext = m.__get_coroutineContext().plus_CoroutineContext_k_(context)
    job = Job_JobN_k_(invalid)
    newScope = CoroutineScope_CoroutineContext_k_(newContext.plus_CoroutineContext_k_(job))
    continuation = LaunchContinuation_create_CoroutineScope_SuspendFunction1CoroutineScopeV_CompletableJob_k_(newScope, block, job)
    continuation.start_k_()
    return job
end function

function async_rCoroutineScope_CoroutineContext_SuspendFunction1CoroutineScope_k_(m as Object, context = EmptyCoroutineContext_getInstance(), block = invalid) as Object
    if context = invalid then
        context = EmptyCoroutineContext_getInstance()
    end if
    newContext = m.__get_coroutineContext().plus_CoroutineContext_k_(context)
    deferred = CompletableDeferred_JobN_k_(invalid)
    newScope = CoroutineScope_CoroutineContext_k_(newContext.plus_CoroutineContext_k_(deferred))
    continuation = AsyncContinuation_create_CoroutineScope_SuspendFunction1CoroutineScope_CompletableDeferred_k_(newScope, block, deferred)
    continuation.start_k_()
    return deferred
end function

function runBlocking_CoroutineContext_SuspendFunction1CoroutineScope_k_(context = EmptyCoroutineContext_getInstance(), block = invalid) as Dynamic
    if context = invalid then
        context = EmptyCoroutineContext_getInstance()
    end if
    job = Job_JobN_k_(invalid)
    scope = CoroutineScope_CoroutineContext_k_(context.plus_CoroutineContext_k_(job))
    deferred = CompletableDeferred_JobN_k_(invalid)
    continuation = RunBlockingContinuation_create_CoroutineScope_SuspendFunction1CoroutineScope_CompletableDeferred_k_(scope, block, deferred)
    continuation.start_k_()
    while not deferred.__get_isCompleted() and not deferred.__get_isCancelled()
        processCoroutineQueue_k_()
        processCoroutineDelays_k_()
    end while
    return deferred.getCompleted_k_()
end function

function LaunchContinuation_create_CoroutineScope_SuspendFunction1CoroutineScopeV_CompletableJob_k_(scope as Object, block as Object, job as Object) as Object
    this = {}
    this.__type = "LaunchContinuation"
    this.__proto = ["LaunchContinuation", "Continuation"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.start_k_ = LaunchContinuation_start_k_
    this.resumeWith_Result_k_ = LaunchContinuation_resumeWith_Result_k_
    this.__get_scope = LaunchContinuation___get_scope_k_
    this.__get_block = LaunchContinuation___get_block_k_
    this.__get_job = LaunchContinuation___get_job_k_
    this.__get_context = LaunchContinuation___get_context_k_
    this.scope = scope
    this.block = block
    this.job = job
    return this
end function

sub LaunchContinuation_start_k_()
        try
        startCoroutine_rSuspendFunction1_AnyN_Continuation_k_(m.__get_block(), m.__get_scope(), m)
    catch e
        m.__get_job().completeExceptionally_Throwable_k_(e)
    end try
end sub

sub LaunchContinuation_resumeWith_Result_k_(result as Object)
    exception = result.exceptionOrNull_k_()
    if exception <> invalid then
        m.__get_job().completeExceptionally_Throwable_k_(exception)
    else if true then
        m.__get_job().complete_k_()
    end if
end sub

function LaunchContinuation___get_scope_k_() as Object
    return m.scope
end function

function LaunchContinuation___get_block_k_() as Object
    return m.block
end function

function LaunchContinuation___get_job_k_() as Object
    return m.job
end function

function LaunchContinuation___get_context_k_() as Object
    return m.__get_scope().__get_coroutineContext()
end function

function AsyncContinuation_create_CoroutineScope_SuspendFunction1CoroutineScope_CompletableDeferred_k_(scope as Object, block as Object, deferred as Object) as Object
    this = {}
    this.__type = "AsyncContinuation"
    this.__proto = ["AsyncContinuation", "Continuation"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.start_k_ = AsyncContinuation_start_k_
    this.resumeWith_Result_k_ = AsyncContinuation_resumeWith_Result_k_
    this.__get_scope = AsyncContinuation___get_scope_k_
    this.__get_block = AsyncContinuation___get_block_k_
    this.__get_deferred = AsyncContinuation___get_deferred_k_
    this.__get_context = AsyncContinuation___get_context_k_
    this.scope = scope
    this.block = block
    this.deferred = deferred
    return this
end function

sub AsyncContinuation_start_k_()
        try
        startCoroutine_rSuspendFunction1_AnyN_Continuation_k_(m.__get_block(), m.__get_scope(), m)
    catch e
        m.__get_deferred().completeExceptionally_Throwable_k_(e)
    end try
end sub

sub AsyncContinuation_resumeWith_Result_k_(result as Object)
    exception = result.exceptionOrNull_k_()
    if exception <> invalid then
        m.__get_deferred().completeExceptionally_Throwable_k_(exception)
    else if true then
        m.__get_deferred().complete_AnyN_k_(result.getOrThrow_k_())
    end if
end sub

function AsyncContinuation___get_scope_k_() as Object
    return m.scope
end function

function AsyncContinuation___get_block_k_() as Object
    return m.block
end function

function AsyncContinuation___get_deferred_k_() as Object
    return m.deferred
end function

function AsyncContinuation___get_context_k_() as Object
    return m.__get_scope().__get_coroutineContext()
end function

function RunBlockingContinuation_create_CoroutineScope_SuspendFunction1CoroutineScope_CompletableDeferred_k_(scope as Object, block as Object, deferred as Object) as Object
    this = {}
    this.__type = "RunBlockingContinuation"
    this.__proto = ["RunBlockingContinuation", "Continuation"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.start_k_ = RunBlockingContinuation_start_k_
    this.resumeWith_Result_k_ = RunBlockingContinuation_resumeWith_Result_k_
    this.__get_scope = RunBlockingContinuation___get_scope_k_
    this.__get_block = RunBlockingContinuation___get_block_k_
    this.__get_deferred = RunBlockingContinuation___get_deferred_k_
    this.__get_context = RunBlockingContinuation___get_context_k_
    this.scope = scope
    this.block = block
    this.deferred = deferred
    return this
end function

sub RunBlockingContinuation_start_k_()
        try
        startCoroutine_rSuspendFunction1_AnyN_Continuation_k_(m.__get_block(), m.__get_scope(), m)
    catch e
        m.__get_deferred().completeExceptionally_Throwable_k_(e)
    end try
end sub

sub RunBlockingContinuation_resumeWith_Result_k_(result as Object)
    exception = result.exceptionOrNull_k_()
    if exception <> invalid then
        m.__get_deferred().completeExceptionally_Throwable_k_(exception)
    else if true then
        m.__get_deferred().complete_AnyN_k_(result.getOrThrow_k_())
    end if
end sub

function RunBlockingContinuation___get_scope_k_() as Object
    return m.scope
end function

function RunBlockingContinuation___get_block_k_() as Object
    return m.block
end function

function RunBlockingContinuation___get_deferred_k_() as Object
    return m.deferred
end function

function RunBlockingContinuation___get_context_k_() as Object
    return m.__get_scope().__get_coroutineContext()
end function

sub startCoroutine_rSuspendFunction0_Continuation_k_(m as Object, completion as Object)
    resume_rContinuation_AnyN_k_(intercepted_rContinuation_k_(createCoroutineUnintercepted_rSuspendFunction0_Continuation_k_(m, completion)), invalid)
end sub

sub startCoroutine_rSuspendFunction1_AnyN_Continuation_k_(m as Object, receiver as Dynamic, completion as Object)
    resume_rContinuation_AnyN_k_(intercepted_rContinuation_k_(createCoroutineUnintercepted_rSuspendFunction1_AnyN_Continuation_k_(m, receiver, completion)), invalid)
end sub
