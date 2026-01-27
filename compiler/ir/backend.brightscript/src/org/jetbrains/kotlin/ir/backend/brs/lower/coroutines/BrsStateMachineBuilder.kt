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
import org.jetbrains.kotlin.ir.types.isNothing
import org.jetbrains.kotlin.ir.types.isUnit
import org.jetbrains.kotlin.ir.types.makeNotNull
import org.jetbrains.kotlin.ir.util.isSuspend
import org.jetbrains.kotlin.ir.util.isTrueConst
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
 * This transforms suspend calls into state machine transitions. Each suspend call
 * becomes a state transition that:
 * 1. Sets the state to the next continuation point
 * 2. Stores the suspend result
 * 3. Checks if the result is COROUTINE_SUSPENDED and returns if so
 * 4. Continues to the next state to process the result
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
    val allTheIntermediateLocals = mutableListOf<IrVariable>()
    // Note: globalExceptionVar must be initialized BEFORE rootExceptionTrap
    // because buildExceptionTrapState() uses globalExceptionVar
    private val globalExceptionVar = BrsIrBuilder.buildVar(
        exceptionSymbolGetter.returnType.makeNotNull(),
        function.owner,
        "e"
    )
    val rootExceptionTrap = buildExceptionTrapState()
    lateinit var globalCatch: IrCatch

    private var currentState = entryState
    private var currentBlock = entryState.entryBlock
    private val catchBlockStack = mutableListOf(rootExceptionTrap)
    private val tryStateMap = hashMapOf<IrExpression, TryState>()
    private val tryLoopStack = mutableListOf<IrExpression>()

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

    private fun newState() {
        val newState = SuspendState(unit)
        doDispatch(newState)
        updateState(newState)
    }

    private fun updateState(newState: SuspendState) {
        currentState = newState
        currentBlock = newState.entryBlock
    }

    private fun lastExpression() = currentBlock.statements.lastOrNull() as? IrExpression ?: unitValue

    private fun IrContainerExpression.addStatement(statement: IrStatement) {
        statements.add(statement)
    }

    private fun addStatement(statement: IrStatement) = currentBlock.addStatement(statement)

    private fun isBlockEnded(): Boolean {
        val lastExpression = currentBlock.statements.lastOrNull() as? IrExpression ?: return false
        return lastExpression.type.isNothing()
    }

    private fun maybeDoDispatch(target: SuspendState): Boolean {
        if (!isBlockEnded()) {
            doDispatch(target)
            return true
        }
        return false
    }

    private fun doDispatch(target: SuspendState, andContinue: Boolean = true) = doDispatchImpl(target, currentBlock, andContinue)

    private fun doDispatchImpl(target: SuspendState, block: IrContainerExpression, andContinue: Boolean) {
        val irDispatch = createDispatchPoint(target)
        currentState.successors.add(target)
        block.addStatement(BrsIrBuilder.buildCall(stateSymbolSetter.symbol, unit).apply {
            arguments[0] = thisReceiver
            arguments[1] = irDispatch
        })
        if (andContinue) doContinue(block)
    }

    private fun doContinue(block: IrContainerExpression = currentBlock) {
        block.addStatement(BrsIrBuilder.buildContinue(nothing, rootLoop))
    }

    private fun transformLastExpression(transformer: (IrExpression) -> IrStatement) {
        val expression = lastExpression()
        val newStatement = transformer(expression)
        currentBlock.statements.let { if (it.isNotEmpty()) it[it.lastIndex] = newStatement else it += newStatement }
    }

    private fun buildDispatchBlock(target: SuspendState) = BrsIrBuilder.buildComposite(unit)
        .also { doDispatchImpl(target, it, true) }

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
        val globalExceptionSymbol = globalExceptionVar.symbol
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
                BrsIrBuilder.buildGetValue(globalExceptionSymbol)
            )

            elseBlock.statements += BrsIrBuilder.buildCall(stateSymbolSetter.symbol, unit).apply {
                arguments[0] = thisReceiver
                arguments[1] = exceptionState()
            }
            elseBlock.statements += BrsIrBuilder.buildCall(exceptionSymbolSetter.symbol, unit).apply {
                arguments[0] = thisReceiver
                arguments[1] = BrsIrBuilder.buildGetValue(globalExceptionSymbol)
            }
        } else {
            block.statements += BrsIrBuilder.buildThrow(
                nothing,
                BrsIrBuilder.buildGetValue(globalExceptionSymbol)
            )
        }

        return IrCatchImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, catchVariable, block)
    }

    private fun exceptionState(): IrCall =
        BrsIrBuilder.buildCall(exStateSymbolGetter.symbol, context.irBuiltIns.intType).apply {
            arguments[0] = thisReceiver
        }

    private fun pendingException(): IrExpression =
        BrsIrBuilder.buildCall(exceptionSymbolGetter.symbol, exceptionSymbolGetter.returnType).apply {
            dispatchReceiver = thisReceiver
        }

    private fun setupExceptionState(target: SuspendState) {
        addStatement(BrsIrBuilder.buildCall(exStateSymbolSetter.symbol, unit).apply {
            arguments[0] = thisReceiver
            arguments[1] = createDispatchPoint(target)
        })
    }

    // ==================== Visitor Implementation ====================

    override fun visitElement(element: IrElement) {
        if (element in suspendableNodes) {
            element.acceptChildrenVoid(this)
        } else {
            addStatement(element as IrStatement)
        }
    }

    override fun visitExpression(expression: IrExpression) {
        addStatement(expression)
    }

    override fun visitReturn(expression: IrReturn) {
        addStatement(expression)
    }

    private fun transformLoop(loop: IrLoop, transformer: (IrLoop, SuspendState /*head*/, SuspendState /*exit*/) -> Unit) {
        if (loop !in suspendableNodes) return addStatement(loop)

        newState()

        val loopHeadState = currentState
        val loopExitState = SuspendState(unit)

        loopMap[loop] = LoopBounds(loopHeadState, loopExitState)

        tryLoopStack.add(loop)

        transformer(loop, loopHeadState, loopExitState)

        tryLoopStack.removeAt(tryLoopStack.lastIndex).also { assert(it === loop) }

        loopMap.remove(loop)

        updateState(loopExitState)
    }

    override fun visitWhileLoop(loop: IrWhileLoop) = transformLoop(loop) { l, head, exit ->
        l.condition.acceptVoid(this)

        transformLastExpression {
            val exitCond = BrsIrBuilder.buildCall(booleanNotSymbol).apply { dispatchReceiver = it }
            val irBreak = buildDispatchBlock(exit)
            BrsIrBuilder.buildIfElse(unit, exitCond, irBreak)
        }

        l.body?.acceptVoid(this)

        doDispatch(head)
    }

    override fun visitDoWhileLoop(loop: IrDoWhileLoop) = transformLoop(loop) { l, head, exit ->
        l.body?.acceptVoid(this)

        l.condition.acceptVoid(this)

        transformLastExpression {
            val irContinue = buildDispatchBlock(head)
            BrsIrBuilder.buildIfElse(unit, it, irContinue)
        }

        doDispatch(exit)
    }

    override fun visitBreak(jump: IrBreak) {
        val exitState = loopMap[jump.loop]!!.exitState
        resetExceptionStateIfNeeded(jump.loop)
        doDispatch(exitState)
    }

    override fun visitContinue(jump: IrContinue) {
        val headState = loopMap[jump.loop]!!.headState
        resetExceptionStateIfNeeded(jump.loop)
        doDispatch(headState)
    }

    private fun resetExceptionStateIfNeeded(loop: IrLoop) {
        var nearestTry: IrExpression? = null
        var found = false
        var needReset = false
        for (e in tryLoopStack.asReversed()) {
            if (e is IrTry) {
                needReset = !found
            }
            if (e === loop) {
                found = true
            }
            if (found) {
                if (e is IrTry) {
                    nearestTry = e
                    break
                }
            }
        }

        if (needReset) {
            val tryState = tryStateMap[nearestTry]?.catchState ?: rootExceptionTrap
            setupExceptionState(tryState)
        }
    }

    override fun visitCall(expression: IrCall) {
        super.visitCall(expression)

        if (expression.isSuspend) {
            val result = lastExpression()
            val continueState = SuspendState(unit)

            val dispatch = createDispatchPoint(continueState)

            currentState.successors += continueState

            transformLastExpression {
                BrsIrBuilder.buildCall(stateSymbolSetter.symbol, unit).apply {
                    arguments[0] = thisReceiver
                    arguments[1] = dispatch
                }
            }

            addStatement(setSuspendResultValue(result))

            val irReturn = BrsIrBuilder.buildReturn(function, getSuspendResultAsType(anyN), nothing)
            val coroutineSuspendedGetter = context.brsSymbols.coroutineSymbols.coroutineSuspendedGetter
            val check = BrsIrBuilder.buildCall(eqeqeqSymbol).apply {
                arguments[0] = getSuspendResultAsType(anyN)
                arguments[1] = if (coroutineSuspendedGetter != null) {
                    BrsIrBuilder.buildCall(coroutineSuspendedGetter, coroutineSuspendedGetter.owner.returnType)
                } else {
                    // Fallback during stdlib compilation - should not happen for user code
                    BrsIrBuilder.buildGetValue(thisSymbol)
                }
            }

            val suspensionBlock = BrsIrBuilder.buildBlock(unit, listOf(irReturn))
            addStatement(BrsIrBuilder.buildIfElse(unit, check, suspensionBlock))

            doContinue()

            updateState(continueState)
            // Note: We don't add getSuspendResultAsType as a bare statement here.
            // The result is already stored in suspendResult from setSuspendResultValue above.
            // Subsequent code will access it via getSuspendResultAsType calls where needed.
            // Adding it as a bare statement would generate invalid BrightScript (bare variable reference).
        }
    }

    override fun visitBlock(expression: IrBlock) {
        if (expression !in suspendableNodes) {
            addStatement(expression)
            return
        }
        expression.statements.forEach { it.acceptVoid(this) }
    }

    override fun visitComposite(expression: IrComposite) {
        if (expression !in suspendableNodes) {
            addStatement(expression)
            return
        }
        expression.statements.forEach { it.acceptVoid(this) }
    }

    override fun visitContainerExpression(expression: IrContainerExpression) {
        if (expression !in suspendableNodes) {
            addStatement(expression)
            return
        }
        expression.statements.forEach { it.acceptVoid(this) }
    }

    /**
     * During the splitting of a suspend function into states, we may encounter a situation when a variable is initialized
     * in one scope but is used in a different, which causes invalid access to the variable.
     * To prevent such a situation, all the locals are moved to the beginning of the function containing edges of the state machine.
     */
    override fun visitVariable(declaration: IrVariable) {
        registerLocal(declaration)

        val initializer = declaration.initializer
        declaration.initializer = null

        val startOffset = declaration.startOffset
        val endOffset = declaration.endOffset
        declaration.startOffset = UNDEFINED_OFFSET
        declaration.endOffset = UNDEFINED_OFFSET

        if (declaration !in suspendableNodes) {
            initializer?.let { addStatement(BrsIrBuilder.buildSetValue(declaration.symbol, it, startOffset, endOffset)) }
            return
        }

        initializer?.acceptVoid(this)
        transformLastExpression { BrsIrBuilder.buildSetValue(declaration.symbol, it, startOffset, endOffset) }
    }

    private fun registerLocal(variable: IrVariable) {
        allTheIntermediateLocals.add(variable)
    }

    override fun visitWhen(expression: IrWhen) {
        if (expression !in suspendableNodes) {
            addStatement(expression)
            return
        }

        val exitState = SuspendState(unit)

        for (branch in expression.branches) {
            if (branch.condition in suspendableNodes) {
                branch.condition.acceptVoid(this)
            } else {
                addStatement(branch.condition)
            }

            if (branch.result in suspendableNodes) {
                val condition = lastExpression()
                val branchBlock = BrsIrBuilder.buildBlock(unit)

                transformLastExpression { branchBlock }

                newState()
                val branchState = currentState
                doDispatchImpl(branchState, branchBlock, true)

                // Add condition check
                val check = if (branch === expression.branches.last() && branch.condition.isTrueConst()) {
                    // else branch - always taken
                    condition
                } else {
                    condition
                }

                branch.result.acceptVoid(this)
                maybeDoDispatch(exitState)
            } else {
                transformLastExpression { cond ->
                    BrsIrBuilder.buildIfElse(expression.type, cond, branch.result)
                }
            }
        }

        updateState(exitState)
    }

    private fun buildTryState() = TryState(currentState, SuspendState(unit))

    private fun addExceptionEdge() {
        hasExceptions = true
        currentState.successors += catchBlockStack.last()
    }

    private fun hasResultingValue(expression: IrExpression) = !expression.type.run { isNothing() || isUnit() }

    private fun implicitCast(value: IrExpression, toType: IrType): IrExpression =
        IrTypeOperatorCallImpl(
            UNDEFINED_OFFSET, UNDEFINED_OFFSET,
            toType,
            IrTypeOperator.IMPLICIT_CAST,
            toType,
            value
        )

    private fun tempVar(type: IrType, name: String = "tmp") =
        BrsIrBuilder.buildVar(type, function.owner, name)
            .also(::registerLocal)

    override fun visitThrow(expression: IrThrow) {
        expression.acceptChildrenVoid(this)
        addExceptionEdge()
        transformLastExpression { expression.apply { value = it } }
    }

    override fun visitTry(aTry: IrTry) {
        require(aTry.finallyExpression == null) { "Finally blocks should be lowered before suspend functions lowering" }

        val tryState = buildTryState()
        val enclosingCatch = catchBlockStack.last()

        tryStateMap[aTry] = tryState

        catchBlockStack.add(tryState.catchState)
        tryLoopStack.add(aTry)

        val exitState = SuspendState(unit)

        val varSymbol = if (hasResultingValue(aTry)) tempVar(aTry.type, "TRY_RESULT") else null

        if (varSymbol != null) {
            addStatement(varSymbol)
        }

        setupExceptionState(tryState.catchState)

        val tryResult = if (varSymbol != null) {
            BrsIrBuilder.buildSetVariable(varSymbol.symbol, aTry.tryResult, unit).also {
                if (it.value in suspendableNodes) suspendableNodes += it
            }
        } else aTry.tryResult

        tryResult.acceptVoid(this)

        if (!isBlockEnded()) {
            setupExceptionState(enclosingCatch)
            doDispatch(exitState)
        }
        addExceptionEdge()

        tryStateMap.remove(aTry)
        tryLoopStack.removeAt(tryLoopStack.lastIndex).also { assert(it === aTry) }

        catchBlockStack.removeAt(catchBlockStack.lastIndex)

        updateState(tryState.catchState)
        setupExceptionState(enclosingCatch)

        var rethrowNeeded = true

        for (catch in aTry.catches) {
            val type = catch.catchParameter.type
            val initializer = implicitCast(pendingException(), type)
            val irVar = catch.catchParameter.also {
                it.initializer = initializer
            }
            val catchResult = if (varSymbol != null) {
                BrsIrBuilder.buildSetVariable(varSymbol.symbol, catch.result, unit).also {
                    if (it.value in suspendableNodes) suspendableNodes += it
                }
            } else catch.result

            // BrightScript doesn't have dynamic types, so we use a simpler approach:
            // Just handle the catch block directly (no type checking needed as
            // all exceptions are the same base type in BrightScript)
            rethrowNeeded = false

            addStatement(irVar)
            catchResult.acceptVoid(this)
            maybeDoDispatch(exitState)
        }

        if (rethrowNeeded) {
            addExceptionEdge()
            addStatement(BrsIrBuilder.buildThrow(nothing, pendingException()))
        }

        currentState.successors += enclosingCatch

        updateState(exitState)
        setupExceptionState(enclosingCatch)

        if (varSymbol != null) {
            addStatement(BrsIrBuilder.buildGetValue(varSymbol.symbol))
        }
    }
}
