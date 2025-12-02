/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.ranges

/**
 * Represents a range of values (for example, numbers or characters).
 */
public interface ClosedRange<T : Comparable<T>> {
    /**
     * The minimum value in the range.
     */
    public val start: T

    /**
     * The maximum value in the range (inclusive).
     */
    public val endInclusive: T

    /**
     * Checks whether the specified [value] belongs to the range.
     */
    public operator fun contains(value: T): Boolean = value >= start && value <= endInclusive

    /**
     * Checks whether the range is empty.
     */
    public fun isEmpty(): Boolean = start > endInclusive
}

/**
 * Represents a range of values (for example, numbers or characters) where the upper bound is not included.
 */
@SinceKotlin("1.9")
public interface OpenEndRange<T : Comparable<T>> {
    /**
     * The minimum value in the range.
     */
    public val start: T

    /**
     * The maximum value in the range (exclusive).
     */
    public val endExclusive: T

    /**
     * Checks whether the specified [value] belongs to the range.
     */
    public operator fun contains(value: T): Boolean = value >= start && value < endExclusive

    /**
     * Checks whether the range is empty.
     */
    public fun isEmpty(): Boolean = start >= endExclusive
}

/**
 * A range of values of type `Int`.
 */
public class IntRange(
    start: Int,
    endInclusive: Int
) : IntProgression(start, endInclusive, 1), ClosedRange<Int> {
    override val start: Int get() = first
    override val endInclusive: Int get() = last

    override fun contains(value: Int): Boolean = first <= value && value <= last

    override fun isEmpty(): Boolean = first > last

    override fun equals(other: Any?): Boolean =
        other is IntRange && (isEmpty() && other.isEmpty() || first == other.first && last == other.last)

    override fun hashCode(): Int =
        if (isEmpty()) -1 else (31 * first + last)

    override fun toString(): String = "$first..$last"

    companion object {
        /** An empty range of values of type Int. */
        public val EMPTY: IntRange = IntRange(1, 0)
    }
}

/**
 * A range of values of type `Long`.
 */
public class LongRange(
    start: Long,
    endInclusive: Long
) : LongProgression(start, endInclusive, 1), ClosedRange<Long> {
    override val start: Long get() = first
    override val endInclusive: Long get() = last

    override fun contains(value: Long): Boolean = first <= value && value <= last

    override fun isEmpty(): Boolean = first > last

    override fun equals(other: Any?): Boolean =
        other is LongRange && (isEmpty() && other.isEmpty() || first == other.first && last == other.last)

    override fun hashCode(): Int =
        if (isEmpty()) -1 else (31 * (first xor (first ushr 32)) + (last xor (last ushr 32))).toInt()

    override fun toString(): String = "$first..$last"

    companion object {
        /** An empty range of values of type Long. */
        public val EMPTY: LongRange = LongRange(1, 0)
    }
}

/**
 * A range of values of type `Char`.
 */
public class CharRange(
    start: Char,
    endInclusive: Char
) : CharProgression(start, endInclusive, 1), ClosedRange<Char> {
    override val start: Char get() = first
    override val endInclusive: Char get() = last

    override fun contains(value: Char): Boolean = first <= value && value <= last

    override fun isEmpty(): Boolean = first > last

    override fun equals(other: Any?): Boolean =
        other is CharRange && (isEmpty() && other.isEmpty() || first == other.first && last == other.last)

    override fun hashCode(): Int =
        if (isEmpty()) -1 else (31 * first.code + last.code)

    override fun toString(): String = "$first..$last"

    companion object {
        /** An empty range of values of type Char. */
        public val EMPTY: CharRange = CharRange(1.toChar(), 0.toChar())
    }
}

/**
 * A progression of values of type `Int`.
 */
public open class IntProgression
internal constructor(
    start: Int,
    endInclusive: Int,
    step: Int
) : Iterable<Int> {
    init {
        if (step == 0) throw IllegalArgumentException("Step must be non-zero.")
        if (step == Int.MIN_VALUE) throw IllegalArgumentException("Step must be greater than Int.MIN_VALUE to avoid overflow on negation.")
    }

    /**
     * The first element in the progression.
     */
    public val first: Int = start

    /**
     * The last element in the progression.
     */
    public val last: Int = getProgressionLastElement(start, endInclusive, step)

    /**
     * The step of the progression.
     */
    public val step: Int = step

    override fun iterator(): IntIterator = IntProgressionIterator(first, last, step)

    /** Checks if the progression is empty. */
    public open fun isEmpty(): Boolean = if (step > 0) first > last else first < last

    override fun equals(other: Any?): Boolean =
        other is IntProgression && (isEmpty() && other.isEmpty() ||
                first == other.first && last == other.last && step == other.step)

    override fun hashCode(): Int =
        if (isEmpty()) -1 else (31 * (31 * first + last) + step)

    override fun toString(): String = if (step > 0) "$first..$last step $step" else "$first downTo $last step ${-step}"

    companion object {
        /**
         * Creates IntProgression within the specified bounds of a closed range.
         */
        public fun fromClosedRange(rangeStart: Int, rangeEnd: Int, step: Int): IntProgression =
            IntProgression(rangeStart, rangeEnd, step)
    }
}

/**
 * A progression of values of type `Long`.
 */
