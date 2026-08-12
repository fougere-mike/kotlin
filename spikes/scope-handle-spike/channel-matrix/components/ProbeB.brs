' ProbeB — the "child". Drives the matrix as a step machine (one-shot Timer
' between async steps). Posts the payload on each channel, then re-checks its
' LOCAL copy after A signals it has inspected (ackBell doorbell).
' A 2s watchdog turns a never-firing observer into explicit FAIL lines —
' a missing verdict is itself a finding, never a blank cell.

sub init()
    m.step = 0
    m.sentPayload = invalid
    m.retCopy = invalid
    m.expectedAck = ""

    ' armed in init, before MatrixScene writes anything (arming-order law)
    m.top.observeField("runTests", "onRunTests")
    m.top.observeField("ackBell", "onAck")

    m.stepTimer = createObject("roSGNode", "Timer")
    m.stepTimer.repeat = false
    m.stepTimer.duration = 0.1
    m.stepTimer.observeField("fire", "onStepTimer")
    m.top.appendChild(m.stepTimer)

    m.watchdog = createObject("roSGNode", "Timer")
    m.watchdog.repeat = false
    m.watchdog.duration = 2
    m.watchdog.observeField("fire", "onWatchdog")
    m.top.appendChild(m.watchdog)
end sub

function buildPayload() as object
    return {
        marker: 1,                        ' (a) reference identity: A sets marker=2; B re-reads its local copy
        theNode: m.top,                   ' (b) node ref: A checks isSameNode against B's node
        theFn: probeFunction,             ' (c) function ref: A checks Type() and invokes if callable
        kotlinObj: invalid                ' (d) exercised only in the kotlin-probe app (Task 4)
    }
end function

function probeFunction() as string
    return "fn-alive"
end function

function pf(ok as boolean) as string
    if ok then return "PASS"
    return "FAIL"
end function

function toS(v as dynamic) as string
    if v = invalid then return "invalid"
    t = Type(v)
    if t = "roInteger" or t = "Integer" or t = "roInt" or t = "LongInteger" then return v.toStr()
    if t = "roString" or t = "String" then return v
    if t = "roBoolean" or t = "Boolean" then
        if v then return "true"
        return "false"
    end if
    return "<" + t + ">"
end function

' ---- step machine ----

sub onRunTests()
    if m.top.runTests then nextStep()
end sub

sub nextStep()
    m.watchdog.control = "stop"
    m.step = m.step + 1
    if m.step = 1
        stepNodeField()
    else if m.step = 2
        stepQ42()
    else if m.step = 3
        stepGlobalField()
    else if m.step = 4
        stepCallFunc()
    else if m.step = 5
        stepRtq()
    else
        print "[SPIKE] END"
    end if
end sub

sub advance()
    m.stepTimer.control = "start"
end sub

sub onStepTimer()
    nextStep()
end sub

sub armWatchdog(ack as string)
    m.expectedAck = ack
    m.watchdog.control = "stop"
    m.watchdog.control = "start"
end sub

' ---- steps ----

' Step 1: nodeField (+ Q4.1 first-fire + eventData sub-probe, printed by A)
sub stepNodeField()
    m.sentPayload = buildPayload()
    armWatchdog("nodeField")
    m.top.aNode.setField("mailbox", m.sentPayload)
end sub

' Step 2: Q4.2 — post the IDENTICAL payload object a second time
sub stepQ42()
    armWatchdog("q4.2")
    m.top.aNode.setField("mailbox", m.sentPayload)
end sub

' Step 3: globalField — runtime-added global field + doorbell on A
sub stepGlobalField()
    if not m.global.hasField("spikeBox") then
        m.global.addField("spikeBox", "assocarray", false)
    end if
    m.sentPayload = buildPayload()
    m.global.setField("spikeBox", m.sentPayload)
    armWatchdog("globalField")
    m.top.aNode.setField("doorbell", "globalField")
end sub

' Step 4: callFuncArg (synchronous) + callFuncRet (B inspects the returned
' payload; refIdentity verdict waits for A's delayed mutation + ack)
sub stepCallFunc()
    m.sentPayload = buildPayload()
    ret = invalid
    try
        ret = m.top.aNode.callFunc("spikeCall", m.sentPayload)
    catch e
        print "[SPIKE] callFuncArg.call THREW " + e.message
    end try
    ' callFunc is synchronous: A has already inspected + mutated the arg payload
    checkReferenceIdentity("callFuncArg")

    m.retCopy = ret
    if ret = invalid then
        print "[SPIKE] callFuncRet.nodeRef FAIL ret-invalid"
        print "[SPIKE] callFuncRet.fnRef FAIL ret-invalid"
    else
        try
            nodeOk = false
            detail = " type=" + Type(ret.theNode)
            if ret.theNode <> invalid and Type(ret.theNode) = "roSGNode" then
                nodeOk = ret.theNode.isSameNode(m.top.aNode)
                ' characterize the returned node: same node as probeA by a
                ' second path? a clone with the same subtype/id? detached?
                found = m.top.getParent().findNode("probeA")
                sameAsFound = "n/a"
                if found <> invalid then sameAsFound = pf(ret.theNode.isSameNode(found))
                detail = detail + " subtype=" + ret.theNode.subtype() + " id=" + ret.theNode.id + " sameAsFound=" + sameAsFound
            end if
            print "[SPIKE] callFuncRet.nodeRef " + pf(nodeOk) + detail
        catch e
            print "[SPIKE] callFuncRet.nodeRef FAIL threw: " + e.message
        end try
        try
            fnType = Type(ret.theFn)
            fnOk = (fnType = "roFunction") or (fnType = "Function")
            detail = " type=" + fnType + " keyExists=" + pf(ret.doesExist("theFn"))
            if fnOk then detail = detail + " invoke=" + ret.theFn()
            print "[SPIKE] callFuncRet.fnRef " + pf(fnOk) + detail
        catch e
            print "[SPIKE] callFuncRet.fnRef FAIL threw: " + e.message
        end try
    end if
    armWatchdog("callFuncRet")
