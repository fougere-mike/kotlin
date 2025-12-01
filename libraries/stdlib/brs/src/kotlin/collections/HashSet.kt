/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
/*
 * Based on GWT HashSet
 * Copyright 2008 Google Inc.
 */

package kotlin.collections

/**
 * The implementation of the [MutableSet] interface, backed by a [InternalMap] implementation.
 */
public actual open class HashSet<E> : AbstractMutableSet<E>, MutableSet<E> {

    internal val internalMap: InternalMap<E, Boolean>

    /**
     * Internal constructor to specify the underlying map.
     */
    internal constructor(map: InternalMap<E, Boolean>) {
        internalMap = map
    }

    /**
     * Creates a new empty [HashSet].
     */
    public actual constructor() : this(InternalHashMap())

    /**
     * Creates a new [HashSet] filled with the elements of the specified collection.
     */
    public actual constructor(elements: Collection<E>) : this(InternalHashMap(elements.size)) {
        for (element in elements) {
            internalMap.put(element, true)
        }
    }

    /**
     * Creates a new empty [HashSet] with the specified initial capacity and load factor.
     *
     * @param initialCapacity the initial capacity of the created set.
     * @param loadFactor the load factor of the created set.
     *
     * @throws IllegalArgumentException if [initialCapacity] is negative or [loadFactor] is non-positive.
     */
    public actual constructor(initialCapacity: Int, loadFactor: Float) : this(InternalHashMap(initialCapacity, loadFactor))

    /**
     * Creates a new empty [HashSet] with the specified initial capacity.
     *
     * @param initialCapacity the initial capacity of the created set.
     *
     * @throws IllegalArgumentException if [initialCapacity] is negative.
     */
    public actual constructor(initialCapacity: Int) : this(initialCapacity, 1.0f)

    actual override fun add(element: E): Boolean {
        return internalMap.put(element, true) == null
    }

    actual override fun clear() {
        internalMap.clear()
    }

    actual override operator fun contains(element: E): Boolean = internalMap.contains(element)

    actual override fun isEmpty(): Boolean = internalMap.size == 0

    actual override fun iterator(): MutableIterator<E> = internalMap.keysIterator()

    actual override fun remove(element: E): Boolean = internalMap.remove(element) != null

    actual override val size: Int get() = internalMap.size
}
