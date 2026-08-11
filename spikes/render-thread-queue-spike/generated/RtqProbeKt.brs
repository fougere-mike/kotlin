sub init()
    m.top.command = ""
    m.top.lastEvent = ""
    m.top.initReport = ""
    m.rtq = invalid
    m.instanceNum = 0
    m.clock = CreateObject("roTimespan")
    m.postAtMs = -1
    m.onCommand_RoSGNodeEvent_k_ = RtqProbe_onCommand_RoSGNodeEvent_k_
    m.onRtqMessage_DynamicN_DynamicN_k_ = RtqProbe_onRtqMessage_DynamicN_DynamicN_k_
    m.__get_command_k_ = RtqProbe___get_command_k_
    m.__set_command_Str_k_ = RtqProbe___set_command_Str_k_
    m.__get_lastEvent_k_ = RtqProbe___get_lastEvent_k_
    m.__set_lastEvent_Str_k_ = RtqProbe___set_lastEvent_Str_k_
    m.__get_initReport_k_ = RtqProbe___get_initReport_k_
    m.__set_initReport_Str_k_ = RtqProbe___set_initReport_Str_k_
    m.__get_rtq_k_ = RtqProbe___get_rtq_k_
    m.__set_rtq_DynamicN_k_ = RtqProbe___set_rtq_DynamicN_k_
    m.__get_instanceNum_k_ = RtqProbe___get_instanceNum_k_
    m.__set_instanceNum_I_k_ = RtqProbe___set_instanceNum_I_k_
    m.__get_clock_k_ = RtqProbe___get_clock_k_
    m.__get_postAtMs_k_ = RtqProbe___get_postAtMs_k_
    m.__set_postAtMs_I_k_ = RtqProbe___set_postAtMs_I_k_
    m.clock.mark()
    gaa = GetGlobalAA()
    n = 1
    prev = gaa.lookup("spike_counter")
    if prev <> invalid then
        n = (prev + 1)
    end if
    gaa.addReplace("spike_counter", n)
    gaa.addReplace("spike_marker_" + __kotlin_numToStr_I_k_(n), "present")
    m.instanceNum = n
    rtqType = Type(CreateObject("roRenderThreadQueue"))
    q = CreateObject("roRenderThreadQueue")
    m.rtq = q
    globalType = Type(m.global)
    tokenType = "no-rtq"
    if q <> invalid then
        token = q.AddMessageHandler("spike.pump", "RtqProbe_onRtqMessage_DynamicN_DynamicN_k_")
        tokenType = Type(token)
    end if
    ok = (((rtqType = "roRenderThreadQueue") and (tokenType <> "no-rtq")) and (tokenType <> "Invalid")) and (tokenType <> "<uninitialized>")
    if ok then
        println_AnyN_k_((((((("SPIKE|PASS|rtq_create_and_register|instance=" + __kotlin_numToStr_I_k_(n)) + "|rtqType=") + rtqType) + "|token=") + tokenType) + "|mGlobal=") + globalType)
    else
        println_AnyN_k_((((((("SPIKE|FAIL|rtq_create_and_register|instance=" + __kotlin_numToStr_I_k_(n)) + "|rtqType=") + rtqType) + "|token=") + tokenType) + "|mGlobal=") + globalType)
    end if
    m.top.initReport = ((((((("rtqType=" + rtqType) + "|token=") + tokenType) + "|mGlobal=") + globalType) + "|counter=") + __kotlin_numToStr_I_k_(n))
end sub

