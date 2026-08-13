# ScopeHandle: Cross-Component Scope Borrowing — Design

**Date:** 2026-08-12
**Status:** Approved design, pending implementation plan
**Branch:** `feature/brightscript-backend-2.2.20`
**Supersedes:** the open questions (§8) of
`docs/superpowers/plans/2026-08-12-scopehandle-handoff.md` (decision of record:
World 2 + (A); this spec resolves everything the handoff left open).

## 1. Decisions made during brainstorming (Mike, 2026-08-12)

| # | Decision | Choice |
|---|----------|--------|
| 1 | Program scope | ScopeHandle + BOTH riders: TaskRunner `awaitCompletion` retrofit (Phase 0a) and try/finally FIR stopgap (Phase 0b) |
| 2 | Carrier | BOTH backends in v1: field-mailbox floor (OS 9.4+) + roRenderThreadQueue fast path (OS 15+), PumpScheduler-style detection cached on the global node |
| 3 | Timeout backstop | NO timeouts in the API (kotlinx parity — kotlinx never bakes timeouts into await). Docs prescribe `withTimeout` composition. A once-per-request ~30s watchdog console print (owner subtype+id, caller subtype, request key, elapsed) makes the missed-teardown hang diagnosable. Prints once, never re-arms |
| 4 | Single-flight/dedup | App layer's job (a VM keeps its own in-flight Deferred). No request-identity concept in the protocol |
| 5 | Handle acquisition | Explicit node-based: `scopeHandleOf(node: RoSGNode)`. No DI/registry dependency; a future @AppScoped program mints handles on top without changes here |
| 6 | Teardown convention | `ScopeHost.close()` — whoever retires an owner node calls it first. Idempotent. Post-close requests answered "closed" while the node lives |
| 7 | Placement | Stdlib (forced: the primitives it needs are module-internal). Public API + `ScopeClosedException` in `kotlin.brs` (the only default-imported package → zero-import call sites and catch clauses) |
| 8 | Layering | VM-facade is the intended endgame: a plain-class VM wraps `owner.run {}` so call sites just call `vm.refresh()`. The wrapper NEVER lives on a component class (component-`this`-as-value hole) |
| 9 | StateFlow future-proofing | Kind-tagged message envelope (`{kind, key, ...}`, unknown kinds ignored) + child-side machinery factored as a general internal "component mailbox" layer. StateFlow itself is a separate future program |
| 10 | spawnTask | Recorded as coordinated-adjacent (§10): supersedes the M3 `withContext(IO)` re-layering backlog item; separate program after ScopeHandle |

## 2. Context and load-bearing platform facts

The MVVM app-architecture track needs one new runtime primitive: a child component
awaiting work that genuinely executes in an owner component's coroutine scope. The
coroutine runtime shipped 2026-08-11 (utilities program) provides everything local:
real parks (`ParkedContinuation`), `invokeOnCompletion`, cancel-request handlers
(`invokeOnCancelRequest`), hierarchy + supervisor roots, `withTimeout`/`TimeoutCancellationException`.
What it deliberately excludes is cross-component Job sharing — that exclusion is the
seam this program fills.

Facts the design stands on (device-verified unless marked):

- **`GetGlobalAA` is per-component-instance on the render thread.** Every Kotlin
  `object` singleton (CoroutineQueue, TaskRunner, Dispatchers, ...) is per-component
  state. Plain class instances are ordinary heap objects — *believed* shared by
  reference, but **not device-verified** (spike Q1; existential for the shared-VM
  premise).
- **Enqueue is ambient.** Which component's queue a continuation lands in is decided
  by the ambient component at enqueue time, not by the CoroutineContext. Jobs are
  same-component-only; no Job object ever crosses components in this design.
- **Function-name observers and RTQ handlers run in the REGISTERING component's
  context.** This is the platform-native way to execute code "over there" — the whole
  protocol is built on it.
- **SG field writes deliver per-write, in order, no coalescing** (FieldSemantics
  case 1) — one inbox field per node safely carries concurrent traffic.
- **Posts to dead/unregistered receivers are silently dropped** (RTQ: console
  diagnostic only; fields: silent). No NACK exists — this kills any "assert on post
  to dead owner" design and motivates decision 3 and the teardown convention.
- **Observer arming order is law**: an observer attached after a write never sees it.
  Inboxes arm strictly before they are advertised or posted to.
