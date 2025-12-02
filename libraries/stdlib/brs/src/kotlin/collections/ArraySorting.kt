/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.collections

import kotlin.brs.BrsInline
import kotlin.brs.Dynamic

/**
 * Sorts this list in-place according to the natural order of its elements.
 * The sort is stable: equal elements maintain their relative order.
 */
public fun <T : Comparable<T>> MutableList<T>.sort(): Unit {
    if (size > 1) {
        sortWith(naturalOrder())
    }
}

/**
 * Sorts this list in-place according to the order specified by the given [comparator].
 * The sort is stable: equal elements maintain their relative order.
 */
public fun <T> MutableList<T>.sortWith(comparator: Comparator<in T>): Unit {
    if (size <= 1) return

    // For ArrayList, use BrightScript's native Sort if possible
    if (this is ArrayList) {
        sortArrayList(this, comparator)
    } else {
        // Fallback to insertion sort for small lists or merge sort for larger lists
        if (size < 10) {
            insertionSort(this, 0, size, comparator)
        } else {
            mergeSort(this, comparator)
        }
    }
}

/**
 * Sorts this list in-place in descending order according to the natural order of its elements.
 */
public fun <T : Comparable<T>> MutableList<T>.sortDescending(): Unit {
    sortWith(reverseOrder())
}

/**
 * Sorts this list in-place according to the natural order of the value returned by specified [selector] function.
 */
public inline fun <T, R : Comparable<R>> MutableList<T>.sortBy(crossinline selector: (T) -> R?): Unit {
    if (size > 1) sortWith(compareBy(selector))
}

/**
 * Sorts this list in-place descending according to the natural order of the value returned by specified [selector] function.
 */
public inline fun <T, R : Comparable<R>> MutableList<T>.sortByDescending(crossinline selector: (T) -> R?): Unit {
    if (size > 1) sortWith(compareByDescending(selector))
}

/**
 * Returns a list of all elements sorted according to their natural sort order.
 */
public fun <T : Comparable<T>> Iterable<T>.sorted(): List<T> {
    if (this is Collection) {
        if (size <= 1) return this.toList()
        val list = toMutableList()
        list.sort()
        return list
    }
    return toMutableList().apply { sort() }
}

/**
 * Returns a list of all elements sorted according to the specified [comparator].
 */
public fun <T> Iterable<T>.sortedWith(comparator: Comparator<in T>): List<T> {
    if (this is Collection) {
        if (size <= 1) return this.toList()
        val list = toMutableList()
        list.sortWith(comparator)
        return list
    }
    return toMutableList().apply { sortWith(comparator) }
}

/**
 * Returns a list of all elements sorted descending according to their natural sort order.
 */
public fun <T : Comparable<T>> Iterable<T>.sortedDescending(): List<T> {
    return sortedWith(reverseOrder())
}

/**
 * Returns a list of all elements sorted according to natural sort order of the value returned by specified [selector] function.
 */
public inline fun <T, R : Comparable<R>> Iterable<T>.sortedBy(crossinline selector: (T) -> R?): List<T> {
    return sortedWith(compareBy(selector))
}

/**
 * Returns a list of all elements sorted descending according to natural sort order of the value returned by specified [selector] function.
 */
public inline fun <T, R : Comparable<R>> Iterable<T>.sortedByDescending(crossinline selector: (T) -> R?): List<T> {
    return sortedWith(compareByDescending(selector))
}

// ==================== Internal Sorting Algorithms ====================

/**
 * Sort an ArrayList using optimized approach.
 * For BrightScript, we can't use native Sort on roArray directly with custom comparators,
 * so we use merge sort for efficiency.
 */
private fun <T> sortArrayList(list: ArrayList<T>, comparator: Comparator<in T>) {
    if (list.size <= 1) return
    if (list.size < 10) {
        insertionSort(list, 0, list.size, comparator)
    } else {
        mergeSort(list, comparator)
    }
}

/**
 * Insertion sort - efficient for small lists (< 10 elements).
 * Time complexity: O(n²), but has low overhead and is stable.
 */
private fun <T> insertionSort(
    list: MutableList<T>,
    fromIndex: Int,
    toIndex: Int,
    comparator: Comparator<in T>
) {
    for (i in fromIndex + 1 until toIndex) {
        val key = list[i]
        var j = i - 1

        while (j >= fromIndex && comparator.compare(list[j], key) > 0) {
            list[j + 1] = list[j]
            j--
        }
        list[j + 1] = key
    }
}

/**
 * Merge sort implementation - stable sort with O(n log n) time complexity.
 * This is the primary sorting algorithm for larger lists.
 */
private fun <T> mergeSort(list: MutableList<T>, comparator: Comparator<in T>) {
    val n = list.size
    if (n <= 1) return

    // Create temporary array for merging
    val temp = ArrayList<T>(n)
    for (i in 0 until n) {
        temp.add(list[i])
    }

    mergeSortRecursive(list, temp, 0, n, comparator)
}

/**
 * Recursive merge sort helper.
 */
