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
import org.jetbrains.kotlin.cli.common.config.addKotlinSourceRoot
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.config.AnalysisFlags
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.KotlinCompilerVersion
import org.jetbrains.kotlin.config.Services
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.ir.backend.brs.BrsCompiler
import org.jetbrains.kotlin.backend.common.CommonKLibResolver
import org.jetbrains.kotlin.cli.common.messages.getLogger
import org.jetbrains.kotlin.library.KotlinLibrary
import org.jetbrains.kotlin.library.uniqueName
import org.jetbrains.kotlin.metadata.deserialization.BinaryVersion
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.utils.KotlinPaths
import java.io.File
import org.jetbrains.kotlin.backend.common.serialization.CompatibilityMode
import org.jetbrains.kotlin.backend.common.serialization.IrSerializationSettings
import org.jetbrains.kotlin.backend.common.serialization.serializeModuleIntoKlib
import org.jetbrains.kotlin.diagnostics.DiagnosticReporterFactory
import org.jetbrains.kotlin.fir.pipeline.Fir2KlibMetadataSerializer
import org.jetbrains.kotlin.ir.backend.brs.lower.serialization.ir.BrsIrModuleSerializer
import org.jetbrains.kotlin.ir.backend.brs.lower.serialization.ir.BrsIrFileEmptyMetadataFactory
import org.jetbrains.kotlin.library.*
import org.jetbrains.kotlin.library.impl.BuiltInsPlatform
import org.jetbrains.kotlin.library.impl.buildKotlinLibrary
import org.jetbrains.kotlin.library.metadata.KlibMetadataVersion
import java.util.Properties

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
        val environment = KotlinCoreEnvironment.createForProduction(
            rootDisposable,
            configuration,
            EnvironmentConfigFiles.BRS_CONFIG_FILES
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

        // Load libraries
        val libraries = loadLibraries(arguments.libraries, configuration, messageCollector)
        if (libraries.isNotEmpty()) {
            messageCollector.report(
                CompilerMessageSeverity.INFO,
                "Loaded ${libraries.size} library/libraries"
            )
        }

        // Compile to BrightScript
        val exitCode = compileSourceFiles(
            arguments,
            environment.project,
            sourceFiles,
            configuration,
            targetConfig,
            outputDir,
            messageCollector,
            libraries
        )

        return exitCode
    }

    /**
     * Compile Kotlin source files to BrightScript.
     */
    private fun compileSourceFiles(
        arguments: K2BrsCompilerArguments,
        project: com.intellij.openapi.project.Project,
        sourceFiles: List<KtFile>,
        configuration: CompilerConfiguration,
        targetConfig: BrsTargetConfig,
        outputDir: File?,
        messageCollector: MessageCollector,
        libraries: List<KotlinLibrary>
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
            BrsFirFrontendFacade.analyze(project, sourceFiles, configuration, messageCollector, libraries)
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

        // Branch based on produce mode
        val produceValue = arguments.produce ?: K2BrsArgumentConstants.DEFAULT_PRODUCE

        when (produceValue) {
            K2BrsArgumentConstants.PRODUCE_LIBRARY -> {
                // Resolve and validate output path
                val klibPath = arguments.output!! // Already validated
                val klibFile = File(klibPath).let { file ->
                    if (file.extension != "klib") {
                        File(file.parent, "${file.nameWithoutExtension}.klib")
                    } else file
                }
                klibFile.parentFile?.mkdirs()

                // Serialize to klib
                return serializeToKlib(
                    irResult = irResult,
                    firAnalysisResult = firResult,
                    configuration = configuration,
                    outputPath = klibFile.absolutePath,
                    messageCollector = messageCollector,
                    libraries = libraries
                )
            }

            K2BrsArgumentConstants.PRODUCE_EXECUTABLE -> {
                // Continue with existing BRS backend compilation
            }
        }

        // Step 3: Run BrightScript backend compilation
        messageCollector.report(CompilerMessageSeverity.INFO, "Transforming IR to BrightScript...")

        // Check if we're compiling the stdlib itself
        val isStdlibCompilation = configuration.languageVersionSettings.getFlag(AnalysisFlags.stdlibCompilation)
        if (isStdlibCompilation) {
            messageCollector.report(
                CompilerMessageSeverity.INFO,
                "Stdlib compilation mode enabled - missing stdlib symbols will be handled gracefully"
            )
        }

        val moduleDescriptor = irResult.irModuleFragment.descriptor
        val brsCompiler = BrsCompiler(
            module = moduleDescriptor,
            irBuiltIns = irResult.irBuiltIns,
            symbolTable = irResult.symbolTable,
            configuration = configuration,
            targetConfig = targetConfig,
            isStdlibCompilation = isStdlibCompilation
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

    /**
     * Serialize the IR module into a klib file.
     */
    private fun serializeToKlib(
        irResult: BrsFir2IrResult,
        firAnalysisResult: BrsFirAnalysisResult,
        configuration: CompilerConfiguration,
        outputPath: String,
        messageCollector: MessageCollector,
        libraries: List<KotlinLibrary>
    ): ExitCode {
        val moduleName = configuration.get(CommonConfigurationKeys.MODULE_NAME) ?: "main"

        messageCollector.report(
            CompilerMessageSeverity.INFO,
            "Serializing module '$moduleName' to klib: $outputPath"
        )

        val diagnosticReporter = DiagnosticReporterFactory.createReporter(messageCollector)

        // Create metadata serializer
        val metadataSerializer = Fir2KlibMetadataSerializer(
            compilerConfiguration = configuration,
            firOutputs = firAnalysisResult.outputs,
            fir2IrActualizedResult = irResult.fir2IrActualizedResult,
            exportKDoc = false,
            produceHeaderKlib = false
        )

        // Serialize IR and metadata
        val serializerOutput = serializeModuleIntoKlib(
            moduleName = moduleName,
            irModuleFragment = irResult.irModuleFragment,
            irBuiltins = irResult.irBuiltIns,
            configuration = configuration,
            diagnosticReporter = diagnosticReporter,
            compatibilityMode = CompatibilityMode.CURRENT,
            cleanFiles = emptyList(),
            dependencies = libraries,
            createModuleSerializer = { irDiagnosticReporter, irBuiltins, compatibilityMode,
                                       normalizeAbsolutePaths, sourceBaseDirs,
                                       languageVersionSettings, shouldCheckSignaturesOnUniqueness ->
                BrsIrModuleSerializer(
                    settings = IrSerializationSettings(
                        languageVersionSettings = languageVersionSettings,
                        compatibilityMode = compatibilityMode,
                        normalizeAbsolutePaths = normalizeAbsolutePaths,
                        sourceBaseDirs = sourceBaseDirs,
                        shouldCheckSignaturesOnUniqueness = shouldCheckSignaturesOnUniqueness
                    ),
                    diagnosticReporter = irDiagnosticReporter,
                    irBuiltIns = irBuiltins,
                    brsIrFileMetadataFactory = BrsIrFileEmptyMetadataFactory
                )
            },
            metadataSerializer = metadataSerializer
        )

        // Build and write klib file
        val versions = KotlinLibraryVersioning(
            abiVersion = KotlinAbiVersion.CURRENT,
            compilerVersion = KotlinCompilerVersion.VERSION,
            metadataVersion = KlibMetadataVersion.INSTANCE.toString()
        )

        buildKotlinLibrary(
            linkDependencies = serializerOutput.neededLibraries,
            metadata = serializerOutput.serializedMetadata
                ?: error("Expected serialized metadata"),
            ir = serializerOutput.serializedIr
                ?: error("Expected serialized IR"),
            versions = versions,
            output = outputPath,
            moduleName = moduleName,
            nopack = false,
            perFile = false,
            manifestProperties = Properties(),
            builtInsPlatform = BuiltInsPlatform.COMMON
        )

        messageCollector.report(
            CompilerMessageSeverity.INFO,
            "Klib written successfully: $outputPath"
        )

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

        // Validate produce argument
        arguments.produce?.let { produce ->
            if (produce !in K2BrsArgumentConstants.SUPPORTED_PRODUCE_VALUES) {
                messageCollector.report(
                    CompilerMessageSeverity.ERROR,
                    "Unsupported produce value: $produce. " +
                            "Supported values: ${K2BrsArgumentConstants.SUPPORTED_PRODUCE_VALUES.joinToString()}"
                )
                valid = false
            }
        }

        // Validate library mode requires -output
        val produceValue = arguments.produce ?: K2BrsArgumentConstants.DEFAULT_PRODUCE
        if (produceValue == K2BrsArgumentConstants.PRODUCE_LIBRARY && arguments.output == null) {
            messageCollector.report(
                CompilerMessageSeverity.ERROR,
                "Library mode requires -output argument to specify .klib file path"
            )
            valid = false
        }

        // Warn about conflicting arguments
        if (produceValue == K2BrsArgumentConstants.PRODUCE_LIBRARY && arguments.outputDir != null) {
            messageCollector.report(
                CompilerMessageSeverity.WARNING,
                "Both -output and -output-dir specified. In library mode, -output-dir is ignored."
            )
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
        val moduleName = arguments.moduleName ?: "main"
        configuration.put(BrsConfigurationKeys.MODULE_NAME, moduleName)
        configuration.put(CommonConfigurationKeys.MODULE_NAME, moduleName)
        configuration.put(BrsConfigurationKeys.MIN_ROKU_OS, arguments.minRokuOS ?: K2BrsArgumentConstants.DEFAULT_MIN_ROKU_OS)
        configuration.put(BrsConfigurationKeys.DEBUG_MODE, arguments.debugMode)
        configuration.put(BrsConfigurationKeys.GENERATE_XML, arguments.generateXml)
        configuration.put(BrsConfigurationKeys.MINIFY, arguments.minify)
        configuration.put(BrsConfigurationKeys.STRICT_MODE, arguments.strictMode)

        // Add source files from free arguments
        for (arg in arguments.freeArgs) {
            configuration.addKotlinSourceRoot(arg)
        }
    }

    override fun executableScriptFileName(): String = "kotlinc-brs"

    /**
     * Load klib libraries from the given paths.
     * Uses CommonKLibResolver like the JS backend does.
     */
    private fun loadLibraries(
        librariesArg: String?,
        configuration: CompilerConfiguration,
        messageCollector: MessageCollector
    ): List<KotlinLibrary> {
        if (librariesArg.isNullOrBlank()) {
            return emptyList()
        }

        val libraryPaths = librariesArg.split(File.pathSeparator)
            .filter { it.isNotBlank() }

        if (libraryPaths.isEmpty()) {
            return emptyList()
        }

        return try {
            // Use CommonKLibResolver like the JS backend does
            val resolved = CommonKLibResolver.resolve(
                libraries = libraryPaths,
                logger = configuration.getLogger(treatWarningsAsErrors = false),
                lenient = true  // Lenient for dependencies (e.g., 'kotlin' stdlib)
            )
            val libraries = resolved.getFullResolvedList().map { it.library }

            messageCollector.report(
                CompilerMessageSeverity.LOGGING,
                "Resolved ${libraries.size} library/libraries from ${libraryPaths.size} path(s)"
            )
            for (lib in libraries) {
                messageCollector.report(
                    CompilerMessageSeverity.LOGGING,
                    "  Library: ${lib.uniqueName} (${lib.libraryFile.absolutePath})"
                )
            }

            libraries
        } catch (e: Exception) {
            messageCollector.report(
                CompilerMessageSeverity.ERROR,
                "Failed to resolve libraries: ${e.message}"
            )
            e.printStackTrace()
            emptyList()
        }
    }

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
