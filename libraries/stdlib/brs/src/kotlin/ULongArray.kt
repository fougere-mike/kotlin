/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin

/**
 * An array of unsigned longs. When targeting BrightScript, instances of this class are represented as `LongArray`.
 */
@SinceKotlin("1.3")
public class ULongArray
@PublishedApi
internal constructor(@PublishedApi internal val storage: LongArray) : Collection<ULong> {

    /** Creates a new array of the specified [size], with all elements initialized to zero. */
    public constructor(size: Int) : this(LongArray(size))

    /**
     * Returns the array element at the given [index]. This method can be called using the index operator.
     */
    public operator fun get(index: Int): ULong = storage[index].toULong()

    /**
     * Sets the element at the given [index] to the given [value]. This method can be called using the index operator.
     */
    public operator fun set(index: Int, value: ULong) {
        storage[index] = value.toLong()
    }

    /** Returns the number of elements in the array. */
    public override val size: Int get() = storage.size

    /** Creates an iterator over the elements of the array. */
    @Suppress("BRS_NAME_CASE_CLASH")
    public override operator fun iterator(): kotlin.collections.Iterator<ULong> = Iterator(storage)

    @Suppress("BRS_NAME_CASE_CLASH")
    private class Iterator(private val array: LongArray) : kotlin.collections.Iterator<ULong> {
        private var index = 0
        override fun hasNext() = index < array.size
        override fun next() = if (index < array.size) array[index++].toULong() else throw NoSuchElementException(index.toString())
    }

    override fun contains(element: ULong): Boolean {
        val target = element.toLong()
        for (i in 0 until storage.size) {
            if (storage[i] == target) return true
        }
        return false
    }

    override fun containsAll(elements: Collection<ULong>): Boolean {
        for (element in elements) {
            if (!contains(element)) return false
        }
        return true
    }

    override fun isEmpty(): Boolean = this.storage.size == 0

    override fun equals(other: Any?): Boolean {
        if (other !is ULongArray) return false
        if (storage.size != other.storage.size) return false
        for (i in 0 until storage.size) {
            if (storage[i] != other.storage[i]) return false
        }
        return true
    }

    override fun hashCode(): Int {
        var result = 1
        for (i in 0 until storage.size) {
            val element = storage[i]
            result = 31 * result + (element xor (element ushr 32)).toInt()
        }
        return result
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("ULongArray([")
        for (i in 0 until storage.size) {
            if (i > 0) sb.append(", ")
            sb.append(storage[i].toULong().toString())
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
public inline fun ULongArray(size: Int, init: (Int) -> ULong): ULongArray {
    return ULongArray(LongArray(size) { index -> init(index).toLong() })
}

@SinceKotlin("1.3")
public fun ulongArrayOf(vararg elements: ULong): ULongArray {
    return ULongArray(LongArray(elements.size) { elements[it].toLong() })
}
