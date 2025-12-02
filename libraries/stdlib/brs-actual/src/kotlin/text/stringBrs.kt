/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.text

/**
 * BrightScript String extensions implementation.
 * Maps to BrightScript's built-in string functions.
 */

/**
 * Returns a copy of this string converted to upper case using the rules of the default locale.
 */
@Deprecated("Use uppercase() instead.", ReplaceWith("uppercase()"))
@DeprecatedSinceKotlin(warningSince = "1.5", errorSince = "2.1")
public fun String.toUpperCase(): String = brsIntrinsicUCase(this)

/**
 * Returns a copy of this string converted to upper case using Unicode mapping rules of the invariant locale.
 */
@SinceKotlin("1.5")
public fun String.uppercase(): String = brsIntrinsicUCase(this)

/**
 * Returns a copy of this string converted to lower case using the rules of the default locale.
 */
@Deprecated("Use lowercase() instead.", ReplaceWith("lowercase()"))
@DeprecatedSinceKotlin(warningSince = "1.5", errorSince = "2.1")
public fun String.toLowerCase(): String = brsIntrinsicLCase(this)

/**
 * Returns a copy of this string converted to lower case using Unicode mapping rules of the invariant locale.
 */
@SinceKotlin("1.5")
public fun String.lowercase(): String = brsIntrinsicLCase(this)

/**
 * Returns a copy of this string having its first letter titlecased.
 */
@Deprecated("Use replaceFirstChar instead.", ReplaceWith("replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }"))
@DeprecatedSinceKotlin(warningSince = "1.5", errorSince = "2.1")
public fun String.capitalize(): String {
    return if (isNotEmpty() && this[0].isLowerCase())
        this[0].uppercase() + substring(1)
    else
        this
}

/**
 * Returns a copy of this string having its first letter lowercased.
 */
@Deprecated("Use replaceFirstChar instead.", ReplaceWith("replaceFirstChar { it.lowercase() }"))
@DeprecatedSinceKotlin(warningSince = "1.5", errorSince = "2.1")
public fun String.decapitalize(): String {
    return if (isNotEmpty() && this[0].isUpperCase())
        this[0].lowercase() + substring(1)
    else
        this
}

/**
 * Returns `true` if the contents of this char sequence are equal to the contents of the specified [other].
 */
@SinceKotlin("1.5")
public infix fun CharSequence?.contentEquals(other: CharSequence?): Boolean {
    if (this === other) return true
    if (this == null || other == null) return false
    if (this.length != other.length) return false
    for (i in 0 until this.length) {
        if (this[i] != other[i]) return false
    }
    return true
}

/**
 * Returns `true` if the contents of this char sequence are equal to the contents of the specified [other],
 * optionally ignoring case difference.
 */
@SinceKotlin("1.5")
public fun CharSequence?.contentEquals(other: CharSequence?, ignoreCase: Boolean): Boolean {
    if (this === other) return true
    if (this == null || other == null) return false
    if (this.length != other.length) return false

    if (!ignoreCase) {
        for (i in 0 until this.length) {
            if (this[i] != other[i]) return false
        }
    } else {
        for (i in 0 until this.length) {
            if (!this[i].equals(other[i], ignoreCase = true)) return false
        }
    }
    return true
}

internal actual inline fun String.nativeIndexOf(str: String, fromIndex: Int): Int = brsIntrinsicInstr(fromIndex, this, str)

internal actual inline fun String.nativeLastIndexOf(str: String, fromIndex: Int): Int {
    // BrightScript doesn't have lastIndexOf, implement manually
    var lastIndex = -1
    var searchIndex = 0
    while (searchIndex <= fromIndex) {
        val found = brsIntrinsicInstr(searchIndex, this, str)
        if (found < 0 || found > fromIndex) break
        lastIndex = found
        searchIndex = found + 1
    }
    return lastIndex
}

/**
 * Intrinsic function for BrightScript UCase().
 * This will be lowered to: UCase(<str>)
 */
@PublishedApi
internal external fun brsIntrinsicUCase(str: String): String

