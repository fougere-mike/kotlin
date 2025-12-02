/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.collections

/**
 * Returns a typed array containing all of the elements of this collection.
 */
public fun <T> Collection<T>.toTypedArray(): Array<T> {
    @Suppress("UNCHECKED_CAST")
    val result = arrayOfNulls<Any?>(this.size) as Array<T>
    var index = 0
    for (element in this) {
        result[index++] = element
    }
    return result
}

/**
 * Returns an array containing all elements of the original array and then all elements of the given [elements] collection.
 */
public operator fun <T> Array<T>.plus(elements: Collection<T>): Array<T> {
    val result = this.copyOf(this.size + elements.size)
    var index = this.size
    for (element in elements) {
        result[index++] = element
    }
    return result
}

/**
 * Returns an array containing all elements of the original array and then the given [element].
 */
public operator fun <T> Array<T>.plus(element: T): Array<T> {
    val result = this.copyOf(this.size + 1)
    result[this.size] = element
    return result
}

/**
 * Returns an array containing all elements of the original array and then all elements of the given [elements] array.
 */
public operator fun <T> Array<T>.plus(elements: Array<out T>): Array<T> {
    val result = this.copyOf(this.size + elements.size)
    var index = this.size
    for (element in elements) {
        result[index++] = element
    }
    return result
}

/**
 * Fills the array or its subrange with the specified [element] value.
 *
 * @param fromIndex the start of the range (inclusive) to fill, 0 by default.
 * @param toIndex the end of the range (exclusive) to fill, size of this array by default.
 */
public fun <T> Array<T>.fill(element: T, fromIndex: Int = 0, toIndex: Int = size) {
    if (fromIndex < 0 || toIndex > size) {
        throw IndexOutOfBoundsException("fromIndex: $fromIndex, toIndex: $toIndex, size: $size")
    }
    if (fromIndex > toIndex) {
        throw IllegalArgumentException("fromIndex: $fromIndex > toIndex: $toIndex")
    }
    for (i in fromIndex until toIndex) {
        this[i] = element
    }
}

/**
 * Returns new array which is a copy of the original array.
 */
public fun <T> Array<T>.copyOf(): Array<T> {
    return copyOf(size)
}

/**
 * Returns new array which is a copy of the original array, resized to the given [newSize].
 * The copy is either truncated or padded at the end with `null` values if necessary.
 */
@Suppress("UNCHECKED_CAST")
public fun <T> Array<T>.copyOf(newSize: Int): Array<T> {
    if (newSize < 0) {
        throw IllegalArgumentException("Invalid new array size: $newSize")
    }
    val result = arrayOfNulls<Any?>(newSize) as Array<T>
    val copySize = if (newSize < size) newSize else size
    for (i in 0 until copySize) {
        result[i] = this[i]
    }
    return result
}

/**
 * Returns new array which is a copy of range of original array.
 */
public fun <T> Array<T>.copyOfRange(fromIndex: Int, toIndex: Int): Array<T> {
    if (fromIndex < 0 || toIndex > size) {
        throw IndexOutOfBoundsException("fromIndex: $fromIndex, toIndex: $toIndex, size: $size")
    }
    if (fromIndex > toIndex) {
        throw IllegalArgumentException("fromIndex: $fromIndex > toIndex: $toIndex")
    }

    @Suppress("UNCHECKED_CAST")
    val result = arrayOfNulls<Any?>(toIndex - fromIndex) as Array<T>
    var resultIndex = 0
    for (i in fromIndex until toIndex) {
        result[resultIndex++] = this[i]
    }
    return result
}

/**
 * Reverses elements in the array in-place.
 */
public fun <T> Array<T>.reverse() {
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
 * Reverses elements of the array in the specified range in-place.
 *
 * @param fromIndex the start of the range (inclusive) to reverse.
 * @param toIndex the end of the range (exclusive) to reverse.
 */
public fun <T> Array<T>.reverse(fromIndex: Int, toIndex: Int) {
    if (fromIndex < 0 || toIndex > size) {
        throw IndexOutOfBoundsException("fromIndex: $fromIndex, toIndex: $toIndex, size: $size")
    }
    if (fromIndex >= toIndex) {
        return
    }

    val midPoint = (fromIndex + toIndex) / 2
    var i = fromIndex
    while (i < midPoint) {
        val tmp = this[i]
        this[i] = this[toIndex - i + fromIndex - 1]
        this[toIndex - i + fromIndex - 1] = tmp
        i++
    }
}

/**
 * Returns a list containing all elements of the original array.
 */
public fun <T> Array<out T>.toList(): List<T> {
    val result = ArrayList<T>(this.size)
    for (element in this) {
        result.add(element)
    }
    return result
}

/**
 * Returns a mutable list containing all elements of the original array.
 */
public fun <T> Array<out T>.toMutableList(): MutableList<T> {
    val result = ArrayList<T>(this.size)
    for (element in this) {
        result.add(element)
    }
    return result
}

/**
 * Returns a set containing all distinct elements from the given array.
 */
public fun <T> Array<out T>.toSet(): Set<T> {
    val result = HashSet<T>()
    for (element in this) {
        result.add(element)
    }
    return result
}

/**
 * Returns a mutable set containing all distinct elements from the given array.
 */
public fun <T> Array<out T>.toMutableSet(): MutableSet<T> {
    val result = HashSet<T>()
    for (element in this) {
        result.add(element)
    }
    return result
}

/**
 * Returns `true` if the array is not empty.
 */
public inline fun <T> Array<out T>.isNotEmpty(): Boolean = !isEmpty()

/**
 * Returns `true` if the array is empty.
 */
public inline fun <T> Array<out T>.isEmpty(): Boolean = size == 0

/**
 * Returns the array if it's not `null`, or an empty array otherwise.
 */
public inline fun <T> Array<out T>?.orEmpty(): Array<out T> = this ?: emptyArray<T>()

/**
 * Returns an empty array.
 */
@Suppress("UNCHECKED_CAST")
public inline fun <T> emptyArray(): Array<T> = arrayOfNulls<Any?>(0) as Array<T>
