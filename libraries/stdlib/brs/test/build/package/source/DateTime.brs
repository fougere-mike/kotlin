function RoDateTime_Companion_create_k_() as Object
    this = {}
    this.__type = "RoDateTime_Companion"
    this.__proto = ["RoDateTime_Companion"]
    this.__id = __kotlin_nextObjectId()
    return this
end function

function RoDateTime_Companion_getInstance() as Object
    if m.RoDateTime_Companion_instance = invalid then
        m.RoDateTime_Companion_instance = RoDateTime_Companion_create_k_()
    end if
    return m.RoDateTime_Companion_instance
end function

function RoTimespan_Companion_create_k_() as Object
    this = {}
    this.__type = "RoTimespan_Companion"
    this.__proto = ["RoTimespan_Companion"]
    this.__id = __kotlin_nextObjectId()
    return this
end function

function RoTimespan_Companion_getInstance() as Object
    if m.RoTimespan_Companion_instance = invalid then
        m.RoTimespan_Companion_instance = RoTimespan_Companion_create_k_()
    end if
    return m.RoTimespan_Companion_instance
end function

function getSecondsToHere_rRoTimespan_k_(m as Object) as Object
    total = m.totalMilliseconds()
    return Pair_create_AnyN_AnyN_k_(total / 1000, total mod 1000)
end function
