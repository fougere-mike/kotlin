/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs.roku

import kotlin.brs.BrsCreateObject
import kotlin.brs.Dynamic

/**
 * Provides access to the persistent registry storage.
 *
 * roRegistry provides access to the device's persistent registry,
 * organized into named sections. Each channel has its own isolated registry.
 *
 * Example usage:
 * ```kotlin
 * val registry = RoRegistry.create()
 * val sections = registry.getSectionList()
 * ```
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/components/roregistry.md">roRegistry</a>
 */
public external interface RoRegistry {
    /**
     * Returns a list of all registry section names.
     *
     * @return List of section names.
     */
    public fun getSectionList(): Dynamic

    /**
     * Deletes an entire registry section.
     *
     * @param section The section name to delete.
     * @return True if successful.
     */
    public fun delete(section: String): Boolean

    /**
     * Flushes all pending registry writes to persistent storage.
     *
     * @return True if successful.
     */
    public fun flush(): Boolean

    public companion object {
        /**
         * Creates a new roRegistry instance.
         *
         * Compiles to: `CreateObject("roRegistry")`
         *
         * @return A new RoRegistry instance.
         */
        @BrsCreateObject("roRegistry")
        public fun create(): RoRegistry = definedExternally
    }
}

/**
 * Provides access to a specific section of the persistent registry.
 *
 * roRegistrySection allows reading and writing key-value pairs
 * within a named section of the registry.
 *
 * Example usage:
 * ```kotlin
 * val section = RoRegistrySection.create("settings")
 * section.write("theme", "dark")
 * section.flush()
 *
 * val theme = section.read("theme")
 * ```
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/components/roregistrysection.md">roRegistrySection</a>
 */
public external interface RoRegistrySection {
    /**
     * Reads a value from the registry.
     *
     * @param key The key to read.
     * @return The value, or empty string if not found.
     */
    public fun read(key: String): String

    /**
     * Writes a value to the registry.
     *
     * @param key The key to write.
     * @param value The value to store.
     * @return True if successful.
     */
    public fun write(key: String, value: String): Boolean

    /**
     * Deletes a key from the registry.
     *
     * @param key The key to delete.
     * @return True if successful.
     */
    public fun delete(key: String): Boolean

    /**
     * Checks if a key exists in the registry.
     *
     * @param key The key to check.
     * @return True if the key exists.
     */
    public fun exists(key: String): Boolean

    /**
     * Flushes pending writes to persistent storage.
     *
     * @return True if successful.
     */
    public fun flush(): Boolean

    /**
     * Returns a list of all keys in this section.
     *
     * @return List of key names.
     */
    public fun getKeyList(): Dynamic

    /**
     * Reads multiple keys at once.
     *
     * @param keys List of keys to read.
     * @return Map of key to value.
     */
    public fun readMulti(keys: Any?): Dynamic

    /**
     * Writes multiple keys at once.
     *
     * @param keyValues Map of key to value.
     * @return True if successful.
     */
    public fun writeMulti(keyValues: Any?): Boolean

    public companion object {
        /**
         * Creates a new roRegistrySection for the specified section name.
         *
         * Compiles to: `CreateObject("roRegistrySection", section)`
         *
         * @param section The section name.
         * @return A new RoRegistrySection instance.
         */
        @BrsCreateObject("roRegistrySection")
        public fun create(section: String): RoRegistrySection = definedExternally
    }
}

/**
 * Provides path parsing and manipulation functionality.
 *
 * roPath parses file and URL paths into their component parts.
 *
 * Example usage:
 * ```kotlin
 * val path = RoPath.create("pkg:/images/logo.png")
 * if (path.isValid()) {
 *     println("Extension: ${path.getExtension()}")
 *     println("Filename: ${path.getFilename()}")
 * }
 * ```
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/components/ropath.md">roPath</a>
 */
