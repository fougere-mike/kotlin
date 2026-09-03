/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.lower

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.backend.brs.BrsComponentInfo
import org.jetbrains.kotlin.ir.backend.brs.BrsFieldInfo
import org.jetbrains.kotlin.ir.backend.brs.BrsFieldTypes
import org.jetbrains.kotlin.ir.backend.brs.BrsIrBackendContext
import org.jetbrains.kotlin.ir.backend.brs.KOTLIN_TASK_ERROR_FIELD
import org.jetbrains.kotlin.ir.backend.brs.KOTLIN_TASK_STATE_FIELD
import org.jetbrains.kotlin.ir.builders.declarations.addConstructor
import org.jetbrains.kotlin.ir.builders.declarations.addFunction
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.declarations.buildClass
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.createBlockBody
import org.jetbrains.kotlin.ir.declarations.impl.IrFileImpl
import org.jetbrains.kotlin.ir.declarations.name
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.IrTypeOperatorCall
import org.jetbrains.kotlin.ir.expressions.impl.IrBlockImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrDelegatingConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionExpressionImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrInstanceInitializerCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrReturnImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrFileSymbolImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.NaiveSourceBasedFileEntryImpl
import org.jetbrains.kotlin.ir.util.companionObject
import org.jetbrains.kotlin.ir.util.createThisReceiverParameter
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.patchDeclarationParents
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.BrsStandardClassIds
import org.jetbrains.kotlin.name.Name

/**
 * The flowOn/spawnTask task lift (flow-program spec §5): rewrites
 *
 *  - `<literal flow chain>.flowOn(Dispatchers.Task)` into
 *    `taskFlowLifted<T>("<componentName>", <capturesAA>)`, lifting the ENTIRE
 *    upstream chain expression into a private top-level factory
 *    `__flowUpstream_<san>_<n>(captures: RoAssociativeArray?): Flow<T>`, and
 *  - `spawnTask { <literal block> }` into
 *    `spawnTaskLifted<R>("<componentName>", <capturesAA>)` (one suspend call
 *    replacing another, before any coroutine lowering — the
 *    BrsRunTaskCallLowering template), lifting the block into
 *    `__spawnBlock_<san>_<n>(captures: RoAssociativeArray?): R`,
 *
 * and synthesizes ONE task component per rewritten call site:
 *
 * ```
 * class KotlinFlowTask_<san>_<n> : FlowTaskComponent() {
 *     override fun run() {
 *         driveFlowTask(this.top, __flowUpstream_<san>_<n>(readCapturesFrom(this.top)))
 *         // spawnTask form: driveSpawnTask(this.top) { caps -> __spawnBlock_<san>_<n>(caps) }
 *     }
 * }
 * ```
 *
 * THE this.top LAW: run() hands the drivers `this.top` — in component scope
 * that read emits `m.top`, the REAL task node. `this` alone emits `m`, the
 * m-scope AA, and every driver field access through it would silently miss the
 * node. The drivers are node-typed for exactly this reason (their KDocs
 * cross-reference this pass; keep signatures in sync).
 *
 * Naming: the component/class/file name is LETTER-FIRST
 * (underscore-leading SceneGraph subtype names are device-unpinned);
 * `<san>` is the sanitized file FQ-NAME (package + file name —
 * path-collision-proof, unlike the per-file ScopeRunBlock ordinal alone) and
 * `<n>` a 1-based per-file ordinal SHARED by both lift kinds, so component
 * names stay unique within the file.
 *
 * Per-site synthesis is the design's answer to the ScopeHandle binding-table
 * subset trap: each component's include closure is self-contained — the
 * synthesized class lives in a NEW synthetic IrFile named EXACTLY after the
 * component (load-bearing twice: BrsCompiler.writeOutput routes the .brs to
 * components/<Name>/ by file-name match, and the component XML hard-codes its
 * own script URI from the name), registered via
 * [BrsIrBackendContext.pendingSyntheticFiles] so BrsLoweringPhases joins it to
 * module.files between phases (every later pass and BrsCompiler Passes 1–3
 * then process it like an ordinary file). The lifted functions are appended to
 * the ORIGINATING file (ScopeRunBlock precedent) and ride the function
 * manifest + dependency recording; the synthesized run()'s calls to the
 * drivers and the lifted function record the deps that make the component's
 * include closure pull the originating file and the flow klib.
 *
 * The component's XML/deps.json come from a HAND-CONSTRUCTED
 * [BrsComponentInfo] in [BrsIrBackendContext.synthesizedComponents] (merged
 * into the pre-extracted map before Pass 3): the extractor's inherited walk
 * (stopAtUserComponents) stops at FlowTaskComponent — an isComponent-true
 * ancestor — and would collect ZERO of the six protocol fields, turning every
 * protocol write into an undeclared-field silent no-op. The six entries and
 * the extends value are pinned to the stdlib FlowTaskComponent declaration
 * (SceneComponent.kt) — change BOTH in the same commit.
 *
 * Chain validation consumes [BrsStandardClassIds.Callables.flowLiftChainCallables]
 * — THE whitelist FirBrsFlowLiftChecker validates against (keep-in-sync law
 * KDoc'd there); a receiver this pass cannot lift was already rejected at FIR
 * (BRS_FLOW_UPSTREAM_NOT_LITERAL / BRS_FLOW_ON_INVALID_DISPATCHER), so on any
 * unexpected shape (and when any required symbol is missing — stdlib
 * compilation, no flow klib linked) the pass bails and leaves the call to the
 * guided-throw stdlib backstop.
 */
