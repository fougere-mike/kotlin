/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.brs.backend.ast

/**
 * Base interface for all BrightScript statements.
 */
sealed interface BrsStatement : BrsNode {
    override fun deepCopy(): BrsStatement
}

/**
 * A block of statements.
 *
 * BrightScript doesn't have explicit block syntax, but we use this
 * internally to group statements.
 */
class BrsBlock(
    val statements: MutableList<BrsStatement> = mutableListOf()
) : BrsNodeBase(), BrsStatement {

    override fun <R, D> accept(visitor: BrsVisitor<R, D>, data: D): R = visitor.visitBlock(this, data)

    override fun <R, D> acceptChildren(visitor: BrsVisitor<R, D>, data: D) {
        statements.forEach { it.accept(visitor, data) }
    }

    override fun deepCopy(): BrsBlock = BrsBlock(statements.map { it.deepCopy() }.toMutableList()).also { it.source = source }
}

/**
 * An if/else if/else statement.
 *
 * ```brightscript
 * if condition then
 *     ' statements
 * else if condition2 then
 *     ' statements
 * else
 *     ' statements
 * end if
 * ```
 */
class BrsIf(
    var condition: BrsExpression,
    var thenBranch: BrsStatement,
    var elseBranch: BrsStatement? = null
) : BrsNodeBase(), BrsStatement {

    override fun <R, D> accept(visitor: BrsVisitor<R, D>, data: D): R = visitor.visitIf(this, data)

    override fun <R, D> acceptChildren(visitor: BrsVisitor<R, D>, data: D) {
        condition.accept(visitor, data)
        thenBranch.accept(visitor, data)
        elseBranch?.accept(visitor, data)
    }

    override fun deepCopy(): BrsIf = BrsIf(
        condition.deepCopy(),
        thenBranch.deepCopy(),
        elseBranch?.deepCopy()
    ).also { it.source = source }
}

/**
 * A while loop.
 *
 * ```brightscript
 * while condition
 *     ' statements
 * end while
 * ```
 */
class BrsWhile(
    var condition: BrsExpression,
    var body: BrsStatement
) : BrsNodeBase(), BrsStatement {

    override fun <R, D> accept(visitor: BrsVisitor<R, D>, data: D): R = visitor.visitWhile(this, data)

    override fun <R, D> acceptChildren(visitor: BrsVisitor<R, D>, data: D) {
        condition.accept(visitor, data)
        body.accept(visitor, data)
    }

    override fun deepCopy(): BrsWhile = BrsWhile(condition.deepCopy(), body.deepCopy()).also { it.source = source }
}

/**
 * A for loop with numeric range.
 *
 * ```brightscript
 * for i = start to end step stepValue
 *     ' statements
 * end for
 * ```
 */
class BrsFor(
    var variable: String,
    var start: BrsExpression,
    var end: BrsExpression,
    var step: BrsExpression? = null,
    var body: BrsStatement
) : BrsNodeBase(), BrsStatement {

    override fun <R, D> accept(visitor: BrsVisitor<R, D>, data: D): R = visitor.visitFor(this, data)

    override fun <R, D> acceptChildren(visitor: BrsVisitor<R, D>, data: D) {
        start.accept(visitor, data)
        end.accept(visitor, data)
        step?.accept(visitor, data)
        body.accept(visitor, data)
    }

    override fun deepCopy(): BrsFor = BrsFor(
        variable,
        start.deepCopy(),
        end.deepCopy(),
        step?.deepCopy(),
        body.deepCopy()
    ).also { it.source = source }
}

/**
 * A for-each loop over an iterable.
 *
 * ```brightscript
 * for each item in collection
 *     ' statements
 * end for
 * ```
 */
class BrsForEach(
    var variable: String,
    var iterable: BrsExpression,
    var body: BrsStatement
) : BrsNodeBase(), BrsStatement {

    override fun <R, D> accept(visitor: BrsVisitor<R, D>, data: D): R = visitor.visitForEach(this, data)

    override fun <R, D> acceptChildren(visitor: BrsVisitor<R, D>, data: D) {
        iterable.accept(visitor, data)
        body.accept(visitor, data)
    }

    override fun deepCopy(): BrsForEach = BrsForEach(variable, iterable.deepCopy(), body.deepCopy()).also { it.source = source }
}

/**
 * A return statement.
 *
 * ```brightscript
 * return value
 * ```
 */
class BrsReturn(
    var value: BrsExpression? = null
) : BrsNodeBase(), BrsStatement {

    override fun <R, D> accept(visitor: BrsVisitor<R, D>, data: D): R = visitor.visitReturn(this, data)

    override fun <R, D> acceptChildren(visitor: BrsVisitor<R, D>, data: D) {
        value?.accept(visitor, data)
    }

    override fun deepCopy(): BrsReturn = BrsReturn(value?.deepCopy()).also { it.source = source }
}

/**
 * An expression used as a statement.
 */
class BrsExpressionStatement(
    var expression: BrsExpression
) : BrsNodeBase(), BrsStatement {

    override fun <R, D> accept(visitor: BrsVisitor<R, D>, data: D): R = visitor.visitExpressionStatement(this, data)

    override fun <R, D> acceptChildren(visitor: BrsVisitor<R, D>, data: D) {
        expression.accept(visitor, data)
    }

    override fun deepCopy(): BrsExpressionStatement = BrsExpressionStatement(expression.deepCopy()).also { it.source = source }
}

/**
 * A print statement.
 *
 * ```brightscript
 * print "Hello"; value; "World"
 * ```
 */
