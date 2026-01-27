function MatchGroupCollection_get_I_k_(index as Integer) as Dynamic
end function

function MatchNamedGroupCollection_get_Str_k_(name as String) as Dynamic
end function

function MatchResult_next_k_() as Dynamic
end function

function MatchResult___get_range_k_() as Object
end function

function MatchResult___get_value_k_() as String
end function

function MatchResult___get_groups_k_() as Object
end function

function MatchResult___get_groupValues_k_() as Object
end function

function MatchResult___get_destructured_k_() as Object
    return MatchResult_Destructured_create_MatchResult_k_(m)
end function

function MatchResult_Destructured_create_MatchResult_k_(match as Object) as Object
    this = {}
    this.__type = "MatchResult_Destructured"
    this.__proto = ["MatchResult_Destructured"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.component1_k_ = MatchResult_Destructured_component1_k_
    this.component2_k_ = MatchResult_Destructured_component2_k_
    this.component3_k_ = MatchResult_Destructured_component3_k_
    this.component4_k_ = MatchResult_Destructured_component4_k_
    this.component5_k_ = MatchResult_Destructured_component5_k_
    this.component6_k_ = MatchResult_Destructured_component6_k_
    this.component7_k_ = MatchResult_Destructured_component7_k_
    this.component8_k_ = MatchResult_Destructured_component8_k_
    this.component9_k_ = MatchResult_Destructured_component9_k_
    this.component10_k_ = MatchResult_Destructured_component10_k_
    this.toList_k_ = MatchResult_Destructured_toList_k_
    this.__get_match = MatchResult_Destructured___get_match_k_
    this.match = match
    return this
end function

function MatchResult_Destructured_component1_k_() as String
    return m.__get_match().__get_groupValues().get_I_k_(1)
end function

function MatchResult_Destructured_component2_k_() as String
    return m.__get_match().__get_groupValues().get_I_k_(2)
end function

function MatchResult_Destructured_component3_k_() as String
    return m.__get_match().__get_groupValues().get_I_k_(3)
end function

function MatchResult_Destructured_component4_k_() as String
    return m.__get_match().__get_groupValues().get_I_k_(4)
end function

function MatchResult_Destructured_component5_k_() as String
    return m.__get_match().__get_groupValues().get_I_k_(5)
end function

function MatchResult_Destructured_component6_k_() as String
    return m.__get_match().__get_groupValues().get_I_k_(6)
end function

function MatchResult_Destructured_component7_k_() as String
    return m.__get_match().__get_groupValues().get_I_k_(7)
end function

function MatchResult_Destructured_component8_k_() as String
    return m.__get_match().__get_groupValues().get_I_k_(8)
end function

function MatchResult_Destructured_component9_k_() as String
    return m.__get_match().__get_groupValues().get_I_k_(9)
end function

function MatchResult_Destructured_component10_k_() as String
    return m.__get_match().__get_groupValues().get_I_k_(10)
end function

function MatchResult_Destructured_toList_k_() as Object
    return m.__get_match().__get_groupValues().subList_I_I_k_(1, m.__get_match().__get_groupValues().__get_size())
end function

function MatchResult_Destructured___get_match_k_() as Object
    return m.match
end function
