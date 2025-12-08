function trim_rCharSequence_CharSequence_k_(m as Object) as Object
    return trim_rCharSequence_Function1CZ_CharSequence_k_(m, {invoke: function(it as Object) as Boolean
        return isWhitespace_rC_Z_k_(it)
    end function})
end function

function trim_rStr_Str_k_(m as String) as String
    return trim_rCharSequence_CharSequence_k_(m).toString()
end function

function trimStart_rCharSequence_CharSequence_k_(m as Object) as Object
    return trimStart_rCharSequence_Function1CZ_CharSequence_k_(m, {invoke: function(it as Object) as Boolean
        return isWhitespace_rC_Z_k_(it)
    end function})
end function

function trimStart_rStr_Str_k_(m as String) as String
    return trimStart_rCharSequence_CharSequence_k_(m).toString()
end function

function trimEnd_rCharSequence_CharSequence_k_(m as Object) as Object
    return trimEnd_rCharSequence_Function1CZ_CharSequence_k_(m, {invoke: function(it as Object) as Boolean
        return isWhitespace_rC_Z_k_(it)
    end function})
end function

function trimEnd_rStr_Str_k_(m as String) as String
    return trimEnd_rCharSequence_CharSequence_k_(m).toString()
end function

function trim_rCharSequence_Function1CZ_CharSequence_k_(m as Object, predicate as Function) as Object
    startIndex = 0
    endIndex = m.length - 1
    while (startIndex <= endIndex) and predicate.invoke(m[startIndex])
        startIndex = (startIndex + 1)
    end while
    while (endIndex >= startIndex) and predicate.invoke(m[endIndex])
        endIndex = (endIndex - 1)
    end while
    return m.subSequence(startIndex, endIndex + 1)
end function

function trim_rStr_Function1CZ_Str_k_(m as String, predicate as Function) as String
    return trim_rCharSequence_Function1CZ_CharSequence_k_(m, predicate).toString()
end function

function trimStart_rCharSequence_Function1CZ_CharSequence_k_(m as Object, predicate as Function) as Object
    startIndex = 0
    while (startIndex < m.length) and predicate.invoke(m[startIndex])
        startIndex = (startIndex + 1)
    end while
    return m.subSequence(startIndex, m.length)
end function

function trimStart_rStr_Function1CZ_Str_k_(m as String, predicate as Function) as String
    return trimStart_rCharSequence_Function1CZ_CharSequence_k_(m, predicate).toString()
end function

function trimEnd_rCharSequence_Function1CZ_CharSequence_k_(m as Object, predicate as Function) as Object
    endIndex = m.length - 1
    while (endIndex >= 0) and predicate.invoke(m[endIndex])
        endIndex = (endIndex - 1)
    end while
    return m.subSequence(0, endIndex + 1)
end function

function trimEnd_rStr_Function1CZ_Str_k_(m as String, predicate as Function) as String
    return trimEnd_rCharSequence_Function1CZ_CharSequence_k_(m, predicate).toString()
end function

function trim_rCharSequence_CharArray_CharSequence_k_(m as Object, chars as Object) as Object
    return trim_rCharSequence_Function1CZ_CharSequence_k_(m, {chars: chars, invoke: function(char as Object) as Boolean
        found = false
        i = 0
        while i < m.chars.size
            if char = m.chars[i] then
                found = true
                exit while
            end if
            i = (i + 1)
        end while
        return found
    end function})
end function

function trim_rStr_CharArray_Str_k_(m as String, chars as Object) as String
    return trim_rCharSequence_CharArray_CharSequence_k_(m, [chars]).toString()
end function

function trimStart_rCharSequence_CharArray_CharSequence_k_(m as Object, chars as Object) as Object
    return trimStart_rCharSequence_Function1CZ_CharSequence_k_(m, {chars: chars, invoke: function(char as Object) as Boolean
        found = false
        i = 0
        while i < m.chars.size
            if char = m.chars[i] then
                found = true
                exit while
            end if
            i = (i + 1)
        end while
        return found
    end function})
end function

function trimStart_rStr_CharArray_Str_k_(m as String, chars as Object) as String
    return trimStart_rCharSequence_CharArray_CharSequence_k_(m, [chars]).toString()
