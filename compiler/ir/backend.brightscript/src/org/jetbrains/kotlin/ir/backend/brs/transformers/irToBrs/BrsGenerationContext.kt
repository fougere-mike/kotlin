/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.transformers.irToBrs

import org.jetbrains.kotlin.backend.common.lower.BOUND_RECEIVER_PARAMETER
import org.jetbrains.kotlin.backend.common.lower.BOUND_VALUE_PARAMETER
import org.jetbrains.kotlin.brs.backend.ast.BrsDotAccess
import org.jetbrains.kotlin.brs.backend.ast.BrsExpression
import org.jetbrains.kotlin.brs.backend.ast.BrsMRef
import org.jetbrains.kotlin.brs.backend.ast.BrsStatement
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.backend.brs.BrsIrBackendContext
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrLoop
import org.jetbrains.kotlin.ir.expressions.IrSetValue
import org.jetbrains.kotlin.ir.symbols.IrValueSymbol
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid

/**
 * Shared mutable state used by [IrToBrsTransformer] and its subordinate
 * [IrStatementToBrsTransformer] / [IrExpressionToBrsTransformer] transformers.
 *
 * Relocated from [IrToBrsTransformer] in B1b1 so subordinate transformers no
 * longer reach into sibling-class internals via `parent.*`.
 *
 * B1b1 keeps the bag mutable. A later phase (B1b2) may redesign this to match
 * JS's per-scope-immutable [org.jetbrains.kotlin.ir.backend.js.utils.JsGenerationContext]
 * shape, but that's a separate workstream and will rewrite the mutation model of the traversal.
 */
class BrsGenerationContext(
    @Suppress("unused") private val backendContext: BrsIrBackendContext,
) {
    // ==================== Temp variable state ====================

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

    // ==================== Variable naming state ====================

    /**
     * Maps IR value symbols to unique BrightScript variable names.
     * This is needed because Kotlin IR can have multiple variables with the same name
     * (distinguished by their symbol), but BrightScript uses a single namespace where
     * variable names must be unique to avoid collisions.
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

    // ==================== Returnable block state ====================

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

    // ==================== Loop bookkeeping ====================

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

    // ==================== Hoisted statements ====================

    /**
     * Hoisted statements from when-expression lowered blocks.
     * These need to be emitted before the expression that uses them.
     *
     * Stack of hoisted statement scopes for proper nesting of when-lowered blocks.
     */
    private val hoistedScopes = mutableListOf<MutableList<BrsStatement>>().apply {
        add(mutableListOf()) // Global scope
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

    // ==================== Closure state ====================

    /**
     * Current closure context for variable access rewriting.
     * When non-null, we're inside a closure and need to rewrite captured variable accesses.
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

    // ==================== Constructor/component flags ====================

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

    // ==================== File + enum tracking ====================

    /**
     * The current file path being transformed.
     * Used for dependency tracking - we record which files each source depends on.
     */
    internal var currentFilePath: String? = null

    /**
     * Track enum classes encountered during transformation for initialization.
     */
    internal val enumClassNames = mutableListOf<String>()

    // ==================== Capture detection ====================

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
                if (declaration.visibility == DescriptorVisibilities.LOCAL) {
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
}

/**
 * Tracks captured variables when transforming a closure.
 * Maps IR variable symbols to their capture info (name and mutability).
 */
data class CapturedVariable(
    val symbol: IrValueSymbol,
    val name: String,
    val isMutable: Boolean,
)