class BrsPrint(
    val expressions: MutableList<BrsExpression> = mutableListOf()
) : BrsNodeBase(), BrsStatement {

    override fun <R, D> accept(visitor: BrsVisitor<R, D>, data: D): R = visitor.visitPrint(this, data)

    override fun <R, D> acceptChildren(visitor: BrsVisitor<R, D>, data: D) {
        expressions.forEach { it.accept(visitor, data) }
    }

    override fun deepCopy(): BrsPrint = BrsPrint(expressions.map { it.deepCopy() }.toMutableList()).also { it.source = source }
}

/**
 * A try/catch statement (Roku OS 9.4+).
 *
 * ```brightscript
 * try
 *     ' statements
 * catch e
 *     ' error handling
 * end try
 * ```
 */
class BrsTry(
    var tryBlock: BrsStatement,
    var catchVariable: String? = null,
    var catchBlock: BrsStatement? = null
) : BrsNodeBase(), BrsStatement {

    override fun <R, D> accept(visitor: BrsVisitor<R, D>, data: D): R = visitor.visitTry(this, data)

    override fun <R, D> acceptChildren(visitor: BrsVisitor<R, D>, data: D) {
        tryBlock.accept(visitor, data)
        catchBlock?.accept(visitor, data)
    }

    override fun deepCopy(): BrsTry = BrsTry(
        tryBlock.deepCopy(),
        catchVariable,
        catchBlock?.deepCopy()
    ).also { it.source = source }
}

/**
 * A throw statement (Roku OS 9.4+).
 *
 * ```brightscript
 * throw "Error message"
 * ```
 */
class BrsThrow(
    var message: BrsExpression
) : BrsNodeBase(), BrsStatement {

    override fun <R, D> accept(visitor: BrsVisitor<R, D>, data: D): R = visitor.visitThrow(this, data)

    override fun <R, D> acceptChildren(visitor: BrsVisitor<R, D>, data: D) {
        message.accept(visitor, data)
    }

    override fun deepCopy(): BrsThrow = BrsThrow(message.deepCopy()).also { it.source = source }
}

/**
 * Kind of exit statement.
 */
enum class BrsExitKind {
    FOR,
    WHILE
}

/**
 * An exit statement to break out of a loop.
 *
 * ```brightscript
 * exit for
 * exit while
 * ```
 */
class BrsExit(
    var kind: BrsExitKind
) : BrsNodeBase(), BrsStatement {

    override fun <R, D> accept(visitor: BrsVisitor<R, D>, data: D): R = visitor.visitExit(this, data)

    override fun <R, D> acceptChildren(visitor: BrsVisitor<R, D>, data: D) {}

    override fun deepCopy(): BrsExit = BrsExit(kind).also { it.source = source }
}

/**
 * Kind of continue statement.
 */
enum class BrsContinueKind {
    FOR,
    WHILE
}

/**
 * A continue statement to skip to the next iteration.
 *
 * ```brightscript
 * continue for
 * continue while
 * ```
 */
class BrsContinue(
    var kind: BrsContinueKind
) : BrsNodeBase(), BrsStatement {

    override fun <R, D> accept(visitor: BrsVisitor<R, D>, data: D): R = visitor.visitContinue(this, data)

    override fun <R, D> acceptChildren(visitor: BrsVisitor<R, D>, data: D) {}

    override fun deepCopy(): BrsContinue = BrsContinue(kind).also { it.source = source }
}

/**
 * A stop statement to halt execution (for debugging).
 *
 * ```brightscript
 * stop
 * ```
 */
class BrsStop : BrsNodeBase(), BrsStatement {

    override fun <R, D> accept(visitor: BrsVisitor<R, D>, data: D): R = visitor.visitStop(this, data)

    override fun <R, D> acceptChildren(visitor: BrsVisitor<R, D>, data: D) {}

    override fun deepCopy(): BrsStop = BrsStop().also { it.source = source }
}

/**
 * An empty statement (no-op).
 */
class BrsEmpty : BrsNodeBase(), BrsStatement {

    override fun <R, D> accept(visitor: BrsVisitor<R, D>, data: D): R = visitor.visitEmpty(this, data)

    override fun <R, D> acceptChildren(visitor: BrsVisitor<R, D>, data: D) {}

    override fun deepCopy(): BrsEmpty = BrsEmpty().also { it.source = source }
}

/**
 * A variable declaration/assignment statement.
 *
 * ```brightscript
 * myVar = value
 * myVar as Type = value
 * ```
 */
class BrsVariable(
    var name: String,
    var type: BrsType? = null,
    var initializer: BrsExpression? = null
) : BrsNodeBase(), BrsStatement {

    override fun <R, D> accept(visitor: BrsVisitor<R, D>, data: D): R = visitor.visitVariable(this, data)

    override fun <R, D> acceptChildren(visitor: BrsVisitor<R, D>, data: D) {
        initializer?.accept(visitor, data)
    }

    override fun deepCopy(): BrsVariable = BrsVariable(name, type, initializer?.deepCopy()).also { it.source = source }
}

/**
 * A comment (single-line or REM).
 *
 * ```brightscript
 * ' This is a comment
 * REM This is also a comment
 * ```
 */
class BrsComment(
    var text: String,
    var isRem: Boolean = false
) : BrsNodeBase(), BrsStatement {

    override fun <R, D> accept(visitor: BrsVisitor<R, D>, data: D): R = visitor.visitComment(this, data)

    override fun <R, D> acceptChildren(visitor: BrsVisitor<R, D>, data: D) {}

    override fun deepCopy(): BrsComment = BrsComment(text, isRem).also { it.source = source }
}
