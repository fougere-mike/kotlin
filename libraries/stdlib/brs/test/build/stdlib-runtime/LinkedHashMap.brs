function LinkedHashMap_create_k_() as Object
    this = {}
    this.__type = "LinkedHashMap"
    this.__proto = ["LinkedHashMap", "MutableMap", "Map"]
    this.__id = __kotlin_nextObjectId()
    this.isEmpty_k_ = LinkedHashMap_isEmpty_k_
    this.containsKey_AnyN_k_ = LinkedHashMap_containsKey_AnyN_k_
    this.containsValue_AnyN_k_ = LinkedHashMap_containsValue_AnyN_k_
    this.get_AnyN_k_ = LinkedHashMap_get_AnyN_k_
    this.put_AnyN_AnyN_k_ = LinkedHashMap_put_AnyN_AnyN_k_
    this.remove_AnyN_k_ = LinkedHashMap_remove_AnyN_k_
    this.putAll_Map_k_ = LinkedHashMap_putAll_Map_k_
    this.clear_k_ = LinkedHashMap_clear_k_
    this.equals_AnyN_k_ = LinkedHashMap_equals_AnyN_k_
    this.equals = LinkedHashMap_equals_AnyN_k_
    this.hashCode_k_ = LinkedHashMap_hashCode_k_
    this.hashCode = LinkedHashMap_hashCode_k_
    this.toString_k_ = LinkedHashMap_toString_k_
    this.toString = LinkedHashMap_toString_k_
    this.keyToString_AnyN_k_ = LinkedHashMap_keyToString_AnyN_k_
    this.get_map = LinkedHashMap_get_map_k_
    this.set_map = LinkedHashMap_set_map_Dynamic_k_
    this.get_keyOrder = LinkedHashMap_get_keyOrder_k_
    this.set_keyOrder = LinkedHashMap_set_keyOrder_Dynamic_k_
    this.get__size = LinkedHashMap_get__size_k_
    this.set__size = LinkedHashMap_set__size_I_k_
    this.get_size = LinkedHashMap_get_size_k_
    this.get_keys = LinkedHashMap_get_keys_k_
    this.get_values = LinkedHashMap_get_values_k_
    this.get_entries = LinkedHashMap_get_entries_k_
    this._size = 0
    this.set_map(CreateObject("roAssociativeArray"))
    this.set_keyOrder(CreateObject("roArray", 0, true))
    this.set__size(0)
    return this
end function

function LinkedHashMap_create_I_k_(initialCapacity as Integer) as Object
    this = {}
    this.__type = "LinkedHashMap"
    this.__proto = ["LinkedHashMap", "MutableMap", "Map"]
    this.__id = __kotlin_nextObjectId()
    this.isEmpty_k_ = LinkedHashMap_isEmpty_k_
    this.containsKey_AnyN_k_ = LinkedHashMap_containsKey_AnyN_k_
    this.containsValue_AnyN_k_ = LinkedHashMap_containsValue_AnyN_k_
    this.get_AnyN_k_ = LinkedHashMap_get_AnyN_k_
    this.put_AnyN_AnyN_k_ = LinkedHashMap_put_AnyN_AnyN_k_
    this.remove_AnyN_k_ = LinkedHashMap_remove_AnyN_k_
    this.putAll_Map_k_ = LinkedHashMap_putAll_Map_k_
    this.clear_k_ = LinkedHashMap_clear_k_
    this.equals_AnyN_k_ = LinkedHashMap_equals_AnyN_k_
    this.equals = LinkedHashMap_equals_AnyN_k_
    this.hashCode_k_ = LinkedHashMap_hashCode_k_
    this.hashCode = LinkedHashMap_hashCode_k_
    this.toString_k_ = LinkedHashMap_toString_k_
    this.toString = LinkedHashMap_toString_k_
    this.keyToString_AnyN_k_ = LinkedHashMap_keyToString_AnyN_k_
    this.get_map = LinkedHashMap_get_map_k_
    this.set_map = LinkedHashMap_set_map_Dynamic_k_
    this.get_keyOrder = LinkedHashMap_get_keyOrder_k_
    this.set_keyOrder = LinkedHashMap_set_keyOrder_Dynamic_k_
    this.get__size = LinkedHashMap_get__size_k_
    this.set__size = LinkedHashMap_set__size_I_k_
    this.get_size = LinkedHashMap_get_size_k_
    this.get_keys = LinkedHashMap_get_keys_k_
    this.get_values = LinkedHashMap_get_values_k_
    this.get_entries = LinkedHashMap_get_entries_k_
    this._size = 0
    require_Z_Function0Any_k_(initialCapacity >= 0, {initialCapacity: initialCapacity, invoke: function() as Object
        return "Negative initial capacity: " + __kotlin_numToStr_I_k_(m.initialCapacity)
    end function})
    this.set_map(CreateObject("roAssociativeArray"))
    this.set_keyOrder(CreateObject("roArray", 0, true))
    this.set__size(0)
    return this
end function

