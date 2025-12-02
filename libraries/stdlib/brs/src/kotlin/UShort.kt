/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin

/**
 * Represents a 16-bit unsigned integer.
 * On BrightScript, a UShort is represented as a regular class with a Short backing field.
 */
@SinceKotlin("1.5")
public class UShort public constructor(public val data: Short) : Comparable<UShort> {

    companion object {
        /**
         * A constant holding the minimum value an instance of UShort can have.
         */
        public val MIN_VALUE: UShort = UShort(0)

        /**
         * A constant holding the maximum value an instance of UShort can have.
         */
        public val MAX_VALUE: UShort = UShort(-1)

        /**
         * The number of bytes used to represent an instance of UShort in a binary form.
         */
        public const val SIZE_BYTES: Int = 2

        /**
         * The number of bits used to represent an instance of UShort in a binary form.
         */
        public const val SIZE_BITS: Int = 16
    }

    /** Compares this value with the specified value for order. */
    public inline operator fun compareTo(other: UByte): Int = this.toInt().compareTo(other.toInt())

    /** Compares this value with the specified value for order. */
    public override operator fun compareTo(other: UShort): Int = this.toInt().compareTo(other.toInt())

    /** Compares this value with the specified value for order. */
    public inline operator fun compareTo(other: UInt): Int = this.toUInt().compareTo(other)

    /** Compares this value with the specified value for order. */
    public inline operator fun compareTo(other: ULong): Int = this.toULong().compareTo(other)

    /** Adds the other value to this value. */
    public inline operator fun plus(other: UByte): UInt = this.toUInt().plus(other.toUInt())
    /** Adds the other value to this value. */
    public inline operator fun plus(other: UShort): UInt = this.toUInt().plus(other.toUInt())
    /** Adds the other value to this value. */
    public inline operator fun plus(other: UInt): UInt = this.toUInt().plus(other)
    /** Adds the other value to this value. */
    public inline operator fun plus(other: ULong): ULong = this.toULong().plus(other)

    /** Subtracts the other value from this value. */
    public inline operator fun minus(other: UByte): UInt = this.toUInt().minus(other.toUInt())
    /** Subtracts the other value from this value. */
    public inline operator fun minus(other: UShort): UInt = this.toUInt().minus(other.toUInt())
    /** Subtracts the other value from this value. */
    public inline operator fun minus(other: UInt): UInt = this.toUInt().minus(other)
    /** Subtracts the other value from this value. */
    public inline operator fun minus(other: ULong): ULong = this.toULong().minus(other)

    /** Multiplies this value by the other value. */
    public inline operator fun times(other: UByte): UInt = this.toUInt().times(other.toUInt())
    /** Multiplies this value by the other value. */
    public inline operator fun times(other: UShort): UInt = this.toUInt().times(other.toUInt())
    /** Multiplies this value by the other value. */
    public inline operator fun times(other: UInt): UInt = this.toUInt().times(other)
    /** Multiplies this value by the other value. */
    public inline operator fun times(other: ULong): ULong = this.toULong().times(other)

    /** Divides this value by the other value, truncating the result to an integer that is closer to zero. */
    public inline operator fun div(other: UByte): UInt = this.toUInt().div(other.toUInt())
    /** Divides this value by the other value, truncating the result to an integer that is closer to zero. */
    public inline operator fun div(other: UShort): UInt = this.toUInt().div(other.toUInt())
    /** Divides this value by the other value, truncating the result to an integer that is closer to zero. */
    public inline operator fun div(other: UInt): UInt = this.toUInt().div(other)
    /** Divides this value by the other value, truncating the result to an integer that is closer to zero. */
    public inline operator fun div(other: ULong): ULong = this.toULong().div(other)

    /** Calculates the remainder of truncating division of this value by the other value. */
    public inline operator fun rem(other: UByte): UInt = this.toUInt().rem(other.toUInt())
    /** Calculates the remainder of truncating division of this value by the other value. */
    public inline operator fun rem(other: UShort): UInt = this.toUInt().rem(other.toUInt())
    /** Calculates the remainder of truncating division of this value by the other value. */
    public inline operator fun rem(other: UInt): UInt = this.toUInt().rem(other)
    /** Calculates the remainder of truncating division of this value by the other value. */
    public inline operator fun rem(other: ULong): ULong = this.toULong().rem(other)

