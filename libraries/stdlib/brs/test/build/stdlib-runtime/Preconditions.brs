sub checkBoundsIndexes_I_I_I_k_(startIndex as Integer, endIndex as Integer, size as Integer)
    if (startIndex < 0) or (endIndex > size) then
        throw IndexOutOfBoundsException_create_StrN_k_((((("startIndex: " + __kotlin_numToStr_I_k_(startIndex)) + ", endIndex: ") + __kotlin_numToStr_I_k_(endIndex)) + ", size: ") + __kotlin_numToStr_I_k_(size))
    end if
    if startIndex > endIndex then
        throw IllegalArgumentException_create_StrN_k_((("startIndex: " + __kotlin_numToStr_I_k_(startIndex)) + " > endIndex: ") + __kotlin_numToStr_I_k_(endIndex))
    end if
end sub

sub checkElementIndex_I_I_k_(index as Integer, size as Integer)
    if (index < 0) or (index >= size) then
        throw IndexOutOfBoundsException_create_StrN_k_((("index: " + __kotlin_numToStr_I_k_(index)) + ", size: ") + __kotlin_numToStr_I_k_(size))
    end if
end sub

sub checkPositionIndex_I_I_k_(index as Integer, size as Integer)
    if (index < 0) or (index > size) then
        throw IndexOutOfBoundsException_create_StrN_k_((("index: " + __kotlin_numToStr_I_k_(index)) + ", size: ") + __kotlin_numToStr_I_k_(size))
    end if
end sub

sub checkRangeIndexes_I_I_I_k_(fromIndex as Integer, toIndex as Integer, size as Integer)
    if (fromIndex < 0) or (toIndex > size) then
        throw IndexOutOfBoundsException_create_StrN_k_((((("fromIndex: " + __kotlin_numToStr_I_k_(fromIndex)) + ", toIndex: ") + __kotlin_numToStr_I_k_(toIndex)) + ", size: ") + __kotlin_numToStr_I_k_(size))
    end if
    if fromIndex > toIndex then
        throw IllegalArgumentException_create_StrN_k_((("fromIndex: " + __kotlin_numToStr_I_k_(fromIndex)) + " > toIndex: ") + __kotlin_numToStr_I_k_(toIndex))
    end if
end sub

sub numberFormatError_Str_k_(input as String)
    throw NumberFormatException_create_StrN_k_(("Invalid number format: '" + input) + "'")
end sub
