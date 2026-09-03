# Flow Program: Cold Flows, the Task Lift, and StateFlow VM→View — Design

**Date:** 2026-09-02
**Status:** Implemented (2026-09-03)
**Branch:** `feature/brightscript-backend-2.2.20`
**Implements:** the "StateFlow / VM→View propagation" next program recorded in
`docs/superpowers/plans/2026-08-14-shared-service-design.md` §11 and
`docs/superpowers/plans/2026-08-12-scopehandle-design.md` §11 — expanded by Mike's
rulings (decision 1 below) to the broader flow family including background
production (`flowOn`) and the one-shot task block (`spawnTask`), absorbing the
recorded spawnTask program and the M3 task-cancellation backlog item.

## 1. Decisions made during brainstorming (Mike, 2026-09-02)

| # | Decision | Choice |
|---|----------|--------|
| 1 | Surface | Broader flow family: cold `Flow<T>` + operator set + hot `StateFlow`, not StateFlow-only. Driver: background work must be expressible as `flowOn` + related operators, rendering on the render thread |
| 2 | flowOn emissions | Marshallable-only across the task→render hop (FIR-enforced, ScopeHandle checker family); domain mapping happens DOWNSTREAM on the render side (`.map { toDomain(it) }`). No husk re-animation machinery in v1 |
| 3 | One-shot form | `spawnTask {}` ships in this program (same lift machinery as flowOn). `withContext(Dispatchers.IO)` STAYS banned; its FIR message gains the new alternatives. The kotlinx name would over-promise (arbitrary suspension inside the block) — the platform name states the synchronous-block contract honestly |
| 4 | Dispatcher token | `Dispatchers.Task`, required LITERALLY at the `flowOn` call site (the lift is compile-time; a runtime-chosen dispatcher cannot choose a compile-time lowering). `Dispatchers.IO` stays a blanket FIR error |
| 5 | Program shape | ONE spec (this file), phased plan; each phase lands device-green with gates ratcheted (ScopeHandle precedent) |
| 6 | Hot-tier carrier | Node-hosted doorbells (approach 1): value rides the live shared object, wake-up is a version field + scoped observers. Refined during design review (approved): the doorbell field lives on the GLOBAL node, not the publish node — see §6 rationale |
| 7 | Operators | flatMap family REQUIRED in v1: `flatMapConcat` (Rx concatMap), `flatMapMerge` (Rx flatMap), `flatMapLatest` (Rx switchMap); `transformLatest`/`mapLatest`/`collectLatest` ride the same machinery |
| 8 | Task cancellation | Inner `flowOn(Task)` flows are GENUINELY cancellable (a `flatMapLatest` switch must stop the task-side work): two-layer cancellation — cooperative compiler-inserted checks + hard `control="STOP"` — with a rider adopting STOP for `runTask` cancellation (closes the M3 "task-thread work is not stopped" backlog item) |
| 9 | Dispose hooks | Render-side `onCompletion { cause }` + `try/finally` are guaranteed on completion, failure, AND cancellation (kotlinx/Rx doFinally parity). Task-side `try/finally` is best-effort at cooperative checkpoints; hard STOP skips it (platform refcount-release covers native handles). Probe B pins the actual STOP truth |
| 10 | Flow home (post-spike research ruling, 2026-09-02) | Flow ships as a NEW KLIB `kotlin-flow-brs` (package `kotlin.coroutines.flow` via `-Xallow-kotlin-package`), compiled WITHOUT `-Xstdlib-compilation` so operator internals get real suspend state machines — stdlib compilation generates NONE (`BrsLoweringPhases.kt:354` gate; kotlinx-style operator bodies would miscompile silently in stdlib). Mirrors the kotlin-test-brs second-klib precedent. Stdlib gains only small public ambient-node accessors + the `Dispatchers.Task` token. Package correction: `Dispatchers` really lives at `kotlin.coroutines.dispatchers` (not `kotlin.coroutines` as §14 sketched) — `Task` joins the real object |

## 2. Context and load-bearing facts

Why: SharedService (shipped 2026-08-14) gives cross-component *objects*; ScopeHandle
gives cross-component *execution*; this program gives *propagation* — how state
produced in the background reaches every interested view, and how VM state reaches
collectors, kotlinx-style. TestScreen (`../roku-test-app/components/TestScreen/`)
demonstrates today's manual idiom (suspend `load()` → @SG field → `@BrsOnChange`);
this program replaces the manual piping with `Flow`/`StateFlow`.

