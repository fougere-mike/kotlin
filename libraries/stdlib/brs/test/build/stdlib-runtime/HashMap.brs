function HashMap_create_HashMapAnyNAnyN_k_() as Object
    this = {}
    this.__type = "HashMap"
    this.__proto = ["HashMap"]
    this._size = 0
    this.isEmpty_Z_k_ = HashMap_isEmpty_Z_k_
    this.containsKey_AnyN_Z_k_ = HashMap_containsKey_AnyN_Z_k_
    this.containsValue_AnyN_Z_k_ = HashMap_containsValue_AnyN_Z_k_
    this.get_AnyN_AnyN_k_ = HashMap_get_AnyN_AnyN_k_
    this.put_AnyN_AnyN_AnyN_k_ = HashMap_put_AnyN_AnyN_AnyN_k_
    this.remove_AnyN_AnyN_k_ = HashMap_remove_AnyN_AnyN_k_
    this.putAll_MapAnyNAnyN_k_ = HashMap_putAll_MapAnyNAnyN_k_
    this.clear = HashMap_clear
    this.equals_AnyN_Z_k_ = HashMap_equals_AnyN_Z_k_
    this.hashCode_I_k_ = HashMap_hashCode_I_k_
    this.toString_Str_k_ = HashMap_toString_Str_k_
    this.keyToString_AnyN_Str_k_ = HashMap_keyToString_AnyN_Str_k_
    this.getKeysArray_Dynamic_k_ = HashMap_getKeysArray_Dynamic_k_
    this.getMap_Dynamic_k_ = HashMap_getMap_Dynamic_k_
    this.get_map = HashMap_get_map_Dynamic_k_
    this.set_map = HashMap_set_map_Dynamic_k_
    this.get__size = HashMap_get__size_I_k_
    this.set__size = HashMap_set__size_I_k_
    this.get_size = HashMap_get_size_I_k_
    this.get_keys = HashMap_get_keys_MutableSetAnyN_k_
    this.get_values = HashMap_get_values_MutableCollectionAnyN_k_
    this.get_entries = HashMap_get_entries_MutableSetMutableEntryAnyNAnyN_k_
    m.map = CreateObject("roAssociativeArray")
    m._size = 0
    return this
end function

function HashMap_create_I_HashMapAnyNAnyN_k_(initialCapacity as Integer) as Object
    this = {}
    this.__type = "HashMap"
    this.__proto = ["HashMap"]
    this._size = 0
    this.isEmpty_Z_k_ = HashMap_isEmpty_Z_k_
    this.containsKey_AnyN_Z_k_ = HashMap_containsKey_AnyN_Z_k_
    this.containsValue_AnyN_Z_k_ = HashMap_containsValue_AnyN_Z_k_
    this.get_AnyN_AnyN_k_ = HashMap_get_AnyN_AnyN_k_
    this.put_AnyN_AnyN_AnyN_k_ = HashMap_put_AnyN_AnyN_AnyN_k_
    this.remove_AnyN_AnyN_k_ = HashMap_remove_AnyN_AnyN_k_
    this.putAll_MapAnyNAnyN_k_ = HashMap_putAll_MapAnyNAnyN_k_
    this.clear = HashMap_clear
    this.equals_AnyN_Z_k_ = HashMap_equals_AnyN_Z_k_
    this.hashCode_I_k_ = HashMap_hashCode_I_k_
    this.toString_Str_k_ = HashMap_toString_Str_k_
    this.keyToString_AnyN_Str_k_ = HashMap_keyToString_AnyN_Str_k_
    this.getKeysArray_Dynamic_k_ = HashMap_getKeysArray_Dynamic_k_
    this.getMap_Dynamic_k_ = HashMap_getMap_Dynamic_k_
    this.get_map = HashMap_get_map_Dynamic_k_
    this.set_map = HashMap_set_map_Dynamic_k_
    this.get__size = HashMap_get__size_I_k_
    this.set__size = HashMap_set__size_I_k_
    this.get_size = HashMap_get_size_I_k_
    this.get_keys = HashMap_get_keys_MutableSetAnyN_k_
    this.get_values = HashMap_get_values_MutableCollectionAnyN_k_
    this.get_entries = HashMap_get_entries_MutableSetMutableEntryAnyNAnyN_k_
    require_Z_Function0Any_k_(initialCapacity >= 0, {initialCapacity: initialCapacity, invoke: function() as Object
        return "Negative initial capacity: " + m.initialCapacity
    end function})
    m.map = CreateObject("roAssociativeArray")
    m._size = 0
    return this
end function

