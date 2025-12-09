function lookupAsserter_Asserter_k_() as Object
    if get__asserter_AsserterN_k_() = invalid then
        set__asserter_AsserterN_k_(DefaultBrsAsserter_create_DefaultBrsAsserter_k_())
    end if
    return get__asserter_AsserterN_k_()
end function

function AssertionErrorWithCause_StrN_ThrowableN_AssertionError_k_(message as Dynamic, cause as Dynamic) as Object
    error = AssertionError_create_AnyN_AssertionError_k_(message)
    return error
end function

sub todo_Function0V_k_(block as Object)
    println_AnyN_k_("TODO at " + block)
end sub
