sub hashSetTests_rTestRunner_k_(m as Object)
    m.suite("HashSet", {_this_suite: _this_suite, invoke: function() as Void
        m._this_suite.test("create empty", {invoke: function() as Void
            set = HashSet_create_HashSetAnyN_k_()
            assertTrue_Z_StrN_k_(set.isEmpty())
            assertEquals_AnyN_AnyN_StrN_k_(0, set.size)
        end function})
        m._this_suite.test("create from collection", {invoke: function() as Void
            set = HashSet_create_CollectionAnyN_HashSetAnyN_k_(listOf_Arr_ListAnyN_k_([1, 2, 3, 2, 1]))
            assertEquals_AnyN_AnyN_StrN_k_(3, set.size)
            assertTrue_Z_StrN_k_(set.contains(1))
            assertTrue_Z_StrN_k_(set.contains(2))
            assertTrue_Z_StrN_k_(set.contains(3))
        end function})
        m._this_suite.test("add elements", {invoke: function() as Void
            set = HashSet_create_HashSetAnyN_k_()
            assertTrue_Z_StrN_k_(set.add(1))
            assertTrue_Z_StrN_k_(set.add(2))
            assertFalse_Z_StrN_k_(set.add(1))
            assertEquals_AnyN_AnyN_StrN_k_(2, set.size)
        end function})
        m._this_suite.test("contains", {invoke: function() as Void
            set = hashSetOf_Arr_HashSetAnyN_k_([1, 2, 3])
            assertTrue_Z_StrN_k_(set.contains(2))
            assertFalse_Z_StrN_k_(set.contains(5))
        end function})
        m._this_suite.test("remove", {invoke: function() as Void
            set = hashSetOf_Arr_HashSetAnyN_k_([1, 2, 3])
            assertTrue_Z_StrN_k_(set.remove(2))
            assertFalse_Z_StrN_k_(set.remove(5))
            assertEquals_AnyN_AnyN_StrN_k_(2, set.size)
            assertFalse_Z_StrN_k_(set.contains(2))
        end function})
        m._this_suite.test("clear", {invoke: function() as Void
            set = hashSetOf_Arr_HashSetAnyN_k_([1, 2, 3])
            set.clear()
            assertTrue_Z_StrN_k_(set.isEmpty())
            assertEquals_AnyN_AnyN_StrN_k_(0, set.size)
        end function})
        m._this_suite.test("addAll", {invoke: function() as Void
            set = hashSetOf_Arr_HashSetAnyN_k_([1, 2])
            assertTrue_Z_StrN_k_(set.addAll(listOf_Arr_ListAnyN_k_([3, 4, 2])))
            assertEquals_AnyN_AnyN_StrN_k_(4, set.size)
        end function})
        m._this_suite.test("removeAll", {invoke: function() as Void
            set = hashSetOf_Arr_HashSetAnyN_k_([1, 2, 3, 4, 5])
            assertTrue_Z_StrN_k_(set.removeAll(listOf_Arr_ListAnyN_k_([2, 4])))
            assertEquals_AnyN_AnyN_StrN_k_(3, set.size)
            assertFalse_Z_StrN_k_(set.contains(2))
            assertFalse_Z_StrN_k_(set.contains(4))
        end function})
        m._this_suite.test("retainAll", {invoke: function() as Void
            set = hashSetOf_Arr_HashSetAnyN_k_([1, 2, 3, 4, 5])
            assertTrue_Z_StrN_k_(set.retainAll(listOf_Arr_ListAnyN_k_([2, 3, 6])))
            assertEquals_AnyN_AnyN_StrN_k_(2, set.size)
            assertTrue_Z_StrN_k_(set.contains(2))
            assertTrue_Z_StrN_k_(set.contains(3))
        end function})
        m._this_suite.test("containsAll", {invoke: function() as Void
            set = hashSetOf_Arr_HashSetAnyN_k_([1, 2, 3, 4, 5])
            assertTrue_Z_StrN_k_(set.containsAll(listOf_Arr_ListAnyN_k_([1, 3, 5])))
            assertFalse_Z_StrN_k_(set.containsAll(listOf_Arr_ListAnyN_k_([1, 6])))
        end function})
        m._this_suite.test("iteration", {invoke: function() as Void
            set = hashSetOf_Arr_HashSetAnyN_k_([1, 2, 3])
            collected = mutableListOf_MutableListAnyN_k_()
            for each item in set
                collected.add(item)

            end for
            assertEquals_AnyN_AnyN_StrN_k_(3, collected.size)
            assertTrue_Z_StrN_k_(collected.containsAll(listOf_Arr_ListAnyN_k_([1, 2, 3])))
        end function})
        m._this_suite.test("iterator remove", {invoke: function() as Void
            set = hashSetOf_Arr_HashSetAnyN_k_([1, 2, 3])
            iter = set.iterator()
            while iter.hasNext()
                if iter.next() = 2 then
                    iter.remove()
                end if
            end while
            assertEquals_AnyN_AnyN_StrN_k_(2, set.size)
            assertFalse_Z_StrN_k_(set.contains(2))
        end function})
        m._this_suite.test("equals", {invoke: function() as Void
            set1 = hashSetOf_Arr_HashSetAnyN_k_([1, 2, 3])
            set2 = hashSetOf_Arr_HashSetAnyN_k_([1, 2, 3])
            set3 = hashSetOf_Arr_HashSetAnyN_k_([1, 2, 4])
            assertEquals_AnyN_AnyN_StrN_k_(set1, set2)
            assertNotEquals_AnyN_AnyN_StrN_k_(set1, set3)
        end function})
        m._this_suite.test("toString", {invoke: function() as Void
            set = hashSetOf_Arr_HashSetAnyN_k_([1, 2, 3])
            str = set.toString()
            assertTrue_Z_StrN_k_(startsWith_rStr_Str_Z_Z_k_(str, "["))
            assertTrue_Z_StrN_k_(endsWith_rStr_Str_Z_Z_k_(str, "]"))
        end function})
        m._this_suite.test("string set", {invoke: function() as Void
            set = hashSetOf_Arr_HashSetAnyN_k_(["apple", "banana", "cherry"])
            assertTrue_Z_StrN_k_(set.contains("banana"))
            assertFalse_Z_StrN_k_(set.contains("durian"))
        end function})
    end function})
end sub
