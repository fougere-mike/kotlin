function ArrayList_create_k_() as Object
    this = {}
    this.__type = "ArrayList"
    this.__proto = ["ArrayList", "MutableList", "List", "Collection", "Iterable", "MutableCollection", "MutableIterable", "RandomAccess"]
    this.__id = __kotlin_nextObjectId()
    this.isEmpty_k_ = ArrayList_isEmpty_k_
    this.contains_AnyN_k_ = ArrayList_contains_AnyN_k_
    this.containsAll_Collection_k_ = ArrayList_containsAll_Collection_k_
    this.get_I_k_ = ArrayList_get_I_k_
    this.set_I_AnyN_k_ = ArrayList_set_I_AnyN_k_
    this.add_AnyN_k_ = ArrayList_add_AnyN_k_
    this.add_I_AnyN_k_ = ArrayList_add_I_AnyN_k_
    this.remove_AnyN_k_ = ArrayList_remove_AnyN_k_
    this.removeAt_I_k_ = ArrayList_removeAt_I_k_
    this.addAll_Collection_k_ = ArrayList_addAll_Collection_k_
    this.addAll_I_Collection_k_ = ArrayList_addAll_I_Collection_k_
    this.removeAll_Collection_k_ = ArrayList_removeAll_Collection_k_
    this.retainAll_Collection_k_ = ArrayList_retainAll_Collection_k_
    this.clear_k_ = ArrayList_clear_k_
    this.indexOf_AnyN_k_ = ArrayList_indexOf_AnyN_k_
    this.lastIndexOf_AnyN_k_ = ArrayList_lastIndexOf_AnyN_k_
    this.iterator_k_ = ArrayList_iterator_k_
    this.listIterator_k_ = ArrayList_listIterator_k_
    this.listIterator_I_k_ = ArrayList_listIterator_I_k_
    this.subList_I_I_k_ = ArrayList_subList_I_I_k_
    this.equals_AnyN_k_ = ArrayList_equals_AnyN_k_
    this.equals = ArrayList_equals_AnyN_k_
    this.hashCode_k_ = ArrayList_hashCode_k_
    this.hashCode = ArrayList_hashCode_k_
    this.toString_k_ = ArrayList_toString_k_
    this.toString = ArrayList_toString_k_
    this.brsArrayInsertAt_Dynamic_I_AnyN_k_ = ArrayList_brsArrayInsertAt_Dynamic_I_AnyN_k_
    this.get_array = ArrayList_get_array_k_
    this.set_array = ArrayList_set_array_Dynamic_k_
    this.get_size = ArrayList_get_size_k_
    this.set_array(CreateObject("roArray", 0, true))
    return this
end function

function ArrayList_create_I_k_(initialCapacity as Integer) as Object
    this = {}
    this.__type = "ArrayList"
    this.__proto = ["ArrayList", "MutableList", "List", "Collection", "Iterable", "MutableCollection", "MutableIterable", "RandomAccess"]
    this.__id = __kotlin_nextObjectId()
    this.isEmpty_k_ = ArrayList_isEmpty_k_
    this.contains_AnyN_k_ = ArrayList_contains_AnyN_k_
    this.containsAll_Collection_k_ = ArrayList_containsAll_Collection_k_
    this.get_I_k_ = ArrayList_get_I_k_
    this.set_I_AnyN_k_ = ArrayList_set_I_AnyN_k_
    this.add_AnyN_k_ = ArrayList_add_AnyN_k_
    this.add_I_AnyN_k_ = ArrayList_add_I_AnyN_k_
    this.remove_AnyN_k_ = ArrayList_remove_AnyN_k_
    this.removeAt_I_k_ = ArrayList_removeAt_I_k_
    this.addAll_Collection_k_ = ArrayList_addAll_Collection_k_
    this.addAll_I_Collection_k_ = ArrayList_addAll_I_Collection_k_
    this.removeAll_Collection_k_ = ArrayList_removeAll_Collection_k_
    this.retainAll_Collection_k_ = ArrayList_retainAll_Collection_k_
    this.clear_k_ = ArrayList_clear_k_
    this.indexOf_AnyN_k_ = ArrayList_indexOf_AnyN_k_
    this.lastIndexOf_AnyN_k_ = ArrayList_lastIndexOf_AnyN_k_
    this.iterator_k_ = ArrayList_iterator_k_
    this.listIterator_k_ = ArrayList_listIterator_k_
    this.listIterator_I_k_ = ArrayList_listIterator_I_k_
    this.subList_I_I_k_ = ArrayList_subList_I_I_k_
    this.equals_AnyN_k_ = ArrayList_equals_AnyN_k_
    this.equals = ArrayList_equals_AnyN_k_
    this.hashCode_k_ = ArrayList_hashCode_k_
    this.hashCode = ArrayList_hashCode_k_
    this.toString_k_ = ArrayList_toString_k_
    this.toString = ArrayList_toString_k_
    this.brsArrayInsertAt_Dynamic_I_AnyN_k_ = ArrayList_brsArrayInsertAt_Dynamic_I_AnyN_k_
    this.get_array = ArrayList_get_array_k_
    this.set_array = ArrayList_set_array_Dynamic_k_
    this.get_size = ArrayList_get_size_k_
    require_Z_Function0Any_k_(initialCapacity >= 0, {initialCapacity: initialCapacity, invoke: function() as Object
        return "Negative initial capacity: " + __kotlin_numToStr_I_k_(m.initialCapacity)
    end function})
    this.set_array(CreateObject("roArray", initialCapacity, true))
    return this
