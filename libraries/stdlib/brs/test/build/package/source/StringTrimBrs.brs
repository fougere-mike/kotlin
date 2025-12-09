function trim_rCharSequence_CharSequence_k_(m as Object) as Object
    return trim_rCharSequence_Function1CZ_CharSequence_k_(m, {invoke: function(it as Object) as Boolean
        return isWhitespace_rC_Z_k_(it)
    end function})
end function

function trim_rStr_Str_k_(m as String) as String
    return (function(Str, trim_rCharSequence_CharSequence_k_)
        if trim_rCharSequence_CharSequence_k_(m) = invalid then return "null" else return (function(Str, trim_rCharSequence_CharSequence_k_)
            if (Type(trim_rCharSequence_CharSequence_k_(m)) = "String") or (Type(trim_rCharSequence_CharSequence_k_(m)) = "roString") then return trim_rCharSequence_CharSequence_k_(m) else return (function(Str, trim_rCharSequence_CharSequence_k_)
                if ((((((Type(trim_rCharSequence_CharSequence_k_(m)) = "Integer") or (Type(trim_rCharSequence_CharSequence_k_(m)) = "LongInteger")) or (Type(trim_rCharSequence_CharSequence_k_(m)) = "Float")) or (Type(trim_rCharSequence_CharSequence_k_(m)) = "Double")) or (Type(trim_rCharSequence_CharSequence_k_(m)) = "roInt")) or (Type(trim_rCharSequence_CharSequence_k_(m)) = "roFloat")) or (Type(trim_rCharSequence_CharSequence_k_(m)) = "roDouble") then return Str(trim_rCharSequence_CharSequence_k_(m)) else return (function(trim_rCharSequence_CharSequence_k_)
                    if (Type(trim_rCharSequence_CharSequence_k_(m)) = "Boolean") or (Type(trim_rCharSequence_CharSequence_k_(m)) = "roBoolean") then return (function(trim_rCharSequence_CharSequence_k_)
                        if trim_rCharSequence_CharSequence_k_(m) then return "true" else return "false"
                    end function)(trim_rCharSequence_CharSequence_k_) else return trim_rCharSequence_CharSequence_k_(m).toString()
                end function)(trim_rCharSequence_CharSequence_k_)
            end function)(Str, trim_rCharSequence_CharSequence_k_)
        end function)(Str, trim_rCharSequence_CharSequence_k_)
    end function)(Str, trim_rCharSequence_CharSequence_k_)
end function

function trimStart_rCharSequence_CharSequence_k_(m as Object) as Object
    return trimStart_rCharSequence_Function1CZ_CharSequence_k_(m, {invoke: function(it as Object) as Boolean
        return isWhitespace_rC_Z_k_(it)
    end function})
end function

function trimStart_rStr_Str_k_(m as String) as String
    return (function(Str, trimStart_rCharSequence_CharSequence_k_)
        if trimStart_rCharSequence_CharSequence_k_(m) = invalid then return "null" else return (function(Str, trimStart_rCharSequence_CharSequence_k_)
            if (Type(trimStart_rCharSequence_CharSequence_k_(m)) = "String") or (Type(trimStart_rCharSequence_CharSequence_k_(m)) = "roString") then return trimStart_rCharSequence_CharSequence_k_(m) else return (function(Str, trimStart_rCharSequence_CharSequence_k_)
                if ((((((Type(trimStart_rCharSequence_CharSequence_k_(m)) = "Integer") or (Type(trimStart_rCharSequence_CharSequence_k_(m)) = "LongInteger")) or (Type(trimStart_rCharSequence_CharSequence_k_(m)) = "Float")) or (Type(trimStart_rCharSequence_CharSequence_k_(m)) = "Double")) or (Type(trimStart_rCharSequence_CharSequence_k_(m)) = "roInt")) or (Type(trimStart_rCharSequence_CharSequence_k_(m)) = "roFloat")) or (Type(trimStart_rCharSequence_CharSequence_k_(m)) = "roDouble") then return Str(trimStart_rCharSequence_CharSequence_k_(m)) else return (function(trimStart_rCharSequence_CharSequence_k_)
                    if (Type(trimStart_rCharSequence_CharSequence_k_(m)) = "Boolean") or (Type(trimStart_rCharSequence_CharSequence_k_(m)) = "roBoolean") then return (function(trimStart_rCharSequence_CharSequence_k_)
                        if trimStart_rCharSequence_CharSequence_k_(m) then return "true" else return "false"
                    end function)(trimStart_rCharSequence_CharSequence_k_) else return trimStart_rCharSequence_CharSequence_k_(m).toString()
                end function)(trimStart_rCharSequence_CharSequence_k_)
            end function)(Str, trimStart_rCharSequence_CharSequence_k_)
        end function)(Str, trimStart_rCharSequence_CharSequence_k_)
    end function)(Str, trimStart_rCharSequence_CharSequence_k_)
