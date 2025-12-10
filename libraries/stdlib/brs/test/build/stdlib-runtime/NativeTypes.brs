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
    if m.RoArray_Companion_instance = invalid then
        m.RoArray_Companion_instance = RoArray_Companion_create_k_()
    end if
    return m.RoArray_Companion_instance
end function

function RoAssociativeArray_Companion_create_k_() as Object
    this = {}
    this.__type = "RoAssociativeArray_Companion"
    this.__proto = ["RoAssociativeArray_Companion"]
    this.__id = __kotlin_nextObjectId()
    return this
end function

function RoAssociativeArray_Companion_getInstance() as Object
    if m.RoAssociativeArray_Companion_instance = invalid then
        m.RoAssociativeArray_Companion_instance = RoAssociativeArray_Companion_create_k_()
    end if
    return m.RoAssociativeArray_Companion_instance
end function
