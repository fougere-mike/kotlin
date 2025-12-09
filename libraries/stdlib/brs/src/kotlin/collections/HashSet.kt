/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.collections

import kotlin.brs.BrsInline
import kotlin.brs.Dynamic

/**
 * Hash table based implementation of [MutableSet].
 *
 * This implementation uses [HashMap] internally with a dummy value,
 * providing O(1) average time for add, remove, and contains operations.
 */
public class HashSet<E> : MutableSet<E> {

    @BrsInline("return CreateObject(\"roArray\", 0, true)")
    private external fun brsCreateArray(): Dynamic

    @BrsInline("arr.Push(value)")
    private external fun brsArrayPush(arr: Dynamic, value: Any?): Unit

    /**
     * Returns an array containing the elements for BrightScript for-each iteration.
     */
    internal val array: Dynamic
        get() {
            val result = brsCreateArray()
            val iter = map.keys.iterator()
            while (iter.hasNext()) {
                brsArrayPush(result, iter.next())
            }
            return result
        }

    private val map: HashMap<E, Boolean>

    /**
     * Creates an empty HashSet.
     */
    public constructor() {
        map = HashMap()
    }

    /**
     * Creates a HashSet with the specified initial capacity.
     */
    public constructor(initialCapacity: Int) {
        map = HashMap(initialCapacity)
    }

    /**
     * Creates a HashSet containing the elements from the specified collection.
     */
    public constructor(elements: Collection<E>) {
        map = HashMap(elements.size)
        addAll(elements)
    }

    // ==================== Query Operations ====================

    override val size: Int
        get() = map.size

    override fun isEmpty(): Boolean = map.isEmpty()

    override fun contains(element: E): Boolean = map.containsKey(element)

    override fun containsAll(elements: Collection<E>): Boolean {
        for (element in elements) {
            if (!contains(element)) return false
        }
        return true
    }

    override fun iterator(): MutableIterator<E> = SetIterator(map.keys.iterator())

    // ==================== Modification Operations ====================

    override fun add(element: E): Boolean {
        if (contains(element)) return false
        map.put(element, true)
        return true
    }

    override fun remove(element: E): Boolean {
        if (!contains(element)) return false
        map.remove(element)
        return true
    }

    // ==================== Bulk Operations ====================

    override fun addAll(elements: Collection<E>): Boolean {
        var modified = false
        for (element in elements) {
            if (add(element)) modified = true
        }
        return modified
    }

    override fun removeAll(elements: Collection<E>): Boolean {
        var modified = false
        for (element in elements) {
            if (remove(element)) modified = true
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
        map.clear()
    }

    // ==================== Object Operations ====================

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is Set<*>) return false
        if (other.size != size) return false

        for (element in this) {
            if (element !in other) return false
        }
        return true
    }

    override fun hashCode(): Int {
        var h = 0
        for (element in this) {
            h += element?.hashCode() ?: 0
        }
        return h
    }

    override fun toString(): String {
        if (isEmpty()) return "[]"
        val sb = StringBuilder()
        sb.append("[")
        var first = true
        for (element in this) {
            if (!first) sb.append(", ")
            first = false
            if (element === this) {
                sb.append("(this Collection)")
            } else {
                sb.append(element.toString())
            }
        }
        sb.append("]")
        return sb.toString()
    }

    /**
     * Iterator wrapper for the set.
     */
    private class SetIterator<E>(
        private val keyIterator: MutableIterator<E>
    ) : MutableIterator<E> {
        override fun hasNext(): Boolean = keyIterator.hasNext()
        override fun next(): E = keyIterator.next()
        // Workaround: Expression function bodies with side effects don't generate correctly
        override fun remove() {
            keyIterator.remove()
        }
    }
}

// ==================== Factory Functions ====================

/**
 * Returns an empty new [HashSet].
 */
public fun <T> hashSetOf(): HashSet<T> = HashSet()

/**
 * Returns a new [HashSet] with the given elements.
 */
public fun <T> hashSetOf(vararg elements: T): HashSet<T> {
    val set = HashSet<T>(elements.size)
    for (element in elements) {
        set.add(element)
    }
    return set
}

/**
 * Returns an empty new [MutableSet].
 */
public fun <T> mutableSetOf(): MutableSet<T> = HashSet()

/**
 * Returns a new [MutableSet] with the given elements.
 */
public fun <T> mutableSetOf(vararg elements: T): MutableSet<T> = hashSetOf(*elements)

/**
 * Returns an empty new read-only [Set].
 */
public fun <T> setOf(): Set<T> = emptySet()

/**
 * Returns a new read-only [Set] with the given elements.
 */
public fun <T> setOf(vararg elements: T): Set<T> =
    if (elements.size == 0) emptySet() else hashSetOf(*elements)

/**
 * Returns an empty read-only set.
 */
@Suppress("UNCHECKED_CAST")
public fun <T> emptySet(): Set<T> = EmptyHashSet as Set<T>

/**
 * Singleton empty set implementation.
 */
private object EmptyHashSet : Set<Any?> {
    @BrsInline("return CreateObject(\"roArray\", 0, true)")
    private external fun brsCreateArray(): Dynamic

    /**
     * Returns an empty array for BrightScript for-each iteration.
     */
    internal val array: Dynamic
        get() = brsCreateArray()

    override val size: Int get() = 0
    override fun isEmpty(): Boolean = true
    override fun contains(element: Any?): Boolean = false
    override fun containsAll(elements: Collection<Any?>): Boolean = elements.isEmpty()
    override fun iterator(): Iterator<Any?> = EmptySetIterator
    override fun equals(other: Any?): Boolean = other is Set<*> && other.isEmpty()
    override fun hashCode(): Int = 0
    override fun toString(): String = "[]"
}

private object EmptySetIterator : Iterator<Any?> {
    override fun hasNext(): Boolean = false
    override fun next(): Any? = throw NoSuchElementException()
}
