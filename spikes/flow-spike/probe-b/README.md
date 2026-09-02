# Probe B — task `control="STOP"` semantics

Raw-BrightScript spike fixture for the Flow program (design doc
`docs/superpowers/plans/2026-09-02-flow-program-design.md`, §8 "Probe B — task
stop"). Pins the Task-thread stop/cancellation facts that decide the two-layer
cancellation design and the `runTask` STOP rider.

Run with `./deploy.sh` (credentials from `ROKU_DEVICE_IP`/`ROKU_PASSWORD` or
`roku-test-app/local.properties`; capture defaults to 90s — the run itself is
~65s). Verdicts land in `spike-results.txt`; the sentinel-filtered raw capture
is committed as `../capture-probe-b.txt` (subsequent runs `-runN`).

Shape: `StopScene` sequences six STRICTLY SERIAL sub-runs, each on a FRESH
`StopTask` node (one Task subtype, `mode` field selects the body — mirrors
`runTask`'s fresh-node law). The task writes progress into its own declared
fields (`beatCount`/`phase`/`cleanupRan`); the scene issues `control="STOP"`
at fixed points, samples the fields at settle points, and computes verdicts in
code. Task heartbeat lines (`[SPIKE] b.<sub>.hb ... t=<ms>ms`, roTimespan from
`run()` entry) are the promptness evidence: compare the last heartbeat's `t`
against the matching `[SPIKE] b.<sub>.info stopIssued sceneT=` line (the
`.info start sceneT=` line ties the two clocks; run-entry is within
thread-spawn latency of it).

Every primary sub-run (B1–B6) has a positive control (`b.<q>.control`) where
the mechanism must be proven live BEFORE its absence is read as thread death
(B7/B8 derive from B1/B2 and lean on those controls). A
FAIL is a FINDING, never an error — deploy fails only when `[SPIKE] END` never
prints.

## B1 — sleep loop

`run()` beats every 250ms (`sleep(250) : beatCount++`, bounded ~10s); STOP at
~2s. Verdict lines: `[SPIKE] b.b1.control PASS|FAIL` (beats landed pre-STOP —
must PASS for B1 to mean anything) and `[SPIKE] b.b1 PASS|FAIL` comparing
`beatCount` sampled at STOP+2s vs STOP+4s. PASS (equal → frozen) = STOP killed
the sleeping loop thread; FAIL (climbing) = STOP did not kill a sleep loop
(finding). Promptness: last `b.b1.hb` `t` vs the `stopIssued` line.

## B2 — single long blocking call (sleep 20s)

`run()` sets `phase="blocking"`, one `sleep(20000)`, then `phase="woke"` +
`cleanupRan=true`. STOP at ~2s, mid-sleep. Evidence line `b.b2.info
phaseAtStopPlus5s=...`; verdict `[SPIKE] b.b2 PASS|FAIL` sampled at start+22s
(past the natural 20s wake). PASS (`blocking` + cleanupRan false) = STOP
killed the thread INSIDE one blocking call — **valid only together with the
absence of any `b.b2.hb woke` line in the capture** (the heartbeat print
bypasses node fields, so it excludes a "thread survived but its post-STOP
field writes were dropped" world that frozen fields alone cannot). FAIL
(`woke`/cleanupRan true, `b.b2.hb woke` line ~18s after STOP) = STOP could not
interrupt a blocking sleep — STOP is checkpoint-based (finding).

## B3 — blocked `wait()` on a never-signaled port

`run()` sets `phase="waiting"`, `wait(15000, port)` on a port nobody signals,
then `phase="wokeFromWait"` + `cleanupRan=true`. STOP at ~2s. The 15s timeout
is the discriminator: a LIVE thread timeout-wakes at ~15s even with no
message. Verdict `[SPIKE] b.b3 PASS|FAIL` sampled at start+17s. PASS (frozen
at `waiting`, wake never lands) = STOP killed the waiting thread — **valid
only together with the absence of any `b.b3.hb wokeFromWait` line in the
capture** (like B2, the print bypasses node fields and excludes the
dropped-field-writes world). The wait-timeout wake mechanism itself is
positively demonstrated in-run: B6's first beat is a `wait(100, port)` on a
throwaway port and prints `b.b6.hb waitTimeoutWorks` when it timeout-wakes —
check that line exists before reading B3's PASS as thread death. FAIL
(`wokeFromWait`) = STOP does not interrupt `wait()` (finding). Interim
evidence: `b.b3.info phaseAtStopPlus3s=` (inside the spec's +2s..+5s window).

## B4 — tight compute loop (no sleep/wait)

`run()` does ~30s of arithmetic, `beatCount++` every ~100k iterations, zero
blocking calls. STOP at ~2s. Verdict `[SPIKE] b.b4 PASS|FAIL` comparing
`beatCount` at STOP+2s vs STOP+4s (both post-settle; the `beatAtStop` value in
the detail is the raced-at-STOP reference). PASS (frozen) pins exactly this:
STOP killed a compute loop **whose only system calls were one `beatCount`
field write per ~100k iterations plus a print per 10 beats** — the field
write is a render-thread rendezvous that could itself be the STOP checkpoint,
and a strictly-call-free loop is unobservable and untestable, so the claim is
deliberately scoped and not "STOP kills any pure-compute thread". FAIL
(climbing) = STOP needs interpreter checkpoints (finding). `b.b4.control`
proves the loop beat pre-STOP.

## B5 — sync roUrlTransfer (INFORMATIVE)

`run()` sets `phase="transfer"` then a synchronous `GetToString()` against
`http://192.0.2.1:81/` (RFC5737 TEST-NET-1 — the connect hangs **only when
the SYN is silently blackholed**; a gateway answering ICMP net-unreachable or
an intercepting proxy such as ZScaler makes it fail in milliseconds — no
external dependency either way). STOP at ~2s. Positive control `[SPIKE]
b.b5.control PASS|FAIL`: `phaseAtStop="transfer"` proves the connect was
still in flight when STOP landed; if phase was already `afterTransfer` at
STOP, the connect failed fast pre-STOP and the sub-run is inconclusive.
KNOWN CONFOUNDS, both directions: "no afterTransfer" within the window cannot
distinguish killed-thread from still-blocked-connect → `[SPIKE] b.b5
INFORMATIVE ...` (the normal outcome); and `afterTransfer` appearing at the
+4s sample is a hard `[SPIKE] b.b5 FAIL` (thread survived STOP through a sync
transfer, finding) **only when the control saw `transfer` at STOP** — with a
fast-failed connect the scene emits `[SPIKE] b.b5 INFORMATIVE connect failed
fast pre-STOP ... sub-run inconclusive` instead. Cross-check either reading
against the `b.b5.hb afterTransfer` heartbeat's `t=` value (< ~2000ms =
returned pre-STOP, > ~2000ms = post-STOP). Any platform thread diagnostics
the console prints near the STOP are post-hoc evidence; keep them with the
capture.

## B6 — cooperative cancel field (no STOP)

Task XML declares `cancelRequested`; `run()` beats every 100ms and exits the
loop when it reads the field true, then sets `cleanupRan=true` +
`phase="cleanExit"`. The scene writes `cancelRequested=true` at ~2s and NEVER
issues STOP. Verdict `[SPIKE] b.b6 PASS|FAIL` sampled 2s after the cancel
write. PASS (`cleanExit` + cleanupRan) = mid-run `m.top` reads see
render-side writes AND clean unwinding runs post-loop code — the
Kotlin-`finally` analogue for the cooperative cancellation layer. A
cancel-never-seen run shows as **`phase="looping"`** at the sample — the
distinct `loopBound` natural-exit phase lands ~30s after B6 start (~90s scene
time, after `[SPIKE] END` and at/past the capture edge), so `loopBound` and
its exit heartbeat are never observable in the capture. `b.b6.control` proves
beats pre-cancel. B6's first beat doubles as B3's wait-timeout positive
control (`b.b6.hb waitTimeoutWorks` — see B3).

## B7 — post-STOP node safety + STOP idempotence

Rides on B1's abandoned node: after B1's verdict, the scene writes ordinary
fields (`phase`, `beatCount`) on the dead-thread node, reads them back, issues
a second and a third STOP, reads again. Verdict `[SPIKE] b.b7 PASS|FAIL`
(read-backs coherent both before and after the repeated STOPs); "no crash" is
implied by the run reaching `[SPIKE] END` — a crash aborts the run and
deploy.sh reports the missing END. Gated on the B1 outcome already in hand:
when B1's thread survived STOP (beat samples not equal), the read-backs would
race the live thread's `beatCount` writes (B7 runs at ~6.5s, inside B1's ~10s
loop bound), so the scene emits `[SPIKE] b.b7 INFORMATIVE skipped - B1 thread
survived STOP ...` instead of a PASS/FAIL; re-interpret after resolving the
b.b1 finding.

## B8 — cleanup-after-STOP (the Kotlin-`finally` proxy)

Computed at the end from B1's and B2's nodes, both far past their natural
`run()` end (B1 bound start+10s, B2 wake start+20s; checked at ~64s):
`cleanupRan` is set by the LAST lines of `run()`. Verdict `[SPIKE] b.b8
PASS|FAIL`. PASS (both still false) = a hard STOP skips trailing cleanup code.
FAIL (either true) = trailing cleanup survived STOP — consistent with a
finding on the matching b.b1/b.b2 verdict (checkpoint-based STOP). **Proxy
mapping:**
BrightScript try/catch exists (OS 11+) but has NO `finally` clause, so
"did the trailing lines of `run()` execute after STOP" is the honest raw-BRS
translation of "does a Kotlin `finally` run on STOP" — the fact the design's
dispose contract (design doc §5, decision 9) needs pinned.
