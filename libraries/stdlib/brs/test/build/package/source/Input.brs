function RoInput_create_RoInput_k_() as Object
    this = {}
    this.__type = "RoInput"
    this.__proto = ["RoInput"]
    this.setMessagePort_RoMessagePort_k_ = RoInput_setMessagePort_RoMessagePort_k_
    this.getMessagePort_RoMessagePortN_k_ = RoInput_getMessagePort_RoMessagePortN_k_
    this.markAsHandled = RoInput_markAsHandled
    this.getNative_Dynamic_k_ = RoInput_getNative_Dynamic_k_
    this.get_native = RoInput_get_native_Dynamic_k_
    m.native = CreateObject("roInput")
    return this
end function

sub RoInput_setMessagePort_RoMessagePort_k_(port as Object)
    m.native.SetMessagePort(port.getNative())
end sub

function RoInput_getMessagePort_RoMessagePortN_k_() as Dynamic
    port = m.native.GetMessagePort()
    __when_tmp0 = invalid
    if port <> invalid then
        __when_tmp0 = RoMessagePort_create_Dynamic_RoMessagePort_k_(port)
    else if true then
        __when_tmp0 = invalid
    end if
    return __when_tmp0

end function

sub RoInput_markAsHandled()
    m.native.MarkAsHandled()
end sub

function RoInput_getNative_Dynamic_k_() as Object
    return m.native
end function

function RoInput_get_native_Dynamic_k_() as Object
    return m.native
end function

function RoInputEvent_create_Dynamic_RoInputEvent_k_(event as Object) as Object
    this = {}
    this.__type = "RoInputEvent"
    this.__proto = ["RoInputEvent"]
    this.event = event
    this.isInput_Z_k_ = RoInputEvent_isInput_Z_k_
    this.isScreenSaverExitedEvent_Z_k_ = RoInputEvent_isScreenSaverExitedEvent_Z_k_
    this.getInfo_Dynamic_k_ = RoInputEvent_getInfo_Dynamic_k_
    this.get_event = RoInputEvent_get_event_Dynamic_k_
    return this
end function

function RoInputEvent_isInput_Z_k_() as Boolean
    return m.event.IsInput()
end function

function RoInputEvent_isScreenSaverExitedEvent_Z_k_() as Boolean
    return m.event.IsScreenSaverExitedEvent()
end function

function RoInputEvent_getInfo_Dynamic_k_() as Object
    return m.event.GetInfo()
end function

function RoInputEvent_get_event_Dynamic_k_() as Object
    return m.event
end function
