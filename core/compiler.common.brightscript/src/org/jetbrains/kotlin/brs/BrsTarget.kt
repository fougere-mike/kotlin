/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.brs

/**
 * Roku OS version targets for BrightScript compilation.
 *
 * Different Roku OS versions support different BrightScript language features.
 * This enum allows targeting specific versions for feature compatibility.
 */
enum class RokuOSVersion(
    val versionString: String,
    val major: Int,
    val minor: Int
) {
    /**
     * Roku OS 9.0 - Baseline for modern features.
     */
    ROKU_OS_9_0("9.0", 9, 0),

    /**
     * Roku OS 9.4 - Added try/catch/throw exception handling.
     */
    ROKU_OS_9_4("9.4", 9, 4),

    /**
     * Roku OS 10.0 - Added continue statement.
     */
    ROKU_OS_10_0("10.0", 10, 0),

    /**
     * Roku OS 11.0 - Various improvements.
     */
    ROKU_OS_11_0("11.0", 11, 0),

    /**
     * Roku OS 12.0 - Latest stable.
     */
    ROKU_OS_12_0("12.0", 12, 0),

    /**
     * Roku OS 13.0 - Latest.
     */
    ROKU_OS_13_0("13.0", 13, 0);

    /**
     * Whether this version supports try/catch/throw exception handling.
     */
    val supportsExceptions: Boolean
        get() = this >= ROKU_OS_9_4

    /**
     * Whether this version supports continue statements.
     */
    val supportsContinue: Boolean
        get() = this >= ROKU_OS_10_0

    companion object {
        /**
         * Default target Roku OS version.
         */
        val DEFAULT = ROKU_OS_9_4

        /**
         * Minimum supported Roku OS version.
         */
        val MINIMUM = ROKU_OS_9_0

        /**
         * Parse a version string to RokuOSVersion.
         */
        fun fromString(version: String): RokuOSVersion? {
            return entries.find { it.versionString == version }
        }

        /**
         * Get the minimum version that supports all specified features.
         */
        fun minimumForFeatures(
            needsExceptions: Boolean = false,
            needsContinue: Boolean = false
        ): RokuOSVersion {
            return when {
                needsContinue -> ROKU_OS_10_0
                needsExceptions -> ROKU_OS_9_4
                else -> MINIMUM
            }
        }
    }
}

/**
 * BrightScript compilation target configuration.
 */
data class BrsTargetConfig(
    /**
     * Minimum Roku OS version to target.
     */
    val minRokuOS: RokuOSVersion = RokuOSVersion.DEFAULT,

    /**
     * Whether to generate source maps for debugging.
     */
    val generateSourceMaps: Boolean = true,

    /**
     * Whether to minify output (remove comments, shorten names).
     */
    val minify: Boolean = false,

    /**
     * Whether to include debug print statements.
     */
    val debugMode: Boolean = false,

    /**
     * Whether to use strict type checking mode.
     */
    val strictMode: Boolean = true,

    /**
     * Whether to generate SceneGraph XML component files.
     */
    val generateXml: Boolean = true,

    /**
     * Output directory for generated files.
     */
    val outputDir: String = "out/brs",

    /**
     * Package name prefix for generated modules.
     */
    val packagePrefix: String = ""
) {
    /**
     * Check if a feature requiring exceptions is available.
     */
    fun supportsExceptions(): Boolean = minRokuOS.supportsExceptions

    /**
     * Check if a feature requiring continue is available.
     */
    fun supportsContinue(): Boolean = minRokuOS.supportsContinue

    companion object {
        /**
         * Default configuration.
         */
        val DEFAULT = BrsTargetConfig()

        /**
         * Development configuration with debug enabled.
         */
        val DEVELOPMENT = BrsTargetConfig(
            debugMode = true,
            minify = false,
            generateSourceMaps = true
        )

        /**
         * Production configuration optimized for deployment.
         */
        val PRODUCTION = BrsTargetConfig(
            debugMode = false,
            minify = true,
            generateSourceMaps = false
        )
    }
}
