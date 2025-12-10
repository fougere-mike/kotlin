function LinkedHashSet_create_k_() as Object
    this = {}
    this.__type = "LinkedHashSet"
    this.__proto = ["LinkedHashSet", "MutableSet", "Set", "Collection", "Iterable", "MutableCollection", "MutableIterable"]
    this.__id = __kotlin_nextObjectId()
    this.isEmpty_k_ = LinkedHashSet_isEmpty_k_
    this.contains_AnyN_k_ = LinkedHashSet_contains_AnyN_k_
    this.containsAll_Collection_k_ = LinkedHashSet_containsAll_Collection_k_
    this.iterator_k_ = LinkedHashSet_iterator_k_
    this.add_AnyN_k_ = LinkedHashSet_add_AnyN_k_
    this.remove_AnyN_k_ = LinkedHashSet_remove_AnyN_k_
    this.addAll_Collection_k_ = LinkedHashSet_addAll_Collection_k_
    this.removeAll_Collection_k_ = LinkedHashSet_removeAll_Collection_k_
    this.retainAll_Collection_k_ = LinkedHashSet_retainAll_Collection_k_
    this.clear_k_ = LinkedHashSet_clear_k_
    this.equals_AnyN_k_ = LinkedHashSet_equals_AnyN_k_
    this.equals = LinkedHashSet_equals_AnyN_k_
    this.hashCode_k_ = LinkedHashSet_hashCode_k_
    this.hashCode = LinkedHashSet_hashCode_k_
    this.toString_k_ = LinkedHashSet_toString_k_
    this.toString = LinkedHashSet_toString_k_
    this.get_map = LinkedHashSet_get_map_k_
    this.get_size = LinkedHashSet_get_size_k_
    this.map = LinkedHashMap_create_k_()
    return this
end function

function LinkedHashSet_create_I_k_(initialCapacity as Integer) as Object
    this = {}
    this.__type = "LinkedHashSet"
    this.__proto = ["LinkedHashSet", "MutableSet", "Set", "Collection", "Iterable", "MutableCollection", "MutableIterable"]
    this.__id = __kotlin_nextObjectId()
    this.isEmpty_k_ = LinkedHashSet_isEmpty_k_
    this.contains_AnyN_k_ = LinkedHashSet_contains_AnyN_k_
    this.containsAll_Collection_k_ = LinkedHashSet_containsAll_Collection_k_
    this.iterator_k_ = LinkedHashSet_iterator_k_
    this.add_AnyN_k_ = LinkedHashSet_add_AnyN_k_
    this.remove_AnyN_k_ = LinkedHashSet_remove_AnyN_k_
    this.addAll_Collection_k_ = LinkedHashSet_addAll_Collection_k_
    this.removeAll_Collection_k_ = LinkedHashSet_removeAll_Collection_k_
    this.retainAll_Collection_k_ = LinkedHashSet_retainAll_Collection_k_
    this.clear_k_ = LinkedHashSet_clear_k_
    this.equals_AnyN_k_ = LinkedHashSet_equals_AnyN_k_
    this.equals = LinkedHashSet_equals_AnyN_k_
    this.hashCode_k_ = LinkedHashSet_hashCode_k_
    this.hashCode = LinkedHashSet_hashCode_k_
    this.toString_k_ = LinkedHashSet_toString_k_
    this.toString = LinkedHashSet_toString_k_
    this.get_map = LinkedHashSet_get_map_k_
    this.get_size = LinkedHashSet_get_size_k_
    this.map = LinkedHashMap_create_I_k_(initialCapacity)
    return this
end function

function LinkedHashSet_create_I_F_k_(initialCapacity as Integer, loadFactor as Float) as Object
    this = {}
    this.__type = "LinkedHashSet"
    this.__proto = ["LinkedHashSet", "MutableSet", "Set", "Collection", "Iterable", "MutableCollection", "MutableIterable"]
    this.__id = __kotlin_nextObjectId()
    this.isEmpty_k_ = LinkedHashSet_isEmpty_k_
    this.contains_AnyN_k_ = LinkedHashSet_contains_AnyN_k_
    this.containsAll_Collection_k_ = LinkedHashSet_containsAll_Collection_k_
    this.iterator_k_ = LinkedHashSet_iterator_k_
    this.add_AnyN_k_ = LinkedHashSet_add_AnyN_k_
    this.remove_AnyN_k_ = LinkedHashSet_remove_AnyN_k_
    this.addAll_Collection_k_ = LinkedHashSet_addAll_Collection_k_
    this.removeAll_Collection_k_ = LinkedHashSet_removeAll_Collection_k_
    this.retainAll_Collection_k_ = LinkedHashSet_retainAll_Collection_k_
    this.clear_k_ = LinkedHashSet_clear_k_
    this.equals_AnyN_k_ = LinkedHashSet_equals_AnyN_k_
    this.equals = LinkedHashSet_equals_AnyN_k_
    this.hashCode_k_ = LinkedHashSet_hashCode_k_
    this.hashCode = LinkedHashSet_hashCode_k_
    this.toString_k_ = LinkedHashSet_toString_k_
    this.toString = LinkedHashSet_toString_k_
    this.get_map = LinkedHashSet_get_map_k_
    this.get_size = LinkedHashSet_get_size_k_
    this.map = LinkedHashMap_create_I_F_k_(initialCapacity, loadFactor)
    return this
end function

