/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.brs.backend.ast

/**
 * Base interface for all BrightScript expressions.
 */
sealed interface BrsExpression : BrsNode {
    override fun deepCopy(): BrsExpression
}

// ==================== Literals ====================

/**
 * An integer literal.
 *
 * ```brightscript
 * 42
 * ```
 */
class BrsIntLiteral(
    var value: Int
) : BrsNodeBase(), BrsExpression {

    override fun <R, D> accept(visitor: BrsVisitor<R, D>, data: D): R = visitor.visitIntLiteral(this, data)
    override fun <R, D> acceptChildren(visitor: BrsVisitor<R, D>, data: D) {}
    override fun deepCopy(): BrsIntLiteral = BrsIntLiteral(value).also { it.source = source }
}

/**
 * A long integer literal (64-bit).
 *
 * ```brightscript
 * 9223372036854775807&
 * ```
 */
class BrsLongIntLiteral(
    var value: Long
) : BrsNodeBase(), BrsExpression {

    override fun <R, D> accept(visitor: BrsVisitor<R, D>, data: D): R = visitor.visitLongIntLiteral(this, data)
    override fun <R, D> acceptChildren(visitor: BrsVisitor<R, D>, data: D) {}
    override fun deepCopy(): BrsLongIntLiteral = BrsLongIntLiteral(value).also { it.source = source }
}

/**
 * A float literal (32-bit).
 *
 * ```brightscript
 * 3.14!
 * ```
 */
class BrsFloatLiteral(
    var value: Float
) : BrsNodeBase(), BrsExpression {

    override fun <R, D> accept(visitor: BrsVisitor<R, D>, data: D): R = visitor.visitFloatLiteral(this, data)
    override fun <R, D> acceptChildren(visitor: BrsVisitor<R, D>, data: D) {}
    override fun deepCopy(): BrsFloatLiteral = BrsFloatLiteral(value).also { it.source = source }
}

/**
 * A double literal (64-bit).
 *
 * ```brightscript
 * 3.14159265358979#
 * ```
 */
class BrsDoubleLiteral(
    var value: Double
) : BrsNodeBase(), BrsExpression {

    override fun <R, D> accept(visitor: BrsVisitor<R, D>, data: D): R = visitor.visitDoubleLiteral(this, data)
    override fun <R, D> acceptChildren(visitor: BrsVisitor<R, D>, data: D) {}
    override fun deepCopy(): BrsDoubleLiteral = BrsDoubleLiteral(value).also { it.source = source }
}

/**
 * A string literal.
 *
 * ```brightscript
 * "Hello, World!"
 * ```
 */
class BrsStringLiteral(
    var value: String
) : BrsNodeBase(), BrsExpression {

    override fun <R, D> accept(visitor: BrsVisitor<R, D>, data: D): R = visitor.visitStringLiteral(this, data)
    override fun <R, D> acceptChildren(visitor: BrsVisitor<R, D>, data: D) {}
    override fun deepCopy(): BrsStringLiteral = BrsStringLiteral(value).also { it.source = source }
}

/**
 * A boolean literal.
 *
 * ```brightscript
 * true
 * false
 * ```
 */
class BrsBooleanLiteral(
    var value: Boolean
) : BrsNodeBase(), BrsExpression {

    override fun <R, D> accept(visitor: BrsVisitor<R, D>, data: D): R = visitor.visitBooleanLiteral(this, data)
    override fun <R, D> acceptChildren(visitor: BrsVisitor<R, D>, data: D) {}
    override fun deepCopy(): BrsBooleanLiteral = BrsBooleanLiteral(value).also { it.source = source }
}

/**
 * The invalid literal (BrightScript's null).
 *
 * ```brightscript
 * invalid
 * ```
 */
class BrsInvalidLiteral : BrsNodeBase(), BrsExpression {