Device-pinned facts this design rides (sources: CLAUDE.md truth tables, Suite 4/8/9,
scope-handle spike FINDINGS):

- **Declared-field writes deliver EACH value, in write order, no coalescing,
  cross-thread observers included** (FieldSemantics case 1; Suite 4 task-state
  writes). This is what makes an @SG field a real streaming channel for flowOn
  emissions.
- **Runtime-`addField` fields are first-class for observation**; `alwaysNotify=true`
  set at addField time is honored (channel-matrix Q4.1/Q4.2). `addField` on an
  existing field is a no-op (ScopeHandle dual-role note) — both sides can
  ensure-bind race-free.
- **Scoped observers on one field stack**; a function-name observer runs in the
  REGISTERING component's context (ScopeHandle field carrier, Suite 8).
- **The render/task hop is a copying channel**: primitives/String/AA/array deep-copy,
  node refs cross live, function values strip, class instances HUSK (data keys
  survive, methods gone, first dispatch crashes; `as?` passes on the husk). This is
  why decision 2 exists.
- **Shared-object nested state mutation is live cross-component** (Suite 9
  shared-identity mutation chains): a property UPDATE on an object reached through
  the shared reference is visible everywhere. (INSERTS of new keys through a GetRef
  handle copy the inserted value — the design avoids through-handle inserts
  entirely: no registries live in flow objects.)
- **A node handle nested inside a SetRef'd graph loses GetRef capability on the
  receiving side**; whether it can still host observers is UNPINNED spike bait.
  The global-node doorbell (§6) makes the question moot — no node handle ever
  crosses the shared graph.
- **GetGlobalAA is per-component-instance on the render thread** — per-component
  machinery (parked collectors, doorbell registries) follows the
  ComponentMailbox/CoroutineQueue pattern.
- **Arming order law**: an observer attached after a write never sees it — every
  observer in this design arms strictly before the write it must observe.
- **Task threads are synchronous** (no pump): blocking I/O is natural there;
  pump-based suspension is impossible there.

Unpinned corners were §8's spikes — **EXECUTED 2026-09-02** (Roku Ultra 4800X,
OS 15.3.4 build 2402; facts of record in `spikes/flow-spike/FINDINGS.md`). No
design assumption was falsified. New pinned facts this design now rides:

- **Global-node doorbell carrier (Probe A, 12/12):** runtime-addField int on the
  GLOBAL node + `observeFieldScoped` from other components = per-write in-order
  delivery, each handler in its OWN component's context; alwaysNotify honored;
  observers stack; observer-component death detaches IMMEDIATELY (no ghost
  fires, no crash, survivors unaffected, late re-observe works); main-thread
  writes deliver too (informative).
- **Task STOP (Probe B):** `control="STOP"` is a PROMPT HARD KILL — mid
  sleep-loop, mid single 20s blocking sleep, mid `wait()`, and mid compute loop;
  code after the killed point never runs (`finally` cannot run on hard STOP);
  the abandoned node stays safe (reads/writes coherent, repeated STOP no-op);
  a render-side bool-field write IS seen by mid-run task dot-reads and clean
  cooperative unwinding runs post-loop code. RESIDUAL (spike bait): blocked
  sync roUrlTransfer interruptibility — inconclusive on this network (TEST-NET
  connect failed fast); re-probe against a stalling LAN listener.

## 3. Public surface (package `kotlin.coroutines.flow` unless noted)

