/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
package kotlin.math

import kotlin.internal.InlineOnly

// region ================ Constants ========================================

private const val LN2 = 0.6931471805599453
private const val LOG2E = 1.4426950408889634
private const val LOG10E = 0.4342944819032518

// Taylor series bounds for small value approximations
private const val taylor_n_bound = 1.4901161193847656e-8   // 2^-26
private const val taylor_2_bound = 2.4414062499999998e-4   // 2^-12
private const val upper_taylor_n_bound = 1e10
private const val upper_taylor_2_bound = 1e300

// region ================ Double Math ========================================

/** Computes the sine of the angle [x] given in radians.
 *
 *  Special cases:
 *   - `sin(NaN|+Inf|-Inf)` is `NaN`
 */
@SinceKotlin("1.2")
public actual fun sin(x: Double): Double = kotlin.math.nativeSin(x)

/** Computes the cosine of the angle [x] given in radians.
 *
 *  Special cases:
 *   - `cos(NaN|+Inf|-Inf)` is `NaN`
 */
@SinceKotlin("1.2")
public actual fun cos(x: Double): Double = kotlin.math.nativeCos(x)

/** Computes the tangent of the angle [x] given in radians.
 *
 *  Special cases:
 *   - `tan(NaN|+Inf|-Inf)` is `NaN`
 */
@SinceKotlin("1.2")
public actual fun tan(x: Double): Double = kotlin.math.nativeTan(x)

/**
 * Computes the arc sine of the value [x];
 * the returned value is an angle in the range from `-PI/2` to `PI/2` radians.
 *
 * Special cases:
 *    - `asin(x)` is `NaN`, when `abs(x) > 1` or x is `NaN`
 */
@SinceKotlin("1.2")
public actual fun asin(x: Double): Double = kotlin.math.nativeAsin(x)

/**
 * Computes the arc cosine of the value [x];
 * the returned value is an angle in the range from `0.0` to `PI` radians.
 *
 * Special cases:
 *    - `acos(x)` is `NaN`, when `abs(x) > 1` or x is `NaN`
 */
@SinceKotlin("1.2")
public actual fun acos(x: Double): Double = kotlin.math.nativeAcos(x)

/**
 * Computes the arc tangent of the value [x];
 * the returned value is an angle in the range from `-PI/2` to `PI/2` radians.
 *
 * Special cases:
 *   - `atan(NaN)` is `NaN`
 */
@SinceKotlin("1.2")
public actual fun atan(x: Double): Double = kotlin.math.nativeAtan(x)

/**
 * Returns the angle `theta` of the polar coordinates `(r, theta)` that correspond
 * to the rectangular coordinates `(x, y)` by computing the arc tangent of the value [y] / [x];
 * the returned value is an angle in the range from `-PI` to `PI` radians.
 */
@SinceKotlin("1.2")
public actual fun atan2(y: Double, x: Double): Double = kotlin.math.nativeAtan2(y, x)

/**
 * Computes the hyperbolic sine of the value [x].
 *
 * Special cases:
 *   - `sinh(NaN)` is `NaN`
 *   - `sinh(+Inf)` is `+Inf`
 *   - `sinh(-Inf)` is `-Inf`
 */
@SinceKotlin("1.2")
public actual fun sinh(x: Double): Double {
    if (abs(x) < taylor_n_bound) {
        var result = x
        if (abs(x) > taylor_2_bound) {
            result += (x * x * x) / 6.0
        }
        return result
    } else {
        val y = exp(x)
        val y1 = 1.0 / y
        if (!y.isFinite()) return exp(x - LN2)
        if (!y1.isFinite()) return -exp(-x - LN2)
        return (y - y1) / 2.0
    }
}

/**
 * Computes the hyperbolic cosine of the value [x].
 *
 * Special cases:
 *   - `cosh(NaN)` is `NaN`
 *   - `cosh(+Inf|-Inf)` is `+Inf`
 */
