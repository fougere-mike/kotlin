/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.brs.backend.ast

/**
 * Source location information for BrightScript AST nodes.
 */
data class BrsLocation(
    val file: String,
    val startLine: Int,
    val startColumn: Int,
    val endLine: Int = startLine,
    val endColumn: Int = startColumn
)

/**
 * Base interface for all BrightScript AST nodes.
 */
sealed interface BrsNode {
    /**
     * Source location of this node (for source maps and error reporting).
     */
    var source: BrsLocation?

    /**
     * Accept a visitor.
     */
    fun <R, D> accept(visitor: BrsVisitor<R, D>, data: D): R

    /**
     * Accept a visitor for all children.
     */
    fun <R, D> acceptChildren(visitor: BrsVisitor<R, D>, data: D)

    /**
     * Create a deep copy of this node.
     */
    fun deepCopy(): BrsNode
}

/**
 * Base class providing common functionality for AST nodes.
 */
abstract class BrsNodeBase : BrsNode {
    override var source: BrsLocation? = null
}

/**
 * Base visitor for BrightScript AST nodes.
 *
 * @param R the return type of visit methods
 * @param D the data type passed to visit methods
 */
abstract class BrsVisitor<R, D> {
    abstract fun visitNode(node: BrsNode, data: D): R

    // Statements
    open fun visitStatement(statement: BrsStatement, data: D): R = visitNode(statement, data)
    open fun visitBlock(block: BrsBlock, data: D): R = visitStatement(block, data)
    open fun visitIf(ifStatement: BrsIf, data: D): R = visitStatement(ifStatement, data)
    open fun visitWhile(whileStatement: BrsWhile, data: D): R = visitStatement(whileStatement, data)
    open fun visitFor(forStatement: BrsFor, data: D): R = visitStatement(forStatement, data)
    open fun visitForEach(forEachStatement: BrsForEach, data: D): R = visitStatement(forEachStatement, data)
    open fun visitReturn(returnStatement: BrsReturn, data: D): R = visitStatement(returnStatement, data)
    open fun visitExpressionStatement(statement: BrsExpressionStatement, data: D): R = visitStatement(statement, data)
    open fun visitPrint(printStatement: BrsPrint, data: D): R = visitStatement(printStatement, data)
    open fun visitTry(tryStatement: BrsTry, data: D): R = visitStatement(tryStatement, data)
    open fun visitThrow(throwStatement: BrsThrow, data: D): R = visitStatement(throwStatement, data)
    open fun visitExit(exitStatement: BrsExit, data: D): R = visitStatement(exitStatement, data)
    open fun visitContinue(continueStatement: BrsContinue, data: D): R = visitStatement(continueStatement, data)
    open fun visitStop(stopStatement: BrsStop, data: D): R = visitStatement(stopStatement, data)
    open fun visitEmpty(emptyStatement: BrsEmpty, data: D): R = visitStatement(emptyStatement, data)

    // Expressions
    open fun visitExpression(expression: BrsExpression, data: D): R = visitNode(expression, data)
    open fun visitIntLiteral(literal: BrsIntLiteral, data: D): R = visitExpression(literal, data)
    open fun visitLongIntLiteral(literal: BrsLongIntLiteral, data: D): R = visitExpression(literal, data)
    open fun visitFloatLiteral(literal: BrsFloatLiteral, data: D): R = visitExpression(literal, data)
    open fun visitDoubleLiteral(literal: BrsDoubleLiteral, data: D): R = visitExpression(literal, data)
    open fun visitStringLiteral(literal: BrsStringLiteral, data: D): R = visitExpression(literal, data)
    open fun visitBooleanLiteral(literal: BrsBooleanLiteral, data: D): R = visitExpression(literal, data)
    open fun visitInvalidLiteral(literal: BrsInvalidLiteral, data: D): R = visitExpression(literal, data)
    open fun visitIdentifier(identifier: BrsIdentifier, data: D): R = visitExpression(identifier, data)
    open fun visitBinaryOp(binaryOp: BrsBinaryOp, data: D): R = visitExpression(binaryOp, data)
    open fun visitUnaryOp(unaryOp: BrsUnaryOp, data: D): R = visitExpression(unaryOp, data)
    open fun visitFunctionCall(functionCall: BrsFunctionCall, data: D): R = visitExpression(functionCall, data)
    open fun visitMethodCall(methodCall: BrsMethodCall, data: D): R = visitExpression(methodCall, data)
    open fun visitIndexAccess(indexAccess: BrsIndexAccess, data: D): R = visitExpression(indexAccess, data)
    open fun visitDotAccess(dotAccess: BrsDotAccess, data: D): R = visitExpression(dotAccess, data)
    open fun visitArrayLiteral(arrayLiteral: BrsArrayLiteral, data: D): R = visitExpression(arrayLiteral, data)
    open fun visitAALiteral(aaLiteral: BrsAALiteral, data: D): R = visitExpression(aaLiteral, data)
    open fun visitMRef(mRef: BrsMRef, data: D): R = visitExpression(mRef, data)
    open fun visitConditional(conditional: BrsConditional, data: D): R = visitExpression(conditional, data)
    open fun visitTypeOf(typeOf: BrsTypeOf, data: D): R = visitExpression(typeOf, data)
    open fun visitCreateObject(createObject: BrsCreateObject, data: D): R = visitExpression(createObject, data)
    open fun visitAnonymousFunction(func: BrsAnonymousFunction, data: D): R = visitExpression(func, data)

    // Declarations
    open fun visitDeclaration(declaration: BrsDeclaration, data: D): R = visitNode(declaration, data)
    open fun visitFunction(function: BrsFunction, data: D): R = visitDeclaration(function, data)
    open fun visitSub(sub: BrsSub, data: D): R = visitDeclaration(sub, data)
    open fun visitParameter(parameter: BrsParameter, data: D): R = visitNode(parameter, data)
    open fun visitVariable(variable: BrsVariable, data: D): R = visitStatement(variable, data)
    open fun visitField(field: BrsField, data: D): R = visitDeclaration(field, data)

    // Program structure
    open fun visitProgram(program: BrsProgram, data: D): R = visitNode(program, data)
    open fun visitComponent(component: BrsComponent, data: D): R = visitNode(component, data)

    // Comments
    open fun visitComment(comment: BrsComment, data: D): R = visitNode(comment, data)
}

/**
 * Void visitor that doesn't return anything.
 */
abstract class BrsVisitorVoid : BrsVisitor<Unit, Unit>() {
    override fun visitNode(node: BrsNode, data: Unit) {
        node.acceptChildren(this, data)
    }

    fun accept(node: BrsNode) {
        node.accept(this, Unit)
    }
}
