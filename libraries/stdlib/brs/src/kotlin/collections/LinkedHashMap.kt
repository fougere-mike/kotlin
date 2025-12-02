/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.collections

import kotlin.brs.BrsInline
import kotlin.brs.Dynamic

/**
 * Hash table based implementation of [MutableMap] that preserves insertion order.
 *
 * This implementation uses BrightScript's roAssociativeArray for O(1) lookups
 * and roArray to track insertion order. This provides predictable iteration order
 * while maintaining the performance characteristics of a hash map.
 *
 * Note: BrightScript roAssociativeArray keys are always strings. Non-string keys are
 * converted to strings using their toString() method.
 */
public open class LinkedHashMap<K, V> : MutableMap<K, V> {

    /**
     * The backing roAssociativeArray storage for O(1) lookups.
     */
    private var map: Dynamic

    /**
     * Array of keys in insertion order.
     */
    private var keyOrder: Dynamic

    /**
     * Cached size.
     */
    private var _size: Int = 0

    /**
     * Creates an empty LinkedHashMap.
     */
    public constructor() {
        map = brsCreateAssociativeArray()
        keyOrder = brsCreateArray()
        _size = 0
    }

    /**
     * Creates a LinkedHashMap with the specified initial capacity.
     * Note: BrightScript doesn't support capacity hints - parameter is ignored.
     */
    public constructor(initialCapacity: Int) {
        require(initialCapacity >= 0) { "Negative initial capacity: $initialCapacity" }
        map = brsCreateAssociativeArray()
        keyOrder = brsCreateArray()
        _size = 0
    }

    /**
     * Creates a LinkedHashMap with the specified initial capacity and load factor.
     * Note: BrightScript doesn't support capacity hints - parameters are ignored.
     */
    public constructor(initialCapacity: Int, loadFactor: Float) {
        require(initialCapacity >= 0) { "Negative initial capacity: $initialCapacity" }
        require(loadFactor > 0) { "Non-positive load factor: $loadFactor" }
        map = brsCreateAssociativeArray()
        keyOrder = brsCreateArray()
        _size = 0
    }

    /**
     * Creates a LinkedHashMap containing the entries from the specified map.
     * The iteration order of entries matches the order in the original map.
     */
    public constructor(original: Map<out K, V>) {
        map = brsCreateAssociativeArray()
        keyOrder = brsCreateArray()
        _size = 0
        putAll(original)
    }

    // ==================== Query Operations ====================

    override val size: Int
        get() = _size

    override fun isEmpty(): Boolean = _size == 0

    override fun containsKey(key: K): Boolean {
        val keyStr = keyToString(key)
        return brsDoesExist(map, keyStr)
    }

    override fun containsValue(value: V): Boolean {
        // Iterate in insertion order
        val count = brsArrayCount(keyOrder)
        var i = 0
        while (i < count) {
            val keyStr = brsArrayGet<String>(keyOrder, i)
            val v = brsLookup<V>(map, keyStr)
            if (v == value) return true
            i++
        }
        return false
    }

    override operator fun get(key: K): V? {
        val keyStr = keyToString(key)
        if (!brsDoesExist(map, keyStr)) return null
        return brsLookup(map, keyStr)
    }

    // ==================== Modification Operations ====================

    override fun put(key: K, value: V): V? {
        val keyStr = keyToString(key)
        val oldValue: V? = if (brsDoesExist(map, keyStr)) {
            brsLookup(map, keyStr)
        } else {
            // New key - add to insertion order array
            brsArrayPush(keyOrder, keyStr)
            _size++
            null
        }
        brsAddReplace(map, keyStr, value)
        return oldValue
    }

    override fun remove(key: K): V? {
        val keyStr = keyToString(key)
        if (!brsDoesExist(map, keyStr)) return null

        val oldValue = brsLookup<V>(map, keyStr)
        brsDelete(map, keyStr)

        // Remove from insertion order array
        val count = brsArrayCount(keyOrder)
        var i = 0
        while (i < count) {
            if (brsArrayGet<String>(keyOrder, i) == keyStr) {
                brsArrayDelete(keyOrder, i)
                break
            }
            i++
        }

        _size--
        return oldValue
    }

    // ==================== Bulk Operations ====================

    override fun putAll(from: Map<out K, V>) {
        for (entry in from.entries) {
            put(entry.key, entry.value)
        }
    }

    override fun clear() {
        brsClear(map)
        brsClearArray(keyOrder)
        _size = 0
    }

    // ==================== Views ====================

    override val keys: MutableSet<K>
        get() = LinkedKeySet(this)

    override val values: MutableCollection<V>
        get() = LinkedValueCollection(this)

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = LinkedEntrySet(this)

