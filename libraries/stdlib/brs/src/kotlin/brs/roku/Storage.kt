/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs.roku

import kotlin.brs.BrsInline
import kotlin.brs.Dynamic

/**
 * Provides access to the persistent registry storage.
 *
 * roRegistry provides access to the device's persistent registry,
 * organized into named sections. Each channel has its own isolated registry.
 *
 * Example usage:
 * ```kotlin
 * val registry = RoRegistry()
 * val sections = registry.getSectionList()
 * ```
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/components/roregistry.md">roRegistry</a>
 */
public class RoRegistry {

    private val native: Dynamic

    /**
     * Creates a new roRegistry instance.
     */
    public constructor() {
        native = brsCreateRegistry()
    }

    /**
     * Returns a list of all registry section names.
     *
     * @return List of section names.
     */
    public fun getSectionList(): Dynamic {
        return brsGetSectionList(native)
    }

    /**
     * Deletes an entire registry section.
     *
     * @param section The section name to delete.
     * @return True if successful.
     */
    public fun delete(section: String): Boolean {
        return brsDelete(native, section)
    }

    /**
     * Flushes all pending registry writes to persistent storage.
     *
     * @return True if successful.
     */
    public fun flush(): Boolean {
        return brsFlush(native)
    }

    // ==================== BrightScript Intrinsics ====================

    @BrsInline("return CreateObject(\"roRegistry\")")
    private external fun brsCreateRegistry(): Dynamic

    @BrsInline("return reg.GetSectionList()")
    private external fun brsGetSectionList(reg: Dynamic): Dynamic

    @BrsInline("return reg.Delete(section)")
    private external fun brsDelete(reg: Dynamic, section: String): Boolean

    @BrsInline("return reg.Flush()")
    private external fun brsFlush(reg: Dynamic): Boolean
}

/**
 * Provides access to a specific section of the persistent registry.
 *
 * roRegistrySection allows reading and writing key-value pairs
 * within a named section of the registry.
 *
 * Example usage:
 * ```kotlin
 * val section = RoRegistrySection("settings")
 * section.write("theme", "dark")
 * section.flush()
 *
 * val theme = section.read("theme")
 * ```
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/components/roregistrysection.md">roRegistrySection</a>
 */
public class RoRegistrySection {

    private val native: Dynamic

    /**
     * Creates a new roRegistrySection for the specified section name.
     *
     * @param section The section name.
     */
    public constructor(section: String) {
        native = brsCreateRegistrySection(section)
    }

    /**
     * Reads a value from the registry.
     *
     * @param key The key to read.
     * @return The value, or empty string if not found.
     */
    public fun read(key: String): String {
        return brsRead(native, key)
    }

    /**
     * Writes a value to the registry.
     *
     * @param key The key to write.
     * @param value The value to store.
     * @return True if successful.
     */
    public fun write(key: String, value: String): Boolean {
        return brsWrite(native, key, value)
    }

    /**
     * Deletes a key from the registry.
     *
     * @param key The key to delete.
     * @return True if successful.
     */
    public fun delete(key: String): Boolean {
        return brsDelete(native, key)
    }

    /**
     * Checks if a key exists in the registry.
     *
     * @param key The key to check.
     * @return True if the key exists.
     */
    public fun exists(key: String): Boolean {
        return brsExists(native, key)
    }

    /**
     * Flushes pending writes to persistent storage.
     *
     * @return True if successful.
     */
    public fun flush(): Boolean {
        return brsFlush(native)
    }

    /**
     * Returns a list of all keys in this section.
     *
     * @return List of key names.
     */
    public fun getKeyList(): Dynamic {
        return brsGetKeyList(native)
    }

    /**
     * Reads multiple keys at once.
     *
     * @param keys List of keys to read.
     * @return Map of key to value.
     */
    public fun readMulti(keys: Dynamic): Dynamic {
        return brsReadMulti(native, keys)
    }

    /**
     * Writes multiple keys at once.
     *
     * @param keyValues Map of key to value.
     * @return True if successful.
     */
    public fun writeMulti(keyValues: Dynamic): Boolean {
        return brsWriteMulti(native, keyValues)
    }

    // ==================== BrightScript Intrinsics ====================

    @BrsInline("return CreateObject(\"roRegistrySection\", section)")
    private external fun brsCreateRegistrySection(section: String): Dynamic

    @BrsInline("return rs.Read(key)")
    private external fun brsRead(rs: Dynamic, key: String): String

    @BrsInline("return rs.Write(key, value)")
    private external fun brsWrite(rs: Dynamic, key: String, value: String): Boolean