sub RtqProbe_onCommand_RoSGNodeEvent_k_(msg as Object)
    cmd = toString_AnyN_k_(msg.getData())
    myId = toString_AnyN_k_(m.top.getField("id"))
    q = m.rtq
    if cmd = "selfpost" then
        m.postAtMs = m.clock.totalMilliseconds()
        payload = CreateObject("roAssociativeArray")
        payload.addReplace("src", "A-self")
        q.PostMessage("spike.pump", payload)
        println_AnyN_k_(((("SPIKE|INFO|selfpost_sent|id=" + myId) + "|at=") + __kotlin_numToStr_I_k_(m.postAtMs)) + "ms")
    else if cmd = "crosspost" then
        payload = CreateObject("roAssociativeArray")
        payload.addReplace("src", "B-cross")
        q.PostMessage("spike.pump", payload)
        println_AnyN_k_("SPIKE|INFO|crosspost_sent|id=" + myId)
    else if cmd = "earlypost" then
        payload = CreateObject("roAssociativeArray")
        payload.addReplace("src", "B-early")
        q.PostMessage("spike.early", payload)
        println_AnyN_k_("SPIKE|INFO|earlypost_sent|id=" + myId)
    else if cmd = "registerlate" then
        token = q.AddMessageHandler("spike.early", "RtqProbe_onRtqMessage_DynamicN_DynamicN_k_")
        println_AnyN_k_((("SPIKE|INFO|late_registration|id=" + myId) + "|token=") + Type(token))
    else if cmd = "readglobals" then
        gaa = GetGlobalAA()
        println_AnyN_k_((((((((("SPIKE|INFO|gaa_identity|id=" + myId) + "|instanceProp=") + __kotlin_numToStr_I_k_(m.instanceNum)) + "|counter=") + toString_AnyN_k_(gaa.lookup("spike_counter"))) + "|marker1=") + ((function(gaa)
            if gaa.doesExist("spike_marker_1") then return "true" else return "false"
        end function)(gaa))) + "|marker2=") + ((function(gaa)
            if gaa.doesExist("spike_marker_2") then return "true" else return "false"
        end function)(gaa)))
    end if
end sub

sub RtqProbe_onRtqMessage_DynamicN_DynamicN_k_(data as Dynamic, msgInfo as Dynamic)
    try
        at = m.clock.totalMilliseconds()
        mType = Type(m)
        topType = Type(m.top)
        subtype = "n/a"
        nodeId = "n/a"
        if topType = "roSGNode" then
            subtype = m.top.subtype()
            nodeId = m.top.id
        end if
        src = "no-data"
        if data <> invalid then
            src = toString_AnyN_k_(data.lookup("src"))
        end if
        latency = -1
        if (m.postAtMs >= 0) and (src = "A-self") then
            latency = (at - m.postAtMs)
        end if
        println_AnyN_k_((((((((((((("SPIKE|INFO|pump_handler|src=" + src) + "|latencyMs=") + __kotlin_numToStr_I_k_(latency)) + "|mType=") + mType) + "|topType=") + topType) + "|subtype=") + subtype) + "|nodeId=") + nodeId) + "|instanceProp=") + __kotlin_numToStr_I_k_(m.instanceNum))
        println_AnyN_k_((("SPIKE|INFO|pump_msginfo|src=" + src) + "|info=") + FormatJson(msgInfo))
        m.top.lastEvent = src
    catch e
        println_AnyN_k_("SPIKE|INFO|pump_handler_error|" + toString_AnyN_k_(e.__get_message()))
    end try
end sub

function onKeyEvent(key as String, press as Boolean) as Boolean
    return false
end function

function RtqProbe___get_command_k_() as String
    return m.top.command
end function

sub RtqProbe___set_command_Str_k_(value as String)
    m.top.command = value
end sub

function RtqProbe___get_lastEvent_k_() as String
    return m.top.lastEvent
end function

sub RtqProbe___set_lastEvent_Str_k_(value as String)
    m.top.lastEvent = value
end sub

function RtqProbe___get_initReport_k_() as String
    return m.top.initReport
end function

sub RtqProbe___set_initReport_Str_k_(value as String)
    m.top.initReport = value
end sub

function RtqProbe___get_rtq_k_() as Dynamic
    return m.rtq
end function

sub RtqProbe___set_rtq_DynamicN_k_(value as Dynamic)
    m.rtq = value
end sub

function RtqProbe___get_instanceNum_k_() as Integer
    return m.instanceNum
end function

sub RtqProbe___set_instanceNum_I_k_(value as Integer)
    m.instanceNum = value
end sub

function RtqProbe___get_clock_k_() as Object
    return m.clock
end function

function RtqProbe___get_postAtMs_k_() as Integer
    return m.postAtMs
end function

sub RtqProbe___set_postAtMs_I_k_(value as Integer)
    m.postAtMs = value
end sub
