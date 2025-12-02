/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("NOTHING_TO_INLINE")

package kotlin.math

import kotlin.brs.runtime.*

/**
 * The ratio of the circumference of a circle to its diameter.
 */
public const val PI: Double = 3.141592653589793

/**
 * Base of the natural logarithm.
 */
public const val E: Double = 2.718281828459045

/**
 * Actual implementations of math functions for BrightScript target.
 *
 * BrightScript provides these native math functions:
 * - Sin(x), Cos(x), Tan(x), Atn(x) - trigonometry
 * - Sqr(x) - square root
 * - Exp(x), Log(x) - exponential and natural log
 * - Abs(x), Int(x) - absolute value and truncation
 *
 * Functions not available natively are implemented using mathematical identities.
 */

// ============================================
// Double Trigonometric Functions
// ============================================

@SinceKotlin("1.2")
public fun sin(x: Double): Double = brsIntrinsicSin(x)

@SinceKotlin("1.2")
public fun cos(x: Double): Double = brsIntrinsicCos(x)

@SinceKotlin("1.2")
public fun tan(x: Double): Double = brsIntrinsicTan(x)

@SinceKotlin("1.2")
public fun asin(x: Double): Double {
    // asin(x) = atan(x / sqrt(1 - x^2))
    if (x.isNaN() || x < -1.0 || x > 1.0) return Double.NaN
    if (x == 1.0) return PI / 2
    if (x == -1.0) return -PI / 2
    return brsIntrinsicAtan(x / brsIntrinsicSqrt(1 - x * x))
}

@SinceKotlin("1.2")
public fun acos(x: Double): Double {
    // acos(x) = PI/2 - asin(x)
    if (x.isNaN() || x < -1.0 || x > 1.0) return Double.NaN
    return PI / 2 - asin(x)
}

@SinceKotlin("1.2")
public fun atan(x: Double): Double = brsIntrinsicAtan(x)

@SinceKotlin("1.2")
public fun atan2(y: Double, x: Double): Double {
    // atan2 implementation using atan
    if (y.isNaN() || x.isNaN()) return Double.NaN
    if (y == 0.0 && x > 0.0) return 0.0
    if (y == 0.0 && x < 0.0) return PI
    if (x == 0.0 && y > 0.0) return PI / 2
    if (x == 0.0 && y < 0.0) return -PI / 2
    if (x > 0.0) return brsIntrinsicAtan(y / x)
    if (x < 0.0 && y >= 0.0) return brsIntrinsicAtan(y / x) + PI
    if (x < 0.0 && y < 0.0) return brsIntrinsicAtan(y / x) - PI
    return 0.0
}

// ============================================
// Double Hyperbolic Functions
// ============================================

@SinceKotlin("1.2")
public fun sinh(x: Double): Double {
    // sinh(x) = (e^x - e^-x) / 2
    if (x.isNaN()) return Double.NaN
    if (x.isInfinite()) return x
    return (brsIntrinsicExp(x) - brsIntrinsicExp(-x)) / 2
}

@SinceKotlin("1.2")
public fun cosh(x: Double): Double {
    // cosh(x) = (e^x + e^-x) / 2
    if (x.isNaN()) return Double.NaN
    if (x.isInfinite()) return Double.POSITIVE_INFINITY
    return (brsIntrinsicExp(x) + brsIntrinsicExp(-x)) / 2
}

@SinceKotlin("1.2")
public fun tanh(x: Double): Double {
    // tanh(x) = sinh(x) / cosh(x)
    if (x.isNaN()) return Double.NaN
    if (x == Double.POSITIVE_INFINITY) return 1.0
    if (x == Double.NEGATIVE_INFINITY) return -1.0
    val ex = brsIntrinsicExp(x)
    val emx = brsIntrinsicExp(-x)
    return (ex - emx) / (ex + emx)
}

@SinceKotlin("1.2")
public fun asinh(x: Double): Double {
    // asinh(x) = ln(x + sqrt(x^2 + 1))
    if (x.isNaN()) return Double.NaN
    if (x.isInfinite()) return x
    return brsIntrinsicLog(x + brsIntrinsicSqrt(x * x + 1))
}

@SinceKotlin("1.2")
public fun acosh(x: Double): Double {
    // acosh(x) = ln(x + sqrt(x^2 - 1)), x >= 1
    if (x.isNaN() || x < 1.0) return Double.NaN
    if (x == Double.POSITIVE_INFINITY) return Double.POSITIVE_INFINITY
    return brsIntrinsicLog(x + brsIntrinsicSqrt(x * x - 1))
}

