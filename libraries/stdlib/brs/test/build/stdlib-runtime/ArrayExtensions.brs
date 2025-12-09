function contentToString_rArrN_Str_k_(m as Dynamic) as String
    if m = invalid then
        return "null"
    end if
    sb = StringBuilder_create_StringBuilder_k_()
    sb.append_StrN_StringBuilder_k_("[")
    progression = until_rI_I_IntRange_k_(0, m.get_size())
    inductionVariable = progression.get_first()
    last = progression.get_last()
    if inductionVariable <= last then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if i > 0 then
            sb.append_StrN_StringBuilder_k_(", ")
        end if
        sb.append_AnyN_StringBuilder_k_(m.get_I_AnyN_k_(i))


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if i > 0 then
                sb.append_StrN_StringBuilder_k_(", ")
            end if
            sb.append_AnyN_StringBuilder_k_(m.get_I_AnyN_k_(i))

        end while

    end if

    sb.append_StrN_StringBuilder_k_("]")
    return sb.toString()
end function

function contentEquals_rArrN_ArrN_Z_k_(m as Dynamic, other as Dynamic) as Boolean
    if __kotlin_identityEquals(m, other) then
        return true
    end if
    if (m = invalid) or (other = invalid) then
        return false
    end if
    if m.get_size() <> other.get_size() then
        return false
    end if
    progression = until_rI_I_IntRange_k_(0, m.get_size())
    inductionVariable = progression.get_first()
    last = progression.get_last()
    if inductionVariable <= last then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if m.get_I_AnyN_k_(i) <> other.get_I_AnyN_k_(i) then
            return false
        end if


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if m.get_I_AnyN_k_(i) <> other.get_I_AnyN_k_(i) then
                return false
            end if

        end while

    end if

    return true
end function

function contentToString_rByteArrayN_Str_k_(m as Dynamic) as String
    if m = invalid then
        return "null"
    end if
    list = ArrayList_create_ArrayListAnyN_k_()
    indexedObject = m
    inductionVariable = 0
    last = indexedObject.get_size()
    while inductionVariable < last
        item = indexedObject.get_I_B_k_(inductionVariable)
        inductionVariable = (inductionVariable + 1)

        list.add_AnyN_Z_k_(__kotlin_numToStr_I_Str_k_(item))

    end while

    return ("[" + joinToString_rIterableAnyN_CharSequence_CharSequence_CharSequence_I_CharSequence_Str_k_(list, ", ")) + "]"
end function

function contentEquals_rByteArrayN_ByteArrayN_Z_k_(m as Dynamic, other as Dynamic) as Boolean
    if __kotlin_identityEquals(m, other) then
        return true
    end if
    if (m = invalid) or (other = invalid) then
        return false
    end if
    if m.get_size() <> other.get_size() then
        return false
    end if
    progression = until_rI_I_IntRange_k_(0, m.get_size())
    inductionVariable = progression.get_first()
    last = progression.get_last()
    if inductionVariable <= last then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if m.get_I_B_k_(i) <> other.get_I_B_k_(i) then
            return false
        end if


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if m.get_I_B_k_(i) <> other.get_I_B_k_(i) then
                return false
            end if

        end while

    end if

    return true
end function

function contentToString_rShortArrayN_Str_k_(m as Dynamic) as String
    if m = invalid then
        return "null"
    end if
    list = ArrayList_create_ArrayListAnyN_k_()
    indexedObject = m
    inductionVariable = 0
    last = indexedObject.get_size()
    while inductionVariable < last
        item = indexedObject.get_I_S_k_(inductionVariable)
        inductionVariable = (inductionVariable + 1)

        list.add_AnyN_Z_k_(__kotlin_numToStr_I_Str_k_(item))

    end while

    return ("[" + joinToString_rIterableAnyN_CharSequence_CharSequence_CharSequence_I_CharSequence_Str_k_(list, ", ")) + "]"
end function

function contentEquals_rShortArrayN_ShortArrayN_Z_k_(m as Dynamic, other as Dynamic) as Boolean
    if __kotlin_identityEquals(m, other) then
        return true
    end if
    if (m = invalid) or (other = invalid) then
        return false
    end if
    if m.get_size() <> other.get_size() then
        return false
    end if
    progression = until_rI_I_IntRange_k_(0, m.get_size())
    inductionVariable = progression.get_first()
    last = progression.get_last()
    if inductionVariable <= last then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if m.get_I_S_k_(i) <> other.get_I_S_k_(i) then
            return false
        end if


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if m.get_I_S_k_(i) <> other.get_I_S_k_(i) then
                return false
            end if

        end while

    end if

    return true
