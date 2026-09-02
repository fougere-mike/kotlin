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
see `roku-test-app/components/ShelfView/ShelfView.kt` (onShelfItemsChanged)
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
This is the only sanctioned way to cross the render/task thread boundary.

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
  await IS cancellation-aware: cancelling the awaiting coroutine wakes it
  promptly (CancellationException at the suspend point, registry entry dropped,
  observer disarmed), but the task-thread `run()` still executes to completion
  on the abandoned node (stopping it is M3 backlog).
- FIR diagnostics guard the pattern: `BRS_TASK_STATE_NOT_FIELD` (task state
  must be @SG interface fields - plain properties are lost across the node
  clone) and `BRS_CREATE_COMPONENT_INVALID_TYPE` (+ an "[IR] " backstop).
- Canonical example: `../roku-test-app/components/ShelfView/ShelfView.kt` +
  `FetchShelfTask.kt` (the flagship demo). Acceptance coverage: E2E Suite 4
  (TypedTaskAcceptance).
- Stdlib implementation: `libraries/stdlib/brs/src/kotlin/coroutines/task/TaskRunner.kt`.

### QUARANTINED: `withContext(Dispatchers.IO)` / TaskPool / runIOWorker

`withContext(Dispatchers.IO)`, `TaskPool`, and the `IOWorkerRegistry`/ioWorker
pipeline are **QUARANTINED - do not use**. That pipeline is unverified on
device, has zero E2E coverage, and its last consumer (ShelfView) was migrated
to `runTask` in Phase 2. It is slated to be re-layered as sugar that
synthesizes a typed task per block (M3 backlog); until then any new
`withContext(Dispatchers.IO)` use is a review-blocking regression.

The quarantine is now COMPILER-ENFORCED: any `Dispatchers.IO` reference in
user code is a FIR ERROR (`BRS_IO_DISPATCHER_UNSUPPORTED` — "use runTask<T>").
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
Suite 8 (ScopeHandle, 30 tests — both carriers, mixed pairs, cancellation both
directions, the flagship child→owner→task-thread chain) + the stdlib wire/
registry unit suites.

### The two surfaces

**1. Hand-written requests** — declare a request object with an explicit wire
name, register a handler owner-side, run it from any child holding a handle:

