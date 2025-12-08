function get_code_rC_I_k_(m as Object) as Integer
    return Asc(m)
end function

function toChar_rI_C_k_(m as Integer) as Object
    return Chr(m)
end function

function until_rI_I_IntRange_k_(m as Integer, to_ as Integer) as Object
    if to_ <= -2147483648 then
        return IntRange_Companion_get_EMPTY_IntRange_k_()
    end if
    return m.rangeTo(to_ - 1)
end function

function until_rJ_J_LongRange_k_(m as LongInteger, to_ as LongInteger) as Object
    if to_ <= -9223372036854775808& then
        return LongRange_Companion_get_EMPTY_LongRange_k_()
    end if
    return m.rangeTo(to_ - 1)
end function

function until_rC_C_CharRange_k_(m as Object, to_ as Object) as Object
    if (to_ <= "") <= 0 then
        return CharRange_Companion_get_EMPTY_CharRange_k_()
    end if
    return m.rangeTo(to_ - 1)
end function

function get_indices_rCharSequence_IntRange_k_(m as Object) as Object
    return 0.rangeTo(m.length - 1)
end function

function get_indices_rArr_IntRange_k_(m as Object) as Object
    return 0.rangeTo(m.size - 1)
end function

function get_indices_rIntArray_IntRange_k_(m as Object) as Object
    return 0.rangeTo(m.size - 1)
end function

function get_indices_rCharArray_IntRange_k_(m as Object) as Object
    return 0.rangeTo(m.size - 1)
end function

function get_indices_rByteArray_IntRange_k_(m as Object) as Object
    return 0.rangeTo(m.size - 1)
end function

function coerceAtLeast_rI_I_I_k_(m as Integer, minimumValue as Integer) as Integer
    __when_tmp0 = invalid
    if m < minimumValue then
        __when_tmp0 = minimumValue
    else if true then
        __when_tmp0 = m
    end if
    return __when_tmp0

end function

function coerceAtMost_rI_I_I_k_(m as Integer, maximumValue as Integer) as Integer
    __when_tmp1 = invalid
    if m > maximumValue then
        __when_tmp1 = maximumValue
    else if true then
        __when_tmp1 = m
    end if
    return __when_tmp1

end function

function coerceIn_rI_I_I_I_k_(m as Integer, minimumValue as Integer, maximumValue as Integer) as Integer
    if minimumValue > maximumValue then
        throw IllegalArgumentException_create_StrN_IllegalArgumentException_k_(((("Cannot coerce value to an empty range: maximum " + maximumValue) + " is less than minimum ") + minimumValue) + ".")
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
    if value.not() then
        throw IllegalArgumentException_create_StrN_IllegalArgumentException_k_("Failed requirement.")
    end if
end sub

sub require_Z_Function0Any_k_(value as Boolean, lazyMessage as Function)
    if value.not() then
        throw IllegalArgumentException_create_StrN_IllegalArgumentException_k_(lazyMessage.invoke().toString())
    end if
end sub

sub check_Z_k_(value as Boolean)
    if value.not() then
        throw IllegalStateException_create_StrN_IllegalStateException_k_("Check failed.")
    end if
end sub

sub check_Z_Function0Any_k_(value as Boolean, lazyMessage as Function)
    if value.not() then
        throw IllegalStateException_create_StrN_IllegalStateException_k_(lazyMessage.invoke().toString())
    end if
end sub

sub error_Any_k_(message as Object)
    throw IllegalStateException_create_StrN_IllegalStateException_k_(message.toString())
end sub

function copyOf_rByteArray_I_ByteArray_k_(m as Object, newSize as Integer) as Object
    result = ByteArray_create_I_ByteArray_k_(newSize)
    __when_tmp3 = invalid
    if newSize < m.size then
        __when_tmp3 = newSize
    else if true then
        __when_tmp3 = m.size
    end if
    copySize = __when_tmp3

    i = 0
    while i < copySize
        result.set(i, m[i])
        i = (i + 1)
    end while
    return result
end function

function copyOf_rIntArray_I_IntArray_k_(m as Object, newSize as Integer) as Object
    result = IntArray_create_I_IntArray_k_(newSize)
    __when_tmp4 = invalid
    if newSize < m.size then
        __when_tmp4 = newSize
    else if true then
        __when_tmp4 = m.size
    end if
    copySize = __when_tmp4

    i = 0
    while i < copySize
        result.set(i, m[i])
        i = (i + 1)
    end while
    return result
end function

function copyOf_rCharArray_I_CharArray_k_(m as Object, newSize as Integer) as Object
    result = CharArray_create_I_CharArray_k_(newSize)
    __when_tmp5 = invalid
    if newSize < m.size then
        __when_tmp5 = newSize
    else if true then
        __when_tmp5 = m.size
    end if
    copySize = __when_tmp5

    i = 0
    while i < copySize
        result.set(i, m[i])
        i = (i + 1)
    end while
    return result
end function

function iterator_rStr_CharIterator_k_(m as String) as Object
    return Anon_700af8ff_create_Anon_k_()
end function

function iterator_rCharArray_CharIterator_k_(m as Object) as Object
    return Anon_50d6c88_create_Anon_k_()
end function
