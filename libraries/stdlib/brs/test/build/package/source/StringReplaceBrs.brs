function replace_rStr_Str_Str_Z_Str_k_(m as String, oldValue as String, newValue as String, ignoreCase = false) as String
    if isEmpty_rStr_Z_k_(oldValue) then
        result = StringBuilder_create_StringBuilder_k_()
        result.append(newValue)
        for each i in until_rI_I_IntRange_k_(0, m.length)
            result.append(m[i])
            result.append(newValue)

        end for
        return result.toString()
    end if
    currentIndex = 0
    result = StringBuilder_create_StringBuilder_k_()
    while currentIndex < m.length
        nextIndex = indexOf_rStr_Str_I_Z_I_k_(m, oldValue, currentIndex, ignoreCase)
        if nextIndex < 0 then
            result.append(substring_rStr_I_Str_k_(m, currentIndex))
            exit while
        end if
        result.append(substring_rStr_I_I_Str_k_(m, currentIndex, nextIndex))
        result.append(newValue)
        currentIndex = (nextIndex + oldValue.length)
    end while
    return result.toString()
end function

function replace_rStr_C_C_Z_Str_k_(m as String, oldChar as Object, newChar as Object, ignoreCase = false) as String
    return replace_rStr_Str_Str_Z_Str_k_(m, oldChar.toString(), newChar.toString(), ignoreCase)
end function

function replaceFirst_rStr_Str_Str_Z_Str_k_(m as String, oldValue as String, newValue as String, ignoreCase = false) as String
    index = indexOf_rStr_Str_I_Z_I_k_(m, oldValue, 0, ignoreCase)
    if index < 0 then
        return m
    end if
    return (substring_rStr_I_I_Str_k_(m, 0, index) + newValue) + substring_rStr_I_Str_k_(m, index + oldValue.length)
end function

function replaceFirst_rStr_C_C_Z_Str_k_(m as String, oldChar as Object, newChar as Object, ignoreCase = false) as String
    return replaceFirst_rStr_Str_Str_Z_Str_k_(m, oldChar.toString(), newChar.toString(), ignoreCase)
end function

function replaceRange_rStr_IntRange_CharSequence_Str_k_(m as String, range as Object, replacement as Object) as String
    return replaceRange_rStr_I_I_CharSequence_Str_k_(m, range.start, range.endInclusive + 1, replacement)
end function

function replaceRange_rStr_I_I_CharSequence_Str_k_(m as String, startIndex as Integer, endIndex as Integer, replacement as Object) as String
    if endIndex < startIndex then
        throw IndexOutOfBoundsException_create_StrN_IndexOutOfBoundsException_k_(((("End index (" + endIndex) + ") is less than start index (") + startIndex) + ")")
    end if
    if startIndex < 0 then
        throw IndexOutOfBoundsException_create_StrN_IndexOutOfBoundsException_k_(("Start index (" + startIndex) + ") is negative")
    end if
    if endIndex > m.length then
        throw IndexOutOfBoundsException_create_StrN_IndexOutOfBoundsException_k_(((("End index (" + endIndex) + ") is greater than length (") + m.length) + ")")
    end if
    return (substring_rStr_I_I_Str_k_(m, 0, startIndex) + replacement.toString()) + substring_rStr_I_Str_k_(m, endIndex)
end function

function replaceRange_rCharSequence_IntRange_CharSequence_Str_k_(m as Object, range as Object, replacement as Object) as String
    return replaceRange_rStr_IntRange_CharSequence_Str_k_(m.toString(), range, replacement)
end function

function replaceRange_rCharSequence_I_I_CharSequence_Str_k_(m as Object, startIndex as Integer, endIndex as Integer, replacement as Object) as String
    return replaceRange_rStr_I_I_CharSequence_Str_k_(m.toString(), startIndex, endIndex, replacement)
end function
