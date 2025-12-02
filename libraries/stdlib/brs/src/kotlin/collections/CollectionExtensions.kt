/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.collections

// ==================== forEach ====================

/**
 * Performs the given [action] on each element.
 */
public inline fun <T> Iterable<T>.forEach(action: (T) -> Unit) {
    for (element in this) action(element)
}

/**
 * Performs the given [action] on each element, providing sequential index with the element.
 */
public inline fun <T> Iterable<T>.forEachIndexed(action: (index: Int, T) -> Unit) {
    var index = 0
    for (item in this) action(index++, item)
}

// ==================== map ====================

/**
 * Returns a list containing the results of applying the given [transform] function
 * to each element in the original collection.
 */
public inline fun <T, R> Iterable<T>.map(transform: (T) -> R): List<R> {
    val result = ArrayList<R>()
    for (item in this) {
        result.add(transform(item))
    }
    return result
}

/**
 * Returns a list containing the results of applying the given [transform] function
 * to each element and its index in the original collection.
 */
public inline fun <T, R> Iterable<T>.mapIndexed(transform: (index: Int, T) -> R): List<R> {
    val result = ArrayList<R>()
    var index = 0
    for (item in this) {
        result.add(transform(index++, item))
    }
    return result
}

/**
 * Returns a list containing only the non-null results of applying the given [transform] function
 * to each element in the original collection.
 */
public inline fun <T, R : Any> Iterable<T>.mapNotNull(transform: (T) -> R?): List<R> {
    val result = ArrayList<R>()
    for (item in this) {
        val transformed = transform(item)
        if (transformed != null) {
            result.add(transformed)
        }
    }
    return result
}

// ==================== filter ====================

/**
 * Returns a list containing only elements matching the given [predicate].
 */
public inline fun <T> Iterable<T>.filter(predicate: (T) -> Boolean): List<T> {
    val result = ArrayList<T>()
    for (item in this) {
        if (predicate(item)) result.add(item)
    }
    return result
}

/**
 * Returns a list containing only elements matching the given [predicate].
 */
public inline fun <T> Iterable<T>.filterIndexed(predicate: (index: Int, T) -> Boolean): List<T> {
    val result = ArrayList<T>()
    var index = 0
    for (item in this) {
        if (predicate(index++, item)) result.add(item)
    }
    return result
}

/**
 * Returns a list containing all elements that are not null.
 */
public fun <T : Any> Iterable<T?>.filterNotNull(): List<T> {
    val result = ArrayList<T>()
    for (item in this) {
        if (item != null) result.add(item)
    }
    return result
}

/**
 * Returns a list containing all elements not matching the given [predicate].
 */
public inline fun <T> Iterable<T>.filterNot(predicate: (T) -> Boolean): List<T> {
    val result = ArrayList<T>()
    for (item in this) {
        if (!predicate(item)) result.add(item)
    }
    return result
}

// ==================== find ====================

/**
 * Returns the first element matching the given [predicate], or `null` if no such element was found.
 */
public inline fun <T> Iterable<T>.find(predicate: (T) -> Boolean): T? {
    for (element in this) if (predicate(element)) return element
    return null
}

/**
 * Returns the last element matching the given [predicate], or `null` if no such element was found.
 */
public inline fun <T> Iterable<T>.findLast(predicate: (T) -> Boolean): T? {
    var last: T? = null
    for (element in this) {
        if (predicate(element)) {
            last = element
        }
    }
    return last
}

// ==================== first / last ====================

/**
 * Returns the first element.
 * @throws NoSuchElementException if the collection is empty.
 */
public fun <T> Iterable<T>.first(): T {
    when (this) {
        is List -> {
            if (isEmpty()) throw NoSuchElementException("List is empty.")
            return this[0]
        }
        else -> {
            val iterator = iterator()
            if (!iterator.hasNext()) throw NoSuchElementException("Collection is empty.")
            return iterator.next()
        }
    }
}

/**
 * Returns the first element matching the given [predicate].
 * @throws NoSuchElementException if no such element is found.
 */
public inline fun <T> Iterable<T>.first(predicate: (T) -> Boolean): T {
    for (element in this) if (predicate(element)) return element
    throw NoSuchElementException("Collection contains no element matching the predicate.")
}

/**
 * Returns the first element, or `null` if the collection is empty.
 */
