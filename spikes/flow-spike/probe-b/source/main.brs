' Flow-program Probe B (task control=STOP semantics; design doc section 8).
' Boots a scene that sequences the sub-runs; all logic lives in
' StopScene/StopTask.

sub Main()
    dt = CreateObject("roDateTime")
    print "===SPIKE_SENTINEL_" + dt.AsSeconds().toStr() + "==="
    screen = CreateObject("roSGScreen")
    port = CreateObject("roMessagePort")
    screen.setMessagePort(port)
    screen.CreateScene("StopScene")
    screen.show()
    while true
        msg = wait(0, port)
        if type(msg) = "roSGScreenEvent"
            if msg.isScreenClosed() then return
        end if
    end while
end sub
