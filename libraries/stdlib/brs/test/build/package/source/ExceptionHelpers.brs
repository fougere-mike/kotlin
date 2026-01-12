sub throwUninitializedPropertyAccessException_Str_k_(name as String)
    throw UninitializedPropertyAccessException_create_StrN_k_(("lateinit property " + name) + " has not been initialized")
end sub

sub throwKotlinNothingValueException_k_()
    throw KotlinNothingValueException_create_k_()
end sub

sub noWhenBranchMatchedException_k_()
    throw NoWhenBranchMatchedException_create_k_()
end sub

sub THROW_ISE_k_()
    throw IllegalStateException_create_k_()
end sub

sub THROW_CCE_k_()
    throw ClassCastException_create_k_()
end sub

sub THROW_NPE_k_()
    throw NullPointerException_create_k_()
end sub

sub THROW_IAE_Str_k_(msg as String)
    throw IllegalArgumentException_create_StrN_k_(msg)
end sub

function ensureNotNull_AnyN_k_(v as Dynamic) as Object
    __when_tmp0 = invalid
    if v = invalid then
        __when_tmp0 = THROW_NPE_k_()
    else if true then
        __when_tmp0 = v
    end if
    return __when_tmp0

end function
