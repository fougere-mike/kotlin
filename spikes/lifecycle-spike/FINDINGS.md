# Component-lifecycle spike findings (Phase 0 of the component-lifecycle program)

**Date:** 2026-09-05
**Device:** Roku Ultra 4800X, OS 15.3.4 build 2402 (`192.168.1.125`) — from the run's
`[SPIKE] BEGIN probe-init model=4800X os=15.3.4 build=2402` line.
**Design under test:** `docs/superpowers/plans/2026-09-04-component-lifecycle-design.md` §8
(questions 1–7).
**Raw evidence:** `capture-probe-init.txt` (sentinel-filtered, run-attributable slice; any
later run lands as `capture-probe-init-runN.txt`). Verdict contract and per-question
mapping: `probe-init/README.md`. Package: `probe-init/` (raw BrightScript, flow-spike
harness lineage; `deploy.sh`).

**Outcome: 10 PASS / 0 FAIL; no load-bearing §2 fact was falsified.** Q1–Q4 and Q6
confirm the documented init-order and callFunc facts on device. Q7 — the detach probe the
design expected to come back NEGATIVE (§8 "expected negative per A4") — came back
POSITIVE on both observer forms, and exposed a second fact the A4 summary in §2 did not
carry: a removed component whose node is still referenced keeps observing. Facts only
below; the design decisions those facts open are recorded in the spec, not here.

## Verdict lines (verbatim)

```
[SPIKE] init.q1.childInitBeforeParentInit PASS log=child-init tag=default parent=invalid scene=valid;parent-init childFound=valid childTag=fromXml;
[SPIKE] init.q2.xmlChildParentInvalidInInit PASS log=child-init tag=default parent=invalid scene=valid;parent-init childFound=valid childTag=fromXml;
[SPIKE] init.q4.xmlAttrNotYetAppliedInChildInit PASS log=child-init tag=default parent=invalid scene=valid;parent-init childFound=valid childTag=fromXml;
[SPIKE] init.q3.createObjectParentInvalidInInit PASS log=child-init tag=default parent=invalid scene=valid;
[SPIKE] init.q6a.callFuncRenderToRender PASS result=pong:LcChild
[SPIKE] init.q6b.hasFuncTrueOnComponent PASS 
[SPIKE] init.q6b.hasFuncFalseOnPlainNode PASS 
[SPIKE] init.q6c.selfCallFunc PASS result=self:pong:LcChild
[SPIKE] init.q7a.scopedChangeFiresOnOwnRemoval PASS watch=scoped:remove:invalid;plain:remove:valid;scoped:remove:valid;scoped:remove:invalid;plain:remove:invalid;scoped:remove:valid;scoped:remove:invalid;plain:remove:invalid;scoped:remove:invalid;
[SPIKE] init.q7b.plainChangeFiresOnOwnRemoval PASS watch=scoped:remove:invalid;plain:remove:valid;scoped:remove:valid;scoped:remove:invalid;plain:remove:invalid;scoped:remove:valid;scoped:remove:invalid;plain:remove:invalid;scoped:remove:invalid;
```

## INFORMATIVE lines (verbatim)

```
[SPIKE] init.q4.evidence INFORMATIVE tagAfterCreate=fromXml
[SPIKE] init.q5b.sceneChildParent INFORMATIVE parentIsScene=true parentSubtype=InitScene
[SPIKE] init.q5.getSceneInUnattachedInit INFORMATIVE log=child-init tag=default parent=invalid scene=valid;
[SPIKE] init.q3b.createChildParentAtInit INFORMATIVE log=child-init tag=default parent=invalid scene=valid;
[SPIKE] init.q6.evidence INFORMATIVE threadinfo={"currentThread":{"id":"A2E878","name":"InitScene","type":"Render"},"node":{"address":"9FF6AD","id":"","owningThread":{"id":"A2E878","name":"InitScene","type":"Render"},"type":"LcChild","willRendezvousFromCurrentThread":"No"},"renderThread":{"id":"A2E878","name":"InitScene","type":"Render"}}
[SPIKE] init.q7.evidence INFORMATIVE armed=armScoped:armed;scoped:add:valid;armPlain:armed;scoped:add:valid;plain:add:valid;armScoped:armed;
[SPIKE] init.q7c.reparentRecorded INFORMATIVE watch=scoped:remove:invalid;plain:remove:valid;scoped:remove:valid;scoped:remove:invalid;plain:remove:invalid;scoped:remove:valid;scoped:remove:invalid;plain:remove:invalid;scoped:remove:invalid;
```

## Facts of record

### Q1 — child-before-parent init (XML-declared child)

