/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:kotlin.jvm.JvmMultifileClass
@file:kotlin.jvm.JvmName("ArraysKt")

package kotlin.collections

import kotlin.comparisons.naturalOrder

//
// Sorting functions for arrays
//

/**
 * Sorts the array in-place.
 *
 * @sample samples.collections.Arrays.Sorting.sortArray
 */
public actual fun IntArray.sort(): Unit {
    if (size > 1) {
        @Suppress("UNCHECKED_CAST")
        sortArrayWith(this as Array<Int>, naturalOrder())
    }
}

/**
 * Sorts the array in-place.
 *
 * @sample samples.collections.Arrays.Sorting.sortArray
 */
public actual fun LongArray.sort(): Unit {
    if (size > 1) {
        @Suppress("UNCHECKED_CAST")
        sortArrayWith(this as Array<Long>, naturalOrder())
    }
}

/**
 * Sorts the array in-place.
 *
 * @sample samples.collections.Arrays.Sorting.sortArray
 */
public actual fun ByteArray.sort(): Unit {
    if (size > 1) {
        @Suppress("UNCHECKED_CAST")
        sortArrayWith(this as Array<Byte>, naturalOrder())
    }
}

/**
 * Sorts the array in-place.
 *
 * @sample samples.collections.Arrays.Sorting.sortArray
 */
public actual fun ShortArray.sort(): Unit {
    if (size > 1) {
        @Suppress("UNCHECKED_CAST")
        sortArrayWith(this as Array<Short>, naturalOrder())
    }
}

/**
 * Sorts the array in-place.
 *
 * @sample samples.collections.Arrays.Sorting.sortArray
 */
public actual fun DoubleArray.sort(): Unit {
    if (size > 1) {
        @Suppress("UNCHECKED_CAST")
        sortArrayWith(this as Array<Double>, naturalOrder())
    }
}

/**
 * Sorts the array in-place.
 *
 * @sample samples.collections.Arrays.Sorting.sortArray
 */
public actual fun FloatArray.sort(): Unit {
    if (size > 1) {
        @Suppress("UNCHECKED_CAST")
        sortArrayWith(this as Array<Float>, naturalOrder())
    }
}

/**
 * Sorts the array in-place.
 *
 * @sample samples.collections.Arrays.Sorting.sortArray
 */
public actual fun CharArray.sort(): Unit {
    if (size > 1) {
        @Suppress("UNCHECKED_CAST")
        sortArrayWith(this as Array<Char>, naturalOrder())
    }
}

/**
 * Sorts the array in-place according to the natural order of its elements.
 *
 * The sort is _stable_. It means that equal elements preserve their order relative to each other after sorting.
 *
 * @sample samples.collections.Arrays.Sorting.sortArrayOfComparable
 */
public actual fun <T : Comparable<T>> Array<out T>.sort(): Unit {
    if (size > 1) sortArray(this)
}

/**
 * Sorts a range in the array in-place.
 *
 * The sort is _stable_. It means that equal elements preserve their order relative to each other after sorting.
 *
 * @param fromIndex the start of the range (inclusive) to sort, 0 by default.
 * @param toIndex the end of the range (exclusive) to sort, size of this array by default.
 *
 * @throws IndexOutOfBoundsException if [fromIndex] is less than zero or [toIndex] is greater than the size of this array.
 * @throws IllegalArgumentException if [fromIndex] is greater than [toIndex].
 *
 * @sample samples.collections.Arrays.Sorting.sortRangeOfArrayOfComparable
 */
@SinceKotlin("1.4")
@Suppress("ACTUAL_FUNCTION_WITH_DEFAULT_ARGUMENTS")
public actual fun <T : Comparable<T>> Array<out T>.sort(fromIndex: Int = 0, toIndex: Int = size): Unit {
    AbstractList.checkRangeIndexes(fromIndex, toIndex, size)
    sortArrayWith(this, fromIndex, toIndex, naturalOrder())
}

