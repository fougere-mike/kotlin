I have everything needed. Here is the reference map.

---

# DOMAIN 1 MAP: BRS coroutines stdlib runtime

All paths repo-relative to `/Users/Mike.Fougere/Documents/newt/git/Kotlin`. Stdlib root: `libraries/stdlib/brs/src/kotlin/`.

## 1. ParkedContinuation — `libraries/stdlib/brs/src/kotlin/coroutines/ParkedContinuation.kt`

```kotlin
internal class ParkedContinuation(private val continuation: Continuation<Any?>)   // :26
    internal val handles = mutableListOf<DisposableHandle>()                       // :29
    fun tryResume(value: Any?)                                                     // :37
    fun tryResumeException(exception: Throwable)                                   // :49
    fun finish(): Any?                                                             // :62  "MUST be the last expression of the enclosing intrinsic block"
```

Mechanics that matter:
- **Starts UNARMED** (:17–25 doc): a handler firing synchronously before `finish()` is *recorded*; `finish()` converts it to a synchronous return/throw instead of a double-execute. In the no-interceptor regime (runBlocking/runPumping) handlers can fire while the intrinsic block is still running.
- **Settle-once**: first `tryResume*` wins; all subsequent are no-ops; settling disposes ALL registered `handles` (:72–77).
- **Resumes through the interceptor when present** (`resumeTarget()`, :79–85): `context[ContinuationInterceptor].interceptContinuation(continuation)` → coroutine continues on its dispatcher's queue, not inside the observer callback.

Same file, the cancellation helpers:
```kotlin
public fun Job.ensureActive()                                                      // :93  throws the cancellation cause
public fun CoroutineContext.ensureActive()                                         // :102 no-op when no Job in context
public val CoroutineScope.isActive: Boolean                                        // :107
internal fun registerCallerCancel(parked: ParkedContinuation, context: CoroutineContext)  // :117 mid-park wakeup: caller's cancel → tryResumeException(CE)
```

## 2. The suspend-utility idiom

