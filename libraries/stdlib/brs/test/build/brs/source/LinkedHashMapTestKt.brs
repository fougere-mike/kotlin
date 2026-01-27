sub linkedHashMapTests_rTestRunner_k_(m as Object)
    m.suite_Str_Function1TestRunnerV_k_("LinkedHashMap", linkedHashMapTests_lambda_create_k_())
end sub

function linkedHashMapTests_lambda_lambda_create_k_() as Object
    this = {}
    this.__type = "linkedHashMapTests_lambda_lambda"
    this.__proto = ["linkedHashMapTests_lambda_lambda", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = linkedHashMapTests_lambda_lambda_invoke_k_
    return this
end function

sub linkedHashMapTests_lambda_lambda_invoke_k_()
    map = LinkedHashMap_create_k_()
    set_rMutableMap_AnyN_AnyN_k_(map, "three", 3)
    set_rMutableMap_AnyN_AnyN_k_(map, "one", 1)
    set_rMutableMap_AnyN_AnyN_k_(map, "two", 2)
    keys = toList_rIterable_k_(map.__get_keys())
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_(["three", "one", "two"]), keys, invalid)
    values = toList_rIterable_k_(map.__get_values())
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([3, 1, 2]), values, invalid)
end sub

function linkedHashMapTests_lambda_lambda_1_create_k_() as Object
    this = {}
    this.__type = "linkedHashMapTests_lambda_lambda_1"
    this.__proto = ["linkedHashMapTests_lambda_lambda_1", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = linkedHashMapTests_lambda_lambda_1_invoke_k_
    return this
end function

sub linkedHashMapTests_lambda_lambda_1_invoke_k_()
    map = LinkedHashMap_create_k_()
    set_rMutableMap_AnyN_AnyN_k_(map, "one", 1)
    set_rMutableMap_AnyN_AnyN_k_(map, "two", 2)
    set_rMutableMap_AnyN_AnyN_k_(map, "three", 3)
    set_rMutableMap_AnyN_AnyN_k_(map, "two", 22)
    keys = toList_rIterable_k_(map.__get_keys())
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_(["one", "two", "three"]), keys, invalid)
    assertEquals_AnyN_AnyN_StrN_k_(22, map.get_AnyN_k_("two"), invalid)
end sub

