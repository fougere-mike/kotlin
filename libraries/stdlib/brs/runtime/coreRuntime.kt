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
    if (obj1 === obj2) return true
    if (obj1 == null || obj2 == null) return false

    // For objects with equals method, delegate to it
    return obj1.equals(obj2)
}

/**
 * Converts any value to a string representation.
 */
internal fun toString(obj: Any?): String {
    if (obj == null) return "null"
    return obj.toString()
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
