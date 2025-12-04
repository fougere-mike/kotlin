/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.lower

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.backend.brs.BrsIrBackendContext
import org.jetbrains.kotlin.ir.builders.declarations.buildVariable
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.types.isUnit
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.Name

/**
 * Lowers `when` expressions to statement-level if-else with temp variables.
 *
 * BrightScript doesn't support inline if-then-else expressions, only statement-level
 * if/else/end if blocks. This pass transforms:
 *
 * ```kotlin
 * val x = when {
 *     a -> 1
 *     b -> 2
 *     else -> 3
 * }
 * ```
 *
 * Into:
 *
 * ```kotlin
 * val x = run {
 *     var __when_tmp: Int
 *     when {
 *         a -> __when_tmp = 1
 *         b -> __when_tmp = 2
 *         else -> __when_tmp = 3
 *     }
 *     __when_tmp
 * }
 * ```
 *
 * The resulting block will be transformed by the statement transformer's `visitWhen`
 * which correctly generates BrsIf chains.
 *
 * Note: ANDAND and OROR origins are skipped as they are handled as binary operators.
 */
class BrsWhenExpressionLowering(
    private val context: BrsIrBackendContext
) : FileLoweringPass {

    private var tempVarCounter = 0

    override fun lower(irFile: IrFile) {
        tempVarCounter = 0
        irFile.transformChildrenVoid(WhenExpressionTransformer())
    }

    private inner class WhenExpressionTransformer : IrElementTransformerVoid() {

        // Track whether we're at the top level of a statement (where when is allowed as-is)
        private var insideExpressionContext = false

        override fun visitExpressionBody(body: IrExpressionBody): IrBody {
            // Expression body is an expression context
            val wasInExpression = insideExpressionContext
            insideExpressionContext = true
            val result = super.visitExpressionBody(body)
            insideExpressionContext = wasInExpression
            return result
        }

        override fun visitBlockBody(body: IrBlockBody): IrBody {
            // Block body statements are NOT expression context (when can appear directly)
            val wasInExpression = insideExpressionContext
            insideExpressionContext = false
            val result = super.visitBlockBody(body)
            insideExpressionContext = wasInExpression
            return result
        }

        override fun visitContainerExpression(expression: IrContainerExpression): IrExpression {
            // For blocks, the statements are NOT in expression context,
            // but the last expression (if it's the block's value) IS
            if (expression is IrBlock || expression is IrComposite) {
                val statements = expression.statements.toMutableList()
                val wasInExpression = insideExpressionContext

                for (i in statements.indices) {
                    val isLast = i == statements.lastIndex
                    // Only the last statement in a block used as expression is in expression context
                    insideExpressionContext = wasInExpression && isLast && statements[i] is IrExpression

                    statements[i] = statements[i].transform(this, null) as IrStatement
                }

                insideExpressionContext = wasInExpression

                if (expression is IrBlock) {
                    return IrBlockImpl(
                        expression.startOffset,
                        expression.endOffset,
                        expression.type,
                        expression.origin,
                        statements
                    )
                }
                // IrComposite
                return IrCompositeImpl(
                    expression.startOffset,
                    expression.endOffset,
                    expression.type,
                    expression.origin,
                    statements
                )
            }
            return super.visitContainerExpression(expression)
        }

        override fun visitVariable(declaration: IrVariable): IrStatement {
            // Variable initializer is in expression context
            val wasInExpression = insideExpressionContext
            insideExpressionContext = true
            declaration.initializer = declaration.initializer?.transform(this, null)
            insideExpressionContext = wasInExpression
            return declaration
        }

        override fun visitSetValue(expression: IrSetValue): IrExpression {
            // RHS of assignment is expression context
            val wasInExpression = insideExpressionContext
            insideExpressionContext = true
            val result = super.visitSetValue(expression)
            insideExpressionContext = wasInExpression
            return result
        }

        override fun visitSetField(expression: IrSetField): IrExpression {
            // RHS of field assignment is expression context
            val wasInExpression = insideExpressionContext
            insideExpressionContext = true
            val result = super.visitSetField(expression)
            insideExpressionContext = wasInExpression
            return result
        }

        override fun visitCall(expression: IrCall): IrExpression {
            // Function arguments are expression context
            val wasInExpression = insideExpressionContext
            insideExpressionContext = true
            val result = super.visitCall(expression)
            insideExpressionContext = wasInExpression
            return result
        }

        override fun visitReturn(expression: IrReturn): IrExpression {
            // Return value is expression context
            val wasInExpression = insideExpressionContext
            insideExpressionContext = true
            val result = super.visitReturn(expression)
            insideExpressionContext = wasInExpression
            return result
        }

        override fun visitWhileLoop(loop: IrWhileLoop): IrExpression {
            val wasInExpression = insideExpressionContext
            // Condition IS in expression context (used as boolean)
            insideExpressionContext = true
            loop.condition = loop.condition.transform(this, null)
            // Body is NOT in expression context (executed for side effects)
            insideExpressionContext = false
            loop.body = loop.body?.transform(this, null)
            insideExpressionContext = wasInExpression
            return loop
        }

        override fun visitDoWhileLoop(loop: IrDoWhileLoop): IrExpression {
            val wasInExpression = insideExpressionContext
            // Body is NOT in expression context
            insideExpressionContext = false
            loop.body = loop.body?.transform(this, null)
            // Condition IS in expression context
            insideExpressionContext = true
            loop.condition = loop.condition.transform(this, null)
            insideExpressionContext = wasInExpression
            return loop
        }

        override fun visitStringConcatenation(expression: IrStringConcatenation): IrExpression {
            // String template parts are expression context
            val wasInExpression = insideExpressionContext
            insideExpressionContext = true
            val result = super.visitStringConcatenation(expression)
            insideExpressionContext = wasInExpression
            return result
        }

        override fun visitConstructorCall(expression: IrConstructorCall): IrExpression {
            // Constructor arguments are expression context
            val wasInExpression = insideExpressionContext
            insideExpressionContext = true
            val result = super.visitConstructorCall(expression)
            insideExpressionContext = wasInExpression
            return result
        }

        override fun visitTypeOperator(expression: IrTypeOperatorCall): IrExpression {
            // Type operator argument is expression context (e.g., x as T, x is T)
            val wasInExpression = insideExpressionContext
            insideExpressionContext = true
            val result = super.visitTypeOperator(expression)
            insideExpressionContext = wasInExpression
            return result
        }

        override fun visitWhen(expression: IrWhen): IrExpression {
            // First, transform children (nested when expressions)
            val transformedWhen = super.visitWhen(expression) as IrWhen

            // Skip ANDAND and OROR - they become binary operators
            when (transformedWhen.origin) {
                IrStatementOrigin.ANDAND, IrStatementOrigin.OROR -> {
                    return transformedWhen
                }
                else -> { /* continue */ }
            }

            // If we're not in expression context, leave as-is (statement transformer handles it)
            if (!insideExpressionContext) {
                return transformedWhen
            }

            // Skip if result type is Unit (statement-like when)
            if (transformedWhen.type.isUnit()) {
                return transformedWhen
            }

            // Transform to block with temp variable
            return transformWhenToBlock(transformedWhen)
        }

        /**
         * Transforms a when expression to a block with temp variable:
         *
         * ```
         * {
         *     var __when_tmp: T
         *     when {
         *         cond1 -> { __when_tmp = val1 }
         *         cond2 -> { __when_tmp = val2 }
         *         else -> { __when_tmp = val3 }
         *     }
         *     __when_tmp
         * }
         * ```
         */
        private fun transformWhenToBlock(whenExpr: IrWhen): IrExpression {
            val startOffset = whenExpr.startOffset
            val endOffset = whenExpr.endOffset
            val resultType = whenExpr.type

            // Create temp variable with invalid initializer
            val tempVarName = "__when_tmp${tempVarCounter++}"
            val tempVar = buildVariable(
                parent = null, // Will be set by the IR infrastructure
                startOffset = startOffset,
                endOffset = endOffset,
                origin = IrDeclarationOrigin.IR_TEMPORARY_VARIABLE,
                name = Name.identifier(tempVarName),
                type = resultType,
                isVar = true,
                isConst = false,
                isLateinit = false
            ).apply {
                // Initialize to null/invalid - BrightScript requires an initial value
                initializer = IrConstImpl.constNull(startOffset, endOffset, resultType.makeNullable())
            }

            // Transform each branch to assign to temp var
            val transformedBranches = whenExpr.branches.map { branch ->
                val assignExpr = IrSetValueImpl(
                    startOffset = branch.result.startOffset,
                    endOffset = branch.result.endOffset,
                    type = context.irBuiltIns.unitType,
                    symbol = tempVar.symbol,
                    value = branch.result,
                    origin = null
                )

                IrBranchImpl(
                    startOffset = branch.startOffset,
                    endOffset = branch.endOffset,
                    condition = branch.condition,
                    result = assignExpr
                )
            }

            // Create the modified when (now returns Unit since branches are assignments)
            val modifiedWhen = IrWhenImpl(
                startOffset = startOffset,
                endOffset = endOffset,
                type = context.irBuiltIns.unitType,
                origin = whenExpr.origin
            ).apply {
                branches.addAll(transformedBranches)
            }

            // Create get of temp var (the block's result)
            val getTempVar = IrGetValueImpl(
                startOffset = startOffset,
                endOffset = endOffset,
                type = resultType,
                symbol = tempVar.symbol
            )

            // Create block: { var tmp; when { ... assign to tmp ... }; tmp }
            return IrBlockImpl(
                startOffset = startOffset,
                endOffset = endOffset,
                type = resultType,
                origin = null,
                statements = listOf(tempVar, modifiedWhen, getTempVar)
            )
        }
    }
}
