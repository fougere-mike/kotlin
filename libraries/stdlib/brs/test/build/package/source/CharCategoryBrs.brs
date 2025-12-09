function CharCategory_create_I_Str_CharCategory_k_(__name as String, __ordinal as Integer, value as Integer, code as String) as Object
    this = {}
    this.__type = "CharCategory"
    this.name = __name
    this.ordinal = __ordinal
    this.__proto = ["CharCategory", "Comparable"]
    this.__id = __kotlin_nextObjectId()
    this.value = value
    this.code = code
    this.contains_C_Z_k_ = CharCategory_contains_C_Z_k_
    this.values_Arr_k_ = CharCategory_values_Arr_k_
    this.valueOf_Str_CharCategory_k_ = CharCategory_valueOf_Str_CharCategory_k_
    this.get_value = CharCategory_get_value_I_k_
    this.get_code = CharCategory_get_code_Str_k_
    this.get_entries = CharCategory_get_entries_EnumEntries_k_
    return this
end function

sub CharCategory_initEntries()
    if m.CharCategory_entriesInitialized then
        return
    end if
    m.CharCategory_entriesInitialized = true
    m.CharCategory_UNASSIGNED = CharCategory_create_I_Str_CharCategory_k_("UNASSIGNED", 0, 0, "Cn")
    m.CharCategory_UPPERCASE_LETTER = CharCategory_create_I_Str_CharCategory_k_("UPPERCASE_LETTER", 1, 1, "Lu")
    m.CharCategory_LOWERCASE_LETTER = CharCategory_create_I_Str_CharCategory_k_("LOWERCASE_LETTER", 2, 2, "Ll")
    m.CharCategory_TITLECASE_LETTER = CharCategory_create_I_Str_CharCategory_k_("TITLECASE_LETTER", 3, 3, "Lt")
    m.CharCategory_MODIFIER_LETTER = CharCategory_create_I_Str_CharCategory_k_("MODIFIER_LETTER", 4, 4, "Lm")
    m.CharCategory_OTHER_LETTER = CharCategory_create_I_Str_CharCategory_k_("OTHER_LETTER", 5, 5, "Lo")
    m.CharCategory_NON_SPACING_MARK = CharCategory_create_I_Str_CharCategory_k_("NON_SPACING_MARK", 6, 6, "Mn")
    m.CharCategory_ENCLOSING_MARK = CharCategory_create_I_Str_CharCategory_k_("ENCLOSING_MARK", 7, 7, "Me")
    m.CharCategory_COMBINING_SPACING_MARK = CharCategory_create_I_Str_CharCategory_k_("COMBINING_SPACING_MARK", 8, 8, "Mc")
    m.CharCategory_DECIMAL_DIGIT_NUMBER = CharCategory_create_I_Str_CharCategory_k_("DECIMAL_DIGIT_NUMBER", 9, 9, "Nd")
    m.CharCategory_LETTER_NUMBER = CharCategory_create_I_Str_CharCategory_k_("LETTER_NUMBER", 10, 10, "Nl")
    m.CharCategory_OTHER_NUMBER = CharCategory_create_I_Str_CharCategory_k_("OTHER_NUMBER", 11, 11, "No")
    m.CharCategory_SPACE_SEPARATOR = CharCategory_create_I_Str_CharCategory_k_("SPACE_SEPARATOR", 12, 12, "Zs")
    m.CharCategory_LINE_SEPARATOR = CharCategory_create_I_Str_CharCategory_k_("LINE_SEPARATOR", 13, 13, "Zl")
    m.CharCategory_PARAGRAPH_SEPARATOR = CharCategory_create_I_Str_CharCategory_k_("PARAGRAPH_SEPARATOR", 14, 14, "Zp")
    m.CharCategory_CONTROL = CharCategory_create_I_Str_CharCategory_k_("CONTROL", 15, 15, "Cc")
    m.CharCategory_FORMAT = CharCategory_create_I_Str_CharCategory_k_("FORMAT", 16, 16, "Cf")
    m.CharCategory_PRIVATE_USE = CharCategory_create_I_Str_CharCategory_k_("PRIVATE_USE", 17, 18, "Co")
    m.CharCategory_SURROGATE = CharCategory_create_I_Str_CharCategory_k_("SURROGATE", 18, 19, "Cs")
    m.CharCategory_DASH_PUNCTUATION = CharCategory_create_I_Str_CharCategory_k_("DASH_PUNCTUATION", 19, 20, "Pd")
    m.CharCategory_START_PUNCTUATION = CharCategory_create_I_Str_CharCategory_k_("START_PUNCTUATION", 20, 21, "Ps")
    m.CharCategory_END_PUNCTUATION = CharCategory_create_I_Str_CharCategory_k_("END_PUNCTUATION", 21, 22, "Pe")
    m.CharCategory_CONNECTOR_PUNCTUATION = CharCategory_create_I_Str_CharCategory_k_("CONNECTOR_PUNCTUATION", 22, 23, "Pc")
    m.CharCategory_OTHER_PUNCTUATION = CharCategory_create_I_Str_CharCategory_k_("OTHER_PUNCTUATION", 23, 24, "Po")
    m.CharCategory_MATH_SYMBOL = CharCategory_create_I_Str_CharCategory_k_("MATH_SYMBOL", 24, 25, "Sm")
    m.CharCategory_CURRENCY_SYMBOL = CharCategory_create_I_Str_CharCategory_k_("CURRENCY_SYMBOL", 25, 26, "Sc")
    m.CharCategory_MODIFIER_SYMBOL = CharCategory_create_I_Str_CharCategory_k_("MODIFIER_SYMBOL", 26, 27, "Sk")
    m.CharCategory_OTHER_SYMBOL = CharCategory_create_I_Str_CharCategory_k_("OTHER_SYMBOL", 27, 28, "So")
    m.CharCategory_INITIAL_QUOTE_PUNCTUATION = CharCategory_create_I_Str_CharCategory_k_("INITIAL_QUOTE_PUNCTUATION", 28, 29, "Pi")
    m.CharCategory_FINAL_QUOTE_PUNCTUATION = CharCategory_create_I_Str_CharCategory_k_("FINAL_QUOTE_PUNCTUATION", 29, 30, "Pf")
