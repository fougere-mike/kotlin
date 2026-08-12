# Handoff: ScopeHandle (cross-component scope borrowing) — context + decision for planning

**Date:** 2026-08-12
**Status:** Direction decided with Mike; needs brainstorm-to-spec-to-plan treatment.
**Decision of record:** "World 2 + (A)" — an explicit post/await ScopeHandle primitive, with owner-death delivered as a `CancellationException` subtype. Details below.

## 1. Why this document exists

An app-architecture track (MVVM, DI, plain-class ViewModels shared across SceneGraph
components) converged on needing ONE new runtime primitive: a child component must be
able to await work that genuinely executes in another (owner) component's coroutine
scope. An earlier handoff framed the options while the coroutine runtime still lacked
cancellation; that premise is obsolete. This document replaces it with current facts.

Read first:
- `CLAUDE.md` — "Component Coroutine Scaffolding" and "Coroutine Utilities (awaitAll &
  friends)" sections are CURRENT (updated at the end of the utilities program).
- Spec + plan of the completed runtime program:
  `docs/superpowers/plans/2026-08-11-brs-coroutine-utilities-design.md` and
  `docs/superpowers/plans/2026-08-11-brs-coroutine-utilities.md`.
- Memory: `project_component_coroutine_scaffolding.md`,
  `project_brs_suspend_codegen_defects.md` (follow-up families at the bottom).

## 2. Current runtime state (shipped 2026-08-11, commits bc648ceb..542bd5be + roku-test-app fb8d1ad..6590580)

- Full Job completion protocol on `JobImpl`: `invokeOnCompletion` (public; fires once at
  terminal with cause — null/failure/CancellationException) + internal cancel-request
  handlers that resume PARKED continuations mid-park with CE.
- Real `join`/`await` (ParkedContinuation: once-guard, handle disposal at settle,
  interceptor resume, `parked.finish()` tail idiom), `awaitAll`/`joinAll`,
  `coroutineScope`/`withContext` on the shared `parkScopedBlock` scope-job engine,
  `withTimeout`/`withTimeoutOrNull` + `TimeoutCancellationException : CancellationException`,
  `delay`/`yield` cancellation (entry checks + mid-park wakeup), `SupervisorJob`,
  `ensureActive`/`isActive`.
- Hierarchy live: launch/async attach to the scope's job; child failure cancels
  non-supervisor parents; ALL THREE roots are supervisors (componentScope, runBlocking,
  kotlin.test runPumping — deliberate kotlinx divergence, documented).
- Failure delivery: awaiters receive the ORIGINAL exception (tryFinish fires own
  completion handlers BEFORE parent notify).
- Gates that must not drop: goldens 65, FIR 219, stdlib device 493/50, E2E 41/7 (+3
  xtests), validateComponentIncludes strict 0.
- Both pumping regimes device-verified (PumpScheduler component regime + runBlocking).

Known relevant gap: `runTask`'s `awaitCompletion` (TaskRunner.kt) predates the program
and was NOT retrofitted — a coroutine awaiting a task does not wake mid-park on
cancellation (it wakes when the task finishes, then hits the next entry check).
Retrofitting it onto ParkedContinuation + registerCallerCancel is a small task that
should probably ride ahead of ScopeHandle (shared idiom, makes withTimeout(runTask)
prompt on the waiting side; task-thread work itself still not cancellable — M3).

## 3. Load-bearing platform facts (all device-verified; do not re-litigate)

- `GetGlobalAA` is per-component-instance on the render thread. Every Kotlin `object`
  singleton is therefore per-component state (CoroutineQueue, DelayTracker,
  PumpScheduler, TaskRunner, Dispatchers included). Plain CLASS instances are ordinary
  heap objects shared by reference — a VM shared across components has one copy of its
  fields.
- Which queue a continuation lands in is decided by the AMBIENT component on the call
  stack at enqueue time, NOT by the CoroutineContext. Context determines interceptor
  identity only. Jobs are therefore same-component-only (documented v1 law in
  CLAUDE.md); a Job cancelled in X cannot reach a park registered in Y by traversal.
