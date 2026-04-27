/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin

/**
 * Actual implementations of core Kotlin functions for BrightScript target.
 */

// ============================================
// Double NaN/Infinite Checks
// ============================================

/**
 * Returns `true` if the specified number is a Not-a-Number (NaN) value.
 * NaN is the only value that is not equal to itself.
 */
public actual fun Double.isNaN(): Boolean = this != this

/**
 * Returns `true` if this value is infinitely large in magnitude.
 */
public actual fun Double.isInfinite(): Boolean =
    this == Double.POSITIVE_INFINITY || this == Double.NEGATIVE_INFINITY

/**
 * Returns `true` if the argument is a finite floating-point value.
 */
public actual fun Double.isFinite(): Boolean = !isInfinite() && !isNaN()

// ============================================
// Float NaN/Infinite Checks
// ============================================

/**
 * Returns `true` if the specified number is a Not-a-Number (NaN) value.
 */
public actual fun Float.isNaN(): Boolean = this != this

/**
 * Returns `true` if this value is infinitely large in magnitude.
 */
public actual fun Float.isInfinite(): Boolean =
    this == Float.POSITIVE_INFINITY || this == Float.NEGATIVE_INFINITY

/**
 * Returns `true` if the argument is a finite floating-point value.
 */
public actual fun Float.isFinite(): Boolean = !isInfinite() && !isNaN()

// ============================================
// Double Bit Operations
// ============================================

/**
 * Returns a bit representation of the specified floating-point value as [Long]
 * according to the IEEE 754 floating-point "double format" bit layout.
 *
 * Note: BrightScript doesn't have native bit-level access to doubles.
 * This implementation uses a conversion approach.
 */
@SinceKotlin("1.2")
public actual fun Double.toBits(): Long {
    // Handle special cases
    if (isNaN()) return 0x7ff8000000000000L // Canonical NaN
    return toRawBits()
}

/**
 * Returns a bit representation of the specified floating-point value as [Long],
 * preserving `NaN` values exact layout.
 */
@SinceKotlin("1.2")
public actual fun Double.toRawBits(): Long {
    // BrightScript doesn't have native bit-level access to doubles
    // This will be lowered by the backend to appropriate BRS operations
    // For now, use a simplified approach
    return when {
        this == 0.0 -> if (1.0 / this < 0) Long.MIN_VALUE else 0L // Handle +0 and -0
        isNaN() -> 0x7ff8000000000000L // NaN pattern
        this == Double.POSITIVE_INFINITY -> 0x7ff0000000000000L
        this == Double.NEGATIVE_INFINITY -> -4503599627370496L // 0xfff0000000000000 as signed Long
        else -> {
            // General case - approximate conversion
            // This is a simplified implementation; backend should provide proper intrinsic
            val negative = this < 0
            val absValue = if (negative) -this else this

            if (absValue == 0.0) return 0L

            // Calculate exponent and mantissa
            var exp = 0
            var mantissa = absValue

            // Normalize to [1, 2) range
            while (mantissa >= 2.0) {
                mantissa /= 2.0
                exp++
            }
            while (mantissa < 1.0) {
                mantissa *= 2.0
                exp--
            }

            // IEEE 754: bias = 1023
            val biasedExp = (exp + 1023).toLong()

            // Extract mantissa bits (52 bits)
            mantissa -= 1.0 // Remove implicit leading 1
            var mantissaBits = 0L
            for (i in 0 until 52) {
                mantissa *= 2.0
                if (mantissa >= 1.0) {
                    mantissaBits = mantissaBits or (1L shl (51 - i))
                    mantissa -= 1.0
                }
            }

            val signBit = if (negative) 1L shl 63 else 0L
            signBit or (biasedExp shl 52) or mantissaBits
        }
    }
}

/**
 * Returns the [Double] value corresponding to a given bit representation.
 */
@SinceKotlin("1.2")
public actual fun Double.Companion.fromBits(bits: Long): Double {
    // Handle special cases
    when (bits) {
        0L -> return 0.0
        Long.MIN_VALUE -> return -0.0
        0x7ff0000000000000L -> return Double.POSITIVE_INFINITY
        -0x10000000000000L -> return Double.NEGATIVE_INFINITY // 0xfff0000000000000
    }

    // Check for NaN (exponent all 1s, mantissa non-zero)
    val exp = ((bits shr 52) and 0x7ff).toInt()
    val mantissaBits = bits and 0xfffffffffffffL
    if (exp == 0x7ff && mantissaBits != 0L) {
        return Double.NaN
    }

    // General case
    val negative = bits < 0
    val biasedExp = exp - 1023

    // Reconstruct mantissa
    var mantissa = 1.0 // Implicit leading 1
    for (i in 0 until 52) {
        if ((mantissaBits and (1L shl (51 - i))) != 0L) {
            mantissa += 1.0 / (1L shl (i + 1)).toDouble()
        }
    }

    // Apply exponent
    val result = if (biasedExp >= 0) {
        mantissa * (1L shl biasedExp.coerceAtMost(62)).toDouble()
    } else {
        mantissa / (1L shl (-biasedExp).coerceAtMost(62)).toDouble()
    }

    return if (negative) -result else result
}

