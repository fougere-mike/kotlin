# Kotlin BrightScript Backend Development Guide

## Project Overview

This is a fork of the Kotlin compiler that adds a BrightScript backend for Roku development. It compiles Kotlin source code to BrightScript (.brs) files that run on Roku devices.

## BrightScript Language Notes

**BrightScript is case-insensitive.** This means:
- `myFunction` and `MYFUNCTION` and `MyFunction` are all the same identifier
- `result` and `RESULT` and `Result` are the same variable name
- Method names like `resumeWith_Result_k_` and `resumeWith_RESULT_k_` are identical to BrightScript

This affects code generation - there's no need to use uppercase for "disambiguation" since case doesn't disambiguate anything in BrightScript.

**Bare `and` short-circuited on device** (probe-verified 2026-08-11, Roku Ultra
4800X, Roku OS 15.3.4 build 841): with a false boolean LHS, spliced
`probeArr <> invalid and probeArr.count() > 0` did NOT evaluate the RHS — no
crash on the invalid receiver (stdlib suite "Short-circuit semantics", PROBE
test). The compiler still emits guarded temps for Kotlin `&&`/`||` with an
effectful RHS regardless: the compile target floor is Roku OS 9.4 (older-OS
behavior unverified), and the pre-fix crashes came from the compiler hoisting
the RHS into an unguarded temp evaluated BEFORE the operator — a shape no
platform short-circuit can rescue.

## MANDATORY BUILD RULES

### The ONE Command: `./rebuild.sh`

**For ANY change to compiler or stdlib code, run:**

```bash
./rebuild.sh
```

That's it. This script:
1. Cleans BRS module build directories (forces ~42 BRS-specific tasks to re-execute)
2. Runs all 9 build steps every invocation — Gradle's up-to-date checks skip unchanged modules
3. Uses `-Pkotlin.build.useBootstrapStdlib=true` (prevents metadata version mismatch with bootstrap compiler)
4. Builds `cli-brs:fatJar` (BRS compiler, ~702 tasks — not the full `dist`)
5. Runs `regenerateKlib` and `generateStdlibBrs` using the fat JAR
6. Publishes all artifacts to Maven Local

**DO NOT** run manual cache-clearing commands. **DO NOT** run individual gradlew tasks.
If `./rebuild.sh` doesn't work correctly, that's an **infrastructure failure** - fix the script.

### Testing with roku-test-app

```bash
# From roku-test-app directory - full rebuild including compiler
cd ../roku-test-app && ./rebuild-all.sh --all
```

## INFRASTRUCTURE FAILURE PROTOCOL

When you see ANY of these, it is an **INFRASTRUCTURE FAILURE**:

| Symptom | What It Means |
|---------|---------------|
| UP-TO-DATE for modules you changed | Build cache served stale output |
| Stale timestamps in test output | Device logs from previous run |
| Changes not in generated .brs files | Build didn't include your changes |
| Test failures that don't match code | You're debugging the wrong version |

**MANDATORY RESPONSE:**

1. **STOP** - Do not run another command. Do not try to work around it.
2. **FIX** - Update rebuild.sh or run-tests.sh to prevent this
3. **DOCUMENT** - Add the fix to this file
4. **NEVER** work around infrastructure problems with manual commands

The tooling must work correctly. If it doesn't, fix the tooling.

### How rebuild.sh optimizes incremental builds

On every run, `rebuild.sh` deletes only the BRS compiler module `build/` directories
before invoking Gradle. This forces the ~42 BRS-specific tasks to re-execute while
the ~660 core Kotlin compiler tasks remain `UP-TO-DATE` from Gradle's build cache.

`regenerateKlib` and `generateStdlibBrs` track `cli-brs:fatJar` as a Gradle input.
If the fat JAR hasn't changed, those tasks are also `UP-TO-DATE`.

There are no marker files. There is no `--rerun-tasks`. Gradle's own incremental
build machinery is the source of truth for what needs rebuilding.

**BrightScript modules cleaned on each run:**
- `brightscript/brs.ast/build`
- `core/compiler.common.brightscript/build`
- `brs/brs.frontend/build`
- `compiler/ir/serialization.brs/build`
- `compiler/fir/checkers/checkers.brs/build`
- `compiler/ir/backend.brightscript/build`
- `compiler/cli/cli-brs/build`

### Forcing a full rebuild

Use `--clean` to force a full rebuild from scratch:

```bash
./rebuild.sh --clean
```

This removes all BRS build directories and Maven Local BRS artifacts before rebuilding.

## DO NOT

- **DO NOT** run manual cache-clearing commands - rebuild.sh handles this
- **DO NOT** run individual gradlew commands - use rebuild.sh
- **DO NOT** debug test failures without verifying log timestamps first
- **DO NOT** assume stale logs are showing your current code

## Compiler Architecture Context

- Kotlin uses a multi-stage compilation pipeline: Frontend (FIR) → IR → Backend
- BrightScript is a new backend alongside JVM, JS, Native, and Wasm
- The K2 (FIR) frontend is used; K1 is not supported
- Entry point: `K2BrsCompiler` in `compiler/cli/cli-brs/`

## Native BrightScript Type Interfaces

Native BrightScript objects (roArray, roAssociativeArray, etc.) are modeled as **external interfaces** rather than wrapper classes. This allows user code to pass native BrightScript objects directly to Kotlin functions without wrapping overhead.

### Key Concepts

**1. External Interfaces (not wrapper classes)**

Native types are declared as `external interface`, which tells the compiler the methods exist on the native object:

```kotlin
// In libraries/stdlib/brs/src/kotlin/brs/roku/NativeTypes.kt
public external interface RoArray : IArray, IArrayJoin, IArraySort, IEnumNative {
    companion object {
        @BrsCreateObject("roArray")
        fun create(size: Int, resize: Boolean): RoArray = definedExternally
    }
}
```

**2. @BrsCreateObject Annotation**

Marks companion object factory functions. The compiler transforms calls to `CreateObject()`:

```kotlin
// Kotlin code:
val arr = RoArray.create(10, true)

// Compiles to BrightScript:
arr = CreateObject("roArray", 10, true)
```

Do NOT declare default parameter values on `@BrsCreateObject` factories —
absent arguments at the call site currently misbind (the arg list compacts
positionally; FIR guard is backlog). Declare all parameters required.

**3. NativeArrayIterator Marker Type**

When a type's `iterator()` returns `NativeArrayIterator`, the compiler emits native BrightScript `for each` instead of the Kotlin iterator protocol:

```kotlin
public external interface NativeArrayIterator<out T>  // Marker interface

public interface NativeIterable {
    fun iterator(): NativeArrayIterator<Dynamic>
}

// Types implementing NativeIterable get native for-each:
for (item in roArray) { ... }  // → for each item in roArray ... end for
```

**4. Roku Interface Mappings**

BrightScript interfaces map to Kotlin interfaces:
- `ifArray` → `IArray`
- `ifArrayGet` → `IArrayGet`
- `ifArraySet` → `IArraySet`
- `ifEnum` → `IEnumNative`
- `ifAssociativeArray` → `IAssociativeArray`

### Key Files

| File | Purpose |
|------|---------|
| `libraries/stdlib/brs/src/kotlin/brs/annotations.kt` | `@BrsCreateObject` annotation definition |
| `libraries/stdlib/brs/src/kotlin/brs/roku/NativeTypes.kt` | Native type interfaces (RoArray, RoAssociativeArray, etc.) |
| `core/compiler.common.brightscript/.../BrsStandardClassIds.kt` | Class IDs for compiler recognition |
| `compiler/ir/backend.brightscript/.../BrsIntrinsics.kt` | `isNativeIterable()`, `returnsNativeArrayIterator()` |
| `compiler/ir/backend.brightscript/.../irToBrs/IrToBrsTransformer.kt` | Declaration/for-each handling (irToBrs package, post-B1 split) |
| `compiler/ir/backend.brightscript/.../irToBrs/IrExpressionToBrsTransformer.kt` | Expression codegen, @BrsCreateObject compilation |

### For-Each Loop Compilation Strategies

The compiler uses these strategies (in order) for `for (x in iterable)`:

1. **NativeIterable check**: If type implements `NativeIterable` → native `for each`
2. **NativeArrayIterator check**: If `iterator()` returns `NativeArrayIterator` → native `for each`
3. **Stdlib collections with get_array()**: ArrayList, IntArray, etc. → `for each x in obj.get_array()`
4. **Kotlin Iterable interface**: HashSet, Sequence, etc. → while loop with `iterator_k_()`, `hasNext_k_()`, `next_k_()`
5. **Default**: Native `for each`

**Reality check (backlogged):** strategies 1–2 are currently unreachable from user
code. `for (x in roArray)` does NOT compile: `NativeIterable.iterator()` lacks the
`operator` convention and `NativeArrayIterator` declares no `hasNext()`/`next()`,
so the frontend rejects the loop before the backend's strategy choice matters.
Indexing is also broken for native arrays: `RoArray` operator `get` emits a
`.get(i)` method call that native roArray does not have. Until both are fixed,
drain native arrays with `count()`/`shift()` or render them via `join()` —
see `roku-test-app/src/brsMain/kotlin/com/nuvyyo/roku/components/ShelfView.kt` (onShelfItemsChanged)
for the working pattern.

### Adding New Native Types

To add a new native BrightScript type (e.g., `RoList`):

1. **Define the interface** in `NativeTypes.kt`:
   ```kotlin
   public external interface RoList : IEnumNative {
       fun count(): Int
       fun addTail(value: Dynamic)
       // ... other methods from Roku docs

       companion object {
           @BrsCreateObject("roList")
           fun create(): RoList = definedExternally
       }
   }
   ```

2. **Add class ID** (if needed for compiler recognition) in `BrsStandardClassIds.kt`

3. **No wrapper class needed** - the external interface describes what the native object can do

## Typed Tasks (`runTask`) - THE Task-Boundary Mechanism

Work that must leave the render thread goes through a **typed task component**.
This is the sanctioned way to cross the render/task thread boundary —
`flowOn(Dispatchers.Task)` and `spawnTask {}` (see "Flow & StateFlow") are
compiler-synthesized typed tasks over the same mechanism.

**1. Declare the task** - subclass `TaskComponent`, annotate typed input/output
fields, put the task-thread work in `run()`:

```kotlin
class FetchShelfTask : TaskComponent() {
    @SGIntegerField
    var count: Int = 0            // input

    @SGArrayField
    var items: RoArray? = null    // output

    override fun run() {          // runs on the TASK thread
        val fetched = RoArray.create(0, true)
        for (i in 1..count) {
            fetched.push("$i")
        }
        items = fetched
    }
}
```

