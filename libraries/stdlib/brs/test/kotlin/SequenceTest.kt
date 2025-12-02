/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.sequences

import kotlin.test.*

class SequenceFromCommonMainTest {
    @Test
    fun testSequenceMapFilter() {
        val list = listOf(1, 2, 3, 4, 5)
        val result = list.asSequence()
            .map { it * 2 }
            .filter { it > 5 }
            .toList()

        assertEquals(listOf(6, 8, 10), result)
    }

    @Test
    fun testSequenceWithUnsigned() {
        val list = listOf(UInt(1), UInt(2), UInt(3), UInt(4), UInt(5))
        val result = list.asSequence()
            .filter { it > UInt(2) }
            .map { it * UInt(2) }
            .toList()

        assertEquals(listOf(UInt(6), UInt(8), UInt(10)), result)
    }

    @Test
    fun testSequenceTake() {
        val result = generateSequence(1) { it + 1 }
            .take(5)
            .toList()

        assertEquals(listOf(1, 2, 3, 4, 5), result)
    }

    @Test
    fun testSequenceDrop() {
        val list = listOf(1, 2, 3, 4, 5)
        val result = list.asSequence()
            .drop(2)
            .toList()

        assertEquals(listOf(3, 4, 5), result)
    }

    @Test
    fun testSequenceFlatMap() {
        val list = listOf(1, 2, 3)
        val result = list.asSequence()
            .flatMap { (1..it).asSequence() }
            .toList()

        assertEquals(listOf(1, 1, 2, 1, 2, 3), result)
    }

    @Test
    fun testSequenceDistinct() {
        val list = listOf(1, 2, 2, 3, 3, 3)
        val result = list.asSequence()
            .distinct()
            .toList()

        assertEquals(listOf(1, 2, 3), result)
    }

    @Test
    fun testSequenceSum() {
        val list = listOf(1, 2, 3, 4, 5)
        val result = list.asSequence().sum()

        assertEquals(15, result)
    }

    @Test
    fun testSequenceSorted() {
        val list = listOf(5, 2, 4, 1, 3)
        val result = list.asSequence()
            .sorted()
            .toList()

        assertEquals(listOf(1, 2, 3, 4, 5), result)
    }

    @Test
    fun testSequenceGroupBy() {
        val list = listOf(1, 2, 3, 4, 5, 6)
        val result = list.asSequence()
            .groupBy { it % 2 }

        assertEquals(2, result.size)
        assertEquals(listOf(2, 4, 6), result[0])
        assertEquals(listOf(1, 3, 5), result[1])
    }

    // ===== New Aggregate Operation Tests =====

    @Test
    fun testSequenceCount() {
        val list = listOf(1, 2, 3, 4, 5)
        assertEquals(5, list.asSequence().count())
        assertEquals(3, list.asSequence().count { it > 2 })
    }

    @Test
    fun testSequenceAll() {
        val list = listOf(2, 4, 6, 8)
        assertTrue(list.asSequence().all { it % 2 == 0 })
        assertFalse(list.asSequence().all { it > 5 })
    }

    @Test
    fun testSequenceAny() {
        val list = listOf(1, 3, 5, 7)
        assertTrue(list.asSequence().any())
        assertTrue(list.asSequence().any { it > 5 })
        assertFalse(list.asSequence().any { it > 10 })
    }

    @Test
    fun testSequenceNone() {
        val empty = emptyList<Int>()
        assertTrue(empty.asSequence().none())

        val list = listOf(1, 3, 5, 7)
        assertTrue(list.asSequence().none { it % 2 == 0 })
        assertFalse(list.asSequence().none { it > 5 })
    }

    @Test
    fun testSequenceFold() {
        val list = listOf(1, 2, 3, 4, 5)
        val result = list.asSequence().fold(0) { acc, value -> acc + value }
        assertEquals(15, result)
    }

    @Test
    fun testSequenceReduce() {
        val list = listOf(1, 2, 3, 4, 5)
        val result = list.asSequence().reduce { acc, value -> acc + value }
        assertEquals(15, result)
    }

    @Test
    fun testSequenceSumOf() {
        val list = listOf("a", "bb", "ccc")
        val result = list.asSequence().sumOf { it.length }
        assertEquals(6, result)
    }

    @Test
    fun testSequenceFirst() {
        val list = listOf(1, 2, 3, 4, 5)
        assertEquals(1, list.asSequence().first())
        assertEquals(3, list.asSequence().first { it > 2 })
    }

    @Test
    fun testSequenceLast() {
        val list = listOf(1, 2, 3, 4, 5)
        assertEquals(5, list.asSequence().last())
        assertEquals(4, list.asSequence().last { it < 5 })
    }

    @Test
    fun testSequenceFirstOrNull() {
        val empty = emptyList<Int>()
        assertNull(empty.asSequence().firstOrNull())

        val list = listOf(1, 2, 3)
        assertEquals(1, list.asSequence().firstOrNull())
        assertEquals(3, list.asSequence().firstOrNull { it > 2 })
        assertNull(list.asSequence().firstOrNull { it > 10 })
    }

