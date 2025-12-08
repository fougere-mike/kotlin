function LinkedHashMap_create_LinkedHashMapAnyNAnyN_k_() as Object
    this = {}
    this.__type = "LinkedHashMap"
    this.__proto = ["LinkedHashMap"]
    this._size = 0
    this.isEmpty_Z_k_ = LinkedHashMap_isEmpty_Z_k_
    this.containsKey_AnyN_Z_k_ = LinkedHashMap_containsKey_AnyN_Z_k_
    this.containsValue_AnyN_Z_k_ = LinkedHashMap_containsValue_AnyN_Z_k_
    this.get_AnyN_AnyN_k_ = LinkedHashMap_get_AnyN_AnyN_k_
    this.put_AnyN_AnyN_AnyN_k_ = LinkedHashMap_put_AnyN_AnyN_AnyN_k_
    this.remove_AnyN_AnyN_k_ = LinkedHashMap_remove_AnyN_AnyN_k_
    this.putAll_MapAnyNAnyN_k_ = LinkedHashMap_putAll_MapAnyNAnyN_k_
    this.clear = LinkedHashMap_clear
    this.equals_AnyN_Z_k_ = LinkedHashMap_equals_AnyN_Z_k_
    this.hashCode_I_k_ = LinkedHashMap_hashCode_I_k_
    this.toString_Str_k_ = LinkedHashMap_toString_Str_k_
    this.keyToString_AnyN_Str_k_ = LinkedHashMap_keyToString_AnyN_Str_k_
    this.get_map = LinkedHashMap_get_map_Dynamic_k_
    this.set_map = LinkedHashMap_set_map_Dynamic_k_
    this.get_keyOrder = LinkedHashMap_get_keyOrder_Dynamic_k_
    this.set_keyOrder = LinkedHashMap_set_keyOrder_Dynamic_k_
    this.get__size = LinkedHashMap_get__size_I_k_
    this.set__size = LinkedHashMap_set__size_I_k_
    this.get_size = LinkedHashMap_get_size_I_k_
    this.get_keys = LinkedHashMap_get_keys_MutableSetAnyN_k_
    this.get_values = LinkedHashMap_get_values_MutableCollectionAnyN_k_
    this.get_entries = LinkedHashMap_get_entries_MutableSetMutableEntryAnyNAnyN_k_
    m.map = CreateObject("roAssociativeArray")
    m.keyOrder = CreateObject("roArray", 0, true)
    m._size = 0
    return this
end function

function LinkedHashMap_create_I_LinkedHashMapAnyNAnyN_k_(initialCapacity as Integer) as Object
    this = {}
    this.__type = "LinkedHashMap"
    this.__proto = ["LinkedHashMap"]
    this._size = 0
    this.isEmpty_Z_k_ = LinkedHashMap_isEmpty_Z_k_
    this.containsKey_AnyN_Z_k_ = LinkedHashMap_containsKey_AnyN_Z_k_
    this.containsValue_AnyN_Z_k_ = LinkedHashMap_containsValue_AnyN_Z_k_
    this.get_AnyN_AnyN_k_ = LinkedHashMap_get_AnyN_AnyN_k_
    this.put_AnyN_AnyN_AnyN_k_ = LinkedHashMap_put_AnyN_AnyN_AnyN_k_
    this.remove_AnyN_AnyN_k_ = LinkedHashMap_remove_AnyN_AnyN_k_
    this.putAll_MapAnyNAnyN_k_ = LinkedHashMap_putAll_MapAnyNAnyN_k_
    this.clear = LinkedHashMap_clear
    this.equals_AnyN_Z_k_ = LinkedHashMap_equals_AnyN_Z_k_
    this.hashCode_I_k_ = LinkedHashMap_hashCode_I_k_
    this.toString_Str_k_ = LinkedHashMap_toString_Str_k_
    this.keyToString_AnyN_Str_k_ = LinkedHashMap_keyToString_AnyN_Str_k_
    this.get_map = LinkedHashMap_get_map_Dynamic_k_
    this.set_map = LinkedHashMap_set_map_Dynamic_k_
    this.get_keyOrder = LinkedHashMap_get_keyOrder_Dynamic_k_
    this.set_keyOrder = LinkedHashMap_set_keyOrder_Dynamic_k_
    this.get__size = LinkedHashMap_get__size_I_k_
    this.set__size = LinkedHashMap_set__size_I_k_
    this.get_size = LinkedHashMap_get_size_I_k_
    this.get_keys = LinkedHashMap_get_keys_MutableSetAnyN_k_
    this.get_values = LinkedHashMap_get_values_MutableCollectionAnyN_k_
    this.get_entries = LinkedHashMap_get_entries_MutableSetMutableEntryAnyNAnyN_k_
    require_Z_Function0Any_k_(initialCapacity >= 0, {initialCapacity: initialCapacity, invoke: function() as Object
        return "Negative initial capacity: " + m.initialCapacity
    end function})
    m.map = CreateObject("roAssociativeArray")
    m.keyOrder = CreateObject("roArray", 0, true)
    m._size = 0
    return this
