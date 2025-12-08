function isNaN_rD_Z_k_(m as Double) as Boolean
    return m <> m
end function

function isInfinite_rD_Z_k_(m as Double) as Boolean
    return (m = (1.0E+309#)) or (m = (-1.0E+309#))
end function

function isFinite_rD_Z_k_(m as Double) as Boolean
    return isInfinite_rD_Z_k_(m).not() and isNaN_rD_Z_k_(m).not()
end function

function isNaN_rF_Z_k_(m as Float) as Boolean
    return m <> m
end function

function isInfinite_rF_Z_k_(m as Float) as Boolean
    return (m = Infinity!) or (m = -Infinity!)
end function

function isFinite_rF_Z_k_(m as Float) as Boolean
    return isInfinite_rF_Z_k_(m).not() and isNaN_rF_Z_k_(m).not()
end function

function toBits_rD_J_k_(m as Double) as LongInteger
    if isNaN_rD_Z_k_(m) then
        return 9221120237041090560&
    end if
    return toRawBits_rD_J_k_(m)
end function

function toRawBits_rD_J_k_(m as Double) as LongInteger
    __when_tmp3 = invalid
    if m = 0.0# then
        __when_tmp0 = invalid
        if (1.0# / m) < 0 then
            __when_tmp0 = -9223372036854775808&
        else if true then
            __when_tmp0 = 0&
        end if
        __when_tmp3 = __when_tmp0
    else if isNaN_rD_Z_k_(m) then
        __when_tmp3 = 9221120237041090560&
    else if m = (1.0E+309#) then
        __when_tmp3 = 9218868437227405312&
    else if m = (-1.0E+309#) then
        __when_tmp3 = -4503599627370496&
    else if true then
        __when_tmp3 = signBit.or(biasedExp.shl(52)).or(mantissaBits)
    end if
    return __when_tmp3

end function

function fromBits_rCompanion_J_D_k_(m as Object, bits as LongInteger) as Double
    tmp0_subject = bits
    if tmp0_subject = 0& then
        return 0.0#
    else if tmp0_subject = -9223372036854775808& then
        return -0.0#
    else if tmp0_subject = 9218868437227405312& then
        return (1.0E+309#)
    else if tmp0_subject = -4503599627370496& then
        return (-1.0E+309#)
    end if

    exp = bits.shr(52).and(2047&)
    mantissaBits = bits.and(4503599627370495&)
    if (exp = 2047) and (mantissaBits <> 0&) then
        return (0.0# / 0.0#)
    end if
    negative = bits < 0
    biasedExp = exp - 1023
    mantissa = 1.0#
    progression = until_rI_I_IntRange_k_(0, 52)
    inductionVariable = progression.first
    last = progression.last
    if lessOrEqual_I_I_Z_k_(inductionVariable, last) then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if mantissaBits.and(1&.shl(51 - i)) <> 0& then
            mantissa = (mantissa + (1.0# / 1&.shl(i + 1)))
        end if


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if mantissaBits.and(1&.shl(51 - i)) <> 0& then
                mantissa = (mantissa + (1.0# / 1&.shl(i + 1)))
            end if

        end while

    end if

    __when_tmp4 = invalid
    if biasedExp >= 0 then
        __when_tmp4 = (mantissa * 1&.shl(coerceAtMost_rI_I_I_k_(biasedExp, 62)))
    else if true then
        __when_tmp4 = (mantissa / 1&.shl(coerceAtMost_rI_I_I_k_(-biasedExp, 62)))
    end if
    result = __when_tmp4

    __when_tmp5 = invalid
    if negative then
        __when_tmp5 = -result
    else if true then
        __when_tmp5 = result
    end if
    return __when_tmp5

end function

function toBits_rF_I_k_(m as Float) as Integer
    if isNaN_rF_Z_k_(m) then
        return 2143289344
    end if
    return toRawBits_rF_I_k_(m)
end function

function toRawBits_rF_I_k_(m as Float) as Integer
    __when_tmp9 = invalid
    if m = 0.0! then
        __when_tmp6 = invalid
        if (1.0! / m) < 0 then
            __when_tmp6 = -2147483648
        else if true then
            __when_tmp6 = 0
        end if
        __when_tmp9 = __when_tmp6
    else if isNaN_rF_Z_k_(m) then
        __when_tmp9 = 2143289344
    else if m = Infinity! then
        __when_tmp9 = 2139095040
    else if m = -Infinity! then
        __when_tmp9 = 4286578688&
    else if true then
        __when_tmp9 = signBit.or(biasedExp.shl(23)).or(mantissaBits)
    end if
    return __when_tmp9

end function

function fromBits_rCompanion_I_F_k_(m as Object, bits as Integer) as Float
    tmp0_subject = bits
    if tmp0_subject = 0 then
        return 0.0!
    else if tmp0_subject = -2147483648 then
        return -0.0!
    else if tmp0_subject = 2139095040 then
        return Infinity!
    else if tmp0_subject = 4286578688& then
        return -Infinity!
    end if

    exp = bits.shr(23).and(255)
    mantissaBits = bits.and(8388607)
    if (exp = 255) and (mantissaBits <> 0) then
        return NaN!
    end if
    negative = bits < 0
    biasedExp = exp - 127
    mantissa = 1.0#
    progression = until_rI_I_IntRange_k_(0, 23)
    inductionVariable = progression.first
    last = progression.last
    if lessOrEqual_I_I_Z_k_(inductionVariable, last) then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if mantissaBits.and(1.shl(22 - i)) <> 0 then
            mantissa = (mantissa + (1.0# / 1.shl(i + 1)))
        end if


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if mantissaBits.and(1.shl(22 - i)) <> 0 then
                mantissa = (mantissa + (1.0# / 1.shl(i + 1)))
            end if

        end while

    end if

    __when_tmp10 = invalid
    if biasedExp >= 0 then
        __when_tmp10 = (mantissa * 1.shl(coerceAtMost_rI_I_I_k_(biasedExp, 30)))
    else if true then
        __when_tmp10 = (mantissa / 1.shl(coerceAtMost_rI_I_I_k_(-biasedExp, 30)))
    end if
    result = __when_tmp10

    __when_tmp11 = invalid
    if negative then
        __when_tmp11 = -result
    else if true then
        __when_tmp11 = result
    end if
    return __when_tmp11

end function

function lazy_Function0AnyN_LazyAnyN_k_(initializer as Function) as Object
    return UnsafeLazyImpl_create_Function0AnyN_UnsafeLazyImplAnyN_k_(initializer)
end function

function lazy_LazyThreadSafetyMode_Function0AnyN_LazyAnyN_k_(mode as Object, initializer as Function) as Object
    return UnsafeLazyImpl_create_Function0AnyN_UnsafeLazyImplAnyN_k_(initializer)
end function

function lazy_AnyN_Function0AnyN_LazyAnyN_k_(lock as Dynamic, initializer as Function) as Object
    return UnsafeLazyImpl_create_Function0AnyN_UnsafeLazyImplAnyN_k_(initializer)
end function

function UnsafeLazyImpl_create_Function0AnyN_UnsafeLazyImplAnyN_k_(initializer as Function) as Object
    this = {}
    this.__type = "UnsafeLazyImpl"
    this.__proto = ["UnsafeLazyImpl"]
    this.initializer = initializer
    this._value = UNINITIALIZED_VALUE_getInstance()
    this.isInitialized_Z_k_ = UnsafeLazyImpl_isInitialized_Z_k_
    this.toString_Str_k_ = UnsafeLazyImpl_toString_Str_k_
    this.get_initializer = UnsafeLazyImpl_get_initializer_Function0AnyN_k_
    this.get__value = UnsafeLazyImpl_get__value_AnyN_k_
    this.set__value = UnsafeLazyImpl_set__value_AnyN_k_
    this.get_value = UnsafeLazyImpl_get_value_AnyN_k_
    return this
end function

function UnsafeLazyImpl_isInitialized_Z_k_() as Boolean
    return EQEQEQ_AnyN_AnyN_Z_k_(m._value, UNINITIALIZED_VALUE_getInstance()).not()
end function

function UnsafeLazyImpl_toString_Str_k_() as String
    __when_tmp12 = invalid
    if m.isInitialized() then
        __when_tmp12 = m.value.toString()
    else if true then
        __when_tmp12 = "Lazy value not initialized yet."
    end if
    return __when_tmp12

end function

function UnsafeLazyImpl_get_initializer_Function0AnyN_k_() as Function
    return m.initializer
end function

function UnsafeLazyImpl_get__value_AnyN_k_() as Dynamic
    return m._value
end function

sub UnsafeLazyImpl_set__value_AnyN_k_(value as Dynamic)
    m._value = value
end sub

function UnsafeLazyImpl_get_value_AnyN_k_() as Dynamic
    if EQEQEQ_AnyN_AnyN_Z_k_(m._value, UNINITIALIZED_VALUE_getInstance()) then
        m._value = m.initializer.invoke()
    end if
    return m._value
end function

function UNINITIALIZED_VALUE_create_UNINITIALIZED_VALUE_k_() as Object
    this = {}
    this.__type = "UNINITIALIZED_VALUE"
    this.__proto = ["UNINITIALIZED_VALUE"]
    return this
end function

function UNINITIALIZED_VALUE_getInstance() as Object
    if m.UNINITIALIZED_VALUE_instance = invalid then
        m.UNINITIALIZED_VALUE_instance = UNINITIALIZED_VALUE_create()
    end if
    return m.UNINITIALIZED_VALUE_instance
end function