public fun <T> Iterable<T>.firstOrNull(): T? {
    when (this) {
        is List -> return if (isEmpty()) null else this[0]
        else -> {
            val iterator = iterator()
            if (!iterator.hasNext()) return null
            return iterator.next()
        }
    }
}

/**
 * Returns the first element matching the given [predicate], or `null` if no such element was found.
 */
public inline fun <T> Iterable<T>.firstOrNull(predicate: (T) -> Boolean): T? {
    for (element in this) if (predicate(element)) return element
    return null
}

/**
 * Returns the last element.
 * @throws NoSuchElementException if the collection is empty.
 */
public fun <T> Iterable<T>.last(): T {
    when (this) {
        is List -> {
            if (isEmpty()) throw NoSuchElementException("List is empty.")
            return this[size - 1]
        }
        else -> {
            val iterator = iterator()
            if (!iterator.hasNext()) throw NoSuchElementException("Collection is empty.")
            var last = iterator.next()
            while (iterator.hasNext()) last = iterator.next()
            return last
        }
    }
}

/**
 * Returns the last element matching the given [predicate].
 * @throws NoSuchElementException if no such element is found.
 */
public inline fun <T> Iterable<T>.last(predicate: (T) -> Boolean): T {
    var last: T? = null
    var found = false
    for (element in this) {
        if (predicate(element)) {
            last = element
            found = true
        }
    }
    if (!found) throw NoSuchElementException("Collection contains no element matching the predicate.")
    @Suppress("UNCHECKED_CAST")
    return last as T
}

/**
 * Returns the last element, or `null` if the collection is empty.
 */
public fun <T> Iterable<T>.lastOrNull(): T? {
    when (this) {
        is List -> return if (isEmpty()) null else this[size - 1]
        else -> {
            val iterator = iterator()
            if (!iterator.hasNext()) return null
            var last = iterator.next()
            while (iterator.hasNext()) last = iterator.next()
            return last
        }
    }
}

/**
 * Returns the last element matching the given [predicate], or `null` if no such element was found.
 */
public inline fun <T> Iterable<T>.lastOrNull(predicate: (T) -> Boolean): T? {
    var last: T? = null
    for (element in this) {
        if (predicate(element)) {
            last = element
        }
    }
    return last
}

// ==================== any / all / none ====================

/**
 * Returns `true` if collection has at least one element.
 */
public fun <T> Iterable<T>.any(): Boolean {
    if (this is Collection) return !isEmpty()
    return iterator().hasNext()
}

/**
 * Returns `true` if at least one element matches the given [predicate].
 */
public inline fun <T> Iterable<T>.any(predicate: (T) -> Boolean): Boolean {
    if (this is Collection && isEmpty()) return false
    for (element in this) if (predicate(element)) return true
    return false
}

/**
 * Returns `true` if all elements match the given [predicate].
 */
public inline fun <T> Iterable<T>.all(predicate: (T) -> Boolean): Boolean {
    if (this is Collection && isEmpty()) return true
    for (element in this) if (!predicate(element)) return false
    return true
}

/**
 * Returns `true` if collection has no elements.
 */
public fun <T> Iterable<T>.none(): Boolean {
    if (this is Collection) return isEmpty()
    return !iterator().hasNext()
}

/**
 * Returns `true` if no elements match the given [predicate].
 */
public inline fun <T> Iterable<T>.none(predicate: (T) -> Boolean): Boolean {
    if (this is Collection && isEmpty()) return true
    for (element in this) if (predicate(element)) return false
    return true
}

// ==================== count ====================

/**
 * Returns the number of elements in this collection.
 */
public fun <T> Iterable<T>.count(): Int {
    if (this is Collection) return size
    var count = 0
    for (element in this) count++
    return count
}

/**
 * Returns the number of elements matching the given [predicate].
 */
public inline fun <T> Iterable<T>.count(predicate: (T) -> Boolean): Int {
    if (this is Collection && isEmpty()) return 0
    var count = 0
    for (element in this) if (predicate(element)) count++
    return count
}

// ==================== fold / reduce ====================

/**
 * Accumulates value starting with [initial] value and applying [operation] from left to right
 * to current accumulator value and each element.
 */
public inline fun <T, R> Iterable<T>.fold(initial: R, operation: (acc: R, T) -> R): R {
    var accumulator = initial
    for (element in this) accumulator = operation(accumulator, element)
    return accumulator
}

/**
 * Accumulates value starting with [initial] value and applying [operation] from left to right
 * to current accumulator value and each element with its index in the original collection.
 */
