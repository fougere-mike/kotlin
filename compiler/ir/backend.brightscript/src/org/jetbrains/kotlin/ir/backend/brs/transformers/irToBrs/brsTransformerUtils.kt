/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.transformers.irToBrs

import org.jetbrains.kotlin.brs.backend.ast.BrsArrayLiteral
import org.jetbrains.kotlin.brs.backend.ast.BrsConditional
import org.jetbrains.kotlin.brs.backend.ast.BrsExpression
import org.jetbrains.kotlin.brs.backend.ast.BrsFunctionCall
import org.jetbrains.kotlin.brs.backend.ast.BrsIdentifier
import org.jetbrains.kotlin.brs.backend.ast.BrsInvalidLiteral
import org.jetbrains.kotlin.brs.backend.ast.BrsMethodCall
import org.jetbrains.kotlin.brs.backend.ast.BrsParameter
import org.jetbrains.kotlin.brs.backend.ast.BrsStringLiteral
import org.jetbrains.kotlin.brs.backend.ast.BrsType
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.backend.brs.BrsIntrinsics
import org.jetbrains.kotlin.ir.backend.brs.BrsIrBackendContext
import org.jetbrains.kotlin.ir.backend.brs.lower.coroutines.BrsStatementOrigins
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.expressions.IrBreak
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrContinue
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetEnumValue
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrLoop
import org.jetbrains.kotlin.ir.expressions.IrSetField
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.IrTypeOperator
import org.jetbrains.kotlin.ir.expressions.IrTypeOperatorCall
import org.jetbrains.kotlin.ir.expressions.IrWhen
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.classifierOrNull
import org.jetbrains.kotlin.ir.types.isAny
import org.jetbrains.kotlin.ir.types.isBoolean
import org.jetbrains.kotlin.ir.types.isByte
import org.jetbrains.kotlin.ir.types.isChar
import org.jetbrains.kotlin.ir.types.isDouble
import org.jetbrains.kotlin.ir.types.isFloat
import org.jetbrains.kotlin.ir.types.isInt
import org.jetbrains.kotlin.ir.types.isLong
import org.jetbrains.kotlin.ir.types.isNothing
import org.jetbrains.kotlin.ir.types.isShort
import org.jetbrains.kotlin.ir.types.isString
import org.jetbrains.kotlin.ir.types.isUnit
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.isFunction
import org.jetbrains.kotlin.ir.util.isNullable
import org.jetbrains.kotlin.ir.util.isTypeParameter
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid

// BrightScript reserved keywords that cannot be used as identifiers
// Includes language keywords plus special identifiers like 'global' (m.global), 'm' (this), 'top' (m.top)
private val brsReservedKeywords = setOf(
    "and", "as", "boolean", "box", "catch", "class", "dim", "double", "dynamic",
    "each", "else", "elseif", "end", "endfor", "endif", "endsub", "endwhile",
    "exit", "extends", "false", "float", "for", "function", "global", "goto", "if", "in",
    "integer", "interface", "invalid", "let", "library", "line_num", "longinteger",
    "m", "mod", "next", "not", "object", "or", "override", "print", "private", "protected",
    "public", "rem", "return", "run", "step", "stop", "string", "sub", "then",
    "throw", "to", "top", "true", "try", "type", "while"
)

/**
 * Sanitize parameter names for BrightScript.
 * Kotlin IR uses special names like <set-?> for setter parameters,
 * <unused var> for underscore placeholders in lambdas, etc.
 * Also escapes BrightScript reserved keywords.
 */
fun sanitizeParameterName(name: String): String {
    val sanitized = when {
        // Setter parameter: <set-?> -> value
        name.startsWith("<set-") && name.endsWith(">") -> "value"
        // Unused parameter placeholder (from _ in lambdas): <unused var> -> _unused
        // Handle both with and without angle brackets
        name == "<unused var>" || name == "unused var" -> "_unused"
        // Receiver reference: <this> -> __this (avoid potential BrightScript 'm.this' issues)
        name == "<this>" -> "__this"
        // Any other special name: strip angle brackets and sanitize
        name.startsWith("<") && name.endsWith(">") ->
            name.removePrefix("<").removeSuffix(">")
                .replace("-", "_")
                .replace(" ", "_")
        // Names with spaces (shouldn't happen, but sanitize anyway)
        name.contains(" ") -> name.replace(" ", "_")
        else -> name
    }
    // Sanitize invalid BrightScript identifier characters ($ is not valid in BrightScript)
    val cleaned = sanitized.replace("$", "_")
    // Escape reserved keywords by adding underscore suffix
    return if (cleaned.lowercase() in brsReservedKeywords) {
        "${cleaned}_"
    } else {
        cleaned
    }
}

