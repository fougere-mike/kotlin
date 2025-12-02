/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.dsl

import org.jetbrains.kotlin.cli.common.arguments.K2BrsCompilerArguments

internal object KotlinBrsCompilerOptionsHelper {

    internal fun fillCompilerArguments(
        from: KotlinBrsCompilerOptions,
        args: K2BrsCompilerArguments,
    ) {
        KotlinCommonCompilerOptionsHelper.fillCompilerArguments(from, args)
        args.moduleName = from.moduleName.orNull
        args.minRokuOS = from.minRokuOS.orNull
        args.debugMode = from.debugMode.get()
        args.generateXml = from.generateXml.get()
        args.minify = from.minify.get()
        args.strictMode = from.strictMode.get()
    }

    internal fun syncOptionsAsConvention(
        from: KotlinBrsCompilerOptions,
        into: KotlinBrsCompilerOptions,
    ) {
        KotlinCommonCompilerOptionsHelper.syncOptionsAsConvention(from, into)
        into.moduleName.convention(from.moduleName)
        into.minRokuOS.convention(from.minRokuOS)
        into.debugMode.convention(from.debugMode)
        into.generateXml.convention(from.generateXml)
        into.minify.convention(from.minify)
        into.strictMode.convention(from.strictMode)
    }
}