@SinceKotlin("1.2")
public actual fun cosh(x: Double): Double {
    val y = exp(x)
    val y1 = 1.0 / y
    if (!y.isFinite() || !y1.isFinite()) return exp(abs(x) - LN2)
    return (y + y1) / 2.0
}

/**
 * Computes the hyperbolic tangent of the value [x].
 *
 * Special cases:
 *   - `tanh(NaN)` is `NaN`
 *   - `tanh(+Inf)` is `1.0`
 *   - `tanh(-Inf)` is `-1.0`
 */
@SinceKotlin("1.2")
public actual fun tanh(x: Double): Double {
    if (abs(x) < taylor_n_bound) {
        var result = x
        if (abs(x) > taylor_2_bound) {
            result -= (x * x * x) / 3.0
        }
        return result
    } else {
        val a = exp(x)
        val b = exp(-x)
        return if (a == Double.POSITIVE_INFINITY) 1.0 else if (b == Double.POSITIVE_INFINITY) -1.0 else (a - b) / (a + b)
    }
}

/**
 * Computes the inverse hyperbolic sine of the value [x].
 *
 * The returned value is `y` such that `sinh(y) == x`.
 */
@SinceKotlin("1.2")
public actual fun asinh(x: Double): Double {
    return when {
        x >= taylor_n_bound -> {
            when {
                x > upper_taylor_2_bound -> ln(x) + LN2
                x > upper_taylor_n_bound -> ln(x * 2.0 + 1.0 / (x * 2.0))
                else -> ln(x + sqrt(x * x + 1.0))
            }
        }
        x <= -taylor_n_bound -> -asinh(-x)
        else -> {
            var result = x
            if (abs(x) >= taylor_2_bound) {
                val x3 = x * x * x
                result -= x3 / 6.0
            }
            result
        }
    }
}

/**
 * Computes the inverse hyperbolic cosine of the value [x].
 *
 * The returned value is positive `y` such that `cosh(y) == x`.
 */
@SinceKotlin("1.2")
public actual fun acosh(x: Double): Double {
    return when {
        x < 1.0 -> Double.NaN
        x - 1.0 >= taylor_n_bound -> {
            if (x > upper_taylor_2_bound) {
                ln(x) + LN2
            } else {
                ln(x + sqrt(x * x - 1.0))
            }
        }
        else -> {
            val y = sqrt(x - 1.0)
            var result = y
            if (y >= taylor_2_bound) {
                val y3 = y * y * y
                result -= y3 / 12.0
            }
            sqrt(2.0) * result
        }
    }
}

/**
 * Computes the inverse hyperbolic tangent of the value [x].
 *
 * The returned value is `y` such that `tanh(y) == x`.
 */
@SinceKotlin("1.2")
public actual fun atanh(x: Double): Double {
    if (abs(x) < taylor_n_bound) {
        var result = x
        if (abs(x) > taylor_2_bound) {
            result += (x * x * x) / 3.0
        }
        return result
    }
    return ln((1.0 + x) / (1.0 - x)) / 2.0
}

/**
 * Computes `sqrt(x^2 + y^2)` without intermediate overflow or underflow.
 */
@SinceKotlin("1.2")
public actual fun hypot(x: Double, y: Double): Double {
    if (x.isInfinite() || y.isInfinite()) return Double.POSITIVE_INFINITY
    if (x.isNaN() || y.isNaN()) return Double.NaN
    return sqrt(x * x + y * y)
}

/**
 * Computes the positive square root of the value [x].
 */
@SinceKotlin("1.2")
public actual fun sqrt(x: Double): Double = kotlin.math.nativeSqrt(x)

/**
 * Computes Euler's number `e` raised to the power of the value [x].
 */
@SinceKotlin("1.2")
public actual fun exp(x: Double): Double = kotlin.math.nativeExp(x)

