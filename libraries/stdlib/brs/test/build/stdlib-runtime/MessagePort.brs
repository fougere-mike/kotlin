function RoMessagePort_create_k_() as Object
    this = {}
    this.__type = "RoMessagePort"
    this.__proto = ["RoMessagePort", "IMessagePort"]
    this.__id = __kotlin_nextObjectId()
    this.waitMessage_I_k_ = RoMessagePort_waitMessage_I_k_
    this.getMessage_k_ = RoMessagePort_getMessage_k_
    this.peekMessage_k_ = RoMessagePort_peekMessage_k_
    this.getNative_k_ = RoMessagePort_getNative_k_
    this.get_native = RoMessagePort_get_native_k_
    this.native = CreateObject("roMessagePort")
    return this
end function

function RoMessagePort_create_Dynamic_k_(nativePort as Object) as Object
    this = {}
    this.__type = "RoMessagePort"
    this.__proto = ["RoMessagePort", "IMessagePort"]
    this.__id = __kotlin_nextObjectId()
    this.waitMessage_I_k_ = RoMessagePort_waitMessage_I_k_
    this.getMessage_k_ = RoMessagePort_getMessage_k_
    this.peekMessage_k_ = RoMessagePort_peekMessage_k_
    this.getNative_k_ = RoMessagePort_getNative_k_
    this.get_native = RoMessagePort_get_native_k_
    this.native = nativePort
    return this
end function

function RoMessagePort_waitMessage_I_k_(timeout as Integer) as Dynamic
    return m.get_native().WaitMessage(timeout)
end function

function RoMessagePort_getMessage_k_() as Dynamic
    return m.get_native().GetMessage()
end function

function RoMessagePort_peekMessage_k_() as Dynamic
    return m.get_native().PeekMessage()
end function

function RoMessagePort_getNative_k_() as Object
    return m.get_native()
end function

function RoMessagePort_get_native_k_() as Object
    return m.native
end function
