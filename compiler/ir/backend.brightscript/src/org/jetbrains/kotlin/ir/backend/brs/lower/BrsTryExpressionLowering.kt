/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.lower

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.ir.backend.brs.BrsIrBackendContext
import org.jetbrains.kotlin.ir.builders.declarations.buildVariable
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.types.isNothing
import org.jetbrains.kotlin.ir.types.isUnit
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.Name

/**
 * Rewrites non-Unit `try` expressions into statement-level try/catch with a
 * temp result variable — BrightScript's try/catch is statement-only, and the
 * expression transformer's fallback (BrsStatementAsExpression) rendered
 * invalid code like `x = return try` (Task 18 finisher ledger).
 *
 * ```kotlin
 * val x = try { a() } catch (e: Throwable) { b() }
 * ```
 * becomes
 * ```kotlin
 * val x = run {
 *     var tmp: T? = null
 *     try { tmp = a() } catch (e: Throwable) { tmp = b() }
 *     tmp
 * }
 * ```
 *
 * Runs on every non-Unit IrTry regardless of position: a statement-position
 * non-Unit try just gains a harmless temp, and the block shape matches what
 * BrsWhenExpressionLowering already produces, so all downstream handling
 * (block-as-expression hoisting, condition consumption) applies unchanged.
 */
class BrsTryExpressionLowering(
    private val context: BrsIrBackendContext
) : FileLoweringPass {

    private var tempVarCounter = 0

    override fun lower(irFile: IrFile) {
        tempVarCounter = 0
        irFile.transformChildrenVoid(TryExpressionTransformer())
    }

    private inner class TryExpressionTransformer : IrElementTransformerVoid() {

        private var currentDeclarationParent: IrDeclarationParent? = null

        override fun visitFunction(
            declaration: org.jetbrains.kotlin.ir.declarations.IrFunction
        ): IrStatement {
            val previous = currentDeclarationParent
            currentDeclarationParent = declaration
            val result = super.visitFunction(declaration)
            currentDeclarationParent = previous
            return result
        }

        override fun visitClass(
            declaration: org.jetbrains.kotlin.ir.declarations.IrClass
        ): IrStatement {
            val previous = currentDeclarationParent
            currentDeclarationParent = declaration
            val result = super.visitClass(declaration)
            currentDeclarationParent = previous
            return result
        }

        override fun visitTry(aTry: IrTry): IrExpression {
            // Children first: nested tries inside arms get their own temps.
            val transformed = super.visitTry(aTry) as IrTry
            if (transformed.type.isUnit()) {
                return transformed
            }
            // Nothing-typed tries (all arms throw) are lowered too: the arms stay
            // unwrapped (Nothing guard in assignArmTo) and the tail read of the
            // temp is dead but legal — leaving them would hit the statement-as-
            // expression fallback and render `x = try ...` garbage.
            return transformToBlock(transformed)
        }

        private fun transformToBlock(aTry: IrTry): IrExpression {
            val startOffset = aTry.startOffset
            val endOffset = aTry.endOffset
            val resultType = aTry.type

            val tempVar = buildVariable(
                parent = currentDeclarationParent
                    ?: error("No declaration parent for try-expression temp"),
                startOffset = startOffset,
                endOffset = endOffset,
                origin = IrDeclarationOrigin.IR_TEMPORARY_VARIABLE,
                name = Name.identifier("__try_tmp${tempVarCounter++}"),
                type = resultType,
                isVar = true,
                isConst = false,
                isLateinit = false
            ).apply {
                initializer = IrConstImpl.constNull(startOffset, endOffset, resultType.makeNullable())
            }

            val newTry = IrTryImpl(
                startOffset, endOffset, context.irBuiltIns.unitType
            ).apply {
                tryResult = assignArmTo(tempVar, aTry.tryResult)
                for (catch in aTry.catches) {
                    catches.add(
                        IrCatchImpl(
                            catch.startOffset, catch.endOffset,
                            catch.catchParameter,
                            assignArmTo(tempVar, catch.result)
                        )
                    )
                }
                finallyExpression = aTry.finallyExpression
            }

            return IrBlockImpl(
                startOffset, endOffset, resultType, null,
                listOf(tempVar, newTry, IrGetValueImpl(startOffset, endOffset, resultType, tempVar.symbol))
            )
        }

        /** Rewrites an arm so its value lands in [tempVar]; control-flow tails stay. */
        private fun assignArmTo(tempVar: IrVariable, arm: IrExpression): IrExpression {
            fun assign(value: IrExpression): IrExpression = IrSetValueImpl(
                value.startOffset, value.endOffset,
                context.irBuiltIns.unitType, tempVar.symbol, value, null
            )

            return when (arm) {
                is IrBlock -> {
                    val statements = arm.statements.toMutableList()
                    val last = statements.lastOrNull()
                    if (last is IrExpression && last !is IrReturn && last !is IrThrow &&
                        last !is IrBreak && last !is IrContinue && !last.type.isNothing()
                    ) {
                        statements[statements.lastIndex] = assign(last)
                    }
                    IrBlockImpl(arm.startOffset, arm.endOffset, context.irBuiltIns.unitType, arm.origin, statements)
                }
                is IrReturn, is IrThrow, is IrBreak, is IrContinue -> arm
                else -> if (arm.type.isNothing()) arm else assign(arm)
            }
        }
    }
}
