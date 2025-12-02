/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.collections

import kotlin.brs.BrsInline
import kotlin.brs.Dynamic
import kotlin.collections.RandomAccess
import kotlin.util.checkElementIndex
import kotlin.util.checkPositionIndex
import kotlin.util.checkRangeIndexes

/**
 * Resizable array implementation of [MutableList] backed by BrightScript's roArray.
 *
 * This class provides a Kotlin-idiomatic interface over BrightScript arrays,
 * supporting all standard list operations with O(1) random access.
 */
public class ArrayList<E> : MutableList<E>, RandomAccess {

    /**
     * The backing roArray storage.
     * In BrightScript, this compiles to a native roArray.
     */
    private var array: Dynamic

    /**
     * Creates an empty ArrayList.
     */
    public constructor() {
        array = brsCreateArray()
    }

    /**
     * Creates an ArrayList with the specified initial capacity.
     * Note: BrightScript roArray is always resizable, so capacity is a hint.
     */
    public constructor(initialCapacity: Int) {
        require(initialCapacity >= 0) { "Negative initial capacity: $initialCapacity" }
        array = brsCreateArray()
    }

    /**
     * Creates an ArrayList containing the elements of the specified collection.
     */
    public constructor(elements: Collection<E>) {
        array = brsCreateArray()
        addAll(elements)
    }

    // ==================== Query Operations ====================

    override val size: Int
        get() = brsArrayCount(array)

    override fun isEmpty(): Boolean = size == 0

    override fun contains(element: E): Boolean = indexOf(element) >= 0

    override fun containsAll(elements: Collection<E>): Boolean {
        for (element in elements) {
            if (!contains(element)) return false
        }
        return true
    }

    // ==================== Positional Access Operations ====================

    override operator fun get(index: Int): E {
        checkElementIndex(index, size)
        return brsArrayGet(array, index)
    }

    override operator fun set(index: Int, element: E): E {
        checkElementIndex(index, size)
        val oldValue = brsArrayGet<E>(array, index)
        brsArraySet(array, index, element)
        return oldValue
    }

    // ==================== Modification Operations ====================

    override fun add(element: E): Boolean {
        brsArrayPush(array, element)
        return true
    }

    override fun add(index: Int, element: E) {
        checkPositionIndex(index, size)
        if (index == size) {
            brsArrayPush(array, element)
        } else {
            // Shift elements and insert
            brsArrayInsertAt(array, index, element)
        }
    }

    override fun remove(element: E): Boolean {
        val index = indexOf(element)
        if (index < 0) return false
        removeAt(index)
        return true
    }

    override fun removeAt(index: Int): E {
        checkElementIndex(index, size)
        val oldValue = brsArrayGet<E>(array, index)
        brsArrayDelete(array, index)
        return oldValue
    }

    // ==================== Bulk Modification Operations ====================

    override fun addAll(elements: Collection<E>): Boolean {
        if (elements.isEmpty()) return false
        for (element in elements) {
            brsArrayPush(array, element)
        }
        return true
    }

    override fun addAll(index: Int, elements: Collection<E>): Boolean {
        checkPositionIndex(index, size)
        if (elements.isEmpty()) return false

        var currentIndex = index
        for (element in elements) {
            add(currentIndex, element)
            currentIndex++
        }
        return true
    }

    override fun removeAll(elements: Collection<E>): Boolean {
        var modified = false
        for (element in elements) {
            while (remove(element)) {
                modified = true
            }
        }
        return modified
    }

    override fun retainAll(elements: Collection<E>): Boolean {
        var modified = false
        val iter = iterator()
        while (iter.hasNext()) {
            if (iter.next() !in elements) {
                iter.remove()
                modified = true
            }
        }
        return modified
    }

    override fun clear() {
        brsArrayClear(array)
    }

    // ==================== Search Operations ====================

    override fun indexOf(element: E): Int {
        for (i in 0 until size) {
            if (brsArrayGet<E>(array, i) == element) return i
        }
        return -1
    }

    override fun lastIndexOf(element: E): Int {
        var i = size - 1
        while (i >= 0) {
            if (brsArrayGet<E>(array, i) == element) return i
            i--
        }
        return -1
    }

    // ==================== Iterator Operations ====================

    override fun iterator(): MutableIterator<E> = ArrayListIterator(this, 0)

    override fun listIterator(): MutableListIterator<E> = ArrayListIterator(this, 0)

    override fun listIterator(index: Int): MutableListIterator<E> {
        checkPositionIndex(index, size)
        return ArrayListIterator(this, index)
    }

    // ==================== View Operations ====================

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> {
        checkRangeIndexes(fromIndex, toIndex, size)
        return SubList(this, fromIndex, toIndex)
    }

