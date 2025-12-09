function StringBuilder_create_Str_StringBuilder_k_(content as String) as Object
    this = {}
    this.__type = "StringBuilder"
    this.__proto = ["StringBuilder"]
    this.string = content
    this.get_I_C_k_ = StringBuilder_get_I_C_k_
    this.subSequence_I_I_CharSequence_k_ = StringBuilder_subSequence_I_I_CharSequence_k_
    this.append_C_StringBuilder_k_ = StringBuilder_append_C_StringBuilder_k_
    this.append_CharSequenceN_StringBuilder_k_ = StringBuilder_append_CharSequenceN_StringBuilder_k_
    this.append_CharSequenceN_I_I_StringBuilder_k_ = StringBuilder_append_CharSequenceN_I_I_StringBuilder_k_
    this.reverse_StringBuilder_k_ = StringBuilder_reverse_StringBuilder_k_
    this.append_AnyN_StringBuilder_k_ = StringBuilder_append_AnyN_StringBuilder_k_
    this.append_Z_StringBuilder_k_ = StringBuilder_append_Z_StringBuilder_k_
    this.append_B_StringBuilder_k_ = StringBuilder_append_B_StringBuilder_k_
    this.append_S_StringBuilder_k_ = StringBuilder_append_S_StringBuilder_k_
    this.append_I_StringBuilder_k_ = StringBuilder_append_I_StringBuilder_k_
    this.append_J_StringBuilder_k_ = StringBuilder_append_J_StringBuilder_k_
    this.append_F_StringBuilder_k_ = StringBuilder_append_F_StringBuilder_k_
    this.append_D_StringBuilder_k_ = StringBuilder_append_D_StringBuilder_k_
    this.append_CharArray_StringBuilder_k_ = StringBuilder_append_CharArray_StringBuilder_k_
    this.append_StrN_StringBuilder_k_ = StringBuilder_append_StrN_StringBuilder_k_
    this.capacity_I_k_ = StringBuilder_capacity_I_k_
    this.ensureCapacity_I_k_ = StringBuilder_ensureCapacity_I_k_
    this.indexOf_Str_I_k_ = StringBuilder_indexOf_Str_I_k_
    this.indexOf_Str_I_I_k_ = StringBuilder_indexOf_Str_I_I_k_
    this.lastIndexOf_Str_I_k_ = StringBuilder_lastIndexOf_Str_I_k_
    this.lastIndexOf_Str_I_I_k_ = StringBuilder_lastIndexOf_Str_I_I_k_
    this.insert_I_Z_StringBuilder_k_ = StringBuilder_insert_I_Z_StringBuilder_k_
    this.insert_I_B_StringBuilder_k_ = StringBuilder_insert_I_B_StringBuilder_k_
    this.insert_I_S_StringBuilder_k_ = StringBuilder_insert_I_S_StringBuilder_k_
    this.insert_I_I_StringBuilder_k_ = StringBuilder_insert_I_I_StringBuilder_k_
    this.insert_I_J_StringBuilder_k_ = StringBuilder_insert_I_J_StringBuilder_k_
    this.insert_I_F_StringBuilder_k_ = StringBuilder_insert_I_F_StringBuilder_k_
    this.insert_I_D_StringBuilder_k_ = StringBuilder_insert_I_D_StringBuilder_k_
    this.insert_I_C_StringBuilder_k_ = StringBuilder_insert_I_C_StringBuilder_k_
    this.insert_I_CharArray_StringBuilder_k_ = StringBuilder_insert_I_CharArray_StringBuilder_k_
    this.insert_I_CharSequenceN_StringBuilder_k_ = StringBuilder_insert_I_CharSequenceN_StringBuilder_k_
    this.insert_I_AnyN_StringBuilder_k_ = StringBuilder_insert_I_AnyN_StringBuilder_k_
    this.insert_I_StrN_StringBuilder_k_ = StringBuilder_insert_I_StrN_StringBuilder_k_
    this.setLength_I_k_ = StringBuilder_setLength_I_k_
    this.substring_I_Str_k_ = StringBuilder_substring_I_Str_k_
    this.substring_I_I_Str_k_ = StringBuilder_substring_I_I_Str_k_
    this.trimToSize = StringBuilder_trimToSize
    this.toString_Str_k_ = StringBuilder_toString_Str_k_
    this.clear_StringBuilder_k_ = StringBuilder_clear_StringBuilder_k_
    this.set_I_C_k_ = StringBuilder_set_I_C_k_
    this.setRange_I_I_Str_StringBuilder_k_ = StringBuilder_setRange_I_I_Str_StringBuilder_k_
    this.checkReplaceRange_I_I_I_k_ = StringBuilder_checkReplaceRange_I_I_I_k_
    this.deleteAt_I_StringBuilder_k_ = StringBuilder_deleteAt_I_StringBuilder_k_
    this.deleteRange_I_I_StringBuilder_k_ = StringBuilder_deleteRange_I_I_StringBuilder_k_
    this.toCharArray_CharArray_I_I_I_k_ = StringBuilder_toCharArray_CharArray_I_I_I_k_
    this.appendRange_CharArray_I_I_StringBuilder_k_ = StringBuilder_appendRange_CharArray_I_I_StringBuilder_k_
    this.appendRange_CharSequence_I_I_StringBuilder_k_ = StringBuilder_appendRange_CharSequence_I_I_StringBuilder_k_
    this.insertRange_I_CharArray_I_I_StringBuilder_k_ = StringBuilder_insertRange_I_CharArray_I_I_StringBuilder_k_
    this.insertRange_I_CharSequence_I_I_StringBuilder_k_ = StringBuilder_insertRange_I_CharSequence_I_I_StringBuilder_k_
    this.appendLine_StringBuilder_k_ = StringBuilder_appendLine_StringBuilder_k_
    this.appendLine_CharSequenceN_StringBuilder_k_ = StringBuilder_appendLine_CharSequenceN_StringBuilder_k_
    this.appendLine_StrN_StringBuilder_k_ = StringBuilder_appendLine_StrN_StringBuilder_k_
    this.appendLine_AnyN_StringBuilder_k_ = StringBuilder_appendLine_AnyN_StringBuilder_k_
    this.appendLine_C_StringBuilder_k_ = StringBuilder_appendLine_C_StringBuilder_k_
    this.appendLine_Z_StringBuilder_k_ = StringBuilder_appendLine_Z_StringBuilder_k_
    this.appendLine_I_StringBuilder_k_ = StringBuilder_appendLine_I_StringBuilder_k_
    this.appendLine_S_StringBuilder_k_ = StringBuilder_appendLine_S_StringBuilder_k_
    this.appendLine_B_StringBuilder_k_ = StringBuilder_appendLine_B_StringBuilder_k_
    this.appendLine_J_StringBuilder_k_ = StringBuilder_appendLine_J_StringBuilder_k_
    this.appendLine_F_StringBuilder_k_ = StringBuilder_appendLine_F_StringBuilder_k_
    this.appendLine_D_StringBuilder_k_ = StringBuilder_appendLine_D_StringBuilder_k_
    this.get_string = StringBuilder_get_string_Str_k_
    this.set_string = StringBuilder_set_string_Str_k_
    this.get_length = StringBuilder_get_length_I_k_
    return this