    @Test
    fun testSequenceSingle() {
        val list = listOf(42)
        assertEquals(42, list.asSequence().single())

        val multiList = listOf(1, 2, 3)
        assertEquals(2, multiList.asSequence().single { it == 2 })
    }

    @Test
    fun testSequenceElementAt() {
        val list = listOf(1, 2, 3, 4, 5)
        assertEquals(1, list.asSequence().elementAt(0))
        assertEquals(3, list.asSequence().elementAt(2))
        assertEquals(5, list.asSequence().elementAt(4))
    }

    @Test
    fun testSequenceElementAtOrNull() {
        val list = listOf(1, 2, 3)
        assertEquals(2, list.asSequence().elementAtOrNull(1))
        assertNull(list.asSequence().elementAtOrNull(10))
        assertNull(list.asSequence().elementAtOrNull(-1))
    }

    @Test
    fun testSequenceIndexOf() {
        val list = listOf(1, 2, 3, 2, 1)
        assertEquals(0, list.asSequence().indexOf(1))
        assertEquals(1, list.asSequence().indexOf(2))
        assertEquals(-1, list.asSequence().indexOf(5))
    }

    @Test
    fun testSequenceContains() {
        val list = listOf(1, 2, 3, 4, 5)
        assertTrue(list.asSequence().contains(3))
        assertFalse(list.asSequence().contains(10))
    }

    @Test
    fun testSequenceMinMax() {
        val list = listOf(5, 2, 8, 1, 9, 3)
        assertEquals(1, list.asSequence().minOrNull())
        assertEquals(9, list.asSequence().maxOrNull())
    }

    @Test
    fun testSequencePartition() {
        val list = listOf(1, 2, 3, 4, 5, 6)
        val (evens, odds) = list.asSequence().partition { it % 2 == 0 }
        assertEquals(listOf(2, 4, 6), evens)
        assertEquals(listOf(1, 3, 5), odds)
    }

    @Test
    fun testSequenceJoinToString() {
        val list = listOf(1, 2, 3, 4, 5)
        assertEquals("1, 2, 3, 4, 5", list.asSequence().joinToString())
        assertEquals("1-2-3-4-5", list.asSequence().joinToString(separator = "-"))
        assertEquals("[1, 2, 3]", list.asSequence().joinToString(prefix = "[", postfix = "]"))
        assertEquals("1, 2, ...", list.asSequence().joinToString(limit = 2))
    }

    @Test
    fun testSequenceOnEach() {
        val list = listOf(1, 2, 3)
        var sum = 0
        val result = list.asSequence()
            .onEach { sum += it }
            .toList()

        assertEquals(listOf(1, 2, 3), result)
        assertEquals(6, sum)
    }

    @Test
    fun testSequenceWithIndex() {
        val list = listOf("a", "b", "c")
        val result = list.asSequence()
            .withIndex()
            .toList()

        assertEquals(0, result[0].index)
        assertEquals("a", result[0].value)
        assertEquals(2, result[2].index)
        assertEquals("c", result[2].value)
    }

    @Test
    fun testSequenceMapNotNull() {
        val list = listOf(1, 2, 3, 4, 5)
        val result = list.asSequence()
            .mapNotNull { if (it % 2 == 0) it * 2 else null }
            .toList()

        assertEquals(listOf(4, 8), result)
    }

    @Test
    fun testSequenceScan() {
        val list = listOf(1, 2, 3, 4)
        val result = list.asSequence()
            .scan(0) { acc, value -> acc + value }
            .toList()

        assertEquals(listOf(0, 1, 3, 6, 10), result)
    }

    @Test
    fun testSequenceForEach() {
        val list = listOf(1, 2, 3)
        val collected = mutableListOf<Int>()
        list.asSequence().forEach { collected.add(it * 2) }

        assertEquals(listOf(2, 4, 6), collected)
    }

    @Test
    fun testSequenceForEachIndexed() {
        val list = listOf("a", "b", "c")
        val collected = mutableListOf<String>()
        list.asSequence().forEachIndexed { index, value ->
            collected.add("$index:$value")
        }

        assertEquals(listOf("0:a", "1:b", "2:c"), collected)
    }

    @Test
    fun testSequenceFind() {
        val list = listOf(1, 2, 3, 4, 5)
        assertEquals(3, list.asSequence().find { it > 2 })
        assertNull(list.asSequence().find { it > 10 })
    }

    @Test
    fun testSequenceIndexOfFirst() {
        val list = listOf(1, 2, 3, 4, 5)
        assertEquals(2, list.asSequence().indexOfFirst { it > 2 })
        assertEquals(-1, list.asSequence().indexOfFirst { it > 10 })
    }

    @Test
    fun testSequenceFirstNotNullOf() {
        val list = listOf(1, 2, 3, 4, 5)
        val result = list.asSequence().firstNotNullOf { if (it > 3) it * 2 else null }
        assertEquals(8, result)
    }

    @Test
    fun testSequenceGroupByWithTransform() {
        val list = listOf(1, 2, 3, 4, 5, 6)
        val result = list.asSequence()
            .groupBy({ it % 2 }, { it * 10 })

        assertEquals(2, result.size)
        assertEquals(listOf(20, 40, 60), result[0])
        assertEquals(listOf(10, 30, 50), result[1])
    }
}
