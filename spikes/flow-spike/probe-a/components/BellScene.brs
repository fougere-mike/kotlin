' BellScene — the WRITER role ("component A") for Probe A: global-node doorbell.
' Pins the StateFlow doorbell carrier facts (flow design doc section 8, Probe A):
'   A1 order       — 3 back-to-back synchronous writes to a runtime-addField'd
'                    global int field (alwaysNotify=true) each fire scoped
'                    observers in B and C, per-write, in write order, in the
'                    OBSERVING component's context.
'   A2 alwaysNotify — the SAME value written twice fires both observers twice.
'   A3 stacking    — two scoped observers on ONE global field both fire (B=C counts).
'   A4 auto-detach — POSITIVE CONTROL first (retired=true while B is still
'                    attached and ALIVE, one write, assert the ghost detector
'                    recorded it — prove the path live before relying on its
'                    silence), then remove ObsB from the tree + release every
'                    scene reference and keep writing in TWO windows:
'                      same-stack (8,9)  — INFORMATIVE: shares the removal's
'                        synchronous call stack; teardown MAY legally be
'                        deferred past it, so a ghost here is a teardown-timing
'                        fact, not the design's question;
'                      settled (10,11)   — PRIMARY verdict: written a full
'                        settle step after removal+deref — the design's actual
'                        question (component death, then LATER emits).
'                    Ghost fires are recorded VALUE-TAGGED (csv on a global
'                    string field), so the windows can never contaminate each
'                    other's verdicts. Then a FRESH ObsD arms late and receives
'                    subsequent writes.
'   A5 INFORMATIVE — main.brs writes the bell once from the MAIN thread after
'                    the scene signals readyForMain; observers' counts advance.
'
' Bell value map (every window uses DISTINCT values — ghost-record membership
' pins exactly which window a ghost fire came from):
'   1,2,3 = A1     7,7 = A2      6 = A4 detector control
'   8,9   = A4 same-stack (INFORMATIVE)   10,11 = A4 settled (PRIMARY)
'   12    = A4 late re-observe (D)        13    = A5 main-thread
'
' Platform laws honored here:
' - render thread NEVER sleeps: all sequencing is a one-shot settle Timer
'   driving a stage counter (precedent: scope-handle channel-matrix ProbeB).
' - arming order: global fields are addField'd BEFORE the observer components
'   are created; the ghost record exists BEFORE any ghost fire is possible;
'   ObsD is created a full settle step BEFORE the write it must observe.
' - a FAIL is a FINDING, never suppressed.

sub init()
    m.who = "Scene"   ' context discriminator: if an observer handler ever ran in
                      ' the scene's context, whoSeen would read "Scene" (see ObsB.brs)
    m.clock = CreateObject("roTimespan")
    di = CreateObject("roDeviceInfo")
    osv = di.GetOSVersion()
    print "[SPIKE] BEGIN probe-a model=" + di.GetModel() + " os=" + osv.major + "." + osv.minor + "." + osv.revision + " build=" + osv.build

    ' Global fields FIRST (arming-order law: the field must exist before any
    ' observeFieldScoped arms on it; the ghost record is pre-added per spec A4).
    ' The ghost record is a STRING csv of ghost-fired values — value-tagged,
    ' NOT a sticky boolean — so each verdict tests membership of ITS window's
    ' values only and an immediate-window ghost cannot contaminate the settled
    ' verdict (nor can a straggler legit delivery landing after retired=true).
    m.global.addField("__kotlinFlowSpikeBell", "integer", true)          ' alwaysNotify=true (A2)
    m.global.addField("__kotlinFlowSpikeGhost", "string", false)
    m.global.addField("__kotlinFlowSpikeReadyForMain", "integer", false)
    m.global.__kotlinFlowSpikeGhost = ""
    m.global.__kotlinFlowSpikeReadyForMain = 0

    ' Observers B and C. Their inits arm observeFieldScoped synchronously inside
    ' createObject; the scene additionally rings armNow as a belt-and-braces
    ' fallback (see ObsB.brs init) — either way, arming completes a full settle
    ' step (0.6s) before the first bell write.
    m.obsB = createObject("roSGNode", "ObsB")
    m.obsB.id = "obsB"
    m.top.appendChild(m.obsB)
    m.obsB.armNow = true
    m.obsC = createObject("roSGNode", "ObsC")
    m.obsC.id = "obsC"
    m.top.appendChild(m.obsC)
    m.obsC.armNow = true
    m.obsD = invalid

    m.preB = 0
    m.preC = 0
    m.preD = 0
    m.stage = 0
    m.settle = createObject("roSGNode", "Timer")
    m.settle.repeat = false
    m.settle.duration = 0.6
    m.settle.observeField("fire", "onSettle")
    m.top.appendChild(m.settle)
    settle(0.6)
