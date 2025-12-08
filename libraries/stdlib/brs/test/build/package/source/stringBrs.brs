function toUpperCase_rStr_Str_k_(m as String) as String
    return UCase(m)
end function

function uppercase_rStr_Str_k_(m as String) as String
    return UCase(m)
end function

function toLowerCase_rStr_Str_k_(m as String) as String
    return LCase(m)
end function

function lowercase_rStr_Str_k_(m as String) as String
    return LCase(m)
end function

function capitalize_rStr_Str_k_(m as String) as String
    __when_tmp0 = invalid
    if isNotEmpty_rStr_Z_k_(m) and isLowerCase_rC_Z_k_(m[0]) then
        __when_tmp0 = (uppercase_rC_Str_k_(m[0]) + substring_rStr_I_Str_k_(m, 1))
    else if true then
        __when_tmp0 = m
    end if
    return __when_tmp0

end function

function decapitalize_rStr_Str_k_(m as String) as String
    __when_tmp1 = invalid
    if isNotEmpty_rStr_Z_k_(m) and isUpperCase_rC_Z_k_(m[0]) then
        __when_tmp1 = (lowercase_rC_Str_k_(m[0]) + substring_rStr_I_Str_k_(m, 1))
    else if true then
        __when_tmp1 = m
    end if
    return __when_tmp1

end function

function contentEquals_rCharSequenceN_CharSequenceN_Z_k_(m as Dynamic, other as Dynamic) as Boolean
    if EQEQEQ_AnyN_AnyN_Z_k_(m, other) then
        return true
    end if
    if (m = invalid) or (other = invalid) then
        return false
    end if
    if m.length <> other.length then
        return false
    end if
    progression = until_rI_I_IntRange_k_(0, m.length)
    inductionVariable = progression.first
    last = progression.last
    if lessOrEqual_I_I_Z_k_(inductionVariable, last) then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if m[i] <> other[i] then
            return false
        end if


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if m[i] <> other[i] then
                return false
            end if

        end while

    end if

    return true
end function

function contentEquals_rCharSequenceN_CharSequenceN_Z_Z_k_(m as Dynamic, other as Dynamic, ignoreCase as Boolean) as Boolean
    if EQEQEQ_AnyN_AnyN_Z_k_(m, other) then
        return true
    end if
    if (m = invalid) or (other = invalid) then
        return false
    end if
    if m.length <> other.length then
        return false
    end if
    if ignoreCase.not() then
        progression = until_rI_I_IntRange_k_(0, m.length)
        inductionVariable = progression.first
        last = progression.last
        if lessOrEqual_I_I_Z_k_(inductionVariable, last) then
                        i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if m[i] <> other[i] then
                return false
            end if


            while i <> last
                i = inductionVariable
                inductionVariable = (inductionVariable + 1)

                if m[i] <> other[i] then
                    return false
                end if

            end while

        end if
    else if true then
        progression = until_rI_I_IntRange_k_(0, m.length)
        inductionVariable = progression.first
        last = progression.last
        if lessOrEqual_I_I_Z_k_(inductionVariable, last) then
                        i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if equals_rC_C_Z_Z_k_(m[i], other[i], true).not() then
                return false
            end if


            while i <> last
                i = inductionVariable
                inductionVariable = (inductionVariable + 1)

                if equals_rC_C_Z_Z_k_(m[i], other[i], true).not() then
                    return false
                end if

            end while

        end if
    end if
    return true
end function

function nativeIndexOf_rStr_Str_I_I_k_(m as String, str as String, fromIndex as Integer) as Integer
    return Instr(fromIndex, m, str)
end function

function nativeLastIndexOf_rStr_Str_I_I_k_(m as String, str as String, fromIndex as Integer) as Integer
    lastIndex = -1
    searchIndex = 0
    while searchIndex <= fromIndex
        found = Instr(searchIndex, m, str)
        if (found < 0) or (found > fromIndex) then
            exit while
        end if
        lastIndex = found
        searchIndex = (found + 1)
    end while
    return lastIndex
end function

function substring_rStr_I_Str_k_(m as String, startIndex as Integer) as String
    return substring_rStr_I_I_Str_k_(m, startIndex, m.length)
