/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs.text

/**
 * BrightScript-specific string utilities.
 * These are separate from kotlin.text to avoid conflicts during bootstrap compilation.
 */

/**
 * Converts the characters in the specified array to a string.
 */
public fun brsStringFromChars(chars: CharArray): String {
    var result = ""
    for (char in chars) {
        result += char
    }
    return result
}

/**
 * Converts the characters from a portion of the specified array to a string.
 *
 * @throws IndexOutOfBoundsException if either [offset] or [length] are less than zero
 * or `offset + length` is out of [chars] array bounds.
 */
public fun brsStringFromChars(chars: CharArray, offset: Int, length: Int): String {
    if (offset < 0 || length < 0 || chars.size - offset < length)
        throw IndexOutOfBoundsException("size: ${chars.size}; offset: $offset; length: $length")
    var result = ""
    for (index in offset until offset + length) {
        result += chars[index]
    }
    return result
}

/**
 * Concatenates characters in this [CharArray] into a String.
 */
public fun CharArray.brsConcatToString(): String {
    var result = ""
    for (char in this) {
        result += char
    }
    return result
}

/**
 * Concatenates characters in this [CharArray] or its subrange into a String.
 *
 * @param startIndex the beginning (inclusive) of the subrange of characters, 0 by default.
 * @param endIndex the end (exclusive) of the subrange of characters, size of this array by default.
 *
 * @throws IndexOutOfBoundsException if [startIndex] is less than zero or [endIndex] is greater than the size of this array.
 * @throws IllegalArgumentException if [startIndex] is greater than [endIndex].
 */
public fun CharArray.brsConcatToString(startIndex: Int = 0, endIndex: Int = this.size): String {
    checkBoundsIndexes(startIndex, endIndex, this.size)
    var result = ""
    for (index in startIndex until endIndex) {
        result += this[index]
    }
    return result
}

/**
 * Returns a [CharArray] containing characters of this string.
 */
public fun String.brsToCharArray(): CharArray {
    return CharArray(length) { get(it) }
}

/**
 * Returns a [CharArray] containing characters of this string or its substring.
 *
 * @param startIndex the beginning (inclusive) of the substring, 0 by default.
 * @param endIndex the end (exclusive) of the substring, length of this string by default.
 *
 * @throws IndexOutOfBoundsException if [startIndex] is less than zero or [endIndex] is greater than the length of this string.
 * @throws IllegalArgumentException if [startIndex] is greater than [endIndex].
 */
public fun String.brsToCharArray(startIndex: Int = 0, endIndex: Int = this.length): CharArray {
    checkBoundsIndexes(startIndex, endIndex, length)
    return CharArray(endIndex - startIndex) { get(startIndex + it) }
}

/**
 * Returns a copy of this string converted to upper case.
 */
public fun String.brsUppercase(): String {
    var result = ""
    for (char in this) {
        result += char.uppercaseChar()
    }
    return result
}

/**
 * Returns a copy of this string converted to lower case.
 */
public fun String.brsLowercase(): String {
    var result = ""
    for (char in this) {
        result += char.lowercaseChar()
    }
    return result
}

/**
 * Compares two strings lexicographically, optionally ignoring case differences.
 */
public fun String.brsCompareTo(other: String, ignoreCase: Boolean = false): Int {
    if (ignoreCase) {
        val n1 = this.length
        val n2 = other.length
        val min = minOf(n1, n2)
        if (min == 0) return n1 - n2
        for (index in 0 until min) {
            var thisChar = this[index]
            var otherChar = other[index]

            if (thisChar != otherChar) {
                thisChar = thisChar.uppercaseChar()
                otherChar = otherChar.uppercaseChar()

                if (thisChar != otherChar) {
                    thisChar = thisChar.lowercaseChar()
                    otherChar = otherChar.lowercaseChar()

                    if (thisChar != otherChar) {
                        return thisChar.compareTo(otherChar)
                    }
                }
            }
        }
        return n1 - n2
    } else {
        return compareTo(other)
    }
}

/**
 * Returns `true` if the contents of this char sequence are equal to the contents of the specified [other].
 */
public infix fun CharSequence?.brsContentEquals(other: CharSequence?): Boolean {
    if (this === other) return true
    if (this == null || other == null) return false
    if (this.length != other.length) return false
    for (i in this.indices) {
        if (this[i] != other[i]) return false
    }
    return true
}

/**
 * Returns `true` if the contents of this char sequence are equal to the contents of the specified [other],
 * optionally ignoring case difference.
 */
public fun CharSequence?.brsContentEquals(other: CharSequence?, ignoreCase: Boolean): Boolean {
    if (!ignoreCase) return this brsContentEquals other
    if (this === other) return true
    if (this == null || other == null) return false
    if (this.length != other.length) return false
    for (i in this.indices) {
        if (!this[i].equals(other[i], ignoreCase = true)) return false
    }
    return true
}

/**
 * Case-insensitive string comparator for BrightScript.
 */
public val BRS_CASE_INSENSITIVE_ORDER: Comparator<String> = Comparator { a, b ->
    a.brsCompareTo(b, ignoreCase = true)
}

// Internal helper function
private fun checkBoundsIndexes(startIndex: Int, endIndex: Int, size: Int) {
    if (startIndex < 0 || endIndex > size) {
        throw IndexOutOfBoundsException("startIndex: $startIndex, endIndex: $endIndex, size: $size")
    }
    if (startIndex > endIndex) {
        throw IllegalArgumentException("startIndex: $startIndex > endIndex: $endIndex")
    }
}