    // ==================== Object Operations ====================

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is Map<*, *>) return false
        if (other.size != size) return false

        for (entry in entries) {
            @Suppress("UNCHECKED_CAST")
            val otherMap = other as Map<Any?, Any?>
            val otherValue = otherMap[entry.key]
            if (entry.value != otherValue) return false
            if (otherValue == null && !otherMap.containsKey(entry.key)) return false
        }
        return true
    }

    override fun hashCode(): Int {
        var result = 0
        for (entry in entries) {
            result += entry.hashCode()
        }
        return result
    }

    override fun toString(): String {
        val entries = entries.joinToString(", ", "{", "}") { "${it.key}=${it.value}" }
        return entries
    }

    // ==================== Helper Methods ====================

    /**
     * Converts a key to a string for use in roAssociativeArray.
     */
    private fun keyToString(key: K): String {
        return key.toString()
    }

    // ==================== View Classes ====================

    /**
     * KeySet that maintains insertion order.
     */
    private class LinkedKeySet<K, V>(private val map: LinkedHashMap<K, V>) : MutableSet<K> {
        override val size: Int get() = map.size
        override fun isEmpty(): Boolean = map.isEmpty()
        override fun contains(element: K): Boolean = map.containsKey(element)
        override fun containsAll(elements: Collection<K>): Boolean {
            for (element in elements) {
                if (!contains(element)) return false
            }
            return true
        }

        override fun iterator(): MutableIterator<K> = object : MutableIterator<K> {
            private var currentIndex = 0
            private val orderCount = brsArrayCount(map.keyOrder)
            private var lastReturnedKey: String? = null
            private var canRemove = false

            override fun hasNext(): Boolean = currentIndex < orderCount

            override fun next(): K {
                if (!hasNext()) throw NoSuchElementException()
                val keyStr = brsArrayGet<String>(map.keyOrder, currentIndex)
                lastReturnedKey = keyStr
                currentIndex++
                canRemove = true
                @Suppress("UNCHECKED_CAST")
                return stringToKey(keyStr) as K
            }

            override fun remove() {
                check(canRemove) { "Call next() before removing element" }
                val keyStr = lastReturnedKey ?: throw IllegalStateException()
                @Suppress("UNCHECKED_CAST")
                map.remove(stringToKey(keyStr) as K)
                currentIndex-- // Adjust index after removal
                canRemove = false
            }

            // Helper to reconstruct key from string (simplified - assumes toString() is reversible)
            private fun stringToKey(keyStr: String): Any? {
                // Note: This is a limitation - we can't perfectly reconstruct the original key
                // For now, return the string representation
                return keyStr
            }
        }

        override fun add(element: K): Boolean =
            throw UnsupportedOperationException("Add not supported on key set")

        override fun addAll(elements: Collection<K>): Boolean =
            throw UnsupportedOperationException("Add not supported on key set")

        override fun remove(element: K): Boolean {
            if (!map.containsKey(element)) return false
            map.remove(element)
            return true
        }

        override fun removeAll(elements: Collection<K>): Boolean {
            var modified = false
            for (element in elements) {
                if (remove(element)) modified = true
            }
            return modified
        }

        override fun retainAll(elements: Collection<K>): Boolean {
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
    }

    /**
     * ValueCollection that maintains insertion order.
     */
    private class LinkedValueCollection<K, V>(private val map: LinkedHashMap<K, V>) : MutableCollection<V> {
        override val size: Int get() = map.size
        override fun isEmpty(): Boolean = map.isEmpty()
        override fun contains(element: V): Boolean = map.containsValue(element)
        override fun containsAll(elements: Collection<V>): Boolean {
            for (element in elements) {
                if (!contains(element)) return false
            }
            return true
        }

        override fun iterator(): MutableIterator<V> = object : MutableIterator<V> {
            private var currentIndex = 0
            private val orderCount = brsArrayCount(map.keyOrder)
            private var lastReturnedKey: String? = null
            private var canRemove = false

            override fun hasNext(): Boolean = currentIndex < orderCount

            override fun next(): V {
                if (!hasNext()) throw NoSuchElementException()
                val keyStr = brsArrayGet<String>(map.keyOrder, currentIndex)
                lastReturnedKey = keyStr
                currentIndex++
                canRemove = true
                return brsLookup(map.map, keyStr)
            }

            override fun remove() {
                check(canRemove) { "Call next() before removing element" }
                val keyStr = lastReturnedKey ?: throw IllegalStateException()
                brsDelete(map.map, keyStr)
                brsArrayDelete(map.keyOrder, currentIndex - 1)
                map._size--
                currentIndex--
                canRemove = false
            }
        }

        override fun add(element: V): Boolean =
            throw UnsupportedOperationException("Add not supported on value collection")

        override fun addAll(elements: Collection<V>): Boolean =
            throw UnsupportedOperationException("Add not supported on value collection")

        override fun remove(element: V): Boolean {
            val iter = iterator()
            while (iter.hasNext()) {
                if (iter.next() == element) {
                    iter.remove()
                    return true
                }
            }
            return false
        }

        override fun removeAll(elements: Collection<V>): Boolean {
            var modified = false
            for (element in elements) {
                while (remove(element)) {
                    modified = true
                }
            }
            return modified
        }

        override fun retainAll(elements: Collection<V>): Boolean {
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
    }

    /**
     * EntrySet that maintains insertion order.
     */
    private class LinkedEntrySet<K, V>(private val map: LinkedHashMap<K, V>) :
        MutableSet<MutableMap.MutableEntry<K, V>> {
        override val size: Int get() = map.size
        override fun isEmpty(): Boolean = map.isEmpty()
        override fun contains(element: MutableMap.MutableEntry<K, V>): Boolean {
            val value = map[element.key]
            return value == element.value && (value != null || map.containsKey(element.key))
        }

        override fun containsAll(elements: Collection<MutableMap.MutableEntry<K, V>>): Boolean {
            for (element in elements) {
                if (!contains(element)) return false
            }
            return true
        }

        override fun iterator(): MutableIterator<MutableMap.MutableEntry<K, V>> =
            object : MutableIterator<MutableMap.MutableEntry<K, V>> {
                private var currentIndex = 0
                private val orderCount = brsArrayCount(map.keyOrder)
                private var lastReturnedKey: String? = null
                private var canRemove = false

                override fun hasNext(): Boolean = currentIndex < orderCount

                override fun next(): MutableMap.MutableEntry<K, V> {
                    if (!hasNext()) throw NoSuchElementException()
                    val keyStr = brsArrayGet<String>(map.keyOrder, currentIndex)
                    val value = brsLookup<V>(map.map, keyStr)
                    lastReturnedKey = keyStr
                    currentIndex++
                    canRemove = true

                    @Suppress("UNCHECKED_CAST")
                    return SimpleEntry(keyStr as K, value)
                }

                override fun remove() {
                    check(canRemove) { "Call next() before removing element" }
                    val keyStr = lastReturnedKey ?: throw IllegalStateException()
                    brsDelete(map.map, keyStr)
                    brsArrayDelete(map.keyOrder, currentIndex - 1)
                    map._size--
                    currentIndex--
                    canRemove = false
                }
            }

        override fun add(element: MutableMap.MutableEntry<K, V>): Boolean =
            throw UnsupportedOperationException("Add not supported on entry set")

        override fun addAll(elements: Collection<MutableMap.MutableEntry<K, V>>): Boolean =
            throw UnsupportedOperationException("Add not supported on entry set")

        override fun remove(element: MutableMap.MutableEntry<K, V>): Boolean {
            if (!contains(element)) return false
            map.remove(element.key)
            return true
        }

        override fun removeAll(elements: Collection<MutableMap.MutableEntry<K, V>>): Boolean {
            var modified = false
            for (element in elements) {
                if (remove(element)) modified = true
            }
            return modified
        }

        override fun retainAll(elements: Collection<MutableMap.MutableEntry<K, V>>): Boolean {
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
    }

    /**
     * Simple mutable map entry implementation.
     */
    private class SimpleEntry<K, V>(override val key: K, override var value: V) :
        MutableMap.MutableEntry<K, V> {
        override fun setValue(newValue: V): V {
            val oldValue = value
            value = newValue
            return oldValue
        }

        override fun equals(other: Any?): Boolean {
            if (other !is Map.Entry<*, *>) return false
            return key == other.key && value == other.value
        }

        override fun hashCode(): Int {
            return (key?.hashCode() ?: 0) xor (value?.hashCode() ?: 0)
        }

        override fun toString(): String = "$key=$value"
    }
}

// ==================== BrightScript Intrinsics ====================

@BrsInline("return CreateObject(\"roAssociativeArray\")")
private external fun brsCreateAssociativeArray(): Dynamic

@BrsInline("return CreateObject(\"roArray\", 0, true)")
private external fun brsCreateArray(): Dynamic

@BrsInline("return arr.Count()")
private external fun brsArrayCount(arr: Dynamic): Int

@BrsInline("return arr[index]")
private external fun <T> brsArrayGet(arr: Dynamic, index: Int): T

@BrsInline("arr.Push(value)")
private external fun brsArrayPush(arr: Dynamic, value: Any?)

@BrsInline("arr.Delete(index)")
private external fun brsArrayDelete(arr: Dynamic, index: Int)

@BrsInline("return map.DoesExist(key)")
private external fun brsDoesExist(map: Dynamic, key: String): Boolean

@BrsInline("return map[key]")
private external fun <T> brsLookup(map: Dynamic, key: String): T

@BrsInline("map.AddReplace(key, value)")
private external fun brsAddReplace(map: Dynamic, key: String, value: Any?)

@BrsInline("map.Delete(key)")
private external fun brsDelete(map: Dynamic, key: String)

@BrsInline("map.Clear()")
private external fun brsClear(map: Dynamic)

@BrsInline("arr.Clear()")
private external fun brsClearArray(arr: Dynamic)
