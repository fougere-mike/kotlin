sub check_Str_Z_Str_k_(name as String, pass as Boolean, detail as String)
    if pass then
        println_AnyN_k_((("SPIKE|PASS|" + name) + "|") + detail)
    else
        println_AnyN_k_((("SPIKE|FAIL|" + name) + "|") + detail)
    end if
end sub

sub main()
    dt = CreateObject("roDateTime")
    dt.mark()
    println_AnyN_k_(("===SPIKE_SENTINEL_" + __kotlin_numToStr_J_k_(dt.asSeconds())) + "===")
    println_AnyN_k_("SPIKE|START|port-observe-spike")
    screen = CreateObject("roSGScreen")
    port = CreateObject("roMessagePort")
    screen.setMessagePort(port)
    scene = screen.createScene("SpikeScene")
    screen.show()
    probe = scene.createChild("PortProbe")
    probe.setField("id", "probe1")
    okEcho = probe.observeField("echo", port)
    okTimer = probe.observeField("timerOut", port)
    okInput = probe.observeFieldScoped("input", port)
    println_AnyN_k_((((("SPIKE|observe_returns|echo=" + ((function(okEcho)
        if okEcho then return "true" else return "false"
    end function)(okEcho))) + "|timerOut=") + ((function(okTimer)
        if okTimer then return "true" else return "false"
    end function)(okTimer))) + "|inputScoped=") + ((function(okInput)
        if okInput then return "true" else return "false"
    end function)(okInput)))
    probe.setField("input", "ping1")
    gotEcho1 = false
    gotEcho2 = false
    gotTimer = false
    gotInput1 = false
    gotInput2 = false
    gotTask = false
    sentPing2 = false
    taskStarted = false
    accessorsChecked = false
    accessorNodeInfo = "none"
    accessorSubtype = "none"
    accessorData = "none"
    taskObserveOk = false
    taskNode = invalid
    eventCount = 0
    clock = CreateObject("roTimespan")
    clock.mark()
    while clock.totalMilliseconds() < 20000
        __break0 = false
        while true
            msg = port.waitMessage(100)
            if msg = invalid then
                exit while
            end if
            msgType = Type(msg)
            if msgType <> "roSGNodeEvent" then
                println_AnyN_k_("SPIKE|OTHER_EVENT|" + msgType)
                exit while
            end if
            ev = msg
            field = ev.getField()
            dataStr = toString_AnyN_k_(ev.getData())
            eventCount = (eventCount + 1)
            println_AnyN_k_(((((((("SPIKE|EVENT|#" + __kotlin_numToStr_I_k_(eventCount)) + "|field=") + field) + "|data=") + dataStr) + "|at=") + __kotlin_numToStr_I_k_(clock.totalMilliseconds())) + "ms")
            if not accessorsChecked and (field = "echo") then
                accessorsChecked = true
                n = ev.getNode()
                rn = ev.getRoSGNode()
                accessorNodeInfo = ((("typeOf=" + Type(n)) + " value=") + toString_AnyN_k_(n))
                accessorSubtype = rn.subtype()
                accessorData = dataStr
                println_AnyN_k_((((((("SPIKE|ACCESSORS|getField=" + field) + "|getData=") + dataStr) + "|getNode:") + accessorNodeInfo) + "|getRoSGNode.subtype=") + accessorSubtype)
            end if
            if field = "input" then
                if dataStr = "ping1" then
                    gotInput1 = true
                end if
                if dataStr = "ping2" then
                    gotInput2 = true
                end if
            else if field = "echo" then
                if dataStr = "echo:ping1" then
                    gotEcho1 = true
                end if
                if dataStr = "echo:ping2" then
                    gotEcho2 = true
                end if
                if gotEcho1 and not sentPing2 then
                    sentPing2 = true
                    probe.setField("input", "ping2")
                end if
            else if field = "timerOut" then
                gotTimer = true
            else if field = "taskOut" then
                if dataStr = "task-wrote:hello-task" then
                    gotTask = true
                end if
            end if
            if ((gotEcho1 and gotEcho2) and gotTimer) and not taskStarted then
                taskStarted = true
                t = CreateObject("roSGNode", "SpikeTask")
                taskNode = t
                taskObserveOk = t.observeField("taskOut", port)
                println_AnyN_k_("SPIKE|task_observe_return|" + ((function(taskObserveOk)
                    if taskObserveOk then return "true" else return "false"
                end function)(taskObserveOk)))
                t.setField("taskIn", "hello-task")
                t.setField("control", "RUN")
            end if
            if ((((gotEcho1 and gotEcho2) and gotTimer) and gotInput1) and gotInput2) and gotTask then
                __break0 = true
                exit while
            end if
            exit while
        end while
        if __break0 then
            exit while
        end if
    end while
    println_AnyN_k_(((("SPIKE|LOOP_DONE|events=" + __kotlin_numToStr_I_k_(eventCount)) + "|elapsed=") + __kotlin_numToStr_I_k_(clock.totalMilliseconds())) + "ms")
    finalEcho = toString_AnyN_k_(probe.getField("echo"))
    finalTimer = toString_AnyN_k_(probe.getField("timerOut"))
    finalTask = "no-task-node"
    t2 = taskNode
    if t2 <> invalid then
        finalTask = toString_AnyN_k_(t2.getField("taskOut"))
    end if
    println_AnyN_k_((((("SPIKE|FINAL_FIELDS|echo=" + finalEcho) + "|timerOut=") + finalTimer) + "|taskOut=") + finalTask)
    check_Str_Z_Str_k_("port_observe_accepted", okEcho and okTimer, (("observeField(field, port) returned echo=" + ((function(okEcho)
        if okEcho then return "true" else return "false"
    end function)(okEcho))) + " timerOut=") + ((function(okTimer)
        if okTimer then return "true" else return "false"
    end function)(okTimer)))
    check_Str_Z_Str_k_("scoped_port_observe_accepted", okInput, "observeFieldScoped(field, port) from main returned " + ((function(okInput)
        if okInput then return "true" else return "false"
    end function)(okInput)))
    check_Str_Z_Str_k_("render_onchange_to_main_port", gotEcho1 and gotEcho2, ((("echo events for ping1=" + ((function(gotEcho1)
        if gotEcho1 then return "true" else return "false"
    end function)(gotEcho1))) + " ping2=") + ((function(gotEcho2)
        if gotEcho2 then return "true" else return "false"
    end function)(gotEcho2))) + " (two full set->onChange->event cycles)")
    check_Str_Z_Str_k_("main_set_self_event", gotInput1 and gotInput2, ((("input events seen ping1=" + ((function(gotInput1)
        if gotInput1 then return "true" else return "false"
    end function)(gotInput1))) + " ping2=") + ((function(gotInput2)
        if gotInput2 then return "true" else return "false"
    end function)(gotInput2))) + " (scoped observer)")
    check_Str_Z_Str_k_("spontaneous_timer_write", gotTimer, "timerOut event from render-thread Timer handler")
    check_Str_Z_Str_k_("event_accessors", (accessorsChecked and (accessorData = "echo:ping1")) and (accessorSubtype = "PortProbe"), ((((("data=" + accessorData) + " subtype=") + accessorSubtype) + " getNode(") + accessorNodeInfo) + ")")
    check_Str_Z_Str_k_("task_thread_write_to_main_port", gotTask and taskObserveOk, (("taskOut event received=" + ((function(gotTask)
        if gotTask then return "true" else return "false"
    end function)(gotTask))) + " observeReturn=") + ((function(taskObserveOk)
        if taskObserveOk then return "true" else return "false"
    end function)(taskObserveOk)))
    check_Str_Z_Str_k_("final_field_reads", (finalEcho = "echo:ping2") and (finalTimer = "timer-fired"), (((("echo=" + finalEcho) + " timerOut=") + finalTimer) + " taskOut=") + finalTask)
    println_AnyN_k_("SPIKE|END")
    lingering = CreateObject("roTimespan")
    lingering.mark()
    while lingering.totalMilliseconds() < 3000
        msg = port.waitMessage(100)
        if (msg <> invalid) and (Type(msg) = "roSGScreenEvent") then
            ev = msg
            if ev.isScreenClosed() then
                return
            end if
        end if
    end while
    screen.close()
end sub
