function ArrayList_create_ArrayListAnyN_k_() as Object
    this = {}
    this.__type = "ArrayList"
    this.__proto = ["ArrayList"]
    this.isEmpty_Z_k_ = ArrayList_isEmpty_Z_k_
    this.contains_AnyN_Z_k_ = ArrayList_contains_AnyN_Z_k_
    this.containsAll_CollectionAnyN_Z_k_ = ArrayList_containsAll_CollectionAnyN_Z_k_
    this.get_I_AnyN_k_ = ArrayList_get_I_AnyN_k_
    this.set_I_AnyN_AnyN_k_ = ArrayList_set_I_AnyN_AnyN_k_
    this.add_AnyN_Z_k_ = ArrayList_add_AnyN_Z_k_
    this.add_I_AnyN_k_ = ArrayList_add_I_AnyN_k_
    this.remove_AnyN_Z_k_ = ArrayList_remove_AnyN_Z_k_
    this.removeAt_I_AnyN_k_ = ArrayList_removeAt_I_AnyN_k_
    this.addAll_CollectionAnyN_Z_k_ = ArrayList_addAll_CollectionAnyN_Z_k_
    this.addAll_I_CollectionAnyN_Z_k_ = ArrayList_addAll_I_CollectionAnyN_Z_k_
    this.removeAll_CollectionAnyN_Z_k_ = ArrayList_removeAll_CollectionAnyN_Z_k_
    this.retainAll_CollectionAnyN_Z_k_ = ArrayList_retainAll_CollectionAnyN_Z_k_
    this.clear = ArrayList_clear
    this.indexOf_AnyN_I_k_ = ArrayList_indexOf_AnyN_I_k_
    this.lastIndexOf_AnyN_I_k_ = ArrayList_lastIndexOf_AnyN_I_k_
    this.iterator_MutableIteratorAnyN_k_ = ArrayList_iterator_MutableIteratorAnyN_k_
    this.listIterator_MutableListIteratorAnyN_k_ = ArrayList_listIterator_MutableListIteratorAnyN_k_
    this.listIterator_I_MutableListIteratorAnyN_k_ = ArrayList_listIterator_I_MutableListIteratorAnyN_k_
    this.subList_I_I_MutableListAnyN_k_ = ArrayList_subList_I_I_MutableListAnyN_k_
    this.equals_AnyN_Z_k_ = ArrayList_equals_AnyN_Z_k_
    this.hashCode_I_k_ = ArrayList_hashCode_I_k_
    this.toString_Str_k_ = ArrayList_toString_Str_k_
    this.brsArrayInsertAt_Dynamic_I_AnyN_k_ = ArrayList_brsArrayInsertAt_Dynamic_I_AnyN_k_
    this.get_array = ArrayList_get_array_Dynamic_k_
    this.set_array = ArrayList_set_array_Dynamic_k_
    this.get_size = ArrayList_get_size_I_k_
    m.array = CreateObject("roArray", 0, true)
    return this
end function

function ArrayList_create_I_ArrayListAnyN_k_(initialCapacity as Integer) as Object
    this = {}
    this.__type = "ArrayList"
    this.__proto = ["ArrayList"]
    this.isEmpty_Z_k_ = ArrayList_isEmpty_Z_k_
    this.contains_AnyN_Z_k_ = ArrayList_contains_AnyN_Z_k_
    this.containsAll_CollectionAnyN_Z_k_ = ArrayList_containsAll_CollectionAnyN_Z_k_
    this.get_I_AnyN_k_ = ArrayList_get_I_AnyN_k_
    this.set_I_AnyN_AnyN_k_ = ArrayList_set_I_AnyN_AnyN_k_
    this.add_AnyN_Z_k_ = ArrayList_add_AnyN_Z_k_
    this.add_I_AnyN_k_ = ArrayList_add_I_AnyN_k_
    this.remove_AnyN_Z_k_ = ArrayList_remove_AnyN_Z_k_
    this.removeAt_I_AnyN_k_ = ArrayList_removeAt_I_AnyN_k_
    this.addAll_CollectionAnyN_Z_k_ = ArrayList_addAll_CollectionAnyN_Z_k_
    this.addAll_I_CollectionAnyN_Z_k_ = ArrayList_addAll_I_CollectionAnyN_Z_k_
    this.removeAll_CollectionAnyN_Z_k_ = ArrayList_removeAll_CollectionAnyN_Z_k_
    this.retainAll_CollectionAnyN_Z_k_ = ArrayList_retainAll_CollectionAnyN_Z_k_
    this.clear = ArrayList_clear
    this.indexOf_AnyN_I_k_ = ArrayList_indexOf_AnyN_I_k_
    this.lastIndexOf_AnyN_I_k_ = ArrayList_lastIndexOf_AnyN_I_k_
    this.iterator_MutableIteratorAnyN_k_ = ArrayList_iterator_MutableIteratorAnyN_k_
    this.listIterator_MutableListIteratorAnyN_k_ = ArrayList_listIterator_MutableListIteratorAnyN_k_
    this.listIterator_I_MutableListIteratorAnyN_k_ = ArrayList_listIterator_I_MutableListIteratorAnyN_k_
    this.subList_I_I_MutableListAnyN_k_ = ArrayList_subList_I_I_MutableListAnyN_k_
    this.equals_AnyN_Z_k_ = ArrayList_equals_AnyN_Z_k_
    this.hashCode_I_k_ = ArrayList_hashCode_I_k_
    this.toString_Str_k_ = ArrayList_toString_Str_k_
    this.brsArrayInsertAt_Dynamic_I_AnyN_k_ = ArrayList_brsArrayInsertAt_Dynamic_I_AnyN_k_
    this.get_array = ArrayList_get_array_Dynamic_k_
    this.set_array = ArrayList_set_array_Dynamic_k_
    this.get_size = ArrayList_get_size_I_k_
    require_Z_Function0Any_k_(initialCapacity >= 0, {initialCapacity: initialCapacity, invoke: function() as Object
        return "Negative initial capacity: " + m.initialCapacity
    end function})
    m.array = CreateObject("roArray", initialCapacity, true)
    return this
