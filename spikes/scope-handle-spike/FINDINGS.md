# Scope-Handle Spike Findings — cross-component channel matrix (Q1, Q4; Q2/Q3 blocked) (2026-08-12)

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
original copy (`evProbe=invalid fieldProbe=invalid`). Every read path hands out
its own copy; no read path shares identity with the sender's object.

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
with `kotlin-probe/deploy.sh`. Run-attributable capture:
`capture-kotlin-probe.txt`. The run reached `[SPIKE] END` with every cell
reporting; the full verdict set reproduced identically across two complete
runs (plus a third earlier-format run that agreed on every shared cell).

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
   function-valued slot, not a type.
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
