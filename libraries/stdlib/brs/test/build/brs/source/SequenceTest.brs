sub sequenceTests_rTestRunner_k_(m as Object)
    m.suite_Str_Function1TestRunnerV_k_("Sequence", {invoke: function(m as Object) as Void
        m.test_Str_Function0V_k_("map and filter", {invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 3, 4, 5])
            result = toList_rSequenceAnyN_ListAnyN_k_(filter_rSequenceAnyN_Function1AnyNZ_SequenceAnyN_k_(map_rSequenceAnyN_Function1AnyNAnyN_SequenceAnyN_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list), {invoke: function(it as Integer) as Integer
                return it * 2
            end function}), {invoke: function(it as Integer) as Boolean
                return it > 5
            end function}))
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([6, 8, 10]), result)
        end function})
        m.test_Str_Function0V_k_("sequence with unsigned", {invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([UInt_create_I_UInt_k_(1), UInt_create_I_UInt_k_(2), UInt_create_I_UInt_k_(3), UInt_create_I_UInt_k_(4), UInt_create_I_UInt_k_(5)])
            result = toList_rSequenceAnyN_ListAnyN_k_(map_rSequenceAnyN_Function1AnyNAnyN_SequenceAnyN_k_(filter_rSequenceAnyN_Function1AnyNZ_SequenceAnyN_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list), {invoke: function(it as Object) as Boolean
                return (it > UInt_create_I_UInt_k_(2)) > 0
            end function}), {invoke: function(it as Object) as Object
                return it * UInt_create_I_UInt_k_(2)
            end function}))
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([UInt_create_I_UInt_k_(6), UInt_create_I_UInt_k_(8), UInt_create_I_UInt_k_(10)]), result)
        end function})
        m.test_Str_Function0V_k_("take", {invoke: function() as Void
            result = toList_rSequenceAnyN_ListAnyN_k_(take_rSequenceAnyN_I_SequenceAnyN_k_(generateSequence_AnyN_Function1AnyAnyN_SequenceAny_k_(1, {invoke: function(it as Integer) as Dynamic
                return it + 1
            end function}), 5))
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([1, 2, 3, 4, 5]), result)
        end function})
        m.test_Str_Function0V_k_("drop", {invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 3, 4, 5])
            result = toList_rSequenceAnyN_ListAnyN_k_(drop_rSequenceAnyN_I_SequenceAnyN_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list), 2))
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([3, 4, 5]), result)
        end function})
        m.test_Str_Function0V_k_("flatMap", {invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 3])
            result = toList_rSequenceAnyN_ListAnyN_k_(flatMap_rSequenceAnyN_Function1AnyNIterableAnyN_SequenceAnyN_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list), {invoke: function(n as Integer) as Object
                return toList_rIterableAnyN_ListAnyN_k_(1.rangeTo_I_IntRange_k_(n))
            end function}))
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([1, 1, 2, 1, 2, 3]), result)
        end function})
        m.test_Str_Function0V_k_("distinct", {invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 2, 3, 3, 3])
            result = toList_rSequenceAnyN_ListAnyN_k_(distinct_rSequenceAnyN_SequenceAnyN_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list)))
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([1, 2, 3]), result)
        end function})
        m.test_Str_Function0V_k_("sum", {invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 3, 4, 5])
            result = sum_rSequenceI_I_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list))
            assertEquals_AnyN_AnyN_StrN_k_(15, result)
        end function})
        m.test_Str_Function0V_k_("sorted", {invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([5, 2, 4, 1, 3])
            result = toList_rSequenceAnyN_ListAnyN_k_(sorted_rSequenceAny_SequenceAny_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list)))
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([1, 2, 3, 4, 5]), result)
        end function})
        m.test_Str_Function0V_k_("groupBy", {invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 3, 4, 5, 6])
            result = groupBy_rSequenceAnyN_Function1AnyNAnyN_MapAnyNListAnyN_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list), {invoke: function(it as Integer) as Integer
                return it mod 2
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(2, result.get_size())
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([2, 4, 6]), result.get_AnyN_AnyN_k_(0))
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([1, 3, 5]), result.get_AnyN_AnyN_k_(1))
        end function})
        m.test_Str_Function0V_k_("count", {invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 3, 4, 5])
            assertEquals_AnyN_AnyN_StrN_k_(5, count_rSequenceAnyN_I_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list)))
            assertEquals_AnyN_AnyN_StrN_k_(3, count_rSequenceAnyN_Function1AnyNZ_I_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list), {invoke: function(it as Integer) as Boolean
                return it > 2
            end function}))
        end function})
        m.test_Str_Function0V_k_("all", {invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([2, 4, 6, 8])
            assertTrue_Z_StrN_k_(all_rSequenceAnyN_Function1AnyNZ_Z_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list), {invoke: function(it as Integer) as Boolean
                return (it mod 2) = 0
            end function}))
            assertFalse_Z_StrN_k_(all_rSequenceAnyN_Function1AnyNZ_Z_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list), {invoke: function(it as Integer) as Boolean
                return it > 5
            end function}))
        end function})
        m.test_Str_Function0V_k_("any", {invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 3, 5, 7])
            assertTrue_Z_StrN_k_(any_rSequenceAnyN_Z_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list)))
            assertTrue_Z_StrN_k_(any_rSequenceAnyN_Function1AnyNZ_Z_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list), {invoke: function(it as Integer) as Boolean
                return it > 5
            end function}))
            assertFalse_Z_StrN_k_(any_rSequenceAnyN_Function1AnyNZ_Z_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list), {invoke: function(it as Integer) as Boolean
                return it > 10
            end function}))
        end function})
        m.test_Str_Function0V_k_("none", {invoke: function() as Void
            empty = emptyList_ListAnyN_k_()
            assertTrue_Z_StrN_k_(none_rSequenceAnyN_Z_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(empty)))
            list = listOf_Arr_ListAnyN_k_([1, 3, 5, 7])
            assertTrue_Z_StrN_k_(none_rSequenceAnyN_Function1AnyNZ_Z_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list), {invoke: function(it as Integer) as Boolean
                return (it mod 2) = 0
            end function}))
            assertFalse_Z_StrN_k_(none_rSequenceAnyN_Function1AnyNZ_Z_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list), {invoke: function(it as Integer) as Boolean
                return it > 5
            end function}))
        end function})
        m.test_Str_Function0V_k_("fold", {invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 3, 4, 5])
            result = fold_rSequenceAnyN_AnyN_Function2AnyNAnyNAnyN_AnyN_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list), 0, {invoke: function(acc as Integer, value as Integer) as Integer
                return acc + value
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(15, result)
        end function})
        m.test_Str_Function0V_k_("reduce", {invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 3, 4, 5])
            result = reduce_rSequenceAnyN_Function2AnyNAnyNAnyN_AnyN_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list), {invoke: function(acc as Integer, value as Integer) as Integer
                return acc + value
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(15, result)
        end function})
        m.test_Str_Function0V_k_("first", {invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 3, 4, 5])
            assertEquals_AnyN_AnyN_StrN_k_(1, first_rSequenceAnyN_AnyN_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list)))
            assertEquals_AnyN_AnyN_StrN_k_(3, first_rSequenceAnyN_Function1AnyNZ_AnyN_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list), {invoke: function(it as Integer) as Boolean
                return it > 2
            end function}))
        end function})
        m.test_Str_Function0V_k_("last", {invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 3, 4, 5])
            assertEquals_AnyN_AnyN_StrN_k_(5, last_rSequenceAnyN_AnyN_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list)))
            assertEquals_AnyN_AnyN_StrN_k_(4, last_rSequenceAnyN_Function1AnyNZ_AnyN_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list), {invoke: function(it as Integer) as Boolean
                return it < 5
            end function}))
        end function})
        m.test_Str_Function0V_k_("firstOrNull", {invoke: function() as Void
            empty = emptyList_ListAnyN_k_()
            assertNull_AnyN_StrN_k_(firstOrNull_rSequenceAnyN_AnyN_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(empty)))
            list = listOf_Arr_ListAnyN_k_([1, 2, 3])
            assertEquals_AnyN_AnyN_StrN_k_(1, firstOrNull_rSequenceAnyN_AnyN_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list)))
            assertEquals_AnyN_AnyN_StrN_k_(3, firstOrNull_rSequenceAnyN_Function1AnyNZ_AnyN_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list), {invoke: function(it as Integer) as Boolean
                return it > 2
            end function}))
            assertNull_AnyN_StrN_k_(firstOrNull_rSequenceAnyN_Function1AnyNZ_AnyN_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list), {invoke: function(it as Integer) as Boolean
                return it > 10
            end function}))
        end function})
        m.test_Str_Function0V_k_("single", {invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([42])
            assertEquals_AnyN_AnyN_StrN_k_(42, single_rSequenceAnyN_AnyN_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list)))
            multiList = listOf_Arr_ListAnyN_k_([1, 2, 3])
            assertEquals_AnyN_AnyN_StrN_k_(2, single_rSequenceAnyN_Function1AnyNZ_AnyN_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(multiList), {invoke: function(it as Integer) as Boolean
                return it = 2
            end function}))
        end function})
        m.test_Str_Function0V_k_("elementAt", {invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 3, 4, 5])
            assertEquals_AnyN_AnyN_StrN_k_(1, elementAt_rSequenceAnyN_I_AnyN_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list), 0))
            assertEquals_AnyN_AnyN_StrN_k_(3, elementAt_rSequenceAnyN_I_AnyN_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list), 2))
            assertEquals_AnyN_AnyN_StrN_k_(5, elementAt_rSequenceAnyN_I_AnyN_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list), 4))
        end function})
        m.test_Str_Function0V_k_("elementAtOrNull", {invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 3])
            assertEquals_AnyN_AnyN_StrN_k_(2, elementAtOrNull_rSequenceAnyN_I_AnyN_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list), 1))
            assertNull_AnyN_StrN_k_(elementAtOrNull_rSequenceAnyN_I_AnyN_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list), 10))
            assertNull_AnyN_StrN_k_(elementAtOrNull_rSequenceAnyN_I_AnyN_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list), -1))
        end function})
        m.test_Str_Function0V_k_("indexOf", {invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 3, 2, 1])
            assertEquals_AnyN_AnyN_StrN_k_(0, indexOf_rSequenceAnyN_AnyN_I_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list), 1))
            assertEquals_AnyN_AnyN_StrN_k_(1, indexOf_rSequenceAnyN_AnyN_I_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list), 2))
            assertEquals_AnyN_AnyN_StrN_k_(-1, indexOf_rSequenceAnyN_AnyN_I_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list), 5))
        end function})
        m.test_Str_Function0V_k_("contains", {invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 3, 4, 5])
            assertTrue_Z_StrN_k_(contains_rSequenceAnyN_AnyN_Z_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list), 3))
            assertFalse_Z_StrN_k_(contains_rSequenceAnyN_AnyN_Z_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list), 10))
        end function})
        m.test_Str_Function0V_k_("minMax", {invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([5, 2, 8, 1, 9, 3])
            assertEquals_AnyN_AnyN_StrN_k_(1, minOrNull_rSequenceAny_AnyN_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list)))
            assertEquals_AnyN_AnyN_StrN_k_(9, maxOrNull_rSequenceAny_AnyN_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list)))
        end function})
        m.test_Str_Function0V_k_("partition", {invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 3, 4, 5, 6])
            __destruct_1 = partition_rSequenceAnyN_Function1AnyNZ_PairListAnyNListAnyN_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list), {invoke: function(it as Integer) as Boolean
                return (it mod 2) = 0
            end function})
            evens = __destruct_1.component1()
            odds = __destruct_1.component2()
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([2, 4, 6]), evens)
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([1, 3, 5]), odds)
        end function})
        m.test_Str_Function0V_k_("joinToString", {invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 3, 4, 5])
            assertEquals_AnyN_AnyN_StrN_k_("1, 2, 3, 4, 5", joinToString_h85ta6_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list)))
            assertEquals_AnyN_AnyN_StrN_k_("1-2-3-4-5", joinToString_h85ta6_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list), "-"))
            assertEquals_AnyN_AnyN_StrN_k_("[1, 2, 3]", joinToString_h85ta6_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list), "[", "]"))
            assertEquals_AnyN_AnyN_StrN_k_("1, 2, ...", joinToString_h85ta6_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list), 2))
        end function})
        m.test_Str_Function0V_k_("onEach", {invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 3])
            sum = 0
            result = toList_rSequenceAnyN_ListAnyN_k_(onEach_rSequenceAnyN_Function1AnyNV_SequenceAnyN_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list), {sum: {value: sum}, invoke: function(it as Integer) as Void
                m.sum.value = (m.sum.value + it)
            end function}))
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([1, 2, 3]), result)
            assertEquals_AnyN_AnyN_StrN_k_(6, sum)
        end function})
        m.test_Str_Function0V_k_("withIndex", {invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_(["a", "b", "c"])
            result = toList_rSequenceAnyN_ListAnyN_k_(withIndex_rSequenceAnyN_SequenceIndexedValueAnyN_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list)))
            assertEquals_AnyN_AnyN_StrN_k_(0, result.get_I_AnyN_k_(0).index)
            assertEquals_AnyN_AnyN_StrN_k_("a", result.get_I_AnyN_k_(0).value)
            assertEquals_AnyN_AnyN_StrN_k_(2, result.get_I_AnyN_k_(2).index)
            assertEquals_AnyN_AnyN_StrN_k_("c", result.get_I_AnyN_k_(2).value)
        end function})
        m.test_Str_Function0V_k_("mapNotNull", {invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 3, 4, 5])
            result = toList_rSequenceAnyN_ListAnyN_k_(mapNotNull_rSequenceAnyN_Function1AnyNAnyN_SequenceAny_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list), {invoke: function(it as Integer) as Dynamic
                __when_tmp0 = invalid
                if (it mod 2) = 0 then
                    __when_tmp0 = (it * 2)
                else if true then
                    __when_tmp0 = invalid
                end if
                return __when_tmp0

            end function}))
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([4, 8]), result)
        end function})
        m.test_Str_Function0V_k_("scan", {invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 3, 4])
            result = toList_rSequenceAnyN_ListAnyN_k_(scan_rSequenceAnyN_AnyN_Function2AnyNAnyNAnyN_SequenceAnyN_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list), 0, {invoke: function(acc as Integer, value as Integer) as Integer
                return acc + value
            end function}))
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([0, 1, 3, 6, 10]), result)
        end function})
        m.test_Str_Function0V_k_("forEach", {invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 3])
            collected = mutableListOf_MutableListAnyN_k_()
            forEach_rSequenceAnyN_Function1AnyNV_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list), {collected: collected, invoke: function(it as Integer) as Void
                m.collected.add_AnyN_Z_k_(it * 2)
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([2, 4, 6]), collected)
        end function})
        m.test_Str_Function0V_k_("forEachIndexed", {invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_(["a", "b", "c"])
            collected = mutableListOf_MutableListAnyN_k_()
            forEachIndexed_rSequenceAnyN_Function2IAnyNV_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list), {collected: collected, invoke: function(index as Integer, value as String) as Void
                m.collected.add_AnyN_Z_k_((index + ":") + value)
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_(["0:a", "1:b", "2:c"]), collected)
        end function})
        m.test_Str_Function0V_k_("find", {invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 3, 4, 5])
            assertEquals_AnyN_AnyN_StrN_k_(3, find_rSequenceAnyN_Function1AnyNZ_AnyN_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list), {invoke: function(it as Integer) as Boolean
                return it > 2
            end function}))
            assertNull_AnyN_StrN_k_(find_rSequenceAnyN_Function1AnyNZ_AnyN_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list), {invoke: function(it as Integer) as Boolean
                return it > 10
            end function}))
        end function})
        m.test_Str_Function0V_k_("indexOfFirst", {invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 3, 4, 5])
            assertEquals_AnyN_AnyN_StrN_k_(2, indexOfFirst_rSequenceAnyN_Function1AnyNZ_I_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list), {invoke: function(it as Integer) as Boolean
                return it > 2
            end function}))
            assertEquals_AnyN_AnyN_StrN_k_(-1, indexOfFirst_rSequenceAnyN_Function1AnyNZ_I_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list), {invoke: function(it as Integer) as Boolean
                return it > 10
            end function}))
        end function})
        m.test_Str_Function0V_k_("firstNotNullOf", {invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 3, 4, 5])
            result = firstNotNullOf_rSequenceAnyN_Function1AnyNAnyN_Any_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list), {invoke: function(it as Integer) as Dynamic
                __when_tmp1 = invalid
                if it > 3 then
                    __when_tmp1 = (it * 2)
                else if true then
                    __when_tmp1 = invalid
                end if
                return __when_tmp1

            end function})
            assertEquals_AnyN_AnyN_StrN_k_(8, result)
        end function})
        m.test_Str_Function0V_k_("groupBy with transform", {invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 3, 4, 5, 6])
            result = groupBy_rSequenceAnyN_Function1AnyNAnyN_Function1AnyNAnyN_MapAnyNListAnyN_k_(asSequence_rIterableAnyN_SequenceAnyN_k_(list), {invoke: function(it as Integer) as Integer
                return it mod 2
            end function}, {invoke: function(it as Integer) as Integer
                return it * 10
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(2, result.get_size())
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([20, 40, 60]), result.get_AnyN_AnyN_k_(0))
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([10, 30, 50]), result.get_AnyN_AnyN_k_(1))
        end function})
    end function})
end sub
