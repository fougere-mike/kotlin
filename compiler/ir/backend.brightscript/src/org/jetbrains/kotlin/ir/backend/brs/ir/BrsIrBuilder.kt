/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.ir

import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.declarations.buildValueParameter
import org.jetbrains.kotlin.ir.builders.declarations.buildVariable
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.symbols.*
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.name.Name

/**
 * Statement origin for synthesized BrightScript statements.
 */
object BrsStatementOrigins {
    val SYNTHESIZED_STATEMENT by IrStatementOriginImpl
}

/**
 * IR builder utilities for BrightScript backend.
 * Modeled after JsIrBuilder for consistency.
 */
object BrsIrBuilder {
    val SYNTHESIZED_DECLARATION by IrDeclarationOriginImpl

    fun buildCall(
        target: IrSimpleFunctionSymbol,
        type: IrType? = null,
        typeArguments: List<IrType>? = null,
        origin: IrStatementOrigin = BrsStatementOrigins.SYNTHESIZED_STATEMENT,
        superQualifierSymbol: IrClassSymbol? = null,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall {
        val owner = target.owner
        return IrCallImpl(
            startOffset,
            endOffset,
            type ?: owner.returnType,
            target,
            superQualifierSymbol = superQualifierSymbol,
            typeArgumentsCount = owner.typeParameters.size,
            origin = origin
        ).apply {
            typeArguments?.let {
                assert(typeArguments.size == this.typeArguments.size)
                it.withIndex().forEach { (i, t) ->
                    this.typeArguments[i] = t
                }
            }
        }
    }

    fun buildConstructorCall(
        target: IrConstructorSymbol,
        typeArguments: List<IrType?>? = null,
        origin: IrStatementOrigin = BrsStatementOrigins.SYNTHESIZED_STATEMENT,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET,
    ): IrConstructorCall {
        val owner = target.owner
        val irClass = owner.parentAsClass

        return IrConstructorCallImpl(
            startOffset,
            endOffset,
            owner.returnType,
            target,
            typeArgumentsCount = irClass.typeParameters.size,
            constructorTypeArgumentsCount = owner.typeParameters.size,
            origin = origin
        ).apply {
            typeArguments?.let {
                assert(it.size == this.typeArguments.size)
                it.withIndex().forEach { (i, t) ->
                    this.typeArguments[i] = t
                }
            }
        }
    }

    fun buildReturn(targetSymbol: IrFunctionSymbol, value: IrExpression, type: IrType) =
        IrReturnImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, type, targetSymbol, value)

