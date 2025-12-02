/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.sequences

/**
 * Returns a [List] containing all elements of this sequence.
 */
public fun <T> Sequence<T>.toList(): List<T> {
    return this.toMutableList()
}

/**
 * Returns a [MutableList] filled with all elements of this sequence.
 */
public fun <T> Sequence<T>.toMutableList(): MutableList<T> {
    return toCollection(ArrayList<T>())
}

/**
 * Returns a [Set] of all elements.
 */
public fun <T> Sequence<T>.toSet(): Set<T> {
    return toCollection(HashSet<T>())
}

/**
 * Returns a [MutableSet] of all elements.
 */
public fun <T> Sequence<T>.toMutableSet(): MutableSet<T> {
    return toCollection(HashSet<T>())
}

/**
 * Returns a [HashSet] of all elements.
 */
public fun <T> Sequence<T>.toHashSet(): HashSet<T> {
    return toCollection(HashSet<T>())
}

/**
 * Appends all elements to the given [destination] collection.
 */
public fun <T, C : MutableCollection<in T>> Sequence<T>.toCollection(destination: C): C {
    for (item in this) {
        destination.add(item)
    }
    return destination
}

/**
 * Returns a [Map] containing key-value pairs provided by [transform] function
 * applied to elements of the given sequence.
 */
public inline fun <T, K, V> Sequence<T>.associate(transform: (T) -> Pair<K, V>): Map<K, V> {
    return associateTo(HashMap<K, V>(), transform)
}

/**
 * Returns a [Map] containing the elements from the given sequence indexed by the key
 * returned from [keySelector] function applied to each element.
 */
public inline fun <T, K> Sequence<T>.associateBy(keySelector: (T) -> K): Map<K, T> {
    return associateByTo(HashMap<K, T>(), keySelector)
}

/**
 * Returns a [Map] containing the values provided by [valueTransform] and indexed by [keySelector] functions applied to elements of the given sequence.
 */
public inline fun <T, K, V> Sequence<T>.associateBy(keySelector: (T) -> K, valueTransform: (T) -> V): Map<K, V> {
    return associateByTo(HashMap<K, V>(), keySelector, valueTransform)
}

/**
 * Populates and returns the [destination] mutable map with key-value pairs
 * provided by [transform] function applied to each element of the given sequence.
 */
public inline fun <T, K, V, M : MutableMap<in K, in V>> Sequence<T>.associateTo(destination: M, transform: (T) -> Pair<K, V>): M {
    for (element in this) {
        val pair = transform(element)
        destination.put(pair.first, pair.second)
    }
    return destination
}

/**
 * Populates and returns the [destination] mutable map with key-value pairs,
 * where key is provided by the [keySelector] function applied to each element of the given sequence
 * and value is the element itself.
 */
public inline fun <T, K, M : MutableMap<in K, in T>> Sequence<T>.associateByTo(destination: M, keySelector: (T) -> K): M {
    for (element in this) {
        destination.put(keySelector(element), element)
    }
    return destination
}

/**
 * Populates and returns the [destination] mutable map with key-value pairs,
 * where key is provided by the [keySelector] function and
 * and value is provided by the [valueTransform] function applied to elements of the given sequence.
 */
public inline fun <T, K, V, M : MutableMap<in K, in V>> Sequence<T>.associateByTo(
    destination: M,
    keySelector: (T) -> K,
    valueTransform: (T) -> V
): M {
    for (element in this) {
        destination.put(keySelector(element), valueTransform(element))
    }
    return destination
}

/**
 * Returns a [Map] where keys are elements from the given sequence and values are
 * produced by the [valueSelector] function applied to each element.
 */
public inline fun <K, V> Sequence<K>.associateWith(valueSelector: (K) -> V): Map<K, V> {
    val result = HashMap<K, V>()
    return associateWithTo(result, valueSelector)
}

