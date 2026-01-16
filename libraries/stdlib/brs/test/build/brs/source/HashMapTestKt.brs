sub hashMapTests_rTestRunner_k_(m as Object)
    m.suite_Str_Function1TestRunnerV_k_("HashMap", {invoke: function(m as Object) as Void
        m.test_Str_Function0V_k_("create empty", {invoke: function() as Void
            map = HashMap_create_k_()
            assertTrue_Z_StrN_k_(map.isEmpty_k_(), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(0, map.get_size(), invalid)
        end function})
        m.test_Str_Function0V_k_("create from map", {invoke: function() as Void
            map = HashMap_create_Map_k_(mapOf_Arr_k_([to_rAnyN_AnyN_k_("a", 1), to_rAnyN_AnyN_k_("b", 2)]))
            assertEquals_AnyN_AnyN_StrN_k_(2, map.get_size(), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(1, map.get_AnyN_k_("a"), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(2, map.get_AnyN_k_("b"), invalid)
        end function})
        m.test_Str_Function0V_k_("put and get", {invoke: function() as Void
            map = HashMap_create_k_()
            assertNull_AnyN_StrN_k_(map.put_AnyN_AnyN_k_("one", 1), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(1, map.get_AnyN_k_("one"), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(1, map.put_AnyN_AnyN_k_("one", 10), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(10, map.get_AnyN_k_("one"), invalid)
        end function})
        m.test_Str_Function0V_k_("get nonexistent", {invoke: function() as Void
            map = HashMap_create_k_()
            assertNull_AnyN_StrN_k_(map.get_AnyN_k_("missing"), invalid)
        end function})
        m.test_Str_Function0V_k_("containsKey", {invoke: function() as Void
            map = hashMapOf_Arr_k_([to_rAnyN_AnyN_k_("a", 1), to_rAnyN_AnyN_k_("b", 2)])
            assertTrue_Z_StrN_k_(map.containsKey_AnyN_k_("a"), invalid)
            assertFalse_Z_StrN_k_(map.containsKey_AnyN_k_("c"), invalid)
        end function})
        m.test_Str_Function0V_k_("containsValue", {invoke: function() as Void
            map = hashMapOf_Arr_k_([to_rAnyN_AnyN_k_("a", 1), to_rAnyN_AnyN_k_("b", 2)])
            assertTrue_Z_StrN_k_(map.containsValue_AnyN_k_(1), invalid)
            assertFalse_Z_StrN_k_(map.containsValue_AnyN_k_(3), invalid)
        end function})
        m.test_Str_Function0V_k_("remove", {invoke: function() as Void
            map = hashMapOf_Arr_k_([to_rAnyN_AnyN_k_("a", 1), to_rAnyN_AnyN_k_("b", 2), to_rAnyN_AnyN_k_("c", 3)])
            assertEquals_AnyN_AnyN_StrN_k_(2, map.remove_AnyN_k_("b"), invalid)
            assertNull_AnyN_StrN_k_(map.remove_AnyN_k_("missing"), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(2, map.get_size(), invalid)
            assertFalse_Z_StrN_k_(map.containsKey_AnyN_k_("b"), invalid)
        end function})
        m.test_Str_Function0V_k_("clear", {invoke: function() as Void
            map = hashMapOf_Arr_k_([to_rAnyN_AnyN_k_("a", 1), to_rAnyN_AnyN_k_("b", 2)])
            map.clear_k_()
            assertTrue_Z_StrN_k_(map.isEmpty_k_(), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(0, map.get_size(), invalid)
        end function})
        m.test_Str_Function0V_k_("keys", {invoke: function() as Void
            map = hashMapOf_Arr_k_([to_rAnyN_AnyN_k_("a", 1), to_rAnyN_AnyN_k_("b", 2), to_rAnyN_AnyN_k_("c", 3)])
            keys = map.get_keys()
            assertEquals_AnyN_AnyN_StrN_k_(3, keys.get_size(), invalid)
            assertTrue_Z_StrN_k_(keys.contains_AnyN_k_("a"), invalid)
            assertTrue_Z_StrN_k_(keys.contains_AnyN_k_("b"), invalid)
            assertTrue_Z_StrN_k_(keys.contains_AnyN_k_("c"), invalid)
        end function})
        m.test_Str_Function0V_k_("values", {invoke: function() as Void
            map = hashMapOf_Arr_k_([to_rAnyN_AnyN_k_("a", 1), to_rAnyN_AnyN_k_("b", 2), to_rAnyN_AnyN_k_("c", 3)])
            values = map.get_values()
            assertEquals_AnyN_AnyN_StrN_k_(3, values.get_size(), invalid)
            assertTrue_Z_StrN_k_(values.contains_AnyN_k_(1), invalid)
            assertTrue_Z_StrN_k_(values.contains_AnyN_k_(2), invalid)
            assertTrue_Z_StrN_k_(values.contains_AnyN_k_(3), invalid)
        end function})
        m.test_Str_Function0V_k_("entries", {invoke: function() as Void
            map = hashMapOf_Arr_k_([to_rAnyN_AnyN_k_("a", 1), to_rAnyN_AnyN_k_("b", 2)])
            entries = map.get_entries()
            assertEquals_AnyN_AnyN_StrN_k_(2, entries.get_size(), invalid)
        end function})
        m.test_Str_Function0V_k_("putAll", {invoke: function() as Void
            map = hashMapOf_Arr_k_([to_rAnyN_AnyN_k_("a", 1)])
            map.putAll_Map_k_(mapOf_Arr_k_([to_rAnyN_AnyN_k_("b", 2), to_rAnyN_AnyN_k_("c", 3)]))
            assertEquals_AnyN_AnyN_StrN_k_(3, map.get_size(), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(1, map.get_AnyN_k_("a"), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(2, map.get_AnyN_k_("b"), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(3, map.get_AnyN_k_("c"), invalid)
        end function})
        m.test_Str_Function0V_k_("getOrDefault", {invoke: function() as Void
            map = hashMapOf_Arr_k_([to_rAnyN_AnyN_k_("a", 1)])
            assertEquals_AnyN_AnyN_StrN_k_(1, getOrDefault_rMap_AnyN_AnyN_k_(map, "a", 0), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(0, getOrDefault_rMap_AnyN_AnyN_k_(map, "b", 0), invalid)
        end function})
        m.test_Str_Function0V_k_("getOrPut", {invoke: function() as Void
            map = hashMapOf_Arr_k_([to_rAnyN_AnyN_k_("a", 1)])
            assertEquals_AnyN_AnyN_StrN_k_(1, getOrPut_rMutableMap_AnyN_Function0_k_(map, "a", {invoke: function() as Integer
                return 10
            end function}), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(10, getOrPut_rMutableMap_AnyN_Function0_k_(map, "b", {invoke: function() as Integer
                return 10
            end function}), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(10, map.get_AnyN_k_("b"), invalid)
        end function})
        m.test_Str_Function0V_k_("iteration via entries", {invoke: function() as Void
            map = hashMapOf_Arr_k_([to_rAnyN_AnyN_k_("a", 1), to_rAnyN_AnyN_k_("b", 2)])
            keys = mutableListOf_k_()
            values = mutableListOf_k_()
            __iter_0 = map.get_entries().iterator_k_()
            while __iter_0.hasNext_k_()
                entry = __iter_0.next_k_()
                keys.add_AnyN_k_(entry.get_key())
                values.add_AnyN_k_(entry.get_value())

            end while

            assertEquals_AnyN_AnyN_StrN_k_(2, keys.get_size(), invalid)
            assertEquals_AnyN_AnyN_StrN_k_(2, values.get_size(), invalid)
        end function})
        m.test_Str_Function0V_k_("equals", {invoke: function() as Void
            map1 = hashMapOf_Arr_k_([to_rAnyN_AnyN_k_("a", 1), to_rAnyN_AnyN_k_("b", 2)])
            map2 = hashMapOf_Arr_k_([to_rAnyN_AnyN_k_("a", 1), to_rAnyN_AnyN_k_("b", 2)])
            map3 = hashMapOf_Arr_k_([to_rAnyN_AnyN_k_("a", 1), to_rAnyN_AnyN_k_("b", 3)])
            assertEquals_AnyN_AnyN_StrN_k_(map1, map2, invalid)
            assertNotEquals_AnyN_AnyN_StrN_k_(map1, map3, invalid)
        end function})
        m.test_Str_Function0V_k_("null values", {invoke: function() as Void
            map = HashMap_create_k_()
            set_rMutableMap_AnyN_AnyN_k_(map, "a", invalid)
            assertTrue_Z_StrN_k_(map.containsKey_AnyN_k_("a"), invalid)
            assertNull_AnyN_StrN_k_(map.get_AnyN_k_("a"), invalid)
        end function})
        m.test_Str_Function0V_k_("integer keys", {invoke: function() as Void
            map = HashMap_create_k_()
            set_rMutableMap_AnyN_AnyN_k_(map, 1, "one")
            set_rMutableMap_AnyN_AnyN_k_(map, 2, "two")
            assertEquals_AnyN_AnyN_StrN_k_("one", map.get_AnyN_k_(1), invalid)
            assertEquals_AnyN_AnyN_StrN_k_("two", map.get_AnyN_k_(2), invalid)
        end function})
    end function})
end sub