- roRenderThreadQueue (OS 15.0+): handlers run in the REGISTERING component's script
  context; channels app-global with per-instance ids; payload is MOVED; posts to
  UNREGISTERED/dead channels are silently DROPPED (no NACK — this kills any
  "assert on post to dead owner" design).
- SG field observers are the platform-native cross-component dispatch that works on the
  compile floor (OS 9.4): observer callbacks run in the OBSERVING component's context.
  The typed-task protocol (TaskRunner + kotlinTaskState/kotlinTaskId correlation ids)
  is exactly this shape, proven cross-THREAD.
- UNVERIFIED (spike required, gates the API shape): when component Y invokes a lambda
  OBJECT created by component X (same render thread, shared heap), does GetGlobalAA
  inside the lambda body resolve to Y (the invoker)? If yes, `owner.run { block }` can
  ship blocks by shared reference (message = doorbell + correlation only). If no, v1
  API becomes typed named requests (`owner.run(RefreshWatchlist)`), same semantics.

## 4. The decision (made with Mike, 2026-08-12)

**World 2:** build ScopeHandle — an explicit, call-site-visible post/await primitive.
A child suspends locally while the work executes on the owner's machinery. NEVER a
transparent withContext (the withContext(IO)/TaskPool pipeline is the cautionary
precedent: invisible context switching, shipped unverified, now a FIR error).

**Owner-death semantics = (A) with a subtype:** signaled owner teardown delivers
`ScopeClosedException : CancellationException` at the child's suspend point —
quiet-cancel semantics (uncaught = coroutine winds down; supervisor root keeps the
component alive) plus a diagnosable type. Precedent: TimeoutCancellationException.
Options (B)-as-non-CE (failure-propagation wart) and (C) owner-must-outlive
(unimplementable assert — dead channels drop posts silently) are rejected/superseded.

**Backstop for UNSIGNALED death** (owner node removed, no teardown ran): `withTimeout`
around ScopeHandle awaits, converting silent hangs into TimeoutCancellationException.
Documentation should recommend it; consider whether the API bakes in a default.

