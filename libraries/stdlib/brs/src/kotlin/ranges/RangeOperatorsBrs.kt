/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.ranges

// ============================================
// Int Range Operators
// ============================================

/**
 * Creates a range from this value to the specified [other] value.
 *
 * This value needs to be smaller than or equal to [other] value, otherwise the returned range will be empty.
 */
public operator fun Int.rangeTo(other: Int): IntRange = IntRange(this, other)

/**
 * Creates a range from this value to the specified [other] value.
 */
public operator fun Int.rangeTo(other: Long): LongRange = LongRange(this.toLong(), other)

/**
 * Creates a range from this value down to the specified [to] value with the step equal to -1.
 *
 * The [to] value must be less than or equal to `this` value.
 */
public infix fun Int.downTo(to: Int): IntProgression {
    return IntProgression.fromClosedRange(this, to, -1)
}

/**
 * Creates a range from this value down to the specified [to] value with the step equal to -1.
 */
public infix fun Int.downTo(to: Long): LongProgression {
    return LongProgression.fromClosedRange(this.toLong(), to, -1L)
}

/**
 * Returns a progression that goes over the same range with the given [step].
 */
public infix fun IntProgression.step(step: Int): IntProgression {
    checkStepIsPositive(step > 0, step)
    return IntProgression.fromClosedRange(first, last, if (this.step > 0) step else -step)
}

// ============================================
// Long Range Operators
// ============================================

/**
 * Creates a range from this value to the specified [other] value.
 */
public operator fun Long.rangeTo(other: Long): LongRange = LongRange(this, other)

/**
 * Creates a range from this value down to the specified [to] value with the step equal to -1.
 */
public infix fun Long.downTo(to: Int): LongProgression {
    return LongProgression.fromClosedRange(this, to.toLong(), -1L)
}

/**
 * Creates a range from this value down to the specified [to] value with the step equal to -1.
 */
public infix fun Long.downTo(to: Long): LongProgression {
    return LongProgression.fromClosedRange(this, to, -1L)
}

/**
 * Returns a progression that goes over the same range with the given [step].
 */
public infix fun LongProgression.step(step: Long): LongProgression {
    checkStepIsPositive(step > 0, step)
    return LongProgression.fromClosedRange(first, last, if (this.step > 0) step else -step)
}

// ============================================
// Char Range Operators
// ============================================

/**
 * Creates a range from this value to the specified [other] value.
 *
 * This value needs to be smaller than or equal to [other] value, otherwise the returned range will be empty.
 */
public operator fun Char.rangeTo(other: Char): CharRange = CharRange(this, other)

/**
 * Creates a range from this value down to the specified [to] value with the step equal to -1.
 *
 * The [to] value must be less than or equal to `this` value.
 */
public infix fun Char.downTo(to: Char): CharProgression {
    return CharProgression.fromClosedRange(this, to, -1)
}

/**
 * Returns a progression that goes over the same range with the given [step].
 */
public infix fun CharProgression.step(step: Int): CharProgression {
    checkStepIsPositive(step > 0, step)
    return CharProgression.fromClosedRange(first, last, if (this.step > 0) step else -step)
}
