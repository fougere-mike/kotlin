sub mathTests_rTestRunner_k_(m as Object)
    m.suite_Str_Function1TestRunnerV_k_("Math", mathTests_lambda_create_k_())
end sub

function mathTests_lambda_lambda_create_k_() as Object
    this = {}
    this.__type = "mathTests_lambda_lambda"
    this.__proto = ["mathTests_lambda_lambda", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = mathTests_lambda_lambda_invoke_k_
    return this
end function

sub mathTests_lambda_lambda_invoke_k_()
    assertTrue_Z_StrN_k_(3.141592653589793# > 3.14#, invalid)
    assertTrue_Z_StrN_k_(3.141592653589793# < 3.15#, invalid)
end sub

function mathTests_lambda_lambda_1_create_k_() as Object
    this = {}
    this.__type = "mathTests_lambda_lambda_1"
    this.__proto = ["mathTests_lambda_lambda_1", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = mathTests_lambda_lambda_1_invoke_k_
    return this
end function

sub mathTests_lambda_lambda_1_invoke_k_()
    assertTrue_Z_StrN_k_(2.718281828459045# > 2.71#, invalid)
    assertTrue_Z_StrN_k_(2.718281828459045# < 2.72#, invalid)
end sub

function mathTests_lambda_lambda_2_create_k_() as Object
    this = {}
    this.__type = "mathTests_lambda_lambda_2"
    this.__proto = ["mathTests_lambda_lambda_2", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = mathTests_lambda_lambda_2_invoke_k_
    return this
end function

sub mathTests_lambda_lambda_2_invoke_k_()
    assertEquals_AnyN_AnyN_StrN_k_(5, abs_I_k_(-5), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(5, abs_I_k_(5), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(0, abs_I_k_(0), invalid)
end sub

function mathTests_lambda_lambda_3_create_k_() as Object
    this = {}
    this.__type = "mathTests_lambda_lambda_3"
    this.__proto = ["mathTests_lambda_lambda_3", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = mathTests_lambda_lambda_3_invoke_k_
    return this
end function

sub mathTests_lambda_lambda_3_invoke_k_()
    assertEquals_AnyN_AnyN_StrN_k_(5.5#, abs_D_k_(-5.5#), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(5.5#, abs_D_k_(5.5#), invalid)
end sub

function mathTests_lambda_lambda_4_create_k_() as Object
    this = {}
    this.__type = "mathTests_lambda_lambda_4"
    this.__proto = ["mathTests_lambda_lambda_4", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = mathTests_lambda_lambda_4_invoke_k_
    return this
end function

sub mathTests_lambda_lambda_4_invoke_k_()
    assertEquals_AnyN_AnyN_StrN_k_(1, min_I_I_k_(1, 2), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(-5, min_I_I_k_(-5, 3), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(1.5#, min_D_D_k_(1.5#, 2.5#), invalid)
end sub

function mathTests_lambda_lambda_5_create_k_() as Object
    this = {}
    this.__type = "mathTests_lambda_lambda_5"
    this.__proto = ["mathTests_lambda_lambda_5", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = mathTests_lambda_lambda_5_invoke_k_
    return this
end function

sub mathTests_lambda_lambda_5_invoke_k_()
    assertEquals_AnyN_AnyN_StrN_k_(2, max_I_I_k_(1, 2), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(3, max_I_I_k_(-5, 3), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(2.5#, max_D_D_k_(1.5#, 2.5#), invalid)
end sub

function mathTests_lambda_lambda_6_create_k_() as Object
    this = {}
    this.__type = "mathTests_lambda_lambda_6"
    this.__proto = ["mathTests_lambda_lambda_6", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = mathTests_lambda_lambda_6_invoke_k_
    return this
end function

sub mathTests_lambda_lambda_6_invoke_k_()
    assertEquals_AnyN_AnyN_StrN_k_(3.0#, floor_D_k_(3.7#), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(-4.0#, floor_D_k_(-3.3#), invalid)
end sub

function mathTests_lambda_lambda_7_create_k_() as Object
    this = {}
    this.__type = "mathTests_lambda_lambda_7"
    this.__proto = ["mathTests_lambda_lambda_7", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = mathTests_lambda_lambda_7_invoke_k_
    return this
end function

sub mathTests_lambda_lambda_7_invoke_k_()
    assertEquals_AnyN_AnyN_StrN_k_(4.0#, ceil_D_k_(3.3#), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(-3.0#, ceil_D_k_(-3.7#), invalid)
end sub

function mathTests_lambda_lambda_8_create_k_() as Object
    this = {}
    this.__type = "mathTests_lambda_lambda_8"
    this.__proto = ["mathTests_lambda_lambda_8", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = mathTests_lambda_lambda_8_invoke_k_
    return this
end function

sub mathTests_lambda_lambda_8_invoke_k_()
    assertEquals_AnyN_AnyN_StrN_k_(4.0#, round_D_k_(3.7#), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(3.0#, round_D_k_(3.3#), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(4.0#, round_D_k_(3.5#), invalid)
end sub

function mathTests_lambda_lambda_9_create_k_() as Object
    this = {}
    this.__type = "mathTests_lambda_lambda_9"
    this.__proto = ["mathTests_lambda_lambda_9", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = mathTests_lambda_lambda_9_invoke_k_
    return this
end function

sub mathTests_lambda_lambda_9_invoke_k_()
    assertEquals_AnyN_AnyN_StrN_k_(3.0#, truncate_D_k_(3.7#), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(-3.0#, truncate_D_k_(-3.7#), invalid)
end sub

function mathTests_lambda_lambda_10_create_k_() as Object
    this = {}
    this.__type = "mathTests_lambda_lambda_10"
    this.__proto = ["mathTests_lambda_lambda_10", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = mathTests_lambda_lambda_10_invoke_k_
    return this
end function

sub mathTests_lambda_lambda_10_invoke_k_()
    assertEquals_AnyN_AnyN_StrN_k_(4, roundToInt_rD_k_(3.7#), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(3, roundToInt_rD_k_(3.3#), invalid)
end sub

function mathTests_lambda_lambda_11_create_k_() as Object
    this = {}
    this.__type = "mathTests_lambda_lambda_11"
    this.__proto = ["mathTests_lambda_lambda_11", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = mathTests_lambda_lambda_11_invoke_k_
    return this
end function

sub mathTests_lambda_lambda_11_invoke_k_()
    assertEquals_AnyN_AnyN_StrN_k_(4&, roundToLong_rD_k_(3.7#), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(3&, roundToLong_rD_k_(3.3#), invalid)
end sub

function mathTests_lambda_lambda_12_create_k_() as Object
    this = {}
    this.__type = "mathTests_lambda_lambda_12"
    this.__proto = ["mathTests_lambda_lambda_12", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = mathTests_lambda_lambda_12_invoke_k_
    return this
end function

sub mathTests_lambda_lambda_12_invoke_k_()
    assertEquals_AnyN_AnyN_StrN_k_(3.0#, sqrt_D_k_(9.0#), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(2.0#, sqrt_D_k_(4.0#), invalid)
    assertTrue_Z_StrN_k_((sqrt_D_k_(2.0#) > 1.41#) and (sqrt_D_k_(2.0#) < 1.42#), invalid)
end sub

function mathTests_lambda_lambda_13_create_k_() as Object
    this = {}
    this.__type = "mathTests_lambda_lambda_13"
    this.__proto = ["mathTests_lambda_lambda_13", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = mathTests_lambda_lambda_13_invoke_k_
    return this
end function

sub mathTests_lambda_lambda_13_invoke_k_()
    assertEquals_AnyN_AnyN_StrN_k_(8.0#, pow_rD_D_k_(2.0#, 3.0#), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(1.0#, pow_rD_D_k_(5.0#, 0.0#), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(0.25#, pow_rD_D_k_(2.0#, -2.0#), invalid)
end sub

function mathTests_lambda_lambda_14_create_k_() as Object
    this = {}
    this.__type = "mathTests_lambda_lambda_14"
    this.__proto = ["mathTests_lambda_lambda_14", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = mathTests_lambda_lambda_14_invoke_k_
    return this
end function

sub mathTests_lambda_lambda_14_invoke_k_()
    assertEquals_AnyN_AnyN_StrN_k_(8.0#, pow_rD_I_k_(2.0#, 3), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(1.0#, pow_rD_I_k_(5.0#, 0), invalid)
end sub

function mathTests_lambda_lambda_15_create_k_() as Object
    this = {}
    this.__type = "mathTests_lambda_lambda_15"
    this.__proto = ["mathTests_lambda_lambda_15", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = mathTests_lambda_lambda_15_invoke_k_
    return this
end function

sub mathTests_lambda_lambda_15_invoke_k_()
    assertEquals_AnyN_AnyN_StrN_k_(1.0#, exp_D_k_(0.0#), invalid)
    assertTrue_Z_StrN_k_((exp_D_k_(1.0#) > 2.71#) and (exp_D_k_(1.0#) < 2.72#), invalid)
end sub

function mathTests_lambda_lambda_16_create_k_() as Object
    this = {}
    this.__type = "mathTests_lambda_lambda_16"
    this.__proto = ["mathTests_lambda_lambda_16", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = mathTests_lambda_lambda_16_invoke_k_
    return this
end function

sub mathTests_lambda_lambda_16_invoke_k_()
    assertEquals_AnyN_AnyN_StrN_k_(0.0#, ln_D_k_(1.0#), invalid)
    assertTrue_Z_StrN_k_(abs_D_k_(ln_D_k_(2.718281828459045#) - 1.0#) < 1.0E-4#, "ln(E) should be approximately 1.0")
end sub

function mathTests_lambda_lambda_17_create_k_() as Object
    this = {}
    this.__type = "mathTests_lambda_lambda_17"
    this.__proto = ["mathTests_lambda_lambda_17", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = mathTests_lambda_lambda_17_invoke_k_
    return this
end function

sub mathTests_lambda_lambda_17_invoke_k_()
    assertEquals_AnyN_AnyN_StrN_k_(0.0#, log10_D_k_(1.0#), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(1.0#, log10_D_k_(10.0#), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(2.0#, log10_D_k_(100.0#), invalid)
end sub

function mathTests_lambda_lambda_18_create_k_() as Object
    this = {}
    this.__type = "mathTests_lambda_lambda_18"
    this.__proto = ["mathTests_lambda_lambda_18", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = mathTests_lambda_lambda_18_invoke_k_
    return this
end function

sub mathTests_lambda_lambda_18_invoke_k_()
    assertEquals_AnyN_AnyN_StrN_k_(0.0#, log2_D_k_(1.0#), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(1.0#, log2_D_k_(2.0#), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(3.0#, log2_D_k_(8.0#), invalid)
end sub

function mathTests_lambda_lambda_19_create_k_() as Object
    this = {}
    this.__type = "mathTests_lambda_lambda_19"
    this.__proto = ["mathTests_lambda_lambda_19", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = mathTests_lambda_lambda_19_invoke_k_
    return this
end function

sub mathTests_lambda_lambda_19_invoke_k_()
    assertEquals_AnyN_AnyN_StrN_k_(2.0#, log_D_D_k_(4.0#, 2.0#), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(3.0#, log_D_D_k_(8.0#, 2.0#), invalid)
end sub

function mathTests_lambda_lambda_20_create_k_() as Object
    this = {}
    this.__type = "mathTests_lambda_lambda_20"
    this.__proto = ["mathTests_lambda_lambda_20", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = mathTests_lambda_lambda_20_invoke_k_
    return this
end function

sub mathTests_lambda_lambda_20_invoke_k_()
    assertEquals_AnyN_AnyN_StrN_k_(0.0#, sin_D_k_(0.0#), invalid)
    assertTrue_Z_StrN_k_(abs_D_k_(sin_D_k_(3.141592653589793# / 2) - 1.0#) < 1.0E-4#, invalid)
end sub

function mathTests_lambda_lambda_21_create_k_() as Object
    this = {}
    this.__type = "mathTests_lambda_lambda_21"
    this.__proto = ["mathTests_lambda_lambda_21", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = mathTests_lambda_lambda_21_invoke_k_
    return this
end function

sub mathTests_lambda_lambda_21_invoke_k_()
    assertEquals_AnyN_AnyN_StrN_k_(1.0#, cos_D_k_(0.0#), invalid)
    assertTrue_Z_StrN_k_(abs_D_k_(cos_D_k_(3.141592653589793#) + 1.0#) < 1.0E-4#, invalid)
end sub

function mathTests_lambda_lambda_22_create_k_() as Object
    this = {}
    this.__type = "mathTests_lambda_lambda_22"
    this.__proto = ["mathTests_lambda_lambda_22", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = mathTests_lambda_lambda_22_invoke_k_
    return this
end function

sub mathTests_lambda_lambda_22_invoke_k_()
    assertEquals_AnyN_AnyN_StrN_k_(0.0#, tan_D_k_(0.0#), invalid)
    assertTrue_Z_StrN_k_(abs_D_k_(tan_D_k_(3.141592653589793# / 4) - 1.0#) < 1.0E-4#, invalid)
end sub

function mathTests_lambda_lambda_23_create_k_() as Object
    this = {}
    this.__type = "mathTests_lambda_lambda_23"
    this.__proto = ["mathTests_lambda_lambda_23", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = mathTests_lambda_lambda_23_invoke_k_
    return this
end function

sub mathTests_lambda_lambda_23_invoke_k_()
    assertEquals_AnyN_AnyN_StrN_k_(0.0#, asin_D_k_(0.0#), invalid)
    assertTrue_Z_StrN_k_(abs_D_k_(asin_D_k_(1.0#) - (3.141592653589793# / 2)) < 1.0E-4#, invalid)
end sub

function mathTests_lambda_lambda_24_create_k_() as Object
    this = {}
    this.__type = "mathTests_lambda_lambda_24"
    this.__proto = ["mathTests_lambda_lambda_24", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = mathTests_lambda_lambda_24_invoke_k_
    return this
end function

sub mathTests_lambda_lambda_24_invoke_k_()
    assertTrue_Z_StrN_k_(abs_D_k_(acos_D_k_(1.0#)) < 1.0E-4#, invalid)
    assertTrue_Z_StrN_k_(abs_D_k_(acos_D_k_(0.0#) - (3.141592653589793# / 2)) < 1.0E-4#, invalid)
end sub

function mathTests_lambda_lambda_25_create_k_() as Object
    this = {}
    this.__type = "mathTests_lambda_lambda_25"
    this.__proto = ["mathTests_lambda_lambda_25", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = mathTests_lambda_lambda_25_invoke_k_
    return this
end function

sub mathTests_lambda_lambda_25_invoke_k_()
    assertEquals_AnyN_AnyN_StrN_k_(0.0#, atan_D_k_(0.0#), invalid)
    assertTrue_Z_StrN_k_(abs_D_k_(atan_D_k_(1.0#) - (3.141592653589793# / 4)) < 1.0E-4#, invalid)
end sub

function mathTests_lambda_lambda_26_create_k_() as Object
    this = {}
    this.__type = "mathTests_lambda_lambda_26"
    this.__proto = ["mathTests_lambda_lambda_26", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = mathTests_lambda_lambda_26_invoke_k_
    return this
end function

sub mathTests_lambda_lambda_26_invoke_k_()
    assertEquals_AnyN_AnyN_StrN_k_(0.0#, atan2_D_D_k_(0.0#, 1.0#), invalid)
    assertTrue_Z_StrN_k_(abs_D_k_(atan2_D_D_k_(1.0#, 1.0#) - (3.141592653589793# / 4)) < 1.0E-4#, invalid)
end sub

function mathTests_lambda_lambda_27_create_k_() as Object
    this = {}
    this.__type = "mathTests_lambda_lambda_27"
    this.__proto = ["mathTests_lambda_lambda_27", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = mathTests_lambda_lambda_27_invoke_k_
    return this
end function

sub mathTests_lambda_lambda_27_invoke_k_()
    assertEquals_AnyN_AnyN_StrN_k_(0.0#, sinh_D_k_(0.0#), invalid)
end sub

function mathTests_lambda_lambda_28_create_k_() as Object
    this = {}
    this.__type = "mathTests_lambda_lambda_28"
    this.__proto = ["mathTests_lambda_lambda_28", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = mathTests_lambda_lambda_28_invoke_k_
    return this
end function

sub mathTests_lambda_lambda_28_invoke_k_()
    assertEquals_AnyN_AnyN_StrN_k_(1.0#, cosh_D_k_(0.0#), invalid)
end sub

function mathTests_lambda_lambda_29_create_k_() as Object
    this = {}
    this.__type = "mathTests_lambda_lambda_29"
    this.__proto = ["mathTests_lambda_lambda_29", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = mathTests_lambda_lambda_29_invoke_k_
    return this
end function

sub mathTests_lambda_lambda_29_invoke_k_()
    assertEquals_AnyN_AnyN_StrN_k_(0.0#, tanh_D_k_(0.0#), invalid)
end sub

function mathTests_lambda_lambda_30_create_k_() as Object
    this = {}
    this.__type = "mathTests_lambda_lambda_30"
    this.__proto = ["mathTests_lambda_lambda_30", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = mathTests_lambda_lambda_30_invoke_k_
    return this
end function

sub mathTests_lambda_lambda_30_invoke_k_()
    assertEquals_AnyN_AnyN_StrN_k_(5.0#, hypot_D_D_k_(3.0#, 4.0#), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(13.0#, hypot_D_D_k_(5.0#, 12.0#), invalid)
end sub

function mathTests_lambda_lambda_31_create_k_() as Object
    this = {}
    this.__type = "mathTests_lambda_lambda_31"
    this.__proto = ["mathTests_lambda_lambda_31", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = mathTests_lambda_lambda_31_invoke_k_
    return this
end function

sub mathTests_lambda_lambda_31_invoke_k_()
    assertEquals_AnyN_AnyN_StrN_k_(1.0#, sign_D_k_(42.0#), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(-1.0#, sign_D_k_(-42.0#), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(0.0#, sign_D_k_(0.0#), invalid)
end sub

function mathTests_lambda_lambda_32_create_k_() as Object
    this = {}
    this.__type = "mathTests_lambda_lambda_32"
    this.__proto = ["mathTests_lambda_lambda_32", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = mathTests_lambda_lambda_32_invoke_k_
    return this
end function

sub mathTests_lambda_lambda_32_invoke_k_()
    assertEquals_AnyN_AnyN_StrN_k_(1, __get_sign_rI_k_(42), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(-1, __get_sign_rI_k_(-42), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(0, __get_sign_rI_k_(0), invalid)
end sub

function mathTests_lambda_create_k_() as Object
    this = {}
    this.__type = "mathTests_lambda"
    this.__proto = ["mathTests_lambda", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = mathTests_lambda_invoke_AnyN_k_
    return this
end function

sub mathTests_lambda_invoke_AnyN_k_(_this_suite as Object)
    _this_suite.test_Str_Function0V_k_("PI", mathTests_lambda_lambda_create_k_())
    _this_suite.test_Str_Function0V_k_("E", mathTests_lambda_lambda_1_create_k_())
    _this_suite.test_Str_Function0V_k_("abs Int", mathTests_lambda_lambda_2_create_k_())
    _this_suite.test_Str_Function0V_k_("abs Double", mathTests_lambda_lambda_3_create_k_())
    _this_suite.test_Str_Function0V_k_("min", mathTests_lambda_lambda_4_create_k_())
    _this_suite.test_Str_Function0V_k_("max", mathTests_lambda_lambda_5_create_k_())
    _this_suite.test_Str_Function0V_k_("floor", mathTests_lambda_lambda_6_create_k_())
    _this_suite.test_Str_Function0V_k_("ceil", mathTests_lambda_lambda_7_create_k_())
    _this_suite.test_Str_Function0V_k_("round", mathTests_lambda_lambda_8_create_k_())
    _this_suite.test_Str_Function0V_k_("truncate", mathTests_lambda_lambda_9_create_k_())
    _this_suite.test_Str_Function0V_k_("roundToInt", mathTests_lambda_lambda_10_create_k_())
    _this_suite.test_Str_Function0V_k_("roundToLong", mathTests_lambda_lambda_11_create_k_())
    _this_suite.test_Str_Function0V_k_("sqrt", mathTests_lambda_lambda_12_create_k_())
    _this_suite.test_Str_Function0V_k_("pow", mathTests_lambda_lambda_13_create_k_())
    _this_suite.test_Str_Function0V_k_("pow Int", mathTests_lambda_lambda_14_create_k_())
    _this_suite.test_Str_Function0V_k_("exp", mathTests_lambda_lambda_15_create_k_())
    _this_suite.test_Str_Function0V_k_("ln", mathTests_lambda_lambda_16_create_k_())
    _this_suite.test_Str_Function0V_k_("log10", mathTests_lambda_lambda_17_create_k_())
    _this_suite.test_Str_Function0V_k_("log2", mathTests_lambda_lambda_18_create_k_())
    _this_suite.test_Str_Function0V_k_("log with base", mathTests_lambda_lambda_19_create_k_())
    _this_suite.test_Str_Function0V_k_("sin", mathTests_lambda_lambda_20_create_k_())
    _this_suite.test_Str_Function0V_k_("cos", mathTests_lambda_lambda_21_create_k_())
    _this_suite.test_Str_Function0V_k_("tan", mathTests_lambda_lambda_22_create_k_())
    _this_suite.test_Str_Function0V_k_("asin", mathTests_lambda_lambda_23_create_k_())
    _this_suite.test_Str_Function0V_k_("acos", mathTests_lambda_lambda_24_create_k_())
    _this_suite.test_Str_Function0V_k_("atan", mathTests_lambda_lambda_25_create_k_())
    _this_suite.test_Str_Function0V_k_("atan2", mathTests_lambda_lambda_26_create_k_())
    _this_suite.test_Str_Function0V_k_("sinh", mathTests_lambda_lambda_27_create_k_())
    _this_suite.test_Str_Function0V_k_("cosh", mathTests_lambda_lambda_28_create_k_())
    _this_suite.test_Str_Function0V_k_("tanh", mathTests_lambda_lambda_29_create_k_())
    _this_suite.test_Str_Function0V_k_("hypot", mathTests_lambda_lambda_30_create_k_())
    _this_suite.test_Str_Function0V_k_("sign", mathTests_lambda_lambda_31_create_k_())
    _this_suite.test_Str_Function0V_k_("sign Int", mathTests_lambda_lambda_32_create_k_())
end sub
