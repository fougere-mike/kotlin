function RoInput_Companion_create_k_() as Object
    this = {}
    this.__type = "RoInput_Companion"
    this.__proto = ["RoInput_Companion"]
    this.__id = __kotlin_nextObjectId()
    return this
end function

function RoInput_Companion_getInstance() as Object
    if GetGlobalAA().RoInput_Companion_instance = invalid then
        GetGlobalAA().RoInput_Companion_instance = RoInput_Companion_create_k_()
    end if
    return GetGlobalAA().RoInput_Companion_instance
end function
