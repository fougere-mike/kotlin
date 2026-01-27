function fold_rResult_Function1_Function1Throwable_k_(m as Object, onSuccess as Object, onFailure as Object) as Dynamic
    exception = m.exceptionOrNull_k_()
    __when_tmp0 = invalid
    if exception = invalid then
        __when_tmp0 = onSuccess.invoke_AnyN_k_(m.__get_value())
    else if true then
        __when_tmp0 = onFailure.invoke_AnyN_k_(exception)
    end if
    return __when_tmp0

end function

function runCatching_Function0_k_(block as Object) as Object
    result = Result_Companion_getInstance().success_AnyN_k_(invalid)
    try
        result = Result_Companion_getInstance().success_AnyN_k_(block.invoke_k_())
    catch e
        result = Result_Companion_getInstance().failure_Throwable_k_(e)
    end try
    return result
end function

function runCatching_rAnyN_Function1_k_(m as Dynamic, block as Object) as Object
    result = Result_Companion_getInstance().success_AnyN_k_(invalid)
    try
        result = Result_Companion_getInstance().success_AnyN_k_(block.invoke_AnyN_k_(m))
    catch e
        result = Result_Companion_getInstance().failure_Throwable_k_(e)
    end try
    return result
end function