    // ==================== Object Operations ====================

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is List<*>) return false
        if (other.size != size) return false

        var i = 0
        for (element in other) {
            if (get(i) != element) return false
            i++
        }
        return true
    }

    override fun hashCode(): Int {
        var hashCode = 1
        for (i in 0 until size) {
            val element = get(i)
            hashCode = 31 * hashCode + (element?.hashCode() ?: 0)
        }
        return hashCode
    }

    override fun toString(): String {
        if (isEmpty()) return "[]"
        val sb = StringBuilder()
        sb.append("[")
        for (i in 0 until size) {
            if (i > 0) sb.append(", ")
            val element = get(i)
            if (element === this) {
                sb.append("(this Collection)")
            } else {
                sb.append(element.toString())
            }
        }
        sb.append("]")
        return sb.toString()
    }

    // ==================== BrightScript Intrinsics ====================

    @BrsInline("return CreateObject(\"roArray\", 0, true)")
    private external fun brsCreateArray(): Dynamic

    @BrsInline("return arr.Count()")
    private external fun brsArrayCount(arr: Dynamic): Int

    @BrsInline("return arr[index]")
    private external fun <T> brsArrayGet(arr: Dynamic, index: Int): T

    @BrsInline("arr[index] = value")
    private external fun brsArraySet(arr: Dynamic, index: Int, value: Any?): Unit

    @BrsInline("arr.Push(value)")
    private external fun brsArrayPush(arr: Dynamic, value: Any?): Unit

    @BrsInline("return arr.Delete(index)")
    private external fun brsArrayDelete(arr: Dynamic, index: Int): Boolean

    @BrsInline("arr.Clear()")
    private external fun brsArrayClear(arr: Dynamic): Unit

    /**
     * Insert at a specific index by shifting elements.
     * BrightScript doesn't have a native insertAt, so we implement it.
     */
    private fun brsArrayInsertAt(arr: Dynamic, index: Int, value: Any?) {
        // Push to extend the array
        brsArrayPush(arr, value)
        // Shift elements from end to index
        var i = size - 1
        while (i > index) {
            brsArraySet(arr, i, brsArrayGet<Any?>(arr, i - 1))
            i--
        }
        brsArraySet(arr, index, value)
    }
}

/**
 * Iterator implementation for ArrayList.
 */
private class ArrayListIterator<E>(
    private val list: ArrayList<E>,
    private var index: Int
) : MutableListIterator<E> {

    private var lastReturned: Int = -1

    override fun hasNext(): Boolean = index < list.size

    override fun next(): E {
        if (!hasNext()) throw NoSuchElementException()
        lastReturned = index
        return list[index++]
    }

    override fun hasPrevious(): Boolean = index > 0

    override fun previous(): E {
        if (!hasPrevious()) throw NoSuchElementException()
        lastReturned = --index
        return list[index]
    }

    override fun nextIndex(): Int = index

    override fun previousIndex(): Int = index - 1

    override fun remove() {
        check(lastReturned >= 0) { "Call next() or previous() before remove()" }
        list.removeAt(lastReturned)
        if (lastReturned < index) index--
        lastReturned = -1
    }

    override fun set(element: E) {
        check(lastReturned >= 0) { "Call next() or previous() before set()" }
        list[lastReturned] = element
    }

    override fun add(element: E) {
        list.add(index++, element)
        lastReturned = -1
    }
}

/**
 * A view of a portion of an ArrayList.
 */
