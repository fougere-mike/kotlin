' ProbeA — the "owner" analog. Receives the payload on every channel and runs
' the same inspection body. ALL observers/handlers are armed here in init,
' which runs before MatrixScene wires anything (arming-order law).
' Every cross-channel read is try/catch-guarded so one FAIL cannot abort the
' rest of the matrix; a caught throw is recorded verbatim as the finding.

sub init()
    m.mailboxFires = 0
    m.lastReceived = invalid
    m.retAA = invalid
    m.probeBNode = invalid

    ' Q4.1: RUNTIME-ADDED field + observeField. alwaysNotify=true for Q4.2.
    m.top.addField("mailbox", "assocarray", true)
    m.top.observeField("mailbox", "onMailbox")

    ' globalField channel doorbell (declared field, armed before any post)
    m.top.observeField("doorbell", "onDoorbell")

    ' callFuncRet: one-shot timer; mutates the kept return-AA ~100ms after
    ' spikeCall returns, then rings B's ackBell
    m.retTimer = createObject("roSGNode", "Timer")
    m.retTimer.repeat = false
    m.retTimer.duration = 0.1
    m.retTimer.observeField("fire", "onRetTimer")
    m.top.appendChild(m.retTimer)

    ' rtq: register channel "spike.a" from init (render thread; RTQ spike
    ' proved init-time registration works and pre-15 CreateObject -> invalid)
    m.rtq = CreateObject("roRenderThreadQueue")
    m.rtqToken = invalid
    if m.rtq <> invalid then
        m.rtqToken = m.rtq.AddMessageHandler("spike.a", "onRtqMessage")
        print "[SPIKE] rtq.register token=" + Type(m.rtqToken)
    else
        print "[SPIKE] rtq.register SKIP os<15 (CreateObject returned invalid)"
    end if
end sub

function getProbeB() as object
    if m.probeBNode = invalid then m.probeBNode = m.top.getParent().findNode("probeB")
    return m.probeBNode
end function

sub ringB(channel as string)
    b = getProbeB()
    if b <> invalid then
        b.setField("ackBell", channel)
    else
        print "[SPIKE] WARN probeB-not-found cannot-ack " + channel
    end if
end sub

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

' A's own function for the callFuncRet return payload (B invokes it if the
' ref survives the hop). Distinct return string so the capture is unambiguous.
function probeFunctionA() as string
    return "fn-alive-A"
end function

' Same inspection body for every channel's arrival point.
sub inspectPayload(payload as object, channel as string)
    if payload = invalid then
        print "[SPIKE] " + channel + ".nodeRef FAIL payload-invalid"
        print "[SPIKE] " + channel + ".fnRef FAIL payload-invalid"
        return
    end if

    ' (a) mutate for the reference-identity check (B re-checks its local copy)
    try
        payload.marker = 2
    catch e
        print "[SPIKE] " + channel + ".mutate THREW " + e.message
    end try
    m.lastReceived = payload

    ' (b) node identity
    try
        nodeOk = false
        detail = " type=" + Type(payload.theNode)
        if payload.theNode <> invalid and Type(payload.theNode) = "roSGNode" then
            nodeOk = payload.theNode.isSameNode(getProbeB())
        end if
        print "[SPIKE] " + channel + ".nodeRef " + pf(nodeOk) + detail
    catch e
        print "[SPIKE] " + channel + ".nodeRef FAIL threw: " + e.message
    end try

    ' (c) function ref — keyExists distinguishes "stripped to invalid" from
    ' "key dropped entirely"
    try
        fnType = Type(payload.theFn)
        fnOk = (fnType = "roFunction") or (fnType = "Function")
        detail = " type=" + fnType + " keyExists=" + pf(payload.doesExist("theFn"))
        if fnOk then detail = detail + " invoke=" + payload.theFn()
        print "[SPIKE] " + channel + ".fnRef " + pf(fnOk) + detail
    catch e
        print "[SPIKE] " + channel + ".fnRef FAIL threw: " + e.message
    end try
end sub

' ---- channel arrival points ----

sub onMailbox(event as object)
    m.mailboxFires = m.mailboxFires + 1
    if m.mailboxFires = 1 then
        print "[SPIKE] q4.1.addFieldObserver PASS observer-fired"

        ' eventData sub-probe: is event.getData() the SAME object as a field
        ' read? Mutate the event copy, look for it in the field read (and
        ' plant a second marker on the field read for B's cross-check).
        try
            data = event.getData()
            fieldRead = m.top.mailbox
            evOk = false
            if data <> invalid and fieldRead <> invalid then
                data.evProbe = "ev1"
                evOk = (fieldRead.evProbe <> invalid) and (fieldRead.evProbe = "ev1")
                fieldRead.fieldProbe = "fr1"
            end if
            print "[SPIKE] eventData.refIdentity " + pf(evOk) + " getDataType=" + Type(data) + " fieldType=" + Type(fieldRead)
        catch e
            print "[SPIKE] eventData.refIdentity FAIL threw: " + e.message
        end try

        inspectPayload(event.getData(), "nodeField")
        ringB("nodeField")
    else if m.mailboxFires = 2 then
        print "[SPIKE] q4.2.alwaysNotifyRepost PASS fires=2"
        ringB("q4.2")
    end if
end sub

sub onDoorbell(event as object)
    ch = event.getData()
    if ch = "globalField" then
        payload = invalid
        try
            payload = m.global.spikeBox
        catch e
            print "[SPIKE] globalField.read THREW " + e.message
        end try
        inspectPayload(payload, "globalField")
        ringB("globalField")
    end if
end sub

function spikeCall(payload as object) as object
    inspectPayload(payload, "callFuncArg")

    ' callFuncRet: build the return payload, KEEP a reference, mutate it on
    ' the timer ~100ms after returning, then ring B.
    m.retAA = {
        retMarker: 1,
        theNode: m.top,
        theFn: probeFunctionA
    }
    m.retTimer.control = "start"
    return m.retAA
end function

sub onRetTimer()
    if m.retAA <> invalid then m.retAA.retMarker = 2
    ringB("callFuncRet")
end sub

sub onRtqMessage(data as Dynamic, msgInfo as Dynamic)
    try
        print "[SPIKE] rtq.delivered type=" + Type(data) + " keys=" + toS(data.count()) + " marker=" + toS(data.marker)
    catch e
        print "[SPIKE] rtq.delivered inspect-threw: " + e.message
    end try
    inspectPayload(data, "rtq")
    ringB("rtq")
end sub
