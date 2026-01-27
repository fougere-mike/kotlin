function maxOf_Any_Any_k_(a as Object, b as Object) as Object
    __when_tmp0 = invalid
    if a.compareTo_AnyN_k_(b) >= 0 then
        __when_tmp0 = a
    else if true then
        __when_tmp0 = b
    end if
    return __when_tmp0

end function

function maxOf_B_B_k_(a as Integer, b as Integer) as Integer
    __when_tmp1 = invalid
    if a >= b then
        __when_tmp1 = a
    else if true then
        __when_tmp1 = b
    end if
    return __when_tmp1

end function

function maxOf_S_S_k_(a as Integer, b as Integer) as Integer
    __when_tmp2 = invalid
    if a >= b then
        __when_tmp2 = a
    else if true then
        __when_tmp2 = b
    end if
    return __when_tmp2

end function

function maxOf_I_I_k_(a as Integer, b as Integer) as Integer
    return max_I_I_k_(a, b)
end function

function maxOf_J_J_k_(a as LongInteger, b as LongInteger) as LongInteger
    return max_J_J_k_(a, b)
end function

function maxOf_F_F_k_(a as Float, b as Float) as Float
    return max_F_F_k_(a, b)
end function

function maxOf_D_D_k_(a as Double, b as Double) as Double
    return max_D_D_k_(a, b)
end function

function maxOf_Any_Any_Any_k_(a as Object, b as Object, c as Object) as Object
    return maxOf_Any_Any_k_(a, maxOf_Any_Any_k_(b, c))
end function

function maxOf_B_B_B_k_(a as Integer, b as Integer, c as Integer) as Integer
    tmp0 = a
    tmp0_1 = b
    tmp2_1 = c
    tmp_ret_0 = invalid

    while true
        a = tmp0_1
        b = tmp2_1
        __when_tmp3 = invalid
        if a >= b then
            __when_tmp3 = a
        else if true then
            __when_tmp3 = b
        end if
        tmp_ret_0 = __when_tmp3
        exit while
    end while
    tmp2 = tmp_ret_0

    tmp_ret_1 = invalid

    while true
        a = tmp0
        b = tmp2
        __when_tmp4 = invalid
        if a >= b then
            __when_tmp4 = a
        else if true then
            __when_tmp4 = b
        end if
        tmp_ret_1 = __when_tmp4
        exit while
    end while
    return tmp_ret_1

end function

function maxOf_S_S_S_k_(a as Integer, b as Integer, c as Integer) as Integer
    tmp0 = a
    tmp0_1 = b
    tmp2_1 = c
    tmp_ret_0 = invalid

    while true
        a = tmp0_1
        b = tmp2_1
        __when_tmp5 = invalid
        if a >= b then
            __when_tmp5 = a
        else if true then
            __when_tmp5 = b
        end if
        tmp_ret_0 = __when_tmp5
        exit while
    end while
    tmp2 = tmp_ret_0

    tmp_ret_1 = invalid

    while true
        a = tmp0
        b = tmp2
        __when_tmp6 = invalid
        if a >= b then
            __when_tmp6 = a
        else if true then
            __when_tmp6 = b
        end if
        tmp_ret_1 = __when_tmp6
        exit while
    end while
    return tmp_ret_1

end function

function maxOf_I_I_I_k_(a as Integer, b as Integer, c as Integer) as Integer
    tmp0 = a
    tmp0_1 = b
    tmp2_1 = c
    tmp_ret_0 = invalid

    while true
        a = tmp0_1
        b = tmp2_1
        tmp_ret_0 = max_I_I_k_(a, b)
        exit while
    end while
    tmp2 = tmp_ret_0

    tmp_ret_1 = invalid

    while true
        a = tmp0
        b = tmp2
        tmp_ret_1 = max_I_I_k_(a, b)
        exit while
    end while
    return tmp_ret_1

end function

function maxOf_J_J_J_k_(a as LongInteger, b as LongInteger, c as LongInteger) as LongInteger
    tmp0 = a
    tmp0_1 = b
    tmp2_1 = c
    tmp_ret_0 = invalid

    while true
        a = tmp0_1
        b = tmp2_1
        tmp_ret_0 = max_J_J_k_(a, b)
        exit while
    end while
    tmp2 = tmp_ret_0

    tmp_ret_1 = invalid

    while true
        a = tmp0
        b = tmp2
        tmp_ret_1 = max_J_J_k_(a, b)
        exit while
    end while
    return tmp_ret_1

