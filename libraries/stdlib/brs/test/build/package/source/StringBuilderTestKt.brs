sub stringBuilderTests_rTestRunner_k_(m as Object)
    m.suite_Str_Function1TestRunnerV_k_("StringBuilder", stringBuilderTests_lambda_create_k_())
end sub

function stringBuilderTests_lambda_lambda_create_k_() as Object
    this = {}
    this.__type = "stringBuilderTests_lambda_lambda"
    this.__proto = ["stringBuilderTests_lambda_lambda", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = stringBuilderTests_lambda_lambda_invoke_k_
    return this
end function

sub stringBuilderTests_lambda_lambda_invoke_k_()
    sb = StringBuilder_create_k_()
    assertEquals_AnyN_AnyN_StrN_k_(0, sb.__get_length(), invalid)
    assertEquals_AnyN_AnyN_StrN_k_("", sb.toString(), invalid)
end sub

function stringBuilderTests_lambda_lambda_1_create_k_() as Object
    this = {}
    this.__type = "stringBuilderTests_lambda_lambda_1"
    this.__proto = ["stringBuilderTests_lambda_lambda_1", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = stringBuilderTests_lambda_lambda_1_invoke_k_
    return this
end function

sub stringBuilderTests_lambda_lambda_1_invoke_k_()
    sb = StringBuilder_create_Str_k_("hello")
    assertEquals_AnyN_AnyN_StrN_k_(5, sb.__get_length(), invalid)
    assertEquals_AnyN_AnyN_StrN_k_("hello", sb.toString(), invalid)
end sub

function stringBuilderTests_lambda_lambda_2_create_k_() as Object
    this = {}
    this.__type = "stringBuilderTests_lambda_lambda_2"
    this.__proto = ["stringBuilderTests_lambda_lambda_2", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = stringBuilderTests_lambda_lambda_2_invoke_k_
    return this
end function

sub stringBuilderTests_lambda_lambda_2_invoke_k_()
    sb = StringBuilder_create_k_()
    sb.append_StrN_k_("hello")
    sb.append_StrN_k_(" ")
    sb.append_StrN_k_("world")
    assertEquals_AnyN_AnyN_StrN_k_("hello world", sb.toString(), invalid)
end sub

function stringBuilderTests_lambda_lambda_3_create_k_() as Object
    this = {}
    this.__type = "stringBuilderTests_lambda_lambda_3"
    this.__proto = ["stringBuilderTests_lambda_lambda_3", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = stringBuilderTests_lambda_lambda_3_invoke_k_
    return this
end function

sub stringBuilderTests_lambda_lambda_3_invoke_k_()
    sb = StringBuilder_create_k_()
    sb.append_I_k_(42)
    sb.append_Z_k_(true)
    sb.append_C_k_("X")
    assertEquals_AnyN_AnyN_StrN_k_("42trueX", sb.toString(), invalid)
end sub

function stringBuilderTests_lambda_lambda_4_create_k_() as Object
    this = {}
    this.__type = "stringBuilderTests_lambda_lambda_4"
    this.__proto = ["stringBuilderTests_lambda_lambda_4", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = stringBuilderTests_lambda_lambda_4_invoke_k_
    return this
end function

sub stringBuilderTests_lambda_lambda_4_invoke_k_()
    result = StringBuilder_create_k_().append_StrN_k_("a").append_StrN_k_("b").append_StrN_k_("c").toString()
    assertEquals_AnyN_AnyN_StrN_k_("abc", result, invalid)
end sub

function stringBuilderTests_lambda_lambda_5_create_k_() as Object
    this = {}
    this.__type = "stringBuilderTests_lambda_lambda_5"
    this.__proto = ["stringBuilderTests_lambda_lambda_5", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = stringBuilderTests_lambda_lambda_5_invoke_k_
    return this
end function

sub stringBuilderTests_lambda_lambda_5_invoke_k_()
    sb = StringBuilder_create_Str_k_("hello world")
    sb.insert_I_StrN_k_(6, "beautiful ")
    assertEquals_AnyN_AnyN_StrN_k_("hello beautiful world", sb.toString(), invalid)
end sub

function stringBuilderTests_lambda_lambda_6_create_k_() as Object
    this = {}
    this.__type = "stringBuilderTests_lambda_lambda_6"
    this.__proto = ["stringBuilderTests_lambda_lambda_6", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = stringBuilderTests_lambda_lambda_6_invoke_k_
    return this
end function

sub stringBuilderTests_lambda_lambda_6_invoke_k_()
    sb = StringBuilder_create_Str_k_("world")
    sb.insert_I_StrN_k_(0, "hello ")
    assertEquals_AnyN_AnyN_StrN_k_("hello world", sb.toString(), invalid)
end sub

function stringBuilderTests_lambda_lambda_7_create_k_() as Object
    this = {}
    this.__type = "stringBuilderTests_lambda_lambda_7"
    this.__proto = ["stringBuilderTests_lambda_lambda_7", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = stringBuilderTests_lambda_lambda_7_invoke_k_
    return this
end function

sub stringBuilderTests_lambda_lambda_7_invoke_k_()
    sb = StringBuilder_create_Str_k_("hello")
    sb.deleteAt_I_k_(2)
    assertEquals_AnyN_AnyN_StrN_k_("helo", sb.toString(), invalid)
end sub

function stringBuilderTests_lambda_lambda_8_create_k_() as Object
    this = {}
    this.__type = "stringBuilderTests_lambda_lambda_8"
    this.__proto = ["stringBuilderTests_lambda_lambda_8", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = stringBuilderTests_lambda_lambda_8_invoke_k_
    return this
end function

sub stringBuilderTests_lambda_lambda_8_invoke_k_()
    sb = StringBuilder_create_Str_k_("hello")
    assertEquals_AnyN_AnyN_StrN_k_("h", sb.get_I_k_(0), invalid)
    assertEquals_AnyN_AnyN_StrN_k_("e", sb.get_I_k_(1), invalid)
    assertEquals_AnyN_AnyN_StrN_k_("o", sb.get_I_k_(4), invalid)
end sub

function stringBuilderTests_lambda_lambda_9_create_k_() as Object
    this = {}
    this.__type = "stringBuilderTests_lambda_lambda_9"
    this.__proto = ["stringBuilderTests_lambda_lambda_9", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = stringBuilderTests_lambda_lambda_9_invoke_k_
    return this
end function

sub stringBuilderTests_lambda_lambda_9_invoke_k_()
    sb = StringBuilder_create_Str_k_("hello world")
    assertEquals_AnyN_AnyN_StrN_k_("world", sb.substring_I_k_(6), invalid)
    assertEquals_AnyN_AnyN_StrN_k_("ello", sb.substring_I_I_k_(1, 5), invalid)
end sub

function stringBuilderTests_lambda_lambda_10_create_k_() as Object
    this = {}
    this.__type = "stringBuilderTests_lambda_lambda_10"
    this.__proto = ["stringBuilderTests_lambda_lambda_10", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = stringBuilderTests_lambda_lambda_10_invoke_k_
    return this
end function

sub stringBuilderTests_lambda_lambda_10_invoke_k_()
    sb = StringBuilder_create_Str_k_("hello")
    sb.reverse_k_()
    assertEquals_AnyN_AnyN_StrN_k_("olleh", sb.toString(), invalid)
end sub

function stringBuilderTests_lambda_lambda_11_create_k_() as Object
    this = {}
    this.__type = "stringBuilderTests_lambda_lambda_11"
    this.__proto = ["stringBuilderTests_lambda_lambda_11", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = stringBuilderTests_lambda_lambda_11_invoke_k_
    return this
end function

sub stringBuilderTests_lambda_lambda_11_invoke_k_()
    sb = StringBuilder_create_Str_k_("hello")
    sb.clear_k_()
    assertEquals_AnyN_AnyN_StrN_k_(0, sb.__get_length(), invalid)
    assertEquals_AnyN_AnyN_StrN_k_("", sb.toString(), invalid)
end sub

function stringBuilderTests_lambda_lambda_12_create_k_() as Object
    this = {}
    this.__type = "stringBuilderTests_lambda_lambda_12"
    this.__proto = ["stringBuilderTests_lambda_lambda_12", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = stringBuilderTests_lambda_lambda_12_invoke_k_
    return this
end function

sub stringBuilderTests_lambda_lambda_12_invoke_k_()
    sb = StringBuilder_create_k_()
    assertTrue_Z_StrN_k_(isEmpty_rCharSequence_k_(sb), invalid)
    sb.append_StrN_k_("x")
    assertFalse_Z_StrN_k_(isEmpty_rCharSequence_k_(sb), invalid)
end sub

function stringBuilderTests_lambda_lambda_13_create_k_() as Object
    this = {}
    this.__type = "stringBuilderTests_lambda_lambda_13"
    this.__proto = ["stringBuilderTests_lambda_lambda_13", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = stringBuilderTests_lambda_lambda_13_invoke_k_
    return this
end function

sub stringBuilderTests_lambda_lambda_13_invoke_k_()
    sb = StringBuilder_create_k_()
    assertFalse_Z_StrN_k_(isNotEmpty_rCharSequence_k_(sb), invalid)
    sb.append_StrN_k_("x")
    assertTrue_Z_StrN_k_(isNotEmpty_rCharSequence_k_(sb), invalid)
end sub

function stringBuilderTests_lambda_lambda_14_create_k_() as Object
    this = {}
    this.__type = "stringBuilderTests_lambda_lambda_14"
    this.__proto = ["stringBuilderTests_lambda_lambda_14", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = stringBuilderTests_lambda_lambda_14_invoke_k_
    return this
end function

sub stringBuilderTests_lambda_lambda_14_invoke_k_()
    sb = StringBuilder_create_k_()
    sb.appendLine_StrN_k_("hello")
    sb.appendLine_StrN_k_("world")
    assertTrue_Z_StrN_k_(contains_rStr_Str_Z_k_(sb.toString(), "hello", invalid), invalid)
    assertTrue_Z_StrN_k_(contains_rStr_Str_Z_k_(sb.toString(), "world", invalid), invalid)
end sub

function stringBuilderTests_lambda_create_k_() as Object
    this = {}
    this.__type = "stringBuilderTests_lambda"
    this.__proto = ["stringBuilderTests_lambda", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = stringBuilderTests_lambda_invoke_AnyN_k_
    return this
end function

sub stringBuilderTests_lambda_invoke_AnyN_k_(_this_suite as Object)
    _this_suite.test_Str_Function0V_k_("create empty", stringBuilderTests_lambda_lambda_create_k_())
    _this_suite.test_Str_Function0V_k_("create with initial content", stringBuilderTests_lambda_lambda_1_create_k_())
    _this_suite.test_Str_Function0V_k_("append string", stringBuilderTests_lambda_lambda_2_create_k_())
    _this_suite.test_Str_Function0V_k_("append various types", stringBuilderTests_lambda_lambda_3_create_k_())
    _this_suite.test_Str_Function0V_k_("append chaining", stringBuilderTests_lambda_lambda_4_create_k_())
    _this_suite.test_Str_Function0V_k_("insert", stringBuilderTests_lambda_lambda_5_create_k_())
    _this_suite.test_Str_Function0V_k_("insert at beginning", stringBuilderTests_lambda_lambda_6_create_k_())
    _this_suite.test_Str_Function0V_k_("deleteAt", stringBuilderTests_lambda_lambda_7_create_k_())
    _this_suite.test_Str_Function0V_k_("charAt", stringBuilderTests_lambda_lambda_8_create_k_())
    _this_suite.test_Str_Function0V_k_("substring", stringBuilderTests_lambda_lambda_9_create_k_())
    _this_suite.test_Str_Function0V_k_("reverse", stringBuilderTests_lambda_lambda_10_create_k_())
    _this_suite.test_Str_Function0V_k_("clear", stringBuilderTests_lambda_lambda_11_create_k_())
    _this_suite.test_Str_Function0V_k_("isEmpty", stringBuilderTests_lambda_lambda_12_create_k_())
    _this_suite.test_Str_Function0V_k_("isNotEmpty", stringBuilderTests_lambda_lambda_13_create_k_())
    _this_suite.test_Str_Function0V_k_("appendLine", stringBuilderTests_lambda_lambda_14_create_k_())
end sub
