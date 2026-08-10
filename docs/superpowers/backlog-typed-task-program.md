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
