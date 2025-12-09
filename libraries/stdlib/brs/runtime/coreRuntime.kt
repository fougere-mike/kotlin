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

    // Object - call toString method directly (using inline to avoid recursive dispatch)
    return brsObjectToString(obj)
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
    val s = brsIntrinsicStr(value)
    if (brsIntrinsicLeft(s, 1) == " ") return brsIntrinsicMid(s, 2)
    return s
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

// BrightScript global function intrinsics
// Named with 'brsIntrinsic' prefix so the compiler recognizes them
// and replaces calls with direct BrightScript function calls.
internal external fun brsIntrinsicStr(x: Any?): String
internal external fun brsIntrinsicLeft(s: String, n: Int): String
internal external fun brsIntrinsicMid(s: String, start: Int): String
