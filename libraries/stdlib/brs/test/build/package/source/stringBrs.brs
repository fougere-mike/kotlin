function toUpperCase_rStr_k_(m as String) as String
    return UCase(m)
end function

function uppercase_rStr_k_(m as String) as String
    return UCase(m)
end function

function toLowerCase_rStr_k_(m as String) as String
    return LCase(m)
end function

function lowercase_rStr_k_(m as String) as String
    return LCase(m)
end function

function capitalize_rStr_k_(m as String) as String
    __when_tmp0 = invalid
    if isNotEmpty_rStr_k_(m) and isLowerCase_rC_k_(Mid(m, 0 + 1, 1)) then
        __when_tmp0 = (uppercase_rC_k_(Mid(m, 0 + 1, 1)) + substring_rStr_I_k_(m, 1))
    else if true then
        __when_tmp0 = m
    end if
    return __when_tmp0

end function

function decapitalize_rStr_k_(m as String) as String
    __when_tmp1 = invalid
    if isNotEmpty_rStr_k_(m) and isUpperCase_rC_k_(Mid(m, 0 + 1, 1)) then
        __when_tmp1 = (lowercase_rC_k_(Mid(m, 0 + 1, 1)) + substring_rStr_I_k_(m, 1))
    else if true then
        __when_tmp1 = m
    end if
    return __when_tmp1

end function

function contentEquals_rCharSequenceN_CharSequenceN_k_(m as Dynamic, other as Dynamic) as Boolean
    if __kotlin_identityEquals(m, other) then
        return true
    end if
    if (m = invalid) or (other = invalid) then
        return false
    end if
    if __kotlin_charSequenceLength_CharSequenceN_k_(m) <> __kotlin_charSequenceLength_CharSequenceN_k_(other) then
        return false
    end if
    progression = until_rI_I_k_(0, __kotlin_charSequenceLength_CharSequenceN_k_(m))
    inductionVariable = progression.get_first()
    last = progression.get_last()
    if inductionVariable <= last then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if m.get_I_k_(i) <> other.get_I_k_(i) then
            return false
        end if


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if m.get_I_k_(i) <> other.get_I_k_(i) then
                return false
            end if

        end while

    end if

    return true
end function

function contentEquals_rCharSequenceN_CharSequenceN_Z_k_(m as Dynamic, other as Dynamic, ignoreCase as Boolean) as Boolean
    if __kotlin_identityEquals(m, other) then
        return true
    end if
    if (m = invalid) or (other = invalid) then
        return false
    end if
    if __kotlin_charSequenceLength_CharSequenceN_k_(m) <> __kotlin_charSequenceLength_CharSequenceN_k_(other) then
        return false
    end if
    if not ignoreCase then
        progression = until_rI_I_k_(0, __kotlin_charSequenceLength_CharSequenceN_k_(m))
        inductionVariable = progression.get_first()
        last = progression.get_last()
        if inductionVariable <= last then
                        i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if m.get_I_k_(i) <> other.get_I_k_(i) then
                return false
            end if


            while i <> last
                i = inductionVariable
                inductionVariable = (inductionVariable + 1)

                if m.get_I_k_(i) <> other.get_I_k_(i) then
                    return false
                end if

            end while

        end if
    else if true then
        progression = until_rI_I_k_(0, __kotlin_charSequenceLength_CharSequenceN_k_(m))
        inductionVariable = progression.get_first()
        last = progression.get_last()
        if inductionVariable <= last then
                        i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if not equals_rC_C_Z_k_(m.get_I_k_(i), other.get_I_k_(i), true) then
                return false
            end if


            while i <> last
                i = inductionVariable
                inductionVariable = (inductionVariable + 1)

                if not equals_rC_C_Z_k_(m.get_I_k_(i), other.get_I_k_(i), true) then
                    return false
                end if

            end while

        end if
    end if
    return true
end function

function nativeIndexOf_rStr_Str_I_k_(m as String, str as String, fromIndex as Integer) as Integer
    return (function(fromIndex, m, str)
        if Instr(fromIndex + 1, m, str) = 0 then return -1 else return Instr(fromIndex + 1, m, str) - 1
    end function)(fromIndex, m, str)
end function

function nativeLastIndexOf_rStr_Str_I_k_(m as String, str as String, fromIndex as Integer) as Integer
    lastIndex = -1
    searchIndex = 0
    while searchIndex <= fromIndex
        found = (function(m, searchIndex, str)
            if Instr(searchIndex + 1, m, str) = 0 then return -1 else return Instr(searchIndex + 1, m, str) - 1
        end function)(m, searchIndex, str)
        if (found < 0) or (found > fromIndex) then
            exit while
        end if
        lastIndex = found
        searchIndex = (found + 1)
    end while
    return lastIndex
