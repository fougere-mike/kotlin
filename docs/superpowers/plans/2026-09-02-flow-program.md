# Flow Program Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking. This plan is SELF-CONTAINED for a fresh session: read the spec first (`docs/superpowers/plans/2026-09-02-flow-program-design.md` — its §1 decision table and §2 pinned facts GOVERN; Phase-0 spikes are DONE, `spikes/flow-spike/FINDINGS.md` is the device truth), then this plan. Each task cites its research appendix (`docs/superpowers/plans/2026-09-02-flow-program-research/*.md` — exact signatures + file:line anchors verified 2026-09-02); read the cited sections before coding.

**Goal:** Ship the flow family — cold `Flow<T>` + kotlinx-shaped operators (incl. flatMapConcat/Merge/Latest), `flowOn(Dispatchers.Task)` + `spawnTask {}` over per-call-site synthesized task components with real two-layer cancellation, and `MutableStateFlow`/`StateFlow` VM→View propagation over global-node doorbells — with the FIR rule family, E2E Suite 10, and the TestScreen flagship rewrite.

**Architecture:** Per the spec. Flow ships as a NEW user-mode klib `kotlin-flow-brs` (spec decision 10: stdlib compilation generates no suspend state machines — operators must compile as user code; kotlin-test-brs is the proven second-klib precedent). Phase 1: klib infra + cold core (runBlocking regime, no device components). Phase 2: the task lift (compiler synthesis + FIR family + runtime + Suite 10a). Phase 3: hot tier (doorbell StateFlow + Suite 10b + flagship + docs).

**Tech Stack:** new `libraries/flow/brs` klib (+ checked-in prebuilt), BRS stdlib additions, IR backend lowering (`compiler/ir/backend.brightscript`), FIR checkers (`checkers.brs`), kotlin-roku KGP wiring, roku-test-app E2E (Suite 10).

## Global Constraints