end function

function LinkedHashMap_create_I_F_LinkedHashMapAnyNAnyN_k_(initialCapacity as Integer, loadFactor as Float) as Object
    this = {}
    this.__type = "LinkedHashMap"
    this.__proto = ["LinkedHashMap"]
    this._size = 0
    this.isEmpty_Z_k_ = LinkedHashMap_isEmpty_Z_k_
    this.containsKey_AnyN_Z_k_ = LinkedHashMap_containsKey_AnyN_Z_k_
    this.containsValue_AnyN_Z_k_ = LinkedHashMap_containsValue_AnyN_Z_k_
    this.get_AnyN_AnyN_k_ = LinkedHashMap_get_AnyN_AnyN_k_
    this.put_AnyN_AnyN_AnyN_k_ = LinkedHashMap_put_AnyN_AnyN_AnyN_k_
    this.remove_AnyN_AnyN_k_ = LinkedHashMap_remove_AnyN_AnyN_k_
    this.putAll_MapAnyNAnyN_k_ = LinkedHashMap_putAll_MapAnyNAnyN_k_
    this.clear = LinkedHashMap_clear
    this.equals_AnyN_Z_k_ = LinkedHashMap_equals_AnyN_Z_k_
    this.hashCode_I_k_ = LinkedHashMap_hashCode_I_k_
    this.toString_Str_k_ = LinkedHashMap_toString_Str_k_
    this.keyToString_AnyN_Str_k_ = LinkedHashMap_keyToString_AnyN_Str_k_
    this.get_map = LinkedHashMap_get_map_Dynamic_k_
    this.set_map = LinkedHashMap_set_map_Dynamic_k_
    this.get_keyOrder = LinkedHashMap_get_keyOrder_Dynamic_k_
    this.set_keyOrder = LinkedHashMap_set_keyOrder_Dynamic_k_
    this.get__size = LinkedHashMap_get__size_I_k_
    this.set__size = LinkedHashMap_set__size_I_k_
    this.get_size = LinkedHashMap_get_size_I_k_
    this.get_keys = LinkedHashMap_get_keys_MutableSetAnyN_k_
    this.get_values = LinkedHashMap_get_values_MutableCollectionAnyN_k_
    this.get_entries = LinkedHashMap_get_entries_MutableSetMutableEntryAnyNAnyN_k_
    require_Z_Function0Any_k_(initialCapacity >= 0, {initialCapacity: initialCapacity, invoke: function() as Object
        return "Negative initial capacity: " + m.initialCapacity
    end function})
    require_Z_Function0Any_k_(loadFactor > 0, {loadFactor: loadFactor, invoke: function() as Object
        return "Non-positive load factor: " + m.loadFactor
    end function})
    m.map = CreateObject("roAssociativeArray")
    m.keyOrder = CreateObject("roArray", 0, true)
    m._size = 0
    return this
end function

