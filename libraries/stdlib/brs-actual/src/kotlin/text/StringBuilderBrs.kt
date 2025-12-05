/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.text

import kotlin.util.checkPositionIndex
import kotlin.util.checkElementIndex
import kotlin.util.checkBoundsIndexes

/**
 * A mutable sequence of characters.
 *
 * String builder can be used to efficiently perform multiple string manipulation operations.
 */
public class StringBuilder constructor(content: String) : Appendable, CharSequence {
    /**
     * Constructs an empty string builder with the specified initial [capacity].
     *
     * In Kotlin/BrightScript implementation of StringBuilder the initial capacity has no effect on the further performance of operations.
     */
    public constructor(capacity: Int) : this() {
    }

    /** Constructs a string builder that contains the same characters as the specified [content] char sequence. */
    public constructor(content: CharSequence) : this(content.toString()) {}

    /** Constructs an empty string builder. */
    public constructor() : this("")

    private var string: String = content

    override val length: Int
        get() = string.length

    override fun get(index: Int): Char =
        string.getOrElse(index) { throw IndexOutOfBoundsException("index: $index, length: $length}") }

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence = string.substring(startIndex, endIndex)

    override fun append(value: Char): StringBuilder {
        string += value
        return this
    }

    override fun append(value: CharSequence?): StringBuilder {
        string += value.toString()
        return this
    }

    override fun append(value: CharSequence?, startIndex: Int, endIndex: Int): StringBuilder =
        this.appendRange(value ?: "null", startIndex, endIndex)

    /**
     * Reverses the contents of this string builder and returns this instance.
     */
    public fun reverse(): StringBuilder {
        var reversed = ""
        var index = string.length - 1
        while (index >= 0) {
            val low = string[index--]
            if (low.isLowSurrogate() && index >= 0) {
                val high = string[index--]
                if (high.isHighSurrogate()) {
                    reversed = reversed + high + low
                } else {
                    reversed = reversed + low + high
                }
            } else {
                reversed += low
            }
        }
        string = reversed
        return this
    }

    /**
     * Appends the string representation of the specified object [value] to this string builder and returns this instance.
     */
    public fun append(value: Any?): StringBuilder {
        string += value.toString()
        return this
    }

    /**
     * Appends the string representation of the specified boolean [value] to this string builder and returns this instance.
     */
    @SinceKotlin("1.3")
    public fun append(value: Boolean): StringBuilder {
        string += value
        return this
    }

    @SinceKotlin("1.9")
    public fun append(value: Byte): StringBuilder = append(value.toString())

    @SinceKotlin("1.9")
    public fun append(value: Short): StringBuilder = append(value.toString())

    @SinceKotlin("1.9")
    public fun append(value: Int): StringBuilder = append(value.toString())

    @SinceKotlin("1.9")
    public fun append(value: Long): StringBuilder = append(value.toString())

    @SinceKotlin("1.9")
    public fun append(value: Float): StringBuilder = append(value.toString())

    @SinceKotlin("1.9")
    public fun append(value: Double): StringBuilder = append(value.toString())

    @SinceKotlin("1.4")
    public fun append(value: CharArray): StringBuilder {
        string += value.concatToString()
        return this
    }

    @SinceKotlin("1.3")
    public fun append(value: String?): StringBuilder {
        this.string += value ?: "null"
        return this
    }

    @SinceKotlin("1.3")
    @Deprecated("Obtaining StringBuilder capacity is not supported in BrightScript and common code.", level = DeprecationLevel.WARNING)
    public fun capacity(): Int = length

    @SinceKotlin("1.4")
    public fun ensureCapacity(minimumCapacity: Int) {
    }

    @SinceKotlin("1.4")
    public fun indexOf(string: String): Int = this.string.indexOf(string)

    @SinceKotlin("1.4")
    public fun indexOf(string: String, startIndex: Int): Int = this.string.indexOf(string, startIndex)

    @SinceKotlin("1.4")
    public fun lastIndexOf(string: String): Int = this.string.lastIndexOf(string)

