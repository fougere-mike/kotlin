/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.transformers.irToBrs

import org.jetbrains.kotlin.brs.backend.ast.*
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.ir.backend.brs.BrsIntrinsics
import org.jetbrains.kotlin.ir.backend.brs.lower.BrsCodeOutliningLowering
import org.jetbrains.kotlin.ir.backend.brs.lower.BrsInlineCallTransformer
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.backend.brs.BrsIrBackendContext
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.isFunction
import org.jetbrains.kotlin.ir.util.isInterface
import org.jetbrains.kotlin.ir.util.isNullable
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.symbols.IrValueSymbol
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid

/**
 * Transforms Kotlin IR to BrightScript AST.
 *
 * This is the core transformation that converts lowered Kotlin IR
 * into BrightScript-specific AST nodes that can then be rendered
 * to text.
 */
class IrToBrsTransformer(
    private val context: BrsIrBackendContext
) : IrElementVisitor<BrsNode?, Unit> {

    private val statementTransformer = IrStatementToBrsTransformer(this, context)
    private val expressionTransformer = IrExpressionToBrsTransformer(this, context)
    private val inlineCallTransformer = BrsInlineCallTransformer(context)

    // Track enum classes encountered during transformation for initialization
    private val enumClassNames = mutableListOf<String>()

    /**
     * Tracks captured variables when transforming a closure.
     * Maps IR variable symbols to their capture info (name and mutability).
     */
    data class CapturedVariable(
        val symbol: IrValueSymbol,
        val name: String,
        val isMutable: Boolean
    )

    /**
     * Current closure context for variable access rewriting.
     * When non-null, we're inside a closure and need to rewrite captured variable accesses.
     * Internal visibility so expression/statement transformers can access it.
     */
    internal var currentClosureContext: List<CapturedVariable>? = null

    /**
     * Temp variable substitution map for increment/decrement inlining.
     * When transforming increment blocks, we need to inline the temp variable's
     * initializer instead of outputting a reference to the temp var.
     */
    private val tempVarSubstitutions = mutableMapOf<IrValueSymbol, IrExpression>()

    fun pushTempVarSubstitution(symbol: IrValueSymbol, initializer: IrExpression) {
        tempVarSubstitutions[symbol] = initializer
    }

    fun popTempVarSubstitution(symbol: IrValueSymbol) {
        tempVarSubstitutions.remove(symbol)
    }

    fun getTempVarSubstitution(symbol: IrValueSymbol): IrExpression? {
        return tempVarSubstitutions[symbol]
    }

    /**
     * Detect variables captured by a function expression.
     * Returns a list of variables that are referenced but not declared within the function.
     */
    fun detectCapturedVariables(function: IrSimpleFunction): List<CapturedVariable> {
        val declaredSymbols = mutableSetOf<IrValueSymbol>()
        val referencedSymbols = mutableMapOf<IrValueSymbol, Boolean>() // symbol -> isMutated

        // Collect function parameters as declared
        function.valueParameters.forEach { declaredSymbols.add(it.symbol) }

        // Walk the function body to find declared and referenced variables
        function.body?.acceptVoid(object : IrElementVisitorVoid {
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }

            override fun visitVariable(declaration: IrVariable) {
                declaredSymbols.add(declaration.symbol)
                declaration.acceptChildrenVoid(this)
            }

            override fun visitGetValue(expression: IrGetValue) {
                val symbol = expression.symbol
                if (symbol !in declaredSymbols) {
                    // Only track variables and value parameters from outer scope
                    val owner = symbol.owner
                    if (owner is IrVariable || owner is IrValueParameter) {
                        if (symbol !in referencedSymbols) {
                            referencedSymbols[symbol] = false
                        }
                    }
                }
                expression.acceptChildrenVoid(this)
            }

            override fun visitSetValue(expression: IrSetValue) {
                val symbol = expression.symbol
                if (symbol !in declaredSymbols) {
                    // Mark as mutated
                    referencedSymbols[symbol] = true
                }
                expression.acceptChildrenVoid(this)
            }
        })

        // Build the captured variables list
        return referencedSymbols.map { (symbol, isMutated) ->
            val owner = symbol.owner
            val isMutable = when (owner) {
                is IrVariable -> owner.isVar || isMutated
                else -> isMutated
            }
            CapturedVariable(
                symbol = symbol,
                name = owner.name.asString(),
                isMutable = isMutable
            )
        }
    }

    /**
     * Check if a variable symbol is captured in the current closure context.
     */
    fun getCapturedVariable(symbol: IrValueSymbol): CapturedVariable? {
        return currentClosureContext?.find { it.symbol == symbol }
    }

    // ==================== Entry Points ====================

    /**
     * Transform an IR file to a BrightScript program.
     */
    fun transformFile(irFile: IrFile): BrsProgram {
        val declarations = mutableListOf<BrsDeclaration>()
        val statements = mutableListOf<BrsStatement>()

        // Clear tracked enums from any previous transformation
        enumClassNames.clear()

        for (declaration in irFile.declarations) {
            when (declaration) {
                is IrFunction -> {
                    transformFunction(declaration)?.let { declarations.add(it) }
                }
                is IrClass -> {
                    // Classes are expanded into functions and statements
                    // (enums are tracked during transformClassDeclarations)
                    transformClassDeclarations(declaration, declarations, statements)
                }
                is IrProperty -> {
                    // Top-level properties become variables or functions
                    transformProperty(declaration, declarations, statements)
                }
                else -> {
                    // Other declarations (type aliases, etc.) are typically not emitted
                }
            }
        }

        // Note: BrightScript doesn't support top-level statements outside functions,
        // so enum initialization is handled lazily via initEntries() calls in values()/valueOf()

        return BrsProgram(declarations, statements)
    }

    /**
     * Transform an IR class to its BrightScript representation.
     */
    fun transformClass(irClass: IrClass): List<BrsDeclaration> {
        val declarations = mutableListOf<BrsDeclaration>()
        val statements = mutableListOf<BrsStatement>()
        transformClassDeclarations(irClass, declarations, statements)
        return declarations
    }

    // ==================== Declarations ====================

    /**
     * Transform an IR function to a BrightScript function or sub.
     */
    fun transformFunction(irFunction: IrFunction): BrsDeclaration? {
        if (irFunction is IrSimpleFunction && irFunction.isFakeOverride) return null
        if (irFunction.isExternal) return null

        val name = context.getBrsName(irFunction)
        val parameters = irFunction.valueParameters.map { param ->
            BrsParameter(
                name = sanitizeParameterName(param.name.asString()),
                type = mapTypeToBrs(param.type),
                defaultValue = param.defaultValue?.expression?.let { transformExpression(it) }
            )
        }

        // Check if this function has @BrsInline - use parsed code instead of IR body
        val inlineInfo = context.inlineFunctionInfo[irFunction.symbol] as? BrsCodeOutliningLowering.BrsInlineInfo
        val body = if (inlineInfo != null) {
            // Use the parsed @BrsInline code
            BrsBlock(inlineInfo.parsedStatements.toMutableList())
        } else {
            irFunction.body?.let { transformBody(it) } ?: BrsBlock()
        }

        val returnType = mapTypeToBrs(irFunction.returnType)

        // Functions with Unit return type become subs
        return if (irFunction.returnType.isUnit() || irFunction.returnType.isNothing()) {
            BrsSub(name, parameters.toMutableList(), body)
        } else {
            BrsFunction(name, parameters.toMutableList(), returnType, body)
        }
    }

    /**
     * Transform class declarations into top-level functions and statements.
     */
    private fun transformClassDeclarations(
        irClass: IrClass,
        declarations: MutableList<BrsDeclaration>,
        statements: MutableList<BrsStatement>
    ) {
        // Handle object singletons specially
        if (irClass.kind == ClassKind.OBJECT) {
            transformObjectDeclaration(irClass, declarations, statements)
            return
        }

        // Handle enum classes specially
        if (irClass.kind == ClassKind.ENUM_CLASS) {
            enumClassNames.add(context.getBrsName(irClass))
            transformEnumDeclaration(irClass, declarations, statements)
            return
        }

        // Handle data classes - generate synthetic members
        if (irClass.isData) {
            transformDataClassDeclaration(irClass, declarations, statements)
            return
        }

        // Generate constructor function
        for (constructor in irClass.declarations.filterIsInstance<IrConstructor>()) {
            transformConstructor(irClass, constructor)?.let { declarations.add(it) }
        }

        // Generate member functions
        for (function in irClass.declarations.filterIsInstance<IrSimpleFunction>()) {
            if (!function.isFakeOverride) {
                transformFunction(function)?.let { declarations.add(it) }
            }
        }

        // Generate property accessors
        for (property in irClass.declarations.filterIsInstance<IrProperty>()) {
            transformProperty(property, declarations, statements)
        }

        // Process nested classes
        for (nested in irClass.declarations.filterIsInstance<IrClass>()) {
            transformClassDeclarations(nested, declarations, statements)
        }
    }

    /**
     * Transform an object declaration (singleton) to BrightScript.
     *
     * Generates:
     * 1. An instance variable: MySingleton_instance = invalid
     * 2. A getInstance function that lazily creates the singleton
     * 3. Member functions with proper naming
     */
    private fun transformObjectDeclaration(
        irClass: IrClass,
        declarations: MutableList<BrsDeclaration>,
        statements: MutableList<BrsStatement>
    ) {
        val className = context.getBrsName(irClass)
        val instanceVarName = "${className}_instance"

        // Note: BrightScript doesn't support top-level statements, so we use m. for global storage
        // Instance is lazily initialized in getInstance()

        // Generate the _create function (like regular class constructor)
        for (constructor in irClass.declarations.filterIsInstance<IrConstructor>()) {
            transformConstructor(irClass, constructor)?.let { declarations.add(it) }
        }

        // Generate getInstance function
        val getInstanceBody = mutableListOf<BrsStatement>()

        // if m.MySingleton_instance = invalid then
        //     m.MySingleton_instance = MySingleton_create()
        // end if
        getInstanceBody.add(
            BrsIf(
                condition = BrsBinaryOp(
                    BrsDotAccess(BrsIdentifier("m"), instanceVarName),
                    BrsBinaryOperator.EQ,
                    BrsInvalidLiteral()
                ),
                thenBranch = BrsBlock(mutableListOf(
                    BrsExpressionStatement(
                        BrsBinaryOp(
                            BrsDotAccess(BrsIdentifier("m"), instanceVarName),
                            BrsBinaryOperator.EQ,
                            BrsFunctionCall(BrsIdentifier("${className}_create"), mutableListOf())
                        )
                    )
                ))
            )
        )

        // return m.MySingleton_instance
        getInstanceBody.add(BrsReturn(BrsDotAccess(BrsIdentifier("m"), instanceVarName)))

        declarations.add(
            BrsFunction(
                name = "${className}_getInstance",
                parameters = mutableListOf(),
                returnType = BrsType.OBJECT,
                body = BrsBlock(getInstanceBody)
            )
        )

        // Generate member functions
        for (function in irClass.declarations.filterIsInstance<IrSimpleFunction>()) {
            if (!function.isFakeOverride) {
                transformFunction(function)?.let { declarations.add(it) }
            }
        }

        // Generate property accessors
        for (property in irClass.declarations.filterIsInstance<IrProperty>()) {
            transformProperty(property, declarations, statements)
        }

        // Process nested classes
        for (nested in irClass.declarations.filterIsInstance<IrClass>()) {
            transformClassDeclarations(nested, declarations, statements)
        }
    }

    /**
     * Transform an enum class declaration to BrightScript.
     *
     * Generates:
     * 1. Entry variables: Color_RED = invalid, Color_GREEN = invalid
     * 2. Entries initialized flag: Color_entriesInitialized = false
     * 3. Entry initializer function: Color_initEntries()
     * 4. Constructor function: Color_create(name, ordinal, ...)
     * 5. values() function returning array of all entries
     * 6. valueOf() function to get entry by name
     */
    private fun transformEnumDeclaration(
        irClass: IrClass,
        declarations: MutableList<BrsDeclaration>,
        statements: MutableList<BrsStatement>
    ) {
        val className = context.getBrsName(irClass)

        // Get all enum entries
        val enumEntries = irClass.declarations.filterIsInstance<IrEnumEntry>()

        // Note: BrightScript doesn't support top-level statements, so we use m.global
        // for enum entry storage. Variables are initialized lazily in initEntries().
        val initializedFlagName = "${className}_entriesInitialized"

        // 3. Generate the _create constructor function
        // Constructor takes name, ordinal, plus any custom parameters
        for (constructor in irClass.declarations.filterIsInstance<IrConstructor>()) {
            transformEnumConstructor(irClass, constructor)?.let { declarations.add(it) }
        }

        // 4. Generate initEntries sub (using m. prefix for global scope)
        val initEntriesBody = mutableListOf<BrsStatement>()

        // if m.Color_entriesInitialized then return
        initEntriesBody.add(
            BrsIf(
                condition = BrsDotAccess(BrsIdentifier("m"), initializedFlagName),
                thenBranch = BrsReturn(null)
            )
        )

        // m.Color_entriesInitialized = true
        initEntriesBody.add(
            BrsExpressionStatement(
                BrsBinaryOp(
                    BrsDotAccess(BrsIdentifier("m"), initializedFlagName),
                    BrsBinaryOperator.EQ,
                    BrsBooleanLiteral(true)
                )
            )
        )

        // Initialize each entry: m.Color_RED = Color_create("RED", 0, ...)
        enumEntries.forEachIndexed { ordinal, entry ->
            val entryVarName = "${className}_${entry.name.asString()}"
            val args = mutableListOf<BrsExpression>(
                BrsStringLiteral(entry.name.asString()),  // name
                BrsIntLiteral(ordinal)                      // ordinal
            )

            // Add initializer arguments if present
            entry.initializerExpression?.let { init ->
                // Extract arguments from the constructor call
                val initExpr = init.expression
                if (initExpr is IrEnumConstructorCall) {
                    for (i in 0 until initExpr.valueArgumentsCount) {
                        initExpr.getValueArgument(i)?.let { arg ->
                            args.add(transformExpression(arg))
                        }
                    }
                }
            }

            initEntriesBody.add(
                BrsExpressionStatement(
                    BrsBinaryOp(
                        BrsDotAccess(BrsIdentifier("m"), entryVarName),
                        BrsBinaryOperator.EQ,
                        BrsFunctionCall(BrsIdentifier("${className}_create"), args)
                    )
                )
            )
        }

        declarations.add(
            BrsSub(
                name = "${className}_initEntries",
                parameters = mutableListOf(),
                body = BrsBlock(initEntriesBody)
            )
        )

        // 5. Generate values() function
        val valuesBody = mutableListOf<BrsStatement>()

        // Color_initEntries()
        valuesBody.add(
            BrsExpressionStatement(
                BrsFunctionCall(BrsIdentifier("${className}_initEntries"), mutableListOf())
            )
        )

        // return [m.Color_RED, m.Color_GREEN, ...]
        val entryRefs = enumEntries.map { entry ->
            BrsDotAccess(BrsIdentifier("m"), "${className}_${entry.name.asString()}")
        }
        valuesBody.add(BrsReturn(BrsArrayLiteral(entryRefs.toMutableList())))

        declarations.add(
            BrsFunction(
                name = "${className}_values",
                parameters = mutableListOf(),
                returnType = BrsType.OBJECT,
                body = BrsBlock(valuesBody)
            )
        )

        // 6. Generate valueOf(name) function
        val valueOfBody = mutableListOf<BrsStatement>()

        // Color_initEntries()
        valueOfBody.add(
            BrsExpressionStatement(
                BrsFunctionCall(BrsIdentifier("${className}_initEntries"), mutableListOf())
            )
        )

        // Generate if-else chain for each entry
        var currentIf: BrsIf? = null
        var firstIf: BrsIf? = null
        for (entry in enumEntries) {
            val entryName = entry.name.asString()
            val entryVarName = "${className}_$entryName"

            val newIf = BrsIf(
                condition = BrsBinaryOp(
                    BrsIdentifier("name"),
                    BrsBinaryOperator.EQ,
                    BrsStringLiteral(entryName)
                ),
                thenBranch = BrsReturn(BrsDotAccess(BrsIdentifier("m"), entryVarName))
            )

            if (firstIf == null) {
                firstIf = newIf
            }
            if (currentIf != null) {
                currentIf.elseBranch = newIf
            }
            currentIf = newIf
        }

        // Add else branch that returns invalid
        currentIf?.elseBranch = BrsReturn(BrsInvalidLiteral())

        firstIf?.let { valueOfBody.add(it) }

        declarations.add(
            BrsFunction(
                name = "${className}_valueOf",
                parameters = mutableListOf(BrsParameter("name", BrsType.STRING, null)),
                returnType = BrsType.OBJECT,
                body = BrsBlock(valueOfBody)
            )
        )

        // Generate member functions (if any), skipping synthetic values/valueOf
        for (function in irClass.declarations.filterIsInstance<IrSimpleFunction>()) {
            val functionName = function.name.asString()
            if (!function.isFakeOverride && functionName != "values" && functionName != "valueOf") {
                transformFunction(function)?.let { declarations.add(it) }
            }
        }
    }

    /**
     * Transform an enum constructor to a BrightScript function.
     * Adds name and ordinal parameters before any custom parameters.
     */
    private fun transformEnumConstructor(irClass: IrClass, constructor: IrConstructor): BrsFunction? {
        val className = context.getBrsName(irClass)
        val name = "${className}_create"

        // Start with name and ordinal parameters
        val parameters = mutableListOf(
            BrsParameter("__name", BrsType.STRING, null),
            BrsParameter("__ordinal", BrsType.INTEGER, null)
        )

        // Add constructor parameters
        constructor.valueParameters.forEach { param ->
            parameters.add(
                BrsParameter(
                    name = param.name.asString(),
                    type = mapTypeToBrs(param.type),
                    defaultValue = param.defaultValue?.expression?.let { transformExpression(it) }
                )
            )
        }

        // Build constructor body
        val bodyStatements = mutableListOf<BrsStatement>()

        // this = {}
        bodyStatements.add(
            BrsVariable(
                name = "this",
                type = BrsType.OBJECT,
                initializer = BrsAALiteral(mutableListOf())
            )
        )

        // this.__type = "ClassName"
        bodyStatements.add(
            BrsExpressionStatement(
                BrsBinaryOp(
                    BrsDotAccess(BrsIdentifier("this"), "__type"),
                    BrsBinaryOperator.EQ,
                    BrsStringLiteral(className)
                )
            )
        )

        // this.name = __name
        bodyStatements.add(
            BrsExpressionStatement(
                BrsBinaryOp(
                    BrsDotAccess(BrsIdentifier("this"), "name"),
                    BrsBinaryOperator.EQ,
                    BrsIdentifier("__name")
                )
            )
        )

        // this.ordinal = __ordinal
        bodyStatements.add(
            BrsExpressionStatement(
                BrsBinaryOp(
                    BrsDotAccess(BrsIdentifier("this"), "ordinal"),
                    BrsBinaryOperator.EQ,
                    BrsIdentifier("__ordinal")
                )
            )
        )

        // Add custom properties from constructor parameters
        constructor.valueParameters.forEach { param ->
            bodyStatements.add(
                BrsExpressionStatement(
                    BrsBinaryOp(
                        BrsDotAccess(BrsIdentifier("this"), param.name.asString()),
                        BrsBinaryOperator.EQ,
                        BrsIdentifier(param.name.asString())
                    )
                )
            )
        }

        // Transform constructor body statements if present
        (constructor.body as? IrBlockBody)?.statements?.forEach { stmt ->
            when (stmt) {
                is IrDelegatingConstructorCall -> {
                    // Skip delegating calls for enum constructors
                }
                is IrInstanceInitializerCall -> {
                    // Skip instance initializer calls
                }
                else -> {
                    transformStatement(stmt)?.let { bodyStatements.add(it) }
                }
            }
        }

        // return this
        bodyStatements.add(BrsReturn(BrsIdentifier("this")))

        return BrsFunction(
            name = name,
            parameters = parameters,
            returnType = BrsType.OBJECT,
            body = BrsBlock(bodyStatements)
        )
    }

    /**
     * Transform a data class declaration to BrightScript.
     *
     * Generates:
     * 1. Constructor function: Person_create(name, age)
     * 2. equals method: Person_equals(other)
     * 3. hashCode method: Person_hashCode()
     * 4. toString method: Person_toString()
     * 5. copy method: Person_copy(name, age)
     * 6. componentN methods: Person_component1(), Person_component2(), etc.
     */
    private fun transformDataClassDeclaration(
        irClass: IrClass,
        declarations: MutableList<BrsDeclaration>,
        statements: MutableList<BrsStatement>
    ) {
        val className = context.getBrsName(irClass)

        // Get primary constructor parameters (these are the data class properties)
        val primaryConstructor = irClass.declarations.filterIsInstance<IrConstructor>().firstOrNull()
        val properties = primaryConstructor?.valueParameters ?: emptyList()

        // 1. Generate constructor function
        for (constructor in irClass.declarations.filterIsInstance<IrConstructor>()) {
            transformDataClassConstructor(irClass, constructor, properties)?.let { declarations.add(it) }
        }

        // 2. Generate equals method
        declarations.add(generateDataClassEquals(className, properties))

        // 3. Generate hashCode method
        declarations.add(generateDataClassHashCode(className, properties))

        // 4. Generate toString method
        declarations.add(generateDataClassToString(className, properties))

        // 5. Generate copy method
        declarations.add(generateDataClassCopy(className, properties))

        // 6. Generate componentN methods
        properties.forEachIndexed { index, param ->
            declarations.add(generateDataClassComponent(className, param, index + 1))
        }

        // Generate any additional member functions (not synthetic data class methods)
        for (function in irClass.declarations.filterIsInstance<IrSimpleFunction>()) {
            val name = function.name.asString()
            // Skip synthetic data class methods
            if (!function.isFakeOverride &&
                name != "equals" && name != "hashCode" && name != "toString" &&
                name != "copy" && !name.startsWith("component")
            ) {
                transformFunction(function)?.let { declarations.add(it) }
            }
        }
    }

    /**
     * Transform a data class constructor.
     */
    private fun transformDataClassConstructor(
        irClass: IrClass,
        constructor: IrConstructor,
        properties: List<IrValueParameter>
    ): BrsFunction? {
        val className = context.getBrsName(irClass)
        val name = "${className}_create"

        val parameters = constructor.valueParameters.map { param ->
            BrsParameter(
                name = param.name.asString(),
                type = mapTypeToBrs(param.type),
                defaultValue = param.defaultValue?.expression?.let { transformExpression(it) }
            )
        }

        val bodyStatements = mutableListOf<BrsStatement>()

        // this = {}
        bodyStatements.add(
            BrsVariable(
                name = "this",
                type = BrsType.OBJECT,
                initializer = BrsAALiteral(mutableListOf())
            )
        )

        // this.__type = "ClassName"
        bodyStatements.add(
            BrsExpressionStatement(
                BrsBinaryOp(
                    BrsDotAccess(BrsIdentifier("this"), "__type"),
                    BrsBinaryOperator.EQ,
                    BrsStringLiteral(className)
                )
            )
        )

        // this.__proto = ["ClassName"]
        bodyStatements.add(
            BrsExpressionStatement(
                BrsBinaryOp(
                    BrsDotAccess(BrsIdentifier("this"), "__proto"),
                    BrsBinaryOperator.EQ,
                    BrsArrayLiteral(mutableListOf(BrsStringLiteral(className)))
                )
            )
        )

        // Add all properties: this.name = name, this.age = age
        constructor.valueParameters.forEach { param ->
            bodyStatements.add(
                BrsExpressionStatement(
                    BrsBinaryOp(
                        BrsDotAccess(BrsIdentifier("this"), param.name.asString()),
                        BrsBinaryOperator.EQ,
                        BrsIdentifier(param.name.asString())
                    )
                )
            )
        }

        // return this
        bodyStatements.add(BrsReturn(BrsIdentifier("this")))

        return BrsFunction(
            name = name,
            parameters = parameters.toMutableList(),
            returnType = BrsType.OBJECT,
            body = BrsBlock(bodyStatements)
        )
    }

    /**
     * Generate equals method for data class.
     */
    private fun generateDataClassEquals(className: String, properties: List<IrValueParameter>): BrsFunction {
        val bodyStatements = mutableListOf<BrsStatement>()

        // if Type(other) <> "roAssociativeArray" then return false
        bodyStatements.add(
            BrsIf(
                condition = BrsBinaryOp(
                    BrsFunctionCall(BrsIdentifier("Type"), mutableListOf(BrsIdentifier("other"))),
                    BrsBinaryOperator.NE,
                    BrsStringLiteral("roAssociativeArray")
                ),
                thenBranch = BrsReturn(BrsBooleanLiteral(false))
            )
        )

        // if other.__type <> "ClassName" then return false
        bodyStatements.add(
            BrsIf(
                condition = BrsBinaryOp(
                    BrsDotAccess(BrsIdentifier("other"), "__type"),
                    BrsBinaryOperator.NE,
                    BrsStringLiteral(className)
                ),
                thenBranch = BrsReturn(BrsBooleanLiteral(false))
            )
        )

        // Compare each property: if m.name <> other.name then return false
        properties.forEach { param ->
            val propName = param.name.asString()
            bodyStatements.add(
                BrsIf(
                    condition = BrsBinaryOp(
                        BrsDotAccess(BrsIdentifier("m"), propName),
                        BrsBinaryOperator.NE,
                        BrsDotAccess(BrsIdentifier("other"), propName)
                    ),
                    thenBranch = BrsReturn(BrsBooleanLiteral(false))
                )
            )
        }

        // return true
        bodyStatements.add(BrsReturn(BrsBooleanLiteral(true)))

        return BrsFunction(
            name = "${className}_equals",
            parameters = mutableListOf(BrsParameter("other", BrsType.OBJECT, null)),
            returnType = BrsType.BOOLEAN,
            body = BrsBlock(bodyStatements)
        )
    }

    /**
     * Generate hashCode method for data class.
     */
    private fun generateDataClassHashCode(className: String, properties: List<IrValueParameter>): BrsFunction {
        val bodyStatements = mutableListOf<BrsStatement>()

        // result = 1
        bodyStatements.add(
            BrsVariable("result", BrsType.INTEGER, BrsIntLiteral(1))
        )

        // For each property: result = result * 31 + hash(property)
        // Note: BrightScript doesn't have a built-in hash function, so we use simple calculation
        properties.forEach { param ->
            val propName = param.name.asString()
            // result = result * 31 + (m.property if type is number, or Len(m.property) for strings)
            bodyStatements.add(
                BrsExpressionStatement(
                    BrsBinaryOp(
                        BrsIdentifier("result"),
                        BrsBinaryOperator.EQ,
                        BrsBinaryOp(
                            BrsBinaryOp(
                                BrsIdentifier("result"),
                                BrsBinaryOperator.MUL,
                                BrsIntLiteral(31)
                            ),
                            BrsBinaryOperator.ADD,
                            BrsDotAccess(BrsIdentifier("m"), propName)
                        )
                    )
                )
            )
        }

        // return result
        bodyStatements.add(BrsReturn(BrsIdentifier("result")))

        return BrsFunction(
            name = "${className}_hashCode",
            parameters = mutableListOf(),
            returnType = BrsType.INTEGER,
            body = BrsBlock(bodyStatements)
        )
    }

    /**
     * Generate toString method for data class.
     */
    private fun generateDataClassToString(className: String, properties: List<IrValueParameter>): BrsFunction {
        val bodyStatements = mutableListOf<BrsStatement>()

        // Build: "ClassName(prop1=" + m.prop1 + ", prop2=" + m.prop2 + ")"
        val propStrings = properties.mapIndexed { index, param ->
            val propName = param.name.asString()
            val prefix = if (index == 0) "$className($propName=" else ", $propName="
            listOf(
                BrsStringLiteral(prefix),
                BrsDotAccess(BrsIdentifier("m"), propName)
            )
        }.flatten()

        // Concatenate all parts
        val concatenation = if (propStrings.isEmpty()) {
            BrsStringLiteral("$className()")
        } else {
            var result: BrsExpression = propStrings.first()
            propStrings.drop(1).forEach { part ->
                result = BrsBinaryOp(result, BrsBinaryOperator.ADD, part)
            }
            BrsBinaryOp(result, BrsBinaryOperator.ADD, BrsStringLiteral(")"))
        }

        bodyStatements.add(BrsReturn(concatenation))

        return BrsFunction(
            name = "${className}_toString",
            parameters = mutableListOf(),
            returnType = BrsType.STRING,
            body = BrsBlock(bodyStatements)
        )
    }

    /**
     * Generate copy method for data class.
     *
     * BrightScript doesn't support expressions like `m.name` as default parameter values,
     * so we use `invalid` as the default and add runtime checks to substitute the current value.
     */
    private fun generateDataClassCopy(className: String, properties: List<IrValueParameter>): BrsFunction {
        val bodyStatements = mutableListOf<BrsStatement>()

        // Parameters with invalid as default (BrightScript doesn't allow m.property as default)
        val parameters = properties.map { param ->
            val propName = param.name.asString()
            BrsParameter(
                name = propName,
                type = null,  // Untyped to allow invalid
                defaultValue = BrsIdentifier("invalid")
            )
        }

        // Add runtime checks: if param = invalid then param = m.param
        properties.forEach { param ->
            val propName = param.name.asString()
            bodyStatements.add(
                BrsIf(
                    condition = BrsBinaryOp(
                        BrsIdentifier(propName),
                        BrsBinaryOperator.EQ,
                        BrsIdentifier("invalid")
                    ),
                    thenBranch = BrsExpressionStatement(
                        BrsBinaryOp(
                            BrsIdentifier(propName),
                            BrsBinaryOperator.EQ,
                            BrsDotAccess(BrsIdentifier("m"), propName)
                        )
                    ),
                    elseBranch = null
                )
            )
        }

        // return ClassName_create(name, age, ...)
        val args = properties.map { param ->
            BrsIdentifier(param.name.asString())
        }
        bodyStatements.add(
            BrsReturn(BrsFunctionCall(BrsIdentifier("${className}_create"), args.toMutableList()))
        )

        return BrsFunction(
            name = "${className}_copy",
            parameters = parameters.toMutableList(),
            returnType = BrsType.OBJECT,
            body = BrsBlock(bodyStatements)
        )
    }

    /**
     * Generate componentN method for data class.
     */
    private fun generateDataClassComponent(
        className: String,
        param: IrValueParameter,
        index: Int
    ): BrsFunction {
        val bodyStatements = mutableListOf<BrsStatement>()

        // return m.propertyName
        bodyStatements.add(
            BrsReturn(BrsDotAccess(BrsIdentifier("m"), param.name.asString()))
        )

        return BrsFunction(
            name = "${className}_component$index",
            parameters = mutableListOf(),
            returnType = mapTypeToBrs(param.type),
            body = BrsBlock(bodyStatements)
        )
    }

    /**
     * Transform a constructor to a BrightScript function.
     *
     * Classes are transformed to constructor functions that return associative arrays.
     * Inheritance is handled via prototype chain pattern:
     * - Child calls parent constructor first
     * - Parent methods stored in `_super` for super calls
     * - `__type` tracks class name, `__proto` tracks inheritance chain
     */
    private fun transformConstructor(irClass: IrClass, constructor: IrConstructor): BrsFunction? {
        // Use mangled constructor name to support overloading
        val name = context.getBrsName(constructor)
        // Class name is used for __type and __proto metadata (not mangled)
        val className = context.getBrsName(irClass)

        val parametersList = mutableListOf<BrsParameter>()

        // For inner classes, add $outer parameter first
        if (irClass.isInner) {
            parametersList.add(BrsParameter("\$outer", BrsType.OBJECT))
        }

        // Add regular constructor parameters
        parametersList.addAll(constructor.valueParameters.map { param ->
            BrsParameter(
                name = param.name.asString(),
                type = mapTypeToBrs(param.type),
                defaultValue = param.defaultValue?.expression?.let { transformExpression(it) }
            )
        })

        val parameters = parametersList

        // Build constructor body
        val bodyStatements = mutableListOf<BrsStatement>()

        // Check if there's a superclass (not Any)
        val superClass = irClass.superTypes
            .mapNotNull { it.classOrNull?.owner }
            .firstOrNull { !it.isInterface && it.name.asString() != "Any" }

        if (superClass != null) {
            // Find the delegating constructor call to get the super constructor
            val delegatingCall = findDelegatingConstructorCall(constructor)
            val superConstructor = delegatingCall?.symbol?.owner

            // Call parent constructor: this = ParentClass_create_ParamTypes_k$(...)
            val superConstructorName = if (superConstructor != null) {
                context.getBrsName(superConstructor)
            } else {
                // Fallback: use class name + _create for no-arg constructor
                "${context.getBrsName(superClass)}_create"
            }
            val superConstructorCall = BrsFunctionCall(
                BrsIdentifier(superConstructorName),
                getSuperConstructorArgs(constructor)
            )
            bodyStatements.add(
                BrsVariable(name = "this", initializer = superConstructorCall)
            )

            // Store parent methods for super calls: this._super = {}
            bodyStatements.add(
                BrsExpressionStatement(
                    BrsBinaryOp(
                        BrsDotAccess(BrsIdentifier("this"), "_super"),
                        BrsBinaryOperator.EQ,
                        BrsAALiteral()
                    )
                )
            )

            // Copy overridden methods to _super before overriding
            for (function in irClass.declarations.filterIsInstance<IrSimpleFunction>()) {
                if (!function.isFakeOverride && function.overriddenSymbols.isNotEmpty()) {
                    val methodName = function.name.asString()
                    bodyStatements.add(
                        BrsExpressionStatement(
                            BrsBinaryOp(
                                BrsDotAccess(
                                    BrsDotAccess(BrsIdentifier("this"), "_super"),
                                    methodName
                                ),
                                BrsBinaryOperator.EQ,
                                BrsDotAccess(BrsIdentifier("this"), methodName)
                            )
                        )
                    )
                }
            }

            // Update __proto chain
            bodyStatements.add(
                BrsExpressionStatement(
                    BrsBinaryOp(
                        BrsDotAccess(BrsIdentifier("this"), "__proto"),
                        BrsBinaryOperator.EQ,
                        BrsArrayLiteral(mutableListOf(
                            BrsStringLiteral(className),
                            BrsDotAccess(BrsIdentifier("this"), "__proto")
                        ))
                    )
                )
            )
        } else {
            // No superclass - create new object
            bodyStatements.add(
                BrsVariable(name = "this", initializer = BrsAALiteral())
            )

            // Set type info
            bodyStatements.add(
                BrsExpressionStatement(
                    BrsBinaryOp(
                        BrsDotAccess(BrsIdentifier("this"), "__type"),
                        BrsBinaryOperator.EQ,
                        BrsStringLiteral(className)
                    )
                )
            )

            // Initialize __proto chain with just this class
            bodyStatements.add(
                BrsExpressionStatement(
                    BrsBinaryOp(
                        BrsDotAccess(BrsIdentifier("this"), "__proto"),
                        BrsBinaryOperator.EQ,
                        BrsArrayLiteral(mutableListOf(BrsStringLiteral(className)))
                    )
                )
            )
        }

        // Update __type to current class for inherited classes
        // (base classes already set __type above in the else branch)
        if (superClass != null) {
            bodyStatements.add(
                BrsExpressionStatement(
                    BrsBinaryOp(
                        BrsDotAccess(BrsIdentifier("this"), "__type"),
                        BrsBinaryOperator.EQ,
                        BrsStringLiteral(className)
                    )
                )
            )
        }

        // For inner classes, store the outer reference
        if (irClass.isInner) {
            bodyStatements.add(
                BrsExpressionStatement(
                    BrsBinaryOp(
                        BrsDotAccess(BrsIdentifier("this"), "\$outer"),
                        BrsBinaryOperator.EQ,
                        BrsIdentifier("\$outer")
                    )
                )
            )
        }

        // Initialize fields (direct field declarations)
        val initializedFields = mutableSetOf<String>()
        for (field in irClass.declarations.filterIsInstance<IrField>()) {
            field.initializer?.expression?.let { initializer ->
                initializedFields.add(field.name.asString())
                bodyStatements.add(
                    BrsExpressionStatement(
                        BrsBinaryOp(
                            BrsDotAccess(BrsIdentifier("this"), field.name.asString()),
                            BrsBinaryOperator.EQ,
                            transformExpression(initializer)
                        )
                    )
                )
            }
        }

        // Also initialize property backing fields (may not be direct members of declarations)
        for (property in irClass.declarations.filterIsInstance<IrProperty>()) {
            val fieldName = property.name.asString()
            if (fieldName !in initializedFields) {
                property.backingField?.initializer?.expression?.let { initializer ->
                    bodyStatements.add(
                        BrsExpressionStatement(
                            BrsBinaryOp(
                                BrsDotAccess(BrsIdentifier("this"), fieldName),
                                BrsBinaryOperator.EQ,
                                transformExpression(initializer)
                            )
                        )
                    )
                }
            }
        }

        // Add methods to the instance
        for (function in irClass.declarations.filterIsInstance<IrSimpleFunction>()) {
            if (!function.isFakeOverride && !function.isExternal) {
                val methodName = function.name.asString()
                val fullMethodName = "${className}_${methodName}"
                bodyStatements.add(
                    BrsExpressionStatement(
                        BrsBinaryOp(
                            BrsDotAccess(BrsIdentifier("this"), methodName),
                            BrsBinaryOperator.EQ,
                            BrsIdentifier(fullMethodName)
                        )
                    )
                )
            }
        }

        // Execute constructor body (handles property initialization from parameters, etc.)
        constructor.body?.let { body ->
            val transformed = transformBody(body)
            // Filter out delegating constructor calls since we handle them above
            val filteredStatements = transformed.statements.filter { stmt ->
                !(stmt is BrsExpressionStatement && isDelegatingConstructorCall(stmt))
            }
            bodyStatements.addAll(filteredStatements)
        }

        // Return the constructed object
        bodyStatements.add(BrsReturn(BrsIdentifier("this")))

        return BrsFunction(
            name,
            parameters.toMutableList(),
            BrsType.OBJECT,
            BrsBlock(bodyStatements)
        )
    }

    /**
     * Find the delegating constructor call (super() or this()) in a constructor body.
     */
    private fun findDelegatingConstructorCall(constructor: IrConstructor): IrDelegatingConstructorCall? {
        return constructor.body?.let { body ->
            when (body) {
                is IrBlockBody -> body.statements.filterIsInstance<IrDelegatingConstructorCall>().firstOrNull()
                else -> null
            }
        }
    }

    /**
     * Get arguments for super constructor call from the delegating constructor call in the body.
     */
    private fun getSuperConstructorArgs(constructor: IrConstructor): MutableList<BrsExpression> {
        val delegatingCall = findDelegatingConstructorCall(constructor)

        return if (delegatingCall != null) {
            (0 until delegatingCall.valueArgumentsCount).mapNotNull { i ->
                delegatingCall.getValueArgument(i)?.let { transformExpression(it) }
            }.toMutableList()
        } else {
            mutableListOf()
        }
    }

    /**
     * Check if a statement is a delegating constructor call (super() or this()).
     * These calls generate BrsFunctionCall to ParentClass_create() functions.
     */
    private fun isDelegatingConstructorCall(stmt: BrsStatement): Boolean {
        if (stmt !is BrsExpressionStatement) return false
        val expr = stmt.expression
        if (expr !is BrsFunctionCall) return false
        val target = expr.target
        if (target !is BrsIdentifier) return false
        return target.name.endsWith("_create")
    }

    /**
     * Transform a property to BrightScript representation.
     */
    private fun transformProperty(
        property: IrProperty,
        declarations: MutableList<BrsDeclaration>,
        statements: MutableList<BrsStatement>
    ) {
        // Generate getter function if present
        property.getter?.let { getter ->
            if (!getter.isFakeOverride) {
                transformFunction(getter)?.let { declarations.add(it) }
            }
        }

        // Generate setter function if present
        property.setter?.let { setter ->
            if (!setter.isFakeOverride) {
                transformFunction(setter)?.let { declarations.add(it) }
            }
        }

        // For top-level properties with backing fields, generate initialization
        property.backingField?.let { field ->
            if (field.parent is IrFile) {
                field.initializer?.expression?.let { initializer ->
                    statements.add(
                        BrsVariable(
                            name = property.name.asString(),
                            type = mapTypeToBrs(field.type),
                            initializer = transformExpression(initializer)
                        )
                    )
                }
            }
        }
    }

    // ==================== Body and Statements ====================

    /**
     * Transform an IR body to a BrightScript block.
     */
    fun transformBody(body: IrBody): BrsBlock {
        return when (body) {
            is IrBlockBody -> {
                val statements = body.statements.mapNotNull { statement ->
                    transformStatement(statement)
                }
                BrsBlock(statements.toMutableList())
            }
            is IrExpressionBody -> {
                BrsBlock(mutableListOf(BrsReturn(transformExpression(body.expression))))
            }
            is IrSyntheticBody -> {
                BrsBlock()
            }
        }
    }

    /**
     * Transform an IR statement to a BrightScript statement.
     *
     * Note: Some IR "statements" are actually expressions (like IrSetField).
     * When these appear in a statement position, we wrap them in BrsExpressionStatement.
     */
    fun transformStatement(statement: IrStatement): BrsStatement? {
        // First try the statement transformer
        val result = statement.accept(statementTransformer, Unit)
        if (result != null) return result

        // Skip constructor-related IR nodes - handled during class/enum transformation
        if (statement is IrDelegatingConstructorCall ||
            statement is IrInstanceInitializerCall ||
            statement is IrEnumConstructorCall) {
            return null
        }

        // If the statement is actually an expression, wrap it in an expression statement
        if (statement is IrExpression) {
            return BrsExpressionStatement(transformExpression(statement))
        }

        return null
    }

    // ==================== Expressions ====================

    /**
     * Transform an IR expression to a BrightScript expression.
     */
    fun transformExpression(expression: IrExpression): BrsExpression {
        return expression.accept(expressionTransformer, Unit)
    }

    // ==================== Helpers ====================

    /**
     * Sanitize property accessor names for BrightScript.
     * Kotlin IR uses names like <get-foo> and <set-foo> for property accessors.
     */
    fun sanitizeMethodName(name: String): String {
        return when {
            name.startsWith("<get-") && name.endsWith(">") ->
                "get_" + name.removePrefix("<get-").removeSuffix(">")
            name.startsWith("<set-") && name.endsWith(">") ->
                "set_" + name.removePrefix("<set-").removeSuffix(">")
            else -> name
        }
    }

    /**
     * Sanitize parameter names for BrightScript.
     * Kotlin IR uses special names like <set-?> for setter parameters.
     */
    fun sanitizeParameterName(name: String): String {
        return when {
            // Setter parameter: <set-?> -> value
            name.startsWith("<set-") && name.endsWith(">") -> "value"
            // Any other special name: strip angle brackets
            name.startsWith("<") && name.endsWith(">") ->
                name.removePrefix("<").removeSuffix(">").replace("-", "_")
            else -> name
        }
    }

    // ==================== Type Mapping ====================

    /**
     * Map a Kotlin IR type to a BrightScript type.
     */
    fun mapTypeToBrs(type: IrType): BrsType? {
        return when {
            type.isInt() || type.isShort() || type.isByte() -> BrsType.INTEGER
            type.isLong() -> BrsType.LONG_INTEGER
            type.isFloat() -> BrsType.FLOAT
            type.isDouble() -> BrsType.DOUBLE
            type.isBoolean() -> BrsType.BOOLEAN
            type.isString() -> BrsType.STRING
            type.isUnit() -> BrsType.VOID
            type.isNothing() -> BrsType.VOID
            type.isNullable() -> BrsType.DYNAMIC
            type.isFunction() -> BrsType.FUNCTION
            else -> BrsType.OBJECT
        }
    }

    // ==================== Visitor Implementation ====================

    override fun visitElement(element: IrElement, data: Unit): BrsNode? {
        // Default implementation - should be handled by specialized transformers
        return null
    }
}