/**
 * Sorts a range in the array in-place.
 *
 * @param fromIndex the start of the range (inclusive) to sort, 0 by default.
 * @param toIndex the end of the range (exclusive) to sort, size of this array by default.
 *
 * @throws IndexOutOfBoundsException if [fromIndex] is less than zero or [toIndex] is greater than the size of this array.
 * @throws IllegalArgumentException if [fromIndex] is greater than [toIndex].
 *
 * @sample samples.collections.Arrays.Sorting.sortRangeOfArray
 */
@SinceKotlin("1.4")
@Suppress("ACTUAL_FUNCTION_WITH_DEFAULT_ARGUMENTS")
public actual fun ByteArray.sort(fromIndex: Int = 0, toIndex: Int = size): Unit {
    AbstractList.checkRangeIndexes(fromIndex, toIndex, size)
    @Suppress("UNCHECKED_CAST")
    sortArrayWith(this as Array<Byte>, fromIndex, toIndex, naturalOrder())
}

/**
 * Sorts a range in the array in-place.
 *
 * @param fromIndex the start of the range (inclusive) to sort, 0 by default.
 * @param toIndex the end of the range (exclusive) to sort, size of this array by default.
 *
 * @throws IndexOutOfBoundsException if [fromIndex] is less than zero or [toIndex] is greater than the size of this array.
 * @throws IllegalArgumentException if [fromIndex] is greater than [toIndex].
 *
 * @sample samples.collections.Arrays.Sorting.sortRangeOfArray
 */
@SinceKotlin("1.4")
@Suppress("ACTUAL_FUNCTION_WITH_DEFAULT_ARGUMENTS")
public actual fun ShortArray.sort(fromIndex: Int = 0, toIndex: Int = size): Unit {
    AbstractList.checkRangeIndexes(fromIndex, toIndex, size)
    @Suppress("UNCHECKED_CAST")
    sortArrayWith(this as Array<Short>, fromIndex, toIndex, naturalOrder())
}

/**
 * Sorts a range in the array in-place.
 *
 * @param fromIndex the start of the range (inclusive) to sort, 0 by default.
 * @param toIndex the end of the range (exclusive) to sort, size of this array by default.
 *
 * @throws IndexOutOfBoundsException if [fromIndex] is less than zero or [toIndex] is greater than the size of this array.
 * @throws IllegalArgumentException if [fromIndex] is greater than [toIndex].
 *
 * @sample samples.collections.Arrays.Sorting.sortRangeOfArray
 */
@SinceKotlin("1.4")
@Suppress("ACTUAL_FUNCTION_WITH_DEFAULT_ARGUMENTS")
public actual fun IntArray.sort(fromIndex: Int = 0, toIndex: Int = size): Unit {
    AbstractList.checkRangeIndexes(fromIndex, toIndex, size)
    @Suppress("UNCHECKED_CAST")
    sortArrayWith(this as Array<Int>, fromIndex, toIndex, naturalOrder())
}

/**
 * Sorts a range in the array in-place.
 *
 * @param fromIndex the start of the range (inclusive) to sort, 0 by default.
 * @param toIndex the end of the range (exclusive) to sort, size of this array by default.
 *
 * @throws IndexOutOfBoundsException if [fromIndex] is less than zero or [toIndex] is greater than the size of this array.
 * @throws IllegalArgumentException if [fromIndex] is greater than [toIndex].
 *
 * @sample samples.collections.Arrays.Sorting.sortRangeOfArray
 */
@SinceKotlin("1.4")
@Suppress("ACTUAL_FUNCTION_WITH_DEFAULT_ARGUMENTS")
public actual fun LongArray.sort(fromIndex: Int = 0, toIndex: Int = size): Unit {
    AbstractList.checkRangeIndexes(fromIndex, toIndex, size)
    @Suppress("UNCHECKED_CAST")
    sortArrayWith(this as Array<Long>, fromIndex, toIndex, naturalOrder())
}

