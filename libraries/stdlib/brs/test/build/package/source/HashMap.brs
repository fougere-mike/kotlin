function HashMap_create_k_() as Object
    this = {}
    this.__type = "HashMap"
    this.__proto = ["HashMap", "MutableMap", "Map"]
    this.__id = __kotlin_nextObjectId()
    this.isEmpty_k_ = HashMap_isEmpty_k_
    this.containsKey_AnyN_k_ = HashMap_containsKey_AnyN_k_
    this.containsValue_AnyN_k_ = HashMap_containsValue_AnyN_k_
    this.get_AnyN_k_ = HashMap_get_AnyN_k_
    this.put_AnyN_AnyN_k_ = HashMap_put_AnyN_AnyN_k_
    this.remove_AnyN_k_ = HashMap_remove_AnyN_k_
    this.putAll_Map_k_ = HashMap_putAll_Map_k_
    this.clear_k_ = HashMap_clear_k_
    this.equals_AnyN_k_ = HashMap_equals_AnyN_k_
    this.equals = HashMap_equals_AnyN_k_
    this.hashCode_k_ = HashMap_hashCode_k_
    this.hashCode = HashMap_hashCode_k_
    this.toString_k_ = HashMap_toString_k_
    this.toString = HashMap_toString_k_
    this.keyToString_AnyN_k_ = HashMap_keyToString_AnyN_k_
    this.getKeysArray_k_ = HashMap_getKeysArray_k_
    this.getMap_k_ = HashMap_getMap_k_
    this.get_map = HashMap_get_map_k_
    this.set_map = HashMap_set_map_Dynamic_k_
    this.get__size = HashMap_get__size_k_
    this.set__size = HashMap_set__size_I_k_
    this.get_size = HashMap_get_size_k_
    this.get_keys = HashMap_get_keys_k_
    this.get_values = HashMap_get_values_k_
    this.get_entries = HashMap_get_entries_k_
    this._size = 0
    this.set_map(CreateObject("roAssociativeArray"))
    this.set__size(0)
    return this
end function

function HashMap_create_I_k_(initialCapacity as Integer) as Object
    this = {}
    this.__type = "HashMap"
    this.__proto = ["HashMap", "MutableMap", "Map"]
    this.__id = __kotlin_nextObjectId()
    this.isEmpty_k_ = HashMap_isEmpty_k_
    this.containsKey_AnyN_k_ = HashMap_containsKey_AnyN_k_
    this.containsValue_AnyN_k_ = HashMap_containsValue_AnyN_k_
    this.get_AnyN_k_ = HashMap_get_AnyN_k_
    this.put_AnyN_AnyN_k_ = HashMap_put_AnyN_AnyN_k_
    this.remove_AnyN_k_ = HashMap_remove_AnyN_k_
    this.putAll_Map_k_ = HashMap_putAll_Map_k_
    this.clear_k_ = HashMap_clear_k_
    this.equals_AnyN_k_ = HashMap_equals_AnyN_k_
    this.equals = HashMap_equals_AnyN_k_
    this.hashCode_k_ = HashMap_hashCode_k_
    this.hashCode = HashMap_hashCode_k_
    this.toString_k_ = HashMap_toString_k_
    this.toString = HashMap_toString_k_
    this.keyToString_AnyN_k_ = HashMap_keyToString_AnyN_k_
    this.getKeysArray_k_ = HashMap_getKeysArray_k_
    this.getMap_k_ = HashMap_getMap_k_
    this.get_map = HashMap_get_map_k_
    this.set_map = HashMap_set_map_Dynamic_k_
    this.get__size = HashMap_get__size_k_
    this.set__size = HashMap_set__size_I_k_
    this.get_size = HashMap_get_size_k_
    this.get_keys = HashMap_get_keys_k_
    this.get_values = HashMap_get_values_k_
    this.get_entries = HashMap_get_entries_k_
    this._size = 0
    require_Z_Function0Any_k_(initialCapacity >= 0, {initialCapacity: initialCapacity, invoke: function() as Object
        return "Negative initial capacity: " + __kotlin_numToStr_I_k_(m.initialCapacity)
    end function})
    this.set_map(CreateObject("roAssociativeArray"))
    this.set__size(0)
    return this
end function