/**
 * The BrightScript key an [org.jetbrains.kotlin.ir.declarations.IrField] is
 * emitted under (visitGetField/visitSetField, class-initializer emission):
 * IR-special names (`<this>`, `<iterator>`, …) are de-bracketed, and the `$`
 * LocalDeclarationsLowering puts on capture fields becomes `_`.
 *
 * ONE rule, shared: BrsSuspendFunctionsLowering checks coroutine-class field
 * names against the CoroutineImpl base's fields with this same function
 * (renameFieldsShadowingCoroutineBase) — a copy of the rule that drifted
 * would silently reopen the `i.state = state` clobber.
 */
fun sanitizeFieldName(rawName: String): String = when {
    rawName == "<this>" -> "__this"
    rawName.startsWith("<") && rawName.endsWith(">") ->
        rawName.removePrefix("<").removeSuffix(">").replace("-", "_").replace(" ", "_")
    else -> rawName.replace("$", "_")
}

/**
 * Sanitize property accessor names for BrightScript.
 * Kotlin IR uses names like <get-foo> and <set-foo> for property accessors.
 * We use "__get_" and "__set_" prefixes (double underscore) to clearly distinguish
 * compiler-generated property accessors from user-defined functions, following
 * common conventions for internal/generated names.
 */
fun sanitizeMethodName(name: String): String {
    return when {
        name.startsWith("<get-") && name.endsWith(">") ->
            "__get_" + name.removePrefix("<get-").removeSuffix(">")
        name.startsWith("<set-") && name.endsWith(">") ->
            "__set_" + name.removePrefix("<set-").removeSuffix(">")
        else -> name
    }
}

/**
 * Deduplicate parameter names by adding numeric suffixes.
 * This handles cases like multiple `_unused` parameters from lambdas with multiple _ placeholders.
 */
fun deduplicateParameterNames(parameters: List<BrsParameter>): List<BrsParameter> {
    val nameCounts = mutableMapOf<String, Int>()
    return parameters.map { param ->
        val count = nameCounts.getOrDefault(param.name, 0)
        nameCounts[param.name] = count + 1
        if (count > 0) {
            BrsParameter("${param.name}$count", param.type, param.defaultValue)
        } else {
            param
        }
    }
}

/**
 * Create a BrsFunctionCall and record the dependency with the given context.
 * This is the central place where all function calls are created, ensuring
 * that dependency tracking captures every function we emit.
 */
fun createFunctionCall(
    functionName: String,
    args: MutableList<BrsExpression>,
    context: BrsIrBackendContext
): BrsFunctionCall {
    context.recordFunctionDependency(functionName)
    return BrsFunctionCall(BrsIdentifier(functionName), args)
}

/**
 * Create a BrsFunctionCall with no arguments and record the dependency.
 */
fun createFunctionCall(functionName: String, context: BrsIrBackendContext): BrsFunctionCall {
    return createFunctionCall(functionName, mutableListOf(), context)
}

/**
 * Check whether a property on `ownerClass` is the SceneGraph Layout accessor property.
 *
 * Supports two patterns:
 * 1. Top-level class: `MainScreen_Layout` (new pattern)
 * 2. Nested class: `MainScreen.Layout` (legacy pattern, for backwards compatibility)
 */
fun isLayoutClassProperty(property: IrProperty, ownerClass: IrClass): Boolean {
    val propertyType = property.getter?.returnType ?: property.backingField?.type ?: return false
    val typeClass = propertyType.classOrNull?.owner ?: return false

    val ownerClassName = ownerClass.name.asString()
    val typeClassName = typeClass.name.asString()

    // New pattern: top-level class named OwnerClassName_Layout
    if (typeClassName == "${ownerClassName}_Layout" && typeClass.parent !is IrClass) {
        return true
    }

    // Legacy pattern: nested class named "Layout" within the owner class
    if (typeClassName == "Layout" && typeClass.parent == ownerClass) {
        return true
    }

    return false
}

