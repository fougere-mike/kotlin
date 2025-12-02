/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.collections

/**
 * Hash table based implementation of [MutableSet] that preserves insertion order.
 *
 * This implementation uses [LinkedHashMap] internally with a dummy value,
 * providing O(1) average time for add, remove, and contains operations
 * while maintaining predictable iteration order.
 */
public open class LinkedHashSet<E> : MutableSet<E> {

    private val map: LinkedHashMap<E, Boolean>

    /**
     * Creates an empty LinkedHashSet.
     */
    public constructor() {
        map = LinkedHashMap()
    }

    /**
     * Creates a LinkedHashSet with the specified initial capacity.
     */
    public constructor(initialCapacity: Int) {
        map = LinkedHashMap(initialCapacity)
    }

    /**
     * Creates a LinkedHashSet with the specified initial capacity and load factor.
     */
    public constructor(initialCapacity: Int, loadFactor: Float) {
        map = LinkedHashMap(initialCapacity, loadFactor)
    }

    /**
     * Creates a LinkedHashSet containing the elements from the specified collection.
     * The iteration order matches the order in the original collection.
     */
    public constructor(elements: Collection<E>) {
        map = LinkedHashMap(elements.size)
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
        return containsAll(other)
    }

    override fun hashCode(): Int {
        var result = 0
        for (element in this) {
            result += element?.hashCode() ?: 0
        }
        return result
    }

    override fun toString(): String {
        return joinToString(", ", "[", "]")
    }

    // ==================== Helper Classes ====================

    /**
     * Iterator wrapper for set elements.
     */
    private class SetIterator<E>(private val keyIterator: MutableIterator<E>) : MutableIterator<E> {
        override fun hasNext(): Boolean = keyIterator.hasNext()
        override fun next(): E = keyIterator.next()
        override fun remove() = keyIterator.remove()
    }
}
