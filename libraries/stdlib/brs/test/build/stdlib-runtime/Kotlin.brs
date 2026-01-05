function isNaN_rD_k_(m as Double) as Boolean
    return m <> m
end function

function isInfinite_rD_k_(m as Double) as Boolean
    return (m = (1.0E+309#)) or (m = (-1.0E+309#))
end function

function isFinite_rD_k_(m as Double) as Boolean
    return not isInfinite_rD_k_(m) and not isNaN_rD_k_(m)
end function

function isNaN_rF_k_(m as Float) as Boolean
    return m <> m
end function

function isInfinite_rF_k_(m as Float) as Boolean
    return (m = Infinity!) or (m = -Infinity!)
end function

function isFinite_rF_k_(m as Float) as Boolean
    return not isInfinite_rF_k_(m) and not isNaN_rF_k_(m)
end function

function toBits_rD_k_(m as Double) as LongInteger
    if isNaN_rD_k_(m) then
        return 9221120237041090560&
    end if
    return toRawBits_rD_k_(m)
end function

function toRawBits_rD_k_(m as Double) as LongInteger
    __when_tmp3 = invalid
    if m = 0.0# then
        __when_tmp0 = invalid
        if (1.0# / m) < 0 then
            __when_tmp0 = -9223372036854775808&
        else if true then
            __when_tmp0 = 0&
        end if
        __when_tmp3 = __when_tmp0
    else if isNaN_rD_k_(m) then
        __when_tmp3 = 9221120237041090560&
    else if m = (1.0E+309#) then
        __when_tmp3 = 9218868437227405312&
    else if m = (-1.0E+309#) then
        __when_tmp3 = -4503599627370496&
    else if true then
        negative = m < 0
        __when_tmp1 = invalid
        if negative then
            __when_tmp1 = -m
        else if true then
            __when_tmp1 = m
        end if
        absValue = __when_tmp1
        if absValue = 0.0# then
            return 0&
        end if
        exp = 0
        mantissa = absValue
        while mantissa >= 2.0#
            mantissa = (mantissa / 2.0#)
            exp = (exp + 1)
        end while
        while mantissa < 1.0#
            mantissa = (mantissa * 2.0#)
            exp = (exp - 1)
        end while
        biasedExp = exp + 1023
        mantissa = (mantissa - 1.0#)
        mantissaBits = 0&
        progression = until_rI_I_k_(0, 52)
        inductionVariable = progression.get_first()
        last = progression.get_last()
        if inductionVariable <= last then
                        i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            mantissa = (mantissa * 2.0#)
            if mantissa >= 1.0# then
                mantissaBits = (mantissaBits or (1& * (2 ^ (51 - i))))
                mantissa = (mantissa - 1.0#)
            end if


            while i <> last
                i = inductionVariable
                inductionVariable = (inductionVariable + 1)

                mantissa = (mantissa * 2.0#)
                if mantissa >= 1.0# then
                    mantissaBits = (mantissaBits or (1& * (2 ^ (51 - i))))
                    mantissa = (mantissa - 1.0#)
                end if

            end while

        end if
        __when_tmp2 = invalid
        if negative then
            __when_tmp2 = (1& * (2 ^ 63))
        else if true then
            __when_tmp2 = 0&
        end if
        signBit = __when_tmp2
        __when_tmp3 = ((signBit or (biasedExp * (2 ^ 52))) or mantissaBits)
    end if
    return __when_tmp3

end function

function fromBits_rCompanion_J_k_(m as Object, bits as LongInteger) as Double
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

    exp = (bits \ (2 ^ 52)) and 2047&
    mantissaBits = bits and 4503599627370495&
    if (exp = 2047) and (mantissaBits <> 0&) then
        return (0.0# / 0.0#)
    end if
    negative = bits < 0
    biasedExp = exp - 1023
    mantissa = 1.0#
    progression = until_rI_I_k_(0, 52)
    inductionVariable = progression.get_first()
    last = progression.get_last()
    if inductionVariable <= last then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if (mantissaBits and (1& * (2 ^ (51 - i)))) <> 0& then
            mantissa = (mantissa + (1.0# / (1& * (2 ^ (i + 1)))))
        end if


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if (mantissaBits and (1& * (2 ^ (51 - i)))) <> 0& then
                mantissa = (mantissa + (1.0# / (1& * (2 ^ (i + 1)))))
            end if

        end while

    end if

    __when_tmp4 = invalid
    if biasedExp >= 0 then
        __when_tmp4 = (mantissa * (1& * (2 ^ coerceAtMost_rI_I_k_(biasedExp, 62))))
    else if true then
        __when_tmp4 = (mantissa / (1& * (2 ^ coerceAtMost_rI_I_k_(-biasedExp, 62))))
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

function toBits_rF_k_(m as Float) as Integer
    if isNaN_rF_k_(m) then
        return 2143289344
    end if
    return toRawBits_rF_k_(m)
end function

function toRawBits_rF_k_(m as Float) as Integer
    __when_tmp9 = invalid
    if m = 0.0! then
        __when_tmp6 = invalid
        if (1.0! / m) < 0 then
            __when_tmp6 = -2147483648
        else if true then
            __when_tmp6 = 0
        end if
        __when_tmp9 = __when_tmp6
    else if isNaN_rF_k_(m) then
        __when_tmp9 = 2143289344
    else if m = Infinity! then
        __when_tmp9 = 2139095040
    else if m = -Infinity! then
        __when_tmp9 = 4286578688&
    else if true then
        negative = m < 0
        __when_tmp7 = invalid
        if negative then
            __when_tmp7 = -m
        else if true then
            __when_tmp7 = m
        end if
        absValue = __when_tmp7
        exp = 0
        mantissa = absValue
        while mantissa >= 2.0#
            mantissa = (mantissa / 2.0#)
            exp = (exp + 1)
        end while
        while mantissa < 1.0#
            mantissa = (mantissa * 2.0#)
            exp = (exp - 1)
        end while
        biasedExp = exp + 127
        mantissa = (mantissa - 1.0#)
        mantissaBits = 0
        progression = until_rI_I_k_(0, 23)
        inductionVariable = progression.get_first()
        last = progression.get_last()
        if inductionVariable <= last then
                        i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            mantissa = (mantissa * 2.0#)
            if mantissa >= 1.0# then
                mantissaBits = (mantissaBits or (1 * (2 ^ (22 - i))))
                mantissa = (mantissa - 1.0#)
            end if


            while i <> last
                i = inductionVariable
                inductionVariable = (inductionVariable + 1)

                mantissa = (mantissa * 2.0#)
                if mantissa >= 1.0# then
                    mantissaBits = (mantissaBits or (1 * (2 ^ (22 - i))))
                    mantissa = (mantissa - 1.0#)
                end if

            end while

        end if
        __when_tmp8 = invalid
        if negative then
            __when_tmp8 = (1 * (2 ^ 31))
        else if true then
            __when_tmp8 = 0
        end if
        signBit = __when_tmp8
        __when_tmp9 = ((signBit or (biasedExp * (2 ^ 23))) or mantissaBits)
    end if
    return __when_tmp9

end function

function fromBits_rCompanion_I_k_(m as Object, bits as Integer) as Float
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

    exp = (bits \ (2 ^ 23)) and 255
    mantissaBits = bits and 8388607
    if (exp = 255) and (mantissaBits <> 0) then
        return NaN!
    end if
    negative = bits < 0
    biasedExp = exp - 127
    mantissa = 1.0#
    progression = until_rI_I_k_(0, 23)
    inductionVariable = progression.get_first()
    last = progression.get_last()
    if inductionVariable <= last then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if (mantissaBits and (1 * (2 ^ (22 - i)))) <> 0 then
            mantissa = (mantissa + (1.0# / (1 * (2 ^ (i + 1)))))
        end if


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if (mantissaBits and (1 * (2 ^ (22 - i)))) <> 0 then
                mantissa = (mantissa + (1.0# / (1 * (2 ^ (i + 1)))))
            end if

        end while

    end if

    __when_tmp10 = invalid
    if biasedExp >= 0 then
        __when_tmp10 = (mantissa * (1 * (2 ^ coerceAtMost_rI_I_k_(biasedExp, 30))))
    else if true then
        __when_tmp10 = (mantissa / (1 * (2 ^ coerceAtMost_rI_I_k_(-biasedExp, 30))))
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

function lazy_Function0_k_(initializer as Object) as Object
    return UnsafeLazyImpl_create_Function0_k_(initializer)
end function

function lazy_LazyThreadSafetyMode_Function0_k_(mode as Object, initializer as Object) as Object
    return UnsafeLazyImpl_create_Function0_k_(initializer)
end function

function lazy_AnyN_Function0_k_(lock as Dynamic, initializer as Object) as Object
    return UnsafeLazyImpl_create_Function0_k_(initializer)
end function

function UnsafeLazyImpl_create_Function0_k_(initializer as Object) as Object
    this = {}
    this.__type = "UnsafeLazyImpl"
    this.__proto = ["UnsafeLazyImpl", "Lazy"]
    this.__id = __kotlin_nextObjectId()
    this.isInitialized_k_ = UnsafeLazyImpl_isInitialized_k_
    this.toString_k_ = UnsafeLazyImpl_toString_k_
    this.toString = UnsafeLazyImpl_toString_k_
    this.get_initializer = UnsafeLazyImpl_get_initializer_k_
    this.get__value = UnsafeLazyImpl_get__value_k_
    this.set__value = UnsafeLazyImpl_set__value_AnyN_k_
    this.get_value = UnsafeLazyImpl_get_value_k_
    this.initializer = initializer
    this._value = UNINITIALIZED_VALUE_getInstance()
    return this
end function

function UnsafeLazyImpl_isInitialized_k_() as Boolean
    return not __kotlin_identityEquals(m.get__value(), UNINITIALIZED_VALUE_getInstance())
end function

function UnsafeLazyImpl_toString_k_() as String
    __when_tmp12 = invalid
    if m.isInitialized_k_() then
        __when_tmp12 = toString_AnyN_k_(m.get_value())
    else if true then
        __when_tmp12 = "Lazy value not initialized yet."
    end if
    return __when_tmp12

end function

function UnsafeLazyImpl_get_initializer_k_() as Object
    return m.initializer
end function

function UnsafeLazyImpl_get__value_k_() as Dynamic
    return m._value
end function

sub UnsafeLazyImpl_set__value_AnyN_k_(value as Dynamic)
    m._value = value
end sub

function UnsafeLazyImpl_get_value_k_() as Dynamic
    if __kotlin_identityEquals(m.get__value(), UNINITIALIZED_VALUE_getInstance()) then
        m.set__value(m.get_initializer().invoke())
    end if
    return m.get__value()
end function

function UNINITIALIZED_VALUE_create_k_() as Object
    this = {}
    this.__type = "UNINITIALIZED_VALUE"
    this.__proto = ["UNINITIALIZED_VALUE"]
    this.__id = __kotlin_nextObjectId()
    return this
end function

function UNINITIALIZED_VALUE_getInstance() as Object
    if m.UNINITIALIZED_VALUE_instance = invalid then
        m.UNINITIALIZED_VALUE_instance = UNINITIALIZED_VALUE_create_k_()
    end if
    return m.UNINITIALIZED_VALUE_instance
end function

function brsStructuralEquals_AnyN_AnyN_k_(a as Dynamic, b as Dynamic) as Boolean
    if __kotlin_identityEquals(a, b) then
        return true
    end if
    if __kotlin_identityEquals(a, invalid) or __kotlin_identityEquals(b, invalid) then
        return false
    end if
    __when_tmp13 = invalid
    if Type(a) = "roAssociativeArray" then
        __when_tmp13 = a.equals(b)
    else if true then
        __when_tmp13 = (a = b)
    end if
    return __when_tmp13

end function

function brsCompareTo_AnyN_AnyN_k_(a as Dynamic, b as Dynamic) as Integer
    if __kotlin_identityEquals(a, b) then
        return 0
    end if
    if __kotlin_identityEquals(a, invalid) then
        return -1
    end if
    if __kotlin_identityEquals(b, invalid) then
        return 1
    end if
    __when_tmp14 = invalid
    if Type(a) = "roAssociativeArray" then
        __when_tmp14 = a.compareTo(b)
    else if true then
        __when_tmp14 = ((function(a, b)
            if a < b then return -1 else return (function(a, b)
                if a > b then return 1 else return 0
            end function)(a, b)
        end function)(a, b))
    end if
    return __when_tmp14

end function
