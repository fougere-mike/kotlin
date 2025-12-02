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
}