end function

function maxOf_F_F_F_k_(a as Float, b as Float, c as Float) as Float
    tmp0 = a
    tmp0_1 = b
    tmp2_1 = c
    tmp_ret_0 = invalid

    while true
        a = tmp0_1
        b = tmp2_1
        tmp_ret_0 = max_F_F_k_(a, b)
        exit while
    end while
    tmp2 = tmp_ret_0

    tmp_ret_1 = invalid

    while true
        a = tmp0
        b = tmp2
        tmp_ret_1 = max_F_F_k_(a, b)
        exit while
    end while
    return tmp_ret_1

end function

function maxOf_D_D_D_k_(a as Double, b as Double, c as Double) as Double
    tmp0 = a
    tmp0_1 = b
    tmp2_1 = c
    tmp_ret_0 = invalid

    while true
        a = tmp0_1
        b = tmp2_1
        tmp_ret_0 = max_D_D_k_(a, b)
        exit while
    end while
    tmp2 = tmp_ret_0

    tmp_ret_1 = invalid

    while true
        a = tmp0
        b = tmp2
        tmp_ret_1 = max_D_D_k_(a, b)
        exit while
    end while
    return tmp_ret_1

end function

function maxOf_Any_Arr_k_(a as Object, other as Object) as Object
    max = a
    indexedObject = other
    inductionVariable = 0
    last = indexedObject.count()
    while inductionVariable < last
        e = indexedObject[inductionVariable]
        inductionVariable = (inductionVariable + 1)

        if max.compareTo_AnyN_k_(e) < 0 then
            max = e
        end if
    end while

    return max
end function

function maxOf_B_ByteArray_k_(a as Integer, other as Object) as Integer
    max = a
    indexedObject = other
    inductionVariable = 0
    last = indexedObject.__get_size()
    while inductionVariable < last
        e = indexedObject.get_I_k_(inductionVariable)
        inductionVariable = (inductionVariable + 1)

        if max < e then
            max = e
        end if
    end while

    return max
end function

function maxOf_S_ShortArray_k_(a as Integer, other as Object) as Integer
    max = a
    indexedObject = other
    inductionVariable = 0
    last = indexedObject.__get_size()
    while inductionVariable < last
        e = indexedObject.get_I_k_(inductionVariable)
        inductionVariable = (inductionVariable + 1)

        if max < e then
            max = e
        end if
    end while

    return max
end function

function maxOf_I_IntArray_k_(a as Integer, other as Object) as Integer
    max = a
    indexedObject = other
    inductionVariable = 0
    last = indexedObject.__get_size()
    while inductionVariable < last
        e = indexedObject.get_I_k_(inductionVariable)
        inductionVariable = (inductionVariable + 1)

        if max < e then
            max = e
        end if
    end while

    return max
end function

function maxOf_J_LongArray_k_(a as LongInteger, other as Object) as LongInteger
    max = a
    indexedObject = other
    inductionVariable = 0
    last = indexedObject.__get_size()
    while inductionVariable < last
        e = indexedObject.get_I_k_(inductionVariable)
        inductionVariable = (inductionVariable + 1)

        if max < e then
            max = e
        end if
    end while

    return max
end function

function maxOf_F_FloatArray_k_(a as Float, other as Object) as Float
    max = a
    indexedObject = other
    inductionVariable = 0
    last = indexedObject.__get_size()
    while inductionVariable < last
        e = indexedObject.get_I_k_(inductionVariable)
        inductionVariable = (inductionVariable + 1)

        if isNaN_rF_k_(max) then
            return NaN!
        end if
        if isNaN_rF_k_(e) then
            return NaN!
        end if
        if max < e then
            max = e
        end if

    end while

    return max
end function

