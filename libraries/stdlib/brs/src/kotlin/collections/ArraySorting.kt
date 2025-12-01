/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.collections

/**
 * Sorts the given array using the provided comparison function.
 *
 * The sort is _stable_. It means that equal elements preserve their order relative to each other after sorting.
 */
internal fun <T> sortArrayWith(array: Array<out T>, comparison: (T, T) -> Int) {
    if (array.size > 1) {
        @Suppress("UNCHECKED_CAST")
        mergeSort(array as Array<T>, 0, array.lastIndex, Comparator(comparison))
    }
}

/**
 * Sorts the given array using the provided comparator.
 *
 * The sort is _stable_. It means that equal elements preserve their order relative to each other after sorting.
 */
internal fun <T> sortArrayWith(array: Array<out T>, comparator: Comparator<in T>) {
    if (array.size > 1) {
        @Suppress("UNCHECKED_CAST")
        mergeSort(array as Array<T>, 0, array.lastIndex, comparator)
    }
}

/**
 * Sorts the specified range of the array using the provided comparator.
 *
 * The sort is _stable_. It means that equal elements preserve their order relative to each other after sorting.
 *
 * @param fromIndex the start of the range (inclusive) to sort.
 * @param toIndex the end of the range (exclusive) to sort.
 */
internal fun <T> sortArrayWith(array: Array<out T>, fromIndex: Int, toIndex: Int, comparator: Comparator<in T>) {
    if (fromIndex < toIndex - 1) {
        @Suppress("UNCHECKED_CAST")
        mergeSort(array as Array<T>, fromIndex, toIndex - 1, comparator)
    }
}

/**
 * Sorts the given array of comparable elements.
 *
 * The sort is _stable_. It means that equal elements preserve their order relative to each other after sorting.
 */
internal fun <T : Comparable<T>> sortArray(array: Array<out T>) {
    if (array.size > 1) {
        @Suppress("UNCHECKED_CAST")
        mergeSort(array as Array<T>, 0, array.lastIndex, naturalOrder())
    }
}

/**
 * Stable merge sort implementation.
 *
 * BrightScript's RoArray.sort() doesn't support custom comparators,
 * so we always use merge sort for consistent, stable sorting behavior.
 */
private fun <T> mergeSort(array: Array<T>, start: Int, endInclusive: Int, comparator: Comparator<in T>) {
    @Suppress("UNCHECKED_CAST")
    val buffer = arrayOfNulls<Any?>(array.size) as Array<T>
    val result = mergeSort(array, buffer, start, endInclusive, comparator)
    if (result !== array) {
        for (i in start..endInclusive) array[i] = result[i]
    }
}

/**
 * Recursive merge sort that alternates between array and buffer to minimize copies.
 *
 * @param array The source array (or buffer from previous recursion)
 * @param buffer The buffer array (or source from previous recursion)
 * @param start The start index (inclusive)
 * @param end The end index (inclusive)
 * @param comparator The comparator to use for element comparison
 * @return The array containing the sorted elements (either array or buffer)
 */
private fun <T> mergeSort(array: Array<T>, buffer: Array<T>, start: Int, end: Int, comparator: Comparator<in T>): Array<T> {
    if (start == end) {
        return array
    }

    val median = (start + end) / 2
    val left = mergeSort(array, buffer, start, median, comparator)
    val right = mergeSort(array, buffer, median + 1, end, comparator)

    val target = if (left === buffer) array else buffer

    // Merge the two sorted halves
    var leftIndex = start
    var rightIndex = median + 1
    for (i in start..end) {
        when {
            leftIndex <= median && rightIndex <= end -> {
                val leftValue = left[leftIndex]
                val rightValue = right[rightIndex]

                if (comparator.compare(leftValue, rightValue) <= 0) {
                    target[i] = leftValue
                    leftIndex++
                } else {
                    target[i] = rightValue
                    rightIndex++
                }
            }
            leftIndex <= median -> {
                target[i] = left[leftIndex]
                leftIndex++
            }
            else /* rightIndex <= end */ -> {
                target[i] = right[rightIndex]
                rightIndex++
            }
        }
    }

    return target
}
