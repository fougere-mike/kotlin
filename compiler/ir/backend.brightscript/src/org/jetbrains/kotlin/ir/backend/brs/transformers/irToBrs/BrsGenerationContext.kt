/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.transformers.irToBrs

import org.jetbrains.kotlin.ir.backend.brs.BrsIrBackendContext
import org.jetbrains.kotlin.ir.expressions.IrExpression
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

    // ==================== Returnable block counter ====================

    /**
     * Counter for generating unique returnable block flag names.
     */
    internal var returnableBlockFlagCounter = 0
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