One import swap from Android: `kotlinx.coroutines.flow.*` → `kotlin.coroutines.flow.*`.
Names and signatures mirror kotlinx exactly for everything shipped. Operators are
regular non-inline functions (klib inline never inlines at user call sites on this
backend; kotlinx's `inline` there is a JVM perf tactic, not semantics).

**Types:** `Flow<T>` (`suspend fun collect(collector: FlowCollector<T>)`),
`FlowCollector<T>` (`suspend fun emit(value: T)`), `FlowCollector<T>.emitAll(flow)`.

**Builders:** `flow { }`, `flowOf(vararg values)`, `Iterable<T>.asFlow()`,
`List<T>.asFlow()`.

**Intermediate:** `map`, `mapLatest`, `filter`, `filterNotNull`, `transform`,
`transformLatest`, `onEach`, `onStart`, `onCompletion`, `catch`,
`distinctUntilChanged`, `take`, `drop`, `conflate`,
`combine(f1, f2, transform)` (2-arity; cuttable if phase 1 bloats),
`flatMapConcat`, `flatMapMerge(concurrency = 16)`, `flatMapLatest`,
`flowOn(context)` (v1: literal `Dispatchers.Task` only).

**Terminal:** `collect { }`, `collect(collector)`, `collectLatest { }`, `first()`,
`first(predicate)`, `firstOrNull()`, `toList()`, `launchIn(scope)`.

**Hot:** `MutableStateFlow(initial)` (plain constructor, kotlinx parity),
`StateFlow<T> : Flow<T>` (`value` get), `MutableStateFlow<T>` (`value` get/set),
`asStateFlow()`, `stateIn(scope, initialValue)` (eager-only v1 — no
`SharingStarted` modes).

**Task world** (package `kotlin.coroutines.task`): `suspend fun <R> spawnTask(block: () -> R): R`,
`fun ensureTaskActive()` (task-side cooperative checkpoint; a no-op when called
off-task, so shared helpers can call it unconditionally).
**Dispatcher token** (`kotlin.coroutines`): `Dispatchers.Task` — a compile-time
token: the only legal use POSITION is the `flowOn` argument (FIR-enforced, §7);
it is not a runtime dispatcher you can `launch`/`withContext` on.

## 4. Tier 1: the cold core

A cold flow runs entirely inside the collector's coroutine: `collect` invokes the
producer body with the downstream collector; no platform machinery, no thread
crossing. Works in ANY coroutine context — component `launch {}`, main-thread
`runBlocking`, the test driver's `runPumping`.

**Contracts (kotlinx semantics, implemented the standard way):**

- Cold body re-executes per collect.
- `emit` entry-checks the job (`context.ensureActive()`, the every-suspend-point
  law) — a cancelled collector stops a producer loop at its next emit, and the CE
  unwinds through `try/finally` and `onCompletion` normally.
- **Exception transparency**: `catch {}` sees UPSTREAM exceptions only and may emit
  fallbacks; collector-block exceptions propagate out of `collect` uncaught.
- `onCompletion { cause }` fires on normal completion (null), failure (the
  throwable), and cancellation (the CE) — the Rx `doFinally` analogue (decision 9).

**The internal same-context queue primitive.** `flatMapMerge`, `flatMapLatest`,
`transformLatest`, `combine`, and `conflate` need concurrent inner collection:
upstream(s) collected in child coroutines feeding one downstream collector. kotlinx
builds these on channels; v1 instead ships a SMALL INTERNAL primitive (not public
API): a same-context SPSC/MPSC queue over the existing parked-continuation + child
coroutine (`coroutineScope`, cancellation) machinery from the utilities program.
All render-thread (or whatever single context the collect runs in) — no thread
safety, no cross-component reach. `flatMapLatest` = collect upstream in a child;
each new value cancels the previous inner-collection child and starts a new one.

**Single-context interleaving note:** children interleave only at suspension
points. An inner flow that never suspends runs to completion synchronously when
started — `flatMapMerge` over non-suspending inners degenerates to concat order.
Comparable to kotlinx on a single confined dispatcher; documented, not fought.

**Honest risk, stated up front:** the cold core is the heaviest exercise of the
backend's suspend/continuation codegen yet (generic suspend lambdas nested through
operator chains). Known open defect classes may surface — `primitive varargs`
directly threatens `flowOf(1, 2, 3)`; statement-splice drops; BrsInline if/else.
The plan front-loads a cold-core device suite in the stdlib runBlocking regime as
the phase-1 exit gate; compiler defects found en route are fixed in-phase
(ScopeHandle precedent: 4 latent defects fixed that way).

## 5. Tier 2: the task lift — `flowOn(Dispatchers.Task)` + `spawnTask {}`

### Lowering

At a `flowOn(Dispatchers.Task)` call site the compiler lifts the ENTIRE upstream
chain expression into a named function and synthesizes a PER-CALL-SITE
TaskComponent:

- typed @SG fields for each capture (by copy, marshallable-only);
- one output field carrying kind-tagged envelope AAs:
  `emit`(seq, value) / `complete`(seq) / `error`(message, number, backtrace) —
  the ScopeWire kind-tag vocabulary, new kinds, unknown kinds ignored;
- `run()` evaluates the upstream chain with a SHIM collector whose `emit` writes an
  envelope (each write delivers in order cross-thread — pinned).

Collector side is runTask-shaped: create fresh unparented node → set capture
fields → ARM the scoped output observer → `control = "RUN"` (arming-order law).
Envelopes enqueue into the collector-side queue primitive; downstream of `flowOn`
is ordinary cold collection in the collector's context. Per-site synthesis keeps
each component's include closure self-contained — no dispatch table, no
binding-table-subset trap (the known ScopeHandle hole class is deliberately not
reintroduced). XML + deps ride the existing dedicated Pass-3 machinery;
`validateComponentIncludes` stays strict-0.

