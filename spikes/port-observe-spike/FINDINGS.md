# Port-Observe Spike Findings — Spike A: main-thread port rendezvous (2026-07-31)

Device: Roku at 192.168.1.125. Result: **8/8 checks passed** (`deploy.sh`, results in
`spike-results.txt`, raw capture in `spike-output.txt`). **The E2E harness architecture
stands**: field changes made on the render thread and on a task thread ARE delivered as
roSGNodeEvents to the main thread's message port via the port form of
`observeField(field, port)` / `observeFieldScoped(field, port)`, waking
`port.waitMessage(100)` while `main()` never returns.

Unlike Spike B (hand-written BRS side-load), this spike ran through the REAL toolchain:
Kotlin sources (`sources/`) compiled by the BRS compiler, packaged and deployed with the
kotlin-roku `installRoku` Gradle task. The generated artifacts that ran on device are in
`generated/`. The port form was reached via `@BrsInline` shims in `sources/Main.kt`
(the stdlib has no port-form observeField API yet — SceneGraph.kt:159-185 has
function-name forms only); the shims spliced to clean `probe.observeField("echo", port)`
call sites (see `generated/MainKt.brs`).

| Check | Result | Implication for the design |
|---|---|---|
| port_observe_accepted | PASS | `node.observeField(field, port)` called from main() returns `true` for fields on a scene-parented component. The port form works from the main thread — no Task-node relay fallback needed. |
| scoped_port_observe_accepted | PASS | `node.observeFieldScoped(field, port)` ALSO returns `true` from main() and delivers events (see main_set_self_event). The harness can use the scoped form for auto-cleanup semantics. |
| render_onchange_to_main_port | PASS | Two full rendezvous cycles: main() `setField("input", v)` → `@BrsOnChange` handler on the RENDER thread writes `echo` → roSGNodeEvent wakes `waitMessage(100)` on the main port. Round-trip latency ~0-1ms. This is the harness's core await primitive. |
| main_set_self_event | PASS | The main thread's own `setField` on an observed field (`input`, scoped observer) is delivered back to the main port. Driver-side writes are observable — useful, but means the driver will see its OWN writes; the harness must filter by field/value, not assume every event is a fixture response. |
| spontaneous_timer_write | PASS | A render-thread Timer `fire` handler write (`timerOut` at ~496ms, not triggered by any concurrent main-thread set) is delivered to the main port. Asynchronous fixture-initiated signals work. |
| event_accessors | PASS | `getField()` = field name, `getData()` = new value, `getRoSGNode().subtype()` = "PortProbe" — all usable. **`getNode()` returned the node's `id` STRING ("probe1", typeOf=String), not a node object.** The stdlib's `RoSGNodeEvent.getNode(): RoSGNode` typing is wrong per device behavior (matches Roku docs: getNode returns the id). Harness must use `getRoSGNode()` for the object and should set `id` on fixtures for event correlation; a later stdlib fix should retype `getNode()` as `String`. |
| task_thread_write_to_main_port | PASS (stretch) | An UNPARENTED `CreateObject("roSGNode","SpikeTask")` task node: port-observe `taskOut` → set input → `control=RUN` → the run body's TASK-thread `setField` arrives on the main port (~4ms after RUN). Bonus: this was a **Kotlin `TaskComponent`** — first on-device proof that TaskComponent codegen (XML extends="Task", `functionName` via `brsName(::method)`, run body as top-level sub) works end-to-end. |
| final_field_reads | PASS | After the event phase, direct `getField()` reads from main() agree with the last events (`echo:ping2`, `timer-fired`, `task-wrote:hello-task`). Events and field state are consistent. |

Event trace (from `spike-results.txt`):

```
SPIKE|observe_returns|echo=true|timerOut=true|inputScoped=true
SPIKE|EVENT|#1|field=echo|data=echo:ping1|at=0ms
SPIKE|EVENT|#2|field=input|data=ping1|at=0ms
SPIKE|EVENT|#3|field=echo|data=echo:ping2|at=0ms
SPIKE|EVENT|#4|field=input|data=ping2|at=1ms
SPIKE|EVENT|#5|field=timerOut|data=timer-fired|at=496ms
SPIKE|EVENT|#6|field=taskOut|data=task-wrote:hello-task|at=500ms
```

Notes:
- **Cross-field event ordering is not write-order**: `input` was set before `echo` was
  written (the echo write happens in input's onChange), yet the `echo` event (#1) was
  dequeued before the `input` event (#2). Per-field ordering held (ping1 before ping2,
  echo:ping1 before echo:ping2). The harness must not assume ordering across different
  observed fields — correlate by (field, value).
- The compiler-generated component `init()` assigns every `@SG*Field` property its Kotlin
  default (`m.top.input = ""`), which fires the field's onChange once at init time —
  before main() attaches observers, so nothing leaked to the port here. A harness fixture
  whose onChange has side effects will see one init-time invocation with the default value.
- `@BrsInline` extraction requires the annotation body to be a single `return expr`
  statement for expression-position use (IrExpressionToBrsTransformer.transformBrsInlineCall);
  both shims follow that shape. This is the pattern to lift into a real stdlib
  `observeField(field: RoMessagePort)` overload in Phase 1 (NOT added here per the brief).
- The 20s driver loop, waitMessage(100) pump, sentinel print, and SPIKE| line protocol in
  `sources/Main.kt` are directly reusable as the TestMain driver skeleton.
- Spike edits to roku-test-app were reverted after the run (WIP snapshot commit a11b691
  "wip: pre-spike snapshot" is the restore point); these copies are the durable record.