function LinkedHashSet_create_Collection_k_(elements as Object) as Object
    this = {}
    this.__type = "LinkedHashSet"
    this.__proto = ["LinkedHashSet", "MutableSet", "Set", "Collection", "Iterable", "MutableCollection", "MutableIterable"]
    this.__id = __kotlin_nextObjectId()
    this.isEmpty_k_ = LinkedHashSet_isEmpty_k_
    this.contains_AnyN_k_ = LinkedHashSet_contains_AnyN_k_
    this.containsAll_Collection_k_ = LinkedHashSet_containsAll_Collection_k_
    this.iterator_k_ = LinkedHashSet_iterator_k_
    this.add_AnyN_k_ = LinkedHashSet_add_AnyN_k_
    this.remove_AnyN_k_ = LinkedHashSet_remove_AnyN_k_
    this.addAll_Collection_k_ = LinkedHashSet_addAll_Collection_k_
    this.removeAll_Collection_k_ = LinkedHashSet_removeAll_Collection_k_
    this.retainAll_Collection_k_ = LinkedHashSet_retainAll_Collection_k_
    this.clear_k_ = LinkedHashSet_clear_k_
    this.equals_AnyN_k_ = LinkedHashSet_equals_AnyN_k_
    this.equals = LinkedHashSet_equals_AnyN_k_
    this.hashCode_k_ = LinkedHashSet_hashCode_k_
    this.hashCode = LinkedHashSet_hashCode_k_
    this.toString_k_ = LinkedHashSet_toString_k_
    this.toString = LinkedHashSet_toString_k_
    this.get_map = LinkedHashSet_get_map_k_
    this.get_size = LinkedHashSet_get_size_k_
    this.map = LinkedHashMap_create_I_k_(elements.get_size())
    this.addAll_Collection_k_(elements)
    return this
end function

function LinkedHashSet_isEmpty_k_() as Boolean
    return m.get_map().isEmpty_k_()
end function

function LinkedHashSet_contains_AnyN_k_(element as Dynamic) as Boolean
    return m.get_map().containsKey_AnyN_k_(element)
end function

function LinkedHashSet_containsAll_Collection_k_(elements as Object) as Boolean
    __iter_122 = elements.iterator_k_()
    while __iter_122.hasNext_k_()
        element = __iter_122.next_k_()
        if not m.contains_AnyN_k_(element) then
            return false
        end if
    end while

    return true
end function

function LinkedHashSet_iterator_k_() as Object
    return LinkedHashSet_SetIterator_create_MutableIterator_k_(m.get_map().get_keys().iterator_k_())
end function

function LinkedHashSet_add_AnyN_k_(element as Dynamic) as Boolean
    if m.contains_AnyN_k_(element) then
        return false
    end if
    m.get_map().put_AnyN_AnyN_k_(element, true)
    return true
end function

function LinkedHashSet_remove_AnyN_k_(element as Dynamic) as Boolean
    if not m.contains_AnyN_k_(element) then
        return false
    end if
    m.get_map().remove_AnyN_k_(element)
    return true
end function

function LinkedHashSet_addAll_Collection_k_(elements as Object) as Boolean
    modified = false
    __iter_123 = elements.iterator_k_()
    while __iter_123.hasNext_k_()
        element = __iter_123.next_k_()
        if m.add_AnyN_k_(element) then
            modified = true
        end if
    end while

    return modified
end function

function LinkedHashSet_removeAll_Collection_k_(elements as Object) as Boolean
    modified = false
    __iter_124 = elements.iterator_k_()
    while __iter_124.hasNext_k_()
        element = __iter_124.next_k_()
        if m.remove_AnyN_k_(element) then
            modified = true
        end if
    end while

    return modified
end function

function LinkedHashSet_retainAll_Collection_k_(elements as Object) as Boolean
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

sub LinkedHashSet_clear_k_()
    m.get_map().clear_k_()
end sub

function LinkedHashSet_equals_AnyN_k_(other as Dynamic) as Boolean
    if __kotlin_identityEquals(other, m) then
        return true
    end if
    if not __kotlin_isInstanceOf(other, "Set") then
        return false
    end if
    if other.get_size() <> m.get_size() then
        return false
    end if
    return all_rIterable_Function1Z_k_(other, {this: m, invoke: function(it as Dynamic) as Boolean
        return contains_rIterable_AnyN_k_(m.this, it)
    end function})
end function

function LinkedHashSet_hashCode_k_() as Integer
    result = 0
    __iter_125 = m.iterator_k_()
    while __iter_125.hasNext_k_()
        element = __iter_125.next_k_()
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
        result = (result + __when_tmp1)

    end while

    return result
end function

function LinkedHashSet_toString_k_() as String
    return joinToString_rIterable_CharSequence_CharSequence_CharSequence_I_CharSequence_k_(m, ", ", "[", "]")
end function

function LinkedHashSet_get_map_k_() as Object
    return m.map
end function

function LinkedHashSet_get_size_k_() as Integer
    return m.get_map().get_size()
end function

function LinkedHashSet_SetIterator_create_MutableIterator_k_(keyIterator as Object) as Object
    this = {}
    this.__type = "LinkedHashSet_SetIterator"
    this.__proto = ["LinkedHashSet_SetIterator", "MutableIterator", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.hasNext_k_ = LinkedHashSet_SetIterator_hasNext_k_
    this.next_k_ = LinkedHashSet_SetIterator_next_k_
    this.remove_k_ = LinkedHashSet_SetIterator_remove_k_
    this.get_keyIterator = LinkedHashSet_SetIterator_get_keyIterator_k_
    this.keyIterator = keyIterator
    return this
end function

function LinkedHashSet_SetIterator_hasNext_k_() as Boolean
    return m.get_keyIterator().hasNext_k_()
end function

function LinkedHashSet_SetIterator_next_k_() as Dynamic
    return m.get_keyIterator().next_k_()
end function

sub LinkedHashSet_SetIterator_remove_k_()
    return
end sub

function LinkedHashSet_SetIterator_get_keyIterator_k_() as Object
    return m.keyIterator
end function