    @SinceKotlin("1.4")
    public fun lastIndexOf(string: String, startIndex: Int): Int {
        if (string.isEmpty() && startIndex < 0) return -1
        return this.string.lastIndexOf(string, startIndex)
    }

    @SinceKotlin("1.4")
    public fun insert(index: Int, value: Boolean): StringBuilder {
        checkPositionIndex(index, length)
        string = string.substring(0, index) + value + string.substring(index)
        return this
    }

    @SinceKotlin("1.9")
    public fun insert(index: Int, value: Byte): StringBuilder = insert(index, value.toString())

    @SinceKotlin("1.9")
    public fun insert(index: Int, value: Short): StringBuilder = insert(index, value.toString())

    @SinceKotlin("1.9")
    public fun insert(index: Int, value: Int): StringBuilder = insert(index, value.toString())

    @SinceKotlin("1.9")
    public fun insert(index: Int, value: Long): StringBuilder = insert(index, value.toString())

    @SinceKotlin("1.9")
    public fun insert(index: Int, value: Float): StringBuilder = insert(index, value.toString())

    @SinceKotlin("1.9")
    public fun insert(index: Int, value: Double): StringBuilder = insert(index, value.toString())

    @SinceKotlin("1.4")
    public fun insert(index: Int, value: Char): StringBuilder {
        checkPositionIndex(index, length)
        string = string.substring(0, index) + value + string.substring(index)
        return this
    }

    @SinceKotlin("1.4")
    public fun insert(index: Int, value: CharArray): StringBuilder {
        checkPositionIndex(index, length)
        string = string.substring(0, index) + value.concatToString() + string.substring(index)
        return this
    }

    @SinceKotlin("1.4")
    public fun insert(index: Int, value: CharSequence?): StringBuilder {
        checkPositionIndex(index, length)
        string = string.substring(0, index) + value.toString() + string.substring(index)
        return this
    }

    @SinceKotlin("1.4")
    public fun insert(index: Int, value: Any?): StringBuilder {
        checkPositionIndex(index, length)
        string = string.substring(0, index) + value.toString() + string.substring(index)
        return this
    }

    @SinceKotlin("1.4")
    public fun insert(index: Int, value: String?): StringBuilder {
        checkPositionIndex(index, length)
        val toInsert = value ?: "null"
        this.string = this.string.substring(0, index) + toInsert + this.string.substring(index)
        return this
    }

    @SinceKotlin("1.4")
    public fun setLength(newLength: Int) {
        if (newLength < 0) {
            throw IllegalArgumentException("Negative new length: $newLength.")
        }
        if (newLength <= length) {
            string = string.substring(0, newLength)
        } else {
            // Note: BrightScript doesn't support null characters in strings,
            // so we use spaces instead of '\u0000' which Java uses
            for (i in length until newLength) {
                string += ' '
            }
        }
    }

    @SinceKotlin("1.4")
    public fun substring(startIndex: Int): String {
        checkPositionIndex(startIndex, length)
        return string.substring(startIndex)
    }

    @SinceKotlin("1.4")
    public fun substring(startIndex: Int, endIndex: Int): String {
        checkBoundsIndexes(startIndex, endIndex, length)
        return string.substring(startIndex, endIndex)
    }

    @SinceKotlin("1.4")
    public fun trimToSize() {
    }

    override fun toString(): String = string

    @SinceKotlin("1.3")
    public fun clear(): StringBuilder {
        string = ""
        return this
    }

    @SinceKotlin("1.4")
    public operator fun set(index: Int, value: Char) {
        checkElementIndex(index, length)
        string = string.substring(0, index) + value + string.substring(index + 1)
    }

    @SinceKotlin("1.4")
    public fun setRange(startIndex: Int, endIndex: Int, value: String): StringBuilder {
        checkReplaceRange(startIndex, endIndex, length)
        this.string = this.string.substring(0, startIndex) + value + this.string.substring(endIndex)
        return this
    }

