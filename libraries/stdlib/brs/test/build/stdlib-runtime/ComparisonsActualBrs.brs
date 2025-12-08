function maxOf_Any_Any_Any_k_(a as Object, b as Object) as Object
    __when_tmp0 = invalid
    if (a >= b) >= 0 then
        __when_tmp0 = a
    else if true then
        __when_tmp0 = b
    end if
    return __when_tmp0

end function

function maxOf_B_B_B_k_(a as Integer, b as Integer) as Integer
    __when_tmp1 = invalid
    if a >= b then
        __when_tmp1 = a
    else if true then
        __when_tmp1 = b
    end if
    return __when_tmp1

end function

function maxOf_S_S_S_k_(a as Integer, b as Integer) as Integer
    __when_tmp2 = invalid
    if a >= b then
        __when_tmp2 = a
    else if true then
        __when_tmp2 = b
    end if
    return __when_tmp2

end function

function maxOf_I_I_I_k_(a as Integer, b as Integer) as Integer
    return max_I_I_I_k_(a, b)
end function

function maxOf_J_J_J_k_(a as LongInteger, b as LongInteger) as LongInteger
    return max_J_J_J_k_(a, b)
end function

function maxOf_F_F_F_k_(a as Float, b as Float) as Float
    return max_F_F_F_k_(a, b)
end function

function maxOf_D_D_D_k_(a as Double, b as Double) as Double
    return max_D_D_D_k_(a, b)
end function

function maxOf_Any_Any_Any_Any_k_(a as Object, b as Object, c as Object) as Object
    return maxOf_Any_Any_Any_k_(a, maxOf_Any_Any_Any_k_(b, c))
end function

function maxOf_B_B_B_B_k_(a as Integer, b as Integer, c as Integer) as Integer
    return maxOf_B_B_B_k_(a, maxOf_B_B_B_k_(b, c))
end function

function maxOf_S_S_S_S_k_(a as Integer, b as Integer, c as Integer) as Integer
    return maxOf_S_S_S_k_(a, maxOf_S_S_S_k_(b, c))
end function

function maxOf_I_I_I_I_k_(a as Integer, b as Integer, c as Integer) as Integer
    return maxOf_I_I_I_k_(a, maxOf_I_I_I_k_(b, c))
end function

function maxOf_J_J_J_J_k_(a as LongInteger, b as LongInteger, c as LongInteger) as LongInteger
    return maxOf_J_J_J_k_(a, maxOf_J_J_J_k_(b, c))
end function

function maxOf_F_F_F_F_k_(a as Float, b as Float, c as Float) as Float
    return maxOf_F_F_F_k_(a, maxOf_F_F_F_k_(b, c))
end function

function maxOf_D_D_D_D_k_(a as Double, b as Double, c as Double) as Double
    return maxOf_D_D_D_k_(a, maxOf_D_D_D_k_(b, c))
end function

function maxOf_Any_Arr_Any_k_(a as Object, other as Object) as Object
    max = a
    for each e in other
        if (max < e) < 0 then
            max = e
        end if
    end for
    return max
end function

function maxOf_B_ByteArray_B_k_(a as Integer, other as Object) as Integer
    max = a
    for each e in other
        if max < e then
            max = e
        end if
    end for
    return max
end function

function maxOf_S_ShortArray_S_k_(a as Integer, other as Object) as Integer
    max = a
    for each e in other
        if max < e then
            max = e
        end if
    end for
    return max
end function

function maxOf_I_IntArray_I_k_(a as Integer, other as Object) as Integer
    max = a
    for each e in other
        if max < e then
            max = e
        end if
    end for
    return max
end function

function maxOf_J_LongArray_J_k_(a as LongInteger, other as Object) as LongInteger
    max = a
    for each e in other
        if max < e then
            max = e
        end if
    end for
    return max
end function

function maxOf_F_FloatArray_F_k_(a as Float, other as Object) as Float
    max = a
    for each e in other
        if isNaN_rF_Z_k_(max) then
            return NaN!
        end if
        if isNaN_rF_Z_k_(e) then
            return NaN!
        end if
        if max < e then
            max = e
        end if

    end for
    return max
end function