public external interface RoPath {
    /**
     * Checks if the path was parsed successfully.
     *
     * @return True if the path is valid.
     */
    public fun isValid(): Boolean

    /**
     * Changes the path to a new value.
     *
     * @param path The new path string.
     * @return True if the path was parsed successfully.
     */
    public fun change(path: String): Boolean

    /**
     * Returns the full path as a string.
     */
    public fun getString(): String

    /**
     * Returns just the filename portion of the path.
     */
    public fun getFilename(): String

    /**
     * Returns the parent directory path.
     */
    public fun getParent(): String

    /**
     * Returns the file extension (without the dot).
     */
    public fun getExtension(): String

    /**
     * Splits the path into its component parts.
     *
     * @return Map with keys like "basename", "extension", "parent", "phy".
     */
    public fun split(): Dynamic

    public companion object {
        /**
         * Creates a new roPath for the specified path string.
         *
         * Compiles to: `CreateObject("roPath", path)`
         *
         * @param path The path to parse.
         * @return A new RoPath instance.
         */
        @BrsCreateObject("roPath")
        public fun create(path: String): RoPath = definedExternally
    }
}

/**
 * Provides file system operations.
 *
 * roFileSystem allows checking disk space, file existence, and performing
 * file system operations like copy, move, and delete.
 *
 * Example usage:
 * ```kotlin
 * val fs = RoFileSystem.create()
 * if (fs.exists("tmp:/data.json")) {
 *     val stat = fs.stat("tmp:/data.json")
 *     println("Size: ${stat}")
 * }
 * ```
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/components/rofilesystem.md">roFileSystem</a>
 */
public external interface RoFileSystem {
    /**
     * Checks if a file or directory exists.
     *
     * @param path The path to check.
     * @return True if the path exists.
     */
    public fun exists(path: String): Boolean

    /**
     * Returns file/directory information.
     *
     * @param path The path to stat.
     * @return Map with file info, or invalid if not found.
     */
    public fun stat(path: String): Dynamic

    /**
     * Returns the list of volumes (storage locations).
     *
     * @return List of volume names.
     */
    public fun getVolumeList(): Dynamic

    /**
     * Returns information about a volume.
     *
     * @param volume The volume name (e.g., "tmp", "cachefs").
     * @return Map with volume info.
     */
    public fun getVolumeInfo(volume: String): Dynamic

    /**
     * Returns the list of files and directories in a directory.
     *
     * @param path The directory path.
     * @return List of file/directory names.
     */
    public fun getDirectoryListing(path: String): Dynamic

    /**
     * Creates a directory.
     *
     * @param path The directory path to create.
     * @return True if successful.
     */
    public fun createDirectory(path: String): Boolean

    /**
     * Deletes a file or directory.
     *
     * @param path The path to delete.
     * @return True if successful.
     */
    public fun delete(path: String): Boolean

    /**
     * Copies a file.
     *
     * @param source Source file path.
     * @param destination Destination file path.
     * @return True if successful.
     */
    public fun copyFile(source: String, destination: String): Boolean

    /**
     * Moves/renames a file.
     *
     * @param source Source file path.
     * @param destination Destination file path.
     * @return True if successful.
     */
    public fun moveFile(source: String, destination: String): Boolean

    /**
     * Matches files against a pattern.
     *
     * @param path Directory path to search.
     * @param pattern Pattern to match (e.g., "*.json").
     * @return List of matching files.
     */
    public fun match(path: String, pattern: String): Dynamic

    /**
     * Finds files recursively.
     *
     * @param path Directory path to search.
     * @param regex Regular expression pattern.
     * @return List of matching file paths.
     */
    public fun find(path: String, regex: String): Dynamic

    /**
     * Finds files recursively with depth limit.
     *
     * @param path Directory path to search.
     * @param regex Regular expression pattern.
     * @param maxDepth Maximum recursion depth.
     * @return List of matching file paths.
     */
    public fun findRecurse(path: String, regex: String, maxDepth: Int): Dynamic

    public companion object {
        /**
         * Creates a new roFileSystem instance.
         *
         * Compiles to: `CreateObject("roFileSystem")`
         *
         * @return A new RoFileSystem instance.
         */
        @BrsCreateObject("roFileSystem")
        public fun create(): RoFileSystem = definedExternally
    }
}
