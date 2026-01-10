/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.platform.brs

import org.jetbrains.kotlin.platform.SimplePlatform
import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.platform.toTargetPlatform

/**
 * Platform definition for BrightScript (Roku) compilation target.
 */
abstract class BrsPlatform : SimplePlatform("BrightScript") {
    override val oldFashionedDescription: String
        get() = "BrightScript for Roku"
}

/**
 * Default BrightScript platform targeting Roku OS 9.4+.
 */
object BrsPlatformDefault : BrsPlatform() {
    override val targetName: String
        get() = "brs"
}

/**
 * BrightScript platform targeting Roku OS 9.0 (legacy).
 */
object BrsPlatformLegacy : BrsPlatform() {
    override val targetName: String
        get() = "brs-legacy"
}

/**
 * Creates a TargetPlatform for BrightScript.
 */
fun brsTargetPlatform(): TargetPlatform = BrsPlatformDefault.toTargetPlatform()

/**
 * Creates a legacy TargetPlatform for BrightScript (Roku OS 9.0).
 */
fun brsLegacyTargetPlatform(): TargetPlatform = BrsPlatformLegacy.toTargetPlatform()

/**
 * Checks if a platform is BrightScript.
 */
fun TargetPlatform?.isBrs(): Boolean = this?.isNotEmpty() == true && all { it is BrsPlatform }

/**
 * BrightScript platforms registry, similar to JvmPlatforms, NativePlatforms, etc.
 */
@Suppress("DEPRECATION_ERROR")
object BrsPlatforms {
    val defaultBrsPlatform: TargetPlatform = BrsPlatformDefault.toTargetPlatform()
    val legacyBrsPlatform: TargetPlatform = BrsPlatformLegacy.toTargetPlatform()

    val unspecifiedBrsPlatform: TargetPlatform
        get() = CompatBrsPlatform

    val allBrsPlatforms: List<TargetPlatform> = listOf(
        defaultBrsPlatform,
        legacyBrsPlatform
    )

    @Deprecated(
        message = "Should be accessed only by compatibility layer, other clients should use 'defaultBrsPlatform'",
        level = DeprecationLevel.ERROR
    )
    object CompatBrsPlatform : TargetPlatform(setOf(BrsPlatformDefault))
}

/**
 * Extension to create singleton TargetPlatform.
 */
private fun SimplePlatform.toTargetPlatform(): TargetPlatform = TargetPlatform(setOf(this))
