function DefaultBrsAsserter_create_k_() as Object
    this = {}
    this.__type = "DefaultBrsAsserter"
    this.__proto = ["DefaultBrsAsserter", "Asserter"]
    this.__id = __kotlin_nextObjectId()
    this.assertEquals_StrN_AnyN_AnyN_k_ = DefaultBrsAsserter_assertEquals_StrN_AnyN_AnyN_k_
    this.assertNotEquals_StrN_AnyN_AnyN_k_ = DefaultBrsAsserter_assertNotEquals_StrN_AnyN_AnyN_k_
    this.assertSame_StrN_AnyN_AnyN_k_ = DefaultBrsAsserter_assertSame_StrN_AnyN_AnyN_k_
    this.assertNotSame_StrN_AnyN_AnyN_k_ = DefaultBrsAsserter_assertNotSame_StrN_AnyN_AnyN_k_
    this.assertTrue_StrN_Z_k_ = DefaultBrsAsserter_assertTrue_StrN_Z_k_
    this.assertFalse_StrN_Z_k_ = DefaultBrsAsserter_assertFalse_StrN_Z_k_
    this.assertNotNull_StrN_AnyN_k_ = DefaultBrsAsserter_assertNotNull_StrN_AnyN_k_
    this.assertNull_StrN_AnyN_k_ = DefaultBrsAsserter_assertNull_StrN_AnyN_k_
    this.fail_StrN_k_ = DefaultBrsAsserter_fail_StrN_k_
    return this
end function

sub DefaultBrsAsserter_assertEquals_StrN_AnyN_AnyN_k_(message as Dynamic, expected as Dynamic, actual as Dynamic)
    if not brsStructuralEquals_AnyN_AnyN_k_(expected, actual) then
        __when_tmp0 = invalid
        if expected <> invalid then
            __when_tmp0 = toString_AnyN_k_(expected)
        else if true then
            __when_tmp0 = "null"
        end if
        expectedStr = __when_tmp0
        __when_tmp1 = invalid
        if actual <> invalid then
            __when_tmp1 = toString_AnyN_k_(actual)
        else if true then
            __when_tmp1 = "null"
        end if
        actualStr = __when_tmp1
        tmp0_elvis_lhs = message
        __when_tmp2 = invalid
        if tmp0_elvis_lhs = invalid then
            __when_tmp2 = (((("Expected <" + expectedStr) + ">, actual <") + actualStr) + ">.")
        else if true then
            __when_tmp2 = tmp0_elvis_lhs
        end if
        m.fail_StrN_k_(__when_tmp2)
    end if
end sub

sub DefaultBrsAsserter_assertNotEquals_StrN_AnyN_AnyN_k_(message as Dynamic, illegal as Dynamic, actual as Dynamic)
    if brsStructuralEquals_AnyN_AnyN_k_(illegal, actual) then
        __when_tmp3 = invalid
        if actual <> invalid then
            __when_tmp3 = toString_AnyN_k_(actual)
        else if true then
            __when_tmp3 = "null"
        end if
        actualStr = __when_tmp3
        tmp0_elvis_lhs = message
        __when_tmp4 = invalid
        if tmp0_elvis_lhs = invalid then
            __when_tmp4 = (("Values should be different. Actual: <" + actualStr) + ">.")
        else if true then
            __when_tmp4 = tmp0_elvis_lhs
        end if
        m.fail_StrN_k_(__when_tmp4)
    end if
end sub

sub DefaultBrsAsserter_assertSame_StrN_AnyN_AnyN_k_(message as Dynamic, expected as Dynamic, actual as Dynamic)
    if not __kotlin_identityEquals(expected, actual) then
        tmp0_elvis_lhs = message
        __when_tmp5 = invalid
        if tmp0_elvis_lhs = invalid then
            __when_tmp5 = "Expected and actual are not the same instance."
        else if true then
            __when_tmp5 = tmp0_elvis_lhs
        end if
        m.fail_StrN_k_(__when_tmp5)
    end if
end sub

sub DefaultBrsAsserter_assertNotSame_StrN_AnyN_AnyN_k_(message as Dynamic, illegal as Dynamic, actual as Dynamic)
    if __kotlin_identityEquals(illegal, actual) then
        tmp0_elvis_lhs = message
        __when_tmp6 = invalid
        if tmp0_elvis_lhs = invalid then
            __when_tmp6 = "Expected and actual should not be the same instance."
        else if true then
            __when_tmp6 = tmp0_elvis_lhs
        end if
        m.fail_StrN_k_(__when_tmp6)
    end if
end sub

sub DefaultBrsAsserter_assertTrue_StrN_Z_k_(message as Dynamic, actual as Boolean)
    if not actual then
        tmp0_elvis_lhs = message
        __when_tmp7 = invalid
        if tmp0_elvis_lhs = invalid then
            __when_tmp7 = "Expected value to be true."
        else if true then
            __when_tmp7 = tmp0_elvis_lhs
        end if
        m.fail_StrN_k_(__when_tmp7)
    end if
end sub

sub DefaultBrsAsserter_assertFalse_StrN_Z_k_(message as Dynamic, actual as Boolean)
    if actual then
        tmp0_elvis_lhs = message
        __when_tmp8 = invalid
        if tmp0_elvis_lhs = invalid then
            __when_tmp8 = "Expected value to be false."
        else if true then
            __when_tmp8 = tmp0_elvis_lhs
        end if
        m.fail_StrN_k_(__when_tmp8)
    end if
end sub

sub DefaultBrsAsserter_assertNotNull_StrN_AnyN_k_(message as Dynamic, actual as Dynamic)
    if actual = invalid then
        tmp0_elvis_lhs = message
        __when_tmp9 = invalid
        if tmp0_elvis_lhs = invalid then
            __when_tmp9 = "Expected value to be not null."
        else if true then
            __when_tmp9 = tmp0_elvis_lhs
        end if
        m.fail_StrN_k_(__when_tmp9)
    end if
end sub

sub DefaultBrsAsserter_assertNull_StrN_AnyN_k_(message as Dynamic, actual as Dynamic)
    if actual <> invalid then
        tmp0_elvis_lhs = message
        __when_tmp10 = invalid
        if tmp0_elvis_lhs = invalid then
            __when_tmp10 = "Expected value to be null."
        else if true then
            __when_tmp10 = tmp0_elvis_lhs
        end if
        m.fail_StrN_k_(__when_tmp10)
    end if
end sub

sub DefaultBrsAsserter_fail_StrN_k_(message as Dynamic)
    throw AssertionError_create_AnyN_k_(message)
end sub
