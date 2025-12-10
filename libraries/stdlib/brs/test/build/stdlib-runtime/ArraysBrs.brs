function toTypedArray_rCollection_k_(m as Object) as Object
    result = arrayOfNulls_I_k_(m.get_size())
    index = 0
    __iter_1 = m.iterator_k_()
    while __iter_1.hasNext_k_()
        element = __iter_1.next_k_()
        __incr_tmp_0 = index
        index = (__incr_tmp_0 + 1)

        result[__incr_tmp_0] = element

    end while

    return result
end function

function plus_rArr_Collection_k_(m as Object, elements as Object) as Object
    result = copyOf_rArr_I_k_(m, m.count() + elements.get_size())
    index = m.count()
    __iter_3 = elements.iterator_k_()
    while __iter_3.hasNext_k_()
        element = __iter_3.next_k_()
        __incr_tmp_2 = index
        index = (__incr_tmp_2 + 1)

        result[__incr_tmp_2] = element

    end while

    return result
end function

function plus_rArr_AnyN_k_(m as Object, element as Dynamic) as Object
    result = copyOf_rArr_I_k_(m, m.count() + 1)
    result[m.count()] = element
    return result
end function

function plus_rArr_Arr_k_(m as Object, elements as Object) as Object
    result = copyOf_rArr_I_k_(m, m.count() + elements.count())
    index = m.count()
    indexedObject = elements
    inductionVariable = 0
    last = indexedObject.count()
    while inductionVariable < last
        element = indexedObject[inductionVariable]
        inductionVariable = (inductionVariable + 1)

        __incr_tmp_4 = index
        index = (__incr_tmp_4 + 1)

        result[__incr_tmp_4] = element

    end while

    return result
end function

sub fill_rArr_AnyN_I_I_k_(m as Object, element as Dynamic, fromIndex = 0, toIndex = m.count())
    if (fromIndex < 0) or (toIndex > m.count()) then
        throw IndexOutOfBoundsException_create_StrN_k_((((("fromIndex: " + fromIndex) + ", toIndex: ") + toIndex) + ", size: ") + m.count())
    end if
    if fromIndex > toIndex then
        throw IllegalArgumentException_create_StrN_k_((("fromIndex: " + fromIndex) + " > toIndex: ") + toIndex)
    end if
    progression = until_rI_I_k_(fromIndex, toIndex)
    inductionVariable = progression.get_first()
    last = progression.get_last()
    if inductionVariable <= last then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        m[i] = element


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            m[i] = element

        end while

    end if

end sub

function copyOf_rArr_k_(m as Object) as Object
    return copyOf_rArr_I_k_(m, m.count())
end function

function copyOf_rArr_I_k_(m as Object, newSize as Integer) as Object
    if newSize < 0 then
        throw IllegalArgumentException_create_StrN_k_("Invalid new array size: " + newSize)
    end if
    result = arrayOfNulls_I_k_(newSize)
    __when_tmp0 = invalid
    if newSize < m.count() then
        __when_tmp0 = newSize
    else if true then
        __when_tmp0 = m.count()
    end if
    copySize = __when_tmp0

    progression = until_rI_I_k_(0, copySize)
    inductionVariable = progression.get_first()
    last = progression.get_last()
    if inductionVariable <= last then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        result[i] = m[i]


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            result[i] = m[i]

        end while

    end if

    return result
end function

function copyOfRange_rArr_I_I_k_(m as Object, fromIndex as Integer, toIndex as Integer) as Object
    if (fromIndex < 0) or (toIndex > m.count()) then
        throw IndexOutOfBoundsException_create_StrN_k_((((("fromIndex: " + fromIndex) + ", toIndex: ") + toIndex) + ", size: ") + m.count())
    end if
    if fromIndex > toIndex then
        throw IllegalArgumentException_create_StrN_k_((("fromIndex: " + fromIndex) + " > toIndex: ") + toIndex)
    end if
    result = arrayOfNulls_I_k_(toIndex - fromIndex)
    resultIndex = 0
    progression = until_rI_I_k_(fromIndex, toIndex)
    inductionVariable = progression.get_first()
    last = progression.get_last()
    if inductionVariable <= last then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        __incr_tmp_5 = resultIndex
        resultIndex = (__incr_tmp_5 + 1)

        result[__incr_tmp_5] = m[i]


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            __incr_tmp_5 = resultIndex
            resultIndex = (__incr_tmp_5 + 1)

            result[__incr_tmp_5] = m[i]

        end while

    end if

    return result
end function

sub reverse_rArr_k_(m as Object)
    midPoint = m.count() / 2
    i = 0
    while i < midPoint
        tmp = m[i]
        m[i] = m[(m.count() - i) - 1]
        m[(m.count() - i) - 1] = tmp
        i = (i + 1)
    end while
end sub

sub reverse_rArr_I_I_k_(m as Object, fromIndex as Integer, toIndex as Integer)
    if (fromIndex < 0) or (toIndex > m.count()) then
        throw IndexOutOfBoundsException_create_StrN_k_((((("fromIndex: " + fromIndex) + ", toIndex: ") + toIndex) + ", size: ") + m.count())
    end if
    if fromIndex >= toIndex then
        return
    end if
    midPoint = (fromIndex + toIndex) / 2
    i = fromIndex
    while i < midPoint
        tmp = m[i]
        m[i] = m[((toIndex - i) + fromIndex) - 1]
        m[((toIndex - i) + fromIndex) - 1] = tmp
        i = (i + 1)
    end while
end sub

function toList_rArr_k_(m as Object) as Object
    result = ArrayList_create_I_k_(m.count())
    indexedObject = m
    inductionVariable = 0
    last = indexedObject.count()
    while inductionVariable < last
        element = indexedObject[inductionVariable]
        inductionVariable = (inductionVariable + 1)

        result.add_AnyN_k_(element)

    end while

    return result
end function

function toMutableList_rArr_k_(m as Object) as Object
    result = ArrayList_create_I_k_(m.count())
    indexedObject = m
    inductionVariable = 0
    last = indexedObject.count()
    while inductionVariable < last
        element = indexedObject[inductionVariable]
        inductionVariable = (inductionVariable + 1)

        result.add_AnyN_k_(element)

    end while

    return result
end function

function toSet_rArr_k_(m as Object) as Object
    result = HashSet_create_k_()
    indexedObject = m
    inductionVariable = 0
    last = indexedObject.count()
    while inductionVariable < last
        element = indexedObject[inductionVariable]
        inductionVariable = (inductionVariable + 1)

        result.add_AnyN_k_(element)

    end while

    return result
end function

function toMutableSet_rArr_k_(m as Object) as Object
    result = HashSet_create_k_()
    indexedObject = m
    inductionVariable = 0
    last = indexedObject.count()
    while inductionVariable < last
        element = indexedObject[inductionVariable]
        inductionVariable = (inductionVariable + 1)

        result.add_AnyN_k_(element)

    end while

    return result
end function

function isNotEmpty_rArr_k_(m as Object) as Boolean
    return not isEmpty_rArr_k_(m)
end function

function isEmpty_rArr_k_(m as Object) as Boolean
    return m.count() = 0
end function

function orEmpty_rArrN_k_(m as Dynamic) as Object
    tmp0_elvis_lhs = m
    __when_tmp1 = invalid
    if tmp0_elvis_lhs = invalid then
        __when_tmp1 = emptyArray_k_()
    else if true then
        __when_tmp1 = tmp0_elvis_lhs
    end if
    return __when_tmp1

end function

function emptyArray_k_() as Object
    return arrayOfNulls_I_k_(0)
end function
