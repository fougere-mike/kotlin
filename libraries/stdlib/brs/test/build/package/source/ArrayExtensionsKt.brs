function contentToString_rArrN_k_(m as Dynamic) as String
    if m = invalid then
        return "null"
    end if
    sb = StringBuilder_create_k_()
    sb.append_StrN_k_("[")
    progression = until_rI_I_k_(0, m.count())
    inductionVariable = progression.get_first()
    last = progression.get_last()
    if inductionVariable <= last then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if i > 0 then
            sb.append_StrN_k_(", ")
        end if
        sb.append_AnyN_k_(m[i])


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if i > 0 then
                sb.append_StrN_k_(", ")
            end if
            sb.append_AnyN_k_(m[i])

        end while

    end if

    sb.append_StrN_k_("]")
    return sb.toString()
end function

function contentEquals_rArrN_ArrN_k_(m as Dynamic, other as Dynamic) as Boolean
    if __kotlin_identityEquals(m, other) then
        return true
    end if
    if (m = invalid) or (other = invalid) then
        return false
    end if
    if m.count() <> other.count() then
        return false
    end if
    progression = until_rI_I_k_(0, m.count())
    inductionVariable = progression.get_first()
    last = progression.get_last()
    if inductionVariable <= last then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if not brsStructuralEquals_AnyN_AnyN_k_(m[i], other[i]) then
            return false
        end if


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if not brsStructuralEquals_AnyN_AnyN_k_(m[i], other[i]) then
                return false
            end if

        end while

    end if

    return true
end function

function contentToString_rByteArrayN_k_(m as Dynamic) as String
    if m = invalid then
        return "null"
    end if
    list = ArrayList_create_k_()
    indexedObject = m
    inductionVariable = 0
    last = indexedObject.get_size()
    while inductionVariable < last
        item = indexedObject.get_I_k_(inductionVariable)
        inductionVariable = (inductionVariable + 1)

        list.add_AnyN_k_(__kotlin_numToStr_I_k_(item))

    end while

    return ("[" + joinToString_rIterable_CharSequence_CharSequence_CharSequence_I_CharSequence_k_(list, ", ", invalid, invalid, invalid, invalid)) + "]"
end function

function contentEquals_rByteArrayN_ByteArrayN_k_(m as Dynamic, other as Dynamic) as Boolean
    if __kotlin_identityEquals(m, other) then
        return true
    end if
    if (m = invalid) or (other = invalid) then
        return false
    end if
    if m.get_size() <> other.get_size() then
        return false
    end if
    progression = until_rI_I_k_(0, m.get_size())
    inductionVariable = progression.get_first()
    last = progression.get_last()
    if inductionVariable <= last then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if m.get_I_k_(i) <> other.get_I_k_(i) then
            return false
        end if


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if m.get_I_k_(i) <> other.get_I_k_(i) then
                return false
            end if

        end while

    end if

    return true
end function

function contentToString_rShortArrayN_k_(m as Dynamic) as String
    if m = invalid then
        return "null"
    end if
    list = ArrayList_create_k_()
    indexedObject = m
    inductionVariable = 0
    last = indexedObject.get_size()
    while inductionVariable < last
        item = indexedObject.get_I_k_(inductionVariable)
        inductionVariable = (inductionVariable + 1)

        list.add_AnyN_k_(__kotlin_numToStr_I_k_(item))

    end while

    return ("[" + joinToString_rIterable_CharSequence_CharSequence_CharSequence_I_CharSequence_k_(list, ", ", invalid, invalid, invalid, invalid)) + "]"
end function

function contentEquals_rShortArrayN_ShortArrayN_k_(m as Dynamic, other as Dynamic) as Boolean
    if __kotlin_identityEquals(m, other) then
        return true
    end if
    if (m = invalid) or (other = invalid) then
        return false
    end if
    if m.get_size() <> other.get_size() then
        return false
    end if
    progression = until_rI_I_k_(0, m.get_size())
    inductionVariable = progression.get_first()
    last = progression.get_last()
    if inductionVariable <= last then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if m.get_I_k_(i) <> other.get_I_k_(i) then
            return false
        end if


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if m.get_I_k_(i) <> other.get_I_k_(i) then
                return false
            end if

        end while

    end if

    return true
