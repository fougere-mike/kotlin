# Coordinator Handoff — BRS Backend Program Series

**Date:** 2026-08-15
**For:** a fresh coordinator session taking on the next phases of the Kotlin→BrightScript backend roadmap.
**Read order:** this file → repo `CLAUDE.md` (the law — every section is device-pinned) → the memory directory auto-loads with your session → then the specific spec/plan/backlog files referenced below as needed.

---

## 1. State of the world (all pushed, all green)

| Repo | Branch | HEAD | Remote |
|---|---|---|---|
| Kotlin fork | `feature/brightscript-backend-2.2.20` | `e760c30a0772` | `fougere-mike/Kotlin` (remote prints a moved-to-`fougere-mike/kotlin` hint; pushes redirect fine — updating the remote URL would silence it) |
| roku-test-app | `main` | `1b1c627` | `fougere-mike/roku-test-app` |

**Gate table (must not drop; grows with every program):**
goldens **79** · FIR diagnostics **227** · stdlib device **524 tests / 53 suites** · E2E **86 active / 9 suites** (+3 red-guarded xtests) · `validateComponentIncludes` + `validateTestComponentIncludes` **strict 0/0**. Counting caveats live in CLAUDE.md's gate-table notes. `validateComponentIncludes` (main package) is NOT wired into `rokuTest` — run it standalone (`./gradlew validateComponentIncludes`) or it silently goes unchecked in sweeps.

**Recently shipped (2026-08-14/15, in order):**
1. **ScopeHandle** (cross-component scope borrowing) — CLAUDE.md section is the law.
2. **SharedService** (reference-shared classes + static dispatch) — spec `docs/superpowers/plans/2026-08-14-shared-service-design.md`, plan `2026-08-14-shared-service.md`. Two device-forced spec deviations shipped: generation-stamp liveness (IsSameObject is device-false for nested stash entries) and the `isLive(node)` overload (SetRef-crossed node handles lose GetRef capability). Two post-close fix waves: shared data-class generated members (call sites AND dispatcher rungs) excluded from static dispatch by name; 556 phantom manifest entries removed.
3. **top/global/m/layout-in-lambda fix** — `launch { shareOn(top, vm) }` works as written now; fixture hoists retired; the receiver-blind scope-property gate tightened.
4. **Include-closure Pass 3 fix** — per-component deps.json was order-dependent (files transformed after a component read as leaves); now generated after all transforms. Pinned by the `projectHelperTransitiveDeps` multi-file golden.
5. **TestScreen modernization** (roku-test-app) — the living demo of the whole modern stack: `TestScreenVM : ViewModel : SharedService`, `suspend load()` running two concurrent `runTask<FakeApiTask>` calls via `coroutineScope`/`async`/`awaitAll`, state piped to UI via `@SGStringField` + `@BrsOnChange`. Read `components/TestScreen/` as the canonical screen shape before designing anything VM-adjacent.

## 2. Operating manual (hard-won on THIS machine — do not relearn these)

**Models.** `model: "fable"` for EVERY subagent — implementers AND reviewers. Sonnet is banned in this codebase (Mike, explicit; a fable adversarial pass twice caught defects sonnet reviews had approved — see `feedback_no_sonnet_subagents.md` in memory). The "out of usage credits" API death is SPURIOUS on Mike's account: never downgrade — SendMessage-resume the same agent (transcript + working tree carry its state); only persistent failure is a stop-and-ask.

**Wakeups & waiting.** This machine kills background wakeups AND the harness blocks `sleep N; cmd` compound polls. The working pattern:
- Long commands: `nohup caffeinate -ims <cmd> > <scratchpad>/log 2>&1 &`, then a **Monitor** with an until-loop keyed on completion AND failure markers, gated on the PID actually exiting (`until grep -qE "BUILD FAILED|=== Build complete ===" $LOG && ! kill -0 $PID; do sleep 30; done`).
- Arm a reconciliation timer (Monitor one-shot `sleep N; echo reconcile...`) for every long dispatch; lost wakeups happened repeatedly — check log mtime/tail, then nudge the agent.
- Tell every implementer verbatim: never end a turn mid-build/run; never rely on background wakeups; use foreground until-loop waits inside single Bash calls.
- Do NOT emit no-op keep-alive Bash calls while waiting — end the turn with text; messages/monitors wake you. (Mike flagged the no-op spam twice.)

