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
 * Rewrites non-Unit `try` expressions in EXPRESSION position into
 * statement-level try/catch with a temp result variable — BrightScript's
 * try/catch is statement-only, and the expression transformer's fallback
 * (BrsStatementAsExpression) rendered invalid code like `x = return try`
 * (Task 18 finisher ledger).
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
 * The block shape matches what BrsWhenExpressionLowering produces, so all
 * downstream handling (block-as-expression hoisting) applies unchanged, and
 * the expression-context tracking mirrors that lowering's proven model.
 *
 * Statement-position tries are left alone even when their IR type is non-Unit
 * (e.g. `try { unitCall() } catch (e) { boolCall() }` — the discarded catch
 * value makes the IrTry non-Unit): the statement transformer's visitTry
 * handles them directly. Lowering them anyway would leave the block's
 * trailing temp read in a discarded position, which the emitter renders as a
 * bare identifier statement — a BrightScript SYNTAX ERROR (broke the stdlib's
 * Builders.kt on device; fix round 2).
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

        // Track whether we're in a position whose value is consumed. Mirrors
        // BrsWhenExpressionLowering's WhenExpressionTransformer exactly.
        private var insideExpressionContext = false

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

        // ==================== Expression-context tracking ====================

        override fun visitExpressionBody(body: IrExpressionBody): IrBody {
            val wasInExpression = insideExpressionContext
            insideExpressionContext = true
            val result = super.visitExpressionBody(body)
            insideExpressionContext = wasInExpression
            return result
        }

        override fun visitBlockBody(body: IrBlockBody): IrBody {
            // Block body statements are NOT expression context
            val wasInExpression = insideExpressionContext
            insideExpressionContext = false
            val result = super.visitBlockBody(body)
            insideExpressionContext = wasInExpression
            return result
        }

        override fun visitContainerExpression(expression: IrContainerExpression): IrExpression {
            // Only the last statement of a block used as an expression is in
            // expression context; the rest are statements.
            if (expression is IrBlock || expression is IrComposite) {
                val wasInExpression = insideExpressionContext
                val statements = expression.statements
                for (i in statements.indices) {
                    insideExpressionContext =
                        wasInExpression && i == statements.lastIndex && statements[i] is IrExpression
                    statements[i] = statements[i].transform(this, null) as IrStatement
                }
                insideExpressionContext = wasInExpression
                return expression
            }
            return super.visitContainerExpression(expression)
        }

        override fun visitVariable(declaration: IrVariable): IrStatement {
            val wasInExpression = insideExpressionContext
            insideExpressionContext = true
            declaration.initializer = declaration.initializer?.transform(this, null)
            insideExpressionContext = wasInExpression
            return declaration
        }

        override fun visitSetValue(expression: IrSetValue): IrExpression {
            val wasInExpression = insideExpressionContext
            insideExpressionContext = true
            val result = super.visitSetValue(expression)
            insideExpressionContext = wasInExpression
            return result
        }

        override fun visitSetField(expression: IrSetField): IrExpression {
            val wasInExpression = insideExpressionContext
            insideExpressionContext = true
            val result = super.visitSetField(expression)
            insideExpressionContext = wasInExpression
            return result
        }

        override fun visitCall(expression: IrCall): IrExpression {
            val wasInExpression = insideExpressionContext
            insideExpressionContext = true
            val result = super.visitCall(expression)
            insideExpressionContext = wasInExpression
            return result
        }

        override fun visitReturn(expression: IrReturn): IrExpression {
            val wasInExpression = insideExpressionContext
            insideExpressionContext = true
            val result = super.visitReturn(expression)
            insideExpressionContext = wasInExpression
            return result
        }

        override fun visitWhileLoop(loop: IrWhileLoop): IrExpression {
            val wasInExpression = insideExpressionContext
            insideExpressionContext = true
            loop.condition = loop.condition.transform(this, null)
            insideExpressionContext = false
            loop.body = loop.body?.transform(this, null)
            insideExpressionContext = wasInExpression
            return loop
        }

        override fun visitDoWhileLoop(loop: IrDoWhileLoop): IrExpression {
            val wasInExpression = insideExpressionContext
            insideExpressionContext = false
            loop.body = loop.body?.transform(this, null)
            insideExpressionContext = true
            loop.condition = loop.condition.transform(this, null)
            insideExpressionContext = wasInExpression
            return loop
        }

        override fun visitStringConcatenation(expression: IrStringConcatenation): IrExpression {
            val wasInExpression = insideExpressionContext
            insideExpressionContext = true
            val result = super.visitStringConcatenation(expression)
            insideExpressionContext = wasInExpression
            return result
        }

        override fun visitConstructorCall(expression: IrConstructorCall): IrExpression {
            val wasInExpression = insideExpressionContext
            insideExpressionContext = true
            val result = super.visitConstructorCall(expression)
            insideExpressionContext = wasInExpression
            return result
        }

        override fun visitDelegatingConstructorCall(expression: IrDelegatingConstructorCall): IrExpression {
            val wasInExpression = insideExpressionContext
            insideExpressionContext = true
            val result = super.visitDelegatingConstructorCall(expression)
            insideExpressionContext = wasInExpression
            return result
        }

        override fun visitTypeOperator(expression: IrTypeOperatorCall): IrExpression {
            // IMPLICIT_COERCION_TO_UNIT wraps expressions used as statements —
            // the value is discarded, so NOT expression context.
            val wasInExpression = insideExpressionContext
            if (expression.operator != IrTypeOperator.IMPLICIT_COERCION_TO_UNIT) {
                insideExpressionContext = true
            }
            val result = super.visitTypeOperator(expression)
            insideExpressionContext = wasInExpression
            return result
        }

        // ==================== The rewrite ====================

        override fun visitTry(aTry: IrTry): IrExpression {
            val wasInExpression = insideExpressionContext

            // Children first: nested tries inside arms get their own temps.
            // Arm values are consumed iff the try's value is consumed, so the
            // arms are visited under the try's own context; the finally block
            // is executed purely for effect.
            aTry.tryResult = aTry.tryResult.transform(this, null)
            for (aCatch in aTry.catches) {
                aCatch.result = aCatch.result.transform(this, null)
            }
            insideExpressionContext = false
            aTry.finallyExpression = aTry.finallyExpression?.transform(this, null)
            insideExpressionContext = wasInExpression

            // Statement position: the statement transformer's visitTry handles
            // it directly. Lowering here would strand the block's trailing temp
            // read as a bare identifier statement — a BRS syntax error.
            if (!wasInExpression) {
                return aTry
            }
            if (aTry.type.isUnit()) {
                return aTry
            }
            // Nothing-typed tries (all arms throw) in expression position are
            // lowered too: the arms stay unwrapped (Nothing guard in
            // assignArmTo) and the tail read of the temp is dead but legal —
            // leaving them would hit the statement-as-expression fallback and
            // render `x = try ...` garbage.
            return transformToBlock(aTry)
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
                        last !is IrBreak && last !is IrContinue && !last.type.isNothing() &&
                        !terminalIsAssignment(last)
                    ) {
                        statements[statements.lastIndex] = assign(last)
                    }
                    IrBlockImpl(arm.startOffset, arm.endOffset, context.irBuiltIns.unitType, arm.origin, statements)
                }
                is IrReturn, is IrThrow, is IrBreak, is IrContinue -> arm
                else -> if (arm.type.isNothing() || terminalIsAssignment(arm)) arm else assign(arm)
            }
        }
    }
}

