# ScopeHandle Implementation Plan — Stage 1 (Riders + Spike + Checkpoint)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Land the two riders (TaskRunner cancellation retrofit, try/finally FIR
stopgap), run the device spike that gates the ScopeHandle API freeze, and record the
branch decision — producing the inputs for the Stage 2 (Phase 2–4) implementation plan.

**Architecture:** Per the approved spec
`docs/superpowers/plans/2026-08-12-scopehandle-design.md`. This stage deliberately
stops at the spike checkpoint: the spec's decision tree (§5) makes Phase 2+ code
depend on device facts (heap sharing, foreign-lambda ambient identity) that do not
exist yet. Task 6 writes the Stage 2 plan from the recorded findings.

**Tech Stack:** Kotlin/BRS stdlib (`libraries/stdlib/brs`), FIR checkers
(`checkers.brs`), raw BrightScript + kotlin-roku spike apps, roku-test-app E2E.

## Global Constraints

- **Build:** `./rebuild.sh` is the ONLY build command. Exceptions, both documented in
  CLAUDE.md: the two `generateCheckersComponents` regen commands (Task 2) and the
  test-run commands listed in CLAUDE.md "Running Tests".
- **Device:** `export ROKU_DEVICE_IP=192.168.1.125; export ROKU_PASSWORD=<ask Mike if unset>`.
  Console preflight before EVERY device run: `echo | nc -w 3 192.168.1.125 8085` —
  if it prints `Console connection is already in use`, STOP and ask Mike to
  disconnect the IDE's Roku console. Do NOT kill the IDE.
- **Gate baselines (must not drop):** goldens 65 · FIR (checkers.brs) 219 · stdlib
  device 493 tests / 50 suites · E2E 41 active / 7 suites (+3 xtests) ·
  `validateComponentIncludes` strict 0.
- **Commits:** per-task, on `feature/brightscript-backend-2.2.20`. Prefixes: `stdlib:`,
  `brs:`, `test-infra:`, `docs:`, `spike:`. EVERY stdlib source change is followed by a
  separate `brs-prebuilt: regenerate stdlib klib (<reason>)` commit (rebuild.sh
  regenerates it; commit the changed klib). All messages end with
  `Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>`.
- **Two repos:** roku-test-app is a SEPARATE git repo (`../roku-test-app`) — commit it
  separately with the same message conventions.
- **Stdlib suspend idiom:** any stdlib suspend edit follows tail-delegation +
  `parked.finish()` as the intrinsic block's LAST expression (CLAUDE.md "stdlib
  suspend-function idiom").
- **Golden diffs:** none expected in this stage. If any golden changes, STOP and
  review the diff line-by-line before committing — unexpected golden churn means an
  unintended codegen change.

## File Map (who owns what)

| File | Task | Action |
|---|---|---|
| `libraries/stdlib/brs/src/kotlin/coroutines/task/TaskRunner.kt` | 1 | Modify (retrofit) |
| `../roku-test-app/components/fixtures/TypedTaskProbe.kt` | 1 | Modify (new mode) |
| `../roku-test-app/components/fixtures/SleepTask.kt` | 1 | Create (only if no slow-task fixture exists) |
| `../roku-test-app/src/brsTest/kotlin/tests/TypedTaskTests.kt` | 1 | Modify (+1 test) |
| `compiler/fir/checkers/checkers-component-generator/src/.../diagnostics/FirBrsDiagnosticsList.kt` | 2 | Modify |
| `compiler/fir/checkers/checkers.brs/gen/.../FirBrsErrors.kt` | 2 | Regenerated (commit) |
| `compiler/fir/checkers/gen/.../FirNonSuppressibleErrorNames.kt` | 2 | Regenerated (commit) |
| `compiler/fir/checkers/checkers.brs/src/.../FirBrsErrorsDefaultMessages.kt` | 2 | Modify |
| `compiler/fir/checkers/checkers.brs/src/.../FirBrsTryFinallyChecker.kt` | 2 | Create |
| checkers.brs expression-checkers registrar (locate; see Task 2 Step 3) | 2 | Modify |
| `compiler/testData/diagnostics/testsWithBrsStdLib/tryFinallyNonSuspend.kt` (+ generated test registration) | 2 | Create |
| `spikes/scope-handle-spike/channel-matrix/` (raw BRS app) | 3 | Create |
| `spikes/scope-handle-spike/kotlin-probe/` (kotlin-roku app) | 4 | Create |
| `spikes/scope-handle-spike/FINDINGS.md` | 3–5 | Create/extend |
| `CLAUDE.md` (platform-facts additions) | 5 | Modify |
| `docs/superpowers/plans/2026-08-1X-scopehandle-stage2.md` | 6 | Create |

