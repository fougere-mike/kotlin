function RegexOption_create(__name as String, __ordinal as Integer, flag as String) as Object
    this = {}
    this.__type = "RegexOption"
    this.name = __name
    this.ordinal = __ordinal
    this.flag = flag
    this.values_Arr_k_ = RegexOption_values_Arr_k_
    this.valueOf_Str_RegexOption_k_ = RegexOption_valueOf_Str_RegexOption_k_
    this.get_flag = RegexOption_get_flag_Str_k_
    this.get_entries = RegexOption_get_entries_EnumEntries_k_
    return this
end function

sub RegexOption_initEntries()
    if m.RegexOption_entriesInitialized then
        return
    end if
    m.RegexOption_entriesInitialized = true
    m.RegexOption_IGNORE_CASE = RegexOption_create("IGNORE_CASE", 0, "i")
    m.RegexOption_MULTILINE = RegexOption_create("MULTILINE", 1, "m")
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

function MatchGroup_create(value as String) as Object
    this = {}
    this.__type = "MatchGroup"
    this.__proto = ["MatchGroup"]
    this.value = value
    this.component1_Str_k_ = MatchGroup_component1_Str_k_
    this.copy_Str_MatchGroup_k_ = MatchGroup_copy_Str_MatchGroup_k_
    this.toString_Str_k_ = MatchGroup_toString_Str_k_
    this.hashCode_I_k_ = MatchGroup_hashCode_I_k_
    this.equals_AnyN_Z_k_ = MatchGroup_equals_AnyN_Z_k_
    this.get_value = MatchGroup_get_value_Str_k_
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
    return MatchGroup_create(value)
end function

function MatchGroup_component1() as String
    return m.value
end function

function Regex_create_Str_SetRegexOption_Regex_k_(pattern as String, options as Object) as Object
    this = {}
    this.__type = "Regex"
    this.__proto = ["Regex"]
    this.pattern = pattern
    this.options = toSet_rIterableAnyN_SetAnyN_k_(options)
    this.nativePattern = CreateObject("roRegex", pattern, joinToString_77mgo1_k_(options, "", {invoke: function(it as Object) as Object
        return it.flag
    end function}))
    this.matches_CharSequence_Z_k_ = Regex_matches_CharSequence_Z_k_
    this.containsMatchIn_CharSequence_Z_k_ = Regex_containsMatchIn_CharSequence_Z_k_
    this.matchEntire_CharSequence_MatchResultN_k_ = Regex_matchEntire_CharSequence_MatchResultN_k_
    this.matchAt_CharSequence_I_MatchResultN_k_ = Regex_matchAt_CharSequence_I_MatchResultN_k_
    this.matchesAt_CharSequence_I_Z_k_ = Regex_matchesAt_CharSequence_I_Z_k_
    this.find_CharSequence_I_MatchResultN_k_ = Regex_find_CharSequence_I_MatchResultN_k_
    this.findAll_CharSequence_I_SequenceMatchResult_k_ = Regex_findAll_CharSequence_I_SequenceMatchResult_k_
    this.replace_CharSequence_Str_Str_k_ = Regex_replace_CharSequence_Str_Str_k_
    this.replace_CharSequence_Function1MatchResultCharSequence_Str_k_ = Regex_replace_CharSequence_Function1MatchResultCharSequence_Str_k_
    this.replaceFirst_CharSequence_Str_Str_k_ = Regex_replaceFirst_CharSequence_Str_Str_k_
    this.split_CharSequence_I_ListStr_k_ = Regex_split_CharSequence_I_ListStr_k_
    this.splitToSequence_CharSequence_I_SequenceStr_k_ = Regex_splitToSequence_CharSequence_I_SequenceStr_k_
    this.toString_Str_k_ = Regex_toString_Str_k_
    this.createMatchResult_Str_Arr_I_MatchResult_k_ = Regex_createMatchResult_Str_Arr_I_MatchResult_k_
    this.get_pattern = Regex_get_pattern_Str_k_
    this.get_options = Regex_get_options_SetRegexOption_k_
    this.get_nativePattern = Regex_get_nativePattern_BrsRegex_k_
    return this
end function

