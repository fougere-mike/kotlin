/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.sequences

import kotlin.contracts.*

/**
 * Additional sequence operations for BrightScript stdlib.
 * This file contains aggregate, terminal, and utility operations.
 */

// ===== Element Access Operations =====

/**
 * Returns `true` if [element] is found in the sequence.
 * The operation is _terminal_.
 */
public operator fun <@kotlin.internal.OnlyInputTypes T> Sequence<T>.contains(element: T): Boolean {
    return indexOf(element) >= 0
}

/**
 * Returns an element at the given [index] or throws an [IndexOutOfBoundsException] if the [index] is out of bounds of this sequence.
 * The operation is _terminal_.
 */
public fun <T> Sequence<T>.elementAt(index: Int): T {
    return elementAtOrElse(index) { throw IndexOutOfBoundsException("Sequence doesn't contain element at index $index.") }
}

/**
 * Returns an element at the given [index] or the result of calling the [defaultValue] function if the [index] is out of bounds of this sequence.
 * The operation is _terminal_.
 */
public fun <T> Sequence<T>.elementAtOrElse(index: Int, defaultValue: (Int) -> T): T {
    contract {
        callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE)
    }
    if (index < 0)
        return defaultValue(index)
    val iterator = iterator()
    var count = 0
    while (iterator.hasNext()) {
        val element = iterator.next()
        if (index == count++)
            return element
    }
    return defaultValue(index)
}

/**
 * Returns an element at the given [index] or `null` if the [index] is out of bounds of this sequence.
 * The operation is _terminal_.
 */
public fun <T> Sequence<T>.elementAtOrNull(index: Int): T? {
    if (index < 0)
        return null
    val iterator = iterator()
    var count = 0
    while (iterator.hasNext()) {
        val element = iterator.next()
        if (index == count++)
            return element
    }
    return null
}

/**
 * Returns the first element matching the given [predicate], or `null` if no such element was found.
 * The operation is _terminal_.
 */
@kotlin.internal.InlineOnly
public inline fun <T> Sequence<T>.find(predicate: (T) -> Boolean): T? {
    return firstOrNull(predicate)
}

/**
 * Returns the last element matching the given [predicate], or `null` if no such element was found.
 * The operation is _terminal_.
 */
@kotlin.internal.InlineOnly
public inline fun <T> Sequence<T>.findLast(predicate: (T) -> Boolean): T? {
    return lastOrNull(predicate)
}

/**
 * Returns the first element.
 * @throws NoSuchElementException if the sequence is empty.
 */
public fun <T> Sequence<T>.first(): T {
    val iterator = iterator()
    if (!iterator.hasNext())
        throw NoSuchElementException("Sequence is empty.")
    return iterator.next()
}

/**
 * Returns the first element matching the given [predicate].
 * @throws NoSuchElementException if no such element is found.
 */
public inline fun <T> Sequence<T>.first(predicate: (T) -> Boolean): T {
    for (element in this) if (predicate(element)) return element
    throw NoSuchElementException("Sequence contains no element matching the predicate.")
}

/**
 * Returns the first element, or `null` if the sequence is empty.
 */
public fun <T> Sequence<T>.firstOrNull(): T? {
    val iterator = iterator()
    if (!iterator.hasNext())
        return null
    return iterator.next()
}

/**
 * Returns the first element matching the given [predicate], or `null` if element was not found.
 */
public inline fun <T> Sequence<T>.firstOrNull(predicate: (T) -> Boolean): T? {
    for (element in this) if (predicate(element)) return element
    return null
}

/**
 * Returns first non-null value produced by [transform] function being applied to elements of this sequence in iteration order,
 * or throws [NoSuchElementException] if no non-null value was produced.
 */
public inline fun <T, R : Any> Sequence<T>.firstNotNullOf(transform: (T) -> R?): R {
    return firstNotNullOfOrNull(transform) ?: throw NoSuchElementException("No element of the sequence was transformed to a non-null value.")
}

/**
 * Returns first non-null value produced by [transform] function being applied to elements of this sequence in iteration order,
 * or `null` if no non-null value was produced.
 */
public inline fun <T, R : Any> Sequence<T>.firstNotNullOfOrNull(transform: (T) -> R?): R? {
    for (element in this) {
        val result = transform(element)
        if (result != null) {
            return result
        }
    }
    return null
}

/**
 * Returns the zero-based index of the first occurrence of the specified [element] in the sequence, or -1 if not found.
 */
