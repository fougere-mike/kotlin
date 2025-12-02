/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin

/**
 * Represents a 32-bit unsigned integer.
 * On BrightScript, a UInt is represented as a regular class with an Int backing field.
 */
@SinceKotlin("1.5")
public class UInt public constructor(public val data: Int) : Comparable<UInt> {

    companion object {
        /**
         * A constant holding the minimum value an instance of UInt can have.
         */
        public val MIN_VALUE: UInt = UInt(0)

        /**
         * A constant holding the maximum value an instance of UInt can have.
         */
        public val MAX_VALUE: UInt = UInt(-1)

        /**
         * The number of bytes used to represent an instance of UInt in a binary form.
         */
        public const val SIZE_BYTES: Int = 4

        /**
         * The number of bits used to represent an instance of UInt in a binary form.
         */
        public const val SIZE_BITS: Int = 32
    }

    /**
     * Compares this value with the specified value for order.
     * Returns zero if this value is equal to the specified other value, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    public inline operator fun compareTo(other: UByte): Int = this.compareTo(other.toUInt())

    /**
     * Compares this value with the specified value for order.
     * Returns zero if this value is equal to the specified other value, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    public inline operator fun compareTo(other: UShort): Int = this.compareTo(other.toUInt())

    /**
     * Compares this value with the specified value for order.
     * Returns zero if this value is equal to the specified other value, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    public override operator fun compareTo(other: UInt): Int = uintCompare(this.data, other.data)

    /**
     * Compares this value with the specified value for order.
     * Returns zero if this value is equal to the specified other value, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    public inline operator fun compareTo(other: ULong): Int = this.toULong().compareTo(other)

    /** Adds the other value to this value. */
    public inline operator fun plus(other: UByte): UInt = this.plus(other.toUInt())
    /** Adds the other value to this value. */
    public inline operator fun plus(other: UShort): UInt = this.plus(other.toUInt())
    /** Adds the other value to this value. */
    public inline operator fun plus(other: UInt): UInt = UInt(this.data.plus(other.data))
    /** Adds the other value to this value. */
    public inline operator fun plus(other: ULong): ULong = this.toULong().plus(other)

    /** Subtracts the other value from this value. */
    public inline operator fun minus(other: UByte): UInt = this.minus(other.toUInt())
    /** Subtracts the other value from this value. */
    public inline operator fun minus(other: UShort): UInt = this.minus(other.toUInt())
    /** Subtracts the other value from this value. */
    public inline operator fun minus(other: UInt): UInt = UInt(this.data.minus(other.data))
    /** Subtracts the other value from this value. */
    public inline operator fun minus(other: ULong): ULong = this.toULong().minus(other)

    /** Multiplies this value by the other value. */
    public inline operator fun times(other: UByte): UInt = this.times(other.toUInt())
    /** Multiplies this value by the other value. */
    public inline operator fun times(other: UShort): UInt = this.times(other.toUInt())
    /** Multiplies this value by the other value. */
    public inline operator fun times(other: UInt): UInt = UInt(this.data.times(other.data))
    /** Multiplies this value by the other value. */
    public inline operator fun times(other: ULong): ULong = this.toULong().times(other)

    /** Divides this value by the other value, truncating the result to an integer that is closer to zero. */
    public inline operator fun div(other: UByte): UInt = this.div(other.toUInt())
    /** Divides this value by the other value, truncating the result to an integer that is closer to zero. */
    public inline operator fun div(other: UShort): UInt = this.div(other.toUInt())
    /** Divides this value by the other value, truncating the result to an integer that is closer to zero. */
    public operator fun div(other: UInt): UInt = uintDivide(this.data, other.data)
    /** Divides this value by the other value, truncating the result to an integer that is closer to zero. */
    public inline operator fun div(other: ULong): ULong = this.toULong().div(other)

    /**
     * Calculates the remainder of truncating division of this value (dividend) by the other value (divisor).
     *
     * The result is always less than the divisor.
     */
    public inline operator fun rem(other: UByte): UInt = this.rem(other.toUInt())
    /**
     * Calculates the remainder of truncating division of this value (dividend) by the other value (divisor).
     *
     * The result is always less than the divisor.
     */
    public inline operator fun rem(other: UShort): UInt = this.rem(other.toUInt())
    /**
     * Calculates the remainder of truncating division of this value (dividend) by the other value (divisor).
     *
     * The result is always less than the divisor.
     */
    public operator fun rem(other: UInt): UInt = uintRemainder(this.data, other.data)
    /**
     * Calculates the remainder of truncating division of this value (dividend) by the other value (divisor).
     *
     * The result is always less than the divisor.
     */
    public inline operator fun rem(other: ULong): ULong = this.toULong().rem(other)

