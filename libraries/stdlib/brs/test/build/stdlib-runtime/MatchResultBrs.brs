function MatchGroupCollection_get_I_MatchGroupN_k_(index as Integer) as Dynamic
end function

function MatchNamedGroupCollection_get_Str_MatchGroupN_k_(name as String) as Dynamic
end function

function MatchResult_next_MatchResultN_k_() as Dynamic
end function

function MatchResult_get_range_IntRange_k_() as Object
end function

function MatchResult_get_value_Str_k_() as String
end function

function MatchResult_get_groups_MatchGroupCollection_k_() as Object
end function

function MatchResult_get_groupValues_ListStr_k_() as Object
end function

function MatchResult_get_destructured_Destructured_k_() as Object
    return MatchResult_Destructured_create_MatchResult_Destructured_k_(m)
end function

function MatchResult_Destructured_create_MatchResult_Destructured_k_(match as Object) as Object
    this = {}
    this.__type = "MatchResult_Destructured"
    this.__proto = ["MatchResult_Destructured"]
    this.match = match
    this.component1_Str_k_ = MatchResult_Destructured_component1_Str_k_
    this.component2_Str_k_ = MatchResult_Destructured_component2_Str_k_
    this.component3_Str_k_ = MatchResult_Destructured_component3_Str_k_
    this.component4_Str_k_ = MatchResult_Destructured_component4_Str_k_
    this.component5_Str_k_ = MatchResult_Destructured_component5_Str_k_
    this.component6_Str_k_ = MatchResult_Destructured_component6_Str_k_
    this.component7_Str_k_ = MatchResult_Destructured_component7_Str_k_
    this.component8_Str_k_ = MatchResult_Destructured_component8_Str_k_
    this.component9_Str_k_ = MatchResult_Destructured_component9_Str_k_
    this.component10_Str_k_ = MatchResult_Destructured_component10_Str_k_
    this.toList_ListStr_k_ = MatchResult_Destructured_toList_ListStr_k_
    this.get_match = MatchResult_Destructured_get_match_MatchResult_k_
    return this
end function

function MatchResult_Destructured_component1_Str_k_() as String
    return m.match.groupValues[1]
end function

function MatchResult_Destructured_component2_Str_k_() as String
    return m.match.groupValues[2]
end function

function MatchResult_Destructured_component3_Str_k_() as String
    return m.match.groupValues[3]
end function

function MatchResult_Destructured_component4_Str_k_() as String
    return m.match.groupValues[4]
end function

function MatchResult_Destructured_component5_Str_k_() as String
    return m.match.groupValues[5]
end function

function MatchResult_Destructured_component6_Str_k_() as String
    return m.match.groupValues[6]
end function

function MatchResult_Destructured_component7_Str_k_() as String
    return m.match.groupValues[7]
end function

function MatchResult_Destructured_component8_Str_k_() as String
    return m.match.groupValues[8]
end function

function MatchResult_Destructured_component9_Str_k_() as String
    return m.match.groupValues[9]
end function

function MatchResult_Destructured_component10_Str_k_() as String
    return m.match.groupValues[10]
end function

function MatchResult_Destructured_toList_ListStr_k_() as Object
    return m.match.groupValues.subList(1, m.match.groupValues.size)
end function

function MatchResult_Destructured_get_match_MatchResult_k_() as Object
    return m.match
end function