end function

function ArrayList_create_Collection_k_(elements as Object) as Object
    this = {}
    this.__type = "ArrayList"
    this.__proto = ["ArrayList", "MutableList", "List", "Collection", "Iterable", "MutableCollection", "MutableIterable", "RandomAccess"]
    this.__id = __kotlin_nextObjectId()
    this.isEmpty_k_ = ArrayList_isEmpty_k_
    this.contains_AnyN_k_ = ArrayList_contains_AnyN_k_
    this.containsAll_Collection_k_ = ArrayList_containsAll_Collection_k_
    this.get_I_k_ = ArrayList_get_I_k_
    this.set_I_AnyN_k_ = ArrayList_set_I_AnyN_k_
    this.add_AnyN_k_ = ArrayList_add_AnyN_k_
    this.add_I_AnyN_k_ = ArrayList_add_I_AnyN_k_
    this.remove_AnyN_k_ = ArrayList_remove_AnyN_k_
    this.removeAt_I_k_ = ArrayList_removeAt_I_k_
    this.addAll_Collection_k_ = ArrayList_addAll_Collection_k_
    this.addAll_I_Collection_k_ = ArrayList_addAll_I_Collection_k_
    this.removeAll_Collection_k_ = ArrayList_removeAll_Collection_k_
    this.retainAll_Collection_k_ = ArrayList_retainAll_Collection_k_
    this.clear_k_ = ArrayList_clear_k_
    this.indexOf_AnyN_k_ = ArrayList_indexOf_AnyN_k_
    this.lastIndexOf_AnyN_k_ = ArrayList_lastIndexOf_AnyN_k_
    this.iterator_k_ = ArrayList_iterator_k_
    this.listIterator_k_ = ArrayList_listIterator_k_
    this.listIterator_I_k_ = ArrayList_listIterator_I_k_
    this.subList_I_I_k_ = ArrayList_subList_I_I_k_
    this.equals_AnyN_k_ = ArrayList_equals_AnyN_k_
    this.equals = ArrayList_equals_AnyN_k_
    this.hashCode_k_ = ArrayList_hashCode_k_
    this.hashCode = ArrayList_hashCode_k_
    this.toString_k_ = ArrayList_toString_k_
    this.toString = ArrayList_toString_k_
    this.brsArrayInsertAt_Dynamic_I_AnyN_k_ = ArrayList_brsArrayInsertAt_Dynamic_I_AnyN_k_
    this.get_array = ArrayList_get_array_k_
    this.set_array = ArrayList_set_array_Dynamic_k_
    this.get_size = ArrayList_get_size_k_
    this.set_array(CreateObject("roArray", 0, true))
    this.addAll_Collection_k_(elements)
    return this
end function

function ArrayList_isEmpty_k_() as Boolean
    return m.get_size() = 0
end function

function ArrayList_contains_AnyN_k_(element as Dynamic) as Boolean
    return m.indexOf_AnyN_k_(element) >= 0
end function

function ArrayList_containsAll_Collection_k_(elements as Object) as Boolean
    __iter_96 = elements.iterator_k_()
    while __iter_96.hasNext_k_()
        element = __iter_96.next_k_()
        if not m.contains_AnyN_k_(element) then
            return false
        end if
    end while

    return true
end function

function ArrayList_get_I_k_(index as Integer) as Dynamic
    checkElementIndex_I_I_k_(index, m.get_size())
    return m.get_array()[index]
end function

function ArrayList_set_I_AnyN_k_(index as Integer, element as Dynamic) as Dynamic
    checkElementIndex_I_I_k_(index, m.get_size())
    oldValue = m.get_array()[index]
    m.get_array()[index] = element
    return oldValue
end function

function ArrayList_add_AnyN_k_(element as Dynamic) as Boolean
    m.get_array().Push(element)
    return true
end function