public fun <@kotlin.internal.OnlyInputTypes T> Sequence<T>.indexOf(element: T): Int {
    var index = 0
    for (item in this) {
        if (element == item)
            return index
        index++
    }
    return -1
}

/**
 * Returns index of the first element matching the given [predicate], or -1 if the sequence does not contain such element.
 */
public inline fun <T> Sequence<T>.indexOfFirst(predicate: (T) -> Boolean): Int {
    var index = 0
    for (item in this) {
        if (predicate(item))
            return index
        index++
    }
    return -1
}

/**
 * Returns index of the last element matching the given [predicate], or -1 if the sequence does not contain such element.
 */
public inline fun <T> Sequence<T>.indexOfLast(predicate: (T) -> Boolean): Int {
    var lastIndex = -1
    var index = 0
    for (item in this) {
        if (predicate(item))
            lastIndex = index
        index++
    }
    return lastIndex
}

/**
 * Returns the last element.
 * @throws NoSuchElementException if the sequence is empty.
 */
public fun <T> Sequence<T>.last(): T {
    val iterator = iterator()
    if (!iterator.hasNext())
        throw NoSuchElementException("Sequence is empty.")
    var last = iterator.next()
    while (iterator.hasNext())
        last = iterator.next()
    return last
}

/**
 * Returns the last element matching the given [predicate].
 * @throws NoSuchElementException if no such element is found.
 */
public inline fun <T> Sequence<T>.last(predicate: (T) -> Boolean): T {
    var last: T? = null
    var found = false
    for (element in this) {
        if (predicate(element)) {
            last = element
            found = true
        }
    }
    if (!found) throw NoSuchElementException("Sequence contains no element matching the predicate.")
    @Suppress("UNCHECKED_CAST")
    return last as T
}

/**
 * Returns the zero-based index of the last occurrence of the specified [element] in the sequence, or -1 if not found.
 */
public fun <@kotlin.internal.OnlyInputTypes T> Sequence<T>.lastIndexOf(element: T): Int {
    var lastIndex = -1
    var index = 0
    for (item in this) {
        if (element == item)
            lastIndex = index
        index++
    }
    return lastIndex
}

/**
 * Returns the last element, or `null` if the sequence is empty.
 */
public fun <T> Sequence<T>.lastOrNull(): T? {
    val iterator = iterator()
    if (!iterator.hasNext())
        return null
    var last = iterator.next()
    while (iterator.hasNext())
        last = iterator.next()
    return last
}

/**
 * Returns the last element matching the given [predicate], or `null` if no such element was found.
 */
public inline fun <T> Sequence<T>.lastOrNull(predicate: (T) -> Boolean): T? {
    var last: T? = null
    for (element in this) {
        if (predicate(element)) {
            last = element
        }
    }
    return last
}

/**
 * Returns the single element, or throws an exception if the sequence is empty or has more than one element.
 */
public fun <T> Sequence<T>.single(): T {
    val iterator = iterator()
    if (!iterator.hasNext())
        throw NoSuchElementException("Sequence is empty.")
    val single = iterator.next()
    if (iterator.hasNext())
        throw IllegalArgumentException("Sequence has more than one element.")
    return single
}

/**
 * Returns the single element matching the given [predicate], or throws exception if there is no or more than one matching element.
 */
public inline fun <T> Sequence<T>.single(predicate: (T) -> Boolean): T {
    var single: T? = null
    var found = false
    for (element in this) {
        if (predicate(element)) {
            if (found) throw IllegalArgumentException("Sequence contains more than one matching element.")
            single = element
            found = true
        }
    }
    if (!found) throw NoSuchElementException("Sequence contains no element matching the predicate.")
    @Suppress("UNCHECKED_CAST")
    return single as T
}

/**
 * Returns single element, or `null` if the sequence is empty or has more than one element.
 */
public fun <T> Sequence<T>.singleOrNull(): T? {
    val iterator = iterator()
    if (!iterator.hasNext())
        return null
    val single = iterator.next()
    if (iterator.hasNext())
        return null
    return single
}

/**
 * Returns the single element matching the given [predicate], or `null` if element was not found or more than one element was found.
 */
public inline fun <T> Sequence<T>.singleOrNull(predicate: (T) -> Boolean): T? {
    var single: T? = null
    var found = false
    for (element in this) {
        if (predicate(element)) {
            if (found) return null
            single = element
            found = true
        }
    }
    if (!found) return null
    return single
}

// ===== Aggregate Operations =====

/**
 * Returns `true` if all elements match the given [predicate].
 */