function HashMap_create_MapAnyNAnyN_HashMapAnyNAnyN_k_(original as Object) as Object
    this = {}
    this.__type = "HashMap"
    this.__proto = ["HashMap"]
    this._size = 0
    this.isEmpty_Z_k_ = HashMap_isEmpty_Z_k_
    this.containsKey_AnyN_Z_k_ = HashMap_containsKey_AnyN_Z_k_
    this.containsValue_AnyN_Z_k_ = HashMap_containsValue_AnyN_Z_k_
    this.get_AnyN_AnyN_k_ = HashMap_get_AnyN_AnyN_k_
    this.put_AnyN_AnyN_AnyN_k_ = HashMap_put_AnyN_AnyN_AnyN_k_
    this.remove_AnyN_AnyN_k_ = HashMap_remove_AnyN_AnyN_k_
    this.putAll_MapAnyNAnyN_k_ = HashMap_putAll_MapAnyNAnyN_k_
    this.clear = HashMap_clear
    this.equals_AnyN_Z_k_ = HashMap_equals_AnyN_Z_k_
    this.hashCode_I_k_ = HashMap_hashCode_I_k_
    this.toString_Str_k_ = HashMap_toString_Str_k_
    this.keyToString_AnyN_Str_k_ = HashMap_keyToString_AnyN_Str_k_
    this.getKeysArray_Dynamic_k_ = HashMap_getKeysArray_Dynamic_k_
    this.getMap_Dynamic_k_ = HashMap_getMap_Dynamic_k_
    this.get_map = HashMap_get_map_Dynamic_k_
    this.set_map = HashMap_set_map_Dynamic_k_
    this.get__size = HashMap_get__size_I_k_
    this.set__size = HashMap_set__size_I_k_
    this.get_size = HashMap_get_size_I_k_
    this.get_keys = HashMap_get_keys_MutableSetAnyN_k_
    this.get_values = HashMap_get_values_MutableCollectionAnyN_k_
    this.get_entries = HashMap_get_entries_MutableSetMutableEntryAnyNAnyN_k_
    m.map = CreateObject("roAssociativeArray")
    m._size = 0
    m.putAll(original)
    return this
end function

function HashMap_isEmpty_Z_k_() as Boolean
    return m._size = 0
end function

function HashMap_containsKey_AnyN_Z_k_(key as Dynamic) as Boolean
    keyStr = m.keyToString(key)
    return m.DoesExist(keyStr)
end function

function HashMap_containsValue_AnyN_Z_k_(value as Dynamic) as Boolean
    keysArray = m.Keys()
    count = keysArray.Count()
    i = 0
    while i < count
        keyStr = keysArray[i]
        v = m.Lookup(keyStr)
        if v = value then
            return true
        end if
        i = (i + 1)
    end while
    return false
end function

function HashMap_get_AnyN_AnyN_k_(key as Dynamic) as Dynamic
    keyStr = m.keyToString(key)
    if m.DoesExist(keyStr).not() then
        return invalid
    end if
    return m.Lookup(keyStr)
end function

function HashMap_put_AnyN_AnyN_AnyN_k_(key as Dynamic, value as Dynamic) as Dynamic
    keyStr = m.keyToString(key)
    __when_tmp0 = invalid
    if m.DoesExist(keyStr) then
        __when_tmp0 = m.Lookup(keyStr)
    else if true then
        __when_tmp0 = invalid
    end if
    oldValue = __when_tmp0

    m.AddReplace(keyStr, value)
    return oldValue
end function

function HashMap_remove_AnyN_AnyN_k_(key as Dynamic) as Dynamic
    keyStr = m.keyToString(key)
    if m.DoesExist(keyStr).not() then
        return invalid
    end if
    oldValue = m.Lookup(keyStr)
    m.Delete(keyStr)
    m._size = (m._size - 1)
    return oldValue
end function

sub HashMap_putAll_MapAnyNAnyN_k_(from as Object)
    for each entry in from.entries
        m.put(entry.key, entry.value)

    end for
end sub

sub HashMap_clear()
    m.Clear()
    m._size = 0
end sub

function HashMap_equals_AnyN_Z_k_(other as Dynamic) as Boolean
    if EQEQEQ_AnyN_AnyN_Z_k_(other, m) then
        return true
    end if
    if not __kotlin_isInstanceOf(other, "Map") then
        return false
    end if
    if other.size <> m.size then
        return false
    end if
    for each entry in m.entries
        otherMap = other
        otherValue = otherMap[entry.key]
        if entry.value <> otherValue then
            return false
        end if

    end for
    return true
end function

function HashMap_hashCode_I_k_() as Integer
    h = 0
    for each entry in m.entries
        h = (h + entry.hashCode())

    end for
    return h
end function