/**
 * Sorts a range in the array in-place.
 *
 * @param fromIndex the start of the range (inclusive) to sort, 0 by default.
 * @param toIndex the end of the range (exclusive) to sort, size of this array by default.
 *
 * @throws IndexOutOfBoundsException if [fromIndex] is less than zero or [toIndex] is greater than the size of this array.
 * @throws IllegalArgumentException if [fromIndex] is greater than [toIndex].
 *
 * @sample samples.collections.Arrays.Sorting.sortRangeOfArray
 */
@SinceKotlin("1.4")
@Suppress("ACTUAL_FUNCTION_WITH_DEFAULT_ARGUMENTS")
public actual fun FloatArray.sort(fromIndex: Int = 0, toIndex: Int = size): Unit {
    AbstractList.checkRangeIndexes(fromIndex, toIndex, size)
    @Suppress("UNCHECKED_CAST")
    sortArrayWith(this as Array<Float>, fromIndex, toIndex, naturalOrder())
}

/**
 * Sorts a range in the array in-place.
 *
 * @param fromIndex the start of the range (inclusive) to sort, 0 by default.
 * @param toIndex the end of the range (exclusive) to sort, size of this array by default.
 *
 * @throws IndexOutOfBoundsException if [fromIndex] is less than zero or [toIndex] is greater than the size of this array.
 * @throws IllegalArgumentException if [fromIndex] is greater than [toIndex].
 *
 * @sample samples.collections.Arrays.Sorting.sortRangeOfArray
 */
@SinceKotlin("1.4")
@Suppress("ACTUAL_FUNCTION_WITH_DEFAULT_ARGUMENTS")
public actual fun DoubleArray.sort(fromIndex: Int = 0, toIndex: Int = size): Unit {
    AbstractList.checkRangeIndexes(fromIndex, toIndex, size)
    @Suppress("UNCHECKED_CAST")
    sortArrayWith(this as Array<Double>, fromIndex, toIndex, naturalOrder())
}

/**
 * Sorts a range in the array in-place.
 *
 * @param fromIndex the start of the range (inclusive) to sort, 0 by default.
 * @param toIndex the end of the range (exclusive) to sort, size of this array by default.
 *
 * @throws IndexOutOfBoundsException if [fromIndex] is less than zero or [toIndex] is greater than the size of this array.
 * @throws IllegalArgumentException if [fromIndex] is greater than [toIndex].
 *
 * @sample samples.collections.Arrays.Sorting.sortRangeOfArray
 */
@SinceKotlin("1.4")
@Suppress("ACTUAL_FUNCTION_WITH_DEFAULT_ARGUMENTS")
public actual fun CharArray.sort(fromIndex: Int = 0, toIndex: Int = size): Unit {
    AbstractList.checkRangeIndexes(fromIndex, toIndex, size)
    @Suppress("UNCHECKED_CAST")
    sortArrayWith(this as Array<Char>, fromIndex, toIndex, naturalOrder())
}

/**
 * Sorts the array in-place according to the order specified by the given [comparator].
 *
 * The sort is _stable_. It means that equal elements preserve their order relative to each other after sorting.
 */
public actual fun <T> Array<out T>.sortWith(comparator: Comparator<in T>): Unit {
    if (size > 1) sortArrayWith(this, comparator)
}

/**
 * Sorts a range in the array in-place with the given [comparator].
 *
 * The sort is _stable_. It means that equal elements preserve their order relative to each other after sorting.
 *
 * @param fromIndex the start of the range (inclusive) to sort, 0 by default.
 * @param toIndex the end of the range (exclusive) to sort, size of this array by default.
 *
 * @throws IndexOutOfBoundsException if [fromIndex] is less than zero or [toIndex] is greater than the size of this array.
 * @throws IllegalArgumentException if [fromIndex] is greater than [toIndex].
 */
@SinceKotlin("1.4")
@Suppress("ACTUAL_FUNCTION_WITH_DEFAULT_ARGUMENTS")
public actual fun <T> Array<out T>.sortWith(comparator: Comparator<in T>, fromIndex: Int = 0, toIndex: Int = size): Unit {
    AbstractList.checkRangeIndexes(fromIndex, toIndex, size)
    sortArrayWith(this, fromIndex, toIndex, comparator)
}
