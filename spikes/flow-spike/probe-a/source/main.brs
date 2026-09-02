' Flow-spike Probe A — global-node doorbell (StateFlow carrier facts).
' Boots BellScene, which runs A1-A4 as a render-thread step machine.
' A5 (INFORMATIVE) is driven from HERE: after the scene finishes A1-A4 it sets
' __kotlinFlowSpikeReadyForMain=1 on the global node; main then writes the bell
' ONCE from the MAIN thread. Main MAY sleep/poll — only the render thread may not.

sub Main()
    dt = CreateObject("roDateTime")
    print "===SPIKE_SENTINEL_" + dt.AsSeconds().toStr() + "==="
    screen = CreateObject("roSGScreen")
    port = CreateObject("roMessagePort")
    screen.setMessagePort(port)
    screen.CreateScene("BellScene")
    screen.show()

    ' --- A5 sequencing: poll readyForMain on the MAIN thread (bounded 20s) ---
    ' hasField guard: the scene addFields these during scene init, which may not
    ' have run yet on the render thread when this loop first spins.
    gn = screen.getGlobalNode()
    clock = CreateObject("roTimespan")
    wrote = false
    while clock.TotalMilliseconds() < 20000
        ready = false
        if gn.hasField("__kotlinFlowSpikeReadyForMain") then
            if gn.__kotlinFlowSpikeReadyForMain = 1 then ready = true
        end if
        if ready then
            ' The one main-thread emit. The bell field is guaranteed to exist:
            ' the scene addField'd it BEFORE readyForMain (arming-order law).
            gn.__kotlinFlowSpikeBell = 13
            wrote = true
            print "[SPIKE] a.a5.info main-thread-wrote value=13 waited=" + clock.TotalMilliseconds().toStr() + "ms"
            exit while
        end if
        sleep(100)
    end while
    if not wrote then
        print "[SPIKE] a.a5.info main-poll-timeout scene-never-signaled-readyForMain-within-20s"
    end if

    while true
        msg = wait(0, port)
        if type(msg) = "roSGScreenEvent"
            if msg.isScreenClosed() then return
        end if
    end while
end sub
