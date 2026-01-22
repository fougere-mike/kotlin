/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.collections

import kotlin.brs.BrsInline
import kotlin.brs.Dynamic

/**
 * Hash table based implementation of [MutableMap] backed by BrightScript's roAssociativeArray.
 *
 * This class provides a Kotlin-idiomatic interface over BrightScript associative arrays,
 * supporting all standard map operations.
 *
 * Note: BrightScript roAssociativeArray keys are always strings. Non-string keys are
 * converted to strings using their toString() method.
 */
public class HashMap<K, V> : MutableMap<K, V> {

    /**
     * The backing roAssociativeArray storage.
     */
    private var map: Dynamic

    /**
     * Cached size - roAssociativeArray.Count() can be slow.
     */
    private var _size: Int = 0

    /**
     * Creates an empty HashMap.
     */
    public constructor() {
        map = brsCreateAssociativeArray()
        _size = 0
    }

    /**
     * Creates a HashMap with the specified initial capacity.
     * Note: BrightScript roAssociativeArray doesn't support capacity hints.
     */
    public constructor(initialCapacity: Int) {
        require(initialCapacity >= 0) { "Negative initial capacity: $initialCapacity" }
        map = brsCreateAssociativeArray()
        _size = 0
    }

    /**
     * Creates a HashMap containing the entries from the specified map.
     */
    public constructor(original: Map<out K, V>) {
        map = brsCreateAssociativeArray()
        _size = 0
        putAll(original)
    }

    // ==================== Query Operations ====================

    override val size: Int
        get() = _size

    override fun isEmpty(): Boolean = _size == 0

    override fun containsKey(key: K): Boolean {
        val keyStr = keyToString(key)
        return brsDoesExist(keyStr)
    }

    override fun containsValue(value: V): Boolean {
        val keysArray = brsKeys()
        val count = brsArrayCount(keysArray)
        var i = 0
        while (i < count) {
            val keyStr = brsArrayGet<String>(keysArray, i)
            val entry = brsLookup<Dynamic>(keyStr)
            val v = brsEntryGetValue<V>(entry)
            if (v == value) return true
            i++
        }
        return false
    }

    override operator fun get(key: K): V? {
        val keyStr = keyToString(key)
        if (!brsDoesExist(keyStr)) return null
        val entry = brsLookup<Dynamic>(keyStr)
        return brsEntryGetValue(entry)
    }

    // ==================== Modification Operations ====================

    override fun put(key: K, value: V): V? {
        val keyStr = keyToString(key)
        var oldValue: V? = null
        if (brsDoesExist(keyStr)) {
            val oldEntry = brsLookup<Dynamic>(keyStr)
            oldValue = brsEntryGetValue(oldEntry)
        } else {
            _size++
        }
        // Store entry with original key and value for type preservation during iteration
        val entry = brsCreateEntry(key, value)
        brsAddReplace(keyStr, entry)
        return oldValue
    }

    override fun remove(key: K): V? {
        val keyStr = keyToString(key)
        if (!brsDoesExist(keyStr)) return null
        val oldEntry = brsLookup<Dynamic>(keyStr)
        val oldValue = brsEntryGetValue<V>(oldEntry)
        brsDelete(keyStr)
        _size--
        return oldValue
    }

    // ==================== Bulk Operations ====================

    override fun putAll(from: Map<out K, V>) {
        // With type erasure, iterator method names are consistent regardless of generic types
        for (entry in from.entries) {
            @Suppress("UNCHECKED_CAST")
            put(entry.key as K, entry.value)
        }
    }

    override fun clear() {
        brsClear()
        _size = 0
    }

    // ==================== Views ====================

    override val keys: MutableSet<K>
        get() = KeySet(this)