end function

function substring_rStr_I_I_Str_k_(m as String, startIndex as Integer, endIndex as Integer) as String
    if startIndex < 0 then
        throw IndexOutOfBoundsException_create_StrN_IndexOutOfBoundsException_k_("startIndex: " + startIndex)
    end if
    if endIndex > m.length then
        throw IndexOutOfBoundsException_create_StrN_IndexOutOfBoundsException_k_((("endIndex: " + endIndex) + ", length: ") + m.length)
    end if
    if startIndex > endIndex then
        throw IllegalArgumentException_create_StrN_IllegalArgumentException_k_((("startIndex: " + startIndex) + " > endIndex: ") + endIndex)
    end if
    if startIndex = endIndex then
        return ""
    end if
    return Mid(m, startIndex + 1, endIndex - startIndex)
end function

function indexOf_rStr_Str_I_Z_I_k_(m as String, string_ as String, startIndex = 0, ignoreCase = false) as Integer
    if ignoreCase then
        return indexOf_rStr_Str_I_Z_I_k_(lowercase_rStr_Str_k_(m), lowercase_rStr_Str_k_(string_), startIndex, false)
    end if
    if startIndex < 0 then
        return indexOf_rStr_Str_I_Z_I_k_(m, string_, 0)
    end if
    if startIndex >= m.length then
        __when_tmp2 = invalid
        if isEmpty_rStr_Z_k_(string_) then
            __when_tmp2 = m.length
        else if true then
            __when_tmp2 = -1
        end if
        return __when_tmp2

    end if
    return nativeIndexOf_rStr_Str_I_I_k_(m, string_, startIndex)
end function

function lastIndexOf_rStr_Str_I_Z_I_k_(m as String, string_ as String, startIndex = get_lastIndex_rStr_I_k_(m), ignoreCase = false) as Integer
    if ignoreCase then
        return lastIndexOf_rStr_Str_I_Z_I_k_(lowercase_rStr_Str_k_(m), lowercase_rStr_Str_k_(string_), startIndex, false)
    end if
    if startIndex < 0 then
        return -1
    end if
    return nativeLastIndexOf_rStr_Str_I_I_k_(m, string_, coerceAtMost_rI_I_I_k_(startIndex, m.length))
end function

function isEmpty_rStr_Z_k_(m as String) as Boolean
    return m.length = 0
end function

function isNotEmpty_rStr_Z_k_(m as String) as Boolean
    return m.length > 0
end function

function isBlank_rStr_Z_k_(m as String) as Boolean
    if isEmpty_rStr_Z_k_(m) then
        return true
    end if
    progression = until_rI_I_IntRange_k_(0, m.length)
    inductionVariable = progression.first
    last = progression.last
    if lessOrEqual_I_I_Z_k_(inductionVariable, last) then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if isWhitespace_rC_Z_k_(m[i]).not() then
            return false
        end if


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if isWhitespace_rC_Z_k_(m[i]).not() then
                return false
            end if

        end while

    end if

    return true
end function

function isNotBlank_rStr_Z_k_(m as String) as Boolean
    return isBlank_rStr_Z_k_(m).not()
end function

function get_lastIndex_rStr_I_k_(m as String) as Integer
    return m.length - 1
end function

function getOrElse_rStr_I_Function1IC_C_k_(m as String, index as Integer, defaultValue as Function) as Object
    __when_tmp3 = invalid
    if (index >= 0) and (index < m.length) then
        __when_tmp3 = m[index]
    else if true then
        __when_tmp3 = defaultValue.invoke(index)
    end if
    return __when_tmp3

end function

function padStart_rStr_I_C_Str_k_(m as String, length as Integer, padChar = " ") as String
    if length <= 0 then
        throw IllegalArgumentException_create_StrN_IllegalArgumentException_k_(("Desired length " + length) + " is less than zero.")
    end if
    if m.length >= length then
        return m
    end if
    sb = StringBuilder_create_I_StringBuilder_k_(length)
    i = 0
    times = length - m.length
    while i < times
        sb.append(padChar)
        i = (i + 1)
    end while
    sb.append(m)
    return sb.toString()
end function