end function

function StringBuilder_create_I_StringBuilder_k_(capacity as Integer) as Object
    return StringBuilder_create_StringBuilder_k_()
end function

function StringBuilder_create_CharSequence_StringBuilder_k_(content as Object) as Object
    return StringBuilder_create_Str_StringBuilder_k_((function(Str, content)
        if content = invalid then return "null" else return (function(Str, content)
            if (Type(content) = "String") or (Type(content) = "roString") then return content else return (function(Str, content)
                if ((((((Type(content) = "Integer") or (Type(content) = "LongInteger")) or (Type(content) = "Float")) or (Type(content) = "Double")) or (Type(content) = "roInt")) or (Type(content) = "roFloat")) or (Type(content) = "roDouble") then return Str(content) else return (function(content)
                    if (Type(content) = "Boolean") or (Type(content) = "roBoolean") then return (function(content)
                        if content then return "true" else return "false"
                    end function)(content) else return content.toString()
                end function)(content)
            end function)(Str, content)
        end function)(Str, content)
    end function)(Str, content))
end function

function StringBuilder_create_StringBuilder_k_() as Object
    return StringBuilder_create_Str_StringBuilder_k_("")
end function

function StringBuilder_get_I_C_k_(index as Integer) as Object
    return getOrElse_rStr_I_Function1IC_C_k_(m.get_string(), index, {index: index, this: m, invoke: function(it as Integer) as Object
        throw IndexOutOfBoundsException_create_StrN_IndexOutOfBoundsException_k_(((("index: " + m.index) + ", length: ") + m.this.get_length()) + "}")
    end function})
