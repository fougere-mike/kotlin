sub hashSetTests_rTestRunner_k_(m as Object)
    m.suite_Str_Function1TestRunnerV_k_("HashSet", {invoke: function(m as Object) as Void
        m.test_Str_Function0V_k_("create empty", {invoke: function() as Void
            set = HashSet_create_k_()
            assertTrue_Z_StrN_k_(set.isEmpty_k_(), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(0, set.get_size(), invalid)
        end function})
        m.test_Str_Function0V_k_("create from collection", {invoke: function() as Void
            set = HashSet_create_Collection_k_(listOf_Arr_k_([1, 2, 3, 2, 1]))
            assertEquals_AnyN_AnyN_StrN_k_(3, set.get_size(), invalid)
            assertTrue_Z_StrN_k_(set.contains_AnyN_k_(1), invalid)
            assertTrue_Z_StrN_k_(set.contains_AnyN_k_(2), invalid)
            assertTrue_Z_StrN_k_(set.contains_AnyN_k_(3), invalid)
        end function})
        m.test_Str_Function0V_k_("add elements", {invoke: function() as Void
            set = HashSet_create_k_()
            assertTrue_Z_StrN_k_(set.add_AnyN_k_(1), invalid)
            assertTrue_Z_StrN_k_(set.add_AnyN_k_(2), invalid)
            assertFalse_Z_StrN_k_(set.add_AnyN_k_(1), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(2, set.get_size(), invalid)
        end function})
        m.test_Str_Function0V_k_("contains", {invoke: function() as Void
            set = hashSetOf_Arr_k_([1, 2, 3])
            assertTrue_Z_StrN_k_(set.contains_AnyN_k_(2), invalid)
            assertFalse_Z_StrN_k_(set.contains_AnyN_k_(5), invalid)
        end function})
        m.test_Str_Function0V_k_("remove", {invoke: function() as Void
            set = hashSetOf_Arr_k_([1, 2, 3])
            assertTrue_Z_StrN_k_(set.remove_AnyN_k_(2), invalid)
            assertFalse_Z_StrN_k_(set.remove_AnyN_k_(5), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(2, set.get_size(), invalid)
            assertFalse_Z_StrN_k_(set.contains_AnyN_k_(2), invalid)
        end function})
        m.test_Str_Function0V_k_("clear", {invoke: function() as Void
            set = hashSetOf_Arr_k_([1, 2, 3])
            set.clear_k_()
            assertTrue_Z_StrN_k_(set.isEmpty_k_(), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(0, set.get_size(), invalid)
        end function})
        m.test_Str_Function0V_k_("addAll", {invoke: function() as Void
            set = hashSetOf_Arr_k_([1, 2])
            assertTrue_Z_StrN_k_(set.addAll_Collection_k_(listOf_Arr_k_([3, 4, 2])), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(4, set.get_size(), invalid)
        end function})
        m.test_Str_Function0V_k_("removeAll", {invoke: function() as Void
            set = hashSetOf_Arr_k_([1, 2, 3, 4, 5])
            assertTrue_Z_StrN_k_(set.removeAll_Collection_k_(listOf_Arr_k_([2, 4])), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(3, set.get_size(), invalid)
            assertFalse_Z_StrN_k_(set.contains_AnyN_k_(2), invalid)
            assertFalse_Z_StrN_k_(set.contains_AnyN_k_(4), invalid)
        end function})
        m.test_Str_Function0V_k_("retainAll", {invoke: function() as Void
            set = hashSetOf_Arr_k_([1, 2, 3, 4, 5])
            assertTrue_Z_StrN_k_(set.retainAll_Collection_k_(listOf_Arr_k_([2, 3, 6])), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(2, set.get_size(), invalid)
            assertTrue_Z_StrN_k_(set.contains_AnyN_k_(2), invalid)
            assertTrue_Z_StrN_k_(set.contains_AnyN_k_(3), invalid)
        end function})
        m.test_Str_Function0V_k_("containsAll", {invoke: function() as Void
            set = hashSetOf_Arr_k_([1, 2, 3, 4, 5])
            assertTrue_Z_StrN_k_(set.containsAll_Collection_k_(listOf_Arr_k_([1, 3, 5])), invalid)
            assertFalse_Z_StrN_k_(set.containsAll_Collection_k_(listOf_Arr_k_([1, 6])), invalid)
        end function})
        m.test_Str_Function0V_k_("iteration", {invoke: function() as Void
            set = hashSetOf_Arr_k_([1, 2, 3])
            collected = mutableListOf_k_()
            __iter_1 = set.iterator_k_()
            while __iter_1.hasNext_k_()
                item = __iter_1.next_k_()
                collected.add_AnyN_k_(item)

            end while

            assertEquals_AnyN_AnyN_StrN_k_(3, collected.get_size(), invalid)
            assertTrue_Z_StrN_k_(collected.containsAll_Collection_k_(listOf_Arr_k_([1, 2, 3])), invalid)
        end function})
        m.test_Str_Function0V_k_("iterator remove", {invoke: function() as Void
            set = hashSetOf_Arr_k_([1, 2, 3])
            iter = set.iterator_k_()
            while iter.hasNext_k_()
                if iter.next_k_() = 2 then
                    iter.remove_k_()
                end if
            end while
            assertEquals_AnyN_AnyN_StrN_k_(2, set.get_size(), invalid)
            assertFalse_Z_StrN_k_(set.contains_AnyN_k_(2), invalid)
        end function})
        m.test_Str_Function0V_k_("equals", {invoke: function() as Void
            set1 = hashSetOf_Arr_k_([1, 2, 3])
            set2 = hashSetOf_Arr_k_([1, 2, 3])
            set3 = hashSetOf_Arr_k_([1, 2, 4])
            assertEquals_AnyN_AnyN_StrN_k_(set1, set2, invalid)
            assertNotEquals_AnyN_AnyN_StrN_k_(set1, set3, invalid)
        end function})
        m.test_Str_Function0V_k_("toString", {invoke: function() as Void
            set = hashSetOf_Arr_k_([1, 2, 3])
            str = set.toString()
            assertTrue_Z_StrN_k_(startsWith_rStr_Str_Z_k_(str, "[", invalid), invalid)
            assertTrue_Z_StrN_k_(endsWith_rStr_Str_Z_k_(str, "]", invalid), invalid)
        end function})
        m.test_Str_Function0V_k_("string set", {invoke: function() as Void
            set = hashSetOf_Arr_k_(["apple", "banana", "cherry"])
            assertTrue_Z_StrN_k_(set.contains_AnyN_k_("banana"), invalid)
            assertFalse_Z_StrN_k_(set.contains_AnyN_k_("durian"), invalid)
        end function})
    end function})
end sub
