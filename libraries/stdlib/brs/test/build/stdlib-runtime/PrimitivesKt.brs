function __get_code_rC_k_(m as Object) as Integer
    return Asc(m)
end function

function toChar_rI_k_(m as Integer) as Object
    return Chr(m)
end function

function until_rI_I_k_(m as Integer, to_ as Integer) as Object
    if to_ <= -2147483648 then
        return IntRange_Companion_getInstance().__get_EMPTY()
    end if
    return rangeTo_rI_I_k_(m, to_ - 1)
end function

function until_rJ_J_k_(m as LongInteger, to_ as LongInteger) as Object
    if to_ <= -9223372036854775808& then
        return LongRange_Companion_getInstance().__get_EMPTY()
    end if
    return rangeTo_rJ_J_k_(m, to_ - 1)
end function

function until_rC_C_k_(m as Object, to_ as Object) as Object
    if __kotlin_stringCompare(to_, "") <= 0 then
        return CharRange_Companion_getInstance().__get_EMPTY()
    end if
    return rangeTo_rC_C_k_(m, to_ - 1)
end function

function __get_indices_rCharSequence_k_(m as Object) as Object
    return rangeTo_rI_I_k_(0, __kotlin_charSequenceLength_CharSequenceN_k_(m) - 1)
end function

function __get_indices_rArr_k_(m as Object) as Object
    return rangeTo_rI_I_k_(0, m.count() - 1)
end function

function __get_indices_rIntArray_k_(m as Object) as Object
    return rangeTo_rI_I_k_(0, m.__get_size() - 1)
end function

function __get_indices_rCharArray_k_(m as Object) as Object
    return rangeTo_rI_I_k_(0, m.__get_size() - 1)
end function

function __get_indices_rByteArray_k_(m as Object) as Object
    return rangeTo_rI_I_k_(0, m.__get_size() - 1)
end function

function coerceAtLeast_rI_I_k_(m as Integer, minimumValue as Integer) as Integer
    __when_tmp0 = invalid
    if m < minimumValue then
        __when_tmp0 = minimumValue
    else if true then
        __when_tmp0 = m
    end if
    return __when_tmp0

end function

function coerceAtMost_rI_I_k_(m as Integer, maximumValue as Integer) as Integer
    __when_tmp1 = invalid
    if m > maximumValue then
        __when_tmp1 = maximumValue
    else if true then
        __when_tmp1 = m
    end if
    return __when_tmp1

end function

function coerceIn_rI_I_I_k_(m as Integer, minimumValue as Integer, maximumValue as Integer) as Integer
    if minimumValue > maximumValue then
        throw IllegalArgumentException_create_StrN_k_(((("Cannot coerce value to an empty range: maximum " + __kotlin_numToStr_I_k_(maximumValue)) + " is less than minimum ") + __kotlin_numToStr_I_k_(minimumValue)) + ".")
    end if
    __when_tmp2 = invalid
    if m < minimumValue then
        __when_tmp2 = minimumValue
    else if m > maximumValue then
        __when_tmp2 = maximumValue
    else if true then
        __when_tmp2 = m
    end if
    return __when_tmp2

end function

sub require_Z_k_(value as Boolean)
    if not value then
        throw IllegalArgumentException_create_StrN_k_("Failed requirement.")
    end if
end sub

sub require_Z_Function0Any_k_(value as Boolean, lazyMessage as Object)
    if not value then
        throw IllegalArgumentException_create_StrN_k_(toString_AnyN_k_(lazyMessage.invoke_k_()))
    end if
end sub

sub check_Z_k_(value as Boolean)
    if not value then
        throw IllegalStateException_create_StrN_k_("Check failed.")
    end if
end sub

sub check_Z_Function0Any_k_(value as Boolean, lazyMessage as Object)
    if not value then
        throw IllegalStateException_create_StrN_k_(toString_AnyN_k_(lazyMessage.invoke_k_()))
    end if
end sub

function error_Any_k_(message as Object) as Dynamic
    throw IllegalStateException_create_StrN_k_(toString_AnyN_k_(message))
end function

