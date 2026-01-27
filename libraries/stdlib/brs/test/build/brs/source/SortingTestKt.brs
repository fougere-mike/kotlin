sub sortingTests_rTestRunner_k_(m as Object)
    m.suite_Str_Function1TestRunnerV_k_("Sorting", sortingTests_lambda_create_k_())
end sub

function sortingTests_lambda_lambda_create_k_() as Object
    this = {}
    this.__type = "sortingTests_lambda_lambda"
    this.__proto = ["sortingTests_lambda_lambda", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = sortingTests_lambda_lambda_invoke_k_
    return this
end function

sub sortingTests_lambda_lambda_invoke_k_()
    list = mutableListOf_Arr_k_([5, 2, 8, 1, 9, 3])
    sort_rMutableList_k_(list)
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([1, 2, 3, 5, 8, 9]), list, invalid)
end sub

function sortingTests_lambda_lambda_1_create_k_() as Object
    this = {}
    this.__type = "sortingTests_lambda_lambda_1"
    this.__proto = ["sortingTests_lambda_lambda_1", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = sortingTests_lambda_lambda_1_invoke_k_
    return this
end function

sub sortingTests_lambda_lambda_1_invoke_k_()
    list = mutableListOf_Arr_k_(["dog", "cat", "apple", "zebra"])
    sort_rMutableList_k_(list)
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_(["apple", "cat", "dog", "zebra"]), list, invalid)
end sub

function sortingTests_lambda_lambda_2_create_k_() as Object
    this = {}
    this.__type = "sortingTests_lambda_lambda_2"
    this.__proto = ["sortingTests_lambda_lambda_2", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = sortingTests_lambda_lambda_2_invoke_k_
    return this
end function

sub sortingTests_lambda_lambda_2_invoke_k_()
    list = mutableListOf_Arr_k_([5, 2, 8, 1, 9, 3])
    sortWith_rMutableList_Comparator_k_(list, reverseOrder_k_())
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([9, 8, 5, 3, 2, 1]), list, invalid)
end sub

function sortingTests_lambda_lambda_3_create_k_() as Object
    this = {}
    this.__type = "sortingTests_lambda_lambda_3"
    this.__proto = ["sortingTests_lambda_lambda_3", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = sortingTests_lambda_lambda_3_invoke_k_
    return this
end function

sub sortingTests_lambda_lambda_3_invoke_k_()
    list = mutableListOf_Arr_k_([5, 2, 8, 1, 9, 3])
    sortDescending_rMutableList_k_(list)
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([9, 8, 5, 3, 2, 1]), list, invalid)
end sub

