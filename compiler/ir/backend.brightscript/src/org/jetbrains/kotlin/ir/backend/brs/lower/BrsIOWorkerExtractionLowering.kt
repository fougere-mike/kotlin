/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.lower

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.backend.brs.BrsIrBackendContext
import org.jetbrains.kotlin.ir.backend.brs.lower.coroutines.BrsStatementOrigins
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.builders.declarations.buildVariable
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.symbols.IrValueSymbol
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.Name

/**
 * Transforms `withContext(Dispatchers.IO)` blocks into the worker registry pattern.
 *
 * ## Problem
 *
 * BrightScript Task threads receive a **clone** of `m` (the component scope).
 * Function references (lambdas) don't survive this cloning process. When you pass
 * a lambda to a Task thread via field data, it becomes invalid.
 *
 * ## Solution: Automatic Worker Extraction
 *
 * This lowering transforms:
 * ```kotlin
 * val result = withContext(Dispatchers.IO) {
 *     val http = RoUrlTransfer.create()
 *     http.setUrl(url)  // 'url' captured from enclosing scope
 *     http.getToString() ?: ""
 * }
 * ```
 *
 * Into:
 * ```kotlin
 * // Generated top-level worker function
 * fun __ioWorker_MyClass_myFunc_1(captures: Dynamic?): String {
 *     val cap = captures as? RoAssociativeArray ?: return ""
 *     val url = cap.lookup("url") as? String ?: return ""
 *     val http = RoUrlTransfer.create()
 *     http.setUrl(url)
 *     http.getToString() ?: ""
 * }
 *
 * // Registration (stored for code generation)
 * IOWorkerRegistry.register("__ioWorker_MyClass_myFunc_1", ::__ioWorker_MyClass_myFunc_1)
 *
 * // Replacement at call site
 * val result = runIOWorker<String>("__ioWorker_MyClass_myFunc_1") {
 *     put("url", url)
 * }
 * ```
 *
 * ## Limitations
 *
 * - **`this` references**: Lambdas that capture `this` from an instance method cannot be
 *   extracted because instance state cannot cross the Task thread boundary. An error is emitted.
 * - **Non-serializable captures**: Complex objects that don't survive cloning will fail at runtime.
 *   A warning is emitted for potentially problematic captures.
 *
 * @see kotlin.coroutines.task.IOWorkerRegistry
 * @see kotlin.coroutines.task.runIOWorker
 */