sub ArrayList_add_I_AnyN_k_(index as Integer, element as Dynamic)
    checkPositionIndex_I_I_k_(index, m.get_size())
    if index = m.get_size() then
        m.get_array().Push(element)
    else if true then
        m.brsArrayInsertAt_Dynamic_I_AnyN_k_(m.get_array(), index, element)
    end if
end sub

function ArrayList_remove_AnyN_k_(element as Dynamic) as Boolean
    index = m.indexOf_AnyN_k_(element)
    if index < 0 then
        return false
    end if
    m.removeAt_I_k_(index)
    return true
end function

function ArrayList_removeAt_I_k_(index as Integer) as Dynamic
    checkElementIndex_I_I_k_(index, m.get_size())
    oldValue = m.get_array()[index]
    m.get_array().Delete(index)
    return oldValue
end function

function ArrayList_addAll_Collection_k_(elements as Object) as Boolean
    if elements.isEmpty_k_() then
        return false
    end if
    __iter_97 = elements.iterator_k_()
    while __iter_97.hasNext_k_()
        element = __iter_97.next_k_()
        m.get_array().Push(element)

    end while

    return true
end function

function ArrayList_addAll_I_Collection_k_(index as Integer, elements as Object) as Boolean
    checkPositionIndex_I_I_k_(index, m.get_size())
    if elements.isEmpty_k_() then
        return false
    end if
    currentIndex = index
    __iter_98 = elements.iterator_k_()
    while __iter_98.hasNext_k_()
        element = __iter_98.next_k_()
        m.add_I_AnyN_k_(currentIndex, element)
        currentIndex = (currentIndex + 1)

    end while

    return true
end function

function ArrayList_removeAll_Collection_k_(elements as Object) as Boolean
    modified = false
    __iter_99 = elements.iterator_k_()
    while __iter_99.hasNext_k_()
        element = __iter_99.next_k_()
        while m.remove_AnyN_k_(element)
            modified = true
        end while

    end while

    return modified
end function

function ArrayList_retainAll_Collection_k_(elements as Object) as Boolean
    modified = false
    iter = m.iterator_k_()
    while iter.hasNext_k_()
        if not elements.contains_AnyN_k_(iter.next_k_()) then
            iter.remove_k_()
            modified = true
        end if
    end while
    return modified
end function

sub ArrayList_clear_k_()
    m.get_array().Clear()
end sub

function ArrayList_indexOf_AnyN_k_(element as Dynamic) as Integer
    progression = until_rI_I_k_(0, m.get_size())
    inductionVariable = progression.get_first()
    last = progression.get_last()
    if inductionVariable <= last then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if brsStructuralEquals_AnyN_AnyN_k_(m.get_array()[i], element) then
            return i
        end if


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if brsStructuralEquals_AnyN_AnyN_k_(m.get_array()[i], element) then
                return i
            end if

        end while

    end if

    return -1
end function

function ArrayList_lastIndexOf_AnyN_k_(element as Dynamic) as Integer
    i = m.get_size() - 1
    while i >= 0
        if brsStructuralEquals_AnyN_AnyN_k_(m.get_array()[i], element) then
            return i
        end if
        i = (i - 1)
    end while
    return -1
end function

function ArrayList_iterator_k_() as Object
    return ArrayListIterator_create_ArrayList_I_k_(m, 0)
end function

function ArrayList_listIterator_k_() as Object
    return ArrayListIterator_create_ArrayList_I_k_(m, 0)
end function

function ArrayList_listIterator_I_k_(index as Integer) as Object
    checkPositionIndex_I_I_k_(index, m.get_size())
    return ArrayListIterator_create_ArrayList_I_k_(m, index)
end function

function ArrayList_subList_I_I_k_(fromIndex as Integer, toIndex as Integer) as Object
    checkRangeIndexes_I_I_I_k_(fromIndex, toIndex, m.get_size())
    return SubList_create_ArrayList_I_I_k_(m, fromIndex, toIndex)
end function

function ArrayList_equals_AnyN_k_(other as Dynamic) as Boolean
    if __kotlin_identityEquals(other, m) then
        return true
    end if
    if not __kotlin_isInstanceOf(other, "List") then
        return false
    end if
    if other.get_size() <> m.get_size() then
        return false
    end if
    i = 0
    __iter_100 = other.iterator_k_()
    while __iter_100.hasNext_k_()
        element = __iter_100.next_k_()
        if not brsStructuralEquals_AnyN_AnyN_k_(m.get_I_k_(i), element) then
            return false
        end if
        i = (i + 1)

    end while

    return true
end function

function ArrayList_hashCode_k_() as Integer
    hashCode = 1
    progression = until_rI_I_k_(0, m.get_size())
    inductionVariable = progression.get_first()
    last = progression.get_last()
    if inductionVariable <= last then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        element = m.get_I_k_(i)
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

            element = m.get_I_k_(i)
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