// ============================================
// Float Bit Operations
// ============================================

/**
 * Returns a bit representation of the specified floating-point value as [Int]
 * according to the IEEE 754 floating-point "single format" bit layout.
 */
@SinceKotlin("1.2")
public actual fun Float.toBits(): Int {
    if (isNaN()) return 0x7fc00000 // Canonical NaN
    return toRawBits()
}

/**
 * Returns a bit representation of the specified floating-point value as [Int],
 * preserving `NaN` values exact layout.
 */
@SinceKotlin("1.2")
public actual fun Float.toRawBits(): Int {
    return when {
        this == 0.0f -> if (1.0f / this < 0) Int.MIN_VALUE else 0
        isNaN() -> 0x7fc00000
        this == Float.POSITIVE_INFINITY -> 0x7f800000
        this == Float.NEGATIVE_INFINITY -> 0xff800000.toInt()
        else -> {
            val negative = this < 0
            val absValue = if (negative) -this else this

            var exp = 0
            var mantissa = absValue.toDouble()

            while (mantissa >= 2.0) {
                mantissa /= 2.0
                exp++
            }
            while (mantissa < 1.0) {
                mantissa *= 2.0
                exp--
            }

            val biasedExp = exp + 127

            mantissa -= 1.0
            var mantissaBits = 0
            for (i in 0 until 23) {
                mantissa *= 2.0
                if (mantissa >= 1.0) {
                    mantissaBits = mantissaBits or (1 shl (22 - i))
                    mantissa -= 1.0
                }
            }

            val signBit = if (negative) 1 shl 31 else 0
            signBit or (biasedExp shl 23) or mantissaBits
        }
    }
}

/**
 * Returns the [Float] value corresponding to a given bit representation.
 */
@SinceKotlin("1.2")
public actual fun Float.Companion.fromBits(bits: Int): Float {
    when (bits) {
        0 -> return 0.0f
        Int.MIN_VALUE -> return -0.0f
        0x7f800000 -> return Float.POSITIVE_INFINITY
        0xff800000.toInt() -> return Float.NEGATIVE_INFINITY
    }

    val exp = (bits shr 23) and 0xff
    val mantissaBits = bits and 0x7fffff
    if (exp == 0xff && mantissaBits != 0) {
        return Float.NaN
    }

    val negative = bits < 0
    val biasedExp = exp - 127

    var mantissa = 1.0
    for (i in 0 until 23) {
        if ((mantissaBits and (1 shl (22 - i))) != 0) {
            mantissa += 1.0 / (1 shl (i + 1)).toDouble()
        }
    }

    val result = if (biasedExp >= 0) {
        mantissa * (1 shl biasedExp.coerceAtMost(30)).toDouble()
    } else {
        mantissa / (1 shl (-biasedExp).coerceAtMost(30)).toDouble()
    }

    return (if (negative) -result else result).toFloat()
}

// ============================================
// Lazy Initialization
// ============================================

/**
 * Creates a new instance of the [Lazy] that uses the specified initialization function.
 * In BrightScript, there's no multi-threading, so thread-safety mode is ignored.
 */
public actual fun <T> lazy(initializer: () -> T): Lazy<T> = UnsafeLazyImpl(initializer)

/**
 * Creates a new instance of the [Lazy] with the specified thread-safety mode.
 * In BrightScript, there's no multi-threading, so mode is ignored.
 */
public actual fun <T> lazy(mode: LazyThreadSafetyMode, initializer: () -> T): Lazy<T> =
    UnsafeLazyImpl(initializer)

/**
 * Creates a new instance of the [Lazy] with a lock parameter.
 * The lock is ignored in BrightScript.
 */
@Deprecated("Synchronization on Any? object is supported only in Kotlin/JVM.", ReplaceWith("lazy(initializer)"))
@DeprecatedSinceKotlin(warningSince = "1.9", errorSince = "2.1")
public actual fun <T> lazy(lock: Any?, initializer: () -> T): Lazy<T> = UnsafeLazyImpl(initializer)

