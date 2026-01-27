function UByteArray_create_ByteArray_k_(storage as Object) as Object
    this = {}
    this.__type = "UByteArray"
    this.__proto = ["UByteArray", "Collection", "Iterable"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.get_I_k_ = UByteArray_get_I_k_
    this.set_I_UByte_k_ = UByteArray_set_I_UByte_k_
    this.iterator_k_ = UByteArray_iterator_k_
    this.contains_AnyN_k_ = UByteArray_contains_AnyN_k_
    this.containsAll_Collection_k_ = UByteArray_containsAll_Collection_k_
    this.isEmpty_k_ = UByteArray_isEmpty_k_
    this.equals_AnyN_k_ = UByteArray_equals_AnyN_k_
    this.equals = UByteArray_equals_AnyN_k_
    this.hashCode_k_ = UByteArray_hashCode_k_
    this.hashCode = UByteArray_hashCode_k_
    this.toString_k_ = UByteArray_toString_k_
    this.toString = UByteArray_toString_k_
    this.__get_storage = UByteArray___get_storage_k_
    this.__get_size = UByteArray___get_size_k_
    this.storage = storage
    return this
end function

function UByteArray_create_I_k_(size as Integer) as Object
    return UByteArray_create_ByteArray_k_(ByteArray_create_I_k_(size))
end function

function UByteArray_get_I_k_(index as Integer) as Object
    return toUByte_rB_k_(m.__get_storage().get_I_k_(index))
end function

sub UByteArray_set_I_UByte_k_(index as Integer, value as Object)
    tmp0 = value
    tmp_ret_0 = invalid

    while true
        this = tmp0
        tmp_ret_0 = this.__get_data()
        exit while
    end while
    m.__get_storage().set_I_B_k_(index, tmp_ret_0)

end sub

function UByteArray_iterator_k_() as Object
    return UByteArray_Iterator_create_ByteArray_k_(m.__get_storage())
end function

function UByteArray_contains_AnyN_k_(element as Object) as Boolean
    tmp0 = element
    target = tmp_ret_0

    tmp_ret_0 = invalid
    while true
        this = tmp0
        tmp_ret_0 = this.__get_data()
        exit while
    end while
    progression = until_rI_I_k_(0, m.__get_storage().__get_size())

    inductionVariable = progression.__get_first()
    last = progression.__get_last()
    if inductionVariable <= last then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if m.__get_storage().get_I_k_(i) = target then
            return true
        end if


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if m.__get_storage().get_I_k_(i) = target then
                return true
            end if

        end while

    end if

    return false
end function

function UByteArray_containsAll_Collection_k_(elements as Object) as Boolean
    __iter_98 = elements.iterator_k_()
    while __iter_98.hasNext_k_()
        element = __iter_98.next_k_()
        if not m.contains_AnyN_k_(element) then
            return false
        end if
    end while

    return true
end function

function UByteArray_isEmpty_k_() as Boolean
    return m.__get_storage().__get_size() = 0
end function

function UByteArray_equals_AnyN_k_(other as Dynamic) as Boolean
    if not __kotlin_isInstanceOf(other, "UByteArray") then
        return false
    end if
    if m.__get_storage().__get_size() <> other.__get_storage().__get_size() then
        return false
    end if
    progression = until_rI_I_k_(0, m.__get_storage().__get_size())
    inductionVariable = progression.__get_first()
    last = progression.__get_last()
    if inductionVariable <= last then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if m.__get_storage().get_I_k_(i) <> other.__get_storage().get_I_k_(i) then
            return false
        end if


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if m.__get_storage().get_I_k_(i) <> other.__get_storage().get_I_k_(i) then
                return false
            end if

        end while

    end if

    return true
end function

function UByteArray_hashCode_k_() as Integer
    result = 1
    progression = until_rI_I_k_(0, m.__get_storage().__get_size())
    inductionVariable = progression.__get_first()
    last = progression.__get_last()
    if inductionVariable <= last then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        result = ((31 * result) + m.__get_storage().get_I_k_(i))


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            result = ((31 * result) + m.__get_storage().get_I_k_(i))

        end while

    end if

    return result
end function

function UByteArray_toString_k_() as String
    sb = StringBuilder_create_k_()
    sb.append_StrN_k_("UByteArray([")
    progression = until_rI_I_k_(0, m.__get_storage().__get_size())
    inductionVariable = progression.__get_first()
    last = progression.__get_last()
    if inductionVariable <= last then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if i > 0 then
            sb.append_StrN_k_(", ")
        end if
        sb.append_StrN_k_(toUByte_rB_k_(m.__get_storage().get_I_k_(i)).toString())


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if i > 0 then
                sb.append_StrN_k_(", ")
            end if
            sb.append_StrN_k_(toUByte_rB_k_(m.__get_storage().get_I_k_(i)).toString())

        end while

    end if

    sb.append_StrN_k_("])")
    return sb.toString()
end function

function UByteArray___get_storage_k_() as Object
    return m.storage
end function

function UByteArray___get_size_k_() as Integer
    return m.__get_storage().__get_size()
end function

function UByteArray_Iterator_create_ByteArray_k_(array as Object) as Object
    this = {}
    this.__type = "UByteArray_Iterator"
    this.__proto = ["UByteArray_Iterator", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.hasNext_k_ = UByteArray_Iterator_hasNext_k_
    this.next_k_ = UByteArray_Iterator_next_k_
    this.__get_array = UByteArray_Iterator___get_array_k_
    this.__get_index = UByteArray_Iterator___get_index_k_
    this.__set_index = UByteArray_Iterator___set_index_I_k_
    this.array = array
    this.index = 0
    return this
end function

function UByteArray_Iterator_hasNext_k_() as Boolean
    return m.__get_index() < m.__get_array().__get_size()
end function

function UByteArray_Iterator_next_k_() as Object
    __when_tmp0 = invalid
    if m.__get_index() < m.__get_array().__get_size() then
        __incr_tmp_99 = m.__get_index()
        m.__set_index(__incr_tmp_99 + 1)
        __when_tmp0 = toUByte_rB_k_(m.__get_array().get_I_k_(__incr_tmp_99))
    else if true then
        throw NoSuchElementException_create_StrN_k_(__kotlin_numToStr_I_k_(m.__get_index()))
    end if
    return __when_tmp0

end function

function UByteArray_Iterator___get_array_k_() as Object
    return m.array
end function

function UByteArray_Iterator___get_index_k_() as Integer
    return m.index
end function

sub UByteArray_Iterator___set_index_I_k_(value as Integer)
    m.index = value
end sub

function UByteArray_I_Function1IUByte_k_(size as Integer, init as Object) as Object
    return UByteArray_create_ByteArray_k_(ByteArray_create_I_Function1IB_k_(size, UByteArray_lambda_create_Function1IUByte_k_(init)))
end function

function ubyteArrayOf_UByteArray_k_(elements as Object) as Object
    return UByteArray_create_ByteArray_k_(ByteArray_create_I_Function1IB_k_(elements.__get_size(), ubyteArrayOf_lambda_create_UByteArray_k_(elements)))
end function

function UByteArray_lambda_create_Function1IUByte_k_(_init as Object) as Object
    this = {}
    this.__type = "UByteArray_lambda"
    this.__proto = ["UByteArray_lambda", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = UByteArray_lambda_invoke_AnyN_k_
    this._init = _init
    return this
end function

function UByteArray_lambda_invoke_AnyN_k_(index as Integer) as Integer
    tmp0 = m._init.invoke_AnyN_k_(index)
    tmp_ret_0 = invalid

    while true
        this = tmp0
        tmp_ret_0 = this.__get_data()
        exit while
    end while
    return tmp_ret_0

end function

function ubyteArrayOf_lambda_create_UByteArray_k_(_elements as Object) as Object
    this = {}
    this.__type = "ubyteArrayOf_lambda"
    this.__proto = ["ubyteArrayOf_lambda", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = ubyteArrayOf_lambda_invoke_AnyN_k_
    this._elements = _elements
    return this
end function

function ubyteArrayOf_lambda_invoke_AnyN_k_(it as Integer) as Integer
    tmp0 = m._elements.get_I_k_(it)
    tmp_ret_0 = invalid

    while true
        this = tmp0
        tmp_ret_0 = this.__get_data()
        exit while
    end while
    return tmp_ret_0

end function