**Teardown convention required:** SceneGraph has no destruction callback. The
architecture must define an explicit owner teardown hook that calls
`ownerScope.cancel()` (which cascades to all in-flight request jobs — supervisor roots
still cascade DOWNWARD cancels — firing each request's invokeOnCompletion with CE).

## 5. Mechanism sketch (constraints for the spec, not final design)

Two-hop correlation-id protocol; mirror jobs; NO Job object ever crosses components:

```
child:  suspend fn → ParkedContinuation in CHILD registry keyed by requestId
                   → registerCallerCancel (child's own cancel wakes park locally, free)
                   → post {requestId, request} to owner mailbox
owner:  handler (runs in OWNER context) → launch request job as CHILD of owner scope
                   → job.invokeOnCompletion { outcome → post {requestId, outcome} back }
                     (single egress: fires for value, failure, AND cancellation/CE)
child:  handler (runs in CHILD context) → registry lookup → parked.tryResume/-Exception
```

- Child-side cancel: park wakes locally with CE immediately; additionally post
  {requestId, cancel} so the owner cancels the request job (best-effort; late replies
  hit registry-miss/once-guard and drop — TaskRunner precedent).
- Failures marshal TaskException-style (message/number/backtrace AA) — never move
  exception objects. Deliver the ORIGINAL failure info (runtime already guarantees
  this locally; the protocol must preserve it across the hop).
- Carrier is INVISIBLE in user code. Two candidates: (1) shared-node @SG field mailbox —
  works on OS 9.4+, the proven typed-task shape; (2) roRenderThreadQueue — OS 15+,
  lower latency. Recommendation to evaluate: field-mailbox first, RTQ as detected fast
  path (mirrors PumpScheduler's backend pattern). Note kotlin-roku plugin sentinel
  nonce and arming-order rules if node fields are used (observer armed before write).
- Owner-side handler code must not hit the nested-launch DX trap (`launch` in component
  files binds to ComponentBase.launch — see CLAUDE.md DX traps); use the owner's scope
  explicitly.
- Do not reuse the pump's RTQ channel; register a dedicated per-instance channel;
  registration strictly before first post.

## 6. User-facing shape agreed (illustrative names)

```kotlin
// Child component — the only new call-site concept:
launch {
    try {
        withTimeout(10_000) {                 // backstop: unsignaled owner death
            owner.run { vm.refresh() }        // suspends here; executes on owner
        }
        render(vm.items)
    } catch (e: TimeoutCancellationException) {
        showToast("unavailable")
    }
    // Signaled teardown (ownerScope.cancel()) = ScopeClosedException (a CE):
    // typically NOT caught — the coroutine ends quietly, component unaffected.
}
```

Caller-scope execution (child calls a shared VM's suspend fn directly, work services
in the child) remains legal and correct for non-shared work; ScopeHandle is for
shared-VM mutations that must centralize or outlive the caller. The "owner-only
discipline" alternative (children may only signal via fields) is REJECTED — it forbids
awaiting/catching and merely relocates the owner-death hang.

## 7. Adjacent items (separate scope — do not fold into ScopeHandle, but coordinate)

- **TaskRunner awaitCompletion retrofit** (see §2 gap): small; recommend before or
  alongside ScopeHandle phase 1.
- **VM→View state propagation:** undecided. Known trap: a plain subscriber-callback
  list on a shared VM runs the subscriber's code on the SETTER's component (ambient
  rule). SG field observation is the native correct carrier (observer runs in observing
  component). Within-component StateFlow is now buildable on the shipped runtime and
  Mike prefers "build the real thing once" — treat as its own brainstorm/program.
- **Identity divergence (DI):** `object` singletons silently per-component; needs a
  global-node-backed @AppScoped registry (precedent: PumpScheduler's backend cache on
  the global node). Different problem from scope; different program.
- **Backlog families from the runtime program's final review** (in memory file):
  (a) silent-drop: try/finally FIR stopgap FIRST (non-suspend visitTry drops finally —
  and ScopeHandle docs will encourage try/catch patterns), data/enum init blocks,
  brs() statement-splice, native-callee/@BrsInline absent-arg guard, primitive varargs;
  (b) scope-guards: attachChild terminal-parent check + leaked-receiver and
  nested-launch FIR warnings; (c) infra: kotlin-roku sentinel nonce, host-side BRS
  syntax lint, golden-gap batch.

## 8. Open questions for the planning conversation

1. Spike result (§3 last bullet) → `owner.run { block }` vs typed named requests.
2. Carrier: field-mailbox-first + RTQ fast path, or RTQ-only (raises effective floor)?
3. How does a child obtain a ScopeHandle (DI? node field? registry)? Interacts with the
   @AppScoped design but must not wait for it.
4. Single-flight/dedup semantics on the owner side — in scope for v1 or caller's problem?
5. The owner teardown hook: what convention (explicit close() on scene exit? tied to
   an existing lifecycle signal?) — needed for (A) to ever fire.
6. Does `withTimeout` backstop get baked into the API (default timeout) or stay a
   documented pattern?
7. Where does ScopeHandle live: stdlib (kotlin.brs?) vs app-layer library? (Uses only
   public runtime primitives by design — invokeOnCompletion, ParkedContinuation idiom
   would need exposure or an internal home.)

## 9. Process expectations (same as the completed program)

Brainstorm → spec → plan → subagent execution with per-task review; `./rebuild.sh` is
the only build command; device gates at every phase (spike results device-verified
before the API is fixed); commit per phase on `feature/brightscript-backend-2.2.20`;
gates in §2 must not drop. Device: ROKU_DEVICE_IP=192.168.1.125, console preflight
`echo | nc -w 3 192.168.1.125 8085` (single-client console).