end function

function trimEnd_rCharSequence_CharSequence_k_(m as Object) as Object
    return trimEnd_rCharSequence_Function1CZ_CharSequence_k_(m, {invoke: function(it as Object) as Boolean
        return isWhitespace_rC_Z_k_(it)
    end function})
end function

function trimEnd_rStr_Str_k_(m as String) as String
    return (function(Str, trimEnd_rCharSequence_CharSequence_k_)
        if trimEnd_rCharSequence_CharSequence_k_(m) = invalid then return "null" else return (function(Str, trimEnd_rCharSequence_CharSequence_k_)
            if (Type(trimEnd_rCharSequence_CharSequence_k_(m)) = "String") or (Type(trimEnd_rCharSequence_CharSequence_k_(m)) = "roString") then return trimEnd_rCharSequence_CharSequence_k_(m) else return (function(Str, trimEnd_rCharSequence_CharSequence_k_)
                if ((((((Type(trimEnd_rCharSequence_CharSequence_k_(m)) = "Integer") or (Type(trimEnd_rCharSequence_CharSequence_k_(m)) = "LongInteger")) or (Type(trimEnd_rCharSequence_CharSequence_k_(m)) = "Float")) or (Type(trimEnd_rCharSequence_CharSequence_k_(m)) = "Double")) or (Type(trimEnd_rCharSequence_CharSequence_k_(m)) = "roInt")) or (Type(trimEnd_rCharSequence_CharSequence_k_(m)) = "roFloat")) or (Type(trimEnd_rCharSequence_CharSequence_k_(m)) = "roDouble") then return Str(trimEnd_rCharSequence_CharSequence_k_(m)) else return (function(trimEnd_rCharSequence_CharSequence_k_)
                    if (Type(trimEnd_rCharSequence_CharSequence_k_(m)) = "Boolean") or (Type(trimEnd_rCharSequence_CharSequence_k_(m)) = "roBoolean") then return (function(trimEnd_rCharSequence_CharSequence_k_)
                        if trimEnd_rCharSequence_CharSequence_k_(m) then return "true" else return "false"
                    end function)(trimEnd_rCharSequence_CharSequence_k_) else return trimEnd_rCharSequence_CharSequence_k_(m).toString()
                end function)(trimEnd_rCharSequence_CharSequence_k_)
            end function)(Str, trimEnd_rCharSequence_CharSequence_k_)
        end function)(Str, trimEnd_rCharSequence_CharSequence_k_)
    end function)(Str, trimEnd_rCharSequence_CharSequence_k_)
end function

function trim_rCharSequence_Function1CZ_CharSequence_k_(m as Object, predicate as Object) as Object
    startIndex = 0
    endIndex = m.get_length() - 1
    while (startIndex <= endIndex) and predicate.invoke(m.get_I_C_k_(startIndex))
        startIndex = (startIndex + 1)
    end while
    while (endIndex >= startIndex) and predicate.invoke(m.get_I_C_k_(endIndex))
        endIndex = (endIndex - 1)
    end while
    return m.subSequence_I_I_CharSequence_k_(startIndex, endIndex + 1)
end function

