' StopScene - drives Probe B (task control=STOP semantics) as STRICTLY SERIAL
' sub-runs, each on a FRESH StopTask node (runTask's fresh-node law). The
' render thread never sleeps: ONE one-shot Timer, re-armed with per-step
' durations, drives a step-counter state machine. The timer's "fire" observer
' is armed in init BEFORE the first start (an observer armed after a write
' never sees that write).
'
' Task nodes are deliberately left UNPARENTED (like runTask's fresh nodes);
' the node object stays readable from the render side after its thread dies,
' which is exactly what post-STOP sampling relies on - B7 verifies it
' explicitly.
'
' Approximate timeline (scene roTimespan ms; every sample offset is annotated
' with its anchor - "STOP+" = relative to the stopIssued write, "start+" =
' relative to that sub-run's start):
'   B1 sleepLoop    0.5s .. 6.5s   STOP at start+2s; samples STOP+2s/STOP+4s;
'                                  then B7
'   B2 longSleep    7s   .. 29s    STOP at start+2s; samples STOP+5s (start+7s)
'                                  and start+22s (STOP+20s - past the
'                                  sleep(20000) natural wake at start+20s)
'   B3 blockedWait  29.5s.. 46.5s  STOP at start+2s; samples STOP+3s (start+5s)
'                                  and start+17s (past the wait(15000) timeout
'                                  wake at start+15s)
'   B4 compute      47s  .. 53.5s  STOP at start+2s; samples STOP+2s/STOP+4s
'   B5 transfer     53.5s.. 60s    STOP at start+2s; INFORMATIVE sample at
'                                  STOP+4s (start+6s)
'   B6 coop cancel  60.5s.. 64.5s  cancel write at start+2s (never STOPped);
'                                  sample at cancel+2s
'   B8 verdict+END  ~64.5s
' Total ~65s -> deploy.sh CAPTURE_SECONDS defaults to 90.
'
' Caveat (commented, deliberate): if a STOP FAILS to kill a thread, that
' abandoned task keeps running past its sub-run window (all bodies are
' bounded: B1 ~10s, B4/B6 ~30s, B2 20s, B3 15s). Sequencing stays scene-
' serial regardless; the surviving thread's tagged heartbeats remain
' attributable post-hoc via their b.<sub>.hb tags.

sub init()
    di = CreateObject("roDeviceInfo")
    osv = di.GetOSVersion()
    print "[SPIKE] BEGIN probe-b model=" + di.GetModel() + " os=" + osv.major + "." + osv.minor + "." + osv.revision + " build=" + osv.build

    m.ts = CreateObject("roTimespan")
    m.ts.Mark()
    m.stepNum = 0   ' not "step": STEP is a BrightScript reserved word

    m.timer = createObject("roSGNode", "Timer")
    m.timer.repeat = false
    m.timer.observeField("fire", "onStep")   ' armed BEFORE any control="start"
    m.top.appendChild(m.timer)

    armStep(0.5)
end sub

sub armStep(seconds as float)
    m.timer.duration = seconds
    m.timer.control = "start"
end sub

function sceneT() as string
    return m.ts.TotalMilliseconds().toStr() + "ms"
end function

function bstr(b as boolean) as string
    if b then return "true"
    return "false"
end function

sub verdict(q as string, pass as boolean, detail as string)
    ' A FAIL is a FINDING, not an error - never suppressed.
    tag = "FAIL"
    if pass then tag = "PASS"
    print "[SPIKE] " + q + " " + tag + " " + detail
end sub

function startTask(mode as string) as object
    t = createObject("roSGNode", "StopTask")
    t.id = "task_" + mode
    t.mode = mode
    ' StopTask.init() has already set functionName="taskMain" (CreateObject
    ' runs init synchronously), so control="RUN" is safe here.
    t.control = "RUN"
    print "[SPIKE] b." + mode + ".info start sceneT=" + sceneT()
    return t
end function

