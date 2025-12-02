/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.sequences

/**
 * A sequence that returns the results of applying the given [transformer] function to the values
 * in the underlying [sequence].
 */
internal class TransformingSequence<T, R>(
    private val sequence: Sequence<T>,
    private val transformer: (T) -> R
) : Sequence<R> {
    override fun iterator(): Iterator<R> = object : Iterator<R> {
        val iterator = sequence.iterator()
        override fun next(): R {
            return transformer(iterator.next())
        }

        override fun hasNext(): Boolean {
            return iterator.hasNext()
        }
    }

    internal fun <E> flatten(iterator: (R) -> Iterator<E>): Sequence<E> {
        return FlatteningSequence(sequence, transformer, iterator)
    }
}

/**
 * A sequence that returns the results of applying the given [transformer] function to the values
 * in the underlying [sequence] that match the given [predicate].
 */
internal class TransformingIndexedSequence<T, R>(
    private val sequence: Sequence<T>,
    private val transformer: (Int, T) -> R
) : Sequence<R> {
    override fun iterator(): Iterator<R> = object : Iterator<R> {
        val iterator = sequence.iterator()
        var index = 0
        override fun next(): R {
            return transformer(index++, iterator.next())
        }

        override fun hasNext(): Boolean {
            return iterator.hasNext()
        }
    }
}

/**
 * A sequence that returns values from the underlying [sequence] that match the given [predicate].
 */
internal class FilteringSequence<T>(
    private val sequence: Sequence<T>,
    private val sendWhen: Boolean = true,
    private val predicate: (T) -> Boolean
) : Sequence<T> {

    override fun iterator(): Iterator<T> = object : Iterator<T> {
        val iterator = sequence.iterator()
        var nextState: Int = -1 // -1 for unknown, 0 for done, 1 for continue
        var nextItem: T? = null

        private fun calcNext() {
            while (iterator.hasNext()) {
                val item = iterator.next()
                if (predicate(item) == sendWhen) {
                    nextItem = item
                    nextState = 1
                    return
                }
            }
            nextState = 0
        }

        override fun next(): T {
            if (nextState == -1)
                calcNext()
            if (nextState == 0)
                throw NoSuchElementException()
            val result = nextItem
            nextItem = null
            nextState = -1
            @Suppress("UNCHECKED_CAST")
            return result as T
        }

        override fun hasNext(): Boolean {
            if (nextState == -1)
                calcNext()
            return nextState == 1
        }
    }
}

/**
 * A sequence that returns values from the underlying [sequence] that match the given [predicate],
 * along with their index.
 */
internal class FilteringIndexedSequence<T>(
    private val sequence: Sequence<T>,
    private val sendWhen: Boolean = true,
    private val predicate: (Int, T) -> Boolean
) : Sequence<T> {

    override fun iterator(): Iterator<T> = object : Iterator<T> {
        val iterator = sequence.iterator()
        var index = 0
        var nextState: Int = -1 // -1 for unknown, 0 for done, 1 for continue
        var nextItem: T? = null

        private fun calcNext() {
            while (iterator.hasNext()) {
                val item = iterator.next()
                if (predicate(index++, item) == sendWhen) {
                    nextItem = item
                    nextState = 1
                    return
                }
            }
            nextState = 0
        }

        override fun next(): T {
            if (nextState == -1)
                calcNext()
            if (nextState == 0)
                throw NoSuchElementException()
            val result = nextItem
            nextItem = null
            nextState = -1
            @Suppress("UNCHECKED_CAST")
            return result as T
        }

        override fun hasNext(): Boolean {
            if (nextState == -1)
                calcNext()
            return nextState == 1
        }
    }
}

/**
 * A sequence that returns the results of flattening the results of applying the given [transformer]
 * function to the values in the underlying [sequence].
 */
