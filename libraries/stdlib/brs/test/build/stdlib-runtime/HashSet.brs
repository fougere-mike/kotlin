function HashSet_create_HashSetAnyN_k_() as Object
    this = {}
    this.__type = "HashSet"
    this.__proto = ["HashSet", "MutableSet", "Set", "Collection", "Iterable", "MutableCollection", "MutableIterable"]
    this.__id = __kotlin_nextObjectId()
    this.isEmpty_Z_k_ = HashSet_isEmpty_Z_k_
    this.contains_AnyN_Z_k_ = HashSet_contains_AnyN_Z_k_
    this.containsAll_CollectionAnyN_Z_k_ = HashSet_containsAll_CollectionAnyN_Z_k_
    this.iterator_MutableIteratorAnyN_k_ = HashSet_iterator_MutableIteratorAnyN_k_
    this.add_AnyN_Z_k_ = HashSet_add_AnyN_Z_k_
    this.remove_AnyN_Z_k_ = HashSet_remove_AnyN_Z_k_
    this.addAll_CollectionAnyN_Z_k_ = HashSet_addAll_CollectionAnyN_Z_k_
    this.removeAll_CollectionAnyN_Z_k_ = HashSet_removeAll_CollectionAnyN_Z_k_
    this.retainAll_CollectionAnyN_Z_k_ = HashSet_retainAll_CollectionAnyN_Z_k_
    this.clear = HashSet_clear
    this.equals_AnyN_Z_k_ = HashSet_equals_AnyN_Z_k_
    this.equals = HashSet_equals_AnyN_Z_k_
    this.hashCode_I_k_ = HashSet_hashCode_I_k_
    this.hashCode = HashSet_hashCode_I_k_
    this.toString_Str_k_ = HashSet_toString_Str_k_
    this.toString = HashSet_toString_Str_k_
    this.get_array = HashSet_get_array_Dynamic_k_
    this.get_map = HashSet_get_map_HashMapAnyNZ_k_
    this.get_size = HashSet_get_size_I_k_
    this.map = HashMap_create_HashMapAnyNAnyN_k_()
    return this
end function

function HashSet_create_I_HashSetAnyN_k_(initialCapacity as Integer) as Object
    this = {}
    this.__type = "HashSet"
    this.__proto = ["HashSet", "MutableSet", "Set", "Collection", "Iterable", "MutableCollection", "MutableIterable"]
    this.__id = __kotlin_nextObjectId()
    this.isEmpty_Z_k_ = HashSet_isEmpty_Z_k_
    this.contains_AnyN_Z_k_ = HashSet_contains_AnyN_Z_k_
    this.containsAll_CollectionAnyN_Z_k_ = HashSet_containsAll_CollectionAnyN_Z_k_
    this.iterator_MutableIteratorAnyN_k_ = HashSet_iterator_MutableIteratorAnyN_k_
    this.add_AnyN_Z_k_ = HashSet_add_AnyN_Z_k_
    this.remove_AnyN_Z_k_ = HashSet_remove_AnyN_Z_k_
    this.addAll_CollectionAnyN_Z_k_ = HashSet_addAll_CollectionAnyN_Z_k_
    this.removeAll_CollectionAnyN_Z_k_ = HashSet_removeAll_CollectionAnyN_Z_k_
    this.retainAll_CollectionAnyN_Z_k_ = HashSet_retainAll_CollectionAnyN_Z_k_
    this.clear = HashSet_clear
    this.equals_AnyN_Z_k_ = HashSet_equals_AnyN_Z_k_
    this.equals = HashSet_equals_AnyN_Z_k_
    this.hashCode_I_k_ = HashSet_hashCode_I_k_
    this.hashCode = HashSet_hashCode_I_k_
    this.toString_Str_k_ = HashSet_toString_Str_k_
    this.toString = HashSet_toString_Str_k_
    this.get_array = HashSet_get_array_Dynamic_k_
    this.get_map = HashSet_get_map_HashMapAnyNZ_k_
    this.get_size = HashSet_get_size_I_k_
    this.map = HashMap_create_I_HashMapAnyNAnyN_k_(initialCapacity)
    return this