function LinkedHashMap_create_I_F_k_(initialCapacity as Integer, loadFactor as Float) as Object
    this = {}
    this.__type = "LinkedHashMap"
    this.__proto = ["LinkedHashMap", "MutableMap", "Map"]
    this.__id = __kotlin_nextObjectId()
    this.isEmpty_k_ = LinkedHashMap_isEmpty_k_
    this.containsKey_AnyN_k_ = LinkedHashMap_containsKey_AnyN_k_
    this.containsValue_AnyN_k_ = LinkedHashMap_containsValue_AnyN_k_
    this.get_AnyN_k_ = LinkedHashMap_get_AnyN_k_
    this.put_AnyN_AnyN_k_ = LinkedHashMap_put_AnyN_AnyN_k_
    this.remove_AnyN_k_ = LinkedHashMap_remove_AnyN_k_
    this.putAll_Map_k_ = LinkedHashMap_putAll_Map_k_
    this.clear_k_ = LinkedHashMap_clear_k_
    this.equals_AnyN_k_ = LinkedHashMap_equals_AnyN_k_
    this.equals = LinkedHashMap_equals_AnyN_k_
    this.hashCode_k_ = LinkedHashMap_hashCode_k_
    this.hashCode = LinkedHashMap_hashCode_k_
    this.toString_k_ = LinkedHashMap_toString_k_
    this.toString = LinkedHashMap_toString_k_
    this.keyToString_AnyN_k_ = LinkedHashMap_keyToString_AnyN_k_
    this.get_map = LinkedHashMap_get_map_k_
    this.set_map = LinkedHashMap_set_map_Dynamic_k_
    this.get_keyOrder = LinkedHashMap_get_keyOrder_k_
    this.set_keyOrder = LinkedHashMap_set_keyOrder_Dynamic_k_
    this.get__size = LinkedHashMap_get__size_k_
    this.set__size = LinkedHashMap_set__size_I_k_
    this.get_size = LinkedHashMap_get_size_k_
    this.get_keys = LinkedHashMap_get_keys_k_
    this.get_values = LinkedHashMap_get_values_k_
    this.get_entries = LinkedHashMap_get_entries_k_
    this._size = 0
    require_Z_Function0Any_k_(initialCapacity >= 0, {initialCapacity: initialCapacity, invoke: function() as Object
        return "Negative initial capacity: " + __kotlin_numToStr_I_k_(m.initialCapacity)
    end function})
    require_Z_Function0Any_k_(loadFactor > 0, {loadFactor: loadFactor, invoke: function() as Object
        return "Non-positive load factor: " + __kotlin_numToStr_F_k_(m.loadFactor)
    end function})
    this.set_map(CreateObject("roAssociativeArray"))
    this.set_keyOrder(CreateObject("roArray", 0, true))
    this.set__size(0)
    return this
end function

function LinkedHashMap_create_Map_k_(original as Object) as Object
    this = {}
    this.__type = "LinkedHashMap"
    this.__proto = ["LinkedHashMap", "MutableMap", "Map"]
    this.__id = __kotlin_nextObjectId()
    this.isEmpty_k_ = LinkedHashMap_isEmpty_k_
    this.containsKey_AnyN_k_ = LinkedHashMap_containsKey_AnyN_k_
    this.containsValue_AnyN_k_ = LinkedHashMap_containsValue_AnyN_k_
    this.get_AnyN_k_ = LinkedHashMap_get_AnyN_k_
    this.put_AnyN_AnyN_k_ = LinkedHashMap_put_AnyN_AnyN_k_
    this.remove_AnyN_k_ = LinkedHashMap_remove_AnyN_k_
    this.putAll_Map_k_ = LinkedHashMap_putAll_Map_k_
    this.clear_k_ = LinkedHashMap_clear_k_
    this.equals_AnyN_k_ = LinkedHashMap_equals_AnyN_k_
    this.equals = LinkedHashMap_equals_AnyN_k_
    this.hashCode_k_ = LinkedHashMap_hashCode_k_
    this.hashCode = LinkedHashMap_hashCode_k_
    this.toString_k_ = LinkedHashMap_toString_k_
    this.toString = LinkedHashMap_toString_k_
    this.keyToString_AnyN_k_ = LinkedHashMap_keyToString_AnyN_k_
    this.get_map = LinkedHashMap_get_map_k_
    this.set_map = LinkedHashMap_set_map_Dynamic_k_
    this.get_keyOrder = LinkedHashMap_get_keyOrder_k_
    this.set_keyOrder = LinkedHashMap_set_keyOrder_Dynamic_k_
    this.get__size = LinkedHashMap_get__size_k_
    this.set__size = LinkedHashMap_set__size_I_k_
    this.get_size = LinkedHashMap_get_size_k_
    this.get_keys = LinkedHashMap_get_keys_k_
    this.get_values = LinkedHashMap_get_values_k_
    this.get_entries = LinkedHashMap_get_entries_k_
    this._size = 0
    this.set_map(CreateObject("roAssociativeArray"))
    this.set_keyOrder(CreateObject("roArray", 0, true))
    this.set__size(0)
    this.putAll_Map_k_(original)
    return this
end function

function LinkedHashMap_isEmpty_k_() as Boolean
    return m.get__size() = 0
end function

function LinkedHashMap_containsKey_AnyN_k_(key as Dynamic) as Boolean
    keyStr = m.keyToString_AnyN_k_(key)
    return m.get_map().DoesExist(keyStr)
end function

function LinkedHashMap_containsValue_AnyN_k_(value as Dynamic) as Boolean
    count = m.get_keyOrder().Count()
    i = 0
    while i < count
        keyStr = m.get_keyOrder()[i]
        entry = m.get_map()[keyStr]
        v = entry.v
        if brsStructuralEquals_AnyN_AnyN_k_(v, value) then
            return true
        end if
        i = (i + 1)
    end while
    return false
end function

function LinkedHashMap_get_AnyN_k_(key as Dynamic) as Dynamic
    keyStr = m.keyToString_AnyN_k_(key)
    if not m.get_map().DoesExist(keyStr) then
        return invalid
    end if
    entry = m.get_map()[keyStr]
    return entry.v
end function

function LinkedHashMap_put_AnyN_AnyN_k_(key as Dynamic, value as Dynamic) as Dynamic
    keyStr = m.keyToString_AnyN_k_(key)
    __when_tmp0 = invalid
    if m.get_map().DoesExist(keyStr) then
        oldEntry = m.get_map()[keyStr]
        __when_tmp0 = oldEntry.v
    else if true then
        m.get_keyOrder().Push(keyStr)
        m.set__size(m.get__size() + 1)
        __when_tmp0 = invalid
    end if
    oldValue = __when_tmp0

    entry = {k: key, v: value}
    m.get_map().AddReplace(keyStr, entry)
    return oldValue
