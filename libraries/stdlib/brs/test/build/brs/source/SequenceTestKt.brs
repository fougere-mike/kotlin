sub sequenceTests_rTestRunner_k_(m as Object)
    m.suite_Str_Function1TestRunnerV_k_("Sequence", sequenceTests_lambda_create_k_())
end sub

function sequenceTests_lambda_lambda_lambda_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_lambda"
    this.__proto = ["sequenceTests_lambda_lambda_lambda", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = sequenceTests_lambda_lambda_lambda_invoke_AnyN_k_
    return this
end function

function sequenceTests_lambda_lambda_lambda_invoke_AnyN_k_(it as Integer) as Integer
    return it * 2
end function

function sequenceTests_lambda_lambda_lambda_1_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_lambda_1"
    this.__proto = ["sequenceTests_lambda_lambda_lambda_1", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = sequenceTests_lambda_lambda_lambda_1_invoke_AnyN_k_
    return this
end function

function sequenceTests_lambda_lambda_lambda_1_invoke_AnyN_k_(it as Integer) as Boolean
    return it > 5
end function

function sequenceTests_lambda_lambda_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda"
    this.__proto = ["sequenceTests_lambda_lambda", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = sequenceTests_lambda_lambda_invoke_k_
    return this
end function

sub sequenceTests_lambda_lambda_invoke_k_()
    list = listOf_Arr_k_([1, 2, 3, 4, 5])
    result = toList_rSequence_k_(filter_rSequence_Function1Z_k_(map_rSequence_Function1_k_(asSequence_rIterable_k_(list), sequenceTests_lambda_lambda_lambda_create_k_()), sequenceTests_lambda_lambda_lambda_1_create_k_()))
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([6, 8, 10]), result, invalid)
end sub

function sequenceTests_lambda_lambda_lambda_2_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_lambda_2"
    this.__proto = ["sequenceTests_lambda_lambda_lambda_2", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = sequenceTests_lambda_lambda_lambda_2_invoke_AnyN_k_
    return this
end function

function sequenceTests_lambda_lambda_lambda_2_invoke_AnyN_k_(it as Object) as Boolean
    return it.compareTo_AnyN_k_(UInt_create_I_k_(2)) > 0
end function

function sequenceTests_lambda_lambda_lambda_3_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_lambda_3"
    this.__proto = ["sequenceTests_lambda_lambda_lambda_3", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = sequenceTests_lambda_lambda_lambda_3_invoke_AnyN_k_
    return this
end function

function sequenceTests_lambda_lambda_lambda_3_invoke_AnyN_k_(it as Object) as Object
    return it.times_UInt_k_(UInt_create_I_k_(2))
end function

function sequenceTests_lambda_lambda_1_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_1"
    this.__proto = ["sequenceTests_lambda_lambda_1", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = sequenceTests_lambda_lambda_1_invoke_k_
    return this
end function

sub sequenceTests_lambda_lambda_1_invoke_k_()
    list = listOf_Arr_k_([UInt_create_I_k_(1), UInt_create_I_k_(2), UInt_create_I_k_(3), UInt_create_I_k_(4), UInt_create_I_k_(5)])
    result = toList_rSequence_k_(map_rSequence_Function1_k_(filter_rSequence_Function1Z_k_(asSequence_rIterable_k_(list), sequenceTests_lambda_lambda_lambda_2_create_k_()), sequenceTests_lambda_lambda_lambda_3_create_k_()))
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([UInt_create_I_k_(6), UInt_create_I_k_(8), UInt_create_I_k_(10)]), result, invalid)
end sub

function sequenceTests_lambda_lambda_lambda_4_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_lambda_4"
    this.__proto = ["sequenceTests_lambda_lambda_lambda_4", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = sequenceTests_lambda_lambda_lambda_4_invoke_AnyN_k_
    return this
end function

function sequenceTests_lambda_lambda_lambda_4_invoke_AnyN_k_(it as Integer) as Dynamic
    return it + 1
end function

function sequenceTests_lambda_lambda_2_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_2"
    this.__proto = ["sequenceTests_lambda_lambda_2", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = sequenceTests_lambda_lambda_2_invoke_k_
    return this
end function

sub sequenceTests_lambda_lambda_2_invoke_k_()
    result = toList_rSequence_k_(take_rSequence_I_k_(generateSequence_AnyN_Function1_k_(1, sequenceTests_lambda_lambda_lambda_4_create_k_()), 5))
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([1, 2, 3, 4, 5]), result, invalid)
end sub

function sequenceTests_lambda_lambda_3_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_3"
    this.__proto = ["sequenceTests_lambda_lambda_3", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = sequenceTests_lambda_lambda_3_invoke_k_
    return this
end function

sub sequenceTests_lambda_lambda_3_invoke_k_()
    list = listOf_Arr_k_([1, 2, 3, 4, 5])
    result = toList_rSequence_k_(drop_rSequence_I_k_(asSequence_rIterable_k_(list), 2))
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([3, 4, 5]), result, invalid)
end sub

function sequenceTests_lambda_lambda_lambda_5_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_lambda_5"
    this.__proto = ["sequenceTests_lambda_lambda_lambda_5", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = sequenceTests_lambda_lambda_lambda_5_invoke_AnyN_k_
    return this
end function

function sequenceTests_lambda_lambda_lambda_5_invoke_AnyN_k_(n as Integer) as Object
    return toList_rIterable_k_(rangeTo_rI_I_k_(1, n))
end function

function sequenceTests_lambda_lambda_4_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_4"
    this.__proto = ["sequenceTests_lambda_lambda_4", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = sequenceTests_lambda_lambda_4_invoke_k_
    return this
end function

sub sequenceTests_lambda_lambda_4_invoke_k_()
    list = listOf_Arr_k_([1, 2, 3])
    result = toList_rSequence_k_(flatMap_rSequence_Function1Iterable_k_(asSequence_rIterable_k_(list), sequenceTests_lambda_lambda_lambda_5_create_k_()))
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([1, 1, 2, 1, 2, 3]), result, invalid)
end sub

function sequenceTests_lambda_lambda_5_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_5"
    this.__proto = ["sequenceTests_lambda_lambda_5", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = sequenceTests_lambda_lambda_5_invoke_k_
    return this
end function

sub sequenceTests_lambda_lambda_5_invoke_k_()
    list = listOf_Arr_k_([1, 2, 2, 3, 3, 3])
    result = toList_rSequence_k_(distinct_rSequence_k_(asSequence_rIterable_k_(list)))
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([1, 2, 3]), result, invalid)
end sub

function sequenceTests_lambda_lambda_6_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_6"
    this.__proto = ["sequenceTests_lambda_lambda_6", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = sequenceTests_lambda_lambda_6_invoke_k_
    return this
end function

sub sequenceTests_lambda_lambda_6_invoke_k_()
    list = listOf_Arr_k_([1, 2, 3, 4, 5])
    result = sum_rSequenceI_k_(asSequence_rIterable_k_(list))
    assertEquals_AnyN_AnyN_StrN_k_(15, result, invalid)
end sub

function sequenceTests_lambda_lambda_7_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_7"
    this.__proto = ["sequenceTests_lambda_lambda_7", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = sequenceTests_lambda_lambda_7_invoke_k_
    return this
end function

sub sequenceTests_lambda_lambda_7_invoke_k_()
    list = listOf_Arr_k_([5, 2, 4, 1, 3])
    result = toList_rSequence_k_(sorted_rSequence_k_(asSequence_rIterable_k_(list)))
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([1, 2, 3, 4, 5]), result, invalid)
end sub

function sequenceTests_lambda_lambda_lambda_6_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_lambda_6"
    this.__proto = ["sequenceTests_lambda_lambda_lambda_6", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = sequenceTests_lambda_lambda_lambda_6_invoke_AnyN_k_
    return this
end function

function sequenceTests_lambda_lambda_lambda_6_invoke_AnyN_k_(it as Integer) as Integer
    return it mod 2
end function

function sequenceTests_lambda_lambda_8_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_8"
    this.__proto = ["sequenceTests_lambda_lambda_8", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = sequenceTests_lambda_lambda_8_invoke_k_
    return this
end function

sub sequenceTests_lambda_lambda_8_invoke_k_()
    list = listOf_Arr_k_([1, 2, 3, 4, 5, 6])
    result = groupBy_rSequence_Function1_k_(asSequence_rIterable_k_(list), sequenceTests_lambda_lambda_lambda_6_create_k_())
    assertEquals_AnyN_AnyN_StrN_k_(2, result.__get_size(), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([2, 4, 6]), result.get_AnyN_k_(0), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([1, 3, 5]), result.get_AnyN_k_(1), invalid)
end sub

function sequenceTests_lambda_lambda_lambda_7_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_lambda_7"
    this.__proto = ["sequenceTests_lambda_lambda_lambda_7", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = sequenceTests_lambda_lambda_lambda_7_invoke_AnyN_k_
    return this
end function

function sequenceTests_lambda_lambda_lambda_7_invoke_AnyN_k_(it as Integer) as Boolean
    return it > 2
end function

function sequenceTests_lambda_lambda_9_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_9"
    this.__proto = ["sequenceTests_lambda_lambda_9", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = sequenceTests_lambda_lambda_9_invoke_k_
    return this
end function

sub sequenceTests_lambda_lambda_9_invoke_k_()
    list = listOf_Arr_k_([1, 2, 3, 4, 5])
    assertEquals_AnyN_AnyN_StrN_k_(5, count_rSequence_k_(asSequence_rIterable_k_(list)), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(3, count_rSequence_Function1Z_k_(asSequence_rIterable_k_(list), sequenceTests_lambda_lambda_lambda_7_create_k_()), invalid)
end sub

function sequenceTests_lambda_lambda_lambda_8_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_lambda_8"
    this.__proto = ["sequenceTests_lambda_lambda_lambda_8", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = sequenceTests_lambda_lambda_lambda_8_invoke_AnyN_k_
    return this
end function

function sequenceTests_lambda_lambda_lambda_8_invoke_AnyN_k_(it as Integer) as Boolean
    return (it mod 2) = 0
end function

function sequenceTests_lambda_lambda_lambda_9_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_lambda_9"
    this.__proto = ["sequenceTests_lambda_lambda_lambda_9", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = sequenceTests_lambda_lambda_lambda_9_invoke_AnyN_k_
    return this
end function

function sequenceTests_lambda_lambda_lambda_9_invoke_AnyN_k_(it as Integer) as Boolean
    return it > 5
end function

function sequenceTests_lambda_lambda_10_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_10"
    this.__proto = ["sequenceTests_lambda_lambda_10", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = sequenceTests_lambda_lambda_10_invoke_k_
    return this
end function

sub sequenceTests_lambda_lambda_10_invoke_k_()
    list = listOf_Arr_k_([2, 4, 6, 8])
    assertTrue_Z_StrN_k_(all_rSequence_Function1Z_k_(asSequence_rIterable_k_(list), sequenceTests_lambda_lambda_lambda_8_create_k_()), invalid)
    assertFalse_Z_StrN_k_(all_rSequence_Function1Z_k_(asSequence_rIterable_k_(list), sequenceTests_lambda_lambda_lambda_9_create_k_()), invalid)
end sub

function sequenceTests_lambda_lambda_lambda_10_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_lambda_10"
    this.__proto = ["sequenceTests_lambda_lambda_lambda_10", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = sequenceTests_lambda_lambda_lambda_10_invoke_AnyN_k_
    return this
end function

function sequenceTests_lambda_lambda_lambda_10_invoke_AnyN_k_(it as Integer) as Boolean
    return it > 5
end function

function sequenceTests_lambda_lambda_lambda_11_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_lambda_11"
    this.__proto = ["sequenceTests_lambda_lambda_lambda_11", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = sequenceTests_lambda_lambda_lambda_11_invoke_AnyN_k_
    return this
end function

function sequenceTests_lambda_lambda_lambda_11_invoke_AnyN_k_(it as Integer) as Boolean
    return it > 10
end function

function sequenceTests_lambda_lambda_11_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_11"
    this.__proto = ["sequenceTests_lambda_lambda_11", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = sequenceTests_lambda_lambda_11_invoke_k_
    return this
end function

sub sequenceTests_lambda_lambda_11_invoke_k_()
    list = listOf_Arr_k_([1, 3, 5, 7])
    assertTrue_Z_StrN_k_(any_rSequence_k_(asSequence_rIterable_k_(list)), invalid)
    assertTrue_Z_StrN_k_(any_rSequence_Function1Z_k_(asSequence_rIterable_k_(list), sequenceTests_lambda_lambda_lambda_10_create_k_()), invalid)
    assertFalse_Z_StrN_k_(any_rSequence_Function1Z_k_(asSequence_rIterable_k_(list), sequenceTests_lambda_lambda_lambda_11_create_k_()), invalid)
end sub

function sequenceTests_lambda_lambda_lambda_12_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_lambda_12"
    this.__proto = ["sequenceTests_lambda_lambda_lambda_12", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = sequenceTests_lambda_lambda_lambda_12_invoke_AnyN_k_
    return this
end function

function sequenceTests_lambda_lambda_lambda_12_invoke_AnyN_k_(it as Integer) as Boolean
    return (it mod 2) = 0
end function

function sequenceTests_lambda_lambda_lambda_13_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_lambda_13"
    this.__proto = ["sequenceTests_lambda_lambda_lambda_13", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = sequenceTests_lambda_lambda_lambda_13_invoke_AnyN_k_
    return this
end function

function sequenceTests_lambda_lambda_lambda_13_invoke_AnyN_k_(it as Integer) as Boolean
    return it > 5
end function

function sequenceTests_lambda_lambda_12_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_12"
    this.__proto = ["sequenceTests_lambda_lambda_12", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = sequenceTests_lambda_lambda_12_invoke_k_
    return this
end function

sub sequenceTests_lambda_lambda_12_invoke_k_()
    empty = emptyList_k_()
    assertTrue_Z_StrN_k_(none_rSequence_k_(asSequence_rIterable_k_(empty)), invalid)
    list = listOf_Arr_k_([1, 3, 5, 7])
    assertTrue_Z_StrN_k_(none_rSequence_Function1Z_k_(asSequence_rIterable_k_(list), sequenceTests_lambda_lambda_lambda_12_create_k_()), invalid)
    assertFalse_Z_StrN_k_(none_rSequence_Function1Z_k_(asSequence_rIterable_k_(list), sequenceTests_lambda_lambda_lambda_13_create_k_()), invalid)
end sub

function sequenceTests_lambda_lambda_lambda_14_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_lambda_14"
    this.__proto = ["sequenceTests_lambda_lambda_lambda_14", "Function2", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_AnyN_k_ = sequenceTests_lambda_lambda_lambda_14_invoke_AnyN_AnyN_k_
    return this
end function

function sequenceTests_lambda_lambda_lambda_14_invoke_AnyN_AnyN_k_(acc as Integer, value as Integer) as Integer
    return acc + value
end function

function sequenceTests_lambda_lambda_13_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_13"
    this.__proto = ["sequenceTests_lambda_lambda_13", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = sequenceTests_lambda_lambda_13_invoke_k_
    return this
end function

sub sequenceTests_lambda_lambda_13_invoke_k_()
    list = listOf_Arr_k_([1, 2, 3, 4, 5])
    result = fold_rSequence_AnyN_Function2_k_(asSequence_rIterable_k_(list), 0, sequenceTests_lambda_lambda_lambda_14_create_k_())
    assertEquals_AnyN_AnyN_StrN_k_(15, result, invalid)
end sub

function sequenceTests_lambda_lambda_lambda_15_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_lambda_15"
    this.__proto = ["sequenceTests_lambda_lambda_lambda_15", "Function2", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_AnyN_k_ = sequenceTests_lambda_lambda_lambda_15_invoke_AnyN_AnyN_k_
    return this
end function

function sequenceTests_lambda_lambda_lambda_15_invoke_AnyN_AnyN_k_(acc as Integer, value as Integer) as Integer
    return acc + value
end function

function sequenceTests_lambda_lambda_14_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_14"
    this.__proto = ["sequenceTests_lambda_lambda_14", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = sequenceTests_lambda_lambda_14_invoke_k_
    return this
end function

sub sequenceTests_lambda_lambda_14_invoke_k_()
    list = listOf_Arr_k_([1, 2, 3, 4, 5])
    result = reduce_rSequence_Function2_k_(asSequence_rIterable_k_(list), sequenceTests_lambda_lambda_lambda_15_create_k_())
    assertEquals_AnyN_AnyN_StrN_k_(15, result, invalid)
end sub

function sequenceTests_lambda_lambda_lambda_16_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_lambda_16"
    this.__proto = ["sequenceTests_lambda_lambda_lambda_16", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = sequenceTests_lambda_lambda_lambda_16_invoke_AnyN_k_
    return this
end function

function sequenceTests_lambda_lambda_lambda_16_invoke_AnyN_k_(it as Integer) as Boolean
    return it > 2
end function

function sequenceTests_lambda_lambda_15_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_15"
    this.__proto = ["sequenceTests_lambda_lambda_15", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = sequenceTests_lambda_lambda_15_invoke_k_
    return this
end function

sub sequenceTests_lambda_lambda_15_invoke_k_()
    list = listOf_Arr_k_([1, 2, 3, 4, 5])
    assertEquals_AnyN_AnyN_StrN_k_(1, first_rSequence_k_(asSequence_rIterable_k_(list)), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(3, first_rSequence_Function1Z_k_(asSequence_rIterable_k_(list), sequenceTests_lambda_lambda_lambda_16_create_k_()), invalid)
end sub

function sequenceTests_lambda_lambda_lambda_17_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_lambda_17"
    this.__proto = ["sequenceTests_lambda_lambda_lambda_17", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = sequenceTests_lambda_lambda_lambda_17_invoke_AnyN_k_
    return this
end function

function sequenceTests_lambda_lambda_lambda_17_invoke_AnyN_k_(it as Integer) as Boolean
    return it < 5
end function

function sequenceTests_lambda_lambda_16_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_16"
    this.__proto = ["sequenceTests_lambda_lambda_16", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = sequenceTests_lambda_lambda_16_invoke_k_
    return this
end function

sub sequenceTests_lambda_lambda_16_invoke_k_()
    list = listOf_Arr_k_([1, 2, 3, 4, 5])
    assertEquals_AnyN_AnyN_StrN_k_(5, last_rSequence_k_(asSequence_rIterable_k_(list)), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(4, last_rSequence_Function1Z_k_(asSequence_rIterable_k_(list), sequenceTests_lambda_lambda_lambda_17_create_k_()), invalid)
end sub

function sequenceTests_lambda_lambda_lambda_18_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_lambda_18"
    this.__proto = ["sequenceTests_lambda_lambda_lambda_18", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = sequenceTests_lambda_lambda_lambda_18_invoke_AnyN_k_
    return this
end function

function sequenceTests_lambda_lambda_lambda_18_invoke_AnyN_k_(it as Integer) as Boolean
    return it > 2
end function

function sequenceTests_lambda_lambda_lambda_19_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_lambda_19"
    this.__proto = ["sequenceTests_lambda_lambda_lambda_19", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = sequenceTests_lambda_lambda_lambda_19_invoke_AnyN_k_
    return this
end function

function sequenceTests_lambda_lambda_lambda_19_invoke_AnyN_k_(it as Integer) as Boolean
    return it > 10
end function

function sequenceTests_lambda_lambda_17_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_17"
    this.__proto = ["sequenceTests_lambda_lambda_17", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = sequenceTests_lambda_lambda_17_invoke_k_
    return this
end function

sub sequenceTests_lambda_lambda_17_invoke_k_()
    empty = emptyList_k_()
    assertNull_AnyN_StrN_k_(firstOrNull_rSequence_k_(asSequence_rIterable_k_(empty)), invalid)
    list = listOf_Arr_k_([1, 2, 3])
    assertEquals_AnyN_AnyN_StrN_k_(1, firstOrNull_rSequence_k_(asSequence_rIterable_k_(list)), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(3, firstOrNull_rSequence_Function1Z_k_(asSequence_rIterable_k_(list), sequenceTests_lambda_lambda_lambda_18_create_k_()), invalid)
    assertNull_AnyN_StrN_k_(firstOrNull_rSequence_Function1Z_k_(asSequence_rIterable_k_(list), sequenceTests_lambda_lambda_lambda_19_create_k_()), invalid)
end sub

function sequenceTests_lambda_lambda_lambda_20_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_lambda_20"
    this.__proto = ["sequenceTests_lambda_lambda_lambda_20", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = sequenceTests_lambda_lambda_lambda_20_invoke_AnyN_k_
    return this
end function

function sequenceTests_lambda_lambda_lambda_20_invoke_AnyN_k_(it as Integer) as Boolean
    return it = 2
end function

function sequenceTests_lambda_lambda_18_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_18"
    this.__proto = ["sequenceTests_lambda_lambda_18", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = sequenceTests_lambda_lambda_18_invoke_k_
    return this
end function

sub sequenceTests_lambda_lambda_18_invoke_k_()
    list = listOf_Arr_k_([42])
    assertEquals_AnyN_AnyN_StrN_k_(42, single_rSequence_k_(asSequence_rIterable_k_(list)), invalid)
    multiList = listOf_Arr_k_([1, 2, 3])
    assertEquals_AnyN_AnyN_StrN_k_(2, single_rSequence_Function1Z_k_(asSequence_rIterable_k_(multiList), sequenceTests_lambda_lambda_lambda_20_create_k_()), invalid)
end sub

function sequenceTests_lambda_lambda_19_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_19"
    this.__proto = ["sequenceTests_lambda_lambda_19", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = sequenceTests_lambda_lambda_19_invoke_k_
    return this
end function

sub sequenceTests_lambda_lambda_19_invoke_k_()
    list = listOf_Arr_k_([1, 2, 3, 4, 5])
    assertEquals_AnyN_AnyN_StrN_k_(1, elementAt_rSequence_I_k_(asSequence_rIterable_k_(list), 0), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(3, elementAt_rSequence_I_k_(asSequence_rIterable_k_(list), 2), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(5, elementAt_rSequence_I_k_(asSequence_rIterable_k_(list), 4), invalid)
end sub

function sequenceTests_lambda_lambda_20_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_20"
    this.__proto = ["sequenceTests_lambda_lambda_20", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = sequenceTests_lambda_lambda_20_invoke_k_
    return this
end function

sub sequenceTests_lambda_lambda_20_invoke_k_()
    list = listOf_Arr_k_([1, 2, 3])
    assertEquals_AnyN_AnyN_StrN_k_(2, elementAtOrNull_rSequence_I_k_(asSequence_rIterable_k_(list), 1), invalid)
    assertNull_AnyN_StrN_k_(elementAtOrNull_rSequence_I_k_(asSequence_rIterable_k_(list), 10), invalid)
    assertNull_AnyN_StrN_k_(elementAtOrNull_rSequence_I_k_(asSequence_rIterable_k_(list), -1), invalid)
end sub

function sequenceTests_lambda_lambda_21_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_21"
    this.__proto = ["sequenceTests_lambda_lambda_21", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = sequenceTests_lambda_lambda_21_invoke_k_
    return this
end function

sub sequenceTests_lambda_lambda_21_invoke_k_()
    list = listOf_Arr_k_([1, 2, 3, 2, 1])
    assertEquals_AnyN_AnyN_StrN_k_(0, indexOf_rSequence_AnyN_k_(asSequence_rIterable_k_(list), 1), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(1, indexOf_rSequence_AnyN_k_(asSequence_rIterable_k_(list), 2), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(-1, indexOf_rSequence_AnyN_k_(asSequence_rIterable_k_(list), 5), invalid)
end sub

function sequenceTests_lambda_lambda_22_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_22"
    this.__proto = ["sequenceTests_lambda_lambda_22", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = sequenceTests_lambda_lambda_22_invoke_k_
    return this
end function

sub sequenceTests_lambda_lambda_22_invoke_k_()
    list = listOf_Arr_k_([1, 2, 3, 4, 5])
    assertTrue_Z_StrN_k_(contains_rSequence_AnyN_k_(asSequence_rIterable_k_(list), 3), invalid)
    assertFalse_Z_StrN_k_(contains_rSequence_AnyN_k_(asSequence_rIterable_k_(list), 10), invalid)
end sub

function sequenceTests_lambda_lambda_23_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_23"
    this.__proto = ["sequenceTests_lambda_lambda_23", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = sequenceTests_lambda_lambda_23_invoke_k_
    return this
end function

sub sequenceTests_lambda_lambda_23_invoke_k_()
    list = listOf_Arr_k_([5, 2, 8, 1, 9, 3])
    assertEquals_AnyN_AnyN_StrN_k_(1, minOrNull_rSequence_k_(asSequence_rIterable_k_(list)), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(9, maxOrNull_rSequence_k_(asSequence_rIterable_k_(list)), invalid)
end sub

function sequenceTests_lambda_lambda_lambda_21_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_lambda_21"
    this.__proto = ["sequenceTests_lambda_lambda_lambda_21", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = sequenceTests_lambda_lambda_lambda_21_invoke_AnyN_k_
    return this
end function

function sequenceTests_lambda_lambda_lambda_21_invoke_AnyN_k_(it as Integer) as Boolean
    return (it mod 2) = 0
end function

function sequenceTests_lambda_lambda_24_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_24"
    this.__proto = ["sequenceTests_lambda_lambda_24", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = sequenceTests_lambda_lambda_24_invoke_k_
    return this
end function

sub sequenceTests_lambda_lambda_24_invoke_k_()
    list = listOf_Arr_k_([1, 2, 3, 4, 5, 6])
    __destruct_1 = partition_rSequence_Function1Z_k_(asSequence_rIterable_k_(list), sequenceTests_lambda_lambda_lambda_21_create_k_())
    evens = __destruct_1.component1()
    odds = __destruct_1.component2()
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([2, 4, 6]), evens, invalid)
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([1, 3, 5]), odds, invalid)
end sub

function sequenceTests_lambda_lambda_25_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_25"
    this.__proto = ["sequenceTests_lambda_lambda_25", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = sequenceTests_lambda_lambda_25_invoke_k_
    return this
end function

sub sequenceTests_lambda_lambda_25_invoke_k_()
    list = listOf_Arr_k_([1, 2, 3, 4, 5])
    assertEquals_AnyN_AnyN_StrN_k_("1, 2, 3, 4, 5", joinToString_rtpr9o_k_(asSequence_rIterable_k_(list), invalid, invalid, invalid, invalid, invalid, invalid), invalid)
    assertEquals_AnyN_AnyN_StrN_k_("1-2-3-4-5", joinToString_rtpr9o_k_(asSequence_rIterable_k_(list), "-", invalid, invalid, invalid, invalid, invalid), invalid)
    assertEquals_AnyN_AnyN_StrN_k_("[1, 2, 3, 4, 5]", joinToString_rtpr9o_k_(asSequence_rIterable_k_(list), invalid, "[", "]", invalid, invalid, invalid), invalid)
    assertEquals_AnyN_AnyN_StrN_k_("1, 2, ...", joinToString_rtpr9o_k_(asSequence_rIterable_k_(list), invalid, invalid, invalid, 2, invalid, invalid), invalid)
end sub

function sequenceTests_lambda_lambda_lambda_22_create_AnyN_k_(_sum as Dynamic) as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_lambda_22"
    this.__proto = ["sequenceTests_lambda_lambda_lambda_22", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = sequenceTests_lambda_lambda_lambda_22_invoke_AnyN_k_
    this._sum = _sum
    return this
end function

sub sequenceTests_lambda_lambda_lambda_22_invoke_AnyN_k_(it as Integer)
    m._sum.value = (m._sum.value + it)
end sub

function sequenceTests_lambda_lambda_26_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_26"
    this.__proto = ["sequenceTests_lambda_lambda_26", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = sequenceTests_lambda_lambda_26_invoke_k_
    return this
end function

sub sequenceTests_lambda_lambda_26_invoke_k_()
    list = listOf_Arr_k_([1, 2, 3])
    sum = {value: 0}
    result = toList_rSequence_k_(onEach_rSequence_Function1V_k_(asSequence_rIterable_k_(list), sequenceTests_lambda_lambda_lambda_22_create_AnyN_k_(sum)))
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([1, 2, 3]), result, invalid)
    assertEquals_AnyN_AnyN_StrN_k_(6, sum.value, invalid)
end sub

function sequenceTests_lambda_lambda_27_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_27"
    this.__proto = ["sequenceTests_lambda_lambda_27", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = sequenceTests_lambda_lambda_27_invoke_k_
    return this
end function

sub sequenceTests_lambda_lambda_27_invoke_k_()
    list = listOf_Arr_k_(["a", "b", "c"])
    result = toList_rSequence_k_(withIndex_rSequence_k_(asSequence_rIterable_k_(list)))
    assertEquals_AnyN_AnyN_StrN_k_(0, result.get_I_k_(0).index, invalid)
    assertEquals_AnyN_AnyN_StrN_k_("a", result.get_I_k_(0).value, invalid)
    assertEquals_AnyN_AnyN_StrN_k_(2, result.get_I_k_(2).index, invalid)
    assertEquals_AnyN_AnyN_StrN_k_("c", result.get_I_k_(2).value, invalid)
end sub

function sequenceTests_lambda_lambda_lambda_23_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_lambda_23"
    this.__proto = ["sequenceTests_lambda_lambda_lambda_23", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = sequenceTests_lambda_lambda_lambda_23_invoke_AnyN_k_
    return this
end function

function sequenceTests_lambda_lambda_lambda_23_invoke_AnyN_k_(it as Integer) as Dynamic
    __when_tmp0 = invalid
    if (it mod 2) = 0 then
        __when_tmp0 = (it * 2)
    else if true then
        __when_tmp0 = invalid
    end if
    return __when_tmp0

end function

function sequenceTests_lambda_lambda_28_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_28"
    this.__proto = ["sequenceTests_lambda_lambda_28", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = sequenceTests_lambda_lambda_28_invoke_k_
    return this
end function

sub sequenceTests_lambda_lambda_28_invoke_k_()
    list = listOf_Arr_k_([1, 2, 3, 4, 5])
    result = toList_rSequence_k_(mapNotNull_rSequence_Function1_k_(asSequence_rIterable_k_(list), sequenceTests_lambda_lambda_lambda_23_create_k_()))
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([4, 8]), result, invalid)
end sub

function sequenceTests_lambda_lambda_lambda_24_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_lambda_24"
    this.__proto = ["sequenceTests_lambda_lambda_lambda_24", "Function2", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_AnyN_k_ = sequenceTests_lambda_lambda_lambda_24_invoke_AnyN_AnyN_k_
    return this
end function

function sequenceTests_lambda_lambda_lambda_24_invoke_AnyN_AnyN_k_(acc as Integer, value as Integer) as Integer
    return acc + value
end function

function sequenceTests_lambda_lambda_29_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_29"
    this.__proto = ["sequenceTests_lambda_lambda_29", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = sequenceTests_lambda_lambda_29_invoke_k_
    return this
end function

sub sequenceTests_lambda_lambda_29_invoke_k_()
    list = listOf_Arr_k_([1, 2, 3, 4])
    result = toList_rSequence_k_(scan_rSequence_AnyN_Function2_k_(asSequence_rIterable_k_(list), 0, sequenceTests_lambda_lambda_lambda_24_create_k_()))
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([0, 1, 3, 6, 10]), result, invalid)
end sub

function sequenceTests_lambda_lambda_lambda_25_create_MutableListI_k_(_collected as Object) as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_lambda_25"
    this.__proto = ["sequenceTests_lambda_lambda_lambda_25", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = sequenceTests_lambda_lambda_lambda_25_invoke_AnyN_k_
    this._collected = _collected
    return this
end function

sub sequenceTests_lambda_lambda_lambda_25_invoke_AnyN_k_(it as Integer)
    m._collected.add_AnyN_k_(it * 2)
end sub

function sequenceTests_lambda_lambda_30_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_30"
    this.__proto = ["sequenceTests_lambda_lambda_30", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = sequenceTests_lambda_lambda_30_invoke_k_
    return this
end function

sub sequenceTests_lambda_lambda_30_invoke_k_()
    list = listOf_Arr_k_([1, 2, 3])
    collected = mutableListOf_k_()
    forEach_rSequence_Function1V_k_(asSequence_rIterable_k_(list), sequenceTests_lambda_lambda_lambda_25_create_MutableListI_k_(collected))
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([2, 4, 6]), collected, invalid)
end sub

function sequenceTests_lambda_lambda_lambda_26_create_MutableListStr_k_(_collected as Object) as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_lambda_26"
    this.__proto = ["sequenceTests_lambda_lambda_lambda_26", "Function2", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_AnyN_k_ = sequenceTests_lambda_lambda_lambda_26_invoke_AnyN_AnyN_k_
    this._collected = _collected
    return this
end function

sub sequenceTests_lambda_lambda_lambda_26_invoke_AnyN_AnyN_k_(index as Integer, value as String)
    m._collected.add_AnyN_k_((__kotlin_numToStr_I_k_(index) + ":") + value)
end sub

function sequenceTests_lambda_lambda_31_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_31"
    this.__proto = ["sequenceTests_lambda_lambda_31", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = sequenceTests_lambda_lambda_31_invoke_k_
    return this
end function

sub sequenceTests_lambda_lambda_31_invoke_k_()
    list = listOf_Arr_k_(["a", "b", "c"])
    collected = mutableListOf_k_()
    forEachIndexed_rSequence_Function2IV_k_(asSequence_rIterable_k_(list), sequenceTests_lambda_lambda_lambda_26_create_MutableListStr_k_(collected))
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_(["0:a", "1:b", "2:c"]), collected, invalid)
end sub

function sequenceTests_lambda_lambda_lambda_27_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_lambda_27"
    this.__proto = ["sequenceTests_lambda_lambda_lambda_27", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = sequenceTests_lambda_lambda_lambda_27_invoke_AnyN_k_
    return this
end function

function sequenceTests_lambda_lambda_lambda_27_invoke_AnyN_k_(it as Integer) as Boolean
    return it > 2
end function

function sequenceTests_lambda_lambda_lambda_28_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_lambda_28"
    this.__proto = ["sequenceTests_lambda_lambda_lambda_28", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = sequenceTests_lambda_lambda_lambda_28_invoke_AnyN_k_
    return this
end function

function sequenceTests_lambda_lambda_lambda_28_invoke_AnyN_k_(it as Integer) as Boolean
    return it > 10
end function

function sequenceTests_lambda_lambda_32_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_32"
    this.__proto = ["sequenceTests_lambda_lambda_32", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = sequenceTests_lambda_lambda_32_invoke_k_
    return this
end function

sub sequenceTests_lambda_lambda_32_invoke_k_()
    list = listOf_Arr_k_([1, 2, 3, 4, 5])
    assertEquals_AnyN_AnyN_StrN_k_(3, find_rSequence_Function1Z_k_(asSequence_rIterable_k_(list), sequenceTests_lambda_lambda_lambda_27_create_k_()), invalid)
    assertNull_AnyN_StrN_k_(find_rSequence_Function1Z_k_(asSequence_rIterable_k_(list), sequenceTests_lambda_lambda_lambda_28_create_k_()), invalid)
end sub

function sequenceTests_lambda_lambda_lambda_29_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_lambda_29"
    this.__proto = ["sequenceTests_lambda_lambda_lambda_29", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = sequenceTests_lambda_lambda_lambda_29_invoke_AnyN_k_
    return this
end function

function sequenceTests_lambda_lambda_lambda_29_invoke_AnyN_k_(it as Integer) as Boolean
    return it > 2
end function

function sequenceTests_lambda_lambda_lambda_30_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_lambda_30"
    this.__proto = ["sequenceTests_lambda_lambda_lambda_30", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = sequenceTests_lambda_lambda_lambda_30_invoke_AnyN_k_
    return this
end function

function sequenceTests_lambda_lambda_lambda_30_invoke_AnyN_k_(it as Integer) as Boolean
    return it > 10
end function

function sequenceTests_lambda_lambda_33_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_33"
    this.__proto = ["sequenceTests_lambda_lambda_33", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = sequenceTests_lambda_lambda_33_invoke_k_
    return this
end function

sub sequenceTests_lambda_lambda_33_invoke_k_()
    list = listOf_Arr_k_([1, 2, 3, 4, 5])
    assertEquals_AnyN_AnyN_StrN_k_(2, indexOfFirst_rSequence_Function1Z_k_(asSequence_rIterable_k_(list), sequenceTests_lambda_lambda_lambda_29_create_k_()), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(-1, indexOfFirst_rSequence_Function1Z_k_(asSequence_rIterable_k_(list), sequenceTests_lambda_lambda_lambda_30_create_k_()), invalid)
end sub

function sequenceTests_lambda_lambda_lambda_31_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_lambda_31"
    this.__proto = ["sequenceTests_lambda_lambda_lambda_31", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = sequenceTests_lambda_lambda_lambda_31_invoke_AnyN_k_
    return this
end function

function sequenceTests_lambda_lambda_lambda_31_invoke_AnyN_k_(it as Integer) as Dynamic
    __when_tmp1 = invalid
    if it > 3 then
        __when_tmp1 = (it * 2)
    else if true then
        __when_tmp1 = invalid
    end if
    return __when_tmp1

end function

function sequenceTests_lambda_lambda_34_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_34"
    this.__proto = ["sequenceTests_lambda_lambda_34", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = sequenceTests_lambda_lambda_34_invoke_k_
    return this
end function

sub sequenceTests_lambda_lambda_34_invoke_k_()
    list = listOf_Arr_k_([1, 2, 3, 4, 5])
    result = firstNotNullOf_rSequence_Function1_k_(asSequence_rIterable_k_(list), sequenceTests_lambda_lambda_lambda_31_create_k_())
    assertEquals_AnyN_AnyN_StrN_k_(8, result, invalid)
end sub

function sequenceTests_lambda_lambda_lambda_32_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_lambda_32"
    this.__proto = ["sequenceTests_lambda_lambda_lambda_32", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = sequenceTests_lambda_lambda_lambda_32_invoke_AnyN_k_
    return this
end function

function sequenceTests_lambda_lambda_lambda_32_invoke_AnyN_k_(it as Integer) as Integer
    return it mod 2
end function

function sequenceTests_lambda_lambda_lambda_33_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_lambda_33"
    this.__proto = ["sequenceTests_lambda_lambda_lambda_33", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = sequenceTests_lambda_lambda_lambda_33_invoke_AnyN_k_
    return this
end function

function sequenceTests_lambda_lambda_lambda_33_invoke_AnyN_k_(it as Integer) as Integer
    return it * 10
end function

function sequenceTests_lambda_lambda_35_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda_lambda_35"
    this.__proto = ["sequenceTests_lambda_lambda_35", "Function0", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_k_ = sequenceTests_lambda_lambda_35_invoke_k_
    return this
end function

sub sequenceTests_lambda_lambda_35_invoke_k_()
    list = listOf_Arr_k_([1, 2, 3, 4, 5, 6])
    result = groupBy_rSequence_Function1_Function1_k_(asSequence_rIterable_k_(list), sequenceTests_lambda_lambda_lambda_32_create_k_(), sequenceTests_lambda_lambda_lambda_33_create_k_())
    assertEquals_AnyN_AnyN_StrN_k_(2, result.__get_size(), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([20, 40, 60]), result.get_AnyN_k_(0), invalid)
    assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([10, 30, 50]), result.get_AnyN_k_(1), invalid)
end sub

function sequenceTests_lambda_create_k_() as Object
    this = {}
    this.__type = "sequenceTests_lambda"
    this.__proto = ["sequenceTests_lambda", "Function1", "Function"]
    this.__id = __kotlin_nextObjectId()
    this.equals = Any_equals_AnyN_k_
    this.hashCode = Any_hashCode_k_
    this.toString = Any_toString_k_
    this.invoke_AnyN_k_ = sequenceTests_lambda_invoke_AnyN_k_
    return this
end function

sub sequenceTests_lambda_invoke_AnyN_k_(_this_suite as Object)
    _this_suite.test_Str_Function0V_k_("map and filter", sequenceTests_lambda_lambda_create_k_())
    _this_suite.test_Str_Function0V_k_("sequence with unsigned", sequenceTests_lambda_lambda_1_create_k_())
    _this_suite.test_Str_Function0V_k_("take", sequenceTests_lambda_lambda_2_create_k_())
    _this_suite.test_Str_Function0V_k_("drop", sequenceTests_lambda_lambda_3_create_k_())
    _this_suite.test_Str_Function0V_k_("flatMap", sequenceTests_lambda_lambda_4_create_k_())
    _this_suite.test_Str_Function0V_k_("distinct", sequenceTests_lambda_lambda_5_create_k_())
    _this_suite.test_Str_Function0V_k_("sum", sequenceTests_lambda_lambda_6_create_k_())
    _this_suite.test_Str_Function0V_k_("sorted", sequenceTests_lambda_lambda_7_create_k_())
    _this_suite.test_Str_Function0V_k_("groupBy", sequenceTests_lambda_lambda_8_create_k_())
    _this_suite.test_Str_Function0V_k_("count", sequenceTests_lambda_lambda_9_create_k_())
    _this_suite.test_Str_Function0V_k_("all", sequenceTests_lambda_lambda_10_create_k_())
    _this_suite.test_Str_Function0V_k_("any", sequenceTests_lambda_lambda_11_create_k_())
    _this_suite.test_Str_Function0V_k_("none", sequenceTests_lambda_lambda_12_create_k_())
    _this_suite.test_Str_Function0V_k_("fold", sequenceTests_lambda_lambda_13_create_k_())
    _this_suite.test_Str_Function0V_k_("reduce", sequenceTests_lambda_lambda_14_create_k_())
    _this_suite.test_Str_Function0V_k_("first", sequenceTests_lambda_lambda_15_create_k_())
    _this_suite.test_Str_Function0V_k_("last", sequenceTests_lambda_lambda_16_create_k_())
    _this_suite.test_Str_Function0V_k_("firstOrNull", sequenceTests_lambda_lambda_17_create_k_())
    _this_suite.test_Str_Function0V_k_("single", sequenceTests_lambda_lambda_18_create_k_())
    _this_suite.test_Str_Function0V_k_("elementAt", sequenceTests_lambda_lambda_19_create_k_())
    _this_suite.test_Str_Function0V_k_("elementAtOrNull", sequenceTests_lambda_lambda_20_create_k_())
    _this_suite.test_Str_Function0V_k_("indexOf", sequenceTests_lambda_lambda_21_create_k_())
    _this_suite.test_Str_Function0V_k_("contains", sequenceTests_lambda_lambda_22_create_k_())
    _this_suite.test_Str_Function0V_k_("minMax", sequenceTests_lambda_lambda_23_create_k_())
    _this_suite.test_Str_Function0V_k_("partition", sequenceTests_lambda_lambda_24_create_k_())
    _this_suite.test_Str_Function0V_k_("joinToString", sequenceTests_lambda_lambda_25_create_k_())
    _this_suite.test_Str_Function0V_k_("onEach", sequenceTests_lambda_lambda_26_create_k_())
    _this_suite.test_Str_Function0V_k_("withIndex", sequenceTests_lambda_lambda_27_create_k_())
    _this_suite.test_Str_Function0V_k_("mapNotNull", sequenceTests_lambda_lambda_28_create_k_())
    _this_suite.test_Str_Function0V_k_("scan", sequenceTests_lambda_lambda_29_create_k_())
    _this_suite.test_Str_Function0V_k_("forEach", sequenceTests_lambda_lambda_30_create_k_())
    _this_suite.test_Str_Function0V_k_("forEachIndexed", sequenceTests_lambda_lambda_31_create_k_())
    _this_suite.test_Str_Function0V_k_("find", sequenceTests_lambda_lambda_32_create_k_())
    _this_suite.test_Str_Function0V_k_("indexOfFirst", sequenceTests_lambda_lambda_33_create_k_())
    _this_suite.test_Str_Function0V_k_("firstNotNullOf", sequenceTests_lambda_lambda_34_create_k_())
    _this_suite.test_Str_Function0V_k_("groupBy with transform", sequenceTests_lambda_lambda_35_create_k_())
end sub