/**
 * Result-temp wrap guard shared by [BrsTryExpressionLowering],
 * [BrsWhenExpressionLowering] and BrsStateMachineBuilder (visitTry/visitWhen):
 * true when the arm/branch's TERMINAL statement is an ASSIGNMENT (recursing
 * through container tails and implicit casts). Such an arm must NOT be
 * rewritten into a result-temp assignment.
 *
 * The arm's static TYPE cannot be trusted for this: under generic inference (a
 * runBlocking<T>-shaped builder inferring T = Any from the LUB of an Int arm
 * and a Unit arm) an arm whose terminal statement is an assignment arrives
 * typed Any, yet an assignment is Unit-valued in Kotlin no matter what the
 * coerced type says. Wrapping it makes the assignment the RHS of the temp set,
 * and BrightScript renders an assignment in expression position as a
 * COMPARISON — the write is silently lost
 * (m.__try_tmp = (m._result.value = ...)). An unwrapped arm leaves the temp at
 * its per-entry `invalid` initializer — exactly the BRS mapping of Unit, the
 * arm's true Kotlin value.
 *
 * Deliberately narrow: EVERY other terminal keeps its wrap, Unit-typed values
 * included — an unwrapped pure terminal emits as a bare value statement, which
 * is a BrightScript SYNTAX ERROR (device-proven: the stdlib's AwaitKt.brs
 * `if pendingCount = 0 then invalid` when a broader produces-a-value guard
 * briefly excluded Unit terminals from wrapping).
 */
internal fun terminalIsAssignment(statement: IrStatement): Boolean = when (statement) {
    is IrSetValue, is IrSetField -> true
    is IrContainerExpression -> statement.statements.lastOrNull()?.let(::terminalIsAssignment) == true
    // An implicit coercion doesn't change what the underlying terminal is.
    is IrTypeOperatorCall ->
        statement.operator == IrTypeOperator.IMPLICIT_CAST && terminalIsAssignment(statement.argument)
    else -> false
}