function ArrayList_toString_k_() as String
    if m.isEmpty_k_() then
        return "[]"
    end if
    sb = StringBuilder_create_k_()
    sb.append_StrN_k_("[")
    progression = until_rI_I_k_(0, m.get_size())
    inductionVariable = progression.get_first()
    last = progression.get_last()
    if inductionVariable <= last then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if i > 0 then
            sb.append_StrN_k_(", ")
        end if
        element = m.get_I_k_(i)
        if __kotlin_identityEquals(element, m) then
            sb.append_StrN_k_("(this Collection)")
        else if true then
            sb.append_StrN_k_(toString_AnyN_k_(element))
        end if


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if i > 0 then
                sb.append_StrN_k_(", ")
            end if
            element = m.get_I_k_(i)
            if __kotlin_identityEquals(element, m) then
                sb.append_StrN_k_("(this Collection)")
            else if true then
                sb.append_StrN_k_(toString_AnyN_k_(element))
            end if

        end while

    end if

    sb.append_StrN_k_("]")
    return sb.toString()
end function

sub ArrayList_brsArrayInsertAt_Dynamic_I_AnyN_k_(arr as Object, index as Integer, value as Dynamic)
    arr.Push(value)
    i = m.get_size() - 1
    while i > index
        arr[i] = arr[i - 1]
        i = (i - 1)
    end while
    arr[index] = value
end sub

function ArrayList_get_array_k_() as Object
    return m.array
end function

sub ArrayList_set_array_Dynamic_k_(value as Object)
    m.array = value
end sub

function ArrayList_get_size_k_() as Integer
    return m.get_array().Count()
end function

function ArrayListIterator_create_ArrayList_I_k_(list as Object, index as Integer) as Object
    this = {}
    this.__type = "ArrayListIterator"
    this.__proto = ["ArrayListIterator", "MutableListIterator", "ListIterator", "Iterator", "MutableIterator"]
    this.__id = __kotlin_nextObjectId()
    this.hasNext_k_ = ArrayListIterator_hasNext_k_
    this.next_k_ = ArrayListIterator_next_k_
    this.hasPrevious_k_ = ArrayListIterator_hasPrevious_k_
    this.previous_k_ = ArrayListIterator_previous_k_
    this.nextIndex_k_ = ArrayListIterator_nextIndex_k_
    this.previousIndex_k_ = ArrayListIterator_previousIndex_k_
    this.remove_k_ = ArrayListIterator_remove_k_
    this.set_AnyN_k_ = ArrayListIterator_set_AnyN_k_
    this.add_AnyN_k_ = ArrayListIterator_add_AnyN_k_
    this.get_list = ArrayListIterator_get_list_k_
    this.get_index = ArrayListIterator_get_index_k_
    this.set_index = ArrayListIterator_set_index_I_k_
    this.get_lastReturned = ArrayListIterator_get_lastReturned_k_
    this.set_lastReturned = ArrayListIterator_set_lastReturned_I_k_
    this.list = list
    this.index = index
    this.lastReturned = -1
    return this
end function

function ArrayListIterator_hasNext_k_() as Boolean
    return m.get_index() < m.get_list().get_size()
end function

function ArrayListIterator_next_k_() as Dynamic
    if not m.hasNext_k_() then
        throw NoSuchElementException_create_k_()
    end if
    m.set_lastReturned(m.get_index())
    __incr_tmp_101 = m.get_index()
    m.set_index(__incr_tmp_101 + 1)
    return m.get_list().get_I_k_(__incr_tmp_101)

end function

function ArrayListIterator_hasPrevious_k_() as Boolean
    return m.get_index() > 0
end function

function ArrayListIterator_previous_k_() as Dynamic
    if not m.hasPrevious_k_() then
        throw NoSuchElementException_create_k_()
    end if
    m.set_lastReturned(m.get_index())
    return m.get_list().get_I_k_(m.get_index())
end function

function ArrayListIterator_nextIndex_k_() as Integer
    return m.get_index()
end function

function ArrayListIterator_previousIndex_k_() as Integer
    return m.get_index() - 1
end function

sub ArrayListIterator_remove_k_()
    check_Z_Function0Any_k_(m.get_lastReturned() >= 0, {invoke: function() as Object
        return "Call next() or previous() before remove()"
    end function})
    m.get_list().removeAt_I_k_(m.get_lastReturned())
    if m.get_lastReturned() < m.get_index() then
        m.set_index(m.get_index() - 1)
    end if
    m.set_lastReturned(-1)
end sub

