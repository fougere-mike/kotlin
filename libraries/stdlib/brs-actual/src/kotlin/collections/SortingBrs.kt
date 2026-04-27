/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.collections

import kotlin.brsCompareTo
import kotlin.brs.BrsIntrinsic

/**
 * Calls the comparator's compare method directly.
 * This is an intrinsic that generates: comparator.compare_AnyN_AnyN_k_(a, b)
 * Works around closure capture issues with method calls.
 */
@Suppress("BRS_INTRINSIC_USER_DEFINED")
@BrsIntrinsic("brsIntrinsicCallComparator")
private external fun <T> brsInvokeComparator(comparator: Comparator<in T>, a: T, b: T): Int

/**
 * Sorts the array in-place according to the natural order of its elements.
 */
public fun <T : Comparable<T>> Array<T>.sort() {
    if (size <= 1) return
    quickSort(this, 0, size - 1) { a, b -> brsCompareTo(a, b) }
}

/**
 * Sorts the array in-place according to the order specified by the given [comparator].
 */
public fun <T> Array<T>.sortWith(comparator: Comparator<in T>) {
    if (size <= 1) return
    quickSort(this, 0, size - 1) { a, b -> brsInvokeComparator(comparator, a, b) }
}

/**
 * Sorts a range in the array in-place with the given [comparator].
 *
 * @param fromIndex the start of the range (inclusive) to sort, 0 by default.
 * @param toIndex the end of the range (exclusive) to sort, size of this array by default.
 */
public fun <T> Array<T>.sortWith(comparator: Comparator<in T>, fromIndex: Int = 0, toIndex: Int = size) {
    if (fromIndex < 0 || toIndex > size) {
        throw IndexOutOfBoundsException("fromIndex: $fromIndex, toIndex: $toIndex, size: $size")
    }
    if (fromIndex >= toIndex - 1) return
    quickSort(this, fromIndex, toIndex - 1) { a, b -> brsInvokeComparator(comparator, a, b) }
}

/**
 * Sorts the list in-place according to the natural order of its elements.
 */
public fun <T : Comparable<T>> MutableList<T>.sort() {
    if (size <= 1) return
    quickSortList(this, 0, size - 1) { a, b -> brsCompareTo(a, b) }
}

/**
 * Sorts the list in-place according to the order specified by the given [comparator].
 */
public fun <T> MutableList<T>.sortWith(comparator: Comparator<in T>) {
    if (size <= 1) return
    quickSortList(this, 0, size - 1) { a, b -> brsInvokeComparator(comparator, a, b) }
}

/**
 * Returns a list of all elements sorted according to their natural sort order.
 */
public fun <T : Comparable<T>> Iterable<T>.sorted(): List<T> {
    if (this is Collection) {
        if (size <= 1) return this.toList()
        val list = this.toMutableList()
        list.sort()
        return list
    }
    val list = this.toMutableList()
    list.sort()
    return list
}

/**
 * Returns a list of all elements sorted according to the specified [comparator].
 */
public fun <T> Iterable<T>.sortedWith(comparator: Comparator<in T>): List<T> {
    if (this is Collection) {
        if (size <= 1) return this.toList()
        val list = this.toMutableList()
        list.sortWith(comparator)
        return list
    }
    val list = this.toMutableList()
    list.sortWith(comparator)
    return list
}

/**
 * Returns a list of all elements sorted descending according to their natural sort order.
 */
public fun <T : Comparable<T>> Iterable<T>.sortedDescending(): List<T> {
    val list = this.toMutableList()
    quickSortList(list, 0, list.size - 1) { a, b -> brsCompareTo(b, a) }
    return list
}

/**
 * Returns a list of all elements sorted according to natural sort order of the value returned by specified [selector] function.
 */
public fun <T, R : Comparable<R>> Iterable<T>.sortedBy(selector: (T) -> R?): List<T> {
    return sortedWith(compareBy(selector))
}

/**
 * Returns a list of all elements sorted descending according to natural sort order of the value returned by specified [selector] function.
 */
public fun <T, R : Comparable<R>> Iterable<T>.sortedByDescending(selector: (T) -> R?): List<T> {
    return sortedWith(compareByDescending(selector))
}

/**
 * Sorts elements in the list in-place descending according to their natural sort order.
 */
public fun <T : Comparable<T>> MutableList<T>.sortDescending() {
    if (size <= 1) return
    quickSortList(this, 0, size - 1) { a, b -> brsCompareTo(b, a) }
}

/**
 * Sorts elements in the list in-place according to natural sort order of the value returned by specified [selector] function.
 */
public fun <T, R : Comparable<R>> MutableList<T>.sortBy(selector: (T) -> R?) {
    if (size > 1) sortWith(compareBy(selector))
}

