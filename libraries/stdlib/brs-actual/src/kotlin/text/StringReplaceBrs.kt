/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.text

/**
 * Replaces all occurrences of the specified [oldValue] with the specified [newValue].
 *
 * @param oldValue The substring to be replaced.
 * @param newValue The replacement substring.
 * @param ignoreCase `true` to ignore character case when matching. By default `false`.
 * @return A new string with all occurrences replaced.
 */
public fun String.replace(oldValue: String, newValue: String, ignoreCase: Boolean = false): String {
    if (oldValue.isEmpty()) {
        // Replace empty string: insert newValue between every character
        val result = StringBuilder()
        result.append(newValue)
        for (i in 0 until this.length) {
            result.append(this[i])
            result.append(newValue)
        }
        return result.toString()
    }

    var currentIndex = 0
    val result = StringBuilder()

    while (currentIndex < this.length) {
        val nextIndex = this.indexOf(oldValue, currentIndex, ignoreCase)

        if (nextIndex < 0) {
            // No more occurrences found, append remaining string
            result.append(this.substring(currentIndex))
            break
        }

        // Append the part before the match
        result.append(this.substring(currentIndex, nextIndex))
        // Append the replacement
        result.append(newValue)
        // Move past the matched substring
        currentIndex = nextIndex + oldValue.length
    }

    return result.toString()
}

/**
 * Replaces all occurrences of the specified [oldChar] with the specified [newChar].
 *
 * @param oldChar The character to be replaced.
 * @param newChar The replacement character.
 * @param ignoreCase `true` to ignore character case when matching. By default `false`.
 * @return A new string with all occurrences replaced.
 */
public fun String.replace(oldChar: Char, newChar: Char, ignoreCase: Boolean = false): String {
    return this.replace(oldChar.toString(), newChar.toString(), ignoreCase)
}

/**
 * Replaces the first occurrence of the specified [oldValue] with the specified [newValue].
 *
 * @param oldValue The substring to be replaced.
 * @param newValue The replacement substring.
 * @param ignoreCase `true` to ignore character case when matching. By default `false`.
 * @return A new string with the first occurrence replaced, or the original string if no match found.
 */
public fun String.replaceFirst(oldValue: String, newValue: String, ignoreCase: Boolean = false): String {
    val index = this.indexOf(oldValue, 0, ignoreCase)

    if (index < 0) {
        // No occurrence found
        return this
    }

    return this.substring(0, index) + newValue + this.substring(index + oldValue.length)
}

/**
 * Replaces the first occurrence of the specified [oldChar] with the specified [newChar].
 *
 * @param oldChar The character to be replaced.
 * @param newChar The replacement character.
 * @param ignoreCase `true` to ignore character case when matching. By default `false`.
 * @return A new string with the first occurrence replaced, or the original string if no match found.
 */
public fun String.replaceFirst(oldChar: Char, newChar: Char, ignoreCase: Boolean = false): String {
    return this.replaceFirst(oldChar.toString(), newChar.toString(), ignoreCase)
}

/**
 * Replaces the part of this string at the specified [range] with the specified [replacement].
 *
 * @param range The range of indices to replace.
 * @param replacement The replacement string.
 * @return A new string with the specified range replaced.
 */
public fun String.replaceRange(range: IntRange, replacement: CharSequence): String {
    return replaceRange(range.start, range.endInclusive + 1, replacement)
}

/**
 * Replaces the part of this string from [startIndex] (inclusive) to [endIndex] (exclusive) with [replacement].
 *
 * @param startIndex The start index (inclusive) of the range to replace.
 * @param endIndex The end index (exclusive) of the range to replace.
 * @param replacement The replacement string.
 * @return A new string with the specified range replaced.
 */
public fun String.replaceRange(startIndex: Int, endIndex: Int, replacement: CharSequence): String {
    if (endIndex < startIndex) {
        throw IndexOutOfBoundsException("End index ($endIndex) is less than start index ($startIndex)")
    }
    if (startIndex < 0) {
        throw IndexOutOfBoundsException("Start index ($startIndex) is negative")
    }
    if (endIndex > this.length) {
        throw IndexOutOfBoundsException("End index ($endIndex) is greater than length (${this.length})")
    }

    return this.substring(0, startIndex) + replacement.toString() + this.substring(endIndex)
}

/**
 * Replaces the part of this CharSequence at the specified [range] with the specified [replacement].
 *
 * @param range The range of indices to replace.
 * @param replacement The replacement string.
 * @return A new string with the specified range replaced.
 */
public fun CharSequence.replaceRange(range: IntRange, replacement: CharSequence): String {
    return this.toString().replaceRange(range, replacement)
}

/**
 * Replaces the part of this CharSequence from [startIndex] (inclusive) to [endIndex] (exclusive) with [replacement].
 *
 * @param startIndex The start index (inclusive) of the range to replace.
 * @param endIndex The end index (exclusive) of the range to replace.
 * @param replacement The replacement string.
 * @return A new string with the specified range replaced.
 */
public fun CharSequence.replaceRange(startIndex: Int, endIndex: Int, replacement: CharSequence): String {
    return this.toString().replaceRange(startIndex, endIndex, replacement)
}
