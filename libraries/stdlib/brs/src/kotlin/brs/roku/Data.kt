/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs.roku

import kotlin.brs.BrsInline
import kotlin.brs.Dynamic

/**
 * Byte array for binary data manipulation.
 *
 * roByteArray provides functionality for working with binary data,
 * including reading/writing files, encoding/decoding, and cryptographic operations.
 *
 * Example usage:
 * ```kotlin
 * val bytes = RoByteArray()
 * bytes.fromAsciiString("Hello, World!")
 * val base64 = bytes.toBase64String()
 * println(base64)
 * ```
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/components/robytearray.md">roByteArray</a>
 */
public class RoByteArray {

    private val native: Dynamic

    /**
     * Creates a new empty roByteArray.
     */
    public constructor() {
        native = brsCreateByteArray()
    }

    /**
     * Internal constructor for wrapping an existing native byte array.
     */
    internal constructor(nativeArray: Dynamic) {
        native = nativeArray
    }

    // ==================== Size and Capacity ====================

    /**
     * Returns the number of bytes in the array.
     */
    public fun count(): Int {
        return brsCount(native)
    }

    /**
     * Returns the current capacity of the array.
     */
    public fun capacity(): Int {
        return brsCapacity(native)
    }

    /**
     * Returns true if the array is empty.
     */
    public fun isEmpty(): Boolean {
        return brsIsEmpty(native)
    }

    /**
     * Clears the array contents.
     */
    public fun clear() {
        brsClear(native)
    }

    // ==================== Element Access ====================

    /**
     * Gets the byte at the specified index.
     *
     * @param index The index to read from.
     * @return The byte value (0-255).
     */
    public operator fun get(index: Int): Int {
        return brsGetByte(native, index)
    }

    /**
     * Sets the byte at the specified index.
     *
     * @param index The index to write to.
     * @param value The byte value (0-255).
     */
    public operator fun set(index: Int, value: Int) {
        brsSetByte(native, index, value)
    }

    /**
     * Appends a byte to the end of the array.
     *
     * @param value The byte value (0-255).
     */
    public fun push(value: Int) {
        brsPush(native, value)
    }

    /**
     * Removes and returns the last byte.
     *
     * @return The byte value, or invalid if empty.
     */
    public fun pop(): Int? {
        return brsPop(native)
    }

    /**
     * Returns the last byte without removing it.
     *
     * @return The byte value, or invalid if empty.
     */
    public fun peek(): Int? {
        return brsPeek(native)
    }

    /**
     * Removes and returns the first byte.
     *
     * @return The byte value, or invalid if empty.
     */
    public fun shift(): Int? {
        return brsShift(native)
    }

    /**
     * Prepends a byte to the beginning of the array.
     *
     * @param value The byte value (0-255).
     */
    public fun unshift(value: Int) {
        brsUnshift(native, value)
    }

    // ==================== String Conversion ====================

    /**
     * Sets the contents from an ASCII string.
     *
     * @param str The ASCII string.
     */
    public fun fromAsciiString(str: String) {
        brsFromAsciiString(native, str)
    }

    /**
     * Returns the contents as an ASCII string.
     *
     * @return The ASCII string representation.
     */
    public fun toAsciiString(): String {
        return brsToAsciiString(native)
    }

    /**
     * Sets the contents from a Base64 encoded string.
     *
     * @param base64 The Base64 encoded string.
     */
    public fun fromBase64String(base64: String) {
        brsFromBase64String(native, base64)
    }

    /**
     * Returns the contents as a Base64 encoded string.
     *
     * @return The Base64 encoded string.
     */
    public fun toBase64String(): String {
        return brsToBase64String(native)
    }

    /**
     * Sets the contents from a hex string.
     *
     * @param hex The hex string (e.g., "48656C6C6F").
     */
    public fun fromHexString(hex: String) {
        brsFromHexString(native, hex)
    }

    /**
     * Returns the contents as a hex string.
     *
     * @return The hex string representation.
     */
    public fun toHexString(): String {
        return brsToHexString(native)
    }

    // ==================== File Operations ====================

    /**
     * Reads the contents from a file.
     *
     * @param path The file path to read.
     * @return True if successful.
     */
    public fun readFile(path: String): Boolean {
        return brsReadFile(native, path)
    }

    /**
     * Writes the contents to a file.
     *
     * @param path The file path to write.
     * @return True if successful.
     */
    public fun writeFile(path: String): Boolean {
        return brsWriteFile(native, path)
    }

    /**
     * Appends the contents to a file.
     *
     * @param path The file path to append to.
     * @return True if successful.
     */
    public fun appendFile(path: String): Boolean {
        return brsAppendFile(native, path)
    }

    // ==================== Cryptographic Operations ====================

