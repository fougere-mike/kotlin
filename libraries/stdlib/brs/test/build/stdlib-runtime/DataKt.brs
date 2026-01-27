function RoByteArray_Companion_create_k_() as Object
    this = {}
    this.__type = "RoByteArray_Companion"
    this.__proto = ["RoByteArray_Companion"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    return this
end function

function RoByteArray_Companion_getInstance() as Object
    if GetGlobalAA().RoByteArray_Companion_instance = invalid then
        GetGlobalAA().RoByteArray_Companion_instance = RoByteArray_Companion_create_k_()
    end if
    return GetGlobalAA().RoByteArray_Companion_instance
end function

sub resize_rRoByteArray_I_k_(m as Object, newSize as Integer)
    m.resize(newSize, 0)
end sub
