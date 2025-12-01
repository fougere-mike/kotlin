/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs.roku

import kotlin.brs.*

/**
 * Options for Kotlin-style regex matching using roRegex.
 *
 * These map to roRegex flags in BrightScript.
 */
public enum class BrsRegexOption(internal val flag: String) {
    /** Enables case-insensitive matching (roRegex "i" flag). */
    IGNORE_CASE("i"),

    /** Enables multiline mode (roRegex "m" flag). */
    MULTILINE("m"),

    /** Enables dotall mode - dot matches newlines (roRegex "s" flag). */
    DOT_MATCHES_ALL("s"),

    /** Enables extended mode - whitespace is ignored (roRegex "x" flag). */
    COMMENTS("x")
}

/**
 * Kotlin-idiomatic wrapper for BrightScript roRegex.
 *
 * Provides a more Kotlin-like API for regular expression operations on Roku.
 *
 * ```kotlin
 * val regex = BrsRegex("\\d+")
 * if (regex.matches("12345")) {
 *     println("All digits!")
 * }
 *
 * val result = BrsRegex("[a-z]+", BrsRegexOption.IGNORE_CASE).find("Hello World")
 * println(result?.value) // "Hello"
 * ```
 *
 * @property pattern The regex pattern string.
 * @property options The set of regex options.
 */
public class BrsRegex(
    public val pattern: String,
    public val options: Set<BrsRegexOption> = emptySet()
) {
    private val roRegex: RoRegex

    init {
        val flags = options.joinToString("") { it.flag }
        roRegex = RoRegex(pattern, flags)
    }

    /**
     * Creates a BrsRegex with a single option.
     */
    public constructor(pattern: String, option: BrsRegexOption) : this(pattern, setOf(option))

    /**
     * Indicates whether the regular expression matches the entire [input].
     *
     * @param input The string to match against.
     * @return True if the entire input matches the pattern.
     */
    public infix fun matches(input: CharSequence): Boolean {
        // roRegex.isMatch returns true if pattern matches anywhere in string
        // For full match, we need to check if the match covers the entire string
        val matchResult = roRegex.match(input.toString())
        if (matchResult.count() == 0) return false

        val match = matchResult[0] as? String ?: return false
        return match == input.toString()
    }

    /**
     * Indicates whether the regular expression can find at least one match in the [input].
     *
     * @param input The string to search.
     * @return True if a match is found.
     */
    public fun containsMatchIn(input: CharSequence): Boolean {
        return roRegex.isMatch(input.toString())
    }

    /**
     * Returns the first match of a regular expression in the [input].
     *
     * @param input The string to search.
     * @return A [BrsMatchResult] if a match was found, or null otherwise.
     */
    public fun find(input: CharSequence): BrsMatchResult? {
        val result = roRegex.match(input.toString())
        if (result.count() == 0) return null

        val match = result[0] as? String ?: return null
        val groups = mutableListOf<String?>()
        for (i in 0 until result.count()) {
            groups.add(result[i] as? String)
        }

        // Find the index of the match
        val index = input.toString().indexOf(match)
        return BrsMatchResult(match, groups, index, input.toString())
    }

    /**
     * Returns a list of all occurrences of a regular expression in the [input].
     *
     * @param input The string to search.
     * @return A list of [BrsMatchResult] for all matches.
     */
    public fun findAll(input: CharSequence): List<BrsMatchResult> {
        val results = mutableListOf<BrsMatchResult>()
        val allMatches = roRegex.matchAll(input.toString())

        for (i in 0 until allMatches.count()) {
            val matchArray = allMatches[i] as? RoArray ?: continue
            if (matchArray.count() == 0) continue

            val match = matchArray[0] as? String ?: continue
            val groups = mutableListOf<String?>()
            for (j in 0 until matchArray.count()) {
                groups.add(matchArray[j] as? String)
            }

            // Find the index (approximate - roRegex doesn't provide indices)
            val previousText = results.lastOrNull()?.let {
                input.substring(it.range.last + 1)
            } ?: input.toString()
            val index = previousText.indexOf(match) +
                (results.lastOrNull()?.range?.last?.plus(1) ?: 0)

            results.add(BrsMatchResult(match, groups, index, input.toString()))
        }

        return results
    }

    /**
     * Replaces the first occurrence of this regular expression in [input] with [replacement].
     *
     * @param input The input string.
     * @param replacement The replacement string. Can contain backreferences like $1, $2.
     * @return The resulting string after replacement.
     */
    public fun replaceFirst(input: CharSequence, replacement: String): String {
        return roRegex.replace(input.toString(), replacement)
    }

    /**
     * Replaces all occurrences of this regular expression in [input] with [replacement].
     *
     * @param input The input string.
     * @param replacement The replacement string. Can contain backreferences like $1, $2.
     * @return The resulting string after all replacements.
     */
    public fun replace(input: CharSequence, replacement: String): String {
        return roRegex.replaceAll(input.toString(), replacement)
    }

    /**
     * Splits the [input] string around matches of this regular expression.
     *
     * @param input The string to split.
     * @return A list of strings resulting from the split.
     */
    public fun split(input: CharSequence): List<String> {
        val result = roRegex.split(input.toString())
        val list = mutableListOf<String>()
        for (i in 0 until result.count()) {
            (result[i] as? String)?.let { list.add(it) }
        }
        return list
    }

    override fun toString(): String = pattern
}

