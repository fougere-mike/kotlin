/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.lower.coroutines

import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrBlock
import org.jetbrains.kotlin.ir.expressions.IrCatch
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrSetValue
import org.jetbrains.kotlin.ir.expressions.impl.IrBlockImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrCompositeImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetFieldImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrSetFieldImpl
import org.jetbrains.kotlin.ir.symbols.IrFieldSymbol
import org.jetbrains.kotlin.ir.symbols.IrValueSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid

/**
 * Transforms local variable accesses to field accesses on the coroutine class.
 *
 * Variables that are live across suspension points must be stored in fields
 * of the coroutine class so they persist across state machine transitions.
 * This transformer replaces IrGetValue/IrSetValue for such variables with
 * IrGetField/IrSetField accessing the corresponding coroutine class fields.
 *
 * Based on JS backend's LiveLocalsTransformer.
 */
class BrsLiveLocalsTransformer(
    private val localMap: Map<IrValueSymbol, IrFieldSymbol>,
    private val receiver: () -> IrExpression,
    private val unitType: IrType
) : IrElementTransformerVoid() {

    override fun visitGetValue(expression: IrGetValue): IrExpression {
        val field = localMap[expression.symbol] ?: return expression

        return expression.run {
            IrGetFieldImpl(startOffset, endOffset, field, type, receiver(), origin)
        }
    }

    override fun visitSetValue(expression: IrSetValue): IrExpression {
        expression.transformChildrenVoid(this)
        val field = localMap[expression.symbol] ?: return expression

        return expression.run {
            IrSetFieldImpl(startOffset, endOffset, field, receiver(), value, unitType, origin)
        }
    }

    override fun visitVariable(declaration: IrVariable): IrStatement {
        declaration.transformChildrenVoid(this)
        val field = localMap[declaration.symbol] ?: return declaration
        val initializer = declaration.initializer

        return if (initializer != null) {
            declaration.run {
                IrSetFieldImpl(startOffset, endOffset, field, receiver(), initializer, unitType)
            }
        } else {
            IrCompositeImpl(declaration.startOffset, declaration.endOffset, unitType)
        }
    }

    /**
     * Handle catch blocks specially. The catch parameter must remain an IrVariable
     * in the IrCatch structure, but we still want to transform accesses to it.
     *
     * We transform the result block normally, but leave the catch parameter alone
     * and manually handle its initialization through field access.
     */
    override fun visitCatch(aCatch: IrCatch): IrCatch {
        val catchParameter = aCatch.catchParameter
        val field = localMap[catchParameter.symbol]

        if (field != null) {
            // Transform the initializer if present
            val initializer = catchParameter.initializer
            if (initializer != null) {
                catchParameter.initializer = initializer.transform(this, null)

                // Transform the result block - accesses to catchParameter will be transformed to field accesses
                aCatch.result = aCatch.result.transform(this, null)

                // The catch parameter stays as a variable, but its initializer is now a field set
                // We need to prepend a field set to the catch result block
                val setField = IrSetFieldImpl(
                    catchParameter.startOffset,
                    catchParameter.endOffset,
                    field,
                    receiver(),
                    catchParameter.initializer!!,
                    unitType
                )
                catchParameter.initializer = null

                // Wrap the result with the field initialization
                val resultBlock = aCatch.result
                if (resultBlock is IrBlock) {
                    resultBlock.statements.add(0, setField)
                }

                return aCatch
            } else {
                // Global catch or synthetic catch - no initializer means the catch parameter
                // is implicitly initialized by the catch mechanism. We need to copy the catch
                // parameter value to the field before the result block uses it.
                aCatch.result = aCatch.result.transform(this, null)

                // Create a field set to copy catch parameter to the field
                // This must happen BEFORE the result block runs, so prepend it
                val setField = IrSetFieldImpl(
                    catchParameter.startOffset,
                    catchParameter.endOffset,
                    field,
                    receiver(),
                    IrGetValueImpl(
                        catchParameter.startOffset,
                        catchParameter.endOffset,
                        catchParameter.type,
                        catchParameter.symbol
                    ),
                    unitType
                )

                // Wrap the result with the field initialization
                val resultBlock = aCatch.result
                if (resultBlock is IrBlock) {
                    resultBlock.statements.add(0, setField)
                } else {
                    // If result is not a block, wrap it in one with the field set first
                    val block = IrBlockImpl(
                        resultBlock.startOffset,
                        resultBlock.endOffset,
                        resultBlock.type,
                        origin = null,
                        statements = mutableListOf(setField, resultBlock)
                    )
                    aCatch.result = block
                }

                return aCatch
            }
        }

        return super.visitCatch(aCatch)
    }
}
