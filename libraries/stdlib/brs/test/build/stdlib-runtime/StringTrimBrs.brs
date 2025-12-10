function trim_rCharSequence_k_(m as Object) as Object
    return trim_rCharSequence_Function1CZ_k_(m, {invoke: function(it as Object) as Boolean
        return isWhitespace_rC_k_(it)
    end function})
end function

function trim_rStr_k_(m as String) as String
    return toString_AnyN_k_(trim_rCharSequence_k_(m))
end function

function trimStart_rCharSequence_k_(m as Object) as Object
    return trimStart_rCharSequence_Function1CZ_k_(m, {invoke: function(it as Object) as Boolean
        return isWhitespace_rC_k_(it)
    end function})
end function

function trimStart_rStr_k_(m as String) as String
    return toString_AnyN_k_(trimStart_rCharSequence_k_(m))
end function

function trimEnd_rCharSequence_k_(m as Object) as Object
    return trimEnd_rCharSequence_Function1CZ_k_(m, {invoke: function(it as Object) as Boolean
        return isWhitespace_rC_k_(it)
    end function})
end function

function trimEnd_rStr_k_(m as String) as String
    return toString_AnyN_k_(trimEnd_rCharSequence_k_(m))
end function

function trim_rCharSequence_Function1CZ_k_(m as Object, predicate as Object) as Object
    startIndex = 0
    endIndex = Len(m) - 1
    while (startIndex <= endIndex) and predicate.invoke(m.get_I_k_(startIndex))
        startIndex = (startIndex + 1)
    end while
    while (endIndex >= startIndex) and predicate.invoke(m.get_I_k_(endIndex))
        endIndex = (endIndex - 1)
    end while
    return m.subSequence_I_I_k_(startIndex, endIndex + 1)
end function

function trim_rStr_Function1CZ_k_(m as String, predicate as Object) as String
    return toString_AnyN_k_(trim_rCharSequence_Function1CZ_k_(m, predicate))
end function

function trimStart_rCharSequence_Function1CZ_k_(m as Object, predicate as Object) as Object
    startIndex = 0
    while (startIndex < Len(m)) and predicate.invoke(m.get_I_k_(startIndex))
        startIndex = (startIndex + 1)
    end while
    return m.subSequence_I_I_k_(startIndex, Len(m))
end function

function trimStart_rStr_Function1CZ_k_(m as String, predicate as Object) as String
    return toString_AnyN_k_(trimStart_rCharSequence_Function1CZ_k_(m, predicate))
end function

function trimEnd_rCharSequence_Function1CZ_k_(m as Object, predicate as Object) as Object
    endIndex = Len(m) - 1
    while (endIndex >= 0) and predicate.invoke(m.get_I_k_(endIndex))
        endIndex = (endIndex - 1)
    end while
    return m.subSequence_I_I_k_(0, endIndex + 1)
end function

function trimEnd_rStr_Function1CZ_k_(m as String, predicate as Object) as String
    return toString_AnyN_k_(trimEnd_rCharSequence_Function1CZ_k_(m, predicate))
end function

function trim_rCharSequence_CharArray_k_(m as Object, chars as Object) as Object
    return trim_rCharSequence_Function1CZ_k_(m, {chars: chars, invoke: function(char as Object) as Boolean
        found = false
        i = 0
        while i < m.chars.get_size()
            if char = m.chars.get_I_k_(i) then
                found = true
                exit while
            end if
            i = (i + 1)
        end while
        return found
    end function})
end function

function trim_rStr_CharArray_k_(m as String, chars as Object) as String
    return toString_AnyN_k_(trim_rCharSequence_CharArray_k_(m, chars))
end function

function trimStart_rCharSequence_CharArray_k_(m as Object, chars as Object) as Object
    return trimStart_rCharSequence_Function1CZ_k_(m, {chars: chars, invoke: function(char as Object) as Boolean
        found = false
        i = 0
        while i < m.chars.get_size()
            if char = m.chars.get_I_k_(i) then
                found = true
                exit while
            end if
            i = (i + 1)
        end while
        return found
    end function})
end function

function trimStart_rStr_CharArray_k_(m as String, chars as Object) as String
    return toString_AnyN_k_(trimStart_rCharSequence_CharArray_k_(m, chars))