sub ArrayListIterator_set_AnyN_k_(element as Dynamic)
    check_Z_Function0Any_k_(m.get_lastReturned() >= 0, {invoke: function() as Object
        return "Call next() or previous() before set()"
    end function})
    m.get_list().set_I_AnyN_k_(m.get_lastReturned(), element)
end sub

sub ArrayListIterator_add_AnyN_k_(element as Dynamic)
    __incr_tmp_102 = m.get_index()
    m.set_index(__incr_tmp_102 + 1)
    m.get_list().add_I_AnyN_k_(__incr_tmp_102, element)

    m.set_lastReturned(-1)
end sub

function ArrayListIterator_get_list_k_() as Object
    return m.list
end function

function ArrayListIterator_get_index_k_() as Integer
    return m.index
end function

sub ArrayListIterator_set_index_I_k_(value as Integer)
    m.index = value
end sub

function ArrayListIterator_get_lastReturned_k_() as Integer
    return m.lastReturned
end function

sub ArrayListIterator_set_lastReturned_I_k_(value as Integer)
    m.lastReturned = value
end sub

function SubList_create_ArrayList_I_I_k_(parent as Object, fromIndex as Integer, toIndex as Integer) as Object
    this = {}
    this.__type = "SubList"
    this.__proto = ["SubList", "MutableList", "List", "Collection", "Iterable", "MutableCollection", "MutableIterable"]
    this.__id = __kotlin_nextObjectId()
    this.isEmpty_k_ = SubList_isEmpty_k_
    this.contains_AnyN_k_ = SubList_contains_AnyN_k_
    this.containsAll_Collection_k_ = SubList_containsAll_Collection_k_
    this.get_I_k_ = SubList_get_I_k_
    this.set_I_AnyN_k_ = SubList_set_I_AnyN_k_
    this.add_AnyN_k_ = SubList_add_AnyN_k_
    this.add_I_AnyN_k_ = SubList_add_I_AnyN_k_
    this.remove_AnyN_k_ = SubList_remove_AnyN_k_
    this.removeAt_I_k_ = SubList_removeAt_I_k_
    this.addAll_Collection_k_ = SubList_addAll_Collection_k_
    this.addAll_I_Collection_k_ = SubList_addAll_I_Collection_k_
    this.removeAll_Collection_k_ = SubList_removeAll_Collection_k_
    this.retainAll_Collection_k_ = SubList_retainAll_Collection_k_
    this.clear_k_ = SubList_clear_k_
    this.indexOf_AnyN_k_ = SubList_indexOf_AnyN_k_
    this.lastIndexOf_AnyN_k_ = SubList_lastIndexOf_AnyN_k_
    this.iterator_k_ = SubList_iterator_k_
    this.listIterator_k_ = SubList_listIterator_k_
    this.listIterator_I_k_ = SubList_listIterator_I_k_
    this.subList_I_I_k_ = SubList_subList_I_I_k_
    this.get_parent = SubList_get_parent_k_
    this.get_fromIndex = SubList_get_fromIndex_k_
    this.get_toIndex = SubList_get_toIndex_k_
    this.set_toIndex = SubList_set_toIndex_I_k_
    this.get_size = SubList_get_size_k_
    this.get_array = SubList_get_array_k_
    this.parent = parent
    this.fromIndex = fromIndex
    this.toIndex = toIndex
    return this
end function

function SubList_isEmpty_k_() as Boolean
    return m.get_size() = 0
end function

function SubList_contains_AnyN_k_(element as Dynamic) as Boolean
    return m.indexOf_AnyN_k_(element) >= 0
end function

function SubList_containsAll_Collection_k_(elements as Object) as Boolean
    __iter_103 = elements.iterator_k_()
    while __iter_103.hasNext_k_()
        element = __iter_103.next_k_()
        if not m.contains_AnyN_k_(element) then
            return false
        end if
    end while

    return true
end function

function SubList_get_I_k_(index as Integer) as Dynamic
    checkElementIndex_I_I_k_(index, m.get_size())
    return m.get_parent().get_I_k_(m.get_fromIndex() + index)
end function

function SubList_set_I_AnyN_k_(index as Integer, element as Dynamic) as Dynamic
    checkElementIndex_I_I_k_(index, m.get_size())
    return m.get_parent().set_I_AnyN_k_(m.get_fromIndex() + index, element)
end function

function SubList_add_AnyN_k_(element as Dynamic) as Boolean
    m.get_parent().add_I_AnyN_k_(m.get_toIndex(), element)
    m.set_toIndex(m.get_toIndex() + 1)
    return true
end function

sub SubList_add_I_AnyN_k_(index as Integer, element as Dynamic)
    checkPositionIndex_I_I_k_(index, m.get_size())
    m.get_parent().add_I_AnyN_k_(m.get_fromIndex() + index, element)
    m.set_toIndex(m.get_toIndex() + 1)
