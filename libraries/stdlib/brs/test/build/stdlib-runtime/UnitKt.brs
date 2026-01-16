function Unit_create_k_() as Object
    this = {}
    this.__type = "Unit"
    this.__proto = ["Unit"]
    this.__id = __kotlin_nextObjectId()
    this.toString_k_ = Unit_toString_k_
    this.toString = Unit_toString_k_
    return this
end function

function Unit_getInstance() as Object
    if GetGlobalAA().Unit_instance = invalid then
        GetGlobalAA().Unit_instance = Unit_create_k_()
    end if
    return GetGlobalAA().Unit_instance
end function

function Unit_toString_k_() as String
    return "kotlin.Unit"
end function
