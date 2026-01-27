function LinkedHashMap_create_k_() as Object
    this = {}
    this.__type = "LinkedHashMap"
    this.__proto = ["LinkedHashMap", "MutableMap", "Map"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
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
    this.__get_map = LinkedHashMap___get_map_k_
    this.__set_map = LinkedHashMap___set_map_Dynamic_k_
    this.__get_keyOrder = LinkedHashMap___get_keyOrder_k_
    this.__set_keyOrder = LinkedHashMap___set_keyOrder_Dynamic_k_
    this.__get__size = LinkedHashMap___get__size_k_
    this.__set__size = LinkedHashMap___set__size_I_k_
    this.__get_size = LinkedHashMap___get_size_k_
    this.__get_keys = LinkedHashMap___get_keys_k_
    this.__get_values = LinkedHashMap___get_values_k_
    this.__get_entries = LinkedHashMap___get_entries_k_
    this._size = 0
    this.__set_map(CreateObject("roAssociativeArray"))
    this.__set_keyOrder(CreateObject("roArray", 0, true))
    this.__set__size(0)
    return this
end function

function LinkedHashMap_create_I_k_(initialCapacity as Integer) as Object
    this = {}
    this.__type = "LinkedHashMap"
    this.__proto = ["LinkedHashMap", "MutableMap", "Map"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
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
    this.__get_map = LinkedHashMap___get_map_k_
    this.__set_map = LinkedHashMap___set_map_Dynamic_k_
    this.__get_keyOrder = LinkedHashMap___get_keyOrder_k_
    this.__set_keyOrder = LinkedHashMap___set_keyOrder_Dynamic_k_
    this.__get__size = LinkedHashMap___get__size_k_
    this.__set__size = LinkedHashMap___set__size_I_k_
    this.__get_size = LinkedHashMap___get_size_k_
    this.__get_keys = LinkedHashMap___get_keys_k_
    this.__get_values = LinkedHashMap___get_values_k_
    this.__get_entries = LinkedHashMap___get_entries_k_
    this._size = 0
    tmp0 = initialCapacity >= 0
    tmp_ret_1 = invalid
    while true
        value = tmp0
        if not value then
            tmp_ret_0 = invalid
            while true
                tmp_ret_0 = ("Negative initial capacity: " + __kotlin_numToStr_I_k_(initialCapacity))
                exit while
            end while
            throw IllegalArgumentException_create_StrN_k_(toString_AnyN_k_(tmp_ret_0))
        end if
        tmp_ret_1 = invalid
        exit while
    end while

    this.__set_map(CreateObject("roAssociativeArray"))
    this.__set_keyOrder(CreateObject("roArray", 0, true))
    this.__set__size(0)
    return this
end function

function LinkedHashMap_create_I_F_k_(initialCapacity as Integer, loadFactor as Float) as Object
    this = {}
    this.__type = "LinkedHashMap"
    this.__proto = ["LinkedHashMap", "MutableMap", "Map"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
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
    this.__get_map = LinkedHashMap___get_map_k_
    this.__set_map = LinkedHashMap___set_map_Dynamic_k_
    this.__get_keyOrder = LinkedHashMap___get_keyOrder_k_
    this.__set_keyOrder = LinkedHashMap___set_keyOrder_Dynamic_k_
    this.__get__size = LinkedHashMap___get__size_k_
    this.__set__size = LinkedHashMap___set__size_I_k_
    this.__get_size = LinkedHashMap___get_size_k_
    this.__get_keys = LinkedHashMap___get_keys_k_
    this.__get_values = LinkedHashMap___get_values_k_
    this.__get_entries = LinkedHashMap___get_entries_k_
    this._size = 0
    tmp0 = initialCapacity >= 0
    tmp_ret_1_1 = invalid
    while true
        value = tmp0
        if not value then
            tmp_ret_0_1 = invalid
            while true
                tmp_ret_0_1 = ("Negative initial capacity: " + __kotlin_numToStr_I_k_(initialCapacity))
                exit while
            end while
            throw IllegalArgumentException_create_StrN_k_(toString_AnyN_k_(tmp_ret_0_1))
        end if
        tmp_ret_1_1 = invalid
        exit while
    end while

    tmp0 = loadFactor > 0
    tmp_ret_3 = invalid
    while true
        value = tmp0
        if not value then
            tmp_ret_2 = invalid
            while true
                tmp_ret_2 = ("Non-positive load factor: " + __kotlin_numToStr_F_k_(loadFactor))
                exit while
            end while
            throw IllegalArgumentException_create_StrN_k_(toString_AnyN_k_(tmp_ret_2))
        end if
        tmp_ret_3 = invalid
        exit while
    end while

    this.__set_map(CreateObject("roAssociativeArray"))
    this.__set_keyOrder(CreateObject("roArray", 0, true))
    this.__set__size(0)
    return this
end function

function LinkedHashMap_create_Map_k_(original as Object) as Object
    this = {}
    this.__type = "LinkedHashMap"
    this.__proto = ["LinkedHashMap", "MutableMap", "Map"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
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
    this.__get_map = LinkedHashMap___get_map_k_
    this.__set_map = LinkedHashMap___set_map_Dynamic_k_
    this.__get_keyOrder = LinkedHashMap___get_keyOrder_k_
    this.__set_keyOrder = LinkedHashMap___set_keyOrder_Dynamic_k_
    this.__get__size = LinkedHashMap___get__size_k_
    this.__set__size = LinkedHashMap___set__size_I_k_
    this.__get_size = LinkedHashMap___get_size_k_
    this.__get_keys = LinkedHashMap___get_keys_k_
    this.__get_values = LinkedHashMap___get_values_k_
    this.__get_entries = LinkedHashMap___get_entries_k_
    this._size = 0
    this.__set_map(CreateObject("roAssociativeArray"))
    this.__set_keyOrder(CreateObject("roArray", 0, true))
    this.__set__size(0)
    this.putAll_Map_k_(original)
    return this
end function

function LinkedHashMap_isEmpty_k_() as Boolean
    return m.__get__size() = 0
end function

function LinkedHashMap_containsKey_AnyN_k_(key as Dynamic) as Boolean
    keyStr = m.keyToString_AnyN_k_(key)
    return m.__get_map().DoesExist(keyStr)
end function

function LinkedHashMap_containsValue_AnyN_k_(value as Dynamic) as Boolean
    count = m.__get_keyOrder().Count()
    i = 0
    while i < count
        keyStr = m.__get_keyOrder()[i]
        entry = m.__get_map()[keyStr]
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
    if not m.__get_map().DoesExist(keyStr) then
        return invalid
    end if
    entry = m.__get_map()[keyStr]
    return entry.v
end function

function LinkedHashMap_put_AnyN_AnyN_k_(key as Dynamic, value as Dynamic) as Dynamic
    keyStr = m.keyToString_AnyN_k_(key)
    __when_tmp0 = invalid
    if m.__get_map().DoesExist(keyStr) then
        oldEntry = m.__get_map()[keyStr]
        __when_tmp0 = oldEntry.v
    else if true then
        m.__get_keyOrder().Push(keyStr)
        m.__set__size(m.__get__size() + 1)
        __when_tmp0 = invalid
    end if
    oldValue = __when_tmp0

    entry = {k: key, v: value}
    m.__get_map().AddReplace(keyStr, entry)
    return oldValue
end function

function LinkedHashMap_remove_AnyN_k_(key as Dynamic) as Dynamic
    keyStr = m.keyToString_AnyN_k_(key)
    if not m.__get_map().DoesExist(keyStr) then
        return invalid
    end if
    oldEntry = m.__get_map()[keyStr]
    oldValue = oldEntry.v
    m.__get_map().Delete(keyStr)
    count = m.__get_keyOrder().Count()
    i = 0
    while i < count
        if m.__get_keyOrder()[i] = keyStr then
            m.__get_keyOrder().Delete(i)
            exit while
        end if
        i = (i + 1)
    end while
    m.__set__size(m.__get__size() - 1)
    return oldValue
end function

sub LinkedHashMap_putAll_Map_k_(from as Object)
    __iter_183 = from.__get_entries().iterator_k_()
    while __iter_183.hasNext_k_()
        entry = __iter_183.next_k_()
        m.put_AnyN_AnyN_k_(entry.__get_key(), entry.__get_value())

    end while

end sub

sub LinkedHashMap_clear_k_()
    m.__get_map().Clear()
    m.__get_keyOrder().Clear()
    m.__set__size(0)
end sub

function LinkedHashMap_equals_AnyN_k_(other as Dynamic) as Boolean
    if __kotlin_identityEquals(other, m) then
        return true
    end if
    if not __kotlin_isInstanceOf(other, "Map") then
        return false
    end if
    if other.__get_size() <> m.__get_size() then
        return false
    end if
    __iter_184 = m.__get_entries().iterator_k_()
    while __iter_184.hasNext_k_()
        entry = __iter_184.next_k_()
        otherMap = other
        otherValue = otherMap.get_AnyN_k_(entry.__get_key())
        if not brsStructuralEquals_AnyN_AnyN_k_(entry.__get_value(), otherValue) then
            return false
        end if
        if (otherValue = invalid) and not otherMap.containsKey_AnyN_k_(entry.__get_key()) then
            return false
        end if

    end while

    return true
end function

function LinkedHashMap_hashCode_k_() as Integer
    result = 0
    __iter_185 = m.__get_entries().iterator_k_()
    while __iter_185.hasNext_k_()
        entry = __iter_185.next_k_()
        result = (result + entry.hashCode())

    end while

    return result
end function

function LinkedHashMap_toString_k_() as String
    tmp0 = m.__get_entries()
    tmp2 = ", "
    tmp4 = "{"
    tmp6 = "}"
    entries = tmp_ret_1

    tmp_ret_1 = invalid
    while true
        this = tmp0
        separator = tmp2
        prefix = tmp4
        postfix = tmp6
        limit = -1
        truncated = "..."
        sb = StringBuilder_create_k_()
        sb.append_CharSequenceN_k_(prefix)
        count = 0
        __iter_186 = this.iterator_k_()
        while __iter_186.hasNext_k_()
            element = __iter_186.next_k_()
            count = (count + 1)
            if count > 1 then
                sb.append_CharSequenceN_k_(separator)
            end if
            if (limit < 0) or (count <= limit) then
                tmp0_1 = element
                tmp_ret_0 = invalid

                while true
                    it = tmp0_1
                    tmp_ret_0 = ((toString_AnyN_k_(it.__get_key()) + "=") + toString_AnyN_k_(it.__get_value()))
                    exit while
                end while
                sb.append_CharSequenceN_k_(tmp_ret_0)
            else if true then
                exit while
            end if

        end while
        if (limit >= 0) and (count > limit) then
            sb.append_CharSequenceN_k_(truncated)
        end if
        sb.append_CharSequenceN_k_(postfix)
        tmp_ret_1 = sb.toString()
        exit while
    end while
    return entries

end function

function LinkedHashMap_keyToString_AnyN_k_(key as Dynamic) as String
    return toString_AnyN_k_(key)
end function

function LinkedHashMap___get_map_k_() as Object
    return m.map
end function

sub LinkedHashMap___set_map_Dynamic_k_(value as Object)
    m.map = value
end sub

function LinkedHashMap___get_keyOrder_k_() as Object
    return m.keyOrder
end function

sub LinkedHashMap___set_keyOrder_Dynamic_k_(value as Object)
    m.keyOrder = value
end sub

function LinkedHashMap___get__size_k_() as Integer
    return m._size
end function

sub LinkedHashMap___set__size_I_k_(value as Integer)
    m._size = value
end sub

function LinkedHashMap___get_size_k_() as Integer
    return m.__get__size()
end function

function LinkedHashMap___get_keys_k_() as Object
    return LinkedHashMap_LinkedKeySet_create_LinkedHashMap_k_(m)
end function

function LinkedHashMap___get_values_k_() as Object
    return LinkedHashMap_LinkedValueCollection_create_LinkedHashMap_k_(m)
end function

function LinkedHashMap___get_entries_k_() as Object
    return LinkedHashMap_LinkedEntrySet_create_LinkedHashMap_k_(m)
end function

function LinkedHashMap_LinkedKeySet_create_LinkedHashMap_k_(map as Object) as Object
    this = {}
    this.__type = "LinkedHashMap_LinkedKeySet"
    this.__proto = ["LinkedHashMap_LinkedKeySet", "MutableSet", "Set", "Collection", "Iterable", "MutableCollection", "MutableIterable"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
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
    this.__get_map = LinkedHashMap_LinkedKeySet___get_map_k_
    this.__get_array = LinkedHashMap_LinkedKeySet___get_array_k_
    this.__get_size = LinkedHashMap_LinkedKeySet___get_size_k_
    this.map = map
    return this
end function

function LinkedHashMap_LinkedKeySet_isEmpty_k_() as Boolean
    return m.__get_map().isEmpty_k_()
end function

function LinkedHashMap_LinkedKeySet_contains_AnyN_k_(element as Dynamic) as Boolean
    return m.__get_map().containsKey_AnyN_k_(element)
end function

function LinkedHashMap_LinkedKeySet_containsAll_Collection_k_(elements as Object) as Boolean
    __iter_187 = elements.iterator_k_()
    while __iter_187.hasNext_k_()
        element = __iter_187.next_k_()
        if not m.contains_AnyN_k_(element) then
            return false
        end if
    end while

    return true
end function

function LinkedHashMap_LinkedKeySet_iterator_k_() as Object
    return LinkedHashMap_LinkedKeyIterator_create_LinkedHashMap_k_(m.__get_map())
end function

function LinkedHashMap_LinkedKeySet_add_AnyN_k_(element as Dynamic) as Boolean
    throw UnsupportedOperationException_create_StrN_k_("Add not supported on key set")
end function

function LinkedHashMap_LinkedKeySet_addAll_Collection_k_(elements as Object) as Boolean
    throw UnsupportedOperationException_create_StrN_k_("Add not supported on key set")
end function

function LinkedHashMap_LinkedKeySet_remove_AnyN_k_(element as Dynamic) as Boolean
    if not m.__get_map().containsKey_AnyN_k_(element) then
        return false
    end if
    m.__get_map().remove_AnyN_k_(element)
    return true
end function

function LinkedHashMap_LinkedKeySet_removeAll_Collection_k_(elements as Object) as Boolean
    modified = false
    __iter_188 = elements.iterator_k_()
    while __iter_188.hasNext_k_()
        element = __iter_188.next_k_()
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
    m.__get_map().clear_k_()
end sub

function LinkedHashMap_LinkedKeySet___get_map_k_() as Object
    return m.map
end function

function LinkedHashMap_LinkedKeySet___get_array_k_() as Object
    return m.__get_map().__get_keyOrder()
end function

function LinkedHashMap_LinkedKeySet___get_size_k_() as Integer
    return m.__get_map().__get_size()
end function

function LinkedHashMap_LinkedValueCollection_create_LinkedHashMap_k_(map as Object) as Object
    this = {}
    this.__type = "LinkedHashMap_LinkedValueCollection"
    this.__proto = ["LinkedHashMap_LinkedValueCollection", "MutableCollection", "Collection", "Iterable", "MutableIterable"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
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
    this.__get_map = LinkedHashMap_LinkedValueCollection___get_map_k_
    this.__get_array = LinkedHashMap_LinkedValueCollection___get_array_k_
    this.__get_size = LinkedHashMap_LinkedValueCollection___get_size_k_
    this.map = map
    return this
end function

function LinkedHashMap_LinkedValueCollection_isEmpty_k_() as Boolean
    return m.__get_map().isEmpty_k_()
end function

function LinkedHashMap_LinkedValueCollection_contains_AnyN_k_(element as Dynamic) as Boolean
    return m.__get_map().containsValue_AnyN_k_(element)
end function

function LinkedHashMap_LinkedValueCollection_containsAll_Collection_k_(elements as Object) as Boolean
    __iter_189 = elements.iterator_k_()
    while __iter_189.hasNext_k_()
        element = __iter_189.next_k_()
        if not m.contains_AnyN_k_(element) then
            return false
        end if
    end while

    return true
end function

function LinkedHashMap_LinkedValueCollection_iterator_k_() as Object
    return LinkedHashMap_LinkedValueIterator_create_LinkedHashMap_k_(m.__get_map())
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
    __iter_190 = elements.iterator_k_()
    while __iter_190.hasNext_k_()
        element = __iter_190.next_k_()
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
    m.__get_map().clear_k_()
end sub

function LinkedHashMap_LinkedValueCollection___get_map_k_() as Object
    return m.map
end function

function LinkedHashMap_LinkedValueCollection___get_array_k_() as Object
    result = CreateObject("roArray", 0, true)
    keyCount = m.__get_map().__get_keyOrder().Count()
    i = 0
    while i < keyCount
        keyStr = m.__get_map().__get_keyOrder()[i]
        value = m.__get_map().__get_map()[keyStr]
        result.Push(value)
        i = (i + 1)
    end while
    return result
end function

function LinkedHashMap_LinkedValueCollection___get_size_k_() as Integer
    return m.__get_map().__get_size()
end function

function LinkedHashMap_LinkedEntrySet_create_LinkedHashMap_k_(map as Object) as Object
    this = {}
    this.__type = "LinkedHashMap_LinkedEntrySet"
    this.__proto = ["LinkedHashMap_LinkedEntrySet", "MutableSet", "Set", "Collection", "Iterable", "MutableCollection", "MutableIterable"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.isEmpty_k_ = LinkedHashMap_LinkedEntrySet_isEmpty_k_
    this.contains_AnyN_k_ = LinkedHashMap_LinkedEntrySet_contains_AnyN_k_
    this.containsAll_Collection_k_ = LinkedHashMap_LinkedEntrySet_containsAll_Collection_k_
    this.iterator_k_ = LinkedHashMap_LinkedEntrySet_iterator_k_
    this.add_AnyN_k_ = LinkedHashMap_LinkedEntrySet_add_AnyN_k_
    this.addAll_Collection_k_ = LinkedHashMap_LinkedEntrySet_addAll_Collection_k_
    this.remove_AnyN_k_ = LinkedHashMap_LinkedEntrySet_remove_AnyN_k_
    this.removeAll_Collection_k_ = LinkedHashMap_LinkedEntrySet_removeAll_Collection_k_
    this.retainAll_Collection_k_ = LinkedHashMap_LinkedEntrySet_retainAll_Collection_k_
    this.clear_k_ = LinkedHashMap_LinkedEntrySet_clear_k_
    this.__get_map = LinkedHashMap_LinkedEntrySet___get_map_k_
    this.__get_array = LinkedHashMap_LinkedEntrySet___get_array_k_
    this.__get_size = LinkedHashMap_LinkedEntrySet___get_size_k_
    this.map = map
    return this
end function

function LinkedHashMap_LinkedEntrySet_isEmpty_k_() as Boolean
    return m.__get_map().isEmpty_k_()
end function

function LinkedHashMap_LinkedEntrySet_contains_AnyN_k_(element as Object) as Boolean
    value = m.__get_map().get_AnyN_k_(element.__get_key())
    return brsStructuralEquals_AnyN_AnyN_k_(value, element.__get_value()) and ((value <> invalid) or m.__get_map().containsKey_AnyN_k_(element.__get_key()))
end function

function LinkedHashMap_LinkedEntrySet_containsAll_Collection_k_(elements as Object) as Boolean
    __iter_191 = elements.iterator_k_()
    while __iter_191.hasNext_k_()
        element = __iter_191.next_k_()
        if not m.contains_AnyN_k_(element) then
            return false
        end if
    end while

    return true
end function

function LinkedHashMap_LinkedEntrySet_iterator_k_() as Object
    return LinkedHashMap_LinkedEntryIterator_create_LinkedHashMap_k_(m.__get_map())
end function

function LinkedHashMap_LinkedEntrySet_add_AnyN_k_(element as Object) as Boolean
    throw UnsupportedOperationException_create_StrN_k_("Add not supported on entry set")
end function

function LinkedHashMap_LinkedEntrySet_addAll_Collection_k_(elements as Object) as Boolean
    throw UnsupportedOperationException_create_StrN_k_("Add not supported on entry set")
end function

function LinkedHashMap_LinkedEntrySet_remove_AnyN_k_(element as Object) as Boolean
    if not m.contains_AnyN_k_(element) then
        return false
    end if
    m.__get_map().remove_AnyN_k_(element.__get_key())
    return true
end function

function LinkedHashMap_LinkedEntrySet_removeAll_Collection_k_(elements as Object) as Boolean
    modified = false
    __iter_192 = elements.iterator_k_()
    while __iter_192.hasNext_k_()
        element = __iter_192.next_k_()
        if m.remove_AnyN_k_(element) then
            modified = true
        end if
    end while

    return modified
end function

function LinkedHashMap_LinkedEntrySet_retainAll_Collection_k_(elements as Object) as Boolean
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
    m.__get_map().clear_k_()
end sub

function LinkedHashMap_LinkedEntrySet___get_map_k_() as Object
    return m.map
end function

function LinkedHashMap_LinkedEntrySet___get_array_k_() as Object
    result = CreateObject("roArray", 0, true)
    keyCount = m.__get_map().__get_keyOrder().Count()
    i = 0
    while i < keyCount
        keyStr = m.__get_map().__get_keyOrder()[i]
        value = m.__get_map().__get_map()[keyStr]
        result.Push(LinkedHashMap_SimpleEntry_create_AnyN_AnyN_k_(keyStr, value))
        i = (i + 1)
    end while
    return result
end function

function LinkedHashMap_LinkedEntrySet___get_size_k_() as Integer
    return m.__get_map().__get_size()
end function

function LinkedHashMap_LinkedKeyIterator_create_LinkedHashMap_k_(map as Object) as Object
    this = {}
    this.__type = "LinkedHashMap_LinkedKeyIterator"
    this.__proto = ["LinkedHashMap_LinkedKeyIterator", "MutableIterator", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.hasNext_k_ = LinkedHashMap_LinkedKeyIterator_hasNext_k_
    this.next_k_ = LinkedHashMap_LinkedKeyIterator_next_k_
    this.remove_k_ = LinkedHashMap_LinkedKeyIterator_remove_k_
    this.__get_map = LinkedHashMap_LinkedKeyIterator___get_map_k_
    this.__get_currentIndex = LinkedHashMap_LinkedKeyIterator___get_currentIndex_k_
    this.__set_currentIndex = LinkedHashMap_LinkedKeyIterator___set_currentIndex_I_k_
    this.__get_orderCount = LinkedHashMap_LinkedKeyIterator___get_orderCount_k_
    this.__set_orderCount = LinkedHashMap_LinkedKeyIterator___set_orderCount_I_k_
    this.__get_lastReturnedKey = LinkedHashMap_LinkedKeyIterator___get_lastReturnedKey_k_
    this.__set_lastReturnedKey = LinkedHashMap_LinkedKeyIterator___set_lastReturnedKey_AnyN_k_
    this.__get_canRemove = LinkedHashMap_LinkedKeyIterator___get_canRemove_k_
    this.__set_canRemove = LinkedHashMap_LinkedKeyIterator___set_canRemove_Z_k_
    this.map = map
    this.currentIndex = 0
    this.orderCount = this.__get_map().__get_keyOrder().Count()
    this.lastReturnedKey = invalid
    this.canRemove = false
    return this
end function

function LinkedHashMap_LinkedKeyIterator_hasNext_k_() as Boolean
    return m.__get_currentIndex() < m.__get_orderCount()
end function

function LinkedHashMap_LinkedKeyIterator_next_k_() as Dynamic
    if not m.hasNext_k_() then
        throw NoSuchElementException_create_k_()
    end if
    keyStr = m.__get_map().__get_keyOrder()[m.__get_currentIndex()]
    entry = m.__get_map().__get_map()[keyStr]
    key = entry.k
    m.__set_lastReturnedKey(key)
    m.__set_currentIndex(m.__get_currentIndex() + 1)
    m.__set_canRemove(true)
    return key
end function

sub LinkedHashMap_LinkedKeyIterator_remove_k_()
    tmp0 = m.__get_canRemove()
    while true
        value = tmp0
        if not value then
            while true
                exit while
            end while
            throw IllegalStateException_create_StrN_k_(toString_AnyN_k_(invalid))
        end if
        exit while
    end while

    tmp0_elvis_lhs = m.__get_lastReturnedKey()
    __when_tmp1 = invalid
    if tmp0_elvis_lhs = invalid then
        throw IllegalStateException_create_k_()
    else if true then
        __when_tmp1 = tmp0_elvis_lhs
    end if
    key = __when_tmp1

    m.__get_map().remove_AnyN_k_(key)
    m.__set_currentIndex(m.__get_currentIndex() - 1)
    m.__set_orderCount(m.__get_orderCount() - 1)
    m.__set_canRemove(false)
end sub

function LinkedHashMap_LinkedKeyIterator___get_map_k_() as Object
    return m.map
end function

function LinkedHashMap_LinkedKeyIterator___get_currentIndex_k_() as Integer
    return m.currentIndex
end function

sub LinkedHashMap_LinkedKeyIterator___set_currentIndex_I_k_(value as Integer)
    m.currentIndex = value
end sub

function LinkedHashMap_LinkedKeyIterator___get_orderCount_k_() as Integer
    return m.orderCount
end function

sub LinkedHashMap_LinkedKeyIterator___set_orderCount_I_k_(value as Integer)
    m.orderCount = value
end sub

function LinkedHashMap_LinkedKeyIterator___get_lastReturnedKey_k_() as Dynamic
    return m.lastReturnedKey
end function

sub LinkedHashMap_LinkedKeyIterator___set_lastReturnedKey_AnyN_k_(value as Dynamic)
    m.lastReturnedKey = value
end sub

function LinkedHashMap_LinkedKeyIterator___get_canRemove_k_() as Boolean
    return m.canRemove
end function

sub LinkedHashMap_LinkedKeyIterator___set_canRemove_Z_k_(value as Boolean)
    m.canRemove = value
end sub

function LinkedHashMap_LinkedValueIterator_create_LinkedHashMap_k_(map as Object) as Object
    this = {}
    this.__type = "LinkedHashMap_LinkedValueIterator"
    this.__proto = ["LinkedHashMap_LinkedValueIterator", "MutableIterator", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.hasNext_k_ = LinkedHashMap_LinkedValueIterator_hasNext_k_
    this.next_k_ = LinkedHashMap_LinkedValueIterator_next_k_
    this.remove_k_ = LinkedHashMap_LinkedValueIterator_remove_k_
    this.__get_map = LinkedHashMap_LinkedValueIterator___get_map_k_
    this.__get_currentIndex = LinkedHashMap_LinkedValueIterator___get_currentIndex_k_
    this.__set_currentIndex = LinkedHashMap_LinkedValueIterator___set_currentIndex_I_k_
    this.__get_orderCount = LinkedHashMap_LinkedValueIterator___get_orderCount_k_
    this.__set_orderCount = LinkedHashMap_LinkedValueIterator___set_orderCount_I_k_
    this.__get_lastReturnedKey = LinkedHashMap_LinkedValueIterator___get_lastReturnedKey_k_
    this.__set_lastReturnedKey = LinkedHashMap_LinkedValueIterator___set_lastReturnedKey_AnyN_k_
    this.__get_canRemove = LinkedHashMap_LinkedValueIterator___get_canRemove_k_
    this.__set_canRemove = LinkedHashMap_LinkedValueIterator___set_canRemove_Z_k_
    this.map = map
    this.currentIndex = 0
    this.orderCount = this.__get_map().__get_keyOrder().Count()
    this.lastReturnedKey = invalid
    this.canRemove = false
    return this
end function

function LinkedHashMap_LinkedValueIterator_hasNext_k_() as Boolean
    return m.__get_currentIndex() < m.__get_orderCount()
end function

function LinkedHashMap_LinkedValueIterator_next_k_() as Dynamic
    if not m.hasNext_k_() then
        throw NoSuchElementException_create_k_()
    end if
    keyStr = m.__get_map().__get_keyOrder()[m.__get_currentIndex()]
    entry = m.__get_map().__get_map()[keyStr]
    key = entry.k
    value = entry.v
    m.__set_lastReturnedKey(key)
    m.__set_currentIndex(m.__get_currentIndex() + 1)
    m.__set_canRemove(true)
    return value
end function

sub LinkedHashMap_LinkedValueIterator_remove_k_()
    tmp0 = m.__get_canRemove()
    while true
        value = tmp0
        if not value then
            while true
                exit while
            end while
            throw IllegalStateException_create_StrN_k_(toString_AnyN_k_(invalid))
        end if
        exit while
    end while

    tmp0_elvis_lhs = m.__get_lastReturnedKey()
    __when_tmp2 = invalid
    if tmp0_elvis_lhs = invalid then
        throw IllegalStateException_create_k_()
    else if true then
        __when_tmp2 = tmp0_elvis_lhs
    end if
    key = __when_tmp2

    m.__get_map().remove_AnyN_k_(key)
    m.__set_currentIndex(m.__get_currentIndex() - 1)
    m.__set_orderCount(m.__get_orderCount() - 1)
    m.__set_canRemove(false)
end sub

function LinkedHashMap_LinkedValueIterator___get_map_k_() as Object
    return m.map
end function

function LinkedHashMap_LinkedValueIterator___get_currentIndex_k_() as Integer
    return m.currentIndex
end function

sub LinkedHashMap_LinkedValueIterator___set_currentIndex_I_k_(value as Integer)
    m.currentIndex = value
end sub

function LinkedHashMap_LinkedValueIterator___get_orderCount_k_() as Integer
    return m.orderCount
end function

sub LinkedHashMap_LinkedValueIterator___set_orderCount_I_k_(value as Integer)
    m.orderCount = value
end sub

function LinkedHashMap_LinkedValueIterator___get_lastReturnedKey_k_() as Dynamic
    return m.lastReturnedKey
end function

sub LinkedHashMap_LinkedValueIterator___set_lastReturnedKey_AnyN_k_(value as Dynamic)
    m.lastReturnedKey = value
end sub

function LinkedHashMap_LinkedValueIterator___get_canRemove_k_() as Boolean
    return m.canRemove
end function

sub LinkedHashMap_LinkedValueIterator___set_canRemove_Z_k_(value as Boolean)
    m.canRemove = value
end sub

function LinkedHashMap_LinkedEntryIterator_create_LinkedHashMap_k_(map as Object) as Object
    this = {}
    this.__type = "LinkedHashMap_LinkedEntryIterator"
    this.__proto = ["LinkedHashMap_LinkedEntryIterator", "MutableIterator", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.hasNext_k_ = LinkedHashMap_LinkedEntryIterator_hasNext_k_
    this.next_k_ = LinkedHashMap_LinkedEntryIterator_next_k_
    this.remove_k_ = LinkedHashMap_LinkedEntryIterator_remove_k_
    this.__get_map = LinkedHashMap_LinkedEntryIterator___get_map_k_
    this.__get_currentIndex = LinkedHashMap_LinkedEntryIterator___get_currentIndex_k_
    this.__set_currentIndex = LinkedHashMap_LinkedEntryIterator___set_currentIndex_I_k_
    this.__get_orderCount = LinkedHashMap_LinkedEntryIterator___get_orderCount_k_
    this.__set_orderCount = LinkedHashMap_LinkedEntryIterator___set_orderCount_I_k_
    this.__get_lastReturnedKey = LinkedHashMap_LinkedEntryIterator___get_lastReturnedKey_k_
    this.__set_lastReturnedKey = LinkedHashMap_LinkedEntryIterator___set_lastReturnedKey_AnyN_k_
    this.__get_canRemove = LinkedHashMap_LinkedEntryIterator___get_canRemove_k_
    this.__set_canRemove = LinkedHashMap_LinkedEntryIterator___set_canRemove_Z_k_
    this.map = map
    this.currentIndex = 0
    this.orderCount = this.__get_map().__get_keyOrder().Count()
    this.lastReturnedKey = invalid
    this.canRemove = false
    return this
end function

function LinkedHashMap_LinkedEntryIterator_hasNext_k_() as Boolean
    return m.__get_currentIndex() < m.__get_orderCount()
end function

function LinkedHashMap_LinkedEntryIterator_next_k_() as Object
    if not m.hasNext_k_() then
        throw NoSuchElementException_create_k_()
    end if
    keyStr = m.__get_map().__get_keyOrder()[m.__get_currentIndex()]
    entry = m.__get_map().__get_map()[keyStr]
    key = entry.k
    value = entry.v
    m.__set_lastReturnedKey(key)
    m.__set_currentIndex(m.__get_currentIndex() + 1)
    m.__set_canRemove(true)
    return LinkedHashMap_SimpleEntry_create_AnyN_AnyN_k_(key, value)
end function

sub LinkedHashMap_LinkedEntryIterator_remove_k_()
    tmp0 = m.__get_canRemove()
    while true
        value = tmp0
        if not value then
            while true
                exit while
            end while
            throw IllegalStateException_create_StrN_k_(toString_AnyN_k_(invalid))
        end if
        exit while
    end while

    tmp0_elvis_lhs = m.__get_lastReturnedKey()
    __when_tmp3 = invalid
    if tmp0_elvis_lhs = invalid then
        throw IllegalStateException_create_k_()
    else if true then
        __when_tmp3 = tmp0_elvis_lhs
    end if
    key = __when_tmp3

    m.__get_map().remove_AnyN_k_(key)
    m.__set_currentIndex(m.__get_currentIndex() - 1)
    m.__set_orderCount(m.__get_orderCount() - 1)
    m.__set_canRemove(false)
end sub

function LinkedHashMap_LinkedEntryIterator___get_map_k_() as Object
    return m.map
end function

function LinkedHashMap_LinkedEntryIterator___get_currentIndex_k_() as Integer
    return m.currentIndex
end function

sub LinkedHashMap_LinkedEntryIterator___set_currentIndex_I_k_(value as Integer)
    m.currentIndex = value
end sub

function LinkedHashMap_LinkedEntryIterator___get_orderCount_k_() as Integer
    return m.orderCount
end function

sub LinkedHashMap_LinkedEntryIterator___set_orderCount_I_k_(value as Integer)
    m.orderCount = value
end sub

function LinkedHashMap_LinkedEntryIterator___get_lastReturnedKey_k_() as Dynamic
    return m.lastReturnedKey
end function

sub LinkedHashMap_LinkedEntryIterator___set_lastReturnedKey_AnyN_k_(value as Dynamic)
    m.lastReturnedKey = value
end sub

function LinkedHashMap_LinkedEntryIterator___get_canRemove_k_() as Boolean
    return m.canRemove
end function

sub LinkedHashMap_LinkedEntryIterator___set_canRemove_Z_k_(value as Boolean)
    m.canRemove = value
end sub

function LinkedHashMap_SimpleEntry_create_AnyN_AnyN_k_(key as Dynamic, value as Dynamic) as Object
    this = {}
    this.__type = "LinkedHashMap_SimpleEntry"
    this.__proto = ["LinkedHashMap_SimpleEntry", "MutableMap_MutableEntry", "Map_Entry"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.setValue_AnyN_k_ = LinkedHashMap_SimpleEntry_setValue_AnyN_k_
    this.equals_AnyN_k_ = LinkedHashMap_SimpleEntry_equals_AnyN_k_
    this.equals = LinkedHashMap_SimpleEntry_equals_AnyN_k_
    this.hashCode_k_ = LinkedHashMap_SimpleEntry_hashCode_k_
    this.hashCode = LinkedHashMap_SimpleEntry_hashCode_k_
    this.toString_k_ = LinkedHashMap_SimpleEntry_toString_k_
    this.toString = LinkedHashMap_SimpleEntry_toString_k_
    this.__get_key = LinkedHashMap_SimpleEntry___get_key_k_
    this.__get_value = LinkedHashMap_SimpleEntry___get_value_k_
    this.__set_value = LinkedHashMap_SimpleEntry___set_value_AnyN_k_
    this.key = key
    this.value = value
    return this
end function

function LinkedHashMap_SimpleEntry_setValue_AnyN_k_(newValue as Dynamic) as Dynamic
    oldValue = m.__get_value()
    m.__set_value(newValue)
    return oldValue
end function

function LinkedHashMap_SimpleEntry_equals_AnyN_k_(other as Dynamic) as Boolean
    if not __kotlin_isInstanceOf(other, "Map_Entry") then
        return false
    end if
    return brsStructuralEquals_AnyN_AnyN_k_(m.__get_key(), other.__get_key()) and brsStructuralEquals_AnyN_AnyN_k_(m.__get_value(), other.__get_value())
end function

function LinkedHashMap_SimpleEntry_hashCode_k_() as Integer
    tmp0_safe_receiver = m.__get_key()
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
    tmp2_safe_receiver = m.__get_value()
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
    return (__when_tmp5 or __when_tmp7) and not (__when_tmp5 and __when_tmp7)

end function

function LinkedHashMap_SimpleEntry_toString_k_() as String
    return (toString_AnyN_k_(m.__get_key()) + "=") + toString_AnyN_k_(m.__get_value())
end function

function LinkedHashMap_SimpleEntry___get_key_k_() as Dynamic
    return m.key
end function

function LinkedHashMap_SimpleEntry___get_value_k_() as Dynamic
    return m.value
end function

sub LinkedHashMap_SimpleEntry___set_value_AnyN_k_(value as Dynamic)
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