function Regex_create_Str_RegexOption_Regex_k_(pattern as String, option as Object) as Object
    this = {}
    this.__type = "Regex"
    this.__proto = ["Regex"]
    this.pattern = pattern
    this.options = toSet_rIterableAnyN_SetAnyN_k_(options)
    this.nativePattern = CreateObject("roRegex", pattern, joinToString_77mgo1_k_(options, "", {invoke: function(it as Object) as Object
        return it.flag
    end function}))
    this.matches_CharSequence_Z_k_ = Regex_matches_CharSequence_Z_k_
    this.containsMatchIn_CharSequence_Z_k_ = Regex_containsMatchIn_CharSequence_Z_k_
    this.matchEntire_CharSequence_MatchResultN_k_ = Regex_matchEntire_CharSequence_MatchResultN_k_
    this.matchAt_CharSequence_I_MatchResultN_k_ = Regex_matchAt_CharSequence_I_MatchResultN_k_
    this.matchesAt_CharSequence_I_Z_k_ = Regex_matchesAt_CharSequence_I_Z_k_
    this.find_CharSequence_I_MatchResultN_k_ = Regex_find_CharSequence_I_MatchResultN_k_
    this.findAll_CharSequence_I_SequenceMatchResult_k_ = Regex_findAll_CharSequence_I_SequenceMatchResult_k_
    this.replace_CharSequence_Str_Str_k_ = Regex_replace_CharSequence_Str_Str_k_
    this.replace_CharSequence_Function1MatchResultCharSequence_Str_k_ = Regex_replace_CharSequence_Function1MatchResultCharSequence_Str_k_
    this.replaceFirst_CharSequence_Str_Str_k_ = Regex_replaceFirst_CharSequence_Str_Str_k_
    this.split_CharSequence_I_ListStr_k_ = Regex_split_CharSequence_I_ListStr_k_
    this.splitToSequence_CharSequence_I_SequenceStr_k_ = Regex_splitToSequence_CharSequence_I_SequenceStr_k_
    this.toString_Str_k_ = Regex_toString_Str_k_
    this.createMatchResult_Str_Arr_I_MatchResult_k_ = Regex_createMatchResult_Str_Arr_I_MatchResult_k_
    this.get_pattern = Regex_get_pattern_Str_k_
    this.get_options = Regex_get_options_SetRegexOption_k_
    this.get_nativePattern = Regex_get_nativePattern_BrsRegex_k_
    return this
end function

function Regex_create_Str_Regex_k_(pattern as String) as Object
    this = {}
    this.__type = "Regex"
    this.__proto = ["Regex"]
    this.pattern = pattern
    this.options = toSet_rIterableAnyN_SetAnyN_k_(options)
    this.nativePattern = CreateObject("roRegex", pattern, joinToString_77mgo1_k_(options, "", {invoke: function(it as Object) as Object
        return it.flag
    end function}))
    this.matches_CharSequence_Z_k_ = Regex_matches_CharSequence_Z_k_
    this.containsMatchIn_CharSequence_Z_k_ = Regex_containsMatchIn_CharSequence_Z_k_
    this.matchEntire_CharSequence_MatchResultN_k_ = Regex_matchEntire_CharSequence_MatchResultN_k_
    this.matchAt_CharSequence_I_MatchResultN_k_ = Regex_matchAt_CharSequence_I_MatchResultN_k_
    this.matchesAt_CharSequence_I_Z_k_ = Regex_matchesAt_CharSequence_I_Z_k_
    this.find_CharSequence_I_MatchResultN_k_ = Regex_find_CharSequence_I_MatchResultN_k_
    this.findAll_CharSequence_I_SequenceMatchResult_k_ = Regex_findAll_CharSequence_I_SequenceMatchResult_k_
    this.replace_CharSequence_Str_Str_k_ = Regex_replace_CharSequence_Str_Str_k_
    this.replace_CharSequence_Function1MatchResultCharSequence_Str_k_ = Regex_replace_CharSequence_Function1MatchResultCharSequence_Str_k_
    this.replaceFirst_CharSequence_Str_Str_k_ = Regex_replaceFirst_CharSequence_Str_Str_k_
    this.split_CharSequence_I_ListStr_k_ = Regex_split_CharSequence_I_ListStr_k_
    this.splitToSequence_CharSequence_I_SequenceStr_k_ = Regex_splitToSequence_CharSequence_I_SequenceStr_k_
    this.toString_Str_k_ = Regex_toString_Str_k_
    this.createMatchResult_Str_Arr_I_MatchResult_k_ = Regex_createMatchResult_Str_Arr_I_MatchResult_k_
    this.get_pattern = Regex_get_pattern_Str_k_
    this.get_options = Regex_get_options_SetRegexOption_k_
    this.get_nativePattern = Regex_get_nativePattern_BrsRegex_k_
    return this
end function

function Regex_matches_CharSequence_Z_k_(input as Object) as Boolean
    inputStr = input.toString()
    match = m.nativePattern.Match(inputStr)
    if isEmpty_rArr_Z_k_(match) then
        return false
    end if
    return match[0] = inputStr
