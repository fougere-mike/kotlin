/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.brs

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.backend.common.serialization.mangle.KotlinExportChecker
import org.jetbrains.kotlin.backend.common.serialization.mangle.KotlinMangleComputer
import org.jetbrains.kotlin.backend.common.serialization.mangle.MangleMode
import org.jetbrains.kotlin.backend.common.serialization.mangle.ir.IrBasedKotlinManglerImpl
import org.jetbrains.kotlin.backend.common.serialization.mangle.ir.IrExportCheckerVisitor
import org.jetbrains.kotlin.backend.common.serialization.mangle.ir.IrMangleComputer
import org.jetbrains.kotlin.cli.common.fir.FirDiagnosticsCompilerResultsReporter
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.common.prepareBrsSessions
import org.jetbrains.kotlin.library.KotlinLibrary
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.diagnostics.DiagnosticReporterFactory
import org.jetbrains.kotlin.diagnostics.impl.BaseDiagnosticsCollector
import org.jetbrains.kotlin.fir.DependencyListForCliModule
import org.jetbrains.kotlin.fir.backend.Fir2IrConfiguration
import org.jetbrains.kotlin.fir.backend.Fir2IrExtensions
import org.jetbrains.kotlin.fir.backend.Fir2IrVisibilityConverter
import org.jetbrains.kotlin.fir.pipeline.Fir2IrActualizedResult
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.jetbrains.kotlin.fir.pipeline.FirResult
import org.jetbrains.kotlin.cli.brs.extensions.BrsFirExtensionRegistrar
import org.jetbrains.kotlin.fir.pipeline.ModuleCompilerAnalyzedOutput
import org.jetbrains.kotlin.fir.pipeline.buildResolveAndCheckFirFromKtFiles
import org.jetbrains.kotlin.fir.pipeline.convertToIrAndActualize
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.types.IrTypeSystemContextImpl
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtFile

/**
 * Result of FIR analysis for BrightScript compilation.
 */
data class BrsFirAnalysisResult(
    /**
     * The FIR analysis outputs for each module.
     */
    val outputs: List<ModuleCompilerAnalyzedOutput>,

    /**
     * The diagnostics collector with any errors/warnings.
     */
    val diagnosticsReporter: BaseDiagnosticsCollector,

    /**
     * Whether analysis completed successfully (no fatal errors).
     */
    val hasErrors: Boolean,

    /**
     * The FIR result, needed for metadata serialization.
     */
    val firResult: FirResult? = null
)

/**
 * Result of FIR to IR conversion for BrightScript compilation.
 */
data class BrsFir2IrResult(
    /**
     * The generated IR module fragment.
     */
    val irModuleFragment: IrModuleFragment,

    /**
     * IR built-ins for the target platform.
     */
    val irBuiltIns: org.jetbrains.kotlin.ir.IrBuiltIns,

    /**
     * Symbol table for IR declarations.
     */
    val symbolTable: org.jetbrains.kotlin.ir.util.SymbolTable,

    /**
     * The diagnostics collector with any errors/warnings.
     */
    val diagnosticsReporter: BaseDiagnosticsCollector,

    /**
     * The FIR to IR actualized result, needed for metadata serialization.
     */
    val fir2IrActualizedResult: Fir2IrActualizedResult? = null
)

/**
 * Frontend facade for BrightScript FIR compilation.
 *
 * This class handles the FIR frontend analysis and conversion to IR
 * for BrightScript compilation.
 */
object BrsFirFrontendFacade {

