/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs.roku

import kotlin.brs.BrsCreateObject
import kotlin.brs.Dynamic

// =============================================================================
// Marker Types for Native Iteration
// =============================================================================

/**
 * Marker interface for native BrightScript array iteration.
 *
 * When a type's `iterator()` function returns this type, the compiler emits
 * native BrightScript `for each` instead of the Kotlin iterator protocol
 * (hasNext/next calls).
 *
 * This is a marker type - it has no methods. It signals to the compiler that
 * the iterable type supports native BrightScript enumeration via ifEnum.
 */
public external interface NativeArrayIterator<out T>

/**
 * Interface for types that support native BrightScript for-each iteration.
 *
 * Types implementing this interface can be used in `for (x in collection)` loops,
 * and the compiler will emit native BrightScript `for each` statements.
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/interfaces/ifenum.md">ifEnum</a>
 */
public interface NativeIterable {
    /**
     * Returns a native iterator for this collection.
     *
     * The return type [NativeArrayIterator] signals to the compiler that this type
     * supports native BrightScript for-each iteration.
     */
    public fun iterator(): NativeArrayIterator<Dynamic>
}

// =============================================================================
// BrightScript Interface Mappings
// =============================================================================

/**
 * Maps to BrightScript's ifArrayGet interface.
 * Provides indexed read access to elements.
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/interfaces/ifarrayget.md">ifArrayGet</a>
 */
public external interface IArrayGet {
    /**
     * Gets the element at the specified index.
     *
     * @param index The zero-based index of the element.
     * @return The element at the index, or invalid if out of bounds.
     */
    public operator fun get(index: Int): Dynamic
}

/**
 * Maps to BrightScript's ifArraySet interface.
 * Provides indexed write access to elements.
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/interfaces/ifarrayset.md">ifArraySet</a>
 */
public external interface IArraySet {
    /**
     * Sets the element at the specified index.
     *
     * @param index The zero-based index of the element.
     * @param value The value to set.
     */
    public operator fun set(index: Int, value: Any?)
}

/**
 * Maps to BrightScript's ifArray interface.
 * Core array manipulation methods.
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/interfaces/ifarray.md">ifArray</a>
 */
public external interface IArray : IArrayGet, IArraySet {
    /**
     * Returns the number of elements in the array.
     */
    public fun count(): Int

    /**
     * Appends a value to the end of the array.
     *
     * @param value The value to append.
     */
    public fun push(value: Any?)

    /**
     * Removes and returns the last element of the array.
     *
     * @return The last element, or invalid if the array is empty.
     */
    public fun pop(): Dynamic?

    /**
     * Removes and returns the first element of the array.
     *
     * @return The first element, or invalid if the array is empty.
     */
    public fun shift(): Dynamic?

    /**
     * Inserts a value at the beginning of the array.
     *
     * @param value The value to insert.
     */
    public fun unshift(value: Any?)

    /**
     * Deletes the element at the specified index.
     *
     * @param index The zero-based index of the element to delete.
     * @return True if the element was deleted, false if index was invalid.
     */
    public fun delete(index: Int): Boolean

    /**
     * Removes all elements from the array.
     */
    public fun clear()

    /**
     * Appends all elements from another array to this array.
     *
     * @param arr The array to append.
     */
    public fun append(arr: RoArray)

    /**
     * Returns the last element without removing it.
     *
     * @return The last element, or invalid if the array is empty.
     */
    public fun peek(): Dynamic?
}

/**
 * Maps to BrightScript's ifArrayJoin interface.
 * Provides array-to-string conversion.
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/interfaces/ifarrayjoin.md">ifArrayJoin</a>
 */
public external interface IArrayJoin {
    /**
     * Joins all elements into a string with the specified separator.
     *
     * @param separator The string to use between elements.
     * @return The joined string.
     */
    public fun join(separator: String): String
}

/**
 * Maps to BrightScript's ifArraySort interface.
 * Provides in-place sorting capabilities.
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/interfaces/ifarraysort.md">ifArraySort</a>
 */
public external interface IArraySort {
    /**
     * Sorts the array in ascending order.
     * For associative arrays, sorts by a default key.
     */
    public fun sort()

    /**
     * Sorts an array of associative arrays by a specific field.
     *
     * @param fieldName The field name to sort by.
     */
    public fun sortBy(fieldName: String)

    /**
     * Reverses the order of elements in the array.
     */
    public fun reverse()
}

/**
 * Maps to BrightScript's ifEnum interface for native types.
 * Provides native iteration support.
 *
 * Unlike [IEnum] which is for Kotlin wrapper types, this interface
 * is for external native BrightScript types that support enumeration.
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/interfaces/ifenum.md">ifEnum</a>
 */
