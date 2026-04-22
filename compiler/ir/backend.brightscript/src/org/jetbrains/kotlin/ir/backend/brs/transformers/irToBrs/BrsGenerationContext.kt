/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.transformers.irToBrs

import org.jetbrains.kotlin.brs.backend.ast.BrsStatement
import org.jetbrains.kotlin.ir.backend.brs.BrsIrBackendContext
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrLoop
import org.jetbrains.kotlin.ir.symbols.IrValueSymbol

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
