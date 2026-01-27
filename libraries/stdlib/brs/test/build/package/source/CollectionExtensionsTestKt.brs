sub collectionExtensionsTests_rTestRunner_k_(m as Object)
    m.suite_Str_Function1TestRunnerV_k_("Collection Extensions", collectionExtensionsTests_lambda_create_k_())
end sub

function collectionExtensionsTests_lambda_lambda_lambda_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_lambda"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_lambda", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = collectionExtensionsTests_lambda_lambda_lambda_invoke_AnyN_k_
    return this
end function

function collectionExtensionsTests_lambda_lambda_lambda_invoke_AnyN_k_(it as Integer) as Integer
    return it * 2
end function

function collectionExtensionsTests_lambda_lambda_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda"
    this.__proto = ["collectionExtensionsTests_lambda_lambda", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = collectionExtensionsTests_lambda_lambda_invoke_k_
    return this
end function

sub collectionExtensionsTests_lambda_lambda_invoke_k_()
    list = listOf_Arr_k_([1, 2, 3])
    result = map_rIterable_Function1_k_(list, collectionExtensionsTests_lambda_lambda_lambda_create_k_())
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([2, 4, 6]), result, invalid)
end sub

function collectionExtensionsTests_lambda_lambda_lambda_1_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_lambda_1"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_lambda_1", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = collectionExtensionsTests_lambda_lambda_lambda_1_invoke_AnyN_k_
    return this
end function

function collectionExtensionsTests_lambda_lambda_lambda_1_invoke_AnyN_k_(it as Integer) as Dynamic
    __when_tmp0 = invalid
    if (it mod 2) = 0 then
        __when_tmp0 = (it * 2)
    else if true then
        __when_tmp0 = invalid
    end if
    return __when_tmp0

end function

function collectionExtensionsTests_lambda_lambda_1_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_1"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_1", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = collectionExtensionsTests_lambda_lambda_1_invoke_k_
    return this
end function

sub collectionExtensionsTests_lambda_lambda_1_invoke_k_()
    list = listOf_Arr_k_([1, 2, 3, 4])
    result = mapNotNull_rIterable_Function1_k_(list, collectionExtensionsTests_lambda_lambda_lambda_1_create_k_())
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([4, 8]), result, invalid)
end sub

function collectionExtensionsTests_lambda_lambda_lambda_2_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_lambda_2"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_lambda_2", "Function2", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_AnyN_k_ = collectionExtensionsTests_lambda_lambda_lambda_2_invoke_AnyN_AnyN_k_
    return this
end function

function collectionExtensionsTests_lambda_lambda_lambda_2_invoke_AnyN_AnyN_k_(index as Integer, value as String) as String
    return (__kotlin_numToStr_I_k_(index) + ":") + value
end function

function collectionExtensionsTests_lambda_lambda_2_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_2"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_2", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = collectionExtensionsTests_lambda_lambda_2_invoke_k_
    return this
end function

sub collectionExtensionsTests_lambda_lambda_2_invoke_k_()
    list = listOf_Arr_k_(["a", "b", "c"])
    result = mapIndexed_rIterable_Function2I_k_(list, collectionExtensionsTests_lambda_lambda_lambda_2_create_k_())
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_(["0:a", "1:b", "2:c"]), result, invalid)
end sub

function collectionExtensionsTests_lambda_lambda_lambda_3_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_lambda_3"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_lambda_3", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = collectionExtensionsTests_lambda_lambda_lambda_3_invoke_AnyN_k_
    return this
end function

function collectionExtensionsTests_lambda_lambda_lambda_3_invoke_AnyN_k_(it as Integer) as Object
    return listOf_Arr_k_([it, it * 10])
end function

function collectionExtensionsTests_lambda_lambda_3_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_3"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_3", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = collectionExtensionsTests_lambda_lambda_3_invoke_k_
    return this
end function

sub collectionExtensionsTests_lambda_lambda_3_invoke_k_()
    list = listOf_Arr_k_([1, 2, 3])
    result = flatMap_rIterable_Function1Iterable_k_(list, collectionExtensionsTests_lambda_lambda_lambda_3_create_k_())
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([1, 10, 2, 20, 3, 30]), result, invalid)
end sub

function collectionExtensionsTests_lambda_lambda_lambda_4_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_lambda_4"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_lambda_4", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = collectionExtensionsTests_lambda_lambda_lambda_4_invoke_AnyN_k_
    return this
end function

function collectionExtensionsTests_lambda_lambda_lambda_4_invoke_AnyN_k_(it as Integer) as Boolean
    return it > 3
end function

function collectionExtensionsTests_lambda_lambda_4_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_4"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_4", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = collectionExtensionsTests_lambda_lambda_4_invoke_k_
    return this
end function

sub collectionExtensionsTests_lambda_lambda_4_invoke_k_()
    list = listOf_Arr_k_([1, 2, 3, 4, 5])
    result = filter_rIterable_Function1Z_k_(list, collectionExtensionsTests_lambda_lambda_lambda_4_create_k_())
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([4, 5]), result, invalid)
end sub