end function

function substring_rStr_I_k_(m as String, startIndex as Integer) as String
    return substring_rStr_I_I_k_(m, startIndex, Len(m))
end function

function substring_rStr_I_I_k_(m as String, startIndex as Integer, endIndex as Integer) as String
    if startIndex < 0 then
        throw IndexOutOfBoundsException_create_StrN_k_("startIndex: " + __kotlin_numToStr_I_k_(startIndex))
    end if
    if endIndex > Len(m) then
        throw IndexOutOfBoundsException_create_StrN_k_((("endIndex: " + __kotlin_numToStr_I_k_(endIndex)) + ", length: ") + __kotlin_numToStr_I_k_(Len(m)))
    end if
    if startIndex > endIndex then
        throw IllegalArgumentException_create_StrN_k_((("startIndex: " + __kotlin_numToStr_I_k_(startIndex)) + " > endIndex: ") + __kotlin_numToStr_I_k_(endIndex))
    end if
    if startIndex = endIndex then
        return ""
    end if
    return Mid(m, startIndex + 1, endIndex - startIndex)
end function

function indexOf_rStr_Str_I_Z_k_(m as String, string_ as String, startIndex = 0, ignoreCase = false) as Integer
    if startIndex = invalid then
        startIndex = 0
    end if
    if ignoreCase = invalid then
        ignoreCase = false
    end if
    if ignoreCase then
        return indexOf_rStr_Str_I_Z_k_(lowercase_rStr_k_(m), lowercase_rStr_k_(string_), startIndex, false)
    end if
    if startIndex < 0 then
        return indexOf_rStr_Str_I_Z_k_(m, string_, 0, invalid)
    end if
    if startIndex >= Len(m) then
        __when_tmp2 = invalid
        if isEmpty_rStr_k_(string_) then
            __when_tmp2 = Len(m)
        else if true then
            __when_tmp2 = -1
        end if
        return __when_tmp2

    end if
    return nativeIndexOf_rStr_Str_I_k_(m, string_, startIndex)
end function

function lastIndexOf_rStr_Str_I_Z_k_(m as String, string_ as String, startIndex = get_lastIndex_rStr_k_(m), ignoreCase = false) as Integer
    if startIndex = invalid then
        startIndex = get_lastIndex_rStr_k_(m)
    end if
    if ignoreCase = invalid then
        ignoreCase = false
    end if
    if ignoreCase then
        return lastIndexOf_rStr_Str_I_Z_k_(lowercase_rStr_k_(m), lowercase_rStr_k_(string_), startIndex, false)
    end if
    if startIndex < 0 then
        return -1
    end if
    return nativeLastIndexOf_rStr_Str_I_k_(m, string_, coerceAtMost_rI_I_k_(startIndex, Len(m)))
end function

function isEmpty_rStr_k_(m as String) as Boolean
    return Len(m) = 0
end function

function isNotEmpty_rStr_k_(m as String) as Boolean
    return Len(m) > 0
end function

function isBlank_rStr_k_(m as String) as Boolean
    if isEmpty_rStr_k_(m) then
        return true
    end if
    progression = until_rI_I_k_(0, Len(m))
    inductionVariable = progression.get_first()
    last = progression.get_last()
    if inductionVariable <= last then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if not isWhitespace_rC_k_(Mid(m, i + 1, 1)) then
            return false
        end if


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if not isWhitespace_rC_k_(Mid(m, i + 1, 1)) then
                return false
            end if

        end while

    end if

    return true
end function

function isNotBlank_rStr_k_(m as String) as Boolean
    return not isBlank_rStr_k_(m)
end function

function get_lastIndex_rStr_k_(m as String) as Integer
    return Len(m) - 1
end function

function getOrElse_rStr_I_Function1IC_k_(m as String, index as Integer, defaultValue as Object) as Object
    __when_tmp3 = invalid
    if (index >= 0) and (index < Len(m)) then
        __when_tmp3 = Mid(m, index + 1, 1)
    else if true then
        __when_tmp3 = defaultValue.invoke(index)
    end if
    return __when_tmp3

end function

function padStart_rStr_I_C_k_(m as String, length as Integer, padChar = " ") as String
    if padChar = invalid then
        padChar = " "
    end if
    if length <= 0 then
        throw IllegalArgumentException_create_StrN_k_(("Desired length " + __kotlin_numToStr_I_k_(length)) + " is less than zero.")
    end if
    if Len(m) >= length then
        return m
    end if
    sb = StringBuilder_create_I_k_(length)
    i = 0
    times = length - Len(m)
    while i < times
        sb.append_C_k_(padChar)
        i = (i + 1)
    end while
    sb.append_StrN_k_(m)
    return sb.toString()
end function

