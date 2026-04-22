/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.transformers.irToBrs

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
import org.jetbrains.kotlin.ir.backend.brs.BrsIrBackendContext
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.expressions.IrBreak
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrContinue
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetEnumValue
import org.jetbrains.kotlin.ir.expressions.IrLoop
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.classifierOrNull
import org.jetbrains.kotlin.ir.types.isAny
import org.jetbrains.kotlin.ir.types.isChar
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.isTypeParameter
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import org.jetbrains.kotlin.ir.types.isBoolean
import org.jetbrains.kotlin.ir.types.isByte
import org.jetbrains.kotlin.ir.types.isDouble
import org.jetbrains.kotlin.ir.types.isFloat
import org.jetbrains.kotlin.ir.types.isInt
import org.jetbrains.kotlin.ir.types.isLong
import org.jetbrains.kotlin.ir.types.isNothing
import org.jetbrains.kotlin.ir.types.isShort
import org.jetbrains.kotlin.ir.types.isString
import org.jetbrains.kotlin.ir.types.isUnit
import org.jetbrains.kotlin.ir.util.isFunction
import org.jetbrains.kotlin.ir.util.isNullable

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
fun transformToString(receiverExpr: BrsExpression, receiverType: IrType): BrsExpression {
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
            BrsFunctionCall(
                BrsIdentifier(funcName),
                mutableListOf(receiverExpr)
            )
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
            generateRuntimeToString(receiverExpr)
        }

        // Dynamic type and external interfaces: use runtime type checking
        // These are native BrightScript types that don't have a toString() method
        isDynamicType(receiverType) || isExternalInterfaceType(receiverType) -> {
            generateRuntimeToString(receiverExpr)
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
fun generateRuntimeToString(valueExpr: BrsExpression): BrsExpression {
    // Call the stdlib toString function which handles all type checking
    // This avoids nested IIFEs that cause scope issues with global built-in functions
    return BrsFunctionCall(
        BrsIdentifier("toString_AnyN_k_"),
        mutableListOf(valueExpr)
    )
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
 */
fun normalizeParametersForBrs(parameters: List<BrsParameter>): List<BrsParameter> {
    var sawDefault = false
    return parameters.map { param ->
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
