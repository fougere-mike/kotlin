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
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.BrsStandardClassIds

/**
 * Rewrites reified `sharedFrom<T>(node[, key])` / `sharedFromOrNull<T>(...)`
 * call sites into `sharedAcquire(node, key, "<ClassName>", orNull)`.
 *
 * sharedFrom is a klib inline function and BrsInlineFunctionResolver cannot
 * inline klib functions, so user call sites reach the backend un-inlined —
 * the compiled wrapper body's own `brsSharedServiceName<T>()` has an
 * unsubstituted type parameter and would hit the runtime stub. This pass
 * performs exactly the one inline step the resolver cannot (the runTask
 * precedent, BrsRunTaskCallLowering): the call site's concrete type argument
 * resolves to a class-name string constant through [BrsIrBackendContext.getBrsName]
 * — the SAME naming that emits `__proto` heads and `is`-check type names, so
 * publish keys, acquire keys, and the acquire-side type check all derive one
 * string. The rest of the semantics stay in the real stdlib function
 * `sharedAcquire` — keep its signature in sync with SharedStash.kt.
 *
 * The no-key overloads pass the class name as the key (the default-key law:
 * both ends are typed, so both ends derive the class name). Calls with an
 * unsubstituted type parameter (a user-written reified forwarder's body) are
 * left alone, mirroring the createComponent intrinsic.
 */
class BrsSharedFromCallLowering(
    private val context: BrsIrBackendContext
) : FileLoweringPass {

    private val sharedFromFqName = BrsStandardClassIds.Callables.sharedFrom.asSingleFqName()
    private val sharedFromOrNullFqName = BrsStandardClassIds.Callables.sharedFromOrNull.asSingleFqName()

    override fun lower(irFile: IrFile) {
        val sharedAcquire = context.brsSymbols.sharedAcquireOrNull ?: return

        irFile.transformChildrenVoid(object : IrElementTransformerVoid() {
            override fun visitCall(expression: IrCall): IrExpression {
                expression.transformChildrenVoid(this)

                val orNull = when (expression.symbol.owner.fqNameWhenAvailable) {
                    sharedFromFqName -> false
                    sharedFromOrNullFqName -> true
                    else -> return expression
                }
                val serviceType = expression.typeArguments.getOrNull(0) ?: return expression
                val serviceClass = serviceType.classOrNull?.owner ?: return expression
                val node = expression.getValueArgument(0) ?: return expression
                val typeName = context.getBrsName(serviceClass)

                fun typeNameConst(): IrExpression = IrConstImpl.string(
                    expression.startOffset, expression.endOffset, context.irBuiltIns.stringType, typeName
                )

                // Keyed overloads carry the caller's key; keyless ones derive
                // the default key — the class name, same as the type check's.
                val key = if (expression.valueArgumentsCount > 1) {
                    expression.getValueArgument(1) ?: return expression
                } else {
                    typeNameConst()
                }

                return IrCallImpl(
                    startOffset = expression.startOffset,
                    endOffset = expression.endOffset,
                    type = expression.type,
                    symbol = sharedAcquire,
                    typeArgumentsCount = 1,
                ).apply {
                    putTypeArgument(0, serviceType)
                    putValueArgument(0, node)
                    putValueArgument(1, key)
                    putValueArgument(2, typeNameConst())
                    putValueArgument(
                        3,
                        IrConstImpl.boolean(
                            expression.startOffset, expression.endOffset, context.irBuiltIns.booleanType, orNull
                        )
                    )
                }
            }
        })
    }
}
