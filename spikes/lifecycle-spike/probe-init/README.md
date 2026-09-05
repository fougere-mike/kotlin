# Lifecycle-spike Probe init — initialization order, callFunc, detach

Raw BrightScript; `./deploy.sh` sideloads and captures. Pins spec §8 of
`docs/superpowers/plans/2026-09-04-component-lifecycle-design.md`. The scene
runs six 0.6s settle stages; the run ends with `[SPIKE] END` (~5s). A FAIL is
a FINDING, never an error.

| Verdict | PASS means |
|---|---|
| `init.q1.childInitBeforeParentInit` | the XML child's init logged before the parent's |
| `init.q2.xmlChildParentInvalidInInit` | `getParent()` was invalid inside the XML child's init (documented) |
| `init.q4.xmlAttrNotYetAppliedInChildInit` | the child's `tag` read its default, not the XML markup value, inside init |
| `init.q3.createObjectParentInvalidInInit` | `getParent()` invalid inside a CreateObject-created child's init |
| `init.q6a.callFuncRenderToRender` | scene→child callFunc returned `pong:LcChild` (ran with the child's m) |
| `init.q6b.hasFuncTrueOnComponent` / `hasFuncFalseOnPlainNode` | `hasFunc` discriminates a component with the function from a plain Group |
| `init.q6c.selfCallFunc` | a component's callFunc on its own node executed |
| `init.q7a.scopedChangeFiresOnOwnRemoval` / `q7b.plain…` | SCREEN, necessary-not-sufficient: some `change` observer of that form fired with Operation `remove` — a still-attached sibling's fire satisfies it; whether the REMOVED watcher's own observer fired is established by the FINDINGS decode of the full watch log, not by this verdict (expected FAIL per A4) |

INFORMATIVE: `q4.evidence` (tag after create), `q5.getSceneInUnattachedInit`,
`q3b.createChildParentAtInit`, `q5b.sceneChildParent`, `q6.evidence`
(threadinfo), `q7.evidence`, `q7c.reparentRecorded`.