function linkedHashMapTests_lambda_lambda_2_create_k_() as Object
    this = {}
    this.__type = "linkedHashMapTests_lambda_lambda_2"
    this.__proto = ["linkedHashMapTests_lambda_lambda_2", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = linkedHashMapTests_lambda_lambda_2_invoke_k_
    return this
end function

sub linkedHashMapTests_lambda_lambda_2_invoke_k_()
    original = mapOf_Arr_k_([to_rAnyN_AnyN_k_("a", 1), to_rAnyN_AnyN_k_("b", 2), to_rAnyN_AnyN_k_("c", 3)])
    linked = LinkedHashMap_create_Map_k_(original)
    assertEquals_AnyN_AnyN_StrN_k_(3, linked.__get_size(), invalid)
    assertTrue_Z_StrN_k_(linked.containsKey_AnyN_k_("a"), invalid)
    assertTrue_Z_StrN_k_(linked.containsKey_AnyN_k_("b"), invalid)
    assertTrue_Z_StrN_k_(linked.containsKey_AnyN_k_("c"), invalid)
end sub

function linkedHashMapTests_lambda_lambda_3_create_k_() as Object
    this = {}
    this.__type = "linkedHashMapTests_lambda_lambda_3"
    this.__proto = ["linkedHashMapTests_lambda_lambda_3", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = linkedHashMapTests_lambda_lambda_3_invoke_k_
    return this
end function

sub linkedHashMapTests_lambda_lambda_3_invoke_k_()
    map = LinkedHashMap_create_k_()
    assertNull_AnyN_StrN_k_(map.put_AnyN_AnyN_k_(1, "one"), invalid)
    assertEquals_AnyN_AnyN_StrN_k_("one", map.put_AnyN_AnyN_k_(1, "ONE"), invalid)
    assertEquals_AnyN_AnyN_StrN_k_("ONE", map.get_AnyN_k_(1), invalid)
end sub

function linkedHashMapTests_lambda_lambda_4_create_k_() as Object
    this = {}
    this.__type = "linkedHashMapTests_lambda_lambda_4"
    this.__proto = ["linkedHashMapTests_lambda_lambda_4", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = linkedHashMapTests_lambda_lambda_4_invoke_k_
    return this
end function

sub linkedHashMapTests_lambda_lambda_4_invoke_k_()
    map = LinkedHashMap_create_k_()
    set_rMutableMap_AnyN_AnyN_k_(map, "one", 1)
    set_rMutableMap_AnyN_AnyN_k_(map, "two", 2)
    set_rMutableMap_AnyN_AnyN_k_(map, "three", 3)
    assertEquals_AnyN_AnyN_StrN_k_(2, map.remove_AnyN_k_("two"), invalid)
    assertFalse_Z_StrN_k_(map.containsKey_AnyN_k_("two"), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(2, map.__get_size(), invalid)
    keys = toList_rIterable_k_(map.__get_keys())
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_(["one", "three"]), keys, invalid)
end sub

function linkedHashMapTests_lambda_lambda_5_create_k_() as Object
    this = {}
    this.__type = "linkedHashMapTests_lambda_lambda_5"
    this.__proto = ["linkedHashMapTests_lambda_lambda_5", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = linkedHashMapTests_lambda_lambda_5_invoke_k_
    return this
end function

sub linkedHashMapTests_lambda_lambda_5_invoke_k_()
    map = LinkedHashMap_create_k_()
    set_rMutableMap_AnyN_AnyN_k_(map, "one", 1)
    set_rMutableMap_AnyN_AnyN_k_(map, "two", 2)
    map.clear_k_()
    assertEquals_AnyN_AnyN_StrN_k_(0, map.__get_size(), invalid)
    assertTrue_Z_StrN_k_(map.isEmpty_k_(), invalid)
end sub

function linkedHashMapTests_lambda_lambda_6_create_k_() as Object
    this = {}
    this.__type = "linkedHashMapTests_lambda_lambda_6"
    this.__proto = ["linkedHashMapTests_lambda_lambda_6", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = linkedHashMapTests_lambda_lambda_6_invoke_k_
    return this
end function

sub linkedHashMapTests_lambda_lambda_6_invoke_k_()
    map = LinkedHashMap_create_k_()
    set_rMutableMap_AnyN_AnyN_k_(map, "one", 1)
    set_rMutableMap_AnyN_AnyN_k_(map, "two", 2)
    set_rMutableMap_AnyN_AnyN_k_(map, "three", 3)
    iter = map.__get_keys().iterator_k_()
    while iter.hasNext_k_()
        if iter.next_k_() = "two" then
            iter.remove_k_()
        end if
    end while
    assertEquals_AnyN_AnyN_StrN_k_(2, map.__get_size(), invalid)
    assertFalse_Z_StrN_k_(map.containsKey_AnyN_k_("two"), invalid)
end sub

function linkedHashMapTests_lambda_lambda_7_create_k_() as Object
    this = {}
    this.__type = "linkedHashMapTests_lambda_lambda_7"
    this.__proto = ["linkedHashMapTests_lambda_lambda_7", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = linkedHashMapTests_lambda_lambda_7_invoke_k_
    return this
end function

sub linkedHashMapTests_lambda_lambda_7_invoke_k_()
    map = LinkedHashMap_create_k_()
    set_rMutableMap_AnyN_AnyN_k_(map, "one", 1)
    set_rMutableMap_AnyN_AnyN_k_(map, "two", 2)
    entries = toList_rIterable_k_(map.__get_entries())
    assertEquals_AnyN_AnyN_StrN_k_(2, entries.__get_size(), invalid)
    assertEquals_AnyN_AnyN_StrN_k_("one", entries.get_I_k_(0).__get_key(), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(1, entries.get_I_k_(0).__get_value(), invalid)
end sub

function linkedHashMapTests_lambda_lambda_8_create_k_() as Object
    this = {}
    this.__type = "linkedHashMapTests_lambda_lambda_8"
    this.__proto = ["linkedHashMapTests_lambda_lambda_8", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = linkedHashMapTests_lambda_lambda_8_invoke_k_
    return this
end function

sub linkedHashMapTests_lambda_lambda_8_invoke_k_()
    map = LinkedHashMap_create_k_()
    set_rMutableMap_AnyN_AnyN_k_(map, "one", 1)
    other = linkedMapOf_Arr_k_([to_rAnyN_AnyN_k_("two", 2), to_rAnyN_AnyN_k_("three", 3)])
    map.putAll_Map_k_(other)
    assertEquals_AnyN_AnyN_StrN_k_(3, map.__get_size(), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_(["one", "two", "three"]), toList_rIterable_k_(map.__get_keys()), invalid)
end sub

function linkedHashMapTests_lambda_create_k_() as Object
    this = {}
    this.__type = "linkedHashMapTests_lambda"
    this.__proto = ["linkedHashMapTests_lambda", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = linkedHashMapTests_lambda_invoke_AnyN_k_
    return this
end function

sub linkedHashMapTests_lambda_invoke_AnyN_k_(_this_suite as Object)
    _this_suite.test_Str_Function0V_k_("insertion order", linkedHashMapTests_lambda_lambda_create_k_())
    _this_suite.test_Str_Function0V_k_("insertion order with updates", linkedHashMapTests_lambda_lambda_1_create_k_())
    _this_suite.test_Str_Function0V_k_("constructor with map", linkedHashMapTests_lambda_lambda_2_create_k_())
    _this_suite.test_Str_Function0V_k_("put and get", linkedHashMapTests_lambda_lambda_3_create_k_())
    _this_suite.test_Str_Function0V_k_("remove", linkedHashMapTests_lambda_lambda_4_create_k_())
    _this_suite.test_Str_Function0V_k_("clear", linkedHashMapTests_lambda_lambda_5_create_k_())
    _this_suite.test_Str_Function0V_k_("iterator remove", linkedHashMapTests_lambda_lambda_6_create_k_())
    _this_suite.test_Str_Function0V_k_("entry set", linkedHashMapTests_lambda_lambda_7_create_k_())
    _this_suite.test_Str_Function0V_k_("putAll", linkedHashMapTests_lambda_lambda_8_create_k_())
end sub
