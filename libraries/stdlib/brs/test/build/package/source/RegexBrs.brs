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
    this.nativePattern = CreateObject("roRegex", pattern, joinToString_v4kj63_k_(options, "", invalid, invalid, invalid, invalid, {invoke: function(it as Object) as Object
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
        throw IndexOutOfBoundsException_create_StrN_k_((("index out of bounds: " + __kotlin_numToStr_I_k_(index)) + ", input length: ") + __kotlin_numToStr_I_k_(Len(input)))
    end if
    inputStr = toString_AnyN_k_(input)
    substring = substring_rStr_I_k_(inputStr, index)
    match = m.get_nativePattern().Match_Str_k_(substring)
    if isEmpty_rArr_k_(match) then
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
    if (startIndex < 0) or (startIndex > Len(input)) then
        throw IndexOutOfBoundsException_create_StrN_k_((("Start index out of bounds: " + __kotlin_numToStr_I_k_(startIndex)) + ", input length: ") + __kotlin_numToStr_I_k_(Len(input)))
    end if
    inputStr = toString_AnyN_k_(input)
    if startIndex = 0 then
        match = m.get_nativePattern().Match_Str_k_(inputStr)
        if isEmpty_rArr_k_(match) then
            return invalid
        end if
        matchIndex = indexOf_rStr_Str_I_Z_k_(inputStr, match[0], invalid, invalid)
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
    if (startIndex < 0) or (startIndex > Len(input)) then
        throw IndexOutOfBoundsException_create_StrN_k_((("Start index out of bounds: " + __kotlin_numToStr_I_k_(startIndex)) + ", input length: ") + __kotlin_numToStr_I_k_(Len(input)))
    end if
    firstMatch = m.find_CharSequence_I_k_(input, startIndex)
    return generateSequence_AnyN_Function1_k_(firstMatch, {invoke: function(match as Object) as Dynamic
        return match.next_k_()
    end function})
end function

function Regex_replace_CharSequence_Str_k_(input as Object, replacement as String) as String
    if not contains_rStr_Str_Z_k_(replacement, "\", invalid) and not contains_rStr_Str_Z_k_(replacement, "$", invalid) then
        return m.get_nativePattern().ReplaceAll_Str_Str_k_(toString_AnyN_k_(input), replacement)
    end if
    return m.replace_CharSequence_Function1MatchResultCharSequence_k_(input, {replacement: replacement, invoke: function(it as Object) as Object
        return substituteGroupRefs_MatchResult_Str_k_(it, m.replacement)
    end function})
end function

function Regex_replace_CharSequence_Function1MatchResultCharSequence_k_(input as Object, transform as Object) as String
    match = m.find_CharSequence_I_k_(input, invalid)
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
    tmp0_elvis_lhs = m.find_CharSequence_I_k_(input, invalid)
    __when_tmp0 = invalid
    if tmp0_elvis_lhs = invalid then
        return toString_AnyN_k_(input)
    else if true then
        __when_tmp0 = tmp0_elvis_lhs
    end if
    match = __when_tmp0

    if not contains_rStr_Str_Z_k_(replacement, "\", invalid) and not contains_rStr_Str_Z_k_(replacement, "$", invalid) then
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
    for each match in matches
        result.add_AnyN_k_(toString_AnyN_k_(input.subSequence_I_I_k_(lastStart, match.get_range().get_start())))
        lastStart = (match.get_range().get_endInclusive() + 1)

    end for
    result.add_AnyN_k_(toString_AnyN_k_(input.subSequence_I_I_k_(lastStart, Len(input))))
    return result
end function

function Regex_splitToSequence_CharSequence_I_k_(input as Object, limit = 0) as Object
    if limit = invalid then
        limit = 0
    end if
    requireNonNegativeLimit_I_k_(limit)
    return Sequence_Function0Iterator_k_({this: m, _input: _input, _limit: _limit, this_0: this_0, this: m, this: m, value: value, this: m, this: m, this: m, value: value, this: m, this: m, value: value, this: m, this: m, value: value, this: m, this: m, this: m, input: input, limit: limit, invoke: function() as Object
        return Anon_3f23f106_create_Regex_CharSequence_I_k_(m.this, m.input, m.limit)
    end function})
end function

function Regex_toString_k_() as String
    return ((("Regex(" + m.get_pattern()) + ", options=") + m.get_options().toString()) + ")"
end function

function Regex_createMatchResult_Str_Arr_I_k_(input as String, matchArray as Object, matchIndex as Integer) as Object
    return Anon_5afee63d_create_I_Arr_Str_Regex_k_(matchIndex, matchArray, input, m)
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
        __incr_tmp_27 = index
        index = (__incr_tmp_27 + 1)

        char = replacement.get_I_k_(__incr_tmp_27)
        if char = "\" then
            if index = Len(replacement) then
                throw IllegalArgumentException_create_StrN_k_("The Char to be escaped is missing")
            end if
            __incr_tmp_28 = index
            index = (__incr_tmp_28 + 1)

            result.append_C_k_(replacement.get_I_k_(__incr_tmp_28))
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
        return ("Limit must be non-negative, but was " + __kotlin_numToStr_I_k_(m.limit)) + "."
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

function Anon_3f23f106_create_Regex_CharSequence_I_k_(this_0 as Object, _input as Object, _limit as Integer) as Object
    this = {}
    this.__type = "Anon_3f23f106"
    this.__proto = ["Anon_3f23f106", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.hasNext_k_ = Anon_3f23f106_hasNext_k_
    this.next_k_ = Anon_3f23f106_next_k_
    this.get_match = Anon_3f23f106_get_match_k_
    this.set_match = Anon_3f23f106_set_match_MatchResultN_k_
    this.get_firstMatch = Anon_3f23f106_get_firstMatch_k_
    this.get_nextStart = Anon_3f23f106_get_nextStart_k_
    this.set_nextStart = Anon_3f23f106_set_nextStart_I_k_
    this.get_splitCount = Anon_3f23f106_get_splitCount_k_
    this.set_splitCount = Anon_3f23f106_set_splitCount_I_k_
    this.get_emitted = Anon_3f23f106_get_emitted_k_
    this.set_emitted = Anon_3f23f106_set_emitted_Z_k_
    this.match = this_0.find_CharSequence_I_k_(_input, invalid)
    this.firstMatch = this.get_match()
    this.nextStart = 0
    this.splitCount = 0
    this.emitted = false
    this._input = _input
    this._limit = _limit
    return this
end function

function Anon_3f23f106_hasNext_k_() as Boolean
    if not m.get_emitted() and ((m.get_firstMatch() = invalid) or (m._limit = 1)) then
        return true
    end if
    if (m._limit > 0) and (m.get_splitCount() >= (m._limit - 1)) then
        return not m.get_emitted() or (m.get_nextStart() < Len(m._input))
    end if
    return (m.get_nextStart() < Len(m._input)) or ((m.get_match() <> invalid) and not m.get_emitted())
end function

function Anon_3f23f106_next_k_() as String
    if not m.hasNext_k_() then
        throw NoSuchElementException_create_k_()
    end if
    if not m.get_emitted() and ((m.get_firstMatch() = invalid) or (m._limit = 1)) then
        m.set_emitted(true)
        return toString_AnyN_k_(m._input)
    end if
    if (m.get_match() <> invalid) and ((m._limit = 0) or (m.get_splitCount() < (m._limit - 1))) then
        foundMatch = m.get_match()
        result = toString_AnyN_k_(m._input.subSequence_I_I_k_(m.get_nextStart(), foundMatch.get_range().get_first()))
        m.set_nextStart(foundMatch.get_range().get_endInclusive() + 1)
        m.set_match(foundMatch.next_k_())
        m.set_splitCount(m.get_splitCount() + 1)
        m.set_emitted(true)
        return result
    else if true then
        result = toString_AnyN_k_(m._input.subSequence_I_I_k_(m.get_nextStart(), Len(m._input)))
        m.set_nextStart(Len(m._input))
        m.set_emitted(true)
        return result
    end if
end function

function Anon_3f23f106_get_match_k_() as Dynamic
    return m.match
end function

sub Anon_3f23f106_set_match_MatchResultN_k_(value as Dynamic)
    m.match = value
end sub

function Anon_3f23f106_get_firstMatch_k_() as Dynamic
    return m.firstMatch
end function

function Anon_3f23f106_get_nextStart_k_() as Integer
    return m.nextStart
end function

sub Anon_3f23f106_set_nextStart_I_k_(value as Integer)
    m.nextStart = value
end sub

function Anon_3f23f106_get_splitCount_k_() as Integer
    return m.splitCount
end function

sub Anon_3f23f106_set_splitCount_I_k_(value as Integer)
    m.splitCount = value
end sub

function Anon_3f23f106_get_emitted_k_() as Boolean
    return m.emitted
end function

sub Anon_3f23f106_set_emitted_Z_k_(value as Boolean)
    m.emitted = value
end sub

function Anon_49db5424_create_Anon_k_(this_0 as Object) as Object
    this = {}
    this.__type = "Anon_49db5424"
    this.__proto = ["Anon_49db5424", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.hasNext_k_ = Anon_49db5424_hasNext_k_
    this.next_k_ = Anon_49db5424_next_k_
    this.get_index = Anon_49db5424_get_index_k_
    this.set_index = Anon_49db5424_set_index_I_k_
    this.index = 0
    this.this_0 = this_0
    return this
end function

function Anon_49db5424_hasNext_k_() as Boolean
    return m.get_index() < m.this_0.get_size()
end function

function Anon_49db5424_next_k_() as Dynamic
    if not m.hasNext_k_() then
        throw NoSuchElementException_create_k_()
    end if
    __incr_tmp_29 = m.get_index()
    m.set_index(__incr_tmp_29 + 1)
    return m.this_0.get_I_k_(__incr_tmp_29)

end function

function Anon_49db5424_get_index_k_() as Integer
    return m.index
end function

sub Anon_49db5424_set_index_I_k_(value as Integer)
    m.index = value
end sub

function Anon_55266bab_create_Arr_k_(_matchArray as Object) as Object
    this = {}
    this.__type = "Anon_55266bab"
    this.__proto = ["Anon_55266bab", "MatchGroupCollection", "Collection", "Iterable"]
    this.__id = __kotlin_nextObjectId()
    this.iterator_k_ = Anon_55266bab_iterator_k_
    this.get_I_k_ = Anon_55266bab_get_I_k_
    this.contains_MatchGroupN_k_ = Anon_55266bab_contains_MatchGroupN_k_
    this.containsAll_CollectionMatchGroupN_k_ = Anon_55266bab_containsAll_CollectionMatchGroupN_k_
    this.isEmpty_k_ = Anon_55266bab_isEmpty_k_
    this.get_size = Anon_55266bab_get_size_k_
    this._matchArray = _matchArray
    return this
end function

function Anon_55266bab_iterator_k_() as Object
    return Anon_49db5424_create_Anon_k_(m)
end function

function Anon_55266bab_get_I_k_(index as Integer) as Dynamic
    if index >= m._matchArray.count() then
        return invalid
    end if
    groupValue = m._matchArray[index]
    __when_tmp2 = invalid
    if isEmpty_rStr_k_(groupValue) and (index > 0) then
        __when_tmp2 = invalid
    else if true then
        __when_tmp2 = MatchGroup_create_Str_k_(groupValue)
    end if
    return __when_tmp2

end function

function Anon_55266bab_contains_MatchGroupN_k_(element as Dynamic) as Boolean
    progression = until_rI_I_k_(0, m.get_size())
    inductionVariable = progression.get_first()
    last = progression.get_last()
    if inductionVariable <= last then
                i = inductionVariable
        inductionVariable = (inductionVariable + 1)

        if brsStructuralEquals_AnyN_AnyN_k_(m.get_I_k_(i), element) then
            return true
        end if


        while i <> last
            i = inductionVariable
            inductionVariable = (inductionVariable + 1)

            if brsStructuralEquals_AnyN_AnyN_k_(m.get_I_k_(i), element) then
                return true
            end if

        end while

    end if

    return false
end function

function Anon_55266bab_containsAll_CollectionMatchGroupN_k_(elements as Object) as Boolean
    __iter_30 = elements.iterator_k_()
    while __iter_30.hasNext_k_()
        element = __iter_30.next_k_()
        if not m.contains_MatchGroupN_k_(element) then
            return false
        end if
    end while

    return true
end function

function Anon_55266bab_isEmpty_k_() as Boolean
    return m.get_size() = 0
end function

function Anon_55266bab_get_size_k_() as Integer
    return m._matchArray.count()
end function

function Anon_49951c5f_create_Anon_k_(this_0 as Object) as Object
    this = {}
    this.__type = "Anon_49951c5f"
    this.__proto = ["Anon_49951c5f", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.hasNext_k_ = Anon_49951c5f_hasNext_k_
    this.next_k_ = Anon_49951c5f_next_k_
    this.get_index = Anon_49951c5f_get_index_k_
    this.set_index = Anon_49951c5f_set_index_I_k_
    this.index = 0
    this.this_0 = this_0
    return this
end function

function Anon_49951c5f_hasNext_k_() as Boolean
    return m.get_index() < m.this_0.get_size()
end function

function Anon_49951c5f_next_k_() as String
    if not m.hasNext_k_() then
        throw NoSuchElementException_create_k_()
    end if
    __incr_tmp_31 = m.get_index()
    m.set_index(__incr_tmp_31 + 1)
    return m.this_0.get_I_k_(__incr_tmp_31)

end function

function Anon_49951c5f_get_index_k_() as Integer
    return m.index
end function

sub Anon_49951c5f_set_index_I_k_(value as Integer)
    m.index = value
end sub

function Anon_6721ccca_create_I_Anon_k_(_index as Integer, this_0 as Object) as Object
    this = {}
    this.__type = "Anon_6721ccca"
    this.__proto = ["Anon_6721ccca", "ListIterator", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.hasNext_k_ = Anon_6721ccca_hasNext_k_
    this.hasPrevious_k_ = Anon_6721ccca_hasPrevious_k_
    this.next_k_ = Anon_6721ccca_next_k_
    this.nextIndex_k_ = Anon_6721ccca_nextIndex_k_
    this.previous_k_ = Anon_6721ccca_previous_k_
    this.previousIndex_k_ = Anon_6721ccca_previousIndex_k_
    this.get_currentIndex = Anon_6721ccca_get_currentIndex_k_
    this.set_currentIndex = Anon_6721ccca_set_currentIndex_I_k_
    this.currentIndex = _index
    this.this_0 = this_0
    return this
end function

function Anon_6721ccca_hasNext_k_() as Boolean
    return m.get_currentIndex() < m.this_0.get_size()
end function

function Anon_6721ccca_hasPrevious_k_() as Boolean
    return m.get_currentIndex() > 0
end function

function Anon_6721ccca_next_k_() as String
    if not m.hasNext_k_() then
        throw NoSuchElementException_create_k_()
    end if
    __incr_tmp_32 = m.get_currentIndex()
    m.set_currentIndex(__incr_tmp_32 + 1)
    return m.this_0.get_I_k_(__incr_tmp_32)

end function

function Anon_6721ccca_nextIndex_k_() as Integer
    return m.get_currentIndex()
end function

function Anon_6721ccca_previous_k_() as String
    if not m.hasPrevious_k_() then
        throw NoSuchElementException_create_k_()
    end if
    return m.this_0.get_I_k_(m.get_currentIndex())
end function

function Anon_6721ccca_previousIndex_k_() as Integer
    return m.get_currentIndex() - 1
end function

function Anon_6721ccca_get_currentIndex_k_() as Integer
    return m.currentIndex
end function

sub Anon_6721ccca_set_currentIndex_I_k_(value as Integer)
    m.currentIndex = value
end sub

function Anon_4682e0cf_create_Anon_k_(this_0 as Object) as Object
    this = {}
    this.__type = "Anon_4682e0cf"
    this.__proto = ["Anon_4682e0cf", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.hasNext_k_ = Anon_4682e0cf_hasNext_k_
    this.next_k_ = Anon_4682e0cf_next_k_
    this.get_index = Anon_4682e0cf_get_index_k_
    this.set_index = Anon_4682e0cf_set_index_I_k_
    this.index = 0
    this.this_0 = this_0
    return this
end function

function Anon_4682e0cf_hasNext_k_() as Boolean
    return m.get_index() < m.this_0.get_size()
end function

function Anon_4682e0cf_next_k_() as String
    if not m.hasNext_k_() then
        throw NoSuchElementException_create_k_()
    end if
    __incr_tmp_33 = m.get_index()
    m.set_index(__incr_tmp_33 + 1)
    return m.this_0.get_I_k_(__incr_tmp_33)

end function

function Anon_4682e0cf_get_index_k_() as Integer
    return m.index
end function

sub Anon_4682e0cf_set_index_I_k_(value as Integer)
    m.index = value
end sub

function Anon_4c606f8c_create_I_Anon_k_(_index as Integer, this_0 as Object) as Object
    this = {}
    this.__type = "Anon_4c606f8c"
    this.__proto = ["Anon_4c606f8c", "ListIterator", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.hasNext_k_ = Anon_4c606f8c_hasNext_k_
    this.hasPrevious_k_ = Anon_4c606f8c_hasPrevious_k_
    this.next_k_ = Anon_4c606f8c_next_k_
    this.nextIndex_k_ = Anon_4c606f8c_nextIndex_k_
    this.previous_k_ = Anon_4c606f8c_previous_k_
    this.previousIndex_k_ = Anon_4c606f8c_previousIndex_k_
    this.get_currentIndex = Anon_4c606f8c_get_currentIndex_k_
    this.set_currentIndex = Anon_4c606f8c_set_currentIndex_I_k_
    this.currentIndex = _index
    this.this_0 = this_0
    return this
end function

function Anon_4c606f8c_hasNext_k_() as Boolean
    return m.get_currentIndex() < m.this_0.get_size()
end function

function Anon_4c606f8c_hasPrevious_k_() as Boolean
    return m.get_currentIndex() > 0
end function

function Anon_4c606f8c_next_k_() as String
    if not m.hasNext_k_() then
        throw NoSuchElementException_create_k_()
    end if
    __incr_tmp_34 = m.get_currentIndex()
    m.set_currentIndex(__incr_tmp_34 + 1)
    return m.this_0.get_I_k_(__incr_tmp_34)

end function

function Anon_4c606f8c_nextIndex_k_() as Integer
    return m.get_currentIndex()
end function

function Anon_4c606f8c_previous_k_() as String
    if not m.hasPrevious_k_() then
        throw NoSuchElementException_create_k_()
    end if
    return m.this_0.get_I_k_(m.get_currentIndex())
end function

function Anon_4c606f8c_previousIndex_k_() as Integer
    return m.get_currentIndex() - 1
end function

function Anon_4c606f8c_get_currentIndex_k_() as Integer
    return m.currentIndex
end function

sub Anon_4c606f8c_set_currentIndex_I_k_(value as Integer)
    m.currentIndex = value
end sub

function Anon_48b36b5d_create_Anon_k_(this_0 as Object) as Object
    this = {}
    this.__type = "Anon_48b36b5d"
    this.__proto = ["Anon_48b36b5d", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.hasNext_k_ = Anon_48b36b5d_hasNext_k_
    this.next_k_ = Anon_48b36b5d_next_k_
    this.get_index = Anon_48b36b5d_get_index_k_
    this.set_index = Anon_48b36b5d_set_index_I_k_
    this.index = 0
    this.this_0 = this_0
    return this
end function

function Anon_48b36b5d_hasNext_k_() as Boolean
    return m.get_index() < m.this_0.get_size()
end function

function Anon_48b36b5d_next_k_() as String
    if not m.hasNext_k_() then
        throw NoSuchElementException_create_k_()
    end if
    __incr_tmp_35 = m.get_index()
    m.set_index(__incr_tmp_35 + 1)
    return m.this_0.get_I_k_(__incr_tmp_35)

end function

function Anon_48b36b5d_get_index_k_() as Integer
    return m.index
end function

sub Anon_48b36b5d_set_index_I_k_(value as Integer)
    m.index = value
end sub

function Anon_1563b6d3_create_I_Anon_k_(_index as Integer, this_0 as Object) as Object
    this = {}
    this.__type = "Anon_1563b6d3"
    this.__proto = ["Anon_1563b6d3", "ListIterator", "Iterator"]
    this.__id = __kotlin_nextObjectId()
    this.hasNext_k_ = Anon_1563b6d3_hasNext_k_
    this.hasPrevious_k_ = Anon_1563b6d3_hasPrevious_k_
    this.next_k_ = Anon_1563b6d3_next_k_
    this.nextIndex_k_ = Anon_1563b6d3_nextIndex_k_
    this.previous_k_ = Anon_1563b6d3_previous_k_
    this.previousIndex_k_ = Anon_1563b6d3_previousIndex_k_
    this.get_currentIndex = Anon_1563b6d3_get_currentIndex_k_
    this.set_currentIndex = Anon_1563b6d3_set_currentIndex_I_k_
    this.currentIndex = _index
    this.this_0 = this_0
    return this
end function

function Anon_1563b6d3_hasNext_k_() as Boolean
    return m.get_currentIndex() < m.this_0.get_size()
end function

function Anon_1563b6d3_hasPrevious_k_() as Boolean
    return m.get_currentIndex() > 0
end function

function Anon_1563b6d3_next_k_() as String
    if not m.hasNext_k_() then
        throw NoSuchElementException_create_k_()
    end if
    __incr_tmp_36 = m.get_currentIndex()
    m.set_currentIndex(__incr_tmp_36 + 1)
    return m.this_0.get_I_k_(__incr_tmp_36)

end function

function Anon_1563b6d3_nextIndex_k_() as Integer
    return m.get_currentIndex()
end function

function Anon_1563b6d3_previous_k_() as String
    if not m.hasPrevious_k_() then
        throw NoSuchElementException_create_k_()
    end if
    return m.this_0.get_I_k_(m.get_currentIndex())
end function

function Anon_1563b6d3_previousIndex_k_() as Integer
    return m.get_currentIndex() - 1
end function

function Anon_1563b6d3_get_currentIndex_k_() as Integer
    return m.currentIndex
end function

sub Anon_1563b6d3_set_currentIndex_I_k_(value as Integer)
    m.currentIndex = value
end sub

function Anon_3a646acd_create_I_Arr_I_k_(_nestedSubSize as Integer, _matchArray as Object, _actualFromIndex as Integer) as Object
    this = {}
    this.__type = "Anon_3a646acd"
    this.__proto = ["Anon_3a646acd", "List", "Collection", "Iterable"]
    this.__id = __kotlin_nextObjectId()
    this.get_I_k_ = Anon_3a646acd_get_I_k_
    this.indexOf_Str_k_ = Anon_3a646acd_indexOf_Str_k_
    this.lastIndexOf_Str_k_ = Anon_3a646acd_lastIndexOf_Str_k_
    this.isEmpty_k_ = Anon_3a646acd_isEmpty_k_
    this.contains_Str_k_ = Anon_3a646acd_contains_Str_k_
    this.containsAll_CollectionStr_k_ = Anon_3a646acd_containsAll_CollectionStr_k_
    this.iterator_k_ = Anon_3a646acd_iterator_k_
    this.listIterator_k_ = Anon_3a646acd_listIterator_k_
    this.listIterator_I_k_ = Anon_3a646acd_listIterator_I_k_
    this.subList_I_I_k_ = Anon_3a646acd_subList_I_I_k_
    this.get_size = Anon_3a646acd_get_size_k_
    this.size = _nestedSubSize
    this._matchArray = _matchArray
    this._actualFromIndex = _actualFromIndex
    return this
end function

function Anon_3a646acd_get_I_k_(index as Integer) as String
    if (index < 0) or (index >= m.get_size()) then
        throw IndexOutOfBoundsException_create_k_()
    end if
    return m._matchArray[m._actualFromIndex + index]
end function

function Anon_3a646acd_indexOf_Str_k_(element as String) as Integer
    progression = until_rI_I_k_(0, m.get_size())
    inductionVariable = progression.get_first()
    last = progression.get_last()
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

function Anon_3a646acd_lastIndexOf_Str_k_(element as String) as Integer
    inductionVariable = m.get_size() - 1
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

function Anon_3a646acd_isEmpty_k_() as Boolean
    return m.get_size() = 0
end function

function Anon_3a646acd_contains_Str_k_(element as String) as Boolean
    return m.indexOf_Str_k_(element) >= 0
end function

function Anon_3a646acd_containsAll_CollectionStr_k_(elements as Object) as Boolean
    __iter_37 = elements.iterator_k_()
    while __iter_37.hasNext_k_()
        element = __iter_37.next_k_()
        if not m.contains_Str_k_(element) then
            return false
        end if
    end while

    return true
end function

function Anon_3a646acd_iterator_k_() as Object
    return Anon_48b36b5d_create_Anon_k_(m)
end function

function Anon_3a646acd_listIterator_k_() as Object
    return m.listIterator_I_k_(0)
end function

function Anon_3a646acd_listIterator_I_k_(index as Integer) as Object
    if (index < 0) or (index > m.get_size()) then
        throw IndexOutOfBoundsException_create_k_()
    end if
    return Anon_1563b6d3_create_I_Anon_k_(index, m)
end function

function Anon_3a646acd_subList_I_I_k_(fromIndex as Integer, toIndex as Integer) as Object
    if ((fromIndex < 0) or (toIndex > m.get_size())) or (fromIndex > toIndex) then
        throw IndexOutOfBoundsException_create_k_()
    end if
    return emptyList_k_()
end function

function Anon_3a646acd_get_size_k_() as Integer
    return m.size
end function

function Anon_28723bdb_create_I_Arr_I_k_(_subSize as Integer, _matchArray as Object, _outerFromIndex as Integer) as Object
    this = {}
    this.__type = "Anon_28723bdb"
    this.__proto = ["Anon_28723bdb", "List", "Collection", "Iterable"]
    this.__id = __kotlin_nextObjectId()
    this.get_I_k_ = Anon_28723bdb_get_I_k_
    this.indexOf_Str_k_ = Anon_28723bdb_indexOf_Str_k_
    this.lastIndexOf_Str_k_ = Anon_28723bdb_lastIndexOf_Str_k_
    this.isEmpty_k_ = Anon_28723bdb_isEmpty_k_
    this.contains_Str_k_ = Anon_28723bdb_contains_Str_k_
    this.containsAll_CollectionStr_k_ = Anon_28723bdb_containsAll_CollectionStr_k_
    this.iterator_k_ = Anon_28723bdb_iterator_k_
    this.listIterator_k_ = Anon_28723bdb_listIterator_k_
    this.listIterator_I_k_ = Anon_28723bdb_listIterator_I_k_
    this.subList_I_I_k_ = Anon_28723bdb_subList_I_I_k_
    this.get_size = Anon_28723bdb_get_size_k_
    this.size = _subSize
    this._matchArray = _matchArray
    this._outerFromIndex = _outerFromIndex
    return this
end function

function Anon_28723bdb_get_I_k_(index as Integer) as String
    if (index < 0) or (index >= m.get_size()) then
        throw IndexOutOfBoundsException_create_k_()
    end if
    return m._matchArray[m._outerFromIndex + index]
end function

function Anon_28723bdb_indexOf_Str_k_(element as String) as Integer
    progression = until_rI_I_k_(0, m.get_size())
    inductionVariable = progression.get_first()
    last = progression.get_last()
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

function Anon_28723bdb_lastIndexOf_Str_k_(element as String) as Integer
    inductionVariable = m.get_size() - 1
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

function Anon_28723bdb_isEmpty_k_() as Boolean
    return m.get_size() = 0
end function

function Anon_28723bdb_contains_Str_k_(element as String) as Boolean
    return m.indexOf_Str_k_(element) >= 0
end function

function Anon_28723bdb_containsAll_CollectionStr_k_(elements as Object) as Boolean
    __iter_38 = elements.iterator_k_()
    while __iter_38.hasNext_k_()
        element = __iter_38.next_k_()
        if not m.contains_Str_k_(element) then
            return false
        end if
    end while

    return true
end function

function Anon_28723bdb_iterator_k_() as Object
    return Anon_4682e0cf_create_Anon_k_(m)
end function

function Anon_28723bdb_listIterator_k_() as Object
    return m.listIterator_I_k_(0)
end function

function Anon_28723bdb_listIterator_I_k_(index as Integer) as Object
    if (index < 0) or (index > m.get_size()) then
        throw IndexOutOfBoundsException_create_k_()
    end if
    return Anon_4c606f8c_create_I_Anon_k_(index, m)
end function

function Anon_28723bdb_subList_I_I_k_(fromIndex as Integer, toIndex as Integer) as Object
    if ((fromIndex < 0) or (toIndex > m.get_size())) or (fromIndex > toIndex) then
        throw IndexOutOfBoundsException_create_k_()
    end if
    actualFromIndex = m._outerFromIndex + fromIndex
    actualToIndex = m._outerFromIndex + toIndex
    nestedSubSize = actualToIndex - actualFromIndex
    return Anon_3a646acd_create_I_Arr_I_k_(nestedSubSize, m._matchArray, actualFromIndex)
end function

function Anon_28723bdb_get_size_k_() as Integer
    return m.size
end function

function Anon_1b4b7aa5_create_Arr_k_(_matchArray as Object) as Object
    this = {}
    this.__type = "Anon_1b4b7aa5"
    this.__proto = ["Anon_1b4b7aa5", "List", "Collection", "Iterable"]
    this.__id = __kotlin_nextObjectId()
    this.get_I_k_ = Anon_1b4b7aa5_get_I_k_
    this.indexOf_Str_k_ = Anon_1b4b7aa5_indexOf_Str_k_
    this.lastIndexOf_Str_k_ = Anon_1b4b7aa5_lastIndexOf_Str_k_
    this.isEmpty_k_ = Anon_1b4b7aa5_isEmpty_k_
    this.contains_Str_k_ = Anon_1b4b7aa5_contains_Str_k_
    this.containsAll_CollectionStr_k_ = Anon_1b4b7aa5_containsAll_CollectionStr_k_
    this.iterator_k_ = Anon_1b4b7aa5_iterator_k_
    this.listIterator_k_ = Anon_1b4b7aa5_listIterator_k_
    this.listIterator_I_k_ = Anon_1b4b7aa5_listIterator_I_k_
    this.subList_I_I_k_ = Anon_1b4b7aa5_subList_I_I_k_
    this.get_size = Anon_1b4b7aa5_get_size_k_
    this._matchArray = _matchArray
    return this
end function

function Anon_1b4b7aa5_get_I_k_(index as Integer) as String
    if (index < 0) or (index >= m._matchArray.count()) then
        throw IndexOutOfBoundsException_create_StrN_k_((("Index " + __kotlin_numToStr_I_k_(index)) + " out of bounds for size ") + __kotlin_numToStr_I_k_(m.get_size()))
    end if
    return m._matchArray[index]
end function

function Anon_1b4b7aa5_indexOf_Str_k_(element as String) as Integer
    progression = until_rI_I_k_(0, m.get_size())
    inductionVariable = progression.get_first()
    last = progression.get_last()
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

function Anon_1b4b7aa5_lastIndexOf_Str_k_(element as String) as Integer
    inductionVariable = m.get_size() - 1
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

function Anon_1b4b7aa5_isEmpty_k_() as Boolean
    return m.get_size() = 0
end function

function Anon_1b4b7aa5_contains_Str_k_(element as String) as Boolean
    return m.indexOf_Str_k_(element) >= 0
end function

function Anon_1b4b7aa5_containsAll_CollectionStr_k_(elements as Object) as Boolean
    __iter_39 = elements.iterator_k_()
    while __iter_39.hasNext_k_()
        element = __iter_39.next_k_()
        if not m.contains_Str_k_(element) then
            return false
        end if
    end while

    return true
end function

function Anon_1b4b7aa5_iterator_k_() as Object
    return Anon_49951c5f_create_Anon_k_(m)
end function

function Anon_1b4b7aa5_listIterator_k_() as Object
    return m.listIterator_I_k_(0)
end function

function Anon_1b4b7aa5_listIterator_I_k_(index as Integer) as Object
    if (index < 0) or (index > m.get_size()) then
        throw IndexOutOfBoundsException_create_k_()
    end if
    return Anon_6721ccca_create_I_Anon_k_(index, m)
end function

function Anon_1b4b7aa5_subList_I_I_k_(fromIndex as Integer, toIndex as Integer) as Object
    if ((fromIndex < 0) or (toIndex > m.get_size())) or (fromIndex > toIndex) then
        throw IndexOutOfBoundsException_create_k_()
    end if
    outerFromIndex = fromIndex
    subSize = toIndex - fromIndex
    return Anon_28723bdb_create_I_Arr_I_k_(subSize, m._matchArray, outerFromIndex)
end function

function Anon_1b4b7aa5_get_size_k_() as Integer
    return m._matchArray.count()
end function

function Anon_5afee63d_create_I_Arr_Str_Regex_k_(_matchIndex as Integer, _matchArray as Object, _input as String, this_0 as Object) as Object
    this = {}
    this.__type = "Anon_5afee63d"
    this.__proto = ["Anon_5afee63d", "MatchResult"]
    this.__id = __kotlin_nextObjectId()
    this.next_k_ = Anon_5afee63d_next_k_
    this.get_range = Anon_5afee63d_get_range_k_
    this.get_value = Anon_5afee63d_get_value_k_
    this.get_groups = Anon_5afee63d_get_groups_k_
    this.get_groupValues_ = Anon_5afee63d_get_groupValues__k_
    this.set_groupValues_ = Anon_5afee63d_set_groupValues__ListStrN_k_
    this.get_groupValues = Anon_5afee63d_get_groupValues_k_
    this.range = until_rI_I_k_(_matchIndex, _matchIndex + Len(_matchArray[0]))
    this.value = _matchArray[0]
    this.groups = Anon_55266bab_create_Arr_k_(_matchArray)
    this.groupValues_ = invalid
    this._matchArray = _matchArray
    this._input = _input
    this.this_0 = this_0
    return this
end function

function Anon_5afee63d_next_k_() as Dynamic
    __when_tmp3 = invalid
    if m.get_range().isEmpty_k_() then
        __when_tmp3 = (m.get_range().get_start() + 1)
    else if true then
        __when_tmp3 = (m.get_range().get_endInclusive() + 1)
    end if
    nextStartIndex = __when_tmp3

    if nextStartIndex > Len(m._input) then
        return invalid
    end if
    return m.this_0.find_CharSequence_I_k_(m._input, nextStartIndex)
end function

function Anon_5afee63d_get_range_k_() as Object
    return m.range
end function

function Anon_5afee63d_get_value_k_() as String
    return m.value
end function

function Anon_5afee63d_get_groups_k_() as Object
    return m.groups
end function

function Anon_5afee63d_get_groupValues__k_() as Dynamic
    return m.groupValues_
end function

sub Anon_5afee63d_set_groupValues__ListStrN_k_(value as Dynamic)
    m.groupValues_ = value
end sub

function Anon_5afee63d_get_groupValues_k_() as Object
    if m.get_groupValues_() = invalid then
        m.set_groupValues_(Anon_1b4b7aa5_create_Arr_k_(m._matchArray))
    end if
    return m.get_groupValues_()
end function
