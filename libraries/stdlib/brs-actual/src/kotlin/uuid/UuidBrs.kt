/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.uuid

import kotlin.random.Random

/**
 * Generates a random UUID using the platform's random number generator.
 * BrightScript doesn't have a secure random API, so we use Rnd() seeded randomly.
 */
@ExperimentalUuidApi
internal actual fun secureRandomUuid(): Uuid {
    val randomBytes = ByteArray(16)
    Random.nextBytes(randomBytes)
    return uuidFromRandomBytes(randomBytes)
}

@ExperimentalUuidApi
internal actual fun serializedUuid(uuid: Uuid): Any =
    throw UnsupportedOperationException("Serialization is supported only in Kotlin/JVM")

/**
 * Extract a Long from byte array at given index (big-endian).
 * Uses the common implementation since BRS supports Long operations.
 */
@ExperimentalUuidApi
internal actual fun ByteArray.getLongAt(index: Int): Long =
    getLongAtCommonImpl(index)

/**
 * Format Long bytes into hex string in destination array.
 */
@ExperimentalUuidApi
@OptIn(ExperimentalStdlibApi::class)
internal actual fun Long.formatBytesInto(dst: ByteArray, dstOffset: Int, startIndex: Int, endIndex: Int) {
    var dstIndex = dstOffset
    for (i in startIndex until endIndex) {
        val shift = (7 - i) * 8
        val byte = ((this shr shift) and 0xFFL).toInt()
        val byteDigits = BYTE_TO_LOWER_CASE_HEX_DIGITS[byte]
        dst[dstIndex++] = (byteDigits shr 8).toByte()
        dst[dstIndex++] = byteDigits.toByte()
    }
}

/**
 * Set Long value into byte array at given index (big-endian).
 */
@ExperimentalUuidApi
internal actual fun ByteArray.setLongAt(index: Int, value: Long) {
    for (i in 0 until 8) {
        val shift = (7 - i) * 8
        this[index + i] = ((value shr shift) and 0xFFL).toByte()
    }
}

/**
 * Parse UUID from hyphenated hex string (xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx).
 */
@OptIn(ExperimentalStdlibApi::class)
@ExperimentalUuidApi
internal actual fun uuidParseHexDash(hexDashString: String): Uuid {
    // xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
    val part1 = hexDashString.hexToLong(startIndex = 0, endIndex = 8)
    hexDashString.checkHyphenAt(8)
    val part2 = hexDashString.hexToLong(startIndex = 9, endIndex = 13)
    hexDashString.checkHyphenAt(13)
    val part3 = hexDashString.hexToLong(startIndex = 14, endIndex = 18)
    hexDashString.checkHyphenAt(18)
    val part4 = hexDashString.hexToLong(startIndex = 19, endIndex = 23)
    hexDashString.checkHyphenAt(23)
    val part5 = hexDashString.hexToLong(startIndex = 24, endIndex = 36)

    val msb = (part1 shl 32) or (part2 shl 16) or part3
    val lsb = (part4 shl 48) or part5
    return Uuid.fromLongs(msb, lsb)
}

/**
 * Parse UUID from non-hyphenated hex string (32 hex digits).
 */
@OptIn(ExperimentalStdlibApi::class)
@ExperimentalUuidApi
internal actual fun uuidParseHex(hexString: String): Uuid {
    val msb = hexString.hexToLong(startIndex = 0, endIndex = 16)
    val lsb = hexString.hexToLong(startIndex = 16, endIndex = 32)
    return Uuid.fromLongs(msb, lsb)
}

@OptIn(ExperimentalStdlibApi::class)
private fun String.hexToLong(startIndex: Int, endIndex: Int): Long {
    var result = 0L
    for (i in startIndex until endIndex) {
        val digit = this[i].digitToIntOrNull(16)
            ?: throw IllegalArgumentException("Expected a hexadecimal digit at index $i in $this")
        result = (result shl 4) or digit.toLong()
    }
    return result
}

private fun String.checkHyphenAt(index: Int) {
    if (this[index] != '-') {
        throw IllegalArgumentException("Expected '-' at index $index in $this")
    }
}