```kotlin
object RefreshWatchlist : ScopeRequest<String>("RefreshWatchlist")   // 0-arg
object SlowAdd : ScopeRequest2<Int, Int, Int>("SlowAdd")             // arities 0–2

class VmHost : RectangleComponent() {
    private var host: ScopeHost? = null
    init {
        host = exposeScope {                       // one host per component
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
  today (backlog). KNOWN HOLE
  (pump-scan parity): an `exposeScope` call reached only via another file's
  helper escapes the per-file scan — no binding table is injected; hand-
  registered requests still work, and `run {}` blocks get the guided
  dispatch-miss error (lower stakes than the pump hole; KDoc'd at
  `IrToBrsTransformer.fileCallsExposeScope`).

**v1 laws (both surfaces):** render-thread component callers only (like
`runTask`); no timeouts in the API — compose with `withTimeout`; one
`exposeScope` per component (second call throws); request names unique per
owner, `'#'` reserved for compiler-lowered block names; handles are
construction-context-free (mint owner-side, inject into a plain-class VM, call
from any child — all caller-side machinery resolves from the ambient component
at each `run`); `run` against a node that never exposed a scope fails fast
with `IllegalStateException`. Failures cross as DATA:
`ScopeRequestException(message, number, backtrace)` — exception TYPES never
cross a component boundary; domain failures needing typed handling belong in
result values. Caller cancellation sends a best-effort cancel envelope and the
owner cancels the request job (`runTask`'s task-thread non-stop law is
unchanged underneath). A request from the owner to ITSELF dispatches locally
(same-component fast path) with wire-identical semantics — including by-copy
args (`deepCopyAA`).

### The teardown LAW: `host.close()` before retiring an owner node

`close()` (idempotent) tears down the EXPOSED scope only. The default exposed
scope is a DEDICATED child supervisor scope of `componentScope()` — close()
never touches the owner's unrelated coroutines (device-pinned:
scopePostCloseOwnerLaunchAlive), and component-scope cancellation still
cascades into exposed requests. An explicitly-passed scope is cancelled
as-given — the caller owns its blast radius.

- In-flight requests settle "closed": the child's `run` throws
  `ScopeClosedException` (extends `CancellationException`, so an uncaught one
  winds the child down quietly — TimeoutCancellationException precedent).
- Post-close requests are answered "closed" immediately, for as long as the
  node lives (the inbox stays armed).
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

Canonical example: `../roku-test-app/components/fixtures/ScopeOwnerProbe.kt` +
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

**Input timing (interim DX):** node handles arrive via `@SGNodeField` +
`@BrsOnChange` — acquire in the onChange, not in `init` (SG sets fields after
creation). The `createComponent<T> { }` configure-lambda +
`@SGRequired`/`onInputsReady()` pair is the recorded adjacent program (design
decision 10, backlogged). Scene-stash idiom for app-wide services: publish on
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

Canonical example: `../roku-test-app/components/fixtures/SharedOwnerProbe.kt`
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
- The compiler injects `__kotlinPumpAttach(m.top, m.global)` into the
  generated `init()` of every component whose FILE uses coroutines (per-file
  IR scan; task components excluded). Legacy
  `CoroutineScope(Dispatchers.Main).launch` therefore auto-pumps too.
  KNOWN HOLE: coroutine use hidden entirely inside another file's helper
  escapes the scan — `launch {}`/`componentScope()` lazily attach at runtime,
  so prefer them.
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
"runTask await wakes promptly on caller cancellation"); cancellation cascades
through the hierarchy. NOT promised: task-thread work is not stopped — the
cancelled task's `run()` keeps executing to completion on the abandoned node
(stopping it is M3 backlog).

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
demo (`../roku-test-app/components/ShelfView/ShelfView.kt`) fetches ip + shelf
concurrently via `async`/`awaitAll`.

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
`../roku-test-app/components/TestLayout/TestLayout.kt`.

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
changes** (step 8), and `kotlin-flow-brs` likewise (step 9). This catches FIR-level regressions in `kotlin.test` that golden
file tests and the diagnostic suite don't exercise. If step 8 or 9 fails, the new diagnostic
or checker change is firing on real `kotlin.test` source — fix at the declaration site
with `@Suppress("BRS_<NAME>")` (mirroring Job.Key, ContinuationInterceptor.Key, and
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
`roku-test-app/components/fixtures/`, sideloads it, and parses structured
`[KOTLINTEST_EVENT]` JSON events off the telnet console (sentinel-armed like
the stdlib runner: replayed events from a previous run are discarded).
Results land in `build/test-results/roku/` as JSON + JUnit XML.

**The suites (9 suites, 86 active tests + 3 red-guarded `xtest` placeholders):**

| Suite | File | Exercises |
|-------|------|-----------|
| 0 HarnessSmoke | `tests/HarnessSmokeTests.kt` | driver plumbing, sync + async pass/fail paths |
| 1 ComponentObserver | `tests/ComponentObserverTests.kt` | @SG field writes, @BrsOnChange, rapid sets |
| 2 RenderCoroutines | `tests/RenderCoroutineTests.kt` | coroutines on the render thread, captured vars |
| 3 TaskBoundary | `tests/TaskBoundaryTests.kt` | task-thread round trips via EchoTask fixtures |
| 4 TypedTaskAcceptance | `tests/TypedTaskTests.kt` | `runTask` success/error/overlap/round-trip/derived/cancellation |
| 6 FieldSemantics | `tests/FieldSemanticsTests.kt` | dot-assign vs setField truth table + lambda self-write routing (case 8) + lambda scope-property reads (case 9) |
| 7 CoroutineUtilities | `tests/CoroutineUtilityTests.kt` | awaitAll/coroutineScope/supervisor/withTimeout in the component pumping regime + awaitAll over concurrent `runTask`s |
| 8 ScopeHandle | `tests/ScopeHandleTests.kt` | cross-component scope borrowing: both surfaces, close/watchdog, cancellation both directions, dual-backend + mixed pairs, the flagship child→owner→task-thread chain |
| 9 SharedService | `tests/SharedServiceTests.kt` | reference-shared classes over SetRef: shared-identity mutation chains, guided ISEs, explicit keys, republish + isLive generations, scene stash, static dispatch cross-component (final/base-hook/template/super/suspend), the fn-slot CANARY |

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
`components/fixtures/` components appended to the TestScene.

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
| E2E fixture components | `roku-test-app/components/fixtures/` |

### Current Gate Numbers (as of the include-closure transitivity fix, 2026-08-14)

These are the whole-branch green gates; a drop in any of them is a regression.
(Counting note: the gate is EXECUTED tests. A raw `grep -c "@Test"` on
BrsGoldenFileTests.kt reads one high — it counts the commented-out
`// @Test` on the long-disabled brsName golden.)

| Gate | Count |
|------|-------|
| Golden file tests | 79 |
| FIR diagnostic suite (checkers.brs) | 227 |
| Stdlib device suite | 524 tests / 53 suites |
| rokuTest E2E | 86 active tests / 9 suites (+3 red-guarded xtests) |
| `validateComponentIncludes` + `validateTestComponentIncludes` | strict mode, 0 findings (no allowlist) |

### Test Output

| Test Type | Report Location |
|-----------|-----------------|
| Compiler tests (HTML) | `compiler/ir/backend.brightscript/build/reports/tests/test/index.html` |
| Compiler tests (XML) | `compiler/ir/backend.brightscript/build/test-results/test/*.xml` |
| Stdlib tests (raw log) | `libraries/stdlib/brs/test/build/test-output.txt` |
| Stdlib tests (JSON) | `libraries/stdlib/brs/test/build/results.json` |
| E2E tests (JSON) | `roku-test-app/build/test-results/roku/results.json` |
| E2E tests (XML) | `roku-test-app/build/test-results/roku/results.xml` |