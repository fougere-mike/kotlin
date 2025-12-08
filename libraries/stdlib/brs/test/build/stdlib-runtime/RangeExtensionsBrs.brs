function reversed_rIntProgression_IntProgression_k_(m as Object) as Object
    return IntProgression_Companion_fromClosedRange_I_I_I_IntProgression_k_(m.last, m.first, -m.step)
end function

function reversed_rLongProgression_LongProgression_k_(m as Object) as Object
    return LongProgression_Companion_fromClosedRange_J_J_J_LongProgression_k_(m.last, m.first, -m.step)
end function

function reversed_rCharProgression_CharProgression_k_(m as Object) as Object
    return CharProgression_Companion_fromClosedRange_C_C_I_CharProgression_k_(m.last, m.first, -m.step)
end function

function coerceIn_rJ_J_J_J_k_(m as LongInteger, minimumValue as LongInteger, maximumValue as LongInteger) as LongInteger
    if minimumValue > maximumValue then
        throw IllegalArgumentException_create_StrN_IllegalArgumentException_k_(((("Cannot coerce value to an empty range: maximum " + maximumValue) + " is less than minimum ") + minimumValue) + ".")
    end if
    if m < minimumValue then
        return minimumValue
    end if
    if m > maximumValue then
        return maximumValue
    end if
    return m
end function

function coerceIn_rJ_ClosedRangeJ_J_k_(m as LongInteger, range as Object) as LongInteger
    if __kotlin_isInstanceOf(range, "ClosedFloatingPointRange") then
        return coerceIn_rJ_ClosedFloatingPointRangeJ_J_k_(m, range)
    end if
    if range.isEmpty() then
        throw IllegalArgumentException_create_StrN_IllegalArgumentException_k_(("Cannot coerce value to an empty range: " + range) + ".")
    end if
    __when_tmp0 = invalid
    if m < range.start then
        __when_tmp0 = range.start
    else if m > range.endInclusive then
        __when_tmp0 = range.endInclusive
    else if true then
        __when_tmp0 = m
    end if
    return __when_tmp0

end function

function coerceIn_rJ_OpenEndRangeJ_J_k_(m as LongInteger, range as Object) as LongInteger
    if range.isEmpty() then
        throw IllegalArgumentException_create_StrN_IllegalArgumentException_k_(("Cannot coerce value to an empty range: " + range) + ".")
    end if
    __when_tmp1 = invalid
    if m < range.start then
        __when_tmp1 = range.start
    else if m >= range.endExclusive then
        __when_tmp1 = (range.endExclusive - 1)
    else if true then
        __when_tmp1 = m
    end if
    return __when_tmp1

end function

function coerceIn_rC_C_C_C_k_(m as Object, minimumValue as Object, maximumValue as Object) as Object
    if (minimumValue > maximumValue) > 0 then
        throw IllegalArgumentException_create_StrN_IllegalArgumentException_k_(((("Cannot coerce value to an empty range: maximum " + maximumValue) + " is less than minimum ") + minimumValue) + ".")
    end if
    if (m < minimumValue) < 0 then
        return minimumValue
    end if
    if (m > maximumValue) > 0 then
        return maximumValue
    end if
    return m
end function

function coerceIn_rC_ClosedRangeC_C_k_(m as Object, range as Object) as Object
    if range.isEmpty() then
        throw IllegalArgumentException_create_StrN_IllegalArgumentException_k_(("Cannot coerce value to an empty range: " + range) + ".")
    end if
    __when_tmp2 = invalid
    if (m < range.start) < 0 then
        __when_tmp2 = range.start
    else if (m > range.endInclusive) > 0 then
        __when_tmp2 = range.endInclusive
    else if true then
        __when_tmp2 = m
    end if
    return __when_tmp2

end function

function coerceIn_rC_OpenEndRangeC_C_k_(m as Object, range as Object) as Object
    if range.isEmpty() then
        throw IllegalArgumentException_create_StrN_IllegalArgumentException_k_(("Cannot coerce value to an empty range: " + range) + ".")
    end if
    __when_tmp3 = invalid
    if (m < range.start) < 0 then
        __when_tmp3 = range.start
    else if (m >= range.endExclusive) >= 0 then
        __when_tmp3 = (get_code_rC_I_k_(range.endExclusive) - 1)
    else if true then
        __when_tmp3 = m
    end if
    return __when_tmp3

end function

function coerceAtLeast_rJ_J_J_k_(m as LongInteger, minimumValue as LongInteger) as LongInteger
    __when_tmp4 = invalid
    if m < minimumValue then
        __when_tmp4 = minimumValue
    else if true then
        __when_tmp4 = m
    end if
    return __when_tmp4

end function