function trim_rStr_Function1CZ_Str_k_(m as String, predicate as Object) as String
    return (function(Str, predicate, trim_rCharSequence_Function1CZ_CharSequence_k_)
        if trim_rCharSequence_Function1CZ_CharSequence_k_(m, predicate) = invalid then return "null" else return (function(Str, predicate, trim_rCharSequence_Function1CZ_CharSequence_k_)
            if (Type(trim_rCharSequence_Function1CZ_CharSequence_k_(m, predicate)) = "String") or (Type(trim_rCharSequence_Function1CZ_CharSequence_k_(m, predicate)) = "roString") then return trim_rCharSequence_Function1CZ_CharSequence_k_(m, predicate) else return (function(Str, predicate, trim_rCharSequence_Function1CZ_CharSequence_k_)
                if ((((((Type(trim_rCharSequence_Function1CZ_CharSequence_k_(m, predicate)) = "Integer") or (Type(trim_rCharSequence_Function1CZ_CharSequence_k_(m, predicate)) = "LongInteger")) or (Type(trim_rCharSequence_Function1CZ_CharSequence_k_(m, predicate)) = "Float")) or (Type(trim_rCharSequence_Function1CZ_CharSequence_k_(m, predicate)) = "Double")) or (Type(trim_rCharSequence_Function1CZ_CharSequence_k_(m, predicate)) = "roInt")) or (Type(trim_rCharSequence_Function1CZ_CharSequence_k_(m, predicate)) = "roFloat")) or (Type(trim_rCharSequence_Function1CZ_CharSequence_k_(m, predicate)) = "roDouble") then return Str(trim_rCharSequence_Function1CZ_CharSequence_k_(m, predicate)) else return (function(predicate, trim_rCharSequence_Function1CZ_CharSequence_k_)
                    if (Type(trim_rCharSequence_Function1CZ_CharSequence_k_(m, predicate)) = "Boolean") or (Type(trim_rCharSequence_Function1CZ_CharSequence_k_(m, predicate)) = "roBoolean") then return (function(predicate, trim_rCharSequence_Function1CZ_CharSequence_k_)
                        if trim_rCharSequence_Function1CZ_CharSequence_k_(m, predicate) then return "true" else return "false"
                    end function)(predicate, trim_rCharSequence_Function1CZ_CharSequence_k_) else return trim_rCharSequence_Function1CZ_CharSequence_k_(m, predicate).toString()
                end function)(predicate, trim_rCharSequence_Function1CZ_CharSequence_k_)
            end function)(Str, predicate, trim_rCharSequence_Function1CZ_CharSequence_k_)
        end function)(Str, predicate, trim_rCharSequence_Function1CZ_CharSequence_k_)
    end function)(Str, predicate, trim_rCharSequence_Function1CZ_CharSequence_k_)
end function

function trimStart_rCharSequence_Function1CZ_CharSequence_k_(m as Object, predicate as Object) as Object
    startIndex = 0
    while (startIndex < m.get_length()) and predicate.invoke(m.get_I_C_k_(startIndex))
        startIndex = (startIndex + 1)
    end while
    return m.subSequence_I_I_CharSequence_k_(startIndex, m.get_length())
end function

function trimStart_rStr_Function1CZ_Str_k_(m as String, predicate as Object) as String
    return (function(Str, predicate, trimStart_rCharSequence_Function1CZ_CharSequence_k_)
        if trimStart_rCharSequence_Function1CZ_CharSequence_k_(m, predicate) = invalid then return "null" else return (function(Str, predicate, trimStart_rCharSequence_Function1CZ_CharSequence_k_)
            if (Type(trimStart_rCharSequence_Function1CZ_CharSequence_k_(m, predicate)) = "String") or (Type(trimStart_rCharSequence_Function1CZ_CharSequence_k_(m, predicate)) = "roString") then return trimStart_rCharSequence_Function1CZ_CharSequence_k_(m, predicate) else return (function(Str, predicate, trimStart_rCharSequence_Function1CZ_CharSequence_k_)
                if ((((((Type(trimStart_rCharSequence_Function1CZ_CharSequence_k_(m, predicate)) = "Integer") or (Type(trimStart_rCharSequence_Function1CZ_CharSequence_k_(m, predicate)) = "LongInteger")) or (Type(trimStart_rCharSequence_Function1CZ_CharSequence_k_(m, predicate)) = "Float")) or (Type(trimStart_rCharSequence_Function1CZ_CharSequence_k_(m, predicate)) = "Double")) or (Type(trimStart_rCharSequence_Function1CZ_CharSequence_k_(m, predicate)) = "roInt")) or (Type(trimStart_rCharSequence_Function1CZ_CharSequence_k_(m, predicate)) = "roFloat")) or (Type(trimStart_rCharSequence_Function1CZ_CharSequence_k_(m, predicate)) = "roDouble") then return Str(trimStart_rCharSequence_Function1CZ_CharSequence_k_(m, predicate)) else return (function(predicate, trimStart_rCharSequence_Function1CZ_CharSequence_k_)
                    if (Type(trimStart_rCharSequence_Function1CZ_CharSequence_k_(m, predicate)) = "Boolean") or (Type(trimStart_rCharSequence_Function1CZ_CharSequence_k_(m, predicate)) = "roBoolean") then return (function(predicate, trimStart_rCharSequence_Function1CZ_CharSequence_k_)
                        if trimStart_rCharSequence_Function1CZ_CharSequence_k_(m, predicate) then return "true" else return "false"
                    end function)(predicate, trimStart_rCharSequence_Function1CZ_CharSequence_k_) else return trimStart_rCharSequence_Function1CZ_CharSequence_k_(m, predicate).toString()
                end function)(predicate, trimStart_rCharSequence_Function1CZ_CharSequence_k_)
            end function)(Str, predicate, trimStart_rCharSequence_Function1CZ_CharSequence_k_)
        end function)(Str, predicate, trimStart_rCharSequence_Function1CZ_CharSequence_k_)
    end function)(Str, predicate, trimStart_rCharSequence_Function1CZ_CharSequence_k_)