end function

function HashSet_create_CollectionAnyN_HashSetAnyN_k_(elements as Object) as Object
    this = {}
    this.__type = "HashSet"
    this.__proto = ["HashSet", "MutableSet", "Set", "Collection", "Iterable", "MutableCollection", "MutableIterable"]
    this.__id = __kotlin_nextObjectId()
    this.isEmpty_Z_k_ = HashSet_isEmpty_Z_k_
    this.contains_AnyN_Z_k_ = HashSet_contains_AnyN_Z_k_
    this.containsAll_CollectionAnyN_Z_k_ = HashSet_containsAll_CollectionAnyN_Z_k_
    this.iterator_MutableIteratorAnyN_k_ = HashSet_iterator_MutableIteratorAnyN_k_
    this.add_AnyN_Z_k_ = HashSet_add_AnyN_Z_k_
    this.remove_AnyN_Z_k_ = HashSet_remove_AnyN_Z_k_
    this.addAll_CollectionAnyN_Z_k_ = HashSet_addAll_CollectionAnyN_Z_k_
    this.removeAll_CollectionAnyN_Z_k_ = HashSet_removeAll_CollectionAnyN_Z_k_
    this.retainAll_CollectionAnyN_Z_k_ = HashSet_retainAll_CollectionAnyN_Z_k_
    this.clear = HashSet_clear
    this.equals_AnyN_Z_k_ = HashSet_equals_AnyN_Z_k_
    this.equals = HashSet_equals_AnyN_Z_k_
    this.hashCode_I_k_ = HashSet_hashCode_I_k_
    this.hashCode = HashSet_hashCode_I_k_
    this.toString_Str_k_ = HashSet_toString_Str_k_
    this.toString = HashSet_toString_Str_k_
    this.get_array = HashSet_get_array_Dynamic_k_
    this.get_map = HashSet_get_map_HashMapAnyNZ_k_
    this.get_size = HashSet_get_size_I_k_
    this.map = HashMap_create_I_HashMapAnyNAnyN_k_(elements.get_size())
    this.addAll_CollectionAnyN_Z_k_(elements)
    return this
end function

function HashSet_isEmpty_Z_k_() as Boolean
    return m.get_map().isEmpty_Z_k_()
end function

function HashSet_contains_AnyN_Z_k_(element as Dynamic) as Boolean
    return m.get_map().containsKey_AnyN_Z_k_(element)
end function

function HashSet_containsAll_CollectionAnyN_Z_k_(elements as Object) as Boolean
    for each element in elements
        if not m.contains_AnyN_Z_k_(element) then
            return false
        end if
    end for
    return true
end function

function HashSet_iterator_MutableIteratorAnyN_k_() as Object
    return HashSet_SetIterator_create_MutableIteratorAnyN_SetIteratorAnyN_k_(m.get_map().get_keys().iterator_MutableIteratorAnyN_k_())
end function

function HashSet_add_AnyN_Z_k_(element as Dynamic) as Boolean
    if m.contains_AnyN_Z_k_(element) then
        return false
    end if
    m.get_map().put_AnyN_AnyN_AnyN_k_(element, true)
    return true
end function

function HashSet_remove_AnyN_Z_k_(element as Dynamic) as Boolean
    if not m.contains_AnyN_Z_k_(element) then
        return false
    end if
    m.get_map().remove_AnyN_AnyN_k_(element)
    return true
end function

function HashSet_addAll_CollectionAnyN_Z_k_(elements as Object) as Boolean
    modified = false
    for each element in elements
        if m.add_AnyN_Z_k_(element) then
            modified = true
        end if
    end for
    return modified
end function

function HashSet_removeAll_CollectionAnyN_Z_k_(elements as Object) as Boolean
    modified = false
    for each element in elements
        if m.remove_AnyN_Z_k_(element) then
            modified = true
        end if
    end for
    return modified
end function