class BrsIOWorkerExtractionLowering(
    private val context: BrsIrBackendContext
) : FileLoweringPass {

    /**
     * Information about a captured variable.
     */
    data class CapturedVariable(
        val symbol: IrValueSymbol,
        val name: String,
        val type: IrType,
        val isThis: Boolean
    )

    /**
     * Types that are known to serialize safely across Task thread boundaries.
     */
    private val serializableTypeNames = setOf(
        "Int", "Long", "Float", "Double", "Boolean", "String",
        "RoArray", "RoAssociativeArray", "Dynamic"
    )

    override fun lower(irFile: IrFile) {
        val generatedWorkers = mutableListOf<IrSimpleFunction>()

        // Transform the file, collecting generated workers
        irFile.transformChildrenVoid(WithContextTransformer(irFile, generatedWorkers))

        // Add generated workers to the file
        for (worker in generatedWorkers) {
            worker.parent = irFile
            irFile.declarations.add(worker)
        }
    }

    private inner class WithContextTransformer(
        private val irFile: IrFile,
        private val generatedWorkers: MutableList<IrSimpleFunction>
    ) : IrElementTransformerVoid() {

        private var currentClass: IrClass? = null
        private var currentFunction: IrFunction? = null

        override fun visitClass(declaration: IrClass): IrStatement {
            val previousClass = currentClass
            currentClass = declaration
            val result = super.visitClass(declaration)
            currentClass = previousClass
            return result
        }

        override fun visitFunction(declaration: IrFunction): IrStatement {
            val previousFunction = currentFunction
            currentFunction = declaration
            val result = super.visitFunction(declaration)
            currentFunction = previousFunction
            return result
        }

        override fun visitCall(expression: IrCall): IrExpression {
            // First transform children
            expression.transformChildrenVoid(this)

            // Check if this is a withContext call
            val callee = expression.symbol.owner
            if (!isWithContextCall(callee)) {
                return expression
            }

            // Get the context argument - check if it's Dispatchers.IO
            val contextArg = expression.getValueArgument(0) ?: return expression
            if (!isDispatchersIO(contextArg)) {
                return expression
            }

            // Get the lambda argument
            val lambdaArg = expression.getValueArgument(1) ?: return expression

            // Extract the lambda function
            val lambdaFunction = extractLambdaFunction(lambdaArg)
            if (lambdaFunction == null) {
                // Couldn't extract lambda - emit warning and keep original
                context.reportWarning(
                    expression,
                    "withContext(Dispatchers.IO): Could not extract lambda for automatic worker generation. " +
                    "Use runIOWorker() with IOWorkerRegistry instead."
                )
                return expression
            }

            // Analyze captured variables
            val captures = analyzeCapturedVariables(lambdaFunction)

            // Check for 'this' references - these cannot be serialized
            val thisCaptures = captures.filter { it.isThis }
            if (thisCaptures.isNotEmpty()) {
                context.reportError(
                    expression,
                    "withContext(Dispatchers.IO) cannot reference 'this' (instance state). " +
                    "Instance state cannot cross the Task thread boundary. " +
                    "Extract needed fields to local variables before withContext block."
                )
                return expression
            }

            // Warn about potentially non-serializable captures
            for (capture in captures) {
                if (!isSerializableType(capture.type)) {
                    context.reportWarning(
                        expression,
                        "Captured variable '${capture.name}' of type ${capture.type.classFqName?.asString() ?: "unknown"} " +
                        "may not survive Task thread boundary."
                    )
                }
            }

            // Generate the worker function
            val className = currentClass?.name?.asString()
            val functionName = currentFunction?.name?.asString() ?: "anonymous"
            val workerName = context.nextIOWorkerName(className, functionName)

            val workerFunction = generateWorkerFunction(
                workerName,
                lambdaFunction,
                captures,
                expression.type
            )

            // Add to list for later insertion into file
            generatedWorkers.add(workerFunction)

            // Register for code generation
            context.ioWorkerRegistrations[workerName] = workerFunction

            // Generate the replacement call: runIOWorker<T>(workerName) { captures }
            return generateRunIOWorkerCall(expression, workerName, captures)
        }

        private fun isWithContextCall(function: IrFunction): Boolean {
            val fqName = function.fqNameWhenAvailable?.asString() ?: return false
            return fqName == "kotlin.coroutines.builders.withContext"
        }

        private fun isDispatchersIO(expression: IrExpression): Boolean {
            when (expression) {
                is IrGetField -> {
                    val field = expression.symbol.owner
                    if (field.name.asString() == "IO") {
                        val parent = field.parent
                        if (parent is IrClass && parent.name.asString() == "Dispatchers") {
                            return true
                        }
                    }
                }
                is IrCall -> {
                    val callee = expression.symbol.owner
                    if (callee.name.asString() == "<get-IO>") {
                        return true
                    }
                }
                is IrGetObjectValue -> {
                    val objectClass = expression.symbol.owner
                    if (objectClass.name.asString() == "IODispatcher") {
                        return true
                    }
                }
            }
            return false
        }

        /**
         * Extract the lambda function from the argument.
         * The lambda could be:
         * - IrFunctionExpression: Direct lambda literal
         * - IrBlock containing a class definition and constructor call (after callable reference lowering)
         */
        private fun extractLambdaFunction(arg: IrExpression): IrSimpleFunction? {
            return when (arg) {
                is IrFunctionExpression -> arg.function
                is IrBlock -> {
                    // After callable reference lowering, lambda is wrapped in a block
                    // containing a class and constructor call
                    // The class has an invoke method we need
                    for (stmt in arg.statements) {
                        if (stmt is IrClass) {
                            // Find the invoke method
                            val invokeMethod = stmt.functions.find {
                                it.name.asString() == "invoke" && !it.isFakeOverride
                            }
                            if (invokeMethod != null) {
                                return invokeMethod
                            }
                        }
                    }
                    null
                }
                else -> null
            }
        }

        /**
         * Analyze the lambda to find all captured variables from outer scopes.
         */
        private fun analyzeCapturedVariables(lambdaFunction: IrSimpleFunction): List<CapturedVariable> {
            val lambdaParams = lambdaFunction.valueParameters.map { it.symbol }.toSet() +
                    setOfNotNull(lambdaFunction.extensionReceiverParameter?.symbol)

            val declaredLocals = mutableSetOf<IrValueSymbol>()
            val captures = mutableMapOf<IrValueSymbol, CapturedVariable>()

            lambdaFunction.body?.acceptVoid(object : IrVisitorVoid() {
                override fun visitElement(element: IrElement) {
                    element.acceptChildrenVoid(this)
                }

                override fun visitVariable(declaration: IrVariable) {
                    declaredLocals.add(declaration.symbol)
                    declaration.acceptChildrenVoid(this)
                }

                override fun visitGetValue(expression: IrGetValue) {
                    val symbol = expression.symbol
                    if (symbol !in lambdaParams && symbol !in declaredLocals) {
                        val owner = symbol.owner
                        when (owner) {
                            is IrVariable -> {
                                val name = owner.name.asString()
                                captures[symbol] = CapturedVariable(
                                    symbol = symbol,
                                    name = sanitizeName(name),
                                    type = owner.type,
                                    isThis = false
                                )
                            }
                            is IrValueParameter -> {
                                val name = owner.name.asString()
                                val isThis = name == "<this>" || name.startsWith("\$this")
                                captures[symbol] = CapturedVariable(
                                    symbol = symbol,
                                    name = if (isThis) "this" else sanitizeName(name),
                                    type = owner.type,
                                    isThis = isThis
                                )
                            }
                        }
                    }
                    expression.acceptChildrenVoid(this)
                }
            })

            return captures.values.toList()
        }

        private fun sanitizeName(name: String): String {
            return name.replace("$", "_").replace("<", "").replace(">", "")
        }

        private fun isSerializableType(type: IrType): Boolean {
            val notNullType = if (type.isMarkedNullable()) type.makeNotNull() else type
            if (notNullType.isPrimitiveType()) return true
            if (notNullType.isString()) return true

            val classifier = notNullType.classifierOrNull?.owner
            if (classifier is IrClass) {
                val name = classifier.name.asString()
                if (name in serializableTypeNames) return true
                // Dynamic type is always safe
                if (classifier.fqNameWhenAvailable?.asString() == "kotlin.brs.Dynamic") return true
            }
            return false
        }

        /**
         * Generate the worker function.
         */
        private fun generateWorkerFunction(
            workerName: String,
            lambdaFunction: IrSimpleFunction,
            captures: List<CapturedVariable>,
            returnType: IrType
        ): IrSimpleFunction {
            val irFactory = context.irFactory
            val irBuiltIns = context.irBuiltIns

            // Create the worker function
            val workerFunction = irFactory.buildFun {
                name = Name.identifier(workerName)
                visibility = DescriptorVisibilities.PRIVATE
                modality = Modality.FINAL
                this.returnType = returnType.makeNotNull()
                origin = BrsDeclarationOrigin.IO_WORKER
            }

            // Add captures parameter: captures: Dynamic?
            val capturesParam = workerFunction.addValueParameter {
                name = Name.identifier("captures")
                type = irBuiltIns.anyNType
            }

            // Build the function body
            val bodyStatements = mutableListOf<IrStatement>()

            // Add: val cap = captures
            val capVariable = buildVariable(
                parent = workerFunction,
                startOffset = UNDEFINED_OFFSET,
                endOffset = UNDEFINED_OFFSET,
                origin = IrDeclarationOrigin.DEFINED,
                name = Name.identifier("cap"),
                type = irBuiltIns.anyNType
            ).apply {
                initializer = IrGetValueImpl(
                    UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                    capturesParam.type,
                    capturesParam.symbol
                )
            }
            bodyStatements.add(capVariable)

            // Extract each captured variable from the captures AA
            val captureVariables = mutableMapOf<IrValueSymbol, IrVariable>()
            for (capture in captures) {
                val extractedVar = buildVariable(
                    parent = workerFunction,
                    startOffset = UNDEFINED_OFFSET,
                    endOffset = UNDEFINED_OFFSET,
                    origin = IrDeclarationOrigin.DEFINED,
                    name = Name.identifier(capture.name),
                    type = capture.type
                ).apply {
                    // Initialize with cap.lookup("name") - represented as IrGetValue(cap)
                    // The actual cap.lookup() call will be emitted in code generation
                    // For now, just reference cap - IrToBrsTransformer will handle this specially
                    initializer = IrGetValueImpl(
                        UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                        irBuiltIns.anyNType,
                        capVariable.symbol
                    )
                }

                // Store the mapping for rewriting
                captureVariables[capture.symbol] = extractedVar
                bodyStatements.add(extractedVar)
            }

            // Copy and rewrite the lambda body, then wrap in runBlocking
            val lambdaBody = lambdaFunction.body
            if (lambdaBody != null) {
                // Deep copy the body and rewrite captured variable accesses
                val rewrittenBody = rewriteLambdaBody(lambdaBody, captureVariables, workerFunction)

                // Wrap the body in runBlocking to enable suspend function support.
                // This transforms: { statements... } into: runBlocking { statements... }
                val runBlockingResult = wrapBodyInRunBlocking(
                    rewrittenBody,
                    returnType,
                    workerFunction
                )

                // Add the return statement for the runBlocking result
                bodyStatements.add(
                    IrReturnImpl(
                        UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                        irBuiltIns.nothingType,
                        workerFunction.symbol,
                        runBlockingResult
                    )
                )
            }

            workerFunction.body = irFactory.createBlockBody(
                UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                bodyStatements
            )

            return workerFunction
        }

        /**
         * Wrap the given body in a runBlocking call to enable suspend function support.
         *
         * This transforms:
         * ```kotlin
         * {
         *     delay(100)
         *     "result"
         * }
         * ```
         *
         * Into:
         * ```kotlin
         * runBlocking(EmptyCoroutineContext) {
         *     delay(100)
         *     "result"
         * }
         * ```
         *
         * The runBlocking function:
         * - Provides a coroutine scope for suspend function calls
         * - Includes an event loop that processes delays and dispatched work
         * - Blocks until the coroutine completes and returns the result
         */
        private fun wrapBodyInRunBlocking(
            body: IrBody,
            returnType: IrType,
            parent: IrFunction
        ): IrExpression {
            val irFactory = context.irFactory
            val irBuiltIns = context.irBuiltIns

            // Get the runBlocking function symbol
            val runBlockingSymbol = context.brsSymbols.runBlockingFunction
            if (runBlockingSymbol == null) {
                // If runBlocking is not available, fall back to just returning the body's result
                // This shouldn't happen in normal usage but provides a graceful fallback
                context.reportWarning(
                    parent,
                    "runBlocking function not found. IO worker may not support suspend functions."
                )
                return when (body) {
                    is IrBlockBody -> {
                        val lastStmt = body.statements.lastOrNull()
                        if (lastStmt is IrReturn) lastStmt.value
                        else if (lastStmt is IrExpression) lastStmt
                        else IrConstImpl.constNull(UNDEFINED_OFFSET, UNDEFINED_OFFSET, irBuiltIns.anyNType)
                    }
                    is IrExpressionBody -> body.expression
                    else -> IrConstImpl.constNull(UNDEFINED_OFFSET, UNDEFINED_OFFSET, irBuiltIns.anyNType)
                }
            }

            // Create a suspend lambda function to pass to runBlocking
            // Signature: suspend CoroutineScope.() -> T
            val suspendLambda = irFactory.buildFun {
                name = Name.special("<anonymous>")
                visibility = DescriptorVisibilities.LOCAL
                modality = Modality.FINAL
                this.returnType = returnType.makeNotNull()
                isSuspend = true
                origin = IrDeclarationOrigin.LOCAL_FUNCTION_FOR_LAMBDA
            }

            // The lambda has no explicit parameters (CoroutineScope is extension receiver)
            // but we don't add it since we're not using scope functions

            // Set the lambda's body to the rewritten body
            suspendLambda.body = when (body) {
                is IrBlockBody -> {
                    // The body may have return statements targeting the original function.
                    // We need to keep them but the last expression should be the return value.
                    irFactory.createBlockBody(
                        UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                        body.statements.toMutableList()
                    )
                }
                is IrExpressionBody -> {
                    // Convert expression body to block body with implicit return
                    irFactory.createBlockBody(
                        UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                        mutableListOf(body.expression)
                    )
                }
                else -> irFactory.createBlockBody(UNDEFINED_OFFSET, UNDEFINED_OFFSET)
            }

            suspendLambda.parent = parent

            // Create the IrFunctionExpression wrapper for the lambda
            // The type should be suspend () -> T
            val lambdaType = irBuiltIns.suspendFunctionN(0).typeWith(returnType.makeNotNull())
            val lambdaExpression = IrFunctionExpressionImpl(
                UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                lambdaType,
                suspendLambda,
                IrStatementOrigin.LAMBDA
            )

            // Get EmptyCoroutineContext for the first argument
            val emptyContextSymbol = context.brsSymbols.emptyCoroutineContext
            val contextArg = if (emptyContextSymbol != null) {
                IrGetObjectValueImpl(
                    UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                    emptyContextSymbol.defaultType,
                    emptyContextSymbol
                )
            } else {
                // Fallback: pass null/invalid if EmptyCoroutineContext not found
                IrConstImpl.constNull(UNDEFINED_OFFSET, UNDEFINED_OFFSET, irBuiltIns.anyNType)
            }

            // Create the call: runBlocking<T>(EmptyCoroutineContext, suspendLambda)
            return IrCallImpl(
                startOffset = UNDEFINED_OFFSET,
                endOffset = UNDEFINED_OFFSET,
                type = returnType.makeNotNull(),
                symbol = runBlockingSymbol,
                typeArgumentsCount = 1
            ).apply {
                putTypeArgument(0, returnType.makeNotNull())
                putValueArgument(0, contextArg)
                putValueArgument(1, lambdaExpression)
            }
        }

        /**
         * Rewrite the lambda body, replacing captured variable accesses with local variable accesses.
         */
        private fun rewriteLambdaBody(
            body: IrBody,
            captureVariables: Map<IrValueSymbol, IrVariable>,
            newParent: IrFunction
        ): IrBody {
            // Deep copy the body
            val copiedBody = body.deepCopyWithSymbols(newParent)

            // Rewrite variable accesses
            copiedBody.transformChildrenVoid(object : IrElementTransformerVoid() {
                override fun visitGetValue(expression: IrGetValue): IrExpression {
                    val newVar = captureVariables[expression.symbol]
                    return if (newVar != null) {
                        IrGetValueImpl(
                            expression.startOffset,
                            expression.endOffset,
                            newVar.type,
                            newVar.symbol
                        )
                    } else {
                        super.visitGetValue(expression)
                    }
                }

                override fun visitSetValue(expression: IrSetValue): IrExpression {
                    val newVar = captureVariables[expression.symbol]
                    return if (newVar != null) {
                        // This shouldn't happen since captured vars in workers are val
                        context.reportWarning(
                            expression,
                            "Cannot write to captured variable '${newVar.name}' in IO worker"
                        )
                        super.visitSetValue(expression)
                    } else {
                        super.visitSetValue(expression)
                    }
                }
            })

            return copiedBody
        }

        /**
         * Generate the replacement call: runIOWorker<T>(workerName) { put("name", value); ... }
         */
        private fun generateRunIOWorkerCall(
            originalCall: IrCall,
            workerName: String,
            captures: List<CapturedVariable>
        ): IrExpression {
            val irBuiltIns = context.irBuiltIns

            // Emit a synthetic block that the transformer can recognize
            // We use a special origin to mark this as an IO worker call
            return IrBlockImpl(
                originalCall.startOffset,
                originalCall.endOffset,
                originalCall.type,
                BrsStatementOrigins.IO_WORKER_CALL
            ).apply {
                // Add the worker name as a string constant
                statements.add(
                    IrConstImpl.string(
                        UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                        irBuiltIns.stringType,
                        workerName
                    )
                )

                // Add capture assignments as IrSetField expressions with special handling
                // We'll use IrComposite to group them
                if (captures.isNotEmpty()) {
                    val captureBlock = IrCompositeImpl(
                        UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                        irBuiltIns.unitType,
                        BrsStatementOrigins.IO_WORKER_CAPTURES
                    )
                    for (capture in captures) {
                        // Add the value expression
                        captureBlock.statements.add(
                            IrGetValueImpl(
                                UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                                capture.type,
                                capture.symbol
                            )
                        )
                        // Add the name as a string
                        captureBlock.statements.add(
                            IrConstImpl.string(
                                UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                                irBuiltIns.stringType,
                                capture.name
                            )
                        )
                    }
                    statements.add(captureBlock)
                }
            }
        }
    }
}