end function

function LinkedHashMap_remove_AnyN_k_(key as Dynamic) as Dynamic
    keyStr = m.keyToString_AnyN_k_(key)
    if not m.get_map().DoesExist(keyStr) then
        return invalid
    end if
    oldEntry = m.get_map()[keyStr]
    oldValue = oldEntry.v
    m.get_map().Delete(keyStr)
    count = m.get_keyOrder().Count()
    i = 0
    while i < count
        if m.get_keyOrder()[i] = keyStr then
            m.get_keyOrder().Delete(i)
            exit while
        end if
        i = (i + 1)
    end while
    m.set__size(m.get__size() - 1)
    return oldValue
end function

sub LinkedHashMap_putAll_Map_k_(from as Object)
    __iter_127 = from.get_entries().iterator_k_()
    while __iter_127.hasNext_k_()
        entry = __iter_127.next_k_()
        m.put_AnyN_AnyN_k_(entry.get_key(), entry.get_value())

    end while

end sub

sub LinkedHashMap_clear_k_()
    m.get_map().Clear()
    m.get_keyOrder().Clear()
    m.set__size(0)
end sub

function LinkedHashMap_equals_AnyN_k_(other as Dynamic) as Boolean
    if __kotlin_identityEquals(other, m) then
        return true
    end if
    if not __kotlin_isInstanceOf(other, "Map") then
        return false
    end if
    if other.get_size() <> m.get_size() then
        return false
    end if
    __iter_128 = m.get_entries().iterator_k_()
    while __iter_128.hasNext_k_()
        entry = __iter_128.next_k_()
        otherMap = other
        otherValue = otherMap.get_AnyN_k_(entry.get_key())
        if not brsStructuralEquals_AnyN_AnyN_k_(entry.get_value(), otherValue) then
            return false
        end if
        if (otherValue = invalid) and not otherMap.containsKey_AnyN_k_(entry.get_key()) then
            return false
        end if

    end while

    return true
end function

function LinkedHashMap_hashCode_k_() as Integer
    result = 0
    __iter_129 = m.get_entries().iterator_k_()
    while __iter_129.hasNext_k_()
        entry = __iter_129.next_k_()
        result = (result + entry.hashCode())

    end while

    return result
end function

function LinkedHashMap_toString_k_() as String
    entries = joinToString_v4kj63_k_(m.get_entries(), ", ", "{", "}", invalid, invalid, {invoke: function(it as Object) as Object
        return (toString_AnyN_k_(it.get_key()) + "=") + toString_AnyN_k_(it.get_value())
    end function})
    return entries
end function

function LinkedHashMap_keyToString_AnyN_k_(key as Dynamic) as String
    return toString_AnyN_k_(key)
end function

function LinkedHashMap_get_map_k_() as Object
    return m.map
end function

sub LinkedHashMap_set_map_Dynamic_k_(value as Object)
    m.map = value
end sub

function LinkedHashMap_get_keyOrder_k_() as Object
    return m.keyOrder
end function

sub LinkedHashMap_set_keyOrder_Dynamic_k_(value as Object)
    m.keyOrder = value
end sub

function LinkedHashMap_get__size_k_() as Integer
    return m._size
end function

sub LinkedHashMap_set__size_I_k_(value as Integer)
    m._size = value
end sub

function LinkedHashMap_get_size_k_() as Integer
    return m.get__size()
end function

function LinkedHashMap_get_keys_k_() as Object
    return LinkedHashMap_LinkedKeySet_create_LinkedHashMap_k_(m)
end function

function LinkedHashMap_get_values_k_() as Object
    return LinkedHashMap_LinkedValueCollection_create_LinkedHashMap_k_(m)
end function

function LinkedHashMap_get_entries_k_() as Object
    return LinkedHashMap_LinkedEntrySet_create_LinkedHashMap_k_(m)
end function

function LinkedHashMap_LinkedKeySet_create_LinkedHashMap_k_(map as Object) as Object
    this = {}
    this.__type = "LinkedHashMap_LinkedKeySet"
    this.__proto = ["LinkedHashMap_LinkedKeySet", "MutableSet", "Set", "Collection", "Iterable", "MutableCollection", "MutableIterable"]
    this.__id = __kotlin_nextObjectId()
    this.isEmpty_k_ = LinkedHashMap_LinkedKeySet_isEmpty_k_
    this.contains_AnyN_k_ = LinkedHashMap_LinkedKeySet_contains_AnyN_k_
    this.containsAll_Collection_k_ = LinkedHashMap_LinkedKeySet_containsAll_Collection_k_
    this.iterator_k_ = LinkedHashMap_LinkedKeySet_iterator_k_
    this.add_AnyN_k_ = LinkedHashMap_LinkedKeySet_add_AnyN_k_
    this.addAll_Collection_k_ = LinkedHashMap_LinkedKeySet_addAll_Collection_k_
    this.remove_AnyN_k_ = LinkedHashMap_LinkedKeySet_remove_AnyN_k_
    this.removeAll_Collection_k_ = LinkedHashMap_LinkedKeySet_removeAll_Collection_k_
    this.retainAll_Collection_k_ = LinkedHashMap_LinkedKeySet_retainAll_Collection_k_
    this.clear_k_ = LinkedHashMap_LinkedKeySet_clear_k_
    this.get_map = LinkedHashMap_LinkedKeySet_get_map_k_
    this.get_array = LinkedHashMap_LinkedKeySet_get_array_k_
    this.get_size = LinkedHashMap_LinkedKeySet_get_size_k_
    this.map = map
    return this
end function

function LinkedHashMap_LinkedKeySet_isEmpty_k_() as Boolean
    return m.get_map().isEmpty_k_()