end function

function contentToString_rIntArrayN_Str_k_(m as Dynamic) as String
    if m = invalid then
        return "null"
    end if
    list = ArrayList_create_ArrayListAnyN_k_()
    indexedObject = m
    inductionVariable = 0
    last = indexedObject.get_size()
    while inductionVariable < last
        item = indexedObject.get_I_I_k_(inductionVariable)
        inductionVariable = (inductionVariable + 1)

        list.add_AnyN_Z_k_(__kotlin_numToStr_I_Str_k_(item))

    end while

    return ("[" + joinToString_rIterableAnyN_CharSequence_CharSequence_CharSequence_I_CharSequence_Str_k_(list, ", ")) + "]"
end function

function contentEquals_rIntArrayN_IntArrayN_Z_k_(m as Dynamic, other as Dynamic) as Boolean
    if __kotlin_identityEquals(m, other) then
        return true
    end if
    if (m = invalid) or (other = invalid) then
        return false
    end if
    if m.get_size() <> other.get_size() then
        return false
    end if
    progression = until_rI_I_IntRange_k_(0, m.get_size())
    inductionVariable = progression.get_first()
    last = progression.get_last()
    if inductionVariable <= last then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if m.get_I_I_k_(i) <> other.get_I_I_k_(i) then
            return false
        end if


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if m.get_I_I_k_(i) <> other.get_I_I_k_(i) then
                return false
            end if

        end while

    end if

    return true
end function

function contentToString_rLongArrayN_Str_k_(m as Dynamic) as String
    if m = invalid then
        return "null"
    end if
    list = ArrayList_create_ArrayListAnyN_k_()
    indexedObject = m
    inductionVariable = 0
    last = indexedObject.get_size()
    while inductionVariable < last
        item = indexedObject.get_I_J_k_(inductionVariable)
        inductionVariable = (inductionVariable + 1)

        list.add_AnyN_Z_k_(__kotlin_numToStr_J_Str_k_(item))

    end while

    return ("[" + joinToString_rIterableAnyN_CharSequence_CharSequence_CharSequence_I_CharSequence_Str_k_(list, ", ")) + "]"
end function

function contentEquals_rLongArrayN_LongArrayN_Z_k_(m as Dynamic, other as Dynamic) as Boolean
    if __kotlin_identityEquals(m, other) then
        return true
    end if
    if (m = invalid) or (other = invalid) then
        return false
    end if
    if m.get_size() <> other.get_size() then
        return false
    end if
    progression = until_rI_I_IntRange_k_(0, m.get_size())
    inductionVariable = progression.get_first()
    last = progression.get_last()
    if inductionVariable <= last then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if m.get_I_J_k_(i) <> other.get_I_J_k_(i) then
            return false
        end if


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if m.get_I_J_k_(i) <> other.get_I_J_k_(i) then
                return false
            end if

        end while

    end if

    return true
end function

function contentToString_rFloatArrayN_Str_k_(m as Dynamic) as String
    if m = invalid then
        return "null"
    end if
    list = ArrayList_create_ArrayListAnyN_k_()
    indexedObject = m
    inductionVariable = 0
    last = indexedObject.get_size()
    while inductionVariable < last
        item = indexedObject.get_I_F_k_(inductionVariable)
        inductionVariable = (inductionVariable + 1)

        list.add_AnyN_Z_k_(__kotlin_numToStr_F_Str_k_(item))

    end while

    return ("[" + joinToString_rIterableAnyN_CharSequence_CharSequence_CharSequence_I_CharSequence_Str_k_(list, ", ")) + "]"
end function

function contentEquals_rFloatArrayN_FloatArrayN_Z_k_(m as Dynamic, other as Dynamic) as Boolean
    if __kotlin_identityEquals(m, other) then
        return true
    end if
    if (m = invalid) or (other = invalid) then
        return false
    end if
    if m.get_size() <> other.get_size() then
        return false
    end if
    progression = until_rI_I_IntRange_k_(0, m.get_size())
    inductionVariable = progression.get_first()
    last = progression.get_last()
    if inductionVariable <= last then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if m.get_I_F_k_(i) <> other.get_I_F_k_(i) then
            return false
        end if


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if m.get_I_F_k_(i) <> other.get_I_F_k_(i) then
                return false
            end if

        end while

    end if

    return true