    override fun <R, D> accept(visitor: BrsVisitor<R, D>, data: D): R = visitor.visitInvalidLiteral(this, data)
    override fun <R, D> acceptChildren(visitor: BrsVisitor<R, D>, data: D) {}
    override fun deepCopy(): BrsInvalidLiteral = BrsInvalidLiteral().also { it.source = source }
}

// ==================== Identifiers and References ====================

/**
 * An identifier reference.
 *
 * ```brightscript
 * myVariable
 * ```
 */
class BrsIdentifier(
    var name: String
) : BrsNodeBase(), BrsExpression {

    override fun <R, D> accept(visitor: BrsVisitor<R, D>, data: D): R = visitor.visitIdentifier(this, data)
    override fun <R, D> acceptChildren(visitor: BrsVisitor<R, D>, data: D) {}
    override fun deepCopy(): BrsIdentifier = BrsIdentifier(name).also { it.source = source }
}

/**
 * Reference to the implicit `m` variable (this pointer in BrightScript).
 *
 * ```brightscript
 * m
 * ```
 */
class BrsMRef : BrsNodeBase(), BrsExpression {

    override fun <R, D> accept(visitor: BrsVisitor<R, D>, data: D): R = visitor.visitMRef(this, data)
    override fun <R, D> acceptChildren(visitor: BrsVisitor<R, D>, data: D) {}
    override fun deepCopy(): BrsMRef = BrsMRef().also { it.source = source }
}

// ==================== Operators ====================

/**
 * BrightScript binary operators.
 */
enum class BrsBinaryOperator(val symbol: String) {
    // Arithmetic
    ADD("+"),
    SUB("-"),
    MUL("*"),
    DIV("/"),
    INT_DIV("\\"),
    MOD("mod"),
    POW("^"),

    // Comparison
    EQ("="),
    NE("<>"),
    LT("<"),
    LE("<="),
    GT(">"),
    GE(">="),

    // Logical
    AND("and"),
    OR("or"),

    // Bitwise (same keywords as logical, context-dependent)
    BIT_AND("and"),
    BIT_OR("or"),

    // String concatenation
    CONCAT("+"),

    // Shift
    LEFT_SHIFT("<<"),
    RIGHT_SHIFT(">>")
}

/**
 * A binary operation expression.
 *
 * ```brightscript
 * a + b
 * x and y
 * ```
 */
class BrsBinaryOp(
    var left: BrsExpression,
    var operator: BrsBinaryOperator,
    var right: BrsExpression
) : BrsNodeBase(), BrsExpression {

    override fun <R, D> accept(visitor: BrsVisitor<R, D>, data: D): R = visitor.visitBinaryOp(this, data)

    override fun <R, D> acceptChildren(visitor: BrsVisitor<R, D>, data: D) {
        left.accept(visitor, data)
        right.accept(visitor, data)
    }

    override fun deepCopy(): BrsBinaryOp = BrsBinaryOp(left.deepCopy(), operator, right.deepCopy()).also { it.source = source }
}

/**
 * BrightScript unary operators.
 */
enum class BrsUnaryOperator(val symbol: String) {
    NEG("-"),
    POS("+"),
    NOT("not")
}

/**
 * A unary operation expression.
 *
 * ```brightscript
 * -value
 * not condition
 * ```
 */
class BrsUnaryOp(
    var operator: BrsUnaryOperator,
    var operand: BrsExpression
) : BrsNodeBase(), BrsExpression {

    override fun <R, D> accept(visitor: BrsVisitor<R, D>, data: D): R = visitor.visitUnaryOp(this, data)

    override fun <R, D> acceptChildren(visitor: BrsVisitor<R, D>, data: D) {
        operand.accept(visitor, data)
    }

    override fun deepCopy(): BrsUnaryOp = BrsUnaryOp(operator, operand.deepCopy()).also { it.source = source }
}

// ==================== Function Calls ====================

/**
 * A function call expression.
 *
 * ```brightscript
 * myFunction(arg1, arg2)
 * ```
 */
