sub linkedHashSetTests_rTestRunner_k_(m as Object)
    m.suite_Str_Function1TestRunnerV_k_("LinkedHashSet", linkedHashSetTests_lambda_create_k_())
end sub

function linkedHashSetTests_lambda_lambda_create_k_() as Object
    this = {}
    this.__type = "linkedHashSetTests_lambda_lambda"
    this.__proto = ["linkedHashSetTests_lambda_lambda", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = linkedHashSetTests_lambda_lambda_invoke_k_
    return this
end function

sub linkedHashSetTests_lambda_lambda_invoke_k_()
    set = LinkedHashSet_create_k_()
    set.add_AnyN_k_("three")
    set.add_AnyN_k_("one")
    set.add_AnyN_k_("two")
    list = toList_rIterable_k_(set)
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_(["three", "one", "two"]), list, invalid)
end sub

function linkedHashSetTests_lambda_lambda_1_create_k_() as Object
    this = {}
    this.__type = "linkedHashSetTests_lambda_lambda_1"
    this.__proto = ["linkedHashSetTests_lambda_lambda_1", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = linkedHashSetTests_lambda_lambda_1_invoke_k_
    return this
end function

sub linkedHashSetTests_lambda_lambda_1_invoke_k_()
    set = LinkedHashSet_create_k_()
    assertTrue_Z_StrN_k_(set.add_AnyN_k_(1), invalid)
    assertTrue_Z_StrN_k_(set.add_AnyN_k_(2), invalid)
    assertFalse_Z_StrN_k_(set.add_AnyN_k_(1), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(2, set.__get_size(), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([1, 2]), toList_rIterable_k_(set), invalid)
end sub

function linkedHashSetTests_lambda_lambda_2_create_k_() as Object
    this = {}
    this.__type = "linkedHashSetTests_lambda_lambda_2"
    this.__proto = ["linkedHashSetTests_lambda_lambda_2", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = linkedHashSetTests_lambda_lambda_2_invoke_k_
    return this
end function

sub linkedHashSetTests_lambda_lambda_2_invoke_k_()
    list = listOf_Arr_k_([3, 1, 2, 1, 3])
    set = LinkedHashSet_create_Collection_k_(list)
    assertEquals_AnyN_AnyN_StrN_k_(3, set.__get_size(), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([3, 1, 2]), toList_rIterable_k_(set), invalid)
end sub

function linkedHashSetTests_lambda_lambda_3_create_k_() as Object
    this = {}
    this.__type = "linkedHashSetTests_lambda_lambda_3"
    this.__proto = ["linkedHashSetTests_lambda_lambda_3", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = linkedHashSetTests_lambda_lambda_3_invoke_k_
    return this
end function

sub linkedHashSetTests_lambda_lambda_3_invoke_k_()
    set = LinkedHashSet_create_k_()
    set.add_AnyN_k_("one")
    set.add_AnyN_k_("two")
    set.add_AnyN_k_("three")
    assertTrue_Z_StrN_k_(set.remove_AnyN_k_("two"), invalid)
    assertFalse_Z_StrN_k_(set.remove_AnyN_k_("four"), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(2, set.__get_size(), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_(["one", "three"]), toList_rIterable_k_(set), invalid)
end sub

function linkedHashSetTests_lambda_lambda_4_create_k_() as Object
    this = {}
    this.__type = "linkedHashSetTests_lambda_lambda_4"
    this.__proto = ["linkedHashSetTests_lambda_lambda_4", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = linkedHashSetTests_lambda_lambda_4_invoke_k_
    return this
end function

sub linkedHashSetTests_lambda_lambda_4_invoke_k_()
    set = LinkedHashSet_create_k_()
    set.add_AnyN_k_(1)
    set.add_AnyN_k_(2)
    set.add_AnyN_k_(3)
    assertTrue_Z_StrN_k_(set.contains_AnyN_k_(2), invalid)
    assertFalse_Z_StrN_k_(set.contains_AnyN_k_(5), invalid)
end sub

function linkedHashSetTests_lambda_lambda_5_create_k_() as Object
    this = {}
    this.__type = "linkedHashSetTests_lambda_lambda_5"
    this.__proto = ["linkedHashSetTests_lambda_lambda_5", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = linkedHashSetTests_lambda_lambda_5_invoke_k_
    return this
end function

sub linkedHashSetTests_lambda_lambda_5_invoke_k_()
    set = LinkedHashSet_create_k_()
    set.add_AnyN_k_(10)
    set.add_AnyN_k_(20)
    set.add_AnyN_k_(30)
    collected = mutableListOf_k_()
    __iter_2 = set.iterator_k_()
    while __iter_2.hasNext_k_()
        element = __iter_2.next_k_()
        collected.add_AnyN_k_(element)

    end while

    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([10, 20, 30]), collected, invalid)
end sub

function linkedHashSetTests_lambda_lambda_6_create_k_() as Object
    this = {}
    this.__type = "linkedHashSetTests_lambda_lambda_6"
    this.__proto = ["linkedHashSetTests_lambda_lambda_6", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = linkedHashSetTests_lambda_lambda_6_invoke_k_
    return this
end function

sub linkedHashSetTests_lambda_lambda_6_invoke_k_()
    set = LinkedHashSet_create_k_()
    set.add_AnyN_k_("a")
    set.add_AnyN_k_("b")
    set.add_AnyN_k_("c")
    iter = set.iterator_k_()
    while iter.hasNext_k_()
        if iter.next_k_() = "b" then
            iter.remove_k_()
        end if
    end while
    assertEquals_AnyN_AnyN_StrN_k_(2, set.__get_size(), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_(["a", "c"]), toList_rIterable_k_(set), invalid)
end sub

function linkedHashSetTests_lambda_lambda_7_create_k_() as Object
    this = {}
    this.__type = "linkedHashSetTests_lambda_lambda_7"
    this.__proto = ["linkedHashSetTests_lambda_lambda_7", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = linkedHashSetTests_lambda_lambda_7_invoke_k_
    return this
end function

sub linkedHashSetTests_lambda_lambda_7_invoke_k_()
    set = LinkedHashSet_create_k_()
    set.add_AnyN_k_(1)
    result = set.addAll_Collection_k_(listOf_Arr_k_([2, 3, 1]))
    assertTrue_Z_StrN_k_(result, invalid)
    assertEquals_AnyN_AnyN_StrN_k_(3, set.__get_size(), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([1, 2, 3]), toList_rIterable_k_(set), invalid)
end sub

function linkedHashSetTests_lambda_lambda_8_create_k_() as Object
    this = {}
    this.__type = "linkedHashSetTests_lambda_lambda_8"
    this.__proto = ["linkedHashSetTests_lambda_lambda_8", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = linkedHashSetTests_lambda_lambda_8_invoke_k_
    return this
end function

sub linkedHashSetTests_lambda_lambda_8_invoke_k_()
    set = LinkedHashSet_create_k_()
    set.addAll_Collection_k_(listOf_Arr_k_([1, 2, 3, 4, 5]))
    result = set.removeAll_Collection_k_(listOf_Arr_k_([2, 4]))
    assertTrue_Z_StrN_k_(result, invalid)
    assertEquals_AnyN_AnyN_StrN_k_(3, set.__get_size(), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([1, 3, 5]), toList_rIterable_k_(set), invalid)
end sub

function linkedHashSetTests_lambda_lambda_9_create_k_() as Object
    this = {}
    this.__type = "linkedHashSetTests_lambda_lambda_9"
    this.__proto = ["linkedHashSetTests_lambda_lambda_9", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = linkedHashSetTests_lambda_lambda_9_invoke_k_
    return this
end function

sub linkedHashSetTests_lambda_lambda_9_invoke_k_()
    set = LinkedHashSet_create_k_()
    set.addAll_Collection_k_(listOf_Arr_k_([1, 2, 3, 4, 5]))
    result = set.retainAll_Collection_k_(listOf_Arr_k_([2, 3, 6]))
    assertTrue_Z_StrN_k_(result, invalid)
    assertEquals_AnyN_AnyN_StrN_k_(2, set.__get_size(), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([2, 3]), toList_rIterable_k_(set), invalid)
end sub

function linkedHashSetTests_lambda_lambda_10_create_k_() as Object
    this = {}
    this.__type = "linkedHashSetTests_lambda_lambda_10"
    this.__proto = ["linkedHashSetTests_lambda_lambda_10", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = linkedHashSetTests_lambda_lambda_10_invoke_k_
    return this
end function

sub linkedHashSetTests_lambda_lambda_10_invoke_k_()
    set = LinkedHashSet_create_k_()
    set.addAll_Collection_k_(listOf_Arr_k_(["a", "b", "c"]))
    set.clear_k_()
    assertEquals_AnyN_AnyN_StrN_k_(0, set.__get_size(), invalid)
    assertTrue_Z_StrN_k_(set.isEmpty_k_(), invalid)
end sub

function linkedHashSetTests_lambda_create_k_() as Object
    this = {}
    this.__type = "linkedHashSetTests_lambda"
    this.__proto = ["linkedHashSetTests_lambda", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = linkedHashSetTests_lambda_invoke_AnyN_k_
    return this
end function

sub linkedHashSetTests_lambda_invoke_AnyN_k_(_this_suite as Object)
    _this_suite.test_Str_Function0V_k_("insertion order", linkedHashSetTests_lambda_lambda_create_k_())
    _this_suite.test_Str_Function0V_k_("no duplicates", linkedHashSetTests_lambda_lambda_1_create_k_())
    _this_suite.test_Str_Function0V_k_("constructor with collection", linkedHashSetTests_lambda_lambda_2_create_k_())
    _this_suite.test_Str_Function0V_k_("remove", linkedHashSetTests_lambda_lambda_3_create_k_())
    _this_suite.test_Str_Function0V_k_("contains", linkedHashSetTests_lambda_lambda_4_create_k_())
    _this_suite.test_Str_Function0V_k_("iterator", linkedHashSetTests_lambda_lambda_5_create_k_())
    _this_suite.test_Str_Function0V_k_("iterator remove", linkedHashSetTests_lambda_lambda_6_create_k_())
    _this_suite.test_Str_Function0V_k_("addAll", linkedHashSetTests_lambda_lambda_7_create_k_())
    _this_suite.test_Str_Function0V_k_("removeAll", linkedHashSetTests_lambda_lambda_8_create_k_())
    _this_suite.test_Str_Function0V_k_("retainAll", linkedHashSetTests_lambda_lambda_9_create_k_())
    _this_suite.test_Str_Function0V_k_("clear", linkedHashSetTests_lambda_lambda_10_create_k_())
end sub
