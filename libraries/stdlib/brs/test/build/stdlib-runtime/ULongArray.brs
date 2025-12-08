function ULongArray_create_LongArray_ULongArray_k_(storage as Object) as Object
    this = {}
    this.__type = "ULongArray"
    this.__proto = ["ULongArray"]
    this.storage = storage
    this.get_I_ULong_k_ = ULongArray_get_I_ULong_k_
    this.set_I_ULong_k_ = ULongArray_set_I_ULong_k_
    this.iterator_IteratorULong_k_ = ULongArray_iterator_IteratorULong_k_
    this.contains_ULong_Z_k_ = ULongArray_contains_ULong_Z_k_
    this.containsAll_CollectionULong_Z_k_ = ULongArray_containsAll_CollectionULong_Z_k_
    this.isEmpty_Z_k_ = ULongArray_isEmpty_Z_k_
    this.equals_AnyN_Z_k_ = ULongArray_equals_AnyN_Z_k_
    this.hashCode_I_k_ = ULongArray_hashCode_I_k_
    this.toString_Str_k_ = ULongArray_toString_Str_k_
    this.get_storage = ULongArray_get_storage_LongArray_k_
    this.get_size = ULongArray_get_size_I_k_
    return this
end function

function ULongArray_create_I_ULongArray_k_(size as Integer) as Object
    this = {}
    this.__type = "ULongArray"
    this.__proto = ["ULongArray"]
    this.storage = storage
    this.get_I_ULong_k_ = ULongArray_get_I_ULong_k_
    this.set_I_ULong_k_ = ULongArray_set_I_ULong_k_
    this.iterator_IteratorULong_k_ = ULongArray_iterator_IteratorULong_k_
    this.contains_ULong_Z_k_ = ULongArray_contains_ULong_Z_k_
    this.containsAll_CollectionULong_Z_k_ = ULongArray_containsAll_CollectionULong_Z_k_
    this.isEmpty_Z_k_ = ULongArray_isEmpty_Z_k_
    this.equals_AnyN_Z_k_ = ULongArray_equals_AnyN_Z_k_
    this.hashCode_I_k_ = ULongArray_hashCode_I_k_
    this.toString_Str_k_ = ULongArray_toString_Str_k_
    this.get_storage = ULongArray_get_storage_LongArray_k_
    this.get_size = ULongArray_get_size_I_k_
    return this
end function

function ULongArray_get_I_ULong_k_(index as Integer) as Object
    return toULong_rJ_ULong_k_(m.storage[index])
end function

sub ULongArray_set_I_ULong_k_(index as Integer, value as Object)
    m.storage.set(index, value)
end sub

function ULongArray_iterator_IteratorULong_k_() as Object
    return ULongArray_Iterator_create_LongArray_Iterator_k_(m.storage)
end function

function ULongArray_contains_ULong_Z_k_(element as Object) as Boolean
    target = element
    progression = until_rI_I_IntRange_k_(0, m.storage.size)
    inductionVariable = progression.first
    last = progression.last
    if lessOrEqual_I_I_Z_k_(inductionVariable, last) then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if m.storage[i] = target then
            return true
        end if


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if m.storage[i] = target then
                return true
            end if

        end while

    end if

    return false
end function

function ULongArray_containsAll_CollectionULong_Z_k_(elements as Object) as Boolean
    for each element in elements
        if m.contains(element).not() then
            return false
        end if
    end for
    return true
end function

function ULongArray_isEmpty_Z_k_() as Boolean
    return m.storage.size = 0
end function

function ULongArray_equals_AnyN_Z_k_(other as Dynamic) as Boolean
    if not __kotlin_isInstanceOf(other, "ULongArray") then
        return false
    end if
    if m.storage.size <> other.storage.size then
        return false
    end if
    progression = until_rI_I_IntRange_k_(0, m.storage.size)
    inductionVariable = progression.first
    last = progression.last
    if lessOrEqual_I_I_Z_k_(inductionVariable, last) then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if m.storage[i] <> other.storage[i] then
            return false
        end if


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if m.storage[i] <> other.storage[i] then
                return false
            end if

        end while

    end if

    return true
end function

function ULongArray_hashCode_I_k_() as Integer
    result = 1
    progression = until_rI_I_IntRange_k_(0, m.storage.size)
    inductionVariable = progression.first
    last = progression.last
    if lessOrEqual_I_I_Z_k_(inductionVariable, last) then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        element = m.storage[i]
        result = ((31 * result) + element.xor(element.ushr(32)))


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            element = m.storage[i]
            result = ((31 * result) + element.xor(element.ushr(32)))

        end while

    end if

    return result
end function

function ULongArray_toString_Str_k_() as String
    sb = StringBuilder_create_StringBuilder_k_()
    sb.append("ULongArray([")
    progression = until_rI_I_IntRange_k_(0, m.storage.size)
    inductionVariable = progression.first
    last = progression.last
    if lessOrEqual_I_I_Z_k_(inductionVariable, last) then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if i > 0 then
            sb.append(", ")
        end if
        sb.append(toULong_rJ_ULong_k_(m.storage[i]).toString())


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if i > 0 then
                sb.append(", ")
            end if
            sb.append(toULong_rJ_ULong_k_(m.storage[i]).toString())

        end while

    end if

    sb.append("])")
    return sb.toString()
end function

function ULongArray_get_storage_LongArray_k_() as Object
    return m.storage
end function

function ULongArray_get_size_I_k_() as Integer
    return m.storage.size
end function

function ULongArray_Iterator_create_LongArray_Iterator_k_(array as Object) as Object
    this = {}
    this.__type = "ULongArray_Iterator"
    this.__proto = ["ULongArray_Iterator"]
    this.array = array
    this.index = 0
    this.hasNext_Z_k_ = ULongArray_Iterator_hasNext_Z_k_
    this.next_ULong_k_ = ULongArray_Iterator_next_ULong_k_
    this.get_array = ULongArray_Iterator_get_array_LongArray_k_
    this.get_index = ULongArray_Iterator_get_index_I_k_
    this.set_index = ULongArray_Iterator_set_index_I_k_
    return this
end function

function ULongArray_Iterator_hasNext_Z_k_() as Boolean
    return m.index < m.array.size
end function

function ULongArray_Iterator_next_ULong_k_() as Object
    __when_tmp0 = invalid
    if m.index < m.array.size then
        __when_tmp0 = toULong_rJ_ULong_k_(m.array[m.index = (m.index + 1)])
    else if true then
        throw NoSuchElementException_create_StrN_NoSuchElementException_k_(Str(m.index))
    end if
    return __when_tmp0

end function

function ULongArray_Iterator_get_array_LongArray_k_() as Object
    return m.array
end function

function ULongArray_Iterator_get_index_I_k_() as Integer
    return m.index
end function

sub ULongArray_Iterator_set_index_I_k_(value as Integer)
    m.index = value
end sub

function ULongArray_I_Function1IULong_ULongArray_k_(size as Integer, init as Function) as Object
    return ULongArray_create_LongArray_ULongArray_k_(LongArray_create_I_Function1IJ_LongArray_k_(size, {init: init, invoke: function(index as Integer) as LongInteger
        return m.init.invoke(index)
    end function}))
end function

function ulongArrayOf_ULongArray_ULongArray_k_(elements as Object) as Object
    return ULongArray_create_LongArray_ULongArray_k_(LongArray_create_I_Function1IJ_LongArray_k_(elements.size, {elements: elements, invoke: function(it as Integer) as LongInteger
        return m.elements[it]
    end function}))
end function
