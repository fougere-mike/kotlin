/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.collections

/**
 * Represents a source of elements with a key associated with each element.
 */
public interface Grouping<T, out K> {
    /**
     * Returns a key for the given [element].
     */
    public fun keyOf(element: T): K

    /**
     * Returns an iterator over the elements of the source.
     */
    public fun sourceIterator(): Iterator<T>
}

/**
 * Groups elements from the [Grouping] source by key and counts elements in each group.
 *
 * @return a [Map] associating the key of each group with the count of elements in the group.
 */
public fun <T, K> Grouping<T, K>.eachCount(): Map<K, Int> {
    return fold(0) { _: K, acc: Int, _: T -> acc + 1 }
}

/**
 * Groups elements from the [Grouping] source by key and applies [operation] to the elements of each group sequentially,
 * passing the previously accumulated value and the current element as arguments, and stores the results in a new map.
 *
 * An initial value of accumulator is the same [initialValue] for each group.
 *
 * @param initialValue the initial value for the accumulator for each group.
 * @param operation a function that is invoked on each element with the following parameters:
 *  - key: the key of the group this element belongs to;
 *  - accumulator: the current value of the accumulator of the group;
 *  - element: the element from the source being accumulated.
 *
 * @return a [Map] associating the key of each group with the result of accumulating the group elements.
 */
public inline fun <T, K, R> Grouping<T, K>.fold(
    initialValue: R,
    operation: (key: K, accumulator: R, element: T) -> R
): Map<K, R> {
    val result = HashMap<K, R>()
    val iterator = sourceIterator()
    while (iterator.hasNext()) {
        val element = iterator.next()
        val key = keyOf(element)
        val accumulator = result[key] ?: initialValue
        result[key] = operation(key, accumulator, element)
    }
    return result
}

/**
 * Groups elements from the [Grouping] source by key and applies [operation] to the elements of each group sequentially,
 * passing the previously accumulated value and the current element as arguments, and stores the results in a new map.
 *
 * An initial value of accumulator is provided by [initialValueSelector] function.
 *
 * @param initialValueSelector a function that provides an initial value of accumulator for each group.
 *  It's invoked with parameters:
 *  - key: the key of the group;
 *  - element: the first element being encountered in that group.
 *
 * @param operation a function that is invoked on each element with the following parameters:
 *  - key: the key of the group this element belongs to;
 *  - accumulator: the current value of the accumulator of the group;
 *  - element: the element from the source being accumulated.
 *
 * @return a [Map] associating the key of each group with the result of accumulating the group elements.
 */
public inline fun <T, K, R> Grouping<T, K>.fold(
    initialValueSelector: (key: K, element: T) -> R,
    operation: (key: K, accumulator: R, element: T) -> R
): Map<K, R> {
    val result = HashMap<K, R>()
    val iterator = sourceIterator()
    while (iterator.hasNext()) {
        val element = iterator.next()
        val key = keyOf(element)
        val accumulator = result[key]
        result[key] = if (accumulator == null && !result.containsKey(key)) {
            initialValueSelector(key, element)
        } else {
            operation(key, accumulator as R, element)
        }
    }
    return result
}

/**
 * Groups elements from the [Grouping] source by key and applies the reducing [operation] to the elements of each group
 * sequentially starting from the second element of the group,
 * passing the previously accumulated value and the current element as arguments,
 * and stores the results in a new map.
 * An initial value of accumulator is the first element of the group.
 *
 * @param operation a function that is invoked on each subsequent element of the group with the following parameters:
 *  - key: the key of the group this element belongs to;
 *  - accumulator: the current value of the accumulator of the group;
 *  - element: the element from the source being accumulated.
 *
 * @return a [Map] associating the key of each group with the result of accumulating the group elements.
 */
public inline fun <S, T : S, K> Grouping<T, K>.reduce(
    operation: (key: K, accumulator: S, element: T) -> S
): Map<K, S> {
    val result = HashMap<K, S>()
    val iterator = sourceIterator()
    while (iterator.hasNext()) {
        val element = iterator.next()
        val key = keyOf(element)
        val accumulator = result[key]
        result[key] = if (accumulator == null && !result.containsKey(key)) {
            element
        } else {
            operation(key, accumulator as S, element)
        }
    }
    return result
}

/**
 * Groups elements of the original collection by the key returned by the given [keySelector] function
 * applied to each element and returns a map where each group key is associated with a list of corresponding elements.
 */
public inline fun <T, K> Iterable<T>.groupBy(keySelector: (T) -> K): Map<K, List<T>> {
    return groupByTo(HashMap<K, MutableList<T>>(), keySelector)
}

/**
 * Groups values returned by the [valueTransform] function applied to each element of the original collection
 * by the key returned by the given [keySelector] function applied to the element
 * and returns a map where each group key is associated with a list of corresponding values.
 */
