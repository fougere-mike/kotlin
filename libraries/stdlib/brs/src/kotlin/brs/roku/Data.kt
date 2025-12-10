/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs.roku

import kotlin.brs.BrsCreateObject
import kotlin.brs.Dynamic

/**
 * Byte array for binary data manipulation.
 *
 * roByteArray provides functionality for working with binary data,
 * including reading/writing files, encoding/decoding, and cryptographic operations.
 *
 * Example usage:
 * ```kotlin
 * val bytes = RoByteArray.create()
 * bytes.fromAsciiString("Hello, World!")
 * val base64 = bytes.toBase64String()
 * println(base64)
 * ```
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/components/robytearray.md">roByteArray</a>
 */
public external interface RoByteArray {
    // ==================== Size and Capacity ====================

    /**
     * Returns the number of bytes in the array.
     */
    public fun count(): Int

    /**
     * Returns the current capacity of the array.
     */
    public fun capacity(): Int

    /**
     * Returns true if the array is empty.
     */
    public fun isEmpty(): Boolean

    /**
     * Clears the array contents.
     */
    public fun clear()

    // ==================== Element Access ====================

    /**
     * Gets the byte at the specified index.
     *
     * @param index The index to read from.
     * @return The byte value (0-255).
     */
    public operator fun get(index: Int): Int

    /**
     * Sets the byte at the specified index.
     *
     * @param index The index to write to.
     * @param value The byte value (0-255).
     */
    public operator fun set(index: Int, value: Int)

    /**
     * Appends a byte to the end of the array.
     *
     * @param value The byte value (0-255).
     */
    public fun push(value: Int)

    /**
     * Removes and returns the last byte.
     *
     * @return The byte value, or invalid if empty.
     */
    public fun pop(): Int?

    /**
     * Returns the last byte without removing it.
     *
     * @return The byte value, or invalid if empty.
     */
    public fun peek(): Int?

    /**
     * Removes and returns the first byte.
     *
     * @return The byte value, or invalid if empty.
     */
    public fun shift(): Int?

    /**
     * Prepends a byte to the beginning of the array.
     *
     * @param value The byte value (0-255).
     */
    public fun unshift(value: Int)

    // ==================== String Conversion ====================

    /**
     * Sets the contents from an ASCII string.
     *
     * @param str The ASCII string.
     */
    public fun fromAsciiString(str: String)

    /**
     * Returns the contents as an ASCII string.
     *
     * @return The ASCII string representation.
     */
    public fun toAsciiString(): String

    /**
     * Sets the contents from a Base64 encoded string.
     *
     * @param base64 The Base64 encoded string.
     */
    public fun fromBase64String(base64: String)

    /**
     * Returns the contents as a Base64 encoded string.
     *
     * @return The Base64 encoded string.
     */
    public fun toBase64String(): String

    /**
     * Sets the contents from a hex string.
     *
     * @param hex The hex string (e.g., "48656C6C6F").
     */
    public fun fromHexString(hex: String)

    /**
     * Returns the contents as a hex string.
     *
     * @return The hex string representation.
     */
    public fun toHexString(): String

    // ==================== File Operations ====================

    /**
     * Reads the contents from a file.
     *
     * @param path The file path to read.
     * @return True if successful.
     */
    public fun readFile(path: String): Boolean

    /**
     * Writes the contents to a file.
     *
     * @param path The file path to write.
     * @return True if successful.
     */
    public fun writeFile(path: String): Boolean

    /**
     * Appends the contents to a file.
     *
     * @param path The file path to append to.
     * @return True if successful.
     */
    public fun appendFile(path: String): Boolean

    // ==================== Cryptographic Operations ====================

    /**
     * Returns the CRC32 checksum of the contents.
     *
     * @return The CRC32 checksum.
     */
    public fun getCRC32(): Int

    /**
     * Returns the MD5 hash as a hex string.
     *
     * @return The MD5 hash.
     */
    public fun getMD5(): String

    /**
     * Returns the SHA1 hash as a hex string.
     *
     * @return The SHA1 hash.
     */
    public fun getSHA1(): String

    /**
     * Returns the SHA256 hash as a hex string.
     *
     * @return The SHA256 hash.
     */
    public fun getSHA256(): String

    /**
     * Returns the SHA512 hash as a hex string.
     *
     * @return The SHA512 hash.
     */
    public fun getSHA512(): String

    // ==================== Resize and Copy ====================

    /**
     * Resizes the array to the specified size.
     *
     * @param newSize The new size.
     * @param fillValue The value to fill new elements with (0-255).
     */
    public fun resize(newSize: Int, fillValue: Int)

    /**
     * Appends the contents of another byte array.
     *
     * @param other The byte array to append.
     */
    public fun append(other: RoByteArray)

    public companion object {
        /**
         * Creates a new empty roByteArray.
         *
         * Compiles to: `CreateObject("roByteArray")`
         *
         * @return A new RoByteArray instance.
         */
        @BrsCreateObject("roByteArray")
        public fun create(): RoByteArray = definedExternally
    }
}

// ==================== Extension Functions ====================

/**
 * Resizes the array to the specified size, filling new elements with 0.
 *
 * @param newSize The new size.
 */
public fun RoByteArray.resize(newSize: Int) {
    resize(newSize, 0)
}