/**
 * Intrinsic function for BrightScript LCase().
 * This will be lowered to: LCase(<str>)
 */
@PublishedApi
internal external fun brsIntrinsicLCase(str: String): String

/**
 * Intrinsic function for BrightScript Instr().
 * This will be lowered to: Instr(<start>, <source>, <substring>)
 * Returns 0-based index or -1 if not found.
 */
@PublishedApi
internal external fun brsIntrinsicInstr(start: Int, source: String, substring: String): Int

/**
 * Intrinsic function for BrightScript Mid().
 * This will be lowered to: Mid(<source>, <start>, <length>)
 * Note: BrightScript Mid() uses 1-based indexing.
 */
@PublishedApi
internal external fun brsIntrinsicMid(source: String, start: Int, length: Int): String

// ============================================
// String Extension Functions
// ============================================

/**
 * Returns a substring of this string starting at [startIndex] and ending at the end of the string.
 */
public fun String.substring(startIndex: Int): String =
    this.substring(startIndex, this.length)

/**
 * Returns a substring of this string starting at [startIndex] and ending right before [endIndex].
 */
public fun String.substring(startIndex: Int, endIndex: Int): String {
    if (startIndex < 0) throw IndexOutOfBoundsException("startIndex: $startIndex")
    if (endIndex > this.length) throw IndexOutOfBoundsException("endIndex: $endIndex, length: ${this.length}")
    if (startIndex > endIndex) throw IllegalArgumentException("startIndex: $startIndex > endIndex: $endIndex")
    if (startIndex == endIndex) return ""
    // BrightScript Mid() uses 1-based indexing
    return brsIntrinsicMid(this, startIndex + 1, endIndex - startIndex)
}

/**
 * Returns the index within this string of the first occurrence of the specified [string],
 * starting from the specified [startIndex].
 *
 * @param ignoreCase `true` to ignore character case when matching a string. By default `false`.
 * @return An index of the first occurrence of [string] or `-1` if none is found.
 */
public fun String.indexOf(string: String, startIndex: Int = 0, ignoreCase: Boolean = false): Int {
    if (ignoreCase) {
        return this.lowercase().indexOf(string.lowercase(), startIndex, ignoreCase = false)
    }
    if (startIndex < 0) return indexOf(string, 0)
    if (startIndex >= this.length) {
        return if (string.isEmpty()) this.length else -1
    }
    return nativeIndexOf(string, startIndex)
}

/**
 * Returns the index within this string of the last occurrence of the specified [string],
 * starting from the specified [startIndex].
 *
 * @param startIndex The index of character to start searching at. The search proceeds backward toward the beginning of the string.
 * @param ignoreCase `true` to ignore character case when matching a string. By default `false`.
 * @return An index of the last occurrence of [string] or `-1` if none is found.
 */
public fun String.lastIndexOf(string: String, startIndex: Int = lastIndex, ignoreCase: Boolean = false): Int {
    if (ignoreCase) {
        return this.lowercase().lastIndexOf(string.lowercase(), startIndex, ignoreCase = false)
    }
    if (startIndex < 0) return -1
    return nativeLastIndexOf(string, startIndex.coerceAtMost(this.length))
}

/**
 * Returns `true` if this string is empty (contains no characters).
 */
public fun String.isEmpty(): Boolean = this.length == 0

/**
 * Returns `true` if this string is not empty.
 */
public fun String.isNotEmpty(): Boolean = this.length > 0

/**
 * Returns `true` if this string is empty or consists solely of whitespace characters.
 */
public fun String.isBlank(): Boolean {
    if (isEmpty()) return true
    for (i in 0 until length) {
        if (!this[i].isWhitespace()) return false
    }
    return true
}

/**
 * Returns `true` if this string is not empty and contains some characters except whitespace characters.
 */
public fun String.isNotBlank(): Boolean = !isBlank()

/**
 * Returns the last valid character index in this string, or -1 if the string is empty.
 */
public val String.lastIndex: Int
    get() = this.length - 1