end function

function ArrayList_create_CollectionAnyN_ArrayListAnyN_k_(elements as Object) as Object
    this = {}
    this.__type = "ArrayList"
    this.__proto = ["ArrayList"]
    this.isEmpty_Z_k_ = ArrayList_isEmpty_Z_k_
    this.contains_AnyN_Z_k_ = ArrayList_contains_AnyN_Z_k_
    this.containsAll_CollectionAnyN_Z_k_ = ArrayList_containsAll_CollectionAnyN_Z_k_
    this.get_I_AnyN_k_ = ArrayList_get_I_AnyN_k_
    this.set_I_AnyN_AnyN_k_ = ArrayList_set_I_AnyN_AnyN_k_
    this.add_AnyN_Z_k_ = ArrayList_add_AnyN_Z_k_
    this.add_I_AnyN_k_ = ArrayList_add_I_AnyN_k_
    this.remove_AnyN_Z_k_ = ArrayList_remove_AnyN_Z_k_
    this.removeAt_I_AnyN_k_ = ArrayList_removeAt_I_AnyN_k_
    this.addAll_CollectionAnyN_Z_k_ = ArrayList_addAll_CollectionAnyN_Z_k_
    this.addAll_I_CollectionAnyN_Z_k_ = ArrayList_addAll_I_CollectionAnyN_Z_k_
    this.removeAll_CollectionAnyN_Z_k_ = ArrayList_removeAll_CollectionAnyN_Z_k_
    this.retainAll_CollectionAnyN_Z_k_ = ArrayList_retainAll_CollectionAnyN_Z_k_
    this.clear = ArrayList_clear
    this.indexOf_AnyN_I_k_ = ArrayList_indexOf_AnyN_I_k_
    this.lastIndexOf_AnyN_I_k_ = ArrayList_lastIndexOf_AnyN_I_k_
    this.iterator_MutableIteratorAnyN_k_ = ArrayList_iterator_MutableIteratorAnyN_k_
    this.listIterator_MutableListIteratorAnyN_k_ = ArrayList_listIterator_MutableListIteratorAnyN_k_
    this.listIterator_I_MutableListIteratorAnyN_k_ = ArrayList_listIterator_I_MutableListIteratorAnyN_k_
    this.subList_I_I_MutableListAnyN_k_ = ArrayList_subList_I_I_MutableListAnyN_k_
    this.equals_AnyN_Z_k_ = ArrayList_equals_AnyN_Z_k_
    this.hashCode_I_k_ = ArrayList_hashCode_I_k_
    this.toString_Str_k_ = ArrayList_toString_Str_k_
    this.brsArrayInsertAt_Dynamic_I_AnyN_k_ = ArrayList_brsArrayInsertAt_Dynamic_I_AnyN_k_
    this.get_array = ArrayList_get_array_Dynamic_k_
    this.set_array = ArrayList_set_array_Dynamic_k_
    this.get_size = ArrayList_get_size_I_k_
    m.array = CreateObject("roArray", 0, true)
    m.addAll(elements)
    return this
end function

function ArrayList_isEmpty_Z_k_() as Boolean
    return m.size = 0
end function

function ArrayList_contains_AnyN_Z_k_(element as Dynamic) as Boolean
    return m.indexOf(element) >= 0
end function

function ArrayList_containsAll_CollectionAnyN_Z_k_(elements as Object) as Boolean
    for each element in elements
        if m.contains(element).not() then
            return false
        end if
    end for
    return true