/**
 * Checks if the given IR element contains any IrContinue statements targeting the specified loop.
 * Used to determine if a loop needs to be wrapped with a continue-simulation pattern
 * when native continue is not supported.
 */
fun containsContinueFor(element: IrElement?, targetLoop: IrLoop): Boolean {
    if (element == null) return false
    var found = false
    element.acceptVoid(object : IrVisitorVoid() {
        override fun visitElement(element: IrElement) {
            if (!found) element.acceptChildrenVoid(this)
        }
        override fun visitContinue(jump: IrContinue) {
            if (jump.loop === targetLoop) {
                found = true
            }
        }
    })
    return found
}

/**
 * Checks if the given IR element contains any IrBreak statements targeting the specified loop.
 * Used to determine if we need a break flag variable when simulating continue.
 */
fun containsBreakFor(element: IrElement?, targetLoop: IrLoop): Boolean {
    if (element == null) return false
    var found = false
    element.acceptVoid(object : IrVisitorVoid() {
        override fun visitElement(element: IrElement) {
            if (!found) element.acceptChildrenVoid(this)
        }
        override fun visitBreak(jump: IrBreak) {
            if (jump.loop === targetLoop) {
                found = true
            }
        }
    })
    return found
}

/**
 * Map a Kotlin IR type to a BrightScript type.
 */
fun mapTypeToBrs(type: IrType): BrsType? {
    return when {
        type.isInt() || type.isShort() || type.isByte() -> BrsType.INTEGER
        type.isLong() -> BrsType.LONG_INTEGER
        type.isFloat() -> BrsType.FLOAT
        type.isDouble() -> BrsType.DOUBLE
        type.isBoolean() -> BrsType.BOOLEAN
        type.isString() -> BrsType.STRING
        type.isUnit() -> BrsType.VOID
        // Nothing maps to Dynamic for parameters (Void only valid for return types)
        type.isNothing() -> BrsType.DYNAMIC
        type.isNullable() -> BrsType.DYNAMIC
        // Function types are implemented as closure objects (AA with invoke method), not as
        // BrightScript Function type. Map to Object to accept closure objects as arguments.
        type.isFunction() -> BrsType.OBJECT
        else -> BrsType.OBJECT
    }
}

/**
 * Check whether an IR expression is a compile-time constant (literal, enum value,
 * or a known enum-property access). Uses the backend context to resolve enum-constant
 * property maps for calls like `MyEnum.FOO.someProp`.
 */
fun isConstantExpression(expression: IrExpression, context: BrsIrBackendContext): Boolean {
    return when (expression) {
        is IrConst -> true
        is IrGetEnumValue -> true  // Enum constants are always statically known
        is IrCall -> {
            // Check for property access on constant enum value
            val receiver = expression.dispatchReceiver
            if (receiver is IrGetEnumValue) {
                val entry = receiver.symbol.owner
                val functionName = expression.symbol.owner.name.asString()
                when {
                    functionName == "<get-ordinal>" || functionName == "ordinal" -> true
                    functionName == "<get-name>" || functionName == "name" -> true
                    functionName.startsWith("<get-") -> {
                        val propName = functionName.removePrefix("<get-").removeSuffix(">")
                        context.getEnumConstantProperties(entry)?.containsKey(propName) == true
                    }
                    else -> false
                }
            } else {
                false
            }
        }
        else -> false
    }
}

/**
 * Transform a toString() call to appropriate BrightScript code.
 * BrightScript primitives don't have methods, so we need to handle each type specially.
 */
