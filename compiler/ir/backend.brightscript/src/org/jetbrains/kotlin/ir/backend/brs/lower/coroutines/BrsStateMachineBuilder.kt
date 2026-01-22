/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.lower.coroutines

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.backend.brs.BrsIrBackendContext
import org.jetbrains.kotlin.ir.backend.brs.ir.BrsIrBuilder
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.irAttribute
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrValueParameterSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.makeNotNull
import org.jetbrains.kotlin.ir.util.isSuspend
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid

/**
 * Represents a state in the coroutine state machine.
 */
class SuspendState(type: IrType) {
    val entryBlock: IrContainerExpression = IrCompositeImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, type)
    val successors = mutableSetOf<SuspendState>()
    var id = -1
}

private var IrComposite.suspendState: SuspendState? by irAttribute(copyByDefault = false)

data class LoopBounds(val headState: SuspendState, val exitState: SuspendState)

data class TryState(val tryState: SuspendState, val catchState: SuspendState)

/**
 * Creates a dispatch point placeholder that will be replaced with the state ID later.
 */
private fun createDispatchPoint(target: SuspendState): IrComposite =
    IrCompositeImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, target.entryBlock.type).also { it.suspendState = target }

/**
 * Transforms dispatch point placeholders into actual state IDs.
 */
class BrsDispatchPointTransformer(val action: (SuspendState) -> IrExpression) : IrElementTransformerVoid() {
    override fun visitComposite(expression: IrComposite): IrExpression {
        val suspendState = expression.suspendState ?: return super.visitComposite(expression)
        return action(suspendState)
    }
}

/**
 * Collects nodes that contain or are suspension points.
 */
class BrsSuspendableNodesCollector(private val suspendableNodes: MutableSet<IrElement>) : IrVisitorVoid() {
    private var hasSuspendableChildren = false

    private fun markNode(node: IrElement) {
        suspendableNodes += node
        hasSuspendableChildren = true
    }

    override fun visitElement(element: IrElement) {
        val current = hasSuspendableChildren
        hasSuspendableChildren = false
        element.acceptChildrenVoid(this)
        if (hasSuspendableChildren) {
            markNode(element)
        }
        hasSuspendableChildren = hasSuspendableChildren || current
    }

    override fun visitCall(expression: IrCall) {
        super.visitCall(expression)
        if (expression.isSuspend) {
            markNode(expression)
        }
    }
}

fun collectSuspendableNodes(function: IrBlock): MutableSet<IrElement> {
    val suspendableNodes = mutableSetOf<IrElement>()
    var size: Int
    do {
        size = suspendableNodes.size
        function.acceptVoid(BrsSuspendableNodesCollector(suspendableNodes))
    } while (size != suspendableNodes.size)
    return suspendableNodes
}

/**
 * Builds the state machine IR for a suspend function.
 *
 * This is a simplified implementation for Milestone 1 that creates
 * the basic state machine structure. Full state machine generation
 * with proper suspension point handling will be implemented incrementally.
 */
