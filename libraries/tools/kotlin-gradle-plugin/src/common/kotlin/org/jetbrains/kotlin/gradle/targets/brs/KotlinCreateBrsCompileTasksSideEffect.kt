/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.brs

import org.gradle.api.plugins.BasePlugin
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilationInfo
import org.jetbrains.kotlin.gradle.plugin.mpp.compilationImpl.KotlinCompilationSideEffect
import org.jetbrains.kotlin.gradle.tasks.dependsOn
import org.jetbrains.kotlin.gradle.tasks.registerTask

/**
 * Registers and configures the [KotlinBrsCompile] task for [KotlinBrsIrCompilation].
 *
 * This side effect is responsible for wiring up the BrightScript compile task
 * to the Kotlin compilation lifecycle.
 */
internal val KotlinCreateBrsCompileTasksSideEffect = KotlinCompilationSideEffect<KotlinBrsIrCompilation> { compilation ->
    val project = compilation.project
    val compilationInfo = KotlinCompilationInfo(compilation)

    val kotlinBrsCompile = project.registerTask<KotlinBrsCompile>(
        compilation.compileKotlinTaskName
    ) { task ->
        task.group = BasePlugin.BUILD_GROUP
        task.description = "Compiles Kotlin sources to BrightScript for the '${compilationInfo.compilationName}' " +
                "compilation in target '${compilationInfo.targetDisambiguationClassifier}'."

        // Configure output directory
        task.outputDirectory.set(
            project.layout.buildDirectory.dir("brs/${compilationInfo.targetDisambiguationClassifier}/${compilationInfo.compilationName}")
        )

        // Configure module name from compilation
        task.moduleName.set(compilationInfo.moduleName)

        // Wire sources from the compilation's default source set
        task.sources.from(compilation.allKotlinSourceSets.map { sourceSet ->
            sourceSet.kotlin.sourceDirectories
        })
    }

    // Wire the compile task output to the compilation's classes directories
    compilationInfo.classesDirs.from(kotlinBrsCompile.map { it.outputDirectory })

    // Make lifecycle tasks depend on the compile task
    project.tasks.named(compilation.compileAllTaskName).dependsOn(kotlinBrsCompile)
    project.tasks.named(LifecycleBasePlugin.ASSEMBLE_TASK_NAME).dependsOn(kotlinBrsCompile)
}
