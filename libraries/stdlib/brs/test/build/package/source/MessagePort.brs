function RoMessagePort_create_RoMessagePort_k_() as Object
    this = {}
    this.__type = "RoMessagePort"
    this.__proto = ["RoMessagePort", "IMessagePort"]
    this.__id = __kotlin_nextObjectId()
    this.waitMessage_I_DynamicN_k_ = RoMessagePort_waitMessage_I_DynamicN_k_
    this.getMessage_DynamicN_k_ = RoMessagePort_getMessage_DynamicN_k_
    this.peekMessage_DynamicN_k_ = RoMessagePort_peekMessage_DynamicN_k_
    this.getNative_Dynamic_k_ = RoMessagePort_getNative_Dynamic_k_
    this.get_native = RoMessagePort_get_native_Dynamic_k_
    this.native = CreateObject("roMessagePort")
    return this
end function

function RoMessagePort_create_Dynamic_RoMessagePort_k_(nativePort as Object) as Object
    this = {}
    this.__type = "RoMessagePort"
    this.__proto = ["RoMessagePort", "IMessagePort"]
    this.__id = __kotlin_nextObjectId()
    this.waitMessage_I_DynamicN_k_ = RoMessagePort_waitMessage_I_DynamicN_k_
    this.getMessage_DynamicN_k_ = RoMessagePort_getMessage_DynamicN_k_
    this.peekMessage_DynamicN_k_ = RoMessagePort_peekMessage_DynamicN_k_
    this.getNative_Dynamic_k_ = RoMessagePort_getNative_Dynamic_k_
    this.get_native = RoMessagePort_get_native_Dynamic_k_
    this.native = nativePort
    return this
end function

function RoMessagePort_waitMessage_I_DynamicN_k_(timeout as Integer) as Dynamic
    return m.get_native().WaitMessage(timeout)
end function

function RoMessagePort_getMessage_DynamicN_k_() as Dynamic
    return m.get_native().GetMessage()
end function

function RoMessagePort_peekMessage_DynamicN_k_() as Dynamic
    return m.get_native().PeekMessage()
end function

function RoMessagePort_getNative_Dynamic_k_() as Object
    return m.get_native()
end function

function RoMessagePort_get_native_Dynamic_k_() as Object
    return m.native
end function