end function

function StringBuilder_subSequence_I_I_CharSequence_k_(startIndex as Integer, endIndex as Integer) as Object
    return substring_rStr_I_I_Str_k_(m.get_string(), startIndex, endIndex)
end function

function StringBuilder_append_C_StringBuilder_k_(value as Object) as Object
    m.set_string(m.get_string() + value)
    return m
end function

function StringBuilder_append_CharSequenceN_StringBuilder_k_(value as Dynamic) as Object
    m.set_string(m.get_string() + ((function(Str, value)
        if value = invalid then return "null" else return (function(Str, value)
            if (Type(value) = "String") or (Type(value) = "roString") then return value else return (function(Str, value)
                if ((((((Type(value) = "Integer") or (Type(value) = "LongInteger")) or (Type(value) = "Float")) or (Type(value) = "Double")) or (Type(value) = "roInt")) or (Type(value) = "roFloat")) or (Type(value) = "roDouble") then return Str(value) else return (function(value)
                    if (Type(value) = "Boolean") or (Type(value) = "roBoolean") then return (function(value)
                        if value then return "true" else return "false"
                    end function)(value) else return value.toString()
                end function)(value)
            end function)(Str, value)
        end function)(Str, value)
    end function)(Str, value)))
    return m
end function

function StringBuilder_append_CharSequenceN_I_I_StringBuilder_k_(value as Dynamic, startIndex as Integer, endIndex as Integer) as Object
    tmp0_elvis_lhs = value
    __when_tmp0 = invalid
    if tmp0_elvis_lhs = invalid then
        __when_tmp0 = "null"
    else if true then
        __when_tmp0 = tmp0_elvis_lhs
    end if
    return m.appendRange_CharSequence_I_I_StringBuilder_k_(__when_tmp0, startIndex, endIndex)

end function

function StringBuilder_reverse_StringBuilder_k_() as Object
    reversed = ""
    index = m.get_string().get_length() - 1
    while index >= 0
        low = m.get_string().get_I_C_k_(index = (index - 1))
        if isLowSurrogate_rC_Z_k_(low) and (index >= 0) then
            high = m.get_string().get_I_C_k_(index = (index - 1))
            if isHighSurrogate_rC_Z_k_(high) then
                reversed = ((reversed + high) + low)
            else if true then
                reversed = ((reversed + low) + high)
            end if
        else if true then
            reversed = (reversed + low)
        end if
    end while
    m.set_string(reversed)
    return m
end function

function StringBuilder_append_AnyN_StringBuilder_k_(value as Dynamic) as Object
    m.set_string(m.get_string() + ((function(Str, value)
        if value = invalid then return "null" else return (function(Str, value)
            if (Type(value) = "String") or (Type(value) = "roString") then return value else return (function(Str, value)
                if ((((((Type(value) = "Integer") or (Type(value) = "LongInteger")) or (Type(value) = "Float")) or (Type(value) = "Double")) or (Type(value) = "roInt")) or (Type(value) = "roFloat")) or (Type(value) = "roDouble") then return Str(value) else return (function(value)
                    if (Type(value) = "Boolean") or (Type(value) = "roBoolean") then return (function(value)
                        if value then return "true" else return "false"
                    end function)(value) else return value.toString()
                end function)(value)
            end function)(Str, value)
        end function)(Str, value)
    end function)(Str, value)))
    return m
end function

function StringBuilder_append_Z_StringBuilder_k_(value as Boolean) as Object
    m.set_string(m.get_string() + value)
    return m
end function

function StringBuilder_append_B_StringBuilder_k_(value as Integer) as Object
    return m.append_StrN_StringBuilder_k_(Str(value))
end function

function StringBuilder_append_S_StringBuilder_k_(value as Integer) as Object
    return m.append_StrN_StringBuilder_k_(Str(value))
end function

function StringBuilder_append_I_StringBuilder_k_(value as Integer) as Object
    return m.append_StrN_StringBuilder_k_(Str(value))
end function

function StringBuilder_append_J_StringBuilder_k_(value as LongInteger) as Object
    return m.append_StrN_StringBuilder_k_(Str(value))
end function

