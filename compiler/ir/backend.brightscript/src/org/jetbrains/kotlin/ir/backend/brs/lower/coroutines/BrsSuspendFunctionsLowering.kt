/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.lower.coroutines

import org.jetbrains.kotlin.backend.common.BodyLoweringPass
import org.jetbrains.kotlin.backend.common.capturedConstructor
import org.jetbrains.kotlin.backend.common.capturedFields
import org.jetbrains.kotlin.backend.common.compilationException
import org.jetbrains.kotlin.backend.common.descriptors.synthesizedName
import org.jetbrains.kotlin.backend.common.lower.AbstractSuspendFunctionsLowering
import org.jetbrains.kotlin.backend.common.lower.FinallyBlocksLowering
import org.jetbrains.kotlin.backend.common.lower.ReturnableBlockTransformer
import org.jetbrains.kotlin.backend.common.lower.coroutines.loweredSuspendFunctionReturnType
import org.jetbrains.kotlin.backend.common.lower.createIrBuilder
import org.jetbrains.kotlin.backend.common.lower.optimizations.LivenessAnalysis
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.backend.brs.BrsIrBackendContext
import org.jetbrains.kotlin.ir.backend.brs.lower.BrsDeclarationOrigin
import org.jetbrains.kotlin.ir.backend.brs.transformers.irToBrs.sanitizeFieldName
import org.jetbrains.kotlin.ir.backend.brs.transformers.irToBrs.sanitizeParameterName
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.declarations.impl.IrVariableImpl
import org.jetbrains.kotlin.ir.symbols.IrFieldSymbol
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrValueSymbol
import org.jetbrains.kotlin.ir.symbols.IrVariableSymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrVariableSymbolImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.isUnit
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.*
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.DFS
import org.jetbrains.kotlin.backend.common.lower.WebCallableReferenceLowering
import org.jetbrains.kotlin.util.OperatorNameConventions

/**
 * Object keys the emitter writes on EVERY class instance (see the create-function
 * emission in IrToBrsTransformer): part of a coroutine object's namespace before
 * any user-named field lands on it.
 */
private val EMITTER_OBJECT_KEYS: Set<String> = setOf("__type", "__proto", "__id", "_super")

/**
 * Emitted keys (lower-cased) of the data fields every coroutine object inherits
 * from `kotlin.coroutines.CoroutineImpl` and `InterceptedCoroutine`
 * (libraries/stdlib/brs/src/kotlin/coroutines/CoroutineImpl.kt). MIRRORED by
 * hand because the lazy IR the base class arrives as hides its private fields
 * (see [BrsSuspendFunctionsLowering.coroutineBaseFieldKeys]). Divergence law:
 * a field added to either class lands here in the same commit — CoroutineImpl.kt
 * carries the cross-reference.
 */
private val COROUTINE_BASE_FIELD_KEYS: Set<String> = setOf(
    "resultcontinuation", "state", "exceptionstate", "result", "exception", "finallypath", "_context",
    "_intercepted",
)

/**
 * Transforms suspend functions into CoroutineImpl instances with state machines.
 *
 * This is the BrightScript-specific implementation of suspend function lowering.
 * It converts suspend functions into classes that extend CoroutineImpl and contain
 * a doResume() method that implements a state machine.
 *
 * Based on the JS backend implementation (JsSuspendFunctionsLowering).
 */
