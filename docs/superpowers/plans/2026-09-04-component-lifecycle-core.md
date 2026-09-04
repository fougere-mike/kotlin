# Component Lifecycle Core (onStart/onStop, retire/revive) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Give every SceneGraph render component a gated `onStart()` hook, a reversible `retire`/`revive` cycle with `onStop()`, and fix the two latent component-dispatch defects that sit in the way — everything in spec 1 EXCEPT constructor inputs and typed layout builders, which are plan B (`2026-09-04-component-constructor-inputs.md`, written after this plan).

**Architecture:** One new stdlib file (`kotlin/brs/lifecycle/ComponentLifecycle.kt`) holds a per-instance lifecycle registry, the single-park `awaitReady` gate, and the retire/revive implementations; the compiler injects an unconditional attach into every render component's `init()`, synthesizes a `launch { awaitReady(); onStart() }` driver method when a class hierarchy overrides `onStart`, and emits bare-named `__kotlinRetire`/`__kotlinRevive` entries (the `__kotlinTaskMain` shape) that the parent reaches through `callFunc`. A raw-BrightScript spike pins the platform facts first.

**Tech Stack:** Kotlin K2 compiler fork (IR lowerings + IrToBrs transformer), BrightScript stdlib (`kotlin.brs`), kotlin-roku Gradle plugin (include validator), roku-test-app E2E suites on a Roku Ultra (OS 15.3.4), raw-BRS spike package.

**Spec:** `docs/superpowers/plans/2026-09-04-component-lifecycle-design.md` — read §1–§5, §8, §9 before starting; this plan argues from it.

## Global Constraints

- Build with `./rebuild.sh` ONLY (Kotlin repo) and `cd ../roku-test-app && ./rebuild-all.sh --all` (test app). Never run individual gradlew tasks except the two golden/diagnostic test commands the guide documents.
- Compile target floor is Roku OS 9.4; the device of record is a Roku Ultra 4800X on OS 15.3.4 at `ROKU_DEVICE_IP` (creds also in `../roku-test-app/local.properties`). The debug console (8085) allows ONE client; never kill the IDE.
- Render-thread-only laws: `awaitReady` guards with the ambient-top oracle (`PumpScheduler.hostTopOrNull()`), throwing the guided ISE pattern `"<fn> must be called from a render-thread component context (like runTask)"`.
- Stdlib compilation generates NO suspend state machines: every suspend function in the stdlib is single-park `ParkedContinuation` style with `parked.finish()` as the block's last expression, or tail-delegates.
- Include-closure law: a name-registered observer handler lives in the SAME FILE as its registration; new stdlib entry points reached from generated code are bare-named `@BrsStatic`; every emitted call goes through `createFunctionCall` (records the dependency) EXCEPT super calls into a user base component (see Task 3 — recording would pull the base's script into the leaf's XML and collide two `sub init()`s).
- Hooks are `onStart` (suspend) and `onStop` (plain); parent-side calls are `retire(node)`/`revive(node)`. `onReady`/`onDestroy` are rejected names (spec §1 decision 7).
- Gates never drop: golden tests 100 → 100 + new; FIR diagnostics 232 (untouched here); stdlib device suite 610/62 + new; E2E 104/10 + Suite 11 + 2 Suite 8 tests; `validateComponentIncludes` strict, 0 findings.
- Commit prefixes: `spike:`/`spikes:` (spike package / executed findings), `stdlib:`, `brs:` (compiler), `docs:`; roku-test-app commits use `e2e:`; kotlin-roku commits use `plugin:`. Every commit ends with the `Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>` trailer.

---

## File structure

| File | Responsibility |
|---|---|
| `spikes/lifecycle-spike/probe-init/` (new) | Raw-BRS spike: init order, getParent/getScene in init, XML attr timing, callFunc/hasFunc, detach observers |
| `spikes/lifecycle-spike/FINDINGS.md` (new) | Facts only; verdict lines quoted |
| `libraries/stdlib/brs/src/kotlin/brs/lifecycle/ComponentLifecycle.kt` (new) | Registry, tickets, attach, gate (`awaitReady` + marker observer + watchdog), driver claim, retire/revive impls, parent-side `retire`/`revive`, test hooks — ONE file (same-file include law) |
| `libraries/stdlib/brs/src/kotlin/brs/SceneComponent.kt` (modify) | `ComponentBase.onStart()` / `onStop()` |
| `libraries/stdlib/brs/src/kotlin/brs/ComponentCoroutines.kt` (modify) | `ComponentScopeHolder` internal + `cancelAndResetComponentScope()` |
| `libraries/stdlib/brs/src/kotlin/brs/roku/SceneGraph.kt` (modify) | `hasFunc` binding on `ISGNodeDict`/`RoSGNode` |
| `libraries/stdlib/brs/src/kotlin/brs/scope/ScopeApi.kt`, `ScopeHostImpl.kt` (modify) | `exposeScope` re-callable after retire (holder nulled, inbox unobserved on close-by-retire) |
| `libraries/stdlib/brs/test/kotlin/lifecycle/ComponentLifecycleTest.kt` (new) + `TestMain.kt` | Main-thread-legal unit subset |
| `compiler/.../brs/BrsIntrinsics.kt` (modify) | `hierarchyOverrides(irClass, name, signature)` |
| `compiler/.../irToBrs/IrToBrsTransformer.kt` (modify) | unconditional attach; `onKeyEvent` wrapper via hierarchy scan; `__kotlinRetire`/`__kotlinRevive` generation |
| `compiler/.../irToBrs/IrExpressionToBrsTransformer.kt` (modify) | `super.f()` static call in components |
| `compiler/.../brs/BrsCompiler.kt` (modify) | XML `<function>` entries for retire/revive |
| `compiler/.../brs/BrsSymbols.kt` (modify) | `launchOrNull`, `awaitReadyOrNull`, `kotlinLifecycleClaimDriverOrNull`, `coroutineScopeClassOrNull` |
| `compiler/.../brs/lower/BrsComponentLifecycleLowering.kt` (new) + `BrsLoweringPhases.kt` | Driver synthesis (phase 0.054) |
| `compiler/testData/codegen/brs/components/*` + `test/.../BrsGoldenFileTests.kt` | 5 new goldens, 1 renamed |
| `../kotlin-roku/src/main/kotlin/com/example/roku/gradle/tasks/ComponentIncludeValidator.kt` (+ test) | extends-chain awareness |
| `../roku-test-app/src/brsMain/kotlin/com/nuvyyo/roku/components/fixtures/Lifecycle*.kt` (new) | Suite 11 fixtures |
| `../roku-test-app/src/brsTest/kotlin/tests/ComponentLifecycleTests.kt` (new), `ScopeHandleTests.kt`, `TestMain.kt` | Suite 11 + 2 Suite 8 tests |
| `CLAUDE.md` | Lifecycle section, scaffolding + ScopeHandle amendments, gate table |

---

### Task 1: Spike package `probe-init` — pin the platform facts (spec §8)

**Files:**
- Create: `spikes/lifecycle-spike/probe-init/manifest`, `source/main.brs`, `components/lcCommon.brs`, `components/InitScene.xml`, `components/InitScene.brs`, `components/LcParent.xml`, `components/LcParent.brs`, `components/LcChild.xml`, `components/LcChild.brs`, `components/LcWatcher.xml`, `components/LcWatcher.brs`, `README.md`, `deploy.sh`, `.gitignore`
- Create: `spikes/lifecycle-spike/FINDINGS.md`
- Modify: `docs/superpowers/plans/2026-09-04-component-lifecycle-design.md` §8 (stamp EXECUTED), §2 (fold any falsified fact)

**Interfaces:**
- Produces: `spikes/lifecycle-spike/capture-probe-init.txt` (committed raw capture) and FINDINGS.md verdict lines `init.q1…q7`. Tasks 5–9 cite these facts; if Q6a/Q6c FAIL, STOP and re-plan Task 6's hop (spec §12 fallback) before continuing.

- [ ] **Step 1: Create the package skeleton by copying the flow-spike template**

```bash
mkdir -p spikes/lifecycle-spike/probe-init/components spikes/lifecycle-spike/probe-init/source
cp spikes/flow-spike/probe-a/deploy.sh spikes/lifecycle-spike/probe-init/deploy.sh
cp spikes/flow-spike/probe-a/.gitignore spikes/lifecycle-spike/probe-init/.gitignore
```

Edit `deploy.sh`: replace every `capture-probe-a` with `capture-probe-init`, and change the default capture window `${CAPTURE_SECONDS:-30}` to `${CAPTURE_SECONDS:-25}` (the run is ~5s of settle steps). The `APP_ROOT` relative path (`../../../../roku-test-app`) is unchanged: `spikes/lifecycle-spike/probe-init` sits at the same depth as `spikes/flow-spike/probe-a`.

- [ ] **Step 2: Write `manifest` and `source/main.brs`**

`manifest`:
```
title=lifecycle-spike-probe-init
major_version=1
minor_version=0
build_version=0
```

`source/main.brs`:
```brightscript
' Lifecycle-spike Probe init — component initialization order, parent/scene
' validity in init, XML attribute timing, callFunc/hasFunc, detach observers
' (design doc 2026-09-04-component-lifecycle-design.md, section 8).
' Everything runs in InitScene as a render-thread settle-Timer stage machine.
sub Main()
    dt = CreateObject("roDateTime")
    print "===SPIKE_SENTINEL_" + dt.AsSeconds().toStr() + "==="
    screen = CreateObject("roSGScreen")
    port = CreateObject("roMessagePort")
    screen.setMessagePort(port)
    screen.CreateScene("InitScene")
    screen.show()
    while true
        msg = wait(0, port)
        if type(msg) = "roSGScreenEvent"
            if msg.isScreenClosed() then return
        end if
    end while
end sub
```

- [ ] **Step 3: Write the shared helpers `components/lcCommon.brs`**

Included by every component XML below (component script scope, not `source/`).

```brightscript
' Shared helpers for the lifecycle spike components. Included via <script> by
' each component so they resolve in that component's scope.

' Appends one entry to the global init log (a string csv the scene reads).
sub lcLog(entry as String)
    if m.global <> invalid then
        if m.global.hasField("__lcLog") then m.global.__lcLog = m.global.__lcLog + entry + ";"
    end if
end sub

' Appends one entry to the global detach-watch log.
sub lcWatch(entry as String)
    if m.global <> invalid then
        if m.global.hasField("__lcWatch") then m.global.__lcWatch = m.global.__lcWatch + entry + ";"
    end if
end sub

function lcBool(b as Boolean) as String
    if b then return "valid"
    return "invalid"
end function

function lcStr(v as Dynamic) as String
    if v = invalid then return "invalid"
    if type(v) = "roString" or type(v) = "String" then return v
    if type(v) = "roBoolean" or type(v) = "Boolean" then
        if v then return "true" else return "false"
    end if
    return FormatJson(v)
end function
```

- [ ] **Step 4: Write `LcChild` (the probed child component)**

`components/LcChild.xml`:
```xml
<?xml version="1.0" encoding="utf-8" ?>
<component name="LcChild" extends="Group">
    <interface>
        <field id="tag" type="string" value="default" />
        <function name="lcPing" />
        <function name="lcSelfPing" />
    </interface>
    <script type="text/brightscript" uri="pkg:/components/lcCommon.brs" />
    <script type="text/brightscript" uri="pkg:/components/LcChild.brs" />
</component>
```

`components/LcChild.brs`:
```brightscript
' LcChild — logs, FROM INSIDE init(), whether getParent()/getScene() are valid
' and what its `tag` field reads (Q2/Q3/Q4/Q5). Exposes lcPing/lcSelfPing for
' the callFunc probes (Q6).
sub init()
    m.who = "LcChild"
    parentValid = (m.top.getParent() <> invalid)
    sceneValid = (m.top.getScene() <> invalid)
    lcLog("child-init tag=" + m.top.tag + " parent=" + lcBool(parentValid) + " scene=" + lcBool(sceneValid))
end sub

' Q6a target: proves callFunc ran with THIS component's m (m.who set in init).
function lcPing() as String
    return "pong:" + m.who
end function

' Q6c target: a component calling callFunc on ITS OWN node.
function lcSelfPing() as String
    return "self:" + lcStr(m.top.callFunc("lcPing"))
end function
```

- [ ] **Step 5: Write `LcParent` (XML-declared child, Q1/Q2/Q4)**

`components/LcParent.xml`:
```xml
<?xml version="1.0" encoding="utf-8" ?>
<component name="LcParent" extends="Group">
    <children>
        <LcChild id="xmlChild" tag="fromXml" />
    </children>
    <script type="text/brightscript" uri="pkg:/components/lcCommon.brs" />
    <script type="text/brightscript" uri="pkg:/components/LcParent.brs" />
</component>
```

`components/LcParent.brs`:
```brightscript
' LcParent — its init() runs AFTER its XML child's init() per the Roku
' "Component initialization order" page; the log order pins it (Q1).
sub init()
    m.who = "LcParent"
    child = m.top.findNode("xmlChild")
    childTag = "none"
    if child <> invalid then childTag = child.tag
    lcLog("parent-init childFound=" + lcBool(child <> invalid) + " childTag=" + childTag)
end sub
```

- [ ] **Step 6: Write `LcWatcher` (detach observers, Q7)**

`components/LcWatcher.xml`:
```xml
<?xml version="1.0" encoding="utf-8" ?>
<component name="LcWatcher" extends="Group">
    <interface>
        <field id="armScoped" type="boolean" value="false" />
        <field id="armPlain" type="boolean" value="false" />
    </interface>
    <script type="text/brightscript" uri="pkg:/components/lcCommon.brs" />
    <script type="text/brightscript" uri="pkg:/components/LcWatcher.brs" />
</component>
```

`components/LcWatcher.brs`:
```brightscript
' LcWatcher — arms an observer on its PARENT's `change` field (scoped or plain
' form) and logs every fire with the Operation and whether it is still
' attached. Expected NEGATIVE per flow-spike Probe A4 (a removed component's
' observers die in the same stack as removeChild); a fire is a FINDING that
' would permit a detach backstop.
sub init()
    m.who = "LcWatcher"
    m.top.observeField("armScoped", "onArmScoped")
    m.top.observeField("armPlain", "onArmPlain")
end sub

sub onArmScoped()
    p = m.top.getParent()
    if p = invalid then
        lcWatch("armScoped:no-parent")
        return
    end if
    p.observeFieldScoped("change", "onParentChangeScoped")
    lcWatch("armScoped:armed")
end sub

sub onArmPlain()
    p = m.top.getParent()
    if p = invalid then
        lcWatch("armPlain:no-parent")
        return
    end if
    p.observeField("change", "onParentChangePlain")
    lcWatch("armPlain:armed")
end sub

sub onParentChangeScoped(event as Object)
    recordChange("scoped", event)
end sub

sub onParentChangePlain(event as Object)
    recordChange("plain", event)
end sub

' try/catch: m.top on a half-removed node may crash (Probe A4 note); the
' record must survive to the global log either way.
sub recordChange(form as String, event as Object)
    try
        c = event.getData()
        attached = (m.top.getParent() <> invalid)
        lcWatch(form + ":" + lcStr(c.Operation) + ":" + lcBool(attached))
    catch e
        lcWatch(form + ":handler-crash:" + e.message)
    end try
end sub
```

