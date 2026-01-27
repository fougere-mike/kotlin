sub arrayListTests_rTestRunner_k_(m as Object)
    m.suite_Str_Function1TestRunnerV_k_("ArrayList", arrayListTests_lambda_create_k_())
end sub

function arrayListTests_lambda_lambda_create_k_() as Object
    this = {}
    this.__type = "arrayListTests_lambda_lambda"
    this.__proto = ["arrayListTests_lambda_lambda", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = arrayListTests_lambda_lambda_invoke_k_
    return this
end function

sub arrayListTests_lambda_lambda_invoke_k_()
    list = ArrayList_create_k_()
    assertTrue_Z_StrN_k_(list.isEmpty_k_(), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(0, list.__get_size(), invalid)
end sub

function arrayListTests_lambda_lambda_1_create_k_() as Object
    this = {}
    this.__type = "arrayListTests_lambda_lambda_1"
    this.__proto = ["arrayListTests_lambda_lambda_1", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = arrayListTests_lambda_lambda_1_invoke_k_
    return this
end function

sub arrayListTests_lambda_lambda_1_invoke_k_()
    list = ArrayList_create_I_k_(10)
    assertTrue_Z_StrN_k_(list.isEmpty_k_(), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(0, list.__get_size(), invalid)
end sub

function arrayListTests_lambda_lambda_2_create_k_() as Object
    this = {}
    this.__type = "arrayListTests_lambda_lambda_2"
    this.__proto = ["arrayListTests_lambda_lambda_2", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = arrayListTests_lambda_lambda_2_invoke_k_
    return this
end function

sub arrayListTests_lambda_lambda_2_invoke_k_()
    list = ArrayList_create_Collection_k_(listOf_Arr_k_([1, 2, 3]))
    assertEquals_AnyN_AnyN_StrN_k_(3, list.__get_size(), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(1, list.get_I_k_(0), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(2, list.get_I_k_(1), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(3, list.get_I_k_(2), invalid)
end sub

function arrayListTests_lambda_lambda_3_create_k_() as Object
    this = {}
    this.__type = "arrayListTests_lambda_lambda_3"
    this.__proto = ["arrayListTests_lambda_lambda_3", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = arrayListTests_lambda_lambda_3_invoke_k_
    return this
end function

sub arrayListTests_lambda_lambda_3_invoke_k_()
    list = ArrayList_create_k_()
    assertTrue_Z_StrN_k_(list.add_AnyN_k_(1), invalid)
    assertTrue_Z_StrN_k_(list.add_AnyN_k_(2), invalid)
    assertTrue_Z_StrN_k_(list.add_AnyN_k_(3), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(3, list.__get_size(), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(1, list.get_I_k_(0), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(2, list.get_I_k_(1), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(3, list.get_I_k_(2), invalid)
end sub

function arrayListTests_lambda_lambda_4_create_k_() as Object
    this = {}
    this.__type = "arrayListTests_lambda_lambda_4"
    this.__proto = ["arrayListTests_lambda_lambda_4", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = arrayListTests_lambda_lambda_4_invoke_k_
    return this
end function

sub arrayListTests_lambda_lambda_4_invoke_k_()
    list = arrayListOf_Arr_k_([1, 3])
    list.add_I_AnyN_k_(1, 2)
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([1, 2, 3]), toList_rIterable_k_(list), invalid)
end sub

function arrayListTests_lambda_lambda_5_create_k_() as Object
    this = {}
    this.__type = "arrayListTests_lambda_lambda_5"
    this.__proto = ["arrayListTests_lambda_lambda_5", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = arrayListTests_lambda_lambda_5_invoke_k_
    return this
end function

sub arrayListTests_lambda_lambda_5_invoke_k_()
    list = arrayListOf_Arr_k_([10, 20, 30])
    assertEquals_AnyN_AnyN_StrN_k_(10, list.get_I_k_(0), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(20, list.get_I_k_(1), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(30, list.get_I_k_(2), invalid)
end sub

function arrayListTests_lambda_lambda_6_create_k_() as Object
    this = {}
    this.__type = "arrayListTests_lambda_lambda_6"
    this.__proto = ["arrayListTests_lambda_lambda_6", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = arrayListTests_lambda_lambda_6_invoke_k_
    return this
end function

sub arrayListTests_lambda_lambda_6_invoke_k_()
    list = arrayListOf_Arr_k_([1, 2, 3])
    old = list.set_I_AnyN_k_(1, 20)
    assertEquals_AnyN_AnyN_StrN_k_(2, old, invalid)
    assertEquals_AnyN_AnyN_StrN_k_(20, list.get_I_k_(1), invalid)
end sub

function arrayListTests_lambda_lambda_7_create_k_() as Object
    this = {}
    this.__type = "arrayListTests_lambda_lambda_7"
    this.__proto = ["arrayListTests_lambda_lambda_7", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = arrayListTests_lambda_lambda_7_invoke_k_
    return this
end function

sub arrayListTests_lambda_lambda_7_invoke_k_()
    list = arrayListOf_Arr_k_([1, 2, 3])
    removed = list.removeAt_I_k_(1)
    assertEquals_AnyN_AnyN_StrN_k_(2, removed, invalid)
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([1, 3]), toList_rIterable_k_(list), invalid)
end sub

function arrayListTests_lambda_lambda_8_create_k_() as Object
    this = {}
    this.__type = "arrayListTests_lambda_lambda_8"
    this.__proto = ["arrayListTests_lambda_lambda_8", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = arrayListTests_lambda_lambda_8_invoke_k_
    return this
end function

sub arrayListTests_lambda_lambda_8_invoke_k_()
    list = arrayListOf_Arr_k_([1, 2, 3])
    assertTrue_Z_StrN_k_(list.remove_AnyN_k_(2), invalid)
    assertFalse_Z_StrN_k_(list.remove_AnyN_k_(5), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([1, 3]), toList_rIterable_k_(list), invalid)
end sub

function arrayListTests_lambda_lambda_9_create_k_() as Object
    this = {}
    this.__type = "arrayListTests_lambda_lambda_9"
    this.__proto = ["arrayListTests_lambda_lambda_9", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = arrayListTests_lambda_lambda_9_invoke_k_
    return this
end function

sub arrayListTests_lambda_lambda_9_invoke_k_()
    list = arrayListOf_Arr_k_([1, 2, 3])
    assertTrue_Z_StrN_k_(list.contains_AnyN_k_(2), invalid)
    assertFalse_Z_StrN_k_(list.contains_AnyN_k_(5), invalid)
end sub

function arrayListTests_lambda_lambda_10_create_k_() as Object
    this = {}
    this.__type = "arrayListTests_lambda_lambda_10"
    this.__proto = ["arrayListTests_lambda_lambda_10", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = arrayListTests_lambda_lambda_10_invoke_k_
    return this
end function

sub arrayListTests_lambda_lambda_10_invoke_k_()
    list = arrayListOf_Arr_k_([1, 2, 3, 2])
    assertEquals_AnyN_AnyN_StrN_k_(1, list.indexOf_AnyN_k_(2), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(-1, list.indexOf_AnyN_k_(5), invalid)
end sub

function arrayListTests_lambda_lambda_11_create_k_() as Object
    this = {}
    this.__type = "arrayListTests_lambda_lambda_11"
    this.__proto = ["arrayListTests_lambda_lambda_11", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = arrayListTests_lambda_lambda_11_invoke_k_
    return this
end function

sub arrayListTests_lambda_lambda_11_invoke_k_()
    list = arrayListOf_Arr_k_([1, 2, 3, 2])
    assertEquals_AnyN_AnyN_StrN_k_(3, list.lastIndexOf_AnyN_k_(2), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(-1, list.lastIndexOf_AnyN_k_(5), invalid)
end sub

function arrayListTests_lambda_lambda_12_create_k_() as Object
    this = {}
    this.__type = "arrayListTests_lambda_lambda_12"
    this.__proto = ["arrayListTests_lambda_lambda_12", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = arrayListTests_lambda_lambda_12_invoke_k_
    return this
end function

sub arrayListTests_lambda_lambda_12_invoke_k_()
    list = arrayListOf_Arr_k_([1, 2, 3])
    list.clear_k_()
    assertTrue_Z_StrN_k_(list.isEmpty_k_(), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(0, list.__get_size(), invalid)
end sub

function arrayListTests_lambda_lambda_13_create_k_() as Object
    this = {}
    this.__type = "arrayListTests_lambda_lambda_13"
    this.__proto = ["arrayListTests_lambda_lambda_13", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = arrayListTests_lambda_lambda_13_invoke_k_
    return this
end function

sub arrayListTests_lambda_lambda_13_invoke_k_()
    list = arrayListOf_Arr_k_([1, 2])
    list.addAll_Collection_k_(listOf_Arr_k_([3, 4, 5]))
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([1, 2, 3, 4, 5]), toList_rIterable_k_(list), invalid)
end sub

function arrayListTests_lambda_lambda_14_create_k_() as Object
    this = {}
    this.__type = "arrayListTests_lambda_lambda_14"
    this.__proto = ["arrayListTests_lambda_lambda_14", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = arrayListTests_lambda_lambda_14_invoke_k_
    return this
end function

sub arrayListTests_lambda_lambda_14_invoke_k_()
    list = arrayListOf_Arr_k_([1, 5])
    list.addAll_I_Collection_k_(1, listOf_Arr_k_([2, 3, 4]))
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([1, 2, 3, 4, 5]), toList_rIterable_k_(list), invalid)
end sub

function arrayListTests_lambda_lambda_15_create_k_() as Object
    this = {}
    this.__type = "arrayListTests_lambda_lambda_15"
    this.__proto = ["arrayListTests_lambda_lambda_15", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = arrayListTests_lambda_lambda_15_invoke_k_
    return this
end function

sub arrayListTests_lambda_lambda_15_invoke_k_()
    list = arrayListOf_Arr_k_([1, 2, 3, 4, 5])
    list.removeAll_Collection_k_(listOf_Arr_k_([2, 4]))
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([1, 3, 5]), toList_rIterable_k_(list), invalid)
end sub

function arrayListTests_lambda_lambda_16_create_k_() as Object
    this = {}
    this.__type = "arrayListTests_lambda_lambda_16"
    this.__proto = ["arrayListTests_lambda_lambda_16", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = arrayListTests_lambda_lambda_16_invoke_k_
    return this
end function

sub arrayListTests_lambda_lambda_16_invoke_k_()
    list = arrayListOf_Arr_k_([1, 2, 3, 4, 5])
    list.retainAll_Collection_k_(listOf_Arr_k_([2, 3, 6]))
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([2, 3]), toList_rIterable_k_(list), invalid)
end sub

function arrayListTests_lambda_lambda_17_create_k_() as Object
    this = {}
    this.__type = "arrayListTests_lambda_lambda_17"
    this.__proto = ["arrayListTests_lambda_lambda_17", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = arrayListTests_lambda_lambda_17_invoke_k_
    return this
end function

sub arrayListTests_lambda_lambda_17_invoke_k_()
    list = arrayListOf_Arr_k_([1, 2, 3, 4, 5])
    subview = list.subList_I_I_k_(1, 4)
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([2, 3, 4]), toList_rIterable_k_(subview), invalid)
end sub

function arrayListTests_lambda_lambda_18_create_k_() as Object
    this = {}
    this.__type = "arrayListTests_lambda_lambda_18"
    this.__proto = ["arrayListTests_lambda_lambda_18", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = arrayListTests_lambda_lambda_18_invoke_k_
    return this
end function

sub arrayListTests_lambda_lambda_18_invoke_k_()
    list = arrayListOf_Arr_k_([1, 2, 3])
    collected = mutableListOf_k_()
    iter = list.iterator_k_()
    while iter.hasNext_k_()
        collected.add_AnyN_k_(iter.next_k_())
    end while
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([1, 2, 3]), collected, invalid)
end sub

function arrayListTests_lambda_lambda_19_create_k_() as Object
    this = {}
    this.__type = "arrayListTests_lambda_lambda_19"
    this.__proto = ["arrayListTests_lambda_lambda_19", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = arrayListTests_lambda_lambda_19_invoke_k_
    return this
end function

sub arrayListTests_lambda_lambda_19_invoke_k_()
    list = arrayListOf_Arr_k_([1, 2, 3])
    iter = list.iterator_k_()
    while iter.hasNext_k_()
        if iter.next_k_() = 2 then
            iter.remove_k_()
        end if
    end while
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([1, 3]), toList_rIterable_k_(list), invalid)
end sub

function arrayListTests_lambda_lambda_20_create_k_() as Object
    this = {}
    this.__type = "arrayListTests_lambda_lambda_20"
    this.__proto = ["arrayListTests_lambda_lambda_20", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = arrayListTests_lambda_lambda_20_invoke_k_
    return this
end function

sub arrayListTests_lambda_lambda_20_invoke_k_()
    list = arrayListOf_Arr_k_([1, 2, 3])
    collected = mutableListOf_k_()
    for each item in list.__get_array()
        collected.add_AnyN_k_(item)

    end for
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([1, 2, 3]), collected, invalid)
end sub

function arrayListTests_lambda_lambda_21_create_k_() as Object
    this = {}
    this.__type = "arrayListTests_lambda_lambda_21"
    this.__proto = ["arrayListTests_lambda_lambda_21", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = arrayListTests_lambda_lambda_21_invoke_k_
    return this
end function

sub arrayListTests_lambda_lambda_21_invoke_k_()
    list1 = arrayListOf_Arr_k_([1, 2, 3])
    list2 = arrayListOf_Arr_k_([1, 2, 3])
    list3 = arrayListOf_Arr_k_([1, 2, 4])
    assertEquals_AnyN_AnyN_StrN_k_(list1, list2, invalid)
    assertNotEquals_AnyN_AnyN_StrN_k_(list1, list3, invalid)
end sub

function arrayListTests_lambda_lambda_22_create_k_() as Object
    this = {}
    this.__type = "arrayListTests_lambda_lambda_22"
    this.__proto = ["arrayListTests_lambda_lambda_22", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = arrayListTests_lambda_lambda_22_invoke_k_
    return this
end function

sub arrayListTests_lambda_lambda_22_invoke_k_()
    list = arrayListOf_Arr_k_([1, 2, 3])
    assertEquals_AnyN_AnyN_StrN_k_("[1, 2, 3]", list.toString(), invalid)
end sub

function arrayListTests_lambda_create_k_() as Object
    this = {}
    this.__type = "arrayListTests_lambda"
    this.__proto = ["arrayListTests_lambda", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = arrayListTests_lambda_invoke_AnyN_k_
    return this
end function

sub arrayListTests_lambda_invoke_AnyN_k_(_this_suite as Object)
    _this_suite.test_Str_Function0V_k_("create empty", arrayListTests_lambda_lambda_create_k_())
    _this_suite.test_Str_Function0V_k_("create with initial capacity", arrayListTests_lambda_lambda_1_create_k_())
    _this_suite.test_Str_Function0V_k_("create from collection", arrayListTests_lambda_lambda_2_create_k_())
    _this_suite.test_Str_Function0V_k_("add elements", arrayListTests_lambda_lambda_3_create_k_())
    _this_suite.test_Str_Function0V_k_("add at index", arrayListTests_lambda_lambda_4_create_k_())
    _this_suite.test_Str_Function0V_k_("get element", arrayListTests_lambda_lambda_5_create_k_())
    _this_suite.test_Str_Function0V_k_("set element", arrayListTests_lambda_lambda_6_create_k_())
    _this_suite.test_Str_Function0V_k_("remove at index", arrayListTests_lambda_lambda_7_create_k_())
    _this_suite.test_Str_Function0V_k_("remove element", arrayListTests_lambda_lambda_8_create_k_())
    _this_suite.test_Str_Function0V_k_("contains", arrayListTests_lambda_lambda_9_create_k_())
    _this_suite.test_Str_Function0V_k_("indexOf", arrayListTests_lambda_lambda_10_create_k_())
    _this_suite.test_Str_Function0V_k_("lastIndexOf", arrayListTests_lambda_lambda_11_create_k_())
    _this_suite.test_Str_Function0V_k_("clear", arrayListTests_lambda_lambda_12_create_k_())
    _this_suite.test_Str_Function0V_k_("addAll", arrayListTests_lambda_lambda_13_create_k_())
    _this_suite.test_Str_Function0V_k_("addAll at index", arrayListTests_lambda_lambda_14_create_k_())
    _this_suite.test_Str_Function0V_k_("removeAll", arrayListTests_lambda_lambda_15_create_k_())
    _this_suite.test_Str_Function0V_k_("retainAll", arrayListTests_lambda_lambda_16_create_k_())
    _this_suite.test_Str_Function0V_k_("subList", arrayListTests_lambda_lambda_17_create_k_())
    _this_suite.test_Str_Function0V_k_("iterator", arrayListTests_lambda_lambda_18_create_k_())
    _this_suite.test_Str_Function0V_k_("iterator remove", arrayListTests_lambda_lambda_19_create_k_())
    _this_suite.test_Str_Function0V_k_("for-each loop", arrayListTests_lambda_lambda_20_create_k_())
    _this_suite.test_Str_Function0V_k_("equals", arrayListTests_lambda_lambda_21_create_k_())
    _this_suite.test_Str_Function0V_k_("toString", arrayListTests_lambda_lambda_22_create_k_())
end sub