function HashMap_toString_Str_k_() as String
    if m.isEmpty() then
        return "{}"
    end if
    sb = StringBuilder_create_StringBuilder_k_()
    sb.append("{")
    first = true
    for each entry in m.entries
        if first.not() then
            sb.append(", ")
        end if
        first = false
        k = entry.key
        v = entry.value
                if EQEQEQ_AnyN_AnyN_Z_k_(k, m) then
            sb.append("(this Map)")
        else if true then
            sb.append(k.toString())
        end if
        sb.append("=")
        if EQEQEQ_AnyN_AnyN_Z_k_(v, m) then
            sb.append("(this Map)")
        else if true then
            sb.append(v.toString())
        end if

    end for
    sb.append("}")
    return sb.toString()
end function

function HashMap_keyToString_AnyN_Str_k_(key as Dynamic) as String
    return key.toString()
end function

function HashMap_getKeysArray_Dynamic_k_() as Object
    return m.Keys()
end function

function HashMap_getMap_Dynamic_k_() as Object
    return m.map
end function

function HashMap_get_map_Dynamic_k_() as Object
    return m.map
end function

sub HashMap_set_map_Dynamic_k_(value as Object)
    m.map = value
end sub

function HashMap_get__size_I_k_() as Integer
    return m._size
end function

sub HashMap_set__size_I_k_(value as Integer)
    m._size = value
end sub

function HashMap_get_size_I_k_() as Integer
    return m._size
end function

function HashMap_get_keys_MutableSetAnyN_k_() as Object
    return KeySet_create_HashMapAnyNAnyN_KeySetAnyNAnyN_k_(m)
end function

function HashMap_get_values_MutableCollectionAnyN_k_() as Object
    return ValueCollection_create_HashMapAnyNAnyN_ValueCollectionAnyNAnyN_k_(m)
end function

function HashMap_get_entries_MutableSetMutableEntryAnyNAnyN_k_() as Object
    return EntrySet_create_HashMapAnyNAnyN_EntrySetAnyNAnyN_k_(m)
end function

function HashMapEntry_create_HashMapAnyNAnyN_AnyN_HashMapEntryAnyNAnyN_k_(map as Object, key as Dynamic) as Object
    this = {}
    this.__type = "HashMapEntry"
    this.__proto = ["HashMapEntry"]
    this.map = map
    this.key = key
    this.setValue_AnyN_AnyN_k_ = HashMapEntry_setValue_AnyN_AnyN_k_
    this.equals_AnyN_Z_k_ = HashMapEntry_equals_AnyN_Z_k_
    this.hashCode_I_k_ = HashMapEntry_hashCode_I_k_
    this.toString_Str_k_ = HashMapEntry_toString_Str_k_
    this.get_map = HashMapEntry_get_map_HashMapAnyNAnyN_k_
    this.get_key = HashMapEntry_get_key_AnyN_k_
    this.get_value = HashMapEntry_get_value_AnyN_k_
    return this
end function

function HashMapEntry_setValue_AnyN_AnyN_k_(newValue as Dynamic) as Dynamic
    old = m.map.put(m.key, newValue)
    return CHECK_NOT_NULL_AnyN_Any_k_(old)
end function

function HashMapEntry_equals_AnyN_Z_k_(other as Dynamic) as Boolean
    if not __kotlin_isInstanceOf(other, "Map_Entry") then
        return false
    end if
    return (m.key = other.key) and (m.value = other.value)
end function

function HashMapEntry_hashCode_I_k_() as Integer
    tmp0_safe_receiver = m.key
    __when_tmp1 = invalid
    if tmp0_safe_receiver = invalid then
        __when_tmp1 = invalid
    else if true then
        __when_tmp1 = tmp0_safe_receiver.hashCode()
    end if
    tmp1_elvis_lhs = __when_tmp1
    __when_tmp2 = invalid
    if tmp1_elvis_lhs = invalid then
        __when_tmp2 = 0
    else if true then
        __when_tmp2 = tmp1_elvis_lhs
    end if
    tmp2_safe_receiver = m.value
    __when_tmp3 = invalid
    if tmp2_safe_receiver = invalid then
        __when_tmp3 = invalid
    else if true then
        __when_tmp3 = tmp2_safe_receiver.hashCode()
    end if
    tmp3_elvis_lhs = __when_tmp3
    __when_tmp4 = invalid
    if tmp3_elvis_lhs = invalid then
        __when_tmp4 = 0
    else if true then
        __when_tmp4 = tmp3_elvis_lhs
    end if
    return __when_tmp2.xor(__when_tmp4)

end function

function HashMapEntry_toString_Str_k_() as String
    return (m.key + "=") + m.value
end function

function HashMapEntry_get_map_HashMapAnyNAnyN_k_() as Object
    return m.map
end function

function HashMapEntry_get_key_AnyN_k_() as Dynamic
    return m.key
end function

function HashMapEntry_get_value_AnyN_k_() as Dynamic
    return CHECK_NOT_NULL_AnyN_Any_k_(m.map[m.key])
end function

