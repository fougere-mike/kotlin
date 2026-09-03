/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.lower

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.ir.backend.brs.BrsIrBackendContext
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.typeOrNull
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.name.BrsStandardClassIds

/**
 * Routes StateFlow/Flow interface access through the flow klib's static access
 * layer (Doorbells.kt; flow-program spec §6 implementation note):
 *
 *  1. `value` accessor calls whose accessor resolves to (or overrides)
 *     `StateFlow.value` / `MutableStateFlow.value` →
 *     `stateFlowGetValue(receiver)` / `stateFlowSetValue(receiver, value)`;
 *  2. `Flow.collect(collector)` MEMBER calls (incl. fake overrides on
 *     subtypes) → `flowCollectDispatch(receiver, collector)`
 *     (suspend-for-suspend, the 0.05x convention).
 *
 * WHY (spec §6): interface-receiver member/accessor calls are fn-slot dispatch
 * on this backend — they record no include-closure dependency (the doorbell
 * machinery would be missing from the collecting component's scripts), and on
 * a shared-VM-held StateFlow the slot fn-ref would cross the SetRef graph on
 * the disclaimed path the SharedService program forbids. The statics are
 * ordinary top-level calls: dependency-recorded at emission, no slot read.
 *
 * Matching is override-based, not receiver-type-based: an accessor/function
 * matches when IT or anything it (transitively) overrides is declared on the
 * flow interfaces — precise for fake overrides and user subtypes, and it fails
 * toward NOT rewriting on anything else (the member path still works
 * same-component; the task-brief law). Foreign StateFlow implementations are
 * still correct through the statics' member-call fallbacks.
 *
 * Guards: user modules only. The flow klib's own compile
 * (`kotlin.coroutines.flow`) is EXEMPT — its internals already route through
 * the statics in source, and the statics' deliberate member calls
 * (flowCollectDispatch's cold-flow else-branch, the foreign-impl fallbacks)
 * must stay member calls: rewriting them would recurse the dispatch into
 * itself. OrNull-bail (BrsSharedFromCallLowering template) covers modules
 * compiled without the kotlin-flow-brs klib.
 */
class BrsFlowAccessLowering(
    private val context: BrsIrBackendContext
) : FileLoweringPass {

    private val flowFqName = BrsStandardClassIds.Flow.flowInterface.asSingleFqName()
    private val stateFlowFqName = BrsStandardClassIds.Flow.stateFlowInterface.asSingleFqName()
    private val mutableStateFlowFqName = BrsStandardClassIds.Flow.mutableStateFlowInterface.asSingleFqName()
    private val collectName = BrsStandardClassIds.Callables.flowCollect.callableName.asString()

    override fun lower(irFile: IrFile) {
        if (context.isStdlibCompilation) return
        if (irFile.packageFqName == BrsStandardClassIds.BASE_COROUTINES_FLOW_PACKAGE) return

        val getValue = context.brsSymbols.stateFlowGetValueOrNull ?: return
        val setValue = context.brsSymbols.stateFlowSetValueOrNull ?: return
        val collectDispatch = context.brsSymbols.flowCollectDispatchOrNull ?: return

        irFile.transformChildrenVoid(object : IrElementTransformerVoid() {
            override fun visitCall(expression: IrCall): IrExpression {
                expression.transformChildrenVoid(this)

                val callee = expression.symbol.owner
                val receiver = expression.dispatchReceiver ?: return expression

                if (callee.overridesFlowCollect()) {
                    val collector = expression.getValueArgument(0) ?: return expression
                    return IrCallImpl(
                        startOffset = expression.startOffset,
                        endOffset = expression.endOffset,
                        type = expression.type,
                        symbol = collectDispatch,
                        typeArgumentsCount = 1,
                    ).apply {
                        putTypeArgument(0, receiverElementType(receiver))
                        putValueArgument(0, receiver)
                        putValueArgument(1, collector)
                    }
                }

                if (callee.overridesStateFlowValueAccessor()) {
                    if (expression.valueArgumentsCount == 0) {
                        return IrCallImpl(
                            startOffset = expression.startOffset,
                            endOffset = expression.endOffset,
                            type = expression.type,
                            symbol = getValue,
                            typeArgumentsCount = 1,
                        ).apply {
                            putTypeArgument(0, expression.type)
                            putValueArgument(0, receiver)
                        }
                    }
                    val newValue = expression.getValueArgument(0) ?: return expression
                    return IrCallImpl(
                        startOffset = expression.startOffset,
                        endOffset = expression.endOffset,
                        type = expression.type,
                        symbol = setValue,
                        typeArgumentsCount = 1,
                    ).apply {
                        putTypeArgument(0, newValue.type)
                        putValueArgument(0, receiver)
                        putValueArgument(1, newValue)
                    }
                }

                return expression
            }
        })
    }

    /** The receiver's element type argument (Flow<T> → T); anyN when unavailable. */
    private fun receiverElementType(receiver: IrExpression): IrType {
        val simple = receiver.type as? IrSimpleType
        return simple?.arguments?.getOrNull(0)?.typeOrNull ?: context.irBuiltIns.anyNType
    }

    /** True when this function is (or transitively overrides) `Flow.collect`. */
    private fun IrSimpleFunction.overridesFlowCollect(): Boolean {
        if (name.asString() == collectName && parentClassOrNull?.fqNameWhenAvailable == flowFqName) return true
        return overriddenSymbols.any { it.owner.overridesFlowCollect() }
    }

    /**
     * True when this function is (or transitively overrides) an accessor of
     * `StateFlow.value` / `MutableStateFlow.value`.
     */
    private fun IrSimpleFunction.overridesStateFlowValueAccessor(): Boolean {
        if (correspondingPropertySymbol?.owner?.name?.asString() == "value") {
            val parentFqName = parentClassOrNull?.fqNameWhenAvailable
            if (parentFqName == stateFlowFqName || parentFqName == mutableStateFlowFqName) return true
        }
        return overriddenSymbols.any { it.owner.overridesStateFlowValueAccessor() }
    }
}