end sub

function SubList_remove_AnyN_k_(element as Dynamic) as Boolean
    index = m.indexOf_AnyN_k_(element)
    if index < 0 then
        return false
    end if
    m.removeAt_I_k_(index)
    return true
end function

function SubList_removeAt_I_k_(index as Integer) as Dynamic
    checkElementIndex_I_I_k_(index, m.get_size())
    result = m.get_parent().removeAt_I_k_(m.get_fromIndex() + index)
    m.set_toIndex(m.get_toIndex() - 1)
    return result
end function

function SubList_addAll_Collection_k_(elements as Object) as Boolean
    return m.addAll_I_Collection_k_(m.get_size(), elements)
end function

function SubList_addAll_I_Collection_k_(index as Integer, elements as Object) as Boolean
    checkPositionIndex_I_I_k_(index, m.get_size())
    if elements.isEmpty_k_() then
        return false
    end if
    m.get_parent().addAll_I_Collection_k_(m.get_fromIndex() + index, elements)
    m.set_toIndex(m.get_toIndex() + elements.get_size())
    return true
end function

function SubList_removeAll_Collection_k_(elements as Object) as Boolean
    modified = false
    iter = m.iterator_k_()
    while iter.hasNext_k_()
        if elements.contains_AnyN_k_(iter.next_k_()) then
            iter.remove_k_()
            modified = true
        end if
    end while
    return modified
end function

function SubList_retainAll_Collection_k_(elements as Object) as Boolean
    modified = false
    iter = m.iterator_k_()
    while iter.hasNext_k_()
        if not elements.contains_AnyN_k_(iter.next_k_()) then
            iter.remove_k_()
            modified = true
        end if
    end while
    return modified
end function

sub SubList_clear_k_()
    while m.get_size() > 0
        m.removeAt_I_k_(0)
    end while
end sub

function SubList_indexOf_AnyN_k_(element as Dynamic) as Integer
    progression = until_rI_I_k_(0, m.get_size())
    inductionVariable = progression.get_first()
    last = progression.get_last()
    if inductionVariable <= last then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if brsStructuralEquals_AnyN_AnyN_k_(m.get_I_k_(i), element) then
            return i
        end if


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if brsStructuralEquals_AnyN_AnyN_k_(m.get_I_k_(i), element) then
                return i
            end if

        end while

    end if

    return -1
end function

function SubList_lastIndexOf_AnyN_k_(element as Dynamic) as Integer
    i = m.get_size() - 1
    while i >= 0
        if brsStructuralEquals_AnyN_AnyN_k_(m.get_I_k_(i), element) then
            return i
        end if
        i = (i - 1)
    end while
    return -1
end function

function SubList_iterator_k_() as Object
    return m.listIterator_k_()
end function

function SubList_listIterator_k_() as Object
    return SubListIterator_create_SubList_I_k_(m, 0)
end function

function SubList_listIterator_I_k_(index as Integer) as Object
    checkPositionIndex_I_I_k_(index, m.get_size())
    return SubListIterator_create_SubList_I_k_(m, index)
end function

function SubList_subList_I_I_k_(fromIndex as Integer, toIndex as Integer) as Object
    checkRangeIndexes_I_I_I_k_(fromIndex, toIndex, m.get_size())
    return SubList_create_ArrayList_I_I_k_(m.get_parent(), m.get_fromIndex() + fromIndex, m.get_fromIndex() + toIndex)
end function

function SubList_get_parent_k_() as Object
    return m.parent
end function

function SubList_get_fromIndex_k_() as Integer
    return m.fromIndex
end function

function SubList_get_toIndex_k_() as Integer
    return m.toIndex
end function

sub SubList_set_toIndex_I_k_(value as Integer)
    m.toIndex = value
end sub

function SubList_get_size_k_() as Integer
    return m.get_toIndex() - m.get_fromIndex()
end function

function SubList_get_array_k_() as Object
    result = ArrayList_create_I_k_(m.get_size())
    progression = until_rI_I_k_(0, m.get_size())
    inductionVariable = progression.get_first()
    last = progression.get_last()
    if inductionVariable <= last then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        result.add_AnyN_k_(m.get_parent().get_I_k_(m.get_fromIndex() + i))


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            result.add_AnyN_k_(m.get_parent().get_I_k_(m.get_fromIndex() + i))

        end while

    end if

    return result.get_array()
end function