**Device.** Single-client console (port 8085): preflight EVERY device run with `echo | nc -w 3 $IP 8085` — output containing "Console connection is already in use" = STOP and ask Mike (it's usually his IDE). A replayed backlog line is normal and fine. Credentials auto-resolve from `roku-test-app/local.properties`. Runners: `./run-stdlib-tests.sh` (Kotlin repo), `./run-device-tests.sh` wrapper (rta — NEVER bare `./gradlew rokuTest`).

**Builds.** `./rebuild.sh` only (Kotlin); `./rebuild-all.sh --all` (rta). The two FIR-regen commands and the documented test commands are the only exceptions. Infra failure = fix the tooling, never work around.

**Commits.** Per-task on the feature branch; prefixes `stdlib:`/`brs:`/`test-infra:`/`docs:` (rta demo code used `app:`); every stdlib SOURCE change pairs with a separate `brs-prebuilt: regenerate stdlib klib (<reason>)` commit (verify byte-change via git status — compiler-only changes usually don't regen); ALL messages end `Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>`. rta is a separate repo, committed separately, EXPLICIT paths only — the pre-existing `components/ShelfView/ShelfView.kt` +1-blank-line working-tree edit is Mike's; leave it uncommitted forever. Push ONLY on Mike's word (he answers "push" when he wants it).

**Process that worked (use it).** superpowers flow: `brainstorming` (classify honestly; the ratchet upgraded mid-task twice this session — say so and stop when it does) → `writing-plans` (plans must be FRESH-SESSION-SELF-CONTAINED with the env procedure baked into Global Constraints — `docs/superpowers/plans/2026-08-14-shared-service.md` is the template) → `subagent-driven-development` (ledger in `.superpowers/sdd/<plan>/`, one implementer at a time — shared build dirs + single console — fresh fable reviewer per task with diff packages via the skill's scripts, red-first goldens, scoped re-reviews per fix round). Rulings not stalls: decide, ledger `Ruling: what — why — cost if wrong`, surface ALL rulings to Mike at the end. Adversarial fable re-verification of anything a weaker gate approved paid for itself twice.

**Platform truth discipline.** The device is the only authority; three spec assumptions were device-falsified in one program (GetRef inserts copy while reads are live; nested-entry IsSameObject false; SetRef-crossed node handles lose GetRef capability). When a design leans on an unpinned platform corner, spike it BEFORE building on it — a 20-minute probe run beats two blind fix waves (learned the hard way). Date device facts in comments; name un-run discriminator probes as spike bait.

**Mike's preferences.** Self-contained design questions carrying premise + kotlinx-parity comparison in the question itself (he rejected context-free options twice — memory `feedback_self_contained_questions.md`). Spec deviations surfaced prominently, never buried. Report outcomes plainly; no hedging.

## 3. Next phases (the queue)

Recorded next programs (spec §11 of the SharedService design + ScopeHandle records). Each needs its own brainstorm → spec → plan cycle with Mike before execution:

1. **StateFlow / VM→View propagation** — the designated next program. Foundations now in place: SharedService base-class hierarchies (VMs are real shared objects), the ScopeHandle kind-tagged-envelope mailbox substrate (built deliberately StateFlow-forward-compatible), and the @SG-field piping idiom TestScreen demonstrates manually. Open design surface: the propagation carrier (typed @SG fields vs the mailbox), collection semantics on the render thread (component `launch {}` collectors?), lifecycle/teardown (close-is-terminal convention), and how `screenState`-style sealed states flow without violating the marshallable-set laws (SharedService instances are compile-errors in copying channels — propagation must ride references or plain data).
2. **@AppScoped DI** — separate program; the scene-stash idiom (`shareOn(scene, service)` / `sharedFrom<T>(getScene())`, documented in CLAUDE.md + TestScreen) is its manual precursor; the program mints those calls.
3. **spawnTask {}** — explicit task-thread block spawner (compiler-assisted lambda lift + synthesized task component, captures BY COPY, TaskException rethrow at the suspend point). Supersedes the quarantined `withContext(Dispatchers.IO)` re-layering. Recorded at ScopeHandle close.
4. **Component-input DX pair** (SharedService decision 10): `createComponent<T> { field = v }` configure-lambda + `@SGRequired` inputs with generated `onInputsReady()`. Constructor-parameter syntax for SG inputs was REJECTED on principle (no creation-time argument channel on the platform) — do not re-litigate.
5. **Backlog sweeps** (fold into whichever program touches the area; the file is `docs/superpowers/backlog-typed-task-program.md`):
   - Include-closure: the binding-table mid-Pass-2 closure is the LAST order-dependent consumer of the deps graph (subset of Pass 3 — packaging-safe, second-hop `run{}` blocks get the guided dispatch-miss; fix = Pass-3 backfill of the retained table literal or a pre-pass dep scan). Plus the multi-file golden's freeArgs-order hardening.
   - Data-class family: `data class X : SharedService()` only PARTIALLY supported (pre-existing ctor non-chaining — no base init, no base `__proto` entry); hand-written equals/hashCode/toString BODIES silently replaced in ALL data classes; non-data render-vs-attachment inconsistency for component-prefixed names; `generateDataClassHashCode` Int+String `+` latent bug.
   - SharedService residuals: interface-typed receiver dispatch; multi-module dispatchers → FIR diagnostic when multi-module becomes real; unpublish/retract API; accessor-slot treatment revisit; consumer no-arg `isLive()` revisit if a usable cross-graph identity API appears; canary-retirement runbook (CLAUDE.md).
   - FIR rule-3 hardening: subtype-typed receiver fixture pin (fake-override classId trap family); `firstOrNull` arg-extraction vararg fragility; `sharedAcquire` reserved-prefix message could name the caller's fix (M1 nit).
   - Suite 6 could gain a device test for layout-in-lambda reads and the un-hijacked `top`-named-property emission (golden-only today).

**Watchpoints (not work items):** the fn-slot CANARY going red with tests 8-12 green means Roku changed the disclaimed behavior — the runbook is in CLAUDE.md's SharedService section; do NOT "fix" the canary. Tests 8/11/12 of Suite 9 catch a correct-but-slot regression via include-closure collapse, not shape assertions — the shape pins are the goldens.

## 4. Key file map

| What | Where |
|---|---|
| The law (all patterns, device-pinned) | Kotlin repo `CLAUDE.md` |
| SharedService spec + plan (the program template) | `docs/superpowers/plans/2026-08-14-shared-service{-design,}.md` |
| ScopeHandle design + spike truth | `docs/superpowers/plans/2026-08-12-scopehandle-design.md`, `spikes/scope-handle-spike/FINDINGS.md` |
| Backlog (all programs) | `docs/superpowers/backlog-typed-task-program.md` |
| Canonical modern screen | rta `components/TestScreen/` (VM+component+task) |
| Canonical fixtures (scope/shared/task) | rta `components/fixtures/`, suites in rta `src/brsTest/kotlin/tests/` |
| Session memory (auto-loads) | `~/.claude/projects/-Users-Mike-Fougere-Documents-newt-git-Kotlin/memory/` |

**Suggested first move for the new coordinator:** confirm the gate baseline with a fresh sweep only if the tree has moved since `e760c30a0772`/`1b1c627`; otherwise start the StateFlow brainstorm with Mike — classification will be architectural, and the spike-before-design rule applies to any propagation-carrier assumption not already device-pinned.
