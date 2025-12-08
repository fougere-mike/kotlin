/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.collections

import kotlin.test.*

/**
 * Tests for ArrayList operations.
 */
fun TestRunner.arrayListTests() {
    suite("ArrayList") {
        test("create empty") {
            val list = ArrayList<Int>()
            assertTrue(list.isEmpty())
            assertEquals(0, list.size)
        }

        test("create with initial capacity") {
            val list = ArrayList<Int>(10)
            assertTrue(list.isEmpty())
            assertEquals(0, list.size)
        }

        test("create from collection") {
            val list = ArrayList(listOf(1, 2, 3))
            assertEquals(3, list.size)
            assertEquals(1, list[0])
            assertEquals(2, list[1])
            assertEquals(3, list[2])
        }

        test("add elements") {
            val list = ArrayList<Int>()
            assertTrue(list.add(1))
            assertTrue(list.add(2))
            assertTrue(list.add(3))
            assertEquals(3, list.size)
            assertEquals(1, list[0])
            assertEquals(2, list[1])
            assertEquals(3, list[2])
        }

        test("add at index") {
            val list = arrayListOf(1, 3)
            list.add(1, 2)
            assertEquals(listOf(1, 2, 3), list.toList())
        }

        test("get element") {
            val list = arrayListOf(10, 20, 30)
            assertEquals(10, list[0])
            assertEquals(20, list[1])
            assertEquals(30, list[2])
        }

        test("set element") {
            val list = arrayListOf(1, 2, 3)
            val old = list.set(1, 20)
            assertEquals(2, old)
            assertEquals(20, list[1])
        }

        test("remove at index") {
            val list = arrayListOf(1, 2, 3)
            val removed = list.removeAt(1)
            assertEquals(2, removed)
            assertEquals(listOf(1, 3), list.toList())
        }

        test("remove element") {
            val list = arrayListOf(1, 2, 3)
            assertTrue(list.remove(2))
            assertFalse(list.remove(5))
            assertEquals(listOf(1, 3), list.toList())
        }

        test("contains") {
            val list = arrayListOf(1, 2, 3)
            assertTrue(list.contains(2))
            assertFalse(list.contains(5))
        }

        test("indexOf") {
            val list = arrayListOf(1, 2, 3, 2)
            assertEquals(1, list.indexOf(2))
            assertEquals(-1, list.indexOf(5))
        }

        test("lastIndexOf") {
            val list = arrayListOf(1, 2, 3, 2)
            assertEquals(3, list.lastIndexOf(2))
            assertEquals(-1, list.lastIndexOf(5))
        }

        test("clear") {
            val list = arrayListOf(1, 2, 3)
            list.clear()
            assertTrue(list.isEmpty())
            assertEquals(0, list.size)
        }

        test("addAll") {
            val list = arrayListOf(1, 2)
            list.addAll(listOf(3, 4, 5))
            assertEquals(listOf(1, 2, 3, 4, 5), list.toList())
        }

        test("addAll at index") {
            val list = arrayListOf(1, 5)
            list.addAll(1, listOf(2, 3, 4))
            assertEquals(listOf(1, 2, 3, 4, 5), list.toList())
        }

        test("removeAll") {
            val list = arrayListOf(1, 2, 3, 4, 5)
            list.removeAll(listOf(2, 4))
            assertEquals(listOf(1, 3, 5), list.toList())
        }

        test("retainAll") {
            val list = arrayListOf(1, 2, 3, 4, 5)
            list.retainAll(listOf(2, 3, 6))
            assertEquals(listOf(2, 3), list.toList())
        }

        test("subList") {
            val list = arrayListOf(1, 2, 3, 4, 5)
            val subview = list.subList(1, 4)
            assertEquals(listOf(2, 3, 4), subview.toList())
        }

        test("iterator") {
            val list = arrayListOf(1, 2, 3)
            val collected = mutableListOf<Int>()
            val iter = list.iterator()
            while (iter.hasNext()) {
                collected.add(iter.next())
            }
            assertEquals(listOf(1, 2, 3), collected)
        }

        test("iterator remove") {
            val list = arrayListOf(1, 2, 3)
            val iter = list.iterator()
            while (iter.hasNext()) {
                if (iter.next() == 2) {
                    iter.remove()
                }
            }
            assertEquals(listOf(1, 3), list.toList())
        }

        test("for-each loop") {
            val list = arrayListOf(1, 2, 3)
            val collected = mutableListOf<Int>()
            for (item in list) {
                collected.add(item)
            }
            assertEquals(listOf(1, 2, 3), collected)
        }

        test("equals") {
            val list1 = arrayListOf(1, 2, 3)
            val list2 = arrayListOf(1, 2, 3)
            val list3 = arrayListOf(1, 2, 4)
            assertEquals(list1, list2)
            assertNotEquals(list1, list3)
        }

        test("toString") {
            val list = arrayListOf(1, 2, 3)
            assertEquals("[1, 2, 3]", list.toString())
        }
    }
}
