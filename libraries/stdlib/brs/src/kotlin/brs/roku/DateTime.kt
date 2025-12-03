/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs.roku

import kotlin.brs.BrsInline
import kotlin.brs.Dynamic

/**
 * Provides date and time functionality.
 *
 * roDateTime represents a date and time and provides methods for manipulation,
 * formatting, and conversion between local and UTC time.
 *
 * Example usage:
 * ```kotlin
 * val dateTime = RoDateTime()
 * dateTime.mark() // Set to current time
 *
 * println("Year: ${dateTime.getYear()}")
 * println("ISO: ${dateTime.toISOString()}")
 * ```
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/components/rodatetime.md">roDateTime</a>
 */
public class RoDateTime {

    /** The native BrightScript roDateTime instance. */
    private val native: Dynamic

    /**
     * Creates a new roDateTime instance set to the current date and time.
     */
    public constructor() {
        native = brsCreateDateTime()
    }

    /**
     * Sets the date/time value to the current UTC date and time.
     */
    public fun mark() {
        brsMark(native)
    }

    /**
     * Returns the current date/time as the number of seconds from
     * the Unix epoch (00:00:00 1/1/1970 GMT).
     *
     * @return Seconds since Unix epoch.
     */
    public fun asSeconds(): Long {
        return brsAsSeconds(native)
    }

    /**
     * Sets the date/time value using the number of seconds from
     * the Unix epoch.
     *
     * @param seconds Seconds since Unix epoch.
     */
    public fun fromSeconds(seconds: Long) {
        brsFromSeconds(native, seconds)
    }

    /**
     * Returns the date/time formatted as an ISO 8601 string (e.g., "2024-01-15T10:30:00Z").
     *
     * @return ISO 8601 formatted date/time string.
     */
    public fun toISOString(): String {
        return brsToISOString(native)
    }

    /**
     * Sets the date/time value from an ISO 8601 string.
     *
     * @param iso ISO 8601 formatted date/time string.
     * @return True if parsing succeeded, false otherwise.
     */
    public fun fromISO8601String(iso: String): Boolean {
        return brsFromISO8601String(native, iso)
    }

    // ==================== UTC Accessors ====================

    /**
     * Returns the year portion of the date/time (UTC).
     */
    public fun getYear(): Int = brsGetYear(native)

    /**
     * Returns the month portion of the date/time (1-12) (UTC).
     */
    public fun getMonth(): Int = brsGetMonth(native)

    /**
     * Returns the day of month portion of the date/time (1-31) (UTC).
     */
    public fun getDayOfMonth(): Int = brsGetDayOfMonth(native)

    /**
     * Returns the day of week (0=Sunday, 6=Saturday) (UTC).
     */
    public fun getDayOfWeek(): Int = brsGetDayOfWeek(native)

    /**
     * Returns the hour portion of the date/time (0-23) (UTC).
     */
    public fun getHours(): Int = brsGetHours(native)

    /**
     * Returns the minute portion of the date/time (0-59) (UTC).
     */
    public fun getMinutes(): Int = brsGetMinutes(native)

    /**
     * Returns the second portion of the date/time (0-59) (UTC).
     */
    public fun getSeconds(): Int = brsGetSeconds(native)

    /**
     * Returns the millisecond portion of the date/time (0-999).
     */
    public fun getMilliseconds(): Int = brsGetMilliseconds(native)

    /**
     * Returns the date/time value's offset from UTC in minutes.
     */
    public fun getTimeZoneOffset(): Int = brsGetTimeZoneOffset(native)

    // ==================== Local Time Accessors ====================

    /**
     * Returns the year portion of the date/time in local time.
     */
    public fun getLocalYear(): Int = brsGetLocalYear(native)

    /**
     * Returns the month portion of the date/time in local time (1-12).
     */
    public fun getLocalMonth(): Int = brsGetLocalMonth(native)

    /**
     * Returns the day of month in local time (1-31).
     */
    public fun getLocalDayOfMonth(): Int = brsGetLocalDayOfMonth(native)

    /**
     * Returns the day of week in local time (0=Sunday, 6=Saturday).
     */
    public fun getLocalDayOfWeek(): Int = brsGetLocalDayOfWeek(native)

    /**
     * Returns the hour portion in local time (0-23).
     */
    public fun getLocalHours(): Int = brsGetLocalHours(native)

    /**
     * Returns the minute portion in local time (0-59).
     */
    public fun getLocalMinutes(): Int = brsGetLocalMinutes(native)

    /**
     * Returns the second portion in local time (0-59).
     */
    public fun getLocalSeconds(): Int = brsGetLocalSeconds(native)

    /**
     * Returns the weekday name in the current locale (e.g., "Monday").
     */
    public fun getWeekday(): String = brsGetWeekday(native)

    /**
     * Returns the last day of the month (28-31).
     */
    public fun getLastDayOfMonth(): Int = brsGetLastDayOfMonth(native)

    override fun toString(): String = toISOString()