function StringBuilder_append_F_StringBuilder_k_(value as Float) as Object
    return m.append_StrN_StringBuilder_k_(Str(value))
end function

function StringBuilder_append_D_StringBuilder_k_(value as Double) as Object
    return m.append_StrN_StringBuilder_k_(Str(value))
end function

function StringBuilder_append_CharArray_StringBuilder_k_(value as Object) as Object
    m.set_string(m.get_string() + concatToString_rCharArray_Str_k_(value))
    return m
end function

function StringBuilder_append_StrN_StringBuilder_k_(value as Dynamic) as Object
    tmp0_elvis_lhs = value
    __when_tmp1 = invalid
    if tmp0_elvis_lhs = invalid then
        __when_tmp1 = "null"
    else if true then
        __when_tmp1 = tmp0_elvis_lhs
    end if
    m.set_string(m.get_string() + __when_tmp1)

    return m
end function

function StringBuilder_capacity_I_k_() as Integer
    return m.get_length()
end function

sub StringBuilder_ensureCapacity_I_k_(minimumCapacity as Integer)
end sub

function StringBuilder_indexOf_Str_I_k_(string_ as String) as Integer
    return indexOf_rStr_Str_I_Z_I_k_(m.get_string(), string_)
end function

function StringBuilder_indexOf_Str_I_I_k_(string_ as String, startIndex as Integer) as Integer
    return indexOf_rStr_Str_I_Z_I_k_(m.get_string(), string_, startIndex)
end function

function StringBuilder_lastIndexOf_Str_I_k_(string_ as String) as Integer
    return lastIndexOf_rStr_Str_I_Z_I_k_(m.get_string(), string_)
end function

function StringBuilder_lastIndexOf_Str_I_I_k_(string_ as String, startIndex as Integer) as Integer
    if isEmpty_rStr_Z_k_(string_) and (startIndex < 0) then
        return -1
    end if
    return lastIndexOf_rStr_Str_I_Z_I_k_(m.get_string(), string_, startIndex)
end function

function StringBuilder_insert_I_Z_StringBuilder_k_(index as Integer, value as Boolean) as Object
    checkPositionIndex_I_I_k_(index, m.get_length())
    m.set_string((substring_rStr_I_I_Str_k_(m.get_string(), 0, index) + value) + substring_rStr_I_Str_k_(m.get_string(), index))
    return m
end function

function StringBuilder_insert_I_B_StringBuilder_k_(index as Integer, value as Integer) as Object
    return m.insert_I_StrN_StringBuilder_k_(index, Str(value))
end function

function StringBuilder_insert_I_S_StringBuilder_k_(index as Integer, value as Integer) as Object
    return m.insert_I_StrN_StringBuilder_k_(index, Str(value))
end function

function StringBuilder_insert_I_I_StringBuilder_k_(index as Integer, value as Integer) as Object
    return m.insert_I_StrN_StringBuilder_k_(index, Str(value))
end function

function StringBuilder_insert_I_J_StringBuilder_k_(index as Integer, value as LongInteger) as Object
    return m.insert_I_StrN_StringBuilder_k_(index, Str(value))
end function

function StringBuilder_insert_I_F_StringBuilder_k_(index as Integer, value as Float) as Object
    return m.insert_I_StrN_StringBuilder_k_(index, Str(value))
end function

function StringBuilder_insert_I_D_StringBuilder_k_(index as Integer, value as Double) as Object
    return m.insert_I_StrN_StringBuilder_k_(index, Str(value))
end function

function StringBuilder_insert_I_C_StringBuilder_k_(index as Integer, value as Object) as Object
    checkPositionIndex_I_I_k_(index, m.get_length())
    m.set_string((substring_rStr_I_I_Str_k_(m.get_string(), 0, index) + value) + substring_rStr_I_Str_k_(m.get_string(), index))
    return m
end function

function StringBuilder_insert_I_CharArray_StringBuilder_k_(index as Integer, value as Object) as Object
    checkPositionIndex_I_I_k_(index, m.get_length())
    m.set_string((substring_rStr_I_I_Str_k_(m.get_string(), 0, index) + concatToString_rCharArray_Str_k_(value)) + substring_rStr_I_Str_k_(m.get_string(), index))
    return m
end function

