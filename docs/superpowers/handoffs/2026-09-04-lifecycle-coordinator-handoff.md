# Coordinator Handoff — Component Lifecycle Program (spec 1: plans A + B)

**Date:** 2026-09-04
**For:** a fresh coordinator session EXECUTING the component-lifecycle program. The design and both implementation plans are written, reviewed with Mike, and committed; your job is execution, not re-design.
**Read order:** this file → `CLAUDE.md` (the law) → the memory directory (auto-loads) → `docs/superpowers/plans/2026-09-04-component-lifecycle-design.md` (the spec) → `docs/superpowers/plans/2026-09-04-component-lifecycle-core.md` (plan A — execute first) → `docs/superpowers/plans/2026-09-04-component-constructor-inputs.md` (plan B — after A ships). The previous coordinator handoff (`2026-08-15-coordinator-handoff.md`) still holds for everything not restated here; its operating manual is summarized in §3.

---

## 1. State of the world

| Repo | Branch | HEAD at handoff | Notes |
|---|---|---|---|
| Kotlin fork | `feature/brightscript-backend-2.2.20` | `978a67c5ca39` | 9 commits AHEAD of origin (all docs) — NOT pushed; push only on Mike's word |
| roku-test-app | `main` | `e690c2a` | working tree has Mike's uncommitted experiment in `src/brsMain/.../TestScreenVM.kt` (an unused `MessageHandler(RoMessagePort.create())` line in `load()` + an import reshuffle) — LEAVE IT, never commit it, stage explicit paths only |
| kotlin-roku | `main` | `30d47e9` | clean |

**Gate baseline (from CLAUDE.md, 2026-09-04, single-BRS-compilation close):** goldens **100** · FIR **232** · stdlib device **610 / 62** (the 4-test `coroutineFieldShadowingTests` suite is listed as "device run pending" — a fresh stdlib run early in Task 5 settles it) · E2E **104 active / 10 suites** (+3 red-guarded xtests) · `validateComponentIncludes` + test variant **strict 0/0**. Nothing in the tree has moved since those numbers except docs.

**Committed this session (all `docs:`):** the spec (`7a096e535abd`) plus five fact-fold commits as Mike downloaded Roku pages (`c79421b9ec0c`, `be067abc896d`, `6e51141cc5b6`, `757173468185`), plan A (`7c4b4ef6a656`), plan B (`21fcd774c8a5`), and two plan corrections (`84787069f150`, `978a67c5ca39`: separate `brs-prebuilt:` commits; `test-infra:` prefix for the test app).

**Roku docs now local in `../RokuDocs/` (HTML, extract with a tag-stripper — see the spec §2 for what each pins):** `Node.html` (the `change` field), `roSGNode.html`, `ifSGNodeChildren.html`, `ifSGNodeDict.html` (callFunc, **hasFunc**), `ifSGNodeField.html`, `ifSGNodeFocus.html`, `ifSGNodeBoundingRect.html`, `ifSGNodeHttpAgentAccess.html`, `roSGNodeEvent.html`, `roSGScreenEvent.html`, `roHttpAgent.html`, `ifHttpAgent.html`, `Component initialization order.html`, `Creating custom components.html`, `SceneGraph compilation.html`. The pages say appending `.md` to a docs URL yields Markdown and `https://developer.roku.com/dev/llms.txt` is an index — untested against the bot block CLAUDE.md records; ask Mike to download, never fetch.

## 2. What this program is, in one screen

Components get a gated `suspend onStart()` (fires once per activation after init, inputs, and declared dependencies), a plain `onStop()`, and an explicit REVERSIBLE `retire(node)`/`revive(node)` cycle over `callFunc`. Constructor parameters carrying `@SG*Field` are REQUIRED INPUTS written by a lowered constructor call (`Screen("123")`) or by constants in a plugin-generated typed layout builder; a FIR rule bans reading them in `init`. Two latent compiler defects are fixed en route (component `super.f()` self-recursion; the leaf `onKeyEvent` wrapper shadowing an inherited override), and the pump attach becomes an unconditional lifecycle attach. Spec 2 (scoped SharedService lookup: walking `sharedFrom`, suspend `awaitShared`, `serviceKey` as a constructor val, `by sharedService { key }` registering into the gate) is NOT written yet and comes after both plans ship.