fun transformToString(receiverExpr: BrsExpression, receiverType: IrType, context: BrsIrBackendContext): BrsExpression {
    return when {
        // String: just return the string itself (pass-through)
        receiverType.isString() -> receiverExpr

        // Char: already a string in BrightScript, just pass through
        receiverType.isChar() -> receiverExpr

        // Numeric types: use __kotlin_numToStr() helper function
        // BrightScript's Str() adds a leading space for positive numbers
        // We use a helper function because anonymous functions can't call global built-ins
        // Note: Method names do NOT include return types (like Java) to support polymorphism
        receiverType.isInt() || receiverType.isShort() || receiverType.isByte() ||
        receiverType.isLong() || receiverType.isFloat() || receiverType.isDouble() -> {
            // Choose the appropriate overload based on type
            val funcName = when {
                receiverType.isInt() || receiverType.isShort() || receiverType.isByte() -> "__kotlin_numToStr_I_k_"
                receiverType.isLong() -> "__kotlin_numToStr_J_k_"
                receiverType.isFloat() -> "__kotlin_numToStr_F_k_"
                receiverType.isDouble() -> "__kotlin_numToStr_D_k_"
                else -> "__kotlin_numToStr_AnyN_k_"
            }
            createFunctionCall(funcName, mutableListOf(receiverExpr), context)
        }

        // Boolean: use conditional to return "true" or "false"
        receiverType.isBoolean() -> {
            BrsConditional(
                receiverExpr,
                BrsStringLiteral("true"),
                BrsStringLiteral("false")
            )
        }

        // Any?, Dynamic, nullable types, or type parameters: use runtime type checking
        // since BrightScript primitives don't have .toString() method.
        // Type parameters must use runtime checking because at runtime T could be
        // a primitive (Int, String, Boolean, etc.) which don't have .toString() methods.
        receiverType.isNullable() || receiverType.isAny() || receiverType.isTypeParameter() -> {
            generateRuntimeToString(receiverExpr, context)
        }

        // Dynamic type and external interfaces: use runtime type checking
        // These are native BrightScript types that don't have a toString() method
        isDynamicType(receiverType) || isExternalInterfaceType(receiverType) -> {
            generateRuntimeToString(receiverExpr, context)
        }

        // Non-nullable objects: call toString method
        else -> {
            BrsMethodCall(receiverExpr, "toString", mutableListOf())
        }
    }
}

/**
 * Generate runtime type-checking toString logic for Any? types.
 * This is used by brsIntrinsicToString when the type is not known at compile time.
 *
 * Instead of generating inline nested conditionals (which become IIFEs with scope issues),
 * we call the stdlib toString_AnyN_k_ function which handles all types properly.
 * Note: Method names do NOT include return types (like Java) to support polymorphism.
 */
fun generateRuntimeToString(valueExpr: BrsExpression, context: BrsIrBackendContext): BrsExpression {
    // Call the stdlib toString function which handles all type checking
    // This avoids nested IIFEs that cause scope issues with global built-in functions
    return createFunctionCall("toString_AnyN_k_", mutableListOf(valueExpr), context)
}

/**
 * Checks if a type is the Dynamic type (kotlin.brs.Dynamic).
 * Dynamic type needs runtime type checking for toString since it can hold any value.
 */
fun isDynamicType(type: IrType): Boolean {
    val classifier = type.classifierOrNull
    if (classifier !is IrClassSymbol) return false
    return classifier.owner.fqNameWhenAvailable?.asString() == "kotlin.brs.Dynamic"
}

/**
 * Checks if a type is an external interface (native BrightScript type).
 * External interfaces don't have Kotlin methods like toString().
 */
fun isExternalInterfaceType(type: IrType): Boolean {
    val irClass = type.classOrNull?.owner ?: return false
    return irClass.isExternal || isExternalClass(irClass)
}

/**
 * Checks if a class is marked with @BrsExternal annotation.
 */
fun isExternalClass(irClass: IrClass): Boolean {
    return irClass.annotations.any { annotation ->
        val annotationClass = annotation.type.classifierOrNull?.owner as? IrClass
        annotationClass?.name?.asString() == "BrsExternal"
    }
}

/**
 * Peel value-preserving type operators (implicit/explicit casts, not-null assertions)
 * off a receiver expression so receiver-identity checks see the underlying value.
 */
internal fun unwrapReceiverCasts(expression: IrExpression): IrExpression {
    var unwrapped: IrExpression = expression
    while (unwrapped is IrTypeOperatorCall &&
        (unwrapped.operator == IrTypeOperator.IMPLICIT_CAST ||
            unwrapped.operator == IrTypeOperator.CAST ||
            unwrapped.operator == IrTypeOperator.IMPLICIT_NOTNULL)
    ) {
        unwrapped = unwrapped.argument
    }
    return unwrapped
}