- **`DelayTracker` has no deregistration** — the watchdog's deadline callback may fire
  after its request settled; the registry-miss check makes that a no-op (and on the
  Timer pump backend it costs one stale wakeup — accepted).
- Available primitives and their visibility (all one stdlib module, so `internal`
  suffices): `ParkedContinuation` + `registerCallerCancel`
  (`coroutines/ParkedContinuation.kt`), `Job.invokeOnCompletion` (public),
  `JobImpl.invokeOnCancelRequest` (internal), the `parkScopedBlock` engine,
  `TimeoutCancellationException` (public class, internal ctor — the model for
  `ScopeClosedException`), TaskRunner's correlation-id registry + arming-order
  precedent, PumpScheduler's global-node backend cache + force-hooks precedent.

**Never a transparent `withContext`.** The quarantined `withContext(IO)`/TaskPool
pipeline is the cautionary precedent: invisible context switching, shipped
unverified. The primitive here is explicit at its own layer (`owner.run` is visible
in the VM's source); what an app-layer VM wraps above it is the VM author's
documented contract.

## 3. Phase 0a — TaskRunner `awaitCompletion` retrofit (rider)

`runTask`'s await predates the utilities program: a raw continuation in
`TaskRunner.pending`, no entry check, no mid-park cancel wakeup. This rider is
**structural for ScopeHandle**, not hygiene: the chain *child cancel / owner
`close()` → owner cancels request job → request job parked awaiting `runTask`* only
unwinds promptly if that park wakes mid-cancel.

Changes (all in `libraries/stdlib/brs/src/kotlin/coroutines/task/TaskRunner.kt`):

- `awaitCompletion` suspension block becomes the canonical idiom: entry
  `ensureActive()`; construct `ParkedContinuation`; `registerCallerCancel(parked,
  context)`; `parked.finish()` as the block's last expression.
- The registry stores the `ParkedContinuation` (keyed by taskId as today).
- The caller-cancel handler additionally does what the park cannot know about:
  `TaskRunner.remove(taskId)` + `unobserveFieldScoped(TASK_STATE_FIELD)`.
- `onKotlinTaskStateChanged`/`resumeTask` settle via `parked.tryResume` /
  `tryResumeException(taskExceptionFrom(node))`; `resumeTask`'s hand-rolled
  interceptor routing is deleted (ParkedContinuation.resumeTarget does it).
- KDoc + CLAUDE.md "NOT promised" paragraph updated: awaiting side is now
  cancellation-prompt; task-thread `run()` still executes to completion (M3).

Test: +1 E2E Suite 4 test — cancel a coroutine awaiting `runTask`; assert prompt CE
at the suspend point and no crash when the task later finishes (late terminal write
hits the settled once-guard).

## 4. Phase 0b — try/finally FIR stopgap (rider)

Codegen fact: non-suspend `visitTry` silently DROPS `finallyExpression` — `finally`
never emits outside suspend state machines. ScopeHandle documentation encourages
try/catch patterns right next to this hole; the stopgap lands first.

- New FIR error `BRS_TRY_FINALLY_UNSUPPORTED` (working name): fires on any `try` with
  a `finally` block whose nearest containing callable is **not** suspend (inside
  suspend state machines, finally works). Message: states that the finally block is
  silently dropped by the BrightScript backend today; suggests moving the code into
  a suspend function, restructuring without `finally`, or `@Suppress` with a tracking
  comment for deliberate acceptance.
- Full rollout ritual: `FirBrsDiagnosticsList.kt` → BOTH `generateCheckersComponents`
  regens (commit both generated files) → `FirBrsErrorsDefaultMessages.kt` → fixture
  tests in `compiler/testData/diagnostics/testsWithBrsStdLib/`.
- rebuild.sh step 7 flushes any existing stdlib/kotlin.test try/finally sites — each
  is either fixed (the finally was already being dropped!) or `@Suppress`ed with a
  tracking comment, mirroring the Job.Key suppression sites.

## 5. Phase 1 — device spike (gates API freeze)

`spikes/scope-handle-spike/` + `FINDINGS.md` (house precedent: port-observe,
render-thread-queue, task-node). Results recorded in CLAUDE.md as platform facts.
Per the process expectations, **the API is not frozen until the spike is
device-verified**.

### Q1 — Cross-component heap sharing (existential; added by Mike)

For each channel a value can cross between two render-thread components:

| Channel |
|---|
| SG node field (`setField`/`getField`, AA payload) |
| Global-node field (runtime `addField`) |
| `callFunc` argument (render→render) |
| `callFunc` return value |
| RTQ `postMessage` payload ("data is moved" — may dodge clone semantics entirely) |
| Observer event `getData()` |

test each of: **(a)** plain AA — does a receiver-side mutation appear at the sender
(reference identity)? **(b)** AA containing a node ref — `isSameNode` after the hop?
**(c)** AA containing a function ref — callable on the receiving side? **(d)** a
Kotlin class instance — method callable receiver-side, field mutation visible from
both sides? (Fixture arranges the class's generated file in BOTH components' include
closures and confirms call-dependency tracking does this automatically.)

### Q2 — Ambient identity inside a foreign lambda

Owner invokes a lambda the child created (delivered via whatever channel Q1 proves):
does `GetGlobalAA` inside the body resolve to the **invoker** (owner)? Probe: the
lambda touches a known per-component singleton; observe whose copy changed. Also:
captured child locals intact after the hop?

### Q3 — Suspend machinery across the hop

A `delay()` inside the shipped block must be serviced by the **owner's** pump
(ambient enqueue rule, observed end-to-end). Additionally: a `runTask<T>` initiated
from inside the foreign-created block completes correctly with the owner as the
ambient component (the flagship MVVM chain depends on this).

### Q4 — Protocol plumbing

Runtime-`addField` + `observeFieldScoped` on the added field fires (the TaskPool
shape — never device-verified); `alwaysNotify` honored on runtime-added fields (two
identical consecutive payloads both delivered).

### Decision tree

- **Q1 finds no reference-preserving channel for Kotlin objects** → STOP, escalate to
  Mike: the shared-VM premise pivots (VM state on nodes, or message-only MVI) before
  this API is worth freezing. ScopeHandle survives either way with branch B payloads.
- **Q1 works only via RTQ** → shared-object MVVM is OS 15+ only → Mike decides at a
  checkpoint: raise the architecture floor vs dual-mode.
- **Q2 yes AND a block-delivery path exists** → **branch A** (`owner.run { block }`,
  the preference of record). Candidate block-delivery paths, in order of preference:
  the mailbox payload itself (field AA or RTQ data), a global-node stash + mailbox
  doorbell, a compiler-injected `callFunc` interface function (most machinery; only
  if the cheaper paths fail).
- **Q2 no, or no block path** → **branch B** (typed named requests, §6.4). Same
  protocol, same semantics, carrier-proof payloads.
- **Q4 fails** → inbox fields become *declared* fields via a stdlib base-class shim
  or compiler-injected XML — a mechanism fallback; the API is unchanged.

## 6. Phase 2 — core primitive on the field-mailbox backend

### 6.1 User-facing surface (package `kotlin.brs`)

```kotlin
// ── Owner component ──────────────────────────────────────────────
class VmHost : RectangleComponent() {
    private val host: ScopeHost = exposeScope()   // installs inbox, advertises on this node
    val vm = WatchlistVm(scopeHandleOf(top))      // owner may mint a handle to itself

    fun onDismiss() = host.close()                // THE teardown convention
}

// ── Plain-class VM (the intended facade layer) ───────────────────
class WatchlistVm(private val owner: ScopeHandle) {
    suspend fun refresh(): Items = owner.run { refreshInternal() }
    private suspend fun refreshInternal(): Items { /* executes owner-side */ }
}

// ── Any child component holding the shared vm ───────────────────
launch {
    try {
        render(vm.refresh())                       // hop invisible at call site — VM's contract
    } catch (e: ScopeClosedException) { /* usually NOT caught: quiet wind-down */ }
}
```

- `ComponentBase.exposeScope(scope: CoroutineScope = componentScope()): ScopeHost` —
  owner-side, typically once in `init`. Installs the request inbox (observer/handler
  armed in the owner's context, strictly BEFORE readiness is advertised on the node)
  and publishes the advertisement fields (backend, channel/readiness).
- `ScopeHost.close()` — idempotent. Cancels the exposed scope with a
  `ScopeClosedException` cause → cascades to every in-flight request job → each job's
  single completion egress posts a "closed" outcome to its child → each child park
  wakes with `ScopeClosedException`. The inbox stays armed while the node lives, so a
  late request receives an immediate "closed" reply instead of a silent drop.
- `scopeHandleOf(owner: RoSGNode): ScopeHandle` — child-side (or owner-side, to mint
  a handle for injection). Takes a NODE, never a component reference. Reads the
  advertisement lazily; a `run()` against a node that never exposed a scope fails
  fast with `IllegalStateException`.
- `suspend fun <T> ScopeHandle.run(block: suspend () -> T): T` (branch A) — the only
  new call-site concept. Suspends the caller; the block executes as a child job of
  the owner's exposed scope; the result, failure, or cancellation comes back to this
  suspend point.
- `class ScopeClosedException internal constructor(message: String) :
  CancellationException` — public class, internal constructor
  (TimeoutCancellationException precedent). Uncaught, it winds the child coroutine
  down quietly; the supervisor root keeps the component alive.

**kotlinx-parity notes:** `run` has no kotlinx analogue (deliberately — this is the
one new concept). No timeouts anywhere (parity: kotlinx never bakes timeouts into
awaits; the liveness hole kotlinx closes structurally is closed here by the teardown
convention + watchdog diagnosability). No dedup (parity: `launch`/`async` never
dedup). SCE-as-CE mirrors TimeoutCancellationException's quiet-cancel semantics.

### 6.2 Invariants

- **Construction-context-free handle.** A `ScopeHandle` holds only the owner's
  identity (node + mailbox address). ALL caller-side machinery — response inbox, park
  registry, request-key counter, watchdog — resolves from the AMBIENT component at
  each `run()` call. This is what makes owner-minted handles injectable into shared
  VMs and used from any child. (Own E2E test: handle minted owner-side, called from
  two different children.)
- **Same-component fast path.** When the ambient component IS the owner, `run()`
  skips the mailbox round-trip: launch the block as a child job of the exposed scope
  and await it locally (pure local Job machinery). Identical semantics, including
  `ScopeClosedException` after `close()` and `IllegalStateException` when never
  exposed.
- **No Job crosses components.** Mirror-job protocol only: the child has its park,
  the owner has the request job; correlation is by key.
- **Render-thread components only (v1).** `run()` from main-thread `runBlocking` or
  a task thread throws `IllegalStateException` (no component context to install the
  response inbox on) — mirroring `runTask`'s v1 law.
- **Stdlib handler code uses explicit scope APIs** — owner-side request jobs launch
  via `kotlin.coroutines.builders.launch` on the exposed scope (stdlib code; the
  ComponentBase.launch shadowing trap is a component-file problem, but the import is
  explicit anyway).

### 6.3 Wire protocol (the internal "component mailbox" layer)

Factored as a small internal layer — per-component inbox + key→park registry + local
wake — with the request/response protocol as its first client (decision 9; a future
flow program adds envelope kinds without touching carrier, detection, or arming).

- **Request key:** `"<callerUuid>#<n>"` — one UUID per caller component instance
  (roDeviceInfo.GetRandomUUID, cached per instance) + monotonic counter. Globally
  unique (two children never collide, unlike TaskRunner's per-component int) and
  embeds caller identity for the owner's map and the watchdog line.
- **Envelope:** every message is an AA `{kind, key, ...}`. v1 kinds: `request`
  (child→owner: + `replyTo`, payload), `cancel` (child→owner), `outcome`
  (owner→child: + one of value/error/closed). Handlers ignore unknown kinds.
- **Child side, per `run()`:** entry `ensureActive()` → `ParkedContinuation`
  registered in the ambient component's registry under the key →
  `registerCallerCancel` + a cancel handler that (1) wakes the park locally with CE
  — immediate, free — (2) best-effort posts `{cancel, key}` to the owner, (3) cleans
  up registry + watchdog bookkeeping → arm the once-only watchdog → ensure the
  ambient component's response inbox is installed (lazy, once per component, armed
  before this first post can produce a reply) → post the request → `parked.finish()`.
- **Owner side:** inbox handler (owner context by platform rule) dispatches by kind.
  `request`: launch the block/handler as a child job of the exposed scope, record
  key→Job in the in-flight map, and register the **single egress**:
  `job.invokeOnCompletion { cause -> post outcome }` — one code path for value,
  failure, and cancellation (`close()`'s SCE included); also removes the map entry.
  `cancel`: look up key→Job, `job.cancel()` (best-effort; a miss means the job
  already settled — drop).
- **Failure marshalling:** TaskException-style — `message`/`number`/`backtrace` as
  data in the outcome AA; exception objects never cross. The child rethrows a
  reconstructed exception at the suspend point carrying the ORIGINAL failure info
  (the runtime already guarantees original-cause delivery locally; the protocol
  preserves it across the hop). `closed` outcomes rethrow `ScopeClosedException`.
- **Late/duplicate delivery:** registry-miss or the park's once-guard drops it
  (TaskRunner precedent). Cancelled-then-completed races are harmless by
  construction.
- **Watchdog (decision 3):** one `DelayTracker` entry per request at
  `SCOPE_WATCHDOG_MS` (internal, default 30_000; test hook shrinks it). On fire: if
  the key is still registered, print once —
  `[kotlin.coroutines] ScopeHandle request <key> to owner <subtype>(id=<id>) still
  pending after <n>s (caller: <subtype>) — owner torn down without close()?`
  — and never re-arm. Settled requests make it a silent no-op.

### 6.4 Branch B — typed named requests (if the spike forecloses branch A)

Same protocol; only the payload and call-site sugar change:

```kotlin
// Shared request declarations (plain objects; wire identity = the object's
// fully-qualified generated class name, unique per app by construction):
object RefreshWatchlist : ScopeRequest<Items>

// Owner registers handlers at expose time:
private val host = exposeScope {
    handle(RefreshWatchlist) { refreshInternal() }
}

// Child:
val items = owner.run(RefreshWatchlist)
```

The request payload carries the request NAME (a string — carrier-proof) + optional
marshallable args AA; the owner's handler map (per-owner object state, correct) looks
it up. Unregistered name → immediate error outcome. Semantics (cancellation, close,
watchdog, marshalling) identical to branch A.

### 6.5 Backend A — field mailbox (the floor)

- Owner inbox: runtime-`addField`'d AA field on the OWNER's node (`alwaysNotify`),
  observer armed by the owner at `exposeScope()` time, strictly before the readiness
  advertisement lands on the node. Working field names: inbox `__kotlinScopeInbox`;
  advertisement `__kotlinScope` (string: absent = not a host; `"field"` = field
  backend; `"rtq:<channelId>"` = RTQ backend, doubling as the owner's channel id).
  Child response inbox mirrors the shape on the child's node.
- Child inbox: same shape on the CHILD's node, armed lazily before that component's
  first-ever post.
- Reply address: the child's node reference inside the request AA (spike Q1b
  verifies node-refs-in-AAs survive the field-write clone).
- Concurrency: per-write in-order no-coalescing delivery is device-proven; one inbox
  field per node suffices.

### 6.6 E2E fixtures + Suite 8 (component pumping regime)

Fixtures: `ScopeOwnerProbe` (modes: normal / never-reply for watchdog / close-with-
in-flight) + two child probes (`ScopeChildProbe`, `ScopeChildProbeB`) under
`roku-test-app/components/fixtures/`. New **Suite 8 ScopeHandle**:

1. Happy path: value round-trip through `owner.run`.
2. Failure marshalling: owner-side throw arrives with original message/number.
3. Child-cancel mid-flight: local park wakes immediately with CE; owner's request
   job is cancelled best-effort; late reply is dropped harmlessly.
4. `close()` with in-flight requests from two children: both wake with
   `ScopeClosedException`; owner component itself unaffected (supervisor root).
5. Post-close request: immediate `ScopeClosedException`.
6. Two concurrent children round-tripping interleaved requests (key isolation).
7. Construction-context-free: handle minted owner-side, used by both children.
8. VM-facade: plain-class VM injected with an owner-minted handle; children call
   `vm.refresh()`.
9. Flagship chain: `vm.refresh()` → owner-side block runs `runTask<T>` → result
   propagates back through both parks. Plus its cancellation twin: child cancels
   mid-task → child wakes immediately; owner's request job unwinds at the task park
   (proves rider 0a end-to-end).
10. `withTimeout` composition: `withTimeout { owner.run { delay(long) } }` →
    TimeoutCancellationException at the child; owner job cancelled best-effort.
11. Same-component fast path: owner calls its own `vm.refresh()`.
12. Watchdog: shrunken watchdog + never-reply owner mode → watchdog-fired test hook
    observed (counter/callback hook, PumpScheduler test-hook precedent).

## 7. Phase 3 — RTQ fast-path backend

- Internal carrier interface with the two implementations; detection feature-detects
  once per app session and caches on the global node (PumpScheduler pattern,
  separate cache field).
- Dedicated per-instance channels `kotlin.scope.<uuid>` — NEVER the pump's channel.
  Registration strictly before the channel id is advertised/used as a reply address.
- Reply address on this backend: the child's channel id string in the payload.
- Test hooks: force-field-backend (session + local variants, mirroring
  `kotlinPumpForceTimerBackend*`). Suite 8's carrier-affecting tests (value
  round-trip, failure marshalling, child-cancel, plus the lowered round-trip)
  run on BOTH backends via the local-force hook (PumpBackendProbe precedent);
  carrier-agnostic behaviors are not duplicated — see Addendum A.6's narrowing
  rationale.

## 8. Phase 4 — acceptance + documentation

- Full acceptance sweep of Suite 8 on both backends (carrier-affecting subset,
  A.6); all gates green.
- CLAUDE.md: new "ScopeHandle" section (API, the teardown convention as LAW, the
  watchdog line format, VM-facade guidance, DX notes: node-not-component handles,
  render-thread-only, no timeouts + withTimeout pattern). Spike facts merged into
  the platform-facts sections.
- Memory update (`project_component_coroutine_scaffolding.md` or successor):
  program state + gate deltas.

## 9. Testing

| Regime | What it carries |
|---|---|
| Stdlib device suite (runBlocking regime) | Locally-testable units only: `ScopeClosedException` type/identity, request-key allocation, envelope encode/decode helpers, failure-marshalling round-trip (AA→exception→AA), `run()` outside component context throws IllegalStateException |
| E2E rokuTest (component pumping regime) | Everything real: Suite 8 (§6.6) + the Suite 4 cancellation test (rider 0a) — both backends for the core protocol (carrier-affecting subset, A.6) |
| FIR diagnostic suite | 0b stopgap fixtures (positive, suspend-context negative, @Suppress) |
| Spike | Its own fixture app; results in FINDINGS.md, not a recurring gate |

**Gates (must not drop):** goldens 65 (no codegen changes planned; any incidental
change reviewed diff-by-diff); FIR 219 → +0b fixtures; stdlib device 493/50 → +units
above; E2E 41/7 (+3 xtests) → +1 Suite 4, +Suite 8; `validateComponentIncludes`
strict 0 (new fixtures + stdlib handler files must satisfy the include closure —
call-dependency tracking covers observer callbacks registered by name).

Every phase ends device-green. `./rebuild.sh` is the only build command. Device:
`ROKU_DEVICE_IP=192.168.1.125`; console preflight
`echo | nc -w 3 192.168.1.125 8085` (single-client console — if busy, STOP and ask
Mike to disconnect the IDE console). Commit-per-task on
`feature/brightscript-backend-2.2.20`; every stdlib source change is followed by a
separate `brs-prebuilt: regenerate stdlib klib` commit; roku-test-app is a separate
repo, committed separately.

## 10. Risks

- **Spike Q1 fails entirely** (no reference-preserving channel): the shared-VM
  premise — not just this API — pivots. Mitigation: STOP-and-escalate checkpoint
  before Phase 2; branch B keeps ScopeHandle viable under any outcome.
- **Q1 works only via RTQ**: floor decision (OS 15+ vs dual-mode) escalated to Mike.
- **Q4 fails** (runtime-addField observers don't fire): declared-field fallback via
  stdlib base class or compiler-injected XML — more machinery, API unchanged.
- **Branch A captured-state hazards**: a shipped block capturing the child's
  component `this` hits the documented residual hole. Docs forbid it; the existing
  FIR-warning backlog item (aliasing component `this`) gains priority if branch A
  lands.
- **Watchdog false positives** on legitimately long owner-side work: accepted — one
  benign line phrased as a question, never repeated.
- **Two-repo drift**: fixtures land in roku-test-app while stdlib lands here;
  per-phase gate sweeps run both.

## 11. Out of scope (documented)

- **VM→View state propagation / StateFlow<T>** — separate future program. This
  design deliberately leaves it buildable: the kind-tagged envelope + component
  mailbox layer are exactly the substrate a cross-component flow needs (emit =
  doorbell to each collector's inbox; collector wakes in its own context and reads
  the latest value off the shared object).
- **`spawnTask { }`** (Mike, this session) — explicit task-thread block spawner:
  suspends, runs a non-suspend block on a task thread, returns the result or
  rethrows at the suspend point (TaskException carrying original
  message/number/backtrace; original exception TYPE not reconstructible
  cross-thread). Compiler-assisted: lambda lifted to a named function, synthesized
  task component + name-dispatch, captures marshalled BY COPY as @SG fields, FIR
  guards for non-marshallable captures. Supersedes the M3 "re-layer withContext(IO)
  as typed-task sugar" backlog item. Separate program after ScopeHandle; rider 0a
  serves it too. Composes: `owner.run { spawnTask { fetch() } }`.
- **@AppScoped / DI registry** — separate program; mints ScopeHandles on top of
  `scopeHandleOf` without changes here.
- **Task-thread work cancellation** (M3), **request-node pooling**, **single-flight
  dedup** (decision 4), **cross-thread ScopeHandle callers** (main/task threads),
  **default timeouts** (decision 3).

## 12. Public API summary (new/changed)

```kotlin
// package kotlin.brs (default-imported)

public class ScopeClosedException internal constructor(message: String) :
    CancellationException(message)

public interface ScopeHost {
    public val scope: CoroutineScope
    public fun close()
}

public fun ComponentBase.exposeScope(
    scope: CoroutineScope = componentScope(),
): ScopeHost
// Branch B adds: exposeScope(scope) { handle(Request) { ... } } registration DSL

public class ScopeHandle internal constructor(/* owner node + mailbox address */) {
    public suspend fun <T> run(block: suspend () -> T): T        // branch A
    // branch B instead: public suspend fun <T> run(request: ScopeRequest<T>): T
}

public fun scopeHandleOf(owner: RoSGNode): ScopeHandle

// Changed semantics (Phase 0a): kotlin.coroutines.task.runTask —
// awaiting side now wakes mid-park on caller cancellation (CE at the suspend
// point); task-thread run() still executes to completion (M3 unchanged).

// New FIR diagnostic (Phase 0b): BRS_TRY_FINALLY_UNSUPPORTED — error on
// try/finally whose nearest containing callable is not suspend.
```

---

# Addendum — Stage 1 checkpoint decisions (Mike, 2026-08-12)

Stage 1 (riders + spike) is complete; the spike record and decision walk live in
`spikes/scope-handle-spike/FINDINGS.md` (§6 is the decision of record). This
addendum revises §5–§6 and §12 of this spec accordingly. Where this addendum and
the body disagree, the addendum governs Stage 2.

## A.1 Decisions

| # | Decision | Choice |
|---|----------|--------|
| A1 | API branch | **Branch B wire protocol** — nothing callable crosses any signaling channel (device-proven), and the one fn-slot-preserving path (SetRef) is officially disclaimed by Roku, so the wire never depends on it |
| A2 | User surface | **DUAL, both in Stage 2**: hand-written requests AND compiler-lowered `owner.run { block }` (branch-A ergonomics on branch-B wire) |
| A3 | Diagnostics | **Immaculate-errors mandate**: FIR errors for cannot-work, warnings for reads-differently-than-it-behaves, corrective guidance in every message; runtime protocol errors name the fix |
| A4 | Shared-VM architecture (MVVM track, not Stage 2 scope) | **(ii) + toolchain demotion path**, MVVM layer floor = OS 15.0+; ScopeHandle primitive stays on the 9.4 compile floor |
| A5 | Carrier | Field-copy floor + RTQ-move fast path as decided; **SetRef payload stash backlogged**; MoveIntoField → typed-task output optimization (M3 backlog) |

## A.2 Revised user surface (replaces §6.1's branch alternatives and §6.4)

```kotlin
// ── Hand-written requests (always available) ────────────────────
object RefreshWatchlist : ScopeRequest<Items>("RefreshWatchlist")          // 0-arg
object FetchEpisode : ScopeRequest1<EpisodeId, Episode>("FetchEpisode")    // 1-arg (arities 0–2;
                                                                           // more args → one data holder)
class VmHost : RectangleComponent() {
    private val host = exposeScope {
        handle(RefreshWatchlist) { refreshInternal() }         // owner's own code, owner's scope
        handle(FetchEpisode) { id -> library.fetch(id) }
    }
    fun onDismiss() = host.close()
}
val items = owner.run(RefreshWatchlist)
val ep    = owner.run(FetchEpisode, episodeId)

// ── Compiler-lowered blocks (same wire, branch-A ergonomics) ────
class WatchlistVm(private val owner: ScopeHandle) {
    suspend fun refresh(): Items = owner.run { refreshInternal() }
}
```

Request classes take an explicit wire-name constructor argument
(compile-time-derived names would require compiler magic on the hand-written
surface, which stays compiler-free by design); '#' is reserved for
compiler-generated block names.

Compiler lowering of `owner.run { block }` (per call site with a literal lambda):

- Block lifted to a named generated function (existing lambda machinery); request
  name synthesized deterministically per compilation (e.g. `<fileFq>#<ordinal>`)
  — names only need to agree within one build, both sides are generated together.
- Captures marshalled BY COPY into the request payload AA (see A.3 for what may
  be captured). Wire envelope: `{kind:"request", key, replyTo, name, captures}`.
- **Binding table**: into the generated `init()` of every component that calls
  `exposeScope` (injection precedent: `__kotlinPumpAttach`), the compiler emits a
  name→local-function-reference AA covering every lifted block whose FILE is in
  that component's include closure. Function references are legal within one
  component's own scope — the ban is only on crossing. The VM-facade pattern
  makes coverage automatic: the block lives in the VM class file, and the owner
  includes it because it constructs/references the VM.
- **Dispatch miss** (block's file not in the owner's closure): immediate error
  outcome at the child's suspend point with guidance — "declare the operation in
  a file the owner component includes (typically your VM class)". Never a hang,
  never a husk crash.
- Results marshal back as data (RTQ backend moves on 15+). Large results belong
  in shared VM state, not the response — documented pattern (A.5).

Everything else in §6 stands unchanged: two-hop correlation protocol, request
keys, single-egress owner side, TaskException-style failure marshalling
(original message/number/backtrace rethrown at the suspend point; exception
TYPES do not cross — domain failures that need typed handling are result
values), `ScopeClosedException` on `close()`, watchdog, same-component fast
path, construction-context-free handles, kind-tagged envelope.

## A.3 Diagnostics family (Stage 2 deliverable, per the immaculate mandate)

| Diagnostic | Severity | Fires on | Guidance in message |
|---|---|---|---|
| `BRS_SCOPE_CAPTURE_UNMARSHALLABLE` | ERROR | run-block capturing a function-typed value, component `this`, or other non-marshallable | name the capture, list marshallable kinds, suggest passing data or moving code into the block |
| `BRS_SCOPE_CAPTURE_MUTATION_LOST` | WARNING | run-block writing to a captured `var` (captures cross by copy; owner-side writes never propagate back) | "return a value from the block instead of mutating a capture" |
| `BRS_SCOPE_RESULT_NOT_DATA` | WARNING | run-block/request result type that loses behavior crossing the hop (function-typed, method-bearing class) | "results cross as data; share large/behavioral state via the VM, return data here" |
| runtime dispatch-miss | error outcome | name not in owner's binding table | names the missing file-inclusion fix |

Existing rules continue to apply at the boundary (no component-`this` aliasing;
args/results marshallable — same family as `runTask` inputs and future
`spawnTask` captures; suppression escapes documented per house convention).

## A.4 Decision A4 consequences (recorded here, scoped to the VM/MVVM program)

Day-one FIR rules for shared VMs (final classes; no function-typed properties;
liveness marker helper — `as?` passes on husks), a permanent device canary
pinning fn-slot-through-SetRef behavior, and early verification of the
static-dispatch demotion lowering. NOT Stage 2 scope; the VM program's
brainstorm picks these up.

## A.5 Documented patterns (Stage 2 docs deliverable)

- Big results land in shared VM state; ScopeHandle responses stay small.
- Build ContentNode trees on the task thread; node refs cross every channel by
  reference on every supported OS.
- MoveIntoField typed-task output optimization → M3/runTask backlog (any-thread
  per RokuDocs; only the "copied because externally referenced" half is
  device-pinned so far).

## A.6 Gates impact for Stage 2

Compiler lowering means GOLDENS grow (new lowered shapes: run-block call sites,
binding-table init injection) and the FIR suite grows (A.3 family + fixtures).
Suite 8 (§6.6) gains dual-surface coverage: the core protocol behaviors (value
round-trip, failure marshalling, child-cancel, close-with-in-flight, fast path)
run via BOTH surfaces, plus a dispatch-miss test and a captured-var-warning
fixture; surface-agnostic tests (watchdog, unknown-kind, per-child mint,
interleaving, withTimeout) run once on the hand-written surface — they
exercise carrier/protocol machinery the surface choice cannot affect.