public inline fun <T> Sequence<T>.all(predicate: (T) -> Boolean): Boolean {
    contract {
        callsInPlace(predicate)
    }
    for (element in this) if (!predicate(element)) return false
    return true
}

/**
 * Returns `true` if sequence has at least one element.
 */
public fun <T> Sequence<T>.any(): Boolean {
    for (element in this) return true
    return false
}

/**
 * Returns `true` if at least one element matches the given [predicate].
 */
public inline fun <T> Sequence<T>.any(predicate: (T) -> Boolean): Boolean {
    contract {
        callsInPlace(predicate)
    }
    for (element in this) if (predicate(element)) return true
    return false
}

/**
 * Returns the number of elements in this sequence.
 */
public fun <T> Sequence<T>.count(): Int {
    var count = 0
    for (element in this) count++
    return count
}

/**
 * Returns the number of elements matching the given [predicate].
 */
public inline fun <T> Sequence<T>.count(predicate: (T) -> Boolean): Int {
    contract {
        callsInPlace(predicate)
    }
    var count = 0
    for (element in this) if (predicate(element)) count++
    return count
}

/**
 * Accumulates value starting with [initial] value and applying [operation] from left to right to current accumulator value and each element.
 */
public inline fun <T, R> Sequence<T>.fold(initial: R, operation: (acc: R, T) -> R): R {
    contract {
        callsInPlace(operation)
    }
    var accumulator = initial
    for (element in this) accumulator = operation(accumulator, element)
    return accumulator
}

/**
 * Accumulates value starting with [initial] value and applying [operation] from left to right
 * to current accumulator value and each element with its index in the original sequence.
 */
public inline fun <T, R> Sequence<T>.foldIndexed(initial: R, operation: (index: Int, acc: R, T) -> R): R {
    contract {
        callsInPlace(operation)
    }
    var index = 0
    var accumulator = initial
    for (element in this) accumulator = operation(index++, accumulator, element)
    return accumulator
}

/**
 * Performs the given [action] on each element.
 */
public inline fun <T> Sequence<T>.forEach(action: (T) -> Unit): Unit {
    contract {
        callsInPlace(action)
    }
    for (element in this) action(element)
}

/**
 * Performs the given [action] on each element, providing sequential index with the element.
 */
public inline fun <T> Sequence<T>.forEachIndexed(action: (index: Int, T) -> Unit): Unit {
    contract {
        callsInPlace(action)
    }
    var index = 0
    for (item in this) action(index++, item)
}

/**
 * Returns `true` if the sequence has no elements.
 */
public fun <T> Sequence<T>.none(): Boolean {
    for (element in this) return false
    return true
}

/**
 * Returns `true` if no elements match the given [predicate].
 */
public inline fun <T> Sequence<T>.none(predicate: (T) -> Boolean): Boolean {
    contract {
        callsInPlace(predicate)
    }
    for (element in this) if (predicate(element)) return false
    return true
}

/**
 * Accumulates value starting with the first element and applying [operation] from left to right to current accumulator value and each element.
 * @throws UnsupportedOperationException if the sequence is empty.
 */
public inline fun <S, T : S> Sequence<T>.reduce(operation: (acc: S, T) -> S): S {
    contract {
        callsInPlace(operation)
    }
    val iterator = this.iterator()
    if (!iterator.hasNext()) throw UnsupportedOperationException("Empty sequence can't be reduced.")
    var accumulator: S = iterator.next()
    while (iterator.hasNext()) {
        accumulator = operation(accumulator, iterator.next())
    }
    return accumulator
}

/**
 * Accumulates value starting with the first element and applying [operation] from left to right
 * to current accumulator value and each element with its index in the original sequence.
 * @throws UnsupportedOperationException if the sequence is empty.
 */
public inline fun <S, T : S> Sequence<T>.reduceIndexed(operation: (index: Int, acc: S, T) -> S): S {
    contract {
        callsInPlace(operation)
    }
    val iterator = this.iterator()
    if (!iterator.hasNext()) throw UnsupportedOperationException("Empty sequence can't be reduced.")
    var index = 1
    var accumulator: S = iterator.next()
    while (iterator.hasNext()) {
        accumulator = operation(index++, accumulator, iterator.next())
    }
    return accumulator
}

/**
 * Accumulates value starting with the first element and applying [operation] from left to right
 * to current accumulator value and each element with its index in the original sequence.
 * Returns `null` if the sequence is empty.
 */
