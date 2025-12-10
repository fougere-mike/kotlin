function RegexOption_create_Str_k_(__name as String, __ordinal as Integer, flag as String) as Object
    this = {}
    this.__type = "RegexOption"
    this.name = __name
    this.ordinal = __ordinal
    this.__proto = ["RegexOption", "Comparable"]
    this.__id = __kotlin_nextObjectId()
    this.flag = flag
    this.values_k_ = RegexOption_values_k_
    this.valueOf_Str_k_ = RegexOption_valueOf_Str_k_
    this.get_flag = RegexOption_get_flag_k_
    this.get_entries = RegexOption_get_entries_k_
    return this
end function

sub RegexOption_initEntries()
    if m.RegexOption_entriesInitialized then
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
    this.get_pattern = Regex_get_pattern_k_
    this.get_options = Regex_get_options_k_
    this.get_nativePattern = Regex_get_nativePattern_k_
    this.pattern = pattern
    this.options = toSet_rIterable_k_(options)
    this.nativePattern = CreateObject("roRegex", pattern, joinToString_v4kj63_k_(options, "", {invoke: function(it as Object) as Object
        return it.get_flag()
    end function}))
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
    match = m.get_nativePattern().Match_Str_k_(inputStr)
    if isEmpty_rArr_k_(match) then
        return false
    end if
    return match[0] = inputStr
end function

function Regex_containsMatchIn_CharSequence_k_(input as Object) as Boolean
    return m.get_nativePattern().IsMatch_Str_k_(toString_AnyN_k_(input))
end function

function Regex_matchEntire_CharSequence_k_(input as Object) as Dynamic
    inputStr = toString_AnyN_k_(input)
    match = m.get_nativePattern().Match_Str_k_(inputStr)
    if isEmpty_rArr_k_(match) then
        return invalid
    end if
    if match[0] <> inputStr then
        return invalid
    end if
    return m.createMatchResult_Str_Arr_I_k_(inputStr, match, 0)
end function

function Regex_matchAt_CharSequence_I_k_(input as Object, index as Integer) as Dynamic
    if (index < 0) or (index > Len(input)) then
        throw IndexOutOfBoundsException_create_StrN_k_((("index out of bounds: " + index) + ", input length: ") + Len(input))
    end if
    inputStr = toString_AnyN_k_(input)
    substring = substring_rStr_I_k_(inputStr, index)
    match = m.get_nativePattern().Match_Str_k_(substring)
    if isEmpty_rArr_k_(match) then
        return invalid
    end if
    matchIndex = indexOf_rStr_Str_I_Z_k_(substring, match[0])
    if matchIndex <> 0 then
        return invalid
    end if
    return m.createMatchResult_Str_Arr_I_k_(inputStr, match, index)
end function

function Regex_matchesAt_CharSequence_I_k_(input as Object, index as Integer) as Boolean
    return m.matchAt_CharSequence_I_k_(input, index) <> invalid
end function

function Regex_find_CharSequence_I_k_(input as Object, startIndex = 0) as Dynamic
    if (startIndex < 0) or (startIndex > Len(input)) then
        throw IndexOutOfBoundsException_create_StrN_k_((("Start index out of bounds: " + startIndex) + ", input length: ") + Len(input))
    end if
    inputStr = toString_AnyN_k_(input)
    if startIndex = 0 then
        match = m.get_nativePattern().Match_Str_k_(inputStr)
        if isEmpty_rArr_k_(match) then
            return invalid
        end if
        matchIndex = indexOf_rStr_Str_I_Z_k_(inputStr, match[0])
        if matchIndex < 0 then
            return invalid
        end if
        return m.createMatchResult_Str_Arr_I_k_(inputStr, match, matchIndex)
    else if true then
        substring = substring_rStr_I_k_(inputStr, startIndex)
        match = m.get_nativePattern().Match_Str_k_(substring)
        if isEmpty_rArr_k_(match) then
            return invalid
        end if
        matchIndex = indexOf_rStr_Str_I_Z_k_(substring, match[0])
        if matchIndex < 0 then
            return invalid
        end if
        return m.createMatchResult_Str_Arr_I_k_(inputStr, match, startIndex + matchIndex)
    end if
end function

function Regex_findAll_CharSequence_I_k_(input as Object, startIndex = 0) as Object
    if (startIndex < 0) or (startIndex > Len(input)) then
        throw IndexOutOfBoundsException_create_StrN_k_((("Start index out of bounds: " + startIndex) + ", input length: ") + Len(input))
    end if
    firstMatch = m.find_CharSequence_I_k_(input, startIndex)
    return generateSequence_AnyN_Function1_k_(firstMatch, {invoke: function(match as Object) as Dynamic
        return match.next_k_()
    end function})