public open class LongProgression
internal constructor(
    start: Long,
    endInclusive: Long,
    step: Long
) : Iterable<Long> {
    init {
        if (step == 0L) throw IllegalArgumentException("Step must be non-zero.")
        if (step == Long.MIN_VALUE) throw IllegalArgumentException("Step must be greater than Long.MIN_VALUE to avoid overflow on negation.")
    }

    public val first: Long = start
    public val last: Long = getProgressionLastElement(start, endInclusive, step)
    public val step: Long = step

    override fun iterator(): LongIterator = LongProgressionIterator(first, last, step)

    public open fun isEmpty(): Boolean = if (step > 0) first > last else first < last

    override fun equals(other: Any?): Boolean =
        other is LongProgression && (isEmpty() && other.isEmpty() ||
                first == other.first && last == other.last && step == other.step)

    override fun hashCode(): Int =
        if (isEmpty()) -1 else (31 * (31 * (first xor (first ushr 32)) + (last xor (last ushr 32))) + (step xor (step ushr 32))).toInt()

    override fun toString(): String = if (step > 0) "$first..$last step $step" else "$first downTo $last step ${-step}"

    companion object {
        public fun fromClosedRange(rangeStart: Long, rangeEnd: Long, step: Long): LongProgression =
            LongProgression(rangeStart, rangeEnd, step)
    }
}

/**
 * A progression of values of type `Char`.
 */
public open class CharProgression
internal constructor(
    start: Char,
    endInclusive: Char,
    step: Int
) : Iterable<Char> {
    init {
        if (step == 0) throw IllegalArgumentException("Step must be non-zero.")
        if (step == Int.MIN_VALUE) throw IllegalArgumentException("Step must be greater than Int.MIN_VALUE to avoid overflow on negation.")
    }

    public val first: Char = start
    public val last: Char = getProgressionLastElement(start.code, endInclusive.code, step).toChar()
    public val step: Int = step

    override fun iterator(): CharIterator = CharProgressionIterator(first, last, step)

    public open fun isEmpty(): Boolean = if (step > 0) first > last else first < last

    override fun equals(other: Any?): Boolean =
        other is CharProgression && (isEmpty() && other.isEmpty() ||
                first == other.first && last == other.last && step == other.step)

    override fun hashCode(): Int =
        if (isEmpty()) -1 else (31 * (31 * first.code + last.code) + step)

    override fun toString(): String = if (step > 0) "$first..$last step $step" else "$first downTo $last step ${-step}"

    companion object {
        public fun fromClosedRange(rangeStart: Char, rangeEnd: Char, step: Int): CharProgression =
            CharProgression(rangeStart, rangeEnd, step)
    }
}

// ============================================
// Progression Iterators
// ============================================

internal class IntProgressionIterator(first: Int, last: Int, val step: Int) : IntIterator() {
    private val finalElement: Int = last
    private var hasNext: Boolean = if (step > 0) first <= last else first >= last
    private var next: Int = if (hasNext) first else finalElement

    override fun hasNext(): Boolean = hasNext

    override fun nextInt(): Int {
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

internal class LongProgressionIterator(first: Long, last: Long, val step: Long) : LongIterator() {
    private val finalElement: Long = last
    private var hasNext: Boolean = if (step > 0) first <= last else first >= last
    private var next: Long = if (hasNext) first else finalElement

    override fun hasNext(): Boolean = hasNext

    override fun nextLong(): Long {
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

internal class CharProgressionIterator(first: Char, last: Char, val step: Int) : CharIterator() {
    private val finalElement: Int = last.code
    private var hasNext: Boolean = if (step > 0) first <= last else first >= last
    private var next: Int = if (hasNext) first.code else finalElement

    override fun hasNext(): Boolean = hasNext

    override fun nextChar(): Char {
        val value = next
        if (value == finalElement) {
            if (!hasNext) throw NoSuchElementException()
            hasNext = false
        } else {
            next += step
        }
        return value.toChar()
    }
}

// ============================================
// Helper Functions
// ============================================

/**
 * Calculates the final element of a bounded arithmetic progression.
 */
internal fun getProgressionLastElement(start: Int, end: Int, step: Int): Int = when {
    step > 0 -> if (start >= end) end else end - differenceModulo(end, start, step)
    step < 0 -> if (start <= end) end else end + differenceModulo(start, end, -step)
    else -> throw IllegalArgumentException("Step is zero.")
}

internal fun getProgressionLastElement(start: Long, end: Long, step: Long): Long = when {
    step > 0 -> if (start >= end) end else end - differenceModulo(end, start, step)
    step < 0 -> if (start <= end) end else end + differenceModulo(start, end, -step)
    else -> throw IllegalArgumentException("Step is zero.")
}

private fun differenceModulo(a: Int, b: Int, c: Int): Int {
    return mod(mod(a, c) - mod(b, c), c)
}

private fun differenceModulo(a: Long, b: Long, c: Long): Long {
    return mod(mod(a, c) - mod(b, c), c)
}

private fun mod(a: Int, b: Int): Int {
    val mod = a % b
    return if (mod >= 0) mod else mod + b
}

private fun mod(a: Long, b: Long): Long {
    val mod = a % b
    return if (mod >= 0) mod else mod + b
}