public inline fun <T, K, V> Iterable<T>.groupBy(keySelector: (T) -> K, valueTransform: (T) -> V): Map<K, List<V>> {
    return groupByTo(HashMap<K, MutableList<V>>(), keySelector, valueTransform)
}

/**
 * Groups elements of the original collection by the key returned by the given [keySelector] function
 * applied to each element and puts to the [destination] map each group key associated with a list of corresponding elements.
 */
public inline fun <T, K, M : MutableMap<in K, MutableList<T>>> Iterable<T>.groupByTo(destination: M, keySelector: (T) -> K): M {
    for (element in this) {
        val key = keySelector(element)
        val list = destination.getOrPut(key) { ArrayList<T>() }
        list.add(element)
    }
    return destination
}

/**
 * Groups values returned by the [valueTransform] function applied to each element of the original collection
 * by the key returned by the given [keySelector] function applied to the element
 * and puts to the [destination] map each group key associated with a list of corresponding values.
 */
public inline fun <T, K, V, M : MutableMap<in K, MutableList<V>>> Iterable<T>.groupByTo(
    destination: M,
    keySelector: (T) -> K,
    valueTransform: (T) -> V
): M {
    for (element in this) {
        val key = keySelector(element)
        val list = destination.getOrPut(key) { ArrayList<V>() }
        list.add(valueTransform(element))
    }
    return destination
}

/**
 * Creates a [Grouping] source from a collection to be used later with one of group-and-fold operations
 * using the specified [keySelector] function to extract a key from each element.
 */
public inline fun <T, K> Iterable<T>.groupingBy(crossinline keySelector: (T) -> K): Grouping<T, K> {
    return object : Grouping<T, K> {
        override fun sourceIterator(): Iterator<T> = this@groupingBy.iterator()
        override fun keyOf(element: T): K = keySelector(element)
    }
}

/**
 * Returns a [Map] containing key-value pairs provided by [transform] function
 * applied to elements of the given collection.
 */
public inline fun <T, K, V> Iterable<T>.associate(transform: (T) -> Pair<K, V>): Map<K, V> {
    val result = HashMap<K, V>()
    for (element in this) {
        val pair = transform(element)
        result[pair.first] = pair.second
    }
    return result
}

/**
 * Returns a [Map] containing the elements from the given collection indexed by the key
 * returned from [keySelector] function applied to each element.
 */
public inline fun <T, K> Iterable<T>.associateBy(keySelector: (T) -> K): Map<K, T> {
    val result = HashMap<K, T>()
    for (element in this) {
        result[keySelector(element)] = element
    }
    return result
}

/**
 * Returns a [Map] containing the values provided by [valueTransform] and indexed by [keySelector] functions applied to elements of the given collection.
 */
public inline fun <T, K, V> Iterable<T>.associateBy(keySelector: (T) -> K, valueTransform: (T) -> V): Map<K, V> {
    val result = HashMap<K, V>()
    for (element in this) {
        result[keySelector(element)] = valueTransform(element)
    }
    return result
}

/**
 * Populates and returns the [destination] mutable map with key-value pairs,
 * where key is provided by the [keySelector] function applied to each element of the given collection
 * and value is the element itself.
 */
public inline fun <T, K, M : MutableMap<in K, in T>> Iterable<T>.associateByTo(destination: M, keySelector: (T) -> K): M {
    for (element in this) {
        destination[keySelector(element)] = element
    }
    return destination
}

/**
 * Populates and returns the [destination] mutable map with key-value pairs,
 * where key is provided by the [keySelector] function and
 * value is provided by the [valueTransform] function applied to elements of the given collection.
 */
public inline fun <T, K, V, M : MutableMap<in K, in V>> Iterable<T>.associateByTo(
    destination: M,
    keySelector: (T) -> K,
    valueTransform: (T) -> V
): M {
    for (element in this) {
        destination[keySelector(element)] = valueTransform(element)
    }
    return destination
}

/**
 * Returns a [Map] where keys are elements from the given collection and values are
 * produced by the [valueSelector] function applied to each element.
 */
public inline fun <K, V> Iterable<K>.associateWith(valueSelector: (K) -> V): Map<K, V> {
    val result = HashMap<K, V>()
    for (element in this) {
        result[element] = valueSelector(element)
    }
    return result
}

/**
 * Populates and returns the [destination] mutable map with key-value pairs for each element of the given collection,
 * where key is the element itself and value is provided by the [valueSelector] function applied to that key.
 */
public inline fun <K, V, M : MutableMap<in K, in V>> Iterable<K>.associateWithTo(destination: M, valueSelector: (K) -> V): M {
    for (element in this) {
        destination[element] = valueSelector(element)
    }
    return destination
}