/**
 * Checks if a property is a SceneGraph interface field.
 *
 * Interface fields live on the NODE (accessed via `<node m>.top.fieldName`), not on
 * the component m-scope object. This includes properties annotated with any
 * @SG*Field annotation or @BrsField — the ONE set is [BrsIntrinsics.interfaceFieldAnnotationIds]
 * (shared with the extractor's field list and `BrsIntrinsics.constructorInputs`).
 */
internal fun hasInterfaceFieldAnnotation(property: IrProperty): Boolean =
    BrsIntrinsics.hasInterfaceFieldAnnotation(property)

/**
 * How an `IrSetField` whose field belongs to a SceneGraph component is emitted. ONE decision
 * for BOTH `visitSetField` sites — `IrExpressionToBrsTransformer` (a write in expression
 * position) and `IrStatementToBrsTransformer` (a write in statement position, including a
 * coroutine-field write whose VALUE is a hoisted block) each carry their own emission code,
 * and which one a given write reaches depends only on its position in the tree, so the two
 * must never disagree. (The first COMPONENT_INPUT_WRITE guard lived on only the expression
 * site: `val s = Screen(id)` in a plain body was right while `return Screen(id)`,
 * `held = Screen(id)`, `Outer(Inner())`, and a local live across a suspension all emitted
 * `n.top.field = v` — a silent input drop on device.)
 */
internal enum class ComponentFieldWriteRoute {
    /**
     * A constructor-input write from BrsComponentConstructorCallLowering: the receiver IS the
     * freshly created roSGNode handle → plain `receiver.field = v`.
     */
    INPUT_WRITE,

    /**
     * A component's OWN `@SG*Field`/`@BrsField` backing-field write (setter body, init): the
     * receiver is the m-scope object and the field lives on its node → `receiver.top.field = v`.
     */
    INTERFACE_FIELD,

    /** Not a component interface-field write — the ordinary field-assignment path. */
    NONE,
}

internal fun componentFieldWriteRoute(expression: IrSetField, context: BrsIrBackendContext): ComponentFieldWriteRoute {
    if (expression.origin == BrsStatementOrigins.COMPONENT_INPUT_WRITE) return ComponentFieldWriteRoute.INPUT_WRITE
    val field = expression.symbol.owner
    val parentClass = field.parent as? IrClass ?: return ComponentFieldWriteRoute.NONE
    val property = field.correspondingPropertySymbol?.owner ?: return ComponentFieldWriteRoute.NONE
    // Delegated properties are excluded — their backing field holds the delegate.
    if (property.isDelegated) return ComponentFieldWriteRoute.NONE
    if (!context.intrinsics.isSceneGraphComponent(parentClass)) return ComponentFieldWriteRoute.NONE
    if (!hasInterfaceFieldAnnotation(property)) return ComponentFieldWriteRoute.NONE
    return ComponentFieldWriteRoute.INTERFACE_FIELD
}

/**
 * True when [expression] can be evaluated unconditionally with no side effects
 * and no possibility of a runtime crash — the requirement for keeping a Kotlin
 * `&&`/`||` operand inside a bare BrightScript `and`/`or`, which evaluates BOTH
 * operands. Anything not on this whitelist (calls, member/indexed access,
 * safe-calls) gets the hoisted guarded-temp form instead.
 */
