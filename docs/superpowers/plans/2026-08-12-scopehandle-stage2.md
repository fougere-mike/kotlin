# ScopeHandle Stage 2 Implementation Plan (core + dual surface + lowering + RTQ)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ship the ScopeHandle primitive: cross-component post/await on the branch-B
wire with BOTH user surfaces (hand-written `ScopeRequest` objects AND
compiler-lowered `owner.run { block }`), field-mailbox floor + RTQ fast path,
the A.3 diagnostics family, and Suite 8 device acceptance.

**Architecture:** Per `docs/superpowers/plans/2026-08-12-scopehandle-design.md`
**including its Addendum A.1–A.6 (the addendum governs where they differ)** and the
platform facts in `spikes/scope-handle-spike/FINDINGS.md`. Two-hop correlation-id
protocol over a kind-tagged envelope; no Job and no callable ever crosses
components; owner-side dispatch = hand-registered handler map + compiler-built
binding table of local function refs.

**Tech Stack:** BRS stdlib (`libraries/stdlib/brs`), IR backend
(`compiler/ir/backend.brightscript`), FIR checkers (`checkers.brs`), roku-test-app
E2E (Suite 8), kotlin-roku toolchain.

## Global Constraints

- **Build:** `./rebuild.sh` only. Exceptions: the two `generateCheckersComponents`
  regen commands (documented manual step) and the test-run commands in CLAUDE.md
  "Running Tests".
- **Device:** credentials auto-resolve from `../roku-test-app/local.properties`;
  if a script demands env vars, source the same values from that file.
  Console preflight before EVERY device run:
  `echo | nc -w 3 192.168.1.125 8085` → `Console connection is already in use`
  means STOP and ask Mike. Builds/device runs are long; foreground, never kill.
- **Gate baselines at Stage 2 start (must not drop):** goldens 65 · FIR 220 ·
  stdlib device 493/50 · E2E 42 active / 7 suites (+3 xtests) ·
  `validateComponentIncludes` strict 0. Goldens/FIR/E2E GROW in this stage —
  record each task's new totals in the ledger.
- **Commits:** per-task on `feature/brightscript-backend-2.2.20`; prefixes
  `stdlib:` / `brs:` / `test-infra:` / `docs:`; every stdlib source change is
  followed by a separate `brs-prebuilt: regenerate stdlib klib (<reason>)` commit;
  all messages end `Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>`.
  roku-test-app is a separate repo — commit separately.
