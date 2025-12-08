/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.collections

import kotlin.test.*

/**
 * Tests for LinkedHashSet - a set that maintains insertion order.
 */
fun TestRunner.linkedHashSetTests() {
    suite("LinkedHashSet") {
        test("insertion order") {
            val set = LinkedHashSet<String>()
            set.add("three")
            set.add("one")
            set.add("two")

            val list = set.toList()
            assertEquals(listOf("three", "one", "two"), list)
        }

        test("no duplicates") {
            val set = LinkedHashSet<Int>()
            assertTrue(set.add(1))
            assertTrue(set.add(2))
            assertFalse(set.add(1))  // Duplicate

            assertEquals(2, set.size)
            assertEquals(listOf(1, 2), set.toList())
        }

        test("constructor with collection") {
            val list = listOf(3, 1, 2, 1, 3)  // Has duplicates
            val set = LinkedHashSet(list)

            assertEquals(3, set.size)
            assertEquals(listOf(3, 1, 2), set.toList())  // Maintains first occurrence order
        }

        test("remove") {
            val set = LinkedHashSet<String>()
            set.add("one")
            set.add("two")
            set.add("three")

            assertTrue(set.remove("two"))
            assertFalse(set.remove("four"))

            assertEquals(2, set.size)
            assertEquals(listOf("one", "three"), set.toList())
        }

        test("contains") {
            val set = LinkedHashSet<Int>()
            set.add(1)
            set.add(2)
            set.add(3)

            assertTrue(set.contains(2))
            assertFalse(set.contains(5))
        }

        test("iterator") {
            val set = LinkedHashSet<Int>()
            set.add(10)
            set.add(20)
            set.add(30)

            val collected = mutableListOf<Int>()
            for (element in set) {
                collected.add(element)
            }

            assertEquals(listOf(10, 20, 30), collected)
        }

        test("iterator remove") {
            val set = LinkedHashSet<String>()
            set.add("a")
            set.add("b")
            set.add("c")

            val iter = set.iterator()
            while (iter.hasNext()) {
                if (iter.next() == "b") {
                    iter.remove()
                }
            }

            assertEquals(2, set.size)
            assertEquals(listOf("a", "c"), set.toList())
        }

        test("addAll") {
            val set = LinkedHashSet<Int>()
            set.add(1)

            val result = set.addAll(listOf(2, 3, 1))  // 1 is duplicate
            assertTrue(result)

            assertEquals(3, set.size)
            assertEquals(listOf(1, 2, 3), set.toList())
        }

        test("removeAll") {
            val set = LinkedHashSet<Int>()
            set.addAll(listOf(1, 2, 3, 4, 5))

            val result = set.removeAll(listOf(2, 4))
            assertTrue(result)

            assertEquals(3, set.size)
            assertEquals(listOf(1, 3, 5), set.toList())
        }

        test("retainAll") {
            val set = LinkedHashSet<Int>()
            set.addAll(listOf(1, 2, 3, 4, 5))

            val result = set.retainAll(listOf(2, 3, 6))
            assertTrue(result)

            assertEquals(2, set.size)
            assertEquals(listOf(2, 3), set.toList())
        }

        test("clear") {
            val set = LinkedHashSet<String>()
            set.addAll(listOf("a", "b", "c"))

            set.clear()

            assertEquals(0, set.size)
            assertTrue(set.isEmpty())
        }
    }
}
