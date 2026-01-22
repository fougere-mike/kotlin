/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.lower

import org.jetbrains.kotlin.backend.common.ir.SharedVariablesManager
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.declarations.buildVariable
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrSetValue
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrSetValueImpl
import org.jetbrains.kotlin.ir.symbols.IrValueSymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrVariableSymbolImpl

/**
 * Manages shared variables for BrightScript code generation.
 *
 * In BrightScript, shared variables (captured by closures) are implemented
 * by wrapping them in associative arrays with a single "value" field.
 * This allows the captured reference to be mutable across closure boundaries.
 *
 * For example:
 * ```kotlin
 * var x = 0
 * val lambda = { x++ }
 * ```
 *
 * Becomes:
 * ```brightscript
 * x = { value: 0 }
 * lambda = function()
 *     x.value = x.value + 1
 * end function
 * ```
 *
 * NOTE: This class creates the IR-level transformation, but the actual wrapping
 * and .value access is handled at transform time in IrToBrsTransformer based on
 * the sharedVariables set detected via detectSharedVariables().
 *
 * The new variable created by declareSharedVariable has SHARED_VARIABLE_WRAPPER origin
 * which is detected by IrToBrsTransformer to box the initializer in {value: ...}.
 */
class BrsSharedVariablesManager(
    private val irBuiltIns: IrBuiltIns
) : SharedVariablesManager() {

    /**
     * Declares a shared variable.
     *
     * The new variable will have the same name but with SHARED_VARIABLE_WRAPPER origin,
     * which tells IrToBrsTransformer to wrap the initializer in {value: ...}.
     *
     * @param originalDeclaration The original variable declaration
     * @return A new variable declaration that will be boxed at transform time
     */
    override fun declareSharedVariable(originalDeclaration: IrVariable): IrVariable {
        // Create a new variable with special origin to mark it as shared
        // The type is anyNType because it will hold an AA wrapper
        return buildVariable(
            parent = originalDeclaration.parent,
            startOffset = originalDeclaration.startOffset,
            endOffset = originalDeclaration.endOffset,
            origin = BrsDeclarationOrigin.SHARED_VARIABLE_WRAPPER,
            name = originalDeclaration.name,
            type = irBuiltIns.anyNType // AA type in BrightScript
        ).apply {
            // Copy the original initializer - it will be boxed at transform time
            this.initializer = originalDeclaration.initializer
        }
    }

    /**
     * Defines the value to store in a shared variable slot.
     *
     * @param originalDeclaration The original variable declaration
     * @param sharedVariableDeclaration The shared variable declaration
     * @return The shared variable declaration as an IrStatement
     */
    override fun defineSharedValue(
        originalDeclaration: IrVariable,
        sharedVariableDeclaration: IrVariable
    ): IrStatement {
        // Return the shared variable declaration - the actual AA wrapping
        // happens at transform time in IrToBrsTransformer
        return sharedVariableDeclaration
    }

    /**
     * Gets the value from a shared variable.
     *
     * At IR level, this returns an IrGetValue for the shared variable.
     * At transform time, IrToBrsTransformer will generate: varName.value
     *
     * @param sharedVariableSymbol The shared variable's symbol
     * @param originalGet The original get expression
     * @return An IrGetValue for the shared variable (transformed to .value access later)
     */
    override fun getSharedValue(
        sharedVariableSymbol: IrValueSymbol,
        originalGet: IrGetValue
    ): IrExpression {
        // Return a get of the shared variable
        // The transform to .value access happens in IrToBrsTransformer based on sharedVariables set
        return IrGetValueImpl(
            originalGet.startOffset,
            originalGet.endOffset,
            originalGet.type,
            sharedVariableSymbol,
            originalGet.origin
        )
    }

    /**
     * Sets the value in a shared variable.
     *
     * At IR level, this returns an IrSetValue for the shared variable.
     * At transform time, IrToBrsTransformer will generate: varName.value = newValue
     *
     * @param sharedVariableSymbol The shared variable's symbol
     * @param originalSet The original set expression
     * @return An IrSetValue for the shared variable (transformed to .value = access later)
     */
    override fun setSharedValue(
        sharedVariableSymbol: IrValueSymbol,
        originalSet: IrSetValue
    ): IrExpression {
        // Return a set of the shared variable
        // The transform to .value = access happens in IrToBrsTransformer based on sharedVariables set
        return IrSetValueImpl(
            originalSet.startOffset,
            originalSet.endOffset,
            originalSet.type,
            sharedVariableSymbol,
            originalSet.value,
            originalSet.origin
        )
    }
}
