/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.collections

/**
 * Allows to use the index operator for storing values in a mutable map.
 */
public operator fun <K, V> MutableMap<K, V>.set(key: K, value: V) {
    put(key, value)
}

/**
 * Returns the value for the given key, or the result of the [defaultValue] function if there was no entry for the given key.
 */
public inline fun <K, V> Map<K, V>.getOrElse(key: K, defaultValue: () -> V): V {
    return get(key) ?: defaultValue()
}

/**
 * Returns the value for the given [key] if the value is present and not null. Otherwise, calls the [defaultValue]
 * function, puts its result into the map under the given key and returns it.
 */
public inline fun <K, V> MutableMap<K, V>.getOrPut(key: K, defaultValue: () -> V): V {
    val value = get(key)
    return if (value == null && !containsKey(key)) {
        val answer = defaultValue()
        put(key, answer)
        answer
    } else {
        @Suppress("UNCHECKED_CAST")
        value as V
    }
}

/**
 * Returns the value for the given key or the specified default value if the key is not found.
 */
public fun <K, V> Map<K, V>.getOrDefault(key: K, defaultValue: V): V {
    return get(key) ?: defaultValue
}

/**
 * Returns a new read-only map containing all key-value pairs from the original map.
 */
public fun <K, V> Map<out K, V>.toMap(): Map<K, V> {
    val result = HashMap<K, V>()
    for (entry in entries) {
        result.put(entry.key, entry.value)
    }
    return result
}

/**
 * Returns a new mutable map containing all key-value pairs from the original map.
 */
public fun <K, V> Map<out K, V>.toMutableMap(): MutableMap<K, V> {
    val result = HashMap<K, V>()
    for (entry in entries) {
        result.put(entry.key, entry.value)
    }
    return result
}

/**
 * Creates a new read-only map by replacing or adding entries to this map from another [map].
 */
public operator fun <K, V> Map<out K, V>.plus(map: Map<out K, V>): Map<K, V> {
    val result = HashMap<K, V>()
    for (entry in entries) {
        result.put(entry.key, entry.value)
    }
    for (entry in map.entries) {
        result.put(entry.key, entry.value)
    }
    return result
}

/**
 * Creates a new read-only map by replacing or adding an entry to this map from a given key-value [pair].
 */
public operator fun <K, V> Map<out K, V>.plus(pair: Pair<K, V>): Map<K, V> {
    val result = HashMap<K, V>()
    for (entry in entries) {
        result.put(entry.key, entry.value)
    }
    result.put(pair.first, pair.second)
    return result
}
