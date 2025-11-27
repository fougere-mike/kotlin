/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.brs

import com.intellij.openapi.Disposable
import org.jetbrains.kotlin.brs.BrsTargetConfig
import org.jetbrains.kotlin.brs.RokuOSVersion
import org.jetbrains.kotlin.cli.common.CLICompiler
import org.jetbrains.kotlin.cli.common.CommonCompilerPerformanceManager
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.common.arguments.K2BrsCompilerArguments
import org.jetbrains.kotlin.cli.common.arguments.K2BrsArgumentConstants
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.Services
import org.jetbrains.kotlin.ir.backend.brs.BrsCompiler
import org.jetbrains.kotlin.metadata.deserialization.BinaryVersion
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.utils.KotlinPaths
import java.io.File

/**
 * CLI compiler for Kotlin to BrightScript.
 *
 * This compiler takes Kotlin source files and produces BrightScript (.brs) output
 * suitable for Roku application development.
 */
class K2BrsCompiler : CLICompiler<K2BrsCompilerArguments>() {

    class K2BrsCompilerPerformanceManager : CommonCompilerPerformanceManager("Kotlin to BrightScript Compiler")

    override val defaultPerformanceManager: CommonCompilerPerformanceManager = K2BrsCompilerPerformanceManager()

    override fun createArguments(): K2BrsCompilerArguments = K2BrsCompilerArguments()

    override fun createMetadataVersion(versionArray: IntArray): BinaryVersion {
        // Use a simple version for now
        return object : BinaryVersion(*versionArray) {
            override fun isCompatibleWithCurrentCompilerVersion(): Boolean = true
        }
    }

    override fun doExecute(
        arguments: K2BrsCompilerArguments,
        configuration: CompilerConfiguration,
        rootDisposable: Disposable,
        paths: KotlinPaths?
    ): ExitCode {
        val messageCollector = configuration.getNotNull(CommonConfigurationKeys.MESSAGE_COLLECTOR_KEY)

        // Validate arguments
        if (!validateArguments(arguments, messageCollector)) {
            return ExitCode.COMPILATION_ERROR
        }

        // Configure BrightScript target
        val targetConfig = createTargetConfig(arguments, messageCollector)

        // Determine output directory
        val outputDir = arguments.outputDir?.let { File(it) }
        if (outputDir != null && !outputDir.exists()) {
            outputDir.mkdirs()
        }

        messageCollector.report(
            CompilerMessageSeverity.INFO,
            "Kotlin/BrightScript compiler targeting Roku OS ${targetConfig.minRokuOS.versionString}"
        )

        // Create compilation environment
        // Use JS_CONFIG_FILES as a starting point since BRS is similar (scripting language)
        val environment = KotlinCoreEnvironment.createForProduction(
            rootDisposable,
            configuration,
            EnvironmentConfigFiles.JS_CONFIG_FILES
        )

        // Get source files
        val sourceFiles = environment.getSourceFiles()
        if (sourceFiles.isEmpty()) {
            messageCollector.report(CompilerMessageSeverity.ERROR, "No source files provided")
            return ExitCode.COMPILATION_ERROR
        }

        messageCollector.report(
            CompilerMessageSeverity.INFO,
            "Compiling ${sourceFiles.size} source file(s) to BrightScript"
        )

        // Report the files being compiled
        for (file in sourceFiles) {
            messageCollector.report(
                CompilerMessageSeverity.LOGGING,
                "Processing: ${file.virtualFilePath}"
            )
        }

        // Compile to BrightScript
        val exitCode = compileSourceFiles(
            environment.project,
            sourceFiles,
            configuration,
            targetConfig,
            outputDir,
            messageCollector
        )

        return exitCode
    }