- [ ] **Step 7: Write `InitScene` (the stage machine and verdicts)**

`components/InitScene.xml`:
```xml
<?xml version="1.0" encoding="utf-8" ?>
<component name="InitScene" extends="Scene">
    <script type="text/brightscript" uri="pkg:/components/lcCommon.brs" />
    <script type="text/brightscript" uri="pkg:/components/InitScene.brs" />
</component>
```

`components/InitScene.brs`:
```brightscript
' InitScene — render-thread settle-Timer stage machine (the render thread never
' sleeps). Verdict grammar: "[SPIKE] init.<q> PASS|FAIL k=v"; INFORMATIVE lines
' never PASS/FAIL; a FAIL is a FINDING. Global fields are addField'd FIRST
' (arming-order law) so every component created later can log into them.
sub init()
    m.clock = CreateObject("roTimespan")
    di = CreateObject("roDeviceInfo")
    osv = di.GetOSVersion()
    print "[SPIKE] BEGIN probe-init model=" + di.GetModel() + " os=" + osv.major + "." + osv.minor + "." + osv.revision + " build=" + osv.build
    m.global.addField("__lcLog", "string", false)
    m.global.addField("__lcWatch", "string", false)
    m.global.__lcLog = ""
    m.global.__lcWatch = ""
    m.stage = 0
    m.settle = CreateObject("roSGNode", "Timer")
    m.settle.duration = 0.6
    m.settle.repeat = false
    m.settle.observeField("fire", "onSettle")
    m.settle.control = "start"
end sub

sub verdict(name as String, ok as Boolean, kv as String)
    v = "FAIL"
    if ok then v = "PASS"
    print "[SPIKE] " + name + " " + v + " " + kv
end sub

sub onSettle()
    m.stage = m.stage + 1
    if m.stage = 1 then
        stageInitOrder()
    else if m.stage = 2 then
        stageDynamic()
    else if m.stage = 3 then
        stageCallFunc()
    else if m.stage = 4 then
        stageDetachArm()
    else if m.stage = 5 then
        stageDetachRemove()
    else if m.stage = 6 then
        stageDetachVerdict()
    else
        print "[SPIKE] END"
        return
    end if
    m.settle.control = "start"
end sub

' Q1 child-before-parent init; Q2 getParent in the XML child's init (docs:
' invalid); Q4 whether the XML attribute is visible inside the child's init
' (docs imply "after" => "default"); Q5b what getParent() answers for a
' Scene's direct child (Scene children are hidden framework elements).
sub stageInitOrder()
    m.global.__lcLog = ""
    m.parent = CreateObject("roSGNode", "LcParent")
    log = m.global.__lcLog
    ci = Instr(1, log, "child-init")
    pi = Instr(1, log, "parent-init")
    verdict("init.q1.childInitBeforeParentInit", ci > 0 and pi > 0 and ci < pi, "log=" + log)
    verdict("init.q2.xmlChildParentInvalidInInit", Instr(1, log, "parent=invalid") > 0, "log=" + log)
    verdict("init.q4.xmlAttrNotYetAppliedInChildInit", Instr(1, log, "tag=default") > 0, "log=" + log)
    xmlChild = m.parent.findNode("xmlChild")
    tagAfter = "none"
    if xmlChild <> invalid then tagAfter = xmlChild.tag
    print "[SPIKE] init.q4.evidence INFORMATIVE tagAfterCreate=" + tagAfter
    m.top.appendChild(m.parent)
    pp = m.parent.getParent()
    same = false
    sub = "invalid"
    if pp <> invalid then
        same = pp.isSameNode(m.top)
        sub = pp.subtype()
    end if
    print "[SPIKE] init.q5b.sceneChildParent INFORMATIVE parentIsScene=" + lcStr(same) + " parentSubtype=" + sub
end sub

' Q3 getParent inside a CreateObject-created child's init (expected invalid)
' and the createChild create-and-append variant; Q5 getScene inside init for
' an unattached node.
sub stageDynamic()
    m.global.__lcLog = ""
    dyn = CreateObject("roSGNode", "LcChild")
    log1 = m.global.__lcLog
    verdict("init.q3.createObjectParentInvalidInInit", Instr(1, log1, "parent=invalid") > 0, "log=" + log1)
    print "[SPIKE] init.q5.getSceneInUnattachedInit INFORMATIVE log=" + log1
    m.global.__lcLog = ""
    cc = m.top.createChild("LcChild")
    log2 = m.global.__lcLog
    print "[SPIKE] init.q3b.createChildParentAtInit INFORMATIVE log=" + log2
    m.dyn = dyn
    m.cc = cc
    m.top.appendChild(dyn)
end sub

' Q6a render->render callFunc into a child component; Q6b hasFunc true/false;
' Q6c self callFunc. threadinfo is supporting evidence.
sub stageCallFunc()
    r = m.dyn.callFunc("lcPing")
    verdict("init.q6a.callFuncRenderToRender", lcStr(r) = "pong:LcChild", "result=" + lcStr(r))
    plain = CreateObject("roSGNode", "Group")
    verdict("init.q6b.hasFuncTrueOnComponent", m.dyn.hasFunc("lcPing") = true, "")
    verdict("init.q6b.hasFuncFalseOnPlainNode", plain.hasFunc("lcPing") = false, "")
    s = m.dyn.callFunc("lcSelfPing")
    verdict("init.q6c.selfCallFunc", lcStr(s) = "self:pong:LcChild", "result=" + lcStr(s))
    print "[SPIKE] init.q6.evidence INFORMATIVE threadinfo=" + FormatJson(m.dyn.threadinfo())
end sub

' Q7 arm: three watchers under LcParent — scoped observer, plain observer, and
' one that will be reparent()ed. The scene KEEPS references to all three.
sub stageDetachArm()
    m.global.__lcWatch = ""
    m.w1 = m.parent.createChild("LcWatcher")
    m.w1.armScoped = true
    m.w2 = m.parent.createChild("LcWatcher")
    m.w2.armPlain = true
    m.w3 = m.parent.createChild("LcWatcher")
    m.w3.armScoped = true
    m.other = CreateObject("roSGNode", "Group")
    m.top.appendChild(m.other)
end sub

sub stageDetachRemove()
    print "[SPIKE] init.q7.evidence INFORMATIVE armed=" + m.global.__lcWatch
    m.global.__lcWatch = ""
    m.parent.removeChild(m.w1)
    m.parent.removeChild(m.w2)
    m.w3.reparent(m.other, false)
end sub

' PASS here means the removed watcher's observer DID fire on its own removal
' (a detach backstop would be possible); FAIL is the A4-predicted silence.
sub stageDetachVerdict()
    w = m.global.__lcWatch
    verdict("init.q7a.scopedChangeFiresOnOwnRemoval", Instr(1, w, "scoped:remove") > 0, "watch=" + w)
    verdict("init.q7b.plainChangeFiresOnOwnRemoval", Instr(1, w, "plain:remove") > 0, "watch=" + w)
    print "[SPIKE] init.q7c.reparentRecorded INFORMATIVE watch=" + w
end sub
```

- [ ] **Step 8: Write `README.md` (verdict contract)**

```markdown
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
| `init.q7a.scopedChangeFiresOnOwnRemoval` / `q7b.plain…` | the removed watcher's `change` observer fired (expected FAIL per A4) |

INFORMATIVE: `q4.evidence` (tag after create), `q5.getSceneInUnattachedInit`,
`q3b.createChildParentAtInit`, `q5b.sceneChildParent`, `q6.evidence`
(threadinfo), `q7.evidence`, `q7c.reparentRecorded`.
```

- [ ] **Step 9: Run the spike**

```bash
cd spikes/lifecycle-spike/probe-init && chmod +x deploy.sh && ./deploy.sh
```

Expected: `Install OK`, `Sentinel fresh`, a `[SPIKE] BEGIN … END` block, exit 0. If `no sentinel found`, the app did not launch — read `spike-output.txt` for `*** ERROR compiling` (a BRS syntax slip) and fix before re-running. If the console is held: `lsof -nP -iTCP | grep 8085`.

- [ ] **Step 10: Write `spikes/lifecycle-spike/FINDINGS.md` (facts only)**

Shape (flow-spike precedent): Date; Device (model, OS build from the BEGIN line); Design under test (spec §8); Raw evidence pointer (`capture-probe-init.txt`); then one "Facts of record" bullet per verdict quoting the line verbatim, an INFORMATIVE section, and a "Design consequence" paragraph per question stating which spec statement it confirms or falsifies. Do NOT write design decisions here.

- [ ] **Step 11: Stamp the spec and fold falsified facts**

In the design doc §8 heading add `**EXECUTED <date>** — findings in spikes/lifecycle-spike/FINDINGS.md; <N> PASS / <M> FAIL; falsified: <none|list>`. If Q6a or Q6c FAILED, stop here and raise it: Task 6's `callFunc` hop needs the §12 fallback. If Q4 FAILED (attribute visible in init), add the init-clobber hazard to §2 as a pre-existing bug to record in plan B.

- [ ] **Step 12: Commit (two commits)**

```bash
git add spikes/lifecycle-spike/probe-init
git commit -m "spike: lifecycle-spike probe-init package (init order, callFunc/hasFunc, detach observers)

Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>"
git add spikes/lifecycle-spike/FINDINGS.md spikes/lifecycle-spike/capture-probe-init.txt docs/superpowers/plans/2026-09-04-component-lifecycle-design.md
git commit -m "spikes: lifecycle probe-init executed — facts pinned, design stamped

Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>"
```

---

### Task 2: `hierarchyOverrides` + fix the inherited `onKeyEvent` wrapper (spec §5.2, §5.5)

**Files:**
- Modify: `compiler/ir/backend.brightscript/src/org/jetbrains/kotlin/ir/backend/brs/BrsIntrinsics.kt` (after `findOnKeyEventOverride`, ~line 328)
- Modify: `compiler/ir/backend.brightscript/src/org/jetbrains/kotlin/ir/backend/brs/transformers/irToBrs/IrToBrsTransformer.kt:797-813` (`generateOnKeyEventFunction`)
- Create: `compiler/testData/codegen/brs/components/onKeyEventInheritedWrapper.kt` (+ generated `.brs.txt`)
- Modify: `compiler/ir/backend.brightscript/test/org/jetbrains/kotlin/ir/backend/brs/test/BrsGoldenFileTests.kt` (add `@Test`)

**Interfaces:**
- Produces: `BrsIntrinsics.hierarchyOverrides(irClass: IrClass, name: String, signature: (IrSimpleFunction) -> Boolean = { true }): IrSimpleFunction?` — the nearest REAL override in the USER hierarchy, or null. Tasks 7 and 8 call it with `"onStart"` and `"onStop"`.

- [ ] **Step 1: Add the golden source and test, capture the CURRENT (buggy) output**

`compiler/testData/codegen/brs/components/onKeyEventInheritedWrapper.kt`:
```kotlin
// A concrete user base overrides onKeyEvent; the leaf does not. The leaf's
// generated SceneGraph-facing `function onKeyEvent` must reach the inherited
// override through the slot the BASE's init attached (m.onKeyEvent_Str_Z_k_),
// not shadow it with `return false` (spec 2026-09-04-component-lifecycle §5.5).
import kotlin.brs.GroupComponent

open class KeyBase : GroupComponent() {
    override fun onKeyEvent(key: String, press: Boolean): Boolean = press && key == "OK"
}

class KeyLeaf : KeyBase()
```

In `BrsGoldenFileTests.kt`, next to `componentNoCoroutinesNoPumpAttach`:
```kotlin
    @Test
    fun onKeyEventInheritedWrapper() = runTest("components/onKeyEventInheritedWrapper.kt")
```

Run: `./run-compiler-tests.sh --update` then `grep -n "onKeyEvent" compiler/testData/codegen/brs/components/onKeyEventInheritedWrapper.brs.txt`
Expected (the defect, captured): in the `KeyLeafKt.brs` section, `function onKeyEvent(key as String, press as Boolean) as Boolean` followed by `return false`.

- [ ] **Step 2: Add `hierarchyOverrides` to `BrsIntrinsics`**

```kotlin
    /**
     * The nearest REAL (non-fake, non-abstract) override of member [name] that
     * also satisfies [signature], walking [irClass] and its USER component
     * supertypes. Stops — answering null — at the first stdlib base: a class
     * directly annotated @BrsSceneGraphComponent, or kotlin.brs.ComponentBase.
     * Null means nothing in the user hierarchy overrides the member and the
     * stdlib default applies.
     *
     * Why a hierarchy walk: SceneGraph runs base init() before derived init()
     * over a shared m, so an override declared in a concrete user BASE is
     * attached as a slot the leaf inherits — the leaf's generated wrappers and
     * lifecycle entries must consult the whole chain, not just the leaf
     * (the onKeyEvent leaf-wrapper shadowing defect, spec §5.5).
     */
    fun hierarchyOverrides(
        irClass: IrClass,
        name: String,
        signature: (IrSimpleFunction) -> Boolean = { true },
    ): IrSimpleFunction? {
        var current: IrClass? = irClass
        while (current != null) {
            if (current.hasAnnotation(brsSceneGraphComponentFqn)) return null
            if (current.fqNameWhenAvailable?.asString() == "kotlin.brs.ComponentBase") return null
            val found = current.declarations.filterIsInstance<IrSimpleFunction>().find {
                it.name.asString() == name &&
                    !it.isFakeOverride &&
                    it.modality != Modality.ABSTRACT &&
                    signature(it)
            }
            if (found != null) return found
            current = current.superTypes
                .mapNotNull { it.classOrNull?.owner }
                .firstOrNull { !it.isInterface }
        }
        return null
    }
```
Imports needed in `BrsIntrinsics.kt` if absent: `org.jetbrains.kotlin.descriptors.Modality`, `org.jetbrains.kotlin.ir.util.fqNameWhenAvailable`, `org.jetbrains.kotlin.ir.util.isInterface`, `org.jetbrains.kotlin.ir.types.classOrNull`.

- [ ] **Step 3: Use it in `generateOnKeyEventFunction`**

Replace the `val override = context.intrinsics.findOnKeyEventOverride(irClass)` block and its slot-name derivation with:

