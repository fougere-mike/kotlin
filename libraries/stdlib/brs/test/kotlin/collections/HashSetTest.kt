/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.collections

import kotlin.test.*

/**
 * Tests for HashSet operations.
 */
fun TestRunner.hashSetTests() {
    suite("HashSet") {
        test("create empty") {
            val set = HashSet<Int>()
            assertTrue(set.isEmpty())
            assertEquals(0, set.size)
        }

        test("create from collection") {
            val set = HashSet(listOf(1, 2, 3, 2, 1))  // Duplicates
            assertEquals(3, set.size)
            assertTrue(set.contains(1))
            assertTrue(set.contains(2))
            assertTrue(set.contains(3))
        }

        test("add elements") {
            val set = HashSet<Int>()
            assertTrue(set.add(1))
            assertTrue(set.add(2))
            assertFalse(set.add(1))  // Duplicate
            assertEquals(2, set.size)
        }

        test("contains") {
            val set = hashSetOf(1, 2, 3)
            assertTrue(set.contains(2))
            assertFalse(set.contains(5))
        }

        test("remove") {
            val set = hashSetOf(1, 2, 3)
            assertTrue(set.remove(2))
            assertFalse(set.remove(5))
            assertEquals(2, set.size)
            assertFalse(set.contains(2))
        }

        test("clear") {
            val set = hashSetOf(1, 2, 3)
            set.clear()
            assertTrue(set.isEmpty())
            assertEquals(0, set.size)
        }

        test("addAll") {
            val set = hashSetOf(1, 2)
            assertTrue(set.addAll(listOf(3, 4, 2)))  // 2 is duplicate
            assertEquals(4, set.size)
        }

        test("removeAll") {
            val set = hashSetOf(1, 2, 3, 4, 5)
            assertTrue(set.removeAll(listOf(2, 4)))
            assertEquals(3, set.size)
            assertFalse(set.contains(2))
            assertFalse(set.contains(4))
        }

        test("retainAll") {
            val set = hashSetOf(1, 2, 3, 4, 5)
            assertTrue(set.retainAll(listOf(2, 3, 6)))
            assertEquals(2, set.size)
            assertTrue(set.contains(2))
            assertTrue(set.contains(3))
        }

        test("containsAll") {
            val set = hashSetOf(1, 2, 3, 4, 5)
            assertTrue(set.containsAll(listOf(1, 3, 5)))
            assertFalse(set.containsAll(listOf(1, 6)))
        }

        test("iteration") {
            val set = hashSetOf(1, 2, 3)
            val collected = mutableListOf<Int>()
            for (item in set) {
                collected.add(item)
            }
            assertEquals(3, collected.size)
            assertTrue(collected.containsAll(listOf(1, 2, 3)))
        }

        test("iterator remove") {
            val set = hashSetOf(1, 2, 3)
            val iter = set.iterator()
            while (iter.hasNext()) {
                if (iter.next() == 2) {
                    iter.remove()
                }
            }
            assertEquals(2, set.size)
            assertFalse(set.contains(2))
        }

        test("equals") {
            val set1 = hashSetOf(1, 2, 3)
            val set2 = hashSetOf(1, 2, 3)
            val set3 = hashSetOf(1, 2, 4)
            assertEquals(set1, set2)
            assertNotEquals(set1, set3)
        }

        test("toString") {
            val set = hashSetOf(1, 2, 3)
            val str = set.toString()
            assertTrue(str.startsWith("["))
            assertTrue(str.endsWith("]"))
        }

        test("string set") {
            val set = hashSetOf("apple", "banana", "cherry")
            assertTrue(set.contains("banana"))
            assertFalse(set.contains("durian"))
        }

    }
}
