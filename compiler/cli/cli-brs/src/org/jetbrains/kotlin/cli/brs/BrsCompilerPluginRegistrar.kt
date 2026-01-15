/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.brs

import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter
import org.jetbrains.kotlin.cli.brs.extensions.BrsFirExtensionRegistrar

/**
 * Compiler plugin registrar for BrightScript-specific extensions.
 *
 * This registrar is discovered via service loader by both:
 * - The CLI compiler (K2BrsCompiler)
 * - The IDE (KtCompilerPluginsProviderIdeImpl)
 *
 * It registers the SceneGraph layout generator which creates synthetic
 * nested classes and properties for @SGLayout annotated functions.
 */
class BrsCompilerPluginRegistrar : CompilerPluginRegistrar() {

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        FirExtensionRegistrarAdapter.registerExtension(BrsFirExtensionRegistrar())
    }

    override val supportsK2: Boolean
        get() = true
}