function SubListIterator_create_SubList_I_k_(list as Object, index as Integer) as Object
    this = {}
    this.__type = "SubListIterator"
    this.__proto = ["SubListIterator", "MutableListIterator", "ListIterator", "Iterator", "MutableIterator"]
    this.__id = __kotlin_nextObjectId()
    this.hasNext_k_ = SubListIterator_hasNext_k_
    this.next_k_ = SubListIterator_next_k_
    this.hasPrevious_k_ = SubListIterator_hasPrevious_k_
    this.previous_k_ = SubListIterator_previous_k_
    this.nextIndex_k_ = SubListIterator_nextIndex_k_
    this.previousIndex_k_ = SubListIterator_previousIndex_k_
    this.remove_k_ = SubListIterator_remove_k_
    this.set_AnyN_k_ = SubListIterator_set_AnyN_k_
    this.add_AnyN_k_ = SubListIterator_add_AnyN_k_
    this.get_list = SubListIterator_get_list_k_
    this.get_index = SubListIterator_get_index_k_
    this.set_index = SubListIterator_set_index_I_k_
    this.get_lastReturned = SubListIterator_get_lastReturned_k_
    this.set_lastReturned = SubListIterator_set_lastReturned_I_k_
    this.list = list
    this.index = index
    this.lastReturned = -1
    return this
end function

function SubListIterator_hasNext_k_() as Boolean
    return m.get_index() < m.get_list().get_size()
end function

function SubListIterator_next_k_() as Dynamic
    if not m.hasNext_k_() then
        throw NoSuchElementException_create_k_()
    end if
    m.set_lastReturned(m.get_index())
    __incr_tmp_104 = m.get_index()
    m.set_index(__incr_tmp_104 + 1)
    return m.get_list().get_I_k_(__incr_tmp_104)

end function

function SubListIterator_hasPrevious_k_() as Boolean
    return m.get_index() > 0
end function

function SubListIterator_previous_k_() as Dynamic
    if not m.hasPrevious_k_() then
        throw NoSuchElementException_create_k_()
    end if
    m.set_lastReturned(m.get_index())
    return m.get_list().get_I_k_(m.get_index())
end function

function SubListIterator_nextIndex_k_() as Integer
    return m.get_index()
end function

function SubListIterator_previousIndex_k_() as Integer
    return m.get_index() - 1
end function

sub SubListIterator_remove_k_()
    check_Z_Function0Any_k_(m.get_lastReturned() >= 0, {invoke: function() as Object
        return "Call next() or previous() before remove()"
    end function})
    m.get_list().removeAt_I_k_(m.get_lastReturned())
    if m.get_lastReturned() < m.get_index() then
        m.set_index(m.get_index() - 1)
    end if
    m.set_lastReturned(-1)
end sub

sub SubListIterator_set_AnyN_k_(element as Dynamic)
    check_Z_Function0Any_k_(m.get_lastReturned() >= 0, {invoke: function() as Object
        return "Call next() or previous() before set()"
    end function})
    m.get_list().set_I_AnyN_k_(m.get_lastReturned(), element)
end sub

sub SubListIterator_add_AnyN_k_(element as Dynamic)
    __incr_tmp_105 = m.get_index()
    m.set_index(__incr_tmp_105 + 1)
    m.get_list().add_I_AnyN_k_(__incr_tmp_105, element)

    m.set_lastReturned(-1)
end sub

function SubListIterator_get_list_k_() as Object
    return m.list
end function

function SubListIterator_get_index_k_() as Integer
    return m.index
end function

sub SubListIterator_set_index_I_k_(value as Integer)
    m.index = value
end sub

function SubListIterator_get_lastReturned_k_() as Integer
    return m.lastReturned
end function

sub SubListIterator_set_lastReturned_I_k_(value as Integer)
    m.lastReturned = value
end sub

function arrayListOf_k_() as Object
    return ArrayList_create_k_()
end function

function arrayListOf_Arr_k_(elements as Object) as Object
    list = ArrayList_create_I_k_(elements.count())
    indexedObject = elements
    inductionVariable = 0
    last = indexedObject.count()
    while inductionVariable < last
        element = indexedObject[inductionVariable]
        inductionVariable = (inductionVariable + 1)

        list.add_AnyN_k_(element)

    end while

    return list
end function

function mutableListOf_k_() as Object
    return ArrayList_create_k_()
end function

function mutableListOf_Arr_k_(elements as Object) as Object
    return arrayListOf_Arr_k_(elements)
end function

function listOf_k_() as Object
    return emptyList_k_()
end function

function listOf_Arr_k_(elements as Object) as Object
    __when_tmp2 = invalid
    if elements.count() = 0 then
        __when_tmp2 = emptyList_k_()
    else if true then
        __when_tmp2 = arrayListOf_Arr_k_(elements)
    end if
    return __when_tmp2

end function

function emptyList_k_() as Object
    return EmptyList_getInstance()
end function

