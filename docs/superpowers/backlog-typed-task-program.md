# Post-Merge Backlog — Typed-Task + E2E Program (closed 2026-08-10)

Triaged by the final whole-branch review (verdict: merge-ready, zero pre-merge items).
Source ranges: Kotlin 44573c8df0d8..bb3bc656af1e, roku-test-app aa524b6..11ca54d, kotlin-roku 101ac95..5048fc1.

## P1 — Silent-miscompile class (wrong code, no diagnostic)
- `@BrsInline` template with single-line if/else splices literal `invalid` at expression call sites.
- try/catch in EXPRESSION position emits literal `return try` (statement position fine).
- `visitGetField` not ported in the state machine (suspend call as field-read receiver) — same bare-emission family fixed twice already; likeliest next bite.
- Catch-var hoisting: catch-block elvis temp hoisted above try (`e` referenced before try) — today only trips quarantined CoroutineTaskKt.brs.

## P2 — E2E-gate trust residual
- Run-ID handshake: a FULLY-replayed previous run (sentinel..END in console backlog) before the new sentinel = false green in BOTH runners (KGP RunRokuTestsTask + run-tests.sh; shells share the hole). Closure: runner requires sentinel ts > install time, or device-side run-ID echo.
- Smaller rails items: unbounded port-drain loop (add cap+print); throwing awaitField predicate bypasses endRun (documented caution today — try/finally closes it); DeviceTestLoop drain-guarantee comment slightly overstates mechanism; sentinel freshness constants derived from one place.

## P3 — Typed-task v1 hardening
- Task #19 (board): class-level checker for inherited task state (BRS_TASK_STATE_NOT_FIELD v2); fold in: delegated-property skip revisit; interface-ancestor lookupInterfaces divergence.
- FIR checker for derived component re-declaring a USER-ancestor's field with a different annotation (duplicate <field> = device-fatal at creation).
- Guard user WRITES to kotlinTask* protocol fields (public vars, KDoc-only protection).
- BrsRunTaskCallLowering: loud error instead of silent whole-pass no-op on missing stdlib symbols (version skew).
- runTask via callable ref / user reified forwarder falls to runtime stub (same as createComponent) — document or diagnose.
- Component class/file name coupling (F1, task-12); nullable Int on @SGIntegerField unexercised by extractor/FIR (from the UrlTransferTask experiment, rta 11ca54d).
- JobImpl.join()/await() real continuation queue (M3); then re-layer withContext(Dispatchers.IO) as sugar over typed tasks, replacing the quarantined IOWorker pipeline; optional BRS_IO_DISPATCHER_UNVERIFIED warning meanwhile.
- TaskPool.kt:341 non-null getRoSGNode deref (quarantined — fix if pipeline revived).
- M2 multi-emission (taskEmit/collectEmissions doorbell+drain protocol — design in the typed-task sub-plan; spike-proven zero-coalescing datum).
- M3 cancellation/timeout (control="STOP", runTask(timeoutMs)).

## P4 — Cleanup debt
- Remove retired compiler-side layout-accessor generation machinery (dead in live pipeline; access sites fixed; golden layoutStubAccess is the tripwire).
- Rename builtins Primitives.kt vs runtime primitives.kt (case-colliding .brs output; APFS clobber; nothing calls clobbered names today). NOT done by Task 14 despite earlier ledger wording.
- Assorted cosmetics from task reviews (stub-generator named-arg regex false-match on *id; @BrsField KDoc empty-string sentinel; warnDroppedAttribute import/state style; validator UP-TO-DATE warning visibility; TestEvent visibility; hybrid CopyKotlinToBsTask overwrite pre-merge-guard; "called from" wording for mixed call+ref).

## P5 — Documented platform gaps (already in CLAUDE.md)
- Native for-each over RoArray user path not compilable (NativeIterable.iterator() lacks `operator`; NativeArrayIterator lacks hasNext/next).
- RoArray operator get emits nonexistent `.get(i)` (drain via count()/shift() meanwhile — see ShelfView comment).

## Consciously dropped
- DeepDepsProbe runtime fixture (plan Phase 1b) — coverage subsumed by deps.json goldens + strict validator + device suites.

# ScopeHandle Stage 2 backlog (recorded 2026-08-14, Task 8)

Source ranges: Kotlin e56b1603b368..HEAD of the Stage 2 program, roku-test-app
5cf871e.. , kotlin-roku 4fe70b2. Decisions of record:
`docs/superpowers/plans/2026-08-12-scopehandle-design.md` Addendum A.1–A.6 +
`spikes/scope-handle-spike/FINDINGS.md` §6. Execution ledger:
`.superpowers/sdd/2026-08-12-scopehandle-stage2/progress.md`.

## Deferred by design decision (pointer items)
- **SetRef payload stash** (decision A5): roSGNode SetRef as a zero-copy
  payload channel — deliberately NOT in v1; revisit with the VM program.
