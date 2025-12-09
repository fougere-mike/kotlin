function UByteArray_create_ByteArray_UByteArray_k_(storage as Object) as Object
    this = {}
    this.__type = "UByteArray"
    this.__proto = ["UByteArray", "Collection", "Iterable"]
    this.__id = __kotlin_nextObjectId()
    this.get_I_UByte_k_ = UByteArray_get_I_UByte_k_
    this.set_I_UByte_k_ = UByteArray_set_I_UByte_k_
    this.iterator_IteratorUByte_k_ = UByteArray_iterator_IteratorUByte_k_
    this.contains_UByte_Z_k_ = UByteArray_contains_UByte_Z_k_
    this.containsAll_CollectionUByte_Z_k_ = UByteArray_containsAll_CollectionUByte_Z_k_
    this.isEmpty_Z_k_ = UByteArray_isEmpty_Z_k_
    this.equals_AnyN_Z_k_ = UByteArray_equals_AnyN_Z_k_
    this.equals = UByteArray_equals_AnyN_Z_k_
    this.hashCode_I_k_ = UByteArray_hashCode_I_k_
    this.hashCode = UByteArray_hashCode_I_k_
    this.toString_Str_k_ = UByteArray_toString_Str_k_
    this.toString = UByteArray_toString_Str_k_
    this.get_storage = UByteArray_get_storage_ByteArray_k_
    this.get_size = UByteArray_get_size_I_k_
    this.storage = storage
    return this
end function

function UByteArray_create_I_UByteArray_k_(size as Integer) as Object
    return UByteArray_create_ByteArray_UByteArray_k_(ByteArray_create_I_ByteArray_k_(size))
end function

function UByteArray_get_I_UByte_k_(index as Integer) as Object
    return toUByte_rB_UByte_k_(m.get_storage().get_I_B_k_(index))
end function

sub UByteArray_set_I_UByte_k_(index as Integer, value as Object)
    m.get_storage().set_I_B_k_(index, value)
end sub

function UByteArray_iterator_IteratorUByte_k_() as Object
    return UByteArray_Iterator_create_ByteArray_Iterator_k_(m.get_storage())
end function

function UByteArray_contains_UByte_Z_k_(element as Object) as Boolean
    target = element
    progression = until_rI_I_IntRange_k_(0, m.get_storage().get_size())
    inductionVariable = progression.get_first()
    last = progression.get_last()
    if inductionVariable <= last then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if m.get_storage().get_I_B_k_(i) = target then
            return true
        end if


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if m.get_storage().get_I_B_k_(i) = target then
                return true
            end if

        end while

    end if

    return false
end function

function UByteArray_containsAll_CollectionUByte_Z_k_(elements as Object) as Boolean
    for each element in elements
        if not m.contains_UByte_Z_k_(element) then
            return false
        end if
    end for
    return true
end function

function UByteArray_isEmpty_Z_k_() as Boolean
    return m.get_storage().get_size() = 0
end function

function UByteArray_equals_AnyN_Z_k_(other as Dynamic) as Boolean
    if not __kotlin_isInstanceOf(other, "UByteArray") then
        return false
    end if
    if m.get_storage().get_size() <> other.get_storage().get_size() then
        return false
    end if
    progression = until_rI_I_IntRange_k_(0, m.get_storage().get_size())
    inductionVariable = progression.get_first()
    last = progression.get_last()
    if inductionVariable <= last then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if m.get_storage().get_I_B_k_(i) <> other.get_storage().get_I_B_k_(i) then
            return false
        end if


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if m.get_storage().get_I_B_k_(i) <> other.get_storage().get_I_B_k_(i) then
                return false
            end if

        end while

    end if

    return true
end function

function UByteArray_hashCode_I_k_() as Integer
    result = 1
    progression = until_rI_I_IntRange_k_(0, m.get_storage().get_size())
    inductionVariable = progression.get_first()
    last = progression.get_last()
    if inductionVariable <= last then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        result = ((31 * result) + m.get_storage().get_I_B_k_(i))


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            result = ((31 * result) + m.get_storage().get_I_B_k_(i))

        end while

    end if

    return result
end function

function UByteArray_toString_Str_k_() as String
    sb = StringBuilder_create_StringBuilder_k_()
    sb.append_StrN_StringBuilder_k_("UByteArray([")
    progression = until_rI_I_IntRange_k_(0, m.get_storage().get_size())
    inductionVariable = progression.get_first()
    last = progression.get_last()
    if inductionVariable <= last then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if i > 0 then
            sb.append_StrN_StringBuilder_k_(", ")
        end if
        sb.append_StrN_StringBuilder_k_(toUByte_rB_UByte_k_(m.get_storage().get_I_B_k_(i)).toString())


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if i > 0 then
                sb.append_StrN_StringBuilder_k_(", ")
            end if
            sb.append_StrN_StringBuilder_k_(toUByte_rB_UByte_k_(m.get_storage().get_I_B_k_(i)).toString())

        end while

    end if

    sb.append_StrN_StringBuilder_k_("])")
    return sb.toString()
end function

function UByteArray_get_storage_ByteArray_k_() as Object
    return m.storage
end function

function UByteArray_get_size_I_k_() as Integer
    return m.get_storage().get_size()
end function

function UByteArray_Iterator_create_ByteArray_Iterator_k_(array as Object) as Object
    this = {}
    this.__type = "UByteArray_Iterator"
    this.__proto = ["UByteArray_Iterator", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.hasNext_Z_k_ = UByteArray_Iterator_hasNext_Z_k_
    this.next_UByte_k_ = UByteArray_Iterator_next_UByte_k_
    this.get_array = UByteArray_Iterator_get_array_ByteArray_k_
    this.get_index = UByteArray_Iterator_get_index_I_k_
    this.set_index = UByteArray_Iterator_set_index_I_k_
    this.array = array
    this.index = 0
    return this
end function

function UByteArray_Iterator_hasNext_Z_k_() as Boolean
    return m.get_index() < m.get_array().get_size()
end function

function UByteArray_Iterator_next_UByte_k_() as Object
    __when_tmp0 = invalid
    if m.get_index() < m.get_array().get_size() then
        __incr_tmp_13 = m.get_index()
        m.set_index(__incr_tmp_13 + 1)
        __when_tmp0 = toUByte_rB_UByte_k_(m.get_array().get_I_B_k_(__incr_tmp_13))
    else if true then
        throw NoSuchElementException_create_StrN_NoSuchElementException_k_(__kotlin_numToStr_I_Str_k_(m.get_index()))
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

function UByteArray_I_Function1IUByte_UByteArray_k_(size as Integer, init as Object) as Object
    return UByteArray_create_ByteArray_UByteArray_k_(ByteArray_create_I_Function1IB_ByteArray_k_(size, {init: init, invoke: function(index as Integer) as Integer
        return m.init.invoke(index)
    end function}))
end function

function ubyteArrayOf_UByteArray_UByteArray_k_(elements as Object) as Object
    return UByteArray_create_ByteArray_UByteArray_k_(ByteArray_create_I_Function1IB_ByteArray_k_(elements.get_size(), {elements: elements, invoke: function(it as Integer) as Integer
        return m.elements.get_I_UByte_k_(it)
    end function}))
end function