**The canonical park shape** (Delay.kt:40–53 `delay`, Job.kt:324–338 `join`, Job.kt:424–446 `await`, Await.kt:22–66 `awaitAll`):
```kotlin
return suspendCoroutineUninterceptedOrReturn { continuation ->
    continuation.context.ensureActive()          // entry check FIRST, inside the block
    // fast path: return the value directly (or throw) if already done
    // slow path:
    val parked = ParkedContinuation(continuation as Continuation<Any?>)
    parked.handles.add(<source>.invokeOnCompletion { ... parked.tryResume/tryResumeException ... })
    registerCallerCancel(parked, continuation.context)
    parked.finish()                              // LAST expression
}
```
- **Entry check is inside the intrinsic block** because **there is no `coroutineContext` intrinsic usable in stdlib source** (TaskRunner.kt:233–236 comment; `getCoroutineContext()` in intrinsics/IntrinsicsBrs.kt:117 throws — a compiler intrinsic that isn't wired for stdlib). Context is reachable only via `continuation.context`.
- **Tail-delegation law** (TaskRunner.kt:232–236 comment): stdlib suspend functions get NO state machine, so a suspend fn that wraps another suspend call must RETURN the inner call directly (e.g. `awaitAll(vararg)` = `deferreds.toList().awaitAll()`, Await.kt:18–19; `runTaskImpl` ends with `return task.awaitCompletion()`, TaskRunner.kt:279).
- **No captured mutable locals in stdlib closures** (Await.kt:45–47 comment): "stdlib closures must not need shared-box codegen" — mutable state goes in a class instance (`private class CountdownState(var remaining: Int)`, Await.kt:108).
- **Extra cancel-path cleanup** the park can't know about (registry entries, observer disarm) registers via `jobImplOf(context[Job])?.invokeOnCancelRequest { ... }` BEFORE `registerCallerCancel` — TaskRunner.kt:246–252 is the exact template the flowOn cancel sequence (disarm observer → cancel field → STOP) will slot into.

**Await.kt** (`libraries/stdlib/brs/src/kotlin/coroutines/Await.kt`):
```kotlin
public suspend fun <T> awaitAll(vararg deferreds: Deferred<T>): List<T>            // :18
public suspend fun <T> Collection<Deferred<T>>.awaitAll(): List<T>                 // :22
public suspend fun joinAll(vararg jobs: Job)                                       // :72
public suspend fun Collection<Job>.joinAll()                                       // :77
```

**Scopes.kt** (`libraries/stdlib/brs/src/kotlin/coroutines/Scopes.kt`) — the scope engine the internal queue primitive should reuse:
```kotlin
public suspend fun <R> coroutineScope(block: suspend CoroutineScope.() -> R): R    // :24
internal class ScopeResultHolder { var value: Any? = null }                        // :45
internal class ScopeBlockCompletion<R>(override val context: CoroutineContext, private val job: JobImpl, private val holder: ScopeResultHolder) : Continuation<R>  // :50
internal fun <R> parkScopedBlock(
    continuation: Continuation<Any?>, scopeContext: CoroutineContext, scopeJob: JobImpl,
    block: suspend CoroutineScope.() -> R,
    onTerminal: (cause: Throwable?, value: Any?, parked: ParkedContinuation) -> Unit,
): Any?                                                                            // :72 — returns parked.finish(); caller must return it LAST
```
`parkScopedBlock` mechanism: registers `scopeJob.invokeOnCompletion` → `onTerminal`, then `block.startCoroutine(CoroutineScope(scopeContext), ScopeBlockCompletion(...))`. Scope jobs are built as `JobImpl(outerContext[Job], hasBody = true, upcallsFailure = false)` — failure is DELIVERED to the parked caller, never upcalled (Scopes.kt:28, WithContext.kt:80, Timeout.kt:39).

**builders/WithContext.kt**:
```kotlin
public suspend fun <T> withContext(context: CoroutineContext, block: suspend CoroutineScope.() -> T): T   // :63
```
Contains an identity check `dispatcher === Dispatchers.IO || dispatcher === IODispatcher` (:70) routing to the quarantined TaskPool path — otherwise `parkScopedBlock` with `outerContext + context + scopeJob` (:84).

**builders/Timeout.kt**:
```kotlin
public class TimeoutCancellationException internal constructor(message: String) : CancellationException(message)  // :17
public suspend fun <T> withTimeout(timeMillis: Long, block: suspend CoroutineScope.() -> T): T                    // :32
public suspend fun <T> withTimeoutOrNull(timeMillis: Long, block: suspend CoroutineScope.() -> T): T?             // :66
```
Expiry = `DelayTracker.current.register(timeMillis) { if (!scopeJob.isCompleted) scopeJob.cancel(timeout) }` (:41–45); `orNull` matches its own timeout by IDENTITY (`cause === timeout`, :87).

## 3. Job machinery — `libraries/stdlib/brs/src/kotlin/coroutines/Job.kt`

```kotlin
public interface DisposableHandle { public fun dispose() }                          // :15
public interface Job : CoroutineContext.Element                                     // :23
    public companion object Key : CoroutineContext.Key<Job>                         // :26 (@Suppress("BRS_NAME_CASE_CLASH"))
    public val isActive: Boolean; public val isCompleted: Boolean; public val isCancelled: Boolean   // :29,32,35
    public fun cancel(cause: Throwable? = null)                                     // :37
    public suspend fun join()                                                       // :44
    public fun start(): Boolean                                                     // :46
    public fun invokeOnCompletion(handler: (Throwable?) -> Unit): DisposableHandle  // :55
public interface CompletableJob : Job                                               // :58 — complete(): Boolean, completeExceptionally(exception): Boolean
public fun Job(parent: Job? = null): CompletableJob                                 // :67
public fun SupervisorJob(parent: Job? = null): CompletableJob                       // :74
internal fun jobImplOf(job: Job?): JobImpl?                                         // :78
internal open class JobImpl(parent: Job? = null, hasBody: Boolean = false, isSupervisor: Boolean = false,
    upcallsFailure: Boolean = true, reportsUnhandled: Boolean = false) : CompletableJob   // :116
    internal var completionCauseInternal: Throwable?                                // :132
    internal fun invokeOnCancelRequest(handler: (Throwable?) -> Unit): DisposableHandle   // :174 — fires at cancel REQUEST (mid-park wakeup hook)
    internal fun attachChild / detachChild / childFailed                            // :220,224,234
public interface Deferred<out T> : Job                                              // :344 — await(): T, getCompleted(): T, getCompletionExceptionOrNull(): Throwable?
public interface CompletableDeferred<T> : Deferred<T>, CompletableJob               // :361 — complete(value: T): Boolean
public fun <T> CompletableDeferred(parent: Job? = null): CompletableDeferred<T>     // :365
internal class CompletableDeferredImpl<T>(parent: Job? = null, hasBody: Boolean = false)  // :372 — exposes .innerJob
```

Cascade mechanics: single-threaded per component, plain booleans/lists (:98–99). `cancel()` → `cancelRequested=true` → fire cancel handlers → cancel children (wrapping non-CE causes in `CancellationException("Parent job was cancelled", cause)`) → `tryFinish()` when body done + children empty. **Own completion handlers fire BEFORE the parent is notified** (:268–280 — load-bearing for await ordering). `invokeOnCompletion` on an already-terminal job **fires synchronously at registration** (:156–161, with an explicit CAUTION comment for stdlib utilities). CE causes are "quiet" — never upcalled as failure, never reported unhandled (:266). Unhandled launch failures print `[kotlin.coroutines] Unhandled exception in coroutine: ...` (:321).

**CancellationException** (`coroutines/cancellation/CancellationException.kt:14`): `public open class CancellationException : IllegalStateException` — 3 constructors `()`, `(message)`, `(message, cause)`. `ScopeClosedException` (brs/scope) and `TimeoutCancellationException` extend it.

## 4. Builders, scopes, context layout, Dispatchers

**builders/Builders.kt**:
```kotlin
public fun CoroutineScope.launch(context: CoroutineContext = EmptyCoroutineContext, block: suspend CoroutineScope.() -> Unit): Job          // :30
public fun <T> CoroutineScope.async(context: CoroutineContext = EmptyCoroutineContext, block: suspend CoroutineScope.() -> T): Deferred<T>  // :62
public fun <T> runBlocking(context: CoroutineContext = EmptyCoroutineContext, block: suspend CoroutineScope.() -> T): T                     // :95 — SupervisorJob root (kotlinx divergence)
public fun <T> (suspend () -> T).startCoroutine(completion: Continuation<T>)                                                                // :218
public fun <R, T> (suspend R.() -> T).startCoroutine(receiver: R, completion: Continuation<T>)                                              // :225
```
`launch`/`async` on an already-cancelled scope return a DEAD job/deferred without running the block (:36–42, :68–72). Job created as `JobImpl(parent, hasBody = true, reportsUnhandled = true)` for launch; deferred `CompletableDeferredImpl<T>(parent, hasBody = true)`.

**ComponentBase members** (`brs/ComponentCoroutines.kt`, package `kotlin.brs` — default-imported):
```kotlin
public fun ComponentBase.componentScope(): CoroutineScope                            // :53 — Dispatchers.Main + SupervisorJob(), one per component instance (ComponentScopeHolder object on GetGlobalAA :30); lazily PumpScheduler.attach()es
public fun ComponentBase.launch(context: CoroutineContext = EmptyCoroutineContext, block: suspend CoroutineScope.() -> Unit): Job   // :83
```
`top`/`global` bridged via `@BrsInline("return component.top")` external fns (:21–25) — the pattern for stdlib code needing a component's nodes.

**Context layout**: `CoroutineScope.kt:22` interface + `:41` `public fun CoroutineScope(context: CoroutineContext): CoroutineScope`. `CoroutineContext.kt:14` — standard `get/fold/plus/minusKey`, `plus` keeps interceptor LAST (:30–43). `CombinedContext.kt:12` standard. `EmptyCoroutineContext.kt` standard object. `ContinuationInterceptor.kt:14` — `companion object Key`, `interceptContinuation`, `releaseInterceptedContinuation`.

**Dispatchers** — `coroutines/dispatchers/Dispatchers.kt:37`: **`public object Dispatchers` lives in package `kotlin.coroutines.dispatchers`, NOT `kotlin.coroutines`** (spec §14 says `kotlin.coroutines` — divergence to resolve in the plan). Members:
```kotlin
public val Default: CoroutineDispatcher = DefaultDispatcher     // :51
public val Main: CoroutineDispatcher = DefaultDispatcher        // :65 — Main === Default (same object!)
public val Unconfined: CoroutineDispatcher = UnconfinedDispatcher  // :78
public val IO: CoroutineDispatcher = IODispatcher               // :95 — quarantined; FIR error on user reference
```
`CoroutineDispatcher.kt:24` `public abstract class CoroutineDispatcher : ContinuationInterceptor` — **its `key` returns `ContinuationInterceptor` (:48), NOT the CoroutineDispatcher companion** (root-cause comment :34–46: identity-keyed lookups; storing under any other key silently disables all dispatch). `dispatch(context, block: Runnable)` abstract :59; `isDispatchNeeded` :67; `interceptContinuation` wraps in `DispatchedContinuation` :72–73, :86. `DefaultDispatcher` (DefaultDispatcher.kt:34) = enqueue to CoroutineQueue. `UnconfinedDispatcher` (:21) = `isDispatchNeeded=false`, runs inline. `IODispatcher` (IODispatcher.kt:55) = quarantined TaskPool path. For `Dispatchers.Task` note: `withContext` finds the dispatcher via `context[ContinuationInterceptor] as? CoroutineDispatcher` (WithContext.kt:67) and checks `===` identity — a Task token typed `CoroutineDispatcher` merged into a context WOULD intercept resumptions, so if the token is never meant to run, either give it a `dispatch` that fails loudly or don't make it a real interceptor element; the FIR position rule is the primary guard.

## 5. CoroutineQueue + PumpScheduler + DelayTracker

**CoroutineQueue** (`coroutines/dispatchers/CoroutineQueue.kt`): `internal object CoroutineQueue` :21 — `fun enqueue(block: Runnable)` :31 (calls `PumpScheduler.onEnqueue()`), `processAll(): Int` :47, `clear(): Int` :63, `isEmpty()` :74, `size()` :79. Public wrappers: `processCoroutineQueue(): Int` :102, `hasCoroutineWork(): Boolean` :109, `clearCoroutineQueue(): Int` :121. `Runnable` is `kotlin.concurrent.Runnable` — `public fun interface Runnable` (`kotlin/concurrent/Runnable.kt:14`). **Internal but same-module**: code in `kotlin.coroutines.flow` can call `CoroutineQueue.enqueue` directly (the whole stdlib compiles as one module, "stdlib").

**PumpScheduler** (`coroutines/pump/PumpScheduler.kt`): `internal object PumpScheduler` :53.
```kotlin
fun attach(top: RoSGNode, global: RoSGNode)      // :84 idempotent
fun onEnqueue()                                  // :95
fun onDelayRegistered()                          // :101
fun backendName(): String                        // :109
fun hostTopOrNull(): RoSGNode?                   // :119 — THE ambient-component oracle (ScopeHandle precedent)
fun hostGlobalOrNull(): RoSGNode?                // :126 — THE ambient-global oracle → this is how the doorbell reaches m.global from stdlib flow code
fun drain()                                      // :248 — 256-pass cap, then re-post (yields frames)
```
Also: `@BrsStatic public fun __kotlinPumpAttach(node, global)` :308 (compiler-injected init hook); test hooks `kotlinPumpForceTimerBackend(global)` :319, `kotlinPumpForceTimerBackendLocal()` :331, `kotlinPumpBackendName()` :339; `internal external interface RoRenderThreadQueue` :273 with `@BrsInline` creator :281. Per-GetGlobalAA-scope singleton = per-component-instance on the render thread (:41–46).

**Render-thread guard precedent** for flowOn/spawnTask/StateFlow guided ISEs: `if (PumpScheduler.hostTopOrNull() == null) throw IllegalStateException(...)` — see `brs/shared/SharedService.kt:166,250`, `brs/shared/SharedStash.kt:110`, and `brs/scope/ComponentMailbox.kt:73` (`scopeAmbientTopOrNull`).

**DelayTracker** (`coroutines/delay/DelayTracker.kt`): `public class DelayTracker` :32 — `currentTimeMs(): Int` :51, `register(delayMs: Long, callback: () -> Unit)` :60 (notifies `PumpScheduler.onDelayRegistered`), `msUntilNextDeadline(): Int` :73, `tick(): Int` :96, `clear(): Int` :124, `hasPendingDelays()` :135, `companion { val current: DelayTracker }` :154 (per-thread via m-scope). **No deregistration** — a cancelled `delay`'s callback still fires and lands in the park's once-guard (Delay.kt:47–49 comment).

**Delay.kt**: `public suspend fun delay(timeMillis: Long)` :40 (entry-check + park + registerCallerCancel); `public suspend fun yield()` :87 (re-dispatch through interceptor; **no-op without one**); `processCoroutineDelays(): Int` :115.

## 6. Suspend-codegen surface the cold core leans on

**THE dominant constraint — stdlib gets NO suspend state machines.** `compiler/ir/backend.brightscript/src/org/jetbrains/kotlin/ir/backend/brs/lower/BrsLoweringPhases.kt:354-356`:
```kotlin
val fullCoroutinesAvailable = !context.isStdlibCompilation &&
    context.brsSymbols.coroutineSymbols.areCoroutineSymbolsAvailable
```
`BrsSuspendFunctionsLowering` (state-machine generation) runs ONLY for user code. Continuation-parameter addition (16.5.2–16.5.4) runs for stdlib too. Consequence, documented at `libraries/stdlib/brs/src/kotlin/brs/scope/ScopeHostImpl.kt:41-51`: *"stdlib compilation generates no suspend state machines … so a stdlib suspend lambda with a non-tail suspend call would miscompile."* A naive stdlib `map` (`upstream.collect { emit(transform(it)) }` — two non-tail suspend calls in a stdlib lambda) is EXACTLY this shape. The plan must decide how the cold core's operator bodies are compiled (hand-rolled continuations, the ScopeHostImpl start-the-USER's-CoroutineImpl pattern, or lifting the stdlib restriction) — this is not mentioned in the spec and is the largest hidden design constraint in this domain.