/**
 * Represents the result of a single match of a regular expression.
 *
 * @property value The matched string.
 * @property groupValues The values of all capturing groups (including group 0 - the entire match).
 * @property range The range of indices in the original string where the match was found.
 */
public class BrsMatchResult(
    public val value: String,
    public val groupValues: List<String?>,
    private val startIndex: Int,
    private val input: String
) {
    /**
     * The range of indices in the original string where the match was found.
     */
    public val range: IntRange
        get() = startIndex until (startIndex + value.length)

    /**
     * Gets the value of the group at the specified index.
     *
     * @param index The group index (0 = entire match, 1 = first group, etc.)
     * @return The matched string for that group, or null if the group didn't participate.
     */
    public operator fun get(index: Int): String? = groupValues.getOrNull(index)

    /**
     * Returns a string representation of this match result.
     */
    override fun toString(): String = "BrsMatchResult(value=$value, range=$range)"
}

// ============================================
// Extension functions for String
// ============================================

/**
 * Returns true if this string matches the given [pattern].
 *
 * @param pattern The regex pattern.
 * @return True if the entire string matches the pattern.
 */
public fun String.matchesBrsRegex(pattern: String): Boolean {
    return BrsRegex(pattern).matches(this)
}

/**
 * Returns true if this string contains a match of the given [pattern].
 *
 * @param pattern The regex pattern.
 * @return True if the pattern is found anywhere in the string.
 */
public fun String.containsBrsRegex(pattern: String): Boolean {
    return BrsRegex(pattern).containsMatchIn(this)
}

/**
 * Replaces the first occurrence of [pattern] with [replacement].
 *
 * @param pattern The regex pattern.
 * @param replacement The replacement string.
 * @return The resulting string.
 */
public fun String.replaceBrsRegex(pattern: String, replacement: String): String {
    return BrsRegex(pattern).replaceFirst(this, replacement)
}

/**
 * Replaces all occurrences of [pattern] with [replacement].
 *
 * @param pattern The regex pattern.
 * @param replacement The replacement string.
 * @return The resulting string.
 */
public fun String.replaceAllBrsRegex(pattern: String, replacement: String): String {
    return BrsRegex(pattern).replace(this, replacement)
}

/**
 * Splits this string around matches of the given [pattern].
 *
 * @param pattern The regex pattern.
 * @return A list of strings.
 */
public fun String.splitBrsRegex(pattern: String): List<String> {
    return BrsRegex(pattern).split(this)
}