end function

function ArrayList_get_I_AnyN_k_(index as Integer) as Dynamic
    checkElementIndex_I_I_k_(index, m.size)
    return m.array[index]
end function

function ArrayList_set_I_AnyN_AnyN_k_(index as Integer, element as Dynamic) as Dynamic
    checkElementIndex_I_I_k_(index, m.size)
    oldValue = m.array[index]
    m.array[index] = element
    return oldValue
end function

function ArrayList_add_AnyN_Z_k_(element as Dynamic) as Boolean
    m.array.Push(element)
    return true
end function

sub ArrayList_add_I_AnyN_k_(index as Integer, element as Dynamic)
    checkPositionIndex_I_I_k_(index, m.size)
    if index = m.size then
        m.array.Push(element)
    else if true then
        m.brsArrayInsertAt(m.array, index, element)
    end if
end sub

function ArrayList_remove_AnyN_Z_k_(element as Dynamic) as Boolean
    index = m.indexOf(element)
    if index < 0 then
        return false
    end if
    m.removeAt(index)
    return true
end function

function ArrayList_removeAt_I_AnyN_k_(index as Integer) as Dynamic
    checkElementIndex_I_I_k_(index, m.size)
    oldValue = m.array[index]
    m.array.Delete(index)
    return oldValue
end function

function ArrayList_addAll_CollectionAnyN_Z_k_(elements as Object) as Boolean
    if elements.isEmpty() then
        return false
    end if
    for each element in elements
        m.array.Push(element)

    end for
    return true
end function

function ArrayList_addAll_I_CollectionAnyN_Z_k_(index as Integer, elements as Object) as Boolean
    checkPositionIndex_I_I_k_(index, m.size)
    if elements.isEmpty() then
        return false
    end if
    currentIndex = index
    for each element in elements
        m.add(currentIndex, element)
        currentIndex = (unary + 1)

    end for
    return true
end function

function ArrayList_removeAll_CollectionAnyN_Z_k_(elements as Object) as Boolean
    modified = false
    for each element in elements
        while m.remove(element)
            modified = true
        end while

    end for
    return modified
end function

function ArrayList_retainAll_CollectionAnyN_Z_k_(elements as Object) as Boolean
    modified = false
    iter = m.iterator()
    while iter.hasNext()
        if elements.contains(iter.next()).not() then
            iter.remove()
            modified = true
        end if
    end while
    return modified
end function

sub ArrayList_clear()
    m.array.Clear()
end sub

function ArrayList_indexOf_AnyN_I_k_(element as Dynamic) as Integer
    progression = until_rI_I_IntRange_k_(0, m.size)
    inductionVariable = progression.first
    last = progression.last
    if lessOrEqual_I_I_Z_k_(inductionVariable, last) then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if m.array[i] = element then
            return i
        end if


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if m.array[i] = element then
                return i
            end if

        end while

    end if

    return -1
end function

function ArrayList_lastIndexOf_AnyN_I_k_(element as Dynamic) as Integer
    i = m.size - 1
    while i >= 0
        if m.array[i] = element then
            return i
        end if
        i = (i - 1)
    end while
    return -1
end function

function ArrayList_iterator_MutableIteratorAnyN_k_() as Object
    return ArrayListIterator_create_ArrayListAnyN_I_ArrayListIteratorAnyN_k_(m, 0)
end function

function ArrayList_listIterator_MutableListIteratorAnyN_k_() as Object
    return ArrayListIterator_create_ArrayListAnyN_I_ArrayListIteratorAnyN_k_(m, 0)
end function

function ArrayList_listIterator_I_MutableListIteratorAnyN_k_(index as Integer) as Object
    checkPositionIndex_I_I_k_(index, m.size)
    return ArrayListIterator_create_ArrayListAnyN_I_ArrayListIteratorAnyN_k_(m, index)
end function

function ArrayList_subList_I_I_MutableListAnyN_k_(fromIndex as Integer, toIndex as Integer) as Object
    checkRangeIndexes_I_I_I_k_(fromIndex, toIndex, m.size)
    return SubList_create_ArrayListAnyN_I_I_SubListAnyN_k_(m, fromIndex, toIndex)
end function

function ArrayList_equals_AnyN_Z_k_(other as Dynamic) as Boolean
    if EQEQEQ_AnyN_AnyN_Z_k_(other, m) then
        return true
    end if
    if not __kotlin_isInstanceOf(other, "List") then
        return false
    end if
    if other.size <> m.size then
        return false
    end if
    i = 0
    for each element in other
        if m.get(i) <> element then
            return false
        end if
        i = (unary + 1)

    end for
    return true
end function

