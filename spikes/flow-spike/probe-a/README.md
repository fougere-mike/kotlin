# Flow-spike Probe A — global-node doorbell

Pins the StateFlow doorbell carrier facts (flow program design doc, section 8,
Probe A). Raw BrightScript; sideload with `./deploy.sh` (credentials from
`ROKU_DEVICE_IP`/`ROKU_PASSWORD` or `roku-test-app/local.properties`). The
scene (`BellScene`) plays the writer role ("component A"); `ObsB`/`ObsC`/`ObsD`
are small Group-extending observers that `observeFieldScoped` a
runtime-`addField`'d int field `__kotlinFlowSpikeBell` on the GLOBAL node
(`alwaysNotify=true`). Every verdict is computed in code from counters/string
fields sampled after settling timers; `[SPIKE] a.evidence.*` lines are
supporting evidence only. The run ends with `[SPIKE] END`; whole run is ~9s.
A FAIL is a FINDING, never an error.

Bell value map (each write window uses distinct values so the value-tagged
ghost record pins which window a fire came from): `1,2,3`=A1, `7,7`=A2,
`6`=A4 detector control, `8,9`=A4 same-stack (informative), `10,11`=A4 settled
(primary), `12`=A4 late re-observe, `13`=A5 main-thread.

## A1 — order (per-write delivery, in order, in the observer's context)

The scene writes values 1,2,3 back-to-back synchronously; B and C each append
every observed value to a `received` string field on their OWN node and count
fires. Verdict lines: `[SPIKE] a.a1.order.B PASS|FAIL fires=<n> received=<csv>`
and `.C` — PASS means exactly 3 fires AND `received=1,2,3` (any coalescing,
loss, or reordering FAILs with the actual csv as the finding). Context proof is
computed, not eyeballed: each handler writes `m.who` (set in that component's
init) into its node's `whoSeen` field — `[SPIKE] a.a1.context.B PASS|FAIL
whoSeen=<s>` passes only if the handler ran with THAT component's `m`
(wrong-context runs would read `Scene`/another who, or never land the write on
that node at all). Per-fire `a.evidence.fire who=... value=... t=...ms` lines
carry roTimespan timestamps.

## A2 — alwaysNotify (repeat values still fire)

The scene writes the SAME value (7) twice back-to-back; deltas are computed
against fire counts sampled before the writes. Verdict lines:
`[SPIKE] a.a2.alwaysNotify.B PASS|FAIL delta=<n> received=<csv>` and `.C` —
PASS means delta is exactly 2. FAIL (delta=1) means same-value writes coalesce
despite `alwaysNotify=true` at addField time, which would break the doorbell's
wraparound safety.

## A3 — stacking (two scoped observers on one global field)

Implicit in A1's data: `[SPIKE] a.a3.stacking PASS|FAIL bFires=<n> cFires=<n>`
— PASS means B and C BOTH observed all 3 of A1's writes (counts equal and
complete). FAIL means scoped observers on one global field displace or starve
each other.

## A4 — auto-detach on observer death + late re-observe

Ghost fires are recorded VALUE-TAGGED: any fire B's handler sees with
`retired=true` appends its bell value to a global string csv
(`__kotlinFlowSpikeGhost`), and every verdict below tests membership of ITS
window's values only — a ghost in one window can never contaminate another
window's verdict.

**Positive control first** (prove the detector before relying on its silence):
the scene sets `retired=true` while B is still attached and ALIVE, writes 6,
and asserts the record contains it: `[SPIKE] a.a4.ghostDetectorControl
PASS|FAIL ghosts=<csv>` — a FAIL means the detector path (retired read →
global record write) is broken and every ghost verdict below is UNPROVEN
silence, not a PASS. The record is then reset to empty.

**Kill + same-stack window (INFORMATIVE):** the scene removes B from the tree,
releases every scene reference (`m.obsB = invalid`), and writes 8,9 in the
SAME synchronous call stack. `[SPIKE] a.a4.survivorAdvances PASS|FAIL no-crash
cFires=<n> delta=<n>` — PASS means the render thread survived the post-removal
writes AND ObsC's count advanced by exactly 2 (a crash instead would abort the
run before END; deploy.sh flags that). `[SPIKE] a.a4.ghostFires.immediate
INFORMATIVE same-stack-window ghosts=<csv|none>` — values 8/9 here mean
node/observer teardown is deferred past the removing call stack (a legal
implementation), which is a teardown-TIMING fact, not the design's question;
`none` means teardown is synchronous even same-stack. Never PASS/FAIL.

**Settled window (the PRIMARY verdict):** a full settle step (0.6s) after the
removal, the scene writes 10,11. `[SPIKE] a.a4.noGhostFires PASS|FAIL
settled-window ghosts=<csv>` — computed ONLY from membership of 10/11 in the
record. PASS means B's handler never ran for emits made well after node death
— scoped observers auto-detach for the design's actual shape (component death
without cancellation, always followed by LATER emits). FAIL means they do NOT
auto-detach even after teardown has settled (a design-relevant finding, not an
error). A PASS should be cross-checked against the absence of
`a.evidence.ghostFire` lines for values 10+ in spike-results.txt (a detector
that threw would print `ghostFire ... detector-threw` instead of recording).
`[SPIKE] a.a4.noGhostFires.final PASS|FAIL post-settle ghosts=<csv>` re-checks
at the very end over every post-settle value (10,11,12,13) — 8/9 may appear in
the csv detail without failing it, so an immediate-window-only ghost is
distinguishable from persistent ghosting.

Then a FRESH ObsD is created (armed one full settle step before the next
write) and the scene writes 12: `[SPIKE] a.a4.lateReobserve.D PASS|FAIL
fires=<n> received=<csv> whoSeen=<s>` — PASS means exactly 1 fire with value
12 in D's own context (late re-observe works).

## A5 — main-thread emit (INFORMATIVE)

After A1–A4 complete (so a scene-side write has already succeeded), the scene
sets `__kotlinFlowSpikeReadyForMain=1`; `main.brs` polls that field on the MAIN
thread (main may sleep; 100ms cadence, 20s bound) and writes the bell once
(value 13) via `screen.getGlobalNode()`. Line: `[SPIKE] a.a5 INFORMATIVE
main-thread-emit delivered=<b> cDelta=<n> dDelta=<n> ...` — INFORMATIVE, never
PASS/FAIL, because main-thread emit is out of v1 scope; we want the fact
anyway. `delivered=true` means both surviving observers advanced by exactly the
one main-thread write. Supporting lines: `a.a5.info signaling-main`,
`a.a5.info main-thread-wrote value=13 waited=<ms>` (or
`a.a5.info main-poll-timeout ...` if the scene never signaled).