end function

function Regex_replace_CharSequence_Str_k_(input as Object, replacement as String) as String
    if not contains_rStr_Str_Z_k_(replacement, "\") and not contains_rStr_Str_Z_k_(replacement, "$") then
        return m.get_nativePattern().ReplaceAll_Str_Str_k_(toString_AnyN_k_(input), replacement)
    end if
    return m.replace_CharSequence_Function1MatchResultCharSequence_k_(input, {replacement: replacement, invoke: function(it as Object) as Object
        return substituteGroupRefs_MatchResult_Str_k_(it, m.replacement)
    end function})
end function

function Regex_replace_CharSequence_Function1MatchResultCharSequence_k_(input as Object, transform as Object) as String
    match = m.find_CharSequence_I_k_(input)
    if match = invalid then
        return toString_AnyN_k_(input)
    end if
    lastStart = 0
    length = Len(input)
    sb = StringBuilder_create_I_k_(length)
    foundMatch = match
    sb.append_CharSequenceN_I_I_k_(input, lastStart, foundMatch.get_range().get_start())
    sb.append_CharSequenceN_k_(transform.invoke(foundMatch))
    lastStart = (foundMatch.get_range().get_endInclusive() + 1)
    match = foundMatch.next_k_()

    while (lastStart < length) and (match <> invalid)
        foundMatch = match
        sb.append_CharSequenceN_I_I_k_(input, lastStart, foundMatch.get_range().get_start())
        sb.append_CharSequenceN_k_(transform.invoke(foundMatch))
        lastStart = (foundMatch.get_range().get_endInclusive() + 1)
        match = foundMatch.next_k_()
    end while


    if lastStart < length then
        sb.append_CharSequenceN_I_I_k_(input, lastStart, length)
    end if
    return sb.toString()
end function

function Regex_replaceFirst_CharSequence_Str_k_(input as Object, replacement as String) as String
    tmp0_elvis_lhs = m.find_CharSequence_I_k_(input)
    __when_tmp0 = invalid
    if tmp0_elvis_lhs = invalid then
        return toString_AnyN_k_(input)
    else if true then
        __when_tmp0 = tmp0_elvis_lhs
    end if
    match = __when_tmp0

    if not contains_rStr_Str_Z_k_(replacement, "\") and not contains_rStr_Str_Z_k_(replacement, "$") then
        return m.get_nativePattern().Replace_Str_Str_k_(toString_AnyN_k_(input), replacement)
    end if
    inputStr = toString_AnyN_k_(input)
    sb = StringBuilder_create_k_()
    sb.append_StrN_k_(substring_rStr_I_I_k_(inputStr, 0, match.get_range().get_first()))
    sb.append_StrN_k_(substituteGroupRefs_MatchResult_Str_k_(match, replacement))
    sb.append_StrN_k_(substring_rStr_I_I_k_(inputStr, match.get_range().get_last() + 1, Len(inputStr)))
    return sb.toString()
end function

function Regex_split_CharSequence_I_k_(input as Object, limit = 0) as Object
    requireNonNegativeLimit_I_k_(limit)
    allMatches = m.findAll_CharSequence_I_k_(input)
    __when_tmp1 = invalid
    if limit = 0 then
        __when_tmp1 = allMatches
    else if true then
        __when_tmp1 = take_rSequence_I_k_(allMatches, limit - 1)
    end if
    matches = __when_tmp1

    result = mutableListOf_k_()
    lastStart = 0
    for each match in matches
        result.add_AnyN_k_(toString_AnyN_k_(input.subSequence_I_I_k_(lastStart, match.get_range().get_start())))
        lastStart = (match.get_range().get_endInclusive() + 1)

    end for
    result.add_AnyN_k_(toString_AnyN_k_(input.subSequence_I_I_k_(lastStart, Len(input))))
    return result
end function

function Regex_splitToSequence_CharSequence_I_k_(input as Object, limit = 0) as Object
    requireNonNegativeLimit_I_k_(limit)
    return Sequence_Function0Iterator_k_({this: m, input: input, this: m, this: m, value: value, this: m, this: m, this: m, this: m, value: value, this: m, this: m, value: value, this: m, this: m, value: value, this: m, limit: limit, this: m, invoke: function() as Object
        return Anon_3db36e64_create_k_()
    end function})
end function

function Regex_toString_k_() as String
    return ((("Regex(" + m.get_pattern()) + ", options=") + m.get_options()) + ")"
end function

function Regex_createMatchResult_Str_Arr_I_k_(input as String, matchArray as Object, matchIndex as Integer) as Object
    return Anon_3fbc770d_create_k_()
end function

function Regex_get_pattern_k_() as String
    return m.pattern
end function

function Regex_get_options_k_() as Object
    return m.options
end function

function Regex_get_nativePattern_k_() as Object
    return m.nativePattern
end function

function Regex_Companion_create_k_() as Object
    this = {}
    this.__type = "Regex_Companion"
    this.__proto = ["Regex_Companion"]
    this.__id = __kotlin_nextObjectId()
    this.fromLiteral_Str_k_ = Regex_Companion_fromLiteral_Str_k_
    this.escape_Str_k_ = Regex_Companion_escape_Str_k_
    this.escapeReplacement_Str_k_ = Regex_Companion_escapeReplacement_Str_k_
    return this
end function

function Regex_Companion_getInstance() as Object
    if m.Regex_Companion_instance = invalid then
        m.Regex_Companion_instance = Regex_Companion_create_k_()
    end if
    return m.Regex_Companion_instance
end function

function Regex_Companion_fromLiteral_Str_k_(literal as String) as Object
    return Regex_create_Str_k_(Regex_Companion_escape_Str_k_(literal))
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
        __incr_tmp_24 = index
        index = (__incr_tmp_24 + 1)

        char = replacement.get_I_k_(__incr_tmp_24)
        if char = "\" then
            if index = Len(replacement) then
                throw IllegalArgumentException_create_StrN_k_("The Char to be escaped is missing")
            end if
            __incr_tmp_25 = index
            index = (__incr_tmp_25 + 1)

            result.append_C_k_(replacement.get_I_k_(__incr_tmp_25))
        else if char = "$" then
            if index = Len(replacement) then
                throw IllegalArgumentException_create_StrN_k_("Capturing group index is missing")
            end if
            if replacement.get_I_k_(index) = "{" then
                endIndex = readGroupName_rStr_I_k_(replacement, index)
                if index = endIndex then
                    throw IllegalArgumentException_create_StrN_k_("Named capturing group reference should have a non-empty name")
                end if
                if (endIndex = Len(replacement)) or (replacement.get_I_k_(endIndex) <> "}") then
                    throw IllegalArgumentException_create_StrN_k_("Named capturing group reference is missing trailing '}'")
                end if
                throw UnsupportedOperationException_create_StrN_k_("Named capturing groups are not yet supported in BrightScript regex")
            else if true then
                if not rangeTo_rC_C_k_("0", "9").contains_C_k_(replacement.get_I_k_(index)) then
                    throw IllegalArgumentException_create_StrN_k_("Invalid capturing group reference")
                end if
                groups = match.get_groups()
                endIndex = readGroupIndex_rStr_I_I_k_(replacement, index, groups.get_size())
                groupIndexStr = substring_rStr_I_I_k_(replacement, index, endIndex)
                groupIndex = parseIntOrThrow_Str_Str_k_(groupIndexStr, "Invalid group index: " + groupIndexStr)
                if groupIndex >= groups.get_size() then
                    throw IndexOutOfBoundsException_create_StrN_k_(("Group with index " + groupIndex) + " does not exist")
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
        if m.get_I_k_(index) = "}" then
            exit while
        else if true then
            index = (index + 1)
        end if
    end while
    return index
end function

function readGroupIndex_rStr_I_I_k_(m as String, startIndex as Integer, groupCount as Integer) as Integer
    index = startIndex + 1
    groupIndex = m.get_I_k_(startIndex) - "0"
    while (index < Len(m)) and rangeTo_rC_C_k_("0", "9").contains_C_k_(m.get_I_k_(index))
        newGroupIndex = (groupIndex * 10) + (m.get_I_k_(index) - "0")
        if until_rI_I_k_(0, groupCount).contains_I_k_(newGroupIndex) then
            groupIndex = newGroupIndex
            index = (index + 1)
        else if true then
            exit while
        end if
    end while
    return index
end function

sub requireNonNegativeLimit_I_k_(limit as Integer)
    require_Z_Function0Any_k_(limit >= 0, {limit: limit, invoke: function() as Object
        return ("Limit must be non-negative, but was " + m.limit) + "."
    end function})
end sub

function parseIntOrThrow_Str_Str_k_(str as String, errorMessage as String) as Integer
    result = 0
    negative = false
    index = 0
    if isEmpty_rStr_k_(str) then
        throw NumberFormatException_create_StrN_k_(errorMessage)
    end if
    if str.get_I_k_(0) = "-" then
        negative = true
        index = 1
    else if str.get_I_k_(0) = "+" then
        index = 1
    end if
    if index >= Len(str) then
        throw NumberFormatException_create_StrN_k_(errorMessage)
    end if
    while index < Len(str)
        char = str.get_I_k_(index)
        if ((char < "0") < 0) or ((char > "9") > 0) then
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