function ArrayList_hashCode_I_k_() as Integer
    hashCode = 1
    progression = until_rI_I_IntRange_k_(0, m.size)
    inductionVariable = progression.first
    last = progression.last
    if lessOrEqual_I_I_Z_k_(inductionVariable, last) then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        element = m.get(i)
        tmp0_safe_receiver = element
        __when_tmp0 = invalid
        if tmp0_safe_receiver = invalid then
            __when_tmp0 = invalid
        else if true then
            __when_tmp0 = tmp0_safe_receiver.hashCode()
        end if
        tmp1_elvis_lhs = __when_tmp0
        __when_tmp1 = invalid
        if tmp1_elvis_lhs = invalid then
            __when_tmp1 = 0
        else if true then
            __when_tmp1 = tmp1_elvis_lhs
        end if
        hashCode = ((31 * hashCode) + __when_tmp1)


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            element = m.get(i)
            tmp0_safe_receiver = element
            __when_tmp0 = invalid
            if tmp0_safe_receiver = invalid then
                __when_tmp0 = invalid
            else if true then
                __when_tmp0 = tmp0_safe_receiver.hashCode()
            end if
            tmp1_elvis_lhs = __when_tmp0
            __when_tmp1 = invalid
            if tmp1_elvis_lhs = invalid then
                __when_tmp1 = 0
            else if true then
                __when_tmp1 = tmp1_elvis_lhs
            end if
            hashCode = ((31 * hashCode) + __when_tmp1)

        end while

    end if

    return hashCode
end function

function ArrayList_toString_Str_k_() as String
    if m.isEmpty() then
        return "[]"
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
        element = m.get(i)
        if EQEQEQ_AnyN_AnyN_Z_k_(element, m) then
            sb.append("(this Collection)")
        else if true then
            sb.append(element.toString())
        end if


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if i > 0 then
                sb.append(", ")
            end if
            element = m.get(i)
            if EQEQEQ_AnyN_AnyN_Z_k_(element, m) then
                sb.append("(this Collection)")
            else if true then
                sb.append(element.toString())
            end if

        end while

    end if

    sb.append("]")
    return sb.toString()
end function

sub ArrayList_brsArrayInsertAt_Dynamic_I_AnyN_k_(arr as Object, index as Integer, value as Dynamic)
    arr.Push(value)
    i = m.size - 1
    while i > index
        arr[i] = arr[i - 1]
        i = (i - 1)
    end while
    arr[index] = value
end sub

function ArrayList_get_array_Dynamic_k_() as Object
    return m.array
end function

sub ArrayList_set_array_Dynamic_k_(value as Object)
    m.array = value
end sub

function ArrayList_get_size_I_k_() as Integer
    return m.array.Count()
end function

function ArrayListIterator_create_ArrayListAnyN_I_ArrayListIteratorAnyN_k_(list as Object, index as Integer) as Object
    this = {}
    this.__type = "ArrayListIterator"
    this.__proto = ["ArrayListIterator"]
    this.list = list
    this.index = index
    this.lastReturned = -1
    this.hasNext_Z_k_ = ArrayListIterator_hasNext_Z_k_
    this.next_AnyN_k_ = ArrayListIterator_next_AnyN_k_
    this.hasPrevious_Z_k_ = ArrayListIterator_hasPrevious_Z_k_
    this.previous_AnyN_k_ = ArrayListIterator_previous_AnyN_k_
    this.nextIndex_I_k_ = ArrayListIterator_nextIndex_I_k_
    this.previousIndex_I_k_ = ArrayListIterator_previousIndex_I_k_
    this.remove = ArrayListIterator_remove
    this.set_AnyN_k_ = ArrayListIterator_set_AnyN_k_
    this.add_AnyN_k_ = ArrayListIterator_add_AnyN_k_
    this.get_list = ArrayListIterator_get_list_ArrayListAnyN_k_
    this.get_index = ArrayListIterator_get_index_I_k_
    this.set_index = ArrayListIterator_set_index_I_k_
    this.get_lastReturned = ArrayListIterator_get_lastReturned_I_k_
    this.set_lastReturned = ArrayListIterator_set_lastReturned_I_k_
    return this
end function

function ArrayListIterator_hasNext_Z_k_() as Boolean
    return m.index < m.list.size
end function

function ArrayListIterator_next_AnyN_k_() as Dynamic
    if m.hasNext().not() then
        throw NoSuchElementException_create_NoSuchElementException_k_()
    end if
    m.lastReturned = m.index
    return m.list[m.index = (m.index + 1)]
end function

function ArrayListIterator_hasPrevious_Z_k_() as Boolean
    return m.index > 0
end function

function ArrayListIterator_previous_AnyN_k_() as Dynamic
    if m.hasPrevious().not() then
        throw NoSuchElementException_create_NoSuchElementException_k_()
    end if
    m.lastReturned = (m.index = (m.index - 1))
    return m.list[m.index]
