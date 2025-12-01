/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

/*
 * Based on GWT LinkedHashMap
 * Copyright 2008 Google Inc.
 */
package kotlin.collections

/**
 * Hash table based implementation of the [MutableMap] interface,
 * which additionally preserves the insertion order of entries during the iteration.
 *
 * The insertion order is preserved by the corresponding [InternalMap] implementation.
 */
public actual open class LinkedHashMap<K, V> : HashMap<K, V>, MutableMap<K, V> {
    /**
     * Creates a new empty [LinkedHashMap].
     */
    public actual constructor() : super()

    /**
     * Creates a new empty [LinkedHashMap] with the specified initial capacity.
     *
     * @param initialCapacity the initial capacity of the created map.
     *
     * @throws IllegalArgumentException if [initialCapacity] is negative.
     */
    public actual constructor(initialCapacity: Int) : super(initialCapacity)

    /**
     * Creates a new empty [LinkedHashMap] with the specified initial capacity and load factor.
     *
     * @param initialCapacity the initial capacity of the created map.
     * @param loadFactor the load factor of the created map.
     *
     * @throws IllegalArgumentException if [initialCapacity] is negative or [loadFactor] is non-positive.
     */
    public actual constructor(initialCapacity: Int, loadFactor: Float) : super(initialCapacity, loadFactor)

    /**
     * Creates a new [LinkedHashMap] filled with the contents of the specified [original] map.
     *
     * The iteration order of entries in the created map is the same as in the [original] map.
     */
    public actual constructor(original: Map<out K, V>) : super(original)

    internal constructor(internalMap: InternalMap<K, V>) : super(internalMap)

    private object EmptyHolder {
        val value = LinkedHashMap(InternalHashMap<Nothing, Nothing>(0).also { it.build() })
    }

    @PublishedApi
    internal fun build(): Map<K, V> {
        internalMap.build()
        @Suppress("UNCHECKED_CAST")
        return if (size > 0) this else EmptyHolder.value as Map<K, V>
    }

    override fun checkIsMutable() = internalMap.checkIsMutable()
}
