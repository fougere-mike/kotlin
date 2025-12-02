/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.ranges

import kotlin.internal.*

// Helper functions for progression calculation

private fun differenceModulo(a: UInt, b: UInt, c: UInt): UInt {
    val ac = a % c
    val bc = b % c
    return if (ac >= bc) ac - bc else ac - bc + c
}

private fun differenceModulo(a: ULong, b: ULong, c: ULong): ULong {
    val ac = a % c
    val bc = b % c
    return if (ac >= bc) ac - bc else ac - bc + c
}

@PublishedApi
internal fun getProgressionLastElement(start: UInt, end: UInt, step: Int): UInt = when {
    step > 0 -> if (start >= end) end else end - differenceModulo(end, start, step.toUInt())
    step < 0 -> if (start <= end) end else end + differenceModulo(start, end, (-step).toUInt())
    else -> throw IllegalArgumentException("Step is zero.")
}

@PublishedApi
internal fun getProgressionLastElement(start: ULong, end: ULong, step: Long): ULong = when {
    step > 0 -> if (start >= end) end else end - differenceModulo(end, start, step.toULong())
    step < 0 -> if (start <= end) end else end + differenceModulo(start, end, (-step).toULong())
    else -> throw IllegalArgumentException("Step is zero.")
}

// UIntRange

/**
 * A range of values of type `UInt`.
 */
@SinceKotlin("1.5")
public class UIntRange(start: UInt, endInclusive: UInt) : UIntProgression(start, endInclusive, 1), ClosedRange<UInt>, OpenEndRange<UInt> {
    override val start: UInt get() = first
    override val endInclusive: UInt get() = last

    @Deprecated("Can throw an exception when it's impossible to represent the value with UInt type, for example, when the range includes MAX_VALUE. It's recommended to use 'endInclusive' property that doesn't throw.")
    @SinceKotlin("1.9")
    override val endExclusive: UInt get() {
        if (last == UInt.MAX_VALUE) error("Cannot return the exclusive upper bound of a range that includes MAX_VALUE.")
        return last + 1u
    }

    override fun contains(value: UInt): Boolean = first <= value && value <= last

    override fun isEmpty(): Boolean = first > last

    override fun equals(other: Any?): Boolean =
        other is UIntRange && (isEmpty() && other.isEmpty() ||
                first == other.first && last == other.last)

    override fun hashCode(): Int =
        if (isEmpty()) -1 else (31 * first.toInt() + last.toInt())

    override fun toString(): String = "$first..$last"

    public companion object {
        /** An empty range of values of type UInt. */
        public val EMPTY: UIntRange = UIntRange(UInt.MAX_VALUE, UInt.MIN_VALUE)
    }
}

/**
 * A progression of values of type `UInt`.
 */
@SinceKotlin("1.5")
public open class UIntProgression
internal constructor(
    start: UInt,
    endInclusive: UInt,
    step: Int
) : Iterable<UInt> {
    init {
        if (step == 0) throw IllegalArgumentException("Step must be non-zero.")
        if (step == Int.MIN_VALUE) throw IllegalArgumentException("Step must be greater than Int.MIN_VALUE to avoid overflow on negation.")
    }

    public val first: UInt = start
    public val last: UInt = getProgressionLastElement(start, endInclusive, step)
    public val step: Int = step

    final override fun iterator(): Iterator<UInt> = UIntProgressionIterator(first, last, step)

    public open fun isEmpty(): Boolean = if (step > 0) first > last else first < last

    override fun equals(other: Any?): Boolean =
        other is UIntProgression && (isEmpty() && other.isEmpty() ||
                first == other.first && last == other.last && step == other.step)

    override fun hashCode(): Int =
        if (isEmpty()) -1 else (31 * (31 * first.toInt() + last.toInt()) + step)

    override fun toString(): String = if (step > 0) "$first..$last step $step" else "$first downTo $last step ${-step}"

    public companion object {
        public fun fromClosedRange(rangeStart: UInt, rangeEnd: UInt, step: Int): UIntProgression =
            UIntProgression(rangeStart, rangeEnd, step)
    }
}

private class UIntProgressionIterator(first: UInt, last: UInt, step: Int) : Iterator<UInt> {
    private val finalElement = last
    private var hasNext: Boolean = if (step > 0) first <= last else first >= last
    private val step = step.toUInt()
    private var next = if (hasNext) first else finalElement

    override fun hasNext(): Boolean = hasNext

    override fun next(): UInt {
        val value = next
        if (value == finalElement) {
            if (!hasNext) throw NoSuchElementException()
            hasNext = false
        } else {
            next += step
        }
        return value
    }
}

// ULongRange

/**
 * A range of values of type `ULong`.
 */
@SinceKotlin("1.5")
public class ULongRange(start: ULong, endInclusive: ULong) : ULongProgression(start, endInclusive, 1L), ClosedRange<ULong>, OpenEndRange<ULong> {
    override val start: ULong get() = first
    override val endInclusive: ULong get() = last