end function

function ArrayListIterator_nextIndex_I_k_() as Integer
    return m.index
end function

function ArrayListIterator_previousIndex_I_k_() as Integer
    return m.index - 1
end function

sub ArrayListIterator_remove()
    check_Z_Function0Any_k_(m.lastReturned >= 0, {invoke: function() as Object
        return "Call next() or previous() before remove()"
    end function})
    m.list.removeAt(m.lastReturned)
    if m.lastReturned < m.index then
        m.index = (unary - 1)
    end if
    m.lastReturned = -1
end sub

sub ArrayListIterator_set_AnyN_k_(element as Dynamic)
    check_Z_Function0Any_k_(m.lastReturned >= 0, {invoke: function() as Object
        return "Call next() or previous() before set()"
    end function})
    m.list.set(m.lastReturned, element)
end sub

sub ArrayListIterator_add_AnyN_k_(element as Dynamic)
    m.list.add(m.index = (m.index + 1), element)
    m.lastReturned = -1
end sub

function ArrayListIterator_get_list_ArrayListAnyN_k_() as Object
    return m.list
end function

function ArrayListIterator_get_index_I_k_() as Integer
    return m.index
end function

sub ArrayListIterator_set_index_I_k_(value as Integer)
    m.index = value
end sub

function ArrayListIterator_get_lastReturned_I_k_() as Integer
    return m.lastReturned
end function

sub ArrayListIterator_set_lastReturned_I_k_(value as Integer)
    m.lastReturned = value
end sub

function SubList_create_ArrayListAnyN_I_I_SubListAnyN_k_(parent as Object, fromIndex as Integer, toIndex as Integer) as Object
    this = {}
    this.__type = "SubList"
    this.__proto = ["SubList"]
    this.parent = parent
    this.fromIndex = fromIndex
    this.toIndex = toIndex
    this.isEmpty_Z_k_ = SubList_isEmpty_Z_k_
    this.contains_AnyN_Z_k_ = SubList_contains_AnyN_Z_k_
    this.containsAll_CollectionAnyN_Z_k_ = SubList_containsAll_CollectionAnyN_Z_k_
    this.get_I_AnyN_k_ = SubList_get_I_AnyN_k_
    this.set_I_AnyN_AnyN_k_ = SubList_set_I_AnyN_AnyN_k_
    this.add_AnyN_Z_k_ = SubList_add_AnyN_Z_k_
    this.add_I_AnyN_k_ = SubList_add_I_AnyN_k_
    this.remove_AnyN_Z_k_ = SubList_remove_AnyN_Z_k_
    this.removeAt_I_AnyN_k_ = SubList_removeAt_I_AnyN_k_
    this.addAll_CollectionAnyN_Z_k_ = SubList_addAll_CollectionAnyN_Z_k_
    this.addAll_I_CollectionAnyN_Z_k_ = SubList_addAll_I_CollectionAnyN_Z_k_
    this.removeAll_CollectionAnyN_Z_k_ = SubList_removeAll_CollectionAnyN_Z_k_
    this.retainAll_CollectionAnyN_Z_k_ = SubList_retainAll_CollectionAnyN_Z_k_
    this.clear = SubList_clear
    this.indexOf_AnyN_I_k_ = SubList_indexOf_AnyN_I_k_
    this.lastIndexOf_AnyN_I_k_ = SubList_lastIndexOf_AnyN_I_k_
    this.iterator_MutableIteratorAnyN_k_ = SubList_iterator_MutableIteratorAnyN_k_
    this.listIterator_MutableListIteratorAnyN_k_ = SubList_listIterator_MutableListIteratorAnyN_k_
    this.listIterator_I_MutableListIteratorAnyN_k_ = SubList_listIterator_I_MutableListIteratorAnyN_k_
    this.subList_I_I_MutableListAnyN_k_ = SubList_subList_I_I_MutableListAnyN_k_
    this.get_parent = SubList_get_parent_ArrayListAnyN_k_
    this.get_fromIndex = SubList_get_fromIndex_I_k_
    this.get_toIndex = SubList_get_toIndex_I_k_
    this.set_toIndex = SubList_set_toIndex_I_k_
    this.get_size = SubList_get_size_I_k_
    return this
end function

function SubList_isEmpty_Z_k_() as Boolean
    return m.size = 0
end function

function SubList_contains_AnyN_Z_k_(element as Dynamic) as Boolean
    return m.indexOf(element) >= 0
end function

function SubList_containsAll_CollectionAnyN_Z_k_(elements as Object) as Boolean
    for each element in elements
        if m.contains(element).not() then
            return false
        end if
    end for
    return true
end function

function SubList_get_I_AnyN_k_(index as Integer) as Dynamic
    checkElementIndex_I_I_k_(index, m.size)
    return m.parent[m.fromIndex + index]
