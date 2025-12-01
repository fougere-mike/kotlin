/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.math

/**
 * Native math functions for BrightScript.
 *
 * These functions map to BrightScript's roMath component or equivalent runtime implementations.
 * BrightScript provides: Sin, Cos, Tan, Atn (atan), Sqr (sqrt), Exp, Log, Abs, Pow (via ^ operator)
 */

// The actual implementations will be provided by the BrightScript backend lowering phase.
// These are placeholder actual declarations that will be intrinsified by the compiler.

internal actual fun nativeSin(x: Double): Double = brsIntrinsicSin(x)
internal actual fun nativeCos(x: Double): Double = brsIntrinsicCos(x)
internal actual fun nativeTan(x: Double): Double = brsIntrinsicTan(x)
internal actual fun nativeAsin(x: Double): Double = brsIntrinsicAsin(x)
internal actual fun nativeAcos(x: Double): Double = brsIntrinsicAcos(x)
internal actual fun nativeAtan(x: Double): Double = brsIntrinsicAtan(x)
internal actual fun nativeAtan2(y: Double, x: Double): Double = brsIntrinsicAtan2(y, x)
internal actual fun nativeSqrt(x: Double): Double = brsIntrinsicSqrt(x)
internal actual fun nativeExp(x: Double): Double = brsIntrinsicExp(x)
internal actual fun nativeLog(x: Double): Double = brsIntrinsicLog(x)
internal actual fun nativeCeil(x: Double): Double = brsIntrinsicCeil(x)
internal actual fun nativeFloor(x: Double): Double = brsIntrinsicFloor(x)
internal actual fun nativeRound(x: Double): Double = brsIntrinsicRound(x)
internal actual fun nativeAbs(x: Double): Double = brsIntrinsicAbs(x)
internal actual fun nativePow(base: Double, exponent: Double): Double = brsIntrinsicPow(base, exponent)

// These are intrinsic functions that will be replaced by the BrightScript backend
// with direct BrightScript function calls or expressions
@PublishedApi
internal external fun brsIntrinsicSin(x: Double): Double

@PublishedApi
internal external fun brsIntrinsicCos(x: Double): Double

@PublishedApi
internal external fun brsIntrinsicTan(x: Double): Double

@PublishedApi
internal external fun brsIntrinsicAsin(x: Double): Double

@PublishedApi
internal external fun brsIntrinsicAcos(x: Double): Double

@PublishedApi
internal external fun brsIntrinsicAtan(x: Double): Double

@PublishedApi
internal external fun brsIntrinsicAtan2(y: Double, x: Double): Double

@PublishedApi
internal external fun brsIntrinsicSqrt(x: Double): Double

@PublishedApi
internal external fun brsIntrinsicExp(x: Double): Double

@PublishedApi
internal external fun brsIntrinsicLog(x: Double): Double

@PublishedApi
internal external fun brsIntrinsicCeil(x: Double): Double

@PublishedApi
internal external fun brsIntrinsicFloor(x: Double): Double

@PublishedApi
internal external fun brsIntrinsicRound(x: Double): Double

@PublishedApi
internal external fun brsIntrinsicAbs(x: Double): Double

@PublishedApi
internal external fun brsIntrinsicPow(base: Double, exponent: Double): Double
