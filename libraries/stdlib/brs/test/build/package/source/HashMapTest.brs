sub hashMapTests_rTestRunner_k_(m as Object)
    m.suite("HashMap", {_this_suite: _this_suite, invoke: function() as Void
        m._this_suite.test("create empty", {invoke: function() as Void
            map = HashMap_create_HashMapAnyNAnyN_k_()
            assertTrue_Z_StrN_k_(map.isEmpty())
            assertEquals_AnyN_AnyN_StrN_k_(0, map.size)
        end function})
        m._this_suite.test("create from map", {invoke: function() as Void
            map = HashMap_create_MapAnyNAnyN_HashMapAnyNAnyN_k_(mapOf_Arr_MapAnyNAnyN_k_([to_rAnyN_AnyN_PairAnyNAnyN_k_("a", 1), to_rAnyN_AnyN_PairAnyNAnyN_k_("b", 2)]))
            assertEquals_AnyN_AnyN_StrN_k_(2, map.size)
            assertEquals_AnyN_AnyN_StrN_k_(1, map["a"])
            assertEquals_AnyN_AnyN_StrN_k_(2, map["b"])
        end function})
        m._this_suite.test("put and get", {invoke: function() as Void
            map = HashMap_create_HashMapAnyNAnyN_k_()
            assertNull_AnyN_StrN_k_(map.put("one", 1))
            assertEquals_AnyN_AnyN_StrN_k_(1, map["one"])
            assertEquals_AnyN_AnyN_StrN_k_(1, map.put("one", 10))
            assertEquals_AnyN_AnyN_StrN_k_(10, map["one"])
        end function})
        m._this_suite.test("get nonexistent", {invoke: function() as Void
            map = HashMap_create_HashMapAnyNAnyN_k_()
            assertNull_AnyN_StrN_k_(map["missing"])
        end function})
        m._this_suite.test("containsKey", {invoke: function() as Void
            map = hashMapOf_Arr_HashMapAnyNAnyN_k_([to_rAnyN_AnyN_PairAnyNAnyN_k_("a", 1), to_rAnyN_AnyN_PairAnyNAnyN_k_("b", 2)])
            assertTrue_Z_StrN_k_(map.containsKey("a"))
            assertFalse_Z_StrN_k_(map.containsKey("c"))
        end function})
        m._this_suite.test("containsValue", {invoke: function() as Void
            map = hashMapOf_Arr_HashMapAnyNAnyN_k_([to_rAnyN_AnyN_PairAnyNAnyN_k_("a", 1), to_rAnyN_AnyN_PairAnyNAnyN_k_("b", 2)])
            assertTrue_Z_StrN_k_(map.containsValue(1))
            assertFalse_Z_StrN_k_(map.containsValue(3))
        end function})
        m._this_suite.test("remove", {invoke: function() as Void
            map = hashMapOf_Arr_HashMapAnyNAnyN_k_([to_rAnyN_AnyN_PairAnyNAnyN_k_("a", 1), to_rAnyN_AnyN_PairAnyNAnyN_k_("b", 2), to_rAnyN_AnyN_PairAnyNAnyN_k_("c", 3)])
            assertEquals_AnyN_AnyN_StrN_k_(2, map.remove("b"))
            assertNull_AnyN_StrN_k_(map.remove("missing"))
            assertEquals_AnyN_AnyN_StrN_k_(2, map.size)
            assertFalse_Z_StrN_k_(map.containsKey("b"))
        end function})
        m._this_suite.test("clear", {invoke: function() as Void
            map = hashMapOf_Arr_HashMapAnyNAnyN_k_([to_rAnyN_AnyN_PairAnyNAnyN_k_("a", 1), to_rAnyN_AnyN_PairAnyNAnyN_k_("b", 2)])
            map.clear()
            assertTrue_Z_StrN_k_(map.isEmpty())
            assertEquals_AnyN_AnyN_StrN_k_(0, map.size)
        end function})
        m._this_suite.test("keys", {invoke: function() as Void
            map = hashMapOf_Arr_HashMapAnyNAnyN_k_([to_rAnyN_AnyN_PairAnyNAnyN_k_("a", 1), to_rAnyN_AnyN_PairAnyNAnyN_k_("b", 2), to_rAnyN_AnyN_PairAnyNAnyN_k_("c", 3)])
            keys = map.keys
            assertEquals_AnyN_AnyN_StrN_k_(3, keys.size)
            assertTrue_Z_StrN_k_(keys.contains("a"))
            assertTrue_Z_StrN_k_(keys.contains("b"))
            assertTrue_Z_StrN_k_(keys.contains("c"))
        end function})
        m._this_suite.test("values", {invoke: function() as Void
            map = hashMapOf_Arr_HashMapAnyNAnyN_k_([to_rAnyN_AnyN_PairAnyNAnyN_k_("a", 1), to_rAnyN_AnyN_PairAnyNAnyN_k_("b", 2), to_rAnyN_AnyN_PairAnyNAnyN_k_("c", 3)])
            values = map.values
            assertEquals_AnyN_AnyN_StrN_k_(3, values.size)
            assertTrue_Z_StrN_k_(values.contains(1))
            assertTrue_Z_StrN_k_(values.contains(2))
            assertTrue_Z_StrN_k_(values.contains(3))
        end function})
        m._this_suite.test("entries", {invoke: function() as Void
            map = hashMapOf_Arr_HashMapAnyNAnyN_k_([to_rAnyN_AnyN_PairAnyNAnyN_k_("a", 1), to_rAnyN_AnyN_PairAnyNAnyN_k_("b", 2)])
            entries = map.entries
            assertEquals_AnyN_AnyN_StrN_k_(2, entries.size)
        end function})
        m._this_suite.test("putAll", {invoke: function() as Void
            map = hashMapOf_Arr_HashMapAnyNAnyN_k_([to_rAnyN_AnyN_PairAnyNAnyN_k_("a", 1)])
            map.putAll(mapOf_Arr_MapAnyNAnyN_k_([to_rAnyN_AnyN_PairAnyNAnyN_k_("b", 2), to_rAnyN_AnyN_PairAnyNAnyN_k_("c", 3)]))
            assertEquals_AnyN_AnyN_StrN_k_(3, map.size)
            assertEquals_AnyN_AnyN_StrN_k_(1, map["a"])
            assertEquals_AnyN_AnyN_StrN_k_(2, map["b"])
            assertEquals_AnyN_AnyN_StrN_k_(3, map["c"])
        end function})
        m._this_suite.test("getOrDefault", {invoke: function() as Void
            map = hashMapOf_Arr_HashMapAnyNAnyN_k_([to_rAnyN_AnyN_PairAnyNAnyN_k_("a", 1)])
            assertEquals_AnyN_AnyN_StrN_k_(1, getOrDefault_rMapAnyNAnyN_AnyN_AnyN_AnyN_k_(map, "a", 0))
            assertEquals_AnyN_AnyN_StrN_k_(0, getOrDefault_rMapAnyNAnyN_AnyN_AnyN_AnyN_k_(map, "b", 0))
        end function})
        m._this_suite.test("getOrPut", {invoke: function() as Void
            map = hashMapOf_Arr_HashMapAnyNAnyN_k_([to_rAnyN_AnyN_PairAnyNAnyN_k_("a", 1)])
            assertEquals_AnyN_AnyN_StrN_k_(1, getOrPut_rMutableMapAnyNAnyN_AnyN_Function0AnyN_AnyN_k_(map, "a", {invoke: function() as Integer
                return 10
            end function}))
            assertEquals_AnyN_AnyN_StrN_k_(10, getOrPut_rMutableMapAnyNAnyN_AnyN_Function0AnyN_AnyN_k_(map, "b", {invoke: function() as Integer
                return 10
            end function}))
            assertEquals_AnyN_AnyN_StrN_k_(10, map["b"])
        end function})
        m._this_suite.test("iteration via entries", {invoke: function() as Void
            map = hashMapOf_Arr_HashMapAnyNAnyN_k_([to_rAnyN_AnyN_PairAnyNAnyN_k_("a", 1), to_rAnyN_AnyN_PairAnyNAnyN_k_("b", 2)])
            keys = mutableListOf_MutableListAnyN_k_()
            values = mutableListOf_MutableListAnyN_k_()
            for each entry in map.entries
                keys.add(entry.key)
                values.add(entry.value)

            end for
            assertEquals_AnyN_AnyN_StrN_k_(2, keys.size)
            assertEquals_AnyN_AnyN_StrN_k_(2, values.size)
        end function})
        m._this_suite.test("equals", {invoke: function() as Void
            map1 = hashMapOf_Arr_HashMapAnyNAnyN_k_([to_rAnyN_AnyN_PairAnyNAnyN_k_("a", 1), to_rAnyN_AnyN_PairAnyNAnyN_k_("b", 2)])
            map2 = hashMapOf_Arr_HashMapAnyNAnyN_k_([to_rAnyN_AnyN_PairAnyNAnyN_k_("a", 1), to_rAnyN_AnyN_PairAnyNAnyN_k_("b", 2)])
            map3 = hashMapOf_Arr_HashMapAnyNAnyN_k_([to_rAnyN_AnyN_PairAnyNAnyN_k_("a", 1), to_rAnyN_AnyN_PairAnyNAnyN_k_("b", 3)])
            assertEquals_AnyN_AnyN_StrN_k_(map1, map2)
            assertNotEquals_AnyN_AnyN_StrN_k_(map1, map3)
        end function})
        m._this_suite.test("null values", {invoke: function() as Void
            map = HashMap_create_HashMapAnyNAnyN_k_()
            set_rMutableMapAnyNAnyN_AnyN_AnyN_k_(map, "a", invalid)
            assertTrue_Z_StrN_k_(map.containsKey("a"))
            assertNull_AnyN_StrN_k_(map["a"])
        end function})
        m._this_suite.test("integer keys", {invoke: function() as Void
            map = HashMap_create_HashMapAnyNAnyN_k_()
            set_rMutableMapAnyNAnyN_AnyN_AnyN_k_(map, 1, "one")
            set_rMutableMapAnyNAnyN_AnyN_AnyN_k_(map, 2, "two")
            assertEquals_AnyN_AnyN_StrN_k_("one", map[1])
            assertEquals_AnyN_AnyN_StrN_k_("two", map[2])
        end function})
    end function})
end sub
