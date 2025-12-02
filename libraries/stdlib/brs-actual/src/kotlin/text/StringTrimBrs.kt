/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.text

/**
 * Returns a string with leading and trailing whitespace removed.
 */
public fun CharSequence.trim(): CharSequence {
    return trim { it.isWhitespace() }
}

/**
 * Returns a string with leading and trailing whitespace removed.
 */
public inline fun String.trim(): String {
    return (this as CharSequence).trim().toString()
}

/**
 * Returns a string with leading whitespace removed.
 */
public fun CharSequence.trimStart(): CharSequence {
    return trimStart { it.isWhitespace() }
}

/**
 * Returns a string with leading whitespace removed.
 */
public inline fun String.trimStart(): String {
    return (this as CharSequence).trimStart().toString()
}

/**
 * Returns a string with trailing whitespace removed.
 */
public fun CharSequence.trimEnd(): CharSequence {
    return trimEnd { it.isWhitespace() }
}

/**
 * Returns a string with trailing whitespace removed.
 */
public inline fun String.trimEnd(): String {
    return (this as CharSequence).trimEnd().toString()
}

/**
 * Returns a string with leading and trailing characters matching the [predicate] removed.
 *
 * @param predicate A function that returns `true` for characters to be trimmed.
 */
public fun CharSequence.trim(predicate: (Char) -> Boolean): CharSequence {
    var startIndex = 0
    var endIndex = this.length - 1

    // Find first non-matching character from start
    while (startIndex <= endIndex && predicate(this[startIndex])) {
        startIndex++
    }

    // Find first non-matching character from end
    while (endIndex >= startIndex && predicate(this[endIndex])) {
        endIndex--
    }

    return this.subSequence(startIndex, endIndex + 1)
}

/**
 * Returns a string with leading and trailing characters matching the [predicate] removed.
 *
 * @param predicate A function that returns `true` for characters to be trimmed.
 */
public inline fun String.trim(noinline predicate: (Char) -> Boolean): String {
    return (this as CharSequence).trim(predicate).toString()
}

/**
 * Returns a string with leading characters matching the [predicate] removed.
 *
 * @param predicate A function that returns `true` for characters to be trimmed.
 */
public fun CharSequence.trimStart(predicate: (Char) -> Boolean): CharSequence {
    var startIndex = 0

    // Find first non-matching character from start
    while (startIndex < this.length && predicate(this[startIndex])) {
        startIndex++
    }

    return this.subSequence(startIndex, this.length)
}

/**
 * Returns a string with leading characters matching the [predicate] removed.
 *
 * @param predicate A function that returns `true` for characters to be trimmed.
 */
public inline fun String.trimStart(noinline predicate: (Char) -> Boolean): String {
    return (this as CharSequence).trimStart(predicate).toString()
}

/**
 * Returns a string with trailing characters matching the [predicate] removed.
 *
 * @param predicate A function that returns `true` for characters to be trimmed.
 */
public fun CharSequence.trimEnd(predicate: (Char) -> Boolean): CharSequence {
    var endIndex = this.length - 1

    // Find first non-matching character from end
    while (endIndex >= 0 && predicate(this[endIndex])) {
        endIndex--
    }

    return this.subSequence(0, endIndex + 1)
}

/**
 * Returns a string with trailing characters matching the [predicate] removed.
 *
 * @param predicate A function that returns `true` for characters to be trimmed.
 */
public inline fun String.trimEnd(noinline predicate: (Char) -> Boolean): String {
    return (this as CharSequence).trimEnd(predicate).toString()
}

/**
 * Returns a string with leading and trailing occurrences of characters in [chars] removed.
 *
 * @param chars A set of characters to trim.
 */
public fun CharSequence.trim(vararg chars: Char): CharSequence {
    return trim { char ->
        var found = false
        var i = 0
        while (i < chars.size) {
            if (char == chars[i]) {
                found = true
                break
            }
            i++
        }
        found
    }
}

/**
 * Returns a string with leading and trailing occurrences of characters in [chars] removed.
 *
 * @param chars A set of characters to trim.
 */
public fun String.trim(vararg chars: Char): String {
    return (this as CharSequence).trim(*chars).toString()
}

/**
 * Returns a string with leading occurrences of characters in [chars] removed.
 *
 * @param chars A set of characters to trim.
 */
public fun CharSequence.trimStart(vararg chars: Char): CharSequence {
    return trimStart { char ->
        var found = false
        var i = 0
        while (i < chars.size) {
            if (char == chars[i]) {
                found = true
                break
            }
            i++
        }
        found
    }
}

