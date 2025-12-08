function equals_AnyN_AnyN_Z_k_(obj1 as Dynamic, obj2 as Dynamic) as Boolean
    if EQEQEQ_AnyN_AnyN_Z_k_(obj1, obj2) then
        return true
    end if
    if (obj1 = invalid) or (obj2 = invalid) then
        return false
    end if
    return obj1.equals(obj2)
end function

function toString_AnyN_Str_k_(obj as Dynamic) as String
    if obj = invalid then
        return "null"
    end if
    return obj.toString()
end function

function hashCode_AnyN_I_k_(obj as Dynamic) as Integer
    if obj = invalid then
        return 0
    end if
    return obj.hashCode()
end function

function getStringHashCode_Str_I_k_(str as String) as Integer
    hash = 0
    for each i in until_rI_I_IntRange_k_(0, str.length)
        hash = ((31 * hash) + get_code_rC_I_k_(str[i]))

    end for
    return hash
end function

function getBooleanHashCode_Z_I_k_(value as Boolean) as Integer
    __when_tmp0 = invalid
    if value then
        __when_tmp0 = 1231
    else if true then
        __when_tmp0 = 1237
    end if
    return __when_tmp0

end function

function get_objectHashCodeCounter_I_k_() as Integer
    return GetGlobalAA().objectHashCodeCounter
end function

sub set_objectHashCodeCounter_I_k_(value as Integer)
    m.objectHashCodeCounter = value
end sub

function identityHashCode_AnyN_I_k_(obj as Dynamic) as Integer
    if obj = invalid then
        return 0
    end if
    __when_tmp1 = invalid
    if Type(tmp0_subject) = "roString" then
        __when_tmp1 = getStringHashCode_Str_I_k_(obj)
    else if Type(tmp0_subject) = "roBoolean" then
        __when_tmp1 = getBooleanHashCode_Z_I_k_(obj)
    else if __kotlin_isInstanceOf(tmp0_subject, "Number") then
        __when_tmp1 = obj.hashCode()
    else if true then
        __when_tmp1 = set_objectHashCodeCounter_I_k_(get_objectHashCodeCounter_I_k_() + 1)
    end if
    return __when_tmp1

end function

function newThrowable_StrN_ThrowableN_Throwable_k_(message as Dynamic, cause as Dynamic) as Object
    return Throwable_create_StrN_ThrowableN_Throwable_k_(message, cause)
end function

sub captureStack_Throwable_k_(instance as Object)
    instance.captureStack()
end sub

function isInstance_AnyN_Str_Z_k_(obj as Dynamic, type_ as String) as Boolean
    if obj = invalid then
        return false
    end if
    return true
end function

function checkCast_AnyN_Str_AnyN_k_(obj as Dynamic, type_ as String) as Dynamic
    if obj = invalid then
        return invalid
    end if
    if isInstance_AnyN_Str_Z_k_(obj, type_).not() then
        throw ClassCastException_create_StrN_ClassCastException_k_((("Cannot cast " + "/* Unsupported: IrGetClassImpl */".simpleName) + " to ") + type_)
    end if
    return obj
end function