    @BrsInline("return rs.Delete(key)")
    private external fun brsDelete(rs: Dynamic, key: String): Boolean

    @BrsInline("return rs.Exists(key)")
    private external fun brsExists(rs: Dynamic, key: String): Boolean

    @BrsInline("return rs.Flush()")
    private external fun brsFlush(rs: Dynamic): Boolean

    @BrsInline("return rs.GetKeyList()")
    private external fun brsGetKeyList(rs: Dynamic): Dynamic

    @BrsInline("return rs.ReadMulti(keys)")
    private external fun brsReadMulti(rs: Dynamic, keys: Dynamic): Dynamic

    @BrsInline("return rs.WriteMulti(keyValues)")
    private external fun brsWriteMulti(rs: Dynamic, keyValues: Dynamic): Boolean
}

/**
 * Provides path parsing and manipulation functionality.
 *
 * roPath parses file and URL paths into their component parts.
 *
 * Example usage:
 * ```kotlin
 * val path = RoPath("pkg:/images/logo.png")
 * if (path.isValid()) {
 *     println("Extension: ${path.getExtension()}")
 *     println("Filename: ${path.getFilename()}")
 * }
 * ```
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/components/ropath.md">roPath</a>
 */
public class RoPath {

    private val native: Dynamic

    /**
     * Creates a new roPath for the specified path string.
     *
     * @param path The path to parse.
     */
    public constructor(path: String) {
        native = brsCreatePath(path)
    }

    /**
     * Checks if the path was parsed successfully.
     *
     * @return True if the path is valid.
     */
    public fun isValid(): Boolean {
        return brsIsValid(native)
    }

    /**
     * Changes the path to a new value.
     *
     * @param path The new path string.
     * @return True if the path was parsed successfully.
     */
    public fun change(path: String): Boolean {
        return brsChange(native, path)
    }

    /**
     * Returns the full path as a string.
     */
    public fun getString(): String {
        return brsGetString(native)
    }

    /**
     * Returns just the filename portion of the path.
     */
    public fun getFilename(): String {
        return brsGetFilename(native)
    }

    /**
     * Returns the parent directory path.
     */
    public fun getParent(): String {
        return brsGetParent(native)
    }

    /**
     * Returns the file extension (without the dot).
     */
    public fun getExtension(): String {
        return brsGetExtension(native)
    }

    /**
     * Splits the path into its component parts.
     *
     * @return Map with keys like "basename", "extension", "parent", "phy".
     */
    public fun split(): Dynamic {
        return brsSplit(native)
    }

    override fun toString(): String = getString()

    // ==================== BrightScript Intrinsics ====================

    @BrsInline("return CreateObject(\"roPath\", path)")
    private external fun brsCreatePath(path: String): Dynamic

    @BrsInline("return p.IsValid()")
    private external fun brsIsValid(p: Dynamic): Boolean

    @BrsInline("return p.Change(path)")
    private external fun brsChange(p: Dynamic, path: String): Boolean

    @BrsInline("return p.GetString()")
    private external fun brsGetString(p: Dynamic): String

    @BrsInline("return p.GetFilename()")
    private external fun brsGetFilename(p: Dynamic): String

    @BrsInline("return p.GetParent()")
    private external fun brsGetParent(p: Dynamic): String

    @BrsInline("return p.GetExtension()")
    private external fun brsGetExtension(p: Dynamic): String

    @BrsInline("return p.Split()")
    private external fun brsSplit(p: Dynamic): Dynamic
}

/**
 * Provides file system operations.
 *
 * roFileSystem allows checking disk space, file existence, and performing
 * file system operations like copy, move, and delete.
 *
 * Example usage:
 * ```kotlin
 * val fs = RoFileSystem()
 * if (fs.exists("tmp:/data.json")) {
 *     val stat = fs.stat("tmp:/data.json")
 *     println("Size: ${stat}")
 * }
 * ```
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/components/rofilesystem.md">roFileSystem</a>
 */
public class RoFileSystem {

    private val native: Dynamic

    /**
     * Creates a new roFileSystem instance.
     */
    public constructor() {
        native = brsCreateFileSystem()
    }

    /**
     * Checks if a file or directory exists.
     *
     * @param path The path to check.
     * @return True if the path exists.
     */
    public fun exists(path: String): Boolean {
        return brsExists(native, path)
    }

    /**
     * Returns file/directory information.
     *
     * @param path The path to stat.
     * @return Map with file info, or invalid if not found.
     */
    public fun stat(path: String): Dynamic {
        return brsStat(native, path)
    }

