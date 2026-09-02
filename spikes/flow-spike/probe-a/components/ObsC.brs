' ObsC — scoped observer of the global doorbell field. Identical to ObsB/ObsD
' except m.who; the scene never retires this instance (retired stays false;
' ghost branch is dead code here — kept deliberately for file symmetry).
' Records every observed value into its OWN node's fields (fires/received/
' whoSeen) so the scene can compute verdicts from sampled state, not prints.

sub init()
    m.who = "ObsC"
    m.clock = CreateObject("roTimespan")
    m.armed = false
    ' Belt-and-braces arming: m.global is expected valid in init for nodes
    ' created on the render thread, but the scene also rings armNow right after
    ' appendChild in case it isn't. Either path completes a full settle step
    ' (0.6s) before the first bell write — the arming-order law holds.
    m.top.observeField("armNow", "onArmNow")
    arm("init")
end sub

sub onArmNow()
    arm("armNow")
end sub

sub arm(where as string)
    if m.armed then return
    if m.global = invalid then
        print "[SPIKE] a.evidence.armSkip who=" + m.who + " global-invalid-at-" + where
        return
    end if
    m.armed = true
    m.global.observeFieldScoped("__kotlinFlowSpikeBell", "onBell")
    print "[SPIKE] a.evidence.armed who=" + m.who + " via=" + where + " t=" + m.clock.TotalMilliseconds().toStr() + "ms"
end sub

sub onBell(event as object)
    v = event.getData()
    ' A4 ghost detector FIRST — before any other m.top access can crash a
    ' half-dead removed node and lose the record. The whole block, INCLUDING
    ' the m.top.retired read, is try/catch'd. The scene sets retired=true
    ' before the positive-control write and leaves it set through removal;
    ' every fire seen with retired=true APPENDS its value to the global ghost
    ' record (value-tagged csv, not a sticky boolean — the scene's verdicts
    ' test per-window membership, so a same-stack ghost can never contaminate
    ' the settled-window verdict).
    try
        if m.top.retired then
            g = m.global.__kotlinFlowSpikeGhost
            if g = "" then
                m.global.__kotlinFlowSpikeGhost = v.toStr()
            else
                m.global.__kotlinFlowSpikeGhost = g + "," + v.toStr()
            end if
            print "[SPIKE] a.evidence.ghostFire who=" + m.who + " value=" + v.toStr()
        end if
    catch e
        print "[SPIKE] a.evidence.ghostFire who=" + m.who + " value=" + v.toStr() + " detector-threw: " + e.message
    end try
    ' All bookkeeping lands on THIS component's node: if the handler ran in some
    ' other context, m.top would be a different node and none of this would be
    ' visible when the scene samples this observer.
    m.top.fires = m.top.fires + 1
    if m.top.received = "" then
        m.top.received = v.toStr()
    else
        m.top.received = m.top.received + "," + v.toStr()
    end if
    m.top.whoSeen = m.who   ' identity marker stored in THIS component's m at init
    parentState = "attached"
    if m.top.getParent() = invalid then parentState = "detached"
    print "[SPIKE] a.evidence.fire who=" + m.who + " value=" + v.toStr() + " fires=" + m.top.fires.toStr() + " parent=" + parentState + " t=" + m.clock.TotalMilliseconds().toStr() + "ms"
end sub