```kotlin
        // Walk the USER hierarchy: an override declared in a concrete base is
        // attached by the BASE's init over the shared m (base init runs first);
        // the slot short name is derived against the DECLARING class, not this
        // leaf. Only when nothing in the chain overrides does the wrapper
        // return false.
        val override = context.intrinsics.hierarchyOverrides(irClass, "onKeyEvent") { fn ->
            fn.valueParameters.size == 2 &&
                fn.valueParameters[0].type.isString() &&
                fn.valueParameters[1].type.isBoolean() &&
                fn.returnType.isBoolean()
        }

        val body = if (override != null) {
            val declaringClass = override.parent as IrClass
            val shortName = context.getBrsName(override)
                .removePrefix("${context.getBrsName(declaringClass)}_")

            BrsBlock(mutableListOf(
                BrsReturn(
                    BrsFunctionCall(
                        BrsDotAccess(BrsMRef(), shortName),
                        mutableListOf(BrsIdentifier("key"), BrsIdentifier("press"))
                    )
                )
            ))
        } else {
            BrsBlock(mutableListOf(BrsReturn(BrsBooleanLiteral(false))))
        }
```
Leave `findOnKeyEventOverride` in place if anything else references it (grep); delete it if this was its only caller.

- [ ] **Step 4: Regenerate and verify the golden**

Run: `./run-compiler-tests.sh --update` then `grep -n -A1 "function onKeyEvent" compiler/testData/codegen/brs/components/onKeyEventInheritedWrapper.brs.txt`
Expected: BOTH `KeyBaseKt.brs` and `KeyLeafKt.brs` wrappers read `return m.onKeyEvent_Str_Z_k_(key, press)`. Then run `./run-compiler-tests.sh` (no update) — all 101 pass; `git diff --stat compiler/testData` shows only the new pair.

- [ ] **Step 5: Commit**

```bash
git add compiler/ir/backend.brightscript compiler/testData/codegen/brs/components/onKeyEventInheritedWrapper.*
git commit -m "brs: hierarchyOverrides + inherited onKeyEvent override no longer shadowed by the leaf wrapper

Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>"
```

---

### Task 3: `super.f()` inside components is a static call (spec §5.4)

**Files:**
- Modify: `compiler/ir/backend.brightscript/src/org/jetbrains/kotlin/ir/backend/brs/transformers/irToBrs/IrExpressionToBrsTransformer.kt` (the regular-method-call branch ending at `return BrsMethodCall(receiverExpr, methodName, args)`, ~line 1697)
- Create: `compiler/testData/codegen/brs/components/superDispatchComponent.kt` (+ `.brs.txt`); `@Test` in `BrsGoldenFileTests.kt`
- Modify (kotlin-roku): `../kotlin-roku/src/main/kotlin/com/example/roku/gradle/tasks/ComponentIncludeValidator.kt` + its test

**Interfaces:**
- Produces: nothing new for later tasks; Task 9 test 8 pins it on device.

- [ ] **Step 1: Golden source + test; capture the buggy output**

`compiler/testData/codegen/brs/components/superDispatchComponent.kt`:
```kotlin
// super.f() inside a SceneGraph component must call the BASE's implementation
// statically (every component class emits its members as globals over the
// shared m; SceneGraph makes base functions callable from the derived scope).
// The slot form (m.f_k_) reaches the override's OWN slot and recurses
// (spec 2026-09-04-component-lifecycle §5.4).
import kotlin.brs.GroupComponent

open class SuperBase : GroupComponent() {
    override fun onKeyEvent(key: String, press: Boolean): Boolean = key == "back"
    open fun describe(): String = "base"
}

class SuperLeaf : SuperBase() {
    override fun onKeyEvent(key: String, press: Boolean): Boolean {
        if (press && key == "OK") return true
        return super.onKeyEvent(key, press)
    }
    override fun describe(): String = super.describe() + "+leaf"
}
```
Add `@Test fun superDispatchComponent() = runTest("components/superDispatchComponent.kt")`.

Run: `./run-compiler-tests.sh --update` then `grep -n "onKeyEvent_Str_Z_k_(key, press)\|describe_k_()" compiler/testData/codegen/brs/components/superDispatchComponent.brs.txt`
Expected (defect): inside `SuperLeaf_onKeyEvent_Str_Z_k_` a `m.onKeyEvent_Str_Z_k_(key, press)` call and inside `SuperLeaf_describe_k_` a `m.describe_k_()` call — self-recursion.

- [ ] **Step 2: Emit the static call**

In `IrExpressionToBrsTransformer.visitCall`, at the top of the regular-method-call branch (immediately before `val rawMethodName = function.name.asString()`), insert:

```kotlin
                // super.f(...) inside a SceneGraph component: call the base's
                // implementation as a GLOBAL. The slot form would resolve to the
                // override's own slot on the shared m and recurse. Dependency
                // recording is deliberate and split: a USER base component's
                // function is already in scope through the SceneGraph extends
                // chain, and recording it would pull the base's script into the
                // leaf's <script> list where two `sub init()`s collide (the
                // __kotlinTaskMain rationale); a STDLIB base default
                // (ComponentBase_onKeyEvent_Str_Z_k_) lives in a stdlib file and
                // must be recorded so the closure includes it.
                if (expression.superQualifierSymbol != null && parentClass != null &&
                    context.intrinsics.isSceneGraphComponent(parentClass)
                ) {
                    val target = function.resolveFakeOverride() ?: function
                    val targetClass = target.parent as? IrClass
                    val superArgs = mutableListOf<BrsExpression>()
                    for (i in 0 until expression.valueArgumentsCount) {
                        val arg = expression.getValueArgument(i)
                        superArgs.add(
                            if (arg != null) arg.accept(this, data) else absentArgumentPlaceholder(function, i)
                        )
                    }
                    val targetName = context.getBrsName(target)
                    val targetIsStdlibBase = targetClass != null &&
                        (targetClass.hasAnnotation(brsSceneGraphComponentFqn) ||
                            targetClass.fqNameWhenAvailable?.asString() == "kotlin.brs.ComponentBase")
                    return if (targetIsStdlibBase) {
                        createFunctionCall(targetName, superArgs, context)
                    } else {
                        BrsFunctionCall(BrsIdentifier(targetName), superArgs)
                    }
                }
```
`brsSceneGraphComponentFqn` is the FqName the intrinsics use (`BrsIntrinsics.brsSceneGraphComponentFqn`); if it is private there, expose it as `internal val` or compare `targetClass.hasAnnotation(FqName("kotlin.brs.BrsSceneGraphComponent"))`.

- [ ] **Step 3: Regenerate and verify**

Run: `./run-compiler-tests.sh --update` then grep as in Step 1.
Expected: `SuperBase_onKeyEvent_Str_Z_k_(key, press)` and `SuperBase_describe_k_()` as direct calls; the SuperLeaf deps.json does NOT list a `SuperBase` script (no recorded edge to the user base). `./run-compiler-tests.sh` — 102 pass.

- [ ] **Step 4: Teach the include validator the extends chain (kotlin-roku)**

The validator cross-checks each component's calls against definitions in ITS `<script>` URIs only. A leaf's static call to `SuperBase_describe_k_` is defined in the base component's script, which the leaf's XML does not list — a false finding in strict mode. In `ComponentIncludeValidator.kt`:

1. Add `fun parseExtends(xml: String): String?` — regex `extends\s*=\s*"([^"]+)"` on the `<component` tag, returning the name or null.
2. Give `ComponentScripts` an `extends: String?` field; populate it in `ValidateComponentIncludesTask` where `ComponentScripts(...)` is constructed (both loops), passing `ComponentIncludeValidator.parseExtends(xml.readText())`.
3. In `validate(...)`, before checking a component, compute its EFFECTIVE script set: its own URIs plus, walking `extends` case-insensitively through the `components` list until the base is not a packaged component (a SceneGraph built-in), each ancestor's URIs. Check references against definitions in the effective set. Document in the KDoc: "SceneGraph: all functions defined in a component that is extended can be called from the derived component (Creating custom components page)".
4. Unit test in `ValidateComponentIncludesTaskTest.kt` (mirror an existing case): base XML `<component name="B" extends="Group">` with script `b.brs` defining `B_describe_k_`; leaf XML `<component name="L" extends="B">` with script `l.brs` calling `B_describe_k_()`; assert zero findings; and a negative: leaf calling `Zzz_k_()` defined nowhere → one finding.

Run the plugin tests: `cd ../kotlin-roku && ./gradlew test --tests '*ValidateComponentIncludes*'`. Then republish: `cd ../roku-test-app && ./rebuild-all.sh --plugin`.

- [ ] **Step 5: Commit (Kotlin repo, then kotlin-roku)**

```bash
git add compiler/ir/backend.brightscript compiler/testData/codegen/brs/components/superDispatchComponent.*
git commit -m "brs: super.f() in components calls the base implementation statically (no self-recursion)

Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>"
cd ../kotlin-roku && git add src && git commit -m "plugin: include validator treats extends-chain scripts as in scope

Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>" && cd ../Kotlin
```

---

### Task 4: Unconditional lifecycle attach (spec §5.1) — stdlib file skeleton + injection

**Files:**
- Create: `libraries/stdlib/brs/src/kotlin/brs/lifecycle/ComponentLifecycle.kt` (skeleton; Task 5 grows it)
- Modify: `IrToBrsTransformer.kt:995-1010` (the gated `__kotlinPumpAttach` block) and, if it becomes unused, `fileUsesCoroutines` (T:242-283) + `genCtx.currentFileUsesCoroutines` (set at T:112)
- Rename golden: `components/componentNoCoroutinesNoPumpAttach.{kt,brs.txt}` → `components/componentAttachUnconditional.{kt,brs.txt}`; update its `@Test`
- Regenerate every component golden whose init carried `__kotlinPumpAttach`

**Interfaces:**
- Produces: `@BrsStatic public fun __kotlinComponentAttach(top: RoSGNode, global: RoSGNode)` in `kotlin.brs`, called first in every non-task component's `init()`. `LifecycleRegistry.attached` (internal).

- [ ] **Step 1: Write the stdlib skeleton**

`libraries/stdlib/brs/src/kotlin/brs/lifecycle/ComponentLifecycle.kt`:
```kotlin
/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs

import kotlin.brs.roku.RoSGNode
import kotlin.coroutines.pump.PumpScheduler

/**
 * Component lifecycle runtime (design of record:
 * docs/superpowers/plans/2026-09-04-component-lifecycle-design.md, §4).
 *
 * ONE file by the include-closure law: the compiler-injected attach call below
 * records the edge that pulls this file into every render component's closure,
 * and the name-registered observer handler added in the gate section must live
 * beside its registration.
 */

/** Per-component-instance state (object singletons live on GetGlobalAA — per instance on the render thread). */
internal object LifecycleRegistry {
    /** Set by the compiler-injected attach as init()'s first statement. */
    internal var attached: Boolean = false
}

/**
 * Component-init entry point, injected UNCONDITIONALLY as the first statement
 * of every render component's generated init() (task components excluded).
 * Performs the pump attach internally — this supersedes the coroutine-scan-
 * gated `__kotlinPumpAttach` injection and closes its helper-file hole. Cost:
 * one call, two ref stores; backend resolution and timers stay deferred to
 * the first real wakeup.
 */
@BrsStatic
public fun __kotlinComponentAttach(top: RoSGNode, global: RoSGNode) {
    PumpScheduler.attach(top, global)
    LifecycleRegistry.attached = true
}
```

- [ ] **Step 2: Rebuild so the prebuilt stdlib klib carries the new function**

Run: `./rebuild.sh` (expect BUILD SUCCESSFUL through step 9; steps 8/9 compile-gate kotlin-test-brs and flow).

- [ ] **Step 3: Replace the gated injection**

In `transformComponentInitBlock`, replace the block

```kotlin
        if (genCtx.currentFileUsesCoroutines && !isConcreteTaskComponent(irClass)) {
            bodyStatements.add(
                BrsExpressionStatement(
                    createFunctionCall(
                        "__kotlinPumpAttach",
```
with
```kotlin
        // Lifecycle attach (spec §5.1): UNCONDITIONAL for every render component
        // that emits an init() — abstract user intermediates included (they sit
        // in the SceneGraph extends chain; the attach is idempotent). Performs
        // the pump attach internally, so the per-file coroutine scan no longer
        // gates anything here (its helper-file hole is closed by construction).
        // Task components are excluded as before: their work runs on the task
        // thread, where the run loop pumps.
        if (!isConcreteTaskComponent(irClass)) {
            bodyStatements.add(
                BrsExpressionStatement(
                    createFunctionCall(
                        "__kotlinComponentAttach",
                        mutableListOf(
                            BrsDotAccess(BrsMRef(), "top"),
                            BrsDotAccess(BrsMRef(), "global")
                        ),
                        context
                    )
                )
            )
        }
```
Then `grep -n "currentFileUsesCoroutines\|fileUsesCoroutines" compiler/ir/backend.brightscript/src -r`. If the injection was the only consumer, delete `fileUsesCoroutines`, `isCoroutineMachineryFqName`, the `currentFileUsesCoroutines` field and its assignment at T:112, and their KDoc.

- [ ] **Step 4: Rename the negative-control golden**

```bash
git mv compiler/testData/codegen/brs/components/componentNoCoroutinesNoPumpAttach.kt compiler/testData/codegen/brs/components/componentAttachUnconditional.kt
git rm -q compiler/testData/codegen/brs/components/componentNoCoroutinesNoPumpAttach.brs.txt
```
Edit the new `.kt` header comment to: `// A component whose file never references coroutine machinery STILL gets the lifecycle attach (__kotlinComponentAttach) as init()'s first statement — the attach is unconditional (spec 2026-09-04-component-lifecycle §5.1) and absorbs the pump attach.` Rename the `@Test` to `componentAttachUnconditional` with the new path.

- [ ] **Step 5: Regenerate and audit the golden diff**

Run: `./run-compiler-tests.sh --update`, then:
```bash
grep -rc "__kotlinPumpAttach" compiler/testData/codegen/brs | grep -v ":0"     # expect NO output
git diff --stat compiler/testData/codegen/brs
```
Expected: every component golden's init gains/renames to `__kotlinComponentAttach(m.top, m.global)` as the first statement (after `functionName` for tasks — tasks are unchanged), and coroutine-free components' `deps.json` now list the lifecycle file and its transitive stdlib deps. Inspect two diffs by eye (one coroutine-using, one coroutine-free) to confirm nothing else moved. `./run-compiler-tests.sh` — 102 pass.

- [ ] **Step 6: Commit**

```bash
git add libraries/stdlib/brs/src/kotlin/brs/lifecycle compiler/ir/backend.brightscript compiler/testData/codegen/brs libraries/stdlib/brs-prebuilt
git commit -m "stdlib+brs: unconditional __kotlinComponentAttach in every render component init (absorbs pump attach)

Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>"
```

---

### Task 5: Hooks + registry + the `awaitReady` gate (spec §3, §4)

