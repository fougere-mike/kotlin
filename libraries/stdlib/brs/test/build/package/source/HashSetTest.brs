sub hashSetTests_rTestRunner_k_(m as Object)
    m.suite_Str_Function1TestRunnerV_k_("HashSet", {invoke: function(m as Object) as Void
        m.test_Str_Function0V_k_("create empty", {invoke: function() as Void
            set = HashSet_create_HashSetAnyN_k_()
            assertTrue_Z_StrN_k_(set.isEmpty_Z_k_())
            assertEquals_AnyN_AnyN_StrN_k_(0, set.get_size())
        end function})
        m.test_Str_Function0V_k_("create from collection", {invoke: function() as Void
            set = HashSet_create_CollectionAnyN_HashSetAnyN_k_(listOf_Arr_ListAnyN_k_([1, 2, 3, 2, 1]))
            assertEquals_AnyN_AnyN_StrN_k_(3, set.get_size())
            assertTrue_Z_StrN_k_(set.contains_AnyN_Z_k_(1))
            assertTrue_Z_StrN_k_(set.contains_AnyN_Z_k_(2))
            assertTrue_Z_StrN_k_(set.contains_AnyN_Z_k_(3))
        end function})
        m.test_Str_Function0V_k_("add elements", {invoke: function() as Void
            set = HashSet_create_HashSetAnyN_k_()
            assertTrue_Z_StrN_k_(set.add_AnyN_Z_k_(1))
            assertTrue_Z_StrN_k_(set.add_AnyN_Z_k_(2))
            assertFalse_Z_StrN_k_(set.add_AnyN_Z_k_(1))
            assertEquals_AnyN_AnyN_StrN_k_(2, set.get_size())
        end function})
        m.test_Str_Function0V_k_("contains", {invoke: function() as Void
            set = hashSetOf_Arr_HashSetAnyN_k_([1, 2, 3])
            assertTrue_Z_StrN_k_(set.contains_AnyN_Z_k_(2))
            assertFalse_Z_StrN_k_(set.contains_AnyN_Z_k_(5))
        end function})
        m.test_Str_Function0V_k_("remove", {invoke: function() as Void
            set = hashSetOf_Arr_HashSetAnyN_k_([1, 2, 3])
            assertTrue_Z_StrN_k_(set.remove_AnyN_Z_k_(2))
            assertFalse_Z_StrN_k_(set.remove_AnyN_Z_k_(5))
            assertEquals_AnyN_AnyN_StrN_k_(2, set.get_size())
            assertFalse_Z_StrN_k_(set.contains_AnyN_Z_k_(2))
        end function})
        m.test_Str_Function0V_k_("clear", {invoke: function() as Void
            set = hashSetOf_Arr_HashSetAnyN_k_([1, 2, 3])
            set.clear()
            assertTrue_Z_StrN_k_(set.isEmpty_Z_k_())
            assertEquals_AnyN_AnyN_StrN_k_(0, set.get_size())
        end function})
        m.test_Str_Function0V_k_("addAll", {invoke: function() as Void
            set = hashSetOf_Arr_HashSetAnyN_k_([1, 2])
            assertTrue_Z_StrN_k_(set.addAll_CollectionAnyN_Z_k_(listOf_Arr_ListAnyN_k_([3, 4, 2])))
            assertEquals_AnyN_AnyN_StrN_k_(4, set.get_size())
        end function})
        m.test_Str_Function0V_k_("removeAll", {invoke: function() as Void
            set = hashSetOf_Arr_HashSetAnyN_k_([1, 2, 3, 4, 5])
            assertTrue_Z_StrN_k_(set.removeAll_CollectionAnyN_Z_k_(listOf_Arr_ListAnyN_k_([2, 4])))
            assertEquals_AnyN_AnyN_StrN_k_(3, set.get_size())
            assertFalse_Z_StrN_k_(set.contains_AnyN_Z_k_(2))
            assertFalse_Z_StrN_k_(set.contains_AnyN_Z_k_(4))
        end function})
        m.test_Str_Function0V_k_("retainAll", {invoke: function() as Void
            set = hashSetOf_Arr_HashSetAnyN_k_([1, 2, 3, 4, 5])
            assertTrue_Z_StrN_k_(set.retainAll_CollectionAnyN_Z_k_(listOf_Arr_ListAnyN_k_([2, 3, 6])))
            assertEquals_AnyN_AnyN_StrN_k_(2, set.get_size())
            assertTrue_Z_StrN_k_(set.contains_AnyN_Z_k_(2))
            assertTrue_Z_StrN_k_(set.contains_AnyN_Z_k_(3))
        end function})
        m.test_Str_Function0V_k_("containsAll", {invoke: function() as Void
            set = hashSetOf_Arr_HashSetAnyN_k_([1, 2, 3, 4, 5])
            assertTrue_Z_StrN_k_(set.containsAll_CollectionAnyN_Z_k_(listOf_Arr_ListAnyN_k_([1, 3, 5])))
            assertFalse_Z_StrN_k_(set.containsAll_CollectionAnyN_Z_k_(listOf_Arr_ListAnyN_k_([1, 6])))
        end function})
        m.test_Str_Function0V_k_("iteration", {invoke: function() as Void
            set = hashSetOf_Arr_HashSetAnyN_k_([1, 2, 3])
            collected = mutableListOf_MutableListAnyN_k_()
            for each item in set
                collected.add_AnyN_Z_k_(item)

            end for
            assertEquals_AnyN_AnyN_StrN_k_(3, collected.get_size())
            assertTrue_Z_StrN_k_(collected.containsAll_CollectionAnyN_Z_k_(listOf_Arr_ListAnyN_k_([1, 2, 3])))
        end function})
        m.test_Str_Function0V_k_("iterator remove", {invoke: function() as Void
            set = hashSetOf_Arr_HashSetAnyN_k_([1, 2, 3])
            iter = set.iterator_MutableIteratorAnyN_k_()
            while iter.hasNext_Z_k_()
                if iter.next_AnyN_k_() = 2 then
                    iter.remove()
                end if
            end while
            assertEquals_AnyN_AnyN_StrN_k_(2, set.get_size())
            assertFalse_Z_StrN_k_(set.contains_AnyN_Z_k_(2))
        end function})
        m.test_Str_Function0V_k_("equals", {invoke: function() as Void
            set1 = hashSetOf_Arr_HashSetAnyN_k_([1, 2, 3])
            set2 = hashSetOf_Arr_HashSetAnyN_k_([1, 2, 3])
            set3 = hashSetOf_Arr_HashSetAnyN_k_([1, 2, 4])
            assertEquals_AnyN_AnyN_StrN_k_(set1, set2)
            assertNotEquals_AnyN_AnyN_StrN_k_(set1, set3)
        end function})
        m.test_Str_Function0V_k_("toString", {invoke: function() as Void
            set = hashSetOf_Arr_HashSetAnyN_k_([1, 2, 3])
            str = set.toString()
            assertTrue_Z_StrN_k_(startsWith_rStr_Str_Z_Z_k_(str, "["))
            assertTrue_Z_StrN_k_(endsWith_rStr_Str_Z_Z_k_(str, "]"))
        end function})
        m.test_Str_Function0V_k_("string set", {invoke: function() as Void
            set = hashSetOf_Arr_HashSetAnyN_k_(["apple", "banana", "cherry"])
            assertTrue_Z_StrN_k_(set.contains_AnyN_Z_k_("banana"))
            assertFalse_Z_StrN_k_(set.contains_AnyN_Z_k_("durian"))
        end function})
    end function})
end sub
