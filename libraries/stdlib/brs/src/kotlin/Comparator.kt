/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin

/**
 * Provides a comparison function for imposing a total ordering between instances of the type [T].
 */
public fun interface Comparator<T> {
    /**
     * Compares its two arguments for order. Returns zero if the arguments are equal,
     * a negative number if the first argument is less than the second,
     * or a positive number if the first argument is greater than the second.
     */
    public fun compare(a: T, b: T): Int
}

/**
 * Creates a comparator using the function to transform value to a [Comparable] instance for comparison.
 */
public inline fun <T> compareBy(crossinline selector: (T) -> Comparable<*>?): Comparator<T> =
    Comparator { a, b -> compareValuesBy(a, b, selector) }

/**
 * Compares two values using the specified functions [selector] to calculate a result of comparison.
 * The function returns zero if the values are equal, a negative number if [a] is less than [b],
 * or a positive number if [a] is greater than [b].
 */
public inline fun <T> compareValuesBy(a: T, b: T, selector: (T) -> Comparable<*>?): Int {
    return compareValues(selector(a), selector(b))
}

/**
 * Compares two nullable [Comparable] values. Null is considered less than any value.
 */
public fun <T : Comparable<*>> compareValues(a: T?, b: T?): Int {
    if (a === b) return 0
    if (a == null) return -1
    if (b == null) return 1

    @Suppress("UNCHECKED_CAST")
    return (a as Comparable<Any>).compareTo(b)
}

/**
 * Returns a comparator that compares [Comparable] objects in natural order.
 */
public fun <T : Comparable<T>> naturalOrder(): Comparator<T> = @Suppress("UNCHECKED_CAST") (NaturalOrderComparator as Comparator<T>)

/**
 * Returns a comparator that compares [Comparable] objects in reversed natural order.
 */
public fun <T : Comparable<T>> reverseOrder(): Comparator<T> = @Suppress("UNCHECKED_CAST") (ReverseOrderComparator as Comparator<T>)

/**
 * Returns a comparator that imposes the reverse ordering of this comparator.
 */
public fun <T> Comparator<T>.reversed(): Comparator<T> = Comparator { a, b -> compare(b, a) }

private object NaturalOrderComparator : Comparator<Comparable<Any>> {
    override fun compare(a: Comparable<Any>, b: Comparable<Any>): Int = a.compareTo(b)
}

private object ReverseOrderComparator : Comparator<Comparable<Any>> {
    override fun compare(a: Comparable<Any>, b: Comparable<Any>): Int = b.compareTo(a)
}
