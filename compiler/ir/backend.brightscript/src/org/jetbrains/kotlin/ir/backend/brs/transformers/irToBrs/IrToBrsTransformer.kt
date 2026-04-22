/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.transformers.irToBrs

import org.jetbrains.kotlin.backend.common.capturedFields
import org.jetbrains.kotlin.backend.common.lower.AbstractSuspendFunctionsLowering
import org.jetbrains.kotlin.backend.common.lower.BOUND_VALUE_PARAMETER
import org.jetbrains.kotlin.backend.common.lower.BOUND_RECEIVER_PARAMETER
import org.jetbrains.kotlin.backend.common.lower.WebCallableReferenceLowering
import org.jetbrains.kotlin.brs.backend.ast.*
import org.jetbrains.kotlin.brs.backend.ast.parser.parseBrightScriptStatements
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.ir.backend.brs.BrsIntrinsics
import org.jetbrains.kotlin.ir.backend.brs.lower.BrsCodeOutliningLowering
import org.jetbrains.kotlin.ir.backend.brs.lower.BrsDeclarationOrigin
import org.jetbrains.kotlin.ir.backend.brs.lower.BrsInlineCallTransformer
import org.jetbrains.kotlin.ir.backend.brs.lower.coroutines.BrsStatementOrigins
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.backend.brs.BrsIrBackendContext
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.fileOrNull
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.getAnnotation
import org.jetbrains.kotlin.ir.util.getPackageFragment
import org.jetbrains.kotlin.ir.util.isFunction
import org.jetbrains.kotlin.ir.util.isInterface
import org.jetbrains.kotlin.ir.util.isNullable
import org.jetbrains.kotlin.ir.util.isTypeParameter
import org.jetbrains.kotlin.ir.util.isUnsigned
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.name.BrsStandardClassIds
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrTypeParameterSymbol
import org.jetbrains.kotlin.ir.symbols.IrValueSymbol
import org.jetbrains.kotlin.ir.visitors.IrVisitor
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import java.io.File

/**
 * Transforms Kotlin IR to BrightScript AST.
 *
 * This is the core transformation that converts lowered Kotlin IR
 * into BrightScript-specific AST nodes that can then be rendered
 * to text.
 */