end function

function contentToString_rDoubleArrayN_Str_k_(m as Dynamic) as String
    if m = invalid then
        return "null"
    end if
    list = ArrayList_create_ArrayListAnyN_k_()
    indexedObject = m
    inductionVariable = 0
    last = indexedObject.get_size()
    while inductionVariable < last
        item = indexedObject.get_I_D_k_(inductionVariable)
        inductionVariable = (inductionVariable + 1)

        list.add_AnyN_Z_k_(__kotlin_numToStr_D_Str_k_(item))

    end while

    return ("[" + joinToString_rIterableAnyN_CharSequence_CharSequence_CharSequence_I_CharSequence_Str_k_(list, ", ")) + "]"
end function

function contentEquals_rDoubleArrayN_DoubleArrayN_Z_k_(m as Dynamic, other as Dynamic) as Boolean
    if __kotlin_identityEquals(m, other) then
        return true
    end if
    if (m = invalid) or (other = invalid) then
        return false
    end if
    if m.get_size() <> other.get_size() then
        return false
    end if
    progression = until_rI_I_IntRange_k_(0, m.get_size())
    inductionVariable = progression.get_first()
    last = progression.get_last()
    if inductionVariable <= last then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if m.get_I_D_k_(i) <> other.get_I_D_k_(i) then
            return false
        end if


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if m.get_I_D_k_(i) <> other.get_I_D_k_(i) then
                return false
            end if

        end while

    end if

    return true
end function

function contentToString_rBooleanArrayN_Str_k_(m as Dynamic) as String
    if m = invalid then
        return "null"
    end if
    list = ArrayList_create_ArrayListAnyN_k_()
    indexedObject = m
    inductionVariable = 0
    last = indexedObject.get_size()
    while inductionVariable < last
        item = indexedObject.get_I_Z_k_(inductionVariable)
        inductionVariable = (inductionVariable + 1)

        list.add_AnyN_Z_k_((function(item)
            if item then return "true" else return "false"
        end function)(item))

    end while

    return ("[" + joinToString_rIterableAnyN_CharSequence_CharSequence_CharSequence_I_CharSequence_Str_k_(list, ", ")) + "]"
end function

function contentEquals_rBooleanArrayN_BooleanArrayN_Z_k_(m as Dynamic, other as Dynamic) as Boolean
    if __kotlin_identityEquals(m, other) then
        return true
    end if
    if (m = invalid) or (other = invalid) then
        return false
    end if
    if m.get_size() <> other.get_size() then
        return false
    end if
    progression = until_rI_I_IntRange_k_(0, m.get_size())
    inductionVariable = progression.get_first()
    last = progression.get_last()
    if inductionVariable <= last then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if m.get_I_Z_k_(i) <> other.get_I_Z_k_(i) then
            return false
        end if


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if m.get_I_Z_k_(i) <> other.get_I_Z_k_(i) then
                return false
            end if

        end while

    end if

    return true
end function

function contentToString_rCharArrayN_Str_k_(m as Dynamic) as String
    if m = invalid then
        return "null"
    end if
    list = ArrayList_create_ArrayListAnyN_k_()
    indexedObject = m
    inductionVariable = 0
    last = indexedObject.get_size()
    while inductionVariable < last
        item = indexedObject.get_I_C_k_(inductionVariable)
        inductionVariable = (inductionVariable + 1)

        list.add_AnyN_Z_k_(item.toString())

    end while

    return ("[" + joinToString_rIterableAnyN_CharSequence_CharSequence_CharSequence_I_CharSequence_Str_k_(list, ", ")) + "]"
end function

function contentEquals_rCharArrayN_CharArrayN_Z_k_(m as Dynamic, other as Dynamic) as Boolean
    if __kotlin_identityEquals(m, other) then
        return true
    end if
    if (m = invalid) or (other = invalid) then
        return false
    end if
    if m.get_size() <> other.get_size() then
        return false
    end if
    progression = until_rI_I_IntRange_k_(0, m.get_size())
    inductionVariable = progression.get_first()
    last = progression.get_last()
    if inductionVariable <= last then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if m.get_I_C_k_(i) <> other.get_I_C_k_(i) then
            return false
        end if


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if m.get_I_C_k_(i) <> other.get_I_C_k_(i) then
                return false
            end if

        end while

    end if

    return true
end function
