function contentToString_rArrN_Str_k_(m as Dynamic) as String
    if m = invalid then
        return "null"
    end if
    sb = StringBuilder_create_StringBuilder_k_()
    sb.append("[")
    progression = until_rI_I_IntRange_k_(0, m.size)
    inductionVariable = progression.first
    last = progression.last
    if lessOrEqual_I_I_Z_k_(inductionVariable, last) then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if i > 0 then
            sb.append(", ")
        end if
        sb.append(m[i])


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if i > 0 then
                sb.append(", ")
            end if
            sb.append(m[i])

        end while

    end if

    sb.append("]")
    return sb.toString()
end function

function contentEquals_rArrN_ArrN_Z_k_(m as Dynamic, other as Dynamic) as Boolean
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

function contentToString_rByteArrayN_Str_k_(m as Dynamic) as String
    if m = invalid then
        return "null"
    end if
    list = ArrayList_create_ArrayListAnyN_k_()
    indexedObject = m
    inductionVariable = 0
    last = indexedObject.size
    while less_I_I_Z_k_(inductionVariable, last)
        item = indexedObject.get(inductionVariable)
        inductionVariable = (inductionVariable + 1)

        list.add(Str(item))

    end while

    return ("[" + joinToString_rIterableAnyN_CharSequence_CharSequence_CharSequence_I_CharSequence_Str_k_(list, ", ")) + "]"
end function

function contentEquals_rByteArrayN_ByteArrayN_Z_k_(m as Dynamic, other as Dynamic) as Boolean
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

function contentToString_rShortArrayN_Str_k_(m as Dynamic) as String
    if m = invalid then
        return "null"
    end if
    list = ArrayList_create_ArrayListAnyN_k_()
    indexedObject = m
    inductionVariable = 0
    last = indexedObject.size
    while less_I_I_Z_k_(inductionVariable, last)
        item = indexedObject.get(inductionVariable)
        inductionVariable = (inductionVariable + 1)

        list.add(Str(item))

    end while

    return ("[" + joinToString_rIterableAnyN_CharSequence_CharSequence_CharSequence_I_CharSequence_Str_k_(list, ", ")) + "]"
end function

function contentEquals_rShortArrayN_ShortArrayN_Z_k_(m as Dynamic, other as Dynamic) as Boolean
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

function contentToString_rIntArrayN_Str_k_(m as Dynamic) as String
    if m = invalid then
        return "null"
    end if
    list = ArrayList_create_ArrayListAnyN_k_()
    indexedObject = m
    inductionVariable = 0
    last = indexedObject.size
    while less_I_I_Z_k_(inductionVariable, last)
        item = indexedObject.get(inductionVariable)
        inductionVariable = (inductionVariable + 1)

        list.add(Str(item))

    end while

    return ("[" + joinToString_rIterableAnyN_CharSequence_CharSequence_CharSequence_I_CharSequence_Str_k_(list, ", ")) + "]"
end function

function contentEquals_rIntArrayN_IntArrayN_Z_k_(m as Dynamic, other as Dynamic) as Boolean
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

function contentToString_rLongArrayN_Str_k_(m as Dynamic) as String
    if m = invalid then
        return "null"
    end if
    list = ArrayList_create_ArrayListAnyN_k_()
    indexedObject = m
    inductionVariable = 0
    last = indexedObject.size
    while less_I_I_Z_k_(inductionVariable, last)
        item = indexedObject.get(inductionVariable)
        inductionVariable = (inductionVariable + 1)

        list.add(Str(item))

    end while

    return ("[" + joinToString_rIterableAnyN_CharSequence_CharSequence_CharSequence_I_CharSequence_Str_k_(list, ", ")) + "]"
end function

function contentEquals_rLongArrayN_LongArrayN_Z_k_(m as Dynamic, other as Dynamic) as Boolean
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

function contentToString_rFloatArrayN_Str_k_(m as Dynamic) as String
    if m = invalid then
        return "null"
    end if
    list = ArrayList_create_ArrayListAnyN_k_()
    indexedObject = m
    inductionVariable = 0
    last = indexedObject.size
    while less_I_I_Z_k_(inductionVariable, last)
        item = indexedObject.get(inductionVariable)
        inductionVariable = (inductionVariable + 1)

        list.add(Str(item))

    end while

    return ("[" + joinToString_rIterableAnyN_CharSequence_CharSequence_CharSequence_I_CharSequence_Str_k_(list, ", ")) + "]"
end function

function contentEquals_rFloatArrayN_FloatArrayN_Z_k_(m as Dynamic, other as Dynamic) as Boolean
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

function contentToString_rDoubleArrayN_Str_k_(m as Dynamic) as String
    if m = invalid then
        return "null"
    end if
    list = ArrayList_create_ArrayListAnyN_k_()
    indexedObject = m
    inductionVariable = 0
    last = indexedObject.size
    while less_I_I_Z_k_(inductionVariable, last)
        item = indexedObject.get(inductionVariable)
        inductionVariable = (inductionVariable + 1)

        list.add(Str(item))

    end while

    return ("[" + joinToString_rIterableAnyN_CharSequence_CharSequence_CharSequence_I_CharSequence_Str_k_(list, ", ")) + "]"
end function

function contentEquals_rDoubleArrayN_DoubleArrayN_Z_k_(m as Dynamic, other as Dynamic) as Boolean
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

function contentToString_rBooleanArrayN_Str_k_(m as Dynamic) as String
    if m = invalid then
        return "null"
    end if
    list = ArrayList_create_ArrayListAnyN_k_()
    indexedObject = m
    inductionVariable = 0
    last = indexedObject.size
    while less_I_I_Z_k_(inductionVariable, last)
        item = indexedObject.get(inductionVariable)
        inductionVariable = (inductionVariable + 1)

        list.add((function(item)
            if item then return "true" else return "false"
        end function)(item))

    end while

    return ("[" + joinToString_rIterableAnyN_CharSequence_CharSequence_CharSequence_I_CharSequence_Str_k_(list, ", ")) + "]"
end function

function contentEquals_rBooleanArrayN_BooleanArrayN_Z_k_(m as Dynamic, other as Dynamic) as Boolean
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

function contentToString_rCharArrayN_Str_k_(m as Dynamic) as String
    if m = invalid then
        return "null"
    end if
    list = ArrayList_create_ArrayListAnyN_k_()
    indexedObject = m
    inductionVariable = 0
    last = indexedObject.size
    while less_I_I_Z_k_(inductionVariable, last)
        item = indexedObject.get(inductionVariable)
        inductionVariable = (inductionVariable + 1)

        list.add(item.toString())

    end while

    return ("[" + joinToString_rIterableAnyN_CharSequence_CharSequence_CharSequence_I_CharSequence_Str_k_(list, ", ")) + "]"
end function

function contentEquals_rCharArrayN_CharArrayN_Z_k_(m as Dynamic, other as Dynamic) as Boolean
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