    /**
     * Compile Kotlin source files to BrightScript.
     */
    private fun compileSourceFiles(
        project: com.intellij.openapi.project.Project,
        sourceFiles: List<KtFile>,
        configuration: CompilerConfiguration,
        targetConfig: BrsTargetConfig,
        outputDir: File?,
        messageCollector: MessageCollector
    ): ExitCode {
        // Report configuration
        messageCollector.report(
            CompilerMessageSeverity.INFO,
            "Target configuration: minRokuOS=${targetConfig.minRokuOS.versionString}, " +
                    "strictMode=${targetConfig.strictMode}, debugMode=${targetConfig.debugMode}"
        )

        if (outputDir != null) {
            messageCollector.report(
                CompilerMessageSeverity.INFO,
                "Output directory: ${outputDir.absolutePath}"
            )
        }

        // Step 1: Run FIR frontend analysis
        messageCollector.report(CompilerMessageSeverity.INFO, "Running FIR analysis...")

        val firResult = try {
            BrsFirFrontendFacade.analyze(project, sourceFiles, configuration, messageCollector)
        } catch (e: NotImplementedError) {
            messageCollector.report(
                CompilerMessageSeverity.WARNING,
                "FIR session creation for BrightScript is not yet implemented. ${e.message}"
            )
            messageCollector.report(
                CompilerMessageSeverity.INFO,
                "BrightScript compiler backend is under development. " +
                        "Source files were parsed but full compilation is not yet available."
            )
            return ExitCode.OK
        } catch (e: Exception) {
            messageCollector.report(
                CompilerMessageSeverity.ERROR,
                "FIR analysis failed: ${e.message}"
            )
            return ExitCode.COMPILATION_ERROR
        }

        if (firResult.hasErrors) {
            messageCollector.report(
                CompilerMessageSeverity.ERROR,
                "FIR analysis found errors"
            )
            return ExitCode.COMPILATION_ERROR
        }

        // Step 2: Convert FIR to IR
        messageCollector.report(CompilerMessageSeverity.INFO, "Converting FIR to IR...")

        val irResult = BrsFirFrontendFacade.convertToIr(firResult, configuration, messageCollector)
        if (irResult == null) {
            messageCollector.report(
                CompilerMessageSeverity.ERROR,
                "FIR to IR conversion failed"
            )
            return ExitCode.COMPILATION_ERROR
        }

        // Step 3: Run BrightScript backend compilation
        messageCollector.report(CompilerMessageSeverity.INFO, "Transforming IR to BrightScript...")

        val moduleDescriptor = irResult.irModuleFragment.descriptor
        val brsCompiler = BrsCompiler(
            module = moduleDescriptor,
            irBuiltIns = irResult.irBuiltIns,
            symbolTable = irResult.symbolTable,
            configuration = configuration,
            targetConfig = targetConfig
        )

        val compilationResult = brsCompiler.compile(irResult.irModuleFragment)

        // Report any compilation errors
        for (error in compilationResult.errors) {
            messageCollector.report(CompilerMessageSeverity.ERROR, error)
        }

        if (compilationResult.errors.isNotEmpty()) {
            return ExitCode.COMPILATION_ERROR
        }

        // Step 4: Write output files
        if (outputDir != null) {
            messageCollector.report(
                CompilerMessageSeverity.INFO,
                "Writing ${compilationResult.outputs.size} BrightScript file(s)..."
            )
            BrsCompiler.writeOutput(compilationResult, outputDir)
            messageCollector.report(
                CompilerMessageSeverity.INFO,
                "BrightScript compilation completed successfully"
            )
        } else {
            messageCollector.report(
                CompilerMessageSeverity.WARNING,
                "No output directory specified, skipping file output"
            )
        }

        return ExitCode.OK
    }

    override fun MutableList<String>.addPlatformOptions(arguments: K2BrsCompilerArguments) {
        // BrightScript has no scripting support
    }

    private fun validateArguments(
        arguments: K2BrsCompilerArguments,
        messageCollector: MessageCollector
    ): Boolean {
        var valid = true

        // Validate min-roku-os
        arguments.minRokuOS?.let { version ->
            if (version !in K2BrsArgumentConstants.SUPPORTED_ROKU_OS_VERSIONS) {
                messageCollector.report(
                    CompilerMessageSeverity.ERROR,
                    "Unsupported Roku OS version: $version. " +
                            "Supported versions: ${K2BrsArgumentConstants.SUPPORTED_ROKU_OS_VERSIONS.joinToString()}"
                )
                valid = false
            }
        }

        // Validate output directory
        arguments.outputDir?.let { dir ->
            val file = File(dir)
            if (file.exists() && !file.isDirectory) {
                messageCollector.report(
                    CompilerMessageSeverity.ERROR,
                    "Output path is not a directory: $dir"
                )
                valid = false
            }
        }

        return valid
    }

