/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.text

/**
 * BrightScript roRegex wrapper interface.
 * Represents the native BrightScript roRegex component with PCRE regex support.
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
private external interface BrsRegex {
    /**
     * Returns true if the string matches the pattern.
     */
    fun IsMatch(str: String): Boolean

    /**
     * Returns an array with the matched groups.
     * Index 0 is the full match, indices 1+ are captured groups.
     * Returns an empty array if no match.
     */
    fun Match(str: String): Array<String>

    /**
     * Replaces the first occurrence of the pattern in the string.
     * Supports backreferences: \1, \2, etc.
     */
    fun Replace(str: String, replacement: String): String

    /**
     * Replaces all occurrences of the pattern in the string.
     * Supports backreferences: \1, \2, etc.
     */
    fun ReplaceAll(str: String, replacement: String): String

    /**
     * Splits the string by the pattern delimiter.
     * Returns a list of strings.
     */
    fun Split(str: String): Array<String>
}

/**
 * Creates a BrightScript roRegex object.
 */
private external fun brsIntrinsicCreateObject(className: String, pattern: String, flags: String): BrsRegex

/**
 * Provides enumeration values to use to set regular expression options.
 */
public actual enum class RegexOption(internal val flag: String) {
    /** Enables case-insensitive matching. */
    IGNORE_CASE("i"),

    /** Enables multiline mode.
     *
     * In multiline mode the expressions `^` and `$` match just after or just before,
     * respectively, a line terminator or the end of the input sequence. */
    MULTILINE("m")
}

/**
 * Represents the results from a single capturing group within a [MatchResult] of [Regex].
 *
 * @param value The value of captured group.
 */
public actual data class MatchGroup(public actual val value: String)

/**
 * Represents a compiled regular expression.
 * Provides functions to match strings in text with a pattern, replace the found occurrences and split text around matches.
 *
 * This implementation wraps BrightScript's native roRegex component which provides PCRE (Perl Compatible Regular Expressions) support.
 *
 * For pattern syntax reference see BrightScript roRegex documentation:
 * - https://developer.roku.com/docs/references/brightscript/components/roregex.md
 * - https://docs.brightsign.biz/developers/roregex
 *
 * @constructor Creates a regular expression from the specified [pattern] string and the specified set of [options].
 */