end function

function SubList_set_I_AnyN_AnyN_k_(index as Integer, element as Dynamic) as Dynamic
    checkElementIndex_I_I_k_(index, m.size)
    return m.parent.set(m.fromIndex + index, element)
end function

function SubList_add_AnyN_Z_k_(element as Dynamic) as Boolean
    m.parent.add(m.toIndex, element)
    m.toIndex = (m.toIndex + 1)
    return true
end function

sub SubList_add_I_AnyN_k_(index as Integer, element as Dynamic)
    checkPositionIndex_I_I_k_(index, m.size)
    m.parent.add(m.fromIndex + index, element)
    m.toIndex = (m.toIndex + 1)
end sub

function SubList_remove_AnyN_Z_k_(element as Dynamic) as Boolean
    index = m.indexOf(element)
    if index < 0 then
        return false
    end if
    m.removeAt(index)
    return true
end function

function SubList_removeAt_I_AnyN_k_(index as Integer) as Dynamic
    checkElementIndex_I_I_k_(index, m.size)
    result = m.parent.removeAt(m.fromIndex + index)
    m.toIndex = (m.toIndex - 1)
    return result
end function

function SubList_addAll_CollectionAnyN_Z_k_(elements as Object) as Boolean
    return m.addAll(m.size, elements)
end function

function SubList_addAll_I_CollectionAnyN_Z_k_(index as Integer, elements as Object) as Boolean
    checkPositionIndex_I_I_k_(index, m.size)
    if elements.isEmpty() then
        return false
    end if
    m.parent.addAll(m.fromIndex + index, elements)
    m.toIndex = (m.toIndex + elements.size)
    return true
end function

function SubList_removeAll_CollectionAnyN_Z_k_(elements as Object) as Boolean
    modified = false
    iter = m.iterator()
    while iter.hasNext()
        if elements.contains(iter.next()) then
            iter.remove()
            modified = true
        end if
    end while
    return modified
end function

function SubList_retainAll_CollectionAnyN_Z_k_(elements as Object) as Boolean
    modified = false
    iter = m.iterator()
    while iter.hasNext()
        if elements.contains(iter.next()).not() then
            iter.remove()
            modified = true
        end if
    end while
    return modified
end function

sub SubList_clear()
    while m.size > 0
        m.removeAt(0)
    end while
end sub

function SubList_indexOf_AnyN_I_k_(element as Dynamic) as Integer
    progression = until_rI_I_IntRange_k_(0, m.size)
    inductionVariable = progression.first
    last = progression.last
    if lessOrEqual_I_I_Z_k_(inductionVariable, last) then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if m.get(i) = element then
            return i
        end if


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if m.get(i) = element then
                return i
            end if

        end while

    end if

    return -1
end function

function SubList_lastIndexOf_AnyN_I_k_(element as Dynamic) as Integer
    i = m.size - 1
    while i >= 0
        if m.get(i) = element then
            return i
        end if
        i = (i - 1)
    end while
    return -1
end function

function SubList_iterator_MutableIteratorAnyN_k_() as Object
    return m.listIterator()
end function

function SubList_listIterator_MutableListIteratorAnyN_k_() as Object
    return SubListIterator_create_SubListAnyN_I_SubListIteratorAnyN_k_(m, 0)
end function

function SubList_listIterator_I_MutableListIteratorAnyN_k_(index as Integer) as Object
    checkPositionIndex_I_I_k_(index, m.size)
    return SubListIterator_create_SubListAnyN_I_SubListIteratorAnyN_k_(m, index)
end function

function SubList_subList_I_I_MutableListAnyN_k_(fromIndex as Integer, toIndex as Integer) as Object
    checkRangeIndexes_I_I_I_k_(fromIndex, toIndex, m.size)
    return SubList_create_ArrayListAnyN_I_I_SubListAnyN_k_(m.parent, m.fromIndex + fromIndex, m.fromIndex + toIndex)
end function

function SubList_get_parent_ArrayListAnyN_k_() as Object
    return m.parent
end function

function SubList_get_fromIndex_I_k_() as Integer
    return m.fromIndex
end function

function SubList_get_toIndex_I_k_() as Integer
    return m.toIndex
end function

sub SubList_set_toIndex_I_k_(value as Integer)
    m.toIndex = value
end sub

function SubList_get_size_I_k_() as Integer
    return m.toIndex - m.fromIndex
end function

