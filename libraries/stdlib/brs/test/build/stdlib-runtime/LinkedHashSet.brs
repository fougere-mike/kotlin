function LinkedHashSet_create_LinkedHashSetAnyN_k_() as Object
    this = {}
    this.__type = "LinkedHashSet"
    this.__proto = ["LinkedHashSet"]
    this.isEmpty_Z_k_ = LinkedHashSet_isEmpty_Z_k_
    this.contains_AnyN_Z_k_ = LinkedHashSet_contains_AnyN_Z_k_
    this.containsAll_CollectionAnyN_Z_k_ = LinkedHashSet_containsAll_CollectionAnyN_Z_k_
    this.iterator_MutableIteratorAnyN_k_ = LinkedHashSet_iterator_MutableIteratorAnyN_k_
    this.add_AnyN_Z_k_ = LinkedHashSet_add_AnyN_Z_k_
    this.remove_AnyN_Z_k_ = LinkedHashSet_remove_AnyN_Z_k_
    this.addAll_CollectionAnyN_Z_k_ = LinkedHashSet_addAll_CollectionAnyN_Z_k_
    this.removeAll_CollectionAnyN_Z_k_ = LinkedHashSet_removeAll_CollectionAnyN_Z_k_
    this.retainAll_CollectionAnyN_Z_k_ = LinkedHashSet_retainAll_CollectionAnyN_Z_k_
    this.clear = LinkedHashSet_clear
    this.equals_AnyN_Z_k_ = LinkedHashSet_equals_AnyN_Z_k_
    this.hashCode_I_k_ = LinkedHashSet_hashCode_I_k_
    this.toString_Str_k_ = LinkedHashSet_toString_Str_k_
    this.get_map = LinkedHashSet_get_map_LinkedHashMapAnyNZ_k_
    this.get_size = LinkedHashSet_get_size_I_k_
    m.map = LinkedHashMap_create_LinkedHashMapAnyNAnyN_k_()
    return this
end function

function LinkedHashSet_create_I_LinkedHashSetAnyN_k_(initialCapacity as Integer) as Object
    this = {}
    this.__type = "LinkedHashSet"
    this.__proto = ["LinkedHashSet"]
    this.isEmpty_Z_k_ = LinkedHashSet_isEmpty_Z_k_
    this.contains_AnyN_Z_k_ = LinkedHashSet_contains_AnyN_Z_k_
    this.containsAll_CollectionAnyN_Z_k_ = LinkedHashSet_containsAll_CollectionAnyN_Z_k_
    this.iterator_MutableIteratorAnyN_k_ = LinkedHashSet_iterator_MutableIteratorAnyN_k_
    this.add_AnyN_Z_k_ = LinkedHashSet_add_AnyN_Z_k_
    this.remove_AnyN_Z_k_ = LinkedHashSet_remove_AnyN_Z_k_
    this.addAll_CollectionAnyN_Z_k_ = LinkedHashSet_addAll_CollectionAnyN_Z_k_
    this.removeAll_CollectionAnyN_Z_k_ = LinkedHashSet_removeAll_CollectionAnyN_Z_k_
    this.retainAll_CollectionAnyN_Z_k_ = LinkedHashSet_retainAll_CollectionAnyN_Z_k_
    this.clear = LinkedHashSet_clear
    this.equals_AnyN_Z_k_ = LinkedHashSet_equals_AnyN_Z_k_
    this.hashCode_I_k_ = LinkedHashSet_hashCode_I_k_
    this.toString_Str_k_ = LinkedHashSet_toString_Str_k_
    this.get_map = LinkedHashSet_get_map_LinkedHashMapAnyNZ_k_
    this.get_size = LinkedHashSet_get_size_I_k_
    m.map = LinkedHashMap_create_I_LinkedHashMapAnyNAnyN_k_(initialCapacity)
    return this
end function