internal class FlatteningSequence<T, R, E>(
    private val sequence: Sequence<T>,
    private val transformer: (T) -> R,
    private val iterator: (R) -> Iterator<E>
) : Sequence<E> {
    override fun iterator(): Iterator<E> = object : Iterator<E> {
        val iterator = sequence.iterator()
        var itemIterator: Iterator<E>? = null

        override fun next(): E {
            if (!ensureItemIterator())
                throw NoSuchElementException()
            return itemIterator!!.next()
        }

        override fun hasNext(): Boolean {
            return ensureItemIterator()
        }

        private fun ensureItemIterator(): Boolean {
            if (itemIterator?.hasNext() == false)
                itemIterator = null

            while (itemIterator == null) {
                if (!iterator.hasNext()) {
                    return false
                } else {
                    val element = iterator.next()
                    val nextItemIterator = iterator(transformer(element))
                    if (nextItemIterator.hasNext()) {
                        itemIterator = nextItemIterator
                        return true
                    }
                }
            }
            return true
        }
    }
}

/**
 * A sequence that only returns the first [count] values from the underlying [sequence].
 */
internal class TakingSequence<T>(
    private val sequence: Sequence<T>,
    private val count: Int
) : Sequence<T>, DropTakeSequence<T> {

    init {
        require(count >= 0) { "count must be non-negative, but was $count." }
    }

    override fun drop(n: Int): Sequence<T> = if (n >= count) emptySequence() else SubSequence(sequence, n, count)
    override fun take(n: Int): Sequence<T> = if (n >= count) this else TakingSequence(sequence, n)

    override fun iterator(): Iterator<T> = object : Iterator<T> {
        var left = count
        val iterator = sequence.iterator()

        override fun next(): T {
            if (left == 0)
                throw NoSuchElementException()
            left--
            return iterator.next()
        }

        override fun hasNext(): Boolean {
            return left > 0 && iterator.hasNext()
        }
    }
}

/**
 * A sequence that only returns values from the underlying [sequence] while the [predicate] returns true.
 */
internal class TakingWhileSequence<T>(
    private val sequence: Sequence<T>,
    private val predicate: (T) -> Boolean
) : Sequence<T> {
    override fun iterator(): Iterator<T> = object : Iterator<T> {
        val iterator = sequence.iterator()
        var nextState: Int = -1 // -1 for unknown, 0 for done, 1 for continue
        var nextItem: T? = null

        private fun calcNext() {
            if (iterator.hasNext()) {
                val item = iterator.next()
                if (predicate(item)) {
                    nextState = 1
                    nextItem = item
                    return
                }
            }
            nextState = 0
        }

        override fun next(): T {
            if (nextState == -1)
                calcNext()
            if (nextState == 0)
                throw NoSuchElementException()
            @Suppress("UNCHECKED_CAST")
            val result = nextItem as T
            nextItem = null
            nextState = -1
            return result
        }

        override fun hasNext(): Boolean {
            if (nextState == -1)
                calcNext()
            return nextState == 1
        }
    }
}

/**
 * A sequence that skips the first [count] values from the underlying [sequence].
 */
internal class DroppingSequence<T>(
    private val sequence: Sequence<T>,
    private val count: Int
) : Sequence<T>, DropTakeSequence<T> {

    init {
        require(count >= 0) { "count must be non-negative, but was $count." }
    }

    override fun drop(n: Int): Sequence<T> = DroppingSequence(sequence, count + n)
    override fun take(n: Int): Sequence<T> = SubSequence(sequence, count, n)

    override fun iterator(): Iterator<T> = object : Iterator<T> {
        val iterator = sequence.iterator()
        var left = count

        // Optimization to drop items up-front
        private fun drop() {
            while (left > 0 && iterator.hasNext()) {
                iterator.next()
                left--
            }
        }

        override fun next(): T {
            drop()
            return iterator.next()
        }

        override fun hasNext(): Boolean {
            drop()
            return iterator.hasNext()
        }
    }
}

/**
 * A sequence that skips values from the underlying [sequence] while the [predicate] returns true.
 */
