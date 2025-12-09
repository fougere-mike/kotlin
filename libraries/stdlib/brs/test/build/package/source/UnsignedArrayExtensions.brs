function contentToString_rUByteArrayN_Str_k_(m as Dynamic) as String
    if m = invalid then
        return "null"
    end if
    list = ArrayList_create_ArrayListAnyN_k_()
    for each item in m
        list.add_AnyN_Z_k_(item.toString())

    end for
    return ("[" + joinToString_rIterableAnyN_CharSequence_CharSequence_CharSequence_I_CharSequence_Str_k_(list, ", ")) + "]"
end function

function contentEquals_rUByteArrayN_UByteArrayN_Z_k_(m as Dynamic, other as Dynamic) as Boolean
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

        if m.get_I_UByte_k_(i) <> other.get_I_UByte_k_(i) then
            return false
        end if


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if m.get_I_UByte_k_(i) <> other.get_I_UByte_k_(i) then
                return false
            end if

        end while

    end if

    return true
end function

function contentToString_rUShortArrayN_Str_k_(m as Dynamic) as String
    if m = invalid then
        return "null"
    end if
    list = ArrayList_create_ArrayListAnyN_k_()
    for each item in m
        list.add_AnyN_Z_k_(item.toString())

    end for
    return ("[" + joinToString_rIterableAnyN_CharSequence_CharSequence_CharSequence_I_CharSequence_Str_k_(list, ", ")) + "]"
end function

function contentEquals_rUShortArrayN_UShortArrayN_Z_k_(m as Dynamic, other as Dynamic) as Boolean
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

        if m.get_I_UShort_k_(i) <> other.get_I_UShort_k_(i) then
            return false
        end if


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if m.get_I_UShort_k_(i) <> other.get_I_UShort_k_(i) then
                return false
            end if

        end while

    end if

    return true
end function

function contentToString_rUIntArrayN_Str_k_(m as Dynamic) as String
    if m = invalid then
        return "null"
    end if
    list = ArrayList_create_ArrayListAnyN_k_()
    for each item in m
        list.add_AnyN_Z_k_(item.toString())

    end for
    return ("[" + joinToString_rIterableAnyN_CharSequence_CharSequence_CharSequence_I_CharSequence_Str_k_(list, ", ")) + "]"
end function

function contentEquals_rUIntArrayN_UIntArrayN_Z_k_(m as Dynamic, other as Dynamic) as Boolean
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

        if m.get_I_UInt_k_(i) <> other.get_I_UInt_k_(i) then
            return false
        end if


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if m.get_I_UInt_k_(i) <> other.get_I_UInt_k_(i) then
                return false
            end if

        end while

    end if

    return true
end function

function contentToString_rULongArrayN_Str_k_(m as Dynamic) as String
    if m = invalid then
        return "null"
    end if
    list = ArrayList_create_ArrayListAnyN_k_()
    for each item in m
        list.add_AnyN_Z_k_(item.toString())

    end for
    return ("[" + joinToString_rIterableAnyN_CharSequence_CharSequence_CharSequence_I_CharSequence_Str_k_(list, ", ")) + "]"
end function

function contentEquals_rULongArrayN_ULongArrayN_Z_k_(m as Dynamic, other as Dynamic) as Boolean
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

        if m.get_I_ULong_k_(i) <> other.get_I_ULong_k_(i) then
            return false
        end if


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if m.get_I_ULong_k_(i) <> other.get_I_ULong_k_(i) then
                return false
            end if

        end while

    end if

    return true
end function
