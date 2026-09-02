# Flow-program spike findings (Phase 0 of the Flow program)

**Date:** 2026-09-02
**Device:** Roku Ultra 4800X, OS 15.3.4 build 2402 (`192.168.1.125`)
**Design under test:** `docs/superpowers/plans/2026-09-02-flow-program-design.md` §8
**Raw evidence:** `capture-probe-a.txt`, `capture-probe-b.txt` (sentinel-filtered,
run-attributable slices; per-run `-runN` suffixes on any later run). Verdict
conventions and per-question mapping: each probe package's `README.md`.

**Outcome: no design assumption was falsified.** Both probes confirm the approved
architecture. One sub-question (B5) is inconclusive on this network and stays
recorded as spike bait.

## Probe A — global-node doorbell carrier (12 PASS / 0 FAIL)

Facts of record for the StateFlow doorbell (design §6):

- **A1 — ordered per-write delivery, observer context.** A runtime-`addField`'d
  int field on the GLOBAL node, written 3× back-to-back from one component's
  context, fired BOTH other components' `observeFieldScoped` observers 3× each,
  values in order, each handler running in ITS OWN component's context
  (`a.a1.order.B/C`, `a.a1.context.B/C`).
- **A2 — alwaysNotify honored** on a same-value repeat write (`a.a2.*` delta=2).
- **A3 — scoped observers stack** on one global field (two observers, both fully
  delivered; `a.a3.stacking`).
- **A4 — auto-detach on observer death.** After removing the observing component
  from the tree and releasing every reference: no crash, the surviving observer
  kept advancing, and NO ghost fires landed in the same-stack window, the settled
  window, or the whole remaining run (`a.a4.survivorAdvances`,
  `a.a4.ghostFires.immediate` = none, `a.a4.noGhostFires`,
  `a.a4.noGhostFires.final`) — detach was immediate, not merely eventual. The
  ghost detector was positively controlled while the observer was still alive
  (`a.a4.ghostDetectorControl`). A FRESH component observing the same field
  afterward receives subsequent writes (`a.a4.lateReobserve.D`).
- **A5 (INFORMATIVE) — main-thread emit delivered.** A write to the global field
  from the MAIN thread fired both render-side observers (`a.a5`). Out of v1
  scope (render-component-only emit law stands); recorded for the future.

**Design consequence:** the §6 hot-tier carrier (value on the live shared object,
version-int doorbell on the global node, `observeFieldScoped` collectors,
platform-native teardown, no registry) rides entirely on pinned facts now.

## Probe B — task `control="STOP"` matrix (12 PASS / 1 control-FAIL / 1 INFORMATIVE)

Facts of record for two-layer cancellation (design §5, decision 8) and the
runTask STOP rider:

- **B1 — STOP kills a sleep loop.** `beatCount` frozen at STOP+2s and STOP+4s
  (7 → 7 → 7); last heartbeat 1754ms vs STOP at 2502ms — prompt.
- **B2 — STOP kills a thread INSIDE one blocking call.** Stopped mid-`sleep(20000)`:
  phase stayed `blocking` past the natural 20s wake, no `woke` line ever,
  `cleanupRan=false`. STOP is a hard mid-block kill, NOT checkpoint-based.
- **B3 — STOP kills a thread blocked in `wait()`.** No timeout wake ever landed
  (sampled past the 15s wait timeout), phase frozen at `waiting`. The
  wait-timeout discriminator itself was positively controlled in-run
  (`b.b6.hb waitTimeoutWorks`).
- **B4 — STOP kills a compute loop** (one field write per ~100k iterations + a
  print per 10 beats — NOT a strictly call-free loop, which is unobservable;
  caveat stated in the verdict line). Count frozen 38 → 38 → 38.
- **B5 — INCONCLUSIVE (network): sync roUrlTransfer interruptibility.** The
  TEST-NET connect failed FAST (39ms, len=0) — this network refuses/intercepts
  instead of black-holing — so the thread was never actually blocked in the
  transfer at STOP time. The control gate caught it (`b.b5.control FAIL` →
  verdict demoted to INFORMATIVE instead of pinning a wrong fact). SPIKE BAIT:
  re-run B5 with a genuinely slow/hanging endpoint (e.g. a LAN listener that
  accepts and stalls) to pin blocked-TRANSFER kill specifically. B2/B3 pin the
  general mid-block kill for the platform's own blocking primitives.
- **B6 — cooperative cancel field works.** A render-side write to a bool field on
  the running task's node is SEEN by the task's mid-loop dot-read (~0ms later
  beat), the loop exits, and post-loop code runs (`phase=cleanExit`,
  `cleanupRan=true`) — clean unwinding, the Kotlin-`finally` analogue for the
  cooperative layer.
- **B7 — abandoned node safe.** Post-STOP field writes/reads on the dead task's
  node are coherent; second and third STOPs are no-ops (no crash).
- **B8 — hard kill skips trailing cleanup.** `cleanupRan` stayed false on the
  B1/B2 nodes read ~64s in, far past both natural run() ends: code after the
  killed point NEVER executes — a Kotlin `finally` cannot run on hard STOP.

**Design consequences:**
- The §5 two-layer cancellation ships as designed: cancel-request field first
  (cooperative unwind runs finallys — B6), then `control="STOP"` (prompt hard
  kill — B1–B4), with the dispose contract exactly as written (render-side
  guaranteed; task-side best-effort at checkpoints — B8 is the proof the
  best-effort caveat is REQUIRED, not pessimism).
- The runTask STOP rider is GO: STOP is safe (B7) and effective (B1–B4).
- Promptness can be documented as "hard-prompt" (sub-second in all pinned
  shapes), not "bounded by the blocking call" — with the B5 transfer residual
  noted where the docs say so.

## Fixture provenance

Both packages were authored and adversarially verified (discrimination/confound,
platform-law, and mechanical lenses; 12 agents) before first deploy; the A4
settled-window redesign and the B5 control gate — the two things that made these
results trustworthy — came out of that review. Packages: `probe-a/`, `probe-b/`
(raw BRS, channel-matrix harness lineage; `deploy.sh` in each).
