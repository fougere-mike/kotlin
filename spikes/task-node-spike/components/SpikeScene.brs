' SpikeScene.brs — orchestrates the task-node rendezvous checks sequentially.
' Every SpikeTask node is created UNPARENTED (never appended to the scene);
' only the watchdog Timer is parented. Results print as SPIKE| lines.

sub init()
    m.pass = 0
    m.fail = 0
    m.step = 0
    m.emitFires = 0

    m.watchdog = createObject("roSGNode", "Timer")
    m.watchdog.repeat = false
    m.watchdog.observeFieldScoped("fire", "FakeStdlib_onWatchdog")
    m.top.appendChild(m.watchdog)

    print "SPIKE|BEGIN"
    nextStep()
end sub

sub nextStep()
    m.watchdog.control = "stop"
    m.step = m.step + 1
    if m.step = 1
        startEchoCheck()
    else if m.step = 2
        startCrashCheck()
    else if m.step = 3
        startEmitCheck()
    else if m.step = 4
        startRerunCheck()
    else if m.step = 5
        startConcurrentCheck()
    else
        finish()
    end if
end sub

sub armWatchdog(seconds as float)
    m.watchdog.control = "stop"
    m.watchdog.duration = seconds
    m.watchdog.control = "start"
end sub

function makeTask(id as integer, inputText as string, delayMs as integer, crash as boolean) as object
    node = createObject("roSGNode", "SpikeTask")
    if node = invalid then return invalid
    node.observeFieldScoped("kotlinTaskState", "FakeStdlib_onTaskState")
    if inputText = "emit200" then node.observeFieldScoped("emitSeq", "FakeStdlib_onEmitSeq")
    node.kotlinTaskId = id
    node.inputText = inputText
    node.delayMs = delayMs
    node.crashPlease = crash
    return node
end function

sub record(name as string, ok as boolean, detail as string)
    status = "FAIL"
    if ok
        status = "PASS"
        m.pass = m.pass + 1
    else
        m.fail = m.fail + 1
    end if
    print "SPIKE|CHECK|" + name + "|" + status + "|" + detail
end sub

function bstr(b as boolean) as string
    if b then return "true"
    return "false"
end function

' ---- step 1: unparented create/observe/RUN round trip ----

sub startEchoCheck()
    armWatchdog(10)
    node = makeTask(1, "hello", 0, false)
    if node = invalid
        record("field_name_legality", false, "SpikeTask creation failed (XML rejected?)")
        record("unparented_run_roundtrip", false, "no node")
        nextStep()
        return
    end if
    record("field_name_legality", node.kotlinTaskState = "" and node.kotlinTaskId = 1, "kotlinTask* fields readable on fresh node")
    m.echoNode = node
    node.control = "RUN"
end sub

' ---- step 2: crash in run body -> error AA + state=error ----

sub startCrashCheck()
    armWatchdog(10)
    m.crashNode = makeTask(2, "x", 0, true)
    m.crashNode.control = "RUN"
end sub

' ---- step 3: 200 rapid alwaysNotify writes ----

sub startEmitCheck()
    armWatchdog(20)
    m.emitFires = 0
    m.emitNode = makeTask(3, "emit200", 0, false)
    m.emitNode.control = "RUN"
end sub

' ---- step 4: re-RUN the completed step-1 node (docs datum) ----

sub startRerunCheck()
    armWatchdog(3)
    m.echoNode.control = "RUN"
end sub

' ---- step 5: two concurrent instances of the same task type ----

sub startConcurrentCheck()
    armWatchdog(15)
    m.concDone = 0
    m.concOk = true
    m.concDetail = ""
    m.concA = makeTask(71, "A", 400, false)
    m.concB = makeTask(72, "B", 60, false)
    m.concA.control = "RUN"
    m.concB.control = "RUN"
end sub

sub finish()
    print "SPIKE|SUMMARY|pass=" + m.pass.toStr() + "|fail=" + m.fail.toStr()
    print "SPIKE|END"
end sub

' ---- observer dispatch (called from FakeStdlib.brs callbacks) ----

sub onEmitFire()
    m.emitFires = m.emitFires + 1
end sub

sub handleWatchdog()
    if m.step = 4
        ' No completion event after re-RUN is a legitimate outcome to document.
        record("rerun_same_node", true, "no completion event within 3s after re-RUN (task did not re-run)")
    else
        record("step_" + m.step.toStr() + "_watchdog", false, "step timed out")
    end if
    nextStep()
end sub

sub handleTaskState(node as object, state as string)
    if state <> "done" and state <> "error" then return

    if m.step = 1 and node.kotlinTaskId = 1
        record("included_script_callback", true, "observer callback resolved from FakeStdlib.brs")
        expected = "echo:hello|mInit:set-in-init|id:1"
        got = node.outputText
        record("unparented_run_roundtrip", state = "done" and got = expected, "state=" + state + " output=" + got)
        record("m_clone_in_visibility", instr(1, got, "mInit:set-in-init") > 0, "clone-in of init-set m values: " + got)
        checkMScopeWriteback(node)
        nextStep()

    else if m.step = 2 and node.kotlinTaskId = 2
        errMsg = ""
        errAA = node.kotlinTaskError
        if errAA <> invalid
            ' nested ifs: BrightScript AND does not short-circuit
            if errAA.message <> invalid then errMsg = errAA.message
        end if
        record("crash_to_error_state", state = "error" and instr(1, errMsg, "spike-intentional-crash") > 0, "state=" + state + " msg=" + errMsg)
        nextStep()

    else if m.step = 3 and node.kotlinTaskId = 3
        arr = node.emissions
        n = 0
        if arr <> invalid then n = arr.count()
        ordered = true
        if n = 200
            for i = 0 to 199
                if arr[i] <> i + 1
                    ordered = false
                    exit for
                end if
            end for
        else
            ordered = false
        end if
        record("rapid_alwaysnotify_datum", state = "done", "observer fires=" + m.emitFires.toStr() + "/200 (coalescing datum)")
        record("emission_array_drain", ordered, "final array n=" + n.toStr() + " ordered=" + bstr(ordered) + " => doorbell+drain viability")
        nextStep()

    else if m.step = 4 and node.kotlinTaskId = 1
        record("rerun_same_node", true, "re-RUN executed; runCount=" + node.runCount.toStr() + " state=" + state)
        nextStep()

    else if m.step = 5 and (node.kotlinTaskId = 71 or node.kotlinTaskId = 72)
        idStr = node.kotlinTaskId.toStr()
        expected = "echo:" + node.inputText + "|mInit:set-in-init|id:" + idStr
        if state <> "done" or node.outputText <> expected
            m.concOk = false
        end if
        m.concDetail = m.concDetail + "[" + idStr + ":" + state + " " + node.outputText + "]"
        m.concDone = m.concDone + 1
        if m.concDone = 2
            record("concurrent_instances", m.concOk, m.concDetail)
            nextStep()
        end if
    end if
end sub

sub checkMScopeWriteback(node as object)
    result = invalid
    try
        result = node.callFunc("checkMScope", invalid)
    catch e
        record("m_writeback_isolation", false, "callFunc on Task node threw: " + e.message)
        return
    end try
    if result = invalid
        record("m_writeback_isolation", false, "callFunc returned invalid")
    else
        detail = "owner-thread m sees taskSetValue=" + result.taskSetValue + ", initSetValue=" + result.initSetValue
        record("m_writeback_isolation", result.taskSetValue = "invalid", detail + " (invalid => no write-back; checker premise holds)")
    end if
end sub
