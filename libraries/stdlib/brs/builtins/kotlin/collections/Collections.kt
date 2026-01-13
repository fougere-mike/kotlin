/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.collections

/**
 * Classes that inherit from this interface can be represented as a sequence of elements that can
 * be iterated over.
 * @param T the type of element being iterated over. The iterator is covariant in its element type.
 */
public actual interface Iterable<out T> {
    /**
     * Returns an iterator over the elements of this object.
     */
    public actual operator fun iterator(): Iterator<T>
}

/**
 * Classes that inherit from this interface can be represented as a sequence of elements that can
 * be iterated over and that supports removing elements during iteration.
 * @param T the type of element being iterated over. The mutable iterator is invariant in its element type.
 */
public actual interface MutableIterable<out T> : Iterable<T> {
    /**
     * Returns an iterator over the elements of this sequence that supports removing elements during iteration.
     */
    actual override fun iterator(): MutableIterator<T>
}

/**
 * A generic collection of elements.
 * @param E the type of elements contained in the collection. The collection is covariant in its element type.
 */
public actual interface Collection<out E> : Iterable<E> {
    public actual val size: Int
    public actual fun isEmpty(): Boolean
    public actual operator fun contains(element: @UnsafeVariance E): Boolean
    actual override fun iterator(): Iterator<E>
    public actual fun containsAll(elements: Collection<@UnsafeVariance E>): Boolean
}

/**
 * A generic collection of elements that supports adding and removing elements.
 * @param E the type of elements contained in the collection. The mutable collection is invariant in its element type.
 */
public actual interface MutableCollection<E> : Collection<E>, MutableIterable<E> {
    actual override fun iterator(): MutableIterator<E>
    public actual fun add(element: E): Boolean
    public actual fun remove(element: E): Boolean
    public actual fun addAll(elements: Collection<E>): Boolean
    public actual fun removeAll(elements: Collection<E>): Boolean
    public actual fun retainAll(elements: Collection<E>): Boolean
    public actual fun clear(): Unit
}

/**
 * A generic ordered collection of elements.
 * @param E the type of elements contained in the list. The list is covariant in its element type.
 */
public actual interface List<out E> : Collection<E> {
    actual override val size: Int
    actual override fun isEmpty(): Boolean
    actual override fun contains(element: @UnsafeVariance E): Boolean
    actual override fun iterator(): Iterator<E>
    actual override fun containsAll(elements: Collection<@UnsafeVariance E>): Boolean
    public actual operator fun get(index: Int): E
    public actual fun indexOf(element: @UnsafeVariance E): Int
    public actual fun lastIndexOf(element: @UnsafeVariance E): Int
    public actual fun listIterator(): ListIterator<E>
    public actual fun listIterator(index: Int): ListIterator<E>
    public actual fun subList(fromIndex: Int, toIndex: Int): List<E>
}

/**
 * A generic ordered collection of elements that supports adding and removing elements.
 * @param E the type of elements contained in the list. The mutable list is invariant in its element type.
 */
public actual interface MutableList<E> : List<E>, MutableCollection<E> {
    actual override fun add(element: E): Boolean
    actual override fun remove(element: E): Boolean
    actual override fun addAll(elements: Collection<E>): Boolean
    public actual fun addAll(index: Int, elements: Collection<E>): Boolean
    actual override fun removeAll(elements: Collection<E>): Boolean
    actual override fun retainAll(elements: Collection<E>): Boolean
    actual override fun clear(): Unit
    public actual operator fun set(index: Int, element: E): E
    public actual fun add(index: Int, element: E): Unit
    public actual fun removeAt(index: Int): E
    actual override fun listIterator(): MutableListIterator<E>
    actual override fun listIterator(index: Int): MutableListIterator<E>
    actual override fun subList(fromIndex: Int, toIndex: Int): MutableList<E>
}

/**
 * A generic unordered collection of unique elements.
 * @param E the type of elements contained in the set. The set is covariant in its element type.
 */
public actual interface Set<out E> : Collection<E> {
    actual override val size: Int
    actual override fun isEmpty(): Boolean
    actual override fun contains(element: @UnsafeVariance E): Boolean
    actual override fun iterator(): Iterator<E>
    actual override fun containsAll(elements: Collection<@UnsafeVariance E>): Boolean
}

/**
 * A generic unordered collection of unique elements that supports adding and removing elements.
 * @param E the type of elements contained in the set. The mutable set is invariant in its element type.
 */
public actual interface MutableSet<E> : Set<E>, MutableCollection<E> {
    actual override fun iterator(): MutableIterator<E>
    actual override fun add(element: E): Boolean
    actual override fun remove(element: E): Boolean
    actual override fun addAll(elements: Collection<E>): Boolean
    actual override fun removeAll(elements: Collection<E>): Boolean
    actual override fun retainAll(elements: Collection<E>): Boolean
    actual override fun clear(): Unit
}

/**
 * A collection that holds pairs of objects (keys and values).
 * @param K the type of map keys. The map is invariant in its key type.
 * @param V the type of map values. The map is covariant in its value type.
 */
public actual interface Map<K, out V> {
    public actual val size: Int
    public actual fun isEmpty(): Boolean
    public actual fun containsKey(key: K): Boolean
    public actual fun containsValue(value: @UnsafeVariance V): Boolean
    public actual operator fun get(key: K): V?
    public actual val keys: Set<K>
    public actual val values: Collection<V>
    public actual val entries: Set<Map.Entry<K, V>>

    public actual interface Entry<out K, out V> {
        public actual val key: K
        public actual val value: V
    }
}

/**
 * A modifiable collection that holds pairs of objects (keys and values).
 * @param K the type of map keys. The map is invariant in its key type.
 * @param V the type of map values. The mutable map is invariant in its value type.
 */
public actual interface MutableMap<K, V> : Map<K, V> {
    public actual fun put(key: K, value: V): V?
    public actual fun remove(key: K): V?
    public actual fun putAll(from: Map<out K, V>): Unit
    public actual fun clear(): Unit
    actual override val keys: MutableSet<K>
    actual override val values: MutableCollection<V>
    actual override val entries: MutableSet<MutableMap.MutableEntry<K, V>>

    public actual interface MutableEntry<K, V> : Map.Entry<K, V> {
        public actual fun setValue(newValue: V): V
    }
}