function StringBuilder_insert_I_CharSequenceN_StringBuilder_k_(index as Integer, value as Dynamic) as Object
    checkPositionIndex_I_I_k_(index, m.get_length())
    m.set_string((substring_rStr_I_I_Str_k_(m.get_string(), 0, index) + ((function(Str, value)
        if value = invalid then return "null" else return (function(Str, value)
            if (Type(value) = "String") or (Type(value) = "roString") then return value else return (function(Str, value)
                if ((((((Type(value) = "Integer") or (Type(value) = "LongInteger")) or (Type(value) = "Float")) or (Type(value) = "Double")) or (Type(value) = "roInt")) or (Type(value) = "roFloat")) or (Type(value) = "roDouble") then return Str(value) else return (function(value)
                    if (Type(value) = "Boolean") or (Type(value) = "roBoolean") then return (function(value)
                        if value then return "true" else return "false"
                    end function)(value) else return value.toString()
                end function)(value)
            end function)(Str, value)
        end function)(Str, value)
    end function)(Str, value))) + substring_rStr_I_Str_k_(m.get_string(), index))
    return m
end function

function StringBuilder_insert_I_AnyN_StringBuilder_k_(index as Integer, value as Dynamic) as Object
    checkPositionIndex_I_I_k_(index, m.get_length())
    m.set_string((substring_rStr_I_I_Str_k_(m.get_string(), 0, index) + ((function(Str, value)
        if value = invalid then return "null" else return (function(Str, value)
            if (Type(value) = "String") or (Type(value) = "roString") then return value else return (function(Str, value)
                if ((((((Type(value) = "Integer") or (Type(value) = "LongInteger")) or (Type(value) = "Float")) or (Type(value) = "Double")) or (Type(value) = "roInt")) or (Type(value) = "roFloat")) or (Type(value) = "roDouble") then return Str(value) else return (function(value)
                    if (Type(value) = "Boolean") or (Type(value) = "roBoolean") then return (function(value)
                        if value then return "true" else return "false"
                    end function)(value) else return value.toString()
                end function)(value)
            end function)(Str, value)
        end function)(Str, value)
    end function)(Str, value))) + substring_rStr_I_Str_k_(m.get_string(), index))
    return m
end function

function StringBuilder_insert_I_StrN_StringBuilder_k_(index as Integer, value as Dynamic) as Object
    checkPositionIndex_I_I_k_(index, m.get_length())
    tmp0_elvis_lhs = value
    __when_tmp2 = invalid
    if tmp0_elvis_lhs = invalid then
        __when_tmp2 = "null"
    else if true then
        __when_tmp2 = tmp0_elvis_lhs
    end if
    toInsert = __when_tmp2

    m.set_string((substring_rStr_I_I_Str_k_(m.get_string(), 0, index) + toInsert) + substring_rStr_I_Str_k_(m.get_string(), index))
    return m
end function

sub StringBuilder_setLength_I_k_(newLength as Integer)
    if newLength < 0 then
        throw IllegalArgumentException_create_StrN_IllegalArgumentException_k_(("Negative new length: " + newLength) + ".")
    end if
    if newLength <= m.get_length() then
        m.set_string(substring_rStr_I_I_Str_k_(m.get_string(), 0, newLength))
    else if true then
        progression = until_rI_I_IntRange_k_(m.get_length(), newLength)
        inductionVariable = progression.get_first()
        last = progression.get_last()
        if inductionVariable <= last then
                        i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            m.set_string(m.get_string() + " ")


            while i <> last
                i = inductionVariable
                inductionVariable = (inductionVariable + 1)

                m.set_string(m.get_string() + " ")

            end while

        end if
    end if
end sub

function StringBuilder_substring_I_Str_k_(startIndex as Integer) as String
    checkPositionIndex_I_I_k_(startIndex, m.get_length())
    return substring_rStr_I_Str_k_(m.get_string(), startIndex)
end function

function StringBuilder_substring_I_I_Str_k_(startIndex as Integer, endIndex as Integer) as String
    checkBoundsIndexes_I_I_I_k_(startIndex, endIndex, m.get_length())
    return substring_rStr_I_I_Str_k_(m.get_string(), startIndex, endIndex)
end function

sub StringBuilder_trimToSize()
end sub

function StringBuilder_toString_Str_k_() as String
    return m.get_string()
end function

function StringBuilder_clear_StringBuilder_k_() as Object
    m.set_string("")
    return m
end function

