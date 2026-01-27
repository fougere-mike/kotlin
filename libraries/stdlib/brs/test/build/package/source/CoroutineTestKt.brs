sub coroutineTests_rTestRunner_k_(m as Object)
    m.suite_Str_Function1TestRunnerV_k_("Coroutine Primitives", coroutineTests_lambda_create_k_())
end sub

sub suspendFunctionTests_rTestRunner_k_(m as Object)
    m.suite_Str_Function1TestRunnerV_k_("Suspend Functions", suspendFunctionTests_lambda_create_k_())
end sub

sub dispatcherTests_rTestRunner_k_(m as Object)
    m.suite_Str_Function1TestRunnerV_k_("Dispatchers", dispatcherTests_lambda_create_k_())
end sub

sub delayTrackerTests_rTestRunner_k_(m as Object)
    m.suite_Str_Function1TestRunnerV_k_("DelayTracker", delayTrackerTests_lambda_create_k_())
end sub

sub delayFunctionTests_rTestRunner_k_(m as Object)
    m.suite_Str_Function1TestRunnerV_k_("delay() Function", delayFunctionTests_lambda_create_k_())
end sub

sub yieldFunctionTests_rTestRunner_k_(m as Object)
    m.suite_Str_Function1TestRunnerV_k_("yield() Function", yieldFunctionTests_lambda_create_k_())
end sub

sub coroutineQueueTests_rTestRunner_k_(m as Object)
    m.suite_Str_Function1TestRunnerV_k_("Coroutine Queue", coroutineQueueTests_lambda_create_k_())
end sub

sub runBlockingTests_rTestRunner_k_(m as Object)
    m.suite_Str_Function1TestRunnerV_k_("runBlocking with Delay", runBlockingTests_lambda_create_k_())
end sub

