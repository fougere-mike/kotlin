/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("UNUSED_PARAMETER")

package kotlin.brs.roku

import kotlin.brs.*

/**
 * Base interface for all BrightScript roInterface types.
 */
public external interface RoInterface

/**
 * BrightScript roArray - dynamic array type.
 */
@BrsExternal
public external class RoArray : RoInterface {
    /**
     * Creates a new array.
     * @param size Initial size (optional).
     * @param resize If true, array is resizable (default: true).
     */
    public constructor(size: Int = 0, resize: Boolean = true)

    /**
     * Appends a value to the end of the array.
     */
    public fun push(value: Any?)

    /**
     * Removes and returns the last element.
     */
    public fun pop(): Any?

    /**
     * Removes and returns the first element.
     */
    public fun shift(): Any?

    /**
     * Inserts a value at the beginning.
     */
    public fun unshift(value: Any?)

    /**
     * Returns the number of elements.
     */
    public fun count(): Int

    /**
     * Removes all elements.
     */
    public fun clear()

    /**
     * Appends all elements from another array.
     */
    public fun append(other: RoArray)

    /**
     * Gets the element at the specified index.
     */
    public operator fun get(index: Int): Any?

    /**
     * Sets the element at the specified index.
     */
    public operator fun set(index: Int, value: Any?)
}

/**
 * BrightScript roAssociativeArray - dictionary/map type.
 */
@BrsExternal
public external class RoAssociativeArray : RoInterface {
    public constructor()

    /**
     * Adds a key-value pair.
     */
    public fun addReplace(key: String, value: Any?)

    /**
     * Removes a key.
     */
    public fun delete(key: String): Boolean

    /**
     * Checks if a key exists.
     */
    public fun doesExist(key: String): Boolean

    /**
     * Looks up a value by key.
     */
    public fun lookup(key: String): Any?

    /**
     * Returns all keys as an array.
     */
    public fun keys(): RoArray

    /**
     * Returns all values as an array.
     */
    public fun items(): RoArray

    /**
     * Returns the number of key-value pairs.
     */
    public fun count(): Int

    /**
     * Removes all entries.
     */
    public fun clear()

    /**
     * Appends all entries from another AA.
     */
    public fun append(other: RoAssociativeArray)

    /**
     * Gets a value by key.
     */
    public operator fun get(key: String): Any?

    /**
     * Sets a value by key.
     */
    public operator fun set(key: String, value: Any?)
}

/**
 * BrightScript roString type.
 */
@BrsExternal
public external class RoString : RoInterface {
    public constructor(value: String = "")

    public fun len(): Int
    public fun left(count: Int): String
    public fun right(count: Int): String
    public fun mid(start: Int, length: Int = -1): String
    public fun instr(substring: String): Int
    public fun instr(start: Int, substring: String): Int
    public fun trim(): String
    public fun toInt(): Int
    public fun toFloat(): Float
    public fun tokenize(delimiters: String): RoArray
    public fun replace(from: String, to: String): String
    public fun split(delimiter: String): RoArray
}

/**
 * BrightScript roRegex type.
 */
@BrsExternal
public external class RoRegex : RoInterface {
    public constructor(pattern: String, flags: String = "")

    public fun isMatch(str: String): Boolean
    public fun match(str: String): RoArray
    public fun matchAll(str: String): RoArray
    public fun replace(str: String, replacement: String): String
    public fun replaceAll(str: String, replacement: String): String
    public fun split(str: String): RoArray
}

/**
 * BrightScript roDateTime type.
 */
@BrsExternal
public external class RoDateTime : RoInterface {
    public constructor()

    public fun mark()
    public fun toISOString(): String
    public fun fromISO8601String(dateString: String)
    public fun fromSeconds(seconds: Int)
    public fun asSeconds(): Int
    public fun getYear(): Int
    public fun getMonth(): Int
    public fun getDayOfMonth(): Int
    public fun getHours(): Int
    public fun getMinutes(): Int
    public fun getSeconds(): Int
    public fun getMilliseconds(): Int
    public fun getDayOfWeek(): Int
    public fun getLastDayOfMonth(): Int
    public fun getTimeZoneOffset(): Int
    public fun toLocalTime()
}

/**
 * BrightScript roTimespan type for measuring elapsed time.
 */
@BrsExternal
public external class RoTimespan : RoInterface {
    public constructor()

    public fun mark()
    public fun totalMilliseconds(): Int
    public fun totalSeconds(): Int
    public fun getSecondsToISO8601Date(dateString: String): Int
}

/**
 * BrightScript roByteArray type.
 */
@BrsExternal
public external class RoByteArray : RoInterface {
    public constructor()

    public fun fromHexString(hexString: String)
    public fun toHexString(): String
    public fun fromBase64String(base64String: String)
    public fun toBase64String(): String
    public fun fromAsciiString(str: String)
    public fun toAsciiString(): String
    public fun getSignedByte(index: Int): Int
    public fun getSignedLong(index: Int): Int
    public fun count(): Int
    public fun isLittleEndianCPU(): Boolean
}

/**
 * BrightScript roMessagePort for event handling.
 */
@BrsExternal
public external class RoMessagePort : RoInterface {
    public constructor()

    /**
     * Waits for a message with optional timeout.
     * @param timeout Timeout in milliseconds (0 = forever).
     * @return The message received, or invalid on timeout.
     */
    public fun waitMessage(timeout: Int): Any?

    /**
     * Gets a message without waiting.
     * @return The message, or invalid if none available.
     */
    public fun getMessage(): Any?

    /**
     * Peeks at the next message without removing it.
     */
    public fun peekMessage(): Any?
}