**2. Run it** from a render-thread coroutine — `launch {}` is available on every
component with zero imports and zero pump wiring (see "Component coroutine
scaffolding" below). `runTask` creates a fresh node, configures the inputs,
runs it on the task thread, and suspends until the task completes - resuming
with the completed node so typed outputs read directly:

```kotlin
launch {
    try {
        val task = runTask<FetchShelfTask> { count = 5 }
        shelfItems = task.items   // typed self-write works in lambda scope
    } catch (e: TaskException) {
        // run() threw on the task thread; message/number/backtrace preserved
        println("fetch failed: ${e.message}")
    }
}
```

(Historical note: lambda bodies used to hand results back through an aliased
`val node = top` + `setField`, because the compiler miscompiled typed
self-writes in lambda scope — the captured `this` is the component m-object,
and the write landed there as a dead AA key. Fixed 2026-08-10: the compiler
routes lambda-captured self access through `.top`; the alias workaround is
retired.)

Key facts:

- A failed `run()` surfaces as `TaskException` at the suspend point (fields of
  the BrightScript error object: `message`, `number`, `backtrace`).
- v1 constraints: must be called from render-thread component context; a fresh
  unparented node per invocation (no pooling); one-shot; no timeout yet. The
  await IS cancellation-aware, and cancellation now STOPS the task thread
  (the flow-program STOP rider, 2026-09-03 — the M3 "task-thread work is not
  stopped" item is CLOSED): caller cancel wakes the await promptly
  (CancellationException at the suspend point, registry entry dropped, observer
  disarmed) and writes `control="STOP"` on the abandoned node — a PROMPT hard
  kill (Probe B, spikes/flow-spike/FINDINGS.md: sub-second in every probed
  shape — sleep loop, mid-blocking-call, blocked wait(), compute loop;
  abandoned-node safe; repeated STOP idempotent; blocked sync roUrlTransfer is
  the one unpinned shape). Task-side code after the killed point — `finally`
  included — NEVER runs on the hard kill; see the dispose contract in "Flow &
  StateFlow". Scope note (recorded deviation (c)): plain `runTask` cancellation
  is the hard STOP alone — no cooperative cancel field is written on this path
  (cooperative checkpoints for plain typed tasks are recorded backlog);
  `flowOn`/`spawnTask` get the two-layer sequence.
- FIR diagnostics guard the pattern: `BRS_TASK_STATE_NOT_FIELD` (task state
  must be @SG interface fields - plain properties are lost across the node
  clone) and `BRS_CREATE_COMPONENT_INVALID_TYPE` (+ an "[IR] " backstop).
- Canonical example: `../roku-test-app/src/brsMain/kotlin/com/nuvyyo/roku/components/ShelfView.kt` +
  `FetchShelfTask.kt` (the flagship demo). Acceptance coverage: E2E Suite 4
  (TypedTaskAcceptance).
- Stdlib implementation: `libraries/stdlib/brs/src/kotlin/coroutines/task/TaskRunner.kt`.

### QUARANTINED: `withContext(Dispatchers.IO)` / TaskPool / runIOWorker

`withContext(Dispatchers.IO)`, `TaskPool`, and the `IOWorkerRegistry`/ioWorker
pipeline are **QUARANTINED - do not use**. That pipeline is unverified on
device, has zero E2E coverage, and its last consumer (ShelfView) was migrated
to `runTask` in Phase 2. `spawnTask {}` (see "Flow & StateFlow") IS the
recorded re-layering — it supersedes the quarantined pipeline; any new
`withContext(Dispatchers.IO)` use is a review-blocking regression.

The quarantine is COMPILER-ENFORCED: any `Dispatchers.IO` reference in user
code is a FIR ERROR (`BRS_IO_DISPATCHER_UNSUPPORTED` — "Use
flowOn(Dispatchers.Task) for background streams, spawnTask for one-shot
blocks, or runTask<T> for typed tasks").
Deliberate opt-ins into the quarantined pipeline must
`@Suppress("BRS_IO_DISPATCHER_UNSUPPORTED")`, which is exactly the review
signal the quarantine wants.

## ScopeHandle (cross-component scope borrowing)

Landed 2026-08-14 (the ScopeHandle Stage 2 program). A child component awaits
work that GENUINELY EXECUTES in an owner component's coroutine scope — the
sanctioned cross-component request/response mechanism. (Jobs are
same-component-only; @SG fields and observers are for signalling; ScopeHandle
is for awaiting owner-side work.) Design of record:
`docs/superpowers/plans/2026-08-12-scopehandle-design.md` + Addendum A.1–A.6;
spike truth in `spikes/scope-handle-spike/FINDINGS.md`. Device coverage: E2E
Suite 8 (ScopeHandle, 33 tests — both carriers, mixed pairs, cancellation both
directions, retire/re-expose/post-retire, the flagship child→owner→task-thread
chain) + the stdlib wire/registry unit suites.

### The two surfaces

**1. Hand-written requests** — declare a request object with an explicit wire
name, register a handler owner-side, run it from any child holding a handle:

```kotlin
object RefreshWatchlist : ScopeRequest<String>("RefreshWatchlist")   // 0-arg
object SlowAdd : ScopeRequest2<Int, Int, Int>("SlowAdd")             // arities 0–2

class VmHost : RectangleComponent() {
    private var host: ScopeHost? = null
    init {
        // an init-time expose stays CLOSED after retire→revive; recyclable owners expose in onStart
        host = exposeScope {                       // one OPEN host per component
            handle(RefreshWatchlist) { refreshInternal() }  // owner's code, owner's scope
            handle(SlowAdd) { a, b -> a + b }
        }
    }
    fun onDismiss() = host?.close()                // THE teardown convention
}

// Child side — handle minted from the owner's NODE, never a component ref:
val owner = scopeHandleOf(ownerNode)
val items = owner.run(RefreshWatchlist)            // suspends; executes owner-side
val sum = owner.run(SlowAdd, 2, 3)                 // args cross BY COPY
```

**2. Compiler-lowered blocks** — `owner.run { ... }` with a LITERAL lambda;
the compiler lifts the block to a named function and synthesizes the request:

```kotlin
val shelf = owner.run { buildShelf(genre) }   // genre crosses BY COPY
```

- Literal lambdas only: a stored function value is a FIR ERROR
  (`BRS_SCOPE_BLOCK_NOT_LITERAL`) — declare a `ScopeRequest` instead.
- Captures are marshalled BY COPY into the request payload; the marshallable
  set below applies (`BRS_SCOPE_CAPTURE_UNMARSHALLABLE` guards it).
- Dispatch needs the block's FILE in the owner's include closure: the compiler
  injects a binding table (`__kotlinScopeBindingsInstall`) into the generated
  `init()` of every component whose file calls `exposeScope`. The table is
  filled MID-TRANSFORM, so it covers the lifted blocks visible in the closure
  at that point — a SUBSET of the final include closure (which is resolved in
  a dedicated pass after all files transform): a `run {}` block in a project
  file that transforms after the owner (a second-hop helper) can be silently
  absent from the table and gets the guided dispatch-miss below (Pass-3
  backfill of the table is backlogged; KDoc'd at
  `BrsCompiler.populateScopeBindingTables`). The VM-facade pattern makes
  coverage automatic in practice (the block lives in the VM class file; the
  owner includes it by constructing the VM). A miss is never a hang: the child
  gets an immediate `ScopeRequestException` naming the fix ("declare the
  operation in a file the owner includes — typically your VM class").
  Cross-module `run {}` blocks get the guided miss too — single-module only
  today (backlog). KNOWN HOLE: the binding-table injection is gated on a
  per-file scan for `exposeScope` CALLS, so an `exposeScope` reached only via
  another file's helper escapes it — no binding table is injected; hand-
  registered requests still work, and `run {}` blocks get the guided
  dispatch-miss error (KDoc'd at `IrToBrsTransformer.fileCallsExposeScope`).

**v1 laws (both surfaces):** render-thread component callers only (like
`runTask`); no timeouts in the API — compose with `withTimeout`; one OPEN
`exposeScope` host per component — a second call while a host is OPEN throws;
after `retire()` closes it (via a scope-package retire hook) `exposeScope` may
be called again, so recyclable owners expose in `onStart` (an init-time expose
stays closed after `revive`; Suite 8 scopeReExposeAfterRetireAndRevive);
request names unique per
owner, `'#'` reserved for compiler-lowered block names; handles are
construction-context-free (mint owner-side, inject into a plain-class VM, call
from any child — all caller-side machinery resolves from the ambient component
at each `run`); `run` against a node that never exposed a scope fails fast
with `IllegalStateException`. Failures cross as DATA:
`ScopeRequestException(message, number, backtrace)` — exception TYPES never
cross a component boundary; domain failures needing typed handling belong in
result values. Caller cancellation sends a best-effort cancel envelope and the
owner cancels the request job (a `runTask` the request was awaiting now STOPs
its task thread underneath — the flow-program STOP rider). A request from the
owner to ITSELF dispatches locally
(same-component fast path) with wire-identical semantics — including by-copy
args (`deepCopyAA`).

### The teardown LAW: `host.close()` before retiring an owner node

`close()` (idempotent) tears down the EXPOSED scope only. `retire(node)`
(lifecycle program, 2026-09-09) runs it for you through a scope-package retire
hook BEFORE the component-scope cancel — the recyclable-owner path; the manual
`host.close()` remains for owners torn down without `retire`. The default exposed
scope is a DEDICATED child supervisor scope of `componentScope()` — close()
never touches the owner's unrelated coroutines (device-pinned:
scopePostCloseOwnerLaunchAlive), and component-scope cancellation still
cascades into exposed requests. An explicitly-passed scope is cancelled
as-given — the caller owns its blast radius.

- In-flight requests settle "closed": the child's `run` throws
  `ScopeClosedException` (extends `CancellationException`, so an uncaught one
  winds the child down quietly — TimeoutCancellationException precedent).
- Post-close requests are answered "closed" immediately, for as long as the
  node lives (the inbox stays armed). Post-RETIRE requests likewise (Suite 8
  scopePostRetireRequestSettlesClosed — prompt, not via the watchdog): retire
  leaves the closed host installed and does NOT strip the inbox observer, so a
  dual-role owner+child keeps its child-role observer too.
- UNSIGNALED owner death (node retired without close()) cannot be signalled on
  this platform — writes to dead receivers drop silently. The child's watchdog
  makes it diagnosable: after 30s (default) pending, ONE console line per
  request, exact format:

  ```
  [kotlin.coroutines] ScopeHandle request <uuid>#<n> to owner <Subtype>(id=<id>) still pending after 30s (caller: <Subtype>) — owner torn down without close()?
  ```

  Once-only per request, never re-arms (a settled request makes the deadline
  callback a registry-miss no-op). Hooks: `kotlinScopeWatchdogMillis(ms)`
  (per-component override), `kotlinScopeWatchdogFires()` (counter — assert the
  counter, not line text, in sub-second tests).

### The marshallable set (what crosses the boundary)

Device-pinned truth (spike Q1d; the FIR family below enforces it at compile
time):

| Value | Crossing behavior |
|---|---|
| Primitives, String, Dynamic | Cross fine (by copy) |
| RoAssociativeArray / RoArray | Cross by DEEP copy (every level — owner-side mutation never propagates back) |
| Node refs (RoSGNode, ContentNode) | Cross BY REFERENCE on every carrier |
| Function values | STRIPPED (dropped or Invalid — never callable) |
| Class instances (incl. data classes, ArrayList/HashMap, component `this`) | HUSK: data keys survive, every method slot stripped; first method call crashes "Member function not found" |

**The husk trap:** `as?` PASSES on husks (the `is`/`as` machinery walks
`__proto`, which is plain data and survives the copy) — Kotlin's type check is
NO liveness guard; the failure surfaces only at first dispatch.

**The FIR family** (all fixture-pinned in the checkers.brs suite):

| Diagnostic | Severity | Fires on |
|---|---|---|
| `BRS_SCOPE_BLOCK_NOT_LITERAL` | ERROR | `run { }` argument that isn't a literal lambda |
| `BRS_SCOPE_CAPTURE_UNMARSHALLABLE` | ERROR | run-block capturing a function-typed value or class instance |
| `BRS_SCOPE_ARG_NOT_MARSHALLABLE` | ERROR | `ScopeRequest1`/`ScopeRequest2` DECLARATION whose `A1`/`A2` type argument is outside the marshallable set (no call-site checker — call sites are constrained by the declaration's generics) |
| `BRS_SCOPE_CAPTURE_MUTATION_LOST` | WARNING | run-block assigning to a captured `var` (copies — the write never reaches the caller) |
| `BRS_SCOPE_RESULT_NOT_DATA` | WARNING | request/block result type that loses behavior crossing the hop |

### Carrier: field floor + rtq fast path

Two transports, one protocol (kind-tagged envelope AAs: request/cancel/
outcome; unknown kinds ignored silently — forward-compat, decision 9):

- **Field floor** (every OS): per-node `__kotlinScopeInbox` AA fields with
  scoped observers.
- **roRenderThreadQueue fast path** (Roku OS 15.0+): one per-instance channel
  per component (`kotlin.scope.<uuid>` — NEVER the pump's channel), detected
  once per app session, cached on the global node under `__kotlinScopeBackend`
  (NEVER the pump's field) and memoized per-component. PostMessage MOVE
  semantics are safe here: envelopes are built fresh per post, and nested
  values copy — the by-copy law holds on this carrier with no explicit
  deepCopyAA (spike-pinned move-vs-copy rule).
- **Registration-before-advertisement law** (both sides, both carriers): the
  owner's inbox observer/channel handler is armed strictly BEFORE the
  advertisement (`__kotlinScope` = `"field"` or `"rtq:<channelId>"`) lands on
  the node; the child's reply inbox/channel is armed before its first post.
- **Mixed interop is supported and device-pinned** (Suite 8 dual-backend
  tests): transport TO a peer always follows the PEER's declaration — the
  owner's ad says how to post requests; the request's reply key
  (`replyTo` node ref vs `replyToChannel` id) says how to post outcomes. A
  field-forced child interoperates with an rtq owner and vice versa.

Test hooks: `kotlinScopeForceFieldBackend(global)` (session-wide),
`kotlinScopeForceFieldBackendLocal()` (calling component only — how the E2E
constructs mixed pairs), `kotlinScopeBackendName()` ("none" until resolved,
then "rtq"/"field"), plus the watchdog pair above.

### Documented patterns

- **Big results land in shared VM state; responses stay small.** The response
  envelope is a by-copy hop; share bulk via the VM (or node refs).
- **Build ContentNode trees task-side** and pass the ROOT node ref — node refs
  cross every channel by reference on every supported OS.
- **Return values — don't mutate captures.** Captures cross by copy; an
  owner-side write to one is silently lost (`BRS_SCOPE_CAPTURE_MUTATION_LOST`).

### Key files

| File | Purpose |
|------|---------|
| `libraries/stdlib/brs/src/kotlin/brs/scope/ScopeApi.kt` | User API: `exposeScope`, `ScopeHandle.run`, `scopeHandleOf`, test hooks |
| `libraries/stdlib/brs/src/kotlin/brs/scope/ScopeHostImpl.kt` | Owner half: dispatch, single egress, close(), same-component fast path |
| `libraries/stdlib/brs/src/kotlin/brs/scope/ComponentMailbox.kt` | Child half: request keys, parked awaits, watchdog |
| `libraries/stdlib/brs/src/kotlin/brs/scope/ScopeWire.kt` | Envelope contract + `deepCopyAA` |
| `libraries/stdlib/brs/src/kotlin/brs/scope/ScopeRtqBackend.kt` | rtq carrier: detection, channels, reply-address duality |
| `compiler/ir/backend.brightscript/src/.../lower/BrsScopeRunBlockLowering.kt` | `run {}` block lifting + binding-table synthesis |
| `compiler/fir/checkers/checkers.brs/src/.../BrsScopeMarshallability.kt` + Scope checkers | The FIR family above |

Canonical example: `../roku-test-app/src/brsMain/kotlin/com/nuvyyo/roku/components/fixtures/ScopeOwnerProbe.kt` +
`ScopeChildProbe.kt` + `ScopeVmFixture.kt` (owner, child, and the VM-facade
layering the design ships for).

## SharedService (reference-shared classes)

Landed 2026-08-14 (the SharedService program). An OWNER component publishes a
live class instance onto a node it controls; any component holding that node
acquires THE SAME instance — genuine shared identity over SetRef/GetRef, never
a copy, never a husk. This is the cross-component OBJECTS mechanism (shared
ViewModels, services); ScopeHandle above is the cross-component EXECUTION
mechanism — they compose (a VM holding a ScopeHandle is the canonical
layering). Design of record:
`docs/superpowers/plans/2026-08-14-shared-service-design.md`. Device coverage:
E2E Suite 9 (SharedService, 13 tests — sharing acceptance, static dispatch
cross-component, the CANARY) + the stdlib SharedService unit suite.

### The API (package kotlin.brs, default-imported)

```kotlin
abstract class ViewModel : SharedService() {            // APP-owned vocabulary + common code
    protected fun log(t: String, m: String) { ... }     // final member: static call
    abstract fun onAction(action: String)               // hook: dispatcher-served
}
class ApiClient(baseUrl: String) : SharedService() { ... }
class GuideVm(private val owner: ScopeHandle, private val api: ApiClient) : ViewModel() {
    var selectedDay: Int = 0
    override fun onAction(action: String) { ... }
    suspend fun refresh(): Int = owner.run { ... }      // composes with ScopeHandle
}
// Scene bootstrap:      shareOn(top, ApiClient("https://..."))         // app-wide services live on the SCENE
// Owner screen:         shareOn(top, GuideVm(scopeHandleOf(top), api))
// Any component:        val api = sharedFrom<ApiClient>(top.getScene())
// Child of the screen:  val vm  = sharedFrom<GuideVm>(screenNode)      // node via getParent()/findNode/@SGNodeField
```

Surface: `shareOn(node, instance[, key])` / `sharedFrom<T>(node[, key])` /
`sharedFromOrNull<T>(node[, key])` / `SharedService.isLive()` +
`isLive(node)` / `canShare()`. ONE stdlib marker type (`abstract class
SharedService`) — the toolchain has no ViewModel opinion; apps mint their own
vocabulary on top. REAL base-class hierarchies are legal (members, overridable
hooks, any depth); the only structural rule is concrete descendants are final
(FIR-enforced below).

- **Stash:** one runtime-added AA field `__kotlinShared` per publishing node
  (key → instance entries + the reserved `__gens` counters map), ALWAYS
  written via SetRef — an ordinary setField would copy. Keys default to the
  class name (the `__proto` head, the same string `is`-checks use; reified
  `sharedFrom<T>` call sites are REWRITTEN by the backend —
  `BrsSharedFromCallLowering` — because klib inline functions never inline at
  user call sites); explicit `key` covers two-instances-of-one-type. Keys
  starting `__` are RESERVED — guided ISE at `shareOn`.
  Informational: the FIRST publish on a node prints a benign platform
  console-warning pair ("Warning occurred while calling canGetRef" / "Tried
  to set nonexistent field") from shareOn's canGetRef probe — pre-existing,
  harmless, expected on every first publish (don't chase it in boot logs).
- **Live by construction:** `sharedFrom` returns the GetRef reference; no API
  path returns a copy. Acquire type-checks the entry (proto walk): a
  wrong-type entry is a guided ISE naming it ("key collision; use explicit
  keys") in BOTH the throwing and orNull variants — a collision is a caller
  bug, not an absence. Nothing shared: guided ISE ("shareOn(node, instance)
  in the owner first, or use sharedFromOrNull"); `sharedFromOrNull` answers
  null.
- **Republish replaces** (same key, same node): the prior generation's
  `isLive` goes false; holders re-acquire. Pairs with the
  recreate-don't-reuse screen convention — a retiring screen takes its VM
  with it (and ScopeHandle's close-is-terminal law applies to any host the VM
  fronted); a fresh screen constructs + publishes a fresh VM, never revives
  one. No unpublish/retract API in v1 — republish + node death cover it
  (backlog).
- **v1 laws:** render-thread component callers only (guided ISE otherwise —
  runTask/ScopeHandle precedent). OS 15.0+ floor: `shareOn` AND `sharedFrom`
  THROW the guided floor ISE pre-15 ("SharedService requires Roku OS 15.0+
  (SetRef) — the MVVM-layer floor decision (design A4); gate with
  canShare()"); `sharedFromOrNull` answers a truthful null pre-15 (nothing
  can ever be shared there — degrade-gracefully callers need no canShare gate
  of their own). Single-module closed world (dispatch section below).

**Input timing:** node handles arrive via `@SGNodeField` + `@BrsOnChange`
(acquire in the onChange), or — since the lifecycle program, 2026-09-09 — in
`override suspend fun onStart()`: for a layout child it runs after the parent's
`init()` (device order child-init < parent-init < child-start, Suite 11 test 1),
so a parent that publishes in `init` is visible there. Never acquire in `init`
(SG sets fields after creation). Design decision 10's `createComponent<T> { }`
+ `@SGRequired`/`onInputsReady()` pair is SUPERSEDED: constructor `@SG` inputs
LANDED 2026-09-09 (see "Constructor Inputs and Typed Layout Builders"). Scene-stash
idiom for app-wide services: publish on
the SCENE at bootstrap, acquire anywhere via `sharedFrom<T>(top.getScene())`.

**Publish-in-lambda (FIXED 2026-08-14):** `launch { shareOn(top, vm) }` used
to crash — a `top` READ inside a lowered lambda emitted an uninstalled
`__get_top_k_()` ("Member function not found"). The compiler now routes
component-scope property reads (`top`/`global`/`m`, plus layout-stub
properties) through the captured self (`m.this_0.top`), the same mechanism as
the case-8 @SG-write routing — no hoisting needed. Pinned by the
lambdaComponentScopeRead golden + E2E Suite 6 `lambdaScopePropertyReads`; the
Suite 9 fixtures' `val node = top` hoists are retired.

### isLive: generation stamps, two forms

Liveness is GENERATION-STAMP based — per-key counters in the stash's `__gens`
map + a `__sharedGen` stamp on the instance — NOT IsSameObject identity: the
identity oracle is device-FALSE for nested stash entries across separate
GetRefs (platform facts below), so the design's original oracle was unusable.
Semantics, both forms: never shared → false; replaced by a republish → false;
current generation → true; stash unreachable (node destroyed, off-context,
crippled handle) → false, never a crash.

- `isLive()` (no-arg) checks via the PUBLISH-TIME node handle — reliable
  OWNER-side. In a consumer the stored handle may have lost GetRef capability
  crossing inside the SetRef graph → answers false (KDoc'd).
- `isLive(node: RoSGNode)` (caller-supplied handle — the node you acquired
  from) — reliable both sides. THE consumer form.

Residual (KDoc'd): a same-generation instance that crossed a COPYING channel
(a husk) would still stamp-match — acceptable because copying channels are
compile errors for shared types (rule 3 below).

### Device-pinned platform facts (new facts of record, 2026-08-14, Suite 9 diagnostic runs 1–8)

- **GetRef-handle READS are live; INSERTS through the handle COPY**
  (intra-thread, fn slots intact — a silent identity break, not a husk).
  `shareOn` therefore rebuilds the stash as a fresh LOCAL AA + re-SetRef on
  every publish: instances reach the stash only via plain local insert +
  SetRef, the identity-preserving primitive.
- **roUtils.IsSameObject answers false for NESTED stash entries** reached via
  two separate GetRefs — while state sharing on the same entries is green in
  the same runs. The scope-handle spike had pinned IsSameObject-true only for
  the TOP-LEVEL SetRef'd AA.
- **A node handle nested inside a SetRef'd graph loses its GetRef capability
  on the receiving side** (`canGetRef` false / `getRef` Invalid) while the
  SAME node passed over ordinary channels works. Detach-vs-capability
  discriminator probe = spike bait (named in SharedService.kt's isLiveAgainst
  comments).

### The dispatch model (no normal path reads a fn slot)

Methods of shared classes emit as receiver-first globals —
`GuideVm_onAction_Str_k_(m, action)`; suspend members append `_completion`
LAST (and the mangle includes the Continuation parameter). Call sites: a FINAL
method → direct static call; an open/abstract method reached through a base
static type (including base-internal `this.hook()` template calls) → a
generated dispatcher `<implName>__dispatch(recv, args...)` that reads
`recv.__proto[0]` and if-chains over the compilation's concrete descendants
(source-name order) with a guided closed-world else-arm; `super.f()` → direct
static call to the base impl (the old self-recursive super emission is
FIXED). Simple val/var reads stay direct member access; non-trivial/open
accessors get the same static + dispatcher treatment (`__get_X` shapes).
Wrapper slots are still attached under the original slot names for the
residual shapes. Machinery: `BrsIntrinsics.isSharedServiceClass` (the sole
predicate root) + `BrsSharedDispatchLowering` (registry, call-site
classification, dispatcher generation — its header documents why it is NOT a
pipeline pass).

**Residual slot paths** (documented — the CANARY watches these): receivers
statically typed `Any` or an interface; `toString()` on any shared receiver
(the Any toString interception wins — never static; hand-written
`equals`/`hashCode` overrides on class-typed receivers go static/dispatcher
and resolve correctly, with ONE rung exception: a dispatcher rung that
resolves to a DATA-class leaf's GENERATED structural override emits a SLOT
call through the leaf's simple-named attachment — no mangled global exists
for generated members, a mangled rung was fix-wave-2 finding D1 — so that
rung joins this residual family); member extensions; data-class
generated-member NAMES on a shared DATA class
(equals/hashCode/toString/copy/componentN — excluded by NAME, hand-written
same-named members included, because the data-class emitters own those names
and emit simple-named globals; `isDataClassGeneratedMemberName` is the single
source); dependency-klib open members.

**Include-closure:** static calls and dispatchers record file dependencies
automatically — no include anchor needed; a data-only acquirer records
nothing and needs nothing; a dispatcher makes the BASE's file pull ALL leaf
files (closed-world consequence — EXCEPTION: data-class leaves, whose slot
rungs record no dependency; the leaf's file arrives via its constructor
call). Single-module closed world today — a
separately-compiled module's shared subclass would be invisible to
dispatchers; promote to a FIR diagnostic when multi-module becomes real
(backlog, same note as ScopeHandle's `run {}` blocks).

### CANARY: `sharedCanaryFnSlot` (E2E Suite 9)

Pins Roku's officially-disclaimed fn-ref-through-SetRef behavior by invoking a
wrapper slot AS a slot cross-component (a `@BrsInline` splice in
SharedConsumerProbe — deliberately immune to call-site-lowering evolution).
RED here + everything else green = Roku changed the disclaimed behavior;
normal operation is unaffected (no normal path reads fn slots). Runbook:
verify tests 8–12 (sharedFinalStaticCall through sharedSuspendMember) green,
then the wrapper-slot residual paths are dead — schedule their removal and
retire the canary. Do NOT "fix" the test. (Informational: tests 8/11/12 would
catch a correct-but-slot call-site regression only via include-closure
collapse — "Function is not defined in component's namespace" — not shape
assertions; the shape pins live in the sharedEmission/sharedDispatch
goldens.)

### The FIR family (fixture-pinned in the checkers.brs suite)

| Diagnostic | Severity | Fires on |
|---|---|---|
| `BRS_SHARED_CLASS_NOT_FINAL` | ERROR | a CONCRETE SharedService descendant declared `open` (dispatchers enumerate concrete leaves; member-bearing abstract bases are LEGAL — the rule is purely structural) |
| `BRS_SHARED_FN_PROPERTY` | ERROR | a function-typed property anywhere in a shared hierarchy (a stored callback is a fn ref in the shared bag — the one shape static dispatch cannot rescue) |
| `BRS_SHARED_THROUGH_COPYING_CHANNEL` | ERROR | a SharedService-typed value into a copying channel: `setField` value args and `callFunc` args (both unwrap `asDynamic()`/`unsafeCast()` first), plus `@SG*Field`/`@BrsField` DECLARATIONS — task components only (the render/task clone is the statically checkable copying hop) |

Rule 3's four disclosed STATIC holes (under-approximation by design,
BrsScopeMarshallability precedent): values pre-erased to `Any`/`Dynamic`
before the call site; `setFields(aa)` (the plural form); observer `getData()`
values; generic `T : SharedService`-typed values (ConeTypeParameterType).
ScopeHandle's channels need NO new rule — its marshallability checkers
already classify shared types as unmarshallable. The shared-class predicate
is MIRRORED, not imported (module boundary): checkers.brs
`BrsSharedServiceTypes` ↔ backend `BrsIntrinsics.isSharedServiceClass`, with
cross-referencing comments both sites — divergence law: any change lands in
BOTH in the same commit.

### Test hooks

`canShare(): Boolean` — true iff ambient render-thread component context AND
the OS 15.0 reference APIs exist (`CreateObject("roUtils")` probe — the
crash-free detection family; cached per component instance). Never throws;
the gate for apps that degrade features on older devices.

### Key files

| File | Purpose |
|------|---------|
| `libraries/stdlib/brs/src/kotlin/brs/shared/SharedService.kt` | Marker class, `isLive` (both forms), `shareOn`, `canShare`, `sharedFrom`/`OrNull` |
| `libraries/stdlib/brs/src/kotlin/brs/shared/SharedStash.kt` | Stash layout + `sharedAcquire` (the guided messages), key derivation |
| `libraries/stdlib/brs/src/kotlin/brs/roku/SceneGraph.kt` | SetRef/GetRef/CanGetRef/Move bindings + `RoUtils` (OS 15 reference APIs) |
| `compiler/ir/backend.brightscript/src/.../lower/BrsSharedFromCallLowering.kt` | Reified `sharedFrom` call-site rewrite |
| `compiler/ir/backend.brightscript/src/.../lower/BrsSharedDispatchLowering.kt` | Dispatch registry + call-site classification + `__proto`-name dispatchers |
| `compiler/fir/checkers/checkers.brs/src/.../BrsSharedServiceTypes.kt` + Shared checkers | The FIR family above (mirrored predicate) |

Canonical example: `../roku-test-app/src/brsMain/kotlin/com/nuvyyo/roku/components/fixtures/SharedOwnerProbe.kt`
+ `SharedConsumerProbe.kt` + `SharedFixtures.kt` (owner, consumer, and the
base-with-hook + final-subclass hierarchy Suite 9 exercises on device).

## Component Coroutine Scaffolding: `launch {}` + the self-scheduling pump

Coroutines in SceneGraph components need ZERO wiring — no pump timers, no
`processCoroutineQueue()` calls, no dispatcher choice:

```kotlin
class ShelfView : RectangleComponent() {
    init {
        launch {                                   // no imports needed
            val task = runTask<FetchShelfTask> { count = 5 }
            shelfItems = task.items
        }
    }
}
```

How it works:

- `ComponentBase.launch {}` / `componentScope()` (`kotlin.brs`,
  default-imported) give every component a lazy per-instance scope
  (`Dispatchers.Main + Job`, stored via the per-instance GetGlobalAA).
- The **PumpScheduler** (`libraries/stdlib/brs/src/kotlin/coroutines/pump/`)
  wakes the render thread exactly when work exists: `CoroutineQueue.enqueue`
  and `DelayTracker.register` notify it; it schedules ONE wakeup per burst.
  Backends: **roRenderThreadQueue** (Roku OS 15.0+, per-instance channel,
  sub-frame latency — `spikes/render-thread-queue-spike/FINDINGS.md`) with a
  **one-shot Timer** fallback (<15). `delay()` arms a one-shot timer at the
  NEXT deadline (no 10ms polling). Idle components run zero timers. Backend is
  detected once per app session (cached on the global node).
- The compiler injects `__kotlinComponentAttach(m.top, m.global)`
  UNCONDITIONALLY as the FIRST statement of every render component's generated
  `init()` — abstract user intermediates included; the WHOLE task hierarchy
  (abstract task intermediates included) excluded, since task threads have no
  pump. It performs the pump attach internally
  (`libraries/stdlib/brs/src/kotlin/brs/lifecycle/ComponentLifecycle.kt`), so
  legacy `CoroutineScope(Dispatchers.Main).launch` auto-pumps too. The former
  per-file coroutine scan, its helper-file KNOWN HOLE, and `__kotlinPumpAttach`
  itself are GONE (2026-09-09, the lifecycle program — golden
  `componentAttachUnconditional` replaced `componentNoCoroutinesNoPumpAttach`);
  see "Component Lifecycle" below.
- Test hooks: `kotlinPumpBackendName()`, `kotlinPumpForceTimerBackend(global)`
  (session-wide), `kotlinPumpForceTimerBackendLocal()` (one component — used
  by the PumpBackendProbe E2E fixture so both backends stay device-covered).
- Do NOT hand-write pump timers or call `processCoroutineQueue()` /
  `processCoroutineDelays()` from components — those entry points are for
  run-loop OWNERS only (main-thread `runBlocking`, the kotlin.test driver's
  `runPumping`). Task-thread `run()` bodies are synchronous by design: no
  `launch {}` there.
- On top of this scaffolding sits the full utility surface — `join`/`await`,
  `awaitAll`, `coroutineScope`, `withContext`, `withTimeout`, cancellation —
  documented in "Coroutine Utilities" below (landed 2026-08-11).

Historical note (root cause, 2026-08-10): before this scaffolding, dispatch
through the coroutine queue had NEVER worked on device — `CoroutineDispatcher`
stored itself under its own companion key while every framework lookup queried
`ContinuationInterceptor.Key` (identity matching), so `intercepted()` returned
null and ALL bodies/resumptions ran inline. The hand-written 10ms pump timers
were only ever servicing `DelayTracker`. Fixed by storing dispatchers under
`ContinuationInterceptor.Key` (CoroutineDispatcher.kt); pinned by stdlib tests
("dispatcher resolvable via ContinuationInterceptor key") and the E2E backend
assertion.

## Component Lifecycle: `onStart`/`onStop`, `retire`/`revive`

Landed 2026-09-09 (plan A of the component-lifecycle program). Components get
a "dependencies ready" moment and a REVERSIBLE teardown. Design of record:
`docs/superpowers/plans/2026-09-04-component-lifecycle-design.md`; spike truth
in `spikes/lifecycle-spike/FINDINGS.md`. Device coverage: E2E Suite 11
(ComponentLifecycle, 9 tests) + Suite 8's retire/re-expose/post-retire trio +
the stdlib `ComponentLifecycle (awaitReady/retire/revive)` unit suite.

```kotlin
class GuideScreen : GroupComponent() {
    init { /* ONE-TIME structural setup only */ }
    override suspend fun onStart() {                 // once per ACTIVATION, after the gate
        val host = exposeScope { handle(Refresh) { refreshInternal() } }  // recyclable-owner idiom
        val vm = sharedFrom<GuideVm>(top.getParent())
        launch { vm.refresh() }
    }
    override fun onStop() { /* synchronous; live state still readable; don't launch here */ }
}
// Parent side — render thread OR main thread (callFunc rendezvouses into the child):
retire(node)   // onStop → retire hooks (host closes) → cancel+reset scope → retired
revive(node)   // after re-adding: reset tickets → re-arm → re-check gate → driver again
```

### The two hooks and the compiler-synthesized driver

- **`onStart()`** (`protected open suspend` on ComponentBase) fires once per
  ACTIVATION, on the render thread, as a child coroutine of `componentScope()`,
  after `init()` has returned AND the gate below is open. Earliest: the first
  pump tick after init — for an XML-declared layout child that is AFTER the
  parent's `init()` (device order child-init < parent-init < child-start,
  Suite 11 test 1). Fires again after every `revive`. A failure prints the
  standard `[kotlin.coroutines] Unhandled exception in coroutine:` line and the
  component stays alive; `retire` cancels an `onStart` still running.
- **`onStop()`** (`protected open`, plain) runs synchronously inside `retire`,
  BEFORE the scope cancel, so live state is still readable; try/caught
  (`[kotlin.lifecycle] onStop threw: <message>`, retire continues). Launching
  here is pointless — the scope dies next.
- **The driver is compiler-synthesized**, ONLY for concrete render components
  whose hierarchy overrides `onStart` (`BrsIntrinsics.hierarchyOverrides`):
  `BrsComponentLifecycleLowering` (phase 0.054 — BEFORE the coroutine
  lowerings so the lambda gets a real state machine; the stdlib cannot host it,
  no state machines in stdlib compilation) adds a member `__kotlinStartDriver()`
  = `if (!kotlinLifecycleClaimDriver()) return; launch { awaitReady(); onStart() }`
  and calls it as the LAST init statement. The claim is SYNCHRONOUS and once
  per activation: SceneGraph runs EVERY level's `init()` over the shared `m`,
  base first, so a concrete base + concrete leaf launch exactly ONE driver, and
  its body runs after the whole init cascade when the leaf's slot is attached →
  the most-derived override by construction (Suite 11 test 2).
- **Per-instance cost is statically gated** (spec decision 10): no hook
  override → the attach only (one call, two ref stores); input-bearing types
  pay one boolean marker field (`__kotlinInputsReady`); only hook-overriding types pay a
  coroutine per instance. Informational pin: 1000 `LifecycleDumbProbe`
  instances with attach = 598 ms (Roku Ultra 4800X; Suite 11
  `timingThousandDumbInstancesInformational`, `[LIFECYCLE-INFO]` console line).

### The gate: `awaitReady()`

`ComponentBase.awaitReady()` (`kotlin.brs`, public) suspends until the current
activation's gate is open: not retired AND inputs ready AND every registered
dependency ticket resolved. Re-entrant (returns immediately once open);
render-thread component context only (guided ISE "awaitReady must be called
from a render-thread component context (like runTask)"). Public so a coroutine
launched from an observer handler can await it too. Inputs readiness reads the
`__kotlinInputsReady` marker field, which every INPUT-BEARING type declares
since plan B (2026-09-09; a type without the field is inputs-ready by
definition — see "Constructor Inputs and Typed Layout Builders"); tickets are
spec 2's interface (design §13), not yet registered by anything. So today the
gate parks for exactly one reason besides `retired`: an input-bearing node
whose marker nobody wrote (created outside its Kotlin constructor / static
layout). That park/wake/
watchdog path (marker observer wake, retire cancels a parked driver with
CancellationException, one watchdog line per activation) is DEVICE-PINNED by
Suite 11 `rawCreatedInputComponentStaysClosedUntilMarkerAndWatchdogFires` +
`retireCancelsOnStartParkedInAwaitReady`; ticket resolution and the revive wake
remain inspection-verified against the ParkedContinuation idiom. The stdlib
`ComponentLifecycle` unit suite covers only the main-thread-legal subset
(off-context ISE, the `hasFunc` guard, watchdog-hook plumbing).

**The watchdog:** a gate still closed after 30s (default) prints ONE console
line per activation, exact format:

```
[kotlin.lifecycle] <Subtype>(id=<id>) not ready after 30s — inputs <set|UNSET (created outside its Kotlin constructor?)>, unresolved: <labels>
```

Registry-miss no-op once the gate opens or the component retires. Hooks:
`kotlinLifecycleWatchdogMillis(ms)` (per-component override),
`kotlinLifecycleWatchdogFires()` (counter — assert the counter, not the line
text, in sub-second tests; the ScopeHandle watchdog precedent).

### `retire(node)` / `revive(node)` and THE RECYCLING LAW

Both are parent-side `kotlin.brs` functions over `callFunc`
(`__kotlinRetire`/`__kotlinRevive` — generated per concrete render component
and advertised as XML `<function>` entries by
`IrToBrsTransformer.generateLifecycleEntryFunctions`); callFunc rendezvouses
into the child's owning thread with the CHILD's `m`, so they work from the
render thread and from the main thread alike (Suite 11 test 7 is the
main-thread path). Typed-handle overloads (`retire(component)`) take a
createComponent/constructor handle. Guard FIRST: `node.hasFunc("__kotlinRetire")`
— a node that is not a Kotlin render component (raw BRS, a ContentNode
component, a legacy `@BrsComponent` plain class) gets a guided ISE ("… is not a
Kotlin render component — retire/revive target components compiled from
ComponentBase subclasses").

**Retire sequence** (idempotent — the `__kotlinIsRetired` guard runs first, a
second retire is a no-op): `onStop()` when the hierarchy overrides it → retire
hooks (the scope package's close-the-exposed-host hook is one; spec 2's
doorbell disarm will be another) → cancel + RESET the component scope (STOPs
`runTask` task threads underneath, deregisters StateFlow collectors, wakes
parked `awaitReady` calls with CE) → `retired = true`, activation bump. The
scope SELF-HEALS: the next `launch {}` gets a fresh supervisor scope, so an app
that forgets `revive` gets working coroutines and a visibly missing `onStart`,
not silent dead jobs. Retire does NOT remove the node — the caller owns the tree.

**Revive sequence** (no-op on a live component), two-phase on purpose: EVERY
ticket is reset first, THEN each is re-armed (a synchronous resolve inside an
early rearm must not see stale `resolved` on later tickets and open the gate
early) → the gate is re-checked (a coroutine that called `awaitReady` during
the retired window wakes here) → the driver relaunches, so `onStart` fires
again.

**THE RECYCLING LAW:** `retire(node) → removeChild → reconfigure var fields →
appendChild → revive(node)`. `init {}` is ONE-TIME structural setup;
per-activation work is `onStart`. Recyclable content is `@SG var` fields (Roku
`itemContent` parity); constructor `val` inputs are identity and
survive the cycle. Device-pinned end to end by Suite 11
`recycleCycleFiresOnStartAgainAndCoroutinesWork` (child-stop →
child-start:second → a `launch` in the revived scope runs) and
`constructorInputsSurviveRetireRevive` (the input reads the same after revive).

**What retire cannot promise:**

- **Unretired death is silent.** The platform cannot signal a node that dies
  without `retire()`; the ScopeHandle child watchdog and this section's
  watchdog are the only diagnosis.
- **Task-side `finally` effectively never runs** on the STOP that retire's
  scope cancel sends to a `runTask`/`flowOn` task thread (the Flow dispose
  contract).
- **removeChild does NOT silence observers** (device fact, spike Q7,
  2026-09-05 — `spikes/lifecycle-spike/FINDINGS.md` F7.3): a removed but still
  REFERENCED node keeps its scoped AND plain observers firing on later parent
  mutations (nine fires from three mutations × three live observers, one per
  observer per mutation). Observer death follows node DESTRUCTION, not tree
  membership — this refines Probe A4, whose silence needed every reference
  released. Only the explicit `retire()` disarm (through the retire hooks)
  stops a retained node's Kotlin-side observers, so every node-field
  `@BrsOnChange` handler must be IDEMPOTENT across the recycle cycle too (the
  "Field Writes" re-fire finding).
- **The positive Q7 detach signal is RECORDED, not built on:** the parent's
  `change` field fires `remove` synchronously inside the mutating call, for
  both observer forms (`reparent` records as `remove` on the old parent) — but
  explicit `retire()` is the sole contract (decision D3, 2026-09-09; a backstop
  is a future program).

### Three latent compiler defects fixed en route (all golden-pinned)

1. **`super.f()` in a component compiled to a self-recursive slot call.**
   SceneGraph rule behind it: every level's `init()` runs over ONE shared `m`,
   base first, so the leaf's slot attachment overwrites the base's same-named
   slot and `m.f()` inside the base resolved to the leaf. Now a STATIC call to
   the base's mangled global (`Base_f_..._k_(...)`, recording the include edge;
   the SharedService super fix was the template) — `superDispatchComponent`
   golden; Suite 11 test 3 also pins the runtime assumption that a bare
   (non-method) call binds `m` to the COMPONENT scope even from inside a
   coroutine `doResume`.
2. **An inherited `onKeyEvent` override was shadowed by the leaf's generated
   wrapper** (`return false`). SceneGraph rule behind it: one script per file,
   the leaf's XML includes the whole chain, and the leaf's same-named
   `onKeyEvent` entry WINS. The wrapper now dispatches to the slot whenever
   `hierarchyOverrides(cls, "onKeyEvent")` — `onKeyEventInheritedWrapper`
   golden; Suite 11 tests 4/5.
3. **Suspend-MEMBER state machines read component scope unrouted** (first
   device exposure: `suspend fun onStart()` is the first suspend member whose
   body touches component scope AFTER a real suspension). Inside
   `X__onStartCOROUTINE__doResume_k_`, `m` is the coroutine object and self is
   the `__this` capture: `m.global` crashed ("'Dot' Operator attempted with
   invalid BrightScript Component … (number 236)") and an @SG write landed as
   a dead AA key on the coroutine object. Fixed 2026-09-09 (`784fef7f309e`):
   the lambda `this_0` routing generalized to the `__this` capture — reads
   `m.__this.<prop>`, @SG writes `m.__this.top.<field>`
   (`collectCapturedComponentSelfFields` accepts the
   `DECLARATION_ORIGIN_COROUTINE_IMPL` field origin under the SAME provenance
   rule, so a component-typed suspend PARAMETER is still not marked; the
   captured-self check now runs BEFORE the `isInComponentContext` shortcut).
   `suspendMemberComponentScope` golden; Suite 11 tests 3 and 7. A suspend
   `onStart` with NO suspension point compiles to a plain function and was
   never affected.

**Constructor inputs and typed layout builders: plan B — LANDED 2026-09-09.**
`class Screen(@SGStringField val airingId: String)`, the `Screen("123")`
creation lowering, the `__kotlinInputsReady` marker, plugin-generated builders
and the FIR family have their own section, "Constructor Inputs and Typed
Layout Builders", directly below this one.

### Backlog (lifecycle program, recorded 2026-09-09)

1. **Two SceneGraph components in ONE file, or a SHARED helper declared in a
   component file, are SILENT MISCOMPILES**: the compiler emits one script per
   FILE and one XML per CLASS with a hardcoded own-script URI
   (`pkg:/components/<Name>/<Name>Kt.brs`), so extra classes get dangling URIs
   and any component whose closure pulls another component's script inherits a
   duplicate `init()` (device: "Function init defined more than once"). Want a
   FIR "one component per file" error and shared helpers in non-component
   files. Fixture convention meanwhile: one component per file; shared helpers
   (like `lifecycleLog`) in their own file (`fixtures/LifecycleLog.kt`).
2. `validateComponentIncludes` does not flag a function DEFINED MORE THAN ONCE
   in a closure, and does not run under `build` (only the rokuTest wrapper
   runs the test variant) — `rebuild-all` reports success on output the
   wrapper rejects.
3. The Q7 detach signal exists; no backstop is built (explicit retire is the
   contract).
4. The kotlin.test harness duplicates delivery on a repeated same-node+field
   `roundTrip` (re-observing from the main scope does not detach the port
   observer — the `runPumping` comment in DeviceTestLoop.kt); Suite 11 tests
   4/5 leave a 300 ms gap so the duplicate drains before the report read.
   Want a predicate `roundTrip` overload and/or a real port unobserve.
5. Plan B (constructor inputs + typed builders) SHIPPED 2026-09-09 — its own
   backlog is in the "Constructor Inputs and Typed Layout Builders" section.
   Spec 2 (scoped SharedService lookup / `by sharedService`, consuming design
   §13's ticket interface) is NEXT.

### Key files

| File | Purpose |
|------|---------|
| `libraries/stdlib/brs/src/kotlin/brs/lifecycle/ComponentLifecycle.kt` | ONE file (include-closure law): `__kotlinComponentAttach`, the registry, `awaitReady` + watchdog, `__kotlinRetireImpl`/`__kotlinReviveImpl`, `retire`/`revive`, test hooks |
| `compiler/ir/backend.brightscript/src/.../lower/BrsComponentLifecycleLowering.kt` | `__kotlinStartDriver` synthesis (phase 0.054) |
| `compiler/ir/backend.brightscript/src/.../BrsIntrinsics.kt` | `hierarchyOverrides`, `emitsLifecycleEntries` |
| `compiler/ir/backend.brightscript/src/.../irToBrs/IrToBrsTransformer.kt` | unconditional attach injection; `generateLifecycleEntryFunctions` (`__kotlinRetire`/`__kotlinRevive` subs + XML `<function>` entries) |
| `libraries/stdlib/brs/src/kotlin/brs/scope/ScopeApi.kt` | the retire hook that closes the exposed host; `exposeScope` re-callable after retire |
| `../roku-test-app/src/brsMain/kotlin/com/nuvyyo/roku/components/fixtures/Lifecycle*.kt` | Suite 11 fixtures (Parent/Child/Base/Leaf/Super/Task/Dumb probes + `LifecycleLog.kt`) |

Goldens: `compiler/testData/codegen/brs/components/{componentAttachUnconditional,
onKeyEventInheritedWrapper, superDispatchComponent, onStartDriver,
retireReviveEntries, suspendMemberComponentScope}`.

## Constructor Inputs and Typed Layout Builders

Landed 2026-09-09 (plan B of the component-lifecycle program). A component
declares its REQUIRED INPUTS as annotated primary-constructor properties; the
constructor call is the creation API, and static layouts get a plugin-generated
typed builder per component. Design of record:
`docs/superpowers/plans/2026-09-04-component-lifecycle-design.md` (§3 inputs +
builders, §4 inputs readiness, §5.7–5.10 compiler, §6 plugin, §7 FIR); plan
`docs/superpowers/plans/2026-09-04-component-constructor-inputs.md`; execution
record `.superpowers/sdd/2026-09-04-component-constructor-inputs/` (local,
untracked — not present in other clones). Device
coverage: E2E Suite 11 tests 10–14 (the five constructor-input tests, named in
the suite row) + goldens `components/{ctorInputs, ctorCallLowering,
builderCallExtraction}` + the `LayoutInputValidationTest` unit test + the
`componentInputs/` FIR fixtures.

```kotlin
class AiringDetailsScreen(
    @SGStringField val airingId: String,      // REQUIRED INPUT (non-null is fine)
    @SGIntegerField val row: Int,
) : GroupComponent() {
    @SGStringField var status: String = "idle" // recyclable CONTENT: var
    override suspend fun onStart() {           // inputs are SET here
        status = "$airingId:$row"
    }
}
// Creation — the constructor call IS the API (render-thread component code):
val screen = AiringDetailsScreen("123", 2)
top.appendChild(screen)
// Static layout — the generated builder; required inputs = required parameters:
companion object {
    @SGLayout
    fun defineLayout() = sceneLayout {
        airingDetailsScreen(id = "details", airingId = "123", row = 2)
    }
}
```

### Syntax and meaning

- A **constructor input** is a PRIMARY-constructor `val`/`var` parameter that
  carries `@SG*Field` (or legacy `@BrsField`). It is an ordinary XML `<field>`
  like every other `@SG` property AND a required input. ONE predicate defines
  the set: `BrsIntrinsics.constructorInputs` (backend — `isConstructorParameterProperty`
  + `hasInterfaceFieldAnnotation`, the class's OWN declarations, declaration
  order), mirrored by checkers.brs `BrsComponentTypes.constructorInputs`
  (module boundary — divergence law: a change lands in both in one commit). A
  plain un-annotated `val` parameter has no node field and is NOT an input
  (its write would be a silent drop on device).
- **`val` is a Kotlin-side promise**: no Kotlin code can reassign it, but the
  node field is a normal SceneGraph field — a parent's `setField`, XML, or raw
  BrightScript can still write it. Non-null types are fine: the value is
  written BEFORE `onStart`; what `init` would see is the SG type default
  (`""`/`0`/`false`), which is exactly why reading it there is an error (below).
- **Inputs are IDENTITY, content is `var`** (the recycling law): a constructor
  input survives `retire → removeChild → appendChild → revive` unchanged
  (Suite 11 `constructorInputsSurviveRetireRevive`); per-activation
  reconfiguration goes through `@SG var` fields.
- Constructor-parameter `@SG` properties emit NO `m.top.x = x` initializer line
  in the generated `init()` (there is no local to read inside `sub init()`;
  `ctorInputs` golden) — the XML `<field>` is still declared.

### Construction: the constructor call and what it lowers to

`AiringDetailsScreen("123", 2)` is lowered by
`BrsComponentConstructorCallLowering` (phase 0.0555, between the runTask and
sharedFrom lowerings) into:

```
__kotlinNewComponent_1 = CreateObject("roSGNode", "AiringDetailsScreen")
__kotlinNewComponent_1.airingId = "123"     ' one plain node-field dot-assign per input, parameter order
__kotlinNewComponent_1.row = 2
kotlinLifecycleMarkInputsReady_RoSGNode_k_(__kotlinNewComponent_1)   ' the ready marker — LAST
screen = __kotlinNewComponent_1
```

- Parameter → property by IR LINK (the property whose backing-field initializer
  reads that parameter), never by name; only `constructorInputs` properties
  get a write; the marker call is emitted only when at least one input was
  written — an input-less component (`VideoItem()`) is a bare `CreateObject`
  with no marker.
- The writes are tagged `COMPONENT_INPUT_WRITE` so the emitter's
  `componentFieldWriteRoute` (ONE decision for both `visitSetField` sites)
  emits `handle.field = v`, NOT the component-class `.top` routing (the handle
  is a bare roSGNode; `n.top` is invalid there).
- Temp names carry a per-FILE ordinal, unique per call site (`_1`, `_2`, …):
  nested `Outer(Inner(…))`,
  argument position, property-write position, discarded-statement position
  and locals hoisted into coroutine state machines are all pinned by the
  `ctorCallLowering` golden.
- Evaluation-order deviation: the node is CREATED before the arguments are
  evaluated (Kotlin evaluates arguments first); a throwing argument leaves an
  orphan unparented node, which is GC'd.
- `createComponent<T>()` / `brsCreateComponent<T>()` on an input-bearing class
  is a FIR ERROR (`BRS_CREATE_COMPONENT_HAS_INPUTS` — "'T' declares required
  inputs (a, b); construct it with T(…) so every input is written before
  onStart(), instead of createComponent<T>()"). It wins over
  `BRS_CREATE_COMPONENT_INVALID_TYPE` for an input-bearing abstract class;
  `runTask<T>` is untouched.
- **Task components take NO constructor inputs**: an `@SG`/`@BrsField`
  constructor-parameter property on a `TaskComponent` descendant is
  `BRS_TASK_CONSTRUCTOR_INPUT` ("Task components take inputs through
  runTask<T> { field = value }; declare 'x' as a var field instead of a
  constructor parameter"); a PLAIN constructor `val` on a task now fires
  `BRS_TASK_STATE_NOT_FIELD` (the fake-source skip in that checker is gone).
- Single-module closed world (SharedService/ScopeHandle/Flow precedent): the
  lowering needs the IR backing-field link, which a DEPENDENCY-klib class does
  not carry — see backlog (a). This is why roku-test-app's brsTest constructs
  its input probes through a brsMain driver fixture (`LifecycleInputDriverProbe`),
  never directly from the test body.

### Static layouts: the generated typed builders

The kotlin-roku plugin (`GenerateLayoutStubsTask`, pass 1) scans every source
file with a CONSTRAINED grammar and writes one `ComponentBuilders_<package>.kt`
per package into the layout-stubs srcDir (brsMain — the builders reach the
main compile and the test klib), one builder per eligible component:

```kotlin
@SGComponentBuilder("Badge")
fun LayoutBuilder.badge(id: String, label: String, translation: Vector2D? = null, …, init: ComponentBuilder.() -> Unit = {}) {
    component("Badge", id = id, translation = translation, …) { attr("label", label); init() }
}
```

- **Required inputs are required parameters**; the standard attributes stay
  optional; `init` nests children. The compiler keys extraction on the
  `@SGComponentBuilder` ANNOTATION (never the name or the body): the CALL
  site's arguments become XML attributes by parameter name —
  `<Badge id="hostBadge" label="NEW" __kotlinInputsReady="true"/>`.
- **Constants only.** Attribute values must be compile-time constants (the
  existing `@SGLayout` rule). For a REQUIRED input, a missing or non-constant
  value is a compile ERROR (not the usual dropped-with-warning), reported on
  the OWNER component, exact shape:
  `[BRS layout] component 'Badge' (id="hostBadge") declared in BadgeHost's layout requires input 'label' as a compile-time constant — supply it in the builder call (or attr("label", …)), or construct Badge in code`.
  Validation runs in `BrsCompiler` after every file is extracted and before
  lowering (`LayoutInputValidation.validate`), then `withInputMarkers` stamps
  the marker attribute on every child whose required inputs are all present.
  A non-constant `id` still silently drops the node (`component()` parity —
  backlog).
- The raw `component("Badge", id = "x") { attr("label", "NEW") }` form still
  works and is validated identically (children are checked recursively; a
  child whose type is a SceneGraph built-in or a dependency-klib component is
  never checked).
- The lowerCamel builder BESIDE its PascalCase class is legal:
  `@SGComponentBuilder` functions are exempt from `BRS_NAME_CASE_CLASH` (R33 —
  the builder's mangled BRS global never collides with the class's globals; the
  exemption is annotation-keyed and pinned by a negative-control fixture,
  `nameCaseClash/sgComponentBuilderBesideComponentClassOk`).
- **Which classes get NO builder** (the plugin logs each skip — WARN when a
  constructor PARAMETER piece could not be parsed or a name collides, INFO for
  designed-in ineligibility): an input typed node/AA/nullable — only
  `String`/`Int`/`Float`/`Double`/`Boolean` can be XML constants (construct
  those in code; INFO); a constructor parameter that is not an
  `@SG<Kind>Field val name: Type` input (WARN); an input named `id`/`init`/a
  standard attribute (WARN); abstract/sealed/data/enum/annotation/inner/value/
  private classes (INFO — not instantiable as layout children); a builder name
  colliding with a built-in DSL method or a Kotlin hard keyword (WARN). A class
  whose HEADER the grammar does not recognize never enters the scan at all —
  SILENT: no builder, no warning (backlog (e)). Kotlin default values on
  constructor inputs are parsed and IGNORED — the builder parameter stays
  required because the marker needs EVERY input.
- **The grammar** is `[modifiers] class X(<@SG…Field val a: T, …>) : Base(`
  with `Base` a stdlib render base or another component of the module
  (cross-file base chains resolved module-wide; comments stripped
  string-aware). It rejects rather than guesses; the compiler extractor is the
  source of truth — a wrong builder is a compile error at its call site, never
  a silent XML. roku-test-app currently generates 31 builders.

### The ready marker and the gate

- Every input-bearing TYPE declares one extra boolean XML field
  `__kotlinInputsReady` (default `false`); input-less types have none — that
  absence is how the stdlib learns a class has no inputs (`awaitReady` treats
  a missing marker field as inputs-ready). Two writers open it: the
  constructor-call lowering (`kotlinLifecycleMarkInputsReady`, after the input
  writes) and the static-layout extractor (`__kotlinInputsReady="true"` on a
  satisfied child — open at creation). A node created ANY other way (raw
  `CreateObject`, hand-written XML, a dependency-klib constructor call) has
  nobody writing it: the gate stays CLOSED, `onStart` never fires, and after
  30s (default) the watchdog prints its UNSET clause, exact format:

  ```
  [kotlin.lifecycle] AiringDetailsScreen(id=) not ready after 30s — inputs UNSET (created outside its Kotlin constructor?), unresolved:
  ```

  A manual `setField("__kotlinInputsReady", true)` opens it (Suite 11
  `rawCreatedInputComponentStaysClosedUntilMarkerAndWatchdogFires` — the first
  device exercise of the awaitReady park/wake/watchdog path; the watchdog fires
  exactly ONCE per activation, `kotlinLifecycleWatchdogFires()` is the durable
  assertion). `retireCancelsOnStartParkedInAwaitReady` pins retire while parked:
  no `onStart` after a late marker.
- **Mirror law:** the literal lives in BOTH `LIFECYCLE_INPUTS_READY_FIELD`
  (stdlib `ComponentLifecycle.kt`) and `LayoutInputValidation.READY_MARKER_ATTRIBUTE`
  (compiler `BrsComponentInfo.kt`), cross-referenced in KDoc — the module
  boundary forbids sharing the constant; a change lands in both in one commit.

### The init-read rule (`BRS_COMPONENT_INPUT_READ_IN_INIT`)

SceneGraph runs `init()` INSIDE `CreateObject`, before any field write, so a
constructor input read in `init {}` or in a sibling property initializer sees
the declared default. FIR ERROR: "Input 'airingId' of component 'X' is read
during init: SceneGraph runs init() inside CreateObject, before any field is
written, so this read sees the declared default. Read it in onStart() (fires
once all inputs are set) or in a lambda/observer." Both spellings fire
(`airingId` and `this.airingId` — K2 fact: a `val` constructor parameter is
NOT in scope as a parameter inside init blocks/initializers, its bare name
resolves to the PROPERTY with an implicit `this`); reads through another
receiver (`peer?.airingId`), in methods, lambdas (`launch {}` in init
included), `onStart`, and the delegated-super-call forward `: Base(airingId)`
are clean. **Disclosed holes** (KDoc'd on the checker): a synchronously-invoked
INLINE lambda in init (`run { airingId }`) classifies as deferred and passes;
a same-class helper called from init that reads the input is a method read
(interprocedural — not chased). `@Suppress("BRS_COMPONENT_INPUT_READ_IN_INIT")`
on the statement is the escape.

### The FIR family (fixture-pinned in the checkers.brs suite: `componentInputs/` 14 + `nameCaseClash/` 1)

| Diagnostic | Severity | Fires on |
|---|---|---|
| `BRS_COMPONENT_INPUT_READ_IN_INIT` | ERROR | a constructor input read with implicit/explicit `this` inside the owner's `init {}` or a sibling property initializer |
| `BRS_CREATE_COMPONENT_HAS_INPUTS` | ERROR | `createComponent<T>()` / `brsCreateComponent<T>()` where `T` declares constructor inputs (precedence over INVALID_TYPE; `runTask` exempt) |
| `BRS_TASK_CONSTRUCTOR_INPUT` | ERROR | an `@SG*Field`/`@BrsField` primary-constructor property on any `TaskComponent` descendant (abstract task bases included) |

Plus the static-layout missing/non-constant-input ERROR above (a compiler
`reportError`, not a FIR diagnostic — pinned by `LayoutInputValidationTest`,
which `run-compiler-tests.sh` gates alongside the goldens), and the
`BRS_TASK_STATE_NOT_FIELD` widening (plain task constructor `val`s now fire).

### Backlog (plan B, recorded 2026-09-09 — surfaced by execution, none in v1 scope)

- (a) **CROSS-MODULE component constructor call is SILENT-WRONG codegen**: a
  dependency-klib input-bearing class lowers to a bare `CreateObject` — no
  input writes, no marker, no diagnostic (the deserialized class lacks the IR
  backing-field link `isConstructorParameterProperty` needs; FIR's
  `fromPrimaryConstructor` is likewise never set by deserialization, so
  `BRS_CREATE_COMPONENT_HAS_INPUTS` is clean too). The gate then times out
  with the watchdog's "created outside its Kotlin constructor?" guess. Want a
  LOUD FIR error + a multi-module golden. Found by Task 7 round 1 (ruling R39
  reshaped the tests to the in-scope same-module driver).
- (b) **Alias-shape mirror gap**: backend `constructorInputs` is LINK-based
  (`@SGStringField val b: String = a` counts `b` as an input, written from
  arg `a`), FIR `isConstructorInput` uses `fromPrimaryConstructor` (does not) —
  same annotation set, different "which properties" on that alias shape.
- (c) **Constructor-input FIR hardening bundle** (each needs a NEW diagnostic
  + regen): inherited `@SG` constructor input (`abstract Base(@SGStringField val x)`
  + `class Leaf(x) : Base(x)` — the leaf's own inputs are empty, so no
  marker attribute/write while the inherited marker FIELD defaults false →
  silent 30s watchdog, R34); a default-valued constructor input (an all-defaulted
  call site writes no marker → gate never opens); a secondary constructor on a
  component (still emits the undefined `<Class>_create_…` call); a plain
  non-`@SG` constructor value parameter on a component (silently skipped);
  a subclass `init` reading an inherited input (clean by owner-identity
  scoping).
- (d) `@BrsField(name = "other")` on a constructor parameter: `requiredInputs`
  and the input write use the PROPERTY name while the XML field uses the
  override — property-name-vs-XML-name mismatch, now reachable through inputs.
- (e) **Generator grammar holes** (rejected, not guessed): a supertype list
  that starts with an interface (`class X : Foo, GroupComponent()`) is not
  recognized as a component (no builder, no warning); a comma inside a string
  default breaks the parameter split (WARN, no builder); `stripComments` does
  not understand char literals (`'"'` opens a "string"). Plus the
  `interfaceField` collision (Task 6 C1): the generator's built-in-DSL-name
  set omits `LayoutBuilder.interfaceField`, so `class InterfaceField :
  GroupComponent()` gets a builder, and a POSITIONAL `interfaceField("x")`
  call resolves to the MEMBER (member beats extension) — it declares an
  interface field named `x` instead of a child, silently (the named
  `id = "x"` form is unaffected). Also: non-constant builder `id` silently
  drops the node; no end-to-end negative pin for the layout error (the golden
  harness has no expected-error mode — the unit test is the pin).
- (f) **Generator vs compiler component-NAME divergence** (found by the
  plan-B whole-branch review): the kotlin-roku generator derives the
  `@SGComponentBuilder`/`component()` type name from the raw Kotlin simple
  class name, but the compiler names the component via `getBrsName` (honors
  `@BrsName`, prefixes nested classes `Outer_Inner`). For a `@BrsName`-renamed
  or nested INPUT-BEARING component they diverge: the generated builder emits
  a child of the raw-name type the compiler never registered (runtime: child
  fails to instantiate) AND `requiredByType` is keyed by the compiler name, so
  `LayoutInputValidation` never checks that child — no missing-input
  diagnostic, no ready marker. Silent-wrong when it occurs; low likelihood
  in-tree (no `@BrsName` on a component class; one-component-per-file rules out
  nested components). Fix: the generator honors `getBrsName`, OR a LOUD FIR
  guard forbidding `@BrsName`/nesting on an input-bearing `@SGComponentBuilder`
  component. Same shape as (d) but for the type name, not an attribute name.

### Key files

| File | Purpose |
|------|---------|
| `compiler/ir/backend.brightscript/src/.../BrsComponentExtractor.kt` | `requiredInputs` + the marker `<field>` on input-bearing types; `@SGComponentBuilder` call recognition (`extractBuilderComponentNode`) |
| `compiler/ir/backend.brightscript/src/.../BrsComponentInfo.kt` | `LayoutInputValidation` (`validate`, `withInputMarkers`, `READY_MARKER_ATTRIBUTE`), `LayoutInputFinding.message()`; invoked from `BrsCompiler` post-extraction |
| `compiler/ir/backend.brightscript/src/.../lower/BrsComponentConstructorCallLowering.kt` | constructor call → `CreateObject` + `COMPONENT_INPUT_WRITE` field writes + `kotlinLifecycleMarkInputsReady` (phase 0.0555) |
| `compiler/ir/backend.brightscript/src/.../irToBrs/IrToBrsTransformer.kt` | the init-emission SKIP for constructor-parameter `@SG` properties (`transformComponentInitBlock`); `brsTransformerUtils.componentFieldWriteRoute` is the shared write-routing decision |
| `compiler/ir/backend.brightscript/src/.../BrsIntrinsics.kt` | `isConstructorParameterProperty`, `constructorInputs`, `interfaceFieldAnnotationIds` / `hasInterfaceFieldAnnotation` — THE backend predicate |
| `compiler/fir/checkers/checkers.brs/src/.../BrsComponentTypes.kt` | the FIR mirror: `isComponentClass`, `isTaskComponentClass`, `isConstructorInput`, `constructorInputs`, `fieldAnnotationIds` |
| `compiler/fir/checkers/checkers.brs/src/.../expression/FirBrsComponentInputEarlyReadChecker.kt` | `BRS_COMPONENT_INPUT_READ_IN_INIT` |
| `compiler/fir/checkers/checkers.brs/src/.../expression/FirBrsCreateComponentTypeChecker.kt` | `BRS_CREATE_COMPONENT_HAS_INPUTS` (beside the existing INVALID_TYPE) |
| `compiler/fir/checkers/checkers.brs/src/.../declaration/FirBrsTaskConstructorInputChecker.kt` | `BRS_TASK_CONSTRUCTOR_INPUT` |
| `compiler/fir/checkers/checkers.brs/src/.../declaration/FirBrsNameClashFileTopLevelDeclarationsChecker.kt` | the `@SGComponentBuilder` exemption (R33) |
| `libraries/stdlib/brs/src/kotlin/brs/scenegraph/NodeEntry.kt` | `@SGComponentBuilder(componentType)` |
| `libraries/stdlib/brs/src/kotlin/brs/lifecycle/ComponentLifecycle.kt` | `LIFECYCLE_INPUTS_READY_FIELD`, `kotlinLifecycleMarkInputsReady`, the inputs half of the gate + the watchdog UNSET clause |
| `../kotlin-roku/src/main/kotlin/com/example/roku/gradle/tasks/GenerateLayoutStubsTask.kt` | pass 1: constrained-grammar component scan + `ComponentBuilders_<pkg>.kt`; pass 2 layout stubs include builder-call ids |
| `../roku-test-app/src/brsMain/kotlin/com/nuvyyo/roku/components/fixtures/LifecycleInputProbe.kt` + `LifecycleBuilderParentProbe.kt` + `LifecycleInputDriverProbe.kt` | Suite 11 constructor-input fixtures (probe, generated-builder parent, same-module constructor driver) |

Goldens: `compiler/testData/codegen/brs/components/{ctorInputs, ctorCallLowering,
builderCallExtraction}`; unit: `compiler/ir/backend.brightscript/test/.../LayoutInputValidationTest.kt`.

## Coroutine Utilities (awaitAll & friends)

Landed 2026-08-11 (the coroutine-utilities program). The stdlib now has a real
structured-concurrency surface over the pump scaffolding above:

- **Job protocol**: `invokeOnCompletion` (settle-once, handler disposal),
  real `join()`/`await()` — parked continuations, NOT busy-waits.
  `Job.ensureActive()` / `CoroutineContext.ensureActive()` /
  `CoroutineScope.isActive` for cooperative checks.
- **`awaitAll(vararg)` / `Collection<Deferred<T>>.awaitAll()`**, `joinAll`
  (both forms) — `libraries/stdlib/brs/src/kotlin/coroutines/Await.kt`.
  awaitAll rethrows the FIRST failure as soon as it fires, WITHOUT cancelling
  the remaining deferreds (kotlinx parity; wrap in `coroutineScope` if you
  want sibling cancellation).
- **`coroutineScope {}`** and **`withContext(context) {}`** — real scoped
  children over the shared `parkScopedBlock` engine (`Scopes.kt`,
  `builders/WithContext.kt`).
- **`withTimeout` / `withTimeoutOrNull`** + `TimeoutCancellationException`
  (`builders/Timeout.kt`). `withTimeoutOrNull` maps only its OWN expiry to
  null; other failures rethrow.
- **`SupervisorJob(parent)`** — children's failures don't cancel siblings/parent.
- **`delay()` / `yield()`** are cancellation-aware (entry checks + mid-park
  wakeup).

**Hierarchy and supervisor roots.** `launch`/`async` attach to the parent job;
a child failure cancels siblings and propagates up — UNLESS the parent is a
supervisor. ALL THREE root scopes are `SupervisorJob()` roots (deliberate
kotlinx DIVERGENCE — kotlinx roots `runBlocking` on a regular Job):
`componentScope()` (`brs/ComponentCoroutines.kt:60`), `runBlocking`
(`coroutines/builders/Builders.kt:102`), and the kotlin.test driver's
`runPumping` (`kotlin.test/.../device/DeviceTestLoop.kt:295`). One failing
top-level `launch {}` therefore never kills the component scope, the app main
loop, or an unrelated device test. An unhandled launch failure prints
`[kotlin.coroutines] Unhandled exception in coroutine: ...` to the console
(`Job.kt:319`) — grep for that prefix when a fire-and-forget coroutine dies.

**Cancellation promises (v1).** Every stdlib suspend point entry-checks the
job (`delay`, `yield`, `join`, `await`, `awaitAll`, scope builders, `runTask`/
`awaitCompletion`); a PARKED suspension is woken mid-park by caller cancel
(CancellationException at the suspend point) — including a coroutine parked in
`runTask`, whose cleanup drops the registry entry and disarms the state
observer so the task's late terminal write finds nothing to do (E2E Suite 4
"runTask await wakes promptly on caller cancellation and STOPs the task");
cancellation cascades through the hierarchy. Task-thread work IS stopped
(flow-program STOP rider, 2026-09-03): a cancelled `runTask` writes
`control="STOP"` after the protocol teardown — a prompt hard kill (Probe B);
`flowOn`/`spawnTask` additionally write the cooperative `flowCancel` field
first. Task-side `finally` effectively never runs on the cancel path (the
hard STOP wins the race) — see the Flow dispose contract.

**Jobs are same-component-only.** GetGlobalAA — and therefore the queue, the
DelayTracker, and the pump — is per-component-instance on the render thread.
A Job/Deferred handed to ANOTHER component cannot be serviced there; don't
share Jobs across components (cross-component signalling is what @SG fields
and observers are for).

**The stdlib suspend-function idiom (for FUTURE utilities): tail-delegation +
`parked.finish()`.** Suspend utilities are written over `ParkedContinuation`
(`coroutines/ParkedContinuation.kt`): inside
`suspendCoroutineUninterceptedOrReturn`, create the park, register completion/
cancel handlers on it (it owns their disposal and the settle-once guard, and
resumes through the ContinuationInterceptor when one is present), and make
`parked.finish()` the block's LAST expression — the park starts UNARMED, so a
handler firing synchronously (no-interceptor regime: runBlocking/runPumping)
is recorded and `finish()` converts it into a synchronous return/throw instead
of a double-execute. Suspend functions that merely wrap another suspend call
must TAIL-DELEGATE (return the inner call directly), not park around it.

**DX traps:**

- **`runBlocking`/`runPumping` return when the BLOCK completes, not when its
  launched children do** — a kotlinx divergence; `join()` explicitly on any
  child you need finished before returning.
- **Nested `launch` inside `coroutineScope {}` IN A COMPONENT** resolves to
  `ComponentBase.launch` (a NEW top-level coroutine on the component scope),
  not to the scope receiver — the `coroutineScope` completes childless,
  silently, and awaits nothing. Import `kotlin.coroutines.builders.launch`
  explicitly in component files that use `coroutineScope { launch { } }`
  (FIR-warning candidate, backlogged).
- **Don't leak the scope receiver**: the `CoroutineScope` receiver of
  `coroutineScope`/`withContext`/`withTimeout` is only valid INSIDE the block;
  launching on a stored copy after the block returns is undefined (KDoc'd on
  the builders; runtime/FIR guard backlogged).

Device coverage: stdlib "join/await suspension", awaitAll, scopes, timeout
suites (runBlocking regime) + E2E Suite 7 CoroutineUtilities (component
pumping regime, incl. `awaitAll` over concurrent `runTask`s). The flagship
demo (`../roku-test-app/src/brsMain/kotlin/com/nuvyyo/roku/components/ShelfView.kt`) fetches ip + shelf
concurrently via `async`/`awaitAll`.

## Flow & StateFlow (kotlin-flow-brs)

Landed 2026-09-03 (the Flow program). kotlinx-style flows for the BRS target —
ONE import swap from Android: `kotlinx.coroutines.flow.*` →
`kotlin.coroutines.flow.*`; names and signatures mirror kotlinx for everything
shipped. Design of record:
`docs/superpowers/plans/2026-09-02-flow-program-design.md`; spike truth in
`spikes/flow-spike/FINDINGS.md` (Probe A global-node doorbell, Probe B task
STOP). Device coverage: the stdlib flow suites (runBlocking regime) + E2E
Suite 10 (Flow — 10a flowOn/spawnTask, 10b StateFlow) + the TestScreen
flagship.

### The klib home: `kotlin-flow-brs` (a second klib, NOT stdlib)

Flow ships as its own klib (package `kotlin.coroutines.flow` via
`-Xallow-kotlin-package`), compiled WITHOUT `-Xstdlib-compilation` — the whole
point (spec decision 10): stdlib compilation generates NO suspend state
machines, so kotlinx-style operator internals would miscompile SILENTLY inside
the stdlib; as a separate klib they compile as USER-mode code with real state
machines (kotlin-test-brs second-klib precedent). Sources:
`libraries/flow/brs/src`; prebuilt klib + source-hash freshness check:
`libraries/flow/brs-prebuilt/` (verifyKlib fails the build on stale klib).
The stdlib itself gained only the `Dispatchers.Task` token and small public
ambient-node accessors.

**Commit prefixes:** `flow:` (library source), `flow-prebuilt:` (regenerated
klib + hash). EVERY flow source change pairs with a `flow-prebuilt:` regen
commit — `./rebuild.sh` regenerates (step 3) and compile-gates (step 9)
automatically; commit both.

### The three tiers

1. **Cold core** — `Flow<T>`/`FlowCollector<T>`, `flow {}`, `flowOf`,
   `asFlow`; operators `map`/`mapLatest`/`filter`/`filterNotNull`/`transform`/
   `transformLatest`/`onEach`/`onStart`/`onCompletion`/`catch`/
   `distinctUntilChanged`/`take`/`drop`/`conflate`/`combine`/`flatMapConcat`/
   `flatMapMerge`/`flatMapLatest`; terminals `collect`/`collectLatest`/
   `first`/`firstOrNull`/`toList`/`launchIn`. A cold flow runs entirely inside
   the collector's coroutine, in ANY context (component `launch {}`,
   `runBlocking`, `runPumping`); the body re-executes per collect. Exception
   transparency (`catch {}` sees UPSTREAM failures only); `onCompletion
   { cause }` fires on completion (null), failure, and cancellation (the CE).
2. **The task lift** — `flowOn(Dispatchers.Task)` + `spawnTask {}`. The
   compiler lifts the LITERAL upstream chain (or spawnTask block) into a named
   function and synthesizes a PER-CALL-SITE TaskComponent
   (`BrsFlowTaskLiftLowering`): captures cross as ONE `flowCaptures` AA field
   on the stdlib `FlowTaskComponent` base (deep copy — recorded deviation (a);
   not per-capture typed fields), plus one output field
   carrying kind-tagged envelope AAs (emit/complete/error — the ScopeWire
   vocabulary), a shim collector whose `emit` writes envelopes. Collector side
   is runTask-shaped (fresh unparented node, arm observer BEFORE
   `control=RUN`); downstream of `flowOn` is ordinary cold collection.
   Per-site synthesis keeps include closures correct by construction — no
   binding table, no ScopeHandle-style mid-transform subset hole.
3. **Hot tier** — `MutableStateFlow(initial)` / `StateFlow` / `asStateFlow()`
   / `stateIn(scope, initialValue)`. The VALUE lives on the flow object as a
   plain property (live cross-component when the flow rides a SharedService
   VM); the DOORBELL is a version int on the GLOBAL node.

### The law lists (task lift; FIR-guarded where statically visible)

- **Literal upstream**: the `flowOn` receiver must be a literal flow chain at
  the call site — a `Flow<T>` parameter cannot be lifted (its code isn't
  visible). `Dispatchers.Task` is a compile-time token: literal, and legal
  ONLY in `flowOn` argument position.
- **Captures marshallable-only, BY COPY** — class instances banned INCLUDING
  implicit `this`: hoist instance-property reads to locals first
  (`val url = baseUrl`); the error message says exactly that. The hoist keeps
  evaluation TIME visible (cold bodies are lazily evaluated).
- **Emissions marshallable-only** + the map-downstream idiom: emit parsed AAs
  task-side, `.map { toDomain(it) }` render-side. Same checker covers
  `spawnTask`'s return type.
- **Sync-only lifted region**: no suspend calls except `emit`/`emitAll` —
  blocking I/O (sync roUrlTransfer) is the point of being there.
  `ensureTaskActive()` = cooperative cancellation checkpoint inside long
  non-emitting compute (no-op off-task, so shared helpers call it
  unconditionally).
- **Escaped-suspension backstop**: a suspension that escapes FIR (suppressed
  or through a disclosed hole) can never resume — task threads have no pump —
  so the driver surfaces a guided error naming `BRS_TASK_SUSPEND_IN_LIFTED`
  at the collector instead of hanging.
- **Exceptions cross as DATA**: an upstream throw is rethrown as
  `TaskException` at the collector (`catch {}` sees it); original exception
  TYPES never cross a thread boundary (runTask precedent).
- **Render-context laws**: `flowOn` collection, `spawnTask`, StateFlow emit
  and collect all require render-thread component context (guided ISE).
  `value` GET is a plain property read and works anywhere; construction works
  anywhere.
- **Cold flows NEVER cross a component boundary** (their lambdas strip on
  every ordinary channel); only StateFlow crosses, statically routed (below).

### StateFlow: the static access layer + the doorbell protocol

**Why a static access layer:** interface-receiver member calls are fn-slot
dispatch on this backend — they record no include-closure dependency, and on a
shared-VM-held flow the slot fn-ref would ride the disclaimed SetRef path.
`BrsFlowAccessLowering` (phase 0.059) rewrites `StateFlow`/`MutableStateFlow`
`.value` access and `collect` calls to static flow-klib functions;
`StateFlowImpl` is a plain data holder — no cross-component fn slot ever
fires. (Discovery of record, Task 10: user `collect {}` resolves to the
MEMBER — member beats extension — so the member rewrite is the dominant path.)

**Doorbell protocol:** one runtime-addField int `__kotlinFlow_<uuid>` on the
GLOBAL node per flow instance (`alwaysNotify=true`, lazily bound), observed
via `observeFieldScoped` — each observer fires in the COLLECTOR's own context
(Probe A). Emit (`value = x`, non-suspending): structural-equality gate (skip
if `== current`) → store on the flow object → ring (increment the version).
Collect: register in the per-component doorbell registry (ComponentMailbox
pattern, refcounted) → deliver the CURRENT value immediately (late joiners —
kotlinx contract) → park; each ring reads the live value, dedups by `==`.
Conflation is correct by construction (a slow collector wakes to whatever is
newest). `collect` on a StateFlow never completes normally — it ends only by
cancellation (which runs `onCompletion`/`finally`). Collector-component death
WITHOUT cancellation: scoped observers auto-detach, no ghost deliveries
(Probe A4, Suite 10b teardown test). NOTE: doorbell fields ACCUMULATE on the
global node over an app session — one int per flow INSTANCE, SceneGraph has no
removeField (name-reuse pool is recorded backlog).

**`stateIn(scope, initialValue)` — eager-only v1** (no SharingStarted modes):
launches the bridge collector in `scope` immediately; Jobs are
same-component-only, so pass the OWNING component's scope
(`componentScope()`, the ScopeHandle-injection idiom). The bridge dies with
the screen; the StateFlow stays readable afterwards (last value frozen).

### THE DISPOSE CONTRACT (device truth, Task 9)

- **Render side: GUARANTEED.** `onCompletion`/`finally` in the collector run
  on completion, failure, AND cancellation.
- **Task side: `finally` EFFECTIVELY NEVER runs on cancel.** The cancel
  sequence (disarm observer → drop queued envelopes → `flowCancel=true` →
  `control="STOP"`) writes the cooperative field and the hard STOP
  back-to-back, and the hard STOP wins the race — trailing code after the
  killed point, `finally` included, does not run (Probe B8; Suite 10a
  ensureTaskActiveStops pins the beat-freeze). `ensureTaskActive()`'s value is
  the EARLY EXIT itself — stop wasting the task thread — not a cleanup
  guarantee. The platform refcount-releases the dead thread's objects, so
  native handles don't leak; user cleanup code is what's skipped. STOP
  promptness is pinned in every probed shape except blocked sync roUrlTransfer
  (B5 inconclusive — recorded spike bait). Plain `runTask` cancellation is the
  hard STOP alone (no cooperative field on that path — see the runTask
  section).

### kotlinx divergences (documented, deliberate)

- **No backpressure across the task hop**: the producer never suspends; the
  render-side queue is unbounded (kotlinx buffers 64 and suspends).
  `conflate()` downstream is the state-shaped answer.
- **`TaskException` typing**: upstream exception types never cross the hop.
- **Single-context interleaving**: children interleave only at suspension
  points — `flatMapMerge` over non-suspending inners degenerates to concat
  order (comparable to kotlinx on one confined dispatcher).
- **Eager-only `stateIn`** (no `WhileSubscribed`/`Lazily`).
- **Supervisor-rooted `runBlocking`/`runPumping`/`componentScope`** (the
  pre-existing divergence — see "Coroutine Utilities").

### The FIR family (fixture-pinned in the checkers.brs suite)

| Diagnostic | Severity | Fires on |
|---|---|---|
| `BRS_FLOW_UPSTREAM_NOT_LITERAL` | ERROR | `flowOn` receiver isn't a literal flow chain at the call site |
| `BRS_FLOW_ON_INVALID_DISPATCHER` | ERROR | `flowOn` argument isn't literal `Dispatchers.Task`; also fires REVERSED — `Dispatchers.Task` anywhere other than a `flowOn` argument position |
| `BRS_TASK_CAPTURE_UNMARSHALLABLE` | ERROR | lifted region (flowOn upstream / spawnTask block) captures a class instance (incl. implicit `this`), function value, or other unmarshallable — message names the hoist-to-local fix |
| `BRS_TASK_EMIT_NOT_MARSHALLABLE` | ERROR | emission type upstream of `flowOn`, or `spawnTask` return type, outside the marshallable set |
| `BRS_TASK_SUSPEND_IN_LIFTED` | ERROR | suspend call other than `emit`/`emitAll` in the lifted region |
| `BRS_TASK_CAPTURE_MUTATION_LOST` | WARNING | lifted region assigns a captured `var` (copies — the write never reaches the caller) |

Plus the updated `BRS_IO_DISPATCHER_UNSUPPORTED` message (quarantine section).
Marshallability reuses the shared ScopeHandle oracle, which REJECTS `Any`
(stricter than the spec's original disclosed-holes line — Task-6 ruling;
cast/hoist to a concrete marshallable type, or `@Suppress`). Remaining
disclosed holes: unresolvable suspend function REFERENCES passed to upstream
operators; generic `T`-typed emissions. Single-module closed world for lifted
regions (ScopeHandle/SharedService precedent; multi-module is recorded
backlog).

### Key files

| File | Purpose |
|------|---------|
| `libraries/flow/brs/src/kotlin/coroutines/flow/Flow.kt` + `FlowBuilders.kt` | `Flow`/`FlowCollector`/`emitAll`; `flow {}`, `flowOf`, `asFlow` |
| `.../flow/Operators.kt`, `Terminals.kt`, `Errors.kt` | sequential operators, terminals, catch/onCompletion |
| `.../flow/ConcurrentOperators.kt` + `FlowChannel.kt` | flatMap family/combine/conflate over the internal same-context queue primitive |
| `.../flow/TaskFlow.kt` | flowOn collector half: driveFlowTask, envelope queue, the cancel sequence, escaped-suspension backstop |
| `.../task/SpawnTask.kt` | `spawnTask` runtime + `ensureTaskActive` |
| `.../flow/StateFlow.kt`, `Doorbells.kt`, `StateIn.kt` | hot tier: value holder, global-node doorbell machinery, stateIn bridge |
| `compiler/ir/backend.brightscript/src/.../lower/BrsFlowTaskLiftLowering.kt` | the lift: per-site TaskComponent synthesis |
| `compiler/ir/backend.brightscript/src/.../lower/BrsFlowAccessLowering.kt` | StateFlow static access rewrite (phase 0.059) |
| `compiler/fir/checkers/checkers.brs/src/.../FirBrsFlowLiftCheckers.kt` | the FIR family above |
| `libraries/flow/brs-prebuilt/` | prebuilt klib + source-hash check |

Goldens live in `compiler/testData/codegen/brs/flow/` (lift shapes, operator
chain, StateFlow access, in-component-file + package-qualified lifts).
Canonical examples: the TestScreen flagship
(`../roku-test-app/src/brsMain/kotlin/com/nuvyyo/roku/components/TestScreen.kt` + `TestScreenVM.kt` +
`TestScreenChildLabel.kt` — StateFlow VM, flowOn repository flow,
cross-component child collector) and the Suite 10 fixtures
(`../roku-test-app/src/brsMain/kotlin/com/nuvyyo/roku/components/fixtures/Flow*.kt`).

## Cross-component channel semantics (scope-handle spike, 2026-08-12)

Device-verified on Roku Ultra 4800X, OS 15.3.4 build 841. Full evidence and
raw verdict lines: `spikes/scope-handle-spike/FINDINGS.md` (channel-matrix =
raw-BRS run; kotlin-probe = compiled-Kotlin runs). Facts only — the
ScopeHandle design decision is recorded separately in that file.

- Ordinary channels COPY — top-level on all six probed channels (node field,
  global field, callFunc arg, callFunc return, rtq PostMessage, observer
  `getData()`), deep on every channel probed for depth: a receiver-side
  mutation is never visible to the sender, top-level or nested
  (channel-matrix Q1a all-FAIL; kotlin-probe `nested FAIL innerMarker=1` ×3).
- Function references never survive an ordinary hop, in two SILENT strip
  modes: field channels and rtq DROP the key; callFunc (both directions)
  keeps the key valued `Invalid`. No crash on any channel (channel-matrix
  fnRef row; kotlin-probe fn-slot strips).
- Node references survive every ordinary channel EXCEPT the callFunc RETURN,
  which detaches even a bare directly-returned node into an id-less `Node`;
  the arg direction preserves identity, bare or AA-wrapped (channel-matrix
  nodeRef row; kotlin-probe barenode probes).
- rtq `PostMessage` has MOVE semantics: the sender's posted AA is gutted to
  `keys=0` at its top level, while the sender's direct references to nested
  values stay intact — externally-referenced nested objects are copied, not
  moved (channel-matrix `rtq.postState`; kotlin-probe `q1d.rtq.postState`).
- A Kotlin object crosses any probed ordinary channel as a HUSK: data keys survive,
  every fn slot is stripped, first method call crashes `Member function not
  found` — yet `as? T` still PASSES on the husk (the cast walks `__proto`,
  which is plain data). Explicit liveness markers, not type checks, are the
  only runtime guard (kotlin-probe Q1d ×3 channels; negative control
  `castControl.plainAA`).
- SetRef/GetRef (OS 15.0+, RENDER-THREAD-ONLY, AA fields only, unusable with
  queueFields) give genuine cross-component shared identity — top-level AND
  nested mutations visible both ways, `roUtils.IsSameObject` true — and are
  observer-SILENT: SetRef never fires the field's observer, so a delivery
  signal needs a separate ordinary write as doorbell (kotlin-probe run2
  `setref.*`).
- A Kotlin object passed via SetRef/GetRef arrives ALIVE today (fn slots
  intact, cross-component dispatch works, state shared) — but this rides the
  fn-ref namespacing behavior Roku explicitly says not to build dependencies
  on (kotlin-probe run2 `setrefVm.*`; RokuDocs "Optimized data transfer and
  reference handling").
- `CanGetRef` returns false for a field written via ordinary setField —
  references must be explicitly SetRef'd before GetRef succeeds (kotlin-probe
  run2 `setref.canGetRefOnSetFieldField`).
- Runtime-`addField` fields are first-class for observation: observers fire,
  and `alwaysNotify=true` set at addField time is honored (channel-matrix
  Q4.1/Q4.2).
- MoveIntoField/MoveFromField (OS 15.0+, any thread) work cross-component;
  nested objects with live external references are COPIED, not moved
  (`move.into copiedCount=2`, held ref intact) — the pure-move half of the
  rule is doc-only, not device-pinned (kotlin-probe run2 `move.*`).

## SceneGraph Layouts: @SGLayout DSL + Layout Accessors

The layout story: declare the component's children ONCE in the `sceneLayout {}`
DSL; access them through the generated `<ClassName>_Layout` accessor class.
The living end-to-end example is
`../roku-test-app/src/brsMain/kotlin/com/nuvyyo/roku/components/TestLayout.kt`.

```kotlin
class MainScreen : SceneComponent() {
    private val layout = MainScreen_Layout(top)   // generated stub

    init {
        layout.shelf_view.setFocus(true)          // typed accessor
    }

    companion object {
        @SGLayout
        fun defineLayout() = sceneLayout {
            layoutGroup(id = "mainLayout", layoutDirection = LayoutDirection.horiz) {
                component("ShelfView", id = "shelf_view", focusable = true)
            }
        }
    }
}
```

How it works:

- The compiler extracts the DSL into the component XML `<children>` section.
  Attribute values must be compile-time constants - a non-constant value is
  dropped from the XML with a compiler warning naming the attribute and
  component (set those at runtime in `init {}` instead).
- The kotlin-roku Gradle plugin (`GenerateLayoutStubsTask`) generates the
  `<ClassName>_Layout` stub class (lazily cached `findNode()` getters) for
  every node id in the DSL - including embedded custom components declared
  via `component("Type", id = "...")`.
- The stub compiles as normal Kotlin and IS the runtime implementation; the
  component stores it in its `layout` property (`m.layout` in generated BRS).

**@SGNodeField is NOT for layout-child access.** It declares a node-typed
*interface field* on the component (`<field type="node" nodeType="..."/>`) - a
slot that external code can set/observe. Use Layout accessors for nodes the
component declares itself; use `@SGNodeField` only for node-valued
inputs/outputs on the component's public interface.

## Field Writes: dot-assign vs setField

Device-verified truth table (executable spec: the FieldSemantics suite,
`../roku-test-app/src/brsTest/kotlin/tests/FieldSemanticsTests.kt`, Suite 6;
pinned on device 2026-08-10; case numbers below are that suite's tests).
Roku's ifSGNodeField SDK page is not in `../RokuDocs` yet — SDK doc pending
download; every claim here is device-observed.

| Write | dot-assign (`m.top.f = v` — what `@SG*Field` property writes compile to) | `setField("f", v)` |
|---|---|---|
| Declared field, new value | Observers fire (function AND port form), each write delivers its own value, in write order — no coalescing (case 1) | Identical (case 2); returns `true` |
| Declared field, same value (no `alwaysNotify`) | Observer skipped (case 6) | Identical (case 6) |
| Declared field, wrong type | Silent no-op: no throw, value intact (case 5) | No-op, returns `false` (case 5) |
| UNDECLARED name (the typo trap) | Complete silent no-op: no error, no ad-hoc field, no readback, no observer anywhere (cases 3, render companion) | Same silent drop — and returns `true` from the app main thread (`false` same-thread): the Boolean reports dispatch, not field acceptance (case 4, render companion; task threads expected same but not yet exercised) |
| Runtime `addField` named after an ifSGNodeDict method (`update`) | Behaves as a normal field on current OS, even dot-read (case 7 — informative, don't rely on it) | Works (case 7) |
| Own field, from a LAMBDA body (captured `this` — coroutine or plain) | Works: the compiler routes captured-self access through `.top` (`m.this_0.top.f`), observers fire, reads see the node (case 8; fixed 2026-08-10 — previously a silent AA-key write on the captured m-object) | n/a — with a typed property there is no reason to setField |

**The rule of thumb:** typed property access (`@SG*Field` properties,
compiled to dot-assign) is the safe form — field names derive from validated
annotations, so a misnamed field cannot compile — and it is safe in lambda
scope too (case 8). `setField` is for receivers the type system can't see
through: `RoSGNode`-typed handles (SDK built-in fields like
`control`/`duration`, `addField`-dynamic fields, generic node code). Raw
string names are where typos hide, and the platform gives them NO runtime
signal: the write silently vanishes, and setField's return value lies about
it cross-thread.

**Residual hole (KDoc'd on `collectCapturedComponentSelfFields`):** the
captured-self routing covers compiler-introduced lambda captures only. A
component `this` the user passes around as a T-typed VALUE (stored in a
property, passed as an argument) is statically indistinguishable from a
`createComponent<T>()` node handle — its @SG writes still vanish into the
m-scope AA. Don't alias component `this` into values; a FIR warning is a
possible follow-up guard.

**Observer parity is total on declared fields** — there is no
observer-not-firing trap in choosing dot-assign over setField (cross-thread
included: the typed-task protocol's `m.top.kotlinTaskState` writes are
dot-assign, proven by Suite 4). The real observer trap is ARMING ORDER: an
observer attached after a write never sees it — arm before triggering
(TaskRunner.kt:250-256 arms the state observer before `control=RUN`;
`roundTrip`/`awaitField` in DeviceTestLoop.kt arm before writing).

**NODE-typed field observers RE-FIRE (device finding, 2026-09-03, flagship
boot):** a node-typed `@SGNodeField`'s `@BrsOnChange` observer fires AGAIN on
later scene-graph mutation — ONE parent `setField("screenRef", top)` produced
FOUR handler fires as subsequent graph writes landed (the string/int rows
above are unaffected; the truth table pins only those). Any node-field
@BrsOnChange handler must therefore be IDEMPOTENT — the one-shot-guard idiom
in `../roku-test-app/src/brsMain/kotlin/com/nuvyyo/roku/components/TestScreenChildLabel.kt` is the
pattern (without it the flagship launched four duplicate collectors). The
recycle cycle (`retire → removeChild → … → appendChild → revive`) is more of
the same: removeChild does NOT silence a retained node's observers (spike Q7
F7.3 — see "Component Lifecycle"), so every node-field handler must stay
idempotent across activations. A proper
Suite 6 probe row for node-typed fields is a recorded backlog candidate.

## Component Include Closure (deps.json)

Each SceneGraph component's include closure (the `dependencies` list in
`<Component>.deps.json`, materialized into XML `<script>` tags by the KGP
plugin) is computed in `BrsCompiler`: per-file deps are recorded at emission
time (`recordFunctionDependency`), merged with the stdlib klib manifest's
`brs_file_dependencies`, and resolved transitively. Component XML + deps.json
generation runs in a DEDICATED PASS after every file in the module is
transformed (fixed 2026-08-14). It used to run inside the transform loop,
which made the closure order-dependent: a project file transformed after the
component's own file (the VM-facade shape — a plain-class VM calling stdlib
suspend helpers) had no graph entry yet, so its own deps were silently
dropped from the component's includes — a runtime "Function is not defined"
in component scope without the strict validators. Pinned by the multi-file
golden `components/projectHelperTransitiveDeps` (harness support:
`runMultiFileTest`, which compiles a testData directory as one module in
sorted-name order) and by `validateComponentIncludes` /
`validateTestComponentIncludes` in roku-test-app.

**One compilation per source set (2026-09-04).** kotlin-roku no longer creates a
separate `components` compilation: SceneGraph components are ordinary `brsMain`
classes (the compiler routes each detected component's output to
`components/<Name>/`, everything else to `source/`). The old split made brsMain
declarations unresolvable from component code — its `associateWith`/`libraries`
wiring handed the compiler `.brs` output DIRECTORIES, which `-libraries` silently
ignores (only klibs load) — and marked `components/` as a TEST source set in the
IDE (`associateWith` ⇒ `isTestCompilation`). brsTest sees brsMain (components
included) through `build/brs/klib/main.klib` (`compileMainKlibBrs`).
`roku.componentsDir` is now only the OPTIONAL hand-written-XML directory.

## Key Directories

### Compiler Modules
- `compiler/ir/backend.brightscript/` - IR to BrightScript transformer (IrToBrsTransformer)
- `compiler/cli/cli-brs/` - CLI entry point (K2BrsCompiler)
- `core/compiler.common.brightscript/` - Shared BRS compiler types
- `compiler/fir/checkers/checkers.brs/` - BRS-specific FIR checkers
- `compiler/ir/serialization.brs/` - Klib serialization for BRS
- `brightscript/brs.ast/` - BrightScript AST definitions

### Stdlib
- `libraries/stdlib/brs/` - Stdlib source (builtins/, runtime/, src/)
- `libraries/stdlib/brs-actual/` - Platform-specific implementations
- `libraries/stdlib/brs-prebuilt/` - Pre-compiled klib (checked into VCS)

### Related External Repositories
- `../kotlin-roku/` - Gradle plugin for Roku projects
- `../roku-test-app/` - Test application for validation

## Build Dependency Chain

```
Compiler Sources → Fat JAR → Stdlib klib → Maven Local → kotlin-roku plugin → roku-test-app
```

The rebuild.sh script handles this entire chain correctly. Manual commands break the chain.

## Bootstrap Architecture

### Why BRS Requires Special Handling

This is a fork of the Kotlin compiler. The remote bootstrap compiler from JetBrains
(v2.2.20-Beta2-71, see `gradle.properties:42`) speaks metadata version 2.0.x. If
`:kotlin-stdlib:jvmJar` is forced to run (e.g. by `--rerun-tasks`), the project's
own 2.2.x stdlib lands on `:kotlin-util-klib`'s compile classpath — the 2.0.x
bootstrap compiler cannot read 2.2.x metadata → BUILD FAILED:

```
binary version of its metadata is 2.2.0, expected version is 2.0.0
```

### The Fix: `-Pkotlin.build.useBootstrapStdlib=true`

`rebuild.sh` passes `-Pkotlin.build.useBootstrapStdlib=true` to every Gradle
invocation. This flag (defined in
`repo/gradle-build-conventions/buildsrc-compat/src/main/kotlin/BuildPropertiesExt.kt`)
makes `kotlinStdlib()` (`repoDependencies.kt:58-63`) return the external Maven
bootstrap stdlib instead of `project(":kotlin-stdlib")`. The project's own stdlib
never appears on the bootstrap compiler's classpath.

**DO NOT remove this flag from rebuild.sh.** Without it, any build that re-executes
`:kotlin-stdlib:jvmJar` will fail with the metadata version error above.

### Why `cli-brs:fatJar` Instead of `dist`

The `dist` task builds the full Kotlin distribution: JVM + JS + Native + BRS
(~1381 tasks). BRS only needs the BRS backend. `compiler:cli-brs:fatJar` produces
a self-contained fat JAR with all BRS compiler dependencies bundled (~702 tasks
with `useBootstrapStdlib=true`).

`regenerateKlib` (`brs-prebuilt/build.gradle.kts`) and `generateStdlibBrs`
(`stdlib/build.gradle.kts`) both depend on `:compiler:cli-brs:fatJar`. The fat JAR
is also tracked as a Gradle input — so changes to the compiler automatically trigger
klib and runtime regeneration.

### How `rebuild.sh` Works (Current Architecture)

All 9 steps run on every invocation. Incremental speed comes from two mechanisms:

1. **Build directory cleanup**: BRS compiler module `build/` dirs are deleted before
   each run, forcing only the ~42 BRS-specific Gradle tasks to re-execute. The ~660
   core Kotlin compiler tasks see their outputs unchanged and are `UP-TO-DATE`.
2. **Gradle up-to-date checks**: `regenerateKlib` and `generateStdlibBrs` track the
   fat JAR as an input. If the JAR hasn't changed, those tasks are UP-TO-DATE.

There are no marker files. There is no `--rerun-tasks`. Gradle's own incremental
build machinery is the source of truth.

### Bootstrap Build Sequence

```
:compiler:cli-brs:fatJar  (~702 tasks, useBootstrapStdlib=true)
         ↓
:kotlin-stdlib-brs-prebuilt:regenerateKlib  (uses fatJar as classpath)
         ↓
publishToMavenLocal  (compiler, KGP, stdlib klib, stdlib runtime, kotlin-test-brs)
```

### Fresh Clone Workflow

From a fresh clone, just run:

```bash
./rebuild.sh
```

This handles everything automatically. After completion, all artifacts are in Maven
Local under `com.nuvyyo:*` at version `2.2.20-brs.1`.

## Troubleshooting

### Device Test Run Dies Immediately ("no sentinel found" / "no fresh [KOTLINTEST_END] marker")

The Roku debug console (telnet port 8085) allows exactly ONE client. If another
client holds it — most commonly BrightScript Studio's Roku console tool window,
or a leftover process from a killed run — the device answers every new
connection with a single line, `Console connection is already in use`, and the
runner sees an empty stream: the stdlib runner fails with "no sentinel found",
the E2E runner with "no fresh [KOTLINTEST_END] marker received" within seconds
of launch. Both runners now pre-flight probe the console and abort with a
message naming this cause. Find the holder with `lsof -nP -iTCP | grep 8085`;
if it's the IDE, ask Mike to disconnect its Roku console. Do NOT kill the IDE.

### E2E Re-run With No Source Change Never Relaunches ("no fresh [KOTLINTEST_END] marker" / 300s event timeout on an identical package)

Roku's dev installer answers `mysubmit=Replace` on a byte-identical package
with "Identical to previous version -- not replacing." (HTTP 200) and does NOT
relaunch the app. The kotlin-roku `installRokuTests` task recognizes only
"Install Success" / "Application Received", falls through to a non-failing
"Roku responded with status 200", and sends no ECP launch — `runRokuTests` then
waits 300s for events that never come ("Connection to Roku device timed out"),
the console backlog is byte-identical to the previous run, and ECP
`query/active-app` shows the Home screen. `run-device-tests.sh` does not force
a delete first; the stdlib runner is immune because
`libraries/stdlib/brs/test/run-tests.sh:261-268` sends ECP Home +
`mysubmit=Delete` before every install. Until the wrapper or plugin mirrors
that (recorded tooling backlog — `deleteRoku` exists in kotlin-roku's
RokuPlugin.kt but is not in the `rokuTest` chain), any "re-run once" of the E2E
suite needs a source change or a manual Home + Delete on the dev installer
page. Observed 2026-09-09 (lifecycle program, Task 9 runs 3–4).

### SSL Errors During Gradle Builds

If you see SSL certificate errors, handshake failures, or connection reset errors during Gradle builds, **STOP and ask the user to turn off ZScaler**. This is the cause 99% of the time. Do not try to debug SSL issues yourself.

### Roku Documentation Access

Roku's official documentation (developer.roku.com) blocks bot/programmatic access. If you need to reference Roku SDK documentation:

1. **Provide the URL** to the user (e.g., `https://developer.roku.com/docs/references/scenegraph/widget-nodes/button.md`)
2. **Stop and wait** - the user will manually download the page
3. **Read from `../RokuDocs/`** - downloaded documentation is stored there

**DO NOT** repeatedly try to fetch from developer.roku.com - it will always fail with 403.

## Regenerating FIR diagnostic containers

When you edit `compiler/fir/checkers/checkers-component-generator/src/.../diagnostics/FirBrsDiagnosticsList.kt` (add/rename/remove a BRS FIR diagnostic), regenerate the generated containers:

```bash
./gradlew :compiler:fir:checkers:checkers.brs:generateCheckersComponents --no-configuration-cache
./gradlew :compiler:fir:checkers:generateCheckersComponents --no-configuration-cache
```

The first regenerates `compiler/fir/checkers/checkers.brs/gen/org/jetbrains/kotlin/fir/analysis/diagnostics/brs/FirBrsErrors.kt`. The second regenerates `compiler/fir/checkers/gen/org/jetbrains/kotlin/fir/analysis/diagnostics/FirNonSuppressibleErrorNames.kt` (the cross-platform aggregator). **Commit both generated files.** Then add the new diagnostic's message to `compiler/fir/checkers/checkers.brs/src/org/jetbrains/kotlin/fir/analysis/diagnostics/brs/FirBrsErrorsDefaultMessages.kt`.

This is a manual step — `./rebuild.sh` does not invoke the generator. Follow the upstream JS/Wasm pattern: generated output is committed, regen happens on demand.

## Quick Reference

**rebuild.sh now compile-checks `kotlin-test-brs` whenever the stdlib or compiler
changes** (step 8), and `kotlin-flow-brs` likewise (step 9). This catches FIR-level
regressions that golden file tests and the diagnostic suite don't exercise. If step 8
fails, the new diagnostic or checker change is firing on real `kotlin.test` source; if
step 9 fails, on real FLOW source (`libraries/flow/brs/src`) — fix at the declaration
site with `@Suppress("BRS_<NAME>")` (mirroring Job.Key, ContinuationInterceptor.Key, and
the kotlin.test Test/test() suppression sites) or rework the checker.

| What Changed | Run This |
|--------------|----------|
| Compiler or stdlib code | `./rebuild.sh` |
| Full clean rebuild (nuclear option) | `./rebuild.sh --clean` |
| Everything + test app | `cd ../roku-test-app && ./rebuild-all.sh --all` |
| kotlin-roku plugin only | `cd ../roku-test-app && ./rebuild-all.sh --plugin` |
| Run stdlib tests | `./run-stdlib-tests.sh` |
| Run E2E device tests | `cd ../roku-test-app && ./run-device-tests.sh` |

**Careful with `--plugin --clean`:** `--clean` wipes ALL `com.nuvyyo` artifacts
from Maven Local but `--plugin` republishes only the kotlin-roku plugin - the
compiler/stdlib artifacts stay missing until the next `./rebuild.sh`. Use
`--clean` only together with `--all` (or right after a fresh `./rebuild.sh`).

`./rebuild.sh` handles all cache cleaning automatically. Use `--clean` when things are in a bad state.

## Running Tests

### Compiler Tests (Golden File Tests)

Golden file tests verify that Kotlin code compiles to the expected BrightScript output. No Roku device required.

```bash
# Run all compiler tests
./run-compiler-tests.sh

# Or with Gradle directly
./gradlew :compiler:backend.brightscript:test --tests "*GoldenFile*" --no-configuration-cache -Dorg.gradle.dependency.verification=off
```

### Updating Golden Files

After making intentional changes to the compiler's output:

```bash
# Update golden files with current compiler output
./run-compiler-tests.sh --update

# Or with Gradle
./gradlew :compiler:backend.brightscript:test --tests "*GoldenFile*" -PupdateGoldenFiles=true --no-configuration-cache -Dorg.gradle.dependency.verification=off
```

**Golden testData is a tracked test-task input** (fixed 2026-08-11): the test task
declares `compiler/testData/codegen/brs` via `inputs.dir` in
`compiler/ir/backend.brightscript/build.gradle.kts`. Before that, editing only a
testData `.kt`/`.brs.txt` left `:compiler:backend.brightscript:test` UP-TO-DATE and
silently served stale results. If a golden edit ever appears not to take effect,
check for exactly this class of bug.

### Stdlib Tests (Device Tests)

Run stdlib unit tests on a physical Roku device. Requires device IP and password.

```bash
# Set device credentials
export ROKU_DEVICE_IP=192.168.1.xxx
export ROKU_PASSWORD=your_password

# Run stdlib tests
./run-stdlib-tests.sh

# Build only (no device required) - useful for checking compilation
./run-stdlib-tests.sh --build-only
```

**Stale Log Handling (Sentinel-Based Filtering)**

The Roku debug console (telnet port 8085) has an internal buffer that retains logs from previous runs. The test infrastructure handles this using a **flood + sentinel** approach:

1. **At app startup**: The test adapter prints a unique sentinel marker (`===KOTLINTEST_SENTINEL_<timestamp>===`)
2. **Buffer flood**: 100 lines are printed to push stale logs through the buffer
3. **Filtering**: The test runner finds the sentinel and discards all output before it

This guarantees you only see logs from the current run, regardless of what's in the device buffer.

**The completion monitor is sentinel-scoped too.** The wait loop only accepts a sentinel that
(a) arrived in the capture AFTER deployment finished and (b) has a fresh embedded timestamp
(within 300s, tolerating device clock skew), and it greps for `[KOTLINTEST_END]` / crash
markers ONLY in the lines after that sentinel. Before this fix, a previous run's
`[KOTLINTEST_END]` sitting in the console backlog stopped the capture mid-run and silently
truncated the tail of the suite while still reporting "All tests passed" — if a run ever
reports a suspiciously low total with zero failures, check for exactly this class of bug.

**Why telnet instead of nc (netcat)?**

The test runner uses `telnet` to connect to the Roku debug console, NOT `nc`. This is because `nc` exits immediately after receiving the initial buffer dump from the Roku (about 65 lines), while `telnet` stays connected waiting for more data. When run from a script (vs interactive terminal), `nc` doesn't keep the connection open for incoming data.

**What you'll see:**
```
Filtering output by sentinel...
  Sentinel found at line 847 (3s ago - FRESH)
  Filtered: 156 lines (discarded 846 stale lines from buffer)
```

**If sentinel is stale** (> 120s old):
- The current app didn't start correctly
- Try: Re-run tests, or reboot the Roku device

**If no sentinel found:** the run is a HARD FAILURE (exit 1) — the capture
contains nothing attributable to this run, so the runner refuses to parse it.
(Before 2026-08-11 it "proceeded with unfiltered output", which parsed a
PREVIOUS run's replayed all-green backlog and reported "All tests passed" for
an app that never launched.) Causes, in observed-frequency order:
- The app never launched because the DEVICE-SIDE BrightScript compile failed —
  look for `*** ERROR compiling pkg:/source/<file>.brs` in the raw capture
  (the runner prints its tail). A generated-code syntax error means a compiler
  codegen bug or a known miscompile pattern in new test code.
- App crashed before `startRun()` was called
- Check the unfiltered output (`test/build/test-output.txt`) for details

The test output file is saved to: `libraries/stdlib/brs/test/build/test-output.txt`

### E2E Device Tests (roku-test-app)

Run E2E tests from the roku-test-app project on a physical Roku device.

```bash
# Set device credentials
export ROKU_DEVICE_IP=192.168.1.xxx
export ROKU_PASSWORD=your_password

# Run E2E tests (from roku-test-app directory)
cd ../roku-test-app && ./run-device-tests.sh
```

**Always use the `./run-device-tests.sh` wrapper, not `./gradlew rokuTest`
directly.** The plugin's stream parser will arm on a REPLAYED sentinel from a
previous run if it's under 120s old (plugin nonce fix is backlogged in
kotlin-roku); the wrapper adds a replay guard that closes this false-green
window. Direct `./gradlew rokuTest` BYPASSES that guard.

**What actually runs:** `rokuTest` (KGP task) packages the test app from
`roku-test-app/src/brsTest/kotlin/tests/` + the fixture components in
`roku-test-app/src/brsMain/kotlin/com/nuvyyo/roku/components/fixtures/` (ordinary brsMain
classes — there is no separate components compilation), sideloads it, and parses structured
`[KOTLINTEST_EVENT]` JSON events off the telnet console (sentinel-armed like
the stdlib runner: replayed events from a previous run are discarded).
Results land in `build/test-results/roku/` as JSON + JUnit XML.

**The suites (11 suites, 121 active tests + 3 red-guarded `xtest` placeholders):**

| Suite | File | Exercises |
|-------|------|-----------|
| 0 HarnessSmoke | `tests/HarnessSmokeTests.kt` | driver plumbing, sync + async pass/fail paths |
| 1 ComponentObserver | `tests/ComponentObserverTests.kt` | @SG field writes, @BrsOnChange, rapid sets |
| 2 RenderCoroutines | `tests/RenderCoroutineTests.kt` | coroutines on the render thread, captured vars |
| 3 TaskBoundary | `tests/TaskBoundaryTests.kt` | task-thread round trips via EchoTask fixtures |
| 4 TypedTaskAcceptance | `tests/TypedTaskTests.kt` | `runTask` success/error/overlap/round-trip/derived/cancellation (await wake + task-thread STOP, the flow-program rider) |
| 6 FieldSemantics | `tests/FieldSemanticsTests.kt` | dot-assign vs setField truth table + lambda self-write routing (case 8) + lambda scope-property reads (case 9) |
| 7 CoroutineUtilities | `tests/CoroutineUtilityTests.kt` | awaitAll/coroutineScope/supervisor/withTimeout in the component pumping regime + awaitAll over concurrent `runTask`s |
| 8 ScopeHandle | `tests/ScopeHandleTests.kt` | cross-component scope borrowing: both surfaces, close/watchdog, cancellation both directions, dual-backend + mixed pairs, the flagship child→owner→task-thread chain, retire settles pending requests closed / re-expose after retire+revive / post-retire requests answer closed promptly |
| 9 SharedService | `tests/SharedServiceTests.kt` | reference-shared classes over SetRef: shared-identity mutation chains, guided ISEs, explicit keys, republish + isLive generations, scene stash, static dispatch cross-component (final/base-hook/template/super/suspend), the fn-slot CANARY |
| 10 Flow | `tests/FlowTests.kt` | the flow family on device: 10a flowOn/spawnTask (lift round trips, mid-stream cancel, flatMapLatest switch STOPs the task, spawnTask success/error/cancel, ensureTaskActive) + 10b StateFlow (same- and cross-component collect, late-join current value, equality dedup, burst conflation, collector-death teardown, onCompletion-on-cancel, stateIn bridge) |
| 11 ComponentLifecycle | `tests/ComponentLifecycleTests.kt` | onStart after parent init (layout child), once-per-instance driver reaching the leaf override, `super.onStart()`/`super.onKeyEvent()` static dispatch (incl. component-scope access after a suspension), inherited onKeyEvent reached through the leaf wrapper, dumb component launches nothing, retire → onStop + runTask STOP + idempotence (main-thread callFunc path), full recycle cycle (onStart again, revived scope runs a launch), 1000-instance attach timing (informational, 598 ms); plan-B constructor inputs (5, 2026-09-09): `constructorInputsReadableInOnStartViaConstructorCall` (same-module lowered constructor call → input readable in onStart), `constructorInputsFromTypedBuilderConstant` (plugin-generated builder → XML constant + marker), `rawCreatedInputComponentStaysClosedUntilMarkerAndWatchdogFires` (gate parks, watchdog fires once, manual marker wakes it), `retireCancelsOnStartParkedInAwaitReady`, `constructorInputsSurviveRetireRevive` |

**The main-thread driver:** `tests/TestMain.kt` is a `main()` that creates the
SceneGraph screen, installs the screen's message port as the shared `TestPort`,
shows the (empty) `TestScene`, and calls `runTests { ... }`. Test bodies use the
device-test API from `kotlin.test.device` (`libraries/kotlin.test/brs/src/main/kotlin/kotlin/test/device/DeviceTestLoop.kt`):

- `testAsync("name") { ... }` — suspending test body, pumped by the driver's
  port loop (`runPumping`); default 10s whole-test timeout.
- `awaitField(node, "field") { predicate }` — suspends until the field changes
  to a value the predicate accepts (scoped observers, port-drained between runs).
- `awaitFieldEquals(node, "field", expected)` / `roundTrip(node, set, value, await)` —
  conveniences over `awaitField`.

Predicates must return false rather than throw. Probe nodes are created via
`src/brsMain/kotlin/com/nuvyyo/roku/components/fixtures/` components appended to the TestScene.

### Run All Tests

```bash
# Run compiler tests, then E2E if device is configured
./run-all-tests.sh
```

### View Test Results

```bash
# Display summary of most recent test results
./test-report.sh
```

### Test Locations

| Test Type | Location |
|-----------|----------|
| Golden file tests | `compiler/ir/backend.brightscript/test/.../BrsGoldenFileTests.kt` |
| Golden file test data | `compiler/testData/codegen/brs/` |
| FIR diagnostic tests | `compiler/fir/checkers/checkers.brs/test/` + fixtures in `compiler/testData/diagnostics/testsWithBrsStdLib/` |
| Stdlib tests (source) | `libraries/stdlib/brs/test/kotlin/` |
| Stdlib tests (generated) | `libraries/stdlib/brs/test/build/brs/source/` |
| Device-test API (kotlin.test) | `libraries/kotlin.test/brs/src/main/kotlin/kotlin/test/device/DeviceTestLoop.kt` |
| E2E test suites + driver | `roku-test-app/src/brsTest/kotlin/tests/` (TestMain.kt is the main-thread driver) |
| E2E fixture components | `roku-test-app/src/brsMain/kotlin/com/nuvyyo/roku/components/fixtures/` |

### Current Gate Numbers (as of the constructor-inputs close — plan B, 2026-09-09)

These are the whole-branch green gates; a drop in any of them is a regression.
(Counting note: the gate is EXECUTED tests. A raw `grep -c "@Test"` on
BrsGoldenFileTests.kt reads one high — it counts the commented-out
`// @Test` on the long-disabled brsName golden.)

| Gate | Count |
|------|-------|
| Golden file tests | 108 (plan B, 2026-09-09: +ctorInputs, ctorCallLowering, builderCallExtraction; plan A: +onKeyEventInheritedWrapper, superDispatchComponent, onStartDriver, retireReviveEntries, suspendMemberComponentScope, componentAttachUnconditional replaced componentNoCoroutinesNoPumpAttach) |
| `LayoutInputValidationTest` (unit, gated by `run-compiler-tests.sh` alongside the goldens, counted separately) | 5 |
| FIR diagnostic suite (checkers.brs) | 247 (plan B: +14 `componentInputs/` fixtures, +1 `nameCaseClash/sgComponentBuilderBesideComponentClassOk`) |
| Stdlib device suite | 618 tests / 64 suites (unchanged by plan B; plan A: +4/1 `coroutineFieldShadowingTests` now device-counted; +4/1 `ComponentLifecycle (awaitReady/retire/revive)`) |
| rokuTest E2E | 121 active tests / 11 suites (+3 red-guarded xtests) — Suite 11 ComponentLifecycle 9 → 14 (plan B); Suite 8 30 → 33 (plan A) |
| `validateComponentIncludes` + `validateTestComponentIncludes` | strict mode, 0 findings (no allowlist) |

Verified 2026-09-09 on device (Roku Ultra 4800X, OS 15.3.4): E2E at the plan-B
close (Task 7 round 2: 124 = 121 pass / 0 fail / 3 ignored, first run of the
committed package); stdlib at the plan-A addendum (plan B added no stdlib
device tests and changed no stdlib behavior — one annotation, one marker
writer). Goldens 108 + `LayoutInputValidationTest` 5 and FIR 247 re-run at the
plan-B docs close. Layout unchanged since 2026-09-04 (single BRS compilation,
no `components` compilation; app scripts are `plugins {}` + `roku { test { }
validation { } }`; no dependency substitution — the fork publishes correct
coordinates).

### Test Output

| Test Type | Report Location |
|-----------|-----------------|
| Compiler tests (HTML) | `compiler/ir/backend.brightscript/build/reports/tests/test/index.html` |
| Compiler tests (XML) | `compiler/ir/backend.brightscript/build/test-results/test/*.xml` |
| Stdlib tests (raw log) | `libraries/stdlib/brs/test/build/test-output.txt` |
| Stdlib tests (JSON) | `libraries/stdlib/brs/test/build/results.json` |
| E2E tests (JSON) | `roku-test-app/build/test-results/roku/results.json` |
| E2E tests (XML) | `roku-test-app/build/test-results/roku/results.xml` |