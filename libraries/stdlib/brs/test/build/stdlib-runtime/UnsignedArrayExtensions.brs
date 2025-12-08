function contentToString_rUByteArrayN_Str_k_(m as Dynamic) as String
    if m = invalid then
        return "null"
    end if
    list = ArrayList_create_ArrayListAnyN_k_()
    for each item in m
        list.add(item.toString())

    end for
    return ("[" + joinToString_rIterableAnyN_CharSequence_CharSequence_CharSequence_I_CharSequence_Str_k_(list, ", ")) + "]"
end function

function contentEquals_rUByteArrayN_UByteArrayN_Z_k_(m as Dynamic, other as Dynamic) as Boolean
    if EQEQEQ_AnyN_AnyN_Z_k_(m, other) then
        return true
    end if
    if (m = invalid) or (other = invalid) then
        return false
    end if
    if m.size <> other.size then
        return false
    end if
    progression = until_rI_I_IntRange_k_(0, m.size)
    inductionVariable = progression.first
    last = progression.last
    if lessOrEqual_I_I_Z_k_(inductionVariable, last) then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if m[i] <> other[i] then
            return false
        end if


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if m[i] <> other[i] then
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
        list.add(item.toString())

    end for
    return ("[" + joinToString_rIterableAnyN_CharSequence_CharSequence_CharSequence_I_CharSequence_Str_k_(list, ", ")) + "]"
end function

function contentEquals_rUShortArrayN_UShortArrayN_Z_k_(m as Dynamic, other as Dynamic) as Boolean
    if EQEQEQ_AnyN_AnyN_Z_k_(m, other) then
        return true
    end if
    if (m = invalid) or (other = invalid) then
        return false
    end if
    if m.size <> other.size then
        return false
    end if
    progression = until_rI_I_IntRange_k_(0, m.size)
    inductionVariable = progression.first
    last = progression.last
    if lessOrEqual_I_I_Z_k_(inductionVariable, last) then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if m[i] <> other[i] then
            return false
        end if


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if m[i] <> other[i] then
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
        list.add(item.toString())

    end for
    return ("[" + joinToString_rIterableAnyN_CharSequence_CharSequence_CharSequence_I_CharSequence_Str_k_(list, ", ")) + "]"
end function

function contentEquals_rUIntArrayN_UIntArrayN_Z_k_(m as Dynamic, other as Dynamic) as Boolean
    if EQEQEQ_AnyN_AnyN_Z_k_(m, other) then
        return true
    end if
    if (m = invalid) or (other = invalid) then
        return false
    end if
    if m.size <> other.size then
        return false
    end if
    progression = until_rI_I_IntRange_k_(0, m.size)
    inductionVariable = progression.first
    last = progression.last
    if lessOrEqual_I_I_Z_k_(inductionVariable, last) then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if m[i] <> other[i] then
            return false
        end if


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if m[i] <> other[i] then
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
        list.add(item.toString())

    end for
    return ("[" + joinToString_rIterableAnyN_CharSequence_CharSequence_CharSequence_I_CharSequence_Str_k_(list, ", ")) + "]"
end function

function contentEquals_rULongArrayN_ULongArrayN_Z_k_(m as Dynamic, other as Dynamic) as Boolean
    if EQEQEQ_AnyN_AnyN_Z_k_(m, other) then
        return true
    end if
    if (m = invalid) or (other = invalid) then
        return false
    end if
    if m.size <> other.size then
        return false
    end if
    progression = until_rI_I_IntRange_k_(0, m.size)
    inductionVariable = progression.first
    last = progression.last
    if lessOrEqual_I_I_Z_k_(inductionVariable, last) then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if m[i] <> other[i] then
            return false
        end if


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if m[i] <> other[i] then
                return false
            end if

        end while

    end if

    return true
end function
