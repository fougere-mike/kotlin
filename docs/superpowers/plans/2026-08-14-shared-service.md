# SharedService Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking. This plan is SELF-CONTAINED for a fresh session: read the spec first (`docs/superpowers/plans/2026-08-14-shared-service-design.md` — its §1 decision table and §2 facts govern), then this plan; the ScopeHandle program's ledger snapshot is history, not required reading.

**Goal:** Ship reference-shared classes across components: `SharedService` + `shareOn`/`sharedFrom` over SetRef, real base-class hierarchies via `__proto`-name dispatchers, three FIR rules, and E2E Suite 9 with the fn-slot canary.

**Architecture:** Per the spec. Phase 1 ships sharing on today's slot dispatch (device-proven to work); Phase 2 the FIR rules; Phase 3 the dispatch lowering that removes the disclaimed-fn-ref dependence from every normal path; Phase 4 acceptance + docs.

**Tech Stack:** BRS stdlib (`libraries/stdlib/brs`), IR backend (`compiler/ir/backend.brightscript`), FIR checkers (`checkers.brs`), roku-test-app E2E (Suite 9).

## Global Constraints

- **Build:** `./rebuild.sh` only. Documented exceptions: the two `generateCheckersComponents` regen commands (CLAUDE.md "Regenerating FIR diagnostic containers") and the test-run commands in CLAUDE.md "Running Tests".
- **ENVIRONMENT PROCEDURE (MANDATORY, this machine kills background wakeups):** prefix every long command with `caffeinate -ims`; NEVER end a turn while a build/device run is in flight — background it to a scratchpad log, then stay in-turn with bounded foreground polls (`sleep 240; tail -5 <log>`, each Bash call under 10 minutes) until the completion marker (`=== Build complete ===` / `EXIT=0` / `All tests passed!` / results marker). Controllers: treat idle-without-report during a known run as a lost wakeup — check the log mtime/tail, then nudge.
- **Device:** credentials auto-resolve from `../roku-test-app/local.properties` (scripts fall back to it; for `./run-stdlib-tests.sh` source env vars from that file via `$(grep roku.deviceIp ../roku-test-app/local.properties | cut -d= -f2)` etc.). Console preflight before EVERY device run: `echo | nc -w 3 $(grep roku.deviceIp ../roku-test-app/local.properties | cut -d= -f2) 8085` → `Console connection is already in use` = STOP and ask Mike. Single-client console.
- **Gate baselines at start (must not drop; all grow):** goldens 71 · FIR 225 · stdlib device 514/51 · E2E 72 active / 8 suites (+3 xtests) · `validateComponentIncludes` + `validateTestComponentIncludes` strict 0. Record actuals per task in the ledger.
- **Commits:** per-task on `feature/brightscript-backend-2.2.20`; prefixes `stdlib:` / `brs:` / `test-infra:` / `docs:`; every stdlib source change (comments included) is followed by a separate `brs-prebuilt: regenerate stdlib klib (<reason>)` commit; all messages end `Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>`. roku-test-app is a SEPARATE repo — commit separately.
- **Golden discipline:** every golden diff reviewed line-by-line before committing; unexpected churn in goldens you didn't target = stop and investigate. Broken-shape evidence goldens are committed BEFORE fixes when pinning a defect.
- **Spec bindings (verbatim):** OS floor — `shareOn` THROWS pre-15 ("SharedService requires Roku OS 15.0+ (SetRef) — the MVVM-layer floor decision (design A4); gate with canShare()"); stash field `__kotlinShared` (AA, SetRef-written, never ordinary setField); keys = class name by default, explicit `key` overload; acquisition returns GetRef references only; republish replaces; `SharedService` ClassId is the SOLE machinery root (no ViewModel type anywhere); concrete shared classes final; single-module closed world documented.
- **Immaculate diagnostics:** every FIR message and runtime error names the fix. `@Suppress` escapes with tracking comments.
- **No parallel implementers** (shared build dirs + single device console).

## File Map (who owns what)

