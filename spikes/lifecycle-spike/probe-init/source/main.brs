' Lifecycle-spike Probe init — component initialization order, parent/scene
' validity in init, XML attribute timing, callFunc/hasFunc, detach observers
' (design doc 2026-09-04-component-lifecycle-design.md, section 8).
' Everything runs in InitScene as a render-thread settle-Timer stage machine.
sub Main()
    dt = CreateObject("roDateTime")
    print "===SPIKE_SENTINEL_" + dt.AsSeconds().toStr() + "==="
    screen = CreateObject("roSGScreen")
    port = CreateObject("roMessagePort")
    screen.setMessagePort(port)
    screen.CreateScene("InitScene")
    screen.show()
    while true
        msg = wait(0, port)
        if type(msg) = "roSGScreenEvent"
            if msg.isScreenClosed() then return
        end if
    end while
end sub
