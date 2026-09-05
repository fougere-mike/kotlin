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
    initLog = m.global.__lcLog
    ci = Instr(1, initLog, "child-init")
    pi = Instr(1, initLog, "parent-init")
    verdict("init.q1.childInitBeforeParentInit", ci > 0 and pi > 0 and ci < pi, "log=" + initLog)
    verdict("init.q2.xmlChildParentInvalidInInit", Instr(1, initLog, "parent=invalid") > 0, "log=" + initLog)
    verdict("init.q4.xmlAttrNotYetAppliedInChildInit", Instr(1, initLog, "tag=default") > 0, "log=" + initLog)
    xmlChild = m.parent.findNode("xmlChild")
    tagAfter = "none"
    if xmlChild <> invalid then tagAfter = xmlChild.tag
    print "[SPIKE] init.q4.evidence INFORMATIVE tagAfterCreate=" + tagAfter
    m.top.appendChild(m.parent)
    pp = m.parent.getParent()
    same = false
    psub = "invalid"
    if pp <> invalid then
        same = pp.isSameNode(m.top)
        psub = pp.subtype()
    end if
    print "[SPIKE] init.q5b.sceneChildParent INFORMATIVE parentIsScene=" + lcStr(same) + " parentSubtype=" + psub
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