end function

function trimEnd_rCharSequence_Function1CZ_CharSequence_k_(m as Object, predicate as Object) as Object
    endIndex = m.get_length() - 1
    while (endIndex >= 0) and predicate.invoke(m.get_I_C_k_(endIndex))
        endIndex = (endIndex - 1)
    end while
    return m.subSequence_I_I_CharSequence_k_(0, endIndex + 1)
end function

function trimEnd_rStr_Function1CZ_Str_k_(m as String, predicate as Object) as String
    return (function(Str, predicate, trimEnd_rCharSequence_Function1CZ_CharSequence_k_)
        if trimEnd_rCharSequence_Function1CZ_CharSequence_k_(m, predicate) = invalid then return "null" else return (function(Str, predicate, trimEnd_rCharSequence_Function1CZ_CharSequence_k_)
            if (Type(trimEnd_rCharSequence_Function1CZ_CharSequence_k_(m, predicate)) = "String") or (Type(trimEnd_rCharSequence_Function1CZ_CharSequence_k_(m, predicate)) = "roString") then return trimEnd_rCharSequence_Function1CZ_CharSequence_k_(m, predicate) else return (function(Str, predicate, trimEnd_rCharSequence_Function1CZ_CharSequence_k_)
                if ((((((Type(trimEnd_rCharSequence_Function1CZ_CharSequence_k_(m, predicate)) = "Integer") or (Type(trimEnd_rCharSequence_Function1CZ_CharSequence_k_(m, predicate)) = "LongInteger")) or (Type(trimEnd_rCharSequence_Function1CZ_CharSequence_k_(m, predicate)) = "Float")) or (Type(trimEnd_rCharSequence_Function1CZ_CharSequence_k_(m, predicate)) = "Double")) or (Type(trimEnd_rCharSequence_Function1CZ_CharSequence_k_(m, predicate)) = "roInt")) or (Type(trimEnd_rCharSequence_Function1CZ_CharSequence_k_(m, predicate)) = "roFloat")) or (Type(trimEnd_rCharSequence_Function1CZ_CharSequence_k_(m, predicate)) = "roDouble") then return Str(trimEnd_rCharSequence_Function1CZ_CharSequence_k_(m, predicate)) else return (function(predicate, trimEnd_rCharSequence_Function1CZ_CharSequence_k_)
                    if (Type(trimEnd_rCharSequence_Function1CZ_CharSequence_k_(m, predicate)) = "Boolean") or (Type(trimEnd_rCharSequence_Function1CZ_CharSequence_k_(m, predicate)) = "roBoolean") then return (function(predicate, trimEnd_rCharSequence_Function1CZ_CharSequence_k_)
                        if trimEnd_rCharSequence_Function1CZ_CharSequence_k_(m, predicate) then return "true" else return "false"
                    end function)(predicate, trimEnd_rCharSequence_Function1CZ_CharSequence_k_) else return trimEnd_rCharSequence_Function1CZ_CharSequence_k_(m, predicate).toString()
                end function)(predicate, trimEnd_rCharSequence_Function1CZ_CharSequence_k_)
            end function)(Str, predicate, trimEnd_rCharSequence_Function1CZ_CharSequence_k_)
        end function)(Str, predicate, trimEnd_rCharSequence_Function1CZ_CharSequence_k_)
    end function)(Str, predicate, trimEnd_rCharSequence_Function1CZ_CharSequence_k_)
