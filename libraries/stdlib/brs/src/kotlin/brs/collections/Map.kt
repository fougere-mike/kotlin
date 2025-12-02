/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs.collections

/**
 * A collection that holds pairs of objects (keys and values) and supports efficiently retrieving
 * the value corresponding to each key. Map keys are unique; the map holds only one value for each key.
 * Methods in this interface support only read-only access to the map; read-write access is supported through
 * the [MutableMap] interface.
 */
public interface Map<K, out V> {
    /**
     * Returns the number of key/value pairs in the map.
     */
    public val size: Int

    /**
     * Returns `true` if the map is empty (contains no elements), `false` otherwise.
     */
    public fun isEmpty(): Boolean

    /**
     * Returns `true` if the map contains the specified [key].
     */
    public fun containsKey(key: K): Boolean

    /**
     * Returns `true` if the map maps one or more keys to the specified [value].
     */
    public fun containsValue(value: @UnsafeVariance V): Boolean

    /**
     * Returns the value corresponding to the given [key], or `null` if such a key is not present in the map.
     */
    public operator fun get(key: K): V?

    /**
     * Returns a read-only [Set] of all keys in this map.
     */
    public val keys: Set<K>

    /**
     * Returns a read-only [Collection] of all values in this map.
     */
    public val values: Collection<V>

    /**
     * Returns a read-only [Set] of all key/value pairs in this map.
     */
    public val entries: Set<Entry<K, V>>

    /**
     * Represents a key/value pair held by a [Map].
     */
    public interface Entry<out K, out V> {
        /**
         * Returns the key of this key/value pair.
         */
        public val key: K

        /**
         * Returns the value of this key/value pair.
         */
        public val value: V
    }
}

/**
 * A modifiable collection that holds pairs of objects (keys and values) and supports efficiently retrieving
 * the value corresponding to each key. Map keys are unique; the map holds only one value for each key.
 */
public interface MutableMap<K, V> : Map<K, V> {
    /**
     * Associates the specified [value] with the specified [key] in the map.
     *
     * @return the previous value associated with the key, or `null` if the key was not present in the map.
     */
    public fun put(key: K, value: V): V?

    /**
     * Removes the specified key and its corresponding value from this map.
     *
     * @return the previous value associated with the key, or `null` if the key was not present in the map.
     */
    public fun remove(key: K): V?

    /**
     * Updates this map with key/value pairs from the specified map [from].
     */
    public fun putAll(from: Map<out K, V>)

    /**
     * Removes all elements from this map.
     */
    public fun clear()

    /**
     * Returns a [MutableSet] of all keys in this map.
     */
    override val keys: MutableSet<K>

    /**
     * Returns a [MutableCollection] of all values in this map.
     */
    override val values: MutableCollection<V>

    /**
     * Returns a [MutableSet] of all key/value pairs in this map.
     */
    override val entries: MutableSet<MutableEntry<K, V>>

    /**
     * A modifiable [Map.Entry].
     */
    public interface MutableEntry<K, V> : Map.Entry<K, V> {
        /**
         * Changes the value associated with the key of this entry.
         *
         * @return the previous value corresponding to the key.
         */
        public fun setValue(newValue: V): V
    }
}