- **MoveIntoField typed-task output optimization** (M3/runTask backlog):
  any-thread per RokuDocs; only the "copied because externally referenced"
  half is device-pinned so far — the "moved half" (uniquely-referenced value
  actually MOVES, no copy) is UNPINNED; pin before relying on it.
- **VM-program items** (decision A.4 — picked up by the VM/MVVM brainstorm,
  not Stage 2): day-one FIR rules for shared VMs (final classes, no
  function-typed properties), a liveness marker helper (`as?` PASSES on husks
  — no type-check guard exists), a permanent device canary pinning
  fn-slot-through-SetRef behavior, static-dispatch demotion feasibility.

## Protocol/runtime residuals
- **alwaysNotify × scoped-observer cell unprobed**: the inbox fields are
  declared alwaysNotify=true; the identical-envelope-value re-delivery cell of
  the FieldSemantics truth table has no scope-carrier probe.
- **exposeScope partial-failure retry message misdescribes**: the
  double-call guard's "reuse the ScopeHost returned by the first call" wording
  is wrong for a first call that THREW mid-arming (nothing to reuse; state
  holder already installed) — and also wrong after close(): close is terminal
  and no re-expose exists, so there is no host to reuse there either. Low
  stakes, wording + maybe holder rollback.
- **missing-advertisement fail-fast un-pinned on device**: the "node has not
  exposed a scope" IllegalStateException path has unit coverage only;
  ScopeOwnerProbe's "neverReply" mode is the natural fixture (kept alive for
  exactly this).
- **unknown-kind law on rtq = structural whitelist only**: the rtq handler
  ignores unknown kinds by code shape (explicit kind whitelist), but no device
  test can POST an unknown kind to a live rtq channel from outside the
  protocol; the field-carrier device pin was restored by Task 8's fieldForced
  ride-along. Acceptable: the law is structural on rtq by construction.
- **Session-wide force hook unconsumed**: `kotlinScopeForceFieldBackend(global)`
  has no E2E consumer (local hook covers the mixed-pair tests); kept for pump
  parity (`kotlinPumpForceTimerBackend` precedent). Exercise or accept.
- **TaskRunner awaitCompletion fast-paths run before entry ensureActive**
  (Stage 1 finding): an already-cancelled caller awaiting an already-done task
  gets the value instead of CancellationException. Fix on next TaskRunner
  touch with the Await.kt all-inside-block shape.