public inline fun <T, R> Iterable<T>.foldIndexed(initial: R, operation: (index: Int, acc: R, T) -> R): R {
    var index = 0
    var accumulator = initial
    for (element in this) accumulator = operation(index++, accumulator, element)
    return accumulator
}

/**
 * Accumulates value starting with the first element and applying [operation] from left to right
 * to current accumulator value and each element.
 * @throws UnsupportedOperationException if this collection is empty.
 */
public inline fun <S, T : S> Iterable<T>.reduce(operation: (acc: S, T) -> S): S {
    val iterator = this.iterator()
    if (!iterator.hasNext()) throw UnsupportedOperationException("Empty collection can't be reduced.")
    var accumulator: S = iterator.next()
    while (iterator.hasNext()) {
        accumulator = operation(accumulator, iterator.next())
    }
    return accumulator
}

/**
 * Accumulates value starting with the first element and applying [operation] from left to right
 * to current accumulator value and each element with its index in the original collection.
 * @throws UnsupportedOperationException if this collection is empty.
 */
public inline fun <S, T : S> Iterable<T>.reduceIndexed(operation: (index: Int, acc: S, T) -> S): S {
    val iterator = this.iterator()
    if (!iterator.hasNext()) throw UnsupportedOperationException("Empty collection can't be reduced.")
    var index = 1
    var accumulator: S = iterator.next()
    while (iterator.hasNext()) {
        accumulator = operation(index++, accumulator, iterator.next())
    }
    return accumulator
}

/**
 * Accumulates value starting with the first element and applying [operation] from left to right
 * to current accumulator value and each element. Returns `null` if the collection is empty.
 */
public inline fun <S, T : S> Iterable<T>.reduceOrNull(operation: (acc: S, T) -> S): S? {
    val iterator = this.iterator()
    if (!iterator.hasNext()) return null
    var accumulator: S = iterator.next()
    while (iterator.hasNext()) {
        accumulator = operation(accumulator, iterator.next())
    }
    return accumulator
}

// ==================== sum ====================

/**
 * Returns the sum of all elements in the collection.
 */
public fun Iterable<Int>.sum(): Int {
    var sum = 0
    for (element in this) sum += element
    return sum
}

/**
 * Returns the sum of all elements in the collection.
 */
public fun Iterable<Long>.sum(): Long {
    var sum = 0L
    for (element in this) sum += element
    return sum
}

/**
 * Returns the sum of all elements in the collection.
 */
public fun Iterable<Float>.sum(): Float {
    var sum = 0.0f
    for (element in this) sum += element
    return sum
}

/**
 * Returns the sum of all elements in the collection.
 */
public fun Iterable<Double>.sum(): Double {
    var sum = 0.0
    for (element in this) sum += element
    return sum
}

/**
 * Returns the sum of all values produced by [selector] function applied to each element.
 */
public inline fun <T> Iterable<T>.sumOf(selector: (T) -> Int): Int {
    var sum = 0
    for (element in this) sum += selector(element)
    return sum
}

/**
 * Returns the sum of all values produced by [selector] function applied to each element.
 */
public inline fun <T> Iterable<T>.sumOfDouble(selector: (T) -> Double): Double {
    var sum = 0.0
    for (element in this) sum += selector(element)
    return sum
}

// ==================== take / drop ====================

/**
 * Returns a list containing first [n] elements.
 */
public fun <T> Iterable<T>.take(n: Int): List<T> {
    require(n >= 0) { "Requested element count $n is less than zero." }
    if (n == 0) return emptyList()
    if (this is Collection) {
        if (n >= size) return toList()
    }
    var count = 0
    val list = ArrayList<T>()
    for (item in this) {
        list.add(item)
        if (++count == n) break
    }
    return list
}

/**
 * Returns a list containing all elements except first [n] elements.
 */
public fun <T> Iterable<T>.drop(n: Int): List<T> {
    require(n >= 0) { "Requested element count $n is less than zero." }
    if (n == 0) return toList()
    val list = ArrayList<T>()
    var count = 0
    for (item in this) {
        if (count >= n) {
            list.add(item)
        }
        count++
    }
    return list
}

/**
 * Returns a list containing first elements satisfying the given [predicate].
 */
public inline fun <T> Iterable<T>.takeWhile(predicate: (T) -> Boolean): List<T> {
    val list = ArrayList<T>()
    for (item in this) {
        if (!predicate(item)) break
        list.add(item)
    }
    return list
}

