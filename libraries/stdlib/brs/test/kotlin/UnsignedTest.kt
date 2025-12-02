/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.unsigned

import kotlin.test.*

class UnsignedBasicsTest {
    @Test
    fun testUIntBasics() {
        val a = UInt(1)
        val b = UInt(2)
        assertEquals(UInt(3), a + b)
        assertEquals(UInt(0), UInt.MIN_VALUE)
        assertEquals(UInt(-1), UInt.MAX_VALUE)
    }

    @Test
    fun testUIntComparison() {
        val a = UInt(1)
        val b = UInt(2)
        assertTrue(a < b)
        assertTrue(b > a)
        assertEquals(0, a.compareTo(a))
    }

    @Test
    fun testUIntArithmetic() {
        val a = UInt(10)
        val b = UInt(3)
        assertEquals(UInt(13), a + b)
        assertEquals(UInt(7), a - b)
        assertEquals(UInt(30), a * b)
        assertEquals(UInt(3), a / b)
        assertEquals(UInt(1), a % b)
    }

    @Test
    fun testUIntConversions() {
        val i = -1
        val u = i.toUInt()
        assertEquals(UInt.MAX_VALUE, u)
        assertEquals(4294967295u, u.toULong().data)
    }

    @Test
    fun testULongBasics() {
        val a = ULong(1)
        val b = ULong(2)
        assertEquals(ULong(3), a + b)
        assertEquals(ULong(0), ULong.MIN_VALUE)
        assertEquals(ULong(-1), ULong.MAX_VALUE)
    }

    @Test
    fun testUByteBasics() {
        val a = UByte(1)
        val b = UByte(2)
        assertEquals(UInt(3), a + b)
        assertEquals(UByte(0), UByte.MIN_VALUE)
        assertEquals(UByte(-1), UByte.MAX_VALUE)
    }

    @Test
    fun testUShortBasics() {
        val a = UShort(1)
        val b = UShort(2)
        assertEquals(UInt(3), a + b)
        assertEquals(UShort(0), UShort.MIN_VALUE)
        assertEquals(UShort(-1), UShort.MAX_VALUE)
    }
}

class UnsignedArrayTest {
    @Test
    fun testUIntArray() {
        val arr = UIntArray(3) { UInt(it + 1) }
        assertEquals(3, arr.size)
        assertEquals(UInt(1), arr[0])
        assertEquals(UInt(2), arr[1])
        assertEquals(UInt(3), arr[2])

        arr[1] = UInt(10)
        assertEquals(UInt(10), arr[1])
    }

    @Test
    fun testUIntArrayCreation() {
        val arr = uintArrayOf(UInt(1), UInt(2), UInt(3))
        assertEquals(3, arr.size)
        assertEquals(UInt(1), arr[0])
    }

    @Test
    fun testULongArray() {
        val arr = ULongArray(2) { ULong(it.toLong()) }
        assertEquals(2, arr.size)
        assertEquals(ULong(0), arr[0])
        assertEquals(ULong(1), arr[1])
    }

    @Test
    fun testUByteArray() {
        val arr = UByteArray(2) { UByte(it.toByte()) }
        assertEquals(2, arr.size)
        assertEquals(UByte(0), arr[0])
    }

    @Test
    fun testUShortArray() {
        val arr = UShortArray(2) { UShort(it.toShort()) }
        assertEquals(2, arr.size)
        assertEquals(UShort(0), arr[0])
    }
}

class UnsignedRangeTest {
    @Test
    fun testUIntRange() {
        val range = UInt(1)..UInt(5)
        val list = range.toList()
        assertEquals(5, list.size)
        assertEquals(UInt(1), list[0])
        assertEquals(UInt(5), list[4])
    }

    @Test
    fun testUIntRangeUntil() {
        val range = UInt(1) until UInt(5)
        val list = range.toList()
        assertEquals(4, list.size)
        assertEquals(UInt(1), list[0])
        assertEquals(UInt(4), list[3])
    }

    @Test
    fun testUIntRangeDownTo() {
        val range = UInt(5) downTo UInt(1)
        val list = range.toList()
        assertEquals(5, list.size)
        assertEquals(UInt(5), list[0])
        assertEquals(UInt(1), list[4])
    }

    @Test
    fun testUIntRangeStep() {
        val range = (UInt(1)..UInt(10)) step 2
        val list = range.toList()
        assertEquals(5, list.size)
        assertEquals(UInt(1), list[0])
        assertEquals(UInt(3), list[1])
        assertEquals(UInt(9), list[4])
    }

    @Test
    fun testULongRange() {
        val range = ULong(1)..ULong(3)
        val list = range.toList()
        assertEquals(3, list.size)
        assertEquals(ULong(1), list[0])
        assertEquals(ULong(3), list[2])
    }
}
