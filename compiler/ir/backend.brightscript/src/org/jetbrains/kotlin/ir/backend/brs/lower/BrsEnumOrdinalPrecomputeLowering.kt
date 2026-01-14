/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.lower

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.backend.brs.BrsIrBackendContext
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrEnumConstructorCall
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid

/**
 * Pre-computes enum ordinals and stores them in context.mapping.
 *
 * This lowering pass runs early in the pipeline to populate enum entry ordinal
 * and name mappings before other passes need them. This enables:
 * - @BrsConstant evaluation to reference `SomeEnum.ENTRY.ordinal`
 * - When-statement optimization to use ordinal comparisons
 * - Constant inlining at all usage sites
 *
 * Must run before BrsConstantEvaluationLowering and EnumLowering.
 */
class BrsEnumOrdinalPrecomputeLowering(
    private val context: BrsIrBackendContext
) : FileLoweringPass {

    override fun lower(irFile: IrFile) {
        irFile.acceptVoid(object : IrVisitorVoid() {
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }

            override fun visitClass(declaration: IrClass) {
                if (declaration.kind == ClassKind.ENUM_CLASS) {
                    precomputeEnumOrdinals(declaration)
                }
                super.visitClass(declaration)
            }
        })
    }

    /**
     * Pre-computes ordinals, names, and constant property values for all entries in an enum class.
     */
    private fun precomputeEnumOrdinals(enumClass: IrClass) {
        val enumEntries = enumClass.declarations.filterIsInstance<IrEnumEntry>()

        enumEntries.forEachIndexed { ordinal, entry ->
            // Store ordinal value
            context.mapping.enumEntryOrdinals[entry] = ordinal

            // Store name string
            context.mapping.enumEntryNames[entry] = entry.name.asString()

            // Extract constant property values from enum constructor arguments
            entry.initializerExpression?.let { init ->
                val initExpr = init.expression
                if (initExpr is IrEnumConstructorCall) {
                    val constantProps = mutableMapOf<String, Any?>()
                    val constructor = initExpr.symbol.owner

                    for (i in 0 until initExpr.valueArgumentsCount) {
                        initExpr.getValueArgument(i)?.let { arg ->
                            if (arg is IrConst) {
                                val paramName = constructor.valueParameters.getOrNull(i)?.name?.asString()
                                if (paramName != null) {
                                    constantProps[paramName] = arg.value
                                }
                            }
                        }
                    }

                    if (constantProps.isNotEmpty()) {
                        context.mapping.enumEntryConstantProperties[entry] = constantProps
                    }
                }
            }
        }
    }
}