public external interface IEnumNative : NativeIterable {
    /**
     * Resets the enumeration position to the beginning.
     */
    public fun reset()

    /**
     * Returns the element at the current position and advances to the next.
     *
     * @return The current element, or invalid if enumeration is complete.
     */
    public fun next(): Dynamic?

    /**
     * Checks if there are more elements to enumerate.
     *
     * @return True if there are more elements.
     */
    public fun isNext(): Boolean

    /**
     * Returns a native iterator for this collection.
     * Enables use in `for (x in collection)` loops with native BrightScript for-each.
     */
    override fun iterator(): NativeArrayIterator<Dynamic>
}

/**
 * Maps to BrightScript's ifAssociativeArray interface.
 * Provides key-value storage with string keys.
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/interfaces/ifassociativearray.md">ifAssociativeArray</a>
 */
public external interface IAssociativeArray {
    /**
     * Checks if a key exists in the associative array.
     *
     * @param key The key to check.
     * @return True if the key exists.
     */
    public fun doesExist(key: String): Boolean

    /**
     * Looks up a value by key.
     *
     * @param key The key to look up.
     * @return The value, or invalid if the key doesn't exist.
     */
    public fun lookup(key: String): Dynamic?

    /**
     * Adds or replaces a key-value pair.
     *
     * @param key The key.
     * @param value The value to associate with the key.
     */
    public fun addReplace(key: String, value: Any?)

    /**
     * Deletes a key-value pair.
     *
     * @param key The key to delete.
     * @return True if the key was deleted, false if it didn't exist.
     */
    public fun delete(key: String): Boolean

    /**
     * Removes all key-value pairs.
     */
    public fun clear()

    /**
     * Returns an array of all keys.
     *
     * @return An roArray containing all keys.
     */
    public fun keys(): RoArray

    /**
     * Returns an array of all key-value pairs as associative arrays.
     *
     * @return An roArray where each element is an associative array with "key" and "value" fields.
     */
    public fun items(): RoArray

    /**
     * Returns the number of key-value pairs.
     *
     * @return The count of entries.
     */
    public fun count(): Int

    /**
     * Sets the lookup mode to case-sensitive.
     * By default, key lookups are case-insensitive.
     */
    public fun setModeCaseSensitive()

    /**
     * Case-insensitive lookup.
     *
     * @param key The key to look up (case-insensitive).
     * @return The value, or invalid if the key doesn't exist.
     */
    public fun lookupCI(key: String): Dynamic?

    /**
     * Appends all entries from another associative array.
     *
     * @param aa The associative array to append from.
     */
    public fun append(aa: RoAssociativeArray)
}

// =============================================================================
// Native Type Interfaces
// =============================================================================

/**
 * Interface representing a native BrightScript roArray.
 *
 * This interface describes the capabilities of a native roArray. User code can
 * pass native roArray values directly to functions expecting this type - no
 * wrapping is required.
 *
 * roArray is BrightScript's dynamic array type that can hold elements of any type.
 *
 * Example:
 * ```kotlin
 * // Create a native array
 * val arr = RoArray.create(10, true)
 * arr.push("hello")
 * arr.push(42)
 *
 * // Iterate using native for-each
 * for (item in arr) {
 *     println(item)
 * }
 * ```
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/components/roarray.md">roArray</a>
 */
public external interface RoArray : IArray, IArrayJoin, IArraySort, IEnumNative {
    public companion object {
        /**
         * Creates a native roArray.
         *
         * Compiles to: `CreateObject("roArray", size, resize)`
         *
         * Note: Parameters are required because external functions with @BrsCreateObject
         * cannot have default values (they compile directly to CreateObject() calls).
         *
         * @param size Initial capacity of the array.
         * @param resize If true, the array will automatically resize when needed.
         * @return A new roArray instance.
         */
        @BrsCreateObject("roArray")
        public fun create(size: Int, resize: Boolean): RoArray = definedExternally
    }
}

/**
 * Interface representing a native BrightScript roAssociativeArray.
 *
 * This interface describes the capabilities of a native roAssociativeArray.
 * User code can pass native roAssociativeArray values directly to functions
 * expecting this type - no wrapping is required.
 *
 * roAssociativeArray is BrightScript's key-value map type with string keys.
 * Keys are case-insensitive by default.
 *
 * Example:
 * ```kotlin
 * // Create a native associative array
 * val aa = RoAssociativeArray.create()
 * aa.addReplace("name", "John")
 * aa.addReplace("age", 30)
 *
 * // Access by key
 * val name = aa.lookup("name")
 *
 * // Iterate over keys
 * for (key in aa) {
 *     println("$key = ${aa.lookup(key)}")
 * }
 * ```
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/components/roassociativearray.md">roAssociativeArray</a>
 */
public external interface RoAssociativeArray : IAssociativeArray, IArrayGet, IArraySet, IEnumNative {
    public companion object {
        /**
         * Creates a native roAssociativeArray.
         *
         * Compiles to: `CreateObject("roAssociativeArray")`
         *
         * @return A new roAssociativeArray instance.
         */
        @BrsCreateObject("roAssociativeArray")
        public fun create(): RoAssociativeArray = definedExternally
    }
}