end sub

sub settle(secs as float)
    m.settle.duration = secs
    m.settle.control = "stop"
    m.settle.control = "start"
end sub

function pf(ok as boolean) as string
    if ok then return "PASS"
    return "FAIL"
end function

function boolStr(b as boolean) as string
    if b then return "true"
    return "false"
end function

' Membership test on the value-tagged global ghost record ("8,9" style csv).
' Comma-delimited on both sides so "1" never matches inside "10"-"13".
function ghostHas(v as integer) as boolean
    return Instr(1, "," + m.global.__kotlinFlowSpikeGhost + ",", "," + v.toStr() + ",") > 0
end function

function ghostCsvOrNone() as string
    g = m.global.__kotlinFlowSpikeGhost
    if g = "" then return "none"
    return g
end function

' ---- stage machine (one-shot settle timer between every step) ----

sub onSettle()
    m.stage = m.stage + 1
    if m.stage = 1
        stepA1Write()
    else if m.stage = 2
        sampleA1A3()
    else if m.stage = 3
        sampleA2ThenControl()
    else if m.stage = 4
        sampleControlThenKill()
    else if m.stage = 5
        sampleA4HarshThenSettledWrites()
    else if m.stage = 6
        sampleA4Settled()
    else if m.stage = 7
        stepA4LateWrite()
    else if m.stage = 8
        sampleA4Late()
    else if m.stage = 9
        sampleA5()
    end if
end sub

' Stage 1 — A1: three back-to-back synchronous writes (distinct values, in order).
sub stepA1Write()
    print "[SPIKE] a.evidence.write who=Scene values=1,2,3 t=" + m.clock.TotalMilliseconds().toStr() + "ms"
    m.global.__kotlinFlowSpikeBell = 1
    m.global.__kotlinFlowSpikeBell = 2
    m.global.__kotlinFlowSpikeBell = 3
    settle(0.6)
end sub

' Stage 2 — sample A1 (order + context) and A3 (stacking), then fire A2's writes.
sub sampleA1A3()
    bOk = (m.obsB.fires = 3) and (m.obsB.received = "1,2,3")
    print "[SPIKE] a.a1.order.B " + pf(bOk) + " fires=" + m.obsB.fires.toStr() + " received=" + m.obsB.received
    cOk = (m.obsC.fires = 3) and (m.obsC.received = "1,2,3")
    print "[SPIKE] a.a1.order.C " + pf(cOk) + " fires=" + m.obsC.fires.toStr() + " received=" + m.obsC.received
    ' Context proof, computed: whoSeen is written from the handler's own m —
    ' it reads "ObsB"/"ObsC" only if the handler ran in THAT component's context
    ' (and the write itself only lands on obsB/obsC if m.top was that node).
    print "[SPIKE] a.a1.context.B " + pf(m.obsB.whoSeen = "ObsB") + " whoSeen=" + m.obsB.whoSeen
    print "[SPIKE] a.a1.context.C " + pf(m.obsC.whoSeen = "ObsC") + " whoSeen=" + m.obsC.whoSeen
    ' A3: two scoped observers stacked on one global field both fired, equally.
    print "[SPIKE] a.a3.stacking " + pf((m.obsB.fires = 3) and (m.obsC.fires = 3)) + " bFires=" + m.obsB.fires.toStr() + " cFires=" + m.obsC.fires.toStr()

    ' A2: the SAME value twice, back-to-back (alwaysNotify=true was set at addField).
    m.preB = m.obsB.fires
    m.preC = m.obsC.fires
    print "[SPIKE] a.evidence.write who=Scene values=7,7 (same value twice) t=" + m.clock.TotalMilliseconds().toStr() + "ms"
    m.global.__kotlinFlowSpikeBell = 7
    m.global.__kotlinFlowSpikeBell = 7
    settle(0.6)
