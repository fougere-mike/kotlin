function split_rCharSequence_Str_Z_I_ListStr_k_(m as Object, delimiter as String, ignoreCase = false, limit = 0) as Object
    if limit < 0 then
        throw IllegalArgumentException_create_StrN_IllegalArgumentException_k_("Limit must be non-negative, but was " + limit)
    end if
    str = toString_AnyN_Str_k_(m)
    if isEmpty_rStr_Z_k_(delimiter) then
        result = ArrayList_create_ArrayListAnyN_k_()
        __when_tmp0 = invalid
        if (limit = 0) or (limit > Len(str)) then
            __when_tmp0 = Len(str)
        else if true then
            __when_tmp0 = limit
        end if
        maxChars = __when_tmp0
        progression = until_rI_I_IntRange_k_(0, maxChars - 1)
        inductionVariable = progression.get_first()
        last = progression.get_last()
        if inductionVariable <= last then
                        i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            result.add_AnyN_Z_k_(str.get_I_C_k_(i).toString())


            while i <> last
                i = inductionVariable
                inductionVariable = (inductionVariable + 1)

                result.add_AnyN_Z_k_(str.get_I_C_k_(i).toString())

            end while

        end if
        if (maxChars > 0) and (maxChars <= Len(str)) then
            result.add_AnyN_Z_k_(substring_rStr_I_Str_k_(str, maxChars - 1))
        end if
        return result
    end if
    result = ArrayList_create_ArrayListAnyN_k_()
    currentIndex = 0
    matchCount = 0
    while currentIndex < Len(str)
        if (limit > 0) and (matchCount >= (limit - 1)) then
            result.add_AnyN_Z_k_(substring_rStr_I_Str_k_(str, currentIndex))
            exit while
        end if
        nextIndex = indexOf_rStr_Str_I_Z_I_k_(str, delimiter, currentIndex, ignoreCase)
        if nextIndex < 0 then
            result.add_AnyN_Z_k_(substring_rStr_I_Str_k_(str, currentIndex))
            exit while
        end if
        result.add_AnyN_Z_k_(substring_rStr_I_I_Str_k_(str, currentIndex, nextIndex))
        currentIndex = (nextIndex + Len(delimiter))
        matchCount = (matchCount + 1)
    end while
    if (currentIndex = Len(str)) and endsWith_rStr_Str_Z_Z_k_(str, delimiter, ignoreCase) then
        result.add_AnyN_Z_k_("")
    end if
    return result
end function

function split_rCharSequence_C_Z_I_ListStr_k_(m as Object, delimiter as Object, ignoreCase = false, limit = 0) as Object
    return split_rCharSequence_Str_Z_I_ListStr_k_(m, delimiter.toString(), ignoreCase, limit)
end function

function split_rCharSequence_CharArray_Z_I_ListStr_k_(m as Object, delimiters as Object, ignoreCase = false, limit = 0) as Object
    if limit < 0 then
        throw IllegalArgumentException_create_StrN_IllegalArgumentException_k_("Limit must be non-negative, but was " + limit)
    end if
    if delimiters.get_size() = 0 then
        return listOf_Arr_ListAnyN_k_([toString_AnyN_Str_k_(m)])
    end if
    str = toString_AnyN_Str_k_(m)
    result = ArrayList_create_ArrayListAnyN_k_()
    currentIndex = 0
    matchCount = 0
    while currentIndex < Len(str)
        if (limit > 0) and (matchCount >= (limit - 1)) then
            result.add_AnyN_Z_k_(substring_rStr_I_Str_k_(str, currentIndex))
            exit while
        end if
        nextIndex = -1
        matchedDelimiterLength = 1
        delimIndex = 0
        while delimIndex < delimiters.get_size()
            delimiter = delimiters.get_I_C_k_(delimIndex)
            index = indexOf_rStr_Str_I_Z_I_k_(str, delimiter.toString(), currentIndex, ignoreCase)
            if (index >= 0) and ((nextIndex < 0) or (index < nextIndex)) then
                nextIndex = index
            end if
            delimIndex = (delimIndex + 1)
        end while
        if nextIndex < 0 then
            result.add_AnyN_Z_k_(substring_rStr_I_Str_k_(str, currentIndex))
            exit while
        end if
        result.add_AnyN_Z_k_(substring_rStr_I_I_Str_k_(str, currentIndex, nextIndex))
        currentIndex = (nextIndex + matchedDelimiterLength)
        matchCount = (matchCount + 1)
    end while
    if currentIndex = Len(str) then
        delimIndex = 0
        while delimIndex < delimiters.get_size()
            delimiter = delimiters.get_I_C_k_(delimIndex)
            if endsWith_rStr_Str_Z_Z_k_(str, delimiter.toString(), ignoreCase) then
                result.add_AnyN_Z_k_("")
                exit while
            end if
            delimIndex = (delimIndex + 1)
        end while
    end if
    return result
end function

function split_rCharSequence_Arr_Z_I_ListStr_k_(m as Object, delimiters as Object, ignoreCase = false, limit = 0) as Object
    if limit < 0 then
        throw IllegalArgumentException_create_StrN_IllegalArgumentException_k_("Limit must be non-negative, but was " + limit)
    end if
    if delimiters.count() = 0 then
        return listOf_Arr_ListAnyN_k_([toString_AnyN_Str_k_(m)])
    end if
    str = toString_AnyN_Str_k_(m)
    result = ArrayList_create_ArrayListAnyN_k_()
    currentIndex = 0
    matchCount = 0
    while currentIndex < Len(str)
        if (limit > 0) and (matchCount >= (limit - 1)) then
            result.add_AnyN_Z_k_(substring_rStr_I_Str_k_(str, currentIndex))
            exit while
        end if
        nextIndex = -1
        matchedDelimiterLength = 0
        delimIndex = 0
        while delimIndex < delimiters.count()
            delimiter = delimiters[delimIndex]
            if isEmpty_rStr_Z_k_(delimiter) then
                delimIndex = (delimIndex + 1)
                ' continue not supported on target Roku OS
            end if
            index = indexOf_rStr_Str_I_Z_I_k_(str, delimiter, currentIndex, ignoreCase)
            if (index >= 0) and ((nextIndex < 0) or (index < nextIndex)) then
                nextIndex = index
                matchedDelimiterLength = Len(delimiter)
            end if
            delimIndex = (delimIndex + 1)
        end while
        if nextIndex < 0 then
            result.add_AnyN_Z_k_(substring_rStr_I_Str_k_(str, currentIndex))
            exit while
        end if
        result.add_AnyN_Z_k_(substring_rStr_I_I_Str_k_(str, currentIndex, nextIndex))
        currentIndex = (nextIndex + matchedDelimiterLength)
        matchCount = (matchCount + 1)
    end while
    if currentIndex = Len(str) then
        delimIndex = 0
        while delimIndex < delimiters.count()
            delimiter = delimiters[delimIndex]
            if endsWith_rStr_Str_Z_Z_k_(str, delimiter, ignoreCase) then
                result.add_AnyN_Z_k_("")
                exit while
            end if
            delimIndex = (delimIndex + 1)
        end while
    end if
    return result
end function

function lines_rCharSequence_ListStr_k_(m as Object) as Object
    return split_rCharSequence_Arr_Z_I_ListStr_k_(m, [chr(13) + chr(10), chr(10), chr(13)])
end function
