# Task-Node Spike Findings (2026-07-31)

Device: Roku at 192.168.1.125. Result: **10/10 checks passed** (`deploy.sh`, results in
`spike-results.txt`). These findings de-risk the typed-task M1 design; the hand-written
`SpikeTask.{xml,brs}` files are the reference for the M1 golden expectations.

| Check | Result | Implication for the design |
|---|---|---|
| field_name_legality | PASS | `kotlinTaskState/kotlinTaskError/kotlinTaskId` interface field names are legal and readable. |
| included_script_callback | PASS | `observeFieldScoped(field, "name")` resolves callbacks defined in a separate `<script>` include — the stdlib TaskRunner (top-level callback in an included file) works. |
| unparented_run_roundtrip | PASS | `CreateObject("roSGNode","SpikeTask")` without parenting → observe → `control=RUN` → completion event → outputs readable. Core rendezvous chain proven. |
| m_clone_in_visibility | PASS | Plain `m.<name>` values set in `init()` ARE visible in the task thread (clone-in). So un-annotated state is readable-as-a-copy in `run()`. |
| m_writeback_isolation | PASS | Task-thread writes to plain `m.<name>` do NOT propagate back to the owner thread. The dangerous direction for `BRS_TASK_STATE_NOT_FIELD` is write-back loss — checker message should say "writes from run() are silently lost", not "invisible". Also: `callFunc` works on Task nodes (owner-thread execution). |
| crash_to_error_state | PASS | `throw` in run body → catch wrapper → `kotlinTaskError` AA (message intact) then `kotlinTaskState="error"`. Completion protocol viable. |
| rapid_alwaysnotify_datum | PASS | **200/200 observer fires — zero coalescing** for rapid `alwaysNotify` int writes on this device/OS. M2 doorbell may over-deliver, never under-deliver here; still keep the array-drain design (coalescing remains allowed by SDK contract). |
| emission_array_drain | PASS | Final array field carried all 200 entries in order — M2 `kotlinTaskEmissions` + seq-doorbell drain is sound even under coalescing. |
| rerun_same_node | PASS | Re-`RUN` of a completed node executes again (`runCount=2`). Node pooling stays a viable later optimization. |
| concurrent_instances | PASS | Two simultaneous instances of the same task type complete independently with correct per-node outputs (id correlation works). |

Notes:
- BrightScript `and`/`or` do NOT short-circuit — guard AA member access with nested ifs
  (bit us during authoring; relevant to generated code too).
- The `__kotlinTaskMain` wrapper shape used here (try/catch, error AA written before
  state, state written last) is the shape the M1 compiler emission should produce.
