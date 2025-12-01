/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("DEPRECATION")

package org.jetbrains.kotlin.gradle.targets.brs

import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonOptions
import org.jetbrains.kotlin.gradle.plugin.mpp.AbstractKotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.compilationImpl.KotlinCompilationImpl
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import javax.inject.Inject

/**
 * Compilation for Kotlin/BrightScript target.
 */
open class KotlinBrsIrCompilation @Inject internal constructor(
    compilation: KotlinCompilationImpl
) : AbstractKotlinCompilation<KotlinCommonOptions>(compilation) {

    override val target: KotlinBrsIrTarget
        get() = super.target as KotlinBrsIrTarget

    @Suppress("UNCHECKED_CAST")
    override val compileTaskProvider: TaskProvider<KotlinCompilationTask<KotlinCommonCompilerOptions>>
        get() = compilation.compileTaskProvider as TaskProvider<KotlinCompilationTask<KotlinCommonCompilerOptions>>
}
