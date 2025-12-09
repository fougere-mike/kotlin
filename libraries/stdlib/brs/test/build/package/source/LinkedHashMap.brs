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
    this.set_map(CreateObject("roAssociativeArray"))
    this.set_keyOrder(CreateObject("roArray", 0, true))
    this.set__size(0)
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
    this.set_map(CreateObject("roAssociativeArray"))
    this.set_keyOrder(CreateObject("roArray", 0, true))
    this.set__size(0)
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
    this.set_map(CreateObject("roAssociativeArray"))
    this.set_keyOrder(CreateObject("roArray", 0, true))
    this.set__size(0)
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
    this.set_map(CreateObject("roAssociativeArray"))
    this.set_keyOrder(CreateObject("roArray", 0, true))
    this.set__size(0)
    this.putAll_MapAnyNAnyN_k_(original)
    return this
end function

function LinkedHashMap_isEmpty_Z_k_() as Boolean
    return m.get__size() = 0
end function

function LinkedHashMap_containsKey_AnyN_Z_k_(key as Dynamic) as Boolean
    keyStr = m.keyToString_AnyN_Str_k_(key)
    return m.get_map().DoesExist(keyStr)
end function

function LinkedHashMap_containsValue_AnyN_Z_k_(value as Dynamic) as Boolean
    count = m.get_keyOrder().Count()
    i = 0
    while i < count
        keyStr = m.get_keyOrder()[i]
        v = m.get_map()[keyStr]
        if v = value then
            return true
        end if
        i = (i + 1)
    end while
    return false
end function

function LinkedHashMap_get_AnyN_AnyN_k_(key as Dynamic) as Dynamic
    keyStr = m.keyToString_AnyN_Str_k_(key)
    if not m.get_map().DoesExist(keyStr) then
        return invalid
    end if
    return m.get_map()[keyStr]
end function

function LinkedHashMap_put_AnyN_AnyN_AnyN_k_(key as Dynamic, value as Dynamic) as Dynamic
    keyStr = m.keyToString_AnyN_Str_k_(key)
    __when_tmp0 = invalid
    if m.get_map().DoesExist(keyStr) then
        __when_tmp0 = m.get_map()[keyStr]
    else if true then
        __when_tmp0 = invalid
    end if
    oldValue = __when_tmp0

    m.get_map().AddReplace(keyStr, value)
    return oldValue
end function

function LinkedHashMap_remove_AnyN_AnyN_k_(key as Dynamic) as Dynamic
    keyStr = m.keyToString_AnyN_Str_k_(key)
    if not m.get_map().DoesExist(keyStr) then
        return invalid
    end if
    oldValue = m.get_map()[keyStr]
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

sub LinkedHashMap_putAll_MapAnyNAnyN_k_(from as Object)
    for each entry in from.get_entries().array
        m.put_AnyN_AnyN_AnyN_k_(entry.get_key(), entry.get_value())

    end for
end sub

sub LinkedHashMap_clear()
    m.get_map().Clear()
    m.get_keyOrder().Clear()
    m.set__size(0)
end sub

function LinkedHashMap_equals_AnyN_Z_k_(other as Dynamic) as Boolean
    if EQEQEQ_AnyN_AnyN_Z_k_(other, m) then
        return true
    end if
    if not __kotlin_isInstanceOf(other, "Map") then
        return false
    end if
    if other.get_size() <> m.get_size() then
        return false
    end if
    for each entry in m.get_entries().array
        otherMap = other
        otherValue = otherMap.get_AnyN_AnyN_k_(entry.get_key())
        if entry.get_value() <> otherValue then
            return false
        end if
        if (otherValue = invalid) and not otherMap.containsKey_AnyN_Z_k_(entry.get_key()) then
            return false
        end if

    end for
    return true
end function

function LinkedHashMap_hashCode_I_k_() as Integer
    result = 0
    for each entry in m.get_entries().array
        result = (result + entry.hashCode())

    end for
    return result
end function

function LinkedHashMap_toString_Str_k_() as String
    entries = joinToString_77mgo1_k_(m.get_entries(), ", ", "{", "}", {invoke: function(it as Object) as Object
        return (it.get_key() + "=") + it.get_value()
    end function})
    return entries
end function

