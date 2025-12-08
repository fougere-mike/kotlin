function toTypedArray_rCollectionAnyN_Arr_k_(m as Object) as Object
    result = arrayOfNulls_I_Arr_k_(m.size)
    index = 0
    for each element in m
        result.set(index = (index + 1), element)

    end for
    return result
end function

function plus_rArr_CollectionAnyN_Arr_k_(m as Object, elements as Object) as Object
    result = copyOf_rArr_I_Arr_k_(m, m.size + elements.size)
    index = m.size
    for each element in elements
        result.set(index = (index + 1), element)

    end for
    return result
end function

function plus_rArr_AnyN_Arr_k_(m as Object, element as Dynamic) as Object
    result = copyOf_rArr_I_Arr_k_(m, m.size + 1)
    result.set(m.size, element)
    return result
end function

function plus_rArr_Arr_Arr_k_(m as Object, elements as Object) as Object
    result = copyOf_rArr_I_Arr_k_(m, m.size + elements.size)
    index = m.size
    indexedObject = elements
    inductionVariable = 0
    last = indexedObject.size
    while less_I_I_Z_k_(inductionVariable, last)
        element = indexedObject.get(inductionVariable)
        inductionVariable = (inductionVariable + 1)

        result.set(index = (index + 1), element)

    end while

    return result
end function

sub fill_rArr_AnyN_I_I_k_(m as Object, element as Dynamic, fromIndex = 0, toIndex = m.size)
    if (fromIndex < 0) or (toIndex > m.size) then
        throw IndexOutOfBoundsException_create_StrN_IndexOutOfBoundsException_k_((((("fromIndex: " + fromIndex) + ", toIndex: ") + toIndex) + ", size: ") + m.size)
    end if
    if fromIndex > toIndex then
        throw IllegalArgumentException_create_StrN_IllegalArgumentException_k_((("fromIndex: " + fromIndex) + " > toIndex: ") + toIndex)
    end if
    progression = until_rI_I_IntRange_k_(fromIndex, toIndex)
    inductionVariable = progression.first
    last = progression.last
    if lessOrEqual_I_I_Z_k_(inductionVariable, last) then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        m.set(i, element)


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            m.set(i, element)

        end while

    end if

end sub

function copyOf_rArr_Arr_k_(m as Object) as Object
    return copyOf_rArr_I_Arr_k_(m, m.size)
end function

function copyOf_rArr_I_Arr_k_(m as Object, newSize as Integer) as Object
    if newSize < 0 then
        throw IllegalArgumentException_create_StrN_IllegalArgumentException_k_("Invalid new array size: " + newSize)
    end if
    result = arrayOfNulls_I_Arr_k_(newSize)
    __when_tmp0 = invalid
    if newSize < m.size then
        __when_tmp0 = newSize
    else if true then
        __when_tmp0 = m.size
    end if
    copySize = __when_tmp0

    progression = until_rI_I_IntRange_k_(0, copySize)
    inductionVariable = progression.first
    last = progression.last
    if lessOrEqual_I_I_Z_k_(inductionVariable, last) then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        result.set(i, m[i])


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            result.set(i, m[i])

        end while

    end if

    return result
end function

function copyOfRange_rArr_I_I_Arr_k_(m as Object, fromIndex as Integer, toIndex as Integer) as Object
    if (fromIndex < 0) or (toIndex > m.size) then
        throw IndexOutOfBoundsException_create_StrN_IndexOutOfBoundsException_k_((((("fromIndex: " + fromIndex) + ", toIndex: ") + toIndex) + ", size: ") + m.size)
    end if
    if fromIndex > toIndex then
        throw IllegalArgumentException_create_StrN_IllegalArgumentException_k_((("fromIndex: " + fromIndex) + " > toIndex: ") + toIndex)
    end if
    result = arrayOfNulls_I_Arr_k_(toIndex - fromIndex)
    resultIndex = 0
    progression = until_rI_I_IntRange_k_(fromIndex, toIndex)
    inductionVariable = progression.first
    last = progression.last
    if lessOrEqual_I_I_Z_k_(inductionVariable, last) then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        result.set(resultIndex = (resultIndex + 1), m[i])


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            result.set(resultIndex = (resultIndex + 1), m[i])

        end while

    end if

    return result
