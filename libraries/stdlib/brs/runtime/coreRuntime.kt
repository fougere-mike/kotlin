/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs.runtime

/**
 * Core runtime functions for BrightScript target.
 * These functions are used by generated code and internal stdlib implementation.
 */

/**
 * Compares two objects for equality.
 * Handles BrightScript-specific comparison semantics.
 */
internal fun equals(obj1: Any?, obj2: Any?): Boolean {
    // Identity check first - uses the EQEQEQ intrinsic which will be lowered
    // by the compiler to __kotlin_identityEquals
    if (obj1 === obj2) return true
    if (obj1 == null || obj2 == null) return false

    // For objects with equals method, delegate to it
    return obj1.equals(obj2)
}

/**
 * Converts any value to a string representation.
 *
 * Written using only if-statements (not if-expressions) to avoid generating
 * IIFEs that cause scope issues with global built-in functions in BrightScript.
 */
internal fun toString(obj: Any?): String {
    if (obj == null) return "null"

    // Get type string
    val t = brsType(obj)

    // String types - return as-is
    if (t == "String" || t == "roString") {
        @Suppress("UNCHECKED_CAST")
        return obj as String
    }

    // Numeric types - convert without leading space
    if (t == "Integer" || t == "LongInteger" || t == "Float" || t == "Double" ||
        t == "roInt" || t == "roFloat" || t == "roDouble" || t == "roInteger" || t == "roLongInteger") {
        return __kotlin_numToStr(obj)
    }

    // Boolean types
    if (t == "Boolean" || t == "roBoolean") {
        @Suppress("UNCHECKED_CAST")
        if (obj as Boolean) return "true"
        return "false"
    }

    // Handle roAssociativeArray - check if it's a Kotlin object or native AA
    if (t == "roAssociativeArray") {
        if (brsHasField(obj, "__type")) {
            // Kotlin object - check if it actually has toString method
            if (brsHasField(obj, "toString")) {
                return brsObjectToString(obj)
            }
            // No toString method (e.g., anonymous class) - return type name
            return brsGetField(obj, "__type") as String
        }
        // Native AA - format manually
        return __kotlin_nativeAAToString(obj, 0)
    }

    // Handle roArray - native arrays without toString method
    if (t == "roArray") {
        return __kotlin_nativeArrayToString(obj, 0)
    }

    // Function types - return placeholder
    if (t == "Function" || t == "roFunction") {
        return "[Function]"
    }

    // Unknown native types - return type name instead of crashing
    return "[" + t + "]"
}

/**
 * Gets the BrightScript type name of a value.
 */
@kotlin.brs.BrsInline("return Type(obj)")
private external fun brsType(obj: Any?): String

/**
 * Calls toString() method on an object. Uses inline to ensure direct method call.
 */
@kotlin.brs.BrsInline("return obj.toString()")
private external fun brsObjectToString(obj: Any?): String

/**
 * Converts native roAssociativeArray to string: {key=value, key2=value2}
 * Uses depth tracking to prevent infinite recursion on deeply nested structures.
 */
private fun __kotlin_nativeAAToString(aa: Any, depth: Int): String {
    if (depth > 3) return "{...}"
    val keys = brsIntrinsicKeys(aa)
    val count = brsIntrinsicCount(keys)
    if (count == 0) return "{}"
    var result = "{"
    var i = 0
    while (i < count) {
        if (i > 0) result = result + ", "
        @Suppress("UNCHECKED_CAST")
        val key = brsIntrinsicArrayGet(keys, i) as String
        result = result + key + "="
        val value = brsIntrinsicGetField(aa, key)
        result = result + __kotlin_valueToStringWithDepth(value, depth + 1)
        i = i + 1
    }
    return result + "}"
}

/**
 * Converts native roArray to string: [elem, elem, elem]
 * Uses depth tracking to prevent infinite recursion on deeply nested structures.
 */
private fun __kotlin_nativeArrayToString(arr: Any, depth: Int): String {
    if (depth > 3) return "[...]"
    val count = brsIntrinsicCount(arr)
    if (count == 0) return "[]"
    var result = "["
    var i = 0
    while (i < count) {
        if (i > 0) result = result + ", "
        val element = brsIntrinsicArrayGet(arr, i)
        result = result + __kotlin_valueToStringWithDepth(element, depth + 1)
        i = i + 1
    }
    return result + "]"
}

/**
 * Internal helper for recursive toString with depth tracking.
 * Handles all value types for use in nested structure formatting.
 */