function HashMap_create_Map_k_(original as Object) as Object
    this = {}
    this.__type = "HashMap"
    this.__proto = ["HashMap", "MutableMap", "Map"]
    this.__id = __kotlin_nextObjectId()
    this.isEmpty_k_ = HashMap_isEmpty_k_
    this.containsKey_AnyN_k_ = HashMap_containsKey_AnyN_k_
    this.containsValue_AnyN_k_ = HashMap_containsValue_AnyN_k_
    this.get_AnyN_k_ = HashMap_get_AnyN_k_
    this.put_AnyN_AnyN_k_ = HashMap_put_AnyN_AnyN_k_
    this.remove_AnyN_k_ = HashMap_remove_AnyN_k_
    this.putAll_Map_k_ = HashMap_putAll_Map_k_
    this.clear_k_ = HashMap_clear_k_
    this.equals_AnyN_k_ = HashMap_equals_AnyN_k_
    this.equals = HashMap_equals_AnyN_k_
    this.hashCode_k_ = HashMap_hashCode_k_
    this.hashCode = HashMap_hashCode_k_
    this.toString_k_ = HashMap_toString_k_
    this.toString = HashMap_toString_k_
    this.keyToString_AnyN_k_ = HashMap_keyToString_AnyN_k_
    this.getKeysArray_k_ = HashMap_getKeysArray_k_
    this.getMap_k_ = HashMap_getMap_k_
    this.get_map = HashMap_get_map_k_
    this.set_map = HashMap_set_map_Dynamic_k_
    this.get__size = HashMap_get__size_k_
    this.set__size = HashMap_set__size_I_k_
    this.get_size = HashMap_get_size_k_
    this.get_keys = HashMap_get_keys_k_
    this.get_values = HashMap_get_values_k_
    this.get_entries = HashMap_get_entries_k_
    this._size = 0
    this.set_map(CreateObject("roAssociativeArray"))
    this.set__size(0)
    this.putAll_Map_k_(original)
    return this
end function

function HashMap_isEmpty_k_() as Boolean
    return m.get__size() = 0
end function

function HashMap_containsKey_AnyN_k_(key as Dynamic) as Boolean
    keyStr = m.keyToString_AnyN_k_(key)
    return m.get_map().DoesExist(keyStr)
end function

function HashMap_containsValue_AnyN_k_(value as Dynamic) as Boolean
    keysArray = m.get_map().Keys()
    count = keysArray.Count()
    i = 0
    while i < count
        keyStr = keysArray[i]
        entry = m.get_map().Lookup(keyStr)
        v = entry.v
        if brsStructuralEquals_AnyN_AnyN_k_(v, value) then
            return true
        end if
        i = (i + 1)
    end while
    return false
end function

function HashMap_get_AnyN_k_(key as Dynamic) as Dynamic
    keyStr = m.keyToString_AnyN_k_(key)
    if not m.get_map().DoesExist(keyStr) then
        return invalid
    end if
    entry = m.get_map().Lookup(keyStr)
    return entry.v
end function

function HashMap_put_AnyN_AnyN_k_(key as Dynamic, value as Dynamic) as Dynamic
    keyStr = m.keyToString_AnyN_k_(key)
    oldValue = invalid
    if m.get_map().DoesExist(keyStr) then
        oldEntry = m.get_map().Lookup(keyStr)
        oldValue = oldEntry.v
    else if true then
        m.set__size(m.get__size() + 1)
    end if
    entry = {k: key, v: value}
    m.get_map().AddReplace(keyStr, entry)
    return oldValue
end function

function HashMap_remove_AnyN_k_(key as Dynamic) as Dynamic
    keyStr = m.keyToString_AnyN_k_(key)
    if not m.get_map().DoesExist(keyStr) then
        return invalid
    end if
    oldEntry = m.get_map().Lookup(keyStr)
    oldValue = oldEntry.v
    m.get_map().Delete(keyStr)
    m.set__size(m.get__size() - 1)
    return oldValue
end function

sub HashMap_putAll_Map_k_(from as Object)
    __iter_156 = from.get_entries().iterator_k_()
    while __iter_156.hasNext_k_()
        entry = __iter_156.next_k_()
        m.put_AnyN_AnyN_k_(entry.get_key(), entry.get_value())

    end while

end sub

sub HashMap_clear_k_()
    m.get_map().Clear()
    m.set__size(0)
end sub

function HashMap_equals_AnyN_k_(other as Dynamic) as Boolean
    if __kotlin_identityEquals(other, m) then
        return true
    end if
    if not __kotlin_isInstanceOf(other, "Map") then
        return false
    end if
    if other.get_size() <> m.get_size() then
        return false
    end if
    __iter_157 = m.get_entries().iterator_k_()
    while __iter_157.hasNext_k_()
        entry = __iter_157.next_k_()
        otherMap = other
        otherValue = otherMap.get_AnyN_k_(entry.get_key())
        if not brsStructuralEquals_AnyN_AnyN_k_(entry.get_value(), otherValue) then
            return false
        end if

    end while

    return true
end function

function HashMap_hashCode_k_() as Integer
    h = 0
    __iter_158 = m.get_entries().iterator_k_()
    while __iter_158.hasNext_k_()
        entry = __iter_158.next_k_()
        h = (h + entry.hashCode())

    end while

    return h
end function