    private fun checkReplaceRange(startIndex: Int, endIndex: Int, length: Int) {
        if (startIndex < 0 || startIndex > length) {
            throw IndexOutOfBoundsException("startIndex: $startIndex, length: $length")
        }
        if (startIndex > endIndex) {
            throw IllegalArgumentException("startIndex($startIndex) > endIndex($endIndex)")
        }
    }

    @SinceKotlin("1.4")
    public fun deleteAt(index: Int): StringBuilder {
        checkElementIndex(index, length)
        string = string.substring(0, index) + string.substring(index + 1)
        return this
    }

    @SinceKotlin("1.4")
    public fun deleteRange(startIndex: Int, endIndex: Int): StringBuilder {
        checkReplaceRange(startIndex, endIndex, length)
        string = string.substring(0, startIndex) + string.substring(endIndex)
        return this
    }

    @SinceKotlin("1.4")
    public fun toCharArray(destination: CharArray, destinationOffset: Int = 0, startIndex: Int = 0, endIndex: Int = this.length) {
        checkBoundsIndexes(startIndex, endIndex, length)
        checkBoundsIndexes(destinationOffset, destinationOffset + endIndex - startIndex, destination.size)
        var dstIndex = destinationOffset
        for (index in startIndex until endIndex) {
            destination[dstIndex++] = string[index]
        }
    }

    @SinceKotlin("1.4")
    public fun appendRange(value: CharArray, startIndex: Int, endIndex: Int): StringBuilder {
        string += value.concatToString(startIndex, endIndex)
        return this
    }

    @SinceKotlin("1.4")
    public fun appendRange(value: CharSequence, startIndex: Int, endIndex: Int): StringBuilder {
        val stringCsq = value.toString()
        checkBoundsIndexes(startIndex, endIndex, stringCsq.length)
        string += stringCsq.substring(startIndex, endIndex)
        return this
    }

    @SinceKotlin("1.4")
    public fun insertRange(index: Int, value: CharArray, startIndex: Int, endIndex: Int): StringBuilder {
        checkPositionIndex(index, this.length)
        string = string.substring(0, index) + value.concatToString(startIndex, endIndex) + string.substring(index)
        return this
    }

    @SinceKotlin("1.4")
    public fun insertRange(index: Int, value: CharSequence, startIndex: Int, endIndex: Int): StringBuilder {
        checkPositionIndex(index, length)
        val stringCsq = value.toString()
        checkBoundsIndexes(startIndex, endIndex, stringCsq.length)
        string = string.substring(0, index) + stringCsq.substring(startIndex, endIndex) + string.substring(index)
        return this
    }

    @SinceKotlin("1.4")
    public fun appendLine(): StringBuilder = append('\n')

    @SinceKotlin("1.4")
    public fun appendLine(value: CharSequence?): StringBuilder = append(value).appendLine()

    @SinceKotlin("1.4")
    public fun appendLine(value: String?): StringBuilder = append(value).appendLine()

    @SinceKotlin("1.4")
    public fun appendLine(value: Any?): StringBuilder = append(value).appendLine()

    @SinceKotlin("1.4")
    public fun appendLine(value: Char): StringBuilder = append(value).appendLine()

    @SinceKotlin("1.4")
    public fun appendLine(value: Boolean): StringBuilder = append(value).appendLine()

    @SinceKotlin("1.9")
    public fun appendLine(value: Int): StringBuilder = append(value).appendLine()

    @SinceKotlin("1.9")
    public fun appendLine(value: Short): StringBuilder = append(value.toInt()).appendLine()

    @SinceKotlin("1.9")
    public fun appendLine(value: Byte): StringBuilder = append(value.toInt()).appendLine()

    @SinceKotlin("1.9")
    public fun appendLine(value: Long): StringBuilder = append(value).appendLine()

    @SinceKotlin("1.9")
    public fun appendLine(value: Float): StringBuilder = append(value).appendLine()

    @SinceKotlin("1.9")
    public fun appendLine(value: Double): StringBuilder = append(value).appendLine()
}

// Note: Using helper functions from kotlin.util.Preconditions
// (checkPositionIndex, checkElementIndex, checkBoundsIndexes)

// Extension functions for StringBuilder (expect actuals)

