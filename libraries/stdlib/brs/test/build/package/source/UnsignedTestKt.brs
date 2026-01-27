sub unsignedTests_rTestRunner_k_(m as Object)
    m.suite_Str_Function1TestRunnerV_k_("Unsigned Basics", unsignedTests_lambda_create_k_())
    m.suite_Str_Function1TestRunnerV_k_("Unsigned Ranges", unsignedTests_lambda_1_create_k_())
end sub

function unsignedTests_lambda_lambda_create_k_() as Object
    this = {}
    this.__type = "unsignedTests_lambda_lambda"
    this.__proto = ["unsignedTests_lambda_lambda", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = unsignedTests_lambda_lambda_invoke_k_
    return this
end function

sub unsignedTests_lambda_lambda_invoke_k_()
    a = UInt_create_I_k_(1)
    b = UInt_create_I_k_(2)
    assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_k_(3), a.plus_UInt_k_(b), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_k_(0), UInt_Companion_getInstance().__get_MIN_VALUE(), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_k_(-1), UInt_Companion_getInstance().__get_MAX_VALUE(), invalid)
end sub

function unsignedTests_lambda_lambda_1_create_k_() as Object
    this = {}
    this.__type = "unsignedTests_lambda_lambda_1"
    this.__proto = ["unsignedTests_lambda_lambda_1", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = unsignedTests_lambda_lambda_1_invoke_k_
    return this
end function

sub unsignedTests_lambda_lambda_1_invoke_k_()
    a = UInt_create_I_k_(1)
    b = UInt_create_I_k_(2)
    assertTrue_Z_StrN_k_(a.compareTo_AnyN_k_(b) < 0, invalid)
    assertTrue_Z_StrN_k_(b.compareTo_AnyN_k_(a) > 0, invalid)
    assertEquals_AnyN_AnyN_StrN_k_(0, a.compareTo_AnyN_k_(a), invalid)
end sub

function unsignedTests_lambda_lambda_2_create_k_() as Object
    this = {}
    this.__type = "unsignedTests_lambda_lambda_2"
    this.__proto = ["unsignedTests_lambda_lambda_2", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = unsignedTests_lambda_lambda_2_invoke_k_
    return this
end function

sub unsignedTests_lambda_lambda_2_invoke_k_()
    a = UInt_create_I_k_(10)
    b = UInt_create_I_k_(3)
    assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_k_(13), a.plus_UInt_k_(b), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_k_(7), a.minus_UInt_k_(b), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_k_(30), a.times_UInt_k_(b), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_k_(3), a.div_UInt_k_(b), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_k_(1), a.rem_UInt_k_(b), invalid)
end sub

function unsignedTests_lambda_lambda_3_create_k_() as Object
    this = {}
    this.__type = "unsignedTests_lambda_lambda_3"
    this.__proto = ["unsignedTests_lambda_lambda_3", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = unsignedTests_lambda_lambda_3_invoke_k_
    return this
end function

sub unsignedTests_lambda_lambda_3_invoke_k_()
    a = ULong_create_J_k_(1&)
    b = ULong_create_J_k_(2&)
    assertEquals_AnyN_AnyN_StrN_k_(ULong_create_J_k_(3&), a.plus_ULong_k_(b), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(ULong_create_J_k_(0&), ULong_Companion_getInstance().__get_MIN_VALUE(), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(ULong_create_J_k_(-1&), ULong_Companion_getInstance().__get_MAX_VALUE(), invalid)
end sub

function unsignedTests_lambda_lambda_4_create_k_() as Object
    this = {}
    this.__type = "unsignedTests_lambda_lambda_4"
    this.__proto = ["unsignedTests_lambda_lambda_4", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = unsignedTests_lambda_lambda_4_invoke_k_
    return this
end function

sub unsignedTests_lambda_lambda_4_invoke_k_()
    a = UByte_create_B_k_(1)
    b = UByte_create_B_k_(2)
    assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_k_(3), a.plus_UByte_k_(b), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(UByte_create_B_k_(0), UByte_Companion_getInstance().__get_MIN_VALUE(), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(UByte_create_B_k_(-1), UByte_Companion_getInstance().__get_MAX_VALUE(), invalid)
end sub

function unsignedTests_lambda_lambda_5_create_k_() as Object
    this = {}
    this.__type = "unsignedTests_lambda_lambda_5"
    this.__proto = ["unsignedTests_lambda_lambda_5", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = unsignedTests_lambda_lambda_5_invoke_k_
    return this
end function

sub unsignedTests_lambda_lambda_5_invoke_k_()
    a = UShort_create_S_k_(1)
    b = UShort_create_S_k_(2)
    assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_k_(3), a.plus_UShort_k_(b), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(UShort_create_S_k_(0), UShort_Companion_getInstance().__get_MIN_VALUE(), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(UShort_create_S_k_(-1), UShort_Companion_getInstance().__get_MAX_VALUE(), invalid)
end sub

function unsignedTests_lambda_create_k_() as Object
    this = {}
    this.__type = "unsignedTests_lambda"
    this.__proto = ["unsignedTests_lambda", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = unsignedTests_lambda_invoke_AnyN_k_
    return this
end function

sub unsignedTests_lambda_invoke_AnyN_k_(_this_suite as Object)
    _this_suite.test_Str_Function0V_k_("UInt basics", unsignedTests_lambda_lambda_create_k_())
    _this_suite.test_Str_Function0V_k_("UInt comparison", unsignedTests_lambda_lambda_1_create_k_())
    _this_suite.test_Str_Function0V_k_("UInt arithmetic", unsignedTests_lambda_lambda_2_create_k_())
    _this_suite.test_Str_Function0V_k_("ULong basics", unsignedTests_lambda_lambda_3_create_k_())
    _this_suite.test_Str_Function0V_k_("UByte basics", unsignedTests_lambda_lambda_4_create_k_())
    _this_suite.test_Str_Function0V_k_("UShort basics", unsignedTests_lambda_lambda_5_create_k_())
end sub

function unsignedTests_lambda_lambda_6_create_k_() as Object
    this = {}
    this.__type = "unsignedTests_lambda_lambda_6"
    this.__proto = ["unsignedTests_lambda_lambda_6", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = unsignedTests_lambda_lambda_6_invoke_k_
    return this
end function

sub unsignedTests_lambda_lambda_6_invoke_k_()
    range = UInt_create_I_k_(1).rangeTo_UInt_k_(UInt_create_I_k_(5))
    list = toList_rIterable_k_(range)
    assertEquals_AnyN_AnyN_StrN_k_(5, list.__get_size(), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_k_(1), list.get_I_k_(0), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_k_(5), list.get_I_k_(4), invalid)
end sub

function unsignedTests_lambda_lambda_7_create_k_() as Object
    this = {}
    this.__type = "unsignedTests_lambda_lambda_7"
    this.__proto = ["unsignedTests_lambda_lambda_7", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = unsignedTests_lambda_lambda_7_invoke_k_
    return this
end function

sub unsignedTests_lambda_lambda_7_invoke_k_()
    range = until_rUInt_UInt_k_(UInt_create_I_k_(1), UInt_create_I_k_(5))
    list = toList_rIterable_k_(range)
    assertEquals_AnyN_AnyN_StrN_k_(4, list.__get_size(), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_k_(1), list.get_I_k_(0), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_k_(4), list.get_I_k_(3), invalid)
end sub

function unsignedTests_lambda_lambda_8_create_k_() as Object
    this = {}
    this.__type = "unsignedTests_lambda_lambda_8"
    this.__proto = ["unsignedTests_lambda_lambda_8", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = unsignedTests_lambda_lambda_8_invoke_k_
    return this
end function

sub unsignedTests_lambda_lambda_8_invoke_k_()
    range = downTo_rUInt_UInt_k_(UInt_create_I_k_(5), UInt_create_I_k_(1))
    list = toList_rIterable_k_(range)
    assertEquals_AnyN_AnyN_StrN_k_(5, list.__get_size(), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_k_(5), list.get_I_k_(0), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_k_(1), list.get_I_k_(4), invalid)
end sub

function unsignedTests_lambda_lambda_9_create_k_() as Object
    this = {}
    this.__type = "unsignedTests_lambda_lambda_9"
    this.__proto = ["unsignedTests_lambda_lambda_9", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = unsignedTests_lambda_lambda_9_invoke_k_
    return this
end function

sub unsignedTests_lambda_lambda_9_invoke_k_()
    range = step_rUIntProgression_I_k_(UInt_create_I_k_(1).rangeTo_UInt_k_(UInt_create_I_k_(10)), 2)
    list = toList_rIterable_k_(range)
    assertEquals_AnyN_AnyN_StrN_k_(5, list.__get_size(), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_k_(1), list.get_I_k_(0), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_k_(3), list.get_I_k_(1), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(UInt_create_I_k_(9), list.get_I_k_(4), invalid)
end sub

function unsignedTests_lambda_lambda_10_create_k_() as Object
    this = {}
    this.__type = "unsignedTests_lambda_lambda_10"
    this.__proto = ["unsignedTests_lambda_lambda_10", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = unsignedTests_lambda_lambda_10_invoke_k_
    return this
end function

sub unsignedTests_lambda_lambda_10_invoke_k_()
    range = ULong_create_J_k_(1&).rangeTo_ULong_k_(ULong_create_J_k_(3&))
    list = toList_rIterable_k_(range)
    assertEquals_AnyN_AnyN_StrN_k_(3, list.__get_size(), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(ULong_create_J_k_(1&), list.get_I_k_(0), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(ULong_create_J_k_(3&), list.get_I_k_(2), invalid)
end sub

function unsignedTests_lambda_1_create_k_() as Object
    this = {}
    this.__type = "unsignedTests_lambda_1"
    this.__proto = ["unsignedTests_lambda_1", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = unsignedTests_lambda_1_invoke_AnyN_k_
    return this
end function

sub unsignedTests_lambda_1_invoke_AnyN_k_(_this_suite as Object)
    _this_suite.test_Str_Function0V_k_("UIntRange", unsignedTests_lambda_lambda_6_create_k_())
    _this_suite.test_Str_Function0V_k_("UIntRange until", unsignedTests_lambda_lambda_7_create_k_())
    _this_suite.test_Str_Function0V_k_("UIntRange downTo", unsignedTests_lambda_lambda_8_create_k_())
    _this_suite.test_Str_Function0V_k_("UIntRange step", unsignedTests_lambda_lambda_9_create_k_())
    _this_suite.test_Str_Function0V_k_("ULongRange", unsignedTests_lambda_lambda_10_create_k_())
end sub