function collectionExtensionsTests_lambda_lambda_lambda_5_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_lambda_5"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_lambda_5", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = collectionExtensionsTests_lambda_lambda_lambda_5_invoke_AnyN_k_
    return this
end function

function collectionExtensionsTests_lambda_lambda_lambda_5_invoke_AnyN_k_(it as Integer) as Boolean
    return it > 3
end function

function collectionExtensionsTests_lambda_lambda_5_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_5"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_5", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = collectionExtensionsTests_lambda_lambda_5_invoke_k_
    return this
end function

sub collectionExtensionsTests_lambda_lambda_5_invoke_k_()
    list = listOf_Arr_k_([1, 2, 3, 4, 5])
    result = filterNot_rIterable_Function1Z_k_(list, collectionExtensionsTests_lambda_lambda_lambda_5_create_k_())
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([1, 2, 3]), result, invalid)
end sub

function collectionExtensionsTests_lambda_lambda_lambda_6_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_lambda_6"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_lambda_6", "Function2", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_AnyN_k_ = collectionExtensionsTests_lambda_lambda_lambda_6_invoke_AnyN_AnyN_k_
    return this
end function

function collectionExtensionsTests_lambda_lambda_lambda_6_invoke_AnyN_AnyN_k_(index as Integer, _unused as String) as Boolean
    return (index mod 2) = 0
end function

function collectionExtensionsTests_lambda_lambda_6_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_6"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_6", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = collectionExtensionsTests_lambda_lambda_6_invoke_k_
    return this
end function

sub collectionExtensionsTests_lambda_lambda_6_invoke_k_()
    list = listOf_Arr_k_(["a", "b", "c", "d"])
    result = filterIndexed_rIterable_Function2IZ_k_(list, collectionExtensionsTests_lambda_lambda_lambda_6_create_k_())
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_(["a", "c"]), result, invalid)
end sub

function collectionExtensionsTests_lambda_lambda_7_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_7"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_7", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = collectionExtensionsTests_lambda_lambda_7_invoke_k_
    return this
end function

sub collectionExtensionsTests_lambda_lambda_7_invoke_k_()
    list = listOf_Arr_k_([1, invalid, 2, invalid, 3])
    result = filterNotNull_rIterable_k_(list)
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([1, 2, 3]), result, invalid)
end sub

function collectionExtensionsTests_lambda_lambda_lambda_7_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_lambda_7"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_lambda_7", "Function2", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_AnyN_k_ = collectionExtensionsTests_lambda_lambda_lambda_7_invoke_AnyN_AnyN_k_
    return this
end function

function collectionExtensionsTests_lambda_lambda_lambda_7_invoke_AnyN_AnyN_k_(acc as Integer, n as Integer) as Integer
    return acc + n
end function

function collectionExtensionsTests_lambda_lambda_8_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_8"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_8", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = collectionExtensionsTests_lambda_lambda_8_invoke_k_
    return this
end function

sub collectionExtensionsTests_lambda_lambda_8_invoke_k_()
    list = listOf_Arr_k_([1, 2, 3, 4])
    sum = fold_rIterable_AnyN_Function2_k_(list, 0, collectionExtensionsTests_lambda_lambda_lambda_7_create_k_())
    assertEquals_AnyN_AnyN_StrN_k_(10, sum, invalid)
end sub

function collectionExtensionsTests_lambda_lambda_lambda_8_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_lambda_8"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_lambda_8", "Function2", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_AnyN_k_ = collectionExtensionsTests_lambda_lambda_lambda_8_invoke_AnyN_AnyN_k_
    return this
end function

function collectionExtensionsTests_lambda_lambda_lambda_8_invoke_AnyN_AnyN_k_(acc as Integer, n as Integer) as Integer
    return acc * n
end function

function collectionExtensionsTests_lambda_lambda_9_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_9"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_9", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = collectionExtensionsTests_lambda_lambda_9_invoke_k_
    return this
end function

sub collectionExtensionsTests_lambda_lambda_9_invoke_k_()
    list = listOf_Arr_k_([1, 2, 3, 4])
    product = reduce_rIterable_Function2_k_(list, collectionExtensionsTests_lambda_lambda_lambda_8_create_k_())
    assertEquals_AnyN_AnyN_StrN_k_(24, product, invalid)
end sub

function collectionExtensionsTests_lambda_lambda_10_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_10"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_10", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = collectionExtensionsTests_lambda_lambda_10_invoke_k_
    return this
end function

sub collectionExtensionsTests_lambda_lambda_10_invoke_k_()
    list = listOf_Arr_k_([1, 2, 3, 4, 5])
    assertEquals_AnyN_AnyN_StrN_k_(15, sum_rIterableI_k_(list), invalid)
end sub

function collectionExtensionsTests_lambda_lambda_lambda_9_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_lambda_9"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_lambda_9", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = collectionExtensionsTests_lambda_lambda_lambda_9_invoke_AnyN_k_
    return this
end function

function collectionExtensionsTests_lambda_lambda_lambda_9_invoke_AnyN_k_(it as String) as Integer
    return Len(it)
end function

function collectionExtensionsTests_lambda_lambda_11_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_11"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_11", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = collectionExtensionsTests_lambda_lambda_11_invoke_k_
    return this
end function

