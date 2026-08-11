# Render-Thread-Queue Spike Findings — roRenderThreadQueue as the PumpScheduler wakeup backend (2026-08-10)

Device: Roku Ultra 4802CA at 192.168.1.125, **Roku OS 15.2.4** (roRenderThreadQueue
requires 15.0+). Result: **all checks passed** (`deploy.sh`, results in
`spike-results.txt`, raw capture in `spike-output.txt`). **GO for the RTQ backend.**

Like the port-observe spike, this ran through the REAL toolchain: Kotlin sources
(`sources/`) compiled by the BRS compiler, deployed with kotlin-roku `installRoku`.
The artifacts that ran on device are in `generated/`. roRenderThreadQueue was
reached via `@BrsInline` shims (no stdlib API yet — Phase 1 adds it, informed by
this spike). Two `RtqProbe` instances both registered a handler for the SAME
channel id from init — deliberately mirroring a shared-channel PumpScheduler
configuration to answer the multi-instance questions in one observation.

| Check | Result | Implication for the design |
|---|---|---|
| rtq_create (render) | PASS — `CreateObject("roRenderThreadQueue")` → valid, `type()` = `roRenderThreadQueue` | Feature-detect by CreateObject-returns-invalid works. |
| rtq_create (task, main) | Valid on BOTH (task thread + main thread) | Posting from any thread is possible; creation is unrestricted. |
| rtq_handler_registration (init) | PASS — `AddMessageHandler` from component init returns a `roRenderThreadQueueRegistration` token | `attach()` can register during init. Token type name is the unregister handle. |
| rtq_handler_registration (main thread) | Returns `Invalid`, **no crash** | Render-thread-only confirmed; silent Invalid, no throw. Main thread keeps runBlocking/runPumping. |
| rtq_handler_scope | **Each handler invocation runs in its REGISTERING component's own script context**: for one post, A's handler saw `m.top`=probeA and B's handler saw `m.top`=probeB (`type(m)`=roAssociativeArray, component properties readable, typed field writes work) | THE load-bearing answer: a component's scheduler handler drains that component's own (per-instance) CoroutineQueue. No foreign-context hazard. |
| globalaa_identity | **GetGlobalAA is PER-COMPONENT-INSTANCE** on the render thread: both instances saw `counter=1`, own marker only (`marker2=false`) | CoroutineQueue/DelayTracker/PumpScheduler `object` singletons are per-component. Each component needs its own attach/registration (the design already assumed this domain). `m.global` is a valid roSGNode at init → session-wide backend cache on the global node is viable. |
| rtq_channel_identity | Channels are APP-GLOBAL and multi-handler: one post to `spike.pump` fired BOTH probes' handlers (each in its own context), from render-thread posts AND task-thread posts | A shared channel id would wake EVERY component's scheduler on every post (N invocations/post). **Production uses per-instance channel ids** (`kotlin.pump.<uuid>`) so one post wakes exactly one scheduler. |
| rtq_self_post_latency | **latencyMs=0** in the handler (post at 1009ms, port mirror at 1012ms; second handler at 1020ms) | Sub-frame immediate wakeup. Far better than the 10ms repeating Timer, with zero idle cost. |
| rtq_cross_thread_post | PASS — task-thread `run()` created its own RTQ handle and posted; payload AA (`src`, `num`) arrived intact at render-thread handlers | Cross-thread posting works as documented. (Production pump doesn't need it — runTask resumption is observer-based — but it's available.) |
| rtq_post_before_handler | **DROPPED, not queued** — platform prints `could not deliver message-id 'spike.early' from '<caller>'`; a handler registered later never received it | Register-before-post is mandatory. PumpScheduler registers at attach() and only posts after — safe. The console diagnostic is a nice debugging breadcrumb. |
| unparented_timer_fires | BOTH a fully-unparented Timer and a Timer child of an out-of-scene Group FIRED (~420ms after start, port-observed) | The Timer fallback/delay-deadline Timer has NO scene-attachment constraint. We still parent it to the component's top (matches the 4 existing device-proven components), but init-before-scene-attach cannot stall the pump. |
| msgInfo contents | `FormatJson(msgInfo)` → empty string | msgInfo is not JSON-serializable on this OS; don't rely on its contents (we don't). |

Event trace (from `spike-results.txt`):

```
SPIKE|PASS|rtq_create_and_register|instance=1|rtqType=roRenderThreadQueue|token=roRenderThreadQueueRegistration|mGlobal=roSGNode   (x2, probeA+probeB)
SPIKE|INFO|selfpost_sent|id=probeA|at=1009ms
SPIKE|INFO|pump_handler|src=A-self|latencyMs=0|...|nodeId=probeA
SPIKE|EVENT|lastEvent|#1|from=probeA|src=A-self|at=1012ms
SPIKE|INFO|pump_handler|src=A-self|...|nodeId=probeB          <- same post, second registered handler, ITS own context
SPIKE|EVENT|lastEvent|#2|from=probeB|src=A-self|at=1020ms
... (B-cross and task posts likewise fired both handlers)
could not deliver message-id 'spike.early' from 'rtqprobe_oncommand_rosgnodeevent_k_'   <- platform diagnostic for the pre-registration post
SPIKE|INFO|gaa_identity|id=probeA|instanceProp=1|counter=1|marker1=true|marker2=false   (identical from probeB)
SPIKE|EVENT|timer_fire|from=timer1|at=7626ms  (unparented; started 7200ms, duration 0.4)
SPIKE|EVENT|timer_fire|from=timer2|at=7631ms  (child of out-of-scene Group)
SPIKE|INFO|main_rtq_create|type=roRenderThreadQueue
SPIKE|INFO|main_rtq_addhandler|token=Invalid   <- render-thread-only, silent
```

## Decisions fed into Phase 1 (PumpScheduler)

1. **GO on the RTQ backend** — all three gate criteria met (init registration works,
   handler reaches the registering instance's own scope, latency ≲ one frame).
2. **Per-instance channel id**: `"kotlin.pump." + <uuid>` (roDeviceInfo GetRandomUUID
   at attach), NOT a shared id — avoids N-handler storms and pre-registration drops.
3. **GetGlobalAA is per-component-instance** — every stdlib `object` singleton
   (CoroutineQueue, DelayTracker, Dispatchers, TaskRunner, PumpScheduler) is
   per-component state on the render thread. The existing per-component pump
   boilerplate was in fact REQUIRED under these semantics; the scheduler simply
   automates the same per-component wiring. Backend DETECTION is still cached
   once per session on the global node (valid at init).
4. Timer fallback + delay-deadline Timer: parent to `hostTop` (proven), but
   unparented firing means no ordering constraint vs scene attachment.
5. Registration must precede the first post — attach() before any enqueue-driven
   post; never post to a channel that might lack a handler.

Notes:
- Spike edits to roku-test-app were reverted after the run (WIP snapshot commit
  f72f6cd "wip: pre-spike snapshot" is the restore point); these copies are the
  durable record.
- The handler signature `(data, msgInfo)` works as a component METHOD registered
  via `brsName(::onRtqMessage)` — same mechanism as observer callbacks.
- `Dynamic?` is not a supertype of external interface types in the BRS frontend —
  shim payload params must be `Any?` (compile error otherwise).
