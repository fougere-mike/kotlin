sub sortingTests_rTestRunner_k_(m as Object)
    m.suite_Str_Function1TestRunnerV_k_("Sorting", {invoke: function(m as Object) as Void
        m.test_Str_Function0V_k_("sort integers", {invoke: function() as Void
            list = mutableListOf_Arr_MutableListAnyN_k_([5, 2, 8, 1, 9, 3])
            sort_rMutableListAny_k_(list)
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([1, 2, 3, 5, 8, 9]), list)
        end function})
        m.test_Str_Function0V_k_("sort strings", {invoke: function() as Void
            list = mutableListOf_Arr_MutableListAnyN_k_(["dog", "cat", "apple", "zebra"])
            sort_rMutableListAny_k_(list)
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_(["apple", "cat", "dog", "zebra"]), list)
        end function})
        m.test_Str_Function0V_k_("sort with comparator", {invoke: function() as Void
            list = mutableListOf_Arr_MutableListAnyN_k_([5, 2, 8, 1, 9, 3])
            sortWith_rMutableListAnyN_ComparatorAnyN_k_(list, reverseOrder_ComparatorAny_k_())
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([9, 8, 5, 3, 2, 1]), list)
        end function})
        m.test_Str_Function0V_k_("sort descending", {invoke: function() as Void
            list = mutableListOf_Arr_MutableListAnyN_k_([5, 2, 8, 1, 9, 3])
            sortDescending_rMutableListAny_k_(list)
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([9, 8, 5, 3, 2, 1]), list)
        end function})
        m.test_Str_Function0V_k_("sortBy", {invoke: function() as Void
            list = mutableListOf_Arr_MutableListAnyN_k_(["apple", "pie", "a", "zoo"])
            sortBy_rMutableListAnyN_Function1AnyNAnyN_k_(list, {invoke: function(it as String) as Dynamic
                return it.get_length()
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_(["a", "pie", "zoo", "apple"]), list)
        end function})
        m.test_Str_Function0V_k_("sortByDescending", {invoke: function() as Void
            list = mutableListOf_Arr_MutableListAnyN_k_(["apple", "pie", "a", "zoo"])
            sortByDescending_rMutableListAnyN_Function1AnyNAnyN_k_(list, {invoke: function(it as String) as Dynamic
                return it.get_length()
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_(["apple", "pie", "zoo", "a"]), list)
        end function})
        m.test_Str_Function0V_k_("sorted (immutable)", {invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([5, 2, 8, 1, 9, 3])
            sorted = sorted_rIterableAny_ListAny_k_(list)
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([5, 2, 8, 1, 9, 3]), list)
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([1, 2, 3, 5, 8, 9]), sorted)
        end function})
        m.test_Str_Function0V_k_("sortedWith", {invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([5, 2, 8, 1, 9, 3])
            sorted = sortedWith_rIterableAnyN_ComparatorAnyN_ListAnyN_k_(list, reverseOrder_ComparatorAny_k_())
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([9, 8, 5, 3, 2, 1]), sorted)
        end function})
        m.test_Str_Function0V_k_("sortedDescending", {invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([5, 2, 8, 1, 9, 3])
            sorted = sortedDescending_rIterableAny_ListAny_k_(list)
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([9, 8, 5, 3, 2, 1]), sorted)
        end function})
        m.test_Str_Function0V_k_("sortedBy", {invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_(["apple", "pie", "a", "zoo"])
            sorted = sortedBy_rIterableAnyN_Function1AnyNAnyN_ListAnyN_k_(list, {invoke: function(it as String) as Dynamic
                return it.get_length()
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_(["a", "pie", "zoo", "apple"]), sorted)
        end function})
        m.test_Str_Function0V_k_("sortedByDescending", {invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_(["apple", "pie", "a", "zoo"])
            sorted = sortedByDescending_rIterableAnyN_Function1AnyNAnyN_ListAnyN_k_(list, {invoke: function(it as String) as Dynamic
                return it.get_length()
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_(["apple", "pie", "zoo", "a"]), sorted)
        end function})
        m.test_Str_Function0V_k_("sort stability", {name: name, this: m, age: age, this: m, this: m, this: m, this: m, name: name, age: age, this: m, this: m, this: m, other: other, invoke: function() as Void
            list = mutableListOf_Arr_MutableListAnyN_k_([Person_create_Str_I_Person_k_("Alice", 30), Person_create_Str_I_Person_k_("Bob", 25), Person_create_Str_I_Person_k_("Charlie", 30), Person_create_Str_I_Person_k_("David", 25)])
            sortBy_rMutableListAnyN_Function1AnyNAnyN_k_(list, {invoke: function(it as Object) as Dynamic
                return it.age
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(25, list.get_I_AnyN_k_(0).age)
            assertEquals_AnyN_AnyN_StrN_k_("Bob", list.get_I_AnyN_k_(0).name)
            assertEquals_AnyN_AnyN_StrN_k_(25, list.get_I_AnyN_k_(1).age)
            assertEquals_AnyN_AnyN_StrN_k_("David", list.get_I_AnyN_k_(1).name)
            assertEquals_AnyN_AnyN_StrN_k_(30, list.get_I_AnyN_k_(2).age)
            assertEquals_AnyN_AnyN_StrN_k_("Alice", list.get_I_AnyN_k_(2).name)
            assertEquals_AnyN_AnyN_StrN_k_(30, list.get_I_AnyN_k_(3).age)
            assertEquals_AnyN_AnyN_StrN_k_("Charlie", list.get_I_AnyN_k_(3).name)
        end function})
        m.test_Str_Function0V_k_("sort empty list", {invoke: function() as Void
            list = mutableListOf_MutableListAnyN_k_()
            sort_rMutableListAny_k_(list)
            assertEquals_AnyN_AnyN_StrN_k_(emptyList_ListAnyN_k_(), list)
        end function})
        m.test_Str_Function0V_k_("sort single element", {invoke: function() as Void
            list = mutableListOf_Arr_MutableListAnyN_k_([42])
            sort_rMutableListAny_k_(list)
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([42]), list)
        end function})
        m.test_Str_Function0V_k_("sort large list", {invoke: function() as Void
            list = mutableListOf_MutableListAnyN_k_()
            inductionVariable = 100
            if 1 <= inductionVariable then
                                i = inductionVariable
                inductionVariable = (inductionVariable + -1)

                list.add_AnyN_Z_k_(i)


                while 1 <= inductionVariable
                    i = inductionVariable
                    inductionVariable = (inductionVariable + -1)

                    list.add_AnyN_Z_k_(i)

                end while

            end if

            sort_rMutableListAny_k_(list)
            progression = until_rI_I_IntRange_k_(0, 100)
            inductionVariable = progression.get_first()
            last = progression.get_last()
            if inductionVariable <= last then
                                i = inductionVariable
                inductionVariable = (inductionVariable + 1)

                assertEquals_AnyN_AnyN_StrN_k_(i + 1, list.get_I_AnyN_k_(i))


                while i <> last
                    i = inductionVariable
                    inductionVariable = (inductionVariable + 1)

                    assertEquals_AnyN_AnyN_StrN_k_(i + 1, list.get_I_AnyN_k_(i))

                end while

            end if

        end function})
        m.test_Str_Function0V_k_("compareBy", {name: name, this: m, age: age, this: m, this: m, this: m, this: m, name: name, age: age, this: m, this: m, this: m, other: other, invoke: function() as Void
            list = mutableListOf_Arr_MutableListAnyN_k_([Person_create_Str_I_Person_k_("Alice", 30), Person_create_Str_I_Person_k_("Bob", 25), Person_create_Str_I_Person_k_("Charlie", 30)])
            sortWith_rMutableListAnyN_ComparatorAnyN_k_(list, compareBy_Function1AnyNComparableStarN_ComparatorAnyN_k_({invoke: function(it as Object) as Dynamic
                return it.age
            end function}))
            assertEquals_AnyN_AnyN_StrN_k_("Bob", list.get_I_AnyN_k_(0).name)
            assertEquals_AnyN_AnyN_StrN_k_("Alice", list.get_I_AnyN_k_(1).name)
            assertEquals_AnyN_AnyN_StrN_k_("Charlie", list.get_I_AnyN_k_(2).name)
        end function})
        m.test_Str_Function0V_k_("comparator reversed", {invoke: function() as Void
            list = mutableListOf_Arr_MutableListAnyN_k_([5, 2, 8, 1, 9, 3])
            sortWith_rMutableListAnyN_ComparatorAnyN_k_(list, reversed_rComparatorAnyN_ComparatorAnyN_k_(naturalOrder_ComparatorAny_k_()))
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([9, 8, 5, 3, 2, 1]), list)
        end function})
        m.test_Str_Function0V_k_("compareValues", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_(0, compareValues_AnyN_AnyN_I_k_(5, 5))
            assertTrue_Z_StrN_k_(compareValues_AnyN_AnyN_I_k_(3, 5) < 0)
            assertTrue_Z_StrN_k_(compareValues_AnyN_AnyN_I_k_(5, 3) > 0)
            assertTrue_Z_StrN_k_(compareValues_AnyN_AnyN_I_k_(invalid, 5) < 0)
            assertTrue_Z_StrN_k_(compareValues_AnyN_AnyN_I_k_(5, invalid) > 0)
            assertEquals_AnyN_AnyN_StrN_k_(0, compareValues_AnyN_AnyN_I_k_(invalid, invalid))
        end function})
    end function})
end sub
