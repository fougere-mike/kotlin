/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin

/**
 * Represents an 8-bit unsigned integer.
 * On BrightScript, a UByte is represented as a regular class with a Byte backing field.
 */
@SinceKotlin("1.5")
public class UByte @PublishedApi internal constructor(@PublishedApi internal val data: Byte) : Comparable<UByte> {

    companion object {
        /**
         * A constant holding the minimum value an instance of UByte can have.
         */
        public val MIN_VALUE: UByte = UByte(0)

        /**
         * A constant holding the maximum value an instance of UByte can have.
         */
        public val MAX_VALUE: UByte = UByte(-1)

        /**
         * The number of bytes used to represent an instance of UByte in a binary form.
         */
        public const val SIZE_BYTES: Int = 1

        /**
         * The number of bits used to represent an instance of UByte in a binary form.
         */
        public const val SIZE_BITS: Int = 8
    }

    /** Compares this value with the specified value for order. */
    public override operator fun compareTo(other: UByte): Int = this.toInt().compareTo(other.toInt())

    /** Compares this value with the specified value for order. */
    public inline operator fun compareTo(other: UShort): Int = this.toInt().compareTo(other.toInt())

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
    public inline operator fun inc(): UByte = UByte((data + 1).toByte())

    /** Returns this value decremented by one. */
    public inline operator fun dec(): UByte = UByte((data - 1).toByte())

    /** Creates a range from this value to the specified [other] value. */
    public operator fun rangeTo(other: UByte): UIntRange = UIntRange(this.toUInt(), other.toUInt())

    /**
     * Creates a range from this value up to but excluding the specified [other] value.
     */
    @SinceKotlin("1.9")
    public operator fun rangeUntil(other: UByte): UIntRange = this.toUInt() until other.toUInt()

    /** Performs a bitwise AND operation between the two values. */
    public inline infix fun and(other: UByte): UByte = UByte((this.toInt() and other.toInt()).toByte())
    /** Performs a bitwise OR operation between the two values. */
    public inline infix fun or(other: UByte): UByte = UByte((this.toInt() or other.toInt()).toByte())
    /** Performs a bitwise XOR operation between the two values. */
    public inline infix fun xor(other: UByte): UByte = UByte((this.toInt() xor other.toInt()).toByte())
    /** Inverts the bits in this value. */
    public inline fun inv(): UByte = UByte(this.toInt().inv().toByte())

    /** Converts this [UByte] value to [Byte]. */
    public inline fun toByte(): Byte = data
    /** Converts this [UByte] value to [Short]. */
    public inline fun toShort(): Short = (data.toInt() and 0xFF).toShort()
    /** Converts this [UByte] value to [Int]. */
    public inline fun toInt(): Int = data.toInt() and 0xFF
    /** Converts this [UByte] value to [Long]. */
    public inline fun toLong(): Long = data.toLong() and 0xFF

    /** Returns this value. */
    public inline fun toUByte(): UByte = this
    /** Converts this [UByte] value to [UShort]. */
    public inline fun toUShort(): UShort = UShort((data.toInt() and 0xFF).toShort())
    /** Converts this [UByte] value to [UInt]. */
    public inline fun toUInt(): UInt = UInt(data.toInt() and 0xFF)
    /** Converts this [UByte] value to [ULong]. */
    public inline fun toULong(): ULong = ULong(data.toLong() and 0xFF)

    /** Converts this [UByte] value to [Float]. */
    public inline fun toFloat(): Float = this.toInt().toFloat()
    /** Converts this [UByte] value to [Double]. */
    public inline fun toDouble(): Double = this.toInt().toDouble()

    public override fun toString(): String = toInt().toString()

    public override fun equals(other: Any?): Boolean =
        other is UByte && this.data == other.data

    public override fun hashCode(): Int = data.toInt()
}

/**
 * Converts this [Byte] value to [UByte].
 */
@SinceKotlin("1.5")
public fun Byte.toUByte(): UByte = UByte(this)
/**
 * Converts this [Short] value to [UByte].
 */
@SinceKotlin("1.5")
public fun Short.toUByte(): UByte = UByte(this.toByte())
/**
 * Converts this [Int] value to [UByte].
 */
@SinceKotlin("1.5")
public fun Int.toUByte(): UByte = UByte(this.toByte())
/**
 * Converts this [Long] value to [UByte].
 */
@SinceKotlin("1.5")
public fun Long.toUByte(): UByte = UByte(this.toByte())