/**
 * Computes `exp(x) - 1`.
 *
 * This function can be implemented to produce more precise result for [x] near zero.
 */
@SinceKotlin("1.2")
public actual fun expm1(x: Double): Double {
    if (abs(x) < taylor_n_bound) {
        val x2 = x * x
        val x3 = x2 * x
        val x4 = x3 * x
        return x4 / 24.0 + x3 / 6.0 + x2 / 2.0 + x
    }
    return exp(x) - 1.0
}

/**
 * Computes the logarithm of the value [x] to the given [base].
 */
@SinceKotlin("1.2")
public actual fun log(x: Double, base: Double): Double {
    if (base <= 0.0 || base == 1.0) return Double.NaN
    return ln(x) / ln(base)
}

/**
 * Computes the natural logarithm (base `E`) of the value [x].
 */
@SinceKotlin("1.2")
public actual fun ln(x: Double): Double = kotlin.math.nativeLog(x)

/**
 * Computes the common logarithm (base 10) of the value [x].
 */
@SinceKotlin("1.2")
public actual fun log10(x: Double): Double = ln(x) * LOG10E

/**
 * Computes the binary logarithm (base 2) of the value [x].
 */
@SinceKotlin("1.2")
public actual fun log2(x: Double): Double = ln(x) * LOG2E

/**
 * Computes `ln(x + 1)`.
 *
 * This function can be implemented to produce more precise result for [x] near zero.
 */
@SinceKotlin("1.2")
public actual fun ln1p(x: Double): Double {
    if (abs(x) < taylor_n_bound) {
        val x2 = x * x
        val x3 = x2 * x
        val x4 = x3 * x
        return -x4 / 4.0 + x3 / 3.0 - x2 / 2.0 + x
    }
    return ln(x + 1.0)
}

/**
 * Rounds the given value [x] to an integer towards positive infinity.
 */
@SinceKotlin("1.2")
public actual fun ceil(x: Double): Double = kotlin.math.nativeCeil(x)

/**
 * Rounds the given value [x] to an integer towards negative infinity.
 */
@SinceKotlin("1.2")
public actual fun floor(x: Double): Double = kotlin.math.nativeFloor(x)

/**
 * Rounds the given value [x] to an integer towards zero.
 */
@SinceKotlin("1.2")
public actual fun truncate(x: Double): Double {
    return if (x.isNaN()) Double.NaN
    else if (x > 0) floor(x)
    else ceil(x)
}

/**
 * Rounds the given value [x] towards the closest integer with ties rounded towards even integer.
 */
@SinceKotlin("1.2")
public actual fun round(x: Double): Double {
    if (x % 0.5 != 0.0) {
        return kotlin.math.nativeRound(x)
    }
    val floorVal = floor(x)
    return if (floorVal % 2.0 == 0.0) floorVal else ceil(x)
}

/**
 * Returns the absolute value of the given value [x].
 */
@SinceKotlin("1.2")
public actual fun abs(x: Double): Double = kotlin.math.nativeAbs(x)

/**
 * Returns the sign of the given value [x]:
 *   - `-1.0` if the value is negative,
 *   - zero if the value is zero,
 *   - `1.0` if the value is positive
 */
@SinceKotlin("1.2")
public actual fun sign(x: Double): Double {
    if (x.isNaN()) return Double.NaN
    if (x == 0.0) return x
    return if (x > 0) 1.0 else -1.0
}

/**
 * Returns the smaller of two values.
 */
@SinceKotlin("1.2")
public actual fun min(a: Double, b: Double): Double = if (a.isNaN() || b.isNaN()) Double.NaN else if (a < b) a else b

/**
 * Returns the greater of two values.
 */
@SinceKotlin("1.2")
public actual fun max(a: Double, b: Double): Double = if (a.isNaN() || b.isNaN()) Double.NaN else if (a > b) a else b

/**
 * Returns the cube root of [x].
 */
