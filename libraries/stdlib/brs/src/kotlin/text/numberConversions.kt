/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.text

/**
 * Returns `true` if this string is not `null` and its content is equal to the word "true", ignoring case, and `false` otherwise.
 */
@SinceKotlin("1.4")
public actual fun String?.toBoolean(): Boolean = this != null && this.lowercase() == "true"

/**
 * Parses the string to a [Byte] number.
 *
 * @throws NumberFormatException if the string is not a valid representation of a [Byte].
 */
public actual fun String.toByte(): Byte = toByteOrNull() ?: numberFormatError(this)

/**
 * Parses the string as a signed [Byte] number and returns the result.
 * @throws NumberFormatException if the string is not a valid representation of a number.
 * @throws IllegalArgumentException when [radix] is not a valid radix for string to number conversion.
 */
public actual fun String.toByte(radix: Int): Byte = toByteOrNull(radix) ?: numberFormatError(this)

/**
 * Parses the string to a [Short] number.
 *
 * @throws NumberFormatException if the string is not a valid representation of a [Short].
 */
public actual fun String.toShort(): Short = toShortOrNull() ?: numberFormatError(this)

/**
 * Parses the string as a [Short] number and returns the result.
 * @throws NumberFormatException if the string is not a valid representation of a number.
 * @throws IllegalArgumentException when [radix] is not a valid radix for string to number conversion.
 */
public actual fun String.toShort(radix: Int): Short = toShortOrNull(radix) ?: numberFormatError(this)

/**
 * Parses the string to an [Int] number.
 *
 * @throws NumberFormatException if the string is not a valid representation of an [Int].
 */
public actual fun String.toInt(): Int = toIntOrNull() ?: numberFormatError(this)

/**
 * Parses the string as an [Int] number and returns the result.
 * @throws NumberFormatException if the string is not a valid representation of a number.
 * @throws IllegalArgumentException when [radix] is not a valid radix for string to number conversion.
 */
public actual fun String.toInt(radix: Int): Int = toIntOrNull(radix) ?: numberFormatError(this)

/**
 * Parses the string to a [Long] number.
 *
 * @throws NumberFormatException if the string is not a valid representation of a [Long].
 */
public actual fun String.toLong(): Long = toLongOrNull() ?: numberFormatError(this)

/**
 * Parses the string as a [Long] number and returns the result.
 * @throws NumberFormatException if the string is not a valid representation of a number.
 * @throws IllegalArgumentException when [radix] is not a valid radix for string to number conversion.
 */
public actual fun String.toLong(radix: Int): Long = toLongOrNull(radix) ?: numberFormatError(this)

/**
 * Parses the string as a [Double] number and returns the result.
 * @throws NumberFormatException if the string is not a valid representation of a number.
 */
public actual fun String.toDouble(): Double {
    val result = toDoubleOrNull()
    if (result == null) {
        numberFormatError(this)
    }
    return result
}

/**
 * Parses the string as a [Float] number and returns the result.
 * @throws NumberFormatException if the string is not a valid representation of a number.
 */
@kotlin.internal.InlineOnly
public actual inline fun String.toFloat(): Float = toDouble().toFloat()

/**
 * Parses the string as a [Double] number and returns the result
 * or `null` if the string is not a valid representation of a number.
 */
public actual fun String.toDoubleOrNull(): Double? {
    val trimmed = this.trim()
    if (trimmed.isEmpty()) return null

    // Handle special cases
    when (trimmed.lowercase()) {
        "nan", "+nan", "-nan" -> return Double.NaN
        "infinity", "+infinity" -> return Double.POSITIVE_INFINITY
        "-infinity" -> return Double.NEGATIVE_INFINITY
    }

    return parseDouble(trimmed)
}

/**
 * Parses the string as a [Float] number and returns the result
 * or `null` if the string is not a valid representation of a number.
 */
@kotlin.internal.InlineOnly
public actual inline fun String.toFloatOrNull(): Float? = toDoubleOrNull()?.toFloat()

/**
 * Returns a string representation of this [Byte] value in the specified [radix].
 *
 * @throws IllegalArgumentException when [radix] is not a valid radix for number to string conversion.
 */
@SinceKotlin("1.2")
@kotlin.internal.InlineOnly
public actual inline fun Byte.toString(radix: Int): String = this.toInt().toString(radix)

/**
 * Returns a string representation of this [Short] value in the specified [radix].
 *
 * @throws IllegalArgumentException when [radix] is not a valid radix for number to string conversion.
 */
@SinceKotlin("1.2")
@kotlin.internal.InlineOnly
public actual inline fun Short.toString(radix: Int): String = this.toInt().toString(radix)

/**
 * Returns a string representation of this [Int] value in the specified [radix].
 *
 * @throws IllegalArgumentException when [radix] is not a valid radix for number to string conversion.
 */