@Suppress("NOTHING_TO_INLINE")
@SinceKotlin("1.9")
public inline fun StringBuilder.append(value: Byte): StringBuilder = this.append(value)

@Suppress("NOTHING_TO_INLINE")
@SinceKotlin("1.9")
public inline fun StringBuilder.append(value: Short): StringBuilder = this.append(value)

@Suppress("NOTHING_TO_INLINE")
@SinceKotlin("1.9")
public inline fun StringBuilder.insert(index: Int, value: Byte): StringBuilder = this.insert(index, value)

@Suppress("NOTHING_TO_INLINE")
@SinceKotlin("1.9")
public inline fun StringBuilder.insert(index: Int, value: Short): StringBuilder = this.insert(index, value)

@SinceKotlin("1.3")
@Suppress("NOTHING_TO_INLINE")
public inline fun StringBuilder.clear(): StringBuilder = this.clear()

@SinceKotlin("1.4")
@Suppress("NOTHING_TO_INLINE")
public inline operator fun StringBuilder.set(index: Int, value: Char): Unit = this.set(index, value)

@SinceKotlin("1.4")
@Suppress("NOTHING_TO_INLINE")
public inline fun StringBuilder.setRange(startIndex: Int, endIndex: Int, value: String): StringBuilder =
    this.setRange(startIndex, endIndex, value)

@SinceKotlin("1.4")
@Suppress("NOTHING_TO_INLINE")
public inline fun StringBuilder.deleteAt(index: Int): StringBuilder = this.deleteAt(index)

@SinceKotlin("1.4")
@Suppress("NOTHING_TO_INLINE")
public inline fun StringBuilder.deleteRange(startIndex: Int, endIndex: Int): StringBuilder = this.deleteRange(startIndex, endIndex)

@SinceKotlin("1.4")
@Suppress("NOTHING_TO_INLINE", "ACTUAL_FUNCTION_WITH_DEFAULT_ARGUMENTS")
public inline fun StringBuilder.toCharArray(destination: CharArray, destinationOffset: Int = 0, startIndex: Int = 0, endIndex: Int = this.length): Unit =
    this.toCharArray(destination, destinationOffset, startIndex, endIndex)

@SinceKotlin("1.4")
@Suppress("NOTHING_TO_INLINE")
public inline fun StringBuilder.appendRange(value: CharArray, startIndex: Int, endIndex: Int): StringBuilder =
    this.appendRange(value, startIndex, endIndex)

@SinceKotlin("1.4")
@Suppress("NOTHING_TO_INLINE")
public inline fun StringBuilder.appendRange(value: CharSequence, startIndex: Int, endIndex: Int): StringBuilder =
    this.appendRange(value, startIndex, endIndex)

@SinceKotlin("1.4")
@Suppress("NOTHING_TO_INLINE")
public inline fun StringBuilder.insertRange(index: Int, value: CharArray, startIndex: Int, endIndex: Int): StringBuilder =
    this.insertRange(index, value, startIndex, endIndex)

@SinceKotlin("1.4")
@Suppress("NOTHING_TO_INLINE")
public inline fun StringBuilder.insertRange(index: Int, value: CharSequence, startIndex: Int, endIndex: Int): StringBuilder =
    this.insertRange(index, value, startIndex, endIndex)

@SinceKotlin("1.9")
public inline fun StringBuilder.appendLine(value: Int): StringBuilder = append(value).appendLine()

@SinceKotlin("1.9")
public inline fun StringBuilder.appendLine(value: Short): StringBuilder = append(value.toInt()).appendLine()

@SinceKotlin("1.9")
public inline fun StringBuilder.appendLine(value: Byte): StringBuilder = append(value.toInt()).appendLine()

@SinceKotlin("1.9")
public inline fun StringBuilder.appendLine(value: Long): StringBuilder = append(value).appendLine()

@SinceKotlin("1.9")
public inline fun StringBuilder.appendLine(value: Float): StringBuilder = append(value).appendLine()

@SinceKotlin("1.9")
public inline fun StringBuilder.appendLine(value: Double): StringBuilder = append(value).appendLine()