sub StringBuilder_set_I_C_k_(index as Integer, value as Object)
    checkElementIndex_I_I_k_(index, m.get_length())
    m.set_string((substring_rStr_I_I_Str_k_(m.get_string(), 0, index) + value) + substring_rStr_I_Str_k_(m.get_string(), index + 1))
end sub

function StringBuilder_setRange_I_I_Str_StringBuilder_k_(startIndex as Integer, endIndex as Integer, value as String) as Object
    m.checkReplaceRange_I_I_I_k_(startIndex, endIndex, m.get_length())
    m.set_string((substring_rStr_I_I_Str_k_(m.get_string(), 0, startIndex) + value) + substring_rStr_I_Str_k_(m.get_string(), endIndex))
    return m
end function

sub StringBuilder_checkReplaceRange_I_I_I_k_(startIndex as Integer, endIndex as Integer, length as Integer)
    if (startIndex < 0) or (startIndex > length) then
        throw IndexOutOfBoundsException_create_StrN_IndexOutOfBoundsException_k_((("startIndex: " + startIndex) + ", length: ") + length)
    end if
    if startIndex > endIndex then
        throw IllegalArgumentException_create_StrN_IllegalArgumentException_k_(((("startIndex(" + startIndex) + ") > endIndex(") + endIndex) + ")")
    end if
end sub

function StringBuilder_deleteAt_I_StringBuilder_k_(index as Integer) as Object
    checkElementIndex_I_I_k_(index, m.get_length())
    m.set_string(substring_rStr_I_I_Str_k_(m.get_string(), 0, index) + substring_rStr_I_Str_k_(m.get_string(), index + 1))
    return m
end function

function StringBuilder_deleteRange_I_I_StringBuilder_k_(startIndex as Integer, endIndex as Integer) as Object
    m.checkReplaceRange_I_I_I_k_(startIndex, endIndex, m.get_length())
    m.set_string(substring_rStr_I_I_Str_k_(m.get_string(), 0, startIndex) + substring_rStr_I_Str_k_(m.get_string(), endIndex))
    return m
end function

sub StringBuilder_toCharArray_CharArray_I_I_I_k_(destination as Object, destinationOffset = 0, startIndex = 0, endIndex = m.get_length())
    checkBoundsIndexes_I_I_I_k_(startIndex, endIndex, m.get_length())
    checkBoundsIndexes_I_I_I_k_(destinationOffset, (destinationOffset + endIndex) - startIndex, destination.get_size())
    dstIndex = destinationOffset
    progression = until_rI_I_IntRange_k_(startIndex, endIndex)
    inductionVariable = progression.get_first()
    last = progression.get_last()
    if inductionVariable <= last then
                index = inductionVariable
        inductionVariable = (inductionVariable + 1)

        destination.set_I_C_k_(dstIndex = (dstIndex + 1), m.get_string().get_I_C_k_(index))


        while index <> last
            index = inductionVariable
            inductionVariable = (inductionVariable + 1)

            destination.set_I_C_k_(dstIndex = (dstIndex + 1), m.get_string().get_I_C_k_(index))

        end while

    end if

end sub

function StringBuilder_appendRange_CharArray_I_I_StringBuilder_k_(value as Object, startIndex as Integer, endIndex as Integer) as Object
    m.set_string(m.get_string() + concatToString_rCharArray_I_I_Str_k_(value, startIndex, endIndex))
    return m
end function

function StringBuilder_appendRange_CharSequence_I_I_StringBuilder_k_(value as Object, startIndex as Integer, endIndex as Integer) as Object
    stringCsq = (function(Str, value)
        if value = invalid then return "null" else return (function(Str, value)
            if (Type(value) = "String") or (Type(value) = "roString") then return value else return (function(Str, value)
                if ((((((Type(value) = "Integer") or (Type(value) = "LongInteger")) or (Type(value) = "Float")) or (Type(value) = "Double")) or (Type(value) = "roInt")) or (Type(value) = "roFloat")) or (Type(value) = "roDouble") then return Str(value) else return (function(value)
                    if (Type(value) = "Boolean") or (Type(value) = "roBoolean") then return (function(value)
                        if value then return "true" else return "false"
                    end function)(value) else return value.toString()
                end function)(value)
            end function)(Str, value)
        end function)(Str, value)
    end function)(Str, value)
    checkBoundsIndexes_I_I_I_k_(startIndex, endIndex, stringCsq.get_length())
    m.set_string(m.get_string() + substring_rStr_I_I_Str_k_(stringCsq, startIndex, endIndex))
    return m
