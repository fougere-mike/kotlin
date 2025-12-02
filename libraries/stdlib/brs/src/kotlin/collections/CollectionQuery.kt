/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.collections

/**
 * Returns `true` if the collection is not empty.
 */
@kotlin.internal.InlineOnly
public inline fun <T> Collection<T>.isNotEmpty(): Boolean = !isEmpty()

/**
 * Returns the first element.
 * @throws NoSuchElementException if the collection is empty.
 */
public fun <T> Iterable<T>.first(): T {
    when (this) {
        is List -> return this[0]
        else -> {
            val iterator = iterator()
            if (!iterator.hasNext())
                throw NoSuchElementException("Collection is empty.")
            return iterator.next()
        }
    }
}

/**
 * Returns the first element, or `null` if the collection is empty.
 */
public fun <T> Iterable<T>.firstOrNull(): T? {
    when (this) {
        is List -> {
            if (isEmpty()) return null
            return this[0]
        }
        else -> {
            val iterator = iterator()
            if (!iterator.hasNext())
                return null
            return iterator.next()
        }
    }
}

/**
 * Returns the last element.
 * @throws NoSuchElementException if the collection is empty.
 */
public fun <T> List<T>.last(): T {
    if (isEmpty())
        throw NoSuchElementException("List is empty.")
    return this[size - 1]
}

/**
 * Returns the last element, or `null` if the list is empty.
 */
public fun <T> List<T>.lastOrNull(): T? {
    return if (isEmpty()) null else this[size - 1]
}