    /**
     * Run FIR analysis on the given source files.
     *
     * @param project The IntelliJ project
     * @param sourceFiles The Kotlin source files to analyze
     * @param configuration The compiler configuration
     * @param messageCollector The message collector for diagnostics
     * @return The FIR analysis result
     */
    fun analyze(
        project: Project,
        sourceFiles: List<KtFile>,
        configuration: CompilerConfiguration,
        messageCollector: MessageCollector,
        libraries: List<KotlinLibrary> = emptyList()
    ): BrsFirAnalysisResult {
        val diagnosticsReporter = DiagnosticReporterFactory.createReporter(messageCollector)

        val moduleName = configuration.get(CommonConfigurationKeys.MODULE_NAME) ?: "main"
        val escapedModuleName = Name.special("<$moduleName>")

        // Build dependency list with library dependencies
        val dependencyList = DependencyListForCliModule.build(escapedModuleName) {
            dependencies(libraries.map { it.libraryFile.absolutePath })
        }

        // Create FIR session using prepareBrsSessions
        // Include BRS-specific extension registrar along with any project-level registrars
        val extensionRegistrars = FirExtensionRegistrar.getInstances(project) + BrsFirExtensionRegistrar()

        try {
            // Use the new prepareBrsSessions function to create sessions
            val sessionsWithSources = prepareBrsSessions(
                files = sourceFiles,
                configuration = configuration,
                rootModuleName = escapedModuleName,
                resolvedLibraries = libraries,
                libraryList = dependencyList,
                extensionRegistrars = extensionRegistrars,
                isCommonSource = { false }, // No common sources for MVP
                fileBelongsToModule = { _, _ -> true }, // All files belong to the module
                icData = null,
            )

            if (sessionsWithSources.isEmpty()) {
                messageCollector.report(
                    CompilerMessageSeverity.ERROR,
                    "Failed to create FIR sessions for BrightScript compilation"
                )
                return BrsFirAnalysisResult(
                    outputs = emptyList(),
                    diagnosticsReporter = diagnosticsReporter,
                    hasErrors = true
                )
            }

            // Build, resolve, and check FIR for each session
            val outputs = mutableListOf<ModuleCompilerAnalyzedOutput>()
            for (sessionWithSources in sessionsWithSources) {
                val output = buildResolveAndCheckFirFromKtFiles(
                    sessionWithSources.session,
                    sessionWithSources.files,
                    diagnosticsReporter
                )
                outputs.add(output)
            }

            val hasErrors = diagnosticsReporter.hasErrors

            // Report all diagnostics to the message collector
            FirDiagnosticsCompilerResultsReporter.reportToMessageCollector(
                diagnosticsReporter,
                messageCollector,
                renderDiagnosticName = false
            )

            // Create FirResult for metadata serialization
            val firResult = FirResult(outputs)

            return BrsFirAnalysisResult(
                outputs = outputs,
                diagnosticsReporter = diagnosticsReporter,
                hasErrors = hasErrors,
                firResult = firResult
            )
        } catch (e: Exception) {
            // Print full stack trace for debugging
            System.err.println("=== FIR ANALYSIS EXCEPTION ===")
            e.printStackTrace(System.err)
            System.err.println("=== END EXCEPTION ===")

            messageCollector.report(
                CompilerMessageSeverity.ERROR,
                "FIR analysis failed: ${e.message}"
            )
            return BrsFirAnalysisResult(
                outputs = emptyList(),
                diagnosticsReporter = diagnosticsReporter,
                hasErrors = true
            )
        }
    }

    /**
     * Convert FIR analysis output to IR.
     *
     * @param firResult The FIR analysis result
     * @param configuration The compiler configuration
     * @param messageCollector The message collector for diagnostics
     * @return The IR module fragment, or null if conversion failed
     */
    fun convertToIr(
        firResult: BrsFirAnalysisResult,
        configuration: CompilerConfiguration,
        messageCollector: MessageCollector
    ): BrsFir2IrResult? {
        if (firResult.hasErrors || firResult.outputs.isEmpty()) {
            return null
        }

        try {
            val fir2IrExtensions = Fir2IrExtensions.Default
            val fir2IrConfiguration = Fir2IrConfiguration.forKlibCompilation(
                configuration,
                firResult.diagnosticsReporter
            )

            val fir2IrActualizedResult = FirResult(firResult.outputs).convertToIrAndActualize(
                fir2IrExtensions,
                fir2IrConfiguration,
                emptyList(), // No IR generation extensions for now
                irMangler = BrsManglerIr,
                visibilityConverter = Fir2IrVisibilityConverter.Default,
                kotlinBuiltIns = org.jetbrains.kotlin.builtins.DefaultBuiltIns.Instance,
                typeSystemContextProvider = ::IrTypeSystemContextImpl,
                specialAnnotationsProvider = null,
                extraActualDeclarationExtractorsInitializer = { emptyList() },
            )

            return BrsFir2IrResult(
                irModuleFragment = fir2IrActualizedResult.irModuleFragment,
                irBuiltIns = fir2IrActualizedResult.irBuiltIns,
                symbolTable = fir2IrActualizedResult.symbolTable,
                diagnosticsReporter = firResult.diagnosticsReporter,
                fir2IrActualizedResult = fir2IrActualizedResult
            )
        } catch (e: Exception) {
            messageCollector.report(
                CompilerMessageSeverity.ERROR,
                "FIR to IR conversion failed: ${e.message}"
            )
            return null
        }
    }

}

/**
 * IR mangler for BrightScript.
 *
 * The mangler is responsible for creating unique names for IR declarations
 * that may have the same simple name but different signatures.
 *
 * BrightScript mangling is similar to JS since both are scripting languages.
 */
object BrsManglerIr : IrBasedKotlinManglerImpl() {

    private class BrsIrExportChecker(compatibleMode: Boolean) : IrExportCheckerVisitor(compatibleMode) {
        override fun IrDeclaration.isPlatformSpecificExported() = false
    }

    override fun getExportChecker(compatibleMode: Boolean): KotlinExportChecker<IrDeclaration> =
        BrsIrExportChecker(compatibleMode)

    override fun getMangleComputer(mode: MangleMode, compatibleMode: Boolean): KotlinMangleComputer<IrDeclaration> {
        return IrMangleComputer(StringBuilder(256), mode, compatibleMode)
    }
}
