sub hashSetTests_rTestRunner_k_(m as Object)
    m.suite_Str_Function1TestRunnerV_k_("HashSet", hashSetTests_lambda_create_k_())
end sub

function hashSetTests_lambda_lambda_create_k_() as Object
    this = {}
    this.__type = "hashSetTests_lambda_lambda"
    this.__proto = ["hashSetTests_lambda_lambda", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = hashSetTests_lambda_lambda_invoke_k_
    return this
end function

sub hashSetTests_lambda_lambda_invoke_k_()
    set = HashSet_create_k_()
    assertTrue_Z_StrN_k_(set.isEmpty_k_(), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(0, set.__get_size(), invalid)
end sub

function hashSetTests_lambda_lambda_1_create_k_() as Object
    this = {}
    this.__type = "hashSetTests_lambda_lambda_1"
    this.__proto = ["hashSetTests_lambda_lambda_1", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = hashSetTests_lambda_lambda_1_invoke_k_
    return this
end function

sub hashSetTests_lambda_lambda_1_invoke_k_()
    set = HashSet_create_Collection_k_(listOf_Arr_k_([1, 2, 3, 2, 1]))
    assertEquals_AnyN_AnyN_StrN_k_(3, set.__get_size(), invalid)
    assertTrue_Z_StrN_k_(set.contains_AnyN_k_(1), invalid)
    assertTrue_Z_StrN_k_(set.contains_AnyN_k_(2), invalid)
    assertTrue_Z_StrN_k_(set.contains_AnyN_k_(3), invalid)
end sub

function hashSetTests_lambda_lambda_2_create_k_() as Object
    this = {}
    this.__type = "hashSetTests_lambda_lambda_2"
    this.__proto = ["hashSetTests_lambda_lambda_2", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = hashSetTests_lambda_lambda_2_invoke_k_
    return this
end function

sub hashSetTests_lambda_lambda_2_invoke_k_()
    set = HashSet_create_k_()
    assertTrue_Z_StrN_k_(set.add_AnyN_k_(1), invalid)
    assertTrue_Z_StrN_k_(set.add_AnyN_k_(2), invalid)
    assertFalse_Z_StrN_k_(set.add_AnyN_k_(1), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(2, set.__get_size(), invalid)
end sub

function hashSetTests_lambda_lambda_3_create_k_() as Object
    this = {}
    this.__type = "hashSetTests_lambda_lambda_3"
    this.__proto = ["hashSetTests_lambda_lambda_3", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = hashSetTests_lambda_lambda_3_invoke_k_
    return this
end function

sub hashSetTests_lambda_lambda_3_invoke_k_()
    set = hashSetOf_Arr_k_([1, 2, 3])
    assertTrue_Z_StrN_k_(set.contains_AnyN_k_(2), invalid)
    assertFalse_Z_StrN_k_(set.contains_AnyN_k_(5), invalid)
end sub

function hashSetTests_lambda_lambda_4_create_k_() as Object
    this = {}
    this.__type = "hashSetTests_lambda_lambda_4"
    this.__proto = ["hashSetTests_lambda_lambda_4", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = hashSetTests_lambda_lambda_4_invoke_k_
    return this
end function

sub hashSetTests_lambda_lambda_4_invoke_k_()
    set = hashSetOf_Arr_k_([1, 2, 3])
    assertTrue_Z_StrN_k_(set.remove_AnyN_k_(2), invalid)
    assertFalse_Z_StrN_k_(set.remove_AnyN_k_(5), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(2, set.__get_size(), invalid)
    assertFalse_Z_StrN_k_(set.contains_AnyN_k_(2), invalid)
end sub

function hashSetTests_lambda_lambda_5_create_k_() as Object
    this = {}
    this.__type = "hashSetTests_lambda_lambda_5"
    this.__proto = ["hashSetTests_lambda_lambda_5", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = hashSetTests_lambda_lambda_5_invoke_k_
    return this
end function

sub hashSetTests_lambda_lambda_5_invoke_k_()
    set = hashSetOf_Arr_k_([1, 2, 3])
    set.clear_k_()
    assertTrue_Z_StrN_k_(set.isEmpty_k_(), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(0, set.__get_size(), invalid)
end sub

function hashSetTests_lambda_lambda_6_create_k_() as Object
    this = {}
    this.__type = "hashSetTests_lambda_lambda_6"
    this.__proto = ["hashSetTests_lambda_lambda_6", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = hashSetTests_lambda_lambda_6_invoke_k_
    return this
end function

sub hashSetTests_lambda_lambda_6_invoke_k_()
    set = hashSetOf_Arr_k_([1, 2])
    assertTrue_Z_StrN_k_(set.addAll_Collection_k_(listOf_Arr_k_([3, 4, 2])), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(4, set.__get_size(), invalid)
end sub

function hashSetTests_lambda_lambda_7_create_k_() as Object
    this = {}
    this.__type = "hashSetTests_lambda_lambda_7"
    this.__proto = ["hashSetTests_lambda_lambda_7", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = hashSetTests_lambda_lambda_7_invoke_k_
    return this
end function

sub hashSetTests_lambda_lambda_7_invoke_k_()
    set = hashSetOf_Arr_k_([1, 2, 3, 4, 5])
    assertTrue_Z_StrN_k_(set.removeAll_Collection_k_(listOf_Arr_k_([2, 4])), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(3, set.__get_size(), invalid)
    assertFalse_Z_StrN_k_(set.contains_AnyN_k_(2), invalid)
    assertFalse_Z_StrN_k_(set.contains_AnyN_k_(4), invalid)
end sub

function hashSetTests_lambda_lambda_8_create_k_() as Object
    this = {}
    this.__type = "hashSetTests_lambda_lambda_8"
    this.__proto = ["hashSetTests_lambda_lambda_8", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = hashSetTests_lambda_lambda_8_invoke_k_
    return this
end function

sub hashSetTests_lambda_lambda_8_invoke_k_()
    set = hashSetOf_Arr_k_([1, 2, 3, 4, 5])
    assertTrue_Z_StrN_k_(set.retainAll_Collection_k_(listOf_Arr_k_([2, 3, 6])), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(2, set.__get_size(), invalid)
    assertTrue_Z_StrN_k_(set.contains_AnyN_k_(2), invalid)
    assertTrue_Z_StrN_k_(set.contains_AnyN_k_(3), invalid)
end sub

function hashSetTests_lambda_lambda_9_create_k_() as Object
    this = {}
    this.__type = "hashSetTests_lambda_lambda_9"
    this.__proto = ["hashSetTests_lambda_lambda_9", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = hashSetTests_lambda_lambda_9_invoke_k_
    return this
end function

sub hashSetTests_lambda_lambda_9_invoke_k_()
    set = hashSetOf_Arr_k_([1, 2, 3, 4, 5])
    assertTrue_Z_StrN_k_(set.containsAll_Collection_k_(listOf_Arr_k_([1, 3, 5])), invalid)
    assertFalse_Z_StrN_k_(set.containsAll_Collection_k_(listOf_Arr_k_([1, 6])), invalid)
end sub

function hashSetTests_lambda_lambda_10_create_k_() as Object
    this = {}
    this.__type = "hashSetTests_lambda_lambda_10"
    this.__proto = ["hashSetTests_lambda_lambda_10", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = hashSetTests_lambda_lambda_10_invoke_k_
    return this
end function

sub hashSetTests_lambda_lambda_10_invoke_k_()
    set = hashSetOf_Arr_k_([1, 2, 3])
    collected = mutableListOf_k_()
    __iter_1 = set.iterator_k_()
    while __iter_1.hasNext_k_()
        item = __iter_1.next_k_()
        collected.add_AnyN_k_(item)

    end while

    assertEquals_AnyN_AnyN_StrN_k_(3, collected.__get_size(), invalid)
    assertTrue_Z_StrN_k_(collected.containsAll_Collection_k_(listOf_Arr_k_([1, 2, 3])), invalid)
end sub

function hashSetTests_lambda_lambda_11_create_k_() as Object
    this = {}
    this.__type = "hashSetTests_lambda_lambda_11"
    this.__proto = ["hashSetTests_lambda_lambda_11", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = hashSetTests_lambda_lambda_11_invoke_k_
    return this
end function

sub hashSetTests_lambda_lambda_11_invoke_k_()
    set = hashSetOf_Arr_k_([1, 2, 3])
    iter = set.iterator_k_()
    while iter.hasNext_k_()
        if iter.next_k_() = 2 then
            iter.remove_k_()
        end if
    end while
    assertEquals_AnyN_AnyN_StrN_k_(2, set.__get_size(), invalid)
    assertFalse_Z_StrN_k_(set.contains_AnyN_k_(2), invalid)
end sub

function hashSetTests_lambda_lambda_12_create_k_() as Object
    this = {}
    this.__type = "hashSetTests_lambda_lambda_12"
    this.__proto = ["hashSetTests_lambda_lambda_12", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = hashSetTests_lambda_lambda_12_invoke_k_
    return this
end function

sub hashSetTests_lambda_lambda_12_invoke_k_()
    set1 = hashSetOf_Arr_k_([1, 2, 3])
    set2 = hashSetOf_Arr_k_([1, 2, 3])
    set3 = hashSetOf_Arr_k_([1, 2, 4])
    assertEquals_AnyN_AnyN_StrN_k_(set1, set2, invalid)
    assertNotEquals_AnyN_AnyN_StrN_k_(set1, set3, invalid)
end sub

function hashSetTests_lambda_lambda_13_create_k_() as Object
    this = {}
    this.__type = "hashSetTests_lambda_lambda_13"
    this.__proto = ["hashSetTests_lambda_lambda_13", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = hashSetTests_lambda_lambda_13_invoke_k_
    return this
end function

sub hashSetTests_lambda_lambda_13_invoke_k_()
    set = hashSetOf_Arr_k_([1, 2, 3])
    str = set.toString()
    assertTrue_Z_StrN_k_(startsWith_rStr_Str_Z_k_(str, "[", invalid), invalid)
    assertTrue_Z_StrN_k_(endsWith_rStr_Str_Z_k_(str, "]", invalid), invalid)
end sub

function hashSetTests_lambda_lambda_14_create_k_() as Object
    this = {}
    this.__type = "hashSetTests_lambda_lambda_14"
    this.__proto = ["hashSetTests_lambda_lambda_14", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = hashSetTests_lambda_lambda_14_invoke_k_
    return this
end function

sub hashSetTests_lambda_lambda_14_invoke_k_()
    set = hashSetOf_Arr_k_(["apple", "banana", "cherry"])
    assertTrue_Z_StrN_k_(set.contains_AnyN_k_("banana"), invalid)
    assertFalse_Z_StrN_k_(set.contains_AnyN_k_("durian"), invalid)
end sub

function hashSetTests_lambda_create_k_() as Object
    this = {}
    this.__type = "hashSetTests_lambda"
    this.__proto = ["hashSetTests_lambda", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = hashSetTests_lambda_invoke_AnyN_k_
    return this
end function

sub hashSetTests_lambda_invoke_AnyN_k_(_this_suite as Object)
    _this_suite.test_Str_Function0V_k_("create empty", hashSetTests_lambda_lambda_create_k_())
    _this_suite.test_Str_Function0V_k_("create from collection", hashSetTests_lambda_lambda_1_create_k_())
    _this_suite.test_Str_Function0V_k_("add elements", hashSetTests_lambda_lambda_2_create_k_())
    _this_suite.test_Str_Function0V_k_("contains", hashSetTests_lambda_lambda_3_create_k_())
    _this_suite.test_Str_Function0V_k_("remove", hashSetTests_lambda_lambda_4_create_k_())
    _this_suite.test_Str_Function0V_k_("clear", hashSetTests_lambda_lambda_5_create_k_())
    _this_suite.test_Str_Function0V_k_("addAll", hashSetTests_lambda_lambda_6_create_k_())
    _this_suite.test_Str_Function0V_k_("removeAll", hashSetTests_lambda_lambda_7_create_k_())
    _this_suite.test_Str_Function0V_k_("retainAll", hashSetTests_lambda_lambda_8_create_k_())
    _this_suite.test_Str_Function0V_k_("containsAll", hashSetTests_lambda_lambda_9_create_k_())
    _this_suite.test_Str_Function0V_k_("iteration", hashSetTests_lambda_lambda_10_create_k_())
    _this_suite.test_Str_Function0V_k_("iterator remove", hashSetTests_lambda_lambda_11_create_k_())
    _this_suite.test_Str_Function0V_k_("equals", hashSetTests_lambda_lambda_12_create_k_())
    _this_suite.test_Str_Function0V_k_("toString", hashSetTests_lambda_lambda_13_create_k_())
    _this_suite.test_Str_Function0V_k_("string set", hashSetTests_lambda_lambda_14_create_k_())
end sub