function maxOf_D_DoubleArray_D_k_(a as Double, other as Object) as Double
    max = a
    for each e in other
        if isNaN_rD_Z_k_(max) then
            return (0.0# / 0.0#)
        end if
        if isNaN_rD_Z_k_(e) then
            return (0.0# / 0.0#)
        end if
        if max < e then
            max = e
        end if

    end for
    return max
end function

function minOf_Any_Any_Any_k_(a as Object, b as Object) as Object
    __when_tmp3 = invalid
    if (a <= b) <= 0 then
        __when_tmp3 = a
    else if true then
        __when_tmp3 = b
    end if
    return __when_tmp3

end function

function minOf_B_B_B_k_(a as Integer, b as Integer) as Integer
    __when_tmp4 = invalid
    if a <= b then
        __when_tmp4 = a
    else if true then
        __when_tmp4 = b
    end if
    return __when_tmp4

end function

function minOf_S_S_S_k_(a as Integer, b as Integer) as Integer
    __when_tmp5 = invalid
    if a <= b then
        __when_tmp5 = a
    else if true then
        __when_tmp5 = b
    end if
    return __when_tmp5

end function

function minOf_I_I_I_k_(a as Integer, b as Integer) as Integer
    return min_I_I_I_k_(a, b)
end function

function minOf_J_J_J_k_(a as LongInteger, b as LongInteger) as LongInteger
    return min_J_J_J_k_(a, b)
end function

function minOf_F_F_F_k_(a as Float, b as Float) as Float
    return min_F_F_F_k_(a, b)
end function

function minOf_D_D_D_k_(a as Double, b as Double) as Double
    return min_D_D_D_k_(a, b)
end function

function minOf_Any_Any_Any_Any_k_(a as Object, b as Object, c as Object) as Object
    return minOf_Any_Any_Any_k_(a, minOf_Any_Any_Any_k_(b, c))
end function

function minOf_B_B_B_B_k_(a as Integer, b as Integer, c as Integer) as Integer
    return minOf_B_B_B_k_(a, minOf_B_B_B_k_(b, c))
end function

function minOf_S_S_S_S_k_(a as Integer, b as Integer, c as Integer) as Integer
    return minOf_S_S_S_k_(a, minOf_S_S_S_k_(b, c))
end function

function minOf_I_I_I_I_k_(a as Integer, b as Integer, c as Integer) as Integer
    return minOf_I_I_I_k_(a, minOf_I_I_I_k_(b, c))
end function

function minOf_J_J_J_J_k_(a as LongInteger, b as LongInteger, c as LongInteger) as LongInteger
    return minOf_J_J_J_k_(a, minOf_J_J_J_k_(b, c))
end function

function minOf_F_F_F_F_k_(a as Float, b as Float, c as Float) as Float
    return minOf_F_F_F_k_(a, minOf_F_F_F_k_(b, c))
end function

function minOf_D_D_D_D_k_(a as Double, b as Double, c as Double) as Double
    return minOf_D_D_D_k_(a, minOf_D_D_D_k_(b, c))
end function

function minOf_Any_Arr_Any_k_(a as Object, other as Object) as Object
    min = a
    for each e in other
        if (e < min) < 0 then
            min = e
        end if
    end for
    return min
end function

function minOf_B_ByteArray_B_k_(a as Integer, other as Object) as Integer
    min = a
    for each e in other
        if e < min then
            min = e
        end if
    end for
    return min
end function

function minOf_S_ShortArray_S_k_(a as Integer, other as Object) as Integer
    min = a
    for each e in other
        if e < min then
            min = e
        end if
    end for
    return min
end function

function minOf_I_IntArray_I_k_(a as Integer, other as Object) as Integer
    min = a
    for each e in other
        if e < min then
            min = e
        end if
    end for
    return min
end function

function minOf_J_LongArray_J_k_(a as LongInteger, other as Object) as LongInteger
    min = a
    for each e in other
        if e < min then
            min = e
        end if
    end for
    return min
end function

function minOf_F_FloatArray_F_k_(a as Float, other as Object) as Float
    min = a
    for each e in other
        if isNaN_rF_Z_k_(min) then
            return NaN!
        end if
        if isNaN_rF_Z_k_(e) then
            return NaN!
        end if
        if e < min then
            min = e
        end if

    end for
    return min
end function

function minOf_D_DoubleArray_D_k_(a as Double, other as Object) as Double
    min = a
    for each e in other
        if isNaN_rD_Z_k_(min) then
            return (0.0# / 0.0#)
        end if
        if isNaN_rD_Z_k_(e) then
            return (0.0# / 0.0#)
        end if
        if e < min then
            min = e
        end if

    end for
    return min
end function