    @Deprecated("Can throw an exception when it's impossible to represent the value with ULong type, for example, when the range includes MAX_VALUE. It's recommended to use 'endInclusive' property that doesn't throw.")
    @SinceKotlin("1.9")
    override val endExclusive: ULong get() {
        if (last == ULong.MAX_VALUE) error("Cannot return the exclusive upper bound of a range that includes MAX_VALUE.")
        return last + 1u
    }

    override fun contains(value: ULong): Boolean = first <= value && value <= last

    override fun isEmpty(): Boolean = first > last

    override fun equals(other: Any?): Boolean =
        other is ULongRange && (isEmpty() && other.isEmpty() ||
                first == other.first && last == other.last)

    override fun hashCode(): Int =
        if (isEmpty()) -1 else (31 * (first xor (first shr 32)).toInt() + (last xor (last shr 32)).toInt())

    override fun toString(): String = "$first..$last"

    public companion object {
        /** An empty range of values of type ULong. */
        public val EMPTY: ULongRange = ULongRange(ULong.MAX_VALUE, ULong.MIN_VALUE)
    }
}

/**
 * A progression of values of type `ULong`.
 */
@SinceKotlin("1.5")
public open class ULongProgression
internal constructor(
    start: ULong,
    endInclusive: ULong,
    step: Long
) : Iterable<ULong> {
    init {
        if (step == 0L) throw IllegalArgumentException("Step must be non-zero.")
        if (step == Long.MIN_VALUE) throw IllegalArgumentException("Step must be greater than Long.MIN_VALUE to avoid overflow on negation.")
    }

    public val first: ULong = start
    public val last: ULong = getProgressionLastElement(start, endInclusive, step)
    public val step: Long = step

    final override fun iterator(): Iterator<ULong> = ULongProgressionIterator(first, last, step)

    public open fun isEmpty(): Boolean = if (step > 0) first > last else first < last

    override fun equals(other: Any?): Boolean =
        other is ULongProgression && (isEmpty() && other.isEmpty() ||
                first == other.first && last == other.last && step == other.step)

    override fun hashCode(): Int =
        if (isEmpty()) -1 else (31 * (31 * (first xor (first shr 32)).toInt() + (last xor (last shr 32)).toInt()) + (step xor (step ushr 32)).toInt())

    override fun toString(): String = if (step > 0) "$first..$last step $step" else "$first downTo $last step ${-step}"

    public companion object {
        public fun fromClosedRange(rangeStart: ULong, rangeEnd: ULong, step: Long): ULongProgression =
            ULongProgression(rangeStart, rangeEnd, step)
    }
}

private class ULongProgressionIterator(first: ULong, last: ULong, step: Long) : Iterator<ULong> {
    private val finalElement = last
    private var hasNext: Boolean = if (step > 0) first <= last else first >= last
    private val step = step.toULong()
    private var next = if (hasNext) first else finalElement

    override fun hasNext(): Boolean = hasNext

    override fun next(): ULong {
        val value = next
        if (value == finalElement) {
            if (!hasNext) throw NoSuchElementException()
            hasNext = false
        } else {
            next += step
        }
        return value
    }
}

// Utility functions for creating ranges and progressions

/**
 * Creates a range from this value up to but excluding the specified [to] value.
 */
@SinceKotlin("1.5")
public infix fun UInt.until(to: UInt): UIntRange {
    if (to <= UInt.MIN_VALUE) return UIntRange.EMPTY
    return this .. (to - 1u)
}

/**
 * Creates a range from this value up to but excluding the specified [to] value.
 */
@SinceKotlin("1.5")
public infix fun ULong.until(to: ULong): ULongRange {
    if (to <= ULong.MIN_VALUE) return ULongRange.EMPTY
    return this .. (to - 1u)
}

/**
 * Returns a progression from this value down to the specified [to] value with the step -1.
 */
@SinceKotlin("1.5")
public infix fun UInt.downTo(to: UInt): UIntProgression {
    return UIntProgression.fromClosedRange(this, to, -1)
}

/**
 * Returns a progression from this value down to the specified [to] value with the step -1.
 */
@SinceKotlin("1.5")
public infix fun ULong.downTo(to: ULong): ULongProgression {
    return ULongProgression.fromClosedRange(this, to, -1)
}

/**
 * Returns a progression that goes over the same range with the given [step].
 */
@SinceKotlin("1.5")
public infix fun UIntProgression.step(step: Int): UIntProgression {
    checkStepIsPositive(step > 0, step)
    return UIntProgression.fromClosedRange(first, last, if (this.step > 0) step else -step)
}

/**
 * Returns a progression that goes over the same range with the given [step].
 */
@SinceKotlin("1.5")
public infix fun ULongProgression.step(step: Long): ULongProgression {
    checkStepIsPositive(step > 0, step)
    return ULongProgression.fromClosedRange(first, last, if (this.step > 0) step else -step)
}

internal fun checkStepIsPositive(isPositive: Boolean, step: Number) {
    if (!isPositive) throw IllegalArgumentException("Step must be positive, was: $step.")
}