@SinceKotlin("1.2")
public fun atanh(x: Double): Double {
    // atanh(x) = 0.5 * ln((1 + x) / (1 - x)), -1 < x < 1
    if (x.isNaN() || x < -1.0 || x > 1.0) return Double.NaN
    if (x == 1.0) return Double.POSITIVE_INFINITY
    if (x == -1.0) return Double.NEGATIVE_INFINITY
    return 0.5 * brsIntrinsicLog((1 + x) / (1 - x))
}

// ============================================
// Double Exponential and Logarithmic Functions
// ============================================

@SinceKotlin("1.2")
public fun hypot(x: Double, y: Double): Double {
    if (x.isInfinite() || y.isInfinite()) return Double.POSITIVE_INFINITY
    if (x.isNaN() || y.isNaN()) return Double.NaN
    return brsIntrinsicSqrt(x * x + y * y)
}

@SinceKotlin("1.2")
public fun sqrt(x: Double): Double = brsIntrinsicSqrt(x)

@SinceKotlin("1.2")
public fun exp(x: Double): Double = brsIntrinsicExp(x)

@SinceKotlin("1.2")
public fun expm1(x: Double): Double {
    // expm1(x) = exp(x) - 1, more precise for small x
    if (x.isNaN()) return Double.NaN
    if (x == Double.POSITIVE_INFINITY) return Double.POSITIVE_INFINITY
    if (x == Double.NEGATIVE_INFINITY) return -1.0
    return brsIntrinsicExp(x) - 1
}

@SinceKotlin("1.2")
public fun log(x: Double, base: Double): Double {
    if (x.isNaN() || base.isNaN()) return Double.NaN
    if (x < 0 || base <= 0 || base == 1.0) return Double.NaN
    return brsIntrinsicLog(x) / brsIntrinsicLog(base)
}

@SinceKotlin("1.2")
public fun ln(x: Double): Double = brsIntrinsicLog(x)

@SinceKotlin("1.2")
public fun log10(x: Double): Double = brsIntrinsicLog(x) / brsIntrinsicLog(10.0)

@SinceKotlin("1.2")
public fun log2(x: Double): Double = brsIntrinsicLog(x) / brsIntrinsicLog(2.0)

@SinceKotlin("1.2")
public fun ln1p(x: Double): Double {
    if (x.isNaN() || x < -1.0) return Double.NaN
    if (x == -1.0) return Double.NEGATIVE_INFINITY
    if (x == Double.POSITIVE_INFINITY) return Double.POSITIVE_INFINITY
    return brsIntrinsicLog(1 + x)
}

// ============================================
// Double Rounding Functions
// ============================================

@SinceKotlin("1.2")
public fun ceil(x: Double): Double = brsIntrinsicCeil(x)

@SinceKotlin("1.2")
public fun floor(x: Double): Double = brsIntrinsicFloor(x)

@SinceKotlin("1.2")
public fun truncate(x: Double): Double = brsIntrinsicTrunc(x).toDouble()

@SinceKotlin("1.2")
public fun round(x: Double): Double {
    if (x.isNaN() || x.isInfinite()) return x
    // Round to nearest, ties to even
    val floor = floor(x)
    val diff = x - floor
    return when {
        diff < 0.5 -> floor
        diff > 0.5 -> floor + 1
        else -> if (floor.toLong() % 2 == 0L) floor else floor + 1
    }
}

// ============================================
// Double Absolute Value and Sign
// ============================================

@SinceKotlin("1.2")
public fun abs(x: Double): Double = brsIntrinsicAbs(x)

@SinceKotlin("1.2")
public fun sign(x: Double): Double = when {
    x.isNaN() -> Double.NaN
    x > 0 -> 1.0
    x < 0 -> -1.0
    else -> 0.0
}

@SinceKotlin("1.2")
public fun min(a: Double, b: Double): Double = brsIntrinsicMinDouble(a, b)

@SinceKotlin("1.2")
public fun max(a: Double, b: Double): Double = brsIntrinsicMaxDouble(a, b)

@SinceKotlin("1.8")
public fun cbrt(x: Double): Double {
    // cbrt(x) = x^(1/3), preserving sign
    if (x.isNaN() || x.isInfinite() || x == 0.0) return x
    val sign = if (x < 0) -1.0 else 1.0
    return sign * brsIntrinsicPow(abs(x), 1.0 / 3.0)
}

// ============================================
// Double Extension Functions
// ============================================