    /**
     * Returns the list of volumes (storage locations).
     *
     * @return List of volume names.
     */
    public fun getVolumeList(): Dynamic {
        return brsGetVolumeList(native)
    }

    /**
     * Returns information about a volume.
     *
     * @param volume The volume name (e.g., "tmp", "cachefs").
     * @return Map with volume info.
     */
    public fun getVolumeInfo(volume: String): Dynamic {
        return brsGetVolumeInfo(native, volume)
    }

    /**
     * Returns the list of files and directories in a directory.
     *
     * @param path The directory path.
     * @return List of file/directory names.
     */
    public fun getDirectoryListing(path: String): Dynamic {
        return brsGetDirectoryListing(native, path)
    }

    /**
     * Creates a directory.
     *
     * @param path The directory path to create.
     * @return True if successful.
     */
    public fun createDirectory(path: String): Boolean {
        return brsCreateDirectory(native, path)
    }

    /**
     * Deletes a file or directory.
     *
     * @param path The path to delete.
     * @return True if successful.
     */
    public fun delete(path: String): Boolean {
        return brsDelete(native, path)
    }

    /**
     * Copies a file.
     *
     * @param source Source file path.
     * @param destination Destination file path.
     * @return True if successful.
     */
    public fun copyFile(source: String, destination: String): Boolean {
        return brsCopyFile(native, source, destination)
    }

    /**
     * Moves/renames a file.
     *
     * @param source Source file path.
     * @param destination Destination file path.
     * @return True if successful.
     */
    public fun moveFile(source: String, destination: String): Boolean {
        return brsMoveFile(native, source, destination)
    }

    /**
     * Matches files against a pattern.
     *
     * @param path Directory path to search.
     * @param pattern Pattern to match (e.g., "*.json").
     * @return List of matching files.
     */
    public fun match(path: String, pattern: String): Dynamic {
        return brsMatch(native, path, pattern)
    }

    /**
     * Finds files recursively.
     *
     * @param path Directory path to search.
     * @param regex Regular expression pattern.
     * @return List of matching file paths.
     */
    public fun find(path: String, regex: String): Dynamic {
        return brsFind(native, path, regex)
    }

    /**
     * Finds files recursively with depth limit.
     *
     * @param path Directory path to search.
     * @param regex Regular expression pattern.
     * @param maxDepth Maximum recursion depth.
     * @return List of matching file paths.
     */
    public fun findRecurse(path: String, regex: String, maxDepth: Int = 10): Dynamic {
        return brsFindRecurse(native, path, regex, maxDepth)
    }

    // ==================== BrightScript Intrinsics ====================

    @BrsInline("return CreateObject(\"roFileSystem\")")
    private external fun brsCreateFileSystem(): Dynamic

    @BrsInline("return fs.Exists(path)")
    private external fun brsExists(fs: Dynamic, path: String): Boolean

    @BrsInline("return fs.Stat(path)")
    private external fun brsStat(fs: Dynamic, path: String): Dynamic

    @BrsInline("return fs.GetVolumeList()")
    private external fun brsGetVolumeList(fs: Dynamic): Dynamic

    @BrsInline("return fs.GetVolumeInfo(volume)")
    private external fun brsGetVolumeInfo(fs: Dynamic, volume: String): Dynamic

    @BrsInline("return fs.GetDirectoryListing(path)")
    private external fun brsGetDirectoryListing(fs: Dynamic, path: String): Dynamic

    @BrsInline("return fs.CreateDirectory(path)")
    private external fun brsCreateDirectory(fs: Dynamic, path: String): Boolean

    @BrsInline("return fs.Delete(path)")
    private external fun brsDelete(fs: Dynamic, path: String): Boolean

    @BrsInline("return fs.CopyFile(source, destination)")
    private external fun brsCopyFile(fs: Dynamic, source: String, destination: String): Boolean

    @BrsInline("return fs.MoveFile(source, destination)")
    private external fun brsMoveFile(fs: Dynamic, source: String, destination: String): Boolean

    @BrsInline("return fs.Match(path, pattern)")
    private external fun brsMatch(fs: Dynamic, path: String, pattern: String): Dynamic

    @BrsInline("return fs.Find(path, regex)")
    private external fun brsFind(fs: Dynamic, path: String, regex: String): Dynamic

    @BrsInline("return fs.FindRecurse(path, regex, maxDepth)")
    private external fun brsFindRecurse(fs: Dynamic, path: String, regex: String, maxDepth: Int): Dynamic
}