public inline fun <S, T : S> Sequence<T>.reduceIndexedOrNull(operation: (index: Int, acc: S, T) -> S): S? {
    contract {
        callsInPlace(operation)
    }
    val iterator = this.iterator()
    if (!iterator.hasNext()) return null
    var index = 1
    var accumulator: S = iterator.next()
    while (iterator.hasNext()) {
        accumulator = operation(index++, accumulator, iterator.next())
    }
    return accumulator
}

/**
 * Accumulates value starting with the first element and applying [operation] from left to right to current accumulator value and each element.
 * Returns `null` if the sequence is empty.
 */
public inline fun <S, T : S> Sequence<T>.reduceOrNull(operation: (acc: S, T) -> S): S? {
    contract {
        callsInPlace(operation)
    }
    val iterator = this.iterator()
    if (!iterator.hasNext()) return null
    var accumulator: S = iterator.next()
    while (iterator.hasNext()) {
        accumulator = operation(accumulator, iterator.next())
    }
    return accumulator
}

/**
 * Returns a sequence containing successive accumulation values generated by applying [operation] from left to right
 * to each element and current accumulator value that starts with [initial] value.
 */
public fun <T, R> Sequence<T>.scan(initial: R, operation: (acc: R, T) -> R): Sequence<R> {
    return object : Sequence<R> {
        override fun iterator(): Iterator<R> = object : Iterator<R> {
            val iterator = this@scan.iterator()
            var accumulator = initial
            var firstEmitted = false

            override fun hasNext(): Boolean {
                return !firstEmitted || iterator.hasNext()
            }

            override fun next(): R {
                if (!firstEmitted) {
                    firstEmitted = true
                    return initial
                }
                accumulator = operation(accumulator, iterator.next())
                return accumulator
            }
        }
    }
}

/**
 * Returns a sequence containing successive accumulation values generated by applying [operation] from left to right
 * to each element, its index in the original sequence, and current accumulator value that starts with [initial] value.
 */
public fun <T, R> Sequence<T>.scanIndexed(initial: R, operation: (index: Int, acc: R, T) -> R): Sequence<R> {
    return object : Sequence<R> {
        override fun iterator(): Iterator<R> = object : Iterator<R> {
            val iterator = this@scanIndexed.iterator()
            var accumulator = initial
            var index = 0
            var firstEmitted = false

            override fun hasNext(): Boolean {
                return !firstEmitted || iterator.hasNext()
            }

            override fun next(): R {
                if (!firstEmitted) {
                    firstEmitted = true
                    return initial
                }
                accumulator = operation(index++, accumulator, iterator.next())
                return accumulator
            }
        }
    }
}

// ===== Sum Operations =====

/**
 * Returns the sum of all elements in the sequence.
 */
public fun Sequence<Byte>.sum(): Int {
    var sum: Int = 0
    for (element in this) {
        sum += element
    }
    return sum
}

/**
 * Returns the sum of all elements in the sequence.
 */
public fun Sequence<Short>.sum(): Int {
    var sum: Int = 0
    for (element in this) {
        sum += element
    }
    return sum
}

/**
 * Returns the sum of all elements in the sequence.
 */
public fun Sequence<Int>.sum(): Int {
    var sum: Int = 0
    for (element in this) {
        sum += element
    }
    return sum
}

/**
 * Returns the sum of all elements in the sequence.
 */
public fun Sequence<Long>.sum(): Long {
    var sum: Long = 0L
    for (element in this) {
        sum += element
    }
    return sum
}

/**
 * Returns the sum of all elements in the sequence.
 */
public fun Sequence<Float>.sum(): Float {
    var sum: Float = 0.0f
    for (element in this) {
        sum += element
    }
    return sum
}

/**
 * Returns the sum of all elements in the sequence.
 */
public fun Sequence<Double>.sum(): Double {
    var sum: Double = 0.0
    for (element in this) {
        sum += element
    }
    return sum
}

/**
 * Returns the sum of all values produced by [selector] function applied to each element in the sequence.
 */
@Deprecated("Use sumOf instead.", ReplaceWith("sumOf(selector)"))
@DeprecatedSinceKotlin(warningSince = "1.5")
public inline fun <T> Sequence<T>.sumBy(selector: (T) -> Int): Int {
    var sum: Int = 0
    for (element in this) {
        sum += selector(element)
    }
    return sum
}

/**
 * Returns the sum of all values produced by [selector] function applied to each element in the sequence.
 */
