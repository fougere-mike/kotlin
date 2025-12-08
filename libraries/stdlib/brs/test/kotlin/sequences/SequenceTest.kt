/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.sequences

import kotlin.test.*

/**
 * Tests for Sequence operations.
 */
fun TestRunner.sequenceTests() {
    suite("Sequence") {
        // Basic transformations
        test("map and filter") {
            val list = listOf(1, 2, 3, 4, 5)
            val result = list.asSequence()
                .map { it * 2 }
                .filter { it > 5 }
                .toList()
            assertEquals(listOf(6, 8, 10), result)
        }

        test("sequence with unsigned") {
            val list = listOf(UInt(1), UInt(2), UInt(3), UInt(4), UInt(5))
            val result = list.asSequence()
                .filter { it > UInt(2) }
                .map { it * UInt(2) }
                .toList()
            assertEquals(listOf(UInt(6), UInt(8), UInt(10)), result)
        }

        test("take") {
            val result = generateSequence(1) { it + 1 }
                .take(5)
                .toList()
            assertEquals(listOf(1, 2, 3, 4, 5), result)
        }

        test("drop") {
            val list = listOf(1, 2, 3, 4, 5)
            val result = list.asSequence()
                .drop(2)
                .toList()
            assertEquals(listOf(3, 4, 5), result)
        }

        test("flatMap") {
            val list = listOf(1, 2, 3)
            val result = list.asSequence()
                .flatMap { n: Int -> (1..n).toList() }
                .toList()
            assertEquals(listOf(1, 1, 2, 1, 2, 3), result)
        }

        test("distinct") {
            val list = listOf(1, 2, 2, 3, 3, 3)
            val result = list.asSequence()
                .distinct()
                .toList()
            assertEquals(listOf(1, 2, 3), result)
        }

        test("sum") {
            val list = listOf(1, 2, 3, 4, 5)
            val result = list.asSequence().sum()
            assertEquals(15, result)
        }

        test("sorted") {
            val list = listOf(5, 2, 4, 1, 3)
            val result = list.asSequence()
                .sorted()
                .toList()
            assertEquals(listOf(1, 2, 3, 4, 5), result)
        }

        test("groupBy") {
            val list = listOf(1, 2, 3, 4, 5, 6)
            val result = list.asSequence()
                .groupBy { it % 2 }
            assertEquals(2, result.size)
            assertEquals(listOf(2, 4, 6), result[0])
            assertEquals(listOf(1, 3, 5), result[1])
        }

        // Aggregate operations
        test("count") {
            val list = listOf(1, 2, 3, 4, 5)
            assertEquals(5, list.asSequence().count())
            assertEquals(3, list.asSequence().count { it > 2 })
        }

        test("all") {
            val list = listOf(2, 4, 6, 8)
            assertTrue(list.asSequence().all { it % 2 == 0 })
            assertFalse(list.asSequence().all { it > 5 })
        }

        test("any") {
            val list = listOf(1, 3, 5, 7)
            assertTrue(list.asSequence().any())
            assertTrue(list.asSequence().any { it > 5 })
            assertFalse(list.asSequence().any { it > 10 })
        }

        test("none") {
            val empty = emptyList<Int>()
            assertTrue(empty.asSequence().none())

            val list = listOf(1, 3, 5, 7)
            assertTrue(list.asSequence().none { it % 2 == 0 })
            assertFalse(list.asSequence().none { it > 5 })
        }

        test("fold") {
            val list = listOf(1, 2, 3, 4, 5)
            val result = list.asSequence().fold(0) { acc, value -> acc + value }
            assertEquals(15, result)
        }

        test("reduce") {
            val list = listOf(1, 2, 3, 4, 5)
            val result = list.asSequence().reduce { acc, value -> acc + value }
            assertEquals(15, result)
        }

        // Element access
        test("first") {
            val list = listOf(1, 2, 3, 4, 5)
            assertEquals(1, list.asSequence().first())
            assertEquals(3, list.asSequence().first { it > 2 })
        }

        test("last") {
            val list = listOf(1, 2, 3, 4, 5)
            assertEquals(5, list.asSequence().last())
            assertEquals(4, list.asSequence().last { it < 5 })
        }

        test("firstOrNull") {
            val empty = emptyList<Int>()
            assertNull(empty.asSequence().firstOrNull())

            val list = listOf(1, 2, 3)
            assertEquals(1, list.asSequence().firstOrNull())
            assertEquals(3, list.asSequence().firstOrNull { it > 2 })
            assertNull(list.asSequence().firstOrNull { it > 10 })
        }

        test("single") {
            val list = listOf(42)
            assertEquals(42, list.asSequence().single())

            val multiList = listOf(1, 2, 3)
            assertEquals(2, multiList.asSequence().single { it == 2 })
        }

        test("elementAt") {
            val list = listOf(1, 2, 3, 4, 5)
            assertEquals(1, list.asSequence().elementAt(0))
            assertEquals(3, list.asSequence().elementAt(2))
            assertEquals(5, list.asSequence().elementAt(4))
        }

        test("elementAtOrNull") {
            val list = listOf(1, 2, 3)
            assertEquals(2, list.asSequence().elementAtOrNull(1))
            assertNull(list.asSequence().elementAtOrNull(10))
            assertNull(list.asSequence().elementAtOrNull(-1))
        }

        test("indexOf") {
            val list = listOf(1, 2, 3, 2, 1)
            assertEquals(0, list.asSequence().indexOf(1))
            assertEquals(1, list.asSequence().indexOf(2))
            assertEquals(-1, list.asSequence().indexOf(5))
        }

        test("contains") {
            val list = listOf(1, 2, 3, 4, 5)
            assertTrue(list.asSequence().contains(3))
            assertFalse(list.asSequence().contains(10))
        }

        test("minMax") {
            val list = listOf(5, 2, 8, 1, 9, 3)
            assertEquals(1, list.asSequence().minOrNull())
            assertEquals(9, list.asSequence().maxOrNull())
        }

        test("partition") {
            val list = listOf(1, 2, 3, 4, 5, 6)
            val (evens, odds) = list.asSequence().partition { it % 2 == 0 }
            assertEquals(listOf(2, 4, 6), evens)
            assertEquals(listOf(1, 3, 5), odds)
        }

        test("joinToString") {
            val list = listOf(1, 2, 3, 4, 5)
            assertEquals("1, 2, 3, 4, 5", list.asSequence().joinToString())
            assertEquals("1-2-3-4-5", list.asSequence().joinToString(separator = "-"))
            assertEquals("[1, 2, 3]", list.asSequence().joinToString(prefix = "[", postfix = "]"))
            assertEquals("1, 2, ...", list.asSequence().joinToString(limit = 2))
        }

        test("onEach") {
            val list = listOf(1, 2, 3)
            var sum = 0
            val result = list.asSequence()
                .onEach { sum += it }
                .toList()
            assertEquals(listOf(1, 2, 3), result)
            assertEquals(6, sum)
        }

        test("withIndex") {
            val list = listOf("a", "b", "c")
            val result = list.asSequence()
                .withIndex()
                .toList()
            assertEquals(0, result[0].index)
            assertEquals("a", result[0].value)
            assertEquals(2, result[2].index)
            assertEquals("c", result[2].value)
        }

        test("mapNotNull") {
            val list = listOf(1, 2, 3, 4, 5)
            val result = list.asSequence()
                .mapNotNull { if (it % 2 == 0) it * 2 else null }
                .toList()
            assertEquals(listOf(4, 8), result)
        }

        test("scan") {
            val list = listOf(1, 2, 3, 4)
            val result = list.asSequence()
                .scan(0) { acc, value -> acc + value }
                .toList()
            assertEquals(listOf(0, 1, 3, 6, 10), result)
        }

        test("forEach") {
            val list = listOf(1, 2, 3)
            val collected = mutableListOf<Int>()
            list.asSequence().forEach { collected.add(it * 2) }
            assertEquals(listOf(2, 4, 6), collected)
        }

        test("forEachIndexed") {
            val list = listOf("a", "b", "c")
            val collected = mutableListOf<String>()
            list.asSequence().forEachIndexed { index, value ->
                collected.add("$index:$value")
            }
            assertEquals(listOf("0:a", "1:b", "2:c"), collected)
        }

        test("find") {
            val list = listOf(1, 2, 3, 4, 5)
            assertEquals(3, list.asSequence().find { it > 2 })
            assertNull(list.asSequence().find { it > 10 })
        }

        test("indexOfFirst") {
            val list = listOf(1, 2, 3, 4, 5)
            assertEquals(2, list.asSequence().indexOfFirst { it > 2 })
            assertEquals(-1, list.asSequence().indexOfFirst { it > 10 })
        }

        test("firstNotNullOf") {
            val list = listOf(1, 2, 3, 4, 5)
            val result = list.asSequence().firstNotNullOf { if (it > 3) it * 2 else null }
            assertEquals(8, result)
        }

        test("groupBy with transform") {
            val list = listOf(1, 2, 3, 4, 5, 6)
            val result = list.asSequence()
                .groupBy({ it % 2 }, { it * 10 })
            assertEquals(2, result.size)
            assertEquals(listOf(20, 40, 60), result[0])
            assertEquals(listOf(10, 30, 50), result[1])
        }
    }
}
