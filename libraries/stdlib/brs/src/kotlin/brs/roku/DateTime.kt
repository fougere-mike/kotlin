/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs.roku

import kotlin.brs.BrsCreateObject
import kotlin.brs.Dynamic

/**
 * Provides date and time functionality.
 *
 * roDateTime represents a date and time and provides methods for manipulation,
 * formatting, and conversion between local and UTC time.
 *
 * Example usage:
 * ```kotlin
 * val dateTime = RoDateTime.create()
 * dateTime.mark() // Set to current time
 *
 * println("Year: ${dateTime.getYear()}")
 * println("ISO: ${dateTime.toISOString()}")
 * ```
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/components/rodatetime.md">roDateTime</a>
 */
public external interface RoDateTime {
    /**
     * Sets the date/time value to the current UTC date and time.
     */
    public fun mark()

    /**
     * Returns the current date/time as the number of seconds from
     * the Unix epoch (00:00:00 1/1/1970 GMT).
     *
     * @return Seconds since Unix epoch.
     */
    public fun asSeconds(): Long

    /**
     * Sets the date/time value using the number of seconds from
     * the Unix epoch.
     *
     * @param seconds Seconds since Unix epoch.
     */
    public fun fromSeconds(seconds: Long)

    /**
     * Returns the date/time formatted as an ISO 8601 string (e.g., "2024-01-15T10:30:00Z").
     *
     * @return ISO 8601 formatted date/time string.
     */
    public fun toISOString(): String

    /**
     * Sets the date/time value from an ISO 8601 string.
     *
     * @param iso ISO 8601 formatted date/time string.
     * @return True if parsing succeeded, false otherwise.
     */
    public fun fromISO8601String(iso: String): Boolean

    // ==================== UTC Accessors ====================

    /**
     * Returns the year portion of the date/time (UTC).
     */
    public fun getYear(): Int

    /**
     * Returns the month portion of the date/time (1-12) (UTC).
     */
    public fun getMonth(): Int

    /**
     * Returns the day of month portion of the date/time (1-31) (UTC).
     */
    public fun getDayOfMonth(): Int

    /**
     * Returns the day of week (0=Sunday, 6=Saturday) (UTC).
     */
    public fun getDayOfWeek(): Int

    /**
     * Returns the hour portion of the date/time (0-23) (UTC).
     */
    public fun getHours(): Int

    /**
     * Returns the minute portion of the date/time (0-59) (UTC).
     */
    public fun getMinutes(): Int

    /**
     * Returns the second portion of the date/time (0-59) (UTC).
     */
    public fun getSeconds(): Int

    /**
     * Returns the millisecond portion of the date/time (0-999).
     */
    public fun getMilliseconds(): Int

    /**
     * Returns the date/time value's offset from UTC in minutes.
     */
    public fun getTimeZoneOffset(): Int

    // ==================== Local Time Accessors ====================

    /**
     * Returns the year portion of the date/time in local time.
     */
    public fun getLocalYear(): Int

    /**
     * Returns the month portion of the date/time in local time (1-12).
     */
    public fun getLocalMonth(): Int

    /**
     * Returns the day of month in local time (1-31).
     */
    public fun getLocalDayOfMonth(): Int

    /**
     * Returns the day of week in local time (0=Sunday, 6=Saturday).
     */
    public fun getLocalDayOfWeek(): Int

    /**
     * Returns the hour portion in local time (0-23).
     */
    public fun getLocalHours(): Int

    /**
     * Returns the minute portion in local time (0-59).
     */
    public fun getLocalMinutes(): Int

    /**
     * Returns the second portion in local time (0-59).
     */
    public fun getLocalSeconds(): Int

    /**
     * Returns the weekday name in the current locale (e.g., "Monday").
     */
    public fun getWeekday(): String

    /**
     * Returns the last day of the month (28-31).
     */
    public fun getLastDayOfMonth(): Int

    public companion object {
        /**
         * Creates a new roDateTime instance set to the current date and time.
         *
         * Compiles to: `CreateObject("roDateTime")`
         *
         * @return A new RoDateTime instance.
         */
        @BrsCreateObject("roDateTime")
        public fun create(): RoDateTime = definedExternally
    }
}

/**
 * Measures elapsed time with millisecond precision.
 *
 * roTimespan is useful for measuring elapsed time between operations.
 *
 * Example usage:
 * ```kotlin
 * val timer = RoTimespan.create()
 * timer.mark()
 * // ... perform operations ...
 * val elapsed = timer.totalMilliseconds()
 * println("Operation took $elapsed ms")
 * ```
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/components/rotimespan.md">roTimespan</a>
 */
public external interface RoTimespan {
    /**
     * Sets the timespan's mark to the current time.
     */
    public fun mark()

    /**
     * Returns the total milliseconds elapsed since the mark.
     */
    public fun totalMilliseconds(): Int

    /**
     * Returns the total seconds elapsed since the mark.
     */
    public fun totalSeconds(): Int

    public companion object {
        /**
         * Creates a new roTimespan and marks the current time.
         *
         * Compiles to: `CreateObject("roTimespan")`
         *
         * @return A new RoTimespan instance.
         */
        @BrsCreateObject("roTimespan")
        public fun create(): RoTimespan = definedExternally
    }
}

// ==================== Extension Functions ====================

/**
 * Returns seconds and milliseconds elapsed since the mark.
 *
 * @return Pair of (seconds, milliseconds)
 */
public fun RoTimespan.getSecondsToHere(): Pair<Int, Int> {
    val total = totalMilliseconds()
    return Pair(total / 1000, total % 1000)
}