@Suppress("NO_ACTUAL_CLASS_MEMBER_FOR_EXPECTED_CLASS")
public actual class Regex public actual constructor(pattern: String, options: Set<RegexOption>) {

    /** Creates a regular expression from the specified [pattern] string and the specified single [option].  */
    public actual constructor(pattern: String, option: RegexOption) : this(pattern, setOf(option))

    /** Creates a regular expression from the specified [pattern] string and the default options.  */
    public actual constructor(pattern: String) : this(pattern, emptySet())

    /** The pattern string of this regular expression. */
    public actual val pattern: String = pattern

    /** The set of options that were used to create this regular expression. */
    public actual val options: Set<RegexOption> = options.toSet()

    /** The native BrightScript roRegex object. */
    private val nativePattern: BrsRegex = brsIntrinsicCreateObject(
        "roRegex",
        pattern,
        options.joinToString("") { it.flag }
    )

    /** Indicates whether the regular expression matches the entire [input]. */
    public actual infix fun matches(input: CharSequence): Boolean {
        val inputStr = input.toString()

        // For matches(), we need to ensure the pattern matches the ENTIRE string
        // We do this by checking if a match exists and if it covers the entire input
        val match = nativePattern.Match(inputStr)
        if (match.isEmpty()) return false

        // Check if the full match (match[0]) equals the entire input
        return match[0] == inputStr
    }

    /** Indicates whether the regular expression can find at least one match in the specified [input]. */
    public actual fun containsMatchIn(input: CharSequence): Boolean {
        return nativePattern.IsMatch(input.toString())
    }

    /**
     * Attempts to match the entire [input] CharSequence against the pattern.
     *
     * @return An instance of [MatchResult] if the entire input matches or `null` otherwise.
     */
    public actual fun matchEntire(input: CharSequence): MatchResult? {
        val inputStr = input.toString()
        val match = nativePattern.Match(inputStr)

        if (match.isEmpty()) return null

        // Check if the full match equals the entire input
        if (match[0] != inputStr) return null

        return createMatchResult(inputStr, match, 0)
    }

    /**
     * Attempts to match a regular expression exactly at the specified [index] in the [input] char sequence.
     *
     * Unlike [matchEntire] function, it doesn't require the match to span to the end of [input].
     *
     * @return An instance of [MatchResult] if the input matches this [Regex] at the specified [index] or `null` otherwise.
     * @throws IndexOutOfBoundsException if [index] is less than zero or greater than the length of the [input] char sequence.
     */
    @SinceKotlin("1.7")
    public actual fun matchAt(input: CharSequence, index: Int): MatchResult? {
        if (index < 0 || index > input.length) {
            throw IndexOutOfBoundsException("index out of bounds: $index, input length: ${input.length}")
        }

        // Try to match starting at the specified index
        val inputStr = input.toString()
        val substring = inputStr.substring(index)
        val match = nativePattern.Match(substring)

        if (match.isEmpty()) return null

        // Check if match starts at position 0 of the substring (i.e., at the specified index)
        // roRegex.Match() finds the first match, so we need to verify it's at the start
        val matchIndex = substring.indexOf(match[0])
        if (matchIndex != 0) return null

        return createMatchResult(inputStr, match, index)
    }

    /**
     * Checks if a regular expression matches a part of the specified [input] char sequence
     * exactly at the specified [index].
     *
     * Unlike [matches] function, it doesn't require the match to span to the end of [input].
     *
     * @throws IndexOutOfBoundsException if [index] is less than zero or greater than the length of the [input] char sequence.
     */
    @SinceKotlin("1.7")
    public actual fun matchesAt(input: CharSequence, index: Int): Boolean {
        return matchAt(input, index) != null
    }

    /**
     * Returns the first match of a regular expression in the [input], beginning at the specified [startIndex].
     *
     * @param startIndex An index to start search with, by default 0. Must be not less than zero and not greater than `input.length()`
     * @return An instance of [MatchResult] if match was found or `null` otherwise.
     * @throws IndexOutOfBoundsException if [startIndex] is less than zero or greater than the length of the [input] char sequence.
     */
    @Suppress("ACTUAL_FUNCTION_WITH_DEFAULT_ARGUMENTS")
    public actual fun find(input: CharSequence, startIndex: Int = 0): MatchResult? {
        if (startIndex < 0 || startIndex > input.length) {
            throw IndexOutOfBoundsException("Start index out of bounds: $startIndex, input length: ${input.length}")
        }

        val inputStr = input.toString()

        if (startIndex == 0) {
            // Simple case: search from the beginning
            val match = nativePattern.Match(inputStr)
            if (match.isEmpty()) return null

            val matchIndex = inputStr.indexOf(match[0])
            if (matchIndex < 0) return null

            return createMatchResult(inputStr, match, matchIndex)
        } else {
            // Search from startIndex by using substring
            val substring = inputStr.substring(startIndex)
            val match = nativePattern.Match(substring)
            if (match.isEmpty()) return null

            val matchIndex = substring.indexOf(match[0])
            if (matchIndex < 0) return null

            return createMatchResult(inputStr, match, startIndex + matchIndex)
        }
    }

    /**
     * Returns a sequence of all occurrences of a regular expression within the [input] string, beginning at the specified [startIndex].
     *
     * @throws IndexOutOfBoundsException if [startIndex] is less than zero or greater than the length of the [input] char sequence.
     */
    @Suppress("ACTUAL_FUNCTION_WITH_DEFAULT_ARGUMENTS")
    public actual fun findAll(input: CharSequence, startIndex: Int = 0): Sequence<MatchResult> {
        if (startIndex < 0 || startIndex > input.length) {
            throw IndexOutOfBoundsException("Start index out of bounds: $startIndex, input length: ${input.length}")
        }
        val firstMatch = find(input, startIndex)
        return generateSequence(firstMatch) { match -> match.next() }
    }

    /**
     * Replaces all occurrences of this regular expression in the specified [input] string with specified [replacement] expression.
     *
     * The replacement string may contain references to the captured groups during a match. Occurrences of `${name}` or `$index`
     * in the replacement string will be substituted with the subsequences corresponding to the captured groups with the specified name or index.
     *
     * @param input the char sequence to find matches of this regular expression in
     * @param replacement the expression to replace found matches with
     * @return the result of replacing each occurrence of this regular expression in [input] with the result of evaluating the [replacement] expression
     */
    public actual fun replace(input: CharSequence, replacement: String): String {
        // Use the transform version to handle group substitution
        if (!replacement.contains("\\") && !replacement.contains("$")) {
            // Simple replacement with no group references - use native ReplaceAll
            return nativePattern.ReplaceAll(input.toString(), replacement)
        }

        return replace(input) { substituteGroupRefs(it, replacement) }
    }

    /**
     * Replaces all occurrences of this regular expression in the specified [input] string with the result of
     * the given function [transform] that takes [MatchResult] and returns a string to be used as a
     * replacement for that match.
     */
    public actual fun replace(input: CharSequence, transform: (MatchResult) -> CharSequence): String {
        var match = find(input)
        if (match == null) return input.toString()

        var lastStart = 0
        val length = input.length
        val sb = StringBuilder(length)
        do {
            val foundMatch = match!!
            sb.append(input, lastStart, foundMatch.range.start)
            sb.append(transform(foundMatch))
            lastStart = foundMatch.range.endInclusive + 1
            match = foundMatch.next()
        } while (lastStart < length && match != null)

        if (lastStart < length) {
            sb.append(input, lastStart, length)
        }

        return sb.toString()
    }

    /**
     * Replaces the first occurrence of this regular expression in the specified [input] string with specified [replacement] expression.
     *
     * @param input the char sequence to find a match of this regular expression in
     * @param replacement the expression to replace the found match with
     * @return the result of replacing the first occurrence of this regular expression in [input]
     */
    public actual fun replaceFirst(input: CharSequence, replacement: String): String {
        val match = find(input) ?: return input.toString()

        if (!replacement.contains("\\") && !replacement.contains("$")) {
            // Simple replacement with no group references - use native Replace
            return nativePattern.Replace(input.toString(), replacement)
        }

        val inputStr = input.toString()
        val sb = StringBuilder()
        sb.append(inputStr.substring(0, match.range.first))
        sb.append(substituteGroupRefs(match, replacement))
        sb.append(inputStr.substring(match.range.last + 1, inputStr.length))
        return sb.toString()
    }

    /**
     * Splits the [input] CharSequence to a list of strings around matches of this regular expression.
     *
     * @param limit Non-negative value specifying the maximum number of substrings the string can be split to.
     * Zero by default means no limit is set.
     */
    @Suppress("ACTUAL_FUNCTION_WITH_DEFAULT_ARGUMENTS")
    public actual fun split(input: CharSequence, limit: Int = 0): List<String> {
        requireNonNegativeLimit(limit)
        val allMatches = findAll(input)
        val matches = if (limit == 0) allMatches else allMatches.take(limit - 1)
        val result = mutableListOf<String>()
        var lastStart = 0

        for (match in matches) {
            result.add(input.subSequence(lastStart, match.range.start).toString())
            lastStart = match.range.endInclusive + 1
        }
        result.add(input.subSequence(lastStart, input.length).toString())
        return result
    }

    /**
     * Splits the [input] CharSequence to a sequence of strings around matches of this regular expression.
     *
     * @param limit Non-negative value specifying the maximum number of substrings the string can be split to.
     * Zero by default means no limit is set.
     */
    @SinceKotlin("1.6")
    @Suppress("ACTUAL_FUNCTION_WITH_DEFAULT_ARGUMENTS")
    public actual fun splitToSequence(input: CharSequence, limit: Int = 0): Sequence<String> {
        requireNonNegativeLimit(limit)

        // Use generateSequence instead of sequence builder (coroutines not available in BRS)
        return Sequence {
            object : Iterator<String> {
                var match: MatchResult? = find(input)
                val firstMatch = match
                var nextStart = 0
                var splitCount = 0
                var emitted = false

                override fun hasNext(): Boolean {
                    if (!emitted && (firstMatch == null || limit == 1)) return true
                    if (limit > 0 && splitCount >= limit - 1) return !emitted || nextStart < input.length
                    return nextStart < input.length || (match != null && !emitted)
                }

                override fun next(): String {
                    if (!hasNext()) throw NoSuchElementException()

                    if (!emitted && (firstMatch == null || limit == 1)) {
                        emitted = true
                        return input.toString()
                    }

                    if (match != null && (limit == 0 || splitCount < limit - 1)) {
                        val foundMatch = match!!
                        val result = input.subSequence(nextStart, foundMatch.range.first).toString()
                        nextStart = foundMatch.range.endInclusive + 1
                        match = foundMatch.next()
                        splitCount++
                        emitted = true
                        return result
                    } else {
                        val result = input.subSequence(nextStart, input.length).toString()
                        nextStart = input.length
                        emitted = true
                        return result
                    }
                }
            }
        }
    }

    /**
     * Returns the string representation of this regular expression, namely the [pattern] of this regular expression.
     *
     * Note that another regular expression constructed from the same pattern string may have different [options]
     * and may match strings differently.
     */
    public override fun toString(): String = "Regex($pattern, options=$options)"

    /**
     * Creates a MatchResult from a BrightScript regex match array.
     */
    private fun createMatchResult(input: String, matchArray: Array<String>, matchIndex: Int): MatchResult {
        return object : MatchResult {
            override val range: IntRange = matchIndex until (matchIndex + matchArray[0].length)
            override val value: String = matchArray[0]

            override val groups: MatchGroupCollection = object : MatchGroupCollection {
                override val size: Int get() = matchArray.size

                override fun iterator(): Iterator<MatchGroup?> {
                    return object : Iterator<MatchGroup?> {
                        private var index = 0
                        override fun hasNext(): Boolean = index < size
                        override fun next(): MatchGroup? {
                            if (!hasNext()) throw NoSuchElementException()
                            return get(index++)
                        }
                    }
                }

                override fun get(index: Int): MatchGroup? {
                    if (index >= matchArray.size) return null
                    val groupValue = matchArray[index]
                    // Empty string means the group didn't participate in the match
                    return if (groupValue.isEmpty() && index > 0) null else MatchGroup(groupValue)
                }

                override fun contains(element: MatchGroup?): Boolean {
                    for (i in 0 until size) {
                        if (get(i) == element) return true
                    }
                    return false
                }

                override fun containsAll(elements: Collection<MatchGroup?>): Boolean {
                    for (element in elements) {
                        if (!contains(element)) return false
                    }
                    return true
                }

                override fun isEmpty(): Boolean = size == 0
            }

            private var groupValues_: List<String>? = null

            override val groupValues: List<String>
                get() {
                    if (groupValues_ == null) {
                        groupValues_ = object : List<String> {
                            override val size: Int get() = matchArray.size

                            override fun get(index: Int): String {
                                if (index < 0 || index >= matchArray.size) {
                                    throw IndexOutOfBoundsException("Index $index out of bounds for size $size")
                                }
                                return matchArray[index]
                            }

                            override fun indexOf(element: String): Int {
                                for (i in 0 until size) {
                                    if (matchArray[i] == element) return i
                                }
                                return -1
                            }

                            override fun lastIndexOf(element: String): Int {
                                for (i in size - 1 downTo 0) {
                                    if (matchArray[i] == element) return i
                                }
                                return -1
                            }

                            override fun isEmpty(): Boolean = size == 0

                            override fun contains(element: String): Boolean = indexOf(element) >= 0

                            override fun containsAll(elements: Collection<String>): Boolean {
                                for (element in elements) {
                                    if (!contains(element)) return false
                                }
                                return true
                            }

                            override fun iterator(): Iterator<String> {
                                return object : Iterator<String> {
                                    private var index = 0
                                    override fun hasNext(): Boolean = index < size
                                    override fun next(): String {
                                        if (!hasNext()) throw NoSuchElementException()
                                        return get(index++)
                                    }
                                }
                            }

                            override fun listIterator(): ListIterator<String> = listIterator(0)

                            override fun listIterator(index: Int): ListIterator<String> {
                                if (index < 0 || index > size) {
                                    throw IndexOutOfBoundsException()
                                }
                                return object : ListIterator<String> {
                                    private var currentIndex = index

                                    override fun hasNext(): Boolean = currentIndex < size
                                    override fun hasPrevious(): Boolean = currentIndex > 0
                                    override fun next(): String {
                                        if (!hasNext()) throw NoSuchElementException()
                                        return get(currentIndex++)
                                    }
                                    override fun nextIndex(): Int = currentIndex
                                    override fun previous(): String {
                                        if (!hasPrevious()) throw NoSuchElementException()
                                        return get(--currentIndex)
                                    }
                                    override fun previousIndex(): Int = currentIndex - 1
                                }
                            }

                            override fun subList(fromIndex: Int, toIndex: Int): List<String> {
                                if (fromIndex < 0 || toIndex > size || fromIndex > toIndex) {
                                    throw IndexOutOfBoundsException()
                                }
                                // Capture the outer fromIndex to use in nested subList
                                val outerFromIndex = fromIndex
                                val subSize = toIndex - fromIndex
                                return object : List<String> {
                                    override val size: Int = subSize
                                    override fun get(index: Int): String {
                                        if (index < 0 || index >= size) {
                                            throw IndexOutOfBoundsException()
                                        }
                                        return matchArray[outerFromIndex + index]
                                    }
                                    override fun indexOf(element: String): Int {
                                        for (i in 0 until size) {
                                            if (get(i) == element) return i
                                        }
                                        return -1
                                    }
                                    override fun lastIndexOf(element: String): Int {
                                        for (i in size - 1 downTo 0) {
                                            if (get(i) == element) return i
                                        }
                                        return -1
                                    }
                                    override fun isEmpty(): Boolean = size == 0
                                    override fun contains(element: String): Boolean = indexOf(element) >= 0
                                    override fun containsAll(elements: Collection<String>): Boolean {
                                        for (element in elements) {
                                            if (!contains(element)) return false
                                        }
                                        return true
                                    }
                                    override fun iterator(): Iterator<String> {
                                        return object : Iterator<String> {
                                            private var index = 0
                                            override fun hasNext(): Boolean = index < size
                                            override fun next(): String {
                                                if (!hasNext()) throw NoSuchElementException()
                                                return get(index++)
                                            }
                                        }
                                    }
                                    override fun listIterator(): ListIterator<String> = listIterator(0)
                                    override fun listIterator(index: Int): ListIterator<String> {
                                        if (index < 0 || index > size) {
                                            throw IndexOutOfBoundsException()
                                        }
                                        return object : ListIterator<String> {
                                            private var currentIndex = index
                                            override fun hasNext(): Boolean = currentIndex < size
                                            override fun hasPrevious(): Boolean = currentIndex > 0
                                            override fun next(): String {
                                                if (!hasNext()) throw NoSuchElementException()
                                                return get(currentIndex++)
                                            }
                                            override fun nextIndex(): Int = currentIndex
                                            override fun previous(): String {
                                                if (!hasPrevious()) throw NoSuchElementException()
                                                return get(--currentIndex)
                                            }
                                            override fun previousIndex(): Int = currentIndex - 1
                                        }
                                    }
                                    override fun subList(fromIndex: Int, toIndex: Int): List<String> {
                                        if (fromIndex < 0 || toIndex > size || fromIndex > toIndex) {
                                            throw IndexOutOfBoundsException()
                                        }
                                        // Nested sublist - calculate actual indices
                                        val actualFromIndex = outerFromIndex + fromIndex
                                        val actualToIndex = outerFromIndex + toIndex
                                        val nestedSubSize = actualToIndex - actualFromIndex
                                        return object : List<String> {
                                            override val size: Int = nestedSubSize
                                            override fun get(index: Int): String {
                                                if (index < 0 || index >= size) throw IndexOutOfBoundsException()
                                                return matchArray[actualFromIndex + index]
                                            }
                                            override fun indexOf(element: String): Int {
                                                for (i in 0 until size) {
                                                    if (get(i) == element) return i
                                                }
                                                return -1
                                            }
                                            override fun lastIndexOf(element: String): Int {
                                                for (i in size - 1 downTo 0) {
                                                    if (get(i) == element) return i
                                                }
                                                return -1
                                            }
                                            override fun isEmpty(): Boolean = size == 0
                                            override fun contains(element: String): Boolean = indexOf(element) >= 0
                                            override fun containsAll(elements: Collection<String>): Boolean {
                                                for (element in elements) {
                                                    if (!contains(element)) return false
                                                }
                                                return true
                                            }
                                            override fun iterator(): Iterator<String> {
                                                return object : Iterator<String> {
                                                    private var index = 0
                                                    override fun hasNext(): Boolean = index < size
                                                    override fun next(): String {
                                                        if (!hasNext()) throw NoSuchElementException()
                                                        return get(index++)
                                                    }
                                                }
                                            }
                                            override fun listIterator(): ListIterator<String> = listIterator(0)
                                            override fun listIterator(index: Int): ListIterator<String> {
                                                if (index < 0 || index > size) throw IndexOutOfBoundsException()
                                                return object : ListIterator<String> {
                                                    private var currentIndex = index
                                                    override fun hasNext(): Boolean = currentIndex < size
                                                    override fun hasPrevious(): Boolean = currentIndex > 0
                                                    override fun next(): String {
                                                        if (!hasNext()) throw NoSuchElementException()
                                                        return get(currentIndex++)
                                                    }
                                                    override fun nextIndex(): Int = currentIndex
                                                    override fun previous(): String {
                                                        if (!hasPrevious()) throw NoSuchElementException()
                                                        return get(--currentIndex)
                                                    }
                                                    override fun previousIndex(): Int = currentIndex - 1
                                                }
                                            }
                                            override fun subList(fromIndex: Int, toIndex: Int): List<String> {
                                                if (fromIndex < 0 || toIndex > size || fromIndex > toIndex) {
                                                    throw IndexOutOfBoundsException()
                                                }
                                                // Limit nesting depth - return empty list
                                                return emptyList()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    return groupValues_!!
                }

            override fun next(): MatchResult? {
                val nextStartIndex = if (range.isEmpty()) range.start + 1 else range.endInclusive + 1
                if (nextStartIndex > input.length) return null
                return this@Regex.find(input, nextStartIndex)
            }
        }
    }

    public actual companion object {
        /**
         * Returns a regular expression that matches the specified [literal] string literally.
         * No characters of that string will have special meaning when searching for an occurrence of the regular expression.
         */
        public actual fun fromLiteral(literal: String): Regex = Regex(escape(literal))

        /**
         * Returns a regular expression pattern string that matches the specified [literal] string literally.
         * No characters of that string will have special meaning when searching for an occurrence of the regular expression.
         */
        public actual fun escape(literal: String): String {
            // Escape all special regex characters
            val sb = StringBuilder()
            for (c in literal) {
                when (c) {
                    '\\', '^', '$', '*', '+', '?', '.', '(', ')', '[', ']', '{', '}', '|' -> {
                        sb.append('\\')
                        sb.append(c)
                    }
                    else -> sb.append(c)
                }
            }
            return sb.toString()
        }

        /**
         * Returns a literal replacement expression for the specified [literal] string.
         * No characters of that string will have special meaning when it is used as a replacement string in [Regex.replace] function.
         */
        public actual fun escapeReplacement(literal: String): String {
            // Escape $ and \ characters in replacement strings
            val sb = StringBuilder()
            for (c in literal) {
                when (c) {
                    '\\', '$' -> {
                        sb.append('\\')
                        sb.append(c)
                    }
                    else -> sb.append(c)
                }
            }
            return sb.toString()
        }
    }
}

/**
 * Helper function to substitute group references in replacement strings.
 * Handles $1, $2, ${name}, etc.
 */
private fun substituteGroupRefs(match: MatchResult, replacement: String): String {
    var index = 0
    val result = StringBuilder()

    while (index < replacement.length) {
        val char = replacement[index++]
        if (char == '\\') {
            if (index == replacement.length)
                throw IllegalArgumentException("The Char to be escaped is missing")

            result.append(replacement[index++])
        } else if (char == '$') {
            if (index == replacement.length)
                throw IllegalArgumentException("Capturing group index is missing")

            if (replacement[index] == '{') {
                val endIndex = replacement.readGroupName(++index)

                if (index == endIndex)
                    throw IllegalArgumentException("Named capturing group reference should have a non-empty name")
                if (endIndex == replacement.length || replacement[endIndex] != '}')
                    throw IllegalArgumentException("Named capturing group reference is missing trailing '}'")

                // Named groups not yet supported in BRS
                throw UnsupportedOperationException("Named capturing groups are not yet supported in BrightScript regex")
            } else {
                if (replacement[index] !in '0'..'9')
                    throw IllegalArgumentException("Invalid capturing group reference")

                val groups = match.groups
                val endIndex = replacement.readGroupIndex(index, groups.size)
                val groupIndexStr = replacement.substring(index, endIndex)
                val groupIndex = parseIntOrThrow(groupIndexStr, "Invalid group index: $groupIndexStr")

                if (groupIndex >= groups.size)
                    throw IndexOutOfBoundsException("Group with index $groupIndex does not exist")

                val group = groups.get(groupIndex)
                result.append(group?.value ?: "")
                index = endIndex
            }
        } else {
            result.append(char)
        }
    }
    return result.toString()
}

/**
 * Reads the group name from a replacement string (for ${name} syntax).
 */
private fun String.readGroupName(startIndex: Int): Int {
    var index = startIndex
    while (index < length) {
        if (this[index] == '}') {
            break
        } else {
            index++
        }
    }
    return index
}

/**
 * Reads the group index from a replacement string (for $1, $2 syntax).
 */
private fun String.readGroupIndex(startIndex: Int, groupCount: Int): Int {
    // at least one digit after '$' is always captured
    var index = startIndex + 1
    var groupIndex = this[startIndex] - '0'

    // capture the largest valid group index
    while (index < length && this[index] in '0'..'9') {
        val newGroupIndex = (groupIndex * 10) + (this[index] - '0')
        if (newGroupIndex in 0 until groupCount) {
            groupIndex = newGroupIndex
            index++
        } else {
            break
        }
    }
    return index
}

/**
 * Checks that the limit is non-negative.
 */
private fun requireNonNegativeLimit(limit: Int) {
    require(limit >= 0) { "Limit must be non-negative, but was $limit." }
}

/**
 * Parses a string to an Int or throws an exception.
 */
private fun parseIntOrThrow(str: String, errorMessage: String): Int {
    var result = 0
    var negative = false
    var index = 0

    if (str.isEmpty()) {
        throw NumberFormatException(errorMessage)
    }

    if (str[0] == '-') {
        negative = true
        index = 1
    } else if (str[0] == '+') {
        index = 1
    }

    if (index >= str.length) {
        throw NumberFormatException(errorMessage)
    }

    while (index < str.length) {
        val char = str[index]
        if (char < '0' || char > '9') {
            throw NumberFormatException(errorMessage)
        }
        result = result * 10 + (char - '0')
        index++
    }

    return if (negative) -result else result
}