/**
 * Returns a list containing all elements except first elements that satisfy the given [predicate].
 */
public inline fun <T> Iterable<T>.dropWhile(predicate: (T) -> Boolean): List<T> {
    var yielding = false
    val list = ArrayList<T>()
    for (item in this) {
        if (yielding) {
            list.add(item)
        } else if (!predicate(item)) {
            yielding = true
            list.add(item)
        }
    }
    return list
}

// ==================== distinct ====================

/**
 * Returns a list containing only distinct elements from the given collection.
 */
public fun <T> Iterable<T>.distinct(): List<T> {
    return toMutableSet().toList()
}

/**
 * Returns a list containing only elements from the given collection
 * having distinct keys returned by the given [selector] function.
 */
public inline fun <T, K> Iterable<T>.distinctBy(selector: (T) -> K): List<T> {
    val set = HashSet<K>()
    val list = ArrayList<T>()
    for (e in this) {
        val key = selector(e)
        if (set.add(key)) {
            list.add(e)
        }
    }
    return list
}

// ==================== flatMap ====================

/**
 * Returns a single list of all elements yielded from results of [transform] function
 * being invoked on each element of original collection.
 */
public inline fun <T, R> Iterable<T>.flatMap(transform: (T) -> Iterable<R>): List<R> {
    val result = ArrayList<R>()
    for (element in this) {
        val list = transform(element)
        result.addAll(list.toList())
    }
    return result
}

/**
 * Returns a single list of all elements from all collections in the given collection.
 */
public fun <T> Iterable<Iterable<T>>.flatten(): List<T> {
    val result = ArrayList<T>()
    for (element in this) {
        result.addAll(element.toList())
    }
    return result
}

// ==================== toList / toSet / toMutableList / toMutableSet ====================

/**
 * Returns a [List] containing all elements.
 */
public fun <T> Iterable<T>.toList(): List<T> {
    if (this is Collection) {
        return when (size) {
            0 -> emptyList()
            1 -> listOf(if (this is List) get(0) else iterator().next())
            else -> ArrayList(this)
        }
    }
    return toMutableList()
}

/**
 * Returns a [MutableList] filled with all elements of this collection.
 */
public fun <T> Iterable<T>.toMutableList(): MutableList<T> {
    if (this is Collection) return ArrayList(this)
    val result = ArrayList<T>()
    for (element in this) {
        result.add(element)
    }
    return result
}

/**
 * Returns a [Set] of all elements.
 */
public fun <T> Iterable<T>.toSet(): Set<T> {
    if (this is Collection) {
        return when (size) {
            0 -> emptySet()
            else -> toMutableSet()
        }
    }
    return toMutableSet()
}

/**
 * Returns a [MutableSet] of all elements.
 */
public fun <T> Iterable<T>.toMutableSet(): MutableSet<T> {
    val set = HashSet<T>()
    for (item in this) set.add(item)
    return set
}

// ==================== joinToString ====================

/**
 * Creates a string from all the elements separated using [separator] and using the given [prefix] and [postfix] if supplied.
 */
public fun <T> Iterable<T>.joinToString(
    separator: CharSequence = ", ",
    prefix: CharSequence = "",
    postfix: CharSequence = "",
    limit: Int = -1,
    truncated: CharSequence = "..."
): String {
    val sb = StringBuilder()
    sb.append(prefix)
    var count = 0
    for (element in this) {
        if (++count > 1) sb.append(separator)
        if (limit < 0 || count <= limit) {
            sb.append(element.toString())
        } else break
    }
    if (limit >= 0 && count > limit) sb.append(truncated)
    sb.append(postfix)
    return sb.toString()
}

/**
 * Creates a string from all the elements separated using [separator] and using the given [prefix] and [postfix] if supplied.
 * If the collection could be huge, you can specify a non-negative value of [limit], in which case only the first [limit]
 * elements will be appended, followed by the [truncated] string (which defaults to "...").
 */
public inline fun <T> Iterable<T>.joinToString(
    separator: CharSequence = ", ",
    prefix: CharSequence = "",
    postfix: CharSequence = "",
    limit: Int = -1,
    truncated: CharSequence = "...",
    transform: (T) -> CharSequence
): String {
    val sb = StringBuilder()
    sb.append(prefix)
    var count = 0
    for (element in this) {
        if (++count > 1) sb.append(separator)
        if (limit < 0 || count <= limit) {
            sb.append(transform(element))
        } else break
    }
    if (limit >= 0 && count > limit) sb.append(truncated)
    sb.append(postfix)
    return sb.toString()
}