    override val values: MutableCollection<V>
        get() = ValueCollection(this)

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = EntrySet(this)

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
        }
        return true
    }

    override fun hashCode(): Int {
        var h = 0
        for (entry in entries) {
            h += entry.hashCode()
        }
        return h
    }

    override fun toString(): String {
        if (isEmpty()) return "{}"
        val sb = StringBuilder()
        sb.append("{")
        var first = true
        for (entry in entries) {
            if (!first) sb.append(", ")
            first = false
            val k = entry.key
            val v = entry.value
            if (k === this) {
                sb.append("(this Map)")
            } else {
                sb.append(k.toString())
            }
            sb.append("=")
            if (v === this) {
                sb.append("(this Map)")
            } else {
                sb.append(v.toString())
            }
        }
        sb.append("}")
        return sb.toString()
    }

    // ==================== Internal Helpers ====================

    private fun keyToString(key: K): String {
        return key.toString()
    }

    internal fun getKeysArray(): Dynamic = brsKeys()
    internal fun getMap(): Dynamic = map

    // ==================== BrightScript Intrinsics ====================
    // Note: These use m.__get_map() to access the internal storage because @BrsInline
    // doesn't substitute parameters - 'm' in templates refers to the BrightScript implicit 'this'.
    // Property accessors use the '__get_' prefix in generated BrightScript.

    @BrsInline("return CreateObject(\"roAssociativeArray\")")
    private external fun brsCreateAssociativeArray(): Dynamic

    @BrsInline("return m.__get_map().DoesExist(key)")
    private external fun brsDoesExist(key: String): Boolean

    @BrsInline("return m.__get_map().Lookup(key)")
    private external fun <T> brsLookup(key: String): T

    @BrsInline("m.__get_map().AddReplace(key, value)")
    private external fun brsAddReplace(key: String, value: Any?): Unit

    @BrsInline("return m.__get_map().Delete(key)")
    private external fun brsDelete(key: String): Boolean

    @BrsInline("m.__get_map().Clear()")
    private external fun brsClear(): Unit

    @BrsInline("return m.__get_map().Keys()")
    private external fun brsKeys(): Dynamic

    @BrsInline("return arr.Count()")
    private external fun brsArrayCount(arr: Dynamic): Int

    @BrsInline("return arr[index]")
    private external fun <T> brsArrayGet(arr: Dynamic, index: Int): T

    // Entry storage intrinsics - store {k: originalKey, v: value} to preserve key types
    @BrsInline("return {k: key, v: value}")
    private external fun brsCreateEntry(key: Any?, value: Any?): Dynamic

    @BrsInline("return entry.k")
    private external fun <T> brsEntryGetKey(entry: Dynamic): T

    @BrsInline("return entry.v")
    private external fun <T> brsEntryGetValue(entry: Dynamic): T
}

/**
 * Entry implementation for HashMap.
 */
