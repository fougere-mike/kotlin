function get_PI_k_() as Double
    return 3.141592653589793#
end function

function get_E_k_() as Double
    return 2.718281828459045#
end function

function sin_D_k_(x as Double) as Double
    return Sin(x)
end function

function cos_D_k_(x as Double) as Double
    return Cos(x)
end function

function tan_D_k_(x as Double) as Double
    return Tan(x)
end function

function asin_D_k_(x as Double) as Double
    if (isNaN_rD_k_(x) or (x < -1.0#)) or (x > 1.0#) then
        return (0.0# / 0.0#)
    end if
    if x = 1.0# then
        return 3.141592653589793# / 2
    end if
    if x = -1.0# then
        return -3.141592653589793# / 2
    end if
    return Atn(x / Sqr(1 - (x * x)))
end function

function acos_D_k_(x as Double) as Double
    if (isNaN_rD_k_(x) or (x < -1.0#)) or (x > 1.0#) then
        return (0.0# / 0.0#)
    end if
    return (3.141592653589793# / 2) - asin_D_k_(x)
end function

function atan_D_k_(x as Double) as Double
    return Atn(x)
end function

function atan2_D_D_k_(y as Double, x as Double) as Double
    if isNaN_rD_k_(y) or isNaN_rD_k_(x) then
        return (0.0# / 0.0#)
    end if
    if (y = 0.0#) and (x > 0.0#) then
        return 0.0#
    end if
    if (y = 0.0#) and (x < 0.0#) then
        return 3.141592653589793#
    end if
    if (x = 0.0#) and (y > 0.0#) then
        return 3.141592653589793# / 2
    end if
    if (x = 0.0#) and (y < 0.0#) then
        return -3.141592653589793# / 2
    end if
    if x > 0.0# then
        return Atn(y / x)
    end if
    if (x < 0.0#) and (y >= 0.0#) then
        return Atn(y / x) + 3.141592653589793#
    end if
    if (x < 0.0#) and (y < 0.0#) then
        return Atn(y / x) - 3.141592653589793#
    end if
    return 0.0#
end function

function sinh_D_k_(x as Double) as Double
    if isNaN_rD_k_(x) then
        return (0.0# / 0.0#)
    end if
    if isInfinite_rD_k_(x) then
        return x
    end if
    return (Exp(x) - Exp(-x)) / 2
end function

function cosh_D_k_(x as Double) as Double
    if isNaN_rD_k_(x) then
        return (0.0# / 0.0#)
    end if
    if isInfinite_rD_k_(x) then
        return (1.0E+309#)
    end if
    return (Exp(x) + Exp(-x)) / 2
end function

function tanh_D_k_(x as Double) as Double
    if isNaN_rD_k_(x) then
        return (0.0# / 0.0#)
    end if
    if x = (1.0E+309#) then
        return 1.0#
    end if
    if x = (-1.0E+309#) then
        return -1.0#
    end if
    ex = Exp(x)
    emx = Exp(-x)
    return (ex - emx) / (ex + emx)
end function

function asinh_D_k_(x as Double) as Double
    if isNaN_rD_k_(x) then
        return (0.0# / 0.0#)
    end if
    if isInfinite_rD_k_(x) then
        return x
    end if
    return Log(x + Sqr((x * x) + 1))
end function

function acosh_D_k_(x as Double) as Double
    if isNaN_rD_k_(x) or (x < 1.0#) then
        return (0.0# / 0.0#)
    end if
    if x = (1.0E+309#) then
        return (1.0E+309#)
    end if
    return Log(x + Sqr((x * x) - 1))
end function

function atanh_D_k_(x as Double) as Double
    if (isNaN_rD_k_(x) or (x < -1.0#)) or (x > 1.0#) then
        return (0.0# / 0.0#)
    end if
    if x = 1.0# then
        return (1.0E+309#)
    end if
    if x = -1.0# then
        return (-1.0E+309#)
    end if
    return 0.5# * Log((1 + x) / (1 - x))
end function

function hypot_D_D_k_(x as Double, y as Double) as Double
    if isInfinite_rD_k_(x) or isInfinite_rD_k_(y) then
        return (1.0E+309#)
    end if
    if isNaN_rD_k_(x) or isNaN_rD_k_(y) then
        return (0.0# / 0.0#)
    end if
    return Sqr((x * x) + (y * y))
end function

function sqrt_D_k_(x as Double) as Double
    return Sqr(x)
end function

function exp_D_k_(x as Double) as Double
    return Exp(x)
end function

function expm1_D_k_(x as Double) as Double
    if isNaN_rD_k_(x) then
        return (0.0# / 0.0#)
    end if
    if x = (1.0E+309#) then
        return (1.0E+309#)
    end if
    if x = (-1.0E+309#) then
        return -1.0#
    end if
    return Exp(x) - 1
end function

function log_D_D_k_(x as Double, base as Double) as Double
    if isNaN_rD_k_(x) or isNaN_rD_k_(base) then
        return (0.0# / 0.0#)
    end if
    if ((x < 0) or (base <= 0)) or (base = 1.0#) then
        return (0.0# / 0.0#)
    end if
    return Log(x) / Log(base)
end function

function ln_D_k_(x as Double) as Double
    return Log(x)
end function

function log10_D_k_(x as Double) as Double
    return Log(x) / Log(10.0#)
end function

function log2_D_k_(x as Double) as Double
    return Log(x) / Log(2.0#)
end function

function ln1p_D_k_(x as Double) as Double
    if isNaN_rD_k_(x) or (x < -1.0#) then
        return (0.0# / 0.0#)
    end if
    if x = -1.0# then
        return (-1.0E+309#)
    end if
    if x = (1.0E+309#) then
        return (1.0E+309#)
    end if
    return Log(1 + x)
end function

function ceil_D_k_(x as Double) as Double
    return (function(x)
        if x > Int(x) then return Int(x) + 1 else return Int(x)
    end function)(x)
end function

function floor_D_k_(x as Double) as Double
    return Int(x)
end function

function truncate_D_k_(x as Double) as Double
    return Int(x)
end function

function round_D_k_(x as Double) as Double
    if isNaN_rD_k_(x) or isInfinite_rD_k_(x) then
        return x
    end if
    floor = floor_D_k_(x)
    diff = x - floor
    __when_tmp1 = invalid
    if diff < 0.5# then
        __when_tmp1 = floor
    else if diff > 0.5# then
        __when_tmp1 = (floor + 1)
    else if true then
        __when_tmp0 = invalid
        if (floor mod 2) = 0& then
            __when_tmp0 = floor
        else if true then
            __when_tmp0 = (floor + 1)
        end if
        __when_tmp1 = __when_tmp0
    end if
    return __when_tmp1

end function

function abs_D_k_(x as Double) as Double
    return Abs(x)
end function

function sign_D_k_(x as Double) as Double
    __when_tmp2 = invalid
    if isNaN_rD_k_(x) then
        __when_tmp2 = (0.0# / 0.0#)
    else if x > 0 then
        __when_tmp2 = 1.0#
    else if x < 0 then
        __when_tmp2 = -1.0#
    else if true then
        __when_tmp2 = 0.0#
    end if
    return __when_tmp2

end function

function min_D_D_k_(a as Double, b as Double) as Double
    return brsIntrinsicMinDouble_D_D_k_(a, b)
end function

function max_D_D_k_(a as Double, b as Double) as Double
    return brsIntrinsicMaxDouble_D_D_k_(a, b)
end function

function cbrt_D_k_(x as Double) as Double
    if (isNaN_rD_k_(x) or isInfinite_rD_k_(x)) or (x = 0.0#) then
        return x
    end if
    __when_tmp3 = invalid
    if x < 0 then
        __when_tmp3 = -1.0#
    else if true then
        __when_tmp3 = 1.0#
    end if
    sign = __when_tmp3

    return sign * (abs_D_k_(x) ^ (1.0# / 3.0#))
end function

function pow_rD_D_k_(m as Double, x as Double) as Double
    return m ^ x
end function

function pow_rD_I_k_(m as Double, n as Integer) as Double
    return m ^ n
end function

function get_absoluteValue_rD_k_(m as Double) as Double
    return abs_D_k_(m)
end function

function get_sign_rD_k_(m as Double) as Double
    return sign_D_k_(m)
end function

function withSign_rD_D_k_(m as Double, sign as Double) as Double
    __when_tmp4 = invalid
    if (m < 0) or ((m = 0.0#) and ((1.0# / m) < 0)) then
        __when_tmp4 = -1.0#
    else if true then
        __when_tmp4 = 1.0#
    end if
    thisSign = __when_tmp4

    __when_tmp5 = invalid
    if (sign < 0) or ((sign = 0.0#) and ((1.0# / sign) < 0)) then
        __when_tmp5 = -1.0#
    else if true then
        __when_tmp5 = 1.0#
    end if
    newSign = __when_tmp5

    __when_tmp6 = invalid
    if thisSign = newSign then
        __when_tmp6 = m
    else if true then
        __when_tmp6 = -m
    end if
    return __when_tmp6

end function

function withSign_rD_I_k_(m as Double, sign as Integer) as Double
    return withSign_rD_D_k_(m, sign)
end function

function get_ulp_rD_k_(m as Double) as Double
    if isNaN_rD_k_(m) then
        return (0.0# / 0.0#)
    end if
    if isInfinite_rD_k_(m) then
        return (1.0E+309#)
    end if
    if m = 0.0# then
        return 4.9E-324#
    end if
    bits = toBits_rD_k_(m)
    __when_tmp7 = invalid
    if m > 0 then
        __when_tmp7 = (bits + 1)
    else if true then
        __when_tmp7 = (bits - 1)
    end if
    nextBits = __when_tmp7

    return abs_D_k_(fromBits_rCompanion_J_k_(Double_Companion_getInstance(), nextBits) - m)
end function

function nextUp_rD_k_(m as Double) as Double
    if isNaN_rD_k_(m) or (m = (1.0E+309#)) then
        return m
    end if
    if m = 0.0# then
        return 4.9E-324#
    end if
    bits = toBits_rD_k_(m)
    __when_tmp8 = invalid
    if m > 0 then
        __when_tmp8 = (bits + 1)
    else if true then
        __when_tmp8 = (bits - 1)
    end if
    return fromBits_rCompanion_J_k_(Double_Companion_getInstance(), __when_tmp8)

end function

function nextDown_rD_k_(m as Double) as Double
    if isNaN_rD_k_(m) or (m = (-1.0E+309#)) then
        return m
    end if
    if m = 0.0# then
        return -4.9E-324#
    end if
    bits = toBits_rD_k_(m)
    __when_tmp9 = invalid
    if m > 0 then
        __when_tmp9 = (bits - 1)
    else if true then
        __when_tmp9 = (bits + 1)
    end if
    return fromBits_rCompanion_J_k_(Double_Companion_getInstance(), __when_tmp9)

end function

function nextTowards_rD_D_k_(m as Double, to_ as Double) as Double
    if isNaN_rD_k_(m) or isNaN_rD_k_(to_) then
        return (0.0# / 0.0#)
    end if
    if m = to_ then
        return m
    end if
    __when_tmp10 = invalid
    if to_ > m then
        __when_tmp10 = nextUp_rD_k_(m)
    else if true then
        __when_tmp10 = nextDown_rD_k_(m)
    end if
    return __when_tmp10

end function

function roundToInt_rD_k_(m as Double) as Integer
    if isNaN_rD_k_(m) then
        throw IllegalArgumentException_create_StrN_k_("Cannot round NaN to Int")
    end if
    if m > 2147483647 then
        return 2147483647
    end if
    if m < -2147483648 then
        return -2147483648
    end if
    return m + 0.5#
end function

function roundToLong_rD_k_(m as Double) as LongInteger
    if isNaN_rD_k_(m) then
        throw IllegalArgumentException_create_StrN_k_("Cannot round NaN to Long")
    end if
    if m > 9223372036854775807& then
        return 9223372036854775807&
    end if
    if m < -9223372036854775808& then
        return -9223372036854775808&
    end if
    return m + 0.5#
end function

function sin_F_k_(x as Float) as Float
    return sin_D_k_(x)
end function

function cos_F_k_(x as Float) as Float
    return cos_D_k_(x)
end function

function tan_F_k_(x as Float) as Float
    return tan_D_k_(x)
end function

function asin_F_k_(x as Float) as Float
    return asin_D_k_(x)
end function

function acos_F_k_(x as Float) as Float
    return acos_D_k_(x)
end function

function atan_F_k_(x as Float) as Float
    return atan_D_k_(x)
end function

function atan2_F_F_k_(y as Float, x as Float) as Float
    return atan2_D_D_k_(y, x)
end function

function sinh_F_k_(x as Float) as Float
    return sinh_D_k_(x)
end function

function cosh_F_k_(x as Float) as Float
    return cosh_D_k_(x)
end function

function tanh_F_k_(x as Float) as Float
    return tanh_D_k_(x)
end function

function asinh_F_k_(x as Float) as Float
    return asinh_D_k_(x)
end function

function acosh_F_k_(x as Float) as Float
    return acosh_D_k_(x)
end function

function atanh_F_k_(x as Float) as Float
    return atanh_D_k_(x)
end function

function hypot_F_F_k_(x as Float, y as Float) as Float
    return hypot_D_D_k_(x, y)
end function

function sqrt_F_k_(x as Float) as Float
    return sqrt_D_k_(x)
end function

function exp_F_k_(x as Float) as Float
    return exp_D_k_(x)
end function

function expm1_F_k_(x as Float) as Float
    return expm1_D_k_(x)
end function

function log_F_F_k_(x as Float, base as Float) as Float
    return log_D_D_k_(x, base)
end function

function ln_F_k_(x as Float) as Float
    return ln_D_k_(x)
end function

function log10_F_k_(x as Float) as Float
    return log10_D_k_(x)
end function

function log2_F_k_(x as Float) as Float
    return log2_D_k_(x)
end function

function ln1p_F_k_(x as Float) as Float
    return ln1p_D_k_(x)
end function

function ceil_F_k_(x as Float) as Float
    return ceil_D_k_(x)
end function

function floor_F_k_(x as Float) as Float
    return floor_D_k_(x)
end function

function truncate_F_k_(x as Float) as Float
    return truncate_D_k_(x)
end function

function round_F_k_(x as Float) as Float
    return round_D_k_(x)
end function

function abs_F_k_(x as Float) as Float
    return abs_D_k_(x)
end function

function sign_F_k_(x as Float) as Float
    return sign_D_k_(x)
end function

function min_F_F_k_(a as Float, b as Float) as Float
    return min_D_D_k_(a, b)
end function

function max_F_F_k_(a as Float, b as Float) as Float
    return max_D_D_k_(a, b)
end function

function cbrt_F_k_(x as Float) as Float
    return cbrt_D_k_(x)
end function

function pow_rF_F_k_(m as Float, x as Float) as Float
    return pow_rD_D_k_(m, x)
end function

function pow_rF_I_k_(m as Float, n as Integer) as Float
    return pow_rD_I_k_(m, n)
end function

function get_absoluteValue_rF_k_(m as Float) as Float
    return abs_F_k_(m)
end function

function get_sign_rF_k_(m as Float) as Float
    return sign_F_k_(m)
end function

function withSign_rF_F_k_(m as Float, sign as Float) as Float
    return withSign_rD_D_k_(m, sign)
end function

function withSign_rF_I_k_(m as Float, sign as Integer) as Float
    return withSign_rD_I_k_(m, sign)
end function

function roundToInt_rF_k_(m as Float) as Integer
    return roundToInt_rD_k_(m)
end function

function roundToLong_rF_k_(m as Float) as LongInteger
    return roundToLong_rD_k_(m)
end function

function abs_I_k_(n as Integer) as Integer
    return Abs(n)
end function

function min_I_I_k_(a as Integer, b as Integer) as Integer
    return brsIntrinsicMinInt_I_I_k_(a, b)
end function

function max_I_I_k_(a as Integer, b as Integer) as Integer
    return brsIntrinsicMaxInt_I_I_k_(a, b)
end function

function get_absoluteValue_rI_k_(m as Integer) as Integer
    return abs_I_k_(m)
end function

function get_sign_rI_k_(m as Integer) as Integer
    __when_tmp11 = invalid
    if m > 0 then
        __when_tmp11 = 1
    else if m < 0 then
        __when_tmp11 = -1
    else if true then
        __when_tmp11 = 0
    end if
    return __when_tmp11

end function

function abs_J_k_(n as LongInteger) as LongInteger
    __when_tmp12 = invalid
    if n < 0 then
        __when_tmp12 = -n
    else if true then
        __when_tmp12 = n
    end if
    return __when_tmp12

end function

function min_J_J_k_(a as LongInteger, b as LongInteger) as LongInteger
    __when_tmp13 = invalid
    if a <= b then
        __when_tmp13 = a
    else if true then
        __when_tmp13 = b
    end if
    return __when_tmp13

end function

function max_J_J_k_(a as LongInteger, b as LongInteger) as LongInteger
    __when_tmp14 = invalid
    if a >= b then
        __when_tmp14 = a
    else if true then
        __when_tmp14 = b
    end if
    return __when_tmp14

end function

function get_absoluteValue_rJ_k_(m as LongInteger) as LongInteger
    return abs_J_k_(m)
end function

function get_sign_rJ_k_(m as LongInteger) as Integer
    __when_tmp15 = invalid
    if m > 0& then
        __when_tmp15 = 1
    else if m < 0& then
        __when_tmp15 = -1
    else if true then
        __when_tmp15 = 0
    end if
    return __when_tmp15

end function
