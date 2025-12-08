function UByteArray_create_ByteArray_UByteArray_k_(storage as Object) as Object
    this = {}
    this.__type = "UByteArray"
    this.__proto = ["UByteArray"]
    this.storage = storage
    this.get_I_UByte_k_ = UByteArray_get_I_UByte_k_
    this.set_I_UByte_k_ = UByteArray_set_I_UByte_k_
    this.iterator_IteratorUByte_k_ = UByteArray_iterator_IteratorUByte_k_
    this.contains_UByte_Z_k_ = UByteArray_contains_UByte_Z_k_
    this.containsAll_CollectionUByte_Z_k_ = UByteArray_containsAll_CollectionUByte_Z_k_
    this.isEmpty_Z_k_ = UByteArray_isEmpty_Z_k_
    this.equals_AnyN_Z_k_ = UByteArray_equals_AnyN_Z_k_
    this.hashCode_I_k_ = UByteArray_hashCode_I_k_
    this.toString_Str_k_ = UByteArray_toString_Str_k_
    this.get_storage = UByteArray_get_storage_ByteArray_k_
    this.get_size = UByteArray_get_size_I_k_
    return this
end function

function UByteArray_create_I_UByteArray_k_(size as Integer) as Object
    this = {}
    this.__type = "UByteArray"
    this.__proto = ["UByteArray"]
    this.storage = storage
    this.get_I_UByte_k_ = UByteArray_get_I_UByte_k_
    this.set_I_UByte_k_ = UByteArray_set_I_UByte_k_
    this.iterator_IteratorUByte_k_ = UByteArray_iterator_IteratorUByte_k_
    this.contains_UByte_Z_k_ = UByteArray_contains_UByte_Z_k_
    this.containsAll_CollectionUByte_Z_k_ = UByteArray_containsAll_CollectionUByte_Z_k_
    this.isEmpty_Z_k_ = UByteArray_isEmpty_Z_k_
    this.equals_AnyN_Z_k_ = UByteArray_equals_AnyN_Z_k_
    this.hashCode_I_k_ = UByteArray_hashCode_I_k_
    this.toString_Str_k_ = UByteArray_toString_Str_k_
    this.get_storage = UByteArray_get_storage_ByteArray_k_
    this.get_size = UByteArray_get_size_I_k_
    return this
end function

function UByteArray_get_I_UByte_k_(index as Integer) as Object
    return toUByte_rB_UByte_k_(m.storage[index])
end function

sub UByteArray_set_I_UByte_k_(index as Integer, value as Object)
    m.storage.set(index, value)
end sub

function UByteArray_iterator_IteratorUByte_k_() as Object
    return UByteArray_Iterator_create_ByteArray_Iterator_k_(m.storage)
end function

function UByteArray_contains_UByte_Z_k_(element as Object) as Boolean
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

function UByteArray_containsAll_CollectionUByte_Z_k_(elements as Object) as Boolean
    for each element in elements
        if m.contains(element).not() then
            return false
        end if
    end for
    return true
end function

function UByteArray_isEmpty_Z_k_() as Boolean
    return m.storage.size = 0
end function

function UByteArray_equals_AnyN_Z_k_(other as Dynamic) as Boolean
    if not __kotlin_isInstanceOf(other, "UByteArray") then
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

function UByteArray_hashCode_I_k_() as Integer
    result = 1
    progression = until_rI_I_IntRange_k_(0, m.storage.size)
    inductionVariable = progression.first
    last = progression.last
    if lessOrEqual_I_I_Z_k_(inductionVariable, last) then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        result = ((31 * result) + m.storage[i])


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            result = ((31 * result) + m.storage[i])

        end while

    end if

    return result
end function

function UByteArray_toString_Str_k_() as String
    sb = StringBuilder_create_StringBuilder_k_()
    sb.append("UByteArray([")
    progression = until_rI_I_IntRange_k_(0, m.storage.size)
    inductionVariable = progression.first
    last = progression.last
    if lessOrEqual_I_I_Z_k_(inductionVariable, last) then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if i > 0 then
            sb.append(", ")
        end if
        sb.append(toUByte_rB_UByte_k_(m.storage[i]).toString())


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if i > 0 then
                sb.append(", ")
            end if
            sb.append(toUByte_rB_UByte_k_(m.storage[i]).toString())

        end while

    end if

    sb.append("])")
    return sb.toString()
end function

function UByteArray_get_storage_ByteArray_k_() as Object
    return m.storage
end function

function UByteArray_get_size_I_k_() as Integer
    return m.storage.size
end function

function UByteArray_Iterator_create_ByteArray_Iterator_k_(array as Object) as Object
    this = {}
    this.__type = "UByteArray_Iterator"
    this.__proto = ["UByteArray_Iterator"]
    this.array = array
    this.index = 0
    this.hasNext_Z_k_ = UByteArray_Iterator_hasNext_Z_k_
    this.next_UByte_k_ = UByteArray_Iterator_next_UByte_k_
    this.get_array = UByteArray_Iterator_get_array_ByteArray_k_
    this.get_index = UByteArray_Iterator_get_index_I_k_
    this.set_index = UByteArray_Iterator_set_index_I_k_
    return this
end function

function UByteArray_Iterator_hasNext_Z_k_() as Boolean
    return m.index < m.array.size
end function

function UByteArray_Iterator_next_UByte_k_() as Object
    __when_tmp0 = invalid
    if m.index < m.array.size then
        __when_tmp0 = toUByte_rB_UByte_k_(m.array[m.index = (m.index + 1)])
    else if true then
        throw NoSuchElementException_create_StrN_NoSuchElementException_k_(Str(m.index))
    end if
    return __when_tmp0

end function

function UByteArray_Iterator_get_array_ByteArray_k_() as Object
    return m.array
end function

function UByteArray_Iterator_get_index_I_k_() as Integer
    return m.index
end function

sub UByteArray_Iterator_set_index_I_k_(value as Integer)
    m.index = value
end sub

function UByteArray_I_Function1IUByte_UByteArray_k_(size as Integer, init as Function) as Object
    return UByteArray_create_ByteArray_UByteArray_k_(ByteArray_create_I_Function1IB_ByteArray_k_(size, {init: init, invoke: function(index as Integer) as Integer
        return m.init.invoke(index)
    end function}))
end function

function ubyteArrayOf_UByteArray_UByteArray_k_(elements as Object) as Object
    return UByteArray_create_ByteArray_UByteArray_k_(ByteArray_create_I_Function1IB_ByteArray_k_(elements.size, {elements: elements, invoke: function(it as Integer) as Integer
        return m.elements[it]
    end function}))
end function
