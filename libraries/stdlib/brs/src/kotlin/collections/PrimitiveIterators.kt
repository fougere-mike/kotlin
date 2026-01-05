/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.collections

/**
 * An iterator over a sequence of values of type `Byte`.
 */
public abstract class ByteIterator : Iterator<Byte> {
    final override fun next(): Byte = nextByte()
    public abstract fun nextByte(): Byte
}

/**
 * An iterator over a sequence of values of type `Char`.
 */
public abstract class CharIterator : Iterator<Char> {
    final override fun next(): Char = nextChar()
    public abstract fun nextChar(): Char
}

/**
 * An iterator over a sequence of values of type `Short`.
 */
public abstract class ShortIterator : Iterator<Short> {
    final override fun next(): Short = nextShort()
    public abstract fun nextShort(): Short
}

/**
 * An iterator over a sequence of values of type `Int`.
 */
public abstract class IntIterator : Iterator<Int> {
    final override fun next(): Int = nextInt()
    public abstract fun nextInt(): Int
}

/**
 * An iterator over a sequence of values of type `Long`.
 */
public abstract class LongIterator : Iterator<Long> {
    final override fun next(): Long = nextLong()
    public abstract fun nextLong(): Long
}

/**
 * An iterator over a sequence of values of type `Float`.
 */
public abstract class FloatIterator : Iterator<Float> {
    final override fun next(): Float = nextFloat()
    public abstract fun nextFloat(): Float
}

/**
 * An iterator over a sequence of values of type `Double`.
 */
public abstract class DoubleIterator : Iterator<Double> {
    final override fun next(): Double = nextDouble()
    public abstract fun nextDouble(): Double
}

/**
 * An iterator over a sequence of values of type `Boolean`.
 */
public abstract class BooleanIterator : Iterator<Boolean> {
    final override fun next(): Boolean = nextBoolean()
    public abstract fun nextBoolean(): Boolean
}