/**
 * Sorts elements in the list in-place descending according to natural sort order of the value returned by specified [selector] function.
 */
public fun <T, R : Comparable<R>> MutableList<T>.sortByDescending(selector: (T) -> R?) {
    if (size > 1) sortWith(compareByDescending(selector))
}

// Internal quicksort implementation for arrays
private fun <T> quickSort(array: Array<T>, low: Int, high: Int, compareFn: (T, T) -> Int) {
    if (low >= high) return

    // Use insertion sort for small arrays
    if (high - low < 10) {
        insertionSort(array, low, high, compareFn)
        return
    }

    // Partition
    val pivotIndex = partition(array, low, high, compareFn)

    // Recursively sort partitions
    quickSort(array, low, pivotIndex - 1, compareFn)
    quickSort(array, pivotIndex + 1, high, compareFn)
}

// Partition function for quicksort
private fun <T> partition(array: Array<T>, low: Int, high: Int, compareFn: (T, T) -> Int): Int {
    val pivot = array[high]
    var i = low - 1

    var j = low
    while (j < high) {
        if (compareFn(array[j], pivot) <= 0) {
            i++
            // Swap array[i] and array[j]
            val temp = array[i]
            array[i] = array[j]
            array[j] = temp
        }
        j++
    }

    // Swap array[i+1] and array[high] (pivot)
    val temp = array[i + 1]
    array[i + 1] = array[high]
    array[high] = temp

    return i + 1
}

// Insertion sort for small arrays
private fun <T> insertionSort(array: Array<T>, low: Int, high: Int, compareFn: (T, T) -> Int) {
    var i = low + 1
    while (i <= high) {
        val key = array[i]
        var j = i - 1

        while (j >= low && compareFn(array[j], key) > 0) {
            array[j + 1] = array[j]
            j--
        }
        array[j + 1] = key
        i++
    }
}

// Internal quicksort implementation for lists
private fun <T> quickSortList(list: MutableList<T>, low: Int, high: Int, compareFn: (T, T) -> Int) {
    if (low >= high) return

    // Use insertion sort for small lists
    if (high - low < 10) {
        insertionSortList(list, low, high, compareFn)
        return
    }

    // Partition
    val pivotIndex = partitionList(list, low, high, compareFn)

    // Recursively sort partitions
    quickSortList(list, low, pivotIndex - 1, compareFn)
    quickSortList(list, pivotIndex + 1, high, compareFn)
}

// Partition function for quicksort on lists
private fun <T> partitionList(list: MutableList<T>, low: Int, high: Int, compareFn: (T, T) -> Int): Int {
    val pivot = list[high]
    var i = low - 1

    var j = low
    while (j < high) {
        if (compareFn(list[j], pivot) <= 0) {
            i++
            // Swap list[i] and list[j]
            val temp = list[i]
            list[i] = list[j]
            list[j] = temp
        }
        j++
    }

    // Swap list[i+1] and list[high] (pivot)
    val temp = list[i + 1]
    list[i + 1] = list[high]
    list[high] = temp

    return i + 1
}

// Insertion sort for small lists
private fun <T> insertionSortList(list: MutableList<T>, low: Int, high: Int, compareFn: (T, T) -> Int) {
    var i = low + 1
    while (i <= high) {
        val key = list[i]
        var j = i - 1

        while (j >= low && compareFn(list[j], key) > 0) {
            list[j + 1] = list[j]
            j--
        }
        list[j + 1] = key
        i++
    }
}

/**
 * Reverses elements in the list in-place.
 */
public fun <T> MutableList<T>.reverse() {
    val midPoint = size / 2
    var i = 0
    while (i < midPoint) {
        val tmp = this[i]
        this[i] = this[size - i - 1]
        this[size - i - 1] = tmp
        i++
    }
}

/**
 * Returns a list with elements in reversed order.
 */
public fun <T> Iterable<T>.reversed(): List<T> {
    if (this is Collection && size <= 1) return toList()
    val list = toMutableList()
    list.reverse()
    return list
}

// Shuffle functions temporarily disabled until Random package is properly resolved
// TODO: Re-enable when Random is available

/*
public fun <T> MutableList<T>.shuffle() {
    shuffle(kotlin.random.Random)
}

public fun <T> MutableList<T>.shuffle(random: kotlin.random.Random) {
    var i = size - 1
    while (i > 0) {
        val j = random.nextInt(i + 1)
        val temp = this[i]
        this[i] = this[j]
        this[j] = temp
        i--
    }
}

public fun <T> Iterable<T>.shuffled(): List<T> {
    val list = toMutableList()
    list.shuffle()
    return list
}

public fun <T> Iterable<T>.shuffled(random: kotlin.random.Random): List<T> {
    val list = toMutableList()
    list.shuffle(random)
    return list
}
*/
