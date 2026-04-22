/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin

/**
 * An array of unsigned ints. When targeting BrightScript, instances of this class are represented as `IntArray`.
 */
@SinceKotlin("1.3")
public class UIntArray
@PublishedApi
internal constructor(@PublishedApi internal val storage: IntArray) : Collection<UInt> {

    /** Creates a new array of the specified [size], with all elements initialized to zero. */
    public constructor(size: Int) : this(IntArray(size))

    /**
     * Returns the array element at the given [index]. This method can be called using the index operator.
     */
    public operator fun get(index: Int): UInt = storage[index].toUInt()

    /**
     * Sets the element at the given [index] to the given [value]. This method can be called using the index operator.
     */
    public operator fun set(index: Int, value: UInt) {
        storage[index] = value.toInt()
    }

    /** Returns the number of elements in the array. */
    public override val size: Int get() = storage.size

    /** Creates an iterator over the elements of the array. */
    @Suppress("BRS_NAME_CASE_CLASH")
    public override operator fun iterator(): kotlin.collections.Iterator<UInt> = Iterator(storage)

    @Suppress("BRS_NAME_CASE_CLASH")
    private class Iterator(private val array: IntArray) : kotlin.collections.Iterator<UInt> {
        private var index = 0
        override fun hasNext() = index < array.size
        override fun next() = if (index < array.size) array[index++].toUInt() else throw NoSuchElementException(index.toString())
    }

    override fun contains(element: UInt): Boolean {
        val target = element.toInt()
        for (i in 0 until storage.size) {
            if (storage[i] == target) return true
        }
        return false
    }

    override fun containsAll(elements: Collection<UInt>): Boolean {
        for (element in elements) {
            if (!contains(element)) return false
        }
        return true
    }

    override fun isEmpty(): Boolean = this.storage.size == 0

    override fun equals(other: Any?): Boolean {
        if (other !is UIntArray) return false
        if (storage.size != other.storage.size) return false
        for (i in 0 until storage.size) {
            if (storage[i] != other.storage[i]) return false
        }
        return true
    }

    override fun hashCode(): Int {
        var result = 1
        for (i in 0 until storage.size) {
            result = 31 * result + storage[i]
        }
        return result
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("UIntArray([")
        for (i in 0 until storage.size) {
            if (i > 0) sb.append(", ")
            sb.append(storage[i].toUInt().toString())
        }
        sb.append("])")
        return sb.toString()
    }
}

/**
 * Creates a new array of the specified [size], where each element is calculated by calling the specified
 * [init] function.
 */
@SinceKotlin("1.3")
public inline fun UIntArray(size: Int, init: (Int) -> UInt): UIntArray {
    return UIntArray(IntArray(size) { index -> init(index).toInt() })
}

@SinceKotlin("1.3")
public fun uintArrayOf(vararg elements: UInt): UIntArray {
    return UIntArray(IntArray(elements.size) { elements[it].toInt() })
}