    private fun createTargetConfig(
        arguments: K2BrsCompilerArguments,
        messageCollector: MessageCollector
    ): BrsTargetConfig {
        val minRokuOS = when (arguments.minRokuOS) {
            K2BrsArgumentConstants.MIN_ROKU_OS_9_0 -> RokuOSVersion.ROKU_OS_9_0
            K2BrsArgumentConstants.MIN_ROKU_OS_9_4 -> RokuOSVersion.ROKU_OS_9_4
            K2BrsArgumentConstants.MIN_ROKU_OS_10_0 -> RokuOSVersion.ROKU_OS_10_0
            K2BrsArgumentConstants.MIN_ROKU_OS_11_0 -> RokuOSVersion.ROKU_OS_11_0
            K2BrsArgumentConstants.MIN_ROKU_OS_12_0 -> RokuOSVersion.ROKU_OS_12_0
            K2BrsArgumentConstants.MIN_ROKU_OS_13_0 -> RokuOSVersion.ROKU_OS_13_0
            else -> {
                messageCollector.report(
                    CompilerMessageSeverity.INFO,
                    "Using default target: Roku OS ${K2BrsArgumentConstants.DEFAULT_MIN_ROKU_OS}"
                )
                RokuOSVersion.ROKU_OS_9_4
            }
        }

        return BrsTargetConfig(
            minRokuOS = minRokuOS,
            debugMode = arguments.debugMode,
            generateXml = arguments.generateXml,
            strictMode = arguments.strictMode
        )
    }

    override fun setupPlatformSpecificArgumentsAndServices(
        configuration: CompilerConfiguration,
        arguments: K2BrsCompilerArguments,
        services: Services
    ) {
        // Configure BrightScript-specific services
        arguments.outputDir?.let { configuration.put(BrsConfigurationKeys.OUTPUT_DIR, it) }
        configuration.put(BrsConfigurationKeys.MODULE_NAME, arguments.moduleName ?: "main")
        configuration.put(BrsConfigurationKeys.MIN_ROKU_OS, arguments.minRokuOS ?: K2BrsArgumentConstants.DEFAULT_MIN_ROKU_OS)
        configuration.put(BrsConfigurationKeys.DEBUG_MODE, arguments.debugMode)
        configuration.put(BrsConfigurationKeys.GENERATE_XML, arguments.generateXml)
        configuration.put(BrsConfigurationKeys.MINIFY, arguments.minify)
        configuration.put(BrsConfigurationKeys.STRICT_MODE, arguments.strictMode)
    }

    override fun executableScriptFileName(): String = "kotlinc-brs"

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            doMain(K2BrsCompiler(), args)
        }
    }
}

/**
 * Configuration keys for BrightScript compilation.
 */
object BrsConfigurationKeys {
    val OUTPUT_DIR = org.jetbrains.kotlin.config.CompilerConfigurationKey.create<String>("output directory")
    val MODULE_NAME = org.jetbrains.kotlin.config.CompilerConfigurationKey.create<String>("module name")
    val MIN_ROKU_OS = org.jetbrains.kotlin.config.CompilerConfigurationKey.create<String>("min Roku OS version")
    val DEBUG_MODE = org.jetbrains.kotlin.config.CompilerConfigurationKey.create<Boolean>("debug mode")
    val GENERATE_XML = org.jetbrains.kotlin.config.CompilerConfigurationKey.create<Boolean>("generate XML")
    val MINIFY = org.jetbrains.kotlin.config.CompilerConfigurationKey.create<Boolean>("minify output")
    val STRICT_MODE = org.jetbrains.kotlin.config.CompilerConfigurationKey.create<Boolean>("strict mode")
}
