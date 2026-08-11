/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.lower.coroutines

import org.jetbrains.kotlin.backend.common.ir.isPure
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.backend.brs.BrsIrBackendContext
import org.jetbrains.kotlin.ir.backend.brs.ir.BrsIrBuilder
import org.jetbrains.kotlin.ir.backend.brs.ir.BrsStatementOrigins as BrsIrStatementOrigins
import org.jetbrains.kotlin.ir.backend.brs.lower.BrsDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.irAttribute
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrReturnableBlockSymbol
import org.jetbrains.kotlin.ir.symbols.IrValueParameterSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.isNothing
import org.jetbrains.kotlin.ir.types.isUnit
import org.jetbrains.kotlin.ir.types.makeNotNull
import org.jetbrains.kotlin.ir.util.deepCopyWithSymbols
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
        // The trap state executes at the top of the dispatch loop, OUTSIDE the global
        // catch clause, so the catch-local variable is out of scope here. It must
        // rethrow the exception stored on the coroutine - set either by the global
        // catch before dispatching, or by CoroutineImpl.resumeWith on exceptional
        // resume (which dispatches straight to this state).
        state.entryBlock.statements += BrsIrBuilder.buildThrow(nothing, pendingException())
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
        expression.acceptChildrenVoid(this)
        val returnTarget = expression.returnTargetSymbol
        if (returnTarget !is IrReturnableBlockSymbol) {
            transformLastExpression { expression.apply { value = it } }
        }
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
            // Expose the resumed value as the last expression of the continue state so that
            // enclosing constructs (visitVariable, visitSetValue, visitReturn, visitTypeOperator)
            // pick it up via transformLastExpression. Without this, lastExpression() returns
            // unitValue and e.g. `val x = suspendCall()` resumes with `x = invalid`.
            // If nothing consumes it, the emitter drops the pure read statement
            // (see IrStatementToBrsTransformer.isDiscardablePureExpression).
            // Unit-returning suspend calls don't carry a value, so nothing is exposed for them.
            if (hasResultingValue(expression)) {
                addStatement(getSuspendResultAsType(expression.type))
            }
        }
    }

    override fun visitSetValue(expression: IrSetValue) {
        if (expression !in suspendableNodes) return addStatement(expression)
        expression.acceptChildrenVoid(this)
        transformLastExpression { expression.apply { value = it } }
    }

    override fun visitTypeOperator(expression: IrTypeOperatorCall) {
        if (expression !in suspendableNodes) return addStatement(expression)
        expression.acceptChildrenVoid(this)
        transformLastExpression { expression.apply { argument = it } }
    }

    /**
     * Splits suspendable sub-expressions out of an argument list, preserving
     * evaluation order across the suspension points: every argument up to the
     * last suspendable one is evaluated (state-split if suspendable) into an
     * ARGUMENT temp; arguments after the last suspension point are left in place.
     */
    private fun <E : IrExpression?> transformArguments(arguments: MutableList<E>) {
        var suspendableCount = arguments.fold(0) { r, n -> if (n != null && n in suspendableNodes) r + 1 else r }
        arguments.replaceAll { arg ->
            if (arg.isPure(false)) arg else {
                require(arg != null)
                if (suspendableCount > 0) {
                    if (arg in suspendableNodes) suspendableCount--
                    arg.acceptVoid(this)
                    val irVar = tempVar(arg.type, "ARGUMENT")
                    transformLastExpression {
                        BrsIrBuilder.buildSetValue(irVar.symbol, it)
                    }
                    @Suppress("UNCHECKED_CAST")
                    BrsIrBuilder.buildGetValue(irVar.symbol) as E
                } else {
                    arg.deepCopyWithSymbols(function.owner)
                }
            }
        }
    }

    override fun visitMemberAccess(expression: IrMemberAccessExpression<*>) {
        if (expression !in suspendableNodes) {
            addExceptionEdge()
            return addStatement(expression)
        }

        transformArguments(expression.arguments)

        addExceptionEdge()
        addStatement(expression)
    }

    // By state-machine time a var captured by a nested lambda is a shared-box FIELD
    // write, so `capturedVar = suspendCall()` arrives here as IrSetField - the
    // visitSetValue split above never sees it. Without this override the whole
    // assignment was emitted bare in one state and the box received
    // COROUTINE_SUSPENDED itself (device fingerprint: "actual <CoroutineSingletons>").
    override fun visitSetField(expression: IrSetField) {
        if (expression !in suspendableNodes) return addStatement(expression)

        val newArguments = mutableListOf(expression.receiver, expression.value).also(this::transformArguments)

        val receiver = newArguments[0]
        val value = newArguments[1]!!

        addStatement(expression.run {
            IrSetFieldImpl(
                startOffset,
                endOffset,
                symbol,
                receiver,
                value,
                unit,
                origin,
                superQualifierSymbol
            )
        })
    }

    // String templates survive to this point as IrStringConcatenation
    // (StringConcatenationLowering is a no-op), so a suspend call inside a
    // template must be split out of the concatenation like any other argument.
    override fun visitStringConcatenation(expression: IrStringConcatenation) {
        if (expression !in suspendableNodes) return addStatement(expression)

        val newArguments = expression.arguments.toMutableList().apply(this::transformArguments)

        addStatement(expression.run {
            IrStringConcatenationImpl(
                startOffset,
                endOffset,
                type,
                newArguments
            )
        })
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

        // Shared (closure-boxed) variables are boxed at their declaration by the emitter.
        // Since the declaration is hoisted here and the initializer becomes an assignment,
        // tag that assignment so the emitter creates the box there instead.
        val setOrigin = if (declaration.origin == BrsDeclarationOrigin.SHARED_VARIABLE_WRAPPER)
            BrsStatementOrigins.SHARED_BOX_INIT
        else
            BrsIrStatementOrigins.SYNTHESIZED_STATEMENT

        if (declaration !in suspendableNodes) {
            initializer?.let { addStatement(BrsIrBuilder.buildSetValue(declaration.symbol, it, startOffset, endOffset, setOrigin)) }
            return
        }

        initializer?.acceptVoid(this)
        transformLastExpression { BrsIrBuilder.buildSetValue(declaration.symbol, it, startOffset, endOffset, setOrigin) }
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

        // Wrap an arm in a TRY_RESULT assignment only when the ARM itself carries a
        // value. A Unit-typed arm (e.g. catch { thrown = e } while the other arm makes
        // the try's LUB type non-Unit) must run unwrapped: wrapping it makes the arm's
        // trailing assignment the RHS of the TRY_RESULT set, and BrightScript renders
        // an assignment in expression position as a COMPARISON — the arm's effect is
        // silently lost (m.TRY_RESULT = (m.thrown = e)). The consumer of an unassigned
        // TRY_RESULT reads invalid, which is exactly the BRS mapping of Unit.
        val tryResult = if (varSymbol != null && hasResultingValue(aTry.tryResult)) {
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
            // Same per-arm value guard as tryResult above.
            val catchResult = if (varSymbol != null && hasResultingValue(catch.result)) {
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

        // Expose the try's value as the last expression of the exit state so enclosing
        // constructs (visitVariable, visitSetValue, visitReturn, visitTypeOperator) can
        // consume it via transformLastExpression — same protocol as the resumed-value
        // exposure in visitCall. If nothing consumes it, the emitter drops the pure
        // read (isDiscardablePureExpression — which covers the IrGetField this read
        // becomes after BrsLiveLocalsTransformer promotes TRY_RESULT to an m-field).
        if (varSymbol != null) {
            addStatement(BrsIrBuilder.buildGetValue(varSymbol.symbol))
        }
    }
}
