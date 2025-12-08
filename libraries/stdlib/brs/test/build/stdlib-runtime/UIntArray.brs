function UIntArray_create_IntArray_UIntArray_k_(storage as Object) as Object
    this = {}
    this.__type = "UIntArray"
    this.__proto = ["UIntArray"]
    this.storage = storage
    this.get_I_UInt_k_ = UIntArray_get_I_UInt_k_
    this.set_I_UInt_k_ = UIntArray_set_I_UInt_k_
    this.iterator_IteratorUInt_k_ = UIntArray_iterator_IteratorUInt_k_
    this.contains_UInt_Z_k_ = UIntArray_contains_UInt_Z_k_
    this.containsAll_CollectionUInt_Z_k_ = UIntArray_containsAll_CollectionUInt_Z_k_
    this.isEmpty_Z_k_ = UIntArray_isEmpty_Z_k_
    this.equals_AnyN_Z_k_ = UIntArray_equals_AnyN_Z_k_
    this.hashCode_I_k_ = UIntArray_hashCode_I_k_
    this.toString_Str_k_ = UIntArray_toString_Str_k_
    this.get_storage = UIntArray_get_storage_IntArray_k_
    this.get_size = UIntArray_get_size_I_k_
    return this
end function

function UIntArray_create_I_UIntArray_k_(size as Integer) as Object
    this = {}
    this.__type = "UIntArray"
    this.__proto = ["UIntArray"]
    this.storage = storage
    this.get_I_UInt_k_ = UIntArray_get_I_UInt_k_
    this.set_I_UInt_k_ = UIntArray_set_I_UInt_k_
    this.iterator_IteratorUInt_k_ = UIntArray_iterator_IteratorUInt_k_
    this.contains_UInt_Z_k_ = UIntArray_contains_UInt_Z_k_
    this.containsAll_CollectionUInt_Z_k_ = UIntArray_containsAll_CollectionUInt_Z_k_
    this.isEmpty_Z_k_ = UIntArray_isEmpty_Z_k_
    this.equals_AnyN_Z_k_ = UIntArray_equals_AnyN_Z_k_
    this.hashCode_I_k_ = UIntArray_hashCode_I_k_
    this.toString_Str_k_ = UIntArray_toString_Str_k_
    this.get_storage = UIntArray_get_storage_IntArray_k_
    this.get_size = UIntArray_get_size_I_k_
    return this
end function

function UIntArray_get_I_UInt_k_(index as Integer) as Object
    return toUInt_rI_UInt_k_(m.storage[index])
end function

sub UIntArray_set_I_UInt_k_(index as Integer, value as Object)
    m.storage.set(index, value)
end sub

function UIntArray_iterator_IteratorUInt_k_() as Object
    return UIntArray_Iterator_create_IntArray_Iterator_k_(m.storage)
end function

function UIntArray_contains_UInt_Z_k_(element as Object) as Boolean
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

function UIntArray_containsAll_CollectionUInt_Z_k_(elements as Object) as Boolean
    for each element in elements
        if m.contains(element).not() then
            return false
        end if
    end for
    return true
end function

function UIntArray_isEmpty_Z_k_() as Boolean
    return m.storage.size = 0
end function

function UIntArray_equals_AnyN_Z_k_(other as Dynamic) as Boolean
    if not __kotlin_isInstanceOf(other, "UIntArray") then
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

function UIntArray_hashCode_I_k_() as Integer
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

function UIntArray_toString_Str_k_() as String
    sb = StringBuilder_create_StringBuilder_k_()
    sb.append("UIntArray([")
    progression = until_rI_I_IntRange_k_(0, m.storage.size)
    inductionVariable = progression.first
    last = progression.last
    if lessOrEqual_I_I_Z_k_(inductionVariable, last) then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if i > 0 then
            sb.append(", ")
        end if
        sb.append(toUInt_rI_UInt_k_(m.storage[i]).toString())


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if i > 0 then
                sb.append(", ")
            end if
            sb.append(toUInt_rI_UInt_k_(m.storage[i]).toString())

        end while

    end if

    sb.append("])")
    return sb.toString()
end function

function UIntArray_get_storage_IntArray_k_() as Object
    return m.storage
end function

function UIntArray_get_size_I_k_() as Integer
    return m.storage.size
end function

function UIntArray_Iterator_create_IntArray_Iterator_k_(array as Object) as Object
    this = {}
    this.__type = "UIntArray_Iterator"
    this.__proto = ["UIntArray_Iterator"]
    this.array = array
    this.index = 0
    this.hasNext_Z_k_ = UIntArray_Iterator_hasNext_Z_k_
    this.next_UInt_k_ = UIntArray_Iterator_next_UInt_k_
    this.get_array = UIntArray_Iterator_get_array_IntArray_k_
    this.get_index = UIntArray_Iterator_get_index_I_k_
    this.set_index = UIntArray_Iterator_set_index_I_k_
    return this
end function

function UIntArray_Iterator_hasNext_Z_k_() as Boolean
    return m.index < m.array.size
end function

function UIntArray_Iterator_next_UInt_k_() as Object
    __when_tmp0 = invalid
    if m.index < m.array.size then
        __when_tmp0 = toUInt_rI_UInt_k_(m.array[m.index = (m.index + 1)])
    else if true then
        throw NoSuchElementException_create_StrN_NoSuchElementException_k_(Str(m.index))
    end if
    return __when_tmp0

end function

function UIntArray_Iterator_get_array_IntArray_k_() as Object
    return m.array
end function

function UIntArray_Iterator_get_index_I_k_() as Integer
    return m.index
end function

sub UIntArray_Iterator_set_index_I_k_(value as Integer)
    m.index = value
end sub

function UIntArray_I_Function1IUInt_UIntArray_k_(size as Integer, init as Function) as Object
    return UIntArray_create_IntArray_UIntArray_k_(IntArray_create_I_Function1II_IntArray_k_(size, {init: init, invoke: function(index as Integer) as Integer
        return m.init.invoke(index)
    end function}))
end function

function uintArrayOf_UIntArray_UIntArray_k_(elements as Object) as Object
    return UIntArray_create_IntArray_UIntArray_k_(IntArray_create_I_Function1II_IntArray_k_(elements.size, {elements: elements, invoke: function(it as Integer) as Integer
        return m.elements[it]
    end function}))
end function