end function

function trim_rCharSequence_CharArray_CharSequence_k_(m as Object, chars as Object) as Object
    return trim_rCharSequence_Function1CZ_CharSequence_k_(m, {chars: chars, invoke: function(char as Object) as Boolean
        found = false
        i = 0
        while i < m.chars.get_size()
            if char = m.chars.get_I_C_k_(i) then
                found = true
                exit while
            end if
            i = (i + 1)
        end while
        return found
    end function})
end function

function trim_rStr_CharArray_Str_k_(m as String, chars as Object) as String
    return (function(Str, chars, trim_rCharSequence_CharArray_CharSequence_k_)
        if trim_rCharSequence_CharArray_CharSequence_k_(m, chars) = invalid then return "null" else return (function(Str, chars, trim_rCharSequence_CharArray_CharSequence_k_)
            if (Type(trim_rCharSequence_CharArray_CharSequence_k_(m, chars)) = "String") or (Type(trim_rCharSequence_CharArray_CharSequence_k_(m, chars)) = "roString") then return trim_rCharSequence_CharArray_CharSequence_k_(m, chars) else return (function(Str, chars, trim_rCharSequence_CharArray_CharSequence_k_)
                if ((((((Type(trim_rCharSequence_CharArray_CharSequence_k_(m, chars)) = "Integer") or (Type(trim_rCharSequence_CharArray_CharSequence_k_(m, chars)) = "LongInteger")) or (Type(trim_rCharSequence_CharArray_CharSequence_k_(m, chars)) = "Float")) or (Type(trim_rCharSequence_CharArray_CharSequence_k_(m, chars)) = "Double")) or (Type(trim_rCharSequence_CharArray_CharSequence_k_(m, chars)) = "roInt")) or (Type(trim_rCharSequence_CharArray_CharSequence_k_(m, chars)) = "roFloat")) or (Type(trim_rCharSequence_CharArray_CharSequence_k_(m, chars)) = "roDouble") then return Str(trim_rCharSequence_CharArray_CharSequence_k_(m, chars)) else return (function(chars, trim_rCharSequence_CharArray_CharSequence_k_)
                    if (Type(trim_rCharSequence_CharArray_CharSequence_k_(m, chars)) = "Boolean") or (Type(trim_rCharSequence_CharArray_CharSequence_k_(m, chars)) = "roBoolean") then return (function(chars, trim_rCharSequence_CharArray_CharSequence_k_)
                        if trim_rCharSequence_CharArray_CharSequence_k_(m, chars) then return "true" else return "false"
                    end function)(chars, trim_rCharSequence_CharArray_CharSequence_k_) else return trim_rCharSequence_CharArray_CharSequence_k_(m, chars).toString()
                end function)(chars, trim_rCharSequence_CharArray_CharSequence_k_)
            end function)(Str, chars, trim_rCharSequence_CharArray_CharSequence_k_)
        end function)(Str, chars, trim_rCharSequence_CharArray_CharSequence_k_)
    end function)(Str, chars, trim_rCharSequence_CharArray_CharSequence_k_)
end function

function trimStart_rCharSequence_CharArray_CharSequence_k_(m as Object, chars as Object) as Object
    return trimStart_rCharSequence_Function1CZ_CharSequence_k_(m, {chars: chars, invoke: function(char as Object) as Boolean
        found = false
        i = 0
        while i < m.chars.get_size()
            if char = m.chars.get_I_C_k_(i) then
                found = true
                exit while
            end if
            i = (i + 1)
        end while
        return found
    end function})
end function