end function

function StringBuilder_insertRange_I_CharArray_I_I_StringBuilder_k_(index as Integer, value as Object, startIndex as Integer, endIndex as Integer) as Object
    checkPositionIndex_I_I_k_(index, m.get_length())
    m.set_string((substring_rStr_I_I_Str_k_(m.get_string(), 0, index) + concatToString_rCharArray_I_I_Str_k_(value, startIndex, endIndex)) + substring_rStr_I_Str_k_(m.get_string(), index))
    return m
end function

function StringBuilder_insertRange_I_CharSequence_I_I_StringBuilder_k_(index as Integer, value as Object, startIndex as Integer, endIndex as Integer) as Object
    checkPositionIndex_I_I_k_(index, m.get_length())
    stringCsq = (function(Str, value)
        if value = invalid then return "null" else return (function(Str, value)
            if (Type(value) = "String") or (Type(value) = "roString") then return value else return (function(Str, value)
                if ((((((Type(value) = "Integer") or (Type(value) = "LongInteger")) or (Type(value) = "Float")) or (Type(value) = "Double")) or (Type(value) = "roInt")) or (Type(value) = "roFloat")) or (Type(value) = "roDouble") then return Str(value) else return (function(value)
                    if (Type(value) = "Boolean") or (Type(value) = "roBoolean") then return (function(value)
                        if value then return "true" else return "false"
                    end function)(value) else return value.toString()
                end function)(value)
            end function)(Str, value)
        end function)(Str, value)
    end function)(Str, value)
    checkBoundsIndexes_I_I_I_k_(startIndex, endIndex, stringCsq.get_length())
    m.set_string((substring_rStr_I_I_Str_k_(m.get_string(), 0, index) + substring_rStr_I_I_Str_k_(stringCsq, startIndex, endIndex)) + substring_rStr_I_Str_k_(m.get_string(), index))
    return m
end function

function StringBuilder_appendLine_StringBuilder_k_() as Object
    return m.append_C_StringBuilder_k_(chr(10))
end function

function StringBuilder_appendLine_CharSequenceN_StringBuilder_k_(value as Dynamic) as Object
    return m.append_CharSequenceN_StringBuilder_k_(value).appendLine_StringBuilder_k_()
end function

function StringBuilder_appendLine_StrN_StringBuilder_k_(value as Dynamic) as Object
    return m.append_StrN_StringBuilder_k_(value).appendLine_StringBuilder_k_()
end function

function StringBuilder_appendLine_AnyN_StringBuilder_k_(value as Dynamic) as Object
    return m.append_AnyN_StringBuilder_k_(value).appendLine_StringBuilder_k_()
end function

function StringBuilder_appendLine_C_StringBuilder_k_(value as Object) as Object
    return m.append_C_StringBuilder_k_(value).appendLine_StringBuilder_k_()
end function

function StringBuilder_appendLine_Z_StringBuilder_k_(value as Boolean) as Object
    return m.append_Z_StringBuilder_k_(value).appendLine_StringBuilder_k_()
end function

function StringBuilder_appendLine_I_StringBuilder_k_(value as Integer) as Object
    return m.append_I_StringBuilder_k_(value).appendLine_StringBuilder_k_()
end function

function StringBuilder_appendLine_S_StringBuilder_k_(value as Integer) as Object
    return m.append_I_StringBuilder_k_(value).appendLine_StringBuilder_k_()
end function

function StringBuilder_appendLine_B_StringBuilder_k_(value as Integer) as Object
    return m.append_I_StringBuilder_k_(value).appendLine_StringBuilder_k_()
end function

function StringBuilder_appendLine_J_StringBuilder_k_(value as LongInteger) as Object
    return m.append_J_StringBuilder_k_(value).appendLine_StringBuilder_k_()
end function

function StringBuilder_appendLine_F_StringBuilder_k_(value as Float) as Object
    return m.append_F_StringBuilder_k_(value).appendLine_StringBuilder_k_()
end function

function StringBuilder_appendLine_D_StringBuilder_k_(value as Double) as Object
    return m.append_D_StringBuilder_k_(value).appendLine_StringBuilder_k_()
end function

function StringBuilder_get_string_Str_k_() as String
    return m.string
end function