function padEnd_rStr_I_C_k_(m as String, length as Integer, padChar = " ") as String
    if padChar = invalid then
        padChar = " "
    end if
    if length <= 0 then
        throw IllegalArgumentException_create_StrN_k_(("Desired length " + __kotlin_numToStr_I_k_(length)) + " is less than zero.")
    end if
    if Len(m) >= length then
        return m
    end if
    sb = StringBuilder_create_I_k_(length)
    sb.append_StrN_k_(m)
    i = 0
    times = length - Len(m)
    while i < times
        sb.append_C_k_(padChar)
        i = (i + 1)
    end while
    return sb.toString()
end function

function repeat_rStr_I_k_(m as String, n as Integer) as String
    if n < 0 then
        throw IllegalArgumentException_create_StrN_k_(("Count 'n' must be non-negative, but was " + __kotlin_numToStr_I_k_(n)) + ".")
    end if
    if (n = 0) or isEmpty_rStr_k_(m) then
        return ""
    end if
    if n = 1 then
        return m
    end if
    sb = StringBuilder_create_I_k_(Len(m) * n)
    i = 0
    while i < n
        sb.append_StrN_k_(m)
        i = (i + 1)
    end while
    return sb.toString()
end function

function startsWith_rStr_Str_Z_k_(m as String, prefix as String, ignoreCase = false) as Boolean
    if ignoreCase = invalid then
        ignoreCase = false
    end if
    if Len(prefix) > Len(m) then
        return false
    end if
    if ignoreCase then
        return equals_rStr_StrN_Z_k_(substring_rStr_I_I_k_(m, 0, Len(prefix)), prefix, true)
    end if
    return substring_rStr_I_I_k_(m, 0, Len(prefix)) = prefix
end function

function endsWith_rStr_Str_Z_k_(m as String, suffix as String, ignoreCase = false) as Boolean
    if ignoreCase = invalid then
        ignoreCase = false
    end if
    if Len(suffix) > Len(m) then
        return false
    end if
    if ignoreCase then
        return equals_rStr_StrN_Z_k_(substring_rStr_I_k_(m, Len(m) - Len(suffix)), suffix, true)
    end if
    return substring_rStr_I_k_(m, Len(m) - Len(suffix)) = suffix
end function

function equals_rStr_StrN_Z_k_(m as String, other as Dynamic, ignoreCase = false) as Boolean
    if ignoreCase = invalid then
        ignoreCase = false
    end if
    if other = invalid then
        return false
    end if
    if __kotlin_identityEquals(m, other) then
        return true
    end if
    if Len(m) <> Len(other) then
        return false
    end if
    if not ignoreCase then
        return m = other
    end if
    return lowercase_rStr_k_(m) = lowercase_rStr_k_(other)
end function

function contains_rStr_Str_Z_k_(m as String, other as String, ignoreCase = false) as Boolean
    if ignoreCase = invalid then
        ignoreCase = false
    end if
    return indexOf_rStr_Str_I_Z_k_(m, other, 0, ignoreCase) >= 0
end function

function concatToString_rCharArray_k_(m as Object) as String
    sb = StringBuilder_create_I_k_(m.get_size())
    indexedObject = m
    inductionVariable = 0
    last = indexedObject.get_size()
    while inductionVariable < last
        c = indexedObject.get_I_k_(inductionVariable)
        inductionVariable = (inductionVariable + 1)

        sb.append_C_k_(c)
    end while

    return sb.toString()
end function

function concatToString_rCharArray_I_I_k_(m as Object, startIndex = 0, endIndex = m.get_size()) as String
    if startIndex = invalid then
        startIndex = 0
    end if
    if endIndex = invalid then
        endIndex = m.get_size()
    end if
    if (startIndex < 0) or (endIndex > m.get_size()) then
        throw IndexOutOfBoundsException_create_StrN_k_((((("startIndex: " + __kotlin_numToStr_I_k_(startIndex)) + ", endIndex: ") + __kotlin_numToStr_I_k_(endIndex)) + ", size: ") + __kotlin_numToStr_I_k_(m.get_size()))
    end if
    if startIndex > endIndex then
        throw IllegalArgumentException_create_StrN_k_((("startIndex: " + __kotlin_numToStr_I_k_(startIndex)) + " > endIndex: ") + __kotlin_numToStr_I_k_(endIndex))
    end if
    sb = StringBuilder_create_I_k_(endIndex - startIndex)
    progression = until_rI_I_k_(startIndex, endIndex)
    inductionVariable = progression.get_first()
    last = progression.get_last()
    if inductionVariable <= last then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        sb.append_C_k_(m.get_I_k_(i))

        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            sb.append_C_k_(m.get_I_k_(i))
        end while

    end if

    return sb.toString()
end function