- **Build:** `./rebuild.sh` only. Documented exceptions: the two `generateCheckersComponents` regen commands (CLAUDE.md "Regenerating FIR diagnostic containers") and the test-run commands in CLAUDE.md "Running Tests".
- **ENVIRONMENT PROCEDURE (MANDATORY, this machine kills background wakeups):** prefix every long command with `caffeinate -ims`; NEVER end a turn while a build/device run is in flight — background it to a scratchpad log, then stay in-turn with bounded foreground polls (`sleep 240; tail -5 <log>`, each Bash call under 10 minutes) until the completion marker (`=== Build complete ===` / `EXIT=0` / `All tests passed!` / results marker). Controllers: treat idle-without-report during a known run as a lost wakeup — check the log mtime/tail, then nudge.
- **Device:** credentials auto-resolve from `../roku-test-app/local.properties`. Console preflight before EVERY device run: `echo | nc -w 3 $(grep roku.deviceIp ../roku-test-app/local.properties | cut -d= -f2) 8085` → `Console connection is already in use` = STOP and ask Mike. Single-client console; no parallel device runs.
- **Gate baselines at start (must not drop; all grow):** goldens **79** · FIR **227** · stdlib device **524/53** · E2E **86 active / 9 suites** (+3 xtests) · `validateComponentIncludes` + `validateTestComponentIncludes` strict **0/0**. Record actuals per task in the ledger. NEW gate this program adds: the flow-klib device tests ride the stdlib device suite runner (same 524/53 counter — it grows).
- **Commits:** per-task on `feature/brightscript-backend-2.2.20`; prefixes `stdlib:` / `flow:` (new, for `libraries/flow/`) / `brs:` / `test-infra:` / `docs:` (rta uses `app:`/`test-infra:`); every stdlib SOURCE change is followed by a separate `brs-prebuilt: regenerate stdlib klib (<reason>)` commit; every flow-klib SOURCE change by `flow-prebuilt: regenerate flow klib (<reason>)` once the prebuilt exists (Task 1). All messages end `Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>`. roku-test-app and kotlin-roku are SEPARATE repos — commit separately, EXPLICIT paths only (the pre-existing rta `components/ShelfView/ShelfView.kt` +1-line edit is Mike's; never commit it).
- **Golden discipline:** every golden diff reviewed line-by-line before committing; unexpected churn in untargeted goldens = stop and investigate. Broken-shape evidence goldens are committed BEFORE fixes when pinning a defect.
- **Spec bindings (verbatim):** operator/API signatures = spec §14 with two recorded corrections — `Dispatchers` really lives at `kotlin.coroutines.dispatchers` (decision 10) and flow/spawnTask entry points live in the flow klib under packages `kotlin.coroutines.flow` / `kotlin.coroutines.task` via `-Xallow-kotlin-package`. flowOn emissions + spawnTask returns + lifted captures are marshallable-only (FIR family, spec §7). Task cancellation = cancel-field-then-STOP, runTask rider included (spec §5 + FINDINGS Probe B). Doorbell = `__kotlinFlow_<uuid>` int field on the GLOBAL node, `alwaysNotify=true` (spec §6 + FINDINGS Probe A). `catch {}` must exclude `CancellationException` explicitly (it extends IllegalStateException on this platform). `collect` on StateFlow never completes normally. Equality dedup uses `==`.
- **Recorded implementation deviations (semantics identical unless noted, surfaced per house rule):** (a) lifted captures cross as ONE `flowCaptures` AA field on a stdlib `FlowTaskComponent` base (deep copy — same law), not per-capture typed fields as spec §5 sketched; the lift synthesizes only a concrete `run()`; node refs inside AAs cross live (channel-matrix pinned). (b) The cooperative cancel field is named `flowCancel` (spec §5 wrote `__kotlinCancelRequested`). (c) The runTask STOP rider is STOP-only — plain TaskComponents have no cancel field, so the cooperative-first half of the sequence applies to flow-lifted tasks only, and `ensureTaskActive()` is a no-op inside plain `runTask` bodies (narrowing of spec §5's "adopts the same sequence"; cooperative checkpoints for plain typed tasks are backlog). (d) `List<T>.asFlow()` is subsumed by `Iterable<T>.asFlow()` (spec §14 governs over the §3 sketch). (e) NEW compiler piece not in the spec (adversarial-review finding): a `BrsFlowAccessLowering` call-site rewrite routes `StateFlow`/`MutableStateFlow` `value` accessor calls and interface-typed `Flow.collect(collector)` member calls to static flow-klib functions — REQUIRED because interface-receiver member/accessor calls are fn-slot dispatch, which (i) records no include-closure file dependency and (ii) crosses the SetRef graph on the disclaimed fn-ref path for shared-VM-held flows, both of which the SharedService program's laws forbid. Task 10 owns it; goldens pin it. Companion law (KDoc'd, Task 2): cold `Flow` OBJECTS must never cross a component boundary (their lambdas strip; only StateFlow, whose access is statically routed, is cross-component).
- **Immaculate diagnostics:** every FIR message and runtime error names the fix. `@Suppress` escapes get tracking comments. New FIR diagnostics touch FOUR files + the test harness map (appendix `compiler-lowerings-fir.md` §7, gotcha 8).
- **No parallel implementers** (shared build dirs + single device console).
- **Stdlib no-state-machine law (decision 10):** any NEW suspend function in `libraries/stdlib/brs` must be tail-delegating or single-park (`ParkedContinuation` + `finish()` last); kotlinx-style multi-suspend bodies go in the flow klib ONLY. Appendix `coroutines-runtime.md` gotchas 1–4 are binding.

## File Map (who owns what)

| File | Task | Action |
|---|---|---|
| `libraries/flow/brs/` module (build.gradle.kts, src tree) + `libraries/flow/brs-prebuilt/` (checked-in klib + regenerateKlib) + `settings.gradle` + `rebuild.sh` + stdlib-test-runner `-libraries` + golden/FIR harness classpaths | 1 | Create/Modify |
| `libraries/flow/brs/src/kotlin/coroutines/flow/Flow.kt`, `Builders.kt`, `Terminals.kt`, `Errors.kt` | 2 | Create |
| `.../flow/Operators.kt` (simple intermediates) | 3 | Create |
| `.../flow/FlowChannel.kt`, `ConcurrentOperators.kt` (combine/conflate/flatMap*/­*Latest) | 4 | Create |
| `libraries/stdlib/brs/src/kotlin/coroutines/dispatchers/Dispatchers.kt` (+`TaskTokenDispatcher`) · FIR `FirBrsTaskDispatcherChecker` + IO-message edit + diagnostics list + regens + fixtures | 5 | Modify/Create |
| FIR lifted-region family: `FirBrsFlowLiftCheckers.kt` (+ list entries, regens, messages, harness map, fixtures `flowLift*.kt`) | 6 | Create/Modify |
| `libraries/stdlib/brs/src/kotlin/brs/SceneComponent.kt` (`FlowTaskComponent` base) · `compiler/.../lower/BrsFlowTaskLiftLowering.kt` · `BrsStandardClassIds.kt` + `BrsSymbols.kt` ids · `BrsIrBackendContext.kt` (`synthesizedComponents`) · `BrsCompiler.kt` (post-lowering extraction merge) · goldens `flow/flowOnLift.kt`, `flow/spawnTaskLift.kt` | 7 | Create/Modify |
| `libraries/flow/brs/src/kotlin/coroutines/flow/TaskFlow.kt` (collector half + task driver) + `.../task/SpawnTask.kt` + `ensureTaskActive` · stdlib `TaskRunner.kt` cancellation rider (STOP) + `PumpScheduler.kt` public ambient accessors · rta Suite 4 test update | 8 | Create/Modify |
| kotlin-roku `RokuPlugin.kt`/`RokuExtension.kt` (flow-klib default dependency + packaging + validate index) · rta fixtures `components/fixtures/FlowProbe.kt` + `src/brsTest/kotlin/tests/FlowTests.kt` (Suite 10a) + `TestMain.kt` | 9 | Create/Modify |
| flow klib `StateFlow.kt` + `Doorbells.kt` + `StateIn.kt` · compiler `lower/BrsFlowAccessLowering.kt` + golden `flow/stateFlowAccess.kt` | 10 | Create/Modify |
| rta `FlowTests.kt` Suite 10b (StateFlow device tests) + fixtures `FlowVmFixture.kt`, `FlowCollectorProbe.kt` | 11 | Create/Extend |
| rta `components/TestScreen/TestScreenVM.kt` + `TestScreen.kt` + new `TestScreenChildLabel` child collector (flagship rewrite, spec-§6 acceptance) | 12 | Modify/Create |
| CLAUDE.md (Flow section, runTask rider, quarantine message, gate table) + backlog + spec status | 13 | Modify |

Ordering: 1→2→3→4 (Phase 1), 5→6→7→8→9 (Phase 2), 10→11→12→13 (Phase 3). No task starts before its predecessor's review closes.

---

# Phase 1 — kotlin-flow-brs klib + cold core

### Task 1: The kotlin-flow-brs module + toolchain wiring

**Files:**
- Create: `libraries/flow/brs/build.gradle.kts`, `libraries/flow/brs/src/kotlin/coroutines/flow/Flow.kt` (placeholder: the two interfaces only), `libraries/flow/brs-prebuilt/build.gradle.kts` (+ checked-in `kotlin-flow-brs.klib` + `.klib-source-hash`)
- Modify: `settings.gradle` (register `:kotlin-flow-brs`, `:kotlin-flow-brs-prebuilt` — stdlib precedent at :749/:775), `rebuild.sh` (flow regenerateKlib step after stdlib's; publish step), `libraries/stdlib/brs/test/run-tests.sh` (add flow klib to `-libraries` + package its .brs), `libraries/stdlib/brs/test/build.gradle.kts` (compile dep), `compiler/ir/backend.brightscript/build.gradle.kts` + `AbstractBrsGoldenFileTest.kt` (flow prebuilt on golden classpath), `compiler/fir/checkers/checkers.brs` test harness `AbstractBrsDiagnosticTest.kt` (flow prebuilt on fixture classpath)

**Interfaces:**
- Consumes: `libraries/stdlib/brs-prebuilt/build.gradle.kts` (the regenerateKlib template: fat-JAR classpath, `-Xallow-kotlin-package -Xstdlib-compilation` — the flow variant DROPS `-Xstdlib-compilation`, that is the whole point); `libraries/kotlin.test/brs` build as the second-klib compile/publish precedent — INCLUDING its `generateBrsRuntime` + `brsRuntimeJar` tasks and the `com.nuvyyo:kotlin-test-brs-runtime` publication (kotlin.test/brs/build.gradle.kts:105-116; KGP stages runtime .brs from that artifact at RokuPlugin.kt:131/145 — the flow klib needs the SAME runtime-jar publication or Task 9 has nothing to stage); appendix `test-infrastructure.md` §1 (run-tests.sh mechanics, `-libraries stdlib.klib:kotlin-test.klib`), appendix `coroutines-runtime.md` §7 — CAUTION: §7's directory guidance ("create it inside the stdlib") predates spec decision 10 and is SUPERSEDED; consume only its regenerateKlib/generateStdlibBrs mechanics and the prebuilt + `.klib-source-hash` convention.
- Produces: module `:kotlin-flow-brs` publishing BOTH `com.nuvyyo:kotlin-flow-brs:2.2.20-brs.1` (klib) AND `com.nuvyyo:kotlin-flow-brs-runtime:2.2.20-brs.1` (the source→.brs runtime JAR, kotlin-test-brs-runtime pattern) to Maven Local; checked-in prebuilt at `libraries/flow/brs-prebuilt/kotlin-flow-brs.klib`; compile flags `-Xallow-kotlin-package` (NO `-Xstdlib-compilation`), `-libraries <stdlib prebuilt>`; placeholder source:

```kotlin
package kotlin.coroutines.flow

public interface Flow<out T> {
    public suspend fun collect(collector: FlowCollector<T>)
}

public fun interface FlowCollector<in T> {
    public suspend fun emit(value: T)
}
```

- [ ] **Step 1:** Read the three precedents (stdlib brs-prebuilt build, kotlin.test/brs build, run-tests.sh lines around the `-libraries` assembly) before writing anything. Mirror, don't invent.
- [ ] **Step 2:** Create the module + prebuilt with the placeholder source; wire settings.gradle, rebuild.sh (regen + publish), run-tests.sh (`-libraries` + packaging of the flow klib's generated .brs alongside kotlin-test's — copy exactly how kotlin-test's outputs are staged), both test-harness classpaths.
- [ ] **Step 3:** Verification test (red-first where possible): a throwaway stdlib-suite test file importing `kotlin.coroutines.flow.Flow` compiles under `./run-stdlib-tests.sh --build-only` — FAILS before the wiring, passes after. Then `caffeinate -ims ./rebuild.sh` end-to-end green (new steps included), `./run-compiler-tests.sh` (79 unchanged), FIR suite green (227 unchanged).
- [ ] **Step 4:** Suspend-state-machine PROOF (the decision-10 acceptance): add a flow-klib function `public suspend fun __smokeTwoSuspends(a: suspend () -> Int, b: suspend () -> Int): Int = a() + b()` — PUBLIC, not internal: the stdlib test module is a SEPARATE module with no friend wiring, so internal flow-klib declarations are invisible to it (this visibility law applies to every flow-klib symbol any test touches directly). Add a stdlib-suite test running it under `runBlocking` with two `delay(1)`-ing lambdas — this exact shape MISCOMPILES in stdlib mode; passing on device proves user-mode compilation. Keep the test, rename `flowKlibStateMachineSmoke`.
- [ ] **Step 5:** Commits: `flow: kotlin-flow-brs module + prebuilt + toolchain wiring (user-mode compilation)` (+ `flow-prebuilt:` klib commit). Device run for the smoke test may fold into Task 2's run if reviewer agrees.

### Task 2: Cold core — types, builders, terminals

**Files:**
- Create: `libraries/flow/brs/src/kotlin/coroutines/flow/Builders.kt`, `Terminals.kt`, `Errors.kt` (extend `Flow.kt` with `emitAll`)
- Test: `libraries/stdlib/brs/test/kotlin/coroutines/flow/FlowCoreTest.kt` (+ TestMain.kt import & call — BOTH, or the suite silently doesn't run)

**Interfaces:**
- Consumes: spec §3/§4 signatures; public stdlib coroutine API only (`runBlocking`, `launch`, `Job`, `ensureActive`, `suspendCoroutine`, `CancellationException`); appendix `coroutines-runtime.md` §2–4.
- Produces (spec §14 verbatim):

```kotlin
public suspend fun <T> FlowCollector<T>.emitAll(flow: Flow<T>)
public fun <T> flow(block: suspend FlowCollector<T>.() -> Unit): Flow<T>
public fun <T> flowOf(vararg values: T): Flow<T>
public fun <T> Iterable<T>.asFlow(): Flow<T>
public suspend fun <T> Flow<T>.collect(action: suspend (T) -> Unit)
public suspend fun <T> Flow<T>.first(): T
public suspend fun <T> Flow<T>.first(predicate: suspend (T) -> Boolean): T
public suspend fun <T> Flow<T>.firstOrNull(): T?
public suspend fun <T> Flow<T>.toList(): List<T>
public fun <T> Flow<T>.launchIn(scope: CoroutineScope): Job
internal class AbortFlowException : CancellationException("flow terminal aborted")   // Errors.kt; first/take use it
```

Contracts to encode in code + tests: cold body re-executes per collect; `emit` entry-checks via `coroutineContext.ensureActive()` (user-mode code — the intrinsic IS available here, unlike stdlib); `first()` aborts upstream via `AbortFlowException` caught by its own collector (never leaks to the caller); `flowOf` uses a defensive `values.toList()` ONCE at construction (primitive-varargs defect watch — see step 2).

- [ ] **Step 1:** Write the device tests FIRST (red list, runBlocking regime — `test("...") { runBlocking { ... } }`, import `kotlin.coroutines.builders.runBlocking`): flow re-executes per collect; emit values in order; `flowOf(1,2,3).toList() == [1,2,3]` AND `flowOf<Int>().toList().isEmpty()` (empty + primitive vararg — known-defect probes, spec §12); `first()` stops the producer (side-effect counter stops at 1); `first{pred}`/`firstOrNull` empty-flow paths (`first()` on empty throws `NoSuchElementException`); `launchIn` collects in the scope; cancellation mid-collect runs `try/finally` in the flow body. `./run-stdlib-tests.sh --build-only` must FAIL (unresolved refs) before implementation.
- [ ] **Step 2:** Implement. If the primitive-vararg golden probe MISCOMPILES (known open defect class), STOP feature work, pin the defect with a red golden in `compiler/testData/codegen/brs/flow/`, fix the compiler defect, then proceed (ScopeHandle precedent: latent defects fixed in-phase).
- [ ] **Step 3:** `caffeinate -ims ./rebuild.sh` → `./run-stdlib-tests.sh` (device; suite count grows, record) → `./run-compiler-tests.sh` (79 + any defect-pin goldens).
- [ ] **Step 4:** Commits: `flow: cold core — Flow/FlowCollector, builders, terminals` + `flow-prebuilt:` regen (+ any `brs:` defect-fix commits, each with its own golden).

### Task 3: Simple intermediate operators

**Files:**
- Create: `libraries/flow/brs/src/kotlin/coroutines/flow/Operators.kt`
- Test: extend `FlowCoreTest.kt` or new `FlowOperatorsTest.kt` (+ TestMain registration)

**Interfaces:**
- Produces (spec §14 verbatim): `map`, `mapLatest` (Task 4 delivers `transformLatest`; declare `mapLatest` there if preferred — keep ONE home, note in ledger), `filter`, `filterNotNull`, `transform`, `onEach`, `onStart`, `onCompletion`, `catch`, `distinctUntilChanged`, `take`, `drop`. All non-inline regular extensions built over `flow { }` + `collect` (kotlinx-style — legal here, user-mode).
- Key semantics (tests pin each): `catch {}` sees UPSTREAM exceptions only, may emit fallbacks, and RETHROWS `CancellationException` unconditionally (CE extends IllegalStateException on this platform — an explicit `if (e is CancellationException) throw e` guard, spec Global-Constraints binding); `onCompletion(cause)` fires with null / failure / CE (the Rx doFinally parity matrix, spec decision 9); downstream (collector-block) exceptions propagate uncaught through `collect`; `take(n)` aborts upstream via `AbortFlowException` after n emissions; `distinctUntilChanged` uses `==`.

- [ ] **Step 1:** Red-first device tests: per-operator happy path + the semantics list above, incl. the full `onCompletion` cause matrix (normal/failure/cancellation) and a `catch`-must-not-swallow-CE test (cancel a collector inside `catch`-wrapped flow; assert the coroutine actually cancels).
- [ ] **Step 2:** Implement; `--build-only` red→green first, then `caffeinate -ims ./rebuild.sh` + full stdlib device run + goldens-unchanged sweep.
- [ ] **Step 3:** Commits: `flow: simple intermediate operators (map..distinctUntilChanged)` + `flow-prebuilt:` regen.

### Task 4: FlowChannel + concurrent operators (the flatMap family)

**Files:**
- Create: `libraries/flow/brs/src/kotlin/coroutines/flow/FlowChannel.kt`, `ConcurrentOperators.kt`
- Test: `FlowConcurrentTest.kt` (+ TestMain registration)

**Interfaces:**
- Produces:

```kotlin
// FlowChannel.kt — INTERNAL same-context queue (not public API; spec §4)
internal class FlowChannel<T> {
    fun send(value: T)                        // non-suspend, unbounded
    fun close(cause: Throwable? = null)
    suspend fun receiveOrClosed(): Any?       // next value, or the CLOSED marker
    val closeCause: Throwable?
    companion object { val CLOSED: Any }      // sentinel object
}
// ConcurrentOperators.kt (spec §14 verbatim)
public fun <T1, T2, R> combine(f1: Flow<T1>, f2: Flow<T2>, transform: suspend (T1, T2) -> R): Flow<R>
public fun <T> Flow<T>.conflate(): Flow<T>
public fun <T, R> Flow<T>.flatMapConcat(transform: suspend (T) -> Flow<R>): Flow<R>
public fun <T, R> Flow<T>.flatMapMerge(concurrency: Int = 16, transform: suspend (T) -> Flow<R>): Flow<R>
public fun <T, R> Flow<T>.flatMapLatest(transform: suspend (T) -> Flow<R>): Flow<R>
public fun <T, R> Flow<T>.transformLatest(block: suspend FlowCollector<R>.(T) -> Unit): Flow<R>
public fun <T, R> Flow<T>.mapLatest(transform: suspend (T) -> R): Flow<R>
public suspend fun <T> Flow<T>.collectLatest(action: suspend (T) -> Unit)
```

- Consumes: `coroutineScope`, `launch` (import `kotlin.coroutines.builders.launch` EXPLICITLY in any test fixture that nests it — appendix `test-infrastructure.md` gotcha 5), `Job.cancel`, `suspendCoroutine` parks. Mutable state in class holders, not captured vars, even in the flow klib (defensive convention; the stdlib law is appendix gotcha 4).
- Shapes: `flatMapConcat = transform { emitAll(transform(it)) }`. Merge/latest/combine: upstream(s) collected in `coroutineScope` children feeding one `FlowChannel`; downstream loop drains `receiveOrClosed()`. `transformLatest`: new upstream value → `previousChild.cancel()` + `join` → start new child. `collectLatest(action) = transformLatest<T, Unit> { action(it) }.collect { }` (there is NO no-arg `collect()` in the surface). Single-context interleaving note (spec §4): non-suspending inners run to completion synchronously — document in KDoc, pin ONE test asserting the degenerate-concat ordering for non-suspending inners (truth, not a bug). `FlowChannel` is INTERNAL and stays internal: it is tested exclusively THROUGH the public operators (flatMapMerge/latest/combine/conflate pin FIFO, close-with-cause, and drain semantics) — no direct FlowChannel tests from the separate test module (visibility law, Task 1 step 4).

- [ ] **Step 1:** Red-first device tests (through PUBLIC operators only): flatMapConcat ordering; flatMapMerge interleaves suspending inners (use `delay(1)` inners) and respects concurrency=1 == concat; flatMapLatest cancels the in-flight inner (inner's `finally` runs, late emissions dropped) — the Rx switchMap contract; combine emits on each side's update after both have values; conflate under a burst delivers latest (pins FlowChannel drain + close-with-cause via the operator surface); collectLatest restarts the action.
- [ ] **Step 2:** Implement; `--build-only` → `caffeinate -ims ./rebuild.sh` → full stdlib device run (record counts) → goldens sweep.
- [ ] **Step 3:** The spec-§9 operator-chain golden: `compiler/testData/codegen/brs/flow/operatorChain.kt` — a component collecting a representative chain (`flowOf(...).map{}.filter{}.flatMapLatest{}.onCompletion{}`) in `launch {}`; pins user-side suspend-chain codegen against the flow prebuilt (Task 1 put it on the golden classpath; regen the prebuilt BEFORE `--update` so the golden compiles against this task's operators). Red-first, then `--update`, line-by-line review. **Phase 1 exit gate:** stdlib device suite green with all flow suites in; goldens 79+1 green; rebuild green.
- [ ] **Step 4:** Commits: `flow: FlowChannel + concurrent operators (flatMap family, combine, conflate, *Latest)` + `flow-prebuilt:` regen; `brs: operator-chain golden (flow/operatorChain)`.

---

# Phase 2 — the task lift

### Task 5: Dispatchers.Task token + dispatcher-position FIR

**Files:**
- Modify: `libraries/stdlib/brs/src/kotlin/coroutines/dispatchers/Dispatchers.kt`; `FirBrsDiagnosticsList.kt` (new `FLOW_TASK_LIFT` group: `BRS_FLOW_ON_INVALID_DISPATCHER`); `FirBrsErrorsDefaultMessages.kt` (new message + EDIT `BRS_IO_DISPATCHER_UNSUPPORTED` message at ~L98–103 → "…Use flowOn(Dispatchers.Task) for background streams, spawnTask for one-shot blocks, or runTask<T> for typed tasks."); `AbstractBrsDiagnosticTest.kt` `diagnosticRenderedMessages` map; `BrsExpressionCheckers.kt`
- Create: `checkers.brs/src/.../expression/FirBrsTaskDispatcherChecker.kt`; fixtures `compiler/testData/diagnostics/testsWithBrsStdLib/flowTaskDispatcher.kt`; test methods in `BrsDiagnosticTests.kt`
- Test: FIR suite; existing `ioDispatcherUnsupported` fixture updated for the new message

**Interfaces:**
- Produces (stdlib):

```kotlin
// Dispatchers.kt (package kotlin.coroutines.dispatchers) — after val IO:
/** Compile-time token selecting the task-thread lift for flowOn. NOT a runtime
 * dispatcher: the only legal position is a flowOn argument (FIR-enforced);
 * dispatch() throws a guided error as the runtime backstop. Spec decision 4/10. */
public val Task: CoroutineDispatcher = TaskTokenDispatcher
internal object TaskTokenDispatcher : CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable): Unit =
        throw IllegalStateException("Dispatchers.Task is a compile-time flowOn token, not a runtime dispatcher — use flowOn(Dispatchers.Task), spawnTask {}, or runTask<T>")
}
```

- FIR rule (`BRS_FLOW_ON_INVALID_DISPATCHER`, ERROR, suppressible): fires (a) on a `flowOn` call whose argument is not the literal `Dispatchers.Task` property access, and (b) on any `Dispatchers.Task` property access NOT in a flowOn-argument position. Checker: `FirPropertyAccessExpressionChecker` for (b) using `FirBrsIODispatcherChecker`'s ClassId (`kotlin.coroutines.dispatchers.Dispatchers`, member `Task`) + enclosing-call test via `context.callsOrAssignments`; `FirFunctionCallChecker` for (a) matching `Callables.flowOn` (new CallableId in `BrsStandardClassIds.Callables`: `CallableId(FqName("kotlin.coroutines.flow"), Name.identifier("flowOn"))`). Appendix `compiler-lowerings-fir.md` §7 templates.
- NOTE: `flowOn` and `spawnTask` the FUNCTIONS arrive for real in Tasks 7/8; the CallableIds + fixtures compile against the flow prebuilt (Task 1 wired the harness classpath). Add BOTH public stubs in the flow klib NOW — `public fun <T> Flow<T>.flowOn(context: CoroutineContext): Flow<T>` (Flow.kt) and `public suspend fun <R> spawnTask(block: () -> R): R` (new `task/SpawnTask.kt`, package `kotlin.coroutines.task`) — bodies: guided-ISE backstop ("compiled without the lift — this call must be compiler-lowered; check the argument is the literal Dispatchers.Task / a literal block"), KDoc "compiler-lowered"; regen prebuilt. Task 6's fixtures and Task 7's goldens depend on these stubs resolving.

- [ ] **Step 1:** Red-first: fixtures with `<!BRS_FLOW_ON_INVALID_DISPATCHER!>` marks — flowOn with `Dispatchers.IO` (also gets IO error), flowOn with a variable holding Task, bare `launch(Dispatchers.Task)`, `withContext(Dispatchers.Task)`, plus CLEAN `flowOn(Dispatchers.Task)`. Update the IO-message fixture expectations.
- [ ] **Step 2:** Diagnostics-list entry → BOTH regen commands → message + harness-map entries → checker → registration. FIR suite green (227 + new, record). rebuild step-7 (kotlin-test compile) green.
- [ ] **Step 3:** Commits: `stdlib: Dispatchers.Task compile-time token (guided-throw backstop)` + `brs-prebuilt:` regen; `flow: flowOn public stub (lift target)` + `flow-prebuilt:` regen; `brs: BRS_FLOW_ON_INVALID_DISPATCHER + IO message update (+ regenerated containers)`.

### Task 6: FIR lifted-region family

**Files:**
- Create: `checkers.brs/src/.../expression/FirBrsFlowLiftCheckers.kt` (one file, the four rules); fixtures `flowLiftLiteral.kt`, `flowLiftCaptures.kt`, `flowLiftEmitTypes.kt`, `flowLiftSuspend.kt`
- Modify: `FirBrsDiagnosticsList.kt` (+`BRS_FLOW_UPSTREAM_NOT_LITERAL` error, `BRS_TASK_CAPTURE_UNMARSHALLABLE` error, `BRS_TASK_EMIT_NOT_MARSHALLABLE` error, `BRS_TASK_SUSPEND_IN_LIFTED` error, `BRS_TASK_CAPTURE_MUTATION_LOST` warning), regens, messages (+harness map), `BrsExpressionCheckers.kt`, `BrsStandardClassIds.kt` (`Callables.spawnTask` = `CallableId(FqName("kotlin.coroutines.task"), "spawnTask")`; flow-builder/operator CallableIds needed to walk chains)

**Interfaces:**
- Consumes: `FirBrsScopeBlockChecker` (literal-lambda template), `FirBrsScopeCaptureChecker` (free-value walk incl. implicit `this` — appendix `compiler-lowerings-fir.md` §7), `BrsScopeMarshallability.isMarshallable(type, session)` (the oracle — reuse, do not fork).
- Semantics (spec §5/§7 verbatim): the LIFTED REGION of a `flowOn(Dispatchers.Task)` call = the receiver expression subtree (must be a literal chain: each receiver hop is a flow-builder/operator call whose lambdas are literal `FirAnonymousFunctionExpression`s, bottoming out at `flow {}`/`flowOf`/`asFlow` — a `Flow`-typed variable/parameter receiver = `BRS_FLOW_UPSTREAM_NOT_LITERAL`, message names the fix: "declare the flow chain literally at the flowOn call site"); of a `spawnTask {}` call = the literal block (non-literal = same diagnostic, spawnTask wording). Within the region's lambdas: captures must be marshallable and NOT class instances/function values/implicit `this` (`BRS_TASK_CAPTURE_UNMARSHALLABLE`, message: "hoist the value to a local first: val x = <expr>"); captured-var assignment = `BRS_TASK_CAPTURE_MUTATION_LOST` warning; suspend calls other than `emit`/`emitAll` = `BRS_TASK_SUSPEND_IN_LIFTED`; `emit`/`emitAll` argument types + `spawnTask`'s return type must satisfy the marshallability oracle (`BRS_TASK_EMIT_NOT_MARSHALLABLE`). Disclosed holes stay disclosed (spec §7): `Any`-erased values, unresolvable function refs, generic `T` emissions.

- [ ] **Step 1:** Red-first fixtures: positive CLEAN cases (AA emissions, hoisted locals, blocking non-suspend calls) + each rule's negatives, incl. implicit-`this` capture via instance-property read in a class-declared chain, a `delay(100)` upstream (suspend-in-lifted), a data-class emission (unmarshallable), a `Flow` parameter flowOn'd (not-literal) — PLUS the spec-§9 disclosed-hole NON-firing cases, clean-marked: a value pre-erased to `Any`/`Dynamic` captured and emitted; a suspend function REFERENCE passed to an upstream operator where the callee isn't resolvable; a generic `T`-typed emission. Each asserts NO diagnostic fires (the holes stay disclosed, not accidentally closed).
- [ ] **Step 2:** List entries → regens → messages + harness map (single-quote templates; NO bare braces — "spawnTask" without `{}` in message text, MessageFormat law) → checkers → registration. FIR suite green (record). rebuild step-7 green.
- [ ] **Step 3:** Commits: `brs: FIR lifted-region family (literal upstream, captures, emissions, suspend) + fixtures (+ regenerated containers)`.

### Task 7: The lift lowering + component synthesis + goldens

**Files:**
- Create: `compiler/ir/backend.brightscript/src/.../lower/BrsFlowTaskLiftLowering.kt`; goldens `compiler/testData/codegen/brs/flow/flowOnLift.kt` + `.brs.txt`, `flow/spawnTaskLift.kt` + `.brs.txt`; golden test methods
- Modify: `libraries/stdlib/brs/src/kotlin/brs/SceneComponent.kt` (add `FlowTaskComponent`); `BrsStandardClassIds.kt` (Components.FlowTaskComponent, Callables.taskFlowLifted/spawnTaskLifted); `BrsSymbols.kt` (`flowTaskComponentClass`, `taskFlowLiftedOrNull`, `spawnTaskLiftedOrNull` — `by lazy` + `findOptionalFunction`, OrNull-bail pattern); `BrsIrBackendContext.kt` (`val synthesizedComponents: MutableMap<String, BrsComponentInfo>`); `BrsCompiler.kt` (merge `synthesizedComponents` into the Pass-3 map — post-lowering, before L222 loop); `BrsLoweringPhases.kt` (register at 0.058, after `BrsScopeRunBlockLowering` L165, BEFORE `UpgradeCallableReferences` L182)

**Interfaces:**
- Consumes: `BrsScopeRunBlockLowering` (capture collection `collectCaptures`, lifted-function build, AA construction — appendix `compiler-lowerings-fir.md` §1, reuse/extract its helpers rather than copying); `BrsRunTaskCallLowering` (call-site suspend-for-suspend rewrite template); extraction/merge mechanics appendix §5–6 + `task-machinery.md` §3 (the VERDICT block — synthetic IrFile named EXACTLY after the component; append IrClass to a NEW synthetic IrFile, not the originating file, so `writeOutput` routes the .brs to `components/<Name>/`).
- Produces (stdlib base, Task 7 owns it):

```kotlin
// SceneComponent.kt — after TaskComponent. Extends TaskComponent DELIBERATELY
// (inherits the kotlinTask* protocol fields AND all isTaskComponent machinery:
// __kotlinTaskMain wrapper, functionName init wiring, pump-attach exclusion).
// The DIRECT @BrsSceneGraphComponent annotation is LOAD-BEARING twice:
// (1) isComponentBaseDeclaration (BrsIntrinsics.kt:274 — abstract + DIRECTLY
//     annotated) is the only thing that makes transformSceneGraphComponent
//     (IrToBrsTransformer.kt:669) skip base codegen; unannotated, stdlib
//     compilation emits a sub init() for this class into the SHARED
//     SceneComponentKt.brs — a device-wide duplicate-init collision.
// (2) getExtendsComponent (BrsComponentExtractor.kt:189) resolves the
//     synthesized leaf's XML extends= from the direct superclass's annotation;
//     unannotated it emits extends="FlowTaskComponent", a subtype no package
//     defines — node creation fails.
/** Base of compiler-synthesized flowOn/spawnTask task components (spec §5).
 * Captures cross as ONE deep-copied AA; envelopes stream over flowOut
 * (alwaysNotify — every write must deliver); flowCancel is the cooperative
 * cancellation signal (FINDINGS Probe B6). Never subclass by hand. */
@BrsSceneGraphComponent(extends = "Task")
public abstract class FlowTaskComponent : TaskComponent() {
    @SGAssocArrayField public var flowCaptures: RoAssociativeArray? = null
    @SGAssocArrayField(alwaysNotify = true) public var flowOut: RoAssociativeArray? = null
    @SGBooleanField public var flowCancel: Boolean = false
}
```

(FIELD-INFO WARNING — do NOT trust the extractor's inherited walk for the
synthesized leaf: `collectInheritedDeclarations(stopAtUserComponents = true)`
stops at ANY `isComponent`-true ancestor, and FlowTaskComponent IS one (its
supertype walk hits the annotated TaskComponent) — the walk would collect ZERO
of the six fields and every protocol write becomes an undeclared-field silent
no-op. Instead the lowering HAND-CONSTRUCTS the six `BrsFieldInfo` entries
(kotlinTaskState string alwaysNotify / kotlinTaskError assocarray /
kotlinTaskId integer / flowCaptures assocarray / flowOut assocarray
alwaysNotify / flowCancel boolean — `BrsFieldInfo` needs only
name/type/alwaysNotify, `BrsComponentInfo.kt:178-224`) when registering the
leaf's `BrsComponentInfo`. The golden pins the six-field XML. The leaf
declares only `run()`.)
- Lowering behavior: match `flowOn(Dispatchers.Task)` full-chain call / `spawnTask {}` call in user files (OrNull-bail on stdlib compilation) → collect captures across ALL lambdas in the region (scope-lowering collector, extended to walk the chain) → lift: flowOn → `private fun __flowUpstream_<san>_<n>(captures: RoAssociativeArray?): Flow<T>` (non-suspend factory rebuilding the chain with capture locals); spawnTask → `private fun __spawnBlock_<san>_<n>(captures: RoAssociativeArray?): R` → synthesize `class KotlinFlowTask_<san>_<n> : FlowTaskComponent()` — LETTER-FIRST name (underscore-leading SceneGraph subtype names are device-unpinned) with `<san>` = sanitized file FQ-name (path-collision-proof; the ScopeRunBlock per-file ordinal alone is not globally unique) — with `override fun run() { driveFlowTask(this.top, __flowUpstream_...(readCapturesFrom(this.top))) }` (resp. `driveSpawnTask(this.top) { __spawnBlock_...(it) }`). CRITICAL: run() passes `this.top` — the component-scope `top` read emits `m.top`, the real task NODE. `this` alone emits `m`, the m-scope AA, and every driver field write/read through it would silently land on the wrong object (adversarial-review finding; the drivers are node-typed for exactly this reason — Task 8's Produces). Synthetic class lives in a NEW synthetic IrFile named `KotlinFlowTask_<san>_<n>.kt` → register hand-constructed `BrsComponentInfo` (six fields, warning block above) in `context.synthesizedComponents` → rewrite call site to `taskFlowLifted<T>("<componentName>", capturesAA)` / `spawnTaskLifted<R>("<componentName>", capturesAA)`.
- Rewrite targets + drivers exist FROM THIS TASK as flow-klib backstop stubs (Task 8 fills the real bodies; without the stubs the OrNull lookups bail and the goldens can never go green): add to `TaskFlow.kt`/`SpawnTask.kt` the four signatures from Task 8's Produces with `throw IllegalStateException("not yet implemented — Task 8")` bodies + `flow-prebuilt:` regen in this task's commits.
- Golden output includes `.xml` + `.deps.json` (harness captures them) — the goldens PIN: component XML with the six fields + `__kotlinTaskMain` export, deps.json containing the originating file + flow-klib files, run() shape, call-site rewrite shape.

- [ ] **Step 1:** RED-first goldens: write `flow/flowOnLift.kt` (a component whose `init` collects `flow { emit(roAA) }.map { ... }.flowOn(Dispatchers.Task)` with one hoisted capture) + `flow/spawnTaskLift.kt`; run `./run-compiler-tests.sh` — fails (no lowering). Commit the failing-shape evidence per golden discipline ONLY as expected-output-absent (test addition), not broken output.
- [ ] **Step 2:** Stdlib base class + klib regen; flow-klib backstop stubs + prebuilt regen; ClassIds/Symbols; lowering; context/compiler merge; phase registration.
- [ ] **Step 3:** `./run-compiler-tests.sh --update` for the two new goldens → line-by-line review (synthesized XML has ALL SIX fields + extends="Task" + `__kotlinTaskMain` export; deps closure; run() passes `m.top`; call-site rewrite) → full golden suite (80 + 2 new + any Task-2 defect pins; zero churn elsewhere) → `caffeinate -ims ./rebuild.sh` green → stdlib device suite unchanged-green. VERIFY the base emitted NO artifacts of its own: grep the generated stdlib runtime for a FlowTaskComponent `sub init()` (must be absent — the annotation makes it a base declaration) and confirm no FlowTaskComponent.xml exists in any output.
- [ ] **Step 4:** Commits: `stdlib: FlowTaskComponent base` + `brs-prebuilt:` regen; `flow: lift rewrite-target + driver backstop stubs` + `flow-prebuilt:` regen; `brs: BrsFlowTaskLiftLowering — per-site task synthesis + call-site rewrite + goldens`.

### Task 8: Runtime halves + two-layer cancellation + runTask rider

**Files:**
- Create: `libraries/flow/brs/src/kotlin/coroutines/flow/TaskFlow.kt`, `libraries/flow/brs/src/kotlin/coroutines/task/SpawnTask.kt`
- Modify: stdlib `TaskRunner.kt` (cancellation rider: STOP on cancel); stdlib `coroutines/pump/PumpScheduler.kt` (the two PUBLIC ambient accessors — produced HERE because this task's guard needs them; Task 10 reuses them):

```kotlin
public fun kotlinAmbientTopOrNull(): RoSGNode?      // = PumpScheduler.hostTopOrNull()
public fun kotlinAmbientGlobalOrNull(): RoSGNode?   // = PumpScheduler.hostGlobalOrNull()
```

- Modify: flow klib `Flow.kt` (flowOn stub body stays backstop); rta `src/brsTest/kotlin/tests/TypedTaskTests.kt` (the cancellation test's pinned meaning changes — see step 4)

**Interfaces:**
- Produces (flow klib; lowering rewrite targets — keep signatures in sync with Task 7, the runTask KDoc convention):

```kotlin
// TaskFlow.kt (package kotlin.coroutines.flow) — drivers are NODE-typed: the
// synthesized run() passes this.top (= m.top, the task node); a
// FlowTaskComponent-typed `this` would be the m-scope AA and every field
// write/read would silently miss the node (adversarial-review finding).
internal fun <T> taskFlowLifted(componentName: String, captures: RoAssociativeArray?): Flow<T>
internal fun onKotlinFlowTaskOut(event: RoSGNodeEvent)      // envelope observer (function-name form, THIS file)
internal fun driveFlowTask(node: RoSGNode, upstream: Flow<Any?>)   // called by synthesized run(), task thread
internal fun readCapturesFrom(node: RoSGNode): RoAssociativeArray? // node.getField("flowCaptures") helper the synthesized run() calls
// SpawnTask.kt (package kotlin.coroutines.task)
public suspend fun <R> spawnTask(block: () -> R): R          // body: guided-ISE backstop (compiler-rewritten; stub from Task 5)
internal suspend fun <R> spawnTaskLifted(componentName: String, captures: RoAssociativeArray?): R
internal fun driveSpawnTask(node: RoSGNode, block: (RoAssociativeArray?) -> Any?)
public fun ensureTaskActive()                                 // task-side checkpoint; no-op off-task
```

- Collector half (`taskFlowLifted`'s Flow, at collect): render-component guard (`kotlinAmbientTopOrNull() == null` → guided ISE "flowOn(Dispatchers.Task) collection requires a render-thread component context (like runTask)") → create node via a `@BrsInline("return CreateObject(\"roSGNode\", name)")` splice → set `flowCaptures` → allocate task id → arm `flowOut` scoped observer (BEFORE control write; TaskRunner arming law) → `control = "RUN"` → drain envelopes through a `FlowChannel` into the downstream; `error` envelope → throw `TaskException(message, number, backtrace)`; `complete` → close. Envelope AAs: `{kind: "emit"|"complete"|"error", seq: Int, value|message/number/backtrace}` — unknown kinds ignored (ScopeWire decision-9 convention). Cancellation (spec §5 + Probe B): on caller cancel — unobserve, drop channel, `flowCancel = true` THEN `control = "STOP"` (`invokeOnCancelRequest` cleanup BEFORE `registerCallerCancel`, TaskRunner.kt:246–252 template). Registry keyed off a per-component map in this file (ComponentMailbox pattern).
- Task half (`driveFlowTask(node, upstream)`, task thread): stash the NODE in a task-thread slot (task-thread GetGlobalAA object — the task thread's m-scope, private to this run) so `ensureTaskActive()` can find it; drive `upstream.collect(shim)` synchronously via a flow-klib suspend wrapper + `startCoroutine(completion)` (no interceptor on the task thread → inline resumption → runs to completion for legal bodies); shim `emit` = check `node.getField("flowCancel")` (truthy → throw internal `FlowTaskCancellation` → clean unwind, Probe B6) then `node.setField("flowOut", envelope(emit, seq++, value))`; completion writes `complete`/`error` envelope + kotlinTaskState protocol values LAST. **Escaped-suspension backstop (spec §7 mandate):** if `startCoroutine` returns WITHOUT the completion having fired, a suspend call genuinely parked — a task thread has no pump, so it can never resume; write an `error` envelope naming the law ("a suspend call parked inside a flowOn(Dispatchers.Task)/spawnTask region — only emit/emitAll may suspend there (BRS_TASK_SUSPEND_IN_LIFTED); an @Suppress-escaped or FIR-hole call did this") so the collector throws instead of hanging. `ensureTaskActive()` = stashed-node `flowCancel` read → throw `FlowTaskCancellation`; no stash → no-op (plain runTask bodies: recorded deviation (c)). `spawnTaskLifted` carries the same render-component guard sentence as the collector half (guided ISE, "spawnTask requires a render-thread component context (like runTask)").
- runTask rider (stdlib `TaskRunner.kt` `awaitCompletion` cancel handler): add `node.setField("control", "STOP")` after the existing remove+unobserve (Probe B: safe on abandoned nodes, idempotent, prompt kill). KDoc + CLAUDE.md update note (Task 13).
- EARLY VERIFY (first device contact, Task 9): the COLLECTOR half's node handle really is the node (it comes from CreateObject — safe by construction), and the render-side `node.setField("flowCaptures", ...)` + observer arming round-trips. The task half uses node.setField/getField exclusively (never typed property access on a component-typed value — that value would be the m-scope AA; the `taskNodeOf(task)` splice returns the same wrong object and is NOT a fallback).

- [ ] **Step 1:** Implement both halves + rider. `--build-only` compile gate.
- [ ] **Step 2:** Stdlib-suite device smoke where possible (runBlocking regime CANNOT run flowOn — component-context law — so the smoke is compile-only here; device proof is Task 9's Suite 10a). `caffeinate -ims ./rebuild.sh` green; goldens sweep (rider may not touch goldens; verify).
- [ ] **Step 3:** Commits: `flow: taskFlowLifted/spawnTask runtime (collector half, task driver, ensureTaskActive)` + `flow-prebuilt:` regen; `stdlib: runTask STOP rider + public ambient-node accessors (Probe B; closes M3 backlog item)` + `brs-prebuilt:` regen.
- [ ] **Step 4:** rta (separate repo): update Suite 4's `runTask await wakes promptly on caller cancellation` test to ALSO pin the new stop behavior (task-side beat counter frozen post-cancel — EchoTask gains a beat mode, or assert via a long-task fixture). Commit `test-infra: Suite 4 cancellation test pins runTask STOP rider`.

### Task 9: KGP wiring + E2E Suite 10a (task lift on device)

**Files:**
- Modify (kotlin-roku repo): `RokuPlugin.kt` (add kotlin-flow-brs to default dependencies where kotlin-stdlib-brs is injected; stage its klib-generated .brs like kotlin-test's; validate-index inclusion), version catalog if any
- Create (rta): `components/fixtures/FlowProbe.kt`; `src/brsTest/kotlin/tests/FlowTests.kt` (Suite 10, `fun TestRunner.flowSuite(scene: RoSGNode)`); Modify: `TestMain.kt` (+1 import, +1 call)
- Test: `cd ../roku-test-app && ./run-device-tests.sh`

**Interfaces:**
- Consumes: driver API (`testAsync`, `awaitField`, `roundTrip` — appendix `test-infrastructure.md` §2 signatures), untyped-probe pattern (Suite 7 template): `@SGStringField var mode`, `@SGBooleanField @BrsOnChange("onStartChanged") var start`, `@SGStringField(alwaysNotify = true) var result` written LAST, `detail` before it; fixture does work in `launch {}`.
- Suite 10a tests (spec §9 first half): `flowOnRoundTrip` (task-side AA fetch → map downstream → result); `flowOnMultiEmission` (3 emissions, in order); `flowOnMidStreamCancel` (cancel collector after first emission; onCompletion cause is CE; probe asserts via detail); `switchMapCancelsTask` — MECHANISM (the synthesized node is runtime-internal, the fixture never holds it): the fixture pre-creates a plain beat NODE (`createChild("Node")` + addField beat/cleanup ints) and the lifted upstream CAPTURES that node ref (node refs are marshallable and cross LIVE); the task-side loop `setField`s beats onto it cross-thread; after the flatMapLatest switch, assert the beat field freezes (Probe B1 shape at the API level); `spawnTaskSuccess` / `spawnTaskError` (TaskException message surfaces) / `spawnTaskCancel`; `onCompletionOnCancel`; `ensureTaskActiveStops` (long compute loop task-side exits early on cancel; cleanup marker via the captured beat node). Budget timeouts: whole-test ≤10s default, per-await explicit where >5s.

- [ ] **Step 1 (kotlin-roku):** wire the dependency + packaging + validate index (read how kotlin-test-brs and stdlib runtime .brs are staged — `PackageRokuTask.stdlibBrs` + `ValidateComponentIncludesTask.runtimeBrs`; mirror). `cd ../roku-test-app && ./rebuild-all.sh --plugin` then a main-app `packageRoku` + `./gradlew validateComponentIncludes` strict-0 with the flow klib staged.
- [ ] **Step 2 (rta):** red-first — Suite 10a tests + FlowProbe written before any device run; `./run-device-tests.sh` (console preflight) — new tests fail only for the right reasons (assert-shaped), then green. Record E2E counts (86+9 → grows, suites 9→10). `./gradlew validateTestComponentIncludes` strict-0 — the FIRST validate pass over compiler-synthesized components (spec §9; do it here, not at sweep).
- [ ] **Step 3:** Commits — kotlin-roku: `feat: stage kotlin-flow-brs klib (default dependency + packaging + validation index)`; rta: `test-infra: E2E Suite 10a — flowOn/spawnTask device acceptance` (+ `app:`-prefixed fixture commit if split). **Phase 2 exit gate:** goldens (82 = 79 +operatorChain +flowOnLift +spawnTaskLift, + any defect pins) · FIR (227+new) · stdlib (grown) · E2E (grown, 10 suites) · validate 0/0 — all green, recorded.

---

# Phase 3 — hot tier

### Task 10: StateFlow + doorbell + stateIn + the access lowering

**Files:**
- Create: `libraries/flow/brs/src/kotlin/coroutines/flow/StateFlow.kt`, `Doorbells.kt` (collect registry + `onKotlinFlowDoorbell` + the static dispatch entry points live HERE together), `StateIn.kt`; `compiler/.../lower/BrsFlowAccessLowering.kt` + golden `compiler/testData/codegen/brs/flow/stateFlowAccess.kt`
- Modify: `BrsStandardClassIds.kt` (StateFlow/MutableStateFlow ClassIds; `Callables.flowCollect` for the member), `BrsSymbols.kt` (static-target lookups), `BrsLoweringPhases.kt` (register beside the other rewrites, 0.059)
- Consumes: the public ambient accessors Task 8 added to `PumpScheduler.kt`
- Test: `FlowStateTest.kt` (stdlib suite — the runBlocking regime can exercise ONLY: `value` get, construction, `asStateFlow` identity, and the off-context-emit guided-ISE message via `assertFailsWith`; dedup/conflation/stateIn BEHAVIOR need component context and live in Suite 10b — `stateFlowEqualityDedup`, `stateInBridgesFlow`)

**Interfaces:**
- Produces (flow klib, spec §14 verbatim + §6 protocol):

```kotlin
public interface StateFlow<out T> : Flow<T> { public val value: T }
public interface MutableStateFlow<T> : StateFlow<T> { public override var value: T }
public fun <T> MutableStateFlow(value: T): MutableStateFlow<T>
public fun <T> MutableStateFlow<T>.asStateFlow(): StateFlow<T>
public fun <T> Flow<T>.stateIn(scope: CoroutineScope, initialValue: T): StateFlow<T>
internal fun onKotlinFlowDoorbell(event: RoSGNodeEvent)     // scoped-observer handler, THIS file (include-closure law)
```

- Produces (flow klib, the static access layer — Doorbells.kt):

```kotlin
// The access-lowering rewrite targets. ALSO the ONLY paths flow-klib internals
// use to touch a possibly-cross-component flow: interface-receiver member/
// accessor calls are fn-slot dispatch — no include-closure dependency recorded,
// and for a shared-VM-held StateFlow the slot fn-ref crosses the SetRef graph
// on the disclaimed path the SharedService program forbids (deviation (e)).
internal fun <T> stateFlowGetValue(flow: StateFlow<T>): T
internal fun <T> stateFlowSetValue(flow: MutableStateFlow<T>, value: T)
internal suspend fun <T> flowCollectDispatch(flow: Flow<T>, collector: FlowCollector<T>)
// flowCollectDispatch: `if (flow is StateFlowImpl) stateFlowCollectData(...) else flow.collect(collector)`
// — the else-branch member call is safe: cold/operator Flow objects are always
// same-component-local (the cold-flows-never-cross law, Task 2 KDoc).
```

- **`BrsFlowAccessLowering`** (user modules only, OrNull-bail; `BrsSharedFromCallLowering` is the template; slot 0.059): rewrites (1) `value` accessor calls where the receiver's static type reaches StateFlow/MutableStateFlow → `stateFlowGetValue`/`stateFlowSetValue`; (2) `Flow.collect(collector)` MEMBER calls → `flowCollectDispatch`. The public `collect(action)`/terminal EXTENSIONS (Terminals.kt) and all operator internals call `flowCollectDispatch` directly in source — so every user file that collects records a static dep on DoorbellsKt and the include closure pulls the doorbell/registry file transitively (the closure hole the adversarial review found).
- Protocol (spec §6 verbatim; Probe A pins every carrier fact): impl class `StateFlowImpl<T>(initial)` holds PLAIN STORED `value` + `uuid: String` ("" until bound) + `version: Int`; behavior lives in the Doorbells.kt statics operating on that data — no cross-component slot ever fires. Emit (`stateFlowSetValue`): equality gate (`x == current` → return, dedup) → render-context guard (guided ISE: "StateFlow emits require a render-thread component context; reads work anywhere") → store → lazy bind (uuid via `CreateObject("roDeviceInfo").GetRandomUUID()` splice + `global.addField("__kotlinFlow_"+uuid, "integer", true)`) → `global.setField(field, ++version)`. GUARD BEFORE STORE — an off-context emit must not mutate a value collectors were never told about. Collect (`stateFlowCollectData`): guard + bind if unbound → deliver current value → loop { park on a per-component `FlowDoorbells` registry entry (field-name → parked list, refcounted `observeFieldScoped(field, brsName(::onKotlinFlowDoorbell))` per component); on wake read live `value`, deliver unless `==` lastDelivered }. Never completes; cancellation → deregister, last-collector unobserve. `stateIn(scope, initial)`: eager `scope.launch { upstream collected via flowCollectDispatch into stateFlowSetValue }` → returns the read-only view; frozen after scope death.
- Consumes: `suspendCoroutine` parks (user-mode code — ordinary idioms legal), `RoSGNode.addField/setField/observeFieldScoped/unobserveFieldScoped` (public), Probe A facts (stacking, per-write in-order, alwaysNotify, auto-detach on death).

- [ ] **Step 1:** Red-first stdlib-suite tests (runBlocking regime covers ONLY: `value` get returns initial, construction anywhere, `asStateFlow` view identity, off-context-emit guided ISE via `assertFailsWith` + message contains "render-thread", and — because the guard precedes the store — `value` unchanged after the failed emit). Everything behavioral is Task 11 device territory; keep this file honest about the split.
- [ ] **Step 2:** RED-first golden `flow/stateFlowAccess.kt`: a component reading `vm.screenState.value`, setting a `MutableStateFlow.value`, and collecting via the extension — pins the three rewrites (get/set → statics, collect chain → flowCollectDispatch dep on DoorbellsKt in deps.json). This is spec §9's "StateFlow emit/collect codegen" golden.
- [ ] **Step 3:** Implement flow-klib + lowering; `--build-only` → `./run-compiler-tests.sh --update` for the golden (line-by-line review incl. the deps.json DoorbellsKt edge) → `caffeinate -ims ./rebuild.sh` → stdlib device suite (grown) → full goldens sweep.
- [ ] **Step 4:** Commits: `flow: MutableStateFlow/StateFlow over global-node doorbells + stateIn + static access layer` + `flow-prebuilt:` regen; `brs: BrsFlowAccessLowering + stateFlowAccess golden`.

### Task 11: E2E Suite 10b — StateFlow on device

**Files:**
- Create (rta): `components/fixtures/FlowVmFixture.kt` (plain-class VM: `class FlowFixtureVm : SharedService() { val screenState = MutableStateFlow<...>("initial") ... }` — plain file under `components/`, generic wiring exists), `components/fixtures/FlowOwnerProbe.kt` (publishes the VM via `shareOn(top, vm)`, emits on command), `components/fixtures/FlowCollectorProbe.kt` (acquires via `sharedFrom`, collects, mirrors deliveries into a result field)
- Modify: `src/brsTest/kotlin/tests/FlowTests.kt` (Suite 10b tests), fixtures as above

**Interfaces:**
- Consumes: Suite 8/9 two-probe management pattern (`Probes` holds ONE probe; the second node via manual `scene.appendChild`/`removeChild` — appendix `test-infrastructure.md` gotcha 14); SharedService API (`shareOn`/`sharedFrom` — OS 15 floor is fine, device is 15.3.4).
- Suite 10b tests (spec §9 second half): `stateFlowSameComponent` (owner collects its own VM's flow; ordered deliveries); `stateFlowCrossComponent` (child acquires the SAME VM via sharedFrom, collects — sealed/AA state read live, no husk); `stateFlowLateJoin` (collector starting after 2 emits sees current value immediately); `stateFlowEqualityDedup` (equal re-emit not delivered); `stateFlowConflationBurst` (N rapid emits → collector sees latest, monotonic, no duplicates beyond dedup law); `stateFlowCollectorDeathTeardown` (destroy collector component; owner keeps emitting — no crash, no ghost; Probe A4 parity at the API level); `stateFlowCancelCollect` (cancel → onCompletion CE + unobserve; re-collect works); `stateInBridgesFlow` (flowOn repository flow stateIn'd in owner scope; child observes updates).

- [ ] **Step 1:** Red-first tests + fixtures; `./run-device-tests.sh` (preflight) to green. Record counts. validateTestComponentIncludes strict-0.
- [ ] **Step 2:** Commits (rta): `test-infra: E2E Suite 10b — StateFlow device acceptance` (+ `app:` fixture commit if split).

### Task 12: TestScreen flagship rewrite

**Files:**
- Modify (rta): `components/TestScreen/TestScreenVM.kt`, `components/TestScreen/TestScreen.kt` (`FakeApiTask.kt` retired or kept per step 1 — the repository flow replaces `runTask<FakeApiTask>` with task-side fetch upstream of flowOn)

**Interfaces (the spec §6 flagship end-state, preserved wiring per appendix `test-infrastructure.md` §4):**

```kotlin
// TestScreenVM.kt
class TestScreenVM : ViewModel() {
    private val _screenState = MutableStateFlow<State>(State.Loading)
    val screenState: StateFlow<State> = _screenState.asStateFlow()
    suspend fun load() {
        _screenState.value = try {
            repositoryFlow().first()
        } catch (e: TaskException) { State.Failed(e.message ?: "unknown task failure") }
    }
    private fun repositoryFlow(): Flow<State> =
        flow { emit(fetchBothJson()) }                 // task-side: blocking fetches, emits ONE AA {user, greeting}
            .flowOn(Dispatchers.Task)
            .map { State.Loaded(stringField(it, ...), stringField(it, ...)) }   // render-side domain mapping
}
// TestScreen.kt init
init {
    shareOn(top, vm)
    launch { vm.screenState.collect { screenTitle = renderTitle(it) } }
    launch { vm.load() }
}
```

(`fetchBothJson` = top-level function doing the two canned fetches with the old latency sleeps — blocking is natural task-side; marshallable-emission idiom: emit the parsed AA, map downstream. `screenTitle` @SG field + `@BrsOnChange` pipe and the layout stub stay untouched.)

- [ ] **Step 1:** Rewrite per the block; keep `State` sealed class + `renderTitle` unchanged; delete or repurpose FakeApiTask (if nothing else uses it, retire it — check Suite references first).
- [ ] **Step 2:** The spec-§6 flagship child fixture (its own sentence in the spec's acceptance): a small `TestScreenChildLabel : GroupComponent` embedded in TestScreen's `@SGLayout`, acquiring the SAME VM via `sharedFrom<TestScreenVM>(node)` (node via `@SGNodeField` or `getParent()` — Suite 9 SharedConsumerProbe pattern) and collecting `vm.screenState` into its own label — the same shared flow, two collectors, cross-component. Smoke-assert via the E2E screen-boot path or a Suite 10b addendum test if the main app isn't E2E-driven.
- [ ] **Step 3:** `cd ../roku-test-app && ./rebuild-all.sh --all` then `./run-device-tests.sh` full-suite green + `./gradlew validateComponentIncludes` strict-0 (main app packages the synthesized flowOn component now); manually confirm the screen renders "Loading…" → greeting (both labels) on the device.
- [ ] **Step 4:** Commit (rta): `app: TestScreen flagship — StateFlow VM + flowOn repository flow + child collector (the modern-stack demo)`.

### Task 13: Sweep, CLAUDE.md, backlog, spec close

**Files:**
- Modify: `CLAUDE.md` (new "Flow & StateFlow" section per spec §10 — three tiers, law lists incl. cold-flows-never-cross + the static access layer, dispose contract, doorbell protocol, marshallable-emission idiom, kotlinx divergences, the kotlin-flow-brs klib home + `flow:`/`flow-prebuilt:` commit prefixes; runTask section gains the STOP rider + the recorded deviation (c) scope note; quarantine section message update; gate-table numbers + "(as of ...)" header), `docs/superpowers/backlog-typed-task-program.md` (append the FULL spec §13 recorded list: SharedFlow/events, callbackFlow/awaitClose, channelFlow + public Channel API, buffer configs/backpressure, SharingStarted modes, debounce/sample, zip, doorbell field-name reuse pool, task-side delay-as-sleep, husk re-animation, multi-module lifted regions, task-thread `.value` writes + main-thread collect — plus this program's own recordings: B5 blocked-transfer re-probe against a stalling LAN listener, cooperative checkpoints for plain typed tasks (deviation (c))), spec header Status → "Implemented"
- Test: the full gate sweep

- [ ] **Step 1:** Full sweep, all recorded: `caffeinate -ims ./rebuild.sh` → `./run-compiler-tests.sh` → FIR suite → `./run-stdlib-tests.sh` → rta `./run-device-tests.sh` → `./gradlew validateComponentIncludes` (standalone — NOT wired into rokuTest) + `validateTestComponentIncludes`.
- [ ] **Step 2:** Docs edits above; gate table updated with final numbers.
- [ ] **Step 3:** Commits: `docs: CLAUDE.md Flow & StateFlow section + runTask STOP rider + gate table NN/NN/...` ; `docs: backlog — flow program recorded follow-ups`. Surface ALL ledger rulings to Mike; do NOT push (Mike's word only).