**How stdlib starts user suspend lambdas** (fully state-machined in user code):
- 0-arg / receiver form: `block.startCoroutine(receiver, completion)` (Builders.kt:225, used by `parkScopedBlock`).
- 1/2-param non-receiver: no public starter; cast to `CoroutineImpl` and `impl.create(a1[, a2], completion).intercepted().resume(Unit)` — `ScopeHostImpl.kt:341-368` (`startScopeHandler0/1/2`).
- `CoroutineImpl.create` overloads exist for **0, 1, and 2 value slots ONLY** (`coroutines/CoroutineImpl.kt:~155-180`; the 2-value overload was added specifically for ScopeHandle). Receiver counts as a slot: `suspend FlowCollector<R>.(T) -> Unit` = 2 slots (OK); `combine`'s `suspend (T1, T2) -> R` = 2 (OK); anything needing 3 slots has no `create` — would need a new overload + lowering support (see the KDoc at the 2-param overload re `superCreateFunction`).
- Raw lowered top-level suspend fn pointers follow `fn(args..., completion)` returning result-or-COROUTINE_SUSPENDED (`invokeScopeBinding`, ScopeHostImpl.kt:370-381 — relevant to the flowOn lift's shim invocation shape).

**fun-interface precedents:** only NON-suspend ones exist (`kotlin/Comparator.kt:11`, `kotlin/concurrent/Runnable.kt:14`). There is **no fun interface with a suspend member anywhere in the BRS stdlib** — `FlowCollector` as `fun interface { suspend fun emit }` + SAM conversion of user suspend lambdas is unexercised codegen. Suspend INTERFACE members do exist and work: `Job.join()` (Job.kt:44/324), `Deferred.await()` (Job.kt:349/424) — but all implementations are stdlib-side tail-park intrinsic blocks, never overridden by user code.

**Intrinsics** (`coroutines/intrinsics/IntrinsicsBrs.kt`): `suspendCoroutineUninterceptedOrReturn(crossinline block: (Continuation<T>) -> Any?): T` :162; `createCoroutineUnintercepted` :20/:38; `intercepted()` :57; `startCoroutineUninterceptedOrReturn` :68/:85 (note: BRS version always returns COROUTINE_SUSPENDED, :73). `Continuation.kt`: `resume` :32 / `resumeWithException` :43 are NON-inline (klib inline never inlines at user call sites); `suspendCoroutine` :68.

**runTask/task anchors for the lift + cancellation rider** (`coroutines/task/TaskRunner.kt`):
```kotlin
public class TaskException(message: String, public val number: Int, public val backtrace: Dynamic?) : RuntimeException(message)  // :51
internal object TaskRunner                                                          // :89 — allocateTaskId/hasPending/register/remove, keyed by kotlinTaskId
internal fun onKotlinTaskStateChanged(event: RoSGNodeEvent)                         // :118 — destroyed-node guard, unobserve-before-resume, park settle
public suspend fun <T : TaskComponent> T.awaitCompletion(): T                       // :204 — arm observer BEFORE reading state; cancel-cleanup handler at :246-252
@PublishedApi internal suspend fun <T : TaskComponent> runTaskImpl(task: T, configure: T.() -> Unit): T   // :273 — arm observer BEFORE control=RUN
public suspend inline fun <reified T : TaskComponent> runTask(noinline configure: T.() -> Unit): T        // :306 — call site REWRITTEN by BrsRunTaskCallLowering (klib inline never inlines)
```
`TaskComponent` (`brs/SceneComponent.kt:209-244`): `@BrsSceneGraphComponent(extends = "Task")`, protocol fields `kotlinTaskState` (`@SGStringField(alwaysNotify = true)`) :218, `kotlinTaskError` :227, `kotlinTaskId` :234, `protected abstract fun run()` :243. `createComponent` (`brs/ComponentFactory.kt:27`): `public inline fun <reified T : ComponentBase> createComponent(): T = brsCreateComponent<T>()`; `brsCreateComponent` :35 is backend-lowered. `brsName` (`brs/annotations.kt:452`): `public external fun <T : Function<*>> brsName(function: T): String`. Node surface for the doorbell (`brs/roku/SceneGraph.kt`): `getField(fieldName: String): Dynamic?` :82, `addField(fieldName: String, type: String, alwaysNotify: Boolean): Boolean` :99, `setField(fieldName: String, value: Any?): Boolean` :152, `observeFieldScoped(fieldName: String, functionName: String): Boolean` :201, `unobserveFieldScoped(fieldName: String): Boolean` :241.

QUARANTINED, do not build on: `coroutines/task/TaskPool.kt`, `IOWorkerRegistry.kt`, `CoroutineTask.kt`, `dispatchers/IODispatcher.kt`, `withContextIOSuspend` (WithContext.kt:136).

## 7. Where `kotlin.coroutines.flow` lives + build pickup

- **Directory**: create `libraries/stdlib/brs/src/kotlin/coroutines/flow/` (sibling of `builders/`, `pump/`, `task/`); `kotlin.coroutines.task` additions go in the existing `task/` dir. Package/dir naming follows path 1:1 by convention.
- **Build pickup is automatic**: both `regenerateKlib` (`libraries/stdlib/brs-prebuilt/build.gradle.kts:31-36,107-115` — compiles `brs/builtins`, `brs/runtime`, `brs/src`, `brs-actual/src` as one module named `stdlib` with `-Xallow-kotlin-package -Xstdlib-compilation`) and `generateStdlibBrs` (`libraries/stdlib/build.gradle.kts:~1047-1090`, same dirs, module `kotlin-stdlib-brs`) glob whole directories. `./rebuild.sh` drives both. The prebuilt klib + `.klib-source-hash` are checked in — commit both after regen.
- **Default imports**: only `kotlin.brs.*` is platform-default-imported (`brs/brs.frontend/src/org/jetbrains/kotlin/brs/resolve/BrsPlatformAnalyzerServices.kt:21-23`). `kotlin.coroutines.dispatchers.Dispatchers` etc. require explicit imports today (every E2E test file imports it). `kotlin.coroutines.flow.*` will need explicit imports unless the plan adds it to `computePlatformSpecificDefaultImports` — the spec's "one import swap from Android" implies explicit imports, which matches.
- `-Xstdlib-compilation` sets `isStdlibCompilation` → triggers the no-state-machine gate above AND null-tolerant symbol lookup (`BrsCoroutineSymbols.kt:29-50,169-178`).

## GOTCHAS (things a plan written without this file would get wrong)

1. **Stdlib suspend code gets NO state machines** (`BrsLoweringPhases.kt:354-362`). Every flow operator body written in stdlib source must be tail-delegating, single-intrinsic-park, or hand-driven via `CoroutineImpl.create`/`startCoroutine` (ScopeHostImpl precedent). A naive kotlinx-style `map`/`transform` implementation **miscompiles silently**. The spec's "heaviest exercise of suspend codegen" framing is about USER chains; the stdlib-side operator internals face a categorically different constraint the spec never states.
2. **No `coroutineContext` intrinsic in stdlib source** — entry checks must be `continuation.context.ensureActive()` inside the intrinsic block (TaskRunner.kt:233-236). Flow's per-emit cancellation check needs a continuation in hand at that point.
3. **`ParkedContinuation.finish()` must be the literal last expression**; parks start unarmed; `invokeOnCompletion` on a terminal job fires synchronously AT registration (Job.kt:156-161) — fast-path or rely on the unarmed latch.
4. **No captured mutable locals in stdlib closures** (no shared-box codegen) — mutable state in class-instance holders (`CountdownState` pattern, Await.kt:45-47). The internal SPSC/MPSC queue primitive must be a class, not captured vars.
5. **`Dispatchers` lives in `kotlin.coroutines.dispatchers`**, not `kotlin.coroutines` (spec §14 conflict); `Dispatchers.Main === Dispatchers.Default` (same object) — identity checks against Main also match Default.
6. **`CoroutineImpl.create` tops out at 2 value slots + completion** — receiver counts as a slot. All spec'd operator lambda shapes fit, but any 3-slot suspend lambda invoked from stdlib needs a new overload + lowering support (KDoc at the 2-param overload explains the pairing with `superCreateFunction`).
7. **klib inline functions never inline at user call sites** — `runTask` needed a dedicated call-site lowering (`BrsRunTaskCallLowering`, TaskRunner.kt:259-264). Any reified/inline flow API needs the same treatment; the spec already mandates non-inline operators — keep it that way.
8. **`delay`/`withTimeout` deadlines never deregister** (DelayTracker has no removal); rely on the park's settle-once + `isCompleted` guards (Timeout.kt:41-45 pattern) — a StateFlow collect that outlives timeouts will accumulate no-op callbacks only until they fire.
9. **CancellationException extends IllegalStateException** (cancellation/CancellationException.kt:14) — `catch (e: IllegalStateException)` in user code swallows cancellation; flow's `catch {}` upstream-only classification must exclude CE explicitly.
10. **Function-name observers resolve in the registering component's include closure** — a doorbell/task-state handler must live in a file the collector's component includes; call-dependency recording covers it because the collector calls into that file (TaskRunner.kt:109-116 comment). Keep the doorbell handler in the same file as the collect entry point.
11. **`yield()` without an interceptor is a no-op resume** (Delay.kt:87-100); in the runBlocking/runPumping no-interceptor regime, handler fire-during-block and inline resumes are normal — the cold-core stdlib suite (phase-1 gate) runs exactly there.
12. **Jobs/queue/pump/DelayTracker are per-component-instance** (GetGlobalAA); the doorbell observer firing in the collector's own context (Probe A) is what makes `CoroutineQueue.enqueue`-from-observer correct — never hand a park or Job across components.
13. **Arming-order + unobserve-before-resume + destroyed-node guard** are all embodied in `onKotlinTaskStateChanged`/`awaitCompletion` (TaskRunner.kt:118-161, 204-255) — the flowOn envelope observer should copy that structure verbatim, including the `taskEventNodeOrNull` invalid-node guard and the double-await/`hasPending` guards.
14. **`withContext` contains a live `=== Dispatchers.IO` branch** (WithContext.kt:70) — if `Dispatchers.Task` is a real `CoroutineDispatcher` value, nothing in `withContext` stops it from being merged into a context and intercepting resumptions; the "token, not dispatcher" law is enforced ONLY by the planned FIR rule (BRS_FLOW_ON_INVALID_DISPATCHER reversed-position clause). Consider a `dispatch` that throws a guided error as the runtime backstop.
15. **`runBlocking`'s loop spins on `!deferred.isCompleted`** (Builders.kt:112-118) with no port wait — a flow test that parks forever in the runBlocking regime busy-spins rather than deadlocking visibly; whole-test timeouts come from the kotlin.test driver, not runBlocking.