sub collectionExtensionsTests_lambda_lambda_11_invoke_k_()
    list = listOf_Arr_k_(["a", "bb", "ccc"])
    assertEquals_AnyN_AnyN_StrN_k_(6, sumOf_rIterable_Function1I_k_(list, collectionExtensionsTests_lambda_lambda_lambda_9_create_k_()), invalid)
end sub

function collectionExtensionsTests_lambda_lambda_lambda_10_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_lambda_10"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_lambda_10", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = collectionExtensionsTests_lambda_lambda_lambda_10_invoke_AnyN_k_
    return this
end function

function collectionExtensionsTests_lambda_lambda_lambda_10_invoke_AnyN_k_(it as Integer) as Boolean
    return it > 3
end function

function collectionExtensionsTests_lambda_lambda_12_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_12"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_12", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = collectionExtensionsTests_lambda_lambda_12_invoke_k_
    return this
end function

sub collectionExtensionsTests_lambda_lambda_12_invoke_k_()
    list = listOf_Arr_k_([1, 2, 3, 4, 5])
    assertEquals_AnyN_AnyN_StrN_k_(5, count_rIterable_k_(list), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(2, count_rIterable_Function1Z_k_(list, collectionExtensionsTests_lambda_lambda_lambda_10_create_k_()), invalid)
end sub

function collectionExtensionsTests_lambda_lambda_lambda_11_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_lambda_11"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_lambda_11", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = collectionExtensionsTests_lambda_lambda_lambda_11_invoke_AnyN_k_
    return this
end function

function collectionExtensionsTests_lambda_lambda_lambda_11_invoke_AnyN_k_(it as Integer) as Boolean
    return it > 2
end function

function collectionExtensionsTests_lambda_lambda_lambda_12_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_lambda_12"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_lambda_12", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = collectionExtensionsTests_lambda_lambda_lambda_12_invoke_AnyN_k_
    return this
end function

function collectionExtensionsTests_lambda_lambda_lambda_12_invoke_AnyN_k_(it as Integer) as Boolean
    return it > 10
end function

function collectionExtensionsTests_lambda_lambda_13_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_13"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_13", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = collectionExtensionsTests_lambda_lambda_13_invoke_k_
    return this
end function

sub collectionExtensionsTests_lambda_lambda_13_invoke_k_()
    list = listOf_Arr_k_([1, 2, 3])
    assertTrue_Z_StrN_k_(any_rIterable_k_(list), invalid)
    assertTrue_Z_StrN_k_(any_rIterable_Function1Z_k_(list, collectionExtensionsTests_lambda_lambda_lambda_11_create_k_()), invalid)
    assertFalse_Z_StrN_k_(any_rIterable_Function1Z_k_(list, collectionExtensionsTests_lambda_lambda_lambda_12_create_k_()), invalid)
end sub

function collectionExtensionsTests_lambda_lambda_lambda_13_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_lambda_13"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_lambda_13", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = collectionExtensionsTests_lambda_lambda_lambda_13_invoke_AnyN_k_
    return this
end function

function collectionExtensionsTests_lambda_lambda_lambda_13_invoke_AnyN_k_(it as Integer) as Boolean
    return (it mod 2) = 0
end function

function collectionExtensionsTests_lambda_lambda_lambda_14_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_lambda_14"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_lambda_14", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = collectionExtensionsTests_lambda_lambda_lambda_14_invoke_AnyN_k_
    return this
end function

function collectionExtensionsTests_lambda_lambda_lambda_14_invoke_AnyN_k_(it as Integer) as Boolean
    return it > 3
end function

function collectionExtensionsTests_lambda_lambda_14_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_14"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_14", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = collectionExtensionsTests_lambda_lambda_14_invoke_k_
    return this
end function

sub collectionExtensionsTests_lambda_lambda_14_invoke_k_()
    list = listOf_Arr_k_([2, 4, 6])
    assertTrue_Z_StrN_k_(all_rIterable_Function1Z_k_(list, collectionExtensionsTests_lambda_lambda_lambda_13_create_k_()), invalid)
    assertFalse_Z_StrN_k_(all_rIterable_Function1Z_k_(list, collectionExtensionsTests_lambda_lambda_lambda_14_create_k_()), invalid)
end sub

function collectionExtensionsTests_lambda_lambda_lambda_15_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_lambda_15"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_lambda_15", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = collectionExtensionsTests_lambda_lambda_lambda_15_invoke_AnyN_k_
    return this
end function

function collectionExtensionsTests_lambda_lambda_lambda_15_invoke_AnyN_k_(it as Integer) as Boolean
    return it > 10
end function

function collectionExtensionsTests_lambda_lambda_15_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_15"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_15", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = collectionExtensionsTests_lambda_lambda_15_invoke_k_
    return this
end function

sub collectionExtensionsTests_lambda_lambda_15_invoke_k_()
    list = listOf_Arr_k_([1, 2, 3])
    assertFalse_Z_StrN_k_(none_rIterable_k_(list), invalid)
    assertTrue_Z_StrN_k_(none_rIterable_Function1Z_k_(list, collectionExtensionsTests_lambda_lambda_lambda_15_create_k_()), invalid)
end sub

function collectionExtensionsTests_lambda_lambda_lambda_16_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_lambda_16"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_lambda_16", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = collectionExtensionsTests_lambda_lambda_lambda_16_invoke_AnyN_k_
    return this
end function

function collectionExtensionsTests_lambda_lambda_lambda_16_invoke_AnyN_k_(it as Integer) as Boolean
    return it > 1
end function

function collectionExtensionsTests_lambda_lambda_16_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_16"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_16", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = collectionExtensionsTests_lambda_lambda_16_invoke_k_
    return this
end function

sub collectionExtensionsTests_lambda_lambda_16_invoke_k_()
    list = listOf_Arr_k_([1, 2, 3])
    assertEquals_AnyN_AnyN_StrN_k_(1, first_rIterable_k_(list), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(3, last_rIterable_k_(list), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(2, first_rIterable_Function1Z_k_(list, collectionExtensionsTests_lambda_lambda_lambda_16_create_k_()), invalid)
end sub

function collectionExtensionsTests_lambda_lambda_lambda_17_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_lambda_17"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_lambda_17", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = collectionExtensionsTests_lambda_lambda_lambda_17_invoke_AnyN_k_
    return this
end function

function collectionExtensionsTests_lambda_lambda_lambda_17_invoke_AnyN_k_(it as Integer) as Boolean
    return it > 10
end function

function collectionExtensionsTests_lambda_lambda_17_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_17"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_17", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = collectionExtensionsTests_lambda_lambda_17_invoke_k_
    return this
end function

sub collectionExtensionsTests_lambda_lambda_17_invoke_k_()
    list = listOf_Arr_k_([1, 2, 3])
    assertEquals_AnyN_AnyN_StrN_k_(1, firstOrNull_rIterable_k_(list), invalid)
    assertNull_AnyN_StrN_k_(firstOrNull_rIterable_Function1Z_k_(list, collectionExtensionsTests_lambda_lambda_lambda_17_create_k_()), invalid)
    empty = emptyList_k_()
    assertNull_AnyN_StrN_k_(firstOrNull_rIterable_k_(empty), invalid)
end sub

function collectionExtensionsTests_lambda_lambda_lambda_18_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_lambda_18"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_lambda_18", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = collectionExtensionsTests_lambda_lambda_lambda_18_invoke_AnyN_k_
    return this
end function

function collectionExtensionsTests_lambda_lambda_lambda_18_invoke_AnyN_k_(it as Integer) as Boolean
    return it > 2
end function

function collectionExtensionsTests_lambda_lambda_lambda_19_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_lambda_19"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_lambda_19", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = collectionExtensionsTests_lambda_lambda_lambda_19_invoke_AnyN_k_
    return this
end function

function collectionExtensionsTests_lambda_lambda_lambda_19_invoke_AnyN_k_(it as Integer) as Boolean
    return it > 10
end function

function collectionExtensionsTests_lambda_lambda_18_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_18"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_18", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = collectionExtensionsTests_lambda_lambda_18_invoke_k_
    return this
end function

sub collectionExtensionsTests_lambda_lambda_18_invoke_k_()
    list = listOf_Arr_k_([1, 2, 3, 4])
    assertEquals_AnyN_AnyN_StrN_k_(3, find_rIterable_Function1Z_k_(list, collectionExtensionsTests_lambda_lambda_lambda_18_create_k_()), invalid)
    assertNull_AnyN_StrN_k_(find_rIterable_Function1Z_k_(list, collectionExtensionsTests_lambda_lambda_lambda_19_create_k_()), invalid)
end sub

function collectionExtensionsTests_lambda_lambda_lambda_20_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_lambda_20"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_lambda_20", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = collectionExtensionsTests_lambda_lambda_lambda_20_invoke_AnyN_k_
    return this
end function

function collectionExtensionsTests_lambda_lambda_lambda_20_invoke_AnyN_k_(it as Integer) as Integer
    return it mod 2
end function

function collectionExtensionsTests_lambda_lambda_19_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_19"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_19", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = collectionExtensionsTests_lambda_lambda_19_invoke_k_
    return this
end function

sub collectionExtensionsTests_lambda_lambda_19_invoke_k_()
    list = listOf_Arr_k_([1, 2, 3, 4, 5, 6])
    groups = groupBy_rIterable_Function1_k_(list, collectionExtensionsTests_lambda_lambda_lambda_20_create_k_())
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([2, 4, 6]), groups.get_AnyN_k_(0), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([1, 3, 5]), groups.get_AnyN_k_(1), invalid)
end sub

function collectionExtensionsTests_lambda_lambda_lambda_21_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_lambda_21"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_lambda_21", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = collectionExtensionsTests_lambda_lambda_lambda_21_invoke_AnyN_k_
    return this
end function

function collectionExtensionsTests_lambda_lambda_lambda_21_invoke_AnyN_k_(it as Integer) as Boolean
    return (it mod 2) = 0
end function

function collectionExtensionsTests_lambda_lambda_20_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_20"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_20", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = collectionExtensionsTests_lambda_lambda_20_invoke_k_
    return this
end function

sub collectionExtensionsTests_lambda_lambda_20_invoke_k_()
    list = listOf_Arr_k_([1, 2, 3, 4, 5])
    __destruct_0 = partition_rIterable_Function1Z_k_(list, collectionExtensionsTests_lambda_lambda_lambda_21_create_k_())
    evens = __destruct_0.component1()
    odds = __destruct_0.component2()
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([2, 4]), evens, invalid)
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([1, 3, 5]), odds, invalid)
end sub

