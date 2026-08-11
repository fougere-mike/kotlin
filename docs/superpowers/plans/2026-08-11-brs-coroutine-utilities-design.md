# BRS Coroutine Utilities — Design (awaitAll & friends)

**Date:** 2026-08-11
**Status:** Approved design, pending implementation plan
**Branch:** `feature/brightscript-backend-2.2.20`

## Decisions made during brainstorming (Mike, 2026-08-10/11)

| Decision | Choice |
|---|---|
| v1 scope | Full set: completion protocol, join/await/awaitAll/joinAll, real coroutineScope, withContext rework, withTimeout(+OrNull), parent-child hierarchy |
| Cancellation promise | Mid-park wakeup: `cancel()` immediately resumes the victim's parked continuations with `CancellationException` (once-guarded), plus entry checks at every stdlib suspend point, plus cascade to children |
| Failure model | kotlinx parity inside `coroutineScope {}` (child failure cancels siblings, rethrow after all children finish); `componentScope()` root is supervisor-like (failed top-level `launch` reports to console and dies alone) |
| Architecture | Instance-held callbacks on `JobImpl` (invokeOnCompletion protocol); no registries, no ids |
| Phase 0 | Fix two codegen defects first: `&&`/`||` short-circuit miscompile and expression-position try/catch. `@BrsInline` if/else template splice stays backlog |

## Context: what the audit found (2026-08-10)

The utilities half-exist already. `async {}`, `Deferred<T>`, `CompletableDeferred<T>`,
`coroutineScope {}`, and `withContext` are all in the stdlib today; what is missing is
not API surface but a working completion protocol underneath:

1. **`JobImpl.join()` hangs forever** (`Job.kt:121`): `while (!completed) {}` with an
   empty body on a single-threaded runtime. Nothing pumps inside the loop, so the
   awaited coroutine can never progress. It only appeared to work before the
   2026-08-10 dispatcher-key fix because everything ran inline and jobs were always
   complete before anyone joined.
2. **`Deferred.await()` is doubly broken**: it calls `join()` (busy-wait) and has code
   after the suspension — which violates the tail-delegation constraint (below).