- **Stdlib suspend idiom:** every new suspend utility is tail-delegation +
  `ParkedContinuation` with `parked.finish()` as the intrinsic block's LAST
  expression; entry `ensureActive()` via `continuation.context.ensureActive()`
  in-block (the codebase's canonical shape — Job.kt:326, Await.kt:26).
- **Owner-side stdlib code uses `kotlin.coroutines.builders.launch` explicitly**
  (never ComponentBase.launch).
- **Protocol laws (spike-pinned, non-negotiable):** observers armed strictly
  before the field they watch can be written (arming order); payloads are DATA
  only (primitives/String/AA/array/node refs — never function values, never
  class instances); late/duplicate deliveries drop via registry-miss + park
  once-guard; unknown envelope kinds are ignored (forward-compat, spec
  decision 9).
- **Immaculate-diagnostics mandate (A.3):** every new FIR message names the
  problem AND the fix; every runtime protocol error names the fix.
  `@Suppress` escapes documented.
- **No SetRef anywhere in this stage** (backlogged, decision A5). No timeouts in
  the API (decision 3). No dedup (decision 4).

## File Map (who owns what)

| File | Task | Action |
|---|---|---|
| `libraries/stdlib/brs/src/kotlin/brs/scope/ScopeApi.kt` | 1 | Create (public surface) |
| `libraries/stdlib/brs/src/kotlin/brs/scope/ScopeWire.kt` | 1 | Create (constants, keys, envelope, marshalling, deep-copy) |
| `libraries/stdlib/brs/src/kotlin/brs/scope/ComponentMailbox.kt` | 1 | Create (internal general mailbox layer) |
| `libraries/stdlib/brs/src/kotlin/brs/scope/ScopeHostImpl.kt` | 1 | Create (owner side) |
| `libraries/stdlib/brs/test/kotlin/` (new ScopeWire tests file) | 1 | Create (runBlocking-regime units) |
| `../roku-test-app/components/fixtures/ScopeOwnerProbe.kt`, `ScopeChildProbe.kt`, `ScopeChildProbeB.kt`, `ScopeVmFixture.kt` (plain class), `ScopeSleepTask.kt` (reuse SleepTask if suitable) | 2 | Create |
| `../roku-test-app/src/brsTest/kotlin/tests/ScopeHandleTests.kt` (Suite 8) | 2, 3, 5, 7 | Create/extend |
| `compiler/ir/backend.brightscript/src/.../irToBrs/` run-block lowering (locate exact home in the irToBrs package during Task 4) | 4 | Modify |
| `compiler/testData/codegen/brs/scopehandle/runBlockLowering.kt` + `.brs.txt` | 4 | Create (golden) |
| `compiler/testData/codegen/brs/scopehandle/bindingTableInjection.kt` + `.brs.txt` | 5 | Create (golden) |
| `checkers.brs`: `FirBrsScopeBlockChecker.kt` (BLOCK_NOT_LITERAL, Task 4), `FirBrsScopeCaptureChecker.kt` (captures/results family, Task 6) + diagnostics list, regens, messages, fixtures | 4, 6 | Create/Modify |
| `libraries/stdlib/brs/src/kotlin/brs/scope/ScopeRtqBackend.kt` | 7 | Create |
| `CLAUDE.md` ScopeHandle section; memory; backlog notes | 8 | Modify |

Ordering: Task 1 → 2 → 3 are strictly sequential. Task 4 → 5 → 6 sequential
(lowering before binding table before capture checks). Task 7 needs Task 2 (suite
exists). Task 8 last. Task 4 may start any time after Task 1 (it consumes the
`runLowered` internal entry point).

---

# Phase A — core primitive, hand-written surface, field backend

### Task 1: Stdlib protocol core

**Files:**
- Create: the four `libraries/stdlib/brs/src/kotlin/brs/scope/*.kt` files from the map
- Test: new file in `libraries/stdlib/brs/test/kotlin/` following that directory's existing naming/registration conventions (read a neighbor first)

**Interfaces:**
- Consumes: `ParkedContinuation` + `registerCallerCancel` (internal,
  `coroutines/ParkedContinuation.kt:26,117`), `jobImplOf`/`invokeOnCancelRequest`
  (Job.kt:78,174), `Job.invokeOnCompletion` (public), `DelayTracker.current.register`
  (DelayTracker.kt:60), `kotlin.coroutines.builders.launch`, `componentScope()`
  (`brs/ComponentCoroutines.kt`), `RoSGNode` surface incl. `addField` /
  `setField` / `getField` / `observeFieldScoped` / `isSameNode`
  (`brs/roku/SceneGraph.kt`), `TaskException`-style marshalling precedent
  (`coroutines/task/TaskRunner.kt:172-189`), `brsName(::fn)` observer
  registration (TaskRunner precedent), per-instance UUID via roDeviceInfo
  (PumpScheduler.kt:267 precedent).
- Produces (later tasks depend on these exact signatures):

```kotlin
// package kotlin.brs — ScopeApi.kt (all public unless noted)
public class ScopeClosedException internal constructor(message: String) : CancellationException(message)
public class ScopeRequestException(message: String, public val number: Int, public val backtrace: Dynamic?) : RuntimeException(message)

public abstract class ScopeRequest<R>(public val name: String)
public abstract class ScopeRequest1<A1, R>(public val name: String)
public abstract class ScopeRequest2<A1, A2, R>(public val name: String)
// usage: object RefreshShelf : ScopeRequest<Int>("RefreshShelf")
// Names containing '#' are rejected at registration ('#' is reserved for
// compiler-lowered block names — guarantees the two namespaces never collide).

public interface ScopeHost {
    public val scope: CoroutineScope
    public fun close()
}

public class ScopeHandlerRegistry internal constructor(/* owner state */) {
    public fun <R> handle(request: ScopeRequest<R>, handler: suspend () -> R)
    public fun <A1, R> handle(request: ScopeRequest1<A1, R>, handler: suspend (A1) -> R)
    public fun <A1, A2, R> handle(request: ScopeRequest2<A1, A2, R>, handler: suspend (A1, A2) -> R)
    // duplicate name → IllegalStateException("ScopeHandle: request name '<n>' registered twice on this host — request names must be unique per owner")
    // name containing '#' → IllegalStateException("ScopeHandle: request name '<n>' may not contain '#' (reserved for compiler-generated blocks)")
}

public fun ComponentBase.exposeScope(
    scope: CoroutineScope = componentScope(),
    register: ScopeHandlerRegistry.() -> Unit = {},
): ScopeHost

public class ScopeHandle internal constructor(internal val ownerNode: RoSGNode) {
    public suspend fun <R> run(request: ScopeRequest<R>): R
    public suspend fun <A1, R> run(request: ScopeRequest1<A1, R>, a1: A1): R
    public suspend fun <A1, A2, R> run(request: ScopeRequest2<A1, A2, R>, a1: A1, a2: A2): R
    public suspend fun <R> run(block: suspend () -> R): R
    // In Task 1 the block overload's body is:
    //   throw IllegalStateException("ScopeHandle.run(block) requires compiler lowering (BRS_SCOPE_BLOCK_NOT_LITERAL guards call sites); this call was not lowered")
    // Task 4 replaces call sites with runLowered; the body stays as the backstop.
}
public fun scopeHandleOf(owner: RoSGNode): ScopeHandle

// internal — the lowered entry point Task 4 targets (ScopeApi.kt):
internal suspend fun <R> ScopeHandle.runLowered(name: String, captures: RoAssociativeArray?): R
```

```kotlin
// ScopeWire.kt (internal): the exact wire contract every later task assumes
internal const val SCOPE_AD_FIELD = "__kotlinScope"        // "field" (Task 7 adds "rtq:<channelId>")
internal const val SCOPE_INBOX_FIELD = "__kotlinScopeInbox" // AA, alwaysNotify=true, on BOTH owner and child nodes
internal const val SCOPE_WATCHDOG_MS_DEFAULT = 30_000
// Envelope AAs (all values marshal-safe):
//   request: { kind:"request", key, replyTo:<child node>, name, args:<RoArray?>, captures:<AA?> }
//   cancel:  { kind:"cancel",  key }
//   outcome: { kind:"outcome", key, status:"value"|"error"|"closed", value?, message?, number?, backtrace? }
// Request key: "<callerUuid>#<n>" — one UUID per caller component instance
// (cached in a per-component object), monotonic n from 1.
// deepCopyAA(value): recursive AA/array copy, floor-safe (plain loops, no
// roUtils) — used by the same-component fast path so its captures/args have
// identical by-copy semantics to the wire path. Node refs copied by reference.
```

Behavioral contract (implement exactly; each numbered item gets a device test in
Task 2/3):

1. `exposeScope`: installs owner state (per-component object holding: handler
   map name→{arity, fn}, in-flight map key→Job, closed flag, the scope);
   `addField(SCOPE_INBOX_FIELD, "assocarray", true)` +
   `observeFieldScoped(SCOPE_INBOX_FIELD, brsName(::onKotlinScopeInbox))` on the
   owner's `top` — armed BEFORE the advertisement `addField(SCOPE_AD_FIELD,
   "string", false)` + `setField(SCOPE_AD_FIELD, "field")` lands. Runs the
   `register` block against the registry. Idempotent guard: second exposeScope on
   the same component → IllegalStateException naming the rule (one host per
   component).
2. `scopeHandleOf(node)`: stores the node only (construction-context-free — NO
   ambient state captured; the handle may be minted owner-side and used from any
   component).
3. `run(request…)` child-side sequence, in order: entry
   `continuation.context.ensureActive()` (inside the intrinsic block) → ambient
   component check: no component context → IllegalStateException("ScopeHandle.run
   must be called from a render-thread component context (like runTask)") →
   same-component fast path: if ambient top `isSameNode(ownerNode)` → local
   dispatch (item 6) with `deepCopyAA`-copied args, NO mailbox → otherwise read
   `ownerNode.getField(SCOPE_AD_FIELD)`: empty → IllegalStateException("node
   '<id>' has not exposed a scope: call exposeScope() in the owner component
   before minting handles") → ensure the AMBIENT component's child inbox
   installed+armed (lazy once per component: same addField/observeFieldScoped
   shape, handler `onKotlinScopeOutcome`) → allocate key → `ParkedContinuation`
   into the per-component child registry (object, map key→park + watchdog-fired
   counter) → cancel wiring: `registerCallerCancel(parked, context)` PLUS an
   `invokeOnCancelRequest` cleanup handle registered FIRST (removes key from
   registry, best-effort posts `{kind:"cancel", key}` to the owner inbox) — both
   handles appended to `parked.handles` (Task 1 of Stage 1 is the exact
   precedent, TaskRunner.kt) → arm watchdog:
   `DelayTracker.current.register(watchdogMs) { if key still in registry: print
   the line below + increment counter; never re-arms }` → post the request
   envelope via `ownerNode.setField(SCOPE_INBOX_FIELD, envelope)` →
   `parked.finish()`.
   Watchdog line (exact format):
   `[kotlin.coroutines] ScopeHandle request <key> to owner <subtype>(id=<id>) still pending after <n>s (caller: <subtype>) — owner torn down without close()?`
4. `onKotlinScopeInbox` (top-level fn, runs in OWNER context): destroyed-node
   guard (taskEventNodeOrNull precedent) → parse envelope by `kind`; unknown
   kind → return silently. `request`: if closed flag → immediately post
   `{outcome, status:"closed"}` to replyTo; else resolve name in handler map
   (Task 5 adds the binding-table fallback) — miss → post error outcome with
   message `ScopeHandle: no handler for request '<name>' on owner <subtype> — register it in exposeScope { handle(...) } (or, for run{ } blocks, declare the operation in a file the owner includes — typically your VM class)`
   → hit: `kotlin.coroutines.builders.launch(host.scope)` the arity-dispatched
   handler invocation; store key→Job; single egress
   `job.invokeOnCompletion { cause -> ... }`: cause null → status "value" +
   result; cause is CancellationException (ScopeClosedException included) →
   status "closed"; else → status "error" + message/number/backtrace
   (TaskException-style extraction); always: remove in-flight entry, post
   outcome to replyTo's inbox field. `cancel`: in-flight lookup → `job.cancel()`
   (miss = already settled, drop).
5. `onKotlinScopeOutcome` (runs in CHILD context): destroyed-node guard → parse
   → registry remove by key (miss → drop) → status "value" →
   `parked.tryResume(value)`; "error" →
   `tryResumeException(ScopeRequestException(message, number, backtrace))`;
   "closed" → `tryResumeException(ScopeClosedException("owner scope closed"))`.
6. Same-component fast path: resolve handler locally (owner state is ambient) —
   including closed flag (→ throw ScopeClosedException synchronously via the
   park's sync-settle path) and miss (→ ScopeRequestException with the same
   guided message); launch the handler as a child job of the exposed scope and
   await it with the standard local park (invokeOnCompletion +
   registerCallerCancel + finish) — semantics identical to the wire path
   including args deep-copy.
7. `close()`: idempotent (closed flag) → `scope.cancel(ScopeClosedException("scope closed by owner"))`
   → in-flight jobs unwind through their own egress (posting "closed"
   outcomes) → inbox stays armed (late requests get immediate "closed").
8. Test hooks (PumpScheduler precedent, same file as the API):
   `kotlinScopeWatchdogMillis(ms: Int)` (per-component override),
   `kotlinScopeWatchdogFires(): Int` (per-component counter).

- [ ] **Step 1: Write the runBlocking-regime unit tests** (stdlib device suite;
  read a neighboring test file first for the suite/assert conventions):
  - `ScopeClosedException` is a `CancellationException`; `ScopeRequestException`
    carries message/number/backtrace.
  - Request-key format/uniqueness: two allocations differ, both match
    `"<uuid>#<n>"` shape (assert contains "#", monotonic suffix).
  - Envelope build/parse round-trip for all three kinds; unknown kind parses to
    an ignorable result.
  - `deepCopyAA`: nested AA mutation on the copy invisible to the original;
    node refs copied by reference (same instance).
  - `run(request)` outside component context (runBlocking on main thread)
    throws IllegalStateException naming the rule.
  - Registration guards: duplicate name throws; '#' in name throws.
- [ ] **Step 2: Implement the four files** to the contract above.
- [ ] **Step 3: `./rebuild.sh`** — green including step 7.
- [ ] **Step 4: `ROKU_DEVICE_IP=192.168.1.125 ROKU_PASSWORD=pass ./run-stdlib-tests.sh`**
  — 493 + new units, 0 failures. Record the new total.
- [ ] **Step 5: `./run-compiler-tests.sh`** — 65 goldens unchanged (no compiler
  changes in this task; any golden diff here is a stop-and-investigate).
- [ ] **Step 6: Commit** (two commits):

```bash
git add libraries/stdlib/brs/src/kotlin/brs/scope/ libraries/stdlib/brs/test/kotlin/
git commit -m "stdlib: ScopeHandle protocol core — hand-written surface, field mailbox (branch B wire)

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
git add libraries/stdlib/brs-prebuilt
git commit -m "brs-prebuilt: regenerate stdlib klib (ScopeHandle protocol core)

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

