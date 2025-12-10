sub arrayListTests_rTestRunner_k_(m as Object)
    m.suite_Str_Function1TestRunnerV_k_("ArrayList", {invoke: function(m as Object) as Void
        m.test_Str_Function0V_k_("create empty", {invoke: function() as Void
            list = ArrayList_create_k_()
            assertTrue_Z_StrN_k_(list.isEmpty_k_(), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(0, list.get_size(), invalid)
        end function})
        m.test_Str_Function0V_k_("create with initial capacity", {invoke: function() as Void
            list = ArrayList_create_I_k_(10)
            assertTrue_Z_StrN_k_(list.isEmpty_k_(), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(0, list.get_size(), invalid)
        end function})
        m.test_Str_Function0V_k_("create from collection", {invoke: function() as Void
            list = ArrayList_create_Collection_k_(listOf_Arr_k_([1, 2, 3]))
            assertEquals_AnyN_AnyN_StrN_k_(3, list.get_size(), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(1, list.get_I_k_(0), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(2, list.get_I_k_(1), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(3, list.get_I_k_(2), invalid)
        end function})
        m.test_Str_Function0V_k_("add elements", {invoke: function() as Void
            list = ArrayList_create_k_()
            assertTrue_Z_StrN_k_(list.add_AnyN_k_(1), invalid)
            assertTrue_Z_StrN_k_(list.add_AnyN_k_(2), invalid)
            assertTrue_Z_StrN_k_(list.add_AnyN_k_(3), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(3, list.get_size(), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(1, list.get_I_k_(0), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(2, list.get_I_k_(1), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(3, list.get_I_k_(2), invalid)
        end function})
        m.test_Str_Function0V_k_("add at index", {invoke: function() as Void
            list = arrayListOf_Arr_k_([1, 3])
            list.add_I_AnyN_k_(1, 2)
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([1, 2, 3]), toList_rIterable_k_(list), invalid)
        end function})
        m.test_Str_Function0V_k_("get element", {invoke: function() as Void
            list = arrayListOf_Arr_k_([10, 20, 30])
            assertEquals_AnyN_AnyN_StrN_k_(10, list.get_I_k_(0), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(20, list.get_I_k_(1), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(30, list.get_I_k_(2), invalid)
        end function})
        m.test_Str_Function0V_k_("set element", {invoke: function() as Void
            list = arrayListOf_Arr_k_([1, 2, 3])
            old = list.set_I_AnyN_k_(1, 20)
            assertEquals_AnyN_AnyN_StrN_k_(2, old, invalid)
            assertEquals_AnyN_AnyN_StrN_k_(20, list.get_I_k_(1), invalid)
        end function})
        m.test_Str_Function0V_k_("remove at index", {invoke: function() as Void
            list = arrayListOf_Arr_k_([1, 2, 3])
            removed = list.removeAt_I_k_(1)
            assertEquals_AnyN_AnyN_StrN_k_(2, removed, invalid)
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([1, 3]), toList_rIterable_k_(list), invalid)
        end function})
        m.test_Str_Function0V_k_("remove element", {invoke: function() as Void
            list = arrayListOf_Arr_k_([1, 2, 3])
            assertTrue_Z_StrN_k_(list.remove_AnyN_k_(2), invalid)
            assertFalse_Z_StrN_k_(list.remove_AnyN_k_(5), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([1, 3]), toList_rIterable_k_(list), invalid)
        end function})
        m.test_Str_Function0V_k_("contains", {invoke: function() as Void
            list = arrayListOf_Arr_k_([1, 2, 3])
            assertTrue_Z_StrN_k_(list.contains_AnyN_k_(2), invalid)
            assertFalse_Z_StrN_k_(list.contains_AnyN_k_(5), invalid)
        end function})
        m.test_Str_Function0V_k_("indexOf", {invoke: function() as Void
            list = arrayListOf_Arr_k_([1, 2, 3, 2])
            assertEquals_AnyN_AnyN_StrN_k_(1, list.indexOf_AnyN_k_(2), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(-1, list.indexOf_AnyN_k_(5), invalid)
        end function})
        m.test_Str_Function0V_k_("lastIndexOf", {invoke: function() as Void
            list = arrayListOf_Arr_k_([1, 2, 3, 2])
            assertEquals_AnyN_AnyN_StrN_k_(3, list.lastIndexOf_AnyN_k_(2), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(-1, list.lastIndexOf_AnyN_k_(5), invalid)
        end function})
        m.test_Str_Function0V_k_("clear", {invoke: function() as Void
            list = arrayListOf_Arr_k_([1, 2, 3])
            list.clear_k_()
            assertTrue_Z_StrN_k_(list.isEmpty_k_(), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(0, list.get_size(), invalid)
        end function})
        m.test_Str_Function0V_k_("addAll", {invoke: function() as Void
            list = arrayListOf_Arr_k_([1, 2])
            list.addAll_Collection_k_(listOf_Arr_k_([3, 4, 5]))
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([1, 2, 3, 4, 5]), toList_rIterable_k_(list), invalid)
        end function})
        m.test_Str_Function0V_k_("addAll at index", {invoke: function() as Void
            list = arrayListOf_Arr_k_([1, 5])
            list.addAll_I_Collection_k_(1, listOf_Arr_k_([2, 3, 4]))
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([1, 2, 3, 4, 5]), toList_rIterable_k_(list), invalid)
        end function})
        m.test_Str_Function0V_k_("removeAll", {invoke: function() as Void
            list = arrayListOf_Arr_k_([1, 2, 3, 4, 5])
            list.removeAll_Collection_k_(listOf_Arr_k_([2, 4]))
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([1, 3, 5]), toList_rIterable_k_(list), invalid)
        end function})
        m.test_Str_Function0V_k_("retainAll", {invoke: function() as Void
            list = arrayListOf_Arr_k_([1, 2, 3, 4, 5])
            list.retainAll_Collection_k_(listOf_Arr_k_([2, 3, 6]))
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([2, 3]), toList_rIterable_k_(list), invalid)
        end function})
        m.test_Str_Function0V_k_("subList", {invoke: function() as Void
            list = arrayListOf_Arr_k_([1, 2, 3, 4, 5])
            subview = list.subList_I_I_k_(1, 4)
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([2, 3, 4]), toList_rIterable_k_(subview), invalid)
        end function})
        m.test_Str_Function0V_k_("iterator", {invoke: function() as Void
            list = arrayListOf_Arr_k_([1, 2, 3])
            collected = mutableListOf_k_()
            iter = list.iterator_k_()
            while iter.hasNext_k_()
                collected.add_AnyN_k_(iter.next_k_())
            end while
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([1, 2, 3]), collected, invalid)
        end function})
        m.test_Str_Function0V_k_("iterator remove", {invoke: function() as Void
            list = arrayListOf_Arr_k_([1, 2, 3])
            iter = list.iterator_k_()
            while iter.hasNext_k_()
                if iter.next_k_() = 2 then
                    iter.remove_k_()
                end if
            end while
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([1, 3]), toList_rIterable_k_(list), invalid)
        end function})
        m.test_Str_Function0V_k_("for-each loop", {invoke: function() as Void
            list = arrayListOf_Arr_k_([1, 2, 3])
            collected = mutableListOf_k_()
            for each item in list.get_array()
                collected.add_AnyN_k_(item)

            end for
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_k_([1, 2, 3]), collected, invalid)
        end function})
        m.test_Str_Function0V_k_("equals", {invoke: function() as Void
            list1 = arrayListOf_Arr_k_([1, 2, 3])
            list2 = arrayListOf_Arr_k_([1, 2, 3])
            list3 = arrayListOf_Arr_k_([1, 2, 4])
            assertEquals_AnyN_AnyN_StrN_k_(list1, list2, invalid)
            assertNotEquals_AnyN_AnyN_StrN_k_(list1, list3, invalid)
        end function})
        m.test_Str_Function0V_k_("toString", {invoke: function() as Void
            list = arrayListOf_Arr_k_([1, 2, 3])
            assertEquals_AnyN_AnyN_StrN_k_("[1, 2, 3]", list.toString(), invalid)
        end function})
    end function})
end sub