end function

function LinkedHashMap_LinkedKeySet_contains_AnyN_k_(element as Dynamic) as Boolean
    return m.get_map().containsKey_AnyN_k_(element)
end function

function LinkedHashMap_LinkedKeySet_containsAll_Collection_k_(elements as Object) as Boolean
    __iter_130 = elements.iterator_k_()
    while __iter_130.hasNext_k_()
        element = __iter_130.next_k_()
        if not m.contains_AnyN_k_(element) then
            return false
        end if
    end while

    return true
end function

function LinkedHashMap_LinkedKeySet_iterator_k_() as Object
    return LinkedHashMap_LinkedKeyIterator_create_LinkedHashMap_k_(m.get_map())
end function

function LinkedHashMap_LinkedKeySet_add_AnyN_k_(element as Dynamic) as Boolean
    throw UnsupportedOperationException_create_StrN_k_("Add not supported on key set")
end function

function LinkedHashMap_LinkedKeySet_addAll_Collection_k_(elements as Object) as Boolean
    throw UnsupportedOperationException_create_StrN_k_("Add not supported on key set")
end function

function LinkedHashMap_LinkedKeySet_remove_AnyN_k_(element as Dynamic) as Boolean
    if not m.get_map().containsKey_AnyN_k_(element) then
        return false
    end if
    m.get_map().remove_AnyN_k_(element)
    return true
end function

function LinkedHashMap_LinkedKeySet_removeAll_Collection_k_(elements as Object) as Boolean
    modified = false
    __iter_131 = elements.iterator_k_()
    while __iter_131.hasNext_k_()
        element = __iter_131.next_k_()
        if m.remove_AnyN_k_(element) then
            modified = true
        end if
    end while

    return modified
end function

function LinkedHashMap_LinkedKeySet_retainAll_Collection_k_(elements as Object) as Boolean
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

sub LinkedHashMap_LinkedKeySet_clear_k_()
    m.get_map().clear_k_()
end sub

function LinkedHashMap_LinkedKeySet_get_map_k_() as Object
    return m.map
end function

function LinkedHashMap_LinkedKeySet_get_array_k_() as Object
    return m.get_map().get_keyOrder()
end function

function LinkedHashMap_LinkedKeySet_get_size_k_() as Integer
    return m.get_map().get_size()
end function

function LinkedHashMap_LinkedValueCollection_create_LinkedHashMap_k_(map as Object) as Object
    this = {}
    this.__type = "LinkedHashMap_LinkedValueCollection"
    this.__proto = ["LinkedHashMap_LinkedValueCollection", "MutableCollection", "Collection", "Iterable", "MutableIterable"]
    this.__id = __kotlin_nextObjectId()
    this.isEmpty_k_ = LinkedHashMap_LinkedValueCollection_isEmpty_k_
    this.contains_AnyN_k_ = LinkedHashMap_LinkedValueCollection_contains_AnyN_k_
    this.containsAll_Collection_k_ = LinkedHashMap_LinkedValueCollection_containsAll_Collection_k_
    this.iterator_k_ = LinkedHashMap_LinkedValueCollection_iterator_k_
    this.add_AnyN_k_ = LinkedHashMap_LinkedValueCollection_add_AnyN_k_
    this.addAll_Collection_k_ = LinkedHashMap_LinkedValueCollection_addAll_Collection_k_
    this.remove_AnyN_k_ = LinkedHashMap_LinkedValueCollection_remove_AnyN_k_
    this.removeAll_Collection_k_ = LinkedHashMap_LinkedValueCollection_removeAll_Collection_k_
    this.retainAll_Collection_k_ = LinkedHashMap_LinkedValueCollection_retainAll_Collection_k_
    this.clear_k_ = LinkedHashMap_LinkedValueCollection_clear_k_
    this.get_map = LinkedHashMap_LinkedValueCollection_get_map_k_
    this.get_array = LinkedHashMap_LinkedValueCollection_get_array_k_
    this.get_size = LinkedHashMap_LinkedValueCollection_get_size_k_
    this.map = map
    return this
end function

function LinkedHashMap_LinkedValueCollection_isEmpty_k_() as Boolean
    return m.get_map().isEmpty_k_()
end function

function LinkedHashMap_LinkedValueCollection_contains_AnyN_k_(element as Dynamic) as Boolean
    return m.get_map().containsValue_AnyN_k_(element)
end function

function LinkedHashMap_LinkedValueCollection_containsAll_Collection_k_(elements as Object) as Boolean
    __iter_132 = elements.iterator_k_()
    while __iter_132.hasNext_k_()
        element = __iter_132.next_k_()
        if not m.contains_AnyN_k_(element) then
            return false
        end if
    end while

    return true
end function

function LinkedHashMap_LinkedValueCollection_iterator_k_() as Object
    return LinkedHashMap_LinkedValueIterator_create_LinkedHashMap_k_(m.get_map())
end function

function LinkedHashMap_LinkedValueCollection_add_AnyN_k_(element as Dynamic) as Boolean
    throw UnsupportedOperationException_create_StrN_k_("Add not supported on value collection")
end function

function LinkedHashMap_LinkedValueCollection_addAll_Collection_k_(elements as Object) as Boolean
    throw UnsupportedOperationException_create_StrN_k_("Add not supported on value collection")
end function

function LinkedHashMap_LinkedValueCollection_remove_AnyN_k_(element as Dynamic) as Boolean
    iter = m.iterator_k_()
    while iter.hasNext_k_()
        if brsStructuralEquals_AnyN_AnyN_k_(iter.next_k_(), element) then
            iter.remove_k_()
            return true
        end if
    end while
    return false
end function