function LinkedHashMap_create_MapAnyNAnyN_LinkedHashMapAnyNAnyN_k_(original as Object) as Object
    this = {}
    this.__type = "LinkedHashMap"
    this.__proto = ["LinkedHashMap"]
    this._size = 0
    this.isEmpty_Z_k_ = LinkedHashMap_isEmpty_Z_k_
    this.containsKey_AnyN_Z_k_ = LinkedHashMap_containsKey_AnyN_Z_k_
    this.containsValue_AnyN_Z_k_ = LinkedHashMap_containsValue_AnyN_Z_k_
    this.get_AnyN_AnyN_k_ = LinkedHashMap_get_AnyN_AnyN_k_
    this.put_AnyN_AnyN_AnyN_k_ = LinkedHashMap_put_AnyN_AnyN_AnyN_k_
    this.remove_AnyN_AnyN_k_ = LinkedHashMap_remove_AnyN_AnyN_k_
    this.putAll_MapAnyNAnyN_k_ = LinkedHashMap_putAll_MapAnyNAnyN_k_
    this.clear = LinkedHashMap_clear
    this.equals_AnyN_Z_k_ = LinkedHashMap_equals_AnyN_Z_k_
    this.hashCode_I_k_ = LinkedHashMap_hashCode_I_k_
    this.toString_Str_k_ = LinkedHashMap_toString_Str_k_
    this.keyToString_AnyN_Str_k_ = LinkedHashMap_keyToString_AnyN_Str_k_
    this.get_map = LinkedHashMap_get_map_Dynamic_k_
    this.set_map = LinkedHashMap_set_map_Dynamic_k_
    this.get_keyOrder = LinkedHashMap_get_keyOrder_Dynamic_k_
    this.set_keyOrder = LinkedHashMap_set_keyOrder_Dynamic_k_
    this.get__size = LinkedHashMap_get__size_I_k_
    this.set__size = LinkedHashMap_set__size_I_k_
    this.get_size = LinkedHashMap_get_size_I_k_
    this.get_keys = LinkedHashMap_get_keys_MutableSetAnyN_k_
    this.get_values = LinkedHashMap_get_values_MutableCollectionAnyN_k_
    this.get_entries = LinkedHashMap_get_entries_MutableSetMutableEntryAnyNAnyN_k_
    m.map = CreateObject("roAssociativeArray")
    m.keyOrder = CreateObject("roArray", 0, true)
    m._size = 0
    m.putAll(original)
    return this
end function

function LinkedHashMap_isEmpty_Z_k_() as Boolean
    return m._size = 0
end function

function LinkedHashMap_containsKey_AnyN_Z_k_(key as Dynamic) as Boolean
    keyStr = m.keyToString(key)
    return m.map.DoesExist(keyStr)
end function

function LinkedHashMap_containsValue_AnyN_Z_k_(value as Dynamic) as Boolean
    count = m.keyOrder.Count()
    i = 0
    while i < count
        keyStr = m.keyOrder[i]
        v = m.map[keyStr]
        if v = value then
            return true
        end if
        i = (i + 1)
    end while
    return false
end function

function LinkedHashMap_get_AnyN_AnyN_k_(key as Dynamic) as Dynamic
    keyStr = m.keyToString(key)
    if m.map.DoesExist(keyStr).not() then
        return invalid
    end if
    return m.map[keyStr]
end function

function LinkedHashMap_put_AnyN_AnyN_AnyN_k_(key as Dynamic, value as Dynamic) as Dynamic
    keyStr = m.keyToString(key)
    __when_tmp0 = invalid
    if m.map.DoesExist(keyStr) then
        __when_tmp0 = m.map[keyStr]
    else if true then
        __when_tmp0 = invalid
    end if
    oldValue = __when_tmp0

    m.map.AddReplace(keyStr, value)
    return oldValue
end function

function LinkedHashMap_remove_AnyN_AnyN_k_(key as Dynamic) as Dynamic
    keyStr = m.keyToString(key)
    if m.map.DoesExist(keyStr).not() then
        return invalid
    end if
    oldValue = m.map[keyStr]
    m.map.Delete(keyStr)
    count = m.keyOrder.Count()
    i = 0
    while i < count
        if m.keyOrder[i] = keyStr then
            m.keyOrder.Delete(i)
            exit while
        end if
        i = (i + 1)
    end while
    m._size = (m._size - 1)
    return oldValue
end function

sub LinkedHashMap_putAll_MapAnyNAnyN_k_(from as Object)
    for each entry in from.entries
        m.put(entry.key, entry.value)

    end for
end sub

sub LinkedHashMap_clear()
    m.map.Clear()
    m.keyOrder.Clear()
    m._size = 0
end sub

function LinkedHashMap_equals_AnyN_Z_k_(other as Dynamic) as Boolean
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
        if (otherValue = invalid) and otherMap.containsKey(entry.key).not() then
            return false
        end if

    end for
    return true
end function

