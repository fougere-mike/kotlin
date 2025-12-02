/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.lower

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.ir.backend.brs.BrsIrBackendContext
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.Name

/**
 * Lowers increment/decrement operators to explicit addition/subtraction.
 *
 * BrightScript does not have `++` or `--` operators, so we need to transform:
 * - `++i` (prefix increment) → `i = i + 1`
 * - `i++` (postfix increment) → stores old value, then `i = i + 1`, returns old value
 * - `--i` (prefix decrement) → `i = i - 1`
 * - `i--` (postfix decrement) → stores old value, then `i = i - 1`, returns old value
 *
 * In Kotlin IR, increment/decrement is represented as:
 * - `IrSetValue` or `IrSetField` with origin = PREFIX_INCR/POSTFIX_INCR/PREFIX_DECR/POSTFIX_DECR
 * - The value is a call to `inc()` or `dec()` on the variable
 *
 * This lowering transforms the `inc()`/`dec()` calls to binary `+ 1` or `- 1` operations.
 */
class IncrementDecrementLowering(
    private val context: BrsIrBackendContext
) : FileLoweringPass {

    override fun lower(irFile: IrFile) {
        irFile.transformChildrenVoid(IncrementDecrementTransformer())
    }

    private inner class IncrementDecrementTransformer : IrElementTransformerVoid() {

        /**
         * For postfix increment/decrement blocks, we DON'T transform the structure here.
         * We just let the children (inc()/dec() calls) be transformed.
         * The BrightScript transformer will handle the block structure by detecting
         * the POSTFIX_INCR/POSTFIX_DECR origin.
         */
        override fun visitBlock(expression: IrBlock): IrExpression {
            // Just transform children, don't change the block structure
            // The POSTFIX_INCR/POSTFIX_DECR origin is preserved for the transformer
            return super.visitBlock(expression)
        }

        override fun visitSetValue(expression: IrSetValue): IrExpression {
            val origin = expression.origin

            // Check if this is an increment/decrement operation
            if (origin == IrStatementOrigin.PREFIX_INCR ||
                origin == IrStatementOrigin.POSTFIX_INCR ||
                origin == IrStatementOrigin.PREFIX_DECR ||
                origin == IrStatementOrigin.POSTFIX_DECR) {

                // Transform the value expression (the inc()/dec() call)
                val transformedValue = transformIncrementCall(expression.value, origin)
                if (transformedValue != null) {
                    expression.value = transformedValue
                }
            }

            // Continue transforming children
            return super.visitSetValue(expression)
        }

        override fun visitSetField(expression: IrSetField): IrExpression {
            val origin = expression.origin

            // Check if this is an increment/decrement operation
            if (origin == IrStatementOrigin.PREFIX_INCR ||
                origin == IrStatementOrigin.POSTFIX_INCR ||
                origin == IrStatementOrigin.PREFIX_DECR ||
                origin == IrStatementOrigin.POSTFIX_DECR) {

                // Transform the value expression (the inc()/dec() call)
                val transformedValue = transformIncrementCall(expression.value, origin)
                if (transformedValue != null) {
                    expression.value = transformedValue
                }
            }

            // Continue transforming children
            return super.visitSetField(expression)
        }

        override fun visitCall(expression: IrCall): IrExpression {
            // Also handle standalone inc()/dec() calls that might not be wrapped in a set
            // This can happen in certain IR patterns
            val function = expression.symbol.owner
            val functionName = function.name.asString()

            if ((functionName == "inc" || functionName == "dec") &&
                function.valueParameters.isEmpty() &&
                expression.dispatchReceiver != null) {

                // Transform inc()/dec() call to binary operation
                val receiver = expression.dispatchReceiver!!
                val isIncrement = functionName == "inc"

                // First transform the receiver in case it has nested inc/dec
                val transformedReceiver = receiver.transform(this, null)

                return createBinaryOperation(
                    transformedReceiver,
                    expression.type,
                    isIncrement,
                    expression.startOffset,
                    expression.endOffset
                )
            }

            return super.visitCall(expression)
        }

        /**
         * Transforms an inc()/dec() call to a binary + 1 or - 1 operation.
         */
        private fun transformIncrementCall(
            value: IrExpression,
            origin: IrStatementOrigin
        ): IrExpression? {
            // The value should be a call to inc() or dec()
            if (value !is IrCall) return null

            val function = value.symbol.owner
            val functionName = function.name.asString()

            // Verify this is inc() or dec()
            if (functionName != "inc" && functionName != "dec") return null

            // Get the operand (the dispatch receiver of inc()/dec())
            val operand = value.dispatchReceiver ?: return null

            // Determine if we're incrementing or decrementing
            val isIncrement = when (origin) {
                IrStatementOrigin.PREFIX_INCR, IrStatementOrigin.POSTFIX_INCR -> true
                IrStatementOrigin.PREFIX_DECR, IrStatementOrigin.POSTFIX_DECR -> false
                else -> functionName == "inc"
            }

            // Transform the operand in case it has nested increment/decrement
            val transformedOperand = operand.transform(this, null)

            return createBinaryOperation(
                transformedOperand,
                value.type,
                isIncrement,
                value.startOffset,
                value.endOffset
            )
        }

        /**
         * Creates a binary operation: operand + 1 or operand - 1
         */
        private fun createBinaryOperation(
            operand: IrExpression,
            resultType: IrType,
            isIncrement: Boolean,
            startOffset: Int,
            endOffset: Int
        ): IrExpression {
            // Create the constant 1 with the appropriate type
            val oneConst = when {
                resultType.isInt() -> IrConstImpl.int(startOffset, endOffset, resultType, 1)
                resultType.isLong() -> IrConstImpl.long(startOffset, endOffset, resultType, 1L)
                resultType.isShort() -> IrConstImpl.short(startOffset, endOffset, resultType, 1)
                resultType.isByte() -> IrConstImpl.byte(startOffset, endOffset, resultType, 1)
                resultType.isFloat() -> IrConstImpl.float(startOffset, endOffset, resultType, 1.0f)
                resultType.isDouble() -> IrConstImpl.double(startOffset, endOffset, resultType, 1.0)
                resultType.isChar() -> {
                    // For Char, we'll use Int and the result will be cast
                    IrConstImpl.int(startOffset, endOffset, context.irBuiltIns.intType, 1)
                }
                else -> {
                    // For other types (like user-defined types with inc/dec),
                    // fall back to Int
                    IrConstImpl.int(startOffset, endOffset, context.irBuiltIns.intType, 1)
                }
            }

            // Find the appropriate plus/minus function
            val operatorSymbol = if (isIncrement) {
                findPlusOperator(resultType)
            } else {
                findMinusOperator(resultType)
            }

            // If we found a built-in operator, use IrCall
            if (operatorSymbol != null) {
                return IrCallImpl(
                    startOffset = startOffset,
                    endOffset = endOffset,
                    type = resultType,
                    symbol = operatorSymbol,
                    typeArgumentsCount = 0,
                    origin = if (isIncrement) IrStatementOrigin.PLUS else IrStatementOrigin.MINUS
                ).apply {
                    dispatchReceiver = operand
                    putValueArgument(0, oneConst)
                }
            }

            // Fallback: use Int.plus/minus
            val fallbackOperator = context.irBuiltIns.getBinaryOperator(
                Name.identifier(if (isIncrement) "plus" else "minus"),
                context.irBuiltIns.intType,
                context.irBuiltIns.intType
            )

            return IrCallImpl(
                startOffset = startOffset,
                endOffset = endOffset,
                type = resultType,
                symbol = fallbackOperator,
                typeArgumentsCount = 0,
                origin = if (isIncrement) IrStatementOrigin.PLUS else IrStatementOrigin.MINUS
            ).apply {
                dispatchReceiver = operand
                putValueArgument(0, oneConst)
            }
        }

        /**
         * Finds the plus operator symbol for the given type.
         */
        private fun findPlusOperator(type: IrType): org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol? {
            return try {
                context.irBuiltIns.getBinaryOperator(
                    Name.identifier("plus"),
                    type,
                    type
                )
            } catch (e: Exception) {
                null
            }
        }

        /**
         * Finds the minus operator symbol for the given type.
         */
        private fun findMinusOperator(type: IrType): org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol? {
            return try {
                context.irBuiltIns.getBinaryOperator(
                    Name.identifier("minus"),
                    type,
                    type
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