private fun <T> mergeSortRecursive(
    list: MutableList<T>,
    temp: MutableList<T>,
    left: Int,
    right: Int,
    comparator: Comparator<in T>
) {
    if (right - left <= 1) return

    // Use insertion sort for small subarrays
    if (right - left < 10) {
        insertionSort(list, left, right, comparator)
        return
    }

    val mid = (left + right) / 2

    // Sort both halves
    mergeSortRecursive(list, temp, left, mid, comparator)
    mergeSortRecursive(list, temp, mid, right, comparator)

    // Merge the sorted halves
    merge(list, temp, left, mid, right, comparator)
}

/**
 * Merge two sorted subarrays.
 */
private fun <T> merge(
    list: MutableList<T>,
    temp: MutableList<T>,
    left: Int,
    mid: Int,
    right: Int,
    comparator: Comparator<in T>
) {
    // Copy elements to temp array
    for (i in left until right) {
        temp[i] = list[i]
    }

    var i = left
    var j = mid
    var k = left

    // Merge back to list
    while (i < mid && j < right) {
        if (comparator.compare(temp[i], temp[j]) <= 0) {
            list[k] = temp[i]
            i++
        } else {
            list[k] = temp[j]
            j++
        }
        k++
    }

    // Copy remaining elements from left half
    while (i < mid) {
        list[k] = temp[i]
        i++
        k++
    }

    // Copy remaining elements from right half
    while (j < right) {
        list[k] = temp[j]
        j++
        k++
    }
}

// ==================== Comparator Utilities ====================

/**
 * Returns a comparator that compares [Comparable] objects in natural order.
 */
public fun <T : Comparable<T>> naturalOrder(): Comparator<T> = NaturalOrderComparator as Comparator<T>

/**
 * Returns a comparator that compares [Comparable] objects in reversed natural order.
 */
public fun <T : Comparable<T>> reverseOrder(): Comparator<T> = ReverseOrderComparator as Comparator<T>

/**
 * Returns a comparator that compares values by the result of the selector function, in natural order.
 */
public inline fun <T, K : Comparable<K>> compareBy(crossinline selector: (T) -> K?): Comparator<T> =
    object : Comparator<T> {
        override fun compare(a: T, b: T): Int {
            val aValue = selector(a)
            val bValue = selector(b)
            return compareValues(aValue, bValue)
        }
    }

/**
 * Returns a comparator that compares values by the result of the selector function, in reversed natural order.
 */
public inline fun <T, K : Comparable<K>> compareByDescending(crossinline selector: (T) -> K?): Comparator<T> =
    object : Comparator<T> {
        override fun compare(a: T, b: T): Int {
            val aValue = selector(a)
            val bValue = selector(b)
            return compareValues(bValue, aValue) // Note: reversed order
        }
    }

/**
 * Compares two nullable [Comparable] values.
 * Null is considered less than any value.
 */
public fun <T : Comparable<*>> compareValues(a: T?, b: T?): Int {
    if (a === b) return 0
    if (a == null) return -1
    if (b == null) return 1

    @Suppress("UNCHECKED_CAST")
    return (a as Comparable<Any>).compareTo(b as Any)
}

/**
 * Natural order comparator for Comparable types.
 */
private object NaturalOrderComparator : Comparator<Comparable<Any>> {
    override fun compare(a: Comparable<Any>, b: Comparable<Any>): Int = a.compareTo(b)
}

/**
 * Reverse natural order comparator for Comparable types.
 */
private object ReverseOrderComparator : Comparator<Comparable<Any>> {
    override fun compare(a: Comparable<Any>, b: Comparable<Any>): Int = b.compareTo(a)
}

/**
 * Returns a comparator that imposes the reverse ordering of this comparator.
 */
public fun <T> Comparator<T>.reversed(): Comparator<T> = object : Comparator<T> {
    override fun compare(a: T, b: T): Int = this@reversed.compare(b, a)
}

/**
 * Combines this comparator with the given [comparator] such that the latter is applied only
 * when the former returns zero.
 */
public infix fun <T> Comparator<T>.then(comparator: Comparator<in T>): Comparator<T> = object : Comparator<T> {
    override fun compare(a: T, b: T): Int {
        val result = this@then.compare(a, b)
        return if (result != 0) result else comparator.compare(a, b)
    }
}

/**
 * Creates a comparator using the sequence of functions to calculate a result of comparison.
 * The functions are called sequentially, receive the given values [a] and [b]
 * and return [Comparable] objects. As soon as the [Comparable] objects are not equal,
 * their result is returned from the [Comparator].
 */
public fun <T> compareBy(vararg selectors: (T) -> Comparable<*>?): Comparator<T> = object : Comparator<T> {
    override fun compare(a: T, b: T): Int {
        for (selector in selectors) {
            val aValue = selector(a)
            val bValue = selector(b)
            val result = compareValues(aValue, bValue)
            if (result != 0) return result
        }
        return 0
    }
}

/**
 * Creates a descending comparator using the sequence of functions to calculate a result of comparison.
 */
public fun <T> compareByDescending(vararg selectors: (T) -> Comparable<*>?): Comparator<T> = object : Comparator<T> {
    override fun compare(a: T, b: T): Int {
        for (selector in selectors) {
            val aValue = selector(a)
            val bValue = selector(b)
            val result = compareValues(bValue, aValue) // Reversed
            if (result != 0) return result
        }
        return 0
    }
}