function LinkedHashMap_hashCode_I_k_() as Integer
    result = 0
    for each entry in m.entries
        result = (result + entry.hashCode())

    end for
    return result
end function

function LinkedHashMap_toString_Str_k_() as String
    entries = joinToString_77mgo1_k_(m.entries, ", ", "{", "}", {invoke: function(it as Object) as Object
        return (it.key + "=") + it.value
    end function})
    return entries
end function

function LinkedHashMap_keyToString_AnyN_Str_k_(key as Dynamic) as String
    return key.toString()
end function

function LinkedHashMap_get_map_Dynamic_k_() as Object
    return m.map
end function

sub LinkedHashMap_set_map_Dynamic_k_(value as Object)
    m.map = value
end sub

function LinkedHashMap_get_keyOrder_Dynamic_k_() as Object
    return m.keyOrder
end function

sub LinkedHashMap_set_keyOrder_Dynamic_k_(value as Object)
    m.keyOrder = value
end sub

function LinkedHashMap_get__size_I_k_() as Integer
    return m._size
end function

sub LinkedHashMap_set__size_I_k_(value as Integer)
    m._size = value
end sub

function LinkedHashMap_get_size_I_k_() as Integer
    return m._size
end function

function LinkedHashMap_get_keys_MutableSetAnyN_k_() as Object
    return LinkedHashMap_LinkedKeySet_create_LinkedHashMapAnyNAnyN_LinkedKeySetAnyNAnyN_k_(m)
end function

function LinkedHashMap_get_values_MutableCollectionAnyN_k_() as Object
    return LinkedHashMap_LinkedValueCollection_create_LinkedHashMapAnyNAnyN_LinkedValueCollectionAnyNAnyN_k_(m)
end function

function LinkedHashMap_get_entries_MutableSetMutableEntryAnyNAnyN_k_() as Object
    return LinkedHashMap_LinkedEntrySet_create_LinkedHashMapAnyNAnyN_LinkedEntrySetAnyNAnyN_k_(m)
end function

function LinkedHashMap_LinkedKeySet_create_LinkedHashMapAnyNAnyN_LinkedKeySetAnyNAnyN_k_(map as Object) as Object
    this = {}
    this.__type = "LinkedHashMap_LinkedKeySet"
    this.__proto = ["LinkedHashMap_LinkedKeySet"]
    this.map = map
    this.isEmpty_Z_k_ = LinkedHashMap_LinkedKeySet_isEmpty_Z_k_
    this.contains_AnyN_Z_k_ = LinkedHashMap_LinkedKeySet_contains_AnyN_Z_k_
    this.containsAll_CollectionAnyN_Z_k_ = LinkedHashMap_LinkedKeySet_containsAll_CollectionAnyN_Z_k_
    this.iterator_MutableIteratorAnyN_k_ = LinkedHashMap_LinkedKeySet_iterator_MutableIteratorAnyN_k_
    this.add_AnyN_Z_k_ = LinkedHashMap_LinkedKeySet_add_AnyN_Z_k_
    this.addAll_CollectionAnyN_Z_k_ = LinkedHashMap_LinkedKeySet_addAll_CollectionAnyN_Z_k_
    this.remove_AnyN_Z_k_ = LinkedHashMap_LinkedKeySet_remove_AnyN_Z_k_
    this.removeAll_CollectionAnyN_Z_k_ = LinkedHashMap_LinkedKeySet_removeAll_CollectionAnyN_Z_k_
    this.retainAll_CollectionAnyN_Z_k_ = LinkedHashMap_LinkedKeySet_retainAll_CollectionAnyN_Z_k_
    this.clear = LinkedHashMap_LinkedKeySet_clear
    this.get_map = LinkedHashMap_LinkedKeySet_get_map_LinkedHashMapAnyNAnyN_k_
    this.get_size = LinkedHashMap_LinkedKeySet_get_size_I_k_
    return this
end function

function LinkedHashMap_LinkedKeySet_isEmpty_Z_k_() as Boolean
    return m.map.isEmpty()
end function

function LinkedHashMap_LinkedKeySet_contains_AnyN_Z_k_(element as Dynamic) as Boolean
    return m.map.containsKey(element)
end function

function LinkedHashMap_LinkedKeySet_containsAll_CollectionAnyN_Z_k_(elements as Object) as Boolean
    for each element in elements
        if m.contains(element).not() then
            return false
        end if
    end for
    return true
