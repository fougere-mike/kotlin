/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("DEPRECATION", "DEPRECATION_ERROR", "OVERRIDE_DEPRECATION")

package org.jetbrains.kotlin.gradle.targets.brs

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.process.ExecOperations
import org.gradle.work.Incremental
import org.jetbrains.kotlin.cli.common.arguments.K2BrsCompilerArguments
import org.jetbrains.kotlin.gradle.dsl.KotlinBrsCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinBrsCompilerOptionsHelper
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinBrsCompile as KotlinBrsCompileInterface
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerArgumentsProducer.CreateCompilerArgumentsContext
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerArgumentsProducer.CreateCompilerArgumentsContext.Companion.create
import org.jetbrains.kotlin.gradle.targets.brs.internal.KotlinBrsOptionsCompat
import org.jetbrains.kotlin.gradle.tasks.AbstractKotlinCompileTool
import javax.inject.Inject

/**
 * Task for compiling Kotlin source files to BrightScript.
 *
 * This task invokes the K2BrsCompiler to transform Kotlin IR to BrightScript code.
 */
@CacheableTask
abstract class KotlinBrsCompile @Inject constructor(
    final override val compilerOptions: KotlinBrsCompilerOptions,
    objectFactory: ObjectFactory,
    private val execOperations: ExecOperations,
) : AbstractKotlinCompileTool<K2BrsCompilerArguments>(objectFactory),
    KotlinBrsCompileInterface {

    private val kotlinBrsOptionsCompat by lazy { KotlinBrsOptionsCompat(compilerOptions) }

    // Required by AbstractKotlinCompileTool - BRS doesn't use build tools API
    @get:Internal
    override val runViaBuildToolsApi: Property<Boolean> = objectFactory
        .property(Boolean::class.java)
        .convention(false)

    // Required by TaskWithLocalState - BRS doesn't maintain local state directories
    @get:Internal
    override val localStateDirectories: ConfigurableFileCollection = objectFactory.fileCollection()

    /**
     * Deprecated kotlinOptions for compatibility.
     */
    @Deprecated("Use compilerOptions instead")
    @get:Internal
    override val kotlinOptions: KotlinCommonOptions
        get() = kotlinBrsOptionsCompat

    /**
     * The klib libraries to use during compilation.
     */
    @get:Classpath
    @get:Incremental
    abstract override val libraries: ConfigurableFileCollection

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
     * Provides the destination directory for the KotlinCompileTool interface.
     * Delegates to outputDirectory for compatibility.
     */
    @get:Internal
    override val destinationDirectory: DirectoryProperty
        get() = outputDirectory

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

    /**
     * Creates compiler arguments for the BRS compiler.
     * Used by the IDE for import and by other Kotlin Gradle plugin infrastructure.
     */
    override fun createCompilerArguments(
        context: CreateCompilerArgumentsContext
    ): K2BrsCompilerArguments = context.create<K2BrsCompilerArguments> {
        primitive { args ->
            KotlinBrsCompilerOptionsHelper.fillCompilerArguments(compilerOptions, args)
            args.outputDir = outputDirectory.get().asFile.absolutePath

            if (stdlibCompilation.getOrElse(false)) {
                args.freeArgs = args.freeArgs + listOf("-Xstdlib-compilation", "-Xallow-kotlin-package")
            }
        }

        dependencyClasspath { args ->
            if (libraries.files.isNotEmpty()) {
                args.libraries = libraries.files.joinToString(java.io.File.pathSeparator) { it.absolutePath }
            }
        }

        sources { args ->
            val excludes = excludePatterns.orNull ?: emptySet()
            val sourceFiles = sources.files.flatMap { sourceFile ->
                if (sourceFile.isDirectory) {
                    sourceFile.walkTopDown()
                        .filter { it.isFile && it.extension == "kt" && it.name !in excludes }
                        .toList()
                } else if (sourceFile.name !in excludes) {
                    listOf(sourceFile)
                } else {
                    emptyList()
                }
            }
            args.freeArgs = args.freeArgs + sourceFiles.map { it.absolutePath }
        }
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

        // Get exclude patterns
        val excludes = excludePatterns.orNull ?: emptySet()

        // Helper to check if file should be excluded
        fun shouldExclude(file: java.io.File): Boolean {
            return excludes.any { pattern -> file.name == pattern }
        }

        // Add source files - expand directories into individual .kt files
        // Use the inherited sources property from AbstractKotlinCompileTool
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
