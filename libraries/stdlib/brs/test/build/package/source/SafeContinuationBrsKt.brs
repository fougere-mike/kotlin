function SafeContinuation_create_Continuation_AnyN_k_(delegate as Object, initialResult as Dynamic) as Object
    this = {}
    this.__type = "SafeContinuation"
    this.__proto = ["SafeContinuation", "Continuation"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.resumeWith_Result_k_ = SafeContinuation_resumeWith_Result_k_
    this.getOrThrow_k_ = SafeContinuation_getOrThrow_k_
    this.__get_delegate = SafeContinuation___get_delegate_k_
    this.__get_context = SafeContinuation___get_context_k_
    this.__get_result = SafeContinuation___get_result_k_
    this.__set_result = SafeContinuation___set_result_AnyN_k_
    this.delegate = delegate
    this.result = initialResult
    return this
end function

function SafeContinuation_create_Continuation_k_(delegate as Object) as Object
    return SafeContinuation_create_Continuation_AnyN_k_(delegate, [CoroutineSingletons_initEntries(), m.CoroutineSingletons_UNDECIDED][1])
end function

sub SafeContinuation_resumeWith_Result_k_(result as Object)
    cur = m.__get_result()
    if __kotlin_identityEquals(cur, [CoroutineSingletons_initEntries(), m.CoroutineSingletons_UNDECIDED][1]) then
        tmp0 = result
        tmp_ret_2 = invalid

        while true
            this = tmp0
            exception = this.exceptionOrNull_k_()
            __when_tmp0 = invalid
            if exception = invalid then
                tmp0 = this.__get_value()
                tmp_ret_0 = invalid
                while true
                    it = tmp0
                    tmp_ret_0 = it
                    exit while
                end while
                __when_tmp0 = tmp_ret_0
            else if true then
                tmp0 = exception
                tmp_ret_1 = invalid
                while true
                    it = tmp0
                    tmp_ret_1 = ResultFailure_create_Throwable_k_(it)
                    exit while
                end while
                __when_tmp0 = tmp_ret_1
            end if
            tmp_ret_2 = __when_tmp0
            exit while
        end while
        m.__set_result(tmp_ret_2)
    else if __kotlin_identityEquals(cur, [CoroutineSingletons_initEntries(), m.CoroutineSingletons_COROUTINE_SUSPENDED][1]) then
        m.__set_result([CoroutineSingletons_initEntries(), m.CoroutineSingletons_RESUMED][1])
        m.__get_delegate().resumeWith_Result_k_(result)
    else if true then
        throw IllegalStateException_create_StrN_k_("Already resumed")
    end if
end sub

function SafeContinuation_getOrThrow_k_() as Dynamic
    if __kotlin_identityEquals(m.__get_result(), [CoroutineSingletons_initEntries(), m.CoroutineSingletons_UNDECIDED][1]) then
        m.__set_result([CoroutineSingletons_initEntries(), m.CoroutineSingletons_COROUTINE_SUSPENDED][1])
        return __get_COROUTINE_SUSPENDED_k_()
    end if
    result = m.__get_result()
    __when_tmp1 = invalid
    if __kotlin_identityEquals(result, [CoroutineSingletons_initEntries(), m.CoroutineSingletons_RESUMED][1]) then
        __when_tmp1 = __get_COROUTINE_SUSPENDED_k_()
    else if __kotlin_isInstanceOf(result, "ResultFailure") then
        throw result.__get_exception()
    else if true then
        __when_tmp1 = result
    end if
    return __when_tmp1

end function

function SafeContinuation___get_delegate_k_() as Object
    return m.delegate
end function

function SafeContinuation___get_context_k_() as Object
    return m.__get_delegate().__get_context()
end function

function SafeContinuation___get_result_k_() as Dynamic
    return m.result
end function

sub SafeContinuation___set_result_AnyN_k_(value as Dynamic)
    m.result = value
end sub

function ResultFailure_create_Throwable_k_(exception as Object) as Object
    this = {}
    this.__type = "ResultFailure"
    this.__proto = ["ResultFailure"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.__get_exception = ResultFailure___get_exception_k_
    this.exception = exception
    return this
end function

function ResultFailure___get_exception_k_() as Object
    return m.exception
end function