end function

function Regex_containsMatchIn_CharSequence_Z_k_(input as Object) as Boolean
    return m.nativePattern.IsMatch(input.toString())
end function

function Regex_matchEntire_CharSequence_MatchResultN_k_(input as Object) as Dynamic
    inputStr = input.toString()
    match = m.nativePattern.Match(inputStr)
    if isEmpty_rArr_Z_k_(match) then
        return invalid
    end if
    if match[0] <> inputStr then
        return invalid
    end if
    return m.createMatchResult(inputStr, match, 0)
end function

function Regex_matchAt_CharSequence_I_MatchResultN_k_(input as Object, index as Integer) as Dynamic
    if (index < 0) or (index > input.length) then
        throw IndexOutOfBoundsException_create_StrN_IndexOutOfBoundsException_k_((("index out of bounds: " + index) + ", input length: ") + input.length)
    end if
    inputStr = input.toString()
    substring = substring_rStr_I_Str_k_(inputStr, index)
    match = m.nativePattern.Match(substring)
    if isEmpty_rArr_Z_k_(match) then
        return invalid
    end if
    matchIndex = indexOf_rStr_Str_I_Z_I_k_(substring, match[0])
    if matchIndex <> 0 then
        return invalid
    end if
    return m.createMatchResult(inputStr, match, index)
end function

function Regex_matchesAt_CharSequence_I_Z_k_(input as Object, index as Integer) as Boolean
    return m.matchAt(input, index) <> invalid
end function

function Regex_find_CharSequence_I_MatchResultN_k_(input as Object, startIndex = 0) as Dynamic
    if (startIndex < 0) or (startIndex > input.length) then
        throw IndexOutOfBoundsException_create_StrN_IndexOutOfBoundsException_k_((("Start index out of bounds: " + startIndex) + ", input length: ") + input.length)
    end if
    inputStr = input.toString()
    if startIndex = 0 then
        match = m.nativePattern.Match(inputStr)
        if isEmpty_rArr_Z_k_(match) then
            return invalid
        end if
        matchIndex = indexOf_rStr_Str_I_Z_I_k_(inputStr, match[0])
        if matchIndex < 0 then
            return invalid
        end if
        return m.createMatchResult(inputStr, match, matchIndex)
    else if true then
        substring = substring_rStr_I_Str_k_(inputStr, startIndex)
        match = m.nativePattern.Match(substring)
        if isEmpty_rArr_Z_k_(match) then
            return invalid
        end if
        matchIndex = indexOf_rStr_Str_I_Z_I_k_(substring, match[0])
        if matchIndex < 0 then
            return invalid
        end if
        return m.createMatchResult(inputStr, match, startIndex + matchIndex)
    end if
end function

function Regex_findAll_CharSequence_I_SequenceMatchResult_k_(input as Object, startIndex = 0) as Object
    if (startIndex < 0) or (startIndex > input.length) then
        throw IndexOutOfBoundsException_create_StrN_IndexOutOfBoundsException_k_((("Start index out of bounds: " + startIndex) + ", input length: ") + input.length)
    end if
    firstMatch = m.find(input, startIndex)
    return generateSequence_AnyN_Function1AnyAnyN_SequenceAny_k_(firstMatch, {invoke: function(match as Object) as Dynamic
        return match.next()
    end function})
end function

