/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.lower

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.backend.brs.BrsIrBackendContext
import org.jetbrains.kotlin.ir.builders.declarations.buildVariable
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.expressions.IrSetValue
import org.jetbrains.kotlin.ir.expressions.IrTypeOperator
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetObjectValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrReturnImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrSetValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrTypeOperatorCallImpl
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrValueSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.Name

/**
 * Shared machinery for the two capture-lifting rewrites — the ScopeHandle
 * `run { block }` lift ([BrsScopeRunBlockLowering]) and the flowOn/spawnTask
 * task lift ([BrsFlowTaskLiftLowering]). Both move a literal region of user
 * code into a compiler-named top-level function whose single parameter is a
 * `captures: RoAssociativeArray?` bag: free values of the region are collected
 * here, re-declared at the lifted function's entry by AA lookup, and written
 * into the bag at the rewritten call site — captures therefore cross the
 * respective wire BY COPY (the marshallable-set laws both features share).
 */

/** A captured value: the out-of-scope symbol the lifted region reads plus its AA key / lifted-local name. */
internal class LiftCapture(val symbol: IrValueSymbol, val name: String)

/**
 * Free-value analysis of a literal region: every [IrGetValue]/[IrSetValue]
 * whose target is declared OUTSIDE the region, in first-reference order.
 * [preDeclared] seeds the declared set (a lifted lambda's own parameters);
 * declarations nested anywhere inside the region (locals, nested lambdas'
 * parameters) are recorded as declared when the walk reaches them, which is
 * always before their references in tree order. Names are the source names
 * (receivers become "this"), uniquified within the region when shadowing
 * produces duplicates.
 */
internal fun collectLiftCaptures(region: IrElement?, preDeclared: Collection<IrValueParameter>): List<LiftCapture> {
    val declared = mutableSetOf<IrValueSymbol>()
    preDeclared.forEach { declared.add(it.symbol) }

    val order = mutableListOf<IrValueSymbol>()
    val seen = mutableSetOf<IrValueSymbol>()

    fun record(symbol: IrValueSymbol) {
        if (symbol in declared || !seen.add(symbol)) return
        order += symbol
    }

    region?.acceptVoid(object : IrVisitorVoid() {
        override fun visitElement(element: IrElement) = element.acceptChildrenVoid(this)

        override fun visitVariable(declaration: IrVariable) {
            declared.add(declaration.symbol)
            declaration.acceptChildrenVoid(this)
        }

        override fun visitValueParameter(declaration: IrValueParameter) {
            declared.add(declaration.symbol)
            declaration.acceptChildrenVoid(this)
        }

        override fun visitGetValue(expression: IrGetValue) {
            record(expression.symbol)
        }

        override fun visitSetValue(expression: IrSetValue) {
            record(expression.symbol)
            expression.acceptChildrenVoid(this)
        }
    })

    val usedNames = mutableSetOf<String>()
    return order.map { symbol ->
        val raw = symbol.owner.name.asString()
        val base = when {
            raw == "<this>" || raw.startsWith("\$this") -> "this"
            else -> raw.replace("$", "_").replace("<", "").replace(">", "")
        }
        var name = base
        var suffix = 2
        while (!usedNames.add(name)) {
            name = "${base}_${suffix++}"
        }
        LiftCapture(symbol, name)
    }
}

/**
 * The lifted function's entry prologue: one local per capture, initialized by
 * reading the `captures` AA by name (`val shelfId = captures.lookup("shelfId")`
 * behind an IMPLICIT_CAST to the captured type). Returns the prologue
 * statements plus the original-symbol → local remapping for
 * [remapLiftedRegion].
 */
internal fun buildCaptureLocals(
    context: BrsIrBackendContext,
    lifted: IrSimpleFunction,
    capturesParam: IrValueParameter,
    captures: List<LiftCapture>,
    aaLookup: IrSimpleFunctionSymbol,
): Pair<List<IrStatement>, Map<IrValueSymbol, IrVariable>> {
    val statements = mutableListOf<IrStatement>()
    val remapping = mutableMapOf<IrValueSymbol, IrVariable>()
    for (capture in captures) {
        val capturedType = capture.symbol.owner.type
        val lookupCall = IrCallImpl(
            UNDEFINED_OFFSET, UNDEFINED_OFFSET,
            aaLookup.owner.returnType,
            aaLookup,
            typeArgumentsCount = 0,
        ).apply {
            dispatchReceiver = IrGetValueImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, capturesParam.type, capturesParam.symbol)
            putValueArgument(0, liftStringConst(context, capture.name))
        }
        val local = buildVariable(
            parent = lifted,
            startOffset = UNDEFINED_OFFSET,
            endOffset = UNDEFINED_OFFSET,
            origin = IrDeclarationOrigin.DEFINED,
            name = Name.identifier(capture.name),
            type = capturedType,
        ).apply {
            initializer = IrTypeOperatorCallImpl(
                UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                capturedType, IrTypeOperator.IMPLICIT_CAST, capturedType, lookupCall
            )
        }
        remapping[capture.symbol] = local
        statements += local
    }
    return statements to remapping
}