@Deprecated("Use sumOf instead.", ReplaceWith("sumOf(selector)"))
@DeprecatedSinceKotlin(warningSince = "1.5")
public inline fun <T> Sequence<T>.sumByDouble(selector: (T) -> Double): Double {
    var sum: Double = 0.0
    for (element in this) {
        sum += selector(element)
    }
    return sum
}

/**
 * Returns the sum of all values produced by [selector] function applied to each element in the sequence.
 */
public inline fun <T> Sequence<T>.sumOf(selector: (T) -> Int): Int {
    var sum: Int = 0
    for (element in this) {
        sum += selector(element)
    }
    return sum
}

/**
 * Returns the sum of all values produced by [selector] function applied to each element in the sequence.
 */
public inline fun <T> Sequence<T>.sumOf(selector: (T) -> Long): Long {
    var sum: Long = 0L
    for (element in this) {
        sum += selector(element)
    }
    return sum
}

/**
 * Returns the sum of all values produced by [selector] function applied to each element in the sequence.
 */
public inline fun <T> Sequence<T>.sumOf(selector: (T) -> Double): Double {
    var sum: Double = 0.0
    for (element in this) {
        sum += selector(element)
    }
    return sum
}

/**
 * Returns the sum of all values produced by [selector] function applied to each element in the sequence.
 */
public inline fun <T> Sequence<T>.sumOf(selector: (T) -> UInt): UInt {
    var sum: UInt = 0u
    for (element in this) {
        sum += selector(element)
    }
    return sum
}

/**
 * Returns the sum of all values produced by [selector] function applied to each element in the sequence.
 */
public inline fun <T> Sequence<T>.sumOf(selector: (T) -> ULong): ULong {
    var sum: ULong = 0u
    for (element in this) {
        sum += selector(element)
    }
    return sum
}

// ===== Min/Max Operations =====

/**
 * Returns the largest element or throws [NoSuchElementException] if there are no elements.
 */
@Deprecated("Use maxOrNull instead.", ReplaceWith("this.maxOrNull()"))
@DeprecatedSinceKotlin(warningSince = "1.4")
public fun <T : Comparable<T>> Sequence<T>.max(): T {
    return maxOrNull() ?: throw NoSuchElementException()
}

/**
 * Returns the largest element or `null` if there are no elements.
 */
public fun <T : Comparable<T>> Sequence<T>.maxOrNull(): T? {
    val iterator = iterator()
    if (!iterator.hasNext()) return null
    var max = iterator.next()
    while (iterator.hasNext()) {
        val e = iterator.next()
        if (brsCompareTo(max, e) < 0) max = e
    }
    return max
}

/**
 * Returns the smallest element or throws [NoSuchElementException] if there are no elements.
 */
@Deprecated("Use minOrNull instead.", ReplaceWith("this.minOrNull()"))
@DeprecatedSinceKotlin(warningSince = "1.4")
public fun <T : Comparable<T>> Sequence<T>.min(): T {
    return minOrNull() ?: throw NoSuchElementException()
}

/**
 * Returns the smallest element or `null` if there are no elements.
 */
public fun <T : Comparable<T>> Sequence<T>.minOrNull(): T? {
    val iterator = iterator()
    if (!iterator.hasNext()) return null
    var min = iterator.next()
    while (iterator.hasNext()) {
        val e = iterator.next()
        if (brsCompareTo(min, e) > 0) min = e
    }
    return min
}

// ===== Grouping Operations =====

/**
 * Groups elements of the original sequence by the key returned by the given [keySelector] function
 * applied to each element and returns a map where each group key is associated with a list of corresponding elements.
 */
public inline fun <T, K> Sequence<T>.groupBy(keySelector: (T) -> K): Map<K, List<T>> {
    return groupByTo(HashMap<K, MutableList<T>>(), keySelector)
}

/**
 * Groups values returned by the [valueTransform] function applied to each element of the original sequence
 * by the key returned by the given [keySelector] function applied to the element
 * and returns a map where each group key is associated with a list of corresponding values.
 */
public inline fun <T, K, V> Sequence<T>.groupBy(keySelector: (T) -> K, valueTransform: (T) -> V): Map<K, List<V>> {
    return groupByTo(HashMap<K, MutableList<V>>(), keySelector, valueTransform)
}

/**
 * Groups elements of the original sequence by the key returned by the given [keySelector] function
 * applied to each element and puts to the [destination] map each group key associated with a list of corresponding elements.
 */