    fun buildThrow(type: IrType, value: IrExpression) =
        IrThrowImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, type, value)

    fun buildGetValue(symbol: IrValueSymbol, origin: IrStatementOrigin = BrsStatementOrigins.SYNTHESIZED_STATEMENT) =
        IrGetValueImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, symbol.owner.type, symbol, origin)

    fun buildSetValue(
        symbol: IrValueSymbol,
        value: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET,
    ) =
        IrSetValueImpl(startOffset, endOffset, symbol.owner.type, symbol, value, BrsStatementOrigins.SYNTHESIZED_STATEMENT)

    fun buildGetField(
        symbol: IrFieldSymbol,
        receiver: IrExpression? = null,
        superQualifierSymbol: IrClassSymbol? = null,
        type: IrType? = null,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET,
        origin: IrStatementOrigin? = BrsStatementOrigins.SYNTHESIZED_STATEMENT
    ) =
        IrGetFieldImpl(
            startOffset,
            endOffset,
            symbol,
            type ?: symbol.owner.type,
            receiver,
            origin,
            superQualifierSymbol
        )

    fun buildSetField(
        symbol: IrFieldSymbol,
        receiver: IrExpression?,
        value: IrExpression,
        type: IrType,
        superQualifierSymbol: IrClassSymbol? = null
    ) =
        IrSetFieldImpl(
            UNDEFINED_OFFSET, UNDEFINED_OFFSET, symbol, receiver, value, type,
            BrsStatementOrigins.SYNTHESIZED_STATEMENT, superQualifierSymbol
        )

    fun buildBlock(type: IrType) =
        IrBlockImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, type, BrsStatementOrigins.SYNTHESIZED_STATEMENT)

    fun buildBlock(type: IrType, statements: List<IrStatement>) =
        IrBlockImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, type, BrsStatementOrigins.SYNTHESIZED_STATEMENT, statements)

    fun buildComposite(type: IrType, statements: List<IrStatement> = emptyList()) =
        IrCompositeImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, type, BrsStatementOrigins.SYNTHESIZED_STATEMENT, statements)

    fun buildVar(
        type: IrType,
        parent: IrDeclarationParent?,
        name: String = "tmp",
        isVar: Boolean = false,
        isConst: Boolean = false,
        isLateinit: Boolean = false,
        initializer: IrExpression? = null,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrVariable = buildVariable(
        parent,
        startOffset,
        endOffset,
        SYNTHESIZED_DECLARATION,
        Name.identifier(name),
        type,
        isVar,
        isConst,
        isLateinit,
    ).also {
        it.initializer = initializer
    }

    fun buildBreak(type: IrType, loop: IrLoop) =
        IrBreakImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, type, loop)

    fun buildContinue(type: IrType, loop: IrLoop) =
        IrContinueImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, type, loop)

    fun buildIfElse(
        type: IrType,
        cond: IrExpression,
        thenBranch: IrExpression,
        elseBranch: IrExpression? = null,
        thenBranchStartOffset: Int = cond.startOffset,
        thenBranchEndOffset: Int = thenBranch.endOffset,
        elseBranchStartOffset: Int = UNDEFINED_OFFSET,
        elseBranchEndOffset: Int = elseBranch?.endOffset ?: UNDEFINED_OFFSET,
    ): IrWhen =
        buildIfElse(
            UNDEFINED_OFFSET,
            UNDEFINED_OFFSET,
            type,
            cond = cond,
            thenBranch = thenBranch,
            elseBranch = elseBranch,
            origin = BrsStatementOrigins.SYNTHESIZED_STATEMENT,
            thenBranchStartOffset = thenBranchStartOffset,
            thenBranchEndOffset = thenBranchEndOffset,
            elseBranchStartOffset = elseBranchStartOffset,
            elseBranchEndOffset = elseBranchEndOffset
        )

    fun buildIfElse(
        startOffset: Int,
        endOffset: Int,
        type: IrType,
        cond: IrExpression,
        thenBranch: IrExpression,
        elseBranch: IrExpression? = null,
        origin: IrStatementOrigin? = null,
        thenBranchStartOffset: Int = cond.startOffset,
        thenBranchEndOffset: Int = thenBranch.endOffset,
        elseBranchStartOffset: Int = UNDEFINED_OFFSET,
        elseBranchEndOffset: Int = elseBranch?.endOffset ?: UNDEFINED_OFFSET,
    ): IrWhen {
        val element = IrWhenImpl(startOffset, endOffset, type, origin)
        element.branches.add(IrBranchImpl(thenBranchStartOffset, thenBranchEndOffset, cond, thenBranch))
        if (elseBranch != null) {
            val irTrue = IrConstImpl.constTrue(UNDEFINED_OFFSET, UNDEFINED_OFFSET, cond.type)
            element.branches.add(IrElseBranchImpl(elseBranchStartOffset, elseBranchEndOffset, irTrue, elseBranch))
        }
        return element
    }

    fun buildWhen(type: IrType, branches: List<IrBranch>, origin: IrStatementOrigin = BrsStatementOrigins.SYNTHESIZED_STATEMENT) =
        IrWhenImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, type, origin, branches)

    fun buildNull(type: IrType) = IrConstImpl.constNull(UNDEFINED_OFFSET, UNDEFINED_OFFSET, type)
    fun buildBoolean(type: IrType, v: Boolean) = IrConstImpl.boolean(UNDEFINED_OFFSET, UNDEFINED_OFFSET, type, v)
    fun buildInt(type: IrType, v: Int) = IrConstImpl.int(UNDEFINED_OFFSET, UNDEFINED_OFFSET, type, v)
    fun buildString(type: IrType, s: String) = IrConstImpl.string(UNDEFINED_OFFSET, UNDEFINED_OFFSET, type, s)

    fun buildTry(type: IrType) = IrTryImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, type)
    fun buildCatch(ex: IrVariable, block: IrBlockImpl) = IrCatchImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, ex, block)

    fun buildGetObjectValue(type: IrType, classSymbol: IrClassSymbol) =
        IrGetObjectValueImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, type, classSymbol)
}
