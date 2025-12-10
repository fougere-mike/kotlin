function JsonTestAdapter_create_k_() as Object
    this = {}
    this.__type = "JsonTestAdapter"
    this.__proto = ["JsonTestAdapter", "FrameworkAdapter"]
    this.__id = __kotlin_nextObjectId()
    this.startRun_k_ = JsonTestAdapter_startRun_k_
    this.endRun_k_ = JsonTestAdapter_endRun_k_
    this.suite_Str_Z_Function0V_k_ = JsonTestAdapter_suite_Str_Z_Function0V_k_
    this.test_Str_Z_Function0V_k_ = JsonTestAdapter_test_Str_Z_Function0V_k_
    this.emitJson_MapStrAnyN_k_ = JsonTestAdapter_emitJson_MapStrAnyN_k_
    this.currentTimeMillis_k_ = JsonTestAdapter_currentTimeMillis_k_
    this.get_currentSuite = JsonTestAdapter_get_currentSuite_k_
    this.set_currentSuite = JsonTestAdapter_set_currentSuite_Str_k_
    this.get_suiteTimer = JsonTestAdapter_get_suiteTimer_k_
    this.set_suiteTimer = JsonTestAdapter_set_suiteTimer_RoTimespan_k_
    this.get_testTimer = JsonTestAdapter_get_testTimer_k_
    this.set_testTimer = JsonTestAdapter_set_testTimer_RoTimespan_k_
    this.get_runTimer = JsonTestAdapter_get_runTimer_k_
    this.set_runTimer = JsonTestAdapter_set_runTimer_RoTimespan_k_
    this.get_suitePassed = JsonTestAdapter_get_suitePassed_k_
    this.set_suitePassed = JsonTestAdapter_set_suitePassed_I_k_
    this.get_suiteFailed = JsonTestAdapter_get_suiteFailed_k_
    this.set_suiteFailed = JsonTestAdapter_set_suiteFailed_I_k_
    this.get_suiteIgnored = JsonTestAdapter_get_suiteIgnored_k_
    this.set_suiteIgnored = JsonTestAdapter_set_suiteIgnored_I_k_
    this.get_totalSuites = JsonTestAdapter_get_totalSuites_k_
    this.set_totalSuites = JsonTestAdapter_set_totalSuites_I_k_
    this.get_totalPassed = JsonTestAdapter_get_totalPassed_k_
    this.set_totalPassed = JsonTestAdapter_set_totalPassed_I_k_
    this.get_totalFailed = JsonTestAdapter_get_totalFailed_k_
    this.set_totalFailed = JsonTestAdapter_set_totalFailed_I_k_
    this.get_totalIgnored = JsonTestAdapter_get_totalIgnored_k_
    this.set_totalIgnored = JsonTestAdapter_set_totalIgnored_I_k_
    this.get_runStarted = JsonTestAdapter_get_runStarted_k_
    this.set_runStarted = JsonTestAdapter_set_runStarted_Z_k_
    this.currentSuite = ""
    this.suiteTimer = RoTimespan_create_k_()
    this.testTimer = RoTimespan_create_k_()
    this.runTimer = RoTimespan_create_k_()
    this.suitePassed = 0
    this.suiteFailed = 0
    this.suiteIgnored = 0
    this.totalSuites = 0
    this.totalPassed = 0
    this.totalFailed = 0
    this.totalIgnored = 0
    this.runStarted = false
    return this
end function

sub JsonTestAdapter_startRun_k_()
    m.set_runStarted(true)
    m.get_runTimer().mark_k_()
    println_AnyN_k_("[KOTLINTEST_START]")
    m.emitJson_MapStrAnyN_k_(mapOf_Arr_k_([to_rAnyN_AnyN_k_("type", "run_start"), to_rAnyN_AnyN_k_("timestamp", m.currentTimeMillis_k_())]))
end sub

sub JsonTestAdapter_endRun_k_()
    duration = m.get_runTimer().totalMilliseconds_k_()
    m.emitJson_MapStrAnyN_k_(mapOf_Arr_k_([to_rAnyN_AnyN_k_("type", "run_complete"), to_rAnyN_AnyN_k_("total_suites", m.get_totalSuites()), to_rAnyN_AnyN_k_("total_tests", (m.get_totalPassed() + m.get_totalFailed()) + m.get_totalIgnored()), to_rAnyN_AnyN_k_("passed", m.get_totalPassed()), to_rAnyN_AnyN_k_("failed", m.get_totalFailed()), to_rAnyN_AnyN_k_("ignored", m.get_totalIgnored()), to_rAnyN_AnyN_k_("duration_ms", duration)]))
    println_AnyN_k_("[KOTLINTEST_END]")
end sub

sub JsonTestAdapter_suite_Str_Z_Function0V_k_(name as String, ignored as Boolean, suiteFn as Object)
    if not m.get_runStarted() then
        m.startRun_k_()
    end if
    if ignored then
        m.emitJson_MapStrAnyN_k_(mapOf_Arr_k_([to_rAnyN_AnyN_k_("type", "suite_ignored"), to_rAnyN_AnyN_k_("suite", name), to_rAnyN_AnyN_k_("reason", "Suite marked as ignored")]))
        return
    end if
    m.set_currentSuite(name)
    m.get_suiteTimer().mark_k_()
    m.set_suitePassed(0)
    m.set_suiteFailed(0)
    m.set_suiteIgnored(0)
    m.set_totalSuites(m.get_totalSuites() + 1)
    m.emitJson_MapStrAnyN_k_(mapOf_Arr_k_([to_rAnyN_AnyN_k_("type", "suite_start"), to_rAnyN_AnyN_k_("suite", name), to_rAnyN_AnyN_k_("timestamp", m.currentTimeMillis_k_())]))
    suiteFn.invoke()
    duration = m.get_suiteTimer().totalMilliseconds_k_()
    m.emitJson_MapStrAnyN_k_(mapOf_Arr_k_([to_rAnyN_AnyN_k_("type", "suite_end"), to_rAnyN_AnyN_k_("suite", name), to_rAnyN_AnyN_k_("passed", m.get_suitePassed()), to_rAnyN_AnyN_k_("failed", m.get_suiteFailed()), to_rAnyN_AnyN_k_("ignored", m.get_suiteIgnored()), to_rAnyN_AnyN_k_("duration_ms", duration)]))