function trimStart_rStr_CharArray_Str_k_(m as String, chars as Object) as String
    return (function(Str, chars, trimStart_rCharSequence_CharArray_CharSequence_k_)
        if trimStart_rCharSequence_CharArray_CharSequence_k_(m, chars) = invalid then return "null" else return (function(Str, chars, trimStart_rCharSequence_CharArray_CharSequence_k_)
            if (Type(trimStart_rCharSequence_CharArray_CharSequence_k_(m, chars)) = "String") or (Type(trimStart_rCharSequence_CharArray_CharSequence_k_(m, chars)) = "roString") then return trimStart_rCharSequence_CharArray_CharSequence_k_(m, chars) else return (function(Str, chars, trimStart_rCharSequence_CharArray_CharSequence_k_)
                if ((((((Type(trimStart_rCharSequence_CharArray_CharSequence_k_(m, chars)) = "Integer") or (Type(trimStart_rCharSequence_CharArray_CharSequence_k_(m, chars)) = "LongInteger")) or (Type(trimStart_rCharSequence_CharArray_CharSequence_k_(m, chars)) = "Float")) or (Type(trimStart_rCharSequence_CharArray_CharSequence_k_(m, chars)) = "Double")) or (Type(trimStart_rCharSequence_CharArray_CharSequence_k_(m, chars)) = "roInt")) or (Type(trimStart_rCharSequence_CharArray_CharSequence_k_(m, chars)) = "roFloat")) or (Type(trimStart_rCharSequence_CharArray_CharSequence_k_(m, chars)) = "roDouble") then return Str(trimStart_rCharSequence_CharArray_CharSequence_k_(m, chars)) else return (function(chars, trimStart_rCharSequence_CharArray_CharSequence_k_)
                    if (Type(trimStart_rCharSequence_CharArray_CharSequence_k_(m, chars)) = "Boolean") or (Type(trimStart_rCharSequence_CharArray_CharSequence_k_(m, chars)) = "roBoolean") then return (function(chars, trimStart_rCharSequence_CharArray_CharSequence_k_)
                        if trimStart_rCharSequence_CharArray_CharSequence_k_(m, chars) then return "true" else return "false"
                    end function)(chars, trimStart_rCharSequence_CharArray_CharSequence_k_) else return trimStart_rCharSequence_CharArray_CharSequence_k_(m, chars).toString()
                end function)(chars, trimStart_rCharSequence_CharArray_CharSequence_k_)
            end function)(Str, chars, trimStart_rCharSequence_CharArray_CharSequence_k_)
        end function)(Str, chars, trimStart_rCharSequence_CharArray_CharSequence_k_)
    end function)(Str, chars, trimStart_rCharSequence_CharArray_CharSequence_k_)
end function

function trimEnd_rCharSequence_CharArray_CharSequence_k_(m as Object, chars as Object) as Object
    return trimEnd_rCharSequence_Function1CZ_CharSequence_k_(m, {chars: chars, invoke: function(char as Object) as Boolean
        found = false
        i = 0
        while i < m.chars.get_size()
            if char = m.chars.get_I_C_k_(i) then
                found = true
                exit while
            end if
            i = (i + 1)
        end while
        return found
    end function})
end function

function trimEnd_rStr_CharArray_Str_k_(m as String, chars as Object) as String
    return (function(Str, chars, trimEnd_rCharSequence_CharArray_CharSequence_k_)
        if trimEnd_rCharSequence_CharArray_CharSequence_k_(m, chars) = invalid then return "null" else return (function(Str, chars, trimEnd_rCharSequence_CharArray_CharSequence_k_)
            if (Type(trimEnd_rCharSequence_CharArray_CharSequence_k_(m, chars)) = "String") or (Type(trimEnd_rCharSequence_CharArray_CharSequence_k_(m, chars)) = "roString") then return trimEnd_rCharSequence_CharArray_CharSequence_k_(m, chars) else return (function(Str, chars, trimEnd_rCharSequence_CharArray_CharSequence_k_)
                if ((((((Type(trimEnd_rCharSequence_CharArray_CharSequence_k_(m, chars)) = "Integer") or (Type(trimEnd_rCharSequence_CharArray_CharSequence_k_(m, chars)) = "LongInteger")) or (Type(trimEnd_rCharSequence_CharArray_CharSequence_k_(m, chars)) = "Float")) or (Type(trimEnd_rCharSequence_CharArray_CharSequence_k_(m, chars)) = "Double")) or (Type(trimEnd_rCharSequence_CharArray_CharSequence_k_(m, chars)) = "roInt")) or (Type(trimEnd_rCharSequence_CharArray_CharSequence_k_(m, chars)) = "roFloat")) or (Type(trimEnd_rCharSequence_CharArray_CharSequence_k_(m, chars)) = "roDouble") then return Str(trimEnd_rCharSequence_CharArray_CharSequence_k_(m, chars)) else return (function(chars, trimEnd_rCharSequence_CharArray_CharSequence_k_)
                    if (Type(trimEnd_rCharSequence_CharArray_CharSequence_k_(m, chars)) = "Boolean") or (Type(trimEnd_rCharSequence_CharArray_CharSequence_k_(m, chars)) = "roBoolean") then return (function(chars, trimEnd_rCharSequence_CharArray_CharSequence_k_)
                        if trimEnd_rCharSequence_CharArray_CharSequence_k_(m, chars) then return "true" else return "false"
                    end function)(chars, trimEnd_rCharSequence_CharArray_CharSequence_k_) else return trimEnd_rCharSequence_CharArray_CharSequence_k_(m, chars).toString()
                end function)(chars, trimEnd_rCharSequence_CharArray_CharSequence_k_)
            end function)(Str, chars, trimEnd_rCharSequence_CharArray_CharSequence_k_)
        end function)(Str, chars, trimEnd_rCharSequence_CharArray_CharSequence_k_)
    end function)(Str, chars, trimEnd_rCharSequence_CharArray_CharSequence_k_)
