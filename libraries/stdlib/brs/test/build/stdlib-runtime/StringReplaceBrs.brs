function replace_rStr_Str_Str_Z_Str_k_(m as String, oldValue as String, newValue as String, ignoreCase = false) as String
    if isEmpty_rStr_Z_k_(oldValue) then
        result = StringBuilder_create_StringBuilder_k_()
        result.append_StrN_StringBuilder_k_(newValue)
        progression = until_rI_I_IntRange_k_(0, m.get_length())
        inductionVariable = progression.get_first()
        last = progression.get_last()
        if inductionVariable <= last then
                        i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            result.append_C_StringBuilder_k_(m.get_I_C_k_(i))
            result.append_StrN_StringBuilder_k_(newValue)


            while i <> last
                i = inductionVariable
                inductionVariable = (inductionVariable + 1)

                result.append_C_StringBuilder_k_(m.get_I_C_k_(i))
                result.append_StrN_StringBuilder_k_(newValue)

            end while

        end if
        return result.toString()
    end if
    currentIndex = 0
    result = StringBuilder_create_StringBuilder_k_()
    while currentIndex < m.get_length()
        nextIndex = indexOf_rStr_Str_I_Z_I_k_(m, oldValue, currentIndex, ignoreCase)
        if nextIndex < 0 then
            result.append_StrN_StringBuilder_k_(substring_rStr_I_Str_k_(m, currentIndex))
            exit while
        end if
        result.append_StrN_StringBuilder_k_(substring_rStr_I_I_Str_k_(m, currentIndex, nextIndex))
        result.append_StrN_StringBuilder_k_(newValue)
        currentIndex = (nextIndex + oldValue.get_length())
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
    return (substring_rStr_I_I_Str_k_(m, 0, index) + newValue) + substring_rStr_I_Str_k_(m, index + oldValue.get_length())
end function

function replaceFirst_rStr_C_C_Z_Str_k_(m as String, oldChar as Object, newChar as Object, ignoreCase = false) as String
    return replaceFirst_rStr_Str_Str_Z_Str_k_(m, oldChar.toString(), newChar.toString(), ignoreCase)
end function

function replaceRange_rStr_IntRange_CharSequence_Str_k_(m as String, range as Object, replacement as Object) as String
    return replaceRange_rStr_I_I_CharSequence_Str_k_(m, range.get_start(), range.get_endInclusive() + 1, replacement)
end function

function replaceRange_rStr_I_I_CharSequence_Str_k_(m as String, startIndex as Integer, endIndex as Integer, replacement as Object) as String
    if endIndex < startIndex then
        throw IndexOutOfBoundsException_create_StrN_IndexOutOfBoundsException_k_(((("End index (" + endIndex) + ") is less than start index (") + startIndex) + ")")
    end if
    if startIndex < 0 then
        throw IndexOutOfBoundsException_create_StrN_IndexOutOfBoundsException_k_(("Start index (" + startIndex) + ") is negative")
    end if
    if endIndex > m.get_length() then
        throw IndexOutOfBoundsException_create_StrN_IndexOutOfBoundsException_k_(((("End index (" + endIndex) + ") is greater than length (") + m.get_length()) + ")")
    end if
    return (substring_rStr_I_I_Str_k_(m, 0, startIndex) + ((function(Str, replacement)
        if replacement = invalid then return "null" else return (function(Str, replacement)
            if (Type(replacement) = "String") or (Type(replacement) = "roString") then return replacement else return (function(Str, replacement)
                if ((((((Type(replacement) = "Integer") or (Type(replacement) = "LongInteger")) or (Type(replacement) = "Float")) or (Type(replacement) = "Double")) or (Type(replacement) = "roInt")) or (Type(replacement) = "roFloat")) or (Type(replacement) = "roDouble") then return Str(replacement) else return (function(replacement)
                    if (Type(replacement) = "Boolean") or (Type(replacement) = "roBoolean") then return (function(replacement)
                        if replacement then return "true" else return "false"
                    end function)(replacement) else return replacement.toString()
                end function)(replacement)
            end function)(Str, replacement)
        end function)(Str, replacement)
    end function)(Str, replacement))) + substring_rStr_I_Str_k_(m, endIndex)
end function

function replaceRange_rCharSequence_IntRange_CharSequence_Str_k_(m as Object, range as Object, replacement as Object) as String
    return replaceRange_rStr_IntRange_CharSequence_Str_k_((function(Str)
        if m = invalid then return "null" else return (function(Str)
            if (Type(m) = "String") or (Type(m) = "roString") then return m else return (function(Str)
                if ((((((Type(m) = "Integer") or (Type(m) = "LongInteger")) or (Type(m) = "Float")) or (Type(m) = "Double")) or (Type(m) = "roInt")) or (Type(m) = "roFloat")) or (Type(m) = "roDouble") then return Str(m) else return (function()
                    if (Type(m) = "Boolean") or (Type(m) = "roBoolean") then return (function()
                        if m then return "true" else return "false"
                    end function)() else return m.toString()
                end function)()
            end function)(Str)
        end function)(Str)
    end function)(Str), range, replacement)
end function

function replaceRange_rCharSequence_I_I_CharSequence_Str_k_(m as Object, startIndex as Integer, endIndex as Integer, replacement as Object) as String
    return replaceRange_rStr_I_I_CharSequence_Str_k_((function(Str)
        if m = invalid then return "null" else return (function(Str)
            if (Type(m) = "String") or (Type(m) = "roString") then return m else return (function(Str)
                if ((((((Type(m) = "Integer") or (Type(m) = "LongInteger")) or (Type(m) = "Float")) or (Type(m) = "Double")) or (Type(m) = "roInt")) or (Type(m) = "roFloat")) or (Type(m) = "roDouble") then return Str(m) else return (function()
                    if (Type(m) = "Boolean") or (Type(m) = "roBoolean") then return (function()
                        if m then return "true" else return "false"
                    end function)() else return m.toString()
                end function)()
            end function)(Str)
        end function)(Str)
    end function)(Str), startIndex, endIndex, replacement)
end function