## Compiler/FIR residuals
- **Single-narrow-clause-catches-everything hole** (pre-existing, deliberately
  preserved by Task 4's multi-catch fix): a suspend-context `try` with ONE
  typed catch clause still catches everything (clause type ignored). The fix
  B asymmetry (multi-catch dispatches correctly, single-catch doesn't) is
  documented in the lowering; closing it is a behavior change needing its own
  goldens.
- **Cross-module `run {}` blocks**: lowering + binding table are single-module
  today; a block declared in another module gets the guided dispatch-miss at
  runtime (never a hang). Document until multi-module lands.
- **Int-division-emits-float-division codegen wrinkle** (Task 1 observation):
  Kotlin `Int / Int` emits BrightScript `/` (float division) with a
  truncation wrapper only in some positions; audit the arithmetic lowering.
- **nested run{}-in-run{} double-report unexamined**: whether the capture
  checkers report twice (outer + inner walk) for a run block nested in
  another run block was not examined; fixture it before touching the walker.
- **generic-intermediate-base ScopeRequest evasion** (documented v1): a
  request class deriving ScopeRequest via a user generic intermediate base
  evades the declaration checker's direct-supertype scan.
- **apply-receiver-nested false-positive fixture**: `apply {}` receiver use
  nested inside a run block — suspected capture-checker false positive;
  fixture to pin intended behavior.
- **FIR checker keying-prologue duplicated** between FirBrsScopeBlockChecker
  and FirBrsScopeCaptureChecker (~6 lines); cosmetic dedup candidate.

## Test-infrastructure residuals
- **FIR harness verifies name multisets, not positions**: the checkers.brs
  fixture harness matches diagnostic NAME multisets per file, not marker
  positions — a diagnostic firing on the wrong element with the right name
  passes. Note when reading fixtures as evidence; harness upgrade is its own
  task.
- **kotlin-roku fold-ins**: (1) plain `.kt` files under `components/` are
  staged by neither pipeline — the consumer-side wiring workaround lives in
  roku-test-app's build; fold into the plugin (joins the spike KGP hole).
  (2) rokuTest sentinel nonce (replay guard) still wrapper-side; plugin-side
  fix pre-existing backlog.
- **channel-matrix vs kotlin-probe deploy.sh run-stamping asymmetry**: fixed
  during Stage 1 for consistency (record only — no open work).
- **golden-registration bundling** (record only — no action): commit 827e6c2
  carries the bindingTableInjection golden's @Test registration that belonged
  with 050592d; history note.

# SharedService program backlog (recorded 2026-08-14, Task 8)

Source ranges: Kotlin 29b02c2b1ca3..41a6e1bcf4b7, roku-test-app
31f4dd0 + 9f25c92. Design of record:
`docs/superpowers/plans/2026-08-14-shared-service-design.md`; execution
ledger: `.superpowers/sdd/2026-08-14-shared-service/progress.md`. CLAUDE.md's
"SharedService" section carries the shipped laws, the device-pinned platform
facts, and the CANARY runbook.

## Deferred by design decision (pointer items)

- **Interface-typed receiver dispatch**: receivers statically typed as an
  interface still slot-dispatch through the wrapper slots (documented
  residual, design §4/§11 — the CANARY covers the day Roku breaks slots).
  Closing it means dispatcher enumeration over interface implementors, not
  just class descendants.
- **Multi-module dispatchers**: dispatcher enumeration is closed-world
  single-module; a separately-compiled module's shared subclass is invisible
  to the base's dispatcher (silently missing else-arm case). Promote to a FIR
  diagnostic when multi-module becomes real (same note as ScopeHandle `run{}`
  blocks).
- **Unpublish/retract API**: v1 = republish + node death (design §11). Add an
  explicit retraction only if screens need to empty a key without replacing
  it.
- **Component-input DX pair** (design decision 10, recorded adjacent):
  `createComponent<T> { field = v }` configure-lambda + `@SGRequired` inputs
  with a generated `onInputsReady()` hook. Interim idiom (`@SGNodeField` +
  `@BrsOnChange`, acquire in the onChange) documented in CLAUDE.md.
- **Accessor-slot treatment revisit**: non-trivial/open accessors emit
  `__get_X` statics + dispatchers today; revisit the shape if non-trivial
  accessors prove common in real VMs.
- **Consumer-side no-arg `isLive()`**: answers false in consumers (the stored
  publish-time handle loses GetRef capability crossing inside the SetRef
  graph — device-pinned, Suite 9 runs 6-8; `isLive(node)` is THE consumer
  form). Revisit only if a usable cross-graph identity/handle API appears;
  the detach-vs-capability discriminator probe is named spike bait in
  SharedService.kt.
- **Canary retirement runbook**: pointer only — CLAUDE.md "SharedService"
  CANARY subsection (sharedCanaryFnSlot RED + tests 8-12 green → wrapper-slot
  residual paths are dead → schedule removal, retire the canary; never "fix"
  the test).

## Compiler/FIR residuals

- **`top`-read-inside-lambda codegen hole** (discovered in Suite 9 fixture
  work; NOT SharedService-specific): a component `top` READ inside a lambda
  body emits a call to an uninstalled `__get_top_k_()` → "Member function not
  found" at runtime. The natural user shape `launch { shareOn(top, vm) }`
  crashes; workaround = hoist `val node = top` to method scope and capture
  that. The 2026-08-10 captured-self routing fixed top WRITES
  (`m.this_0.top.f = v`); the read path needs the same routing.
- **FIR rule-3 hardening** (BRS_SHARED_THROUGH_COPYING_CHANNEL): (a) add a
  fixture pinning a setField/callFunc call resolved through a SUBTYPE-typed
  receiver (the fake-override classId trap family — same class the onChange
  extractor fix closed); (b) the checker's arg extraction
  (`firstOrNull { param.name == "value"/"arg" }`) is vararg-fragile — KDoc'd
  caveat in FirBrsSharedCopyChannelChecker; harden if the binding surface
  grows vararg forms.
- **Shared golden coverage additions**: default-arg + Unit-returning
  (sub-shaped) dispatcher/wrapper cases — the current
  sharedEmission/sharedDispatch goldens pin the return-carrying function
  shapes. (Param-carrying wrapper forwarding is now pinned:
  sharedDataClass.kt's `scale(factor)`, Task 6 fix wave.)
- **buildSharedDispatcher leading-m guard**: internal-invariant failure
  throws a bare IllegalStateException; switch to `compilationException` for
  consistency with the backend's other invariant failures.

## Pre-existing (surfaced by this program, not caused by it)

- **`data class X : SharedService()` is PARTIALLY supported** until the
  pre-existing data-class ctor-chaining gap closes (broader than
  SharedService): data-class constructors do not chain the superclass ctor —
  no SharedService base-field init (`__sharedGen`/`__sharedKey`/`__sharedNode`
  start undefined, so `isLive()` semantics are unreliable), no base `__proto`
  entry (runtime `is SharedService` answers false), and body-property
  initializers are skipped (the known data/enum init-block defect). Call-site
  SHAPES are correct and golden-pinned (`sharedDataClass.kt`, Task 6 fix wave
  C1: generated-member names slot-dispatch, hand-written members static) —
  do NOT read that golden as full support.
- **Data-class emission skip vs attachment skip mismatch**: RESOLVED in the
  Task 6 fix wave — both skips (and the shared-dispatch exclusion) now share
  one digit-checked predicate, `isDataClassGeneratedMemberName`
  (BrsSharedDispatchLowering.kt); a hand-written `componentFoo()` is emitted
  and attached like any ordinary member.