end sub

' Stage 3 — sample A2, then arm the A4 ghost-detector POSITIVE CONTROL:
' retired=true while B is still attached and ALIVE, then one bell write. B's
' handler must record the value on the global ghost record — proving the exact
' detector path (retired read -> global record write) live immediately before
' we rely on its silence (positive control before negative assertion).
sub sampleA2ThenControl()
    bDelta = m.obsB.fires - m.preB
    cDelta = m.obsC.fires - m.preC
    print "[SPIKE] a.a2.alwaysNotify.B " + pf(bDelta = 2) + " delta=" + bDelta.toStr() + " received=" + m.obsB.received
    print "[SPIKE] a.a2.alwaysNotify.C " + pf(cDelta = 2) + " delta=" + cDelta.toStr() + " received=" + m.obsC.received

    m.obsB.retired = true
    print "[SPIKE] a.evidence.write who=Scene values=6 (ghost-detector control: B alive+retired) t=" + m.clock.TotalMilliseconds().toStr() + "ms"
    m.global.__kotlinFlowSpikeBell = 6
    settle(0.6)
end sub

' Stage 4 — control verdict, reset the record, then the A4 kill + the
' same-stack (INFORMATIVE) writes.
sub sampleControlThenKill()
    ' FAIL here means the detector path is broken: every ghost verdict below
    ' would be meaningless silence — treat them as UNPROVEN, not PASS.
    print "[SPIKE] a.a4.ghostDetectorControl " + pf(ghostHas(6)) + " ghosts=" + ghostCsvOrNone()
    ' Reset: from here on, anything in the record is a genuine post-removal ghost.
    m.global.__kotlinFlowSpikeGhost = ""

    ' A4 kill: remove B from the tree, release the scene's ONLY reference.
    ' (retired stays true from the control — the detector stays armed.)
    m.preC = m.obsC.fires
    m.top.removeChild(m.obsB)
    m.obsB = invalid
    print "[SPIKE] a.evidence.removedB refs-released t=" + m.clock.TotalMilliseconds().toStr() + "ms"
    ' SAME-STACK window (INFORMATIVE): these two writes share the synchronous
    ' call stack with the removal. If node/observer teardown is deferred past
    ' the current stack (a legal implementation), 8/9 may legitimately reach
    ' B's still-live context — a teardown-TIMING fact, NOT the design's
    ' question (component death, then LATER emits — the settled window below).
    ' Marker BEFORE the writes: if they crash the render thread (run never
    ' reaches END), the capture attributes it here.
    print "[SPIKE] a.evidence.write who=Scene values=8,9 (same-stack post-removal, INFORMATIVE window) t=" + m.clock.TotalMilliseconds().toStr() + "ms"
    m.global.__kotlinFlowSpikeBell = 8
    m.global.__kotlinFlowSpikeBell = 9
    settle(0.6)
end sub

' Stage 5 — sample A4(a) survivor + the same-stack window (INFORMATIVE), then
' fire the SETTLED writes: teardown has now had a full settle step (0.6s) past
' the removal stack to complete — the PRIMARY window.
sub sampleA4HarshThenSettledWrites()
    ' Reaching this line at all is the no-crash half of (a).
    cDelta = m.obsC.fires - m.preC
    print "[SPIKE] a.a4.survivorAdvances " + pf(cDelta = 2) + " no-crash cFires=" + m.obsC.fires.toStr() + " delta=" + cDelta.toStr() + " cReceived=" + m.obsC.received
    ' Ghosts 8/9 here mean teardown was deferred past the removing call stack —
    ' informative timing fact; "none" means teardown was synchronous even
    ' within the removal's own stack.
    print "[SPIKE] a.a4.ghostFires.immediate INFORMATIVE same-stack-window ghosts=" + ghostCsvOrNone()

    print "[SPIKE] a.evidence.write who=Scene values=10,11 (settled post-removal, PRIMARY window) t=" + m.clock.TotalMilliseconds().toStr() + "ms"
    m.global.__kotlinFlowSpikeBell = 10
    m.global.__kotlinFlowSpikeBell = 11
    settle(0.6)