function copyOf_rByteArray_I_k_(m as Object, newSize as Integer) as Object
    result = ByteArray_create_I_k_(newSize)
    __when_tmp3 = invalid
    if newSize < m.__get_size() then
        __when_tmp3 = newSize
    else if true then
        __when_tmp3 = m.__get_size()
    end if
    copySize = __when_tmp3

    i = 0
    while i < copySize
        result.set_I_B_k_(i, m.get_I_k_(i))
        i = (i + 1)
    end while
    return result
end function

function copyOf_rIntArray_I_k_(m as Object, newSize as Integer) as Object
    result = IntArray_create_I_k_(newSize)
    __when_tmp4 = invalid
    if newSize < m.__get_size() then
        __when_tmp4 = newSize
    else if true then
        __when_tmp4 = m.__get_size()
    end if
    copySize = __when_tmp4

    i = 0
    while i < copySize
        result.set_I_I_k_(i, m.get_I_k_(i))
        i = (i + 1)
    end while
    return result
end function

function copyOf_rCharArray_I_k_(m as Object, newSize as Integer) as Object
    result = CharArray_create_I_k_(newSize)
    __when_tmp5 = invalid
    if newSize < m.__get_size() then
        __when_tmp5 = newSize
    else if true then
        __when_tmp5 = m.__get_size()
    end if
    copySize = __when_tmp5

    i = 0
    while i < copySize
        result.set_I_C_k_(i, m.get_I_k_(i))
        i = (i + 1)
    end while
    return result
end function

function iterator_rStr_k_(m as String) as Object
    return Anon_51cb73f5_create_Str_k_(m)
end function

function iterator_rCharArray_k_(m as Object) as Object
    return Anon_2bfc2d54_create_CharArray_k_(m)
end function

function Anon_51cb73f5_create_Str_k_(_this_iterator as String) as Object
    this = CharIterator_create_k_()
    this._super = {}
    this._super.hasNext_k_ = this.hasNext_k_
    this._super.nextChar_k_ = this.nextChar_k_
    this.__proto = ["Anon_51cb73f5", this.__proto]
    this.__type = "Anon_51cb73f5"
    this.hasNext_k_ = Anon_51cb73f5_hasNext_k_
    this.nextChar_k_ = Anon_51cb73f5_nextChar_k_
    this.__get_index = Anon_51cb73f5___get_index_k_
    this.__set_index = Anon_51cb73f5___set_index_I_k_
    this.index = 0
    this._this_iterator = _this_iterator
    return this
end function

function Anon_51cb73f5_hasNext_k_() as Boolean
    return m.__get_index() < Len(m._this_iterator)
end function

function Anon_51cb73f5_nextChar_k_() as Object
    __incr_tmp_199 = m.__get_index()
    m.__set_index(__incr_tmp_199 + 1)
    return Mid(m._this_iterator, __incr_tmp_199 + 1, 1)

end function

function Anon_51cb73f5___get_index_k_() as Integer
    return m.index
end function

sub Anon_51cb73f5___set_index_I_k_(value as Integer)
    m.index = value
end sub

function Anon_2bfc2d54_create_CharArray_k_(_this_iterator as Object) as Object
    this = CharIterator_create_k_()
    this._super = {}
    this._super.hasNext_k_ = this.hasNext_k_
    this._super.nextChar_k_ = this.nextChar_k_
    this.__proto = ["Anon_2bfc2d54", this.__proto]
    this.__type = "Anon_2bfc2d54"
    this.hasNext_k_ = Anon_2bfc2d54_hasNext_k_
    this.nextChar_k_ = Anon_2bfc2d54_nextChar_k_
    this.__get_index = Anon_2bfc2d54___get_index_k_
    this.__set_index = Anon_2bfc2d54___set_index_I_k_
    this.index = 0
    this._this_iterator = _this_iterator
    return this
end function

function Anon_2bfc2d54_hasNext_k_() as Boolean
    return m.__get_index() < m._this_iterator.__get_size()
end function

function Anon_2bfc2d54_nextChar_k_() as Object
    __incr_tmp_200 = m.__get_index()
    m.__set_index(__incr_tmp_200 + 1)
    return m._this_iterator.get_I_k_(__incr_tmp_200)

end function

function Anon_2bfc2d54___get_index_k_() as Integer
    return m.index
end function

sub Anon_2bfc2d54___set_index_I_k_(value as Integer)
    m.index = value
end sub
