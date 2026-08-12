# Scope-Handle Spike Findings — cross-component channel matrix (Q1a–c, Q4) (2026-08-12)

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
| `callFuncArg` (`callFunc` argument) | FAIL (`marker=1 keys=4`) | **PASS** | FAIL — key KEPT, value → `Invalid` (`keyExists=PASS type=Invalid`) | args deep-copied despite synchronous same-thread call |
| `callFuncRet` (`callFunc` return value) | FAIL (`retMarker=1`; A's post-return mutation of its kept ref invisible to B) | **FAIL** — arrived as a detached bare `Node` (`subtype=Node id= sameAsFound=FAIL`), NOT probeA and not even a ProbeA-subtype clone | FAIL — key KEPT, value → `Invalid` | the ONLY channel that destroys node identity — asymmetric with the arg direction |
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

1. **No channel preserves roAssociativeArray reference identity** (Q1a is a
   flat NO). Every hop is a deep copy — including synchronous same-thread
   `callFunc` in both directions — and RTQ is worse: it MOVES the AA out of
   the sender's variable (`keys=0` afterward).
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