function KeySet_create_HashMapAnyNAnyN_KeySetAnyNAnyN_k_(map as Object) as Object
    this = {}
    this.__type = "KeySet"
    this.__proto = ["KeySet"]
    this.map = map
    this.isEmpty_Z_k_ = KeySet_isEmpty_Z_k_
    this.contains_AnyN_Z_k_ = KeySet_contains_AnyN_Z_k_
    this.containsAll_CollectionAnyN_Z_k_ = KeySet_containsAll_CollectionAnyN_Z_k_
    this.iterator_MutableIteratorAnyN_k_ = KeySet_iterator_MutableIteratorAnyN_k_
    this.add_AnyN_Z_k_ = KeySet_add_AnyN_Z_k_
    this.remove_AnyN_Z_k_ = KeySet_remove_AnyN_Z_k_
    this.addAll_CollectionAnyN_Z_k_ = KeySet_addAll_CollectionAnyN_Z_k_
    this.removeAll_CollectionAnyN_Z_k_ = KeySet_removeAll_CollectionAnyN_Z_k_
    this.retainAll_CollectionAnyN_Z_k_ = KeySet_retainAll_CollectionAnyN_Z_k_
    this.clear = KeySet_clear
    this.get_map = KeySet_get_map_HashMapAnyNAnyN_k_
    this.get_size = KeySet_get_size_I_k_
    return this
end function

function KeySet_isEmpty_Z_k_() as Boolean
    return m.map.isEmpty()
end function

function KeySet_contains_AnyN_Z_k_(element as Dynamic) as Boolean
    return m.map.containsKey(element)
end function

function KeySet_containsAll_CollectionAnyN_Z_k_(elements as Object) as Boolean
    for each element in elements
        if m.contains(element).not() then
            return false
        end if
    end for
    return true
end function

function KeySet_iterator_MutableIteratorAnyN_k_() as Object
    return KeyIterator_create_HashMapAnyNAnyN_KeyIteratorAnyNAnyN_k_(m.map)
end function

function KeySet_add_AnyN_Z_k_(element as Dynamic) as Boolean
    throw UnsupportedOperationException_create_StrN_UnsupportedOperationException_k_("Add is not supported on keys")
end function

function KeySet_remove_AnyN_Z_k_(element as Dynamic) as Boolean
    if m.contains(element).not() then
        return false
    end if
    m.map.remove(element)
    return true
end function

function KeySet_addAll_CollectionAnyN_Z_k_(elements as Object) as Boolean
    throw UnsupportedOperationException_create_StrN_UnsupportedOperationException_k_("Add is not supported on keys")
end function

function KeySet_removeAll_CollectionAnyN_Z_k_(elements as Object) as Boolean
    modified = false
    for each element in elements
        if m.remove(element) then
            modified = true
        end if
    end for
    return modified
end function

function KeySet_retainAll_CollectionAnyN_Z_k_(elements as Object) as Boolean
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

sub KeySet_clear()
    m.map.clear()
end sub

function KeySet_get_map_HashMapAnyNAnyN_k_() as Object
    return m.map
end function

function KeySet_get_size_I_k_() as Integer
    return m.map.size
end function

function ValueCollection_create_HashMapAnyNAnyN_ValueCollectionAnyNAnyN_k_(map as Object) as Object
    this = {}
    this.__type = "ValueCollection"
    this.__proto = ["ValueCollection"]
    this.map = map
    this.isEmpty_Z_k_ = ValueCollection_isEmpty_Z_k_
    this.contains_AnyN_Z_k_ = ValueCollection_contains_AnyN_Z_k_
    this.containsAll_CollectionAnyN_Z_k_ = ValueCollection_containsAll_CollectionAnyN_Z_k_
    this.iterator_MutableIteratorAnyN_k_ = ValueCollection_iterator_MutableIteratorAnyN_k_
    this.add_AnyN_Z_k_ = ValueCollection_add_AnyN_Z_k_
    this.remove_AnyN_Z_k_ = ValueCollection_remove_AnyN_Z_k_
    this.addAll_CollectionAnyN_Z_k_ = ValueCollection_addAll_CollectionAnyN_Z_k_
    this.removeAll_CollectionAnyN_Z_k_ = ValueCollection_removeAll_CollectionAnyN_Z_k_
    this.retainAll_CollectionAnyN_Z_k_ = ValueCollection_retainAll_CollectionAnyN_Z_k_
    this.clear = ValueCollection_clear
    this.get_map = ValueCollection_get_map_HashMapAnyNAnyN_k_
    this.get_size = ValueCollection_get_size_I_k_
    return this
end function

function ValueCollection_isEmpty_Z_k_() as Boolean
    return m.map.isEmpty()
end function

function ValueCollection_contains_AnyN_Z_k_(element as Dynamic) as Boolean
    return m.map.containsValue(element)
