sub sortingTests_rTestRunner_k_(m as Object)
    m.suite("Sorting", {_this_suite: _this_suite, it: it, it: it, it: it, it: it, name: name, this: this, age: age, this: this, this: this, this: this, this: this, name: name, age: age, this: this, this: this, this: this, other: other, it: it, name: name, this: this, age: age, this: this, this: this, this: this, this: this, name: name, age: age, this: this, this: this, this: this, other: other, it: it, invoke: function() as Void
        m._this_suite.test("sort integers", {invoke: function() as Void
            list = mutableListOf_Arr_MutableListAnyN_k_([5, 2, 8, 1, 9, 3])
            sort_rMutableListAny_k_(list)
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([1, 2, 3, 5, 8, 9]), list)
        end function})
        m._this_suite.test("sort strings", {invoke: function() as Void
            list = mutableListOf_Arr_MutableListAnyN_k_(["dog", "cat", "apple", "zebra"])
            sort_rMutableListAny_k_(list)
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_(["apple", "cat", "dog", "zebra"]), list)
        end function})
        m._this_suite.test("sort with comparator", {invoke: function() as Void
            list = mutableListOf_Arr_MutableListAnyN_k_([5, 2, 8, 1, 9, 3])
            sortWith_rMutableListAnyN_ComparatorAnyN_k_(list, reverseOrder_ComparatorAny_k_())
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([9, 8, 5, 3, 2, 1]), list)
        end function})
        m._this_suite.test("sort descending", {invoke: function() as Void
            list = mutableListOf_Arr_MutableListAnyN_k_([5, 2, 8, 1, 9, 3])
            sortDescending_rMutableListAny_k_(list)
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([9, 8, 5, 3, 2, 1]), list)
        end function})
        m._this_suite.test("sortBy", {it: it, invoke: function() as Void
            list = mutableListOf_Arr_MutableListAnyN_k_(["apple", "pie", "a", "zoo"])
            sortBy_rMutableListAnyN_Function1AnyNAnyN_k_(list, {invoke: function(it as String) as Dynamic
                return m.it.length
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_(["a", "pie", "zoo", "apple"]), list)
        end function})
        m._this_suite.test("sortByDescending", {it: it, invoke: function() as Void
            list = mutableListOf_Arr_MutableListAnyN_k_(["apple", "pie", "a", "zoo"])
            sortByDescending_rMutableListAnyN_Function1AnyNAnyN_k_(list, {invoke: function(it as String) as Dynamic
                return m.it.length
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_(["apple", "pie", "zoo", "a"]), list)
        end function})
        m._this_suite.test("sorted (immutable)", {invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([5, 2, 8, 1, 9, 3])
            sorted = sorted_rIterableAny_ListAny_k_(list)
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([5, 2, 8, 1, 9, 3]), list)
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([1, 2, 3, 5, 8, 9]), sorted)
        end function})
        m._this_suite.test("sortedWith", {invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([5, 2, 8, 1, 9, 3])
            sorted = sortedWith_rIterableAnyN_ComparatorAnyN_ListAnyN_k_(list, reverseOrder_ComparatorAny_k_())
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([9, 8, 5, 3, 2, 1]), sorted)
        end function})
        m._this_suite.test("sortedDescending", {invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_([5, 2, 8, 1, 9, 3])
            sorted = sortedDescending_rIterableAny_ListAny_k_(list)
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([9, 8, 5, 3, 2, 1]), sorted)
        end function})
        m._this_suite.test("sortedBy", {it: it, invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_(["apple", "pie", "a", "zoo"])
            sorted = sortedBy_rIterableAnyN_Function1AnyNAnyN_ListAnyN_k_(list, {invoke: function(it as String) as Dynamic
                return m.it.length
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_(["a", "pie", "zoo", "apple"]), sorted)
        end function})
        m._this_suite.test("sortedByDescending", {it: it, invoke: function() as Void
            list = listOf_Arr_ListAnyN_k_(["apple", "pie", "a", "zoo"])
            sorted = sortedByDescending_rIterableAnyN_Function1AnyNAnyN_ListAnyN_k_(list, {invoke: function(it as String) as Dynamic
                return m.it.length
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_(["apple", "pie", "zoo", "a"]), sorted)
        end function})
        m._this_suite.test("sort stability", {name: name, this: this, age: age, this: this, this: this, this: this, this: this, name: name, age: age, this: this, this: this, this: this, other: other, it: it, invoke: function() as Void
            list = mutableListOf_Arr_MutableListAnyN_k_([Person_create_Str_I_Person_k_("Alice", 30), Person_create_Str_I_Person_k_("Bob", 25), Person_create_Str_I_Person_k_("Charlie", 30), Person_create_Str_I_Person_k_("David", 25)])
            sortBy_rMutableListAnyN_Function1AnyNAnyN_k_(list, {invoke: function(it as Object) as Dynamic
                return m.it.age
            end function})
            assertEquals_AnyN_AnyN_StrN_k_(25, list[0].age)
            assertEquals_AnyN_AnyN_StrN_k_("Bob", list[0].name)
            assertEquals_AnyN_AnyN_StrN_k_(25, list[1].age)
            assertEquals_AnyN_AnyN_StrN_k_("David", list[1].name)
            assertEquals_AnyN_AnyN_StrN_k_(30, list[2].age)
            assertEquals_AnyN_AnyN_StrN_k_("Alice", list[2].name)
            assertEquals_AnyN_AnyN_StrN_k_(30, list[3].age)
            assertEquals_AnyN_AnyN_StrN_k_("Charlie", list[3].name)
        end function})
        m._this_suite.test("sort empty list", {invoke: function() as Void
            list = mutableListOf_MutableListAnyN_k_()
            sort_rMutableListAny_k_(list)
            assertEquals_AnyN_AnyN_StrN_k_(emptyList_ListAnyN_k_(), list)
        end function})
        m._this_suite.test("sort single element", {invoke: function() as Void
            list = mutableListOf_Arr_MutableListAnyN_k_([42])
            sort_rMutableListAny_k_(list)
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([42]), list)
        end function})
        m._this_suite.test("sort large list", {invoke: function() as Void
            list = mutableListOf_MutableListAnyN_k_()
            inductionVariable = 100
            if lessOrEqual_I_I_Z_k_(1, inductionVariable) then
                                i = inductionVariable
                inductionVariable = (inductionVariable + -1)

                list.add(i)


                while lessOrEqual_I_I_Z_k_(1, inductionVariable)
                    i = inductionVariable
                    inductionVariable = (inductionVariable + -1)

                    list.add(i)

                end while

            end if

            sort_rMutableListAny_k_(list)
            progression = until_rI_I_IntRange_k_(0, 100)
            inductionVariable = progression.first
            last = progression.last
            if lessOrEqual_I_I_Z_k_(inductionVariable, last) then
                                i = inductionVariable
                inductionVariable = (inductionVariable + 1)

                assertEquals_AnyN_AnyN_StrN_k_(i + 1, list[i])


                while i <> last
                    i = inductionVariable
                    inductionVariable = (inductionVariable + 1)

                    assertEquals_AnyN_AnyN_StrN_k_(i + 1, list[i])

                end while

            end if

        end function})
        m._this_suite.test("compareBy", {name: name, this: this, age: age, this: this, this: this, this: this, this: this, name: name, age: age, this: this, this: this, this: this, other: other, it: it, invoke: function() as Void
            list = mutableListOf_Arr_MutableListAnyN_k_([Person_create_Str_I_Person_k_("Alice", 30), Person_create_Str_I_Person_k_("Bob", 25), Person_create_Str_I_Person_k_("Charlie", 30)])
            sortWith_rMutableListAnyN_ComparatorAnyN_k_(list, compareBy_Function1AnyNComparableStarN_ComparatorAnyN_k_({invoke: function(it as Object) as Dynamic
                return m.it.age
            end function}))
            assertEquals_AnyN_AnyN_StrN_k_("Bob", list[0].name)
            assertEquals_AnyN_AnyN_StrN_k_("Alice", list[1].name)
            assertEquals_AnyN_AnyN_StrN_k_("Charlie", list[2].name)
        end function})
        m._this_suite.test("comparator reversed", {invoke: function() as Void
            list = mutableListOf_Arr_MutableListAnyN_k_([5, 2, 8, 1, 9, 3])
            sortWith_rMutableListAnyN_ComparatorAnyN_k_(list, reversed_rComparatorAnyN_ComparatorAnyN_k_(naturalOrder_ComparatorAny_k_()))
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([9, 8, 5, 3, 2, 1]), list)
        end function})
        m._this_suite.test("compareValues", {invoke: function() as Void
            assertEquals_AnyN_AnyN_StrN_k_(0, compareValues_AnyN_AnyN_I_k_(5, 5))
            assertTrue_Z_StrN_k_(compareValues_AnyN_AnyN_I_k_(3, 5) < 0)
            assertTrue_Z_StrN_k_(compareValues_AnyN_AnyN_I_k_(5, 3) > 0)
            assertTrue_Z_StrN_k_(compareValues_AnyN_AnyN_I_k_(invalid, 5) < 0)
            assertTrue_Z_StrN_k_(compareValues_AnyN_AnyN_I_k_(5, invalid) > 0)
            assertEquals_AnyN_AnyN_StrN_k_(0, compareValues_AnyN_AnyN_I_k_(invalid, invalid))
        end function})
    end function})
end sub
