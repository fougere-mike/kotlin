/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.lower

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.backend.brs.BrsIrBackendContext
import org.jetbrains.kotlin.ir.backend.brs.lower.coroutines.BrsStatementOrigins
import org.jetbrains.kotlin.ir.backend.brs.transformers.irToBrs.hasInterfaceFieldAnnotation
import org.jetbrains.kotlin.ir.builders.declarations.buildVariable
import org.jetbrains.kotlin.ir.declarations.IrDeclarationBase
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrTypeOperator
import org.jetbrains.kotlin.ir.expressions.impl.IrBlockImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrSetFieldImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrTypeOperatorCallImpl
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.Name

/**
 * Component constructor calls → node creation (spec 2026-09-04-component-lifecycle §5.7).
 *
 * `DetailsScreen(id, row)` becomes
 *
 * ```
 * {
 *   val n = brsCreateComponent<DetailsScreen>()     // codegen intrinsic → CreateObject("roSGNode", "DetailsScreen")
 *   n.airingId = id                                   // COMPONENT_INPUT_WRITE: plain node-field set on the HANDLE
 *   n.row = row
 *   kotlinLifecycleMarkInputsReady(n)                 // LAST — proof of sanctioned construction
 *   n
 * }
 * ```
 *
 * Constructor inputs are the primary-constructor properties (the `isConstructorParameterProperty`
 * shape) that ALSO carry an `@SG*Field` annotation — only those have an XML interface field on
 * the node; a plain `val` parameter has no field to write, so its write is skipped (a write to
 * an undeclared node field is a silent drop on device). Each argument goes to the field of the
 * property whose backing-field initializer reads THAT parameter — the IR link, never the name
 * (an alias `@SGStringField val b: String = a` maps `a` → field `b`).
 *
 * Components with no inputs lower to the bare create; the ready marker is written only when at
 * least one input write was emitted. Abstract classes are left alone (FIR rejects them).
 * Phase 0.0555 — alongside the runTask/sharedFrom call-site rewrites, before any coroutine
 * lowering. Before this pass the same call compiled to `<Class>_create_…_k_`, a function that
 * is never emitted ("Function is not defined" on device).
 *
 * The handle temporary gets a per-file ordinal (`__kotlinNewComponent_<n>`) so a constructor
 * call nested in another's argument (`Outer(Inner())`) cannot clobber the outer handle when
 * codegen hoists the inner block's statements ahead of the outer field write.
 */
class BrsComponentConstructorCallLowering(private val context: BrsIrBackendContext) : FileLoweringPass {

    override fun lower(irFile: IrFile) {
        val brsCreateComponent = context.brsSymbols.brsCreateComponentOrNull ?: return
        val markReady = context.brsSymbols.kotlinLifecycleMarkInputsReadyOrNull ?: return
        val roSGNode = context.brsSymbols.roSGNodeClassOrNull ?: return

        var ordinal = 0

        irFile.transformChildrenVoid(object : IrElementTransformerVoid() {

            // Nearest declaration parent for the synthesized temporary (BrsScopeRunBlockLowering's
            // currentFunction tracking, widened to classes/fields so property initializers get a
            // parent too).
            private var currentParent: IrDeclarationParent = irFile

            override fun visitDeclaration(declaration: IrDeclarationBase): IrStatement {
                if (declaration !is IrDeclarationParent) return super.visitDeclaration(declaration)
                val previous = currentParent
                currentParent = declaration
                try {
                    return super.visitDeclaration(declaration)
                } finally {
                    currentParent = previous
                }
            }

            override fun visitConstructorCall(expression: IrConstructorCall): IrExpression {
                expression.transformChildrenVoid(this)
                val constructor = expression.symbol.owner
                val irClass = constructor.parentAsClass
                if (!context.intrinsics.isSceneGraphComponent(irClass)) return expression
                if (irClass.modality == Modality.ABSTRACT) return expression
                if (!constructor.isPrimary) return expression

                val classType = irClass.defaultType
                val createCall = IrCallImpl(
                    expression.startOffset, expression.endOffset, classType, brsCreateComponent, typeArgumentsCount = 1,
                ).apply { putTypeArgument(0, classType) }

                ordinal += 1
                val handle = buildVariable(
                    parent = currentParent, startOffset = expression.startOffset, endOffset = expression.endOffset,
                    origin = IrDeclarationOrigin.IR_TEMPORARY_VARIABLE,
                    name = Name.identifier("__kotlinNewComponent_$ordinal"), type = classType,
                ).apply { initializer = createCall }
                fun handleRead() = IrGetValueImpl(expression.startOffset, expression.endOffset, classType, handle.symbol)

                val properties = irClass.declarations.filterIsInstance<IrProperty>()
                val statements = mutableListOf<IrStatement>(handle)
                var wroteInput = false
                for ((index, parameter) in constructor.valueParameters.withIndex()) {
                    // param → property by the IR link: the property whose backing field is
                    // initialized from THIS parameter (not by name).
                    val property = properties.firstOrNull { p ->
                        val init = p.backingField?.initializer?.expression as? IrGetValue
                        init?.symbol == parameter.symbol
                    } ?: continue
                    // Only @SG-annotated inputs have a node field to write.
                    if (!hasInterfaceFieldAnnotation(property)) continue
                    val field = property.backingField ?: continue
                    val argument = expression.getValueArgument(index) ?: continue
                    statements.add(
                        IrSetFieldImpl(
                            expression.startOffset, expression.endOffset, field.symbol, handleRead(), argument,
                            context.irBuiltIns.unitType, origin = BrsStatementOrigins.COMPONENT_INPUT_WRITE,
                        )
                    )
                    wroteInput = true
                }
                if (wroteInput) {
                    statements.add(
                        IrCallImpl(
                            expression.startOffset, expression.endOffset, context.irBuiltIns.unitType, markReady, typeArgumentsCount = 0,
                        ).apply {
                            putValueArgument(
                                0,
                                IrTypeOperatorCallImpl(
                                    expression.startOffset, expression.endOffset, roSGNode.defaultType,
                                    IrTypeOperator.IMPLICIT_CAST, roSGNode.defaultType, handleRead(),
                                )
                            )
                        }
                    )
                }
                statements.add(handleRead())
                return IrBlockImpl(expression.startOffset, expression.endOffset, classType, null, statements)
            }
        })
    }
}
