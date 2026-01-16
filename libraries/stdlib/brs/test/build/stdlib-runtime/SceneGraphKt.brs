function RoSGScreen_Companion_create_k_() as Object
    this = {}
    this.__type = "RoSGScreen_Companion"
    this.__proto = ["RoSGScreen_Companion"]
    this.__id = __kotlin_nextObjectId()
    return this
end function

function RoSGScreen_Companion_getInstance() as Object
    if GetGlobalAA().RoSGScreen_Companion_instance = invalid then
        GetGlobalAA().RoSGScreen_Companion_instance = RoSGScreen_Companion_create_k_()
    end if
    return GetGlobalAA().RoSGScreen_Companion_instance
end function

function RoSGNode_Companion_create_k_() as Object
    this = {}
    this.__type = "RoSGNode_Companion"
    this.__proto = ["RoSGNode_Companion"]
    this.__id = __kotlin_nextObjectId()
    return this
end function

function RoSGNode_Companion_getInstance() as Object
    if GetGlobalAA().RoSGNode_Companion_instance = invalid then
        GetGlobalAA().RoSGNode_Companion_instance = RoSGNode_Companion_create_k_()
    end if
    return GetGlobalAA().RoSGNode_Companion_instance
end function