function HashSet_retainAll_CollectionAnyN_Z_k_(elements as Object) as Boolean
    modified = false
    iter = m.iterator_MutableIteratorAnyN_k_()
    while iter.hasNext_Z_k_()
        if not elements.contains_AnyN_Z_k_(iter.next_AnyN_k_()) then
            iter.remove()
            modified = true
        end if
    end while
    return modified
end function

sub HashSet_clear()
    m.get_map().clear()
end sub

function HashSet_equals_AnyN_Z_k_(other as Dynamic) as Boolean
    if __kotlin_identityEquals(other, m) then
        return true
    end if
    if not __kotlin_isInstanceOf(other, "Set") then
        return false
    end if
    if other.get_size() <> m.get_size() then
        return false
    end if
    for each element in m
        if not other.contains_AnyN_Z_k_(element) then
            return false
        end if
    end for
    return true
end function

function HashSet_hashCode_I_k_() as Integer
    h = 0
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
        h = (h + __when_tmp1)

    end for
    return h
end function

function HashSet_toString_Str_k_() as String
    if m.isEmpty_Z_k_() then
        return "[]"
    end if
    sb = StringBuilder_create_StringBuilder_k_()
    sb.append_StrN_StringBuilder_k_("[")
    first = true
    for each element in m
        if not first then
            sb.append_StrN_StringBuilder_k_(", ")
        end if
        first = false
        if __kotlin_identityEquals(element, m) then
            sb.append_StrN_StringBuilder_k_("(this Collection)")
        else if true then
            sb.append_StrN_StringBuilder_k_(toString_AnyN_Str_k_(element))
        end if

    end for
    sb.append_StrN_StringBuilder_k_("]")
    return sb.toString()
end function

function HashSet_get_array_Dynamic_k_() as Object
    result = CreateObject("roArray", 0, true)
    iter = m.get_map().get_keys().iterator_MutableIteratorAnyN_k_()
    while iter.hasNext_Z_k_()
        result.Push(iter.next_AnyN_k_())
    end while
    return result
end function

function HashSet_get_map_HashMapAnyNZ_k_() as Object
    return m.map
end function

function HashSet_get_size_I_k_() as Integer
    return m.get_map().get_size()
end function