end function

function ValueCollection_containsAll_CollectionAnyN_Z_k_(elements as Object) as Boolean
    for each element in elements
        if m.contains(element).not() then
            return false
        end if
    end for
    return true
end function

function ValueCollection_iterator_MutableIteratorAnyN_k_() as Object
    return ValueIterator_create_HashMapAnyNAnyN_ValueIteratorAnyNAnyN_k_(m.map)
end function

function ValueCollection_add_AnyN_Z_k_(element as Dynamic) as Boolean
    throw UnsupportedOperationException_create_StrN_UnsupportedOperationException_k_("Add is not supported on values")
end function

function ValueCollection_remove_AnyN_Z_k_(element as Dynamic) as Boolean
    iter = m.iterator()
    while iter.hasNext()
        if iter.next() = element then
            iter.remove()
            return true
        end if
    end while
    return false
end function

function ValueCollection_addAll_CollectionAnyN_Z_k_(elements as Object) as Boolean
    throw UnsupportedOperationException_create_StrN_UnsupportedOperationException_k_("Add is not supported on values")
end function

function ValueCollection_removeAll_CollectionAnyN_Z_k_(elements as Object) as Boolean
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

function ValueCollection_retainAll_CollectionAnyN_Z_k_(elements as Object) as Boolean
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

sub ValueCollection_clear()
    m.map.clear()
end sub

function ValueCollection_get_map_HashMapAnyNAnyN_k_() as Object
    return m.map
end function

function ValueCollection_get_size_I_k_() as Integer
    return m.map.size
end function

function EntrySet_create_HashMapAnyNAnyN_EntrySetAnyNAnyN_k_(map as Object) as Object
    this = {}
    this.__type = "EntrySet"
    this.__proto = ["EntrySet"]
    this.map = map
    this.isEmpty_Z_k_ = EntrySet_isEmpty_Z_k_
    this.contains_MutableEntryAnyNAnyN_Z_k_ = EntrySet_contains_MutableEntryAnyNAnyN_Z_k_
    this.containsAll_CollectionMutableEntryAnyNAnyN_Z_k_ = EntrySet_containsAll_CollectionMutableEntryAnyNAnyN_Z_k_
    this.iterator_MutableIteratorMutableEntryAnyNAnyN_k_ = EntrySet_iterator_MutableIteratorMutableEntryAnyNAnyN_k_
    this.add_MutableEntryAnyNAnyN_Z_k_ = EntrySet_add_MutableEntryAnyNAnyN_Z_k_
    this.remove_MutableEntryAnyNAnyN_Z_k_ = EntrySet_remove_MutableEntryAnyNAnyN_Z_k_
    this.addAll_CollectionMutableEntryAnyNAnyN_Z_k_ = EntrySet_addAll_CollectionMutableEntryAnyNAnyN_Z_k_
    this.removeAll_CollectionMutableEntryAnyNAnyN_Z_k_ = EntrySet_removeAll_CollectionMutableEntryAnyNAnyN_Z_k_
    this.retainAll_CollectionMutableEntryAnyNAnyN_Z_k_ = EntrySet_retainAll_CollectionMutableEntryAnyNAnyN_Z_k_
    this.clear = EntrySet_clear
    this.get_map = EntrySet_get_map_HashMapAnyNAnyN_k_
    this.get_size = EntrySet_get_size_I_k_
    return this
end function

function EntrySet_isEmpty_Z_k_() as Boolean
    return m.map.isEmpty()
end function

function EntrySet_contains_MutableEntryAnyNAnyN_Z_k_(element as Object) as Boolean
    value = m.map[element.key]
    return (value <> invalid) and (value = element.value)
end function

function EntrySet_containsAll_CollectionMutableEntryAnyNAnyN_Z_k_(elements as Object) as Boolean
    for each element in elements
        if m.contains(element).not() then
            return false
        end if
    end for
    return true
end function

function EntrySet_iterator_MutableIteratorMutableEntryAnyNAnyN_k_() as Object
    return EntryIterator_create_HashMapAnyNAnyN_EntryIteratorAnyNAnyN_k_(m.map)
end function

function EntrySet_add_MutableEntryAnyNAnyN_Z_k_(element as Object) as Boolean
    hadKey = m.map.containsKey(element.key)
    m.map.put(element.key, element.value)
    return hadKey.not()
end function

function EntrySet_remove_MutableEntryAnyNAnyN_Z_k_(element as Object) as Boolean
    if m.contains(element).not() then
        return false
    end if
    m.map.remove(element.key)
    return true
end function

function EntrySet_addAll_CollectionMutableEntryAnyNAnyN_Z_k_(elements as Object) as Boolean
    modified = false
    for each element in elements
        if m.add(element) then
            modified = true
        end if
    end for
    return modified