function collectionExtensionsTests_lambda_lambda_lambda_22_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_lambda_22"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_lambda_22", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = collectionExtensionsTests_lambda_lambda_lambda_22_invoke_AnyN_k_
    return this
end function

function collectionExtensionsTests_lambda_lambda_lambda_22_invoke_AnyN_k_(it as String) as Object
    return to_rAnyN_AnyN_k_(it, Len(it))
end function

function collectionExtensionsTests_lambda_lambda_21_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_21"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_21", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = collectionExtensionsTests_lambda_lambda_21_invoke_k_
    return this
end function

sub collectionExtensionsTests_lambda_lambda_21_invoke_k_()
    list = listOf_Arr_k_(["a", "bb", "ccc"])
    map = associate_rIterable_Function1Pair_k_(list, collectionExtensionsTests_lambda_lambda_lambda_22_create_k_())
    assertEquals_AnyN_AnyN_StrN_k_(1, map.get_AnyN_k_("a"), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(2, map.get_AnyN_k_("bb"), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(3, map.get_AnyN_k_("ccc"), invalid)
end sub

function collectionExtensionsTests_lambda_lambda_lambda_23_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_lambda_23"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_lambda_23", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = collectionExtensionsTests_lambda_lambda_lambda_23_invoke_AnyN_k_
    return this
end function

function collectionExtensionsTests_lambda_lambda_lambda_23_invoke_AnyN_k_(it as String) as Integer
    return Len(it)
end function

function collectionExtensionsTests_lambda_lambda_22_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_22"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_22", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = collectionExtensionsTests_lambda_lambda_22_invoke_k_
    return this
end function

sub collectionExtensionsTests_lambda_lambda_22_invoke_k_()
    list = listOf_Arr_k_(["a", "bb", "ccc"])
    map = associateWith_rIterable_Function1_k_(list, collectionExtensionsTests_lambda_lambda_lambda_23_create_k_())
    assertEquals_AnyN_AnyN_StrN_k_(1, map.get_AnyN_k_("a"), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(2, map.get_AnyN_k_("bb"), invalid)
end sub

function collectionExtensionsTests_lambda_lambda_lambda_24_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_lambda_24"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_lambda_24", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = collectionExtensionsTests_lambda_lambda_lambda_24_invoke_AnyN_k_
    return this
end function

function collectionExtensionsTests_lambda_lambda_lambda_24_invoke_AnyN_k_(it as String) as Integer
    return Len(it)
end function

function collectionExtensionsTests_lambda_lambda_23_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_23"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_23", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = collectionExtensionsTests_lambda_lambda_23_invoke_k_
    return this
end function

sub collectionExtensionsTests_lambda_lambda_23_invoke_k_()
    list = listOf_Arr_k_(["a", "bb", "ccc"])
    map = associateBy_rIterable_Function1_k_(list, collectionExtensionsTests_lambda_lambda_lambda_24_create_k_())
    assertEquals_AnyN_AnyN_StrN_k_("a", map.get_AnyN_k_(1), invalid)
    assertEquals_AnyN_AnyN_StrN_k_("bb", map.get_AnyN_k_(2), invalid)
end sub

function collectionExtensionsTests_lambda_lambda_24_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_24"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_24", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = collectionExtensionsTests_lambda_lambda_24_invoke_k_
    return this
end function

sub collectionExtensionsTests_lambda_lambda_24_invoke_k_()
    set = hashSetOf_Arr_k_([1, 2, 3])
    list = toList_rIterable_k_(set)
    assertEquals_AnyN_AnyN_StrN_k_(3, list.__get_size(), invalid)
end sub

function collectionExtensionsTests_lambda_lambda_25_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_25"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_25", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = collectionExtensionsTests_lambda_lambda_25_invoke_k_
    return this
end function

sub collectionExtensionsTests_lambda_lambda_25_invoke_k_()
    list = listOf_Arr_k_([1, 2, 2, 3, 3, 3])
    set = toSet_rIterable_k_(list)
    assertEquals_AnyN_AnyN_StrN_k_(3, set.__get_size(), invalid)
end sub

function collectionExtensionsTests_lambda_lambda_26_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_26"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_26", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = collectionExtensionsTests_lambda_lambda_26_invoke_k_
    return this
end function

sub collectionExtensionsTests_lambda_lambda_26_invoke_k_()
    list = listOf_Arr_k_([1, 2, 3])
    assertEquals_AnyN_AnyN_StrN_k_("1, 2, 3", joinToString_rIterable_CharSequence_CharSequence_CharSequence_I_CharSequence_k_(list, invalid, invalid, invalid, invalid, invalid), invalid)
    assertEquals_AnyN_AnyN_StrN_k_("1-2-3", joinToString_rIterable_CharSequence_CharSequence_CharSequence_I_CharSequence_k_(list, "-", invalid, invalid, invalid, invalid), invalid)
    assertEquals_AnyN_AnyN_StrN_k_("[1, 2, 3]", joinToString_rIterable_CharSequence_CharSequence_CharSequence_I_CharSequence_k_(list, invalid, "[", "]", invalid, invalid), invalid)
end sub

function collectionExtensionsTests_lambda_lambda_lambda_25_create_MutableListI_k_(_collected as Object) as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_lambda_25"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_lambda_25", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = collectionExtensionsTests_lambda_lambda_lambda_25_invoke_AnyN_k_
    this._collected = _collected
    return this
end function

sub collectionExtensionsTests_lambda_lambda_lambda_25_invoke_AnyN_k_(it as Integer)
    m._collected.add_AnyN_k_(it)
end sub

function collectionExtensionsTests_lambda_lambda_27_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_27"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_27", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = collectionExtensionsTests_lambda_lambda_27_invoke_k_
    return this
end function

sub collectionExtensionsTests_lambda_lambda_27_invoke_k_()
    list = listOf_Arr_k_([1, 2, 3])
    collected = mutableListOf_k_()
    forEach_rIterable_Function1V_k_(list, collectionExtensionsTests_lambda_lambda_lambda_25_create_MutableListI_k_(collected))
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([1, 2, 3]), collected, invalid)
end sub

function collectionExtensionsTests_lambda_lambda_lambda_26_create_MutableListStr_k_(_collected as Object) as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_lambda_26"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_lambda_26", "Function2", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_AnyN_k_ = collectionExtensionsTests_lambda_lambda_lambda_26_invoke_AnyN_AnyN_k_
    this._collected = _collected
    return this
end function

sub collectionExtensionsTests_lambda_lambda_lambda_26_invoke_AnyN_AnyN_k_(i as Integer, v as String)
    m._collected.add_AnyN_k_((__kotlin_numToStr_I_k_(i) + ":") + v)
end sub

function collectionExtensionsTests_lambda_lambda_28_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_28"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_28", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = collectionExtensionsTests_lambda_lambda_28_invoke_k_
    return this
end function

sub collectionExtensionsTests_lambda_lambda_28_invoke_k_()
    list = listOf_Arr_k_(["a", "b", "c"])
    collected = mutableListOf_k_()
    forEachIndexed_rIterable_Function2IV_k_(list, collectionExtensionsTests_lambda_lambda_lambda_26_create_MutableListStr_k_(collected))
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_(["0:a", "1:b", "2:c"]), collected, invalid)
end sub

function collectionExtensionsTests_lambda_lambda_29_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_29"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_29", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = collectionExtensionsTests_lambda_lambda_29_invoke_k_
    return this
end function

sub collectionExtensionsTests_lambda_lambda_29_invoke_k_()
    list = listOf_Arr_k_([1, 2, 3, 2])
    assertEquals_AnyN_AnyN_StrN_k_(1, list.indexOf_AnyN_k_(2), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(-1, list.indexOf_AnyN_k_(5), invalid)
end sub

function collectionExtensionsTests_lambda_lambda_lambda_27_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_lambda_27"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_lambda_27", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = collectionExtensionsTests_lambda_lambda_lambda_27_invoke_AnyN_k_
    return this
end function

function collectionExtensionsTests_lambda_lambda_lambda_27_invoke_AnyN_k_(it as Integer) as Boolean
    return it > 2
end function

function collectionExtensionsTests_lambda_lambda_30_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_30"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_30", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = collectionExtensionsTests_lambda_lambda_30_invoke_k_
    return this
end function

sub collectionExtensionsTests_lambda_lambda_30_invoke_k_()
    list = listOf_Arr_k_([1, 2, 3, 4])
    assertEquals_AnyN_AnyN_StrN_k_(2, indexOfFirst_rIterable_Function1Z_k_(list, collectionExtensionsTests_lambda_lambda_lambda_27_create_k_()), invalid)
end sub

function collectionExtensionsTests_lambda_lambda_lambda_28_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_lambda_28"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_lambda_28", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = collectionExtensionsTests_lambda_lambda_lambda_28_invoke_AnyN_k_
    return this
end function

function collectionExtensionsTests_lambda_lambda_lambda_28_invoke_AnyN_k_(it as Integer) as Boolean
    return it > 2
end function

function collectionExtensionsTests_lambda_lambda_31_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_31"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_31", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = collectionExtensionsTests_lambda_lambda_31_invoke_k_
    return this
end function

sub collectionExtensionsTests_lambda_lambda_31_invoke_k_()
    list = listOf_Arr_k_([1, 2, 3, 4])
    assertEquals_AnyN_AnyN_StrN_k_(3, indexOfLast_rIterable_Function1Z_k_(list, collectionExtensionsTests_lambda_lambda_lambda_28_create_k_()), invalid)
end sub

function collectionExtensionsTests_lambda_lambda_32_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_32"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_32", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = collectionExtensionsTests_lambda_lambda_32_invoke_k_
    return this
end function

sub collectionExtensionsTests_lambda_lambda_32_invoke_k_()
    list = listOf_Arr_k_([1, 2, 2, 3, 3, 3])
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([1, 2, 3]), distinct_rIterable_k_(list), invalid)
end sub

function collectionExtensionsTests_lambda_lambda_lambda_29_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_lambda_29"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_lambda_29", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = collectionExtensionsTests_lambda_lambda_lambda_29_invoke_AnyN_k_
    return this
end function

function collectionExtensionsTests_lambda_lambda_lambda_29_invoke_AnyN_k_(it as String) as Integer
    return Len(it)
end function

function collectionExtensionsTests_lambda_lambda_33_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_33"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_33", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = collectionExtensionsTests_lambda_lambda_33_invoke_k_
    return this
end function

sub collectionExtensionsTests_lambda_lambda_33_invoke_k_()
    list = listOf_Arr_k_(["a", "bb", "c", "dd"])
    result = distinctBy_rIterable_Function1_k_(list, collectionExtensionsTests_lambda_lambda_lambda_29_create_k_())
    assertEquals_AnyN_AnyN_StrN_k_(2, result.__get_size(), invalid)
end sub

function collectionExtensionsTests_lambda_lambda_34_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_34"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_34", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = collectionExtensionsTests_lambda_lambda_34_invoke_k_
    return this
end function

sub collectionExtensionsTests_lambda_lambda_34_invoke_k_()
    list = listOf_Arr_k_([1, 2, 3, 4, 5])
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([1, 2, 3]), take_rIterable_I_k_(list, 3), invalid)
end sub