@SinceKotlin("1.8")
@WasExperimental(ExperimentalStdlibApi::class)
public actual fun cbrt(x: Double): Double {
    if (x.isNaN() || x.isInfinite() || x == 0.0) return x
    val negative = x < 0
    val absX = if (negative) -x else x
    val result = exp(ln(absX) / 3.0)
    return if (negative) -result else result
}

// extensions

/**
 * Raises this value to the power [x].
 */
@SinceKotlin("1.2")
public actual fun Double.pow(x: Double): Double = kotlin.math.nativePow(this, x)

/**
 * Raises this value to the integer power [n].
 */
@SinceKotlin("1.2")
public actual fun Double.pow(n: Int): Double = kotlin.math.nativePow(this, n.toDouble())

/**
 * Returns the absolute value of this value.
 */
@SinceKotlin("1.2")
@InlineOnly
public actual inline val Double.absoluteValue: Double get() = abs(this)

/**
 * Returns the sign of this value.
 */
@SinceKotlin("1.2")
@InlineOnly
public actual inline val Double.sign: Double get() = sign(this)

/**
 * Returns this value with the sign bit same as of the [sign] value.
 */
@SinceKotlin("1.2")
public actual fun Double.withSign(sign: Double): Double {
    val thisSignBit = this < 0.0 || (this == 0.0 && 1.0 / this < 0.0)
    val newSignBit = sign < 0.0 || (sign == 0.0 && 1.0 / sign < 0.0)
    return if (thisSignBit == newSignBit) this else -this
}

/**
 * Returns this value with the sign bit same as of the [sign] value.
 */
@SinceKotlin("1.2")
@InlineOnly
public actual inline fun Double.withSign(sign: Int): Double = this.withSign(sign.toDouble())

/**
 * Returns the ulp (unit in the last place) of this value.
 */
@SinceKotlin("1.2")
public actual val Double.ulp: Double get() = when {
    this < 0 -> (-this).ulp
    this.isNaN() || this == Double.POSITIVE_INFINITY -> this
    this == Double.MAX_VALUE -> this - this.nextDown()
    else -> this.nextUp() - this
}

/**
 * Returns the [Double] value nearest to this value in direction of positive infinity.
 */
@SinceKotlin("1.2")
public actual fun Double.nextUp(): Double = when {
    this.isNaN() || this == Double.POSITIVE_INFINITY -> this
    this == 0.0 -> Double.MIN_VALUE
    else -> Double.fromBits(this.toRawBits() + if (this > 0) 1 else -1)
}

/**
 * Returns the [Double] value nearest to this value in direction of negative infinity.
 */
@SinceKotlin("1.2")
public actual fun Double.nextDown(): Double = when {
    this.isNaN() || this == Double.NEGATIVE_INFINITY -> this
    this == 0.0 -> -Double.MIN_VALUE
    else -> Double.fromBits(this.toRawBits() + if (this > 0) -1 else 1)
}

/**
 * Returns the [Double] value nearest to this value in direction from this value towards the value [to].
 */
@SinceKotlin("1.2")
public actual fun Double.nextTowards(to: Double): Double = when {
    this.isNaN() || to.isNaN() -> Double.NaN
    to == this -> to
    to > this -> this.nextUp()
    else /* to < this */ -> this.nextDown()
}

/**
 * Rounds this [Double] value to the nearest integer and converts the result to [Int].
 */
@SinceKotlin("1.2")
public actual fun Double.roundToInt(): Int = when {
    isNaN() -> throw IllegalArgumentException("Cannot round NaN value.")
    this > Int.MAX_VALUE -> Int.MAX_VALUE
    this < Int.MIN_VALUE -> Int.MIN_VALUE
    else -> kotlin.math.nativeRound(this).toInt()
}

/**
 * Rounds this [Double] value to the nearest integer and converts the result to [Long].
 */
