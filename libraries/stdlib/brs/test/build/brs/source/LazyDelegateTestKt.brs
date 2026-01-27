sub lazyDelegateTests_rTestRunner_k_(m as Object)
    m.suite_Str_Function1TestRunnerV_k_("Lazy Delegates", lazyDelegateTests_lambda_create_k_())
end sub

function lazyDelegateTests_lambda_lambda_lambda_create_AnyN_k_(_initCount as Dynamic) as Object
    this = {}
    this.__type = "lazyDelegateTests_lambda_lambda_lambda"
    this.__proto = ["lazyDelegateTests_lambda_lambda_lambda", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = lazyDelegateTests_lambda_lambda_lambda_invoke_k_
    this._initCount = _initCount
    return this
end function

function lazyDelegateTests_lambda_lambda_lambda_invoke_k_() as String
    m._initCount.value = (m._initCount.value + 1)
    return "value"
end function

function lazyDelegateTests_lambda_lambda_create_k_() as Object
    this = {}
    this.__type = "lazyDelegateTests_lambda_lambda"
    this.__proto = ["lazyDelegateTests_lambda_lambda", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = lazyDelegateTests_lambda_lambda_invoke_k_
    return this
end function

sub lazyDelegateTests_lambda_lambda_invoke_k_()
    initCount = {value: 0}
    lazyInstance = lazy_Function0_k_(lazyDelegateTests_lambda_lambda_lambda_create_AnyN_k_(initCount))
    assertFalse_Z_StrN_k_(lazyInstance.isInitialized_k_(), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(0, initCount.value, invalid)
    assertEquals_AnyN_AnyN_StrN_k_("value", lazyInstance.__get_value(), invalid)
    assertTrue_Z_StrN_k_(lazyInstance.isInitialized_k_(), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(1, initCount.value, invalid)
    assertEquals_AnyN_AnyN_StrN_k_("value", lazyInstance.__get_value(), invalid)
    assertTrue_Z_StrN_k_(lazyInstance.isInitialized_k_(), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(1, initCount.value, invalid)
end sub

function lazyDelegateTests_lambda_lambda_lambda_1_create_k_() as Object
    this = {}
    this.__type = "lazyDelegateTests_lambda_lambda_lambda_1"
    this.__proto = ["lazyDelegateTests_lambda_lambda_lambda_1", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = lazyDelegateTests_lambda_lambda_lambda_1_invoke_k_
    return this
end function

function lazyDelegateTests_lambda_lambda_lambda_1_invoke_k_() as String
    return "hello"
end function

function lazyDelegateTests_lambda_lambda_1_create_k_() as Object
    this = {}
    this.__type = "lazyDelegateTests_lambda_lambda_1"
    this.__proto = ["lazyDelegateTests_lambda_lambda_1", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = lazyDelegateTests_lambda_lambda_1_invoke_k_
    return this
end function

sub lazyDelegateTests_lambda_lambda_1_invoke_k_()
    lazyInstance = lazy_Function0_k_(lazyDelegateTests_lambda_lambda_lambda_1_create_k_())
    assertEquals_AnyN_AnyN_StrN_k_("Lazy value not initialized yet.", toString_AnyN_k_(lazyInstance), invalid)
    lazyInstance.__get_value()
    assertEquals_AnyN_AnyN_StrN_k_("hello", toString_AnyN_k_(lazyInstance), invalid)
end sub

function lazyDelegateTests_lambda_create_k_() as Object
    this = {}
    this.__type = "lazyDelegateTests_lambda"
    this.__proto = ["lazyDelegateTests_lambda", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = lazyDelegateTests_lambda_invoke_AnyN_k_
    return this
end function

sub lazyDelegateTests_lambda_invoke_AnyN_k_(_this_suite as Object)
    _this_suite.test_Str_Function0V_k_("lazy isInitialized check", lazyDelegateTests_lambda_lambda_create_k_())
    _this_suite.test_Str_Function0V_k_("lazy toString before and after initialization", lazyDelegateTests_lambda_lambda_1_create_k_())
end sub
