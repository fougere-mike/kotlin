' StopTask - one Task subtype for all Probe B sub-runs; m.top.mode selects the
' body. The scene creates a FRESH node per sub-run (mirroring runTask's
' fresh-node-per-invocation law), issues control="STOP" (or, for B6, the
' cooperative cancelRequested write), and samples the typed fields
' (beatCount / phase / cleanupRan) at settle points after the thread should
' be gone.
'
' Every heartbeat print carries roTimespan ms measured from run() entry -
' the TASK-side clock for promptness analysis. The scene prints its own
' sceneT ms at sub-run start and at stopIssued; run-entry is within
' thread-spawn latency of the sub-run start line, which ties the two clocks
' well enough to read STOP promptness off the last heartbeat.
'
' NOTE (B8's proxy mapping): BrightScript try/catch exists (OS 11+) but has
' NO finally clause. "cleanupRan is set by the LAST lines of run()" is
' therefore the honest raw-BRS translation of "does a Kotlin finally run on
' STOP" - if those trailing lines never execute after a STOP, a hard kill
' skips trailing cleanup code.

sub init()
    ' Raw-BRS Task law: functionName must be set before the scene writes
    ' control="RUN". init() runs at CreateObject time, safely before that.
    ' Named "taskMain", not "run": RUN is a BrightScript reserved word
    ' (like STEP/STOP), so "sub run()" would not compile on device.
    m.top.functionName = "taskMain"
end sub

sub taskMain()
    ts = CreateObject("roTimespan")
    ts.Mark()
    mode = m.top.mode
    print "[SPIKE] b." + mode + ".hb run-entry t=" + ts.TotalMilliseconds().toStr() + "ms"
    if mode = "b1"
        runB1(ts)
    else if mode = "b2"
        runB2(ts)
    else if mode = "b3"
        runB3(ts)
    else if mode = "b4"
        runB4(ts)
    else if mode = "b5"
        runB5(ts)
    else if mode = "b6"
        runB6(ts)
    else
        print "[SPIKE] b.task.hb ERROR unknown mode=" + mode
    end if
end sub

sub runB1(ts as object)
    ' B1: sleep loop, beat every 250ms. Bounded at 40 beats (~10s) so a STOP
    ' that does NOT kill the thread still reaches a natural end - the
    ' trailing cleanup lines below double as B8's probe.
    m.top.phase = "looping"
    n = 0
    for i = 1 to 40
        sleep(250)
        n = n + 1
        m.top.beatCount = n
        print "[SPIKE] b.b1.hb beat=" + n.toStr() + " t=" + ts.TotalMilliseconds().toStr() + "ms"
    end for
    ' Trailing cleanup (B8 proxy): reached only if the thread survived.
    m.top.cleanupRan = true
    m.top.phase = "cleanExit"
    print "[SPIKE] b.b1.hb cleanExit t=" + ts.TotalMilliseconds().toStr() + "ms"
end sub

sub runB2(ts as object)
    ' B2: ONE long blocking call. STOP arrives ~2s in, mid-sleep. If "woke"
    ' appears ~18s after the STOP, STOP could not interrupt a blocking sleep
    ' (checkpoint-based FINDING); if it never appears and phase stays
    ' "blocking", STOP killed a thread INSIDE one blocking call.
    m.top.phase = "blocking"
    print "[SPIKE] b.b2.hb blocking t=" + ts.TotalMilliseconds().toStr() + "ms"
    sleep(20000)
    m.top.phase = "woke"
    m.top.cleanupRan = true
    print "[SPIKE] b.b2.hb woke t=" + ts.TotalMilliseconds().toStr() + "ms"
end sub

sub runB3(ts as object)
    ' B3: blocked in wait() on a port nobody ever signals. The 15s timeout is
    ' the discriminator: a live thread wakes at ~15s even with no message, so
    ' "wokeFromWait never lands" (sampled past start+17s) means the waiting
    ' thread is dead, not merely still waiting.
    port = CreateObject("roMessagePort")
    m.top.phase = "waiting"
    print "[SPIKE] b.b3.hb waiting t=" + ts.TotalMilliseconds().toStr() + "ms"
    r = wait(15000, port)
    m.top.phase = "wokeFromWait"
    m.top.cleanupRan = true
    print "[SPIKE] b.b3.hb wokeFromWait t=" + ts.TotalMilliseconds().toStr() + "ms"
end sub

