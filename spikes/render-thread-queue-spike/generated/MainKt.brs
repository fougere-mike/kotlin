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
    println_AnyN_k_("SPIKE|START|render-thread-queue-spike")
    screen = CreateObject("roSGScreen")
    port = CreateObject("roMessagePort")
    screen.setMessagePort(port)
    scene = screen.createScene("RtqSpikeScene")
    screen.show()
    probeA = scene.createChild("RtqProbe")
    probeA.setField("id", "probeA")
    probeB = scene.createChild("RtqProbe")
    probeB.setField("id", "probeB")
    probeA.observeField("lastEvent", port)
    probeB.observeField("lastEvent", port)
    clock = CreateObject("roTimespan")
    clock.mark()
    reportsRead = false
    sentSelf = false
    sentCross = false
    sentEarly = false
    sentLateReg = false
    startedTask = false
    sentReadGlobals = false
    timersStarted = false
    mainRtqDone = false
    gotSelf = false
    gotCross = false
    gotTask = false
    gotEarly = false
    eventCount = 0
    timer1Fired = false
    timer2Fired = false
    taskNode = invalid
    timer1 = invalid
    orphanGroup = invalid
    while clock.totalMilliseconds() < 14000
        while true
            ms = clock.totalMilliseconds()
            if not reportsRead and (ms > 700) then
                reportsRead = true
                println_AnyN_k_("SPIKE|INFO|initReportA|" + toString_AnyN_k_(probeA.getField("initReport")))
                println_AnyN_k_("SPIKE|INFO|initReportB|" + toString_AnyN_k_(probeB.getField("initReport")))
            end if
            if not sentSelf and (ms > 1000) then
                sentSelf = true
                probeA.setField("command", "selfpost")
            end if
            if not sentCross and (ms > 2200) then
                sentCross = true
                probeB.setField("command", "crosspost")
            end if
            if not sentEarly and (ms > 3400) then
                sentEarly = true
                probeB.setField("command", "earlypost")
            end if
            if not sentLateReg and (ms > 4000) then
                sentLateReg = true
                probeA.setField("command", "registerlate")
            end if
            if not startedTask and (ms > 5200) then
                startedTask = true
                t = CreateObject("roSGNode", "RtqSpikeTask")
                taskNode = t
                t.setField("control", "RUN")
            end if
            if not sentReadGlobals and (ms > 6600) then
                sentReadGlobals = true
                probeA.setField("command", "readglobals")
                probeB.setField("command", "readglobals")
            end if
            if not timersStarted and (ms > 7200) then
                timersStarted = true
                t1 = CreateObject("roSGNode", "Timer")
                t1.setField("id", "timer1")
                t1.observeField("fire", port)
                t1.setField("duration", 0.4#)
                t1.setField("control", "start")
                timer1 = t1
                g = CreateObject("roSGNode", "Group")
                orphanGroup = g
                t2 = g.createChild("Timer")
                t2.setField("id", "timer2")
                t2.observeField("fire", port)
                t2.setField("duration", 0.4#)
                t2.setField("control", "start")
            end if
            if not mainRtqDone and (ms > 9500) then
                mainRtqDone = true
                println_AnyN_k_("SPIKE|INFO|main_rtq_create|type=" + Type(CreateObject("roRenderThreadQueue")))
                                try
                    q = CreateObject("roRenderThreadQueue")
                    if q <> invalid then
                        tok = q.AddMessageHandler("spike.main", "RtqMainNeverCalled")
                        println_AnyN_k_("SPIKE|INFO|main_rtq_addhandler|token=" + Type(tok))
                    else
                        println_AnyN_k_("SPIKE|INFO|main_rtq_addhandler|skipped-no-rtq-on-main")
                    end if
                catch e
                    println_AnyN_k_("SPIKE|INFO|main_rtq_addhandler|threw=" + toString_AnyN_k_(e.__get_message()))
                end try
            end if
            msg = port.waitMessage(50)
            if msg = invalid then
                exit while
            end if
            if Type(msg) <> "roSGNodeEvent" then
                exit while
            end if
            ev = msg
            field = ev.getField()
            dataStr = toString_AnyN_k_(ev.getData())
            nodeId = ev.getNode()
            if field = "lastEvent" then
                eventCount = (eventCount + 1)
                println_AnyN_k_(((((((("SPIKE|EVENT|lastEvent|#" + __kotlin_numToStr_I_k_(eventCount)) + "|from=") + nodeId) + "|src=") + dataStr) + "|at=") + __kotlin_numToStr_I_k_(clock.totalMilliseconds())) + "ms")
                if dataStr = "A-self" then
                    gotSelf = true
                end if
                if dataStr = "B-cross" then
                    gotCross = true
                end if
                if dataStr = "task" then
                    gotTask = true
                end if
                if dataStr = "B-early" then
                    gotEarly = true
                end if
            else if field = "fire" then
                println_AnyN_k_(((("SPIKE|EVENT|timer_fire|from=" + nodeId) + "|at=") + __kotlin_numToStr_I_k_(clock.totalMilliseconds())) + "ms")
                if nodeId = "timer1" then
                    timer1Fired = true
                end if
                if nodeId = "timer2" then
                    timer2Fired = true
                end if
            end if
            exit while
        end while
    end while
    println_AnyN_k_(((("SPIKE|LOOP_DONE|events=" + __kotlin_numToStr_I_k_(eventCount)) + "|elapsed=") + __kotlin_numToStr_I_k_(clock.totalMilliseconds())) + "ms")
    check_Str_Z_Str_k_("rtq_selfpost_delivered", gotSelf, "render-thread PostMessage -> handler -> lastEvent mirror received=" + ((function(gotSelf)
        if gotSelf then return "true" else return "false"
    end function)(gotSelf)))
    println_AnyN_k_("SPIKE|INFO|crosspost_delivered|" + ((function(gotCross)
        if gotCross then return "true" else return "false"
    end function)(gotCross)))
    println_AnyN_k_("SPIKE|INFO|task_post_delivered|" + ((function(gotTask)
        if gotTask then return "true" else return "false"
    end function)(gotTask)))
    println_AnyN_k_("SPIKE|INFO|early_post_delivered_after_late_registration|" + ((function(gotEarly)
        if gotEarly then return "true" else return "false"
    end function)(gotEarly)))
    println_AnyN_k_((("SPIKE|INFO|unparented_timer_fired|timer1=" + ((function(timer1Fired)
        if timer1Fired then return "true" else return "false"
    end function)(timer1Fired))) + "|timer2=") + ((function(timer2Fired)
        if timer2Fired then return "true" else return "false"
    end function)(timer2Fired)))
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
