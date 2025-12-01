/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs.time

import kotlin.math.*

/**
 * BrightScript-specific duration formatting utilities.
 */

internal inline val brsDurationAssertionsEnabled: Boolean get() = true

internal fun formatToExactDecimals(value: Double, decimals: Int): String {
    val rounded = if (decimals == 0) {
        value
    } else {
        val pow = 10.0.pow(decimals)
        round(abs(value) * pow) / pow * sign(value)
    }

    if (abs(rounded) >= 1e21) {
        // Handle very large numbers with scientific notation
        val positive = abs(rounded)
        val positiveString = formatLargeNumber(positive, decimals)
        return if (rounded < 0) "-$positiveString" else positiveString
    }

    return formatFixedDecimals(rounded, decimals)
}

private fun formatFixedDecimals(value: Double, decimals: Int): String {
    if (decimals == 0) {
        return value.toLong().toString()
    }

    val longPart = value.toLong()
    val fractionalPart = abs(value - longPart)

    val multiplier = 10.0.pow(decimals)
    val fractionalDigits = (fractionalPart * multiplier + 0.5).toLong()

    val fractionalString = fractionalDigits.toString().padStart(decimals, '0')

    return if (value < 0 && longPart == 0L) {
        "-0.$fractionalString"
    } else {
        "$longPart.$fractionalString"
    }
}

private fun formatLargeNumber(value: Double, decimals: Int): String {
    val exponent = floor(log10(value)).toInt()
    val mantissa = value / 10.0.pow(exponent)
    val mantissaString = formatFixedDecimals(mantissa, decimals + exponent)
    return mantissaString
}