end function

sub reverse_rArr_k_(m as Object)
    midPoint = m.size / 2
    i = 0
    while i < midPoint
        tmp = m[i]
        m.set(i, m[(m.size - i) - 1])
        m.set((m.size - i) - 1, tmp)
        i = (i + 1)
    end while
end sub

sub reverse_rArr_I_I_k_(m as Object, fromIndex as Integer, toIndex as Integer)
    if (fromIndex < 0) or (toIndex > m.size) then
        throw IndexOutOfBoundsException_create_StrN_IndexOutOfBoundsException_k_((((("fromIndex: " + fromIndex) + ", toIndex: ") + toIndex) + ", size: ") + m.size)
    end if
    if fromIndex >= toIndex then
        return
    end if
    midPoint = (fromIndex + toIndex) / 2
    i = fromIndex
    while i < midPoint
        tmp = m[i]
        m.set(i, m[((toIndex - i) + fromIndex) - 1])
        m.set(((toIndex - i) + fromIndex) - 1, tmp)
        i = (i + 1)
    end while
end sub

function toList_rArr_ListAnyN_k_(m as Object) as Object
    result = ArrayList_create_I_ArrayListAnyN_k_(m.size)
    indexedObject = m
    inductionVariable = 0
    last = indexedObject.size
    while less_I_I_Z_k_(inductionVariable, last)
        element = indexedObject.get(inductionVariable)
        inductionVariable = (inductionVariable + 1)

        result.add(element)

    end while

    return result
end function

function toMutableList_rArr_MutableListAnyN_k_(m as Object) as Object
    result = ArrayList_create_I_ArrayListAnyN_k_(m.size)
    indexedObject = m
    inductionVariable = 0
    last = indexedObject.size
    while less_I_I_Z_k_(inductionVariable, last)
        element = indexedObject.get(inductionVariable)
        inductionVariable = (inductionVariable + 1)

        result.add(element)

    end while

    return result
end function

function toSet_rArr_SetAnyN_k_(m as Object) as Object
    result = HashSet_create_HashSetAnyN_k_()
    indexedObject = m
    inductionVariable = 0
    last = indexedObject.size
    while less_I_I_Z_k_(inductionVariable, last)
        element = indexedObject.get(inductionVariable)
        inductionVariable = (inductionVariable + 1)

        result.add(element)

    end while

    return result
end function

function toMutableSet_rArr_MutableSetAnyN_k_(m as Object) as Object
    result = HashSet_create_HashSetAnyN_k_()
    indexedObject = m
    inductionVariable = 0
    last = indexedObject.size
    while less_I_I_Z_k_(inductionVariable, last)
        element = indexedObject.get(inductionVariable)
        inductionVariable = (inductionVariable + 1)

        result.add(element)

    end while

    return result
end function

function isNotEmpty_rArr_Z_k_(m as Object) as Boolean
    return isEmpty_rArr_Z_k_(m).not()
end function

function isEmpty_rArr_Z_k_(m as Object) as Boolean
    return m.size = 0
end function

function orEmpty_rArrN_Arr_k_(m as Dynamic) as Object
    tmp0_elvis_lhs = m
    __when_tmp1 = invalid
    if tmp0_elvis_lhs = invalid then
        __when_tmp1 = emptyArray_Arr_k_()
    else if true then
        __when_tmp1 = tmp0_elvis_lhs
    end if
    return __when_tmp1

end function

function emptyArray_Arr_k_() as Object
    return arrayOfNulls_I_Arr_k_(0)
end function