| File | Task | Action |
|---|---|---|
| `libraries/stdlib/brs/src/kotlin/brs/roku/SceneGraph.kt` (ISGNodeField + RoUtils) | 1 | Modify/Add |
| `libraries/stdlib/brs/src/kotlin/brs/shared/SharedService.kt` (public surface) | 2 | Create |
| `libraries/stdlib/brs/src/kotlin/brs/shared/SharedStash.kt` (internal stash/keys/messages) | 2 | Create |
| `libraries/stdlib/brs/test/kotlin/` new SharedServiceTest file (mirror suite conventions) | 2 | Create |
| `../roku-test-app/components/fixtures/SharedOwnerProbe.kt`, `SharedConsumerProbe.kt`, `SharedFixtures.kt` (hierarchy classes) | 3 | Create |
| `../roku-test-app/src/brsTest/kotlin/tests/SharedServiceTests.kt` (Suite 9) + `TestMain.kt` registration | 3, 7 | Create/Extend |
| `core/compiler.common.brightscript/.../BrsStandardClassIds.kt` (SharedService ClassId) | 4 | Modify |
| `checkers.brs`: shared supertype-walk predicate + `FirBrsSharedClassChecker.kt` (rules 1+2) + `FirBrsSharedCopyChannelChecker.kt` (rule 3) + diagnostics list + BOTH regens + messages + fixtures `sharedClassRules.kt` / `sharedCopyChannel.kt` | 4 | Create/Modify |
| `compiler/ir/backend.brightscript/.../lower/BrsSharedDispatchLowering.kt` (call-site rewrite) + emission changes in `IrToBrsTransformer.kt` (ctor/method emission for shared classes) + dispatcher generation in `BrsCompiler.kt` (binding-table precedent) | 5, 6 | Create/Modify |
| `compiler/testData/codegen/brs/shared/` goldens: `sharedEmission.kt`, `sharedDispatch.kt`, `sharedFromLowering.kt` | 5, 6 | Create |
| `CLAUDE.md` SharedService section + gate table; backlog append | 8 | Modify |

Ordering: 1 → 2 → 3 sequential (Phase 1). 4 (Phase 2) after 3. 5 → 6 → 7 sequential (Phase 3). 8 last. No task may start before its predecessor's review closes.

---

# Phase 1 — sharing on today's dispatch

### Task 1: SetRef/roUtils stdlib bindings

**Files:**
- Modify: `libraries/stdlib/brs/src/kotlin/brs/roku/SceneGraph.kt` (ISGNodeField interface ~:62-241; add RoUtils near the other CreateObject-factory interfaces)
- Test: extend `libraries/stdlib/brs/test/kotlin/` — READ a neighboring test file first for suite/registration conventions

**Interfaces:**
- Consumes: existing external-interface declaration style (KDoc with roku.com URL, `@BrsName` only for overload folding; external-interface methods emit unmangled simple names at call sites).
- Produces (Task 2+ compile against these exact signatures, on ISGNodeField so RoSGNode inherits them):

```kotlin
public fun setRef(fieldName: String, data: Dynamic): Boolean   // Boolean per device + doc Return Value section
public fun canGetRef(fieldName: String): Boolean
public fun getRef(fieldName: String): Dynamic
public fun moveIntoField(fieldName: String, data: Dynamic): Int
public fun moveFromField(fieldName: String): Dynamic

public external interface RoUtils {
    fun isSameObject(data1: Dynamic, data2: Dynamic): Boolean
    fun deepCopy(data: Dynamic): Dynamic
    companion object { @BrsCreateObject("roUtils") fun create(): RoUtils }   // ALL params required — none here
}
```