class IrToBrsTransformer(
    private val context: BrsIrBackendContext
) : IrVisitor<BrsNode?, Unit>() {

    private val statementTransformer = IrStatementToBrsTransformer(this, context)
    private val expressionTransformer = IrExpressionToBrsTransformer(this, context)

    // Expose statement visitor for when-lowered block handling
    val statementVisitor: IrStatementToBrsTransformer get() = statementTransformer
    internal val inlineCallTransformer = BrsInlineCallTransformer(context)

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
     * Set of variable symbols that are shared (captured by closures and mutable).
     * These variables need to be boxed in {value: x} wrappers at declaration time,
     * and all accesses (both inside and outside closures) need to use .value.
     * This is set per-function before transforming the function body.
     */
    internal var sharedVariables: Set<IrValueSymbol> = emptySet()

    /**
     * Current lambda extension receiver symbol.
     * When non-null, we're inside a lambda with an extension receiver and need to rewrite
     * references to this receiver as 'm' (the first parameter of the lambda).
     */
    internal var currentLambdaExtensionReceiver: IrValueSymbol? = null

    /**
     * Flag to indicate we're inside a constructor body.
     * When true, '<this>' references should map to 'this' (local variable) instead of 'm'.
     */
    internal var isInConstructorBody: Boolean = false

    /**
     * Flag to indicate we're inside a SceneGraph component class transformation.
     * When true:
     * - 'top', 'global', and 'm' property accesses compile to m.top, m.global, m
     * - 'this' references are not used (component doesn't create an object)
     */
    internal var isInComponentContext: Boolean = false

    /**
     * The current SceneGraph component class being transformed, if any.
     * Used to determine which property accesses should compile to m.<name>.
     */
    internal var currentComponentClass: IrClass? = null

    /**
     * Flag to indicate we're inside a lambda that was created in component context.
     * When true, component state accesses use m._componentM instead of m directly,
     * because 'm' inside the lambda refers to the closure object, not the component.
     */
    internal var isInComponentLambda: Boolean = false

    /**
     * The current file path being transformed.
     * Used for dependency tracking - we record which files each source depends on.
     */
    internal var currentFilePath: String? = null

    /**
     * Temp variable substitution map for increment/decrement inlining.
     * When transforming increment blocks, we need to inline the temp variable's
     * initializer instead of outputting a reference to the temp var.
     */
    private val tempVarSubstitutions = mutableMapOf<IrValueSymbol, IrExpression?>()

    /**
     * Map from IR temp var symbols to generated BRS variable names.
     * Used when increment/decrement blocks are used as expressions.
     */
    private val tempVarNames = mutableMapOf<IrValueSymbol, String>()

    /**
     * Set of IR loops that have been transformed to while loops in the output.
     * When a for-each loop is transformed to a while loop (e.g., Strategy 4 - iterator protocol),
     * any break statements inside should generate "exit while" instead of "exit for".
     * The key is the IrLoop instance (the loop that IrBreak.loop references).
     */
    internal val loopsTransformedToWhile = mutableSetOf<IrLoop>()

    /**
     * Counter tracking how many FOR_LOOP blocks we're currently inside that will be
     * transformed to while loops. When > 0, any break statements with FOR_LOOP_INNER_WHILE
     * origin should generate "exit while" instead of "exit for".
     *
     * This is needed because after inlining, the IrBreak.loop may reference a different
     * IrWhileLoop instance than what we traverse (inlining creates copies), so we can't
     * rely on object identity in loopsTransformedToWhile.
     */
    internal var forLoopToWhileNestingDepth = 0

    /**
     * Maps IR loops to their break flag variable names.
     * When continue is not supported, loops are wrapped in an inner while(true) loop.
     * Break statements must set a flag and exit the inner loop, then the outer loop
     * checks this flag and exits if set.
     */
    internal val loopBreakFlags = mutableMapOf<IrLoop, String>()

    /**
     * Set of IR loops that are currently being transformed with a continue wrapper.
     * This is used during the transformation to know which loop a continue/break
     * should target. We add the loop before transforming its body and remove it after.
     * IMPORTANT: This only applies while we're actively inside the body transformation.
     */
    internal val loopsWithContinueWrapper = mutableSetOf<IrLoop>()

    /**
     * Stack of loops currently being transformed with continue wrappers.
     * A loop is pushed when we start transforming its body and popped when done.
     * IrContinue/IrBreak should only be transformed to exit while if their target
     * loop is on this stack (meaning we're inside its body transformation).
     */
    internal val continueWrapperLoopStack = mutableListOf<IrLoop>()

    /**
     * Stack of returnable block exit flag variable names.
     * When a returnable block contains while loops that have returns targeting the block,
     * we can't use simple "exit while" (it would exit the inner loop, not the block wrapper).
     * Instead, we use a flag-based approach: set the flag and exit while, then check the flag
     * after each inner while loop.
     *
     * The stack supports nested returnable blocks. The top of the stack is the innermost
     * returnable block currently being transformed.
     */
    internal val returnableBlockFlagStack = mutableListOf<String>()

    /**
     * Counter for generating unique returnable block flag names.
     */
    internal var returnableBlockFlagCounter = 0

    /**
     * Counter for generating unique temp variable names.
     */
    private var tempIdCounter = 0

    fun nextTempId(): Int {
        return tempIdCounter++
    }

    fun pushTempVarSubstitution(symbol: IrValueSymbol, initializer: IrExpression?) {
        tempVarSubstitutions[symbol] = initializer
    }

    fun popTempVarSubstitution(symbol: IrValueSymbol) {
        tempVarSubstitutions.remove(symbol)
        tempVarNames.remove(symbol)
    }

    fun getTempVarSubstitution(symbol: IrValueSymbol): IrExpression? {
        return tempVarSubstitutions[symbol]
    }

    fun setTempVarName(symbol: IrValueSymbol, name: String) {
        tempVarNames[symbol] = name
    }

    fun getTempVarName(symbol: IrValueSymbol): String? {
        return tempVarNames[symbol]
    }

    // ==================== Function Call with Dependency Recording ====================

    /**
     * Create a BrsFunctionCall and record the dependency.
     * This is the central place where all function calls are created, ensuring
     * that dependency tracking captures every function we emit.
     *
     * @param functionName The BrightScript function name being called
     * @param args The arguments to pass to the function
     * @return A BrsFunctionCall node
     */
    fun createFunctionCall(functionName: String, args: MutableList<BrsExpression>): BrsFunctionCall {
        context.recordFunctionDependency(functionName)
        return BrsFunctionCall(BrsIdentifier(functionName), args)
    }

    /**
     * Create a BrsFunctionCall with no arguments and record the dependency.
     */
    fun createFunctionCall(functionName: String): BrsFunctionCall {
        return createFunctionCall(functionName, mutableListOf())
    }

    /**
     * Maps IR value symbols to unique BrightScript variable names.
     * This is needed because Kotlin IR can have multiple variables with the same name
     * (distinguished by their symbol), but BrightScript uses a single namespace where
     * variable names must be unique to avoid collisions.
     *
     * Example: After inlining `this.toUInt().plus(other.toUInt())`, the IR may have:
     *   val tmp0 = this          // symbol A
     *   val tmp0 = tmp_ret_0     // symbol B (different symbol, same name!)
     *   val tmp0 = other         // symbol C (yet another symbol)
     *
     * Without renaming, these all map to `tmp0` in BrightScript, causing the
     * second and third assignments to overwrite the first value.
     */
    private val symbolToUniqueName = mutableMapOf<IrValueSymbol, String>()

    /**
     * Set of variable names already used in the current function.
     * Used to detect collisions and generate unique suffixes.
     */
    private val usedVariableNames = mutableSetOf<String>()

    /**
     * Clears the variable naming state. Should be called at the start of each function.
     */
    fun resetVariableNaming() {
        symbolToUniqueName.clear()
        usedVariableNames.clear()
    }

    /**
     * Gets or creates a unique BrightScript variable name for the given IR symbol.
     * If this symbol has already been assigned a name, returns it.
     * If this is a new symbol, generates a unique name (possibly with a numeric suffix
     * to avoid collision with existing names) and registers it.
     */
    fun getUniqueVariableName(symbol: IrValueSymbol, baseName: String): String {
        // Check if we already have a name for this exact symbol
        symbolToUniqueName[symbol]?.let { return it }

        // Generate a unique name
        var uniqueName = baseName
        var suffix = 1
        while (uniqueName in usedVariableNames) {
            uniqueName = "${baseName}_${suffix++}"
        }

        // Register the mapping
        symbolToUniqueName[symbol] = uniqueName
        usedVariableNames.add(uniqueName)

        return uniqueName
    }

    /**
     * Gets the unique name for a symbol that should already have been declared.
     * Returns the base name if the symbol wasn't registered (for parameters, etc.)
     */
    fun getVariableName(symbol: IrValueSymbol, baseName: String): String {
        return symbolToUniqueName[symbol] ?: baseName
    }

    /**
     * Checks if the given IR element contains any IrContinue statements targeting the specified loop.
     * Used to determine if a loop needs to be wrapped with a continue-simulation pattern
     * when native continue is not supported.
     */
    fun containsContinueFor(element: IrElement?, targetLoop: IrLoop): Boolean {
        if (element == null) return false
        var found = false
        element.acceptVoid(object : IrVisitorVoid() {
            override fun visitElement(element: IrElement) {
                if (!found) element.acceptChildrenVoid(this)
            }
            override fun visitContinue(jump: IrContinue) {
                if (jump.loop === targetLoop) {
                    found = true
                }
            }
        })
        return found
    }

    /**
     * Checks if the given IR element contains any IrBreak statements targeting the specified loop.
     * Used to determine if we need a break flag variable when simulating continue.
     */
    fun containsBreakFor(element: IrElement?, targetLoop: IrLoop): Boolean {
        if (element == null) return false
        var found = false
        element.acceptVoid(object : IrVisitorVoid() {
            override fun visitElement(element: IrElement) {
                if (!found) element.acceptChildrenVoid(this)
            }
            override fun visitBreak(jump: IrBreak) {
                if (jump.loop === targetLoop) {
                    found = true
                }
            }
        })
        return found
    }

    /**
     * Hoisted statements from when-expression lowered blocks.
     * These need to be emitted before the expression that uses them.
     */
    // Stack of hoisted statement scopes for proper nesting of when-lowered blocks
    private val hoistedScopes = mutableListOf<MutableList<BrsStatement>>()

    init {
        hoistedScopes.add(mutableListOf()) // Global scope
    }

    fun pushHoistedScope() {
        hoistedScopes.add(mutableListOf())
    }

    fun popHoistedScope(): List<BrsStatement> {
        if (hoistedScopes.size <= 1) {
            // Don't pop the global scope - just return and clear it
            val result = hoistedScopes.last().toList()
            hoistedScopes.last().clear()
            return result
        }
        return hoistedScopes.removeAt(hoistedScopes.lastIndex)
    }

    fun addHoistedStatement(stmt: BrsStatement) {
        hoistedScopes.last().add(stmt)
    }

    fun takeHoistedStatements(): List<BrsStatement> {
        val scope = hoistedScopes.last()
        val result = scope.toList()
        scope.clear()
        return result
    }

    fun hasHoistedStatements(): Boolean = hoistedScopes.last().isNotEmpty()

    fun clearHoistedScopes() {
        hoistedScopes.clear()
        hoistedScopes.add(mutableListOf()) // Reset to global scope
    }

    /**
     * Get the correct BrightScript expression to access the component's 'm' reference.
     *
     * When inside a lambda in component context, 'm' refers to the closure object,
     * not the component. In this case, we use 'm._componentM' to access the captured
     * component reference. Otherwise, we use 'm' directly.
     */
    fun getComponentMRef(): BrsExpression {
        return if (isInComponentLambda) {
            // Inside a lambda, access the captured component m via m._componentM
            BrsDotAccess(BrsMRef(), "_componentM")
        } else {
            // Direct m access when not in a lambda
            BrsMRef()
        }
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
        // Also include extension receiver parameter so it's not treated as captured
        function.extensionReceiverParameter?.let { declaredSymbols.add(it.symbol) }

        // Walk the function body to find declared and referenced variables
        function.body?.acceptVoid(object : IrVisitorVoid() {
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }

            // Skip nested function expressions - they have their own capture detection
            // We only want to detect variables captured directly by this function,
            // not variables used inside nested lambdas (which are their parameters)
            override fun visitFunctionExpression(expression: IrFunctionExpression) {
                // Don't recurse into nested function expressions
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
                        // Skip parameters added by LocalDeclarationsLowering
                        // These are synthetic parameters for captured values and are already
                        // available as function parameters, not outer-scope captures
                        if (owner is IrValueParameter &&
                            (owner.origin == BOUND_VALUE_PARAMETER ||
                             owner.origin == BOUND_RECEIVER_PARAMETER)) {
                            expression.acceptChildrenVoid(this)
                            return
                        }
                        // Skip parameters from functions that are not ancestors of this lambda
                        // Parameters from non-ancestor functions come from inlined code
                        if (owner is IrValueParameter) {
                            val paramParent = owner.parent as? IrFunction
                            if (paramParent != null) {
                                // Check if paramParent is an ancestor of this function
                                var parent: IrDeclarationParent? = function.parent
                                var isAncestor = false
                                while (parent != null) {
                                    if (parent === paramParent) {
                                        isAncestor = true
                                        break
                                    }
                                    parent = (parent as? IrDeclaration)?.parent
                                }
                                if (!isAncestor) {
                                    // This parameter is from a non-ancestor function (inlined code)
                                    expression.acceptChildrenVoid(this)
                                    return
                                }

                                // Skip extension receiver of the IMMEDIATE parent (the lambda's own receiver)
                                // But DO capture extension receivers from ANCESTOR functions - these must be captured
                                // so the closure can access the outer function's receiver
                                if (paramParent.extensionReceiverParameter === owner && paramParent === function) {
                                    expression.acceptChildrenVoid(this)
                                    return
                                }

                                // Skip captured receiver parameters (names starting with $this$)
                                // These are value parameters created by inline function expansion to hold
                                // the extension receiver. They're accessible via m in BrightScript.
                                val paramName = owner.name.asString()
                                if (paramName.startsWith("\$this\$")) {
                                    expression.acceptChildrenVoid(this)
                                    return
                                }
                            }
                        }
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
                    // Skip parameters added by LocalDeclarationsLowering
                    val owner = symbol.owner
                    if (owner is IrValueParameter &&
                        (owner.origin == BOUND_VALUE_PARAMETER ||
                         owner.origin == BOUND_RECEIVER_PARAMETER)) {
                        expression.acceptChildrenVoid(this)
                        return
                    }
                    // Mark as mutated
                    referencedSymbols[symbol] = true
                }
                expression.acceptChildrenVoid(this)
            }
        })

        // Build the captured variables list
        // Deduplicate by name - multiple IR symbols may map to the same BrightScript name
        // (e.g., multiple receiver symbols all become "this")
        return referencedSymbols.map { (symbol, isMutated) ->
            val owner = symbol.owner
            val isMutable = when (owner) {
                is IrVariable -> owner.isVar || isMutated
                else -> isMutated
            }
            CapturedVariable(
                symbol = symbol,
                name = sanitizeParameterName(owner.name.asString()),
                isMutable = isMutable
            )
        }.distinctBy { it.name }
    }

    /**
     * Check if a variable symbol is captured in the current closure context.
     */
    fun getCapturedVariable(symbol: IrValueSymbol): CapturedVariable? {
        return currentClosureContext?.find { it.symbol == symbol }
    }

    /**
     * Detect all shared variables in a function body.
     * A shared variable is a mutable variable (var) that is captured by at least one closure.
     * These variables need to be boxed in {value: x} wrappers so mutations inside closures
     * are visible outside and vice versa.
     *
     * @param body The function body to analyze
     * @return Set of variable symbols that need to be shared (boxed)
     */
    fun detectSharedVariables(body: IrBody): Set<IrValueSymbol> {
        val sharedVars = mutableSetOf<IrValueSymbol>()

        // Walk the body to find all function expressions (closures) and local classes
        body.acceptVoid(object : IrVisitorVoid() {
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }

            override fun visitFunctionExpression(expression: IrFunctionExpression) {
                // Detect captured variables for this closure
                val capturedVars = detectCapturedVariables(expression.function)

                // Add mutable captured variables to the shared set
                for (captured in capturedVars) {
                    if (captured.isMutable) {
                        sharedVars.add(captured.symbol)
                    }
                }

                // Continue searching for nested closures within this one
                expression.function.body?.acceptVoid(this)
            }

            override fun visitClass(declaration: IrClass) {
                // Handle local classes (including anonymous object expressions)
                // These also capture outer variables
                if (declaration.visibility == org.jetbrains.kotlin.descriptors.DescriptorVisibilities.LOCAL) {
                    val capturedVars = detectCapturedVariablesInClass(declaration)
                    for (captured in capturedVars) {
                        if (captured.isMutable) {
                            sharedVars.add(captured.symbol)
                        }
                    }
                }

                // Continue searching for nested closures within this class
                declaration.acceptChildrenVoid(this)
            }
        })

        return sharedVars
    }

    /**
     * Detect variables captured by a local class from outer scopes.
     * Similar to detectCapturedVariables but for classes instead of functions.
     */
    private fun detectCapturedVariablesInClass(irClass: IrClass): List<CapturedVariable> {
        val declaredSymbols = mutableSetOf<IrValueSymbol>()
        val referencedSymbols = mutableMapOf<IrValueSymbol, Boolean>() // symbol -> isMutated

        // Collect symbols declared within the class (parameters, local variables, etc.)
        // Note: Class fields are not value symbols, so they don't need to be excluded

        // Walk the class members to find referenced variables
        irClass.declarations.forEach { declaration ->
            when (declaration) {
                is IrSimpleFunction -> {
                    // Add function parameters as declared (they're local to the function)
                    declaration.valueParameters.forEach { declaredSymbols.add(it.symbol) }
                    declaration.extensionReceiverParameter?.let { declaredSymbols.add(it.symbol) }
                    declaration.dispatchReceiverParameter?.let { declaredSymbols.add(it.symbol) }

                    // Walk the function body
                    declaration.body?.acceptVoid(object : IrVisitorVoid() {
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
                                val owner = symbol.owner
                                if (owner is IrVariable || owner is IrValueParameter) {
                                    // Skip parameters added by LocalDeclarationsLowering
                                    if (owner is IrValueParameter &&
                                        (owner.origin == BOUND_VALUE_PARAMETER ||
                                         owner.origin == BOUND_RECEIVER_PARAMETER)) {
                                        expression.acceptChildrenVoid(this)
                                        return
                                    }
                                    // Mark as referenced (not mutated)
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
                                val owner = symbol.owner
                                if (owner is IrVariable || owner is IrValueParameter) {
                                    // Skip parameters added by LocalDeclarationsLowering
                                    if (owner is IrValueParameter &&
                                        (owner.origin == BOUND_VALUE_PARAMETER ||
                                         owner.origin == BOUND_RECEIVER_PARAMETER)) {
                                        expression.acceptChildrenVoid(this)
                                        return
                                    }
                                    // Mark as mutated
                                    referencedSymbols[symbol] = true
                                }
                            }
                            expression.acceptChildrenVoid(this)
                        }

                        // Skip nested function expressions - they have their own capture detection
                        override fun visitFunctionExpression(expression: IrFunctionExpression) {
                            // Don't recurse into nested function expressions
                        }

                        // Skip nested classes
                        override fun visitClass(declaration: IrClass) {
                            // Don't recurse into nested classes
                        }
                    })
                }
                is IrConstructor -> {
                    // Add constructor parameters as declared
                    declaration.valueParameters.forEach { declaredSymbols.add(it.symbol) }
                }
                else -> {}
            }
        }

        // Build the captured variables list
        return referencedSymbols.map { (symbol, isMutated) ->
            val owner = symbol.owner
            val isMutable = when (owner) {
                is IrVariable -> owner.isVar || isMutated
                else -> isMutated
            }
            CapturedVariable(
                symbol = symbol,
                name = sanitizeParameterName(owner.name.asString()),
                isMutable = isMutable
            )
        }.distinctBy { it.name }
    }

    // ==================== Entry Points ====================

    /**
     * Transform an IR file to a BrightScript program.
     */
    fun transformFile(irFile: IrFile): BrsProgram {
        val declarations = mutableListOf<BrsDeclaration>()
        val statements = mutableListOf<BrsStatement>()

        // Track current file for dependency collection
        currentFilePath = irFile.path

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

        // Generate IO worker registration function if this file has any workers
        val fileWorkers = context.ioWorkerRegistrations.filter { (_, func) ->
            func.parent === irFile
        }
        if (fileWorkers.isNotEmpty()) {
            val registrationFunction = generateIOWorkerRegistrationFunction(irFile, fileWorkers)
            declarations.add(registrationFunction)
        }

        return BrsProgram(declarations, statements)
    }

    /**
     * Generate a function that registers all IO workers in a file.
     *
     * Generates:
     * ```brightscript
     * sub __registerIOWorkers_FileName()
     *     IOWorkerRegistry_register_k_("workerName1", __ioWorker_...)
     *     IOWorkerRegistry_register_k_("workerName2", __ioWorker_...)
     * end sub
     * ```
     *
     * Users should call this function from their Scene's init block, before using
     * withContext(Dispatchers.IO).
     */
    private fun generateIOWorkerRegistrationFunction(
        irFile: IrFile,
        workers: Map<String, IrSimpleFunction>
    ): BrsDeclaration {
        val fileName = irFile.name.removeSuffix(".kt")
        val functionName = "__registerIOWorkers_${fileName}_k_"

        val registrationStatements = mutableListOf<BrsStatement>()
        for ((workerName, workerFunction) in workers) {
            // Get the BrightScript name for the worker function
            val brsFunctionName = context.getBrsName(workerFunction)

            // Emit: IOWorkerRegistry_register_k_("workerName", workerFunctionRef)
            // In BrightScript, function references are just the function name as identifier
            registrationStatements.add(
                BrsExpressionStatement(
                    createFunctionCall(
                        "IOWorkerRegistry_register_Str_k_",
                        mutableListOf(
                            BrsStringLiteral(workerName),
                            BrsIdentifier(brsFunctionName)
                        )
                    )
                )
            )
        }

        return BrsSub(
            name = functionName,
            parameters = mutableListOf(),
            body = BrsBlock(registrationStatements)
        )
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

        // Clear any leftover hoisted statements from previous function transformations
        clearHoistedScopes()

        // Reset variable naming state for this function
        // This ensures each function gets fresh unique names and doesn't collide with other functions
        resetVariableNaming()

        val name = context.getBrsName(irFunction)

        // Build parameter list, starting with extension receiver if present
        val allParameters = mutableListOf<BrsParameter>()

        // Add extension receiver as first parameter if this is an extension function
        // This is needed because in BrightScript there's no receiver concept - extension
        // functions are compiled as regular functions with the receiver as first argument.
        // The name "m" matches what visitGetValue outputs for <this> references.
        if (irFunction is IrSimpleFunction) {
            irFunction.extensionReceiverParameter?.let { receiver ->
                allParameters.add(BrsParameter(
                    name = "m",
                    type = mapTypeToBrs(receiver.type),
                    defaultValue = null
                ))
            }
        }

        // Add value parameters
        val rawParameters = irFunction.valueParameters.map { param ->
            BrsParameter(
                name = sanitizeParameterName(param.name.asString()),
                type = mapTypeToBrs(param.type),
                defaultValue = param.defaultValue?.expression?.let { transformExpression(it) }
            )
        }
        allParameters.addAll(rawParameters)

        val parameters = normalizeParametersForBrs(deduplicateParameterNames(allParameters))

        // Detect shared variables (mutable vars captured by closures) before transforming body
        // Save and restore to handle nested function transformations
        val previousSharedVariables = sharedVariables
        // First check if BrsSharedVariableDetectionLowering already detected shared variables for this function
        // (this runs before local class extraction, so it can detect variables captured by local classes)
        // If not found, fall back to detecting them now (for lambdas and inline functions)
        sharedVariables = context.sharedVariablesByFunction[irFunction.symbol]
            ?: irFunction.body?.let { detectSharedVariables(it) }
            ?: emptySet()

        // Check if this function has @BrsInline - use parsed code instead of IR body
        val inlineInfo = context.inlineFunctionInfo[irFunction.symbol] as? BrsCodeOutliningLowering.BrsInlineInfo
        var body = if (inlineInfo != null) {
            // Use the parsed @BrsInline code
            BrsBlock(inlineInfo.parsedStatements.toMutableList())
        } else {
            irFunction.body?.let { transformBody(it) } ?: BrsBlock()
        }

        // Restore previous shared variables
        sharedVariables = previousSharedVariables

        // Check for any remaining hoisted statements and prepend them to the body
        val remainingHoisted = takeHoistedStatements()
        if (remainingHoisted.isNotEmpty()) {
            val combinedStatements = remainingHoisted.toMutableList()
            combinedStatements.addAll(body.statements)
            body = BrsBlock(combinedStatements)
        }

        // Generate runtime checks for parameters with default values
        // When caller passes `invalid`, substitute with the actual default value
        // BrightScript's default parameter syntax only applies when args are omitted, not when invalid is passed
        val defaultValueChecks = mutableListOf<BrsStatement>()
        irFunction.valueParameters.forEach { param ->
            val defaultExpr = param.defaultValue?.expression
            if (defaultExpr != null) {
                val paramName = sanitizeParameterName(param.name.asString())
                val defaultValue = transformExpression(defaultExpr)
                defaultValueChecks.add(
                    BrsIf(
                        condition = BrsBinaryOp(
                            BrsIdentifier(paramName),
                            BrsBinaryOperator.EQ,
                            BrsIdentifier("invalid")
                        ),
                        thenBranch = BrsExpressionStatement(
                            BrsBinaryOp(
                                BrsIdentifier(paramName),
                                BrsBinaryOperator.EQ,
                                defaultValue
                            )
                        ),
                        elseBranch = null
                    )
                )
            }
        }
        if (defaultValueChecks.isNotEmpty()) {
            val combinedStatements = defaultValueChecks.toMutableList()
            combinedStatements.addAll(body.statements)
            body = BrsBlock(combinedStatements)
        }

        val returnType = mapTypeToBrs(irFunction.returnType)

        // Check if this is a suspend lambda's invoke method that needs to return doResume result.
        // These methods have their bodies rewritten to return the result of create().doResume(),
        // so they must be functions even though the Kotlin return type is Unit.
        //
        // Detection: The invoke method is in a LAMBDA_IMPL class and is a suspend function.
        // After the coroutine lowering, the invoke method's body is replaced to call create().doResume(),
        // which returns a value, so it needs to be a BrightScript function, not a sub.
        val isSuspendLambdaInvoke = irFunction is IrSimpleFunction &&
            irFunction.isSuspend &&
            irFunction.name.asString() == "invoke" &&
            irFunction.parentClassOrNull?.origin == WebCallableReferenceLowering.LAMBDA_IMPL

        // Functions with Unit return type become subs, EXCEPT for suspend lambda invoke methods
        // Note: Functions returning Nothing (e.g., lambdas with non-local returns, throw expressions)
        // should still be functions, not subs, because they may implement interfaces expecting return values
        return if (irFunction.returnType.isUnit() && !isSuspendLambdaInvoke) {
            BrsSub(name, parameters.toMutableList(), body)
        } else {
            // For Nothing/Unit suspend invoke, use Dynamic as the BrightScript return type
            val effectiveReturnType = when {
                irFunction.returnType.isNothing() -> BrsType.DYNAMIC
                irFunction.returnType.isUnit() -> BrsType.DYNAMIC  // suspend lambda invoke
                else -> returnType
            }
            BrsFunction(name, parameters.toMutableList(), effectiveReturnType, body)
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
        // Skip top-level Layout classes (e.g., MainScreen_Layout)
        // These can be:
        // 1. FIR-generated (from SceneGraphLayoutGenerator - legacy)
        // 2. Gradle-generated stubs (from GenerateLayoutStubsTask)
        //
        // The actual BrightScript implementation is generated via generateLayoutAccessorClass()
        // when processing the owner component class (which has the @SGLayout annotation).
        if (isLayoutStubClass(irClass)) {
            return
        }

        // Handle object singletons specially
        if (irClass.kind == ClassKind.OBJECT) {
            // Skip @BrsConstant objects - their properties are inlined at usage sites
            if (context.isConstantObject(irClass)) {
                return
            }
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

        // Handle SceneGraph component classes specially
        if (context.intrinsics.isSceneGraphComponent(irClass)) {
            transformSceneGraphComponent(irClass, declarations)
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
     * Transform a SceneGraph component class to BrightScript.
     *
     * Component classes are compiled differently from regular classes:
     * - No constructor function is generated (component lifecycle is managed by SceneGraph)
     * - The init {} block compiles to BrightScript's sub init()
     * - Property accessors for inherited properties (top, global, m) are NOT generated
     * - Member functions become top-level functions in the component script
     *
     * Example input:
     * ```kotlin
     * class MyComponent : SceneComponent() {
     *     init {
     *         top.setFocus(true)
     *     }
     *     fun handleButton() { println("pressed") }
     * }
     * ```
     *
     * Example output:
     * ```brightscript
     * sub init()
     *     m.top.setFocus(true)
     * end sub
     *
     * sub handleButton_k_()
     *     println_AnyN_k_("pressed")
     * end sub
     * ```
     */
    private fun transformSceneGraphComponent(
        irClass: IrClass,
        declarations: MutableList<BrsDeclaration>
    ) {
        // Set component context flags
        val previousInComponent = isInComponentContext
        val previousComponentClass = currentComponentClass
        isInComponentContext = true
        currentComponentClass = irClass

        try {
            // Extract layout info from companion @SGLayout function (new pattern)
            // Falls back to legacy @SGLayout property pattern for backwards compatibility
            val layoutInfo = extractLayoutInfoFromCompanion(irClass)
                ?: irClass.declarations.filterIsInstance<IrProperty>()
                    .find { context.intrinsics.hasSGLayoutAnnotation(it) }
                    ?.let { extractLayoutInfo(irClass, it) }

            // Generate layout accessor class if we have @SGLayout
            if (layoutInfo != null) {
                declarations.addAll(generateLayoutAccessorClass(layoutInfo))
            }

            // Find the primary constructor to extract init block content
            val primaryConstructor = irClass.declarations.filterIsInstance<IrConstructor>().firstOrNull()

            // Generate sub init() from the constructor body (which contains init block code)
            if (primaryConstructor != null) {
                val initSub = transformComponentInitBlock(irClass, primaryConstructor, layoutInfo)
                if (initSub != null) {
                    declarations.add(initSub)
                }
            }

            // Generate member functions (but not inherited property accessors like get_top)
            // Note: onKeyEvent overrides ARE included here - they generate the mangled implementation
            // function (e.g., ShelfView_onKeyEvent_Str_Z_k_). The unmangled wrapper is added below.
            for (function in irClass.declarations.filterIsInstance<IrSimpleFunction>()) {
                if (!function.isFakeOverride && !isComponentScopeAccessor(function)) {
                    transformFunction(function)?.let { declarations.add(it) }
                }
            }

            // Generate onKeyEvent function for components that need it
            // This must be named exactly "onKeyEvent" (no mangling) to work with SceneGraph
            val onKeyEventFunction = generateOnKeyEventFunction(irClass)
            if (onKeyEventFunction != null) {
                declarations.add(onKeyEventFunction)
            }

            // Generate property accessor functions (for delegated properties, custom getters/setters, etc.)
            for (property in irClass.declarations.filterIsInstance<IrProperty>()) {
                if (context.intrinsics.isComponentScopeProperty(property.name.asString())) continue
                if (layoutInfo != null && isLayoutClassProperty(property, irClass)) continue
                transformProperty(property, declarations, mutableListOf())
            }

            // Process nested classes (companion objects, etc.)
            // Note: Layout stubs are handled at the top level by isLayoutStubClass()
            for (nested in irClass.declarations.filterIsInstance<IrClass>()) {
                val nestedDeclarations = mutableListOf<BrsDeclaration>()
                val nestedStatements = mutableListOf<BrsStatement>()
                transformClassDeclarations(nested, nestedDeclarations, nestedStatements)
                declarations.addAll(nestedDeclarations)
            }
        } finally {
            // Restore context flags
            isInComponentContext = previousInComponent
            currentComponentClass = previousComponentClass
        }
    }

    /**
     * Check if a function is a component scope property accessor (top, global, m)
     * that should not be emitted as we handle these specially.
     */
    private fun isComponentScopeAccessor(function: IrSimpleFunction): Boolean {
        val property = function.correspondingPropertySymbol?.owner ?: return false
        val propName = property.name.asString()
        return context.intrinsics.isComponentScopeProperty(propName)
    }

    /**
     * Generate the onKeyEvent function for a SceneGraph component.
     *
     * For components that extend GroupComponent, LayoutComponent, or SceneComponent,
     * we generate an onKeyEvent function that:
     * - Returns false by default (allowing events to propagate to focused children)
     * - Calls the Kotlin override if present
     *
     * This is required for proper key event handling in SceneGraph. Without an
     * onKeyEvent function, Group components may block key events from reaching
     * focused children.
     *
     * Generated BrightScript:
     * ```brightscript
     * function onKeyEvent(key as String, press as Boolean) as Boolean
     *     return false  ' or: return m.onKeyEvent_String_Boolean_k_(key, press)
     * end function
     * ```
     *
     * @return The generated function, or null if this component doesn't need onKeyEvent
     */
    private fun generateOnKeyEventFunction(irClass: IrClass): BrsFunction? {
        // Only generate for components that can receive key events
        if (!context.intrinsics.componentNeedsOnKeyEvent(irClass)) {
            return null
        }

        // Don't generate for abstract base classes (GroupComponent, SceneComponent, etc.)
        // Only concrete user classes should have onKeyEvent generated
        if (irClass.modality == org.jetbrains.kotlin.descriptors.Modality.ABSTRACT) {
            return null
        }

        val parameters = mutableListOf(
            BrsParameter("key", BrsType.STRING),
            BrsParameter("press", BrsType.BOOLEAN)
        )

        // Check if the user has overridden onKeyEvent
        val override = context.intrinsics.findOnKeyEventOverride(irClass)

        val body = if (override != null) {
            // Call the user's override: return m.onKeyEvent_String_Boolean_k_(key, press)
            val mangledName = context.getBrsName(override)
            val className = context.getBrsName(irClass)
            val shortName = mangledName.removePrefix("${className}_")

            BrsBlock(mutableListOf(
                BrsReturn(
                    BrsFunctionCall(
                        BrsDotAccess(BrsMRef(), shortName),
                        mutableListOf(BrsIdentifier("key"), BrsIdentifier("press"))
                    )
                )
            ))
        } else {
            // Default: return false (allow event to propagate to children)
            BrsBlock(mutableListOf(BrsReturn(BrsBooleanLiteral(false))))
        }

        return BrsFunction(
            name = "onKeyEvent",
            parameters = parameters,
            returnType = BrsType.BOOLEAN,
            body = body
        )
    }

    /**
     * Transform the component's init {} block to BrightScript's sub init().
     *
     * Extracts statements from the constructor body, filtering out:
     * - Delegating constructor calls (super())
     * - Instance initializer calls
     *
     * @param layoutInfo Optional layout info for @SGLayout property initialization
     */
    private fun transformComponentInitBlock(
        irClass: IrClass,
        constructor: IrConstructor,
        layoutInfo: LayoutAccessorInfo? = null
    ): BrsSub? {
        val bodyStatements = mutableListOf<BrsStatement>()

        // Initialize property backing fields on m (like regular classes do with 'this')
        // This must happen BEFORE init block statements execute
        for (property in irClass.declarations.filterIsInstance<IrProperty>()) {
            // Skip inherited component scope properties (top, global, m)
            if (context.intrinsics.isComponentScopeProperty(property.name.asString())) {
                continue
            }

            // Skip @SGLayout properties - we'll initialize them separately
            if (context.intrinsics.hasSGLayoutAnnotation(property)) {
                continue
            }

            // Skip properties whose type is the generated Layout class
            // These are initialized via layoutInfo using ComponentName_Layout_create
            if (layoutInfo != null && isLayoutClassProperty(property, irClass)) {
                continue
            }

            val backingField = property.backingField ?: continue
            // Sanitize field name for special names like <this>
            val rawFieldName = backingField.name.asString()
            val fieldName = when {
                rawFieldName == "<this>" -> "__this"
                rawFieldName.startsWith("<") && rawFieldName.endsWith(">") ->
                    rawFieldName.removePrefix("<").removeSuffix(">").replace("-", "_").replace(" ", "_")
                else -> rawFieldName.replace("$", "_")
            }
            backingField.initializer?.expression?.let { initializer ->
                val transformedInit = transformExpression(initializer)
                val hoisted = takeHoistedStatements()
                bodyStatements.addAll(hoisted)
                // Interface fields (with SGField annotations) go on m.top, internal state goes on m
                val target = if (expressionTransformer.hasInterfaceFieldAnnotation(property)) {
                    BrsDotAccess(BrsDotAccess(BrsMRef(), "top"), fieldName)  // m.top.fieldName
                } else {
                    BrsDotAccess(BrsMRef(), fieldName)  // m.fieldName
                }
                bodyStatements.add(
                    BrsExpressionStatement(
                        BrsBinaryOp(
                            target,
                            BrsBinaryOperator.EQ,
                            transformedInit
                        )
                    )
                )
            }
        }

        // Initialize @SGLayout property with the accessor class instance
        if (layoutInfo != null) {
            // m._layout = ComponentName_Layout_create(m.top)
            bodyStatements.add(
                BrsExpressionStatement(
                    BrsBinaryOp(
                        BrsDotAccess(BrsMRef(), "_${layoutInfo.propertyName}"),
                        BrsBinaryOperator.EQ,
                        BrsFunctionCall(
                            BrsIdentifier("${layoutInfo.className}_create"),
                            mutableListOf(BrsDotAccess(BrsMRef(), "top"))
                        )
                    )
                )
            )
        }

        // Attach member functions to m (like regular classes attach to 'this')
        // This must happen BEFORE init block statements so methods are available
        val className = context.getBrsName(irClass)
        for (function in irClass.declarations.filterIsInstance<IrSimpleFunction>()) {
            if (function.isFakeOverride) continue
            if (isComponentScopeAccessor(function)) continue  // Skip top/global/m accessors

            val methodName = context.getBrsName(function)
            val shortName = methodName.removePrefix("${className}_")

            bodyStatements.add(
                BrsExpressionStatement(
                    BrsBinaryOp(
                        BrsDotAccess(BrsMRef(), shortName),
                        BrsBinaryOperator.EQ,
                        BrsIdentifier(methodName)
                    )
                )
            )
        }

        // Attach property accessor methods to m
        for (property in irClass.declarations.filterIsInstance<IrProperty>()) {
            if (context.intrinsics.isComponentScopeProperty(property.name.asString())) continue
            if (layoutInfo != null && isLayoutClassProperty(property, irClass)) continue

            property.getter?.let { getter ->
                if (!getter.isFakeOverride) {
                    val getterName = context.getBrsName(getter)
                    val shortName = getterName.removePrefix("${className}_")
                    bodyStatements.add(
                        BrsExpressionStatement(
                            BrsBinaryOp(
                                BrsDotAccess(BrsMRef(), shortName),
                                BrsBinaryOperator.EQ,
                                BrsIdentifier(getterName)
                            )
                        )
                    )
                }
            }

            property.setter?.let { setter ->
                if (!setter.isFakeOverride) {
                    val setterName = context.getBrsName(setter)
                    val shortName = setterName.removePrefix("${className}_")
                    bodyStatements.add(
                        BrsExpressionStatement(
                            BrsBinaryOp(
                                BrsDotAccess(BrsMRef(), shortName),
                                BrsBinaryOperator.EQ,
                                BrsIdentifier(setterName)
                            )
                        )
                    )
                }
            }
        }

        // Extract init block statements from IrAnonymousInitializer declarations on the class
        // (These haven't been lowered/inlined into the constructor for BRS backend)
        for (declaration in irClass.declarations) {
            if (declaration is IrAnonymousInitializer && !declaration.isStatic) {
                val initBody = declaration.body
                for (stmt in initBody.statements) {
                    transformStatement(stmt)?.let { bodyStatements.add(it) }
                }
            }
        }

        // Also check constructor body for any statements after delegating call
        val body = constructor.body
        if (body is IrBlockBody) {
            for (stmt in body.statements) {
                when (stmt) {
                    is IrDelegatingConstructorCall -> continue
                    is IrInstanceInitializerCall -> continue
                    else -> {
                        transformStatement(stmt)?.let { bodyStatements.add(it) }
                    }
                }
            }
        }

        // Only generate init() if there's actual code
        if (bodyStatements.isEmpty()) return null

        return BrsSub(
            name = "init",
            parameters = mutableListOf(),
            body = BrsBlock(bodyStatements)
        )
    }

    // ==================== Layout Accessor Generation ====================

    /**
     * Information about a layout accessor class to generate.
     */
    data class LayoutAccessorInfo(
        /** The name of the property (e.g., "layout") */
        val propertyName: String,
        /** The name of the generated accessor class (e.g., "MainScreen_Layout") */
        val className: String,
        /** List of all node IDs that need accessor properties */
        val nodeIds: List<String>
    )

    /**
     * Extract layout accessor info from an @SGLayout property.
     *
     * This extracts the node IDs from the sceneLayout { } DSL call to generate
     * the appropriate accessor class.
     */
    private fun extractLayoutInfo(irClass: IrClass, property: IrProperty): LayoutAccessorInfo? {
        val componentName = context.getBrsName(irClass)
        val propertyName = property.name.asString()
        val className = "${componentName}_Layout"

        // Get the backing field initializer
        val backingField = property.backingField ?: return null
        val initializer = backingField.initializer?.expression ?: return null

        // The initializer should be a call to sceneLayout { }
        val sceneLayoutCall = initializer as? IrCall ?: return null
        if (sceneLayoutCall.symbol.owner.name.asString() != "sceneLayout") return null

        // Extract node IDs from the DSL lambda
        val nodeIds = extractNodeIdsFromLayoutDsl(sceneLayoutCall)
        if (nodeIds.isEmpty()) return null

        return LayoutAccessorInfo(propertyName, className, nodeIds)
    }

    /**
     * Extract layout accessor info from a companion object's @SGLayout function.
     *
     * This pattern uses @SGLayout as a marker annotation:
     * ```kotlin
     * companion object {
     *     @SGLayout
     *     fun defineLayout() = sceneLayout {
     *         button(id = "myButton")
     *         label(id = "myLabel")
     *     }
     * }
     * ```
     *
     * The node IDs are extracted from the DSL body by finding calls like `button(id = "xyz")`.
     */
    private fun extractLayoutInfoFromCompanion(irClass: IrClass): LayoutAccessorInfo? {
        // Find companion object
        val companion = irClass.declarations
            .filterIsInstance<IrClass>()
            .find { it.isCompanion }
            ?: return null

        // Find function with @SGLayout annotation
        val layoutFunction = companion.declarations
            .filterIsInstance<IrSimpleFunction>()
            .find { context.intrinsics.hasSGLayoutAnnotation(it) }
            ?: return null

        // Extract node IDs from function body by traversing the DSL
        val nodeIds = extractNodeIdsFromFunctionBody(layoutFunction)
        if (nodeIds.isEmpty()) return null

        val componentName = context.getBrsName(irClass)
        val className = "${componentName}_Layout"

        return LayoutAccessorInfo("layout", className, nodeIds)
    }

    /**
     * Extract node IDs from a @SGLayout function body.
     * Finds the sceneLayout { } call and extracts IDs from DSL builder calls inside.
     */
    private fun extractNodeIdsFromFunctionBody(function: IrSimpleFunction): List<String> {
        val body = function.body ?: return emptyList()

        // Find the sceneLayout { } call
        val sceneLayoutCall = findSceneLayoutCall(body) ?: return emptyList()

        // Extract node IDs from the DSL
        return extractNodeIdsFromLayoutDsl(sceneLayoutCall)
    }

    /**
     * Find the sceneLayout { } call within a function body.
     */
    private fun findSceneLayoutCall(body: IrBody): IrCall? {
        return when (body) {
            is IrBlockBody -> {
                for (statement in body.statements) {
                    // Check direct return statement
                    if (statement is IrReturn) {
                        val call = statement.value as? IrCall
                        if (call?.symbol?.owner?.name?.asString() == "sceneLayout") {
                            return call
                        }
                    }
                    // Check direct call statement
                    if (statement is IrCall && statement.symbol.owner.name.asString() == "sceneLayout") {
                        return statement
                    }
                }
                null
            }
            is IrExpressionBody -> {
                val call = body.expression as? IrCall
                if (call?.symbol?.owner?.name?.asString() == "sceneLayout") call else null
            }
            else -> null
        }
    }

    /**
     * Check if a property has the generated Layout class type.
     *
     * This is used to identify properties like `val layout = MainScreen_Layout(top)` that should
     * be initialized via the generated Layout_create function instead of direct constructor call.
     *
     * Supports two patterns:
     * 1. Top-level class: `MainScreen_Layout` (new pattern)
     * 2. Nested class: `MainScreen.Layout` (legacy pattern, for backwards compatibility)
     */
    internal fun isLayoutClassProperty(property: IrProperty, ownerClass: IrClass): Boolean {
        val propertyType = property.getter?.returnType ?: property.backingField?.type ?: return false
        val typeClass = propertyType.classOrNull?.owner ?: return false

        val ownerClassName = ownerClass.name.asString()
        val typeClassName = typeClass.name.asString()

        // New pattern: top-level class named OwnerClassName_Layout
        if (typeClassName == "${ownerClassName}_Layout" && typeClass.parent !is IrClass) {
            return true
        }

        // Legacy pattern: nested class named "Layout" within the owner class
        if (typeClassName == "Layout" && typeClass.parent == ownerClass) {
            return true
        }

        return false
    }

    /**
     * Check if a class is a FIR-generated Layout class.
     *
     * Note: FIR generation via SceneGraphLayoutGenerator has been removed.
     * This function is kept for backwards compatibility with any existing
     * compiled artifacts that may still have FIR-generated Layout classes.
     */
    private fun isFirGeneratedLayoutClass(irClass: IrClass): Boolean {
        val origin = irClass.origin
        return origin is IrDeclarationOrigin.GeneratedByPlugin &&
               origin.pluginId.contains("SGLayoutKey")
    }

    /**
     * Check if a class is a FIR-generated top-level Layout class.
     */
    private fun isFirGeneratedTopLevelLayoutClass(irClass: IrClass): Boolean {
        return isFirGeneratedLayoutClass(irClass) &&
               irClass.name.asString().endsWith("_Layout") &&
               irClass.parent !is IrClass
    }

    /**
     * Check if a class is a Layout stub class that should be skipped.
     *
     * Only FIR-generated Layout classes are skipped (legacy path - now removed).
     * Gradle-generated Layout stubs are compiled normally since user code
     * may reference them directly (e.g., `val layout = MainScreen_Layout(top)`).
     */
    private fun isLayoutStubClass(irClass: IrClass): Boolean {
        // Must be a top-level class (not nested)
        if (irClass.parent is IrClass) return false

        // Must have name ending with _Layout
        val className = irClass.name.asString()
        if (!className.endsWith("_Layout")) return false

        // Only skip FIR-generated classes (legacy path - no longer generated)
        // Gradle-generated stubs should be compiled normally
        return isFirGeneratedLayoutClass(irClass)
    }

    /**
     * Extract all node IDs from a sceneLayout { } DSL call.
     */
    private fun extractNodeIdsFromLayoutDsl(call: IrCall): List<String> {
        val nodeIds = mutableListOf<String>()

        // The lambda is the last argument
        val lambdaArg = call.getValueArgument(call.valueArgumentsCount - 1)
        val lambdaBody = extractLambdaBody(lambdaArg) ?: return emptyList()

        // Recursively extract node IDs from the lambda body
        extractNodeIdsFromStatements(lambdaBody.statements, nodeIds)

        return nodeIds
    }

    /**
     * Extract the body from a lambda expression.
     */
    private fun extractLambdaBody(arg: IrExpression?): IrBlockBody? {
        return when (arg) {
            is IrFunctionExpression -> arg.function.body as? IrBlockBody
            is IrBlock -> {
                val functionRef = arg.statements.filterIsInstance<IrFunctionReference>().firstOrNull()
                val function = functionRef?.symbol?.owner ?: return null
                function.body as? IrBlockBody
            }
            else -> null
        }
    }

    /**
     * Recursively extract node IDs from builder method calls.
     */
    private fun extractNodeIdsFromStatements(statements: List<IrStatement>, nodeIds: MutableList<String>) {
        for (statement in statements) {
            val call = statement as? IrCall ?: continue
            val functionName = call.symbol.owner.name.asString()

            // Check if this is a DSL builder method
            val isBuilderMethod = functionName in setOf(
                "group", "layoutGroup", "label", "poster", "rectangle",
                "button", "buttonGroup", "textEditBox", "keyboard"
            )
            if (!isBuilderMethod) continue

            // Extract the id argument (first argument)
            val idArg = call.getValueArgument(0) as? IrConst ?: continue
            val id = idArg.value as? String ?: continue
            nodeIds.add(id)

            // Check for children lambda (last argument for container nodes)
            val function = call.symbol.owner
            val initParamIndex = function.valueParameters.indexOfFirst { it.name.asString() == "init" }
            if (initParamIndex >= 0) {
                val childLambda = call.getValueArgument(initParamIndex)
                val childBody = extractLambdaBody(childLambda)
                if (childBody != null) {
                    extractNodeIdsFromStatements(childBody.statements, nodeIds)
                }
            }
        }
    }

    /**
     * Generate the layout accessor class declarations.
     *
     * For a component with @SGLayout property, generates:
     * - A _create function that initializes the accessor instance
     * - A getter function for each declared node ID
     *
     * Example output for MainScreen with nodes "mainLayout" and "counterLabel":
     * ```brightscript
     * function MainScreen_Layout_create(top)
     *     instance = {}
     *     instance._top = top
     *     instance._mainLayout = invalid
     *     instance._counterLabel = invalid
     *     instance.get_mainLayout = MainScreen_Layout_get_mainLayout
     *     instance.get_counterLabel = MainScreen_Layout_get_counterLabel
     *     return instance
     * end function
     *
     * function MainScreen_Layout_get_mainLayout()
     *     if m._mainLayout = invalid then
     *         m._mainLayout = m._top.findNode("mainLayout")
     *     end if
     *     return m._mainLayout
     * end function
     * ```
     */
    private fun generateLayoutAccessorClass(info: LayoutAccessorInfo): List<BrsDeclaration> {
        val declarations = mutableListOf<BrsDeclaration>()

        // Generate _create function
        declarations.add(generateLayoutCreateFunction(info))

        // Generate getter for each node ID
        for (nodeId in info.nodeIds) {
            declarations.add(generateLayoutNodeGetter(info.className, nodeId))
        }

        return declarations
    }

    /**
     * Generate the _create function for the layout accessor class.
     */
    private fun generateLayoutCreateFunction(info: LayoutAccessorInfo): BrsFunction {
        val statements = mutableListOf<BrsStatement>()

        // instance = {}
        statements.add(
            BrsVariable(
                name = "instance",
                initializer = BrsAALiteral(mutableListOf())
            )
        )

        // instance._top = top
        statements.add(
            BrsExpressionStatement(
                BrsBinaryOp(
                    BrsDotAccess(BrsIdentifier("instance"), "_top"),
                    BrsBinaryOperator.EQ,
                    BrsIdentifier("top")
                )
            )
        )

        // For each node ID: instance._nodeId = invalid
        for (nodeId in info.nodeIds) {
            statements.add(
                BrsExpressionStatement(
                    BrsBinaryOp(
                        BrsDotAccess(BrsIdentifier("instance"), "_$nodeId"),
                        BrsBinaryOperator.EQ,
                        BrsInvalidLiteral()
                    )
                )
            )
        }

        // Attach getter functions: instance.get_nodeId = ClassName_get_nodeId
        for (nodeId in info.nodeIds) {
            statements.add(
                BrsExpressionStatement(
                    BrsBinaryOp(
                        BrsDotAccess(BrsIdentifier("instance"), "get_$nodeId"),
                        BrsBinaryOperator.EQ,
                        BrsIdentifier("${info.className}_get_$nodeId")
                    )
                )
            )
        }

        // return instance
        statements.add(BrsReturn(BrsIdentifier("instance")))

        return BrsFunction(
            name = "${info.className}_create",
            parameters = mutableListOf(BrsParameter("top", BrsType.OBJECT)),
            returnType = BrsType.OBJECT,
            body = BrsBlock(statements)
        )
    }

    /**
     * Generate a getter function for a specific node ID.
     *
     * Generates code that:
     * 1. Lazily looks up the node on first access
     * 2. Caches the result for subsequent accesses
     * 3. Throws a descriptive error if the node is not found
     */
    private fun generateLayoutNodeGetter(className: String, nodeId: String): BrsFunction {
        val statements = mutableListOf<BrsStatement>()

        // if m._nodeId = invalid then
        //     m._nodeId = m._top.findNode("nodeId")
        //     if m._nodeId = invalid then
        //         throw "Layout node 'nodeId' not found in component"
        //     end if
        // end if
        statements.add(
            BrsIf(
                condition = BrsBinaryOp(
                    BrsDotAccess(BrsMRef(), "_$nodeId"),
                    BrsBinaryOperator.EQ,
                    BrsInvalidLiteral()
                ),
                thenBranch = BrsBlock(mutableListOf(
                    // m._nodeId = m._top.findNode("nodeId")
                    BrsExpressionStatement(
                        BrsBinaryOp(
                            BrsDotAccess(BrsMRef(), "_$nodeId"),
                            BrsBinaryOperator.EQ,
                            BrsMethodCall(
                                BrsDotAccess(BrsMRef(), "_top"),
                                "findNode",
                                mutableListOf(BrsStringLiteral(nodeId))
                            )
                        )
                    ),
                    // if m._nodeId = invalid then throw "..." end if
                    BrsIf(
                        condition = BrsBinaryOp(
                            BrsDotAccess(BrsMRef(), "_$nodeId"),
                            BrsBinaryOperator.EQ,
                            BrsInvalidLiteral()
                        ),
                        thenBranch = BrsBlock(mutableListOf(
                            BrsThrow(BrsStringLiteral("Layout node '$nodeId' not found in component"))
                        )),
                        elseBranch = null
                    )
                )),
                elseBranch = null
            )
        )

        // return m._nodeId
        statements.add(BrsReturn(BrsDotAccess(BrsMRef(), "_$nodeId")))

        return BrsFunction(
            name = "${className}_get_$nodeId",
            parameters = mutableListOf(),  // No parameters - uses m scope
            returnType = BrsType.OBJECT,
            body = BrsBlock(statements)
        )
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

        // Note: BrightScript doesn't support top-level statements, so we use GetGlobalAA() for global storage
        // Instance is lazily initialized in getInstance()

        // Generate the _create function (like regular class constructor)
        // Find the primary constructor first (needed for getInstance function)
        val primaryConstructor = irClass.declarations.filterIsInstance<IrConstructor>().firstOrNull()
        for (constructor in irClass.declarations.filterIsInstance<IrConstructor>()) {
            transformConstructor(irClass, constructor)?.let { declarations.add(it) }
        }

        // Generate getInstance function
        val getInstanceBody = mutableListOf<BrsStatement>()

        // Get the mangled constructor name (must match the definition)
        val constructorName = if (primaryConstructor != null) {
            context.getBrsName(primaryConstructor)
        } else {
            "${className}_create"  // Fallback for edge cases
        }

        // Helper to generate GetGlobalAA().instanceVarName
        fun globalInstanceAccess() = BrsDotAccess(
            BrsFunctionCall(BrsIdentifier("GetGlobalAA"), mutableListOf()),
            instanceVarName
        )

        // if GetGlobalAA().MySingleton_instance = invalid then
        //     GetGlobalAA().MySingleton_instance = MySingleton_create()
        // end if
        getInstanceBody.add(
            BrsIf(
                condition = BrsBinaryOp(
                    globalInstanceAccess(),
                    BrsBinaryOperator.EQ,
                    BrsInvalidLiteral()
                ),
                thenBranch = BrsBlock(mutableListOf(
                    BrsExpressionStatement(
                        BrsBinaryOp(
                            globalInstanceAccess(),
                            BrsBinaryOperator.EQ,
                            BrsFunctionCall(BrsIdentifier(constructorName), mutableListOf())
                        )
                    )
                ))
            )
        )

        // return GetGlobalAA().MySingleton_instance
        getInstanceBody.add(BrsReturn(globalInstanceAccess()))

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

        // if m.Color_entriesInitialized = true then return
        // Note: Use explicit = true comparison because the flag may be invalid (uninitialized)
        // on first access, and BrightScript doesn't allow invalid in bare if-clause conditions.
        initEntriesBody.add(
            BrsIf(
                condition = BrsBinaryOp(
                    BrsDotAccess(BrsIdentifier("m"), initializedFlagName),
                    BrsBinaryOperator.EQ,
                    BrsBooleanLiteral(true)
                ),
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
            // Store enum metadata for constant inlining optimization
            context.mapping.enumEntryOrdinals[entry] = ordinal
            context.mapping.enumEntryNames[entry] = entry.name.asString()

            // Get the constructor from the initializer expression
            var entryConstructor: IrConstructor? = null

            // Extract constant property values for inlining
            entry.initializerExpression?.let { init ->
                val initExpr = init.expression
                if (initExpr is IrEnumConstructorCall) {
                    val constantProps = mutableMapOf<String, Any?>()
                    val constructor = initExpr.symbol.owner
                    entryConstructor = constructor
                    for (i in 0 until initExpr.valueArgumentsCount) {
                        initExpr.getValueArgument(i)?.let { arg ->
                            if (arg is IrConst) {
                                val paramName = constructor.valueParameters.getOrNull(i)?.name?.asString()
                                if (paramName != null) {
                                    constantProps[paramName] = arg.value
                                }
                            }
                        }
                    }
                    if (constantProps.isNotEmpty()) {
                        context.mapping.enumEntryConstantProperties[entry] = constantProps
                    }
                }
            }

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

            // Get the mangled constructor name (must match the definition)
            val constructorName = if (entryConstructor != null) {
                context.getBrsName(entryConstructor!!)
            } else {
                "${className}_create"  // Fallback for edge cases
            }

            initEntriesBody.add(
                BrsExpressionStatement(
                    BrsBinaryOp(
                        BrsDotAccess(BrsIdentifier("m"), entryVarName),
                        BrsBinaryOperator.EQ,
                        BrsFunctionCall(BrsIdentifier(constructorName), args)
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
        // Use mangled constructor name to support overloading (must match call sites)
        val name = context.getBrsName(constructor)

        // Start with name and ordinal parameters
        val parameters = mutableListOf(
            BrsParameter("__name", BrsType.STRING, null),
            BrsParameter("__ordinal", BrsType.INTEGER, null)
        )

        // Add constructor parameters
        constructor.valueParameters.forEach { param ->
            parameters.add(
                BrsParameter(
                    name = sanitizeParameterName(param.name.asString()),
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

        // this.__proto = ["ClassName", ...interfaces]
        val allInterfaceNames = collectAllInterfaceNames(irClass)
        val protoElements = mutableListOf<BrsExpression>(BrsStringLiteral(className))
        allInterfaceNames.forEach { protoElements.add(BrsStringLiteral(it)) }
        bodyStatements.add(
            BrsExpressionStatement(
                BrsBinaryOp(
                    BrsDotAccess(BrsIdentifier("this"), "__proto"),
                    BrsBinaryOperator.EQ,
                    BrsArrayLiteral(protoElements)
                )
            )
        )

        // this.__id = __kotlin_nextObjectId()
        // Unique object ID for identity checks (===)
        context.recordFunctionDependency("__kotlin_nextObjectId")
        bodyStatements.add(
            BrsExpressionStatement(
                BrsBinaryOp(
                    BrsDotAccess(BrsIdentifier("this"), "__id"),
                    BrsBinaryOperator.EQ,
                    BrsFunctionCall(BrsIdentifier("__kotlin_nextObjectId"), mutableListOf())
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
                        BrsIdentifier(sanitizeParameterName(param.name.asString()))
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

        // Add methods and property accessors to the instance
        addMethodAttachments(irClass, className, bodyStatements)

        // return this
        bodyStatements.add(BrsReturn(BrsIdentifier("this")))

        return BrsFunction(
            name = name,
            parameters = normalizeParametersForBrs(parameters).toMutableList(),
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
        declarations.add(generateDataClassCopy(className, primaryConstructor, properties))

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

        // Process nested classes (companion objects, etc.)
        for (nested in irClass.declarations.filterIsInstance<IrClass>()) {
            transformClassDeclarations(nested, declarations, statements)
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
        // Use mangled constructor name to support overloading (must match call sites)
        val name = context.getBrsName(constructor)

        val parameters = normalizeParametersForBrs(constructor.valueParameters.map { param ->
            BrsParameter(
                name = sanitizeParameterName(param.name.asString()),
                type = mapTypeToBrs(param.type),
                defaultValue = param.defaultValue?.expression?.let { transformExpression(it) }
            )
        })

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

        // this.__proto = ["ClassName", ...interfaces]
        val allInterfaceNamesDataClass = collectAllInterfaceNames(irClass)
        val protoElementsDataClass = mutableListOf<BrsExpression>(BrsStringLiteral(className))
        allInterfaceNamesDataClass.forEach { protoElementsDataClass.add(BrsStringLiteral(it)) }
        bodyStatements.add(
            BrsExpressionStatement(
                BrsBinaryOp(
                    BrsDotAccess(BrsIdentifier("this"), "__proto"),
                    BrsBinaryOperator.EQ,
                    BrsArrayLiteral(protoElementsDataClass)
                )
            )
        )

        // this.__id = __kotlin_nextObjectId()
        // Unique object ID for identity checks (===)
        context.recordFunctionDependency("__kotlin_nextObjectId")
        bodyStatements.add(
            BrsExpressionStatement(
                BrsBinaryOp(
                    BrsDotAccess(BrsIdentifier("this"), "__id"),
                    BrsBinaryOperator.EQ,
                    BrsFunctionCall(BrsIdentifier("__kotlin_nextObjectId"), mutableListOf())
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
                        BrsIdentifier(sanitizeParameterName(param.name.asString()))
                    )
                )
            )
        }

        // Add any additional methods and property accessors to the instance
        addMethodAttachments(irClass, className, bodyStatements)

        // Attach synthetic data class methods (equals, hashCode, toString, copy)
        val syntheticMethods = listOf("equals", "hashCode", "toString", "copy")
        syntheticMethods.forEach { methodName ->
            bodyStatements.add(
                BrsExpressionStatement(
                    BrsBinaryOp(
                        BrsDotAccess(BrsIdentifier("this"), methodName),
                        BrsBinaryOperator.EQ,
                        BrsIdentifier("${className}_$methodName")
                    )
                )
            )
        }

        // Attach componentN methods
        properties.forEachIndexed { index, _ ->
            bodyStatements.add(
                BrsExpressionStatement(
                    BrsBinaryOp(
                        BrsDotAccess(BrsIdentifier("this"), "component${index + 1}"),
                        BrsBinaryOperator.EQ,
                        BrsIdentifier("${className}_component${index + 1}")
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
    private fun generateDataClassCopy(className: String, primaryConstructor: IrConstructor?, properties: List<IrValueParameter>): BrsFunction {
        val bodyStatements = mutableListOf<BrsStatement>()

        // Parameters with invalid as default (BrightScript doesn't allow m.property as default)
        val parameters = properties.map { param ->
            val propName = param.name.asString()
            val sanitizedName = sanitizeParameterName(propName)
            BrsParameter(
                name = sanitizedName,
                type = null,  // Untyped to allow invalid
                defaultValue = BrsIdentifier("invalid")
            )
        }

        // Add runtime checks: if param = invalid then param = m.param
        properties.forEach { param ->
            val propName = param.name.asString()
            val sanitizedName = sanitizeParameterName(propName)
            bodyStatements.add(
                BrsIf(
                    condition = BrsBinaryOp(
                        BrsIdentifier(sanitizedName),
                        BrsBinaryOperator.EQ,
                        BrsIdentifier("invalid")
                    ),
                    thenBranch = BrsExpressionStatement(
                        BrsBinaryOp(
                            BrsIdentifier(sanitizedName),
                            BrsBinaryOperator.EQ,
                            BrsDotAccess(BrsIdentifier("m"), propName)  // Property access uses original name
                        )
                    ),
                    elseBranch = null
                )
            )
        }

        // Get the mangled constructor name (must match the definition)
        val constructorName = if (primaryConstructor != null) {
            context.getBrsName(primaryConstructor)
        } else {
            "${className}_create"  // Fallback for edge cases
        }

        // return ClassName_create(name, age, ...)
        val args = properties.map { param ->
            BrsIdentifier(sanitizeParameterName(param.name.asString()))
        }
        bodyStatements.add(
            BrsReturn(BrsFunctionCall(BrsIdentifier(constructorName), args.toMutableList()))
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

        // For inner classes, add _outer parameter first
        if (irClass.isInner) {
            parametersList.add(BrsParameter("_outer", BrsType.OBJECT))
        }

        // Add regular constructor parameters
        parametersList.addAll(constructor.valueParameters.map { param ->
            BrsParameter(
                name = sanitizeParameterName(param.name.asString()),
                type = mapTypeToBrs(param.type),
                defaultValue = param.defaultValue?.expression?.let { transformExpression(it) }
            )
        })

        val parameters = normalizeParametersForBrs(parametersList)

        // Build constructor body
        val bodyStatements = mutableListOf<BrsStatement>()

        // Check if this constructor delegates to another constructor of the same class (this(...))
        // If so, we just call that constructor and return the result - no object initialization here
        val delegatingCall = findDelegatingConstructorCall(constructor)
        if (delegatingCall != null) {
            val delegatedConstructor = delegatingCall.symbol.owner
            val delegatedClass = delegatedConstructor.parentAsClass
            if (delegatedClass == irClass) {
                // This is a this(...) delegation - just call the other constructor and return
                val (hoistedStatements, args) = getSuperConstructorArgsWithHoisting(constructor)
                bodyStatements.addAll(hoistedStatements)
                val delegatedName = context.getBrsName(delegatedConstructor)
                val call = BrsFunctionCall(BrsIdentifier(delegatedName), args)
                bodyStatements.add(BrsReturn(call))
                return BrsFunction(name, parameters.toMutableList(), BrsType.OBJECT, BrsBlock(bodyStatements))
            }
        }

        // Check if there's a superclass (not Any)
        val superClass = irClass.superTypes
            .mapNotNull { it.classOrNull?.owner }
            .firstOrNull { !it.isInterface && it.name.asString() != "Any" }

        if (superClass != null) {
            // Find the delegating constructor call to get the super constructor
            val delegatingCall = findDelegatingConstructorCall(constructor)
            val superConstructor = delegatingCall?.symbol?.owner

            // Get super constructor arguments with any hoisted statements
            // This handles cases like super(message?.toString()) where the safe call
            // creates a block with temp variables that need to be extracted
            val (hoistedStatements, superArgs) = getSuperConstructorArgsWithHoisting(constructor)

            // Add hoisted statements before the super call
            bodyStatements.addAll(hoistedStatements)

            // Call parent constructor: this = ParentClass_create_ParamTypes_k$(...)
            val superConstructorName = if (superConstructor != null) {
                context.getBrsName(superConstructor)
            } else {
                // Fallback: use class name + _create for no-arg constructor
                "${context.getBrsName(superClass)}_create"
            }
            val superConstructorCall = BrsFunctionCall(
                BrsIdentifier(superConstructorName),
                superArgs
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
                    val fullMethodName = context.getBrsName(function)
                    val methodName = fullMethodName.removePrefix("${className}_")
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

            // Update __proto chain - include this class and its direct interfaces
            // (inherited interfaces come from the parent's __proto)
            val directInterfaceNames = irClass.superTypes
                .filter { it.classOrNull?.owner?.isInterface == true }
                .map { context.getBrsName(it.classOrNull!!.owner) }
            val protoElements = mutableListOf<BrsExpression>(BrsStringLiteral(className))
            directInterfaceNames.forEach { protoElements.add(BrsStringLiteral(it)) }
            protoElements.add(BrsDotAccess(BrsIdentifier("this"), "__proto"))
            bodyStatements.add(
                BrsExpressionStatement(
                    BrsBinaryOp(
                        BrsDotAccess(BrsIdentifier("this"), "__proto"),
                        BrsBinaryOperator.EQ,
                        BrsArrayLiteral(protoElements)
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

            // Initialize __proto chain with this class and all implemented interfaces
            val allInterfaceNames = collectAllInterfaceNames(irClass)
            val protoElements = mutableListOf<BrsExpression>(BrsStringLiteral(className))
            allInterfaceNames.forEach { protoElements.add(BrsStringLiteral(it)) }
            bodyStatements.add(
                BrsExpressionStatement(
                    BrsBinaryOp(
                        BrsDotAccess(BrsIdentifier("this"), "__proto"),
                        BrsBinaryOperator.EQ,
                        BrsArrayLiteral(protoElements)
                    )
                )
            )

            // this.__id = __kotlin_nextObjectId()
            // Unique object ID for identity checks (===)
            context.recordFunctionDependency("__kotlin_nextObjectId")
            bodyStatements.add(
                BrsExpressionStatement(
                    BrsBinaryOp(
                        BrsDotAccess(BrsIdentifier("this"), "__id"),
                        BrsBinaryOperator.EQ,
                        BrsFunctionCall(BrsIdentifier("__kotlin_nextObjectId"), mutableListOf())
                    )
                )
            )

            // Add default Any methods for classes that directly extend Any
            // (Classes with explicit superclasses inherit these from the parent constructor call)
            // These provide default implementations that can be overridden by the class itself
            addAnyMethodDefaults(bodyStatements)
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
                        BrsDotAccess(BrsIdentifier("this"), "_outer"),
                        BrsBinaryOperator.EQ,
                        BrsIdentifier("_outer")
                    )
                )
            )
        }

        // Set flag so that '<this>' references map to 'this' instead of 'm'
        // This must be set BEFORE transforming field/property initializers, as they may
        // reference constructor parameters via getters on <this>
        isInConstructorBody = true

        // Add methods and property accessors to the instance BEFORE field/property initializers
        // because initializers may call methods on 'this' (e.g., this.get_map().get_keyOrder())
        addMethodAttachments(irClass, className, bodyStatements)

        // Initialize fields (direct field declarations)
        val initializedFields = mutableSetOf<String>()
        for (field in irClass.declarations.filterIsInstance<IrField>()) {
            field.initializer?.expression?.let { initializer ->
                // Sanitize field name for special names like <this>
                val rawName = field.name.asString()
                val sanitizedName = when {
                    rawName == "<this>" -> "__this"
                    rawName.startsWith("<") && rawName.endsWith(">") ->
                        rawName.removePrefix("<").removeSuffix(">").replace("-", "_").replace(" ", "_")
                    else -> rawName.replace("$", "_")
                }
                initializedFields.add(sanitizedName)
                val transformedInit = transformExpression(initializer)
                // Consume hoisted statements from when-lowered blocks in the initializer
                val hoisted = takeHoistedStatements()
                bodyStatements.addAll(hoisted)
                bodyStatements.add(
                    BrsExpressionStatement(
                        BrsBinaryOp(
                            BrsDotAccess(BrsIdentifier("this"), sanitizedName),
                            BrsBinaryOperator.EQ,
                            transformedInit
                        )
                    )
                )
            }
        }

        // Also initialize property backing fields (may not be direct members of declarations)
        for (property in irClass.declarations.filterIsInstance<IrProperty>()) {
            val backingField = property.backingField ?: continue
            // Use backing field name, not property name - for delegated properties, the backing field
            // is named <propertyName>$delegate (e.g., lazyValue$delegate)
            // Also sanitize special names like <this>
            val rawFieldName = backingField.name.asString()
            val fieldName = when {
                rawFieldName == "<this>" -> "__this"
                rawFieldName.startsWith("<") && rawFieldName.endsWith(">") ->
                    rawFieldName.removePrefix("<").removeSuffix(">").replace("-", "_").replace(" ", "_")
                else -> rawFieldName.replace("$", "_")
            }
            if (fieldName !in initializedFields) {
                backingField.initializer?.expression?.let { initializer ->
                    val transformedInit = transformExpression(initializer)
                    // Consume hoisted statements from when-lowered blocks in the initializer
                    val hoisted = takeHoistedStatements()
                    bodyStatements.addAll(hoisted)
                    bodyStatements.add(
                        BrsExpressionStatement(
                            BrsBinaryOp(
                                BrsDotAccess(BrsIdentifier("this"), fieldName),
                                BrsBinaryOperator.EQ,
                                transformedInit
                            )
                        )
                    )
                }
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

        // Reset constructor context flag
        isInConstructorBody = false

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
     * Add default Any method implementations for classes that directly extend Any.
     *
     * All Kotlin classes implicitly extend Any, which provides equals, hashCode, and toString.
     * When a class has an explicit superclass, these methods are inherited from the parent.
     * When a class only extends Any (no explicit superclass), we need to attach these methods
     * directly so that structural equality checks and other operations work correctly.
     *
     * These are default implementations - if the class overrides any of these methods,
     * the overriding implementation will be attached later by addMethodAttachments and will
     * replace these defaults.
     */
    private fun addAnyMethodDefaults(bodyStatements: MutableList<BrsStatement>) {
        // All calls to equals/hashCode/toString use simple names (see call site generation),
        // so we only need to attach the simple name aliases. The mangled names are never called.

        // this.equals = Any_equals_AnyN_k_
        bodyStatements.add(
            BrsExpressionStatement(
                BrsBinaryOp(
                    BrsDotAccess(BrsIdentifier("this"), "equals"),
                    BrsBinaryOperator.EQ,
                    BrsIdentifier("Any_equals_AnyN_k_")
                )
            )
        )
        // this.hashCode = Any_hashCode_k_
        bodyStatements.add(
            BrsExpressionStatement(
                BrsBinaryOp(
                    BrsDotAccess(BrsIdentifier("this"), "hashCode"),
                    BrsBinaryOperator.EQ,
                    BrsIdentifier("Any_hashCode_k_")
                )
            )
        )
        // this.toString = Any_toString_k_
        bodyStatements.add(
            BrsExpressionStatement(
                BrsBinaryOp(
                    BrsDotAccess(BrsIdentifier("this"), "toString"),
                    BrsBinaryOperator.EQ,
                    BrsIdentifier("Any_toString_k_")
                )
            )
        )
    }

    /**
     * Add method and property accessor attachments to a class instance.
     * This attaches both IrSimpleFunction methods and property getter/setters.
     *
     * Important: Regular methods use mangled names (with type signatures) to support overloading.
     * Property accessors use simple names (via sanitizeMethodName) to match call sites.
     */
    // Synthetic data class method names that are handled separately with simple names
    private val syntheticDataClassMethods = setOf("equals", "hashCode", "toString", "copy")

    private fun addMethodAttachments(
        irClass: IrClass,
        className: String,
        bodyStatements: MutableList<BrsStatement>
    ) {
        val isDataClass = irClass.isData
        val isEnumClass = irClass.kind == ClassKind.ENUM_CLASS

        // Add regular methods (IrSimpleFunction)
        // Use mangled names (fullMethodName minus class prefix) to support overloading
        for (function in irClass.declarations.filterIsInstance<IrSimpleFunction>()) {
            if (!function.isFakeOverride && !function.isExternal) {
                val methodBaseName = function.name.asString()

                // Skip synthetic data class methods - they're attached separately with simple names
                // This prevents creating references like Pair_toString_Str_k_ which don't exist
                if (isDataClass && syntheticDataClassMethods.contains(methodBaseName)) {
                    continue
                }

                // Skip componentN methods for data classes - also handled separately
                if (isDataClass && methodBaseName.startsWith("component") && methodBaseName.drop(9).toIntOrNull() != null) {
                    continue
                }

                // Skip values and valueOf for enum classes - these are generated with custom implementations
                // and would have different names (ClassName_values vs ClassName_values_k_)
                if (isEnumClass && (methodBaseName == "values" || methodBaseName == "valueOf")) {
                    continue
                }

                val fullMethodName = context.getBrsName(function)
                val methodName = fullMethodName.removePrefix("${className}_")
                bodyStatements.add(
                    BrsExpressionStatement(
                        BrsBinaryOp(
                            BrsDotAccess(BrsIdentifier("this"), methodName),
                            BrsBinaryOperator.EQ,
                            BrsIdentifier(fullMethodName)
                        )
                    )
                )

                // For Any methods (equals, hashCode, toString), also add simple-named alias
                // This allows polymorphic calls like a.equals(b) where 'a' is Any
                if (!isDataClass && methodBaseName in listOf("equals", "hashCode", "toString")) {
                    bodyStatements.add(
                        BrsExpressionStatement(
                            BrsBinaryOp(
                                BrsDotAccess(BrsIdentifier("this"), methodBaseName),
                                BrsBinaryOperator.EQ,
                                BrsIdentifier(fullMethodName)
                            )
                        )
                    )
                }
            }
        }

        // Add property accessor methods (getter/setter)
        // Use sanitizeMethodName to match call site naming convention
        // Skip for data classes - their properties are constructor parameters accessed directly (e.g., m.first)
        if (!isDataClass) {
            for (property in irClass.declarations.filterIsInstance<IrProperty>()) {
                // Skip the synthetic 'entries' property for enum classes - it's generated separately
                if (isEnumClass && property.name.asString() == "entries") {
                    continue
                }
                property.getter?.let { getter ->
                    if (!getter.isFakeOverride && !getter.isExternal) {
                        val fullMethodName = context.getBrsName(getter)
                        val methodName = sanitizeMethodName(getter.name.asString())
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
                property.setter?.let { setter ->
                    if (!setter.isFakeOverride && !setter.isExternal) {
                        val fullMethodName = context.getBrsName(setter)
                        val methodName = sanitizeMethodName(setter.name.asString())
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
            }
        }
    }

    /**
     * Recursively collect all interface names from a class's supertypes.
     * This is used to populate the __proto chain for instanceof checks.
     */
    private fun collectAllInterfaceNames(irClass: IrClass): List<String> {
        val result = mutableSetOf<String>()
        val visited = mutableSetOf<IrClass>()

        fun collectInterfaces(type: IrType) {
            val classifier = type.classOrNull?.owner ?: return
            if (classifier in visited) return
            visited.add(classifier)

            if (classifier.isInterface) {
                result.add(context.getBrsName(classifier))
            }

            // Recursively collect supertype interfaces
            classifier.superTypes.forEach { collectInterfaces(it) }
        }

        irClass.superTypes.forEach { collectInterfaces(it) }
        return result.toList()
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
     * Returns a pair of (hoisted statements, argument expressions).
     * Hoisted statements should be inserted before the super call.
     *
     * This handles cases where arguments contain blocks (e.g., from safe calls like `message?.toString()`)
     * that need their prefix statements hoisted before the super call.
     */
    private fun getSuperConstructorArgsWithHoisting(constructor: IrConstructor): Pair<List<BrsStatement>, MutableList<BrsExpression>> {
        val delegatingCall = findDelegatingConstructorCall(constructor) ?: return Pair(emptyList(), mutableListOf())

        val hoistedStatements = mutableListOf<BrsStatement>()
        val arguments = mutableListOf<BrsExpression>()

        for (i in 0 until delegatingCall.valueArgumentsCount) {
            val arg = delegatingCall.getValueArgument(i) ?: continue

            // Flatten nested blocks and extract the final expression value
            val (stmts, valueExpr) = flattenBlockForHoisting(arg)
            stmts.forEach { stmt ->
                val transformed = when (stmt) {
                    is IrVariable -> {
                        val varName = stmt.name.asString()
                        // Sanitize variable names - handle special names and escape reserved keywords
                        val sanitizedVarName = sanitizeParameterName(varName)
                        val init = stmt.initializer?.let { transformExpression(it) }
                        if (init != null) BrsVariable(sanitizedVarName, mapTypeToBrs(stmt.type), init) else null
                    }
                    is IrWhen -> statementTransformer.visitWhen(stmt, Unit)
                    else -> transformStatement(stmt)
                }
                transformed?.let { hoistedStatements.add(it) }
            }

            if (valueExpr != null) {
                arguments.add(transformExpression(valueExpr))
            } else {
                arguments.add(BrsInvalidLiteral())
            }
        }

        return Pair(hoistedStatements, arguments)
    }

    /**
     * Recursively flatten nested blocks, extracting all prefix statements and the final value expression.
     * This handles patterns like:
     *   Block { val x = ..., Block { var tmp = null, When { ... }, GetValue(tmp) } }
     * Returns a pair of (all prefix statements to hoist, final value expression).
     */
    private fun flattenBlockForHoisting(expr: IrExpression): Pair<List<IrStatement>, IrExpression?> {
        if (expr !is IrBlock || expr.statements.isEmpty()) {
            return Pair(emptyList(), expr)
        }

        val allStatements = mutableListOf<IrStatement>()
        val blockStatements = expr.statements

        // Process all statements except the last
        for (i in 0 until blockStatements.size - 1) {
            allStatements.add(blockStatements[i])
        }

        // Check if the last statement is itself a block that needs flattening
        val lastStmt = blockStatements.lastOrNull()
        return when {
            lastStmt is IrBlock -> {
                // Recursively flatten the nested block
                val (nestedStmts, nestedValue) = flattenBlockForHoisting(lastStmt)
                allStatements.addAll(nestedStmts)
                Pair(allStatements, nestedValue)
            }
            lastStmt is IrExpression -> {
                Pair(allStatements, lastStmt)
            }
            else -> {
                Pair(allStatements, null)
            }
        }
    }

    /**
     * Get arguments for super constructor call from the delegating constructor call in the body.
     * This is a backward-compatible wrapper around getSuperConstructorArgsWithHoisting.
     */
    private fun getSuperConstructorArgs(constructor: IrConstructor): MutableList<BrsExpression> {
        return getSuperConstructorArgsWithHoisting(constructor).second
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
        // using GetGlobalAA() for module-level storage.
        // Skip constant initializers since they are inlined in visitGetField.
        property.backingField?.let { field ->
            if (field.parent is IrFile) {
                field.initializer?.expression?.let { initializer ->
                    // Skip constant expressions - they will be inlined in the getter
                    if (!isConstantExpression(initializer)) {
                        val propName = property.name.asString()
                        // Use GetGlobalAA().propName = value for module-level property initialization
                        statements.add(
                            BrsExpressionStatement(
                                BrsBinaryOp(
                                    BrsDotAccess(
                                        BrsFunctionCall(BrsIdentifier("GetGlobalAA"), mutableListOf()),
                                        propName
                                    ),
                                    BrsBinaryOperator.EQ,
                                    transformExpression(initializer)
                                )
                            )
                        )
                    }
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
            return expressionToStatementOrNull(transformExpression(statement))
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

    /**
     * Wrap a BRS expression in a statement, or return null if the expression
     * has no side effects and shouldn't be emitted as a standalone statement.
     */
    fun expressionToStatementOrNull(expr: BrsExpression): BrsStatement? {
        // Skip side-effect-free expressions (like simple variable reads, literals)
        return when (expr) {
            is BrsIdentifier,
            is BrsInvalidLiteral,
            is BrsIntLiteral,
            is BrsLongIntLiteral,
            is BrsDoubleLiteral,
            is BrsFloatLiteral,
            is BrsStringLiteral,
            is BrsBooleanLiteral -> null
            else -> BrsExpressionStatement(expr)
        }
    }

    // ==================== Helpers ====================

    /**
     * Check if an IR expression is a compile-time constant that can be inlined.
     * Recognizes:
     * - Primitive constants (IrConst)
     * - Enum value references (IrGetEnumValue)
     * - Property accesses on enum values for ordinal, name, and constant properties
     */
    fun isConstantExpression(expression: IrExpression): Boolean {
        return when (expression) {
            is IrConst -> true
            is IrGetEnumValue -> true  // Enum constants are always statically known
            is IrCall -> {
                // Check for property access on constant enum value
                val receiver = expression.dispatchReceiver
                if (receiver is IrGetEnumValue) {
                    val entry = receiver.symbol.owner
                    val functionName = expression.symbol.owner.name.asString()
                    when {
                        functionName == "<get-ordinal>" || functionName == "ordinal" -> true
                        functionName == "<get-name>" || functionName == "name" -> true
                        functionName.startsWith("<get-") -> {
                            val propName = functionName.removePrefix("<get-").removeSuffix(">")
                            context.getEnumConstantProperties(entry)?.containsKey(propName) == true
                        }
                        else -> false
                    }
                } else {
                    false
                }
            }
            else -> false
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
            // Nothing maps to Dynamic for parameters (Void only valid for return types)
            type.isNothing() -> BrsType.DYNAMIC
            type.isNullable() -> BrsType.DYNAMIC
            // Function types are implemented as closure objects (AA with invoke method), not as
            // BrightScript Function type. Map to Object to accept closure objects as arguments.
            type.isFunction() -> BrsType.OBJECT
            else -> BrsType.OBJECT
        }
    }

    // ==================== ToString Transformation ====================

    /**
     * Transform a toString() call to appropriate BrightScript code.
     * BrightScript primitives don't have methods, so we need to handle each type specially.
     */
    fun transformToString(receiverExpr: BrsExpression, receiverType: IrType): BrsExpression {
        return when {
            // String: just return the string itself (pass-through)
            receiverType.isString() -> receiverExpr

            // Char: already a string in BrightScript, just pass through
            receiverType.isChar() -> receiverExpr

            // Numeric types: use __kotlin_numToStr() helper function
            // BrightScript's Str() adds a leading space for positive numbers
            // We use a helper function because anonymous functions can't call global built-ins
            // Note: Method names do NOT include return types (like Java) to support polymorphism
            receiverType.isInt() || receiverType.isShort() || receiverType.isByte() ||
            receiverType.isLong() || receiverType.isFloat() || receiverType.isDouble() -> {
                // Choose the appropriate overload based on type
                val funcName = when {
                    receiverType.isInt() || receiverType.isShort() || receiverType.isByte() -> "__kotlin_numToStr_I_k_"
                    receiverType.isLong() -> "__kotlin_numToStr_J_k_"
                    receiverType.isFloat() -> "__kotlin_numToStr_F_k_"
                    receiverType.isDouble() -> "__kotlin_numToStr_D_k_"
                    else -> "__kotlin_numToStr_AnyN_k_"
                }
                BrsFunctionCall(
                    BrsIdentifier(funcName),
                    mutableListOf(receiverExpr)
                )
            }

            // Boolean: use conditional to return "true" or "false"
            receiverType.isBoolean() -> {
                BrsConditional(
                    receiverExpr,
                    BrsStringLiteral("true"),
                    BrsStringLiteral("false")
                )
            }

            // Any?, Dynamic, nullable types, or type parameters: use runtime type checking
            // since BrightScript primitives don't have .toString() method.
            // Type parameters must use runtime checking because at runtime T could be
            // a primitive (Int, String, Boolean, etc.) which don't have .toString() methods.
            receiverType.isNullable() || receiverType.isAny() || receiverType.isTypeParameter() -> {
                generateRuntimeToString(receiverExpr)
            }

            // Dynamic type and external interfaces: use runtime type checking
            // These are native BrightScript types that don't have a toString() method
            isDynamicType(receiverType) || isExternalInterfaceType(receiverType) -> {
                generateRuntimeToString(receiverExpr)
            }

            // Non-nullable objects: call toString method
            else -> {
                BrsMethodCall(receiverExpr, "toString", mutableListOf())
            }
        }
    }

    /**
     * Generate runtime type-checking toString logic for Any? types.
     * This is used by brsIntrinsicToString when the type is not known at compile time.
     *
     * Instead of generating inline nested conditionals (which become IIFEs with scope issues),
     * we call the stdlib toString_AnyN_k_ function which handles all types properly.
     * Note: Method names do NOT include return types (like Java) to support polymorphism.
     */
    fun generateRuntimeToString(valueExpr: BrsExpression): BrsExpression {
        // Call the stdlib toString function which handles all type checking
        // This avoids nested IIFEs that cause scope issues with global built-in functions
        return BrsFunctionCall(
            BrsIdentifier("toString_AnyN_k_"),
            mutableListOf(valueExpr)
        )
    }

    /**
     * Checks if a type is the Dynamic type (kotlin.brs.Dynamic).
     * Dynamic type needs runtime type checking for toString since it can hold any value.
     */
    private fun isDynamicType(type: IrType): Boolean {
        val classifier = type.classifierOrNull
        if (classifier !is IrClassSymbol) return false
        return classifier.owner.fqNameWhenAvailable?.asString() == "kotlin.brs.Dynamic"
    }

    /**
     * Checks if a type is an external interface (native BrightScript type).
     * External interfaces don't have Kotlin methods like toString().
     */
    private fun isExternalInterfaceType(type: IrType): Boolean {
        val irClass = type.classOrNull?.owner ?: return false
        return irClass.isExternal || isExternalClass(irClass)
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

    // ==================== Visitor Implementation ====================

    override fun visitElement(element: IrElement, data: Unit): BrsNode? {
        // Default implementation - should be handled by specialized transformers
        return null
    }

    /**
     * Check if a class is an extracted local class (lambda or function reference).
     * These classes are extracted from function bodies by BrsLocalClassExtractionLowering
     * but their code is still generated inline in the containing file, not as separate .brs files.
     */
    internal fun isExtractedLocalClass(irClass: IrClass): Boolean {
        // Check for lambda or function reference origin
        val origin = irClass.origin
        if (origin == org.jetbrains.kotlin.backend.common.lower.WebCallableReferenceLowering.LAMBDA_IMPL ||
            origin == org.jetbrains.kotlin.backend.common.lower.WebCallableReferenceLowering.FUNCTION_REFERENCE_IMPL) {
            return true
        }
        // Also check if parent is still a function (before extraction)
        if (irClass.parent is IrFunction) {
            return true
        }
        return false
    }

    /**
     * Determine the .brs file name a class's constructor will be compiled to.
     * Returns null if no dependency should be recorded (e.g., external or local class).
     */
    internal fun determineBrsFileNameForClass(irClass: IrClass): String? {
        // External classes don't produce output files
        if (irClass.isExternal) return null

        // Extracted local classes (lambdas, anonymous) are inlined
        if (isExtractedLocalClass(irClass)) return null

        // Check manifest first - this handles stdlib classes correctly
        val constructor = irClass.declarations.filterIsInstance<IrConstructor>().firstOrNull()
        if (constructor != null) {
            val constructorName = context.getBrsName(constructor)
            context.dependencyFunctionManifest[constructorName]?.let { return it }
        }

        // For companion objects, look up the containing class instead.
        // Companion objects are compiled into the same file as their containing class.
        if (irClass.isCompanion) {
            val containingClass = irClass.parent as? IrClass
            if (containingClass != null) {
                return determineBrsFileNameForClass(containingClass)
            }
        }

        // Find the containing file
        var parent: org.jetbrains.kotlin.ir.declarations.IrDeclarationParent = irClass.parent
        while (parent !is IrFile && parent is org.jetbrains.kotlin.ir.declarations.IrDeclaration) {
            parent = (parent as org.jetbrains.kotlin.ir.declarations.IrDeclaration).parent
        }

        return when (parent) {
            is IrFile -> {
                // Top-level class - use source file name
                java.io.File(parent.path).nameWithoutExtension + "Kt.brs"
            }
            else -> {
                // Fallback to class name (for nested classes in same-module code)
                context.getBrsName(irClass) + "Kt.brs"
            }
        }
    }
}

/**
 * Transforms IR statements to BrightScript statements.
 */
class IrStatementToBrsTransformer(
    private val parent: IrToBrsTransformer,
    private val context: BrsIrBackendContext
) : IrVisitor<BrsStatement?, Unit>() {

    override fun visitElement(element: IrElement, data: Unit): BrsStatement? = null

    override fun visitCall(expression: IrCall, data: Unit): BrsStatement {
        return visitCallAsStatement(expression)
    }

    override fun visitTypeOperator(expression: IrTypeOperatorCall, data: Unit): BrsStatement? {
        // Handle IMPLICIT_COERCION_TO_UNIT by unwrapping to the inner expression/block
        // This handles cases like `toIndex++` used as a statement, where the increment block
        // is wrapped in IMPLICIT_COERCION_TO_UNIT because the Int result is discarded
        if (expression.operator == IrTypeOperator.IMPLICIT_COERCION_TO_UNIT) {
            val innerArg = expression.argument
            return when (innerArg) {
                is IrBlock -> visitBlock(innerArg, data)
                is IrWhen -> visitWhen(innerArg, data)
                is IrCall -> visitCall(innerArg, data)
                else -> {
                    // For other expressions, transform and wrap as expression statement
                    val expr = parent.transformExpression(innerArg)
                    val hoisted = parent.takeHoistedStatements()
                    if (hoisted.isNotEmpty()) {
                        BrsBlock((hoisted + BrsExpressionStatement(expr)).toMutableList())
                    } else {
                        BrsExpressionStatement(expr)
                    }
                }
            }
        }
        return null
    }

    override fun visitVariable(declaration: IrVariable, data: Unit): BrsStatement {
        val initializer = declaration.initializer

        // Check if this variable is shared (captured by closure and mutable)
        // Two detection mechanisms:
        // 1. Via SharedVariablesLowering which sets SHARED_VARIABLE_WRAPPER origin
        // 2. Via BrsSharedVariableDetectionLowering which populates sharedVariables set
        val isShared = declaration.origin == BrsDeclarationOrigin.SHARED_VARIABLE_WRAPPER ||
                       declaration.symbol in parent.sharedVariables

        // Get a unique variable name to avoid collision with other variables
        // that may have the same name in the IR (e.g., from multiple inline expansions)
        val baseName = sanitizeParameterName(declaration.name.asString())
        val uniqueName = parent.getUniqueVariableName(declaration.symbol, baseName)

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
                var lastExpr: BrsExpression = when (last) {
                    is BrsExpressionStatement -> last.expression
                    is BrsVariable -> last.initializer ?: BrsInvalidLiteral()
                    else -> BrsInvalidLiteral()
                }
                // Box shared variables
                if (isShared) {
                    lastExpr = BrsAALiteral(mutableListOf(BrsAAEntry("value", lastExpr)))
                }
                statements.add(BrsVariable(
                    name = uniqueName,
                    type = parent.mapTypeToBrs(declaration.type),
                    initializer = lastExpr
                ))
            } else {
                var initExpr: BrsExpression = BrsInvalidLiteral()
                if (isShared) {
                    initExpr = BrsAALiteral(mutableListOf(BrsAAEntry("value", initExpr)))
                }
                statements.add(BrsVariable(
                    name = uniqueName,
                    type = parent.mapTypeToBrs(declaration.type),
                    initializer = initExpr
                ))
            }

            return BrsBlock(statements)
        }

        // Simple case - the expression transformer handles when-lowered blocks via hoisting
        // Transform the initializer, which may add hoisted statements
        val transformedInit = initializer?.let { parent.transformExpression(it) }

        // If shared (already checked above), wrap the initializer in {value: ...} box
        val finalInit = if (isShared && transformedInit != null) {
            // Box the value: {value: initializer}
            BrsAALiteral(mutableListOf(BrsAAEntry("value", transformedInit)))
        } else {
            transformedInit
        }

        // Check for hoisted statements from when-lowered blocks
        val hoisted = parent.takeHoistedStatements()
        return if (hoisted.isEmpty()) {
            BrsVariable(
                name = uniqueName,
                type = parent.mapTypeToBrs(declaration.type),
                initializer = finalInit
            )
        } else {
            // Prepend hoisted statements before the variable declaration
            val allStatements = hoisted.toMutableList()
            allStatements.add(BrsVariable(
                name = uniqueName,
                type = parent.mapTypeToBrs(declaration.type),
                initializer = finalInit
            ))
            BrsBlock(allStatements)
        }
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
                    // Check if this is a FOR_LOOP block - if so, increment the nesting counter
                    // so that any break statements inside get "exit while" instead of "exit for"
                    val isForLoop = stmt.origin == IrStatementOrigin.FOR_LOOP
                    if (isForLoop) {
                        parent.forLoopToWhileNestingDepth++
                    }
                    try {
                        // Recursively flatten nested blocks
                        flattenBlockStatements(stmt.statements, output, data)
                    } finally {
                        if (isForLoop) {
                            parent.forLoopToWhileNestingDepth--
                        }
                    }
                }
                stmt is IrVariable -> {
                    output.add(visitVariable(stmt, data))
                }
                stmt is IrWhen -> {
                    // When statement - transform using statement transformer
                    parent.transformStatement(stmt)?.let { output.add(it) }
                }
                stmt is IrExpression -> {
                    val expr = parent.transformExpression(stmt)
                    // For the LAST expression, always add it as an expression statement
                    // because it may be the value of the block (e.g., __when_tmp variable read)
                    // For non-last expressions, skip side-effect-free ones
                    if (isLast) {
                        // Always add last expression - it's the block's value
                        output.add(BrsExpressionStatement(expr))
                    } else if (expr !is BrsIdentifier && expr !is BrsInvalidLiteral &&
                        expr !is BrsIntLiteral && expr !is BrsDoubleLiteral &&
                        expr !is BrsStringLiteral && expr !is BrsBooleanLiteral) {
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
        val rawParameters = declaration.valueParameters.map { param ->
            BrsParameter(
                name = sanitizeParameterName(param.name.asString()),
                type = parent.mapTypeToBrs(param.type)
            )
        }
        val parameters = deduplicateParameterNames(rawParameters)

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
        // Transform the expression - hoisted statements from when-lowered blocks go to current scope
        val transformedValue = parent.transformExpression(expression.value)
        val hoisted = parent.takeHoistedStatements()

        // If the transformed value is a BrsStatementAsExpression wrapping a control-flow statement
        // (throw, return, exit, continue), we should emit that statement directly instead of
        // wrapping it in a return. These statements don't produce values and "return throw x" is invalid.
        if (transformedValue is BrsStatementAsExpression) {
            val innerStmt = transformedValue.statement
            if (innerStmt is BrsThrow || innerStmt is BrsReturn || innerStmt is BrsExit || innerStmt is BrsContinue) {
                return if (hoisted.isNotEmpty()) {
                    val allStatements = hoisted.toMutableList()
                    allStatements.add(innerStmt)
                    BrsBlock(allStatements)
                } else {
                    innerStmt
                }
            }
        }

        val returnTarget = expression.returnTargetSymbol.owner

        // Returns to returnable blocks (from inlined functions) after lowering
        // The BrsReturnableBlockLowering transforms:
        //   return@block value  ->  result = value; return@block Unit
        // And wraps the block in: while true { ... exit while } end while
        //
        // So when we see return@block Unit, we should emit "exit while" to break out of the wrapper loop.
        // The result variable has already been set by the lowering.
        //
        // IMPORTANT: If we're using flag-based approach (because the return is inside a while loop),
        // we need to set the flag before exiting.
        if (returnTarget !is IrFunction) {
            // This is return@block Unit (after lowering) - emit exit while
            val statements = hoisted.toMutableList()

            // If there's a flag on the stack, we're inside a flag-based returnable block
            val flagName = parent.returnableBlockFlagStack.lastOrNull()
            if (flagName != null) {
                statements.add(BrsExpressionStatement(
                    BrsBinaryOp(BrsIdentifier(flagName), BrsBinaryOperator.EQ, BrsBooleanLiteral(true))
                ))
            }
            statements.add(BrsExit(BrsExitKind.WHILE))
            return if (statements.size == 1) statements[0] else BrsBlock(statements)
        }

        // Check if the function we're returning from expects a value
        // (e.g., suspend functions have their return type changed from Unit to Any?)
        val functionReturnsValue = !returnTarget.returnType.isUnit() && !returnTarget.returnType.isNothing()

        // For Unit return types, we still need to execute the expression (it may have side effects)
        // but we don't return a value. Emit the expression as a statement followed by empty return.
        // EXCEPTION: If the function expects a return value (like suspend functions returning Any?),
        // we must return the expression result even if it's Unit-typed, because the actual runtime
        // value may be COROUTINE_SUSPENDED which needs to propagate to the caller.
        if (expression.value.type.isUnit()) {
            if (functionReturnsValue) {
                // Function expects a return value - return the expression result even if it's Unit-typed
                // This is critical for suspend functions where Unit expressions may actually carry
                // suspension signals (COROUTINE_SUSPENDED) at runtime
                val statements = hoisted.toMutableList()
                statements.add(BrsReturn(value = transformedValue))
                return if (statements.size == 1) statements[0] else BrsBlock(statements)
            } else {
                // Function doesn't expect return value - emit as statement then return nothing
                val statements = hoisted.toMutableList()
                // Only add the expression as a statement if it's not just a literal/identifier
                // (to avoid emitting standalone "invalid" statements)
                if (transformedValue !is BrsInvalidLiteral && transformedValue !is BrsIdentifier) {
                    statements.add(BrsExpressionStatement(transformedValue))
                }
                statements.add(BrsReturn(value = null))
                return if (statements.size == 1) statements[0] else BrsBlock(statements)
            }
        }

        return if (hoisted.isNotEmpty()) {
            val allStatements = hoisted.toMutableList()
            allStatements.add(BrsReturn(value = transformedValue))
            BrsBlock(allStatements)
        } else {
            BrsReturn(value = transformedValue)
        }
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
        // Helper to transform a statement/expression inside try/catch blocks
        // IrWhen needs special handling because it's technically an IrExpression
        // but when used in statement context (like `if (x) return`) it should be
        // transformed as a statement, not an expression (which returns BrsInvalidLiteral for Unit)
        fun transformBlockStatement(stmt: IrStatement): BrsStatement? {
            return when (stmt) {
                is IrWhen -> visitWhen(stmt, data) // Transform as statement (if-then-else)
                is IrExpression -> BrsExpressionStatement(parent.transformExpression(stmt))
                else -> parent.transformStatement(stmt)
            }
        }

        return if (context.supportsExceptions) {
            // Transform try block - tryResult is an IrExpression (often IrBlock)
            val tryBlock = when (val tryResult = aTry.tryResult) {
                is IrBlock -> {
                    val statements = tryResult.statements.mapNotNull { stmt ->
                        transformBlockStatement(stmt)
                    }
                    BrsBlock(statements.toMutableList())
                }
                else -> BrsBlock(mutableListOf(BrsExpressionStatement(parent.transformExpression(tryResult))))
            }

            // Transform catch block
            val catchBlock = aTry.catches.firstOrNull()?.let { catch ->
                when (val catchResult = catch.result) {
                    is IrBlock -> {
                        val statements = catchResult.statements.mapNotNull { stmt ->
                            transformBlockStatement(stmt)
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
                is IrBlock -> {
                    val statements = tryResult.statements.mapNotNull { stmt ->
                        transformBlockStatement(stmt)
                    }
                    BrsBlock(statements.toMutableList())
                }
                else -> BrsExpressionStatement(parent.transformExpression(tryResult))
            }
        }
    }

    override fun visitWhen(expression: IrWhen, data: Unit): BrsStatement {
        // Collect statements that must precede the if chain (e.g., variable declarations
        // from ELVIS blocks that are referenced in conditions)
        val precedingStatements = mutableListOf<BrsStatement>()

        // Transform to if/else chain
        var result: BrsIf? = null
        var current: BrsIf? = null

        for (branch in expression.branches) {
            val condition = parent.transformExpression(branch.condition)

            // CRITICAL: Consume hoisted statements from condition transformation.
            // This handles cases like elvis operator where the condition references
            // a variable that was declared in an enclosing ELVIS block.
            precedingStatements.addAll(parent.takeHoistedStatements())

            // Handle branch result - certain IR nodes need statement transformation
            val bodyStatement: BrsStatement = when (val branchResult = branch.result) {
                is IrReturn -> parent.transformStatement(branchResult) ?: BrsEmpty()
                is IrThrow -> visitThrow(branchResult, data)
                is IrBreak -> visitBreak(branchResult, data)
                is IrContinue -> visitContinue(branchResult, data)
                is IrSetValue -> visitSetValue(branchResult, data)
                is IrSetField -> visitSetField(branchResult, data)
                is IrBlock -> {
                    // Transform block which may contain returns
                    val transformed = transformBlockOrStatement(branchResult)
                    transformed
                }
                is IrComposite -> {
                    // Transform composite (used in coroutine state machine branches)
                    val transformed = transformBlockOrStatement(branchResult)
                    transformed
                }
                // IrTypeOperatorCall with IMPLICIT_COERCION_TO_UNIT wraps expressions used as statements
                is IrTypeOperatorCall -> {
                    if (branchResult.operator == IrTypeOperator.IMPLICIT_COERCION_TO_UNIT) {
                        val innerArg = branchResult.argument
                        when (innerArg) {
                            is IrBlock -> transformBlockOrStatement(innerArg)
                            is IrCall -> BrsExpressionStatement(parent.transformExpression(innerArg))
                            else -> {
                                val body = parent.transformExpression(branchResult)
                                BrsExpressionStatement(body)
                            }
                        }
                    } else {
                        val body = parent.transformExpression(branchResult)
                        BrsExpressionStatement(body)
                    }
                }
                else -> {
                    // Null constants in statement context should produce no code.
                    // This handles safe-call null branches: a?.foo() where null branch does nothing.
                    if (branchResult is IrConst && (branchResult as IrConst).value == null) {
                        BrsEmpty()
                    } else {
                        val body = parent.transformExpression(branchResult)
                        BrsExpressionStatement(body)
                    }
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

        val ifChain = result ?: BrsEmpty()

        // If we have preceding statements (e.g., variable declarations from ELVIS blocks),
        // wrap the if chain in a block so they're emitted first
        return if (precedingStatements.isNotEmpty()) {
            precedingStatements.add(ifChain)
            BrsBlock(precedingStatements.toMutableList())
        } else {
            ifChain
        }
    }

    override fun visitWhileLoop(loop: IrWhileLoop, data: Unit): BrsStatement {
        val condition = parent.transformExpression(loop.condition)
        // Take hoisted statements from condition transformation (e.g., from inlined returnable blocks)
        val conditionHoisted = parent.takeHoistedStatements()
        val bodyContainsContinue = parent.containsContinueFor(loop.body, loop)

        if (!context.supportsContinue && bodyContainsContinue) {
            // Wrap body in inner while(true) loop to simulate continue.
            // continue becomes "exit while" which exits the inner loop, and the outer loop continues.
            // break needs special handling: set a flag, exit inner, check flag after inner loop.
            val bodyContainsBreak = parent.containsBreakFor(loop.body, loop)
            val breakFlagName = if (bodyContainsBreak) "__break${parent.nextTempId()}" else null

            // Register this loop as having a continue wrapper
            parent.loopsWithContinueWrapper.add(loop)
            if (breakFlagName != null) {
                parent.loopBreakFlags[loop] = breakFlagName
            }

            // Push this loop onto the stack BEFORE transforming its body.
            // This ensures that only IrContinue/IrBreak that are encountered
            // during the body transformation will be converted to exit while.
            parent.continueWrapperLoopStack.add(loop)

            try {
                // Transform the body (continue/break will be handled by visitContinue/visitBreak)
                val innerBody = loop.body?.let { transformBlockOrStatement(it) } ?: BrsBlock()

                // Ensure inner loop always exits at the end (normal flow)
                val innerBodyStatements = when (innerBody) {
                    is BrsBlock -> innerBody.statements.toMutableList()
                    else -> mutableListOf(innerBody)
                }
                // Add exit while at end if not already terminating
                if (innerBodyStatements.isEmpty() || !isTerminating(innerBodyStatements.last())) {
                    innerBodyStatements.add(BrsExit(BrsExitKind.WHILE))
                }

                val innerLoop = BrsWhile(
                    condition = BrsBooleanLiteral(true),
                    body = BrsBlock(innerBodyStatements)
                )

                // Build outer loop body
                val outerBodyStatements = mutableListOf<BrsStatement>()
                if (breakFlagName != null) {
                    // Initialize break flag to false at start of each iteration
                    outerBodyStatements.add(BrsVariable(breakFlagName, null, BrsBooleanLiteral(false)))
                }
                outerBodyStatements.add(innerLoop)
                if (breakFlagName != null) {
                    // Check break flag after inner loop
                    outerBodyStatements.add(
                        BrsIf(
                            condition = BrsIdentifier(breakFlagName),
                            thenBranch = BrsExit(BrsExitKind.WHILE),
                            elseBranch = null
                        )
                    )
                }

                // Handle hoisted statements from complex while conditions
                if (conditionHoisted.isNotEmpty()) {
                    // Add condition check at start and re-evaluation at end
                    val fullBodyStatements = mutableListOf<BrsStatement>()
                    // Condition check
                    fullBodyStatements.add(
                        BrsIf(
                            condition = BrsUnaryOp(BrsUnaryOperator.NOT, condition),
                            thenBranch = BrsExit(BrsExitKind.WHILE),
                            elseBranch = null
                        )
                    )
                    fullBodyStatements.addAll(outerBodyStatements)
                    // Re-evaluate condition
                    fullBodyStatements.addAll(conditionHoisted)

                    val whileLoop = BrsWhile(
                        condition = BrsBooleanLiteral(true),
                        body = BrsBlock(fullBodyStatements)
                    )
                    return BrsBlock((conditionHoisted + whileLoop).toMutableList())
                }

                return BrsWhile(
                    condition = condition,
                    body = BrsBlock(outerBodyStatements)
                )
            } finally {
                // Clean up tracking - remove from stack and sets
                parent.continueWrapperLoopStack.remove(loop)
                parent.loopsWithContinueWrapper.remove(loop)
                if (breakFlagName != null) {
                    parent.loopBreakFlags.remove(loop)
                }
            }
        } else {
            // Handle hoisted statements from complex while conditions (e.g., inlined returnable blocks)
            // For conditions like `while (queue.isNotEmpty())` where isNotEmpty is an inlined function,
            // the condition transformation produces hoisted statements that set up a temporary variable.
            // We need to:
            // 1. Run the hoisted statements before the loop (initial condition evaluation)
            // 2. Check the condition at the start of the loop
            // 3. Run the hoisted statements at the end of the loop body (re-evaluate for next iteration)
            if (conditionHoisted.isNotEmpty()) {
                val body = loop.body?.let { transformBlockOrStatement(it) } ?: BrsBlock()
                val bodyStatements = when (body) {
                    is BrsBlock -> body.statements.toMutableList()
                    else -> mutableListOf(body)
                }

                // Build: while true { if not condition then exit while end if; body; hoisted }
                val loopBody = mutableListOf<BrsStatement>()
                // Condition check at start of loop
                loopBody.add(
                    BrsIf(
                        condition = BrsUnaryOp(BrsUnaryOperator.NOT, condition),
                        thenBranch = BrsExit(BrsExitKind.WHILE),
                        elseBranch = null
                    )
                )
                // Original body
                loopBody.addAll(bodyStatements)
                // Re-evaluate condition for next iteration
                loopBody.addAll(conditionHoisted)

                val whileLoop = BrsWhile(
                    condition = BrsBooleanLiteral(true),
                    body = BrsBlock(loopBody)
                )

                // Return: hoisted statements + while loop
                return BrsBlock((conditionHoisted + whileLoop).toMutableList())
            }

            return BrsWhile(
                condition = condition,
                body = loop.body?.let { transformBlockOrStatement(it) } ?: BrsBlock()
            )
        }
    }

    /**
     * Checks if a statement is terminating (return, exit, throw, etc.)
     */
    private fun isTerminating(stmt: BrsStatement): Boolean {
        return when (stmt) {
            is BrsReturn -> true
            is BrsExit -> true
            is BrsThrow -> true
            is BrsContinue -> true
            is BrsBlock -> stmt.statements.isNotEmpty() && isTerminating(stmt.statements.last())
            else -> false
        }
    }

    override fun visitDoWhileLoop(loop: IrDoWhileLoop, data: Unit): BrsStatement {
        // Coroutine root loops have special handling - use simple transformation
        // The loop is structured as: do { try { state machine } catch { ... } } while(true)
        // Continue statements become "exit while" (handled in visitContinue)
        // which exits the try block and re-enters the while loop from the top.
        if (loop.origin == BrsStatementOrigins.COROUTINE_ROOT_LOOP) {
            val body = loop.body?.let { transformBlockOrStatement(it) } ?: BrsBlock()
            val condition = parent.transformExpression(loop.condition)
            // do { body } while(true) → while(true) { body }
            // For coroutines, condition is always true, so we just use a while(true)
            return BrsWhile(condition, body)
        }

        // BrightScript doesn't have do-while, transform to while with entry guard
        // First execution runs unconditionally, then subsequent iterations check condition
        val bodyContainsContinue = parent.containsContinueFor(loop.body, loop)

        if (!context.supportsContinue && bodyContainsContinue) {
            // Similar to while loop, but do-while executes body first, then checks condition
            // We transform to: body; while(condition) { body }
            // But with continue wrapper for the while part
            val bodyContainsBreak = parent.containsBreakFor(loop.body, loop)
            val breakFlagName = if (bodyContainsBreak) "__break${parent.nextTempId()}" else null

            // Register this loop as having a continue wrapper
            parent.loopsWithContinueWrapper.add(loop)
            if (breakFlagName != null) {
                parent.loopBreakFlags[loop] = breakFlagName
            }

            // Transform the body for the first (unconditional) execution
            // The first execution is NOT inside the loop, so continues should NOT
            // be converted to exit while. We don't push to the stack yet.
            val firstBody = loop.body?.let { transformBlockOrStatement(it) } ?: BrsBlock()

            // Now push to the stack for the while loop body transformation
            parent.continueWrapperLoopStack.add(loop)

            try {
                // Transform the body again for the while loop (with continue wrapper)
                val innerBody = loop.body?.let { transformBlockOrStatement(it) } ?: BrsBlock()

                val condition = parent.transformExpression(loop.condition)

                // Ensure inner loop always exits at the end
                val innerBodyStatements = when (innerBody) {
                    is BrsBlock -> innerBody.statements.toMutableList()
                    else -> mutableListOf(innerBody)
                }
                if (innerBodyStatements.isEmpty() || !isTerminating(innerBodyStatements.last())) {
                    innerBodyStatements.add(BrsExit(BrsExitKind.WHILE))
                }

                val innerLoop = BrsWhile(
                    condition = BrsBooleanLiteral(true),
                    body = BrsBlock(innerBodyStatements)
                )

                // Build while loop body
                val whileBodyStatements = mutableListOf<BrsStatement>()
                if (breakFlagName != null) {
                    whileBodyStatements.add(BrsVariable(breakFlagName, null, BrsBooleanLiteral(false)))
                }
                whileBodyStatements.add(innerLoop)
                if (breakFlagName != null) {
                    whileBodyStatements.add(
                        BrsIf(
                            condition = BrsIdentifier(breakFlagName),
                            thenBranch = BrsExit(BrsExitKind.WHILE),
                            elseBranch = null
                        )
                    )
                }

                val whileLoop = BrsWhile(
                    condition = condition,
                    body = BrsBlock(whileBodyStatements)
                )

                // For do-while with break in first body execution, we need similar handling
                // But it's tricky since there's no outer loop. For now, handle simple case.
                return BrsBlock(mutableListOf(firstBody, whileLoop))
            } finally {
                // Clean up tracking - remove from stack and sets
                parent.continueWrapperLoopStack.remove(loop)
                parent.loopsWithContinueWrapper.remove(loop)
                if (breakFlagName != null) {
                    parent.loopBreakFlags.remove(loop)
                }
            }
        } else {
            // Original transformation without continue wrapper
            val body = loop.body?.let { transformBlockOrStatement(it) } ?: BrsBlock()
            val condition = parent.transformExpression(loop.condition)

            return BrsBlock(mutableListOf(
                body,
                BrsWhile(condition, body.deepCopy())
            ))
        }
    }

    override fun visitBreak(jump: IrBreak, data: Unit): BrsStatement {
        // Check if this break targets a loop with a continue wrapper (simulated continue)
        // In this case, we need to set the break flag and exit the inner wrapper loop.
        // The outer loop will check the flag and exit.
        // We check the stack to ensure we're inside the loop body transformation.
        val breakFlagName = parent.loopBreakFlags[jump.loop]
        if (breakFlagName != null && parent.continueWrapperLoopStack.contains(jump.loop)) {
            // Set break flag to true, then exit the inner while loop
            return BrsBlock(mutableListOf(
                BrsExpressionStatement(
                    BrsBinaryOp(
                        BrsIdentifier(breakFlagName),
                        BrsBinaryOperator.EQ,
                        BrsBooleanLiteral(true)
                    )
                ),
                BrsExit(BrsExitKind.WHILE)
            ))
        }

        // Check if the loop this break references was transformed to a while loop
        // This happens when a for-each loop uses Strategy 4 (iterator protocol),
        // which generates a while loop instead of a native for-each
        // First check: are we inside a FOR_LOOP that's being transformed to while?
        // This handles cases where inlining creates new loop instances that we can't track by identity.
        if (parent.forLoopToWhileNestingDepth > 0 &&
            (jump.loop.origin == IrStatementOrigin.FOR_LOOP_INNER_WHILE ||
             jump.loop.origin == IrStatementOrigin.FOR_LOOP)) {
            return BrsExit(BrsExitKind.WHILE)
        }

        // Second check: is this specific loop instance in our tracking set?
        if (parent.loopsTransformedToWhile.contains(jump.loop)) {
            return BrsExit(BrsExitKind.WHILE)
        }

        // Determine exit kind based on the loop type
        // FOR_LOOP origin indicates a for or for-each loop
        val exitKind = when (jump.loop.origin) {
            IrStatementOrigin.FOR_LOOP,
            IrStatementOrigin.FOR_LOOP_INNER_WHILE -> BrsExitKind.FOR
            else -> BrsExitKind.WHILE
        }
        return BrsExit(exitKind)
    }

    override fun visitContinue(jump: IrContinue, data: Unit): BrsStatement {
        // Coroutine root loops use while(true) with try/catch inside.
        // The state machine uses continue to re-enter the loop from any state.
        // When a suspend call returns immediately (doesn't suspend), we need to
        // continue the loop to re-read the state and execute the next state.
        // This requires "continue while" to restart the loop iteration.
        if (jump.loop.origin == BrsStatementOrigins.COROUTINE_ROOT_LOOP) {
            return BrsContinue(BrsContinueKind.WHILE)
        }

        // Check if this continue targets a loop with a continue wrapper
        // In this case, emit "exit while" to exit the inner wrapper loop,
        // which allows the outer loop to continue naturally.
        // We check the stack (not just the set) to ensure we're actually inside
        // the loop body transformation. This is important for coroutines where
        // IrContinue statements may exist both inside and outside the loop.
        if (parent.continueWrapperLoopStack.contains(jump.loop)) {
            return BrsExit(BrsExitKind.WHILE)
        }

        // First check: are we inside a FOR_LOOP that's being transformed to while?
        if (parent.forLoopToWhileNestingDepth > 0 &&
            (jump.loop.origin == IrStatementOrigin.FOR_LOOP_INNER_WHILE ||
             jump.loop.origin == IrStatementOrigin.FOR_LOOP)) {
            return if (context.supportsContinue) {
                BrsContinue(BrsContinueKind.WHILE)
            } else {
                BrsComment("continue not supported on target Roku OS")
            }
        }

        // Second check: is this specific loop instance in our tracking set?
        if (parent.loopsTransformedToWhile.contains(jump.loop)) {
            return if (context.supportsContinue) {
                BrsContinue(BrsContinueKind.WHILE)
            } else {
                BrsComment("continue not supported on target Roku OS")
            }
        }

        // Determine continue kind based on the loop type
        val continueKind = when (jump.loop.origin) {
            IrStatementOrigin.FOR_LOOP,
            IrStatementOrigin.FOR_LOOP_INNER_WHILE -> BrsContinueKind.FOR
            else -> BrsContinueKind.WHILE
        }
        return if (context.supportsContinue) {
            BrsContinue(continueKind)
        } else {
            // For older Roku OS, we'd need to restructure the loop
            BrsComment("continue not supported on target Roku OS")
        }
    }

    override fun visitBlockBody(body: IrBlockBody, data: Unit): BrsStatement {
        return parent.transformBody(body)
    }

    override fun visitBlock(expression: IrBlock, data: Unit): BrsStatement {
        // Check if this is an IrReturnableBlock (from inlined functions)
        // After BrsReturnableBlockLowering, the block contains:
        // - Statements that may include "return@block Unit" (which becomes exit while)
        // We wrap this in "while true { ... }" so exit while can break out of it.
        if (expression is IrReturnableBlock) {
            return transformReturnableBlock(expression)
        }

        // Check if this is a FOR loop that was lowered to a while loop
        val isForLoop = expression.origin == IrStatementOrigin.FOR_LOOP
        if (isForLoop) {
            val forLoopResult = tryTransformForLoop(expression)
            if (forLoopResult != null) {
                return forLoopResult
            }
            // If tryTransformForLoop returns null, fall through to general processing.
            // This can happen after inlining changes the block structure.
            // Register any inner while loops so that visitBreak generates "exit while" instead of "exit for".
            // We need to do this BEFORE general processing transforms the body.
            // Recursively find ALL while loops in the block, including those nested inside
            // when expressions, type operators, etc.
            fun registerInnerLoops(element: IrElement) {
                when (element) {
                    is IrWhileLoop -> {
                        // This while loop was originally a for-loop's inner while
                        // Any break statements targeting it should use "exit while"
                        parent.loopsTransformedToWhile.add(element)
                        // Also recurse into the loop body
                        element.body?.let { registerInnerLoops(it) }
                    }
                    is IrBlock -> element.statements.forEach { registerInnerLoops(it) }
                    is IrContainerExpression -> element.statements.forEach { registerInnerLoops(it) }
                    is IrWhen -> element.branches.forEach { branch ->
                        registerInnerLoops(branch.condition)
                        registerInnerLoops(branch.result)
                    }
                    is IrTypeOperatorCall -> registerInnerLoops(element.argument)
                    is IrReturn -> registerInnerLoops(element.value)
                    is IrSetValue -> registerInnerLoops(element.value)
                    is IrSetField -> registerInnerLoops(element.value)
                    is IrVariable -> element.initializer?.let { registerInnerLoops(it) }
                    is IrCall -> {
                        element.dispatchReceiver?.let { registerInnerLoops(it) }
                        element.extensionReceiver?.let { registerInnerLoops(it) }
                        for (i in 0 until element.valueArgumentsCount) {
                            element.getValueArgument(i)?.let { registerInnerLoops(it) }
                        }
                    }
                }
            }
            expression.statements.forEach { registerInnerLoops(it) }
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

        // Check for when-lowered blocks in statement context
        // Structure: { var __when_tmp; if/when { ... -> __when_tmp = ... }; __when_tmp }
        // Or: { var __when_tmp; if/when { ... -> __when_tmp = ... }; var result = __when_tmp }
        // We need to properly flatten these into their component statements
        val blockStatements = expression.statements
        if (blockStatements.size >= 3) {
            val firstStmt = blockStatements.firstOrNull()
            val lastStmt = blockStatements.lastOrNull()

            // Detect when-lowered block by checking for __when_tmp variable
            val firstIsWhenTmp = firstStmt is IrVariable &&
                firstStmt.name.asString().startsWith("__when_tmp")

            // Last can be either:
            // 1. IrGetValue(__when_tmp) - when used as expression
            // 2. IrVariable whose initializer is IrGetValue(__when_tmp) - when result is assigned to a variable
            val lastIsWhenTmpRef = lastStmt is IrGetValue &&
                lastStmt.symbol.owner.name.asString().startsWith("__when_tmp")
            val lastIsVarWithWhenTmpInit = lastStmt is IrVariable &&
                (lastStmt.initializer as? IrGetValue)?.symbol?.owner?.name?.asString()?.startsWith("__when_tmp") == true

            val isWhenLoweredBlock = firstIsWhenTmp && (lastIsWhenTmpRef || lastIsVarWithWhenTmpInit)

            if (isWhenLoweredBlock) {
                // Transform all statements
                // - Skip the last if it's just IrGetValue(__when_tmp) (return value, not needed)
                // - Include the last if it's IrVariable (the result variable assignment)
                //
                // We push a new hoisting scope so nested transformations don't interfere with
                // outer scopes. This block directly returns its statements (not hoisted).
                parent.pushHoistedScope()
                val resultStatements = mutableListOf<BrsStatement>()
                val lastIndex = if (lastIsWhenTmpRef) blockStatements.size - 1 else blockStatements.size

                for (i in 0 until lastIndex) {
                    val stmt = blockStatements[i]
                    val transformed: BrsStatement? = when (stmt) {
                        is IrVariable -> {
                            val init = stmt.initializer?.let { parent.transformExpression(it) }
                            // Consume any hoisted statements from nested when-lowered blocks in the initializer
                            val hoisted = parent.takeHoistedStatements()
                            resultStatements.addAll(hoisted)
                            BrsVariable(stmt.name.asString(), parent.mapTypeToBrs(stmt.type), init)
                        }
                        is IrWhen -> {
                            val whenStmt = visitWhen(stmt, Unit)
                            resultStatements.addAll(parent.takeHoistedStatements())
                            whenStmt
                        }
                        is IrWhileLoop -> {
                            val loopStmt = visitWhileLoop(stmt, Unit)
                            resultStatements.addAll(parent.takeHoistedStatements())
                            loopStmt
                        }
                        is IrDoWhileLoop -> {
                            val loopStmt = visitDoWhileLoop(stmt, Unit)
                            resultStatements.addAll(parent.takeHoistedStatements())
                            loopStmt
                        }
                        is IrBlock -> {
                            val blockStmt = visitBlock(stmt, Unit)
                            resultStatements.addAll(parent.takeHoistedStatements())
                            blockStmt
                        }
                        is IrSetValue -> {
                            val setStmt = visitSetValue(stmt, Unit)
                            resultStatements.addAll(parent.takeHoistedStatements())
                            setStmt
                        }
                        is IrSetField -> {
                            val setStmt = visitSetField(stmt, Unit)
                            resultStatements.addAll(parent.takeHoistedStatements())
                            setStmt
                        }
                        else -> {
                            val transformed = parent.transformStatement(stmt)
                            resultStatements.addAll(parent.takeHoistedStatements())
                            transformed
                        }
                    }
                    if (transformed != null) {
                        resultStatements.add(transformed)
                    }
                }

                // Pop our scope (should be empty now) and discard
                parent.popHoistedScope()

                return BrsBlock(resultStatements.toMutableList())
            }
        }

        val statements = expression.statements.flatMap { stmt ->
            // Helper to consume and prepend hoisted statements
            fun prependHoisted(stmts: List<BrsStatement>): List<BrsStatement> {
                val hoisted = parent.takeHoistedStatements()
                return if (hoisted.isNotEmpty()) hoisted + stmts else stmts
            }

            when (stmt) {
                // Route IrReturn to statement transformer (has visitReturn handler)
                // IrReturn extends IrExpression but needs statement-level handling
                is IrReturn -> {
                    val transformed = parent.transformStatement(stmt)
                    prependHoisted(listOfNotNull(transformed))
                }
                // These are handled during constructor/enum transformation, skip here
                is IrDelegatingConstructorCall -> emptyList()
                is IrInstanceInitializerCall -> emptyList()
                is IrEnumConstructorCall -> emptyList()
                // Skip temp variable REFERENCES (IrGetValue) from increment/decrement blocks
                // and when-lowering blocks. These are return values that shouldn't be statements.
                is IrGetValue -> {
                    val varName = stmt.symbol.owner.name.asString()
                    if (varName.startsWith("<") && varName.endsWith(">")) {
                        emptyList()  // Skip temp variable returns like <unary>
                    } else if (varName.startsWith("__when_tmp")) {
                        emptyList()  // Skip when-lowering temp variable returns
                    } else {
                        val expr = parent.transformExpression(stmt)
                        prependHoisted(listOf(BrsExpressionStatement(expr)))
                    }
                }
                // Keep temp variable DECLARATIONS - they're needed by the setter call
                is IrVariable -> {
                    val varName = stmt.name.asString()
                    // Sanitize the name for BrightScript - handle special names and escape reserved keywords
                    val sanitizedName = sanitizeParameterName(varName)
                    val init = stmt.initializer?.let { parent.transformExpression(it) }
                    // Check for hoisted statements from when-lowered blocks in the initializer
                    val hoisted = parent.takeHoistedStatements()
                    // Always create the variable declaration (use invalid for uninitialized vars)
                    val varDecl = BrsVariable(sanitizedName, parent.mapTypeToBrs(stmt.type), init ?: BrsInvalidLiteral())
                    if (hoisted.isNotEmpty()) {
                        // Prepend hoisted statements before the variable declaration
                        hoisted + varDecl
                    } else {
                        listOf(varDecl)
                    }
                }
                // For nested blocks/composites, flatten them to extract all statements
                // This is important for postfix increment/decrement which use blocks
                is IrBlock -> {
                    val nested = visitBlock(stmt, Unit)
                    val stmts = if (nested is BrsBlock) nested.statements else listOf(nested)
                    prependHoisted(stmts)
                }
                is IrComposite -> {
                    // Process each statement in the composite
                    // In statement context, the last element might be a result value that should be discarded
                    val compositeStmts = stmt.statements.flatMapIndexed { index, nestedStmt ->
                        val isLast = index == stmt.statements.lastIndex
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
                            // Control flow must be handled as statements, not expressions
                            is IrThrow -> listOf(visitThrow(nestedStmt, Unit))
                            is IrBreak -> listOf(visitBreak(nestedStmt, Unit))
                            is IrContinue -> listOf(visitContinue(nestedStmt, Unit))
                            // IrReturn needs special handling - use statement transformer which has visitReturn
                            is IrReturn -> listOfNotNull(parent.transformStatement(nestedStmt))
                            // Skip pure value reads at the end (result values discarded in statement context)
                            is IrGetValue -> if (isLast) emptyList() else listOf(BrsExpressionStatement(parent.transformExpression(nestedStmt)))
                            // IrSetValue needs statement visitor to handle hoisting from nested composites properly
                            is IrSetValue -> {
                                val transformed = visitSetValue(nestedStmt, Unit)
                                if (transformed is BrsBlock) transformed.statements else listOf(transformed)
                            }
                            is IrExpression -> listOf(BrsExpressionStatement(parent.transformExpression(nestedStmt)))
                            else -> listOfNotNull(parent.transformStatement(nestedStmt))
                        }
                    }
                    prependHoisted(compositeStmts)
                }
                // Loops are expressions (extend IrExpression) but should be transformed as statements
                is IrWhileLoop -> {
                    val transformed = visitWhileLoop(stmt, Unit)
                    prependHoisted(listOf(transformed))
                }
                is IrDoWhileLoop -> {
                    val transformed = visitDoWhileLoop(stmt, Unit)
                    prependHoisted(listOf(transformed))
                }
                // IrWhen (if/when) with Unit type should be transformed as statements, not expressions
                is IrWhen -> {
                    val transformed = visitWhen(stmt, Unit)
                    prependHoisted(listOf(transformed))
                }
                // Control flow must be handled as statements, not expressions
                is IrThrow -> {
                    val transformed = visitThrow(stmt, Unit)
                    prependHoisted(listOf(transformed))
                }
                is IrBreak -> {
                    val transformed = visitBreak(stmt, Unit)
                    prependHoisted(listOf(transformed))
                }
                is IrContinue -> {
                    val transformed = visitContinue(stmt, Unit)
                    prependHoisted(listOf(transformed))
                }
                // Handle type operator calls (like IMPLICIT_COERCION_TO_UNIT wrapping increment blocks)
                is IrTypeOperatorCall -> {
                    val transformed = visitTypeOperator(stmt, Unit)
                    if (transformed != null) {
                        prependHoisted(listOf(transformed))
                    } else {
                        // Fallback to expression transformer
                        val expr = parent.transformExpression(stmt)
                        prependHoisted(listOf(BrsExpressionStatement(expr)))
                    }
                }
                is IrExpression -> {
                    val expr = parent.transformExpression(stmt)
                    // Skip BrsInvalidLiteral - these come from empty IrComposite left behind
                    // when LocalDeclarationsLowering hoists local functions
                    if (expr is BrsInvalidLiteral) {
                        emptyList()
                    } else {
                        prependHoisted(listOf(BrsExpressionStatement(expr)))
                    }
                }
                else -> {
                    val transformed = parent.transformStatement(stmt)
                    prependHoisted(listOfNotNull(transformed))
                }
            }
        }
        // Consume any remaining hoisted statements and prepend them to the block
        val remainingHoisted = parent.takeHoistedStatements()
        val finalStatements = if (remainingHoisted.isNotEmpty()) {
            remainingHoisted + statements
        } else {
            statements
        }

        // If this was a FOR_LOOP that fell through to general processing,
        // convert any "exit for" to "exit while" since the loop may have been
        // transformed to a while loop during inlining
        val convertedStatements = if (isForLoop) {
            convertForControlToWhile(finalStatements)
        } else {
            finalStatements
        }

        return BrsBlock(convertedStatements.toMutableList())
    }

    /**
     * Check if any returns within the block target this returnable block.
     * After BrsReturnableBlockLowering, these will be "return@block Unit" statements.
     */
    private fun hasReturnsTargetingBlock(block: IrReturnableBlock): Boolean {
        var hasReturns = false
        block.acceptVoid(object : IrVisitorVoid() {
            override fun visitElement(element: IrElement) {
                if (!hasReturns) {
                    element.acceptChildrenVoid(this)
                }
            }

            override fun visitReturn(expression: IrReturn) {
                if (expression.returnTargetSymbol == block.symbol) {
                    hasReturns = true
                }
                // Don't recurse into nested returns
            }
        })
        return hasReturns
    }

    /**
     * Check if any returns targeting this block are inside a while loop.
     * This is critical because BrightScript's "exit while" only exits the innermost
     * while loop, so if a return@block is inside a while loop, we need to use a
     * flag-based approach instead of just "exit while".
     */
    private fun hasReturnsInsideWhileLoop(block: IrReturnableBlock): Boolean {
        var hasReturnsInWhile = false
        var insideWhileLoop = 0

        block.acceptVoid(object : IrVisitorVoid() {
            override fun visitElement(element: IrElement) {
                if (!hasReturnsInWhile) {
                    element.acceptChildrenVoid(this)
                }
            }

            override fun visitWhileLoop(loop: IrWhileLoop) {
                insideWhileLoop++
                loop.acceptChildrenVoid(this)
                insideWhileLoop--
            }

            override fun visitDoWhileLoop(loop: IrDoWhileLoop) {
                insideWhileLoop++
                loop.acceptChildrenVoid(this)
                insideWhileLoop--
            }

            override fun visitReturn(expression: IrReturn) {
                if (expression.returnTargetSymbol == block.symbol && insideWhileLoop > 0) {
                    hasReturnsInWhile = true
                }
            }
        })
        return hasReturnsInWhile
    }

    /**
     * Check if a BrsStatement contains a BrsWhile loop (at any nesting level).
     * This is used to determine if we need to insert flag checks after a statement
     * in the flag-based returnable block approach.
     */
    private fun containsBrsWhileLoop(stmt: BrsStatement): Boolean {
        return when (stmt) {
            is BrsWhile -> true
            is BrsBlock -> stmt.statements.any { containsBrsWhileLoop(it) }
            is BrsIf -> {
                containsBrsWhileLoop(stmt.thenBranch) ||
                (stmt.elseBranch?.let { containsBrsWhileLoop(it) } ?: false)
            }
            is BrsFor -> containsBrsWhileLoop(stmt.body)
            is BrsForEach -> containsBrsWhileLoop(stmt.body)
            is BrsTry -> {
                containsBrsWhileLoop(stmt.tryBlock) ||
                (stmt.catchBlock?.let { containsBrsWhileLoop(it) } ?: false)
            }
            else -> false
        }
    }

    /**
     * Insert flag checks after any BrsWhile statements found at any nesting level.
     * This post-processes a BrsStatement to add "if flagName then exit while" after
     * each while loop, ensuring early exit propagates correctly through nested loops.
     *
     * @param stmt The statement to process
     * @param flagName The flag variable name to check
     * @return A new statement with flag checks inserted
     */
    private fun insertFlagChecksAfterWhileLoops(stmt: BrsStatement, flagName: String): BrsStatement {
        return when (stmt) {
            is BrsBlock -> {
                val newStatements = mutableListOf<BrsStatement>()
                for (s in stmt.statements) {
                    val processed = insertFlagChecksAfterWhileLoops(s, flagName)
                    newStatements.add(processed)
                    // Add flag check after while loops
                    if (processed is BrsWhile) {
                        newStatements.add(
                            BrsIf(
                                condition = BrsIdentifier(flagName),
                                thenBranch = BrsExit(BrsExitKind.WHILE),
                                elseBranch = null
                            )
                        )
                    }
                }
                BrsBlock(newStatements)
            }
            is BrsIf -> BrsIf(
                condition = stmt.condition,
                thenBranch = insertFlagChecksAfterWhileLoops(stmt.thenBranch, flagName),
                elseBranch = stmt.elseBranch?.let { insertFlagChecksAfterWhileLoops(it, flagName) }
            )
            is BrsFor -> BrsFor(
                variable = stmt.variable,
                start = stmt.start,
                end = stmt.end,
                step = stmt.step,
                body = insertFlagChecksAfterWhileLoops(stmt.body, flagName)
            )
            is BrsForEach -> BrsForEach(
                variable = stmt.variable,
                iterable = stmt.iterable,
                body = insertFlagChecksAfterWhileLoops(stmt.body, flagName)
            )
            is BrsTry -> BrsTry(
                tryBlock = insertFlagChecksAfterWhileLoops(stmt.tryBlock, flagName),
                catchVariable = stmt.catchVariable,
                catchBlock = stmt.catchBlock?.let { insertFlagChecksAfterWhileLoops(it, flagName) }
            )
            is BrsWhile -> {
                // Don't recurse into while loop body - we only care about while loops
                // at the returnable block level, not nested while loops inside those
                stmt
            }
            else -> stmt
        }
    }

    /**
     * Transform a returnable block (from inlined functions).
     *
     * After BrsReturnableBlockLowering, the returnable block contains:
     * - Statements that may include "return@block Unit" (converted to exit while)
     * - The actual return value has been hoisted to a result variable by the lowering
     *
     * We wrap this in "while true { ... exit while }" so that return@block Unit
     * (which becomes "exit while") can break out of the block.
     *
     * IMPORTANT: If there are NO returns targeting this block, we don't need the
     * while wrapper at all - just emit the statements directly. This avoids
     * generating nested while loops for cases like `let { return it }` where
     * the return is a function return, not a block return.
     *
     * CRITICAL: When returns are inside while loops, we use a flag-based approach
     * because BrightScript's "exit while" only exits the innermost loop.
     * The flag approach: set __done = true; exit while, then after each inner
     * while loop check: if __done then exit while.
     */
    private fun transformReturnableBlock(block: IrReturnableBlock): BrsStatement {
        // Check if any returns actually target this block
        // If not, we don't need the while wrapper - just emit statements directly
        val needsWhileWrapper = hasReturnsTargetingBlock(block)
        val needsFlagApproachCheck = hasReturnsInsideWhileLoop(block)

        // DEBUG: Log returnable block transformation
        java.io.File("/tmp/returnable-block-debug.log").appendText("[DEBUG-RB] transformReturnableBlock: needsWhileWrapper=$needsWhileWrapper, needsFlagApproach=$needsFlagApproachCheck\n")
        java.io.File("/tmp/returnable-block-debug.log").appendText("[DEBUG-RB]   statements count: ${block.statements.size}\n")
        block.statements.forEachIndexed { idx, stmt ->
            java.io.File("/tmp/returnable-block-debug.log").appendText("[DEBUG-RB]   [$idx] ${stmt::class.simpleName}: ${stmt.toString().take(100)}\n")
        }

        if (!needsWhileWrapper) {
            // No returns target this block - just transform statements directly
            val bodyStatements = mutableListOf<BrsStatement>()
            for (stmt in block.statements) {
                val transformed = when (stmt) {
                    is IrExpression -> {
                        parent.pushHoistedScope()
                        val result = when (stmt) {
                            is IrWhen -> visitWhen(stmt, Unit)
                            is IrWhileLoop -> visitWhileLoop(stmt, Unit)
                            is IrDoWhileLoop -> visitDoWhileLoop(stmt, Unit)
                            is IrBlock -> visitBlock(stmt, Unit)
                            is IrReturn -> visitReturn(stmt, Unit)
                            else -> BrsExpressionStatement(parent.transformExpression(stmt))
                        }
                        val hoisted = parent.popHoistedScope()
                        if (hoisted.isNotEmpty()) {
                            bodyStatements.addAll(hoisted)
                        }
                        result
                    }
                    else -> parent.transformStatement(stmt)
                }
                if (transformed != null) {
                    bodyStatements.add(transformed)
                }
            }
            return if (bodyStatements.size == 1) bodyStatements[0] else BrsBlock(bodyStatements)
        }

        // Check if any returns targeting this block are inside while loops
        // If so, we need to use a flag-based approach
        val needsFlagApproach = hasReturnsInsideWhileLoop(block)

        // Generate flag name if needed and push onto stack
        val flagName = if (needsFlagApproach) {
            val name = "__ret_done_${parent.returnableBlockFlagCounter++}"
            parent.returnableBlockFlagStack.add(name)
            name
        } else null

        try {
            // Transform the block body
            val bodyStatements = mutableListOf<BrsStatement>()

            for (stmt in block.statements) {
                java.io.File("/tmp/returnable-block-debug.log").appendText("[DEBUG-RB]   Processing stmt: ${stmt::class.simpleName}\n")
                val transformed = when (stmt) {
                    is IrExpression -> {
                        parent.pushHoistedScope()
                        java.io.File("/tmp/returnable-block-debug.log").appendText("[DEBUG-RB]     Is IrExpression, pushed scope\n")
                        val result = when (stmt) {
                            is IrWhen -> visitWhen(stmt, Unit)
                            is IrWhileLoop -> visitWhileLoop(stmt, Unit)
                            is IrDoWhileLoop -> visitDoWhileLoop(stmt, Unit)
                            is IrBlock -> visitBlock(stmt, Unit)
                            is IrReturn -> visitReturn(stmt, Unit)
                            else -> {
                                java.io.File("/tmp/returnable-block-debug.log").appendText("[DEBUG-RB]     else branch: calling transformExpression on ${stmt::class.simpleName}\n")
                                BrsExpressionStatement(parent.transformExpression(stmt))
                            }
                        }
                        val hoisted = parent.popHoistedScope()
                        java.io.File("/tmp/returnable-block-debug.log").appendText("[DEBUG-RB]     popped scope, hoisted count: ${hoisted.size}\n")
                        hoisted.forEachIndexed { idx, h ->
                            java.io.File("/tmp/returnable-block-debug.log").appendText("[DEBUG-RB]       hoisted[$idx]: ${h::class.simpleName}\n")
                        }
                        if (hoisted.isNotEmpty()) {
                            java.io.File("/tmp/returnable-block-debug.log").appendText("[DEBUG-RB]     adding hoisted to bodyStatements BEFORE result\n")
                            bodyStatements.addAll(hoisted)
                            java.io.File("/tmp/returnable-block-debug.log").appendText("[DEBUG-RB]       bodyStatements now has ${bodyStatements.size} items\n")
                        }
                        java.io.File("/tmp/returnable-block-debug.log").appendText("[DEBUG-RB]     result: ${result::class.simpleName}\n")
                        java.io.File("/tmp/returnable-block-debug.log").appendText("[DEBUG-RB]       result content: ${if (result is BrsBlock) "BrsBlock with ${(result as BrsBlock).statements.size} items" else result.toString().take(100)}\n")
                        result
                    }
                    else -> parent.transformStatement(stmt)
                }
                if (transformed != null) {
                    // If the result is a BrsBlock, flatten its statements into bodyStatements
                    // This ensures proper ordering when nested blocks contain variable declarations
                    // and assignments that depend on each other
                    if (transformed is BrsBlock) {
                        java.io.File("/tmp/returnable-block-debug.log").appendText("[DEBUG-RB]   flattening BrsBlock with ${transformed.statements.size} items into bodyStatements\n")
                        bodyStatements.addAll(transformed.statements)
                    } else {
                        java.io.File("/tmp/returnable-block-debug.log").appendText("[DEBUG-RB]   adding ${transformed::class.simpleName} to bodyStatements\n")
                        bodyStatements.add(transformed)
                    }

                    // DEBUG: Log transformed type
                    java.io.File("/tmp/returnable-block-debug.log").appendText("[DEBUG-RB]   bodyStatements.size=${bodyStatements.size}\n")
                }
            }

            // Add "exit while" at the end to ensure we exit after all statements complete
            // (in case there's no early return), but only if the last statement isn't already terminating
            if (bodyStatements.isEmpty() || !isTerminating(bodyStatements.last())) {
                if (flagName != null) {
                    // Set flag before exiting
                    bodyStatements.add(BrsExpressionStatement(
                        BrsBinaryOp(BrsIdentifier(flagName), BrsBinaryOperator.EQ, BrsBooleanLiteral(true))
                    ))
                }
                bodyStatements.add(BrsExit(BrsExitKind.WHILE))
            }

            // Build the while wrapper body
            var whileBody: BrsStatement = BrsBlock(bodyStatements)

            // If using flag approach, post-process the body to insert flag checks after each while loop
            // at any nesting level. This ensures early exit propagates correctly.
            if (flagName != null) {
                whileBody = insertFlagChecksAfterWhileLoops(whileBody, flagName)
                java.io.File("/tmp/returnable-block-debug.log").appendText("[DEBUG-RB]   Applied insertFlagChecksAfterWhileLoops for flag=$flagName\n")
            }

            val whileLoop = BrsWhile(
                condition = BrsBooleanLiteral(true),
                body = whileBody
            )

            // If using flag approach, prepend flag declaration
            return if (flagName != null) {
                BrsBlock(mutableListOf<BrsStatement>(
                    BrsVariable(flagName, BrsType.BOOLEAN, BrsBooleanLiteral(false)),
                    whileLoop
                ))
            } else {
                whileLoop
            }
        } finally {
            if (flagName != null) {
                parent.returnableBlockFlagStack.removeLast()
            }
        }
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

        // Find the temp variable and its initializer for substitution
        // This allows us to inline the temp var references in the assignment
        val tempVar = statements.filterIsInstance<IrVariable>().firstOrNull {
            it.name.asString().startsWith("<") && it.name.asString().endsWith(">")
        }
        val tempVarInitializer = tempVar?.initializer

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

        // Set up temp var substitution if we found one
        if (tempVar != null && tempVarInitializer != null) {
            parent.pushTempVarSubstitution(tempVar.symbol, tempVarInitializer)
        }

        try {
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
        } finally {
            // Clean up temp var substitution
            if (tempVar != null) {
                parent.popTempVarSubstitution(tempVar.symbol)
            }
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
        if (statements.size < 2) {
            return null
        }

        // Find the iterator variable - may be at top level or inside an IrComposite
        var iteratorVar = statements.firstOrNull {
            it is IrVariable && it.origin == IrDeclarationOrigin.FOR_LOOP_ITERATOR
        } as? IrVariable

        // If not found at top level, check inside IrComposite (happens after inlining)
        if (iteratorVar == null) {
            for (stmt in statements) {
                if (stmt is IrComposite) {
                    iteratorVar = stmt.statements.firstOrNull {
                        it is IrVariable && (it as? IrVariable)?.origin == IrDeclarationOrigin.FOR_LOOP_ITERATOR
                    } as? IrVariable
                    if (iteratorVar != null) break
                }
            }
        }

        if (iteratorVar == null) {
            // Still not found - this FOR_LOOP block has an unexpected structure
            return null
        }

        // Find the while loop
        val whileLoop = statements.lastOrNull {
            it is IrWhileLoop && it.origin == IrStatementOrigin.FOR_LOOP_INNER_WHILE
        } as? IrWhileLoop
        if (whileLoop == null) {
            return null
        }

        // Try to extract the iterable from the iterator initialization
        val iteratorInit = iteratorVar.initializer as? IrCall
        if (iteratorInit == null) {
            return null
        }
        val iterableExpr = iteratorInit.dispatchReceiver ?: iteratorInit.extensionReceiver
        if (iterableExpr == null) {
            return null
        }

        // Get the loop body
        val loopBody = whileLoop.body as? IrContainerExpression
        if (loopBody == null) {
            return null
        }
        val bodyStatements = loopBody.statements

        if (bodyStatements.isEmpty()) {
            return null
        }

        // First statement should be the loop variable assignment from iterator.next()
        val loopVarDecl = bodyStatements.firstOrNull() as? IrVariable
        if (loopVarDecl == null) {
            return null
        }
        val loopVarName = loopVarDecl.name.asString()

        // Register the while loop as "transformed to while" BEFORE transforming the body.
        // This ensures that any IrBreak statements inside will generate "exit while" instead of "exit for".
        // We don't know yet which strategy we'll use (it depends on iterable type analysis below),
        // but it's safe to register - if we use native for-each (Strategy 1-3, 5), there won't be
        // any break statements targeting this while loop in the output anyway.
        parent.loopsTransformedToWhile.add(whileLoop)

        // Increment nesting counter so that any nested for-loops (from inlining) also get "exit while"
        parent.forLoopToWhileNestingDepth++
        val actualBody: List<BrsStatement>
        try {
            // Transform the remaining body statements (skip the loop variable declaration)
            actualBody = bodyStatements.drop(1).mapNotNull { stmt ->
            when (stmt) {
                // IrWhen (if statements) should use visitWhen directly to handle returns properly
                is IrWhen -> visitWhen(stmt, Unit)
                // IrWhileLoop and IrDoWhileLoop need explicit handling
                is IrWhileLoop -> visitWhileLoop(stmt, Unit)
                is IrDoWhileLoop -> visitDoWhileLoop(stmt, Unit)
                // IrBlock handling - check if it's a wrapper around a single IrWhen
                is IrBlock -> {
                    // Unwrap single-statement blocks that contain IrWhen
                    // These wrappers are created by various lowering passes
                    val innerStatements = stmt.statements
                    if (innerStatements.size == 1 && innerStatements[0] is IrWhen) {
                        visitWhen(innerStatements[0] as IrWhen, Unit)
                    } else {
                        transformBlockOrStatement(stmt)
                    }
                }
                // IrTypeOperatorCall with IMPLICIT_COERCION_TO_UNIT wraps expressions used as statements
                // Unwrap to get to the inner when/block
                is IrTypeOperatorCall -> {
                    if (stmt.operator == IrTypeOperator.IMPLICIT_COERCION_TO_UNIT) {
                        val innerArg = stmt.argument
                        when (innerArg) {
                            is IrWhen -> visitWhen(innerArg, Unit)
                            is IrBlock -> {
                                // Check if the block contains a single IrWhen
                                if (innerArg.statements.size == 1 && innerArg.statements[0] is IrWhen) {
                                    visitWhen(innerArg.statements[0] as IrWhen, Unit)
                                } else {
                                    transformBlockOrStatement(innerArg)
                                }
                            }
                            else -> transformBlockOrStatement(stmt)
                        }
                    } else {
                        transformBlockOrStatement(stmt)
                    }
                }
                // Expressions - transform and check for hoisted when-lowered blocks
                is IrExpression -> {
                    if (stmt.type.isUnit()) {
                        parent.transformStatement(stmt)
                    } else if (stmt is IrWhen) {
                        // When expressions should always be statements in for loop body,
                        // even if they have a non-Unit type (e.g., StringBuilder.append())
                        visitWhen(stmt, Unit)
                    } else if (stmt is IrTypeOperatorCall && stmt.argument is IrWhen) {
                        // Unwrap type operators around when expressions
                        visitWhen(stmt.argument as IrWhen, Unit)
                    } else {
                        // Transform expression - when-lowered blocks add to hoisted queue
                        val expr = parent.transformExpression(stmt)
                        val hoisted = parent.takeHoistedStatements()
                        // In statement context, skip __when_tmp references (just temp var values)
                        val skipFinalExpr = expr is BrsIdentifier && expr.name.startsWith("__when_tmp")
                        if (hoisted.isNotEmpty()) {
                            val stmts = hoisted.toMutableList()
                            if (!skipFinalExpr && expr !is BrsInvalidLiteral) {
                                stmts.add(BrsExpressionStatement(expr))
                            }
                            if (stmts.isEmpty()) null
                            else if (stmts.size == 1) stmts.first()
                            else BrsBlock(stmts)
                        } else if (!skipFinalExpr) {
                            BrsExpressionStatement(expr)
                        } else {
                            null
                        }
                    }
                }
                else -> parent.transformStatement(stmt)
            }
        }
        } finally {
            parent.forLoopToWhileNestingDepth--
        }

        // Transform the iterable expression
        val iterableBrs = parent.transformExpression(iterableExpr)

        // For Kotlin collection types (ArrayList, MutableList, etc.), we need to iterate
        // over the underlying array, not the wrapper object. In BrightScript, 'for each'
        // on an AA iterates over keys, not values. Collections store items in .array property.
        val iterableType = iterableExpr.type
        val iterableClassName = iterableType.classOrNull?.owner?.name?.asString() ?: ""

        // =============================================================================
        // Strategy 1: Check if type implements NativeIterable (e.g., RoArray, RoAssociativeArray)
        // These types support native BrightScript for-each iteration directly.
        // =============================================================================
        if (context.intrinsics.isNativeIterable(iterableType)) {
            return BrsForEach(
                variable = loopVarName,
                iterable = iterableBrs,
                body = BrsBlock(actualBody.toMutableList())
            )
        }

        // =============================================================================
        // Strategy 2: Check if iterator() returns NativeArrayIterator
        // This signals that the type supports native BrightScript for-each.
        // =============================================================================
        val iteratorMethod = findIteratorMethod(iterableType)
        if (iteratorMethod != null && context.intrinsics.returnsNativeArrayIterator(iteratorMethod)) {
            return BrsForEach(
                variable = loopVarName,
                iterable = iterableBrs,
                body = BrsBlock(actualBody.toMutableList())
            )
        }

        // =============================================================================
        // Strategy 3: Stdlib collections with __get_array() property
        // These concrete stdlib collection types wrap roArray and expose it via __get_array().
        // =============================================================================
        val hasGetArray = iterableClassName in listOf(
            // Array-backed collections
            "ArrayList", "ArrayDeque", "CharArray", "IntArray", "LongArray",
            "FloatArray", "DoubleArray", "BooleanArray", "ByteArray", "ShortArray",
            // HashMap view collections
            "KeySet", "ValueCollection", "EntrySet",
            // LinkedHashMap view collections
            "LinkedKeySet", "LinkedValueCollection", "LinkedEntrySet"
        )

        if (hasGetArray) {
            // Call __get_array() method for iteration - this works for concrete stdlib collections
            val arrayIterable = BrsFunctionCall(BrsDotAccess(iterableBrs, "__get_array"), mutableListOf())
            return BrsForEach(
                variable = loopVarName,
                iterable = arrayIterable,
                body = BrsBlock(actualBody.toMutableList())
            )
        }

        // =============================================================================
        // Strategy 4: Kotlin Iterable interface types → iterator protocol
        // Generate a while loop with iterator_k_(), hasNext_k_(), next_k_() calls.
        // =============================================================================
        val isCollectionInterface = iterableClassName in listOf(
            // Interface types
            "Iterable", "MutableIterable", "Collection", "MutableCollection",
            "List", "MutableList", "Set", "MutableSet",
            // Sequence types - need iterator protocol, not native for-each (which iterates AA keys)
            "Sequence",
            // Concrete collection classes that need iterator-based iteration
            // (these don't have get_array() - they use hash-based storage)
            "HashSet", "LinkedHashSet", "HashMap", "LinkedHashMap"
        )

        if (isCollectionInterface) {
            // Generate: __iter = iterable.iterator_k_(); while __iter.hasNext_k_() { loopVar = __iter.next_k_(); body }
            // Note: Method names do NOT include return types (like Java) to support polymorphism
            val iterVarName = "__iter_${parent.nextTempId()}"
            val iterVar = BrsIdentifier(iterVarName)

            // All iterator methods use the same name regardless of mutable/non-mutable
            // (return types are not part of method name mangling)
            val iteratorMethodName = "iterator_k_"

            // __iter = iterable.iterator_k_()
            val iteratorCall = BrsFunctionCall(BrsDotAccess(iterableBrs, iteratorMethodName), mutableListOf())
            val iterAssign = BrsVariable(iterVarName, null, iteratorCall)

            // __iter.hasNext_k_()  (returns Boolean, but return type not in name)
            val hasNextCall = BrsFunctionCall(BrsDotAccess(iterVar, "hasNext_k_"), mutableListOf())

            // loopVar = __iter.next_k_()  (returns T which erases to Any?, but return type not in name)
            val nextCall = BrsFunctionCall(BrsDotAccess(iterVar, "next_k_"), mutableListOf())
            val loopVarAssign = BrsVariable(loopVarName, null, nextCall)

            // Build while body: assignment + original body
            val whileBody = mutableListOf<BrsStatement>()
            whileBody.add(loopVarAssign)
            whileBody.addAll(actualBody)

            // Convert any "exit for" to "exit while" and "continue for" to continue pattern
            val convertedBody = convertForControlToWhile(whileBody)

            return BrsBlock(mutableListOf(
                iterAssign,
                BrsWhile(hasNextCall, BrsBlock(convertedBody.toMutableList()))
            ))
        }

        // =============================================================================
        // Strategy 5: Default - native for-each
        // For native arrays or other types, use for-each directly.
        // =============================================================================
        return BrsForEach(
            variable = loopVarName,
            iterable = iterableBrs,
            body = BrsBlock(actualBody.toMutableList())
        )
    }

    /**
     * Find the iterator() method on a type.
     * Returns the IrSimpleFunction if found, null otherwise.
     */
    private fun findIteratorMethod(type: IrType): IrSimpleFunction? {
        val classSymbol = type.classOrNull ?: return null
        return classSymbol.owner.declarations
            .filterIsInstance<IrSimpleFunction>()
            .find { it.name.asString() == "iterator" && it.valueParameters.isEmpty() }
    }

    /**
     * Convert "exit for" to "exit while" in statements that were originally in a for-each
     * but are now in a while loop due to iterator-based conversion.
     */
    private fun convertForControlToWhile(statements: List<BrsStatement>): List<BrsStatement> {
        return statements.map { stmt -> convertForControlToWhileStmt(stmt) }
    }

    private fun convertForControlToWhileStmt(stmt: BrsStatement): BrsStatement {
        return when (stmt) {
            is BrsExit -> if (stmt.kind == BrsExitKind.FOR) BrsExit(BrsExitKind.WHILE) else stmt
            is BrsContinue -> if (stmt.kind == BrsContinueKind.FOR) BrsContinue(BrsContinueKind.WHILE) else stmt
            is BrsBlock -> BrsBlock(convertForControlToWhile(stmt.statements).toMutableList())
            is BrsIf -> BrsIf(
                condition = stmt.condition,
                thenBranch = convertForControlToWhileStmt(stmt.thenBranch),
                elseBranch = stmt.elseBranch?.let { convertForControlToWhileStmt(it) }
            )
            is BrsTry -> BrsTry(
                tryBlock = convertForControlToWhileStmt(stmt.tryBlock),
                catchVariable = stmt.catchVariable,
                catchBlock = stmt.catchBlock?.let { convertForControlToWhileStmt(it) }
            )
            is BrsWhile -> BrsWhile(
                condition = stmt.condition,
                body = stmt.body // Don't recurse into nested while - it has its own scope
            )
            is BrsForEach -> stmt // Don't recurse into nested for - it has its own scope
            is BrsFor -> stmt // Don't recurse into nested for - it has its own scope
            // Handle expression statements that might contain nested structures
            is BrsExpressionStatement -> stmt
            is BrsVariable -> stmt
            is BrsReturn -> stmt
            is BrsThrow -> stmt
            is BrsComment -> stmt
            is BrsEmpty -> stmt
            else -> stmt
        }
    }

    /**
     * Recursively scan an expression tree for IrBlocks that need hoisting.
     * Collects the blocks and returns them so their statements can be emitted before the expression.
     */
    private fun collectBlocksFromExpression(expr: IrExpression): List<IrBlock> {
        val blocks = mutableListOf<IrBlock>()
        collectBlocksRecursive(expr, blocks)
        return blocks
    }

    private fun collectBlocksRecursive(expr: IrExpression?, blocks: MutableList<IrBlock>) {
        when (expr) {
            null -> {}
            is IrBlock -> {
                if (expr.statements.size > 1 &&
                    expr.origin != IrStatementOrigin.POSTFIX_INCR &&
                    expr.origin != IrStatementOrigin.POSTFIX_DECR &&
                    expr.origin != IrStatementOrigin.PREFIX_INCR &&
                    expr.origin != IrStatementOrigin.PREFIX_DECR) {
                    blocks.add(expr)
                }
                // Also check statements within the block
                for (stmt in expr.statements) {
                    if (stmt is IrExpression) {
                        collectBlocksRecursive(stmt, blocks)
                    }
                    // Note: IrVariable initializers are handled by hoistBlockStatements which
                    // consumes hoisted statements from transformExpression calls
                }
            }
            is IrCall -> {
                collectBlocksRecursive(expr.dispatchReceiver, blocks)
                collectBlocksRecursive(expr.extensionReceiver, blocks)
                for (i in 0 until expr.valueArgumentsCount) {
                    collectBlocksRecursive(expr.getValueArgument(i), blocks)
                }
            }
            is IrConstructorCall -> {
                for (i in 0 until expr.valueArgumentsCount) {
                    collectBlocksRecursive(expr.getValueArgument(i), blocks)
                }
            }
            is IrWhen -> {
                // Only recurse into conditions, NOT branch results
                // Branch result blocks should stay as branch bodies and be handled by visitWhen
                // They don't need hoisting - only blocks in true expression context do
                for (branch in expr.branches) {
                    collectBlocksRecursive(branch.condition, blocks)
                    // Don't recurse into branch.result - it's a branch body, not a hoistable block
                }
            }
            is IrTypeOperatorCall -> {
                collectBlocksRecursive(expr.argument, blocks)
            }
            is IrStringConcatenation -> {
                for (arg in expr.arguments) {
                    collectBlocksRecursive(arg, blocks)
                }
            }
            is IrGetValue, is IrConst, is IrGetField -> {
                // Leaf nodes - no children to process
            }
            else -> {
                // For other expression types, try to get children via reflection or skip
            }
        }
    }

    /**
     * Hoist the statements from a block (all except the last, which is the value).
     * The last statement will be returned by the expression transformer when the block is visited.
     */
    private fun hoistBlockStatements(block: IrBlock, precedingStatements: MutableList<BrsStatement>) {
        val blockStatements = block.statements
        if (blockStatements.size <= 1) return

        // Transform all statements except the last one as statements
        for (i in 0 until blockStatements.size - 1) {
            val stmt = blockStatements[i]
            val transformed = when (stmt) {
                is IrVariable -> {
                    val varName = stmt.name.asString()
                    // Sanitize the name for BrightScript - handle special names and escape reserved keywords
                    val sanitizedVarName = sanitizeParameterName(varName)
                    val init = stmt.initializer?.let { parent.transformExpression(it) }
                    // Consume any hoisted statements from nested when-lowered blocks in the initializer
                    val hoisted = parent.takeHoistedStatements()
                    precedingStatements.addAll(hoisted)
                    if (init != null) BrsVariable(sanitizedVarName, parent.mapTypeToBrs(stmt.type), init) else null
                }
                is IrWhen -> visitWhen(stmt, Unit)
                is IrSetValue -> visitSetValue(stmt, Unit)
                is IrSetField -> visitSetField(stmt, Unit)
                is IrExpression -> BrsExpressionStatement(parent.transformExpression(stmt))
                else -> parent.transformStatement(stmt)
            }
            if (transformed != null) {
                precedingStatements.add(transformed)
            }
        }
    }

    override fun visitSetValue(expression: IrSetValue, data: Unit): BrsStatement {
        val rawName = expression.symbol.owner.name.asString()

        // Check if this variable is captured in a closure
        val capturedVar = parent.getCapturedVariable(expression.symbol)

        val sanitizedName = when {
            rawName.startsWith("<set-") && rawName.endsWith(">") -> "value"
            rawName.startsWith("<") && rawName.endsWith(">") ->
                rawName.removePrefix("<").removeSuffix(">").replace("-", "_")
            else -> sanitizeParameterName(rawName)
        }

        // Check if this is a shared variable (boxed for closure capture)
        // Two detection mechanisms:
        // 1. Via SharedVariablesLowering which sets SHARED_VARIABLE_WRAPPER origin
        // 2. Via BrsSharedVariableDetectionLowering which populates sharedVariables set
        val owner = expression.symbol.owner
        val isSharedVariable = (owner is IrVariable && owner.origin == BrsDeclarationOrigin.SHARED_VARIABLE_WRAPPER) ||
                               expression.symbol in parent.sharedVariables

        // Build the target expression (LHS of assignment)
        val target = if (capturedVar != null && capturedVar.isMutable) {
            // Rewrite to assign via m (the closure object): m.varName.value = newValue
            BrsDotAccess(BrsDotAccess(BrsMRef(), capturedVar.name), "value")
        } else if (isSharedVariable) {
            // Shared variable accessed outside closure: varName.value = newValue
            // Note: capturedVar is null here due to the first condition being false
            BrsDotAccess(BrsIdentifier(sanitizeParameterName(sanitizedName)), "value")
        } else if (owner is IrVariable) {
            // For local variables, use the unique name to avoid collisions from inline expansion
            BrsIdentifier(parent.getVariableName(expression.symbol, sanitizedName))
        } else {
            BrsIdentifier(sanitizedName)
        }

        // Transform the expression - when-lowered blocks will add to hoisted queue
        java.io.File("/tmp/returnable-block-debug.log").appendText("[DEBUG-SV] StatementVisitor.visitSetValue for '${rawName}'\n")
        val transformedValue = parent.transformExpression(expression.value)
        java.io.File("/tmp/returnable-block-debug.log").appendText("[DEBUG-SV]   transformedValue: ${transformedValue::class.simpleName}\n")
        // Take any hoisted statements from nested when-lowered blocks
        val hoisted = parent.takeHoistedStatements()
        java.io.File("/tmp/returnable-block-debug.log").appendText("[DEBUG-SV]   hoisted count: ${hoisted.size}\n")
        hoisted.forEachIndexed { idx, h ->
            java.io.File("/tmp/returnable-block-debug.log").appendText("[DEBUG-SV]     hoisted[$idx]: ${h::class.simpleName}\n")
        }

        val assignment = BrsExpressionStatement(
            BrsBinaryOp(target, BrsBinaryOperator.EQ, transformedValue)
        )

        val result = if (hoisted.isNotEmpty()) {
            java.io.File("/tmp/returnable-block-debug.log").appendText("[DEBUG-SV]   returning BrsBlock with hoisted + assignment\n")
            BrsBlock((hoisted + assignment).toMutableList())
        } else {
            java.io.File("/tmp/returnable-block-debug.log").appendText("[DEBUG-SV]   returning assignment only\n")
            assignment
        }
        return result
    }

    override fun visitSetField(expression: IrSetField, data: Unit): BrsStatement {
        val receiver = expression.receiver?.let { parent.transformExpression(it) }
            ?: BrsMRef()

        val field = expression.symbol.owner
        // Sanitize field name - LocalDeclarationsLowering uses $ prefix for captured vars.
        // Also handle special names like <this> which occur for extension receiver parameters.
        val rawFieldName = field.name.asString()
        val fieldName = when {
            rawFieldName == "<this>" -> "__this"
            rawFieldName.startsWith("<") && rawFieldName.endsWith(">") ->
                rawFieldName.removePrefix("<").removeSuffix(">").replace("-", "_").replace(" ", "_")
            else -> rawFieldName.replace("$", "_")
        }

        // Check if this field holds a shared variable box (mutable captured variable)
        // If so, we need to write to field.value instead of field
        // EXCEPTION: In constructor body, we're initializing the field with the box itself,
        // so we write directly to the field, not field.value
        val parentClass = field.parent as? IrClass
        val className = parentClass?.name?.asString() ?: ""
        val fieldKey = "$className.$fieldName"
        val isSharedVariableField = fieldKey in context.sharedVariableFields
        val isInConstructor = parent.isInConstructorBody

        val target = if (isSharedVariableField && !isInConstructor) {
            // Write to the box's value: m.fieldName.value = newValue
            BrsDotAccess(BrsDotAccess(receiver, fieldName), "value")
        } else {
            BrsDotAccess(receiver, fieldName)
        }

        // Transform the expression - when-lowered blocks will add to hoisted queue
        val transformedValue = parent.transformExpression(expression.value)
        // Take any hoisted statements from nested when-lowered blocks
        val hoisted = parent.takeHoistedStatements()

        val assignment = BrsExpressionStatement(
            BrsBinaryOp(target, BrsBinaryOperator.EQ, transformedValue)
        )

        return if (hoisted.isNotEmpty()) {
            BrsBlock((hoisted + assignment).toMutableList())
        } else {
            assignment
        }
    }

    /**
     * Handle function calls as statements, extracting block arguments from when expression lowering.
     *
     * When a function call has a block argument (from when expression lowering), we need to:
     * 1. Emit the block's statements (variable declaration, if/else) before the call
     * 2. Replace the block argument with just the final value (the temp variable)
     */
    fun visitCallAsStatement(expression: IrCall): BrsStatement {
        // Transform the expression - when-lowered blocks will add to hoisted queue
        val transformedCall = parent.transformExpression(expression)
        // Take any hoisted statements from nested when-lowered blocks
        val hoisted = parent.takeHoistedStatements()

        val callStmt = BrsExpressionStatement(transformedCall)

        return if (hoisted.isNotEmpty()) {
            BrsBlock((hoisted + callStmt).toMutableList())
        } else {
            callStmt
        }
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
            // Control flow must be handled as statements, not expressions
            is IrThrow -> visitThrow(element, Unit)
            is IrBreak -> visitBreak(element, Unit)
            is IrContinue -> visitContinue(element, Unit)
            // Type operators (like IMPLICIT_COERCION_TO_UNIT) - recursively unwrap to find the inner statement
            is IrTypeOperatorCall -> {
                val innerArg = element.argument
                when (innerArg) {
                    is IrWhen -> visitWhen(innerArg, Unit)
                    is IrBlock -> visitBlock(innerArg, Unit)
                    is IrTypeOperatorCall -> transformBlockOrStatement(innerArg)  // Recursive unwrap
                    is IrComposite -> {
                        // Composite might wrap a when - process statements individually
                        val compositeStmts = innerArg.statements
                        if (compositeStmts.size == 1 && compositeStmts[0] is IrWhen) {
                            visitWhen(compositeStmts[0] as IrWhen, Unit)
                        } else {
                            // Transform all statements and wrap in block
                            val brsStatements = compositeStmts.mapNotNull { transformBlockOrStatement(it) }
                            BrsBlock(brsStatements.toMutableList())
                        }
                    }
                    else -> {
                        // Transform and check for hoisted statements (from when expressions)
                        val expr = parent.transformExpression(element)
                        val hoisted = parent.takeHoistedStatements()
                        if (hoisted.isNotEmpty()) {
                            if (hoisted.size == 1) hoisted.first() else BrsBlock(hoisted.toMutableList())
                        } else {
                            BrsExpressionStatement(expr)
                        }
                    }
                }
            }
            // Composite expressions might contain when statements
            is IrComposite -> {
                val compositeStmts = element.statements
                if (compositeStmts.size == 1 && compositeStmts[0] is IrWhen) {
                    visitWhen(compositeStmts[0] as IrWhen, Unit)
                } else {
                    // Transform all statements and wrap in block
                    val brsStatements = compositeStmts.mapNotNull { transformBlockOrStatement(it) }
                    BrsBlock(brsStatements.toMutableList())
                }
            }
            is IrExpression -> {
                // Transform and check for hoisted statements (from when expressions)
                val expr = parent.transformExpression(element)
                val hoisted = parent.takeHoistedStatements()
                if (hoisted.isNotEmpty()) {
                    if (hoisted.size == 1) hoisted.first() else BrsBlock(hoisted.toMutableList())
                } else {
                    BrsExpressionStatement(expr)
                }
            }
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
) : IrVisitor<BrsExpression, Unit>() {

    override fun visitElement(element: IrElement, data: Unit): BrsExpression {
        return BrsStringLiteral("/* Unsupported: ${element::class.simpleName} */")
    }

    // ==================== Loops in Expression Context ====================

    override fun visitWhileLoop(loop: IrWhileLoop, data: Unit): BrsExpression {
        // While loops in expression context - wrap in BrsStatementAsExpression
        // so they render as statements inline rather than being treated as expressions
        val whileStmt = parent.statementVisitor.visitWhileLoop(loop, Unit)
        return BrsStatementAsExpression(whileStmt)
    }

    override fun visitDoWhileLoop(loop: IrDoWhileLoop, data: Unit): BrsExpression {
        // Do-while loops in expression context - wrap in BrsStatementAsExpression
        val doWhileStmt = parent.statementVisitor.visitDoWhileLoop(loop, Unit)
        return BrsStatementAsExpression(doWhileStmt)
    }

    // ==================== Control Flow in Expression Context ====================

    override fun visitReturn(expression: IrReturn, data: Unit): BrsExpression {
        // Return statements in expression context (e.g., from inlined functions)
        //
        // After BrsReturnableBlockLowering, returns to returnable blocks become return@block Unit,
        // which the statement visitor converts to "exit while" to break out of the wrapper loop.
        // Returns to functions remain as BrsReturn statements.
        //
        // Route all returns through the statement visitor.
        val returnStmt = parent.statementVisitor.visitReturn(expression, Unit)
        return BrsStatementAsExpression(returnStmt)
    }

    override fun visitThrow(expression: IrThrow, data: Unit): BrsExpression {
        // Throw statements in expression context
        val throwStmt = parent.statementVisitor.visitThrow(expression, Unit)
        return BrsStatementAsExpression(throwStmt)
    }

    override fun visitBreak(jump: IrBreak, data: Unit): BrsExpression {
        // Break statements in expression context
        val breakStmt = parent.statementVisitor.visitBreak(jump, Unit)
        return BrsStatementAsExpression(breakStmt)
    }

    override fun visitContinue(jump: IrContinue, data: Unit): BrsExpression {
        // Continue statements in expression context
        val continueStmt = parent.statementVisitor.visitContinue(jump, Unit)
        return BrsStatementAsExpression(continueStmt)
    }

    override fun visitTry(aTry: IrTry, data: Unit): BrsExpression {
        // Try statements in expression context
        val tryStmt = parent.statementVisitor.visitTry(aTry, Unit)
        return BrsStatementAsExpression(tryStmt)
    }

    // ==================== Literals ====================

    override fun visitConst(expression: IrConst, data: Unit): BrsExpression {
        // Handle unsigned types by wrapping in constructor calls
        // UInt, ULong, UByte, UShort are value classes that need explicit boxing in BrightScript
        if (expression.type.isUnsigned() && expression.kind != IrConstKind.Null) {
            val className = expression.type.classOrNull?.owner?.name?.asString()
            return when (className) {
                "UInt" -> BrsFunctionCall(
                    BrsIdentifier("UInt_create_I_k_"),
                    mutableListOf(BrsIntLiteral(expression.value as Int))
                )
                "ULong" -> BrsFunctionCall(
                    BrsIdentifier("ULong_create_J_k_"),
                    mutableListOf(BrsLongIntLiteral(expression.value as Long))
                )
                "UByte" -> BrsFunctionCall(
                    BrsIdentifier("UByte_create_B_k_"),
                    mutableListOf(BrsIntLiteral((expression.value as Byte).toInt()))
                )
                "UShort" -> BrsFunctionCall(
                    BrsIdentifier("UShort_create_S_k_"),
                    mutableListOf(BrsIntLiteral((expression.value as Short).toInt()))
                )
                else -> {
                    // Unknown unsigned type, fall through to regular handling
                    visitConstPrimitive(expression)
                }
            }
        }
        return visitConstPrimitive(expression)
    }

    private fun visitConstPrimitive(expression: IrConst): BrsExpression {
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

    /**
     * Transform an IrConst to a BrightScript literal expression.
     * Used for @BrsConstant property inlining.
     */
    fun transformConstToLiteral(const: IrConst): BrsExpression {
        return when (const.kind) {
            IrConstKind.Int -> BrsIntLiteral(const.value as Int)
            IrConstKind.Long -> BrsLongIntLiteral(const.value as Long)
            IrConstKind.Float -> BrsFloatLiteral(const.value as Float)
            IrConstKind.Double -> BrsDoubleLiteral(const.value as Double)
            IrConstKind.Boolean -> BrsBooleanLiteral(const.value as Boolean)
            IrConstKind.String -> BrsStringLiteral(const.value as String)
            IrConstKind.Char -> BrsStringLiteral((const.value as Char).toString())
            IrConstKind.Byte -> BrsIntLiteral((const.value as Byte).toInt())
            IrConstKind.Short -> BrsIntLiteral((const.value as Short).toInt())
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

        // Check if there's a temp var name mapping (used when increment is an expression)
        val tempVarName = parent.getTempVarName(expression.symbol)
        if (tempVarName != null) {
            return BrsIdentifier(tempVarName)
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

        // Check if this is a reference to a lambda's extension receiver
        // The receiver parameter is named '__receiver' in the generated BrightScript
        // (not 'm', to avoid collision with closure's m reference for captured variables)
        if (expression.symbol == parent.currentLambdaExtensionReceiver) {
            return BrsIdentifier("__receiver")
        }

        // Check if this is a shared variable (mutable var captured by closure) accessed outside the closure
        // These are boxed in {value: x} and need .value access
        // Note: capturedVar is null here because we returned early above if it wasn't
        // Two detection mechanisms:
        // 1. Via SharedVariablesLowering which sets SHARED_VARIABLE_WRAPPER origin
        // 2. Via BrsSharedVariableDetectionLowering which populates sharedVariables set
        val owner = expression.symbol.owner
        val isSharedVariable = (owner is IrVariable && owner.origin == BrsDeclarationOrigin.SHARED_VARIABLE_WRAPPER) ||
                               expression.symbol in parent.sharedVariables
        if (isSharedVariable) {
            val varName = sanitizeParameterName(rawName)
            return BrsDotAccess(BrsIdentifier(varName), "value")
        }

        return when {
            // In constructor bodies, '<this>' refers to the local 'this' variable being constructed
            // In regular methods, '<this>' refers to 'm' (the object the method was called on)
            rawName == "<this>" -> if (parent.isInConstructorBody) BrsIdentifier("this") else BrsMRef()
            // Sanitize setter parameter names
            rawName.startsWith("<set-") && rawName.endsWith(">") -> BrsIdentifier("value")
            // Sanitize other special names
            rawName.startsWith("<") && rawName.endsWith(">") ->
                BrsIdentifier(rawName.removePrefix("<").removeSuffix(">").replace("-", "_"))
            // For local variables, use the unique name to avoid collisions from inline expansion
            owner is IrVariable -> {
                val baseName = sanitizeParameterName(rawName)
                BrsIdentifier(parent.getVariableName(expression.symbol, baseName))
            }
            // Apply full sanitization including reserved keyword escaping
            else -> BrsIdentifier(sanitizeParameterName(rawName))
        }
    }

    override fun visitGetField(expression: IrGetField, data: Unit): BrsExpression {
        val field = expression.symbol.owner

        // For top-level fields with constant initializers, inline the value directly
        // This avoids the need for initialization statements that can't appear at file level
        if (field.parent is IrFile && field.isFinal) {
            field.initializer?.expression?.let { initializer ->
                if (parent.isConstantExpression(initializer)) {
                    return parent.transformExpression(initializer)
                }
            }
        }

        // For top-level fields (parent is IrFile), use GetGlobalAA()
        val receiver = if (field.parent is IrFile) {
            BrsFunctionCall(BrsIdentifier("GetGlobalAA"), mutableListOf())
        } else {
            expression.receiver?.let { it.accept(this, data) } ?: BrsMRef()
        }

        // Sanitize field name - LocalDeclarationsLowering uses $ prefix for captured vars.
        // Also handle special names like <this> which occur for extension receiver parameters.
        val rawFieldName = field.name.asString()
        val fieldName = when {
            rawFieldName == "<this>" -> "__this"
            rawFieldName.startsWith("<") && rawFieldName.endsWith(">") ->
                rawFieldName.removePrefix("<").removeSuffix(">").replace("-", "_").replace(" ", "_")
            else -> rawFieldName.replace("$", "_")
        }

        // Check if accessing outer class field from inner class
        // The receiver will be the outer class reference
        val fieldParentClass = field.parent as? IrClass
        if (fieldParentClass != null) {
            // If the receiver references an outer class instance (not the current class),
            // it means we're accessing an outer class field
            val receiverSymbol = (expression.receiver as? IrGetValue)?.symbol?.owner
            if (receiverSymbol != null && receiverSymbol.name.asString().contains("\$this")) {
                // This is an outer class reference - access through _outer
                return BrsDotAccess(
                    BrsDotAccess(BrsMRef(), "_outer"),
                    fieldName
                )
            }
        }

        // Check if this field holds a shared variable box (mutable captured variable)
        // If so, we need to read field.value instead of field
        val className = fieldParentClass?.name?.asString() ?: ""
        val fieldKey = "$className.$fieldName"
        val isSharedVariableField = fieldKey in context.sharedVariableFields

        return if (isSharedVariableField) {
            // Read the box's value: m.fieldName.value
            BrsDotAccess(BrsDotAccess(receiver, fieldName), "value")
        } else {
            BrsDotAccess(receiver, fieldName)
        }
    }

    override fun visitSetField(expression: IrSetField, data: Unit): BrsExpression {
        val field = expression.symbol.owner

        // For top-level fields (parent is IrFile), use GetGlobalAA()
        val receiver = if (field.parent is IrFile) {
            BrsFunctionCall(BrsIdentifier("GetGlobalAA"), mutableListOf())
        } else {
            expression.receiver?.let { it.accept(this, data) } ?: BrsMRef()
        }

        // Sanitize field name - LocalDeclarationsLowering uses $ prefix for captured vars.
        // Also handle special names like <this> which occur for extension receiver parameters.
        val rawFieldName = field.name.asString()
        val fieldName = when {
            rawFieldName == "<this>" -> "__this"
            rawFieldName.startsWith("<") && rawFieldName.endsWith(">") ->
                rawFieldName.removePrefix("<").removeSuffix(">").replace("-", "_").replace(" ", "_")
            else -> rawFieldName.replace("$", "_")
        }

        // Check if this field holds a shared variable box (mutable captured variable)
        // EXCEPTION: In constructor body, we're initializing the field with the box itself
        val parentClass = field.parent as? IrClass
        val className = parentClass?.name?.asString() ?: ""
        val fieldKey = "$className.$fieldName"
        val isSharedVariableField = fieldKey in context.sharedVariableFields
        val isInConstructor = parent.isInConstructorBody

        // Generate assignment expression: receiver.field = value (or receiver.field.value = value for shared vars)
        val target = if (isSharedVariableField && !isInConstructor) {
            BrsDotAccess(BrsDotAccess(receiver, fieldName), "value")
        } else {
            BrsDotAccess(receiver, fieldName)
        }

        return BrsBinaryOp(
            target,
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
            else -> sanitizeParameterName(rawName)
        }

        // Check if this is a shared variable accessed outside closure
        // Note: sharedVariables only contains mutable vars, and mutable captures returned above
        if (expression.symbol in parent.sharedVariables) {
            return BrsBinaryOp(
                BrsDotAccess(BrsIdentifier(sanitizeParameterName(sanitizedName)), "value"),
                BrsBinaryOperator.EQ,
                expression.value.accept(this, data)
            )
        }

        // For local variables, use the unique name to avoid collisions from inline expansion
        val owner = expression.symbol.owner
        val targetName = if (owner is IrVariable) {
            parent.getVariableName(expression.symbol, sanitizedName)
        } else {
            sanitizedName
        }

        // Generate assignment expression: varName = value
        return BrsBinaryOp(
            BrsIdentifier(targetName),
            BrsBinaryOperator.EQ,
            expression.value.accept(this, data)
        )
    }

    override fun visitGetObjectValue(expression: IrGetObjectValue, data: Unit): BrsExpression {
        // Special case: Unit doesn't need a getInstance call in BrightScript
        // Unit represents "void" which is just invalid in BrightScript
        if (expression.type.isUnit()) {
            return BrsIdentifier("invalid")
        }

        val objectClass = expression.symbol.owner

        // Object singletons are represented as function calls that create/return the instance
        val name = context.getBrsName(objectClass)

        // Record dependency for the singleton/companion object
        // The getInstance function is tracked via the function manifest
        val getInstanceFuncName = "${name}_getInstance"
        context.recordFunctionDependency(getInstanceFuncName)

        return BrsFunctionCall(BrsIdentifier("${name}_getInstance"), mutableListOf())
    }

    override fun visitGetEnumValue(expression: IrGetEnumValue, data: Unit): BrsExpression {
        val enumClass = expression.symbol.owner.parentAsClass
        val className = context.getBrsName(enumClass)
        val entryName = expression.symbol.owner.name.asString()

        // Ensure enum entries are initialized before accessing.
        // BrightScript enum entries are stored in module scope (m.ClassName_EntryName) and are lazily initialized.
        // We must call initEntries() to ensure the entry exists before returning it.
        // Generate: [ClassName_initEntries(), m.ClassName_EntryName][1]
        // This uses BrightScript's array literal with index [1] to execute init and return the value.
        return BrsIndexAccess(
            BrsArrayLiteral(
                mutableListOf(
                    BrsFunctionCall(BrsIdentifier("${className}_initEntries"), mutableListOf()),
                    BrsDotAccess(BrsIdentifier("m"), "${className}_${entryName}")
                )
            ),
            BrsIntLiteral(1)
        )
    }

    // ==================== KClass / Reflection ====================

    /**
     * Handles class literal references like `Person::class`.
     *
     * For Kotlin classes: generates `__kotlin_KClass_create("ClassName")`
     * For external interfaces (native Roku types): generates `__kotlin_KClass_create("roTypeName")`
     *   where roTypeName matches BrightScript's Type() return value (e.g., "roArray", "roSGScreenEvent")
     */
    override fun visitClassReference(expression: IrClassReference, data: Unit): BrsExpression {
        val classType = expression.classType
        val typeName = getTypeNameForKClass(classType)

        // Generate: __kotlin_KClass_create("TypeName")
        context.recordFunctionDependency("__kotlin_KClass_create")
        return BrsFunctionCall(
            BrsIdentifier("__kotlin_KClass_create"),
            mutableListOf(BrsStringLiteral(typeName))
        )
    }

    /**
     * Handles getting the class of an instance like `dog::class` or `animal::class`.
     *
     * At runtime, this checks if the object is a Kotlin-created object (has __type field)
     * or a native BrightScript object (uses Type() function).
     */
    override fun visitGetClass(expression: IrGetClass, data: Unit): BrsExpression {
        val argument = expression.argument.accept(this, data)

        // Generate: __kotlin_getClass(argument)
        context.recordFunctionDependency("__kotlin_getClass")
        return BrsFunctionCall(
            BrsIdentifier("__kotlin_getClass"),
            mutableListOf(argument)
        )
    }

    /**
     * Gets the type name string for a KClass based on the IR type.
     *
     * For native Roku types (external interfaces), the name should match
     * what BrightScript's Type() function returns (e.g., "roArray", "roSGScreenEvent").
     * For Kotlin classes, uses the BRS-mangled class name.
     */
    private fun getTypeNameForKClass(type: IrType): String {
        val classifier = type.classifierOrNull ?: return "Object"

        // Handle type parameters - use "Object" as a fallback
        if (classifier is IrTypeParameterSymbol) {
            return "Object"
        }

        val irClass = type.classOrNull?.owner ?: return "Object"

        // For native Roku types (external interfaces), use the BrightScript type name
        // that matches what Type() returns at runtime
        if (irClass.isExternal) {
            val className = irClass.name.asString()
            // External interfaces in kotlin.brs.roku use PascalCase (e.g., RoArray, RoSGScreenEvent)
            // BrightScript Type() returns "roArray", "roSGScreenEvent", etc.
            // Convert: RoArray -> roArray, RoSGScreenEvent -> roSGScreenEvent
            if (className.startsWith("Ro") && className.length > 2) {
                return "ro${className.substring(2)}"
            }
            // For interfaces like IEnumNative, IArray, etc., use the raw name
            // These typically won't be used as map keys directly
            return className
        }

        // For Kotlin classes, use the BRS-mangled class name
        return context.getBrsName(irClass)
    }

    // ==================== Function Calls ====================

    override fun visitCall(expression: IrCall, data: Unit): BrsExpression {
        val function = expression.symbol.owner

        // Check for operator expressions based on origin
        expression.origin?.let { origin ->
            val operatorResult = transformOperator(expression, origin)
            if (operatorResult != null) return operatorResult
        }

        // Handle identity comparison (===) even when origin is null
        // This happens when coroutine lowering creates calls via local buildCall() with null origin
        if (expression.symbol == context.irBuiltIns.eqeqeqSymbol) {
            val left = expression.getValueArgument(0)?.accept(this, data) ?: return BrsInvalidLiteral()
            val right = expression.getValueArgument(1)?.accept(this, data) ?: return BrsInvalidLiteral()
            context.recordFunctionDependency("__kotlin_identityEquals")
            return BrsFunctionCall(
                BrsIdentifier("__kotlin_identityEquals"),
                mutableListOf(left, right)
            )
        }

        // Handle builtin comparison functions (less, lessOrEqual, greater, greaterOrEqual)
        // These come from irBuiltIns and may not have an origin when introduced by lowering passes
        val builtinComparisonResult = transformBuiltinComparison(expression)
        if (builtinComparisonResult != null) return builtinComparisonResult

        // Handle kotlin.internal.ir intrinsics like CHECK_NOT_NULL, THROW_CCE, etc.
        // These are built-in operators that need to be lowered to BrightScript
        val intrinsicFunctionName = function.name.asString()
        val intrinsicPackageFqName = function.getPackageFragment()?.packageFqName?.asString() ?: ""
        if (intrinsicPackageFqName.startsWith("kotlin.internal")) {
            when (intrinsicFunctionName) {
                "CHECK_NOT_NULL" -> {
                    // In BrightScript we don't have strict null checking,
                    // so just return the value argument directly
                    return expression.getValueArgument(0)?.accept(this, data) ?: BrsInvalidLiteral()
                }
                "THROW_CCE" -> {
                    // Class cast exception - generate a stop statement for debugging
                    return BrsIdentifier("invalid")
                }
                "THROW_ISE" -> {
                    // Illegal state exception - generate a stop statement for debugging
                    return BrsIdentifier("invalid")
                }
            }
        }

        // Handle BrightScript runtime intrinsics (brsFormatJson, brsParseJson, etc.)
        // These are defined in kotlin.brs.runtime but are not marked external
        val runtimeFunctionName = function.name.asString()
        val runtimePackageFqName = function.getPackageFragment()?.packageFqName?.asString() ?: ""
        if (runtimePackageFqName == "kotlin.brs.runtime") {
            when (runtimeFunctionName) {
                "brsFormatJson" -> {
                    // Convert Kotlin collections to plain BrightScript types before JSON serialization
                    val arg = expression.getValueArgument(0)?.accept(this, data) ?: BrsInvalidLiteral()
                    context.recordFunctionDependency("__kotlin_toJsonValue_AnyN_k_")
                    val converted = BrsFunctionCall(BrsIdentifier("__kotlin_toJsonValue_AnyN_k_"), mutableListOf(arg))
                    return BrsFunctionCall(BrsIdentifier("FormatJson"), mutableListOf(converted))
                }
                "brsParseJson" -> {
                    val arg = expression.getValueArgument(0)?.accept(this, data) ?: BrsInvalidLiteral()
                    return BrsFunctionCall(BrsIdentifier("ParseJson"), mutableListOf(arg))
                }
                "brsTypeOf" -> {
                    val arg = expression.getValueArgument(0)?.accept(this, data) ?: BrsInvalidLiteral()
                    return BrsTypeOf(arg)
                }
                "brsIsInvalid" -> {
                    val arg = expression.getValueArgument(0)?.accept(this, data) ?: BrsInvalidLiteral()
                    return BrsBinaryOp(arg, BrsBinaryOperator.EQ, BrsInvalidLiteral())
                }
                "brsCreateObject" -> {
                    val typeArg = expression.getValueArgument(0)
                    val objectType = (typeArg as? IrConst)?.let { it.value.toString() } ?: "Object"
                    val args = (1 until expression.valueArgumentsCount).mapNotNull { i ->
                        expression.getValueArgument(i)?.accept(this, data)
                    }
                    return BrsCreateObject(objectType, args.toMutableList())
                }
                "brsCreateArray" -> {
                    val sizeArg = expression.getValueArgument(0)?.accept(this, data) ?: BrsIntLiteral(0)
                    return BrsCreateObject("roArray", mutableListOf(sizeArg, BrsBooleanLiteral(true)))
                }
                "brsArrayLength" -> {
                    val arg = expression.getValueArgument(0)?.accept(this, data) ?: BrsInvalidLiteral()
                    return BrsMethodCall(arg, "count", mutableListOf())
                }
                "brsCreateAssociativeArray" -> {
                    return BrsAALiteral()
                }
                "brsCreatePlainAA" -> {
                    // Create a plain roAssociativeArray (not a Kotlin class instance)
                    return BrsCreateObject("roAssociativeArray", mutableListOf())
                }
                "brsAAAddReplace" -> {
                    // aa.AddReplace(key, value)
                    val aa = expression.getValueArgument(0)?.accept(this, data) ?: BrsInvalidLiteral()
                    val key = expression.getValueArgument(1)?.accept(this, data) ?: BrsInvalidLiteral()
                    val value = expression.getValueArgument(2)?.accept(this, data) ?: BrsInvalidLiteral()
                    return BrsMethodCall(aa, "AddReplace", mutableListOf(key, value))
                }
                "brsPrint" -> {
                    val arg = expression.getValueArgument(0)?.accept(this, data) ?: BrsInvalidLiteral()
                    return BrsFunctionCall(BrsIdentifier("print"), mutableListOf(arg))
                }
                // AA/Array intrinsics for JSON serialization
                "brsHasField" -> {
                    // DoesExist on AA
                    val obj = expression.getValueArgument(0)?.accept(this, data) ?: BrsInvalidLiteral()
                    val field = expression.getValueArgument(1)?.accept(this, data) ?: BrsStringLiteral("")
                    return BrsMethodCall(obj, "DoesExist", mutableListOf(field))
                }
                "brsGetField" -> {
                    // Direct field access via Lookup
                    val obj = expression.getValueArgument(0)?.accept(this, data) ?: BrsInvalidLiteral()
                    val field = expression.getValueArgument(1)?.accept(this, data) ?: BrsStringLiteral("")
                    return BrsMethodCall(obj, "Lookup", mutableListOf(field))
                }
                "brsIntrinsicCreateObject" -> {
                    val typeArg = expression.getValueArgument(0)
                    val objectType = (typeArg as? IrConst)?.let { it.value.toString() } ?: "roAssociativeArray"
                    // roArray needs size and resize flag arguments
                    val args = if (objectType == "roArray") {
                        mutableListOf<BrsExpression>(BrsIntLiteral(0), BrsBooleanLiteral(true))
                    } else {
                        mutableListOf()
                    }
                    return BrsCreateObject(objectType, args)
                }
                "brsIntrinsicKeys" -> {
                    val aa = expression.getValueArgument(0)?.accept(this, data) ?: BrsInvalidLiteral()
                    return BrsMethodCall(aa, "Keys", mutableListOf())
                }
                "brsIntrinsicGetField" -> {
                    // Same as brsGetField but named differently
                    val obj = expression.getValueArgument(0)?.accept(this, data) ?: BrsInvalidLiteral()
                    val field = expression.getValueArgument(1)?.accept(this, data) ?: BrsStringLiteral("")
                    return BrsMethodCall(obj, "Lookup", mutableListOf(field))
                }
                "brsIntrinsicAddReplace" -> {
                    val aa = expression.getValueArgument(0)?.accept(this, data) ?: BrsInvalidLiteral()
                    val key = expression.getValueArgument(1)?.accept(this, data) ?: BrsStringLiteral("")
                    val value = expression.getValueArgument(2)?.accept(this, data) ?: BrsInvalidLiteral()
                    return BrsMethodCall(aa, "AddReplace", mutableListOf(key, value))
                }
                "brsIntrinsicCallGetArray" -> {
                    val collection = expression.getValueArgument(0)?.accept(this, data) ?: BrsInvalidLiteral()
                    return BrsMethodCall(collection, "__get_array", mutableListOf())
                }
                "brsIntrinsicCount" -> {
                    val array = expression.getValueArgument(0)?.accept(this, data) ?: BrsInvalidLiteral()
                    return BrsMethodCall(array, "count", mutableListOf())
                }
                "brsIntrinsicArrayGet" -> {
                    val array = expression.getValueArgument(0)?.accept(this, data) ?: BrsInvalidLiteral()
                    val index = expression.getValueArgument(1)?.accept(this, data) ?: BrsIntLiteral(0)
                    return BrsIndexAccess(array, index)
                }
                "brsIntrinsicArrayPush" -> {
                    val array = expression.getValueArgument(0)?.accept(this, data) ?: BrsInvalidLiteral()
                    val value = expression.getValueArgument(1)?.accept(this, data) ?: BrsInvalidLiteral()
                    return BrsMethodCall(array, "push", mutableListOf(value))
                }
                "brsIntrinsicCallMethod" -> {
                    val obj = expression.getValueArgument(0)?.accept(this, data) ?: BrsInvalidLiteral()
                    val methodArg = expression.getValueArgument(1)
                    val methodName = (methodArg as? IrConst)?.let { it.value.toString() } ?: "unknown"
                    return BrsMethodCall(obj, methodName, mutableListOf())
                }
                "brsStringEquals" -> {
                    // Simple string comparison: (a = b)
                    val a = expression.getValueArgument(0)?.accept(this, data) ?: BrsInvalidLiteral()
                    val b = expression.getValueArgument(1)?.accept(this, data) ?: BrsStringLiteral("")
                    return BrsBinaryOp(a, BrsBinaryOperator.EQ, b)
                }
                "brsToString" -> {
                    // Convert value to string using the existing runtime function
                    val value = expression.getValueArgument(0)?.accept(this, data) ?: BrsInvalidLiteral()
                    context.recordFunctionDependency("toString_AnyN_k_")
                    return BrsFunctionCall(BrsIdentifier("toString_AnyN_k_"), mutableListOf(value))
                }
                "brsIntrinsicStartsWith" -> {
                    // Left(str, Len(prefix)) = prefix
                    val str = expression.getValueArgument(0)?.accept(this, data) ?: BrsInvalidLiteral()
                    val prefix = expression.getValueArgument(1)?.accept(this, data) ?: BrsStringLiteral("")
                    val lenCall = BrsFunctionCall(BrsIdentifier("Len"), mutableListOf(prefix))
                    val leftCall = BrsFunctionCall(BrsIdentifier("Left"), mutableListOf(str, lenCall))
                    return BrsBinaryOp(leftCall, BrsBinaryOperator.EQ, prefix)
                }
                "brsIntrinsicEndsWith" -> {
                    // Right(str, Len(suffix)) = suffix
                    val str = expression.getValueArgument(0)?.accept(this, data) ?: BrsInvalidLiteral()
                    val suffix = expression.getValueArgument(1)?.accept(this, data) ?: BrsStringLiteral("")
                    val lenCall = BrsFunctionCall(BrsIdentifier("Len"), mutableListOf(suffix))
                    val rightCall = BrsFunctionCall(BrsIdentifier("Right"), mutableListOf(str, lenCall))
                    return BrsBinaryOp(rightCall, BrsBinaryOperator.EQ, suffix)
                }
            }
        }

        // Check for @BrsCreateObject annotation - compile to CreateObject(typeName, args...)
        val brsCreateObjectAnnotation = function.getAnnotation(BrsStandardClassIds.Annotations.BrsCreateObject.asSingleFqName())
        if (brsCreateObjectAnnotation != null) {
            val typeNameArg = brsCreateObjectAnnotation.getValueArgument(0)
            val typeName = if (typeNameArg is IrConst) {
                (typeNameArg as IrConst).value as String
            } else {
                // Fallback if annotation value is not a simple constant
                "Object"
            }
            val args = mutableListOf<BrsExpression>(BrsStringLiteral(typeName))
            // Add all call arguments
            for (i in 0 until expression.valueArgumentsCount) {
                expression.getValueArgument(i)?.let { args.add(it.accept(this, data)) }
            }
            return BrsFunctionCall(BrsIdentifier("CreateObject"), args)
        }

        // Check for @BrsInline functions - inline at call site
        if (parent.inlineCallTransformer.shouldInline(expression)) {
            return transformBrsInlineCall(expression)
        }

        // Check for @BrsNamespace object calls - these were preprocessed by BrsExternalLowering
        // and the resolved name is stored in context.namespaceCallNames
        context.namespaceCallNames[expression]?.let { resolvedName ->
            val args = mutableListOf<BrsExpression>()
            for (i in 0 until expression.valueArgumentsCount) {
                expression.getValueArgument(i)?.let { args.add(it.accept(this, data)) }
            }
            return BrsFunctionCall(BrsIdentifier(resolvedName), args)
        }

        // ==================== @BrsConstant Property Inlining Optimization ====================
        // When accessing properties on @BrsConstant objects, inline the evaluated constant value
        // directly to avoid runtime getInstance() calls and property lookups.
        val constDispatchReceiver = expression.dispatchReceiver
        if (constDispatchReceiver is IrGetObjectValue) {
            val objectClass = constDispatchReceiver.symbol.owner
            if (context.isConstantObject(objectClass)) {
                val functionName = function.name.asString()
                if (functionName.startsWith("<get-")) {
                    val propName = functionName.removePrefix("<get-").removeSuffix(">")
                    context.getConstantObjectProperty(objectClass, propName)?.let { constValue ->
                        return transformConstToLiteral(constValue)
                    }
                }
            }
        }
        // ==================== End @BrsConstant Property Inlining ====================

        // ==================== Enum Property Inlining Optimization ====================
        // When accessing ordinal, name, or constant properties on a compile-time-known
        // enum value, inline the value directly to avoid runtime lookup overhead.
        val dispatchReceiver = expression.dispatchReceiver
        if (dispatchReceiver is IrGetEnumValue) {
            val entry = dispatchReceiver.symbol.owner
            val functionName = function.name.asString()

            when {
                // Inline .ordinal -> integer literal
                functionName == "<get-ordinal>" || functionName == "ordinal" -> {
                    context.getEnumOrdinal(entry)?.let { ordinal ->
                        return BrsIntLiteral(ordinal)
                    }
                }
                // Inline .name -> string literal
                functionName == "<get-name>" || functionName == "name" -> {
                    context.getEnumName(entry)?.let { name ->
                        return BrsStringLiteral(name)
                    }
                }
                // Inline custom val properties with constant values
                functionName.startsWith("<get-") -> {
                    val propName = functionName.removePrefix("<get-").removeSuffix(">")
                    context.getEnumConstantProperties(entry)?.get(propName)?.let { value ->
                        when (value) {
                            is Int -> return BrsIntLiteral(value)
                            is Long -> return BrsLongIntLiteral(value)
                            is String -> return BrsStringLiteral(value)
                            is Boolean -> return BrsBooleanLiteral(value)
                            is Double -> return BrsDoubleLiteral(value)
                            is Float -> return BrsFloatLiteral(value)
                            is Byte -> return BrsIntLiteral(value.toInt())
                            is Short -> return BrsIntLiteral(value.toInt())
                            is Char -> return BrsStringLiteral(value.toString())
                            // Fall through to default handling for other types
                        }
                    }
                }
            }
        }
        // ==================== End Enum Property Inlining ====================

        // Handle primitive type conversion methods and unary operators
        // These are Kotlin methods that don't exist in BrightScript - we need to transform them
        val methodName = function.name.asString()
        val receiver = expression.dispatchReceiver ?: expression.extensionReceiver
        if (receiver != null) {
            // Handle Array.size - BrightScript arrays use .count() not .size property
            if (methodName == "<get-size>" && receiver.type.isArray()) {
                val receiverExpr = receiver.accept(this, data)
                return BrsMethodCall(receiverExpr, "count", mutableListOf())
            }

            // Handle String.length - BrightScript strings use Len() function
            // Native BrightScript strings don't have methods, so str.get_length() won't work.
            // IMPORTANT: Use isStringClassType() to handle both String and String? types.
            // CharSequence could be implemented by StringBuilder or other classes that have
            // a get_length property, so we need polymorphic dispatch for those cases.
            if (methodName == "<get-length>" || methodName == "length") {
                if (receiver.type.isStringClassType()) {
                    val receiverExpr = receiver.accept(this, data)
                    return BrsFunctionCall(BrsIdentifier("Len"), mutableListOf(receiverExpr))
                }
                // For CharSequence (interface that String implements), we need runtime polymorphism.
                // Native BrightScript strings use Len(), while StringBuilder uses get_length().
                // Check if the receiver type is CharSequence by checking the fqName
                val receiverClass = receiver.type.classOrNull?.owner
                val isCharSequence = receiverClass?.fqNameWhenAvailable?.asString() == "kotlin.CharSequence"
                if (isCharSequence) {
                    val receiverExpr = receiver.accept(this, data)
                    // Generate polymorphic dispatch using __kotlin_charSequenceLength helper
                    // Note: The mangled name includes the parameter type (_CharSequenceN_k_)
                    return BrsFunctionCall(
                        BrsIdentifier("__kotlin_charSequenceLength_CharSequenceN_k_"),
                        mutableListOf(receiverExpr)
                    )
                }
                // For other types, fall through to normal property access
                // which will generate m.get_length() for proper polymorphic dispatch
            }

            // Handle Array.get(index) - BrightScript arrays use [] indexing, not .get() method
            if (methodName == "get" && receiver.type.isArray() && expression.valueArgumentsCount == 1) {
                val receiverExpr = receiver.accept(this, data)
                val indexExpr = expression.getValueArgument(0)?.accept(this, data) ?: return BrsInvalidLiteral()
                return BrsIndexAccess(receiverExpr, indexExpr)
            }

            // Handle String.get(index) - BrightScript uses Mid(string, index+1, 1) to get a character
            // Note: BrightScript Mid() is 1-indexed, so we add 1 to the index
            if (methodName == "get" && receiver.type.isString() && expression.valueArgumentsCount == 1) {
                val receiverExpr = receiver.accept(this, data)
                val indexExpr = expression.getValueArgument(0)?.accept(this, data) ?: return BrsInvalidLiteral()
                // Mid(string, index + 1, 1) - BrightScript is 1-indexed
                return BrsFunctionCall(
                    BrsIdentifier("Mid"),
                    mutableListOf(
                        receiverExpr,
                        BrsBinaryOp(indexExpr, BrsBinaryOperator.ADD, BrsIntLiteral(1)),
                        BrsIntLiteral(1)
                    )
                )
            }

            // Handle Array.set(index, value) - BrightScript arrays use [] indexing, not .set() method
            if (methodName == "set" && receiver.type.isArray() && expression.valueArgumentsCount == 2) {
                val receiverExpr = receiver.accept(this, data)
                val indexExpr = expression.getValueArgument(0)?.accept(this, data) ?: return BrsInvalidLiteral()
                val valueExpr = expression.getValueArgument(1)?.accept(this, data) ?: return BrsInvalidLiteral()
                return BrsBinaryOp(BrsIndexAccess(receiverExpr, indexExpr), BrsBinaryOperator.EQ, valueExpr)
            }

            when (methodName) {
                // Type conversions - BrightScript handles most implicitly
                "toDouble", "toFloat", "toInt", "toLong", "toShort", "toByte" -> {
                    return receiver.accept(this, data)
                }
                // Int.toChar() needs Chr() in BrightScript to convert integer code to character string
                "toChar" -> {
                    val receiverType = receiver.type
                    if (receiverType.isInt() || receiverType.isLong() || receiverType.isShort() || receiverType.isByte()) {
                        return BrsFunctionCall(
                            BrsIdentifier("Chr"),
                            mutableListOf(receiver.accept(this, data))
                        )
                    }
                    // For Char.toChar(), it's a no-op
                    return receiver.accept(this, data)
                }
                // Unary minus/plus as method calls (when they don't have UMINUS/UPLUS origin)
                // Only convert to unary operators if receiver is a primitive type.
                // Non-primitive types (UInt, ULong, custom classes) need method calls.
                "unaryMinus" -> {
                    if (receiver.type.isPrimitiveForArithmetic()) {
                        return BrsUnaryOp(BrsUnaryOperator.NEG, receiver.accept(this, data))
                    }
                    // Fall through to normal method call handling for non-primitives
                }
                "unaryPlus" -> {
                    if (receiver.type.isPrimitiveForArithmetic()) {
                        return receiver.accept(this, data)  // No-op for primitives
                    }
                    // Fall through to normal method call handling for non-primitives
                }
                // Boolean.not() - BrightScript uses prefix 'not' operator, not a method call
                "not" -> {
                    if (receiver.type.isBoolean()) {
                        return BrsUnaryOp(BrsUnaryOperator.NOT, receiver.accept(this, data))
                    }
                }
                // Binary plus as method call (String.plus, etc.)
                // Only convert to binary operator if receiver is a primitive type that supports +.
                // Non-primitive types (UInt, ULong, custom classes) need method calls.
                // Note: For shared variables (boxed in {value: x}), the receiver.type may be anyNType
                // but the function's declared receiver type (dispatchReceiverParameter.type) preserves
                // the original type. We use the function's declared type to detect primitives.
                "plus" -> {
                    val arg = expression.getValueArgument(0)
                    val functionReceiverType = function.dispatchReceiverParameter?.type ?: receiver.type
                    if (arg != null && functionReceiverType.isPrimitiveForArithmetic()) {
                        var left = receiver.accept(this, data)
                        var right = arg.accept(this, data)

                        // For PLUS operations involving strings, convert non-string operands to strings
                        // BrightScript's + operator cannot mix Integer/Float with String
                        val leftIsString = receiver.type.isString()
                        val rightIsString = arg.type.isString()

                        if (leftIsString && !rightIsString) {
                            // Right operand needs string conversion
                            right = parent.transformToString(right, arg.type)
                        } else if (!leftIsString && rightIsString) {
                            // Left operand needs string conversion
                            left = parent.transformToString(left, receiver.type)
                        }

                        return BrsBinaryOp(left, BrsBinaryOperator.ADD, right)
                    }
                    // Fall through to normal method call handling for non-primitives
                }
                // toString - handle primitives specially since BrightScript primitives don't have methods
                "toString" -> {
                    val receiverType = receiver.type
                    val receiverExpr = receiver.accept(this, data)
                    return parent.transformToString(receiverExpr, receiverType)
                }
            }
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
                    // Generate mangled invoke method name based on function parameters
                    // Lambda classes from callable reference lowering use mangled names like invoke_AnyN_k_
                    // The mangling suffix is based on the parameter types
                    val paramTypes = (0 until function.valueParameters.size).map { i ->
                        val param = function.valueParameters[i]
                        context.typeToMangledString(param.type)
                    }
                    val mangledSuffix = if (paramTypes.isEmpty()) "k_" else "${paramTypes.joinToString("_")}_k_"
                    val invokeMethodName = "invoke_$mangledSuffix"
                    return BrsMethodCall(receiverExpr, invokeMethodName, args.toMutableList())
                }
            }
        }

        // Handle calls to local functions (parent is another function, not a class or file)
        // Local functions are now closure objects, so we need to call .invoke()
        val isLocalFunction = function.parent is IrFunction
        if (isLocalFunction) {
            val localFunctionName = function.name.asString()
            val args = mutableListOf<BrsExpression>()
            for (i in 0 until expression.valueArgumentsCount) {
                val arg = expression.getValueArgument(i)
                if (arg != null) {
                    args.add(arg.accept(this, data))
                } else {
                    // Parameter uses default value - add invalid to preserve position
                    args.add(BrsInvalidLiteral())
                }
            }
            // Generate mangled invoke method name based on function parameters
            val paramTypes = function.valueParameters.map { param ->
                context.typeToMangledString(param.type)
            }
            val mangledSuffix = if (paramTypes.isEmpty()) "k_" else "${paramTypes.joinToString("_")}_k_"
            val invokeMethodName = "invoke_$mangledSuffix"
            // Call .invoke_*() method on the local function closure object
            return BrsMethodCall(BrsIdentifier(localFunctionName), invokeMethodName, args)
        }

        val functionName = context.getBrsName(function)

        // Record dependency for this function call using the new unified tracking
        // This uses the function manifest to look up the target file, capturing
        // all dependencies including those from lowering-introduced code
        context.recordFunctionDependency(functionName)

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
            // from the outer class (not the current inner class), we access through _outer
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
                // Access through _outer for inner class outer reference
                BrsDotAccess(BrsMRef(), "_outer")
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
            // Use expression.dispatchReceiver to detect dispatch calls (more reliable than
            // function.dispatchReceiverParameter which may be null for interface methods)
            if (expression.dispatchReceiver != null) {
                // Check if the receiver type is a primitive.
                // BrightScript primitives don't support method calls, so we must use function calls
                // with the receiver as the first argument.
                val actualReceiverType = receiver.type
                val isPrimitiveReceiver = actualReceiverType.isInt() || actualReceiverType.isLong() ||
                    actualReceiverType.isFloat() || actualReceiverType.isDouble() ||
                    actualReceiverType.isShort() || actualReceiverType.isByte() ||
                    actualReceiverType.isBoolean() || actualReceiverType.isChar() ||
                    actualReceiverType.isStringClassType()

                // For any call on a primitive receiver, use a function call instead of method call
                if (isPrimitiveReceiver) {
                    val rawName = function.name.asString()

                    // Handle primitive bitwise, comparison, and arithmetic operations as intrinsics
                    // These operations should be compiled inline, not as function calls
                    when (rawName) {
                        // Arithmetic operators - compile to native BrightScript operators
                        "plus" -> {
                            val left = receiverExpr
                            val right = expression.getValueArgument(0)!!.accept(this, data)
                            return BrsBinaryOp(left, BrsBinaryOperator.ADD, right)
                        }
                        "minus" -> {
                            val left = receiverExpr
                            val right = expression.getValueArgument(0)!!.accept(this, data)
                            return BrsBinaryOp(left, BrsBinaryOperator.SUB, right)
                        }
                        "times" -> {
                            val left = receiverExpr
                            val right = expression.getValueArgument(0)!!.accept(this, data)
                            return BrsBinaryOp(left, BrsBinaryOperator.MUL, right)
                        }
                        "div" -> {
                            val left = receiverExpr
                            val right = expression.getValueArgument(0)!!.accept(this, data)
                            return BrsBinaryOp(left, BrsBinaryOperator.DIV, right)
                        }
                        "rem" -> {
                            val left = receiverExpr
                            val right = expression.getValueArgument(0)!!.accept(this, data)
                            return BrsBinaryOp(left, BrsBinaryOperator.MOD, right)
                        }
                        "xor" -> {
                            // BrightScript has NO native XOR operator
                            // Implement as: (a or b) and not (a and b)
                            val left = receiverExpr
                            val right = expression.getValueArgument(0)!!.accept(this, data)
                            // (left or right) and not (left and right)
                            val orPart = BrsBinaryOp(left, BrsBinaryOperator.OR, right)
                            val andPart = BrsBinaryOp(left.deepCopy(), BrsBinaryOperator.AND, right.deepCopy())
                            val notAndPart = BrsUnaryOp(BrsUnaryOperator.NOT, andPart)
                            return BrsBinaryOp(orPart, BrsBinaryOperator.AND, notAndPart)
                        }
                        "and" -> {
                            val left = receiverExpr
                            val right = expression.getValueArgument(0)!!.accept(this, data)
                            return BrsBinaryOp(left, BrsBinaryOperator.AND, right)
                        }
                        "or" -> {
                            val left = receiverExpr
                            val right = expression.getValueArgument(0)!!.accept(this, data)
                            return BrsBinaryOp(left, BrsBinaryOperator.OR, right)
                        }
                        "inv" -> {
                            return BrsUnaryOp(BrsUnaryOperator.NOT, receiverExpr)
                        }
                        "compareTo" -> {
                            val left = receiverExpr
                            val right = expression.getValueArgument(0)!!.accept(this, data)
                            // String/Char comparison uses string comparison; numeric types use __kotlin_intCompare
                            return if (actualReceiverType.isChar() || actualReceiverType.isStringClassType()) {
                                // For Char and String (strings in BrightScript), use __kotlin_stringCompare
                                // which compares strings lexicographically and returns -1, 0, or 1
                                BrsFunctionCall(
                                    BrsIdentifier("__kotlin_stringCompare"),
                                    mutableListOf(left, right)
                                )
                            } else {
                                // Use runtime helper function for numeric compareTo
                                BrsFunctionCall(
                                    BrsIdentifier("__kotlin_intCompare"),
                                    mutableListOf(left, right)
                                )
                            }
                        }
                        "equals" -> {
                            // String.equals(Any?) or primitive.equals(Any?) should be compiled to native =
                            // Only for the single-argument version (not the ignoreCase extension)
                            if (expression.valueArgumentsCount == 1) {
                                val left = receiverExpr
                                val right = expression.getValueArgument(0)!!.accept(this, data)
                                return BrsBinaryOp(left, BrsBinaryOperator.EQ, right)
                            }
                            // For multi-argument equals (extension function), fall through
                        }
                        "hashCode" -> {
                            // For strings, use a hash function; for primitives, the value itself
                            return if (actualReceiverType.isStringClassType()) {
                                // String hashCode needs a runtime helper
                                BrsFunctionCall(
                                    BrsIdentifier("__kotlin_stringHashCode"),
                                    mutableListOf(receiverExpr)
                                )
                            } else {
                                // For numeric primitives, the value is the hash
                                receiverExpr
                            }
                        }
                        "toString" -> {
                            // For strings, just return the string; for primitives, convert
                            return if (actualReceiverType.isStringClassType()) {
                                receiverExpr
                            } else {
                                BrsFunctionCall(
                                    BrsIdentifier("Str"),
                                    mutableListOf(receiverExpr)
                                )
                            }
                        }
                        "shl" -> {
                            // Left shift: a * (2 ^ b)
                            val left = receiverExpr
                            val right = expression.getValueArgument(0)!!.accept(this, data)
                            val powerOf2 = BrsBinaryOp(BrsIntLiteral(2), BrsBinaryOperator.POW, right)
                            return BrsBinaryOp(left, BrsBinaryOperator.MUL, powerOf2)
                        }
                        "shr" -> {
                            // Signed right shift: a \ (2 ^ b) using integer division
                            val left = receiverExpr
                            val right = expression.getValueArgument(0)!!.accept(this, data)
                            val powerOf2 = BrsBinaryOp(BrsIntLiteral(2), BrsBinaryOperator.POW, right)
                            return BrsBinaryOp(left, BrsBinaryOperator.INT_DIV, powerOf2)
                        }
                        "ushr" -> {
                            // Unsigned right shift - use helper function
                            val left = receiverExpr
                            val right = expression.getValueArgument(0)!!.accept(this, data)
                            return BrsFunctionCall(
                                BrsIdentifier("__kotlin_ushr"),
                                mutableListOf(left, right)
                            )
                        }
                    }

                    val args = mutableListOf<BrsExpression>()
                    args.add(receiverExpr)
                    for (i in 0 until expression.valueArgumentsCount) {
                        expression.getValueArgument(i)?.let { arg ->
                            args.add(arg.accept(this, data))
                        }
                    }
                    // For extension functions on primitives, the function name should NOT have
                    // a class prefix. Extension functions are defined as top-level functions with
                    // the receiver type encoded in the signature (e.g., rangeTo_rI_I_IntRange_k_).
                    // The issue is getBrsName returns a name based on the call-site parent (Int class),
                    // not the definition-site parent (package). We need to reconstruct the correct name.
                    // Check if functionName starts with a primitive class prefix that needs stripping
                    val primitiveClassPrefixes = listOf("Int_", "Long_", "Float_", "Double_", "Short_", "Byte_", "Boolean_", "Char_")
                    val hasWrongClassPrefix = primitiveClassPrefixes.any { functionName.startsWith(it) }

                    val correctedFunctionName = if (hasWrongClassPrefix) {
                        // Rebuild the function name with correct format:
                        // functionName_rReceiverType_ParamTypes_k_
                        // Note: Return types are NOT included (like Java) to support polymorphism
                        val rawName = function.name.asString()

                        // Build signature parts
                        val signatureParts = mutableListOf<String>()

                        // Add receiver type with 'r' prefix - use the actual receiver type
                        signatureParts.add("r" + context.typeToMangledString(actualReceiverType))

                        // Add parameter types
                        function.valueParameters.forEach { param ->
                            signatureParts.add(context.typeToMangledString(param.type))
                        }

                        // Build the full name (no return type)
                        val signature = signatureParts.joinToString("_")
                        "${rawName}_${signature}_k_"
                    } else {
                        functionName
                    }
                    return BrsFunctionCall(BrsIdentifier(correctedFunctionName), args)
                }

                // Check if this is a call on a singleton object
                // Singleton methods that reference `m` (like property getters) need to be called
                // as methods on the singleton instance, not as standalone functions.
                val parentClass = function.parent as? IrClass
                if (parentClass?.kind == ClassKind.OBJECT) {
                    val args = (0 until expression.valueArgumentsCount).mapNotNull { i ->
                        expression.getValueArgument(i)?.let { it.accept(this, data) }
                    }
                    val singletonName = context.getBrsName(parentClass)
                    context.recordFunctionDependency("${singletonName}_getInstance")
                    val singletonInstance = BrsFunctionCall(BrsIdentifier("${singletonName}_getInstance"), mutableListOf())
                    // Property accessors are attached with simple names (__get_X, __set_X)
                    // Regular methods are attached with mangled names
                    val rawFuncName = function.name.asString()
                    val methodName = if (rawFuncName.startsWith("<get-") && rawFuncName.endsWith(">")) {
                        "__get_" + rawFuncName.removePrefix("<get-").removeSuffix(">")
                    } else if (rawFuncName.startsWith("<set-") && rawFuncName.endsWith(">")) {
                        "__set_" + rawFuncName.removePrefix("<set-").removeSuffix(">")
                    } else {
                        // Use mangled name for regular methods
                        val fullMethodName = context.getBrsName(function)
                        fullMethodName.removePrefix("${singletonName}_")
                    }
                    return BrsMethodCall(singletonInstance, methodName, args.toMutableList())
                }

                // Handle property getters and setters
                // Check if the property has a simple backing field that can be accessed directly.
                // For computed properties (custom getter), overridden properties (interface impl),
                // or properties from interfaces, we need to call the getter method.
                val rawName = function.name.asString()
                if (rawName.startsWith("<get-") && rawName.endsWith(">")) {
                    val fieldName = rawName.removePrefix("<get-").removeSuffix(">")
                    val property = function.correspondingPropertySymbol?.owner
                    val backingField = property?.backingField

                    // Special handling for SceneGraph component scope properties (top, global, m)
                    // When in component context, these compile to m.top, m.global, m
                    // When inside a lambda, use getComponentMRef() to get the captured component reference
                    if (parent.isInComponentContext && context.intrinsics.isComponentScopeProperty(fieldName)) {
                        val componentM = parent.getComponentMRef()
                        return when (fieldName) {
                            "m" -> componentM  // Just m (or m._componentM in lambda)
                            else -> BrsDotAccess(componentM, fieldName)  // m.top, m.global
                        }
                    }

                    // Handle user-defined properties in component context
                    // Interface fields (with @SGField or @BrsField) compile to m.top.fieldName
                    // Internal state (no annotation) compiles to m.fieldName
                    // Delegated properties must call the getter to unwrap the delegate
                    // When inside a lambda, use getComponentMRef() to get the captured component reference
                    if (parent.isInComponentContext && property != null && backingField != null) {
                        val parentClass = function.parent as? IrClass
                        if (parentClass != null && context.intrinsics.isSceneGraphComponent(parentClass)) {
                            val componentM = parent.getComponentMRef()

                            // Delegated properties must call the getter to unwrap the delegate
                            if (property.isDelegated) {
                                return BrsMethodCall(componentM, "__get_${fieldName}_k_", mutableListOf())
                            }

                            return if (hasInterfaceFieldAnnotation(property)) {
                                // Interface field - access via m.top
                                BrsDotAccess(BrsDotAccess(componentM, "top"), fieldName)
                            } else {
                                // Internal state - access via m
                                // Layout properties are stored with underscore prefix (see layout initialization code)
                                val actualFieldName = if (parent.isLayoutClassProperty(property, parentClass)) {
                                    "_$fieldName"
                                } else {
                                    fieldName
                                }
                                BrsDotAccess(componentM, actualFieldName)
                            }
                        }
                    }

                    // Determine if we should use direct field access or call the getter method
                    // Use direct access ONLY if:
                    // 1. There is a backing field
                    // 2. The getter is not overridden (interface implementation)
                    // 3. The parent class is a data class (simple properties)
                    val isOverridden = function.overriddenSymbols.isNotEmpty()
                    val parentClass = function.parent as? IrClass
                    val isDataClass = parentClass?.isData == true
                    val hasSimpleBackingField = backingField != null && !isOverridden && isDataClass

                    return if (hasSimpleBackingField) {
                        // Direct field access for simple backing field properties
                        BrsDotAccess(receiverExpr, fieldName)
                    } else {
                        // Call getter for computed properties, overridden properties, etc.
                        // SceneGraph component property accessors use mangled names (with _k_ suffix)
                        // Regular class property accessors use simple names (no suffix)
                        val isComponentProperty = parentClass != null && context.intrinsics.isSceneGraphComponent(parentClass)
                        val getterName = if (isComponentProperty) "__get_${fieldName}_k_" else "__get_$fieldName"
                        BrsMethodCall(receiverExpr, getterName, mutableListOf())
                    }
                }

                // Handle property setters
                if (rawName.startsWith("<set-") && rawName.endsWith(">")) {
                    val fieldName = rawName.removePrefix("<set-").removeSuffix(">")
                    val property = function.correspondingPropertySymbol?.owner
                    val backingField = property?.backingField
                    val value = expression.getValueArgument(0)?.accept(this, data) ?: BrsInvalidLiteral()

                    // Handle user-defined properties in component context
                    // Interface fields (with @SGField or @BrsField) compile to m.top.fieldName = value
                    // Internal state (no annotation) compiles to m.fieldName = value
                    // Delegated properties must call the setter
                    // When inside a lambda, use getComponentMRef() to get the captured component reference
                    if (parent.isInComponentContext && property != null && backingField != null) {
                        val parentClass = function.parent as? IrClass
                        if (parentClass != null && context.intrinsics.isSceneGraphComponent(parentClass)) {
                            val componentM = parent.getComponentMRef()

                            // Delegated properties must call the setter
                            if (property.isDelegated) {
                                return BrsMethodCall(componentM, "__set_${fieldName}_k_", mutableListOf(value))
                            }

                            val target = if (hasInterfaceFieldAnnotation(property)) {
                                // Interface field - access via m.top
                                BrsDotAccess(BrsDotAccess(componentM, "top"), fieldName)
                            } else {
                                // Internal state - access via m
                                // Layout properties are stored with underscore prefix (see layout initialization code)
                                val actualFieldName = if (parent.isLayoutClassProperty(property, parentClass)) {
                                    "_$fieldName"
                                } else {
                                    fieldName
                                }
                                BrsDotAccess(componentM, actualFieldName)
                            }
                            return BrsBinaryOp(target, BrsBinaryOperator.EQ, value)
                        }
                    }

                    // Mirror the getter logic: use direct field assignment for data classes
                    // with simple backing fields, call setter method otherwise
                    val isOverridden = function.overriddenSymbols.isNotEmpty()
                    val parentClass = function.parent as? IrClass
                    val isDataClass = parentClass?.isData == true
                    val hasSimpleBackingField = backingField != null && !isOverridden && isDataClass

                    return if (hasSimpleBackingField) {
                        // Direct field assignment for simple backing field properties
                        BrsBinaryOp(
                            BrsDotAccess(receiverExpr, fieldName),
                            BrsBinaryOperator.EQ,
                            value
                        )
                    } else {
                        // Call setter for computed properties, overridden properties, etc.
                        // SceneGraph component property accessors use mangled names (with _k_ suffix)
                        // Regular class property accessors use simple names (no suffix)
                        val isComponentProperty = parentClass != null && context.intrinsics.isSceneGraphComponent(parentClass)
                        val setterName = if (isComponentProperty) "__set_${fieldName}_k_" else "__set_$fieldName"
                        BrsMethodCall(receiverExpr, setterName, mutableListOf(value))
                    }
                }

                // For regular method calls, determine the method name
                val rawMethodName = function.name.asString()

                // Check if the method belongs to an external interface (native BrightScript type)
                // External interface methods use their simple names without mangling
                // Note: parentClass is already defined above at line 4101
                val isExternalInterfaceMethod = parentClass != null &&
                    (parentClass.isExternal || isExternalClass(parentClass))

                // Data class synthetic methods (componentN, copy, equals, hashCode, toString)
                // are attached with simple names, so we should not mangle them
                val isDataClassSyntheticMethod = rawMethodName.startsWith("component") ||
                    rawMethodName in listOf("copy", "equals", "hashCode", "toString")

                val methodName = if (isExternalInterfaceMethod || isDataClassSyntheticMethod) {
                    // Use simple name for external interface methods and data class synthetic methods
                    rawMethodName
                } else {
                    // Use mangled name for other methods (to match how methods are attached)
                    val fullMethodName = context.getBrsName(function)
                    val className = parentClass?.let { context.getBrsName(it) } ?: ""
                    if (className.isNotEmpty()) {
                        fullMethodName.removePrefix("${className}_")
                    } else {
                        fullMethodName
                    }
                }
                val args = mutableListOf<BrsExpression>()
                for (i in 0 until expression.valueArgumentsCount) {
                    val arg = expression.getValueArgument(i)
                    if (arg != null) {
                        args.add(arg.accept(this, data))
                    } else {
                        // Parameter uses default value - add invalid to preserve position
                        args.add(BrsInvalidLiteral())
                    }
                }
                return BrsMethodCall(receiverExpr, methodName, args)
            }
            arguments.add(receiverExpr)
        }

        // Add extension receiver if present
        expression.extensionReceiver?.let { receiver ->
            val receiverType = receiver.type
            // Check if extension receiver is a primitive type
            val isPrimitiveExtension = receiverType.isInt() || receiverType.isLong() ||
                receiverType.isFloat() || receiverType.isDouble() ||
                receiverType.isShort() || receiverType.isByte() ||
                receiverType.isBoolean() || receiverType.isChar()

            if (isPrimitiveExtension) {
                // For extension functions on primitives, we need to fix the function name.
                // When functions are loaded from klib, the parent might be the class (e.g., Int)
                // instead of the package, causing getBrsName to prepend "Int_" to the name.
                // The correct format is: functionName_rReceiverType_ParamTypes_k_
                // Note: Return types are NOT included (like Java) to support polymorphism

                // Check if functionName starts with a primitive class prefix that needs stripping
                val primitiveClassPrefixes = listOf("Int_", "Long_", "Float_", "Double_", "Short_", "Byte_", "Boolean_", "Char_")
                val hasWrongClassPrefix = primitiveClassPrefixes.any { functionName.startsWith(it) }

                val correctedName = if (hasWrongClassPrefix) {
                    // Strip the class prefix and rebuild the correct name
                    val rawName = function.name.asString()
                    val signatureParts = mutableListOf<String>()

                    // Add receiver type with 'r' prefix - use the expression's receiver type
                    signatureParts.add("r" + context.typeToMangledString(receiverType))

                    // Add parameter types
                    function.valueParameters.forEach { param ->
                        signatureParts.add(context.typeToMangledString(param.type))
                    }

                    // Build the full name (no return type)
                    val signature = signatureParts.joinToString("_")
                    "${rawName}_${signature}_k_"
                } else {
                    // Name is already correct (e.g., top-level extension function)
                    functionName
                }

                val args = mutableListOf<BrsExpression>()
                args.add(receiver.accept(this, data))
                for (i in 0 until expression.valueArgumentsCount) {
                    val arg = expression.getValueArgument(i)
                    if (arg != null) {
                        args.add(arg.accept(this, data))
                    } else {
                        // Parameter uses default value - add invalid to preserve position
                        args.add(BrsInvalidLiteral())
                    }
                }
                return BrsFunctionCall(BrsIdentifier(correctedName), args)
            }

            arguments.add(receiver.accept(this, data))
        }

        // Add value arguments, using invalid for default parameters to preserve positional alignment
        for (i in 0 until expression.valueArgumentsCount) {
            val arg = expression.getValueArgument(i)
            if (arg != null) {
                arguments.add(arg.accept(this, data))
            } else {
                // Parameter uses default value - add invalid to preserve position
                arguments.add(BrsInvalidLiteral())
            }
        }

        // Handle companion object/singleton function calls without a dispatch receiver.
        // When calling a method on a companion object statically (e.g., UInt.MIN_VALUE),
        // the IR doesn't provide a dispatch receiver, but the generated getter function
        // expects `m` to be the companion instance. We need to call the function as a method
        // on the singleton instance.
        val parentClass = function.parent as? IrClass
        if (expression.dispatchReceiver == null && parentClass?.kind == ClassKind.OBJECT) {
            // This is a call on a singleton/companion object without a receiver.
            // Call it as a method on the singleton instance.
            val singletonName = context.getBrsName(parentClass)
            context.recordFunctionDependency("${singletonName}_getInstance")
            val singletonInstance = BrsFunctionCall(BrsIdentifier("${singletonName}_getInstance"), mutableListOf())

            // Property accessors are attached with simple names (__get_X, __set_X)
            // Regular methods are attached with mangled names
            val rawFuncName = function.name.asString()
            val methodName = if (rawFuncName.startsWith("<get-") && rawFuncName.endsWith(">")) {
                "__get_" + rawFuncName.removePrefix("<get-").removeSuffix(">")
            } else if (rawFuncName.startsWith("<set-") && rawFuncName.endsWith(">")) {
                "__set_" + rawFuncName.removePrefix("<set-").removeSuffix(">")
            } else {
                // Use mangled name for regular methods
                val fullMethodName = context.getBrsName(function)
                fullMethodName.removePrefix("${singletonName}_")
            }

            return BrsMethodCall(singletonInstance, methodName, arguments)
        }

        return BrsFunctionCall(BrsIdentifier(functionName), arguments)
    }

    /**
     * Transform a call to a @BrsInline function by inlining the parsed BrightScript code.
     */
    private fun transformBrsInlineCall(expression: IrCall): BrsExpression {
        val info = parent.inlineCallTransformer.getInlineInfo(expression)
            ?: return BrsInvalidLiteral()

        // Build argument map: parameter name -> BrsExpression
        val function = expression.symbol.owner
        val arguments = mutableMapOf<String, BrsExpression>()
        function.valueParameters.forEachIndexed { index, param ->
            expression.getValueArgument(index)?.let { arg ->
                arguments[param.name.asString()] = arg.accept(this, Unit)
            }
        }

        // Transform inline code with substitutions
        val statements = parent.inlineCallTransformer.transformInlineCode(info, arguments)

        // Extract expression from statements (most @BrsInline is "return expr")
        return when {
            statements.size == 1 && statements[0] is BrsReturn ->
                (statements[0] as BrsReturn).value ?: BrsInvalidLiteral()
            statements.size == 1 && statements[0] is BrsExpressionStatement ->
                (statements[0] as BrsExpressionStatement).expression
            else -> {
                val lastStmt = statements.lastOrNull()
                if (lastStmt is BrsReturn) lastStmt.value ?: BrsInvalidLiteral()
                else BrsInvalidLiteral()
            }
        }
    }

    private fun transformIntrinsic(expression: IrCall): BrsExpression {
        val function = expression.symbol.owner
        val name = function.name.asString()

        // Check if this is a stdlib intrinsic
        if (context.intrinsics.isStdlibIntrinsic(expression.symbol)) {
            val intrinsicName = context.intrinsics.getIntrinsicName(expression.symbol)
            return transformStdlibIntrinsic(expression, intrinsicName)
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
            "brs" -> {
                // Handle inline BrightScript code: brs("code here")
                val codeArg = expression.getValueArgument(0)
                val codeString = foldBrsCodeString(codeArg)
                if (codeString == null) {
                    context.reportError(expression, "brs() argument must be a compile-time constant string")
                    return BrsInvalidLiteral()
                }

                // Parse the BrightScript code
                val parseResult = parseBrightScriptStatements(codeString)
                if (parseResult.hasErrors) {
                    for (error in parseResult.errors) {
                        context.reportError(expression, "Error in brs() code: $error")
                    }
                    return BrsInvalidLiteral()
                }

                // Extract expression from parsed statements.
                // Contract: exactly one statement or expression. See
                // compiler/ir/backend.brightscript/docs/brs-intrinsic.md.
                val statements = parseResult.result
                when {
                    statements.isEmpty() -> {
                        context.reportError(
                            expression,
                            "brs() argument parsed to no statements; expected a single expression or statement"
                        )
                        BrsInvalidLiteral()
                    }
                    statements.size == 1 && statements[0] is BrsReturn ->
                        (statements[0] as BrsReturn).value ?: BrsInvalidLiteral()
                    statements.size == 1 && statements[0] is BrsExpressionStatement ->
                        (statements[0] as BrsExpressionStatement).expression
                    statements.size == 1 -> BrsInvalidLiteral()
                    else -> {
                        context.reportError(
                            expression,
                            "brs() must contain exactly one expression or statement; got ${statements.size}. " +
                                "Split into multiple brs() calls instead."
                        )
                        BrsInvalidLiteral()
                    }
                }
            }
            else -> BrsFunctionCall(BrsIdentifier(name), mutableListOf())
        }
    }

    /**
     * Constant-fold a string expression for brs() inline code.
     * Returns null if the expression is not a compile-time constant string.
     *
     * `const val` references are followed to their initializer so that
     * `brs("print ${CONST}")` folds identically to `brs("print literal")`.
     * This matches the FIR checker's `canBeEvaluatedAtCompileTime` acceptance.
     */
    private fun foldBrsCodeString(expression: IrExpression?): String? {
        if (expression == null) return null
        return when (expression) {
            is IrConst -> {
                if (expression.kind == IrConstKind.String) expression.value as String
                else null
            }
            is IrStringConcatenation -> {
                // Support string templates like brs("print ${someConstant}")
                val builder = StringBuilder()
                for (arg in expression.arguments) {
                    val part = foldBrsCodeString(arg) ?: return null
                    builder.append(part)
                }
                builder.toString()
            }
            is IrGetValue -> {
                val owner = expression.symbol.owner
                if (owner is IrVariable && owner.isConst) {
                    foldBrsCodeString(owner.initializer)
                } else null
            }
            is IrGetField -> {
                // Mirrors visitGetField's inline-at-visit logic: a final field
                // with a compile-time-constant initializer can be spliced at fold time.
                val field = expression.symbol.owner
                if (field.isFinal) {
                    foldBrsCodeString(field.initializer?.expression)
                } else null
            }
            is IrCall -> {
                // Property getter for a `const val`. `property.isConst` is the
                // Kotlin source-level guarantee that this is a const, not a
                // user-written getter with a body.
                val getter = expression.symbol.owner
                val property = getter.correspondingPropertySymbol?.owner
                if (property?.isConst == true) {
                    foldBrsCodeString(property.backingField?.initializer?.expression)
                } else null
            }
            else -> null
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

            is BrsIntrinsics.StdlibIntrinsic.ToString -> {
                // Type-aware toString for Any? values
                // Generate runtime type checking to handle primitives properly
                // Record dependency on toString_AnyN_k_ via function manifest
                context.recordFunctionDependency("toString_AnyN_k_")
                if (args.isNotEmpty()) {
                    parent.generateRuntimeToString(args[0])
                } else {
                    BrsStringLiteral("null")
                }
            }

            is BrsIntrinsics.StdlibIntrinsic.IsAssociativeArray -> {
                // Type(a) = "roAssociativeArray"
                if (args.isNotEmpty()) {
                    BrsBinaryOp(
                        BrsFunctionCall(BrsIdentifier("Type"), mutableListOf(args[0])),
                        BrsBinaryOperator.EQ,
                        BrsStringLiteral("roAssociativeArray")
                    )
                } else {
                    BrsBooleanLiteral(false)
                }
            }

            is BrsIntrinsics.StdlibIntrinsic.IsSGNode -> {
                // Type(a) = "roSGNode"
                // roSGNode cannot be compared with = operator - causes Type Mismatch error
                if (args.isNotEmpty()) {
                    BrsBinaryOp(
                        BrsFunctionCall(BrsIdentifier("Type"), mutableListOf(args[0])),
                        BrsBinaryOperator.EQ,
                        BrsStringLiteral("roSGNode")
                    )
                } else {
                    BrsBooleanLiteral(false)
                }
            }

            is BrsIntrinsics.StdlibIntrinsic.CallEquals -> {
                // a.equals(b) - direct method call on roAssociativeArray
                if (args.size >= 2) {
                    BrsMethodCall(args[0], "equals", mutableListOf(args[1]))
                } else {
                    BrsBooleanLiteral(false)
                }
            }

            is BrsIntrinsics.StdlibIntrinsic.NativeEquals -> {
                // a = b - native BrightScript comparison
                if (args.size >= 2) {
                    BrsBinaryOp(args[0], BrsBinaryOperator.EQ, args[1])
                } else {
                    BrsBooleanLiteral(false)
                }
            }

            is BrsIntrinsics.StdlibIntrinsic.NativeCompare -> {
                // Native comparison returning -1, 0, or 1
                // Generates: if a < b then -1 else if a > b then 1 else 0
                if (args.size >= 2) {
                    BrsConditional(
                        BrsBinaryOp(args[0], BrsBinaryOperator.LT, args[1]),
                        BrsIntLiteral(-1),
                        BrsConditional(
                            BrsBinaryOp(args[0], BrsBinaryOperator.GT, args[1]),
                            BrsIntLiteral(1),
                            BrsIntLiteral(0)
                        )
                    )
                } else {
                    BrsIntLiteral(0)
                }
            }

            is BrsIntrinsics.StdlibIntrinsic.CallCompareTo -> {
                // a.compareTo(b) - direct method call on roAssociativeArray
                if (args.size >= 2) {
                    BrsMethodCall(args[0], "compareTo", mutableListOf(args[1]))
                } else {
                    BrsIntLiteral(0)
                }
            }

            is BrsIntrinsics.StdlibIntrinsic.CallComparator -> {
                // comparator.compare_AnyN_AnyN_k_(a, b) - call Comparator's compare method directly
                if (args.size >= 3) {
                    BrsMethodCall(args[0], "compare_AnyN_AnyN_k_", mutableListOf(args[1], args[2]))
                } else {
                    BrsIntLiteral(0)
                }
            }

            is BrsIntrinsics.StdlibIntrinsic.Instr -> {
                // Instr(start, source, substring) with index conversion
                // BrightScript Instr: 1-based start, returns 0 if not found, 1-based position otherwise
                // Kotlin indexOf: 0-based start, returns -1 if not found, 0-based position otherwise
                // Generate: if Instr(start+1, source, substring) = 0 then -1 else Instr(start+1, source, substring) - 1
                if (args.size >= 3) {
                    val start = args[0]  // 0-based
                    val source = args[1]
                    val substring = args[2]
                    val startPlus1 = BrsBinaryOp(start, BrsBinaryOperator.ADD, BrsIntLiteral(1))
                    val instrCall = BrsFunctionCall(BrsIdentifier("Instr"), mutableListOf(startPlus1, source, substring))
                    BrsConditional(
                        BrsBinaryOp(instrCall.deepCopy(), BrsBinaryOperator.EQ, BrsIntLiteral(0)),
                        BrsIntLiteral(-1),
                        BrsBinaryOp(instrCall, BrsBinaryOperator.SUB, BrsIntLiteral(1))
                    )
                } else {
                    BrsInvalidLiteral()
                }
            }

            is BrsIntrinsics.StdlibIntrinsic.GetLength -> {
                // Call __get_length() directly on an object: obj.__get_length()
                // This is used by __kotlin_charSequenceLength to avoid recursion
                if (args.isNotEmpty()) {
                    BrsFunctionCall(
                        BrsDotAccess(args[0], "__get_length"),
                        mutableListOf()
                    )
                } else {
                    BrsInvalidLiteral()
                }
            }

            is BrsIntrinsics.StdlibIntrinsic.FunctionName -> {
                // Extract the mangled BrightScript name from a function reference
                // brsName(::myFunction) -> "ClassName_myFunction_k_"
                //
                // IMPORTANT: Always return the FULL mangled name (with class prefix).
                // Roku's observeFieldScoped looks for callback functions at module/script level,
                // NOT in m scope. The function exists at module level with its full name:
                //   sub TestLayout_onButtonPressed_RoSGNodeEvent_k_(message as Object)
                // So the observer must use that full name for Roku to find it.
                val arg = expression.getValueArgument(0)
                when (arg) {
                    is IrFunctionReference -> {
                        val function = arg.symbol.owner
                        val mangledName = context.getBrsName(function)
                        BrsStringLiteral(mangledName)
                    }
                    else -> {
                        context.reportError(
                            expression,
                            "brsName() requires a function reference (::functionName), got ${arg?.javaClass?.simpleName}"
                        )
                        BrsStringLiteral("invalid_function_reference")
                    }
                }
            }

            is BrsIntrinsics.StdlibIntrinsic.UpTime -> {
                // UpTime(dummy) - requires an integer argument per BrightScript docs
                BrsFunctionCall(BrsIdentifier("UpTime"), mutableListOf(BrsIntLiteral(0)))
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
                    val (leftIr, rightIr) = when {
                        innerCall.dispatchReceiver != null -> {
                            Pair(innerCall.dispatchReceiver!!, innerCall.getValueArgument(0)!!)
                        }
                        innerCall.extensionReceiver != null -> {
                            Pair(innerCall.extensionReceiver!!, innerCall.getValueArgument(0)!!)
                        }
                        else -> {
                            Pair(innerCall.getValueArgument(0)!!, innerCall.getValueArgument(1)!!)
                        }
                    }

                    // Check if we need structural comparison for non-primitive types
                    // Exclude null comparisons - BrightScript's = and <> work correctly with invalid
                    val leftType = leftIr.type
                    val rightType = rightIr.type
                    val isNullComparison = (leftIr is IrConst && (leftIr as IrConst).value == null) ||
                                          (rightIr is IrConst && (rightIr as IrConst).value == null)
                    val needsStructuralEquals = !isNullComparison &&
                        (!leftType.isPrimitiveForComparison() || !rightType.isPrimitiveForComparison())

                    val left = leftIr.accept(this, Unit)
                    val right = rightIr.accept(this, Unit)

                    return if (needsStructuralEquals) {
                        // Use NOT brsStructuralEquals() for non-primitive types
                        context.recordFunctionDependency("brsStructuralEquals_AnyN_AnyN_k_")
                        val structuralCall = BrsFunctionCall(
                            BrsIdentifier("brsStructuralEquals_AnyN_AnyN_k_"),
                            mutableListOf(left, right)
                        )
                        BrsUnaryOp(BrsUnaryOperator.NOT, structuralCall)
                    } else {
                        BrsBinaryOp(left, BrsBinaryOperator.NE, right)
                    }
                }
                // Fallback for non-call receivers
                val operand = dispatchReceiver.accept(this, Unit)
                return BrsUnaryOp(BrsUnaryOperator.NOT, operand)
            }
        }

        // Special case: EXCLEQEQ on Boolean.not() call - this means `!==` was lowered to `(a === b).not()`
        // Extract operands from inner call and generate NOT __kotlin_identityEquals(a, b)
        if (origin == IrStatementOrigin.EXCLEQEQ && function.name.asString() == "not") {
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
                    // Generate NOT __kotlin_identityEquals(a, b)
                    context.recordFunctionDependency("__kotlin_identityEquals")
                    val identityCall = BrsFunctionCall(
                        BrsIdentifier("__kotlin_identityEquals"),
                        mutableListOf(left, right)
                    )
                    return BrsUnaryOp(BrsUnaryOperator.NOT, identityCall)
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

            // ==================== Handle compareTo with comparison origin ====================
            // When FIR lowers `a <= b` for types implementing Comparable (like Char), it generates:
            //   a.compareTo(b) with origin LTEQ
            // This gets wrapped in an outer `<= 0` comparison.
            // We must NOT convert this to a native comparison, or we'll get `(a <= b) <= 0`.
            // Instead, let it fall through to method call handling, which will generate
            // an integer comparison that the outer `<= 0` can properly use.
            if (function.name.asString() == "compareTo" &&
                (binaryOp == BrsBinaryOperator.LT || binaryOp == BrsBinaryOperator.GT ||
                 binaryOp == BrsBinaryOperator.LE || binaryOp == BrsBinaryOperator.GE)) {
                // Skip binary operator handling for compareTo - let it be a method call
                // that returns an Int for the outer comparison
                return null
            }

            // ==================== Enum Comparison Optimization ====================
            // When comparing enum values of the same type, use ordinal comparison for performance.
            // This avoids object comparison overhead and is semantically equivalent since
            // enum entries are singletons with unique ordinals.
            if (binaryOp == BrsBinaryOperator.EQ || binaryOp == BrsBinaryOperator.NE) {
                val (leftIr, rightIr) = when {
                    expression.dispatchReceiver != null -> Pair(expression.dispatchReceiver!!, expression.getValueArgument(0))
                    expression.extensionReceiver != null -> Pair(expression.extensionReceiver!!, expression.getValueArgument(0))
                    else -> Pair(expression.getValueArgument(0), expression.getValueArgument(1))
                }

                if (leftIr != null && rightIr != null) {
                    val leftType = leftIr.type
                    val rightType = rightIr.type

                    // Check if both are the same enum type (non-nullable)
                    val leftClass = leftType.classifierOrNull?.owner as? IrClass
                    val rightClass = rightType.classifierOrNull?.owner as? IrClass

                    if (leftClass != null && rightClass != null &&
                        leftClass == rightClass &&
                        leftClass.kind == ClassKind.ENUM_CLASS &&
                        !leftType.isNullable() && !rightType.isNullable()) {

                        // At least one side should be a compile-time constant for optimization benefit
                        val leftIsConst = leftIr is IrGetEnumValue
                        val rightIsConst = rightIr is IrGetEnumValue

                        if (leftIsConst || rightIsConst) {
                            // Transform to ordinal comparison
                            val leftOrdinal = if (leftIsConst) {
                                val entry = (leftIr as IrGetEnumValue).symbol.owner
                                context.getEnumOrdinal(entry)?.let { BrsIntLiteral(it) }
                            } else {
                                // Access .ordinal on the dynamic enum value
                                BrsDotAccess(leftIr.accept(this, Unit), "ordinal")
                            }

                            val rightOrdinal = if (rightIsConst) {
                                val entry = (rightIr as IrGetEnumValue).symbol.owner
                                context.getEnumOrdinal(entry)?.let { BrsIntLiteral(it) }
                            } else {
                                // Access .ordinal on the dynamic enum value
                                BrsDotAccess(rightIr.accept(this, Unit), "ordinal")
                            }

                            if (leftOrdinal != null && rightOrdinal != null) {
                                return BrsBinaryOp(leftOrdinal, binaryOp, rightOrdinal)
                            }
                        }
                    }
                }
            }
            // ==================== End Enum Comparison Optimization ====================

            val (leftIr, rightIr) = when {
                expression.dispatchReceiver != null -> {
                    Pair(expression.dispatchReceiver!!, expression.getValueArgument(0)!!)
                }
                expression.extensionReceiver != null -> {
                    Pair(expression.extensionReceiver!!, expression.getValueArgument(0)!!)
                }
                else -> {
                    Pair(expression.getValueArgument(0)!!, expression.getValueArgument(1)!!)
                }
            }

            // ==================== Structural Equals for Non-Primitive Types ====================
            // BrightScript's = and <> operators can't compare roAssociativeArray objects (Kotlin classes).
            // For non-primitive types (generics, Any, class instances), use brsStructuralEquals.
            // Exclude null comparisons - BrightScript's = and <> work correctly with invalid.
            if (binaryOp == BrsBinaryOperator.EQ || binaryOp == BrsBinaryOperator.NE) {
                val leftType = leftIr.type
                val rightType = rightIr.type

                // Check if this is a null comparison - these don't need structural equals
                val isNullComparison = (leftIr is IrConst && (leftIr as IrConst).value == null) ||
                                       (rightIr is IrConst && (rightIr as IrConst).value == null)

                // Check if either operand is a non-primitive type that needs structural comparison
                val needsStructuralEquals = !isNullComparison &&
                    (!leftType.isPrimitiveForComparison() || !rightType.isPrimitiveForComparison())

                if (needsStructuralEquals) {
                    val left = leftIr.accept(this, Unit)
                    val right = rightIr.accept(this, Unit)
                    context.recordFunctionDependency("brsStructuralEquals_AnyN_AnyN_k_")
                    val structuralCall = BrsFunctionCall(
                        BrsIdentifier("brsStructuralEquals_AnyN_AnyN_k_"),
                        mutableListOf(left, right)
                    )
                    return if (binaryOp == BrsBinaryOperator.NE) {
                        BrsUnaryOp(BrsUnaryOperator.NOT, structuralCall)
                    } else {
                        structuralCall
                    }
                }
            }
            // ==================== End Structural Equals ====================

            // ==================== Comparable Comparison for Non-Primitive Types ====================
            // BrightScript's <, >, <=, >= operators can't compare roAssociativeArray objects.
            // For non-primitive types implementing Comparable, call compareTo and compare with 0.
            //
            // When Kotlin FIR lowers `a > b` for Comparable types, it creates:
            //   IrCall(origin=GT, function=compareTo, receiver=a, arg=b)
            // The function being called is `compareTo`, and the origin indicates the comparison intent.
            // The outer FIR lowering wraps this with `> 0` comparison.
            //
            // So when we see a compareTo call with GT/LT origin:
            // - We should generate just the method call, NOT add `> 0` (the outer wrapper does that)
            // When we see a non-compareTo function with GT/LT origin on non-primitives:
            // - We should generate `a.compareTo(b) > 0`
            if (binaryOp == BrsBinaryOperator.LT || binaryOp == BrsBinaryOperator.GT ||
                binaryOp == BrsBinaryOperator.LE || binaryOp == BrsBinaryOperator.GE) {
                val leftType = leftIr.type
                val rightType = rightIr.type

                val needsCompareTo = !leftType.isPrimitiveForComparison() || !rightType.isPrimitiveForComparison()

                if (needsCompareTo) {
                    val left = leftIr.accept(this, Unit)
                    val right = rightIr.accept(this, Unit)

                    // Find the compareTo method in the left operand's class that matches the right operand type
                    val leftClass = leftType.classifierOrNull?.owner as? IrClass
                    val rightClassifier = rightType.classifierOrNull
                    val compareToMethod = leftClass?.declarations?.filterIsInstance<IrSimpleFunction>()
                        ?.find { method ->
                            method.name.asString() == "compareTo" &&
                            method.valueParameters.size == 1 &&
                            // Match the parameter type with the right operand type
                            method.valueParameters[0].type.classifierOrNull == rightClassifier
                        }
                        // If no exact match, fall back to the first compareTo with the same receiver type
                        ?: leftClass?.declarations?.filterIsInstance<IrSimpleFunction>()
                            ?.find { method ->
                                method.name.asString() == "compareTo" &&
                                method.valueParameters.size == 1 &&
                                method.valueParameters[0].type.classifierOrNull == leftType.classifierOrNull
                            }

                    val methodName = if (compareToMethod != null) {
                        val fullMethodName = context.getBrsName(compareToMethod)
                        val className = leftClass?.let { context.getBrsName(it) } ?: ""
                        if (className.isNotEmpty()) {
                            fullMethodName.removePrefix("${className}_")
                        } else {
                            fullMethodName
                        }
                    } else {
                        // Fallback: try generic compareTo (won't be mangled correctly)
                        "compareTo"
                    }

                    val compareToCall = BrsMethodCall(left, methodName, mutableListOf(right))

                    // Check if this call is already to compareTo - if so, the outer wrapper adds > 0
                    // If not (e.g., operator overloading on non-Comparable), we need to add > 0 ourselves
                    val isAlreadyCompareTo = function.name.asString() == "compareTo"
                    return if (isAlreadyCompareTo) {
                        // Just return the compareTo call - outer wrapper adds > 0
                        compareToCall
                    } else {
                        // Non-compareTo function on non-primitives - add comparison
                        BrsBinaryOp(compareToCall, binaryOp, BrsIntLiteral(0))
                    }
                }
            }
            // ==================== End Comparable Comparison ====================

            // ==================== Arithmetic Operators for Non-Primitive Types ====================
            // BrightScript's +, -, *, /, MOD operators can't operate on roAssociativeArray objects.
            // For non-primitive types (like UInt, ULong), fall back to method calls (plus, minus, times, div, rem).
            if (binaryOp == BrsBinaryOperator.ADD || binaryOp == BrsBinaryOperator.SUB ||
                binaryOp == BrsBinaryOperator.MUL || binaryOp == BrsBinaryOperator.DIV ||
                binaryOp == BrsBinaryOperator.MOD) {
                // Use the function's declared types instead of the expression's operand types.
                // This is important for shared variables (boxed in {value: x}) where the operand
                // may have anyNType but the function's declared receiver type preserves the original type.
                val functionReceiverType = function.dispatchReceiverParameter?.type
                    ?: function.extensionReceiverParameter?.type
                val functionArgType = function.valueParameters.firstOrNull()?.type

                val leftType = functionReceiverType ?: leftIr.type
                val rightType = functionArgType ?: rightIr.type

                // If either operand is non-primitive, let normal method call handling take over
                if (!leftType.isPrimitiveForArithmetic() || !rightType.isPrimitiveForArithmetic()) {
                    return null  // Fall through to method call handling
                }
            }
            // ==================== End Arithmetic Operators ====================

            var left = leftIr.accept(this, Unit)
            var right = rightIr.accept(this, Unit)

            // For PLUS operations involving strings, convert non-string operands to strings
            // BrightScript's + operator cannot mix Integer/Float with String
            if (binaryOp == BrsBinaryOperator.ADD) {
                val leftIsString = leftIr.type.isString()
                val rightIsString = rightIr.type.isString()

                if (leftIsString && !rightIsString) {
                    // Right operand needs string conversion
                    right = parent.transformToString(right, rightIr.type)
                } else if (!leftIsString && rightIsString) {
                    // Left operand needs string conversion
                    left = parent.transformToString(left, leftIr.type)
                }
            }

            return BrsBinaryOp(left, binaryOp, right)
        }

        // Identity operators (=== and !==)
        // These need special handling because BrightScript's = operator doesn't work for associative arrays
        // Check both origin (for user code) and symbol (for synthesized calls from coroutine lowering)
        val isIdentityOp = origin == IrStatementOrigin.EQEQEQ ||
                           origin == IrStatementOrigin.EXCLEQEQ ||
                           expression.symbol == context.irBuiltIns.eqeqeqSymbol
        if (isIdentityOp) {
            // Determine if this is === or !== based on origin (symbol is always eqeqeqSymbol)
            val isNegated = origin == IrStatementOrigin.EXCLEQEQ
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
            context.recordFunctionDependency("__kotlin_identityEquals")
            val identityCall = BrsFunctionCall(
                BrsIdentifier("__kotlin_identityEquals"),
                mutableListOf(left, right)
            )
            return if (isNegated) {
                BrsUnaryOp(BrsUnaryOperator.NOT, identityCall)
            } else {
                identityCall
            }
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
                // Only use BrsIndexAccess for native Kotlin arrays.
                // For other types (like ArrayList), fall through to method call handling.
                val receiver = expression.dispatchReceiver ?: return null
                if (!receiver.type.isArray()) {
                    // Not a native array - return null to let the normal method call handling take over
                    return null
                }
                val array = receiver.accept(this, Unit)
                val index = expression.getValueArgument(0)?.let { it.accept(this, Unit) }
                    ?: return null
                BrsIndexAccess(array, index)
            }
            else -> null
        }
    }

    /**
     * Transform builtin comparison functions from irBuiltIns to binary operators.
     * These functions (less, lessOrEqual, greater, greaterOrEqual) may not have an
     * IrStatementOrigin when introduced by lowering passes, so we check the function
     * name directly.
     */
    private fun transformBuiltinComparison(expression: IrCall): BrsExpression? {
        val function = expression.symbol.owner
        val functionName = function.name.asString()

        // Check if this is a builtin comparison function
        val binaryOp = when (functionName) {
            "less" -> BrsBinaryOperator.LT
            "lessOrEqual" -> BrsBinaryOperator.LE
            "greater" -> BrsBinaryOperator.GT
            "greaterOrEqual" -> BrsBinaryOperator.GE
            else -> return null
        }

        // Verify this is from kotlin.internal (builtins) not a user-defined function
        val packageFqName = function.getPackageFragment().packageFqName.asString()
        if (packageFqName.startsWith("kotlin.internal") || packageFqName.startsWith("kotlin")) {
            // This is a builtin comparison function
        } else {
            return null
        }

        // Extract left and right operands
        val (left, right) = when {
            expression.valueArgumentsCount >= 2 -> {
                val l = expression.getValueArgument(0)?.accept(this, Unit) ?: return null
                val r = expression.getValueArgument(1)?.accept(this, Unit) ?: return null
                Pair(l, r)
            }
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
            else -> return null
        }

        return BrsBinaryOp(left, binaryOp, right)
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
        // For local class constructors, check if arguments are for captured variables
        // If a captured variable is a shared (boxed) variable, pass the box, not .value
        arguments.addAll((0 until expression.valueArgumentsCount).mapNotNull { i ->
            val arg = expression.getValueArgument(i) ?: return@mapNotNull null

            // Check if this argument is for a bound (captured) value parameter
            val param = constructor.valueParameters.getOrNull(i)
            val isCapturedValueParam = param?.origin == BOUND_VALUE_PARAMETER ||
                                        param?.origin == BOUND_RECEIVER_PARAMETER

            // If it's a captured parameter and the argument is a GetValue for a shared variable,
            // pass the box directly instead of dereferencing with .value
            // Two detection mechanisms:
            // 1. Via SharedVariablesLowering which sets SHARED_VARIABLE_WRAPPER origin
            // 2. Via BrsSharedVariableDetectionLowering which populates sharedVariables set
            val isSharedVar = arg is IrGetValue && (
                (arg.symbol.owner is IrVariable && (arg.symbol.owner as IrVariable).origin == BrsDeclarationOrigin.SHARED_VARIABLE_WRAPPER) ||
                arg.symbol in parent.sharedVariables
            )
            if (isCapturedValueParam && isSharedVar) {
                // Pass the box itself, not the dereferenced value
                val varName = sanitizeParameterName((arg as IrGetValue).symbol.owner.name.asString())
                BrsIdentifier(varName)
            } else if (arg is IrGetField) {
                // Check if this field holds a shared variable box (e.g., in coroutine create method)
                // When passing captured fields to a coroutine constructor, we want the box, not .value
                val field = arg.symbol.owner
                val fieldParentClass = field.parent as? IrClass
                val rawFieldName = field.name.asString()
                val fieldName = rawFieldName.replace("$", "_")
                val className = fieldParentClass?.name?.asString() ?: ""
                val fieldKey = "$className.$fieldName"
                val isSharedVariableField = fieldKey in context.sharedVariableFields

                if (isSharedVariableField) {
                    // Pass the box itself, not the dereferenced .value
                    // Generate m.fieldName (without .value)
                    val receiver = arg.receiver?.accept(this, data) ?: BrsMRef()
                    BrsDotAccess(receiver, fieldName)
                } else {
                    arg.accept(this, data)
                }
            } else {
                arg.accept(this, data)
            }
        })

        // Check if this is an external class (Roku SDK type)
        if (irClass.isExternal || isExternalClass(irClass)) {
            // External classes use CreateObject()
            val brsTypeName = getBrsExternalTypeName(irClass)
            return BrsCreateObject(brsTypeName, arguments.toMutableList())
        }

        // Record dependency for this constructor call via function manifest
        val constructorName = context.getBrsName(constructor)
        context.recordFunctionDependency(constructorName)

        // Use mangled constructor name to support overloading
        return BrsFunctionCall(
            BrsIdentifier(constructorName),
            arguments
        )
    }

    override fun visitDelegatingConstructorCall(expression: IrDelegatingConstructorCall, data: Unit): BrsExpression {
        val constructor = expression.symbol.owner
        val irClass = constructor.parentAsClass

        val arguments = (0 until expression.valueArgumentsCount).mapNotNull { i ->
            expression.getValueArgument(i)?.let { it.accept(this, data) }
        }

        // Record dependency for delegating constructor call (e.g., calling super constructor)
        val constructorName = context.getBrsName(constructor)
        if (!isExternalClass(irClass)) {
            context.recordFunctionDependency(constructorName)
        }

        // Use mangled constructor name to support overloading
        return BrsFunctionCall(
            BrsIdentifier(constructorName),
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
        val irClass = constructor.parentAsClass

        val arguments = (0 until expression.valueArgumentsCount).mapNotNull { i ->
            expression.getValueArgument(i)?.let { it.accept(this, data) }
        }

        // Record dependency for enum constructor call
        val constructorName = context.getBrsName(constructor)
        if (!isExternalClass(irClass)) {
            context.recordFunctionDependency(constructorName)
        }

        // Use mangled constructor name to support overloading
        return BrsFunctionCall(
            BrsIdentifier(constructorName),
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
     * Checks if a property is a SceneGraph interface field.
     *
     * Interface fields are accessed via m.top.fieldName instead of m.fieldName.
     * This includes properties annotated with any @SG*Field annotation or @BrsField.
     */
    internal fun hasInterfaceFieldAnnotation(property: IrProperty): Boolean {
        val sgFieldAnnotations = setOf(
            "SGStringField", "SGIntegerField", "SGLongIntegerField", "SGFloatField",
            "SGDoubleField", "SGBooleanField", "SGArrayField", "SGAssocArrayField",
            "SGNodeField", "SGFunctionField", "SGUriField", "SGTimeField",
            "SGVector2DField", "SGColorField", "BrsField"
        )
        return property.annotations.any { annotation ->
            val annotationClass = annotation.type.classifierOrNull?.owner as? IrClass
            annotationClass?.name?.asString() in sgFieldAnnotations
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
        // Handle IMPLICIT_COERCION_TO_UNIT specially - the inner expression's value is discarded
        // For IrWhen inside IMPLICIT_COERCION_TO_UNIT, use the statement transformer to generate
        // proper if-then-else blocks instead of trying to treat it as an expression
        if (expression.operator == IrTypeOperator.IMPLICIT_COERCION_TO_UNIT) {
            val innerArg = expression.argument
            if (innerArg is IrWhen) {
                // Use statement transformer to generate BrsIf
                val brsIf = parent.statementVisitor.visitWhen(innerArg, data)
                return BrsStatementAsExpression(brsIf)
            }
            // For other inner expressions wrapped in IMPLICIT_COERCION_TO_UNIT,
            // just process the inner expression - its value is discarded anyway
            return innerArg.accept(this, data)
        }

        val argument = expression.argument.accept(this, data)

        return when (expression.operator) {
            IrTypeOperator.CAST, IrTypeOperator.IMPLICIT_CAST -> argument
            IrTypeOperator.SAFE_CAST -> {
                // Safe cast returns invalid if type doesn't match
                // For trivial expressions (identifiers, literals), it's safe to evaluate twice.
                // For non-trivial expressions (function calls, etc.), hoist to a temp var
                // to avoid double evaluation of side effects.
                if (isTrivialExpression(argument)) {
                    BrsConditional(
                        generateInstanceCheck(argument.deepCopy(), expression.typeOperand),
                        argument,
                        BrsInvalidLiteral()
                    )
                } else {
                    // Hoist non-trivial expression to temp variable to avoid double evaluation
                    val tempName = "__safeCast_tmp${parent.nextTempId()}"
                    parent.addHoistedStatement(BrsVariable(tempName, null, argument))
                    val tempRef = BrsIdentifier(tempName)
                    BrsConditional(
                        generateInstanceCheck(tempRef.deepCopy(), expression.typeOperand),
                        tempRef,
                        BrsInvalidLiteral()
                    )
                }
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
            IrTypeOperator.SAM_CONVERSION -> {
                // For SAM conversions, we need to add the interface method name in addition to invoke
                // This allows both lambda-style calls (.invoke()) and interface-style calls (.methodName())
                if (argument is BrsAALiteral) {
                    val targetClass = expression.typeOperand.classOrNull?.owner
                    if (targetClass != null && targetClass.isFun) {
                        // Find the single abstract method
                        val samMethod = targetClass.declarations
                            .filterIsInstance<org.jetbrains.kotlin.ir.declarations.IrSimpleFunction>()
                            .firstOrNull { it.modality == org.jetbrains.kotlin.descriptors.Modality.ABSTRACT }
                        if (samMethod != null) {
                            // Get the mangled method name and strip the class prefix
                            // to match how call sites generate method names
                            val fullMethodName = context.getBrsName(samMethod)
                            val className = context.getBrsName(targetClass)
                            val methodName = fullMethodName.removePrefix("${className}_")
                            // Find the invoke entry and duplicate it with the SAM method name
                            val invokeEntry = argument.entries.find { it.key == "invoke" }
                            if (invokeEntry != null) {
                                argument.entries.add(BrsAAEntry(methodName, invokeEntry.value.deepCopy()))
                            }
                        }
                    }
                }
                argument
            }
            else -> argument
        }
    }

    /**
     * Check if a BrsExpression is trivial (safe to evaluate multiple times without side effects).
     * Trivial expressions include identifiers, 'm' reference, and all literal types.
     */
    private fun isTrivialExpression(expr: BrsExpression): Boolean = when (expr) {
        is BrsIdentifier, is BrsMRef,
        is BrsIntLiteral, is BrsLongIntLiteral, is BrsFloatLiteral, is BrsDoubleLiteral,
        is BrsStringLiteral, is BrsBooleanLiteral, is BrsInvalidLiteral -> true
        else -> false
    }

    /**
     * Generate a type check expression for the given argument and target type.
     *
     * For primitive types, uses the __kotlin_isPrimitiveType helper which handles
     * both boxed and unboxed BrightScript type names (e.g., "String" vs "roString").
     * For class types, checks the __proto chain for the class name.
     */
    private fun generateInstanceCheck(argument: BrsExpression, targetType: IrType): BrsExpression {
        // For primitive types, use __kotlin_isPrimitiveType helper which handles
        // both boxed and unboxed type names (e.g., "String" vs "roString")
        when {
            targetType.isInt() || targetType.isShort() || targetType.isByte() -> {
                context.recordFunctionDependency("__kotlin_isPrimitiveType")
                return BrsFunctionCall(
                    BrsIdentifier("__kotlin_isPrimitiveType"),
                    mutableListOf(argument, BrsStringLiteral("Integer"))
                )
            }
            targetType.isLong() -> {
                context.recordFunctionDependency("__kotlin_isPrimitiveType")
                return BrsFunctionCall(
                    BrsIdentifier("__kotlin_isPrimitiveType"),
                    mutableListOf(argument, BrsStringLiteral("LongInteger"))
                )
            }
            targetType.isFloat() -> {
                context.recordFunctionDependency("__kotlin_isPrimitiveType")
                return BrsFunctionCall(
                    BrsIdentifier("__kotlin_isPrimitiveType"),
                    mutableListOf(argument, BrsStringLiteral("Float"))
                )
            }
            targetType.isDouble() -> {
                context.recordFunctionDependency("__kotlin_isPrimitiveType")
                return BrsFunctionCall(
                    BrsIdentifier("__kotlin_isPrimitiveType"),
                    mutableListOf(argument, BrsStringLiteral("Double"))
                )
            }
            targetType.isBoolean() -> {
                context.recordFunctionDependency("__kotlin_isPrimitiveType")
                return BrsFunctionCall(
                    BrsIdentifier("__kotlin_isPrimitiveType"),
                    mutableListOf(argument, BrsStringLiteral("Boolean"))
                )
            }
            targetType.isString() -> {
                context.recordFunctionDependency("__kotlin_isPrimitiveType")
                return BrsFunctionCall(
                    BrsIdentifier("__kotlin_isPrimitiveType"),
                    mutableListOf(argument, BrsStringLiteral("String"))
                )
            }
            targetType.isArray() ->
                return BrsBinaryOp(BrsTypeOf(argument), BrsBinaryOperator.EQ, BrsStringLiteral("roArray"))
        }

        // For class types, check type or __proto chain
        val classType = targetType.classOrNull?.owner

        if (classType != null && (classType.isExternal || isExternalClass(classType))) {
            // For external classes (native BrightScript types like RoArray, RoAssociativeArray),
            // use Type() comparison directly since they don't have __proto chains
            val brsTypeName = getBrsExternalTypeName(classType)
            return BrsBinaryOp(
                BrsTypeOf(argument),
                BrsBinaryOperator.EQ,
                BrsStringLiteral(brsTypeName)
            )
        }

        // For Kotlin classes, check the __proto chain
        val className = if (classType != null) {
            context.getBrsName(classType)
        } else {
            targetType.classFqName?.shortName()?.asString() ?: "Object"
        }

        // Generate: __kotlin_isInstanceOf(argument, "ClassName")
        // This requires the __kotlin_isInstanceOf helper function to be generated
        context.recordFunctionDependency("__kotlin_isInstanceOf")
        return BrsFunctionCall(
            BrsIdentifier("__kotlin_isInstanceOf"),
            mutableListOf(argument, BrsStringLiteral(className))
        )
    }

    /**
     * Maps Kotlin IR types to canonical BrightScript type names.
     * These names are used with __kotlin_isPrimitiveType for type checking.
     */
    private fun mapTypeToString(type: IrType): String {
        return when {
            type.isInt() || type.isShort() || type.isByte() -> "Integer"
            type.isLong() -> "LongInteger"
            type.isFloat() -> "Float"
            type.isDouble() -> "Double"
            type.isBoolean() -> "Boolean"
            type.isString() -> "String"
            type.isArray() -> "roArray"
            else -> "roAssociativeArray"
        }
    }

    // ==================== Binary Operations ====================

    override fun visitStringConcatenation(expression: IrStringConcatenation, data: Unit): BrsExpression {
        val parts = expression.arguments.mapIndexed { index, arg ->
            val expr = arg.accept(this, data)
            // Convert non-string arguments to strings for BrightScript string concatenation
            if (!arg.type.isString()) {
                parent.transformToString(expr, arg.type)
            } else {
                expr
            }
        }
        return parts.reduce { acc, expr ->
            BrsBinaryOp(acc, BrsBinaryOperator.CONCAT, expr)
        }
    }

    // ==================== Collections ====================

    override fun visitVararg(expression: IrVararg, data: Unit): BrsExpression {
        // If vararg has a single spread element, don't wrap it in another array
        // e.g., hashMapOf(*pairs) should just pass pairs, not [pairs]
        if (expression.elements.size == 1 && expression.elements[0] is IrSpreadElement) {
            val spreadElement = expression.elements[0] as IrSpreadElement
            return spreadElement.expression.accept(this, data)
        }

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
        // This happens when a when expression in statement context (value discarded) has
        // a non-Unit result type (e.g., StringBuilder.append() which returns StringBuilder).
        // Instead of generating an IIFE pattern (which BrightScript doesn't support),
        // delegate to the statement transformer to generate proper if-then-else,
        // and wrap it in BrsStatementAsExpression so it renders as a statement.

        // Use the statement transformer to generate a proper BrsIf
        val brsIf = parent.statementVisitor.visitWhen(expression, data)

        // Wrap the if statement in BrsStatementAsExpression so it renders as a statement
        // rather than attempting to use it as a value
        return BrsStatementAsExpression(brsIf)
    }

    private fun IrBranch.isElse(): Boolean {
        return condition is IrConst && (condition as IrConst).value == true
    }

    // ==================== Function Expressions ====================

    override fun visitFunctionExpression(expression: IrFunctionExpression, data: Unit): BrsExpression {
        val function = expression.function

        // Detect captured variables from outer scope
        val capturedVars = parent.detectCapturedVariables(function)

        // Build parameter list, starting with extension receiver if present
        val allParameters = mutableListOf<BrsParameter>()

        // Add extension receiver as first parameter if present (matches regular function handling)
        // Use "__receiver" instead of "m" to avoid collision with closure's m reference
        // This allows closure body to use m._componentM for component state access
        function.extensionReceiverParameter?.let { receiver ->
            allParameters.add(BrsParameter(
                name = "__receiver",
                type = parent.mapTypeToBrs(receiver.type)
            ))
        }

        // Add value parameters
        val rawParameters = function.valueParameters.map { param ->
            BrsParameter(
                name = sanitizeParameterName(param.name.asString()),
                type = parent.mapTypeToBrs(param.type)
            )
        }
        allParameters.addAll(rawParameters)

        val parameters = normalizeParametersForBrs(deduplicateParameterNames(allParameters))

        val returnType = parent.mapTypeToBrs(function.returnType)

        // Generate closure object for ALL function expressions
        // BrightScript anonymous functions cannot access outer scope variables,
        // so we create an object with captured variable fields and an invoke method.
        // The invoke method accesses captured variables via m.fieldName
        // For consistency, even lambdas without captures use this pattern so that
        // all lambdas can be invoked uniformly with .invoke()

        // Save previous closure context and lambda extension receiver
        val previousContext = parent.currentClosureContext
        val previousLambdaReceiver = parent.currentLambdaExtensionReceiver
        val previousInComponentLambda = parent.isInComponentLambda

        // Check if we're in component context - if so, we need to capture the component's m
        val needsComponentCapture = parent.isInComponentContext && !parent.isInComponentLambda

        // Set closure context for body transformation (if there are captures)
        if (capturedVars.isNotEmpty()) {
            parent.currentClosureContext = capturedVars
        }

        // Set lambda extension receiver for body transformation
        // This allows visitGetValue to rewrite receiver references to 'm'
        function.extensionReceiverParameter?.let {
            parent.currentLambdaExtensionReceiver = it.symbol
        }

        // If we're in component context, mark that we're now inside a component lambda
        // so that component state accesses use m._componentM instead of bare m
        if (needsComponentCapture) {
            parent.isInComponentLambda = true
        }

        // Transform body with closure context active (variable accesses will be rewritten)
        val body = function.body?.let { parent.transformBody(it) } ?: BrsBlock()

        // Restore previous context and lambda receiver
        parent.currentClosureContext = previousContext
        parent.currentLambdaExtensionReceiver = previousLambdaReceiver
        parent.isInComponentLambda = previousInComponentLambda

        // Build closure object fields for captured variables
        val entries = mutableListOf<BrsAAEntry>()

        // If we're in component context, capture the component's m reference
        if (needsComponentCapture) {
            entries.add(BrsAAEntry("_componentM", BrsMRef()))
        }

        for (capturedVar in capturedVars) {
            // When capturing 'this' from an outer method, use 'm' (BrightScript's object reference)
            // instead of 'this' (which doesn't exist in BrightScript methods)
            // Note: sanitizeParameterName converts <this> to __this
            val varValue = if (capturedVar.name == "__this") {
                BrsIdentifier("m")
            } else {
                BrsIdentifier(capturedVar.name)
            }

            // Check if this mutable capture is already boxed (part of sharedVariables)
            // If so, pass the box directly instead of re-boxing
            // Two detection mechanisms:
            // 1. Via SharedVariablesLowering which sets SHARED_VARIABLE_WRAPPER origin
            // 2. Via BrsSharedVariableDetectionLowering which populates sharedVariables set
            val varOwner = capturedVar.symbol.owner
            val hasSharedVarOrigin = varOwner is IrVariable && varOwner.origin == BrsDeclarationOrigin.SHARED_VARIABLE_WRAPPER
            val isAlreadyBoxed = capturedVar.isMutable && (hasSharedVarOrigin || capturedVar.symbol in parent.sharedVariables)
            val fieldValue = if (capturedVar.isMutable && !isAlreadyBoxed) {
                // Wrap mutable captures in { value: x } for mutation to propagate
                // (This case shouldn't happen anymore since all mutable captures should be in sharedVariables)
                BrsAALiteral(mutableListOf(BrsAAEntry("value", varValue)))
            } else {
                // Read-only captures or already-boxed mutable captures: store directly
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
        val rawParameters = function.valueParameters.map { param ->
            BrsParameter(
                name = sanitizeParameterName(param.name.asString()),
                type = parent.mapTypeToBrs(param.type)
            )
        }
        val parameters = normalizeParametersForBrs(deduplicateParameterNames(rawParameters))

        // Build the call arguments from the wrapper parameters
        val callArgs: MutableList<BrsExpression> = parameters.map { BrsIdentifier(it.name) as BrsExpression }.toMutableList()

        // Handle bound receiver if present (obj::method)
        val boundReceiver = expression.dispatchReceiver ?: expression.extensionReceiver
        val call = if (boundReceiver != null) {
            // For bound method references, call as method on the receiver
            val receiverExpr = boundReceiver.accept(this, data)
            val methodName = sanitizeMethodName(function.name.asString())
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
        // Composite expressions are sequences where all statements must execute
        // and the last expression is the result value.
        //
        // After BrsReturnableBlockLowering, composites may contain:
        // - A result variable declaration
        // - A returnable block (wrapped in while true { ... exit while })
        // - A get of the result variable (the return value)
        //
        // For nested returnable blocks (e.g., suspendCoroutineUninterceptedOrReturn
        // inside another inline function), each composite hoists only its OWN direct
        // variable declarations. Nested composites handle their own variables when
        // they are processed.
        //
        // This ensures correct ordering: when an assignment like `tmp_ret_2 = <composite>`
        // is processed, the inner composite's hoisted statements (including its variable
        // declaration and while loop) are placed BEFORE the assignment statement.
        val statements = expression.statements
        java.io.File("/tmp/returnable-block-debug.log").appendText("[DEBUG-COMP] visitComposite: ${statements.size} statements\n")
        statements.forEachIndexed { idx, s ->
            java.io.File("/tmp/returnable-block-debug.log").appendText("[DEBUG-COMP]   [$idx] ${s::class.simpleName}\n")
        }
        if (statements.isEmpty()) {
            return BrsInvalidLiteral()
        }

        // PASS 1: Hoist only DIRECT variable declarations from this composite
        // Nested composites will hoist their own variables when processed
        val hoistedVars = mutableSetOf<IrVariable>()
        hoistDirectVariables(statements, hoistedVars)
        java.io.File("/tmp/returnable-block-debug.log").appendText("[DEBUG-COMP] PASS1: hoisted ${hoistedVars.size} vars\n")

        // PASS 2: Process other statements (skip variables, already done)
        for (i in 0 until statements.size - 1) {
            val stmt = statements[i]
            if (stmt is IrVariable) continue  // Already hoisted in pass 1

            java.io.File("/tmp/returnable-block-debug.log").appendText("[DEBUG-COMP] PASS2: processing stmt[$i] ${stmt::class.simpleName}\n")
            parent.pushHoistedScope()
            val transformed = when (stmt) {
                is IrWhen -> parent.statementVisitor.visitWhen(stmt, Unit)
                is IrWhileLoop -> parent.statementVisitor.visitWhileLoop(stmt, Unit)
                is IrDoWhileLoop -> parent.statementVisitor.visitDoWhileLoop(stmt, Unit)
                is IrReturnableBlock -> parent.statementVisitor.visitBlock(stmt, Unit)
                is IrBlock -> parent.statementVisitor.visitBlock(stmt, Unit)
                else -> parent.transformStatement(stmt)
            }
            val nestedHoisted = parent.popHoistedScope()
            java.io.File("/tmp/returnable-block-debug.log").appendText("[DEBUG-COMP]   nestedHoisted: ${nestedHoisted.size}, adding each to parent\n")
            nestedHoisted.forEach { parent.addHoistedStatement(it) }
            transformed?.let {
                java.io.File("/tmp/returnable-block-debug.log").appendText("[DEBUG-COMP]   adding transformed: ${it::class.simpleName}\n")
                parent.addHoistedStatement(it)
            }
        }

        // Return the last statement's value
        val last = statements.last()
        java.io.File("/tmp/returnable-block-debug.log").appendText("[DEBUG-COMP] returning last: ${last::class.simpleName}\n")
        return if (last is IrExpression) {
            last.accept(this, data)
        } else {
            BrsInvalidLiteral()
        }
    }

    /**
     * Hoists only DIRECT variable declarations from the composite's statements.
     *
     * IMPORTANT: We do NOT recurse into nested composites/blocks. Each composite
     * handles its own variable hoisting when it is processed. Deep traversal
     * caused variables from inner composites to be hoisted too early, resulting
     * in wrong statement ordering (e.g., `tmp_ret_2 = tmp_ret_1` appearing before
     * the inner while loop that assigns to `tmp_ret_1`).
     *
     * The correct flow:
     * 1. Outer composite hoists only its direct variable (tmp_ret_2)
     * 2. Outer composite processes its returnable block
     * 3. Inside the block, when IrSetValue(tmp_ret_2, <inner_composite>) is processed,
     *    the inner composite hoists its own variables (tmp_ret_1) at that point
     * 4. The inner composite's hoisted statements go BEFORE the assignment
     */
    private fun hoistDirectVariables(statements: List<IrStatement>, hoistedVars: MutableSet<IrVariable>) {
        for (stmt in statements) {
            if (stmt is IrVariable && stmt !in hoistedVars) {
                hoistedVars.add(stmt)
                val varName = stmt.name.asString()
                java.io.File("/tmp/returnable-block-debug.log").appendText("[DEBUG-HOIST] hoistDirectVariables: hoisting '$varName'\n")
                val transformed = parent.statementVisitor.visitVariable(stmt, Unit)
                parent.addHoistedStatement(transformed)
            }
        }
    }

    override fun visitBlock(expression: IrBlock, data: Unit): BrsExpression {
        // Handle IrReturnableBlock (from inlined functions).
        // If BrsReturnableBlockLowering wrapped this block (because returns target it), we would
        // see an IrComposite here instead, not the IrReturnableBlock directly.
        // So if we see IrReturnableBlock in expression context, it means no returns targeted it,
        // and we should execute it for side effects only and return Unit.
        if (expression is IrReturnableBlock) {
            // Transform the block as a statement (wraps in while true { ... exit while })
            val stmt = parent.statementVisitor.visitBlock(expression, Unit)
            // Hoist the statement for side effects
            parent.addHoistedStatement(stmt)
            // Return Unit (the block has no meaningful return value since no returns target it)
            return BrsInvalidLiteral()
        }

        // Handle IO_WORKER_CALL blocks from BrsIOWorkerExtractionLowering.
        // Structure: [IrConst(workerName), IrComposite(captures: [value, name, value, name, ...])]
        // Transform to: runIOWorker_T_k_("workerName", { "name": value, ... })
        if (expression.origin == BrsStatementOrigins.IO_WORKER_CALL) {
            return transformIOWorkerCall(expression)
        }

        // Handle increment/decrement blocks - when used as expression (value needed),
        // we need to hoist the temp variable and setter, then return the appropriate value
        // Block structure: [IrVariable(<unary>) = getter(), setter(<unary>+1) or IrSetValue, IrGetValue(<unary>)]
        if (expression.origin == IrStatementOrigin.POSTFIX_INCR ||
            expression.origin == IrStatementOrigin.POSTFIX_DECR ||
            expression.origin == IrStatementOrigin.PREFIX_INCR ||
            expression.origin == IrStatementOrigin.PREFIX_DECR) {

            val statements = expression.statements
            val isPostfix = expression.origin == IrStatementOrigin.POSTFIX_INCR ||
                           expression.origin == IrStatementOrigin.POSTFIX_DECR

            // Find the temp variable and its initializer
            val tempVar = statements.filterIsInstance<IrVariable>().firstOrNull {
                it.name.asString().startsWith("<") && it.name.asString().endsWith(">")
            }
            val tempVarInitializer = tempVar?.initializer

            if (tempVar != null && tempVarInitializer != null) {
                // Generate a unique name for the hoisted temp variable
                val tempVarName = "__incr_tmp_${parent.nextTempId()}"

                // 1. Hoist the temp variable declaration with the OLD value
                val hoistedTempVar = BrsVariable(
                    name = tempVarName,
                    type = parent.mapTypeToBrs(tempVar.type),
                    initializer = parent.transformExpression(tempVarInitializer)
                )
                parent.addHoistedStatement(hoistedTempVar)

                // 2. Find and hoist the setter/assignment
                for (stmt in statements) {
                    if (stmt !== statements.last()) {
                        when (stmt) {
                            is IrCall -> {
                                // Property setter - use temp var in the call
                                parent.pushTempVarSubstitution(tempVar.symbol, null) // Signal to use tempVarName
                                parent.setTempVarName(tempVar.symbol, tempVarName)
                                try {
                                    val setterCall = stmt.accept(this, data)
                                    parent.addHoistedStatement(BrsExpressionStatement(setterCall))
                                } finally {
                                    parent.popTempVarSubstitution(tempVar.symbol)
                                }
                            }
                            is IrSetValue, is IrSetField -> {
                                // Local variable assignment - use temp var
                                parent.pushTempVarSubstitution(tempVar.symbol, null)
                                parent.setTempVarName(tempVar.symbol, tempVarName)
                                try {
                                    val assignment = parent.transformStatement(stmt)
                                    if (assignment != null) {
                                        parent.addHoistedStatement(assignment)
                                    }
                                } finally {
                                    parent.popTempVarSubstitution(tempVar.symbol)
                                }
                            }
                            is IrVariable -> {
                                // Skip the temp variable declaration itself
                            }
                        }
                    }
                }

                // 3. Return the appropriate value
                // For postfix: return the OLD value (temp var)
                // For prefix: return the NEW value (temp var +/- 1)
                return if (isPostfix) {
                    BrsIdentifier(tempVarName)
                } else {
                    // For prefix, the result is the new value, which is what the variable
                    // was set to. Since we hoisted the assignment, we can read the variable.
                    // But actually, for prefix, the last statement is also an IrGetValue
                    // which would give us the OLD value. We need to compute NEW value.
                    val isIncrement = expression.origin == IrStatementOrigin.PREFIX_INCR
                    BrsBinaryOp(
                        BrsIdentifier(tempVarName),
                        if (isIncrement) BrsBinaryOperator.ADD else BrsBinaryOperator.SUB,
                        BrsIntLiteral(1)
                    )
                }
            }

            // Fallback: transform and return the last statement (should be the value)
            val lastStmt = statements.lastOrNull()
            if (lastStmt != null && lastStmt is IrExpression) {
                return lastStmt.accept(this, data)
            }
        }

        // Handle for-loop blocks in expression context
        // For-loop blocks need to be transformed as statements and wrapped
        if (expression.origin == IrStatementOrigin.FOR_LOOP) {
            val forLoopStmt = parent.statementVisitor.visitBlock(expression, Unit)
            return BrsStatementAsExpression(forLoopStmt)
        }

        // Handle ELVIS and SAFE_CALL blocks: { val tmp = expr; when { ... } }
        // These are generated by FIR2IR for elvis (a ?: b) and safe call (a?.b) expressions.
        // The variable declaration must be hoisted so it's available in the when condition.
        // Note: After BrsWhenExpressionLowering, the when expression may be wrapped in a
        // when-lowered block { var __when_tmp; when { ... }; __when_tmp }.
        if (expression.origin == IrStatementOrigin.ELVIS || expression.origin == IrStatementOrigin.SAFE_CALL) {
            val statements = expression.statements
            if (statements.size == 2) {
                val firstStmt = statements[0]
                val secondStmt = statements[1]

                // Check if second statement is either:
                // 1. An IrWhen (not yet lowered), or
                // 2. An IrBlock that is a when-lowered block (after BrsWhenExpressionLowering)
                val isWhenOrWhenLoweredBlock = secondStmt is IrWhen ||
                    (secondStmt is IrBlock && secondStmt.statements.firstOrNull()?.let {
                        it is IrVariable && it.name.asString().startsWith("__when_tmp")
                    } == true)

                if (firstStmt is IrVariable && isWhenOrWhenLoweredBlock) {
                    // Transform and hoist the variable declaration
                    val varInit = firstStmt.initializer?.let { parent.transformExpression(it) }

                    // Take any hoisted statements from initializer transformation
                    val initHoisted = parent.takeHoistedStatements()
                    initHoisted.forEach { parent.addHoistedStatement(it) }

                    // Add the temporary variable declaration
                    parent.addHoistedStatement(
                        BrsVariable(
                            firstStmt.name.asString(),
                            parent.mapTypeToBrs(firstStmt.type),
                            varInit
                        )
                    )

                    // Transform the when expression (or when-lowered block) - it will use the now-hoisted variable
                    return (secondStmt as IrExpression).accept(this, data)
                }
            }
        }

        // Check for when-lowered blocks: { var __when_tmp; when { ... -> __when_tmp = ... }; __when_tmp }
        // Or simpler: { var __when_tmp = if (...) a else b; __when_tmp }
        // These need special handling because preceding statements must be hoisted
        val statements = expression.statements
        if (statements.size >= 2) {
            val firstStmt = statements.firstOrNull()
            val lastStmt = statements.lastOrNull()

            // Detect when-lowered block by checking for __when_tmp variable and get
            val isWhenLoweredBlock = firstStmt is IrVariable &&
                firstStmt.name.asString().startsWith("__when_tmp") &&
                lastStmt is IrGetValue &&
                lastStmt.symbol.owner.name.asString().startsWith("__when_tmp")

            if (isWhenLoweredBlock) {
                // When-lowered blocks transform to: hoist all statements except last, return last as expression.
                // The hoisted statements will be consumed by the caller (e.g., flatMap for IrVariable).
                //
                // We push a new hoisting scope so nested transformations don't interfere with
                // outer scopes. After processing, we pop and add our statements to the parent scope.

                parent.pushHoistedScope()

                for (i in 0 until statements.size - 1) {
                    val stmt = statements[i]
                    when (stmt) {
                        is IrVariable -> {
                            // Transform the initializer - if it's a when-lowered block, its statements
                            // will be added to the current hoisted scope
                            val init = stmt.initializer?.let { parent.transformExpression(it) }
                            // Add the variable declaration after any hoisted statements from the initializer
                            parent.addHoistedStatement(BrsVariable(stmt.name.asString(), parent.mapTypeToBrs(stmt.type), init))
                        }
                        is IrWhen -> {
                            // Push a scope to capture any nested when-lowered block hoisting
                            parent.pushHoistedScope()
                            val whenStmt = parent.statementVisitor.visitWhen(stmt, Unit)
                            // Take any statements added during transformation and add to our scope
                            val nestedHoisted = parent.popHoistedScope()
                            nestedHoisted.forEach { parent.addHoistedStatement(it) }
                            parent.addHoistedStatement(whenStmt)
                        }
                        is IrWhileLoop -> {
                            parent.pushHoistedScope()
                            val loopStmt = parent.statementVisitor.visitWhileLoop(stmt, Unit)
                            val nestedHoisted = parent.popHoistedScope()
                            nestedHoisted.forEach { parent.addHoistedStatement(it) }
                            parent.addHoistedStatement(loopStmt)
                        }
                        is IrDoWhileLoop -> {
                            parent.pushHoistedScope()
                            val loopStmt = parent.statementVisitor.visitDoWhileLoop(stmt, Unit)
                            val nestedHoisted = parent.popHoistedScope()
                            nestedHoisted.forEach { parent.addHoistedStatement(it) }
                            parent.addHoistedStatement(loopStmt)
                        }
                        is IrBlock -> {
                            parent.pushHoistedScope()
                            val blockStmt = parent.statementVisitor.visitBlock(stmt, Unit)
                            val nestedHoisted = parent.popHoistedScope()
                            nestedHoisted.forEach { parent.addHoistedStatement(it) }
                            parent.addHoistedStatement(blockStmt)
                        }
                        else -> {
                            parent.pushHoistedScope()
                            val transformed = parent.transformStatement(stmt)
                            val nestedHoisted = parent.popHoistedScope()
                            nestedHoisted.forEach { parent.addHoistedStatement(it) }
                            transformed?.let { parent.addHoistedStatement(it) }
                        }
                    }
                }

                // Pop our scope and add all statements to the parent scope
                val blockStatements = parent.popHoistedScope()
                blockStatements.forEach { parent.addHoistedStatement(it) }

                // Return just the temp var reference
                return (lastStmt as IrExpression).accept(this, data)
            }
        }

        // Handle when-with-subject blocks: { val tmp0_subject = expr; when { ... } }
        // These are generated by FIR2IR for when expressions with subjects like `when (x)`.
        // The subject variable declaration must be hoisted so it's available in the when conditions.
        // The second statement can be either:
        // 1. IrWhen directly (when expression not yet lowered)
        // 2. IrBlock (when-lowered block containing __when_tmp, when, __when_tmp get)
        if (statements.size == 2) {
            val firstStmt = statements[0]
            val secondStmt = statements[1]

            // Detect when-with-subject block: first is subject variable, second is when or when-lowered block
            val isSubjectVariable = firstStmt is IrVariable &&
                firstStmt.name.asString().contains("subject")

            val isWhenOrWhenLoweredBlock = secondStmt is IrWhen ||
                (secondStmt is IrBlock && secondStmt.statements.firstOrNull()?.let {
                    it is IrVariable && it.name.asString().startsWith("__when_tmp")
                } == true)

            if (isSubjectVariable && isWhenOrWhenLoweredBlock) {
                val subjectVar = firstStmt as IrVariable
                // Transform and hoist the subject variable declaration
                val varInit = subjectVar.initializer?.let { parent.transformExpression(it) }

                // Take any hoisted statements from initializer transformation
                val initHoisted = parent.takeHoistedStatements()
                initHoisted.forEach { parent.addHoistedStatement(it) }

                // Add the subject variable declaration
                parent.addHoistedStatement(
                    BrsVariable(
                        subjectVar.name.asString(),
                        parent.mapTypeToBrs(subjectVar.type),
                        varInit
                    )
                )

                // Transform the when expression or when-lowered block - it will use the now-hoisted subject variable
                return (secondStmt as IrExpression).accept(this, data)
            }
        }

        // Default: Block expressions return the last statement's value
        // But we must also execute any preceding statements (side effects)!
        // This is critical for inlined lambda bodies like:
        //   { sideEffect(); returnValue }
        //
        // Note: After BrsReturnableBlockLowering runs, returnable blocks are converted
        // to composites with temp variables, so returns targeting the block become
        // assignments. This handles most cases from inline function expansion.
        //
        // For remaining cases (blocks that weren't from returnable blocks), we hoist
        // the side effect statements so they execute, then return the value.
        return if (statements.isEmpty()) {
            BrsInvalidLiteral()
        } else {
            // Hoist all statements except the last one (which is the return value)
            for (i in 0 until statements.size - 1) {
                val stmt = statements[i]
                parent.pushHoistedScope()
                val transformed = when (stmt) {
                    is IrWhen -> parent.statementVisitor.visitWhen(stmt, Unit)
                    is IrWhileLoop -> parent.statementVisitor.visitWhileLoop(stmt, Unit)
                    is IrDoWhileLoop -> parent.statementVisitor.visitDoWhileLoop(stmt, Unit)
                    is IrBlock -> parent.statementVisitor.visitBlock(stmt, Unit)
                    else -> parent.transformStatement(stmt)
                }
                val nestedHoisted = parent.popHoistedScope()
                nestedHoisted.forEach { parent.addHoistedStatement(it) }
                transformed?.let { parent.addHoistedStatement(it) }
            }

            // Return the last statement's value
            val last = statements.last()
            if (last is IrExpression) {
                last.accept(this, data)
            } else {
                BrsInvalidLiteral()
            }
        }
    }

    /**
     * Transform an IO_WORKER_CALL block into a runIOWorker function call.
     *
     * Block structure from BrsIOWorkerExtractionLowering:
     * - statements[0]: IrConst<String> with the worker name
     * - statements[1]: IrComposite with IO_WORKER_CAPTURES origin containing alternating
     *                  [value, name, value, name, ...] pairs
     *
     * Emits: runIOWorker_T_k_("workerName", { "name": value, ... })
     */
    private fun transformIOWorkerCall(expression: IrBlock): BrsExpression {
        val statements = expression.statements

        // Extract worker name from first statement
        val workerNameConst = statements.firstOrNull() as? IrConst
            ?: return BrsInvalidLiteral()
        val workerName = workerNameConst.value as? String
            ?: return BrsInvalidLiteral()

        // Build the captures AA literal
        val capturesEntries = mutableListOf<BrsAAEntry>()

        // If there's a second statement with captures, extract them
        if (statements.size > 1) {
            val capturesBlock = statements[1]
            if (capturesBlock is IrComposite && capturesBlock.origin == BrsStatementOrigins.IO_WORKER_CAPTURES) {
                // Pairs are: [value, name, value, name, ...]
                val captureStatements = capturesBlock.statements
                var i = 0
                while (i < captureStatements.size - 1) {
                    val valueExpr = captureStatements[i] as? IrExpression
                    val nameConst = captureStatements[i + 1] as? IrConst
                    if (valueExpr != null && nameConst != null) {
                        val captureName = nameConst.value as? String ?: "unknown"
                        val captureValue = parent.transformExpression(valueExpr)
                        capturesEntries.add(BrsAAEntry(captureName, captureValue))
                    }
                    i += 2
                }
            }
        }

        val capturesAA = BrsAALiteral(capturesEntries)

        // Emit: runIOWorker_T_k_("workerName", capturesAA)
        // For now, we use a generic name since type erasure makes all instances the same
        val functionName = "runIOWorker_Str_k_" // Simplified - actual type handling TBD
        return parent.createFunctionCall(functionName, mutableListOf(
            BrsStringLiteral(workerName),
            capturesAA
        ))
    }

    /**
     * Determine the .brs file name a function will be compiled to.
     * Works for both local functions (from source) and klib functions.
     */
    private fun determineBrsFileName(function: IrFunction, functionName: String): String? {
        // Runtime helpers (__kotlin_*) should be looked up in the manifest
        // They are defined in the first stdlib file during compilation
        if (functionName.startsWith("__kotlin_")) {
            return context.dependencyFunctionManifest[functionName]
        }

        // ALWAYS check manifest first for any function
        // This handles stdlib classes/interfaces correctly (e.g., MutableList.add → CollectionsKt.brs)
        // Methods on interfaces like MutableList are compiled into CollectionsKt.brs,
        // not MutableListKt.brs (which doesn't exist)
        context.dependencyFunctionManifest[functionName]?.let { return it }

        val functionParent = function.parent

        return when (functionParent) {
            is IrClass -> {
                // External classes/interfaces (e.g., RoSGNodeEvent) don't produce output files
                // They're just declarations of native BrightScript types
                if (functionParent.isExternal) {
                    null
                } else if (parent.isExtractedLocalClass(functionParent)) {
                    // Lambda classes and function reference classes don't produce separate files
                    // They're generated inline in the containing file even after extraction
                    null
                } else if (functionParent.isCompanion) {
                    // Companion object methods are compiled into the containing class's file
                    val containingClass = functionParent.parent as? IrClass
                    if (containingClass != null) {
                        context.getBrsName(containingClass) + "Kt.brs"
                    } else {
                        context.getBrsName(functionParent) + "Kt.brs"
                    }
                } else {
                    // Function belongs to a class in current module - use class name with Kt suffix
                    context.getBrsName(functionParent) + "Kt.brs"
                }
            }
            is IrFile -> {
                // Top-level function in same module - use source file name with Kt suffix
                File(functionParent.path).nameWithoutExtension + "Kt.brs"
            }
            is IrPackageFragment -> {
                // Top-level function from klib (IrExternalPackageFragment)
                // Manifest was already checked above, this is fallback for older klibs
                // Use function name prefix as a heuristic
                // E.g., mutableListOf_k_ -> mutableListOfKt.brs
                val prefix = functionName.substringBefore("_k_")
                if (prefix.isNotEmpty() && prefix != functionName) {
                    "${prefix}Kt.brs"
                } else {
                    null
                }
            }
            else -> null
        }
    }

}

// ==================== Type Helper Extensions ====================

/**
 * Check if a type is primitive for comparison purposes.
 * Primitive types can use BrightScript's native = and <> operators.
 * Non-primitive types (classes, Any, type parameters) need brsStructuralEquals.
 */
private fun IrType.isPrimitiveForComparison(): Boolean {
    // Get the non-nullable version for checking
    val baseType = this.makeNotNull()

    // Primitive types that BrightScript can compare natively
    if (baseType.isInt() || baseType.isLong() || baseType.isFloat() || baseType.isDouble() ||
        baseType.isShort() || baseType.isByte() || baseType.isBoolean() || baseType.isChar() ||
        baseType.isString()) {
        return true
    }

    // Type parameters (generics) need structural comparison
    if (baseType.classifierOrNull is IrTypeParameterSymbol) {
        return false
    }

    // Any and Nothing types need structural comparison
    if (baseType.isAny() || baseType.isNothing()) {
        return false
    }

    // Class types (non-primitive) need structural comparison
    val classifier = baseType.classifierOrNull?.owner
    if (classifier is IrClass) {
        // Enum comparisons are handled separately above, but regular classes need structural
        return false
    }

    // Default to structural comparison for safety
    return false
}

/**
 * Check if a type is primitive for arithmetic operations.
 * Primitive types can use BrightScript's native +, -, *, /, MOD operators.
 * Non-primitive types (wrapper classes like UInt, ULong) need method calls.
 */
private fun IrType.isPrimitiveForArithmetic(): Boolean {
    val baseType = this.makeNotNull()

    // Primitive numeric types that BrightScript can operate on natively
    if (baseType.isInt() || baseType.isLong() || baseType.isFloat() || baseType.isDouble() ||
        baseType.isShort() || baseType.isByte()) {
        return true
    }

    // String uses + for concatenation
    if (baseType.isString()) {
        return true
    }

    // Everything else (UInt, ULong, UByte, UShort, custom classes) needs method calls
    return false
}