/**
 * Populates and returns the [destination] mutable map with key-value pairs for each element of the given sequence,
 * where key is the element itself and value is provided by the [valueSelector] function applied to that key.
 */
public inline fun <K, V, M : MutableMap<in K, in V>> Sequence<K>.associateWithTo(destination: M, valueSelector: (K) -> V): M {
    for (element in this) {
        destination.put(element, valueSelector(element))
    }
    return destination
}

/**
 * Returns a list of results of applying the given [transform] function to each element in the original sequence.
 */
public fun <T, R> Sequence<T>.map(transform: (T) -> R): Sequence<R> {
    return TransformingSequence(this, transform)
}

/**
 * Returns a list of results of applying the given [transform] function to each element and its index in the original sequence.
 */
public fun <T, R> Sequence<T>.mapIndexed(transform: (Int, T) -> R): Sequence<R> {
    return TransformingIndexedSequence(this, transform)
}

/**
 * Returns a sequence containing only elements matching the given [predicate].
 */
public fun <T> Sequence<T>.filter(predicate: (T) -> Boolean): Sequence<T> {
    return FilteringSequence(this, true, predicate)
}

/**
 * Returns a sequence containing only elements not matching the given [predicate].
 */
public fun <T> Sequence<T>.filterNot(predicate: (T) -> Boolean): Sequence<T> {
    return FilteringSequence(this, false, predicate)
}

/**
 * Returns a sequence containing only elements matching the given [predicate].
 * @param [predicate] function that takes the index of an element and the element itself
 * and returns the result of predicate evaluation on the element.
 */
public fun <T> Sequence<T>.filterIndexed(predicate: (Int, T) -> Boolean): Sequence<T> {
    return FilteringIndexedSequence(this, true, predicate)
}

/**
 * Returns a sequence containing all elements that are not `null`.
 */
public fun <T : Any> Sequence<T?>.filterNotNull(): Sequence<T> {
    @Suppress("UNCHECKED_CAST")
    return filter { it != null } as Sequence<T>
}

/**
 * Returns a single sequence of all elements from results of [transform] function being invoked on each element of original sequence.
 */
public fun <T, R> Sequence<T>.flatMap(transform: (T) -> Iterable<R>): Sequence<R> {
    return FlatteningSequence(this, transform, { it.iterator() })
}

/**
 * Returns a sequence of all elements from all sequences in this sequence.
 */
public fun <T> Sequence<Sequence<T>>.flatten(): Sequence<T> {
    return flatten { it.iterator() }
}

/**
 * Returns a sequence of all elements from all iterables in this sequence.
 */
public fun <T> Sequence<Iterable<T>>.flatten(): Sequence<T> {
    return flatten { it.iterator() }
}

private fun <T, R> Sequence<T>.flatten(iterator: (T) -> Iterator<R>): Sequence<R> {
    if (this is TransformingSequence<*, *>) {
        @Suppress("UNCHECKED_CAST")
        return (this as TransformingSequence<*, T>).flatten(iterator)
    }
    return FlatteningSequence(this, { it }, iterator)
}

/**
 * Returns a sequence containing first [n] elements.
 */
public fun <T> Sequence<T>.take(n: Int): Sequence<T> {
    require(n >= 0) { "Requested element count $n is less than zero." }
    return when {
        n == 0 -> emptySequence()
        this is DropTakeSequence -> this.take(n)
        else -> TakingSequence(this, n)
    }
}

/**
 * Returns a sequence containing first elements satisfying the given [predicate].
 */
public fun <T> Sequence<T>.takeWhile(predicate: (T) -> Boolean): Sequence<T> {
    return TakingWhileSequence(this, predicate)
}

/**
 * Returns a sequence containing all elements except first [n] elements.
 */
public fun <T> Sequence<T>.drop(n: Int): Sequence<T> {
    require(n >= 0) { "Requested element count $n is less than zero." }
    return when {
        n == 0 -> this
        this is DropTakeSequence -> this.drop(n)
        else -> DroppingSequence(this, n)
    }
}

