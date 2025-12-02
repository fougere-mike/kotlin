/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.text

/**
 * Represents a collection of captured groups in a single match of a regular expression.
 *
 * This collection has size of `groupCount + 1` where `groupCount` is the count of groups in the regular expression.
 * Groups are indexed from 1 to `groupCount` and group with the index 0 corresponds to the entire match.
 *
 * An element of the collection at the particular index can be `null`,
 * if the corresponding group in the regular expression is optional and
 * there was no match captured by that group.
 *
 * NOTE: This is a temporary copy for BRS until the compiler backend properly links common sources.
 * Original: /libraries/stdlib/src/kotlin/text/regex/MatchResult.kt
 */
public interface MatchGroupCollection : Collection<MatchGroup?> {

    /** Returns a group with the specified [index].
     *
     * @return An instance of [MatchGroup] if the group with the specified [index] was matched or `null` otherwise.
     *
     * Groups are indexed from 1 to the count of groups in the regular expression. A group with the index 0
     * corresponds to the entire match.
     */
    public operator fun get(index: Int): MatchGroup?
}

/**
 * Extends [MatchGroupCollection] by introducing a way to get matched groups by name, when regex supports it.
 *
 * NOTE: This is a temporary copy for BRS until the compiler backend properly links common sources.
 */
@SinceKotlin("1.1")
public interface MatchNamedGroupCollection : MatchGroupCollection {
    /**
     * Returns a named group with the specified [name].
     * @return An instance of [MatchGroup] if the group with the specified [name] was matched or `null` otherwise.
     * @throws IllegalArgumentException if there is no group with the specified [name] defined in the regex pattern.
     * @throws UnsupportedOperationException if this match group collection doesn't support getting match groups by name,
     * for example, when it's not supported by the current platform.
     */
    public operator fun get(name: String): MatchGroup?
}

/**
 * Represents the results from a single regular expression match.
 *
 * NOTE: This is a temporary copy for BRS until the compiler backend properly links common sources.
 * Original: /libraries/stdlib/src/kotlin/text/regex/MatchResult.kt
 */
public interface MatchResult {
    /** The range of indices in the original string where match was captured. */
    public val range: IntRange
    /** The substring from the input string captured by this match. */
    public val value: String
    /**
     * A collection of groups matched by the regular expression.
     *
     * This collection has size of `groupCount + 1` where `groupCount` is the count of groups in the regular expression.
     * Groups are indexed from 1 to `groupCount` and group with the index 0 corresponds to the entire match.
     */
    public val groups: MatchGroupCollection
    /**
     * A list of matched indexed group values.
     *
     * This list has size of `groupCount + 1` where `groupCount` is the count of groups in the regular expression.
     * Groups are indexed from 1 to `groupCount` and group with the index 0 corresponds to the entire match.
     *
     * If the group in the regular expression is optional and there were no match captured by that group,
     * corresponding item in [groupValues] is an empty string.
     */
    public val groupValues: List<String>

    /**
     * An instance of [MatchResult.Destructured] wrapper providing components for destructuring assignment of group values.
     *
     * component1 corresponds to the value of the first group, component2 — of the second, and so on.
     */
    public val destructured: Destructured get() = Destructured(this)

    /** Returns a new [MatchResult] with the results for the next match, starting at the position
     *  at which the last match ended (at the character after the last matched character).
     */
    public fun next(): MatchResult?

    /**
     * Provides components for destructuring assignment of group values.
     *
     * [component1] corresponds to the value of the first group, [component2] — of the second, and so on.
     *
     * If the group in the regular expression is optional and there were no match captured by that group,
     * corresponding component value is an empty string.
     */
    public class Destructured internal constructor(public val match: MatchResult) {
        public operator fun component1():  String = match.groupValues[1]
        public operator fun component2():  String = match.groupValues[2]
        public operator fun component3():  String = match.groupValues[3]
        public operator fun component4():  String = match.groupValues[4]
        public operator fun component5():  String = match.groupValues[5]
        public operator fun component6():  String = match.groupValues[6]
        public operator fun component7():  String = match.groupValues[7]
        public operator fun component8():  String = match.groupValues[8]
        public operator fun component9():  String = match.groupValues[9]
        public operator fun component10(): String = match.groupValues[10]

        /**
         *  Returns destructured group values as a list of strings.
         *  First value in the returned list corresponds to the value of the first group, and so on.
         */
        public fun toList(): List<String> = match.groupValues.subList(1, match.groupValues.size)
    }
}
