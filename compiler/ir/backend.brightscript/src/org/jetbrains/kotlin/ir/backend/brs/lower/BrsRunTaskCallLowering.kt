/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.lower

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.ir.backend.brs.BrsIrBackendContext
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.BrsStandardClassIds

/**
 * Rewrites reified `runTask<T>(configure)` call sites into
 * `runTaskImpl(brsCreateComponent<T>(), configure)`.
 *
 * runTask is a klib inline function and BrsInlineFunctionResolver cannot
 * inline klib functions, so user call sites reach the backend un-inlined —
 * the compiled runTask body's own `createComponent<T>()` has an unsubstituted
 * type parameter and would hit the runtime stub. This pass performs exactly
 * the one inline step the resolver cannot: the call site's concrete type
 * argument moves onto a `brsCreateComponent<T>()` call (lowered to
 * `CreateObject("roSGNode", name)` by the codegen intrinsic, which also
 * reports the IR-level error for non-concrete classes when the FIR checker
 * was suppressed), and the rest of runTask's semantics stay in the real
 * stdlib function `runTaskImpl` — keep its signature in sync with
 * TaskRunner.kt.
 *
 * Calls with an unsubstituted type parameter (a user-written reified
 * forwarder's body) are left alone, mirroring the createComponent intrinsic.
 * Suspend semantics are preserved: one suspend call is replaced by one
 * suspend call, before any coroutine lowering runs.
 */
class BrsRunTaskCallLowering(
    private val context: BrsIrBackendContext
) : FileLoweringPass {

    private val runTaskFqName = BrsStandardClassIds.Callables.runTask.asSingleFqName()

    override fun lower(irFile: IrFile) {
        val runTaskImpl = context.brsSymbols.runTaskImplOrNull ?: return
        val brsCreateComponent = context.brsSymbols.brsCreateComponentOrNull ?: return

        irFile.transformChildrenVoid(object : IrElementTransformerVoid() {
            override fun visitCall(expression: IrCall): IrExpression {
                expression.transformChildrenVoid(this)

                if (expression.symbol.owner.fqNameWhenAvailable != runTaskFqName) return expression
                val taskType = expression.typeArguments.getOrNull(0) ?: return expression
                if (taskType.classOrNull == null) return expression
                val configure = expression.getValueArgument(0) ?: return expression

                val createCall = IrCallImpl(
                    startOffset = expression.startOffset,
                    endOffset = expression.endOffset,
                    type = taskType,
                    symbol = brsCreateComponent,
                    typeArgumentsCount = 1,
                ).apply {
                    putTypeArgument(0, taskType)
                }

                return IrCallImpl(
                    startOffset = expression.startOffset,
                    endOffset = expression.endOffset,
                    type = expression.type,
                    symbol = runTaskImpl,
                    typeArgumentsCount = 1,
                ).apply {
                    putTypeArgument(0, taskType)
                    putValueArgument(0, createCall)
                    putValueArgument(1, configure)
                }
            }
        })
    }
}
