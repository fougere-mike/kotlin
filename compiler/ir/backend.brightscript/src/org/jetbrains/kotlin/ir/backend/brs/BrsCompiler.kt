/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs

import org.jetbrains.kotlin.brs.BrsTargetConfig
import org.jetbrains.kotlin.brs.backend.ast.*
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.backend.brs.lower.BrsLoweringPhases
import org.jetbrains.kotlin.ir.backend.brs.transformers.irToBrs.IrToBrsTransformer
import org.jetbrains.kotlin.ir.declarations.IrClass
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
 *
 * @param isStdlibCompilation When true, the compiler is compiling the stdlib itself.
 *        This affects how missing stdlib symbols are handled during code generation.
 */
class BrsCompiler(
    private val module: ModuleDescriptor,
    private val irBuiltIns: IrBuiltIns,
    private val symbolTable: SymbolTable,
    private val configuration: CompilerConfiguration,
    private val targetConfig: BrsTargetConfig = BrsTargetConfig.DEFAULT,
    private val isStdlibCompilation: Boolean = false
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
            targetConfig = targetConfig,
            isStdlibCompilation = isStdlibCompilation
        )

        // Create component extractor
        val componentExtractor = BrsComponentExtractor(context)

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

                // Generate component XML for each component class in this file
                generateComponentXmlForFile(file, componentExtractor, context).forEach { (name, xml) ->
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

        // Add runtime helpers to the first file only (they're shared)
        if (context.needsRuntimeHelpers) {
            addRuntimeHelpers(program, context)
            context.needsRuntimeHelpers = false
        }

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

    /**
     * Add runtime helper functions needed by the generated code.
     */
    private fun addRuntimeHelpers(program: BrsProgram, context: BrsIrBackendContext) {
        // Add isInstanceOf helper function for type checking
        val isInstanceOfFunction = createIsInstanceOfHelper()
        program.declarations.add(0, isInstanceOfFunction)

        // In stdlib compilation mode, add inline exception throwing helpers
        // since the stdlib functions are being defined, not linked
        if (context.isStdlibCompilation) {
            addExceptionHelpers(program)
        }
    }

    /**
     * Add exception helper functions for stdlib compilation mode.
     * These are inline implementations that throw BrightScript exceptions.
     */
    private fun addExceptionHelpers(program: BrsProgram) {
        // THROW_NPE - NullPointerException
        program.declarations.add(createExceptionHelper("THROW_NPE", "NullPointerException", "Null pointer access"))

        // THROW_CCE - ClassCastException
        program.declarations.add(createExceptionHelper("THROW_CCE", "ClassCastException", "Invalid type cast"))

        // THROW_ISE - IllegalStateException
        program.declarations.add(createExceptionHelper("THROW_ISE", "IllegalStateException", "Illegal state"))

        // THROW_IAE - IllegalArgumentException
        program.declarations.add(createExceptionHelper("THROW_IAE", "IllegalArgumentException", "Illegal argument"))

        // throwUninitializedPropertyAccessException
        program.declarations.add(createPropertyAccessExceptionHelper())

        // throwKotlinNothingValueException
        program.declarations.add(createNothingValueExceptionHelper())
    }

    /**
     * Create a simple exception throwing helper function.
     *
     * Generated BrightScript:
     * ```
     * sub THROW_NPE()
     *     exc = {
     *         type: "NullPointerException",
     *         message: "Null pointer access"
     *     }
     *     throw exc
     * end sub
     * ```
     */
    private fun createExceptionHelper(name: String, exceptionType: String, message: String): BrsSub {
        val body = BrsBlock(mutableListOf(
            // exc = { type: "...", message: "..." }
            BrsVariable(
                name = "exc",
                initializer = BrsAALiteral(mutableListOf(
                    BrsAAEntry("type", BrsStringLiteral(exceptionType)),
                    BrsAAEntry("message", BrsStringLiteral(message))
                ))
            ),
            // throw exc
            BrsThrow(BrsIdentifier("exc"))
        ))

        return BrsSub(
            name = name,
            parameters = mutableListOf(),
            body = body
        )
    }

    /**
     * Create the throwUninitializedPropertyAccessException helper.
     *
     * Generated BrightScript:
     * ```
     * sub throwUninitializedPropertyAccessException(propertyName as String)
     *     exc = {
     *         type: "UninitializedPropertyAccessException",
     *         message: "lateinit property " + propertyName + " has not been initialized"
     *     }
     *     throw exc
     * end sub
     * ```
     */
    private fun createPropertyAccessExceptionHelper(): BrsSub {
        val body = BrsBlock(mutableListOf(
            BrsVariable(
                name = "exc",
                initializer = BrsAALiteral(mutableListOf(
                    BrsAAEntry("type", BrsStringLiteral("UninitializedPropertyAccessException")),
                    BrsAAEntry("message", BrsBinaryOp(
                        BrsBinaryOp(
                            BrsStringLiteral("lateinit property "),
                            BrsBinaryOperator.ADD,
                            BrsIdentifier("propertyName")
                        ),
                        BrsBinaryOperator.ADD,
                        BrsStringLiteral(" has not been initialized")
                    ))
                ))
            ),
            BrsThrow(BrsIdentifier("exc"))
        ))

        return BrsSub(
            name = "throwUninitializedPropertyAccessException",
            parameters = mutableListOf(BrsParameter("propertyName", BrsType.STRING)),
            body = body
        )
    }

    /**
     * Create the throwKotlinNothingValueException helper.
     *
     * Generated BrightScript:
     * ```
     * sub throwKotlinNothingValueException()
     *     exc = {
     *         type: "KotlinNothingValueException",
     *         message: "This function has a Nothing return type and should never return"
     *     }
     *     throw exc
     * end sub
     * ```
     */
    private fun createNothingValueExceptionHelper(): BrsSub {
        val body = BrsBlock(mutableListOf(
            BrsVariable(
                name = "exc",
                initializer = BrsAALiteral(mutableListOf(
                    BrsAAEntry("type", BrsStringLiteral("KotlinNothingValueException")),
                    BrsAAEntry("message", BrsStringLiteral("This function has a Nothing return type and should never return"))
                ))
            ),
            BrsThrow(BrsIdentifier("exc"))
        ))

        return BrsSub(
            name = "throwKotlinNothingValueException",
            parameters = mutableListOf(),
            body = body
        )
    }

    /**
     * Create the isInstanceOf helper function.
     *
     * Generated BrightScript:
     * ```
     * function isInstanceOf(obj as Object, typeName as String) as Boolean
     *     if obj = invalid then return false
     *     if type(obj) <> "roAssociativeArray" then return false
     *     proto = obj.__proto
     *     if proto = invalid then return false
     *     for each t in proto
     *         if t = typeName then return true
     *     end for
     *     return false
     * end function
     * ```
     */
    private fun createIsInstanceOfHelper(): BrsFunction {
        val body = BrsBlock(mutableListOf(
            // if obj = invalid then return false
            BrsIf(
                condition = BrsBinaryOp(
                    BrsIdentifier("obj"),
                    BrsBinaryOperator.EQ,
                    BrsInvalidLiteral()
                ),
                thenBranch = BrsBlock(mutableListOf(BrsReturn(BrsBooleanLiteral(false)))),
                elseBranch = null
            ),
            // if type(obj) <> "roAssociativeArray" then return false
            BrsIf(
                condition = BrsBinaryOp(
                    BrsTypeOf(BrsIdentifier("obj")),
                    BrsBinaryOperator.NE,
                    BrsStringLiteral("roAssociativeArray")
                ),
                thenBranch = BrsBlock(mutableListOf(BrsReturn(BrsBooleanLiteral(false)))),
                elseBranch = null
            ),
            // proto = obj.__proto
            BrsVariable(
                name = "proto",
                initializer = BrsDotAccess(BrsIdentifier("obj"), "__proto")
            ),
            // if proto = invalid then return false
            BrsIf(
                condition = BrsBinaryOp(
                    BrsIdentifier("proto"),
                    BrsBinaryOperator.EQ,
                    BrsInvalidLiteral()
                ),
                thenBranch = BrsBlock(mutableListOf(BrsReturn(BrsBooleanLiteral(false)))),
                elseBranch = null
            ),
            // for each t in proto
            BrsForEach(
                variable = "t",
                iterable = BrsIdentifier("proto"),
                body = BrsBlock(mutableListOf(
                    // if t = typeName then return true
                    BrsIf(
                        condition = BrsBinaryOp(
                            BrsIdentifier("t"),
                            BrsBinaryOperator.EQ,
                            BrsIdentifier("typeName")
                        ),
                        thenBranch = BrsBlock(mutableListOf(BrsReturn(BrsBooleanLiteral(true)))),
                        elseBranch = null
                    )
                ))
            ),
            // return false
            BrsReturn(BrsBooleanLiteral(false))
        ))

        return BrsFunction(
            name = "isInstanceOf",
            parameters = mutableListOf(
                BrsParameter("obj", BrsType.OBJECT),
                BrsParameter("typeName", BrsType.STRING)
            ),
            returnType = BrsType.BOOLEAN,
            body = body
        )
    }

    private fun computeOutputPath(originalPath: String, context: BrsIrBackendContext): String {
        val fileName = File(originalPath).nameWithoutExtension + ".brs"
        return File(context.targetConfig.outputDir, fileName).path
    }

    /**
     * Generate component XML for all component classes in a file.
     */
    private fun generateComponentXmlForFile(
        irFile: IrFile,
        extractor: BrsComponentExtractor,
        context: BrsIrBackendContext
    ): Map<String, String> {
        val result = mutableMapOf<String, String>()

        // Find component classes in this file
        val componentClasses = irFile.declarations.filterIsInstance<IrClass>()
            .filter { extractor.isComponent(it) }

        for (componentClass in componentClasses) {
            val componentInfo = extractor.extractComponent(componentClass)
            if (componentInfo != null) {
                result[componentInfo.name] = generateComponentXmlContent(componentInfo, context)
            }
        }

        return result
    }

    /**
     * Generate the XML content for a SceneGraph component.
     */
    private fun generateComponentXmlContent(
        component: BrsComponentInfo,
        context: BrsIrBackendContext
    ): String {
        val builder = StringBuilder()

        builder.appendLine("""<?xml version="1.0" encoding="utf-8" ?>""")
        builder.appendLine("""<component name="${component.name}" extends="${component.extendsComponent}">""")

        // Add script reference
        builder.appendLine("""    <script type="text/brightscript" uri="pkg:/source/${component.name}.brs" />""")

        // Add additional scripts
        for (script in component.additionalScripts) {
            builder.appendLine("""    <script type="text/brightscript" uri="$script" />""")
        }

        // Generate interface section
        builder.appendLine("    <interface>")

        // Add fields
        for (field in component.fields) {
            builder.append("        <field id=\"${field.name}\" type=\"${field.type}\"")

            field.defaultValue?.let { defaultValue ->
                builder.append(" value=\"$defaultValue\"")
            }

            field.onChange?.let { onChange ->
                builder.append(" onChange=\"$onChange\"")
            }

            if (field.alwaysNotify) {
                builder.append(" alwaysNotify=\"true\"")
            }

            field.alias?.let { alias ->
                builder.append(" alias=\"$alias\"")
            }

            builder.appendLine(" />")
        }

        // Add exported functions
        for (export in component.exports) {
            builder.appendLine("        <function name=\"${export.name}\" />")
        }

        builder.appendLine("    </interface>")
        builder.appendLine("</component>")

        return builder.toString()
    }

    /**
     * Generate component initialization code for a component class.
     *
     * This creates the init() function that sets up field observers
     * and initializes the component state.
     */
    fun generateComponentInitCode(component: BrsComponentInfo): BrsDeclaration {
        val bodyStatements = mutableListOf<BrsStatement>()

        // Add field observers for onChange handlers
        for (field in component.fields) {
            if (field.onChange != null) {
                // m.top.observeField("fieldName", "handlerName")
                bodyStatements.add(
                    BrsExpressionStatement(
                        BrsMethodCall(
                            BrsDotAccess(BrsMRef(), "top"),
                            "observeField",
                            mutableListOf(
                                BrsStringLiteral(field.name),
                                BrsStringLiteral(field.onChange)
                            )
                        )
                    )
                )
            }
        }

        // Initialize fields with default values from IR
        for (field in component.fields) {
            val irField = field.irField ?: field.irProperty?.backingField
            if (irField?.initializer != null) {
                // m.top.fieldName = initialValue
                // Note: This would require transformer access - handled elsewhere
            }
        }

        return BrsSub(
            name = "init",
            parameters = mutableListOf(),
            body = BrsBlock(bodyStatements)
        )
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
