/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.collections

import kotlin.test.*

/**
 * Tests for HashMap operations.
 */
fun TestRunner.hashMapTests() {
    suite("HashMap") {
        test("create empty") {
            val map = HashMap<String, Int>()
            assertTrue(map.isEmpty())
            assertEquals(0, map.size)
        }

        test("create from map") {
            val map = HashMap(mapOf("a" to 1, "b" to 2))
            assertEquals(2, map.size)
            assertEquals(1, map["a"])
            assertEquals(2, map["b"])
        }

        test("put and get") {
            val map = HashMap<String, Int>()
            assertNull(map.put("one", 1))
            assertEquals(1, map["one"])

            // Overwrite
            assertEquals(1, map.put("one", 10))
            assertEquals(10, map["one"])
        }

        test("get nonexistent") {
            val map = HashMap<String, Int>()
            assertNull(map["missing"])
        }

        test("containsKey") {
            val map = hashMapOf("a" to 1, "b" to 2)
            assertTrue(map.containsKey("a"))
            assertFalse(map.containsKey("c"))
        }

        test("containsValue") {
            val map = hashMapOf("a" to 1, "b" to 2)
            assertTrue(map.containsValue(1))
            assertFalse(map.containsValue(3))
        }

        test("remove") {
            val map = hashMapOf("a" to 1, "b" to 2, "c" to 3)
            assertEquals(2, map.remove("b"))
            assertNull(map.remove("missing"))
            assertEquals(2, map.size)
            assertFalse(map.containsKey("b"))
        }

        test("clear") {
            val map = hashMapOf("a" to 1, "b" to 2)
            map.clear()
            assertTrue(map.isEmpty())
            assertEquals(0, map.size)
        }

        test("keys") {
            val map = hashMapOf("a" to 1, "b" to 2, "c" to 3)
            val keys = map.keys
            assertEquals(3, keys.size)
            assertTrue(keys.contains("a"))
            assertTrue(keys.contains("b"))
            assertTrue(keys.contains("c"))
        }

        test("values") {
            val map = hashMapOf("a" to 1, "b" to 2, "c" to 3)
            val values = map.values
            assertEquals(3, values.size)
            assertTrue(values.contains(1))
            assertTrue(values.contains(2))
            assertTrue(values.contains(3))
        }

        test("entries") {
            val map = hashMapOf("a" to 1, "b" to 2)
            val entries = map.entries
            assertEquals(2, entries.size)
        }

        test("putAll") {
            val map = hashMapOf("a" to 1)
            map.putAll(mapOf("b" to 2, "c" to 3))
            assertEquals(3, map.size)
            assertEquals(1, map["a"])
            assertEquals(2, map["b"])
            assertEquals(3, map["c"])
        }

        test("getOrDefault") {
            val map = hashMapOf("a" to 1)
            assertEquals(1, map.getOrDefault("a", 0))
            assertEquals(0, map.getOrDefault("b", 0))
        }

        test("getOrPut") {
            val map = hashMapOf("a" to 1)
            assertEquals(1, map.getOrPut("a") { 10 })
            assertEquals(10, map.getOrPut("b") { 10 })
            assertEquals(10, map["b"])
        }

        test("iteration via entries") {
            val map = hashMapOf("a" to 1, "b" to 2)
            val keys = mutableListOf<String>()
            val values = mutableListOf<Int>()
            for (entry in map.entries) {
                keys.add(entry.key)
                values.add(entry.value)
            }
            assertEquals(2, keys.size)
            assertEquals(2, values.size)
        }

        test("equals") {
            val map1 = hashMapOf("a" to 1, "b" to 2)
            val map2 = hashMapOf("a" to 1, "b" to 2)
            val map3 = hashMapOf("a" to 1, "b" to 3)
            assertEquals(map1, map2)
            assertNotEquals(map1, map3)
        }

        test("null values") {
            val map = HashMap<String, Int?>()
            map["a"] = null
            assertTrue(map.containsKey("a"))
            assertNull(map["a"])
        }

        test("integer keys") {
            val map = HashMap<Int, String>()
            map[1] = "one"
            map[2] = "two"
            assertEquals("one", map[1])
            assertEquals("two", map[2])
        }
    }
}