function EmptyList_create_k_() as Object
    this = {}
    this.__type = "EmptyList"
    this.__proto = ["EmptyList", "List", "Collection", "Iterable", "RandomAccess"]
    this.__id = __kotlin_nextObjectId()
    this.isEmpty_k_ = EmptyList_isEmpty_k_
    this.contains_Nothing_k_ = EmptyList_contains_Nothing_k_
    this.containsAll_CollectionNothing_k_ = EmptyList_containsAll_CollectionNothing_k_
    this.get_I_k_ = EmptyList_get_I_k_
    this.indexOf_Nothing_k_ = EmptyList_indexOf_Nothing_k_
    this.lastIndexOf_Nothing_k_ = EmptyList_lastIndexOf_Nothing_k_
    this.iterator_k_ = EmptyList_iterator_k_
    this.listIterator_k_ = EmptyList_listIterator_k_
    this.listIterator_I_k_ = EmptyList_listIterator_I_k_
    this.subList_I_I_k_ = EmptyList_subList_I_I_k_
    this.equals_AnyN_k_ = EmptyList_equals_AnyN_k_
    this.equals = EmptyList_equals_AnyN_k_
    this.hashCode_k_ = EmptyList_hashCode_k_
    this.hashCode = EmptyList_hashCode_k_
    this.toString_k_ = EmptyList_toString_k_
    this.toString = EmptyList_toString_k_
    this.get_array = EmptyList_get_array_k_
    this.get_size = EmptyList_get_size_k_
    return this
end function

function EmptyList_getInstance() as Object
    if m.EmptyList_instance = invalid then
        m.EmptyList_instance = EmptyList_create_k_()
    end if
    return m.EmptyList_instance
end function

function EmptyList_isEmpty_k_() as Boolean
    return true
end function

function EmptyList_contains_Nothing_k_(element as Dynamic) as Boolean
    return false
end function

function EmptyList_containsAll_CollectionNothing_k_(elements as Object) as Boolean
    return elements.isEmpty_k_()
end function

sub EmptyList_get_I_k_(index as Integer)
    throw IndexOutOfBoundsException_create_StrN_k_("Empty list")
end sub

function EmptyList_indexOf_Nothing_k_(element as Dynamic) as Integer
    return -1
end function

function EmptyList_lastIndexOf_Nothing_k_(element as Dynamic) as Integer
    return -1
end function

function EmptyList_iterator_k_() as Object
    return EmptyIterator_getInstance()
end function

function EmptyList_listIterator_k_() as Object
    return EmptyIterator_getInstance()
end function

function EmptyList_listIterator_I_k_(index as Integer) as Object
    if index <> 0 then
        throw IndexOutOfBoundsException_create_StrN_k_("Index: " + __kotlin_numToStr_I_k_(index))
    end if
    return EmptyIterator_getInstance()
end function

function EmptyList_subList_I_I_k_(fromIndex as Integer, toIndex as Integer) as Object
    if (fromIndex <> 0) or (toIndex <> 0) then
        throw IndexOutOfBoundsException_create_k_()
    end if
    return m
end function

function EmptyList_equals_AnyN_k_(other as Dynamic) as Boolean
    return __kotlin_isInstanceOf(other, "List") and other.isEmpty_k_()
end function

function EmptyList_hashCode_k_() as Integer
    return 1
end function

function EmptyList_toString_k_() as String
    return "[]"
end function

function EmptyList_get_array_k_() as Object
    return CreateObject("roArray", 0, true)
end function

function EmptyList_get_size_k_() as Integer
    return 0
end function

function EmptyIterator_create_k_() as Object
    this = {}
    this.__type = "EmptyIterator"
    this.__proto = ["EmptyIterator", "ListIterator", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.hasNext_k_ = EmptyIterator_hasNext_k_
    this.next_k_ = EmptyIterator_next_k_
    this.hasPrevious_k_ = EmptyIterator_hasPrevious_k_
    this.previous_k_ = EmptyIterator_previous_k_
    this.nextIndex_k_ = EmptyIterator_nextIndex_k_
    this.previousIndex_k_ = EmptyIterator_previousIndex_k_
    return this
end function

function EmptyIterator_getInstance() as Object
    if m.EmptyIterator_instance = invalid then
        m.EmptyIterator_instance = EmptyIterator_create_k_()
    end if
    return m.EmptyIterator_instance
end function

function EmptyIterator_hasNext_k_() as Boolean
    return false
end function

sub EmptyIterator_next_k_()
    throw NoSuchElementException_create_k_()
end sub

function EmptyIterator_hasPrevious_k_() as Boolean
    return false
end function

sub EmptyIterator_previous_k_()
    throw NoSuchElementException_create_k_()
end sub

function EmptyIterator_nextIndex_k_() as Integer
    return 0
end function

function EmptyIterator_previousIndex_k_() as Integer
    return -1
end function
