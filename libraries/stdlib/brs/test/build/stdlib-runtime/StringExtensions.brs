function isNotEmpty_rCharSequence_Z_k_(m as Object) as Boolean
    return m.length > 0
end function

function isNullOrEmpty_rCharSequenceN_Z_k_(m as Dynamic) as Boolean
    return (m = invalid) or (m.length = 0)
end function

function isEmpty_rCharSequence_Z_k_(m as Object) as Boolean
    return m.length = 0
end function
