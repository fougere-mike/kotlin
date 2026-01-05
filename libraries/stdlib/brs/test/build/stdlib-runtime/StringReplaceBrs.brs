function replace_rStr_Str_Str_Z_k_(m as String, oldValue as String, newValue as String, ignoreCase = false) as String
    if ignoreCase = invalid then
        ignoreCase = false
    end if
    if isEmpty_rStr_k_(oldValue) then
        result = StringBuilder_create_k_()
        result.append_StrN_k_(newValue)
        progression = until_rI_I_k_(0, Len(m))
        inductionVariable = progression.get_first()
        last = progression.get_last()
        if inductionVariable <= last then
                        i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            result.append_C_k_(m.get_I_k_(i))
            result.append_StrN_k_(newValue)


            while i <> last
                i = inductionVariable
                inductionVariable = (inductionVariable + 1)

                result.append_C_k_(m.get_I_k_(i))
                result.append_StrN_k_(newValue)

            end while

        end if
        return result.toString()
    end if
    currentIndex = 0
    result = StringBuilder_create_k_()
    while currentIndex < Len(m)
        nextIndex = indexOf_rStr_Str_I_Z_k_(m, oldValue, currentIndex, ignoreCase)
        if nextIndex < 0 then
            result.append_StrN_k_(substring_rStr_I_k_(m, currentIndex))
            exit while
        end if
        result.append_StrN_k_(substring_rStr_I_I_k_(m, currentIndex, nextIndex))
        result.append_StrN_k_(newValue)
        currentIndex = (nextIndex + Len(oldValue))
    end while
    return result.toString()
end function

function replace_rStr_C_C_Z_k_(m as String, oldChar as Object, newChar as Object, ignoreCase = false) as String
    if ignoreCase = invalid then
        ignoreCase = false
    end if
    return replace_rStr_Str_Str_Z_k_(m, oldChar.toString(), newChar.toString(), ignoreCase)
end function

function replaceFirst_rStr_Str_Str_Z_k_(m as String, oldValue as String, newValue as String, ignoreCase = false) as String
    if ignoreCase = invalid then
        ignoreCase = false
    end if
    index = indexOf_rStr_Str_I_Z_k_(m, oldValue, 0, ignoreCase)
    if index < 0 then
        return m
    end if
    return (substring_rStr_I_I_k_(m, 0, index) + newValue) + substring_rStr_I_k_(m, index + Len(oldValue))
end function

function replaceFirst_rStr_C_C_Z_k_(m as String, oldChar as Object, newChar as Object, ignoreCase = false) as String
    if ignoreCase = invalid then
        ignoreCase = false
    end if
    return replaceFirst_rStr_Str_Str_Z_k_(m, oldChar.toString(), newChar.toString(), ignoreCase)
end function

function replaceRange_rStr_IntRange_CharSequence_k_(m as String, range as Object, replacement as Object) as String
    return replaceRange_rStr_I_I_CharSequence_k_(m, range.get_start(), range.get_endInclusive() + 1, replacement)
end function

function replaceRange_rStr_I_I_CharSequence_k_(m as String, startIndex as Integer, endIndex as Integer, replacement as Object) as String
    if endIndex < startIndex then
        throw IndexOutOfBoundsException_create_StrN_k_(((("End index (" + __kotlin_numToStr_I_k_(endIndex)) + ") is less than start index (") + __kotlin_numToStr_I_k_(startIndex)) + ")")
    end if
    if startIndex < 0 then
        throw IndexOutOfBoundsException_create_StrN_k_(("Start index (" + __kotlin_numToStr_I_k_(startIndex)) + ") is negative")
    end if
    if endIndex > Len(m) then
        throw IndexOutOfBoundsException_create_StrN_k_(((("End index (" + __kotlin_numToStr_I_k_(endIndex)) + ") is greater than length (") + __kotlin_numToStr_I_k_(Len(m))) + ")")
    end if
    return (substring_rStr_I_I_k_(m, 0, startIndex) + toString_AnyN_k_(replacement)) + substring_rStr_I_k_(m, endIndex)
end function

function replaceRange_rCharSequence_IntRange_CharSequence_k_(m as Object, range as Object, replacement as Object) as String
    return replaceRange_rStr_IntRange_CharSequence_k_(toString_AnyN_k_(m), range, replacement)
end function

function replaceRange_rCharSequence_I_I_CharSequence_k_(m as Object, startIndex as Integer, endIndex as Integer, replacement as Object) as String
    return replaceRange_rStr_I_I_CharSequence_k_(toString_AnyN_k_(m), startIndex, endIndex, replacement)
end function
