function RegexOption_create_Str_k_(__name as String, __ordinal as Integer, flag as String) as Object
    this = {}
    this.__type = "RegexOption"
    this.name = __name
    this.ordinal = __ordinal
    this.__proto = ["RegexOption", "Comparable"]
    this.__id = __kotlin_nextObjectId()
    this.flag = flag
    this.__get_flag = RegexOption___get_flag_k_
    return this
end function

sub RegexOption_initEntries()
    if m.RegexOption_entriesInitialized = true then
        return
    end if
    m.RegexOption_entriesInitialized = true
    m.RegexOption_IGNORE_CASE = RegexOption_create_Str_k_("IGNORE_CASE", 0, "i")
    m.RegexOption_MULTILINE = RegexOption_create_Str_k_("MULTILINE", 1, "m")
end sub

function RegexOption_values() as Object
    RegexOption_initEntries()
    return [m.RegexOption_IGNORE_CASE, m.RegexOption_MULTILINE]
end function

function RegexOption_valueOf(name as String) as Object
    RegexOption_initEntries()
    if name = "IGNORE_CASE" then
        return m.RegexOption_IGNORE_CASE
    else if name = "MULTILINE" then
        return m.RegexOption_MULTILINE
    else
        return invalid
    end if
end function

function MatchGroup_create_Str_k_(value as String) as Object
    this = {}
    this.__type = "MatchGroup"
    this.__proto = ["MatchGroup"]
    this.__id = __kotlin_nextObjectId()
    this.value = value
    this.equals = MatchGroup_equals
    this.hashCode = MatchGroup_hashCode
    this.toString = MatchGroup_toString
    this.copy = MatchGroup_copy
    this.component1 = MatchGroup_component1
    return this
end function

function MatchGroup_equals(other as Object) as Boolean
    if Type(other) <> "roAssociativeArray" then
        return false
    end if
    if other.__type <> "MatchGroup" then
        return false
    end if
    if m.value <> other.value then
        return false
    end if
    return true
end function

function MatchGroup_hashCode() as Integer
    result = 1
    result = ((result * 31) + m.value)
    return result
end function

function MatchGroup_toString() as String
    return ("MatchGroup(value=" + m.value) + ")"
end function

function MatchGroup_copy(value = invalid) as Object
    if value = invalid then
        value = m.value
    end if
    return MatchGroup_create_Str_k_(value)
end function

function MatchGroup_component1() as String
    return m.value
end function