end function

function contentToString_rIntArrayN_k_(m as Dynamic) as String
    if m = invalid then
        return "null"
    end if
    list = ArrayList_create_k_()
    indexedObject = m
    inductionVariable = 0
    last = indexedObject.get_size()
    while inductionVariable < last
        item = indexedObject.get_I_k_(inductionVariable)
        inductionVariable = (inductionVariable + 1)

        list.add_AnyN_k_(__kotlin_numToStr_I_k_(item))

    end while

    return ("[" + joinToString_rIterable_CharSequence_CharSequence_CharSequence_I_CharSequence_k_(list, ", ", invalid, invalid, invalid, invalid)) + "]"
end function

function contentEquals_rIntArrayN_IntArrayN_k_(m as Dynamic, other as Dynamic) as Boolean
    if __kotlin_identityEquals(m, other) then
        return true
    end if
    if (m = invalid) or (other = invalid) then
        return false
    end if
    if m.get_size() <> other.get_size() then
        return false
    end if
    progression = until_rI_I_k_(0, m.get_size())
    inductionVariable = progression.get_first()
    last = progression.get_last()
    if inductionVariable <= last then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if m.get_I_k_(i) <> other.get_I_k_(i) then
            return false
        end if


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if m.get_I_k_(i) <> other.get_I_k_(i) then
                return false
            end if

        end while

    end if

    return true
end function

function contentToString_rLongArrayN_k_(m as Dynamic) as String
    if m = invalid then
        return "null"
    end if
    list = ArrayList_create_k_()
    indexedObject = m
    inductionVariable = 0
    last = indexedObject.get_size()
    while inductionVariable < last
        item = indexedObject.get_I_k_(inductionVariable)
        inductionVariable = (inductionVariable + 1)

        list.add_AnyN_k_(__kotlin_numToStr_J_k_(item))

    end while

    return ("[" + joinToString_rIterable_CharSequence_CharSequence_CharSequence_I_CharSequence_k_(list, ", ", invalid, invalid, invalid, invalid)) + "]"
end function

function contentEquals_rLongArrayN_LongArrayN_k_(m as Dynamic, other as Dynamic) as Boolean
    if __kotlin_identityEquals(m, other) then
        return true
    end if
    if (m = invalid) or (other = invalid) then
        return false
    end if
    if m.get_size() <> other.get_size() then
        return false
    end if
    progression = until_rI_I_k_(0, m.get_size())
    inductionVariable = progression.get_first()
    last = progression.get_last()
    if inductionVariable <= last then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if m.get_I_k_(i) <> other.get_I_k_(i) then
            return false
        end if


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if m.get_I_k_(i) <> other.get_I_k_(i) then
                return false
            end if

        end while

    end if

    return true
end function

function contentToString_rFloatArrayN_k_(m as Dynamic) as String
    if m = invalid then
        return "null"
    end if
    list = ArrayList_create_k_()
    indexedObject = m
    inductionVariable = 0
    last = indexedObject.get_size()
    while inductionVariable < last
        item = indexedObject.get_I_k_(inductionVariable)
        inductionVariable = (inductionVariable + 1)

        list.add_AnyN_k_(__kotlin_numToStr_F_k_(item))

    end while

    return ("[" + joinToString_rIterable_CharSequence_CharSequence_CharSequence_I_CharSequence_k_(list, ", ", invalid, invalid, invalid, invalid)) + "]"
end function

function contentEquals_rFloatArrayN_FloatArrayN_k_(m as Dynamic, other as Dynamic) as Boolean
    if __kotlin_identityEquals(m, other) then
        return true
    end if
    if (m = invalid) or (other = invalid) then
        return false
    end if
    if m.get_size() <> other.get_size() then
        return false
    end if
    progression = until_rI_I_k_(0, m.get_size())
    inductionVariable = progression.get_first()
    last = progression.get_last()
    if inductionVariable <= last then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if m.get_I_k_(i) <> other.get_I_k_(i) then
            return false
        end if


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if m.get_I_k_(i) <> other.get_I_k_(i) then
                return false
            end if

        end while

    end if

    return true
