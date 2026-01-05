function equals_AnyN_AnyN_k_(obj1 as Dynamic, obj2 as Dynamic) as Boolean
    if __kotlin_identityEquals(obj1, obj2) then
        return true
    end if
    if (obj1 = invalid) or (obj2 = invalid) then
        return false
    end if
    return obj1.equals(obj2)
end function

function toString_AnyN_k_(obj as Dynamic) as String
    if obj = invalid then
        return "null"
    end if
    t = Type(obj)
    if (t = "String") or (t = "roString") then
        return obj
    end if
    if ((((((((t = "Integer") or (t = "LongInteger")) or (t = "Float")) or (t = "Double")) or (t = "roInt")) or (t = "roFloat")) or (t = "roDouble")) or (t = "roInteger")) or (t = "roLongInteger") then
        return __kotlin_numToStr_AnyN_k_(obj)
    end if
    if (t = "Boolean") or (t = "roBoolean") then
        if obj then
            return "true"
        end if
        return "false"
    end if
    return obj.toString()
end function

function hashCode_AnyN_k_(obj as Dynamic) as Integer
    if obj = invalid then
        return 0
    end if
    return obj.hashCode()
end function

function getStringHashCode_Str_k_(str as String) as Integer
    hash = 0
    progression = until_rI_I_k_(0, Len(str))
    inductionVariable = progression.get_first()
    last = progression.get_last()
    if inductionVariable <= last then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        hash = ((31 * hash) + get_code_rC_k_(str.get_I_k_(i)))


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            hash = ((31 * hash) + get_code_rC_k_(str.get_I_k_(i)))

        end while

    end if

    return hash
end function

function getBooleanHashCode_Z_k_(value as Boolean) as Integer
    __when_tmp0 = invalid
    if value then
        __when_tmp0 = 1231
    else if true then
        __when_tmp0 = 1237
    end if
    return __when_tmp0

end function

function get_objectHashCodeCounter_k_() as Integer
    return GetGlobalAA().objectHashCodeCounter
end function

sub set_objectHashCodeCounter_I_k_(value as Integer)
    m.objectHashCodeCounter = value
end sub

function identityHashCode_AnyN_k_(obj as Dynamic) as Integer
    if obj = invalid then
        return 0
    end if
    tmp0_subject = obj
    __when_tmp1 = invalid
    if Type(tmp0_subject) = "roString" then
        __when_tmp1 = getStringHashCode_Str_k_(obj)
    else if Type(tmp0_subject) = "roBoolean" then
        __when_tmp1 = getBooleanHashCode_Z_k_(obj)
    else if __kotlin_isInstanceOf(tmp0_subject, "Number") then
        __when_tmp1 = obj.hashCode()
    else if true then
        set_objectHashCodeCounter_I_k_(get_objectHashCodeCounter_k_() + 1)
        __when_tmp1 = get_objectHashCodeCounter_k_()
    end if
    return __when_tmp1

end function

function newThrowable_StrN_ThrowableN_k_(message as Dynamic, cause as Dynamic) as Object
    return Throwable_create_StrN_ThrowableN_k_(message, cause)
end function

sub captureStack_Throwable_k_(instance as Object)
    instance.captureStack_k_()
end sub

function isInstance_AnyN_Str_k_(obj as Dynamic, type_ as String) as Boolean
    if obj = invalid then
        return false
    end if
    return true
end function

function checkCast_AnyN_Str_k_(obj as Dynamic, type_ as String) as Dynamic
    if obj = invalid then
        return invalid
    end if
    if not isInstance_AnyN_Str_k_(obj, type_) then
        throw ClassCastException_create_StrN_k_((("Cannot cast " + toString_AnyN_k_("/* Unsupported: IrGetClassImpl */".get_simpleName())) + " to ") + type_)
    end if
    return obj
end function

function __kotlin_numToStr_I_k_(value as Integer) as String
    s = Str(value)
    if Left(s, 1) = " " then
        return Mid(s, 2)
    end if
    return s
end function

function __kotlin_numToStr_J_k_(value as LongInteger) as String
    s = Str(value)
    if Left(s, 1) = " " then
        return Mid(s, 2)
    end if
    return s
end function

function __kotlin_numToStr_F_k_(value as Float) as String
    s = Str(value)
    if Left(s, 1) = " " then
        return Mid(s, 2)
    end if
    return s
end function

function __kotlin_numToStr_D_k_(value as Double) as String
    s = Str(value)
    if Left(s, 1) = " " then
        return Mid(s, 2)
    end if
    return s
end function

function __kotlin_numToStr_AnyN_k_(value as Dynamic) as String
    if value = invalid then
        return "null"
    end if
    s = Str(value)
    if Left(s, 1) = " " then
        return Mid(s, 2)
    end if
    return s
end function