class BrsSuspendFunctionsLowering(
    private val brsContext: BrsIrBackendContext
) : AbstractSuspendFunctionsLowering<BrsIrBackendContext>(brsContext), BodyLoweringPass {

    private val coroutineSymbols = brsContext.brsSymbols.coroutineSymbols

    override val stateMachineMethodName = Name.identifier("doResume")

    override fun getCoroutineBaseClass(function: IrFunction) = context.symbols.coroutineImpl

    override fun nameForCoroutineClass(function: IrFunction) = "${function.name}COROUTINE\$".synthesizedName

    override fun lower(irBody: IrBody, container: IrDeclaration) {
        if (container is IrSimpleFunction && container.isSuspend) {
            transformSuspendFunction(container, irBody)?.let {
                val dc = container.parent as IrDeclarationContainer
                dc.addChild(it)
            }
        }
    }

    private fun transformSuspendFunction(function: IrSimpleFunction, body: IrBody): IrClass? {
        assert(function.isSuspend)

        return when (val functionKind = getSuspendFunctionKind(function, body)) {
            is SuspendFunctionKind.NO_SUSPEND_CALLS -> {
                // No suspend function calls - just an ordinary function.
                null
            }
            is SuspendFunctionKind.DELEGATING -> {
                // Calls another suspend function at the end - no state machine needed.
                removeReturnIfSuspendedCallAndSimplifyDelegatingCall(function, functionKind.delegatingCall)
                null
            }
            is SuspendFunctionKind.NEEDS_STATE_MACHINE -> {
                val isLoweredSuspendLambda = function.isSuspendFunctionValueInvoke()
                if (isLoweredSuspendLambda) {
                    prepareFunctionReferenceCapturedFields(function)
                }
                val coroutine = buildCoroutine(function, isLoweredSuspendLambda)
                renameFieldsShadowingCoroutineBase(coroutine)
                if (isLoweredSuspendLambda) {
                    // Suspend function values (lambdas and ::refs) are called through factory method <create>
                    null
                } else {
                    coroutine
                }
            }
        }
    }

    /**
     * Emitted (lower-cased — BrightScript is case-insensitive) keys every
     * coroutine object already carries before the lowering adds its own fields.
     *
     * Two sources, unioned. [COROUTINE_BASE_FIELD_KEYS] mirrors the data fields
     * of `kotlin.coroutines.CoroutineImpl` and `InterceptedCoroutine` by hand,
     * because the base class reaches this lowering as LAZY FIR-backed IR that
     * exposes only its non-private members — the private `_context`,
     * `_intercepted` and `resultContinuation` fields, exactly the `_`-prefixed
     * ones a LocalDeclarationsLowering capture (`$name` → `_name`) can shadow,
     * are invisible to an IR walk. The IR walk over the base chain is kept on
     * top so a non-private field added to CoroutineImpl later is covered
     * without touching this file. Empty when CoroutineImpl is unresolvable
     * (stdlib compilation, where no state machines are generated anyway).
     */
    private val coroutineBaseFieldKeys: Set<String> by lazy {
        val keys = HashSet<String>()
        var klass: IrClass? = coroutineSymbols.coroutineImpl?.owner
        while (klass != null) {
            for (declaration in klass.declarations) {
                val field = when (declaration) {
                    is IrField -> declaration
                    is IrProperty -> declaration.backingField
                    else -> null
                } ?: continue
                keys.add(sanitizeFieldName(field.name.asString()).lowercase())
            }
            klass = klass.superClass
        }
        if (keys.isNotEmpty()) {
            keys.addAll(COROUTINE_BASE_FIELD_KEYS)
            keys.addAll(EMITTER_OBJECT_KEYS)
        }
        keys
    }

    /**
     * A coroutine class is flattened onto ONE BrightScript AA together with its
     * CoroutineImpl base, so every field this lowering adds — the suspend
     * function's parameters (stored under their raw names by the common
     * lowering's create method), its LocalDeclarationsLowering captures
     * (`$name`, emitted `_name`) and its spilled locals (`name<n>`) — shares a
     * namespace with the base's own slots. A lambda parameter named `state`
     * therefore landed on the state machine's dispatch index: `i.state = <value>`
     * in create, and the next `__get_state()` threw "Type Mismatch. Unable to
     * cast roAssociativeArray to Integer" (device finding 2026-09-04, TestScreen
     * flagship: `collectLatest { state -> ... }`). `Result`, `exception`, and a
     * captured local `context` (→ `_context`, the coroutine-context slot) fail
     * the same way.
     *
     * Renames the SUBCLASS field, never the base: any field whose emitted key
     * ([sanitizeFieldName] — the emitter's rule, one shared function) is
     * already taken by the base chain, the emitter's object keys, or an earlier
     * field of this class gets `_` appended until unique (the
     * sanitizeParameterName reserved-keyword convention). Field accesses are
     * symbol-based (IrGetField/IrSetField in the state machine and the create
     * method's stores), so renaming the IrField reaches every emission site;
     * the one NAME-keyed consumer, [BrsIrBackendContext.sharedVariableClassBoxFields],
     * is re-keyed here. Method slots (`doResume_k_`, `equals`, …) are outside
     * this defence — no plausible parameter name reaches them.
     * Golden: coroutines/coroutineFieldNameShadowing.
     */
    private fun renameFieldsShadowingCoroutineBase(coroutineClass: IrClass) {
        val reserved = coroutineBaseFieldKeys
        if (reserved.isEmpty()) return
        val taken = HashSet(reserved)
        for (field in coroutineClass.declarations.filterIsInstance<IrField>()) {
            val emitted = sanitizeFieldName(field.name.asString())
            if (taken.add(emitted.lowercase())) continue
            // Special names (<unused var>, …) cannot round-trip through Name.identifier;
            // rename from their emitted form instead (same key, no angle brackets).
            var candidate = if (field.name.isSpecial) emitted else field.name.asString()
            do {
                candidate += "_"
            } while (!taken.add(sanitizeFieldName(candidate).lowercase()))
            if (brsContext.sharedVariableClassBoxFields.remove(coroutineClass to emitted)) {
                brsContext.sharedVariableClassBoxFields.add(coroutineClass to sanitizeFieldName(candidate))
            }
            field.name = Name.identifier(candidate)
        }
    }

    /**
     * The `invoke` of a class the callable-reference lowering built as a
     * SUSPEND function VALUE: a LAMBDA_IMPL class, or a FUNCTION_REFERENCE_IMPL
     * class that was given the CoroutineImpl base (suspend ::refs — the
     * supertype check keys exactly on BrsCallableReferenceLowering's
     * isSuspendFunctionValue decision). Both must get the create/doResume
     * factory shape: runtime starters dispatch through
     * impl.create_..._k_(args, completion). Restricting this to LAMBDA_IMPL
     * left suspend ::refs without create — a device-pinned dispatch crash
     * (golden: coroutines/suspendFunctionReference).
     */
    private fun IrSimpleFunction.isSuspendFunctionValueInvoke(): Boolean {
        if (name != OperatorNameConventions.INVOKE) return false
        val parentClass = parentClassOrNull ?: return false
        return when (parentClass.origin) {
            WebCallableReferenceLowering.LAMBDA_IMPL -> true
            WebCallableReferenceLowering.FUNCTION_REFERENCE_IMPL ->
                coroutineSymbols.coroutineImpl != null &&
                    parentClass.superTypes.any { it.classOrNull == coroutineSymbols.coroutineImpl }
            else -> false
        }
    }

    /**
     * FUNCTION_REFERENCE_IMPL classes carry their bound receivers as f$N
     * fields created by the callable-reference lowering — NOT as
     * LocalDeclarationsLowering capture fields — but buildCreateMethod fills
     * the constructor's leading arguments from [capturedFields]. Prepend the
     * bound fields (declaration order == constructor parameter order) so the
     * generated create passes them through, and verify the constructor shape
     * loudly rather than miscompiling.
     */
    private fun prepareFunctionReferenceCapturedFields(function: IrSimpleFunction) {
        val klass = function.parentClassOrNull ?: return
        if (klass.origin !== WebCallableReferenceLowering.FUNCTION_REFERENCE_IMPL) return
        val boundFields = klass.declarations.filterIsInstance<IrField>().filter { it.name.asString().startsWith("f\$") }
        val ldlFields = (klass.capturedFields ?: emptyList()).filter { it !in boundFields }
        klass.capturedFields = boundFields + ldlFields
        val constructor = klass.declarations.filterIsInstance<IrConstructor>().single().let { it.capturedConstructor ?: it }
        val expectedParameters = boundFields.size + ldlFields.size + 1 // + continuation
        if (constructor.parameters.size != expectedParameters) {
            compilationException(
                "Suspend function reference class has unexpected constructor shape: " +
                    "${constructor.parameters.size} parameters, expected $expectedParameters " +
                    "(${boundFields.size} bound + ${ldlFields.size} captured + continuation)",
                klass
            )
        }
    }

    private fun removeReturnIfSuspendedCallAndSimplifyDelegatingCall(irFunction: IrFunction, delegatingCall: IrCall) {
        val returnValue =
            if (delegatingCall.isReturnIfSuspendedCall())
                delegatingCall.arguments[0]!!
            else delegatingCall

        val body = irFunction.body as IrBlockBody
        val statements = body.statements
        val lastStatement = statements.last()

        context.createIrBuilder(
            irFunction.symbol,
            startOffset = lastStatement.startOffset,
            endOffset = lastStatement.endOffset
        ).run {
            assert(lastStatement == delegatingCall || lastStatement is IrReturn) { "Unexpected statement $lastStatement" }

            // Create a temporary variable for the result and return it
            val tempVar = scope.createTemporaryVariable(
                generateDelegatedCall(irFunction.returnType, returnValue),
                irType = context.irBuiltIns.anyType,
            )
            statements[statements.lastIndex] = tempVar
            statements.add(irReturn(irGet(tempVar)))
        }
    }

    override fun buildStateMachine(
        stateMachineFunction: IrFunction,
        transformingFunction: IrFunction,
        argumentToPropertiesMap: Map<IrValueParameter, IrField>
    ) {
        // Transform finally blocks and returnable blocks
        val returnableBlockTransformer = ReturnableBlockTransformer(context)
        val finallyBlockTransformer = FinallyBlocksLowering(context, context.catchAllThrowableType)
        val simplifiedFunction =
            transformingFunction.transform(finallyBlockTransformer, null).transform(returnableBlockTransformer, null) as IrFunction

        val originalBody = simplifiedFunction.body as IrBlockBody

        val body = IrBlockImpl(
            simplifiedFunction.startOffset,
            simplifiedFunction.endOffset,
            context.irBuiltIns.unitType,
            BrsStatementOrigins.COROUTINE_IMPL,
            originalBody.statements
        )

        val coroutineClass = stateMachineFunction.parent as IrClass

        // Get coroutine symbols (may be null during stdlib compilation)
        val resultGetter = coroutineSymbols.coroutineImplResultSymbolGetter
        val resultSetter = coroutineSymbols.coroutineImplResultSymbolSetter
        val labelGetter = coroutineSymbols.coroutineImplLabelPropertyGetter
        val labelSetter = coroutineSymbols.coroutineImplLabelPropertySetter
        val exceptionGetter = coroutineSymbols.coroutineImplExceptionPropertyGetter
        val exceptionSetter = coroutineSymbols.coroutineImplExceptionPropertySetter
        val exStateGetter = coroutineSymbols.coroutineImplExceptionStatePropertyGetter
        val exStateSetter = coroutineSymbols.coroutineImplExceptionStatePropertySetter

        // If any required symbols are missing (stdlib compilation), skip state machine generation
        if (resultGetter == null || resultSetter == null ||
            labelGetter == null || labelSetter == null ||
            exceptionGetter == null || exceptionSetter == null ||
            exStateGetter == null || exStateSetter == null) {
            // During stdlib compilation, we can't generate proper state machines
            // Just keep the original body
            stateMachineFunction.body = context.irFactory.createBlockBody(
                stateMachineFunction.startOffset,
                stateMachineFunction.endOffset,
                originalBody.statements
            )
            return
        }

        val suspendResult = buildVar(
            context.irBuiltIns.anyNType,
            stateMachineFunction,
            "suspendResult",
            true,
            initializer = buildCall(resultGetter.symbol).apply {
                dispatchReceiver = buildGetValue(stateMachineFunction.dispatchReceiverParameter!!.symbol)
            }
        )

        val suspendState = buildVar(labelGetter.returnType, stateMachineFunction, "suspendState", true)

        val unit = context.irBuiltIns.unitType

        val switch = IrWhenImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, unit, BrsStatementOrigins.COROUTINE_SWITCH)
        val stateVar = buildVar(context.irBuiltIns.intType, stateMachineFunction)
        val switchBlock = IrBlockImpl(switch.startOffset, switch.endOffset, switch.type).apply {
            statements += stateVar
            statements += switch
        }
        val rootTry = IrTryImpl(body.startOffset, body.endOffset, unit).apply { tryResult = switchBlock }
        val rootLoop = IrDoWhileLoopImpl(
            body.startOffset,
            body.endOffset,
            unit,
            BrsStatementOrigins.COROUTINE_ROOT_LOOP,
        ).also {
            it.condition = buildBoolean(context.irBuiltIns.booleanType, true)
            it.body = rootTry
            it.label = "\$sm"
        }

        val suspendableNodes = collectSuspendableNodes(body)
        val thisReceiver = (stateMachineFunction.dispatchReceiverParameter as IrValueParameter).symbol
        stateVar.initializer = buildCall(labelGetter.symbol).apply {
            dispatchReceiver = buildGetValue(thisReceiver)
        }

        val stateMachineBuilder = BrsStateMachineBuilder(
            suspendableNodes,
            context,
            stateMachineFunction.symbol,
            rootLoop,
            exceptionGetter,
            exceptionSetter,
            exStateGetter,
            exStateSetter,
            labelSetter,
            thisReceiver,
            getSuspendResultAsType = { type ->
                buildImplicitCast(
                    buildGetValue(suspendResult.symbol),
                    type
                )
            },
            setSuspendResultValue = { value ->
                buildSetVariable(
                    suspendResult.symbol,
                    buildImplicitCast(
                        value,
                        context.irBuiltIns.anyNType
                    ),
                    unit
                )
            }
        )

        body.acceptVoid(stateMachineBuilder)

        stateMachineBuilder.finalizeStateMachine()

        rootTry.catches += stateMachineBuilder.globalCatch

        assignStateIds(stateMachineBuilder.entryState, stateVar.symbol, switch, rootLoop)

        // Set exceptionState to the global catch block
        stateMachineBuilder.entryState.entryBlock.run {
            val receiver = buildGetValue(coroutineClass.thisReceiver!!.symbol)
            val exceptionTrapId = stateMachineBuilder.rootExceptionTrap.id
            check(exceptionTrapId >= 0)
            val id = buildInt(context.irBuiltIns.intType, exceptionTrapId)
            statements.add(0, buildCall(exStateSetter.symbol).also { call ->
                call.arguments[0] = receiver
                call.arguments[1] = id
            })
        }

        val functionBody = context.irFactory.createBlockBody(
            stateMachineFunction.startOffset,
            stateMachineFunction.endOffset,
            stateMachineBuilder.allTheIntermediateLocals + suspendResult + rootLoop
        )

        stateMachineFunction.body = functionBody

        // Move return targets to new function
        functionBody.transformChildrenVoid(object : IrElementTransformerVoid() {
            override fun visitReturn(expression: IrReturn): IrExpression {
                expression.transformChildrenVoid(this)

                return if (expression.returnTargetSymbol != simplifiedFunction.symbol)
                    expression
                else
                    buildReturn(stateMachineFunction.symbol, expression.value, expression.type)
            }
        })

        // Perform liveness analysis and move live locals to coroutine class fields
        val liveLocals = LivenessAnalysis.run(functionBody, { it is IrCall && it.isSuspend })
            .values.flatten().toSet()

        val localToPropertyMap = hashMapOf<IrValueSymbol, IrFieldSymbol>()
        var localCounter = 0
        liveLocals.forEach {
            if (it !== suspendState && it !== suspendResult && it !== stateVar) {
                localToPropertyMap.getOrPut(it.symbol) {
                    // IR-special local names (<iterator>, <destruct>, ...) must be sanitized
                    // here: field accesses emit the name verbatim, and `m.<iterator>0` is a
                    // BrightScript syntax error (pinned by flow/suspendLambdaForLoopIterator).
                    val safeName = sanitizeParameterName(it.name.asString())
                    val field = coroutineClass.addField(Name.identifier("$safeName${localCounter++}"), it.type, (it as? IrVariable)?.isVar ?: false)
                    // If a shared (closure-boxed) variable moves to a coroutine field, record the
                    // field so the emitter keeps box semantics: reads/writes go through .value and
                    // constructor calls pass the box itself. Registered by SYMBOL — a
                    // "ClassName.fieldName" string key collides across sibling suspend lambdas
                    // (same raw class name) and reclassified unrelated same-named plain fields
                    // (pinned by flow/sharedBoxFieldKeyCollision).
                    if ((it as? IrVariable)?.origin == BrsDeclarationOrigin.SHARED_VARIABLE_WRAPPER) {
                        brsContext.sharedVariableBoxFields.add(field.symbol)
                    }
                    field.symbol
                }
            }
        }
        val isSuspendLambda = transformingFunction.parent === coroutineClass
        val parameters = if (isSuspendLambda) simplifiedFunction.nonDispatchParameters else simplifiedFunction.parameters
        for (parameter in parameters) {
            localToPropertyMap.getOrPut(parameter.symbol) {
                argumentToPropertiesMap.getValue(parameter).symbol
            }
        }

        stateMachineFunction.body!!.patchDeclarationParents(stateMachineFunction)
        stateMachineFunction.transform(BrsLiveLocalsTransformer(localToPropertyMap, { buildGetValue(thisReceiver) }, unit), null)
    }

    private fun assignStateIds(entryState: SuspendState, subject: IrVariableSymbol, switch: IrWhen, rootLoop: IrLoop) {
        val visited = mutableSetOf<SuspendState>()

        val sortedStates = DFS.topologicalOrder(listOf(entryState), { it.successors }, { visited.add(it) })
        sortedStates.withIndex().forEach { it.value.id = it.index }

        val eqeqeqInt = context.irBuiltIns.eqeqeqSymbol

        for (state in sortedStates) {
            val condition = buildCall(eqeqeqInt).apply {
                arguments[0] = buildGetValue(subject)
                arguments[1] = buildInt(context.irBuiltIns.intType, state.id)
            }

            switch.branches += IrBranchImpl(state.entryBlock.startOffset, state.entryBlock.endOffset, condition, state.entryBlock)
        }

        val dispatchPointTransformer = BrsDispatchPointTransformer {
            assert(it.id >= 0)
            buildInt(context.irBuiltIns.intType, it.id)
        }

        rootLoop.transformChildrenVoid(dispatchPointTransformer)
    }

    override fun IrBuilderWithScope.generateDelegatedCall(expectedType: IrType, delegatingCall: IrExpression): IrExpression {
        val functionReturnType = (delegatingCall as? IrCall)?.symbol?.owner?.let { function ->
            loweredSuspendFunctionReturnType(function, context.irBuiltIns)
        } ?: delegatingCall.type

        if (!needUnboxingOrUnit(functionReturnType, expectedType)) return delegatingCall

        return irComposite(resultType = expectedType) {
            val tmp = createTmpVariable(delegatingCall, irType = functionReturnType)
            val coroutineSuspended = irCall(coroutineSymbols.coroutineSuspendedGetter!!)
            val condition = irEqeqeq(irGet(tmp), coroutineSuspended)
            +irIfThen(context.irBuiltIns.unitType, condition, irReturn(irGet(tmp)))
            +irImplicitCast(irGet(tmp), expectedType)
        }
    }

    private fun needUnboxingOrUnit(fromType: IrType, toType: IrType): Boolean {
        return fromType.isUnit() && !toType.isUnit()
    }

    override fun IrBlockBodyBuilder.generateCoroutineStart(invokeSuspendFunction: IrFunction, receiver: IrExpression) {
        val resultSetter = coroutineSymbols.coroutineImplResultSymbolSetter
        val exceptionSetter = coroutineSymbols.coroutineImplExceptionPropertySetter

        if (resultSetter == null || exceptionSetter == null) {
            // During stdlib compilation, skip coroutine start generation
            return
        }

        val dispatchReceiverVar = createTmpVariable(receiver, irType = receiver.type)
        +irCall(resultSetter).apply {
            arguments[0] = irGet(dispatchReceiverVar)
            arguments[1] = irGetObject(context.irBuiltIns.unitClass)
        }
        +irCall(exceptionSetter).apply {
            arguments[0] = irGet(dispatchReceiverVar)
            arguments[1] = irNull()
        }
        val call = irCall(invokeSuspendFunction.symbol).apply {
            arguments[0] = irGet(dispatchReceiverVar)
        }
        val functionReturnType = scope.scopeOwnerSymbol.let { (it as IrSimpleFunctionSymbol).owner.returnType }
        +irReturn(generateDelegatedCall(functionReturnType, call))
    }

    // ==================== Suspend Function Analysis ====================

    private fun getSuspendFunctionKind(function: IrSimpleFunction, body: IrBody): SuspendFunctionKind {
        if (function.isSuspendFunctionValueInvoke())
            return SuspendFunctionKind.NEEDS_STATE_MACHINE // Suspend function values always need coroutine implementation.

        var numberOfSuspendCalls = 0
        body.acceptVoid(object : IrVisitorVoid() {
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }

            override fun visitCall(expression: IrCall) {
                expression.acceptChildrenVoid(this)
                if (expression.isSuspend)
                    ++numberOfSuspendCalls
            }
        })

        // Optimize: if there's only one suspend call at the end, we can delegate directly
        val lastCall = when (val lastStatement = (body as IrBlockBody).statements.lastOrNull()) {
            is IrCall ->
                if (lastStatement.type == context.irBuiltIns.unitType && function.returnType == context.irBuiltIns.unitType)
                    lastStatement
                else
                    null
            is IrReturn -> {
                var value: IrElement = lastStatement
                loop@ while (true) {
                    value = when {
                        value is IrBlock && value.statements.size == 1 -> value.statements.first()
                        value is IrReturn -> value.value
                        value is IrTypeOperatorCall && (value.operator == IrTypeOperator.IMPLICIT_CAST || value.operator == IrTypeOperator.IMPLICIT_COERCION_TO_UNIT) -> value.argument
                        else -> break@loop
                    }
                }
                value as? IrCall
            }
            else -> null
        }
        val suspendCallAtEnd = lastCall != null && lastCall.isSuspend

        return when {
            numberOfSuspendCalls == 0 -> SuspendFunctionKind.NO_SUSPEND_CALLS
            numberOfSuspendCalls == 1 && suspendCallAtEnd -> SuspendFunctionKind.DELEGATING(lastCall!!)
            else -> SuspendFunctionKind.NEEDS_STATE_MACHINE
        }
    }

    private fun IrCall.isReturnIfSuspendedCall() =
        symbol == context.symbols.returnIfSuspended

    // ==================== IR Builder Helpers ====================

    private fun buildVar(type: IrType, parent: IrDeclarationParent, name: String = "tmp", isMutable: Boolean = false, initializer: IrExpression? = null): IrVariable {
        return IrVariableImpl(
            startOffset = UNDEFINED_OFFSET,
            endOffset = UNDEFINED_OFFSET,
            origin = IrDeclarationOrigin.IR_TEMPORARY_VARIABLE,
            symbol = IrVariableSymbolImpl(),
            name = Name.identifier(name),
            type = type,
            isVar = isMutable,
            isConst = false,
            isLateinit = false,
        ).apply {
            this.parent = parent
            this.initializer = initializer
        }
    }

    private fun buildCall(symbol: IrSimpleFunctionSymbol) = IrCallImpl(
        UNDEFINED_OFFSET, UNDEFINED_OFFSET,
        symbol.owner.returnType,
        symbol,
        typeArgumentsCount = symbol.owner.typeParameters.size,
        origin = null
    )

    private fun buildGetValue(symbol: IrValueSymbol) = IrGetValueImpl(
        UNDEFINED_OFFSET, UNDEFINED_OFFSET,
        symbol.owner.type,
        symbol
    )

    private fun buildSetVariable(symbol: IrVariableSymbol, value: IrExpression, type: IrType) = IrSetValueImpl(
        UNDEFINED_OFFSET, UNDEFINED_OFFSET,
        type,
        symbol,
        value,
        null
    )

    private fun buildInt(type: IrType, value: Int) = IrConstImpl.int(
        UNDEFINED_OFFSET, UNDEFINED_OFFSET,
        type,
        value
    )

    private fun buildBoolean(type: IrType, value: Boolean) = IrConstImpl.boolean(
        UNDEFINED_OFFSET, UNDEFINED_OFFSET,
        type,
        value
    )

    private fun buildImplicitCast(value: IrExpression, toType: IrType) = IrTypeOperatorCallImpl(
        UNDEFINED_OFFSET, UNDEFINED_OFFSET,
        toType,
        IrTypeOperator.IMPLICIT_CAST,
        toType,
        value
    )

    private fun buildReturn(target: IrSimpleFunctionSymbol, value: IrExpression, type: IrType) = IrReturnImpl(
        UNDEFINED_OFFSET, UNDEFINED_OFFSET,
        type,
        target,
        value
    )

    private fun buildReturn(target: IrFunctionSymbol, value: IrExpression, type: IrType) = IrReturnImpl(
        UNDEFINED_OFFSET, UNDEFINED_OFFSET,
        type,
        target,
        value
    )
}

