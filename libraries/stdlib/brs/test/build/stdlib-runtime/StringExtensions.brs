function isNotEmpty_rCharSequence_k_(m as Object) as Boolean
    return __kotlin_charSequenceLength_CharSequenceN_k_(m) > 0
end function

function isNullOrEmpty_rCharSequenceN_k_(m as Dynamic) as Boolean
    return (m = invalid) or (__kotlin_charSequenceLength_CharSequenceN_k_(m) = 0)
end function

function isEmpty_rCharSequence_k_(m as Object) as Boolean
    return __kotlin_charSequenceLength_CharSequenceN_k_(m) = 0
end function