sub runB4(ts as object)
    ' B4: tight compute loop - NO sleep/wait anywhere. Probes whether STOP
    ' can kill a thread that never reaches a blocking call, or whether it
    ' needs interpreter checkpoints. Bounded at ~30s of arithmetic; beat
    ' every ~100k inner iterations, heartbeat every 10th beat (console load).
    ' SCOPE CAVEAT (inherent to any observable design - the fields are how
    ' the scene samples at all): this is not PURELY compute. The
    ' m.top.beatCount write is a render-thread rendezvous every ~100k
    ' iterations (and the print every 10 beats another system call); if STOP
    ' only takes effect at system-call checkpoints, the field write itself
    ' could be the checkpoint. A frozen counter therefore pins "STOP killed a
    ' compute loop whose only system calls were one field write per ~100k
    ' iterations" - a strictly-call-free loop is unobservable and untestable.
    m.top.phase = "computing"
    n = 0
    acc = 0.0
    while ts.TotalMilliseconds() < 30000
        for j = 1 to 100000
            acc = acc + j * 0.5
        end for
        n = n + 1
        m.top.beatCount = n
        if n mod 10 = 0
            print "[SPIKE] b.b4.hb beat=" + n.toStr() + " t=" + ts.TotalMilliseconds().toStr() + "ms"
        end if
    end while
    m.top.cleanupRan = true
    m.top.phase = "computeDone"
    print "[SPIKE] b.b4.hb computeDone beats=" + n.toStr() + " t=" + ts.TotalMilliseconds().toStr() + "ms"
end sub

sub runB5(ts as object)
    ' B5 (INFORMATIVE): synchronous roUrlTransfer GetToString against
    ' http://192.0.2.1:81/ (RFC5737 TEST-NET-1 - the connect hangs; no
    ' external dependency, nothing routable answers).
    ' KNOWN CONFOUND: within the capture window "no afterTransfer" cannot
    ' distinguish killed-thread from still-blocked-connect, so the scene
    ' emits an INFORMATIVE line for that case. Any "afterTransfer" appearing
    ' post-STOP IS a hard FINDING (thread survived STOP through a sync
    ' transfer) and the scene emits FAIL for it. Console may also print
    ' platform thread diagnostics - post-hoc evidence.
    m.top.phase = "transfer"
    print "[SPIKE] b.b5.hb transfer t=" + ts.TotalMilliseconds().toStr() + "ms"
    xfer = CreateObject("roUrlTransfer")
    xfer.SetUrl("http://192.0.2.1:81/")
    body = xfer.GetToString()
    m.top.phase = "afterTransfer"
    m.top.cleanupRan = true
    print "[SPIKE] b.b5.hb afterTransfer len=" + body.len().toStr() + " t=" + ts.TotalMilliseconds().toStr() + "ms"
end sub

sub runB6(ts as object)
    ' B6: cooperative cancel - NO STOP is ever issued. Probes (a) mid-run
    ' m.top reads see render-side writes, (b) clean unwinding runs the
    ' post-loop code - the Kotlin-finally analogue for the COOPERATIVE
    ' cancellation layer. Bounded at 300 beats (~30s); the distinct
    ' "loopBound" phase separates "cancel never seen" from a clean exit
    ' (NOTE: loopBound lands ~30s in - past [SPIKE] END and the capture
    ' edge - so in the CAPTURE a cancel-miss shows as phase="looping" at the
    ' scene's cancel+2s sample; see the b.b6 verdict comment).
    '
    ' B6 also hosts the run's ONE positive control for wait-timeout wake,
    ' B3's liveness discriminator: beat 1 uses wait(100, port) on a
    ' throwaway never-signaled port instead of sleep(100). The
    ' waitTimeoutWorks heartbeat proves wait() timeout-wakes a LIVE task
    ' thread on this device/OS, so B3's negative assertion ("the 15s wake
    ' never landed => thread dead") rests on an in-run demonstrated
    ' mechanism, not an assumed one. This body is never STOPped, so the
    ' control is uncontaminated.
    m.top.phase = "looping"
    wport = CreateObject("roMessagePort")   ' throwaway; never signaled
    n = 0
    sawCancel = false
    for i = 1 to 300
        if i = 1
            r = wait(100, wport)
            print "[SPIKE] b.b6.hb waitTimeoutWorks t=" + ts.TotalMilliseconds().toStr() + "ms (wait(100,port) timeout-woke a live task thread - B3's discriminator mechanism proven in-run)"
        else
            sleep(100)
        end if
        n = n + 1
        m.top.beatCount = n
        if n mod 5 = 0
            print "[SPIKE] b.b6.hb beat=" + n.toStr() + " t=" + ts.TotalMilliseconds().toStr() + "ms"
        end if
        if m.top.cancelRequested
            sawCancel = true
            print "[SPIKE] b.b6.hb sawCancel beat=" + n.toStr() + " t=" + ts.TotalMilliseconds().toStr() + "ms"
            exit for
        end if
    end for
    m.top.cleanupRan = true
    if sawCancel
        m.top.phase = "cleanExit"
    else
        m.top.phase = "loopBound"
    end if
    print "[SPIKE] b.b6.hb exit phase=" + m.top.phase + " t=" + ts.TotalMilliseconds().toStr() + "ms"
end sub