    /** Divides this value by the other value, flooring the result. */
    public inline fun floorDiv(other: UByte): UInt = this.toUInt().floorDiv(other.toUInt())
    /** Divides this value by the other value, flooring the result. */
    public inline fun floorDiv(other: UShort): UInt = this.toUInt().floorDiv(other.toUInt())
    /** Divides this value by the other value, flooring the result. */
    public inline fun floorDiv(other: UInt): UInt = this.toUInt().floorDiv(other)
    /** Divides this value by the other value, flooring the result. */
    public inline fun floorDiv(other: ULong): ULong = this.toULong().floorDiv(other)

    /** Calculates the remainder of flooring division. */
    public inline fun mod(other: UByte): UByte = this.toUInt().mod(other.toUInt()).toUByte()
    /** Calculates the remainder of flooring division. */
    public inline fun mod(other: UShort): UShort = this.toUInt().mod(other.toUInt()).toUShort()
    /** Calculates the remainder of flooring division. */
    public inline fun mod(other: UInt): UInt = this.toUInt().mod(other)
    /** Calculates the remainder of flooring division. */
    public inline fun mod(other: ULong): ULong = this.toULong().mod(other)

    /** Returns this value incremented by one. */
    public inline operator fun inc(): UShort = UShort((data + 1).toShort())

    /** Returns this value decremented by one. */
    public inline operator fun dec(): UShort = UShort((data - 1).toShort())

    /** Creates a range from this value to the specified [other] value. */
    public operator fun rangeTo(other: UShort): UIntRange = UIntRange(this.toUInt(), other.toUInt())

    /**
     * Creates a range from this value up to but excluding the specified [other] value.
     */
    @SinceKotlin("1.9")
    public operator fun rangeUntil(other: UShort): UIntRange = this.toUInt() until other.toUInt()

    /** Performs a bitwise AND operation between the two values. */
    public inline infix fun and(other: UShort): UShort = UShort((this.toInt() and other.toInt()).toShort())
    /** Performs a bitwise OR operation between the two values. */
    public inline infix fun or(other: UShort): UShort = UShort((this.toInt() or other.toInt()).toShort())
    /** Performs a bitwise XOR operation between the two values. */
    public inline infix fun xor(other: UShort): UShort = UShort((this.toInt() xor other.toInt()).toShort())
    /** Inverts the bits in this value. */
    public inline fun inv(): UShort = UShort(this.toInt().inv().toShort())

    /** Converts this [UShort] value to [Byte]. */
    public inline fun toByte(): Byte = data.toByte()
    /** Converts this [UShort] value to [Short]. */
    public inline fun toShort(): Short = data
    /** Converts this [UShort] value to [Int]. */
    public inline fun toInt(): Int = data.toInt() and 0xFFFF
    /** Converts this [UShort] value to [Long]. */
    public inline fun toLong(): Long = data.toLong() and 0xFFFF

    /** Converts this [UShort] value to [UByte]. */
    public inline fun toUByte(): UByte = UByte(data.toByte())
    /** Returns this value. */
    public inline fun toUShort(): UShort = this
    /** Converts this [UShort] value to [UInt]. */
    public inline fun toUInt(): UInt = UInt(data.toInt() and 0xFFFF)
    /** Converts this [UShort] value to [ULong]. */
    public inline fun toULong(): ULong = ULong(data.toLong() and 0xFFFF)

    /** Converts this [UShort] value to [Float]. */
    public inline fun toFloat(): Float = this.toInt().toFloat()
    /** Converts this [UShort] value to [Double]. */
    public inline fun toDouble(): Double = this.toInt().toDouble()

    public override fun toString(): String = toInt().toString()

    public override fun equals(other: Any?): Boolean =
        other is UShort && this.data == other.data

    public override fun hashCode(): Int = data.toInt()
}

/**
 * Converts this [Byte] value to [UShort].
 */
@SinceKotlin("1.5")
public fun Byte.toUShort(): UShort = UShort(this.toShort())
/**
 * Converts this [Short] value to [UShort].
 */
@SinceKotlin("1.5")
public fun Short.toUShort(): UShort = UShort(this)
/**
 * Converts this [Int] value to [UShort].
 */
@SinceKotlin("1.5")
public fun Int.toUShort(): UShort = UShort(this.toShort())
/**
 * Converts this [Long] value to [UShort].
 */
@SinceKotlin("1.5")
public fun Long.toUShort(): UShort = UShort(this.toShort())
