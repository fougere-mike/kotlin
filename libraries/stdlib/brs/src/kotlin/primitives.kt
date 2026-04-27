/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin

/**
 * Primitive type extensions and conversions for BrightScript target.
 */

// ============================================
// Char Extensions
// ============================================

/**
 * Returns the code of this Char.
 * This is equivalent to Char.toInt() and provides the UTF-16 code unit.
 */
@SinceKotlin("1.5")
public val Char.code: Int
    get() = brsIntrinsicCharCode(this)

/**
 * Intrinsic function to get the character code.
 * Lowered to: Asc(char)
 */
@PublishedApi
internal external fun brsIntrinsicCharCode(char: Char): Int

/**
 * Intrinsic function to convert an Int to a Char.
 * Lowered to: Chr(code)
 */
@PublishedApi
internal external fun brsIntrinsicChr(code: Int): Char

/**
 * Converts this Int to a Char.
 */
public fun Int.toChar(): Char = brsIntrinsicChr(this)

// ============================================
// String Extensions
// ============================================

// Note: String.substring functions are defined in stringBrs.kt
// Do not duplicate them here.

// ============================================
// Range Utilities
// ============================================

/**
 * Returns a range from this value up to but excluding the specified [to] value.
 */
public infix fun Int.until(to: Int): IntRange {
    if (to <= Int.MIN_VALUE) return IntRange.EMPTY
    return this..(to - 1)
}

/**
 * Returns a range from this value up to but excluding the specified [to] value.
 */
public infix fun Long.until(to: Long): LongRange {
    if (to <= Long.MIN_VALUE) return LongRange.EMPTY
    return this..(to - 1)
}

/**
 * Returns a range from this value up to but excluding the specified [to] value.
 */
public infix fun Char.until(to: Char): CharRange {
    if (to <= Char.MIN_VALUE) return CharRange.EMPTY
    return this..(to - 1)
}

// ============================================
// Collection Utilities
// ============================================

/**
 * Returns the range of valid indices for this CharSequence.
 */
public val CharSequence.indices: IntRange
    get() = 0..(this.length - 1)

/**
 * Returns the range of valid indices for this array.
 */
public val <T> Array<out T>.indices: IntRange
    get() = 0..(this.size - 1)

/**
 * Returns the range of valid indices for this array.
 */
public val IntArray.indices: IntRange
    get() = 0..(this.size - 1)

/**
 * Returns the range of valid indices for this array.
 */
public val CharArray.indices: IntRange
    get() = 0..(this.size - 1)

/**
 * Returns the range of valid indices for this array.
 */
public val ByteArray.indices: IntRange
    get() = 0..(this.size - 1)

// ============================================
// Math Utilities
// ============================================

// Note: minOf and maxOf functions are defined in ComparisonsActualBrs.kt
// Do not duplicate them here.

/**
 * Coerces this value to be at least the specified minimum value.
 */
public fun Int.coerceAtLeast(minimumValue: Int): Int = if (this < minimumValue) minimumValue else this

/**
 * Coerces this value to be at most the specified maximum value.
 */
public fun Int.coerceAtMost(maximumValue: Int): Int = if (this > maximumValue) maximumValue else this

/**
 * Coerces this value to be within the specified range.
 */
public fun Int.coerceIn(minimumValue: Int, maximumValue: Int): Int {
    if (minimumValue > maximumValue) throw IllegalArgumentException("Cannot coerce value to an empty range: maximum $maximumValue is less than minimum $minimumValue.")
    return when {
        this < minimumValue -> minimumValue
        this > maximumValue -> maximumValue
        else -> this
    }
}

// ============================================
// Preconditions
// ============================================

/**
 * Throws an [IllegalArgumentException] if the [value] is false.
 */
public fun require(value: Boolean) {
    if (!value) {
        throw IllegalArgumentException("Failed requirement.")
    }
}

/**
 * Throws an [IllegalArgumentException] with the result of calling [lazyMessage] if the [value] is false.
 */
public inline fun require(value: Boolean, lazyMessage: () -> Any) {
    if (!value) {
        throw IllegalArgumentException(lazyMessage().toString())
    }
}

/**
 * Throws an [IllegalStateException] if the [value] is false.
 */
public fun check(value: Boolean) {
    if (!value) {
        throw IllegalStateException("Check failed.")
    }
}

/**
 * Throws an [IllegalStateException] with the result of calling [lazyMessage] if the [value] is false.
 */
public inline fun check(value: Boolean, lazyMessage: () -> Any) {
    if (!value) {
        throw IllegalStateException(lazyMessage().toString())
    }
}

/**
 * Throws an [IllegalStateException] with the given [message].
 */
@Suppress("BRS_NAME_CASE_CLASH")
public fun error(message: Any): Nothing = throw IllegalStateException(message.toString())

// ============================================
// Array Utilities
// ============================================

/**
 * Returns a copy of this array resized to the given [newSize].
 */
public fun ByteArray.copyOf(newSize: Int): ByteArray {
    val result = ByteArray(newSize)
    val copySize = if (newSize < this.size) newSize else this.size
    var i = 0
    while (i < copySize) {
        result[i] = this[i]
        i++
    }
    return result
}

/**
 * Returns a copy of this array resized to the given [newSize].
 */
public fun IntArray.copyOf(newSize: Int): IntArray {
    val result = IntArray(newSize)
    val copySize = if (newSize < this.size) newSize else this.size
    var i = 0
    while (i < copySize) {
        result[i] = this[i]
        i++
    }
    return result
}

/**
 * Returns a copy of this array resized to the given [newSize].
 */
public fun CharArray.copyOf(newSize: Int): CharArray {
    val result = CharArray(newSize)
    val copySize = if (newSize < this.size) newSize else this.size
    var i = 0
    while (i < copySize) {
        result[i] = this[i]
        i++
    }
    return result
}

// ============================================
// Iterator Support
// ============================================

/**
 * Returns an iterator over the characters in this string.
 */
public operator fun String.iterator(): CharIterator = object : CharIterator() {
    private var index = 0
    override fun hasNext(): Boolean = index < length
    override fun nextChar(): Char = get(index++)
}

/**
 * Returns an iterator over the characters in this CharArray.
 */
public operator fun CharArray.iterator(): CharIterator = object : CharIterator() {
    private var index = 0
    override fun hasNext(): Boolean = index < size
    override fun nextChar(): Char = get(index++)
}