function LinkedHashMap_LinkedValueCollection_removeAll_Collection_k_(elements as Object) as Boolean
    modified = false
    __iter_133 = elements.iterator_k_()
    while __iter_133.hasNext_k_()
        element = __iter_133.next_k_()
        while m.remove_AnyN_k_(element)
            modified = true
        end while

    end while

    return modified
end function

function LinkedHashMap_LinkedValueCollection_retainAll_Collection_k_(elements as Object) as Boolean
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

sub LinkedHashMap_LinkedValueCollection_clear_k_()
    m.get_map().clear_k_()
end sub

function LinkedHashMap_LinkedValueCollection_get_map_k_() as Object
    return m.map
end function

function LinkedHashMap_LinkedValueCollection_get_array_k_() as Object
    result = CreateObject("roArray", 0, true)
    keyCount = m.get_map().get_keyOrder().Count()
    i = 0
    while i < keyCount
        keyStr = m.get_map().get_keyOrder()[i]
        value = m.get_map().get_map()[keyStr]
        result.Push(value)
        i = (i + 1)
    end while
    return result
end function

function LinkedHashMap_LinkedValueCollection_get_size_k_() as Integer
    return m.get_map().get_size()
end function

function LinkedHashMap_LinkedEntrySet_create_LinkedHashMap_k_(map as Object) as Object
    this = {}
    this.__type = "LinkedHashMap_LinkedEntrySet"
    this.__proto = ["LinkedHashMap_LinkedEntrySet", "MutableSet", "Set", "Collection", "Iterable", "MutableCollection", "MutableIterable"]
    this.__id = __kotlin_nextObjectId()
    this.isEmpty_k_ = LinkedHashMap_LinkedEntrySet_isEmpty_k_
    this.contains_MutableEntry_k_ = LinkedHashMap_LinkedEntrySet_contains_MutableEntry_k_
    this.containsAll_CollectionMutableEntry_k_ = LinkedHashMap_LinkedEntrySet_containsAll_CollectionMutableEntry_k_
    this.iterator_k_ = LinkedHashMap_LinkedEntrySet_iterator_k_
    this.add_MutableEntry_k_ = LinkedHashMap_LinkedEntrySet_add_MutableEntry_k_
    this.addAll_CollectionMutableEntry_k_ = LinkedHashMap_LinkedEntrySet_addAll_CollectionMutableEntry_k_
    this.remove_MutableEntry_k_ = LinkedHashMap_LinkedEntrySet_remove_MutableEntry_k_
    this.removeAll_CollectionMutableEntry_k_ = LinkedHashMap_LinkedEntrySet_removeAll_CollectionMutableEntry_k_
    this.retainAll_CollectionMutableEntry_k_ = LinkedHashMap_LinkedEntrySet_retainAll_CollectionMutableEntry_k_
    this.clear_k_ = LinkedHashMap_LinkedEntrySet_clear_k_
    this.get_map = LinkedHashMap_LinkedEntrySet_get_map_k_
    this.get_array = LinkedHashMap_LinkedEntrySet_get_array_k_
    this.get_size = LinkedHashMap_LinkedEntrySet_get_size_k_
    this.map = map
    return this
end function

function LinkedHashMap_LinkedEntrySet_isEmpty_k_() as Boolean
    return m.get_map().isEmpty_k_()
end function

function LinkedHashMap_LinkedEntrySet_contains_MutableEntry_k_(element as Object) as Boolean
    value = m.get_map().get_AnyN_k_(element.get_key())
    return brsStructuralEquals_AnyN_AnyN_k_(value, element.get_value()) and ((value <> invalid) or m.get_map().containsKey_AnyN_k_(element.get_key()))
end function

function LinkedHashMap_LinkedEntrySet_containsAll_CollectionMutableEntry_k_(elements as Object) as Boolean
    __iter_134 = elements.iterator_k_()
    while __iter_134.hasNext_k_()
        element = __iter_134.next_k_()
        if not m.contains_MutableEntry_k_(element) then
            return false
        end if
    end while

    return true
end function

function LinkedHashMap_LinkedEntrySet_iterator_k_() as Object
    return LinkedHashMap_LinkedEntryIterator_create_LinkedHashMap_k_(m.get_map())
end function

function LinkedHashMap_LinkedEntrySet_add_MutableEntry_k_(element as Object) as Boolean
    throw UnsupportedOperationException_create_StrN_k_("Add not supported on entry set")
end function

function LinkedHashMap_LinkedEntrySet_addAll_CollectionMutableEntry_k_(elements as Object) as Boolean
    throw UnsupportedOperationException_create_StrN_k_("Add not supported on entry set")
end function

function LinkedHashMap_LinkedEntrySet_remove_MutableEntry_k_(element as Object) as Boolean
    if not m.contains_MutableEntry_k_(element) then
        return false
    end if
    m.get_map().remove_AnyN_k_(element.get_key())
    return true
end function

function LinkedHashMap_LinkedEntrySet_removeAll_CollectionMutableEntry_k_(elements as Object) as Boolean
    modified = false
    __iter_135 = elements.iterator_k_()
    while __iter_135.hasNext_k_()
        element = __iter_135.next_k_()
        if m.remove_MutableEntry_k_(element) then
            modified = true
        end if
    end while

    return modified
end function

function LinkedHashMap_LinkedEntrySet_retainAll_CollectionMutableEntry_k_(elements as Object) as Boolean
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

sub LinkedHashMap_LinkedEntrySet_clear_k_()
    m.get_map().clear_k_()
end sub

function LinkedHashMap_LinkedEntrySet_get_map_k_() as Object
    return m.map
end function

function LinkedHashMap_LinkedEntrySet_get_array_k_() as Object
    result = CreateObject("roArray", 0, true)
    keyCount = m.get_map().get_keyOrder().Count()
    i = 0
    while i < keyCount
        keyStr = m.get_map().get_keyOrder()[i]
        value = m.get_map().get_map()[keyStr]
        result.Push(LinkedHashMap_SimpleEntry_create_AnyN_AnyN_k_(keyStr, value))
        i = (i + 1)
    end while
    return result