- [ ] **Step 1:** Read `spikes/scope-handle-spike/kotlin-probe/components/shared/SpikeHelpers.kt` (:57-88) — the splice precedent these bindings retire (leave the spike untouched; it is historical evidence). KDoc each binding with the spike-pinned constraints: OS 15.0+, `setRef`/`canGetRef`/`getRef` render-thread-only + AA-typed fields + unusable with queueFields + observer-SILENT; `moveIntoField`/`moveFromField` any-thread; source object emptied on move (nested external refs copied).
- [ ] **Step 2:** Declare the five ISGNodeField methods + RoUtils per the Produces block.
- [ ] **Step 3:** Unit tests (runBlocking regime; these run on the MAIN thread where SetRef APIs legitimately no-op/fail — so test only what is testable there): `RoUtils.create()` returns non-null on device; `isSameObject(a, a)` true and `isSameObject(a, b.deepCopy-of-a)` false for a heap AA — this IS main-thread-legal (roUtils has no render-thread restriction per the doc). 2-3 tests.
- [ ] **Step 4:** `caffeinate -ims ./rebuild.sh` (log + in-turn polls; step 7 green), then stdlib device suite (514 + new; record), then `./run-compiler-tests.sh` (71 goldens unchanged).
- [ ] **Step 5:** Commits: `stdlib: SetRef/GetRef/Move*/roUtils bindings (OS 15 reference APIs)` + separate klib regen commit. Standard trailers.

### Task 2: SharedService + shareOn/sharedFrom

