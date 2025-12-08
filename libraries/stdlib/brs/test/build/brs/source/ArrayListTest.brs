sub arrayListTests_rTestRunner_k_(m as Object)
    m.suite("ArrayList", {_this_suite: _this_suite, invoke: function() as Void
        m._this_suite.test("create empty", {invoke: function() as Void
            list = ArrayList_create_ArrayListAnyN_k_()
            assertTrue_Z_StrN_k_(list.isEmpty())
            assertEquals_AnyN_AnyN_StrN_k_(0, list.size)
        end function})
        m._this_suite.test("create with initial capacity", {invoke: function() as Void
            list = ArrayList_create_I_ArrayListAnyN_k_(10)
            assertTrue_Z_StrN_k_(list.isEmpty())
            assertEquals_AnyN_AnyN_StrN_k_(0, list.size)
        end function})
        m._this_suite.test("create from collection", {invoke: function() as Void
            list = ArrayList_create_CollectionAnyN_ArrayListAnyN_k_(listOf_Arr_ListAnyN_k_([1, 2, 3]))
            assertEquals_AnyN_AnyN_StrN_k_(3, list.size)
            assertEquals_AnyN_AnyN_StrN_k_(1, list[0])
            assertEquals_AnyN_AnyN_StrN_k_(2, list[1])
            assertEquals_AnyN_AnyN_StrN_k_(3, list[2])
        end function})
        m._this_suite.test("add elements", {invoke: function() as Void
            list = ArrayList_create_ArrayListAnyN_k_()
            assertTrue_Z_StrN_k_(list.add(1))
            assertTrue_Z_StrN_k_(list.add(2))
            assertTrue_Z_StrN_k_(list.add(3))
            assertEquals_AnyN_AnyN_StrN_k_(3, list.size)
            assertEquals_AnyN_AnyN_StrN_k_(1, list[0])
            assertEquals_AnyN_AnyN_StrN_k_(2, list[1])
            assertEquals_AnyN_AnyN_StrN_k_(3, list[2])
        end function})
        m._this_suite.test("add at index", {invoke: function() as Void
            list = arrayListOf_Arr_ArrayListAnyN_k_([1, 3])
            list.add(1, 2)
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([1, 2, 3]), toList_rIterableAnyN_ListAnyN_k_(list))
        end function})
        m._this_suite.test("get element", {invoke: function() as Void
            list = arrayListOf_Arr_ArrayListAnyN_k_([10, 20, 30])
            assertEquals_AnyN_AnyN_StrN_k_(10, list[0])
            assertEquals_AnyN_AnyN_StrN_k_(20, list[1])
            assertEquals_AnyN_AnyN_StrN_k_(30, list[2])
        end function})
        m._this_suite.test("set element", {invoke: function() as Void
            list = arrayListOf_Arr_ArrayListAnyN_k_([1, 2, 3])
            old = list.set(1, 20)
            assertEquals_AnyN_AnyN_StrN_k_(2, old)
            assertEquals_AnyN_AnyN_StrN_k_(20, list[1])
        end function})
        m._this_suite.test("remove at index", {invoke: function() as Void
            list = arrayListOf_Arr_ArrayListAnyN_k_([1, 2, 3])
            removed = list.removeAt(1)
            assertEquals_AnyN_AnyN_StrN_k_(2, removed)
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([1, 3]), toList_rIterableAnyN_ListAnyN_k_(list))
        end function})
        m._this_suite.test("remove element", {invoke: function() as Void
            list = arrayListOf_Arr_ArrayListAnyN_k_([1, 2, 3])
            assertTrue_Z_StrN_k_(list.remove(2))
            assertFalse_Z_StrN_k_(list.remove(5))
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([1, 3]), toList_rIterableAnyN_ListAnyN_k_(list))
        end function})
        m._this_suite.test("contains", {invoke: function() as Void
            list = arrayListOf_Arr_ArrayListAnyN_k_([1, 2, 3])
            assertTrue_Z_StrN_k_(list.contains(2))
            assertFalse_Z_StrN_k_(list.contains(5))
        end function})
        m._this_suite.test("indexOf", {invoke: function() as Void
            list = arrayListOf_Arr_ArrayListAnyN_k_([1, 2, 3, 2])
            assertEquals_AnyN_AnyN_StrN_k_(1, list.indexOf(2))
            assertEquals_AnyN_AnyN_StrN_k_(-1, list.indexOf(5))
        end function})
        m._this_suite.test("lastIndexOf", {invoke: function() as Void
            list = arrayListOf_Arr_ArrayListAnyN_k_([1, 2, 3, 2])
            assertEquals_AnyN_AnyN_StrN_k_(3, list.lastIndexOf(2))
            assertEquals_AnyN_AnyN_StrN_k_(-1, list.lastIndexOf(5))
        end function})
        m._this_suite.test("clear", {invoke: function() as Void
            list = arrayListOf_Arr_ArrayListAnyN_k_([1, 2, 3])
            list.clear()
            assertTrue_Z_StrN_k_(list.isEmpty())
            assertEquals_AnyN_AnyN_StrN_k_(0, list.size)
        end function})
        m._this_suite.test("addAll", {invoke: function() as Void
            list = arrayListOf_Arr_ArrayListAnyN_k_([1, 2])
            list.addAll(listOf_Arr_ListAnyN_k_([3, 4, 5]))
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([1, 2, 3, 4, 5]), toList_rIterableAnyN_ListAnyN_k_(list))
        end function})
        m._this_suite.test("addAll at index", {invoke: function() as Void
            list = arrayListOf_Arr_ArrayListAnyN_k_([1, 5])
            list.addAll(1, listOf_Arr_ListAnyN_k_([2, 3, 4]))
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([1, 2, 3, 4, 5]), toList_rIterableAnyN_ListAnyN_k_(list))
        end function})
        m._this_suite.test("removeAll", {invoke: function() as Void
            list = arrayListOf_Arr_ArrayListAnyN_k_([1, 2, 3, 4, 5])
            list.removeAll(listOf_Arr_ListAnyN_k_([2, 4]))
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([1, 3, 5]), toList_rIterableAnyN_ListAnyN_k_(list))
        end function})
        m._this_suite.test("retainAll", {invoke: function() as Void
            list = arrayListOf_Arr_ArrayListAnyN_k_([1, 2, 3, 4, 5])
            list.retainAll(listOf_Arr_ListAnyN_k_([2, 3, 6]))
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([2, 3]), toList_rIterableAnyN_ListAnyN_k_(list))
        end function})
        m._this_suite.test("subList", {invoke: function() as Void
            list = arrayListOf_Arr_ArrayListAnyN_k_([1, 2, 3, 4, 5])
            subview = list.subList(1, 4)
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([2, 3, 4]), toList_rIterableAnyN_ListAnyN_k_(subview))
        end function})
        m._this_suite.test("iterator", {invoke: function() as Void
            list = arrayListOf_Arr_ArrayListAnyN_k_([1, 2, 3])
            collected = mutableListOf_MutableListAnyN_k_()
            iter = list.iterator()
            while iter.hasNext()
                collected.add(iter.next())
            end while
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([1, 2, 3]), collected)
        end function})
        m._this_suite.test("iterator remove", {invoke: function() as Void
            list = arrayListOf_Arr_ArrayListAnyN_k_([1, 2, 3])
            iter = list.iterator()
            while iter.hasNext()
                if iter.next() = 2 then
                    iter.remove()
                end if
            end while
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([1, 3]), toList_rIterableAnyN_ListAnyN_k_(list))
        end function})
        m._this_suite.test("for-each loop", {invoke: function() as Void
            list = arrayListOf_Arr_ArrayListAnyN_k_([1, 2, 3])
            collected = mutableListOf_MutableListAnyN_k_()
            for each item in list
                collected.add(item)

            end for
            assertEquals_AnyN_AnyN_StrN_k_(listOf_Arr_ListAnyN_k_([1, 2, 3]), collected)
        end function})
        m._this_suite.test("equals", {invoke: function() as Void
            list1 = arrayListOf_Arr_ArrayListAnyN_k_([1, 2, 3])
            list2 = arrayListOf_Arr_ArrayListAnyN_k_([1, 2, 3])
            list3 = arrayListOf_Arr_ArrayListAnyN_k_([1, 2, 4])
            assertEquals_AnyN_AnyN_StrN_k_(list1, list2)
            assertNotEquals_AnyN_AnyN_StrN_k_(list1, list3)
        end function})
        m._this_suite.test("toString", {invoke: function() as Void
            list = arrayListOf_Arr_ArrayListAnyN_k_([1, 2, 3])
            assertEquals_AnyN_AnyN_StrN_k_("[1, 2, 3]", list.toString())
        end function})
    end function})
end sub