function Regex_create_Str_SetRegexOption_k_(pattern as String, options as Object) as Object
    this = {}
    this.__type = "Regex"
    this.__proto = ["Regex"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.matches_CharSequence_k_ = Regex_matches_CharSequence_k_
    this.containsMatchIn_CharSequence_k_ = Regex_containsMatchIn_CharSequence_k_
    this.matchEntire_CharSequence_k_ = Regex_matchEntire_CharSequence_k_
    this.matchAt_CharSequence_I_k_ = Regex_matchAt_CharSequence_I_k_
    this.matchesAt_CharSequence_I_k_ = Regex_matchesAt_CharSequence_I_k_
    this.find_CharSequence_I_k_ = Regex_find_CharSequence_I_k_
    this.findAll_CharSequence_I_k_ = Regex_findAll_CharSequence_I_k_
    this.replace_CharSequence_Str_k_ = Regex_replace_CharSequence_Str_k_
    this.replace_CharSequence_Function1MatchResultCharSequence_k_ = Regex_replace_CharSequence_Function1MatchResultCharSequence_k_
    this.replaceFirst_CharSequence_Str_k_ = Regex_replaceFirst_CharSequence_Str_k_
    this.split_CharSequence_I_k_ = Regex_split_CharSequence_I_k_
    this.splitToSequence_CharSequence_I_k_ = Regex_splitToSequence_CharSequence_I_k_
    this.toString_k_ = Regex_toString_k_
    this.toString = Regex_toString_k_
    this.createMatchResult_Str_Arr_I_k_ = Regex_createMatchResult_Str_Arr_I_k_
    this.__get_pattern = Regex___get_pattern_k_
    this.__get_options = Regex___get_options_k_
    this.__get_nativePattern = Regex___get_nativePattern_k_
    this.pattern = pattern
    this.options = toSet_rIterable_k_(options)
    this.nativePattern = CreateObject("roRegex", pattern, joinToString_v4kj63_k_(options, "", invalid, invalid, invalid, invalid, Regex_nativePattern_lambda_create_k_()))
    return this
end function

function Regex_create_Str_RegexOption_k_(pattern as String, option as Object) as Object
    return Regex_create_Str_SetRegexOption_k_(pattern, setOf_Arr_k_([option]))
end function

function Regex_create_Str_k_(pattern as String) as Object
    return Regex_create_Str_SetRegexOption_k_(pattern, emptySet_k_())
end function

function Regex_matches_CharSequence_k_(input as Object) as Boolean
    inputStr = toString_AnyN_k_(input)
    match = m.__get_nativePattern().Match(inputStr)
    tmp0 = match
    tmp_ret_0 = invalid

    while true
        this = tmp0
        tmp_ret_0 = (this.count() = 0)
        exit while
    end while
    if tmp_ret_0 then
        return false
    end if

    return match[0] = inputStr
end function

function Regex_containsMatchIn_CharSequence_k_(input as Object) as Boolean
    return m.__get_nativePattern().IsMatch(toString_AnyN_k_(input))
end function

function Regex_matchEntire_CharSequence_k_(input as Object) as Dynamic
    inputStr = toString_AnyN_k_(input)
    match = m.__get_nativePattern().Match(inputStr)
    tmp0 = match
    tmp_ret_0 = invalid

    while true
        this = tmp0
        tmp_ret_0 = (this.count() = 0)
        exit while
    end while
    if tmp_ret_0 then
        return invalid
    end if

    if match[0] <> inputStr then
        return invalid
    end if
    return m.createMatchResult_Str_Arr_I_k_(inputStr, match, 0)
end function

function Regex_matchAt_CharSequence_I_k_(input as Object, index as Integer) as Dynamic
    if (index < 0) or (index > __kotlin_charSequenceLength_CharSequenceN_k_(input)) then
        throw IndexOutOfBoundsException_create_StrN_k_((("index out of bounds: " + __kotlin_numToStr_I_k_(index)) + ", input length: ") + __kotlin_numToStr_I_k_(__kotlin_charSequenceLength_CharSequenceN_k_(input)))
    end if
    inputStr = toString_AnyN_k_(input)
    substring = substring_rStr_I_k_(inputStr, index)
    match = m.__get_nativePattern().Match(substring)
    tmp0 = match
    tmp_ret_0 = invalid

    while true
        this = tmp0
        tmp_ret_0 = (this.count() = 0)
        exit while
    end while
    if tmp_ret_0 then
        return invalid
    end if

    matchIndex = indexOf_rStr_Str_I_Z_k_(substring, match[0], invalid, invalid)
    if matchIndex <> 0 then
        return invalid
    end if
    return m.createMatchResult_Str_Arr_I_k_(inputStr, match, index)
end function

function Regex_matchesAt_CharSequence_I_k_(input as Object, index as Integer) as Boolean
    return m.matchAt_CharSequence_I_k_(input, index) <> invalid
end function

function Regex_find_CharSequence_I_k_(input as Object, startIndex = 0) as Dynamic
    if startIndex = invalid then
        startIndex = 0
    end if
    if (startIndex < 0) or (startIndex > __kotlin_charSequenceLength_CharSequenceN_k_(input)) then
        throw IndexOutOfBoundsException_create_StrN_k_((("Start index out of bounds: " + __kotlin_numToStr_I_k_(startIndex)) + ", input length: ") + __kotlin_numToStr_I_k_(__kotlin_charSequenceLength_CharSequenceN_k_(input)))
    end if
    inputStr = toString_AnyN_k_(input)
    if startIndex = 0 then
        match = m.__get_nativePattern().Match(inputStr)
        tmp0 = match
        tmp_ret_0 = invalid

        while true
            this = tmp0
            tmp_ret_0 = (this.count() = 0)
            exit while
        end while
        if tmp_ret_0 then
            return invalid
        end if

        matchIndex = indexOf_rStr_Str_I_Z_k_(inputStr, match[0], invalid, invalid)
        if matchIndex < 0 then
            return invalid
        end if
        return m.createMatchResult_Str_Arr_I_k_(inputStr, match, matchIndex)
    else if true then
        substring = substring_rStr_I_k_(inputStr, startIndex)
        match = m.__get_nativePattern().Match(substring)
        tmp0_1 = match
        tmp_ret_1 = invalid

        while true
            this = tmp0_1
            tmp_ret_1 = (this.count() = 0)
            exit while
        end while
        if tmp_ret_1 then
            return invalid
        end if

        matchIndex = indexOf_rStr_Str_I_Z_k_(substring, match[0], invalid, invalid)
        if matchIndex < 0 then
            return invalid
        end if
        return m.createMatchResult_Str_Arr_I_k_(inputStr, match, startIndex + matchIndex)
    end if
end function

function Regex_findAll_CharSequence_I_k_(input as Object, startIndex = 0) as Object
    if startIndex = invalid then
        startIndex = 0
    end if
    if (startIndex < 0) or (startIndex > __kotlin_charSequenceLength_CharSequenceN_k_(input)) then
        throw IndexOutOfBoundsException_create_StrN_k_((("Start index out of bounds: " + __kotlin_numToStr_I_k_(startIndex)) + ", input length: ") + __kotlin_numToStr_I_k_(__kotlin_charSequenceLength_CharSequenceN_k_(input)))
    end if
    firstMatch = m.find_CharSequence_I_k_(input, startIndex)
    return generateSequence_AnyN_Function1_k_(firstMatch, Regex_findAll_lambda_create_k_())
end function

function Regex_replace_CharSequence_Str_k_(input as Object, replacement as String) as String
    if not contains_rStr_Str_Z_k_(replacement, "\", invalid) and not contains_rStr_Str_Z_k_(replacement, "$", invalid) then
        return m.__get_nativePattern().ReplaceAll(toString_AnyN_k_(input), replacement)
    end if
    return m.replace_CharSequence_Function1MatchResultCharSequence_k_(input, Regex_replace_lambda_create_Str_k_(replacement))
end function

function Regex_replace_CharSequence_Function1MatchResultCharSequence_k_(input as Object, transform as Object) as String
    match = m.find_CharSequence_I_k_(input, invalid)
    if match = invalid then
        return toString_AnyN_k_(input)
    end if
    lastStart = 0
    length = __kotlin_charSequenceLength_CharSequenceN_k_(input)
    sb = StringBuilder_create_I_k_(length)
    foundMatch = match
    sb.append_CharSequenceN_I_I_k_(input, lastStart, foundMatch.__get_range().__get_start())
    sb.append_CharSequenceN_k_(transform.invoke_AnyN_k_(foundMatch))
    lastStart = (foundMatch.__get_range().__get_endInclusive() + 1)
    match = foundMatch.next_k_()

    while (lastStart < length) and (match <> invalid)
        foundMatch = match
        sb.append_CharSequenceN_I_I_k_(input, lastStart, foundMatch.__get_range().__get_start())
        sb.append_CharSequenceN_k_(transform.invoke_AnyN_k_(foundMatch))
        lastStart = (foundMatch.__get_range().__get_endInclusive() + 1)
        match = foundMatch.next_k_()
    end while


    if lastStart < length then
        sb.append_CharSequenceN_I_I_k_(input, lastStart, length)
    end if
    return sb.toString()
end function

function Regex_replaceFirst_CharSequence_Str_k_(input as Object, replacement as String) as String
    tmp0_elvis_lhs = m.find_CharSequence_I_k_(input, invalid)
    __when_tmp0 = invalid
    if tmp0_elvis_lhs = invalid then
        return toString_AnyN_k_(input)
    else if true then
        __when_tmp0 = tmp0_elvis_lhs
    end if
    match = __when_tmp0

    if not contains_rStr_Str_Z_k_(replacement, "\", invalid) and not contains_rStr_Str_Z_k_(replacement, "$", invalid) then
        return m.__get_nativePattern().Replace(toString_AnyN_k_(input), replacement)
    end if
    inputStr = toString_AnyN_k_(input)
    sb = StringBuilder_create_k_()
    sb.append_StrN_k_(substring_rStr_I_I_k_(inputStr, 0, match.__get_range().__get_first()))
    sb.append_StrN_k_(substituteGroupRefs_MatchResult_Str_k_(match, replacement))
    sb.append_StrN_k_(substring_rStr_I_I_k_(inputStr, match.__get_range().__get_last() + 1, Len(inputStr)))
    return sb.toString()
end function

function Regex_split_CharSequence_I_k_(input as Object, limit = 0) as Object
    if limit = invalid then
        limit = 0
    end if
    requireNonNegativeLimit_I_k_(limit)
    allMatches = m.findAll_CharSequence_I_k_(input, invalid)
    __when_tmp1 = invalid
    if limit = 0 then
        __when_tmp1 = allMatches
    else if true then
        __when_tmp1 = take_rSequence_I_k_(allMatches, limit - 1)
    end if
    matches = __when_tmp1

    result = mutableListOf_k_()
    lastStart = 0
    __iter_82 = matches.iterator_k_()
    while __iter_82.hasNext_k_()
        match = __iter_82.next_k_()
        result.add_AnyN_k_(toString_AnyN_k_(input.subSequence_I_I_k_(lastStart, match.__get_range().__get_start())))
        lastStart = (match.__get_range().__get_endInclusive() + 1)

    end while

    result.add_AnyN_k_(toString_AnyN_k_(input.subSequence_I_I_k_(lastStart, __kotlin_charSequenceLength_CharSequenceN_k_(input))))
    return result
end function

function Regex_splitToSequence_CharSequence_I_k_(input as Object, limit = 0) as Object
    if limit = invalid then
        limit = 0
    end if
    requireNonNegativeLimit_I_k_(limit)
    return Sequence_Function0Iterator_k_(Regex_splitToSequence_lambda_create_Regex_CharSequence_I_k_(m, input, limit))
end function

function Regex_toString_k_() as String
    return ((("Regex(" + m.__get_pattern()) + ", options=") + m.__get_options().toString()) + ")"
end function

function Regex_createMatchResult_Str_Arr_I_k_(input as String, matchArray as Object, matchIndex as Integer) as Object
    return Anon_196d688f_create_I_Arr_Str_Regex_k_(matchIndex, matchArray, input, m)
end function

function Regex___get_pattern_k_() as String
    return m.pattern
end function

function Regex___get_options_k_() as Object
    return m.options
end function

function Regex___get_nativePattern_k_() as Object
    return m.nativePattern
end function

function Regex_Companion_create_k_() as Object
    this = {}
    this.__type = "Regex_Companion"
    this.__proto = ["Regex_Companion"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.fromLiteral_Str_k_ = Regex_Companion_fromLiteral_Str_k_
    this.escape_Str_k_ = Regex_Companion_escape_Str_k_
    this.escapeReplacement_Str_k_ = Regex_Companion_escapeReplacement_Str_k_
    return this
end function

function Regex_Companion_getInstance() as Object
    if GetGlobalAA().Regex_Companion_instance = invalid then
        GetGlobalAA().Regex_Companion_instance = Regex_Companion_create_k_()
    end if
    return GetGlobalAA().Regex_Companion_instance
end function

function Regex_Companion_fromLiteral_Str_k_(literal as String) as Object
    return Regex_create_Str_k_(Regex_Companion_getInstance().escape_Str_k_(literal))
end function

function Regex_Companion_escape_Str_k_(literal as String) as String
    sb = StringBuilder_create_k_()
    for each c in literal
        tmp0_subject = c
        if (((tmp0_subject = "\") or ((tmp0_subject = "^") or (tmp0_subject = "$"))) or (((tmp0_subject = "*") or (tmp0_subject = "+")) or ((tmp0_subject = "?") or (tmp0_subject = ".")))) or (((tmp0_subject = "(") or ((tmp0_subject = ")") or (tmp0_subject = "["))) or (((tmp0_subject = "]") or (tmp0_subject = "{")) or ((tmp0_subject = "}") or (tmp0_subject = "|")))) then
            sb.append_C_k_("\")
            sb.append_C_k_(c)
        else if true then
            sb.append_C_k_(c)
        end if

    end for
    return sb.toString()
end function

function Regex_Companion_escapeReplacement_Str_k_(literal as String) as String
    sb = StringBuilder_create_k_()
    for each c in literal
        tmp0_subject = c
        if (tmp0_subject = "\") or (tmp0_subject = "$") then
            sb.append_C_k_("\")
            sb.append_C_k_(c)
        else if true then
            sb.append_C_k_(c)
        end if

    end for
    return sb.toString()
end function

function substituteGroupRefs_MatchResult_Str_k_(match as Object, replacement as String) as String
    index = 0
    result = StringBuilder_create_k_()
    while index < Len(replacement)
        __incr_tmp_83 = index
        index = (__incr_tmp_83 + 1)

        char = Mid(replacement, __incr_tmp_83 + 1, 1)
        if char = "\" then
            if index = Len(replacement) then
                throw IllegalArgumentException_create_StrN_k_("The Char to be escaped is missing")
            end if
            __incr_tmp_84 = index
            index = (__incr_tmp_84 + 1)

            result.append_C_k_(Mid(replacement, __incr_tmp_84 + 1, 1))
        else if char = "$" then
            if index = Len(replacement) then
                throw IllegalArgumentException_create_StrN_k_("Capturing group index is missing")
            end if
            if Mid(replacement, index + 1, 1) = "{" then
                endIndex = readGroupName_rStr_I_k_(replacement, index)
                if index = endIndex then
                    throw IllegalArgumentException_create_StrN_k_("Named capturing group reference should have a non-empty name")
                end if
                if (endIndex = Len(replacement)) or (Mid(replacement, endIndex + 1, 1) <> "}") then
                    throw IllegalArgumentException_create_StrN_k_("Named capturing group reference is missing trailing '}'")
                end if
                throw UnsupportedOperationException_create_StrN_k_("Named capturing groups are not yet supported in BrightScript regex")
            else if true then
                if not rangeTo_rC_C_k_("0", "9").contains_Any_k_(Mid(replacement, index + 1, 1)) then
                    throw IllegalArgumentException_create_StrN_k_("Invalid capturing group reference")
                end if
                groups = match.__get_groups()
                endIndex = readGroupIndex_rStr_I_I_k_(replacement, index, groups.__get_size())
                groupIndexStr = substring_rStr_I_I_k_(replacement, index, endIndex)
                groupIndex = parseIntOrThrow_Str_Str_k_(groupIndexStr, "Invalid group index: " + groupIndexStr)
                if groupIndex >= groups.__get_size() then
                    throw IndexOutOfBoundsException_create_StrN_k_(("Group with index " + __kotlin_numToStr_I_k_(groupIndex)) + " does not exist")
                end if
                group = groups.get_I_k_(groupIndex)
                tmp0_safe_receiver = group
                __when_tmp4 = invalid
                if tmp0_safe_receiver = invalid then
                    __when_tmp4 = invalid
                else if true then
                    __when_tmp4 = tmp0_safe_receiver.value
                end if
                tmp1_elvis_lhs = __when_tmp4
                __when_tmp5 = invalid
                if tmp1_elvis_lhs = invalid then
                    __when_tmp5 = ""
                else if true then
                    __when_tmp5 = tmp1_elvis_lhs
                end if
                result.append_StrN_k_(__when_tmp5)

                index = endIndex
            end if
        else if true then
            result.append_C_k_(char)
        end if
    end while
    return result.toString()
end function

function readGroupName_rStr_I_k_(m as String, startIndex as Integer) as Integer
    index = startIndex
    while index < Len(m)
        if Mid(m, index + 1, 1) = "}" then
            exit while
        else if true then
            index = (index + 1)
        end if
    end while
    return index
end function

function readGroupIndex_rStr_I_I_k_(m as String, startIndex as Integer, groupCount as Integer) as Integer
    index = startIndex + 1
    groupIndex = Mid(m, startIndex + 1, 1) - "0"
    while (index < Len(m)) and rangeTo_rC_C_k_("0", "9").contains_Any_k_(Mid(m, index + 1, 1))
        newGroupIndex = (groupIndex * 10) + (Mid(m, index + 1, 1) - "0")
        if until_rI_I_k_(0, groupCount).contains_Any_k_(newGroupIndex) then
            groupIndex = newGroupIndex
            index = (index + 1)
        else if true then
            exit while
        end if
    end while
    return index
end function

sub requireNonNegativeLimit_I_k_(limit as Integer)
    tmp0 = limit >= 0
    tmp_ret_1 = invalid
    while true
        value = tmp0
        if not value then
            tmp_ret_0 = invalid
            while true
                tmp_ret_0 = (("Limit must be non-negative, but was " + __kotlin_numToStr_I_k_(limit)) + ".")
                exit while
            end while
            throw IllegalArgumentException_create_StrN_k_(toString_AnyN_k_(tmp_ret_0))
        end if
        tmp_ret_1 = invalid
        exit while
    end while

end sub

function parseIntOrThrow_Str_Str_k_(str as String, errorMessage as String) as Integer
    result = 0
    negative = false
    index = 0
    if isEmpty_rStr_k_(str) then
        throw NumberFormatException_create_StrN_k_(errorMessage)
    end if
    if Mid(str, 0 + 1, 1) = "-" then
        negative = true
        index = 1
    else if Mid(str, 0 + 1, 1) = "+" then
        index = 1
    end if
    if index >= Len(str) then
        throw NumberFormatException_create_StrN_k_(errorMessage)
    end if
    while index < Len(str)
        char = Mid(str, index + 1, 1)
        if (__kotlin_stringCompare(char, "0") < 0) or (__kotlin_stringCompare(char, "9") > 0) then
            throw NumberFormatException_create_StrN_k_(errorMessage)
        end if
        result = ((result * 10) + (char - "0"))
        index = (index + 1)
    end while
    __when_tmp6 = invalid
    if negative then
        __when_tmp6 = -result
    else if true then
        __when_tmp6 = result
    end if
    return __when_tmp6

end function

function Regex_findAll_lambda_create_k_() as Object
    this = {}
    this.__type = "Regex_findAll_lambda"
    this.__proto = ["Regex_findAll_lambda", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = Regex_findAll_lambda_invoke_AnyN_k_
    return this
end function

function Regex_findAll_lambda_invoke_AnyN_k_(match as Object) as Dynamic
    return match.next_k_()
end function

function Regex_replace_lambda_create_Str_k_(_replacement as String) as Object
    this = {}
    this.__type = "Regex_replace_lambda"
    this.__proto = ["Regex_replace_lambda", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = Regex_replace_lambda_invoke_AnyN_k_
    this._replacement = _replacement
    return this
end function

function Regex_replace_lambda_invoke_AnyN_k_(it as Object) as Object
    return substituteGroupRefs_MatchResult_Str_k_(it, m._replacement)
end function

function Anon_4f115513_create_Regex_CharSequence_I_k_(this_0 as Object, _input as Object, _limit as Integer) as Object
    this = {}
    this.__type = "Anon_4f115513"
    this.__proto = ["Anon_4f115513", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.hasNext_k_ = Anon_4f115513_hasNext_k_
    this.next_k_ = Anon_4f115513_next_k_
    this.__get_match = Anon_4f115513___get_match_k_
    this.__set_match = Anon_4f115513___set_match_MatchResultN_k_
    this.__get_firstMatch = Anon_4f115513___get_firstMatch_k_
    this.__get_nextStart = Anon_4f115513___get_nextStart_k_
    this.__set_nextStart = Anon_4f115513___set_nextStart_I_k_
    this.__get_splitCount = Anon_4f115513___get_splitCount_k_
    this.__set_splitCount = Anon_4f115513___set_splitCount_I_k_
    this.__get_emitted = Anon_4f115513___get_emitted_k_
    this.__set_emitted = Anon_4f115513___set_emitted_Z_k_
    this.match = this_0.find_CharSequence_I_k_(_input, invalid)
    this.firstMatch = this.__get_match()
    this.nextStart = 0
    this.splitCount = 0
    this.emitted = false
    this._input = _input
    this._limit = _limit
    return this
end function

function Anon_4f115513_hasNext_k_() as Boolean
    if not m.__get_emitted() and ((m.__get_firstMatch() = invalid) or (m._limit = 1)) then
        return true
    end if
    if (m._limit > 0) and (m.__get_splitCount() >= (m._limit - 1)) then
        return not m.__get_emitted() or (m.__get_nextStart() < __kotlin_charSequenceLength_CharSequenceN_k_(m._input))
    end if
    return (m.__get_nextStart() < __kotlin_charSequenceLength_CharSequenceN_k_(m._input)) or ((m.__get_match() <> invalid) and not m.__get_emitted())
end function

function Anon_4f115513_next_k_() as String
    if not m.hasNext_k_() then
        throw NoSuchElementException_create_k_()
    end if
    if not m.__get_emitted() and ((m.__get_firstMatch() = invalid) or (m._limit = 1)) then
        m.__set_emitted(true)
        return toString_AnyN_k_(m._input)
    end if
    if (m.__get_match() <> invalid) and ((m._limit = 0) or (m.__get_splitCount() < (m._limit - 1))) then
        foundMatch = m.__get_match()
        result = toString_AnyN_k_(m._input.subSequence_I_I_k_(m.__get_nextStart(), foundMatch.__get_range().__get_first()))
        m.__set_nextStart(foundMatch.__get_range().__get_endInclusive() + 1)
        m.__set_match(foundMatch.next_k_())
        m.__set_splitCount(m.__get_splitCount() + 1)
        m.__set_emitted(true)
        return result
    else if true then
        result = toString_AnyN_k_(m._input.subSequence_I_I_k_(m.__get_nextStart(), __kotlin_charSequenceLength_CharSequenceN_k_(m._input)))
        m.__set_nextStart(__kotlin_charSequenceLength_CharSequenceN_k_(m._input))
        m.__set_emitted(true)
        return result
    end if
end function

function Anon_4f115513___get_match_k_() as Dynamic
    return m.match
end function

sub Anon_4f115513___set_match_MatchResultN_k_(value as Dynamic)
    m.match = value
end sub

function Anon_4f115513___get_firstMatch_k_() as Dynamic
    return m.firstMatch
end function

function Anon_4f115513___get_nextStart_k_() as Integer
    return m.nextStart
end function

sub Anon_4f115513___set_nextStart_I_k_(value as Integer)
    m.nextStart = value
end sub

function Anon_4f115513___get_splitCount_k_() as Integer
    return m.splitCount
end function

sub Anon_4f115513___set_splitCount_I_k_(value as Integer)
    m.splitCount = value
end sub

function Anon_4f115513___get_emitted_k_() as Boolean
    return m.emitted
end function

sub Anon_4f115513___set_emitted_Z_k_(value as Boolean)
    m.emitted = value
end sub

function Regex_splitToSequence_lambda_create_Regex_CharSequence_I_k_(this_0 as Object, _input as Object, _limit as Integer) as Object
    this = {}
    this.__type = "Regex_splitToSequence_lambda"
    this.__proto = ["Regex_splitToSequence_lambda", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = Regex_splitToSequence_lambda_invoke_k_
    this.this_0 = this_0
    this._input = _input
    this._limit = _limit
    return this
end function

function Regex_splitToSequence_lambda_invoke_k_() as Object
    return Anon_4f115513_create_Regex_CharSequence_I_k_(m.this_0, m._input, m._limit)
end function

function Anon_2d998dd9_create_Anon_k_(this_0 as Object) as Object
    this = {}
    this.__type = "Anon_2d998dd9"
    this.__proto = ["Anon_2d998dd9", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.hasNext_k_ = Anon_2d998dd9_hasNext_k_
    this.next_k_ = Anon_2d998dd9_next_k_
    this.__get_index = Anon_2d998dd9___get_index_k_
    this.__set_index = Anon_2d998dd9___set_index_I_k_
    this.index = 0
    this.this_0 = this_0
    return this
end function

function Anon_2d998dd9_hasNext_k_() as Boolean
    return m.__get_index() < m.this_0.__get_size()
end function

function Anon_2d998dd9_next_k_() as Dynamic
    if not m.hasNext_k_() then
        throw NoSuchElementException_create_k_()
    end if
    __incr_tmp_85 = m.__get_index()
    m.__set_index(__incr_tmp_85 + 1)
    return m.this_0.get_I_k_(__incr_tmp_85)

end function

function Anon_2d998dd9___get_index_k_() as Integer
    return m.index
end function

sub Anon_2d998dd9___set_index_I_k_(value as Integer)
    m.index = value
end sub

function Anon_5d84478e_create_Anon_k_(this_0 as Object) as Object
    this = {}
    this.__type = "Anon_5d84478e"
    this.__proto = ["Anon_5d84478e", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.hasNext_k_ = Anon_5d84478e_hasNext_k_
    this.next_k_ = Anon_5d84478e_next_k_
    this.__get_index = Anon_5d84478e___get_index_k_
    this.__set_index = Anon_5d84478e___set_index_I_k_
    this.index = 0
    this.this_0 = this_0
    return this
end function

function Anon_5d84478e_hasNext_k_() as Boolean
    return m.__get_index() < m.this_0.__get_size()
end function

function Anon_5d84478e_next_k_() as String
    if not m.hasNext_k_() then
        throw NoSuchElementException_create_k_()
    end if
    __incr_tmp_86 = m.__get_index()
    m.__set_index(__incr_tmp_86 + 1)
    return m.this_0.get_I_k_(__incr_tmp_86)

end function

function Anon_5d84478e___get_index_k_() as Integer
    return m.index
end function

sub Anon_5d84478e___set_index_I_k_(value as Integer)
    m.index = value
end sub

function Anon_32bf3383_create_I_Anon_k_(_index as Integer, this_0 as Object) as Object
    this = {}
    this.__type = "Anon_32bf3383"
    this.__proto = ["Anon_32bf3383", "ListIterator", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.hasNext_k_ = Anon_32bf3383_hasNext_k_
    this.hasPrevious_k_ = Anon_32bf3383_hasPrevious_k_
    this.next_k_ = Anon_32bf3383_next_k_
    this.nextIndex_k_ = Anon_32bf3383_nextIndex_k_
    this.previous_k_ = Anon_32bf3383_previous_k_
    this.previousIndex_k_ = Anon_32bf3383_previousIndex_k_
    this.__get_currentIndex = Anon_32bf3383___get_currentIndex_k_
    this.__set_currentIndex = Anon_32bf3383___set_currentIndex_I_k_
    this.currentIndex = _index
    this.this_0 = this_0
    return this
end function

function Anon_32bf3383_hasNext_k_() as Boolean
    return m.__get_currentIndex() < m.this_0.__get_size()
end function

function Anon_32bf3383_hasPrevious_k_() as Boolean
    return m.__get_currentIndex() > 0
end function

function Anon_32bf3383_next_k_() as String
    if not m.hasNext_k_() then
        throw NoSuchElementException_create_k_()
    end if
    __incr_tmp_87 = m.__get_currentIndex()
    m.__set_currentIndex(__incr_tmp_87 + 1)
    return m.this_0.get_I_k_(__incr_tmp_87)

end function

function Anon_32bf3383_nextIndex_k_() as Integer
    return m.__get_currentIndex()
end function

function Anon_32bf3383_previous_k_() as String
    if not m.hasPrevious_k_() then
        throw NoSuchElementException_create_k_()
    end if
    return m.this_0.get_I_k_(m.__get_currentIndex())
end function

function Anon_32bf3383_previousIndex_k_() as Integer
    return m.__get_currentIndex() - 1
end function

function Anon_32bf3383___get_currentIndex_k_() as Integer
    return m.currentIndex
end function

sub Anon_32bf3383___set_currentIndex_I_k_(value as Integer)
    m.currentIndex = value
end sub

function Anon_62961891_create_Anon_k_(this_0 as Object) as Object
    this = {}
    this.__type = "Anon_62961891"
    this.__proto = ["Anon_62961891", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.hasNext_k_ = Anon_62961891_hasNext_k_
    this.next_k_ = Anon_62961891_next_k_
    this.__get_index = Anon_62961891___get_index_k_
    this.__set_index = Anon_62961891___set_index_I_k_
    this.index = 0
    this.this_0 = this_0
    return this
end function

function Anon_62961891_hasNext_k_() as Boolean
    return m.__get_index() < m.this_0.__get_size()
end function

function Anon_62961891_next_k_() as String
    if not m.hasNext_k_() then
        throw NoSuchElementException_create_k_()
    end if
    __incr_tmp_88 = m.__get_index()
    m.__set_index(__incr_tmp_88 + 1)
    return m.this_0.get_I_k_(__incr_tmp_88)

end function

function Anon_62961891___get_index_k_() as Integer
    return m.index
end function

sub Anon_62961891___set_index_I_k_(value as Integer)
    m.index = value
end sub

function Anon_11011848_create_I_Anon_k_(_index as Integer, this_0 as Object) as Object
    this = {}
    this.__type = "Anon_11011848"
    this.__proto = ["Anon_11011848", "ListIterator", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.hasNext_k_ = Anon_11011848_hasNext_k_
    this.hasPrevious_k_ = Anon_11011848_hasPrevious_k_
    this.next_k_ = Anon_11011848_next_k_
    this.nextIndex_k_ = Anon_11011848_nextIndex_k_
    this.previous_k_ = Anon_11011848_previous_k_
    this.previousIndex_k_ = Anon_11011848_previousIndex_k_
    this.__get_currentIndex = Anon_11011848___get_currentIndex_k_
    this.__set_currentIndex = Anon_11011848___set_currentIndex_I_k_
    this.currentIndex = _index
    this.this_0 = this_0
    return this
end function

function Anon_11011848_hasNext_k_() as Boolean
    return m.__get_currentIndex() < m.this_0.__get_size()
end function

function Anon_11011848_hasPrevious_k_() as Boolean
    return m.__get_currentIndex() > 0
end function

function Anon_11011848_next_k_() as String
    if not m.hasNext_k_() then
        throw NoSuchElementException_create_k_()
    end if
    __incr_tmp_89 = m.__get_currentIndex()
    m.__set_currentIndex(__incr_tmp_89 + 1)
    return m.this_0.get_I_k_(__incr_tmp_89)

end function

function Anon_11011848_nextIndex_k_() as Integer
    return m.__get_currentIndex()
end function

function Anon_11011848_previous_k_() as String
    if not m.hasPrevious_k_() then
        throw NoSuchElementException_create_k_()
    end if
    return m.this_0.get_I_k_(m.__get_currentIndex())
end function

function Anon_11011848_previousIndex_k_() as Integer
    return m.__get_currentIndex() - 1
end function

function Anon_11011848___get_currentIndex_k_() as Integer
    return m.currentIndex
end function

sub Anon_11011848___set_currentIndex_I_k_(value as Integer)
    m.currentIndex = value
end sub

function Anon_769dc41f_create_Anon_k_(this_0 as Object) as Object
    this = {}
    this.__type = "Anon_769dc41f"
    this.__proto = ["Anon_769dc41f", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.hasNext_k_ = Anon_769dc41f_hasNext_k_
    this.next_k_ = Anon_769dc41f_next_k_
    this.__get_index = Anon_769dc41f___get_index_k_
    this.__set_index = Anon_769dc41f___set_index_I_k_
    this.index = 0
    this.this_0 = this_0
    return this
end function

function Anon_769dc41f_hasNext_k_() as Boolean
    return m.__get_index() < m.this_0.__get_size()
end function

function Anon_769dc41f_next_k_() as String
    if not m.hasNext_k_() then
        throw NoSuchElementException_create_k_()
    end if
    __incr_tmp_90 = m.__get_index()
    m.__set_index(__incr_tmp_90 + 1)
    return m.this_0.get_I_k_(__incr_tmp_90)

end function

function Anon_769dc41f___get_index_k_() as Integer
    return m.index
end function

sub Anon_769dc41f___set_index_I_k_(value as Integer)
    m.index = value
end sub

function Anon_38fcd007_create_I_Anon_k_(_index as Integer, this_0 as Object) as Object
    this = {}
    this.__type = "Anon_38fcd007"
    this.__proto = ["Anon_38fcd007", "ListIterator", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.hasNext_k_ = Anon_38fcd007_hasNext_k_
    this.hasPrevious_k_ = Anon_38fcd007_hasPrevious_k_
    this.next_k_ = Anon_38fcd007_next_k_
    this.nextIndex_k_ = Anon_38fcd007_nextIndex_k_
    this.previous_k_ = Anon_38fcd007_previous_k_
    this.previousIndex_k_ = Anon_38fcd007_previousIndex_k_
    this.__get_currentIndex = Anon_38fcd007___get_currentIndex_k_
    this.__set_currentIndex = Anon_38fcd007___set_currentIndex_I_k_
    this.currentIndex = _index
    this.this_0 = this_0
    return this
end function

function Anon_38fcd007_hasNext_k_() as Boolean
    return m.__get_currentIndex() < m.this_0.__get_size()
end function

function Anon_38fcd007_hasPrevious_k_() as Boolean
    return m.__get_currentIndex() > 0
end function

function Anon_38fcd007_next_k_() as String
    if not m.hasNext_k_() then
        throw NoSuchElementException_create_k_()
    end if
    __incr_tmp_91 = m.__get_currentIndex()
    m.__set_currentIndex(__incr_tmp_91 + 1)
    return m.this_0.get_I_k_(__incr_tmp_91)

end function

function Anon_38fcd007_nextIndex_k_() as Integer
    return m.__get_currentIndex()
end function

function Anon_38fcd007_previous_k_() as String
    if not m.hasPrevious_k_() then
        throw NoSuchElementException_create_k_()
    end if
    return m.this_0.get_I_k_(m.__get_currentIndex())
end function

function Anon_38fcd007_previousIndex_k_() as Integer
    return m.__get_currentIndex() - 1
end function

function Anon_38fcd007___get_currentIndex_k_() as Integer
    return m.currentIndex
end function

sub Anon_38fcd007___set_currentIndex_I_k_(value as Integer)
    m.currentIndex = value
end sub

function Anon_692cc9cb_create_I_Arr_I_k_(_nestedSubSize as Integer, _matchArray as Object, _actualFromIndex as Integer) as Object
    this = {}
    this.__type = "Anon_692cc9cb"
    this.__proto = ["Anon_692cc9cb", "List", "Collection", "Iterable"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.get_I_k_ = Anon_692cc9cb_get_I_k_
    this.indexOf_AnyN_k_ = Anon_692cc9cb_indexOf_AnyN_k_
    this.lastIndexOf_AnyN_k_ = Anon_692cc9cb_lastIndexOf_AnyN_k_
    this.isEmpty_k_ = Anon_692cc9cb_isEmpty_k_
    this.contains_AnyN_k_ = Anon_692cc9cb_contains_AnyN_k_
    this.containsAll_Collection_k_ = Anon_692cc9cb_containsAll_Collection_k_
    this.iterator_k_ = Anon_692cc9cb_iterator_k_
    this.listIterator_k_ = Anon_692cc9cb_listIterator_k_
    this.listIterator_I_k_ = Anon_692cc9cb_listIterator_I_k_
    this.subList_I_I_k_ = Anon_692cc9cb_subList_I_I_k_
    this.__get_size = Anon_692cc9cb___get_size_k_
    this.size = _nestedSubSize
    this._matchArray = _matchArray
    this._actualFromIndex = _actualFromIndex
    return this
end function

function Anon_692cc9cb_get_I_k_(index as Integer) as String
    if (index < 0) or (index >= m.__get_size()) then
        throw IndexOutOfBoundsException_create_k_()
    end if
    return m._matchArray[m._actualFromIndex + index]
end function

function Anon_692cc9cb_indexOf_AnyN_k_(element as String) as Integer
    progression = until_rI_I_k_(0, m.__get_size())
    inductionVariable = progression.__get_first()
    last = progression.__get_last()
    if inductionVariable <= last then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if m.get_I_k_(i) = element then
            return i
        end if


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if m.get_I_k_(i) = element then
                return i
            end if

        end while

    end if

    return -1
end function

function Anon_692cc9cb_lastIndexOf_AnyN_k_(element as String) as Integer
    inductionVariable = m.__get_size() - 1
    if 0 <= inductionVariable then
                i = inductionVariable
        inductionVariable = (inductionVariable + -1)

        if m.get_I_k_(i) = element then
            return i
        end if


        while 0 <= inductionVariable
            i = inductionVariable
            inductionVariable = (inductionVariable + -1)

            if m.get_I_k_(i) = element then
                return i
            end if

        end while

    end if

    return -1
end function

function Anon_692cc9cb_isEmpty_k_() as Boolean
    return m.__get_size() = 0
end function

function Anon_692cc9cb_contains_AnyN_k_(element as String) as Boolean
    return m.indexOf_AnyN_k_(element) >= 0
end function

function Anon_692cc9cb_containsAll_Collection_k_(elements as Object) as Boolean
    __iter_92 = elements.iterator_k_()
    while __iter_92.hasNext_k_()
        element = __iter_92.next_k_()
        if not m.contains_AnyN_k_(element) then
            return false
        end if
    end while

    return true
end function

function Anon_692cc9cb_iterator_k_() as Object
    return Anon_769dc41f_create_Anon_k_(m)
end function

function Anon_692cc9cb_listIterator_k_() as Object
    return m.listIterator_I_k_(0)
end function

function Anon_692cc9cb_listIterator_I_k_(index as Integer) as Object
    if (index < 0) or (index > m.__get_size()) then
        throw IndexOutOfBoundsException_create_k_()
    end if
    return Anon_38fcd007_create_I_Anon_k_(index, m)
end function

function Anon_692cc9cb_subList_I_I_k_(fromIndex as Integer, toIndex as Integer) as Object
    if ((fromIndex < 0) or (toIndex > m.__get_size())) or (fromIndex > toIndex) then
        throw IndexOutOfBoundsException_create_k_()
    end if
    return emptyList_k_()
end function

function Anon_692cc9cb___get_size_k_() as Integer
    return m.size
end function

function Anon_6db6702c_create_I_Arr_I_k_(_subSize as Integer, _matchArray as Object, _outerFromIndex as Integer) as Object
    this = {}
    this.__type = "Anon_6db6702c"
    this.__proto = ["Anon_6db6702c", "List", "Collection", "Iterable"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.get_I_k_ = Anon_6db6702c_get_I_k_
    this.indexOf_AnyN_k_ = Anon_6db6702c_indexOf_AnyN_k_
    this.lastIndexOf_AnyN_k_ = Anon_6db6702c_lastIndexOf_AnyN_k_
    this.isEmpty_k_ = Anon_6db6702c_isEmpty_k_
    this.contains_AnyN_k_ = Anon_6db6702c_contains_AnyN_k_
    this.containsAll_Collection_k_ = Anon_6db6702c_containsAll_Collection_k_
    this.iterator_k_ = Anon_6db6702c_iterator_k_
    this.listIterator_k_ = Anon_6db6702c_listIterator_k_
    this.listIterator_I_k_ = Anon_6db6702c_listIterator_I_k_
    this.subList_I_I_k_ = Anon_6db6702c_subList_I_I_k_
    this.__get_size = Anon_6db6702c___get_size_k_
    this.size = _subSize
    this._matchArray = _matchArray
    this._outerFromIndex = _outerFromIndex
    return this
end function

function Anon_6db6702c_get_I_k_(index as Integer) as String
    if (index < 0) or (index >= m.__get_size()) then
        throw IndexOutOfBoundsException_create_k_()
    end if
    return m._matchArray[m._outerFromIndex + index]
end function

function Anon_6db6702c_indexOf_AnyN_k_(element as String) as Integer
    progression = until_rI_I_k_(0, m.__get_size())
    inductionVariable = progression.__get_first()
    last = progression.__get_last()
    if inductionVariable <= last then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if m.get_I_k_(i) = element then
            return i
        end if


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if m.get_I_k_(i) = element then
                return i
            end if

        end while

    end if

    return -1
end function

function Anon_6db6702c_lastIndexOf_AnyN_k_(element as String) as Integer
    inductionVariable = m.__get_size() - 1
    if 0 <= inductionVariable then
                i = inductionVariable
        inductionVariable = (inductionVariable + -1)

        if m.get_I_k_(i) = element then
            return i
        end if


        while 0 <= inductionVariable
            i = inductionVariable
            inductionVariable = (inductionVariable + -1)

            if m.get_I_k_(i) = element then
                return i
            end if

        end while

    end if

    return -1
end function

function Anon_6db6702c_isEmpty_k_() as Boolean
    return m.__get_size() = 0
end function

function Anon_6db6702c_contains_AnyN_k_(element as String) as Boolean
    return m.indexOf_AnyN_k_(element) >= 0
end function

function Anon_6db6702c_containsAll_Collection_k_(elements as Object) as Boolean
    __iter_93 = elements.iterator_k_()
    while __iter_93.hasNext_k_()
        element = __iter_93.next_k_()
        if not m.contains_AnyN_k_(element) then
            return false
        end if
    end while

    return true
end function

function Anon_6db6702c_iterator_k_() as Object
    return Anon_62961891_create_Anon_k_(m)
end function

function Anon_6db6702c_listIterator_k_() as Object
    return m.listIterator_I_k_(0)
end function

function Anon_6db6702c_listIterator_I_k_(index as Integer) as Object
    if (index < 0) or (index > m.__get_size()) then
        throw IndexOutOfBoundsException_create_k_()
    end if
    return Anon_11011848_create_I_Anon_k_(index, m)
end function

function Anon_6db6702c_subList_I_I_k_(fromIndex as Integer, toIndex as Integer) as Object
    if ((fromIndex < 0) or (toIndex > m.__get_size())) or (fromIndex > toIndex) then
        throw IndexOutOfBoundsException_create_k_()
    end if
    actualFromIndex = m._outerFromIndex + fromIndex
    actualToIndex = m._outerFromIndex + toIndex
    nestedSubSize = actualToIndex - actualFromIndex
    return Anon_692cc9cb_create_I_Arr_I_k_(nestedSubSize, m._matchArray, actualFromIndex)
end function

function Anon_6db6702c___get_size_k_() as Integer
    return m.size
end function

function Anon_2a46b68c_create_Arr_k_(_matchArray as Object) as Object
    this = {}
    this.__type = "Anon_2a46b68c"
    this.__proto = ["Anon_2a46b68c", "List", "Collection", "Iterable"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.get_I_k_ = Anon_2a46b68c_get_I_k_
    this.indexOf_AnyN_k_ = Anon_2a46b68c_indexOf_AnyN_k_
    this.lastIndexOf_AnyN_k_ = Anon_2a46b68c_lastIndexOf_AnyN_k_
    this.isEmpty_k_ = Anon_2a46b68c_isEmpty_k_
    this.contains_AnyN_k_ = Anon_2a46b68c_contains_AnyN_k_
    this.containsAll_Collection_k_ = Anon_2a46b68c_containsAll_Collection_k_
    this.iterator_k_ = Anon_2a46b68c_iterator_k_
    this.listIterator_k_ = Anon_2a46b68c_listIterator_k_
    this.listIterator_I_k_ = Anon_2a46b68c_listIterator_I_k_
    this.subList_I_I_k_ = Anon_2a46b68c_subList_I_I_k_
    this.__get_size = Anon_2a46b68c___get_size_k_
    this._matchArray = _matchArray
    return this
end function

function Anon_2a46b68c_get_I_k_(index as Integer) as String
    if (index < 0) or (index >= m._matchArray.count()) then
        throw IndexOutOfBoundsException_create_StrN_k_((("Index " + __kotlin_numToStr_I_k_(index)) + " out of bounds for size ") + __kotlin_numToStr_I_k_(m.__get_size()))
    end if
    return m._matchArray[index]
end function

function Anon_2a46b68c_indexOf_AnyN_k_(element as String) as Integer
    progression = until_rI_I_k_(0, m.__get_size())
    inductionVariable = progression.__get_first()
    last = progression.__get_last()
    if inductionVariable <= last then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if m._matchArray[i] = element then
            return i
        end if


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if m._matchArray[i] = element then
                return i
            end if

        end while

    end if

    return -1
end function

function Anon_2a46b68c_lastIndexOf_AnyN_k_(element as String) as Integer
    inductionVariable = m.__get_size() - 1
    if 0 <= inductionVariable then
                i = inductionVariable
        inductionVariable = (inductionVariable + -1)

        if m._matchArray[i] = element then
            return i
        end if


        while 0 <= inductionVariable
            i = inductionVariable
            inductionVariable = (inductionVariable + -1)

            if m._matchArray[i] = element then
                return i
            end if

        end while

    end if

    return -1
end function

function Anon_2a46b68c_isEmpty_k_() as Boolean
    return m.__get_size() = 0
end function

function Anon_2a46b68c_contains_AnyN_k_(element as String) as Boolean
    return m.indexOf_AnyN_k_(element) >= 0
end function

function Anon_2a46b68c_containsAll_Collection_k_(elements as Object) as Boolean
    __iter_94 = elements.iterator_k_()
    while __iter_94.hasNext_k_()
        element = __iter_94.next_k_()
        if not m.contains_AnyN_k_(element) then
            return false
        end if
    end while

    return true
end function

function Anon_2a46b68c_iterator_k_() as Object
    return Anon_5d84478e_create_Anon_k_(m)
end function

function Anon_2a46b68c_listIterator_k_() as Object
    return m.listIterator_I_k_(0)
end function

function Anon_2a46b68c_listIterator_I_k_(index as Integer) as Object
    if (index < 0) or (index > m.__get_size()) then
        throw IndexOutOfBoundsException_create_k_()
    end if
    return Anon_32bf3383_create_I_Anon_k_(index, m)
end function

function Anon_2a46b68c_subList_I_I_k_(fromIndex as Integer, toIndex as Integer) as Object
    if ((fromIndex < 0) or (toIndex > m.__get_size())) or (fromIndex > toIndex) then
        throw IndexOutOfBoundsException_create_k_()
    end if
    outerFromIndex = fromIndex
    subSize = toIndex - fromIndex
    return Anon_6db6702c_create_I_Arr_I_k_(subSize, m._matchArray, outerFromIndex)
end function

function Anon_2a46b68c___get_size_k_() as Integer
    return m._matchArray.count()
end function

function Anon_196d688f_create_I_Arr_Str_Regex_k_(_matchIndex as Integer, _matchArray as Object, _input as String, this_0 as Object) as Object
    this = {}
    this.__type = "Anon_196d688f"
    this.__proto = ["Anon_196d688f", "MatchResult"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.next_k_ = Anon_196d688f_next_k_
    this.__get_range = Anon_196d688f___get_range_k_
    this.__get_value = Anon_196d688f___get_value_k_
    this.__get_groups = Anon_196d688f___get_groups_k_
    this.__get_groupValues_ = Anon_196d688f___get_groupValues__k_
    this.__set_groupValues_ = Anon_196d688f___set_groupValues__ListStrN_k_
    this.__get_groupValues = Anon_196d688f___get_groupValues_k_
    this.range = until_rI_I_k_(_matchIndex, _matchIndex + Len(_matchArray[0]))
    this.value = _matchArray[0]
    this.groups = Anon_7b8babc7_create_Arr_k_(_matchArray)
    this.groupValues_ = invalid
    this._matchArray = _matchArray
    this._input = _input
    this.this_0 = this_0
    return this
end function

function Anon_196d688f_next_k_() as Dynamic
    __when_tmp3 = invalid
    if m.__get_range().isEmpty_k_() then
        __when_tmp3 = (m.__get_range().__get_start() + 1)
    else if true then
        __when_tmp3 = (m.__get_range().__get_endInclusive() + 1)
    end if
    nextStartIndex = __when_tmp3

    if nextStartIndex > Len(m._input) then
        return invalid
    end if
    return m.this_0.find_CharSequence_I_k_(m._input, nextStartIndex)
end function

function Anon_196d688f___get_range_k_() as Object
    return m.range
end function

function Anon_196d688f___get_value_k_() as String
    return m.value
end function

function Anon_196d688f___get_groups_k_() as Object
    return m.groups
end function

function Anon_196d688f___get_groupValues__k_() as Dynamic
    return m.groupValues_
end function

sub Anon_196d688f___set_groupValues__ListStrN_k_(value as Dynamic)
    m.groupValues_ = value
end sub

function Anon_196d688f___get_groupValues_k_() as Object
    if m.__get_groupValues_() = invalid then
        m.__set_groupValues_(Anon_2a46b68c_create_Arr_k_(m._matchArray))
    end if
    return m.__get_groupValues_()
end function
