/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.transformers.irToBrs

import org.jetbrains.kotlin.backend.common.capturedFields
import org.jetbrains.kotlin.backend.common.lower.AbstractSuspendFunctionsLowering
import org.jetbrains.kotlin.backend.common.lower.BOUND_VALUE_PARAMETER
import org.jetbrains.kotlin.backend.common.lower.BOUND_RECEIVER_PARAMETER
import org.jetbrains.kotlin.backend.common.lower.LocalDeclarationsLowering
import org.jetbrains.kotlin.backend.common.lower.WebCallableReferenceLowering
import org.jetbrains.kotlin.brs.backend.ast.*
import org.jetbrains.kotlin.brs.backend.ast.parser.parseBrightScriptStatements
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.backend.brs.BrsIntrinsics
import org.jetbrains.kotlin.ir.backend.brs.lower.BrsCodeOutliningLowering
import org.jetbrains.kotlin.ir.backend.brs.lower.BrsDeclarationOrigin
import org.jetbrains.kotlin.ir.backend.brs.lower.BrsInlineCallTransformer
import org.jetbrains.kotlin.ir.backend.brs.lower.buildSharedDispatcher
import org.jetbrains.kotlin.ir.backend.brs.lower.isDataClassGeneratedMemberName
import org.jetbrains.kotlin.ir.backend.brs.lower.isSharedExtensionShapedFunction
import org.jetbrains.kotlin.ir.backend.brs.lower.sharedDispatcherCandidate
import org.jetbrains.kotlin.ir.backend.brs.lower.coroutines.BrsStatementOrigins
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.backend.brs.BrsIrBackendContext
import org.jetbrains.kotlin.ir.backend.brs.KOTLIN_RETIRE_FUNCTION_NAME
import org.jetbrains.kotlin.ir.backend.brs.KOTLIN_REVIVE_FUNCTION_NAME
import org.jetbrains.kotlin.ir.backend.brs.KOTLIN_SCOPE_BINDINGS_FIELD
import org.jetbrains.kotlin.ir.backend.brs.KOTLIN_START_DRIVER_NAME
import org.jetbrains.kotlin.ir.backend.brs.KOTLIN_TASK_ERROR_FIELD
import org.jetbrains.kotlin.ir.backend.brs.KOTLIN_TASK_MAIN_FUNCTION_NAME
import org.jetbrains.kotlin.ir.backend.brs.KOTLIN_TASK_STATE_FIELD
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.fileOrNull
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.getAnnotation
import org.jetbrains.kotlin.ir.util.getPackageFragment
import org.jetbrains.kotlin.ir.util.isFunction
import org.jetbrains.kotlin.ir.util.isInterface
import org.jetbrains.kotlin.ir.util.isNullable
import org.jetbrains.kotlin.ir.util.isTypeParameter
import org.jetbrains.kotlin.ir.util.isUnsigned
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.ir.util.resolveFakeOverride
import org.jetbrains.kotlin.name.BrsStandardClassIds
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrFieldSymbol
import org.jetbrains.kotlin.ir.symbols.IrTypeParameterSymbol
import org.jetbrains.kotlin.ir.symbols.IrValueSymbol
import org.jetbrains.kotlin.ir.visitors.IrVisitor
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import java.io.File

/**
 * Suffix of the forwarding wrapper slot generated next to every
 * extension-shaped SharedService member implementation (`<impl>__slot`).
 * Collision-free: user-derived globals always end in `_k_`.
 */
private const val SHARED_SLOT_WRAPPER_SUFFIX = "__slot"

/**
 * Transforms Kotlin IR to BrightScript AST.
 *
 * This is the core transformation that converts lowered Kotlin IR
 * into BrightScript-specific AST nodes that can then be rendered
 * to text.
 */
