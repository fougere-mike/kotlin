/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.collections

import kotlin.test.*

/**
 * Tests for collection extension functions.
 */
fun TestRunner.collectionExtensionsTests() {
    suite("Collection Extensions") {
        // Transformations
        test("map") {
            val list = listOf(1, 2, 3)
            val result = list.map { it * 2 }
            assertEquals(listOf(2, 4, 6), result)
        }

        test("mapNotNull") {
            val list = listOf(1, 2, 3, 4)
            val result = list.mapNotNull { if (it % 2 == 0) it * 2 else null }
            assertEquals(listOf(4, 8), result)
        }

        test("mapIndexed") {
            val list = listOf("a", "b", "c")
            val result = list.mapIndexed { index, value -> "$index:$value" }
            assertEquals(listOf("0:a", "1:b", "2:c"), result)
        }

        test("flatMap") {
            val list = listOf(1, 2, 3)
            val result = list.flatMap { listOf(it, it * 10) }
            assertEquals(listOf(1, 10, 2, 20, 3, 30), result)
        }

        // Filtering
        test("filter") {
            val list = listOf(1, 2, 3, 4, 5)
            val result = list.filter { it > 3 }
            assertEquals(listOf(4, 5), result)
        }

        test("filterNot") {
            val list = listOf(1, 2, 3, 4, 5)
            val result = list.filterNot { it > 3 }
            assertEquals(listOf(1, 2, 3), result)
        }

        test("filterIndexed") {
            val list = listOf("a", "b", "c", "d")
            val result = list.filterIndexed { index, _ -> index % 2 == 0 }
            assertEquals(listOf("a", "c"), result)
        }

        test("filterNotNull") {
            val list = listOf(1, null, 2, null, 3)
            val result = list.filterNotNull()
            assertEquals(listOf(1, 2, 3), result)
        }

        // Aggregation
        test("fold") {
            val list = listOf(1, 2, 3, 4)
            val sum = list.fold(0) { acc, n -> acc + n }
            assertEquals(10, sum)
        }

        test("reduce") {
            val list = listOf(1, 2, 3, 4)
            val product = list.reduce { acc, n -> acc * n }
            assertEquals(24, product)
        }

        test("sum") {
            val list = listOf(1, 2, 3, 4, 5)
            assertEquals(15, list.sum())
        }

        test("sumOf") {
            val list = listOf("a", "bb", "ccc")
            assertEquals(6, list.sumOf { it.length })
        }

        test("count") {
            val list = listOf(1, 2, 3, 4, 5)
            assertEquals(5, list.count())
            assertEquals(2, list.count { it > 3 })
        }

        // Boolean checks
        test("any") {
            val list = listOf(1, 2, 3)
            assertTrue(list.any())
            assertTrue(list.any { it > 2 })
            assertFalse(list.any { it > 10 })
        }

        test("all") {
            val list = listOf(2, 4, 6)
            assertTrue(list.all { it % 2 == 0 })
            assertFalse(list.all { it > 3 })
        }

        test("none") {
            val list = listOf(1, 2, 3)
            assertFalse(list.none())
            assertTrue(list.none { it > 10 })
        }

        // Element access
        test("first and last") {
            val list = listOf(1, 2, 3)
            assertEquals(1, list.first())
            assertEquals(3, list.last())
            assertEquals(2, list.first { it > 1 })
        }

        test("firstOrNull and lastOrNull") {
            val list = listOf(1, 2, 3)
            assertEquals(1, list.firstOrNull())
            assertNull(list.firstOrNull { it > 10 })

            val empty = emptyList<Int>()
            assertNull(empty.firstOrNull())
        }

        test("find") {
            val list = listOf(1, 2, 3, 4)
            assertEquals(3, list.find { it > 2 })
            assertNull(list.find { it > 10 })
        }

        // Grouping and partitioning
        test("groupBy") {
            val list = listOf(1, 2, 3, 4, 5, 6)
            val groups = list.groupBy { it % 2 }
            assertEquals(listOf(2, 4, 6), groups[0])
            assertEquals(listOf(1, 3, 5), groups[1])
        }

        test("partition") {
            val list = listOf(1, 2, 3, 4, 5)
            val (evens, odds) = list.partition { it % 2 == 0 }
            assertEquals(listOf(2, 4), evens)
            assertEquals(listOf(1, 3, 5), odds)
        }

        // Association
        test("associate") {
            val list = listOf("a", "bb", "ccc")
            val map = list.associate { it to it.length }
            assertEquals(1, map["a"])
            assertEquals(2, map["bb"])
            assertEquals(3, map["ccc"])
        }

        test("associateWith") {
            val list = listOf("a", "bb", "ccc")
            val map = list.associateWith { it.length }
            assertEquals(1, map["a"])
            assertEquals(2, map["bb"])
        }

        test("associateBy") {
            val list = listOf("a", "bb", "ccc")
            val map = list.associateBy { it.length }
            assertEquals("a", map[1])
            assertEquals("bb", map[2])
        }

        // Conversion
        test("toList") {
            val set = hashSetOf(1, 2, 3)
            val list = set.toList()
            assertEquals(3, list.size)
        }

        test("toSet") {
            val list = listOf(1, 2, 2, 3, 3, 3)
            val set = list.toSet()
            assertEquals(3, set.size)
        }

        // String operations
        test("joinToString") {
            val list = listOf(1, 2, 3)
            assertEquals("1, 2, 3", list.joinToString())
            assertEquals("1-2-3", list.joinToString("-"))
            assertEquals("[1, 2, 3]", list.joinToString(prefix = "[", postfix = "]"))
        }

        // Iteration
        test("forEach") {
            val list = listOf(1, 2, 3)
            val collected = mutableListOf<Int>()
            list.forEach { collected.add(it) }
            assertEquals(listOf(1, 2, 3), collected)
        }

        test("forEachIndexed") {
            val list = listOf("a", "b", "c")
            val collected = mutableListOf<String>()
            list.forEachIndexed { i, v -> collected.add("$i:$v") }
            assertEquals(listOf("0:a", "1:b", "2:c"), collected)
        }

        // Indexes
        test("indexOf") {
            val list = listOf(1, 2, 3, 2)
            assertEquals(1, list.indexOf(2))
            assertEquals(-1, list.indexOf(5))
        }

        test("indexOfFirst") {
            val list = listOf(1, 2, 3, 4)
            assertEquals(2, list.indexOfFirst { it > 2 })
        }

        test("indexOfLast") {
            val list = listOf(1, 2, 3, 4)
            assertEquals(3, list.indexOfLast { it > 2 })
        }

        // Distinct
        test("distinct") {
            val list = listOf(1, 2, 2, 3, 3, 3)
            assertEquals(listOf(1, 2, 3), list.distinct())
        }

        test("distinctBy") {
            val list = listOf("a", "bb", "c", "dd")
            val result = list.distinctBy { it.length }
            assertEquals(2, result.size)
        }

        // Take and drop
        test("take") {
            val list = listOf(1, 2, 3, 4, 5)
            assertEquals(listOf(1, 2, 3), list.take(3))
        }

        test("drop") {
            val list = listOf(1, 2, 3, 4, 5)
            assertEquals(listOf(3, 4, 5), list.drop(2))
        }

        test("takeWhile") {
            val list = listOf(1, 2, 3, 4, 5)
            assertEquals(listOf(1, 2), list.takeWhile { it < 3 })
        }

        test("dropWhile") {
            val list = listOf(1, 2, 3, 4, 5)
            assertEquals(listOf(3, 4, 5), list.dropWhile { it < 3 })
        }

        // Reversal
        test("reversed") {
            val list = listOf(1, 2, 3)
            assertEquals(listOf(3, 2, 1), list.reversed())
        }

        // Zip
        test("zip") {
            val a = listOf(1, 2, 3)
            val b = listOf("a", "b", "c")
            val zipped = a.zip(b)
            assertEquals(listOf(1 to "a", 2 to "b", 3 to "c"), zipped)
        }
    }
}