### Task 2: E2E fixtures + Suite 8 part 1 (protocol acceptance)

**Files:**
- Create: `../roku-test-app/components/fixtures/ScopeOwnerProbe.kt`,
  `ScopeChildProbe.kt`, `ScopeChildProbeB.kt`, `ScopeVmFixture.kt`
- Create: `../roku-test-app/src/brsTest/kotlin/tests/ScopeHandleTests.kt`
- Modify: `../roku-test-app/src/brsTest/kotlin/tests/TestMain.kt` (register Suite 8
  the way Suites 0–7 are registered — read it first)

**Interfaces:**
- Consumes: Task 1's public API exactly as declared; the E2E driver API
  (`testAsync`, `awaitField`, `awaitFieldEquals`, `roundTrip` —
  `libraries/kotlin.test/brs/src/main/kotlin/kotlin/test/device/DeviceTestLoop.kt`);
  probe install pattern (`createComponent<T>()` + `Probes.adopt(scene, nodeOf(probe))` —
  `tests/Probes.kt`, `TypedTaskTests.kt`).
- Produces: the fixtures + suite file Tasks 3/5/7 extend. Fixture contract:

```kotlin
// ScopeOwnerProbe: RectangleComponent. @SGStringField mode ("normal" | "neverReply" | ...)
// @SGBooleanField ready (set true after exposeScope). @SGStringField lastEvent (alwaysNotify).
// exposeScope in init registers:
object EchoUpper : ScopeRequest1<String, String>("EchoUpper")     // returns input.uppercase()
object Fail42 : ScopeRequest<Int>("Fail42")                        // throws IllegalStateException("boom-42")
object SlowAdd : ScopeRequest2<Int, Int, Int>("SlowAdd")           // delay(3000); a + b
object NeverDone : ScopeRequest<Int>("NeverDone")                  // delay(60_000); 0  (for cancel/close tests)
// "neverReply" mode: exposeScope is NOT called (for the watchdog test — no inbox, no ad)
// closeNow @SGBooleanField @BrsOnChange → host.close()
// ScopeChildProbe / ScopeChildProbeB: @SGStringField outcome (alwaysNotify), @SGStringField op @BrsOnChange
// op dispatch runs launch { } bodies that exercise the child-side API and write results/catch tags into outcome.
// ScopeVmFixture: plain class VmFixture(private val owner: ScopeHandle) {
//     suspend fun echo(s: String): String = owner.run(EchoUpper, s)
// }
```

