function Invalid_create_k_() as Object
    this = {}
    this.__type = "Invalid"
    this.__proto = ["Invalid"]
    this.__id = __kotlin_nextObjectId()
    this.toString_k_ = Invalid_toString_k_
    this.toString = Invalid_toString_k_
    return this
end function

function Invalid_getInstance() as Object
    if m.Invalid_instance = invalid then
        m.Invalid_instance = Invalid_create_k_()
    end if
    return m.Invalid_instance
end function

function Invalid_toString_k_() as String
    return "invalid"
end function

function isInvalid_AnyN_k_(value as Dynamic) as Boolean
    return (value = invalid) or (Type(value) = "Invalid")
end function

function asDynamic_rAnyN_k_(m as Dynamic) as Object
    return unsafeCast_rAnyN_k_(m)
end function

function unsafeCast_rAnyN_k_(m as Dynamic) as Dynamic
    return m
end function
