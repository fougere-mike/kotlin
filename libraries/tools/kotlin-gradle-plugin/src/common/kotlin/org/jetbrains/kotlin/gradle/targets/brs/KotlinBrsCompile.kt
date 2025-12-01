/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.brs

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.process.ExecOperations
import javax.inject.Inject

/**
 * Task for compiling Kotlin source files to BrightScript.
 *
 * This task invokes the K2BrsCompiler to transform Kotlin IR to BrightScript code.
 */
@CacheableTask
abstract class KotlinBrsCompile @Inject constructor(
    private val execOperations: ExecOperations
) : DefaultTask() {

    /**
     * The Kotlin source files to compile.
     */
    @get:InputFiles
    @get:SkipWhenEmpty
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sources: ConfigurableFileCollection

    /**
     * The klib libraries to use during compilation.
     */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:Optional
    abstract val libraries: ConfigurableFileCollection

    /**
     * The compiler JAR file (kotlinc-brs fat jar).
     * If not set, the task will skip compilation with a warning.
     */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    @get:Optional
    abstract val compilerJar: RegularFileProperty

    /**
     * The output directory for generated .brs files.
     */
    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    /**
     * The module name for the compilation.
     */
    @get:Input
    abstract val moduleName: Property<String>

    /**
     * Minimum Roku OS version to target.
     */
    @get:Input
    @get:Optional
    abstract val minRokuOS: Property<String>

    /**
     * Enable debug mode.
     */
    @get:Input
    @get:Optional
    abstract val debugMode: Property<Boolean>

    /**
     * Enable SceneGraph XML generation.
     */
    @get:Input
    @get:Optional
    abstract val generateXml: Property<Boolean>

    init {
        group = "brightscript"
        description = "Compiles Kotlin sources to BrightScript"
    }

    @TaskAction
    fun compile() {
        // Check if compiler JAR is configured
        if (!compilerJar.isPresent) {
            logger.warn("BrightScript compiler JAR not configured. Skipping compilation.")
            logger.warn("To enable compilation, configure the 'compilerJar' property with the path to kotlinc-brs fat jar.")
            return
        }

        val outputDir = outputDirectory.get().asFile
        outputDir.mkdirs()

        val args = mutableListOf<String>()

        // Add source files
        sources.files.forEach { sourceFile ->
            args.add(sourceFile.absolutePath)
        }

        // Add output directory
        args.add("-output-dir")
        args.add(outputDir.absolutePath)

        // Add module name
        if (moduleName.isPresent) {
            args.add("-module-name")
            args.add(moduleName.get())
        }

        // Add libraries
        if (libraries.files.isNotEmpty()) {
            args.add("-libraries")
            args.add(libraries.files.joinToString(java.io.File.pathSeparator) { it.absolutePath })
        }

        // Add Roku OS version
        if (minRokuOS.isPresent) {
            args.add("-min-roku-os")
            args.add(minRokuOS.get())
        }

        // Add debug mode
        if (debugMode.getOrElse(false)) {
            args.add("-debug")
        }

        // Add XML generation
        if (generateXml.getOrElse(true)) {
            args.add("-generate-xml")
        }

        logger.info("Compiling Kotlin to BrightScript: ${sources.files.size} source files")
        logger.debug("Compiler arguments: $args")

        execOperations.javaexec { spec ->
            spec.mainClass.set("org.jetbrains.kotlin.cli.brs.K2BrsCompiler")
            spec.classpath = project.files(compilerJar)
            spec.args = args
        }
    }
}
