/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.brs

import com.intellij.openapi.Disposable
import org.jetbrains.kotlin.brs.BrsTargetConfig
import org.jetbrains.kotlin.brs.RokuOSVersion
import org.jetbrains.kotlin.cli.common.CLICompiler
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
import org.jetbrains.kotlin.library.loader.KlibLoader
import org.jetbrains.kotlin.library.loader.KlibPlatformChecker
import org.jetbrains.kotlin.platform.brs.brsTargetPlatform
import org.jetbrains.kotlin.library.KotlinLibrary
import org.jetbrains.kotlin.library.uniqueName
import org.jetbrains.kotlin.metadata.deserialization.BinaryVersion
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.utils.KotlinPaths
import java.io.File
import org.jetbrains.kotlin.backend.common.serialization.IrSerializationSettings
import org.jetbrains.kotlin.backend.common.serialization.serializeModuleIntoKlib
import org.jetbrains.kotlin.diagnostics.DiagnosticReporterFactory
import org.jetbrains.kotlin.fir.pipeline.Fir2KlibMetadataSerializer
import org.jetbrains.kotlin.ir.backend.brs.lower.serialization.ir.BrsIrModuleSerializer
import org.jetbrains.kotlin.ir.backend.brs.lower.serialization.ir.BrsIrFileEmptyMetadataFactory
import org.jetbrains.kotlin.library.*
import org.jetbrains.kotlin.library.impl.BuiltInsPlatform
import org.jetbrains.kotlin.library.impl.buildKotlinLibrary
import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.util.klibMetadataVersionOrDefault
import java.util.Properties
import java.util.zip.ZipFile
import org.jetbrains.kotlin.ir.backend.brs.BrsIrBackendContext
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.path
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import org.jetbrains.kotlin.descriptors.ClassKind

/**
 * CLI compiler for Kotlin to BrightScript.
 *
 * This compiler takes Kotlin source files and produces BrightScript (.brs) output
 * suitable for Roku application development.
 */
class K2BrsCompiler : CLICompiler<K2BrsCompilerArguments>() {
    override val platform: TargetPlatform
        get() = brsTargetPlatform()

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

        // Load function manifests and file dependencies from klib dependencies
        val dependencyFunctionManifest = loadDependencyFunctionManifests(libraries, messageCollector)
        val dependencyFileDeps = loadDependencyFileDeps(libraries, messageCollector)

