function toLowerCase_rC_C_k_(m as Object) as Object
    return lowercaseChar_rC_C_k_(m)
end function

function lowercaseChar_rC_C_k_(m as Object) as Object
    return lowercase_rC_Str_k_(m)[0]
end function

function lowercase_rC_Str_k_(m as Object) as String
    return LCase(m.toString())
end function

function toUpperCase_rC_C_k_(m as Object) as Object
    return uppercaseChar_rC_C_k_(m)
end function

function uppercaseChar_rC_C_k_(m as Object) as Object
    uppercase = uppercase_rC_Str_k_(m)
    __when_tmp0 = invalid
    if uppercase.length > 1 then
        __when_tmp0 = m
    else if true then
        __when_tmp0 = uppercase[0]
    end if
    return __when_tmp0

end function

function uppercase_rC_Str_k_(m as Object) as String
    return UCase(m.toString())
end function

function titlecaseChar_rC_C_k_(m as Object) as Object
    return uppercaseChar_rC_C_k_(m)
end function

function get_category_rC_CharCategory_k_(m as Object) as Object
    __when_tmp1 = invalid
    if "A".rangeTo("Z").contains(m) then
        __when_tmp1 = CharCategory_UPPERCASE_LETTER
    else if "a".rangeTo("z").contains(m) then
        __when_tmp1 = CharCategory_LOWERCASE_LETTER
    else if "0".rangeTo("9").contains(m) then
        __when_tmp1 = CharCategory_DECIMAL_DIGIT_NUMBER
    else if (m = " ") or (m = chr(9)) then
        __when_tmp1 = CharCategory_SPACE_SEPARATOR
    else if "".rangeTo("").contains(m) or "".rangeTo("").contains(m) then
        __when_tmp1 = CharCategory_CONTROL
    else if true then
        __when_tmp1 = CharCategory_OTHER_SYMBOL
    end if
    return __when_tmp1

end function

function isDefined_rC_Z_k_(m as Object) as Boolean
    return true
end function

function isLetter_rC_Z_k_(m as Object) as Boolean
    return "a".rangeTo("z").contains(m) or "A".rangeTo("Z").contains(m)
end function

function isLetterOrDigit_rC_Z_k_(m as Object) as Boolean
    return isLetter_rC_Z_k_(m) or isDigit_rC_Z_k_(m)
end function

function isDigit_rC_Z_k_(m as Object) as Boolean
    return "0".rangeTo("9").contains(m)
end function

function isUpperCase_rC_Z_k_(m as Object) as Boolean
    return "A".rangeTo("Z").contains(m)
end function

function isLowerCase_rC_Z_k_(m as Object) as Boolean
    return "a".rangeTo("z").contains(m)
end function

function isTitleCase_rC_Z_k_(m as Object) as Boolean
    return false
end function

function isISOControl_rC_Z_k_(m as Object) as Boolean
    return ((m <= "") <= 0) or "".rangeTo("").contains(m)
end function

function isWhitespace_rC_Z_k_(m as Object) as Boolean
    return (((m = " ") or (m = chr(9))) or (m = chr(10))) or (m = chr(13))
end function

function isHighSurrogate_rC_Z_k_(m as Object) as Boolean
    return "?".rangeTo("?").contains(m)
end function

function isLowSurrogate_rC_Z_k_(m as Object) as Boolean
    return "?".rangeTo("?").contains(m)
end function

function isSurrogate_rC_Z_k_(m as Object) as Boolean
    return isHighSurrogate_rC_Z_k_(m) or isLowSurrogate_rC_Z_k_(m)
end function

function equals_rC_C_Z_Z_k_(m as Object, other as Object, ignoreCase = false) as Boolean
    if m = other then
        return true
    end if
    if ignoreCase.not() then
        return false
    end if
    return lowercaseChar_rC_C_k_(m) = lowercaseChar_rC_C_k_(other)
end function
