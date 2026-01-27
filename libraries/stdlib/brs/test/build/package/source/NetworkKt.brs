function RoUrlTransfer_Companion_create_k_() as Object
    this = {}
    this.__type = "RoUrlTransfer_Companion"
    this.__proto = ["RoUrlTransfer_Companion"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    return this
end function

function RoUrlTransfer_Companion_getInstance() as Object
    if GetGlobalAA().RoUrlTransfer_Companion_instance = invalid then
        GetGlobalAA().RoUrlTransfer_Companion_instance = RoUrlTransfer_Companion_create_k_()
    end if
    return GetGlobalAA().RoUrlTransfer_Companion_instance
end function

function getResponseHeader_rRoUrlTransfer_Str_k_(m as Object, name as String) as String
    headers = m.getResponseHeaders()
    return invalid
end function

function getBytesReceived_rRoUrlEvent_k_(m as Object) as Integer
    return m.getInt()
end function

function getResponseHeader_rRoUrlEvent_Str_k_(m as Object, name as String) as String
    headers = m.getResponseHeaders()
    return invalid
end function
