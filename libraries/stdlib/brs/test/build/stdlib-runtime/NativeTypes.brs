function NativeIterable_iterator_k_() as Object
end function

function RoArray_Companion_create_k_() as Object
    this = {}
    this.__type = "RoArray_Companion"
    this.__proto = ["RoArray_Companion"]
    this.__id = __kotlin_nextObjectId()
    return this
end function

function RoArray_Companion_getInstance() as Object
    if GetGlobalAA().RoArray_Companion_instance = invalid then
        GetGlobalAA().RoArray_Companion_instance = RoArray_Companion_create_k_()
    end if
    return GetGlobalAA().RoArray_Companion_instance
end function

function RoAssociativeArray_Companion_create_k_() as Object
    this = {}
    this.__type = "RoAssociativeArray_Companion"
    this.__proto = ["RoAssociativeArray_Companion"]
    this.__id = __kotlin_nextObjectId()
    return this
end function

function RoAssociativeArray_Companion_getInstance() as Object
    if GetGlobalAA().RoAssociativeArray_Companion_instance = invalid then
        GetGlobalAA().RoAssociativeArray_Companion_instance = RoAssociativeArray_Companion_create_k_()
    end if
    return GetGlobalAA().RoAssociativeArray_Companion_instance
end function
