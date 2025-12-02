/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin

/**
 * Compares two Int values as unsigned integers.
 * Returns negative if v1 < v2, zero if v1 == v2, positive if v1 > v2.
 */
@PublishedApi
internal fun uintCompare(v1: Int, v2: Int): Int {
    return (v1 xor Int.MIN_VALUE).compareTo(v2 xor Int.MIN_VALUE)
}

/**
 * Compares two Long values as unsigned longs.
 * Returns negative if v1 < v2, zero if v1 == v2, positive if v1 > v2.
 */
@PublishedApi
internal fun ulongCompare(v1: Long, v2: Long): Int {
    return (v1 xor Long.MIN_VALUE).compareTo(v2 xor Long.MIN_VALUE)
}

/**
 * Performs unsigned division of two Int values.
 * Uses Long intermediate to handle unsigned semantics correctly.
 */
@PublishedApi
internal fun uintDivide(dividend: Int, divisor: Int): UInt {
    val dividendLong = dividend.toLong() and 0xFFFF_FFFF
    val divisorLong = divisor.toLong() and 0xFFFF_FFFF
    return UInt((dividendLong / divisorLong).toInt())
}

/**
 * Performs unsigned remainder of two Int values.
 * Uses Long intermediate to handle unsigned semantics correctly.
 */
@PublishedApi
internal fun uintRemainder(dividend: Int, divisor: Int): UInt {
    val dividendLong = dividend.toLong() and 0xFFFF_FFFF
    val divisorLong = divisor.toLong() and 0xFFFF_FFFF
    return UInt((dividendLong % divisorLong).toInt())
}

/**
 * Performs unsigned division of two Long values.
 * Implements long division algorithm for proper unsigned semantics.
 */
@PublishedApi
internal fun ulongDivide(dividend: Long, divisor: Long): ULong {
    // Special case: divisor has MSB set (> Long.MAX_VALUE when unsigned)
    if (divisor < 0) {
        // dividend < divisor (unsigned) -> quotient is 0
        return if (ulongCompare(dividend, divisor) < 0) ULong(0) else ULong(1)
    }

    // If dividend is positive (MSB not set), can use signed division
    if (dividend >= 0) {
        return ULong(dividend / divisor)
    }

    // Implement unsigned long division for dividend with MSB set
    // Strategy: divide (dividend >>> 1) by divisor, then adjust
    val quotient = ((dividend ushr 1) / divisor) shl 1
    val remainder = dividend - quotient * divisor
    return ULong(quotient + if (ulongCompare(remainder, divisor) >= 0) 1 else 0)
}

/**
 * Performs unsigned remainder of two Long values.
 * Implements long division algorithm for proper unsigned semantics.
 */
@PublishedApi
internal fun ulongRemainder(dividend: Long, divisor: Long): ULong {
    // Special case: divisor has MSB set (> Long.MAX_VALUE when unsigned)
    if (divisor < 0) {
        // dividend < divisor (unsigned) -> remainder is dividend
        // dividend >= divisor (unsigned) -> remainder is dividend - divisor
        return if (ulongCompare(dividend, divisor) < 0) {
            ULong(dividend)
        } else {
            ULong(dividend - divisor)
        }
    }

    // If dividend is positive (MSB not set), can use signed remainder
    if (dividend >= 0) {
        return ULong(dividend % divisor)
    }

    // Implement unsigned long remainder for dividend with MSB set
    val quotient = ((dividend ushr 1) / divisor) shl 1
    val remainder = dividend - quotient * divisor
    return ULong(remainder - if (ulongCompare(remainder, divisor) >= 0) divisor else 0)
}

// Conversion functions

/**
 * Converts UInt backing value to Long, treating it as unsigned.
 */
@PublishedApi
internal fun uintToLong(value: Int): Long = value.toLong() and 0xFFFF_FFFF

/**
 * Converts UInt backing value to ULong.
 */
@PublishedApi
internal fun uintToULong(value: Int): ULong = ULong(uintToLong(value))

/**
 * Converts UInt backing value to Double, treating it as unsigned.
 */
@PublishedApi
internal fun uintToDouble(value: Int): Double =
    (value and Int.MAX_VALUE).toDouble() + (value ushr 31 shl 30).toDouble() * 2

/**
 * Converts UInt backing value to Float.
 */
@PublishedApi
internal fun uintToFloat(value: Int): Float = uintToDouble(value).toFloat()

/**
 * Converts Double to UInt.
 */
@PublishedApi
internal fun doubleToUInt(value: Double): UInt = when {
    value.isNaN() -> UInt(0)
    value <= 0.0 -> UInt(0)
    value >= 4294967295.0 -> UInt(-1)  // UInt.MAX_VALUE
    value <= Int.MAX_VALUE -> UInt(value.toInt())
    else -> UInt((value - Int.MAX_VALUE).toInt() + Int.MAX_VALUE)
}

/**
 * Converts Float to UInt.
 */
@PublishedApi
internal fun floatToUInt(value: Float): UInt = doubleToUInt(value.toDouble())

/**
 * Converts ULong backing value to Double, treating it as unsigned.
 */
@PublishedApi
internal fun ulongToDouble(value: Long): Double =
    (value ushr 11).toDouble() * 2048 + (value and 2047)

/**
 * Converts ULong backing value to Float.
 */
@PublishedApi
internal fun ulongToFloat(value: Long): Float = ulongToDouble(value).toFloat()

/**
 * Converts Double to ULong.
 */
@PublishedApi
internal fun doubleToULong(value: Double): ULong = when {
    value.isNaN() -> ULong(0)
    value <= 0.0 -> ULong(0)
    value >= 18446744073709551615.0 -> ULong(-1)  // ULong.MAX_VALUE
    value < Long.MAX_VALUE -> ULong(value.toLong())
    else -> ULong((value - 9223372036854775808.0).toLong() + Long.MIN_VALUE)
}

/**
 * Converts Float to ULong.
 */
@PublishedApi
internal fun floatToULong(value: Float): ULong = doubleToULong(value.toDouble())

/**
 * Converts UInt backing value to String.
 */
@PublishedApi
internal fun uintToString(value: Int): String = uintToLong(value).toString()

/**
 * Converts UInt backing value to String with given radix.
 */
@PublishedApi
internal fun uintToString(value: Int, base: Int): String = ulongToString(uintToLong(value), base)

/**
 * Converts ULong backing value to String (base 10).
 */
@PublishedApi
internal fun ulongToString(value: Long): String = ulongToString(value, 10)

/**
 * Converts a signed Long to String with given radix (base 2-36).
 */
private fun longToStringWithRadix(value: Long, radix: Int): String {
    if (radix < 2 || radix > 36) throw IllegalArgumentException("radix $radix was not in valid range 2..36")
    if (value == 0L) return "0"

    val isNegative = value < 0
    var v = if (isNegative) -value else value

    val digits = "0123456789abcdefghijklmnopqrstuvwxyz"
    val result = StringBuilder()

    while (v != 0L) {
        val digit = (v % radix).toInt()
        result.insert(0, digits[digit])
        v /= radix
    }

    if (isNegative) result.insert(0, '-')
    return result.toString()
}

/**
 * Converts ULong backing value to String with given radix.
 */
@PublishedApi
internal fun ulongToString(value: Long, base: Int): String {
    if (value >= 0) return longToStringWithRadix(value, base)

    // For large unsigned values, use division algorithm
    var quotient = ((value ushr 1) / base) shl 1
    var rem = value - quotient * base
    if (rem >= base) {
        rem -= base
        quotient += 1
    }
    return longToStringWithRadix(quotient, base) + longToStringWithRadix(rem, base)
}