end function

function LinkedHashMap_LinkedEntrySet_get_size_k_() as Integer
    return m.get_map().get_size()
end function

function LinkedHashMap_LinkedKeyIterator_create_LinkedHashMap_k_(map as Object) as Object
    this = {}
    this.__type = "LinkedHashMap_LinkedKeyIterator"
    this.__proto = ["LinkedHashMap_LinkedKeyIterator", "MutableIterator", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.hasNext_k_ = LinkedHashMap_LinkedKeyIterator_hasNext_k_
    this.next_k_ = LinkedHashMap_LinkedKeyIterator_next_k_
    this.remove_k_ = LinkedHashMap_LinkedKeyIterator_remove_k_
    this.get_map = LinkedHashMap_LinkedKeyIterator_get_map_k_
    this.get_currentIndex = LinkedHashMap_LinkedKeyIterator_get_currentIndex_k_
    this.set_currentIndex = LinkedHashMap_LinkedKeyIterator_set_currentIndex_I_k_
    this.get_orderCount = LinkedHashMap_LinkedKeyIterator_get_orderCount_k_
    this.set_orderCount = LinkedHashMap_LinkedKeyIterator_set_orderCount_I_k_
    this.get_lastReturnedKey = LinkedHashMap_LinkedKeyIterator_get_lastReturnedKey_k_
    this.set_lastReturnedKey = LinkedHashMap_LinkedKeyIterator_set_lastReturnedKey_AnyN_k_
    this.get_canRemove = LinkedHashMap_LinkedKeyIterator_get_canRemove_k_
    this.set_canRemove = LinkedHashMap_LinkedKeyIterator_set_canRemove_Z_k_
    this.map = map
    this.currentIndex = 0
    this.orderCount = this.get_map().get_keyOrder().Count()
    this.lastReturnedKey = invalid
    this.canRemove = false
    return this
end function

function LinkedHashMap_LinkedKeyIterator_hasNext_k_() as Boolean
    return m.get_currentIndex() < m.get_orderCount()
end function

function LinkedHashMap_LinkedKeyIterator_next_k_() as Dynamic
    if not m.hasNext_k_() then
        throw NoSuchElementException_create_k_()
    end if
    keyStr = m.get_map().get_keyOrder()[m.get_currentIndex()]
    entry = m.get_map().get_map()[keyStr]
    key = entry.k
    m.set_lastReturnedKey(key)
    m.set_currentIndex(m.get_currentIndex() + 1)
    m.set_canRemove(true)
    return key
end function

sub LinkedHashMap_LinkedKeyIterator_remove_k_()
    check_Z_Function0Any_k_(m.get_canRemove(), {invoke: function() as Object
        return "Call next() before removing element"
    end function})
    tmp0_elvis_lhs = m.get_lastReturnedKey()
    __when_tmp1 = invalid
    if tmp0_elvis_lhs = invalid then
        throw IllegalStateException_create_k_()
    else if true then
        __when_tmp1 = tmp0_elvis_lhs
    end if
    key = __when_tmp1

    m.get_map().remove_AnyN_k_(key)
    m.set_currentIndex(m.get_currentIndex() - 1)
    m.set_orderCount(m.get_orderCount() - 1)
    m.set_canRemove(false)
end sub

function LinkedHashMap_LinkedKeyIterator_get_map_k_() as Object
    return m.map
end function

function LinkedHashMap_LinkedKeyIterator_get_currentIndex_k_() as Integer
    return m.currentIndex
end function

sub LinkedHashMap_LinkedKeyIterator_set_currentIndex_I_k_(value as Integer)
    m.currentIndex = value
end sub

function LinkedHashMap_LinkedKeyIterator_get_orderCount_k_() as Integer
    return m.orderCount
end function

sub LinkedHashMap_LinkedKeyIterator_set_orderCount_I_k_(value as Integer)
    m.orderCount = value
end sub

function LinkedHashMap_LinkedKeyIterator_get_lastReturnedKey_k_() as Dynamic
    return m.lastReturnedKey
end function

sub LinkedHashMap_LinkedKeyIterator_set_lastReturnedKey_AnyN_k_(value as Dynamic)
    m.lastReturnedKey = value
end sub

function LinkedHashMap_LinkedKeyIterator_get_canRemove_k_() as Boolean
    return m.canRemove
end function

sub LinkedHashMap_LinkedKeyIterator_set_canRemove_Z_k_(value as Boolean)
    m.canRemove = value
end sub

function LinkedHashMap_LinkedValueIterator_create_LinkedHashMap_k_(map as Object) as Object
    this = {}
    this.__type = "LinkedHashMap_LinkedValueIterator"
    this.__proto = ["LinkedHashMap_LinkedValueIterator", "MutableIterator", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.hasNext_k_ = LinkedHashMap_LinkedValueIterator_hasNext_k_
    this.next_k_ = LinkedHashMap_LinkedValueIterator_next_k_
    this.remove_k_ = LinkedHashMap_LinkedValueIterator_remove_k_
    this.get_map = LinkedHashMap_LinkedValueIterator_get_map_k_
    this.get_currentIndex = LinkedHashMap_LinkedValueIterator_get_currentIndex_k_
    this.set_currentIndex = LinkedHashMap_LinkedValueIterator_set_currentIndex_I_k_
    this.get_orderCount = LinkedHashMap_LinkedValueIterator_get_orderCount_k_
    this.set_orderCount = LinkedHashMap_LinkedValueIterator_set_orderCount_I_k_
    this.get_lastReturnedKey = LinkedHashMap_LinkedValueIterator_get_lastReturnedKey_k_
    this.set_lastReturnedKey = LinkedHashMap_LinkedValueIterator_set_lastReturnedKey_AnyN_k_
    this.get_canRemove = LinkedHashMap_LinkedValueIterator_get_canRemove_k_
    this.set_canRemove = LinkedHashMap_LinkedValueIterator_set_canRemove_Z_k_
    this.map = map
    this.currentIndex = 0
    this.orderCount = this.get_map().get_keyOrder().Count()
    this.lastReturnedKey = invalid
    this.canRemove = false
    return this
end function

function LinkedHashMap_LinkedValueIterator_hasNext_k_() as Boolean
    return m.get_currentIndex() < m.get_orderCount()
end function

function LinkedHashMap_LinkedValueIterator_next_k_() as Dynamic
    if not m.hasNext_k_() then
        throw NoSuchElementException_create_k_()
    end if
    keyStr = m.get_map().get_keyOrder()[m.get_currentIndex()]
    entry = m.get_map().get_map()[keyStr]
    key = entry.k
    value = entry.v
    m.set_lastReturnedKey(key)
    m.set_currentIndex(m.get_currentIndex() + 1)
    m.set_canRemove(true)
    return value
end function

sub LinkedHashMap_LinkedValueIterator_remove_k_()
    check_Z_Function0Any_k_(m.get_canRemove(), {invoke: function() as Object
        return "Call next() before removing element"
    end function})
    tmp0_elvis_lhs = m.get_lastReturnedKey()
    __when_tmp2 = invalid
    if tmp0_elvis_lhs = invalid then
        throw IllegalStateException_create_k_()
    else if true then
        __when_tmp2 = tmp0_elvis_lhs
    end if
    key = __when_tmp2

    m.get_map().remove_AnyN_k_(key)
    m.set_currentIndex(m.get_currentIndex() - 1)
    m.set_orderCount(m.get_orderCount() - 1)
    m.set_canRemove(false)
end sub

function LinkedHashMap_LinkedValueIterator_get_map_k_() as Object
    return m.map
end function

function LinkedHashMap_LinkedValueIterator_get_currentIndex_k_() as Integer
    return m.currentIndex
end function

sub LinkedHashMap_LinkedValueIterator_set_currentIndex_I_k_(value as Integer)
    m.currentIndex = value
end sub

function LinkedHashMap_LinkedValueIterator_get_orderCount_k_() as Integer
    return m.orderCount
end function

sub LinkedHashMap_LinkedValueIterator_set_orderCount_I_k_(value as Integer)
    m.orderCount = value
end sub

function LinkedHashMap_LinkedValueIterator_get_lastReturnedKey_k_() as Dynamic
    return m.lastReturnedKey
end function

sub LinkedHashMap_LinkedValueIterator_set_lastReturnedKey_AnyN_k_(value as Dynamic)
    m.lastReturnedKey = value
end sub

function LinkedHashMap_LinkedValueIterator_get_canRemove_k_() as Boolean
    return m.canRemove
end function

sub LinkedHashMap_LinkedValueIterator_set_canRemove_Z_k_(value as Boolean)
    m.canRemove = value
end sub

function LinkedHashMap_LinkedEntryIterator_create_LinkedHashMap_k_(map as Object) as Object
    this = {}
    this.__type = "LinkedHashMap_LinkedEntryIterator"
    this.__proto = ["LinkedHashMap_LinkedEntryIterator", "MutableIterator", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.hasNext_k_ = LinkedHashMap_LinkedEntryIterator_hasNext_k_
    this.next_k_ = LinkedHashMap_LinkedEntryIterator_next_k_
    this.remove_k_ = LinkedHashMap_LinkedEntryIterator_remove_k_
    this.get_map = LinkedHashMap_LinkedEntryIterator_get_map_k_
    this.get_currentIndex = LinkedHashMap_LinkedEntryIterator_get_currentIndex_k_
    this.set_currentIndex = LinkedHashMap_LinkedEntryIterator_set_currentIndex_I_k_
    this.get_orderCount = LinkedHashMap_LinkedEntryIterator_get_orderCount_k_
    this.set_orderCount = LinkedHashMap_LinkedEntryIterator_set_orderCount_I_k_
    this.get_lastReturnedKey = LinkedHashMap_LinkedEntryIterator_get_lastReturnedKey_k_
    this.set_lastReturnedKey = LinkedHashMap_LinkedEntryIterator_set_lastReturnedKey_AnyN_k_
    this.get_canRemove = LinkedHashMap_LinkedEntryIterator_get_canRemove_k_
    this.set_canRemove = LinkedHashMap_LinkedEntryIterator_set_canRemove_Z_k_
    this.map = map
    this.currentIndex = 0
    this.orderCount = this.get_map().get_keyOrder().Count()
    this.lastReturnedKey = invalid
    this.canRemove = false
    return this
end function

function LinkedHashMap_LinkedEntryIterator_hasNext_k_() as Boolean
    return m.get_currentIndex() < m.get_orderCount()
end function

function LinkedHashMap_LinkedEntryIterator_next_k_() as Object
    if not m.hasNext_k_() then
        throw NoSuchElementException_create_k_()
    end if
    keyStr = m.get_map().get_keyOrder()[m.get_currentIndex()]
    entry = m.get_map().get_map()[keyStr]
    key = entry.k
    value = entry.v
    m.set_lastReturnedKey(key)
    m.set_currentIndex(m.get_currentIndex() + 1)
    m.set_canRemove(true)
    return LinkedHashMap_SimpleEntry_create_AnyN_AnyN_k_(key, value)
end function

sub LinkedHashMap_LinkedEntryIterator_remove_k_()
    check_Z_Function0Any_k_(m.get_canRemove(), {invoke: function() as Object
        return "Call next() before removing element"
    end function})
    tmp0_elvis_lhs = m.get_lastReturnedKey()
    __when_tmp3 = invalid
    if tmp0_elvis_lhs = invalid then
        throw IllegalStateException_create_k_()
    else if true then
        __when_tmp3 = tmp0_elvis_lhs
    end if
    key = __when_tmp3

    m.get_map().remove_AnyN_k_(key)
    m.set_currentIndex(m.get_currentIndex() - 1)
    m.set_orderCount(m.get_orderCount() - 1)
    m.set_canRemove(false)
end sub

function LinkedHashMap_LinkedEntryIterator_get_map_k_() as Object
    return m.map
end function

function LinkedHashMap_LinkedEntryIterator_get_currentIndex_k_() as Integer
    return m.currentIndex
end function

sub LinkedHashMap_LinkedEntryIterator_set_currentIndex_I_k_(value as Integer)
    m.currentIndex = value
end sub

function LinkedHashMap_LinkedEntryIterator_get_orderCount_k_() as Integer
    return m.orderCount
end function

sub LinkedHashMap_LinkedEntryIterator_set_orderCount_I_k_(value as Integer)
    m.orderCount = value
end sub

function LinkedHashMap_LinkedEntryIterator_get_lastReturnedKey_k_() as Dynamic
    return m.lastReturnedKey
end function

sub LinkedHashMap_LinkedEntryIterator_set_lastReturnedKey_AnyN_k_(value as Dynamic)
    m.lastReturnedKey = value
end sub

function LinkedHashMap_LinkedEntryIterator_get_canRemove_k_() as Boolean
    return m.canRemove
end function

sub LinkedHashMap_LinkedEntryIterator_set_canRemove_Z_k_(value as Boolean)
    m.canRemove = value
end sub

function LinkedHashMap_SimpleEntry_create_AnyN_AnyN_k_(key as Dynamic, value as Dynamic) as Object
    this = {}
    this.__type = "LinkedHashMap_SimpleEntry"
    this.__proto = ["LinkedHashMap_SimpleEntry", "MutableMap_MutableEntry", "Map_Entry"]
    this.__id = __kotlin_nextObjectId()
    this.setValue_AnyN_k_ = LinkedHashMap_SimpleEntry_setValue_AnyN_k_
    this.equals_AnyN_k_ = LinkedHashMap_SimpleEntry_equals_AnyN_k_
    this.equals = LinkedHashMap_SimpleEntry_equals_AnyN_k_
    this.hashCode_k_ = LinkedHashMap_SimpleEntry_hashCode_k_
    this.hashCode = LinkedHashMap_SimpleEntry_hashCode_k_
    this.toString_k_ = LinkedHashMap_SimpleEntry_toString_k_
    this.toString = LinkedHashMap_SimpleEntry_toString_k_
    this.get_key = LinkedHashMap_SimpleEntry_get_key_k_
    this.get_value = LinkedHashMap_SimpleEntry_get_value_k_
    this.set_value = LinkedHashMap_SimpleEntry_set_value_AnyN_k_
    this.key = key
    this.value = value
    return this
end function

function LinkedHashMap_SimpleEntry_setValue_AnyN_k_(newValue as Dynamic) as Dynamic
    oldValue = m.get_value()
    m.set_value(newValue)
    return oldValue
end function

function LinkedHashMap_SimpleEntry_equals_AnyN_k_(other as Dynamic) as Boolean
    if not __kotlin_isInstanceOf(other, "Map_Entry") then
        return false
    end if
    return brsStructuralEquals_AnyN_AnyN_k_(m.get_key(), other.get_key()) and brsStructuralEquals_AnyN_AnyN_k_(m.get_value(), other.get_value())
end function

function LinkedHashMap_SimpleEntry_hashCode_k_() as Integer
    tmp0_safe_receiver = m.get_key()
    __when_tmp4 = invalid
    if tmp0_safe_receiver = invalid then
        __when_tmp4 = invalid
    else if true then
        __when_tmp4 = tmp0_safe_receiver.hashCode()
    end if
    tmp1_elvis_lhs = __when_tmp4
    __when_tmp5 = invalid
    if tmp1_elvis_lhs = invalid then
        __when_tmp5 = 0
    else if true then
        __when_tmp5 = tmp1_elvis_lhs
    end if
    tmp2_safe_receiver = m.get_value()
    __when_tmp6 = invalid
    if tmp2_safe_receiver = invalid then
        __when_tmp6 = invalid
    else if true then
        __when_tmp6 = tmp2_safe_receiver.hashCode()
    end if
    tmp3_elvis_lhs = __when_tmp6
    __when_tmp7 = invalid
    if tmp3_elvis_lhs = invalid then
        __when_tmp7 = 0
    else if true then
        __when_tmp7 = tmp3_elvis_lhs
    end if
    return xor_rI_I_k_(__when_tmp5, __when_tmp7)

end function

function LinkedHashMap_SimpleEntry_toString_k_() as String
    return (toString_AnyN_k_(m.get_key()) + "=") + toString_AnyN_k_(m.get_value())
end function

function LinkedHashMap_SimpleEntry_get_key_k_() as Dynamic
    return m.key
end function

function LinkedHashMap_SimpleEntry_get_value_k_() as Dynamic
    return m.value
end function

sub LinkedHashMap_SimpleEntry_set_value_AnyN_k_(value as Dynamic)
    m.value = value
end sub

function linkedMapOf_k_() as Object
    return LinkedHashMap_create_k_()
end function

function linkedMapOf_Arr_k_(pairs as Object) as Object
    map = LinkedHashMap_create_I_k_(pairs.count())
    indexedObject = pairs
    inductionVariable = 0
    last = indexedObject.count()
    while inductionVariable < last
        pair = indexedObject[inductionVariable]
        inductionVariable = (inductionVariable + 1)

        map.put_AnyN_AnyN_k_(pair.first, pair.second)

    end while

    return map
end function
