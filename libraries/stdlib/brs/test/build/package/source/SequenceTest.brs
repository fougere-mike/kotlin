sub sequenceTests_rTestRunner_k_(m as Object)
    m.suite_Str_Function1TestRunnerV_k_("Sequence", {invoke: function(m as Object) as Void
        m.test_Str_Function0V_k_("map and filter", {invoke: function() as Void
            list = listOf_Arr_k_([1, 2, 3, 4, 5])
            result = toList_rSequence_k_(filter_rSequence_Function1Z_k_(map_rSequence_Function1_k_(asSequence_rIterable_k_(list), {invoke: function(it as Integer) as Integer
                return it * 2
            end function}), {invoke: function(it as Integer) as Boolean
                return it > 5
            end function}))
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([6, 8, 10]), result)
        end function})
        m.test_Str_Function0V_k_("sequence with unsigned", {invoke: function() as Void
            list = listOf_Arr_k_([UInt_create_I_k_(1), UInt_create_I_k_(2), UInt_create_I_k_(3), UInt_create_I_k_(4), UInt_create_I_k_(5)])
            result = toList_rSequence_k_(map_rSequence_Function1_k_(filter_rSequence_Function1Z_k_(asSequence_rIterable_k_(list), {invoke: function(it as Object) as Boolean
                return (it > UInt_create_I_k_(2)) > 0
            end function}), {invoke: function(it as Object) as Object
                return it * UInt_create_I_k_(2)
            end function}))
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([UInt_create_I_k_(6), UInt_create_I_k_(8), UInt_create_I_k_(10)]), result)
        end function})
        m.test_Str_Function0V_k_("take", {invoke: function() as Void
            result = toList_rSequence_k_(take_rSequence_I_k_(generateSequence_AnyN_Function1_k_(1, {invoke: function(it as Integer) as Dynamic
                return it + 1
            end function}), 5))
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([1, 2, 3, 4, 5]), result)
        end function})
        m.test_Str_Function0V_k_("drop", {invoke: function() as Void
            list = listOf_Arr_k_([1, 2, 3, 4, 5])
            result = toList_rSequence_k_(drop_rSequence_I_k_(asSequence_rIterable_k_(list), 2))
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([3, 4, 5]), result)
        end function})
        m.test_Str_Function0V_k_("flatMap", {invoke: function() as Void
            list = listOf_Arr_k_([1, 2, 3])
            result = toList_rSequence_k_(flatMap_rSequence_Function1Iterable_k_(asSequence_rIterable_k_(list), {invoke: function(n as Integer) as Object
                return toList_rIterable_k_(rangeTo_rI_I_k_(1, n))
            end function}))
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([1, 1, 2, 1, 2, 3]), result)
        end function})
        m.test_Str_Function0V_k_("distinct", {invoke: function() as Void
            list = listOf_Arr_k_([1, 2, 2, 3, 3, 3])
            result = toList_rSequence_k_(distinct_rSequence_k_(asSequence_rIterable_k_(list)))
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([1, 2, 3]), result)
        end function})
        m.test_Str_Function0V_k_("sum", {invoke: function() as Void
            list = listOf_Arr_k_([1, 2, 3, 4, 5])
            result = sum_rSequenceI_k_(asSequence_rIterable_k_(list))
            assertEquals_AnyN_AnyN_StrN_k_(15, result)
        end function})
        m.test_Str_Function0V_k_("sorted", {invoke: function() as Void
            list = listOf_Arr_k_([5, 2, 4, 1, 3])
            result = toList_rSequence_k_(sorted_rSequence_k_(asSequence_rIterable_k_(list)))
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([1, 2, 3, 4, 5]), result)
        end function})
        m.test_Str_Function0V_k_("groupBy", {invoke: function() as Void
            list = listOf_Arr_k_([1, 2, 3, 4, 5, 6])
            result = groupBy_rSequence_Function1_k_(asSequence_rIterable_k_(list), {invoke: function(it as Integer) as Integer
                return it mod 2
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(2, result.get_size())
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([2, 4, 6]), result.get_AnyN_k_(0))
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([1, 3, 5]), result.get_AnyN_k_(1))
        end function})
        m.test_Str_Function0V_k_("count", {invoke: function() as Void
            list = listOf_Arr_k_([1, 2, 3, 4, 5])
            assertEquals_AnyN_AnyN_StrN_k_(5, count_rSequence_k_(asSequence_rIterable_k_(list)))
            assertEquals_AnyN_AnyN_StrN_k_(3, count_rSequence_Function1Z_k_(asSequence_rIterable_k_(list), {invoke: function(it as Integer) as Boolean
                return it > 2
            end function}))
        end function})
        m.test_Str_Function0V_k_("all", {invoke: function() as Void
            list = listOf_Arr_k_([2, 4, 6, 8])
            assertTrue_Z_StrN_k_(all_rSequence_Function1Z_k_(asSequence_rIterable_k_(list), {invoke: function(it as Integer) as Boolean
                return (it mod 2) = 0
            end function}))
            assertFalse_Z_StrN_k_(all_rSequence_Function1Z_k_(asSequence_rIterable_k_(list), {invoke: function(it as Integer) as Boolean
                return it > 5
            end function}))
        end function})
        m.test_Str_Function0V_k_("any", {invoke: function() as Void
            list = listOf_Arr_k_([1, 3, 5, 7])
            assertTrue_Z_StrN_k_(any_rSequence_k_(asSequence_rIterable_k_(list)))
            assertTrue_Z_StrN_k_(any_rSequence_Function1Z_k_(asSequence_rIterable_k_(list), {invoke: function(it as Integer) as Boolean
                return it > 5
            end function}))
            assertFalse_Z_StrN_k_(any_rSequence_Function1Z_k_(asSequence_rIterable_k_(list), {invoke: function(it as Integer) as Boolean
                return it > 10
            end function}))
        end function})
        m.test_Str_Function0V_k_("none", {invoke: function() as Void
            empty = emptyList_k_()
            assertTrue_Z_StrN_k_(none_rSequence_k_(asSequence_rIterable_k_(empty)))
            list = listOf_Arr_k_([1, 3, 5, 7])
            assertTrue_Z_StrN_k_(none_rSequence_Function1Z_k_(asSequence_rIterable_k_(list), {invoke: function(it as Integer) as Boolean
                return (it mod 2) = 0
            end function}))
            assertFalse_Z_StrN_k_(none_rSequence_Function1Z_k_(asSequence_rIterable_k_(list), {invoke: function(it as Integer) as Boolean
                return it > 5
            end function}))
        end function})
        m.test_Str_Function0V_k_("fold", {invoke: function() as Void
            list = listOf_Arr_k_([1, 2, 3, 4, 5])
            result = fold_rSequence_AnyN_Function2_k_(asSequence_rIterable_k_(list), 0, {invoke: function(acc as Integer, value as Integer) as Integer
                return acc + value
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(15, result)
        end function})
        m.test_Str_Function0V_k_("reduce", {invoke: function() as Void
            list = listOf_Arr_k_([1, 2, 3, 4, 5])
            result = reduce_rSequence_Function2_k_(asSequence_rIterable_k_(list), {invoke: function(acc as Integer, value as Integer) as Integer
                return acc + value
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(15, result)
        end function})
        m.test_Str_Function0V_k_("first", {invoke: function() as Void
            list = listOf_Arr_k_([1, 2, 3, 4, 5])
            assertEquals_AnyN_AnyN_StrN_k_(1, first_rSequence_k_(asSequence_rIterable_k_(list)))
            assertEquals_AnyN_AnyN_StrN_k_(3, first_rSequence_Function1Z_k_(asSequence_rIterable_k_(list), {invoke: function(it as Integer) as Boolean
                return it > 2
            end function}))
        end function})
        m.test_Str_Function0V_k_("last", {invoke: function() as Void
            list = listOf_Arr_k_([1, 2, 3, 4, 5])
            assertEquals_AnyN_AnyN_StrN_k_(5, last_rSequence_k_(asSequence_rIterable_k_(list)))
            assertEquals_AnyN_AnyN_StrN_k_(4, last_rSequence_Function1Z_k_(asSequence_rIterable_k_(list), {invoke: function(it as Integer) as Boolean
                return it < 5
            end function}))
        end function})
        m.test_Str_Function0V_k_("firstOrNull", {invoke: function() as Void
            empty = emptyList_k_()
            assertNull_AnyN_StrN_k_(firstOrNull_rSequence_k_(asSequence_rIterable_k_(empty)))
            list = listOf_Arr_k_([1, 2, 3])
            assertEquals_AnyN_AnyN_StrN_k_(1, firstOrNull_rSequence_k_(asSequence_rIterable_k_(list)))
            assertEquals_AnyN_AnyN_StrN_k_(3, firstOrNull_rSequence_Function1Z_k_(asSequence_rIterable_k_(list), {invoke: function(it as Integer) as Boolean
                return it > 2
            end function}))
            assertNull_AnyN_StrN_k_(firstOrNull_rSequence_Function1Z_k_(asSequence_rIterable_k_(list), {invoke: function(it as Integer) as Boolean
                return it > 10
            end function}))
        end function})
        m.test_Str_Function0V_k_("single", {invoke: function() as Void
            list = listOf_Arr_k_([42])
            assertEquals_AnyN_AnyN_StrN_k_(42, single_rSequence_k_(asSequence_rIterable_k_(list)))
            multiList = listOf_Arr_k_([1, 2, 3])
            assertEquals_AnyN_AnyN_StrN_k_(2, single_rSequence_Function1Z_k_(asSequence_rIterable_k_(multiList), {invoke: function(it as Integer) as Boolean
                return it = 2
            end function}))
        end function})
        m.test_Str_Function0V_k_("elementAt", {invoke: function() as Void
            list = listOf_Arr_k_([1, 2, 3, 4, 5])
            assertEquals_AnyN_AnyN_StrN_k_(1, elementAt_rSequence_I_k_(asSequence_rIterable_k_(list), 0))
            assertEquals_AnyN_AnyN_StrN_k_(3, elementAt_rSequence_I_k_(asSequence_rIterable_k_(list), 2))
            assertEquals_AnyN_AnyN_StrN_k_(5, elementAt_rSequence_I_k_(asSequence_rIterable_k_(list), 4))
        end function})
        m.test_Str_Function0V_k_("elementAtOrNull", {invoke: function() as Void
            list = listOf_Arr_k_([1, 2, 3])
            assertEquals_AnyN_AnyN_StrN_k_(2, elementAtOrNull_rSequence_I_k_(asSequence_rIterable_k_(list), 1))
            assertNull_AnyN_StrN_k_(elementAtOrNull_rSequence_I_k_(asSequence_rIterable_k_(list), 10))
            assertNull_AnyN_StrN_k_(elementAtOrNull_rSequence_I_k_(asSequence_rIterable_k_(list), -1))
        end function})
        m.test_Str_Function0V_k_("indexOf", {invoke: function() as Void
            list = listOf_Arr_k_([1, 2, 3, 2, 1])
            assertEquals_AnyN_AnyN_StrN_k_(0, indexOf_rSequence_AnyN_k_(asSequence_rIterable_k_(list), 1))
            assertEquals_AnyN_AnyN_StrN_k_(1, indexOf_rSequence_AnyN_k_(asSequence_rIterable_k_(list), 2))
            assertEquals_AnyN_AnyN_StrN_k_(-1, indexOf_rSequence_AnyN_k_(asSequence_rIterable_k_(list), 5))
        end function})
        m.test_Str_Function0V_k_("contains", {invoke: function() as Void
            list = listOf_Arr_k_([1, 2, 3, 4, 5])
            assertTrue_Z_StrN_k_(contains_rSequence_AnyN_k_(asSequence_rIterable_k_(list), 3))
            assertFalse_Z_StrN_k_(contains_rSequence_AnyN_k_(asSequence_rIterable_k_(list), 10))
        end function})
        m.test_Str_Function0V_k_("minMax", {invoke: function() as Void
            list = listOf_Arr_k_([5, 2, 8, 1, 9, 3])
            assertEquals_AnyN_AnyN_StrN_k_(1, minOrNull_rSequence_k_(asSequence_rIterable_k_(list)))
            assertEquals_AnyN_AnyN_StrN_k_(9, maxOrNull_rSequence_k_(asSequence_rIterable_k_(list)))
        end function})
        m.test_Str_Function0V_k_("partition", {invoke: function() as Void
            list = listOf_Arr_k_([1, 2, 3, 4, 5, 6])
            __destruct_1 = partition_rSequence_Function1Z_k_(asSequence_rIterable_k_(list), {invoke: function(it as Integer) as Boolean
                return (it mod 2) = 0
            end function})
            evens = __destruct_1.component1()
            odds = __destruct_1.component2()
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([2, 4, 6]), evens)
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([1, 3, 5]), odds)
        end function})
        m.test_Str_Function0V_k_("joinToString", {invoke: function() as Void
            list = listOf_Arr_k_([1, 2, 3, 4, 5])
            assertEquals_AnyN_AnyN_StrN_k_("1, 2, 3, 4, 5", joinToString_rtpr9o_k_(asSequence_rIterable_k_(list)))
            assertEquals_AnyN_AnyN_StrN_k_("1-2-3-4-5", joinToString_rtpr9o_k_(asSequence_rIterable_k_(list), "-"))
            assertEquals_AnyN_AnyN_StrN_k_("[1, 2, 3]", joinToString_rtpr9o_k_(asSequence_rIterable_k_(list), "[", "]"))
            assertEquals_AnyN_AnyN_StrN_k_("1, 2, ...", joinToString_rtpr9o_k_(asSequence_rIterable_k_(list), 2))
        end function})
        m.test_Str_Function0V_k_("onEach", {invoke: function() as Void
            list = listOf_Arr_k_([1, 2, 3])
            sum = 0
            result = toList_rSequence_k_(onEach_rSequence_Function1V_k_(asSequence_rIterable_k_(list), {sum: {value: sum}, invoke: function(it as Integer) as Void
                m.sum.value = (m.sum.value + it)
            end function}))
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([1, 2, 3]), result)
            assertEquals_AnyN_AnyN_StrN_k_(6, sum)
        end function})
        m.test_Str_Function0V_k_("withIndex", {invoke: function() as Void
            list = listOf_Arr_k_(["a", "b", "c"])
            result = toList_rSequence_k_(withIndex_rSequence_k_(asSequence_rIterable_k_(list)))
            assertEquals_AnyN_AnyN_StrN_k_(0, result.get_I_k_(0).index)
            assertEquals_AnyN_AnyN_StrN_k_("a", result.get_I_k_(0).value)
            assertEquals_AnyN_AnyN_StrN_k_(2, result.get_I_k_(2).index)
            assertEquals_AnyN_AnyN_StrN_k_("c", result.get_I_k_(2).value)
        end function})
        m.test_Str_Function0V_k_("mapNotNull", {invoke: function() as Void
            list = listOf_Arr_k_([1, 2, 3, 4, 5])
            result = toList_rSequence_k_(mapNotNull_rSequence_Function1_k_(asSequence_rIterable_k_(list), {invoke: function(it as Integer) as Dynamic
                __when_tmp0 = invalid
                if (it mod 2) = 0 then
                    __when_tmp0 = (it * 2)
                else if true then
                    __when_tmp0 = invalid
                end if
                return __when_tmp0

            end function}))
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([4, 8]), result)
        end function})
        m.test_Str_Function0V_k_("scan", {invoke: function() as Void
            list = listOf_Arr_k_([1, 2, 3, 4])
            result = toList_rSequence_k_(scan_rSequence_AnyN_Function2_k_(asSequence_rIterable_k_(list), 0, {invoke: function(acc as Integer, value as Integer) as Integer
                return acc + value
            end function}))
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([0, 1, 3, 6, 10]), result)
        end function})
        m.test_Str_Function0V_k_("forEach", {invoke: function() as Void
            list = listOf_Arr_k_([1, 2, 3])
            collected = mutableListOf_k_()
            forEach_rSequence_Function1V_k_(asSequence_rIterable_k_(list), {collected: collected, invoke: function(it as Integer) as Void
                m.collected.add_AnyN_k_(it * 2)
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([2, 4, 6]), collected)
        end function})
        m.test_Str_Function0V_k_("forEachIndexed", {invoke: function() as Void
            list = listOf_Arr_k_(["a", "b", "c"])
            collected = mutableListOf_k_()
            forEachIndexed_rSequence_Function2IV_k_(asSequence_rIterable_k_(list), {collected: collected, invoke: function(index as Integer, value as String) as Void
                m.collected.add_AnyN_k_((index + ":") + value)
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_(["0:a", "1:b", "2:c"]), collected)
        end function})
        m.test_Str_Function0V_k_("find", {invoke: function() as Void
            list = listOf_Arr_k_([1, 2, 3, 4, 5])
            assertEquals_AnyN_AnyN_StrN_k_(3, find_rSequence_Function1Z_k_(asSequence_rIterable_k_(list), {invoke: function(it as Integer) as Boolean
                return it > 2
            end function}))
            assertNull_AnyN_StrN_k_(find_rSequence_Function1Z_k_(asSequence_rIterable_k_(list), {invoke: function(it as Integer) as Boolean
                return it > 10
            end function}))
        end function})
        m.test_Str_Function0V_k_("indexOfFirst", {invoke: function() as Void
            list = listOf_Arr_k_([1, 2, 3, 4, 5])
            assertEquals_AnyN_AnyN_StrN_k_(2, indexOfFirst_rSequence_Function1Z_k_(asSequence_rIterable_k_(list), {invoke: function(it as Integer) as Boolean
                return it > 2
            end function}))
            assertEquals_AnyN_AnyN_StrN_k_(-1, indexOfFirst_rSequence_Function1Z_k_(asSequence_rIterable_k_(list), {invoke: function(it as Integer) as Boolean
                return it > 10
            end function}))
        end function})
        m.test_Str_Function0V_k_("firstNotNullOf", {invoke: function() as Void
            list = listOf_Arr_k_([1, 2, 3, 4, 5])
            result = firstNotNullOf_rSequence_Function1_k_(asSequence_rIterable_k_(list), {invoke: function(it as Integer) as Dynamic
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
            list = listOf_Arr_k_([1, 2, 3, 4, 5, 6])
            result = groupBy_rSequence_Function1_Function1_k_(asSequence_rIterable_k_(list), {invoke: function(it as Integer) as Integer
                return it mod 2
            end function}, {invoke: function(it as Integer) as Integer
                return it * 10
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(2, result.get_size())
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([20, 40, 60]), result.get_AnyN_k_(0))
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([10, 30, 50]), result.get_AnyN_k_(1))
        end function})
    end function})
end sub