end function

function EntrySet_removeAll_CollectionMutableEntryAnyNAnyN_Z_k_(elements as Object) as Boolean
    modified = false
    for each element in elements
        if m.remove(element) then
            modified = true
        end if
    end for
    return modified
end function

function EntrySet_retainAll_CollectionMutableEntryAnyNAnyN_Z_k_(elements as Object) as Boolean
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

sub EntrySet_clear()
    m.map.clear()
end sub

function EntrySet_get_map_HashMapAnyNAnyN_k_() as Object
    return m.map
end function

function EntrySet_get_size_I_k_() as Integer
    return m.map.size
end function

function HashMapIterator_create_HashMapAnyNAnyN_HashMapIteratorAnyNAnyNAnyN_k_(map as Object) as Object
    this = {}
    this.__type = "HashMapIterator"
    this.__proto = ["HashMapIterator"]
    this.map = map
    this.keysArray = m.map.getKeysArray()
    this.keyCount = m.keysArray.Count()
    this.index = 0
    this.lastKey = invalid
    this.hasNext_Z_k_ = HashMapIterator_hasNext_Z_k_
    this.nextKey_AnyN_k_ = HashMapIterator_nextKey_AnyN_k_
    this.remove = HashMapIterator_remove
    this.get_map = HashMapIterator_get_map_HashMapAnyNAnyN_k_
    this.get_keysArray = HashMapIterator_get_keysArray_Dynamic_k_
    this.get_keyCount = HashMapIterator_get_keyCount_I_k_
    this.get_index = HashMapIterator_get_index_I_k_
    this.set_index = HashMapIterator_set_index_I_k_
    this.get_lastKey = HashMapIterator_get_lastKey_AnyN_k_
    this.set_lastKey = HashMapIterator_set_lastKey_AnyN_k_
    return this
end function

function HashMapIterator_hasNext_Z_k_() as Boolean
    return m.index < m.keyCount
end function

function HashMapIterator_nextKey_AnyN_k_() as Dynamic
    if m.hasNext().not() then
        throw NoSuchElementException_create_NoSuchElementException_k_()
    end if
    key = m.keysArray[m.index]
    m.index = (m.index + 1)
    m.lastKey = key
    return key
end function

sub HashMapIterator_remove()
    tmp0_elvis_lhs = m.lastKey
    __when_tmp5 = invalid
    if tmp0_elvis_lhs = invalid then
        throw IllegalStateException_create_StrN_IllegalStateException_k_("Call next() before remove()")
    else if true then
        __when_tmp5 = tmp0_elvis_lhs
    end if
    key = __when_tmp5

    m.map.remove(key)
    m.lastKey = invalid
end sub

function HashMapIterator_get_map_HashMapAnyNAnyN_k_() as Object
    return m.map
end function

function HashMapIterator_get_keysArray_Dynamic_k_() as Object
    return m.keysArray
end function

function HashMapIterator_get_keyCount_I_k_() as Integer
    return m.keyCount
end function

function HashMapIterator_get_index_I_k_() as Integer
    return m.index
end function

sub HashMapIterator_set_index_I_k_(value as Integer)
    m.index = value
end sub

function HashMapIterator_get_lastKey_AnyN_k_() as Dynamic
    return m.lastKey
end function

sub HashMapIterator_set_lastKey_AnyN_k_(value as Dynamic)
    m.lastKey = value
end sub

function KeyIterator_create_HashMapAnyNAnyN_KeyIteratorAnyNAnyN_k_(map as Object) as Object
    this = HashMapIterator_create_HashMapAnyNAnyN_HashMapIteratorAnyNAnyNAnyN_k_(map)
    this._super = {}
    this._super.next_AnyN_k_ = this.next_AnyN_k_
    this.__proto = ["KeyIterator", this.__proto]
    this.__type = "KeyIterator"
    this.next_AnyN_k_ = KeyIterator_next_AnyN_k_
    return this
end function

function KeyIterator_next_AnyN_k_() as Dynamic
    return m.nextKey()
end function

function ValueIterator_create_HashMapAnyNAnyN_ValueIteratorAnyNAnyN_k_(map as Object) as Object
    this = HashMapIterator_create_HashMapAnyNAnyN_HashMapIteratorAnyNAnyNAnyN_k_(map)
    this._super = {}
    this._super.next_AnyN_k_ = this.next_AnyN_k_
    this.__proto = ["ValueIterator", this.__proto]
    this.__type = "ValueIterator"
    this.next_AnyN_k_ = ValueIterator_next_AnyN_k_
    return this
end function

function ValueIterator_next_AnyN_k_() as Dynamic
    return CHECK_NOT_NULL_AnyN_Any_k_(m.map[m.nextKey()])
end function