internal fun isEffectFreeShortCircuitOperand(expression: IrExpression): Boolean {
    return when (expression) {
        is IrConst -> true
        is IrGetValue -> true
        is IrCall -> {
            // Extension-receiver calls are user code — never effect-free.
            if (expression.extensionReceiver != null) return false
            // Prefix `!` reaches the backend as a Boolean.not() call with a null
            // origin (not EXCL) — the same recognition the transformer uses to
            // emit the prefix `not` operator. Requiring a Boolean dispatch
            // receiver excludes user-defined `not` operators.
            val notReceiver = expression.dispatchReceiver
            if (expression.symbol.owner.name.asString() == "not" &&
                expression.valueArgumentsCount == 0 &&
                notReceiver != null && notReceiver.type.isBoolean()
            ) {
                return isEffectFreeShortCircuitOperand(notReceiver)
            }
            val isEquality = expression.origin == IrStatementOrigin.EQEQ ||
                expression.origin == IrStatementOrigin.EXCLEQ
            val isOrdering = when (expression.origin) {
                IrStatementOrigin.LT, IrStatementOrigin.GT,
                IrStatementOrigin.LTEQ, IrStatementOrigin.GTEQ -> true
                else -> false
            }
            if (!isEquality && !isOrdering) return false
            // Comparison origins also cover user-defined compareTo/equals
            // operator calls, which run user code. Only accept a comparison the
            // emitter compiles to a NATIVE operator: a null comparison (invalid
            // compares safely with = / <>) or operands that are primitive for
            // comparison — mirroring transformOperator's needsStructuralEquals
            // / needsCompareTo tests.
            val operands = mutableListOf<IrExpression>()
            expression.dispatchReceiver?.let { operands.add(it) }
            for (i in 0 until expression.valueArgumentsCount) {
                operands.add(expression.getValueArgument(i) ?: return false)
            }
            if (operands.size != 2) return false
            val (left, right) = operands
            val isNullComparison = (left is IrConst && left.value == null) ||
                (right is IrConst && right.value == null)
            val nativeOperands = left.type.isPrimitiveForComparison() &&
                right.type.isPrimitiveForComparison()
            if (!(isEquality && isNullComparison) && !nativeOperands) return false
            isEffectFreeShortCircuitOperand(left) && isEffectFreeShortCircuitOperand(right)
        }
        is IrWhen -> {
            val sc = expression.origin == IrStatementOrigin.ANDAND ||
                expression.origin == IrStatementOrigin.OROR
            sc && expression.branches.all {
                isEffectFreeShortCircuitOperand(it.condition) &&
                    isEffectFreeShortCircuitOperand(it.result)
            }
        }
        else -> false
    }
}

/**
 * Placeholder for a value argument that is absent at the call site.
 * Default-valued parameters are filled callee-side (the callee guards on
 * invalid), so invalid preserves the position. A missing VARARG has no
 * callee-side default — the absent argument IS the empty array, and
 * callees index/dot into it immediately (e.g. toList_rArr_k_), so it
 * must materialize as [] at the call site.
 */
fun absentArgumentPlaceholder(function: IrFunction, index: Int): BrsExpression =
    if (function.valueParameters.getOrNull(index)?.varargElementType != null)
        BrsArrayLiteral(mutableListOf())
    else
        BrsInvalidLiteral()

/**
 * Ensure parameters satisfy BrightScript's constraint:
 * Once a parameter has a default value, all subsequent parameters must also have defaults.
 *
 * In BrightScript, this is invalid:
 *   function foo(a = 1, b as String)  ' Error: b has no default after a has default
 *
 * This is valid:
 *   function foo(a = 1, b = invalid)  ' OK: both have defaults
 *
 * This function adds `= invalid` to any parameter without a default that follows
 * a parameter with a default.
 *
 * It also rewrites a Void parameter TYPE to Dynamic: "as Void" is legal in
 * return position only — a Void parameter is a device-side compile error
 * ("Type is Invalid", &ha7). A Unit-typed value parameter (a generic
 * instantiated at Unit, e.g. Flow<Unit>.collect { }) maps to VOID in
 * mapTypeToBrs and must be legalized here (pinned by the unitParameterType
 * golden; found by collectLatest's empty collect lambda).
 */
fun normalizeParametersForBrs(parameters: List<BrsParameter>): List<BrsParameter> {
    var sawDefault = false
    return parameters.map { rawParam ->
        val param = if (rawParam.type == BrsType.VOID) {
            BrsParameter(rawParam.name, BrsType.DYNAMIC, rawParam.defaultValue)
        } else {
            rawParam
        }
        if (param.defaultValue != null) {
            sawDefault = true
            param
        } else if (sawDefault) {
            // Parameter after a default - must add default value
            BrsParameter(param.name, param.type, BrsInvalidLiteral())
        } else {
            param
        }
    }
}