end function

function trimEnd_rCharSequence_CharArray_CharSequence_k_(m as Object, chars as Object) as Object
    return trimEnd_rCharSequence_Function1CZ_CharSequence_k_(m, {chars: chars, invoke: function(char as Object) as Boolean
        found = false
        i = 0
        while i < m.chars.size
            if char = m.chars[i] then
                found = true
                exit while
            end if
            i = (i + 1)
        end while
        return found
    end function})
end function

function trimEnd_rStr_CharArray_Str_k_(m as String, chars as Object) as String
    return trimEnd_rCharSequence_CharArray_CharSequence_k_(m, [chars]).toString()
end function

function trimIndent_rStr_Str_k_(m as String) as String
    return replaceIndent_rStr_Str_Str_k_(m, "")
end function

function replaceIndent_rStr_Str_Str_k_(m as String, newIndent = "") as String
    lines = lines_rCharSequence_ListStr_k_(m)
    minIndent = 2147483647
    lineIndex = 0
    while lineIndex < lines.size
        line = lines[lineIndex]
        indent = 0
        while (indent < line.length) and isWhitespace_rC_Z_k_(line[indent])
            indent = (indent + 1)
        end while
        if (indent < line.length) and (indent < minIndent) then
            minIndent = indent
        end if
        lineIndex = (lineIndex + 1)
    end while
    if minIndent = 2147483647 then
        minIndent = 0
    end if
    result = StringBuilder_create_StringBuilder_k_()
    firstLine = true
    i = 0
    while i < lines.size
        line = lines[i]
        if (i = 0) and isBlank_rStr_Z_k_(line) then
            i = (i + 1)
            ' continue not supported on target Roku OS
        end if
        if (i = (lines.size - 1)) and isBlank_rStr_Z_k_(line) then
            i = (i + 1)
            ' continue not supported on target Roku OS
        end if
        if firstLine.not() then
            result.append(chr(10))
        end if
        firstLine = false
        if isNotBlank_rStr_Z_k_(line) then
            result.append(newIndent)
            __when_tmp0 = invalid
            if minIndent < line.length then
                __when_tmp0 = minIndent
            else if true then
                __when_tmp0 = line.length
            end if
            substringStart = __when_tmp0
            result.append(substring_rStr_I_Str_k_(line, substringStart))
        end if
        i = (i + 1)
    end while
    return result.toString()
end function

function trimMargin_rStr_Str_Str_k_(m as String, marginPrefix = "|") as String
    return replaceIndentByMargin_rStr_Str_Str_Str_k_(m, "", marginPrefix)
end function

function replaceIndentByMargin_rStr_Str_Str_Str_k_(m as String, newIndent = "", marginPrefix = "|") as String
    lines = lines_rCharSequence_ListStr_k_(m)
    result = StringBuilder_create_StringBuilder_k_()
    firstLine = true
    i = 0
    while i < lines.size
        line = lines[i]
        if (i = 0) and isBlank_rStr_Z_k_(line) then
            i = (i + 1)
            ' continue not supported on target Roku OS
        end if
        if (i = (lines.size - 1)) and isBlank_rStr_Z_k_(line) then
            i = (i + 1)
            ' continue not supported on target Roku OS
        end if
        if firstLine.not() then
            result.append(chr(10))
        end if
        firstLine = false
        marginIndex = indexOf_rStr_Str_I_Z_I_k_(line, marginPrefix)
        if marginIndex >= 0 then
            beforeMargin = substring_rStr_I_I_Str_k_(line, 0, marginIndex)
            allWhitespace = true
            j = 0
            while j < beforeMargin.length
                if isWhitespace_rC_Z_k_(beforeMargin[j]).not() then
                    allWhitespace = false
                    exit while
                end if
                j = (j + 1)
            end while
            if allWhitespace then
                result.append(newIndent)
                result.append(substring_rStr_I_Str_k_(line, marginIndex + marginPrefix.length))
                i = (i + 1)
                ' continue not supported on target Roku OS
            end if
        end if
        result.append(line)
        i = (i + 1)
    end while
    return result.toString()
end function