/**
 * Transforms IR statements to BrightScript statements.
 */
class IrStatementToBrsTransformer(
    private val parent: IrToBrsTransformer,
    private val context: BrsIrBackendContext
) : IrElementVisitor<BrsStatement?, Unit> {

    override fun visitElement(element: IrElement, data: Unit): BrsStatement? = null

    override fun visitVariable(declaration: IrVariable, data: Unit): BrsStatement {
        val initializer = declaration.initializer

        // Handle block initializers specially - BrightScript doesn't have block expressions
        // so we need to flatten the block's statements before the variable assignment
        if (initializer is IrBlock && initializer.statements.size > 1) {
            val statements = mutableListOf<BrsStatement>()

            // Flatten all statements from the block recursively
            flattenBlockStatements(initializer.statements, statements, data)

            // The last added statement should be the variable assignment
            // Replace the last expression statement with the actual variable declaration
            if (statements.isNotEmpty()) {
                val last = statements.removeLast()
                val lastExpr = when (last) {
                    is BrsExpressionStatement -> last.expression
                    is BrsVariable -> last.initializer ?: BrsInvalidLiteral()
                    else -> BrsInvalidLiteral()
                }
                statements.add(BrsVariable(
                    name = declaration.name.asString(),
                    type = parent.mapTypeToBrs(declaration.type),
                    initializer = lastExpr
                ))
            } else {
                statements.add(BrsVariable(
                    name = declaration.name.asString(),
                    type = parent.mapTypeToBrs(declaration.type),
                    initializer = BrsInvalidLiteral()
                ))
            }

            return BrsBlock(statements)
        }

        // Simple case - no block or single-expression block
        return BrsVariable(
            name = declaration.name.asString(),
            type = parent.mapTypeToBrs(declaration.type),
            initializer = initializer?.let { parent.transformExpression(it) }
        )
    }

    /**
     * Recursively flatten statements from an IrBlock, handling nested blocks.
     * The last statement in each block is treated as an expression value.
     */
    private fun flattenBlockStatements(
        stmts: List<IrStatement>,
        output: MutableList<BrsStatement>,
        data: Unit
    ) {
        for (i in stmts.indices) {
            val stmt = stmts[i]
            val isLast = i == stmts.lastIndex

            when {
                stmt is IrBlock && stmt.statements.isNotEmpty() -> {
                    // Recursively flatten nested blocks
                    flattenBlockStatements(stmt.statements, output, data)
                }
                stmt is IrVariable -> {
                    output.add(visitVariable(stmt, data))
                }
                stmt is IrWhen -> {
                    // When statement - transform using statement transformer
                    parent.transformStatement(stmt)?.let { output.add(it) }
                }
                stmt is IrExpression -> {
                    if (isLast) {
                        // Last expression is the block's value - keep as expression
                        val expr = parent.transformExpression(stmt)
                        output.add(BrsExpressionStatement(expr))
                    } else {
                        // Non-last expression - transform as statement
                        val expr = parent.transformExpression(stmt)
                        output.add(BrsExpressionStatement(expr))
                    }
                }
                else -> {
                    parent.transformStatement(stmt)?.let { output.add(it) }
                }
            }
        }
    }

    /**
     * Handle local function declarations.
     *
     * Local functions are transformed into closure objects when they capture variables
     * from the outer scope. BrightScript anonymous functions cannot access outer scope
     * variables, so we create an object with captured variable fields and an invoke method.
     */
    override fun visitFunction(declaration: IrFunction, data: Unit): BrsStatement {
        if (declaration is IrSimpleFunction && declaration.isFakeOverride) return BrsEmpty()

        val name = declaration.name.asString()
        val parameters = declaration.valueParameters.map { param ->
            BrsParameter(
                name = param.name.asString(),
                type = parent.mapTypeToBrs(param.type)
            )
        }

        val returnType = parent.mapTypeToBrs(declaration.returnType)

        // For subs (void return), use VOID return type
        val effectiveReturnType = if (declaration.returnType.isUnit() || declaration.returnType.isNothing()) {
            BrsType.VOID
        } else {
            returnType
        }

        // Detect captured variables for local functions
        val capturedVars = if (declaration is IrSimpleFunction) {
            parent.detectCapturedVariables(declaration)
        } else {
            emptyList()
        }

        // Save previous closure context
        val previousContext = parent.currentClosureContext

        // Set closure context for body transformation (if there are captures)
        if (capturedVars.isNotEmpty()) {
            parent.currentClosureContext = capturedVars
        }

        // Transform body with closure context active
        val body = declaration.body?.let { parent.transformBody(it) } ?: BrsBlock()

        // Restore previous context
        parent.currentClosureContext = previousContext

        // Build closure object for local function (always, for consistent .invoke() usage)
        val entries = mutableListOf<BrsAAEntry>()

        for (capturedVar in capturedVars) {
            val varValue = BrsIdentifier(capturedVar.name)
            val fieldValue = if (capturedVar.isMutable) {
                // Wrap mutable captures in { value: x } for mutation to propagate
                BrsAALiteral(mutableListOf(BrsAAEntry("value", varValue)))
            } else {
                // Read-only captures can be stored directly
                varValue
            }
            entries.add(BrsAAEntry(capturedVar.name, fieldValue))
        }

        // Add the invoke method
        val invokeFunction = BrsAnonymousFunction(parameters.toMutableList(), effectiveReturnType, body)
        entries.add(BrsAAEntry("invoke", invokeFunction))

        val closureObject = BrsAALiteral(entries)

        return BrsVariable(
            name = name,
            type = BrsType.OBJECT,
            initializer = closureObject
        )
    }

    override fun visitReturn(expression: IrReturn, data: Unit): BrsStatement {
        return BrsReturn(
            value = if (expression.value.type.isUnit()) null
            else parent.transformExpression(expression.value)
        )
    }

    override fun visitThrow(expression: IrThrow, data: Unit): BrsStatement {
        return if (context.supportsExceptions) {
            BrsThrow(parent.transformExpression(expression.value))
        } else {
            // Fallback for older Roku OS versions - use stop
            BrsExpressionStatement(
                BrsFunctionCall(
                    BrsIdentifier("print"),
                    mutableListOf(
                        BrsStringLiteral("Error: "),
                        parent.transformExpression(expression.value)
                    )
                )
            )
        }
    }

    override fun visitTry(aTry: IrTry, data: Unit): BrsStatement {
        return if (context.supportsExceptions) {
            // Transform try block - can be IrBlock or IrBody
            val tryBlock = when (val tryResult = aTry.tryResult) {
                is IrBody -> parent.transformBody(tryResult)
                is IrBlock -> {
                    val statements = tryResult.statements.mapNotNull { stmt ->
                        when (stmt) {
                            is IrExpression -> BrsExpressionStatement(parent.transformExpression(stmt))
                            else -> parent.transformStatement(stmt)
                        }
                    }
                    BrsBlock(statements.toMutableList())
                }
                else -> BrsBlock(mutableListOf(BrsExpressionStatement(parent.transformExpression(tryResult))))
            }

            // Transform catch block
            val catchBlock = aTry.catches.firstOrNull()?.let { catch ->
                when (val catchResult = catch.result) {
                    is IrBody -> parent.transformBody(catchResult)
                    is IrBlock -> {
                        val statements = catchResult.statements.mapNotNull { stmt ->
                            when (stmt) {
                                is IrExpression -> BrsExpressionStatement(parent.transformExpression(stmt))
                                else -> parent.transformStatement(stmt)
                            }
                        }
                        BrsBlock(statements.toMutableList())
                    }
                    else -> BrsBlock(mutableListOf(BrsExpressionStatement(parent.transformExpression(catchResult))))
                }
            }
            val catchVar = aTry.catches.firstOrNull()?.catchParameter?.name?.asString()

            BrsTry(tryBlock, catchVar, catchBlock)
        } else {
            // Without exception support, just execute the try block
            when (val tryResult = aTry.tryResult) {
                is IrBody -> parent.transformBody(tryResult)
                is IrBlock -> {
                    val statements = tryResult.statements.mapNotNull { stmt ->
                        when (stmt) {
                            is IrExpression -> BrsExpressionStatement(parent.transformExpression(stmt))
                            else -> parent.transformStatement(stmt)
                        }
                    }
                    BrsBlock(statements.toMutableList())
                }
                else -> BrsExpressionStatement(parent.transformExpression(tryResult))
            }
        }
    }

    override fun visitWhen(expression: IrWhen, data: Unit): BrsStatement {
        // Transform to if/else chain
        var result: BrsIf? = null
        var current: BrsIf? = null

        for (branch in expression.branches) {
            val condition = parent.transformExpression(branch.condition)

            // Handle branch result - certain IR nodes need statement transformation
            val bodyStatement: BrsStatement = when (val branchResult = branch.result) {
                is IrReturn -> parent.transformStatement(branchResult) ?: BrsEmpty()
                is IrSetValue -> visitSetValue(branchResult, data)
                is IrSetField -> visitSetField(branchResult, data)
                is IrBlock -> {
                    // Transform block which may contain returns
                    val transformed = transformBlockOrStatement(branchResult)
                    transformed
                }
                else -> {
                    val body = parent.transformExpression(branchResult)
                    BrsExpressionStatement(body)
                }
            }

            val ifStmt = BrsIf(
                condition = condition,
                thenBranch = bodyStatement
            )

            if (result == null) {
                result = ifStmt
                current = ifStmt
            } else {
                current?.elseBranch = ifStmt
                current = ifStmt
            }
        }

        return result ?: BrsEmpty()
    }

    override fun visitWhileLoop(loop: IrWhileLoop, data: Unit): BrsStatement {
        return BrsWhile(
            condition = parent.transformExpression(loop.condition),
            body = loop.body?.let { transformBlockOrStatement(it) } ?: BrsBlock()
        )
    }

    override fun visitDoWhileLoop(loop: IrDoWhileLoop, data: Unit): BrsStatement {
        // BrightScript doesn't have do-while, transform to while with entry guard
        val body = loop.body?.let { transformBlockOrStatement(it) } ?: BrsBlock()
        val condition = parent.transformExpression(loop.condition)

        return BrsBlock(mutableListOf(
            body,
            BrsWhile(condition, body.deepCopy())
        ))
    }

    override fun visitBreak(jump: IrBreak, data: Unit): BrsStatement {
        return BrsExit(BrsExitKind.WHILE) // or FOR depending on context
    }

    override fun visitContinue(jump: IrContinue, data: Unit): BrsStatement {
        return if (context.supportsContinue) {
            BrsContinue(BrsContinueKind.WHILE)
        } else {
            // For older Roku OS, we'd need to restructure the loop
            BrsComment("continue not supported on target Roku OS")
        }
    }

    override fun visitBlockBody(body: IrBlockBody, data: Unit): BrsStatement {
        return parent.transformBody(body)
    }

    override fun visitBlock(expression: IrBlock, data: Unit): BrsStatement {
        // Check if this is a FOR loop that was lowered to a while loop
        if (expression.origin == IrStatementOrigin.FOR_LOOP) {
            val forLoopResult = tryTransformForLoop(expression)
            if (forLoopResult != null) {
                return forLoopResult
            }
        }

        // Check if this is an increment/decrement block
        // These have structure: { val <unary> = old; var = <unary>.inc(); <unary> }
        // When used as statement, we can simplify to just the assignment
        if (expression.origin == IrStatementOrigin.POSTFIX_INCR ||
            expression.origin == IrStatementOrigin.POSTFIX_DECR ||
            expression.origin == IrStatementOrigin.PREFIX_INCR ||
            expression.origin == IrStatementOrigin.PREFIX_DECR) {
            val result = transformIncrementDecrementBlock(expression)
            if (result != null) {
                return result
            }
        }

        val statements = expression.statements.flatMap { stmt ->
            when (stmt) {
                // Route IrReturn to statement transformer (has visitReturn handler)
                // IrReturn extends IrExpression but needs statement-level handling
                is IrReturn -> listOfNotNull(parent.transformStatement(stmt))
                // These are handled during constructor/enum transformation, skip here
                is IrDelegatingConstructorCall -> emptyList()
                is IrInstanceInitializerCall -> emptyList()
                is IrEnumConstructorCall -> emptyList()
                // Skip temp variable REFERENCES (IrGetValue) from increment/decrement blocks
                // These are the return values at the end of the block that shouldn't be statements
                is IrGetValue -> {
                    val varName = stmt.symbol.owner.name.asString()
                    if (varName.startsWith("<") && varName.endsWith(">")) {
                        emptyList()  // Skip temp variable returns like <unary>
                    } else {
                        listOf(BrsExpressionStatement(parent.transformExpression(stmt)))
                    }
                }
                // Keep temp variable DECLARATIONS - they're needed by the setter call
                is IrVariable -> {
                    val varName = stmt.name.asString()
                    // Sanitize the name for BrightScript (remove < and >)
                    val sanitizedName = if (varName.startsWith("<") && varName.endsWith(">")) {
                        varName.removePrefix("<").removeSuffix(">").replace("-", "_")
                    } else {
                        varName
                    }
                    val init = stmt.initializer?.let { parent.transformExpression(it) }
                    if (init != null) {
                        listOf(BrsVariable(sanitizedName, parent.mapTypeToBrs(stmt.type), init))
                    } else {
                        emptyList()
                    }
                }
                // For nested blocks/composites, flatten them to extract all statements
                // This is important for postfix increment/decrement which use blocks
                is IrBlock -> {
                    val nested = visitBlock(stmt, Unit)
                    if (nested is BrsBlock) nested.statements else listOf(nested)
                }
                is IrComposite -> {
                    // Process each statement in the composite
                    stmt.statements.flatMap { nestedStmt ->
                        when (nestedStmt) {
                            is IrBlock -> {
                                val nested = visitBlock(nestedStmt, Unit)
                                if (nested is BrsBlock) nested.statements else listOf(nested)
                            }
                            // Loops are expressions (extend IrExpression) but should be transformed as statements
                            is IrWhileLoop -> listOf(visitWhileLoop(nestedStmt, Unit))
                            is IrDoWhileLoop -> listOf(visitDoWhileLoop(nestedStmt, Unit))
                            // IrWhen (if/when) should be transformed as statements
                            is IrWhen -> listOf(visitWhen(nestedStmt, Unit))
                            is IrExpression -> listOf(BrsExpressionStatement(parent.transformExpression(nestedStmt)))
                            else -> listOfNotNull(parent.transformStatement(nestedStmt))
                        }
                    }
                }
                // Loops are expressions (extend IrExpression) but should be transformed as statements
                is IrWhileLoop -> listOf(visitWhileLoop(stmt, Unit))
                is IrDoWhileLoop -> listOf(visitDoWhileLoop(stmt, Unit))
                // IrWhen (if/when) with Unit type should be transformed as statements, not expressions
                is IrWhen -> listOf(visitWhen(stmt, Unit))
                is IrExpression -> listOf(BrsExpressionStatement(parent.transformExpression(stmt)))
                else -> listOfNotNull(parent.transformStatement(stmt))
            }
        }
        return BrsBlock(statements.toMutableList())
    }

    /**
     * Transform an increment/decrement block.
     *
     * Increment/decrement blocks have structure:
     * IrBlock(origin=POSTFIX_INCR/DECR or PREFIX_INCR/DECR) {
     *   val <unary> = oldValue       // temp variable (for postfix)
     *   assignment                    // IrSetValue, IrSetField, or IrCall to setter
     *   <unary> or newValue          // return value - discarded when used as statement
     * }
     *
     * When used as a statement (result discarded), we output:
     * 1. All statements EXCEPT the final value return
     *
     * Returns null if the pattern doesn't match.
     */
    private fun transformIncrementDecrementBlock(block: IrBlock): BrsStatement? {
        val statements = block.statements
        if (statements.isEmpty()) return null

        // Process all statements except the last one (which is the return value)
        // For postfix: [val <unary> = old, setter(<unary>+1), <unary>]
        // For prefix: [val <unary> = var+1, setter(<unary>), <unary>]
        // We want to output just the setter call, skipping temp var and return value

        // Find statements that are NOT just getters (temp var returns)
        // Include setter calls (IrCall to <set-*>), IrSetValue, IrSetField
        val effectfulStatements = statements.filter { stmt ->
            when (stmt) {
                is IrGetValue -> false  // Skip value returns
                is IrVariable -> false  // Skip temp variable declarations
                is IrSetValue -> true   // Include direct assignments
                is IrSetField -> true   // Include field assignments
                is IrCall -> true       // Include all calls (including setter calls)
                else -> false
            }
        }

        if (effectfulStatements.isEmpty()) return null

        // Transform each effectful statement
        val brsStatements = effectfulStatements.mapNotNull { stmt ->
            when (stmt) {
                is IrSetValue -> visitSetValue(stmt, Unit)
                is IrSetField -> visitSetField(stmt, Unit)
                is IrCall -> BrsExpressionStatement(parent.transformExpression(stmt))
                is IrExpression -> BrsExpressionStatement(parent.transformExpression(stmt))
                else -> parent.transformStatement(stmt)
            }
        }

        return if (brsStatements.isEmpty()) {
            null
        } else if (brsStatements.size == 1) {
            brsStatements.first()
        } else {
            BrsBlock(brsStatements.toMutableList())
        }
    }

    /**
     * Try to transform a lowered FOR loop back to a BrightScript for/forEach loop.
     * Returns null if the pattern doesn't match and we should fall back to while loop.
     */
    private fun tryTransformForLoop(block: IrBlock): BrsStatement? {
        // Structure of a lowered FOR loop:
        // IrBlock(origin=FOR_LOOP) {
        //   IrVariable(origin=FOR_LOOP_ITERATOR) = <iterable>.iterator()
        //   IrWhileLoop(origin=FOR_LOOP_INNER_WHILE) {
        //     condition: iterator.hasNext()
        //     body: {
        //       val loopVar = iterator.next()
        //       // actual body
        //     }
        //   }
        // }

        val statements = block.statements
        if (statements.size < 2) return null

        // Find the iterator variable
        val iteratorVar = statements.firstOrNull {
            it is IrVariable && it.origin == IrDeclarationOrigin.FOR_LOOP_ITERATOR
        } as? IrVariable ?: return null

        // Find the while loop
        val whileLoop = statements.lastOrNull {
            it is IrWhileLoop && it.origin == IrStatementOrigin.FOR_LOOP_INNER_WHILE
        } as? IrWhileLoop ?: return null

        // Try to extract the iterable from the iterator initialization
        val iteratorInit = iteratorVar.initializer as? IrCall ?: return null
        val iterableExpr = iteratorInit.dispatchReceiver ?: iteratorInit.extensionReceiver ?: return null

        // Get the loop body
        val loopBody = whileLoop.body as? IrContainerExpression ?: return null
        val bodyStatements = loopBody.statements

        if (bodyStatements.isEmpty()) return null

        // First statement should be the loop variable assignment from iterator.next()
        val loopVarDecl = bodyStatements.firstOrNull() as? IrVariable ?: return null
        val loopVarName = loopVarDecl.name.asString()

        // Transform the remaining body statements (skip the loop variable declaration)
        val actualBody = bodyStatements.drop(1).mapNotNull { stmt ->
            when (stmt) {
                // IrWhen (if statements) should use visitWhen directly to handle returns properly
                is IrWhen -> visitWhen(stmt, Unit)
                // IrWhileLoop and IrDoWhileLoop need explicit handling
                is IrWhileLoop -> visitWhileLoop(stmt, Unit)
                is IrDoWhileLoop -> visitDoWhileLoop(stmt, Unit)
                // IrBlock containing a when (if statement) should unwrap
                is IrBlock -> {
                    // Check if block contains a single IrWhen statement
                    val singleWhen = stmt.statements.singleOrNull() as? IrWhen
                    if (singleWhen != null) {
                        visitWhen(singleWhen, Unit)
                    } else {
                        transformBlockOrStatement(stmt)
                    }
                }
                // Unit-returning expressions should be treated as statements
                is IrExpression -> if (stmt.type.isUnit()) {
                    parent.transformStatement(stmt)
                } else {
                    BrsExpressionStatement(parent.transformExpression(stmt))
                }
                else -> parent.transformStatement(stmt)
            }
        }

        // Generate BrsForEach
        return BrsForEach(
            variable = loopVarName,
            iterable = parent.transformExpression(iterableExpr),
            body = BrsBlock(actualBody.toMutableList())
        )
    }

    override fun visitSetValue(expression: IrSetValue, data: Unit): BrsStatement {
        val rawName = expression.symbol.owner.name.asString()

        // Check if this variable is captured in a closure
        val capturedVar = parent.getCapturedVariable(expression.symbol)
        if (capturedVar != null && capturedVar.isMutable) {
            // Rewrite to assign via m (the closure object): m.varName.value = newValue
            return BrsExpressionStatement(
                BrsBinaryOp(
                    BrsDotAccess(BrsDotAccess(BrsMRef(), capturedVar.name), "value"),
                    BrsBinaryOperator.EQ,
                    parent.transformExpression(expression.value)
                )
            )
        }

        val sanitizedName = when {
            rawName.startsWith("<set-") && rawName.endsWith(">") -> "value"
            rawName.startsWith("<") && rawName.endsWith(">") ->
                rawName.removePrefix("<").removeSuffix(">").replace("-", "_")
            else -> rawName
        }
        return BrsExpressionStatement(
            BrsBinaryOp(
                BrsIdentifier(sanitizedName),
                BrsBinaryOperator.EQ,
                parent.transformExpression(expression.value)
            )
        )
    }

    override fun visitSetField(expression: IrSetField, data: Unit): BrsStatement {
        val receiver = expression.receiver?.let { parent.transformExpression(it) }
            ?: BrsMRef()

        return BrsExpressionStatement(
            BrsBinaryOp(
                BrsDotAccess(receiver, expression.symbol.owner.name.asString()),
                BrsBinaryOperator.EQ,
                parent.transformExpression(expression.value)
            )
        )
    }

    private fun transformBlockOrStatement(element: IrElement): BrsStatement {
        return when (element) {
            is IrBlock -> visitBlock(element, Unit)
            is IrBlockBody -> visitBlockBody(element, Unit)
            // Loops need explicit handling since they're IrExpressions but should be transformed as statements
            is IrWhileLoop -> visitWhileLoop(element, Unit)
            is IrDoWhileLoop -> visitDoWhileLoop(element, Unit)
            // IrWhen (if/when) needs explicit handling for proper statement transformation
            is IrWhen -> visitWhen(element, Unit)
            is IrExpression -> BrsExpressionStatement(parent.transformExpression(element))
            is IrStatement -> parent.transformStatement(element) ?: BrsEmpty()
            else -> BrsEmpty()
        }
    }
}