function SubListIterator_create_SubListAnyN_I_SubListIteratorAnyN_k_(list as Object, index as Integer) as Object
    this = {}
    this.__type = "SubListIterator"
    this.__proto = ["SubListIterator"]
    this.list = list
    this.index = index
    this.lastReturned = -1
    this.hasNext_Z_k_ = SubListIterator_hasNext_Z_k_
    this.next_AnyN_k_ = SubListIterator_next_AnyN_k_
    this.hasPrevious_Z_k_ = SubListIterator_hasPrevious_Z_k_
    this.previous_AnyN_k_ = SubListIterator_previous_AnyN_k_
    this.nextIndex_I_k_ = SubListIterator_nextIndex_I_k_
    this.previousIndex_I_k_ = SubListIterator_previousIndex_I_k_
    this.remove = SubListIterator_remove
    this.set_AnyN_k_ = SubListIterator_set_AnyN_k_
    this.add_AnyN_k_ = SubListIterator_add_AnyN_k_
    this.get_list = SubListIterator_get_list_SubListAnyN_k_
    this.get_index = SubListIterator_get_index_I_k_
    this.set_index = SubListIterator_set_index_I_k_
    this.get_lastReturned = SubListIterator_get_lastReturned_I_k_
    this.set_lastReturned = SubListIterator_set_lastReturned_I_k_
    return this
end function

function SubListIterator_hasNext_Z_k_() as Boolean
    return m.index < m.list.size
end function

function SubListIterator_next_AnyN_k_() as Dynamic
    if m.hasNext().not() then
        throw NoSuchElementException_create_NoSuchElementException_k_()
    end if
    m.lastReturned = m.index
    return m.list[m.index = (m.index + 1)]
end function

function SubListIterator_hasPrevious_Z_k_() as Boolean
    return m.index > 0
end function

function SubListIterator_previous_AnyN_k_() as Dynamic
    if m.hasPrevious().not() then
        throw NoSuchElementException_create_NoSuchElementException_k_()
    end if
    m.lastReturned = (m.index = (m.index - 1))
    return m.list[m.index]
end function

function SubListIterator_nextIndex_I_k_() as Integer
    return m.index
end function

function SubListIterator_previousIndex_I_k_() as Integer
    return m.index - 1
end function

sub SubListIterator_remove()
    check_Z_Function0Any_k_(m.lastReturned >= 0, {invoke: function() as Object
        return "Call next() or previous() before remove()"
    end function})
    m.list.removeAt(m.lastReturned)
    if m.lastReturned < m.index then
        m.index = (unary - 1)
    end if
    m.lastReturned = -1
end sub

sub SubListIterator_set_AnyN_k_(element as Dynamic)
    check_Z_Function0Any_k_(m.lastReturned >= 0, {invoke: function() as Object
        return "Call next() or previous() before set()"
    end function})
    m.list.set(m.lastReturned, element)
end sub

sub SubListIterator_add_AnyN_k_(element as Dynamic)
    m.list.add(m.index = (m.index + 1), element)
    m.lastReturned = -1
end sub

function SubListIterator_get_list_SubListAnyN_k_() as Object
    return m.list
end function

function SubListIterator_get_index_I_k_() as Integer
    return m.index
end function

sub SubListIterator_set_index_I_k_(value as Integer)
    m.index = value
end sub

function SubListIterator_get_lastReturned_I_k_() as Integer
    return m.lastReturned
end function

sub SubListIterator_set_lastReturned_I_k_(value as Integer)
    m.lastReturned = value
end sub

function arrayListOf_ArrayListAnyN_k_() as Object
    return ArrayList_create_ArrayListAnyN_k_()
end function

function arrayListOf_Arr_ArrayListAnyN_k_(elements as Object) as Object
    list = ArrayList_create_I_ArrayListAnyN_k_(elements.size)
    indexedObject = elements
    inductionVariable = 0
    last = indexedObject.size
    while less_I_I_Z_k_(inductionVariable, last)
        element = indexedObject.get(inductionVariable)
        inductionVariable = (inductionVariable + 1)

        list.add(element)

    end while

    return list
end function

function mutableListOf_MutableListAnyN_k_() as Object
    return ArrayList_create_ArrayListAnyN_k_()
end function

function mutableListOf_Arr_MutableListAnyN_k_(elements as Object) as Object
    return arrayListOf_Arr_ArrayListAnyN_k_([elements])
end function

function listOf_ListAnyN_k_() as Object
    return emptyList_ListAnyN_k_()
end function

function listOf_Arr_ListAnyN_k_(elements as Object) as Object
    __when_tmp2 = invalid
    if elements.size = 0 then
        __when_tmp2 = emptyList_ListAnyN_k_()
    else if true then
        __when_tmp2 = arrayListOf_Arr_ArrayListAnyN_k_([elements])
    end if
    return __when_tmp2

end function

function emptyList_ListAnyN_k_() as Object
    return EmptyList_getInstance()
end function

