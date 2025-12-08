function IndexedValue_create(index as Integer, value as Dynamic) as Object
    this = {}
    this.__type = "IndexedValue"
    this.__proto = ["IndexedValue"]
    this.index = index
    this.value = value
    this.component1_I_k_ = IndexedValue_component1_I_k_
    this.component2_AnyN_k_ = IndexedValue_component2_AnyN_k_
    this.copy_I_AnyN_IndexedValueAnyN_k_ = IndexedValue_copy_I_AnyN_IndexedValueAnyN_k_
    this.toString_Str_k_ = IndexedValue_toString_Str_k_
    this.hashCode_I_k_ = IndexedValue_hashCode_I_k_
    this.equals_AnyN_Z_k_ = IndexedValue_equals_AnyN_Z_k_
    this.get_index = IndexedValue_get_index_I_k_
    this.get_value = IndexedValue_get_value_AnyN_k_
    this.equals = IndexedValue_equals
    this.hashCode = IndexedValue_hashCode
    this.toString = IndexedValue_toString
    this.copy = IndexedValue_copy
    this.component1 = IndexedValue_component1
    this.component2 = IndexedValue_component2
    return this
end function

function IndexedValue_equals(other as Object) as Boolean
    if Type(other) <> "roAssociativeArray" then
        return false
    end if
    if other.__type <> "IndexedValue" then
        return false
    end if
    if m.index <> other.index then
        return false
    end if
    if m.value <> other.value then
        return false
    end if
    return true
end function

function IndexedValue_hashCode() as Integer
    result = 1
    result = ((result * 31) + m.index)
    result = ((result * 31) + m.value)
    return result
end function

function IndexedValue_toString() as String
    return ((("IndexedValue(index=" + m.index) + ", value=") + m.value) + ")"
end function

function IndexedValue_copy(index = invalid, value = invalid) as Object
    if index = invalid then
        index = m.index
    end if
    if value = invalid then
        value = m.value
    end if
    return IndexedValue_create(index, value)
end function

function IndexedValue_component1() as Integer
    return m.index
end function

function IndexedValue_component2() as Dynamic
    return m.value
end function