private fun __kotlin_valueToStringWithDepth(obj: Any?, depth: Int): String {
    if (obj == null) return "null"
    val t = brsType(obj)

    if (t == "String" || t == "roString") {
        @Suppress("UNCHECKED_CAST")
        return obj as String
    }
    if (t == "Integer" || t == "LongInteger" || t == "Float" || t == "Double" ||
        t == "roInt" || t == "roFloat" || t == "roDouble" || t == "roInteger" || t == "roLongInteger") {
        return __kotlin_numToStr(obj)
    }
    if (t == "Boolean" || t == "roBoolean") {
        @Suppress("UNCHECKED_CAST")
        if (obj as Boolean) return "true"
        return "false"
    }
    if (t == "roAssociativeArray") {
        if (brsHasField(obj, "__type")) {
            if (brsHasField(obj, "toString")) return brsObjectToString(obj)
            return brsGetField(obj, "__type") as String
        }
        return __kotlin_nativeAAToString(obj, depth)
    }
    if (t == "roArray") return __kotlin_nativeArrayToString(obj, depth)
    if (t == "Function" || t == "roFunction") return "[Function]"
    return "[" + t + "]"
}

/**
 * Computes the hash code for an object.
 */
internal fun hashCode(obj: Any?): Int {
    if (obj == null) return 0
    return obj.hashCode()
}

/**
 * Computes hash code for a String.
 * Uses standard algorithm: s[0]*31^(n-1) + s[1]*31^(n-2) + ... + s[n-1]
 */
internal fun getStringHashCode(str: String): Int {
    var hash = 0
    for (i in 0 until str.length) {
        hash = 31 * hash + str[i].code
    }
    return hash
}

/**
 * Computes hash code for a Boolean.
 */
internal fun getBooleanHashCode(value: Boolean): Int {
    return if (value) 1231 else 1237
}

/**
 * Identity hash code for objects.
 * In BrightScript, objects don't have a native identity hash,
 * so we use a counter-based approach for objects.
 */
private var objectHashCodeCounter = 0

internal fun identityHashCode(obj: Any?): Int {
    if (obj == null) return 0
    // For primitives, use their value-based hash
    return when (obj) {
        is String -> getStringHashCode(obj)
        is Boolean -> getBooleanHashCode(obj)
        is Number -> obj.hashCode()
        else -> {
            // For objects, we'd need to attach a hash code property
            // This will be handled by the backend via object metadata
            objectHashCodeCounter++
        }
    }
}

/**
 * Creates a new Throwable instance with the given message and cause.
 */
internal fun newThrowable(message: String?, cause: Throwable?): Throwable {
    return Throwable(message, cause)
}

/**
 * Captures the stack trace for a throwable.
 */
internal fun captureStack(instance: Throwable) {
    instance.captureStack()
}

/**
 * Type checking utilities for BrightScript runtime.
 */

/**
 * Checks if a value is of a specific type.
 * Used by generated code for type checks and casts.
 */
internal fun isInstance(obj: Any?, type: String): Boolean {
    if (obj == null) return false
    // Type checking will be implemented based on BrightScript's type system
    return true // Placeholder - actual implementation by backend
}

/**
 * Throws a ClassCastException if the object is not of the expected type.
 */
internal fun checkCast(obj: Any?, type: String): Any? {
    if (obj == null) return null
    if (!isInstance(obj, type)) {
        throw ClassCastException("Cannot cast ${obj::class.simpleName} to $type")
    }
    return obj
}

/**
 * Converts a numeric value to a string without the leading space.
 * BrightScript's Str() function adds a leading space for positive numbers.
 *
 * Uses stdlib intrinsics (brsIntrinsic*) which the compiler recognizes
 * and replaces with direct BrightScript function calls.
 */
public fun __kotlin_numToStr(value: Int): String {
    val s = brsIntrinsicStr(value)
    if (brsIntrinsicLeft(s, 1) == " ") return brsIntrinsicMid(s, 2)
    return s
}

public fun __kotlin_numToStr(value: Long): String {
    // BrightScript uses scientific notation for numbers >= 1e10
    // For large values, build the string digit by digit to preserve precision
    if (value >= 10000000000L || value <= -10000000000L) {
        return __kotlin_longToFixedStr(value)
    }
    val s = brsIntrinsicStr(value)
    if (brsIntrinsicLeft(s, 1) == " ") return brsIntrinsicMid(s, 2)
    return s
}

private fun __kotlin_longToFixedStr(value: Long): String {
    if (value == 0L) return "0"
    val isNegative = value < 0
    var remaining = if (isNegative) -value else value
    var result = ""
    while (remaining > 0) {
        val digit = (remaining % 10).toInt()
        result = digit.toString() + result
        remaining = remaining / 10
    }
    return if (isNegative) "-$result" else result
}