    /**
     * Divides this value by the other value, flooring the result to an integer that is closer to negative infinity.
     *
     * For unsigned types, the results of flooring division and truncating division are the same.
     */
    public inline fun floorDiv(other: UByte): UInt = this.floorDiv(other.toUInt())
    /**
     * Divides this value by the other value, flooring the result to an integer that is closer to negative infinity.
     *
     * For unsigned types, the results of flooring division and truncating division are the same.
     */
    public inline fun floorDiv(other: UShort): UInt = this.floorDiv(other.toUInt())
    /**
     * Divides this value by the other value, flooring the result to an integer that is closer to negative infinity.
     *
     * For unsigned types, the results of flooring division and truncating division are the same.
     */
    public inline fun floorDiv(other: UInt): UInt = div(other)
    /**
     * Divides this value by the other value, flooring the result to an integer that is closer to negative infinity.
     *
     * For unsigned types, the results of flooring division and truncating division are the same.
     */
    public inline fun floorDiv(other: ULong): ULong = this.toULong().floorDiv(other)

    /**
     * Calculates the remainder of flooring division of this value (dividend) by the other value (divisor).
     *
     * The result is always less than the divisor.
     *
     * For unsigned types, the remainders of flooring division and truncating division are the same.
     */
    public inline fun mod(other: UByte): UByte = this.mod(other.toUInt()).toUByte()
    /**
     * Calculates the remainder of flooring division of this value (dividend) by the other value (divisor).
     *
     * The result is always less than the divisor.
     *
     * For unsigned types, the remainders of flooring division and truncating division are the same.
     */
    public inline fun mod(other: UShort): UShort = this.mod(other.toUInt()).toUShort()
    /**
     * Calculates the remainder of flooring division of this value (dividend) by the other value (divisor).
     *
     * The result is always less than the divisor.
     *
     * For unsigned types, the remainders of flooring division and truncating division are the same.
     */
    public inline fun mod(other: UInt): UInt = rem(other)
    /**
     * Calculates the remainder of flooring division of this value (dividend) by the other value (divisor).
     *
     * The result is always less than the divisor.
     *
     * For unsigned types, the remainders of flooring division and truncating division are the same.
     */
    public inline fun mod(other: ULong): ULong = this.toULong().mod(other)

    /**
     * Returns this value incremented by one.
     */
    public inline operator fun inc(): UInt = UInt(data.inc())

    /**
     * Returns this value decremented by one.
     */
    public inline operator fun dec(): UInt = UInt(data.dec())

    /** Creates a range from this value to the specified [other] value. */
    public operator fun rangeTo(other: UInt): UIntRange = UIntRange(this, other)

    /**
     * Creates a range from this value up to but excluding the specified [other] value.
     *
     * If the [other] value is less than or equal to `this` value, then the returned range is empty.
     */
    @SinceKotlin("1.9")
    public operator fun rangeUntil(other: UInt): UIntRange = this until other

    /**
     * Shifts this value left by the [bitCount] number of bits.
     *
     * Note that only the five lowest-order bits of the [bitCount] are used as the shift distance.
     * The shift distance actually used is therefore always in the range `0..31`.
     */
    public inline infix fun shl(bitCount: Int): UInt = UInt(data shl bitCount)

    /**
     * Shifts this value right by the [bitCount] number of bits, filling the leftmost bits with zeros.
     *
     * Note that only the five lowest-order bits of the [bitCount] are used as the shift distance.
     * The shift distance actually used is therefore always in the range `0..31`.
     */
    public inline infix fun shr(bitCount: Int): UInt = UInt(data ushr bitCount)

    /** Performs a bitwise AND operation between the two values. */
    public inline infix fun and(other: UInt): UInt = UInt(this.data and other.data)
    /** Performs a bitwise OR operation between the two values. */
    public inline infix fun or(other: UInt): UInt = UInt(this.data or other.data)
    /** Performs a bitwise XOR operation between the two values. */
    public inline infix fun xor(other: UInt): UInt = UInt(this.data xor other.data)
    /** Inverts the bits in this value. */
    public inline fun inv(): UInt = UInt(data.inv())

    /**
     * Converts this [UInt] value to [Byte].
     *
     * If this value is less than or equals to [Byte.MAX_VALUE], the resulting `Byte` value represents
     * the same numerical value as this `UInt`.
     *
     * The resulting `Byte` value is represented by the least significant 8 bits of this `UInt` value.
     * Note that the resulting `Byte` value may be negative.
     */
    public inline fun toByte(): Byte = data.toByte()
    /**
     * Converts this [UInt] value to [Short].
     *
     * If this value is less than or equals to [Short.MAX_VALUE], the resulting `Short` value represents
     * the same numerical value as this `UInt`.
     *
     * The resulting `Short` value is represented by the least significant 16 bits of this `UInt` value.
     * Note that the resulting `Short` value may be negative.
     */
    public inline fun toShort(): Short = data.toShort()
    /**
     * Converts this [UInt] value to [Int].
     *
     * If this value is less than or equals to [Int.MAX_VALUE], the resulting `Int` value represents
     * the same numerical value as this `UInt`. Otherwise the result is negative.
     *
     * The resulting `Int` value has the same binary representation as this `UInt` value.
     */
    public inline fun toInt(): Int = data
    /**
     * Converts this [UInt] value to [Long].
     *
     * The resulting `Long` value represents the same numerical value as this `UInt`.
     *
     * The least significant 32 bits of the resulting `Long` value are the same as the bits of this `UInt` value,
     * whereas the most significant 32 bits are filled with zeros.
     */
    public inline fun toLong(): Long = uintToLong(data)

