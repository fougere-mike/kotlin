/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.collections

/**
 * Returns a new read-only map, mapping only the specified key to the
 * specified value.
 */
@SinceKotlin("1.9")
public actual fun <K, V> mapOf(pair: Pair<K, V>): Map<K, V> = hashMapOf(pair)

/**
 * Builds a new read-only Map by populating a HashMap using the given builderAction.
 *
 * Note: BrightScript uses HashMap instead of LinkedHashMap, so iteration order is not guaranteed.
 */
@PublishedApi
@SinceKotlin("1.3")
internal actual inline fun <K, V> buildMapInternal(builderAction: MutableMap<K, V>.() -> Unit): Map<K, V> {
    val map = HashMap<K, V>()
    builderAction(map)
    return map
}

/**
 * Builds a new read-only Map with the specified capacity by populating a HashMap using the given builderAction.
 *
 * Note: BrightScript uses HashMap instead of LinkedHashMap, so iteration order is not guaranteed.
 */
@PublishedApi
@SinceKotlin("1.3")
internal actual inline fun <K, V> buildMapInternal(capacity: Int, builderAction: MutableMap<K, V>.() -> Unit): Map<K, V> {
    val map = HashMap<K, V>(capacity)
    builderAction(map)
    return map
}

/**
 * Calculate the initial capacity of a map, ensuring it can hold the expected size without rehashing.
 *
 * For BrightScript, we use the same simple strategy as JS: return the expected size directly.
 * LinkedHashMap will handle the actual capacity calculation internally.
 */
@PublishedApi
internal actual fun mapCapacity(expectedSize: Int): Int = expectedSize