public fun __kotlin_numToStr(value: Float): String {
    val s = brsIntrinsicStr(value)
    if (brsIntrinsicLeft(s, 1) == " ") return brsIntrinsicMid(s, 2)
    return s
}

public fun __kotlin_numToStr(value: Double): String {
    val s = brsIntrinsicStr(value)
    if (brsIntrinsicLeft(s, 1) == " ") return brsIntrinsicMid(s, 2)
    return s
}

public fun __kotlin_numToStr(value: Any?): String {
    if (value == null) return "null"
    val s = brsIntrinsicStr(value)
    if (brsIntrinsicLeft(s, 1) == " ") return brsIntrinsicMid(s, 2)
    return s
}

/**
 * Gets the length of a CharSequence value.
 * For native BrightScript strings, uses Len().
 * For other CharSequence implementations (StringBuilder, etc.), uses get_length().
 *
 * Note: This function uses brsIntrinsicGetField to call get_length() directly,
 * avoiding the .length property which would recursively call this function.
 */
public fun __kotlin_charSequenceLength(value: CharSequence?): Int {
    if (value == null) return 0
    val t = brsType(value)
    // Native BrightScript string types use Len()
    if (t == "String" || t == "roString") {
        @Suppress("UNCHECKED_CAST")
        return brsIntrinsicLen(value as String)
    }
    // Other CharSequence implementations (StringBuilder, etc.) use get_length property
    // Use brsIntrinsicGetField to avoid recursive call back to this function
    @Suppress("UNCHECKED_CAST")
    return brsIntrinsicGetLength(value as Any)
}

// BrightScript global function intrinsics
// Named with 'brsIntrinsic' prefix so the compiler recognizes them
// and replaces calls with direct BrightScript function calls.
internal external fun brsIntrinsicStr(x: Any?): String
internal external fun brsIntrinsicLeft(s: String, n: Int): String
internal external fun brsIntrinsicMid(s: String, start: Int): String
internal external fun brsIntrinsicLen(s: String): Int

// Calls obj.get_length() directly - used by __kotlin_charSequenceLength
// to avoid recursive call through .length property
internal external fun brsIntrinsicGetLength(obj: Any): Int

// ============================================
// JSON Serialization Helpers
// ============================================

/**
 * Converts a value to a JSON-serializable format.
 *
 * Kotlin collections (Map, List, etc.) contain method references that
 * BrightScript's FormatJson() cannot serialize. This function recursively
 * converts:
 * - Map -> plain roAssociativeArray (key-value only)
 * - List/Collection -> plain roArray
 * - Other values pass through unchanged
 *
 * Called by the compiler when lowering brsFormatJson().
 */
public fun __kotlin_toJsonValue(value: Any?): Any? {
    if (value == null) return null

    val t = brsType(value)

    // Already a primitive type - pass through
    if (t == "String" || t == "roString" ||
        t == "Integer" || t == "LongInteger" || t == "Float" || t == "Double" ||
        t == "roInt" || t == "roFloat" || t == "roDouble" || t == "roInteger" || t == "roLongInteger" ||
        t == "Boolean" || t == "roBoolean") {
        return value
    }

    // Check if it's a Kotlin Map by looking for the __type field or Kotlin methods
    if (t == "roAssociativeArray") {
        // Check for known Kotlin collection types by their __type field
        if (brsHasField(value, "__type")) {
            val type = brsGetField(value, "__type")
            // Use direct string comparison via native equals check
            if (brsStringEquals(type, "LinkedHashMap") || brsStringEquals(type, "HashMap")) {
                return __kotlin_mapToPlainAA(value)
            }
            if (brsStringEquals(type, "ArrayList") || brsStringEquals(type, "LinkedHashSet") || brsStringEquals(type, "HashSet")) {
                return __kotlin_collectionToPlainArray(value)
            }
            // EmptyMap - return an empty plain AA
            if (brsStringEquals(type, "EmptyMap")) {
                return brsIntrinsicCreateObject("roAssociativeArray")
            }
            // EmptyList - return an empty plain array
            if (brsStringEquals(type, "EmptyList")) {
                return brsIntrinsicCreateObject("roArray")
            }
        }
        // Fallback: check for Kotlin methods that indicate it's a Map
        if (brsHasField(value, "get_map")) {
            // Has get_map method - it's a Kotlin Map
            return __kotlin_mapToPlainAA(value)
        }
        // Plain AA without Kotlin methods - pass through
        return value
    }

    // Check if it's a roArray (including Kotlin Lists compiled to arrays)
    if (t == "roArray") {
        return __kotlin_arrayToPlainArray(value)
    }

    // Unknown type - pass through as-is
    return value
}