        val moduleDescriptor = irResult.irModuleFragment.descriptor
        val brsCompiler = BrsCompiler(
            module = moduleDescriptor,
            irBuiltIns = irResult.irBuiltIns,
            symbolTable = irResult.symbolTable,
            configuration = configuration,
            targetConfig = targetConfig,
            isStdlibCompilation = isStdlibCompilation,
            dependencyFunctionManifest = dependencyFunctionManifest,
            dependencyFileDeps = dependencyFileDeps
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

        // Use "stdlib" as the module name when compiling stdlib for proper IDE resolution
        val isStdlibCompilation = configuration.languageVersionSettings.getFlag(AnalysisFlags.stdlibCompilation)
        val effectiveModuleName = if (isStdlibCompilation) "stdlib" else moduleName

        messageCollector.report(
            CompilerMessageSeverity.INFO,
            "Serializing module '$effectiveModuleName' to klib: $outputPath"
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
            moduleName = effectiveModuleName,
            irModuleFragment = irResult.irModuleFragment,
            configuration = configuration,
            diagnosticReporter = diagnosticReporter,
            cleanFiles = emptyList(),
            dependencies = libraries,
            createModuleSerializer = { irDiagnosticReporter ->
                BrsIrModuleSerializer(
                    settings = IrSerializationSettings(configuration),
                    diagnosticReporter = irDiagnosticReporter,
                    irBuiltIns = irResult.irBuiltIns,
                    brsIrFileMetadataFactory = BrsIrFileEmptyMetadataFactory
                )
            },
            metadataSerializer = metadataSerializer
        )

        // Build and write klib file
        val versions = KotlinLibraryVersioning(
            abiVersion = KotlinAbiVersion.CURRENT,
            compilerVersion = KotlinCompilerVersion.VERSION,
            metadataVersion = configuration.klibMetadataVersionOrDefault()
        )

        // Generate function manifest for this module
        val functionManifest = generateFunctionManifest(
            irResult.irModuleFragment,
            irResult.irBuiltIns,
            irResult.symbolTable,
            configuration
        )

        // Create context for file dependency collection
        val context = BrsIrBackendContext(
            module = irResult.irModuleFragment.descriptor,
            irBuiltIns = irResult.irBuiltIns,
            symbolTable = irResult.symbolTable,
            configuration = configuration,
            isStdlibCompilation = configuration.languageVersionSettings.getFlag(AnalysisFlags.stdlibCompilation)
        )

        // Generate file dependency graph
        val fileDependencies = collectFileDependencies(
            irResult.irModuleFragment,
            functionManifest,
            context
        )

        // Store both manifest and file dependencies in klib properties
        val manifestProperties = Properties().apply {
            if (functionManifest.isNotEmpty()) {
                setProperty(KLIB_PROPERTY_BRS_FUNCTION_MANIFEST, serializeFunctionManifest(functionManifest))
            }
            if (fileDependencies.isNotEmpty()) {
                setProperty(KLIB_PROPERTY_BRS_FILE_DEPENDENCIES, serializeFileDependencies(fileDependencies))
            }
        }

        messageCollector.report(
            CompilerMessageSeverity.LOGGING,
            "Generated function manifest with ${functionManifest.size} entries"
        )
        messageCollector.report(
            CompilerMessageSeverity.LOGGING,
            "Generated file dependencies for ${fileDependencies.size} files"
        )

        buildKotlinLibrary(
            linkDependencies = serializerOutput.neededLibraries,
            metadata = serializerOutput.serializedMetadata
                ?: error("Expected serialized metadata"),
            ir = serializerOutput.serializedIr
                ?: error("Expected serialized IR"),
            versions = versions,
            output = outputPath,
            moduleName = effectiveModuleName,
            nopack = false,
            manifestProperties = manifestProperties,
            builtInsPlatform = BuiltInsPlatform.BRS
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
            // Use KlibLoader like the JS backend does
            val result = KlibLoader {
                libraryPaths(libraryPaths)
                platformChecker(KlibPlatformChecker.Brs)
                maxPermittedAbiVersion(KotlinAbiVersion.CURRENT)
            }.load()

            val libraries = result.librariesStdlibFirst

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

    // ==================== Function Manifest ====================

    /**
     * Generate function-to-file manifest for the IR module.
     * Maps BrightScript function names to their output .brs filenames.
     * This manifest is stored in the klib and used by consuming modules
     * to resolve dependencies accurately.
     */
    private fun generateFunctionManifest(
        irModule: IrModuleFragment,
        irBuiltIns: org.jetbrains.kotlin.ir.IrBuiltIns,
        symbolTable: org.jetbrains.kotlin.ir.util.SymbolTable,
        configuration: CompilerConfiguration
    ): Map<String, String> {
        val manifest = mutableMapOf<String, String>()

        // Create a minimal context for name generation
        val context = BrsIrBackendContext(
            module = irModule.descriptor,
            irBuiltIns = irBuiltIns,
            symbolTable = symbolTable,
            configuration = configuration,
            isStdlibCompilation = configuration.languageVersionSettings.getFlag(AnalysisFlags.stdlibCompilation)
        )

        // Determine the first file name (runtime helpers are added to the first file)
        // Note: BRS compiler appends "Kt" suffix to all output files
        val firstFileName = irModule.files.firstOrNull()?.let {
            File(it.path).nameWithoutExtension + "Kt.brs"
        }

        // Add runtime helper functions to manifest (they're in the first file)
        // These are internal helpers used by stdlib classes like ArrayList, Any, etc.
        if (firstFileName != null && configuration.languageVersionSettings.getFlag(AnalysisFlags.stdlibCompilation)) {
            RUNTIME_HELPER_FUNCTIONS.forEach { helperName ->
                manifest[helperName] = firstFileName
            }
        }

        for (file in irModule.files) {
            // Note: BRS compiler appends "Kt" suffix to all output files
            val outputFileName = File(file.path).nameWithoutExtension + "Kt.brs"
            collectDeclarationNames(file.declarations, outputFileName, context, manifest)
        }

        return manifest
    }

    /**
     * Names of runtime helper functions added to the first stdlib file.
     * These must match the function names generated by BrsCompiler.addRuntimeHelpers()
     */
    private val RUNTIME_HELPER_FUNCTIONS = listOf(
        "__kotlin_ushr",
        "__kotlin_stringCompare",
        "__kotlin_intCompare",
        "__kotlin_nextObjectId",
        "__kotlin_identityEquals",
        "__kotlin_isInstanceOf"
    )

    /**
     * Recursively collect BrightScript names for all declarations in a file.
     */
    private fun collectDeclarationNames(
        declarations: List<org.jetbrains.kotlin.ir.declarations.IrDeclaration>,
        outputFileName: String,
        context: BrsIrBackendContext,
        manifest: MutableMap<String, String>
    ) {
        for (declaration in declarations) {
            when (declaration) {
                is IrFunction -> {
                    val brsName = context.getBrsName(declaration)
                    manifest[brsName] = outputFileName
                }
                is IrClass -> {
                    // Record the class itself
                    val className = context.getBrsName(declaration)
                    manifest[className] = outputFileName

                    // Record all methods in the class
                    for (member in declaration.declarations) {
                        when (member) {
                            is IrFunction -> {
                                val methodName = context.getBrsName(member)
                                manifest[methodName] = outputFileName
                            }
                            is IrProperty -> {
                                // Record getter and setter if present
                                member.getter?.let { getter ->
                                    val getterName = context.getBrsName(getter)
                                    manifest[getterName] = outputFileName
                                }
                                member.setter?.let { setter ->
                                    val setterName = context.getBrsName(setter)
                                    manifest[setterName] = outputFileName
                                }
                            }
                        }
                    }
                }
                is IrProperty -> {
                    // Top-level property - record getter and setter
                    declaration.getter?.let { getter ->
                        val getterName = context.getBrsName(getter)
                        manifest[getterName] = outputFileName
                    }
                    declaration.setter?.let { setter ->
                        val setterName = context.getBrsName(setter)
                        manifest[setterName] = outputFileName
                    }
                }
            }
        }
    }

    /**
     * Collect file-to-file dependencies by analyzing IR call sites.
     * Returns a map of outputFileName → set of dependent fileNames.
     *
     * This walks the IR to find function calls and maps them to their target files
     * using the function manifest. The resulting dependency graph is stored in the
     * klib so that transitive dependencies can be resolved during user code compilation.
     */
    private fun collectFileDependencies(
        irModule: IrModuleFragment,
        functionManifest: Map<String, String>,
        context: BrsIrBackendContext
    ): Map<String, Set<String>> {
        val fileDeps = mutableMapOf<String, MutableSet<String>>()

        // Find the file containing runtime helpers (where __kotlin_nextObjectId is defined)
        // This is the first file in the stdlib (ArraysBrsKt.brs by convention)
        val runtimeHelpersFile = functionManifest["__kotlin_nextObjectId"]

        for (file in irModule.files) {
            // Note: BRS compiler appends "Kt" suffix to all output files
            val thisFileName = File(file.path).nameWithoutExtension + "Kt.brs"
            val deps = mutableSetOf<String>()

            // Track if this file contains class definitions that will need __kotlin_nextObjectId
            var hasClassDefinitions = false

            // Walk all declarations to find function calls
            file.acceptVoid(object : IrVisitorVoid() {
                override fun visitElement(element: IrElement) {
                    element.acceptChildrenVoid(this)
                }

                override fun visitClass(declaration: IrClass) {
                    // Class definitions generate code that calls __kotlin_nextObjectId()
                    // for identity tracking. Skip interfaces and annotation classes.
                    val kind = declaration.kind
                    if (kind != ClassKind.INTERFACE && kind != ClassKind.ANNOTATION_CLASS) {
                        hasClassDefinitions = true
                    }
                    declaration.acceptChildrenVoid(this)
                }

                override fun visitCall(expression: IrCall) {
                    val calledFunction = expression.symbol.owner
                    val calledBrsName = context.getBrsName(calledFunction)
                    val targetFile = functionManifest[calledBrsName]
                    if (targetFile != null && targetFile != thisFileName) {
                        deps.add(targetFile)
                    }

                    // Special handling for intrinsics that generate calls to runtime functions
                    // brsIntrinsicToString calls toString_AnyN_k_ which is in coreRuntimeKt.brs
                    val functionName = calledFunction.name.asString()
                    if (functionName == "brsIntrinsicToString") {
                        deps.add("coreRuntimeKt.brs")
                    }

                    expression.acceptChildrenVoid(this)
                }
            })

            // If this file has class definitions, it needs the runtime helpers file
            // The BRS IR transformer generates __kotlin_nextObjectId() calls for class constructors
            if (hasClassDefinitions && runtimeHelpersFile != null && runtimeHelpersFile != thisFileName) {
                deps.add(runtimeHelpersFile)
            }

            if (deps.isNotEmpty()) {
                fileDeps[thisFileName] = deps
            }
        }

        return fileDeps
    }

    /**
     * Serialize the function manifest to a JSON string for storage in klib properties.
     */
    private fun serializeFunctionManifest(manifest: Map<String, String>): String {
        val sb = StringBuilder()
        sb.append("{")
        val entries = manifest.entries.sortedBy { it.key }
        entries.forEachIndexed { index, (functionName, fileName) ->
            val escapedName = functionName.replace("\\", "\\\\").replace("\"", "\\\"")
            val escapedFile = fileName.replace("\\", "\\\\").replace("\"", "\\\"")
            sb.append("\"$escapedName\":\"$escapedFile\"")
            if (index < entries.size - 1) sb.append(",")
        }
        sb.append("}")
        return sb.toString()
    }

    /**
     * Serialize file dependencies to a compact JSON string for storage in klib properties.
     * Format: {"file.brs":["dep1.brs","dep2.brs"],...}
     */
    private fun serializeFileDependencies(deps: Map<String, Set<String>>): String {
        val sb = StringBuilder()
        sb.append("{")
        val entries = deps.entries.sortedBy { it.key }
        entries.forEachIndexed { index, (fileName, fileDeps) ->
            val escapedFile = fileName.replace("\\", "\\\\").replace("\"", "\\\"")
            val depsArray = fileDeps.sorted().joinToString(",") { dep ->
                val escapedDep = dep.replace("\\", "\\\\").replace("\"", "\\\"")
                "\"$escapedDep\""
            }
            sb.append("\"$escapedFile\":[$depsArray]")
            if (index < entries.size - 1) sb.append(",")
        }
        sb.append("}")
        return sb.toString()
    }

    /**
     * Load function-to-file manifests from all klib dependencies.
     * Each klib may contain a brs_function_manifest property that maps
     * BrightScript function names to their output .brs files.
     */
    private fun loadDependencyFunctionManifests(
        libraries: List<KotlinLibrary>,
        messageCollector: MessageCollector
    ): Map<String, String> {
        val manifest = mutableMapOf<String, String>()

        for (library in libraries) {
            try {
                // Read the manifest property from the klib's manifest file
                val manifestJson = library.manifestProperties.getProperty(KLIB_PROPERTY_BRS_FUNCTION_MANIFEST)
                if (manifestJson != null) {
                    parseAndMergeFunctionManifest(manifestJson, manifest)
                    messageCollector.report(
                        CompilerMessageSeverity.LOGGING,
                        "Loaded function manifest from ${library.uniqueName}"
                    )
                }
            } catch (e: Exception) {
                messageCollector.report(
                    CompilerMessageSeverity.WARNING,
                    "Failed to load function manifest from ${library.uniqueName}: ${e.message}"
                )
            }
        }

        messageCollector.report(
            CompilerMessageSeverity.LOGGING,
            "Total dependency function manifest entries: ${manifest.size}"
        )

        return manifest
    }

    /**
     * Parse JSON function manifest and merge into the target map.
     * JSON format: {"name":"file.brs","name2":"file2.brs",...}
     */
    private fun parseAndMergeFunctionManifest(json: String, target: MutableMap<String, String>) {
        // Simple JSON parsing without external dependencies
        // Format: {"key1":"value1","key2":"value2",...}
        val entryPattern = Regex(""""([^"]+)"\s*:\s*"([^"]+)"""")
        for (match in entryPattern.findAll(json)) {
            val functionName = match.groupValues[1]
            val fileName = match.groupValues[2]
            target[functionName] = fileName
        }
    }

    /**
     * Load file-to-file dependency graphs from all klib dependencies.
     * Each klib may contain a brs_file_dependencies property that maps
     * .brs files to the other .brs files they depend on.
     */
    private fun loadDependencyFileDeps(
        libraries: List<KotlinLibrary>,
        messageCollector: MessageCollector
    ): Map<String, Set<String>> {
        val fileDeps = mutableMapOf<String, MutableSet<String>>()

        for (library in libraries) {
            try {
                val depsJson = library.manifestProperties.getProperty(KLIB_PROPERTY_BRS_FILE_DEPENDENCIES)
                if (depsJson != null) {
                    parseAndMergeFileDeps(depsJson, fileDeps)
                    messageCollector.report(
                        CompilerMessageSeverity.LOGGING,
                        "Loaded file dependencies from ${library.uniqueName}"
                    )
                }
            } catch (e: Exception) {
                messageCollector.report(
                    CompilerMessageSeverity.WARNING,
                    "Failed to load file dependencies from ${library.uniqueName}: ${e.message}"
                )
            }
        }

        messageCollector.report(
            CompilerMessageSeverity.LOGGING,
            "Total file dependency entries: ${fileDeps.size}"
        )

        return fileDeps
    }

    /**
     * Parse JSON file dependencies and merge into the target map.
     * JSON format: {"file1.brs":["dep1.brs","dep2.brs"],...}
     */
    private fun parseAndMergeFileDeps(json: String, target: MutableMap<String, MutableSet<String>>) {
        // Format: {"file.brs":["dep1.brs","dep2.brs"],...}
        val entryPattern = Regex(""""([^"]+)"\s*:\s*\[([^\]]*)\]""")
        for (match in entryPattern.findAll(json)) {
            val fileName = match.groupValues[1]
            val depsArray = match.groupValues[2]

            // Parse the array of dependencies
            val deps = Regex(""""([^"]+)"""").findAll(depsArray)
                .map { it.groupValues[1] }
                .toMutableSet()

            // Merge with existing deps for this file
            target.getOrPut(fileName) { mutableSetOf() }.addAll(deps)
        }
    }

    companion object {
        /** Klib manifest property key for BrightScript function-to-file manifest */
        const val KLIB_PROPERTY_BRS_FUNCTION_MANIFEST = "brs_function_manifest"

        /** Klib manifest property key for BrightScript file-to-file dependency graph */
        const val KLIB_PROPERTY_BRS_FILE_DEPENDENCIES = "brs_file_dependencies"

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