function LinkedHashSet_create_I_F_LinkedHashSetAnyN_k_(initialCapacity as Integer, loadFactor as Float) as Object
    this = {}
    this.__type = "LinkedHashSet"
    this.__proto = ["LinkedHashSet"]
    this.isEmpty_Z_k_ = LinkedHashSet_isEmpty_Z_k_
    this.contains_AnyN_Z_k_ = LinkedHashSet_contains_AnyN_Z_k_
    this.containsAll_CollectionAnyN_Z_k_ = LinkedHashSet_containsAll_CollectionAnyN_Z_k_
    this.iterator_MutableIteratorAnyN_k_ = LinkedHashSet_iterator_MutableIteratorAnyN_k_
    this.add_AnyN_Z_k_ = LinkedHashSet_add_AnyN_Z_k_
    this.remove_AnyN_Z_k_ = LinkedHashSet_remove_AnyN_Z_k_
    this.addAll_CollectionAnyN_Z_k_ = LinkedHashSet_addAll_CollectionAnyN_Z_k_
    this.removeAll_CollectionAnyN_Z_k_ = LinkedHashSet_removeAll_CollectionAnyN_Z_k_
    this.retainAll_CollectionAnyN_Z_k_ = LinkedHashSet_retainAll_CollectionAnyN_Z_k_
    this.clear = LinkedHashSet_clear
    this.equals_AnyN_Z_k_ = LinkedHashSet_equals_AnyN_Z_k_
    this.hashCode_I_k_ = LinkedHashSet_hashCode_I_k_
    this.toString_Str_k_ = LinkedHashSet_toString_Str_k_
    this.get_map = LinkedHashSet_get_map_LinkedHashMapAnyNZ_k_
    this.get_size = LinkedHashSet_get_size_I_k_
    m.map = LinkedHashMap_create_I_F_LinkedHashMapAnyNAnyN_k_(initialCapacity, loadFactor)
    return this
end function

function LinkedHashSet_create_CollectionAnyN_LinkedHashSetAnyN_k_(elements as Object) as Object
    this = {}
    this.__type = "LinkedHashSet"
    this.__proto = ["LinkedHashSet"]
    this.isEmpty_Z_k_ = LinkedHashSet_isEmpty_Z_k_
    this.contains_AnyN_Z_k_ = LinkedHashSet_contains_AnyN_Z_k_
    this.containsAll_CollectionAnyN_Z_k_ = LinkedHashSet_containsAll_CollectionAnyN_Z_k_
    this.iterator_MutableIteratorAnyN_k_ = LinkedHashSet_iterator_MutableIteratorAnyN_k_
    this.add_AnyN_Z_k_ = LinkedHashSet_add_AnyN_Z_k_
    this.remove_AnyN_Z_k_ = LinkedHashSet_remove_AnyN_Z_k_
    this.addAll_CollectionAnyN_Z_k_ = LinkedHashSet_addAll_CollectionAnyN_Z_k_
    this.removeAll_CollectionAnyN_Z_k_ = LinkedHashSet_removeAll_CollectionAnyN_Z_k_
    this.retainAll_CollectionAnyN_Z_k_ = LinkedHashSet_retainAll_CollectionAnyN_Z_k_
    this.clear = LinkedHashSet_clear
    this.equals_AnyN_Z_k_ = LinkedHashSet_equals_AnyN_Z_k_
    this.hashCode_I_k_ = LinkedHashSet_hashCode_I_k_
    this.toString_Str_k_ = LinkedHashSet_toString_Str_k_
    this.get_map = LinkedHashSet_get_map_LinkedHashMapAnyNZ_k_
    this.get_size = LinkedHashSet_get_size_I_k_
    m.map = LinkedHashMap_create_I_LinkedHashMapAnyNAnyN_k_(elements.size)
    m.addAll(elements)
    return this
end function

function LinkedHashSet_isEmpty_Z_k_() as Boolean
    return m.map.isEmpty()
end function

