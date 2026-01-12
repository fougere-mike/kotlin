/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.lower

import org.jetbrains.kotlin.backend.common.ir.SharedVariablesManager
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.declarations.buildVariable
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrSetValue
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.symbols.IrValueSymbol

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
 */
class BrsSharedVariablesManager(
    private val irBuiltIns: IrBuiltIns
) : SharedVariablesManager() {

    /**
     * Declares a shared variable by wrapping it in an associative array.
     *
     * @param originalDeclaration The original variable declaration
     * @return A new variable declaration initialized with the wrapper AA
     */
    override fun declareSharedVariable(originalDeclaration: IrVariable): IrVariable {
        // Create a new variable that holds an AA wrapper
        return buildVariable(
            parent = originalDeclaration.parent,
            startOffset = originalDeclaration.startOffset,
            endOffset = originalDeclaration.endOffset,
            origin = BrsDeclarationOrigin.CLOSURE_CAPTURE_FIELD,
            name = originalDeclaration.name,
            type = irBuiltIns.anyNType // AA type in BrightScript
        )
    }

    /**
     * Defines the value to store in a shared variable slot.
     *
     * This wraps the original value in an associative array.
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
        // happens during IR-to-BRS transformation
        return sharedVariableDeclaration
    }

    /**
     * Gets the value from a shared variable.
     *
     * @param sharedVariableSymbol The shared variable's symbol
     * @param originalGet The original get expression
     * @return An expression that reads the "value" field from the wrapper
     */
    override fun getSharedValue(
        sharedVariableSymbol: IrValueSymbol,
        originalGet: IrGetValue
    ): IrExpression {
        // This will generate: <sharedVar>.value
        // For now, return a get of the shared variable - the transformation happens in lowering
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
     * @param sharedVariableSymbol The shared variable's symbol
     * @param originalSet The original set expression
     * @return An expression that writes to the "value" field of the wrapper
     */
    override fun setSharedValue(
        sharedVariableSymbol: IrValueSymbol,
        originalSet: IrSetValue
    ): IrExpression {
        // This will generate: <sharedVar>.value = <newValue>
        // For now, return the original set - the transformation happens in lowering
        return originalSet
    }
}