@SinceKotlin("1.2")
public actual fun Int.toString(radix: Int): String {
    checkRadix(radix)
    return toStringImpl(this, radix)
}

/**
 * Returns a string representation of this [Long] value in the specified [radix].
 *
 * @throws IllegalArgumentException when [radix] is not a valid radix for number to string conversion.
 */
@SinceKotlin("1.2")
public actual fun Long.toString(radix: Int): String {
    checkRadix(radix)
    return toStringImpl(this, radix)
}

/**
 * Checks whether the given [radix] is valid radix for string to number and number to string conversion.
 */
@PublishedApi
internal actual fun checkRadix(radix: Int): Int {
    if (radix !in 2..36) {
        throw IllegalArgumentException("radix $radix was not in valid range 2..36")
    }
    return radix
}

internal actual fun digitOf(char: Char, radix: Int): Int = when {
    char >= '0' && char <= '9' -> char - '0'
    char >= 'A' && char <= 'Z' -> char - 'A' + 10
    char >= 'a' && char <= 'z' -> char - 'a' + 10
    char < '\u0080' -> -1
    char >= '\uFF21' && char <= '\uFF3A' -> char - '\uFF21' + 10 // full-width latin capital letter
    char >= '\uFF41' && char <= '\uFF5A' -> char - '\uFF41' + 10 // full-width latin small letter
    else -> char.digitToIntImpl()
}.let { if (it >= radix) -1 else it }

// Unicode digit ranges (37 ranges from Unicode standard)
private object Digit {
    internal val rangeStart = intArrayOf(
        0x0030, 0x0660, 0x06f0, 0x07c0, 0x0966, 0x09e6, 0x0a66, 0x0ae6, 0x0b66, 0x0be6,
        0x0c66, 0x0ce6, 0x0d66, 0x0de6, 0x0e50, 0x0ed0, 0x0f20, 0x1040, 0x1090, 0x17e0,
        0x1810, 0x1946, 0x19d0, 0x1a80, 0x1a90, 0x1b50, 0x1bb0, 0x1c40, 0x1c50, 0xa620,
        0xa8d0, 0xa900, 0xa9d0, 0xa9f0, 0xaa50, 0xabf0, 0xff10,
    )
}

/**
 * Returns the index of the largest element in [array] smaller or equal to the specified [needle],
 * or -1 if [needle] is smaller than the smallest element in [array].
 */
internal fun binarySearchRange(array: IntArray, needle: Int): Int {
    var bottom = 0
    var top = array.size - 1
    var middle = -1
    var value = 0
    while (bottom <= top) {
        middle = (bottom + top) / 2
        value = array[middle]
        if (needle > value)
            bottom = middle + 1
        else if (needle == value)
            return middle
        else
            top = middle - 1
    }
    return middle - (if (needle < value) 1 else 0)
}

/**
 * Returns an integer from 0..9 indicating the digit this character represents,
 * or -1 if this character is not a digit.
 */
internal fun Char.digitToIntImpl(): Int {
    val ch = this.code
    val index = binarySearchRange(Digit.rangeStart, ch)
    val diff = ch - Digit.rangeStart[index]
    return if (diff < 10) diff else -1
}

/**
 * Returns `true` if this character is a digit.
 */
internal fun Char.isDigitImpl(): Boolean {
    return digitToIntImpl() >= 0
}

// Internal implementation for Int.toString(radix)
private fun toStringImpl(value: Int, radix: Int): String {
    if (radix == 10) return value.toString()

    if (value == 0) return "0"

    val negative = value < 0
    var n = if (negative) value else -value // Work with negative to handle Int.MIN_VALUE

    val chars = CharArray(33) // Max 32 bits + sign
    var index = chars.size

    while (n < 0) {
        val digit = -(n % radix)
        chars[--index] = digitToChar(digit)
        n /= radix
    }

    if (negative) {
        chars[--index] = '-'
    }

    return chars.concatToString(index, chars.size)
}

// Internal implementation for Long.toString(radix)
private fun toStringImpl(value: Long, radix: Int): String {
    if (radix == 10) return value.toString()

    if (value == 0L) return "0"

    val negative = value < 0
    var n = if (negative) value else -value // Work with negative to handle Long.MIN_VALUE

    val chars = CharArray(65) // Max 64 bits + sign
    var index = chars.size

    while (n < 0) {
        val digit = (-(n % radix)).toInt()
        chars[--index] = digitToChar(digit)
        n /= radix
    }

    if (negative) {
        chars[--index] = '-'
    }

    return chars.concatToString(index, chars.size)
}

private fun digitToChar(digit: Int): Char {
    return if (digit < 10) {
        ('0'.code + digit).toChar()
    } else {
        ('a'.code + digit - 10).toChar()
    }
}