3. **`coroutineScope {}` is a decoy**: builds its scope from `EmptyCoroutineContext`
   (drops the caller's dispatcher), does not wait for children.
4. **`withContext` (non-IO) runs the block via a nested `runBlocking`** on the current
   thread — inside a pump drain this blocks the render thread until the block is done.
5. **No job hierarchy**: `launch`/`async` create orphan jobs; `JobImpl.parent` is
   stored but never read; no children tracking; cancellation is write-only flags;
   no `CancellationException` type exists in the BRS stdlib.
6. **The template to copy exists and is device-proven**: `TaskRunner` registers a
   continuation and resumes it through `ContinuationInterceptor` when the completion
   event fires; the resumption lands on the dispatcher queue, the PumpScheduler wakes
   the render thread, and `runBlocking`/`runPumping` drain the same queue on the main
   thread — both pumping regimes serviced by one mechanism.

### Load-bearing constraints

- **Tail-delegation (no stdlib state machines).** `BrsSuspendFunctionsLowering` skips
  state-machine generation when compiling the stdlib itself (coroutine symbols are
  unavailable — chicken-and-egg). Every stdlib suspend function must therefore be:
  synchronous fast-paths first, then a single `suspendCoroutineUninterceptedOrReturn`
  as the LAST statement, with all post-completion logic in handler closures.
  `delay()`, `yield()`, `awaitCompletion()` already have this shape.
- **Per-component singletons.** `GetGlobalAA` is per-component-instance on the render
  thread, so `CoroutineQueue`/`DelayTracker`/`PumpScheduler` are per-component.
  Handlers held on Job instances work across coroutines within one component; jobs
  must NOT be shared across components (a resume fired in another component's context
  would enqueue into that component's queue). Documented v1 limitation.
- **Both pumping regimes must work**: components (PumpScheduler wakeups) and
  main-thread drivers (`runBlocking`, kotlin.test `runPumping`). Since all
  resumptions go through the interceptor onto the shared queue, both regimes are
  serviced by construction; both get test coverage anyway.
- **`runBlocking` keeps polling flags** (`deferred.isCompleted`) — the new protocol
  still sets those flags, so the main-thread driver needs zero changes.

## Phase 0: codegen fixes (prerequisite)

### 0a. Platform probe (documentation, not a gate)

The repo asserts in two places that BrightScript `and`/`or` do not short-circuit
(TaskRunner KDoc, `__kotlinTaskMain` generator comment) — stated as device truth but
undocumented in `../RokuDocs`. Run a 2-minute device probe
(`x = invalid : if x <> invalid and x.count() > 0`) and record the result in
CLAUDE.md's BrightScript notes. **The codegen fix proceeds regardless** — correctness
must hold on the oldest supported OS, and the probe only proves the OS it runs on.

### 0b. `&&`/`||` short-circuit miscompile

`IrExpressionToBrsTransformer.visitWhen` (line ~3215) pattern-matches
`ANDAND`/`OROR` IrWhen and unconditionally emits bare `and`/`or`. Under
non-short-circuiting BRS semantics, `x != null && x.foo()` compiles to
`x <> invalid and x.foo()` which evaluates `x.foo()` on invalid and crashes.

**Fix:** only take the compact `and`/`or` emission when the RHS is provably
effect-free AND crash-free (conservative whitelist: constants, local reads, `!x` of
those; anything involving a call, member access, or safe-call falls through).
Otherwise emit the lowered form (temp variable + nested if — the shape
`BrsWhenExpressionLowering`/statement transformation already produces). Check whether
the lowering deliberately skips ANDAND/OROR origin and adjust both sides coherently.

**Tests:** new golden (`expressions/shortCircuit.kt`) covering both emissions;
stdlib device test asserting the null-guard idiom `x == null || x.isEmpty()` and
`x != null && x.count() > 0` run without crash for `x = null`. Existing goldens
re-reviewed (pure-boolean cases should keep the compact form — zero diff expected).

### 0c. Expression-position try/catch

`val x = try { ... } catch (e: T) { ... }` currently emits literal `return try`
garbage (Task 18 finisher ledger). **Fix:** statement-extraction lowering — hoist the
try/catch to statement position with a temp result variable (mirror the when-lowering
approach). **Tests:** golden (`controlFlow/tryCatchExpression.kt`) + stdlib device
test (both arms, plus nested-in-string-template case).

Out of scope for Phase 0: the `@BrsInline` single-line if/else template splice
(stdlib-authoring trap, backlog; we author no such templates in this program).

## Phase 1: the Job core + await family

### JobImpl lifecycle

Plain booleans (single-threaded per component; no atomics). States:

- **Active** (initial) → **Completing** (body finished OR cancel requested; children
  still winding down) → terminal **Completed** / **Cancelled**.
- The terminal transition fires only when body-done AND children list empty. This is
  what makes `coroutineScope`'s "no child still running when it returns" guarantee real.
- `isCancelled` becomes true at cancel REQUEST time (kotlinx parity); `isCompleted`
  only at terminal.
- `completionCause: Throwable?` — the failure, or the `CancellationException`
  manufactured by a cause-less `cancel()`.

### Two handler kinds (both return a disposable handle)

```kotlin
public interface DisposableHandle { public fun dispose() }

// Public, kotlinx-parity — fires exactly once at TERMINAL state
// (immediately if already terminal). Handler receives completionCause (null = success).
public fun invokeOnCompletion(handler: (Throwable?) -> Unit): DisposableHandle

// Internal — fires at cancel-REQUEST time (immediately if already cancelled).
// Powers mid-park wakeup.
internal fun invokeOnCancelRequest(handler: (Throwable?) -> Unit): DisposableHandle
```

Disposal matters: awaiters register cancel-request handlers on the CALLER's job —
often the long-lived component root — and must deregister when the await resolves or
handlers accumulate for the component's lifetime.

Handler/children lists are iterated over copies (mutation during firing); a throwing
handler is caught and console-reported, never breaks the firing loop.

### Hierarchy (implemented in Phase 2, interfaces prepared in Phase 1)

- Construction with a parent attaches to the parent's children list (internal
  `attachChild`); terminal detaches (and may complete a Completing parent).
- `cancel(cause)` cascades to children recursively, fires cancel-request handlers.
- **Child failure goes up**: a child completing exceptionally calls into its parent.
  Normal parent → cancels itself with that cause (siblings die; scope rethrows the
  ORIGINAL exception). Supervisor parent (internal flag) → ignores it.
  `CancellationException` is never treated as failure — cancelled children detach quietly.
- **Scope jobs are special** (used by `coroutineScope`/`withContext`/`withTimeout`):
  they attach as children for DOWNWARD cancellation, but their failure is delivered
  to the parked outer continuation (rethrow at the call site), NOT upcalled to the
  caller's job — kotlinx `ScopeCoroutine` parity. Flag: `propagatesFailureToParent = false`.
- Foreign `Job` implementations (user-written) found in a context are treated as
  no-parent; only our impls participate in the hierarchy.

### CancellationException

`kotlin.coroutines.cancellation.CancellationException : IllegalStateException`
(stdlib-common package location). `TimeoutCancellationException` extends it (Phase 3).

### CompletableDeferredImpl

Keeps composition over an inner `JobImpl`; delegates the full Job surface including
both handler kinds. Value/exception storage unchanged (value stored before
`complete()` fires handlers). New: `Deferred.getCompletionExceptionOrNull()`
(kotlinx-parity accessor; also what `awaitAll`'s fast path uses).

### The shared park helper

```kotlin
// One per suspension. Once-flag resolves every completion-vs-cancellation race.
internal class ParkedContinuation(cont: Continuation<Any?>) {
    val handles: MutableList<DisposableHandle>
    fun tryResume(value: Any?)          // first caller wins; disposes handles;
    fun tryResumeException(e: Throwable) // resumes through ContinuationInterceptor
}
```

### Entry checks + cooperative helpers (public, kotlinx-parity)

```kotlin
public fun Job.ensureActive()                        // throws CE if cancelled
public fun CoroutineContext.ensureActive()           // no-op when no Job present
public val CoroutineScope.isActive: Boolean          // for `while (isActive)` loops
```

Every stdlib suspend utility calls `context.ensureActive()` first (the "next-suspend
check" half of the cancellation promise). All null-safe: no Job in context (bare
`runBlocking` bodies, kotlin.test driver) → always active.

### The utilities (all tail-delegating)

- **`Job.join()`** — fast path: target terminal → return. Parked: terminal handler on
  target resumes Unit. Join on a cancelled/failed TARGET returns normally (kotlinx
  parity) — only the CALLER's cancellation throws, via cancel-request handler on the
  caller's job.
- **`Deferred.await()`** — terminal handler resumes with value, or exceptionally with
  the target's failure (including CE if the target was cancelled). Caller
  cancel-request handler as above.
- **`awaitAll(vararg ds: Deferred<T>): List<T>`** + **`Collection<Deferred<T>>.awaitAll()`**
  — empty → `emptyList()` immediately. Fast path: all terminal → collect in input
  order (throw first failure by input order). Parked: countdown latch; first failure
  resumes exceptionally immediately (once-guard; siblings NOT cancelled — kotlinx
  parity); last success assembles `List<T>` in input order via `getCompleted()`.
  Duplicate inputs count independently — harmless.
- **`joinAll(vararg jobs)` / `Collection<Job>.joinAll()`** — countdown over terminal
  handlers; never throws on target failure; caller cancellation wakes it.
- **`delay()` retrofit** — entry check + caller cancel-request handler + once-guard
  shared with the DelayTracker callback. DelayTracker has no deregistration; a
  cancelled delay's callback fires at deadline into a no-op (stale PumpScheduler
  timer wake drains nothing — harmless). No tracker-removal API in v1.
- **`yield()` retrofit** — entry check only.

## Phase 2: hierarchy + builders + scope functions

- **`launch`/`async`** pass `parent = mergedContext[Job]` and attach. Completion:
  body success → `complete()` (holds in Completing until children finish); body
  failure → `completeExceptionally()`; CE → quiet cancellation, not failure.
  Launching on a cancelled scope returns a pre-cancelled job without running the
  block. Bare legacy scopes (no Job in context) keep working: null parent.
- **Unhandled failure reporting**: a failed `launch` with no non-supervisor parent
  prints `[kotlin.coroutines] Unhandled exception in launch: <throwable>` to the
  console (today it is silently swallowed). `async` never auto-reports — failure
  surfaces at `await()`.
- **`SupervisorJob(parent: Job? = null): CompletableJob`** — public factory;
  `componentScope()` root becomes `Dispatchers.Main + SupervisorJob()`.
- **`coroutineScope {}`** — drops `inline` (klib inline is never inlined at call
  sites — `BrsInlineFunctionResolver` limitation — so this is declaration-level, not
  behavioral). Tail-shaped: scope job as child of caller's job, caller's FULL context
  preserved + scope job; block started `startCoroutine`-style with a completion
  continuation that parks the result and completes/fails the scope job; outer
  continuation resumed from the scope job's TERMINAL handler (result or original
  failure). Caller cancellation reaches it via the child cascade — no extra handler.
