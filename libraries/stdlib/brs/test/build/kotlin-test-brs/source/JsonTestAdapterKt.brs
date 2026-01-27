function JsonTestAdapter_create_k_() as Object
    this = {}
    this.__type = "JsonTestAdapter"
    this.__proto = ["JsonTestAdapter", "FrameworkAdapter"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.startRun_k_ = JsonTestAdapter_startRun_k_
    this.endRun_k_ = JsonTestAdapter_endRun_k_
    this.suite_Str_Z_Function0V_k_ = JsonTestAdapter_suite_Str_Z_Function0V_k_
    this.test_Str_Z_Function0V_k_ = JsonTestAdapter_test_Str_Z_Function0V_k_
    this.emitJson_MapStrAnyN_k_ = JsonTestAdapter_emitJson_MapStrAnyN_k_
    this.currentTimeMillis_k_ = JsonTestAdapter_currentTimeMillis_k_
    this.__get_currentSuite = JsonTestAdapter___get_currentSuite_k_
    this.__set_currentSuite = JsonTestAdapter___set_currentSuite_Str_k_
    this.__get_suiteTimer = JsonTestAdapter___get_suiteTimer_k_
    this.__set_suiteTimer = JsonTestAdapter___set_suiteTimer_RoTimespan_k_
    this.__get_testTimer = JsonTestAdapter___get_testTimer_k_
    this.__set_testTimer = JsonTestAdapter___set_testTimer_RoTimespan_k_
    this.__get_runTimer = JsonTestAdapter___get_runTimer_k_
    this.__set_runTimer = JsonTestAdapter___set_runTimer_RoTimespan_k_
    this.__get_suitePassed = JsonTestAdapter___get_suitePassed_k_
    this.__set_suitePassed = JsonTestAdapter___set_suitePassed_I_k_
    this.__get_suiteFailed = JsonTestAdapter___get_suiteFailed_k_
    this.__set_suiteFailed = JsonTestAdapter___set_suiteFailed_I_k_
    this.__get_suiteIgnored = JsonTestAdapter___get_suiteIgnored_k_
    this.__set_suiteIgnored = JsonTestAdapter___set_suiteIgnored_I_k_
    this.__get_totalSuites = JsonTestAdapter___get_totalSuites_k_
    this.__set_totalSuites = JsonTestAdapter___set_totalSuites_I_k_
    this.__get_totalPassed = JsonTestAdapter___get_totalPassed_k_
    this.__set_totalPassed = JsonTestAdapter___set_totalPassed_I_k_
    this.__get_totalFailed = JsonTestAdapter___get_totalFailed_k_
    this.__set_totalFailed = JsonTestAdapter___set_totalFailed_I_k_
    this.__get_totalIgnored = JsonTestAdapter___get_totalIgnored_k_
    this.__set_totalIgnored = JsonTestAdapter___set_totalIgnored_I_k_
    this.__get_runStarted = JsonTestAdapter___get_runStarted_k_
    this.__set_runStarted = JsonTestAdapter___set_runStarted_Z_k_
    this.currentSuite = ""
    this.suiteTimer = CreateObject("roTimespan")
    this.testTimer = CreateObject("roTimespan")
    this.runTimer = CreateObject("roTimespan")
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
    m.__set_runStarted(true)
    m.__get_runTimer().mark()
    runId = m.currentTimeMillis_k_()
    sentinel = ("===KOTLINTEST_SENTINEL_" + __kotlin_numToStr_J_k_(runId)) + "==="
    println_AnyN_k_(sentinel)
    progression = until_rI_I_k_(0, 100)
    inductionVariable = progression.__get_first()
    last = progression.__get_last()
    if inductionVariable <= last then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        println_AnyN_k_(((("[KOTLINTEST_BUFFER_FLUSH:" + __kotlin_numToStr_J_k_(runId)) + ":") + __kotlin_numToStr_I_k_(i)) + "]")


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            println_AnyN_k_(((("[KOTLINTEST_BUFFER_FLUSH:" + __kotlin_numToStr_J_k_(runId)) + ":") + __kotlin_numToStr_I_k_(i)) + "]")

        end while

    end if

    println_AnyN_k_(sentinel)
    println_AnyN_k_(("[KOTLINTEST_RUN_ID:" + __kotlin_numToStr_J_k_(runId)) + "]")
    println_AnyN_k_("[KOTLINTEST_START]")
    m.emitJson_MapStrAnyN_k_(mapOf_Arr_k_([to_rAnyN_AnyN_k_("type", "run_start"), to_rAnyN_AnyN_k_("timestamp", m.currentTimeMillis_k_())]))
end sub

sub JsonTestAdapter_endRun_k_()
    duration = m.__get_runTimer().totalMilliseconds()
    m.emitJson_MapStrAnyN_k_(mapOf_Arr_k_([to_rAnyN_AnyN_k_("type", "run_complete"), to_rAnyN_AnyN_k_("total_suites", m.__get_totalSuites()), to_rAnyN_AnyN_k_("total_tests", (m.__get_totalPassed() + m.__get_totalFailed()) + m.__get_totalIgnored()), to_rAnyN_AnyN_k_("passed", m.__get_totalPassed()), to_rAnyN_AnyN_k_("failed", m.__get_totalFailed()), to_rAnyN_AnyN_k_("ignored", m.__get_totalIgnored()), to_rAnyN_AnyN_k_("duration_ms", duration)]))
    println_AnyN_k_("[KOTLINTEST_END]")
end sub

sub JsonTestAdapter_suite_Str_Z_Function0V_k_(name as String, ignored as Boolean, suiteFn as Object)
    if not m.__get_runStarted() then
        m.startRun_k_()
    end if
    if ignored then
        m.emitJson_MapStrAnyN_k_(mapOf_Arr_k_([to_rAnyN_AnyN_k_("type", "suite_ignored"), to_rAnyN_AnyN_k_("suite", name), to_rAnyN_AnyN_k_("reason", "Suite marked as ignored")]))
        return
    end if
    m.__set_currentSuite(name)
    m.__get_suiteTimer().mark()
    m.__set_suitePassed(0)
    m.__set_suiteFailed(0)
    m.__set_suiteIgnored(0)
    m.__set_totalSuites(m.__get_totalSuites() + 1)
    m.emitJson_MapStrAnyN_k_(mapOf_Arr_k_([to_rAnyN_AnyN_k_("type", "suite_start"), to_rAnyN_AnyN_k_("suite", name), to_rAnyN_AnyN_k_("timestamp", m.currentTimeMillis_k_())]))
    suiteFn.invoke_k_()
    duration = m.__get_suiteTimer().totalMilliseconds()
    m.emitJson_MapStrAnyN_k_(mapOf_Arr_k_([to_rAnyN_AnyN_k_("type", "suite_end"), to_rAnyN_AnyN_k_("suite", name), to_rAnyN_AnyN_k_("passed", m.__get_suitePassed()), to_rAnyN_AnyN_k_("failed", m.__get_suiteFailed()), to_rAnyN_AnyN_k_("ignored", m.__get_suiteIgnored()), to_rAnyN_AnyN_k_("duration_ms", duration)]))
end sub

sub JsonTestAdapter_test_Str_Z_Function0V_k_(name as String, ignored as Boolean, testFn as Object)
    if ignored then
        m.__set_suiteIgnored(m.__get_suiteIgnored() + 1)
        m.__set_totalIgnored(m.__get_totalIgnored() + 1)
        m.emitJson_MapStrAnyN_k_(mapOf_Arr_k_([to_rAnyN_AnyN_k_("type", "test_ignored"), to_rAnyN_AnyN_k_("suite", m.__get_currentSuite()), to_rAnyN_AnyN_k_("test", name), to_rAnyN_AnyN_k_("reason", "Test marked as ignored")]))
        return
    end if
    m.__get_testTimer().mark()
    m.emitJson_MapStrAnyN_k_(mapOf_Arr_k_([to_rAnyN_AnyN_k_("type", "test_start"), to_rAnyN_AnyN_k_("suite", m.__get_currentSuite()), to_rAnyN_AnyN_k_("test", name), to_rAnyN_AnyN_k_("timestamp", m.currentTimeMillis_k_())]))
    testFn.invoke_k_()
    duration = m.__get_testTimer().totalMilliseconds()
    m.__set_suitePassed(m.__get_suitePassed() + 1)
    m.__set_totalPassed(m.__get_totalPassed() + 1)
    m.emitJson_MapStrAnyN_k_(mapOf_Arr_k_([to_rAnyN_AnyN_k_("type", "test_pass"), to_rAnyN_AnyN_k_("suite", m.__get_currentSuite()), to_rAnyN_AnyN_k_("test", name), to_rAnyN_AnyN_k_("duration_ms", duration)]))
end sub

sub JsonTestAdapter_emitJson_MapStrAnyN_k_(data as Object)
    plainAA = __kotlin_mapToPlainAA_ANY_k_(data)
    json = FormatJson(__kotlin_toJsonValue_AnyN_k_(plainAA))
    println_AnyN_k_(json)
end sub

function JsonTestAdapter_currentTimeMillis_k_() as LongInteger
    dt = CreateObject("roDateTime")
    dt.mark()
    return (dt.asSeconds() * 1000&) + dt.getMilliseconds()
end function

function JsonTestAdapter___get_currentSuite_k_() as String
    return m.currentSuite
end function

sub JsonTestAdapter___set_currentSuite_Str_k_(value as String)
    m.currentSuite = value
end sub

function JsonTestAdapter___get_suiteTimer_k_() as Object
    return m.suiteTimer
end function

sub JsonTestAdapter___set_suiteTimer_RoTimespan_k_(value as Object)
    m.suiteTimer = value
end sub

function JsonTestAdapter___get_testTimer_k_() as Object
    return m.testTimer
end function

sub JsonTestAdapter___set_testTimer_RoTimespan_k_(value as Object)
    m.testTimer = value
end sub

function JsonTestAdapter___get_runTimer_k_() as Object
    return m.runTimer
end function

sub JsonTestAdapter___set_runTimer_RoTimespan_k_(value as Object)
    m.runTimer = value
end sub

function JsonTestAdapter___get_suitePassed_k_() as Integer
    return m.suitePassed
end function

sub JsonTestAdapter___set_suitePassed_I_k_(value as Integer)
    m.suitePassed = value
end sub

function JsonTestAdapter___get_suiteFailed_k_() as Integer
    return m.suiteFailed
end function

sub JsonTestAdapter___set_suiteFailed_I_k_(value as Integer)
    m.suiteFailed = value
end sub

function JsonTestAdapter___get_suiteIgnored_k_() as Integer
    return m.suiteIgnored
end function

sub JsonTestAdapter___set_suiteIgnored_I_k_(value as Integer)
    m.suiteIgnored = value
end sub

function JsonTestAdapter___get_totalSuites_k_() as Integer
    return m.totalSuites
end function

sub JsonTestAdapter___set_totalSuites_I_k_(value as Integer)
    m.totalSuites = value
end sub

function JsonTestAdapter___get_totalPassed_k_() as Integer
    return m.totalPassed
end function

sub JsonTestAdapter___set_totalPassed_I_k_(value as Integer)
    m.totalPassed = value
end sub

function JsonTestAdapter___get_totalFailed_k_() as Integer
    return m.totalFailed
end function

sub JsonTestAdapter___set_totalFailed_I_k_(value as Integer)
    m.totalFailed = value
end sub

function JsonTestAdapter___get_totalIgnored_k_() as Integer
    return m.totalIgnored
end function

sub JsonTestAdapter___set_totalIgnored_I_k_(value as Integer)
    m.totalIgnored = value
end sub

function JsonTestAdapter___get_runStarted_k_() as Boolean
    return m.runStarted
end function

sub JsonTestAdapter___set_runStarted_Z_k_(value as Boolean)
    m.runStarted = value
end sub