end function

function trimIndent_rStr_Str_k_(m as String) as String
    return replaceIndent_rStr_Str_Str_k_(m, "")
end function

function replaceIndent_rStr_Str_Str_k_(m as String, newIndent = "") as String
    lines = lines_rCharSequence_ListStr_k_(m)
    minIndent = 2147483647
    lineIndex = 0
    while lineIndex < lines.get_size()
        line = lines.get_I_AnyN_k_(lineIndex)
        indent = 0
        while (indent < line.get_length()) and isWhitespace_rC_Z_k_(line.get_I_C_k_(indent))
            indent = (indent + 1)
        end while
        if (indent < line.get_length()) and (indent < minIndent) then
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
    while i < lines.get_size()
        line = lines.get_I_AnyN_k_(i)
        if (i = 0) and isBlank_rStr_Z_k_(line) then
            i = (i + 1)
            ' continue not supported on target Roku OS
        end if
        if (i = (lines.get_size() - 1)) and isBlank_rStr_Z_k_(line) then
            i = (i + 1)
            ' continue not supported on target Roku OS
        end if
        if not firstLine then
            result.append_C_StringBuilder_k_(chr(10))
        end if
        firstLine = false
        if isNotBlank_rStr_Z_k_(line) then
            result.append_StrN_StringBuilder_k_(newIndent)
            __when_tmp0 = invalid
            if minIndent < line.get_length() then
                __when_tmp0 = minIndent
            else if true then
                __when_tmp0 = line.get_length()
            end if
            substringStart = __when_tmp0
            result.append_StrN_StringBuilder_k_(substring_rStr_I_Str_k_(line, substringStart))
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
    while i < lines.get_size()
        line = lines.get_I_AnyN_k_(i)
        if (i = 0) and isBlank_rStr_Z_k_(line) then
            i = (i + 1)
            ' continue not supported on target Roku OS
        end if
        if (i = (lines.get_size() - 1)) and isBlank_rStr_Z_k_(line) then
            i = (i + 1)
            ' continue not supported on target Roku OS
        end if
        if not firstLine then
            result.append_C_StringBuilder_k_(chr(10))
        end if
        firstLine = false
        marginIndex = indexOf_rStr_Str_I_Z_I_k_(line, marginPrefix)
        if marginIndex >= 0 then
            beforeMargin = substring_rStr_I_I_Str_k_(line, 0, marginIndex)
            allWhitespace = true
            j = 0
            while j < beforeMargin.get_length()
                if not isWhitespace_rC_Z_k_(beforeMargin.get_I_C_k_(j)) then
                    allWhitespace = false
                    exit while
                end if
                j = (j + 1)
            end while
            if allWhitespace then
                result.append_StrN_StringBuilder_k_(newIndent)
                result.append_StrN_StringBuilder_k_(substring_rStr_I_Str_k_(line, marginIndex + marginPrefix.get_length()))
                i = (i + 1)
                ' continue not supported on target Roku OS
            end if
        end if
        result.append_StrN_StringBuilder_k_(line)
        i = (i + 1)
    end while
    return result.toString()
end function