    // ==================== BrightScript Intrinsics ====================

    @BrsInline("return CreateObject(\"roDateTime\")")
    private external fun brsCreateDateTime(): Dynamic

    @BrsInline("dt.Mark()")
    private external fun brsMark(dt: Dynamic)

    @BrsInline("return dt.AsSeconds()")
    private external fun brsAsSeconds(dt: Dynamic): Long

    @BrsInline("dt.FromSeconds(seconds)")
    private external fun brsFromSeconds(dt: Dynamic, seconds: Long)

    @BrsInline("return dt.ToISOString()")
    private external fun brsToISOString(dt: Dynamic): String

    @BrsInline("return dt.FromISO8601String(iso)")
    private external fun brsFromISO8601String(dt: Dynamic, iso: String): Boolean

    @BrsInline("return dt.GetYear()")
    private external fun brsGetYear(dt: Dynamic): Int

    @BrsInline("return dt.GetMonth()")
    private external fun brsGetMonth(dt: Dynamic): Int

    @BrsInline("return dt.GetDayOfMonth()")
    private external fun brsGetDayOfMonth(dt: Dynamic): Int

    @BrsInline("return dt.GetDayOfWeek()")
    private external fun brsGetDayOfWeek(dt: Dynamic): Int

    @BrsInline("return dt.GetHours()")
    private external fun brsGetHours(dt: Dynamic): Int

    @BrsInline("return dt.GetMinutes()")
    private external fun brsGetMinutes(dt: Dynamic): Int

    @BrsInline("return dt.GetSeconds()")
    private external fun brsGetSeconds(dt: Dynamic): Int

    @BrsInline("return dt.GetMilliseconds()")
    private external fun brsGetMilliseconds(dt: Dynamic): Int

    @BrsInline("return dt.GetTimeZoneOffset()")
    private external fun brsGetTimeZoneOffset(dt: Dynamic): Int

    @BrsInline("return dt.GetLocalYear()")
    private external fun brsGetLocalYear(dt: Dynamic): Int

    @BrsInline("return dt.GetLocalMonth()")
    private external fun brsGetLocalMonth(dt: Dynamic): Int

    @BrsInline("return dt.GetLocalDayOfMonth()")
    private external fun brsGetLocalDayOfMonth(dt: Dynamic): Int

    @BrsInline("return dt.GetLocalDayOfWeek()")
    private external fun brsGetLocalDayOfWeek(dt: Dynamic): Int

    @BrsInline("return dt.GetLocalHours()")
    private external fun brsGetLocalHours(dt: Dynamic): Int

    @BrsInline("return dt.GetLocalMinutes()")
    private external fun brsGetLocalMinutes(dt: Dynamic): Int

    @BrsInline("return dt.GetLocalSeconds()")
    private external fun brsGetLocalSeconds(dt: Dynamic): Int

    @BrsInline("return dt.GetWeekday()")
    private external fun brsGetWeekday(dt: Dynamic): String

    @BrsInline("return dt.GetLastDayOfMonth()")
    private external fun brsGetLastDayOfMonth(dt: Dynamic): Int
}

/**
 * Measures elapsed time with millisecond precision.
 *
 * roTimespan is useful for measuring elapsed time between operations.
 *
 * Example usage:
 * ```kotlin
 * val timer = RoTimespan()
 * timer.mark()
 * // ... perform operations ...
 * val elapsed = timer.totalMilliseconds()
 * println("Operation took $elapsed ms")
 * ```
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/components/rotimespan.md">roTimespan</a>
 */
public class RoTimespan {

    private val native: Dynamic

    /**
     * Creates a new roTimespan and marks the current time.
     */
    public constructor() {
        native = brsCreateTimespan()
    }

    /**
     * Sets the timespan's mark to the current time.
     */
    public fun mark() {
        brsMark(native)
    }

    /**
     * Returns the total milliseconds elapsed since the mark.
     */
    public fun totalMilliseconds(): Int {
        return brsTotalMilliseconds(native)
    }

    /**
     * Returns the total seconds elapsed since the mark.
     */
    public fun totalSeconds(): Int {
        return brsTotalSeconds(native)
    }

    /**
     * Returns seconds and milliseconds elapsed since the mark.
     *
     * @return Pair of (seconds, milliseconds)
     */
    public fun getSecondsToHere(): Pair<Int, Int> {
        val total = totalMilliseconds()
        return Pair(total / 1000, total % 1000)
    }

    // ==================== BrightScript Intrinsics ====================

    @BrsInline("return CreateObject(\"roTimespan\")")
    private external fun brsCreateTimespan(): Dynamic

    @BrsInline("ts.Mark()")
    private external fun brsMark(ts: Dynamic)

    @BrsInline("return ts.TotalMilliseconds()")
    private external fun brsTotalMilliseconds(ts: Dynamic): Int

    @BrsInline("return ts.TotalSeconds()")
    private external fun brsTotalSeconds(ts: Dynamic): Int
}
