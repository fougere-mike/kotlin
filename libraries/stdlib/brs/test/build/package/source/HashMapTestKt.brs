sub hashMapTests_rTestRunner_k_(m as Object)
    m.suite_Str_Function1TestRunnerV_k_("HashMap", hashMapTests_lambda_create_k_())
end sub

function hashMapTests_lambda_lambda_create_k_() as Object
    this = {}
    this.__type = "hashMapTests_lambda_lambda"
    this.__proto = ["hashMapTests_lambda_lambda", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = hashMapTests_lambda_lambda_invoke_k_
    return this
end function

sub hashMapTests_lambda_lambda_invoke_k_()
    map = HashMap_create_k_()
    assertTrue_Z_StrN_k_(map.isEmpty_k_(), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(0, map.__get_size(), invalid)
end sub

function hashMapTests_lambda_lambda_1_create_k_() as Object
    this = {}
    this.__type = "hashMapTests_lambda_lambda_1"
    this.__proto = ["hashMapTests_lambda_lambda_1", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = hashMapTests_lambda_lambda_1_invoke_k_
    return this
end function

sub hashMapTests_lambda_lambda_1_invoke_k_()
    map = HashMap_create_Map_k_(mapOf_Arr_k_([to_rAnyN_AnyN_k_("a", 1), to_rAnyN_AnyN_k_("b", 2)]))
    assertEquals_AnyN_AnyN_StrN_k_(2, map.__get_size(), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(1, map.get_AnyN_k_("a"), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(2, map.get_AnyN_k_("b"), invalid)
end sub

function hashMapTests_lambda_lambda_2_create_k_() as Object
    this = {}
    this.__type = "hashMapTests_lambda_lambda_2"
    this.__proto = ["hashMapTests_lambda_lambda_2", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = hashMapTests_lambda_lambda_2_invoke_k_
    return this
end function

sub hashMapTests_lambda_lambda_2_invoke_k_()
    map = HashMap_create_k_()
    assertNull_AnyN_StrN_k_(map.put_AnyN_AnyN_k_("one", 1), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(1, map.get_AnyN_k_("one"), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(1, map.put_AnyN_AnyN_k_("one", 10), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(10, map.get_AnyN_k_("one"), invalid)
end sub

function hashMapTests_lambda_lambda_3_create_k_() as Object
    this = {}
    this.__type = "hashMapTests_lambda_lambda_3"
    this.__proto = ["hashMapTests_lambda_lambda_3", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = hashMapTests_lambda_lambda_3_invoke_k_
    return this
end function

sub hashMapTests_lambda_lambda_3_invoke_k_()
    map = HashMap_create_k_()
    assertNull_AnyN_StrN_k_(map.get_AnyN_k_("missing"), invalid)
end sub

function hashMapTests_lambda_lambda_4_create_k_() as Object
    this = {}
    this.__type = "hashMapTests_lambda_lambda_4"
    this.__proto = ["hashMapTests_lambda_lambda_4", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = hashMapTests_lambda_lambda_4_invoke_k_
    return this
end function

sub hashMapTests_lambda_lambda_4_invoke_k_()
    map = hashMapOf_Arr_k_([to_rAnyN_AnyN_k_("a", 1), to_rAnyN_AnyN_k_("b", 2)])
    assertTrue_Z_StrN_k_(map.containsKey_AnyN_k_("a"), invalid)
    assertFalse_Z_StrN_k_(map.containsKey_AnyN_k_("c"), invalid)
end sub

function hashMapTests_lambda_lambda_5_create_k_() as Object
    this = {}
    this.__type = "hashMapTests_lambda_lambda_5"
    this.__proto = ["hashMapTests_lambda_lambda_5", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = hashMapTests_lambda_lambda_5_invoke_k_
    return this
end function

sub hashMapTests_lambda_lambda_5_invoke_k_()
    map = hashMapOf_Arr_k_([to_rAnyN_AnyN_k_("a", 1), to_rAnyN_AnyN_k_("b", 2)])
    assertTrue_Z_StrN_k_(map.containsValue_AnyN_k_(1), invalid)
    assertFalse_Z_StrN_k_(map.containsValue_AnyN_k_(3), invalid)
end sub

function hashMapTests_lambda_lambda_6_create_k_() as Object
    this = {}
    this.__type = "hashMapTests_lambda_lambda_6"
    this.__proto = ["hashMapTests_lambda_lambda_6", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = hashMapTests_lambda_lambda_6_invoke_k_
    return this
end function

sub hashMapTests_lambda_lambda_6_invoke_k_()
    map = hashMapOf_Arr_k_([to_rAnyN_AnyN_k_("a", 1), to_rAnyN_AnyN_k_("b", 2), to_rAnyN_AnyN_k_("c", 3)])
    assertEquals_AnyN_AnyN_StrN_k_(2, map.remove_AnyN_k_("b"), invalid)
    assertNull_AnyN_StrN_k_(map.remove_AnyN_k_("missing"), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(2, map.__get_size(), invalid)
    assertFalse_Z_StrN_k_(map.containsKey_AnyN_k_("b"), invalid)
end sub

function hashMapTests_lambda_lambda_7_create_k_() as Object
    this = {}
    this.__type = "hashMapTests_lambda_lambda_7"
    this.__proto = ["hashMapTests_lambda_lambda_7", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = hashMapTests_lambda_lambda_7_invoke_k_
    return this
end function

sub hashMapTests_lambda_lambda_7_invoke_k_()
    map = hashMapOf_Arr_k_([to_rAnyN_AnyN_k_("a", 1), to_rAnyN_AnyN_k_("b", 2)])
    map.clear_k_()
    assertTrue_Z_StrN_k_(map.isEmpty_k_(), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(0, map.__get_size(), invalid)
end sub

function hashMapTests_lambda_lambda_8_create_k_() as Object
    this = {}
    this.__type = "hashMapTests_lambda_lambda_8"
    this.__proto = ["hashMapTests_lambda_lambda_8", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = hashMapTests_lambda_lambda_8_invoke_k_
    return this
end function

sub hashMapTests_lambda_lambda_8_invoke_k_()
    map = hashMapOf_Arr_k_([to_rAnyN_AnyN_k_("a", 1), to_rAnyN_AnyN_k_("b", 2), to_rAnyN_AnyN_k_("c", 3)])
    keys = map.__get_keys()
    assertEquals_AnyN_AnyN_StrN_k_(3, keys.__get_size(), invalid)
    assertTrue_Z_StrN_k_(keys.contains_AnyN_k_("a"), invalid)
    assertTrue_Z_StrN_k_(keys.contains_AnyN_k_("b"), invalid)
    assertTrue_Z_StrN_k_(keys.contains_AnyN_k_("c"), invalid)
end sub

function hashMapTests_lambda_lambda_9_create_k_() as Object
    this = {}
    this.__type = "hashMapTests_lambda_lambda_9"
    this.__proto = ["hashMapTests_lambda_lambda_9", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = hashMapTests_lambda_lambda_9_invoke_k_
    return this
end function

sub hashMapTests_lambda_lambda_9_invoke_k_()
    map = hashMapOf_Arr_k_([to_rAnyN_AnyN_k_("a", 1), to_rAnyN_AnyN_k_("b", 2), to_rAnyN_AnyN_k_("c", 3)])
    values = map.__get_values()
    assertEquals_AnyN_AnyN_StrN_k_(3, values.__get_size(), invalid)
    assertTrue_Z_StrN_k_(values.contains_AnyN_k_(1), invalid)
    assertTrue_Z_StrN_k_(values.contains_AnyN_k_(2), invalid)
    assertTrue_Z_StrN_k_(values.contains_AnyN_k_(3), invalid)
end sub

function hashMapTests_lambda_lambda_10_create_k_() as Object
    this = {}
    this.__type = "hashMapTests_lambda_lambda_10"
    this.__proto = ["hashMapTests_lambda_lambda_10", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = hashMapTests_lambda_lambda_10_invoke_k_
    return this
end function

sub hashMapTests_lambda_lambda_10_invoke_k_()
    map = hashMapOf_Arr_k_([to_rAnyN_AnyN_k_("a", 1), to_rAnyN_AnyN_k_("b", 2)])
    entries = map.__get_entries()
    assertEquals_AnyN_AnyN_StrN_k_(2, entries.__get_size(), invalid)
end sub

function hashMapTests_lambda_lambda_11_create_k_() as Object
    this = {}
    this.__type = "hashMapTests_lambda_lambda_11"
    this.__proto = ["hashMapTests_lambda_lambda_11", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = hashMapTests_lambda_lambda_11_invoke_k_
    return this
end function

sub hashMapTests_lambda_lambda_11_invoke_k_()
    map = hashMapOf_Arr_k_([to_rAnyN_AnyN_k_("a", 1)])
    map.putAll_Map_k_(mapOf_Arr_k_([to_rAnyN_AnyN_k_("b", 2), to_rAnyN_AnyN_k_("c", 3)]))
    assertEquals_AnyN_AnyN_StrN_k_(3, map.__get_size(), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(1, map.get_AnyN_k_("a"), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(2, map.get_AnyN_k_("b"), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(3, map.get_AnyN_k_("c"), invalid)
end sub

function hashMapTests_lambda_lambda_12_create_k_() as Object
    this = {}
    this.__type = "hashMapTests_lambda_lambda_12"
    this.__proto = ["hashMapTests_lambda_lambda_12", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = hashMapTests_lambda_lambda_12_invoke_k_
    return this
end function

sub hashMapTests_lambda_lambda_12_invoke_k_()
    map = hashMapOf_Arr_k_([to_rAnyN_AnyN_k_("a", 1)])
    assertEquals_AnyN_AnyN_StrN_k_(1, getOrDefault_rMap_AnyN_AnyN_k_(map, "a", 0), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(0, getOrDefault_rMap_AnyN_AnyN_k_(map, "b", 0), invalid)
end sub

function hashMapTests_lambda_lambda_lambda_create_k_() as Object
    this = {}
    this.__type = "hashMapTests_lambda_lambda_lambda"
    this.__proto = ["hashMapTests_lambda_lambda_lambda", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = hashMapTests_lambda_lambda_lambda_invoke_k_
    return this
end function

function hashMapTests_lambda_lambda_lambda_invoke_k_() as Integer
    return 10
end function

function hashMapTests_lambda_lambda_lambda_1_create_k_() as Object
    this = {}
    this.__type = "hashMapTests_lambda_lambda_lambda_1"
    this.__proto = ["hashMapTests_lambda_lambda_lambda_1", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = hashMapTests_lambda_lambda_lambda_1_invoke_k_
    return this
end function

function hashMapTests_lambda_lambda_lambda_1_invoke_k_() as Integer
    return 10
end function

function hashMapTests_lambda_lambda_13_create_k_() as Object
    this = {}
    this.__type = "hashMapTests_lambda_lambda_13"
    this.__proto = ["hashMapTests_lambda_lambda_13", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = hashMapTests_lambda_lambda_13_invoke_k_
    return this
end function

sub hashMapTests_lambda_lambda_13_invoke_k_()
    map = hashMapOf_Arr_k_([to_rAnyN_AnyN_k_("a", 1)])
    assertEquals_AnyN_AnyN_StrN_k_(1, getOrPut_rMutableMap_AnyN_Function0_k_(map, "a", hashMapTests_lambda_lambda_lambda_create_k_()), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(10, getOrPut_rMutableMap_AnyN_Function0_k_(map, "b", hashMapTests_lambda_lambda_lambda_1_create_k_()), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(10, map.get_AnyN_k_("b"), invalid)
end sub

function hashMapTests_lambda_lambda_14_create_k_() as Object
    this = {}
    this.__type = "hashMapTests_lambda_lambda_14"
    this.__proto = ["hashMapTests_lambda_lambda_14", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = hashMapTests_lambda_lambda_14_invoke_k_
    return this
end function

sub hashMapTests_lambda_lambda_14_invoke_k_()
    map = hashMapOf_Arr_k_([to_rAnyN_AnyN_k_("a", 1), to_rAnyN_AnyN_k_("b", 2)])
    keys = mutableListOf_k_()
    values = mutableListOf_k_()
    __iter_0 = map.__get_entries().iterator_k_()
    while __iter_0.hasNext_k_()
        entry = __iter_0.next_k_()
        keys.add_AnyN_k_(entry.__get_key())
        values.add_AnyN_k_(entry.__get_value())

    end while

    assertEquals_AnyN_AnyN_StrN_k_(2, keys.__get_size(), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(2, values.__get_size(), invalid)
end sub

function hashMapTests_lambda_lambda_15_create_k_() as Object
    this = {}
    this.__type = "hashMapTests_lambda_lambda_15"
    this.__proto = ["hashMapTests_lambda_lambda_15", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = hashMapTests_lambda_lambda_15_invoke_k_
    return this
end function

sub hashMapTests_lambda_lambda_15_invoke_k_()
    map1 = hashMapOf_Arr_k_([to_rAnyN_AnyN_k_("a", 1), to_rAnyN_AnyN_k_("b", 2)])
    map2 = hashMapOf_Arr_k_([to_rAnyN_AnyN_k_("a", 1), to_rAnyN_AnyN_k_("b", 2)])
    map3 = hashMapOf_Arr_k_([to_rAnyN_AnyN_k_("a", 1), to_rAnyN_AnyN_k_("b", 3)])
    assertEquals_AnyN_AnyN_StrN_k_(map1, map2, invalid)
    assertNotEquals_AnyN_AnyN_StrN_k_(map1, map3, invalid)
end sub

function hashMapTests_lambda_lambda_16_create_k_() as Object
    this = {}
    this.__type = "hashMapTests_lambda_lambda_16"
    this.__proto = ["hashMapTests_lambda_lambda_16", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = hashMapTests_lambda_lambda_16_invoke_k_
    return this
end function

sub hashMapTests_lambda_lambda_16_invoke_k_()
    map = HashMap_create_k_()
    set_rMutableMap_AnyN_AnyN_k_(map, "a", invalid)
    assertTrue_Z_StrN_k_(map.containsKey_AnyN_k_("a"), invalid)
    assertNull_AnyN_StrN_k_(map.get_AnyN_k_("a"), invalid)
end sub

function hashMapTests_lambda_lambda_17_create_k_() as Object
    this = {}
    this.__type = "hashMapTests_lambda_lambda_17"
    this.__proto = ["hashMapTests_lambda_lambda_17", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = hashMapTests_lambda_lambda_17_invoke_k_
    return this
end function

sub hashMapTests_lambda_lambda_17_invoke_k_()
    map = HashMap_create_k_()
    set_rMutableMap_AnyN_AnyN_k_(map, 1, "one")
    set_rMutableMap_AnyN_AnyN_k_(map, 2, "two")
    assertEquals_AnyN_AnyN_StrN_k_("one", map.get_AnyN_k_(1), invalid)
    assertEquals_AnyN_AnyN_StrN_k_("two", map.get_AnyN_k_(2), invalid)
end sub

function hashMapTests_lambda_create_k_() as Object
    this = {}
    this.__type = "hashMapTests_lambda"
    this.__proto = ["hashMapTests_lambda", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = hashMapTests_lambda_invoke_AnyN_k_
    return this
end function

sub hashMapTests_lambda_invoke_AnyN_k_(_this_suite as Object)
    _this_suite.test_Str_Function0V_k_("create empty", hashMapTests_lambda_lambda_create_k_())
    _this_suite.test_Str_Function0V_k_("create from map", hashMapTests_lambda_lambda_1_create_k_())
    _this_suite.test_Str_Function0V_k_("put and get", hashMapTests_lambda_lambda_2_create_k_())
    _this_suite.test_Str_Function0V_k_("get nonexistent", hashMapTests_lambda_lambda_3_create_k_())
    _this_suite.test_Str_Function0V_k_("containsKey", hashMapTests_lambda_lambda_4_create_k_())
    _this_suite.test_Str_Function0V_k_("containsValue", hashMapTests_lambda_lambda_5_create_k_())
    _this_suite.test_Str_Function0V_k_("remove", hashMapTests_lambda_lambda_6_create_k_())
    _this_suite.test_Str_Function0V_k_("clear", hashMapTests_lambda_lambda_7_create_k_())
    _this_suite.test_Str_Function0V_k_("keys", hashMapTests_lambda_lambda_8_create_k_())
    _this_suite.test_Str_Function0V_k_("values", hashMapTests_lambda_lambda_9_create_k_())
    _this_suite.test_Str_Function0V_k_("entries", hashMapTests_lambda_lambda_10_create_k_())
    _this_suite.test_Str_Function0V_k_("putAll", hashMapTests_lambda_lambda_11_create_k_())
    _this_suite.test_Str_Function0V_k_("getOrDefault", hashMapTests_lambda_lambda_12_create_k_())
    _this_suite.test_Str_Function0V_k_("getOrPut", hashMapTests_lambda_lambda_13_create_k_())
    _this_suite.test_Str_Function0V_k_("iteration via entries", hashMapTests_lambda_lambda_14_create_k_())
    _this_suite.test_Str_Function0V_k_("equals", hashMapTests_lambda_lambda_15_create_k_())
    _this_suite.test_Str_Function0V_k_("null values", hashMapTests_lambda_lambda_16_create_k_())
    _this_suite.test_Str_Function0V_k_("integer keys", hashMapTests_lambda_lambda_17_create_k_())
end sub
