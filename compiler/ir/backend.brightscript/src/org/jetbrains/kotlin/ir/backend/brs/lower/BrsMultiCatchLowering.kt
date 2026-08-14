/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.lower

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.backend.brs.BrsIrBackendContext
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.impl.IrVariableImpl
import org.jetbrains.kotlin.ir.expressions.IrBranch
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrTry
import org.jetbrains.kotlin.ir.expressions.IrTypeOperator
import org.jetbrains.kotlin.ir.expressions.impl.IrBlockImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrBranchImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrCatchImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrElseBranchImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrThrowImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrTypeOperatorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrWhenImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrVariableSymbolImpl
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.Name

/**
 * Merges a multi-clause `try` into a single catch-all clause with explicit
 * `is`-dispatch, BEFORE any other try handling runs.
 *
 * Both downstream paths lose clause dispatch on multi-catch: the suspend
 * state machine (BrsStateMachineBuilder.visitTry) emits every clause into one
 * catch state with no type tests — the first clause runs unconditionally and
 * the rest are dead code — and the non-suspend emitter
 * (IrStatementToBrsTransformer.visitTry) drops every clause after the first
 * outright, because BrsTry carries a single catch (BrightScript try/catch has
 * no typed clauses). Device-pinned during the Suite 8 ScopeHandle bring-up;
 * golden: coroutines/suspendMultiCatch.
 *
 * The merged shape is exactly the device-proven idiom users write by hand
 * (single `catch (e: Throwable)` + `is`-chain):
 *
 * ```kotlin
 * try { ... }
 * catch (__caught: Throwable) {
 *     when {
 *         __caught is T1 -> { val e1 = __caught as T1; <body1> }
 *         __caught is T2 -> { val e2 = __caught as T2; <body2> }
 *         else -> throw __caught
 *     }
 * }
 * ```
 *
 * A clause typed exactly `kotlin.Throwable` becomes the unconditional else
 * arm — NOT an `is Throwable` test — so native BrightScript errors (which
 * carry no Kotlin __proto and fail every `is` test) still land in a
 * catch-all clause, matching today's single-catch behavior. Clauses after a
 * Throwable clause are unreachable by Kotlin semantics and are dropped. With
 * no Throwable clause, unmatched exceptions (including native errors)
 * rethrow — Kotlin's unmatched-clause semantics, and the part the old
 * emission silently inverted by swallowing everything into clause one.
 *
 * Runs at the top of the phase list, before any lambda/coroutine/try
 * lowering: everything downstream sees single-catch tries only. Single-catch
 * tries are untouched — including the pre-existing (and separately ledgered)
 * behavior that a single NARROW clause catches everything.
 */
class BrsMultiCatchLowering(
    private val context: BrsIrBackendContext
) : FileLoweringPass {

    override fun lower(irFile: IrFile) {
        var counter = 0

        irFile.transformChildrenVoid(object : IrElementTransformerVoid() {
            override fun visitTry(aTry: IrTry): IrExpression {
                aTry.transformChildrenVoid(this)
                if (aTry.catches.size <= 1) return aTry

                counter += 1
                val mergedName = if (counter == 1) "__caught" else "__caught$counter"
                val throwableType = context.irBuiltIns.throwableType
                val merged = IrVariableImpl(
                    startOffset = UNDEFINED_OFFSET,
                    endOffset = UNDEFINED_OFFSET,
                    origin = IrDeclarationOrigin.CATCH_PARAMETER,
                    symbol = IrVariableSymbolImpl(),
                    name = Name.identifier(mergedName),
                    type = throwableType,
                    isVar = false,
                    isConst = false,
                    isLateinit = false,
                )

                fun readMerged() = IrGetValueImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, throwableType, merged.symbol)

                val branches = mutableListOf<IrBranch>()
                var haveCatchAll = false
                for (catch in aTry.catches) {
                    val clauseType = catch.catchParameter.type
                    val clauseParameter = catch.catchParameter.apply {
                        initializer = IrTypeOperatorCallImpl(
                            UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                            clauseType, IrTypeOperator.IMPLICIT_CAST, clauseType, readMerged()
                        )
                    }
                    val body = IrBlockImpl(catch.result.startOffset, catch.result.endOffset, catch.result.type).apply {
                        statements += clauseParameter
                        statements += catch.result
                    }
                    if (clauseType.classFqName?.asString() == "kotlin.Throwable") {
                        branches += IrElseBranchImpl(
                            IrConstImpl.boolean(UNDEFINED_OFFSET, UNDEFINED_OFFSET, context.irBuiltIns.booleanType, true),
                            body,
                        )
                        haveCatchAll = true
                        break // later clauses are unreachable
                    }
                    branches += IrBranchImpl(
                        startOffset = catch.startOffset,
                        endOffset = catch.endOffset,
                        condition = IrTypeOperatorCallImpl(
                            UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                            context.irBuiltIns.booleanType, IrTypeOperator.INSTANCEOF, clauseType, readMerged()
                        ),
                        result = body,
                    )
                }
                if (!haveCatchAll) {
                    branches += IrElseBranchImpl(
                        IrConstImpl.boolean(UNDEFINED_OFFSET, UNDEFINED_OFFSET, context.irBuiltIns.booleanType, true),
                        IrThrowImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, context.irBuiltIns.nothingType, readMerged()),
                    )
                }

                val dispatch = IrWhenImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, aTry.type, null).apply {
                    this.branches += branches
                }

                aTry.catches.clear()
                aTry.catches += IrCatchImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, merged, dispatch)
                return aTry
            }
        })
    }
}