class BrsFunctionCall(
    var target: BrsExpression,
    val arguments: MutableList<BrsExpression> = mutableListOf()
) : BrsNodeBase(), BrsExpression {

    override fun <R, D> accept(visitor: BrsVisitor<R, D>, data: D): R = visitor.visitFunctionCall(this, data)

    override fun <R, D> acceptChildren(visitor: BrsVisitor<R, D>, data: D) {
        target.accept(visitor, data)
        arguments.forEach { it.accept(visitor, data) }
    }

    override fun deepCopy(): BrsFunctionCall = BrsFunctionCall(
        target.deepCopy(),
        arguments.map { it.deepCopy() }.toMutableList()
    ).also { it.source = source }
}

/**
 * A method call expression (dot notation).
 *
 * ```brightscript
 * obj.method(arg1, arg2)
 * ```
 */
class BrsMethodCall(
    var receiver: BrsExpression,
    var method: String,
    val arguments: MutableList<BrsExpression> = mutableListOf()
) : BrsNodeBase(), BrsExpression {

    override fun <R, D> accept(visitor: BrsVisitor<R, D>, data: D): R = visitor.visitMethodCall(this, data)

    override fun <R, D> acceptChildren(visitor: BrsVisitor<R, D>, data: D) {
        receiver.accept(visitor, data)
        arguments.forEach { it.accept(visitor, data) }
    }

    override fun deepCopy(): BrsMethodCall = BrsMethodCall(
        receiver.deepCopy(),
        method,
        arguments.map { it.deepCopy() }.toMutableList()
    ).also { it.source = source }
}

// ==================== Access Expressions ====================

/**
 * An index access expression (array or AA bracket notation).
 *
 * ```brightscript
 * arr[0]
 * aa["key"]
 * ```
 */
class BrsIndexAccess(
    var target: BrsExpression,
    var index: BrsExpression
) : BrsNodeBase(), BrsExpression {

    override fun <R, D> accept(visitor: BrsVisitor<R, D>, data: D): R = visitor.visitIndexAccess(this, data)

    override fun <R, D> acceptChildren(visitor: BrsVisitor<R, D>, data: D) {
        target.accept(visitor, data)
        index.accept(visitor, data)
    }

    override fun deepCopy(): BrsIndexAccess = BrsIndexAccess(target.deepCopy(), index.deepCopy()).also { it.source = source }
}

/**
 * A dot access expression (property access).
 *
 * ```brightscript
 * obj.property
 * ```
 */
class BrsDotAccess(
    var target: BrsExpression,
    var field: String
) : BrsNodeBase(), BrsExpression {

    override fun <R, D> accept(visitor: BrsVisitor<R, D>, data: D): R = visitor.visitDotAccess(this, data)

    override fun <R, D> acceptChildren(visitor: BrsVisitor<R, D>, data: D) {
        target.accept(visitor, data)
    }

    override fun deepCopy(): BrsDotAccess = BrsDotAccess(target.deepCopy(), field).also { it.source = source }
}

// ==================== Collection Literals ====================

/**
 * An array literal.
 *
 * ```brightscript
 * [1, 2, 3]
 * ```
 */
class BrsArrayLiteral(
    val elements: MutableList<BrsExpression> = mutableListOf()
) : BrsNodeBase(), BrsExpression {

    override fun <R, D> accept(visitor: BrsVisitor<R, D>, data: D): R = visitor.visitArrayLiteral(this, data)

    override fun <R, D> acceptChildren(visitor: BrsVisitor<R, D>, data: D) {
        elements.forEach { it.accept(visitor, data) }
    }

    override fun deepCopy(): BrsArrayLiteral = BrsArrayLiteral(elements.map { it.deepCopy() }.toMutableList()).also { it.source = source }
}

/**
 * An entry in an associative array literal.
 */
data class BrsAAEntry(
    val key: String,
    val value: BrsExpression
)

/**
 * An associative array literal.
 *
 * ```brightscript
 * { key1: "value1", key2: 42 }
 * ```
 */