end function

function LinkedHashMap_LinkedKeySet_iterator_MutableIteratorAnyN_k_() as Object
    return Anon_5ee16a6d_create_AnonAnyNAnyN_k_()
end function

function LinkedHashMap_LinkedKeySet_add_AnyN_Z_k_(element as Dynamic) as Boolean
    throw UnsupportedOperationException_create_StrN_UnsupportedOperationException_k_("Add not supported on key set")
end function

function LinkedHashMap_LinkedKeySet_addAll_CollectionAnyN_Z_k_(elements as Object) as Boolean
    throw UnsupportedOperationException_create_StrN_UnsupportedOperationException_k_("Add not supported on key set")
end function

function LinkedHashMap_LinkedKeySet_remove_AnyN_Z_k_(element as Dynamic) as Boolean
    if m.map.containsKey(element).not() then
        return false
    end if
    m.map.remove(element)
    return true
end function

function LinkedHashMap_LinkedKeySet_removeAll_CollectionAnyN_Z_k_(elements as Object) as Boolean
    modified = false
    for each element in elements
        if m.remove(element) then
            modified = true
        end if
    end for
    return modified
end function

function LinkedHashMap_LinkedKeySet_retainAll_CollectionAnyN_Z_k_(elements as Object) as Boolean
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

sub LinkedHashMap_LinkedKeySet_clear()
    m.map.clear()
end sub

function LinkedHashMap_LinkedKeySet_get_map_LinkedHashMapAnyNAnyN_k_() as Object
    return m.map
end function

function LinkedHashMap_LinkedKeySet_get_size_I_k_() as Integer
    return m.map.size
end function

function LinkedHashMap_LinkedValueCollection_create_LinkedHashMapAnyNAnyN_LinkedValueCollectionAnyNAnyN_k_(map as Object) as Object
    this = {}
    this.__type = "LinkedHashMap_LinkedValueCollection"
    this.__proto = ["LinkedHashMap_LinkedValueCollection"]
    this.map = map
    this.isEmpty_Z_k_ = LinkedHashMap_LinkedValueCollection_isEmpty_Z_k_
    this.contains_AnyN_Z_k_ = LinkedHashMap_LinkedValueCollection_contains_AnyN_Z_k_
    this.containsAll_CollectionAnyN_Z_k_ = LinkedHashMap_LinkedValueCollection_containsAll_CollectionAnyN_Z_k_
    this.iterator_MutableIteratorAnyN_k_ = LinkedHashMap_LinkedValueCollection_iterator_MutableIteratorAnyN_k_
    this.add_AnyN_Z_k_ = LinkedHashMap_LinkedValueCollection_add_AnyN_Z_k_
    this.addAll_CollectionAnyN_Z_k_ = LinkedHashMap_LinkedValueCollection_addAll_CollectionAnyN_Z_k_
    this.remove_AnyN_Z_k_ = LinkedHashMap_LinkedValueCollection_remove_AnyN_Z_k_
    this.removeAll_CollectionAnyN_Z_k_ = LinkedHashMap_LinkedValueCollection_removeAll_CollectionAnyN_Z_k_
    this.retainAll_CollectionAnyN_Z_k_ = LinkedHashMap_LinkedValueCollection_retainAll_CollectionAnyN_Z_k_
    this.clear = LinkedHashMap_LinkedValueCollection_clear
    this.get_map = LinkedHashMap_LinkedValueCollection_get_map_LinkedHashMapAnyNAnyN_k_
    this.get_size = LinkedHashMap_LinkedValueCollection_get_size_I_k_
    return this
end function

function LinkedHashMap_LinkedValueCollection_isEmpty_Z_k_() as Boolean
    return m.map.isEmpty()
end function

function LinkedHashMap_LinkedValueCollection_contains_AnyN_Z_k_(element as Dynamic) as Boolean
    return m.map.containsValue(element)
end function

function LinkedHashMap_LinkedValueCollection_containsAll_CollectionAnyN_Z_k_(elements as Object) as Boolean
    for each element in elements
        if m.contains(element).not() then
            return false
        end if
    end for
    return true
end function

function LinkedHashMap_LinkedValueCollection_iterator_MutableIteratorAnyN_k_() as Object
    return Anon_190db09c_create_AnonAnyNAnyN_k_()
