/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.collections

/**
 * An iterator over a collection or another entity that can be represented as a sequence of elements.
 * Allows to sequentially access the elements.
 */
public actual interface Iterator<out T> {
    /**
     * Returns the next element in the iteration.
     *
     * @throws NoSuchElementException if the iteration has no next element.
     */
    public actual operator fun next(): T

    /**
     * Returns `true` if the iteration has more elements.
     */
    public actual operator fun hasNext(): Boolean
}

/**
 * An iterator over a mutable collection. Provides the ability to remove elements while iterating.
 */
public actual interface MutableIterator<out T> : Iterator<T> {
    /**
     * Removes from the underlying collection the last element returned by this iterator.
     */
    public actual fun remove(): Unit
}

/**
 * An iterator over a collection that supports indexed access.
 */
public actual interface ListIterator<out T> : Iterator<T> {
    override actual fun next(): T
    override actual fun hasNext(): Boolean

    /**
     * Returns `true` if there are elements in the iteration before the current element.
     */
    public actual fun hasPrevious(): Boolean

    /**
     * Returns the previous element in the iteration and moves the cursor position backwards.
     */
    public actual fun previous(): T

    /**
     * Returns the index of the element that would be returned by a subsequent call to [next].
     */
    public actual fun nextIndex(): Int

    /**
     * Returns the index of the element that would be returned by a subsequent call to [previous].
     */
    public actual fun previousIndex(): Int
}

/**
 * An iterator over a mutable collection that supports indexed access.
 * Provides the ability to add, modify and remove elements while iterating.
 */
public actual interface MutableListIterator<T> : ListIterator<T>, MutableIterator<T> {
    override actual fun next(): T
    override actual fun hasNext(): Boolean
    override actual fun remove(): Unit

    /**
     * Replaces the last element returned by [next] or [previous] with the specified element.
     */
    public actual fun set(element: T): Unit

    /**
     * Adds the specified element into the underlying collection immediately before the element
     * that would be returned by [next], if any.
     */
    public actual fun add(element: T): Unit
}