- [ ] **Step 1: Write the fixtures** per the contract; mirror fixture style from
  `TypedTaskProbe.kt` (package, imports, mode-dispatch shape, scenario header
  comment).
- [ ] **Step 2: Write Suite 8 part 1 tests** (each is `testAsync`; driver-side
  pattern: install owner probe + child probe(s), set child `op`, await child
  `outcome`). Test list with the child-side op bodies:
  1. `scopeRunValueRoundTrip` — child: `outcome = owner.run(EchoUpper, "abc")` →
     expect `"ABC"`.
  2. `scopeRunTwoArgs` — `owner.run(SlowAdd, 2, 3)` with `withTimeout(8000)` at
     the DRIVER level absent — plain await → expect `"5"` (also implicitly
     proves multi-second requests survive).
  3. `scopeRunFailureMarshalling` — `try { owner.run(Fail42) } catch (e:
     ScopeRequestException) { outcome = "err:" + (e.message ?: "") }` → expect
     the ORIGINAL "boom-42" text present.
  4. `scopeChildCancelMidFlight` — child launches `owner.run(NeverDone)` in a
     job, delay(250), `job.cancel()`, catch CancellationException → outcome
     "cancelled" promptly (well under NeverDone's 60s); owner unaffected.
  5. `scopeCloseWithInFlightTwoChildren` — both children start `NeverDone`
     variants, driver flips `closeNow` → BOTH children's catches see
     `ScopeClosedException` → outcomes "closed"; owner probe still alive
     (write/read one of its fields after).
  6. `scopePostCloseImmediate` — after close, child runs `EchoUpper` → catches
     ScopeClosedException immediately (no watchdog wait, no hang).
  7. `scopeTwoChildrenInterleaved` — A runs SlowAdd(1,2), B runs EchoUpper
     concurrently; both outcomes correct (key isolation).
  8. `scopeOwnerMintedHandle` — owner probe exposes `vmEcho` op that hands its
     OWN `scopeHandleOf(top)`-built VmFixture to both children via a node-field
     handshake… (fixtures share the VmFixture instance through the scene-level
     test driver: driver creates VmFixture? NO — plain objects can't cross
     components. THE TEST: each child mints its own handle from the owner NODE
     and uses it — proving construction-context-free means node-identity-only:
     `scopeHandleOf(ownerNode)` in child A and child B both work against the
     one host. Name the test `scopeHandlePerChildMint`.)
  9. `scopeSameComponentFastPath` — the OWNER probe itself runs
     `owner.run(EchoUpper, "self")` via an op → "SELF"; assert it works with
     the host closed→reopened? No: plain success + separately
     `scopeSameComponentClosed` → after close, owner's own run throws
     ScopeClosedException synchronously.
  10. `scopeUnknownEnvelopeKindIgnored` — child posts a raw
      `{kind:"future-thing", key:"x"}` AA directly to the owner's
      `__kotlinScopeInbox` via setField, then runs a normal EchoUpper → still
      works (forward-compat; nothing crashed).
- [ ] **Step 3: Build + red check.** `cd ../roku-test-app && ./rebuild-all.sh --all`
  then `./run-device-tests.sh`. The suite must COMPILE and every test above must
  PASS (Task 1 shipped the implementation — this is acceptance, not TDD-red;
  the red honesty came from Task 1's unit layer). Any failure here is a Task 1
  defect: STOP, report it, fix in Task 1's files, re-run.
- [ ] **Step 4: Gates.** E2E total = 42 + 11 = 53 active (record actual);
  `validateComponentIncludes` strict 0 (new fixtures must pass the closure check).
- [ ] **Step 5: Commit** (roku-test-app):

```bash
git add components/fixtures/ src/brsTest/kotlin/tests/
git commit -m "test-infra: Suite 8 ScopeHandle — protocol acceptance (hand-written surface, field backend)

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

### Task 3: Suite 8 part 2 — facade, runTask chain, composition, watchdog

**Files:**
- Modify: `../roku-test-app/components/fixtures/ScopeOwnerProbe.kt` (+`FetchViaTask`
  request using the existing `SleepTask`/`FetchShelfTask`-style fixture),
  `ScopeChildProbe.kt` (+ops)
- Modify: `../roku-test-app/src/brsTest/kotlin/tests/ScopeHandleTests.kt`

**Interfaces:**
- Consumes: Tasks 1–2; `runTask<T>` (cancellation-prompt since Stage 1 rider 0a);
  `withTimeout`/`TimeoutCancellationException`; watchdog hooks
  `kotlinScopeWatchdogMillis(ms)`, `kotlinScopeWatchdogFires()`.
- Produces: complete hand-written-surface acceptance; the op vocabulary Task 5
  reuses for the ×2 lowered-surface pass.

- [ ] **Step 1: Add owner-side request + fixture:**
  `object FetchViaTask : ScopeRequest1<Int, String>("FetchViaTask")` whose
  handler does `runTask<SleepTask> { durationMs = it }` then returns "done" —
  wire SleepTask input so 0 → instant, big → cancellable. Also bump the
  Stage 1 `cancelawait` probe's SleepTask duration 5000→8000ms in the same
  commit (ledgered knife-edge: 5000 == roundTrip timeout made regression modes
  ambiguous).
- [ ] **Step 2: Tests:**
  11. `scopeVmFacade` — child op constructs `VmFixture(scopeHandleOf(ownerNode))`
      and calls `vm.echo("hi")` → "HI" (the facade layering, call-site-invisible
      hop).
  12. `scopeFlagshipRunTaskChain` — child runs `FetchViaTask(0)` → "done": the
      full UI → owner scope → task thread → back chain.
  13. `scopeFlagshipCancellationTwin` — child starts `FetchViaTask(8000)`,
      cancels at 300ms → child wakes "cancelled" promptly; owner's request job
      unwinds at its (now cancellation-prompt) task park; no crash when the
      task's 8s expires later.
  14. `scopeWithTimeoutComposition` — child wraps `owner.run(NeverDone)` in
      `withTimeout(1500)` → catches TimeoutCancellationException; owner-side
      job receives the best-effort cancel.
  15. `scopeWatchdogFiresOnce` — child sets `kotlinScopeWatchdogMillis(400)`,
      runs a request against the "neverReply" owner (never exposed — but note
      run() fails FAST on missing advertisement, so the never-reply shape is:
      owner EXPOSED but its handler map has `NeverDone` and mode suppresses…
      NO — simplest true-to-life shape: owner exposed, request `NeverDone`
      (legitimately long), watchdog at 400ms fires once while pending; assert
      `kotlinScopeWatchdogFires() == 1` after ~1s and still `== 1` after
      another 1s (prints once, never re-arms); then cancel the job to clean up.
- [ ] **Step 3: Device run + gates.** E2E = previous + 5 (record). All green.
- [ ] **Step 4: Commit** (roku-test-app): `test-infra: Suite 8 part 2 — facade, runTask chain, withTimeout, watchdog` (+ trailer).

---

# Phase B — compiler lowering + diagnostics (the dual surface)

### Task 4: Lower `owner.run { block }` call sites + BRS_SCOPE_BLOCK_NOT_LITERAL

**Files:**
- Modify: `compiler/ir/backend.brightscript/src/.../transformers/irToBrs/` — the
  lambda/call lowering home (locate by reading how `runTask<T> { ... }`'s
  configure-lambda and ordinary lambdas are compiled TODAY; put the run-block
  rewrite alongside, mirroring the existing intrinsic-call-detection style in
  `IrExpressionToBrsTransformer.kt`)
- Modify: `core/compiler.common.brightscript/.../BrsStandardClassIds.kt` (IDs for
  `kotlin.brs.ScopeHandle`, `run`, `runLowered` so detection is symbol-based, not
  string-matching — mirror how runTask/BrsCreateObject IDs are declared)
- Create: `compiler/testData/codegen/brs/scopehandle/runBlockLowering.kt` + golden
- FIR: `FirBrsDiagnosticsList.kt` + regens + messages + fixture
  `compiler/testData/diagnostics/testsWithBrsStdLib/scopeBlockNotLiteral.kt` +
  checker `FirBrsScopeBlockChecker.kt` (registrar: same object as Stage 1's
  try/finally checker)

**Interfaces:**
- Consumes: Task 1's `internal suspend fun <R> ScopeHandle.runLowered(name: String, captures: RoAssociativeArray?): R`.
- Produces: lowered call shape later tasks rely on —
  every `scopeHandle.run(<literal suspend lambda>)` call site compiles as if the
  user wrote `scopeHandle.runLowered("<fileFqName>#<n>", <capturesAA>)`, plus one
  emitted top-level BRS function per block:
  `function <mangledBlockName>(captures as Object) as Object` whose body is the
  lifted block with each captured value read from `captures` at entry. Name
  synthesis: `<fileFqName>#<n>` where n = 1-based ordinal of run-block call sites
  within that file, in source order (deterministic within a compilation; both
  sides generated together, so cross-build stability is NOT required).
- FIR error `BRS_SCOPE_BLOCK_NOT_LITERAL`: `run(block)`'s argument must be a
  literal lambda at the call site. Message: "ScopeHandle.run { } requires a
  literal lambda at the call site (the compiler lifts it into a named request).
  Passing a stored function value cannot be lowered — declare a ScopeRequest
  object and use run(request, args) instead."

- [ ] **Step 1: FIR first (red).** Add the diagnostic, regen BOTH containers
  (commit generated files), message, checker (fires on `run(block)` overload
  calls whose argument is not an `IrFunctionExpression`-shaped literal — at FIR
  level: not an anonymous-function/lambda argument), fixture with: literal
  lambda CLEAN, `val f: suspend () -> Int = {...}; owner.run(f)` ERROR,
  `@Suppress` escape CLEAN. Run the FIR suite — 220 + new (record). Also
  append case 6 to tryFinallyNonSuspend.kt in the same commit: try/finally in
  a property initializer (no containing callable) → ERROR via the else->false
  fall-through (ledgered gap — pins the initializer path against future
  checker refactors).
- [ ] **Step 2: Golden red.** Write `runBlockLowering.kt` testData: a component
  file with two `owner.run { }` sites — one zero-capture, one capturing a
  `val shelfId: Int` and a `val node: RoSGNode` — plus one hand-written-request
  call as a control (must NOT be touched by the lowering). Generate the CURRENT
  golden (`./run-compiler-tests.sh --update` scoped to the new test) and
  verify it shows the UNLOWERED shape (the Task 1 backstop call). This is the
  honest red.
- [ ] **Step 3: Implement the lowering.** Detection by symbol (ClassIds from
  this task), rewrite to `runLowered(name, captures)`; captures AA construction
  `{ "<captureName>": <IrGetValue> , ... }` (empty → pass `invalid`/null);
  emit the lifted function reading captures by the same names at entry, then
  the original body. VERIFY-IN-PLACE before writing: how the existing lambda
  lowering names generated functions and where emitted top-level functions are
  registered for file output + include-closure/dep tracking (the lifted block
  must be visible to `validateComponentIncludes` exactly like any other
  generated function so owners that include the file get it packaged).
- [ ] **Step 4: Golden green** — regenerate, review the diff LINE BY LINE
  (expected: the two rewritten call sites, two lifted functions, control call
  unchanged), commit the reviewed golden. Goldens 65 + 1 (record).
- [ ] **Step 5: Full gates.** `./rebuild.sh` (step 7 green — the stdlib itself
  contains no run{} blocks yet, so no stdlib lowering effects), FIR suite,
  stdlib device suite, E2E (all Suite 8 part 1/2 still green — the hand path
  must be untouched).
- [ ] **Step 6: Commit** (`brs:` prefix): lowering + ClassIds + golden in one
  commit; FIR bits in the same commit (they gate the same feature).

```bash
git add compiler/ core/compiler.common.brightscript/
git commit -m "brs: lower ScopeHandle.run{block} to named requests (runLowered) + BRS_SCOPE_BLOCK_NOT_LITERAL

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

### Task 5: Binding-table injection + owner dispatch integration + lowered-path E2E

**Files:**
- Modify: the component-XML/init generation site that injects
  `__kotlinPumpAttach` (find it by grepping the backend for `__kotlinPumpAttach`;
  the binding-table injection is a sibling) + the per-file IR scan that decides
  which components get it
- Modify: `libraries/stdlib/brs/src/kotlin/brs/scope/ScopeHostImpl.kt`
  (binding-table fallback in dispatch) + `ScopeWire.kt` if a helper is needed
- Create: `compiler/testData/codegen/brs/scopehandle/bindingTableInjection.kt` + golden
- Modify: `../roku-test-app/` fixtures + `ScopeHandleTests.kt` (lowered-surface tests)

**Interfaces:**
- Consumes: Task 4's lifted functions + name scheme; Task 1's owner dispatch.
- Produces: components that call `exposeScope` get, in generated `init()`, AFTER
  the existing injections:
  `m.__kotlinScopeBindings = { "<fileFq>#<n>": <liftedFunctionName>, ... }` —
  one entry per lifted run-block whose FILE is in that component's include
  closure (the closure is already computed for XML generation — reuse it).
  Owner dispatch order becomes: hand-registered map → `m.__kotlinScopeBindings`
  lookup (invoke `binding(captures)` as the handler body) → guided-miss error
  outcome (Task 1's message already covers both surfaces).
  Stdlib reads the table via a small `@BrsInline`/external accessor (m-scope
  read — VERIFY the idiom against how PumpScheduler reads m-scope state).

- [ ] **Step 1: Golden red** — testData: one component that calls `exposeScope`
  and whose closure contains the Task 4 testData file's blocks; current golden
  shows init WITHOUT the table.
- [ ] **Step 2: Implement injection** (per-file scan: component calls
  exposeScope → emit table; table contents from the closure's lifted-block
  registry built during Task 4's emission). Regenerate golden, review
  line-by-line, commit-ready.
- [ ] **Step 3: Stdlib dispatch fallback** (+ klib regen commit at the end).
- [ ] **Step 4: Lowered-surface E2E.** Fixtures: `ScopeVmFixture` gains
  `suspend fun echoLowered(s: String): String = owner.run { echoUpperLocal(s) }`
  (echoUpperLocal = a plain private fun in the fixture FILE — exercising a
  capture (`s`) and a same-file helper call). ScopeOwnerProbe constructs the
  VmFixture (pulling the file into its closure). Lowered-surface coverage is
  deliberately the protocol-affecting subset (see addendum A.6's rationale):
  value/failure/cancel/close/fast-path/dispatch-miss/capture-semantics;
  surface-agnostic behaviors are covered once in Tasks 2–3. Tests (mirror the
  part-1 numbering, lowered flavor):
  16. `scopeLoweredValueRoundTrip` — `vm.echoLowered("abc")` → "ABC".
  17. `scopeLoweredFailure` — a `vm.failLowered()` op whose block throws
      IllegalStateException("boom-lowered") → ScopeRequestException carrying
      the original message.
  18. `scopeLoweredChildCancel` — block body `delay(60_000)`; child cancels at
      250ms → prompt CE.
  19. `scopeLoweredCloseInFlight` — close() while a lowered block is in flight
      → ScopeClosedException at the child.
  20. `scopeLoweredFastPath` — owner runs its own `vm.echoLowered("self")`.
  21. `scopeDispatchMissGuided` — a SECOND VM fixture file that the owner does
      NOT include (child-only helper class with a run{} block); child calls it
      → ScopeRequestException whose message contains "declare the operation in
      a file the owner includes" (assert the guidance text, not just failure).
  22. `scopeLoweredCaptureByCopy` — block captures a child-side RoAssociativeArray,
      mutates it owner-side (inside the block), returns; child asserts its local
      AA is UNCHANGED (documents by-copy semantics on the wire path).
- [ ] **Step 5: Full gates** (goldens +1, E2E + 7, FIR unchanged, stdlib suite,
  includes strict 0 — the child-only fixture file must still validate).
- [ ] **Step 6: Commits** — `brs:` (injection + golden), `stdlib:` + klib
  (dispatch fallback), `test-infra:` (roku-test-app).

### Task 6: Capture/result diagnostics family

**Files:**
- FIR: `FirBrsDiagnosticsList.kt` + regens + messages +
  `FirBrsScopeCaptureChecker.kt` + fixtures
  `compiler/testData/diagnostics/testsWithBrsStdLib/scopeCaptures.kt`
- (Possible tiny golden if severity verification below requires one)

**Interfaces:**
- Consumes: Task 4's literal-block detection (the checker walks the same call
  shape's lambda captures at FIR level: `FirAnonymousFunction` argument of the
  `run(block)` overload).
- Produces the A.3 family, exact severities:
  - `BRS_SCOPE_CAPTURE_UNMARSHALLABLE` (ERROR): captured value whose type is
    function-typed, a component type (the component-`this` hole), or any
    non-external class type (incl. kotlin collections — their methods die
    crossing; data survives but nothing can be safely called). Marshallable:
    primitives, String, Boolean, Dynamic, external interfaces
    (RoSGNode/RoArray/RoAssociativeArray & friends). Message names the capture,
    its type, the marshallable set, and: "pass plain data (an
    RoAssociativeArray or primitives) or move the value's construction inside
    the block".
  - `BRS_SCOPE_CAPTURE_MUTATION_LOST` (WARNING): assignment to a captured `var`
    inside the block. Message: "captures cross by copy; this write never
    reaches the caller — return a value from the block instead".
  - `BRS_SCOPE_RESULT_NOT_DATA`: block/handler result type outside the
    marshallable set. SEVERITY DECISION STEP: first verify how simple `val`
    property access on a data class compiles (write a scratch golden: does
    `point.x` emit a direct member read or a getter CALL?). Direct member read
    → data-class results are usable-as-data → WARNING (message: "results cross
    as data; methods/equals/copy will not survive — share behavioral state via
    the VM"). Getter call → husk results crash on first property read → ERROR.
    Record the verification outcome in the fixture file's header comment and
    the report.
  - `BRS_SCOPE_ARG_NOT_MARSHALLABLE` (ERROR): fires at
    `object X : ScopeRequest1/2<...>` DECLARATION sites whose A1/A2/R type
    arguments are outside the marshallable set (same set as captures; same
    severity-decision procedure for a data-class R as RESULT_NOT_DATA — they
    share the Step 1 verification). Message names the offending type argument,
    the marshallable set, and: "pass plain data or restructure the request".
- [ ] **Step 1:** severity verification golden/scratch (above) — decide, record.
- [ ] **Step 2:** diagnostics + regens + messages + checker + fixtures: each
  diagnostic gets a firing case, a clean case, and a `@Suppress` case; the
  UNMARSHALLABLE fixture includes the component-`this` capture case and a
  kotlin-List capture case; the ARG_NOT_MARSHALLABLE fixture adds a
  declaration-site firing case, a clean case, and a `@Suppress` case (the FIR
  total grows accordingly).
- [ ] **Step 3:** FIR suite green (record new total); `./rebuild.sh` step 7
  green (no stdlib run{} blocks exist — if step 7 fires anyway, a swept site
  snuck in: fix, don't suppress blindly).
- [ ] **Step 4:** Commit (`brs:` prefix, generated containers included).

---

# Phase C — RTQ fast path

### Task 7: RTQ backend + detection + force hooks + dual-backend E2E

**Files:**
- Create: `libraries/stdlib/brs/src/kotlin/brs/scope/ScopeRtqBackend.kt`
- Modify: `ScopeWire.kt` / `ComponentMailbox.kt` (carrier seam), `ScopeApi.kt`
  (test hooks)
- Modify: `../roku-test-app/src/brsTest/kotlin/tests/ScopeHandleTests.kt`

**Interfaces:**
- Consumes: PumpScheduler's backend pattern verbatim
  (`coroutines/pump/PumpScheduler.kt`): feature-detect once
  (`CreateObject("roRenderThreadQueue")` invalid pre-15), cache on the global
  node under a NEW field `__kotlinScopeBackend` (never reuse the pump's field or
  channels), per-instance channel `"kotlin.scope." + uuid`,
  `addMessageHandler(channel, brsName(::onKotlinScopeRtqMessage))` registered
  STRICTLY before the channel id is advertised or used as a reply address.
- Produces: advertisement value `"rtq:<channelId>"`; child reply address on this
  backend = its own channel id string (`replyToChannel` envelope key replaces
  the node ref); handlers parse the identical envelope (carrier-agnostic
  protocol — only transport + reply-address form differ). Public test hooks:
  `kotlinScopeForceFieldBackend(global)` (session-wide),
  `kotlinScopeForceFieldBackendLocal()` (one component),
  `kotlinScopeBackendName(): String`.

- [ ] **Step 1: Implement** behind the carrier seam: post(envelope, target) +
  reply-address abstraction; owner advertises per its resolved backend; a
  field-backend CHILD must still interoperate with an rtq-advertising owner?
  NO — backend is app-global (one cache), so mixed mode only arises via the
  LOCAL force hook; in that case the advertisement tells the child how to
  reach the owner, and the child's reply address declares its own form — the
  envelope's replyTo/replyToChannel duality covers both directions. Owner
  outcome-posting switches on which reply-address key is present.
- [ ] **Step 2: Backend E2E.** Add to Suite 8:
  23. `scopeBackendNameSane` — on this device expect "rtq".
  24–27. Re-run tests 1, 3, 4, 16 under `kotlinScopeForceFieldBackendLocal()`
  (field backend forced on the probes — proves the floor path on a 15.x
  device; the unforced suite already covers rtq end-to-end). Mirror
  PumpBackendProbe's local-force pattern. Field-force twins are deliberately
  the carrier-affecting subset (round-trip, failure, cancel, lowered
  round-trip); carrier-agnostic behaviors (watchdog, fast path, registration
  guards) are not duplicated — rationale mirrors addendum A.6.
  28. `scopeMixedBackendInterop` — child forced to field backend, owner on rtq
  (or vice versa — whichever the force hook makes constructible): round trip
  still green.
- [ ] **Step 3: Full gates** (E2E + 6, stdlib suite for any new units, klib
  regen commit).
- [ ] **Step 4: Commits** — `stdlib:` + klib regen; `test-infra:`.

---

# Phase D — acceptance + docs

### Task 8: Whole-feature sweep, CLAUDE.md, backlog, memory

**Files:**
- Modify: `CLAUDE.md` (new "ScopeHandle" section after "Typed Tasks"), memory
  file `project_scopehandle_program.md` + MEMORY.md index line
- Modify (append): `docs/superpowers/backlog-typed-task-program.md` or the
  program's own backlog notes in the spec — record: SetRef payload stash
  (decision A5), MoveIntoField typed-task output optimization (M3), the VM
  program's item list (A.4), alwaysNotify×scoped-observer unprobed cell,
  Move* "moved half" unpinned cell,
  TaskRunner awaitCompletion fast-paths run before entry ensureActive
  (already-cancelled caller + already-done task returns value instead of CE)
  — fix on next TaskRunner touch with the Await.kt all-inside-block shape,
  channel-matrix vs kotlin-probe deploy.sh run-stamping asymmetry (fixed in
  this wave for consistency)

- [ ] **Step 1: Full sweep** — every gate, both repos:
  `./run-compiler-tests.sh` (goldens: 65+2), FIR suite (220 + Tasks 4/6
  fixtures), `./run-stdlib-tests.sh` (493+units/50+1), `cd ../roku-test-app &&
  ./run-device-tests.sh` (Suite 8 complete: ~28 new tests; 8 suites),
  `validateComponentIncludes` strict 0. Record the final table.
- [ ] **Step 2: CLAUDE.md section** — API summary (both surfaces, code
  examples), the teardown LAW (`host.close()` before retiring an owner node),
  watchdog line format + once-only semantics, the documented patterns (big
  results in shared VM state; ContentNode trees built task-side; captures/args
  cross by copy — return values, don't mutate captures), the literal-lambda
  rule, marshallable-set table, dispatch-miss guidance, test-hook inventory,
  gate table update.
- [ ] **Step 3: Backlog + memory updates** per the file list above.
- [ ] **Step 4: Commits** — `docs:` (CLAUDE.md + backlog), memory is outside
  the repo (no commit).

---

## Plan self-review notes (kept for the executor)

**Numbering truth (record ACTUALS in the ledger as you go):** goldens 65 → 67
(Tasks 4, 5). FIR 220 → +BLOCK_NOT_LITERAL fixture (T4) + captures fixtures
(T6). Stdlib device 493/50 → +Task 1 units (~6). E2E 42/7 → ~70/8 by Task 8
(11 + 5 + 7 + 6 (Task 2 item 9 defines two tests) new in Tasks 2/3/5/7 — counts are estimates; the recorded
actual is the gate).

**Known unknowns each executor must verify in place:**
- Existing lambda lowering internals: generated-function naming, file
  attachment, dep-tracking registration (Task 4 Step 3) — read before writing;
  the lifted block must flow through include tracking like any generated fn.
- The exposeScope-detection scan home + `__kotlinPumpAttach` injection point
  (Task 5) — grep, mirror.
- m-scope read idiom for `m.__kotlinScopeBindings` from stdlib (Task 5 Step 3)
  — PumpScheduler precedent.
- Data-class property-access codegen (Task 6 Step 1) — decides
  RESULT_NOT_DATA's severity; both outcomes specified.
- `ScopeRequest` abstract-class-with-ctor-arg compilation (Task 1): objects
  extending parameterized abstract classes must initialize `name` correctly —
  plain-class init-block/ctor codegen is in good shape post-utilities, but
  eyeball the generated BRS for one request object in Task 2's first device
  build; if `name` arrives empty, STOP (known init-lowering defect families
  live in `project_brs_suspend_codegen_defects.md`).
- Driver-side `withTimeout` in tests runs in the component-pumping regime —
  fine (Suite 7 precedent), but test 14's cancel-propagation assertion is
  child-observable only (owner-side job state is not directly assertable from
  the driver; assert via owner lastEvent field writes from the handler's
  finally-free completion path — handlers should write lastEvent at start and
  the egress result proves unwind).
- Test 28 (mixed backend) depends on the force hook granularity actually
  making the mix constructible; if not constructible, document why in the
  test file and drop to 27 tests — do not fake it.

**What is deliberately NOT here:** SetRef anywhere (A5 backlog); VM-program
items (A.4 — shared-VM (ii) implementation, demotion FIR rules, fn-slot canary,
static-dispatch verification); spawnTask; StateFlow; @AppScoped. Each is
recorded in Task 8's backlog step with its decision pointer.