/**
 * Returns a sequence containing all elements except first elements that satisfy the given [predicate].
 */
public fun <T> Sequence<T>.dropWhile(predicate: (T) -> Boolean): Sequence<T> {
    return DroppingWhileSequence(this, predicate)
}

/**
 * Returns a sequence containing only distinct elements from the given sequence.
 */
public fun <T> Sequence<T>.distinct(): Sequence<T> {
    return this.distinctBy { it }
}

/**
 * Returns a sequence containing only elements from the given sequence
 * having distinct keys returned by the given [selector] function.
 */
public fun <T, K> Sequence<T>.distinctBy(selector: (T) -> K): Sequence<T> {
    return DistinctSequence(this, selector)
}

/**
 * Returns a sequence of pairs built from the elements of `this` sequence and the [other] sequence with the same index.
 * The returned sequence ends as soon as the shortest input sequence ends.
 */
public infix fun <T, R> Sequence<T>.zip(other: Sequence<R>): Sequence<Pair<T, R>> {
    return MergingSequence(this, other) { t1, t2 -> t1 to t2 }
}

/**
 * Returns a sequence of values built from the elements of `this` sequence and the [other] sequence with the same index
 * using the provided [transform] function applied to each pair of elements.
 * The returned sequence ends as soon as the shortest input sequence ends.
 */
public fun <T, R, V> Sequence<T>.zip(other: Sequence<R>, transform: (a: T, b: R) -> V): Sequence<V> {
    return MergingSequence(this, other, transform)
}

/**
 * Returns a sequence that yields elements of this sequence sorted according to their natural sort order.
 */
public fun <T : Comparable<T>> Sequence<T>.sorted(): Sequence<T> {
    return object : Sequence<T> {
        override fun iterator(): Iterator<T> {
            val sortedList = this@sorted.toMutableList()
            sortedList.sort()
            return sortedList.iterator()
        }
    }
}

/**
 * Returns a sequence that yields elements of this sequence sorted according to the specified [comparator].
 */
public fun <T> Sequence<T>.sortedWith(comparator: Comparator<in T>): Sequence<T> {
    return object : Sequence<T> {
        override fun iterator(): Iterator<T> {
            val sortedList = this@sortedWith.toMutableList()
            sortedList.sortWith(comparator)
            return sortedList.iterator()
        }
    }
}

/**
 * Returns a sequence that yields elements of this sequence sorted descending according to their natural sort order.
 */
public fun <T : Comparable<T>> Sequence<T>.sortedDescending(): Sequence<T> {
    return sortedWith(reverseOrder())
}

/**
 * Returns a sequence that yields elements of this sequence sorted according to natural sort order of the value returned by specified [selector] function.
 */
public inline fun <T, R : Comparable<R>> Sequence<T>.sortedBy(crossinline selector: (T) -> R?): Sequence<T> {
    return sortedWith(compareBy(selector))
}

/**
 * Returns a sequence that yields elements of this sequence sorted descending according to natural sort order of the value returned by specified [selector] function.
 */
public inline fun <T, R : Comparable<R>> Sequence<T>.sortedByDescending(crossinline selector: (T) -> R?): Sequence<T> {
    return sortedWith(compareByDescending(selector))
}

/**
 * Returns a sequence that iterates through the elements either of this sequence
 * or, if this sequence turns out to be empty, of the sequence returned by [defaultValue] function.
 */
public inline fun <T> Sequence<T>.ifEmpty(crossinline defaultValue: () -> Sequence<T>): Sequence<T> {
    return object : Sequence<T> {
        override fun iterator(): Iterator<T> {
            val iterator = this@ifEmpty.iterator()
            if (iterator.hasNext()) {
                return iterator
            } else {
                return defaultValue().iterator()
            }
        }
    }
}
