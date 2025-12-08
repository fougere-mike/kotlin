sub runTests_Function1TestRunnerV_k_(block as Function)
    runner = TestRunner_create_TestRunner_k_()
    runner.run(block)
end sub

function TestRunner_create_TestRunner_k_() as Object
    this = {}
    this.__type = "TestRunner"
    this.__proto = ["TestRunner"]
    this.adapter = JsonTestAdapter_create_JsonTestAdapter_k_()
    this.testClasses = mutableListOf_MutableListAnyN_k_()
    this.directSuites = mutableListOf_MutableListAnyN_k_()
    this.testClass_Any_k_ = TestRunner_testClass_Any_k_
    this.suite_Str_Function1TestRunnerV_k_ = TestRunner_suite_Str_Function1TestRunnerV_k_
    this.test_Str_Function0V_k_ = TestRunner_test_Str_Function0V_k_
    this.xtest_Str_Str_Function0V_k_ = TestRunner_xtest_Str_Str_Function0V_k_
    this.run_Function1TestRunnerV_k_ = TestRunner_run_Function1TestRunnerV_k_
    this.executeTestClass_Any_k_ = TestRunner_executeTestClass_Any_k_
    this.getClassName_Any_Str_k_ = TestRunner_getClassName_Any_Str_k_
    this.getTestMethods_Any_ListPairStrFunction0V_k_ = TestRunner_getTestMethods_Any_ListPairStrFunction0V_k_
    this.getTestMethodsProperty_Any_Function0AnyN_k_ = TestRunner_getTestMethodsProperty_Any_Function0AnyN_k_
    this.tryRunAsTestClass_Any_k_ = TestRunner_tryRunAsTestClass_Any_k_
    this.get_adapter = TestRunner_get_adapter_JsonTestAdapter_k_
    this.get_testClasses = TestRunner_get_testClasses_MutableListAny_k_
    this.get_directSuites = TestRunner_get_directSuites_MutableListPairStrFunction0V_k_
    return this
end function

sub TestRunner_testClass_Any_k_(instance as Object)
    m.testClasses.add(instance)
end sub

sub TestRunner_suite_Str_Function1TestRunnerV_k_(name as String, suiteFn as Function)
    m.directSuites.add(Pair_create_AnyN_AnyN_PairAnyNAnyN_k_(name, {suiteFn: suiteFn, this: this, invoke: function() as Void
        m.suiteFn.invoke(m.this)
    end function}))
end sub

sub TestRunner_test_Str_Function0V_k_(name as String, testFn as Function)
    m.adapter.test(name, false, testFn)
end sub

sub TestRunner_xtest_Str_Str_Function0V_k_(name as String, reason = "", testFn = invalid)
    m.adapter.test(name, true, testFn)
end sub

sub TestRunner_run_Function1TestRunnerV_k_(block as Function)
    m.adapter.startRun()
    block.invoke(m)
    for each <destruct> in m.directSuites
        name = destruct.component1()
        suiteFn = destruct.component2()
        m.adapter.suite(name, false, suiteFn)

    end for
    for each instance in m.testClasses
        m.executeTestClass(instance)

    end for
    m.adapter.endRun()
end sub

sub TestRunner_executeTestClass_Any_k_(instance as Object)
    className = m.getClassName(instance)
    testMethods = m.getTestMethods(instance)
    if testMethods.isEmpty() then
        m.adapter.suite(className, false, {this: this, instance: instance, invoke: function() as Void
            m.this.tryRunAsTestClass(m.instance)
        end function})
        return
    end if
    m.adapter.suite(className, false, {testMethods: testMethods, this: this, invoke: function() as Void
        for each method in m.testMethods
            methodName = method.first
            methodFn = method.second
            m.this.adapter.test(methodName, false, {methodFn: methodFn, invoke: function() as Void
                m.methodFn.invoke()
            end function})

        end for
    end function})
end sub

function TestRunner_getClassName_Any_Str_k_(instance as Object) as String
    tmp0_elvis_lhs = "/* Unsupported: IrGetClassImpl */".simpleName
    __when_tmp0 = invalid
    if tmp0_elvis_lhs = invalid then
        __when_tmp0 = "UnknownClass"
    else if true then
        __when_tmp0 = tmp0_elvis_lhs
    end if
    return __when_tmp0

end function

function TestRunner_getTestMethods_Any_ListPairStrFunction0V_k_(instance as Object) as Object
    getter = m.getTestMethodsProperty(instance)
    __when_tmp1 = invalid
    if getter <> invalid then
        __when_tmp1 = getter.invoke()
    else if true then
        __when_tmp1 = emptyList_ListAnyN_k_()
    end if
    return __when_tmp1

end function

function TestRunner_getTestMethodsProperty_Any_Function0AnyN_k_(instance as Object) as Dynamic
    return invalid
end function

sub TestRunner_tryRunAsTestClass_Any_k_(instance as Object)
end sub

function TestRunner_get_adapter_JsonTestAdapter_k_() as Object
    return m.adapter
end function

function TestRunner_get_testClasses_MutableListAny_k_() as Object
    return m.testClasses
end function

function TestRunner_get_directSuites_MutableListPairStrFunction0V_k_() as Object
    return m.directSuites
end function

function TestMethodInfo_create(name as String, ignored = false, ignoreReason = "") as Object
    this = {}
    this.__type = "TestMethodInfo"
    this.__proto = ["TestMethodInfo"]
    this.name = name
    this.ignored = ignored
    this.ignoreReason = ignoreReason
    this.component1_Str_k_ = TestMethodInfo_component1_Str_k_
    this.component2_Z_k_ = TestMethodInfo_component2_Z_k_
    this.component3_Str_k_ = TestMethodInfo_component3_Str_k_
    this.copy_Str_Z_Str_TestMethodInfo_k_ = TestMethodInfo_copy_Str_Z_Str_TestMethodInfo_k_
    this.toString_Str_k_ = TestMethodInfo_toString_Str_k_
    this.hashCode_I_k_ = TestMethodInfo_hashCode_I_k_
    this.equals_AnyN_Z_k_ = TestMethodInfo_equals_AnyN_Z_k_
    this.get_name = TestMethodInfo_get_name_Str_k_
    this.get_ignored = TestMethodInfo_get_ignored_Z_k_
    this.get_ignoreReason = TestMethodInfo_get_ignoreReason_Str_k_
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
    return TestMethodInfo_create(name, ignored, ignoreReason)
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