function coerceAtMost_rJ_J_J_k_(m as LongInteger, maximumValue as LongInteger) as LongInteger
    __when_tmp5 = invalid
    if m > maximumValue then
        __when_tmp5 = maximumValue
    else if true then
        __when_tmp5 = m
    end if
    return __when_tmp5

end function

function coerceAtLeast_rC_C_C_k_(m as Object, minimumValue as Object) as Object
    __when_tmp6 = invalid
    if (m < minimumValue) < 0 then
        __when_tmp6 = minimumValue
    else if true then
        __when_tmp6 = m
    end if
    return __when_tmp6

end function

function coerceAtMost_rC_C_C_k_(m as Object, maximumValue as Object) as Object
    __when_tmp7 = invalid
    if (m > maximumValue) > 0 then
        __when_tmp7 = maximumValue
    else if true then
        __when_tmp7 = m
    end if
    return __when_tmp7

end function

function coerceAtLeast_rB_B_B_k_(m as Integer, minimumValue as Integer) as Integer
    __when_tmp8 = invalid
    if m < minimumValue then
        __when_tmp8 = minimumValue
    else if true then
        __when_tmp8 = m
    end if
    return __when_tmp8

end function

function coerceAtMost_rB_B_B_k_(m as Integer, maximumValue as Integer) as Integer
    __when_tmp9 = invalid
    if m > maximumValue then
        __when_tmp9 = maximumValue
    else if true then
        __when_tmp9 = m
    end if
    return __when_tmp9

end function

function coerceAtLeast_rS_S_S_k_(m as Integer, minimumValue as Integer) as Integer
    __when_tmp10 = invalid
    if m < minimumValue then
        __when_tmp10 = minimumValue
    else if true then
        __when_tmp10 = m
    end if
    return __when_tmp10

end function

function coerceAtMost_rS_S_S_k_(m as Integer, maximumValue as Integer) as Integer
    __when_tmp11 = invalid
    if m > maximumValue then
        __when_tmp11 = maximumValue
    else if true then
        __when_tmp11 = m
    end if
    return __when_tmp11

end function

function coerceAtLeast_rF_F_F_k_(m as Float, minimumValue as Float) as Float
    __when_tmp12 = invalid
    if m < minimumValue then
        __when_tmp12 = minimumValue
    else if true then
        __when_tmp12 = m
    end if
    return __when_tmp12

end function

function coerceAtMost_rF_F_F_k_(m as Float, maximumValue as Float) as Float
    __when_tmp13 = invalid
    if m > maximumValue then
        __when_tmp13 = maximumValue
    else if true then
        __when_tmp13 = m
    end if
    return __when_tmp13

end function

function coerceAtLeast_rD_D_D_k_(m as Double, minimumValue as Double) as Double
    __when_tmp14 = invalid
    if m < minimumValue then
        __when_tmp14 = minimumValue
    else if true then
        __when_tmp14 = m
    end if
    return __when_tmp14

end function

function coerceAtMost_rD_D_D_k_(m as Double, maximumValue as Double) as Double
    __when_tmp15 = invalid
    if m > maximumValue then
        __when_tmp15 = maximumValue
    else if true then
        __when_tmp15 = m
    end if
    return __when_tmp15

end function

function ClosedFloatingPointRange_lessThanOrEquals_Any_Any_Z_k_(a as Object, b as Object) as Boolean
end function

function ClosedFloatingPointRange_contains_Any_Z_k_(value as Object) as Boolean
    return m.lessThanOrEquals(m.start, value) and m.lessThanOrEquals(value, m.endInclusive)
end function

function coerceIn_rI_ClosedFloatingPointRangeI_I_k_(m as Integer, range as Object) as Integer
    if range.isEmpty() then
        throw IllegalArgumentException_create_StrN_IllegalArgumentException_k_(("Cannot coerce value to an empty range: " + range) + ".")
    end if
    __when_tmp16 = invalid
    if range.lessThanOrEquals(m, range.start) then
        __when_tmp16 = range.start
    else if range.lessThanOrEquals(range.endInclusive, m) then
        __when_tmp16 = range.endInclusive
    else if true then
        __when_tmp16 = m
    end if
    return __when_tmp16

end function

function coerceIn_rJ_ClosedFloatingPointRangeJ_J_k_(m as LongInteger, range as Object) as LongInteger
    if range.isEmpty() then
        throw IllegalArgumentException_create_StrN_IllegalArgumentException_k_(("Cannot coerce value to an empty range: " + range) + ".")
    end if
    __when_tmp17 = invalid
    if range.lessThanOrEquals(m, range.start) then
        __when_tmp17 = range.start
    else if range.lessThanOrEquals(range.endInclusive, m) then
        __when_tmp17 = range.endInclusive
    else if true then
        __when_tmp17 = m
    end if
    return __when_tmp17

end function
