# Scope-Handle Spike Findings — cross-component channel matrix (Q1, Q4, OS15 ref APIs; Q2/Q3 blocked) (2026-08-12)

Device: Roku Ultra at 192.168.1.125 (`roDeviceInfo.GetModel()` = 4800X),
**Roku OS 15.3.4 build 841**. Raw BrightScript spike app in `channel-matrix/`
(MatrixScene + ProbeA "owner" + ProbeB "child", step machine, 2s watchdog per
step so a never-firing observer records an explicit FAIL instead of a blank
cell). Deployed with `channel-matrix/deploy.sh` (zip + curl sideload, telnet
capture, sentinel-filtered). Run-attributable capture:
`capture-channel-matrix.txt`; verdict lines: `channel-matrix/spike-results.txt`.
The run reached `[SPIKE] END` with every matrix cell reporting.

Payload posted on every channel (built fresh per step by B):
`{ marker: 1, theNode: m.top, theFn: probeFunction, kotlinObj: invalid }`.
Probes: **refIdentity** = A mutates `payload.marker = 2` at the arrival point,
B re-reads its LOCAL copy; **nodeRef** = `isSameNode` against the real node;
**fnRef** = `Type()` of the arrived `theFn` (+ invoke when callable).

## Verdict table (Q1a–c)