@SinceKotlin("1.2")
public actual fun Double.roundToLong(): Long = when {
    isNaN() -> throw IllegalArgumentException("Cannot round NaN value.")
    this > Long.MAX_VALUE -> Long.MAX_VALUE
    this < Long.MIN_VALUE -> Long.MIN_VALUE
    else -> kotlin.math.nativeRound(this).toLong()
}

// endregion

// region ================ Float Math ========================================

/** Computes the sine of the angle [x] given in radians. */
@SinceKotlin("1.2")
@InlineOnly
public actual inline fun sin(x: Float): Float = sin(x.toDouble()).toFloat()

/** Computes the cosine of the angle [x] given in radians. */
@SinceKotlin("1.2")
@InlineOnly
public actual inline fun cos(x: Float): Float = cos(x.toDouble()).toFloat()

/** Computes the tangent of the angle [x] given in radians. */
@SinceKotlin("1.2")
@InlineOnly
public actual inline fun tan(x: Float): Float = tan(x.toDouble()).toFloat()

/** Computes the arc sine of the value [x]. */
@SinceKotlin("1.2")
@InlineOnly
public actual inline fun asin(x: Float): Float = asin(x.toDouble()).toFloat()

/** Computes the arc cosine of the value [x]. */
@SinceKotlin("1.2")
@InlineOnly
public actual inline fun acos(x: Float): Float = acos(x.toDouble()).toFloat()

/** Computes the arc tangent of the value [x]. */
@SinceKotlin("1.2")
@InlineOnly
public actual inline fun atan(x: Float): Float = atan(x.toDouble()).toFloat()

/** Returns the angle `theta` of polar coordinates `(r, theta)`. */
@SinceKotlin("1.2")
@InlineOnly
public actual inline fun atan2(y: Float, x: Float): Float = atan2(y.toDouble(), x.toDouble()).toFloat()

/** Computes the hyperbolic sine of the value [x]. */
@SinceKotlin("1.2")
@InlineOnly
public actual inline fun sinh(x: Float): Float = sinh(x.toDouble()).toFloat()

/** Computes the hyperbolic cosine of the value [x]. */
@SinceKotlin("1.2")
@InlineOnly
public actual inline fun cosh(x: Float): Float = cosh(x.toDouble()).toFloat()

/** Computes the hyperbolic tangent of the value [x]. */
@SinceKotlin("1.2")
@InlineOnly
public actual inline fun tanh(x: Float): Float = tanh(x.toDouble()).toFloat()

/** Computes the inverse hyperbolic sine of the value [x]. */
@SinceKotlin("1.2")
@InlineOnly
public actual inline fun asinh(x: Float): Float = asinh(x.toDouble()).toFloat()

/** Computes the inverse hyperbolic cosine of the value [x]. */
@SinceKotlin("1.2")
@InlineOnly
public actual inline fun acosh(x: Float): Float = acosh(x.toDouble()).toFloat()

/** Computes the inverse hyperbolic tangent of the value [x]. */
@SinceKotlin("1.2")
@InlineOnly
public actual inline fun atanh(x: Float): Float = atanh(x.toDouble()).toFloat()

/** Computes `sqrt(x^2 + y^2)` without intermediate overflow or underflow. */
@SinceKotlin("1.2")
@InlineOnly
public actual inline fun hypot(x: Float, y: Float): Float = hypot(x.toDouble(), y.toDouble()).toFloat()

/** Computes the positive square root of the value [x]. */
@SinceKotlin("1.2")
@InlineOnly
public actual inline fun sqrt(x: Float): Float = sqrt(x.toDouble()).toFloat()

/** Computes Euler's number `e` raised to the power of the value [x]. */
@SinceKotlin("1.2")
@InlineOnly
public actual inline fun exp(x: Float): Float = exp(x.toDouble()).toFloat()

/** Computes `exp(x) - 1`. */
@SinceKotlin("1.2")
@InlineOnly
public actual inline fun expm1(x: Float): Float = expm1(x.toDouble()).toFloat()