function HashMap_toString_k_() as String
    if m.isEmpty_k_() then
        return "{}"
    end if
    sb = StringBuilder_create_k_()
    sb.append_StrN_k_("{")
    first = true
    __iter_159 = m.get_entries().iterator_k_()
    while __iter_159.hasNext_k_()
        entry = __iter_159.next_k_()
        if not first then
            sb.append_StrN_k_(", ")
        end if
        first = false
        k = entry.get_key()
        v = entry.get_value()
        if __kotlin_identityEquals(k, m) then
            sb.append_StrN_k_("(this Map)")
        else if true then
            sb.append_StrN_k_(toString_AnyN_k_(k))
        end if
        sb.append_StrN_k_("=")
        if __kotlin_identityEquals(v, m) then
            sb.append_StrN_k_("(this Map)")
        else if true then
            sb.append_StrN_k_(toString_AnyN_k_(v))
        end if

    end while

    sb.append_StrN_k_("}")
    return sb.toString()
end function

function HashMap_keyToString_AnyN_k_(key as Dynamic) as String
    return toString_AnyN_k_(key)
end function

function HashMap_getKeysArray_k_() as Object
    return m.get_map().Keys()
end function

function HashMap_getMap_k_() as Object
    return m.get_map()
end function

function HashMap_get_map_k_() as Object
    return m.map
end function

sub HashMap_set_map_Dynamic_k_(value as Object)
    m.map = value
end sub

function HashMap_get__size_k_() as Integer
    return m._size
end function

sub HashMap_set__size_I_k_(value as Integer)
    m._size = value
end sub

function HashMap_get_size_k_() as Integer
    return m.get__size()
end function

function HashMap_get_keys_k_() as Object
    return KeySet_create_HashMap_k_(m)
end function

function HashMap_get_values_k_() as Object
    return ValueCollection_create_HashMap_k_(m)
end function

function HashMap_get_entries_k_() as Object
    return EntrySet_create_HashMap_k_(m)
end function

function HashMapEntry_create_HashMap_AnyN_k_(map as Object, key as Dynamic) as Object
    this = {}
    this.__type = "HashMapEntry"
    this.__proto = ["HashMapEntry", "MutableMap_MutableEntry", "Map_Entry"]
    this.__id = __kotlin_nextObjectId()
    this.setValue_AnyN_k_ = HashMapEntry_setValue_AnyN_k_
    this.equals_AnyN_k_ = HashMapEntry_equals_AnyN_k_
    this.equals = HashMapEntry_equals_AnyN_k_
    this.hashCode_k_ = HashMapEntry_hashCode_k_
    this.hashCode = HashMapEntry_hashCode_k_
    this.toString_k_ = HashMapEntry_toString_k_
    this.toString = HashMapEntry_toString_k_
    this.get_map = HashMapEntry_get_map_k_
    this.get_key = HashMapEntry_get_key_k_
    this.get_value = HashMapEntry_get_value_k_
    this.map = map
    this.key = key
    return this
end function

function HashMapEntry_setValue_AnyN_k_(newValue as Dynamic) as Dynamic
    old = m.get_map().put_AnyN_AnyN_k_(m.get_key(), newValue)
    return old
end function

function HashMapEntry_equals_AnyN_k_(other as Dynamic) as Boolean
    if not __kotlin_isInstanceOf(other, "Map_Entry") then
        return false
    end if
    return brsStructuralEquals_AnyN_AnyN_k_(m.get_key(), other.get_key()) and brsStructuralEquals_AnyN_AnyN_k_(m.get_value(), other.get_value())
end function

function HashMapEntry_hashCode_k_() as Integer
    tmp0_safe_receiver = m.get_key()
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
    tmp2_safe_receiver = m.get_value()
    __when_tmp2 = invalid
    if tmp2_safe_receiver = invalid then
        __when_tmp2 = invalid
    else if true then
        __when_tmp2 = tmp2_safe_receiver.hashCode()
    end if
    tmp3_elvis_lhs = __when_tmp2
    __when_tmp3 = invalid
    if tmp3_elvis_lhs = invalid then
        __when_tmp3 = 0
    else if true then
        __when_tmp3 = tmp3_elvis_lhs
    end if
    return (__when_tmp1 or __when_tmp3) and not (__when_tmp1 and __when_tmp3)

end function

function HashMapEntry_toString_k_() as String
    return (toString_AnyN_k_(m.get_key()) + "=") + toString_AnyN_k_(m.get_value())
end function

function HashMapEntry_get_map_k_() as Object
    return m.map
end function

function HashMapEntry_get_key_k_() as Dynamic
    return m.key
end function

function HashMapEntry_get_value_k_() as Dynamic
    return m.get_map().get_AnyN_k_(m.get_key())
end function

