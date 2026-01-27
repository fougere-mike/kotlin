sub jsonTests_rTestRunner_k_(m as Object)
    m.suite_Str_Function1TestRunnerV_k_("JSON Formatting", jsonTests_lambda_create_k_())
end sub

function jsonTests_lambda_lambda_create_k_() as Object
    this = {}
    this.__type = "jsonTests_lambda_lambda"
    this.__proto = ["jsonTests_lambda_lambda", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = jsonTests_lambda_lambda_invoke_k_
    return this
end function

sub jsonTests_lambda_lambda_invoke_k_()
    map = mapOf_Arr_k_([to_rAnyN_AnyN_k_("name", "test"), to_rAnyN_AnyN_k_("value", 42)])
    json = FormatJson(__kotlin_toJsonValue_AnyN_k_(map))
    assertTrue_Z_StrN_k_(contains_rStr_Str_Z_k_(json, "name", invalid), invalid)
    assertTrue_Z_StrN_k_(contains_rStr_Str_Z_k_(json, "test", invalid), invalid)
    assertTrue_Z_StrN_k_(contains_rStr_Str_Z_k_(json, "value", invalid), invalid)
    assertTrue_Z_StrN_k_(contains_rStr_Str_Z_k_(json, "42", invalid), invalid)
end sub

function jsonTests_lambda_lambda_1_create_k_() as Object
    this = {}
    this.__type = "jsonTests_lambda_lambda_1"
    this.__proto = ["jsonTests_lambda_lambda_1", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = jsonTests_lambda_lambda_1_invoke_k_
    return this
end function

sub jsonTests_lambda_lambda_1_invoke_k_()
    map = mapOf_Pair_k_(to_rAnyN_AnyN_k_("outer", mapOf_Pair_k_(to_rAnyN_AnyN_k_("inner", "value"))))
    json = FormatJson(__kotlin_toJsonValue_AnyN_k_(map))
    assertTrue_Z_StrN_k_(contains_rStr_Str_Z_k_(json, "outer", invalid), invalid)
    assertTrue_Z_StrN_k_(contains_rStr_Str_Z_k_(json, "inner", invalid), invalid)
    assertTrue_Z_StrN_k_(contains_rStr_Str_Z_k_(json, "value", invalid), invalid)
end sub

function jsonTests_lambda_lambda_2_create_k_() as Object
    this = {}
    this.__type = "jsonTests_lambda_lambda_2"
    this.__proto = ["jsonTests_lambda_lambda_2", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = jsonTests_lambda_lambda_2_invoke_k_
    return this
end function

sub jsonTests_lambda_lambda_2_invoke_k_()
    list = listOf_Arr_k_([1, 2, 3])
    json = FormatJson(__kotlin_toJsonValue_AnyN_k_(list))
    assertTrue_Z_StrN_k_(contains_rStr_Str_Z_k_(json, "1", invalid), invalid)
    assertTrue_Z_StrN_k_(contains_rStr_Str_Z_k_(json, "2", invalid), invalid)
    assertTrue_Z_StrN_k_(contains_rStr_Str_Z_k_(json, "3", invalid), invalid)
end sub

function jsonTests_lambda_lambda_3_create_k_() as Object
    this = {}
    this.__type = "jsonTests_lambda_lambda_3"
    this.__proto = ["jsonTests_lambda_lambda_3", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = jsonTests_lambda_lambda_3_invoke_k_
    return this
end function

sub jsonTests_lambda_lambda_3_invoke_k_()
    map = mapOf_Arr_k_([to_rAnyN_AnyN_k_("string", "hello"), to_rAnyN_AnyN_k_("number", 123), to_rAnyN_AnyN_k_("boolean", true), to_rAnyN_AnyN_k_("list", listOf_Arr_k_([1, 2, 3]))])
    json = FormatJson(__kotlin_toJsonValue_AnyN_k_(map))
    assertTrue_Z_StrN_k_(contains_rStr_Str_Z_k_(json, "string", invalid), invalid)
    assertTrue_Z_StrN_k_(contains_rStr_Str_Z_k_(json, "hello", invalid), invalid)
    assertTrue_Z_StrN_k_(contains_rStr_Str_Z_k_(json, "number", invalid), invalid)
    assertTrue_Z_StrN_k_(contains_rStr_Str_Z_k_(json, "123", invalid), invalid)
    assertTrue_Z_StrN_k_(contains_rStr_Str_Z_k_(json, "boolean", invalid), invalid)
    assertTrue_Z_StrN_k_(contains_rStr_Str_Z_k_(json, "true", invalid), invalid)
    assertTrue_Z_StrN_k_(contains_rStr_Str_Z_k_(json, "list", invalid), invalid)
end sub

function jsonTests_lambda_lambda_4_create_k_() as Object
    this = {}
    this.__type = "jsonTests_lambda_lambda_4"
    this.__proto = ["jsonTests_lambda_lambda_4", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = jsonTests_lambda_lambda_4_invoke_k_
    return this
end function

sub jsonTests_lambda_lambda_4_invoke_k_()
    map = emptyMap_k_()
    json = FormatJson(__kotlin_toJsonValue_AnyN_k_(map))
    assertTrue_Z_StrN_k_(contains_rStr_Str_Z_k_(json, "{", invalid), invalid)
    assertTrue_Z_StrN_k_(contains_rStr_Str_Z_k_(json, "}", invalid), invalid)
end sub

function jsonTests_lambda_lambda_5_create_k_() as Object
    this = {}
    this.__type = "jsonTests_lambda_lambda_5"
    this.__proto = ["jsonTests_lambda_lambda_5", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = jsonTests_lambda_lambda_5_invoke_k_
    return this
end function

sub jsonTests_lambda_lambda_5_invoke_k_()
    list = emptyList_k_()
    json = FormatJson(__kotlin_toJsonValue_AnyN_k_(list))
    assertTrue_Z_StrN_k_(contains_rStr_Str_Z_k_(json, "[", invalid), invalid)
    assertTrue_Z_StrN_k_(contains_rStr_Str_Z_k_(json, "]", invalid), invalid)
end sub

function jsonTests_lambda_lambda_6_create_k_() as Object
    this = {}
    this.__type = "jsonTests_lambda_lambda_6"
    this.__proto = ["jsonTests_lambda_lambda_6", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = jsonTests_lambda_lambda_6_invoke_k_
    return this
end function

sub jsonTests_lambda_lambda_6_invoke_k_()
    json = FormatJson(__kotlin_toJsonValue_AnyN_k_(invalid))
    assertTrue_Z_StrN_k_(contains_rStr_Str_Z_k_(json, "null", invalid), invalid)
end sub

function jsonTests_lambda_create_k_() as Object
    this = {}
    this.__type = "jsonTests_lambda"
    this.__proto = ["jsonTests_lambda", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = jsonTests_lambda_invoke_AnyN_k_
    return this
end function

sub jsonTests_lambda_invoke_AnyN_k_(_this_suite as Object)
    _this_suite.test_Str_Function0V_k_("format simple map", jsonTests_lambda_lambda_create_k_())
    _this_suite.test_Str_Function0V_k_("format nested map", jsonTests_lambda_lambda_1_create_k_())
    _this_suite.test_Str_Function0V_k_("format list", jsonTests_lambda_lambda_2_create_k_())
    _this_suite.test_Str_Function0V_k_("format mixed types", jsonTests_lambda_lambda_3_create_k_())
    _this_suite.test_Str_Function0V_k_("format empty map", jsonTests_lambda_lambda_4_create_k_())
    _this_suite.test_Str_Function0V_k_("format empty list", jsonTests_lambda_lambda_5_create_k_())
    _this_suite.test_Str_Function0V_k_("format null", jsonTests_lambda_lambda_6_create_k_())
end sub