/**
 * Converts a Kotlin Map (roAssociativeArray with methods) to a plain AA.
 * Recursively converts values.
 *
 * LinkedHashMap and HashMap store actual data in an internal `map` field.
 * We extract data from that field directly instead of trying to filter keys.
 */
private fun __kotlin_mapToPlainAA(map: Any): Any {
    val result = brsIntrinsicCreateObject("roAssociativeArray")

    // LinkedHashMap and HashMap store data in a `get_map` method that returns the internal AA
    // The internal map stores entries as {k: originalKey, v: value}
    if (brsHasField(map, "get_map")) {
        val internalMap = brsIntrinsicCallMethod(map, "get_map")
        val keys = brsIntrinsicKeys(internalMap)
        for (internalKey in keys) {
            val entry = brsIntrinsicGetField(internalMap, internalKey)
            // Entry is {k: originalKey, v: value} - extract the original key and value
            @Suppress("UNCHECKED_CAST")
            val originalKey = brsIntrinsicGetField(entry as Any, "k")
            @Suppress("UNCHECKED_CAST")
            val value = brsIntrinsicGetField(entry as Any, "v")
            // Convert key to string for JSON output
            val keyStr = brsToString(originalKey)
            // Recursively convert nested values
            val convertedValue = __kotlin_toJsonValue(value)
            brsIntrinsicAddReplace(result, keyStr, convertedValue)
        }
    } else {
        // Fallback for regular AAs - filter out function-typed keys
        val keys = brsIntrinsicKeys(map)
        for (key in keys) {
            val value = brsIntrinsicGetField(map, key)
            // Skip if value is a function (method)
            val valueType = brsType(value)
            if (valueType != "Function" && valueType != "roFunction") {
                // Skip internal fields that start with __ or end with _k_
                // Use intrinsics instead of stdlib calls to avoid initialization dependencies
                val isInternalKey = brsIntrinsicStartsWith(key, "__") || brsIntrinsicEndsWith(key, "_k_") ||
                    brsIntrinsicStartsWith(key, "get_") || brsIntrinsicStartsWith(key, "set_") ||
                    brsStringEquals(key, "equals") || brsStringEquals(key, "hashCode") || brsStringEquals(key, "toString") ||
                    brsStringEquals(key, "copy") || brsStringEquals(key, "_size")
                if (!isInternalKey) {
                    val convertedValue = __kotlin_toJsonValue(value)
                    brsIntrinsicAddReplace(result, key, convertedValue)
                }
            }
        }
    }
    return result
}

// Convert any value to string (for JSON keys)
internal external fun brsToString(value: Any?): String

// Call a method on an object (no arguments)
internal external fun brsIntrinsicCallMethod(obj: Any, method: String): Any

/**
 * Converts a Kotlin collection (with get_array method) to a plain roArray.
 * Recursively converts elements.
 */
private fun __kotlin_collectionToPlainArray(collection: Any): Any {
    val array = brsIntrinsicCallGetArray(collection)
    return __kotlin_arrayToPlainArray(array)
}

/**
 * Converts an roArray to a plain roArray with recursively converted elements.
 */
private fun __kotlin_arrayToPlainArray(array: Any): Any {
    val count = brsIntrinsicCount(array)
    val result = brsIntrinsicCreateObject("roArray")
    var i = 0
    while (i < count) {
        val element = brsIntrinsicArrayGet(array, i)
        val convertedElement = __kotlin_toJsonValue(element)
        brsIntrinsicArrayPush(result, convertedElement)
        i = i + 1
    }
    return result
}

// BrightScript AA/Array manipulation intrinsics
internal external fun brsHasField(obj: Any, field: String): Boolean
internal external fun brsGetField(obj: Any, field: String): Any?
internal external fun brsIntrinsicCreateObject(type: String): Any
internal external fun brsIntrinsicKeys(aa: Any): Array<String>
internal external fun brsIntrinsicGetField(aa: Any, key: String): Any?
internal external fun brsIntrinsicAddReplace(aa: Any, key: String, value: Any?)
internal external fun brsIntrinsicCallGetArray(collection: Any): Any
internal external fun brsIntrinsicCount(array: Any): Int
internal external fun brsIntrinsicArrayGet(array: Any, index: Int): Any?
internal external fun brsIntrinsicArrayPush(array: Any, value: Any?)

// Simple string comparison that bypasses Kotlin's structural equals
internal external fun brsStringEquals(a: Any?, b: String): Boolean

// Direct BrightScript string prefix/suffix checks - avoids stdlib dependency
// These compile to inline Left()/Right() comparisons
internal external fun brsIntrinsicStartsWith(str: String, prefix: String): Boolean
internal external fun brsIntrinsicEndsWith(str: String, suffix: String): Boolean