/**
 * Rewires a moved region in place: [IrGetValue]/[IrSetValue] of captured
 * symbols are remapped to the [buildCaptureLocals] locals, and — when
 * [retargetReturnsFrom] is given — [IrReturn]s targeting it are retargeted to
 * [retargetReturnsTo] (a lifted lambda's own returns must return from the
 * lifted function; nested lambdas' returns are left alone).
 */
internal fun remapLiftedRegion(
    region: IrElement,
    remapping: Map<IrValueSymbol, IrVariable>,
    retargetReturnsFrom: IrSimpleFunction? = null,
    retargetReturnsTo: IrSimpleFunction? = null,
) {
    region.transformChildrenVoid(object : IrElementTransformerVoid() {
        override fun visitGetValue(expression: IrGetValue): IrExpression {
            val local = remapping[expression.symbol] ?: return expression
            return IrGetValueImpl(expression.startOffset, expression.endOffset, local.type, local.symbol, expression.origin)
        }

        override fun visitSetValue(expression: IrSetValue): IrExpression {
            expression.transformChildrenVoid(this)
            val local = remapping[expression.symbol] ?: return expression
            return IrSetValueImpl(
                expression.startOffset, expression.endOffset, expression.type, local.symbol, expression.value, expression.origin
            )
        }

        override fun visitReturn(expression: IrReturn): IrExpression {
            expression.transformChildrenVoid(this)
            if (retargetReturnsFrom == null || retargetReturnsTo == null) return expression
            if (expression.returnTargetSymbol != retargetReturnsFrom.symbol) return expression
            return IrReturnImpl(
                expression.startOffset, expression.endOffset, expression.type, retargetReturnsTo.symbol, expression.value
            )
        }
    })
}

/**
 * The rewritten call site's captures bag: a temp variable initialized with
 * `RoAssociativeArray.create()`, parented to the enclosing function. Follow
 * with one [buildAddReplaceCall] per capture.
 */
internal fun buildCapturesVariable(
    tempName: String,
    aaCreate: IrSimpleFunction,
    enclosing: IrFunction,
): IrVariable {
    val companion = aaCreate.parent as IrClass
    val createCall = IrCallImpl(
        UNDEFINED_OFFSET, UNDEFINED_OFFSET,
        aaCreate.returnType,
        aaCreate.symbol,
        typeArgumentsCount = 0,
    ).apply {
        dispatchReceiver = IrGetObjectValueImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, companion.defaultType, companion.symbol)
    }
    return buildVariable(
        parent = enclosing,
        startOffset = UNDEFINED_OFFSET,
        endOffset = UNDEFINED_OFFSET,
        origin = IrDeclarationOrigin.IR_TEMPORARY_VARIABLE,
        name = Name.identifier(tempName),
        type = aaCreate.returnType,
    ).apply { initializer = createCall }
}

/** One `<capturesVar>.addReplace("<name>", <capturedValue>)` call for the rewritten call site. */
internal fun buildAddReplaceCall(
    context: BrsIrBackendContext,
    aaAddReplace: IrSimpleFunction,
    capturesVar: IrVariable,
    capture: LiftCapture,
): IrCall =
    IrCallImpl(
        UNDEFINED_OFFSET, UNDEFINED_OFFSET,
        aaAddReplace.returnType,
        aaAddReplace.symbol,
        typeArgumentsCount = 0,
    ).apply {
        dispatchReceiver = IrGetValueImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, capturesVar.type, capturesVar.symbol)
        putValueArgument(0, liftStringConst(context, capture.name))
        putValueArgument(
            1,
            IrGetValueImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, capture.symbol.owner.type, capture.symbol)
        )
    }

/** `invalid` for a zero-capture rewrite: the lifted function's `captures` parameter accepts null. */
internal fun liftNullCaptures(capturesType: IrType): IrExpression =
    IrConstImpl.constNull(UNDEFINED_OFFSET, UNDEFINED_OFFSET, capturesType)

internal fun liftStringConst(context: BrsIrBackendContext, value: String): IrExpression =
    IrConstImpl.string(UNDEFINED_OFFSET, UNDEFINED_OFFSET, context.irBuiltIns.stringType, value)