function LinkedHashMap_keyToString_AnyN_Str_k_(key as Dynamic) as String
    return (function(Str, key)
        if key = invalid then return "null" else return (function(Str, key)
            if (Type(key) = "String") or (Type(key) = "roString") then return key else return (function(Str, key)
                if ((((((Type(key) = "Integer") or (Type(key) = "LongInteger")) or (Type(key) = "Float")) or (Type(key) = "Double")) or (Type(key) = "roInt")) or (Type(key) = "roFloat")) or (Type(key) = "roDouble") then return Str(key) else return (function(key)
                    if (Type(key) = "Boolean") or (Type(key) = "roBoolean") then return (function(key)
                        if key then return "true" else return "false"
                    end function)(key) else return key.toString()
                end function)(key)
            end function)(Str, key)
        end function)(Str, key)
    end function)(Str, key)
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
    return m.get__size()
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
    return m.get_map().isEmpty_Z_k_()
end function

function LinkedHashMap_LinkedKeySet_contains_AnyN_Z_k_(element as Dynamic) as Boolean
    return m.get_map().containsKey_AnyN_Z_k_(element)
end function

function LinkedHashMap_LinkedKeySet_containsAll_CollectionAnyN_Z_k_(elements as Object) as Boolean
    for each element in elements.array
        if not m.contains_AnyN_Z_k_(element) then
            return false
        end if
    end for
    return true
end function

function LinkedHashMap_LinkedKeySet_iterator_MutableIteratorAnyN_k_() as Object
    return Anon_2df6f170_create_AnonAnyNAnyN_k_()
end function

function LinkedHashMap_LinkedKeySet_add_AnyN_Z_k_(element as Dynamic) as Boolean
    throw UnsupportedOperationException_create_StrN_UnsupportedOperationException_k_("Add not supported on key set")
end function

function LinkedHashMap_LinkedKeySet_addAll_CollectionAnyN_Z_k_(elements as Object) as Boolean
    throw UnsupportedOperationException_create_StrN_UnsupportedOperationException_k_("Add not supported on key set")
end function

function LinkedHashMap_LinkedKeySet_remove_AnyN_Z_k_(element as Dynamic) as Boolean
    if not m.get_map().containsKey_AnyN_Z_k_(element) then
        return false
    end if
    m.get_map().remove_AnyN_AnyN_k_(element)
    return true
end function

function LinkedHashMap_LinkedKeySet_removeAll_CollectionAnyN_Z_k_(elements as Object) as Boolean
    modified = false
    for each element in elements.array
        if m.remove_AnyN_Z_k_(element) then
            modified = true
        end if
    end for
    return modified
end function

function LinkedHashMap_LinkedKeySet_retainAll_CollectionAnyN_Z_k_(elements as Object) as Boolean
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

sub LinkedHashMap_LinkedKeySet_clear()
    m.get_map().clear()
end sub

function LinkedHashMap_LinkedKeySet_get_map_LinkedHashMapAnyNAnyN_k_() as Object
    return m.map
end function

function LinkedHashMap_LinkedKeySet_get_size_I_k_() as Integer
    return m.get_map().get_size()
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
    return m.get_map().isEmpty_Z_k_()
end function

function LinkedHashMap_LinkedValueCollection_contains_AnyN_Z_k_(element as Dynamic) as Boolean
    return m.get_map().containsValue_AnyN_Z_k_(element)
end function

function LinkedHashMap_LinkedValueCollection_containsAll_CollectionAnyN_Z_k_(elements as Object) as Boolean
    for each element in elements.array
        if not m.contains_AnyN_Z_k_(element) then
            return false
        end if
    end for
    return true
end function

function LinkedHashMap_LinkedValueCollection_iterator_MutableIteratorAnyN_k_() as Object
    return Anon_36b9420a_create_AnonAnyNAnyN_k_()
end function

function LinkedHashMap_LinkedValueCollection_add_AnyN_Z_k_(element as Dynamic) as Boolean
    throw UnsupportedOperationException_create_StrN_UnsupportedOperationException_k_("Add not supported on value collection")
end function

function LinkedHashMap_LinkedValueCollection_addAll_CollectionAnyN_Z_k_(elements as Object) as Boolean
    throw UnsupportedOperationException_create_StrN_UnsupportedOperationException_k_("Add not supported on value collection")
end function

function LinkedHashMap_LinkedValueCollection_remove_AnyN_Z_k_(element as Dynamic) as Boolean
    iter = m.iterator_MutableIteratorAnyN_k_()
    while iter.hasNext_Z_k_()
        if iter.next_AnyN_k_() = element then
            iter.remove()
            return true
        end if
    end while
    return false
end function

