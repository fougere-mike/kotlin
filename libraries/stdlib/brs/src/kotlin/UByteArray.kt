/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin

/**
 * An array of unsigned bytes. When targeting BrightScript, instances of this class are represented as `ByteArray`.
 */
@SinceKotlin("1.3")
public class UByteArray
@PublishedApi
internal constructor(@PublishedApi internal val storage: ByteArray) : Collection<UByte> {

    /** Creates a new array of the specified [size], with all elements initialized to zero. */
    public constructor(size: Int) : this(ByteArray(size))

    /**
     * Returns the array element at the given [index]. This method can be called using the index operator.
     */
    public operator fun get(index: Int): UByte = storage[index].toUByte()

    /**
     * Sets the element at the given [index] to the given [value]. This method can be called using the index operator.
     */
    public operator fun set(index: Int, value: UByte) {
        storage[index] = value.toByte()
    }

    /** Returns the number of elements in the array. */
    public override val size: Int get() = storage.size

    /** Creates an iterator over the elements of the array. */
    public override operator fun iterator(): kotlin.collections.Iterator<UByte> = Iterator(storage)

    private class Iterator(private val array: ByteArray) : kotlin.collections.Iterator<UByte> {
        private var index = 0
        override fun hasNext() = index < array.size
        override fun next() = if (index < array.size) array[index++].toUByte() else throw NoSuchElementException(index.toString())
    }

    override fun contains(element: UByte): Boolean {
        val target = element.toByte()
        for (i in 0 until storage.size) {
            if (storage[i] == target) return true
        }
        return false
    }

    override fun containsAll(elements: Collection<UByte>): Boolean {
        for (element in elements) {
            if (!contains(element)) return false
        }
        return true
    }

    override fun isEmpty(): Boolean = this.storage.size == 0

    override fun equals(other: Any?): Boolean {
        if (other !is UByteArray) return false
        if (storage.size != other.storage.size) return false
        for (i in 0 until storage.size) {
            if (storage[i] != other.storage[i]) return false
        }
        return true
    }

    override fun hashCode(): Int {
        var result = 1
        for (i in 0 until storage.size) {
            result = 31 * result + storage[i].toInt()
        }
        return result
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("UByteArray([")
        for (i in 0 until storage.size) {
            if (i > 0) sb.append(", ")
            sb.append(storage[i].toUByte().toString())
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
public inline fun UByteArray(size: Int, init: (Int) -> UByte): UByteArray {
    return UByteArray(ByteArray(size) { index -> init(index).toByte() })
}

@SinceKotlin("1.3")
public fun ubyteArrayOf(vararg elements: UByte): UByteArray {
    return UByteArray(ByteArray(elements.size) { elements[it].toByte() })
}
