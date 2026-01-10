/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:JvmName("BrsIdePlatformUtil")
@file:Suppress("DEPRECATION_ERROR", "DeprecatedCallableAddReplaceWith")

package org.jetbrains.kotlin.platform.impl

import org.jetbrains.kotlin.cli.common.arguments.CommonCompilerArguments
import org.jetbrains.kotlin.cli.common.arguments.K2BrsCompilerArguments
import org.jetbrains.kotlin.platform.IdePlatform
import org.jetbrains.kotlin.platform.IdePlatformKind
import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.platform.TargetPlatformVersion
import org.jetbrains.kotlin.platform.brs.brsTargetPlatform
import org.jetbrains.kotlin.platform.brs.isBrs

/**
 * IDE Platform Kind for BrightScript/Roku targets.
 */
object BrsIdePlatformKind : IdePlatformKind() {
    override fun supportsTargetPlatform(platform: TargetPlatform): Boolean = platform.isBrs()

    override fun platformByCompilerArguments(arguments: CommonCompilerArguments): TargetPlatform? {
        return if (arguments is K2BrsCompilerArguments)
            brsTargetPlatform()
        else
            null
    }

    override fun createArguments(): K2BrsCompilerArguments {
        return K2BrsCompilerArguments()
    }

    override val defaultPlatform: TargetPlatform
        get() = brsTargetPlatform()

    @Deprecated(
        message = "IdePlatform is deprecated and will be removed soon, please, migrate to org.jetbrains.kotlin.platform.TargetPlatform",
        level = DeprecationLevel.ERROR
    )
    override fun getDefaultPlatform(): IdePlatform<*, *> = Platform

    override val argumentsClass
        get() = K2BrsCompilerArguments::class.java

    override val name
        get() = "BRS"

    @Deprecated(
        message = "IdePlatform is deprecated and will be removed soon, please, migrate to org.jetbrains.kotlin.platform.TargetPlatform",
        level = DeprecationLevel.ERROR
    )
    object Platform : IdePlatform<BrsIdePlatformKind, K2BrsCompilerArguments>() {
        override val kind get() = BrsIdePlatformKind
        override val version get() = TargetPlatformVersion.NoVersion
        override fun createArguments(init: K2BrsCompilerArguments.() -> Unit) = K2BrsCompilerArguments().apply(init)
    }
}

val IdePlatformKind?.isKotlinBrs
    get() = this is BrsIdePlatformKind

@Deprecated(
    message = "IdePlatform is deprecated and will be removed soon, please, migrate to org.jetbrains.kotlin.platform.TargetPlatform",
    level = DeprecationLevel.ERROR
)
val IdePlatform<*, *>?.isKotlinBrs
    get() = this is BrsIdePlatformKind.Platform
