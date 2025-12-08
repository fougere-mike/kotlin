function DefaultBrsAsserter_create_DefaultBrsAsserter_k_() as Object
    this = {}
    this.__type = "DefaultBrsAsserter"
    this.__proto = ["DefaultBrsAsserter"]
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
    if expected <> actual then
        tmp0_elvis_lhs = message
        __when_tmp0 = invalid
        if tmp0_elvis_lhs = invalid then
            __when_tmp0 = (((("Expected <" + expected) + ">, actual <") + actual) + ">.")
        else if true then
            __when_tmp0 = tmp0_elvis_lhs
        end if
        m.fail(__when_tmp0)
    end if
end sub

sub DefaultBrsAsserter_assertNotEquals_StrN_AnyN_AnyN_k_(message as Dynamic, illegal as Dynamic, actual as Dynamic)
    if illegal = actual then
        tmp0_elvis_lhs = message
        __when_tmp1 = invalid
        if tmp0_elvis_lhs = invalid then
            __when_tmp1 = (("Values should be different. Actual: <" + actual) + ">.")
        else if true then
            __when_tmp1 = tmp0_elvis_lhs
        end if
        m.fail(__when_tmp1)
    end if
end sub

sub DefaultBrsAsserter_assertSame_StrN_AnyN_AnyN_k_(message as Dynamic, expected as Dynamic, actual as Dynamic)
    if EQEQEQ_AnyN_AnyN_Z_k_(expected, actual).not() then
        tmp0_elvis_lhs = message
        __when_tmp2 = invalid
        if tmp0_elvis_lhs = invalid then
            __when_tmp2 = "Expected and actual are not the same instance."
        else if true then
            __when_tmp2 = tmp0_elvis_lhs
        end if
        m.fail(__when_tmp2)
    end if
end sub

sub DefaultBrsAsserter_assertNotSame_StrN_AnyN_AnyN_k_(message as Dynamic, illegal as Dynamic, actual as Dynamic)
    if EQEQEQ_AnyN_AnyN_Z_k_(illegal, actual) then
        tmp0_elvis_lhs = message
        __when_tmp3 = invalid
        if tmp0_elvis_lhs = invalid then
            __when_tmp3 = "Expected and actual should not be the same instance."
        else if true then
            __when_tmp3 = tmp0_elvis_lhs
        end if
        m.fail(__when_tmp3)
    end if
end sub

sub DefaultBrsAsserter_assertTrue_StrN_Z_k_(message as Dynamic, actual as Boolean)
    if actual.not() then
        tmp0_elvis_lhs = message
        __when_tmp4 = invalid
        if tmp0_elvis_lhs = invalid then
            __when_tmp4 = "Expected value to be true."
        else if true then
            __when_tmp4 = tmp0_elvis_lhs
        end if
        m.fail(__when_tmp4)
    end if
end sub

sub DefaultBrsAsserter_assertFalse_StrN_Z_k_(message as Dynamic, actual as Boolean)
    if actual then
        tmp0_elvis_lhs = message
        __when_tmp5 = invalid
        if tmp0_elvis_lhs = invalid then
            __when_tmp5 = "Expected value to be false."
        else if true then
            __when_tmp5 = tmp0_elvis_lhs
        end if
        m.fail(__when_tmp5)
    end if
end sub

sub DefaultBrsAsserter_assertNotNull_StrN_AnyN_k_(message as Dynamic, actual as Dynamic)
    if actual = invalid then
        tmp0_elvis_lhs = message
        __when_tmp6 = invalid
        if tmp0_elvis_lhs = invalid then
            __when_tmp6 = "Expected value to be not null."
        else if true then
            __when_tmp6 = tmp0_elvis_lhs
        end if
        m.fail(__when_tmp6)
    end if
end sub

sub DefaultBrsAsserter_assertNull_StrN_AnyN_k_(message as Dynamic, actual as Dynamic)
    if actual <> invalid then
        tmp0_elvis_lhs = message
        __when_tmp7 = invalid
        if tmp0_elvis_lhs = invalid then
            __when_tmp7 = "Expected value to be null."
        else if true then
            __when_tmp7 = tmp0_elvis_lhs
        end if
        m.fail(__when_tmp7)
    end if
end sub

sub DefaultBrsAsserter_fail_StrN_k_(message as Dynamic)
    throw AssertionError_create_AnyN_AssertionError_k_(message)
end sub