/** Computes the logarithm of the value [x] to the given [base]. */
@SinceKotlin("1.2")
@InlineOnly
public actual inline fun log(x: Float, base: Float): Float = log(x.toDouble(), base.toDouble()).toFloat()

/** Computes the natural logarithm (base `E`) of the value [x]. */
@SinceKotlin("1.2")
@InlineOnly
public actual inline fun ln(x: Float): Float = ln(x.toDouble()).toFloat()

/** Computes the common logarithm (base 10) of the value [x]. */
@SinceKotlin("1.2")
@InlineOnly
public actual inline fun log10(x: Float): Float = log10(x.toDouble()).toFloat()

/** Computes the binary logarithm (base 2) of the value [x]. */
@SinceKotlin("1.2")
@InlineOnly
public actual inline fun log2(x: Float): Float = log2(x.toDouble()).toFloat()

/** Computes `ln(x + 1)`. */
@SinceKotlin("1.2")
@InlineOnly
public actual inline fun ln1p(x: Float): Float = ln1p(x.toDouble()).toFloat()

/** Rounds the given value [x] to an integer towards positive infinity. */
@SinceKotlin("1.2")
@InlineOnly
public actual inline fun ceil(x: Float): Float = ceil(x.toDouble()).toFloat()

/** Rounds the given value [x] to an integer towards negative infinity. */
@SinceKotlin("1.2")
@InlineOnly
public actual inline fun floor(x: Float): Float = floor(x.toDouble()).toFloat()

/** Rounds the given value [x] to an integer towards zero. */
@SinceKotlin("1.2")
@InlineOnly
public actual inline fun truncate(x: Float): Float = truncate(x.toDouble()).toFloat()

/** Rounds the given value [x] towards the closest integer with ties rounded towards even integer. */
@SinceKotlin("1.2")
@InlineOnly
public actual inline fun round(x: Float): Float = round(x.toDouble()).toFloat()

/** Returns the absolute value of the given value [x]. */
@SinceKotlin("1.2")
@InlineOnly
public actual inline fun abs(x: Float): Float = abs(x.toDouble()).toFloat()

/** Returns the sign of the given value [x]. */
@SinceKotlin("1.2")
@InlineOnly
public actual inline fun sign(x: Float): Float = sign(x.toDouble()).toFloat()

/** Returns the smaller of two values. */
@SinceKotlin("1.2")
@InlineOnly
public actual inline fun min(a: Float, b: Float): Float = min(a.toDouble(), b.toDouble()).toFloat()

/** Returns the greater of two values. */
@SinceKotlin("1.2")
@InlineOnly
public actual inline fun max(a: Float, b: Float): Float = max(a.toDouble(), b.toDouble()).toFloat()

/** Returns the cube root of [x]. */
@SinceKotlin("1.8")
@WasExperimental(ExperimentalStdlibApi::class)
@InlineOnly
public actual inline fun cbrt(x: Float): Float = cbrt(x.toDouble()).toFloat()

/** Raises this value to the power [x]. */
@SinceKotlin("1.2")
@InlineOnly
public actual inline fun Float.pow(x: Float): Float = this.toDouble().pow(x.toDouble()).toFloat()

/** Raises this value to the integer power [n]. */
@SinceKotlin("1.2")
@InlineOnly
public actual inline fun Float.pow(n: Int): Float = this.toDouble().pow(n).toFloat()

/** Returns the absolute value of this value. */
@SinceKotlin("1.2")
@InlineOnly
public actual inline val Float.absoluteValue: Float get() = abs(this)

/** Returns the sign of this value. */
@SinceKotlin("1.2")
@InlineOnly
public actual inline val Float.sign: Float get() = sign(this)

/** Returns this value with the sign bit same as of the [sign] value. */
@SinceKotlin("1.2")
@InlineOnly
public actual inline fun Float.withSign(sign: Float): Float = this.toDouble().withSign(sign.toDouble()).toFloat()

