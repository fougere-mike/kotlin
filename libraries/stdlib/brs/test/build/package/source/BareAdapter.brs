function BareAdapter_create_BareAdapter_k_() as Object
    this = {}
    this.__type = "BareAdapter"
    this.__proto = ["BareAdapter"]
    this.results = mutableListOf_MutableListAnyN_k_()
    this.currentSuite = ""
    this.suite_Str_Z_Function0V_k_ = BareAdapter_suite_Str_Z_Function0V_k_
    this.test_Str_Z_Function0V_k_ = BareAdapter_test_Str_Z_Function0V_k_
    this.printSummary = BareAdapter_printSummary
    this.get_results = BareAdapter_get_results_MutableListTestResult_k_
    this.get_currentSuite = BareAdapter_get_currentSuite_Str_k_
    this.set_currentSuite = BareAdapter_set_currentSuite_Str_k_
    return this
end function

sub BareAdapter_suite_Str_Z_Function0V_k_(name as String, ignored as Boolean, suiteFn as Function)
    if ignored then
        println_AnyN_k_("[SUITE IGNORED] " + name)
        return
    end if
    m.currentSuite = name
    println_AnyN_k_("[SUITE START] " + name)
    suiteFn.invoke()
    println_AnyN_k_("[SUITE PASS] " + name)
end sub

sub BareAdapter_test_Str_Z_Function0V_k_(name as String, ignored as Boolean, testFn as Function)
    __when_tmp0 = invalid
    if isNotEmpty_rStr_Z_k_(m.currentSuite) then
        __when_tmp0 = ((m.currentSuite + ".") + name)
    else if true then
        __when_tmp0 = name
    end if
    fullName = __when_tmp0

    if ignored then
        println_AnyN_k_("[TEST IGNORED] " + fullName)
        m.results.add(BareAdapter_TestResult_create_Str_Str_Str_StrN_TestResult_k_(m.currentSuite, name, "IGNORED", invalid))
        return
    end if
    println_AnyN_k_("[TEST START] " + fullName)
    testFn.invoke()
    println_AnyN_k_("[TEST PASS] " + fullName)
    m.results.add(BareAdapter_TestResult_create_Str_Str_Str_StrN_TestResult_k_(m.currentSuite, name, "PASS", invalid))
end sub

sub BareAdapter_printSummary()
    total = m.results.size
    passed = count_rIterableAnyN_Function1AnyNZ_I_k_(m.results, {invoke: function(it as Object) as Boolean
        return it.status = "PASS"
    end function})
    failed = count_rIterableAnyN_Function1AnyNZ_I_k_(m.results, {invoke: function(it as Object) as Boolean
        return it.status = "FAIL"
    end function})
    ignored = count_rIterableAnyN_Function1AnyNZ_I_k_(m.results, {invoke: function(it as Object) as Boolean
        return it.status = "IGNORED"
    end function})
    println_AnyN_k_(chr(10) + "[TEST SUMMARY]")
    println_AnyN_k_("Total: " + total)
    println_AnyN_k_("Passed: " + passed)
    println_AnyN_k_("Failed: " + failed)
    println_AnyN_k_("Ignored: " + ignored)
    println_AnyN_k_("[TEST SUMMARY END]")
end sub

function BareAdapter_get_results_MutableListTestResult_k_() as Object
    return m.results
end function

function BareAdapter_get_currentSuite_Str_k_() as String
    return m.currentSuite
end function

sub BareAdapter_set_currentSuite_Str_k_(value as String)
    m.currentSuite = value
end sub

function BareAdapter_TestResult_create(suite as String, test as String, status as String, message as Dynamic) as Object
    this = {}
    this.__type = "BareAdapter_TestResult"
    this.__proto = ["BareAdapter_TestResult"]
    this.suite = suite
    this.test = test
    this.status = status
    this.message = message
    this.component1_Str_k_ = BareAdapter_TestResult_component1_Str_k_
    this.component2_Str_k_ = BareAdapter_TestResult_component2_Str_k_
    this.component3_Str_k_ = BareAdapter_TestResult_component3_Str_k_
    this.component4_StrN_k_ = BareAdapter_TestResult_component4_StrN_k_
    this.copy_Str_Str_Str_StrN_TestResult_k_ = BareAdapter_TestResult_copy_Str_Str_Str_StrN_TestResult_k_
    this.toString_Str_k_ = BareAdapter_TestResult_toString_Str_k_
    this.hashCode_I_k_ = BareAdapter_TestResult_hashCode_I_k_
    this.equals_AnyN_Z_k_ = BareAdapter_TestResult_equals_AnyN_Z_k_
    this.get_suite = BareAdapter_TestResult_get_suite_Str_k_
    this.get_test = BareAdapter_TestResult_get_test_Str_k_
    this.get_status = BareAdapter_TestResult_get_status_Str_k_
    this.get_message = BareAdapter_TestResult_get_message_StrN_k_
    this.equals = BareAdapter_TestResult_equals
    this.hashCode = BareAdapter_TestResult_hashCode
    this.toString = BareAdapter_TestResult_toString
    this.copy = BareAdapter_TestResult_copy
    this.component1 = BareAdapter_TestResult_component1
    this.component2 = BareAdapter_TestResult_component2
    this.component3 = BareAdapter_TestResult_component3
    this.component4 = BareAdapter_TestResult_component4
    return this
end function

function BareAdapter_TestResult_equals(other as Object) as Boolean
    if Type(other) <> "roAssociativeArray" then
        return false
    end if
    if other.__type <> "BareAdapter_TestResult" then
        return false
    end if
    if m.suite <> other.suite then
        return false
    end if
    if m.test <> other.test then
        return false
    end if
    if m.status <> other.status then
        return false
    end if
    if m.message <> other.message then
        return false
    end if
    return true
end function

function BareAdapter_TestResult_hashCode() as Integer
    result = 1
    result = ((result * 31) + m.suite)
    result = ((result * 31) + m.test)
    result = ((result * 31) + m.status)
    result = ((result * 31) + m.message)
    return result
end function

function BareAdapter_TestResult_toString() as String
    return ((((((("BareAdapter_TestResult(suite=" + m.suite) + ", test=") + m.test) + ", status=") + m.status) + ", message=") + m.message) + ")"
end function

function BareAdapter_TestResult_copy(suite = invalid, test = invalid, status = invalid, message = invalid) as Object
    if suite = invalid then
        suite = m.suite
    end if
    if test = invalid then
        test = m.test
    end if
    if status = invalid then
        status = m.status
    end if
    if message = invalid then
        message = m.message
    end if
    return BareAdapter_TestResult_create(suite, test, status, message)
end function

function BareAdapter_TestResult_component1() as String
    return m.suite
end function

function BareAdapter_TestResult_component2() as String
    return m.test
end function

function BareAdapter_TestResult_component3() as String
    return m.status
end function

function BareAdapter_TestResult_component4() as Dynamic
    return m.message
end function