**Files:**
- Create: `libraries/stdlib/brs/src/kotlin/brs/shared/SharedService.kt`, `SharedStash.kt`
- Test: new stdlib test file (same conventions as Task 1's)

**Interfaces:**
- Consumes: Task 1's bindings; ScopeApi.kt precedents — ambient-component check shape (ComponentMailbox.postScopeRequestAndAwait :168-172 throws the "must be called from a render-thread component context" ISE), per-component holder objects, `scopeHostTopOf`-style @BrsInline top/global bridges (ScopeApi.kt:150-154).
- Produces (spec §12 verbatim — Tasks 3-7 compile against these):

```kotlin
// package kotlin.brs — SharedService.kt
public abstract class SharedService {
    public fun isLive(): Boolean
    // internal stash back-reference: node + key, set by shareOn (null until shared → isLive() false)
}
public fun shareOn(node: RoSGNode, instance: SharedService): Unit
public fun shareOn(node: RoSGNode, instance: SharedService, key: String): Unit
public fun canShare(): Boolean
public inline fun <reified T : SharedService> sharedFrom(node: RoSGNode): T
public inline fun <reified T : SharedService> sharedFrom(node: RoSGNode, key: String): T
public inline fun <reified T : SharedService> sharedFromOrNull(node: RoSGNode): T?
public inline fun <reified T : SharedService> sharedFromOrNull(node: RoSGNode, key: String): T?
```

Behavioral contract (each numbered item gets a device test in Task 3):
1. `shareOn`: ambient render-component check (ISE naming the rule, runTask/ScopeHandle precedent) → `canShare()` false → ISE with the spec's verbatim floor message → ensure stash field: `node.addField("__kotlinShared", "assocarray", false)` if absent → read-modify-write the stash AA **via getRef/setRef only** (first publish: fresh AA + setRef; later: getRef, mutate — reference semantics make the mutation live — no re-setRef needed after the first) → entry `stash[key] = instance` → set the instance's internal back-reference (node, key). Same key → replace (KDoc: prior generation's `isLive()` goes false).
2. Default key: instance's runtime class name — read `__proto` head via a small `@BrsInline` helper (`"return instance.__proto[0]"` shape; VERIFY the proto layout against a generated class before finalizing — explorer: proto[0] is the class's own name, IrToBrsTransformer.kt:2622-2634).
3. `canShare()`: render thread + feature-detect (`CreateObject("roSGNode","Node")` probe node + `canGetRef` after a setRef? SIMPLER: attempt `node.setRef` on a scratch node's added field and return the Boolean — VERIFY cheapest reliable detect in place; PumpScheduler's CreateObject-returns-invalid pattern is the precedent family). Cache per component (holder object).
4. `sharedFrom<T>`: reified lowering mirrors `createComponent<T>()` (ComponentFactory.kt:27, backend-lowered — READ how its reified type resolves to a name string before implementing; if the existing inline-reified path suffices without new compiler work, use it — the acceptance criterion is that `sharedFrom<GuideVm>(node)` compiles to a call carrying "GuideVm"). Sequence: `canGetRef("__kotlinShared")` false → (orNull: null | throwing: guided ISE "nothing shared on node '<id>' — shareOn(node, instance) in the owner first, or use sharedFromOrNull") → `getRef` → `stash[key]` absent → same treatment with the key in the message → present but fails `is T` (proto walk — the existing `__kotlin_isInstanceOf` machinery) → ISE "entry under key '<k>' is a <actual>, not <T> — key collision; use explicit keys".
5. `isLive()`: back-ref null → false; else `RoUtils.create().isSameObject(this, node.getRef("__kotlinShared")[key])` — with destroyed-node guard (getRef failure → false).
- [ ] **Step 1:** stdlib unit tests first (main-thread-testable subset): `shareOn` outside component context throws the exact ISE; `sharedFrom` ditto; `isLive()` false on a never-shared instance; key-derivation helper returns the class simple name for a test class; `canShare()` returns false off the render thread (main-thread regime); message texts contain the guided fragments. (~6 tests.)
- [ ] **Step 2:** Implement per contract.
- [ ] **Step 3:** `caffeinate -ims ./rebuild.sh` + stdlib device suite (record; expect prior + ~5) + goldens (unchanged unless sharedFrom needed lowering help — if goldens change, that lowering gets its own reviewed golden IN THIS TASK).
- [ ] **Step 4:** Commits: `stdlib: SharedService + shareOn/sharedFrom over SetRef (reference-shared classes)` + klib regen (+ `brs:` commit only if reified lowering needed compiler work — separable).

### Task 3: E2E Suite 9 part 1 — sharing acceptance

**Files:**
- Create: `../roku-test-app/components/fixtures/SharedOwnerProbe.kt`, `SharedConsumerProbe.kt`, `SharedFixtures.kt`
- Create: `../roku-test-app/src/brsTest/kotlin/tests/SharedServiceTests.kt`; Modify `TestMain.kt` (register Suite 9 like Suites 0-8)

**Interfaces:**
- Consumes: Task 2's surface; E2E driver API (`testAsync`/`roundTrip`/`awaitFieldEquals` — DeviceTestLoop.kt); probe adopt pattern (Probes.kt + ScopeHandleTests.kt's multi-probe management :29-30); fixture style (TypedTaskProbe/ScopeOwnerProbe: package-per-component, mode/op dispatch, outcome alwaysNotify, single-catch + is-tests ONLY — multi-catch is fixed but keep suite style consistent).
- Produces: fixtures Task 7 extends. `SharedFixtures.kt` (plain classes, shared by both probes' closures):

```kotlin
abstract class FixtureBase : SharedService() {          // member-bearing base — legal
    var baseTouches: Int = 0
    fun touchBase(): Int { baseTouches += 1; return baseTouches }   // final member
    open fun describe(): String = "base"                             // hook WITH impl (dispatcher + super pin in Phase 3)
    fun template(): String { return "T:" + describe() }              // base-internal this.hook()
}
class FixtureVm : FixtureBase() {
    var counter: Int = 0
    fun bump(): Int { counter += 1; return counter }
    override fun describe(): String = super.describe() + ":vm:" + counter   // override + super call in one pin
}
class FixtureSvc(val tag: String) : SharedService() {
    var hits: Int = 0
    fun hit(): Int { hits += 1; return hits }
}
```

- [ ] **Step 1:** Fixtures. Owner probe ops: `publish` (shareOn FixtureVm + FixtureSvc on top), `publishKeyed` (two FixtureSvc under keys "a"/"b"), `republish` (new FixtureVm same key), `mutate` (vm.bump() owner-side, outcome = counter), `publishOnScene` (shareOn(top.getScene(), FixtureSvc("scene"))). Consumer probe (given ownerRef via @SGNodeField): `acquireMutate` (sharedFrom<FixtureVm>, bump, outcome=counter), `acquireMissing` (sharedFrom before publish → catch ISE, outcome carries message fragment), `acquireOrNull`, `acquireKeys` (both "a"/"b", distinct tags), `staleLiveness` (hold ref → owner republishes → outcome = "old:" + oldRef.isLive() + " new:" + sharedFrom<FixtureVm>(ownerRef).isLive()), `sceneAcquire` (sharedFrom via top.getScene()).
- [ ] **Step 2:** Suite 9 part-1 tests (each testAsync, pre-armed awaits where ordering matters — arming-order law):
  1. `sharedSameInstance` — owner publish + mutate (counter→1), consumer acquireMutate (→2), owner mutate again (→3): three writers, one instance, values prove identity.
  2. `sharedAcquireBeforePublishGuided` — consumer first: outcome contains "shareOn(node, instance) in the owner first".
  3. `sharedOrNullNull` — orNull before publish → "null".
  4. `sharedExplicitKeys` — keys "a"/"b" acquire distinct instances (tags differ).
  5. `sharedWrongTypeKeyCollision` — acquire `sharedFrom<FixtureSvc>` under the VM's key → ISE fragment "key collision".
  6. `sharedRepublishStaleLiveness` — "old:false new:true".
  7. `sharedSceneStash` — publishOnScene then a DIFFERENT consumer acquires via getScene(): same instance (hit counts chain).
- [ ] **Step 3:** `cd ../roku-test-app && caffeinate -ims ./rebuild-all.sh --all` (log+polls) → preflight → `caffeinate -ims ./run-device-tests.sh`. Expect 72 + 7 = 79 active / 9 suites; both validators strict 0. A sharing failure here is a Task 1/2 defect: STOP, report with the device log.
- [ ] **Step 4:** Commit (roku-test-app): `test-infra: Suite 9 SharedService — sharing acceptance (slot-dispatch era)`.

---

# Phase 2 — FIR rule family

### Task 4: The three shared-class rules

**Files:**
- Modify: `core/compiler.common.brightscript/.../BrsStandardClassIds.kt` (add `kotlin.brs.SharedService` ClassId — the SOLE root; mirror how Scope ids use `brsId()`)
- Create: shared supertype-walk predicate (one helper used by BOTH new checkers AND Phase 3's lowering — extract to a location both modules see; if checkers and backend cannot share source, mirror with a cross-reference comment naming both sites)
- Create: `FirBrsSharedClassChecker.kt` (rules 1+2, declaration checker), `FirBrsSharedCopyChannelChecker.kt` (rule 3, expression checker) in `checkers.brs/src/.../brs/checkers/` + registrar entries in `BrsExpressionCheckers.kt`/declaration equivalent (mirror FirBrsScopeRequestDeclarationChecker's registration)
- Modify: `FirBrsDiagnosticsList.kt` + BOTH regens (commit generated files) + `FirBrsErrorsDefaultMessages.kt` + harness template map
- Create: fixtures `compiler/testData/diagnostics/testsWithBrsStdLib/sharedClassRules.kt`, `sharedCopyChannel.kt` (+ registration per suite convention)

**Interfaces:**
- Consumes: FirBrsScopeRequestDeclarationChecker (supertype-walk precedent — but note it walks DIRECT supertypes; THESE rules need the FULL transitive walk with `fullyExpandedType` at each hop), FirBrsTryFinallyChecker registrar precedent, the regen ritual.
- Produces: `BRS_SHARED_CLASS_NOT_FINAL` (ERROR), `BRS_SHARED_FN_PROPERTY` (ERROR), `BRS_SHARED_THROUGH_COPYING_CHANNEL` (ERROR) — all suppressible; messages verbatim from spec §5. Phase 3's lowering assumes rule 1 holds (dispatchers enumerate concrete leaves).

- [ ] **Step 1 (red):** fixtures first. `sharedClassRules.kt` cases: open concrete subclass → NOT_FINAL fires; final concrete → clean; abstract base WITH members and an abstract hook → CLEAN (the design's signature case — pin it); deep chain (abstract → abstract → final) → clean; fn-typed property on base → FN_PROPERTY fires; on final subclass → fires; non-shared class with fn property → clean; @Suppress each. `sharedCopyChannel.kt` cases: SharedService-typed value into `setField` value arg → fires; into a `@SGArrayField`-typed task-component field assignment → fires (VERIFY the checkable shape: the @SG declaration TYPE cannot be SharedService — flag at the property declaration on TaskComponent subclasses); as `callFunc` arg → fires; passing the NODE + sharedFrom → clean; @Suppress.
- [ ] **Step 2:** diagnostics list + both regens + messages (spec §5 verbatim; NO literal braces in parameterized templates — MessageFormat trap, comment precedent at the existing site) + checkers + registrar. Rule 1: concrete = non-abstract class reaching SharedService transitively; fires when modality is OPEN (Kotlin default final passes silently). Rule 2: any `FirProperty` whose type `isSuspendOrKSuspendFunctionType || isFunctionType` inside a shared hierarchy. Rule 3: call-site checker keyed on known copying APIs (setField/callFunc callable ids + @SG-field-typed assignments) with a SharedService-reaching argument static type.
- [ ] **Step 3:** FIR suite green — record (225 + 2 fixture files). Semantics wrong → fix checker, never markers-to-match.
- [ ] **Step 4:** `caffeinate -ims ./rebuild.sh` — step 7 is the stdlib false-positive canary (SharedService's own file must not self-trip; the stash internals pass no SharedService values into setField — the stash rides setRef, which is NOT a flagged channel — verify zero suppressions needed).
- [ ] **Step 5:** goldens 71 unchanged; stdlib device suite unchanged (record); commit `brs: SharedService FIR family — final-class, fn-property, copy-channel rules` (generated containers included).

---

# Phase 3 — static dispatch

### Task 5: Extension-shaped emission + wrapper slots (shared classes only)

**Files:**
- Modify: `compiler/ir/backend.brightscript/.../irToBrs/IrToBrsTransformer.kt` — method emission + `addMethodAttachments` (:2838-2938) + ctor emission for classes passing the shared predicate
- Create: golden `compiler/testData/codegen/brs/shared/sharedEmission.kt` + `.brs.txt`

**Interfaces:**
- Consumes: Task 4's shared predicate; the extension emission shape (receiver appended to args, IrExpressionToBrsTransformer.kt:1705/:1748); suspend lowering interplay (BrsSuspendFunctionsLowering) — **VERIFY IN PLACE before coding: the suspend-EXTENSION parameter ordering (receiver vs `_completion`)** by compiling a scratch suspend extension and reading the output; shared suspend members must match it exactly.
- Produces (Task 6 compiles call sites against these): for every declared function `f` of a shared class `C` — a global `C_f_<mangle>` taking the receiver as a trailing explicit param (extension convention), a wrapper slot attached in the ctor whose body is exactly `return C_f_<mangle>(<params...>, m)` (arity- and continuation-correct, generated from the same signature), unchanged `__proto`/`__id`/`__type` emission, simple `val`/`var` reads still direct member access.

- [ ] **Step 1 (red):** golden source — `FixtureBaseG` (abstract, one final member, one abstract hook, one suspend member) + `FixtureVmG : FixtureBaseG` (override + own method + plain properties) + a non-shared control class. Generate the CURRENT golden: shows slot-attached methods with `m`-reading bodies for ALL classes (the control class must KEEP this shape forever).
- [ ] **Step 2:** implement emission switch for shared classes: method bodies read the explicit receiver param (the Stage 2 `<this>`/Regular-kind render rules are the safety net — its goldens are the regression net); ctor attaches WRAPPERS instead of direct globals; accessors: non-trivial accessors get the same treatment, simple ones stay direct member reads (data-class precedent :1585-1591).
- [ ] **Step 3:** regenerate golden; review line-by-line: shared classes flipped (extension-shaped bodies + wrapper slots), control class byte-identical, `__proto` chains unchanged. Goldens 71→72 (record).
- [ ] **Step 4:** `caffeinate -ims ./rebuild.sh` (step 7) + FULL golden run (any non-target churn = stop) + stdlib device suite (unchanged — stdlib has no SharedService subclasses) + **E2E run**: Suite 9 part 1 must stay green — sharing tests now exercise wrapper slots cross-component (the wrappers' first device proof). Expect 79/9.
- [ ] **Step 5:** commit `brs: extension-shaped emission + forwarding wrapper slots for SharedService classes`.

### Task 6: Call-site lowering + proto dispatchers

**Files:**
- Create: `compiler/ir/backend.brightscript/.../lower/BrsSharedDispatchLowering.kt` (call-site rewrite; pipeline position after the scope-run-block lowering, before UpgradeCallableReferences — mirror BrsScopeRunBlockLowering's registration)
- Modify: `BrsCompiler.kt` — dispatcher generation (populateScopeBindingTables precedent: whole-module registry → per-file emission)
- Create: golden `compiler/testData/codegen/brs/shared/sharedDispatch.kt` + `.brs.txt`

**Interfaces:**
- Consumes: Task 5's emitted names (`C_f_<mangle>`), Task 4's predicate + rule 1 (concrete leaves final).
- Produces: call sites on shared-class receivers compile as — receiver static type FINAL shared class → `C_f_<mangle>(args..., recv)`; receiver static type ABSTRACT shared base + method open/abstract → `Base_f_<mangle>_dispatch(args..., recv)`; final method via base type → direct static to the declaring class's impl; `super.f()` → direct static to the superclass impl; receiver typed Any/interface → UNCHANGED slot call (documented residual). Dispatcher bodies: read runtime class name from `recv.__proto` head, if-chain over the compilation's concrete descendants (source-name order for determinism), else-arm = guided runtime error naming the class and "recompile with the module containing it" (closed-world message); abstract-with-no-descendants → body is only the guided error.

- [ ] **Step 1 (red):** golden source exercising every shape: direct final call, base-typed hook call, base-internal `this.hook()` (template), `super` call, Any-typed residual (stays slot), suspend member call (continuation threading through dispatcher). Current golden shows slot dispatch everywhere.
- [ ] **Step 2:** implement lowering + dispatcher generation.
- [ ] **Step 3:** golden green, line-by-line review; goldens 72→73.
- [ ] **Step 4:** gates: `caffeinate -ims ./rebuild.sh` + full goldens + FIR (unchanged 227-region — record actual from Task 4) + stdlib suite + E2E (79/9 still green — Suite 9 now runs on lowered dispatch end-to-end).
- [ ] **Step 5:** commit `brs: static call sites + __proto-name dispatchers for SharedService hierarchies`.

### Task 7: Suite 9 part 2 — dispatch on device + CANARY

**Files:**
- Modify: `../roku-test-app/components/fixtures/SharedOwnerProbe.kt`/`SharedConsumerProbe.kt` (+ops), `SharedFixtures.kt` (only if a shape is missing)
- Modify: `../roku-test-app/src/brsTest/kotlin/tests/SharedServiceTests.kt`

**Interfaces:**
- Consumes: Tasks 5-6 shipped; FixtureBase/FixtureVm from Task 3 (template()/describe()/touchBase() were designed for exactly these tests).
- Produces: the program's device pins + the canary.

- [ ] **Step 1:** consumer ops + tests:
  8. `sharedFinalStaticCall` — cross-component `vm.bump()` (direct static): counter chains across components.
  9. `sharedHookThroughBase` — consumer holds `val b: FixtureBase = sharedFrom<FixtureVm>(...)`; `b.describe()` → dispatcher → "base:vm:<n>" (override reached through base static type AND the override's `super.describe()` call, in one pin, cross-component).
  10. `sharedTemplateMethod` — `b.template()` → "T:base:vm:<n>" (base-internal `this.hook()` dispatch).
  11. `sharedBaseFinalMember` — `b.touchBase()` via base type: direct static, state shared.
  12. `sharedSuspendMember` — a suspend method on FixtureVm called cross-component (add `suspend fun slowBump(): Int { delay(50); return bump() }` to the fixture in this task): proves continuation threading through the lowered path on device.
  13. `sharedCanaryFnSlot` — **CANARY**: invoke a wrapper slot AS a slot cross-component via Dynamic receiver (`(vm as Any)`-typed call or an explicit `@BrsInline("return vm.bump_k_()")` splice mirroring the spike's callBumpDynamic — pick whichever survives FIR cleanly and comment WHY). Test comment block (verbatim requirement): "CANARY: pins Roku's officially-disclaimed fn-ref-through-SetRef behavior. RED here + everything else green = Roku changed it. Runbook: CLAUDE.md 'SharedService' section — verify tests 8-12 green, then the wrapper-slot residual paths are dead: schedule their removal and retire this canary. Do NOT 'fix' this test."
- [ ] **Step 2:** device run: expect 79 + 6 = 85 active / 9 suites; validators 0. A dispatch failure implicates Task 5/6 — STOP with generated-BRS + device-log evidence.
- [ ] **Step 3:** commit (roku-test-app): `test-infra: Suite 9 part 2 — static dispatch on device + fn-slot canary`.

---

# Phase 4 — acceptance + docs

### Task 8: Sweep, CLAUDE.md, backlog

**Files:**
- Modify: `CLAUDE.md` (new "SharedService (reference-shared classes)" section after ScopeHandle; gate table + suites table + header date), `docs/superpowers/backlog-typed-task-program.md` (SharedService section)

**Interfaces:** consumes everything; controller does memory updates (NOT this task).

- [ ] **Step 1:** full sweep, both repos, all gates (`caffeinate -ims` + polls throughout): rebuild.sh, goldens (73), FIR (record Task 4's actual), stdlib (record), E2E (85/9), both validators. Record the final table.
- [ ] **Step 2:** CLAUDE.md section per spec §8: API + the canonical GuideVm example (spec §3), scene-stash idiom, node-handoff + onChange timing, recreate-don't-reuse + close-is-terminal cross-ref, dispatch model paragraph (final→static, hooks→proto dispatchers, residual Any/interface shapes, wrapper slots, single-module closed world), CANARY meaning + runbook, the three FIR rules table, test-hook inventory (canShare), gate table with sweep actuals.
- [ ] **Step 3:** backlog append: interface-typed receiver dispatch (residual); multi-module dispatchers → FIR diagnostic when real; unpublish/retract API (v1: republish + node death); `@SGRequired`/`onInputsReady` + `createComponent{}` configure-lambda (decision 10 pair); accessor-slot treatment revisit if non-trivial accessors prove common; canary-retirement runbook pointer.
- [ ] **Step 4:** commits: `docs: CLAUDE.md SharedService section + gate table` (+ backlog in same commit). Report the final gate table for the controller's memory pass.

---

## Plan self-review notes (kept for the executor)

**Numbering truth (record ACTUALS):** goldens 71 → 73 (T5, T6; +1 more if T2's sharedFrom needed lowering — record). FIR 225 → +2 fixture files (T4). Stdlib 514/51 → + T1/T2 units (~8). E2E 72/8 → 85/9 (7 + 6 new). Counting note: the gate counts EXECUTED tests (one pre-program commented-out `// @Test` exists in BrsGoldenFileTests.kt — see CLAUDE.md's gate-table note).

**Known unknowns each executor must verify in place:**
- Suspend-extension parameter ordering (receiver vs `_completion`) — T5 Step 0 scratch compile; shared suspend members must match.
- `sharedFrom` reified resolution — mirror `createComponent<T>()`'s lowering (ComponentFactory.kt:27); if plain inline+reified suffices, no compiler work (T2).
- `__proto[0]` layout for the key-derivation helper (T2) — read one generated ctor.
- Cheapest reliable `canShare()` detect (T2) — PumpScheduler CreateObject-invalid family.
- The copy-channel checker's @SG-field shape (T4) — flag at TaskComponent property declarations, not assignments, if that is the checkable form.
- checkers.brs ↔ backend predicate sharing (T4) — if module boundaries force duplication, mirror with cross-reference comments both sites (accepted deviation, note in report).
- Dispatcher pipeline position (T6) — after scope-run-block lowering; verify no interference with BrsMultiCatchLowering (0.052) or the suspend lowering.
- Canary call shape (T7) — must survive FIR (Any-typed receiver is the documented residual, so it should; else the @BrsInline splice with a comment).

**What is deliberately NOT here:** StateFlow, @AppScoped, spawnTask, the input-DX pair (recorded adjacent, spec §11), unpublish API, interface-receiver dispatch, multi-module. The spec's §10 risks are the review lens for T5/T6 — wrapper arity (esp. continuations) and emission-shape churn are the two named hazards.