function HashSet_SetIterator_create_MutableIteratorAnyN_SetIteratorAnyN_k_(keyIterator as Object) as Object
    this = {}
    this.__type = "HashSet_SetIterator"
    this.__proto = ["HashSet_SetIterator", "MutableIterator", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.hasNext_Z_k_ = HashSet_SetIterator_hasNext_Z_k_
    this.next_AnyN_k_ = HashSet_SetIterator_next_AnyN_k_
    this.remove = HashSet_SetIterator_remove
    this.get_keyIterator = HashSet_SetIterator_get_keyIterator_MutableIteratorAnyN_k_
    this.keyIterator = keyIterator
    return this
end function

function HashSet_SetIterator_hasNext_Z_k_() as Boolean
    return m.get_keyIterator().hasNext_Z_k_()
end function

function HashSet_SetIterator_next_AnyN_k_() as Dynamic
    return m.get_keyIterator().next_AnyN_k_()
end function

sub HashSet_SetIterator_remove()
    m.get_keyIterator().remove()
end sub

function HashSet_SetIterator_get_keyIterator_MutableIteratorAnyN_k_() as Object
    return m.keyIterator
end function

function hashSetOf_HashSetAnyN_k_() as Object
    return HashSet_create_HashSetAnyN_k_()
end function

function hashSetOf_Arr_HashSetAnyN_k_(elements as Object) as Object
    set = HashSet_create_I_HashSetAnyN_k_(elements.count())
    indexedObject = elements
    inductionVariable = 0
    last = indexedObject.count()
    while inductionVariable < last
        element = indexedObject[inductionVariable]
        inductionVariable = (inductionVariable + 1)

        set.add_AnyN_Z_k_(element)

    end while

    return set
end function

function mutableSetOf_MutableSetAnyN_k_() as Object
    return HashSet_create_HashSetAnyN_k_()
end function

function mutableSetOf_Arr_MutableSetAnyN_k_(elements as Object) as Object
    return hashSetOf_Arr_HashSetAnyN_k_(elements)
end function

function setOf_SetAnyN_k_() as Object
    return emptySet_SetAnyN_k_()
end function

function setOf_Arr_SetAnyN_k_(elements as Object) as Object
    __when_tmp2 = invalid
    if elements.count() = 0 then
        __when_tmp2 = emptySet_SetAnyN_k_()
    else if true then
        __when_tmp2 = hashSetOf_Arr_HashSetAnyN_k_(elements)
    end if
    return __when_tmp2

end function

function emptySet_SetAnyN_k_() as Object
    return EmptyHashSet_getInstance()
end function

function EmptyHashSet_create_EmptyHashSet_k_() as Object
    this = {}
    this.__type = "EmptyHashSet"
    this.__proto = ["EmptyHashSet", "Set", "Collection", "Iterable"]
    this.__id = __kotlin_nextObjectId()
    this.isEmpty_Z_k_ = EmptyHashSet_isEmpty_Z_k_
    this.contains_AnyN_Z_k_ = EmptyHashSet_contains_AnyN_Z_k_
    this.containsAll_CollectionAnyN_Z_k_ = EmptyHashSet_containsAll_CollectionAnyN_Z_k_
    this.iterator_IteratorAnyN_k_ = EmptyHashSet_iterator_IteratorAnyN_k_
    this.equals_AnyN_Z_k_ = EmptyHashSet_equals_AnyN_Z_k_
    this.equals = EmptyHashSet_equals_AnyN_Z_k_
    this.hashCode_I_k_ = EmptyHashSet_hashCode_I_k_
    this.hashCode = EmptyHashSet_hashCode_I_k_
    this.toString_Str_k_ = EmptyHashSet_toString_Str_k_
    this.toString = EmptyHashSet_toString_Str_k_
    this.get_array = EmptyHashSet_get_array_Dynamic_k_
    this.get_size = EmptyHashSet_get_size_I_k_
    return this
end function

function EmptyHashSet_getInstance() as Object
    if m.EmptyHashSet_instance = invalid then
        m.EmptyHashSet_instance = EmptyHashSet_create_EmptyHashSet_k_()
    end if
    return m.EmptyHashSet_instance
end function

function EmptyHashSet_isEmpty_Z_k_() as Boolean
    return true
end function

function EmptyHashSet_contains_AnyN_Z_k_(element as Dynamic) as Boolean
    return false
end function

function EmptyHashSet_containsAll_CollectionAnyN_Z_k_(elements as Object) as Boolean
    return elements.isEmpty_Z_k_()
end function

function EmptyHashSet_iterator_IteratorAnyN_k_() as Object
    return EmptySetIterator_getInstance()
end function

function EmptyHashSet_equals_AnyN_Z_k_(other as Dynamic) as Boolean
    return __kotlin_isInstanceOf(other, "Set") and other.isEmpty_Z_k_()
end function

function EmptyHashSet_hashCode_I_k_() as Integer
    return 0
end function

function EmptyHashSet_toString_Str_k_() as String
    return "[]"
end function

function EmptyHashSet_get_array_Dynamic_k_() as Object
    return CreateObject("roArray", 0, true)
end function

function EmptyHashSet_get_size_I_k_() as Integer
    return 0
end function

function EmptySetIterator_create_EmptySetIterator_k_() as Object
    this = {}
    this.__type = "EmptySetIterator"
    this.__proto = ["EmptySetIterator", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.hasNext_Z_k_ = EmptySetIterator_hasNext_Z_k_
    this.next_AnyN_k_ = EmptySetIterator_next_AnyN_k_
    return this
end function

function EmptySetIterator_getInstance() as Object
    if m.EmptySetIterator_instance = invalid then
        m.EmptySetIterator_instance = EmptySetIterator_create_EmptySetIterator_k_()
    end if
    return m.EmptySetIterator_instance
end function

function EmptySetIterator_hasNext_Z_k_() as Boolean
    return false
end function

function EmptySetIterator_next_AnyN_k_() as Dynamic
    throw NoSuchElementException_create_NoSuchElementException_k_()
end function