function KeySet_create_HashMap_k_(map as Object) as Object
    this = {}
    this.__type = "KeySet"
    this.__proto = ["KeySet", "MutableSet", "Set", "Collection", "Iterable", "MutableCollection", "MutableIterable"]
    this.__id = __kotlin_nextObjectId()
    this.isEmpty_k_ = KeySet_isEmpty_k_
    this.contains_AnyN_k_ = KeySet_contains_AnyN_k_
    this.containsAll_Collection_k_ = KeySet_containsAll_Collection_k_
    this.iterator_k_ = KeySet_iterator_k_
    this.add_AnyN_k_ = KeySet_add_AnyN_k_
    this.remove_AnyN_k_ = KeySet_remove_AnyN_k_
    this.addAll_Collection_k_ = KeySet_addAll_Collection_k_
    this.removeAll_Collection_k_ = KeySet_removeAll_Collection_k_
    this.retainAll_Collection_k_ = KeySet_retainAll_Collection_k_
    this.clear_k_ = KeySet_clear_k_
    this.get_map = KeySet_get_map_k_
    this.get_array = KeySet_get_array_k_
    this.get_size = KeySet_get_size_k_
    this.map = map
    return this
end function

function KeySet_isEmpty_k_() as Boolean
    return m.get_map().isEmpty_k_()
end function

function KeySet_contains_AnyN_k_(element as Dynamic) as Boolean
    return m.get_map().containsKey_AnyN_k_(element)
end function

function KeySet_containsAll_Collection_k_(elements as Object) as Boolean
    __iter_160 = elements.iterator_k_()
    while __iter_160.hasNext_k_()
        element = __iter_160.next_k_()
        if not m.contains_AnyN_k_(element) then
            return false
        end if
    end while

    return true
end function

function KeySet_iterator_k_() as Object
    return KeyIterator_create_HashMap_k_(m.get_map())
end function

function KeySet_add_AnyN_k_(element as Dynamic) as Boolean
    throw UnsupportedOperationException_create_StrN_k_("Add is not supported on keys")
end function

function KeySet_remove_AnyN_k_(element as Dynamic) as Boolean
    if not m.contains_AnyN_k_(element) then
        return false
    end if
    m.get_map().remove_AnyN_k_(element)
    return true
end function

function KeySet_addAll_Collection_k_(elements as Object) as Boolean
    throw UnsupportedOperationException_create_StrN_k_("Add is not supported on keys")
end function

function KeySet_removeAll_Collection_k_(elements as Object) as Boolean
    modified = false
    __iter_161 = elements.iterator_k_()
    while __iter_161.hasNext_k_()
        element = __iter_161.next_k_()
        if m.remove_AnyN_k_(element) then
            modified = true
        end if
    end while

    return modified
end function

function KeySet_retainAll_Collection_k_(elements as Object) as Boolean
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

sub KeySet_clear_k_()
    m.get_map().clear_k_()
end sub

function KeySet_get_map_k_() as Object
    return m.map
end function

function KeySet_get_array_k_() as Object
    keysArray = m.get_map().getKeysArray_k_()
    mapObj = m.get_map().getMap_k_()
    count = keysArray.Count()
    result = CreateObject("roArray", 0, true)
    i = 0
    while i < count
        keyStr = keysArray[i]
        entry = mapObj.Lookup(keyStr)
        result.Push(entry.k)
        i = (i + 1)
    end while
    return result
end function

function KeySet_get_size_k_() as Integer
    return m.get_map().get_size()
end function

function ValueCollection_create_HashMap_k_(map as Object) as Object
    this = {}
    this.__type = "ValueCollection"
    this.__proto = ["ValueCollection", "MutableCollection", "Collection", "Iterable", "MutableIterable"]
    this.__id = __kotlin_nextObjectId()
    this.isEmpty_k_ = ValueCollection_isEmpty_k_
    this.contains_AnyN_k_ = ValueCollection_contains_AnyN_k_
    this.containsAll_Collection_k_ = ValueCollection_containsAll_Collection_k_
    this.iterator_k_ = ValueCollection_iterator_k_
    this.add_AnyN_k_ = ValueCollection_add_AnyN_k_
    this.remove_AnyN_k_ = ValueCollection_remove_AnyN_k_
    this.addAll_Collection_k_ = ValueCollection_addAll_Collection_k_
    this.removeAll_Collection_k_ = ValueCollection_removeAll_Collection_k_
    this.retainAll_Collection_k_ = ValueCollection_retainAll_Collection_k_
    this.clear_k_ = ValueCollection_clear_k_
    this.get_map = ValueCollection_get_map_k_
    this.get_array = ValueCollection_get_array_k_
    this.get_size = ValueCollection_get_size_k_
    this.map = map
    return this
end function

function ValueCollection_isEmpty_k_() as Boolean
    return m.get_map().isEmpty_k_()
end function

function ValueCollection_contains_AnyN_k_(element as Dynamic) as Boolean
    return m.get_map().containsValue_AnyN_k_(element)
end function