end function

function trimEnd_rCharSequence_CharArray_k_(m as Object, chars as Object) as Object
    return trimEnd_rCharSequence_Function1CZ_k_(m, {chars: chars, invoke: function(char as Object) as Boolean
        found = false
        i = 0
        while i < m.chars.get_size()
            if char = m.chars.get_I_k_(i) then
                found = true
                exit while
            end if
            i = (i + 1)
        end while
        return found
    end function})
end function

function trimEnd_rStr_CharArray_k_(m as String, chars as Object) as String
    return toString_AnyN_k_(trimEnd_rCharSequence_CharArray_k_(m, chars))
end function

function trimIndent_rStr_k_(m as String) as String
    return replaceIndent_rStr_Str_k_(m, "")
end function

function replaceIndent_rStr_Str_k_(m as String, newIndent = "") as String
    lines = lines_rCharSequence_k_(m)
    minIndent = 2147483647
    lineIndex = 0
    while lineIndex < lines.get_size()
        line = lines.get_I_k_(lineIndex)
        indent = 0
        while (indent < Len(line)) and isWhitespace_rC_k_(line.get_I_k_(indent))
            indent = (indent + 1)
        end while
        if (indent < Len(line)) and (indent < minIndent) then
            minIndent = indent
        end if
        lineIndex = (lineIndex + 1)
    end while
    if minIndent = 2147483647 then
        minIndent = 0
    end if
    result = StringBuilder_create_k_()
    firstLine = true
    i = 0
    while i < lines.get_size()
        line = lines.get_I_k_(i)
        if (i = 0) and isBlank_rStr_k_(line) then
            i = (i + 1)
            ' continue not supported on target Roku OS
        end if
        if (i = (lines.get_size() - 1)) and isBlank_rStr_k_(line) then
            i = (i + 1)
            ' continue not supported on target Roku OS
        end if
        if not firstLine then
            result.append_C_k_(chr(10))
        end if
        firstLine = false
        if isNotBlank_rStr_k_(line) then
            result.append_StrN_k_(newIndent)
            __when_tmp0 = invalid
            if minIndent < Len(line) then
                __when_tmp0 = minIndent
            else if true then
                __when_tmp0 = Len(line)
            end if
            substringStart = __when_tmp0
            result.append_StrN_k_(substring_rStr_I_k_(line, substringStart))
        end if
        i = (i + 1)
    end while
    return result.toString()
end function

function trimMargin_rStr_Str_k_(m as String, marginPrefix = "|") as String
    return replaceIndentByMargin_rStr_Str_Str_k_(m, "", marginPrefix)
end function

function replaceIndentByMargin_rStr_Str_Str_k_(m as String, newIndent = "", marginPrefix = "|") as String
    lines = lines_rCharSequence_k_(m)
    result = StringBuilder_create_k_()
    firstLine = true
    i = 0
    while i < lines.get_size()
        line = lines.get_I_k_(i)
        if (i = 0) and isBlank_rStr_k_(line) then
            i = (i + 1)
            ' continue not supported on target Roku OS
        end if
        if (i = (lines.get_size() - 1)) and isBlank_rStr_k_(line) then
            i = (i + 1)
            ' continue not supported on target Roku OS
        end if
        if not firstLine then
            result.append_C_k_(chr(10))
        end if
        firstLine = false
        marginIndex = indexOf_rStr_Str_I_Z_k_(line, marginPrefix)
        if marginIndex >= 0 then
            beforeMargin = substring_rStr_I_I_k_(line, 0, marginIndex)
            allWhitespace = true
            j = 0
            while j < Len(beforeMargin)
                if not isWhitespace_rC_k_(beforeMargin.get_I_k_(j)) then
                    allWhitespace = false
                    exit while
                end if
                j = (j + 1)
            end while
            if allWhitespace then
                result.append_StrN_k_(newIndent)
                result.append_StrN_k_(substring_rStr_I_k_(line, marginIndex + Len(marginPrefix)))
                i = (i + 1)
                ' continue not supported on target Roku OS
            end if
        end if
        result.append_StrN_k_(line)
        i = (i + 1)
    end while
    return result.toString()
end function
