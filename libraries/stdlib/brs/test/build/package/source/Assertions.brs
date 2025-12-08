function get_asserter_Asserter_k_() as Object
    tmp0_elvis_lhs = get__asserter_AsserterN_k_()
    __when_tmp0 = invalid
    if tmp0_elvis_lhs = invalid then
        __when_tmp0 = lookupAsserter_Asserter_k_()
    else if true then
        __when_tmp0 = tmp0_elvis_lhs
    end if
    return __when_tmp0

end function

function get__asserter_AsserterN_k_() as Dynamic
    return GetGlobalAA()._asserter
end function

sub set__asserter_AsserterN_k_(value as Dynamic)
    m._asserter = value
end sub

sub Asserter_assertEquals_StrN_AnyN_AnyN_k_(message as Dynamic, expected as Dynamic, actual as Dynamic)
end sub

sub Asserter_assertNotEquals_StrN_AnyN_AnyN_k_(message as Dynamic, illegal as Dynamic, actual as Dynamic)
end sub

sub Asserter_assertSame_StrN_AnyN_AnyN_k_(message as Dynamic, expected as Dynamic, actual as Dynamic)
end sub

sub Asserter_assertNotSame_StrN_AnyN_AnyN_k_(message as Dynamic, illegal as Dynamic, actual as Dynamic)
end sub

sub Asserter_assertTrue_StrN_Z_k_(message as Dynamic, actual as Boolean)
end sub

sub Asserter_assertFalse_StrN_Z_k_(message as Dynamic, actual as Boolean)
end sub

sub Asserter_assertNotNull_StrN_AnyN_k_(message as Dynamic, actual as Dynamic)
end sub

sub Asserter_assertNull_StrN_AnyN_k_(message as Dynamic, actual as Dynamic)
end sub

sub Asserter_fail_StrN_k_(message as Dynamic)
end sub

sub Asserter_fail_StrN_ThrowableN_k_(message as Dynamic, cause as Dynamic)
    m.fail(message)
end sub

function messagePrefix_StrN_Str_k_(message as Dynamic) as String
    __when_tmp1 = invalid
    if message = invalid then
        __when_tmp1 = ""
    else if true then
        __when_tmp1 = (message + ". ")
    end if
    return __when_tmp1

end function

sub assertTrue_Z_StrN_k_(actual as Boolean, message = invalid)
    tmp0_elvis_lhs = message
    __when_tmp2 = invalid
    if tmp0_elvis_lhs = invalid then
        __when_tmp2 = "Expected value to be true."
    else if true then
        __when_tmp2 = tmp0_elvis_lhs
    end if
    get_asserter_Asserter_k_().assertTrue(__when_tmp2, actual)

end sub

sub assertTrue_StrN_Function0Z_k_(message = invalid, block = invalid)
    assertTrue_Z_StrN_k_(block.invoke(), message)
end sub

sub assertFalse_Z_StrN_k_(actual as Boolean, message = invalid)
    tmp0_elvis_lhs = message
    __when_tmp3 = invalid
    if tmp0_elvis_lhs = invalid then
        __when_tmp3 = "Expected value to be false."
    else if true then
        __when_tmp3 = tmp0_elvis_lhs
    end if
    get_asserter_Asserter_k_().assertFalse(__when_tmp3, actual)

end sub

sub assertFalse_StrN_Function0Z_k_(message = invalid, block = invalid)
    assertFalse_Z_StrN_k_(block.invoke(), message)
end sub

sub assertEquals_AnyN_AnyN_StrN_k_(expected as Dynamic, actual as Dynamic, message = invalid)
    get_asserter_Asserter_k_().assertEquals(message, expected, actual)
end sub

sub assertNotEquals_AnyN_AnyN_StrN_k_(illegal as Dynamic, actual as Dynamic, message = invalid)
    get_asserter_Asserter_k_().assertNotEquals(message, illegal, actual)
end sub

sub assertSame_AnyN_AnyN_StrN_k_(expected as Dynamic, actual as Dynamic, message = invalid)
    get_asserter_Asserter_k_().assertSame(message, expected, actual)
end sub

sub assertNotSame_AnyN_AnyN_StrN_k_(illegal as Dynamic, actual as Dynamic, message = invalid)
    get_asserter_Asserter_k_().assertNotSame(message, illegal, actual)
end sub

function assertNotNull_AnyN_StrN_Any_k_(actual as Dynamic, message = invalid) as Object
    get_asserter_Asserter_k_().assertNotNull(message, actual)
    return CHECK_NOT_NULL_AnyN_Any_k_(actual)
end function

function assertNotNull_AnyN_StrN_Function1AnyAnyN_AnyN_k_(actual as Dynamic, message = invalid, block = invalid) as Dynamic
    get_asserter_Asserter_k_().assertNotNull(message, actual)
    return block.invoke(CHECK_NOT_NULL_AnyN_Any_k_(actual))
end function

sub assertNull_AnyN_StrN_k_(actual as Dynamic, message = invalid)
    get_asserter_Asserter_k_().assertNull(message, actual)
end sub

sub fail_StrN_k_(message = invalid)
    get_asserter_Asserter_k_().fail(message)
end sub

sub fail_StrN_ThrowableN_k_(message = invalid, cause = invalid)
    get_asserter_Asserter_k_().fail(message, cause)
end sub
