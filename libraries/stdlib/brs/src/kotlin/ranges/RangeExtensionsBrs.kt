/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.ranges

// ============================================
// Progression Reversal
// ============================================

/**
 * Returns a progression that goes over the same range in the opposite direction with the same step.
 */
public fun IntProgression.reversed(): IntProgression = IntProgression.fromClosedRange(last, first, -step)

/**
 * Returns a progression that goes over the same range in the opposite direction with the same step.
 */
public fun LongProgression.reversed(): LongProgression = LongProgression.fromClosedRange(last, first, -step)

/**
 * Returns a progression that goes over the same range in the opposite direction with the same step.
 */
public fun CharProgression.reversed(): CharProgression = CharProgression.fromClosedRange(last, first, -step)

// ============================================
// Coerce In Range (Long)
// ============================================

/**
 * Ensures that this value lies in the specified range [minimumValue]..[maximumValue].
 *
 * @return this value if it's in the range, or [minimumValue] if this value is less than [minimumValue],
 * or [maximumValue] if this value is greater than [maximumValue].
 */
public fun Long.coerceIn(minimumValue: Long, maximumValue: Long): Long {
    if (minimumValue > maximumValue) throw IllegalArgumentException("Cannot coerce value to an empty range: maximum $maximumValue is less than minimum $minimumValue.")
    if (this < minimumValue) return minimumValue
    if (this > maximumValue) return maximumValue
    return this
}

/**
 * Ensures that this value lies in the specified [range].
 *
 * @return this value if it's in the range, or range.start if this value is less than range.start,
 * or range.endInclusive if this value is greater than range.endInclusive.
 */
public fun Long.coerceIn(range: ClosedRange<Long>): Long {
    if (range is ClosedFloatingPointRange) {
        return this.coerceIn(range as ClosedFloatingPointRange<Long>)
    }
    if (range.isEmpty()) throw IllegalArgumentException("Cannot coerce value to an empty range: $range.")
    return when {
        this < range.start -> range.start
        this > range.endInclusive -> range.endInclusive
        else -> this
    }
}

/**
 * Ensures that this value lies in the specified [range].
 *
 * @return this value if it's in the range, or range.start if this value is less than range.start,
 * or range.endExclusive-1 if this value is greater than or equal to range.endExclusive.
 */
@SinceKotlin("1.9")
public fun Long.coerceIn(range: OpenEndRange<Long>): Long {
    if (range.isEmpty()) throw IllegalArgumentException("Cannot coerce value to an empty range: $range.")
    return when {
        this < range.start -> range.start
        this >= range.endExclusive -> range.endExclusive - 1
        else -> this
    }
}

// ============================================
// Coerce In Range (Char)
// ============================================

/**
 * Ensures that this value lies in the specified range [minimumValue]..[maximumValue].
 *
 * @return this value if it's in the range, or [minimumValue] if this value is less than [minimumValue],
 * or [maximumValue] if this value is greater than [maximumValue].
 */
public fun Char.coerceIn(minimumValue: Char, maximumValue: Char): Char {
    if (minimumValue > maximumValue) throw IllegalArgumentException("Cannot coerce value to an empty range: maximum $maximumValue is less than minimum $minimumValue.")
    if (this < minimumValue) return minimumValue
    if (this > maximumValue) return maximumValue
    return this
}

/**
 * Ensures that this value lies in the specified [range].
 *
 * @return this value if it's in the range, or range.start if this value is less than range.start,
 * or range.endInclusive if this value is greater than range.endInclusive.
 */
public fun Char.coerceIn(range: ClosedRange<Char>): Char {
    if (range.isEmpty()) throw IllegalArgumentException("Cannot coerce value to an empty range: $range.")
    return when {
        this < range.start -> range.start
        this > range.endInclusive -> range.endInclusive
        else -> this
    }
}

/**
 * Ensures that this value lies in the specified [range].
 *
 * @return this value if it's in the range, or range.start if this value is less than range.start,
 * or range.endExclusive-1 if this value is greater than or equal to range.endExclusive.
 */
@SinceKotlin("1.9")
public fun Char.coerceIn(range: OpenEndRange<Char>): Char {
    if (range.isEmpty()) throw IllegalArgumentException("Cannot coerce value to an empty range: $range.")
    return when {
        this < range.start -> range.start
        this >= range.endExclusive -> (range.endExclusive.code - 1).toChar()
        else -> this
    }
}

// ============================================
// Coerce At Least/At Most
// ============================================

/**
 * Ensures that this value is not less than the specified [minimumValue].
 *
 * @return this value if it's greater than or equal to the [minimumValue] or the [minimumValue] otherwise.
 */
public fun Long.coerceAtLeast(minimumValue: Long): Long {
    return if (this < minimumValue) minimumValue else this
}

/**
 * Ensures that this value is not greater than the specified [maximumValue].
 *
 * @return this value if it's less than or equal to the [maximumValue] or the [maximumValue] otherwise.
 */