function collectionExtensionsTests_lambda_lambda_35_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_35"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_35", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = collectionExtensionsTests_lambda_lambda_35_invoke_k_
    return this
end function

sub collectionExtensionsTests_lambda_lambda_35_invoke_k_()
    list = listOf_Arr_k_([1, 2, 3, 4, 5])
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([3, 4, 5]), drop_rIterable_I_k_(list, 2), invalid)
end sub

function collectionExtensionsTests_lambda_lambda_lambda_30_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_lambda_30"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_lambda_30", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = collectionExtensionsTests_lambda_lambda_lambda_30_invoke_AnyN_k_
    return this
end function

function collectionExtensionsTests_lambda_lambda_lambda_30_invoke_AnyN_k_(it as Integer) as Boolean
    return it < 3
end function

function collectionExtensionsTests_lambda_lambda_36_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_36"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_36", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = collectionExtensionsTests_lambda_lambda_36_invoke_k_
    return this
end function

sub collectionExtensionsTests_lambda_lambda_36_invoke_k_()
    list = listOf_Arr_k_([1, 2, 3, 4, 5])
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([1, 2]), takeWhile_rIterable_Function1Z_k_(list, collectionExtensionsTests_lambda_lambda_lambda_30_create_k_()), invalid)
end sub

function collectionExtensionsTests_lambda_lambda_lambda_31_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_lambda_31"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_lambda_31", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = collectionExtensionsTests_lambda_lambda_lambda_31_invoke_AnyN_k_
    return this
