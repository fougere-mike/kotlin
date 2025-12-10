/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs.runtime

/**
 * BrightScript intrinsic functions.
 * These are lowered by the BRS backend to native BrightScript operations.
 */

// ============================================
// Type Checking Intrinsics
// ============================================

/**
 * Gets the BrightScript type name of a value.
 * Lowered to: Type(value)
 */
@PublishedApi
internal fun brsTypeOf(value: Any?): String {
    // This will be lowered by the backend to BrightScript's Type() function
    return "Object" // Placeholder
}

/**
 * Checks if a value is invalid (BrightScript's null equivalent).
 * Lowered to: value = Invalid
 */
@PublishedApi
internal fun brsIsInvalid(value: Any?): Boolean {
    return value == null
}

// ============================================
// Object Creation Intrinsics
// ============================================

/**
 * Creates a BrightScript object using CreateObject.
 * Lowered to: CreateObject(className, ...)
 */
@PublishedApi
internal fun brsCreateObject(className: String, vararg args: Any?): Any? {
    // This will be lowered by the backend to CreateObject()
    error("brsCreateObject should be lowered by the backend")
}

// ============================================
// Array Intrinsics
// ============================================

/**
 * Creates a new BrightScript array (roArray).
 * Lowered to: CreateObject("roArray", size, true)
 */
@PublishedApi
internal fun brsCreateArray(size: Int = 0): Any {
    // This will be lowered by the backend
    error("brsCreateArray should be lowered by the backend")
}

/**
 * Gets the length of a BrightScript array.
 * Lowered to: array.Count()
 */
@PublishedApi
internal fun brsArrayLength(array: Any): Int {
    // This will be lowered by the backend
    error("brsArrayLength should be lowered by the backend")
}

// ============================================
// Associative Array Intrinsics
// ============================================

/**
 * Creates a new BrightScript associative array (roAssociativeArray).
 * Lowered to: {}
 */
@PublishedApi
internal fun brsCreateAssociativeArray(): Any {
    // This will be lowered by the backend
    error("brsCreateAssociativeArray should be lowered by the backend")
}

// ============================================
// String Intrinsics
// ============================================

/**
 * Gets the length of a string.
 * Lowered to: Len(str) or str.Len()
 */
@PublishedApi
internal fun brsStringLength(str: String): Int {
    return str.length
}

/**
 * Concatenates two strings.
 * Lowered to: str1 + str2
 */
@PublishedApi
internal fun brsStringConcat(str1: String, str2: String): String {
    return str1 + str2
}

// ============================================
// Console Intrinsics
// ============================================

/**
 * Prints a value to the console.
 * Lowered to: print value
 */
@PublishedApi
internal fun brsPrint(value: Any?) {
    // This will be lowered by the backend to BrightScript's print statement
}

// ============================================
// JSON Intrinsics
// ============================================

/**
 * Parses a JSON string into an object.
 * Lowered to: ParseJson(jsonString)
 */
@PublishedApi
internal fun brsParseJson(jsonString: String): Any? {
    error("brsParseJson should be lowered by the backend")
}

/**
 * Formats an object as a JSON string.
 * Lowered to: FormatJson(obj)
 *
 * This is useful for creating JSON output for debugging or test results.
 */
public fun brsFormatJson(obj: Any?): String {
    error("brsFormatJson should be lowered by the backend")
}

// ============================================
// Map to Plain AA Conversion
// ============================================

/**
 * Converts a Kotlin Map to a plain BrightScript associative array.
 *
 * This is necessary because Kotlin Maps (HashMap, LinkedHashMap) compile to
 * BrightScript AAs that include method references (like clear_k_, put_k_, etc.).
 * FormatJson() cannot serialize Function types, so we need to extract just
 * the key-value pairs into a plain AA.
 *
 * The returned AA only contains the string keys and their values.
 */
public fun mapToPlainAA(map: Map<String, Any?>): Any {
    val result = brsCreatePlainAA()
    val entries = map.entries
    val iter = entries.iterator()
    while (iter.hasNext()) {
        val entry = iter.next()
        brsAAAddReplace(result, entry.key, entry.value)
    }
    return result
}

@PublishedApi
internal fun brsCreatePlainAA(): Any {
    error("brsCreatePlainAA should be lowered by the backend")
}

@PublishedApi
internal fun brsAAAddReplace(aa: Any, key: String, value: Any?) {
    error("brsAAAddReplace should be lowered by the backend")
}