public fun Long.coerceAtMost(maximumValue: Long): Long {
    return if (this > maximumValue) maximumValue else this
}

/**
 * Ensures that this value is not less than the specified [minimumValue].
 *
 * @return this value if it's greater than or equal to the [minimumValue] or the [minimumValue] otherwise.
 */
public fun Char.coerceAtLeast(minimumValue: Char): Char {
    return if (this < minimumValue) minimumValue else this
}

/**
 * Ensures that this value is not greater than the specified [maximumValue].
 *
 * @return this value if it's less than or equal to the [maximumValue] or the [maximumValue] otherwise.
 */
public fun Char.coerceAtMost(maximumValue: Char): Char {
    return if (this > maximumValue) maximumValue else this
}

/**
 * Ensures that this value is not less than the specified [minimumValue].
 *
 * @return this value if it's greater than or equal to the [minimumValue] or the [minimumValue] otherwise.
 */
public fun Byte.coerceAtLeast(minimumValue: Byte): Byte {
    return if (this < minimumValue) minimumValue else this
}

/**
 * Ensures that this value is not greater than the specified [maximumValue].
 *
 * @return this value if it's less than or equal to the [maximumValue] or the [maximumValue] otherwise.
 */
public fun Byte.coerceAtMost(maximumValue: Byte): Byte {
    return if (this > maximumValue) maximumValue else this
}

/**
 * Ensures that this value is not less than the specified [minimumValue].
 *
 * @return this value if it's greater than or equal to the [minimumValue] or the [minimumValue] otherwise.
 */
public fun Short.coerceAtLeast(minimumValue: Short): Short {
    return if (this < minimumValue) minimumValue else this
}

/**
 * Ensures that this value is not greater than the specified [maximumValue].
 *
 * @return this value if it's less than or equal to the [maximumValue] or the [maximumValue] otherwise.
 */
public fun Short.coerceAtMost(maximumValue: Short): Short {
    return if (this > maximumValue) maximumValue else this
}

/**
 * Ensures that this value is not less than the specified [minimumValue].
 *
 * @return this value if it's greater than or equal to the [minimumValue] or the [minimumValue] otherwise.
 */
public fun Float.coerceAtLeast(minimumValue: Float): Float {
    return if (this < minimumValue) minimumValue else this
}

/**
 * Ensures that this value is not greater than the specified [maximumValue].
 *
 * @return this value if it's less than or equal to the [maximumValue] or the [maximumValue] otherwise.
 */
public fun Float.coerceAtMost(maximumValue: Float): Float {
    return if (this > maximumValue) maximumValue else this
}

/**
 * Ensures that this value is not less than the specified [minimumValue].
 *
 * @return this value if it's greater than or equal to the [minimumValue] or the [minimumValue] otherwise.
 */
public fun Double.coerceAtLeast(minimumValue: Double): Double {
    return if (this < minimumValue) minimumValue else this
}

/**
 * Ensures that this value is not greater than the specified [maximumValue].
 *
 * @return this value if it's less than or equal to the [maximumValue] or the [maximumValue] otherwise.
 */
public fun Double.coerceAtMost(maximumValue: Double): Double {
    return if (this > maximumValue) maximumValue else this
}

// ============================================
// ClosedFloatingPointRange Support
// ============================================

/**
 * Represents a range of floating point numbers.
 * Extends [ClosedRange] interface providing custom operation [lessThanOrEquals] for comparing values of range domain type.
 */
public interface ClosedFloatingPointRange<T : Comparable<T>> : ClosedRange<T> {
    /**
     * Compares two values of range domain type and returns true if first is less than or equal to second.
     */
    fun lessThanOrEquals(a: T, b: T): Boolean

    /**
     * Checks whether the specified [value] belongs to the range.
     */
    override operator fun contains(value: T): Boolean = lessThanOrEquals(start, value) && lessThanOrEquals(value, endInclusive)
}

/**
 * Ensures that this value lies in the specified [range].
 *
 * @return this value if it's in the range, or range.start if this value is less than range.start,
 * or range.endInclusive if this value is greater than range.endInclusive.
 */
internal fun Int.coerceIn(range: ClosedFloatingPointRange<Int>): Int {
    if (range.isEmpty()) throw IllegalArgumentException("Cannot coerce value to an empty range: $range.")
    return when {
        range.lessThanOrEquals(this, range.start) -> range.start
        range.lessThanOrEquals(range.endInclusive, this) -> range.endInclusive
        else -> this
    }
}

/**
 * Ensures that this value lies in the specified [range].
 *
 * @return this value if it's in the range, or range.start if this value is less than range.start,
 * or range.endInclusive if this value is greater than range.endInclusive.
 */
internal fun Long.coerceIn(range: ClosedFloatingPointRange<Long>): Long {
    if (range.isEmpty()) throw IllegalArgumentException("Cannot coerce value to an empty range: $range.")
    return when {
        range.lessThanOrEquals(this, range.start) -> range.start
        range.lessThanOrEquals(range.endInclusive, this) -> range.endInclusive
        else -> this
    }
}