function maxOf_D_DoubleArray_k_(a as Double, other as Object) as Double
    max = a
    indexedObject = other
    inductionVariable = 0
    last = indexedObject.__get_size()
    while inductionVariable < last
        e = indexedObject.get_I_k_(inductionVariable)
        inductionVariable = (inductionVariable + 1)

        if isNaN_rD_k_(max) then
            return (0.0# / 0.0#)
        end if
        if isNaN_rD_k_(e) then
            return (0.0# / 0.0#)
        end if
        if max < e then
            max = e
        end if

    end while

    return max
end function

function minOf_Any_Any_k_(a as Object, b as Object) as Object
    __when_tmp7 = invalid
    if a.compareTo_AnyN_k_(b) <= 0 then
        __when_tmp7 = a
    else if true then
        __when_tmp7 = b
    end if
    return __when_tmp7

end function

function minOf_B_B_k_(a as Integer, b as Integer) as Integer
    __when_tmp8 = invalid
    if a <= b then
        __when_tmp8 = a
    else if true then
        __when_tmp8 = b
    end if
    return __when_tmp8

end function

function minOf_S_S_k_(a as Integer, b as Integer) as Integer
    __when_tmp9 = invalid
    if a <= b then
        __when_tmp9 = a
    else if true then
        __when_tmp9 = b
    end if
    return __when_tmp9

end function

function minOf_I_I_k_(a as Integer, b as Integer) as Integer
    return min_I_I_k_(a, b)
end function

function minOf_J_J_k_(a as LongInteger, b as LongInteger) as LongInteger
    return min_J_J_k_(a, b)
end function

function minOf_F_F_k_(a as Float, b as Float) as Float
    return min_F_F_k_(a, b)
end function

function minOf_D_D_k_(a as Double, b as Double) as Double
    return min_D_D_k_(a, b)
end function

function minOf_Any_Any_Any_k_(a as Object, b as Object, c as Object) as Object
    return minOf_Any_Any_k_(a, minOf_Any_Any_k_(b, c))
end function

function minOf_B_B_B_k_(a as Integer, b as Integer, c as Integer) as Integer
    tmp0 = a
    tmp0_1 = b
    tmp2_1 = c
    tmp_ret_0 = invalid

    while true
        a = tmp0_1
        b = tmp2_1
        __when_tmp10 = invalid
        if a <= b then
            __when_tmp10 = a
        else if true then
            __when_tmp10 = b
        end if
        tmp_ret_0 = __when_tmp10
        exit while
    end while
    tmp2 = tmp_ret_0

    tmp_ret_1 = invalid

    while true
        a = tmp0
        b = tmp2
        __when_tmp11 = invalid
        if a <= b then
            __when_tmp11 = a
        else if true then
            __when_tmp11 = b
        end if
        tmp_ret_1 = __when_tmp11
        exit while
    end while
    return tmp_ret_1

end function

function minOf_S_S_S_k_(a as Integer, b as Integer, c as Integer) as Integer
    tmp0 = a
    tmp0_1 = b
    tmp2_1 = c
    tmp_ret_0 = invalid

    while true
        a = tmp0_1
        b = tmp2_1
        __when_tmp12 = invalid
        if a <= b then
            __when_tmp12 = a
        else if true then
            __when_tmp12 = b
        end if
        tmp_ret_0 = __when_tmp12
        exit while
    end while
    tmp2 = tmp_ret_0

    tmp_ret_1 = invalid

    while true
        a = tmp0
        b = tmp2
        __when_tmp13 = invalid
        if a <= b then
            __when_tmp13 = a
        else if true then
            __when_tmp13 = b
        end if
        tmp_ret_1 = __when_tmp13
        exit while
    end while
    return tmp_ret_1

end function

function minOf_I_I_I_k_(a as Integer, b as Integer, c as Integer) as Integer
    tmp0 = a
    tmp0_1 = b
    tmp2_1 = c
    tmp_ret_0 = invalid

    while true
        a = tmp0_1
        b = tmp2_1
        tmp_ret_0 = min_I_I_k_(a, b)
        exit while
    end while
    tmp2 = tmp_ret_0

    tmp_ret_1 = invalid

    while true
        a = tmp0
        b = tmp2
        tmp_ret_1 = min_I_I_k_(a, b)
        exit while
    end while
    return tmp_ret_1

end function

function minOf_J_J_J_k_(a as LongInteger, b as LongInteger, c as LongInteger) as LongInteger
    tmp0 = a
    tmp0_1 = b
    tmp2_1 = c
    tmp_ret_0 = invalid

    while true
        a = tmp0_1
        b = tmp2_1
        tmp_ret_0 = min_J_J_k_(a, b)
        exit while
    end while
    tmp2 = tmp_ret_0

    tmp_ret_1 = invalid

    while true
        a = tmp0
        b = tmp2
        tmp_ret_1 = min_J_J_k_(a, b)
        exit while
    end while
    return tmp_ret_1

end function

function minOf_F_F_F_k_(a as Float, b as Float, c as Float) as Float
    tmp0 = a
    tmp0_1 = b
    tmp2_1 = c
    tmp_ret_0 = invalid

    while true
        a = tmp0_1
        b = tmp2_1
        tmp_ret_0 = min_F_F_k_(a, b)
        exit while
    end while
    tmp2 = tmp_ret_0

    tmp_ret_1 = invalid

    while true
        a = tmp0
        b = tmp2
        tmp_ret_1 = min_F_F_k_(a, b)
        exit while
    end while
    return tmp_ret_1

end function

function minOf_D_D_D_k_(a as Double, b as Double, c as Double) as Double
    tmp0 = a
    tmp0_1 = b
    tmp2_1 = c
    tmp_ret_0 = invalid

    while true
        a = tmp0_1
        b = tmp2_1
        tmp_ret_0 = min_D_D_k_(a, b)
        exit while
    end while
    tmp2 = tmp_ret_0

    tmp_ret_1 = invalid

    while true
        a = tmp0
        b = tmp2
        tmp_ret_1 = min_D_D_k_(a, b)
        exit while
    end while
    return tmp_ret_1

end function

function minOf_Any_Arr_k_(a as Object, other as Object) as Object
    min = a
    indexedObject = other
    inductionVariable = 0
    last = indexedObject.count()
    while inductionVariable < last
        e = indexedObject[inductionVariable]
        inductionVariable = (inductionVariable + 1)

        if e.compareTo_AnyN_k_(min) < 0 then
            min = e
        end if
    end while

    return min
end function

function minOf_B_ByteArray_k_(a as Integer, other as Object) as Integer
    min = a
    indexedObject = other
    inductionVariable = 0
    last = indexedObject.__get_size()
    while inductionVariable < last
        e = indexedObject.get_I_k_(inductionVariable)
        inductionVariable = (inductionVariable + 1)

        if e < min then
            min = e
        end if
    end while

    return min
end function

function minOf_S_ShortArray_k_(a as Integer, other as Object) as Integer
    min = a
    indexedObject = other
    inductionVariable = 0
    last = indexedObject.__get_size()
    while inductionVariable < last
        e = indexedObject.get_I_k_(inductionVariable)
        inductionVariable = (inductionVariable + 1)

        if e < min then
            min = e
        end if
    end while

    return min
end function

function minOf_I_IntArray_k_(a as Integer, other as Object) as Integer
    min = a
    indexedObject = other
    inductionVariable = 0
    last = indexedObject.__get_size()
    while inductionVariable < last
        e = indexedObject.get_I_k_(inductionVariable)
        inductionVariable = (inductionVariable + 1)

        if e < min then
            min = e
        end if
    end while

    return min
end function

function minOf_J_LongArray_k_(a as LongInteger, other as Object) as LongInteger
    min = a
    indexedObject = other
    inductionVariable = 0
    last = indexedObject.__get_size()
    while inductionVariable < last
        e = indexedObject.get_I_k_(inductionVariable)
        inductionVariable = (inductionVariable + 1)

        if e < min then
            min = e
        end if
    end while

    return min
end function

function minOf_F_FloatArray_k_(a as Float, other as Object) as Float
    min = a
    indexedObject = other
    inductionVariable = 0
    last = indexedObject.__get_size()
    while inductionVariable < last
        e = indexedObject.get_I_k_(inductionVariable)
        inductionVariable = (inductionVariable + 1)

        if isNaN_rF_k_(min) then
            return NaN!
        end if
        if isNaN_rF_k_(e) then
            return NaN!
        end if
        if e < min then
            min = e
        end if

    end while

    return min
end function

function minOf_D_DoubleArray_k_(a as Double, other as Object) as Double
    min = a
    indexedObject = other
    inductionVariable = 0
    last = indexedObject.__get_size()
    while inductionVariable < last
        e = indexedObject.get_I_k_(inductionVariable)
        inductionVariable = (inductionVariable + 1)

        if isNaN_rD_k_(min) then
            return (0.0# / 0.0#)
        end if
        if isNaN_rD_k_(e) then
            return (0.0# / 0.0#)
        end if
        if e < min then
            min = e
        end if

    end while

    return min
end function
