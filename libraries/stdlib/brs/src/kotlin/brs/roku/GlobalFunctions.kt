/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs.roku

import kotlin.brs.BrsIntrinsic
import kotlin.brs.Dynamic

// =============================================================================
// Core Utility Functions
// =============================================================================
// These compile DIRECTLY to BrightScript global functions with zero overhead.
// Example: sleep(100) compiles to Sleep(100)

/**
 * Pauses script execution for the specified duration without consuming CPU cycles.
 *
 * @param milliseconds The number of milliseconds to pause (1000 ms = 1 second).
 * @see <a href="https://developer.roku.com/docs/references/brightscript/language/global-utility-functions.md#sleepmilliseconds-as-integer-as-void">Sleep</a>
 */
@BrsIntrinsic("brsIntrinsicSleep")
public external fun sleep(milliseconds: Int)

/**
 * Waits for an event on the specified message port.
 *
 * @param timeout Timeout in milliseconds. If 0, waits forever.
 * @param port The message port to wait on.
 * @return The event object that was posted to the port, or null if timeout occurred.
 * @see <a href="https://developer.roku.com/docs/references/brightscript/language/global-utility-functions.md#waittimeout-as-integer-port-as-object-as-object">Wait</a>
 */
@BrsIntrinsic("brsIntrinsicWait")
public external fun wait(timeout: Int, port: RoMessagePort): Dynamic?

/**
 * Returns the uptime of the system since the last reboot in seconds.
 *
 * @return The system uptime in seconds as a float.
 * @see <a href="https://developer.roku.com/docs/references/brightscript/language/global-utility-functions.md#uptimedummy-as-integer-as-float">UpTime</a>
 */
@BrsIntrinsic("brsIntrinsicUpTime")
public external fun upTime(): Float

/**
 * Returns a specific interface from an object.
 *
 * @param obj The object to get the interface from.
 * @param ifname The name of the interface (e.g., "ifArray").
 * @return The interface, or null if not found.
 * @see <a href="https://developer.roku.com/docs/references/brightscript/language/global-utility-functions.md#getinterfaceobject-as-object-ifname-as-string-as-interface">GetInterface</a>
 */
@BrsIntrinsic("brsIntrinsicGetInterface")
public external fun getInterface(obj: Any?, ifname: String): Dynamic?

/**
 * Finds which interface provides a specific function on an object.
 *
 * @param obj The object to search.
 * @param funName The function name to find.
 * @return The interface that provides the function, or null if not found.
 * @see <a href="https://developer.roku.com/docs/references/brightscript/language/global-utility-functions.md#findmemberfunctionobject-as-object-funname-as-string-as-interface">FindMemberFunction</a>
 */
@BrsIntrinsic("brsIntrinsicFindMemberFunction")
public external fun findMemberFunction(obj: Any?, funName: String): Dynamic?

// =============================================================================
// JSON Functions
// =============================================================================

/**
 * Parses a JSON string into a BrightScript object.
 *
 * @param jsonString The JSON string to parse (RFC4627 format).
 * @return The parsed object (roArray or roAssociativeArray), or null if invalid JSON.
 * @see <a href="https://developer.roku.com/docs/references/brightscript/language/global-utility-functions.md#parsejsonjsonstring-as-string-flags---as-string-as-object">ParseJson</a>
 */
@BrsIntrinsic("brsIntrinsicParseJson")
public external fun parseJson(jsonString: String): Dynamic?

/**
 * Formats an object as a JSON string.
 *
 * @param obj The object to format (must contain only supported types).
 * @return The JSON string, or empty string on error.
 * @see <a href="https://developer.roku.com/docs/references/brightscript/language/global-utility-functions.md#formatjsonjson-as-object-flags--0-as-integer-as-string">FormatJson</a>
 */
@BrsIntrinsic("brsIntrinsicFormatJson")
public external fun formatJson(obj: Any?): String

// =============================================================================
// File System Functions
// =============================================================================

/**
 * Returns the contents of a directory.
 *
 * @param path The directory path (e.g., "pkg:/images").
 * @return An roArray containing the filenames in the directory.
 * @see <a href="https://developer.roku.com/docs/references/brightscript/language/global-utility-functions.md#listdirpath-as-string-as-object">ListDir</a>
 */
@BrsIntrinsic("brsIntrinsicListDir")
public external fun listDir(path: String): RoArray

/**
 * Reads a file and returns its contents as a string.
 *
 * Files can be encoded as UTF-8 or UTF-16.
 *
 * @param filepath The path to the file.
 * @return The file contents, or empty string if the file cannot be read.
 * @see <a href="https://developer.roku.com/docs/references/brightscript/language/global-utility-functions.md#readasciifilefilepath-as-string-as-string">ReadAsciiFile</a>
 */
@BrsIntrinsic("brsIntrinsicReadAsciiFile")
public external fun readAsciiFile(filepath: String): String

/**
 * Writes a string to a file.
 *
 * The string is written as UTF-8 encoded.
 *
 * @param filepath The path to the file.
 * @param text The text to write.
 * @return True if the file was successfully written.
 * @see <a href="https://developer.roku.com/docs/references/brightscript/language/global-utility-functions.md#writeasciifilefilepath-as-string-text-as-string-as-boolean">WriteAsciiFile</a>
 */
