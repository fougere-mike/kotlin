sub collectionExtensionsTests_rTestRunner_k_(m as Object)
    m.suite("Collection Extensions", {_this_suite: _this_suite, it: it, it: it, index: index, value: value, it: it, it: it, it: it, index: index, acc: acc, n: n, acc: acc, n: n, it: it, it: it, it: it, it: it, it: it, it: it, it: it, it: it, it: it, it: it, it: it, it: it, it: it, it: it, it: it, it: it, it: it, i: i, v: v, it: it, it: it, it: it, it: it, it: it, invoke: function() as Void
        m._this_suite.test("map", {it: it, invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 3])
            result = map_rIterableAnyN_Function1AnyNAnyN_ListAnyN_k_(list, {invoke: function(it as Integer) as Integer
                return m.it * 2
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([2, 4, 6]), result)
        end function})
        m._this_suite.test("mapNotNull", {it: it, invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 3, 4])
            result = mapNotNull_rIterableAnyN_Function1AnyNAnyN_ListAny_k_(list, {invoke: function(it as Integer) as Dynamic
                __when_tmp0 = invalid
                if (m.it mod 2) = 0 then
                    __when_tmp0 = (m.it * 2)
                else if true then
                    __when_tmp0 = invalid
                end if
                return __when_tmp0

            end function})
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([4, 8]), result)
        end function})
        m._this_suite.test("mapIndexed", {index: index, value: value, invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_(["a", "b", "c"])
            result = mapIndexed_rIterableAnyN_Function2IAnyNAnyN_ListAnyN_k_(list, {invoke: function(index as Integer, value as String) as String
                return (m.index + ":") + m.value
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_(["0:a", "1:b", "2:c"]), result)
        end function})
        m._this_suite.test("flatMap", {it: it, invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 3])
            result = flatMap_rIterableAnyN_Function1AnyNIterableAnyN_ListAnyN_k_(list, {invoke: function(it as Integer) as Object
                return listOf_Arr_ListAnyN_k_([m.it, m.it * 10])
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([1, 10, 2, 20, 3, 30]), result)
        end function})
        m._this_suite.test("filter", {it: it, invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 3, 4, 5])
            result = filter_rIterableAnyN_Function1AnyNZ_ListAnyN_k_(list, {invoke: function(it as Integer) as Boolean
                return m.it > 3
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([4, 5]), result)
        end function})
        m._this_suite.test("filterNot", {it: it, invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 3, 4, 5])
            result = filterNot_rIterableAnyN_Function1AnyNZ_ListAnyN_k_(list, {invoke: function(it as Integer) as Boolean
                return m.it > 3
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([1, 2, 3]), result)
        end function})
        m._this_suite.test("filterIndexed", {index: index, invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_(["a", "b", "c", "d"])
            result = filterIndexed_rIterableAnyN_Function2IAnyNZ_ListAnyN_k_(list, {invoke: function(index as Integer, _unused as String) as Boolean
                return (m.index mod 2) = 0
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_(["a", "c"]), result)
        end function})
        m._this_suite.test("filterNotNull", {invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, invalid, 2, invalid, 3])
            result = filterNotNull_rIterableAnyN_ListAny_k_(list)
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([1, 2, 3]), result)
        end function})
        m._this_suite.test("fold", {acc: acc, n: n, invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 3, 4])
            sum = fold_rIterableAnyN_AnyN_Function2AnyNAnyNAnyN_AnyN_k_(list, 0, {invoke: function(acc as Integer, n as Integer) as Integer
                return m.acc + m.n
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(10, sum)
        end function})
        m._this_suite.test("reduce", {acc: acc, n: n, invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 3, 4])
            product = reduce_rIterableAnyN_Function2AnyNAnyNAnyN_AnyN_k_(list, {invoke: function(acc as Integer, n as Integer) as Integer
                return m.acc * m.n
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(24, product)
        end function})
        m._this_suite.test("sum", {invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 3, 4, 5])
            assertEquals_AnyN_AnyN_StrN_k_(15, sum_rIterableI_I_k_(list))
        end function})
        m._this_suite.test("sumOf", {it: it, invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_(["a", "bb", "ccc"])
            assertEquals_AnyN_AnyN_StrN_k_(6, sumOf_rIterableAnyN_Function1AnyNI_I_k_(list, {invoke: function(it as String) as Integer
                return m.it.length
            end function}))
        end function})
        m._this_suite.test("count", {it: it, invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 3, 4, 5])
            assertEquals_AnyN_AnyN_StrN_k_(5, count_rIterableAnyN_I_k_(list))
            assertEquals_AnyN_AnyN_StrN_k_(2, count_rIterableAnyN_Function1AnyNZ_I_k_(list, {invoke: function(it as Integer) as Boolean
                return m.it > 3
            end function}))
        end function})
        m._this_suite.test("any", {it: it, it: it, invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 3])
            assertTrue_Z_StrN_k_(any_rIterableAnyN_Z_k_(list))
            assertTrue_Z_StrN_k_(any_rIterableAnyN_Function1AnyNZ_Z_k_(list, {invoke: function(it as Integer) as Boolean
                return m.it > 2
            end function}))
            assertFalse_Z_StrN_k_(any_rIterableAnyN_Function1AnyNZ_Z_k_(list, {invoke: function(it as Integer) as Boolean
                return m.it > 10
            end function}))
        end function})
        m._this_suite.test("all", {it: it, it: it, invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([2, 4, 6])
            assertTrue_Z_StrN_k_(all_rIterableAnyN_Function1AnyNZ_Z_k_(list, {invoke: function(it as Integer) as Boolean
                return (m.it mod 2) = 0
            end function}))
            assertFalse_Z_StrN_k_(all_rIterableAnyN_Function1AnyNZ_Z_k_(list, {invoke: function(it as Integer) as Boolean
                return m.it > 3
            end function}))
        end function})
        m._this_suite.test("none", {it: it, invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 3])
            assertFalse_Z_StrN_k_(none_rIterableAnyN_Z_k_(list))
            assertTrue_Z_StrN_k_(none_rIterableAnyN_Function1AnyNZ_Z_k_(list, {invoke: function(it as Integer) as Boolean
                return m.it > 10
            end function}))
        end function})
        m._this_suite.test("first and last", {it: it, invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 3])
            assertEquals_AnyN_AnyN_StrN_k_(1, first_rIterableAnyN_AnyN_k_(list))
            assertEquals_AnyN_AnyN_StrN_k_(3, last_rIterableAnyN_AnyN_k_(list))
            assertEquals_AnyN_AnyN_StrN_k_(2, first_rIterableAnyN_Function1AnyNZ_AnyN_k_(list, {invoke: function(it as Integer) as Boolean
                return m.it > 1
            end function}))
        end function})
        m._this_suite.test("firstOrNull and lastOrNull", {it: it, invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 3])
            assertEquals_AnyN_AnyN_StrN_k_(1, firstOrNull_rIterableAnyN_AnyN_k_(list))
            assertNull_AnyN_StrN_k_(firstOrNull_rIterableAnyN_Function1AnyNZ_AnyN_k_(list, {invoke: function(it as Integer) as Boolean
                return m.it > 10
            end function}))
            empty = emptyList_ListAnyN_k_()
            assertNull_AnyN_StrN_k_(firstOrNull_rIterableAnyN_AnyN_k_(empty))
        end function})
        m._this_suite.test("find", {it: it, it: it, invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 3, 4])
            assertEquals_AnyN_AnyN_StrN_k_(3, find_rIterableAnyN_Function1AnyNZ_AnyN_k_(list, {invoke: function(it as Integer) as Boolean
                return m.it > 2
            end function}))
            assertNull_AnyN_StrN_k_(find_rIterableAnyN_Function1AnyNZ_AnyN_k_(list, {invoke: function(it as Integer) as Boolean
                return m.it > 10
            end function}))
        end function})
        m._this_suite.test("groupBy", {it: it, invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 3, 4, 5, 6])
            groups = groupBy_rIterableAnyN_Function1AnyNAnyN_MapAnyNListAnyN_k_(list, {invoke: function(it as Integer) as Integer
                return m.it mod 2
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([2, 4, 6]), groups[0])
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([1, 3, 5]), groups[1])
        end function})
        m._this_suite.test("partition", {it: it, invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 3, 4, 5])
            <destruct> = partition_rIterableAnyN_Function1AnyNZ_PairListAnyNListAnyN_k_(list, {invoke: function(it as Integer) as Boolean
                return (m.it mod 2) = 0
            end function})
            evens = destruct.component1()
            odds = destruct.component2()
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([2, 4]), evens)
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([1, 3, 5]), odds)
        end function})
        m._this_suite.test("associate", {it: it, invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_(["a", "bb", "ccc"])
            map = associate_rIterableAnyN_Function1AnyNPairAnyNAnyN_MapAnyNAnyN_k_(list, {invoke: function(it as String) as Object
                return to_rAnyN_AnyN_PairAnyNAnyN_k_(m.it, m.it.length)
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(1, map["a"])
            assertEquals_AnyN_AnyN_StrN_k_(2, map["bb"])
            assertEquals_AnyN_AnyN_StrN_k_(3, map["ccc"])
        end function})
        m._this_suite.test("associateWith", {it: it, invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_(["a", "bb", "ccc"])
            map = associateWith_rIterableAnyN_Function1AnyNAnyN_MapAnyNAnyN_k_(list, {invoke: function(it as String) as Integer
                return m.it.length
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(1, map["a"])
            assertEquals_AnyN_AnyN_StrN_k_(2, map["bb"])
        end function})
        m._this_suite.test("associateBy", {it: it, invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_(["a", "bb", "ccc"])
            map = associateBy_rIterableAnyN_Function1AnyNAnyN_MapAnyNAnyN_k_(list, {invoke: function(it as String) as Integer
                return m.it.length
            end function})
            assertEquals_AnyN_AnyN_StrN_k_("a", map[1])
            assertEquals_AnyN_AnyN_StrN_k_("bb", map[2])
        end function})
        m._this_suite.test("toList", {invoke: function() as Void
            set = hashSetOf_Arr_HashSetAnyN_k_([1, 2, 3])
            list = toList_rIterableAnyN_ListAnyN_k_(set)
            assertEquals_AnyN_AnyN_StrN_k_(3, list.size)
        end function})
        m._this_suite.test("toSet", {invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 2, 3, 3, 3])
            set = toSet_rIterableAnyN_SetAnyN_k_(list)
            assertEquals_AnyN_AnyN_StrN_k_(3, set.size)
        end function})
        m._this_suite.test("joinToString", {invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 3])
            assertEquals_AnyN_AnyN_StrN_k_("1, 2, 3", joinToString_rIterableAnyN_CharSequence_CharSequence_CharSequence_I_CharSequence_Str_k_(list))
            assertEquals_AnyN_AnyN_StrN_k_("1-2-3", joinToString_rIterableAnyN_CharSequence_CharSequence_CharSequence_I_CharSequence_Str_k_(list, "-"))
            assertEquals_AnyN_AnyN_StrN_k_("[1, 2, 3]", joinToString_rIterableAnyN_CharSequence_CharSequence_CharSequence_I_CharSequence_Str_k_(list, "[", "]"))
        end function})
        m._this_suite.test("forEach", {it: it, invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 3])
            collected = mutableListOf_MutableListAnyN_k_()
            forEach_rIterableAnyN_Function1AnyNV_k_(list, {collected: collected, invoke: function(it as Integer) as Void
                m.collected.add(it)
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([1, 2, 3]), collected)
        end function})
        m._this_suite.test("forEachIndexed", {i: i, v: v, invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_(["a", "b", "c"])
            collected = mutableListOf_MutableListAnyN_k_()
            forEachIndexed_rIterableAnyN_Function2IAnyNV_k_(list, {collected: collected, invoke: function(i as Integer, v as String) as Void
                m.collected.add((i + ":") + v)
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_(["0:a", "1:b", "2:c"]), collected)
        end function})
        m._this_suite.test("indexOf", {invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 3, 2])
            assertEquals_AnyN_AnyN_StrN_k_(1, list.indexOf(2))
            assertEquals_AnyN_AnyN_StrN_k_(-1, list.indexOf(5))
        end function})
        m._this_suite.test("indexOfFirst", {it: it, invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 3, 4])
            assertEquals_AnyN_AnyN_StrN_k_(2, indexOfFirst_rIterableAnyN_Function1AnyNZ_I_k_(list, {invoke: function(it as Integer) as Boolean
                return m.it > 2
            end function}))
        end function})
        m._this_suite.test("indexOfLast", {it: it, invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 3, 4])
            assertEquals_AnyN_AnyN_StrN_k_(3, indexOfLast_rIterableAnyN_Function1AnyNZ_I_k_(list, {invoke: function(it as Integer) as Boolean
                return m.it > 2
            end function}))
        end function})
        m._this_suite.test("distinct", {invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 2, 3, 3, 3])
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([1, 2, 3]), distinct_rIterableAnyN_ListAnyN_k_(list))
        end function})
        m._this_suite.test("distinctBy", {it: it, invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_(["a", "bb", "c", "dd"])
            result = distinctBy_rIterableAnyN_Function1AnyNAnyN_ListAnyN_k_(list, {invoke: function(it as String) as Integer
                return m.it.length
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(2, result.size)
        end function})
        m._this_suite.test("take", {invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 3, 4, 5])
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([1, 2, 3]), take_rIterableAnyN_I_ListAnyN_k_(list, 3))
        end function})
        m._this_suite.test("drop", {invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 3, 4, 5])
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([3, 4, 5]), drop_rIterableAnyN_I_ListAnyN_k_(list, 2))
        end function})
        m._this_suite.test("takeWhile", {it: it, invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 3, 4, 5])
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([1, 2]), takeWhile_rIterableAnyN_Function1AnyNZ_ListAnyN_k_(list, {invoke: function(it as Integer) as Boolean
                return m.it < 3
            end function}))
        end function})
        m._this_suite.test("dropWhile", {it: it, invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 3, 4, 5])
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([3, 4, 5]), dropWhile_rIterableAnyN_Function1AnyNZ_ListAnyN_k_(list, {invoke: function(it as Integer) as Boolean
                return m.it < 3
            end function}))
        end function})
        m._this_suite.test("reversed", {invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([1, 2, 3])
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([3, 2, 1]), reversed_rIterableAnyN_ListAnyN_k_(list))
        end function})
        m._this_suite.test("zip", {invoke: function() as Void
            a = listOf_Arr_ListAnyN_k_([1, 2, 3])
            b = listOf_Arr_ListAnyN_k_(["a", "b", "c"])
            zipped = zip_rIterableAnyN_IterableAnyN_ListPairAnyNAnyN_k_(a, b)
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([to_rAnyN_AnyN_PairAnyNAnyN_k_(1, "a"), to_rAnyN_AnyN_PairAnyNAnyN_k_(2, "b"), to_rAnyN_AnyN_PairAnyNAnyN_k_(3, "c")]), zipped)
        end function})
    end function})
end sub
