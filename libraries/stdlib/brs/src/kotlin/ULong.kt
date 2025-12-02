/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin

/**
 * Represents a 64-bit unsigned integer.
 * On BrightScript, a ULong is represented as a regular class with a Long backing field.
 */
@SinceKotlin("1.5")
public class ULong @PublishedApi internal constructor(@PublishedApi internal val data: Long) : Comparable<ULong> {

    companion object {
        /**
         * A constant holding the minimum value an instance of ULong can have.
         */
        public val MIN_VALUE: ULong = ULong(0)

        /**
         * A constant holding the maximum value an instance of ULong can have.
         */
        public val MAX_VALUE: ULong = ULong(-1)

        /**
         * The number of bytes used to represent an instance of ULong in a binary form.
         */
        public const val SIZE_BYTES: Int = 8

        /**
         * The number of bits used to represent an instance of ULong in a binary form.
         */
        public const val SIZE_BITS: Int = 64
    }

    /**
     * Compares this value with the specified value for order.
     */
    public inline operator fun compareTo(other: UByte): Int = this.compareTo(other.toULong())

    /**
     * Compares this value with the specified value for order.
     */
    public inline operator fun compareTo(other: UShort): Int = this.compareTo(other.toULong())

    /**
     * Compares this value with the specified value for order.
     */
    public inline operator fun compareTo(other: UInt): Int = this.compareTo(other.toULong())

    /**
     * Compares this value with the specified value for order.
     */
    public override operator fun compareTo(other: ULong): Int = ulongCompare(this.data, other.data)

    /** Adds the other value to this value. */
    public inline operator fun plus(other: UByte): ULong = this.plus(other.toULong())
    /** Adds the other value to this value. */
    public inline operator fun plus(other: UShort): ULong = this.plus(other.toULong())
    /** Adds the other value to this value. */
    public inline operator fun plus(other: UInt): ULong = this.plus(other.toULong())
    /** Adds the other value to this value. */
    public inline operator fun plus(other: ULong): ULong = ULong(this.data.plus(other.data))

    /** Subtracts the other value from this value. */
    public inline operator fun minus(other: UByte): ULong = this.minus(other.toULong())
    /** Subtracts the other value from this value. */
    public inline operator fun minus(other: UShort): ULong = this.minus(other.toULong())
    /** Subtracts the other value from this value. */
    public inline operator fun minus(other: UInt): ULong = this.minus(other.toULong())
    /** Subtracts the other value from this value. */
    public inline operator fun minus(other: ULong): ULong = ULong(this.data.minus(other.data))

    /** Multiplies this value by the other value. */
    public inline operator fun times(other: UByte): ULong = this.times(other.toULong())
    /** Multiplies this value by the other value. */
    public inline operator fun times(other: UShort): ULong = this.times(other.toULong())
    /** Multiplies this value by the other value. */
    public inline operator fun times(other: UInt): ULong = this.times(other.toULong())
    /** Multiplies this value by the other value. */
    public inline operator fun times(other: ULong): ULong = ULong(this.data.times(other.data))

    /** Divides this value by the other value, truncating the result to an integer that is closer to zero. */
    public inline operator fun div(other: UByte): ULong = this.div(other.toULong())
    /** Divides this value by the other value, truncating the result to an integer that is closer to zero. */
    public inline operator fun div(other: UShort): ULong = this.div(other.toULong())
    /** Divides this value by the other value, truncating the result to an integer that is closer to zero. */
    public inline operator fun div(other: UInt): ULong = this.div(other.toULong())
    /** Divides this value by the other value, truncating the result to an integer that is closer to zero. */
    public operator fun div(other: ULong): ULong = ulongDivide(this.data, other.data)

    /**
     * Calculates the remainder of truncating division of this value by the other value.
     */
    public inline operator fun rem(other: UByte): ULong = this.rem(other.toULong())
    /**
     * Calculates the remainder of truncating division of this value by the other value.
     */
    public inline operator fun rem(other: UShort): ULong = this.rem(other.toULong())
    /**
     * Calculates the remainder of truncating division of this value by the other value.
     */
    public inline operator fun rem(other: UInt): ULong = this.rem(other.toULong())
    /**
     * Calculates the remainder of truncating division of this value by the other value.
     */
    public operator fun rem(other: ULong): ULong = ulongRemainder(this.data, other.data)