function EntryIterator_create_HashMapAnyNAnyN_EntryIteratorAnyNAnyN_k_(map as Object) as Object
    this = HashMapIterator_create_HashMapAnyNAnyN_HashMapIteratorAnyNAnyNAnyN_k_(map)
    this._super = {}
    this._super.next_MutableEntryAnyNAnyN_k_ = this.next_MutableEntryAnyNAnyN_k_
    this.__proto = ["EntryIterator", this.__proto]
    this.__type = "EntryIterator"
    this.next_MutableEntryAnyNAnyN_k_ = EntryIterator_next_MutableEntryAnyNAnyN_k_
    return this
end function

function EntryIterator_next_MutableEntryAnyNAnyN_k_() as Object
    return HashMapEntry_create_HashMapAnyNAnyN_AnyN_HashMapEntryAnyNAnyN_k_(m.map, m.nextKey())
end function

function hashMapOf_HashMapAnyNAnyN_k_() as Object
    return HashMap_create_HashMapAnyNAnyN_k_()
end function

function hashMapOf_Arr_HashMapAnyNAnyN_k_(pairs as Object) as Object
    map = HashMap_create_I_HashMapAnyNAnyN_k_(pairs.size)
    for each pair in pairs
        map.put(pair.first, pair.second)

    end for
    return map
end function

function mutableMapOf_MutableMapAnyNAnyN_k_() as Object
    return HashMap_create_HashMapAnyNAnyN_k_()
end function

function mutableMapOf_Arr_MutableMapAnyNAnyN_k_(pairs as Object) as Object
    return hashMapOf_Arr_HashMapAnyNAnyN_k_([pairs])
end function

function mapOf_MapAnyNAnyN_k_() as Object
    return emptyMap_MapAnyNAnyN_k_()
end function

function mapOf_Arr_MapAnyNAnyN_k_(pairs as Object) as Object
    __when_tmp6 = invalid
    if pairs.size = 0 then
        __when_tmp6 = emptyMap_MapAnyNAnyN_k_()
    else if true then
        __when_tmp6 = hashMapOf_Arr_HashMapAnyNAnyN_k_([pairs])
    end if
    return __when_tmp6

end function

function emptyMap_MapAnyNAnyN_k_() as Object
    return EmptyMap_getInstance()
end function

function EmptyMap_create_EmptyMap_k_() as Object
    this = {}
    this.__type = "EmptyMap"
    this.__proto = ["EmptyMap"]
    this.isEmpty_Z_k_ = EmptyMap_isEmpty_Z_k_
    this.containsKey_AnyN_Z_k_ = EmptyMap_containsKey_AnyN_Z_k_
    this.containsValue_AnyN_Z_k_ = EmptyMap_containsValue_AnyN_Z_k_
    this.get_AnyN_AnyN_k_ = EmptyMap_get_AnyN_AnyN_k_
    this.equals_AnyN_Z_k_ = EmptyMap_equals_AnyN_Z_k_
    this.hashCode_I_k_ = EmptyMap_hashCode_I_k_
    this.toString_Str_k_ = EmptyMap_toString_Str_k_
    this.get_size = EmptyMap_get_size_I_k_
    this.get_keys = EmptyMap_get_keys_SetAnyN_k_
    this.get_values = EmptyMap_get_values_CollectionAnyN_k_
    this.get_entries = EmptyMap_get_entries_SetEntryAnyNAnyN_k_
    return this
end function

function EmptyMap_getInstance() as Object
    if m.EmptyMap_instance = invalid then
        m.EmptyMap_instance = EmptyMap_create()
    end if
    return m.EmptyMap_instance
end function

function EmptyMap_isEmpty_Z_k_() as Boolean
    return true
end function

function EmptyMap_containsKey_AnyN_Z_k_(key as Dynamic) as Boolean
    return false
end function

function EmptyMap_containsValue_AnyN_Z_k_(value as Dynamic) as Boolean
    return false
end function

function EmptyMap_get_AnyN_AnyN_k_(key as Dynamic) as Dynamic
    return invalid
end function

function EmptyMap_equals_AnyN_Z_k_(other as Dynamic) as Boolean
    return __kotlin_isInstanceOf(other, "Map") and other.isEmpty()
end function

function EmptyMap_hashCode_I_k_() as Integer
    return 0
end function

function EmptyMap_toString_Str_k_() as String
    return "{}"
end function

function EmptyMap_get_size_I_k_() as Integer
    return 0
end function

function EmptyMap_get_keys_SetAnyN_k_() as Object
    return EmptySet_getInstance()
end function

function EmptyMap_get_values_CollectionAnyN_k_() as Object
    return emptyList_ListAnyN_k_()
end function

function EmptyMap_get_entries_SetEntryAnyNAnyN_k_() as Object
    return EmptyEntrySet_getInstance()
end function

