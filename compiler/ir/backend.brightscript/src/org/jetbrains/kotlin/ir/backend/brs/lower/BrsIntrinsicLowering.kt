/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.lower

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.backend.brs.BrsIrBackendContext
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.util.getAnnotation
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.FqName

/**
 * Lowering pass that handles BrightScript intrinsics that need to be processed
 * BEFORE callable reference lowering transforms function references.
 *
 * This pass handles:
 * - `brsName(::function)` - Extracts the mangled BrightScript name from a function reference
 *   and replaces the entire call with a string literal.
 *
 * This MUST run before:
 * - UpgradeCallableReferences (which transforms IrFunctionReference to IrRichFunctionReference)
 * - BrsCallableReferenceLowering (which transforms references into anonymous classes wrapped in IrBlock)
 *
 * After callable reference lowering, the original function reference information is lost
 * (it becomes an anonymous class), so we must extract the function name here while we can
 * still see the original IrFunctionReference.
 */
class BrsIntrinsicLowering(
    private val context: BrsIrBackendContext
) : FileLoweringPass {

    private val brsIntrinsicFqn = FqName("kotlin.brs.BrsIntrinsic")

    override fun lower(irFile: IrFile) {
        irFile.transformChildrenVoid(BrsIntrinsicTransformer())
    }

    private inner class BrsIntrinsicTransformer : IrElementTransformerVoid() {

        override fun visitCall(expression: IrCall): IrExpression {
            // First transform children (in case there are nested calls)
            expression.transformChildrenVoid(this)

            val function = expression.symbol.owner

            // Check if this function has @BrsIntrinsic annotation
            val annotation = function.getAnnotation(brsIntrinsicFqn) ?: return expression

            // Get the intrinsic name from the annotation
            val intrinsicName = (annotation.getValueArgument(0) as? IrConst)?.value as? String
                ?: return expression

            // Handle specific intrinsics
            return when (intrinsicName) {
                "brsIntrinsicFunctionName" -> transformBrsName(expression)
                else -> expression // Other intrinsics handled by IrToBrsTransformer
            }
        }

        /**
         * Transform brsName(::function) into a string literal with the mangled function name.
         *
         * The argument can be:
         * - IrFunctionReference: A direct function reference like ::myFunction
         * - IrRichFunctionReference: After UpgradeCallableReferences (but we run before that)
         * - IrBlock: After callable reference lowering (we can't handle this - it's too late)
         */
        private fun transformBrsName(expression: IrCall): IrExpression {
            val arg = expression.getValueArgument(0)

            val targetFunction: IrFunction? = when (arg) {
                is IrFunctionReference -> {
                    // Direct function reference: ::myFunction or this::myMethod
                    arg.symbol.owner
                }
                is IrRichFunctionReference -> {
                    // Rich function reference (after UpgradeCallableReferences)
                    // Use reflectionTargetSymbol if available, otherwise invokeFunction's symbol
                    arg.reflectionTargetSymbol?.owner as? IrFunction
                        ?: arg.invokeFunction
                }
                is IrBlock -> {
                    // This happens if callable reference lowering already ran
                    // We can't extract the function name from an anonymous class
                    context.reportError(
                        expression,
                        "brsName() received transformed function reference (IrBlock). " +
                        "BrsIntrinsicLowering must run before callable reference lowering."
                    )
                    null
                }
                else -> {
                    context.reportError(
                        expression,
                        "brsName() requires a function reference (::functionName), got ${arg?.javaClass?.simpleName}"
                    )
                    null
                }
            }

            if (targetFunction == null) {
                // Return a placeholder string to allow compilation to continue
                return IrConstImpl.string(
                    expression.startOffset,
                    expression.endOffset,
                    context.irBuiltIns.stringType,
                    "invalid_function_reference"
                )
            }

            // Get the mangled BrightScript name for the function
            val mangledName = context.getBrsName(targetFunction)

            // Replace the entire call with a string literal
            return IrConstImpl.string(
                expression.startOffset,
                expression.endOffset,
                context.irBuiltIns.stringType,
                mangledName
            )
        }
    }
}