end function

function contentToString_rDoubleArrayN_k_(m as Dynamic) as String
    if m = invalid then
        return "null"
    end if
    list = ArrayList_create_k_()
    indexedObject = m
    inductionVariable = 0
    last = indexedObject.get_size()
    while inductionVariable < last
        item = indexedObject.get_I_k_(inductionVariable)
        inductionVariable = (inductionVariable + 1)

        list.add_AnyN_k_(__kotlin_numToStr_D_k_(item))

    end while

    return ("[" + joinToString_rIterable_CharSequence_CharSequence_CharSequence_I_CharSequence_k_(list, ", ", invalid, invalid, invalid, invalid)) + "]"
end function

function contentEquals_rDoubleArrayN_DoubleArrayN_k_(m as Dynamic, other as Dynamic) as Boolean
    if __kotlin_identityEquals(m, other) then
        return true
    end if
    if (m = invalid) or (other = invalid) then
        return false
    end if
    if m.get_size() <> other.get_size() then
        return false
    end if
    progression = until_rI_I_k_(0, m.get_size())
    inductionVariable = progression.get_first()
    last = progression.get_last()
    if inductionVariable <= last then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if m.get_I_k_(i) <> other.get_I_k_(i) then
            return false
        end if


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if m.get_I_k_(i) <> other.get_I_k_(i) then
                return false
            end if

        end while

    end if

    return true
end function

function contentToString_rBooleanArrayN_k_(m as Dynamic) as String
    if m = invalid then
        return "null"
    end if
    list = ArrayList_create_k_()
    indexedObject = m
    inductionVariable = 0
    last = indexedObject.get_size()
    while inductionVariable < last
        item = indexedObject.get_I_k_(inductionVariable)
        inductionVariable = (inductionVariable + 1)

        list.add_AnyN_k_((function(item)
            if item then return "true" else return "false"
        end function)(item))

    end while

    return ("[" + joinToString_rIterable_CharSequence_CharSequence_CharSequence_I_CharSequence_k_(list, ", ", invalid, invalid, invalid, invalid)) + "]"
end function

function contentEquals_rBooleanArrayN_BooleanArrayN_k_(m as Dynamic, other as Dynamic) as Boolean
    if __kotlin_identityEquals(m, other) then
        return true
    end if
    if (m = invalid) or (other = invalid) then
        return false
    end if
    if m.get_size() <> other.get_size() then
        return false
    end if
    progression = until_rI_I_k_(0, m.get_size())
    inductionVariable = progression.get_first()
    last = progression.get_last()
    if inductionVariable <= last then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if m.get_I_k_(i) <> other.get_I_k_(i) then
            return false
        end if


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if m.get_I_k_(i) <> other.get_I_k_(i) then
                return false
            end if

        end while

    end if

    return true
end function

function contentToString_rCharArrayN_k_(m as Dynamic) as String
    if m = invalid then
        return "null"
    end if
    list = ArrayList_create_k_()
    indexedObject = m
    inductionVariable = 0
    last = indexedObject.get_size()
    while inductionVariable < last
        item = indexedObject.get_I_k_(inductionVariable)
        inductionVariable = (inductionVariable + 1)

        list.add_AnyN_k_(item)

    end while

    return ("[" + joinToString_rIterable_CharSequence_CharSequence_CharSequence_I_CharSequence_k_(list, ", ", invalid, invalid, invalid, invalid)) + "]"
end function

function contentEquals_rCharArrayN_CharArrayN_k_(m as Dynamic, other as Dynamic) as Boolean
    if __kotlin_identityEquals(m, other) then
        return true
    end if
    if (m = invalid) or (other = invalid) then
        return false
    end if
    if m.get_size() <> other.get_size() then
        return false
    end if
    progression = until_rI_I_k_(0, m.get_size())
    inductionVariable = progression.get_first()
    last = progression.get_last()
    if inductionVariable <= last then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if m.get_I_k_(i) <> other.get_I_k_(i) then
            return false
        end if


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if m.get_I_k_(i) <> other.get_I_k_(i) then
                return false
            end if

        end while

    end if

    return true
end function