end sub

' Step 5: rtq — render-thread-to-render-thread post to A's channel
sub stepRtq()
    q = CreateObject("roRenderThreadQueue")
    if q = invalid then
        print "[SPIKE] rtq.refIdentity SKIP os<15"
        print "[SPIKE] rtq.nodeRef SKIP os<15"
        print "[SPIKE] rtq.fnRef SKIP os<15"
        advance()
        return
    end if
    m.sentPayload = buildPayload()
    armWatchdog("rtq")
    q.PostMessage("spike.a", m.sentPayload)
    ' v1 run showed marker=invalid at check time — did PostMessage GUT the
    ' sender's AA (move semantics)? Snapshot B's local copy right after post.
    try
        print "[SPIKE] rtq.postState keys=" + toS(m.sentPayload.count()) + " marker=" + toS(m.sentPayload.marker) + " hasTheNode=" + pf(m.sentPayload.doesExist("theNode"))
    catch e
        print "[SPIKE] rtq.postState inspect-threw: " + e.message
    end try
end sub

' ---- post-hop checks ----

sub checkReferenceIdentity(channel as string)
    ok = false
    detail = ""
    if m.sentPayload <> invalid then
        ok = (m.sentPayload.marker = 2)               ' A's mutation visible in B's copy?
        detail = " marker=" + toS(m.sentPayload.marker) + " keys=" + toS(m.sentPayload.count())
        if channel = "nodeField" then
            ' cross-check A's eventData markers: evProbe planted on getData(),
            ' fieldProbe planted on A's m.top.mailbox read
            detail = detail + " evProbe=" + toS(m.sentPayload.evProbe) + " fieldProbe=" + toS(m.sentPayload.fieldProbe)
        end if
    else
        detail = " sentPayload-invalid"
    end if
    print "[SPIKE] " + channel + ".refIdentity " + pf(ok) + detail
end sub

sub onAck(event as object)
    ch = event.getData()
    if ch <> m.expectedAck then
        print "[SPIKE] WARN unexpected-ack got=" + ch + " expected=" + m.expectedAck
        return
    end if
    m.watchdog.control = "stop"
    m.expectedAck = ""
    if ch = "q4.2" then
        ' verdict already printed by A on its second fire
    else if ch = "callFuncRet" then
        ok = false
        detail = ""
        if m.retCopy <> invalid then
            ok = (m.retCopy.retMarker = 2)            ' A's delayed mutation visible in B's copy?
            detail = " retMarker=" + toS(m.retCopy.retMarker)
        else
            detail = " ret-invalid"
        end if
        print "[SPIKE] callFuncRet.refIdentity " + pf(ok) + detail
    else
        checkReferenceIdentity(ch)
    end if
    advance()
end sub

' Watchdog: the channel never delivered / A never acked — record every
' outstanding cell as an explicit FAIL, then keep the matrix moving.
sub onWatchdog()
    ch = m.expectedAck
    if ch = "" then return
    m.expectedAck = ""
    print "[SPIKE] " + ch + ".NO-ACK observer-or-handler-never-fired (2s timeout)"
    if ch = "nodeField" then
        print "[SPIKE] q4.1.addFieldObserver FAIL no-fire (2s timeout)"
        print "[SPIKE] eventData.refIdentity FAIL no-fire"
        print "[SPIKE] nodeField.refIdentity FAIL no-delivery"
        print "[SPIKE] nodeField.nodeRef FAIL no-delivery"
        print "[SPIKE] nodeField.fnRef FAIL no-delivery"
    else if ch = "q4.2" then
        print "[SPIKE] q4.2.alwaysNotifyRepost FAIL no-second-fire (2s timeout)"
    else if ch = "callFuncRet" then
        print "[SPIKE] callFuncRet.refIdentity FAIL no-ack-from-A-timer"
    else
        print "[SPIKE] " + ch + ".refIdentity FAIL no-delivery"
        print "[SPIKE] " + ch + ".nodeRef FAIL no-delivery"
        print "[SPIKE] " + ch + ".fnRef FAIL no-delivery"
    end if
    advance()
end sub