**Files:**
- Modify: `libraries/stdlib/brs/src/kotlin/brs/SceneComponent.kt` (ComponentBase: add `onStart`/`onStop` after `onKeyEvent`)
- Modify: `libraries/stdlib/brs/src/kotlin/brs/roku/SceneGraph.kt` (`hasFunc` in `ISGNodeDict` after the 1-arg `callFunc`, and the `RoSGNode` override list)
- Modify: `libraries/stdlib/brs/src/kotlin/brs/lifecycle/ComponentLifecycle.kt` (grow the skeleton)
- Create: `libraries/stdlib/brs/test/kotlin/lifecycle/ComponentLifecycleTest.kt`; register in `libraries/stdlib/brs/test/kotlin/TestMain.kt`

**Interfaces:**
- Produces (all `kotlin.brs`): `protected open suspend fun ComponentBase.onStart()`, `protected open fun ComponentBase.onStop()`; `public suspend fun ComponentBase.awaitReady()`; `@PublishedApi internal fun kotlinLifecycleClaimDriver(): Boolean`; `internal class DependencyTicket(label, rearm)` + `internal fun kotlinLifecycleRegisterDependency(label: String, rearm: () -> Unit): DependencyTicket`; `internal fun kotlinLifecycleOnRetire(hook: () -> Unit)`; `internal fun kotlinLifecycleIsRetired(): Boolean`; `public fun kotlinLifecycleWatchdogFires(): Int`; `public fun kotlinLifecycleWatchdogMillis(ms: Int)`; `public const val LIFECYCLE_INPUTS_READY_FIELD = "__kotlinInputsReady"`; `RoSGNode.hasFunc(name): Boolean`. Task 7's synthesized driver calls `kotlinLifecycleClaimDriver`, `awaitReady`, `onStart`.

- [ ] **Step 1: Write the failing unit tests**

`libraries/stdlib/brs/test/kotlin/lifecycle/ComponentLifecycleTest.kt`:
```kotlin
/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.lifecycle

import kotlin.brs.ComponentBase
import kotlin.brs.awaitReady
import kotlin.brs.kotlinLifecycleWatchdogFires
import kotlin.brs.kotlinLifecycleWatchdogMillis
import kotlin.brs.retire
import kotlin.brs.revive
import kotlin.brs.roku.RoSGNode
import kotlin.coroutines.builders.runBlocking
import kotlin.test.*

/**
 * Component lifecycle — the MAIN-THREAD-legal subset (runBlocking regime, no
 * ambient render component): guided context ISEs, the hasFunc-based retire
 * guard on a plain node, and the watchdog test hooks. Gate/driver/retire
 * round trips are render-thread-only by design: E2E Suite 11.
 */
fun TestRunner.componentLifecycleTests() {
    suite("ComponentLifecycle (awaitReady/retire/revive)") {
        test("awaitReady outside component context throws the guided ISE") {
            var thrown: Throwable? = null
            runBlocking {
                try {
                    lifecycleProbeAwait()
                } catch (e: Throwable) {
                    thrown = e
                }
            }
            assertTrue(thrown is IllegalStateException, "awaitReady off-component should throw IllegalStateException")
            assertEquals(
                "awaitReady must be called from a render-thread component context (like runTask)",
                "${thrown?.message}"
            )
        }

        test("retire on a plain node throws the guided not-a-component ISE") {
            val node = RoSGNode.create("Node")
            var thrown: Throwable? = null
            try {
                retire(node)
            } catch (e: Throwable) {
                thrown = e
            }
            assertTrue(thrown is IllegalStateException, "retire on a plain node should throw IllegalStateException")
            assertTrue(
                "${thrown?.message}".contains("is not a Kotlin render component"),
                "guided message expected, got: ${thrown?.message}"
            )
        }

        test("revive on a plain node throws the guided not-a-component ISE") {
            val node = RoSGNode.create("Node")
            var thrown: Throwable? = null
            try {
                revive(node)
            } catch (e: Throwable) {
                thrown = e
            }
            assertTrue(thrown is IllegalStateException, "revive on a plain node should throw IllegalStateException")
        }

        test("watchdog hooks: counter starts at zero and millis override is accepted") {
            assertEquals(0, kotlinLifecycleWatchdogFires())
            kotlinLifecycleWatchdogMillis(400)
            kotlinLifecycleWatchdogMillis(30_000)
        }
    }
}

// awaitReady is an extension on ComponentBase; off-thread there is no
// component, so the probe supplies a throwaway receiver — the guard fires on
// the ambient oracle before the receiver is ever touched.
private object LifecycleNoComponent : ComponentBase()

private suspend fun lifecycleProbeAwait() {
    LifecycleNoComponent.awaitReady()
}
```
If `ComponentBase` cannot be subclassed by a plain `object` in test code (constructor visibility), declare `private class LifecycleNoComponent : ComponentBase()` and construct it instead.

Register in `libraries/stdlib/brs/test/kotlin/TestMain.kt`: `import test.lifecycle.componentLifecycleTests` and a `componentLifecycleTests()` call after `coroutineFieldShadowingTests()`.

Run: `./run-stdlib-tests.sh --build-only`
Expected: compile FAILURE — `awaitReady`, `retire`, `revive`, `kotlinLifecycleWatchdog*` unresolved.

- [ ] **Step 2: Add the hooks to `ComponentBase`**

In `SceneComponent.kt`, after `onKeyEvent`:
```kotlin
    /**
     * Lifecycle: fires once per ACTIVATION, on the render thread, as a child
     * coroutine of [componentScope], after init() has returned, every required
     * input is set, and every dependency registered during init has resolved.
     * Earliest: the first pump tick after init. Fires again after every
     * [revive]. The compiler launches it only for classes whose hierarchy
     * overrides it — a component that does not override pays no coroutine.
     * An uncaught failure prints the standard
     * `[kotlin.coroutines] Unhandled exception in coroutine` line; the
     * component stays alive (supervisor root). [retire] cancels an onStart
     * still running.
     *
     * Per-activation work belongs here; init {} is one-time structural setup.
     */
    protected open suspend fun onStart() {}

    /**
     * Lifecycle: runs synchronously inside [retire], BEFORE the component
     * scope is cancelled, so live state is still readable. Do not launch here —
     * the scope dies immediately after. Runs again on every later retire.
     */
    protected open fun onStop() {}
```

- [ ] **Step 3: Bind `hasFunc`**

In `SceneGraph.kt`, `ISGNodeDict` after the 1-arg `callFunc`:
```kotlin
    /**
     * Whether [functionName] is a callable interface function of this node's
     * component (ifSGNodeDict.hasFunc). False on plain nodes and on components
     * that do not declare it — the guard [retire]/[revive] use before callFunc.
     */
    public fun hasFunc(functionName: String): Boolean
```
and in the `RoSGNode` override block after the `callFunc` overrides: `override fun hasFunc(functionName: String): Boolean`.

- [ ] **Step 4: Grow `ComponentLifecycle.kt` into the gate**

Replace the file's body below the header with:
```kotlin
package kotlin.brs

import kotlin.brs.roku.RoSGNode
import kotlin.brs.roku.RoSGNodeEvent
import kotlin.coroutines.Continuation
import kotlin.coroutines.DisposableHandle
import kotlin.coroutines.ParkedContinuation
import kotlin.coroutines.delay.DelayTracker
import kotlin.coroutines.ensureActive
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn
import kotlin.coroutines.pump.PumpScheduler
import kotlin.coroutines.registerCallerCancel

/**
 * The ready marker: one boolean XML field on every input-bearing component
 * TYPE (plan B declares it); absent on input-less types, which is how the
 * stdlib learns whether a class has inputs at all (spec §4).
 */
public const val LIFECYCLE_INPUTS_READY_FIELD: String = "__kotlinInputsReady"

/** Bare-named per-component entries the compiler generates (callFunc targets). */
internal const val LIFECYCLE_RETIRE_FUNCTION: String = "__kotlinRetire"
internal const val LIFECYCLE_REVIVE_FUNCTION: String = "__kotlinRevive"

/**
 * One declared dependency of a component (spec §13). Registered during init
 * (spec 2's `by sharedService` delegate is the first registrant); [resolve]
 * may run synchronously inside registration or later from any callback in
 * the component's context. [rearm] runs on every revive: drop cached state,
 * re-attempt resolution, arm for a later resolve on a miss.
 */
internal class DependencyTicket(
    internal val label: String,
    internal val rearm: () -> Unit,
) {
    internal var resolved: Boolean = false

    /** Idempotent. Wakes parked awaitReady callers when the whole gate is open. */
    internal fun resolve() {
        if (resolved) return
        resolved = true
        lifecycleWakeIfReady()
    }
}

/** Per-component-instance state (object singletons live on GetGlobalAA — per instance on the render thread). */
internal object LifecycleRegistry {
    internal var attached: Boolean = false
    internal var retired: Boolean = false
    internal var driverClaimed: Boolean = false
    /** Monotonic activation counter; bumps on retire. Watchdog callbacks compare against it. */
    internal var activation: Int = 0
    internal var markerObserved: Boolean = false
    internal var watchdogArmedFor: Int = -1
    internal var watchdogMs: Int = 30_000
    internal var watchdogFires: Int = 0
    internal val tickets: ArrayList<DependencyTicket> = ArrayList()
    internal val parked: ArrayList<ParkedContinuation> = ArrayList()
    internal val retireHooks: ArrayList<() -> Unit> = ArrayList()
}

@BrsStatic
public fun __kotlinComponentAttach(top: RoSGNode, global: RoSGNode) {
    PumpScheduler.attach(top, global)
    LifecycleRegistry.attached = true
}

// ---------------------------------------------------------------------------
// The gate
// ---------------------------------------------------------------------------

/** Inputs are ready when the class declares none (no marker field) or the marker is true. */
internal fun lifecycleInputsReady(top: RoSGNode): Boolean {
    if (!top.hasField(LIFECYCLE_INPUTS_READY_FIELD)) return true
    return top.getField(LIFECYCLE_INPUTS_READY_FIELD) == true
}

internal fun lifecycleGateOpen(top: RoSGNode): Boolean {
    val reg = LifecycleRegistry
    if (reg.retired) return false
    if (!lifecycleInputsReady(top)) return false
    for (ticket in reg.tickets) {
        if (!ticket.resolved) return false
    }
    return true
}

/** Re-checks the gate and drains every parked awaiter when it is open. */
internal fun lifecycleWakeIfReady() {
    val top = PumpScheduler.hostTopOrNull() ?: return
    if (!lifecycleGateOpen(top)) return
    val reg = LifecycleRegistry
    if (reg.parked.size == 0) return
    val waiters = ArrayList<ParkedContinuation>()
    waiters.addAll(reg.parked)
    reg.parked.clear()
    for (parked in waiters) {
        parked.tryResume(Unit)
    }
}

/**
 * Marker-field observer handler. Registered BY NAME below, so it lives in this
 * file (include-closure law); runs in the observing component's context and
 * therefore resolves that component's registry instance.
 */
internal fun onKotlinInputsReady(event: RoSGNodeEvent) {
    lifecycleWakeIfReady()
}

private fun lifecycleArmMarkerObserver(top: RoSGNode) {
    val reg = LifecycleRegistry
    if (reg.markerObserved) return
    if (!top.hasField(LIFECYCLE_INPUTS_READY_FIELD)) return
    reg.markerObserved = true
    top.observeFieldScoped(LIFECYCLE_INPUTS_READY_FIELD, brsName(::onKotlinInputsReady))
}

/** Once per activation: DelayTracker has no deregistration, so the callback self-checks (registry-miss idiom). */
private fun lifecycleArmWatchdog(top: RoSGNode) {
    val reg = LifecycleRegistry
    if (reg.watchdogArmedFor == reg.activation) return
    val armedFor = reg.activation
    reg.watchdogArmedFor = armedFor
    val ms = reg.watchdogMs
    DelayTracker.current.register(ms.toLong()) {
        val stillCurrent = !reg.retired && reg.activation == armedFor
        if (stillCurrent) {
            if (!lifecycleGateOpen(top)) {
                var unresolved = ""
                for (ticket in reg.tickets) {
                    if (!ticket.resolved) unresolved = unresolved + ticket.label + " "
                }
                var inputs = "set"
                if (!lifecycleInputsReady(top)) {
                    inputs = "UNSET (created outside its Kotlin constructor?)"
                }
                println(
                    "[kotlin.lifecycle] ${top.subtype()}(id=${top.getField("id")}) not ready after " +
                        "${ms / 1000}s — inputs $inputs, unresolved: $unresolved"
                )
                reg.watchdogFires = reg.watchdogFires + 1
            }
        }
    }
}

/** Removes a parked awaiter on cancellation (the caller-cancel path resumes it exceptionally). */
private class LifecycleParkHandle(private val parked: ParkedContinuation) : DisposableHandle {
    override fun dispose() {
        LifecycleRegistry.parked.remove(parked)
    }
}

/**
 * Suspends until the current activation's gate is open: inputs set (or none
 * declared) AND every registered dependency resolved. Re-entrant; returns
 * immediately once open. The compiler-synthesized onStart driver awaits this;
 * a coroutine launched from an observer handler may await it too. Render-
 * thread component context only (guided ISE otherwise). Cancellation of the
 * calling job (retire cancels the scope) wakes a parked call with
 * CancellationException.
 */
public suspend fun ComponentBase.awaitReady() {
    return suspendCoroutineUninterceptedOrReturn { continuation ->
        val top = PumpScheduler.hostTopOrNull()
            ?: throw IllegalStateException(
                "awaitReady must be called from a render-thread component context (like runTask)"
            )
        continuation.context.ensureActive()
        if (lifecycleGateOpen(top)) {
            Unit
        } else {
            @Suppress("UNCHECKED_CAST")
            val parked = ParkedContinuation(continuation as Continuation<Any?>)
            LifecycleRegistry.parked.add(parked)
            parked.handles.add(LifecycleParkHandle(parked))
            lifecycleArmMarkerObserver(top)
            lifecycleArmWatchdog(top)
            registerCallerCancel(parked, continuation.context)
            parked.finish()
        }
    }
}

/**
 * Once-per-activation claim for the compiler-synthesized onStart driver: a
 * concrete base and its concrete subclass both emit a driver call from their
 * inits; the FIRST (the base's — base init runs first) claims and launches,
 * the second returns false. The launched body runs after the whole init
 * cascade, when the leaf's slot attachments are in place, so `onStart()`
 * resolves to the most-derived override. False while retired.
 */
@PublishedApi
internal fun kotlinLifecycleClaimDriver(): Boolean {
    val reg = LifecycleRegistry
    if (reg.retired) return false
    if (reg.driverClaimed) return false
    reg.driverClaimed = true
    return true
}

// ---------------------------------------------------------------------------
// Spec-2 interface (§13): dependency tickets, retire hooks
// ---------------------------------------------------------------------------

/** Init-time only: after the driver fired, a dependency nobody will wait for is a declaration bug. */
internal fun kotlinLifecycleRegisterDependency(label: String, rearm: () -> Unit): DependencyTicket {
    val reg = LifecycleRegistry
    if (reg.driverClaimed) {
        throw IllegalStateException(
            "lifecycle: dependency '$label' registered after onStart was driven — declare dependencies " +
                "during init (by sharedService { } as a property delegate)"
        )
    }
    val ticket = DependencyTicket(label, rearm)
    reg.tickets.add(ticket)
    return ticket
}

/** Runs inside retire after onStop and before scope cancellation (spec 2 disarms doorbell observers here). */
internal fun kotlinLifecycleOnRetire(hook: () -> Unit) {
    LifecycleRegistry.retireHooks.add(hook)
}

internal fun kotlinLifecycleIsRetired(): Boolean = LifecycleRegistry.retired

// ---------------------------------------------------------------------------
// Test hooks (kotlinScopeWatchdog* precedent)
// ---------------------------------------------------------------------------

/** Per-component count of watchdog lines printed. Assert the counter, not console text. */
public fun kotlinLifecycleWatchdogFires(): Int = LifecycleRegistry.watchdogFires

/** Per-component watchdog deadline override (default 30000). */
public fun kotlinLifecycleWatchdogMillis(ms: Int) {
    LifecycleRegistry.watchdogMs = ms
}
```
Note `parked.tryResume`, `parked.handles`, `parked.finish`, `ParkedContinuation`, `registerCallerCancel` are `internal` in `kotlin.coroutines` — same stdlib module, accessible (ComponentMailbox precedent). The `retire`/`revive` functions the tests reference are added in Task 6; to compile Step 5 now, add TEMPORARY stubs at the end of the file that Task 6 replaces:
```kotlin
// Task 6 replaces these two with the real hop.
public fun retire(node: RoSGNode) {
    throw IllegalStateException("retire: node '${node.getField("id")}' (${node.subtype()}) is not a Kotlin render component — retire/revive target components compiled from ComponentBase subclasses")
}
public fun revive(node: RoSGNode) {
    throw IllegalStateException("revive: node '${node.getField("id")}' (${node.subtype()}) is not a Kotlin render component — retire/revive target components compiled from ComponentBase subclasses")
}
```

