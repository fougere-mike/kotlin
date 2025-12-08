function fold_rResultAnyN_Function1AnyNAnyN_Function1ThrowableAnyN_AnyN_k_(m as Object, onSuccess as Function, onFailure as Function) as Dynamic
    __when_tmp0 = invalid
    if exception = invalid then
        __when_tmp0 = onSuccess.invoke(m.value)
    else if true then
        __when_tmp0 = onFailure.invoke(exception)
    end if
    return __when_tmp0

end function

function runCatching_Function0AnyN_ResultAnyN_k_(block as Function) as Object
    return "/* Unsupported: IrTryImpl */"
end function

function runCatching_rAnyN_Function1AnyNAnyN_ResultAnyN_k_(m as Dynamic, block as Function) as Object
    return "/* Unsupported: IrTryImpl */"
end function