// ==================== contains / indexOf ====================

/**
 * Returns `true` if [element] is found in the collection.
 */
public operator fun <T> Iterable<T>.contains(element: T): Boolean {
    if (this is Collection) return contains(element)
    return indexOf(element) >= 0
}

/**
 * Returns first index of [element], or -1 if the collection does not contain element.
 */
public fun <T> Iterable<T>.indexOf(element: T): Int {
    if (this is List) return this.indexOf(element)
    var index = 0
    for (item in this) {
        if (element == item) return index
        index++
    }
    return -1
}

/**
 * Returns index of the last element matching the given [predicate], or -1 if the collection does not contain such element.
 */
public inline fun <T> Iterable<T>.indexOfFirst(predicate: (T) -> Boolean): Int {
    var index = 0
    for (item in this) {
        if (predicate(item)) return index
        index++
    }
    return -1
}

/**
 * Returns index of the last element matching the given [predicate], or -1 if the collection does not contain such element.
 */
public inline fun <T> Iterable<T>.indexOfLast(predicate: (T) -> Boolean): Int {
    var lastIndex = -1
    var index = 0
    for (item in this) {
        if (predicate(item)) lastIndex = index
        index++
    }
    return lastIndex
}

// ==================== reversed / sorted ====================

/**
 * Returns a list with elements in reversed order.
 */
public fun <T> Iterable<T>.reversed(): List<T> {
    if (this is Collection && size <= 1) return toList()
    val list = toMutableList()
    val n = list.size
    var i = 0
    while (i < n / 2) {
        val temp = list[i]
        list[i] = list[n - 1 - i]
        list[n - 1 - i] = temp
        i++
    }
    return list
}

// ==================== single ====================

/**
 * Returns the single element, or throws an exception if the collection is empty or has more than one element.
 */
public fun <T> Iterable<T>.single(): T {
    when (this) {
        is List -> {
            return when (size) {
                0 -> throw NoSuchElementException("List is empty.")
                1 -> this[0]
                else -> throw IllegalArgumentException("List has more than one element.")
            }
        }
        else -> {
            val iterator = iterator()
            if (!iterator.hasNext()) throw NoSuchElementException("Collection is empty.")
            val single = iterator.next()
            if (iterator.hasNext()) throw IllegalArgumentException("Collection has more than one element.")
            return single
        }
    }
}

/**
 * Returns the single element matching the given [predicate], or throws exception if there is no or more than one matching element.
 */
public inline fun <T> Iterable<T>.single(predicate: (T) -> Boolean): T {
    var single: T? = null
    var found = false
    for (element in this) {
        if (predicate(element)) {
            if (found) throw IllegalArgumentException("Collection contains more than one matching element.")
            single = element
            found = true
        }
    }
    if (!found) throw NoSuchElementException("Collection contains no element matching the predicate.")
    @Suppress("UNCHECKED_CAST")
    return single as T
}

/**
 * Returns single element, or `null` if the collection is empty or has more than one element.
 */
public fun <T> Iterable<T>.singleOrNull(): T? {
    when (this) {
        is List -> {
            return if (size == 1) this[0] else null
        }
        else -> {
            val iterator = iterator()
            if (!iterator.hasNext()) return null
            val single = iterator.next()
            if (iterator.hasNext()) return null
            return single
        }
    }
}

/**
 * Returns the single element matching the given [predicate], or `null` if element was not found or more than one element was found.
 */