class IrToBrsTransformer(
    private val context: BrsIrBackendContext
) : IrVisitor<BrsNode?, Unit>() {

    internal val genCtx = BrsGenerationContext(context)

    private val statementTransformer = IrStatementToBrsTransformer(this, context, genCtx)
    private val expressionTransformer = IrExpressionToBrsTransformer(this, context, genCtx)

    // Expose statement visitor for when-lowered block handling
    val statementVisitor: IrStatementToBrsTransformer get() = statementTransformer
    internal val inlineCallTransformer = BrsInlineCallTransformer(context)

    // ==================== Entry Points ====================

    /**
     * Transform an IR file to a BrightScript program.
     */
    fun transformFile(irFile: IrFile): BrsProgram {
        val declarations = mutableListOf<BrsDeclaration>()
        val statements = mutableListOf<BrsStatement>()

        // Track current file for dependency collection
        genCtx.currentFilePath = irFile.path

        // Clear tracked enums from any previous transformation
        genCtx.enumClassNames.clear()

        // Resolve which lambda/coroutine capture fields hold a component's own `this`
        // (lambda classes live in the same file as the component that spawned them,
        // so a per-file scan sees every creation site)
        collectCapturedComponentSelfFields(irFile)

        // Decide whether components in this file get the lowered run{}-block
        // binding table injected into their generated init() (scope owners)
        genCtx.currentFileCallsExposeScope = fileCallsExposeScope(irFile)

        for (declaration in irFile.declarations) {
            when (declaration) {
                is IrFunction -> {
                    transformFunction(declaration)?.let { declarations.add(it) }
                }
                is IrClass -> {
                    // Classes are expanded into functions and statements
                    // (enums are tracked during transformClassDeclarations)
                    transformClassDeclarations(declaration, declarations, statements)
                }
                is IrProperty -> {
                    // Top-level properties become variables or functions
                    transformProperty(declaration, declarations, statements)
                }
                else -> {
                    // Other declarations (type aliases, etc.) are typically not emitted
                }
            }
        }

        // Note: BrightScript doesn't support top-level statements outside functions,
        // so enum initialization is handled lazily via initEntries() calls in values()/valueOf()

        // Generate IO worker registration function if this file has any workers
        val fileWorkers = context.ioWorkerRegistrations.filter { (_, func) ->
            func.parent === irFile
        }
        if (fileWorkers.isNotEmpty()) {
            val registrationFunction = generateIOWorkerRegistrationFunction(irFile, fileWorkers)
            declarations.add(registrationFunction)
        }

        return BrsProgram(declarations, statements)
    }

    /**
     * Populate [BrsGenerationContext.capturedComponentSelfFields] for [irFile].
     *
     * LocalDeclarationsLowering (and the coroutine lowering built on top of it) turns
     * lambdas into classes whose captured values arrive as constructor parameters and
     * are stored into capture fields. Whether such a field holds the component's own
     * `this` (the m-scope AA at runtime — component code only ever executes m-scoped)
     * or an unrelated component-typed value (a real node handle, e.g. a
     * createComponent<T>() result held in a local) is invisible at the access site,
     * so it is recovered from creation-site provenance: a capture field is marked
     * self iff some constructor call in this file feeds it from a dispatch-receiver
     * `<this>` of a SceneGraph component class — transitively, for capture fields
     * re-fed from an already-marked field (a coroutine's create() copy method passes
     * its own capture field to the fresh instance's constructor).
     *
     * RESIDUAL HOLE (documented, not statically solvable at this layer): a component
     * `this` the USER passes around as a T-typed value — stored in a property, passed
     * as an argument, returned from a function — is indistinguishable from a node
     * handle of the same static type, keeps direct node-field emission, and its
     * @SG*Field writes will silently vanish into the m-scope AA. Only
     * compiler-introduced lambda captures are provenance-tracked here; a FIR warning
     * on using component `this` as a value is a possible follow-up guard.
     */
    private fun collectCapturedComponentSelfFields(irFile: IrFile) {
        genCtx.capturedComponentSelfFields.clear()

        // Constructor parameter -> the capture field it initializes (LDL's `this.f = p` shape)
        val paramToCaptureField = mutableMapOf<IrValueSymbol, IrFieldSymbol>()
        // Every (parameter, argument) pair from constructor invocations in this file
        val constructorArguments = mutableListOf<Pair<IrValueSymbol, IrExpression>>()

        irFile.acceptVoid(object : IrVisitorVoid() {
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }

            override fun visitConstructor(declaration: IrConstructor) {
                val statements = (declaration.body as? IrBlockBody)?.statements.orEmpty()
                for (statement in statements) {
                    val setField = statement as? IrSetField ?: continue
                    val fieldOrigin = setField.symbol.owner.origin
                    if (fieldOrigin != LocalDeclarationsLowering.DECLARATION_ORIGIN_FIELD_FOR_CAPTURED_VALUE) continue
                    val value = setField.value as? IrGetValue ?: continue
                    val parameter = value.symbol.owner as? IrValueParameter ?: continue
                    if (parameter.parent == declaration) {
                        paramToCaptureField[parameter.symbol] = setField.symbol
                    }
                }
                declaration.acceptChildrenVoid(this)
            }

            override fun visitConstructorCall(expression: IrConstructorCall) {
                val parameters = expression.symbol.owner.parameters
                for ((parameter, argument) in parameters.zip(expression.arguments)) {
                    if (argument != null) {
                        constructorArguments.add(parameter.symbol to argument)
                    }
                }
                expression.acceptChildrenVoid(this)
            }
        })

        // Fixpoint: marking a field can make further constructor arguments self-feeding
        var changed = true
        while (changed) {
            changed = false
            for ((parameterSymbol, argument) in constructorArguments) {
                val field = paramToCaptureField[parameterSymbol] ?: continue
                if (field in genCtx.capturedComponentSelfFields) continue
                if (isComponentSelfValue(argument)) {
                    genCtx.capturedComponentSelfFields.add(field)
                    changed = true
                }
            }
        }
    }

    /**
     * True when [irFile] contains a call to `kotlin.brs.exposeScope` — gates
     * the `m.__kotlinScopeBindings` + `__kotlinScopeBindingsInstall` injection
     * in the generated init() of components declared in this file (the owner
     * half of the compiler-lowered `ScopeHandle.run { }` surface). File
     * granularity matches script-include granularity (dependencies are computed
     * per file). KNOWN HOLE: an exposeScope call hidden entirely inside another
     * file's helper escapes the scan, and such an owner serves hand-registered
     * requests only (run{} blocks dispatch to the guided-miss outcome, which
     * names the fix).
     */
    private fun fileCallsExposeScope(irFile: IrFile): Boolean {
        var found = false
        irFile.acceptVoid(object : IrVisitorVoid() {
            override fun visitElement(element: IrElement) {
                if (!found) element.acceptChildrenVoid(this)
            }

            override fun visitCall(expression: IrCall) {
                if (found) return
                if (expression.symbol.owner.fqNameWhenAvailable?.asString() == "kotlin.brs.exposeScope") {
                    found = true
                    return
                }
                expression.acceptChildrenVoid(this)
            }
        })
        return found
    }

    /**
     * True when [expression] is (through value-preserving casts) a component's own
     * `this` — either directly (a dispatch-receiver `<this>` typed as a SceneGraph
     * component class) or transitively (a read of a capture field already marked in
     * [BrsGenerationContext.capturedComponentSelfFields]).
     */
    private fun isComponentSelfValue(expression: IrExpression): Boolean {
        return when (val unwrapped = unwrapReceiverCasts(expression)) {
            is IrGetValue -> {
                val parameter = unwrapped.symbol.owner as? IrValueParameter ?: return false
                if (parameter.name.asString() != "<this>") return false
                if (parameter.kind != IrParameterKind.DispatchReceiver) return false
                val parameterClass = parameter.type.classOrNull?.owner ?: return false
                context.intrinsics.isSceneGraphComponent(parameterClass)
            }
            is IrGetField -> unwrapped.symbol in genCtx.capturedComponentSelfFields
            else -> false
        }
    }

    /**
     * Generate a function that registers all IO workers in a file.
     *
     * Generates:
     * ```brightscript
     * sub __registerIOWorkers_FileName()
     *     IOWorkerRegistry_register_k_("workerName1", __ioWorker_...)
     *     IOWorkerRegistry_register_k_("workerName2", __ioWorker_...)
     * end sub
     * ```
     *
     * Users should call this function from their Scene's init block, before using
     * withContext(Dispatchers.IO).
     */
    private fun generateIOWorkerRegistrationFunction(
        irFile: IrFile,
        workers: Map<String, IrSimpleFunction>
    ): BrsDeclaration {
        val fileName = irFile.name.removeSuffix(".kt")
        val functionName = "__registerIOWorkers_${fileName}_k_"

        val registrationStatements = mutableListOf<BrsStatement>()
        for ((workerName, workerFunction) in workers) {
            // Get the BrightScript name for the worker function
            val brsFunctionName = context.getBrsName(workerFunction)

            // Emit: IOWorkerRegistry_register_k_("workerName", workerFunctionRef)
            // In BrightScript, function references are just the function name as identifier
            registrationStatements.add(
                BrsExpressionStatement(
                    createFunctionCall(
                        "IOWorkerRegistry_register_Str_k_",
                        mutableListOf(
                            BrsStringLiteral(workerName),
                            BrsIdentifier(brsFunctionName)
                        ),
                        context
                    )
                )
            )
        }

        return BrsSub(
            name = functionName,
            parameters = mutableListOf(),
            body = BrsBlock(registrationStatements)
        )
    }

    /**
     * Transform an IR class to its BrightScript representation.
     */
    fun transformClass(irClass: IrClass): List<BrsDeclaration> {
        val declarations = mutableListOf<BrsDeclaration>()
        val statements = mutableListOf<BrsStatement>()
        transformClassDeclarations(irClass, declarations, statements)
        return declarations
    }

    // ==================== Declarations ====================

    /**
     * Transform an IR function to a BrightScript function or sub.
     */
    fun transformFunction(irFunction: IrFunction): BrsDeclaration? {
        if (irFunction is IrSimpleFunction && irFunction.isFakeOverride) return null
        if (irFunction.isExternal) return null

        // Clear any leftover hoisted statements from previous function transformations
        genCtx.clearHoistedScopes()

        // Reset variable naming state for this function
        // This ensures each function gets fresh unique names and doesn't collide with other functions
        genCtx.resetVariableNaming()

        val name = context.getBrsName(irFunction)

        // Build parameter list, starting with extension receiver if present
        val allParameters = mutableListOf<BrsParameter>()

        // Add extension receiver as first parameter if this is an extension function
        // This is needed because in BrightScript there's no receiver concept - extension
        // functions are compiled as regular functions with the receiver as first argument.
        // The name "m" matches what visitGetValue outputs for <this> references.
        if (irFunction is IrSimpleFunction) {
            irFunction.extensionReceiverParameter?.let { receiver ->
                allParameters.add(BrsParameter(
                    name = "m",
                    type = mapTypeToBrs(receiver.type),
                    defaultValue = null
                ))
            }
            // SharedService members are EXTENSION-SHAPED (design §4.1): the
            // implementation takes its receiver as an explicit first parameter,
            // reusing the extension convention above — same "m" name (so `<this>`
            // body references, rendered as "m", read the parameter), same first
            // position (suspend members therefore order `(m, params..., _completion)`,
            // matching the device-proven suspend-extension shape). The constructor
            // attaches a forwarding wrapper slot in its place — see
            // buildSharedSlotWrapper.
            if (isSharedExtensionShapedFunction(irFunction, context)) {
                allParameters.add(BrsParameter(
                    name = "m",
                    type = mapTypeToBrs(irFunction.dispatchReceiverParameter!!.type),
                    defaultValue = null
                ))
            }
        }

        // Add value parameters
        val rawParameters = irFunction.valueParameters.map { param ->
            BrsParameter(
                name = sanitizeParameterName(param.name.asString()),
                type = mapTypeToBrs(param.type),
                defaultValue = param.defaultValue?.expression?.let { transformExpression(it) }
            )
        }
        allParameters.addAll(rawParameters)

        val parameters = normalizeParametersForBrs(deduplicateParameterNames(allParameters))

        // Detect shared variables (mutable vars captured by closures) before transforming body
        // Save and restore to handle nested function transformations
        val previousSharedVariables = genCtx.sharedVariables
        // First check if BrsSharedVariableDetectionLowering already detected shared variables for this function
        // (this runs before local class extraction, so it can detect variables captured by local classes)
        // If not found, fall back to detecting them now (for lambdas and inline functions)
        genCtx.sharedVariables = context.sharedVariablesByFunction[irFunction.symbol]
            ?: irFunction.body?.let { genCtx.detectSharedVariables(it) }
            ?: emptySet()

        // Check if this function has @BrsInline - use parsed code instead of IR body
        val inlineInfo = context.inlineFunctionInfo[irFunction.symbol] as? BrsCodeOutliningLowering.BrsInlineInfo
        var body = if (inlineInfo != null) {
            // Use the parsed @BrsInline code
            BrsBlock(inlineInfo.parsedStatements.toMutableList())
        } else {
            irFunction.body?.let { transformBody(it) } ?: BrsBlock()
        }

        // Restore previous shared variables
        genCtx.sharedVariables = previousSharedVariables

        // Check for any remaining hoisted statements and prepend them to the body
        val remainingHoisted = genCtx.takeHoistedStatements()
        if (remainingHoisted.isNotEmpty()) {
            val combinedStatements = remainingHoisted.toMutableList()
            combinedStatements.addAll(body.statements)
            body = BrsBlock(combinedStatements)
        }

        // Generate runtime checks for parameters with default values
        val defaultValueChecks = defaultValueGuards(irFunction)
        if (defaultValueChecks.isNotEmpty()) {
            val combinedStatements = defaultValueChecks.toMutableList()
            combinedStatements.addAll(body.statements)
            body = BrsBlock(combinedStatements)
        }

        val returnType = mapTypeToBrs(irFunction.returnType)

        // Check if this is a suspend lambda's invoke method that needs to return doResume result.
        // These methods have their bodies rewritten to return the result of create().doResume(),
        // so they must be functions even though the Kotlin return type is Unit.
        //
        // Detection: The invoke method is in a LAMBDA_IMPL class and is a suspend function.
        // After the coroutine lowering, the invoke method's body is replaced to call create().doResume(),
        // which returns a value, so it needs to be a BrightScript function, not a sub.
        val isSuspendLambdaInvoke = irFunction is IrSimpleFunction &&
            irFunction.isSuspend &&
            irFunction.name.asString() == "invoke" &&
            irFunction.parentClassOrNull?.origin == WebCallableReferenceLowering.LAMBDA_IMPL

        // Functions with Unit return type become subs, EXCEPT for suspend lambda invoke methods
        // Note: Functions returning Nothing (e.g., lambdas with non-local returns, throw expressions)
        // should still be functions, not subs, because they may implement interfaces expecting return values
        return if (irFunction.returnType.isUnit() && !isSuspendLambdaInvoke) {
            BrsSub(name, parameters.toMutableList(), body)
        } else {
            // For Nothing/Unit suspend invoke, use Dynamic as the BrightScript return type
            val effectiveReturnType = when {
                irFunction.returnType.isNothing() -> BrsType.DYNAMIC
                irFunction.returnType.isUnit() -> BrsType.DYNAMIC  // suspend lambda invoke
                else -> returnType
            }
            BrsFunction(name, parameters.toMutableList(), effectiveReturnType, body)
        }
    }

    /**
     * Runtime guards that materialize a parameter's default value when the
     * caller passed `invalid` — the call-site marker for a skipped defaulted
     * parameter (see absentArgumentPlaceholder). BrightScript's own default
     * parameter syntax only applies when arguments are OMITTED, so these
     * guards are what make the invalid marker convention work. They must be
     * the first statements of the body, before anything reads the parameters
     * (constructor super-call arguments included).
     */
    private fun defaultValueGuards(irFunction: IrFunction): List<BrsStatement> =
        irFunction.valueParameters.mapNotNull { param ->
            val defaultExpr = param.defaultValue?.expression ?: return@mapNotNull null
            val paramName = sanitizeParameterName(param.name.asString())
            BrsIf(
                condition = BrsBinaryOp(
                    BrsIdentifier(paramName),
                    BrsBinaryOperator.EQ,
                    BrsIdentifier("invalid")
                ),
                thenBranch = BrsExpressionStatement(
                    BrsBinaryOp(
                        BrsIdentifier(paramName),
                        BrsBinaryOperator.EQ,
                        transformExpression(defaultExpr)
                    )
                ),
                elseBranch = null
            )
        }

    /**
     * Transform class declarations into top-level functions and statements.
     */
    private fun transformClassDeclarations(
        irClass: IrClass,
        declarations: MutableList<BrsDeclaration>,
        statements: MutableList<BrsStatement>
    ) {
        // Skip top-level Layout classes (e.g., MainScreen_Layout)
        // These can be:
        // 1. FIR-generated (from SceneGraphLayoutGenerator - legacy)
        // 2. Gradle-generated stubs (from GenerateLayoutStubsTask)
        //
        // The actual BrightScript implementation is generated via generateLayoutAccessorClass()
        // when processing the owner component class (which has the @SGLayout annotation).
        if (isLayoutStubClass(irClass)) {
            return
        }

        // Handle object singletons specially
        if (irClass.kind == ClassKind.OBJECT) {
            // Skip @BrsConstant objects - their properties are inlined at usage sites
            if (context.isConstantObject(irClass)) {
                return
            }
            transformObjectDeclaration(irClass, declarations, statements)
            return
        }

        // Handle enum classes specially
        if (irClass.kind == ClassKind.ENUM_CLASS) {
            genCtx.enumClassNames.add(context.getBrsName(irClass))
            transformEnumDeclaration(irClass, declarations, statements)
            return
        }

        // Handle data classes - generate synthetic members
        if (irClass.isData) {
            transformDataClassDeclaration(irClass, declarations, statements)
            return
        }

        // Handle SceneGraph component classes specially
        if (context.intrinsics.isSceneGraphComponent(irClass)) {
            transformSceneGraphComponent(irClass, declarations)
            return
        }

        // Generate constructor function
        for (constructor in irClass.declarations.filterIsInstance<IrConstructor>()) {
            transformConstructor(irClass, constructor)?.let { declarations.add(it) }
        }

        // Generate member functions (+ forwarding wrapper slots for SharedService members)
        for (function in irClass.declarations.filterIsInstance<IrSimpleFunction>()) {
            if (!function.isFakeOverride) {
                transformFunctionWithSharedWrapper(function, declarations)
            }
        }

        // Generate property accessors
        for (property in irClass.declarations.filterIsInstance<IrProperty>()) {
            transformProperty(property, declarations, statements)
        }

        // Process nested classes
        for (nested in irClass.declarations.filterIsInstance<IrClass>()) {
            transformClassDeclarations(nested, declarations, statements)
        }
    }

    /**
     * Transform a SceneGraph component class to BrightScript.
     *
     * Component classes are compiled differently from regular classes:
     * - No constructor function is generated (component lifecycle is managed by SceneGraph)
     * - The init {} block compiles to BrightScript's sub init()
     * - Property accessors for inherited properties (top, global, m) are NOT generated
     * - Member functions become top-level functions in the component script
     *
     * Example input:
     * ```kotlin
     * class MyComponent : SceneComponent() {
     *     init {
     *         top.setFocus(true)
     *     }
     *     fun handleButton() { println("pressed") }
     * }
     * ```
     *
     * Example output:
     * ```brightscript
     * sub init()
     *     m.top.setFocus(true)
     * end sub
     *
     * sub handleButton_k_()
     *     println_AnyN_k_("pressed")
     * end sub
     * ```
     */
    private fun transformSceneGraphComponent(
        irClass: IrClass,
        declarations: MutableList<BrsDeclaration>
    ) {
        // The abstract stdlib base declarations (GroupComponent, TaskComponent, ...)
        // emit no BrightScript of their own: their contract reaches user components
        // through the extractor's inheritance pass, and their source file is a shared
        // pkg:/source script where an emitted `sub init()` would collide with every
        // component's own init().
        if (context.intrinsics.isComponentBaseDeclaration(irClass)) {
            return
        }

        // Set component context flags
        val previousInComponent = genCtx.isInComponentContext
        val previousComponentClass = genCtx.currentComponentClass
        genCtx.isInComponentContext = true
        genCtx.currentComponentClass = irClass

        try {
            // Extract layout info from companion @SGLayout function (new pattern)
            // Falls back to legacy @SGLayout property pattern for backwards compatibility
            val layoutInfo = extractLayoutInfoFromCompanion(irClass)
                ?: irClass.declarations.filterIsInstance<IrProperty>()
                    .find { context.intrinsics.hasSGLayoutAnnotation(it) }
                    ?.let { extractLayoutInfo(irClass, it) }

            // Generate layout accessor class if we have @SGLayout
            if (layoutInfo != null) {
                declarations.addAll(generateLayoutAccessorClass(layoutInfo))
            }

            // Find the primary constructor to extract init block content
            val primaryConstructor = irClass.declarations.filterIsInstance<IrConstructor>().firstOrNull()

            // Generate sub init() from the constructor body (which contains init block code)
            if (primaryConstructor != null) {
                val initSub = transformComponentInitBlock(irClass, primaryConstructor, layoutInfo)
                if (initSub != null) {
                    declarations.add(initSub)
                }
            }

            // Generate member functions (but not inherited property accessors like get_top)
            // Note: onKeyEvent overrides ARE included here - they generate the mangled implementation
            // function (e.g., ShelfView_onKeyEvent_Str_Z_k_). The unmangled wrapper is added below.
            for (function in irClass.declarations.filterIsInstance<IrSimpleFunction>()) {
                if (!function.isFakeOverride && !isComponentScopeAccessor(function)) {
                    transformFunction(function)?.let { declarations.add(it) }
                }
            }

            // Generate onKeyEvent function for components that need it
            // This must be named exactly "onKeyEvent" (no mangling) to work with SceneGraph
            val onKeyEventFunction = generateOnKeyEventFunction(irClass)
            if (onKeyEventFunction != null) {
                declarations.add(onKeyEventFunction)
            }

            // Generate the task-thread entry wrapper for concrete task components
            val taskMainFunction = generateTaskMainFunction(irClass)
            if (taskMainFunction != null) {
                declarations.add(taskMainFunction)
            }

            // Generate the bare-named lifecycle entries (retire/revive callFunc targets)
            // for concrete render components
            declarations.addAll(generateLifecycleEntryFunctions(irClass))

            // Generate property accessor functions (for delegated properties, custom getters/setters, etc.)
            for (property in irClass.declarations.filterIsInstance<IrProperty>()) {
                if (context.intrinsics.isComponentScopeProperty(property.name.asString())) continue
                if (layoutInfo != null && isLayoutClassProperty(property, irClass)) continue
                transformProperty(property, declarations, mutableListOf())
            }

            // Process nested classes (companion objects, etc.)
            // Note: Layout stubs are handled at the top level by isLayoutStubClass()
            for (nested in irClass.declarations.filterIsInstance<IrClass>()) {
                val nestedDeclarations = mutableListOf<BrsDeclaration>()
                val nestedStatements = mutableListOf<BrsStatement>()
                transformClassDeclarations(nested, nestedDeclarations, nestedStatements)
                declarations.addAll(nestedDeclarations)
            }
        } finally {
            // Restore context flags
            genCtx.isInComponentContext = previousInComponent
            genCtx.currentComponentClass = previousComponentClass
        }
    }

    /**
     * Check if a function is a component scope property accessor (top, global, m)
     * that should not be emitted as we handle these specially.
     */
    private fun isComponentScopeAccessor(function: IrSimpleFunction): Boolean {
        val property = function.correspondingPropertySymbol?.owner ?: return false
        val propName = property.name.asString()
        return context.intrinsics.isComponentScopeProperty(propName)
    }

    /**
     * Generate the onKeyEvent function for a SceneGraph component.
     *
     * For components that extend GroupComponent, LayoutComponent, or SceneComponent,
     * we generate an onKeyEvent function that:
     * - Returns false by default (allowing events to propagate to focused children)
     * - Calls the Kotlin override if present
     *
     * This is required for proper key event handling in SceneGraph. Without an
     * onKeyEvent function, Group components may block key events from reaching
     * focused children.
     *
     * Generated BrightScript:
     * ```brightscript
     * function onKeyEvent(key as String, press as Boolean) as Boolean
     *     return false  ' or: return m.onKeyEvent_String_Boolean_k_(key, press)
     * end function
     * ```
     *
     * @return The generated function, or null if this component doesn't need onKeyEvent
     */
    private fun generateOnKeyEventFunction(irClass: IrClass): BrsFunction? {
        // Only generate for components that can receive key events
        if (!context.intrinsics.componentNeedsOnKeyEvent(irClass)) {
            return null
        }

        // Don't generate for abstract base classes (GroupComponent, SceneComponent, etc.)
        // Only concrete user classes should have onKeyEvent generated
        if (irClass.modality == org.jetbrains.kotlin.descriptors.Modality.ABSTRACT) {
            return null
        }

        val parameters = mutableListOf(
            BrsParameter("key", BrsType.STRING),
            BrsParameter("press", BrsType.BOOLEAN)
        )

        // Walk the USER hierarchy: an override declared in a concrete base is
        // attached by the BASE's init over the shared m (base init runs first);
        // the slot short name is derived against the DECLARING class, not this
        // leaf. Only when nothing in the chain overrides does the wrapper
        // return false.
        val override = context.intrinsics.hierarchyOverrides(irClass, "onKeyEvent") { fn ->
            fn.valueParameters.size == 2 &&
                fn.valueParameters[0].type.isString() &&
                fn.valueParameters[1].type.isBoolean() &&
                fn.returnType.isBoolean()
        }

        val body = if (override != null) {
            val declaringClass = override.parent as IrClass
            val shortName = context.getBrsName(override)
                .removePrefix("${context.getBrsName(declaringClass)}_")

            BrsBlock(mutableListOf(
                BrsReturn(
                    BrsFunctionCall(
                        BrsDotAccess(BrsMRef(), shortName),
                        mutableListOf(BrsIdentifier("key"), BrsIdentifier("press"))
                    )
                )
            ))
        } else {
            BrsBlock(mutableListOf(BrsReturn(BrsBooleanLiteral(false))))
        }

        return BrsFunction(
            name = "onKeyEvent",
            parameters = parameters,
            returnType = BrsType.BOOLEAN,
            body = body
        )
    }

    /**
     * Check if a class is a concrete (instantiable) TaskComponent subclass.
     * Only these get the __kotlinTaskMain wrapper and functionName wiring;
     * CoroutineTask is excluded by isTaskComponent (it extends ComponentBase directly).
     */
    private fun isConcreteTaskComponent(irClass: IrClass): Boolean {
        return context.intrinsics.isTaskComponent(irClass) &&
                irClass.modality != org.jetbrains.kotlin.descriptors.Modality.ABSTRACT
    }

    /**
     * Find the run() implementation THIS class declares (real, non-abstract
     * declaration in the class itself — fake overrides don't count).
     *
     * The wrapper is emitted next to the declaration it calls, so the call never
     * crosses component script files: a class inheriting run() also inherits the
     * declaring class's __kotlinTaskMain through SceneGraph XML inheritance
     * (derived components inherit base component <script> functions), and a class
     * redeclaring run() emits its own wrapper, which overrides the base's by name.
     * Emitting a cross-script call instead (leaf wrapper -> base run_k_) would
     * force the base component's .brs onto the leaf's <script> list, where its
     * `sub init()` would collide with the leaf's own init().
     */
    private fun findLocalTaskRunImplementation(irClass: IrClass): IrSimpleFunction? {
        return irClass.declarations.filterIsInstance<IrSimpleFunction>()
            .find {
                it.name.asString() == "run" && it.valueParameters.isEmpty() &&
                    !it.isFakeOverride &&
                    it.modality != org.jetbrains.kotlin.descriptors.Modality.ABSTRACT
            }
    }

    /**
     * Generate the task-thread entry point for a TaskComponent subclass that
     * declares a run() implementation (abstract intermediates included — their
     * concrete leaves inherit this wrapper via SceneGraph XML inheritance).
     *
     * The shape is device-proven by spikes/task-node-spike/components/SpikeTask.brs:
     * the run() override is wrapped in try/catch, the error AA is written BEFORE the
     * state, and kotlinTaskState is written LAST so observers always see a fully
     * populated node. The catch guards AA member access with nested ifs because
     * BrightScript's and/or operators do not short-circuit.
     *
     * ```brightscript
     * sub __kotlinTaskMain()
     *     try
     *         EchoTask_run_k_()
     *         m.top.kotlinTaskState = "done"
     *     catch e
     *         errInfo = {message: "", number: 0}
     *         if Type(e) = "roAssociativeArray" then
     *             if e.message <> invalid then
     *                 errInfo.message = e.message
     *             end if
     *             ...
     *         end if
     *         m.top.kotlinTaskError = errInfo
     *         m.top.kotlinTaskState = "error"
     *     end try
     * end sub
     * ```
     */
    private fun generateTaskMainFunction(irClass: IrClass): BrsSub? {
        if (!context.intrinsics.isTaskComponent(irClass)) return null
        val runImplementation = findLocalTaskRunImplementation(irClass) ?: return null
        val runName = context.getBrsName(runImplementation)
        // Same file as the run() declaration by construction (self-dependency is
        // filtered), but recorded per house style so any future emission split
        // still lands the defining file on the component's script list.
        context.recordFunctionDependency(runName)

        fun assign(target: BrsExpression, value: BrsExpression): BrsStatement =
            BrsExpressionStatement(BrsBinaryOp(target, BrsBinaryOperator.EQ, value))

        fun mTopField(name: String): BrsExpression =
            BrsDotAccess(BrsDotAccess(BrsMRef(), "top"), name)

        val errorVar = "e"
        val errorInfoVar = "errInfo"

        fun copyErrorMember(member: String): BrsStatement = BrsIf(
            condition = BrsBinaryOp(
                BrsDotAccess(BrsIdentifier(errorVar), member),
                BrsBinaryOperator.NE,
                BrsInvalidLiteral()
            ),
            thenBranch = BrsExpressionStatement(
                BrsBinaryOp(
                    BrsDotAccess(BrsIdentifier(errorInfoVar), member),
                    BrsBinaryOperator.EQ,
                    BrsDotAccess(BrsIdentifier(errorVar), member)
                )
            ),
            elseBranch = null
        )

        val tryBlock = BrsBlock(mutableListOf(
            BrsExpressionStatement(BrsFunctionCall(BrsIdentifier(runName), mutableListOf())),
            assign(mTopField(KOTLIN_TASK_STATE_FIELD), BrsStringLiteral("done"))
        ))

        val catchBlock = BrsBlock(mutableListOf(
            assign(
                BrsIdentifier(errorInfoVar),
                BrsAALiteral(mutableListOf(
                    BrsAAEntry("message", BrsStringLiteral("")),
                    BrsAAEntry("number", BrsIntLiteral(0))
                ))
            ),
            BrsIf(
                condition = BrsBinaryOp(
                    BrsTypeOf(BrsIdentifier(errorVar)),
                    BrsBinaryOperator.EQ,
                    BrsStringLiteral("roAssociativeArray")
                ),
                thenBranch = BrsBlock(mutableListOf(
                    copyErrorMember("message"),
                    copyErrorMember("number"),
                    copyErrorMember("backtrace")
                )),
                elseBranch = null
            ),
            assign(mTopField(KOTLIN_TASK_ERROR_FIELD), BrsIdentifier(errorInfoVar)),
            assign(mTopField(KOTLIN_TASK_STATE_FIELD), BrsStringLiteral("error"))
        ))

        return BrsSub(
            name = KOTLIN_TASK_MAIN_FUNCTION_NAME,
            parameters = mutableListOf(),
            body = BrsBlock(mutableListOf(BrsTry(tryBlock, errorVar, catchBlock)))
        )
    }

    /**
     * Lifecycle entries for concrete RENDER components (spec §5.6): bare-named,
     * so the parent's retire(node)/revive(node) reach them through callFunc
     * (documented: runs in the owning component's thread with ITS m). Each
     * concrete class emits its own pair — the derived's same-named function
     * wins in the SceneGraph namespace (documented), and the leaf knows the
     * full hierarchy at generation time.
     *
     * Retire is idempotent at the ENTRY (spec §4 step 1: `if (retired) return`
     * BEFORE onStop): the guard runs first, so a second retire never re-runs
     * onStop — `__kotlinRetireImpl`'s own guard sits after the hook call and
     * would be too late to prevent that (coordinator ruling R1).
     *
     * ```brightscript
     * sub __kotlinRetire()
     *     if __kotlinIsRetired() then return
     *     try
     *         m.onStop_k_()                     ' only when the hierarchy overrides onStop
     *     catch e
     *         print "[kotlin.lifecycle] onStop threw: " + e.message
     *     end try
     *     __kotlinRetireImpl()
     * end sub
     * sub __kotlinRevive()
     *     __kotlinReviveImpl()
     *     <Class>___kotlinStartDriver_k_()       ' only when a driver was synthesized
     * end sub
     * ```
     */
    private fun generateLifecycleEntryFunctions(irClass: IrClass): List<BrsSub> {
        // Concrete render components only — the same predicate the XML side uses
        if (!context.intrinsics.emitsLifecycleEntries(irClass)) return emptyList()

        val retireBody = mutableListOf<BrsStatement>()
        // Guard FIRST (R1): a retired node's second retire is a complete no-op.
        retireBody.add(
            BrsIf(
                condition = createFunctionCall("__kotlinIsRetired", mutableListOf(), context),
                thenBranch = BrsReturn(null),
                elseBranch = null
            )
        )
        // Same hierarchy walk as the onKeyEvent wrapper: an onStop declared in a
        // concrete user BASE is attached over the shared m by the base's init, and
        // the slot short name derives from the DECLARING class, not this leaf.
        val onStop = context.intrinsics.hierarchyOverrides(irClass, "onStop") { it.valueParameters.isEmpty() && !it.isSuspend }
        if (onStop != null) {
            val declaringClass = onStop.parent as IrClass
            val shortName = context.getBrsName(onStop).removePrefix("${context.getBrsName(declaringClass)}_")
            retireBody.add(
                BrsTry(
                    tryBlock = BrsBlock(mutableListOf(
                        BrsExpressionStatement(BrsFunctionCall(BrsDotAccess(BrsMRef(), shortName), mutableListOf()))
                    )),
                    catchVariable = "e",
                    catchBlock = BrsBlock(mutableListOf(
                        BrsPrint(mutableListOf(
                            BrsBinaryOp(
                                BrsStringLiteral("[kotlin.lifecycle] onStop threw: "),
                                BrsBinaryOperator.CONCAT,
                                BrsDotAccess(BrsIdentifier("e"), "message")
                            )
                        ))
                    ))
                )
            )
        }
        retireBody.add(BrsExpressionStatement(createFunctionCall("__kotlinRetireImpl", mutableListOf(), context)))

        val reviveBody = mutableListOf<BrsStatement>()
        reviveBody.add(BrsExpressionStatement(createFunctionCall("__kotlinReviveImpl", mutableListOf(), context)))
        // The synthesized driver is a member of THIS class (BrsComponentLifecycleLowering
        // adds one to every class whose hierarchy overrides onStart), so its global is
        // in this component's own script — no cross-file dependency to record.
        val driver = irClass.declarations.filterIsInstance<IrSimpleFunction>()
            .find { it.name.asString() == KOTLIN_START_DRIVER_NAME }
        if (driver != null) {
            reviveBody.add(BrsExpressionStatement(BrsFunctionCall(BrsIdentifier(context.getBrsName(driver)), mutableListOf())))
        }

        return listOf(
            BrsSub(name = KOTLIN_RETIRE_FUNCTION_NAME, parameters = mutableListOf(), body = BrsBlock(retireBody)),
            BrsSub(name = KOTLIN_REVIVE_FUNCTION_NAME, parameters = mutableListOf(), body = BrsBlock(reviveBody)),
        )
    }

    /**
     * Transform the component's init {} block to BrightScript's sub init().
     *
     * Extracts statements from the constructor body, filtering out:
     * - Delegating constructor calls (super())
     * - Instance initializer calls
     *
     * @param layoutInfo Optional layout info for @SGLayout property initialization
     */
    private fun transformComponentInitBlock(
        irClass: IrClass,
        constructor: IrConstructor,
        layoutInfo: LayoutAccessorInfo? = null
    ): BrsSub? {
        val bodyStatements = mutableListOf<BrsStatement>()

        // Task components: wire the task-thread entry point first, matching the
        // device-proven spike init shape. This also guarantees init() is emitted
        // even when the class has no properties or init block of its own.
        if (isConcreteTaskComponent(irClass)) {
            bodyStatements.add(
                BrsExpressionStatement(
                    BrsBinaryOp(
                        BrsDotAccess(BrsDotAccess(BrsMRef(), "top"), "functionName"),
                        BrsBinaryOperator.EQ,
                        BrsStringLiteral(KOTLIN_TASK_MAIN_FUNCTION_NAME)
                    )
                )
            )
        }

        // Lifecycle attach (spec §5.1): UNCONDITIONAL for every render component
        // that emits an init() — abstract user intermediates included (they sit
        // in the SceneGraph extends chain; the attach is idempotent). Performs
        // the pump attach internally, so the per-file coroutine scan no longer
        // gates anything here (its helper-file hole is closed by construction).
        // Task components are excluded — the WHOLE hierarchy, abstract
        // intermediates included (ruling 2026-09-05): their init() only ever
        // runs inside a Task node's extends chain, the task thread has no pump
        // (attaching there would copy a hostTop-armed PumpScheduler into every
        // derived task's task-thread m), and the lifecycle include closure is
        // unwanted bloat in task XML. The old concrete-only exclusion was an
        // accident of the retired per-file scan.
        if (!context.intrinsics.isTaskComponent(irClass)) {
            bodyStatements.add(
                BrsExpressionStatement(
                    createFunctionCall(
                        "__kotlinComponentAttach",
                        mutableListOf(
                            BrsDotAccess(BrsMRef(), "top"),
                            BrsDotAccess(BrsMRef(), "global")
                        ),
                        context
                    )
                )
            )
        }

        // Scope owners (files that call exposeScope): install the lowered
        // run{}-block binding table — request name → lifted function pointer —
        // so owner dispatch can serve `owner.run { }` requests. The AA literal
        // is EMPTY here: entries need the component's include closure, which
        // is only computable after the whole file transforms; BrsCompiler
        // populates the registered literal before rendering. The m-scope
        // assignment is the inspectable artifact; the install call hands the
        // same table to the stdlib's per-component holder (GetGlobalAA domain
        // — the __kotlinComponentAttach idiom), which is what dispatch reads.
        if (genCtx.currentFileCallsExposeScope && !isConcreteTaskComponent(irClass)) {
            val bindingTable = BrsAALiteral()
            context.pendingScopeBindingTables.add(bindingTable)
            bodyStatements.add(
                BrsExpressionStatement(
                    BrsBinaryOp(
                        BrsDotAccess(BrsMRef(), KOTLIN_SCOPE_BINDINGS_FIELD),
                        BrsBinaryOperator.EQ,
                        bindingTable
                    )
                )
            )
            bodyStatements.add(
                BrsExpressionStatement(
                    createFunctionCall(
                        "__kotlinScopeBindingsInstall",
                        mutableListOf(BrsDotAccess(BrsMRef(), KOTLIN_SCOPE_BINDINGS_FIELD)),
                        context
                    )
                )
            )
        }

        // Initialize property backing fields on m (like regular classes do with 'this')
        // This must happen BEFORE init block statements execute
        for (property in irClass.declarations.filterIsInstance<IrProperty>()) {
            // Skip inherited component scope properties (top, global, m)
            if (context.intrinsics.isComponentScopeProperty(property.name.asString())) {
                continue
            }

            // Skip @SGLayout properties - we'll initialize them separately
            if (context.intrinsics.hasSGLayoutAnnotation(property)) {
                continue
            }

            // Skip properties whose type is the generated Layout class
            // These are initialized via layoutInfo using ComponentName_Layout_create
            if (layoutInfo != null && isLayoutClassProperty(property, irClass)) {
                continue
            }

            val backingField = property.backingField ?: continue
            // Sanitize field name for special names like <this>
            val rawFieldName = backingField.name.asString()
            val fieldName = when {
                rawFieldName == "<this>" -> "__this"
                rawFieldName.startsWith("<") && rawFieldName.endsWith(">") ->
                    rawFieldName.removePrefix("<").removeSuffix(">").replace("-", "_").replace(" ", "_")
                else -> rawFieldName.replace("$", "_")
            }
            backingField.initializer?.expression?.let { initializer ->
                val transformedInit = transformExpression(initializer)
                val hoisted = genCtx.takeHoistedStatements()
                bodyStatements.addAll(hoisted)
                // Interface fields (with SGField annotations) go on m.top, internal state goes on m
                val target = if (hasInterfaceFieldAnnotation(property)) {
                    BrsDotAccess(BrsDotAccess(BrsMRef(), "top"), fieldName)  // m.top.fieldName
                } else {
                    BrsDotAccess(BrsMRef(), fieldName)  // m.fieldName
                }
                bodyStatements.add(
                    BrsExpressionStatement(
                        BrsBinaryOp(
                            target,
                            BrsBinaryOperator.EQ,
                            transformedInit
                        )
                    )
                )
            }
        }

        // Initialize @SGLayout property with the accessor class instance
        if (layoutInfo != null) {
            // m._layout = ComponentName_Layout_create(m.top)
            bodyStatements.add(
                BrsExpressionStatement(
                    BrsBinaryOp(
                        BrsDotAccess(BrsMRef(), "_${layoutInfo.propertyName}"),
                        BrsBinaryOperator.EQ,
                        createFunctionCall(
                            "${layoutInfo.className}_create",
                            mutableListOf(BrsDotAccess(BrsMRef(), "top")),
                            context
                        )
                    )
                )
            )
        }

        // Attach member functions to m (like regular classes attach to 'this')
        // This must happen BEFORE init block statements so methods are available
        val className = context.getBrsName(irClass)
        for (function in irClass.declarations.filterIsInstance<IrSimpleFunction>()) {
            if (function.isFakeOverride) continue
            if (isComponentScopeAccessor(function)) continue  // Skip top/global/m accessors

            val methodName = context.getBrsName(function)
            val shortName = methodName.removePrefix("${className}_")

            bodyStatements.add(
                BrsExpressionStatement(
                    BrsBinaryOp(
                        BrsDotAccess(BrsMRef(), shortName),
                        BrsBinaryOperator.EQ,
                        BrsIdentifier(methodName)
                    )
                )
            )
        }

        // Attach property accessor methods to m
        for (property in irClass.declarations.filterIsInstance<IrProperty>()) {
            if (context.intrinsics.isComponentScopeProperty(property.name.asString())) continue
            if (layoutInfo != null && isLayoutClassProperty(property, irClass)) continue

            property.getter?.let { getter ->
                if (!getter.isFakeOverride) {
                    val getterName = context.getBrsName(getter)
                    val shortName = getterName.removePrefix("${className}_")
                    bodyStatements.add(
                        BrsExpressionStatement(
                            BrsBinaryOp(
                                BrsDotAccess(BrsMRef(), shortName),
                                BrsBinaryOperator.EQ,
                                BrsIdentifier(getterName)
                            )
                        )
                    )
                }
            }

            property.setter?.let { setter ->
                if (!setter.isFakeOverride) {
                    val setterName = context.getBrsName(setter)
                    val shortName = setterName.removePrefix("${className}_")
                    bodyStatements.add(
                        BrsExpressionStatement(
                            BrsBinaryOp(
                                BrsDotAccess(BrsMRef(), shortName),
                                BrsBinaryOperator.EQ,
                                BrsIdentifier(setterName)
                            )
                        )
                    )
                }
            }
        }

        // Extract init block statements from IrAnonymousInitializer declarations on the class
        // (These haven't been lowered/inlined into the constructor for BRS backend)
        for (declaration in irClass.declarations) {
            if (declaration is IrAnonymousInitializer && !declaration.isStatic) {
                val initBody = declaration.body
                for (stmt in initBody.statements) {
                    transformStatement(stmt)?.let { bodyStatements.add(it) }
                }
            }
        }

        // Also check constructor body for any statements after delegating call
        val body = constructor.body
        if (body is IrBlockBody) {
            for (stmt in body.statements) {
                when (stmt) {
                    is IrDelegatingConstructorCall -> continue
                    is IrInstanceInitializerCall -> continue
                    else -> {
                        transformStatement(stmt)?.let { bodyStatements.add(it) }
                    }
                }
            }
        }

        // Only generate init() if there's actual code
        if (bodyStatements.isEmpty()) return null

        return BrsSub(
            name = "init",
            parameters = mutableListOf(),
            body = BrsBlock(bodyStatements)
        )
    }

    // ==================== Layout Accessor Generation ====================

    /**
     * Information about a layout accessor class to generate.
     */
    data class LayoutAccessorInfo(
        /** The name of the property (e.g., "layout") */
        val propertyName: String,
        /** The name of the generated accessor class (e.g., "MainScreen_Layout") */
        val className: String,
        /** List of all node IDs that need accessor properties */
        val nodeIds: List<String>
    )

    /**
     * Extract layout accessor info from an @SGLayout property.
     *
     * This extracts the node IDs from the sceneLayout { } DSL call to generate
     * the appropriate accessor class.
     */
    private fun extractLayoutInfo(irClass: IrClass, property: IrProperty): LayoutAccessorInfo? {
        val componentName = context.getBrsName(irClass)
        val propertyName = property.name.asString()
        val className = "${componentName}_Layout"

        // Get the backing field initializer
        val backingField = property.backingField ?: return null
        val initializer = backingField.initializer?.expression ?: return null

        // The initializer should be a call to sceneLayout { }
        val sceneLayoutCall = initializer as? IrCall ?: return null
        if (sceneLayoutCall.symbol.owner.name.asString() != "sceneLayout") return null

        // Extract node IDs from the DSL lambda
        val nodeIds = extractNodeIdsFromLayoutDsl(sceneLayoutCall)
        if (nodeIds.isEmpty()) return null

        return LayoutAccessorInfo(propertyName, className, nodeIds)
    }

    /**
     * Extract layout accessor info from a companion object's @SGLayout function.
     *
     * This pattern uses @SGLayout as a marker annotation:
     * ```kotlin
     * companion object {
     *     @SGLayout
     *     fun defineLayout() = sceneLayout {
     *         button(id = "myButton")
     *         label(id = "myLabel")
     *     }
     * }
     * ```
     *
     * The node IDs are extracted from the DSL body by finding calls like `button(id = "xyz")`.
     */
    private fun extractLayoutInfoFromCompanion(irClass: IrClass): LayoutAccessorInfo? {
        // Find companion object
        val companion = irClass.declarations
            .filterIsInstance<IrClass>()
            .find { it.isCompanion }
            ?: return null

        // Find function with @SGLayout annotation
        val layoutFunction = companion.declarations
            .filterIsInstance<IrSimpleFunction>()
            .find { context.intrinsics.hasSGLayoutAnnotation(it) }
            ?: return null

        // Extract node IDs from function body by traversing the DSL
        val nodeIds = extractNodeIdsFromFunctionBody(layoutFunction)
        if (nodeIds.isEmpty()) return null

        val componentName = context.getBrsName(irClass)
        val className = "${componentName}_Layout"

        return LayoutAccessorInfo("layout", className, nodeIds)
    }

    /**
     * Extract node IDs from a @SGLayout function body.
     * Finds the sceneLayout { } call and extracts IDs from DSL builder calls inside.
     */
    private fun extractNodeIdsFromFunctionBody(function: IrSimpleFunction): List<String> {
        val body = function.body ?: return emptyList()

        // Find the sceneLayout { } call
        val sceneLayoutCall = findSceneLayoutCall(body) ?: return emptyList()

        // Extract node IDs from the DSL
        return extractNodeIdsFromLayoutDsl(sceneLayoutCall)
    }

    /**
     * Find the sceneLayout { } call within a function body.
     */
    private fun findSceneLayoutCall(body: IrBody): IrCall? {
        return when (body) {
            is IrBlockBody -> {
                for (statement in body.statements) {
                    // Check direct return statement
                    if (statement is IrReturn) {
                        val call = statement.value as? IrCall
                        if (call?.symbol?.owner?.name?.asString() == "sceneLayout") {
                            return call
                        }
                    }
                    // Check direct call statement
                    if (statement is IrCall && statement.symbol.owner.name.asString() == "sceneLayout") {
                        return statement
                    }
                }
                null
            }
            is IrExpressionBody -> {
                val call = body.expression as? IrCall
                if (call?.symbol?.owner?.name?.asString() == "sceneLayout") call else null
            }
            else -> null
        }
    }

    /**
     * Check if a property has the generated Layout class type.
     *
     * This is used to identify properties like `val layout = MainScreen_Layout(top)` that should
     * be initialized via the generated Layout_create function instead of direct constructor call.
     *
     * Supports two patterns:
     * 1. Top-level class: `MainScreen_Layout` (new pattern)
     * 2. Nested class: `MainScreen.Layout` (legacy pattern, for backwards compatibility)
     */
    /**
     * Check if a class is a FIR-generated Layout class.
     *
     * Note: FIR generation via SceneGraphLayoutGenerator has been removed.
     * This function is kept for backwards compatibility with any existing
     * compiled artifacts that may still have FIR-generated Layout classes.
     */
    private fun isFirGeneratedLayoutClass(irClass: IrClass): Boolean {
        val origin = irClass.origin
        return origin is IrDeclarationOrigin.GeneratedByPlugin &&
               origin.pluginId.contains("SGLayoutKey")
    }

    /**
     * Check if a class is a FIR-generated top-level Layout class.
     */
    private fun isFirGeneratedTopLevelLayoutClass(irClass: IrClass): Boolean {
        return isFirGeneratedLayoutClass(irClass) &&
               irClass.name.asString().endsWith("_Layout") &&
               irClass.parent !is IrClass
    }

    /**
     * Check if a class is a Layout stub class that should be skipped.
     *
     * Only FIR-generated Layout classes are skipped (legacy path - now removed).
     * Gradle-generated Layout stubs are compiled normally since user code
     * may reference them directly (e.g., `val layout = MainScreen_Layout(top)`).
     */
    private fun isLayoutStubClass(irClass: IrClass): Boolean {
        // Must be a top-level class (not nested)
        if (irClass.parent is IrClass) return false

        // Must have name ending with _Layout
        val className = irClass.name.asString()
        if (!className.endsWith("_Layout")) return false

        // Only skip FIR-generated classes (legacy path - no longer generated)
        // Gradle-generated stubs should be compiled normally
        return isFirGeneratedLayoutClass(irClass)
    }

    /**
     * Extract all node IDs from a sceneLayout { } DSL call.
     */
    private fun extractNodeIdsFromLayoutDsl(call: IrCall): List<String> {
        val nodeIds = mutableListOf<String>()

        // The lambda is the last argument
        val lambdaArg = call.getValueArgument(call.valueArgumentsCount - 1)
        val lambdaBody = extractLambdaBody(lambdaArg) ?: return emptyList()

        // Recursively extract node IDs from the lambda body
        extractNodeIdsFromStatements(lambdaBody.statements, nodeIds)

        return nodeIds
    }

    /**
     * Extract the body from a lambda expression.
     */
    private fun extractLambdaBody(arg: IrExpression?): IrBlockBody? {
        return when (arg) {
            is IrFunctionExpression -> arg.function.body as? IrBlockBody
            is IrBlock -> {
                val functionRef = arg.statements.filterIsInstance<IrFunctionReference>().firstOrNull()
                val function = functionRef?.symbol?.owner ?: return null
                function.body as? IrBlockBody
            }
            else -> null
        }
    }

    /**
     * Recursively extract node IDs from builder method calls.
     */
    private fun extractNodeIdsFromStatements(statements: List<IrStatement>, nodeIds: MutableList<String>) {
        for (statement in statements) {
            val call = statement as? IrCall ?: continue
            val functionName = call.symbol.owner.name.asString()

            // Check if this is a DSL builder method
            val isBuilderMethod = functionName in setOf(
                "group", "layoutGroup", "label", "poster", "rectangle",
                "button", "buttonGroup", "textEditBox", "keyboard"
            )
            if (!isBuilderMethod) continue

            // Extract the id argument (first argument)
            val idArg = call.getValueArgument(0) as? IrConst ?: continue
            val id = idArg.value as? String ?: continue
            nodeIds.add(id)

            // Check for children lambda (last argument for container nodes)
            val function = call.symbol.owner
            val initParamIndex = function.valueParameters.indexOfFirst { it.name.asString() == "init" }
            if (initParamIndex >= 0) {
                val childLambda = call.getValueArgument(initParamIndex)
                val childBody = extractLambdaBody(childLambda)
                if (childBody != null) {
                    extractNodeIdsFromStatements(childBody.statements, nodeIds)
                }
            }
        }
    }

    /**
     * Generate the layout accessor class declarations.
     *
     * For a component with @SGLayout property, generates:
     * - A _create function that initializes the accessor instance
     * - A getter function for each declared node ID
     *
     * Example output for MainScreen with nodes "mainLayout" and "counterLabel":
     * ```brightscript
     * function MainScreen_Layout_create(top)
     *     instance = {}
     *     instance._top = top
     *     instance._mainLayout = invalid
     *     instance._counterLabel = invalid
     *     instance.get_mainLayout = MainScreen_Layout_get_mainLayout
     *     instance.get_counterLabel = MainScreen_Layout_get_counterLabel
     *     return instance
     * end function
     *
     * function MainScreen_Layout_get_mainLayout()
     *     if m._mainLayout = invalid then
     *         m._mainLayout = m._top.findNode("mainLayout")
     *     end if
     *     return m._mainLayout
     * end function
     * ```
     */
    private fun generateLayoutAccessorClass(info: LayoutAccessorInfo): List<BrsDeclaration> {
        val declarations = mutableListOf<BrsDeclaration>()

        // Generate _create function
        declarations.add(generateLayoutCreateFunction(info))

        // Generate getter for each node ID
        for (nodeId in info.nodeIds) {
            declarations.add(generateLayoutNodeGetter(info.className, nodeId))
        }

        return declarations
    }

    /**
     * Generate the _create function for the layout accessor class.
     */
    private fun generateLayoutCreateFunction(info: LayoutAccessorInfo): BrsFunction {
        val statements = mutableListOf<BrsStatement>()

        // instance = {}
        statements.add(
            BrsVariable(
                name = "instance",
                initializer = BrsAALiteral(mutableListOf())
            )
        )

        // instance._top = top
        statements.add(
            BrsExpressionStatement(
                BrsBinaryOp(
                    BrsDotAccess(BrsIdentifier("instance"), "_top"),
                    BrsBinaryOperator.EQ,
                    BrsIdentifier("top")
                )
            )
        )

        // For each node ID: instance._nodeId = invalid
        for (nodeId in info.nodeIds) {
            statements.add(
                BrsExpressionStatement(
                    BrsBinaryOp(
                        BrsDotAccess(BrsIdentifier("instance"), "_$nodeId"),
                        BrsBinaryOperator.EQ,
                        BrsInvalidLiteral()
                    )
                )
            )
        }

        // Attach getter functions: instance.get_nodeId = ClassName_get_nodeId
        for (nodeId in info.nodeIds) {
            statements.add(
                BrsExpressionStatement(
                    BrsBinaryOp(
                        BrsDotAccess(BrsIdentifier("instance"), "get_$nodeId"),
                        BrsBinaryOperator.EQ,
                        BrsIdentifier("${info.className}_get_$nodeId")
                    )
                )
            )
        }

        // return instance
        statements.add(BrsReturn(BrsIdentifier("instance")))

        return BrsFunction(
            name = "${info.className}_create",
            parameters = mutableListOf(BrsParameter("top", BrsType.OBJECT)),
            returnType = BrsType.OBJECT,
            body = BrsBlock(statements)
        )
    }

    /**
     * Generate a getter function for a specific node ID.
     *
     * Generates code that:
     * 1. Lazily looks up the node on first access
     * 2. Caches the result for subsequent accesses
     * 3. Throws a descriptive error if the node is not found
     */
    private fun generateLayoutNodeGetter(className: String, nodeId: String): BrsFunction {
        val statements = mutableListOf<BrsStatement>()

        // if m._nodeId = invalid then
        //     m._nodeId = m._top.findNode("nodeId")
        //     if m._nodeId = invalid then
        //         throw "Layout node 'nodeId' not found in component"
        //     end if
        // end if
        statements.add(
            BrsIf(
                condition = BrsBinaryOp(
                    BrsDotAccess(BrsMRef(), "_$nodeId"),
                    BrsBinaryOperator.EQ,
                    BrsInvalidLiteral()
                ),
                thenBranch = BrsBlock(mutableListOf(
                    // m._nodeId = m._top.findNode("nodeId")
                    BrsExpressionStatement(
                        BrsBinaryOp(
                            BrsDotAccess(BrsMRef(), "_$nodeId"),
                            BrsBinaryOperator.EQ,
                            BrsMethodCall(
                                BrsDotAccess(BrsMRef(), "_top"),
                                "findNode",
                                mutableListOf(BrsStringLiteral(nodeId))
                            )
                        )
                    ),
                    // if m._nodeId = invalid then throw "..." end if
                    BrsIf(
                        condition = BrsBinaryOp(
                            BrsDotAccess(BrsMRef(), "_$nodeId"),
                            BrsBinaryOperator.EQ,
                            BrsInvalidLiteral()
                        ),
                        thenBranch = BrsBlock(mutableListOf(
                            BrsThrow(BrsStringLiteral("Layout node '$nodeId' not found in component"))
                        )),
                        elseBranch = null
                    )
                )),
                elseBranch = null
            )
        )

        // return m._nodeId
        statements.add(BrsReturn(BrsDotAccess(BrsMRef(), "_$nodeId")))

        return BrsFunction(
            name = "${className}_get_$nodeId",
            parameters = mutableListOf(),  // No parameters - uses m scope
            returnType = BrsType.OBJECT,
            body = BrsBlock(statements)
        )
    }

    /**
     * Transform an object declaration (singleton) to BrightScript.
     *
     * Generates:
     * 1. An instance variable: MySingleton_instance = invalid
     * 2. A getInstance function that lazily creates the singleton
     * 3. Member functions with proper naming
     */
    private fun transformObjectDeclaration(
        irClass: IrClass,
        declarations: MutableList<BrsDeclaration>,
        statements: MutableList<BrsStatement>
    ) {
        val className = context.getBrsName(irClass)
        val instanceVarName = "${className}_instance"

        // Note: BrightScript doesn't support top-level statements, so we use GetGlobalAA() for global storage
        // Instance is lazily initialized in getInstance()

        // Generate the _create function (like regular class constructor)
        // Find the primary constructor first (needed for getInstance function)
        val primaryConstructor = irClass.declarations.filterIsInstance<IrConstructor>().firstOrNull()
        for (constructor in irClass.declarations.filterIsInstance<IrConstructor>()) {
            transformConstructor(irClass, constructor)?.let { declarations.add(it) }
        }

        // Generate getInstance function
        val getInstanceBody = mutableListOf<BrsStatement>()

        // Get the mangled constructor name (must match the definition)
        val constructorName = if (primaryConstructor != null) {
            context.getBrsName(primaryConstructor)
        } else {
            "${className}_create"  // Fallback for edge cases
        }

        // Helper to generate GetGlobalAA().instanceVarName
        fun globalInstanceAccess() = BrsDotAccess(
            BrsFunctionCall(BrsIdentifier("GetGlobalAA"), mutableListOf()),
            instanceVarName
        )

        // if GetGlobalAA().MySingleton_instance = invalid then
        //     GetGlobalAA().MySingleton_instance = MySingleton_create()
        // end if
        getInstanceBody.add(
            BrsIf(
                condition = BrsBinaryOp(
                    globalInstanceAccess(),
                    BrsBinaryOperator.EQ,
                    BrsInvalidLiteral()
                ),
                thenBranch = BrsBlock(mutableListOf(
                    BrsExpressionStatement(
                        BrsBinaryOp(
                            globalInstanceAccess(),
                            BrsBinaryOperator.EQ,
                            BrsFunctionCall(BrsIdentifier(constructorName), mutableListOf())
                        )
                    )
                ))
            )
        )

        // return GetGlobalAA().MySingleton_instance
        getInstanceBody.add(BrsReturn(globalInstanceAccess()))

        declarations.add(
            BrsFunction(
                name = "${className}_getInstance",
                parameters = mutableListOf(),
                returnType = BrsType.OBJECT,
                body = BrsBlock(getInstanceBody)
            )
        )

        // Generate member functions (+ forwarding wrapper slots for SharedService members)
        for (function in irClass.declarations.filterIsInstance<IrSimpleFunction>()) {
            if (!function.isFakeOverride) {
                transformFunctionWithSharedWrapper(function, declarations)
            }
        }

        // Generate property accessors
        for (property in irClass.declarations.filterIsInstance<IrProperty>()) {
            transformProperty(property, declarations, statements)
        }

        // Process nested classes
        for (nested in irClass.declarations.filterIsInstance<IrClass>()) {
            transformClassDeclarations(nested, declarations, statements)
        }
    }

    /**
     * Transform an enum class declaration to BrightScript.
     *
     * Generates:
     * 1. Entry variables: Color_RED = invalid, Color_GREEN = invalid
     * 2. Entries initialized flag: Color_entriesInitialized = false
     * 3. Entry initializer function: Color_initEntries()
     * 4. Constructor function: Color_create(name, ordinal, ...)
     * 5. values() function returning array of all entries
     * 6. valueOf() function to get entry by name
     */
    private fun transformEnumDeclaration(
        irClass: IrClass,
        declarations: MutableList<BrsDeclaration>,
        statements: MutableList<BrsStatement>
    ) {
        val className = context.getBrsName(irClass)

        // Get all enum entries
        val enumEntries = irClass.declarations.filterIsInstance<IrEnumEntry>()

        // Note: BrightScript doesn't support top-level statements, so we use m.global
        // for enum entry storage. Variables are initialized lazily in initEntries().
        val initializedFlagName = "${className}_entriesInitialized"

        // 3. Generate the _create constructor function
        // Constructor takes name, ordinal, plus any custom parameters
        for (constructor in irClass.declarations.filterIsInstance<IrConstructor>()) {
            transformEnumConstructor(irClass, constructor)?.let { declarations.add(it) }
        }

        // 4. Generate initEntries sub (using m. prefix for global scope)
        val initEntriesBody = mutableListOf<BrsStatement>()

        // if m.Color_entriesInitialized = true then return
        // Note: Use explicit = true comparison because the flag may be invalid (uninitialized)
        // on first access, and BrightScript doesn't allow invalid in bare if-clause conditions.
        initEntriesBody.add(
            BrsIf(
                condition = BrsBinaryOp(
                    BrsDotAccess(BrsIdentifier("m"), initializedFlagName),
                    BrsBinaryOperator.EQ,
                    BrsBooleanLiteral(true)
                ),
                thenBranch = BrsReturn(null)
            )
        )

        // m.Color_entriesInitialized = true
        initEntriesBody.add(
            BrsExpressionStatement(
                BrsBinaryOp(
                    BrsDotAccess(BrsIdentifier("m"), initializedFlagName),
                    BrsBinaryOperator.EQ,
                    BrsBooleanLiteral(true)
                )
            )
        )

        // Initialize each entry: m.Color_RED = Color_create("RED", 0, ...)
        enumEntries.forEachIndexed { ordinal, entry ->
            // Store enum metadata for constant inlining optimization
            context.mapping.enumEntryOrdinals[entry] = ordinal
            context.mapping.enumEntryNames[entry] = entry.name.asString()

            // Get the constructor from the initializer expression
            var entryConstructor: IrConstructor? = null

            // Extract constant property values for inlining
            entry.initializerExpression?.let { init ->
                val initExpr = init.expression
                if (initExpr is IrEnumConstructorCall) {
                    val constantProps = mutableMapOf<String, Any?>()
                    val constructor = initExpr.symbol.owner
                    entryConstructor = constructor
                    for (i in 0 until initExpr.valueArgumentsCount) {
                        initExpr.getValueArgument(i)?.let { arg ->
                            if (arg is IrConst) {
                                val paramName = constructor.valueParameters.getOrNull(i)?.name?.asString()
                                if (paramName != null) {
                                    constantProps[paramName] = arg.value
                                }
                            }
                        }
                    }
                    if (constantProps.isNotEmpty()) {
                        context.mapping.enumEntryConstantProperties[entry] = constantProps
                    }
                }
            }

            val entryVarName = "${className}_${entry.name.asString()}"
            val args = mutableListOf<BrsExpression>(
                BrsStringLiteral(entry.name.asString()),  // name
                BrsIntLiteral(ordinal)                      // ordinal
            )

            // Add initializer arguments if present
            entry.initializerExpression?.let { init ->
                // Extract arguments from the constructor call
                val initExpr = init.expression
                if (initExpr is IrEnumConstructorCall) {
                    for (i in 0 until initExpr.valueArgumentsCount) {
                        val arg = initExpr.getValueArgument(i)
                        if (arg != null) {
                            args.add(transformExpression(arg))
                        } else {
                            // Absent argument: default (filled callee-side) or empty vararg
                            args.add(absentArgumentPlaceholder(initExpr.symbol.owner, i))
                        }
                    }
                }
            }

            // Get the mangled constructor name (must match the definition)
            val constructorName = if (entryConstructor != null) {
                context.getBrsName(entryConstructor!!)
            } else {
                "${className}_create"  // Fallback for edge cases
            }

            initEntriesBody.add(
                BrsExpressionStatement(
                    BrsBinaryOp(
                        BrsDotAccess(BrsIdentifier("m"), entryVarName),
                        BrsBinaryOperator.EQ,
                        BrsFunctionCall(BrsIdentifier(constructorName), args)
                    )
                )
            )
        }

        declarations.add(
            BrsSub(
                name = "${className}_initEntries",
                parameters = mutableListOf(),
                body = BrsBlock(initEntriesBody)
            )
        )

        // 5. Generate values() function
        val valuesBody = mutableListOf<BrsStatement>()

        // Color_initEntries()
        valuesBody.add(
            BrsExpressionStatement(
                BrsFunctionCall(BrsIdentifier("${className}_initEntries"), mutableListOf())
            )
        )

        // return [m.Color_RED, m.Color_GREEN, ...]
        val entryRefs = enumEntries.map { entry ->
            BrsDotAccess(BrsIdentifier("m"), "${className}_${entry.name.asString()}")
        }
        valuesBody.add(BrsReturn(BrsArrayLiteral(entryRefs.toMutableList())))

        declarations.add(
            BrsFunction(
                name = "${className}_values",
                parameters = mutableListOf(),
                returnType = BrsType.OBJECT,
                body = BrsBlock(valuesBody)
            )
        )

        // 6. Generate valueOf(name) function
        val valueOfBody = mutableListOf<BrsStatement>()

        // Color_initEntries()
        valueOfBody.add(
            BrsExpressionStatement(
                BrsFunctionCall(BrsIdentifier("${className}_initEntries"), mutableListOf())
            )
        )

        // Generate if-else chain for each entry
        var currentIf: BrsIf? = null
        var firstIf: BrsIf? = null
        for (entry in enumEntries) {
            val entryName = entry.name.asString()
            val entryVarName = "${className}_$entryName"

            val newIf = BrsIf(
                condition = BrsBinaryOp(
                    BrsIdentifier("name"),
                    BrsBinaryOperator.EQ,
                    BrsStringLiteral(entryName)
                ),
                thenBranch = BrsReturn(BrsDotAccess(BrsIdentifier("m"), entryVarName))
            )

            if (firstIf == null) {
                firstIf = newIf
            }
            if (currentIf != null) {
                currentIf.elseBranch = newIf
            }
            currentIf = newIf
        }

        // Add else branch that returns invalid
        currentIf?.elseBranch = BrsReturn(BrsInvalidLiteral())

        firstIf?.let { valueOfBody.add(it) }

        declarations.add(
            BrsFunction(
                name = "${className}_valueOf",
                parameters = mutableListOf(BrsParameter("name", BrsType.STRING, null)),
                returnType = BrsType.OBJECT,
                body = BrsBlock(valueOfBody)
            )
        )

        // Generate member functions (if any), skipping synthetic values/valueOf
        for (function in irClass.declarations.filterIsInstance<IrSimpleFunction>()) {
            val functionName = function.name.asString()
            if (!function.isFakeOverride && functionName != "values" && functionName != "valueOf") {
                transformFunction(function)?.let { declarations.add(it) }
            }
        }

        // Generate property accessors. The constructor attaches
        // this.__get_x = ClassName___get_x_k_ for every property
        // (addMethodAttachments) and reads through a variable receiver compile
        // to receiver.__get_x(), so the accessor functions must exist.
        // Skip the synthetic 'entries' property, mirroring the attachment side.
        for (property in irClass.declarations.filterIsInstance<IrProperty>()) {
            if (property.name.asString() == "entries") continue
            transformProperty(property, declarations, statements)
        }
    }

    /**
     * Transform an enum constructor to a BrightScript function.
     * Adds name and ordinal parameters before any custom parameters.
     */
    private fun transformEnumConstructor(irClass: IrClass, constructor: IrConstructor): BrsFunction? {
        val className = context.getBrsName(irClass)
        // Use mangled constructor name to support overloading (must match call sites)
        val name = context.getBrsName(constructor)

        // Start with name and ordinal parameters
        val parameters = mutableListOf(
            BrsParameter("__name", BrsType.STRING, null),
            BrsParameter("__ordinal", BrsType.INTEGER, null)
        )

        // Add constructor parameters
        constructor.valueParameters.forEach { param ->
            parameters.add(
                BrsParameter(
                    name = sanitizeParameterName(param.name.asString()),
                    type = mapTypeToBrs(param.type),
                    defaultValue = param.defaultValue?.expression?.let { transformExpression(it) }
                )
            )
        }

        // Build constructor body
        val bodyStatements = mutableListOf<BrsStatement>()

        // Materialize defaults over passed-invalid before anything reads the parameters
        bodyStatements.addAll(defaultValueGuards(constructor))

        // this = {}
        bodyStatements.add(
            BrsVariable(
                name = "this",
                type = BrsType.OBJECT,
                initializer = BrsAALiteral(mutableListOf())
            )
        )

        // this.__type = "ClassName"
        bodyStatements.add(
            BrsExpressionStatement(
                BrsBinaryOp(
                    BrsDotAccess(BrsIdentifier("this"), "__type"),
                    BrsBinaryOperator.EQ,
                    BrsStringLiteral(className)
                )
            )
        )

        // this.name = __name
        bodyStatements.add(
            BrsExpressionStatement(
                BrsBinaryOp(
                    BrsDotAccess(BrsIdentifier("this"), "name"),
                    BrsBinaryOperator.EQ,
                    BrsIdentifier("__name")
                )
            )
        )

        // this.ordinal = __ordinal
        bodyStatements.add(
            BrsExpressionStatement(
                BrsBinaryOp(
                    BrsDotAccess(BrsIdentifier("this"), "ordinal"),
                    BrsBinaryOperator.EQ,
                    BrsIdentifier("__ordinal")
                )
            )
        )

        // this.__proto = ["ClassName", ...interfaces]
        val allInterfaceNames = collectAllInterfaceNames(irClass)
        val protoElements = mutableListOf<BrsExpression>(BrsStringLiteral(className))
        allInterfaceNames.forEach { protoElements.add(BrsStringLiteral(it)) }
        bodyStatements.add(
            BrsExpressionStatement(
                BrsBinaryOp(
                    BrsDotAccess(BrsIdentifier("this"), "__proto"),
                    BrsBinaryOperator.EQ,
                    BrsArrayLiteral(protoElements)
                )
            )
        )

        // this.__id = __kotlin_nextObjectId()
        // Unique object ID for identity checks (===)
        context.recordFunctionDependency("__kotlin_nextObjectId")
        bodyStatements.add(
            BrsExpressionStatement(
                BrsBinaryOp(
                    BrsDotAccess(BrsIdentifier("this"), "__id"),
                    BrsBinaryOperator.EQ,
                    BrsFunctionCall(BrsIdentifier("__kotlin_nextObjectId"), mutableListOf())
                )
            )
        )

        // Add custom properties from constructor parameters
        constructor.valueParameters.forEach { param ->
            bodyStatements.add(
                BrsExpressionStatement(
                    BrsBinaryOp(
                        BrsDotAccess(BrsIdentifier("this"), param.name.asString()),
                        BrsBinaryOperator.EQ,
                        BrsIdentifier(sanitizeParameterName(param.name.asString()))
                    )
                )
            )
        }

        // Transform constructor body statements if present
        (constructor.body as? IrBlockBody)?.statements?.forEach { stmt ->
            when (stmt) {
                is IrDelegatingConstructorCall -> {
                    // Skip delegating calls for enum constructors
                }
                is IrInstanceInitializerCall -> {
                    // Skip instance initializer calls
                }
                else -> {
                    transformStatement(stmt)?.let { bodyStatements.add(it) }
                }
            }
        }

        // Add methods and property accessors to the instance
        addMethodAttachments(irClass, className, bodyStatements)

        // return this
        bodyStatements.add(BrsReturn(BrsIdentifier("this")))

        return BrsFunction(
            name = name,
            parameters = normalizeParametersForBrs(parameters).toMutableList(),
            returnType = BrsType.OBJECT,
            body = BrsBlock(bodyStatements)
        )
    }

    /**
     * Transform a data class declaration to BrightScript.
     *
     * Generates:
     * 1. Constructor function: Person_create(name, age)
     * 2. equals method: Person_equals(other)
     * 3. hashCode method: Person_hashCode()
     * 4. toString method: Person_toString()
     * 5. copy method: Person_copy(name, age)
     * 6. componentN methods: Person_component1(), Person_component2(), etc.
     */
    private fun transformDataClassDeclaration(
        irClass: IrClass,
        declarations: MutableList<BrsDeclaration>,
        statements: MutableList<BrsStatement>
    ) {
        val className = context.getBrsName(irClass)

        // Get primary constructor parameters (these are the data class properties)
        val primaryConstructor = irClass.declarations.filterIsInstance<IrConstructor>().firstOrNull()
        val properties = primaryConstructor?.valueParameters ?: emptyList()

        // 1. Generate constructor function
        for (constructor in irClass.declarations.filterIsInstance<IrConstructor>()) {
            transformDataClassConstructor(irClass, constructor, properties)?.let { declarations.add(it) }
        }

        // 2. Generate equals method
        declarations.add(generateDataClassEquals(className, properties))

        // 3. Generate hashCode method
        declarations.add(generateDataClassHashCode(className, properties))

        // 4. Generate toString method
        declarations.add(generateDataClassToString(className, properties))

        // 5. Generate copy method
        declarations.add(generateDataClassCopy(className, primaryConstructor, properties))

        // 6. Generate componentN methods
        properties.forEachIndexed { index, param ->
            declarations.add(generateDataClassComponent(className, param, index + 1))
        }

        // Generate any additional member functions. Data-class generated-member
        // NAMES are skipped (the generators above own them, simple-named);
        // single source with the attachment skip and the shared-dispatch
        // exclusion (isDataClassGeneratedMemberName) - the digit check keeps
        // hand-written component-prefixed members (componentFoo) ordinary.
        for (function in irClass.declarations.filterIsInstance<IrSimpleFunction>()) {
            val name = function.name.asString()
            if (!function.isFakeOverride && !isDataClassGeneratedMemberName(name)) {
                transformFunctionWithSharedWrapper(function, declarations)
            }
        }

        // Process nested classes (companion objects, etc.)
        for (nested in irClass.declarations.filterIsInstance<IrClass>()) {
            transformClassDeclarations(nested, declarations, statements)
        }
    }

    /**
     * Transform a data class constructor.
     */
    private fun transformDataClassConstructor(
        irClass: IrClass,
        constructor: IrConstructor,
        properties: List<IrValueParameter>
    ): BrsFunction? {
        val className = context.getBrsName(irClass)
        // Use mangled constructor name to support overloading (must match call sites)
        val name = context.getBrsName(constructor)

        val parameters = normalizeParametersForBrs(constructor.valueParameters.map { param ->
            BrsParameter(
                name = sanitizeParameterName(param.name.asString()),
                type = mapTypeToBrs(param.type),
                defaultValue = param.defaultValue?.expression?.let { transformExpression(it) }
            )
        })

        val bodyStatements = mutableListOf<BrsStatement>()

        // Materialize defaults over passed-invalid before anything reads the parameters
        bodyStatements.addAll(defaultValueGuards(constructor))

        // this = {}
        bodyStatements.add(
            BrsVariable(
                name = "this",
                type = BrsType.OBJECT,
                initializer = BrsAALiteral(mutableListOf())
            )
        )

        // this.__type = "ClassName"
        bodyStatements.add(
            BrsExpressionStatement(
                BrsBinaryOp(
                    BrsDotAccess(BrsIdentifier("this"), "__type"),
                    BrsBinaryOperator.EQ,
                    BrsStringLiteral(className)
                )
            )
        )

        // this.__proto = ["ClassName", ...interfaces]
        val allInterfaceNamesDataClass = collectAllInterfaceNames(irClass)
        val protoElementsDataClass = mutableListOf<BrsExpression>(BrsStringLiteral(className))
        allInterfaceNamesDataClass.forEach { protoElementsDataClass.add(BrsStringLiteral(it)) }
        bodyStatements.add(
            BrsExpressionStatement(
                BrsBinaryOp(
                    BrsDotAccess(BrsIdentifier("this"), "__proto"),
                    BrsBinaryOperator.EQ,
                    BrsArrayLiteral(protoElementsDataClass)
                )
            )
        )

        // this.__id = __kotlin_nextObjectId()
        // Unique object ID for identity checks (===)
        context.recordFunctionDependency("__kotlin_nextObjectId")
        bodyStatements.add(
            BrsExpressionStatement(
                BrsBinaryOp(
                    BrsDotAccess(BrsIdentifier("this"), "__id"),
                    BrsBinaryOperator.EQ,
                    BrsFunctionCall(BrsIdentifier("__kotlin_nextObjectId"), mutableListOf())
                )
            )
        )

        // Add all properties: this.name = name, this.age = age
        constructor.valueParameters.forEach { param ->
            bodyStatements.add(
                BrsExpressionStatement(
                    BrsBinaryOp(
                        BrsDotAccess(BrsIdentifier("this"), param.name.asString()),
                        BrsBinaryOperator.EQ,
                        BrsIdentifier(sanitizeParameterName(param.name.asString()))
                    )
                )
            )
        }

        // Add any additional methods and property accessors to the instance
        addMethodAttachments(irClass, className, bodyStatements)

        // Attach synthetic data class methods (equals, hashCode, toString, copy)
        val syntheticMethods = listOf("equals", "hashCode", "toString", "copy")
        syntheticMethods.forEach { methodName ->
            bodyStatements.add(
                BrsExpressionStatement(
                    BrsBinaryOp(
                        BrsDotAccess(BrsIdentifier("this"), methodName),
                        BrsBinaryOperator.EQ,
                        BrsIdentifier("${className}_$methodName")
                    )
                )
            )
        }

        // Attach componentN methods
        properties.forEachIndexed { index, _ ->
            bodyStatements.add(
                BrsExpressionStatement(
                    BrsBinaryOp(
                        BrsDotAccess(BrsIdentifier("this"), "component${index + 1}"),
                        BrsBinaryOperator.EQ,
                        BrsIdentifier("${className}_component${index + 1}")
                    )
                )
            )
        }

        // return this
        bodyStatements.add(BrsReturn(BrsIdentifier("this")))

        return BrsFunction(
            name = name,
            parameters = parameters.toMutableList(),
            returnType = BrsType.OBJECT,
            body = BrsBlock(bodyStatements)
        )
    }

    /**
     * Generate equals method for data class.
     */
    private fun generateDataClassEquals(className: String, properties: List<IrValueParameter>): BrsFunction {
        val bodyStatements = mutableListOf<BrsStatement>()

        // if Type(other) <> "roAssociativeArray" then return false
        bodyStatements.add(
            BrsIf(
                condition = BrsBinaryOp(
                    BrsFunctionCall(BrsIdentifier("Type"), mutableListOf(BrsIdentifier("other"))),
                    BrsBinaryOperator.NE,
                    BrsStringLiteral("roAssociativeArray")
                ),
                thenBranch = BrsReturn(BrsBooleanLiteral(false))
            )
        )

        // if other.__type <> "ClassName" then return false
        bodyStatements.add(
            BrsIf(
                condition = BrsBinaryOp(
                    BrsDotAccess(BrsIdentifier("other"), "__type"),
                    BrsBinaryOperator.NE,
                    BrsStringLiteral(className)
                ),
                thenBranch = BrsReturn(BrsBooleanLiteral(false))
            )
        )

        // Compare each property: if m.name <> other.name then return false
        properties.forEach { param ->
            val propName = param.name.asString()
            bodyStatements.add(
                BrsIf(
                    condition = BrsBinaryOp(
                        BrsDotAccess(BrsIdentifier("m"), propName),
                        BrsBinaryOperator.NE,
                        BrsDotAccess(BrsIdentifier("other"), propName)
                    ),
                    thenBranch = BrsReturn(BrsBooleanLiteral(false))
                )
            )
        }

        // return true
        bodyStatements.add(BrsReturn(BrsBooleanLiteral(true)))

        return BrsFunction(
            name = "${className}_equals",
            parameters = mutableListOf(BrsParameter("other", BrsType.OBJECT, null)),
            returnType = BrsType.BOOLEAN,
            body = BrsBlock(bodyStatements)
        )
    }

    /**
     * Generate hashCode method for data class.
     */
    private fun generateDataClassHashCode(className: String, properties: List<IrValueParameter>): BrsFunction {
        val bodyStatements = mutableListOf<BrsStatement>()

        // result = 1
        bodyStatements.add(
            BrsVariable("result", BrsType.INTEGER, BrsIntLiteral(1))
        )

        // For each property: result = result * 31 + hash(property)
        // Note: BrightScript doesn't have a built-in hash function, so we use simple calculation
        properties.forEach { param ->
            val propName = param.name.asString()
            // result = result * 31 + (m.property if type is number, or Len(m.property) for strings)
            bodyStatements.add(
                BrsExpressionStatement(
                    BrsBinaryOp(
                        BrsIdentifier("result"),
                        BrsBinaryOperator.EQ,
                        BrsBinaryOp(
                            BrsBinaryOp(
                                BrsIdentifier("result"),
                                BrsBinaryOperator.MUL,
                                BrsIntLiteral(31)
                            ),
                            BrsBinaryOperator.ADD,
                            BrsDotAccess(BrsIdentifier("m"), propName)
                        )
                    )
                )
            )
        }

        // return result
        bodyStatements.add(BrsReturn(BrsIdentifier("result")))

        return BrsFunction(
            name = "${className}_hashCode",
            parameters = mutableListOf(),
            returnType = BrsType.INTEGER,
            body = BrsBlock(bodyStatements)
        )
    }

    /**
     * Generate toString method for data class.
     */
    private fun generateDataClassToString(className: String, properties: List<IrValueParameter>): BrsFunction {
        val bodyStatements = mutableListOf<BrsStatement>()

        // Build: "ClassName(prop1=" + m.prop1 + ", prop2=" + m.prop2 + ")"
        val propStrings = properties.mapIndexed { index, param ->
            val propName = param.name.asString()
            val prefix = if (index == 0) "$className($propName=" else ", $propName="
            listOf(
                BrsStringLiteral(prefix),
                BrsDotAccess(BrsIdentifier("m"), propName)
            )
        }.flatten()

        // Concatenate all parts
        val concatenation = if (propStrings.isEmpty()) {
            BrsStringLiteral("$className()")
        } else {
            var result: BrsExpression = propStrings.first()
            propStrings.drop(1).forEach { part ->
                result = BrsBinaryOp(result, BrsBinaryOperator.ADD, part)
            }
            BrsBinaryOp(result, BrsBinaryOperator.ADD, BrsStringLiteral(")"))
        }

        bodyStatements.add(BrsReturn(concatenation))

        return BrsFunction(
            name = "${className}_toString",
            parameters = mutableListOf(),
            returnType = BrsType.STRING,
            body = BrsBlock(bodyStatements)
        )
    }

    /**
     * Generate copy method for data class.
     *
     * BrightScript doesn't support expressions like `m.name` as default parameter values,
     * so we use `invalid` as the default and add runtime checks to substitute the current value.
     */
    private fun generateDataClassCopy(className: String, primaryConstructor: IrConstructor?, properties: List<IrValueParameter>): BrsFunction {
        val bodyStatements = mutableListOf<BrsStatement>()

        // Parameters with invalid as default (BrightScript doesn't allow m.property as default)
        val parameters = properties.map { param ->
            val propName = param.name.asString()
            val sanitizedName = sanitizeParameterName(propName)
            BrsParameter(
                name = sanitizedName,
                type = null,  // Untyped to allow invalid
                defaultValue = BrsIdentifier("invalid")
            )
        }

        // Add runtime checks: if param = invalid then param = m.param
        properties.forEach { param ->
            val propName = param.name.asString()
            val sanitizedName = sanitizeParameterName(propName)
            bodyStatements.add(
                BrsIf(
                    condition = BrsBinaryOp(
                        BrsIdentifier(sanitizedName),
                        BrsBinaryOperator.EQ,
                        BrsIdentifier("invalid")
                    ),
                    thenBranch = BrsExpressionStatement(
                        BrsBinaryOp(
                            BrsIdentifier(sanitizedName),
                            BrsBinaryOperator.EQ,
                            BrsDotAccess(BrsIdentifier("m"), propName)  // Property access uses original name
                        )
                    ),
                    elseBranch = null
                )
            )
        }

        // Get the mangled constructor name (must match the definition)
        val constructorName = if (primaryConstructor != null) {
            context.getBrsName(primaryConstructor)
        } else {
            "${className}_create"  // Fallback for edge cases
        }

        // return ClassName_create(name, age, ...)
        val args = properties.map { param ->
            BrsIdentifier(sanitizeParameterName(param.name.asString()))
        }
        bodyStatements.add(
            BrsReturn(BrsFunctionCall(BrsIdentifier(constructorName), args.toMutableList()))
        )

        return BrsFunction(
            name = "${className}_copy",
            parameters = parameters.toMutableList(),
            returnType = BrsType.OBJECT,
            body = BrsBlock(bodyStatements)
        )
    }

    /**
     * Generate componentN method for data class.
     */
    private fun generateDataClassComponent(
        className: String,
        param: IrValueParameter,
        index: Int
    ): BrsFunction {
        val bodyStatements = mutableListOf<BrsStatement>()

        // return m.propertyName
        bodyStatements.add(
            BrsReturn(BrsDotAccess(BrsIdentifier("m"), param.name.asString()))
        )

        return BrsFunction(
            name = "${className}_component$index",
            parameters = mutableListOf(),
            returnType = mapTypeToBrs(param.type),
            body = BrsBlock(bodyStatements)
        )
    }

    /**
     * Transform a constructor to a BrightScript function.
     *
     * Classes are transformed to constructor functions that return associative arrays.
     * Inheritance is handled via prototype chain pattern:
     * - Child calls parent constructor first
     * - Parent methods stored in `_super` for super calls
     * - `__type` tracks class name, `__proto` tracks inheritance chain
     */
    private fun transformConstructor(irClass: IrClass, constructor: IrConstructor): BrsFunction? {
        // Use mangled constructor name to support overloading
        val name = context.getBrsName(constructor)
        // Class name is used for __type and __proto metadata (not mangled)
        val className = context.getBrsName(irClass)

        val parametersList = mutableListOf<BrsParameter>()

        // For inner classes, add _outer parameter first
        if (irClass.isInner) {
            parametersList.add(BrsParameter("_outer", BrsType.OBJECT))
        }

        // Add regular constructor parameters
        parametersList.addAll(constructor.valueParameters.map { param ->
            BrsParameter(
                name = sanitizeParameterName(param.name.asString()),
                type = mapTypeToBrs(param.type),
                defaultValue = param.defaultValue?.expression?.let { transformExpression(it) }
            )
        })

        val parameters = normalizeParametersForBrs(parametersList)

        // Build constructor body
        val bodyStatements = mutableListOf<BrsStatement>()

        // Materialize defaults over passed-invalid FIRST — super-call and
        // this(...)-delegation arguments below read the parameters.
        bodyStatements.addAll(defaultValueGuards(constructor))

        // Check if this constructor delegates to another constructor of the same class (this(...))
        // If so, we just call that constructor and return the result - no object initialization here
        val delegatingCall = findDelegatingConstructorCall(constructor)
        if (delegatingCall != null) {
            val delegatedConstructor = delegatingCall.symbol.owner
            val delegatedClass = delegatedConstructor.parentAsClass
            if (delegatedClass == irClass) {
                // This is a this(...) delegation - just call the other constructor and return
                val (hoistedStatements, args) = getSuperConstructorArgsWithHoisting(constructor)
                bodyStatements.addAll(hoistedStatements)
                val delegatedName = context.getBrsName(delegatedConstructor)
                val call = createFunctionCall(delegatedName, args, context)
                bodyStatements.add(BrsReturn(call))
                return BrsFunction(name, parameters.toMutableList(), BrsType.OBJECT, BrsBlock(bodyStatements))
            }
        }

        // Check if there's a superclass (not Any)
        val superClass = irClass.superTypes
            .mapNotNull { it.classOrNull?.owner }
            .firstOrNull { !it.isInterface && it.name.asString() != "Any" }

        if (superClass != null) {
            // Find the delegating constructor call to get the super constructor
            val delegatingCall = findDelegatingConstructorCall(constructor)
            val superConstructor = delegatingCall?.symbol?.owner

            // Get super constructor arguments with any hoisted statements
            // This handles cases like super(message?.toString()) where the safe call
            // creates a block with temp variables that need to be extracted
            val (hoistedStatements, superArgs) = getSuperConstructorArgsWithHoisting(constructor)

            // Add hoisted statements before the super call
            bodyStatements.addAll(hoistedStatements)

            // Call parent constructor: this = ParentClass_create_ParamTypes_k$(...)
            val superConstructorName = if (superConstructor != null) {
                context.getBrsName(superConstructor)
            } else {
                // Fallback: use class name + _create for no-arg constructor
                "${context.getBrsName(superClass)}_create"
            }
            val superConstructorCall = createFunctionCall(superConstructorName, superArgs, context)
            bodyStatements.add(
                BrsVariable(name = "this", initializer = superConstructorCall)
            )

            // Store parent methods for super calls: this._super = {}
            bodyStatements.add(
                BrsExpressionStatement(
                    BrsBinaryOp(
                        BrsDotAccess(BrsIdentifier("this"), "_super"),
                        BrsBinaryOperator.EQ,
                        BrsAALiteral()
                    )
                )
            )

            // Copy overridden methods to _super before overriding
            for (function in irClass.declarations.filterIsInstance<IrSimpleFunction>()) {
                if (!function.isFakeOverride && function.overriddenSymbols.isNotEmpty()) {
                    val fullMethodName = context.getBrsName(function)
                    val methodName = fullMethodName.removePrefix("${className}_")
                    bodyStatements.add(
                        BrsExpressionStatement(
                            BrsBinaryOp(
                                BrsDotAccess(
                                    BrsDotAccess(BrsIdentifier("this"), "_super"),
                                    methodName
                                ),
                                BrsBinaryOperator.EQ,
                                BrsDotAccess(BrsIdentifier("this"), methodName)
                            )
                        )
                    )
                }
            }

            // Update __proto chain - include this class and its direct interfaces
            // (inherited interfaces come from the parent's __proto)
            val directInterfaceNames = irClass.superTypes
                .filter { it.classOrNull?.owner?.isInterface == true }
                .map { context.getBrsName(it.classOrNull!!.owner) }
            val protoElements = mutableListOf<BrsExpression>(BrsStringLiteral(className))
            directInterfaceNames.forEach { protoElements.add(BrsStringLiteral(it)) }
            protoElements.add(BrsDotAccess(BrsIdentifier("this"), "__proto"))
            bodyStatements.add(
                BrsExpressionStatement(
                    BrsBinaryOp(
                        BrsDotAccess(BrsIdentifier("this"), "__proto"),
                        BrsBinaryOperator.EQ,
                        BrsArrayLiteral(protoElements)
                    )
                )
            )
        } else {
            // No superclass - create new object
            bodyStatements.add(
                BrsVariable(name = "this", initializer = BrsAALiteral())
            )

            // Set type info
            bodyStatements.add(
                BrsExpressionStatement(
                    BrsBinaryOp(
                        BrsDotAccess(BrsIdentifier("this"), "__type"),
                        BrsBinaryOperator.EQ,
                        BrsStringLiteral(className)
                    )
                )
            )

            // Initialize __proto chain with this class and all implemented interfaces
            val allInterfaceNames = collectAllInterfaceNames(irClass)
            val protoElements = mutableListOf<BrsExpression>(BrsStringLiteral(className))
            allInterfaceNames.forEach { protoElements.add(BrsStringLiteral(it)) }
            bodyStatements.add(
                BrsExpressionStatement(
                    BrsBinaryOp(
                        BrsDotAccess(BrsIdentifier("this"), "__proto"),
                        BrsBinaryOperator.EQ,
                        BrsArrayLiteral(protoElements)
                    )
                )
            )

            // this.__id = __kotlin_nextObjectId()
            // Unique object ID for identity checks (===)
            context.recordFunctionDependency("__kotlin_nextObjectId")
            bodyStatements.add(
                BrsExpressionStatement(
                    BrsBinaryOp(
                        BrsDotAccess(BrsIdentifier("this"), "__id"),
                        BrsBinaryOperator.EQ,
                        BrsFunctionCall(BrsIdentifier("__kotlin_nextObjectId"), mutableListOf())
                    )
                )
            )

            // Add default Any methods for classes that directly extend Any
            // (Classes with explicit superclasses inherit these from the parent constructor call)
            // These provide default implementations that can be overridden by the class itself
            addAnyMethodDefaults(bodyStatements)
        }

        // Update __type to current class for inherited classes
        // (base classes already set __type above in the else branch)
        if (superClass != null) {
            bodyStatements.add(
                BrsExpressionStatement(
                    BrsBinaryOp(
                        BrsDotAccess(BrsIdentifier("this"), "__type"),
                        BrsBinaryOperator.EQ,
                        BrsStringLiteral(className)
                    )
                )
            )
        }

        // For inner classes, store the outer reference
        if (irClass.isInner) {
            bodyStatements.add(
                BrsExpressionStatement(
                    BrsBinaryOp(
                        BrsDotAccess(BrsIdentifier("this"), "_outer"),
                        BrsBinaryOperator.EQ,
                        BrsIdentifier("_outer")
                    )
                )
            )
        }

        // Set flag so that '<this>' references map to 'this' instead of 'm'
        // This must be set BEFORE transforming field/property initializers, as they may
        // reference constructor parameters via getters on <this>
        genCtx.isInConstructorBody = true

        // Add methods and property accessors to the instance BEFORE field/property initializers
        // because initializers may call methods on 'this' (e.g., this.get_map().get_keyOrder())
        addMethodAttachments(irClass, className, bodyStatements)

        // Initialize fields/property backing fields and execute init { } block bodies.
        // Kotlin semantics: property initializers and anonymous initializers run in
        // DECLARATION ORDER (after the super-constructor call), so this must be a
        // single ordered pass over the class declarations — an init block between two
        // properties observes the first initialized and the second not yet.
        val initializedFields = mutableSetOf<String>()

        // Field keys use the shared sanitizeFieldName rule (brsTransformerUtils): special
        // names like <this> de-bracket; a delegated property's backing field
        // <propertyName>$delegate (e.g., lazyValue$delegate) gets its `$` replaced.
        fun emitFieldInitializer(field: IrField) {
            val fieldName = sanitizeFieldName(field.name.asString())
            if (fieldName in initializedFields) return
            field.initializer?.expression?.let { initializer ->
                initializedFields.add(fieldName)
                val transformedInit = transformExpression(initializer)
                // Consume hoisted statements from when-lowered blocks in the initializer
                val hoisted = genCtx.takeHoistedStatements()
                bodyStatements.addAll(hoisted)
                bodyStatements.add(
                    BrsExpressionStatement(
                        BrsBinaryOp(
                            BrsDotAccess(BrsIdentifier("this"), fieldName),
                            BrsBinaryOperator.EQ,
                            transformedInit
                        )
                    )
                )
            }
        }

        for (declaration in irClass.declarations) {
            when (declaration) {
                is IrField -> emitFieldInitializer(declaration)
                // Property backing fields are usually not direct members of declarations
                is IrProperty -> declaration.backingField?.let { emitFieldInitializer(it) }
                is IrAnonymousInitializer -> if (!declaration.isStatic) {
                    for (stmt in declaration.body.statements) {
                        val transformed = transformStatement(stmt)
                        // Statement visitors consume their own hoisted statements; the
                        // bare-expression fall-through in transformStatement does not,
                        // so drain any leftovers before the statement that needs them
                        bodyStatements.addAll(genCtx.takeHoistedStatements())
                        transformed?.let { bodyStatements.add(it) }
                    }
                }
                else -> {}
            }
        }

        // Execute constructor body (handles property initialization from parameters, etc.)
        constructor.body?.let { body ->
            val transformed = transformBody(body)
            // Filter out delegating constructor calls since we handle them above
            val filteredStatements = transformed.statements.filter { stmt ->
                !(stmt is BrsExpressionStatement && isDelegatingConstructorCall(stmt))
            }
            bodyStatements.addAll(filteredStatements)
        }

        // Reset constructor context flag
        genCtx.isInConstructorBody = false

        // Return the constructed object
        bodyStatements.add(BrsReturn(BrsIdentifier("this")))

        return BrsFunction(
            name,
            parameters.toMutableList(),
            BrsType.OBJECT,
            BrsBlock(bodyStatements)
        )
    }

    /**
     * Add default Any method implementations for classes that directly extend Any.
     *
     * All Kotlin classes implicitly extend Any, which provides equals, hashCode, and toString.
     * When a class has an explicit superclass, these methods are inherited from the parent.
     * When a class only extends Any (no explicit superclass), we need to attach these methods
     * directly so that structural equality checks and other operations work correctly.
     *
     * These are default implementations - if the class overrides any of these methods,
     * the overriding implementation will be attached later by addMethodAttachments and will
     * replace these defaults.
     */
    private fun addAnyMethodDefaults(bodyStatements: MutableList<BrsStatement>) {
        // All calls to equals/hashCode/toString use simple names (see call site generation),
        // so we only need to attach the simple name aliases. The mangled names are never called.

        // The Any_* names below are BARE IDENTIFIER references (function values, not calls),
        // so they never flow through createFunctionCall - record them explicitly. Every
        // root-class _create references them cross-file into AnyKt.brs; without these
        // records a component's script list can drop AnyKt.brs entirely and structural
        // equality (obj ==, object-keyed HashMap, "$obj") breaks in component scope.
        context.recordFunctionDependency("Any_equals_AnyN_k_")
        context.recordFunctionDependency("Any_hashCode_k_")
        context.recordFunctionDependency("Any_toString_k_")

        // this.equals = Any_equals_AnyN_k_
        bodyStatements.add(
            BrsExpressionStatement(
                BrsBinaryOp(
                    BrsDotAccess(BrsIdentifier("this"), "equals"),
                    BrsBinaryOperator.EQ,
                    BrsIdentifier("Any_equals_AnyN_k_")
                )
            )
        )
        // this.hashCode = Any_hashCode_k_
        bodyStatements.add(
            BrsExpressionStatement(
                BrsBinaryOp(
                    BrsDotAccess(BrsIdentifier("this"), "hashCode"),
                    BrsBinaryOperator.EQ,
                    BrsIdentifier("Any_hashCode_k_")
                )
            )
        )
        // this.toString = Any_toString_k_
        bodyStatements.add(
            BrsExpressionStatement(
                BrsBinaryOp(
                    BrsDotAccess(BrsIdentifier("this"), "toString"),
                    BrsBinaryOperator.EQ,
                    BrsIdentifier("Any_toString_k_")
                )
            )
        )
    }

    // ==================== SharedService extension-shaped emission ====================
    // The shared predicates (isSharedExtensionShapedFunction /
    // isSimpleSharedAccessor) and the whole dispatch machinery (registry,
    // call-site classification, dispatcher generation) live in
    // lower/BrsSharedDispatchLowering.kt — this file only wires them into
    // emission (Task 6 moved the Task 5 predicates there so the call-site
    // classification and the emission switch share one source).

    /**
     * Emit [function] via [transformFunction] plus, for SharedService members,
     * the forwarding wrapper slot the constructor attaches in its place — and,
     * for open/abstract members of non-final shared classes, the
     * `__proto`-name dispatcher static call sites route virtual calls through
     * (generated HERE, next to the declaring impl, so each dispatcher is
     * defined exactly once per module, in the declaring class's file, with
     * its signature derived from the emitted impl).
     */
    private fun transformFunctionWithSharedWrapper(
        function: IrSimpleFunction,
        declarations: MutableList<BrsDeclaration>
    ) {
        val impl = transformFunction(function) ?: return
        declarations.add(impl)
        if (isSharedExtensionShapedFunction(function, context)) {
            declarations.add(buildSharedSlotWrapper(impl))
            if (sharedDispatcherCandidate(function, context)) {
                declarations.add(buildSharedDispatcher(function, impl, context))
            }
        }
    }

    /**
     * The forwarding wrapper slot for an extension-shaped SharedService member:
     * invoked AS A SLOT (so its implicit `m` is the receiver), it forwards to
     * the implementation global with the receiver made explicit.
     *
     * ```brightscript
     * function C_f_I_k___slot(x as Integer) as Integer
     *     return C_f_I_k_(m, x)
     * end function
     * ```
     *
     * The wrapper is derived from the EMITTED implementation's final parameter
     * list — never re-derived from IR — so its arity (suspend `_completion`
     * included) cannot drift from the implementation's (the spec's named
     * wrapper-arg-shift hazard): params = impl params minus the injected
     * receiver, defaults included; args = `m` plus those params in order.
     */
    private fun buildSharedSlotWrapper(impl: BrsDeclaration): BrsDeclaration {
        val (implName, implParams) = when (impl) {
            is BrsFunction -> impl.name to impl.parameters
            is BrsSub -> impl.name to impl.parameters
            else -> error("SharedService wrapper: unexpected implementation shape ${impl::class.simpleName}")
        }
        check(implParams.firstOrNull()?.name == "m") {
            "SharedService wrapper: implementation $implName has no leading receiver parameter"
        }
        val forwarded = implParams.drop(1)
        val callArgs = mutableListOf<BrsExpression>(BrsMRef())
        forwarded.mapTo(callArgs) { BrsIdentifier(it.name) }
        val call = BrsFunctionCall(BrsIdentifier(implName), callArgs)
        val wrapperName = implName + SHARED_SLOT_WRAPPER_SUFFIX
        return when (impl) {
            is BrsFunction -> BrsFunction(
                wrapperName,
                forwarded.toMutableList(),
                impl.returnType,
                BrsBlock(mutableListOf(BrsReturn(call)))
            )
            is BrsSub -> BrsSub(
                wrapperName,
                forwarded.toMutableList(),
                BrsBlock(mutableListOf(BrsExpressionStatement(call)))
            )
            else -> error("unreachable")
        }
    }

    /**
     * Add method and property accessor attachments to a class instance.
     * This attaches both IrSimpleFunction methods and property getter/setters.
     *
     * Important: Regular methods use mangled names (with type signatures) to support overloading.
     * Property accessors use simple names (via sanitizeMethodName) to match call sites.
     *
     * SharedService members attach their forwarding WRAPPER (`<impl>__slot`)
     * instead of the extension-shaped implementation — attaching the
     * implementation directly would be arity-broken as a slot (its explicit
     * receiver parameter would swallow the first argument).
     */
    private fun addMethodAttachments(
        irClass: IrClass,
        className: String,
        bodyStatements: MutableList<BrsStatement>
    ) {
        val isDataClass = irClass.isData
        val isEnumClass = irClass.kind == ClassKind.ENUM_CLASS

        // Add regular methods (IrSimpleFunction)
        // Use mangled names (fullMethodName minus class prefix) to support overloading
        for (function in irClass.declarations.filterIsInstance<IrSimpleFunction>()) {
            if (!function.isFakeOverride && !function.isExternal) {
                val methodBaseName = function.name.asString()

                // Skip data-class generated-member names - they're attached separately
                // with simple names by the data-class ctor emitter. This prevents
                // creating references like Pair_toString_Str_k_ which don't exist.
                // Single source with the emission skip and the shared-dispatch
                // exclusion (isDataClassGeneratedMemberName) - a mismatch between
                // these skips is exactly what produced dangling attachments before.
                if (isDataClass && isDataClassGeneratedMemberName(methodBaseName)) {
                    continue
                }

                // Skip values and valueOf for enum classes - these are generated with custom implementations
                // and would have different names (ClassName_values vs ClassName_values_k_)
                if (isEnumClass && (methodBaseName == "values" || methodBaseName == "valueOf")) {
                    continue
                }

                val fullMethodName = context.getBrsName(function)
                val methodName = fullMethodName.removePrefix("${className}_")
                // SharedService members attach the forwarding wrapper, not the
                // extension-shaped implementation (see class KDoc above).
                val attachedName = if (isSharedExtensionShapedFunction(function, context)) {
                    fullMethodName + SHARED_SLOT_WRAPPER_SUFFIX
                } else {
                    fullMethodName
                }
                bodyStatements.add(
                    BrsExpressionStatement(
                        BrsBinaryOp(
                            BrsDotAccess(BrsIdentifier("this"), methodName),
                            BrsBinaryOperator.EQ,
                            BrsIdentifier(attachedName)
                        )
                    )
                )

                // For Any methods (equals, hashCode, toString), also add simple-named alias
                // This allows polymorphic calls like a.equals(b) where 'a' is Any
                if (!isDataClass && methodBaseName in listOf("equals", "hashCode", "toString")) {
                    bodyStatements.add(
                        BrsExpressionStatement(
                            BrsBinaryOp(
                                BrsDotAccess(BrsIdentifier("this"), methodBaseName),
                                BrsBinaryOperator.EQ,
                                BrsIdentifier(attachedName)
                            )
                        )
                    )
                }
            }
        }

        // Add property accessor methods (getter/setter)
        // Use sanitizeMethodName to match call site naming convention
        // Skip for data classes - their properties are constructor parameters accessed directly (e.g., m.first)
        if (!isDataClass) {
            for (property in irClass.declarations.filterIsInstance<IrProperty>()) {
                // Skip the synthetic 'entries' property for enum classes - it's generated separately
                if (isEnumClass && property.name.asString() == "entries") {
                    continue
                }
                property.getter?.let { getter ->
                    if (!getter.isFakeOverride && !getter.isExternal) {
                        val fullMethodName = context.getBrsName(getter)
                        val methodName = sanitizeMethodName(getter.name.asString())
                        // Non-trivial SharedService accessors are extension-shaped
                        // like methods; simple ones keep their direct attachment.
                        val attachedName = if (isSharedExtensionShapedFunction(getter, context)) {
                            fullMethodName + SHARED_SLOT_WRAPPER_SUFFIX
                        } else {
                            fullMethodName
                        }
                        bodyStatements.add(
                            BrsExpressionStatement(
                                BrsBinaryOp(
                                    BrsDotAccess(BrsIdentifier("this"), methodName),
                                    BrsBinaryOperator.EQ,
                                    BrsIdentifier(attachedName)
                                )
                            )
                        )
                    }
                }
                property.setter?.let { setter ->
                    if (!setter.isFakeOverride && !setter.isExternal) {
                        val fullMethodName = context.getBrsName(setter)
                        val methodName = sanitizeMethodName(setter.name.asString())
                        val attachedName = if (isSharedExtensionShapedFunction(setter, context)) {
                            fullMethodName + SHARED_SLOT_WRAPPER_SUFFIX
                        } else {
                            fullMethodName
                        }
                        bodyStatements.add(
                            BrsExpressionStatement(
                                BrsBinaryOp(
                                    BrsDotAccess(BrsIdentifier("this"), methodName),
                                    BrsBinaryOperator.EQ,
                                    BrsIdentifier(attachedName)
                                )
                            )
                        )
                    }
                }
            }
        }
    }

    /**
     * Recursively collect all interface names from a class's supertypes.
     * This is used to populate the __proto chain for instanceof checks.
     */
    private fun collectAllInterfaceNames(irClass: IrClass): List<String> {
        val result = mutableSetOf<String>()
        val visited = mutableSetOf<IrClass>()

        fun collectInterfaces(type: IrType) {
            val classifier = type.classOrNull?.owner ?: return
            if (classifier in visited) return
            visited.add(classifier)

            if (classifier.isInterface) {
                result.add(context.getBrsName(classifier))
            }

            // Recursively collect supertype interfaces
            classifier.superTypes.forEach { collectInterfaces(it) }
        }

        irClass.superTypes.forEach { collectInterfaces(it) }
        return result.toList()
    }

    /**
     * Find the delegating constructor call (super() or this()) in a constructor body.
     */
    private fun findDelegatingConstructorCall(constructor: IrConstructor): IrDelegatingConstructorCall? {
        return constructor.body?.let { body ->
            when (body) {
                is IrBlockBody -> body.statements.filterIsInstance<IrDelegatingConstructorCall>().firstOrNull()
                else -> null
            }
        }
    }

    /**
     * Get arguments for super constructor call from the delegating constructor call in the body.
     * Returns a pair of (hoisted statements, argument expressions).
     * Hoisted statements should be inserted before the super call.
     *
     * This handles cases where arguments contain blocks (e.g., from safe calls like `message?.toString()`)
     * that need their prefix statements hoisted before the super call.
     */
    private fun getSuperConstructorArgsWithHoisting(constructor: IrConstructor): Pair<List<BrsStatement>, MutableList<BrsExpression>> {
        val delegatingCall = findDelegatingConstructorCall(constructor) ?: return Pair(emptyList(), mutableListOf())

        val hoistedStatements = mutableListOf<BrsStatement>()
        val arguments = mutableListOf<BrsExpression>()

        for (i in 0 until delegatingCall.valueArgumentsCount) {
            val arg = delegatingCall.getValueArgument(i)
            if (arg == null) {
                // Absent argument: default (filled callee-side) or empty vararg
                arguments.add(absentArgumentPlaceholder(delegatingCall.symbol.owner, i))
                continue
            }

            // Flatten nested blocks and extract the final expression value
            val (stmts, valueExpr) = flattenBlockForHoisting(arg)
            stmts.forEach { stmt ->
                val transformed = when (stmt) {
                    is IrVariable -> {
                        val varName = stmt.name.asString()
                        // Sanitize variable names - handle special names and escape reserved keywords
                        val sanitizedVarName = sanitizeParameterName(varName)
                        val init = stmt.initializer?.let { transformExpression(it) }
                        if (init != null) BrsVariable(sanitizedVarName, mapTypeToBrs(stmt.type), init) else null
                    }
                    is IrWhen -> statementTransformer.visitWhen(stmt, Unit)
                    else -> transformStatement(stmt)
                }
                transformed?.let { hoistedStatements.add(it) }
            }

            if (valueExpr != null) {
                arguments.add(transformExpression(valueExpr))
            } else {
                arguments.add(BrsInvalidLiteral())
            }
        }

        return Pair(hoistedStatements, arguments)
    }

    /**
     * Recursively flatten nested blocks, extracting all prefix statements and the final value expression.
     * This handles patterns like:
     *   Block { val x = ..., Block { var tmp = null, When { ... }, GetValue(tmp) } }
     * Returns a pair of (all prefix statements to hoist, final value expression).
     */
    private fun flattenBlockForHoisting(expr: IrExpression): Pair<List<IrStatement>, IrExpression?> {
        if (expr !is IrBlock || expr.statements.isEmpty()) {
            return Pair(emptyList(), expr)
        }

        val allStatements = mutableListOf<IrStatement>()
        val blockStatements = expr.statements

        // Process all statements except the last
        for (i in 0 until blockStatements.size - 1) {
            allStatements.add(blockStatements[i])
        }

        // Check if the last statement is itself a block that needs flattening
        val lastStmt = blockStatements.lastOrNull()
        return when {
            lastStmt is IrBlock -> {
                // Recursively flatten the nested block
                val (nestedStmts, nestedValue) = flattenBlockForHoisting(lastStmt)
                allStatements.addAll(nestedStmts)
                Pair(allStatements, nestedValue)
            }
            lastStmt is IrExpression -> {
                Pair(allStatements, lastStmt)
            }
            else -> {
                Pair(allStatements, null)
            }
        }
    }

    /**
     * Get arguments for super constructor call from the delegating constructor call in the body.
     * This is a backward-compatible wrapper around getSuperConstructorArgsWithHoisting.
     */
    private fun getSuperConstructorArgs(constructor: IrConstructor): MutableList<BrsExpression> {
        return getSuperConstructorArgsWithHoisting(constructor).second
    }

    /**
     * Check if a statement is a delegating constructor call (super() or this()).
     * These calls generate BrsFunctionCall to ParentClass_create() functions.
     */
    private fun isDelegatingConstructorCall(stmt: BrsStatement): Boolean {
        if (stmt !is BrsExpressionStatement) return false
        val expr = stmt.expression
        if (expr !is BrsFunctionCall) return false
        val target = expr.target
        if (target !is BrsIdentifier) return false
        return target.name.endsWith("_create")
    }

    /**
     * Transform a property to BrightScript representation.
     */
    private fun transformProperty(
        property: IrProperty,
        declarations: MutableList<BrsDeclaration>,
        statements: MutableList<BrsStatement>
    ) {
        // Generate getter function if present (+ wrapper slot for non-trivial
        // SharedService accessors — simple ones stay direct, see
        // isSharedExtensionShapedFunction)
        property.getter?.let { getter ->
            if (!getter.isFakeOverride) {
                transformFunctionWithSharedWrapper(getter, declarations)
            }
        }

        // Generate setter function if present
        property.setter?.let { setter ->
            if (!setter.isFakeOverride) {
                transformFunctionWithSharedWrapper(setter, declarations)
            }
        }

        // For top-level properties with backing fields, generate initialization
        // using GetGlobalAA() for module-level storage.
        // Skip constant initializers since they are inlined in visitGetField.
        property.backingField?.let { field ->
            if (field.parent is IrFile) {
                field.initializer?.expression?.let { initializer ->
                    // Skip constant expressions - they will be inlined in the getter
                    if (!isConstantExpression(initializer, context)) {
                        val propName = property.name.asString()
                        // Use GetGlobalAA().propName = value for module-level property initialization
                        statements.add(
                            BrsExpressionStatement(
                                BrsBinaryOp(
                                    BrsDotAccess(
                                        BrsFunctionCall(BrsIdentifier("GetGlobalAA"), mutableListOf()),
                                        propName
                                    ),
                                    BrsBinaryOperator.EQ,
                                    transformExpression(initializer)
                                )
                            )
                        )
                    }
                }
            }
        }
    }

    // ==================== Body and Statements ====================

    /**
     * Transform an IR body to a BrightScript block.
     */
    fun transformBody(body: IrBody): BrsBlock {
        return when (body) {
            is IrBlockBody -> {
                val statements = body.statements.mapNotNull { statement ->
                    transformStatement(statement)
                }
                BrsBlock(statements.toMutableList())
            }
            is IrExpressionBody -> {
                BrsBlock(mutableListOf(BrsReturn(transformExpression(body.expression))))
            }
            is IrSyntheticBody -> {
                BrsBlock()
            }
        }
    }

    /**
     * Transform an IR statement to a BrightScript statement.
     *
     * Note: Some IR "statements" are actually expressions (like IrSetField).
     * When these appear in a statement position, we wrap them in BrsExpressionStatement.
     */
    fun transformStatement(statement: IrStatement): BrsStatement? {
        // First try the statement transformer
        val result = statement.accept(statementTransformer, Unit)
        if (result != null) return result

        // Skip constructor-related IR nodes - handled during class/enum transformation
        if (statement is IrDelegatingConstructorCall ||
            statement is IrInstanceInitializerCall ||
            statement is IrEnumConstructorCall) {
            return null
        }

        // If the statement is actually an expression, wrap it in an expression statement
        if (statement is IrExpression) {
            return expressionToStatementOrNull(transformExpression(statement))
        }

        return null
    }

    // ==================== Expressions ====================

    /**
     * Transform an IR expression to a BrightScript expression.
     */
    fun transformExpression(expression: IrExpression): BrsExpression {
        return expression.accept(expressionTransformer, Unit)
    }

    /**
     * Wrap a BRS expression in a statement, or return null if the expression
     * has no side effects and shouldn't be emitted as a standalone statement.
     */
    fun expressionToStatementOrNull(expr: BrsExpression): BrsStatement? {
        // Skip side-effect-free expressions (like simple variable reads, literals)
        return when (expr) {
            is BrsIdentifier,
            is BrsInvalidLiteral,
            is BrsIntLiteral,
            is BrsLongIntLiteral,
            is BrsDoubleLiteral,
            is BrsFloatLiteral,
            is BrsStringLiteral,
            is BrsBooleanLiteral -> null
            else -> BrsExpressionStatement(expr)
        }
    }

    // ==================== Helpers ====================

    /**
     * Check if an IR expression is a compile-time constant that can be inlined.
     * Recognizes:
     * - Primitive constants (IrConst)
     * - Enum value references (IrGetEnumValue)
     * - Property accesses on enum values for ordinal, name, and constant properties
     */
    // ==================== Visitor Implementation ====================

    override fun visitElement(element: IrElement, data: Unit): BrsNode? {
        // Default implementation - should be handled by specialized transformers
        return null
    }

    /**
     * Check if a class is an extracted local class (lambda or function reference).
     * These classes are extracted from function bodies by BrsLocalClassExtractionLowering
     * but their code is still generated inline in the containing file, not as separate .brs files.
     */
    internal fun isExtractedLocalClass(irClass: IrClass): Boolean {
        // Check for lambda or function reference origin
        val origin = irClass.origin
        if (origin == org.jetbrains.kotlin.backend.common.lower.WebCallableReferenceLowering.LAMBDA_IMPL ||
            origin == org.jetbrains.kotlin.backend.common.lower.WebCallableReferenceLowering.FUNCTION_REFERENCE_IMPL) {
            return true
        }
        // Also check if parent is still a function (before extraction)
        if (irClass.parent is IrFunction) {
            return true
        }
        return false
    }

    /**
     * Determine the .brs file name a class's constructor will be compiled to.
     * Returns null if no dependency should be recorded (e.g., external or local class).
     */
    internal fun determineBrsFileNameForClass(irClass: IrClass): String? {
        // External classes don't produce output files
        if (irClass.isExternal) return null

        // Extracted local classes (lambdas, anonymous) are inlined
        if (isExtractedLocalClass(irClass)) return null

        // Check manifest first - this handles stdlib classes correctly
        val constructor = irClass.declarations.filterIsInstance<IrConstructor>().firstOrNull()
        if (constructor != null) {
            val constructorName = context.getBrsName(constructor)
            context.dependencyFunctionManifest[constructorName]?.let { return it }
        }

        // For companion objects, look up the containing class instead.
        // Companion objects are compiled into the same file as their containing class.
        if (irClass.isCompanion) {
            val containingClass = irClass.parent as? IrClass
            if (containingClass != null) {
                return determineBrsFileNameForClass(containingClass)
            }
        }

        // Find the containing file
        var parent: org.jetbrains.kotlin.ir.declarations.IrDeclarationParent = irClass.parent
        while (parent !is IrFile && parent is org.jetbrains.kotlin.ir.declarations.IrDeclaration) {
            parent = (parent as org.jetbrains.kotlin.ir.declarations.IrDeclaration).parent
        }

        return when (parent) {
            is IrFile -> {
                // Top-level class - use source file name
                java.io.File(parent.path).nameWithoutExtension + "Kt.brs"
            }
            else -> {
                // Fallback to class name (for nested classes in same-module code)
                context.getBrsName(irClass) + "Kt.brs"
            }
        }
    }
}