sub onStep()
    m.stepNum = m.stepNum + 1
    s = m.stepNum

    ' ================= B1: sleep loop =================
    if s = 1
        m.b1 = startTask("b1")
        armStep(2.0)
    else if s = 2
        ' STOP at ~2s into B1.
        m.b1BeatAtStop = m.b1.beatCount
        m.b1.control = "STOP"
        print "[SPIKE] b.b1.info stopIssued sceneT=" + sceneT() + " beatAtStop=" + m.b1BeatAtStop.toStr()
        armStep(2.0)
    else if s = 3
        m.b1Beat1 = m.b1.beatCount   ' sample at STOP+2s
        armStep(2.0)
    else if s = 4
        beat2 = m.b1.beatCount       ' sample at STOP+4s
        ' Positive control before the negative assertion: prove the loop was
        ' LIVE and beating pre-STOP, so "frozen" can only mean "thread dead".
        verdict("b.b1.control", m.b1BeatAtStop >= 2, "beatAtStop=" + m.b1BeatAtStop.toStr() + " (loop was live and beating pre-STOP)")
        verdict("b.b1", m.b1Beat1 = beat2, "beatAtStop=" + m.b1BeatAtStop.toStr() + " beatStopPlus2s=" + m.b1Beat1.toStr() + " beatStopPlus4s=" + beat2.toStr() + " (equal=frozen=STOP killed the sleep loop; climbing=STOP did not kill it FINDING; promptness: last b.b1.hb t vs stopIssued sceneT)")
        runB7(m.b1Beat1 = beat2)
        armStep(0.5)

    ' ================= B2: single long blocking sleep =================
    else if s = 5
        m.b2 = startTask("b2")
        armStep(2.0)
    else if s = 6
        p = m.b2.phase
        m.b2.control = "STOP"
        print "[SPIKE] b.b2.info stopIssued sceneT=" + sceneT() + " phaseAtStop=" + p
        ' Positive control: run() entered the blocking sleep before the STOP.
        verdict("b.b2.control", p = "blocking", "phaseAtStop=" + p + " (run entered the blocking sleep pre-STOP)")
        armStep(5.0)
    else if s = 7
        print "[SPIKE] b.b2.info sceneT=" + sceneT() + " phaseAtStopPlus5s=" + m.b2.phase + " cleanupRan=" + bstr(m.b2.cleanupRan)
        armStep(15.0)
    else if s = 8
        ' B2 start+22s: past the sleep(20000) natural wake at +20s, so
        ' "still blocking" now means the thread died INSIDE the sleep.
        p = m.b2.phase
        ' The node fields alone cannot exclude a "thread survived STOP but its
        ' post-STOP field writes are dropped" world (frozen fields would look
        ' identical) - the b.b2.hb woke print bypasses the node entirely, so
        ' its ABSENCE from the capture is the required second leg.
        verdict("b.b2", (p = "blocking") and (m.b2.cleanupRan = false), "phaseAtRunEnd=" + p + " cleanupRan=" + bstr(m.b2.cleanupRan) + " (blocking+false=STOP killed the thread INSIDE one blocking sleep - valid ONLY together with no b.b2.hb woke line in the capture; woke/true=STOP is checkpoint-based FINDING)")
        armStep(0.5)

    ' ================= B3: blocked wait() =================
    else if s = 9
        m.b3 = startTask("b3")
        armStep(2.0)
    else if s = 10
        p = m.b3.phase
        m.b3.control = "STOP"
        print "[SPIKE] b.b3.info stopIssued sceneT=" + sceneT() + " phaseAtStop=" + p
        verdict("b.b3.control", p = "waiting", "phaseAtStop=" + p + " (run entered wait() pre-STOP)")
        armStep(3.0)
    else if s = 11
        ' Inside the spec's STOP+2s..+5s window.
        print "[SPIKE] b.b3.info sceneT=" + sceneT() + " phaseAtStopPlus3s=" + m.b3.phase
        armStep(12.0)
    else if s = 12
        ' B3 start+17s: past the wait(15000) timeout wake at +15s - the
        ' discriminator. Frozen at "waiting" now = the waiting thread is
        ' DEAD (a live one would have timeout-woken at ~15s).
        p = m.b3.phase
        ' Same second leg as B2: the b.b3.hb wokeFromWait print bypasses node
        ' fields, so its absence excludes the dropped-field-writes world. The
        ' wait-timeout wake MECHANISM itself is positively demonstrated in-run
        ' by B6's b.b6.hb waitTimeoutWorks heartbeat.
        verdict("b.b3", (p = "waiting") and (m.b3.cleanupRan = false), "phaseAtRunEnd=" + p + " cleanupRan=" + bstr(m.b3.cleanupRan) + " (waiting+false=STOP killed the wait()ing thread - valid ONLY together with no b.b3.hb wokeFromWait line in the capture, and read against the b.b6.hb waitTimeoutWorks positive control; wokeFromWait=STOP does not interrupt wait FINDING)")
        armStep(0.5)

    ' ================= B4: tight compute loop =================
    else if s = 13
        m.b4 = startTask("b4")
        armStep(2.0)
    else if s = 14
        m.b4BeatAtStop = m.b4.beatCount
        m.b4.control = "STOP"
        print "[SPIKE] b.b4.info stopIssued sceneT=" + sceneT() + " beatAtStop=" + m.b4BeatAtStop.toStr()
        verdict("b.b4.control", m.b4BeatAtStop > 0, "beatAtStop=" + m.b4BeatAtStop.toStr() + " (compute loop was live and beating pre-STOP)")
        armStep(2.0)
    else if s = 15
        m.b4Beat1 = m.b4.beatCount   ' sample at STOP+2s
        armStep(2.0)
    else if s = 16
        c2 = m.b4.beatCount          ' sample at STOP+4s
        ' SCOPE (inherent to any observable design): the loop is not strictly
        ' call-free - the beatCount field write every ~100k iterations (and a
        ' print every 10 beats) is a render-thread rendezvous that could
        ' itself be a STOP checkpoint. A strictly-call-free loop is
        ' unobservable and untestable; the verdict pins exactly what it says.
        verdict("b.b4", m.b4Beat1 = c2, "beatAtStop=" + m.b4BeatAtStop.toStr() + " beatStopPlus2s=" + m.b4Beat1.toStr() + " beatStopPlus4s=" + c2.toStr() + " (equal=STOP killed a compute loop whose only system calls were one field write per ~100k iterations + a print per 10 beats - NOT a strictly-call-free loop, which is unobservable; climbing=STOP needs interpreter checkpoints FINDING)")
        armStep(0.5)

    ' ================= B5: sync roUrlTransfer (INFORMATIVE) =================
    else if s = 17
        m.b5 = startTask("b5")
        armStep(2.0)
    else if s = 18
        p = m.b5.phase
        m.b5PhaseAtStop = p
        m.b5.control = "STOP"
        print "[SPIKE] b.b5.info stopIssued sceneT=" + sceneT() + " phaseAtStop=" + p
        ' Positive control: the sub-run only discriminates anything if the
        ' connect was STILL IN FLIGHT when STOP landed. 192.0.2.1:81 hangs
        ' only when the SYN is silently blackholed - on a LAN whose gateway
        ' answers ICMP net-unreachable, or behind an intercepting proxy
        ' (this environment sits behind ZScaler), GetToString returns in
        ' milliseconds and phase is already "afterTransfer" HERE, pre-STOP.
        verdict("b.b5.control", p = "transfer", "phaseAtStop=" + p + " (transfer=connect still in flight at STOP; afterTransfer=connect failed fast pre-STOP - sub-run inconclusive, step 19 emits INFORMATIVE)")
        armStep(4.0)
    else if s = 19
        ' KNOWN CONFOUNDS (both directions):
        '  - "no afterTransfer" cannot separate killed-thread from
        '    still-blocked-connect -> INFORMATIVE, not PASS/FAIL.
        '  - "afterTransfer" is a hard FINDING ONLY when the connect was
        '    still in flight at STOP (m.b5PhaseAtStop="transfer"); if phase
        '    was already "afterTransfer" at STOP the transfer failed fast
        '    pre-STOP and proves nothing about STOP - INFORMATIVE.
        ' Cross-check either way: the b.b5.hb afterTransfer heartbeat's t=
        ' value (< ~2000ms = returned pre-STOP; > ~2000ms = post-STOP).
        p = m.b5.phase
        if (p = "afterTransfer") or m.b5.cleanupRan
            if m.b5PhaseAtStop = "transfer"
                verdict("b.b5", false, "afterTransfer appeared post-STOP (phaseAtStop=transfer phase=" + p + " cleanupRan=" + bstr(m.b5.cleanupRan) + ") - hard FINDING: thread survived STOP through a sync roUrlTransfer; cross-check b.b5.hb afterTransfer t= (> ~2000ms confirms post-STOP)")
            else
                print "[SPIKE] b.b5 INFORMATIVE connect failed fast pre-STOP (phaseAtStop=" + m.b5PhaseAtStop + " phase=" + p + "); sub-run inconclusive - cross-check b.b5.hb afterTransfer t= (< ~2000ms confirms pre-STOP)"
            end if
        else
            print "[SPIKE] b.b5 INFORMATIVE phaseAtStopPlus4s=" + p + " cleanupRan=" + bstr(m.b5.cleanupRan) + " (no afterTransfer; killed-thread vs still-blocked-connect is NOT distinguishable in this window - see confound comment)"
        end if
        armStep(0.5)

    ' ================= B6: cooperative cancel field =================
    else if s = 20
        m.b6 = startTask("b6")
        armStep(2.0)
    else if s = 21
        beats = m.b6.beatCount
        verdict("b.b6.control", beats > 0, "beatsBeforeCancel=" + beats.toStr() + " (loop was live before the cancel write)")
        m.b6.cancelRequested = true   ' cooperative signal - NO STOP ever issued
        print "[SPIKE] b.b6.info cancelIssued sceneT=" + sceneT()
        armStep(2.0)
    else if s = 22
        p = m.b6.phase
        ' NOTE: a cancel-never-seen run is observed here as phase="looping" -
        ' the 300-beat loopBound natural exit lands ~30s after B6 start
        ' (~90s scene time, after END and at/past the capture edge), so
        ' "loopBound" and its exit heartbeat can never appear in the capture.
        verdict("b.b6", (p = "cleanExit") and m.b6.cleanupRan, "phase=" + p + " cleanupRan=" + bstr(m.b6.cleanupRan) + " (cleanExit+true=mid-run field reads see render-side writes AND clean unwinding runs post-loop code; looping=cancel write never seen within 2s FINDING - loopBound natural exit is post-capture, never observable here)")
        runB8()
        print "[SPIKE] END"
    end if
