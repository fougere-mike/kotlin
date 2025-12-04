/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.lower

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.ir.backend.brs.BrsIrBackendContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrBlockImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrBranchImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrWhenImpl
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid

/**
 * Extracts assignment expressions from expression contexts.
 *
 * BrightScript does not support assignment expressions - assignments are statements only.
 * This lowering transforms patterns like:
 *
 * ```kotlin
 * if ((count = count + 1) > 1) { ... }
 * ```
 *
 * Into:
 *
 * ```kotlin
 * count = count + 1
 * if (count > 1) { ... }
 * ```
 *
 * This runs after IncrementDecrementLowering, which transforms `++count` into `count = count + 1`.
 *
 * For prefix increment (++i), the value after assignment is used.
 * For postfix increment (i++), the value before assignment is used (handled via temp variable in IR).
 */
class AssignmentExtractionLowering(
    private val context: BrsIrBackendContext
) : FileLoweringPass {

    override fun lower(irFile: IrFile) {
        irFile.transformChildrenVoid(AssignmentExtractionTransformer())
    }

    private inner class AssignmentExtractionTransformer : IrElementTransformerVoid() {

        /**
         * Transform calls - just recurse into children, extraction is done at visitWhen level.
         */
        override fun visitCall(expression: IrCall): IrExpression {
            expression.transformChildrenVoid(this)
            return expression
        }

        /**
         * Transform when expressions that may have assignments in conditions.
         * For IrWhen used as an if-statement with a single condition containing
         * an increment expression, we need to extract the increment before the when.
         */
        override fun visitWhen(expression: IrWhen): IrExpression {
            // First transform children recursively (this handles nested whens)
            expression.transformChildrenVoid(this)

            // Only handle the first branch's condition for extraction
            // (subsequent branches have short-circuit semantics)
            if (expression.branches.isEmpty()) {
                return expression
            }

            val firstBranch = expression.branches[0]
            val condition = firstBranch.condition

            // Check if condition contains an assignment (e.g., from prefix increment)
            val (newCondition, statements) = extractAssignmentFromExpression(condition)

            if (statements.isEmpty()) {
                return expression
            }

            // Update the branch condition with the extracted expression
            val newBranch = IrBranchImpl(
                firstBranch.startOffset,
                firstBranch.endOffset,
                newCondition,
                firstBranch.result
            )

            // Create a new when with the updated first branch
            val newWhen = IrWhenImpl(
                expression.startOffset,
                expression.endOffset,
                expression.type,
                expression.origin
            ).apply {
                branches.add(newBranch)
                // Add remaining branches unchanged
                for (i in 1 until expression.branches.size) {
                    branches.add(expression.branches[i])
                }
            }

            // Return block with extracted statements followed by the when
            return IrBlockImpl(
                expression.startOffset,
                expression.endOffset,
                expression.type,
                null,
                statements + newWhen
            )
        }

        /**
         * Extract assignments from an expression, recursively checking for blocks.
         */
        private fun extractAssignmentFromExpression(expression: IrExpression): Pair<IrExpression, List<IrStatement>> {
            return when (expression) {
                is IrCall -> {
                    // Check value arguments for increment blocks
                    val extractedStatements = mutableListOf<IrStatement>()
                    var modified = false

                    for (i in 0 until expression.valueArgumentsCount) {
                        val arg = expression.getValueArgument(i)
                        if (arg != null) {
                            val (newArg, stmts) = extractAssignment(arg)
                            if (stmts.isNotEmpty()) {
                                extractedStatements.addAll(stmts)
                                expression.putValueArgument(i, newArg)
                                modified = true
                            }
                        }
                    }

                    // Also check dispatch/extension receivers
                    val dr = expression.dispatchReceiver
                    if (dr != null) {
                        val (newDr, stmts) = extractAssignment(dr)
                        if (stmts.isNotEmpty()) {
                            extractedStatements.addAll(0, stmts) // Receivers evaluated first
                            expression.dispatchReceiver = newDr
                            modified = true
                        }
                    }

                    if (modified) {
                        Pair(expression, extractedStatements)
                    } else {
                        Pair(expression, emptyList())
                    }
                }
                else -> extractAssignment(expression)
            }
        }

        /**
         * Extract assignment expressions from an expression, returning the modified expression
         * and a list of statements to prepend.
         */
        private fun extractAssignment(expression: IrExpression): Pair<IrExpression, List<IrStatement>> {
            return when (expression) {
                is IrSetValue -> {
                    // Direct assignment expression - extract it
                    val origin = expression.origin

                    // For prefix increment, extract the assignment and use the variable value
                    if (origin == IrStatementOrigin.PREFIX_INCR ||
                        origin == IrStatementOrigin.PREFIX_DECR) {
                        // Extract the assignment as a statement
                        // Return a reference to the variable
                        val getValue = IrGetValueImpl(
                            expression.startOffset,
                            expression.endOffset,
                            expression.symbol.owner.type,
                            expression.symbol,
                            null
                        )
                        Pair(getValue, listOf(expression))
                    } else {
                        // For other assignments, also extract
                        val getValue = IrGetValueImpl(
                            expression.startOffset,
                            expression.endOffset,
                            expression.symbol.owner.type,
                            expression.symbol,
                            null
                        )
                        Pair(getValue, listOf(expression))
                    }
                }

                is IrSetField -> {
                    // Field assignment - also need to extract
                    // For simplicity, we'll leave field assignments for now
                    // and handle the common case of variable assignments
                    Pair(expression, emptyList())
                }

                is IrBlock -> {
                    // Check if this is a prefix or postfix increment block
                    val origin = expression.origin
                    when (origin) {
                        IrStatementOrigin.PREFIX_INCR, IrStatementOrigin.PREFIX_DECR -> {
                            // Prefix increment block structure:
                            // IrBlock {
                            //   IrSetValue(count, count + 1)  // assignment
                            //   IrGetValue(count)             // return new value
                            // }
                            // We need to extract the assignment and use the get as the expression
                            val statements = expression.statements
                            if (statements.size == 2) {
                                val assignment = statements[0]
                                val getValue = statements[1]
                                if (assignment is IrSetValue && getValue is IrExpression) {
                                    Pair(getValue, listOf(assignment))
                                } else {
                                    Pair(expression, emptyList())
                                }
                            } else {
                                Pair(expression, emptyList())
                            }
                        }
                        IrStatementOrigin.POSTFIX_INCR, IrStatementOrigin.POSTFIX_DECR -> {
                            // Postfix increment block structure:
                            // IrBlock {
                            //   val <unary> = oldValue
                            //   assignment
                            //   <unary>  (return old value)
                            // }
                            // We need to extract all statements except the last (the return value)
                            // and keep only the return value as the expression
                            val statements = expression.statements
                            if (statements.size >= 2) {
                                val precedingStatements = statements.dropLast(1)
                                val lastExpr = statements.last()
                                if (lastExpr is IrExpression) {
                                    Pair(lastExpr, precedingStatements)
                                } else {
                                    Pair(expression, emptyList())
                                }
                            } else {
                                Pair(expression, emptyList())
                            }
                        }
                        else -> {
                            // Regular block - don't extract
                            Pair(expression, emptyList())
                        }
                    }
                }

                else -> Pair(expression, emptyList())
            }
        }
    }
}
