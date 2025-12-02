/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.brs.backend.ast

/**
 * Renders BrightScript AST nodes to text.
 *
 * This visitor traverses the AST and generates valid BrightScript code.
 */
class BrsRenderer(
    private val builder: StringBuilder = StringBuilder(),
    private val indentString: String = "    "
) : BrsVisitor<Unit, Unit>() {

    private var indentLevel = 0

    /**
     * Get the rendered output as a string.
     */
    fun output(): String = builder.toString()

    /**
     * Render a node and return the result as a string.
     */
    fun render(node: BrsNode): String {
        node.accept(this, Unit)
        return output()
    }

    private fun indent() {
        repeat(indentLevel) { builder.append(indentString) }
    }

    private fun newline() {
        builder.append("\n")
    }

    private fun withIndent(block: () -> Unit) {
        indentLevel++
        block()
        indentLevel--
    }

    // ==================== Base ====================

    override fun visitNode(node: BrsNode, data: Unit) {
        // Default implementation - should be overridden for all concrete types
        builder.append("' Unknown node: ${node::class.simpleName}")
    }

    // ==================== Program Structure ====================

    override fun visitProgram(program: BrsProgram, data: Unit) {
        program.declarations.forEachIndexed { index, decl ->
            if (index > 0) newline()
            decl.accept(this, data)
            newline()
        }

        if (program.declarations.isNotEmpty() && program.statements.isNotEmpty()) {
            newline()
        }

        program.statements.forEach { stmt ->
            stmt.accept(this, data)
            newline()
        }
    }

    override fun visitComponent(component: BrsComponent, data: Unit) {
        // Note: This renders just the BrightScript portion of a component.
        // XML generation would be handled separately.
        builder.append("' Component: ${component.name} extends ${component.extends}")
        newline()
        newline()
        component.script.accept(this, data)
    }

    // ==================== Declarations ====================

    override fun visitFunction(function: BrsFunction, data: Unit) {
        indent()
        builder.append("function ${function.name}(")
        renderParameters(function.parameters)
        builder.append(")")
        function.returnType?.let { builder.append(" as ${it.typeName}") }
        newline()
        withIndent {
            renderBlockContents(function.body)
        }
        indent()
        builder.append("end function")
    }

    override fun visitSub(sub: BrsSub, data: Unit) {
        indent()
        builder.append("sub ${sub.name}(")
        renderParameters(sub.parameters)
        builder.append(")")
        newline()
        withIndent {
            renderBlockContents(sub.body)
        }
        indent()
        builder.append("end sub")
    }

    override fun visitParameter(parameter: BrsParameter, data: Unit) {
        builder.append(parameter.name)
        // BrightScript doesn't allow both type annotation AND default value
        // Only emit type if there's no default value
        if (parameter.defaultValue == null) {
            parameter.type?.let { builder.append(" as ${it.typeName}") }
        }
        parameter.defaultValue?.let {
            builder.append(" = ")
            it.accept(this, data)
        }
    }

    override fun visitField(field: BrsField, data: Unit) {
        // Fields are typically rendered as XML, but provide a comment representation
        indent()
        builder.append("' field: ${field.name} as ${field.type}")
        field.defaultValue?.let { builder.append(" = \"$it\"") }
        field.onChange?.let { builder.append(" onChange=\"$it\"") }
        if (field.alwaysNotify) builder.append(" alwaysNotify=\"true\"")
    }

    private fun renderParameters(parameters: List<BrsParameter>) {
        parameters.forEachIndexed { index, param ->
            if (index > 0) builder.append(", ")
            param.accept(this, Unit)
        }
    }

    // ==================== Statements ====================

    override fun visitBlock(block: BrsBlock, data: Unit) {
        block.statements.forEach { stmt ->
            stmt.accept(this, data)
            newline()
        }
    }

    private fun renderBlockContents(block: BrsBlock) {
        block.statements.forEach { stmt ->
            stmt.accept(this, Unit)
            newline()
        }
    }

    override fun visitIf(ifStatement: BrsIf, data: Unit) {
        renderIfChain(ifStatement, data, isFirst = true)
    }

    /**
     * Render an if/else-if/else chain iteratively to maintain proper indentation.
     */
    private fun renderIfChain(ifStatement: BrsIf, data: Unit, isFirst: Boolean) {
        // Render "if" or "else if"
        indent()
        if (isFirst) {
            builder.append("if ")
        } else {
            builder.append("else if ")
        }
        ifStatement.condition.accept(this, data)
        builder.append(" then")
        newline()

        // Render then branch with proper indentation
        withIndent {
            when (val thenBranch = ifStatement.thenBranch) {
                is BrsBlock -> renderBlockContents(thenBranch)
                else -> {
                    thenBranch.accept(this, data)
                    newline()
                }
            }
        }

        // Handle else branch
        val elseBranch = ifStatement.elseBranch
        when {
            elseBranch == null -> {
                indent()
                builder.append("end if")
            }
            elseBranch is BrsIf -> {
                // Continue the chain with else-if
                renderIfChain(elseBranch, data, isFirst = false)
            }
            else -> {
                indent()
                builder.append("else")
                newline()
                withIndent {
                    when (elseBranch) {
                        is BrsBlock -> renderBlockContents(elseBranch)
                        else -> {
                            elseBranch.accept(this, data)
                            newline()
                        }
                    }
                }
                indent()
                builder.append("end if")
            }
        }
    }

    override fun visitWhile(whileStatement: BrsWhile, data: Unit) {
        indent()
        builder.append("while ")
        whileStatement.condition.accept(this, data)
        newline()

        withIndent {
            when (val body = whileStatement.body) {
                is BrsBlock -> renderBlockContents(body)
                else -> {
                    body.accept(this, data)
                    newline()
                }
            }
        }

        indent()
        builder.append("end while")
    }

    override fun visitFor(forStatement: BrsFor, data: Unit) {
        indent()
        builder.append("for ${forStatement.variable} = ")
        forStatement.start.accept(this, data)
        builder.append(" to ")
        forStatement.end.accept(this, data)
        forStatement.step?.let {
            builder.append(" step ")
            it.accept(this, data)
        }
        newline()

        withIndent {
            when (val body = forStatement.body) {
                is BrsBlock -> renderBlockContents(body)
                else -> {
                    body.accept(this, data)
                    newline()
                }
            }
        }

        indent()
        builder.append("end for")
    }

    override fun visitForEach(forEachStatement: BrsForEach, data: Unit) {
        indent()
        builder.append("for each ${forEachStatement.variable} in ")
        forEachStatement.iterable.accept(this, data)
        newline()

        withIndent {
            when (val body = forEachStatement.body) {
                is BrsBlock -> renderBlockContents(body)
                else -> {
                    body.accept(this, data)
                    newline()
                }
            }
        }

        indent()
        builder.append("end for")
    }

    override fun visitReturn(returnStatement: BrsReturn, data: Unit) {
        indent()
        builder.append("return")
        returnStatement.value?.let {
            builder.append(" ")
            it.accept(this, data)
        }
    }

    override fun visitExpressionStatement(statement: BrsExpressionStatement, data: Unit) {
        indent()
        statement.expression.accept(this, data)
    }

    override fun visitPrint(printStatement: BrsPrint, data: Unit) {
        indent()
        builder.append("print ")
        printStatement.expressions.forEachIndexed { index, expr ->
            if (index > 0) builder.append("; ")
            expr.accept(this, data)
        }
    }

    override fun visitTry(tryStatement: BrsTry, data: Unit) {
        indent()
        builder.append("try")
        newline()

        withIndent {
            when (val tryBlock = tryStatement.tryBlock) {
                is BrsBlock -> renderBlockContents(tryBlock)
                else -> {
                    tryBlock.accept(this, data)
                    newline()
                }
            }
        }

        if (tryStatement.catchBlock != null) {
            indent()
            builder.append("catch")
            tryStatement.catchVariable?.let { builder.append(" $it") }
            newline()

            withIndent {
                when (val catchBlock = tryStatement.catchBlock) {
                    is BrsBlock -> renderBlockContents(catchBlock)
                    else -> {
                        catchBlock?.accept(this, data)
                        newline()
                    }
                }
            }
        }

        indent()
        builder.append("end try")
    }

    override fun visitThrow(throwStatement: BrsThrow, data: Unit) {
        indent()
        builder.append("throw ")
        throwStatement.message.accept(this, data)
    }

    override fun visitExit(exitStatement: BrsExit, data: Unit) {
        indent()
        when (exitStatement.kind) {
            BrsExitKind.FOR -> builder.append("exit for")
            BrsExitKind.WHILE -> builder.append("exit while")
        }
    }

    override fun visitContinue(continueStatement: BrsContinue, data: Unit) {
        indent()
        when (continueStatement.kind) {
            BrsContinueKind.FOR -> builder.append("continue for")
            BrsContinueKind.WHILE -> builder.append("continue while")
        }
    }

    override fun visitStop(stopStatement: BrsStop, data: Unit) {
        indent()
        builder.append("stop")
    }

    override fun visitEmpty(emptyStatement: BrsEmpty, data: Unit) {
        // Empty statement - nothing to render
    }

    override fun visitVariable(variable: BrsVariable, data: Unit) {
        indent()
        builder.append(variable.name)
        // BrightScript local variables don't have type declarations - just assignment
        variable.initializer?.let {
            builder.append(" = ")
            it.accept(this, data)
        }
    }

    override fun visitComment(comment: BrsComment, data: Unit) {
        indent()
        if (comment.isRem) {
            builder.append("REM ${comment.text}")
        } else {
            builder.append("' ${comment.text}")
        }
    }

    // ==================== Expressions ====================

    override fun visitIntLiteral(literal: BrsIntLiteral, data: Unit) {
        builder.append(literal.value)
    }

    override fun visitLongIntLiteral(literal: BrsLongIntLiteral, data: Unit) {
        builder.append("${literal.value}&")
    }

    override fun visitFloatLiteral(literal: BrsFloatLiteral, data: Unit) {
        builder.append("${literal.value}!")
    }

    override fun visitDoubleLiteral(literal: BrsDoubleLiteral, data: Unit) {
        builder.append("${literal.value}#")
    }

    override fun visitStringLiteral(literal: BrsStringLiteral, data: Unit) {
        // Escape quotes in strings by doubling them
        val escaped = literal.value.replace("\"", "\"\"")
        builder.append("\"$escaped\"")
    }

    override fun visitBooleanLiteral(literal: BrsBooleanLiteral, data: Unit) {
        builder.append(if (literal.value) "true" else "false")
    }

    override fun visitInvalidLiteral(literal: BrsInvalidLiteral, data: Unit) {
        builder.append("invalid")
    }

    override fun visitIdentifier(identifier: BrsIdentifier, data: Unit) {
        builder.append(identifier.name)
    }

    override fun visitMRef(mRef: BrsMRef, data: Unit) {
        builder.append("m")
    }

    override fun visitBinaryOp(binaryOp: BrsBinaryOp, data: Unit) {
        val needsParens = binaryOp.left is BrsBinaryOp || binaryOp.left is BrsConditional

        if (needsParens) builder.append("(")
        binaryOp.left.accept(this, data)
        if (needsParens) builder.append(")")

        builder.append(" ${binaryOp.operator.symbol} ")

        val rightNeedsParens = binaryOp.right is BrsBinaryOp || binaryOp.right is BrsConditional
        if (rightNeedsParens) builder.append("(")
        binaryOp.right.accept(this, data)
        if (rightNeedsParens) builder.append(")")
    }

    override fun visitUnaryOp(unaryOp: BrsUnaryOp, data: Unit) {
        builder.append(unaryOp.operator.symbol)
        if (unaryOp.operator == BrsUnaryOperator.NOT) {
            builder.append(" ")
        }
        val needsParens = unaryOp.operand is BrsBinaryOp
        if (needsParens) builder.append("(")
        unaryOp.operand.accept(this, data)
        if (needsParens) builder.append(")")
    }

    override fun visitFunctionCall(functionCall: BrsFunctionCall, data: Unit) {
        functionCall.target.accept(this, data)
        builder.append("(")
        functionCall.arguments.forEachIndexed { index, arg ->
            if (index > 0) builder.append(", ")
            arg.accept(this, data)
        }
        builder.append(")")
    }

    override fun visitMethodCall(methodCall: BrsMethodCall, data: Unit) {
        methodCall.receiver.accept(this, data)
        builder.append(".${methodCall.method}(")
        methodCall.arguments.forEachIndexed { index, arg ->
            if (index > 0) builder.append(", ")
            arg.accept(this, data)
        }
        builder.append(")")
    }

    override fun visitIndexAccess(indexAccess: BrsIndexAccess, data: Unit) {
        indexAccess.target.accept(this, data)
        builder.append("[")
        indexAccess.index.accept(this, data)
        builder.append("]")
    }

    override fun visitDotAccess(dotAccess: BrsDotAccess, data: Unit) {
        dotAccess.target.accept(this, data)
        builder.append(".${dotAccess.field}")
    }

    override fun visitArrayLiteral(arrayLiteral: BrsArrayLiteral, data: Unit) {
        builder.append("[")
        arrayLiteral.elements.forEachIndexed { index, element ->
            if (index > 0) builder.append(", ")
            element.accept(this, data)
        }
        builder.append("]")
    }

    override fun visitAALiteral(aaLiteral: BrsAALiteral, data: Unit) {
        if (aaLiteral.entries.isEmpty()) {
            builder.append("{}")
            return
        }

        builder.append("{")
        aaLiteral.entries.forEachIndexed { index, entry ->
            if (index > 0) builder.append(", ")
            builder.append("${entry.key}: ")
            entry.value.accept(this, data)
        }
        builder.append("}")
    }

    override fun visitConditional(conditional: BrsConditional, data: Unit) {
        // BrightScript doesn't have a ternary operator, so we use inline if
        // This works in expression context in some BrightScript versions
        builder.append("(if ")
        conditional.condition.accept(this, data)
        builder.append(" then ")
        conditional.thenExpr.accept(this, data)
        builder.append(" else ")
        conditional.elseExpr.accept(this, data)
        builder.append(")")
    }

    override fun visitTypeOf(typeOf: BrsTypeOf, data: Unit) {
        builder.append("Type(")
        typeOf.value.accept(this, data)
        builder.append(")")
    }

    override fun visitCreateObject(createObject: BrsCreateObject, data: Unit) {
        builder.append("CreateObject(\"${createObject.objectType}\"")
        createObject.arguments.forEach { arg ->
            builder.append(", ")
            arg.accept(this, data)
        }
        builder.append(")")
    }

    override fun visitAnonymousFunction(func: BrsAnonymousFunction, data: Unit) {
        builder.append("function(")
        renderParameters(func.parameters)
        builder.append(")")
        func.returnType?.let { builder.append(" as ${it.typeName}") }
        newline()
        withIndent {
            renderBlockContents(func.body)
        }
        indent()
        builder.append("end function")
    }

    override fun visitPrintNoNewline(printNoNewline: BrsPrintNoNewline, data: Unit) {
        // In BrightScript, a trailing semicolon after print suppresses the newline
        builder.append("print ")
        printNoNewline.value.accept(this, data)
        builder.append(";")
    }

    companion object {
        /**
         * Render a BrightScript AST node to a string.
         */
        fun render(node: BrsNode): String {
            return BrsRenderer().render(node)
        }
    }
}

/**
 * Extension function to render any BrsNode to a string.
 */
fun BrsNode.render(): String = BrsRenderer.render(this)
