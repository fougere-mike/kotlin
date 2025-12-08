function uintCompare_I_I_I_k_(v1 as Integer, v2 as Integer) as Integer
    return v1.xor(-2147483648).compareTo(v2.xor(-2147483648))
end function

function ulongCompare_J_J_I_k_(v1 as LongInteger, v2 as LongInteger) as Integer
    return v1.xor(-9223372036854775808&).compareTo(v2.xor(-9223372036854775808&))
end function

function uintDivide_I_I_UInt_k_(dividend as Integer, divisor as Integer) as Object
    dividendLong = dividend.and(4294967295&)
    divisorLong = divisor.and(4294967295&)
    return UInt_create_I_UInt_k_(dividendLong / divisorLong)
end function

function uintRemainder_I_I_UInt_k_(dividend as Integer, divisor as Integer) as Object
    dividendLong = dividend.and(4294967295&)
    divisorLong = divisor.and(4294967295&)
    return UInt_create_I_UInt_k_(dividendLong mod divisorLong)
end function

function ulongDivide_J_J_ULong_k_(dividend as LongInteger, divisor as LongInteger) as Object
    if divisor < 0 then
        __when_tmp0 = invalid
        if ulongCompare_J_J_I_k_(dividend, divisor) < 0 then
            __when_tmp0 = ULong_create_J_ULong_k_(0&)
        else if true then
            __when_tmp0 = ULong_create_J_ULong_k_(1&)
        end if
        return __when_tmp0

    end if
    if dividend >= 0 then
        return ULong_create_J_ULong_k_(dividend / divisor)
    end if
    quotient = dividend.ushr(1) / divisor.shl(1)
    remainder = dividend - (quotient * divisor)
    __when_tmp1 = invalid
    if ulongCompare_J_J_I_k_(remainder, divisor) >= 0 then
        __when_tmp1 = 1
    else if true then
        __when_tmp1 = 0
    end if
    return ULong_create_J_ULong_k_(quotient + __when_tmp1)

end function

function ulongRemainder_J_J_ULong_k_(dividend as LongInteger, divisor as LongInteger) as Object
    if divisor < 0 then
        __when_tmp2 = invalid
        if ulongCompare_J_J_I_k_(dividend, divisor) < 0 then
            __when_tmp2 = ULong_create_J_ULong_k_(dividend)
        else if true then
            __when_tmp2 = ULong_create_J_ULong_k_(dividend - divisor)
        end if
        return __when_tmp2

    end if
    if dividend >= 0 then
        return ULong_create_J_ULong_k_(dividend mod divisor)
    end if
    quotient = dividend.ushr(1) / divisor.shl(1)
    remainder = dividend - (quotient * divisor)
    __when_tmp3 = invalid
    if ulongCompare_J_J_I_k_(remainder, divisor) >= 0 then
        __when_tmp3 = divisor
    else if true then
        __when_tmp3 = 0&
    end if
    return ULong_create_J_ULong_k_(remainder - __when_tmp3)

end function

function uintToLong_I_J_k_(value as Integer) as LongInteger
    return value.and(4294967295&)
end function

function uintToULong_I_ULong_k_(value as Integer) as Object
    return ULong_create_J_ULong_k_(uintToLong_I_J_k_(value))
end function

function uintToDouble_I_D_k_(value as Integer) as Double
    return value.and(2147483647) + (value.ushr(31).shl(30) * 2)
end function

function uintToFloat_I_F_k_(value as Integer) as Float
    return uintToDouble_I_D_k_(value)
end function

function doubleToUInt_D_UInt_k_(value as Double) as Object
    __when_tmp4 = invalid
    if isNaN_rD_Z_k_(value) then
        __when_tmp4 = UInt_create_I_UInt_k_(0)
    else if value <= 0.0# then
        __when_tmp4 = UInt_create_I_UInt_k_(0)
    else if value >= 4.294967295E9# then
        __when_tmp4 = UInt_create_I_UInt_k_(-1)
    else if value <= 2147483647 then
        __when_tmp4 = UInt_create_I_UInt_k_(value)
    else if true then
        __when_tmp4 = UInt_create_I_UInt_k_((value - 2147483647) + 2147483647)
    end if
    return __when_tmp4

end function

function floatToUInt_F_UInt_k_(value as Float) as Object
    return doubleToUInt_D_UInt_k_(value)
end function

function ulongToDouble_J_D_k_(value as LongInteger) as Double
    return (value.ushr(11) * 2048) + value.and(2047&)
end function

function ulongToFloat_J_F_k_(value as LongInteger) as Float
    return ulongToDouble_J_D_k_(value)
end function

function doubleToULong_D_ULong_k_(value as Double) as Object
    __when_tmp5 = invalid
    if isNaN_rD_Z_k_(value) then
        __when_tmp5 = ULong_create_J_ULong_k_(0&)
    else if value <= 0.0# then
        __when_tmp5 = ULong_create_J_ULong_k_(0&)
    else if value >= 1.8446744073709552E19# then
        __when_tmp5 = ULong_create_J_ULong_k_(-1&)
    else if value < 9223372036854775807& then
        __when_tmp5 = ULong_create_J_ULong_k_(value)
    else if true then
        __when_tmp5 = ULong_create_J_ULong_k_((value - 9.223372036854776E18#) + -9223372036854775808&)
    end if
    return __when_tmp5

end function

function floatToULong_F_ULong_k_(value as Float) as Object
    return doubleToULong_D_ULong_k_(value)
end function

function uintToString_I_Str_k_(value as Integer) as String
    return Str(uintToLong_I_J_k_(value))
end function

function uintToString_I_I_Str_k_(value as Integer, base as Integer) as String
    return ulongToString_J_I_Str_k_(uintToLong_I_J_k_(value), base)
end function

function ulongToString_J_Str_k_(value as LongInteger) as String
    return ulongToString_J_I_Str_k_(value, 10)
end function

function longToStringWithRadix_J_I_Str_k_(value as LongInteger, radix as Integer) as String
    if (radix < 2) or (radix > 36) then
        throw IllegalArgumentException_create_StrN_IllegalArgumentException_k_(("radix " + radix) + " was not in valid range 2..36")
    end if
    if value = 0& then
        return "0"
    end if
    isNegative = value < 0
    __when_tmp6 = invalid
    if isNegative then
        __when_tmp6 = -value
    else if true then
        __when_tmp6 = value
    end if
    v = __when_tmp6

    digits = "0123456789abcdefghijklmnopqrstuvwxyz"
    result = StringBuilder_create_StringBuilder_k_()
    while v <> 0&
        digit = v mod radix
        result.insert(0, digits[digit])
        v = v.div(radix)
    end while
    if isNegative then
        result.insert(0, "-")
    end if
    return result.toString()
end function

function ulongToString_J_I_Str_k_(value as LongInteger, base as Integer) as String
    if value >= 0 then
        return longToStringWithRadix_J_I_Str_k_(value, base)
    end if
    quotient = value.ushr(1) / base.shl(1)
    rem = value - (quotient * base)
    if rem_ >= base then
        rem = rem_.minus(base)
        quotient = (quotient + 1)
    end if
    return longToStringWithRadix_J_I_Str_k_(quotient, base) + longToStringWithRadix_J_I_Str_k_(rem_, base)
end function