end function

function LinkedHashMap_LinkedValueCollection_add_AnyN_Z_k_(element as Dynamic) as Boolean
    throw UnsupportedOperationException_create_StrN_UnsupportedOperationException_k_("Add not supported on value collection")
end function

function LinkedHashMap_LinkedValueCollection_addAll_CollectionAnyN_Z_k_(elements as Object) as Boolean
    throw UnsupportedOperationException_create_StrN_UnsupportedOperationException_k_("Add not supported on value collection")
end function

function LinkedHashMap_LinkedValueCollection_remove_AnyN_Z_k_(element as Dynamic) as Boolean
    iter = m.iterator()
    while iter.hasNext()
        if iter.next() = element then
            iter.remove()
            return true
        end if
    end while
    return false
end function

function LinkedHashMap_LinkedValueCollection_removeAll_CollectionAnyN_Z_k_(elements as Object) as Boolean
    modified = false
    for each element in elements
        while m.remove(element)
            modified = true
        end while

    end for
    return modified
end function

function LinkedHashMap_LinkedValueCollection_retainAll_CollectionAnyN_Z_k_(elements as Object) as Boolean
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

sub LinkedHashMap_LinkedValueCollection_clear()
    m.map.clear()
end sub

function LinkedHashMap_LinkedValueCollection_get_map_LinkedHashMapAnyNAnyN_k_() as Object
    return m.map
end function

function LinkedHashMap_LinkedValueCollection_get_size_I_k_() as Integer
    return m.map.size
end function

function LinkedHashMap_LinkedEntrySet_create_LinkedHashMapAnyNAnyN_LinkedEntrySetAnyNAnyN_k_(map as Object) as Object
    this = {}
    this.__type = "LinkedHashMap_LinkedEntrySet"
    this.__proto = ["LinkedHashMap_LinkedEntrySet"]
    this.map = map
    this.isEmpty_Z_k_ = LinkedHashMap_LinkedEntrySet_isEmpty_Z_k_
    this.contains_MutableEntryAnyNAnyN_Z_k_ = LinkedHashMap_LinkedEntrySet_contains_MutableEntryAnyNAnyN_Z_k_
    this.containsAll_CollectionMutableEntryAnyNAnyN_Z_k_ = LinkedHashMap_LinkedEntrySet_containsAll_CollectionMutableEntryAnyNAnyN_Z_k_
    this.iterator_MutableIteratorMutableEntryAnyNAnyN_k_ = LinkedHashMap_LinkedEntrySet_iterator_MutableIteratorMutableEntryAnyNAnyN_k_
    this.add_MutableEntryAnyNAnyN_Z_k_ = LinkedHashMap_LinkedEntrySet_add_MutableEntryAnyNAnyN_Z_k_
    this.addAll_CollectionMutableEntryAnyNAnyN_Z_k_ = LinkedHashMap_LinkedEntrySet_addAll_CollectionMutableEntryAnyNAnyN_Z_k_
    this.remove_MutableEntryAnyNAnyN_Z_k_ = LinkedHashMap_LinkedEntrySet_remove_MutableEntryAnyNAnyN_Z_k_
    this.removeAll_CollectionMutableEntryAnyNAnyN_Z_k_ = LinkedHashMap_LinkedEntrySet_removeAll_CollectionMutableEntryAnyNAnyN_Z_k_
    this.retainAll_CollectionMutableEntryAnyNAnyN_Z_k_ = LinkedHashMap_LinkedEntrySet_retainAll_CollectionMutableEntryAnyNAnyN_Z_k_
    this.clear = LinkedHashMap_LinkedEntrySet_clear
    this.get_map = LinkedHashMap_LinkedEntrySet_get_map_LinkedHashMapAnyNAnyN_k_
    this.get_size = LinkedHashMap_LinkedEntrySet_get_size_I_k_
    return this
end function

function LinkedHashMap_LinkedEntrySet_isEmpty_Z_k_() as Boolean
    return m.map.isEmpty()
end function

function LinkedHashMap_LinkedEntrySet_contains_MutableEntryAnyNAnyN_Z_k_(element as Object) as Boolean
    value = m.map[element.key]
    return (value = element.value) and ((value <> invalid) or m.map.containsKey(element.key))
end function