end function

function collectionExtensionsTests_lambda_lambda_lambda_31_invoke_AnyN_k_(it as Integer) as Boolean
    return it < 3
end function

function collectionExtensionsTests_lambda_lambda_37_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_37"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_37", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = collectionExtensionsTests_lambda_lambda_37_invoke_k_
    return this
end function

sub collectionExtensionsTests_lambda_lambda_37_invoke_k_()
    list = listOf_Arr_k_([1, 2, 3, 4, 5])
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([3, 4, 5]), dropWhile_rIterable_Function1Z_k_(list, collectionExtensionsTests_lambda_lambda_lambda_31_create_k_()), invalid)
end sub

function collectionExtensionsTests_lambda_lambda_38_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_38"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_38", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = collectionExtensionsTests_lambda_lambda_38_invoke_k_
    return this
end function

sub collectionExtensionsTests_lambda_lambda_38_invoke_k_()
    list = listOf_Arr_k_([1, 2, 3])
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([3, 2, 1]), reversed_rIterable_k_(list), invalid)
end sub

function collectionExtensionsTests_lambda_lambda_39_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda_lambda_39"
    this.__proto = ["collectionExtensionsTests_lambda_lambda_39", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = collectionExtensionsTests_lambda_lambda_39_invoke_k_
    return this
end function

sub collectionExtensionsTests_lambda_lambda_39_invoke_k_()
    a = listOf_Arr_k_([1, 2, 3])
    b = listOf_Arr_k_(["a", "b", "c"])
    zipped = zip_rIterable_Iterable_k_(a, b)
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([to_rAnyN_AnyN_k_(1, "a"), to_rAnyN_AnyN_k_(2, "b"), to_rAnyN_AnyN_k_(3, "c")]), zipped, invalid)
end sub