/**
 * Transforms IR expressions to BrightScript expressions.
 */
class IrExpressionToBrsTransformer(
    private val parent: IrToBrsTransformer,
    private val context: BrsIrBackendContext
) : IrElementVisitor<BrsExpression, Unit> {

    override fun visitElement(element: IrElement, data: Unit): BrsExpression {
        return BrsStringLiteral("/* Unsupported: ${element::class.simpleName} */")
    }

    // ==================== Literals ====================

    override fun visitConst(expression: IrConst, data: Unit): BrsExpression {
        return when (expression.kind) {
            IrConstKind.Int -> BrsIntLiteral(expression.value as Int)
            IrConstKind.Long -> BrsLongIntLiteral(expression.value as Long)
            IrConstKind.Float -> BrsFloatLiteral(expression.value as Float)
            IrConstKind.Double -> BrsDoubleLiteral(expression.value as Double)
            IrConstKind.Boolean -> BrsBooleanLiteral(expression.value as Boolean)
            IrConstKind.String -> BrsStringLiteral(expression.value as String)
            IrConstKind.Char -> BrsStringLiteral((expression.value as Char).toString())
            IrConstKind.Byte -> BrsIntLiteral((expression.value as Byte).toInt())
            IrConstKind.Short -> BrsIntLiteral((expression.value as Short).toInt())
            IrConstKind.Null -> BrsInvalidLiteral()
        }
    }

    // ==================== References ====================

    override fun visitGetValue(expression: IrGetValue, data: Unit): BrsExpression {
        // Check if this is a temp variable that should be inlined (increment/decrement)
        val substitution = parent.getTempVarSubstitution(expression.symbol)
        if (substitution != null) {
            // Inline the initializer expression instead of outputting the temp var reference
            return substitution.accept(this, data)
        }

        val rawName = expression.symbol.owner.name.asString()

        // Check if this variable is captured in a closure
        val capturedVar = parent.getCapturedVariable(expression.symbol)
        if (capturedVar != null) {
            // Rewrite to access via m (the closure object)
            return if (capturedVar.isMutable) {
                // Mutable captures: m.varName.value
                BrsDotAccess(BrsDotAccess(BrsMRef(), capturedVar.name), "value")
            } else {
                // Read-only captures: m.varName
                BrsDotAccess(BrsMRef(), capturedVar.name)
            }
        }

        return when {
            rawName == "<this>" -> BrsMRef()
            // Sanitize setter parameter names
            rawName.startsWith("<set-") && rawName.endsWith(">") -> BrsIdentifier("value")
            // Sanitize other special names
            rawName.startsWith("<") && rawName.endsWith(">") ->
                BrsIdentifier(rawName.removePrefix("<").removeSuffix(">").replace("-", "_"))
            else -> BrsIdentifier(rawName)
        }
    }

    override fun visitGetField(expression: IrGetField, data: Unit): BrsExpression {
        val field = expression.symbol.owner
        val receiver = expression.receiver?.let { it.accept(this, data) }
            ?: BrsMRef()

        // Check if accessing outer class field from inner class
        // The receiver will be the outer class reference
        val fieldParentClass = field.parent as? IrClass
        if (fieldParentClass != null) {
            // If the receiver references an outer class instance (not the current class),
            // it means we're accessing an outer class field
            val receiverSymbol = (expression.receiver as? IrGetValue)?.symbol?.owner
            if (receiverSymbol != null && receiverSymbol.name.asString().contains("\$this")) {
                // This is an outer class reference - access through $outer
                return BrsDotAccess(
                    BrsDotAccess(BrsMRef(), "\$outer"),
                    field.name.asString()
                )
            }
        }

        return BrsDotAccess(receiver, field.name.asString())
    }

    override fun visitSetField(expression: IrSetField, data: Unit): BrsExpression {
        val field = expression.symbol.owner
        val receiver = expression.receiver?.let { it.accept(this, data) }
            ?: BrsMRef()

        // Generate assignment expression: receiver.field = value
        return BrsBinaryOp(
            BrsDotAccess(receiver, field.name.asString()),
            BrsBinaryOperator.EQ,
            expression.value.accept(this, data)
        )
    }

    override fun visitSetValue(expression: IrSetValue, data: Unit): BrsExpression {
        val rawName = expression.symbol.owner.name.asString()

        // Check if this variable is captured in a closure
        val capturedVar = parent.getCapturedVariable(expression.symbol)
        if (capturedVar != null && capturedVar.isMutable) {
            // Rewrite to assign via m (the closure object): m.varName.value = newValue
            return BrsBinaryOp(
                BrsDotAccess(BrsDotAccess(BrsMRef(), capturedVar.name), "value"),
                BrsBinaryOperator.EQ,
                expression.value.accept(this, data)
            )
        }

        val sanitizedName = when {
            rawName.startsWith("<set-") && rawName.endsWith(">") -> "value"
            rawName.startsWith("<") && rawName.endsWith(">") ->
                rawName.removePrefix("<").removeSuffix(">").replace("-", "_")
            else -> rawName
        }

        // Generate assignment expression: varName = value
        return BrsBinaryOp(
            BrsIdentifier(sanitizedName),
            BrsBinaryOperator.EQ,
            expression.value.accept(this, data)
        )
    }

    override fun visitGetObjectValue(expression: IrGetObjectValue, data: Unit): BrsExpression {
        // Object singletons are represented as function calls that create/return the instance
        val name = context.getBrsName(expression.symbol.owner)
        return BrsFunctionCall(BrsIdentifier("${name}_getInstance"), mutableListOf())
    }

    override fun visitGetEnumValue(expression: IrGetEnumValue, data: Unit): BrsExpression {
        val enumClass = expression.symbol.owner.parentAsClass
        val className = context.getBrsName(enumClass)
        val entryName = expression.symbol.owner.name.asString()
        return BrsIdentifier("${className}_${entryName}")
    }

    // ==================== Function Calls ====================

    override fun visitCall(expression: IrCall, data: Unit): BrsExpression {
        val function = expression.symbol.owner

        // Check for operator expressions based on origin
        expression.origin?.let { origin ->
            val operatorResult = transformOperator(expression, origin)
            if (operatorResult != null) return operatorResult
        }

        // Check for intrinsics
        if (context.intrinsics.isIntrinsic(expression.symbol)) {
            return transformIntrinsic(expression)
        }

        // Handle invoke calls on function-typed variables
        // In Kotlin, calling a function-typed variable like `ref(5)` generates an invoke call.
        // In BrightScript, lambdas are closure objects with an invoke method, so we call .invoke()
        if (function.name.asString() == "invoke") {
            val receiver = expression.dispatchReceiver ?: expression.extensionReceiver
            if (receiver != null) {
                // Check if receiver is a function type (Function0, Function1, etc.) or KFunction
                val receiverTypeName = receiver.type.classFqName?.asString() ?: ""
                val isFunctionType = receiver.type.isFunction() ||
                    receiverTypeName.startsWith("kotlin.Function") ||
                    receiverTypeName.startsWith("kotlin.reflect.KFunction")
                if (isFunctionType) {
                    val receiverExpr = receiver.accept(this, data)
                    val args = (0 until expression.valueArgumentsCount).mapNotNull { i ->
                        expression.getValueArgument(i)?.accept(this, data)
                    }
                    // Call .invoke() method on the closure object
                    return BrsMethodCall(receiverExpr, "invoke", args.toMutableList())
                }
            }
        }

        // Handle calls to local functions (parent is another function, not a class or file)
        // Local functions are now closure objects, so we need to call .invoke()
        val isLocalFunction = function.parent is IrFunction
        if (isLocalFunction) {
            val localFunctionName = function.name.asString()
            val args = (0 until expression.valueArgumentsCount).mapNotNull { i ->
                expression.getValueArgument(i)?.accept(this, data)
            }
            // Call .invoke() method on the local function closure object
            return BrsMethodCall(BrsIdentifier(localFunctionName), "invoke", args.toMutableList())
        }

        val functionName = context.getBrsName(function)
        val arguments = mutableListOf<BrsExpression>()

        // Add dispatch receiver if present
        expression.dispatchReceiver?.let { receiver ->
            // Check if this is an outer class reference (for inner class access)
            // We detect this by checking if the function belongs to the outer class
            // while the current receiver would resolve to a different class
            val functionParentClass = function.parent as? IrClass
            val receiverValue = receiver as? IrGetValue
            val receiverParamOwner = receiverValue?.symbol?.owner

            // If the function belongs to an outer class and the receiver is a dispatch receiver
            // from the outer class (not the current inner class), we access through $outer
            val isOuterClassAccess = if (functionParentClass != null && receiverParamOwner != null) {
                // Check if the receiver is a dispatch receiver that belongs to the function's parent class
                // When we're in an inner class and accessing outer class members,
                // the receiver's parent will be the outer class itself
                val receiverParentClass = receiverParamOwner.parent as? IrClass
                receiverParentClass == functionParentClass && functionParentClass.declarations.any {
                    it is IrClass && it.isInner
                }
            } else {
                false
            }

            val receiverExpr = if (isOuterClassAccess) {
                // Access through $outer for inner class outer reference
                BrsDotAccess(BrsMRef(), "\$outer")
            } else {
                receiver.accept(this, data)
            }

            // For outer class property access, use direct field access instead of getter call
            if (isOuterClassAccess) {
                val rawName = function.name.asString()
                if (rawName.startsWith("<get-") && rawName.endsWith(">")) {
                    // This is a property getter - use direct field access
                    val fieldName = rawName.removePrefix("<get-").removeSuffix(">")
                    return BrsDotAccess(receiverExpr, fieldName)
                }
                if (rawName.startsWith("<set-") && rawName.endsWith(">")) {
                    // This is a property setter - transform to assignment
                    val fieldName = rawName.removePrefix("<set-").removeSuffix(">")
                    val value = expression.getValueArgument(0)?.accept(this, data)
                        ?: BrsInvalidLiteral()
                    return BrsBinaryOp(
                        BrsDotAccess(receiverExpr, fieldName),
                        BrsBinaryOperator.EQ,
                        value
                    )
                }
            }

            // For method calls, transform to dot notation
            if (function.dispatchReceiverParameter != null) {
                // Check if this is a call on a singleton object - use global function instead
                val parentClass = function.parent as? IrClass
                if (parentClass?.kind == ClassKind.OBJECT) {
                    // For singleton objects, call the global function directly
                    val args = (0 until expression.valueArgumentsCount).mapNotNull { i ->
                        expression.getValueArgument(i)?.let { it.accept(this, data) }
                    }
                    return BrsFunctionCall(BrsIdentifier(functionName), args.toMutableList())
                }

                // For regular method calls, sanitize the method name
                val methodName = parent.sanitizeMethodName(function.name.asString())
                val args = (0 until expression.valueArgumentsCount).mapNotNull { i ->
                    expression.getValueArgument(i)?.let { it.accept(this, data) }
                }
                return BrsMethodCall(receiverExpr, methodName, args.toMutableList())
            }
            arguments.add(receiverExpr)
        }

        // Add extension receiver if present
        expression.extensionReceiver?.let { receiver ->
            arguments.add(receiver.accept(this, data))
        }

        // Add value arguments
        for (i in 0 until expression.valueArgumentsCount) {
            expression.getValueArgument(i)?.let { arg ->
                arguments.add(arg.accept(this, data))
            }
        }

        return BrsFunctionCall(BrsIdentifier(functionName), arguments)
    }

    private fun transformIntrinsic(expression: IrCall): BrsExpression {
        val function = expression.symbol.owner
        val name = function.name.asString()

        // Check if this is a stdlib intrinsic
        if (context.intrinsics.isStdlibIntrinsic(expression.symbol)) {
            return transformStdlibIntrinsic(expression, name)
        }

        return when (name) {
            "createObject" -> {
                val typeArg = expression.getValueArgument(0)
                val objectType = (typeArg as? IrConst)?.value?.toString() ?: "Object"
                val args = (1 until expression.valueArgumentsCount).mapNotNull { i ->
                    expression.getValueArgument(i)?.let { it.accept(this, Unit) }
                }
                BrsCreateObject(objectType, args.toMutableList())
            }
            "typeOf" -> {
                val arg = expression.getValueArgument(0)?.let { it.accept(this, Unit) }
                    ?: BrsInvalidLiteral()
                BrsTypeOf(arg)
            }
            "print" -> {
                val args = (0 until expression.valueArgumentsCount).mapNotNull { i ->
                    expression.getValueArgument(i)?.let { it.accept(this, Unit) }
                }
                BrsFunctionCall(BrsIdentifier("print"), args.toMutableList())
            }
            else -> BrsFunctionCall(BrsIdentifier(name), mutableListOf())
        }
    }

    /**
     * Transform a stdlib intrinsic call to its BrightScript equivalent.
     */
    private fun transformStdlibIntrinsic(expression: IrCall, name: String): BrsExpression {
        val intrinsic = context.intrinsics.stdlibIntrinsicMapping[name]

        // Get all arguments
        val args = (0 until expression.valueArgumentsCount).mapNotNull { i ->
            expression.getValueArgument(i)?.let { it.accept(this, Unit) }
        }

        return when (intrinsic) {
            is BrsIntrinsics.StdlibIntrinsic.SimpleCall -> {
                // Simple function call: brsIntrinsicSin(x) -> Sin(x)
                BrsFunctionCall(BrsIdentifier(intrinsic.brsName), args.toMutableList())
            }

            is BrsIntrinsics.StdlibIntrinsic.Pow -> {
                // Power: x ^ y
                if (args.size >= 2) {
                    BrsBinaryOp(args[0], BrsBinaryOperator.POW, args[1])
                } else {
                    BrsInvalidLiteral()
                }
            }

            is BrsIntrinsics.StdlibIntrinsic.Atan2 -> {
                // atan2(y, x) in BrightScript can be computed as Atn(y/x) with quadrant adjustment
                // For now, use a simplified version (BrightScript doesn't have native atan2)
                if (args.size >= 2) {
                    // Generate: Atn(y / x)
                    // Note: This is simplified and doesn't handle all quadrants correctly
                    // A proper implementation would need runtime checks
                    BrsFunctionCall(
                        BrsIdentifier("Atn"),
                        mutableListOf(BrsBinaryOp(args[0], BrsBinaryOperator.DIV, args[1]))
                    )
                } else {
                    BrsInvalidLiteral()
                }
            }

            is BrsIntrinsics.StdlibIntrinsic.Asin -> {
                // asin(x) = atan(x / sqrt(1 - x*x))
                if (args.isNotEmpty()) {
                    val x = args[0]
                    // Generate: Atn(x / Sqr(1 - x * x))
                    val xSquared = BrsBinaryOp(x.deepCopy(), BrsBinaryOperator.MUL, x.deepCopy())
                    val oneMinusXSquared = BrsBinaryOp(BrsDoubleLiteral(1.0), BrsBinaryOperator.SUB, xSquared)
                    val sqrtPart = BrsFunctionCall(BrsIdentifier("Sqr"), mutableListOf(oneMinusXSquared))
                    BrsFunctionCall(
                        BrsIdentifier("Atn"),
                        mutableListOf(BrsBinaryOp(x.deepCopy(), BrsBinaryOperator.DIV, sqrtPart))
                    )
                } else {
                    BrsInvalidLiteral()
                }
            }

            is BrsIntrinsics.StdlibIntrinsic.Acos -> {
                // acos(x) = atan(sqrt(1 - x*x) / x) + adjustment for x < 0
                // Simplified: acos(x) = pi/2 - asin(x)
                if (args.isNotEmpty()) {
                    val x = args[0]
                    // Generate: Atn(Sqr(1 - x * x) / x)
                    // Note: This doesn't handle negative x correctly (needs pi adjustment)
                    // For full correctness, use: pi/2 - Atn(x / Sqr(1 - x * x))
                    val xSquared = BrsBinaryOp(x.deepCopy(), BrsBinaryOperator.MUL, x.deepCopy())
                    val oneMinusXSquared = BrsBinaryOp(BrsDoubleLiteral(1.0), BrsBinaryOperator.SUB, xSquared)
                    val sqrtPart = BrsFunctionCall(BrsIdentifier("Sqr"), mutableListOf(oneMinusXSquared))
                    // pi/2 - asin(x) = 1.5707963267948966 - Atn(x / Sqr(1 - x*x))
                    val asinPart = BrsFunctionCall(
                        BrsIdentifier("Atn"),
                        mutableListOf(BrsBinaryOp(x.deepCopy(), BrsBinaryOperator.DIV, sqrtPart))
                    )
                    BrsBinaryOp(BrsDoubleLiteral(1.5707963267948966), BrsBinaryOperator.SUB, asinPart)
                } else {
                    BrsInvalidLiteral()
                }
            }

            is BrsIntrinsics.StdlibIntrinsic.Ceil -> {
                // ceil(x): Int(x) + (1 if x > Int(x) else 0)
                // Simplified: use conditional expression
                if (args.isNotEmpty()) {
                    val x = args[0]
                    // Generate: if x > Int(x) then Int(x) + 1 else Int(x)
                    val intX = BrsFunctionCall(BrsIdentifier("Int"), mutableListOf(x.deepCopy()))
                    BrsConditional(
                        BrsBinaryOp(x.deepCopy(), BrsBinaryOperator.GT, intX.deepCopy()),
                        BrsBinaryOp(intX.deepCopy(), BrsBinaryOperator.ADD, BrsIntLiteral(1)),
                        intX
                    )
                } else {
                    BrsInvalidLiteral()
                }
            }

            is BrsIntrinsics.StdlibIntrinsic.Floor -> {
                // floor(x): Int(x)
                if (args.isNotEmpty()) {
                    BrsFunctionCall(BrsIdentifier("Int"), mutableListOf(args[0]))
                } else {
                    BrsInvalidLiteral()
                }
            }

            is BrsIntrinsics.StdlibIntrinsic.Round -> {
                // round(x): Int(x + 0.5)
                if (args.isNotEmpty()) {
                    BrsFunctionCall(
                        BrsIdentifier("Int"),
                        mutableListOf(BrsBinaryOp(args[0], BrsBinaryOperator.ADD, BrsDoubleLiteral(0.5)))
                    )
                } else {
                    BrsInvalidLiteral()
                }
            }

            is BrsIntrinsics.StdlibIntrinsic.Sinh -> {
                // sinh(x): (Exp(x) - Exp(-x)) / 2
                if (args.isNotEmpty()) {
                    val x = args[0]
                    val expX = BrsFunctionCall(BrsIdentifier("Exp"), mutableListOf(x.deepCopy()))
                    val expNegX = BrsFunctionCall(
                        BrsIdentifier("Exp"),
                        mutableListOf(BrsUnaryOp(BrsUnaryOperator.NEG, x.deepCopy()))
                    )
                    BrsBinaryOp(
                        BrsBinaryOp(expX, BrsBinaryOperator.SUB, expNegX),
                        BrsBinaryOperator.DIV,
                        BrsDoubleLiteral(2.0)
                    )
                } else {
                    BrsInvalidLiteral()
                }
            }

            is BrsIntrinsics.StdlibIntrinsic.Cosh -> {
                // cosh(x): (Exp(x) + Exp(-x)) / 2
                if (args.isNotEmpty()) {
                    val x = args[0]
                    val expX = BrsFunctionCall(BrsIdentifier("Exp"), mutableListOf(x.deepCopy()))
                    val expNegX = BrsFunctionCall(
                        BrsIdentifier("Exp"),
                        mutableListOf(BrsUnaryOp(BrsUnaryOperator.NEG, x.deepCopy()))
                    )
                    BrsBinaryOp(
                        BrsBinaryOp(expX, BrsBinaryOperator.ADD, expNegX),
                        BrsBinaryOperator.DIV,
                        BrsDoubleLiteral(2.0)
                    )
                } else {
                    BrsInvalidLiteral()
                }
            }

            is BrsIntrinsics.StdlibIntrinsic.Tanh -> {
                // tanh(x): (Exp(2x) - 1) / (Exp(2x) + 1)
                if (args.isNotEmpty()) {
                    val x = args[0]
                    val twoX = BrsBinaryOp(BrsDoubleLiteral(2.0), BrsBinaryOperator.MUL, x.deepCopy())
                    val exp2X = BrsFunctionCall(BrsIdentifier("Exp"), mutableListOf(twoX))
                    BrsBinaryOp(
                        BrsBinaryOp(exp2X.deepCopy(), BrsBinaryOperator.SUB, BrsDoubleLiteral(1.0)),
                        BrsBinaryOperator.DIV,
                        BrsBinaryOp(exp2X, BrsBinaryOperator.ADD, BrsDoubleLiteral(1.0))
                    )
                } else {
                    BrsInvalidLiteral()
                }
            }

            is BrsIntrinsics.StdlibIntrinsic.Print -> {
                // print statement
                if (intrinsic.newline) {
                    BrsFunctionCall(BrsIdentifier("print"), args.toMutableList())
                } else {
                    // Print without newline: print arg;
                    // BrightScript uses semicolon to suppress newline
                    BrsPrintNoNewline(args.firstOrNull() ?: BrsStringLiteral(""))
                }
            }

            is BrsIntrinsics.StdlibIntrinsic.CurrentTimeMillis -> {
                // CreateObject("roTimespan").TotalMilliseconds()
                BrsMethodCall(
                    BrsCreateObject("roTimespan", mutableListOf()),
                    "TotalMilliseconds",
                    mutableListOf()
                )
            }

            is BrsIntrinsics.StdlibIntrinsic.RandomSeed -> {
                // Rnd(0) to seed from system time
                BrsFunctionCall(BrsIdentifier("Rnd"), mutableListOf(BrsIntLiteral(0)))
            }

            is BrsIntrinsics.StdlibIntrinsic.CreateObject -> {
                // CreateObject(type, ...)
                if (args.isNotEmpty()) {
                    val typeArg = args[0]
                    val objectType = if (typeArg is BrsStringLiteral) typeArg.value else "Object"
                    BrsCreateObject(objectType, args.drop(1).toMutableList())
                } else {
                    BrsInvalidLiteral()
                }
            }

            null -> {
                // Unknown intrinsic - generate as function call with the name stripped of prefix
                val simpleName = name.removePrefix("brsIntrinsic")
                BrsFunctionCall(BrsIdentifier(simpleName), args.toMutableList())
            }
        }
    }

    /**
     * Transform operator calls to BrightScript binary/unary operations.
     * Returns null if the origin is not an operator origin.
     */
    private fun transformOperator(expression: IrCall, origin: IrStatementOrigin): BrsExpression? {
        val function = expression.symbol.owner

        // Special case: EXCLEQ on Boolean.not() call - this means `!=` was lowered to `(a == b).not()`
        // The inner equality call also has EXCLEQ origin, so we must extract operands directly
        // and generate NE to avoid double-negation (NOT of <> would give =).
        if (origin == IrStatementOrigin.EXCLEQ && function.name.asString() == "not") {
            val dispatchReceiver = expression.dispatchReceiver
            if (dispatchReceiver != null && function.valueParameters.isEmpty()) {
                if (dispatchReceiver is IrCall) {
                    val innerCall = dispatchReceiver
                    val (left, right) = when {
                        innerCall.dispatchReceiver != null -> {
                            val l = innerCall.dispatchReceiver!!.accept(this, Unit)
                            val r = innerCall.getValueArgument(0)?.accept(this, Unit) ?: return null
                            Pair(l, r)
                        }
                        innerCall.extensionReceiver != null -> {
                            val l = innerCall.extensionReceiver!!.accept(this, Unit)
                            val r = innerCall.getValueArgument(0)?.accept(this, Unit) ?: return null
                            Pair(l, r)
                        }
                        else -> {
                            val l = innerCall.getValueArgument(0)?.accept(this, Unit) ?: return null
                            val r = innerCall.getValueArgument(1)?.accept(this, Unit) ?: return null
                            Pair(l, r)
                        }
                    }
                    return BrsBinaryOp(left, BrsBinaryOperator.NE, right)
                }
                // Fallback for non-call receivers
                val operand = dispatchReceiver.accept(this, Unit)
                return BrsUnaryOp(BrsUnaryOperator.NOT, operand)
            }
        }

        // Binary operators
        val binaryOp = when (origin) {
            IrStatementOrigin.PLUS -> BrsBinaryOperator.ADD
            IrStatementOrigin.MINUS -> BrsBinaryOperator.SUB
            IrStatementOrigin.MUL -> BrsBinaryOperator.MUL
            IrStatementOrigin.DIV -> BrsBinaryOperator.DIV
            IrStatementOrigin.PERC -> BrsBinaryOperator.MOD
            IrStatementOrigin.LT -> BrsBinaryOperator.LT
            IrStatementOrigin.GT -> BrsBinaryOperator.GT
            IrStatementOrigin.LTEQ -> BrsBinaryOperator.LE
            IrStatementOrigin.GTEQ -> BrsBinaryOperator.GE
            IrStatementOrigin.EQEQ -> BrsBinaryOperator.EQ
            IrStatementOrigin.EXCLEQ -> BrsBinaryOperator.NE
            else -> null
        }

        if (binaryOp != null) {
            // For binary operators, determine left and right operands based on structure:
            // 1. If dispatchReceiver exists: left = dispatchReceiver, right = valueArgument(0)
            // 2. If extensionReceiver exists: left = extensionReceiver, right = valueArgument(0)
            // 3. Otherwise: left = valueArgument(0), right = valueArgument(1)
            //
            // Special case: If this is EXCLEQ but valueArgumentsCount is 0, it means the
            // != was converted to (a == b).not() and we're on the outer call. Skip binary handling.
            if (binaryOp == BrsBinaryOperator.NE && expression.valueArgumentsCount == 0) {
                // This case is handled above in the special EXCLEQ handling, but just in case
                return null
            }

            val (left, right) = when {
                expression.dispatchReceiver != null -> {
                    val l = expression.dispatchReceiver!!.accept(this, Unit)
                    val r = expression.getValueArgument(0)?.accept(this, Unit) ?: return null
                    Pair(l, r)
                }
                expression.extensionReceiver != null -> {
                    val l = expression.extensionReceiver!!.accept(this, Unit)
                    val r = expression.getValueArgument(0)?.accept(this, Unit) ?: return null
                    Pair(l, r)
                }
                else -> {
                    val l = expression.getValueArgument(0)?.accept(this, Unit) ?: return null
                    val r = expression.getValueArgument(1)?.accept(this, Unit) ?: return null
                    Pair(l, r)
                }
            }
            return BrsBinaryOp(left, binaryOp, right)
        }

        // Unary operators
        return when (origin) {
            IrStatementOrigin.UMINUS -> {
                val operand = expression.dispatchReceiver?.let { it.accept(this, Unit) }
                    ?: expression.getValueArgument(0)?.let { it.accept(this, Unit) }
                    ?: return null
                BrsUnaryOp(BrsUnaryOperator.NEG, operand)
            }
            IrStatementOrigin.UPLUS -> {
                // Unary plus is a no-op in most cases
                expression.dispatchReceiver?.let { it.accept(this, Unit) }
                    ?: expression.getValueArgument(0)?.let { it.accept(this, Unit) }
            }
            IrStatementOrigin.EXCL -> {
                val operand = expression.dispatchReceiver?.let { it.accept(this, Unit) }
                    ?: expression.getValueArgument(0)?.let { it.accept(this, Unit) }
                    ?: return null
                BrsUnaryOp(BrsUnaryOperator.NOT, operand)
            }
            IrStatementOrigin.GET_ARRAY_ELEMENT -> {
                // Array access: arr[index]
                val array = expression.dispatchReceiver?.let { it.accept(this, Unit) }
                    ?: return null
                val index = expression.getValueArgument(0)?.let { it.accept(this, Unit) }
                    ?: return null
                BrsIndexAccess(array, index)
            }
            else -> null
        }
    }

    override fun visitConstructorCall(expression: IrConstructorCall, data: Unit): BrsExpression {
        val constructor = expression.symbol.owner
        val irClass = constructor.parentAsClass

        val arguments = mutableListOf<BrsExpression>()

        // For inner classes, pass the outer instance as the first argument
        if (irClass.isInner) {
            expression.dispatchReceiver?.let { outer ->
                arguments.add(outer.accept(this, data))
            } ?: run {
                // If no explicit dispatch receiver, use 'm' (the current instance)
                arguments.add(BrsMRef())
            }
        }

        // Add regular constructor arguments
        arguments.addAll((0 until expression.valueArgumentsCount).mapNotNull { i ->
            expression.getValueArgument(i)?.let { it.accept(this, data) }
        })

        // Check if this is an external class (Roku SDK type)
        if (irClass.isExternal || isExternalClass(irClass)) {
            // External classes use CreateObject()
            val brsTypeName = getBrsExternalTypeName(irClass)
            return BrsCreateObject(brsTypeName, arguments.toMutableList())
        }

        // Use mangled constructor name to support overloading
        return BrsFunctionCall(
            BrsIdentifier(context.getBrsName(constructor)),
            arguments
        )
    }

    override fun visitDelegatingConstructorCall(expression: IrDelegatingConstructorCall, data: Unit): BrsExpression {
        val constructor = expression.symbol.owner

        val arguments = (0 until expression.valueArgumentsCount).mapNotNull { i ->
            expression.getValueArgument(i)?.let { it.accept(this, data) }
        }

        // Use mangled constructor name to support overloading
        return BrsFunctionCall(
            BrsIdentifier(context.getBrsName(constructor)),
            arguments.toMutableList()
        )
    }

    override fun visitInstanceInitializerCall(expression: IrInstanceInitializerCall, data: Unit): BrsExpression {
        // Instance initializers are handled during constructor generation
        // Return invalid as a no-op placeholder
        return BrsInvalidLiteral()
    }

    override fun visitEnumConstructorCall(expression: IrEnumConstructorCall, data: Unit): BrsExpression {
        val constructor = expression.symbol.owner

        val arguments = (0 until expression.valueArgumentsCount).mapNotNull { i ->
            expression.getValueArgument(i)?.let { it.accept(this, data) }
        }

        // Use mangled constructor name to support overloading
        return BrsFunctionCall(
            BrsIdentifier(context.getBrsName(constructor)),
            arguments.toMutableList()
        )
    }

    /**
     * Checks if a class is marked with @BrsExternal annotation.
     */
    private fun isExternalClass(irClass: IrClass): Boolean {
        return irClass.annotations.any { annotation ->
            val annotationClass = annotation.type.classifierOrNull?.owner as? IrClass
            annotationClass?.name?.asString() == "BrsExternal"
        }
    }

    /**
     * Gets the BrightScript object type name for CreateObject().
     * E.g., RoArray -> "roArray", RoAssociativeArray -> "roAssociativeArray"
     */
    private fun getBrsExternalTypeName(irClass: IrClass): String {
        // Check for @BrsName annotation first
        val brsNameAnnotation = irClass.annotations.find { annotation ->
            val annotationClass = annotation.type.classifierOrNull?.owner as? IrClass
            annotationClass?.name?.asString() == "BrsName"
        }

        if (brsNameAnnotation != null) {
            val nameArg = brsNameAnnotation.getValueArgument(0)
            if (nameArg is IrConst) {
                return nameArg.value as String
            }
        }

        // Default: convert class name to BrightScript type name
        // RoArray -> roArray, RoDeviceInfo -> roDeviceInfo
        val className = irClass.name.asString()
        return if (className.startsWith("Ro")) {
            "ro${className.substring(2)}"
        } else {
            className.lowercase()
        }
    }

    // ==================== Operators ====================

    override fun visitTypeOperator(expression: IrTypeOperatorCall, data: Unit): BrsExpression {
        val argument = expression.argument.accept(this, data)

        return when (expression.operator) {
            IrTypeOperator.CAST, IrTypeOperator.IMPLICIT_CAST -> argument
            IrTypeOperator.SAFE_CAST -> {
                // Safe cast returns invalid if type doesn't match
                BrsConditional(
                    generateInstanceCheck(argument.deepCopy(), expression.typeOperand),
                    argument,
                    BrsInvalidLiteral()
                )
            }
            IrTypeOperator.INSTANCEOF -> {
                generateInstanceCheck(argument, expression.typeOperand)
            }
            IrTypeOperator.NOT_INSTANCEOF -> {
                BrsUnaryOp(
                    BrsUnaryOperator.NOT,
                    generateInstanceCheck(argument, expression.typeOperand)
                )
            }
            else -> argument
        }
    }

    /**
     * Generate a type check expression for the given argument and target type.
     *
     * For primitive types, uses typeof() comparison.
     * For class types, checks the __proto chain for the class name.
     */
    private fun generateInstanceCheck(argument: BrsExpression, targetType: IrType): BrsExpression {
        // For primitive types, use typeof
        when {
            targetType.isInt() || targetType.isShort() || targetType.isByte() ->
                return BrsBinaryOp(BrsTypeOf(argument), BrsBinaryOperator.EQ, BrsStringLiteral("roInt"))
            targetType.isLong() ->
                return BrsBinaryOp(BrsTypeOf(argument), BrsBinaryOperator.EQ, BrsStringLiteral("LongInteger"))
            targetType.isFloat() ->
                return BrsBinaryOp(BrsTypeOf(argument), BrsBinaryOperator.EQ, BrsStringLiteral("roFloat"))
            targetType.isDouble() ->
                return BrsBinaryOp(BrsTypeOf(argument), BrsBinaryOperator.EQ, BrsStringLiteral("roDouble"))
            targetType.isBoolean() ->
                return BrsBinaryOp(BrsTypeOf(argument), BrsBinaryOperator.EQ, BrsStringLiteral("roBoolean"))
            targetType.isString() ->
                return BrsBinaryOp(BrsTypeOf(argument), BrsBinaryOperator.EQ, BrsStringLiteral("roString"))
            targetType.isArray() ->
                return BrsBinaryOp(BrsTypeOf(argument), BrsBinaryOperator.EQ, BrsStringLiteral("roArray"))
        }

        // For class types, check the __proto chain
        val classType = targetType.classOrNull?.owner
        val className = if (classType != null) {
            context.getBrsName(classType)
        } else {
            targetType.classFqName?.shortName()?.asString() ?: "Object"
        }

        // Generate: isInstanceOf(argument, "ClassName")
        // This requires the isInstanceOf helper function to be generated
        return BrsFunctionCall(
            BrsIdentifier("isInstanceOf"),
            mutableListOf(argument, BrsStringLiteral(className))
        )
    }

    private fun mapTypeToString(type: IrType): String {
        return when {
            type.isInt() || type.isShort() || type.isByte() -> "roInt"
            type.isLong() -> "LongInteger"
            type.isFloat() -> "roFloat"
            type.isDouble() -> "roDouble"
            type.isBoolean() -> "roBoolean"
            type.isString() -> "roString"
            type.isArray() -> "roArray"
            else -> "roAssociativeArray"
        }
    }

    // ==================== Binary Operations ====================

    override fun visitStringConcatenation(expression: IrStringConcatenation, data: Unit): BrsExpression {
        val parts = expression.arguments.map { it.accept(this, data) }
        return parts.reduce { acc, expr ->
            BrsBinaryOp(acc, BrsBinaryOperator.CONCAT, expr)
        }
    }

    // ==================== Collections ====================

    override fun visitVararg(expression: IrVararg, data: Unit): BrsExpression {
        val elements = expression.elements.map { element ->
            when (element) {
                is IrExpression -> element.accept(this, data)
                is IrSpreadElement -> element.expression.accept(this, data)
                else -> BrsInvalidLiteral()
            }
        }
        return BrsArrayLiteral(elements.toMutableList())
    }

    // ==================== Control Flow ====================

    override fun visitWhen(expression: IrWhen, data: Unit): BrsExpression {
        // Check for ANDAND and OROR origins (logical operators)
        when (expression.origin) {
            IrStatementOrigin.ANDAND -> {
                // a && b is represented as: if (a) b else false
                val left = expression.branches[0].condition.accept(this, data)
                val right = expression.branches[0].result.accept(this, data)
                return BrsBinaryOp(left, BrsBinaryOperator.AND, right)
            }
            IrStatementOrigin.OROR -> {
                // a || b is represented as: if (a) true else b
                val left = expression.branches[0].condition.accept(this, data)
                val right = expression.branches[1].result.accept(this, data)
                return BrsBinaryOp(left, BrsBinaryOperator.OR, right)
            }
            else -> { /* fall through to default handling */ }
        }

        // After BrsWhenExpressionLowering, when expressions in expression context
        // should have been transformed to blocks with temp variables.
        // If we get here with a non-Unit type, fall back to statement transformation
        // wrapped in an immediately-invoked function (IIFE pattern).

        // For Unit-returning when (statement-like), return invalid
        // This shouldn't normally happen in expression context, but handle gracefully
        if (expression.type.isUnit()) {
            return BrsInvalidLiteral() // Unit expressions don't have a value
        }

        // For non-Unit when expressions that weren't lowered, we need to handle them.
        // This can happen in certain edge cases. Use a fallback that works:
        // Generate the if-chain as statements and return invalid (caller should handle)
        // OR: we can still generate BrsConditional and hope for the best (some BRS versions support it)

        // Fallback: Generate BrsConditional (legacy behavior)
        // Note: This may fail on Roku if the when is used in expression context
        val branches = expression.branches

        if (branches.isEmpty()) {
            return BrsInvalidLiteral()
        }

        // Build from the end
        var result: BrsExpression = BrsInvalidLiteral()

        for (i in branches.indices.reversed()) {
            val branch = branches[i]
            val condition = branch.condition.accept(this, data)
            val value = branch.result.accept(this, data)

            result = if (i == branches.lastIndex && branch.isElse()) {
                value
            } else {
                BrsConditional(condition, value, result)
            }
        }

        return result
    }

    private fun IrBranch.isElse(): Boolean {
        return condition is IrConst && (condition as IrConst).value == true
    }

    // ==================== Function Expressions ====================

    override fun visitFunctionExpression(expression: IrFunctionExpression, data: Unit): BrsExpression {
        val function = expression.function

        // Detect captured variables from outer scope
        val capturedVars = parent.detectCapturedVariables(function)

        val parameters = function.valueParameters.map { param ->
            BrsParameter(
                name = param.name.asString(),
                type = parent.mapTypeToBrs(param.type)
            )
        }

        val returnType = parent.mapTypeToBrs(function.returnType)

        // Generate closure object for ALL function expressions
        // BrightScript anonymous functions cannot access outer scope variables,
        // so we create an object with captured variable fields and an invoke method.
        // The invoke method accesses captured variables via m.fieldName
        // For consistency, even lambdas without captures use this pattern so that
        // all lambdas can be invoked uniformly with .invoke()

        // Save previous closure context
        val previousContext = parent.currentClosureContext

        // Set closure context for body transformation (if there are captures)
        if (capturedVars.isNotEmpty()) {
            parent.currentClosureContext = capturedVars
        }

        // Transform body with closure context active (variable accesses will be rewritten)
        val body = function.body?.let { parent.transformBody(it) } ?: BrsBlock()

        // Restore previous context
        parent.currentClosureContext = previousContext

        // Build closure object fields for captured variables
        val entries = mutableListOf<BrsAAEntry>()

        for (capturedVar in capturedVars) {
            val varValue = BrsIdentifier(capturedVar.name)
            val fieldValue = if (capturedVar.isMutable) {
                // Wrap mutable captures in { value: x } for mutation to propagate
                BrsAALiteral(mutableListOf(BrsAAEntry("value", varValue)))
            } else {
                // Read-only captures can be stored directly
                varValue
            }
            entries.add(BrsAAEntry(capturedVar.name, fieldValue))
        }

        // Add the invoke method
        val invokeFunction = BrsAnonymousFunction(parameters.toMutableList(), returnType, body)
        entries.add(BrsAAEntry("invoke", invokeFunction))

        return BrsAALiteral(entries)
    }

    // ==================== Function References ====================

    /**
     * Transform a function reference (::functionName) to a BrightScript closure object.
     *
     * Kotlin: ::myFunction
     * BrightScript: { invoke: function(a, b) return myFunction(a, b) end function }
     *
     * All function references use closure objects for consistent .invoke() calling convention.
     */
    override fun visitFunctionReference(expression: IrFunctionReference, data: Unit): BrsExpression {
        val function = expression.symbol.owner
        val functionName = context.getBrsName(function)

        // Create wrapper function parameters matching the referenced function
        val parameters = function.valueParameters.map { param ->
            BrsParameter(
                name = param.name.asString(),
                type = parent.mapTypeToBrs(param.type)
            )
        }

        // Build the call arguments from the wrapper parameters
        val callArgs: MutableList<BrsExpression> = parameters.map { BrsIdentifier(it.name) as BrsExpression }.toMutableList()

        // Handle bound receiver if present (obj::method)
        val boundReceiver = expression.dispatchReceiver ?: expression.extensionReceiver
        val call = if (boundReceiver != null) {
            // For bound method references, call as method on the receiver
            val receiverExpr = boundReceiver.accept(this, data)
            val methodName = parent.sanitizeMethodName(function.name.asString())
            BrsMethodCall(receiverExpr, methodName, callArgs)
        } else {
            // For unbound function references, call the global function
            BrsFunctionCall(BrsIdentifier(functionName), callArgs)
        }

        // Determine return type
        val returnType = if (function.returnType.isUnit() || function.returnType.isNothing()) {
            BrsType.VOID
        } else {
            parent.mapTypeToBrs(function.returnType)
        }

        // Build the wrapper body
        val body = if (returnType == BrsType.VOID) {
            // For void functions, just call without return
            BrsBlock(mutableListOf(BrsExpressionStatement(call)))
        } else {
            // For non-void functions, return the result
            BrsBlock(mutableListOf(BrsReturn(call)))
        }

        // Build closure object with invoke method
        val invokeFunction = BrsAnonymousFunction(parameters.toMutableList(), returnType, body)
        val entries = mutableListOf(BrsAAEntry("invoke", invokeFunction))

        return BrsAALiteral(entries)
    }

    /**
     * Transform a property reference (::propertyName) to a BrightScript anonymous function wrapper.
     *
     * Kotlin: ::propertyName or obj::propertyName
     * BrightScript: function() return propertyName end function
     *             or function() return obj.propertyName end function
     */
    override fun visitPropertyReference(expression: IrPropertyReference, data: Unit): BrsExpression {
        val property = expression.symbol.owner
        val propertyName = property.name.asString()

        // Handle bound receiver if present (obj::property)
        val boundReceiver = expression.dispatchReceiver ?: expression.extensionReceiver

        // Build the property access expression
        val propertyAccess = if (boundReceiver != null) {
            val receiverExpr = boundReceiver.accept(this, data)
            BrsDotAccess(receiverExpr, propertyName)
        } else {
            // For top-level properties, access directly
            BrsIdentifier(propertyName)
        }

        // Get the property type for the return type
        val returnType = property.getter?.returnType?.let { parent.mapTypeToBrs(it) }
            ?: parent.mapTypeToBrs(property.backingField?.type ?: context.irBuiltIns.anyType)

        // Create a getter wrapper function
        val body = BrsBlock(mutableListOf(BrsReturn(propertyAccess)))

        return BrsAnonymousFunction(mutableListOf(), returnType, body)
    }

    // ==================== Composite ====================

    override fun visitComposite(expression: IrComposite, data: Unit): BrsExpression {
        // Composite expressions are sequences - return the last one
        val statements = expression.statements
        return if (statements.isEmpty()) {
            BrsInvalidLiteral()
        } else {
            val last = statements.last()
            if (last is IrExpression) {
                last.accept(this, data)
            } else {
                BrsInvalidLiteral()
            }
        }
    }

    override fun visitBlock(expression: IrBlock, data: Unit): BrsExpression {
        // Handle increment/decrement blocks - when used as statement (value discarded),
        // we need to output just the setter/assignment with inlined temp variable
        // Block structure: [IrVariable(<unary>) = getter(), setter(<unary>+1) or IrSetValue, IrGetValue(<unary>)]
        if (expression.origin == IrStatementOrigin.POSTFIX_INCR ||
            expression.origin == IrStatementOrigin.POSTFIX_DECR ||
            expression.origin == IrStatementOrigin.PREFIX_INCR ||
            expression.origin == IrStatementOrigin.PREFIX_DECR) {

            val statements = expression.statements

            // Find the temp variable and its initializer
            val tempVar = statements.filterIsInstance<IrVariable>().firstOrNull {
                it.name.asString().startsWith("<") && it.name.asString().endsWith(">")
            }
            val tempVarInitializer = tempVar?.initializer

            // For property increment: Find setter call (IrCall that is NOT the last statement)
            for (stmt in statements) {
                if (stmt is IrCall && stmt !== statements.last()) {
                    // This is the setter call - transform it with inlined temp var
                    if (tempVar != null && tempVarInitializer != null) {
                        // Register temp var substitution for this transformation
                        parent.pushTempVarSubstitution(tempVar.symbol, tempVarInitializer)
                        try {
                            return stmt.accept(this, data)
                        } finally {
                            parent.popTempVarSubstitution(tempVar.symbol)
                        }
                    }
                    return stmt.accept(this, data)
                }
            }

            // For local var increment: Find IrSetValue or IrSetField
            for (stmt in statements) {
                if ((stmt is IrSetValue || stmt is IrSetField) && stmt !== statements.last()) {
                    // Transform assignment with inlined temp var
                    if (tempVar != null && tempVarInitializer != null) {
                        parent.pushTempVarSubstitution(tempVar.symbol, tempVarInitializer)
                        try {
                            return parent.transformExpression(stmt)
                        } finally {
                            parent.popTempVarSubstitution(tempVar.symbol)
                        }
                    }
                    return parent.transformExpression(stmt)
                }
            }
        }

        // Default: Block expressions return the last statement's value
        val statements = expression.statements
        return if (statements.isEmpty()) {
            BrsInvalidLiteral()
        } else {
            val last = statements.last()
            if (last is IrExpression) {
                last.accept(this, data)
            } else {
                BrsInvalidLiteral()
            }
        }
    }
}