function Regex_replace_CharSequence_Str_Str_k_(input as Object, replacement as String) as String
    if contains_rStr_Str_Z_Z_k_(replacement, "\").not() and contains_rStr_Str_Z_Z_k_(replacement, "$").not() then
        return m.nativePattern.ReplaceAll(input.toString(), replacement)
    end if
    return m.replace(input, {replacement: replacement, invoke: function(it as Object) as Object
        return substituteGroupRefs_MatchResult_Str_Str_k_(it, m.replacement)
    end function})
end function

function Regex_replace_CharSequence_Function1MatchResultCharSequence_Str_k_(input as Object, transform as Function) as String
    match = m.find(input)
    if match = invalid then
        return input.toString()
    end if
    lastStart = 0
    length = input.length
    sb = StringBuilder_create_I_StringBuilder_k_(length)
    foundMatch = CHECK_NOT_NULL_AnyN_Any_k_(match)
    sb.append(input, lastStart, foundMatch.range.start)
    sb.append(transform.invoke(foundMatch))
    lastStart = (foundMatch.range.endInclusive + 1)
    match = foundMatch.next()

    while (lastStart < length) and (match <> invalid)
        foundMatch = CHECK_NOT_NULL_AnyN_Any_k_(match)
        sb.append(input, lastStart, foundMatch.range.start)
        sb.append(transform.invoke(foundMatch))
        lastStart = (foundMatch.range.endInclusive + 1)
        match = foundMatch.next()
    end while


    if lastStart < length then
        sb.append(input, lastStart, length)
    end if
    return sb.toString()
end function

function Regex_replaceFirst_CharSequence_Str_Str_k_(input as Object, replacement as String) as String
    tmp0_elvis_lhs = m.find(input)
    __when_tmp0 = invalid
    if tmp0_elvis_lhs = invalid then
        return input.toString()
    else if true then
        __when_tmp0 = tmp0_elvis_lhs
    end if
    match = __when_tmp0

    if contains_rStr_Str_Z_Z_k_(replacement, "\").not() and contains_rStr_Str_Z_Z_k_(replacement, "$").not() then
        return m.nativePattern.Replace(input.toString(), replacement)
    end if
    inputStr = input.toString()
    sb = StringBuilder_create_StringBuilder_k_()
    sb.append(substring_rStr_I_I_Str_k_(inputStr, 0, match.range.first))
    sb.append(substituteGroupRefs_MatchResult_Str_Str_k_(match, replacement))
    sb.append(substring_rStr_I_I_Str_k_(inputStr, match.range.last + 1, inputStr.length))
    return sb.toString()
end function

function Regex_split_CharSequence_I_ListStr_k_(input as Object, limit = 0) as Object
    requireNonNegativeLimit_I_k_(limit)
    allMatches = m.findAll(input)
    __when_tmp1 = invalid
    if limit = 0 then
        __when_tmp1 = allMatches
    else if true then
        __when_tmp1 = take_rSequenceAnyN_I_SequenceAnyN_k_(allMatches, limit - 1)
    end if
    matches = __when_tmp1

    result = mutableListOf_MutableListAnyN_k_()
    lastStart = 0
    for each match in matches
        result.add(input.subSequence(lastStart, match.range.start).toString())
        lastStart = (match.range.endInclusive + 1)

    end for
    result.add(input.subSequence(lastStart, input.length).toString())
    return result
end function

function Regex_splitToSequence_CharSequence_I_SequenceStr_k_(input as Object, limit = 0) as Object
    requireNonNegativeLimit_I_k_(limit)
    return Sequence_Function0IteratorAnyN_SequenceAnyN_k_({this: this, input: input, this: this, this: this, value: value, this: this, this: this, this: this, this: this, value: value, this: this, this: this, value: value, this: this, this: this, value: value, this: this, limit: limit, this: this, invoke: function() as Object
        return Anon_7ddacab3_create_Anon_k_()
    end function})
end function

function Regex_toString_Str_k_() as String
    return ((("Regex(" + m.pattern) + ", options=") + m.options) + ")"
end function

function Regex_createMatchResult_Str_Arr_I_MatchResult_k_(input as String, matchArray as Object, matchIndex as Integer) as Object
    return Anon_418e39d2_create_Anon_k_()
end function

function Regex_get_pattern_Str_k_() as String
    return m.pattern
end function

function Regex_get_options_SetRegexOption_k_() as Object
    return m.options
end function

function Regex_get_nativePattern_BrsRegex_k_() as Object
    return m.nativePattern
end function

function Regex_Companion_create_Companion_k_() as Object
    this = {}
    this.__type = "Regex_Companion"
    this.__proto = ["Regex_Companion"]
    this.fromLiteral_Str_Regex_k_ = Regex_Companion_fromLiteral_Str_Regex_k_
    this.escape_Str_Str_k_ = Regex_Companion_escape_Str_Str_k_
    this.escapeReplacement_Str_Str_k_ = Regex_Companion_escapeReplacement_Str_Str_k_
    return this
end function

function Regex_Companion_getInstance() as Object
    if m.Regex_Companion_instance = invalid then
        m.Regex_Companion_instance = Regex_Companion_create()
    end if
    return m.Regex_Companion_instance
end function

function Regex_Companion_fromLiteral_Str_Regex_k_(literal as String) as Object
    return Regex_create_Str_Regex_k_(Regex_Companion_escape_Str_Str_k_(literal))
end function

function Regex_Companion_escape_Str_Str_k_(literal as String) as String
    sb = StringBuilder_create_StringBuilder_k_()
    for each c in literal
        tmp0_subject = c
        if (((tmp0_subject = "\") or ((tmp0_subject = "^") or (tmp0_subject = "$"))) or (((tmp0_subject = "*") or (tmp0_subject = "+")) or ((tmp0_subject = "?") or (tmp0_subject = ".")))) or (((tmp0_subject = "(") or ((tmp0_subject = ")") or (tmp0_subject = "["))) or (((tmp0_subject = "]") or (tmp0_subject = "{")) or ((tmp0_subject = "}") or (tmp0_subject = "|")))) then
            sb.append("\")
            sb.append(c)
        else if true then
            sb.append(c)
        end if

    end for
    return sb.toString()
end function

function Regex_Companion_escapeReplacement_Str_Str_k_(literal as String) as String
    sb = StringBuilder_create_StringBuilder_k_()
    for each c in literal
        tmp0_subject = c
        if (tmp0_subject = "\") or (tmp0_subject = "$") then
            sb.append("\")
            sb.append(c)
        else if true then
            sb.append(c)
        end if

    end for
    return sb.toString()
end function

function substituteGroupRefs_MatchResult_Str_Str_k_(match as Object, replacement as String) as String
    index = 0
    result = StringBuilder_create_StringBuilder_k_()
    while index < replacement.length
        char = replacement[index = (index + 1)]
                if char = "\" then
            if index = replacement.length then
                throw IllegalArgumentException_create_StrN_IllegalArgumentException_k_("The Char to be escaped is missing")
            end if
            result.append(replacement[index = (index + 1)])
        else if char = "$" then
            if index = replacement.length then
                throw IllegalArgumentException_create_StrN_IllegalArgumentException_k_("Capturing group index is missing")
            end if
            if replacement[index] = "{" then
                endIndex = readGroupName_rStr_I_I_k_(replacement, index = (index + 1))
                if index = endIndex then
                    throw IllegalArgumentException_create_StrN_IllegalArgumentException_k_("Named capturing group reference should have a non-empty name")
                end if
                if (endIndex = replacement.length) or (replacement[endIndex] <> "}") then
                    throw IllegalArgumentException_create_StrN_IllegalArgumentException_k_("Named capturing group reference is missing trailing '}'")
                end if
                throw UnsupportedOperationException_create_StrN_UnsupportedOperationException_k_("Named capturing groups are not yet supported in BrightScript regex")
            else if true then
                if "0".rangeTo("9").contains(replacement[index]).not() then
                    throw IllegalArgumentException_create_StrN_IllegalArgumentException_k_("Invalid capturing group reference")
                end if
                groups = match.groups
                endIndex = readGroupIndex_rStr_I_I_I_k_(replacement, index, groups.size)
                groupIndexStr = substring_rStr_I_I_Str_k_(replacement, index, endIndex)
                groupIndex = parseIntOrThrow_Str_Str_I_k_(groupIndexStr, "Invalid group index: " + groupIndexStr)
                if groupIndex >= groups.size then
                    throw IndexOutOfBoundsException_create_StrN_IndexOutOfBoundsException_k_(("Group with index " + groupIndex) + " does not exist")
                end if
                group = groups.get(groupIndex)
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
                result.append(__when_tmp5)
                index = endIndex
            end if
        else if true then
            result.append(char)
        end if
    end while
    return result.toString()
end function

function readGroupName_rStr_I_I_k_(m as String, startIndex as Integer) as Integer
    index = startIndex
    while index < m.length
                if m[index] = "}" then
            exit while
        else if true then
            index = (unary + 1)
        end if
    end while
    return index
end function

function readGroupIndex_rStr_I_I_I_k_(m as String, startIndex as Integer, groupCount as Integer) as Integer
    index = startIndex + 1
    groupIndex = m[startIndex] - "0"
    while (index < m.length) and "0".rangeTo("9").contains(m[index])
        newGroupIndex = (groupIndex * 10) + (m[index] - "0")
                if until_rI_I_IntRange_k_(0, groupCount).contains(newGroupIndex) then
            groupIndex = newGroupIndex
            index = (unary + 1)
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

function parseIntOrThrow_Str_Str_I_k_(str as String, errorMessage as String) as Integer
    result = 0
    negative = false
    index = 0
    if isEmpty_rStr_Z_k_(str) then
        throw NumberFormatException_create_StrN_NumberFormatException_k_(errorMessage)
    end if
    if str[0] = "-" then
        negative = true
        index = 1
    else if str[0] = "+" then
        index = 1
    end if
    if index >= str.length then
        throw NumberFormatException_create_StrN_NumberFormatException_k_(errorMessage)
    end if
    while index < str.length
        char = str[index]
        if ((char < "0") < 0) or ((char > "9") > 0) then
            throw NumberFormatException_create_StrN_NumberFormatException_k_(errorMessage)
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