function LinkedHashMap_LinkedValueCollection_removeAll_CollectionAnyN_Z_k_(elements as Object) as Boolean
    modified = false
    for each element in elements.array
        while m.remove_AnyN_Z_k_(element)
            modified = true
        end while

    end for
    return modified
end function

function LinkedHashMap_LinkedValueCollection_retainAll_CollectionAnyN_Z_k_(elements as Object) as Boolean
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

sub LinkedHashMap_LinkedValueCollection_clear()
    m.get_map().clear()
end sub

function LinkedHashMap_LinkedValueCollection_get_map_LinkedHashMapAnyNAnyN_k_() as Object
    return m.map
end function

function LinkedHashMap_LinkedValueCollection_get_size_I_k_() as Integer
    return m.get_map().get_size()
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
    return m.get_map().isEmpty_Z_k_()
end function

function LinkedHashMap_LinkedEntrySet_contains_MutableEntryAnyNAnyN_Z_k_(element as Object) as Boolean
    value = m.get_map().get_AnyN_AnyN_k_(element.get_key())
    return (value = element.get_value()) and ((value <> invalid) or m.get_map().containsKey_AnyN_Z_k_(element.get_key()))
end function

function LinkedHashMap_LinkedEntrySet_containsAll_CollectionMutableEntryAnyNAnyN_Z_k_(elements as Object) as Boolean
    for each element in elements.array
        if not m.contains_MutableEntryAnyNAnyN_Z_k_(element) then
            return false
        end if
    end for
    return true
end function

function LinkedHashMap_LinkedEntrySet_iterator_MutableIteratorMutableEntryAnyNAnyN_k_() as Object
    return Anon_314f7301_create_AnonAnyNAnyN_k_()
end function

function LinkedHashMap_LinkedEntrySet_add_MutableEntryAnyNAnyN_Z_k_(element as Object) as Boolean
    throw UnsupportedOperationException_create_StrN_UnsupportedOperationException_k_("Add not supported on entry set")
end function

function LinkedHashMap_LinkedEntrySet_addAll_CollectionMutableEntryAnyNAnyN_Z_k_(elements as Object) as Boolean
    throw UnsupportedOperationException_create_StrN_UnsupportedOperationException_k_("Add not supported on entry set")
end function

function LinkedHashMap_LinkedEntrySet_remove_MutableEntryAnyNAnyN_Z_k_(element as Object) as Boolean
    if not m.contains_MutableEntryAnyNAnyN_Z_k_(element) then
        return false
    end if
    m.get_map().remove_AnyN_AnyN_k_(element.get_key())
    return true
end function

function LinkedHashMap_LinkedEntrySet_removeAll_CollectionMutableEntryAnyNAnyN_Z_k_(elements as Object) as Boolean
    modified = false
    for each element in elements.array
        if m.remove_MutableEntryAnyNAnyN_Z_k_(element) then
            modified = true
        end if
    end for
    return modified
end function

function LinkedHashMap_LinkedEntrySet_retainAll_CollectionMutableEntryAnyNAnyN_Z_k_(elements as Object) as Boolean
    modified = false
    iter = m.iterator_MutableIteratorMutableEntryAnyNAnyN_k_()
    while iter.hasNext_Z_k_()
        if not elements.contains_AnyN_Z_k_(iter.next_AnyN_k_()) then
            iter.remove()
            modified = true
        end if
    end while
    return modified
end function

sub LinkedHashMap_LinkedEntrySet_clear()
    m.get_map().clear()
end sub

function LinkedHashMap_LinkedEntrySet_get_map_LinkedHashMapAnyNAnyN_k_() as Object
    return m.map
end function

function LinkedHashMap_LinkedEntrySet_get_size_I_k_() as Integer
    return m.get_map().get_size()
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
    oldValue = m.get_value()
    m.set_value(newValue)
    return oldValue
end function

function LinkedHashMap_SimpleEntry_equals_AnyN_Z_k_(other as Dynamic) as Boolean
    if not __kotlin_isInstanceOf(other, "Map_Entry") then
        return false
    end if
    return (m.get_key() = other.get_key()) and (m.get_value() = other.get_value())
end function

function LinkedHashMap_SimpleEntry_hashCode_I_k_() as Integer
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
    return __when_tmp5.xor_I_I_k_(__when_tmp7)

end function

function LinkedHashMap_SimpleEntry_toString_Str_k_() as String
    return (m.get_key() + "=") + m.get_value()
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