function padEnd_rStr_I_C_Str_k_(m as String, length as Integer, padChar = " ") as String
    if length <= 0 then
        throw IllegalArgumentException_create_StrN_IllegalArgumentException_k_(("Desired length " + length) + " is less than zero.")
    end if
    if m.length >= length then
        return m
    end if
    sb = StringBuilder_create_I_StringBuilder_k_(length)
    sb.append(m)
    i = 0
    times = length - m.length
    while i < times
        sb.append(padChar)
        i = (i + 1)
    end while
    return sb.toString()
end function

function repeat_rStr_I_Str_k_(m as String, n as Integer) as String
    if n < 0 then
        throw IllegalArgumentException_create_StrN_IllegalArgumentException_k_(("Count 'n' must be non-negative, but was " + n) + ".")
    end if
    if (n = 0) or isEmpty_rStr_Z_k_(m) then
        return ""
    end if
    if n = 1 then
        return m
    end if
    sb = StringBuilder_create_I_StringBuilder_k_(m.length * n)
    i = 0
    while i < n
        sb.append(m)
        i = (i + 1)
    end while
    return sb.toString()
end function

function startsWith_rStr_Str_Z_Z_k_(m as String, prefix as String, ignoreCase = false) as Boolean
    if prefix.length > m.length then
        return false
    end if
    if ignoreCase then
        return equals_rStr_StrN_Z_Z_k_(substring_rStr_I_I_Str_k_(m, 0, prefix.length), prefix, true)
    end if
    return substring_rStr_I_I_Str_k_(m, 0, prefix.length) = prefix
end function

function endsWith_rStr_Str_Z_Z_k_(m as String, suffix as String, ignoreCase = false) as Boolean
    if suffix.length > m.length then
        return false
    end if
    if ignoreCase then
        return equals_rStr_StrN_Z_Z_k_(substring_rStr_I_Str_k_(m, m.length - suffix.length), suffix, true)
    end if
    return substring_rStr_I_Str_k_(m, m.length - suffix.length) = suffix
end function

function equals_rStr_StrN_Z_Z_k_(m as String, other as Dynamic, ignoreCase = false) as Boolean
    if other = invalid then
        return false
    end if
    if EQEQEQ_AnyN_AnyN_Z_k_(m, other) then
        return true
    end if
    if m.length <> other.length then
        return false
    end if
    if ignoreCase.not() then
        return m = other
    end if
    return lowercase_rStr_Str_k_(m) = lowercase_rStr_Str_k_(other)
end function

function contains_rStr_Str_Z_Z_k_(m as String, other as String, ignoreCase = false) as Boolean
    return indexOf_rStr_Str_I_Z_I_k_(m, other, 0, ignoreCase) >= 0
end function

function concatToString_rCharArray_Str_k_(m as Object) as String
    sb = StringBuilder_create_I_StringBuilder_k_(m.size)
    indexedObject = m
    inductionVariable = 0
    last = indexedObject.size
    while less_I_I_Z_k_(inductionVariable, last)
        c = indexedObject.get(inductionVariable)
        inductionVariable = (inductionVariable + 1)

        sb.append(c)
    end while

    return sb.toString()
end function

function concatToString_rCharArray_I_I_Str_k_(m as Object, startIndex = 0, endIndex = m.size) as String
    if (startIndex < 0) or (endIndex > m.size) then
        throw IndexOutOfBoundsException_create_StrN_IndexOutOfBoundsException_k_((((("startIndex: " + startIndex) + ", endIndex: ") + endIndex) + ", size: ") + m.size)
    end if
    if startIndex > endIndex then
        throw IllegalArgumentException_create_StrN_IllegalArgumentException_k_((("startIndex: " + startIndex) + " > endIndex: ") + endIndex)
    end if
    sb = StringBuilder_create_I_StringBuilder_k_(endIndex - startIndex)
    progression = until_rI_I_IntRange_k_(startIndex, endIndex)
    inductionVariable = progression.first
    last = progression.last
    if lessOrEqual_I_I_Z_k_(inductionVariable, last) then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        sb.append(m[i])

        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            sb.append(m[i])
        end while

    end if

    return sb.toString()
end function
