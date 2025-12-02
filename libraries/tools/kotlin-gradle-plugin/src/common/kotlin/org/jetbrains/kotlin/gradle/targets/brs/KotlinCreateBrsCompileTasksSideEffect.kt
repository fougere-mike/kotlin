/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.brs

import org.gradle.api.plugins.BasePlugin
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.jetbrains.kotlin.gradle.dsl.KotlinBrsCompilerOptionsDefault
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilationInfo
import org.jetbrains.kotlin.gradle.plugin.mpp.compilationImpl.KotlinCompilationSideEffect
import org.jetbrains.kotlin.gradle.tasks.dependsOn
import org.jetbrains.kotlin.gradle.tasks.registerTask
import org.jetbrains.kotlin.gradle.utils.newInstance

/**
 * Registers and configures the [KotlinBrsCompile] task for [KotlinBrsIrCompilation].
 *
 * This side effect is responsible for wiring up the BrightScript compile task
 * to the Kotlin compilation lifecycle.
 */
internal val KotlinCreateBrsCompileTasksSideEffect = KotlinCompilationSideEffect<KotlinBrsIrCompilation> { compilation ->
    val project = compilation.project
    val compilationInfo = KotlinCompilationInfo(compilation)

    // Create compiler options for this compilation
    val compilerOptions = project.objects.newInstance<KotlinBrsCompilerOptionsDefault>()

    val kotlinBrsCompile = project.registerTask(
        compilation.compileKotlinTaskName,
        KotlinBrsCompile::class.java,
        constructorArgs = listOf(compilerOptions)
    ) { task ->
        task.group = BasePlugin.BUILD_GROUP
        task.description = "Compiles Kotlin sources to BrightScript for the '${compilationInfo.compilationName}' " +
                "compilation in target '${compilationInfo.targetDisambiguationClassifier}'."

        // Configure output directory
        task.outputDirectory.set(
            project.layout.buildDirectory.dir("brs/${compilationInfo.targetDisambiguationClassifier}/${compilationInfo.compilationName}")
        )

        // Configure module name from compilation
        task.compilerOptions.moduleName.set(compilationInfo.moduleName)

        // Wire sources only from the compilation's own source set (not transitive commonMain)
        // This is intentionally different from other targets - BRS doesn't support all common sources yet
        task.sources.from(compilation.defaultSourceSet.kotlin.sourceDirectories)

        // Wire library dependencies from the compilation configuration
        // This allows the BRS compiler to resolve symbols from kotlin-stdlib and other dependencies
        task.libraries.from(
            compilation.configurations.compileDependencyConfiguration
        )

        // Wire compiler classpath from compilerJar for configuration cache compatibility
        // Use a provider to avoid eagerly querying the optional compilerJar property
        task.compilerClasspath.from(
            task.compilerJar.map { listOf(it) }.orElse(emptyList())
        )

        // Note: compilerJar is not configured by default.
        // Users must configure it manually if they want to generate BrightScript output.
        // If not configured, the task will skip with a warning.
    }

    // Wire the compile task output to the compilation's classes directories
    compilationInfo.classesDirs.from(kotlinBrsCompile.map { it.outputDirectory })

    // Make lifecycle tasks depend on the compile task
    project.tasks.named(compilation.compileAllTaskName).dependsOn(kotlinBrsCompile)
    project.tasks.named(LifecycleBasePlugin.ASSEMBLE_TASK_NAME).dependsOn(kotlinBrsCompile)
}