function coroutineTests_lambda_lambda_create_k_() as Object
    this = {}
    this.__type = "coroutineTests_lambda_lambda"
    this.__proto = ["coroutineTests_lambda_lambda", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = coroutineTests_lambda_lambda_invoke_k_
    return this
end function

sub coroutineTests_lambda_lambda_invoke_k_()
    ctx = EmptyCoroutineContext_getInstance()
    assertNull_AnyN_StrN_k_(EmptyCoroutineContext_getInstance().get_Key_k_(Job_Key_getInstance()), invalid)
end sub

function coroutineTests_lambda_lambda_1_create_k_() as Object
    this = {}
    this.__type = "coroutineTests_lambda_lambda_1"
    this.__proto = ["coroutineTests_lambda_lambda_1", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = coroutineTests_lambda_lambda_1_invoke_k_
    return this
end function

sub coroutineTests_lambda_lambda_1_invoke_k_()
    job = Job_JobN_k_(invalid)
    ctx = EmptyCoroutineContext_getInstance().plus_CoroutineContext_k_(job)
    assertEquals_AnyN_AnyN_StrN_k_(job, ctx.get_Key_k_(Job_Key_getInstance()), invalid)
end sub

function coroutineTests_lambda_lambda_2_create_k_() as Object
    this = {}
    this.__type = "coroutineTests_lambda_lambda_2"
    this.__proto = ["coroutineTests_lambda_lambda_2", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = coroutineTests_lambda_lambda_2_invoke_k_
    return this
end function

sub coroutineTests_lambda_lambda_2_invoke_k_()
    job = Job_JobN_k_(invalid)
    ctx = job
    assertEquals_AnyN_AnyN_StrN_k_(job, ctx.get_Key_k_(Job_Key_getInstance()), invalid)
end sub

function coroutineTests_lambda_lambda_3_create_k_() as Object
    this = {}
    this.__type = "coroutineTests_lambda_lambda_3"
    this.__proto = ["coroutineTests_lambda_lambda_3", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = coroutineTests_lambda_lambda_3_invoke_k_
    return this
end function

sub coroutineTests_lambda_lambda_3_invoke_k_()
    job = Job_JobN_k_(invalid)
    ctx1 = EmptyCoroutineContext_getInstance().plus_CoroutineContext_k_(job)
    assertNotNull_AnyN_StrN_k_(ctx1.get_Key_k_(Job_Key_getInstance()), invalid)
end sub

function coroutineTests_lambda_lambda_4_create_k_() as Object
    this = {}
    this.__type = "coroutineTests_lambda_lambda_4"
    this.__proto = ["coroutineTests_lambda_lambda_4", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = coroutineTests_lambda_lambda_4_invoke_k_
    return this
end function

sub coroutineTests_lambda_lambda_4_invoke_k_()
    job = Job_JobN_k_(invalid)
    ctx = EmptyCoroutineContext_getInstance().plus_CoroutineContext_k_(job)
    withoutJob = ctx.minusKey_Key_k_(Job_Key_getInstance())
    assertNull_AnyN_StrN_k_(withoutJob.get_Key_k_(Job_Key_getInstance()), invalid)
end sub

function coroutineTests_lambda_lambda_5_create_k_() as Object
    this = {}
    this.__type = "coroutineTests_lambda_lambda_5"
    this.__proto = ["coroutineTests_lambda_lambda_5", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = coroutineTests_lambda_lambda_5_invoke_k_
    return this
end function

sub coroutineTests_lambda_lambda_5_invoke_k_()
    job = Job_JobN_k_(invalid)
    assertTrue_Z_StrN_k_(job.__get_isActive(), invalid)
    assertFalse_Z_StrN_k_(job.__get_isCompleted(), invalid)
    assertFalse_Z_StrN_k_(job.__get_isCancelled(), invalid)
end sub

function coroutineTests_lambda_lambda_6_create_k_() as Object
    this = {}
    this.__type = "coroutineTests_lambda_lambda_6"
    this.__proto = ["coroutineTests_lambda_lambda_6", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = coroutineTests_lambda_lambda_6_invoke_k_
    return this
end function

sub coroutineTests_lambda_lambda_6_invoke_k_()
    job = Job_JobN_k_(invalid)
    assertTrue_Z_StrN_k_(job.complete_k_(), invalid)
    assertFalse_Z_StrN_k_(job.__get_isActive(), invalid)
    assertTrue_Z_StrN_k_(job.__get_isCompleted(), invalid)
    assertFalse_Z_StrN_k_(job.__get_isCancelled(), invalid)
end sub

function coroutineTests_lambda_lambda_7_create_k_() as Object
    this = {}
    this.__type = "coroutineTests_lambda_lambda_7"
    this.__proto = ["coroutineTests_lambda_lambda_7", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = coroutineTests_lambda_lambda_7_invoke_k_
    return this
end function

sub coroutineTests_lambda_lambda_7_invoke_k_()
    job = Job_JobN_k_(invalid)
    job.cancel_ThrowableN_k_(invalid)
    assertFalse_Z_StrN_k_(job.__get_isActive(), invalid)
    assertTrue_Z_StrN_k_(job.__get_isCancelled(), invalid)
end sub

function coroutineTests_lambda_lambda_8_create_k_() as Object
    this = {}
    this.__type = "coroutineTests_lambda_lambda_8"
    this.__proto = ["coroutineTests_lambda_lambda_8", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = coroutineTests_lambda_lambda_8_invoke_k_
    return this
end function

sub coroutineTests_lambda_lambda_8_invoke_k_()
    job = Job_JobN_k_(invalid)
    assertTrue_Z_StrN_k_(job.complete_k_(), invalid)
    assertFalse_Z_StrN_k_(job.complete_k_(), invalid)
end sub

function coroutineTests_lambda_lambda_9_create_k_() as Object
    this = {}
    this.__type = "coroutineTests_lambda_lambda_9"
    this.__proto = ["coroutineTests_lambda_lambda_9", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = coroutineTests_lambda_lambda_9_invoke_k_
    return this
end function

sub coroutineTests_lambda_lambda_9_invoke_k_()
    job = Job_JobN_k_(invalid)
    job.complete_k_()
    job.cancel_ThrowableN_k_(invalid)
    assertTrue_Z_StrN_k_(job.__get_isCompleted(), invalid)
    assertFalse_Z_StrN_k_(job.__get_isCancelled(), invalid)
end sub

function coroutineTests_lambda_lambda_10_create_k_() as Object
    this = {}
    this.__type = "coroutineTests_lambda_lambda_10"
    this.__proto = ["coroutineTests_lambda_lambda_10", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = coroutineTests_lambda_lambda_10_invoke_k_
    return this
end function

sub coroutineTests_lambda_lambda_10_invoke_k_()
    deferred = CompletableDeferred_JobN_k_(invalid)
    assertTrue_Z_StrN_k_(deferred.__get_isActive(), invalid)
    assertFalse_Z_StrN_k_(deferred.__get_isCompleted(), invalid)
end sub

function coroutineTests_lambda_lambda_11_create_k_() as Object
    this = {}
    this.__type = "coroutineTests_lambda_lambda_11"
    this.__proto = ["coroutineTests_lambda_lambda_11", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = coroutineTests_lambda_lambda_11_invoke_k_
    return this
end function

sub coroutineTests_lambda_lambda_11_invoke_k_()
    deferred = CompletableDeferred_JobN_k_(invalid)
    assertTrue_Z_StrN_k_(deferred.complete_AnyN_k_(42), invalid)
    assertTrue_Z_StrN_k_(deferred.__get_isCompleted(), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(42, deferred.getCompleted_k_(), invalid)
end sub

function coroutineTests_lambda_lambda_12_create_k_() as Object
    this = {}
    this.__type = "coroutineTests_lambda_lambda_12"
    this.__proto = ["coroutineTests_lambda_lambda_12", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = coroutineTests_lambda_lambda_12_invoke_k_
    return this
end function

sub coroutineTests_lambda_lambda_12_invoke_k_()
    deferred = CompletableDeferred_JobN_k_(invalid)
    assertTrue_Z_StrN_k_(deferred.complete_AnyN_k_(1), invalid)
    assertFalse_Z_StrN_k_(deferred.complete_AnyN_k_(2), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(1, deferred.getCompleted_k_(), invalid)
end sub

function coroutineTests_lambda_lambda_13_create_k_() as Object
    this = {}
    this.__type = "coroutineTests_lambda_lambda_13"
    this.__proto = ["coroutineTests_lambda_lambda_13", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = coroutineTests_lambda_lambda_13_invoke_k_
    return this
end function

sub coroutineTests_lambda_lambda_13_invoke_k_()
    deferred = CompletableDeferred_JobN_k_(invalid)
    threw = false
        try
        deferred.getCompleted_k_()
    catch e
        threw = true
    end try
    assertTrue_Z_StrN_k_(threw, "getCompleted should throw IllegalStateException when not completed")
end sub

function coroutineTests_lambda_lambda_14_create_k_() as Object
    this = {}
    this.__type = "coroutineTests_lambda_lambda_14"
    this.__proto = ["coroutineTests_lambda_lambda_14", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = coroutineTests_lambda_lambda_14_invoke_k_
    return this
end function

sub coroutineTests_lambda_lambda_14_invoke_k_()
    ctx = EmptyCoroutineContext_getInstance()
    scope = CoroutineScope_CoroutineContext_k_(ctx)
    assertEquals_AnyN_AnyN_StrN_k_(ctx, scope.__get_coroutineContext(), invalid)
end sub

function coroutineTests_lambda_lambda_15_create_k_() as Object
    this = {}
    this.__type = "coroutineTests_lambda_lambda_15"
    this.__proto = ["coroutineTests_lambda_lambda_15", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = coroutineTests_lambda_lambda_15_invoke_k_
    return this
end function

sub coroutineTests_lambda_lambda_15_invoke_k_()
    job = Job_JobN_k_(invalid)
    scope = CoroutineScope_CoroutineContext_k_(job)
    assertEquals_AnyN_AnyN_StrN_k_(job, scope.__get_coroutineContext().get_Key_k_(Job_Key_getInstance()), invalid)
end sub

function Anon_3d851f09_create_AnyN_k_(_result as Dynamic) as Object
    this = {}
    this.__type = "Anon_3d851f09"
    this.__proto = ["Anon_3d851f09", "Continuation"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.resumeWith_Result_k_ = Anon_3d851f09_resumeWith_Result_k_
    this.__get_context = Anon_3d851f09___get_context_k_
    this.context = EmptyCoroutineContext_getInstance()
    this._result = _result
    return this
end function

sub Anon_3d851f09_resumeWith_Result_k_(r as Object)
    m._result.value = r.getOrNull_k_()
end sub

function Anon_3d851f09___get_context_k_() as Object
    return m.context
end function

function coroutineTests_lambda_lambda_16_create_k_() as Object
    this = {}
    this.__type = "coroutineTests_lambda_lambda_16"
    this.__proto = ["coroutineTests_lambda_lambda_16", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = coroutineTests_lambda_lambda_16_invoke_k_
    return this
end function

sub coroutineTests_lambda_lambda_16_invoke_k_()
    result = {value: invalid}
    continuation = Anon_3d851f09_create_AnyN_k_(result)
    resume_rContinuation_AnyN_k_(continuation, 42)
    assertEquals_AnyN_AnyN_StrN_k_(42, result.value, invalid)
end sub

function Anon_4a44572a_create_AnyN_k_(_exception as Dynamic) as Object
    this = {}
    this.__type = "Anon_4a44572a"
    this.__proto = ["Anon_4a44572a", "Continuation"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.resumeWith_Result_k_ = Anon_4a44572a_resumeWith_Result_k_
    this.__get_context = Anon_4a44572a___get_context_k_
    this.context = EmptyCoroutineContext_getInstance()
    this._exception = _exception
    return this
end function

sub Anon_4a44572a_resumeWith_Result_k_(r as Object)
    m._exception.value = r.exceptionOrNull_k_()
end sub

function Anon_4a44572a___get_context_k_() as Object
    return m.context
end function

function coroutineTests_lambda_lambda_17_create_k_() as Object
    this = {}
    this.__type = "coroutineTests_lambda_lambda_17"
    this.__proto = ["coroutineTests_lambda_lambda_17", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = coroutineTests_lambda_lambda_17_invoke_k_
    return this
end function

sub coroutineTests_lambda_lambda_17_invoke_k_()
    exception = {value: invalid}
    continuation = Anon_4a44572a_create_AnyN_k_(exception)
    testException = RuntimeException_create_StrN_k_("test")
    resumeWithException_rContinuation_Throwable_k_(continuation, testException)
    assertEquals_AnyN_AnyN_StrN_k_(testException, exception.value, invalid)
end sub

function coroutineTests_lambda_lambda_18_create_k_() as Object
    this = {}
    this.__type = "coroutineTests_lambda_lambda_18"
    this.__proto = ["coroutineTests_lambda_lambda_18", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = coroutineTests_lambda_lambda_18_invoke_k_
    return this
end function

sub coroutineTests_lambda_lambda_18_invoke_k_()
    suspended = __get_COROUTINE_SUSPENDED_k_()
    assertNotNull_AnyN_StrN_k_(suspended, invalid)
    assertSame_AnyN_AnyN_StrN_k_(__get_COROUTINE_SUSPENDED_k_(), suspended, invalid)
end sub

function coroutineTests_lambda_create_k_() as Object
    this = {}
    this.__type = "coroutineTests_lambda"
    this.__proto = ["coroutineTests_lambda", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = coroutineTests_lambda_invoke_AnyN_k_
    return this
end function

sub coroutineTests_lambda_invoke_AnyN_k_(_this_suite as Object)
    _this_suite.test_Str_Function0V_k_("EmptyCoroutineContext is empty", coroutineTests_lambda_lambda_create_k_())
    _this_suite.test_Str_Function0V_k_("EmptyCoroutineContext plus element", coroutineTests_lambda_lambda_1_create_k_())
    _this_suite.test_Str_Function0V_k_("Context element lookup", coroutineTests_lambda_lambda_2_create_k_())
    _this_suite.test_Str_Function0V_k_("Combined context", coroutineTests_lambda_lambda_3_create_k_())
    _this_suite.test_Str_Function0V_k_("Context minusKey", coroutineTests_lambda_lambda_4_create_k_())
    _this_suite.test_Str_Function0V_k_("Job initial state is active", coroutineTests_lambda_lambda_5_create_k_())
    _this_suite.test_Str_Function0V_k_("Job complete", coroutineTests_lambda_lambda_6_create_k_())
    _this_suite.test_Str_Function0V_k_("Job cancel", coroutineTests_lambda_lambda_7_create_k_())
    _this_suite.test_Str_Function0V_k_("Job complete returns false if already completed", coroutineTests_lambda_lambda_8_create_k_())
    _this_suite.test_Str_Function0V_k_("Job cancel after complete", coroutineTests_lambda_lambda_9_create_k_())
    _this_suite.test_Str_Function0V_k_("CompletableDeferred initial state", coroutineTests_lambda_lambda_10_create_k_())
    _this_suite.test_Str_Function0V_k_("CompletableDeferred complete with value", coroutineTests_lambda_lambda_11_create_k_())
    _this_suite.test_Str_Function0V_k_("CompletableDeferred complete returns false if already completed", coroutineTests_lambda_lambda_12_create_k_())
    _this_suite.test_Str_Function0V_k_("CompletableDeferred getCompleted throws if not completed", coroutineTests_lambda_lambda_13_create_k_())
    _this_suite.test_Str_Function0V_k_("CoroutineScope factory function", coroutineTests_lambda_lambda_14_create_k_())
    _this_suite.test_Str_Function0V_k_("CoroutineScope with Job", coroutineTests_lambda_lambda_15_create_k_())
    _this_suite.test_Str_Function0V_k_("Continuation resume", coroutineTests_lambda_lambda_16_create_k_())
    _this_suite.test_Str_Function0V_k_("Continuation resumeWithException", coroutineTests_lambda_lambda_17_create_k_())
    _this_suite.test_Str_Function0V_k_("COROUTINE_SUSPENDED is a sentinel", coroutineTests_lambda_lambda_18_create_k_())
end sub

function suspendFunctionTests_lambda_lambda_create_k_() as Object
    this = {}
    this.__type = "suspendFunctionTests_lambda_lambda"
    this.__proto = ["suspendFunctionTests_lambda_lambda", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = suspendFunctionTests_lambda_lambda_invoke_k_
    return this
end function

sub suspendFunctionTests_lambda_lambda_invoke_k_()
    assertTrue_Z_StrN_k_(true, "Suspend function tests compile successfully")
end sub

function suspendFunctionTests_lambda_lambda_1_create_k_() as Object
    this = {}
    this.__type = "suspendFunctionTests_lambda_lambda_1"
    this.__proto = ["suspendFunctionTests_lambda_lambda_1", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = suspendFunctionTests_lambda_lambda_1_invoke_k_
    return this
end function

sub suspendFunctionTests_lambda_lambda_1_invoke_k_()
    assertTrue_Z_StrN_k_(true, "Suspend function with parameters tests compile successfully")
end sub

function suspendFunctionTests_lambda_lambda_slambda_create_ContinuationAnyNN_k_(resultContinuation as Dynamic) as Object
    this = CoroutineImpl_create_ContinuationAnyNN_k_(resultContinuation)
    this._super = {}
    this._super.invoke_Continuation_k_ = this.invoke_Continuation_k_
    this._super.doResume_k_ = this.doResume_k_
    this._super.create_Continuation_k_ = this.create_Continuation_k_
    this.__proto = ["suspendFunctionTests_lambda_lambda_slambda", "SuspendFunction0", this.__proto]
    this.__type = "suspendFunctionTests_lambda_lambda_slambda"
    this.invoke_Continuation_k_ = suspendFunctionTests_lambda_lambda_slambda_invoke_Continuation_k_
    this.doResume_k_ = suspendFunctionTests_lambda_lambda_slambda_doResume_k_
    this.create_Continuation_k_ = suspendFunctionTests_lambda_lambda_slambda_create_Continuation_k_
    return this
end function

function suspendFunctionTests_lambda_lambda_slambda_invoke_Continuation_k_(_completion as Object) as Object
    tmp = m.create_Continuation_k_(_completion)
    tmp.__set_result(invalid)
    tmp.__set_exception(invalid)
    return tmp.doResume_k_()
end function

function suspendFunctionTests_lambda_lambda_slambda_doResume_k_() as Dynamic
    suspendResult = m.__get_result()
    while true
                try
            tmp = m.__get_state()
            if __kotlin_identityEquals(tmp, 0) then
                m.__set_exceptionState(1)
                return 42

                                return invalid
            else if __kotlin_identityEquals(tmp, 1) then
                throw e
            end if
        catch e
                        throw e
        end try
    end while
end function

function suspendFunctionTests_lambda_lambda_slambda_create_Continuation_k_(completion as Object) as Object
    i = suspendFunctionTests_lambda_lambda_slambda_create_ContinuationAnyNN_k_(completion)
    return i
end function

function suspendFunctionTests_lambda_lambda_2_create_k_() as Object
    this = {}
    this.__type = "suspendFunctionTests_lambda_lambda_2"
    this.__proto = ["suspendFunctionTests_lambda_lambda_2", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = suspendFunctionTests_lambda_lambda_2_invoke_k_
    return this
end function

sub suspendFunctionTests_lambda_lambda_2_invoke_k_()
    suspendLambda = suspendFunctionTests_lambda_lambda_slambda_create_ContinuationAnyNN_k_(invalid)
    assertNotNull_AnyN_StrN_k_(suspendLambda, invalid)
end sub

function suspendFunctionTests_lambda_lambda_slambda_1_create_ContinuationAnyNN_k_(resultContinuation as Dynamic) as Object
    this = CoroutineImpl_create_ContinuationAnyNN_k_(resultContinuation)
    this._super = {}
    this._super.invoke_AnyN_Continuation_k_ = this.invoke_AnyN_Continuation_k_
    this._super.doResume_k_ = this.doResume_k_
    this._super.create_AnyN_Continuation_k_ = this.create_AnyN_Continuation_k_
    this.__proto = ["suspendFunctionTests_lambda_lambda_slambda_1", "SuspendFunction1", this.__proto]
    this.__type = "suspendFunctionTests_lambda_lambda_slambda_1"
    this.invoke_AnyN_Continuation_k_ = suspendFunctionTests_lambda_lambda_slambda_1_invoke_AnyN_Continuation_k_
    this.doResume_k_ = suspendFunctionTests_lambda_lambda_slambda_1_doResume_k_
    this.create_AnyN_Continuation_k_ = suspendFunctionTests_lambda_lambda_slambda_1_create_AnyN_Continuation_k_
    return this
end function

function suspendFunctionTests_lambda_lambda_slambda_1_invoke_AnyN_Continuation_k_(__this as Integer, _completion as Object) as Object
    tmp = m.create_AnyN_Continuation_k_(m, _completion)
    tmp.__set_result(invalid)
    tmp.__set_exception(invalid)
    return tmp.doResume_k_()
end function

function suspendFunctionTests_lambda_lambda_slambda_1_doResume_k_() as Dynamic
    suspendResult = m.__get_result()
    while true
                try
            tmp = m.__get_state()
            if __kotlin_identityEquals(tmp, 0) then
                m.__set_exceptionState(1)
                return m.__this * 2

                                return invalid
            else if __kotlin_identityEquals(tmp, 1) then
                throw e
            end if
        catch e
                        throw e
        end try
    end while
end function

function suspendFunctionTests_lambda_lambda_slambda_1_create_AnyN_Continuation_k_(__this as Dynamic, completion as Object) as Object
    i = suspendFunctionTests_lambda_lambda_slambda_1_create_ContinuationAnyNN_k_(completion)
    i.__this = m
    return i
end function

function suspendFunctionTests_lambda_lambda_3_create_k_() as Object
    this = {}
    this.__type = "suspendFunctionTests_lambda_lambda_3"
    this.__proto = ["suspendFunctionTests_lambda_lambda_3", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = suspendFunctionTests_lambda_lambda_3_invoke_k_
    return this
end function

sub suspendFunctionTests_lambda_lambda_3_invoke_k_()
    suspendLambda = suspendFunctionTests_lambda_lambda_slambda_1_create_ContinuationAnyNN_k_(invalid)
    assertNotNull_AnyN_StrN_k_(suspendLambda, invalid)
end sub

function suspendFunctionTests_lambda_create_k_() as Object
    this = {}
    this.__type = "suspendFunctionTests_lambda"
    this.__proto = ["suspendFunctionTests_lambda", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = suspendFunctionTests_lambda_invoke_AnyN_k_
    return this
end function

sub suspendFunctionTests_lambda_invoke_AnyN_k_(_this_suite as Object)
    _this_suite.test_Str_Function0V_k_("Simple suspend function", suspendFunctionTests_lambda_lambda_create_k_())
    _this_suite.test_Str_Function0V_k_("Suspend function with parameters", suspendFunctionTests_lambda_lambda_1_create_k_())
    _this_suite.test_Str_Function0V_k_("Suspend lambda", suspendFunctionTests_lambda_lambda_2_create_k_())
    _this_suite.test_Str_Function0V_k_("Suspend lambda with receiver", suspendFunctionTests_lambda_lambda_3_create_k_())
end sub

function dispatcherTests_lambda_lambda_create_k_() as Object
    this = {}
    this.__type = "dispatcherTests_lambda_lambda"
    this.__proto = ["dispatcherTests_lambda_lambda", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = dispatcherTests_lambda_lambda_invoke_k_
    return this
end function

sub dispatcherTests_lambda_lambda_invoke_k_()
    dispatcher = Dispatchers_getInstance().__get_Default()
    assertNotNull_AnyN_StrN_k_(dispatcher, invalid)
end sub

function dispatcherTests_lambda_lambda_1_create_k_() as Object
    this = {}
    this.__type = "dispatcherTests_lambda_lambda_1"
    this.__proto = ["dispatcherTests_lambda_lambda_1", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = dispatcherTests_lambda_lambda_1_invoke_k_
    return this
end function

sub dispatcherTests_lambda_lambda_1_invoke_k_()
    dispatcher = Dispatchers_getInstance().__get_Main()
    assertNotNull_AnyN_StrN_k_(dispatcher, invalid)
end sub

function dispatcherTests_lambda_lambda_2_create_k_() as Object
    this = {}
    this.__type = "dispatcherTests_lambda_lambda_2"
    this.__proto = ["dispatcherTests_lambda_lambda_2", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = dispatcherTests_lambda_lambda_2_invoke_k_
    return this
end function

sub dispatcherTests_lambda_lambda_2_invoke_k_()
    dispatcher = Dispatchers_getInstance().__get_Unconfined()
    assertNotNull_AnyN_StrN_k_(dispatcher, invalid)
end sub

function dispatcherTests_lambda_lambda_3_create_k_() as Object
    this = {}
    this.__type = "dispatcherTests_lambda_lambda_3"
    this.__proto = ["dispatcherTests_lambda_lambda_3", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = dispatcherTests_lambda_lambda_3_invoke_k_
    return this
end function

sub dispatcherTests_lambda_lambda_3_invoke_k_()
    dispatcher = Dispatchers_getInstance().__get_IO()
    assertNotNull_AnyN_StrN_k_(dispatcher, invalid)
end sub

function dispatcherTests_lambda_lambda_4_create_k_() as Object
    this = {}
    this.__type = "dispatcherTests_lambda_lambda_4"
    this.__proto = ["dispatcherTests_lambda_lambda_4", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = dispatcherTests_lambda_lambda_4_invoke_k_
    return this
end function

sub dispatcherTests_lambda_lambda_4_invoke_k_()
    dispatcher = Dispatchers_getInstance().__get_Unconfined()
    assertFalse_Z_StrN_k_(dispatcher.isDispatchNeeded_CoroutineContext_k_(EmptyCoroutineContext_getInstance()), invalid)
end sub

function dispatcherTests_lambda_lambda_5_create_k_() as Object
    this = {}
    this.__type = "dispatcherTests_lambda_lambda_5"
    this.__proto = ["dispatcherTests_lambda_lambda_5", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = dispatcherTests_lambda_lambda_5_invoke_k_
    return this
end function

sub dispatcherTests_lambda_lambda_5_invoke_k_()
    dispatcher = Dispatchers_getInstance().__get_Default()
    assertTrue_Z_StrN_k_(dispatcher.isDispatchNeeded_CoroutineContext_k_(EmptyCoroutineContext_getInstance()), invalid)
end sub

function dispatcherTests_lambda_create_k_() as Object
    this = {}
    this.__type = "dispatcherTests_lambda"
    this.__proto = ["dispatcherTests_lambda", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = dispatcherTests_lambda_invoke_AnyN_k_
    return this
end function

sub dispatcherTests_lambda_invoke_AnyN_k_(_this_suite as Object)
    _this_suite.test_Str_Function0V_k_("Dispatchers.Default exists", dispatcherTests_lambda_lambda_create_k_())
    _this_suite.test_Str_Function0V_k_("Dispatchers.Main exists", dispatcherTests_lambda_lambda_1_create_k_())
    _this_suite.test_Str_Function0V_k_("Dispatchers.Unconfined exists", dispatcherTests_lambda_lambda_2_create_k_())
    _this_suite.test_Str_Function0V_k_("Dispatchers.IO exists", dispatcherTests_lambda_lambda_3_create_k_())
    _this_suite.test_Str_Function0V_k_("Unconfined dispatcher does not need dispatch", dispatcherTests_lambda_lambda_4_create_k_())
    _this_suite.test_Str_Function0V_k_("Default dispatcher needs dispatch", dispatcherTests_lambda_lambda_5_create_k_())
end sub

function delayTrackerTests_lambda_lambda_create_k_() as Object
    this = {}
    this.__type = "delayTrackerTests_lambda_lambda"
    this.__proto = ["delayTrackerTests_lambda_lambda", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = delayTrackerTests_lambda_lambda_invoke_k_
    return this
end function

sub delayTrackerTests_lambda_lambda_invoke_k_()
    tracker = DelayTracker_Companion_getInstance().__get_current()
    assertNotNull_AnyN_StrN_k_(tracker, invalid)
end sub

function delayTrackerTests_lambda_lambda_1_create_k_() as Object
    this = {}
    this.__type = "delayTrackerTests_lambda_lambda_1"
    this.__proto = ["delayTrackerTests_lambda_lambda_1", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = delayTrackerTests_lambda_lambda_1_invoke_k_
    return this
end function

sub delayTrackerTests_lambda_lambda_1_invoke_k_()
    tracker1 = DelayTracker_Companion_getInstance().__get_current()
    tracker2 = DelayTracker_Companion_getInstance().__get_current()
    assertSame_AnyN_AnyN_StrN_k_(tracker1, tracker2, invalid)
end sub

function delayTrackerTests_lambda_lambda_2_create_k_() as Object
    this = {}
    this.__type = "delayTrackerTests_lambda_lambda_2"
    this.__proto = ["delayTrackerTests_lambda_lambda_2", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = delayTrackerTests_lambda_lambda_2_invoke_k_
    return this
end function

sub delayTrackerTests_lambda_lambda_2_invoke_k_()
    tracker = DelayTracker_create_k_()
    assertFalse_Z_StrN_k_(tracker.hasPendingDelays_k_(), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(0, tracker.pendingCount_k_(), invalid)
end sub

function delayTrackerTests_lambda_lambda_lambda_create_k_() as Object
    this = {}
    this.__type = "delayTrackerTests_lambda_lambda_lambda"
    this.__proto = ["delayTrackerTests_lambda_lambda_lambda", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = delayTrackerTests_lambda_lambda_lambda_invoke_k_
    return this
end function

sub delayTrackerTests_lambda_lambda_lambda_invoke_k_()
    return
end sub

function delayTrackerTests_lambda_lambda_3_create_k_() as Object
    this = {}
    this.__type = "delayTrackerTests_lambda_lambda_3"
    this.__proto = ["delayTrackerTests_lambda_lambda_3", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = delayTrackerTests_lambda_lambda_3_invoke_k_
    return this
end function

sub delayTrackerTests_lambda_lambda_3_invoke_k_()
    tracker = DelayTracker_create_k_()
    tracker.register_J_Function0V_k_(1000&, delayTrackerTests_lambda_lambda_lambda_create_k_())
    assertTrue_Z_StrN_k_(tracker.hasPendingDelays_k_(), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(1, tracker.pendingCount_k_(), invalid)
end sub

function delayTrackerTests_lambda_lambda_4_create_k_() as Object
    this = {}
    this.__type = "delayTrackerTests_lambda_lambda_4"
    this.__proto = ["delayTrackerTests_lambda_lambda_4", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = delayTrackerTests_lambda_lambda_4_invoke_k_
    return this
end function

sub delayTrackerTests_lambda_lambda_4_invoke_k_()
    tracker = DelayTracker_create_k_()
    assertEquals_AnyN_AnyN_StrN_k_(0, tracker.tick_k_(), invalid)
end sub

function delayTrackerTests_lambda_lambda_lambda_1_create_AnyN_k_(_fired as Dynamic) as Object
    this = {}
    this.__type = "delayTrackerTests_lambda_lambda_lambda_1"
    this.__proto = ["delayTrackerTests_lambda_lambda_lambda_1", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = delayTrackerTests_lambda_lambda_lambda_1_invoke_k_
    this._fired = _fired
    return this
end function

sub delayTrackerTests_lambda_lambda_lambda_1_invoke_k_()
    m._fired.value = true
end sub

function delayTrackerTests_lambda_lambda_5_create_k_() as Object
    this = {}
    this.__type = "delayTrackerTests_lambda_lambda_5"
    this.__proto = ["delayTrackerTests_lambda_lambda_5", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = delayTrackerTests_lambda_lambda_5_invoke_k_
    return this
end function

sub delayTrackerTests_lambda_lambda_5_invoke_k_()
    tracker = DelayTracker_create_k_()
    fired = {value: false}
    tracker.register_J_Function0V_k_(0&, delayTrackerTests_lambda_lambda_lambda_1_create_AnyN_k_(fired))
    count = tracker.tick_k_()
    assertEquals_AnyN_AnyN_StrN_k_(1, count, invalid)
    assertTrue_Z_StrN_k_(fired.value, invalid)
    assertFalse_Z_StrN_k_(tracker.hasPendingDelays_k_(), invalid)
end sub

function delayTrackerTests_lambda_lambda_lambda_2_create_AnyN_k_(_fired as Dynamic) as Object
    this = {}
    this.__type = "delayTrackerTests_lambda_lambda_lambda_2"
    this.__proto = ["delayTrackerTests_lambda_lambda_lambda_2", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = delayTrackerTests_lambda_lambda_lambda_2_invoke_k_
    this._fired = _fired
    return this
end function

sub delayTrackerTests_lambda_lambda_lambda_2_invoke_k_()
    m._fired.value = false
end sub

function delayTrackerTests_lambda_lambda_6_create_k_() as Object
    this = {}
    this.__type = "delayTrackerTests_lambda_lambda_6"
    this.__proto = ["delayTrackerTests_lambda_lambda_6", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = delayTrackerTests_lambda_lambda_6_invoke_k_
    return this
end function

sub delayTrackerTests_lambda_lambda_6_invoke_k_()
    tracker = DelayTracker_create_k_()
    fired = {value: false}
    tracker.register_J_Function0V_k_(10000&, delayTrackerTests_lambda_lambda_lambda_2_create_AnyN_k_(fired))
    count = tracker.tick_k_()
    assertEquals_AnyN_AnyN_StrN_k_(0, count, invalid)
    assertFalse_Z_StrN_k_(fired.value, invalid)
    assertTrue_Z_StrN_k_(tracker.hasPendingDelays_k_(), invalid)
end sub

function delayTrackerTests_lambda_lambda_lambda_3_create_MutableListI_k_(_results as Object) as Object
    this = {}
    this.__type = "delayTrackerTests_lambda_lambda_lambda_3"
    this.__proto = ["delayTrackerTests_lambda_lambda_lambda_3", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = delayTrackerTests_lambda_lambda_lambda_3_invoke_k_
    this._results = _results
    return this
end function

sub delayTrackerTests_lambda_lambda_lambda_3_invoke_k_()
    m._results.add_AnyN_k_(1)
end sub

function delayTrackerTests_lambda_lambda_lambda_4_create_MutableListI_k_(_results as Object) as Object
    this = {}
    this.__type = "delayTrackerTests_lambda_lambda_lambda_4"
    this.__proto = ["delayTrackerTests_lambda_lambda_lambda_4", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = delayTrackerTests_lambda_lambda_lambda_4_invoke_k_
    this._results = _results
    return this
end function

sub delayTrackerTests_lambda_lambda_lambda_4_invoke_k_()
    m._results.add_AnyN_k_(2)
end sub

function delayTrackerTests_lambda_lambda_lambda_5_create_MutableListI_k_(_results as Object) as Object
    this = {}
    this.__type = "delayTrackerTests_lambda_lambda_lambda_5"
    this.__proto = ["delayTrackerTests_lambda_lambda_lambda_5", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = delayTrackerTests_lambda_lambda_lambda_5_invoke_k_
    this._results = _results
    return this
end function

sub delayTrackerTests_lambda_lambda_lambda_5_invoke_k_()
    m._results.add_AnyN_k_(3)
end sub

function delayTrackerTests_lambda_lambda_7_create_k_() as Object
    this = {}
    this.__type = "delayTrackerTests_lambda_lambda_7"
    this.__proto = ["delayTrackerTests_lambda_lambda_7", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = delayTrackerTests_lambda_lambda_7_invoke_k_
    return this
end function

sub delayTrackerTests_lambda_lambda_7_invoke_k_()
    tracker = DelayTracker_create_k_()
    results = mutableListOf_k_()
    tracker.register_J_Function0V_k_(0&, delayTrackerTests_lambda_lambda_lambda_3_create_MutableListI_k_(results))
    tracker.register_J_Function0V_k_(0&, delayTrackerTests_lambda_lambda_lambda_4_create_MutableListI_k_(results))
    tracker.register_J_Function0V_k_(0&, delayTrackerTests_lambda_lambda_lambda_5_create_MutableListI_k_(results))
    count = tracker.tick_k_()
    assertEquals_AnyN_AnyN_StrN_k_(3, count, invalid)
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([1, 2, 3]), results, invalid)
end sub

function delayTrackerTests_lambda_lambda_8_create_k_() as Object
    this = {}
    this.__type = "delayTrackerTests_lambda_lambda_8"
    this.__proto = ["delayTrackerTests_lambda_lambda_8", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = delayTrackerTests_lambda_lambda_8_invoke_k_
    return this
end function

sub delayTrackerTests_lambda_lambda_8_invoke_k_()
    tracker = DelayTracker_create_k_()
    time1 = tracker.currentTimeMs_k_()
    sum = 0
    progression = until_rI_I_k_(0, 1000)
    inductionVariable = progression.__get_first()
    last = progression.__get_last()
    if inductionVariable <= last then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        sum = (sum + i)


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            sum = (sum + i)

        end while

    end if

    time2 = tracker.currentTimeMs_k_()
    assertTrue_Z_StrN_k_(time2 >= time1, "Time should not go backwards")
end sub

function delayTrackerTests_lambda_create_k_() as Object
    this = {}
    this.__type = "delayTrackerTests_lambda"
    this.__proto = ["delayTrackerTests_lambda", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = delayTrackerTests_lambda_invoke_AnyN_k_
    return this
end function

sub delayTrackerTests_lambda_invoke_AnyN_k_(_this_suite as Object)
    _this_suite.test_Str_Function0V_k_("DelayTracker.current returns tracker", delayTrackerTests_lambda_lambda_create_k_())
    _this_suite.test_Str_Function0V_k_("DelayTracker.current returns same instance", delayTrackerTests_lambda_lambda_1_create_k_())
    _this_suite.test_Str_Function0V_k_("DelayTracker initially has no pending delays", delayTrackerTests_lambda_lambda_2_create_k_())
    _this_suite.test_Str_Function0V_k_("DelayTracker register adds pending delay", delayTrackerTests_lambda_lambda_3_create_k_())
    _this_suite.test_Str_Function0V_k_("DelayTracker tick with no delays returns 0", delayTrackerTests_lambda_lambda_4_create_k_())
    _this_suite.test_Str_Function0V_k_("DelayTracker tick fires expired delays", delayTrackerTests_lambda_lambda_5_create_k_())
    _this_suite.test_Str_Function0V_k_("DelayTracker tick does not fire unexpired delays", delayTrackerTests_lambda_lambda_6_create_k_())
    _this_suite.test_Str_Function0V_k_("DelayTracker multiple delays fire in order", delayTrackerTests_lambda_lambda_7_create_k_())
    _this_suite.test_Str_Function0V_k_("DelayTracker currentTimeMs increases", delayTrackerTests_lambda_lambda_8_create_k_())
end sub

function delayFunctionTests_lambda_lambda_slambda_create_AnyN_ContinuationAnyNN_k_(_done as Dynamic, resultContinuation as Dynamic) as Object
    this = CoroutineImpl_create_ContinuationAnyNN_k_(resultContinuation)
    this._super = {}
    this._super.invoke_AnyN_Continuation_k_ = this.invoke_AnyN_Continuation_k_
    this._super.doResume_k_ = this.doResume_k_
    this._super.create_AnyN_Continuation_k_ = this.create_AnyN_Continuation_k_
    this.__proto = ["delayFunctionTests_lambda_lambda_slambda", "SuspendFunction1", this.__proto]
    this.__type = "delayFunctionTests_lambda_lambda_slambda"
    this.invoke_AnyN_Continuation_k_ = delayFunctionTests_lambda_lambda_slambda_invoke_AnyN_Continuation_k_
    this.doResume_k_ = delayFunctionTests_lambda_lambda_slambda_doResume_k_
    this.create_AnyN_Continuation_k_ = delayFunctionTests_lambda_lambda_slambda_create_AnyN_Continuation_k_
    this._done = _done
    return this
end function

function delayFunctionTests_lambda_lambda_slambda_invoke_AnyN_Continuation_k_(_this_runBlocking as Object, _completion as Object) as Object
    tmp = m.create_AnyN_Continuation_k_(_this_runBlocking, _completion)
    tmp.__set_result(invalid)
    tmp.__set_exception(invalid)
    return tmp.doResume_k_()
end function

function delayFunctionTests_lambda_lambda_slambda_doResume_k_() as Dynamic
    suspendResult = m.__get_result()
    while true
                try
            tmp = m.__get_state()
            if __kotlin_identityEquals(tmp, 0) then
                m.__set_exceptionState(1)
                m.__set_state(2)
                suspendResult = delay_J_ContinuationV_k_(0&, m)
                if __kotlin_identityEquals(suspendResult, __get_COROUTINE_SUSPENDED_k_()) then
                    return suspendResult
                end if
                continue while
            else if __kotlin_identityEquals(tmp, 1) then
                throw m.e0
            else if __kotlin_identityEquals(tmp, 2) then
                m._done.value = true
                                return invalid
            end if
        catch e
            m.e0 = e
                        throw m.e0
        end try
    end while
end function

function delayFunctionTests_lambda_lambda_slambda_create_AnyN_Continuation_k_(_this_runBlocking as Dynamic, completion as Object) as Object
    i = delayFunctionTests_lambda_lambda_slambda_create_AnyN_ContinuationAnyNN_k_(m._done, completion)
    i._this_runBlocking = _this_runBlocking
    return i
end function

function delayFunctionTests_lambda_lambda_create_k_() as Object
    this = {}
    this.__type = "delayFunctionTests_lambda_lambda"
    this.__proto = ["delayFunctionTests_lambda_lambda", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = delayFunctionTests_lambda_lambda_invoke_k_
    return this
end function

sub delayFunctionTests_lambda_lambda_invoke_k_()
    done = {value: false}
    runBlocking_CoroutineContext_SuspendFunction1CoroutineScope_k_(invalid, delayFunctionTests_lambda_lambda_slambda_create_AnyN_ContinuationAnyNN_k_(done, invalid))
    assertTrue_Z_StrN_k_(done.value, invalid)
end sub

function delayFunctionTests_lambda_lambda_slambda_1_create_AnyN_ContinuationAnyNN_k_(_done as Dynamic, resultContinuation as Dynamic) as Object
    this = CoroutineImpl_create_ContinuationAnyNN_k_(resultContinuation)
    this._super = {}
    this._super.invoke_AnyN_Continuation_k_ = this.invoke_AnyN_Continuation_k_
    this._super.doResume_k_ = this.doResume_k_
    this._super.create_AnyN_Continuation_k_ = this.create_AnyN_Continuation_k_
    this.__proto = ["delayFunctionTests_lambda_lambda_slambda_1", "SuspendFunction1", this.__proto]
    this.__type = "delayFunctionTests_lambda_lambda_slambda_1"
    this.invoke_AnyN_Continuation_k_ = delayFunctionTests_lambda_lambda_slambda_1_invoke_AnyN_Continuation_k_
    this.doResume_k_ = delayFunctionTests_lambda_lambda_slambda_1_doResume_k_
    this.create_AnyN_Continuation_k_ = delayFunctionTests_lambda_lambda_slambda_1_create_AnyN_Continuation_k_
    this._done = _done
    return this
end function

function delayFunctionTests_lambda_lambda_slambda_1_invoke_AnyN_Continuation_k_(_this_runBlocking as Object, _completion as Object) as Object
    tmp = m.create_AnyN_Continuation_k_(_this_runBlocking, _completion)
    tmp.__set_result(invalid)
    tmp.__set_exception(invalid)
    return tmp.doResume_k_()
end function

function delayFunctionTests_lambda_lambda_slambda_1_doResume_k_() as Dynamic
    suspendResult = m.__get_result()
    while true
                try
            tmp = m.__get_state()
            if __kotlin_identityEquals(tmp, 0) then
                m.__set_exceptionState(1)
                m.__set_state(2)
                suspendResult = delay_J_ContinuationV_k_(-100&, m)
                if __kotlin_identityEquals(suspendResult, __get_COROUTINE_SUSPENDED_k_()) then
                    return suspendResult
                end if
                continue while
            else if __kotlin_identityEquals(tmp, 1) then
                throw m.e0
            else if __kotlin_identityEquals(tmp, 2) then
                m._done.value = true
                                return invalid
            end if
        catch e
            m.e0 = e
                        throw m.e0
        end try
    end while
end function

function delayFunctionTests_lambda_lambda_slambda_1_create_AnyN_Continuation_k_(_this_runBlocking as Dynamic, completion as Object) as Object
    i = delayFunctionTests_lambda_lambda_slambda_1_create_AnyN_ContinuationAnyNN_k_(m._done, completion)
    i._this_runBlocking = _this_runBlocking
    return i
end function

function delayFunctionTests_lambda_lambda_1_create_k_() as Object
    this = {}
    this.__type = "delayFunctionTests_lambda_lambda_1"
    this.__proto = ["delayFunctionTests_lambda_lambda_1", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = delayFunctionTests_lambda_lambda_1_invoke_k_
    return this
end function

sub delayFunctionTests_lambda_lambda_1_invoke_k_()
    done = {value: false}
    runBlocking_CoroutineContext_SuspendFunction1CoroutineScope_k_(invalid, delayFunctionTests_lambda_lambda_slambda_1_create_AnyN_ContinuationAnyNN_k_(done, invalid))
    assertTrue_Z_StrN_k_(done.value, invalid)
end sub

function delayFunctionTests_lambda_lambda_slambda_2_create_ContinuationAnyNN_k_(resultContinuation as Dynamic) as Object
    this = CoroutineImpl_create_ContinuationAnyNN_k_(resultContinuation)
    this._super = {}
    this._super.invoke_AnyN_Continuation_k_ = this.invoke_AnyN_Continuation_k_
    this._super.doResume_k_ = this.doResume_k_
    this._super.create_AnyN_Continuation_k_ = this.create_AnyN_Continuation_k_
    this.__proto = ["delayFunctionTests_lambda_lambda_slambda_2", "SuspendFunction1", this.__proto]
    this.__type = "delayFunctionTests_lambda_lambda_slambda_2"
    this.invoke_AnyN_Continuation_k_ = delayFunctionTests_lambda_lambda_slambda_2_invoke_AnyN_Continuation_k_
    this.doResume_k_ = delayFunctionTests_lambda_lambda_slambda_2_doResume_k_
    this.create_AnyN_Continuation_k_ = delayFunctionTests_lambda_lambda_slambda_2_create_AnyN_Continuation_k_
    return this
end function

function delayFunctionTests_lambda_lambda_slambda_2_invoke_AnyN_Continuation_k_(_this_runBlocking as Object, _completion as Object) as Object
    tmp = m.create_AnyN_Continuation_k_(_this_runBlocking, _completion)
    tmp.__set_result(invalid)
    tmp.__set_exception(invalid)
    return tmp.doResume_k_()
end function

function delayFunctionTests_lambda_lambda_slambda_2_doResume_k_() as Dynamic
    suspendResult = m.__get_result()
    while true
                try
            tmp = m.__get_state()
            if __kotlin_identityEquals(tmp, 0) then
                m.__set_exceptionState(1)
                m.__set_state(2)
                suspendResult = delay_J_ContinuationV_k_(100&, m)
                if __kotlin_identityEquals(suspendResult, __get_COROUTINE_SUSPENDED_k_()) then
                    return suspendResult
                end if
                continue while
            else if __kotlin_identityEquals(tmp, 1) then
                throw m.e0
            else if __kotlin_identityEquals(tmp, 2) then
                                return invalid
            end if
        catch e
            m.e0 = e
                        throw m.e0
        end try
    end while
end function

function delayFunctionTests_lambda_lambda_slambda_2_create_AnyN_Continuation_k_(_this_runBlocking as Dynamic, completion as Object) as Object
    i = delayFunctionTests_lambda_lambda_slambda_2_create_ContinuationAnyNN_k_(completion)
    i._this_runBlocking = _this_runBlocking
    return i
end function

function delayFunctionTests_lambda_lambda_2_create_k_() as Object
    this = {}
    this.__type = "delayFunctionTests_lambda_lambda_2"
    this.__proto = ["delayFunctionTests_lambda_lambda_2", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = delayFunctionTests_lambda_lambda_2_invoke_k_
    return this
end function

sub delayFunctionTests_lambda_lambda_2_invoke_k_()
    timer = CreateObject("roTimespan")
    timer.mark()
    runBlocking_CoroutineContext_SuspendFunction1CoroutineScope_k_(invalid, delayFunctionTests_lambda_lambda_slambda_2_create_ContinuationAnyNN_k_(invalid))
    elapsed = timer.totalMilliseconds()
    assertTrue_Z_StrN_k_(elapsed >= 90, "Expected >= 90ms, got " + __kotlin_numToStr_I_k_(elapsed))
    assertTrue_Z_StrN_k_(elapsed < 500, "Expected < 500ms, got " + __kotlin_numToStr_I_k_(elapsed))
end sub

function delayFunctionTests_lambda_lambda_slambda_3_create_ContinuationAnyNN_k_(resultContinuation as Dynamic) as Object
    this = CoroutineImpl_create_ContinuationAnyNN_k_(resultContinuation)
    this._super = {}
    this._super.invoke_AnyN_Continuation_k_ = this.invoke_AnyN_Continuation_k_
    this._super.doResume_k_ = this.doResume_k_
    this._super.create_AnyN_Continuation_k_ = this.create_AnyN_Continuation_k_
    this.__proto = ["delayFunctionTests_lambda_lambda_slambda_3", "SuspendFunction1", this.__proto]
    this.__type = "delayFunctionTests_lambda_lambda_slambda_3"
    this.invoke_AnyN_Continuation_k_ = delayFunctionTests_lambda_lambda_slambda_3_invoke_AnyN_Continuation_k_
    this.doResume_k_ = delayFunctionTests_lambda_lambda_slambda_3_doResume_k_
    this.create_AnyN_Continuation_k_ = delayFunctionTests_lambda_lambda_slambda_3_create_AnyN_Continuation_k_
    return this
end function

function delayFunctionTests_lambda_lambda_slambda_3_invoke_AnyN_Continuation_k_(_this_runBlocking as Object, _completion as Object) as Object
    tmp = m.create_AnyN_Continuation_k_(_this_runBlocking, _completion)
    tmp.__set_result(invalid)
    tmp.__set_exception(invalid)
    return tmp.doResume_k_()
end function

function delayFunctionTests_lambda_lambda_slambda_3_doResume_k_() as Dynamic
    suspendResult = m.__get_result()
    while true
                try
            tmp = m.__get_state()
            if __kotlin_identityEquals(tmp, 0) then
                m.__set_exceptionState(1)
                m.__set_state(2)
                suspendResult = delay_J_ContinuationV_k_(50&, m)
                if __kotlin_identityEquals(suspendResult, __get_COROUTINE_SUSPENDED_k_()) then
                    return suspendResult
                end if
                continue while
            else if __kotlin_identityEquals(tmp, 1) then
                throw m.e0
            else if __kotlin_identityEquals(tmp, 2) then
                m.__set_state(3)
                suspendResult = delay_J_ContinuationV_k_(50&, m)
                if __kotlin_identityEquals(suspendResult, __get_COROUTINE_SUSPENDED_k_()) then
                    return suspendResult
                end if
                continue while
            else if __kotlin_identityEquals(tmp, 3) then
                m.__set_state(4)
                suspendResult = delay_J_ContinuationV_k_(50&, m)
                if __kotlin_identityEquals(suspendResult, __get_COROUTINE_SUSPENDED_k_()) then
                    return suspendResult
                end if
                continue while
            else if __kotlin_identityEquals(tmp, 4) then
                                return invalid
            end if
        catch e
            m.e0 = e
                        throw m.e0
        end try
    end while
end function

function delayFunctionTests_lambda_lambda_slambda_3_create_AnyN_Continuation_k_(_this_runBlocking as Dynamic, completion as Object) as Object
    i = delayFunctionTests_lambda_lambda_slambda_3_create_ContinuationAnyNN_k_(completion)
    i._this_runBlocking = _this_runBlocking
    return i
end function

function delayFunctionTests_lambda_lambda_3_create_k_() as Object
    this = {}
    this.__type = "delayFunctionTests_lambda_lambda_3"
    this.__proto = ["delayFunctionTests_lambda_lambda_3", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = delayFunctionTests_lambda_lambda_3_invoke_k_
    return this
end function

sub delayFunctionTests_lambda_lambda_3_invoke_k_()
    timer = CreateObject("roTimespan")
    timer.mark()
    runBlocking_CoroutineContext_SuspendFunction1CoroutineScope_k_(invalid, delayFunctionTests_lambda_lambda_slambda_3_create_ContinuationAnyNN_k_(invalid))
    elapsed = timer.totalMilliseconds()
    assertTrue_Z_StrN_k_(elapsed >= 140, "Expected >= 140ms for 3x50ms delays, got " + __kotlin_numToStr_I_k_(elapsed))
end sub

function delayFunctionTests_lambda_lambda_slambda_4_create_AnyN_ContinuationAnyNN_k_(_state as Dynamic, resultContinuation as Dynamic) as Object
    this = CoroutineImpl_create_ContinuationAnyNN_k_(resultContinuation)
    this._super = {}
    this._super.invoke_AnyN_Continuation_k_ = this.invoke_AnyN_Continuation_k_
    this._super.doResume_k_ = this.doResume_k_
    this._super.create_AnyN_Continuation_k_ = this.create_AnyN_Continuation_k_
    this.__proto = ["delayFunctionTests_lambda_lambda_slambda_4", "SuspendFunction1", this.__proto]
    this.__type = "delayFunctionTests_lambda_lambda_slambda_4"
    this.invoke_AnyN_Continuation_k_ = delayFunctionTests_lambda_lambda_slambda_4_invoke_AnyN_Continuation_k_
    this.doResume_k_ = delayFunctionTests_lambda_lambda_slambda_4_doResume_k_
    this.create_AnyN_Continuation_k_ = delayFunctionTests_lambda_lambda_slambda_4_create_AnyN_Continuation_k_
    this._state = _state
    return this
end function

function delayFunctionTests_lambda_lambda_slambda_4_invoke_AnyN_Continuation_k_(_this_runBlocking as Object, _completion as Object) as Object
    tmp = m.create_AnyN_Continuation_k_(_this_runBlocking, _completion)
    tmp.__set_result(invalid)
    tmp.__set_exception(invalid)
    return tmp.doResume_k_()
end function

function delayFunctionTests_lambda_lambda_slambda_4_doResume_k_() as Dynamic
    suspendResult = m.__get_result()
    while true
                try
            tmp = m.__get_state()
            if __kotlin_identityEquals(tmp, 0) then
                m.__set_exceptionState(1)
                m._state.value = 1
                m.__set_state(2)
                suspendResult = delay_J_ContinuationV_k_(10&, m)
                if __kotlin_identityEquals(suspendResult, __get_COROUTINE_SUSPENDED_k_()) then
                    return suspendResult
                end if
                continue while
            else if __kotlin_identityEquals(tmp, 1) then
                throw m.e0
            else if __kotlin_identityEquals(tmp, 2) then
                m._state.value = 2
                                return invalid
            end if
        catch e
            m.e0 = e
                        throw m.e0
        end try
    end while
end function

function delayFunctionTests_lambda_lambda_slambda_4_create_AnyN_Continuation_k_(_this_runBlocking as Dynamic, completion as Object) as Object
    i = delayFunctionTests_lambda_lambda_slambda_4_create_AnyN_ContinuationAnyNN_k_(m._state, completion)
    i._this_runBlocking = _this_runBlocking
    return i
end function

function delayFunctionTests_lambda_lambda_4_create_k_() as Object
    this = {}
    this.__type = "delayFunctionTests_lambda_lambda_4"
    this.__proto = ["delayFunctionTests_lambda_lambda_4", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = delayFunctionTests_lambda_lambda_4_invoke_k_
    return this
end function

sub delayFunctionTests_lambda_lambda_4_invoke_k_()
    state = {value: 0}
    runBlocking_CoroutineContext_SuspendFunction1CoroutineScope_k_(invalid, delayFunctionTests_lambda_lambda_slambda_4_create_AnyN_ContinuationAnyNN_k_(state, invalid))
    assertEquals_AnyN_AnyN_StrN_k_(2, state.value, invalid)
end sub

function delayFunctionTests_lambda_create_k_() as Object
    this = {}
    this.__type = "delayFunctionTests_lambda"
    this.__proto = ["delayFunctionTests_lambda", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = delayFunctionTests_lambda_invoke_AnyN_k_
    return this
end function

sub delayFunctionTests_lambda_invoke_AnyN_k_(_this_suite as Object)
    _this_suite.test_Str_Function0V_k_("delay(0) returns immediately", delayFunctionTests_lambda_lambda_create_k_())
    _this_suite.test_Str_Function0V_k_("delay(negative) returns immediately", delayFunctionTests_lambda_lambda_1_create_k_())
    _this_suite.test_Str_Function0V_k_("delay waits approximately correct time", delayFunctionTests_lambda_lambda_2_create_k_())
    _this_suite.test_Str_Function0V_k_("multiple sequential delays", delayFunctionTests_lambda_lambda_3_create_k_())
    _this_suite.test_Str_Function0V_k_("delay suspends and resumes", delayFunctionTests_lambda_lambda_4_create_k_())
end sub

function yieldFunctionTests_lambda_lambda_slambda_create_AnyN_ContinuationAnyNN_k_(_done as Dynamic, resultContinuation as Dynamic) as Object
    this = CoroutineImpl_create_ContinuationAnyNN_k_(resultContinuation)
    this._super = {}
    this._super.invoke_AnyN_Continuation_k_ = this.invoke_AnyN_Continuation_k_
    this._super.doResume_k_ = this.doResume_k_
    this._super.create_AnyN_Continuation_k_ = this.create_AnyN_Continuation_k_
    this.__proto = ["yieldFunctionTests_lambda_lambda_slambda", "SuspendFunction1", this.__proto]
    this.__type = "yieldFunctionTests_lambda_lambda_slambda"
    this.invoke_AnyN_Continuation_k_ = yieldFunctionTests_lambda_lambda_slambda_invoke_AnyN_Continuation_k_
    this.doResume_k_ = yieldFunctionTests_lambda_lambda_slambda_doResume_k_
    this.create_AnyN_Continuation_k_ = yieldFunctionTests_lambda_lambda_slambda_create_AnyN_Continuation_k_
    this._done = _done
    return this
end function

function yieldFunctionTests_lambda_lambda_slambda_invoke_AnyN_Continuation_k_(_this_runBlocking as Object, _completion as Object) as Object
    tmp = m.create_AnyN_Continuation_k_(_this_runBlocking, _completion)
    tmp.__set_result(invalid)
    tmp.__set_exception(invalid)
    return tmp.doResume_k_()
end function

function yieldFunctionTests_lambda_lambda_slambda_doResume_k_() as Dynamic
    suspendResult = m.__get_result()
    while true
                try
            tmp = m.__get_state()
            if __kotlin_identityEquals(tmp, 0) then
                m.__set_exceptionState(1)
                m.__set_state(2)
                suspendResult = yield_ContinuationV_k_(m)
                if __kotlin_identityEquals(suspendResult, __get_COROUTINE_SUSPENDED_k_()) then
                    return suspendResult
                end if
                continue while
            else if __kotlin_identityEquals(tmp, 1) then
                throw m.e0
            else if __kotlin_identityEquals(tmp, 2) then
                m._done.value = true
                                return invalid
            end if
        catch e
            m.e0 = e
                        throw m.e0
        end try
    end while
end function

function yieldFunctionTests_lambda_lambda_slambda_create_AnyN_Continuation_k_(_this_runBlocking as Dynamic, completion as Object) as Object
    i = yieldFunctionTests_lambda_lambda_slambda_create_AnyN_ContinuationAnyNN_k_(m._done, completion)
    i._this_runBlocking = _this_runBlocking
    return i
end function

function yieldFunctionTests_lambda_lambda_create_k_() as Object
    this = {}
    this.__type = "yieldFunctionTests_lambda_lambda"
    this.__proto = ["yieldFunctionTests_lambda_lambda", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = yieldFunctionTests_lambda_lambda_invoke_k_
    return this
end function

sub yieldFunctionTests_lambda_lambda_invoke_k_()
    done = {value: false}
    runBlocking_CoroutineContext_SuspendFunction1CoroutineScope_k_(invalid, yieldFunctionTests_lambda_lambda_slambda_create_AnyN_ContinuationAnyNN_k_(done, invalid))
    assertTrue_Z_StrN_k_(done.value, invalid)
end sub

function yieldFunctionTests_lambda_lambda_slambda_slambda_create_MutableListStr_ContinuationAnyNN_k_(_results as Object, resultContinuation as Dynamic) as Object
    this = CoroutineImpl_create_ContinuationAnyNN_k_(resultContinuation)
    this._super = {}
    this._super.invoke_AnyN_Continuation_k_ = this.invoke_AnyN_Continuation_k_
    this._super.doResume_k_ = this.doResume_k_
    this._super.create_AnyN_Continuation_k_ = this.create_AnyN_Continuation_k_
    this.__proto = ["yieldFunctionTests_lambda_lambda_slambda_slambda", "SuspendFunction1", this.__proto]
    this.__type = "yieldFunctionTests_lambda_lambda_slambda_slambda"
    this.invoke_AnyN_Continuation_k_ = yieldFunctionTests_lambda_lambda_slambda_slambda_invoke_AnyN_Continuation_k_
    this.doResume_k_ = yieldFunctionTests_lambda_lambda_slambda_slambda_doResume_k_
    this.create_AnyN_Continuation_k_ = yieldFunctionTests_lambda_lambda_slambda_slambda_create_AnyN_Continuation_k_
    this._results = _results
    return this
end function

function yieldFunctionTests_lambda_lambda_slambda_slambda_invoke_AnyN_Continuation_k_(_this_launch as Object, _completion as Object) as Object
    tmp = m.create_AnyN_Continuation_k_(_this_launch, _completion)
    tmp.__set_result(invalid)
    tmp.__set_exception(invalid)
    return tmp.doResume_k_()
end function

function yieldFunctionTests_lambda_lambda_slambda_slambda_doResume_k_() as Dynamic
    suspendResult = m.__get_result()
    while true
                try
            tmp = m.__get_state()
            if __kotlin_identityEquals(tmp, 0) then
                m.__set_exceptionState(1)
                m._results.add_AnyN_k_("A1")
                m.__set_state(2)
                suspendResult = yield_ContinuationV_k_(m)
                if __kotlin_identityEquals(suspendResult, __get_COROUTINE_SUSPENDED_k_()) then
                    return suspendResult
                end if
                continue while
            else if __kotlin_identityEquals(tmp, 1) then
                throw m.e0
            else if __kotlin_identityEquals(tmp, 2) then
                m._results.add_AnyN_k_("A2")
                                return invalid
            end if
        catch e
            m.e0 = e
                        throw m.e0
        end try
    end while
end function

function yieldFunctionTests_lambda_lambda_slambda_slambda_create_AnyN_Continuation_k_(_this_launch as Dynamic, completion as Object) as Object
    i = yieldFunctionTests_lambda_lambda_slambda_slambda_create_MutableListStr_ContinuationAnyNN_k_(m._results, completion)
    i._this_launch = _this_launch
    return i
end function

function yieldFunctionTests_lambda_lambda_slambda_slambda_1_create_MutableListStr_ContinuationAnyNN_k_(_results as Object, resultContinuation as Dynamic) as Object
    this = CoroutineImpl_create_ContinuationAnyNN_k_(resultContinuation)
    this._super = {}
    this._super.invoke_AnyN_Continuation_k_ = this.invoke_AnyN_Continuation_k_
    this._super.doResume_k_ = this.doResume_k_
    this._super.create_AnyN_Continuation_k_ = this.create_AnyN_Continuation_k_
    this.__proto = ["yieldFunctionTests_lambda_lambda_slambda_slambda_1", "SuspendFunction1", this.__proto]
    this.__type = "yieldFunctionTests_lambda_lambda_slambda_slambda_1"
    this.invoke_AnyN_Continuation_k_ = yieldFunctionTests_lambda_lambda_slambda_slambda_1_invoke_AnyN_Continuation_k_
    this.doResume_k_ = yieldFunctionTests_lambda_lambda_slambda_slambda_1_doResume_k_
    this.create_AnyN_Continuation_k_ = yieldFunctionTests_lambda_lambda_slambda_slambda_1_create_AnyN_Continuation_k_
    this._results = _results
    return this
end function

function yieldFunctionTests_lambda_lambda_slambda_slambda_1_invoke_AnyN_Continuation_k_(_this_launch as Object, _completion as Object) as Object
    tmp = m.create_AnyN_Continuation_k_(_this_launch, _completion)
    tmp.__set_result(invalid)
    tmp.__set_exception(invalid)
    return tmp.doResume_k_()
end function

function yieldFunctionTests_lambda_lambda_slambda_slambda_1_doResume_k_() as Dynamic
    suspendResult = m.__get_result()
    while true
                try
            tmp = m.__get_state()
            if __kotlin_identityEquals(tmp, 0) then
                m.__set_exceptionState(1)
                m._results.add_AnyN_k_("B1")
                m.__set_state(2)
                suspendResult = yield_ContinuationV_k_(m)
                if __kotlin_identityEquals(suspendResult, __get_COROUTINE_SUSPENDED_k_()) then
                    return suspendResult
                end if
                continue while
            else if __kotlin_identityEquals(tmp, 1) then
                throw m.e0
            else if __kotlin_identityEquals(tmp, 2) then
                m._results.add_AnyN_k_("B2")
                                return invalid
            end if
        catch e
            m.e0 = e
                        throw m.e0
        end try
    end while
end function

function yieldFunctionTests_lambda_lambda_slambda_slambda_1_create_AnyN_Continuation_k_(_this_launch as Dynamic, completion as Object) as Object
    i = yieldFunctionTests_lambda_lambda_slambda_slambda_1_create_MutableListStr_ContinuationAnyNN_k_(m._results, completion)
    i._this_launch = _this_launch
    return i
end function

function yieldFunctionTests_lambda_lambda_slambda_1_create_MutableListStr_ContinuationAnyNN_k_(_results as Object, resultContinuation as Dynamic) as Object
    this = CoroutineImpl_create_ContinuationAnyNN_k_(resultContinuation)
    this._super = {}
    this._super.invoke_AnyN_Continuation_k_ = this.invoke_AnyN_Continuation_k_
    this._super.doResume_k_ = this.doResume_k_
    this._super.create_AnyN_Continuation_k_ = this.create_AnyN_Continuation_k_
    this.__proto = ["yieldFunctionTests_lambda_lambda_slambda_1", "SuspendFunction1", this.__proto]
    this.__type = "yieldFunctionTests_lambda_lambda_slambda_1"
    this.invoke_AnyN_Continuation_k_ = yieldFunctionTests_lambda_lambda_slambda_1_invoke_AnyN_Continuation_k_
    this.doResume_k_ = yieldFunctionTests_lambda_lambda_slambda_1_doResume_k_
    this.create_AnyN_Continuation_k_ = yieldFunctionTests_lambda_lambda_slambda_1_create_AnyN_Continuation_k_
    this._results = _results
    return this
end function

function yieldFunctionTests_lambda_lambda_slambda_1_invoke_AnyN_Continuation_k_(_this_runBlocking as Object, _completion as Object) as Object
    tmp = m.create_AnyN_Continuation_k_(_this_runBlocking, _completion)
    tmp.__set_result(invalid)
    tmp.__set_exception(invalid)
    return tmp.doResume_k_()
end function

function yieldFunctionTests_lambda_lambda_slambda_1_doResume_k_() as Dynamic
    suspendResult = m.__get_result()
    while true
                try
            tmp = m.__get_state()
            if __kotlin_identityEquals(tmp, 0) then
                m.__set_exceptionState(1)
                launch_rCoroutineScope_CoroutineContext_SuspendFunction1CoroutineScopeV_k_(m._this_runBlocking, invalid, yieldFunctionTests_lambda_lambda_slambda_slambda_create_MutableListStr_ContinuationAnyNN_k_(m._results, invalid))
                launch_rCoroutineScope_CoroutineContext_SuspendFunction1CoroutineScopeV_k_(m._this_runBlocking, invalid, yieldFunctionTests_lambda_lambda_slambda_slambda_1_create_MutableListStr_ContinuationAnyNN_k_(m._results, invalid))
                m.__set_state(2)
                suspendResult = yield_ContinuationV_k_(m)
                if __kotlin_identityEquals(suspendResult, __get_COROUTINE_SUSPENDED_k_()) then
                    return suspendResult
                end if
                continue while
            else if __kotlin_identityEquals(tmp, 1) then
                throw m.e0
            else if __kotlin_identityEquals(tmp, 2) then
                m.__set_state(3)
                suspendResult = delay_J_ContinuationV_k_(10&, m)
                if __kotlin_identityEquals(suspendResult, __get_COROUTINE_SUSPENDED_k_()) then
                    return suspendResult
                end if
                continue while
            else if __kotlin_identityEquals(tmp, 3) then
                                return invalid
            end if
        catch e
            m.e0 = e
                        throw m.e0
        end try
    end while
end function

function yieldFunctionTests_lambda_lambda_slambda_1_create_AnyN_Continuation_k_(_this_runBlocking as Dynamic, completion as Object) as Object
    i = yieldFunctionTests_lambda_lambda_slambda_1_create_MutableListStr_ContinuationAnyNN_k_(m._results, completion)
    i._this_runBlocking = _this_runBlocking
    return i
end function

function yieldFunctionTests_lambda_lambda_1_create_k_() as Object
    this = {}
    this.__type = "yieldFunctionTests_lambda_lambda_1"
    this.__proto = ["yieldFunctionTests_lambda_lambda_1", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = yieldFunctionTests_lambda_lambda_1_invoke_k_
    return this
end function

sub yieldFunctionTests_lambda_lambda_1_invoke_k_()
    results = mutableListOf_k_()
    runBlocking_CoroutineContext_SuspendFunction1CoroutineScope_k_(invalid, yieldFunctionTests_lambda_lambda_slambda_1_create_MutableListStr_ContinuationAnyNN_k_(results, invalid))
    assertTrue_Z_StrN_k_(results.contains_AnyN_k_("A1"), "A1 should be in results")
    assertTrue_Z_StrN_k_(results.contains_AnyN_k_("B1"), "B1 should be in results")
    assertTrue_Z_StrN_k_(results.contains_AnyN_k_("A2"), "A2 should be in results")
    assertTrue_Z_StrN_k_(results.contains_AnyN_k_("B2"), "B2 should be in results")
end sub

function yieldFunctionTests_lambda_create_k_() as Object
    this = {}
    this.__type = "yieldFunctionTests_lambda"
    this.__proto = ["yieldFunctionTests_lambda", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = yieldFunctionTests_lambda_invoke_AnyN_k_
    return this
end function

sub yieldFunctionTests_lambda_invoke_AnyN_k_(_this_suite as Object)
    _this_suite.test_Str_Function0V_k_("yield returns to runBlocking", yieldFunctionTests_lambda_lambda_create_k_())
    _this_suite.test_Str_Function0V_k_("yield allows other coroutines to run", yieldFunctionTests_lambda_lambda_1_create_k_())
end sub

function coroutineQueueTests_lambda_lambda_create_k_() as Object
    this = {}
    this.__type = "coroutineQueueTests_lambda_lambda"
    this.__proto = ["coroutineQueueTests_lambda_lambda", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = coroutineQueueTests_lambda_lambda_invoke_k_
    return this
end function

sub coroutineQueueTests_lambda_lambda_invoke_k_()
    count = processCoroutineQueue_k_()
    assertTrue_Z_StrN_k_(count >= 0, invalid)
end sub

function coroutineQueueTests_lambda_lambda_slambda_slambda_create_AnyN_ContinuationAnyNN_k_(_executed as Dynamic, resultContinuation as Dynamic) as Object
    this = CoroutineImpl_create_ContinuationAnyNN_k_(resultContinuation)
    this._super = {}
    this._super.invoke_AnyN_Continuation_k_ = this.invoke_AnyN_Continuation_k_
    this._super.doResume_k_ = this.doResume_k_
    this._super.create_AnyN_Continuation_k_ = this.create_AnyN_Continuation_k_
    this.__proto = ["coroutineQueueTests_lambda_lambda_slambda_slambda", "SuspendFunction1", this.__proto]
    this.__type = "coroutineQueueTests_lambda_lambda_slambda_slambda"
    this.invoke_AnyN_Continuation_k_ = coroutineQueueTests_lambda_lambda_slambda_slambda_invoke_AnyN_Continuation_k_
    this.doResume_k_ = coroutineQueueTests_lambda_lambda_slambda_slambda_doResume_k_
    this.create_AnyN_Continuation_k_ = coroutineQueueTests_lambda_lambda_slambda_slambda_create_AnyN_Continuation_k_
    this._executed = _executed
    return this
end function

function coroutineQueueTests_lambda_lambda_slambda_slambda_invoke_AnyN_Continuation_k_(_this_launch as Object, _completion as Object) as Object
    tmp = m.create_AnyN_Continuation_k_(_this_launch, _completion)
    tmp.__set_result(invalid)
    tmp.__set_exception(invalid)
    return tmp.doResume_k_()
end function

function coroutineQueueTests_lambda_lambda_slambda_slambda_doResume_k_() as Dynamic
    suspendResult = m.__get_result()
    while true
                try
            tmp = m.__get_state()
            if __kotlin_identityEquals(tmp, 0) then
                m.__set_exceptionState(1)
                m._executed.value = true

                                return invalid
            else if __kotlin_identityEquals(tmp, 1) then
                throw e
            end if
        catch e
                        throw e
        end try
    end while
end function

function coroutineQueueTests_lambda_lambda_slambda_slambda_create_AnyN_Continuation_k_(_this_launch as Dynamic, completion as Object) as Object
    i = coroutineQueueTests_lambda_lambda_slambda_slambda_create_AnyN_ContinuationAnyNN_k_(m._executed, completion)
    i._this_launch = _this_launch
    return i
end function

function coroutineQueueTests_lambda_lambda_slambda_create_AnyN_ContinuationAnyNN_k_(_executed as Dynamic, resultContinuation as Dynamic) as Object
    this = CoroutineImpl_create_ContinuationAnyNN_k_(resultContinuation)
    this._super = {}
    this._super.invoke_AnyN_Continuation_k_ = this.invoke_AnyN_Continuation_k_
    this._super.doResume_k_ = this.doResume_k_
    this._super.create_AnyN_Continuation_k_ = this.create_AnyN_Continuation_k_
    this.__proto = ["coroutineQueueTests_lambda_lambda_slambda", "SuspendFunction1", this.__proto]
    this.__type = "coroutineQueueTests_lambda_lambda_slambda"
    this.invoke_AnyN_Continuation_k_ = coroutineQueueTests_lambda_lambda_slambda_invoke_AnyN_Continuation_k_
    this.doResume_k_ = coroutineQueueTests_lambda_lambda_slambda_doResume_k_
    this.create_AnyN_Continuation_k_ = coroutineQueueTests_lambda_lambda_slambda_create_AnyN_Continuation_k_
    this._executed = _executed
    return this
end function

function coroutineQueueTests_lambda_lambda_slambda_invoke_AnyN_Continuation_k_(_this_runBlocking as Object, _completion as Object) as Object
    tmp = m.create_AnyN_Continuation_k_(_this_runBlocking, _completion)
    tmp.__set_result(invalid)
    tmp.__set_exception(invalid)
    return tmp.doResume_k_()
end function

function coroutineQueueTests_lambda_lambda_slambda_doResume_k_() as Dynamic
    suspendResult = m.__get_result()
    while true
                try
            tmp = m.__get_state()
            if __kotlin_identityEquals(tmp, 0) then
                m.__set_exceptionState(1)
                launch_rCoroutineScope_CoroutineContext_SuspendFunction1CoroutineScopeV_k_(m._this_runBlocking, invalid, coroutineQueueTests_lambda_lambda_slambda_slambda_create_AnyN_ContinuationAnyNN_k_(m._executed, invalid))
                m.__set_state(2)
                suspendResult = yield_ContinuationV_k_(m)
                if __kotlin_identityEquals(suspendResult, __get_COROUTINE_SUSPENDED_k_()) then
                    return suspendResult
                end if
                continue while
            else if __kotlin_identityEquals(tmp, 1) then
                throw m.e0
            else if __kotlin_identityEquals(tmp, 2) then
                m.__set_state(3)
                suspendResult = delay_J_ContinuationV_k_(10&, m)
                if __kotlin_identityEquals(suspendResult, __get_COROUTINE_SUSPENDED_k_()) then
                    return suspendResult
                end if
                continue while
            else if __kotlin_identityEquals(tmp, 3) then
                                return invalid
            end if
        catch e
            m.e0 = e
                        throw m.e0
        end try
    end while
end function

function coroutineQueueTests_lambda_lambda_slambda_create_AnyN_Continuation_k_(_this_runBlocking as Dynamic, completion as Object) as Object
    i = coroutineQueueTests_lambda_lambda_slambda_create_AnyN_ContinuationAnyNN_k_(m._executed, completion)
    i._this_runBlocking = _this_runBlocking
    return i
end function

function coroutineQueueTests_lambda_lambda_1_create_k_() as Object
    this = {}
    this.__type = "coroutineQueueTests_lambda_lambda_1"
    this.__proto = ["coroutineQueueTests_lambda_lambda_1", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = coroutineQueueTests_lambda_lambda_1_invoke_k_
    return this
end function

sub coroutineQueueTests_lambda_lambda_1_invoke_k_()
    executed = {value: false}
    runBlocking_CoroutineContext_SuspendFunction1CoroutineScope_k_(invalid, coroutineQueueTests_lambda_lambda_slambda_create_AnyN_ContinuationAnyNN_k_(executed, invalid))
    assertTrue_Z_StrN_k_(executed.value, "Launched coroutine should have executed")
end sub

function coroutineQueueTests_lambda_lambda_slambda_slambda_1_create_AnyN_ContinuationAnyNN_k_(_count as Dynamic, resultContinuation as Dynamic) as Object
    this = CoroutineImpl_create_ContinuationAnyNN_k_(resultContinuation)
    this._super = {}
    this._super.invoke_AnyN_Continuation_k_ = this.invoke_AnyN_Continuation_k_
    this._super.doResume_k_ = this.doResume_k_
    this._super.create_AnyN_Continuation_k_ = this.create_AnyN_Continuation_k_
    this.__proto = ["coroutineQueueTests_lambda_lambda_slambda_slambda_1", "SuspendFunction1", this.__proto]
    this.__type = "coroutineQueueTests_lambda_lambda_slambda_slambda_1"
    this.invoke_AnyN_Continuation_k_ = coroutineQueueTests_lambda_lambda_slambda_slambda_1_invoke_AnyN_Continuation_k_
    this.doResume_k_ = coroutineQueueTests_lambda_lambda_slambda_slambda_1_doResume_k_
    this.create_AnyN_Continuation_k_ = coroutineQueueTests_lambda_lambda_slambda_slambda_1_create_AnyN_Continuation_k_
    this._count = _count
    return this
end function

function coroutineQueueTests_lambda_lambda_slambda_slambda_1_invoke_AnyN_Continuation_k_(_this_launch as Object, _completion as Object) as Object
    tmp = m.create_AnyN_Continuation_k_(_this_launch, _completion)
    tmp.__set_result(invalid)
    tmp.__set_exception(invalid)
    return tmp.doResume_k_()
end function

function coroutineQueueTests_lambda_lambda_slambda_slambda_1_doResume_k_() as Dynamic
    suspendResult = m.__get_result()
    while true
                try
            tmp = m.__get_state()
            if __kotlin_identityEquals(tmp, 0) then
                m.__set_exceptionState(1)
                m._count.value = (m._count.value + 1)

                                return invalid
            else if __kotlin_identityEquals(tmp, 1) then
                throw e
            end if
        catch e
                        throw e
        end try
    end while
end function

function coroutineQueueTests_lambda_lambda_slambda_slambda_1_create_AnyN_Continuation_k_(_this_launch as Dynamic, completion as Object) as Object
    i = coroutineQueueTests_lambda_lambda_slambda_slambda_1_create_AnyN_ContinuationAnyNN_k_(m._count, completion)
    i._this_launch = _this_launch
    return i
end function

function coroutineQueueTests_lambda_lambda_slambda_slambda_2_create_AnyN_ContinuationAnyNN_k_(_count as Dynamic, resultContinuation as Dynamic) as Object
    this = CoroutineImpl_create_ContinuationAnyNN_k_(resultContinuation)
    this._super = {}
    this._super.invoke_AnyN_Continuation_k_ = this.invoke_AnyN_Continuation_k_
    this._super.doResume_k_ = this.doResume_k_
    this._super.create_AnyN_Continuation_k_ = this.create_AnyN_Continuation_k_
    this.__proto = ["coroutineQueueTests_lambda_lambda_slambda_slambda_2", "SuspendFunction1", this.__proto]
    this.__type = "coroutineQueueTests_lambda_lambda_slambda_slambda_2"
    this.invoke_AnyN_Continuation_k_ = coroutineQueueTests_lambda_lambda_slambda_slambda_2_invoke_AnyN_Continuation_k_
    this.doResume_k_ = coroutineQueueTests_lambda_lambda_slambda_slambda_2_doResume_k_
    this.create_AnyN_Continuation_k_ = coroutineQueueTests_lambda_lambda_slambda_slambda_2_create_AnyN_Continuation_k_
    this._count = _count
    return this
end function

function coroutineQueueTests_lambda_lambda_slambda_slambda_2_invoke_AnyN_Continuation_k_(_this_launch as Object, _completion as Object) as Object
    tmp = m.create_AnyN_Continuation_k_(_this_launch, _completion)
    tmp.__set_result(invalid)
    tmp.__set_exception(invalid)
    return tmp.doResume_k_()
end function

function coroutineQueueTests_lambda_lambda_slambda_slambda_2_doResume_k_() as Dynamic
    suspendResult = m.__get_result()
    while true
                try
            tmp = m.__get_state()
            if __kotlin_identityEquals(tmp, 0) then
                m.__set_exceptionState(1)
                m._count.value = (m._count.value + 1)

                                return invalid
            else if __kotlin_identityEquals(tmp, 1) then
                throw e
            end if
        catch e
                        throw e
        end try
    end while
end function

function coroutineQueueTests_lambda_lambda_slambda_slambda_2_create_AnyN_Continuation_k_(_this_launch as Dynamic, completion as Object) as Object
    i = coroutineQueueTests_lambda_lambda_slambda_slambda_2_create_AnyN_ContinuationAnyNN_k_(m._count, completion)
    i._this_launch = _this_launch
    return i
end function

function coroutineQueueTests_lambda_lambda_slambda_slambda_3_create_AnyN_ContinuationAnyNN_k_(_count as Dynamic, resultContinuation as Dynamic) as Object
    this = CoroutineImpl_create_ContinuationAnyNN_k_(resultContinuation)
    this._super = {}
    this._super.invoke_AnyN_Continuation_k_ = this.invoke_AnyN_Continuation_k_
    this._super.doResume_k_ = this.doResume_k_
    this._super.create_AnyN_Continuation_k_ = this.create_AnyN_Continuation_k_
    this.__proto = ["coroutineQueueTests_lambda_lambda_slambda_slambda_3", "SuspendFunction1", this.__proto]
    this.__type = "coroutineQueueTests_lambda_lambda_slambda_slambda_3"
    this.invoke_AnyN_Continuation_k_ = coroutineQueueTests_lambda_lambda_slambda_slambda_3_invoke_AnyN_Continuation_k_
    this.doResume_k_ = coroutineQueueTests_lambda_lambda_slambda_slambda_3_doResume_k_
    this.create_AnyN_Continuation_k_ = coroutineQueueTests_lambda_lambda_slambda_slambda_3_create_AnyN_Continuation_k_
    this._count = _count
    return this
end function

function coroutineQueueTests_lambda_lambda_slambda_slambda_3_invoke_AnyN_Continuation_k_(_this_launch as Object, _completion as Object) as Object
    tmp = m.create_AnyN_Continuation_k_(_this_launch, _completion)
    tmp.__set_result(invalid)
    tmp.__set_exception(invalid)
    return tmp.doResume_k_()
end function

function coroutineQueueTests_lambda_lambda_slambda_slambda_3_doResume_k_() as Dynamic
    suspendResult = m.__get_result()
    while true
                try
            tmp = m.__get_state()
            if __kotlin_identityEquals(tmp, 0) then
                m.__set_exceptionState(1)
                m._count.value = (m._count.value + 1)

                                return invalid
            else if __kotlin_identityEquals(tmp, 1) then
                throw e
            end if
        catch e
                        throw e
        end try
    end while
end function

function coroutineQueueTests_lambda_lambda_slambda_slambda_3_create_AnyN_Continuation_k_(_this_launch as Dynamic, completion as Object) as Object
    i = coroutineQueueTests_lambda_lambda_slambda_slambda_3_create_AnyN_ContinuationAnyNN_k_(m._count, completion)
    i._this_launch = _this_launch
    return i
end function

function coroutineQueueTests_lambda_lambda_slambda_1_create_AnyN_ContinuationAnyNN_k_(_count as Dynamic, resultContinuation as Dynamic) as Object
    this = CoroutineImpl_create_ContinuationAnyNN_k_(resultContinuation)
    this._super = {}
    this._super.invoke_AnyN_Continuation_k_ = this.invoke_AnyN_Continuation_k_
    this._super.doResume_k_ = this.doResume_k_
    this._super.create_AnyN_Continuation_k_ = this.create_AnyN_Continuation_k_
    this.__proto = ["coroutineQueueTests_lambda_lambda_slambda_1", "SuspendFunction1", this.__proto]
    this.__type = "coroutineQueueTests_lambda_lambda_slambda_1"
    this.invoke_AnyN_Continuation_k_ = coroutineQueueTests_lambda_lambda_slambda_1_invoke_AnyN_Continuation_k_
    this.doResume_k_ = coroutineQueueTests_lambda_lambda_slambda_1_doResume_k_
    this.create_AnyN_Continuation_k_ = coroutineQueueTests_lambda_lambda_slambda_1_create_AnyN_Continuation_k_
    this._count = _count
    return this
end function

function coroutineQueueTests_lambda_lambda_slambda_1_invoke_AnyN_Continuation_k_(_this_runBlocking as Object, _completion as Object) as Object
    tmp = m.create_AnyN_Continuation_k_(_this_runBlocking, _completion)
    tmp.__set_result(invalid)
    tmp.__set_exception(invalid)
    return tmp.doResume_k_()
end function

function coroutineQueueTests_lambda_lambda_slambda_1_doResume_k_() as Dynamic
    suspendResult = m.__get_result()
    while true
                try
            tmp = m.__get_state()
            if __kotlin_identityEquals(tmp, 0) then
                m.__set_exceptionState(1)
                launch_rCoroutineScope_CoroutineContext_SuspendFunction1CoroutineScopeV_k_(m._this_runBlocking, invalid, coroutineQueueTests_lambda_lambda_slambda_slambda_1_create_AnyN_ContinuationAnyNN_k_(m._count, invalid))
                launch_rCoroutineScope_CoroutineContext_SuspendFunction1CoroutineScopeV_k_(m._this_runBlocking, invalid, coroutineQueueTests_lambda_lambda_slambda_slambda_2_create_AnyN_ContinuationAnyNN_k_(m._count, invalid))
                launch_rCoroutineScope_CoroutineContext_SuspendFunction1CoroutineScopeV_k_(m._this_runBlocking, invalid, coroutineQueueTests_lambda_lambda_slambda_slambda_3_create_AnyN_ContinuationAnyNN_k_(m._count, invalid))
                m.__set_state(2)
                suspendResult = yield_ContinuationV_k_(m)
                if __kotlin_identityEquals(suspendResult, __get_COROUTINE_SUSPENDED_k_()) then
                    return suspendResult
                end if
                continue while
            else if __kotlin_identityEquals(tmp, 1) then
                throw m.e0
            else if __kotlin_identityEquals(tmp, 2) then
                m.__set_state(3)
                suspendResult = delay_J_ContinuationV_k_(10&, m)
                if __kotlin_identityEquals(suspendResult, __get_COROUTINE_SUSPENDED_k_()) then
                    return suspendResult
                end if
                continue while
            else if __kotlin_identityEquals(tmp, 3) then
                                return invalid
            end if
        catch e
            m.e0 = e
                        throw m.e0
        end try
    end while
end function

function coroutineQueueTests_lambda_lambda_slambda_1_create_AnyN_Continuation_k_(_this_runBlocking as Dynamic, completion as Object) as Object
    i = coroutineQueueTests_lambda_lambda_slambda_1_create_AnyN_ContinuationAnyNN_k_(m._count, completion)
    i._this_runBlocking = _this_runBlocking
    return i
end function

function coroutineQueueTests_lambda_lambda_2_create_k_() as Object
    this = {}
    this.__type = "coroutineQueueTests_lambda_lambda_2"
    this.__proto = ["coroutineQueueTests_lambda_lambda_2", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = coroutineQueueTests_lambda_lambda_2_invoke_k_
    return this
end function

sub coroutineQueueTests_lambda_lambda_2_invoke_k_()
    count = {value: 0}
    runBlocking_CoroutineContext_SuspendFunction1CoroutineScope_k_(invalid, coroutineQueueTests_lambda_lambda_slambda_1_create_AnyN_ContinuationAnyNN_k_(count, invalid))
    assertEquals_AnyN_AnyN_StrN_k_(3, count.value, invalid)
end sub

function coroutineQueueTests_lambda_create_k_() as Object
    this = {}
    this.__type = "coroutineQueueTests_lambda"
    this.__proto = ["coroutineQueueTests_lambda", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = coroutineQueueTests_lambda_invoke_AnyN_k_
    return this
end function

sub coroutineQueueTests_lambda_invoke_AnyN_k_(_this_suite as Object)
    _this_suite.test_Str_Function0V_k_("processCoroutineQueue with empty queue", coroutineQueueTests_lambda_lambda_create_k_())
    _this_suite.test_Str_Function0V_k_("launch enqueues work", coroutineQueueTests_lambda_lambda_1_create_k_())
    _this_suite.test_Str_Function0V_k_("multiple launch calls execute", coroutineQueueTests_lambda_lambda_2_create_k_())
end sub

function runBlockingTests_lambda_lambda_slambda_create_ContinuationAnyNN_k_(resultContinuation as Dynamic) as Object
    this = CoroutineImpl_create_ContinuationAnyNN_k_(resultContinuation)
    this._super = {}
    this._super.invoke_AnyN_Continuation_k_ = this.invoke_AnyN_Continuation_k_
    this._super.doResume_k_ = this.doResume_k_
    this._super.create_AnyN_Continuation_k_ = this.create_AnyN_Continuation_k_
    this.__proto = ["runBlockingTests_lambda_lambda_slambda", "SuspendFunction1", this.__proto]
    this.__type = "runBlockingTests_lambda_lambda_slambda"
    this.invoke_AnyN_Continuation_k_ = runBlockingTests_lambda_lambda_slambda_invoke_AnyN_Continuation_k_
    this.doResume_k_ = runBlockingTests_lambda_lambda_slambda_doResume_k_
    this.create_AnyN_Continuation_k_ = runBlockingTests_lambda_lambda_slambda_create_AnyN_Continuation_k_
    return this
end function

function runBlockingTests_lambda_lambda_slambda_invoke_AnyN_Continuation_k_(_this_runBlocking as Object, _completion as Object) as Object
    tmp = m.create_AnyN_Continuation_k_(_this_runBlocking, _completion)
    tmp.__set_result(invalid)
    tmp.__set_exception(invalid)
    return tmp.doResume_k_()
end function

function runBlockingTests_lambda_lambda_slambda_doResume_k_() as Dynamic
    suspendResult = m.__get_result()
    while true
                try
            tmp = m.__get_state()
            if __kotlin_identityEquals(tmp, 0) then
                m.__set_exceptionState(1)
                return 42

                                return invalid
            else if __kotlin_identityEquals(tmp, 1) then
                throw e
            end if
        catch e
                        throw e
        end try
    end while
end function

function runBlockingTests_lambda_lambda_slambda_create_AnyN_Continuation_k_(_this_runBlocking as Dynamic, completion as Object) as Object
    i = runBlockingTests_lambda_lambda_slambda_create_ContinuationAnyNN_k_(completion)
    i._this_runBlocking = _this_runBlocking
    return i
end function

function runBlockingTests_lambda_lambda_create_k_() as Object
    this = {}
    this.__type = "runBlockingTests_lambda_lambda"
    this.__proto = ["runBlockingTests_lambda_lambda", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = runBlockingTests_lambda_lambda_invoke_k_
    return this
end function

sub runBlockingTests_lambda_lambda_invoke_k_()
    result = runBlocking_CoroutineContext_SuspendFunction1CoroutineScope_k_(invalid, runBlockingTests_lambda_lambda_slambda_create_ContinuationAnyNN_k_(invalid))
    assertEquals_AnyN_AnyN_StrN_k_(42, result, invalid)
end sub

function runBlockingTests_lambda_lambda_slambda_1_create_ContinuationAnyNN_k_(resultContinuation as Dynamic) as Object
    this = CoroutineImpl_create_ContinuationAnyNN_k_(resultContinuation)
    this._super = {}
    this._super.invoke_AnyN_Continuation_k_ = this.invoke_AnyN_Continuation_k_
    this._super.doResume_k_ = this.doResume_k_
    this._super.create_AnyN_Continuation_k_ = this.create_AnyN_Continuation_k_
    this.__proto = ["runBlockingTests_lambda_lambda_slambda_1", "SuspendFunction1", this.__proto]
    this.__type = "runBlockingTests_lambda_lambda_slambda_1"
    this.invoke_AnyN_Continuation_k_ = runBlockingTests_lambda_lambda_slambda_1_invoke_AnyN_Continuation_k_
    this.doResume_k_ = runBlockingTests_lambda_lambda_slambda_1_doResume_k_
    this.create_AnyN_Continuation_k_ = runBlockingTests_lambda_lambda_slambda_1_create_AnyN_Continuation_k_
    return this
end function

function runBlockingTests_lambda_lambda_slambda_1_invoke_AnyN_Continuation_k_(_this_runBlocking as Object, _completion as Object) as Object
    tmp = m.create_AnyN_Continuation_k_(_this_runBlocking, _completion)
    tmp.__set_result(invalid)
    tmp.__set_exception(invalid)
    return tmp.doResume_k_()
end function

function runBlockingTests_lambda_lambda_slambda_1_doResume_k_() as Dynamic
    suspendResult = m.__get_result()
    while true
                try
            tmp = m.__get_state()
            if __kotlin_identityEquals(tmp, 0) then
                m.__set_exceptionState(1)
                m.__set_state(2)
                suspendResult = delay_J_ContinuationV_k_(10&, m)
                if __kotlin_identityEquals(suspendResult, __get_COROUTINE_SUSPENDED_k_()) then
                    return suspendResult
                end if
                continue while
            else if __kotlin_identityEquals(tmp, 1) then
                throw m.e0
            else if __kotlin_identityEquals(tmp, 2) then
                                return "done"
            end if
        catch e
            m.e0 = e
                        throw m.e0
        end try
    end while
end function

function runBlockingTests_lambda_lambda_slambda_1_create_AnyN_Continuation_k_(_this_runBlocking as Dynamic, completion as Object) as Object
    i = runBlockingTests_lambda_lambda_slambda_1_create_ContinuationAnyNN_k_(completion)
    i._this_runBlocking = _this_runBlocking
    return i
end function

function runBlockingTests_lambda_lambda_1_create_k_() as Object
    this = {}
    this.__type = "runBlockingTests_lambda_lambda_1"
    this.__proto = ["runBlockingTests_lambda_lambda_1", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = runBlockingTests_lambda_lambda_1_invoke_k_
    return this
end function

sub runBlockingTests_lambda_lambda_1_invoke_k_()
    result = runBlocking_CoroutineContext_SuspendFunction1CoroutineScope_k_(invalid, runBlockingTests_lambda_lambda_slambda_1_create_ContinuationAnyNN_k_(invalid))
    assertEquals_AnyN_AnyN_StrN_k_("done", result, invalid)
end sub

function runBlockingTests_lambda_lambda_slambda_slambda_create_AnyN_ContinuationAnyNN_k_(_counter as Dynamic, resultContinuation as Dynamic) as Object
    this = CoroutineImpl_create_ContinuationAnyNN_k_(resultContinuation)
    this._super = {}
    this._super.invoke_AnyN_Continuation_k_ = this.invoke_AnyN_Continuation_k_
    this._super.doResume_k_ = this.doResume_k_
    this._super.create_AnyN_Continuation_k_ = this.create_AnyN_Continuation_k_
    this.__proto = ["runBlockingTests_lambda_lambda_slambda_slambda", "SuspendFunction1", this.__proto]
    this.__type = "runBlockingTests_lambda_lambda_slambda_slambda"
    this.invoke_AnyN_Continuation_k_ = runBlockingTests_lambda_lambda_slambda_slambda_invoke_AnyN_Continuation_k_
    this.doResume_k_ = runBlockingTests_lambda_lambda_slambda_slambda_doResume_k_
    this.create_AnyN_Continuation_k_ = runBlockingTests_lambda_lambda_slambda_slambda_create_AnyN_Continuation_k_
    this._counter = _counter
    return this
end function

function runBlockingTests_lambda_lambda_slambda_slambda_invoke_AnyN_Continuation_k_(_this_launch as Object, _completion as Object) as Object
    tmp = m.create_AnyN_Continuation_k_(_this_launch, _completion)
    tmp.__set_result(invalid)
    tmp.__set_exception(invalid)
    return tmp.doResume_k_()
end function

function runBlockingTests_lambda_lambda_slambda_slambda_doResume_k_() as Dynamic
    suspendResult = m.__get_result()
    while true
                try
            tmp = m.__get_state()
            if __kotlin_identityEquals(tmp, 0) then
                m.__set_exceptionState(1)
                m.__set_state(2)
                suspendResult = delay_J_ContinuationV_k_(20&, m)
                if __kotlin_identityEquals(suspendResult, __get_COROUTINE_SUSPENDED_k_()) then
                    return suspendResult
                end if
                continue while
            else if __kotlin_identityEquals(tmp, 1) then
                throw m.e0
            else if __kotlin_identityEquals(tmp, 2) then
                m._counter.value = (m._counter.value + 10)
                                return invalid
            end if
        catch e
            m.e0 = e
                        throw m.e0
        end try
    end while
end function

function runBlockingTests_lambda_lambda_slambda_slambda_create_AnyN_Continuation_k_(_this_launch as Dynamic, completion as Object) as Object
    i = runBlockingTests_lambda_lambda_slambda_slambda_create_AnyN_ContinuationAnyNN_k_(m._counter, completion)
    i._this_launch = _this_launch
    return i
end function

function runBlockingTests_lambda_lambda_slambda_2_create_AnyN_ContinuationAnyNN_k_(_counter as Dynamic, resultContinuation as Dynamic) as Object
    this = CoroutineImpl_create_ContinuationAnyNN_k_(resultContinuation)
    this._super = {}
    this._super.invoke_AnyN_Continuation_k_ = this.invoke_AnyN_Continuation_k_
    this._super.doResume_k_ = this.doResume_k_
    this._super.create_AnyN_Continuation_k_ = this.create_AnyN_Continuation_k_
    this.__proto = ["runBlockingTests_lambda_lambda_slambda_2", "SuspendFunction1", this.__proto]
    this.__type = "runBlockingTests_lambda_lambda_slambda_2"
    this.invoke_AnyN_Continuation_k_ = runBlockingTests_lambda_lambda_slambda_2_invoke_AnyN_Continuation_k_
    this.doResume_k_ = runBlockingTests_lambda_lambda_slambda_2_doResume_k_
    this.create_AnyN_Continuation_k_ = runBlockingTests_lambda_lambda_slambda_2_create_AnyN_Continuation_k_
    this._counter = _counter
    return this
end function

function runBlockingTests_lambda_lambda_slambda_2_invoke_AnyN_Continuation_k_(_this_runBlocking as Object, _completion as Object) as Object
    tmp = m.create_AnyN_Continuation_k_(_this_runBlocking, _completion)
    tmp.__set_result(invalid)
    tmp.__set_exception(invalid)
    return tmp.doResume_k_()
end function

function runBlockingTests_lambda_lambda_slambda_2_doResume_k_() as Dynamic
    suspendResult = m.__get_result()
    while true
                try
            tmp = m.__get_state()
            if __kotlin_identityEquals(tmp, 0) then
                m.__set_exceptionState(1)
                launch_rCoroutineScope_CoroutineContext_SuspendFunction1CoroutineScopeV_k_(m._this_runBlocking, invalid, runBlockingTests_lambda_lambda_slambda_slambda_create_AnyN_ContinuationAnyNN_k_(m._counter, invalid))
                m.__set_state(2)
                suspendResult = delay_J_ContinuationV_k_(10&, m)
                if __kotlin_identityEquals(suspendResult, __get_COROUTINE_SUSPENDED_k_()) then
                    return suspendResult
                end if
                continue while
            else if __kotlin_identityEquals(tmp, 1) then
                throw m.e0
            else if __kotlin_identityEquals(tmp, 2) then
                m._counter.value = (m._counter.value + 1)
                m.__set_state(3)
                suspendResult = delay_J_ContinuationV_k_(30&, m)
                if __kotlin_identityEquals(suspendResult, __get_COROUTINE_SUSPENDED_k_()) then
                    return suspendResult
                end if
                continue while
            else if __kotlin_identityEquals(tmp, 3) then
                                return invalid
            end if
        catch e
            m.e0 = e
                        throw m.e0
        end try
    end while
end function

function runBlockingTests_lambda_lambda_slambda_2_create_AnyN_Continuation_k_(_this_runBlocking as Dynamic, completion as Object) as Object
    i = runBlockingTests_lambda_lambda_slambda_2_create_AnyN_ContinuationAnyNN_k_(m._counter, completion)
    i._this_runBlocking = _this_runBlocking
    return i
end function

function runBlockingTests_lambda_lambda_2_create_k_() as Object
    this = {}
    this.__type = "runBlockingTests_lambda_lambda_2"
    this.__proto = ["runBlockingTests_lambda_lambda_2", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = runBlockingTests_lambda_lambda_2_invoke_k_
    return this
end function

sub runBlockingTests_lambda_lambda_2_invoke_k_()
    counter = {value: 0}
    runBlocking_CoroutineContext_SuspendFunction1CoroutineScope_k_(invalid, runBlockingTests_lambda_lambda_slambda_2_create_AnyN_ContinuationAnyNN_k_(counter, invalid))
    assertEquals_AnyN_AnyN_StrN_k_(11, counter.value, invalid)
end sub

function runBlockingTests_lambda_lambda_slambda_3_create_ContinuationAnyNN_k_(resultContinuation as Dynamic) as Object
    this = CoroutineImpl_create_ContinuationAnyNN_k_(resultContinuation)
    this._super = {}
    this._super.invoke_AnyN_Continuation_k_ = this.invoke_AnyN_Continuation_k_
    this._super.doResume_k_ = this.doResume_k_
    this._super.create_AnyN_Continuation_k_ = this.create_AnyN_Continuation_k_
    this.__proto = ["runBlockingTests_lambda_lambda_slambda_3", "SuspendFunction1", this.__proto]
    this.__type = "runBlockingTests_lambda_lambda_slambda_3"
    this.invoke_AnyN_Continuation_k_ = runBlockingTests_lambda_lambda_slambda_3_invoke_AnyN_Continuation_k_
    this.doResume_k_ = runBlockingTests_lambda_lambda_slambda_3_doResume_k_
    this.create_AnyN_Continuation_k_ = runBlockingTests_lambda_lambda_slambda_3_create_AnyN_Continuation_k_
    return this
end function

function runBlockingTests_lambda_lambda_slambda_3_invoke_AnyN_Continuation_k_(_this_runBlocking as Object, _completion as Object) as Object
    tmp = m.create_AnyN_Continuation_k_(_this_runBlocking, _completion)
    tmp.__set_result(invalid)
    tmp.__set_exception(invalid)
    return tmp.doResume_k_()
end function

function runBlockingTests_lambda_lambda_slambda_3_doResume_k_() as Dynamic
    suspendResult = m.__get_result()
    while true
                try
            tmp = m.__get_state()
            if __kotlin_identityEquals(tmp, 0) then
                m.__set_exceptionState(1)
                m.__set_state(2)
                suspendResult = delay_J_ContinuationV_k_(50&, m)
                if __kotlin_identityEquals(suspendResult, __get_COROUTINE_SUSPENDED_k_()) then
                    return suspendResult
                end if
                continue while
            else if __kotlin_identityEquals(tmp, 1) then
                throw m.e0
            else if __kotlin_identityEquals(tmp, 2) then
                                return invalid
            end if
        catch e
            m.e0 = e
                        throw m.e0
        end try
    end while
end function

function runBlockingTests_lambda_lambda_slambda_3_create_AnyN_Continuation_k_(_this_runBlocking as Dynamic, completion as Object) as Object
    i = runBlockingTests_lambda_lambda_slambda_3_create_ContinuationAnyNN_k_(completion)
    i._this_runBlocking = _this_runBlocking
    return i
end function

function runBlockingTests_lambda_lambda_3_create_k_() as Object
    this = {}
    this.__type = "runBlockingTests_lambda_lambda_3"
    this.__proto = ["runBlockingTests_lambda_lambda_3", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = runBlockingTests_lambda_lambda_3_invoke_k_
    return this
end function

sub runBlockingTests_lambda_lambda_3_invoke_k_()
    timer = CreateObject("roTimespan")
    timer.mark()
    runBlocking_CoroutineContext_SuspendFunction1CoroutineScope_k_(invalid, runBlockingTests_lambda_lambda_slambda_3_create_ContinuationAnyNN_k_(invalid))
    elapsed = timer.totalMilliseconds()
    assertTrue_Z_StrN_k_(elapsed >= 45, "Should wait at least ~50ms, got " + __kotlin_numToStr_I_k_(elapsed))
end sub

function runBlockingTests_lambda_create_k_() as Object
    this = {}
    this.__type = "runBlockingTests_lambda"
    this.__proto = ["runBlockingTests_lambda", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = runBlockingTests_lambda_invoke_AnyN_k_
    return this
end function

sub runBlockingTests_lambda_invoke_AnyN_k_(_this_suite as Object)
    _this_suite.test_Str_Function0V_k_("runBlocking completes simple coroutine", runBlockingTests_lambda_lambda_create_k_())
    _this_suite.test_Str_Function0V_k_("runBlocking with delay completes", runBlockingTests_lambda_lambda_1_create_k_())
    _this_suite.test_Str_Function0V_k_("runBlocking with launch and delay", runBlockingTests_lambda_lambda_2_create_k_())
    _this_suite.test_Str_Function0V_k_("runBlocking processes delays correctly", runBlockingTests_lambda_lambda_3_create_k_())
end sub
