function brsIntrinsicMinDouble_D_D_D_k_(a as Double, b as Double) as Double
    __when_tmp0 = invalid
    if a <= b then
        __when_tmp0 = a
    else if true then
        __when_tmp0 = b
    end if
    return __when_tmp0

end function

function brsIntrinsicMaxDouble_D_D_D_k_(a as Double, b as Double) as Double
    __when_tmp1 = invalid
    if a >= b then
        __when_tmp1 = a
    else if true then
        __when_tmp1 = b
    end if
    return __when_tmp1

end function

function brsIntrinsicMinInt_I_I_I_k_(a as Integer, b as Integer) as Integer
    __when_tmp2 = invalid
    if a <= b then
        __when_tmp2 = a
    else if true then
        __when_tmp2 = b
    end if
    return __when_tmp2

end function

function brsIntrinsicMaxInt_I_I_I_k_(a as Integer, b as Integer) as Integer
    __when_tmp3 = invalid
    if a >= b then
        __when_tmp3 = a
    else if true then
        __when_tmp3 = b
    end if
    return __when_tmp3

end function

function brsIntrinsicIsNaN_D_Z_k_(x as Double) as Boolean
    return x <> x
end function

function brsIntrinsicIsNaNFloat_F_Z_k_(x as Float) as Boolean
    return x <> x
end function

function brsIntrinsicIsInfinite_D_Z_k_(x as Double) as Boolean
    return (x = (1.0E+309#)) or (x = (-1.0E+309#))
end function

function brsIntrinsicIsInfiniteFloat_F_Z_k_(x as Float) as Boolean
    return (x = Infinity!) or (x = -Infinity!)
end function
