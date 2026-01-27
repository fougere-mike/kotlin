function toLowerCase_rC_k_(m as Object) as Object
    tmp0 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0
        tmp_ret_0 = Mid(lowercase_rC_k_(this), 0 + 1, 1)
        exit while
    end while
    return tmp_ret_0

end function

function lowercaseChar_rC_k_(m as Object) as Object
    return Mid(lowercase_rC_k_(m), 0 + 1, 1)
end function

function lowercase_rC_k_(m as Object) as String
    return LCase(m)
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
        __when_tmp0 = Mid(uppercase, 0 + 1, 1)
    end if
    return __when_tmp0

end function

function uppercase_rC_k_(m as Object) as String
    return UCase(m)
end function

function titlecaseChar_rC_k_(m as Object) as Object
    return uppercaseChar_rC_k_(m)
end function

function __get_category_rC_k_(m as Object) as Object
    __when_tmp1 = invalid
    if rangeTo_rC_C_k_("A", "Z").contains_Any_k_(m) then
        __when_tmp1 = [CharCategory_initEntries(), m.CharCategory_UPPERCASE_LETTER][1]
    else if rangeTo_rC_C_k_("a", "z").contains_Any_k_(m) then
        __when_tmp1 = [CharCategory_initEntries(), m.CharCategory_LOWERCASE_LETTER][1]
    else if rangeTo_rC_C_k_("0", "9").contains_Any_k_(m) then
        __when_tmp1 = [CharCategory_initEntries(), m.CharCategory_DECIMAL_DIGIT_NUMBER][1]
    else if (m = " ") or (m = chr(9)) then
        __when_tmp1 = [CharCategory_initEntries(), m.CharCategory_SPACE_SEPARATOR][1]
    else if rangeTo_rC_C_k_("", "").contains_Any_k_(m) or rangeTo_rC_C_k_("", "").contains_Any_k_(m) then
        __when_tmp1 = [CharCategory_initEntries(), m.CharCategory_CONTROL][1]
    else if true then
        __when_tmp1 = [CharCategory_initEntries(), m.CharCategory_OTHER_SYMBOL][1]
    end if
    return __when_tmp1

end function

function isDefined_rC_k_(m as Object) as Boolean
    return true
end function

function isLetter_rC_k_(m as Object) as Boolean
    return rangeTo_rC_C_k_("a", "z").contains_Any_k_(m) or rangeTo_rC_C_k_("A", "Z").contains_Any_k_(m)
end function

function isLetterOrDigit_rC_k_(m as Object) as Boolean
    return isLetter_rC_k_(m) or isDigit_rC_k_(m)
end function

function isDigit_rC_k_(m as Object) as Boolean
    return rangeTo_rC_C_k_("0", "9").contains_Any_k_(m)
end function

function isUpperCase_rC_k_(m as Object) as Boolean
    return rangeTo_rC_C_k_("A", "Z").contains_Any_k_(m)
end function

function isLowerCase_rC_k_(m as Object) as Boolean
    return rangeTo_rC_C_k_("a", "z").contains_Any_k_(m)
end function

function isTitleCase_rC_k_(m as Object) as Boolean
    return false
end function

function isISOControl_rC_k_(m as Object) as Boolean
    return (__kotlin_stringCompare(m, "") <= 0) or rangeTo_rC_C_k_("", "").contains_Any_k_(m)
end function

function isWhitespace_rC_k_(m as Object) as Boolean
    return (((m = " ") or (m = chr(9))) or (m = chr(10))) or (m = chr(13))
end function

function isHighSurrogate_rC_k_(m as Object) as Boolean
    return rangeTo_rC_C_k_("?", "?").contains_Any_k_(m)
end function

function isLowSurrogate_rC_k_(m as Object) as Boolean
    return rangeTo_rC_C_k_("?", "?").contains_Any_k_(m)
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
    tmp0 = m
    tmp_ret_0 = invalid

    while true
        this = tmp0
        tmp_ret_0 = Mid(lowercase_rC_k_(this), 0 + 1, 1)
        exit while
    end while
    tmp0_1 = other
    tmp_ret_1 = invalid

    while true
        this = tmp0_1
        tmp_ret_1 = Mid(lowercase_rC_k_(this), 0 + 1, 1)
        exit while
    end while
    return tmp_ret_0 = tmp_ret_1

end function