end sub

' Stage 6 — PRIMARY A4(b) verdict from the settled window, then create the
' FRESH late observer ObsD.
sub sampleA4Settled()
    ' Membership is value-tagged: only 10/11 (written a full settle step after
    ' removal + deref) count against this verdict — an immediate-window ghost
    ' (8/9) cannot contaminate it. FAIL here is the design-relevant FINDING:
    ' B's handler still runs for emits well after node death — scoped
    ' observers do NOT auto-detach. Cross-check any PASS against the absence
    ' of a.evidence.ghostFire lines for values 10+.
    settledGhost = ghostHas(10) or ghostHas(11)
    print "[SPIKE] a.a4.noGhostFires " + pf(not settledGhost) + " settled-window ghosts=" + ghostCsvOrNone()

    ' Fresh component arms late (its init runs synchronously inside createObject;
    ' the write it must observe happens a full settle step later — arming law).
    m.obsD = createObject("roSGNode", "ObsD")
    m.obsD.id = "obsD"
    m.top.appendChild(m.obsD)
    m.obsD.armNow = true
    print "[SPIKE] a.evidence.createdD t=" + m.clock.TotalMilliseconds().toStr() + "ms"
    settle(0.6)
end sub

' Stage 7 — A4 late-re-observe write.
sub stepA4LateWrite()
    print "[SPIKE] a.evidence.write who=Scene values=12 t=" + m.clock.TotalMilliseconds().toStr() + "ms"
    m.global.__kotlinFlowSpikeBell = 12
    settle(0.6)
end sub

' Stage 8 — sample A4 late re-observe, then signal main for A5.
sub sampleA4Late()
    dOk = (m.obsD.fires = 1) and (m.obsD.received = "12") and (m.obsD.whoSeen = "ObsD")
    print "[SPIKE] a.a4.lateReobserve.D " + pf(dOk) + " fires=" + m.obsD.fires.toStr() + " received=" + m.obsD.received + " whoSeen=" + m.obsD.whoSeen

    ' A5 (INFORMATIVE): signal main.brs, which polls readyForMain on the MAIN
    ' thread (main MAY sleep) and writes bell=13 once. 2s settle covers main's
    ' 100ms poll cadence with wide margin.
    m.preC = m.obsC.fires
    m.preD = m.obsD.fires
    print "[SPIKE] a.a5.info signaling-main t=" + m.clock.TotalMilliseconds().toStr() + "ms"
    m.global.__kotlinFlowSpikeReadyForMain = 1
    settle(2.0)
end sub

' Stage 9 — sample A5 (INFORMATIVE), final ghost re-check, summary, END.
sub sampleA5()
    cDelta = m.obsC.fires - m.preC
    dDelta = m.obsD.fires - m.preD
    ' INFORMATIVE, not PASS/FAIL: main-thread emit is out of v1 scope (spec A5);
    ' we pin the fact either way. delivered=true means both surviving observers
    ' advanced by exactly the one main-thread write.
    print "[SPIKE] a.a5 INFORMATIVE main-thread-emit delivered=" + boolStr((cDelta = 1) and (dDelta = 1)) + " cDelta=" + cDelta.toStr() + " dDelta=" + dDelta.toStr() + " cReceived=" + m.obsC.received + " dReceived=" + m.obsD.received
    ' Final ghost re-check: B has now sat through 10,11,12,13 post-settle.
    ' Value-tagged membership over every post-settle value distinguishes
    ' persistent ghosting from an immediate-window-only ghost — 8/9 may sit in
    ' the csv detail without failing this verdict.
    lateGhost = ghostHas(10) or ghostHas(11) or ghostHas(12) or ghostHas(13)
    print "[SPIKE] a.a4.noGhostFires.final " + pf(not lateGhost) + " post-settle ghosts=" + ghostCsvOrNone()
    print "[SPIKE] a.evidence.summary cReceived=" + m.obsC.received + " dReceived=" + m.obsD.received + " t=" + m.clock.TotalMilliseconds().toStr() + "ms"
    print "[SPIKE] END"
end sub