/** Returns this value with the sign bit same as of the [sign] value. */
@SinceKotlin("1.2")
@InlineOnly
public actual inline fun Float.withSign(sign: Int): Float = this.toDouble().withSign(sign.toDouble()).toFloat()

/** Rounds this [Float] value to the nearest integer and converts the result to [Int]. */
@SinceKotlin("1.2")
@InlineOnly
public actual inline fun Float.roundToInt(): Int = toDouble().roundToInt()

/** Rounds this [Float] value to the nearest integer and converts the result to [Long]. */
@SinceKotlin("1.2")
@InlineOnly
public actual inline fun Float.roundToLong(): Long = toDouble().roundToLong()

// endregion

// region ================ Integer Math ========================================

/**
 * Returns the absolute value of the given value [n].
 *
 * Special cases:
 *   - `abs(Int.MIN_VALUE)` is `Int.MIN_VALUE` due to an overflow
 */
@SinceKotlin("1.2")
public actual fun abs(n: Int): Int = if (n < 0) (-n or 0) else n

/**
 * Returns the smaller of two values.
 */
@SinceKotlin("1.2")
@InlineOnly
public actual inline fun min(a: Int, b: Int): Int = if (a <= b) a else b

/**
 * Returns the greater of two values.
 */
@SinceKotlin("1.2")
@InlineOnly
public actual inline fun max(a: Int, b: Int): Int = if (a >= b) a else b

/**
 * Returns the absolute value of this value.
 */
@SinceKotlin("1.2")
@InlineOnly
public actual inline val Int.absoluteValue: Int get() = abs(this)

/**
 * Returns the sign of this value.
 */
@SinceKotlin("1.2")
public actual val Int.sign: Int get() = (this shr (Int.SIZE_BITS - 1)) or (-this ushr (Int.SIZE_BITS - 1))

/**
 * Returns the absolute value of the given value [n].
 */
@SinceKotlin("1.2")
public actual fun abs(n: Long): Long = if (n < 0) -n else n

/**
 * Returns the smaller of two values.
 */
@SinceKotlin("1.2")
@Suppress("NOTHING_TO_INLINE")
public actual inline fun min(a: Long, b: Long): Long = if (a <= b) a else b

/**
 * Returns the greater of two values.
 */
@SinceKotlin("1.2")
@Suppress("NOTHING_TO_INLINE")
public actual inline fun max(a: Long, b: Long): Long = if (a >= b) a else b

/**
 * Returns the absolute value of this value.
 */
@SinceKotlin("1.2")
@InlineOnly
public actual inline val Long.absoluteValue: Long get() = abs(this)

/**
 * Returns the sign of this value.
 */
@SinceKotlin("1.2")
public actual val Long.sign: Int get() = ((this shr (Long.SIZE_BITS - 1)) or (-this ushr (Long.SIZE_BITS - 1))).toInt()

// endregion

// region ================ Native Math Functions (to be provided by runtime) ========

// These functions will be implemented by the BrightScript backend runtime
internal expect fun nativeSin(x: Double): Double
internal expect fun nativeCos(x: Double): Double
internal expect fun nativeTan(x: Double): Double
internal expect fun nativeAsin(x: Double): Double
internal expect fun nativeAcos(x: Double): Double
internal expect fun nativeAtan(x: Double): Double
internal expect fun nativeAtan2(y: Double, x: Double): Double
internal expect fun nativeSqrt(x: Double): Double
internal expect fun nativeExp(x: Double): Double
internal expect fun nativeLog(x: Double): Double
internal expect fun nativeCeil(x: Double): Double
internal expect fun nativeFloor(x: Double): Double
internal expect fun nativeRound(x: Double): Double
internal expect fun nativeAbs(x: Double): Double
internal expect fun nativePow(base: Double, exponent: Double): Double

// endregion
