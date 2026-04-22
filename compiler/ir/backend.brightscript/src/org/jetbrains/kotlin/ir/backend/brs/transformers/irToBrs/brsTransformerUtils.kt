/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.transformers.irToBrs

import org.jetbrains.kotlin.brs.backend.ast.BrsInvalidLiteral
import org.jetbrains.kotlin.brs.backend.ast.BrsParameter
import org.jetbrains.kotlin.brs.backend.ast.BrsType
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.expressions.IrBreak
import org.jetbrains.kotlin.ir.expressions.IrContinue
import org.jetbrains.kotlin.ir.expressions.IrLoop
import org.jetbrains.kotlin.ir.types.IrType
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