function EmptyList_create_EmptyList_k_() as Object
    this = {}
    this.__type = "EmptyList"
    this.__proto = ["EmptyList"]
    this.isEmpty_Z_k_ = EmptyList_isEmpty_Z_k_
    this.contains_Nothing_Z_k_ = EmptyList_contains_Nothing_Z_k_
    this.containsAll_CollectionNothing_Z_k_ = EmptyList_containsAll_CollectionNothing_Z_k_
    this.get_I_k_ = EmptyList_get_I_k_
    this.indexOf_Nothing_I_k_ = EmptyList_indexOf_Nothing_I_k_
    this.lastIndexOf_Nothing_I_k_ = EmptyList_lastIndexOf_Nothing_I_k_
    this.iterator_IteratorNothing_k_ = EmptyList_iterator_IteratorNothing_k_
    this.listIterator_ListIteratorNothing_k_ = EmptyList_listIterator_ListIteratorNothing_k_
    this.listIterator_I_ListIteratorNothing_k_ = EmptyList_listIterator_I_ListIteratorNothing_k_
    this.subList_I_I_ListNothing_k_ = EmptyList_subList_I_I_ListNothing_k_
    this.equals_AnyN_Z_k_ = EmptyList_equals_AnyN_Z_k_
    this.hashCode_I_k_ = EmptyList_hashCode_I_k_
    this.toString_Str_k_ = EmptyList_toString_Str_k_
    this.get_size = EmptyList_get_size_I_k_
    return this
end function

function EmptyList_getInstance() as Object
    if m.EmptyList_instance = invalid then
        m.EmptyList_instance = EmptyList_create()
    end if
    return m.EmptyList_instance
end function

function EmptyList_isEmpty_Z_k_() as Boolean
    return true
end function

function EmptyList_contains_Nothing_Z_k_(element as Dynamic) as Boolean
    return false
end function

function EmptyList_containsAll_CollectionNothing_Z_k_(elements as Object) as Boolean
    return elements.isEmpty()
end function

sub EmptyList_get_I_k_(index as Integer)
    throw IndexOutOfBoundsException_create_StrN_IndexOutOfBoundsException_k_("Empty list")
end sub

function EmptyList_indexOf_Nothing_I_k_(element as Dynamic) as Integer
    return -1
end function

function EmptyList_lastIndexOf_Nothing_I_k_(element as Dynamic) as Integer
    return -1
end function

function EmptyList_iterator_IteratorNothing_k_() as Object
    return EmptyIterator_getInstance()
end function

function EmptyList_listIterator_ListIteratorNothing_k_() as Object
    return EmptyIterator_getInstance()
end function

function EmptyList_listIterator_I_ListIteratorNothing_k_(index as Integer) as Object
    if index <> 0 then
        throw IndexOutOfBoundsException_create_StrN_IndexOutOfBoundsException_k_("Index: " + index)
    end if
    return EmptyIterator_getInstance()
end function

function EmptyList_subList_I_I_ListNothing_k_(fromIndex as Integer, toIndex as Integer) as Object
    if (fromIndex <> 0) or (toIndex <> 0) then
        throw IndexOutOfBoundsException_create_IndexOutOfBoundsException_k_()
    end if
    return m
end function

function EmptyList_equals_AnyN_Z_k_(other as Dynamic) as Boolean
    return __kotlin_isInstanceOf(other, "List") and other.isEmpty()
end function

function EmptyList_hashCode_I_k_() as Integer
    return 1
end function

function EmptyList_toString_Str_k_() as String
    return "[]"
end function

function EmptyList_get_size_I_k_() as Integer
    return 0
end function

function EmptyIterator_create_EmptyIterator_k_() as Object
    this = {}
    this.__type = "EmptyIterator"
    this.__proto = ["EmptyIterator"]
    this.hasNext_Z_k_ = EmptyIterator_hasNext_Z_k_
    this.next = EmptyIterator_next
    this.hasPrevious_Z_k_ = EmptyIterator_hasPrevious_Z_k_
    this.previous = EmptyIterator_previous
    this.nextIndex_I_k_ = EmptyIterator_nextIndex_I_k_
    this.previousIndex_I_k_ = EmptyIterator_previousIndex_I_k_
    return this
end function

function EmptyIterator_getInstance() as Object
    if m.EmptyIterator_instance = invalid then
        m.EmptyIterator_instance = EmptyIterator_create()
    end if
    return m.EmptyIterator_instance
end function

function EmptyIterator_hasNext_Z_k_() as Boolean
    return false
end function

sub EmptyIterator_next()
    throw NoSuchElementException_create_NoSuchElementException_k_()
end sub

function EmptyIterator_hasPrevious_Z_k_() as Boolean
    return false
end function

sub EmptyIterator_previous()
    throw NoSuchElementException_create_NoSuchElementException_k_()
end sub

function EmptyIterator_nextIndex_I_k_() as Integer
    return 0
end function

function EmptyIterator_previousIndex_I_k_() as Integer
    return -1
end function
