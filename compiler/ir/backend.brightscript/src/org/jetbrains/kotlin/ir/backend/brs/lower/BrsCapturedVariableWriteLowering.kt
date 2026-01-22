/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.lower

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.backend.common.lower.BOUND_VALUE_PARAMETER
import org.jetbrains.kotlin.backend.common.lower.BOUND_RECEIVER_PARAMETER
import org.jetbrains.kotlin.backend.common.lower.LocalDeclarationsLowering
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.backend.brs.BrsIrBackendContext
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrSetFieldImpl
import org.jetbrains.kotlin.ir.symbols.IrValueSymbol
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid

/**
 * Rewrites writes (IrSetValue) to captured variables in local classes to use IrSetField.
 *
 * LocalDeclarationsLowering creates fields for captured variables and rewrites reads (IrGetValue)
 * to IrGetField, but it does NOT rewrite writes (IrSetValue) to IrSetField. This works on JVM/JS
 * because captured variables are boxed and writes go through the box reference.
 *
 * For BrightScript, we need explicit field access for writes too, since there's no boxing mechanism.
 * This lowering runs AFTER LocalDeclarationsLowering and rewrites remaining IrSetValue expressions
 * that target captured variables to use IrSetField instead.
 *
 * The key insight is that after LocalDeclarationsLowering:
 * - The class has constructor parameters for captured values (origin = BOUND_VALUE_PARAMETER)
 * - The class has fields to store these captured values (origin = FIELD_FOR_CAPTURED_VALUE)
 * - Reads (IrGetValue) are transformed to IrGetField
 * - Writes (IrSetValue) are NOT transformed - they still reference the original outer variable
 *
 * We need to find IrSetValue expressions where the target symbol matches a captured variable
 * and transform them to IrSetField using the corresponding field.
 */
class BrsCapturedVariableWriteLowering(
    private val context: BrsIrBackendContext
) : FileLoweringPass {

    override fun lower(irFile: IrFile) {
        irFile.transformChildrenVoid(ClassTransformer())
    }

    private inner class ClassTransformer : IrElementTransformerVoid() {
        override fun visitClass(declaration: IrClass): IrStatement {
            // Find constructor parameters that are for captured values
            // These have origin BOUND_VALUE_PARAMETER or BOUND_RECEIVER_PARAMETER
            val primaryConstructor = declaration.declarations
                .filterIsInstance<IrConstructor>()
                .firstOrNull { it.isPrimary }

            if (primaryConstructor == null) {
                return super.visitClass(declaration)
            }

            val capturedParams = primaryConstructor.valueParameters.filter { param ->
                param.origin == BOUND_VALUE_PARAMETER || param.origin == BOUND_RECEIVER_PARAMETER
            }

            if (capturedParams.isEmpty()) {
                return super.visitClass(declaration)
            }

            // Find fields for captured values
            // These have origin FIELD_FOR_CAPTURED_VALUE and names matching the constructor params
            val capturedFieldOrigin = LocalDeclarationsLowering.DECLARATION_ORIGIN_FIELD_FOR_CAPTURED_VALUE
            val capturedFields = declaration.declarations
                .filterIsInstance<IrField>()
                .filter { it.origin == capturedFieldOrigin }

            if (capturedFields.isEmpty()) {
                return super.visitClass(declaration)
            }

            // Build a map from field name (stripped of prefix) to field
            val fieldsByName = capturedFields.associateBy { field ->
                // Field name is like "$varName" or "_varName" (after sanitization)
                field.name.asString().removePrefix("\$").removePrefix("_")
            }

            // Transform the class members with the captured field information
            declaration.declarations.forEach { member ->
                if (member is IrSimpleFunction && !member.isFakeOverride) {
                    val dispatchReceiver = member.dispatchReceiverParameter ?: return@forEach
                    member.body?.transformChildrenVoid(
                        VariableWriteTransformer(fieldsByName, dispatchReceiver)
                    )
                }
            }

            // Continue visiting nested classes
            return super.visitClass(declaration)
        }
    }

    private inner class VariableWriteTransformer(
        private val fieldsByName: Map<String, IrField>,
        private val dispatchReceiver: IrValueParameter
    ) : IrElementTransformerVoid() {

        override fun visitSetValue(expression: IrSetValue): IrExpression {
            val variable = expression.symbol.owner
            val varName = variable.name.asString()

            // Check if this variable has a corresponding captured field
            val field = fieldsByName[varName]
            if (field != null) {
                // Transform to IrSetField
                return IrSetFieldImpl(
                    expression.startOffset,
                    expression.endOffset,
                    field.symbol,
                    IrGetValueImpl(
                        expression.startOffset,
                        expression.endOffset,
                        dispatchReceiver.type,
                        dispatchReceiver.symbol
                    ),
                    expression.value.transform(this, null),
                    context.irBuiltIns.unitType,
                    expression.origin
                )
            }

            return super.visitSetValue(expression)
        }
    }
}
