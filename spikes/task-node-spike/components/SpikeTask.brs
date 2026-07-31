' SpikeTask — hand-written stand-in for a compiler-generated typed task.
' Mirrors the planned emission: init sets functionName to the wrapper;
' the wrapper try/catches the user run body and writes the completion
' protocol fields (error AA first, state last).

sub init()
    m.top.functionName = "__kotlinTaskMain"
    m.initSetValue = "set-in-init"
end sub

sub __kotlinTaskMain()
    try
        run_k_()
        m.top.kotlinTaskState = "done"
    catch e
        errInfo = { message: "", number: 0 }
        if type(e) = "roAssociativeArray"
            if e.message <> invalid then errInfo.message = e.message
            if e.number <> invalid then errInfo.number = e.number
            if e.backtrace <> invalid then errInfo.backtrace = e.backtrace
        end if
        m.top.kotlinTaskError = errInfo
        m.top.kotlinTaskState = "error"
    end try
end sub

sub run_k_()
    m.top.runCount = m.top.runCount + 1
    seen = "invalid"
    if m.initSetValue <> invalid then seen = m.initSetValue
    m.taskSetValue = "set-in-task"
    if m.top.crashPlease then throw "spike-intentional-crash"
    if m.top.delayMs > 0 then sleep(m.top.delayMs)
    if m.top.inputText = "emit200"
        arr = []
        for i = 1 to 200
            arr.push(i)
            m.top.emissions = arr
            m.top.emitSeq = i
        end for
    end if
    m.top.outputText = "echo:" + m.top.inputText + "|mInit:" + seen + "|id:" + m.top.kotlinTaskId.toStr()
end sub

' Runs on the node's owner thread via callFunc — reveals whether the task
' thread's write to plain m state (m.taskSetValue) propagated back.
function checkMScope(dummy = invalid as dynamic) as object
    result = { initSetValue: "invalid", taskSetValue: "invalid" }
    if m.initSetValue <> invalid then result.initSetValue = m.initSetValue
    if m.taskSetValue <> invalid then result.taskSetValue = m.taskSetValue
    return result
end function
