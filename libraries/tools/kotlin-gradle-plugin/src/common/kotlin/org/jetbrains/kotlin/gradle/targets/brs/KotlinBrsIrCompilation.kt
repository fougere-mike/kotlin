/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("DEPRECATION", "TYPEALIAS_EXPANSION_DEPRECATION")

package org.jetbrains.kotlin.gradle.targets.brs

import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonOptions
import org.jetbrains.kotlin.gradle.plugin.mpp.DeprecatedAbstractKotlinCompilationToRunnableFiles
import org.jetbrains.kotlin.gradle.plugin.mpp.compilationImpl.KotlinCompilationImpl
import javax.inject.Inject

/**
 * Compilation for Kotlin/BrightScript target.
 *
 * Extends DeprecatedAbstractKotlinCompilationToRunnableFiles to enable runtime elements
 * configuration in CreateTargetConfigurationsSideEffect, which is required for proper
 * Maven publication with variant attributes.
 */
open class KotlinBrsIrCompilation @Inject internal constructor(
    compilation: KotlinCompilationImpl
) : DeprecatedAbstractKotlinCompilationToRunnableFiles<KotlinCommonOptions>(compilation) {

    override val target: KotlinBrsIrTarget
        get() = super.target as KotlinBrsIrTarget

    /**
     * Access the BRS-specific compile task.
     */
    val brsCompileTaskProvider: TaskProvider<KotlinBrsCompile>
        get() {
            @Suppress("UNCHECKED_CAST")
            return compilation.compileTaskProvider as TaskProvider<KotlinBrsCompile>
        }
}
