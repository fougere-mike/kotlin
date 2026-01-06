function fold_rResult_Function1_Function1Throwable_k_(m as Object, onSuccess as Object, onFailure as Object) as Dynamic
    __when_tmp0 = invalid
    if exception = invalid then
        __when_tmp0 = onSuccess.invoke(m.get_value())
    else if true then
        __when_tmp0 = onFailure.invoke(exception)
    end if
    return __when_tmp0

end function

function runCatching_Function0_k_(block as Object) as Object
    return "/* Unsupported: IrTryImpl */"
end function

function runCatching_rAnyN_Function1_k_(m as Dynamic, block as Object) as Object
    return "/* Unsupported: IrTryImpl */"
end function