    /**
     * Converts this [UInt] value to [UByte].
     *
     * If this value is less than or equals to [UByte.MAX_VALUE], the resulting `UByte` value represents
     * the same numerical value as this `UInt`.
     *
     * The resulting `UByte` value is represented by the least significant 8 bits of this `UInt` value.
     */
    public inline fun toUByte(): UByte = UByte(data.toByte())
    /**
     * Converts this [UInt] value to [UShort].
     *
     * If this value is less than or equals to [UShort.MAX_VALUE], the resulting `UShort` value represents
     * the same numerical value as this `UInt`.
     *
     * The resulting `UShort` value is represented by the least significant 16 bits of this `UInt` value.
     */
    public inline fun toUShort(): UShort = UShort(data.toShort())
    /** Returns this value. */
    public inline fun toUInt(): UInt = this
    /**
     * Converts this [UInt] value to [ULong].
     *
     * The resulting `ULong` value represents the same numerical value as this `UInt`.
     *
     * The least significant 32 bits of the resulting `ULong` value are the same as the bits of this `UInt` value,
     * whereas the most significant 32 bits are filled with zeros.
     */
    public inline fun toULong(): ULong = uintToULong(data)

    /**
     * Converts this [UInt] value to [Float].
     *
     * The resulting value is the closest `Float` to this `UInt` value.
     * In case when this `UInt` value is exactly between two `Float`s,
     * the one with zero at least significant bit of mantissa is selected.
     */
    public inline fun toFloat(): Float = uintToFloat(data)
    /**
     * Converts this [UInt] value to [Double].
     *
     * The resulting `Double` value represents the same numerical value as this `UInt`.
     */
    public inline fun toDouble(): Double = uintToDouble(data)

    public override fun toString(): String = uintToString(data)

    public override fun equals(other: Any?): Boolean =
        other is UInt && this.data == other.data

    public override fun hashCode(): Int = data
}

/**
 * Converts this [Byte] value to [UInt].
 *
 * If this value is positive, the resulting `UInt` value represents the same numerical value as this `Byte`.
 *
 * The least significant 8 bits of the resulting `UInt` value are the same as the bits of this `Byte` value,
 * whereas the most significant 24 bits are filled with the sign bit of this value.
 */
@SinceKotlin("1.5")
public fun Byte.toUInt(): UInt = UInt(this.toInt())
/**
 * Converts this [Short] value to [UInt].
 *
 * If this value is positive, the resulting `UInt` value represents the same numerical value as this `Short`.
 *
 * The least significant 16 bits of the resulting `UInt` value are the same as the bits of this `Short` value,
 * whereas the most significant 16 bits are filled with the sign bit of this value.
 */
@SinceKotlin("1.5")
public fun Short.toUInt(): UInt = UInt(this.toInt())
/**
 * Converts this [Int] value to [UInt].
 *
 * If this value is positive, the resulting `UInt` value represents the same numerical value as this `Int`.
 * Otherwise the result has the same binary representation as this `Int`.
 */
@SinceKotlin("1.5")
public fun Int.toUInt(): UInt = UInt(this)
/**
 * Converts this [Long] value to [UInt].
 *
 * If this value is positive and less than or equals to [UInt.MAX_VALUE], the resulting `UInt` value represents
 * the same numerical value as this `Long`.
 *
 * The resulting `UInt` value is represented by the least significant 32 bits of this `Long` value.
 */
@SinceKotlin("1.5")
public fun Long.toUInt(): UInt = UInt(this.toInt())

/**
 * Converts this [Float] value to [UInt].
 *
 * The fractional part, if any, is rounded down towards zero.
 * Returns zero if this `Float` value is negative or `NaN`, [UInt.MAX_VALUE] if it's bigger than `UInt.MAX_VALUE`.
 */
@SinceKotlin("1.5")
public fun Float.toUInt(): UInt = floatToUInt(this)

/**
 * Converts this [Double] value to [UInt].
 *
 * The fractional part, if any, is rounded down towards zero.
 * Returns zero if this `Double` value is negative or `NaN`, [UInt.MAX_VALUE] if it's bigger than `UInt.MAX_VALUE`.
 */
@SinceKotlin("1.5")
public fun Double.toUInt(): UInt = doubleToUInt(this)