/**
 * Classification of suspend functions by their suspension behavior.
 */
internal sealed class SuspendFunctionKind {
    /** No suspend calls in the function body - can be left as-is */
    object NO_SUSPEND_CALLS : SuspendFunctionKind()

    /** Single suspend call at the end that can be delegated without a state machine */
    class DELEGATING(val delegatingCall: IrCall) : SuspendFunctionKind()

    /** Multiple suspend calls or complex control flow - needs full state machine */
    object NEEDS_STATE_MACHINE : SuspendFunctionKind()
}

/**
 * Statement origins for coroutine-related IR nodes.
 */
object BrsStatementOrigins {
    val COROUTINE_IMPL = IrStatementOriginImpl("COROUTINE_IMPL")
    val COROUTINE_SWITCH = IrStatementOriginImpl("COROUTINE_SWITCH")
    val COROUTINE_ROOT_LOOP = IrStatementOriginImpl("COROUTINE_ROOT_LOOP")

    // Marks the assignment that initializes a SHARED_VARIABLE_WRAPPER variable after the
    // state machine hoists its declaration. The emitter must create the {value: ...} box
    // at this assignment (all other writes to the variable assign through .value).
    val SHARED_BOX_INIT = IrStatementOriginImpl("SHARED_BOX_INIT")

    // Marks a constructor-input write emitted by BrsComponentConstructorCallLowering: the
    // receiver is the freshly created roSGNode HANDLE, so the emitter writes the node field
    // directly (`n.field = v`), never through the component-self `.top` route.
    val COMPONENT_INPUT_WRITE = IrStatementOriginImpl("COMPONENT_INPUT_WRITE")

    // IO Worker extraction origins
    val IO_WORKER_CALL = IrStatementOriginImpl("IO_WORKER_CALL")
    val IO_WORKER_CAPTURES = IrStatementOriginImpl("IO_WORKER_CAPTURES")
}