class BrsFlowTaskLiftLowering(
    private val context: BrsIrBackendContext
) : FileLoweringPass {

    private val flowOnFqName = BrsStandardClassIds.Callables.flowOn.asSingleFqName()
    private val spawnTaskFqName = BrsStandardClassIds.Callables.spawnTask.asSingleFqName()

    // THE lifted-chain whitelist — the same set FirBrsFlowLiftChecker consumes
    // (BrsStandardClassIds keep-in-sync law: additions land there, never here).
    private val chainHopFqNames =
        BrsStandardClassIds.Callables.flowLiftChainCallables.map { it.asSingleFqName() }.toSet()
    private val builderFqNames =
        BrsStandardClassIds.Callables.flowBuilderCallables.map { it.asSingleFqName() }.toSet()
    private val combineFqName = BrsStandardClassIds.Callables.flowCombine.asSingleFqName()
    private val flowInterfaceFqName = BrsStandardClassIds.Flow.flowInterface.asSingleFqName()
    private val dispatchersFqName = BrsStandardClassIds.Callables.dispatchersClassId.asSingleFqName()
    private val componentBaseFqName = BrsStandardClassIds.Components.ComponentBase.asSingleFqName()

    /** Everything the lift needs resolved; null (bail) when any piece is missing. */
    private class LiftTargets(
        val taskFlowLifted: IrSimpleFunctionSymbol,
        val spawnTaskLifted: IrSimpleFunctionSymbol,
        val driveFlowTask: IrSimpleFunctionSymbol,
        val driveSpawnTask: IrSimpleFunctionSymbol,
        val readCapturesFrom: IrSimpleFunctionSymbol,
        val flowTaskComponent: IrClassSymbol,
        val flowTaskComponentCtor: IrConstructor,
        val taskRun: IrSimpleFunction,
        val topGetter: IrSimpleFunction,
        val aaCreate: IrSimpleFunction,
        val aaLookup: IrSimpleFunction,
        val aaAddReplace: IrSimpleFunction,
        val capturesType: IrType,
    )

    private fun resolveTargets(): LiftTargets? {
        val symbols = context.brsSymbols
        val taskFlowLifted = symbols.taskFlowLiftedOrNull ?: return null
        val spawnTaskLifted = symbols.spawnTaskLiftedOrNull ?: return null
        val driveFlowTask = symbols.driveFlowTaskOrNull ?: return null
        val driveSpawnTask = symbols.driveSpawnTaskOrNull ?: return null
        val readCapturesFrom = symbols.readCapturesFromOrNull ?: return null
        val flowTaskComponent = symbols.flowTaskComponentClass ?: return null
        val flowTaskComponentCtor = flowTaskComponent.owner.declarations
            .filterIsInstance<IrConstructor>().firstOrNull { it.isPrimary } ?: return null
        val taskComponent = context.intrinsics.taskComponentClass?.owner ?: return null
        val taskRun = taskComponent.declarations.filterIsInstance<IrSimpleFunction>()
            .singleOrNull { it.name.asString() == "run" && it.valueParameters.isEmpty() } ?: return null
        val componentBase = taskComponent.superTypes
            .mapNotNull { it.classOrNull?.owner }
            .firstOrNull { it.fqNameWhenAvailable == componentBaseFqName } ?: return null
        val topGetter = componentBase.declarations.filterIsInstance<IrProperty>()
            .singleOrNull { it.name.asString() == "top" }?.getter ?: return null
        val aaClass = symbols.roAssociativeArrayClass ?: return null
        val iaaClass = symbols.iAssociativeArrayClass ?: return null
        val aaCreate = aaClass.owner.companionObject()?.functions
            ?.singleOrNull { it.name.asString() == "create" } ?: return null
        val aaLookup = iaaClass.owner.functions.singleOrNull { it.name.asString() == "lookup" } ?: return null
        val aaAddReplace = iaaClass.owner.functions.singleOrNull { it.name.asString() == "addReplace" } ?: return null
        return LiftTargets(
            taskFlowLifted, spawnTaskLifted, driveFlowTask, driveSpawnTask, readCapturesFrom,
            flowTaskComponent, flowTaskComponentCtor, taskRun, topGetter,
            aaCreate, aaLookup, aaAddReplace,
            capturesType = aaClass.owner.defaultType.makeNullable(),
        )
    }

    override fun lower(irFile: IrFile) {
        val targets = resolveTargets() ?: return

        val fileFq = irFile.packageFqName.asString().let { pkg ->
            val fileName = irFile.name.removeSuffix(".kt")
            if (pkg.isEmpty()) fileName else "$pkg.$fileName"
        }
        val san = fileFq.replace(Regex("[^A-Za-z0-9_]"), "_")

        val liftedFunctions = mutableListOf<IrSimpleFunction>()
        var ordinal = 0

        irFile.transformChildrenVoid(object : IrElementTransformerVoid() {

            private var currentFunction: IrFunction? = null

            override fun visitFunction(declaration: IrFunction): IrStatement {
                val previous = currentFunction
                currentFunction = declaration
                val result = super.visitFunction(declaration)
                currentFunction = previous
                return result
            }

            override fun visitCall(expression: IrCall): IrExpression {
                expression.transformChildrenVoid(this)
                return when (expression.symbol.owner.fqNameWhenAvailable) {
                    flowOnFqName -> rewriteFlowOn(expression, currentFunction) ?: expression
                    spawnTaskFqName -> rewriteSpawnTask(expression, currentFunction) ?: expression
                    else -> expression
                }
            }

            private fun rewriteFlowOn(expression: IrCall, enclosing: IrFunction?): IrExpression? {
                if (expression.symbol.owner.valueParameters.size != 1) return null
                // The lift is selected by the LITERAL Dispatchers.Task token; any
                // other argument was rejected at FIR (BRS_FLOW_ON_INVALID_DISPATCHER).
                val dispatcherArg = expression.getValueArgument(0) ?: return null
                if (!isDispatchersTaskToken(dispatcherArg)) return null
                val receiver = expression.extensionReceiver ?: return null
                if (!isLiteralChain(receiver)) return null
                val elementType = expression.typeArguments.getOrNull(0) ?: return null

                val captures = collectLiftCaptures(receiver, emptyList())
                // Call-site temps need a parent; a captureless site needs none.
                if (captures.isNotEmpty() && enclosing == null) return null

                ordinal += 1
                val componentName = "KotlinFlowTask_${san}_$ordinal"
                val lifted = buildUpstreamFactory("__flowUpstream_${san}_$ordinal", receiver, captures, targets)
                liftedFunctions += lifted
                synthesizeTaskComponent(componentName, irFile, lifted, expression, targets, isFlowLift = true)
                return buildLiftedEntryCall(
                    expression, targets.taskFlowLifted, elementType, componentName, captures, enclosing, targets
                )
            }

            private fun rewriteSpawnTask(expression: IrCall, enclosing: IrFunction?): IrExpression? {
                if (expression.symbol.owner.valueParameters.size != 1) return null
                // Non-literal block: FIR guard errors; a suppressed call keeps the backstop.
                val blockArg = expression.getValueArgument(0) as? IrFunctionExpression ?: return null
                val resultType = expression.typeArguments.getOrNull(0) ?: return null
                val lambda = blockArg.function
                if (lambda.body !is IrBlockBody) return null

                val captures = collectLiftCaptures(lambda.body, lambda.parameters)
                if (captures.isNotEmpty() && enclosing == null) return null

                ordinal += 1
                val componentName = "KotlinFlowTask_${san}_$ordinal"
                val lifted = buildSpawnBlockFunction("__spawnBlock_${san}_$ordinal", lambda, captures, targets)
                liftedFunctions += lifted
                synthesizeTaskComponent(componentName, irFile, lifted, expression, targets, isFlowLift = false)
                return buildLiftedEntryCall(
                    expression, targets.spawnTaskLifted, resultType, componentName, captures, enclosing, targets
                )
            }
        })

        for (lifted in liftedFunctions) {
            lifted.parent = irFile
            irFile.declarations.add(lifted)
        }
    }

    // ==================== Match validation ====================

    /** The literal `Dispatchers.Task` token: a read of the Task property on kotlin.coroutines.dispatchers.Dispatchers. */
    private fun isDispatchersTaskToken(expression: IrExpression): Boolean {
        var unwrapped: IrExpression = expression
        while (unwrapped is IrTypeOperatorCall) unwrapped = unwrapped.argument
        val call = unwrapped as? IrCall ?: return false
        val property = call.symbol.owner.correspondingPropertySymbol?.owner ?: return false
        if (property.name.asString() != "Task") return false
        val holder = property.parent as? IrClass ?: return false
        return holder.fqNameWhenAvailable == dispatchersFqName
    }

    /**
     * IR mirror of FirBrsFlowLiftChecker.walkChain over the SAME whitelist:
     * every hop a call whose FQ name is in flowLiftChainCallables, every
     * functional argument a literal lambda, Flow-typed arguments continuing
     * the chain (combine), builders bottoming it out. Data arguments are fine
     * — they move task-side with the region and their free values are
     * captures.
     */
    private fun isLiteralChain(expression: IrExpression): Boolean {
        val call = expression as? IrCall ?: return false
        val fq = call.symbol.owner.fqNameWhenAvailable ?: return false
        if (fq !in chainHopFqNames) return false

        for (index in 0 until call.valueArgumentsCount) {
            val argument = call.getValueArgument(index) ?: continue
            when {
                argument is IrFunctionExpression -> {}
                argument.type.classFqName == flowInterfaceFqName -> if (!isLiteralChain(argument)) return false
                argument.type.isFunctionLike() -> return false
                else -> {}
            }
        }

        return when {
            fq in builderFqNames -> true // bottom of the chain (asFlow's receiver is a data expression)
            fq == combineFqName -> true // receiverless; chain continued via Flow args above
            else -> {
                val receiver = call.extensionReceiver ?: return false
                isLiteralChain(receiver)
            }
        }
    }

    private fun IrType.isFunctionLike(): Boolean {
        val fq = classFqName?.asString() ?: return false
        return fq.startsWith("kotlin.Function") || fq.startsWith("kotlin.coroutines.SuspendFunction")
    }

    // ==================== The lifted functions ====================

    /**
     * flowOn: `private fun __flowUpstream_<san>_<n>(captures: RoAssociativeArray?): Flow<T>`
     * — a NON-suspend factory that re-declares the captures as locals and
     * returns the moved chain expression (the suspend lambdas inside stay
     * lambdas and ride the ordinary pipeline). Evaluation of the chain thereby
     * moves from the flowOn call site to task-side collection — the spec's
     * explicit-hoist law exists exactly so this shift is visible in user code.
     */
    private fun buildUpstreamFactory(
        functionName: String,
        receiver: IrExpression,
        captures: List<LiftCapture>,
        targets: LiftTargets,
    ): IrSimpleFunction {
        val lifted = context.irFactory.buildFun {
            startOffset = receiver.startOffset
            endOffset = receiver.endOffset
            origin = IrDeclarationOrigin.DEFINED
            name = Name.identifier(functionName)
            visibility = DescriptorVisibilities.PRIVATE
            modality = Modality.FINAL
            returnType = receiver.type
        }
        val capturesParam = lifted.addValueParameter {
            name = Name.identifier("captures")
            type = targets.capturesType
        }

        val statements = mutableListOf<IrStatement>()
        val (prologue, remapping) = buildCaptureLocals(context, lifted, capturesParam, captures, targets.aaLookup.symbol)
        statements += prologue
        // The chain root is always an IrCall (isLiteralChain), so remapping its
        // CHILDREN covers every captured reference; no returns to retarget —
        // returns inside the region's lambdas target those lambdas.
        remapLiftedRegion(receiver, remapping)
        statements += IrReturnImpl(
            UNDEFINED_OFFSET, UNDEFINED_OFFSET, context.irBuiltIns.nothingType, lifted.symbol, receiver
        )

        lifted.body = context.irFactory.createBlockBody(UNDEFINED_OFFSET, UNDEFINED_OFFSET, statements)
        lifted.body!!.patchDeclarationParents(lifted)
        return lifted
    }

    /**
     * spawnTask: `private fun __spawnBlock_<san>_<n>(captures: RoAssociativeArray?): R`
     * — NON-suspend (the block's declared type is a plain function type), body
     * = capture prologue + the block's statements with captured references
     * remapped and returns retargeted (ScopeRunBlock template).
     */
    private fun buildSpawnBlockFunction(
        functionName: String,
        lambda: IrSimpleFunction,
        captures: List<LiftCapture>,
        targets: LiftTargets,
    ): IrSimpleFunction {
        val lifted = context.irFactory.buildFun {
            startOffset = lambda.startOffset
            endOffset = lambda.endOffset
            origin = IrDeclarationOrigin.DEFINED
            name = Name.identifier(functionName)
            visibility = DescriptorVisibilities.PRIVATE
            modality = Modality.FINAL
            returnType = lambda.returnType
        }
        val capturesParam = lifted.addValueParameter {
            name = Name.identifier("captures")
            type = targets.capturesType
        }

        val statements = mutableListOf<IrStatement>()
        val (prologue, remapping) = buildCaptureLocals(context, lifted, capturesParam, captures, targets.aaLookup.symbol)
        statements += prologue

        // Shape guaranteed by the rewrite's pre-check; a miss here is a broken assumption.
        val body = lambda.body as? IrBlockBody
            ?: error("spawnTask block lambda has no block body: $functionName")
        remapLiftedRegion(body, remapping, retargetReturnsFrom = lambda, retargetReturnsTo = lifted)
        statements += body.statements

        lifted.body = context.irFactory.createBlockBody(UNDEFINED_OFFSET, UNDEFINED_OFFSET, statements)
        lifted.body!!.patchDeclarationParents(lifted)
        return lifted
    }

    // ==================== The rewritten call site ====================

    /**
     * `taskFlowLifted<T>("<name>", captures)` / `spawnTaskLifted<R>("<name>", captures)`.
     * Zero captures pass `invalid`; otherwise a block builds the AA
     * (`CreateObject("roAssociativeArray")` + one `addReplace` per capture)
     * first — the ScopeRunBlock call-site shape minus the receiver temp (the
     * chain receiver MOVED into the lifted factory; its evaluation happens
     * task-side by design).
     */
    private fun buildLiftedEntryCall(
        original: IrCall,
        target: IrSimpleFunctionSymbol,
        typeArgument: IrType,
        componentName: String,
        captures: List<LiftCapture>,
        enclosing: IrFunction?,
        targets: LiftTargets,
    ): IrExpression {
        fun entryCall(capturesExpression: IrExpression): IrCallImpl =
            IrCallImpl(
                original.startOffset, original.endOffset,
                original.type,
                target,
                typeArgumentsCount = 1,
            ).apply {
                putTypeArgument(0, typeArgument)
                putValueArgument(0, liftStringConst(context, componentName))
                putValueArgument(1, capturesExpression)
            }

        if (captures.isEmpty()) {
            return entryCall(liftNullCaptures(targets.capturesType))
        }

        val capturesVar = buildCapturesVariable("flowCaptures", targets.aaCreate, enclosing!!)
        return IrBlockImpl(original.startOffset, original.endOffset, original.type).apply {
            statements += capturesVar
            for (capture in captures) {
                statements += buildAddReplaceCall(context, targets.aaAddReplace, capturesVar, capture)
            }
            statements += entryCall(
                IrGetValueImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, capturesVar.type, capturesVar.symbol)
            )
        }
    }

    // ==================== The synthesized component ====================

    /**
     * `class KotlinFlowTask_<san>_<n> : FlowTaskComponent() { override fun run() ... }`
     * in a NEW synthetic IrFile named after the component, plus the
     * hand-constructed six-field [BrsComponentInfo] (see the class KDoc for
     * why both are shaped this way).
     */
    private fun synthesizeTaskComponent(
        componentName: String,
        originatingFile: IrFile,
        liftedFunction: IrSimpleFunction,
        originalCall: IrCall,
        targets: LiftTargets,
        isFlowLift: Boolean,
    ) {
        val irClass = context.irFactory.buildClass {
            startOffset = UNDEFINED_OFFSET
            endOffset = UNDEFINED_OFFSET
            origin = IrDeclarationOrigin.DEFINED
            name = Name.identifier(componentName)
            kind = ClassKind.CLASS
            visibility = DescriptorVisibilities.PUBLIC
            modality = Modality.FINAL
        }
        irClass.superTypes = listOf(targets.flowTaskComponent.owner.defaultType)
        irClass.createThisReceiverParameter()

        // Primary constructor: delegate to FlowTaskComponent() — the generated
        // component init() comes from transformComponentInitBlock, which needs
        // a primary constructor to hang the functionName wiring on (both
        // synthesized statements are skipped-shape-free: the delegating call
        // and the instance-initializer call are filtered there).
        val constructor = irClass.addConstructor {
            isPrimary = true
            visibility = DescriptorVisibilities.PUBLIC
        }
        constructor.body = context.irFactory.createBlockBody(
            UNDEFINED_OFFSET, UNDEFINED_OFFSET,
            listOf(
                IrDelegatingConstructorCallImpl(
                    UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                    context.irBuiltIns.unitType,
                    targets.flowTaskComponentCtor.symbol,
                    typeArgumentsCount = 0,
                ),
                IrInstanceInitializerCallImpl(
                    UNDEFINED_OFFSET, UNDEFINED_OFFSET, irClass.symbol, context.irBuiltIns.unitType
                ),
            )
        )

        // The concrete run() — the ONE member the leaf declares. Locally
        // declared and non-abstract, so generateTaskMainFunction emits this
        // component's own __kotlinTaskMain wrapper around it.
        val run = irClass.addFunction(
            name = "run",
            returnType = context.irBuiltIns.unitType,
            modality = Modality.FINAL,
            visibility = DescriptorVisibilities.PROTECTED,
        )
        run.overriddenSymbols = listOf(targets.taskRun.symbol)
        val thisParameter = run.dispatchReceiverParameter
            ?: error("addFunction did not create a dispatch receiver for $componentName.run")

        // this.top — THE this.top law (class KDoc): component-scope emission
        // turns this getter call into m.top, the real task node.
        fun topRead(): IrExpression = IrCallImpl(
            UNDEFINED_OFFSET, UNDEFINED_OFFSET,
            targets.topGetter.returnType,
            targets.topGetter.symbol,
            typeArgumentsCount = 0,
        ).apply {
            dispatchReceiver = IrGetValueImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, thisParameter.type, thisParameter.symbol)
        }

        val driveCall: IrExpression = if (isFlowLift) {
            // driveFlowTask(this.top, __flowUpstream_<san>_<n>(readCapturesFrom(this.top)))
            IrCallImpl(
                UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                context.irBuiltIns.unitType,
                targets.driveFlowTask,
                typeArgumentsCount = 0,
            ).apply {
                putValueArgument(0, topRead())
                putValueArgument(1, IrCallImpl(
                    UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                    liftedFunction.returnType,
                    liftedFunction.symbol,
                    typeArgumentsCount = 0,
                ).apply {
                    putValueArgument(0, IrCallImpl(
                        UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                        targets.capturesType,
                        targets.readCapturesFrom,
                        typeArgumentsCount = 0,
                    ).apply { putValueArgument(0, topRead()) })
                })
            }
        } else {
            // driveSpawnTask(this.top) { caps -> __spawnBlock_<san>_<n>(caps) }
            val lambdaFunction = context.irFactory.buildFun {
                // Real per-site offsets: anonymous-class naming hashes offsets,
                // so two spawn lifts in one file must not collide.
                startOffset = originalCall.startOffset
                endOffset = originalCall.endOffset
                origin = IrDeclarationOrigin.LOCAL_FUNCTION_FOR_LAMBDA
                name = Name.special("<anonymous>")
                visibility = DescriptorVisibilities.LOCAL
                modality = Modality.FINAL
                returnType = context.irBuiltIns.anyNType
            }
            val capsParameter = lambdaFunction.addValueParameter {
                name = Name.identifier("caps")
                type = targets.capturesType
            }
            lambdaFunction.body = context.irFactory.createBlockBody(
                UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                listOf(
                    IrReturnImpl(
                        UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                        context.irBuiltIns.nothingType,
                        lambdaFunction.symbol,
                        IrCallImpl(
                            UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                            liftedFunction.returnType,
                            liftedFunction.symbol,
                            typeArgumentsCount = 0,
                        ).apply {
                            putValueArgument(
                                0,
                                IrGetValueImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, capsParameter.type, capsParameter.symbol)
                            )
                        }
                    )
                )
            )
            lambdaFunction.parent = run
            val lambdaType = context.irBuiltIns.functionN(1)
                .typeWith(targets.capturesType, context.irBuiltIns.anyNType)
            IrCallImpl(
                UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                context.irBuiltIns.unitType,
                targets.driveSpawnTask,
                typeArgumentsCount = 0,
            ).apply {
                putValueArgument(0, topRead())
                putValueArgument(
                    1,
                    IrFunctionExpressionImpl(
                        originalCall.startOffset, originalCall.endOffset,
                        lambdaType, lambdaFunction, IrStatementOrigin.LAMBDA
                    )
                )
            }
        }
        run.body = context.irFactory.createBlockBody(UNDEFINED_OFFSET, UNDEFINED_OFFSET, listOf(driveCall))

        // The synthetic file: name = component name, load-bearing twice
        // (writeOutput routing; the XML's hard-coded own-script URI).
        val syntheticFile = IrFileImpl(
            NaiveSourceBasedFileEntryImpl("$componentName.kt"),
            IrFileSymbolImpl(),
            originatingFile.packageFqName,
        )
        syntheticFile.module = originatingFile.module
        irClass.parent = syntheticFile
        syntheticFile.declarations.add(irClass)
        context.pendingSyntheticFiles += syntheticFile

        // Hand-constructed component info: the six-field truth of
        // FlowTaskComponent's flattened hierarchy (kotlinTask* from
        // TaskComponent + flow* from FlowTaskComponent) and its annotation's
        // extends value — pinned to SceneComponent.kt; change both in the
        // same commit. Field order: protocol fields first, flow fields after
        // (the golden pins the XML).
        context.synthesizedComponents[componentName] = BrsComponentInfo(
            irClass = irClass,
            name = componentName,
            extendsComponent = "Task",
            fields = listOf(
                BrsFieldInfo(name = KOTLIN_TASK_STATE_FIELD, type = BrsFieldTypes.STRING, alwaysNotify = true),
                BrsFieldInfo(name = KOTLIN_TASK_ERROR_FIELD, type = BrsFieldTypes.ASSOC_ARRAY),
                BrsFieldInfo(name = "kotlinTaskId", type = BrsFieldTypes.INTEGER),
                BrsFieldInfo(name = "flowCaptures", type = BrsFieldTypes.ASSOC_ARRAY),
                BrsFieldInfo(name = "flowOut", type = BrsFieldTypes.ASSOC_ARRAY, alwaysNotify = true),
                BrsFieldInfo(name = "flowCancel", type = BrsFieldTypes.BOOLEAN),
            ),
            exports = emptyList(),
        )
    }
}