function ValueCollection_containsAll_Collection_k_(elements as Object) as Boolean
    __iter_162 = elements.iterator_k_()
    while __iter_162.hasNext_k_()
        element = __iter_162.next_k_()
        if not m.contains_AnyN_k_(element) then
            return false
        end if
    end while

    return true
end function

function ValueCollection_iterator_k_() as Object
    return ValueIterator_create_HashMap_k_(m.get_map())
end function

function ValueCollection_add_AnyN_k_(element as Dynamic) as Boolean
    throw UnsupportedOperationException_create_StrN_k_("Add is not supported on values")
end function

function ValueCollection_remove_AnyN_k_(element as Dynamic) as Boolean
    iter = m.iterator_k_()
    while iter.hasNext_k_()
        if brsStructuralEquals_AnyN_AnyN_k_(iter.next_k_(), element) then
            iter.remove_k_()
            return true
        end if
    end while
    return false
end function

function ValueCollection_addAll_Collection_k_(elements as Object) as Boolean
    throw UnsupportedOperationException_create_StrN_k_("Add is not supported on values")
end function

function ValueCollection_removeAll_Collection_k_(elements as Object) as Boolean
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

function ValueCollection_retainAll_Collection_k_(elements as Object) as Boolean
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

sub ValueCollection_clear_k_()
    m.get_map().clear_k_()
end sub

function ValueCollection_get_map_k_() as Object
    return m.map
end function

function ValueCollection_get_array_k_() as Object
    keysArray = m.get_map().getKeysArray_k_()
    mapObj = m.get_map().getMap_k_()
    count = keysArray.Count()
    result = CreateObject("roArray", 0, true)
    i = 0
    while i < count
        keyStr = keysArray[i]
        entry = mapObj.Lookup(keyStr)
        result.Push(entry.v)
        i = (i + 1)
    end while
    return result
end function

function ValueCollection_get_size_k_() as Integer
    return m.get_map().get_size()
end function

function EntrySet_create_HashMap_k_(map as Object) as Object
    this = {}
    this.__type = "EntrySet"
    this.__proto = ["EntrySet", "MutableSet", "Set", "Collection", "Iterable", "MutableCollection", "MutableIterable"]
    this.__id = __kotlin_nextObjectId()
    this.isEmpty_k_ = EntrySet_isEmpty_k_
    this.contains_MutableEntry_k_ = EntrySet_contains_MutableEntry_k_
    this.containsAll_CollectionMutableEntry_k_ = EntrySet_containsAll_CollectionMutableEntry_k_
    this.iterator_k_ = EntrySet_iterator_k_
    this.add_MutableEntry_k_ = EntrySet_add_MutableEntry_k_
    this.remove_MutableEntry_k_ = EntrySet_remove_MutableEntry_k_
    this.addAll_CollectionMutableEntry_k_ = EntrySet_addAll_CollectionMutableEntry_k_
    this.removeAll_CollectionMutableEntry_k_ = EntrySet_removeAll_CollectionMutableEntry_k_
    this.retainAll_CollectionMutableEntry_k_ = EntrySet_retainAll_CollectionMutableEntry_k_
    this.clear_k_ = EntrySet_clear_k_
    this.get_map = EntrySet_get_map_k_
    this.get_array = EntrySet_get_array_k_
    this.get_size = EntrySet_get_size_k_
    this.map = map
    return this
end function

function EntrySet_isEmpty_k_() as Boolean
    return m.get_map().isEmpty_k_()
end function

function EntrySet_contains_MutableEntry_k_(element as Object) as Boolean
    value = m.get_map().get_AnyN_k_(element.get_key())
    return (value <> invalid) and brsStructuralEquals_AnyN_AnyN_k_(value, element.get_value())
end function

function EntrySet_containsAll_CollectionMutableEntry_k_(elements as Object) as Boolean
    __iter_163 = elements.iterator_k_()
    while __iter_163.hasNext_k_()
        element = __iter_163.next_k_()
        if not m.contains_MutableEntry_k_(element) then
            return false
        end if
    end while

    return true
end function

function EntrySet_iterator_k_() as Object
    return EntryIterator_create_HashMap_k_(m.get_map())
end function

function EntrySet_add_MutableEntry_k_(element as Object) as Boolean
    hadKey = m.get_map().containsKey_AnyN_k_(element.get_key())
    m.get_map().put_AnyN_AnyN_k_(element.get_key(), element.get_value())
    return not hadKey
end function

function EntrySet_remove_MutableEntry_k_(element as Object) as Boolean
    if not m.contains_MutableEntry_k_(element) then
        return false
    end if
    m.get_map().remove_AnyN_k_(element.get_key())
    return true
end function

function EntrySet_addAll_CollectionMutableEntry_k_(elements as Object) as Boolean
    modified = false
    __iter_164 = elements.iterator_k_()
    while __iter_164.hasNext_k_()
        element = __iter_164.next_k_()
        if m.add_MutableEntry_k_(element) then
            modified = true
        end if
    end while

    return modified