sub StringBuilder_set_string_Str_k_(value as String)
    m.string = value
end sub

function StringBuilder_get_length_I_k_() as Integer
    return m.get_string().get_length()
end function

function append_rStringBuilder_B_StringBuilder_k_(m as Object, value as Integer) as Object
    return m.append_B_StringBuilder_k_(value)
end function

function append_rStringBuilder_S_StringBuilder_k_(m as Object, value as Integer) as Object
    return m.append_S_StringBuilder_k_(value)
end function

function insert_rStringBuilder_I_B_StringBuilder_k_(m as Object, index as Integer, value as Integer) as Object
    return m.insert_I_B_StringBuilder_k_(index, value)
end function

function insert_rStringBuilder_I_S_StringBuilder_k_(m as Object, index as Integer, value as Integer) as Object
    return m.insert_I_S_StringBuilder_k_(index, value)
end function

function clear_rStringBuilder_StringBuilder_k_(m as Object) as Object
    return m.clear_StringBuilder_k_()
end function

sub set_rStringBuilder_I_C_k_(m as Object, index as Integer, value as Object)
    return
end sub

function setRange_rStringBuilder_I_I_Str_StringBuilder_k_(m as Object, startIndex as Integer, endIndex as Integer, value as String) as Object
    return m.setRange_I_I_Str_StringBuilder_k_(startIndex, endIndex, value)
end function

function deleteAt_rStringBuilder_I_StringBuilder_k_(m as Object, index as Integer) as Object
    return m.deleteAt_I_StringBuilder_k_(index)
end function

function deleteRange_rStringBuilder_I_I_StringBuilder_k_(m as Object, startIndex as Integer, endIndex as Integer) as Object
    return m.deleteRange_I_I_StringBuilder_k_(startIndex, endIndex)
end function

sub toCharArray_rStringBuilder_CharArray_I_I_I_k_(m as Object, destination as Object, destinationOffset = 0, startIndex = 0, endIndex = m.get_length())
    return
end sub

function appendRange_rStringBuilder_CharArray_I_I_StringBuilder_k_(m as Object, value as Object, startIndex as Integer, endIndex as Integer) as Object
    return m.appendRange_CharArray_I_I_StringBuilder_k_(value, startIndex, endIndex)
end function

function appendRange_rStringBuilder_CharSequence_I_I_StringBuilder_k_(m as Object, value as Object, startIndex as Integer, endIndex as Integer) as Object
    return m.appendRange_CharSequence_I_I_StringBuilder_k_(value, startIndex, endIndex)
end function

function insertRange_rStringBuilder_I_CharArray_I_I_StringBuilder_k_(m as Object, index as Integer, value as Object, startIndex as Integer, endIndex as Integer) as Object
    return m.insertRange_I_CharArray_I_I_StringBuilder_k_(index, value, startIndex, endIndex)
end function

function insertRange_rStringBuilder_I_CharSequence_I_I_StringBuilder_k_(m as Object, index as Integer, value as Object, startIndex as Integer, endIndex as Integer) as Object
    return m.insertRange_I_CharSequence_I_I_StringBuilder_k_(index, value, startIndex, endIndex)
end function

function appendLine_rStringBuilder_I_StringBuilder_k_(m as Object, value as Integer) as Object
    return m.append_I_StringBuilder_k_(value).appendLine_StringBuilder_k_()
end function

function appendLine_rStringBuilder_S_StringBuilder_k_(m as Object, value as Integer) as Object
    return m.append_I_StringBuilder_k_(value).appendLine_StringBuilder_k_()
end function

function appendLine_rStringBuilder_B_StringBuilder_k_(m as Object, value as Integer) as Object
    return m.append_I_StringBuilder_k_(value).appendLine_StringBuilder_k_()
end function

function appendLine_rStringBuilder_J_StringBuilder_k_(m as Object, value as LongInteger) as Object
    return m.append_J_StringBuilder_k_(value).appendLine_StringBuilder_k_()
end function

function appendLine_rStringBuilder_F_StringBuilder_k_(m as Object, value as Float) as Object
    return m.append_F_StringBuilder_k_(value).appendLine_StringBuilder_k_()
end function

function appendLine_rStringBuilder_D_StringBuilder_k_(m as Object, value as Double) as Object
    return m.append_D_StringBuilder_k_(value).appendLine_StringBuilder_k_()
end function