/**
 * Simple unsafe lazy implementation for BrightScript.
 * Thread-safety is not a concern since BrightScript is single-threaded.
 */
private class UnsafeLazyImpl<out T>(private val initializer: () -> T) : Lazy<T> {
    private var _value: Any? = UNINITIALIZED_VALUE

    override val value: T
        get() {
            if (_value === UNINITIALIZED_VALUE) {
                _value = initializer()
            }
            @Suppress("UNCHECKED_CAST")
            return _value as T
        }

    override fun isInitialized(): Boolean = _value !== UNINITIALIZED_VALUE

    override fun toString(): String = if (isInitialized()) value.toString() else "Lazy value not initialized yet."
}

private object UNINITIALIZED_VALUE

// ============================================
// Structural Equality
// ============================================

/**
 * Compares two values for structural equality.
 * This is needed because BrightScript's == operator doesn't work for associative arrays (objects).
 * For objects, we delegate to their equals method.
 * For primitives, we use native BrightScript comparison.
 *
 * This function uses intrinsics to avoid the compiler incorrectly generating recursive calls
 * when compiling `a.equals(b)` on Any? type.
 *
 * @param a first value to compare
 * @param b second value to compare
 * @return true if the values are structurally equal
 */
public fun brsStructuralEquals(a: Any?, b: Any?): Boolean {
    // Identity check (also handles null == null)
    if (a === b) return true
    // Null checks
    if (a === null || b === null) return false
    // For objects (roAssociativeArray), use equals method
    // For primitives, use native comparison via brsNativeEquals intrinsic
    return if (brsIsAssociativeArray(a)) {
        brsCallEquals(a, b)
    } else {
        brsNativeEquals(a, b)
    }
}

/**
 * Check if a value is a roAssociativeArray (Kotlin class instance).
 * This is an intrinsic that compiles to: Type(a) = "roAssociativeArray"
 */
@Suppress("BRS_INTRINSIC_USER_DEFINED")
@kotlin.brs.BrsIntrinsic("brsIntrinsicIsAA")
private external fun brsIsAssociativeArray(a: Any?): Boolean

/**
 * Call the equals method on an object.
 * This is an intrinsic that compiles to: a.equals(b)
 */
@Suppress("BRS_INTRINSIC_USER_DEFINED")
@kotlin.brs.BrsIntrinsic("brsIntrinsicCallEquals")
private external fun brsCallEquals(a: Any?, b: Any?): Boolean

/**
 * Native BrightScript equals comparison.
 * This is an intrinsic that compiles to: a = b
 */
@Suppress("BRS_INTRINSIC_USER_DEFINED")
@kotlin.brs.BrsIntrinsic("brsIntrinsicNativeEquals")
private external fun brsNativeEquals(a: Any?, b: Any?): Boolean

// ============================================
// Comparison for Comparable types
// ============================================

/**
 * Compares two values for ordering.
 * This is needed because BrightScript primitives (roInt, roString, etc.) don't have
 * a compareTo method. For objects (roAssociativeArray), we delegate to compareTo.
 * For primitives, we use native BrightScript comparison operators.
 *
 * @param a first value to compare
 * @param b second value to compare
 * @return negative if a < b, zero if a == b, positive if a > b
 */
public fun brsCompareTo(a: Any?, b: Any?): Int {
    // Handle null cases
    if (a === b) return 0
    if (a === null) return -1
    if (b === null) return 1
    // For objects (roAssociativeArray), use compareTo method
    // For primitives, use native comparison operators
    return if (brsIsAssociativeArray(a)) {
        brsCallCompareTo(a, b)
    } else {
        brsNativeCompare(a, b)
    }
}

/**
 * Call the compareTo method on an object.
 * This is an intrinsic that compiles to: a.compareTo(b)
 */
@Suppress("BRS_INTRINSIC_USER_DEFINED")
@kotlin.brs.BrsIntrinsic("brsIntrinsicCallCompareTo")
private external fun brsCallCompareTo(a: Any?, b: Any?): Int

/**
 * Native BrightScript comparison.
 * This is an intrinsic that compiles to: if a < b then -1 else if a > b then 1 else 0
 */
@Suppress("BRS_INTRINSIC_USER_DEFINED")
@kotlin.brs.BrsIntrinsic("brsIntrinsicNativeCompare")
private external fun brsNativeCompare(a: Any?, b: Any?): Int