- [ ] **Step 5: Rebuild, build the tests, run on device**

Run: `./rebuild.sh` (expect success; step 8/9 gates green) then `./run-stdlib-tests.sh --build-only` (compiles) then `./run-stdlib-tests.sh` (device).
Expected: the new suite reports 4 passed; total 614 tests / 63 suites; sentinel FRESH.

- [ ] **Step 6: Commit**

```bash
git add libraries/stdlib/brs/src libraries/stdlib/brs/test/kotlin libraries/stdlib/brs-prebuilt
git commit -m "stdlib: ComponentBase.onStart/onStop, lifecycle registry, awaitReady gate, hasFunc binding

Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>"
```

---

### Task 6: `retire`/`revive` implementations + scope reset + `exposeScope` re-callable (spec §4)

**Files:**
- Modify: `libraries/stdlib/brs/src/kotlin/brs/lifecycle/ComponentLifecycle.kt` (replace the Task 5 stubs)
- Modify: `libraries/stdlib/brs/src/kotlin/brs/ComponentCoroutines.kt` (`ComponentScopeHolder` → `internal`; add `cancelAndResetComponentScope()`)
- Modify: `libraries/stdlib/brs/src/kotlin/brs/scope/ScopeApi.kt` KDoc ("one OPEN host") and `ScopeHostImpl.kt` (nothing structural; retire nulls the holder)

**Interfaces:**
- Produces: `@BrsStatic public fun __kotlinRetireImpl()`, `@BrsStatic public fun __kotlinReviveImpl()` (called by Task 8's generated entries, in the CHILD's context); `public fun retire(node: RoSGNode)`, `retire(component: ComponentBase)`, `revive(node: RoSGNode)`, `revive(component: ComponentBase)`; `internal fun cancelAndResetComponentScope()`.

- [ ] **Step 1: Scope reset in `ComponentCoroutines.kt`**

Change `private object ComponentScopeHolder` to `internal object ComponentScopeHolder` and add after `componentScope()`:
```kotlin
/**
 * Retire support: cancels the component scope (the cancel cascades into every
 * parked runTask/flowOn/StateFlow/ScopeHost request) and CLEARS the holder,
 * so the next launch {} on this component creates a fresh supervisor scope.
 * A retired-then-revived component therefore gets working coroutines again
 * (spec §4: the scope self-heals). No-op when no scope was ever created.
 */
internal fun cancelAndResetComponentScope() {
    val existing = ComponentScopeHolder.scope ?: return
    ComponentScopeHolder.scope = null
    val job = existing.coroutineContext[Job]
    if (job != null) {
        job.cancel()
    }
}
```
(`Job` is already imported in that file.) Clear the holder BEFORE cancelling so a cancel handler that launches gets a fresh scope, not the dying one.

- [ ] **Step 2: Replace the stubs in `ComponentLifecycle.kt` with the real hop**

Add imports `kotlin.brs.scope.ScopeHostHolder`, `kotlin.brs.scope.ScopeHostImpl`, `kotlin.brs.scope.SCOPE_INBOX_FIELD` (make the constant and the two types `internal` if they are private; they are in the same module). Then:

```kotlin
// ---------------------------------------------------------------------------
// Retire / revive — the child-side impls run INSIDE the child's context via the
// compiler-generated bare entries `__kotlinRetire`/`__kotlinRevive`, which call
// these after (retire) / before (revive) the user hook + driver.
// ---------------------------------------------------------------------------

/**
 * Child-side retire body (the generated `__kotlinRetire` already ran the
 * user's onStop, try/caught). Order: retire hooks (spec 2 disarms doorbell
 * observers) → close + clear the exposed ScopeHost (children settle
 * ScopeClosedException; the inbox observer is unarmed so a later exposeScope
 * can re-arm it) → cancel + reset the component scope (STOPs task threads,
 * deregisters StateFlow collectors, wakes parked awaitReady calls with CE)
 * → retired flag + activation bump. Idempotent.
 */
@BrsStatic
public fun __kotlinRetireImpl() {
    val reg = LifecycleRegistry
    if (reg.retired) return
    val hooks = ArrayList<() -> Unit>()
    hooks.addAll(reg.retireHooks)
    for (hook in hooks) {
        hook()
    }
    val hostState = ScopeHostHolder.state
    if (hostState != null) {
        ScopeHostImpl(hostState).close()
        ScopeHostHolder.state = null
        val top = PumpScheduler.hostTopOrNull()
        if (top != null && top.hasField(SCOPE_INBOX_FIELD)) {
            top.unobserveFieldScoped(SCOPE_INBOX_FIELD)
        }
    }
    cancelAndResetComponentScope()
    reg.retired = true
    reg.activation = reg.activation + 1
}

/**
 * Child-side revive body (the generated `__kotlinRevive` relaunches the
 * onStart driver AFTER this returns, when the hierarchy overrides onStart).
 * No-op on a live component. Tickets are marked unresolved and re-armed
 * (synchronous resolutions count immediately); inputs are unchanged across
 * the cycle, so the marker still holds.
 */
@BrsStatic
public fun __kotlinReviveImpl() {
    val reg = LifecycleRegistry
    if (!reg.retired) return
    reg.retired = false
    reg.driverClaimed = false
    reg.watchdogArmedFor = -1
    for (ticket in reg.tickets) {
        ticket.resolved = false
    }
    val tickets = ArrayList<DependencyTicket>()
    tickets.addAll(reg.tickets)
    for (ticket in tickets) {
        ticket.rearm()
    }
}

private fun lifecycleRequireComponent(node: RoSGNode, verb: String) {
    if (!node.hasFunc(LIFECYCLE_RETIRE_FUNCTION)) {
        throw IllegalStateException(
            "$verb: node '${node.getField("id")}' (${node.subtype()}) is not a Kotlin render component — " +
                "retire/revive target components compiled from ComponentBase subclasses"
        )
    }
}

/**
 * Parent-side (any thread — callFunc rendezvouses into the child's owning
 * thread and runs with the CHILD's m): onStop → retire hooks → close exposed
 * scope → cancel + reset the component scope → retired. Does NOT remove the
 * node; the caller keeps ownership of the tree. Idempotent. Recycling law:
 * `retire → removeChild → reconfigure var fields → appendChild → revive`.
 * Guided ISE on a node that is not a Kotlin render component (hasFunc).
 */
public fun retire(node: RoSGNode) {
    lifecycleRequireComponent(node, "retire")
    node.callFunc(LIFECYCLE_RETIRE_FUNCTION)
}

/** Typed-handle form: a createComponent/constructor handle IS the node; a component `this` is its m (see [componentNodeOf]). */
public fun retire(component: ComponentBase) {
    retire(componentNodeOf(component))
}

/**
 * Parent-side counterpart of [retire], called after re-adding a retired node:
 * clears retired + the driver once-flag, re-arms every dependency, relaunches
 * the onStart driver. No-op on a live component. Guided ISE on a non-component.
 */
public fun revive(node: RoSGNode) {
    lifecycleRequireComponent(node, "revive")
    node.callFunc(LIFECYCLE_REVIVE_FUNCTION)
}

public fun revive(component: ComponentBase) {
    revive(componentNodeOf(component))
}

/**
 * A ComponentBase-typed value has two runtime faces: a creation handle
 * (createComponent<T>() / a constructor call) IS the roSGNode; a component
 * `this` is the m-scope AA whose `.top` is the node. Discriminate at runtime
 * so both `retire(screen)` and `retire(this)` do the right thing.
 */
@BrsInline("if type(component) = \"roSGNode\" then return component else return component.top")
private external fun componentNodeOf(component: ComponentBase): RoSGNode
```
If `SCOPE_INBOX_FIELD`, `ScopeHostHolder` or `ScopeHostImpl` are `private`, widen them to `internal` in `ScopeHostImpl.kt`/`ScopeWire.kt` (same module).

- [ ] **Step 3: Amend `exposeScope`'s KDoc**

In `ScopeApi.kt`, change the "One host per component" sentence to: `One OPEN host per component: a second call while a host is open throws [IllegalStateException]; after [retire] closed and cleared the host, exposeScope may be called again (the recyclable-owner idiom: expose in onStart). Retire also unarms the field-carrier inbox observer so the re-arm below does not double-register.` Update the ISE text to `"…called twice on this component — one OPEN scope host per component; reuse the ScopeHost returned by the first call, or retire() the component before exposing again"`.

- [ ] **Step 4: Rebuild + device unit run**

