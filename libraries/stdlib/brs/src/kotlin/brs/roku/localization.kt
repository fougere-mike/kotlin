/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("UNUSED_PARAMETER")

package kotlin.brs.roku

import kotlin.brs.*

/**
 * roLocalization - provides localized strings for the channel.
 *
 * Reads localized strings from the locale/ directory based on the
 * user's device language setting.
 *
 * Directory structure:
 * - locale/en_US/translations.ts
 * - locale/es_ES/translations.ts
 * - etc.
 */
@BrsExternal
@BrsName("roLocalization")
public external class RoLocalization : RoInterface {
    public constructor()

    /**
     * Get a localized string by key.
     * @param key The string key from translations.ts
     * @return The localized string, or the key if not found
     */
    public fun getLocalizedAsset(key: String): String

    /**
     * Get the current locale (e.g., "en_US", "es_ES").
     */
    public fun getCurrentLocale(): String

    /**
     * Get all available locales in the channel.
     */
    public fun getAvailableLocales(): RoArray
}

/**
 * roInternationalTime - handles date/time formatting for different locales.
 */
@BrsExternal
@BrsName("roInternationalTime")
public external class RoInternationalTime : RoInterface {
    public constructor()
    public constructor(dateTime: RoDateTime)

    /**
     * Get the day of month (1-31).
     */
    public fun getDayOfMonth(): Int

    /**
     * Get the day of week (0=Sunday, 6=Saturday).
     */
    public fun getDayOfWeek(): Int

    /**
     * Get the week of month (1-5).
     */
    public fun getWeekOfMonth(): Int

    /**
     * Get the hours (0-23).
     */
    public fun getHours(): Int

    /**
     * Get the minutes (0-59).
     */
    public fun getMinutes(): Int

    /**
     * Get the seconds (0-59).
     */
    public fun getSeconds(): Int

    /**
     * Get the milliseconds (0-999).
     */
    public fun getMilliseconds(): Int

    /**
     * Get the month (1-12).
     */
    public fun getMonth(): Int

    /**
     * Get the year (e.g., 2024).
     */
    public fun getYear(): Int

    /**
     * Mark as current time.
     */
    public fun mark()

    /**
     * Get the time zone offset in hours from UTC.
     */
    public fun getTimeZoneOffset(): Int

    /**
     * Get the long date format string (e.g., "Monday, January 1, 2024").
     */
    public fun asDateStringLong(): String

    /**
     * Get the short date format string (e.g., "1/1/24").
     */
    public fun asDateString(): String

    /**
     * Get the time string (e.g., "12:34 PM").
     */
    public fun asTimeString(): String

    /**
     * Get the formatted date/time string using device locale preferences.
     */
    public fun toISOString(): String
}

// ============================================
// Locale-aware extension functions
// ============================================

/**
 * Get a localized string using the global roLocalization instance.
 *
 * Usage:
 * ```kotlin
 * val greeting = tr("greeting_hello")
 * ```
 */
@BrsInline("return CreateObject(\"roLocalization\").getLocalizedAsset(key)")
public external fun tr(key: String): String

/**
 * Get a localized string with placeholder substitution.
 *
 * The localized string should contain placeholders like {0}, {1}, etc.
 *
 * Usage:
 * ```kotlin
 * val message = tr("welcome_user", userName)  // "Welcome, {0}!" -> "Welcome, John!"
 * ```
 */
public fun tr(key: String, vararg args: Any?): String {
    var result = tr(key)
    args.forEachIndexed { index, arg ->
        result = result.replace("{$index}", arg?.toString() ?: "")
    }
    return result
}

/**
 * Get the current device locale.
 */
@BrsInline("return CreateObject(\"roDeviceInfo\").getCurrentLocale()")
public external fun currentLocale(): String

/**
 * Get the current device country code.
 */
@BrsInline("return CreateObject(\"roDeviceInfo\").getCountryCode()")
public external fun countryCode(): String

/**
 * Check if a specific locale is available in the channel.
 */
public fun isLocaleAvailable(locale: String): Boolean {
    val localization = RoLocalization()
    val available = localization.getAvailableLocales()
    for (i in 0 until available.count()) {
        if ((available[i] as? String)?.equals(locale, ignoreCase = true) == true) {
            return true
        }
    }
    return false
}

/**
 * Format a number according to the current locale.
 *
 * Note: BrightScript has limited number formatting, this provides a basic implementation.
 */
public fun formatNumber(value: Double, decimals: Int = 2): String {
    val multiplier = mathPow(10.0, decimals.toDouble())
    val rounded = mathRound(value * multiplier) / multiplier
    return rounded.toString()
}

/**
 * Format a currency value.
 *
 * @param value The monetary value
 * @param currencyCode ISO 4217 currency code (e.g., "USD", "EUR")
 */
public fun formatCurrency(value: Double, currencyCode: String = "USD"): String {
    val symbol = when (currencyCode.uppercase()) {
        "USD" -> "$"
        "EUR" -> "€"
        "GBP" -> "£"
        "JPY" -> "¥"
        "CNY" -> "¥"
        "BRL" -> "R$"
        "MXN" -> "$"
        "CAD" -> "CA$"
        "AUD" -> "A$"
        else -> currencyCode
    }
    return "$symbol${formatNumber(value, 2)}"
}

/**
 * Format a date according to common locale patterns.
 */
public fun formatDate(dateTime: RoDateTime, format: DateFormat = DateFormat.SHORT): String {
    return when (format) {
        DateFormat.SHORT -> "${dateTime.getMonth()}/${dateTime.getDayOfMonth()}/${dateTime.getYear()}"
        DateFormat.MEDIUM -> {
            val month = monthName(dateTime.getMonth(), abbreviated = true)
            "$month ${dateTime.getDayOfMonth()}, ${dateTime.getYear()}"
        }
        DateFormat.LONG -> {
            val month = monthName(dateTime.getMonth(), abbreviated = false)
            val day = dayName(dateTime.getDayOfWeek(), abbreviated = false)
            "$day, $month ${dateTime.getDayOfMonth()}, ${dateTime.getYear()}"
        }
    }
}

/**
 * Date format styles.
 */
public enum class DateFormat {
    SHORT,   // 1/15/2024
    MEDIUM,  // Jan 15, 2024
    LONG     // Monday, January 15, 2024
}

/**
 * Get month name.
 */
private fun monthName(month: Int, abbreviated: Boolean): String {
    val names = if (abbreviated) {
        listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    } else {
        listOf("January", "February", "March", "April", "May", "June",
               "July", "August", "September", "October", "November", "December")
    }
    return names.getOrElse(month - 1) { "Unknown" }
}

/**
 * Get day name.
 */
private fun dayName(dayOfWeek: Int, abbreviated: Boolean): String {
    val names = if (abbreviated) {
        listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    } else {
        listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    }
    return names.getOrElse(dayOfWeek) { "Unknown" }
}

// Math helper for formatting - using inline implementations
// Note: BrightScript math functions will be inlined by the backend
private fun mathRound(x: Double): Long = (x + 0.5).toLong()
private fun mathPow(x: Double, y: Double): Double {
    // Simple integer power implementation
    var result = 1.0
    var exp = y.toInt()
    if (exp < 0) {
        return 1.0 / mathPow(x, (-exp).toDouble())
    }
    while (exp > 0) {
        result *= x
        exp--
    }
    return result
}
