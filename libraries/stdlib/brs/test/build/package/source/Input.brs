function RoInput_create_k_() as Object
    this = {}
    this.__type = "RoInput"
    this.__proto = ["RoInput", "ISetMessagePort", "IGetMessagePort"]
    this.__id = __kotlin_nextObjectId()
    this.setMessagePort_RoMessagePort_k_ = RoInput_setMessagePort_RoMessagePort_k_
    this.getMessagePort_k_ = RoInput_getMessagePort_k_
    this.markAsHandled_k_ = RoInput_markAsHandled_k_
    this.getNative_k_ = RoInput_getNative_k_
    this.get_native = RoInput_get_native_k_
    this.native = CreateObject("roInput")
    return this
end function

sub RoInput_setMessagePort_RoMessagePort_k_(port as Object)
    m.get_native().SetMessagePort(port.getNative_k_())
end sub

function RoInput_getMessagePort_k_() as Dynamic
    port = m.get_native().GetMessagePort()
    __when_tmp0 = invalid
    if port <> invalid then
        __when_tmp0 = RoMessagePort_create_Dynamic_k_(port)
    else if true then
        __when_tmp0 = invalid
    end if
    return __when_tmp0

end function

sub RoInput_markAsHandled_k_()
    m.get_native().MarkAsHandled()
end sub

function RoInput_getNative_k_() as Object
    return m.get_native()
end function

function RoInput_get_native_k_() as Object
    return m.native
end function

function RoInputEvent_create_Dynamic_k_(event as Object) as Object
    this = {}
    this.__type = "RoInputEvent"
    this.__proto = ["RoInputEvent"]
    this.__id = __kotlin_nextObjectId()
    this.isInput_k_ = RoInputEvent_isInput_k_
    this.isScreenSaverExitedEvent_k_ = RoInputEvent_isScreenSaverExitedEvent_k_
    this.getInfo_k_ = RoInputEvent_getInfo_k_
    this.get_event = RoInputEvent_get_event_k_
    this.event = event
    return this
end function

function RoInputEvent_isInput_k_() as Boolean
    return m.get_event().IsInput()
end function

function RoInputEvent_isScreenSaverExitedEvent_k_() as Boolean
    return m.get_event().IsScreenSaverExitedEvent()
end function

function RoInputEvent_getInfo_k_() as Object
    return m.get_event().GetInfo()
end function

function RoInputEvent_get_event_k_() as Object
    return m.event
end function