class BrsStateMachineBuilder(
    private val suspendableNodes: MutableSet<IrElement>,
    val context: BrsIrBackendContext,
    val function: IrFunctionSymbol,
    private val rootLoop: IrLoop,
    private val exceptionSymbolGetter: IrSimpleFunction,
    private val exceptionSymbolSetter: IrSimpleFunction,
    private val exStateSymbolGetter: IrSimpleFunction,
    private val exStateSymbolSetter: IrSimpleFunction,
    private val stateSymbolSetter: IrSimpleFunction,
    private val thisSymbol: IrValueParameterSymbol,
    private val getSuspendResultAsType: (IrType) -> IrExpression,
    private val setSuspendResultValue: (IrExpression) -> IrStatement
) : IrVisitorVoid() {

    private val loopMap = hashMapOf<IrLoop, LoopBounds>()
    private val unit = context.irBuiltIns.unitType
    private val anyN = context.irBuiltIns.anyNType
    private val nothing = context.irBuiltIns.nothingType
    private val booleanNotSymbol = context.irBuiltIns.booleanNotSymbol
    private val eqeqeqSymbol = context.irBuiltIns.eqeqeqSymbol

    private val thisReceiver get() = BrsIrBuilder.buildGetValue(thisSymbol)

    private var hasExceptions = false

    val entryState = SuspendState(unit)
    val rootExceptionTrap = buildExceptionTrapState()
    val allTheIntermediateLocals = mutableListOf<IrVariable>()
    private val globalExceptionVar = BrsIrBuilder.buildVar(
        exceptionSymbolGetter.returnType.makeNotNull(),
        function.owner,
        "e"
    )
    lateinit var globalCatch: IrCatch

    private var currentState = entryState
    private val currentBlock get() = currentState.entryBlock
    private val tryStateStack = mutableListOf<TryState>()

    private val unitValue: IrExpression
        get() = IrGetObjectValueImpl(
            UNDEFINED_OFFSET,
            UNDEFINED_OFFSET,
            unit,
            context.irBuiltIns.unitClass
        )

    private fun buildExceptionTrapState(): SuspendState {
        val state = SuspendState(unit)
        state.entryBlock.statements += BrsIrBuilder.buildThrow(
            nothing,
            BrsIrBuilder.buildGetValue(globalExceptionVar.symbol)
        )
        return state
    }

    fun finalizeStateMachine() {
        globalCatch = buildGlobalCatch()
        if (currentBlock.statements.lastOrNull() !is IrReturn) {
            addStatement(
                IrReturnImpl(
                    startOffset = rootLoop.endOffset,
                    endOffset = rootLoop.endOffset,
                    nothing,
                    function,
                    unitValue
                )
            )
        }
        if (!hasExceptions) entryState.successors += rootExceptionTrap
    }

    private fun buildGlobalCatch(): IrCatch {
        val catchVariable = globalExceptionVar
        val block = BrsIrBuilder.buildBlock(unit)

        if (hasExceptions) {
            val thenBlock = BrsIrBuilder.buildBlock(unit)
            val elseBlock = BrsIrBuilder.buildBlock(unit)

            val check = BrsIrBuilder.buildCall(eqeqeqSymbol).apply {
                arguments[0] = exceptionState()
                arguments[1] = createDispatchPoint(rootExceptionTrap)
            }

            block.statements += BrsIrBuilder.buildIfElse(unit, check, thenBlock, elseBlock)

            thenBlock.statements += BrsIrBuilder.buildThrow(
                nothing,
                BrsIrBuilder.buildGetValue(globalExceptionVar.symbol)
            )

            elseBlock.statements += BrsIrBuilder.buildCall(stateSymbolSetter.symbol, unit).apply {
                arguments[0] = thisReceiver
                arguments[1] = exceptionState()
            }
            elseBlock.statements += BrsIrBuilder.buildCall(exceptionSymbolSetter.symbol, unit).apply {
                arguments[0] = thisReceiver
                arguments[1] = BrsIrBuilder.buildGetValue(globalExceptionVar.symbol)
            }
        } else {
            block.statements += BrsIrBuilder.buildThrow(
                nothing,
                BrsIrBuilder.buildGetValue(globalExceptionVar.symbol)
            )
        }

        return IrCatchImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, catchVariable, block)
    }

    private fun exceptionState(): IrCall =
        BrsIrBuilder.buildCall(exStateSymbolGetter.symbol, context.irBuiltIns.intType).apply {
            arguments[0] = thisReceiver
        }

    private fun addStatement(statement: IrStatement) {
        currentBlock.statements += statement
    }

    // Simple visitor implementation - just pass through for now
    override fun visitElement(element: IrElement) {
        element.acceptChildrenVoid(this)
    }

    override fun visitExpression(expression: IrExpression) {
        addStatement(expression)
    }

    override fun visitReturn(expression: IrReturn) {
        addStatement(expression)
    }
}