public inline fun <T> Iterable<T>.singleOrNull(predicate: (T) -> Boolean): T? {
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

// ==================== associate ====================

/**
 * Returns a [Map] containing key-value pairs provided by [transform] function applied to elements of the given collection.
 */
public inline fun <T, K, V> Iterable<T>.associate(transform: (T) -> Pair<K, V>): Map<K, V> {
    val result = HashMap<K, V>()
    for (element in this) {
        val pair = transform(element)
        result.put(pair.first, pair.second)
    }
    return result
}

/**
 * Returns a [Map] containing the elements from the given collection indexed by the key returned from [keySelector] function.
 */
public inline fun <T, K> Iterable<T>.associateBy(keySelector: (T) -> K): Map<K, T> {
    val result = HashMap<K, T>()
    for (element in this) {
        result.put(keySelector(element), element)
    }
    return result
}

/**
 * Returns a [Map] containing the values provided by [valueTransform] and indexed by [keySelector] functions applied to elements of the given collection.
 */
public inline fun <T, K, V> Iterable<T>.associateBy(keySelector: (T) -> K, valueTransform: (T) -> V): Map<K, V> {
    val result = HashMap<K, V>()
    for (element in this) {
        result.put(keySelector(element), valueTransform(element))
    }
    return result
}

/**
 * Returns a [Map] where keys are elements from the given collection and values are produced by the [valueSelector] function.
 */
public inline fun <K, V> Iterable<K>.associateWith(valueSelector: (K) -> V): Map<K, V> {
    val result = HashMap<K, V>()
    for (element in this) {
        result.put(element, valueSelector(element))
    }
    return result
}

// ==================== groupBy ====================

/**
 * Groups elements of the original collection by the key returned by the given [keySelector] function
 * and returns a map where each group key is associated with a list of corresponding elements.
 */
public inline fun <T, K> Iterable<T>.groupBy(keySelector: (T) -> K): Map<K, List<T>> {
    val result = HashMap<K, MutableList<T>>()
    for (element in this) {
        val key = keySelector(element)
        val list = result[key]
        if (list == null) {
            result.put(key, mutableListOf(element))
        } else {
            list.add(element)
        }
    }
    return result
}

/**
 * Groups values returned by the [valueTransform] function applied to each element of the original collection
 * by the key returned by the given [keySelector] function and returns a map where each group key is associated with a list of corresponding values.
 */
public inline fun <T, K, V> Iterable<T>.groupBy(keySelector: (T) -> K, valueTransform: (T) -> V): Map<K, List<V>> {
    val result = HashMap<K, MutableList<V>>()
    for (element in this) {
        val key = keySelector(element)
        val list = result[key]
        if (list == null) {
            result.put(key, mutableListOf(valueTransform(element)))
        } else {
            list.add(valueTransform(element))
        }
    }
    return result
}

// ==================== partition ====================

/**
 * Splits the original collection into pair of lists,
 * where first list contains elements for which [predicate] yielded `true`,
 * while second list contains elements for which [predicate] yielded `false`.
 */
public inline fun <T> Iterable<T>.partition(predicate: (T) -> Boolean): Pair<List<T>, List<T>> {
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

// ==================== zip ====================

/**
 * Returns a list of pairs built from the elements of this collection and the [other] collection with the same index.
 */
public infix fun <T, R> Iterable<T>.zip(other: Iterable<R>): List<Pair<T, R>> {
    val first = iterator()
    val second = other.iterator()
    val result = ArrayList<Pair<T, R>>()
    while (first.hasNext() && second.hasNext()) {
        result.add(Pair(first.next(), second.next()))
    }
    return result
}

/**
 * Returns a list of values built from the elements of this collection and the [other] collection with the same index
 * using the provided [transform] function applied to each pair of elements.
 */
public inline fun <T, R, V> Iterable<T>.zip(other: Iterable<R>, transform: (a: T, b: R) -> V): List<V> {
    val first = iterator()
    val second = other.iterator()
    val result = ArrayList<V>()
    while (first.hasNext() && second.hasNext()) {
        result.add(transform(first.next(), second.next()))
    }
    return result
}

// ==================== plus / minus ====================

/**
 * Returns a list containing all elements of the original collection and then the given [element].
 */
public operator fun <T> Collection<T>.plus(element: T): List<T> {
    val result = ArrayList<T>(size + 1)
    result.addAll(this)
    result.add(element)
    return result
}

/**
 * Returns a list containing all elements of the original collection and then all elements of the given [elements] collection.
 */
public operator fun <T> Collection<T>.plus(elements: Iterable<T>): List<T> {
    val result = ArrayList<T>()
    result.addAll(this)
    result.addAll(elements.toList())
    return result
}

/**
 * Returns a list containing all elements of the original collection except the given [element].
 */
public operator fun <T> Iterable<T>.minus(element: T): List<T> {
    val result = ArrayList<T>()
    var removed = false
    for (item in this) {
        if (!removed && item == element) {
            removed = true
        } else {
            result.add(item)
        }
    }
    return result
}

/**
 * Returns a list containing all elements of the original collection except the elements in the given [elements] collection.
 */
public operator fun <T> Iterable<T>.minus(elements: Iterable<T>): List<T> {
    val other = elements.toSet()
    return filterNot { it in other }
}
