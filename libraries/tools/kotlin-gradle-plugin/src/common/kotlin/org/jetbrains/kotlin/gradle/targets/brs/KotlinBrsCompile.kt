/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("DEPRECATION", "DEPRECATION_ERROR", "OVERRIDE_DEPRECATION")

package org.jetbrains.kotlin.gradle.targets.brs

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.process.ExecOperations
import org.jetbrains.kotlin.gradle.dsl.KotlinBrsCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinBrsCompile as KotlinBrsCompileInterface
import org.jetbrains.kotlin.gradle.targets.brs.internal.KotlinBrsOptionsCompat
import javax.inject.Inject

/**
 * Task for compiling Kotlin source files to BrightScript.
 *
 * This task invokes the K2BrsCompiler to transform Kotlin IR to BrightScript code.
 */
@CacheableTask
abstract class KotlinBrsCompile @Inject constructor(
    final override val compilerOptions: KotlinBrsCompilerOptions,
    private val execOperations: ExecOperations,
) : DefaultTask(), KotlinBrsCompileInterface {

    private val kotlinBrsOptionsCompat by lazy { KotlinBrsOptionsCompat(compilerOptions) }

    /**
     * Deprecated kotlinOptions for compatibility.
     */
    @Deprecated("Use compilerOptions instead")
    @get:Internal
    override val kotlinOptions: KotlinCommonOptions
        get() = kotlinBrsOptionsCompat

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
     * The compiler classpath (derived from compilerJar).
     * This is used internally for configuration cache compatibility.
     */
    @get:Classpath
    abstract val compilerClasspath: ConfigurableFileCollection

    /**
     * The output directory for generated .brs files.
     */
    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    /**
     * Enable stdlib compilation mode.
     * This adds -Xstdlib-compilation and -Xallow-kotlin-package flags.
     */
    @get:Input
    @get:Optional
    abstract val stdlibCompilation: Property<Boolean>

    /**
     * File name patterns to exclude from compilation.
     * Patterns are matched against the file name only (not the full path).
     */
    @get:Input
    @get:Optional
    abstract val excludePatterns: org.gradle.api.provider.SetProperty<String>

    init {
        group = "brightscript"
        description = "Compiles Kotlin sources to BrightScript"
    }

    /**
     * Returns the destination directory as a File.
     * Required for IntelliJ IDE tooling compatibility.
     */
    @get:Internal
    val destinationDir: java.io.File
        get() = outputDirectory.get().asFile

    /**
     * Returns the output file (the destination directory for BRS).
     * Required for IntelliJ IDE tooling compatibility.
     * For BRS, this returns the output directory since we produce multiple files.
     */
    @get:Internal
    val outputFile: java.io.File
        get() = outputDirectory.get().asFile

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

        // Get exclude patterns
        val excludes = excludePatterns.orNull ?: emptySet()

        // Helper to check if file should be excluded
        fun shouldExclude(file: java.io.File): Boolean {
            return excludes.any { pattern -> file.name == pattern }
        }

        // Add source files - expand directories into individual .kt files
        sources.files.forEach { sourceFile ->
            if (sourceFile.isDirectory) {
                // Recursively find all .kt files in directory
                sourceFile.walkTopDown()
                    .filter { it.isFile && it.extension == "kt" && !shouldExclude(it) }
                    .forEach { ktFile -> args.add(ktFile.absolutePath) }
            } else if (!shouldExclude(sourceFile)) {
                args.add(sourceFile.absolutePath)
            }
        }

        // Add output directory
        args.add("-output-dir")
        args.add(outputDir.absolutePath)

        // Add module name from compiler options
        if (compilerOptions.moduleName.isPresent) {
            args.add("-module-name")
            args.add(compilerOptions.moduleName.get())
        }

        // Add libraries
        if (libraries.files.isNotEmpty()) {
            args.add("-libraries")
            args.add(libraries.files.joinToString(java.io.File.pathSeparator) { it.absolutePath })
        }

        // Add Roku OS version from compiler options
        if (compilerOptions.minRokuOS.isPresent) {
            args.add("-min-roku-os")
            args.add(compilerOptions.minRokuOS.get())
        }

        // Add debug mode from compiler options
        if (compilerOptions.debugMode.getOrElse(false)) {
            args.add("-debug")
        }

        // Add XML generation from compiler options
        if (compilerOptions.generateXml.getOrElse(true)) {
            args.add("-generate-xml")
        }

        // Add minify from compiler options
        if (compilerOptions.minify.getOrElse(false)) {
            args.add("-minify")
        }

        // Add strict mode from compiler options
        if (compilerOptions.strictMode.getOrElse(false)) {
            args.add("-strict-mode")
        }

        // Add stdlib compilation mode flags
        if (stdlibCompilation.getOrElse(false)) {
            args.add("-Xstdlib-compilation")
            args.add("-Xallow-kotlin-package")
        }

        logger.info("Compiling Kotlin to BrightScript: ${sources.files.size} source files")
        logger.debug("Compiler arguments: $args")

        execOperations.javaexec { spec ->
            spec.mainClass.set("org.jetbrains.kotlin.cli.brs.K2BrsCompiler")
            spec.classpath = compilerClasspath
            spec.args = args
        }
    }
}