- **`withContext(ctx) {}`** non-IO — exactly the coroutineScope machinery with `ctx`
  merged into the child context. Kills the nested-runBlocking hazard. The quarantined
  IO identity check stays (FIR error already blocks user code).

## Phase 3: withTimeout

- **`TimeoutCancellationException : CancellationException`** (public;
  uncaught → cancels, doesn't crash — by inheritance).
- **`withTimeout(timeMillis: Long, block)`** — coroutineScope shape + one
  `DelayTracker.register(timeMillis)`: at deadline, if scope job not terminal →
  `scopeJob.cancel(TimeoutCancellationException(...))` → cascade wakes children
  mid-park → terminal → outer resumes exceptionally with the TCE. Block wins →
  resume with value; stale delay callback no-ops on terminal check. PumpScheduler
  already arms wakeups for DelayTracker deadlines — component timeouts fire promptly
  for free.
- **`withTimeoutOrNull`** — same machinery; terminal handler maps our own TCE to `null`.
- In-flight task-thread work is NOT stopped (runTask has no cancellation until M3);
  the render-side coroutine stops waiting and the task's eventual completion resumes
  nothing (once-guard/registry miss).

## Phase 4: acceptance + docs

- **ShelfView flagship demo**: the two sequential `runTask`s become concurrent —
  `val ip = async { runTask<UrlTransferTask> {...} }`,
  `val shelf = async { runTask<FetchShelfTask> { count = 5 } }`, `awaitAll(ip, shelf)`.
- New E2E suite + fixture component (see Testing).
- CLAUDE.md: new "Coroutine utilities" section; update the coroutine-scaffolding
  section; memory updates (`project_brs_suspend_codegen_defects` — join() fixed;
  new/updated utilities memory).

## Testing

Both pumping regimes, every phase gated on device.

**Stdlib device suite** (regime 1: `runBlocking` main thread, `./run-stdlib-tests.sh`):
- Phase 0: short-circuit + try/catch-expression semantics tests.
- Phase 1: handler firing/disposal (incl. already-terminal registration), once-guard
  races, join/await/awaitAll/joinAll success/failure/cancellation matrices,
  ensureActive/isActive, delay cancellation, empty/duplicate awaitAll inputs.
- Phase 2: attach/detach/cascade, Completing-state gating, supervisor vs normal
  parent on child failure, coroutineScope result/rethrow/sibling-cancel,
  withContext context-preservation.
- Phase 3: withTimeout win/lose/nested, withTimeoutOrNull, TCE identity.

**E2E rokuTest** (regime 2: PumpScheduler in components, `./run-device-tests.sh`):
new suite (CoroutineUtilities; number assigned at implementation — current numbering
skips 5) + fixture component:
awaitAll over concurrent `async{delay}`s; awaitAll over two concurrent `runTask`s
(the flagship shape); withTimeout expiry with mid-park child wakeup; coroutineScope
sibling cancellation; supervisor-root survival after a failed launch. Existing
Suites 2/4 are the regression canary for launch/delay/runTask behavior.

**Gates (must not drop):** goldens 57 (+new; diffs from Phase 0/coroutineScope
reviewed via `./run-compiler-tests.sh --update`), FIR 219 (no new diagnostics
planned), stdlib 411 (+new), E2E 33/6 (+new suite), `validateComponentIncludes`
strict 0, rebuild.sh step 7 (kotlin-test-brs compile) green every phase.

## Risks

- **Stdlib suspend codegen**: every new suspend utility's generated `.brs` gets
  manual review in `generateStdlibBrs` output during implementation (no state
  machine = no safety net for accidental non-tail suspension).
- **`vararg` + generics in klib suspend functions**: smoke-check in Phase 1;
  fallback is Collection-only overloads.
- **kotlin.test compatibility**: `runPumping`/`awaitField` coroutines run without a
  Job in context — all entry checks are null-safe (no Job → active). Step 7 is the gate.
- **Existing E2E suites** depend on current launch/delay semantics — hierarchy
  attachment changes failure propagation for coroutines that previously leaked
  failures silently; Suite 2/4 green is the canary.
- **Cascade recursion depth**: bounded by real hierarchies (shallow); lists iterated
  over copies to survive mutation-during-firing.

## Out of scope (documented)

- Cross-component Job sharing (per-GetGlobalAA singletons; same-component only).
- Task-thread cancellation (`runTask` cancel/timeout — M3 backlog).
- `CoroutineExceptionHandler` context element (console print is the fixed v1 sink).
- `Job.children` sequence, `select {}`, flows, channels.
- `@BrsInline` if/else template splice fix (backlog).
- DelayTracker deregistration API (once-guards make stale callbacks harmless).

## Public API summary (new/changed)

```kotlin
// kotlin.coroutines
public interface DisposableHandle { public fun dispose() }
public interface Job {  // additions
    public fun invokeOnCompletion(handler: (Throwable?) -> Unit): DisposableHandle
}
public interface Deferred<out T> : Job {  // addition
    public fun getCompletionExceptionOrNull(): Throwable?
}
public fun SupervisorJob(parent: Job? = null): CompletableJob
public fun Job.ensureActive()
public fun CoroutineContext.ensureActive()
public val CoroutineScope.isActive: Boolean
public suspend fun <T> awaitAll(vararg deferreds: Deferred<T>): List<T>
public suspend fun <T> Collection<Deferred<T>>.awaitAll(): List<T>
public suspend fun joinAll(vararg jobs: Job)
public suspend fun Collection<Job>.joinAll()

// kotlin.coroutines.cancellation
public open class CancellationException(message: String? = null) : IllegalStateException

// kotlin.coroutines (signature change: drops inline/crossinline)
public suspend fun <R> coroutineScope(block: suspend CoroutineScope.() -> R): R

// kotlin.coroutines.builders
public class TimeoutCancellationException : CancellationException
public suspend fun <T> withTimeout(timeMillis: Long, block: suspend CoroutineScope.() -> T): T
public suspend fun <T> withTimeoutOrNull(timeMillis: Long, block: suspend CoroutineScope.() -> T): T?
// withContext: same signature, non-IO path rebuilt on scope machinery
```
