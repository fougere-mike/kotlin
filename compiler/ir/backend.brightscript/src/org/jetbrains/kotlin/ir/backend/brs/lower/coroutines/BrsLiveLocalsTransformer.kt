/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.lower.coroutines

import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrSetValue
import org.jetbrains.kotlin.ir.expressions.impl.IrCompositeImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetFieldImpl
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
}