function EmptySet_create_EmptySet_k_() as Object
    this = {}
    this.__type = "EmptySet"
    this.__proto = ["EmptySet"]
    this.isEmpty_Z_k_ = EmptySet_isEmpty_Z_k_
    this.contains_AnyN_Z_k_ = EmptySet_contains_AnyN_Z_k_
    this.containsAll_CollectionAnyN_Z_k_ = EmptySet_containsAll_CollectionAnyN_Z_k_
    this.iterator_IteratorAnyN_k_ = EmptySet_iterator_IteratorAnyN_k_
    this.get_size = EmptySet_get_size_I_k_
    return this
end function

function EmptySet_getInstance() as Object
    if m.EmptySet_instance = invalid then
        m.EmptySet_instance = EmptySet_create()
    end if
    return m.EmptySet_instance
end function

function EmptySet_isEmpty_Z_k_() as Boolean
    return true
end function

function EmptySet_contains_AnyN_Z_k_(element as Dynamic) as Boolean
    return false
end function

function EmptySet_containsAll_CollectionAnyN_Z_k_(elements as Object) as Boolean
    return elements.isEmpty()
end function

function EmptySet_iterator_IteratorAnyN_k_() as Object
    return EmptyMapIterator_getInstance()
end function

function EmptySet_get_size_I_k_() as Integer
    return 0
end function

function EmptyMapIterator_create_EmptyMapIterator_k_() as Object
    this = {}
    this.__type = "EmptyMapIterator"
    this.__proto = ["EmptyMapIterator"]
    this.hasNext_Z_k_ = EmptyMapIterator_hasNext_Z_k_
    this.next_AnyN_k_ = EmptyMapIterator_next_AnyN_k_
    return this
end function

function EmptyMapIterator_getInstance() as Object
    if m.EmptyMapIterator_instance = invalid then
        m.EmptyMapIterator_instance = EmptyMapIterator_create()
    end if
    return m.EmptyMapIterator_instance
end function

function EmptyMapIterator_hasNext_Z_k_() as Boolean
    return false
end function

function EmptyMapIterator_next_AnyN_k_() as Dynamic
    throw NoSuchElementException_create_NoSuchElementException_k_()
end function

function EmptyEntrySet_create_EmptyEntrySet_k_() as Object
    this = {}
    this.__type = "EmptyEntrySet"
    this.__proto = ["EmptyEntrySet"]
    this.isEmpty_Z_k_ = EmptyEntrySet_isEmpty_Z_k_
    this.contains_EntryAnyNAnyN_Z_k_ = EmptyEntrySet_contains_EntryAnyNAnyN_Z_k_
    this.containsAll_CollectionEntryAnyNAnyN_Z_k_ = EmptyEntrySet_containsAll_CollectionEntryAnyNAnyN_Z_k_
    this.iterator_IteratorEntryAnyNAnyN_k_ = EmptyEntrySet_iterator_IteratorEntryAnyNAnyN_k_
    this.get_size = EmptyEntrySet_get_size_I_k_
    return this
end function

function EmptyEntrySet_getInstance() as Object
    if m.EmptyEntrySet_instance = invalid then
        m.EmptyEntrySet_instance = EmptyEntrySet_create()
    end if
    return m.EmptyEntrySet_instance
end function

function EmptyEntrySet_isEmpty_Z_k_() as Boolean
    return true
end function

function EmptyEntrySet_contains_EntryAnyNAnyN_Z_k_(element as Object) as Boolean
    return false
end function

function EmptyEntrySet_containsAll_CollectionEntryAnyNAnyN_Z_k_(elements as Object) as Boolean
    return elements.isEmpty()
end function

function EmptyEntrySet_iterator_IteratorEntryAnyNAnyN_k_() as Object
    return EmptyEntryIterator_getInstance()
end function

function EmptyEntrySet_get_size_I_k_() as Integer
    return 0
end function

function EmptyEntryIterator_create_EmptyEntryIterator_k_() as Object
    this = {}
    this.__type = "EmptyEntryIterator"
    this.__proto = ["EmptyEntryIterator"]
    this.hasNext_Z_k_ = EmptyEntryIterator_hasNext_Z_k_
    this.next_EntryAnyNAnyN_k_ = EmptyEntryIterator_next_EntryAnyNAnyN_k_
    return this
end function

function EmptyEntryIterator_getInstance() as Object
    if m.EmptyEntryIterator_instance = invalid then
        m.EmptyEntryIterator_instance = EmptyEntryIterator_create()
    end if
    return m.EmptyEntryIterator_instance
end function

function EmptyEntryIterator_hasNext_Z_k_() as Boolean
    return false
end function

function EmptyEntryIterator_next_EntryAnyNAnyN_k_() as Object
    throw NoSuchElementException_create_NoSuchElementException_k_()
end function