internal class DroppingWhileSequence<T>(
    private val sequence: Sequence<T>,
    private val predicate: (T) -> Boolean
) : Sequence<T> {

    override fun iterator(): Iterator<T> = object : Iterator<T> {
        val iterator = sequence.iterator()
        var dropState: Int = -1 // -1 for not dropping, 1 for nextItem, 0 for normal iteration
        var nextItem: T? = null

        private fun drop() {
            while (iterator.hasNext()) {
                val item = iterator.next()
                if (!predicate(item)) {
                    nextItem = item
                    dropState = 1
                    return
                }
            }
            dropState = 0
        }

        override fun next(): T {
            if (dropState == -1)
                drop()

            if (dropState == 1) {
                @Suppress("UNCHECKED_CAST")
                val result = nextItem as T
                nextItem = null
                dropState = 0
                return result
            }
            return iterator.next()
        }

        override fun hasNext(): Boolean {
            if (dropState == -1)
                drop()
            return dropState == 1 || iterator.hasNext()
        }
    }
}

/**
 * A sequence that returns a subset of values from the underlying [sequence] starting at [startIndex] and containing [count] values.
 */
internal class SubSequence<T>(
    private val sequence: Sequence<T>,
    private val startIndex: Int,
    private val endIndex: Int
) : Sequence<T>, DropTakeSequence<T> {

    init {
        require(startIndex >= 0) { "startIndex should be non-negative, but is $startIndex" }
        require(endIndex >= 0) { "endIndex should be non-negative, but is $endIndex" }
        require(endIndex >= startIndex) { "endIndex should be not less than startIndex, but was $endIndex < $startIndex" }
    }

    private val count: Int get() = endIndex - startIndex

    override fun drop(n: Int): Sequence<T> = if (n >= count) emptySequence() else SubSequence(sequence, startIndex + n, endIndex)
    override fun take(n: Int): Sequence<T> = if (n >= count) this else SubSequence(sequence, startIndex, startIndex + n)

    override fun iterator(): Iterator<T> = object : Iterator<T> {
        val iterator = sequence.iterator()
        var position = 0

        // Optimization to drop elements up-front
        private fun drop() {
            while (position < startIndex && iterator.hasNext()) {
                iterator.next()
                position++
            }
        }

        override fun hasNext(): Boolean {
            drop()
            return (position < endIndex) && iterator.hasNext()
        }

        override fun next(): T {
            drop()
            if (position >= endIndex)
                throw NoSuchElementException()
            position++
            return iterator.next()
        }
    }
}

/**
 * A sequence that returns distinct elements from the underlying [sequence] based on the [keySelector].
 */
internal class DistinctSequence<T, K>(
    private val source: Sequence<T>,
    private val keySelector: (T) -> K
) : Sequence<T> {
    override fun iterator(): Iterator<T> = object : Iterator<T> {
        val sourceIterator = source.iterator()
        val observed = HashSet<K>()
        var nextValue: T? = null
        var nextState: Int = -1 // -1 for unknown, 0 for done, 1 for continue

        private fun calcNext() {
            while (sourceIterator.hasNext()) {
                val next = sourceIterator.next()
                val key = keySelector(next)

                if (observed.add(key)) {
                    nextValue = next
                    nextState = 1
                    return
                }
            }
            nextState = 0
        }

        override fun hasNext(): Boolean {
            if (nextState == -1)
                calcNext()
            return nextState == 1
        }

        override fun next(): T {
            if (nextState == -1)
                calcNext()
            if (nextState == 0)
                throw NoSuchElementException()
            @Suppress("UNCHECKED_CAST")
            val value = nextValue as T
            nextValue = null
            nextState = -1
            return value
        }
    }
}

/**
 * A sequence that merges two sequences into a sequence of pairs.
 */
internal class MergingSequence<T1, T2, V>(
    private val sequence1: Sequence<T1>,
    private val sequence2: Sequence<T2>,
    private val transform: (T1, T2) -> V
) : Sequence<V> {
    override fun iterator(): Iterator<V> = object : Iterator<V> {
        val iterator1 = sequence1.iterator()
        val iterator2 = sequence2.iterator()

        override fun hasNext(): Boolean = iterator1.hasNext() && iterator2.hasNext()

        override fun next(): V {
            return transform(iterator1.next(), iterator2.next())
        }
    }
}