function collectionExtensionsTests_lambda_create_k_() as Object
    this = {}
    this.__type = "collectionExtensionsTests_lambda"
    this.__proto = ["collectionExtensionsTests_lambda", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = collectionExtensionsTests_lambda_invoke_AnyN_k_
    return this
end function

sub collectionExtensionsTests_lambda_invoke_AnyN_k_(_this_suite as Object)
    _this_suite.test_Str_Function0V_k_("map", collectionExtensionsTests_lambda_lambda_create_k_())
    _this_suite.test_Str_Function0V_k_("mapNotNull", collectionExtensionsTests_lambda_lambda_1_create_k_())
    _this_suite.test_Str_Function0V_k_("mapIndexed", collectionExtensionsTests_lambda_lambda_2_create_k_())
    _this_suite.test_Str_Function0V_k_("flatMap", collectionExtensionsTests_lambda_lambda_3_create_k_())
    _this_suite.test_Str_Function0V_k_("filter", collectionExtensionsTests_lambda_lambda_4_create_k_())
    _this_suite.test_Str_Function0V_k_("filterNot", collectionExtensionsTests_lambda_lambda_5_create_k_())
    _this_suite.test_Str_Function0V_k_("filterIndexed", collectionExtensionsTests_lambda_lambda_6_create_k_())
    _this_suite.test_Str_Function0V_k_("filterNotNull", collectionExtensionsTests_lambda_lambda_7_create_k_())
    _this_suite.test_Str_Function0V_k_("fold", collectionExtensionsTests_lambda_lambda_8_create_k_())
    _this_suite.test_Str_Function0V_k_("reduce", collectionExtensionsTests_lambda_lambda_9_create_k_())
    _this_suite.test_Str_Function0V_k_("sum", collectionExtensionsTests_lambda_lambda_10_create_k_())
    _this_suite.test_Str_Function0V_k_("sumOf", collectionExtensionsTests_lambda_lambda_11_create_k_())
    _this_suite.test_Str_Function0V_k_("count", collectionExtensionsTests_lambda_lambda_12_create_k_())
    _this_suite.test_Str_Function0V_k_("any", collectionExtensionsTests_lambda_lambda_13_create_k_())
    _this_suite.test_Str_Function0V_k_("all", collectionExtensionsTests_lambda_lambda_14_create_k_())
    _this_suite.test_Str_Function0V_k_("none", collectionExtensionsTests_lambda_lambda_15_create_k_())
    _this_suite.test_Str_Function0V_k_("first and last", collectionExtensionsTests_lambda_lambda_16_create_k_())
    _this_suite.test_Str_Function0V_k_("firstOrNull and lastOrNull", collectionExtensionsTests_lambda_lambda_17_create_k_())
    _this_suite.test_Str_Function0V_k_("find", collectionExtensionsTests_lambda_lambda_18_create_k_())
    _this_suite.test_Str_Function0V_k_("groupBy", collectionExtensionsTests_lambda_lambda_19_create_k_())
    _this_suite.test_Str_Function0V_k_("partition", collectionExtensionsTests_lambda_lambda_20_create_k_())
    _this_suite.test_Str_Function0V_k_("associate", collectionExtensionsTests_lambda_lambda_21_create_k_())
    _this_suite.test_Str_Function0V_k_("associateWith", collectionExtensionsTests_lambda_lambda_22_create_k_())
    _this_suite.test_Str_Function0V_k_("associateBy", collectionExtensionsTests_lambda_lambda_23_create_k_())
    _this_suite.test_Str_Function0V_k_("toList", collectionExtensionsTests_lambda_lambda_24_create_k_())
    _this_suite.test_Str_Function0V_k_("toSet", collectionExtensionsTests_lambda_lambda_25_create_k_())
    _this_suite.test_Str_Function0V_k_("joinToString", collectionExtensionsTests_lambda_lambda_26_create_k_())
    _this_suite.test_Str_Function0V_k_("forEach", collectionExtensionsTests_lambda_lambda_27_create_k_())
    _this_suite.test_Str_Function0V_k_("forEachIndexed", collectionExtensionsTests_lambda_lambda_28_create_k_())
    _this_suite.test_Str_Function0V_k_("indexOf", collectionExtensionsTests_lambda_lambda_29_create_k_())
    _this_suite.test_Str_Function0V_k_("indexOfFirst", collectionExtensionsTests_lambda_lambda_30_create_k_())
    _this_suite.test_Str_Function0V_k_("indexOfLast", collectionExtensionsTests_lambda_lambda_31_create_k_())
    _this_suite.test_Str_Function0V_k_("distinct", collectionExtensionsTests_lambda_lambda_32_create_k_())
    _this_suite.test_Str_Function0V_k_("distinctBy", collectionExtensionsTests_lambda_lambda_33_create_k_())
    _this_suite.test_Str_Function0V_k_("take", collectionExtensionsTests_lambda_lambda_34_create_k_())
    _this_suite.test_Str_Function0V_k_("drop", collectionExtensionsTests_lambda_lambda_35_create_k_())
    _this_suite.test_Str_Function0V_k_("takeWhile", collectionExtensionsTests_lambda_lambda_36_create_k_())
    _this_suite.test_Str_Function0V_k_("dropWhile", collectionExtensionsTests_lambda_lambda_37_create_k_())
    _this_suite.test_Str_Function0V_k_("reversed", collectionExtensionsTests_lambda_lambda_38_create_k_())
    _this_suite.test_Str_Function0V_k_("zip", collectionExtensionsTests_lambda_lambda_39_create_k_())
end sub
