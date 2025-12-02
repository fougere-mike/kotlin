/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.collections

/**
 * An iterator over a collection or another entity that can be represented as a sequence of elements.
 * Allows to sequentially access the elements.
 */
public interface Iterator<out T> {
    /**
     * Returns `true` if the iteration has more elements.
     */
    public operator fun hasNext(): Boolean

    /**
     * Returns the next element in the iteration.
     * @throws NoSuchElementException if no more elements exist.
     */
    public operator fun next(): T
}

/**
 * An iterator over a mutable collection. Provides the ability to remove elements while iterating.
 */
public interface MutableIterator<out T> : Iterator<T> {
    /**
     * Removes from the underlying collection the last element returned by this iterator.
     */
    public fun remove()
}

/**
 * An iterator over a collection that supports indexed access.
 */
public interface ListIterator<out T> : Iterator<T> {
    /**
     * Returns `true` if there are elements in the iteration before the current element.
     */
    public fun hasPrevious(): Boolean

    /**
     * Returns the previous element in the iteration and moves the cursor position backwards.
     */
    public fun previous(): T

    /**
     * Returns the index of the element that would be returned by a subsequent call to [next].
     */
    public fun nextIndex(): Int

    /**
     * Returns the index of the element that would be returned by a subsequent call to [previous].
     */
    public fun previousIndex(): Int
}

/**
 * An iterator over a mutable collection that supports indexed access.
 * Provides the ability to add, modify and remove elements while iterating.
 */
public interface MutableListIterator<T> : ListIterator<T>, MutableIterator<T> {
    /**
     * Replaces the last element returned by [next] or [previous] with the specified element.
     */
    public fun set(element: T)

    /**
     * Adds the specified element into the underlying collection immediately before the element
     * that would be returned by [next], if any.
     */
    public fun add(element: T)
}
