/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.collections

/**
 * Provides a [MutableList] implementation, which uses a resizable array as its backing storage.
 */
public actual open class ArrayList<E> internal constructor(private var array: Array<Any?>) : AbstractMutableList<E>(), MutableList<E>, RandomAccess {
    private companion object {
        private val Empty = ArrayList<Nothing>(0).also { it.isReadOnly = true }
    }

    private var isReadOnly: Boolean = false

    /**
     * Creates a new empty [ArrayList].
     */
    public actual constructor() : this(emptyArray()) {}

    /**
     * Creates a new empty [ArrayList] with the specified initial capacity.
     *
     * @param initialCapacity the initial capacity of the created list.
     * @throws IllegalArgumentException if [initialCapacity] is negative.
     */
    public actual constructor(initialCapacity: Int) : this(emptyArray()) {
        require(initialCapacity >= 0) { "Negative initial capacity: $initialCapacity" }
    }

    /**
     * Creates a new [ArrayList] filled with the elements of the specified collection.
     */
    public actual constructor(elements: Collection<E>) : this(elements.toTypedArray<Any?>()) {}

    @PublishedApi
    internal fun build(): List<E> {
        checkIsMutable()
        isReadOnly = true
        return if (size > 0) this else Empty
    }

    /** Does nothing in this ArrayList implementation. */
    public actual fun trimToSize() {}

    /** Does nothing in this ArrayList implementation. */
    public actual fun ensureCapacity(minCapacity: Int) {}

    actual override val size: Int get() = array.size

    @Suppress("UNCHECKED_CAST")
    actual override fun get(index: Int): E = array[rangeCheck(index)] as E

    actual override fun set(index: Int, element: E): E {
        checkIsMutable()
        rangeCheck(index)
        @Suppress("UNCHECKED_CAST")
        return array[index].apply { array[index] = element } as E
    }

    actual override fun add(element: E): Boolean {
        checkIsMutable()
        val newArray = array.copyOf(array.size + 1)
        newArray[array.size] = element
        array = newArray
        modCount++
        return true
    }

    actual override fun add(index: Int, element: E): Unit {
        checkIsMutable()
        insertionRangeCheck(index)
        val newArray = arrayOfNulls<Any>(array.size + 1)
        array.copyInto(newArray, endIndex = index)
        newArray[index] = element
        array.copyInto(newArray, destinationOffset = index + 1, startIndex = index)
        array = newArray
        modCount++
    }

    actual override fun addAll(elements: Collection<E>): Boolean {
        checkIsMutable()
        if (elements.isEmpty()) return false

        val newArray = array.copyOf(array.size + elements.size)
        var index = array.size
        for (element in elements) {
            newArray[index++] = element
        }
        array = newArray
        modCount++
        return true
    }

    actual override fun addAll(index: Int, elements: Collection<E>): Boolean {
        checkIsMutable()
        insertionRangeCheck(index)

        if (elements.isEmpty()) return false

        val newArray = arrayOfNulls<Any>(array.size + elements.size)
        array.copyInto(newArray, endIndex = index)
        var insertIndex = index
        for (element in elements) {
            newArray[insertIndex++] = element
        }
        array.copyInto(newArray, destinationOffset = insertIndex, startIndex = index)
        array = newArray
        modCount++
        return true
    }

    actual override fun removeAt(index: Int): E {
        checkIsMutable()
        rangeCheck(index)
        @Suppress("UNCHECKED_CAST")
        val element = array[index] as E
        val newArray = arrayOfNulls<Any>(array.size - 1)
        array.copyInto(newArray, endIndex = index)
        array.copyInto(newArray, destinationOffset = index, startIndex = index + 1)
        array = newArray
        modCount++
        return element
    }

    actual override fun remove(element: E): Boolean {
        checkIsMutable()
        for (index in array.indices) {
            if (array[index] == element) {
                val newArray = arrayOfNulls<Any>(array.size - 1)
                array.copyInto(newArray, endIndex = index)
                array.copyInto(newArray, destinationOffset = index, startIndex = index + 1)
                array = newArray
                modCount++
                return true
            }
        }
        return false
    }

    override fun removeRange(fromIndex: Int, toIndex: Int) {
        checkIsMutable()
        val removeCount = toIndex - fromIndex
        if (removeCount == 0) return
        val newArray = arrayOfNulls<Any>(array.size - removeCount)
        array.copyInto(newArray, endIndex = fromIndex)
        array.copyInto(newArray, destinationOffset = fromIndex, startIndex = toIndex)
        array = newArray
        modCount++
    }

    actual override fun clear() {
        checkIsMutable()
        array = emptyArray()
        modCount++
    }


    actual override fun indexOf(element: E): Int = array.indexOf(element)

    actual override fun lastIndexOf(element: E): Int = array.lastIndexOf(element)

    override fun toString(): String = array.contentToString()

    @Suppress("UNCHECKED_CAST")
    override fun <T> toArray(array: Array<T>): Array<T> {
        if (array.size < size) {
            return toArray() as Array<T>
        }

        (this.array as Array<T>).copyInto(array)

        return terminateCollectionToArray(size, array)
    }

    override fun toArray(): Array<Any?> {
        return array.copyOf()
    }

    internal override fun checkIsMutable() {
        if (isReadOnly) throw UnsupportedOperationException()
    }

    private fun rangeCheck(index: Int) = index.apply {
        AbstractList.checkElementIndex(index, size)
    }

    private fun insertionRangeCheck(index: Int) = index.apply {
        AbstractList.checkPositionIndex(index, size)
    }
}