function LinkedHashMap_LinkedEntrySet_containsAll_CollectionMutableEntryAnyNAnyN_Z_k_(elements as Object) as Boolean
    for each element in elements
        if m.contains(element).not() then
            return false
        end if
    end for
    return true
end function

function LinkedHashMap_LinkedEntrySet_iterator_MutableIteratorMutableEntryAnyNAnyN_k_() as Object
    return Anon_72afaee3_create_AnonAnyNAnyN_k_()
end function

function LinkedHashMap_LinkedEntrySet_add_MutableEntryAnyNAnyN_Z_k_(element as Object) as Boolean
    throw UnsupportedOperationException_create_StrN_UnsupportedOperationException_k_("Add not supported on entry set")
end function

function LinkedHashMap_LinkedEntrySet_addAll_CollectionMutableEntryAnyNAnyN_Z_k_(elements as Object) as Boolean
    throw UnsupportedOperationException_create_StrN_UnsupportedOperationException_k_("Add not supported on entry set")
end function

function LinkedHashMap_LinkedEntrySet_remove_MutableEntryAnyNAnyN_Z_k_(element as Object) as Boolean
    if m.contains(element).not() then
        return false
    end if
    m.map.remove(element.key)
    return true
end function

function LinkedHashMap_LinkedEntrySet_removeAll_CollectionMutableEntryAnyNAnyN_Z_k_(elements as Object) as Boolean
    modified = false
    for each element in elements
        if m.remove(element) then
            modified = true
        end if
    end for
    return modified
end function

function LinkedHashMap_LinkedEntrySet_retainAll_CollectionMutableEntryAnyNAnyN_Z_k_(elements as Object) as Boolean
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

sub LinkedHashMap_LinkedEntrySet_clear()
    m.map.clear()
end sub

function LinkedHashMap_LinkedEntrySet_get_map_LinkedHashMapAnyNAnyN_k_() as Object
    return m.map
end function

function LinkedHashMap_LinkedEntrySet_get_size_I_k_() as Integer
    return m.map.size
end function

function LinkedHashMap_SimpleEntry_create_AnyN_AnyN_SimpleEntryAnyNAnyN_k_(key as Dynamic, value as Dynamic) as Object
    this = {}
    this.__type = "LinkedHashMap_SimpleEntry"
    this.__proto = ["LinkedHashMap_SimpleEntry"]
    this.key = key
    this.value = value
    this.setValue_AnyN_AnyN_k_ = LinkedHashMap_SimpleEntry_setValue_AnyN_AnyN_k_
    this.equals_AnyN_Z_k_ = LinkedHashMap_SimpleEntry_equals_AnyN_Z_k_
    this.hashCode_I_k_ = LinkedHashMap_SimpleEntry_hashCode_I_k_
    this.toString_Str_k_ = LinkedHashMap_SimpleEntry_toString_Str_k_
    this.get_key = LinkedHashMap_SimpleEntry_get_key_AnyN_k_
    this.get_value = LinkedHashMap_SimpleEntry_get_value_AnyN_k_
    this.set_value = LinkedHashMap_SimpleEntry_set_value_AnyN_k_
    return this
end function

function LinkedHashMap_SimpleEntry_setValue_AnyN_AnyN_k_(newValue as Dynamic) as Dynamic
    oldValue = m.value
    m.value = newValue
    return oldValue
end function

function LinkedHashMap_SimpleEntry_equals_AnyN_Z_k_(other as Dynamic) as Boolean
    if not __kotlin_isInstanceOf(other, "Map_Entry") then
        return false
    end if
    return (m.key = other.key) and (m.value = other.value)
end function

function LinkedHashMap_SimpleEntry_hashCode_I_k_() as Integer
    tmp0_safe_receiver = m.key
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
    tmp2_safe_receiver = m.value
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
    return __when_tmp5.xor(__when_tmp7)

end function

function LinkedHashMap_SimpleEntry_toString_Str_k_() as String
    return (m.key + "=") + m.value
end function

function LinkedHashMap_SimpleEntry_get_key_AnyN_k_() as Dynamic
    return m.key
end function

function LinkedHashMap_SimpleEntry_get_value_AnyN_k_() as Dynamic
    return m.value
end function

sub LinkedHashMap_SimpleEntry_set_value_AnyN_k_(value as Dynamic)
    m.value = value
end sub
