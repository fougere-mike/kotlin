/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs

import org.jetbrains.kotlin.brs.BrsTargetConfig
import org.jetbrains.kotlin.brs.backend.ast.BrsProgram
import org.jetbrains.kotlin.brs.backend.ast.render
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.backend.brs.lower.BrsLoweringPhases
import org.jetbrains.kotlin.ir.backend.brs.transformers.irToBrs.IrToBrsTransformer
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.name
import org.jetbrains.kotlin.ir.declarations.path
import org.jetbrains.kotlin.ir.util.SymbolTable
import java.io.File

/**
 * Output of BrightScript compilation for a single file.
 */
data class BrsCompilationOutput(
    /**
     * The generated BrightScript AST.
     */
    val program: BrsProgram,

    /**
     * The rendered BrightScript source code.
     */
    val sourceCode: String,

    /**
     * The original Kotlin source file path.
     */
    val originalFilePath: String,

    /**
     * The output .brs file path.
     */
    val outputFilePath: String
)

/**
 * Result of compiling a Kotlin module to BrightScript.
 */
data class BrsModuleCompilationResult(
    /**
     * Compilation outputs for each source file.
     */
    val outputs: List<BrsCompilationOutput>,

    /**
     * Any generated SceneGraph component XML files.
     */
    val componentXml: Map<String, String>,

    /**
     * Errors encountered during compilation.
     */
    val errors: List<String>
)

/**
 * Main entry point for BrightScript compilation.
 *
 * This class orchestrates the compilation pipeline from Kotlin IR to BrightScript.
 */
class BrsCompiler(
    private val module: ModuleDescriptor,
    private val irBuiltIns: IrBuiltIns,
    private val symbolTable: SymbolTable,
    private val configuration: CompilerConfiguration,
    private val targetConfig: BrsTargetConfig = BrsTargetConfig.DEFAULT
) {
    /**
     * Compile a Kotlin IR module to BrightScript.
     */
    fun compile(irModule: IrModuleFragment): BrsModuleCompilationResult {
        val context = BrsIrBackendContext(
            module = module,
            irBuiltIns = irBuiltIns,
            symbolTable = symbolTable,
            configuration = configuration,
            targetConfig = targetConfig
        )

        // Run lowering phases
        val loweredModule = BrsLoweringPhases.lower(irModule, context)

        // Transform to BrightScript
        val transformer = IrToBrsTransformer(context)
        val outputs = mutableListOf<BrsCompilationOutput>()
        val errors = mutableListOf<String>()
        val componentXml = mutableMapOf<String, String>()

        for (file in loweredModule.files) {
            try {
                val output = compileFile(file, transformer, context)
                outputs.add(output)

                // Generate component XML if needed
                generateComponentXml(file, context)?.let { (name, xml) ->
                    componentXml[name] = xml
                }
            } catch (e: Exception) {
                errors.add("Error compiling ${file.name}: ${e.message}")
            }
        }

        return BrsModuleCompilationResult(outputs, componentXml, errors)
    }

    private fun compileFile(
        irFile: IrFile,
        transformer: IrToBrsTransformer,
        context: BrsIrBackendContext
    ): BrsCompilationOutput {
        val program = transformer.transformFile(irFile)
        val sourceCode = program.render()

        val originalPath = irFile.path
        val outputPath = computeOutputPath(originalPath, context)

        return BrsCompilationOutput(
            program = program,
            sourceCode = sourceCode,
            originalFilePath = originalPath,
            outputFilePath = outputPath
        )
    }

    private fun computeOutputPath(originalPath: String, context: BrsIrBackendContext): String {
        val fileName = File(originalPath).nameWithoutExtension + ".brs"
        return File(context.targetConfig.outputDir, fileName).path
    }

    private fun generateComponentXml(
        irFile: IrFile,
        context: BrsIrBackendContext
    ): Pair<String, String>? {
        // Find component classes in this file
        val componentClasses = irFile.declarations.filterIsInstance<org.jetbrains.kotlin.ir.declarations.IrClass>()
            .filter { context.isComponent(it) }

        if (componentClasses.isEmpty()) return null

        // Generate XML for each component
        val xmlBuilder = StringBuilder()
        for (componentClass in componentClasses) {
            xmlBuilder.append(generateComponentXmlContent(componentClass, context))
        }

        val componentName = componentClasses.first().name.asString()
        return componentName to xmlBuilder.toString()
    }

    private fun generateComponentXmlContent(
        irClass: org.jetbrains.kotlin.ir.declarations.IrClass,
        context: BrsIrBackendContext
    ): String {
        val className = context.getBrsName(irClass)

        // Find extends annotation or default to Group
        val extendsType = "Group" // Will be extracted from annotation in full implementation

        val scriptName = "${className}.brs"

        return """
            <?xml version="1.0" encoding="utf-8" ?>
            <component name="$className" extends="$extendsType">
                <script type="text/brightscript" uri="pkg:/source/$scriptName" />
                <interface>
                    <!-- Fields and functions will be generated here -->
                </interface>
            </component>
        """.trimIndent()
    }

    companion object {
        /**
         * Write compilation results to the file system.
         */
        fun writeOutput(result: BrsModuleCompilationResult, outputDir: File) {
            outputDir.mkdirs()

            // Write .brs files
            val sourceDir = File(outputDir, "source")
            sourceDir.mkdirs()

            for (output in result.outputs) {
                val outputFile = File(sourceDir, File(output.outputFilePath).name)
                outputFile.writeText(output.sourceCode)
            }

            // Write component XML files
            val componentsDir = File(outputDir, "components")
            if (result.componentXml.isNotEmpty()) {
                componentsDir.mkdirs()
                for ((name, xml) in result.componentXml) {
                    val xmlFile = File(componentsDir, "$name.xml")
                    xmlFile.writeText(xml)
                }
            }
        }
    }
}
