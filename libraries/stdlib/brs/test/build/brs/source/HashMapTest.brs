sub hashMapTests_rTestRunner_k_(m as Object)
    m.suite_Str_Function1TestRunnerV_k_("HashMap", {invoke: function(m as Object) as Void
        m.test_Str_Function0V_k_("create empty", {invoke: function() as Void
            map = HashMap_create_HashMapAnyNAnyN_k_()
            assertTrue_Z_StrN_k_(map.isEmpty_Z_k_())
            assertEquals_AnyN_AnyN_StrN_k_(0, map.get_size())
        end function})
        m.test_Str_Function0V_k_("create from map", {invoke: function() as Void
            map = HashMap_create_MapAnyNAnyN_HashMapAnyNAnyN_k_(mapOf_Arr_MapAnyNAnyN_k_([to_rAnyN_AnyN_PairAnyNAnyN_k_("a", 1), to_rAnyN_AnyN_PairAnyNAnyN_k_("b", 2)]))
            assertEquals_AnyN_AnyN_StrN_k_(2, map.get_size())
            assertEquals_AnyN_AnyN_StrN_k_(1, map.get_AnyN_AnyN_k_("a"))
            assertEquals_AnyN_AnyN_StrN_k_(2, map.get_AnyN_AnyN_k_("b"))
        end function})
        m.test_Str_Function0V_k_("put and get", {invoke: function() as Void
            map = HashMap_create_HashMapAnyNAnyN_k_()
            assertNull_AnyN_StrN_k_(map.put_AnyN_AnyN_AnyN_k_("one", 1))
            assertEquals_AnyN_AnyN_StrN_k_(1, map.get_AnyN_AnyN_k_("one"))
            assertEquals_AnyN_AnyN_StrN_k_(1, map.put_AnyN_AnyN_AnyN_k_("one", 10))
            assertEquals_AnyN_AnyN_StrN_k_(10, map.get_AnyN_AnyN_k_("one"))
        end function})
        m.test_Str_Function0V_k_("get nonexistent", {invoke: function() as Void
            map = HashMap_create_HashMapAnyNAnyN_k_()
            assertNull_AnyN_StrN_k_(map.get_AnyN_AnyN_k_("missing"))
        end function})
        m.test_Str_Function0V_k_("containsKey", {invoke: function() as Void
            map = hashMapOf_Arr_HashMapAnyNAnyN_k_([to_rAnyN_AnyN_PairAnyNAnyN_k_("a", 1), to_rAnyN_AnyN_PairAnyNAnyN_k_("b", 2)])
            assertTrue_Z_StrN_k_(map.containsKey_AnyN_Z_k_("a"))
            assertFalse_Z_StrN_k_(map.containsKey_AnyN_Z_k_("c"))
        end function})
        m.test_Str_Function0V_k_("containsValue", {invoke: function() as Void
            map = hashMapOf_Arr_HashMapAnyNAnyN_k_([to_rAnyN_AnyN_PairAnyNAnyN_k_("a", 1), to_rAnyN_AnyN_PairAnyNAnyN_k_("b", 2)])
            assertTrue_Z_StrN_k_(map.containsValue_AnyN_Z_k_(1))
            assertFalse_Z_StrN_k_(map.containsValue_AnyN_Z_k_(3))
        end function})
        m.test_Str_Function0V_k_("remove", {invoke: function() as Void
            map = hashMapOf_Arr_HashMapAnyNAnyN_k_([to_rAnyN_AnyN_PairAnyNAnyN_k_("a", 1), to_rAnyN_AnyN_PairAnyNAnyN_k_("b", 2), to_rAnyN_AnyN_PairAnyNAnyN_k_("c", 3)])
            assertEquals_AnyN_AnyN_StrN_k_(2, map.remove_AnyN_AnyN_k_("b"))
            assertNull_AnyN_StrN_k_(map.remove_AnyN_AnyN_k_("missing"))
            assertEquals_AnyN_AnyN_StrN_k_(2, map.get_size())
            assertFalse_Z_StrN_k_(map.containsKey_AnyN_Z_k_("b"))
        end function})
        m.test_Str_Function0V_k_("clear", {invoke: function() as Void
            map = hashMapOf_Arr_HashMapAnyNAnyN_k_([to_rAnyN_AnyN_PairAnyNAnyN_k_("a", 1), to_rAnyN_AnyN_PairAnyNAnyN_k_("b", 2)])
            map.clear()
            assertTrue_Z_StrN_k_(map.isEmpty_Z_k_())
            assertEquals_AnyN_AnyN_StrN_k_(0, map.get_size())
        end function})
        m.test_Str_Function0V_k_("keys", {invoke: function() as Void
            map = hashMapOf_Arr_HashMapAnyNAnyN_k_([to_rAnyN_AnyN_PairAnyNAnyN_k_("a", 1), to_rAnyN_AnyN_PairAnyNAnyN_k_("b", 2), to_rAnyN_AnyN_PairAnyNAnyN_k_("c", 3)])
            keys = map.get_keys()
            assertEquals_AnyN_AnyN_StrN_k_(3, keys.get_size())
            assertTrue_Z_StrN_k_(keys.contains_AnyN_Z_k_("a"))
            assertTrue_Z_StrN_k_(keys.contains_AnyN_Z_k_("b"))
            assertTrue_Z_StrN_k_(keys.contains_AnyN_Z_k_("c"))
        end function})
        m.test_Str_Function0V_k_("values", {invoke: function() as Void
            map = hashMapOf_Arr_HashMapAnyNAnyN_k_([to_rAnyN_AnyN_PairAnyNAnyN_k_("a", 1), to_rAnyN_AnyN_PairAnyNAnyN_k_("b", 2), to_rAnyN_AnyN_PairAnyNAnyN_k_("c", 3)])
            values = map.get_values()
            assertEquals_AnyN_AnyN_StrN_k_(3, values.get_size())
            assertTrue_Z_StrN_k_(values.contains_AnyN_Z_k_(1))
            assertTrue_Z_StrN_k_(values.contains_AnyN_Z_k_(2))
            assertTrue_Z_StrN_k_(values.contains_AnyN_Z_k_(3))
        end function})
        m.test_Str_Function0V_k_("entries", {invoke: function() as Void
            map = hashMapOf_Arr_HashMapAnyNAnyN_k_([to_rAnyN_AnyN_PairAnyNAnyN_k_("a", 1), to_rAnyN_AnyN_PairAnyNAnyN_k_("b", 2)])
            entries = map.get_entries()
            assertEquals_AnyN_AnyN_StrN_k_(2, entries.get_size())
        end function})
        m.test_Str_Function0V_k_("putAll", {invoke: function() as Void
            map = hashMapOf_Arr_HashMapAnyNAnyN_k_([to_rAnyN_AnyN_PairAnyNAnyN_k_("a", 1)])
            map.putAll_MapAnyNAnyN_k_(mapOf_Arr_MapAnyNAnyN_k_([to_rAnyN_AnyN_PairAnyNAnyN_k_("b", 2), to_rAnyN_AnyN_PairAnyNAnyN_k_("c", 3)]))
            assertEquals_AnyN_AnyN_StrN_k_(3, map.get_size())
            assertEquals_AnyN_AnyN_StrN_k_(1, map.get_AnyN_AnyN_k_("a"))
            assertEquals_AnyN_AnyN_StrN_k_(2, map.get_AnyN_AnyN_k_("b"))
            assertEquals_AnyN_AnyN_StrN_k_(3, map.get_AnyN_AnyN_k_("c"))
        end function})
        m.test_Str_Function0V_k_("getOrDefault", {invoke: function() as Void
            map = hashMapOf_Arr_HashMapAnyNAnyN_k_([to_rAnyN_AnyN_PairAnyNAnyN_k_("a", 1)])
            assertEquals_AnyN_AnyN_StrN_k_(1, getOrDefault_rMapAnyNAnyN_AnyN_AnyN_AnyN_k_(map, "a", 0))
            assertEquals_AnyN_AnyN_StrN_k_(0, getOrDefault_rMapAnyNAnyN_AnyN_AnyN_AnyN_k_(map, "b", 0))
        end function})
        m.test_Str_Function0V_k_("getOrPut", {invoke: function() as Void
            map = hashMapOf_Arr_HashMapAnyNAnyN_k_([to_rAnyN_AnyN_PairAnyNAnyN_k_("a", 1)])
            assertEquals_AnyN_AnyN_StrN_k_(1, getOrPut_rMutableMapAnyNAnyN_AnyN_Function0AnyN_AnyN_k_(map, "a", {invoke: function() as Integer
                return 10
            end function}))
            assertEquals_AnyN_AnyN_StrN_k_(10, getOrPut_rMutableMapAnyNAnyN_AnyN_Function0AnyN_AnyN_k_(map, "b", {invoke: function() as Integer
                return 10
            end function}))
            assertEquals_AnyN_AnyN_StrN_k_(10, map.get_AnyN_AnyN_k_("b"))
        end function})
        m.test_Str_Function0V_k_("iteration via entries", {invoke: function() as Void
            map = hashMapOf_Arr_HashMapAnyNAnyN_k_([to_rAnyN_AnyN_PairAnyNAnyN_k_("a", 1), to_rAnyN_AnyN_PairAnyNAnyN_k_("b", 2)])
            keys = mutableListOf_MutableListAnyN_k_()
            values = mutableListOf_MutableListAnyN_k_()
            for each entry in map.get_entries().array
                keys.add_AnyN_Z_k_(entry.get_key())
                values.add_AnyN_Z_k_(entry.get_value())

            end for
            assertEquals_AnyN_AnyN_StrN_k_(2, keys.get_size())
            assertEquals_AnyN_AnyN_StrN_k_(2, values.get_size())
        end function})
        m.test_Str_Function0V_k_("equals", {invoke: function() as Void
            map1 = hashMapOf_Arr_HashMapAnyNAnyN_k_([to_rAnyN_AnyN_PairAnyNAnyN_k_("a", 1), to_rAnyN_AnyN_PairAnyNAnyN_k_("b", 2)])
            map2 = hashMapOf_Arr_HashMapAnyNAnyN_k_([to_rAnyN_AnyN_PairAnyNAnyN_k_("a", 1), to_rAnyN_AnyN_PairAnyNAnyN_k_("b", 2)])
            map3 = hashMapOf_Arr_HashMapAnyNAnyN_k_([to_rAnyN_AnyN_PairAnyNAnyN_k_("a", 1), to_rAnyN_AnyN_PairAnyNAnyN_k_("b", 3)])
            assertEquals_AnyN_AnyN_StrN_k_(map1, map2)
            assertNotEquals_AnyN_AnyN_StrN_k_(map1, map3)
        end function})
        m.test_Str_Function0V_k_("null values", {invoke: function() as Void
            map = HashMap_create_HashMapAnyNAnyN_k_()
            set_rMutableMapAnyNAnyN_AnyN_AnyN_k_(map, "a", invalid)
            assertTrue_Z_StrN_k_(map.containsKey_AnyN_Z_k_("a"))
            assertNull_AnyN_StrN_k_(map.get_AnyN_AnyN_k_("a"))
        end function})
        m.test_Str_Function0V_k_("integer keys", {invoke: function() as Void
            map = HashMap_create_HashMapAnyNAnyN_k_()
            set_rMutableMapAnyNAnyN_AnyN_AnyN_k_(map, 1, "one")
            set_rMutableMapAnyNAnyN_AnyN_AnyN_k_(map, 2, "two")
            assertEquals_AnyN_AnyN_StrN_k_("one", map.get_AnyN_AnyN_k_(1))
            assertEquals_AnyN_AnyN_StrN_k_("two", map.get_AnyN_AnyN_k_(2))
        end function})
    end function})
end sub