private class HashMapEntry<K, V>(
    private val map: HashMap<K, V>,
    override val key: K
) : MutableMap.MutableEntry<K, V> {

    override val value: V
        get() = map[key]!!

    override fun setValue(newValue: V): V {
        val old = map.put(key, newValue)
        return old!!
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

/**
 * Key set view of HashMap.
 */
private class KeySet<K, V>(
    private val map: HashMap<K, V>
) : MutableSet<K> {

    @BrsInline("return CreateObject(\"roArray\", 0, true)")
    private external fun brsCreateArray(): Dynamic

    @BrsInline("arr.Push(value)")
    private external fun brsArrayPush(arr: Dynamic, value: Any?): Unit

    @BrsInline("return arr.Count()")
    private external fun brsArrayCount(arr: Dynamic): Int

    @BrsInline("return arr[index]")
    private external fun <T> brsArrayGet(arr: Dynamic, index: Int): T

    @BrsInline("return mapObj.Lookup(keyStr)")
    private external fun <T> brsMapLookup(mapObj: Dynamic, keyStr: String): T

    @BrsInline("return entry.k")
    private external fun <T> brsEntryKey(entry: Dynamic): T

    /**
     * Returns an array containing the keys for BrightScript for-each iteration.
     */
    internal val array: Dynamic
        get() {
            val keysArray = map.getKeysArray()
            val mapObj = map.getMap()
            val count = brsArrayCount(keysArray)
            val result = brsCreateArray()
            var i = 0
            while (i < count) {
                val keyStr = brsArrayGet<String>(keysArray, i)
                val entry = brsMapLookup<Dynamic>(mapObj, keyStr)
                brsArrayPush(result, brsEntryKey<K>(entry))
                i++
            }
            return result
        }

    override val size: Int get() = map.size

    override fun isEmpty(): Boolean = map.isEmpty()

    override fun contains(element: K): Boolean = map.containsKey(element)

    override fun containsAll(elements: Collection<K>): Boolean {
        for (element in elements) {
            if (!contains(element)) return false
        }
        return true
    }

    override fun iterator(): MutableIterator<K> = KeyIterator(map)

    override fun add(element: K): Boolean {
        throw UnsupportedOperationException("Add is not supported on keys")
    }

    override fun remove(element: K): Boolean {
        if (!contains(element)) return false
        map.remove(element)
        return true
    }

    override fun addAll(elements: Collection<K>): Boolean {
        throw UnsupportedOperationException("Add is not supported on keys")
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
 * Value collection view of HashMap.
 */
private class ValueCollection<K, V>(
    private val map: HashMap<K, V>
) : MutableCollection<V> {

    @BrsInline("return CreateObject(\"roArray\", 0, true)")
    private external fun brsCreateArray(): Dynamic

    @BrsInline("arr.Push(value)")
    private external fun brsArrayPush(arr: Dynamic, value: Any?): Unit

    @BrsInline("return arr.Count()")
    private external fun brsArrayCount(arr: Dynamic): Int

    @BrsInline("return arr[index]")
    private external fun <T> brsArrayGet(arr: Dynamic, index: Int): T

    @BrsInline("return mapObj.Lookup(keyStr)")
    private external fun <T> brsMapLookup(mapObj: Dynamic, keyStr: String): T

    @BrsInline("return entry.v")
    private external fun <T> brsEntryValue(entry: Dynamic): T

    /**
     * Returns an array containing the values for BrightScript for-each iteration.
     */
    internal val array: Dynamic
        get() {
            val keysArray = map.getKeysArray()
            val mapObj = map.getMap()
            val count = brsArrayCount(keysArray)
            val result = brsCreateArray()
            var i = 0
            while (i < count) {
                val keyStr = brsArrayGet<String>(keysArray, i)
                val entry = brsMapLookup<Dynamic>(mapObj, keyStr)
                brsArrayPush(result, brsEntryValue<V>(entry))
                i++
            }
            return result
        }

    override val size: Int get() = map.size

    override fun isEmpty(): Boolean = map.isEmpty()

    override fun contains(element: V): Boolean = map.containsValue(element)

    override fun containsAll(elements: Collection<V>): Boolean {
        for (element in elements) {
            if (!contains(element)) return false
        }
        return true
    }

    override fun iterator(): MutableIterator<V> = ValueIterator(map)

    override fun add(element: V): Boolean {
        throw UnsupportedOperationException("Add is not supported on values")
    }

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

    override fun addAll(elements: Collection<V>): Boolean {
        throw UnsupportedOperationException("Add is not supported on values")
    }

    override fun removeAll(elements: Collection<V>): Boolean {
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
 * Entry set view of HashMap.
 */
private class EntrySet<K, V>(
    private val map: HashMap<K, V>
) : MutableSet<MutableMap.MutableEntry<K, V>> {

    @BrsInline("return CreateObject(\"roArray\", 0, true)")
    private external fun brsCreateArray(): Dynamic

    @BrsInline("arr.Push(value)")
    private external fun brsArrayPush(arr: Dynamic, value: Any?): Unit

    @BrsInline("return arr.Count()")
    private external fun brsArrayCount(arr: Dynamic): Int

    @BrsInline("return arr[index]")
    private external fun <T> brsArrayGet(arr: Dynamic, index: Int): T

    @BrsInline("return mapObj.Lookup(keyStr)")
    private external fun <T> brsMapLookup(mapObj: Dynamic, keyStr: String): T

    @BrsInline("return entry.k")
    private external fun <T> brsEntryKey(entry: Dynamic): T

    /**
     * Returns an array containing the entries for BrightScript for-each iteration.
     */
    internal val array: Dynamic
        get() {
            val keysArray = map.getKeysArray()
            val mapObj = map.getMap()
            val count = brsArrayCount(keysArray)
            val result = brsCreateArray()
            var i = 0
            while (i < count) {
                val keyStr = brsArrayGet<String>(keysArray, i)
                val entry = brsMapLookup<Dynamic>(mapObj, keyStr)
                @Suppress("UNCHECKED_CAST")
                val key = brsEntryKey<K>(entry)
                brsArrayPush(result, HashMapEntry(map, key))
                i++
            }
            return result
        }

    override val size: Int get() = map.size

    override fun isEmpty(): Boolean = map.isEmpty()

    override fun contains(element: MutableMap.MutableEntry<K, V>): Boolean {
        val value = map[element.key]
        return value != null && value == element.value
    }

    override fun containsAll(elements: Collection<MutableMap.MutableEntry<K, V>>): Boolean {
        for (element in elements) {
            if (!contains(element)) return false
        }
        return true
    }

    override fun iterator(): MutableIterator<MutableMap.MutableEntry<K, V>> = EntryIterator(map)

    override fun add(element: MutableMap.MutableEntry<K, V>): Boolean {
        val hadKey = map.containsKey(element.key)
        map.put(element.key, element.value)
        return !hadKey
    }

    override fun remove(element: MutableMap.MutableEntry<K, V>): Boolean {
        if (!contains(element)) return false
        map.remove(element.key)
        return true
    }

    override fun addAll(elements: Collection<MutableMap.MutableEntry<K, V>>): Boolean {
        var modified = false
        for (element in elements) {
            if (add(element)) modified = true
        }
        return modified
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
 * Base iterator for HashMap that iterates over keys array snapshot.
 */
private abstract class HashMapIterator<K, V, T>(
    protected val map: HashMap<K, V>
) : MutableIterator<T> {

    @BrsInline("return arr.Count()")
    private external fun brsArrayCount(arr: Dynamic): Int

    @BrsInline("return arr[index]")
    private external fun <R> brsArrayGet(arr: Dynamic, index: Int): R

    // Workaround: Use nullable backing field with lazy initialization
    // because init blocks aren't generated for abstract classes and
    // property initializers referencing constructor params generate wrong code
    private var _keysArray: Dynamic? = null
    private var _keyCount: Int = -1
    private var index: Int = 0
    private var lastKey: K? = null

    // Lazily initialize on first access
    private fun getKeysArrayCached(): Dynamic {
        var arr = _keysArray
        if (arr == null) {
            arr = map.getKeysArray()
            _keysArray = arr
        }
        return arr!!
    }

    private fun getKeyCountCached(): Int {
        var count = _keyCount
        if (count < 0) {
            count = brsArrayCount(getKeysArrayCached())
            _keyCount = count
        }
        return count
    }

    @BrsInline("return m.__get_map().__get_map().Lookup(keyStr)")
    private external fun <T> lookupEntry(keyStr: String): T

    @BrsInline("return entry.k")
    private external fun <T> getEntryKey(entry: Dynamic): T

    override fun hasNext(): Boolean = index < getKeyCountCached()

    protected fun nextKey(): K {
        if (!hasNext()) throw NoSuchElementException()
        // Get the string key from keys array
        val keyStr = brsArrayGet<String>(getKeysArrayCached(), index)
        // Look up the entry to get the original key type
        val entry = lookupEntry<Dynamic>(keyStr)
        @Suppress("UNCHECKED_CAST")
        val key = getEntryKey<K>(entry)
        index++
        lastKey = key
        return key
    }

    override fun remove() {
        val key = lastKey ?: throw IllegalStateException("Call next() before remove()")
        map.remove(key)
        lastKey = null
    }
}

private class KeyIterator<K, V>(map: HashMap<K, V>) : HashMapIterator<K, V, K>(map) {
    override fun next(): K = nextKey()
}

private class ValueIterator<K, V>(map: HashMap<K, V>) : HashMapIterator<K, V, V>(map) {
    override fun next(): V = map[nextKey()]!!
}

private class EntryIterator<K, V>(map: HashMap<K, V>) : HashMapIterator<K, V, MutableMap.MutableEntry<K, V>>(map) {
    override fun next(): MutableMap.MutableEntry<K, V> = HashMapEntry(map, nextKey())
}

// ==================== Factory Functions ====================

/**
 * Returns an empty new [HashMap].
 */
public fun <K, V> hashMapOf(): HashMap<K, V> = HashMap()

/**
 * Returns a new [HashMap] with the specified contents, given as a list of pairs.
 */
public fun <K, V> hashMapOf(vararg pairs: Pair<K, V>): HashMap<K, V> {
    val map = HashMap<K, V>(pairs.size)
    for (pair in pairs) {
        map.put(pair.first, pair.second)
    }
    return map
}

/**
 * Returns an empty new [MutableMap].
 */
public fun <K, V> mutableMapOf(): MutableMap<K, V> = HashMap()

/**
 * Returns a new [MutableMap] with the specified contents, given as a list of pairs.
 */
public fun <K, V> mutableMapOf(vararg pairs: Pair<K, V>): MutableMap<K, V> = hashMapOf(*pairs)

/**
 * Returns an empty new read-only [Map].
 */
public fun <K, V> mapOf(): Map<K, V> = emptyMap()

/**
 * Returns a new read-only [Map] with the specified contents, given as a list of pairs.
 */
public fun <K, V> mapOf(vararg pairs: Pair<K, V>): Map<K, V> =
    if (pairs.size == 0) emptyMap() else hashMapOf(*pairs)

/**
 * Returns an empty read-only map.
 */
@Suppress("UNCHECKED_CAST")
public fun <K, V> emptyMap(): Map<K, V> = EmptyMap as Map<K, V>

/**
 * Singleton empty map implementation.
 */
private object EmptyMap : Map<Any?, Any?> {
    override val size: Int get() = 0
    override fun isEmpty(): Boolean = true
    override fun containsKey(key: Any?): Boolean = false
    override fun containsValue(value: Any?): Boolean = false
    override fun get(key: Any?): Any? = null
    override val keys: Set<Any?> get() = EmptySet
    override val values: Collection<Any?> get() = emptyList()
    override val entries: Set<Map.Entry<Any?, Any?>> get() = EmptyEntrySet
    override fun equals(other: Any?): Boolean = other is Map<*, *> && other.isEmpty()
    override fun hashCode(): Int = 0
    override fun toString(): String = "{}"
}

/**
 * Singleton empty set for map keys.
 */
private object EmptySet : Set<Any?> {
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
    override fun iterator(): Iterator<Any?> = EmptyMapIterator
}

private object EmptyMapIterator : Iterator<Any?> {
    override fun hasNext(): Boolean = false
    override fun next(): Any? = throw NoSuchElementException()
}

/**
 * Singleton empty entry set for map entries.
 */
private object EmptyEntrySet : Set<Map.Entry<Any?, Any?>> {
    @BrsInline("return CreateObject(\"roArray\", 0, true)")
    private external fun brsCreateArray(): Dynamic

    /**
     * Returns an empty array for BrightScript for-each iteration.
     */
    internal val array: Dynamic
        get() = brsCreateArray()

    override val size: Int get() = 0
    override fun isEmpty(): Boolean = true
    override fun contains(element: Map.Entry<Any?, Any?>): Boolean = false
    override fun containsAll(elements: Collection<Map.Entry<Any?, Any?>>): Boolean = elements.isEmpty()
    override fun iterator(): Iterator<Map.Entry<Any?, Any?>> = EmptyEntryIterator
}

private object EmptyEntryIterator : Iterator<Map.Entry<Any?, Any?>> {
    override fun hasNext(): Boolean = false
    override fun next(): Map.Entry<Any?, Any?> = throw NoSuchElementException()
}
