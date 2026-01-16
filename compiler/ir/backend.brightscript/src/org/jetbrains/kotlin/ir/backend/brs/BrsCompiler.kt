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
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrPackageFragment
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.name
import org.jetbrains.kotlin.ir.declarations.path
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.ir.util.getAnnotation
import org.jetbrains.kotlin.name.FqName
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
     * Key is component name, value is XML content.
     */
    val componentXml: Map<String, String>,

    /**
     * Dependency manifests for SceneGraph components.
     * Key is component name, value is deps.json content.
     */
    val componentDepsJson: Map<String, String>,

    /**
     * Function-to-file manifest mapping BrightScript function names to their output .brs files.
     * This is used by consuming modules to resolve dependencies when calling klib functions.
     * Key is the mangled function name (e.g., "mutableListOf_k_"), value is the .brs filename (e.g., "ArrayList.brs").
     */
    val functionManifest: Map<String, String>,

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
    private val isStdlibCompilation: Boolean = false,
    /**
     * Function-to-file manifest loaded from klib dependencies.
     * Maps BrightScript function names to their .brs output files.
     * Used to resolve dependencies accurately when calling klib functions.
     */
    private val dependencyFunctionManifest: Map<String, String> = emptyMap(),
    /**
     * File-to-file dependency graph loaded from klib dependencies.
     * Maps .brs file names to the set of .brs files they depend on.
     * Used to resolve transitive dependencies for deps.json.
     */
    private val dependencyFileDeps: Map<String, Set<String>> = emptyMap()
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
            isStdlibCompilation = isStdlibCompilation,
            dependencyFunctionManifest = dependencyFunctionManifest,
            dependencyFileDeps = dependencyFileDeps
        )

        // Create component extractor
        val componentExtractor = BrsComponentExtractor(context)

        // Validate @BrsStatic annotations before lowering
        validateBrsStaticAnnotations(irModule, context)

        // Run lowering phases
        val loweredModule = BrsLoweringPhases.lower(irModule, context)

        // Transform to BrightScript
        val transformer = IrToBrsTransformer(context)
        val outputs = mutableListOf<BrsCompilationOutput>()
        val errors = mutableListOf<String>()
        val componentXml = mutableMapOf<String, String>()
        val componentDepsJson = mutableMapOf<String, String>()
        val functionManifest = mutableMapOf<String, String>()

        for (file in loweredModule.files) {
            try {
                val output = compileFile(file, transformer, context)
                outputs.add(output)

                // Build function-to-file manifest for this file
                val outputFileName = File(file.path).nameWithoutExtension + "Kt.brs"
                collectFunctionManifest(file, outputFileName, context, functionManifest)

                // Generate component XML and deps.json for each component class in this file
                generateComponentOutputForFile(file, componentExtractor, context).forEach { (name, xml, depsJson) ->
                    componentXml[name] = xml
                    componentDepsJson[name] = depsJson
                }
            } catch (e: Exception) {
                errors.add("Error compiling ${file.name}: ${e.message}")
            }
        }

        return BrsModuleCompilationResult(outputs, componentXml, componentDepsJson, functionManifest, errors)
    }

    private val brsStaticFqn = FqName("kotlin.brs.BrsStatic")

    /**
     * Validate all @BrsStatic annotations in the module.
     * Reports errors for:
     * - @BrsStatic on regular class members (only top-level, object, or companion object allowed)
     * - @BrsStatic on functions that have overloads (same name in same scope)
     */
    private fun validateBrsStaticAnnotations(irModule: IrModuleFragment, context: BrsIrBackendContext) {
        for (file in irModule.files) {
            // Collect all @BrsStatic functions grouped by their scope
            val staticFunctionsByScope = mutableMapOf<Any, MutableList<IrSimpleFunction>>()

            // Check top-level functions
            for (declaration in file.declarations) {
                when (declaration) {
                    is IrSimpleFunction -> {
                        if (declaration.getAnnotation(brsStaticFqn) != null) {
                            staticFunctionsByScope.getOrPut(file) { mutableListOf() }.add(declaration)
                        }
                    }
                    is IrClass -> {
                        validateBrsStaticInClass(declaration, context, staticFunctionsByScope)
                    }
                }
            }

            // Check for overloads within each scope
            for ((scope, functions) in staticFunctionsByScope) {
                val functionsByName = functions.groupBy { it.name.asString() }
                for ((name, overloads) in functionsByName) {
                    if (overloads.size > 1) {
                        // Report error on each function with the same name
                        for (func in overloads) {
                            context.reportError(
                                func,
                                "@BrsStatic function '$name' cannot have overloads. " +
                                "Found ${overloads.size} functions with the same name in the same scope."
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Recursively validate @BrsStatic annotations in a class and its nested declarations.
     */
    private fun validateBrsStaticInClass(
        irClass: IrClass,
        context: BrsIrBackendContext,
        staticFunctionsByScope: MutableMap<Any, MutableList<IrSimpleFunction>>
    ) {
        val isValidContext = irClass.kind == ClassKind.OBJECT || irClass.isCompanion

        for (declaration in irClass.declarations) {
            when (declaration) {
                is IrSimpleFunction -> {
                    if (declaration.getAnnotation(brsStaticFqn) != null) {
                        if (!isValidContext) {
                            // @BrsStatic on a regular class member - error
                            context.reportError(
                                declaration,
                                "@BrsStatic is only valid on top-level functions, " +
                                "object members, or companion object members. " +
                                "Found on member of class '${irClass.name.asString()}'."
                            )
                        } else {
                            // Valid context - track for overload detection
                            staticFunctionsByScope.getOrPut(irClass) { mutableListOf() }.add(declaration)
                        }
                    }
                }
                is IrClass -> {
                    // Recurse into nested classes
                    validateBrsStaticInClass(declaration, context, staticFunctionsByScope)
                }
            }
        }
    }

    /**
     * Collect function-to-file mappings for all declarations in this file.
     * This builds a manifest that maps BrightScript function names to their output .brs files,
     * enabling consuming modules to resolve dependencies correctly.
     */
    private fun collectFunctionManifest(
        file: IrFile,
        outputFileName: String,
        context: BrsIrBackendContext,
        manifest: MutableMap<String, String>
    ) {
        for (declaration in file.declarations) {
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

    private fun compileFile(
        irFile: IrFile,
        transformer: IrToBrsTransformer,
        context: BrsIrBackendContext
    ): BrsCompilationOutput {
        val program = transformer.transformFile(irFile)

        // Add runtime helpers to the first file only (they're shared)
        // Only generate during stdlib compilation - user code uses stdlib's version
        if (context.needsRuntimeHelpers && context.isStdlibCompilation) {
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

        // Add identity check helper function for === operator
        val identityEqualsFunction = createIdentityEqualsHelper()
        program.declarations.add(0, identityEqualsFunction)

        // Add object ID counter and generator for identity tracking
        val nextObjectIdFunction = createNextObjectIdHelper()
        program.declarations.add(0, nextObjectIdFunction)

        // Add Int.compareTo helper for primitive comparisons
        val intCompareFunction = createIntCompareHelper()
        program.declarations.add(0, intCompareFunction)

        // Add String.compareTo helper for Char comparisons
        val stringCompareFunction = createStringCompareHelper()
        program.declarations.add(0, stringCompareFunction)

        // Add unsigned right shift helper for Int.ushr
        val ushrFunction = createUshrHelper()
        program.declarations.add(0, ushrFunction)

        // Note: Exception helpers (THROW_NPE, THROW_CCE, etc.) are defined in
        // libraries/stdlib/brs/src/kotlin/ExceptionHelpers.kt - do NOT add them here
        // as that would create duplicate definitions.
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
     * Create the nextObjectId helper function for generating unique object IDs.
     *
     * Generated BrightScript:
     * ```
     * function __kotlin_nextObjectId() as Integer
     *     if m.__kotlin_objectIdCounter = invalid then
     *         m.__kotlin_objectIdCounter = 0
     *     end if
     *     m.__kotlin_objectIdCounter = m.__kotlin_objectIdCounter + 1
     *     return m.__kotlin_objectIdCounter
     * end function
     * ```
     */
    private fun createNextObjectIdHelper(): BrsFunction {
        val body = BrsBlock(mutableListOf(
            // if m.__kotlin_objectIdCounter = invalid then m.__kotlin_objectIdCounter = 0
            BrsIf(
                condition = BrsBinaryOp(
                    BrsDotAccess(BrsIdentifier("m"), "__kotlin_objectIdCounter"),
                    BrsBinaryOperator.EQ,
                    BrsInvalidLiteral()
                ),
                thenBranch = BrsBlock(mutableListOf(
                    BrsExpressionStatement(
                        BrsBinaryOp(
                            BrsDotAccess(BrsIdentifier("m"), "__kotlin_objectIdCounter"),
                            BrsBinaryOperator.EQ,
                            BrsIntLiteral(0)
                        )
                    )
                )),
                elseBranch = null
            ),
            // m.__kotlin_objectIdCounter = m.__kotlin_objectIdCounter + 1
            BrsExpressionStatement(
                BrsBinaryOp(
                    BrsDotAccess(BrsIdentifier("m"), "__kotlin_objectIdCounter"),
                    BrsBinaryOperator.EQ,
                    BrsBinaryOp(
                        BrsDotAccess(BrsIdentifier("m"), "__kotlin_objectIdCounter"),
                        BrsBinaryOperator.ADD,
                        BrsIntLiteral(1)
                    )
                )
            ),
            // return m.__kotlin_objectIdCounter
            BrsReturn(BrsDotAccess(BrsIdentifier("m"), "__kotlin_objectIdCounter"))
        ))

        return BrsFunction(
            name = "__kotlin_nextObjectId",
            parameters = mutableListOf(),
            returnType = BrsType.INTEGER,
            body = body
        )
    }

    /**
     * Create the identity equals helper function for === operator.
     *
     * Generated BrightScript:
     * ```
     * function __kotlin_identityEquals(a as Dynamic, b as Dynamic) as Boolean
     *     ' Both invalid (null)
     *     if a = invalid and b = invalid then return true
     *     ' One is invalid
     *     if a = invalid or b = invalid then return false
     *     ' Both are primitives - use value equality
     *     aType = type(a)
     *     bType = type(b)
     *     if aType <> "roAssociativeArray" and bType <> "roAssociativeArray" then
     *         return a = b
     *     end if
     *     ' Both must be objects for identity comparison
     *     if aType <> "roAssociativeArray" or bType <> "roAssociativeArray" then
     *         return false
     *     end if
     *     ' Compare object IDs
     *     if a.__id = invalid or b.__id = invalid then return false
     *     return a.__id = b.__id
     * end function
     * ```
     */
    private fun createIdentityEqualsHelper(): BrsFunction {
        val body = BrsBlock(mutableListOf(
            // if a = invalid and b = invalid then return true
            BrsIf(
                condition = BrsBinaryOp(
                    BrsBinaryOp(BrsIdentifier("a"), BrsBinaryOperator.EQ, BrsInvalidLiteral()),
                    BrsBinaryOperator.AND,
                    BrsBinaryOp(BrsIdentifier("b"), BrsBinaryOperator.EQ, BrsInvalidLiteral())
                ),
                thenBranch = BrsBlock(mutableListOf(BrsReturn(BrsBooleanLiteral(true)))),
                elseBranch = null
            ),
            // if a = invalid or b = invalid then return false
            BrsIf(
                condition = BrsBinaryOp(
                    BrsBinaryOp(BrsIdentifier("a"), BrsBinaryOperator.EQ, BrsInvalidLiteral()),
                    BrsBinaryOperator.OR,
                    BrsBinaryOp(BrsIdentifier("b"), BrsBinaryOperator.EQ, BrsInvalidLiteral())
                ),
                thenBranch = BrsBlock(mutableListOf(BrsReturn(BrsBooleanLiteral(false)))),
                elseBranch = null
            ),
            // aType = type(a)
            BrsVariable(name = "aType", initializer = BrsTypeOf(BrsIdentifier("a"))),
            // bType = type(b)
            BrsVariable(name = "bType", initializer = BrsTypeOf(BrsIdentifier("b"))),
            // if aType <> "roAssociativeArray" and bType <> "roAssociativeArray" then return a = b
            BrsIf(
                condition = BrsBinaryOp(
                    BrsBinaryOp(BrsIdentifier("aType"), BrsBinaryOperator.NE, BrsStringLiteral("roAssociativeArray")),
                    BrsBinaryOperator.AND,
                    BrsBinaryOp(BrsIdentifier("bType"), BrsBinaryOperator.NE, BrsStringLiteral("roAssociativeArray"))
                ),
                thenBranch = BrsBlock(mutableListOf(
                    BrsReturn(BrsBinaryOp(BrsIdentifier("a"), BrsBinaryOperator.EQ, BrsIdentifier("b")))
                )),
                elseBranch = null
            ),
            // if aType <> "roAssociativeArray" or bType <> "roAssociativeArray" then return false
            BrsIf(
                condition = BrsBinaryOp(
                    BrsBinaryOp(BrsIdentifier("aType"), BrsBinaryOperator.NE, BrsStringLiteral("roAssociativeArray")),
                    BrsBinaryOperator.OR,
                    BrsBinaryOp(BrsIdentifier("bType"), BrsBinaryOperator.NE, BrsStringLiteral("roAssociativeArray"))
                ),
                thenBranch = BrsBlock(mutableListOf(BrsReturn(BrsBooleanLiteral(false)))),
                elseBranch = null
            ),
            // if a.__id = invalid or b.__id = invalid then return false
            BrsIf(
                condition = BrsBinaryOp(
                    BrsBinaryOp(BrsDotAccess(BrsIdentifier("a"), "__id"), BrsBinaryOperator.EQ, BrsInvalidLiteral()),
                    BrsBinaryOperator.OR,
                    BrsBinaryOp(BrsDotAccess(BrsIdentifier("b"), "__id"), BrsBinaryOperator.EQ, BrsInvalidLiteral())
                ),
                thenBranch = BrsBlock(mutableListOf(BrsReturn(BrsBooleanLiteral(false)))),
                elseBranch = null
            ),
            // return a.__id = b.__id
            BrsReturn(BrsBinaryOp(
                BrsDotAccess(BrsIdentifier("a"), "__id"),
                BrsBinaryOperator.EQ,
                BrsDotAccess(BrsIdentifier("b"), "__id")
            ))
        ))

        return BrsFunction(
            name = "__kotlin_identityEquals",
            parameters = mutableListOf(
                BrsParameter("a", BrsType.DYNAMIC),
                BrsParameter("b", BrsType.DYNAMIC)
            ),
            returnType = BrsType.BOOLEAN,
            body = body
        )
    }

    /**
     * Create the isInstanceOf helper function.
     *
     * Generated BrightScript:
     * The __proto field can be either:
     * 1. Flat array of type names (no inheritance): ["ArrayList", "MutableList", ...]
     * 2. Array with parent proto (inheritance): ["Error", "Interface", parentProto]
     *
     * Uses a stack-based approach to handle both formats:
     * - Push proto onto stack
     * - Pop and check each item
     * - If item is a string, compare to typeName
     * - If item is an array, push all its elements
     *
     * ```
     * function isInstanceOf(obj as Object, typeName as String) as Boolean
     *     if obj = invalid then return false
     *     if type(obj) <> "roAssociativeArray" then return false
     *     proto = obj.__proto
     *     if proto = invalid then return false
     *     stack = [proto]
     *     while stack.count() > 0
     *         item = stack.pop()
     *         if item = invalid then ' skip
     *         else if Type(item) = "roArray" then
     *             for each e in item : stack.push(e) : end for
     *         else if item = typeName then return true
     *         end if
     *     end while
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
            // stack = [proto]
            BrsVariable(
                name = "stack",
                initializer = BrsArrayLiteral(mutableListOf(BrsIdentifier("proto")))
            ),
            // while stack.count() > 0
            BrsWhile(
                condition = BrsBinaryOp(
                    BrsMethodCall(BrsIdentifier("stack"), "count", mutableListOf()),
                    BrsBinaryOperator.GT,
                    BrsIntLiteral(0)
                ),
                body = BrsBlock(mutableListOf(
                    // item = stack.pop()
                    BrsVariable(
                        name = "item",
                        initializer = BrsMethodCall(BrsIdentifier("stack"), "pop", mutableListOf())
                    ),
                    // if item = invalid then skip (continue)
                    BrsIf(
                        condition = BrsBinaryOp(
                            BrsIdentifier("item"),
                            BrsBinaryOperator.EQ,
                            BrsInvalidLiteral()
                        ),
                        thenBranch = BrsBlock(mutableListOf()), // do nothing, continue loop
                        // else if Type(item) = "roArray" then push all elements
                        elseBranch = BrsIf(
                            condition = BrsBinaryOp(
                                BrsTypeOf(BrsIdentifier("item")),
                                BrsBinaryOperator.EQ,
                                BrsStringLiteral("roArray")
                            ),
                            thenBranch = BrsBlock(mutableListOf(
                                // for each e in item : stack.push(e) : end for
                                BrsForEach(
                                    variable = "e",
                                    iterable = BrsIdentifier("item"),
                                    body = BrsBlock(mutableListOf(
                                        BrsExpressionStatement(
                                            BrsMethodCall(BrsIdentifier("stack"), "push", mutableListOf(BrsIdentifier("e")))
                                        )
                                    ))
                                )
                            )),
                            // else if item = typeName then return true
                            elseBranch = BrsIf(
                                condition = BrsBinaryOp(
                                    BrsIdentifier("item"),
                                    BrsBinaryOperator.EQ,
                                    BrsIdentifier("typeName")
                                ),
                                thenBranch = BrsBlock(mutableListOf(BrsReturn(BrsBooleanLiteral(true)))),
                                elseBranch = null
                            )
                        )
                    )
                ))
            ),
            // return false
            BrsReturn(BrsBooleanLiteral(false))
        ))

        return BrsFunction(
            name = "__kotlin_isInstanceOf",
            parameters = mutableListOf(
                BrsParameter("obj", BrsType.OBJECT),
                BrsParameter("typeName", BrsType.STRING)
            ),
            returnType = BrsType.BOOLEAN,
            body = body
        )
    }

    /**
     * Creates the __kotlin_intCompare helper function for Int.compareTo().
     *
     * Generated BrightScript:
     * ```
     * function __kotlin_intCompare(a as Integer, b as Integer) as Integer
     *     if a < b then return -1
     *     if a > b then return 1
     *     return 0
     * end function
     * ```
     */
    private fun createIntCompareHelper(): BrsFunction {
        val body = BrsBlock(mutableListOf(
            // if a < b then return -1
            BrsIf(
                condition = BrsBinaryOp(
                    BrsIdentifier("a"),
                    BrsBinaryOperator.LT,
                    BrsIdentifier("b")
                ),
                thenBranch = BrsBlock(mutableListOf(BrsReturn(BrsIntLiteral(-1)))),
                elseBranch = null
            ),
            // if a > b then return 1
            BrsIf(
                condition = BrsBinaryOp(
                    BrsIdentifier("a"),
                    BrsBinaryOperator.GT,
                    BrsIdentifier("b")
                ),
                thenBranch = BrsBlock(mutableListOf(BrsReturn(BrsIntLiteral(1)))),
                elseBranch = null
            ),
            // return 0
            BrsReturn(BrsIntLiteral(0))
        ))

        return BrsFunction(
            name = "__kotlin_intCompare",
            parameters = mutableListOf(
                BrsParameter("a", BrsType.INTEGER),
                BrsParameter("b", BrsType.INTEGER)
            ),
            returnType = BrsType.INTEGER,
            body = body
        )
    }

    /**
     * Creates the __kotlin_stringCompare helper function for string/Char comparison.
     *
     * Generated BrightScript:
     * ```
     * function __kotlin_stringCompare(a as String, b as String) as Integer
     *     if a < b then return -1
     *     if a > b then return 1
     *     return 0
     * end function
     * ```
     */
    private fun createStringCompareHelper(): BrsFunction {
        val body = BrsBlock(mutableListOf(
            // if a < b then return -1
            BrsIf(
                condition = BrsBinaryOp(
                    BrsIdentifier("a"),
                    BrsBinaryOperator.LT,
                    BrsIdentifier("b")
                ),
                thenBranch = BrsBlock(mutableListOf(BrsReturn(BrsIntLiteral(-1)))),
                elseBranch = null
            ),
            // if a > b then return 1
            BrsIf(
                condition = BrsBinaryOp(
                    BrsIdentifier("a"),
                    BrsBinaryOperator.GT,
                    BrsIdentifier("b")
                ),
                thenBranch = BrsBlock(mutableListOf(BrsReturn(BrsIntLiteral(1)))),
                elseBranch = null
            ),
            // return 0
            BrsReturn(BrsIntLiteral(0))
        ))

        return BrsFunction(
            name = "__kotlin_stringCompare",
            parameters = mutableListOf(
                BrsParameter("a", BrsType.STRING),
                BrsParameter("b", BrsType.STRING)
            ),
            returnType = BrsType.INTEGER,
            body = body
        )
    }

    /**
     * Creates the __kotlin_ushr helper function for unsigned right shift.
     *
     * Generated BrightScript:
     * ```
     * function __kotlin_ushr(value as Integer, shift as Integer) as Integer
     *     if shift >= 32 then return 0
     *     if shift = 0 then return value
     *     if value >= 0 then return value \ (2 ^ shift)
     *     ' For negative values, need to handle sign bit
     *     return ((value and &H7FFFFFFF) \ (2 ^ shift)) or (&H40000000 \ (2 ^ (shift - 1)))
     * end function
     * ```
     */
    private fun createUshrHelper(): BrsFunction {
        val body = BrsBlock(mutableListOf(
            // if shift >= 32 then return 0
            BrsIf(
                condition = BrsBinaryOp(
                    BrsIdentifier("shift"),
                    BrsBinaryOperator.GE,
                    BrsIntLiteral(32)
                ),
                thenBranch = BrsBlock(mutableListOf(BrsReturn(BrsIntLiteral(0)))),
                elseBranch = null
            ),
            // if shift = 0 then return value
            BrsIf(
                condition = BrsBinaryOp(
                    BrsIdentifier("shift"),
                    BrsBinaryOperator.EQ,
                    BrsIntLiteral(0)
                ),
                thenBranch = BrsBlock(mutableListOf(BrsReturn(BrsIdentifier("value")))),
                elseBranch = null
            ),
            // if value >= 0 then return value \ (2 ^ shift)
            BrsIf(
                condition = BrsBinaryOp(
                    BrsIdentifier("value"),
                    BrsBinaryOperator.GE,
                    BrsIntLiteral(0)
                ),
                thenBranch = BrsBlock(mutableListOf(
                    BrsReturn(
                        BrsBinaryOp(
                            BrsIdentifier("value"),
                            BrsBinaryOperator.INT_DIV,
                            BrsBinaryOp(BrsIntLiteral(2), BrsBinaryOperator.POW, BrsIdentifier("shift"))
                        )
                    )
                )),
                elseBranch = null
            ),
            // For negative values: ((value and &H7FFFFFFF) \ (2 ^ shift)) or (&H40000000 \ (2 ^ (shift - 1)))
            BrsReturn(
                BrsBinaryOp(
                    BrsBinaryOp(
                        BrsBinaryOp(
                            BrsIdentifier("value"),
                            BrsBinaryOperator.AND,
                            BrsIntLiteral(0x7FFFFFFF)
                        ),
                        BrsBinaryOperator.INT_DIV,
                        BrsBinaryOp(BrsIntLiteral(2), BrsBinaryOperator.POW, BrsIdentifier("shift"))
                    ),
                    BrsBinaryOperator.OR,
                    BrsBinaryOp(
                        BrsIntLiteral(0x40000000),
                        BrsBinaryOperator.INT_DIV,
                        BrsBinaryOp(
                            BrsIntLiteral(2),
                            BrsBinaryOperator.POW,
                            BrsBinaryOp(BrsIdentifier("shift"), BrsBinaryOperator.SUB, BrsIntLiteral(1))
                        )
                    )
                )
            )
        ))

        return BrsFunction(
            name = "__kotlin_ushr",
            parameters = mutableListOf(
                BrsParameter("value", BrsType.INTEGER),
                BrsParameter("shift", BrsType.INTEGER)
            ),
            returnType = BrsType.INTEGER,
            body = body
        )
    }

    private fun computeOutputPath(originalPath: String, context: BrsIrBackendContext): String {
        val fileName = File(originalPath).nameWithoutExtension + "Kt.brs"
        return File(context.targetConfig.outputDir, fileName).path
    }

    /**
     * Output data for a SceneGraph component.
     */
    private data class BrsComponentOutput(
        val name: String,
        val xml: String,
        val depsJson: String
    )

    /**
     * Generate component XML and deps.json for all component classes in a file.
     */
    private fun generateComponentOutputForFile(
        irFile: IrFile,
        extractor: BrsComponentExtractor,
        context: BrsIrBackendContext
    ): List<BrsComponentOutput> {
        val result = mutableListOf<BrsComponentOutput>()

        // Find component classes in this file
        val componentClasses = irFile.declarations.filterIsInstance<IrClass>()
            .filter { extractor.isComponent(it) }

        // Compute dependencies once for all components in this file
        // (they share the same file path, so dependencies are the same)
        val dependencies = computeComponentDependencies(irFile.path, context)

        for (componentClass in componentClasses) {
            val componentInfo = extractor.extractComponent(componentClass)
            if (componentInfo != null) {
                val xml = generateComponentXmlContent(componentInfo, context, dependencies)
                val depsJson = generateComponentDepsJson(
                    componentInfo.name,
                    dependencies,
                    context.dependencyCollector.getRuntimeFunctions(irFile.path)
                )
                result.add(BrsComponentOutput(componentInfo.name, xml, depsJson))
            }
        }

        return result
    }

    /**
     * Compute the set of dependencies for a component file.
     * Resolves transitive dependencies from both stdlib and user code.
     */
    private fun computeComponentDependencies(
        filePath: String,
        context: BrsIrBackendContext
    ): Set<String> {
        // Get direct dependencies from the collector
        val directDeps = context.dependencyCollector.getDependencies(filePath)

        // Build complete file deps graph by merging:
        // 1. Stdlib file deps (from klib)
        // 2. User code file deps (from current compilation)
        val userFileDeps = context.dependencyCollector.getAllFileDependencies()
        val mergedFileDeps = context.dependencyFileDeps.toMutableMap<String, Set<String>>()
        for ((file, deps) in userFileDeps) {
            mergedFileDeps.merge(file, deps) { existing, new -> existing + new }
        }

        // Resolve transitive dependencies using the merged graph
        return resolveTransitiveDependencies(directDeps, mergedFileDeps)
    }

    /**
     * Generate deps.json content for a SceneGraph component.
     *
     * This manifest lists all .brs files that this component depends on,
     * enabling the Gradle plugin to inject the correct <script> tags.
     *
     * @param componentName The name of the component
     * @param dependencies Pre-computed set of dependencies for this component
     * @param runtimeFunctions Set of __kotlin_* functions used by this component
     */
    private fun generateComponentDepsJson(
        componentName: String,
        dependencies: Set<String>,
        runtimeFunctions: Set<String>
    ): String {
        val allDeps = dependencies.sorted()
        val runtimeFuncs = runtimeFunctions.sorted()

        return buildString {
            appendLine("{")
            appendLine("""  "version": 1,""")
            appendLine("""  "component": "$componentName",""")
            appendLine("""  "dependencies": [""")
            allDeps.forEachIndexed { i, dep ->
                val comma = if (i < allDeps.size - 1) "," else ""
                appendLine("""    "$dep"$comma""")
            }
            appendLine("  ],")
            appendLine("""  "runtimeFunctions": [""")
            runtimeFuncs.forEachIndexed { i, func ->
                val comma = if (i < runtimeFuncs.size - 1) "," else ""
                appendLine("""    "$func"$comma""")
            }
            appendLine("  ]")
            appendLine("}")
        }
    }

    /**
     * Resolve transitive dependencies using a file dependency graph.
     * Starting from a set of direct dependencies, recursively includes
     * all files that those files depend on.
     *
     * @param directDeps The set of directly used .brs files
     * @param fileDepsGraph Map from each .brs file to its own dependencies
     * @return Complete set of all dependencies including transitive ones
     */
    private fun resolveTransitiveDependencies(
        directDeps: Set<String>,
        fileDepsGraph: Map<String, Set<String>>
    ): Set<String> {
        val allDeps = mutableSetOf<String>()
        val queue = ArrayDeque(directDeps)

        while (queue.isNotEmpty()) {
            val dep = queue.removeFirst()
            if (allDeps.add(dep)) {
                // Add transitive dependencies of this file
                fileDepsGraph[dep]?.forEach { transitiveDep ->
                    if (transitiveDep !in allDeps) {
                        queue.add(transitiveDep)
                    }
                }
            }
        }

        return allDeps
    }

    /**
     * Generate the XML content for a SceneGraph component.
     *
     * @param component The component info extracted from the class
     * @param context The backend context
     * @param dependencies Set of .brs file names this component depends on (for script imports)
     */
    private fun generateComponentXmlContent(
        component: BrsComponentInfo,
        context: BrsIrBackendContext,
        dependencies: Set<String> = emptySet()
    ): String {
        val builder = StringBuilder()

        builder.appendLine("""<?xml version="1.0" encoding="utf-8" ?>""")
        builder.appendLine("""<component name="${component.name}" extends="${component.extendsComponent}">""")

        // Generate interface section FIRST - fields must be defined before scripts run
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

        // Add script references AFTER interface so fields are defined before init() runs
        // Dependencies first (stdlib, etc.) so functions are available when component init() runs
        for (dep in dependencies.sorted()) {
            // Layout files go to component directory, others to source
            val depBaseName = dep.removeSuffix("Kt.brs")
            val uri = if (depBaseName.endsWith("_Layout")) {
                val possibleComponent = depBaseName.removeSuffix("_Layout")
                if (possibleComponent == component.name) {
                    "pkg:/components/${component.name}/$dep"
                } else {
                    "pkg:/source/$dep"
                }
            } else {
                "pkg:/source/$dep"
            }
            builder.appendLine("""    <script type="text/brightscript" uri="$uri" />""")
        }

        // Component's own BRS file LAST (can now call stdlib functions in init())
        builder.appendLine("""    <script type="text/brightscript" uri="pkg:/components/${component.name}/${component.name}Kt.brs" />""")

        // Add additional scripts
        for (script in component.additionalScripts) {
            builder.appendLine("""    <script type="text/brightscript" uri="$script" />""")
        }

        // Generate children section from @SGLayout DSL
        val layout = component.layout
        if (layout != null && layout.nodes.isNotEmpty()) {
            builder.appendLine("    <children>")
            for (child in layout.nodes) {
                generateNodeXml(child, builder, indent = "        ")
            }
            builder.appendLine("    </children>")
        }

        builder.appendLine("</component>")

        return builder.toString()
    }

    /**
     * Generate XML for a single node entry and its children.
     */
    private fun generateNodeXml(node: NodeEntryInfo, builder: StringBuilder, indent: String) {
        builder.append("$indent<${node.nodeType} id=\"${node.id}\"")

        // Add all attributes
        for ((key, value) in node.attributes) {
            builder.append(" $key=\"$value\"")
        }

        if (node.children.isEmpty()) {
            builder.appendLine("/>")
        } else {
            builder.appendLine(">")
            for (child in node.children) {
                generateNodeXml(child, builder, "$indent    ")
            }
            builder.appendLine("$indent</${node.nodeType}>")
        }
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

            // Create output directories
            val sourceDir = File(outputDir, "source")
            sourceDir.mkdirs()
            val componentsDir = File(outputDir, "components")

            // Get component names for routing BRS files
            val componentNames = result.componentXml.keys

            // Write .brs files - route component files to component directories
            for (output in result.outputs) {
                val fileName = File(output.outputFilePath).name
                val baseName = fileName.removeSuffix("Kt.brs")

                // Check for exact component match OR layout file for a component
                val componentName = when {
                    componentNames.contains(baseName) -> baseName
                    baseName.endsWith("_Layout") -> {
                        // Check if this is a layout file for a known component
                        val possibleComponent = baseName.removeSuffix("_Layout")
                        if (componentNames.contains(possibleComponent)) possibleComponent else null
                    }
                    else -> null
                }

                val outputFile = if (componentName != null) {
                    // Component BRS goes to components/<ComponentName>/
                    val componentDir = File(componentsDir, componentName)
                    componentDir.mkdirs()
                    File(componentDir, fileName)
                } else {
                    // Regular BRS goes to source/
                    File(sourceDir, fileName)
                }
                outputFile.writeText(output.sourceCode)
            }

            // Write function manifest for klib consumers
            if (result.functionManifest.isNotEmpty()) {
                val manifestFile = File(sourceDir, "function-manifest.json")
                manifestFile.writeText(buildFunctionManifestJson(result.functionManifest))
            }

            // Write component XML and deps.json files
            if (result.componentXml.isNotEmpty()) {
                componentsDir.mkdirs()
                for ((name, xml) in result.componentXml) {
                    // Create component subdirectory (e.g., components/ShelfView/)
                    val componentDir = File(componentsDir, name)
                    componentDir.mkdirs()

                    // Write XML file
                    val xmlFile = File(componentDir, "$name.xml")
                    xmlFile.writeText(xml)

                    // Write deps.json file if available
                    result.componentDepsJson[name]?.let { depsJson ->
                        val depsFile = File(componentDir, "$name.deps.json")
                        depsFile.writeText(depsJson)
                    }
                }
            }
        }

        /**
         * Build JSON content for the function manifest.
         */
        private fun buildFunctionManifestJson(manifest: Map<String, String>): String {
            val sb = StringBuilder()
            sb.appendLine("{")
            sb.appendLine("  \"version\": 1,")
            sb.appendLine("  \"functions\": {")

            val entries = manifest.entries.sortedBy { it.key }
            entries.forEachIndexed { index, (functionName, fileName) ->
                val comma = if (index < entries.size - 1) "," else ""
                // Escape any special characters in the function name
                val escapedName = functionName.replace("\\", "\\\\").replace("\"", "\\\"")
                sb.appendLine("    \"$escapedName\": \"$fileName\"$comma")
            }

            sb.appendLine("  }")
            sb.appendLine("}")
            return sb.toString()
        }
    }
}