end sub

sub runB7(b1Dead as boolean)
    ' B7 rides on B1's abandoned node: ordinary field writes/reads post-STOP,
    ' then a second and third STOP. "No crash" is implied by the run reaching
    ' [SPIKE] END (a render-thread crash would kill the app and deploy.sh
    ' would report the missing END).
    ' GATE: cleanly interpretable only when b.b1 found the thread dead - B7
    ' runs at ~6.5s, inside B1's ~10s loop bound, so a SURVIVING B1 thread's
    ' beatCount writes race the 4242 read-backs and a b.b7 FAIL would say
    ' nothing about post-STOP node safety. In that world emit INFORMATIVE
    ' instead of a PASS/FAIL verdict.
    if not b1Dead
        print "[SPIKE] b.b7 INFORMATIVE skipped - B1 thread survived STOP, read-backs would race the live thread (re-run interpretation after the b.b1 finding)"
        return
    end if
    n = m.b1
    n.phase = "poked"
    n.beatCount = 4242
    ok1 = (n.phase = "poked")
    ok2 = (n.beatCount = 4242)
    n.control = "STOP"   ' second STOP (first was issued at step 2)
    n.control = "STOP"   ' third STOP
    ok3 = (n.phase = "poked")
    ok4 = (n.beatCount = 4242)
    verdict("b.b7", ok1 and ok2 and ok3 and ok4, "postStopWriteReadCoherent=" + bstr(ok1 and ok2) + " coherentAfterRepeatedStop=" + bstr(ok3 and ok4) + " (no-crash implied by the run reaching END)")
end sub

sub runB8()
    ' B8: cleanup-after-STOP, computed from B1 and B2 - both are far past
    ' their natural run() end by now (B1 loop bound = start+10s, B2 sleep
    ' wake = start+20s; scene time here is ~64s). cleanupRan is set by the
    ' LAST lines of run(): the raw-BRS proxy for "does a Kotlin finally run
    ' on STOP" - BrightScript try/catch (OS 11+) has NO finally clause, so
    ' trailing-lines-of-run() is the honest raw-BRS translation.
    c1 = m.b1.cleanupRan
    c2 = m.b2.cleanupRan
    ' Detail wording deliberately avoids the bare tokens " PASS"/" FAIL":
    ' deploy.sh's summary counters grep for the verdict tag and a detail
    ' containing the token would inflate them.
    verdict("b.b8", (c1 = false) and (c2 = false), "b1.cleanupRan=" + bstr(c1) + " b2.cleanupRan=" + bstr(c2) + " (false+false=hard kill skips trailing cleanup; any true=trailing cleanup survived STOP FINDING - consistent with a FINDING on the matching b.b1/b.b2 verdict)")
end sub
