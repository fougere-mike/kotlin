# Component Coroutine Scaffolding — Design + Plan (2026-08-10)

Approved by Mike 2026-08-10 (AskUserQuestion: dual backend / `launch{}` on base /
FIR error on Dispatchers.IO / full package / self-scheduling queue; review note:
backend detection once per session, cached on the global node).

## Problem

Every SceneGraph component using coroutines hand-writes a pump: a Timer child +
`observeFieldScoped("fire", ...)` + a tick handler calling
`processCoroutineQueue()`/`processCoroutineDelays()` + a fragile per-component
"when to stop" heuristic. The pattern is copy-pasted 4× in roku-test-app
(ShelfView, RenderCoroutineProbe, TypedTaskProbe, FieldSemanticsProbe) and
institutionalized in stdlib KDoc (Dispatchers.kt:16-41, Delay.kt:20-49).

The dispatcher argument is also theater:

- `Dispatchers.Main === Dispatchers.Default` — literally the same singleton
  (Dispatchers.kt:57-82).
- `Dispatchers.IO` silently falls back to the same render-thread queue
  (IODispatcher.kt:72-98) because TaskPool is quarantined and never initialized.
- Only `Unconfined` behaves differently (runs inline).

The ONLY effect of `Dispatchers.Main` today is deferring the launch body to
`CoroutineQueue` — which is precisely what creates the pump requirement.

## Design

### 1. Self-scheduling pump (`PumpScheduler`, stdlib)

Key insight: every coroutine resumption already begins on the render thread in
the component's own script context (launch call sites, runTask field observers,
delay expiries via drain). So the queue can schedule its own wakeups:

- `CoroutineQueue.enqueue()` → `PumpScheduler.onEnqueue()` — auto-pumps every
  dispatch path (launch{}, legacy scopes, yield(), runTask resumption).
- `DelayTracker.register()` → `PumpScheduler.onDelayRegistered()` — arms a
  one-shot Timer to the NEXT deadline (replaces 10 ms polling).

Wakeup backends (no repeating timer, ever; nothing ticks when idle):

- **OS 15.0+**: `roRenderThreadQueue` — `PostMessage("kotlin.pump", ...)` from
  the render thread; registered `AddMessageHandler` drains the queue.
  (Doc: ../RokuDocs/roRenderThreadQueue.html; device-verified by
  spikes/render-thread-queue-spike.)
- **< OS 15**: one-shot Timer child (duration ~0.001, repeat=false) armed per
  wakeup.
- delay() needs the one-shot deadline Timer on BOTH backends (RTQ has no
  delayed post).

Backend is detected ONCE per app session and cached on the global node
(`__kotlinPumpBackend` field; IOWorkerRegistry precedent). Each scheduler
instance (one per GetGlobalAA scope — same identity domain as CoroutineQueue,
correct whether GetGlobalAA is per-component or per-thread) still registers its
own RTQ handler; only detection is session-global. `attach(top, global)` is
idempotent; unattached scheduler is fully inert (main-thread
runBlocking/runPumping unaffected).

Drain: clear flags → loop `processCoroutineQueue(); processCoroutineDelays()`
until both are 0 and `!hasCoroutineWork()` → re-arm only if work/delays remain.

Test hooks: `kotlinPumpForceTimerBackend(Boolean)` (pre-attach, writes the
global cache so forcing is app-wide) and `kotlinPumpBackendName(): String`.

### 2. User API: `launch {}` on ComponentBase (zero imports)

`libraries/stdlib/brs/src/kotlin/brs/ComponentCoroutines.kt` (package
`kotlin.brs` is default-imported on BRS):

- `fun ComponentBase.launch(context, block): Job` → `componentScope().launch(...)`
- `fun ComponentBase.componentScope(): CoroutineScope` — lazy, stored at
  `m.__kotlinComponentScope`, context = `Dispatchers.Main + Job()`; calls
  `PumpScheduler.attach(top, global)` (belt-and-braces with init injection).
- `@BrsInline` shims (`componentMOf`/`componentTopOf`/`componentGlobalOf`)
  bridge protected members — component `this` IS the m-scope AA at runtime;
  extension receivers compile as a first parameter literally named `m`
  (IrToBrsTransformer.kt:306-321), so this works with zero compiler changes.

### 3. Compiler init injection

`transformComponentInitBlock` emits `__kotlinPumpAttach(m.top, m.global)`
(after the task-functionName injection precedent, before property
initializers) for components whose FILE uses coroutines
(`currentFileUsesCoroutines`, a per-file IR pre-scan in `transformFile`; file
granularity matches script-include granularity so no include bloat). Task
components are skipped. The injected call `recordFunctionDependency`s so
PumpScheduler's script lands in the component XML automatically.

Known hole: coroutine use hidden entirely inside another file's helper escapes
the predicate — mitigated by the lazy attach in `launch{}`/`componentScope()`.

### 4. FIR error `BRS_IO_DISPATCHER_UNSUPPORTED`

Error on user references to `Dispatchers.IO` ("IO dispatch silently falls back
to the render-thread queue. Use runTask<T> for background work."). Reuses
`isDispatchersIO` from FirBrsIOWorkerCaptureChecker. Suppressible;
`WithContext.kt:71` + stdlib tests get `@Suppress`. BrsIOWorkerExtractionLowering
stays (it's the quarantined pipeline's impl, reachable only via explicit
suppression = the opt-in story).

### 5. Migration

Pump boilerplate deleted from ShelfView + 3 fixtures (one deliberate legacy
`CoroutineScope(Dispatchers.Main).launch` kept in RenderCoroutineProbe to
device-lock legacy auto-pump). New E2E coverage: delay one-shot timing, legacy
scope auto-pump, forced-Timer-backend, RTQ-backend-active. KDoc/CLAUDE.md
rewrites kill the manual-pump recipe; `processCoroutineQueue()`/
`processCoroutineDelays()` stay public for run-loop owners (runPumping,
runBlocking).

## Phases

0. Device spike `spikes/render-thread-queue-spike/` (RTQ semantics: creation
   threads, handler context, GetGlobalAA identity, channel identity, latency,
   cross-thread post, unparented-Timer firing, post-before-handler) — gates the
   RTQ backend. Device: Roku Ultra 4802CA, OS 15.2.4.
1. Stdlib PumpScheduler + hooks + ComponentCoroutines. Verify: rebuild.sh,
   stdlib device suite 409/409.
2. Compiler init injection + goldens (53 → 56).
3. FIR diagnostic + fixtures (214 → ~220).
4. roku-test-app migration + E2E additions (30 → ~34).
5. Docs (KDoc, CLAUDE.md, gate table), memory.

Full execution detail: ~/.claude/plans/ok-so-we-ve-made-effervescent-quasar.md
(session plan; the durable copy is this document plus the spike FINDINGS).