function sortingTests_lambda_lambda_lambda_create_k_() as Object
    this = {}
    this.__type = "sortingTests_lambda_lambda_lambda"
    this.__proto = ["sortingTests_lambda_lambda_lambda", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = sortingTests_lambda_lambda_lambda_invoke_AnyN_k_
    return this
end function

function sortingTests_lambda_lambda_lambda_invoke_AnyN_k_(it as String) as Dynamic
    return Len(it)
end function

function sortingTests_lambda_lambda_4_create_k_() as Object
    this = {}
    this.__type = "sortingTests_lambda_lambda_4"
    this.__proto = ["sortingTests_lambda_lambda_4", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = sortingTests_lambda_lambda_4_invoke_k_
    return this
end function

sub sortingTests_lambda_lambda_4_invoke_k_()
    list = mutableListOf_Arr_k_(["apple", "pie", "a", "zoo"])
    sortBy_rMutableList_Function1_k_(list, sortingTests_lambda_lambda_lambda_create_k_())
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_(["a", "pie", "zoo", "apple"]), list, invalid)
end sub

function sortingTests_lambda_lambda_lambda_1_create_k_() as Object
    this = {}
    this.__type = "sortingTests_lambda_lambda_lambda_1"
    this.__proto = ["sortingTests_lambda_lambda_lambda_1", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = sortingTests_lambda_lambda_lambda_1_invoke_AnyN_k_
    return this
end function

function sortingTests_lambda_lambda_lambda_1_invoke_AnyN_k_(it as String) as Dynamic
    return Len(it)
end function

function sortingTests_lambda_lambda_5_create_k_() as Object
    this = {}
    this.__type = "sortingTests_lambda_lambda_5"
    this.__proto = ["sortingTests_lambda_lambda_5", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = sortingTests_lambda_lambda_5_invoke_k_
    return this
end function

sub sortingTests_lambda_lambda_5_invoke_k_()
    list = mutableListOf_Arr_k_(["apple", "pie", "a", "zoo"])
    sortByDescending_rMutableList_Function1_k_(list, sortingTests_lambda_lambda_lambda_1_create_k_())
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_(["apple", "pie", "zoo", "a"]), list, invalid)
end sub

function sortingTests_lambda_lambda_6_create_k_() as Object
    this = {}
    this.__type = "sortingTests_lambda_lambda_6"
    this.__proto = ["sortingTests_lambda_lambda_6", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = sortingTests_lambda_lambda_6_invoke_k_
    return this
end function

sub sortingTests_lambda_lambda_6_invoke_k_()
    list = listOf_Arr_k_([5, 2, 8, 1, 9, 3])
    sorted = sorted_rIterable_k_(list)
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([5, 2, 8, 1, 9, 3]), list, invalid)
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([1, 2, 3, 5, 8, 9]), sorted, invalid)
end sub

function sortingTests_lambda_lambda_7_create_k_() as Object
    this = {}
    this.__type = "sortingTests_lambda_lambda_7"
    this.__proto = ["sortingTests_lambda_lambda_7", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = sortingTests_lambda_lambda_7_invoke_k_
    return this
end function

sub sortingTests_lambda_lambda_7_invoke_k_()
    list = listOf_Arr_k_([5, 2, 8, 1, 9, 3])
    sorted = sortedWith_rIterable_Comparator_k_(list, reverseOrder_k_())
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([9, 8, 5, 3, 2, 1]), sorted, invalid)
end sub

function sortingTests_lambda_lambda_8_create_k_() as Object
    this = {}
    this.__type = "sortingTests_lambda_lambda_8"
    this.__proto = ["sortingTests_lambda_lambda_8", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = sortingTests_lambda_lambda_8_invoke_k_
    return this
end function

sub sortingTests_lambda_lambda_8_invoke_k_()
    list = listOf_Arr_k_([5, 2, 8, 1, 9, 3])
    sorted = sortedDescending_rIterable_k_(list)
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([9, 8, 5, 3, 2, 1]), sorted, invalid)
end sub

function sortingTests_lambda_lambda_lambda_2_create_k_() as Object
    this = {}
    this.__type = "sortingTests_lambda_lambda_lambda_2"
    this.__proto = ["sortingTests_lambda_lambda_lambda_2", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = sortingTests_lambda_lambda_lambda_2_invoke_AnyN_k_
    return this
end function

function sortingTests_lambda_lambda_lambda_2_invoke_AnyN_k_(it as String) as Dynamic
    return Len(it)
end function

function sortingTests_lambda_lambda_9_create_k_() as Object
    this = {}
    this.__type = "sortingTests_lambda_lambda_9"
    this.__proto = ["sortingTests_lambda_lambda_9", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = sortingTests_lambda_lambda_9_invoke_k_
    return this
end function

sub sortingTests_lambda_lambda_9_invoke_k_()
    list = listOf_Arr_k_(["apple", "pie", "a", "zoo"])
    sorted = sortedBy_rIterable_Function1_k_(list, sortingTests_lambda_lambda_lambda_2_create_k_())
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_(["a", "pie", "zoo", "apple"]), sorted, invalid)
end sub

function sortingTests_lambda_lambda_lambda_3_create_k_() as Object
    this = {}
    this.__type = "sortingTests_lambda_lambda_lambda_3"
    this.__proto = ["sortingTests_lambda_lambda_lambda_3", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = sortingTests_lambda_lambda_lambda_3_invoke_AnyN_k_
    return this
end function

function sortingTests_lambda_lambda_lambda_3_invoke_AnyN_k_(it as String) as Dynamic
    return Len(it)
end function

function sortingTests_lambda_lambda_10_create_k_() as Object
    this = {}
    this.__type = "sortingTests_lambda_lambda_10"
    this.__proto = ["sortingTests_lambda_lambda_10", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = sortingTests_lambda_lambda_10_invoke_k_
    return this
end function

sub sortingTests_lambda_lambda_10_invoke_k_()
    list = listOf_Arr_k_(["apple", "pie", "a", "zoo"])
    sorted = sortedByDescending_rIterable_Function1_k_(list, sortingTests_lambda_lambda_lambda_3_create_k_())
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_(["apple", "pie", "zoo", "a"]), sorted, invalid)
end sub

function sortingTests_lambda_lambda_11_create_k_() as Object
    this = {}
    this.__type = "sortingTests_lambda_lambda_11"
    this.__proto = ["sortingTests_lambda_lambda_11", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = sortingTests_lambda_lambda_11_invoke_k_
    return this
end function

sub sortingTests_lambda_lambda_11_invoke_k_()
    list = mutableListOf_k_()
    sort_rMutableList_k_(list)
    assertEquals_AnyN_AnyN_StrN_k_(emptyList_k_(), list, invalid)
end sub

function sortingTests_lambda_lambda_12_create_k_() as Object
    this = {}
    this.__type = "sortingTests_lambda_lambda_12"
    this.__proto = ["sortingTests_lambda_lambda_12", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = sortingTests_lambda_lambda_12_invoke_k_
    return this
end function

sub sortingTests_lambda_lambda_12_invoke_k_()
    list = mutableListOf_Arr_k_([42])
    sort_rMutableList_k_(list)
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([42]), list, invalid)
end sub

function sortingTests_lambda_lambda_13_create_k_() as Object
    this = {}
    this.__type = "sortingTests_lambda_lambda_13"
    this.__proto = ["sortingTests_lambda_lambda_13", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = sortingTests_lambda_lambda_13_invoke_k_
    return this
end function

sub sortingTests_lambda_lambda_13_invoke_k_()
    list = mutableListOf_k_()
    inductionVariable = 100
    if 1 <= inductionVariable then
                i = inductionVariable
        inductionVariable = (inductionVariable + -1)

        list.add_AnyN_k_(i)


        while 1 <= inductionVariable
            i = inductionVariable
            inductionVariable = (inductionVariable + -1)

            list.add_AnyN_k_(i)

        end while

    end if

    sort_rMutableList_k_(list)
    progression = until_rI_I_k_(0, 100)
    inductionVariable_1 = progression.__get_first()
    last = progression.__get_last()
    if inductionVariable_1 <= last then
                i_1 = inductionVariable_1
        inductionVariable_1 = (inductionVariable_1 + 1)

        assertEquals_AnyN_AnyN_StrN_k_(i_1 + 1, list.get_I_k_(i_1), invalid)


        while i_1 <> last
            i_1 = inductionVariable_1
            inductionVariable_1 = (inductionVariable_1 + 1)

            assertEquals_AnyN_AnyN_StrN_k_(i_1 + 1, list.get_I_k_(i_1), invalid)

        end while

    end if

end sub

function sortingTests_lambda_lambda_14_create_k_() as Object
    this = {}
    this.__type = "sortingTests_lambda_lambda_14"
    this.__proto = ["sortingTests_lambda_lambda_14", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = sortingTests_lambda_lambda_14_invoke_k_
    return this
end function

sub sortingTests_lambda_lambda_14_invoke_k_()
    list = mutableListOf_Arr_k_([5, 2, 8, 1, 9, 3])
    sortWith_rMutableList_Comparator_k_(list, reversed_rComparator_k_(naturalOrder_k_()))
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([9, 8, 5, 3, 2, 1]), list, invalid)
end sub

function sortingTests_lambda_lambda_15_create_k_() as Object
    this = {}
    this.__type = "sortingTests_lambda_lambda_15"
    this.__proto = ["sortingTests_lambda_lambda_15", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = sortingTests_lambda_lambda_15_invoke_k_
    return this
end function

sub sortingTests_lambda_lambda_15_invoke_k_()
    assertEquals_AnyN_AnyN_StrN_k_(0, compareValues_AnyN_AnyN_k_(5, 5), invalid)
    assertTrue_Z_StrN_k_(compareValues_AnyN_AnyN_k_(3, 5) < 0, invalid)
    assertTrue_Z_StrN_k_(compareValues_AnyN_AnyN_k_(5, 3) > 0, invalid)
    assertTrue_Z_StrN_k_(compareValues_AnyN_AnyN_k_(invalid, 5) < 0, invalid)
    assertTrue_Z_StrN_k_(compareValues_AnyN_AnyN_k_(5, invalid) > 0, invalid)
    assertEquals_AnyN_AnyN_StrN_k_(0, compareValues_AnyN_AnyN_k_(invalid, invalid), invalid)
end sub

function sortingTests_lambda_create_k_() as Object
    this = {}
    this.__type = "sortingTests_lambda"
    this.__proto = ["sortingTests_lambda", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = sortingTests_lambda_invoke_AnyN_k_
    return this
end function

sub sortingTests_lambda_invoke_AnyN_k_(_this_suite as Object)
    _this_suite.test_Str_Function0V_k_("sort integers", sortingTests_lambda_lambda_create_k_())
    _this_suite.test_Str_Function0V_k_("sort strings", sortingTests_lambda_lambda_1_create_k_())
    _this_suite.test_Str_Function0V_k_("sort with comparator", sortingTests_lambda_lambda_2_create_k_())
    _this_suite.test_Str_Function0V_k_("sort descending", sortingTests_lambda_lambda_3_create_k_())
    _this_suite.test_Str_Function0V_k_("sortBy", sortingTests_lambda_lambda_4_create_k_())
    _this_suite.test_Str_Function0V_k_("sortByDescending", sortingTests_lambda_lambda_5_create_k_())
    _this_suite.test_Str_Function0V_k_("sorted (immutable)", sortingTests_lambda_lambda_6_create_k_())
    _this_suite.test_Str_Function0V_k_("sortedWith", sortingTests_lambda_lambda_7_create_k_())
    _this_suite.test_Str_Function0V_k_("sortedDescending", sortingTests_lambda_lambda_8_create_k_())
    _this_suite.test_Str_Function0V_k_("sortedBy", sortingTests_lambda_lambda_9_create_k_())
    _this_suite.test_Str_Function0V_k_("sortedByDescending", sortingTests_lambda_lambda_10_create_k_())
    _this_suite.test_Str_Function0V_k_("sort empty list", sortingTests_lambda_lambda_11_create_k_())
    _this_suite.test_Str_Function0V_k_("sort single element", sortingTests_lambda_lambda_12_create_k_())
    _this_suite.test_Str_Function0V_k_("sort large list", sortingTests_lambda_lambda_13_create_k_())
    _this_suite.test_Str_Function0V_k_("comparator reversed", sortingTests_lambda_lambda_14_create_k_())
    _this_suite.test_Str_Function0V_k_("compareValues", sortingTests_lambda_lambda_15_create_k_())
end sub