@BrsIntrinsic("brsIntrinsicWriteAsciiFile")
public external fun writeAsciiFile(filepath: String, text: String): Boolean

/**
 * Copies a file.
 *
 * @param source The source file path.
 * @param destination The destination file path.
 * @return True if the copy was successful.
 * @see <a href="https://developer.roku.com/docs/references/brightscript/language/global-utility-functions.md#copyfilesource-as-string-destination-as-string-as-boolean">CopyFile</a>
 */
@BrsIntrinsic("brsIntrinsicCopyFile")
public external fun copyFile(source: String, destination: String): Boolean

/**
 * Moves (renames) a file.
 *
 * @param source The source file path.
 * @param destination The destination file path.
 * @return True if the move was successful.
 * @see <a href="https://developer.roku.com/docs/references/brightscript/language/global-utility-functions.md#movefilesource-as-string-destination-as-string-as-boolean">MoveFile</a>
 */
@BrsIntrinsic("brsIntrinsicMoveFile")
public external fun moveFile(source: String, destination: String): Boolean

/**
 * Deletes a file.
 *
 * @param file The file path to delete.
 * @return True if the file was deleted.
 * @see <a href="https://developer.roku.com/docs/references/brightscript/language/global-utility-functions.md#deletefilefile-as-string-as-boolean">DeleteFile</a>
 */
@BrsIntrinsic("brsIntrinsicDeleteFile")
public external fun deleteFile(file: String): Boolean

/**
 * Deletes an empty directory.
 *
 * @param dir The directory path to delete.
 * @return True if the directory was deleted. Only empty directories can be deleted.
 * @see <a href="https://developer.roku.com/docs/references/brightscript/language/global-utility-functions.md#deletedirectorydir-as-string-as-boolean">DeleteDirectory</a>
 */
@BrsIntrinsic("brsIntrinsicDeleteDirectory")
public external fun deleteDirectory(dir: String): Boolean

/**
 * Creates a directory.
 *
 * Only one directory level can be created at a time.
 *
 * @param dir The directory path to create.
 * @return True if the directory was created.
 * @see <a href="https://developer.roku.com/docs/references/brightscript/language/global-utility-functions.md#createdirectorydir-as-string-as-boolean">CreateDirectory</a>
 */
@BrsIntrinsic("brsIntrinsicCreateDirectory")
public external fun createDirectory(dir: String): Boolean

/**
 * Searches a directory for files matching a wildmat pattern.
 *
 * Pattern special characters:
 * - `?` matches any single character
 * - `*` matches zero or more characters
 * - `[...]` matches any character in the brackets
 *
 * @param path The directory to search.
 * @param pattern The wildmat pattern to match.
 * @return An roArray of matching filenames (not full paths).
 * @see <a href="https://developer.roku.com/docs/references/brightscript/language/global-utility-functions.md#matchfilespath-as-string-pattern_in-as-string-as-object">MatchFiles</a>
 */
@BrsIntrinsic("brsIntrinsicMatchFiles")
public external fun matchFiles(path: String, pattern: String): RoArray

/**
 * Formats a drive with the specified filesystem.
 *
 * @param drive The drive to format.
 * @param fsType The filesystem type.
 * @return True if successful.
 * @see <a href="https://developer.roku.com/docs/references/brightscript/language/global-utility-functions.md#formatdrivedrive-as-string--fs_type-as-string-as-boolean">FormatDrive</a>
 */
@BrsIntrinsic("brsIntrinsicFormatDrive")
public external fun formatDrive(drive: String, fsType: String): Boolean

// =============================================================================
// String Conversion Functions
// =============================================================================

/**
 * Converts a string to an integer.
 *
 * @param str The string to convert.
 * @return The integer value, or 0 if nothing could be parsed.
 * @see <a href="https://developer.roku.com/docs/references/brightscript/language/global-utility-functions.md#strtoistr-as-string-as-dynamic">StrToI</a>
 */
@BrsIntrinsic("brsIntrinsicStrToI")
public external fun strToI(str: String): Int

// =============================================================================
// System Functions
// =============================================================================

/**
 * Runs the garbage collector on the current thread.
 *
 * You normally don't need to call this function.
 *
 * @return An associative array with garbage collection statistics (COUNT, ORPHANED, ROOT).
 * @see <a href="https://developer.roku.com/docs/references/brightscript/language/global-utility-functions.md#rungarbagecollector-as-object">RunGarbageCollector</a>
 */
@BrsIntrinsic("brsIntrinsicRunGarbageCollector")
public external fun runGarbageCollector(): Dynamic

/**
 * Translates a string to the current locale.
 *
 * Looks for a translations.xml file in the XLIFF format in the pkg:/locale
 * subdirectory for the current locale.
 *
 * @param source The source string to translate.
 * @return The translated string, or the original if no translation is found.
 * @see <a href="https://developer.roku.com/docs/references/brightscript/language/global-utility-functions.md#trsource-as-string-as-string">Tr</a>
 */
@BrsIntrinsic("brsIntrinsicTr")
public external fun tr(source: String): String

/**
 * Requests a system reboot.
 *
 * Note: This feature is disabled on the Roku platform.
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/language/global-utility-functions.md#rebootsystem-as-void">RebootSystem</a>
 */
@BrsIntrinsic("brsIntrinsicRebootSystem")
public external fun rebootSystem()