@SinceKotlin("1.2")
public fun Double.pow(x: Double): Double = brsIntrinsicPow(this, x)

@SinceKotlin("1.2")
public fun Double.pow(n: Int): Double = brsIntrinsicPow(this, n.toDouble())

@SinceKotlin("1.2")
public val Double.absoluteValue: Double
    get() = abs(this)

@SinceKotlin("1.2")
public val Double.sign: Double
    get() = sign(this)

@SinceKotlin("1.2")
public fun Double.withSign(sign: Double): Double {
    val thisSign = if (this < 0 || (this == 0.0 && 1.0 / this < 0)) -1.0 else 1.0
    val newSign = if (sign < 0 || (sign == 0.0 && 1.0 / sign < 0)) -1.0 else 1.0
    return if (thisSign == newSign) this else -this
}

@SinceKotlin("1.2")
public fun Double.withSign(sign: Int): Double = withSign(sign.toDouble())

@SinceKotlin("1.2")
public val Double.ulp: Double
    get() {
        if (isNaN()) return Double.NaN
        if (isInfinite()) return Double.POSITIVE_INFINITY
        if (this == 0.0) return Double.MIN_VALUE
        // Approximate ulp calculation
        val bits = toBits()
        val nextBits = if (this > 0) bits + 1 else bits - 1
        return abs(Double.fromBits(nextBits) - this)
    }

@SinceKotlin("1.2")
public fun Double.nextUp(): Double {
    if (isNaN() || this == Double.POSITIVE_INFINITY) return this
    if (this == 0.0) return Double.MIN_VALUE
    val bits = toBits()
    return Double.fromBits(if (this > 0) bits + 1 else bits - 1)
}

@SinceKotlin("1.2")
public fun Double.nextDown(): Double {
    if (isNaN() || this == Double.NEGATIVE_INFINITY) return this
    if (this == 0.0) return -Double.MIN_VALUE
    val bits = toBits()
    return Double.fromBits(if (this > 0) bits - 1 else bits + 1)
}

@SinceKotlin("1.2")
public fun Double.nextTowards(to: Double): Double {
    if (isNaN() || to.isNaN()) return Double.NaN
    if (this == to) return this
    return if (to > this) nextUp() else nextDown()
}

@SinceKotlin("1.2")
public fun Double.roundToInt(): Int {
    if (isNaN()) throw IllegalArgumentException("Cannot round NaN to Int")
    if (this > Int.MAX_VALUE) return Int.MAX_VALUE
    if (this < Int.MIN_VALUE) return Int.MIN_VALUE
    return (this + 0.5).toInt()
}

@SinceKotlin("1.2")
public fun Double.roundToLong(): Long {
    if (isNaN()) throw IllegalArgumentException("Cannot round NaN to Long")
    if (this > Long.MAX_VALUE) return Long.MAX_VALUE
    if (this < Long.MIN_VALUE) return Long.MIN_VALUE
    return (this + 0.5).toLong()
}

// ============================================
// Float Functions (delegate to Double versions)
// ============================================

@SinceKotlin("1.2")
public fun sin(x: Float): Float = sin(x.toDouble()).toFloat()

@SinceKotlin("1.2")
public fun cos(x: Float): Float = cos(x.toDouble()).toFloat()

@SinceKotlin("1.2")
public fun tan(x: Float): Float = tan(x.toDouble()).toFloat()

@SinceKotlin("1.2")
public fun asin(x: Float): Float = asin(x.toDouble()).toFloat()

@SinceKotlin("1.2")
public fun acos(x: Float): Float = acos(x.toDouble()).toFloat()

@SinceKotlin("1.2")
public fun atan(x: Float): Float = atan(x.toDouble()).toFloat()

@SinceKotlin("1.2")
public fun atan2(y: Float, x: Float): Float = atan2(y.toDouble(), x.toDouble()).toFloat()

@SinceKotlin("1.2")
public fun sinh(x: Float): Float = sinh(x.toDouble()).toFloat()

@SinceKotlin("1.2")
public fun cosh(x: Float): Float = cosh(x.toDouble()).toFloat()

@SinceKotlin("1.2")
public fun tanh(x: Float): Float = tanh(x.toDouble()).toFloat()

@SinceKotlin("1.2")
public fun asinh(x: Float): Float = asinh(x.toDouble()).toFloat()

@SinceKotlin("1.2")
public fun acosh(x: Float): Float = acosh(x.toDouble()).toFloat()

