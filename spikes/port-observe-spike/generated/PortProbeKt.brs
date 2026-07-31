sub init()
    m.top.input = ""
    m.top.echo = ""
    m.top.timerOut = ""
    m.onInputChanged_RoSGNodeEvent_k_ = PortProbe_onInputChanged_RoSGNodeEvent_k_
    m.onTimerFired_RoSGNodeEvent_k_ = PortProbe_onTimerFired_RoSGNodeEvent_k_
    m.__get_input_k_ = PortProbe___get_input_k_
    m.__set_input_Str_k_ = PortProbe___set_input_Str_k_
    m.__get_echo_k_ = PortProbe___get_echo_k_
    m.__set_echo_Str_k_ = PortProbe___set_echo_Str_k_
    m.__get_timerOut_k_ = PortProbe___get_timerOut_k_
    m.__set_timerOut_Str_k_ = PortProbe___set_timerOut_Str_k_
    println_AnyN_k_("PortProbe: init (render thread)")
    timer = m.top.createChild("Timer")
    timer.setField("duration", 0.5#)
    timer.setField("repeat", false)
    timer.observeFieldScoped("fire", "PortProbe_onTimerFired_RoSGNodeEvent_k_")
    timer.setField("control", "start")
end sub

sub PortProbe_onInputChanged_RoSGNodeEvent_k_(msg as Object)
    v = toString_AnyN_k_(msg.getData())
    println_AnyN_k_(("PortProbe: onInputChanged data=" + v) + " (render thread)")
    m.top.setField("echo", "echo:" + v)
end sub

sub PortProbe_onTimerFired_RoSGNodeEvent_k_(msg as Object)
    println_AnyN_k_("PortProbe: timer fired (render thread)")
    m.top.setField("timerOut", "timer-fired")
end sub

function onKeyEvent(key as String, press as Boolean) as Boolean
    return false
end function

function PortProbe___get_input_k_() as String
    return m.input
end function

sub PortProbe___set_input_Str_k_(value as String)
    m.input = value
end sub

function PortProbe___get_echo_k_() as String
    return m.echo
end function

sub PortProbe___set_echo_Str_k_(value as String)
    m.echo = value
end sub

function PortProbe___get_timerOut_k_() as String
    return m.timerOut
end function

sub PortProbe___set_timerOut_Str_k_(value as String)
    m.timerOut = value
end sub