end function

function EntrySet_removeAll_CollectionMutableEntry_k_(elements as Object) as Boolean
    modified = false
    __iter_165 = elements.iterator_k_()
    while __iter_165.hasNext_k_()
        element = __iter_165.next_k_()
        if m.remove_MutableEntry_k_(element) then
            modified = true
        end if
    end while

    return modified
end function

function EntrySet_retainAll_CollectionMutableEntry_k_(elements as Object) as Boolean
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

sub EntrySet_clear_k_()
    m.get_map().clear_k_()
end sub

function EntrySet_get_map_k_() as Object
    return m.map
end function

function EntrySet_get_array_k_() as Object
    keysArray = m.get_map().getKeysArray_k_()
    mapObj = m.get_map().getMap_k_()
    count = keysArray.Count()
    result = CreateObject("roArray", 0, true)
    i = 0
    while i < count
        keyStr = keysArray[i]
        entry = mapObj.Lookup(keyStr)
        key = entry.k
        result.Push(HashMapEntry_create_HashMap_AnyN_k_(m.get_map(), key))
        i = (i + 1)
    end while
    return result
end function

function EntrySet_get_size_k_() as Integer
    return m.get_map().get_size()
end function

function HashMapIterator_create_HashMap_k_(map as Object) as Object
    this = {}
    this.__type = "HashMapIterator"
    this.__proto = ["HashMapIterator", "MutableIterator", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.getKeysArrayCached_k_ = HashMapIterator_getKeysArrayCached_k_
    this.getKeyCountCached_k_ = HashMapIterator_getKeyCountCached_k_
    this.hasNext_k_ = HashMapIterator_hasNext_k_
    this.nextKey_k_ = HashMapIterator_nextKey_k_
    this.remove_k_ = HashMapIterator_remove_k_
    this.get_map = HashMapIterator_get_map_k_
    this.get__keysArray = HashMapIterator_get__keysArray_k_
    this.set__keysArray = HashMapIterator_set__keysArray_DynamicN_k_
    this.get__keyCount = HashMapIterator_get__keyCount_k_
    this.set__keyCount = HashMapIterator_set__keyCount_I_k_
    this.get_index = HashMapIterator_get_index_k_
    this.set_index = HashMapIterator_set_index_I_k_
    this.get_lastKey = HashMapIterator_get_lastKey_k_
    this.set_lastKey = HashMapIterator_set_lastKey_AnyN_k_
    this.map = map
    this._keysArray = invalid
    this._keyCount = -1
    this.index = 0
    this.lastKey = invalid
    return this
end function

function HashMapIterator_getKeysArrayCached_k_() as Object
    arr = m.get__keysArray()
    if arr = invalid then
        arr = m.get_map().getKeysArray_k_()
        m.set__keysArray(arr)
    end if
    return arr
end function

function HashMapIterator_getKeyCountCached_k_() as Integer
    count = m.get__keyCount()
    if count < 0 then
        count = m.getKeysArrayCached_k_().Count()
        m.set__keyCount(count)
    end if
    return count
end function

function HashMapIterator_hasNext_k_() as Boolean
    return m.get_index() < m.getKeyCountCached_k_()
end function

function HashMapIterator_nextKey_k_() as Dynamic
    if not m.hasNext_k_() then
        throw NoSuchElementException_create_k_()
    end if
    keyStr = m.getKeysArrayCached_k_()[m.get_index()]
    entry = m.get_map().get_map().Lookup(keyStr)
    key = entry.k
    m.set_index(m.get_index() + 1)
    m.set_lastKey(key)
    return key
end function

sub HashMapIterator_remove_k_()
    tmp0_elvis_lhs = m.get_lastKey()
    __when_tmp4 = invalid
    if tmp0_elvis_lhs = invalid then
        throw IllegalStateException_create_StrN_k_("Call next() before remove()")
    else if true then
        __when_tmp4 = tmp0_elvis_lhs
    end if
    key = __when_tmp4

    m.get_map().remove_AnyN_k_(key)
    m.set_lastKey(invalid)
end sub

function HashMapIterator_get_map_k_() as Object
    return m.map
end function

function HashMapIterator_get__keysArray_k_() as Dynamic
    return m._keysArray
end function

sub HashMapIterator_set__keysArray_DynamicN_k_(value as Dynamic)
    m._keysArray = value
end sub

function HashMapIterator_get__keyCount_k_() as Integer
    return m._keyCount
end function

sub HashMapIterator_set__keyCount_I_k_(value as Integer)
    m._keyCount = value
end sub

function HashMapIterator_get_index_k_() as Integer
    return m.index
end function

sub HashMapIterator_set_index_I_k_(value as Integer)
    m.index = value
end sub

function HashMapIterator_get_lastKey_k_() as Dynamic
    return m.lastKey
end function

sub HashMapIterator_set_lastKey_AnyN_k_(value as Dynamic)
    m.lastKey = value
end sub

function KeyIterator_create_HashMap_k_(map as Object) as Object
    this = HashMapIterator_create_HashMap_k_(map)
    this._super = {}
    this._super.next_k_ = this.next_k_
    this.__proto = ["KeyIterator", this.__proto]
    this.__type = "KeyIterator"
    this.next_k_ = KeyIterator_next_k_
    return this
end function

function KeyIterator_next_k_() as Dynamic
    return m.nextKey_k_()
end function

function ValueIterator_create_HashMap_k_(map as Object) as Object
    this = HashMapIterator_create_HashMap_k_(map)
    this._super = {}
    this._super.next_k_ = this.next_k_
    this.__proto = ["ValueIterator", this.__proto]
    this.__type = "ValueIterator"
    this.next_k_ = ValueIterator_next_k_
    return this
end function

function ValueIterator_next_k_() as Dynamic
    return m.get_map().get_AnyN_k_(m.nextKey_k_())
end function

function EntryIterator_create_HashMap_k_(map as Object) as Object
    this = HashMapIterator_create_HashMap_k_(map)
    this._super = {}
    this._super.next_k_ = this.next_k_
    this.__proto = ["EntryIterator", this.__proto]
    this.__type = "EntryIterator"
    this.next_k_ = EntryIterator_next_k_
    return this
end function

function EntryIterator_next_k_() as Object
    return HashMapEntry_create_HashMap_AnyN_k_(m.get_map(), m.nextKey_k_())
end function

function hashMapOf_k_() as Object
    return HashMap_create_k_()
end function

function hashMapOf_Arr_k_(pairs as Object) as Object
    map = HashMap_create_I_k_(pairs.count())
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

function mutableMapOf_k_() as Object
    return HashMap_create_k_()
end function

function mutableMapOf_Arr_k_(pairs as Object) as Object
    return hashMapOf_Arr_k_(pairs)
end function

function mapOf_k_() as Object
    return emptyMap_k_()
end function

function mapOf_Arr_k_(pairs as Object) as Object
    __when_tmp5 = invalid
    if pairs.count() = 0 then
        __when_tmp5 = emptyMap_k_()
    else if true then
        __when_tmp5 = hashMapOf_Arr_k_(pairs)
    end if
    return __when_tmp5

end function

function emptyMap_k_() as Object
    return EmptyMap_getInstance()
end function

function EmptyMap_create_k_() as Object
    this = {}
    this.__type = "EmptyMap"
    this.__proto = ["EmptyMap", "Map"]
    this.__id = __kotlin_nextObjectId()
    this.isEmpty_k_ = EmptyMap_isEmpty_k_
    this.containsKey_AnyN_k_ = EmptyMap_containsKey_AnyN_k_
    this.containsValue_AnyN_k_ = EmptyMap_containsValue_AnyN_k_
    this.get_AnyN_k_ = EmptyMap_get_AnyN_k_
    this.equals_AnyN_k_ = EmptyMap_equals_AnyN_k_
    this.equals = EmptyMap_equals_AnyN_k_
    this.hashCode_k_ = EmptyMap_hashCode_k_
    this.hashCode = EmptyMap_hashCode_k_
    this.toString_k_ = EmptyMap_toString_k_
    this.toString = EmptyMap_toString_k_
    this.get_size = EmptyMap_get_size_k_
    this.get_keys = EmptyMap_get_keys_k_
    this.get_values = EmptyMap_get_values_k_
    this.get_entries = EmptyMap_get_entries_k_
    return this
end function

function EmptyMap_getInstance() as Object
    if m.EmptyMap_instance = invalid then
        m.EmptyMap_instance = EmptyMap_create_k_()
    end if
    return m.EmptyMap_instance
end function

function EmptyMap_isEmpty_k_() as Boolean
    return true
end function

function EmptyMap_containsKey_AnyN_k_(key as Dynamic) as Boolean
    return false
end function

function EmptyMap_containsValue_AnyN_k_(value as Dynamic) as Boolean
    return false
end function

function EmptyMap_get_AnyN_k_(key as Dynamic) as Dynamic
    return invalid
end function

function EmptyMap_equals_AnyN_k_(other as Dynamic) as Boolean
    return __kotlin_isInstanceOf(other, "Map") and other.isEmpty_k_()
end function

function EmptyMap_hashCode_k_() as Integer
    return 0
end function

function EmptyMap_toString_k_() as String
    return "{}"
end function

function EmptyMap_get_size_k_() as Integer
    return 0
end function

function EmptyMap_get_keys_k_() as Object
    return EmptySet_getInstance()
end function

function EmptyMap_get_values_k_() as Object
    return emptyList_k_()
end function

function EmptyMap_get_entries_k_() as Object
    return EmptyEntrySet_getInstance()
end function

function EmptySet_create_k_() as Object
    this = {}
    this.__type = "EmptySet"
    this.__proto = ["EmptySet", "Set", "Collection", "Iterable"]
    this.__id = __kotlin_nextObjectId()
    this.isEmpty_k_ = EmptySet_isEmpty_k_
    this.contains_AnyN_k_ = EmptySet_contains_AnyN_k_
    this.containsAll_CollectionAnyN_k_ = EmptySet_containsAll_CollectionAnyN_k_
    this.iterator_k_ = EmptySet_iterator_k_
    this.get_array = EmptySet_get_array_k_
    this.get_size = EmptySet_get_size_k_
    return this
end function

function EmptySet_getInstance() as Object
    if m.EmptySet_instance = invalid then
        m.EmptySet_instance = EmptySet_create_k_()
    end if
    return m.EmptySet_instance
end function

function EmptySet_isEmpty_k_() as Boolean
    return true
end function

function EmptySet_contains_AnyN_k_(element as Dynamic) as Boolean
    return false
end function

function EmptySet_containsAll_CollectionAnyN_k_(elements as Object) as Boolean
    return elements.isEmpty_k_()
end function

function EmptySet_iterator_k_() as Object
    return EmptyMapIterator_getInstance()
end function

function EmptySet_get_array_k_() as Object
    return CreateObject("roArray", 0, true)
end function

function EmptySet_get_size_k_() as Integer
    return 0
end function

function EmptyMapIterator_create_k_() as Object
    this = {}
    this.__type = "EmptyMapIterator"
    this.__proto = ["EmptyMapIterator", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.hasNext_k_ = EmptyMapIterator_hasNext_k_
    this.next_k_ = EmptyMapIterator_next_k_
    return this
end function

function EmptyMapIterator_getInstance() as Object
    if m.EmptyMapIterator_instance = invalid then
        m.EmptyMapIterator_instance = EmptyMapIterator_create_k_()
    end if
    return m.EmptyMapIterator_instance
end function

function EmptyMapIterator_hasNext_k_() as Boolean
    return false
end function

function EmptyMapIterator_next_k_() as Dynamic
    throw NoSuchElementException_create_k_()
end function

function EmptyEntrySet_create_k_() as Object
    this = {}
    this.__type = "EmptyEntrySet"
    this.__proto = ["EmptyEntrySet", "Set", "Collection", "Iterable"]
    this.__id = __kotlin_nextObjectId()
    this.isEmpty_k_ = EmptyEntrySet_isEmpty_k_
    this.contains_EntryAnyNAnyN_k_ = EmptyEntrySet_contains_EntryAnyNAnyN_k_
    this.containsAll_CollectionEntryAnyNAnyN_k_ = EmptyEntrySet_containsAll_CollectionEntryAnyNAnyN_k_
    this.iterator_k_ = EmptyEntrySet_iterator_k_
    this.get_array = EmptyEntrySet_get_array_k_
    this.get_size = EmptyEntrySet_get_size_k_
    return this
end function

function EmptyEntrySet_getInstance() as Object
    if m.EmptyEntrySet_instance = invalid then
        m.EmptyEntrySet_instance = EmptyEntrySet_create_k_()
    end if
    return m.EmptyEntrySet_instance
end function

function EmptyEntrySet_isEmpty_k_() as Boolean
    return true
end function

function EmptyEntrySet_contains_EntryAnyNAnyN_k_(element as Object) as Boolean
    return false
end function

function EmptyEntrySet_containsAll_CollectionEntryAnyNAnyN_k_(elements as Object) as Boolean
    return elements.isEmpty_k_()
end function

function EmptyEntrySet_iterator_k_() as Object
    return EmptyEntryIterator_getInstance()
end function

function EmptyEntrySet_get_array_k_() as Object
    return CreateObject("roArray", 0, true)
end function

function EmptyEntrySet_get_size_k_() as Integer
    return 0
end function

function EmptyEntryIterator_create_k_() as Object
    this = {}
    this.__type = "EmptyEntryIterator"
    this.__proto = ["EmptyEntryIterator", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.hasNext_k_ = EmptyEntryIterator_hasNext_k_
    this.next_k_ = EmptyEntryIterator_next_k_
    return this
end function

function EmptyEntryIterator_getInstance() as Object
    if m.EmptyEntryIterator_instance = invalid then
        m.EmptyEntryIterator_instance = EmptyEntryIterator_create_k_()
    end if
    return m.EmptyEntryIterator_instance
end function

function EmptyEntryIterator_hasNext_k_() as Boolean
    return false
end function

function EmptyEntryIterator_next_k_() as Object
    throw NoSuchElementException_create_k_()
end function