// Parse a double from a string (pure Kotlin implementation)
private fun parseDouble(s: String): Double? {
    if (s.isEmpty()) return null

    var index = 0
    val length = s.length

    // Handle sign
    val negative = when (s[index]) {
        '-' -> { index++; true }
        '+' -> { index++; false }
        else -> false
    }

    if (index >= length) return null

    // Check for hex format
    if (index + 1 < length && s[index] == '0' && (s[index + 1] == 'x' || s[index + 1] == 'X')) {
        return parseHexDouble(s, index + 2, negative)
    }

    // Parse integer part
    var intPart = 0.0
    var hasIntPart = false
    while (index < length && s[index] in '0'..'9') {
        intPart = intPart * 10 + (s[index] - '0')
        hasIntPart = true
        index++
    }

    // Parse fractional part
    var fracPart = 0.0
    var fracDivisor = 1.0
    if (index < length && s[index] == '.') {
        index++
        while (index < length && s[index] in '0'..'9') {
            fracPart = fracPart * 10 + (s[index] - '0')
            fracDivisor *= 10
            index++
        }
    }

    if (!hasIntPart && fracDivisor == 1.0) return null // No digits at all

    var result = intPart + fracPart / fracDivisor

    // Parse exponent
    if (index < length && (s[index] == 'e' || s[index] == 'E')) {
        index++
        if (index >= length) return null

        val expNegative = when (s[index]) {
            '-' -> { index++; true }
            '+' -> { index++; false }
            else -> false
        }

        if (index >= length || s[index] !in '0'..'9') return null

        var exp = 0
        while (index < length && s[index] in '0'..'9') {
            exp = exp * 10 + (s[index] - '0')
            if (exp > 400) exp = 400 // Cap to prevent overflow during calculation
            index++
        }

        if (expNegative) exp = -exp

        result *= pow10(exp)
    }

    // Check no trailing characters
    if (index != length) return null

    return if (negative) -result else result
}

private fun parseHexDouble(s: String, startIndex: Int, negative: Boolean): Double? {
    var index = startIndex
    val length = s.length

    if (index >= length) return null

    // Parse hex integer part
    var intPart = 0.0
    var hasIntPart = false
    while (index < length) {
        val digit = hexDigitOf(s[index])
        if (digit < 0) break
        intPart = intPart * 16 + digit
        hasIntPart = true
        index++
    }

    // Parse hex fractional part
    var fracPart = 0.0
    var fracDivisor = 1.0
    if (index < length && s[index] == '.') {
        index++
        while (index < length) {
            val digit = hexDigitOf(s[index])
            if (digit < 0) break
            fracPart = fracPart * 16 + digit
            fracDivisor *= 16
            index++
        }
    }

    if (!hasIntPart && fracDivisor == 1.0) return null

    var result = intPart + fracPart / fracDivisor

    // Parse binary exponent (p notation)
    if (index < length && (s[index] == 'p' || s[index] == 'P')) {
        index++
        if (index >= length) return null

        val expNegative = when (s[index]) {
            '-' -> { index++; true }
            '+' -> { index++; false }
            else -> false
        }

        if (index >= length || s[index] !in '0'..'9') return null

        var exp = 0
        while (index < length && s[index] in '0'..'9') {
            exp = exp * 10 + (s[index] - '0')
            if (exp > 1100) exp = 1100 // Cap to prevent overflow
            index++
        }

        if (expNegative) exp = -exp

        result *= pow2(exp)
    }

    if (index != length) return null

    return if (negative) -result else result
}

private fun hexDigitOf(c: Char): Int = when (c) {
    in '0'..'9' -> c - '0'
    in 'a'..'f' -> c - 'a' + 10
    in 'A'..'F' -> c - 'A' + 10
    else -> -1
}

private fun pow10(exp: Int): Double {
    if (exp == 0) return 1.0
    if (exp > 0) {
        var result = 1.0
        var base = 10.0
        var e = exp
        while (e > 0) {
            if (e and 1 != 0) result *= base
            base *= base
            e = e shr 1
        }
        return result
    } else {
        var result = 1.0
        var base = 0.1
        var e = -exp
        while (e > 0) {
            if (e and 1 != 0) result *= base
            base *= base
            e = e shr 1
        }
        return result
    }
}

private fun pow2(exp: Int): Double {
    if (exp == 0) return 1.0
    if (exp > 0) {
        var result = 1.0
        var base = 2.0
        var e = exp
        while (e > 0) {
            if (e and 1 != 0) result *= base
            base *= base
            e = e shr 1
        }
        return result
    } else {
        var result = 1.0
        var base = 0.5
        var e = -exp
        while (e > 0) {
            if (e and 1 != 0) result *= base
            base *= base
            e = e shr 1
        }
        return result
    }
}
