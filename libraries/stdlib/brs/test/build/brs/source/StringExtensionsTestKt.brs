sub stringExtensionsTests_rTestRunner_k_(m as Object)
    m.suite_Str_Function1TestRunnerV_k_("String Extensions", stringExtensionsTests_lambda_create_k_())
end sub

function stringExtensionsTests_lambda_lambda_create_k_() as Object
    this = {}
    this.__type = "stringExtensionsTests_lambda_lambda"
    this.__proto = ["stringExtensionsTests_lambda_lambda", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = stringExtensionsTests_lambda_lambda_invoke_k_
    return this
end function

sub stringExtensionsTests_lambda_lambda_invoke_k_()
    assertEquals_AnyN_AnyN_StrN_k_(5, Len("hello"), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(0, Len(""), invalid)
end sub

function stringExtensionsTests_lambda_lambda_1_create_k_() as Object
    this = {}
    this.__type = "stringExtensionsTests_lambda_lambda_1"
    this.__proto = ["stringExtensionsTests_lambda_lambda_1", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = stringExtensionsTests_lambda_lambda_1_invoke_k_
    return this
end function

sub stringExtensionsTests_lambda_lambda_1_invoke_k_()
    assertTrue_Z_StrN_k_(isEmpty_rStr_k_(""), invalid)
    assertFalse_Z_StrN_k_(isEmpty_rStr_k_("hello"), invalid)
    assertFalse_Z_StrN_k_(isNotEmpty_rStr_k_(""), invalid)
    assertTrue_Z_StrN_k_(isNotEmpty_rStr_k_("hello"), invalid)
end sub

function stringExtensionsTests_lambda_lambda_2_create_k_() as Object
    this = {}
    this.__type = "stringExtensionsTests_lambda_lambda_2"
    this.__proto = ["stringExtensionsTests_lambda_lambda_2", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = stringExtensionsTests_lambda_lambda_2_invoke_k_
    return this
end function

sub stringExtensionsTests_lambda_lambda_2_invoke_k_()
    assertTrue_Z_StrN_k_(isBlank_rStr_k_(""), invalid)
    assertTrue_Z_StrN_k_(isBlank_rStr_k_("   "), invalid)
    assertFalse_Z_StrN_k_(isBlank_rStr_k_("hello"), invalid)
    assertTrue_Z_StrN_k_(isNotBlank_rStr_k_("hello"), invalid)
end sub

function stringExtensionsTests_lambda_lambda_3_create_k_() as Object
    this = {}
    this.__type = "stringExtensionsTests_lambda_lambda_3"
    this.__proto = ["stringExtensionsTests_lambda_lambda_3", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = stringExtensionsTests_lambda_lambda_3_invoke_k_
    return this
end function

sub stringExtensionsTests_lambda_lambda_3_invoke_k_()
    assertEquals_AnyN_AnyN_StrN_k_("HELLO", uppercase_rStr_k_("hello"), invalid)
    assertEquals_AnyN_AnyN_StrN_k_("HELLO WORLD", uppercase_rStr_k_("Hello World"), invalid)
end sub

function stringExtensionsTests_lambda_lambda_4_create_k_() as Object
    this = {}
    this.__type = "stringExtensionsTests_lambda_lambda_4"
    this.__proto = ["stringExtensionsTests_lambda_lambda_4", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = stringExtensionsTests_lambda_lambda_4_invoke_k_
    return this
end function

sub stringExtensionsTests_lambda_lambda_4_invoke_k_()
    assertEquals_AnyN_AnyN_StrN_k_("hello", lowercase_rStr_k_("HELLO"), invalid)
    assertEquals_AnyN_AnyN_StrN_k_("hello world", lowercase_rStr_k_("Hello World"), invalid)
end sub

function stringExtensionsTests_lambda_lambda_5_create_k_() as Object
    this = {}
    this.__type = "stringExtensionsTests_lambda_lambda_5"
    this.__proto = ["stringExtensionsTests_lambda_lambda_5", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = stringExtensionsTests_lambda_lambda_5_invoke_k_
    return this
end function

sub stringExtensionsTests_lambda_lambda_5_invoke_k_()
    assertEquals_AnyN_AnyN_StrN_k_("ello", substring_rStr_I_k_("hello", 1), invalid)
    assertEquals_AnyN_AnyN_StrN_k_("ell", substring_rStr_I_I_k_("hello", 1, 4), invalid)
end sub

function stringExtensionsTests_lambda_lambda_6_create_k_() as Object
    this = {}
    this.__type = "stringExtensionsTests_lambda_lambda_6"
    this.__proto = ["stringExtensionsTests_lambda_lambda_6", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = stringExtensionsTests_lambda_lambda_6_invoke_k_
    return this
end function

sub stringExtensionsTests_lambda_lambda_6_invoke_k_()
    assertTrue_Z_StrN_k_(contains_rStr_Str_Z_k_("hello world", "world", invalid), invalid)
    assertFalse_Z_StrN_k_(contains_rStr_Str_Z_k_("hello world", "xyz", invalid), invalid)
    assertTrue_Z_StrN_k_(contains_rStr_Str_Z_k_("Hello World", "world", true), invalid)
end sub

function stringExtensionsTests_lambda_lambda_7_create_k_() as Object
    this = {}
    this.__type = "stringExtensionsTests_lambda_lambda_7"
    this.__proto = ["stringExtensionsTests_lambda_lambda_7", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = stringExtensionsTests_lambda_lambda_7_invoke_k_
    return this
end function

sub stringExtensionsTests_lambda_lambda_7_invoke_k_()
    assertTrue_Z_StrN_k_(startsWith_rStr_Str_Z_k_("hello", "hel", invalid), invalid)
    assertFalse_Z_StrN_k_(startsWith_rStr_Str_Z_k_("hello", "xyz", invalid), invalid)
    assertTrue_Z_StrN_k_(startsWith_rStr_Str_Z_k_("Hello", "hel", true), invalid)
end sub

function stringExtensionsTests_lambda_lambda_8_create_k_() as Object
    this = {}
    this.__type = "stringExtensionsTests_lambda_lambda_8"
    this.__proto = ["stringExtensionsTests_lambda_lambda_8", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = stringExtensionsTests_lambda_lambda_8_invoke_k_
    return this
end function

sub stringExtensionsTests_lambda_lambda_8_invoke_k_()
    assertTrue_Z_StrN_k_(endsWith_rStr_Str_Z_k_("hello", "llo", invalid), invalid)
    assertFalse_Z_StrN_k_(endsWith_rStr_Str_Z_k_("hello", "xyz", invalid), invalid)
    assertTrue_Z_StrN_k_(endsWith_rStr_Str_Z_k_("Hello", "LLO", true), invalid)
end sub

function stringExtensionsTests_lambda_lambda_9_create_k_() as Object
    this = {}
    this.__type = "stringExtensionsTests_lambda_lambda_9"
    this.__proto = ["stringExtensionsTests_lambda_lambda_9", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = stringExtensionsTests_lambda_lambda_9_invoke_k_
    return this
end function

sub stringExtensionsTests_lambda_lambda_9_invoke_k_()
    assertEquals_AnyN_AnyN_StrN_k_(6, indexOf_rStr_Str_I_Z_k_("hello world", "world", invalid, invalid), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(-1, indexOf_rStr_Str_I_Z_k_("hello", "xyz", invalid, invalid), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(0, indexOf_rStr_Str_I_Z_k_("hello hello", "hello", invalid, invalid), invalid)
end sub

function stringExtensionsTests_lambda_lambda_10_create_k_() as Object
    this = {}
    this.__type = "stringExtensionsTests_lambda_lambda_10"
    this.__proto = ["stringExtensionsTests_lambda_lambda_10", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = stringExtensionsTests_lambda_lambda_10_invoke_k_
    return this
end function

sub stringExtensionsTests_lambda_lambda_10_invoke_k_()
    assertEquals_AnyN_AnyN_StrN_k_(6, lastIndexOf_rStr_Str_I_Z_k_("hello hello", "hello", invalid, invalid), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(-1, lastIndexOf_rStr_Str_I_Z_k_("hello", "xyz", invalid, invalid), invalid)
end sub

function stringExtensionsTests_lambda_lambda_11_create_k_() as Object
    this = {}
    this.__type = "stringExtensionsTests_lambda_lambda_11"
    this.__proto = ["stringExtensionsTests_lambda_lambda_11", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = stringExtensionsTests_lambda_lambda_11_invoke_k_
    return this
end function

sub stringExtensionsTests_lambda_lambda_11_invoke_k_()
    assertEquals_AnyN_AnyN_StrN_k_("  abc", padStart_rStr_I_C_k_("abc", 5, invalid), invalid)
    assertEquals_AnyN_AnyN_StrN_k_("00abc", padStart_rStr_I_C_k_("abc", 5, "0"), invalid)
end sub

function stringExtensionsTests_lambda_lambda_12_create_k_() as Object
    this = {}
    this.__type = "stringExtensionsTests_lambda_lambda_12"
    this.__proto = ["stringExtensionsTests_lambda_lambda_12", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = stringExtensionsTests_lambda_lambda_12_invoke_k_
    return this
end function

sub stringExtensionsTests_lambda_lambda_12_invoke_k_()
    assertEquals_AnyN_AnyN_StrN_k_("abc  ", padEnd_rStr_I_C_k_("abc", 5, invalid), invalid)
    assertEquals_AnyN_AnyN_StrN_k_("abc00", padEnd_rStr_I_C_k_("abc", 5, "0"), invalid)
end sub

function stringExtensionsTests_lambda_lambda_13_create_k_() as Object
    this = {}
    this.__type = "stringExtensionsTests_lambda_lambda_13"
    this.__proto = ["stringExtensionsTests_lambda_lambda_13", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = stringExtensionsTests_lambda_lambda_13_invoke_k_
    return this
end function

sub stringExtensionsTests_lambda_lambda_13_invoke_k_()
    assertEquals_AnyN_AnyN_StrN_k_("h", Mid("hello", 0 + 1, 1), invalid)
    assertEquals_AnyN_AnyN_StrN_k_("o", Mid("hello", 4 + 1, 1), invalid)
end sub

function stringExtensionsTests_lambda_lambda_14_create_k_() as Object
    this = {}
    this.__type = "stringExtensionsTests_lambda_lambda_14"
    this.__proto = ["stringExtensionsTests_lambda_lambda_14", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = stringExtensionsTests_lambda_lambda_14_invoke_k_
    return this
end function

sub stringExtensionsTests_lambda_lambda_14_invoke_k_()
    assertTrue_Z_StrN_k_("hello" = "hello", invalid)
    assertFalse_Z_StrN_k_("hello" = "HELLO", invalid)
    assertTrue_Z_StrN_k_(equals_rStr_StrN_Z_k_("hello", "HELLO", true), invalid)
end sub

function stringExtensionsTests_lambda_lambda_15_create_k_() as Object
    this = {}
    this.__type = "stringExtensionsTests_lambda_lambda_15"
    this.__proto = ["stringExtensionsTests_lambda_lambda_15", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = stringExtensionsTests_lambda_lambda_15_invoke_k_
    return this
end function

sub stringExtensionsTests_lambda_lambda_15_invoke_k_()
    assertTrue_Z_StrN_k_(__kotlin_stringCompare("abc", "xyz") < 0, invalid)
    assertTrue_Z_StrN_k_(__kotlin_stringCompare("xyz", "abc") > 0, invalid)
    assertEquals_AnyN_AnyN_StrN_k_(0, __kotlin_stringCompare("abc", "abc"), invalid)
end sub

function stringExtensionsTests_lambda_lambda_16_create_k_() as Object
    this = {}
    this.__type = "stringExtensionsTests_lambda_lambda_16"
    this.__proto = ["stringExtensionsTests_lambda_lambda_16", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = stringExtensionsTests_lambda_lambda_16_invoke_k_
    return this
end function

sub stringExtensionsTests_lambda_lambda_16_invoke_k_()
    assertTrue_Z_StrN_k_(isNullOrEmpty_rCharSequenceN_k_(invalid), invalid)
    assertTrue_Z_StrN_k_(isNullOrEmpty_rCharSequenceN_k_(""), invalid)
    assertFalse_Z_StrN_k_(isNullOrEmpty_rCharSequenceN_k_("hello"), invalid)
end sub

function stringExtensionsTests_lambda_lambda_17_create_k_() as Object
    this = {}
    this.__type = "stringExtensionsTests_lambda_lambda_17"
    this.__proto = ["stringExtensionsTests_lambda_lambda_17", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = stringExtensionsTests_lambda_lambda_17_invoke_k_
    return this
end function

sub stringExtensionsTests_lambda_lambda_17_invoke_k_()
    assertEquals_AnyN_AnyN_StrN_k_("abcabcabc", repeat_rStr_I_k_("abc", 3), invalid)
    assertEquals_AnyN_AnyN_StrN_k_("", repeat_rStr_I_k_("abc", 0), invalid)
end sub

function stringExtensionsTests_lambda_lambda_18_create_k_() as Object
    this = {}
    this.__type = "stringExtensionsTests_lambda_lambda_18"
    this.__proto = ["stringExtensionsTests_lambda_lambda_18", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = stringExtensionsTests_lambda_lambda_18_invoke_k_
    return this
end function

sub stringExtensionsTests_lambda_lambda_18_invoke_k_()
    assertEquals_AnyN_AnyN_StrN_k_(4, __get_lastIndex_rStr_k_("hello"), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(-1, __get_lastIndex_rStr_k_(""), invalid)
end sub

function stringExtensionsTests_lambda_create_k_() as Object
    this = {}
    this.__type = "stringExtensionsTests_lambda"
    this.__proto = ["stringExtensionsTests_lambda", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = stringExtensionsTests_lambda_invoke_AnyN_k_
    return this
end function

sub stringExtensionsTests_lambda_invoke_AnyN_k_(_this_suite as Object)
    _this_suite.test_Str_Function0V_k_("length", stringExtensionsTests_lambda_lambda_create_k_())
    _this_suite.test_Str_Function0V_k_("isEmpty and isNotEmpty", stringExtensionsTests_lambda_lambda_1_create_k_())
    _this_suite.test_Str_Function0V_k_("isBlank and isNotBlank", stringExtensionsTests_lambda_lambda_2_create_k_())
    _this_suite.test_Str_Function0V_k_("uppercase", stringExtensionsTests_lambda_lambda_3_create_k_())
    _this_suite.test_Str_Function0V_k_("lowercase", stringExtensionsTests_lambda_lambda_4_create_k_())
    _this_suite.test_Str_Function0V_k_("substring", stringExtensionsTests_lambda_lambda_5_create_k_())
    _this_suite.test_Str_Function0V_k_("contains", stringExtensionsTests_lambda_lambda_6_create_k_())
    _this_suite.test_Str_Function0V_k_("startsWith", stringExtensionsTests_lambda_lambda_7_create_k_())
    _this_suite.test_Str_Function0V_k_("endsWith", stringExtensionsTests_lambda_lambda_8_create_k_())
    _this_suite.test_Str_Function0V_k_("indexOf", stringExtensionsTests_lambda_lambda_9_create_k_())
    _this_suite.test_Str_Function0V_k_("lastIndexOf", stringExtensionsTests_lambda_lambda_10_create_k_())
    _this_suite.test_Str_Function0V_k_("padStart", stringExtensionsTests_lambda_lambda_11_create_k_())
    _this_suite.test_Str_Function0V_k_("padEnd", stringExtensionsTests_lambda_lambda_12_create_k_())
    _this_suite.test_Str_Function0V_k_("get char", stringExtensionsTests_lambda_lambda_13_create_k_())
    _this_suite.test_Str_Function0V_k_("equals", stringExtensionsTests_lambda_lambda_14_create_k_())
    _this_suite.test_Str_Function0V_k_("compareTo", stringExtensionsTests_lambda_lambda_15_create_k_())
    _this_suite.test_Str_Function0V_k_("isNullOrEmpty", stringExtensionsTests_lambda_lambda_16_create_k_())
    _this_suite.test_Str_Function0V_k_("repeat", stringExtensionsTests_lambda_lambda_17_create_k_())
    _this_suite.test_Str_Function0V_k_("lastIndex", stringExtensionsTests_lambda_lambda_18_create_k_())
end sub