    /**
     * Divides this value by the other value, flooring the result.
     * For unsigned types, flooring and truncating division are the same.
     */
    public inline fun floorDiv(other: UByte): ULong = this.floorDiv(other.toULong())
    /**
     * Divides this value by the other value, flooring the result.
     */
    public inline fun floorDiv(other: UShort): ULong = this.floorDiv(other.toULong())
    /**
     * Divides this value by the other value, flooring the result.
     */
    public inline fun floorDiv(other: UInt): ULong = this.floorDiv(other.toULong())
    /**
     * Divides this value by the other value, flooring the result.
     */
    public inline fun floorDiv(other: ULong): ULong = div(other)

    /**
     * Calculates the remainder of flooring division.
     * For unsigned types, flooring and truncating remainders are the same.
     */
    public inline fun mod(other: UByte): UByte = this.mod(other.toULong()).toUByte()
    /**
     * Calculates the remainder of flooring division.
     */
    public inline fun mod(other: UShort): UShort = this.mod(other.toULong()).toUShort()
    /**
     * Calculates the remainder of flooring division.
     */
    public inline fun mod(other: UInt): UInt = this.mod(other.toULong()).toUInt()
    /**
     * Calculates the remainder of flooring division.
     */
    public inline fun mod(other: ULong): ULong = rem(other)

    /** Returns this value incremented by one. */
    public inline operator fun inc(): ULong = ULong(data.inc())

    /** Returns this value decremented by one. */
    public inline operator fun dec(): ULong = ULong(data.dec())

    /** Creates a range from this value to the specified [other] value. */
    public operator fun rangeTo(other: ULong): ULongRange = ULongRange(this, other)

    /**
     * Creates a range from this value up to but excluding the specified [other] value.
     */
    @SinceKotlin("1.9")
    public operator fun rangeUntil(other: ULong): ULongRange = this until other

    /**
     * Shifts this value left by the [bitCount] number of bits.
     */
    public inline infix fun shl(bitCount: Int): ULong = ULong(data shl bitCount)

    /**
     * Shifts this value right by the [bitCount] number of bits, filling the leftmost bits with zeros.
     */
    public inline infix fun shr(bitCount: Int): ULong = ULong(data ushr bitCount)

    /** Performs a bitwise AND operation between the two values. */
    public inline infix fun and(other: ULong): ULong = ULong(this.data and other.data)
    /** Performs a bitwise OR operation between the two values. */
    public inline infix fun or(other: ULong): ULong = ULong(this.data or other.data)
    /** Performs a bitwise XOR operation between the two values. */
    public inline infix fun xor(other: ULong): ULong = ULong(this.data xor other.data)
    /** Inverts the bits in this value. */
    public inline fun inv(): ULong = ULong(data.inv())

    /** Converts this [ULong] value to [Byte]. */
    public inline fun toByte(): Byte = data.toByte()
    /** Converts this [ULong] value to [Short]. */
    public inline fun toShort(): Short = data.toShort()
    /** Converts this [ULong] value to [Int]. */
    public inline fun toInt(): Int = data.toInt()
    /** Converts this [ULong] value to [Long]. */
    public inline fun toLong(): Long = data

    /** Converts this [ULong] value to [UByte]. */
    public inline fun toUByte(): UByte = UByte(data.toByte())
    /** Converts this [ULong] value to [UShort]. */
    public inline fun toUShort(): UShort = UShort(data.toShort())
    /** Converts this [ULong] value to [UInt]. */
    public inline fun toUInt(): UInt = UInt(data.toInt())
    /** Returns this value. */
    public inline fun toULong(): ULong = this

    /** Converts this [ULong] value to [Float]. */
    public inline fun toFloat(): Float = ulongToFloat(data)
    /** Converts this [ULong] value to [Double]. */
    public inline fun toDouble(): Double = ulongToDouble(data)

    public override fun toString(): String = ulongToString(data)

    public override fun equals(other: Any?): Boolean =
        other is ULong && this.data == other.data

    public override fun hashCode(): Int = data.hashCode()
}

/**
 * Converts this [Byte] value to [ULong].
 */
@SinceKotlin("1.5")
public fun Byte.toULong(): ULong = ULong(this.toLong())
/**
 * Converts this [Short] value to [ULong].
 */
@SinceKotlin("1.5")
public fun Short.toULong(): ULong = ULong(this.toLong())
/**
 * Converts this [Int] value to [ULong].
 */
@SinceKotlin("1.5")
public fun Int.toULong(): ULong = ULong(this.toLong())
/**
 * Converts this [Long] value to [ULong].
 */
@SinceKotlin("1.5")
public fun Long.toULong(): ULong = ULong(this)

/**
 * Converts this [Float] value to [ULong].
 */
@SinceKotlin("1.5")
public fun Float.toULong(): ULong = floatToULong(this)

/**
 * Converts this [Double] value to [ULong].
 */
@SinceKotlin("1.5")
public fun Double.toULong(): ULong = doubleToULong(this)