class BrsAALiteral(
    val entries: MutableList<BrsAAEntry> = mutableListOf()
) : BrsNodeBase(), BrsExpression {

    override fun <R, D> accept(visitor: BrsVisitor<R, D>, data: D): R = visitor.visitAALiteral(this, data)

    override fun <R, D> acceptChildren(visitor: BrsVisitor<R, D>, data: D) {
        entries.forEach { it.value.accept(visitor, data) }
    }

    override fun deepCopy(): BrsAALiteral = BrsAALiteral(
        entries.map { BrsAAEntry(it.key, it.value.deepCopy()) }.toMutableList()
    ).also { it.source = source }
}

// ==================== Special Expressions ====================

/**
 * A conditional (ternary-like) expression using if/then/else inline.
 *
 * BrightScript doesn't have a ternary operator, but we can generate
 * inline if expressions in some contexts.
 */
class BrsConditional(
    var condition: BrsExpression,
    var thenExpr: BrsExpression,
    var elseExpr: BrsExpression
) : BrsNodeBase(), BrsExpression {

    override fun <R, D> accept(visitor: BrsVisitor<R, D>, data: D): R = visitor.visitConditional(this, data)

    override fun <R, D> acceptChildren(visitor: BrsVisitor<R, D>, data: D) {
        condition.accept(visitor, data)
        thenExpr.accept(visitor, data)
        elseExpr.accept(visitor, data)
    }

    override fun deepCopy(): BrsConditional = BrsConditional(
        condition.deepCopy(),
        thenExpr.deepCopy(),
        elseExpr.deepCopy()
    ).also { it.source = source }
}

/**
 * A Type() function call for type checking.
 *
 * ```brightscript
 * Type(value)
 * ```
 */
class BrsTypeOf(
    var value: BrsExpression
) : BrsNodeBase(), BrsExpression {

    override fun <R, D> accept(visitor: BrsVisitor<R, D>, data: D): R = visitor.visitTypeOf(this, data)

    override fun <R, D> acceptChildren(visitor: BrsVisitor<R, D>, data: D) {
        value.accept(visitor, data)
    }

    override fun deepCopy(): BrsTypeOf = BrsTypeOf(value.deepCopy()).also { it.source = source }
}

/**
 * A CreateObject() function call for component instantiation.
 *
 * ```brightscript
 * CreateObject("roArray", 0, true)
 * ```
 */
class BrsCreateObject(
    var objectType: String,
    val arguments: MutableList<BrsExpression> = mutableListOf()
) : BrsNodeBase(), BrsExpression {

    override fun <R, D> accept(visitor: BrsVisitor<R, D>, data: D): R = visitor.visitCreateObject(this, data)

    override fun <R, D> acceptChildren(visitor: BrsVisitor<R, D>, data: D) {
        arguments.forEach { it.accept(visitor, data) }
    }

    override fun deepCopy(): BrsCreateObject = BrsCreateObject(
        objectType,
        arguments.map { it.deepCopy() }.toMutableList()
    ).also { it.source = source }
}

/**
 * An anonymous function expression.
 *
 * ```brightscript
 * function(x, y)
 *     return x + y
 * end function
 * ```
 */
class BrsAnonymousFunction(
    val parameters: MutableList<BrsParameter> = mutableListOf(),
    var returnType: BrsType? = null,
    var body: BrsBlock
) : BrsNodeBase(), BrsExpression {

    override fun <R, D> accept(visitor: BrsVisitor<R, D>, data: D): R = visitor.visitAnonymousFunction(this, data)

    override fun <R, D> acceptChildren(visitor: BrsVisitor<R, D>, data: D) {
        parameters.forEach { it.accept(visitor, data) }
        body.accept(visitor, data)
    }

    override fun deepCopy(): BrsAnonymousFunction = BrsAnonymousFunction(
        parameters.map { it.deepCopy() }.toMutableList(),
        returnType,
        body.deepCopy()
    ).also { it.source = source }
}
