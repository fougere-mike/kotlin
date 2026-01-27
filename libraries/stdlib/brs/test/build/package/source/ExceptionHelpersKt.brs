function throwUninitializedPropertyAccessException_Str_k_(name as String) as Dynamic
    throw UninitializedPropertyAccessException_create_StrN_k_(("lateinit property " + name) + " has not been initialized")
end function

function throwKotlinNothingValueException_k_() as Dynamic
    throw KotlinNothingValueException_create_k_()
end function

function noWhenBranchMatchedException_k_() as Dynamic
    throw NoWhenBranchMatchedException_create_k_()
end function

function THROW_ISE_k_() as Dynamic
    throw IllegalStateException_create_k_()
end function

function THROW_CCE_k_() as Dynamic
    throw ClassCastException_create_k_()
end function

function THROW_NPE_k_() as Dynamic
    throw NullPointerException_create_k_()
end function

function THROW_IAE_Str_k_(msg as String) as Dynamic
    throw IllegalArgumentException_create_StrN_k_(msg)
end function

function ensureNotNull_AnyN_k_(v as Dynamic) as Object
    __when_tmp0 = invalid
    if v = invalid then
        __when_tmp0 = THROW_NPE_k_()
    else if true then
        __when_tmp0 = v
    end if
    return __when_tmp0

end function
