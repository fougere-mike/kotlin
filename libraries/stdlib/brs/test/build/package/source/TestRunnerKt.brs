sub runTests_Function1TestRunnerV_k_(block as Object)
    println_AnyN_k_("[DEBUG] runTests starting")
    runner = TestRunner_create_k_()
    println_AnyN_k_("[DEBUG] TestRunner created")
    runner.run_Function1TestRunnerV_k_(block)
    println_AnyN_k_("[DEBUG] runTests complete")
end sub

function TestRunner_create_k_() as Object
    this = {}
    this.__type = "TestRunner"
    this.__proto = ["TestRunner"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.testClass_Any_k_ = TestRunner_testClass_Any_k_
    this.suite_Str_Function1TestRunnerV_k_ = TestRunner_suite_Str_Function1TestRunnerV_k_
    this.test_Str_Function0V_k_ = TestRunner_test_Str_Function0V_k_
    this.xtest_Str_Str_Function0V_k_ = TestRunner_xtest_Str_Str_Function0V_k_
    this.run_Function1TestRunnerV_k_ = TestRunner_run_Function1TestRunnerV_k_
    this.executeTestClass_Any_k_ = TestRunner_executeTestClass_Any_k_
    this.getClassName_Any_k_ = TestRunner_getClassName_Any_k_
    this.getTestMethods_Any_k_ = TestRunner_getTestMethods_Any_k_
    this.getTestMethodsProperty_Any_k_ = TestRunner_getTestMethodsProperty_Any_k_
    this.tryRunAsTestClass_Any_k_ = TestRunner_tryRunAsTestClass_Any_k_
    this.__get_adapter = TestRunner___get_adapter_k_
    this.__get_testClasses = TestRunner___get_testClasses_k_
    this.__get_directSuites = TestRunner___get_directSuites_k_
    this.adapter = JsonTestAdapter_create_k_()
    this.testClasses = mutableListOf_k_()
    this.directSuites = mutableListOf_k_()
    return this
end function

sub TestRunner_testClass_Any_k_(instance as Object)
    m.__get_testClasses().add_AnyN_k_(instance)
end sub

sub TestRunner_suite_Str_Function1TestRunnerV_k_(name as String, suiteFn as Object)
    m.__get_directSuites().add_AnyN_k_(Pair_create_AnyN_AnyN_k_(name, TestRunner_suite_lambda_create_Function1TestRunnerV_TestRunner_k_(suiteFn, m)))
end sub

sub TestRunner_test_Str_Function0V_k_(name as String, testFn as Object)
    m.__get_adapter().test_Str_Z_Function0V_k_(name, false, testFn)
end sub

sub TestRunner_xtest_Str_Str_Function0V_k_(name as String, reason = "", testFn = invalid)
    if reason = invalid then
        reason = ""
    end if
    m.__get_adapter().test_Str_Z_Function0V_k_(name, true, testFn)
end sub

sub TestRunner_run_Function1TestRunnerV_k_(block as Object)
    println_AnyN_k_("[DEBUG] TestRunner.run starting")
    println_AnyN_k_("[DEBUG] About to call adapter.startRun()")
    m.__get_adapter().startRun_k_()
    println_AnyN_k_("[DEBUG] adapter.startRun() complete")
    println_AnyN_k_("[DEBUG] About to execute registration block")
    block.invoke_AnyN_k_(m)
    println_AnyN_k_("[DEBUG] Registration block complete")
    __iter_0 = m.__get_directSuites().iterator_k_()
    while __iter_0.hasNext_k_()
        __destruct_0 = __iter_0.next_k_()
        name = __destruct_0.component1()
        suiteFn = __destruct_0.component2()
        m.__get_adapter().suite_Str_Z_Function0V_k_(name, false, suiteFn)

    end while

    __iter_1 = m.__get_testClasses().iterator_k_()
    while __iter_1.hasNext_k_()
        instance = __iter_1.next_k_()
        m.executeTestClass_Any_k_(instance)

    end while

    m.__get_adapter().endRun_k_()
end sub

sub TestRunner_executeTestClass_Any_k_(instance as Object)
    className = m.getClassName_Any_k_(instance)
    testMethods = m.getTestMethods_Any_k_(instance)
    if testMethods.isEmpty_k_() then
        m.__get_adapter().suite_Str_Z_Function0V_k_(className, false, TestRunner_executeTestClass_lambda_create_TestRunner_Any_k_(m, instance))
        return
    end if
    m.__get_adapter().suite_Str_Z_Function0V_k_(className, false, TestRunner_executeTestClass_lambda_1_create_ListPairStrFunction0V_TestRunner_k_(testMethods, m))
end sub

function TestRunner_getClassName_Any_k_(instance as Object) as String
    tmp0_elvis_lhs = __kotlin_getClass(instance).__get_simpleName()
    __when_tmp0 = invalid
    if tmp0_elvis_lhs = invalid then
        __when_tmp0 = "UnknownClass"
    else if true then
        __when_tmp0 = tmp0_elvis_lhs
    end if
    return __when_tmp0

end function

function TestRunner_getTestMethods_Any_k_(instance as Object) as Object
    getter = m.getTestMethodsProperty_Any_k_(instance)
    __when_tmp1 = invalid
    if getter <> invalid then
        __when_tmp1 = getter.invoke_k_()
    else if true then
        __when_tmp1 = emptyList_k_()
    end if
    return __when_tmp1

end function

function TestRunner_getTestMethodsProperty_Any_k_(instance as Object) as Dynamic
    return invalid
end function

sub TestRunner_tryRunAsTestClass_Any_k_(instance as Object)
end sub

function TestRunner___get_adapter_k_() as Object
    return m.adapter
end function

function TestRunner___get_testClasses_k_() as Object
    return m.testClasses
end function

function TestRunner___get_directSuites_k_() as Object
    return m.directSuites
end function

function TestMethodInfo_create_Str_Z_Str_k_(name as String, ignored = false, ignoreReason = "") as Object
    this = {}
    this.__type = "TestMethodInfo"
    this.__proto = ["TestMethodInfo"]
    this.__id = __kotlin_nextObjectId()
    this.name = name
    this.ignored = ignored
    this.ignoreReason = ignoreReason
    this.equals = TestMethodInfo_equals
    this.hashCode = TestMethodInfo_hashCode
    this.toString = TestMethodInfo_toString
    this.copy = TestMethodInfo_copy
    this.component1 = TestMethodInfo_component1
    this.component2 = TestMethodInfo_component2
    this.component3 = TestMethodInfo_component3
    return this
end function

function TestMethodInfo_equals(other as Object) as Boolean
    if Type(other) <> "roAssociativeArray" then
        return false
    end if
    if other.__type <> "TestMethodInfo" then
        return false
    end if
    if m.name <> other.name then
        return false
    end if
    if m.ignored <> other.ignored then
        return false
    end if
    if m.ignoreReason <> other.ignoreReason then
        return false
    end if
    return true
end function

function TestMethodInfo_hashCode() as Integer
    result = 1
    result = ((result * 31) + m.name)
    result = ((result * 31) + m.ignored)
    result = ((result * 31) + m.ignoreReason)
    return result
end function

function TestMethodInfo_toString() as String
    return ((((("TestMethodInfo(name=" + m.name) + ", ignored=") + m.ignored) + ", ignoreReason=") + m.ignoreReason) + ")"
end function

function TestMethodInfo_copy(name = invalid, ignored = invalid, ignoreReason = invalid) as Object
    if name = invalid then
        name = m.name
    end if
    if ignored = invalid then
        ignored = m.ignored
    end if
    if ignoreReason = invalid then
        ignoreReason = m.ignoreReason
    end if
    return TestMethodInfo_create_Str_Z_Str_k_(name, ignored, ignoreReason)
end function

function TestMethodInfo_component1() as String
    return m.name
end function

function TestMethodInfo_component2() as Boolean
    return m.ignored
end function

function TestMethodInfo_component3() as String
    return m.ignoreReason
end function

function TestRunner_suite_lambda_create_Function1TestRunnerV_TestRunner_k_(_suiteFn as Object, this_0 as Object) as Object
    this = {}
    this.__type = "TestRunner_suite_lambda"
    this.__proto = ["TestRunner_suite_lambda", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = TestRunner_suite_lambda_invoke_k_
    this._suiteFn = _suiteFn
    this.this_0 = this_0
    return this
end function

sub TestRunner_suite_lambda_invoke_k_()
    m._suiteFn.invoke_AnyN_k_(m.this_0)
end sub

function TestRunner_executeTestClass_lambda_create_TestRunner_Any_k_(this_0 as Object, _instance as Object) as Object
    this = {}
    this.__type = "TestRunner_executeTestClass_lambda"
    this.__proto = ["TestRunner_executeTestClass_lambda", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = TestRunner_executeTestClass_lambda_invoke_k_
    this.this_0 = this_0
    this._instance = _instance
    return this
end function

sub TestRunner_executeTestClass_lambda_invoke_k_()
    m.this_0.tryRunAsTestClass_Any_k_(m._instance)
end sub

function TestRunner_executeTestClass_lambda_lambda_create_Function0V_k_(_methodFn as Object) as Object
    this = {}
    this.__type = "TestRunner_executeTestClass_lambda_lambda"
    this.__proto = ["TestRunner_executeTestClass_lambda_lambda", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = TestRunner_executeTestClass_lambda_lambda_invoke_k_
    this._methodFn = _methodFn
    return this
end function

sub TestRunner_executeTestClass_lambda_lambda_invoke_k_()
    m._methodFn.invoke_k_()
end sub

function TestRunner_executeTestClass_lambda_1_create_ListPairStrFunction0V_TestRunner_k_(_testMethods as Object, this_0 as Object) as Object
    this = {}
    this.__type = "TestRunner_executeTestClass_lambda_1"
    this.__proto = ["TestRunner_executeTestClass_lambda_1", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = TestRunner_executeTestClass_lambda_1_invoke_k_
    this._testMethods = _testMethods
    this.this_0 = this_0
    return this
end function

sub TestRunner_executeTestClass_lambda_1_invoke_k_()
    __iter_2 = m._testMethods.iterator_k_()
    while __iter_2.hasNext_k_()
        method = __iter_2.next_k_()
        methodName = method.first
        methodFn = method.second
        m.this_0.__get_adapter().test_Str_Z_Function0V_k_(methodName, false, TestRunner_executeTestClass_lambda_lambda_create_Function0V_k_(methodFn))

    end while

end sub