| channel | refIdentity (Q1a) | nodeRef (Q1b) | fnRef (Q1c) | notes |
|---|---|---|---|---|
| `nodeField` (runtime-added AA field on A's node, `setField`) | FAIL — B's copy untouched (`marker=1 keys=4`) | **PASS** — `isSameNode` true | FAIL — key DROPPED (`keyExists=FAIL`) | deep copy on write; reads copy too (see eventData) |
| `globalField` (`m.global` runtime-added field) | FAIL (`marker=1 keys=4`) | **PASS** | FAIL — key DROPPED | identical semantics to `nodeField` |
| `callFuncArg` (`callFunc` argument) | FAIL (`marker=1 keys=4`) | **PASS** | FAIL — key KEPT, value → `Invalid` (`keyExists=PASS type=Invalid`) | args deep-copied despite synchronous same-thread call. Scope note: this row tested node-INSIDE-AA; the kotlin-probe bare-node probe (node passed directly as the arg) gave the same verdict — identity preserved (`barenode.callFuncArg PASS subtype=SpikeChild id=spike_child`) |
| `callFuncRet` (`callFunc` return value) | FAIL (`retMarker=1`; A's post-return mutation of its kept ref invisible to B) | **FAIL** — arrived as a detached bare `Node` (`subtype=Node id= sameAsFound=FAIL`), NOT probeA and not even a ProbeA-subtype clone | FAIL — key KEPT, value → `Invalid` | the ONLY channel that destroys node identity — asymmetric with the arg direction. Scope note: this row tested node-inside-AA; the kotlin-probe bare-node probe (node returned directly) gave the same verdict — detached bare `Node` (`barenode.callFuncRet FAIL subtype=Node id=`), so the wrapping AA is not the cause |
| `rtq` (`roRenderThreadQueue.PostMessage` → A's init-registered handler) | FAIL — **sender's AA is GUTTED by PostMessage** (`postState keys=0` immediately after the post; at check time `marker=invalid keys=0`) | **PASS** | FAIL — key DROPPED | **MOVE semantics**: delivered AA is fresh (`keys=3 marker=1`), sender keeps an empty husk |

### eventData sub-probe (on `nodeField`)

**FAIL** — `event.getData()` and a same-moment `m.top.mailbox` field read are
DIFFERENT objects (both `roAssociativeArray`): a probe key planted on the
`getData()` AA was not visible in the field read, and neither probe key
(`evProbe` on getData, `fieldProbe` on the field read) ever appeared in B's
original copy (`evProbe=invalid fieldProbe=invalid`). Every probed read path
hands out its own copy (getData(), dot-read; getField() untested); no read
path shares identity with the sender's object.

## Q4 — runtime-addField observers

| probe | verdict | evidence |
|---|---|---|
| Q4.1 runtime `addField("mailbox","assocarray",true)` + `observeField` fires | **PASS** | `q4.1.addFieldObserver PASS observer-fired` on B's first post |
| Q4.2 `alwaysNotify` honored on runtime-added fields | **PASS** | B posted the IDENTICAL payload object twice; observer fired both times (`q4.2.alwaysNotifyRepost PASS fires=2`) |

Also observed: `AddMessageHandler` from ProbeA's init returned a
`roRenderThreadQueueRegistration` token (init-time RTQ registration works,
matching the RTQ spike).

## The headline facts

1. **No channel gives shared identity** (Q1a is a flat NO) — including
   synchronous same-thread `callFunc` in both directions — and RTQ is worse:
   it MOVES the AA out of the sender's variable (`keys=0` afterward). These
   probes proved it at the payload's top level; the kotlin-probe nested
   sub-probe (Q1d below) confirmed the copy is DEEP at every level — the
   owner mutating `payload.nested.inner.marker` on its received copy was
   never visible in the sender's directly-held `inner` reference, on any
   channel (`nested FAIL innerMarker=1` × 3).
2. **Node references DO survive** every channel EXCEPT the `callFunc` RETURN
   path, where the node came back as a detached bare `Node` (empty id, wrong
   subtype, `isSameNode`=false against probeA by both handle and `findNode`).
   Arg direction preserves identity; return direction destroys it.
3. **Function references never survive a hop**, in two distinct strip modes:
   field-based channels (`nodeField`, `globalField`, `rtq`) DROP the key
   entirely; `callFunc` (both directions) keeps the key with value `Invalid`.
   No crash on any channel — stripping is silent.
4. `invalid`-valued keys survive delivery: the RTQ-delivered AA had `keys=3`
   with `marker` and `theNode` present and `theFn` absent — i.e. the third
   surviving key is the `kotlinObj: invalid` slot. (Arithmetic on observed
   counts, not a printed verdict.)
5. Runtime-added fields are first-class for observation: observers fire, and
   `alwaysNotify=true` set at `addField` time is honored (Q4.1/Q4.2 PASS).

## Raw verdict lines (v2 run, verbatim)

```
[SPIKE] BEGIN model=4800X os=15.3.4 build=841
[SPIKE] rtq.register token=roRenderThreadQueueRegistration
[SPIKE] q4.1.addFieldObserver PASS observer-fired
[SPIKE] eventData.refIdentity FAIL getDataType=roAssociativeArray fieldType=roAssociativeArray
[SPIKE] nodeField.nodeRef PASS type=roSGNode
[SPIKE] nodeField.fnRef FAIL type=Invalid keyExists=FAIL
[SPIKE] nodeField.refIdentity FAIL marker=1 keys=4 evProbe=invalid fieldProbe=invalid
[SPIKE] q4.2.alwaysNotifyRepost PASS fires=2
[SPIKE] globalField.nodeRef PASS type=roSGNode
[SPIKE] globalField.fnRef FAIL type=Invalid keyExists=FAIL
[SPIKE] globalField.refIdentity FAIL marker=1 keys=4
[SPIKE] callFuncArg.nodeRef PASS type=roSGNode
[SPIKE] callFuncArg.fnRef FAIL type=Invalid keyExists=PASS
[SPIKE] callFuncArg.refIdentity FAIL marker=1 keys=4
[SPIKE] callFuncRet.nodeRef FAIL type=roSGNode subtype=Node id= sameAsFound=FAIL
[SPIKE] callFuncRet.fnRef FAIL type=Invalid keyExists=PASS
[SPIKE] callFuncRet.refIdentity FAIL retMarker=1
[SPIKE] rtq.postState keys=0 marker=invalid hasTheNode=FAIL
[SPIKE] rtq.delivered type=roAssociativeArray keys=3 marker=1
[SPIKE] rtq.nodeRef PASS type=roSGNode
[SPIKE] rtq.fnRef FAIL type=Invalid keyExists=FAIL
[SPIKE] rtq.refIdentity FAIL marker=invalid keys=0
[SPIKE] END
```

A first run (identical probes minus the diagnostic detail fields) produced the
same PASS/FAIL verdict in every cell; the v2 run added the characterization
detail (`keyExists`, `postState`, `delivered`, returned-node subtype/id) after
two surprises — the RTQ sender-side gutting and the callFuncRet node-identity
loss — needed probe-bug-vs-platform disambiguation. Both reproduced.

## Method notes

- Every observer/handler was armed in the OWNING component's init, before
  MatrixScene wired anything (arming-order law).
- Every cross-channel read is try/catch-guarded; no probe threw on either run
  (no `THREW`/`threw:` lines in the capture).
- Channels were exercised sequentially (step machine in ProbeB), each step
  bounded by a 2s one-shot watchdog Timer that converts a never-firing
  observer into explicit `FAIL no-delivery` lines. The watchdog never fired.

---

# Kotlin probe (Q1d) — what a cloned Kotlin object is (2026-08-12)

Same device (Roku Ultra 4800X, OS 15.3.4 build 841). Kotlin app in
`kotlin-probe/` (kotlin-roku project: SpikeScene `@SGLayout`-declares
SpikeOwner + SpikeChild; step machine + 2s watchdog mirror ProbeB), deployed
with `kotlin-probe/deploy.sh`. Both runs reached `[SPIKE] END` with every
cell reporting. COMMITTED evidence: `capture-kotlin-probe.txt` (the Q1d run)
and `capture-kotlin-probe-run2.txt` (the OS15-addendum run, which re-executed
the full Q1d matrix before the new steps and matched it cell-for-cell — the
committed reproduction). Two still-earlier runs printed the same Q1d verdicts
but their captures were overwritten before deploy.sh was made run-stamped;
they are testimony, not evidence.

The payload, built fresh per channel by SpikeChild:
`{ marker: 1, kobj: SharedVm-instance, theFn: <the instance's bump_k_ fn value>, nested: {inner: {marker: 1}} }`
where `SharedVm` is a plain Kotlin class (`var counter: Int`,
`fun bump(): Int`), `bump()` called once before shipping (`counter=1`). A
compiled Kotlin object at runtime is an roAssociativeArray with 4 data keys
(`__type`, `__proto`, `__id`, `counter`) and 6 function-valued slots
(`bump_k_`, `__get_counter`, `__set_counter`, `equals`, `hashcode`,
`tostring`) — sender-side print pinned exactly that shape before every hop.

## Q1d verdict table

| channel | arrives as | method dispatch (`bump()`) | `as? SharedVm` | state divergence | nested `inner.marker` |
|---|---|---|---|---|---|
| `nodeField` | roAA, DATA KEYS ONLY — all 6 fn slots DROPPED (`__id,__proto,__type,counter`) | **CRASH** (caught): `Member function not found in BrightScript Component or interface.` | **PASSES** | total — owner wrote `counter=99`, child re-read `localCounter=1` | deep copy — owner wrote 2, child's direct ref read 1 |
| `callFuncArg` | roAA, ALL 10 keys present — every fn slot → `roInvalid` | **CRASH** (caught): same message | **PASSES** | total (same) | deep copy (same) |
| `rtq` | roAA, DATA KEYS ONLY (same strip as nodeField) | **CRASH** (caught): same message | **PASSES** | total (same) | deep copy (same) |

Payload-level fn key (`theFn`, sender-side `type=roFunction` before every
post): `nodeField`/`rtq` DROPPED the key (`keyExists=FAIL`); `callFuncArg`
kept it valued `Invalid` (`keyExists=PASS type=roInvalid`) — Task 3's two
strip modes, reproduced from typed Kotlin.

## The Q1d headline facts

1. **A Kotlin object never crosses a component boundary alive.** Every
   channel delivers a husk: the data keys survive, every function-valued
   slot is stripped (field/rtq channels drop the keys; callFunc keeps them
   as `Invalid` — the same two strip modes Task 3 found for top-level fn
   keys, now proven for method slots nested inside a payload). The first
   method call on the husk crashes with `Member function not found`.
2. **Kotlin's own type check is NO guard.** `as? SharedVm` PASSES on every
   husk — the `is`/`as` machinery walks `__proto`, which is plain data and
   survives the copy. The failure surfaces only at first dispatch. Any
   "is this handle live?" runtime check would have to probe for a
   function-valued slot, not a type. (Negative control, addendum run: `as?
   SharedVm` on a plain `__proto`-less AA returns null —
   `castControl.plainAA PASS castIsNull=true` — so the mechanism really is
   the `__proto`-chain walk, not a vacuously-true cast.)
3. **The copy is deep at every level** (the Task 3 "top-level only" caveat
   is resolved): the owner mutating `nested.inner.marker` on its received
   copy never appeared in the child's DIRECTLY-HELD `inner` reference —
   `innerMarker=1` on all three channels.
4. **RTQ's move semantics gut only the posted AA's top level, sender-side.**
   After `postMessage` the sender's payload AA is empty (`payloadKeys=0`),
   but the sender's direct references to the values it contained stay fully
   intact and functional — the vm kept all 10 keys including callable fn
   slots (`vmCounter=1`), `innerMarker=1`. The DELIVERED copy is still a
   stripped husk (data keys only).
5. **Owner-side writes on the husk succeed silently** (`counter=99`,
   `innerMarker=2` read back fine on the owner's copy) — mutation doesn't
   throw, it just never propagates anywhere.
6. **Bare-node callFunc asymmetry confirmed** (carried item 3): a node
   passed DIRECTLY as the callFunc arg keeps identity
   (`barenode.callFuncArg PASS subtype=SpikeChild id=spike_child`); a node
   returned directly comes back as a detached bare `Node`
   (`barenode.callFuncRet FAIL subtype=Node id=`). The AA wrapper in Task 3
   was not the cause.

## Q2 — ambient identity in a shipped lambda: BLOCKED-BY-Q1

Q2 asked whose ambient state (object singletons, GetGlobalAA) a
child-created lambda reads when the owner invokes it. It is unaskable on
device: Task 3 proved function references never survive any channel (field
channels drop the key, callFunc nulls it, rtq drops it), and Q1d confirmed
the same stripping applies to every function-valued slot nested anywhere in
a payload. A Kotlin lambda IS a compiled object whose `invoke` slot is a
function value — so no channel can deliver a callable block to another
component, and there is nothing for the owner to invoke. Moot for the
design: blocks cannot ship at all.

## Q3 — suspend machinery in a shipped block: BLOCKED-BY-Q1

Blocked for the same reason — a suspend block cannot ship (Q1/Q1d). Design
note: under the typed-named-request branch (the branch left standing), the
"handler" the owner runs in response to a request is the owner's OWN code,
compiled into the owner's component, running on the owner's scope and pump —
same-component coroutine work that the existing runtime already proves on
device (E2E Suites 2/4/7, stdlib suites). The question evaporates rather
than remains open.

## Raw verdict lines (kotlin-probe, final run, verbatim)

```
[SPIKE] rtq.register token=roRenderThreadQueueRegistration
[SPIKE] BEGIN model=4800X os=15.3.4 build=841
[SPIKE] q1d.nodeField.pre vmType=roAssociativeArray counter=1 vmKeys=__get_counter:roFunction,__id:roInteger,__proto:roArray,__set_counter:roFunction,__type:roString,bump_k_:roFunction,counter:roInteger,equals:roFunction,hashcode:roFunction,tostring:roFunction
[SPIKE] q1d.nodeField.preFn keyExists=PASS type=roFunction
[SPIKE] q1d.nodeField.arrived kobjType=roAssociativeArray kobjKeys=__id:roInt,__proto:roArray,__type:roString,counter:roInt
[SPIKE] q1d.nodeField.fnKey keyExists=FAIL type=Invalid
[SPIKE] q1d.nodeField.cast PASS (as? SharedVm on the arrived value)
[SPIKE] q1d.nodeField.method FAIL via=typed threw:Member function not found in BrightScript Component or interface.
[SPIKE] q1d.nodeField.ownerMutate ok counter=99 innerMarker=2
[SPIKE] q1d.nodeField.identity FAIL localCounter=1
[SPIKE] q1d.nodeField.nested FAIL innerMarker=1
[SPIKE] q1d.nodeField.refIdentity FAIL marker=1
[SPIKE] q1d.callFuncArg.pre vmType=roAssociativeArray counter=1 vmKeys=__get_counter:roFunction,__id:roInteger,__proto:roArray,__set_counter:roFunction,__type:roString,bump_k_:roFunction,counter:roInteger,equals:roFunction,hashcode:roFunction,tostring:roFunction
[SPIKE] q1d.callFuncArg.preFn keyExists=PASS type=roFunction
[SPIKE] q1d.callFuncArg.arrived kobjType=roAssociativeArray kobjKeys=__get_counter:roInvalid,__id:roInt,__proto:roArray,__set_counter:roInvalid,__type:roString,bump_k_:roInvalid,counter:roInt,equals:roInvalid,hashcode:roInvalid,tostring:roInvalid
[SPIKE] q1d.callFuncArg.fnKey keyExists=PASS type=roInvalid
[SPIKE] q1d.callFuncArg.cast PASS (as? SharedVm on the arrived value)
[SPIKE] q1d.callFuncArg.method FAIL via=typed threw:Member function not found in BrightScript Component or interface.
[SPIKE] q1d.callFuncArg.ownerMutate ok counter=99 innerMarker=2
[SPIKE] q1d.callFuncArg.identity FAIL localCounter=1
[SPIKE] q1d.callFuncArg.nested FAIL innerMarker=1
[SPIKE] q1d.callFuncArg.refIdentity FAIL marker=1
[SPIKE] barenode.callFuncArg PASS type=roSGNode subtype=SpikeChild id=spike_child
[SPIKE] barenode.callFuncRet FAIL type=roSGNode subtype=Node id=
[SPIKE] q1d.rtq.pre vmType=roAssociativeArray counter=1 vmKeys=__get_counter:roFunction,__id:roInteger,__proto:roArray,__set_counter:roFunction,__type:roString,bump_k_:roFunction,counter:roInteger,equals:roFunction,hashcode:roFunction,tostring:roFunction
[SPIKE] q1d.rtq.preFn keyExists=PASS type=roFunction
[SPIKE] q1d.rtq.postState payloadKeys=0 vmType=roAssociativeArray vmKeys=__get_counter:roFunction,__id:roInteger,__proto:roArray,__set_counter:roFunction,__type:roString,bump_k_:roFunction,counter:roInteger,equals:roFunction,hashcode:roFunction,tostring:roFunction vmCounter=1 innerMarker=1
[SPIKE] q1d.rtq.arrived kobjType=roAssociativeArray kobjKeys=__id:roInteger,__proto:roArray,__type:roString,counter:roInteger
[SPIKE] q1d.rtq.fnKey keyExists=FAIL type=Invalid
[SPIKE] q1d.rtq.cast PASS (as? SharedVm on the arrived value)
[SPIKE] q1d.rtq.method FAIL via=typed threw:Member function not found in BrightScript Component or interface.
[SPIKE] q1d.rtq.ownerMutate ok counter=99 innerMarker=2
[SPIKE] q1d.rtq.identity FAIL localCounter=1
[SPIKE] q1d.rtq.nested FAIL innerMarker=1
[SPIKE] q1d.rtq.refIdentity FAIL marker=invalid
[SPIKE] END
```

## Kotlin-probe method notes + side observations

- The method-dispatch probe went through the TYPED path everywhere (the
  `as?` cast passed on every husk, so `cast.bump()` — compiling to
  `obj.bump_k_()` — was the exact call under test). The raw name-based
  fallback path was never needed.
- All divergence checks read the child's DIRECT references (`m.currentVm`,
  `m.currentInner`), not the payload, so RTQ's sender-side gutting could not
  blind them.
- `Type()` naming inconsistency, verbatim: clone integers report `roInt` on
  the nodeField/callFuncArg copies but `roInteger` on the rtq-delivered copy
  and the sender's originals. Cosmetic, but recorded as printed.
- Stdlib formatting wart found while wiring the sentinel:
  `__kotlin_numToStr` stringifies via BrightScript `Str()`, whose parameter
  is a FLOAT — epoch-sized values print as `1.786574e+09` (scientific, with
  precision loss). The spike's sentinel uses a raw-BRS
  `AsSeconds().toStr()` splice instead. Stdlib fix is backlog-worthy.
- `roDeviceInfo.GetVersion()` is deprecated on OS 15.3.4 (runtime warning,
  returns placeholder `999.99E99999A`); `GetOSVersion()` used instead.
- KGP packaging hole (kotlin-roku backlog): compiled SHARED (non-component)
  `.kt` files in the components source set are staged into the zip at
  `components/<file>.brs`, while the generated XMLs reference
  `pkg:/source/<file>.brs`. `validateComponentIncludes` correctly flagged
  all 12 resulting findings (warning mode); `kotlin-probe/deploy.sh`
  re-homes the strays into `source/` inside the zip after `packageRoku`.
- `@SGLayout`-declared children initialize BEFORE the scene's own init body
  runs (SpikeOwner's `rtq.register` line precedes the scene's `BEGIN` line
  in the capture) — consistent with SceneGraph child-before-parent init
  order; the arming-order law holds under the layout DSL.

---

# OS 15.0 reference APIs (addendum, from RokuDocs "Optimized data transfer and reference handling")

Doc: `../RokuDocs/Optimized data transfer and reference handling.html`
(saved by Mike mid-review; APIs introduced in Roku OS 15.0 that the original
matrix never tested). Probes added to the kotlin-probe app as step-machine
steps 4–7; capture: `capture-kotlin-probe-run2.txt`. Platform floor per doc:
OS 15.0+; SetRef/CanGetRef/GetRef are RENDER-THREAD-ONLY and AA-field-only
(and unusable with queueFields); Move* are any-thread. Both spike components
live on the render thread — the exact regime the ScopeHandle design targets.

## Verdict table

| probe | verdict | evidence |
|---|---|---|
| SetRef returns | Boolean function (doc's signature line says void; its Return Value section is right) | `setref.set ret=true`, `setrefVm.set ret=true` |
| Same-thread identity (owner GetRefs its own SetRef'd field) | **PASS** | `setref.ownerIsSameObject PASS` (roUtils.IsSameObject) |
| **Cross-component shared identity** (child GetRefs the OWNER's field, mutates top-level + nested; owner re-reads its DIRECT refs) | **PASS — genuinely shared, top-level AND nested** | `setref.sharedTopLevel PASS ownerMarker=2`, `setref.sharedNested PASS ownerInnerMarker=2`, `setref.isSameObjectAfterMutate PASS` |
| CanGetRef gating (field written via ordinary setField, never SetRef'd) | **PASS** — returns false per doc ("must explicitly set references before getting them") | `setref.canGetRefOnSetFieldField PASS canGetRef=false` |
| Observer silence on SetRef | **PASS** — zero fires through SetRef + GetRefs + mutations | `setref.observerSilentOnSetRef PASS fires=0` |
| Observer on ordinary setField of the same field afterwards | **PASS** — fires normally | `setref.observerOnSetField PASS fires=1` |
| **Kotlin object via SetRef/GetRef** | **ALIVE and SHARED** — arrived with ALL 10 keys incl. every fn slot (`bump_k_:roFunction` …); `as? SharedVm` passes (genuinely live here); `bump()` called FROM THE CHILD's script scope dispatched (`bumpReturned=2 counterAfter=2`); owner's direct ref then read `ownerCounter=2` | `setrefVm.gotten`, `setrefVm.cast PASS`, `setrefVm.method PASS via=typed`, `setrefVm.sharedState PASS` |
| MoveIntoField (child → owner node, cross-component) | works; source AA gutted; returns copied-count | `move.into copiedCount=2 srcKeysAfter=0 heldIntact=PASS` |
| MoveFromField (owner side) | works; field reads Invalid afterwards | `move.from gotKeys=held:roAssociativeArray,marker:roInteger,orphan:roAssociativeArray fieldAfterType=Invalid` |

## What this means

1. **SetRef/GetRef is the reference-preserving channel the Q1 matrix said
   didn't exist** — with three hard constraints: OS 15.0+ only,
   render-thread-only, and it is a SILENT STASH (no observer fires on
   SetRef; a delivery signal needs a separate doorbell — ordinary setField
   observers still work for that, proven above).
2. **The Kotlin-object cell is works-today-but-officially-unstable.** The
   fn-slot survival and cross-component dispatch ride exactly the
   function-reference behavior the doc warns about: "As a result of
   SceneGraph component namespacing … the function that is called is not the
   one that was referenced in the original component. The behavior … may
   change in a future release; therefore, developers should not build any
   dependencies on it." In this probe both components compile SharedVm into
   their script scopes (same-named functions exist in both namespaces), so
   name-namespace re-resolution is indistinguishable from true fn-value
   identity — and in the Kotlin-app world that same-class-in-both-scopes
   condition usually holds. Roku's DO-NOT-DEPEND label stands regardless.
   This does NOT reopen Q2/Q3 for design purposes.
3. **`move.into copiedCount=2` (not 1) is the doc's external-reference rule
   under BRS local-variable liveness:** the probe held BOTH nested AAs in
   live locals at call time (`held` deliberately, `orphan` incidentally —
   BRS locals live until function exit), so both were copied rather than
   moved (`held` verified intact). This same rule is why the Q1d rtq
   postState showed the sender's vm/inner references fully intact while the
   posted AA was gutted — PostMessage moves the top level and copies
   externally-referenced nested values.
4. roUtils (OS 15.0) provides `IsSameObject`/`DeepCopy` — `IsSameObject` is
   the identity oracle the raw-BRS spike lacked.

## Raw addendum verdict lines (run2, verbatim; run2 also re-ran the full Q1d matrix — identical cell-for-cell)

```
[SPIKE] castControl.plainAA PASS castIsNull=true (as? SharedVm on a __proto-less AA)
[SPIKE] setref.canGetRefOnSetFieldField PASS canGetRef=false (field written via setField, never SetRef)
[SPIKE] setref.set ret=true
[SPIKE] setref.ownerIsSameObject PASS
[SPIKE] setref.canGetRef PASS canGetRef=true
[SPIKE] setref.gotten type=roAssociativeArray keys=marker:roInteger,nested:roAssociativeArray
[SPIKE] setref.sharedTopLevel PASS ownerMarker=2
[SPIKE] setref.sharedNested PASS ownerInnerMarker=2
[SPIKE] setref.isSameObjectAfterMutate PASS
[SPIKE] setref.observerSilentOnSetRef PASS fires=0
[SPIKE] setref.observerOnSetField PASS fires=1
[SPIKE] setrefVm.pre vmType=roAssociativeArray counter=1 vmKeys=__get_counter:roFunction,__id:roInteger,__proto:roArray,__set_counter:roFunction,__type:roString,bump_k_:roFunction,counter:roInteger,equals:roFunction,hashcode:roFunction,tostring:roFunction
[SPIKE] setrefVm.set ret=true
[SPIKE] setrefVm.gotten canGetRef=PASS type=roAssociativeArray keys=__get_counter:roFunction,__id:roInteger,__proto:roArray,__set_counter:roFunction,__type:roString,bump_k_:roFunction,counter:roInteger,equals:roFunction,hashcode:roFunction,tostring:roFunction
[SPIKE] setrefVm.cast PASS (as? SharedVm on the GetRef'd object)
[SPIKE] setrefVm.method PASS via=typed bumpReturned=2 counterAfter=2
[SPIKE] setrefVm.sharedState PASS ownerCounter=2
[SPIKE] move.into copiedCount=2 srcKeysAfter=0 heldIntact=PASS
[SPIKE] move.from gotKeys=held:roAssociativeArray,marker:roInteger,orphan:roAssociativeArray fieldAfterType=Invalid
[SPIKE] END
```

---

# Decision — spec §5 tree walked against the evidence (2026-08-12)

Spec: `docs/superpowers/plans/2026-08-12-scopehandle-design.md` §5 ("Decision
tree"). The tree was written BEFORE the OS 15.0 SetRef/GetRef discovery — its
branches assume RTQ was the only candidate reference-preserving channel. Where
a branch's premise no longer matches the evidence, this walk answers the
branch's INTENT and says so explicitly.

## 1. Is there a reference-preserving channel for Kotlin objects?

The answer has three layers.

**(a) All six ordinary channels: NO.** Every Q1a cell is FAIL —
`nodeField.refIdentity FAIL marker=1 keys=4`, `globalField.refIdentity FAIL
marker=1 keys=4`, `callFuncArg.refIdentity FAIL marker=1 keys=4`,
`callFuncRet.refIdentity FAIL retMarker=1`, `rtq.refIdentity FAIL
marker=invalid keys=0`, `eventData.refIdentity FAIL
getDataType=roAssociativeArray fieldType=roAssociativeArray` — and the copy is
deep at every level (`q1d.<channel>.nested FAIL innerMarker=1` on all three
Q1d channels). For Kotlin objects specifically, the Q1d probe covered the
three carrier-relevant channels (nodeField, callFuncArg, rtq): every one
delivered a husk — data keys survive, every fn slot stripped, first dispatch
crashes (`q1d.nodeField.method FAIL via=typed threw:Member function not found
in BrightScript Component or interface.`, same on callFuncArg and rtq). The
unprobed ordinary channels offer nothing better at the raw-BRS level:
globalField showed semantics identical to nodeField, and callFuncRet is
strictly worse (also detaches nodes). The spec's "Q1 works only via RTQ"
contingency is MOOT: RTQ is not reference-preserving either — it MOVES the
sender's top level and delivers a stripped copy (`rtq.postState keys=0`;
`q1d.rtq.arrived kobjKeys=__id...,counter` — data keys only).

**(b) SetRef/GetRef (OS 15.0+, render-thread-only, AA fields, observer-silent):
YES for plain AA state.** Genuine cross-component shared identity, top-level
AND nested: `setref.sharedTopLevel PASS ownerMarker=2`, `setref.sharedNested
PASS ownerInnerMarker=2`, `setref.isSameObjectAfterMutate PASS`. This is a
documented, stable API surface (RokuDocs "Optimized data transfer and
reference handling") — the reference-preserving channel the Q1 matrix said
didn't exist.

**(c) Kotlin OBJECTS via SetRef: alive today, but resting on behavior Roku
officially disclaims.** The whole object crossed with all 10 keys including
every fn slot (`setrefVm.gotten ... bump_k_:roFunction ...`), dispatch from
the child's script scope worked (`setrefVm.method PASS via=typed
bumpReturned=2 counterAfter=2`), and state is genuinely shared
(`setrefVm.sharedState PASS ownerCounter=2`). But the fn-slot survival rides
exactly the function-reference behavior the doc warns about: "As a result of a
SceneGraph component namespacing, however, the function that is called is not
the one that was referenced in the original component. The behavior of passing
function references in this manner may change in a future release; therefore,
developers should not build any dependencies on it." Usable as
characterization; not a foundation.

## 2. Branch A vs branch B → **branch B (typed named requests)**

Recommendation: **branch B**, on two independent grounds:

1. **Device evidence: blocks cannot ship on any signaling channel.** A Kotlin
   lambda is a compiled object whose `invoke` slot is a function value, and
   function references never survive an ordinary hop (`nodeField.fnRef FAIL
   type=Invalid keyExists=FAIL`, `callFuncArg.fnRef FAIL type=Invalid
   keyExists=PASS`, `rtq.fnRef FAIL type=Invalid keyExists=FAIL`; every Q1d fn
   slot stripped). Branch A's precondition — "Q2 yes AND a block-delivery path
   exists" — is unsatisfiable: Q2 is BLOCKED-BY-Q1 (nothing callable ever
   arrives, so there is nothing to ask the question of).
2. **Platform-stability disclaimer: even where fn slots DO survive (SetRef),
   Roku says do not depend on it.** Building `owner.run { block }` on the one
   surviving delivery path would rest the core API on an officially
   may-change-in-a-future-release behavior.

Q3 evaporates under branch B: the "handler" the owner runs in response to a
request is the owner's OWN compiled code on the owner's scope and pump —
same-component coroutine work the existing runtime already proves on device
(see the Q3 section above).

## 3. Q4 → field-mailbox mechanism as-designed

`q4.1.addFieldObserver PASS observer-fired` and `q4.2.alwaysNotifyRepost PASS
fires=2`: runtime-`addField` inbox fields observe correctly and `alwaysNotify`
is honored on them. The literal spec-Q4 shape (runtime-`addField` +
`observeFieldScoped`) is also closed: `setref.observerOnSetField PASS fires=1`
fired through a runtime-added field's SCOPED observer (SpikeOwner.kt —
`addField("refStash", ...)` + `observeFieldScoped`). The alwaysNotify×scoped
combination (identical-value repost through a scoped observer) remains
unprobed. No declared-field fallback (stdlib base-class shim /
compiler-injected XML) is needed; the mechanism ships as designed.

## 4. The shared-VM premise — the escalation the tree anticipated, now a floor decision

The tree's first two branches anticipated exactly this situation: among
ordinary channels there is NO reference-preserving channel (the
STOP-and-escalate half tripped), but a reference-preserving channel DOES exist
with an OS floor attached — the intent of the "works only via RTQ → OS 15+
only → Mike decides" branch, with SetRef standing where the tree guessed RTQ.
Three options for Mike:

**(i) OS 15+ floor; shared-STATE-AA via SetRef.** [RECOMMENDED — contingent on
the floor raise being product-acceptable] The VM's identity is a shared state
bag crossed via SetRef (stable, documented API); the VM's METHODS stay
component-local — each component compiles the VM class into its own script
scope via the include closure, so local code runs against shared state
(design mechanism, not a probed verdict — the composed
both-sides-method-mutation probe is Stage 2; the spike pinned the
constituents, and the KGP packaging hole in the method notes above is the
open caveat on the staging half). Cost:
needs explicit liveness/validity markers, because Kotlin's type check is no
guard — `as? SharedVm` PASSES on dead husks (`q1d.nodeField.cast PASS` and
siblings; negative control `castControl.plainAA PASS castIsNull=true` proves
the mechanism is the `__proto`-chain walk).

**(ii) OS 15+ floor; full shared Kotlin object via SetRef.** Best DX — the
object with live methods really does cross today (`setrefVm.method PASS
via=typed bumpReturned=2 counterAfter=2`). But it rests on the
officially-unstable fn-slot behavior: one OS update from breakage, against an
explicit do-not-depend label.

**(iii) No floor raise (compile-target floor stays OS 9.4).** The shared-VM
premise pivots — VM state lives on nodes, or message-only MVI. Everything else
proven here (mailbox mechanism, node-ref passing, branch B's constituent
mechanics) works without any OS 15 API.

Dual-mode variants of (i)/(ii) exist (SetRef on 15+, field-copy fallback
below) but cost two carrier paths: two behaviors to test on device, two sets
of semantics to document.

## 5. ScopeHandle carrier consequence (Stage 2)

The Stage 2 field-mailbox design is unchanged on the floor path, and gains an
OPTIONAL OS 15+ payload path: SetRef-stash the payload on the mailbox node,
then ring the proven doorbell. The pairing is NECESSARY, not stylistic —
SetRef is observer-silent (`setref.observerSilentOnSetRef PASS fires=0`) while
an ordinary setField write to the same field still fires its observer
(`setref.observerOnSetField PASS fires=1`; plus the Q4 PASSes). MoveIntoField
is a candidate zero-copy mailbox WRITE on 15+ (any-thread per doc); caveat:
the spike device-pinned only the "copied because externally referenced" half
of its move-vs-copy rule (`move.into copiedCount=2 srcKeysAfter=0
heldIntact=PASS`) — the pure-move half is doc-only for now (deferred, minor).

## 6. Decision record

Decision (Mike, 2026-08-12, Stage 1 checkpoint):

- **[x] Branch: B wire protocol, DUAL user surface — compiler sugar in-program.**
  Hand-written requests (`object Refresh : ScopeRequest<R>` + `exposeScope {
  handle(...) }` registration + `owner.run(Request, args)`) AND compiler-lowered
  `owner.run { block }` (block lifted to a named function, request name
  synthesized, captures marshalled as the payload, per-component binding table
  name→local-fn-ref injected into scope hosts' generated init from each host's
  own include closure; dispatch miss = clean error outcome naming the fix).
  Diagnostics mandate: FIR ERRORS for code that cannot work (unmarshallable /
  function-typed / component-`this` captures), WARNINGS for code that behaves
  differently than it reads (captured-var mutation lost across the by-copy
  hop), corrective guidance in every message. Rationale: blocks cannot cross
  any signaling channel (§2), and the disclaimed fn-ref path stays out of the
  wire protocol so a Roku behavior change cannot break the primitive.
- **[x] Shared-VM option: (ii) + toolchain demotion path. Floor: MVVM layer
  requires OS 15.0+ (ScopeHandle primitive itself stays on the 9.4 compile
  floor).** Full shared Kotlin object via SetRef, with the demotion to
  methods-run-locally engineered into the toolchain, not user code: day-one
  FIR rules (shared VM classes final; no function-typed properties; liveness
  marker helper — `as?` passes on husks per §1), a permanent device canary
  pinning fn-slot-through-SetRef behavior, and early verification that a
  static-dispatch lowering (`WatchlistVm_refresh(vm)` against the shared AA
  receiver) is feasible so a future Roku change is a recompile, not a fleet
  migration. These items belong to the VM/MVVM program, not ScopeHandle
  Stage 2.
- **[x] Carrier: SetRef payload stash BACKLOGGED.** Stage 2 v1 mailbox =
  field-copy floor + RTQ-move fast path as previously decided. Large-payload
  reality routed where it actually flows: MoveIntoField as a typed-task
  OUTPUT optimization → M3/runTask backlog; "build ContentNode trees on the
  task thread" and "big results land in shared VM state, responses stay
  small" → documented patterns in Stage 2 docs.
