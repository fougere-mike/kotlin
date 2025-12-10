function toLowerCase_rC_k_(m as Object) as Object
    return lowercaseChar_rC_k_(m)
end function

function lowercaseChar_rC_k_(m as Object) as Object
    return lowercase_rC_k_(m).get_I_k_(0)
end function

function lowercase_rC_k_(m as Object) as String
    return LCase(m.toString())
end function

function toUpperCase_rC_k_(m as Object) as Object
    return uppercaseChar_rC_k_(m)
end function

function uppercaseChar_rC_k_(m as Object) as Object
    uppercase = uppercase_rC_k_(m)
    __when_tmp0 = invalid
    if Len(uppercase) > 1 then
        __when_tmp0 = m
    else if true then
        __when_tmp0 = uppercase.get_I_k_(0)
    end if
    return __when_tmp0

end function

function uppercase_rC_k_(m as Object) as String
    return UCase(m.toString())
end function

function titlecaseChar_rC_k_(m as Object) as Object
    return uppercaseChar_rC_k_(m)
end function

function get_category_rC_k_(m as Object) as Object
    __when_tmp1 = invalid
    if rangeTo_rC_C_k_("A", "Z").contains_C_k_(m) then
        __when_tmp1 = CharCategory_UPPERCASE_LETTER
    else if rangeTo_rC_C_k_("a", "z").contains_C_k_(m) then
        __when_tmp1 = CharCategory_LOWERCASE_LETTER
    else if rangeTo_rC_C_k_("0", "9").contains_C_k_(m) then
        __when_tmp1 = CharCategory_DECIMAL_DIGIT_NUMBER
    else if (m = " ") or (m = chr(9)) then
        __when_tmp1 = CharCategory_SPACE_SEPARATOR
    else if rangeTo_rC_C_k_("", "").contains_C_k_(m) or rangeTo_rC_C_k_("", "").contains_C_k_(m) then
        __when_tmp1 = CharCategory_CONTROL
    else if true then
        __when_tmp1 = CharCategory_OTHER_SYMBOL
    end if
    return __when_tmp1

end function

function isDefined_rC_k_(m as Object) as Boolean
    return true
end function

function isLetter_rC_k_(m as Object) as Boolean
    return rangeTo_rC_C_k_("a", "z").contains_C_k_(m) or rangeTo_rC_C_k_("A", "Z").contains_C_k_(m)
end function

function isLetterOrDigit_rC_k_(m as Object) as Boolean
    return isLetter_rC_k_(m) or isDigit_rC_k_(m)
end function

function isDigit_rC_k_(m as Object) as Boolean
    return rangeTo_rC_C_k_("0", "9").contains_C_k_(m)
end function

function isUpperCase_rC_k_(m as Object) as Boolean
    return rangeTo_rC_C_k_("A", "Z").contains_C_k_(m)
end function

function isLowerCase_rC_k_(m as Object) as Boolean
    return rangeTo_rC_C_k_("a", "z").contains_C_k_(m)
end function

function isTitleCase_rC_k_(m as Object) as Boolean
    return false
end function

function isISOControl_rC_k_(m as Object) as Boolean
    return ((m <= "") <= 0) or rangeTo_rC_C_k_("", "").contains_C_k_(m)
end function

function isWhitespace_rC_k_(m as Object) as Boolean
    return (((m = " ") or (m = chr(9))) or (m = chr(10))) or (m = chr(13))
end function

function isHighSurrogate_rC_k_(m as Object) as Boolean
    return rangeTo_rC_C_k_("?", "?").contains_C_k_(m)
end function

function isLowSurrogate_rC_k_(m as Object) as Boolean
    return rangeTo_rC_C_k_("?", "?").contains_C_k_(m)
end function

function isSurrogate_rC_k_(m as Object) as Boolean
    return isHighSurrogate_rC_k_(m) or isLowSurrogate_rC_k_(m)
end function

function equals_rC_C_Z_k_(m as Object, other as Object, ignoreCase = false) as Boolean
    if ignoreCase = invalid then
        ignoreCase = false
    end if
    if m = other then
        return true
    end if
    if not ignoreCase then
        return false
    end if
    return lowercaseChar_rC_k_(m) = lowercaseChar_rC_k_(other)
end function