- The XML-declared child's `init()` log entry (`child-init …`) precedes the parent's
  (`parent-init …`) in the global log written from both inits, inside one
  `CreateObject("roSGNode", "LcParent")` call (`init.q1 PASS`).
- Inside the PARENT's init, `findNode("xmlChild")` already finds the child
  (`childFound=valid`) and reads the markup attribute value (`childTag=fromXml`).

**Design consequence:** confirms §2 (third docs batch) "children are fully created BEFORE
the parent's init" on device.

### Q2 — `getParent()` inside the XML-declared child's init

- `m.top.getParent()` was `invalid` inside the XML child's `init()` (`parent=invalid`,
  `init.q2 PASS`), while the same node's parent was set by the time the parent's init
  ran (Q1's `childFound=valid` reached it through the parent).

**Design consequence:** confirms §2 "the parent node is set AFTER the node is created, and
init() is called" for the markup path — the readiness gate cannot be satisfied from init
for XML-declared children.

### Q3 / Q3b — `getParent()` inside a dynamically created child's init

- `CreateObject("roSGNode", "LcChild")` (never attached): `parent=invalid` inside init
  (`init.q3 PASS`).
- `scene.createChild("LcChild")` (create-AND-append): `parent=invalid` inside init too
  (`init.q3b … log=child-init tag=default parent=invalid scene=valid;`) — the append
  happens after the child's init returns.

**Design consequence:** confirms §2's "the gate can never be satisfied from init for ANY
creation path" for all three creation paths (markup, `CreateObject`, `createChild`).

### Q4 — XML `<children>` attribute timing

- Inside the child's own `init()`, the `tag` field read its declared default
  (`tag=default`, `init.q4 PASS`), not the markup value `fromXml`.
- Immediately after `CreateObject("roSGNode", "LcParent")` returned, the child's `tag`
  read `fromXml` (`init.q4.evidence … tagAfterCreate=fromXml`); the parent's own init
  had already seen `fromXml` (Q1). Pinned order: child `init()` → markup attribute
  applied → parent `init()`.

**Design consequence:** confirms the §2 third-batch implication that markup values land
after the child's creation. The §12 risk "init step 4 clobbers layout-declared values" is
NOT realised by this ordering: a child's init-time write to a field is overwritten by the
markup attribute that lands afterwards, so the layout-declared value wins. (Any init-time
READ of such a field sees the default — the premise of §7's
`BRS_COMPONENT_INPUT_READ_IN_INIT` holds for markup-supplied inputs too.) Nothing to fold
into §2.

### Q5 — `getScene()` inside init of a not-yet-attached node

- `m.top.getScene()` was VALID inside `init()` in all three probed shapes: the XML child
  before its parent was set (Q1/Q2 line), a `CreateObject`-created never-attached node
  (`init.q5 … scene=valid`), and a `createChild` node (Q3b line). All three were created by
  render-thread code of the live scene; creation from the main thread was not probed.

**Design consequence:** spec 2's terminal walk hop (`getScene()`) answers from inside
`init()` even for an unattached node — attachment is not a precondition for
`getScene()` on this device.

### Q5b — `getParent()` of a Scene's direct child

- Probe shape: `LcParent` was `appendChild`ed to the Scene at runtime (InitScene declares
  no `<children>`). Its `getParent()` returned the Scene node itself —
  `isSameNode(m.top)` true, `subtype()` = `InitScene` (`init.q5b … parentIsScene=true
  parentSubtype=InitScene`) — not one of the hidden framework elements ifSGNodeChildren
  describes for `getChild()` on a Scene. The `<children>`-markup variant of a Scene's
  direct child was NOT exercised.

**Design consequence:** for an appended child, the §2 open question ("what `getParent()`
answers for a scene's direct child") is answered: the Scene node; a parent walk
terminates when `getParent().isSameNode(getScene())`.

### Q6 — `callFunc` render→render, `hasFunc`, self-`callFunc`

- (a) Scene → child `callFunc("lcPing")` returned `pong:LcChild` — the function ran with
  the CHILD's `m` (`m.who` was set in the child's init) (`init.q6a PASS`).
- (b) `hasFunc("lcPing")` is `true` on the LcChild component and `false` on a plain
  `Group` node (`init.q6b … PASS` ×2). The device offers no way to tell the OS floor of
  `hasFunc`; this pins it on OS 15.3.4 only.
- (c) A component calling `callFunc` on ITS OWN node executed and returned
  (`self:pong:LcChild`, `init.q6c PASS`). This was itself reached through an outer
  `callFunc` from the scene, so a nested callFunc (scene → child → child-self) also
  executes directly.
- `threadinfo()` on the child node: `currentThread` and the node's `owningThread` are the
  same Render thread (`A2E878`, `InitScene`); `willRendezvousFromCurrentThread` = `No`
  — direct same-thread execution, no rendezvous (`init.q6.evidence`).

**Design consequence:** confirms §2 (second docs batch) "callFunc … always executes in the
component's owning ScriptEngine and thread … and will always use the m and m.top of the
owning component" for render→render and self calls; pins §4's `hasFunc` guard. The §12
risk "callFunc render→render from Kotlin is … unpinned" is retired on this device; the
`__kotlinLifecycle`-field fallback hop is not required by the platform.

### Q7 — detach variants (parent `change` field observed by the child)

Decoding the watch log. Three watchers under `LcParent`: w1 (`observeFieldScoped`), w2
(plain `observeField`), w3 (`observeFieldScoped`, later `reparent()`ed). The scene KEPT
`m.w1`/`m.w2`/`m.w3` references throughout. Each entry is `<form>:<Operation>:<whether
the OBSERVING watcher's own getParent() was valid at fire time>`. Observers fired in
registration order (w1, w2, w3) on every mutation.

Arm stage (`init.q7.evidence`): `armScoped:armed;` (w1 armed) → `scoped:add:valid;` (w1
fired on w2's `createChild`) → `armPlain:armed;` (w2 armed) → `scoped:add:valid;
plain:add:valid;` (w1 and w2 fired on w3's `createChild`) → `armScoped:armed;` (w3 armed).

Remove stage (log cleared first; three mutations; read 0.6s later):

| Mutation | w1 (scoped) | w2 (plain) | w3 (scoped) |
|---|---|---|---|
| `parent.removeChild(w1)` | `scoped:remove:invalid` | `plain:remove:valid` | `scoped:remove:valid` |
| `parent.removeChild(w2)` | `scoped:remove:invalid` | `plain:remove:invalid` | `scoped:remove:valid` |
| `w3.reparent(other, false)` | `scoped:remove:invalid` | `plain:remove:invalid` | `scoped:remove:invalid` |

- **F7.1 — the removed watcher's own observer FIRES on its own removal, both forms**
  (`init.q7a PASS`, `init.q7b PASS`): Operation `remove`; inside the handler the
  removed node's `getParent()` is already `invalid`; `m.top` on the just-removed node did
  not crash (no `handler-crash` entries anywhere in the run).
- **F7.2 — delivery is synchronous, inside the mutating call.** w2's reading is `valid`
  for mutation 1 but `invalid` for mutation 2, so mutation 1's callbacks ran before the
  scene's next statement (`removeChild(w2)`); w3's reading is `invalid` for the reparent
  although it ends the run attached under `other`, so the callback ran inside `reparent`
  between detach and re-attach. Deferred delivery would have produced `invalid`/`valid`
  respectively.
- **F7.3 — a removed component whose node is still REFERENCED keeps observing.** w1,
  removed in mutation 1, fired again on mutations 2 and 3; w2, removed in mutation 2,
  fired again on mutation 3 — both `observeFieldScoped` and plain `observeField`.
  Observer liveness followed node LIFE (the scene's `m.w1`/`m.w2` references), not tree
  membership. Nine fires from three mutations × three live observers: exactly one fire
  per observer per mutation, no duplicates, no drops.
- **F7.4 — `reparent(newParent, false)` is recorded on the OLD parent's `change` field
  as Operation `remove`** — no `move` operation was recorded (`init.q7c`). The new
  parent's `change` field was unobserved, so whether an `add` is recorded there was not
  probed.
- **F7.5 — `createChild` records Operation `add`** on the parent's `change` field (arm
  stage).

**Design consequence:** the §8 Q7 expectation ("expected negative per A4") did NOT hold —
the parent-`change`-field detach signal exists on this device, for both observer forms,
synchronously, with the `remove` operation, and the §14 condition "unless §8 Q7 is
positive" is met. Probe A4's own fact stands unchanged under A4's conditions (observer
component removed AND every reference released ⇒ no ghost fires): F7.3 refines the
§2 SUMMARY of A4, which omitted the released-references condition — observer death
follows node destruction, not `removeChild`. Folded into §2. F7.3 also bears on the
recycling law in §4 (a retired-but-retained node is exactly the "removed, still
referenced" shape): the platform does not silence a retained node's observers on
removal.

## Fixture provenance

Package authored from the Task 1 brief of the lifecycle-core plan
(`.superpowers/sdd/2026-09-04-component-lifecycle-core/task-1-brief.md`); two local
identifiers in `InitScene.brs` `stageInitOrder()` were renamed before first deploy
(`sub` → `psub`, `log` → `initLog`: a keyword and a built-in shadow). Device-side compile
was clean on the first deploy; a single run produced every verdict above.