Run: `./rebuild.sh` then `./run-stdlib-tests.sh`.
Expected: 614/63 green (the retire/revive ISE tests now exercise `hasFunc` on a plain node — the binding's first device pin).

- [ ] **Step 5: Commit**

```bash
git add libraries/stdlib/brs/src libraries/stdlib/brs-prebuilt
git commit -m "stdlib: retire/revive impls over callFunc + hasFunc guard, component scope reset, exposeScope re-callable after retire

Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>"
```

---

### Task 7: Compiler — synthesize the onStart driver (spec §5.3)

**Files:**
- Modify: `compiler/ir/backend.brightscript/src/org/jetbrains/kotlin/ir/backend/brs/BrsSymbols.kt` (four `OrNull` accessors near `sharedAcquireOrNull`)
- Create: `compiler/ir/backend.brightscript/src/org/jetbrains/kotlin/ir/backend/brs/lower/BrsComponentLifecycleLowering.kt`
- Modify: `compiler/ir/backend.brightscript/src/org/jetbrains/kotlin/ir/backend/brs/lower/BrsLoweringPhases.kt` (register at 0.054, before `BrsRunTaskCallLowering`)
- Create: `compiler/testData/codegen/brs/components/onStartDriver.kt` (+ `.brs.txt`); `@Test`

**Interfaces:**
- Consumes: `kotlin.brs.launch` (`ComponentBase.launch(context, block)`), `kotlin.brs.awaitReady`, `kotlin.brs.kotlinLifecycleClaimDriver`, `kotlin.coroutines.CoroutineScope`.
- Produces: a synthesized member `__kotlinStartDriver()` (BRS global `<Class>___kotlinStartDriver_k_`, attached as `m.__kotlinStartDriver_k_` by the normal attachment loop) on every concrete render component whose hierarchy overrides `onStart`, plus an appended init block calling it. Task 8's `__kotlinRevive` looks the member up by NAME `__kotlinStartDriver` in `irClass.declarations`.

- [ ] **Step 1: Golden source + test**

`compiler/testData/codegen/brs/components/onStartDriver.kt`:
```kotlin
// The compiler-synthesized onStart driver (spec 2026-09-04-component-lifecycle §5.3):
// a class whose hierarchy overrides onStart gets a `__kotlinStartDriver` member
// (`if (!kotlinLifecycleClaimDriver()) return; launch { awaitReady(); onStart() }`)
// and an init-tail call to it. A concrete base + concrete leaf BOTH emit one (the
// claim makes the second a no-op at runtime); a component that never overrides
// onStart gets nothing.
import kotlin.brs.GroupComponent
import kotlin.brs.SGStringField

open class StartBase : GroupComponent() {
    @SGStringField
    var seen: String = ""

    override suspend fun onStart() {
        seen = "base"
    }
}

class StartLeaf : StartBase() {
    override suspend fun onStart() {
        seen = "leaf"
    }
}

class StartInherits : StartBase()

class NoStart : GroupComponent() {
    @SGStringField
    var label: String = ""
}
```
Add `@Test fun onStartDriver() = runTest("components/onStartDriver.kt")`. Run `./run-compiler-tests.sh --update` to capture the pre-change output (no driver anywhere); `grep -c "__kotlinStartDriver" compiler/testData/codegen/brs/components/onStartDriver.brs.txt` → `0`.

- [ ] **Step 2: Symbols**

In `BrsSymbols.kt`, next to `sharedAcquireOrNull`:
```kotlin
    private val kotlinBrsFqn = FqName("kotlin.brs")
    private val kotlinCoroutinesFqn = FqName("kotlin.coroutines")

    /** kotlin.brs.launch — ComponentBase.launch(context, block) (lifecycle driver synthesis). */
    val componentLaunchOrNull: IrSimpleFunctionSymbol? by lazy { findOptionalFunction(kotlinBrsFqn, "launch") }

    /** kotlin.brs.awaitReady — the lifecycle gate (lifecycle driver synthesis). */
    val awaitReadyOrNull: IrSimpleFunctionSymbol? by lazy { findOptionalFunction(kotlinBrsFqn, "awaitReady") }

    /** kotlin.brs.kotlinLifecycleClaimDriver — once-per-activation claim (lifecycle driver synthesis). */
    val kotlinLifecycleClaimDriverOrNull: IrSimpleFunctionSymbol? by lazy {
        findOptionalFunction(kotlinBrsFqn, "kotlinLifecycleClaimDriver")
    }

    /** kotlin.coroutines.CoroutineScope — the launch block's receiver type. */
    val coroutineScopeClassOrNull: IrClassSymbol? by lazy { findOptionalClass(kotlinCoroutinesFqn, "CoroutineScope") }
```
If `FqName("kotlin.brs")` already exists under another name in the file, reuse it.

- [ ] **Step 3: The lowering**

`compiler/ir/backend.brightscript/src/org/jetbrains/kotlin/ir/backend/brs/lower/BrsComponentLifecycleLowering.kt`:
```kotlin
/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.lower

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.backend.common.ir.addExtensionReceiver
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.backend.brs.BrsIrBackendContext
import org.jetbrains.kotlin.ir.builders.declarations.addFunction
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.createBlockBody
import org.jetbrains.kotlin.ir.declarations.impl.IrAnonymousInitializerImpl
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrBranchImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionExpressionImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetObjectValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrReturnImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrWhenImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrAnonymousInitializerSymbolImpl
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.patchDeclarationParents
import org.jetbrains.kotlin.name.Name

/**
 * Component lifecycle driver synthesis (spec 2026-09-04-component-lifecycle §5.3).
 *
 * For every CONCRETE render component whose user hierarchy overrides
 * `onStart`, adds a member
 *
 * ```kotlin
 * private fun __kotlinStartDriver() {
 *     if (!kotlinLifecycleClaimDriver()) return
 *     launch { awaitReady(); onStart() }
 * }
 * ```
 *
 * and appends an init block calling it (the LAST init statement — the body
 * runs on the next pump tick anyway). Runs at phase 0.054, BEFORE
 * UpgradeCallableReferences and every coroutine lowering, so the suspend
 * lambda is still an IrFunctionExpression that the later passes turn into a
 * real state machine — the whole point: the stdlib cannot host this driver
 * (no state machines in stdlib compilation).
 *
 * `onStart()` is an ordinary dispatch-receiver call, emitted as the
 * `m.onStart_…_k_` slot; slot attachment layers by SceneGraph init order
 * (base first) and the body runs after the whole cascade, so the slot
 * resolves to the most-derived override. A concrete base and its concrete
 * subclass both emit a driver; `kotlinLifecycleClaimDriver` makes the second
 * call a no-op. Abstract classes, task components and ContentNode components
 * get nothing. User-mode modules only: stdlib compilation bails on the OrNull
 * symbols.
 */
class BrsComponentLifecycleLowering(private val context: BrsIrBackendContext) : FileLoweringPass {

    override fun lower(irFile: IrFile) {
        val launch = context.brsSymbols.componentLaunchOrNull ?: return
        val awaitReady = context.brsSymbols.awaitReadyOrNull ?: return
        val claim = context.brsSymbols.kotlinLifecycleClaimDriverOrNull ?: return
        val coroutineScope = context.brsSymbols.coroutineScopeClassOrNull ?: return

        for (declaration in irFile.declarations.toList()) {
            val irClass = declaration as? IrClass ?: continue
            if (!context.intrinsics.isSceneGraphComponent(irClass)) continue
            if (irClass.modality == Modality.ABSTRACT) continue
            if (!context.intrinsics.componentNeedsOnKeyEvent(irClass)) continue   // render components only
            val onStart = context.intrinsics.hierarchyOverrides(irClass, "onStart") { fn ->
                fn.isSuspend && fn.valueParameters.isEmpty()
            } ?: continue
            if (irClass.functions.any { it.name.asString() == DRIVER_NAME }) continue
            synthesizeDriver(irClass, onStart, launch.owner, awaitReady.owner, claim.owner, coroutineScope.owner)
        }
    }

    private fun synthesizeDriver(
        irClass: IrClass,
        onStartOverride: IrSimpleFunction,
        launch: IrSimpleFunction,
        awaitReady: IrSimpleFunction,
        claim: IrSimpleFunction,
        coroutineScope: IrClass,
    ) {
        val unit = context.irBuiltIns.unitType
        val bool = context.irBuiltIns.booleanType

        // The member. Emitted by the component transformer as a global over m
        // and attached as m.__kotlinStartDriver_k_ like every own member.
        val driver = irClass.addFunction(
            name = DRIVER_NAME,
            returnType = unit,
            modality = Modality.FINAL,
            visibility = DescriptorVisibilities.PRIVATE,
        )
        val driverThis = driver.dispatchReceiverParameter
            ?: error("addFunction did not create a dispatch receiver for ${irClass.name}.$DRIVER_NAME")
        fun thisRead() = IrGetValueImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, driverThis.type, driverThis.symbol)

        // The block: suspend CoroutineScope.() -> Unit { awaitReady(this@comp); this@comp.onStart() }
        val lambda = context.irFactory.buildFun {
            startOffset = irClass.startOffset
            endOffset = irClass.endOffset
            origin = IrDeclarationOrigin.LOCAL_FUNCTION_FOR_LAMBDA
            name = Name.special("<anonymous>")
            visibility = DescriptorVisibilities.LOCAL
            modality = Modality.FINAL
            returnType = unit
            isSuspend = true
        }
        lambda.addExtensionReceiver(coroutineScope.defaultType)
        lambda.parent = driver
        val awaitCall = IrCallImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, unit, awaitReady.symbol, typeArgumentsCount = 0).apply {
            extensionReceiver = thisRead()
        }
        // The hook resolved against THIS class (a fake override when inherited):
        // the backend emits it as the m-slot call, which is the point.
        val onStartSymbol = irClass.functions.first { it.name.asString() == "onStart" && it.valueParameters.isEmpty() }.symbol
        val hookCall = IrCallImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, unit, onStartSymbol, typeArgumentsCount = 0).apply {
            dispatchReceiver = thisRead()
        }
        lambda.body = context.irFactory.createBlockBody(UNDEFINED_OFFSET, UNDEFINED_OFFSET, listOf(awaitCall, hookCall))
        val lambdaType = context.irBuiltIns.suspendFunctionN(1).typeWith(coroutineScope.defaultType, unit)

        // if (!kotlinLifecycleClaimDriver()) return
        val claimCall = IrCallImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, bool, claim.symbol, typeArgumentsCount = 0)
        val notClaimed = IrCallImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, bool, context.irBuiltIns.booleanNotSymbol, typeArgumentsCount = 0).apply {
            dispatchReceiver = claimCall
        }
        val earlyReturn = IrReturnImpl(
            UNDEFINED_OFFSET, UNDEFINED_OFFSET, context.irBuiltIns.nothingType, driver.symbol,
            IrGetObjectValueImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, unit, context.irBuiltIns.unitClass)
        )
        val guard = IrWhenImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, unit, IrStatementOrigin.IF).apply {
            branches.add(IrBranchImpl(notClaimed, earlyReturn))
        }

        // launch(this, <default context>, block)
        val launchCall = IrCallImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, launch.returnType, launch.symbol, typeArgumentsCount = 0).apply {
            extensionReceiver = thisRead()
            putValueArgument(0, null)   // context: default (EmptyCoroutineContext) — filled callee-side
            putValueArgument(1, IrFunctionExpressionImpl(irClass.startOffset, irClass.endOffset, lambdaType, lambda, IrStatementOrigin.LAMBDA))
        }
        driver.body = context.irFactory.createBlockBody(UNDEFINED_OFFSET, UNDEFINED_OFFSET, listOf(guard, launchCall))

        // Init tail: an anonymous initializer appended LAST calling the driver.
        val initializer = IrAnonymousInitializerImpl(
            UNDEFINED_OFFSET, UNDEFINED_OFFSET, IrDeclarationOrigin.DEFINED, IrAnonymousInitializerSymbolImpl(), isStatic = false
        )
        initializer.parent = irClass
        val classThis = irClass.thisReceiver ?: error("component ${irClass.name} has no this receiver")
        initializer.body = context.irFactory.createBlockBody(
            UNDEFINED_OFFSET, UNDEFINED_OFFSET,
            listOf(
                IrCallImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, unit, driver.symbol, typeArgumentsCount = 0).apply {
                    dispatchReceiver = IrGetValueImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, classThis.type, classThis.symbol)
                }
            )
        )
        irClass.declarations.add(initializer)
        irClass.patchDeclarationParents(irClass.parent)
    }

    companion object {
        const val DRIVER_NAME = "__kotlinStartDriver"
    }
}
```
API notes for the implementer: `IrCallImpl(...)`'s constructor shape and `putValueArgument`/`extensionReceiver`/`dispatchReceiver` are the ones `BrsFlowTaskLiftLowering` uses in this tree; `addExtensionReceiver` is `org.jetbrains.kotlin.backend.common.ir.addExtensionReceiver` (IrUtils.kt:62); `suspendFunctionN(1)` exists on `IrBuiltIns` alongside `functionN`. If `IrAnonymousInitializerImpl`'s constructor differs, use `context.irFactory.createAnonymousInitializer(startOffset, endOffset, origin, symbol, isStatic)`.

- [ ] **Step 4: Register the phase**

In `BrsLoweringPhases.kt`, immediately BEFORE `phases += BrsRunTaskCallLowering(context)`:
```kotlin
        // Phase 0.054: component lifecycle driver synthesis
        // Adds `__kotlinStartDriver()` + an init-tail call to every concrete render
        // component whose hierarchy overrides onStart (spec 2026-09-04-component-
        // lifecycle §5.3). Must run BEFORE UpgradeCallableReferences and every
        // coroutine lowering: the synthesized `launch { awaitReady(); onStart() }`
        // block is an IrFunctionExpression that those passes turn into a real state
        // machine — the stdlib cannot host this driver (no stdlib state machines).
        phases += BrsComponentLifecycleLowering(context)
```

- [ ] **Step 5: Regenerate and verify the golden**

Run: `./run-compiler-tests.sh --update` then:
```bash
grep -n "__kotlinStartDriver\|kotlinLifecycleClaimDriver\|awaitReady\|onStart" compiler/testData/codegen/brs/components/onStartDriver.brs.txt
```
Expected: `StartBaseKt.brs`, `StartLeafKt.brs`, `StartInheritsKt.brs` each contain `sub <Class>___kotlinStartDriver_k_()` whose body starts with the claim guard and calls `launch_rComponentBase_…_k_(m, invalid, <lambda create>)`; each `sub init()` ends with `<Class>___kotlinStartDriver_k_()` (or its `m.` slot form) as the LAST statement; the lambda's `doResume` state machine calls `awaitReady_rComponentBase_Continuation…_k_` then `m.this_0.onStart_…_k_(…)`; `NoStartKt.brs` contains NO `__kotlinStartDriver`. `./run-compiler-tests.sh` — 103 pass; no other golden changed (`git diff --stat`).

- [ ] **Step 6: Commit**

```bash
git add compiler/ir/backend.brightscript compiler/testData/codegen/brs/components/onStartDriver.*
git commit -m "brs: synthesize the onStart driver (launch { awaitReady(); onStart() }) for hierarchies that override it

Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>"
```

---

### Task 8: Compiler — `__kotlinRetire` / `__kotlinRevive` entries + XML functions (spec §5.6)

**Files:**
- Modify: `IrToBrsTransformer.kt` (new `generateLifecycleEntryFunctions(irClass)` beside `generateTaskMainFunction`; call it where `generateOnKeyEventFunction` is added, ~T:712-716)
- Modify: `BrsComponentInfo.kt` (constants `KOTLIN_RETIRE_FUNCTION_NAME = "__kotlinRetire"`, `KOTLIN_REVIVE_FUNCTION_NAME = "__kotlinRevive"`, `KOTLIN_START_DRIVER_NAME = "__kotlinStartDriver"`)
- Modify: `BrsCompiler.kt:2071-2076` (XML `<function>` entries)
- Create: `compiler/testData/codegen/brs/components/retireReviveEntries.kt` (+ `.brs.txt`); `@Test`

**Interfaces:**
- Consumes: Task 6's `__kotlinRetireImpl`/`__kotlinReviveImpl` (bare `@BrsStatic`), Task 7's member `__kotlinStartDriver`, Task 2's `hierarchyOverrides`.
- Produces: per concrete render component, `sub __kotlinRetire()` / `sub __kotlinRevive()` in the component script and `<function name="…"/>` entries — the `callFunc` targets `retire`/`revive` use.

- [ ] **Step 1: Golden source + test**

`compiler/testData/codegen/brs/components/retireReviveEntries.kt`:
```kotlin
// Every concrete render component gets bare-named __kotlinRetire/__kotlinRevive
// entries + XML <function> entries (spec 2026-09-04-component-lifecycle §5.6).
// __kotlinRetire calls the onStop slot (try/caught) only when the hierarchy
// overrides it; __kotlinRevive relaunches the synthesized onStart driver only
// when one exists.
import kotlin.brs.GroupComponent
import kotlin.brs.SGStringField

class FullLifecycle : GroupComponent() {
    @SGStringField
    var state: String = ""

    override suspend fun onStart() {
        state = "started"
    }

    override fun onStop() {
        state = "stopped"
    }
}

class NoHooks : GroupComponent() {
    @SGStringField
    var label: String = ""
}
```
Add `@Test fun retireReviveEntries() = runTest("components/retireReviveEntries.kt")`; `--update` to capture the pre-change output (no entries).

- [ ] **Step 2: Constants**

In `BrsComponentInfo.kt` after `KOTLIN_TASK_MAIN_FUNCTION_NAME`:
```kotlin
/** Bare-named lifecycle entries emitted for every concrete render component (callFunc targets of retire/revive). */
const val KOTLIN_RETIRE_FUNCTION_NAME = "__kotlinRetire"
const val KOTLIN_REVIVE_FUNCTION_NAME = "__kotlinRevive"

/** The compiler-synthesized onStart driver member (BrsComponentLifecycleLowering). */
const val KOTLIN_START_DRIVER_NAME = "__kotlinStartDriver"
```
and make `BrsComponentLifecycleLowering.DRIVER_NAME` reference `KOTLIN_START_DRIVER_NAME`.

- [ ] **Step 3: Generate the entries**

In `IrToBrsTransformer.kt`, beside `generateTaskMainFunction`:
```kotlin
    /**
     * Lifecycle entries for concrete RENDER components (spec §5.6): bare-named,
     * so the parent's retire(node)/revive(node) reach them through callFunc
     * (documented: runs in the owning component's thread with ITS m). Each
     * concrete class emits its own pair — the derived's same-named function
     * wins in the SceneGraph namespace (documented), and the leaf knows the
     * full hierarchy at generation time.
     *
     * ```brightscript
     * sub __kotlinRetire()
     *     try
     *         m.onStop_k_()                     ' only when the hierarchy overrides onStop
     *     catch e
     *         print "[kotlin.lifecycle] onStop threw: " + e.message
     *     end try
     *     __kotlinRetireImpl()
     * end sub
     * sub __kotlinRevive()
     *     __kotlinReviveImpl()
     *     <Class>___kotlinStartDriver_k_()       ' only when a driver was synthesized
     * end sub
     * ```
     */
    private fun generateLifecycleEntryFunctions(irClass: IrClass): List<BrsSub> {
        if (!context.intrinsics.componentNeedsOnKeyEvent(irClass)) return emptyList()   // render components only
        if (irClass.modality == org.jetbrains.kotlin.descriptors.Modality.ABSTRACT) return emptyList()

        val retireBody = mutableListOf<BrsStatement>()
        val onStop = context.intrinsics.hierarchyOverrides(irClass, "onStop") { it.valueParameters.isEmpty() && !it.isSuspend }
        if (onStop != null) {
            val declaringClass = onStop.parent as IrClass
            val shortName = context.getBrsName(onStop).removePrefix("${context.getBrsName(declaringClass)}_")
            retireBody.add(
                BrsTry(
                    tryBlock = BrsBlock(mutableListOf(
                        BrsExpressionStatement(BrsFunctionCall(BrsDotAccess(BrsMRef(), shortName), mutableListOf()))
                    )),
                    catchVariable = "e",
                    catchBlock = BrsBlock(mutableListOf(
                        BrsPrint(BrsBinaryOp(
                            BrsStringLiteral("[kotlin.lifecycle] onStop threw: "),
                            BrsBinaryOperator.PLUS,
                            BrsDotAccess(BrsIdentifier("e"), "message")
                        ))
                    ))
                )
            )
        }
        retireBody.add(BrsExpressionStatement(createFunctionCall("__kotlinRetireImpl", mutableListOf(), context)))

        val reviveBody = mutableListOf<BrsStatement>()
        reviveBody.add(BrsExpressionStatement(createFunctionCall("__kotlinReviveImpl", mutableListOf(), context)))
        val driver = irClass.declarations.filterIsInstance<IrSimpleFunction>()
            .find { it.name.asString() == KOTLIN_START_DRIVER_NAME }
        if (driver != null) {
            reviveBody.add(BrsExpressionStatement(BrsFunctionCall(BrsIdentifier(context.getBrsName(driver)), mutableListOf())))
        }

        return listOf(
            BrsSub(name = KOTLIN_RETIRE_FUNCTION_NAME, parameters = mutableListOf(), body = BrsBlock(retireBody)),
            BrsSub(name = KOTLIN_REVIVE_FUNCTION_NAME, parameters = mutableListOf(), body = BrsBlock(reviveBody)),
        )
    }
```
Use the actual `BrsTry`/`BrsPrint` constructor shapes from `brightscript/brs.ast` (the try/catch AST already exists — grep `class BrsTry` and mirror `generateTaskMainFunction`'s try emission; if there is no `BrsPrint` node, emit the print through whatever node `println` lowering uses). Add the call where the onKeyEvent wrapper is added:
```kotlin
            declarations.addAll(generateLifecycleEntryFunctions(irClass))
```

- [ ] **Step 4: XML entries**

In `BrsCompiler.kt`, right after the `__kotlinTaskMain` `<function>` block:
```kotlin
        // Concrete render components expose the lifecycle entries retire/revive callFunc into
        if (!context.intrinsics.isTaskComponent(component.irClass) &&
            !context.intrinsics.isContentNodeComponent(component.irClass) &&
            component.irClass.modality != Modality.ABSTRACT
        ) {
            builder.appendLine("        <function name=\"$KOTLIN_RETIRE_FUNCTION_NAME\" />")
            builder.appendLine("        <function name=\"$KOTLIN_REVIVE_FUNCTION_NAME\" />")
        }
```

- [ ] **Step 5: Regenerate; audit ALL goldens**

Run: `./run-compiler-tests.sh --update`, then:
```bash
grep -n "__kotlinRetire\|__kotlinRevive\|onStop_k_\|__kotlinStartDriver" compiler/testData/codegen/brs/components/retireReviveEntries.brs.txt
git diff --stat compiler/testData/codegen/brs
```
Expected: `FullLifecycleKt.brs` has `sub __kotlinRetire()` with the try/catch around `m.onStop_k_()` then `__kotlinRetireImpl()`, and `sub __kotlinRevive()` with `__kotlinReviveImpl()` then `FullLifecycle___kotlinStartDriver_k_()`; `NoHooksKt.brs` has both subs with only the impl calls; both XMLs list the two `<function>` entries. EVERY other render-component golden changes too (two new subs + two XML lines + deps.json edges) — audit that those are the ONLY changes. `./run-compiler-tests.sh` — 104 pass.

- [ ] **Step 6: Commit**

```bash
git add compiler/ir/backend.brightscript compiler/testData/codegen/brs
git commit -m "brs: __kotlinRetire/__kotlinRevive entries + XML functions on every concrete render component

Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>"
```

---

### Task 9: E2E Suite 11 ComponentLifecycle + two Suite 8 tests (spec §9)

**Files (roku-test-app):**
- Create: `src/brsMain/kotlin/com/nuvyyo/roku/components/fixtures/LifecycleChildProbe.kt`, `LifecycleParentProbe.kt`, `LifecycleBaseProbe.kt` (also holds `LifecycleLeafProbe`, `LifecycleSuperProbe`), `LifecycleTaskProbe.kt`, `LifecycleDumbProbe.kt`
- Create: `src/brsTest/kotlin/tests/ComponentLifecycleTests.kt`
- Modify: `src/brsTest/kotlin/tests/TestMain.kt` (register after `flowSuite`), `src/brsTest/kotlin/tests/ScopeHandleTests.kt` (two tests)

**Interfaces:**
- Consumes: `onStart`/`onStop`, `retire`/`revive`, `awaitReady`, `hasFunc`, `SleepTask`, `awaitCompletion`, ScopeOwnerProbe/ScopeChildProbe ops (`arm`, `ready`, `neverDone`, `echo`).
- Evidence channel: a runtime-`addField` string `__lifecycleLog` on the GLOBAL node (init-time facts cannot go through @SG fields); the parent probe's `report` op copies it into `outcome`.

- [ ] **Step 1: Rebuild the toolchain into the test app**

```bash
cd ../roku-test-app && ./rebuild-all.sh --all
```
Expected: BUILD SUCCESSFUL (stdlib, compiler, plugin republished; app compiles).

- [ ] **Step 2: Fixtures**

`LifecycleChildProbe.kt`:
```kotlin
package com.nuvyyo.roku.components.fixtures

import kotlin.brs.roku.RoSGNode

// Global-log evidence channel shared by the Suite 11 fixtures: init-time facts
// cannot go through @SG fields (unset until the parent writes them), so every
// probe appends "<who>-<event>;" to a runtime-addField string on m.global.
internal fun lifecycleLog(global: RoSGNode, entry: String) {
    if (!global.hasField("__lifecycleLog")) global.addField("__lifecycleLog", "string", false)
    global.setField("__lifecycleLog", "${global.getField("__lifecycleLog")}$entry;")
}

// The layout-declared child of LifecycleParentProbe: logs init, onStart and
// onStop. `content` is the recyclable var field the recycle test rewrites
// between retire and revive.
class LifecycleChildProbe : GroupComponent() {

    @SGStringField
    var content: String = ""

    init {
        lifecycleLog(global, "child-init")
    }

    override suspend fun onStart() {
        lifecycleLog(global, "child-start:$content")
        // A nested launch on THIS component's scope: after retire → revive it
        // proves the scope self-healed (a fresh supervisor scope, not the
        // cancelled one, or this line would never run).
        launch { lifecycleLog(global, "child-inner:$content") }
    }

    override fun onStop() {
        lifecycleLog(global, "child-stop")
    }
}
```

`LifecycleParentProbe.kt`:
```kotlin
package com.nuvyyo.roku.components.fixtures

import kotlin.brs.BrsInline
import kotlin.brs.Dynamic
import kotlin.brs.roku.RoSGNodeEvent
import kotlin.brs.roku.RoTimespan
import kotlin.brs.scenegraph.SGLayout
import kotlin.brs.scenegraph.sceneLayout

@BrsInline("return e.message")
private external fun rawMessage(e: Throwable): Dynamic?

// Suite 11 parent: declares one LifecycleChildProbe in its layout (the XML-
// declared shape) and drives lifecycle ops on it from the render thread.
// Driver protocol (FlowOwnerProbe shape): set `arg` where needed, then `op`;
// `outcome` is the terminal field (alwaysNotify, one write per op).
//
// Ops:
//  - "report":       outcome = the global log (then cleared)
//  - "retireChild":  retire(child) -> "retired"
//  - "reviveChild":  revive(child) -> "revived"
//  - "removeChild":  top.removeChild(child) -> "removed"
//  - "appendChild":  top.appendChild(child) -> "appended"
//  - "setContent":   child.content = arg -> "set"
//  - "spawnDumb":    create 1000 LifecycleDumbProbe children, time it,
//                    remove them -> "spawned:<ms>" (INFORMATIONAL)
class LifecycleParentProbe : GroupComponent() {

    private val layout = LifecycleParentProbe_Layout(top)

    @SGStringField(alwaysNotify = true)
    @BrsOnChange("onOpChanged")
    var op: String = ""

    @SGStringField
    var arg: String = ""

    @SGStringField(alwaysNotify = true)
    var outcome: String = ""

    init {
        lifecycleLog(global, "parent-init")
    }

    override suspend fun onStart() {
        lifecycleLog(global, "parent-start")
    }

    private fun onOpChanged(msg: RoSGNodeEvent) {
        val runOp = "${msg.getData()}"
        if (runOp == "") return
        try {
            val child = layout.child
            if (runOp == "report") {
                outcome = "${global.getField("__lifecycleLog")}"
                global.setField("__lifecycleLog", "")
            } else if (runOp == "retireChild") {
                retire(child)
                outcome = "retired"
            } else if (runOp == "reviveChild") {
                revive(child)
                outcome = "revived"
            } else if (runOp == "removeChild") {
                top.removeChild(child)
                outcome = "removed"
            } else if (runOp == "appendChild") {
                top.appendChild(child)
                outcome = "appended"
            } else if (runOp == "setContent") {
                child.setField("content", "${top.getField("arg")}")
                outcome = "set"
            } else if (runOp == "spawnDumb") {
                val clock = RoTimespan.create()
                clock.mark()
                var i = 0
                while (i < 1000) {
                    top.createChild("LifecycleDumbProbe")
                    i = i + 1
                }
                val ms = clock.totalMilliseconds()
                var n = top.getChildCount()
                while (n > 1) {
                    top.removeChildIndex(n - 1)
                    n = n - 1
                }
                outcome = "spawned:$ms"
            } else {
                outcome = "unknown-op:$runOp"
            }
        } catch (e: Throwable) {
            outcome = "unexpected:${rawMessage(e)}"
        }
    }

    companion object {
        @SGLayout
        fun defineLayout() = sceneLayout {
            component("LifecycleChildProbe", id = "child")
        }
    }
}
```
(If `removeChildIndex`/`getChildCount` are not bound on `RoSGNode`, add them to `ISGNodeChildren` in `SceneGraph.kt` — `removeChildIndex(index: Int): Boolean`, `getChildCount(): Int` — and rebuild.)

`LifecycleBaseProbe.kt`:
```kotlin
package com.nuvyyo.roku.components.fixtures

import kotlin.brs.BrsInline
import kotlin.brs.roku.RoSGNodeEvent

// Invokes the component-scope SceneGraph wrapper `function onKeyEvent` (the
// generated entry, not the Kotlin slot) — the only way to exercise the wrapper
// without a physical remote.
@BrsInline("return onKeyEvent(\"OK\", true)")
private external fun invokeKeyWrapper(): Boolean

// Concrete BASE: overrides onStart and onKeyEvent. Its concrete subclasses pin
// (a) the once-per-instance driver reaching the most-derived onStart,
// (b) an inherited onKeyEvent override surviving the leaf's generated wrapper,
// (c) super.onKeyEvent / super.onStart dispatching statically to this base.
open class LifecycleBaseProbe : GroupComponent() {

    @SGStringField(alwaysNotify = true)
    @BrsOnChange("onOpChanged")
    var op: String = ""

    @SGStringField(alwaysNotify = true)
    var outcome: String = ""

    override suspend fun onStart() {
        lifecycleLog(global, "base-start")
    }

    override fun onKeyEvent(key: String, press: Boolean): Boolean {
        lifecycleLog(global, "base-key:$key")
        return true
    }

    private fun onOpChanged(msg: RoSGNodeEvent) {
        val runOp = "${msg.getData()}"
        if (runOp == "report") {
            outcome = "${global.getField("__lifecycleLog")}"
            global.setField("__lifecycleLog", "")
        } else if (runOp == "key") {
            val handled = invokeKeyWrapper()
            outcome = "key:$handled"
        }
    }
}

// Leaf overriding onStart, inheriting onKeyEvent: the driver must call THIS
// onStart exactly once; the wrapper must reach the base's onKeyEvent.
class LifecycleLeafProbe : LifecycleBaseProbe() {
    override suspend fun onStart() {
        lifecycleLog(global, "leaf-start")
    }
}

// Leaf calling super in both hooks: static dispatch to the base, no recursion.
class LifecycleSuperProbe : LifecycleBaseProbe() {
    override suspend fun onStart() {
        super.onStart()
        lifecycleLog(global, "super-start")
    }

    override fun onKeyEvent(key: String, press: Boolean): Boolean {
        lifecycleLog(global, "super-key")
        return super.onKeyEvent(key, press)
    }
}
```

`LifecycleTaskProbe.kt`:
```kotlin
package com.nuvyyo.roku.components.fixtures

import kotlin.brs.BrsInline
import kotlin.brs.roku.RoSGNode
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.task.awaitCompletion

@BrsInline("return task")
private external fun sleepTaskNodeOf(task: SleepTask): RoSGNode

// onStart starts a SleepTask in beat mode and awaits it (TypedTaskProbe's
// cancelawait shape, minus the self-cancel): retire() must cancel the
// component scope, whose runTask cleanup writes control=STOP — the driver
// samples beatCount twice after retire and expects it FROZEN. onStop logs.
class LifecycleTaskProbe : GroupComponent() {

    @SGNodeField
    var taskNode: RoSGNode? = null

    @SGStringField(alwaysNotify = true)
    var awaitOutcome: String = ""

    override suspend fun onStart() {
        val task = createComponent<SleepTask>()
        task.durationMs = 8000
        task.beatIntervalMs = 100
        val node = sleepTaskNodeOf(task)
        taskNode = node
        node.setField("control", "RUN")
        try {
            task.awaitCompletion()
            awaitOutcome = "completed-unexpectedly"
        } catch (e: CancellationException) {
            awaitOutcome = "cancelled"
        }
    }

    override fun onStop() {
        lifecycleLog(global, "task-stop")
    }
}
```

`LifecycleDumbProbe.kt`:
```kotlin
package com.nuvyyo.roku.components.fixtures

// No hooks, no inputs: pays only the attach. Used by the timing line and the
// "never launches anything" assertion (its presence must leave the log empty).
class LifecycleDumbProbe : GroupComponent() {
    @SGStringField
    var label: String = ""
}
```

- [ ] **Step 3: The suite**

`src/brsTest/kotlin/tests/ComponentLifecycleTests.kt`:
```kotlin
package com.nuvyyo.roku.tests

import kotlin.brs.retire
import kotlin.brs.revive
import kotlin.brs.roku.RoSGNode
import kotlin.coroutines.delay
import kotlin.test.TestRunner
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.device.roundTrip

// Suite 11: component lifecycle on device (spec 2026-09-04-component-lifecycle
// §9). Evidence is the global-node log string the fixtures append to; a probe's
// "report" op copies and clears it. retire()/revive() are called from the
// DRIVER (main thread): callFunc rendezvouses into the child's render context.
private object LifecycleExtras {
    private val nodes = mutableListOf<RoSGNode>()
    fun adopt(scene: RoSGNode, subtype: String): RoSGNode {
        val node = scene.createChild(subtype)
        nodes.add(node)
        return node
    }
    fun clear(scene: RoSGNode) {
        for (node in nodes) scene.removeChild(node)
        nodes.clear()
    }
}

// Installs a fresh parent probe; with drain=true (default) waits for the
// init/start entries to land and clears the log, so a test asserts only its
// own entries. Test 1 passes drain=false because those entries ARE its subject.
private suspend fun installParent(scene: RoSGNode, drain: Boolean = true): RoSGNode {
    LifecycleExtras.clear(scene)
    val parent = Probes.create(scene, "LifecycleParentProbe")
    if (drain) {
        delay(300)
        roundTrip(parent, "op", "report", "outcome")
    }
    return parent
}

private suspend fun report(node: RoSGNode): String = "${roundTrip(node, "op", "report", "outcome")}"

fun TestRunner.componentLifecycleSuite(scene: RoSGNode) {
    suite("ComponentLifecycle") {
        testAsync("onStartFiresAfterParentInitForLayoutChild") {
            val parent = installParent(scene, drain = false)
            delay(300)
            val log = report(parent)
            // child init precedes parent init (documented order); both starts
            // fire AFTER the parent's init returned.
            val childInit = log.indexOf("child-init")
            val parentInit = log.indexOf("parent-init")
            val childStart = log.indexOf("child-start")
            assertTrue(childInit >= 0 && parentInit > childInit, "init order, got: $log")
            assertTrue(childStart > parentInit, "child onStart after parent init, got: $log")
            assertTrue(log.contains("parent-start"), "parent onStart fired, got: $log")
        }

        testAsync("onStartFiresOnceReachingTheLeafOverride") {
            installParent(scene)
            val leaf = LifecycleExtras.adopt(scene, "LifecycleLeafProbe")
            delay(300)
            val log = "${roundTrip(leaf, "op", "report", "outcome")}"
            assertEquals("leaf-start;", log)
        }

        testAsync("superOnStartDispatchesToBaseThenLeaf") {
            installParent(scene)
            val probe = LifecycleExtras.adopt(scene, "LifecycleSuperProbe")
            delay(300)
            val log = "${roundTrip(probe, "op", "report", "outcome")}"
            assertEquals("base-start;super-start;", log)
        }

        testAsync("inheritedOnKeyEventReachedThroughLeafWrapper") {
            installParent(scene)
            val leaf = LifecycleExtras.adopt(scene, "LifecycleLeafProbe")
            delay(300)
            roundTrip(leaf, "op", "report", "outcome")   // drain the start entry
            val handled = "${roundTrip(leaf, "op", "key", "outcome")}"
            assertEquals("key:true", handled)
            val log = "${roundTrip(leaf, "op", "report", "outcome")}"
            assertEquals("base-key:OK;", log)
        }

        testAsync("superOnKeyEventDispatchesStatically") {
            installParent(scene)
            val probe = LifecycleExtras.adopt(scene, "LifecycleSuperProbe")
            delay(300)
            roundTrip(probe, "op", "report", "outcome")
            val handled = "${roundTrip(probe, "op", "key", "outcome")}"
            assertEquals("key:true", handled)
            val log = "${roundTrip(probe, "op", "report", "outcome")}"
            assertEquals("super-key;base-key:OK;", log)
        }

        testAsync("dumbComponentLaunchesNothing") {
            val parent = installParent(scene)
            LifecycleExtras.adopt(scene, "LifecycleDumbProbe")
            delay(300)
            assertEquals("", report(parent))
        }

        testAsync("retireRunsOnStopAndStopsTaskAndIsIdempotent") {
            val parent = installParent(scene)
            val probe = LifecycleExtras.adopt(scene, "LifecycleTaskProbe")
            delay(500)   // onStart launched the task and parked in awaitCompletion
            val taskNode = probe.getField("taskNode") as? RoSGNode
            assertTrue(taskNode != null, "task node published by onStart")
            retire(probe)
            delay(400)
            val first = taskNode!!.getField("beatCount") as? Int
            delay(500)
            val second = taskNode.getField("beatCount") as? Int
            assertTrue(first != null && first >= 1, "task beat before the stop, got $first")
            assertEquals(first, second, "beatCount must freeze after retire (control=STOP)")
            assertEquals("cancelled", "${probe.getField("awaitOutcome")}")
            val log = report(parent)
            assertTrue(log.contains("task-stop"), "onStop ran, got: $log")
            retire(probe)   // second retire: no throw, no second onStop
            delay(100)
            assertEquals("", report(parent))
        }

        testAsync("recycleCycleFiresOnStartAgainAndCoroutinesWork") {
            val parent = installParent(scene)
            delay(300)
            report(parent)
            roundTrip(parent, "op", "retireChild", "outcome")
            roundTrip(parent, "op", "removeChild", "outcome")
            parent.setField("arg", "second")
            roundTrip(parent, "op", "setContent", "outcome")
            roundTrip(parent, "op", "appendChild", "outcome")
            roundTrip(parent, "op", "reviveChild", "outcome")
            delay(300)
            val log = report(parent)
            assertTrue(log.contains("child-stop"), "onStop on retire, got: $log")
            assertTrue(log.contains("child-start:second"), "onStart again after revive with new content, got: $log")
            assertTrue(log.contains("child-inner:second"), "the revived child's fresh scope ran a launch, got: $log")
        }

        testAsync("timingThousandDumbInstancesInformational") {
            val parent = installParent(scene)
            val outcome = "${roundTrip(parent, "op", "spawnDumb", "outcome", 15000)}"
            assertTrue(outcome.startsWith("spawned:"), "got: $outcome")
            println("[LIFECYCLE-INFO] 1000 LifecycleDumbProbe instances with attach: $outcome")
        }
    }
}
```
Register in `TestMain.kt`: `componentLifecycleSuite(scene)` after `flowSuite(scene)`.

- [ ] **Step 4: Two Suite 8 tests (ScopeHandleTests.kt, inside `suite("ScopeHandle")`)**

```kotlin
        testAsync("scopeRetireSettlesPendingRequestClosed") {
            val owner = armOwner(scene, "normal")
            val child = installChild(scene, nodeOf(owner))
            child.op = "neverDone"
            delay(300)
            retire(nodeOf(owner))     // closes the exposed host inside the owner's context
            val outcome = awaitField(nodeOf(child), "outcome", 3500) { v -> "$v" != "" }
            assertEquals("closed", "$outcome")
        }

        testAsync("scopeReExposeAfterRetireAndRevive") {
            val owner = armOwner(scene, "normal")
            val ownerNode = nodeOf(owner)
            retire(ownerNode)
            revive(ownerNode)
            ownerNode.setField("ready", false)
            ownerNode.setField("arm", false)
            roundTrip(ownerNode, "arm", true, "ready")     // second exposeScope: one OPEN host per component
            val child = installChild(scene, ownerNode)
            val outcome = roundTrip(nodeOf(child), "op", "echo", "outcome")
            assertEquals("ABC", "$outcome")
        }
```
Add `import kotlin.brs.retire` and `import kotlin.brs.revive` to the file.

- [ ] **Step 5: Run on device**

```bash
cd ../roku-test-app && ./rebuild-all.sh --all && ./run-device-tests.sh
```
Expected: 11 suites; Suite 11 reports 9 passed; Suite 8 reports 32 passed; every pre-existing suite unchanged; `validateComponentIncludes`/`validateTestComponentIncludes` strict with 0 findings (the super static calls resolve through the extends chain via Task 3's validator change). Read `[LIFECYCLE-INFO]` from the console capture and copy the ms into the commit message.

- [ ] **Step 6: Commit (roku-test-app)**

```bash
cd ../roku-test-app && git add src && git commit -m "e2e: Suite 11 ComponentLifecycle (9 tests) + Suite 8 retire/re-expose tests; 1000-instance attach timing: <ms>ms

Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>"
```

---

### Task 10: Documentation + gates (spec §10)

**Files:**
- Modify: `CLAUDE.md` — new section "Component lifecycle: onStart/onStop, retire/revive" placed after "Component Coroutine Scaffolding"; amend that section's KNOWN HOLE paragraph and the `__kotlinPumpAttach` sentence; amend "ScopeHandle" (`exposeScope` re-callable after retire); suite table row for Suite 11; gate table.
- Modify: `docs/superpowers/plans/2026-08-14-shared-service-design.md` decision 10 row: append `**SUPERSEDED** by 2026-09-04-component-lifecycle-design.md (constructor inputs land in plan B).`
- Modify: memory file `project_component_lifecycle_program.md` (plan A shipped; gates).

- [ ] **Step 1: Write the CLAUDE.md lifecycle section**

Cover, in this order, each in one or two short paragraphs: the two hooks and their contract (fires once per activation after the gate; `init` is one-time structural setup, `onStart` is per-activation work); the driver is compiler-synthesized only when the hierarchy overrides `onStart` (per-instance cost table from spec decision 10 with the measured ms from Task 9); `retire(node)`/`revive(node)` sequence and the recycling law verbatim: `retire → removeChild → reconfigure var fields → appendChild → revive`; what retire cannot promise (unretired death is silent; task-side `finally` never runs on STOP); `awaitReady` public and re-entrant; the watchdog line format `[kotlin.lifecycle] <Subtype>(id=<id>) not ready after 30s — inputs <set|UNSET (created outside its Kotlin constructor?)>, unresolved: <labels>` with the `kotlinLifecycleWatchdogFires()`/`kotlinLifecycleWatchdogMillis(ms)` hooks; the `hasFunc` guard; the two fixed defects (`super.f()` static; inherited `onKeyEvent` wrapper) with the documented SceneGraph rules behind them; "Constructor inputs and typed layout builders: plan B (not yet landed)". Key files table: `ComponentLifecycle.kt`, `BrsComponentLifecycleLowering.kt`, the transformer's `generateLifecycleEntryFunctions`, the Suite 11 fixtures.

- [ ] **Step 2: Amend the scaffolding and ScopeHandle sections**

Scaffolding: replace "The compiler injects `__kotlinPumpAttach(m.top, m.global)` into the generated `init()` of every component whose FILE uses coroutines (per-file IR scan; task components excluded)… KNOWN HOLE…" with: "The compiler injects `__kotlinComponentAttach(m.top, m.global)` UNCONDITIONALLY as the first statement of every render component's `init()` (task components excluded); it performs the pump attach internally. The former per-file coroutine scan and its helper-file hole are gone (2026-09-xx, lifecycle program)." ScopeHandle: change "one `exposeScope` per component (second call throws)" to "one OPEN `exposeScope` host per component (a second call while open throws; `retire()` closes and clears it, and `onStart` may expose again — the recyclable-owner idiom)".

- [ ] **Step 3: Suite + gate tables**

Suites header → `(11 suites, 115 active tests + 3 red-guarded xtest placeholders)`; add `| 11 ComponentLifecycle | tests/ComponentLifecycleTests.kt | onStart after parent init (layout child), once-per-instance driver reaching the leaf override, super onStart/onKeyEvent static dispatch, inherited onKeyEvent wrapper, dumb component launches nothing, retire → onStop + runTask STOP + idempotence, full recycle cycle, 1000-instance attach timing (informational) |`. Gate table: Golden `104`; Stdlib `614 / 63`; E2E `115 active / 11 suites`; FIR unchanged `232`.

- [ ] **Step 4: Commit + memory**

```bash
git add CLAUDE.md docs/superpowers/plans/2026-08-14-shared-service-design.md
git commit -m "docs: component lifecycle core shipped — onStart/onStop, retire/revive, unconditional attach; gates 104/232/614-63/115-11

Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>"
```
Update the memory file `project_component_lifecycle_program.md`: plan A SHIPPED (date, gates), plan B next.

---

## Self-review (run by the plan author before handoff)

- **Spec coverage:** §3 hooks/awaitReady/retire/revive/test hooks → Tasks 5–6; §4 registry/gate/watchdog/retire/revive sequences/recycling law/exposeScope amendment → Tasks 5–6 (+9 device pins); §5.1 → Task 4; §5.2 → Task 2; §5.3 → Task 7; §5.4 → Task 3; §5.5 → Task 2; §5.6 → Task 8; §8 spikes → Task 1; §9 goldens (attachUnconditional, onKeyEventInheritedWrapper, superDispatchComponent, onStartDriver, retireReviveEntries) → Tasks 2,3,4,7,8; §9 stdlib units → Task 5; §9 Suite 11 tests 1,2,4,5,8,9(spec-1 half),10,11 → Task 9; §10 docs → Task 10; §13 interface → Task 5. Deferred to plan B by design: constructor inputs, marker field emission, typed builders, FIR family, Suite 11 tests 3/6/7 and the spec-2 half of 9.
- **Placeholders:** none — every step carries its code or exact command; the one contingency (validator change) names the file, the data-model change, and the test shape.
- **Type consistency:** `__kotlinComponentAttach(top, global)` (Task 4 = Task 5); `LifecycleRegistry` fields used identically in Tasks 5–6; `kotlinLifecycleClaimDriver` (Task 5) = `kotlinLifecycleClaimDriverOrNull` lookup (Task 7); `__kotlinStartDriver`/`KOTLIN_START_DRIVER_NAME` (Tasks 7–8); `__kotlinRetireImpl`/`__kotlinReviveImpl` (Task 6 = Task 8); `hasFunc` (Task 5) used by Task 6; `hierarchyOverrides` (Task 2) used by Tasks 7–8; fixture op names in Task 9 match between fixtures and tests.