/**
 * Returns a string with leading occurrences of characters in [chars] removed.
 *
 * @param chars A set of characters to trim.
 */
public fun String.trimStart(vararg chars: Char): String {
    return (this as CharSequence).trimStart(*chars).toString()
}

/**
 * Returns a string with trailing occurrences of characters in [chars] removed.
 *
 * @param chars A set of characters to trim.
 */
public fun CharSequence.trimEnd(vararg chars: Char): CharSequence {
    return trimEnd { char ->
        var found = false
        var i = 0
        while (i < chars.size) {
            if (char == chars[i]) {
                found = true
                break
            }
            i++
        }
        found
    }
}

/**
 * Returns a string with trailing occurrences of characters in [chars] removed.
 *
 * @param chars A set of characters to trim.
 */
public fun String.trimEnd(vararg chars: Char): String {
    return (this as CharSequence).trimEnd(*chars).toString()
}

/**
 * Returns a string with indentation removed from each line.
 *
 * This function detects a common minimal indent of all non-blank lines and removes it.
 */
public fun String.trimIndent(): String {
    return replaceIndent("")
}

/**
 * Detects a common minimal indent of all the input lines, removes it from every line,
 * and also removes the first and the last lines if they are blank.
 *
 * @param newIndent A string to be used as a new indent. By default, empty string is used.
 */
public fun String.replaceIndent(newIndent: String = ""): String {
    val lines = this.lines()

    // Find minimum indent (ignoring blank lines)
    var minIndent = Int.MAX_VALUE
    var lineIndex = 0
    while (lineIndex < lines.size) {
        val line = lines[lineIndex]
        var indent = 0
        while (indent < line.length && line[indent].isWhitespace()) {
            indent++
        }
        if (indent < line.length && indent < minIndent) {
            minIndent = indent
        }
        lineIndex++
    }

    if (minIndent == Int.MAX_VALUE) {
        // All lines are blank
        minIndent = 0
    }

    val result = StringBuilder()
    var firstLine = true

    var i = 0
    while (i < lines.size) {
        val line = lines[i]

        // Skip first line if blank
        if (i == 0 && line.isBlank()) {
            i++
            continue
        }

        // Skip last line if blank
        if (i == lines.size - 1 && line.isBlank()) {
            i++
            continue
        }

        if (!firstLine) {
            result.append('\n')
        }
        firstLine = false

        if (line.isNotBlank()) {
            result.append(newIndent)
            val substringStart = if (minIndent < line.length) minIndent else line.length
            result.append(line.substring(substringStart))
        }
        i++
    }

    return result.toString()
}

/**
 * Returns a string with margin prefix removed from each line.
 *
 * @param marginPrefix The string used as margin prefix. Default is "|".
 */
public fun String.trimMargin(marginPrefix: String = "|"): String {
    return replaceIndentByMargin("", marginPrefix)
}

/**
 * Detects indent by [marginPrefix] in each line, removes it, and replaces it with [newIndent].
 *
 * @param newIndent A string to be used as a new indent. By default, empty string is used.
 * @param marginPrefix The string used as margin prefix. Default is "|".
 */
public fun String.replaceIndentByMargin(newIndent: String = "", marginPrefix: String = "|"): String {
    val lines = this.lines()
    val result = StringBuilder()
    var firstLine = true

    var i = 0
    while (i < lines.size) {
        val line = lines[i]

        // Skip first line if blank
        if (i == 0 && line.isBlank()) {
            i++
            continue
        }

        // Skip last line if blank
        if (i == lines.size - 1 && line.isBlank()) {
            i++
            continue
        }

        if (!firstLine) {
            result.append('\n')
        }
        firstLine = false

        val marginIndex = line.indexOf(marginPrefix)
        if (marginIndex >= 0) {
            // Check if everything before margin is whitespace
            val beforeMargin = line.substring(0, marginIndex)
            var allWhitespace = true
            var j = 0
            while (j < beforeMargin.length) {
                if (!beforeMargin[j].isWhitespace()) {
                    allWhitespace = false
                    break
                }
                j++
            }
            if (allWhitespace) {
                result.append(newIndent)
                result.append(line.substring(marginIndex + marginPrefix.length))
                i++
                continue
            }
        }

        // No margin found or not all whitespace before margin
        result.append(line)
        i++
    }

    return result.toString()
}
