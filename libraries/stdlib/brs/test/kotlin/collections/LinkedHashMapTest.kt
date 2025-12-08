/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.collections

import kotlin.test.*

/**
 * Tests for LinkedHashMap - a map that maintains insertion order.
 */
fun TestRunner.linkedHashMapTests() {
    suite("LinkedHashMap") {
        test("insertion order") {
            val map = LinkedHashMap<String, Int>()
            map["three"] = 3
            map["one"] = 1
            map["two"] = 2

            val keys = map.keys.toList()
            assertEquals(listOf("three", "one", "two"), keys)

            val values = map.values.toList()
            assertEquals(listOf(3, 1, 2), values)
        }

        test("insertion order with updates") {
            val map = LinkedHashMap<String, Int>()
            map["one"] = 1
            map["two"] = 2
            map["three"] = 3
            map["two"] = 22  // Update existing key

            // Order should remain the same after update
            val keys = map.keys.toList()
            assertEquals(listOf("one", "two", "three"), keys)
            assertEquals(22, map["two"])
        }

        test("constructor with map") {
            val original = mapOf("a" to 1, "b" to 2, "c" to 3)
            val linked = LinkedHashMap(original)

            assertEquals(3, linked.size)
            assertTrue(linked.containsKey("a"))
            assertTrue(linked.containsKey("b"))
            assertTrue(linked.containsKey("c"))
        }

        test("put and get") {
            val map = LinkedHashMap<Int, String>()
            assertNull(map.put(1, "one"))
            assertEquals("one", map.put(1, "ONE"))
            assertEquals("ONE", map[1])
        }

        test("remove") {
            val map = LinkedHashMap<String, Int>()
            map["one"] = 1
            map["two"] = 2
            map["three"] = 3

            assertEquals(2, map.remove("two"))
            assertFalse(map.containsKey("two"))
            assertEquals(2, map.size)

            val keys = map.keys.toList()
            assertEquals(listOf("one", "three"), keys)
        }

        test("clear") {
            val map = LinkedHashMap<String, Int>()
            map["one"] = 1
            map["two"] = 2

            map.clear()
            assertEquals(0, map.size)
            assertTrue(map.isEmpty())
        }

        test("iterator remove") {
            val map = LinkedHashMap<String, Int>()
            map["one"] = 1
            map["two"] = 2
            map["three"] = 3

            val iter = map.keys.iterator()
            while (iter.hasNext()) {
                if (iter.next() == "two") {
                    iter.remove()
                }
            }

            assertEquals(2, map.size)
            assertFalse(map.containsKey("two"))
        }

        test("entry set") {
            val map = LinkedHashMap<String, Int>()
            map["one"] = 1
            map["two"] = 2

            val entries = map.entries.toList()
            assertEquals(2, entries.size)
            assertEquals("one", entries[0].key)
            assertEquals(1, entries[0].value)
        }

        test("putAll") {
            val map = LinkedHashMap<String, Int>()
            map["one"] = 1

            val other = mapOf("two" to 2, "three" to 3)
            map.putAll(other)

            assertEquals(3, map.size)
            assertEquals(listOf("one", "two", "three"), map.keys.toList())
        }
    }
}
