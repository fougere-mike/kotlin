/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.text

/**
 * Splits this char sequence to a list of strings around occurrences of the specified [delimiter].
 *
 * @param delimiter One or more characters to be used as a delimiter.
 * @param ignoreCase `true` to ignore character case when matching a delimiter. By default `false`.
 * @param limit The maximum number of substrings to return. Zero by default means no limit is set.
 */
public fun CharSequence.split(delimiter: String, ignoreCase: Boolean = false, limit: Int = 0): List<String> {
    if (limit < 0) {
        throw IllegalArgumentException("Limit must be non-negative, but was $limit")
    }

    val str = this.toString()
    if (delimiter.isEmpty()) {
        // Empty delimiter: split into individual characters
        val result = ArrayList<String>()
        val maxChars = if (limit == 0 || limit > str.length) str.length else limit
        for (i in 0 until maxChars - 1) {
            result.add(str[i].toString())
        }
        if (maxChars > 0 && maxChars <= str.length) {
            result.add(str.substring(maxChars - 1))
        }
        return result
    }

    val result = ArrayList<String>()
    var currentIndex = 0
    var matchCount = 0

    while (currentIndex < str.length) {
        // Check if we've reached the limit (need to keep last part)
        if (limit > 0 && matchCount >= limit - 1) {
            result.add(str.substring(currentIndex))
            break
        }

        val nextIndex = str.indexOf(delimiter, currentIndex, ignoreCase)

        if (nextIndex < 0) {
            // No more delimiters found, add remaining string
            result.add(str.substring(currentIndex))
            break
        }

        // Add the part before the delimiter
        result.add(str.substring(currentIndex, nextIndex))
        currentIndex = nextIndex + delimiter.length
        matchCount++
    }

    // If string ends with delimiter, add empty string at the end
    if (currentIndex == str.length && str.endsWith(delimiter, ignoreCase)) {
        result.add("")
    }

    return result
}

/**
 * Splits this char sequence to a list of strings around occurrences of the specified [delimiter].
 *
 * @param delimiter Character to be used as a delimiter.
 * @param ignoreCase `true` to ignore character case when matching a delimiter. By default `false`.
 * @param limit The maximum number of substrings to return.
 */
public fun CharSequence.split(delimiter: Char, ignoreCase: Boolean = false, limit: Int = 0): List<String> {
    return split(delimiter.toString(), ignoreCase, limit)
}

/**
 * Splits this char sequence to a list of strings around occurrences of any of the specified [delimiters].
 *
 * @param delimiters One or more characters to be used as delimiters.
 * @param ignoreCase `true` to ignore character case when matching a delimiter. By default `false`.
 * @param limit The maximum number of substrings to return. Zero by default means no limit is set.
 */
public fun CharSequence.split(vararg delimiters: Char, ignoreCase: Boolean = false, limit: Int = 0): List<String> {
    if (limit < 0) {
        throw IllegalArgumentException("Limit must be non-negative, but was $limit")
    }

    if (delimiters.size == 0) {
        return listOf(this.toString())
    }

    val str = this.toString()
    val result = ArrayList<String>()
    var currentIndex = 0
    var matchCount = 0

    while (currentIndex < str.length) {
        // Check if we've reached the limit
        if (limit > 0 && matchCount >= limit - 1) {
            result.add(str.substring(currentIndex))
            break
        }

        // Find the next occurrence of any delimiter
        var nextIndex = -1
        var matchedDelimiterLength = 1

        var delimIndex = 0
        while (delimIndex < delimiters.size) {
            val delimiter = delimiters[delimIndex]
            val index = str.indexOf(delimiter.toString(), currentIndex, ignoreCase)
            if (index >= 0 && (nextIndex < 0 || index < nextIndex)) {
                nextIndex = index
            }
            delimIndex++
        }

        if (nextIndex < 0) {
            // No more delimiters found
            result.add(str.substring(currentIndex))
            break
        }

        // Add the part before the delimiter
        result.add(str.substring(currentIndex, nextIndex))
        currentIndex = nextIndex + matchedDelimiterLength
        matchCount++
    }

    // If string ends with a delimiter, add empty string
    if (currentIndex == str.length) {
        var delimIndex = 0
        while (delimIndex < delimiters.size) {
            val delimiter = delimiters[delimIndex]
            if (str.endsWith(delimiter.toString(), ignoreCase)) {
                result.add("")
                break
            }
            delimIndex++
        }
    }

    return result
}

/**
 * Splits this char sequence to a list of strings around occurrences of any of the specified [delimiters].
 *
 * @param delimiters One or more strings to be used as delimiters.
 * @param ignoreCase `true` to ignore character case when matching a delimiter. By default `false`.
 * @param limit The maximum number of substrings to return. Zero by default means no limit is set.
 */
public fun CharSequence.split(vararg delimiters: String, ignoreCase: Boolean = false, limit: Int = 0): List<String> {
    if (limit < 0) {
        throw IllegalArgumentException("Limit must be non-negative, but was $limit")
    }

    if (delimiters.size == 0) {
        return listOf(this.toString())
    }

    val str = this.toString()
    val result = ArrayList<String>()
    var currentIndex = 0
    var matchCount = 0

    while (currentIndex < str.length) {
        // Check if we've reached the limit
        if (limit > 0 && matchCount >= limit - 1) {
            result.add(str.substring(currentIndex))
            break
        }

        // Find the next occurrence of any delimiter
        var nextIndex = -1
        var matchedDelimiterLength = 0

        var delimIndex = 0
        while (delimIndex < delimiters.size) {
            val delimiter = delimiters[delimIndex]
            if (delimiter.isEmpty()) {
                delimIndex++
                continue
            }
            val index = str.indexOf(delimiter, currentIndex, ignoreCase)
            if (index >= 0 && (nextIndex < 0 || index < nextIndex)) {
                nextIndex = index
                matchedDelimiterLength = delimiter.length
            }
            delimIndex++
        }

        if (nextIndex < 0) {
            // No more delimiters found
            result.add(str.substring(currentIndex))
            break
        }

        // Add the part before the delimiter
        result.add(str.substring(currentIndex, nextIndex))
        currentIndex = nextIndex + matchedDelimiterLength
        matchCount++
    }

    // If string ends with a delimiter, add empty string
    if (currentIndex == str.length) {
        var delimIndex = 0
        while (delimIndex < delimiters.size) {
            val delimiter = delimiters[delimIndex]
            if (str.endsWith(delimiter, ignoreCase)) {
                result.add("")
                break
            }
            delimIndex++
        }
    }

    return result
}

/**
 * Splits this char sequence to a list of lines delimited by any of the following character sequences:
 * CRLF, LF or CR.
 */
public fun CharSequence.lines(): List<String> = split("\r\n", "\n", "\r")