function LinkedHashSet_contains_AnyN_Z_k_(element as Dynamic) as Boolean
    return m.map.containsKey(element)
end function

function LinkedHashSet_containsAll_CollectionAnyN_Z_k_(elements as Object) as Boolean
    for each element in elements
        if m.contains(element).not() then
            return false
        end if
    end for
    return true
end function

function LinkedHashSet_iterator_MutableIteratorAnyN_k_() as Object
    return LinkedHashSet_SetIterator_create_MutableIteratorAnyN_SetIteratorAnyN_k_(m.map.keys.iterator())
end function

function LinkedHashSet_add_AnyN_Z_k_(element as Dynamic) as Boolean
    if m.contains(element) then
        return false
    end if
    m.map.put(element, true)
    return true
end function

function LinkedHashSet_remove_AnyN_Z_k_(element as Dynamic) as Boolean
    if m.contains(element).not() then
        return false
    end if
    m.map.remove(element)
    return true
end function

function LinkedHashSet_addAll_CollectionAnyN_Z_k_(elements as Object) as Boolean
    modified = false
    for each element in elements
        if m.add(element) then
            modified = true
        end if
    end for
    return modified
end function

function LinkedHashSet_removeAll_CollectionAnyN_Z_k_(elements as Object) as Boolean
    modified = false
    for each element in elements
        if m.remove(element) then
            modified = true
        end if
    end for
    return modified
end function

function LinkedHashSet_retainAll_CollectionAnyN_Z_k_(elements as Object) as Boolean
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

sub LinkedHashSet_clear()
    m.map.clear()
end sub

function LinkedHashSet_equals_AnyN_Z_k_(other as Dynamic) as Boolean
    if EQEQEQ_AnyN_AnyN_Z_k_(other, m) then
        return true
    end if
    if not __kotlin_isInstanceOf(other, "Set") then
        return false
    end if
    if other.size <> m.size then
        return false
    end if
    return all_rIterableAnyN_Function1AnyNZ_Z_k_(other, {this: this, invoke: function(it as Dynamic) as Boolean
        return contains_rIterableAnyN_AnyN_Z_k_(m.this, it)
    end function})
end function

function LinkedHashSet_hashCode_I_k_() as Integer
    result = 0
    for each element in m
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

    end for
    return result
end function

function LinkedHashSet_toString_Str_k_() as String
    return joinToString_rIterableAnyN_CharSequence_CharSequence_CharSequence_I_CharSequence_Str_k_(m, ", ", "[", "]")
end function

function LinkedHashSet_get_map_LinkedHashMapAnyNZ_k_() as Object
    return m.map
end function

function LinkedHashSet_get_size_I_k_() as Integer
    return m.map.size
end function

function LinkedHashSet_SetIterator_create_MutableIteratorAnyN_SetIteratorAnyN_k_(keyIterator as Object) as Object
    this = {}
    this.__type = "LinkedHashSet_SetIterator"
    this.__proto = ["LinkedHashSet_SetIterator"]
    this.keyIterator = keyIterator
    this.hasNext_Z_k_ = LinkedHashSet_SetIterator_hasNext_Z_k_
    this.next_AnyN_k_ = LinkedHashSet_SetIterator_next_AnyN_k_
    this.remove = LinkedHashSet_SetIterator_remove
    this.get_keyIterator = LinkedHashSet_SetIterator_get_keyIterator_MutableIteratorAnyN_k_
    return this
end function

function LinkedHashSet_SetIterator_hasNext_Z_k_() as Boolean
    return m.keyIterator.hasNext()
end function

function LinkedHashSet_SetIterator_next_AnyN_k_() as Dynamic
    return m.keyIterator.next()
end function

sub LinkedHashSet_SetIterator_remove()
    return
end sub

function LinkedHashSet_SetIterator_get_keyIterator_MutableIteratorAnyN_k_() as Object
    return m.keyIterator
end function
