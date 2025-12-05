/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.sequences

/**
 * Returns an empty sequence.
 */
public fun <T> emptySequence(): Sequence<T> = EmptySequence

/**
 * Returns a sequence containing the specified [element].
 */
public fun <T> sequenceOf(element: T): Sequence<T> = object : Sequence<T> {
    override fun iterator(): Iterator<T> = object : Iterator<T> {
        private var hasNext = true
        override fun hasNext(): Boolean = hasNext
        override fun next(): T {
            if (!hasNext) throw NoSuchElementException()
            hasNext = false
            return element
        }
    }
}

/**
 * Returns a sequence containing all [elements].
 */
public fun <T> sequenceOf(vararg elements: T): Sequence<T> = if (elements.isEmpty()) emptySequence() else elements.asSequence()

/**
 * Returns a sequence that iterates through the elements provided by the given [iterator].
 */
internal fun <T> sequenceOf(iterator: Iterator<T>): Sequence<T> = object : Sequence<T> {
    override fun iterator(): Iterator<T> = iterator
}

/**
 * Returns a sequence that returns values through its iterator from the given [block] that uses [yield] to provide values.
 *
 * Note: Coroutine-based sequence building is not supported for BRS.
 * Use generateSequence() or other sequence builders instead.
 *
 * @suppress This function is not supported for BRS and will always throw an error.
 */
@Deprecated("Coroutine-based sequence building is not supported for BRS. Use generateSequence() instead.", level = DeprecationLevel.ERROR)
public fun <T> sequence(): Sequence<T> {
    throw UnsupportedOperationException("Coroutine-based sequence building is not supported for BRS")
}

/**
 * Returns a sequence of values starting with [seed] value and applying [nextFunction] to get the next value.
 * The sequence is infinite.
 *
 * @sample samples.collections.Sequences.Building.generateSequence
 */
public fun <T : Any> generateSequence(seed: T?, nextFunction: (T) -> T?): Sequence<T> =
    if (seed == null)
        EmptySequence
    else
        GeneratorSequence({ seed }, nextFunction)

/**
 * Returns a sequence defined by the starting value [seed] and the function [nextFunction],
 * which is invoked to calculate the next value based on the previous one on each iteration.
 *
 * The sequence produces values until it encounters first `null` value.
 * If [seed] is `null`, an empty sequence is produced.
 *
 * @sample samples.collections.Sequences.Building.generateSequenceWithSeed
 */
public fun <T : Any> generateSequence(seedFunction: () -> T?, nextFunction: (T) -> T?): Sequence<T> =
    GeneratorSequence(seedFunction, nextFunction)

/**
 * Returns a sequence that wraps each element of the original array into an [IndexedValue] containing the index and the element.
 */
public fun <T> Array<out T>.asSequence(): Sequence<T> = if (isEmpty()) emptySequence() else IndexedSequence(this)

/**
 * Returns a sequence that wraps each element of the original collection into an [IndexedValue] containing the index and the element.
 */
public fun <T> Iterable<T>.asSequence(): Sequence<T> = Sequence { this.iterator() }

/**
 * Creates a sequence that returns all values from this iterator. The sequence is constrained to be iterated only once.
 */
public fun <T> Iterator<T>.asSequence(): Sequence<T> = Sequence { this }

/**
 * Creates a sequence that returns the specified values.
 */
internal fun <T> Sequence(iterator: () -> Iterator<T>): Sequence<T> = object : Sequence<T> {
    override fun iterator(): Iterator<T> = iterator()
}

// Empty sequence singleton
private object EmptySequence : Sequence<Nothing>, DropTakeSequence<Nothing> {
    override fun iterator(): Iterator<Nothing> = SequenceEmptyIterator
    override fun drop(n: Int): Sequence<Nothing> = EmptySequence
    override fun take(n: Int): Sequence<Nothing> = EmptySequence
}

// Empty iterator singleton (renamed to avoid collision with ArrayList's EmptyIterator)
private object SequenceEmptyIterator : Iterator<Nothing> {
    override fun hasNext(): Boolean = false
    override fun next(): Nothing = throw NoSuchElementException()
}

// Generator sequence implementation
private class GeneratorSequence<T : Any>(
    private val getInitialValue: () -> T?,
    private val getNextValue: (T) -> T?
) : Sequence<T> {
    override fun iterator(): Iterator<T> = object : Iterator<T> {
        private var nextItem: T? = null
        private var nextState: Int = -2 // -2 for initial unknown, -1 for next not computed, 0 for done, 1 for continue

        private fun calcNext() {
            nextItem = if (nextState == -2) getInitialValue() else getNextValue(nextItem!!)
            nextState = if (nextItem == null) 0 else 1
        }

        override fun hasNext(): Boolean {
            if (nextState < 0)
                calcNext()
            return nextState == 1
        }

        override fun next(): T {
            if (nextState < 0)
                calcNext()
            if (nextState == 0)
                throw NoSuchElementException()
            val result = nextItem as T
            nextState = -1
            return result
        }
    }
}

// Indexed sequence for arrays
private class IndexedSequence<T>(private val array: Array<out T>) : Sequence<T> {
    override fun iterator(): Iterator<T> = array.iterator()
}

// SequenceScope stub (coroutine support not yet implemented for BRS)
/**
 * The scope for yielding values of a [Sequence] or an [Iterator].
 *
 * Note: Coroutine-based sequence building is not yet supported for BRS.
 * Use generateSequence() or other sequence builders instead.
 */
public abstract class SequenceScope<in T> internal constructor()

// DropTakeSequence interface for optimized drop/take operations
internal interface DropTakeSequence<out T> : Sequence<T> {
    fun drop(n: Int): Sequence<T>
    fun take(n: Int): Sequence<T>
}