**THE DECISION REVERSAL — read before anything else.** The 2026-08-15 handoff says constructor-parameter SG inputs were "REJECTED on principle — do not re-litigate" (SharedService design decision 10). Mike CONSCIOUSLY REVERSED that on 2026-09-04 (spec §1 decision 3): the objection was the init-time-availability PROMISE the syntax makes; the init-read FIR rule plus the ready gate make the promise a checked contract. Do not fight the plans on this; the design doc records the reasoning and plan B's Task 8 annotates the old decision row as superseded.

Other rulings already made (memory `project_component_lifecycle_program.md` has them all): hooks are `onStart`/`onStop`, calls are `retire`/`revive` (not onReady/onDestroy); the driver is compiler-synthesized because the stdlib has no state machines; explicit retire is the contract and parent-`change`-field detach detection is only a spike-gated backstop (Probe A4 gives it a strong negative prior); predicate-based `sharedFrom` lookup was DROPPED for keys; `serviceKey` will be a constructor val, not an open property (stdlib can't dispatch an open member: zero-rung dispatcher / slot path slated for removal).

## 3. Operating manual (unchanged from 2026-08-15 unless marked NEW)

- **Models:** `model: "fable"` for EVERY subagent, implementers and reviewers. Sonnet is banned here. Spurious "out of credits" deaths: resume the same agent, never downgrade.
- **Process:** `superpowers:subagent-driven-development` per plan (ledger in `.superpowers/sdd/<plan>/`), ONE implementer at a time (shared build dirs + single device console), fresh fable reviewer per task, red-first goldens (the plans capture the buggy output with `--update` BEFORE the fix — keep that), scoped re-reviews per fix round. Rulings not stalls: ledger `Ruling: what — why — cost if wrong`; surface all rulings to Mike at the end.
- **Wakeups & waiting:** this machine kills background wakeups and blocks `sleep N; cmd` compounds. Long commands: `nohup caffeinate -ims <cmd> > <scratchpad>/log 2>&1 &` + a Monitor until-loop gated on the PID exiting AND a completion/failure marker; arm a reconciliation timer per long dispatch; tell implementers verbatim never to end a turn mid-build. No no-op keep-alive Bash calls while waiting (Mike flagged it twice).
- **Device:** single-client console (8085) — preflight EVERY run with `echo | nc -w 3 $IP 8085`; "already in use" = stop and ask Mike (usually his IDE), never kill it. Runners: `./run-stdlib-tests.sh`, rta `./run-device-tests.sh` (never bare `./gradlew rokuTest`), spike `./deploy.sh`. Credentials resolve from `roku-test-app/local.properties`.
- **Builds:** `./rebuild.sh` only (Kotlin); rta `./rebuild-all.sh --all` (or `--plugin` after kotlin-roku changes — the plans say when); the two FIR regen commands and the documented golden/diagnostic test commands are the only exceptions. Infra failure = fix the tooling.
- **Commits (NEW table — the plans already use these):** Kotlin `spike:`/`spikes:`/`stdlib:`/`brs:`/`fir:`/`docs:`/`test-infra:`; every stdlib SOURCE change pairs with a SEPARATE `brs-prebuilt: regenerate stdlib klib (<reason>)` commit; roku-test-app `test-infra:` (suites/fixtures) and `app:` (demo code); kotlin-roku `plugin:`. All end `Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>`. Push ONLY when Mike says "push".
- **Platform truth:** the device is the only authority; spike before building on an unpinned corner. Plan A Task 1 IS that spike — run it first, stamp the spec §8, and STOP to re-plan if Q6a/Q6c (render→render `callFunc`, self-`callFunc`) FAIL: Task 6's hop needs the §12 fallback.
- **Mike's preferences:** self-contained questions (premise + parity comparison in the question); spec deviations surfaced prominently; plain outcome reporting. He reads the design doc's decision table as the contract.

## 4. Execution order and first moves

1. **Sanity:** `git status` in all three repos matches §1; `lsof -nP -iTCP | grep 8085` is empty (or ask Mike).
2. **Plan A, Task 1 (spike):** build the raw-BRS package exactly as written, `./deploy.sh`, write `spikes/lifecycle-spike/FINDINGS.md`, stamp spec §8 EXECUTED, two commits. Read the verdicts against the expectations table in the package README: Q1/Q2/Q3 PASS confirm the docs; Q6a/Q6b/Q6c PASS are load-bearing for retire/revive; Q7a/Q7b FAIL is the expected A4 silence.
3. **Plan A, Tasks 2–10** in order via SDD. Watch the known-fragile points: Task 3's include-validator change lives in kotlin-roku (republish with `--plugin`); Task 7's IR synthesis (`addExtensionReceiver`, `suspendFunctionN`, `IrAnonymousInitializerImpl` constructor shapes — the plan names the fallbacks); Task 8's `BrsTry`/print AST shapes (mirror `generateTaskMainFunction`); Task 9's 1000-instance timing line goes into the commit message and CLAUDE.md.
4. **Plan B, Tasks 1–8** via SDD. FIR regen is manual (two gradlew commands); the harness compares diagnostic NAMES only and needs the inverse-message entries; step 8/9 of `./rebuild.sh` may fire the new rules on kotlin-test/flow source — `@Suppress` at the declaration site.
5. **Then** brainstorm spec 2 with Mike (architectural path; the spike for `getParent()` of a Scene's direct child is already in plan A's package as Q5b, and the walk-from-`init`-misses fact is documented).

## 5. Watchpoints and known unknowns

- `hasFunc` has no OS-version note on its doc page; the compile floor is 9.4, the device is 15.3.4. Plan A Task 6's guard depends on it; the spike pins it on the device only.
- XML `<children>` attribute timing (spike Q4): if attributes ARE visible in a child's init, init step 4 clobbers layout-declared values for BODY properties — a pre-existing bug to record in plan B's docs task, not a blocker.
- The `super` static call deliberately does NOT record a dependency for a USER base component (recording would pull the base script into the leaf XML: duplicate `sub init()`); the include validator therefore learns the extends chain (plan A Task 3 step 4). If strict validation still flags something after that, the finding is real — investigate, don't allowlist.
- `exposeScope` becomes re-callable after retire (one OPEN host) — a ScopeHandle law amendment; Suite 8 (30 tests) must stay green and gains two tests.
- Retire from the driver (main thread) relies on `callFunc` rendezvous into the child's context; the plan's Suite 11 tests do this deliberately. If a main-thread `retire` misbehaves while a render-thread one works, that is a finding for the spec, not a test bug.
- Plan B's `FirBrsTaskStateNotFieldChecker` skip removal makes a plain constructor `val` on a task component an error — check `./rebuild.sh` step 8/9 output for stdlib/kotlin-test code that had one.

## 6. Key file map

| What | Where |
|---|---|
| The law | Kotlin `CLAUDE.md` (lifecycle sections land in plan A Task 10 / plan B Task 8) |
| Spec (decision table = contract) | `docs/superpowers/plans/2026-09-04-component-lifecycle-design.md` |
| Plan A / Plan B | `docs/superpowers/plans/2026-09-04-component-lifecycle-core.md` / `2026-09-04-component-constructor-inputs.md` |
| Spike template | `spikes/flow-spike/probe-a/` (deploy.sh, README verdict contract, BellScene stage machine) |
| Runtime precedents the stdlib work mirrors | `libraries/stdlib/brs/src/kotlin/brs/scope/ComponentMailbox.kt` (registry + watchdog), `libraries/flow/brs/src/kotlin/coroutines/flow/Doorbells.kt` (name-registered observer, same-file law), `libraries/stdlib/brs/src/kotlin/coroutines/ParkedContinuation.kt` (single-park idiom), `libraries/stdlib/brs/src/kotlin/brs/ComponentCoroutines.kt` (scope holder) |
| Compiler precedents | `lower/BrsRunTaskCallLowering.kt` (call-site rewrite), `lower/BrsFlowTaskLiftLowering.kt` (IR function synthesis), `lower/BrsSharedDispatchLowering.kt` (super fix template), `IrToBrsTransformer.generateOnKeyEventFunction` / `generateTaskMainFunction` (bare-named entries) |
| FIR precedents | `checkers.brs/.../FirBrsCreateComponentTypeChecker.kt`, `FirBrsTaskStateNotFieldChecker.kt`, upstream `FirUninitializedEnumChecker.kt` (init-context classification) |
| Plugin | `../kotlin-roku/src/main/kotlin/com/example/roku/gradle/tasks/GenerateLayoutStubsTask.kt`, `ComponentIncludeValidator.kt` |
| E2E precedents | rta `tests/ScopeHandleTests.kt` (nodeOf bridge, owner+child install), `tests/FlowTests.kt` (extras + poll awaits), fixtures `FlowOwnerProbe.kt`, `TypedTaskProbe.kt` (cancelawait shape), `SleepTask.kt` |
| Memory | `~/.claude/projects/-Users-Mike-Fougere-Documents-newt-git-Kotlin/memory/project_component_lifecycle_program.md` |

**Suggested first move:** run plan A Task 1 (the spike) on the device before anything else — it is the only step whose result can change the plan.
