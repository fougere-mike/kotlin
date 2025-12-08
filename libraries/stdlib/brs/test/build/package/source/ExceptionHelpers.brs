sub throwUninitializedPropertyAccessException_Str_k_(name as String)
    throw UninitializedPropertyAccessException_create_StrN_UninitializedPropertyAccessException_k_(("lateinit property " + name) + " has not been initialized")
end sub

sub throwKotlinNothingValueException()
    throw KotlinNothingValueException_create_KotlinNothingValueException_k_()
end sub

sub noWhenBranchMatchedException()
    throw NoWhenBranchMatchedException_create_NoWhenBranchMatchedException_k_()
end sub

sub THROW_ISE()
    throw IllegalStateException_create_IllegalStateException_k_()
end sub

sub THROW_CCE()
    throw ClassCastException_create_ClassCastException_k_()
end sub

sub THROW_NPE()
    throw NullPointerException_create_NullPointerException_k_()
end sub

sub THROW_IAE_Str_k_(msg as String)
    throw IllegalArgumentException_create_StrN_IllegalArgumentException_k_(msg)
end sub

function ensureNotNull_AnyN_Any_k_(v as Dynamic) as Object
    __when_tmp0 = invalid
    if v = invalid then
        __when_tmp0 = THROW_NPE()
    else if true then
        __when_tmp0 = v
    end if
    return __when_tmp0

end function
