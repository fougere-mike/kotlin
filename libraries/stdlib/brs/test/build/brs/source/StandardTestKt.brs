sub standardFunctionsTests_rTestRunner_k_(m as Object)
    m.suite_Str_Function1TestRunnerV_k_("Standard Functions", standardFunctionsTests_lambda_create_k_())
end sub

function standardFunctionsTests_lambda_lambda_lambda_create_k_() as Object
    this = {}
    this.__type = "standardFunctionsTests_lambda_lambda_lambda"
    this.__proto = ["standardFunctionsTests_lambda_lambda_lambda", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = standardFunctionsTests_lambda_lambda_lambda_invoke_AnyN_k_
    return this
end function

function standardFunctionsTests_lambda_lambda_lambda_invoke_AnyN_k_(it as String) as Integer
    return Len(it)
end function

function standardFunctionsTests_lambda_lambda_create_k_() as Object
    this = {}
    this.__type = "standardFunctionsTests_lambda_lambda"
    this.__proto = ["standardFunctionsTests_lambda_lambda", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = standardFunctionsTests_lambda_lambda_invoke_k_
    return this
end function

sub standardFunctionsTests_lambda_lambda_invoke_k_()
    result = let_rAnyN_Function1_k_("hello", standardFunctionsTests_lambda_lambda_lambda_create_k_())
    assertEquals_AnyN_AnyN_StrN_k_(5, result, invalid)
end sub

function standardFunctionsTests_lambda_lambda_lambda_1_create_k_() as Object
    this = {}
    this.__type = "standardFunctionsTests_lambda_lambda_lambda_1"
    this.__proto = ["standardFunctionsTests_lambda_lambda_lambda_1", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = standardFunctionsTests_lambda_lambda_lambda_1_invoke_AnyN_k_
    return this
end function

function standardFunctionsTests_lambda_lambda_lambda_1_invoke_AnyN_k_(it as String) as Integer
    return Len(it)
end function

function standardFunctionsTests_lambda_lambda_1_create_k_() as Object
    this = {}
    this.__type = "standardFunctionsTests_lambda_lambda_1"
    this.__proto = ["standardFunctionsTests_lambda_lambda_1", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = standardFunctionsTests_lambda_lambda_1_invoke_k_
    return this
end function

sub standardFunctionsTests_lambda_lambda_1_invoke_k_()
    str = invalid
    tmp0_safe_receiver = str
    __when_tmp0 = invalid
    if tmp0_safe_receiver = invalid then
        __when_tmp0 = invalid
    else if true then
        __when_tmp0 = let_rAnyN_Function1_k_(tmp0_safe_receiver, standardFunctionsTests_lambda_lambda_lambda_1_create_k_())
    end if
    result = __when_tmp0

    assertNull_AnyN_StrN_k_(result, invalid)
end sub

function standardFunctionsTests_lambda_lambda_lambda_2_create_k_() as Object
    this = {}
    this.__type = "standardFunctionsTests_lambda_lambda_lambda_2"
    this.__proto = ["standardFunctionsTests_lambda_lambda_lambda_2", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = standardFunctionsTests_lambda_lambda_lambda_2_invoke_AnyN_k_
    return this
end function

function standardFunctionsTests_lambda_lambda_lambda_2_invoke_AnyN_k_(it as Integer) as Integer
    return it * 2
end function

function standardFunctionsTests_lambda_lambda_lambda_3_create_k_() as Object
    this = {}
    this.__type = "standardFunctionsTests_lambda_lambda_lambda_3"
    this.__proto = ["standardFunctionsTests_lambda_lambda_lambda_3", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = standardFunctionsTests_lambda_lambda_lambda_3_invoke_AnyN_k_
    return this
end function

function standardFunctionsTests_lambda_lambda_lambda_3_invoke_AnyN_k_(it as Integer) as Integer
    return it + 1
end function

function standardFunctionsTests_lambda_lambda_2_create_k_() as Object
    this = {}
    this.__type = "standardFunctionsTests_lambda_lambda_2"
    this.__proto = ["standardFunctionsTests_lambda_lambda_2", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = standardFunctionsTests_lambda_lambda_2_invoke_k_
    return this
end function

sub standardFunctionsTests_lambda_lambda_2_invoke_k_()
    result = let_rAnyN_Function1_k_(let_rAnyN_Function1_k_(5, standardFunctionsTests_lambda_lambda_lambda_2_create_k_()), standardFunctionsTests_lambda_lambda_lambda_3_create_k_())
    assertEquals_AnyN_AnyN_StrN_k_(11, result, invalid)
end sub

function standardFunctionsTests_lambda_lambda_lambda_4_create_k_() as Object
    this = {}
    this.__type = "standardFunctionsTests_lambda_lambda_lambda_4"
    this.__proto = ["standardFunctionsTests_lambda_lambda_lambda_4", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = standardFunctionsTests_lambda_lambda_lambda_4_invoke_AnyN_k_
    return this
end function

function standardFunctionsTests_lambda_lambda_lambda_4_invoke_AnyN_k_(_this_run as String) as Integer
    return Len(_this_run)
end function

function standardFunctionsTests_lambda_lambda_3_create_k_() as Object
    this = {}
    this.__type = "standardFunctionsTests_lambda_lambda_3"
    this.__proto = ["standardFunctionsTests_lambda_lambda_3", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = standardFunctionsTests_lambda_lambda_3_invoke_k_
    return this
end function

sub standardFunctionsTests_lambda_lambda_3_invoke_k_()
    result = run_rAnyN_Function1_k_("hello", standardFunctionsTests_lambda_lambda_lambda_4_create_k_())
    assertEquals_AnyN_AnyN_StrN_k_(5, result, invalid)
end sub

function standardFunctionsTests_lambda_lambda_lambda_5_create_k_() as Object
    this = {}
    this.__type = "standardFunctionsTests_lambda_lambda_lambda_5"
    this.__proto = ["standardFunctionsTests_lambda_lambda_lambda_5", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = standardFunctionsTests_lambda_lambda_lambda_5_invoke_k_
    return this
end function

function standardFunctionsTests_lambda_lambda_lambda_5_invoke_k_() as Integer
    a = 1
    b = 2
    return a + b
end function

function standardFunctionsTests_lambda_lambda_4_create_k_() as Object
    this = {}
    this.__type = "standardFunctionsTests_lambda_lambda_4"
    this.__proto = ["standardFunctionsTests_lambda_lambda_4", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = standardFunctionsTests_lambda_lambda_4_invoke_k_
    return this
end function

sub standardFunctionsTests_lambda_lambda_4_invoke_k_()
    result = run_Function0_k_(standardFunctionsTests_lambda_lambda_lambda_5_create_k_())
    assertEquals_AnyN_AnyN_StrN_k_(3, result, invalid)
end sub

function standardFunctionsTests_lambda_lambda_lambda_6_create_AnyN_k_(_sideEffect as Dynamic) as Object
    this = {}
    this.__type = "standardFunctionsTests_lambda_lambda_lambda_6"
    this.__proto = ["standardFunctionsTests_lambda_lambda_lambda_6", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = standardFunctionsTests_lambda_lambda_lambda_6_invoke_AnyN_k_
    this._sideEffect = _sideEffect
    return this
end function

sub standardFunctionsTests_lambda_lambda_lambda_6_invoke_AnyN_k_(it as String)
    m._sideEffect.value = Len(it)
end sub

function standardFunctionsTests_lambda_lambda_5_create_k_() as Object
    this = {}
    this.__type = "standardFunctionsTests_lambda_lambda_5"
    this.__proto = ["standardFunctionsTests_lambda_lambda_5", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = standardFunctionsTests_lambda_lambda_5_invoke_k_
    return this
end function

sub standardFunctionsTests_lambda_lambda_5_invoke_k_()
    sideEffect = {value: 0}
    result = also_rAnyN_Function1V_k_("hello", standardFunctionsTests_lambda_lambda_lambda_6_create_AnyN_k_(sideEffect))
    assertEquals_AnyN_AnyN_StrN_k_("hello", result, invalid)
    assertEquals_AnyN_AnyN_StrN_k_(5, sideEffect.value, invalid)
end sub

function standardFunctionsTests_lambda_lambda_lambda_7_create_k_() as Object
    this = {}
    this.__type = "standardFunctionsTests_lambda_lambda_lambda_7"
    this.__proto = ["standardFunctionsTests_lambda_lambda_lambda_7", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = standardFunctionsTests_lambda_lambda_lambda_7_invoke_AnyN_k_
    return this
end function

sub standardFunctionsTests_lambda_lambda_lambda_7_invoke_AnyN_k_(it as Object)
    it.add_AnyN_k_(1)
end sub

function standardFunctionsTests_lambda_lambda_lambda_8_create_k_() as Object
    this = {}
    this.__type = "standardFunctionsTests_lambda_lambda_lambda_8"
    this.__proto = ["standardFunctionsTests_lambda_lambda_lambda_8", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = standardFunctionsTests_lambda_lambda_lambda_8_invoke_AnyN_k_
    return this
end function

sub standardFunctionsTests_lambda_lambda_lambda_8_invoke_AnyN_k_(it as Object)
    it.add_AnyN_k_(2)
end sub

function standardFunctionsTests_lambda_lambda_lambda_9_create_k_() as Object
    this = {}
    this.__type = "standardFunctionsTests_lambda_lambda_lambda_9"
    this.__proto = ["standardFunctionsTests_lambda_lambda_lambda_9", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = standardFunctionsTests_lambda_lambda_lambda_9_invoke_AnyN_k_
    return this
end function

sub standardFunctionsTests_lambda_lambda_lambda_9_invoke_AnyN_k_(it as Object)
    it.add_AnyN_k_(3)
end sub

function standardFunctionsTests_lambda_lambda_6_create_k_() as Object
    this = {}
    this.__type = "standardFunctionsTests_lambda_lambda_6"
    this.__proto = ["standardFunctionsTests_lambda_lambda_6", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = standardFunctionsTests_lambda_lambda_6_invoke_k_
    return this
end function

sub standardFunctionsTests_lambda_lambda_6_invoke_k_()
    list = mutableListOf_k_()
    result = also_rAnyN_Function1V_k_(also_rAnyN_Function1V_k_(also_rAnyN_Function1V_k_(list, standardFunctionsTests_lambda_lambda_lambda_7_create_k_()), standardFunctionsTests_lambda_lambda_lambda_8_create_k_()), standardFunctionsTests_lambda_lambda_lambda_9_create_k_())
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([1, 2, 3]), result, invalid)
end sub

function standardFunctionsTests_lambda_lambda_lambda_10_create_k_() as Object
    this = {}
    this.__type = "standardFunctionsTests_lambda_lambda_lambda_10"
    this.__proto = ["standardFunctionsTests_lambda_lambda_lambda_10", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = standardFunctionsTests_lambda_lambda_lambda_10_invoke_AnyN_k_
    return this
end function

sub standardFunctionsTests_lambda_lambda_lambda_10_invoke_AnyN_k_(_this_apply as Object)
    _this_apply.append_StrN_k_("hello")
    _this_apply.append_StrN_k_(" ")
    _this_apply.append_StrN_k_("world")
end sub

function standardFunctionsTests_lambda_lambda_7_create_k_() as Object
    this = {}
    this.__type = "standardFunctionsTests_lambda_lambda_7"
    this.__proto = ["standardFunctionsTests_lambda_lambda_7", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = standardFunctionsTests_lambda_lambda_7_invoke_k_
    return this
end function

sub standardFunctionsTests_lambda_lambda_7_invoke_k_()
    sb = apply_rAnyN_Function1V_k_(StringBuilder_create_k_(), standardFunctionsTests_lambda_lambda_lambda_10_create_k_())
    assertEquals_AnyN_AnyN_StrN_k_("hello world", sb.toString(), invalid)
end sub

function Config_create_Str_I_k_(name = "", value = 0) as Object
    this = {}
    this.__type = "Config"
    this.__proto = ["Config"]
    this.__id = __kotlin_nextObjectId()
    this.name = name
    this.value = value
    this.equals = Config_equals
    this.hashCode = Config_hashCode
    this.toString = Config_toString
    this.copy = Config_copy
    this.component1 = Config_component1
    this.component2 = Config_component2
    return this
end function

function Config_equals(other as Object) as Boolean
    if Type(other) <> "roAssociativeArray" then
        return false
    end if
    if other.__type <> "Config" then
        return false
    end if
    if m.name <> other.name then
        return false
    end if
    if m.value <> other.value then
        return false
    end if
    return true
end function

function Config_hashCode() as Integer
    result = 1
    result = ((result * 31) + m.name)
    result = ((result * 31) + m.value)
    return result
end function

function Config_toString() as String
    return ((("Config(name=" + m.name) + ", value=") + m.value) + ")"
end function

function Config_copy(name = invalid, value = invalid) as Object
    if name = invalid then
        name = m.name
    end if
    if value = invalid then
        value = m.value
    end if
    return Config_create_Str_I_k_(name, value)
end function

function Config_component1() as String
    return m.name
end function

function Config_component2() as Integer
    return m.value
end function

function standardFunctionsTests_lambda_lambda_lambda_11_create_k_() as Object
    this = {}
    this.__type = "standardFunctionsTests_lambda_lambda_lambda_11"
    this.__proto = ["standardFunctionsTests_lambda_lambda_lambda_11", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = standardFunctionsTests_lambda_lambda_lambda_11_invoke_AnyN_k_
    return this
end function

sub standardFunctionsTests_lambda_lambda_lambda_11_invoke_AnyN_k_(_this_apply as Object)
    _this_apply.name = "test"
    _this_apply.value = 42
end sub

function standardFunctionsTests_lambda_lambda_8_create_k_() as Object
    this = {}
    this.__type = "standardFunctionsTests_lambda_lambda_8"
    this.__proto = ["standardFunctionsTests_lambda_lambda_8", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = standardFunctionsTests_lambda_lambda_8_invoke_k_
    return this
end function

sub standardFunctionsTests_lambda_lambda_8_invoke_k_()
    config = apply_rAnyN_Function1V_k_(Config_create_Str_I_k_(), standardFunctionsTests_lambda_lambda_lambda_11_create_k_())
    assertEquals_AnyN_AnyN_StrN_k_("test", config.name, invalid)
    assertEquals_AnyN_AnyN_StrN_k_(42, config.value, invalid)
end sub

function standardFunctionsTests_lambda_lambda_9_create_k_() as Object
    this = {}
    this.__type = "standardFunctionsTests_lambda_lambda_9"
    this.__proto = ["standardFunctionsTests_lambda_lambda_9", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = standardFunctionsTests_lambda_lambda_9_invoke_k_
    return this
end function

sub standardFunctionsTests_lambda_lambda_9_invoke_k_()
    check_Z_k_(true)
    assertTrue_Z_StrN_k_(true, invalid)
end sub

function standardFunctionsTests_lambda_lambda_10_create_k_() as Object
    this = {}
    this.__type = "standardFunctionsTests_lambda_lambda_10"
    this.__proto = ["standardFunctionsTests_lambda_lambda_10", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = standardFunctionsTests_lambda_lambda_10_invoke_k_
    return this
end function

sub standardFunctionsTests_lambda_lambda_10_invoke_k_()
    require_Z_k_(true)
    assertTrue_Z_StrN_k_(true, invalid)
end sub

function standardFunctionsTests_lambda_lambda_11_create_k_() as Object
    this = {}
    this.__type = "standardFunctionsTests_lambda_lambda_11"
    this.__proto = ["standardFunctionsTests_lambda_lambda_11", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = standardFunctionsTests_lambda_lambda_11_invoke_k_
    return this
end function

sub standardFunctionsTests_lambda_lambda_11_invoke_k_()
    pair = to_rAnyN_AnyN_k_("key", 42)
    assertEquals_AnyN_AnyN_StrN_k_("key", pair.first, invalid)
    assertEquals_AnyN_AnyN_StrN_k_(42, pair.second, invalid)
end sub

function standardFunctionsTests_lambda_lambda_12_create_k_() as Object
    this = {}
    this.__type = "standardFunctionsTests_lambda_lambda_12"
    this.__proto = ["standardFunctionsTests_lambda_lambda_12", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = standardFunctionsTests_lambda_lambda_12_invoke_k_
    return this
end function

sub standardFunctionsTests_lambda_lambda_12_invoke_k_()
    __destruct_2 = to_rAnyN_AnyN_k_("hello", "world")
    a = __destruct_2.component1()
    b = __destruct_2.component2()
    assertEquals_AnyN_AnyN_StrN_k_("hello", a, invalid)
    assertEquals_AnyN_AnyN_StrN_k_("world", b, invalid)
end sub

function standardFunctionsTests_lambda_create_k_() as Object
    this = {}
    this.__type = "standardFunctionsTests_lambda"
    this.__proto = ["standardFunctionsTests_lambda", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = standardFunctionsTests_lambda_invoke_AnyN_k_
    return this
end function

sub standardFunctionsTests_lambda_invoke_AnyN_k_(_this_suite as Object)
    _this_suite.test_Str_Function0V_k_("let returns lambda result", standardFunctionsTests_lambda_lambda_create_k_())
    _this_suite.test_Str_Function0V_k_("let with null", standardFunctionsTests_lambda_lambda_1_create_k_())
    _this_suite.test_Str_Function0V_k_("let chain", standardFunctionsTests_lambda_lambda_2_create_k_())
    _this_suite.test_Str_Function0V_k_("run returns lambda result", standardFunctionsTests_lambda_lambda_3_create_k_())
    _this_suite.test_Str_Function0V_k_("run without receiver", standardFunctionsTests_lambda_lambda_4_create_k_())
    _this_suite.test_Str_Function0V_k_("also returns receiver", standardFunctionsTests_lambda_lambda_5_create_k_())
    _this_suite.test_Str_Function0V_k_("also chain", standardFunctionsTests_lambda_lambda_6_create_k_())
    _this_suite.test_Str_Function0V_k_("apply returns receiver", standardFunctionsTests_lambda_lambda_7_create_k_())
    _this_suite.test_Str_Function0V_k_("apply for configuration", standardFunctionsTests_lambda_lambda_8_create_k_())
    _this_suite.test_Str_Function0V_k_("check passes when true", standardFunctionsTests_lambda_lambda_9_create_k_())
    _this_suite.test_Str_Function0V_k_("require passes when true", standardFunctionsTests_lambda_lambda_10_create_k_())
    _this_suite.test_Str_Function0V_k_("to creates Pair", standardFunctionsTests_lambda_lambda_11_create_k_())
    _this_suite.test_Str_Function0V_k_("Pair destructuring", standardFunctionsTests_lambda_lambda_12_create_k_())
end sub