end sub

function CharCategory_values() as Object
    CharCategory_initEntries()
    return [m.CharCategory_UNASSIGNED, m.CharCategory_UPPERCASE_LETTER, m.CharCategory_LOWERCASE_LETTER, m.CharCategory_TITLECASE_LETTER, m.CharCategory_MODIFIER_LETTER, m.CharCategory_OTHER_LETTER, m.CharCategory_NON_SPACING_MARK, m.CharCategory_ENCLOSING_MARK, m.CharCategory_COMBINING_SPACING_MARK, m.CharCategory_DECIMAL_DIGIT_NUMBER, m.CharCategory_LETTER_NUMBER, m.CharCategory_OTHER_NUMBER, m.CharCategory_SPACE_SEPARATOR, m.CharCategory_LINE_SEPARATOR, m.CharCategory_PARAGRAPH_SEPARATOR, m.CharCategory_CONTROL, m.CharCategory_FORMAT, m.CharCategory_PRIVATE_USE, m.CharCategory_SURROGATE, m.CharCategory_DASH_PUNCTUATION, m.CharCategory_START_PUNCTUATION, m.CharCategory_END_PUNCTUATION, m.CharCategory_CONNECTOR_PUNCTUATION, m.CharCategory_OTHER_PUNCTUATION, m.CharCategory_MATH_SYMBOL, m.CharCategory_CURRENCY_SYMBOL, m.CharCategory_MODIFIER_SYMBOL, m.CharCategory_OTHER_SYMBOL, m.CharCategory_INITIAL_QUOTE_PUNCTUATION, m.CharCategory_FINAL_QUOTE_PUNCTUATION]
end function

function CharCategory_valueOf(name as String) as Object
    CharCategory_initEntries()
    if name = "UNASSIGNED" then
        return m.CharCategory_UNASSIGNED
    else if name = "UPPERCASE_LETTER" then
        return m.CharCategory_UPPERCASE_LETTER
    else if name = "LOWERCASE_LETTER" then
        return m.CharCategory_LOWERCASE_LETTER
    else if name = "TITLECASE_LETTER" then
        return m.CharCategory_TITLECASE_LETTER
    else if name = "MODIFIER_LETTER" then
        return m.CharCategory_MODIFIER_LETTER
    else if name = "OTHER_LETTER" then
        return m.CharCategory_OTHER_LETTER
    else if name = "NON_SPACING_MARK" then
        return m.CharCategory_NON_SPACING_MARK
    else if name = "ENCLOSING_MARK" then
        return m.CharCategory_ENCLOSING_MARK
    else if name = "COMBINING_SPACING_MARK" then
        return m.CharCategory_COMBINING_SPACING_MARK
    else if name = "DECIMAL_DIGIT_NUMBER" then
        return m.CharCategory_DECIMAL_DIGIT_NUMBER
    else if name = "LETTER_NUMBER" then
        return m.CharCategory_LETTER_NUMBER
    else if name = "OTHER_NUMBER" then
        return m.CharCategory_OTHER_NUMBER
    else if name = "SPACE_SEPARATOR" then
        return m.CharCategory_SPACE_SEPARATOR
    else if name = "LINE_SEPARATOR" then
        return m.CharCategory_LINE_SEPARATOR
    else if name = "PARAGRAPH_SEPARATOR" then
        return m.CharCategory_PARAGRAPH_SEPARATOR
    else if name = "CONTROL" then
        return m.CharCategory_CONTROL
    else if name = "FORMAT" then
        return m.CharCategory_FORMAT
    else if name = "PRIVATE_USE" then
        return m.CharCategory_PRIVATE_USE
    else if name = "SURROGATE" then
        return m.CharCategory_SURROGATE
    else if name = "DASH_PUNCTUATION" then
        return m.CharCategory_DASH_PUNCTUATION
    else if name = "START_PUNCTUATION" then
        return m.CharCategory_START_PUNCTUATION
    else if name = "END_PUNCTUATION" then
        return m.CharCategory_END_PUNCTUATION
    else if name = "CONNECTOR_PUNCTUATION" then
        return m.CharCategory_CONNECTOR_PUNCTUATION
    else if name = "OTHER_PUNCTUATION" then
        return m.CharCategory_OTHER_PUNCTUATION
    else if name = "MATH_SYMBOL" then
        return m.CharCategory_MATH_SYMBOL
    else if name = "CURRENCY_SYMBOL" then
        return m.CharCategory_CURRENCY_SYMBOL
    else if name = "MODIFIER_SYMBOL" then
        return m.CharCategory_MODIFIER_SYMBOL
    else if name = "OTHER_SYMBOL" then
        return m.CharCategory_OTHER_SYMBOL
    else if name = "INITIAL_QUOTE_PUNCTUATION" then
        return m.CharCategory_INITIAL_QUOTE_PUNCTUATION
    else if name = "FINAL_QUOTE_PUNCTUATION" then
        return m.CharCategory_FINAL_QUOTE_PUNCTUATION
    else
        return invalid
    end if
end function

function CharCategory_contains_C_Z_k_(char as Object) as Boolean
    return get_category_rC_CharCategory_k_(char) = m
end function