private class SubList<E>(
    private val parent: ArrayList<E>,
    private val fromIndex: Int,
    private var toIndex: Int
) : MutableList<E> {

    override val size: Int
        get() = toIndex - fromIndex

    override fun isEmpty(): Boolean = size == 0

    override fun contains(element: E): Boolean = indexOf(element) >= 0

    override fun containsAll(elements: Collection<E>): Boolean {
        for (element in elements) {
            if (!contains(element)) return false
        }
        return true
    }

    override operator fun get(index: Int): E {
        checkElementIndex(index, size)
        return parent[fromIndex + index]
    }

    override operator fun set(index: Int, element: E): E {
        checkElementIndex(index, size)
        return parent.set(fromIndex + index, element)
    }

    override fun add(element: E): Boolean {
        parent.add(toIndex, element)
        toIndex++
        return true
    }

    override fun add(index: Int, element: E) {
        checkPositionIndex(index, size)
        parent.add(fromIndex + index, element)
        toIndex++
    }

    override fun remove(element: E): Boolean {
        val index = indexOf(element)
        if (index < 0) return false
        removeAt(index)
        return true
    }

    override fun removeAt(index: Int): E {
        checkElementIndex(index, size)
        val result = parent.removeAt(fromIndex + index)
        toIndex--
        return result
    }

    override fun addAll(elements: Collection<E>): Boolean = addAll(size, elements)

    override fun addAll(index: Int, elements: Collection<E>): Boolean {
        checkPositionIndex(index, size)
        if (elements.isEmpty()) return false
        parent.addAll(fromIndex + index, elements)
        toIndex += elements.size
        return true
    }

    override fun removeAll(elements: Collection<E>): Boolean {
        var modified = false
        val iter = iterator()
        while (iter.hasNext()) {
            if (iter.next() in elements) {
                iter.remove()
                modified = true
            }
        }
        return modified
    }

    override fun retainAll(elements: Collection<E>): Boolean {
        var modified = false
        val iter = iterator()
        while (iter.hasNext()) {
            if (iter.next() !in elements) {
                iter.remove()
                modified = true
            }
        }
        return modified
    }

    override fun clear() {
        while (size > 0) {
            removeAt(0)
        }
    }

    override fun indexOf(element: E): Int {
        for (i in 0 until size) {
            if (get(i) == element) return i
        }
        return -1
    }

    override fun lastIndexOf(element: E): Int {
        var i = size - 1
        while (i >= 0) {
            if (get(i) == element) return i
            i--
        }
        return -1
    }

    override fun iterator(): MutableIterator<E> = listIterator()

    override fun listIterator(): MutableListIterator<E> = SubListIterator(this, 0)

    override fun listIterator(index: Int): MutableListIterator<E> {
        checkPositionIndex(index, size)
        return SubListIterator(this, index)
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> {
        checkRangeIndexes(fromIndex, toIndex, size)
        return SubList(parent, this.fromIndex + fromIndex, this.fromIndex + toIndex)
    }
}

/**
 * Iterator for SubList.
 */
private class SubListIterator<E>(
    private val list: SubList<E>,
    private var index: Int
) : MutableListIterator<E> {

    private var lastReturned: Int = -1

    override fun hasNext(): Boolean = index < list.size

    override fun next(): E {
        if (!hasNext()) throw NoSuchElementException()
        lastReturned = index
        return list[index++]
    }

    override fun hasPrevious(): Boolean = index > 0

    override fun previous(): E {
        if (!hasPrevious()) throw NoSuchElementException()
        lastReturned = --index
        return list[index]
    }

    override fun nextIndex(): Int = index

    override fun previousIndex(): Int = index - 1

    override fun remove() {
        check(lastReturned >= 0) { "Call next() or previous() before remove()" }
        list.removeAt(lastReturned)
        if (lastReturned < index) index--
        lastReturned = -1
    }

    override fun set(element: E) {
        check(lastReturned >= 0) { "Call next() or previous() before set()" }
        list[lastReturned] = element
    }

    override fun add(element: E) {
        list.add(index++, element)
        lastReturned = -1
    }
}

// ==================== Factory Functions ====================

/**
 * Returns an empty new [ArrayList].
 */
public fun <T> arrayListOf(): ArrayList<T> = ArrayList()

/**
 * Returns a new [ArrayList] with the given elements.
 */
public fun <T> arrayListOf(vararg elements: T): ArrayList<T> {
    val list = ArrayList<T>(elements.size)
    for (element in elements) {
        list.add(element)
    }
    return list
}

/**
 * Returns a new [MutableList] with the given elements.
 */
public fun <T> mutableListOf(): MutableList<T> = ArrayList()

/**
 * Returns a new [MutableList] with the given elements.
 */
public fun <T> mutableListOf(vararg elements: T): MutableList<T> = arrayListOf(*elements)

/**
 * Returns an empty new read-only [List].
 */
public fun <T> listOf(): List<T> = emptyList()

/**
 * Returns a new read-only [List] with the given elements.
 * Note: In BrightScript, this returns an ArrayList but typed as List for immutability.
 */
public fun <T> listOf(vararg elements: T): List<T> = if (elements.size == 0) emptyList() else arrayListOf(*elements)

/**
 * Returns an empty read-only list.
 */
public fun <T> emptyList(): List<T> = EmptyList

/**
 * Singleton empty list implementation.
 */
private object EmptyList : List<Nothing>, RandomAccess {
    override val size: Int get() = 0
    override fun isEmpty(): Boolean = true
    override fun contains(element: Nothing): Boolean = false
    override fun containsAll(elements: Collection<Nothing>): Boolean = elements.isEmpty()
    override fun get(index: Int): Nothing = throw IndexOutOfBoundsException("Empty list")
    override fun indexOf(element: Nothing): Int = -1
    override fun lastIndexOf(element: Nothing): Int = -1
    override fun iterator(): Iterator<Nothing> = EmptyIterator
    override fun listIterator(): ListIterator<Nothing> = EmptyIterator
    override fun listIterator(index: Int): ListIterator<Nothing> {
        if (index != 0) throw IndexOutOfBoundsException("Index: $index")
        return EmptyIterator
    }
    override fun subList(fromIndex: Int, toIndex: Int): List<Nothing> {
        if (fromIndex != 0 || toIndex != 0) throw IndexOutOfBoundsException()
        return this
    }
    override fun equals(other: Any?): Boolean = other is List<*> && other.isEmpty()
    override fun hashCode(): Int = 1
    override fun toString(): String = "[]"
}

/**
 * Empty iterator singleton.
 */
private object EmptyIterator : ListIterator<Nothing> {
    override fun hasNext(): Boolean = false
    override fun next(): Nothing = throw NoSuchElementException()
    override fun hasPrevious(): Boolean = false
    override fun previous(): Nothing = throw NoSuchElementException()
    override fun nextIndex(): Int = 0
    override fun previousIndex(): Int = -1
}
