/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs.runtime

/**
 * Math runtime functions for BrightScript target.
 * These are lowered by the BRS backend to native BrightScript math operations.
 */

// ============================================
// Trigonometric Functions
// Maps to BrightScript built-in functions
// ============================================

/**
 * Computes the sine of an angle in radians.
 * Lowered to: Sin(x)
 */
@PublishedApi
internal external fun brsIntrinsicSin(x: Double): Double

/**
 * Computes the cosine of an angle in radians.
 * Lowered to: Cos(x)
 */
@PublishedApi
internal external fun brsIntrinsicCos(x: Double): Double

/**
 * Computes the tangent of an angle in radians.
 * Lowered to: Tan(x)
 */
@PublishedApi
internal external fun brsIntrinsicTan(x: Double): Double

/**
 * Computes the arctangent of a value.
 * Lowered to: Atn(x)
 */
@PublishedApi
internal external fun brsIntrinsicAtan(x: Double): Double

// ============================================
// Exponential and Logarithmic Functions
// ============================================

/**
 * Computes the natural exponential of a value.
 * Lowered to: Exp(x)
 */
@PublishedApi
internal external fun brsIntrinsicExp(x: Double): Double

/**
 * Computes the natural logarithm of a value.
 * Lowered to: Log(x)
 */
@PublishedApi
internal external fun brsIntrinsicLog(x: Double): Double

/**
 * Computes the square root of a value.
 * Lowered to: Sqr(x)
 */
@PublishedApi
internal external fun brsIntrinsicSqrt(x: Double): Double

// ============================================
// Rounding and Absolute Value Functions
// ============================================

/**
 * Computes the absolute value of a number.
 * Lowered to: Abs(x)
 */
@PublishedApi
internal external fun brsIntrinsicAbs(x: Double): Double

/**
 * Computes the absolute value of an integer.
 * Lowered to: Abs(x)
 */
@PublishedApi
internal external fun brsIntrinsicAbsInt(x: Int): Int

/**
 * Truncates a floating-point number to an integer.
 * Lowered to: Int(x)
 */
@PublishedApi
internal external fun brsIntrinsicTrunc(x: Double): Int

/**
 * Computes the floor of a value (largest integer <= x).
 * Lowered to: Int(x) for positive, Int(x) - 1 for negative non-integers
 */
@PublishedApi
internal external fun brsIntrinsicFloor(x: Double): Double

/**
 * Computes the ceiling of a value (smallest integer >= x).
 * BrightScript doesn't have a native ceiling function.
 * Lowered to: -Int(-x) or Int(x) + 1 for non-integers
 */
@PublishedApi
internal external fun brsIntrinsicCeil(x: Double): Double

/**
 * Rounds a value to the nearest integer.
 * Lowered to: Int(x + 0.5) for positive, Int(x - 0.5) for negative
 */
@PublishedApi
internal external fun brsIntrinsicRound(x: Double): Long

// ============================================
// Power and Modulo Functions
// ============================================

/**
 * Computes base raised to the power of exponent.
 * Lowered to: base ^ exponent
 */
@PublishedApi
internal external fun brsIntrinsicPow(base: Double, exponent: Double): Double

// ============================================
// Min/Max Functions
// These are typically inlined by the backend
// ============================================

/**
 * Returns the smaller of two double values.
 */
@PublishedApi
internal fun brsIntrinsicMinDouble(a: Double, b: Double): Double {
    return if (a <= b) a else b
}

/**
 * Returns the larger of two double values.
 */
@PublishedApi
internal fun brsIntrinsicMaxDouble(a: Double, b: Double): Double {
    return if (a >= b) a else b
}

/**
 * Returns the smaller of two integer values.
 */
@PublishedApi
internal fun brsIntrinsicMinInt(a: Int, b: Int): Int {
    return if (a <= b) a else b
}

/**
 * Returns the larger of two integer values.
 */
@PublishedApi
internal fun brsIntrinsicMaxInt(a: Int, b: Int): Int {
    return if (a >= b) a else b
}

// ============================================
// Special Value Checks
// ============================================

/**
 * Checks if a double value is NaN.
 * In BrightScript, NaN can be created but checking requires comparison tricks.
 */
@PublishedApi
internal fun brsIntrinsicIsNaN(x: Double): Boolean {
    // NaN is the only value that is not equal to itself
    return x != x
}

/**
 * Checks if a float value is NaN.
 */
@PublishedApi
internal fun brsIntrinsicIsNaNFloat(x: Float): Boolean {
    return x != x
}

/**
 * Checks if a double value is infinite.
 */
@PublishedApi
internal fun brsIntrinsicIsInfinite(x: Double): Boolean {
    return x == Double.POSITIVE_INFINITY || x == Double.NEGATIVE_INFINITY
}

/**
 * Checks if a float value is infinite.
 */
@PublishedApi
internal fun brsIntrinsicIsInfiniteFloat(x: Float): Boolean {
    return x == Float.POSITIVE_INFINITY || x == Float.NEGATIVE_INFINITY
}

// ============================================
// Random Number Generation
// ============================================

/**
 * Generates a random number.
 * Lowered to: Rnd(range) or Rnd(0) for seeding
 */
@PublishedApi
internal external fun brsIntrinsicRandom(range: Int): Int

/**
 * Seeds the random number generator.
 * Lowered to: Rnd(0)
 */
@PublishedApi
internal external fun brsIntrinsicRandomSeed()