end sub

sub JsonTestAdapter_test_Str_Z_Function0V_k_(name as String, ignored as Boolean, testFn as Object)
    if ignored then
        m.set_suiteIgnored(m.get_suiteIgnored() + 1)
        m.set_totalIgnored(m.get_totalIgnored() + 1)
        m.emitJson_MapStrAnyN_k_(mapOf_Arr_k_([to_rAnyN_AnyN_k_("type", "test_ignored"), to_rAnyN_AnyN_k_("suite", m.get_currentSuite()), to_rAnyN_AnyN_k_("test", name), to_rAnyN_AnyN_k_("reason", "Test marked as ignored")]))
        return
    end if
    m.get_testTimer().mark_k_()
    m.emitJson_MapStrAnyN_k_(mapOf_Arr_k_([to_rAnyN_AnyN_k_("type", "test_start"), to_rAnyN_AnyN_k_("suite", m.get_currentSuite()), to_rAnyN_AnyN_k_("test", name), to_rAnyN_AnyN_k_("timestamp", m.currentTimeMillis_k_())]))
    testFn.invoke()
    duration = m.get_testTimer().totalMilliseconds_k_()
    m.set_suitePassed(m.get_suitePassed() + 1)
    m.set_totalPassed(m.get_totalPassed() + 1)
    m.emitJson_MapStrAnyN_k_(mapOf_Arr_k_([to_rAnyN_AnyN_k_("type", "test_pass"), to_rAnyN_AnyN_k_("suite", m.get_currentSuite()), to_rAnyN_AnyN_k_("test", name), to_rAnyN_AnyN_k_("duration_ms", duration)]))
end sub

sub JsonTestAdapter_emitJson_MapStrAnyN_k_(data as Object)
    json = FormatJson(data)
    println_AnyN_k_(json)
end sub

function JsonTestAdapter_currentTimeMillis_k_() as LongInteger
    dt = RoDateTime_create_k_()
    dt.mark_k_()
    return (dt.asSeconds_k_() * 1000&) + dt.getMilliseconds_k_()
end function

function JsonTestAdapter_get_currentSuite_k_() as String
    return m.currentSuite
end function

sub JsonTestAdapter_set_currentSuite_Str_k_(value as String)
    m.currentSuite = value
end sub

function JsonTestAdapter_get_suiteTimer_k_() as Object
    return m.suiteTimer
end function

sub JsonTestAdapter_set_suiteTimer_RoTimespan_k_(value as Object)
    m.suiteTimer = value
end sub

function JsonTestAdapter_get_testTimer_k_() as Object
    return m.testTimer
end function

sub JsonTestAdapter_set_testTimer_RoTimespan_k_(value as Object)
    m.testTimer = value
end sub

function JsonTestAdapter_get_runTimer_k_() as Object
    return m.runTimer
end function

sub JsonTestAdapter_set_runTimer_RoTimespan_k_(value as Object)
    m.runTimer = value
end sub

function JsonTestAdapter_get_suitePassed_k_() as Integer
    return m.suitePassed
end function

sub JsonTestAdapter_set_suitePassed_I_k_(value as Integer)
    m.suitePassed = value
end sub

function JsonTestAdapter_get_suiteFailed_k_() as Integer
    return m.suiteFailed
end function

sub JsonTestAdapter_set_suiteFailed_I_k_(value as Integer)
    m.suiteFailed = value
end sub

function JsonTestAdapter_get_suiteIgnored_k_() as Integer
    return m.suiteIgnored
end function

sub JsonTestAdapter_set_suiteIgnored_I_k_(value as Integer)
    m.suiteIgnored = value
end sub

function JsonTestAdapter_get_totalSuites_k_() as Integer
    return m.totalSuites
end function

sub JsonTestAdapter_set_totalSuites_I_k_(value as Integer)
    m.totalSuites = value
end sub

function JsonTestAdapter_get_totalPassed_k_() as Integer
    return m.totalPassed
end function

sub JsonTestAdapter_set_totalPassed_I_k_(value as Integer)
    m.totalPassed = value
end sub

function JsonTestAdapter_get_totalFailed_k_() as Integer
    return m.totalFailed
end function

sub JsonTestAdapter_set_totalFailed_I_k_(value as Integer)
    m.totalFailed = value
end sub

function JsonTestAdapter_get_totalIgnored_k_() as Integer
    return m.totalIgnored
end function

sub JsonTestAdapter_set_totalIgnored_I_k_(value as Integer)
    m.totalIgnored = value
end sub

function JsonTestAdapter_get_runStarted_k_() as Boolean
    return m.runStarted
end function

sub JsonTestAdapter_set_runStarted_Z_k_(value as Boolean)
    m.runStarted = value
end sub
