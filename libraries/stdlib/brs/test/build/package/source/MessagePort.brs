function RoMessagePort_Companion_create_k_() as Object
    this = {}
    this.__type = "RoMessagePort_Companion"
    this.__proto = ["RoMessagePort_Companion"]
    this.__id = __kotlin_nextObjectId()
    return this
end function

function RoMessagePort_Companion_getInstance() as Object
    if GetGlobalAA().RoMessagePort_Companion_instance = invalid then
        GetGlobalAA().RoMessagePort_Companion_instance = RoMessagePort_Companion_create_k_()
    end if
    return GetGlobalAA().RoMessagePort_Companion_instance
end function
