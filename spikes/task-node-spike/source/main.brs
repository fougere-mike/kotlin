' Task-node rendezvous spike (plan Phase 0.4 / typed-task M0).
' Boots a scene that runs the checks; all logic lives in SpikeScene.

sub Main()
    dt = CreateObject("roDateTime")
    print "===SPIKE_SENTINEL_" + dt.AsSeconds().toStr() + "==="
    screen = CreateObject("roSGScreen")
    port = CreateObject("roMessagePort")
    screen.setMessagePort(port)
    screen.CreateScene("SpikeScene")
    screen.show()
    while true
        msg = wait(0, port)
        if type(msg) = "roSGScreenEvent"
            if msg.isScreenClosed() then return
        end if
    end while
end sub