    /**
     * Returns the CRC32 checksum of the contents.
     *
     * @return The CRC32 checksum.
     */
    public fun getCRC32(): Int {
        return brsGetCRC32(native)
    }

    /**
     * Returns the MD5 hash as a hex string.
     *
     * @return The MD5 hash.
     */
    public fun getMD5(): String {
        return brsGetMD5(native)
    }

    /**
     * Returns the SHA1 hash as a hex string.
     *
     * @return The SHA1 hash.
     */
    public fun getSHA1(): String {
        return brsGetSHA1(native)
    }

    /**
     * Returns the SHA256 hash as a hex string.
     *
     * @return The SHA256 hash.
     */
    public fun getSHA256(): String {
        return brsGetSHA256(native)
    }

    /**
     * Returns the SHA512 hash as a hex string.
     *
     * @return The SHA512 hash.
     */
    public fun getSHA512(): String {
        return brsGetSHA512(native)
    }

    // ==================== Resize and Copy ====================

    /**
     * Resizes the array to the specified size.
     *
     * @param newSize The new size.
     * @param fillValue The value to fill new elements with (0-255).
     */
    public fun resize(newSize: Int, fillValue: Int = 0) {
        brsResize(native, newSize, fillValue)
    }

    /**
     * Appends the contents of another byte array.
     *
     * @param other The byte array to append.
     */
    public fun append(other: RoByteArray) {
        brsAppend(native, other.native)
    }

    /**
     * Returns the native BrightScript object.
     */
    internal fun getNative(): Dynamic = native

    // ==================== BrightScript Intrinsics ====================

    @BrsInline("return CreateObject(\"roByteArray\")")
    private external fun brsCreateByteArray(): Dynamic

    @BrsInline("return ba.Count()")
    private external fun brsCount(ba: Dynamic): Int

    @BrsInline("return ba.Capacity()")
    private external fun brsCapacity(ba: Dynamic): Int

    @BrsInline("return ba.IsEmpty()")
    private external fun brsIsEmpty(ba: Dynamic): Boolean

    @BrsInline("ba.Clear()")
    private external fun brsClear(ba: Dynamic)

    @BrsInline("return ba[index]")
    private external fun brsGetByte(ba: Dynamic, index: Int): Int

    @BrsInline("ba[index] = value")
    private external fun brsSetByte(ba: Dynamic, index: Int, value: Int)

    @BrsInline("ba.Push(value)")
    private external fun brsPush(ba: Dynamic, value: Int)

    @BrsInline("return ba.Pop()")
    private external fun brsPop(ba: Dynamic): Int?

    @BrsInline("return ba.Peek()")
    private external fun brsPeek(ba: Dynamic): Int?

    @BrsInline("return ba.Shift()")
    private external fun brsShift(ba: Dynamic): Int?

    @BrsInline("ba.Unshift(value)")
    private external fun brsUnshift(ba: Dynamic, value: Int)

    @BrsInline("ba.FromAsciiString(str)")
    private external fun brsFromAsciiString(ba: Dynamic, str: String)

    @BrsInline("return ba.ToAsciiString()")
    private external fun brsToAsciiString(ba: Dynamic): String

    @BrsInline("ba.FromBase64String(base64)")
    private external fun brsFromBase64String(ba: Dynamic, base64: String)

    @BrsInline("return ba.ToBase64String()")
    private external fun brsToBase64String(ba: Dynamic): String

    @BrsInline("ba.FromHexString(hex)")
    private external fun brsFromHexString(ba: Dynamic, hex: String)

    @BrsInline("return ba.ToHexString()")
    private external fun brsToHexString(ba: Dynamic): String

    @BrsInline("return ba.ReadFile(path)")
    private external fun brsReadFile(ba: Dynamic, path: String): Boolean

    @BrsInline("return ba.WriteFile(path)")
    private external fun brsWriteFile(ba: Dynamic, path: String): Boolean

    @BrsInline("return ba.AppendFile(path)")
    private external fun brsAppendFile(ba: Dynamic, path: String): Boolean

    @BrsInline("return ba.GetCRC32()")
    private external fun brsGetCRC32(ba: Dynamic): Int

    @BrsInline("return ba.GetMD5()")
    private external fun brsGetMD5(ba: Dynamic): String

    @BrsInline("return ba.GetSHA1()")
    private external fun brsGetSHA1(ba: Dynamic): String

    @BrsInline("return ba.GetSHA256()")
    private external fun brsGetSHA256(ba: Dynamic): String

    @BrsInline("return ba.GetSHA512()")
    private external fun brsGetSHA512(ba: Dynamic): String

    @BrsInline("ba.Resize(newSize, fillValue)")
    private external fun brsResize(ba: Dynamic, newSize: Int, fillValue: Int)

    @BrsInline("ba.Append(other)")
    private external fun brsAppend(ba: Dynamic, other: Dynamic)
}