/**
 * Returns the character at [index] or the result of calling [defaultValue] if the [index] is out of bounds.
 */
public fun String.getOrElse(index: Int, defaultValue: (Int) -> Char): Char =
    if (index >= 0 && index < this.length) this[index] else defaultValue(index)

/**
 * Returns a char sequence with content of this char sequence padded at the beginning
 * to the specified [length] with the specified character [padChar] or space.
 */
public fun String.padStart(length: Int, padChar: Char = ' '): String {
    if (length <= 0) throw IllegalArgumentException("Desired length $length is less than zero.")
    if (this.length >= length) return this
    val sb = StringBuilder(length)
    var i = 0
    val times = length - this.length
    while (i < times) {
        sb.append(padChar)
        i++
    }
    sb.append(this)
    return sb.toString()
}

/**
 * Returns a char sequence with content of this char sequence padded at the end
 * to the specified [length] with the specified character [padChar] or space.
 */
public fun String.padEnd(length: Int, padChar: Char = ' '): String {
    if (length <= 0) throw IllegalArgumentException("Desired length $length is less than zero.")
    if (this.length >= length) return this
    val sb = StringBuilder(length)
    sb.append(this)
    var i = 0
    val times = length - this.length
    while (i < times) {
        sb.append(padChar)
        i++
    }
    return sb.toString()
}

/**
 * Returns a string containing this char sequence repeated [n] times.
 */
public fun String.repeat(n: Int): String {
    if (n < 0) throw IllegalArgumentException("Count 'n' must be non-negative, but was $n.")
    if (n == 0 || this.isEmpty()) return ""
    if (n == 1) return this
    val sb = StringBuilder(this.length * n)
    var i = 0
    while (i < n) {
        sb.append(this)
        i++
    }
    return sb.toString()
}

/**
 * Returns `true` if this string starts with the specified prefix.
 */
public fun String.startsWith(prefix: String, ignoreCase: Boolean = false): Boolean {
    if (prefix.length > this.length) return false
    if (ignoreCase) {
        return this.substring(0, prefix.length).equals(prefix, ignoreCase = true)
    }
    return this.substring(0, prefix.length) == prefix
}

/**
 * Returns `true` if this string ends with the specified suffix.
 */
public fun String.endsWith(suffix: String, ignoreCase: Boolean = false): Boolean {
    if (suffix.length > this.length) return false
    if (ignoreCase) {
        return this.substring(this.length - suffix.length).equals(suffix, ignoreCase = true)
    }
    return this.substring(this.length - suffix.length) == suffix
}

/**
 * Returns `true` if this string is equal to [other], optionally ignoring case.
 */
public fun String.equals(other: String?, ignoreCase: Boolean = false): Boolean {
    if (other == null) return false
    if (this === other) return true
    if (this.length != other.length) return false
    if (!ignoreCase) return this == other
    return this.lowercase() == other.lowercase()
}

/**
 * Returns `true` if this string contains the specified [other] sequence.
 */
public fun String.contains(other: String, ignoreCase: Boolean = false): Boolean {
    return indexOf(other, 0, ignoreCase) >= 0
}

// ============================================
// CharArray Extensions
// ============================================

/**
 * Concatenates characters in this [CharArray] into a String.
 */
public fun CharArray.concatToString(): String {
    val sb = StringBuilder(this.size)
    for (c in this) sb.append(c)
    return sb.toString()
}

/**
 * Concatenates characters in this [CharArray] or its subrange into a String.
 */
public fun CharArray.concatToString(startIndex: Int = 0, endIndex: Int = this.size): String {
    if (startIndex < 0 || endIndex > this.size) {
        throw IndexOutOfBoundsException("startIndex: $startIndex, endIndex: $endIndex, size: $size")
    }
    if (startIndex > endIndex) {
        throw IllegalArgumentException("startIndex: $startIndex > endIndex: $endIndex")
    }
    val sb = StringBuilder(endIndex - startIndex)
    for (i in startIndex until endIndex) sb.append(this[i])
    return sb.toString()
}