public inline fun <T, K, M : MutableMap<in K, MutableList<T>>> Sequence<T>.groupByTo(destination: M, keySelector: (T) -> K): M {
    for (element in this) {
        val key = keySelector(element)
        val list = destination.getOrPut(key) { ArrayList<T>() }
        list.add(element)
    }
    return destination
}

/**
 * Groups values returned by the [valueTransform] function applied to each element of the original sequence
 * by the key returned by the given [keySelector] function applied to the element
 * and puts to the [destination] map each group key associated with a list of corresponding values.
 */
public inline fun <T, K, V, M : MutableMap<in K, MutableList<V>>> Sequence<T>.groupByTo(
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
 * Splits the original sequence into pair of lists,
 * where *first* list contains elements for which [predicate] yielded `true`,
 * while *second* list contains elements for which [predicate] yielded `false`.
 */
public inline fun <T> Sequence<T>.partition(predicate: (T) -> Boolean): Pair<List<T>, List<T>> {
    val first = ArrayList<T>()
    val second = ArrayList<T>()
    for (element in this) {
        if (predicate(element)) {
            first.add(element)
        } else {
            second.add(element)
        }
    }
    return Pair(first, second)
}

// ===== String Operations =====

/**
 * Appends the string from all the elements separated using [separator] and using the given [prefix] and [postfix] if supplied.
 *
 * If the collection could be huge, you can specify a non-negative value of [limit], in which case only the first [limit]
 * elements will be appended, followed by the [truncated] string (which defaults to "...").
 */
public fun <T, A : Appendable> Sequence<T>.joinTo(
    buffer: A,
    separator: CharSequence = ", ",
    prefix: CharSequence = "",
    postfix: CharSequence = "",
    limit: Int = -1,
    truncated: CharSequence = "...",
    transform: ((T) -> CharSequence)? = null
): A {
    buffer.append(prefix)
    var count = 0
    for (element in this) {
        if (++count > 1) buffer.append(separator)
        if (limit < 0 || count <= limit) {
            when {
                transform != null -> buffer.append(transform(element))
                element is CharSequence? -> buffer.append(element)
                element is Char -> buffer.append(element)
                else -> buffer.append(element.toString())
            }
        } else break
    }
    if (limit >= 0 && count > limit) buffer.append(truncated)
    buffer.append(postfix)
    return buffer
}

/**
 * Creates a string from all the elements separated using [separator] and using the given [prefix] and [postfix] if supplied.
 *
 * If the collection could be huge, you can specify a non-negative value of [limit], in which case only the first [limit]
 * elements will be appended, followed by the [truncated] string (which defaults to "...").
 */
public fun <T> Sequence<T>.joinToString(
    separator: CharSequence = ", ",
    prefix: CharSequence = "",
    postfix: CharSequence = "",
    limit: Int = -1,
    truncated: CharSequence = "...",
    transform: ((T) -> CharSequence)? = null
): String {
    return joinTo(StringBuilder(), separator, prefix, postfix, limit, truncated, transform).toString()
}

// ===== Utility Operations =====

/**
 * Performs the given [action] on each element and returns the sequence itself afterwards.
 */
public fun <T> Sequence<T>.onEach(action: (T) -> Unit): Sequence<T> {
    return map {
        action(it)
        it
    }
}

/**
 * Performs the given [action] on each element, providing sequential index with the element,
 * and returns the sequence itself afterwards.
 */
public fun <T> Sequence<T>.onEachIndexed(action: (index: Int, T) -> Unit): Sequence<T> {
    return mapIndexed { index, element ->
        action(index, element)
        element
    }
}

/**
 * Returns a sequence of [IndexedValue] for each element of the original sequence.
 */
public fun <T> Sequence<T>.withIndex(): Sequence<IndexedValue<T>> {
    return mapIndexed { index, element -> IndexedValue(index, element) }
}

/**
 * Returns a sequence that returns the results of applying [transform] to non-null values returned by [transform].
 */
public fun <T, R : Any> Sequence<T>.mapNotNull(transform: (T) -> R?): Sequence<R> {
    return TransformingSequence(FilteringSequence(map(transform), true, { it != null }), {
        @Suppress("UNCHECKED_CAST")
        it as R
    })
}

/**
 * Returns a sequence that returns the results of applying [transform] to non-null values in indexed manner.
 */
public fun <T, R : Any> Sequence<T>.mapIndexedNotNull(transform: (index: Int, T) -> R?): Sequence<R> {
    return TransformingSequence(FilteringSequence(mapIndexed(transform), true, { it != null }), {
        @Suppress("UNCHECKED_CAST")
        it as R
    })
}
