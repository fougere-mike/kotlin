sub sortingTests_rTestRunner_k_(m as Object)
    m.suite_Str_Function1TestRunnerV_k_("Sorting", {invoke: function(m as Object) as Void
        m.test_Str_Function0V_k_("sort integers", {invoke: function() as Void
            list = mutableListOf_Arr_k_([5, 2, 8, 1, 9, 3])
            sort_rMutableList_k_(list)
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([1, 2, 3, 5, 8, 9]), list, invalid)
        end function})
        m.test_Str_Function0V_k_("sort strings", {invoke: function() as Void
            list = mutableListOf_Arr_k_(["dog", "cat", "apple", "zebra"])
            sort_rMutableList_k_(list)
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_(["apple", "cat", "dog", "zebra"]), list, invalid)
        end function})
        m.test_Str_Function0V_k_("sort with comparator", {invoke: function() as Void
            list = mutableListOf_Arr_k_([5, 2, 8, 1, 9, 3])
            sortWith_rMutableList_Comparator_k_(list, reverseOrder_k_())
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([9, 8, 5, 3, 2, 1]), list, invalid)
        end function})
        m.test_Str_Function0V_k_("sort descending", {invoke: function() as Void
            list = mutableListOf_Arr_k_([5, 2, 8, 1, 9, 3])
            sortDescending_rMutableList_k_(list)
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([9, 8, 5, 3, 2, 1]), list, invalid)
        end function})
        m.test_Str_Function0V_k_("sortBy", {invoke: function() as Void
            list = mutableListOf_Arr_k_(["apple", "pie", "a", "zoo"])
            sortBy_rMutableList_Function1_k_(list, {invoke: function(it as String) as Dynamic
                return Len(it)
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_(["a", "pie", "zoo", "apple"]), list, invalid)
        end function})
        m.test_Str_Function0V_k_("sortByDescending", {invoke: function() as Void
            list = mutableListOf_Arr_k_(["apple", "pie", "a", "zoo"])
            sortByDescending_rMutableList_Function1_k_(list, {invoke: function(it as String) as Dynamic
                return Len(it)
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_(["apple", "pie", "zoo", "a"]), list, invalid)
        end function})
        m.test_Str_Function0V_k_("sorted (immutable)", {invoke: function() as Void
            list = listOf_Arr_k_([5, 2, 8, 1, 9, 3])
            sorted = sorted_rIterable_k_(list)
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([5, 2, 8, 1, 9, 3]), list, invalid)
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([1, 2, 3, 5, 8, 9]), sorted, invalid)
        end function})
        m.test_Str_Function0V_k_("sortedWith", {invoke: function() as Void
            list = listOf_Arr_k_([5, 2, 8, 1, 9, 3])
            sorted = sortedWith_rIterable_Comparator_k_(list, reverseOrder_k_())
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([9, 8, 5, 3, 2, 1]), sorted, invalid)
        end function})
        m.test_Str_Function0V_k_("sortedDescending", {invoke: function() as Void
            list = listOf_Arr_k_([5, 2, 8, 1, 9, 3])
            sorted = sortedDescending_rIterable_k_(list)
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([9, 8, 5, 3, 2, 1]), sorted, invalid)
        end function})
        m.test_Str_Function0V_k_("sortedBy", {invoke: function() as Void
            list = listOf_Arr_k_(["apple", "pie", "a", "zoo"])
            sorted = sortedBy_rIterable_Function1_k_(list, {invoke: function(it as String) as Dynamic
                return Len(it)
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_(["a", "pie", "zoo", "apple"]), sorted, invalid)
        end function})
        m.test_Str_Function0V_k_("sortedByDescending", {invoke: function() as Void
            list = listOf_Arr_k_(["apple", "pie", "a", "zoo"])
            sorted = sortedByDescending_rIterable_Function1_k_(list, {invoke: function(it as String) as Dynamic
                return Len(it)
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_(["apple", "pie", "zoo", "a"]), sorted, invalid)
        end function})
        m.test_Str_Function0V_k_("sort empty list", {invoke: function() as Void
            list = mutableListOf_k_()
            sort_rMutableList_k_(list)
            assertEquals_AnyN_AnyN_StrN_k_(emptyList_k_(), list, invalid)
        end function})
        m.test_Str_Function0V_k_("sort single element", {invoke: function() as Void
            list = mutableListOf_Arr_k_([42])
            sort_rMutableList_k_(list)
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([42]), list, invalid)
        end function})
        m.test_Str_Function0V_k_("sort large list", {invoke: function() as Void
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
            inductionVariable = progression.get_first()
            last = progression.get_last()
            if inductionVariable <= last then
                                i = inductionVariable
                inductionVariable = (inductionVariable + 1)

                assertEquals_AnyN_AnyN_StrN_k_(i + 1, list.get_I_k_(i), invalid)


                while i <> last
                    i = inductionVariable
                    inductionVariable = (inductionVariable + 1)

                    assertEquals_AnyN_AnyN_StrN_k_(i + 1, list.get_I_k_(i), invalid)

                end while

            end if

        end function})
        m.test_Str_Function0V_k_("comparator reversed", {invoke: function() as Void
            list = mutableListOf_Arr_k_([5, 2, 8, 1, 9, 3])
            sortWith_rMutableList_Comparator_k_(list, reversed_rComparator_k_(naturalOrder_k_()))
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([9, 8, 5, 3, 2, 1]), list, invalid)
        end function})
        m.test_Str_Function0V_k_("compareValues", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_(0, compareValues_AnyN_AnyN_k_(5, 5), invalid)
            assertTrue_Z_StrN_k_(compareValues_AnyN_AnyN_k_(3, 5) < 0, invalid)
            assertTrue_Z_StrN_k_(compareValues_AnyN_AnyN_k_(5, 3) > 0, invalid)
            assertTrue_Z_StrN_k_(compareValues_AnyN_AnyN_k_(invalid, 5) < 0, invalid)
            assertTrue_Z_StrN_k_(compareValues_AnyN_AnyN_k_(5, invalid) > 0, invalid)
            assertEquals_AnyN_AnyN_StrN_k_(0, compareValues_AnyN_AnyN_k_(invalid, invalid), invalid)
        end function})
    end function})
end sub