@SinceKotlin("1.2")
public fun atanh(x: Float): Float = atanh(x.toDouble()).toFloat()

@SinceKotlin("1.2")
public fun hypot(x: Float, y: Float): Float = hypot(x.toDouble(), y.toDouble()).toFloat()

@SinceKotlin("1.2")
public fun sqrt(x: Float): Float = sqrt(x.toDouble()).toFloat()

@SinceKotlin("1.2")
public fun exp(x: Float): Float = exp(x.toDouble()).toFloat()

@SinceKotlin("1.2")
public fun expm1(x: Float): Float = expm1(x.toDouble()).toFloat()

@SinceKotlin("1.2")
public fun log(x: Float, base: Float): Float = log(x.toDouble(), base.toDouble()).toFloat()

@SinceKotlin("1.2")
public fun ln(x: Float): Float = ln(x.toDouble()).toFloat()

@SinceKotlin("1.2")
public fun log10(x: Float): Float = log10(x.toDouble()).toFloat()

@SinceKotlin("1.2")
public fun log2(x: Float): Float = log2(x.toDouble()).toFloat()

@SinceKotlin("1.2")
public fun ln1p(x: Float): Float = ln1p(x.toDouble()).toFloat()

@SinceKotlin("1.2")
public fun ceil(x: Float): Float = ceil(x.toDouble()).toFloat()

@SinceKotlin("1.2")
public fun floor(x: Float): Float = floor(x.toDouble()).toFloat()

@SinceKotlin("1.2")
public fun truncate(x: Float): Float = truncate(x.toDouble()).toFloat()

@SinceKotlin("1.2")
public fun round(x: Float): Float = round(x.toDouble()).toFloat()

@SinceKotlin("1.2")
public fun abs(x: Float): Float = abs(x.toDouble()).toFloat()

@SinceKotlin("1.2")
public fun sign(x: Float): Float = sign(x.toDouble()).toFloat()

@SinceKotlin("1.2")
public fun min(a: Float, b: Float): Float = min(a.toDouble(), b.toDouble()).toFloat()

@SinceKotlin("1.2")
public fun max(a: Float, b: Float): Float = max(a.toDouble(), b.toDouble()).toFloat()

@SinceKotlin("1.8")
public fun cbrt(x: Float): Float = cbrt(x.toDouble()).toFloat()

@SinceKotlin("1.2")
public fun Float.pow(x: Float): Float = this.toDouble().pow(x.toDouble()).toFloat()

@SinceKotlin("1.2")
public fun Float.pow(n: Int): Float = this.toDouble().pow(n).toFloat()

@SinceKotlin("1.2")
public val Float.absoluteValue: Float
    get() = abs(this)

@SinceKotlin("1.2")
public val Float.sign: Float
    get() = sign(this)

@SinceKotlin("1.2")
public fun Float.withSign(sign: Float): Float = this.toDouble().withSign(sign.toDouble()).toFloat()

@SinceKotlin("1.2")
public fun Float.withSign(sign: Int): Float = this.toDouble().withSign(sign).toFloat()

@SinceKotlin("1.2")
public fun Float.roundToInt(): Int = this.toDouble().roundToInt()

@SinceKotlin("1.2")
public fun Float.roundToLong(): Long = this.toDouble().roundToLong()

// ============================================
// Integer Functions
// ============================================

@SinceKotlin("1.2")
public fun abs(n: Int): Int = brsIntrinsicAbsInt(n)

@SinceKotlin("1.2")
public fun min(a: Int, b: Int): Int = brsIntrinsicMinInt(a, b)

@SinceKotlin("1.2")
public fun max(a: Int, b: Int): Int = brsIntrinsicMaxInt(a, b)

@SinceKotlin("1.2")
public val Int.absoluteValue: Int
    get() = abs(this)

@SinceKotlin("1.2")
public val Int.sign: Int
    get() = when {
        this > 0 -> 1
        this < 0 -> -1
        else -> 0
    }

@SinceKotlin("1.2")
public fun abs(n: Long): Long = if (n < 0) -n else n

@SinceKotlin("1.2")
public fun min(a: Long, b: Long): Long = if (a <= b) a else b

@SinceKotlin("1.2")
public fun max(a: Long, b: Long): Long = if (a >= b) a else b

@SinceKotlin("1.2")
public val Long.absoluteValue: Long
    get() = abs(this)

@SinceKotlin("1.2")
public val Long.sign: Int
    get() = when {
        this > 0L -> 1
        this < 0L -> -1
        else -> 0
    }