**Why per-site, not a generic dispatcher task:** a generic task needs name-dispatch
plus a binding table filled mid-transform — the exact order-dependence trap class
ScopeHandle carries as a KNOWN HOLE. Per-site components trade more generated XML
for closures that are correct by construction.

### The law list (each FIR-guarded where statically visible — §7)

- **Literal upstream**: the `flowOn` receiver must be a literal flow chain at the
  call site. A `Flow<T>` parameter cannot be lifted (its code isn't visible);
  the error names the fix (declare the chain literally where you `flowOn` it).
- **Captures**: marshallable-only, by copy. Class instances banned INCLUDING
  implicit `this` — an instance-property read must be hoisted by the user
  (`val url = baseUrl`) and the error message says exactly that. (Lazily-evaluated
  cold bodies + auto-hoisting would silently change WHEN the property is read;
  the explicit hoist keeps evaluation time visible.)
- **Emissions**: marshallable-only (decision 2); same checker covers `spawnTask`'s
  return type. Idiom: emit parsed AAs task-side, `.map { toDomain(it) }`
  render-side.
- **Synchronous upstream world**: no suspend calls in the lifted region except
  `emit`/`emitAll`. Blocking I/O (`roUrlTransfer` sync) is the point of being
  there. `delay`-as-blocking-sleep is recorded backlog.
- **Exceptions**: an upstream throw crosses as DATA and is rethrown as
  `TaskException` at the collector; `catch {}` sees it; original exception TYPES
  never cross a thread boundary (runTask precedent; documented divergence).
- **No backpressure across the hop**: the producer never suspends; the render-side
  queue is unbounded. kotlinx's buffer(64)-and-suspend is a documented divergence;
  `conflate()` downstream is the state-shaped answer.
- Fresh unparented task node per collection; no pooling (runTask v1 laws).

### Cancellation (decision 8 — two layers + rider)

Cancelling a `flowOn` collection or `spawnTask` await (including a `flatMapLatest`
switch, `withTimeout` expiry, scope teardown):

1. disarm the output observer, drop queued envelopes;
2. write `__kotlinCancelRequested = true` on the task node (cooperative signal
   FIRST — gives an at-checkpoint thread the chance to unwind cleanly through its
   `finally` blocks);
3. write `control = "STOP"` (hard platform stop).

**Cooperative layer (compiler):** the shim's `emit` checks the cancel field before
delivering each emission; if set, `run()` unwinds via an internal signal — clean
BrightScript unwinding, task-side `try/finally` RUNS. `ensureTaskActive()` gives
user code checkpoints inside long non-emitting compute (kotlinx `ensureActive`
parity for the task world).

**Hard layer (platform):** `control="STOP"` semantics are PINNED (Probe B,
2026-09-02): a prompt hard kill in every probed shape — sleep loop, single 20s
blocking sleep, blocked `wait()`, compute loop; sub-second promptness in all of
them; node-abandonment safe, repeated STOP idempotent; trailing code (finally)
NEVER runs after a hard kill. Residual: blocked sync roUrlTransfer specifically
was inconclusive on this network (fast-fail connect) — recorded spike bait; the
docs promise "hard-prompt" with that one shape footnoted. The Task node SDK page
is still absent from `../RokuDocs`
(https://developer.roku.com/docs/references/scenegraph/control-nodes/task.md).

**runTask rider:** once Probe B pins STOP, `runTask`'s cancellation path adopts the
same sequence — closing the M3 "task-thread work is not stopped" backlog item
program-wide. Suite 4's "runTask await wakes promptly on caller cancellation" test
and the CLAUDE.md runTask/cancellation-promises sections update accordingly.

**Dispose contract (decision 9):** render-side `onCompletion`/`finally` always run
(cancellation included). Task-side `finally` is best-effort at cooperative
checkpoints; a hard STOP mid-blocking-call skips it — the platform
refcount-releases the dead thread's objects, so native handles don't leak, but
user cleanup code is skipped. Probe B PINNED both halves: cooperative unwinding
runs post-loop cleanup (B6); hard STOP skips trailing code entirely (B8) — the
best-effort caveat is required truth, not pessimism.

### `spawnTask {}`

`suspend fun <R> spawnTask(block: () -> R): R` — same lift (per-site component,
capture law, marshallable `R`), NON-SUSPEND block; suspends the caller until the
outcome envelope; rethrows `TaskException` on task-side throw;
cancellation-aware await (parked continuation, caller cancel → the sequence
above). Supersedes the quarantined `withContext(Dispatchers.IO)` re-layering;
the `BRS_IO_DISPATCHER_UNSUPPORTED` message becomes
"use flowOn(Dispatchers.Task), spawnTask {}, or runTask<T>".

**v1 context law:** `flowOn` collection and `spawnTask` require render-thread
component context (guided ISE — runTask precedent).

## 6. Tier 3: the hot tier — `MutableStateFlow` / `StateFlow`

### Architecture: value on the shared object, doorbell on the global node

The VALUE lives on the flow object as a plain property. Wherever the flow object
is reachable — typically inside a SharedService VM acquired by reference — reads
are LIVE (pinned). Sealed-class states keep their methods because they never
cross a copying channel. The DOORBELL is a version-int field
`__kotlinFlow_<uuid>` lazily added to the GLOBAL node (`alwaysNotify=true` at
addField time), observed by collectors via `observeFieldScoped`.

**Why the global node (refinement approved in design review):** every component
reaches its own `m.global` natively, so NO node handle ever crosses the SetRef
graph — the crippled-handle spike vanishes; `MutableStateFlow(initial)` keeps the
kotlinx constructor with zero publish-time binding machinery; plain-class
(repository) StateFlows and same-component use work identically to the shared-VM
case. Cost: doorbell fields accumulate on the global node over an app session
(ints; a field-name reuse pool is recorded backlog — SceneGraph has no
removeField).

### Protocol

**Emit** (`value = x`, non-suspending — kotlinx parity):
1. equality gate: `x == current` → skip entirely (kotlinx dedup; `==` is
   structural — data-class states use generated equals; the pre-existing
   data-class equals/hashCode latent-bug family is recorded and unchanged);
2. store `x` on the flow object (property UPDATE — live cross-component, pinned);
3. ensure-bind the doorbell field, write the incremented version int.

**Collect** (per collector, in a component `launch {}`):
1. ensure-bind + register in the per-component doorbell registry
   (ComponentMailbox pattern: parked continuations keyed by field name, one
   shared `observeFieldScoped` handler per component per field, refcounted);
2. deliver the CURRENT value immediately (late joiners see current state —
   kotlinx contract);
3. park; each doorbell fire (observer runs in the COLLECTOR's context — pinned)
   reads the live value and delivers it unless `==` last-delivered.

Conflation is correct by construction: a slow collector wakes and reads whatever
is newest; reading newer than the doorbell that fired is StateFlow semantics, not
a race. `collect` never completes normally (kotlinx contract) — it ends only by
cancellation, which runs `onCompletion`/`finally` in the collector.

**Teardown:** collector cancellation → deregister, refcount-decrement, last one
`unobserveFieldScoped`s. Collector component death WITHOUT cancellation → scoped
observers auto-detach (Probe A pins); parked continuations die with the
component's GetGlobalAA. VM republish (SharedService generations): the new VM's
flows are new instances with new doorbell fields; stale collectors keep serving
the old instance until their screen dies — consistent with recreate-don't-reuse.

**v1 laws:** emit and collect require render-thread component context (guided
ISE). `value` GET works anywhere (it's a plain property read). Construction works
anywhere. No new OS floor: the doorbell rides `addField` (every OS); the hot tier
inherits OS 15+ only where the VM itself crosses components via SharedService.

**Implementation note (recorded 2026-09-02, adversarial plan review):** interface-
receiver member/accessor calls are fn-slot dispatch on this backend — they record
no include-closure dependency, and on a shared-VM-held flow the slot fn-ref would
cross the SetRef graph on the disclaimed path this toolchain's laws forbid. The
plan therefore adds a small call-site rewrite (`BrsFlowAccessLowering`,
`BrsSharedFromCallLowering` pattern) routing `StateFlow`/`MutableStateFlow.value`
access and interface-typed `Flow.collect(collector)` calls to static flow-klib
functions; `StateFlowImpl` is a plain data holder and no cross-component slot
ever fires. Companion law: cold `Flow` OBJECTS never cross a component boundary
(their lambdas strip); only StateFlow, statically routed, is cross-component.

### `stateIn(scope, initialValue)` — eager-only v1

Launches a collector coroutine in `scope` piping the upstream (typically a
`flowOn` repository flow) into a fresh `MutableStateFlow(initialValue)`; returns
the read-only view. Jobs are same-component-only, so the scope is the OWNING
component's (`componentScope()` passed into the VM — the ScopeHandle-injection
idiom); the bridge dies with the screen. After scope death the StateFlow remains
readable (last value frozen), collectors see no further emissions.

### Flagship acceptance (the modern screen, end-state)

`TestScreenVM`: private `MutableStateFlow<State>(Loading)`, public
`screenState: StateFlow<State>` via `asStateFlow()`; `load()` becomes a
repository flow — `flow { emit(fetchAA(...)) }.flowOn(Dispatchers.Task)` — mapped
to `State` downstream and driven into the state flow. `TestScreen`:
`launch { vm.screenState.collect { screenTitle = renderTitle(it) } }`. A child
fixture acquires the SAME VM via `sharedFrom` and collects the SAME flow
cross-component.

## 7. FIR rule family (all @Suppress-escapable, messages name the fix)

| Diagnostic | Severity | Fires on |
|---|---|---|
| `BRS_FLOW_UPSTREAM_NOT_LITERAL` | ERROR | `flowOn` receiver isn't a literal flow chain at the call site |
| `BRS_FLOW_ON_INVALID_DISPATCHER` | ERROR | `flowOn` argument isn't literal `Dispatchers.Task`; ALSO fires reversed — `Dispatchers.Task` referenced anywhere other than a `flowOn` argument position (it is a compile-time token, not a runtime dispatcher) |
| `BRS_TASK_CAPTURE_UNMARSHALLABLE` | ERROR | lifted region (flowOn upstream / spawnTask block) captures a class instance (incl. implicit `this`), function value, or other unmarshallable — message names the hoist-to-local fix |
| `BRS_TASK_EMIT_NOT_MARSHALLABLE` | ERROR | emission type upstream of `flowOn`, or `spawnTask` return type, outside the marshallable set |
| `BRS_TASK_SUSPEND_IN_LIFTED` | ERROR | suspend call other than `emit`/`emitAll` in the lifted region (walks lambda literals in the upstream expression) |
| `BRS_TASK_CAPTURE_MUTATION_LOST` | WARNING | lifted region assigns a captured `var` (copies — mirror of `BRS_SCOPE_CAPTURE_MUTATION_LOST`) |

Plus: `BRS_IO_DISPATCHER_UNSUPPORTED` message update (decision 3).

Under-approximation by design (BrsScopeMarshallability precedent), disclosed
holes: suspend function REFERENCES passed to upstream operators where the
callee isn't resolvable; generic `T`-typed emissions. (Amended at close,
Task-6 ruling: `Any`-typed values are FLAGGED, not a hole — the shared
BrsScopeMarshallability oracle rejects `Any`, and consistency with the
ScopeHandle rules beats this section's original letter; `@Suppress` or a
cast/hoist is the escape.) Runtime consequence of an escaped suspend-in-lifted: the task side has
no pump — a real park cannot resume; the shim raises a guided error naming the
law rather than hanging (implementation detail for the plan).

Regen procedure: the two documented `generateCheckersComponents` commands; both
generated files committed; messages in `FirBrsErrorsDefaultMessages.kt`.

## 8. Pre-plan device spikes (raw fixtures, scope-handle-spike precedent)

**EXECUTED 2026-09-02** — findings in `spikes/flow-spike/FINDINGS.md`; facts
folded into §2/§5; NO assumption falsified (Probe A 12 PASS/0 FAIL; Probe B
12 PASS/1 control-FAIL/1 INFORMATIVE — the B5 transfer sub-question is the one
recorded residual). The probe specs below are kept as the record of what was
asked; the packages live at `spikes/flow-spike/probe-a/`, `probe-b/`.

**Probe A — global-node doorbell:**
1. runtime-`addField` int on the GLOBAL node from component A; component B
   `observeFieldScoped`s it: observer fires in B's context, per-write, in order;
2. `alwaysNotify=true` honored on repeat values (wraparound safety);
3. two observers (B and C) on one field both fire;
4. auto-detach: destroy B (node removed from scene, no unobserve), A keeps
   writing — no crash, no ghost handler; a fresh component can observe after;
5. (informative) main-thread write to the field fires render-side observers.

**Probe B — task stop:**
1. `control = "STOP"` against: a sleep loop; a blocked synchronous
   `roUrlTransfer` GetToString; a tight compute loop — does the thread die, and
   how promptly;
2. post-STOP node abandonment safety (late field writes land nowhere, no crash);
3. cancel-request field visibility mid-run (cooperative checkpoint works);
4. whether `try/finally` in `run()` executes on STOP;
5. repeated STOP idempotence.

## 9. Verification

- **Goldens** (red-first per SDD): flowOn per-site component synthesis + capture
  fields + shim shape; spawnTask lowering; StateFlow emit/collect codegen;
  representative operator-chain codegen. Gate 79 → grows.
- **FIR fixtures** per diagnostic incl. the disclosed-hole non-firing cases
  (checkers.brs suite). Gate 227 → grows.
- **Stdlib device suite** (runBlocking regime — no components needed): cold-core
  builders/operators/exception transparency/`onCompletion` cause matrix; flatMap
  family incl. inner-flow cancellation; the internal queue primitive.
  Gate 524/53 → grows.
- **E2E Suite 10 "Flow"** (component pumping regime): flowOn round trip,
  mid-stream cancel, switchMap-cancels-task (assert task-side stop via probe
  counters), spawnTask success/error/cancel, StateFlow same- and cross-component
  (owner VM + child collector fixtures), late-join current value, equality dedup,
  conflation under bursts, collector-component-death teardown,
  onCompletion-on-cancel. Gate 86/9 → grows.
- **`validateComponentIncludes` + `validateTestComponentIncludes`**: strict 0
  with synthesized task components in the tree.
- **Flagship**: TestScreen rewritten per §6; stays the living demo.

## 10. Documentation deliverables (CLAUDE.md)

- New "Flow & StateFlow" section: the three tiers, the law lists, the dispose
  contract, the doorbell protocol, marshallable-emission idiom, divergences from
  kotlinx (no backpressure across the hop; TaskException typing; single-context
  interleaving; eager-only stateIn).
- runTask section: cancellation rider (STOP semantics per Probe B).
- Quarantine section: updated `withContext(IO)` message + spawnTask/flowOn as the
  sanctioned successors.
- Gate table numbers.

## 11. Phasing (each phase device-green, gates ratcheted)

- **Phase 0**: spikes (§8) → FINDINGS; fold facts into this spec; re-open any
  falsified section with Mike.
- **Phase 1**: cold core + internal queue primitive + operator set (incl. flatMap
  family) — stdlib suite + goldens; fix latent suspend-codegen defects in-phase.
- **Phase 2**: task lift — `Dispatchers.Task`, flowOn lowering, spawnTask, FIR
  family, cancellation layers + runTask rider — goldens, FIR fixtures, Suite 10
  first half.
- **Phase 3**: hot tier — StateFlow/doorbell/stateIn — Suite 10 second half,
  flagship TestScreen rewrite, CLAUDE.md docs, gate table.

## 12. Risks

- **Suspend-codegen latent defects** (heaviest exercise yet): front-loaded phase-1
  device suite; fix in-phase (precedent: 4 defects fixed during ScopeHandle).
- **STOP semantics**: RESOLVED by Probe B — hard-prompt kill in all probed
  shapes; the sole residual is blocked sync roUrlTransfer (inconclusive on this
  network, recorded spike bait). Documented promise: hard-prompt, transfer shape
  footnoted.
- **Literal-upstream law surprises ported code** (repositories passing `Flow`
  params to a central flowOn helper): FIR message teaches the rewrite; documented
  prominently.
- **Doorbell field accumulation** on the global node: bounded per flow INSTANCE
  (not per emit); name-reuse pool backlogged.
- **Data-class equality latent bugs** affect the dedup gate (pre-existing family,
  recorded in the backlog; not new exposure — same equals the app calls today).
- **Suite 4 cancellation test asserts the OLD runTask behavior** — updated with
  the rider, called out in the plan so the gate doesn't read as a regression.
- **combine/flatMapMerge on one thread**: interleaving only at suspension points
  (§4 note) — documented divergence, matches confined-dispatcher kotlinx.

## 13. Out of scope (recorded)

- `SharedFlow` / event streams (needs non-conflated per-collector queueing).
- `callbackFlow`/`awaitClose`, `channelFlow`, public Channel API.
- `buffer()` sizing / backpressure configs across the task hop.
- `SharingStarted` modes (`WhileSubscribed`, `Lazily`) for `stateIn`.
- `debounce`/`sample` (timer-backed operators), `zip`.
- Doorbell field-name reuse pool (accumulation mitigation).
- Task-side `delay`-as-blocking-sleep.
- Husk re-animation (live class instances across copying channels).
- Multi-module lifted regions (single-module closed world — ScopeHandle/
  SharedService precedent; FIR diagnostic when multi-module becomes real).
- Task-thread `.value` writes (cross-thread StateFlow emit); main-thread collect.
- `@AppScoped` DI and the component-input DX pair (separate programs, unchanged).

## 14. Public API summary (new/changed)

```kotlin
// package kotlin.coroutines.flow
public interface Flow<out T> { public suspend fun collect(collector: FlowCollector<T>) }
public fun interface FlowCollector<in T> { public suspend fun emit(value: T) }
public suspend fun <T> FlowCollector<T>.emitAll(flow: Flow<T>)

public fun <T> flow(block: suspend FlowCollector<T>.() -> Unit): Flow<T>
public fun <T> flowOf(vararg values: T): Flow<T>
public fun <T> Iterable<T>.asFlow(): Flow<T>

public fun <T, R> Flow<T>.map(transform: suspend (T) -> R): Flow<R>
public fun <T, R> Flow<T>.mapLatest(transform: suspend (T) -> R): Flow<R>
public fun <T> Flow<T>.filter(predicate: suspend (T) -> Boolean): Flow<T>
public fun <T : Any> Flow<T?>.filterNotNull(): Flow<T>
public fun <T, R> Flow<T>.transform(block: suspend FlowCollector<R>.(T) -> Unit): Flow<R>
public fun <T, R> Flow<T>.transformLatest(block: suspend FlowCollector<R>.(T) -> Unit): Flow<R>
public fun <T> Flow<T>.onEach(action: suspend (T) -> Unit): Flow<T>
public fun <T> Flow<T>.onStart(action: suspend FlowCollector<T>.() -> Unit): Flow<T>
public fun <T> Flow<T>.onCompletion(action: suspend FlowCollector<T>.(Throwable?) -> Unit): Flow<T>
public fun <T> Flow<T>.catch(action: suspend FlowCollector<T>.(Throwable) -> Unit): Flow<T>
public fun <T> Flow<T>.distinctUntilChanged(): Flow<T>
public fun <T> Flow<T>.take(count: Int): Flow<T>
public fun <T> Flow<T>.drop(count: Int): Flow<T>
public fun <T> Flow<T>.conflate(): Flow<T>
public fun <T1, T2, R> combine(f1: Flow<T1>, f2: Flow<T2>, transform: suspend (T1, T2) -> R): Flow<R>
public fun <T, R> Flow<T>.flatMapConcat(transform: suspend (T) -> Flow<R>): Flow<R>
public fun <T, R> Flow<T>.flatMapMerge(concurrency: Int = 16, transform: suspend (T) -> Flow<R>): Flow<R>
public fun <T, R> Flow<T>.flatMapLatest(transform: suspend (T) -> Flow<R>): Flow<R>
public fun <T> Flow<T>.flowOn(context: CoroutineContext): Flow<T>   // v1: literal Dispatchers.Task

public suspend fun <T> Flow<T>.collect(action: suspend (T) -> Unit)
public suspend fun <T> Flow<T>.collectLatest(action: suspend (T) -> Unit)
public suspend fun <T> Flow<T>.first(): T
public suspend fun <T> Flow<T>.first(predicate: suspend (T) -> Boolean): T
public suspend fun <T> Flow<T>.firstOrNull(): T?
public suspend fun <T> Flow<T>.toList(): List<T>
public fun <T> Flow<T>.launchIn(scope: CoroutineScope): Job

public interface StateFlow<out T> : Flow<T> { public val value: T }
public interface MutableStateFlow<T> : StateFlow<T> { public override var value: T }
public fun <T> MutableStateFlow(value: T): MutableStateFlow<T>
public fun <T> MutableStateFlow<T>.asStateFlow(): StateFlow<T>
public fun <T> Flow<T>.stateIn(scope: CoroutineScope, initialValue: T): StateFlow<T>

// package kotlin.coroutines.task
public suspend fun <R> spawnTask(block: () -> R): R
public fun ensureTaskActive()

// package kotlin.coroutines
public object Dispatchers { /* existing Main; new: */ public val Task: CoroutineDispatcher }
```

> **Footnote (recorded post-ship):** the shipped flow klib exposes a handful of
> public helpers beyond this list — the envelope helpers in `TaskFlow.kt`
> (`buildFlowEmitEnvelope`/`buildFlowCompleteEnvelope`/`buildFlowErrorEnvelope`,
> `flowErrorEnvelopeFrom`, `flowTaskExceptionFrom`) and `__smokeTwoSuspends`
> (`FlowSmoke.kt`). They are public solely because the stdlib test module has
> no friend wiring (plan Task 1 visibility law); they are protocol/test
> machinery, not API. Adopting a `__` prefix for the flow-klib public helpers
> is recorded backlog.