---

# Phase 0a

### Task 1: TaskRunner `awaitCompletion` retrofit + E2E cancellation test

**Files:**
- Modify: `libraries/stdlib/brs/src/kotlin/coroutines/task/TaskRunner.kt` (registry ~84–101, awaitCompletion ~206–241, observer ~113–150, resumeTask ~158–169)
- Modify: `../roku-test-app/components/fixtures/TypedTaskProbe.kt` (mode dispatch)
- Create (conditional): `../roku-test-app/components/fixtures/SleepTask.kt`
- Test: `../roku-test-app/src/brsTest/kotlin/tests/TypedTaskTests.kt` (Suite 4)

**Interfaces:**
- Consumes: `ParkedContinuation` (internal, `coroutines/ParkedContinuation.kt:26` —
  ctor takes the raw continuation; `handles: MutableList<DisposableHandle>`;
  `tryResume(value)`, `tryResumeException(e)`, `finish()`),
  `registerCallerCancel(parked, context)` (same file :117),
  `jobImplOf(job)` (`coroutines/Job.kt:78`),
  `JobImpl.invokeOnCancelRequest(handler): DisposableHandle` (Job.kt:174 —
  internal; VERIFY the handler's parameter list in place before writing the call).
- Produces: cancellation-prompt `runTask` awaits. No signature changes — later tasks
  rely only on the behavior (owner-side request jobs unwind at task parks).

- [ ] **Step 1: Check for an existing slow-task fixture**

Read `../roku-test-app/src/brsTest/kotlin/tests/TypedTaskTests.kt` (the overlap test)
and `ls ../roku-test-app/components/fixtures/`. If a task fixture with a
configurable duration already exists, use it in Steps 2–3 and skip creating
SleepTask. Otherwise create `../roku-test-app/components/fixtures/SleepTask.kt`:

```kotlin
package com.nuvyyo.roku.fixtures

import kotlin.brs.*

class SleepTask : TaskComponent() {
    @SGIntegerField
    var durationMs: Int = 0

    override fun run() {
        brs("sleep(m.top.durationMs)")
    }
}
```

(Mirror the package/imports of `EchoTask.kt` exactly. If the `brs()` splice of the
bare `sleep` call misbehaves — known open statement-splice bugs are documented for
`BrsIf`/`BrsVariable` splices, a bare call is expected fine — fall back to a
`RoTimespan` busy-wait only if one of the fixtures already demonstrates the pattern;
otherwise flag for review.)

- [ ] **Step 2: Add the `cancelawait` mode to TypedTaskProbe**

Read `../roku-test-app/components/fixtures/TypedTaskProbe.kt` and add a branch to its
mode dispatch, mirroring the existing branches' style:

```kotlin
"cancelawait" -> {
    val job = launch {
        try {
            runTask<SleepTask> { durationMs = 5000 }
            outcome = "completed-unexpectedly"
        } catch (e: CancellationException) {
            outcome = "cancelled"
        }
    }
    launch {
        delay(250)
        job.cancel()
    }
}
```

- [ ] **Step 3: Write the failing E2E test (Suite 4)**

Append to `TypedTaskTests.kt`, mirroring the suite's existing probe setup lines
(typed adopt path — `createComponent<TypedTaskProbe>()` + `Probes.adopt`, see the
existing tests at the top of the file for the exact incantation):

```kotlin
testAsync("runTask await wakes promptly on caller cancellation") {
    val probe = createComponent<TypedTaskProbe>()
    Probes.adopt(scene, nodeOf(probe))
    probe.mode = "cancelawait"
    roundTrip(nodeOf(probe), "start", true, "outcome")
    assertEquals("cancelled", probe.outcome)
}
```

- [ ] **Step 4: Verify red on device**

```bash
cd ../roku-test-app && ./rebuild-all.sh --all && ./run-device-tests.sh
```

Expected: the new test FAILS — either the 10s whole-test timeout (park never woke) or
`outcome == "completed-unexpectedly"` arriving at ~5s (park resumed only when the
task finished). Every other test stays green (41/41). If the new test passes here,
STOP — the premise is wrong; re-read TaskRunner.kt before touching it.

- [ ] **Step 5: Retrofit TaskRunner.kt**

Registry (currently `mutableMapOf<Int, Continuation<Any?>>`):

```kotlin
private val pending = mutableMapOf<Int, ParkedContinuation>()

internal fun register(taskId: Int, parked: ParkedContinuation) { pending[taskId] = parked }
internal fun remove(taskId: Int): ParkedContinuation? = pending.remove(taskId)
```

`awaitCompletion`: add an entry check `coroutineContext.ensureActive()` as the FIRST
line (before the double-await guard; keep the guard exactly where it is — its
placement before the arm is deliberate and KDoc'd). Keep the arm and the
already-done/already-error fast paths unchanged. Replace the raw suspension tail
with the canonical park idiom:

```kotlin
return suspendCoroutineUninterceptedOrReturn { continuation ->
    val parked = ParkedContinuation(continuation)
    TaskRunner.register(taskId, parked)
    // Cancel-path cleanup the park cannot know about: drop the registry entry and
    // disarm the observer so a late terminal write finds nothing to do.
    val jobImpl = jobImplOf(continuation.context[Job])
    if (jobImpl != null) {
        parked.handles.add(jobImpl.invokeOnCancelRequest {
            TaskRunner.remove(taskId)
            node.unobserveFieldScoped(TASK_STATE_FIELD)
        })
    }
    registerCallerCancel(parked, continuation.context)
    parked.finish()
}
```

(VERIFY `invokeOnCancelRequest`'s handler parameter list at Job.kt:174 and adjust the
lambda header; the cleanup handler is registered BEFORE `registerCallerCancel` so
protocol state is gone by the time the park wakes with CE. Both handles land in
`parked.handles`, so a normal settle disposes them.)

`onKotlinTaskStateChanged`: `TaskRunner.remove(taskId)` now yields a
`ParkedContinuation?` — null keeps the existing stale/duplicate drop path. Settle
directly and DELETE `resumeTask` (ParkedContinuation.resumeTarget already does the
interceptor routing that resumeTask hand-rolled):

```kotlin
val parked = TaskRunner.remove(taskId) ?: return   // keep the existing comment/log shape
if (!TaskRunner.hasPending()) {
    node.unobserveFieldScoped(TASK_STATE_FIELD)    // keep the existing guard shape
}
if (state == TASK_STATE_DONE) {
    parked.tryResume(node)
} else {
    parked.tryResumeException(taskExceptionFrom(node))
}
```

Update the KDoc blocks that promise "no cancellation" (~34–39, ~278–285): awaiting
side now wakes mid-park on caller cancellation; task-thread `run()` still executes
to completion (M3 unchanged).

- [ ] **Step 6: Rebuild + verify green on device**

```bash
./rebuild.sh
cd ../roku-test-app && ./rebuild-all.sh --all && ./run-device-tests.sh
```

Expected: 42 active / 7 suites, all green — including the new test with
`outcome == "cancelled"` well before the 5s sleep elapses.

- [ ] **Step 7: Stdlib device suite + remaining gates**

```bash
./run-stdlib-tests.sh          # expect 493/50, no drops
./run-compiler-tests.sh        # expect 65 goldens, no diffs
```

- [ ] **Step 8: Update CLAUDE.md's runTask constraint text**

In the "Typed Tasks (`runTask`)" and "Cancellation promises" sections: replace the
"a coroutine awaiting a task does not wake mid-park on cancellation" claim with the
new behavior (wakes promptly with CE; task-thread work still runs to completion).

- [ ] **Step 9: Commit (three commits)**

```bash
# Kotlin repo
git add libraries/stdlib/brs/src/kotlin/coroutines/task/TaskRunner.kt CLAUDE.md
git commit -m "stdlib: retrofit runTask awaitCompletion onto ParkedContinuation (cancellation-prompt awaits)

Entry ensureActive + registerCallerCancel + cancel-path cleanup
(registry remove + observer disarm); resumeTask's hand-rolled
interceptor routing deleted (resumeTarget owns it). Task-thread run()
still executes to completion (M3 unchanged).

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"

git add libraries/stdlib/brs-prebuilt
git commit -m "brs-prebuilt: regenerate stdlib klib (TaskRunner awaitCompletion retrofit)

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"

# roku-test-app repo
cd ../roku-test-app
git add components/fixtures/ src/brsTest/kotlin/tests/TypedTaskTests.kt
git commit -m "test-infra: Suite 4 runTask cancellation test (cancelawait probe mode)

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

---

# Phase 0b

### Task 2: `BRS_TRY_FINALLY_UNSUPPORTED` FIR stopgap

**Files:**
- Modify: `compiler/fir/checkers/checkers-component-generator/src/.../diagnostics/FirBrsDiagnosticsList.kt`
- Regenerate + commit: `checkers.brs/gen/.../FirBrsErrors.kt`, `checkers/gen/.../FirNonSuppressibleErrorNames.kt`
- Modify: `compiler/fir/checkers/checkers.brs/src/.../FirBrsErrorsDefaultMessages.kt`
- Create: `compiler/fir/checkers/checkers.brs/src/.../FirBrsTryFinallyChecker.kt` + registrar entry
- Test: `compiler/testData/diagnostics/testsWithBrsStdLib/tryFinallyNonSuspend.kt`

**Interfaces:**
- Consumes: the established BRS diagnostic rollout pattern — mirror the
  `BRS_IO_DISPATCHER_UNSUPPORTED` checker (locate it under
  `compiler/fir/checkers/checkers.brs/src/`) for base-class choice, registrar wiring,
  and message registration.
- Produces: FIR error `BRS_TRY_FINALLY_UNSUPPORTED` on any `try` with a `finally`
  whose nearest containing callable is not suspend. Suppressible.

- [ ] **Step 1: Write the failing fixture first**

`compiler/testData/diagnostics/testsWithBrsStdLib/tryFinallyNonSuspend.kt` (marker
positions are a first guess — Step 5 corrects them from actual output; the SEMANTIC
cases are the contract):

```kotlin
// Case 1: non-suspend function — ERROR
fun nonSuspendFinally(): Int {
    <!BRS_TRY_FINALLY_UNSUPPORTED!>try {
        return 1
    } finally {
        println("cleanup")
    }<!>
}

// Case 2: suspend function — CLEAN (finally works inside suspend state machines)
suspend fun suspendFinallyOk(): Int {
    try {
        return 1
    } finally {
        println("cleanup")
    }
}

// Case 3: non-suspend lambda inside a suspend function — ERROR (the lambda compiles
// as its own non-suspend BRS function; nearest callable wins)
suspend fun lambdaTrap() {
    val f = {
        <!BRS_TRY_FINALLY_UNSUPPORTED!>try {
            println("a")
        } finally {
            println("b")
        }<!>
    }
    f()
}

// Case 4: try/catch WITHOUT finally, non-suspend — CLEAN
fun tryCatchOk() {
    try {
        println("a")
    } catch (e: Exception) {
        println("b")
    }
}

// Case 5: suppression escape — CLEAN
@Suppress("BRS_TRY_FINALLY_UNSUPPORTED")
fun deliberate() {
    try {
        println("a")
    } finally {
        println("b")
    }
}
```

Register the fixture the same way the neighboring testsWithBrsStdLib fixtures are
registered (check for a generated test-registration step in that suite's README or
sibling test classes; some suites regenerate via a Gradle task — mirror whatever the
last-added fixture did).

- [ ] **Step 2: Add the diagnostic to the list + regenerate**

In `FirBrsDiagnosticsList.kt`, mirroring the existing entries' style:

```kotlin
val BRS_TRY_FINALLY_UNSUPPORTED by error<KtTryExpression>()
```

(Use whatever PSI/source-element type parameter the sibling entries use if
`KtTryExpression` isn't the local convention.) Then the two documented regen
commands — commit BOTH generated files:

```bash
./gradlew :compiler:fir:checkers:checkers.brs:generateCheckersComponents --no-configuration-cache
./gradlew :compiler:fir:checkers:generateCheckersComponents --no-configuration-cache
```

- [ ] **Step 3: Write the checker + register it**

`FirBrsTryFinallyChecker.kt` — base class and registration copied from the
`BRS_IO_DISPATCHER_UNSUPPORTED` checker's file (same package, same registrar object):

```kotlin
object FirBrsTryFinallyChecker : FirTryExpressionChecker(MppCheckerKind.Common) {
    override fun check(expression: FirTryExpression, context: CheckerContext, reporter: DiagnosticReporter) {
        if (expression.finallyBlock == null) return
        val nearestCallable = context.containingDeclarations.lastOrNull {
            it is FirSimpleFunction || it is FirAnonymousFunction || it is FirConstructor || it is FirPropertyAccessor
        }
        val isSuspend = when (nearestCallable) {
            is FirSimpleFunction -> nearestCallable.isSuspend
            is FirAnonymousFunction -> nearestCallable.isSuspendLambda(context.session)
            else -> false
        }
        if (!isSuspend) {
            reporter.reportOn(expression.source, FirBrsErrors.BRS_TRY_FINALLY_UNSUPPORTED, context)
        }
    }
}
```

(VERIFY in place: `FirTryExpressionChecker` existence — if there is no dedicated
try-expression checker kind, use the general `FirBasicExpressionChecker` and gate on
`expression is FirTryExpression`, which is exactly the kind of substitution the
sibling checkers demonstrate. Same for the suspend-lambda test — find the existing
helper the codebase uses to classify a `FirAnonymousFunction` as suspend rather than
inventing one.)

Default message in `FirBrsErrorsDefaultMessages.kt`:

```
try/finally is not supported outside suspend functions on the BrightScript backend: the 'finally' block is silently dropped. Move this code into a suspend function, restructure without 'finally', or @Suppress("BRS_TRY_FINALLY_UNSUPPORTED") with a tracking comment.
```

- [ ] **Step 4: Sweep existing sources BEFORE the full rebuild**

```bash
grep -rn --include='*.kt' -B3 'finally' \
  libraries/stdlib/brs/src libraries/stdlib/brs-actual libraries/kotlin.test/brs/src \
  ../roku-test-app/components ../roku-test-app/src
```

For every non-suspend try/finally hit: its finally block is ALREADY silently dropped
today — either fix it (restructure) or `@Suppress("BRS_TRY_FINALLY_UNSUPPORTED")`
with a `// TRACKING:` comment, mirroring the Job.Key suppression sites. List every
touched site in the commit body.

- [ ] **Step 5: Run the FIR suite; correct fixture markers**

```bash
./gradlew :compiler:fir:checkers:checkers.brs:test --no-configuration-cache -Dorg.gradle.dependency.verification=off
```

First run: adjust the fixture's `<!...!>` ranges to the checker's actual reported
positions IF the semantics match (cases 1 and 3 error; 2, 4, 5 clean). If the
SEMANTICS are wrong (case 2 errors, case 3 doesn't), fix the checker, not the
fixture. Record the new FIR total (expect 219 + the new fixture's tests).

- [ ] **Step 6: Full rebuild + all gates**

```bash
./rebuild.sh                   # step 7 (kotlin-test-brs compile) is the canary for missed sweep sites
./run-compiler-tests.sh        # 65 goldens, no diffs
./run-stdlib-tests.sh          # 493/50
cd ../roku-test-app && ./run-device-tests.sh   # 42/7 after Task 1
```

- [ ] **Step 7: Commit**

```bash
git add compiler/fir/checkers/ compiler/testData/diagnostics/testsWithBrsStdLib/ \
        libraries/stdlib libraries/kotlin.test
git commit -m "brs: BRS_TRY_FINALLY_UNSUPPORTED FIR stopgap (finally silently dropped outside suspend)

Non-suspend visitTry drops finallyExpression; until codegen emits it,
error at the source. Suppressible; existing sites swept (<list>).
Both generated containers committed.

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

(If the sweep touched stdlib sources, follow with the separate
`brs-prebuilt: regenerate stdlib klib (try/finally sweep)` commit; if it touched
roku-test-app, commit that repo separately as `test-infra: try/finally sweep`.)

---

# Phase 1 — the spike

### Task 3: Channel-matrix spike app (raw BrightScript) — Q1a–c, Q4

**Files:**
- Create: `spikes/scope-handle-spike/channel-matrix/` (manifest, `source/main.brs`,
  `components/MatrixScene.{xml,brs}`, `components/ProbeA.{xml,brs}`,
  `components/ProbeB.{xml,brs}`, `deploy.sh`)
- Create: `spikes/scope-handle-spike/FINDINGS.md` (Q1a–c + Q4 sections)

**Interfaces:**
- Consumes: the deploy/capture harness shape of
  `spikes/render-thread-queue-spike/` — read that directory FIRST and mirror its
  manifest, deploy script, and console-capture approach exactly.
- Produces: per-channel verdicts in FINDINGS.md, consumed by Task 4 (channel choice
  for the Kotlin probes) and Task 5 (decision tree).

- [ ] **Step 1: Mirror the RTQ spike harness**

`ls spikes/render-thread-queue-spike/` and copy its manifest + deploy/capture
scripts into `channel-matrix/`, renaming the app title to `scope-spike-matrix`.

- [ ] **Step 2: Write the probes**

Design: `MatrixScene` creates ProbeA (the "owner" analog) and ProbeB (the "child"),
hands B a node reference to A via a declared node field, then sets `B.runTests =
true`. B drives the matrix as a step machine (one-shot Timer between async steps),
printing one line per verdict:

```
[SPIKE] <channel>.<probe> PASS|FAIL <detail>
```

Channels × probes (every channel runs all four payload probes):

| channel id | mechanism |
|---|---|
| `nodeField` | B does `aNode.setField("mailbox", payload)`; A observes its own `mailbox` (observer armed in A's init, before B ever posts) |
| `globalField` | B does `m.global.addField("spikeBox","assocarray",false)` + `setField`; A reads `m.global.spikeBox` on a doorbell |
| `callFuncArg` | A's XML declares `<function name="spikeCall"/>`; B calls `aNode.callFunc("spikeCall", payload)` |
| `callFuncRet` | `spikeCall` returns an AA A keeps a reference to; A mutates it after returning (on a timer); B re-checks its copy |
| `rtq` | A registers `roRenderThreadQueue` channel `"spike.a"` in init; B posts payload; RENDER-thread-to-render-thread |
| `eventData` | B compares `event.getData()` vs `aNode.getField(...)` inside A's observer (sub-probe of `nodeField`) |

Payload construction (in B) and the four probes:

```brightscript
function buildPayload() as object
    return {
        marker: 1,                        ' (a) reference identity: A sets marker=2; B re-reads its local copy
        theNode: m.top,                   ' (b) node ref: A checks isSameNode against B's node
        theFn: probeFunction,             ' (c) function ref: A checks Type() and invokes if callable
        kotlinObj: invalid                ' (d) exercised only in the kotlin-probe app (Task 4)
    }
end function

function probeFunction() as string
    return "fn-alive"
end function
```

A's receiver logic (same body for every channel's arrival point):

```brightscript
sub inspectPayload(payload as object, channel as string)
    ' (a) mutate for the reference-identity check
    payload.marker = 2
    m.lastReceived = payload
    ' (b) node identity
    nodeOk = (payload.theNode <> invalid) and payload.theNode.isSameNode(m.top.getParent().findNode("probeB"))
    print "[SPIKE] " + channel + ".nodeRef " + pf(nodeOk)
    ' (c) function ref
    fnType = Type(payload.theFn)
    fnOk = (fnType = "roFunction") or (fnType = "Function")
    detail = " type=" + fnType
    if fnOk then detail = detail + " invoke=" + payload.theFn()
    print "[SPIKE] " + channel + ".fnRef " + pf(fnOk) + detail
end sub

function pf(ok as boolean) as string
    if ok then return "PASS"
    return "FAIL"
end function
```

B's post-hop check (after A signals it has inspected — doorbell boolean field on B,
observed by B):

```brightscript
sub checkReferenceIdentity(channel as string)
    ok = (m.sentPayload.marker = 2)      ' A's mutation visible in B's copy?
    print "[SPIKE] " + channel + ".refIdentity " + pf(ok)
end sub
```

Q4 probes (run first, on `nodeField`, since the whole channel depends on them):

```brightscript
' Q4.1: runtime addField + observeField fires at all
' A init:  m.top.addField("mailbox", "assocarray", true)   ' alwaysNotify=true
'          m.top.observeField("mailbox", "onMailbox")
' B:       aNode.setField("mailbox", buildPayload())
' PASS = onMailbox fired.
' Q4.2: alwaysNotify on runtime-added fields — B posts the IDENTICAL payload twice;
' PASS = onMailbox fired twice.
```

Implementation notes for the executor: every observer is armed in the OWNING
component's init before MatrixScene wires anything (arming-order law). `callFuncRet`
needs A to hold the returned AA and mutate it ~100ms later on a one-shot Timer, then
ring B's doorbell. For `rtq`, guard with the OS check the RTQ spike used (CreateObject
returns invalid pre-15) and print `[SPIKE] rtq.* SKIP os<15` if unavailable.

- [ ] **Step 3: Deploy, capture, transcribe**

Console preflight (Global Constraints), then deploy with the mirrored script and
capture ~30s of console:

```bash
cd spikes/scope-handle-spike/channel-matrix && ./deploy.sh
# capture per the RTQ spike's approach; save to ../capture-channel-matrix.txt
grep '\[SPIKE\]' ../capture-channel-matrix.txt
```

Every channel must print a verdict for probes a–c (+ Q4.1/Q4.2, + eventData
sub-probe). A missing line is itself a finding (e.g. observer never fired) — record
it as FAIL with the observed behavior, never leave a cell blank.

- [ ] **Step 4: Record in FINDINGS.md**

Create `spikes/scope-handle-spike/FINDINGS.md` with the header block the RTQ spike
uses (device model, OS version, date), a table: channel × {refIdentity, nodeRef,
fnRef, notes}, and the Q4 verdicts. Raw capture file committed alongside.

- [ ] **Step 5: Commit**

```bash
git add spikes/scope-handle-spike/
git commit -m "spike: scope-handle channel matrix (Q1a-c, Q4) — raw BRS app + findings

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

### Task 4: Kotlin probe app — Q1d, Q2, Q3

**Files:**
- Create: `spikes/scope-handle-spike/kotlin-probe/` (kotlin-roku project: settings +
  build.gradle.kts mirroring `../roku-test-app`'s, manifest, `SpikeMain.kt`,
  `components/SpikeScene.kt`, `components/SpikeOwner.kt`, `components/SpikeChild.kt`,
  `components/SpikeEchoTask.kt`, `shared/SharedVm.kt`, `shared/AmbientProbe.kt`)
- Extend: `spikes/scope-handle-spike/FINDINGS.md` (Q1d, Q2, Q3 sections)

**Interfaces:**
- Consumes: Task 3's per-channel verdicts — run Q1d over every channel Task 3 marked
  reference-preserving, PLUS `nodeField` regardless (to characterize what a cloned
  Kotlin object looks like). Q2/Q3 use the best reference-preserving channel; if none
  exists, Q2/Q3 are moot — record `BLOCKED-BY-Q1` and stop after Q1d.
- Produces: the branch-A/branch-B facts for Task 5's decision tree.

- [ ] **Step 1: Scaffold the app**

Copy `../roku-test-app`'s `settings.gradle.kts`/`build.gradle.kts`/manifest shape into
`kotlin-probe/`, strip the test wiring (no brsTest source set, no rokuTest), keep the
kotlin-roku plugin + Maven Local repos. Components: `SpikeScene` (a `SceneComponent`
that `@SGLayout`-declares `SpikeOwner` and `SpikeChild` as children), plus the files
below. Deploy = the plugin's sideload path used by rebuild-all.sh (read that script
for the task name) or the same curl sideload the raw-BRS spike's deploy.sh uses.

- [ ] **Step 2: The shared classes and probes**

```kotlin
// shared/SharedVm.kt — plain class: THE Q1d payload
class SharedVm {
    var counter: Int = 0
    fun bump(): Int { counter += 1; return counter }
}

// shared/AmbientProbe.kt — object singleton: per-component by platform law;
// whichever component's copy changes tells us who was ambient (Q2)
object AmbientProbe {
    var touchedBy: String = "nobody"
}
```

`SpikeChild`: builds the payloads and reports. `SpikeOwner`: receives (over the
channels chosen per Task 3), inspects, prints. All prints use the `[SPIKE]` prefix +
`pf`-style PASS/FAIL, same as Task 3.

Q1d probe (per channel): child creates `SharedVm()`, calls `bump()` once (counter=1),
ships it; owner calls `bump()` (expect 2 if methods dispatch at all), prints
`q1d.<channel>.method PASS/FAIL counter=<n>`; child then reads ITS reference's
`counter` — 2 = shared identity, 1 = clone — prints `q1d.<channel>.identity`.

Q2 probe: child sets `AmbientProbe.touchedBy = "child"` (its own copy), creates
`val block: () -> String = { val was = AmbientProbe.touchedBy; AmbientProbe.touchedBy = "lambda"; was }`,
ships it; owner invokes; owner prints `q2.sawBefore=<was>` (`"nobody"` ⇒ the lambda
read the OWNER's copy ⇒ ambient = invoker; `"child"` ⇒ ambient stuck to creator);
then both components print their own `AmbientProbe.touchedBy` for confirmation.
Also ship a lambda capturing a child local (`val n = 41; { n + 1 }`) — owner invokes,
prints `q2.captured PASS/FAIL got=<v>`.

Q3 probe: child ships `val sblock: suspend () -> String = { delay(50); runTask<SpikeEchoTask> { input = "q3" }.output ?: "null" }`;
owner runs `launch { println("[SPIKE] q3.result " + sblock()) }` on ITS scope.
PASS = prints `q3.result q3-echoed` (SpikeEchoTask echoes `input + "-echoed"` from
its task-thread `run()`); the delay + task park being serviced at all proves the
owner's pump owns the machinery. Instrument which component's pump woke by printing
`kotlinPumpBackendName()` context if ambiguity remains — the load-bearing check is
that the block completes while the CHILD does nothing after shipping.

```kotlin
// components/SpikeEchoTask.kt
class SpikeEchoTask : TaskComponent() {
    @SGStringField var input: String = ""
    @SGStringField var output: String? = null
    override fun run() { output = input + "-echoed" }
}
```

- [ ] **Step 3: Deploy, capture, transcribe**

Console preflight; deploy; capture ~40s; `grep '\[SPIKE\]'`; save capture to
`spikes/scope-handle-spike/capture-kotlin-probe.txt`.

- [ ] **Step 4: Record Q1d/Q2/Q3 in FINDINGS.md; commit**

```bash
git add spikes/scope-handle-spike/
git commit -m "spike: scope-handle kotlin probe (Q1d shared objects, Q2 ambient lambda, Q3 suspend-across-hop)

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

### Task 5: Findings consolidation, CLAUDE.md platform facts, CHECKPOINT

**Files:**
- Modify: `spikes/scope-handle-spike/FINDINGS.md` (decision section)
- Modify: `CLAUDE.md` (platform-facts additions)

**Interfaces:**
- Consumes: all `[SPIKE]` verdicts from Tasks 3–4.
- Produces: the recorded API-branch decision that Stage 2 planning consumes. THIS
  TASK ENDS IN A HARD STOP FOR MIKE.

- [ ] **Step 1: Walk the spec's decision tree (spec §5) against the verdicts**

Write a `## Decision` section in FINDINGS.md answering, with evidence lines quoted:
(1) does any channel preserve Kotlin-object reference identity — and if ONLY rtq,
say so explicitly (floor consequence); (2) branch A viable (Q2 ambient=invoker AND a
block-delivery path exists) or branch B; (3) Q4 verdict → field-mailbox mechanism
as-designed or declared-field fallback; (4) any escalation tripped
(no-sharing → MVVM premise pivot).

- [ ] **Step 2: Add the new device-verified facts to CLAUDE.md**

Extend the platform-facts material (the "Load-bearing platform facts" knowledge lives
in the coroutine scaffolding section and the handoff — add a short "Cross-component
channel semantics (scope-handle spike, <date>)" block near the Component Coroutine
Scaffolding section) with ONLY the facts, one line each, probe-referenced.

- [ ] **Step 3: Commit**

```bash
git add spikes/scope-handle-spike/FINDINGS.md CLAUDE.md
git commit -m "docs: scope-handle spike findings — channel semantics + API branch decision

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

- [ ] **Step 4: CHECKPOINT — present to Mike and STOP**

Present: the verdict table, the decision-tree walk, and the recommended branch. If
an escalation tripped (no reference-preserving channel, or RTQ-only), present that
decision instead. DO NOT start Task 6 until Mike's decision is recorded as an
addendum in FINDINGS.md (one line: `Decision (Mike, <date>): branch <A|B>, <notes>`).

### Task 6: Write the Stage 2 implementation plan

**Files:**
- Create: `docs/superpowers/plans/2026-08-1X-scopehandle-stage2.md` (use the actual date)

**Interfaces:**
- Consumes: the spec (§6–§9) + FINDINGS.md's recorded decision.
- Produces: the executable plan for Phases 2–4 (core primitive on field-mailbox, RTQ
  backend, acceptance + docs), in this same format, with the branch-specific API
  committed and full inline implementation code.

- [ ] **Step 1: Invoke the writing-plans skill** with the spec + FINDINGS as inputs.
  The Stage 2 plan must cover: the component-mailbox internal layer, the chosen
  branch's public API (spec §6.1/§6.4), the twelve Suite 8 tests (spec §6.6), the
  RTQ backend + force hooks (spec §7), Phase 4 acceptance/docs (spec §8), and the
  gate table updated with Stage 1's actual post-task numbers.

- [ ] **Step 2: Commit** (`docs: ScopeHandle stage 2 implementation plan`), then offer
  Mike the execution-approach choice for Stage 2.

---

## Plan self-review notes (kept for the executor)

**Numbering truth after this stage:** goldens 65 (unchanged) · FIR 219 + Task 2's
fixture tests (record actual) · stdlib device 493/50 (unchanged) · E2E **42**/7
active (+3 xtests) after Task 1 · validateComponentIncludes strict 0.

**Ordering:** Task 1 and Task 2 are independent of each other; Tasks 3→4→5→6 are
strictly sequential. Task 1 before Task 4 is REQUIRED (Q3's cancellation behavior
and any prompt-unwind observations assume the retrofit).

**Known unknowns each executor must verify in place:**
- `JobImpl.invokeOnCancelRequest` handler parameter list (Job.kt:174) — adjust the
  Task 1 lambda header to match.
- Whether Suite 4 already has a slow-task fixture (Task 1 Step 1) — reuse over create.
- `brs("sleep(...)")` bare-call splice behavior (Task 1) — known-open splice bugs are
  BrsIf/BrsVariable; a bare call is expected clean, but eyeball the generated .brs.
- `FirTryExpressionChecker` existence and the suspend-lambda classification helper
  (Task 2 Step 3) — substitute the sibling checkers' actual base/helpers.
- Fixture marker positions (Task 2 Step 5) — semantics are the contract, positions
  follow the checker's positioning strategy.
- testsWithBrsStdLib registration mechanism (Task 2 Step 1) — mirror the last-added
  fixture.
- kotlin-probe deploy task name (Task 4 Step 1) — read `../roku-test-app/rebuild-all.sh`.
- The FIR suite run command (Task 2 Step 5) — if the gradlew line differs, use
  whatever CLAUDE.md "Running Tests" / the module's README prescribes.

**What is deliberately NOT in this plan:** Phases 2–4 (spec §6–§8). The spike's
decision tree has STOP outcomes that change or cancel that work; Stage 2 is planned
in Task 6 from recorded facts, not predictions.
