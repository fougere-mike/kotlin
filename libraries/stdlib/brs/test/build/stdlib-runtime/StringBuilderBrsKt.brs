function StringBuilder_create_Str_k_(content as String) as Object
    this = {}
    this.__type = "StringBuilder"
    this.__proto = ["StringBuilder", "Appendable", "CharSequence"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.get_I_k_ = StringBuilder_get_I_k_
    this.subSequence_I_I_k_ = StringBuilder_subSequence_I_I_k_
    this.append_C_k_ = StringBuilder_append_C_k_
    this.append_CharSequenceN_k_ = StringBuilder_append_CharSequenceN_k_
    this.append_CharSequenceN_I_I_k_ = StringBuilder_append_CharSequenceN_I_I_k_
    this.reverse_k_ = StringBuilder_reverse_k_
    this.append_AnyN_k_ = StringBuilder_append_AnyN_k_
    this.append_Z_k_ = StringBuilder_append_Z_k_
    this.append_B_k_ = StringBuilder_append_B_k_
    this.append_S_k_ = StringBuilder_append_S_k_
    this.append_I_k_ = StringBuilder_append_I_k_
    this.append_J_k_ = StringBuilder_append_J_k_
    this.append_F_k_ = StringBuilder_append_F_k_
    this.append_D_k_ = StringBuilder_append_D_k_
    this.append_CharArray_k_ = StringBuilder_append_CharArray_k_
    this.append_StrN_k_ = StringBuilder_append_StrN_k_
    this.capacity_k_ = StringBuilder_capacity_k_
    this.ensureCapacity_I_k_ = StringBuilder_ensureCapacity_I_k_
    this.indexOf_Str_k_ = StringBuilder_indexOf_Str_k_
    this.indexOf_Str_I_k_ = StringBuilder_indexOf_Str_I_k_
    this.lastIndexOf_Str_k_ = StringBuilder_lastIndexOf_Str_k_
    this.lastIndexOf_Str_I_k_ = StringBuilder_lastIndexOf_Str_I_k_
    this.insert_I_Z_k_ = StringBuilder_insert_I_Z_k_
    this.insert_I_B_k_ = StringBuilder_insert_I_B_k_
    this.insert_I_S_k_ = StringBuilder_insert_I_S_k_
    this.insert_I_I_k_ = StringBuilder_insert_I_I_k_
    this.insert_I_J_k_ = StringBuilder_insert_I_J_k_
    this.insert_I_F_k_ = StringBuilder_insert_I_F_k_
    this.insert_I_D_k_ = StringBuilder_insert_I_D_k_
    this.insert_I_C_k_ = StringBuilder_insert_I_C_k_
    this.insert_I_CharArray_k_ = StringBuilder_insert_I_CharArray_k_
    this.insert_I_CharSequenceN_k_ = StringBuilder_insert_I_CharSequenceN_k_
    this.insert_I_AnyN_k_ = StringBuilder_insert_I_AnyN_k_
    this.insert_I_StrN_k_ = StringBuilder_insert_I_StrN_k_
    this.setLength_I_k_ = StringBuilder_setLength_I_k_
    this.substring_I_k_ = StringBuilder_substring_I_k_
    this.substring_I_I_k_ = StringBuilder_substring_I_I_k_
    this.trimToSize_k_ = StringBuilder_trimToSize_k_
    this.toString_k_ = StringBuilder_toString_k_
    this.toString = StringBuilder_toString_k_
    this.clear_k_ = StringBuilder_clear_k_
    this.set_I_C_k_ = StringBuilder_set_I_C_k_
    this.setRange_I_I_Str_k_ = StringBuilder_setRange_I_I_Str_k_
    this.checkReplaceRange_I_I_I_k_ = StringBuilder_checkReplaceRange_I_I_I_k_
    this.deleteAt_I_k_ = StringBuilder_deleteAt_I_k_
    this.deleteRange_I_I_k_ = StringBuilder_deleteRange_I_I_k_
    this.toCharArray_CharArray_I_I_I_k_ = StringBuilder_toCharArray_CharArray_I_I_I_k_
    this.appendRange_CharArray_I_I_k_ = StringBuilder_appendRange_CharArray_I_I_k_
    this.appendRange_CharSequence_I_I_k_ = StringBuilder_appendRange_CharSequence_I_I_k_
    this.insertRange_I_CharArray_I_I_k_ = StringBuilder_insertRange_I_CharArray_I_I_k_
    this.insertRange_I_CharSequence_I_I_k_ = StringBuilder_insertRange_I_CharSequence_I_I_k_
    this.appendLine_k_ = StringBuilder_appendLine_k_
    this.appendLine_CharSequenceN_k_ = StringBuilder_appendLine_CharSequenceN_k_
    this.appendLine_StrN_k_ = StringBuilder_appendLine_StrN_k_
    this.appendLine_AnyN_k_ = StringBuilder_appendLine_AnyN_k_
    this.appendLine_C_k_ = StringBuilder_appendLine_C_k_
    this.appendLine_Z_k_ = StringBuilder_appendLine_Z_k_
    this.appendLine_I_k_ = StringBuilder_appendLine_I_k_
    this.appendLine_S_k_ = StringBuilder_appendLine_S_k_
    this.appendLine_B_k_ = StringBuilder_appendLine_B_k_
    this.appendLine_J_k_ = StringBuilder_appendLine_J_k_
    this.appendLine_F_k_ = StringBuilder_appendLine_F_k_
    this.appendLine_D_k_ = StringBuilder_appendLine_D_k_
    this.__get_string = StringBuilder___get_string_k_
    this.__set_string = StringBuilder___set_string_Str_k_
    this.__get_length = StringBuilder___get_length_k_
    this.string = content
    return this
end function

function StringBuilder_create_I_k_(capacity as Integer) as Object
    return StringBuilder_create_k_()
end function

function StringBuilder_create_CharSequence_k_(content as Object) as Object
    return StringBuilder_create_Str_k_(toString_AnyN_k_(content))
end function

function StringBuilder_create_k_() as Object
    return StringBuilder_create_Str_k_("")
end function

function StringBuilder_get_I_k_(index as Integer) as Object
    return getOrElse_rStr_I_Function1IC_k_(m.__get_string(), index, StringBuilder_get_lambda_create_I_StringBuilder_k_(index, m))
end function

function StringBuilder_subSequence_I_I_k_(startIndex as Integer, endIndex as Integer) as Object
    return substring_rStr_I_I_k_(m.__get_string(), startIndex, endIndex)
end function

function StringBuilder_append_C_k_(value as Object) as Object
    m.__set_string(m.__get_string() + value)
    return m
end function

function StringBuilder_append_CharSequenceN_k_(value as Dynamic) as Object
    m.__set_string(m.__get_string() + toString_AnyN_k_(value))
    return m
end function

function StringBuilder_append_CharSequenceN_I_I_k_(value as Dynamic, startIndex as Integer, endIndex as Integer) as Object
    tmp0_elvis_lhs = value
    __when_tmp0 = invalid
    if tmp0_elvis_lhs = invalid then
        __when_tmp0 = "null"
    else if true then
        __when_tmp0 = tmp0_elvis_lhs
    end if
    return m.appendRange_CharSequence_I_I_k_(__when_tmp0, startIndex, endIndex)

end function

function StringBuilder_reverse_k_() as Object
    reversed = ""
    index = Len(m.__get_string()) - 1
    while index >= 0
        __incr_tmp_95 = index
        index = (__incr_tmp_95 - 1)

        low = Mid(m.__get_string(), __incr_tmp_95 + 1, 1)
        if isLowSurrogate_rC_k_(low) and (index >= 0) then
            __incr_tmp_96 = index
            index = (__incr_tmp_96 - 1)

            high = Mid(m.__get_string(), __incr_tmp_96 + 1, 1)
            if isHighSurrogate_rC_k_(high) then
                reversed = ((reversed + high) + low)
            else if true then
                reversed = ((reversed + low) + high)
            end if
        else if true then
            reversed = (reversed + low)
        end if
    end while
    m.__set_string(reversed)
    return m
end function

function StringBuilder_append_AnyN_k_(value as Dynamic) as Object
    m.__set_string(m.__get_string() + toString_AnyN_k_(value))
    return m
end function

function StringBuilder_append_Z_k_(value as Boolean) as Object
    m.__set_string(m.__get_string() + ((function(value)
        if value then return "true" else return "false"
    end function)(value)))
    return m
end function

function StringBuilder_append_B_k_(value as Integer) as Object
    return m.append_StrN_k_(__kotlin_numToStr_I_k_(value))
end function

function StringBuilder_append_S_k_(value as Integer) as Object
    return m.append_StrN_k_(__kotlin_numToStr_I_k_(value))
end function

function StringBuilder_append_I_k_(value as Integer) as Object
    return m.append_StrN_k_(__kotlin_numToStr_I_k_(value))
end function

function StringBuilder_append_J_k_(value as LongInteger) as Object
    return m.append_StrN_k_(__kotlin_numToStr_J_k_(value))
end function

function StringBuilder_append_F_k_(value as Float) as Object
    return m.append_StrN_k_(__kotlin_numToStr_F_k_(value))
end function

function StringBuilder_append_D_k_(value as Double) as Object
    return m.append_StrN_k_(__kotlin_numToStr_D_k_(value))
end function

function StringBuilder_append_CharArray_k_(value as Object) as Object
    m.__set_string(m.__get_string() + concatToString_rCharArray_k_(value))
    return m
end function

function StringBuilder_append_StrN_k_(value as Dynamic) as Object
    tmp0_elvis_lhs = value
    __when_tmp1 = invalid
    if tmp0_elvis_lhs = invalid then
        __when_tmp1 = "null"
    else if true then
        __when_tmp1 = tmp0_elvis_lhs
    end if
    m.__set_string(m.__get_string() + __when_tmp1)

    return m
end function

function StringBuilder_capacity_k_() as Integer
    return m.__get_length()
end function

sub StringBuilder_ensureCapacity_I_k_(minimumCapacity as Integer)
end sub

function StringBuilder_indexOf_Str_k_(string_ as String) as Integer
    return indexOf_rStr_Str_I_Z_k_(m.__get_string(), string_, invalid, invalid)
end function

function StringBuilder_indexOf_Str_I_k_(string_ as String, startIndex as Integer) as Integer
    return indexOf_rStr_Str_I_Z_k_(m.__get_string(), string_, startIndex, invalid)
end function

function StringBuilder_lastIndexOf_Str_k_(string_ as String) as Integer
    return lastIndexOf_rStr_Str_I_Z_k_(m.__get_string(), string_, invalid, invalid)
end function

function StringBuilder_lastIndexOf_Str_I_k_(string_ as String, startIndex as Integer) as Integer
    if isEmpty_rStr_k_(string_) and (startIndex < 0) then
        return -1
    end if
    return lastIndexOf_rStr_Str_I_Z_k_(m.__get_string(), string_, startIndex, invalid)
end function

function StringBuilder_insert_I_Z_k_(index as Integer, value as Boolean) as Object
    checkPositionIndex_I_I_k_(index, m.__get_length())
    m.__set_string((substring_rStr_I_I_k_(m.__get_string(), 0, index) + ((function(value)
        if value then return "true" else return "false"
    end function)(value))) + substring_rStr_I_k_(m.__get_string(), index))
    return m
end function

function StringBuilder_insert_I_B_k_(index as Integer, value as Integer) as Object
    return m.insert_I_StrN_k_(index, __kotlin_numToStr_I_k_(value))
end function

function StringBuilder_insert_I_S_k_(index as Integer, value as Integer) as Object
    return m.insert_I_StrN_k_(index, __kotlin_numToStr_I_k_(value))
end function

function StringBuilder_insert_I_I_k_(index as Integer, value as Integer) as Object
    return m.insert_I_StrN_k_(index, __kotlin_numToStr_I_k_(value))
end function

function StringBuilder_insert_I_J_k_(index as Integer, value as LongInteger) as Object
    return m.insert_I_StrN_k_(index, __kotlin_numToStr_J_k_(value))
end function

function StringBuilder_insert_I_F_k_(index as Integer, value as Float) as Object
    return m.insert_I_StrN_k_(index, __kotlin_numToStr_F_k_(value))
end function

function StringBuilder_insert_I_D_k_(index as Integer, value as Double) as Object
    return m.insert_I_StrN_k_(index, __kotlin_numToStr_D_k_(value))
end function

function StringBuilder_insert_I_C_k_(index as Integer, value as Object) as Object
    checkPositionIndex_I_I_k_(index, m.__get_length())
    m.__set_string((substring_rStr_I_I_k_(m.__get_string(), 0, index) + value) + substring_rStr_I_k_(m.__get_string(), index))
    return m
end function

function StringBuilder_insert_I_CharArray_k_(index as Integer, value as Object) as Object
    checkPositionIndex_I_I_k_(index, m.__get_length())
    m.__set_string((substring_rStr_I_I_k_(m.__get_string(), 0, index) + concatToString_rCharArray_k_(value)) + substring_rStr_I_k_(m.__get_string(), index))
    return m
end function

function StringBuilder_insert_I_CharSequenceN_k_(index as Integer, value as Dynamic) as Object
    checkPositionIndex_I_I_k_(index, m.__get_length())
    m.__set_string((substring_rStr_I_I_k_(m.__get_string(), 0, index) + toString_AnyN_k_(value)) + substring_rStr_I_k_(m.__get_string(), index))
    return m
end function

function StringBuilder_insert_I_AnyN_k_(index as Integer, value as Dynamic) as Object
    checkPositionIndex_I_I_k_(index, m.__get_length())
    m.__set_string((substring_rStr_I_I_k_(m.__get_string(), 0, index) + toString_AnyN_k_(value)) + substring_rStr_I_k_(m.__get_string(), index))
    return m
end function

function StringBuilder_insert_I_StrN_k_(index as Integer, value as Dynamic) as Object
    checkPositionIndex_I_I_k_(index, m.__get_length())
    tmp0_elvis_lhs = value
    __when_tmp2 = invalid
    if tmp0_elvis_lhs = invalid then
        __when_tmp2 = "null"
    else if true then
        __when_tmp2 = tmp0_elvis_lhs
    end if
    toInsert = __when_tmp2

    m.__set_string((substring_rStr_I_I_k_(m.__get_string(), 0, index) + toInsert) + substring_rStr_I_k_(m.__get_string(), index))
    return m
end function

sub StringBuilder_setLength_I_k_(newLength as Integer)
    if newLength < 0 then
        throw IllegalArgumentException_create_StrN_k_(("Negative new length: " + __kotlin_numToStr_I_k_(newLength)) + ".")
    end if
    if newLength <= m.__get_length() then
        m.__set_string(substring_rStr_I_I_k_(m.__get_string(), 0, newLength))
    else if true then
        progression = until_rI_I_k_(m.__get_length(), newLength)
        inductionVariable = progression.__get_first()
        last = progression.__get_last()
        if inductionVariable <= last then
                        i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            m.__set_string(m.__get_string() + " ")


            while i <> last
                i = inductionVariable
                inductionVariable = (inductionVariable + 1)

                m.__set_string(m.__get_string() + " ")

            end while

        end if
    end if
end sub

function StringBuilder_substring_I_k_(startIndex as Integer) as String
    checkPositionIndex_I_I_k_(startIndex, m.__get_length())
    return substring_rStr_I_k_(m.__get_string(), startIndex)
end function

function StringBuilder_substring_I_I_k_(startIndex as Integer, endIndex as Integer) as String
    checkBoundsIndexes_I_I_I_k_(startIndex, endIndex, m.__get_length())
    return substring_rStr_I_I_k_(m.__get_string(), startIndex, endIndex)
end function

sub StringBuilder_trimToSize_k_()
end sub

function StringBuilder_toString_k_() as String
    return m.__get_string()
end function

function StringBuilder_clear_k_() as Object
    m.__set_string("")
    return m
end function

sub StringBuilder_set_I_C_k_(index as Integer, value as Object)
    checkElementIndex_I_I_k_(index, m.__get_length())
    m.__set_string((substring_rStr_I_I_k_(m.__get_string(), 0, index) + value) + substring_rStr_I_k_(m.__get_string(), index + 1))
end sub

function StringBuilder_setRange_I_I_Str_k_(startIndex as Integer, endIndex as Integer, value as String) as Object
    m.checkReplaceRange_I_I_I_k_(startIndex, endIndex, m.__get_length())
    m.__set_string((substring_rStr_I_I_k_(m.__get_string(), 0, startIndex) + value) + substring_rStr_I_k_(m.__get_string(), endIndex))
    return m
end function

sub StringBuilder_checkReplaceRange_I_I_I_k_(startIndex as Integer, endIndex as Integer, length as Integer)
    if (startIndex < 0) or (startIndex > length) then
        throw IndexOutOfBoundsException_create_StrN_k_((("startIndex: " + __kotlin_numToStr_I_k_(startIndex)) + ", length: ") + __kotlin_numToStr_I_k_(length))
    end if
    if startIndex > endIndex then
        throw IllegalArgumentException_create_StrN_k_(((("startIndex(" + __kotlin_numToStr_I_k_(startIndex)) + ") > endIndex(") + __kotlin_numToStr_I_k_(endIndex)) + ")")
    end if
end sub

function StringBuilder_deleteAt_I_k_(index as Integer) as Object
    checkElementIndex_I_I_k_(index, m.__get_length())
    m.__set_string(substring_rStr_I_I_k_(m.__get_string(), 0, index) + substring_rStr_I_k_(m.__get_string(), index + 1))
    return m
end function

function StringBuilder_deleteRange_I_I_k_(startIndex as Integer, endIndex as Integer) as Object
    m.checkReplaceRange_I_I_I_k_(startIndex, endIndex, m.__get_length())
    m.__set_string(substring_rStr_I_I_k_(m.__get_string(), 0, startIndex) + substring_rStr_I_k_(m.__get_string(), endIndex))
    return m
end function

sub StringBuilder_toCharArray_CharArray_I_I_I_k_(destination as Object, destinationOffset = 0, startIndex = 0, endIndex = m.__get_length())
    if destinationOffset = invalid then
        destinationOffset = 0
    end if
    if startIndex = invalid then
        startIndex = 0
    end if
    if endIndex = invalid then
        endIndex = m.__get_length()
    end if
    checkBoundsIndexes_I_I_I_k_(startIndex, endIndex, m.__get_length())
    checkBoundsIndexes_I_I_I_k_(destinationOffset, (destinationOffset + endIndex) - startIndex, destination.__get_size())
    dstIndex = destinationOffset
    progression = until_rI_I_k_(startIndex, endIndex)
    inductionVariable = progression.__get_first()
    last = progression.__get_last()
    if inductionVariable <= last then
                index = inductionVariable
        inductionVariable = (inductionVariable + 1)

        __incr_tmp_97 = dstIndex
        dstIndex = (__incr_tmp_97 + 1)

        destination.set_I_C_k_(__incr_tmp_97, Mid(m.__get_string(), index + 1, 1))


        while index <> last
            index = inductionVariable
            inductionVariable = (inductionVariable + 1)

            __incr_tmp_97 = dstIndex
            dstIndex = (__incr_tmp_97 + 1)

            destination.set_I_C_k_(__incr_tmp_97, Mid(m.__get_string(), index + 1, 1))

        end while

    end if

end sub

function StringBuilder_appendRange_CharArray_I_I_k_(value as Object, startIndex as Integer, endIndex as Integer) as Object
    m.__set_string(m.__get_string() + concatToString_rCharArray_I_I_k_(value, startIndex, endIndex))
    return m
end function

function StringBuilder_appendRange_CharSequence_I_I_k_(value as Object, startIndex as Integer, endIndex as Integer) as Object
    stringCsq = toString_AnyN_k_(value)
    checkBoundsIndexes_I_I_I_k_(startIndex, endIndex, Len(stringCsq))
    m.__set_string(m.__get_string() + substring_rStr_I_I_k_(stringCsq, startIndex, endIndex))
    return m
end function

function StringBuilder_insertRange_I_CharArray_I_I_k_(index as Integer, value as Object, startIndex as Integer, endIndex as Integer) as Object
    checkPositionIndex_I_I_k_(index, m.__get_length())
    m.__set_string((substring_rStr_I_I_k_(m.__get_string(), 0, index) + concatToString_rCharArray_I_I_k_(value, startIndex, endIndex)) + substring_rStr_I_k_(m.__get_string(), index))
    return m
end function

function StringBuilder_insertRange_I_CharSequence_I_I_k_(index as Integer, value as Object, startIndex as Integer, endIndex as Integer) as Object
    checkPositionIndex_I_I_k_(index, m.__get_length())
    stringCsq = toString_AnyN_k_(value)
    checkBoundsIndexes_I_I_I_k_(startIndex, endIndex, Len(stringCsq))
    m.__set_string((substring_rStr_I_I_k_(m.__get_string(), 0, index) + substring_rStr_I_I_k_(stringCsq, startIndex, endIndex)) + substring_rStr_I_k_(m.__get_string(), index))
    return m
end function

function StringBuilder_appendLine_k_() as Object
    return m.append_C_k_(chr(10))
end function

function StringBuilder_appendLine_CharSequenceN_k_(value as Dynamic) as Object
    return m.append_CharSequenceN_k_(value).appendLine_k_()
end function

function StringBuilder_appendLine_StrN_k_(value as Dynamic) as Object
    return m.append_StrN_k_(value).appendLine_k_()
end function

function StringBuilder_appendLine_AnyN_k_(value as Dynamic) as Object
    return m.append_AnyN_k_(value).appendLine_k_()
end function

function StringBuilder_appendLine_C_k_(value as Object) as Object
    return m.append_C_k_(value).appendLine_k_()
end function

function StringBuilder_appendLine_Z_k_(value as Boolean) as Object
    return m.append_Z_k_(value).appendLine_k_()
end function

function StringBuilder_appendLine_I_k_(value as Integer) as Object
    return m.append_I_k_(value).appendLine_k_()
end function

function StringBuilder_appendLine_S_k_(value as Integer) as Object
    return m.append_I_k_(value).appendLine_k_()
end function

function StringBuilder_appendLine_B_k_(value as Integer) as Object
    return m.append_I_k_(value).appendLine_k_()
end function

function StringBuilder_appendLine_J_k_(value as LongInteger) as Object
    return m.append_J_k_(value).appendLine_k_()
end function

function StringBuilder_appendLine_F_k_(value as Float) as Object
    return m.append_F_k_(value).appendLine_k_()
end function

function StringBuilder_appendLine_D_k_(value as Double) as Object
    return m.append_D_k_(value).appendLine_k_()
end function

function StringBuilder___get_string_k_() as String
    return m.string
end function

sub StringBuilder___set_string_Str_k_(value as String)
    m.string = value
end sub

function StringBuilder___get_length_k_() as Integer
    return Len(m.__get_string())
end function

function append_rStringBuilder_B_k_(m as Object, value as Integer) as Object
    return m.append_B_k_(value)
end function

function append_rStringBuilder_S_k_(m as Object, value as Integer) as Object
    return m.append_S_k_(value)
end function

function insert_rStringBuilder_I_B_k_(m as Object, index as Integer, value as Integer) as Object
    return m.insert_I_B_k_(index, value)
end function

function insert_rStringBuilder_I_S_k_(m as Object, index as Integer, value as Integer) as Object
    return m.insert_I_S_k_(index, value)
end function

function clear_rStringBuilder_k_(m as Object) as Object
    return m.clear_k_()
end function

sub set_rStringBuilder_I_C_k_(m as Object, index as Integer, value as Object)
    m.set_I_C_k_(index, value)
    return

end sub

function setRange_rStringBuilder_I_I_Str_k_(m as Object, startIndex as Integer, endIndex as Integer, value as String) as Object
    return m.setRange_I_I_Str_k_(startIndex, endIndex, value)
end function

function deleteAt_rStringBuilder_I_k_(m as Object, index as Integer) as Object
    return m.deleteAt_I_k_(index)
end function

function deleteRange_rStringBuilder_I_I_k_(m as Object, startIndex as Integer, endIndex as Integer) as Object
    return m.deleteRange_I_I_k_(startIndex, endIndex)
end function

sub toCharArray_rStringBuilder_CharArray_I_I_I_k_(m as Object, destination as Object, destinationOffset = 0, startIndex = 0, endIndex = m.__get_length())
    if destinationOffset = invalid then
        destinationOffset = 0
    end if
    if startIndex = invalid then
        startIndex = 0
    end if
    if endIndex = invalid then
        endIndex = m.__get_length()
    end if
    m.toCharArray_CharArray_I_I_I_k_(destination, destinationOffset, startIndex, endIndex)
    return

end sub

function appendRange_rStringBuilder_CharArray_I_I_k_(m as Object, value as Object, startIndex as Integer, endIndex as Integer) as Object
    return m.appendRange_CharArray_I_I_k_(value, startIndex, endIndex)
end function

function appendRange_rStringBuilder_CharSequence_I_I_k_(m as Object, value as Object, startIndex as Integer, endIndex as Integer) as Object
    return m.appendRange_CharSequence_I_I_k_(value, startIndex, endIndex)
end function

function insertRange_rStringBuilder_I_CharArray_I_I_k_(m as Object, index as Integer, value as Object, startIndex as Integer, endIndex as Integer) as Object
    return m.insertRange_I_CharArray_I_I_k_(index, value, startIndex, endIndex)
end function

function insertRange_rStringBuilder_I_CharSequence_I_I_k_(m as Object, index as Integer, value as Object, startIndex as Integer, endIndex as Integer) as Object
    return m.insertRange_I_CharSequence_I_I_k_(index, value, startIndex, endIndex)
end function

function appendLine_rStringBuilder_I_k_(m as Object, value as Integer) as Object
    return m.append_I_k_(value).appendLine_k_()
end function

function appendLine_rStringBuilder_S_k_(m as Object, value as Integer) as Object
    return m.append_I_k_(value).appendLine_k_()
end function

function appendLine_rStringBuilder_B_k_(m as Object, value as Integer) as Object
    return m.append_I_k_(value).appendLine_k_()
end function

function appendLine_rStringBuilder_J_k_(m as Object, value as LongInteger) as Object
    return m.append_J_k_(value).appendLine_k_()
end function

function appendLine_rStringBuilder_F_k_(m as Object, value as Float) as Object
    return m.append_F_k_(value).appendLine_k_()
end function

function appendLine_rStringBuilder_D_k_(m as Object, value as Double) as Object
    return m.append_D_k_(value).appendLine_k_()
end function

function StringBuilder_get_lambda_create_I_StringBuilder_k_(_index as Integer, this_0 as Object) as Object
    this = {}
    this.__type = "StringBuilder_get_lambda"
    this.__proto = ["StringBuilder_get_lambda", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = StringBuilder_get_lambda_invoke_AnyN_k_
    this._index = _index
    this.this_0 = this_0
    return this
end function

function StringBuilder_get_lambda_invoke_AnyN_k_(it as Integer) as Object
    throw IndexOutOfBoundsException_create_StrN_k_(((("index: " + __kotlin_numToStr_I_k_(m._index)) + ", length: ") + __kotlin_numToStr_I_k_(m.this_0.__get_length())) + "}")
end function
