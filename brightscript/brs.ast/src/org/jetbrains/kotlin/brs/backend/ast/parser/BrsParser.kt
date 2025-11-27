/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.brs.backend.ast.parser

import org.jetbrains.kotlin.brs.backend.ast.*

/**
 * Parser for BrightScript source code.
 *
 * Uses recursive descent parsing to convert a token stream into
 * BrightScript AST nodes. This parser is used for:
 * - Parsing @BrsInline code strings
 * - Parsing .brs files for mixed project support
 * - Extracting type declarations from existing BrightScript code
 */
class BrsParser(private val tokens: List<BrsToken>) {

    private var current = 0
    private val errors = mutableListOf<String>()

    /**
     * Parse the token stream into a BrightScript program.
     */
    fun parseProgram(): BrsParseResult<BrsProgram> {
        val declarations = mutableListOf<BrsDeclaration>()
        val statements = mutableListOf<BrsStatement>()

        skipNewlines()

        while (!isAtEnd()) {
            try {
                when {
                    check(BrsTokenType.FUNCTION) -> declarations.add(parseFunction())
                    check(BrsTokenType.SUB) -> declarations.add(parseSub())
                    check(BrsTokenType.LIBRARY) -> parseLibrary() // Skip library statements
                    else -> {
                        val stmt = parseStatement()
                        if (stmt != null) {
                            statements.add(stmt)
                        }
                    }
                }
                skipNewlines()
            } catch (e: ParseException) {
                errors.add(e.message ?: "Parse error")
                synchronize()
            }
        }

        return BrsParseResult(BrsProgram(declarations, statements), errors)
    }

    /**
     * Parse a single statement (for @BrsInline code).
     */
    fun parseStatements(): BrsParseResult<List<BrsStatement>> {
        val statements = mutableListOf<BrsStatement>()

        skipNewlines()

        while (!isAtEnd()) {
            try {
                val stmt = parseStatement()
                if (stmt != null) {
                    statements.add(stmt)
                }
                skipNewlines()
            } catch (e: ParseException) {
                errors.add(e.message ?: "Parse error")
                synchronize()
            }
        }

        return BrsParseResult(statements, errors)
    }

    /**
     * Parse a single expression (for inline expressions).
     */
    fun parseExpression(): BrsParseResult<BrsExpression> {
        return try {
            skipNewlines()
            BrsParseResult(expression(), errors)
        } catch (e: ParseException) {
            errors.add(e.message ?: "Parse error")
            BrsParseResult(BrsInvalidLiteral(), errors)
        }
    }

    // ==================== Declarations ====================

    private fun parseFunction(): BrsFunction {
        consume(BrsTokenType.FUNCTION, "Expected 'function'")
        val name = consume(BrsTokenType.IDENTIFIER, "Expected function name").text

        consume(BrsTokenType.LPAREN, "Expected '(' after function name")
        val parameters = parseParameters()
        consume(BrsTokenType.RPAREN, "Expected ')' after parameters")

        val returnType = if (check(BrsTokenType.AS)) {
            advance()
            parseType()
        } else null

        consumeNewlineOrColon()
        val body = parseBlock(BrsTokenType.END)
        consumeEndFunction()

        return BrsFunction(name, parameters.toMutableList(), returnType, body)
    }

    private fun parseSub(): BrsSub {
        consume(BrsTokenType.SUB, "Expected 'sub'")
        val name = consume(BrsTokenType.IDENTIFIER, "Expected sub name").text

        consume(BrsTokenType.LPAREN, "Expected '(' after sub name")
        val parameters = parseParameters()
        consume(BrsTokenType.RPAREN, "Expected ')' after parameters")

        consumeNewlineOrColon()
        val body = parseBlock(BrsTokenType.END)
        consumeEndSub()

        return BrsSub(name, parameters.toMutableList(), body)
    }

    private fun parseParameters(): List<BrsParameter> {
        val params = mutableListOf<BrsParameter>()

        if (!check(BrsTokenType.RPAREN)) {
            do {
                val paramName = consume(BrsTokenType.IDENTIFIER, "Expected parameter name").text
                val paramType = if (check(BrsTokenType.AS)) {
                    advance()
                    parseType()
                } else null

                val defaultValue = if (check(BrsTokenType.EQ)) {
                    advance()
                    expression()
                } else null

                params.add(BrsParameter(paramName, paramType, defaultValue))
            } while (match(BrsTokenType.COMMA))
        }

        return params
    }

    private fun parseType(): BrsType? {
        return when {
            match(BrsTokenType.TYPE_INTEGER) -> BrsType.INTEGER
            match(BrsTokenType.TYPE_LONGINTEGER) -> BrsType.LONG_INTEGER
            match(BrsTokenType.TYPE_FLOAT) -> BrsType.FLOAT
            match(BrsTokenType.TYPE_DOUBLE) -> BrsType.DOUBLE
            match(BrsTokenType.TYPE_STRING) -> BrsType.STRING
            match(BrsTokenType.TYPE_BOOLEAN) -> BrsType.BOOLEAN
            match(BrsTokenType.TYPE_OBJECT) -> BrsType.OBJECT
            match(BrsTokenType.TYPE_DYNAMIC) -> BrsType.DYNAMIC
            match(BrsTokenType.TYPE_VOID) -> BrsType.VOID
            match(BrsTokenType.TYPE_FUNCTION) -> BrsType.FUNCTION
            match(BrsTokenType.IDENTIFIER) -> BrsType.OBJECT // Custom type
            else -> null
        }
    }

    private fun parseLibrary() {
        consume(BrsTokenType.LIBRARY, "Expected 'library'")
        consume(BrsTokenType.STRING_LITERAL, "Expected library path")
        consumeNewlineOrColon()
    }

    private fun consumeEndFunction() {
        consume(BrsTokenType.END, "Expected 'end'")
        consume(BrsTokenType.FUNCTION, "Expected 'function' after 'end'")
    }

    private fun consumeEndSub() {
        consume(BrsTokenType.END, "Expected 'end'")
        consume(BrsTokenType.SUB, "Expected 'sub' after 'end'")
    }

    // ==================== Statements ====================

    private fun parseStatement(): BrsStatement? {
        return when {
            check(BrsTokenType.IF) -> parseIf()
            check(BrsTokenType.WHILE) -> parseWhile()
            check(BrsTokenType.FOR) -> parseFor()
            check(BrsTokenType.RETURN) -> parseReturn()
            check(BrsTokenType.PRINT) -> parsePrint()
            check(BrsTokenType.TRY) -> parseTry()
            check(BrsTokenType.THROW) -> parseThrow()
            check(BrsTokenType.EXIT) -> parseExit()
            check(BrsTokenType.CONTINUE) -> parseContinue()
            check(BrsTokenType.STOP) -> parseStop()
            check(BrsTokenType.COMMENT) -> parseComment()
            check(BrsTokenType.NEWLINE) -> {
                advance()
                null
            }
            else -> parseExpressionStatement()
        }
    }

    private fun parseIf(): BrsIf {
        consume(BrsTokenType.IF, "Expected 'if'")
        val condition = expression()
        consume(BrsTokenType.THEN, "Expected 'then' after condition")

        // Check for single-line if
        if (!check(BrsTokenType.NEWLINE) && !check(BrsTokenType.COLON) && !isAtEnd()) {
            val thenStmt = parseStatement() ?: BrsEmpty()
            val elseStmt = if (match(BrsTokenType.ELSE)) {
                parseStatement()
            } else null
            return BrsIf(condition, thenStmt, elseStmt)
        }

        consumeNewlineOrColon()
        val thenBranch = parseBlock(BrsTokenType.ELSE, BrsTokenType.ELSE_IF, BrsTokenType.END)

        val elseBranch: BrsStatement? = when {
            check(BrsTokenType.ELSE_IF) -> parseElseIf()
            match(BrsTokenType.ELSE) -> {
                consumeNewlineOrColon()
                val elseBlock = parseBlock(BrsTokenType.END)
                consumeEndIf()
                elseBlock
            }
            else -> {
                consumeEndIf()
                null
            }
        }

        return BrsIf(condition, thenBranch, elseBranch)
    }

    private fun parseElseIf(): BrsIf {
        consume(BrsTokenType.ELSE_IF, "Expected 'else if'")
        val condition = expression()
        consume(BrsTokenType.THEN, "Expected 'then' after condition")
        consumeNewlineOrColon()

        val thenBranch = parseBlock(BrsTokenType.ELSE, BrsTokenType.ELSE_IF, BrsTokenType.END)

        val elseBranch: BrsStatement? = when {
            check(BrsTokenType.ELSE_IF) -> parseElseIf()
            match(BrsTokenType.ELSE) -> {
                consumeNewlineOrColon()
                val elseBlock = parseBlock(BrsTokenType.END)
                consumeEndIf()
                elseBlock
            }
            else -> {
                consumeEndIf()
                null
            }
        }

        return BrsIf(condition, thenBranch, elseBranch)
    }

    private fun consumeEndIf() {
        consume(BrsTokenType.END, "Expected 'end'")
        consume(BrsTokenType.IF, "Expected 'if' after 'end'")
    }

    private fun parseWhile(): BrsWhile {
        consume(BrsTokenType.WHILE, "Expected 'while'")
        val condition = expression()
        consumeNewlineOrColon()

        val body = parseBlock(BrsTokenType.END)
        consume(BrsTokenType.END, "Expected 'end'")
        consume(BrsTokenType.WHILE, "Expected 'while' after 'end'")

        return BrsWhile(condition, body)
    }

    private fun parseFor(): BrsStatement {
        consume(BrsTokenType.FOR, "Expected 'for'")

        // Check for "for each" vs "for i = ..."
        return if (match(BrsTokenType.EACH)) {
            parseForEach()
        } else {
            parseForTo()
        }
    }

    private fun parseForTo(): BrsFor {
        val variable = consume(BrsTokenType.IDENTIFIER, "Expected variable name").text
        consume(BrsTokenType.EQ, "Expected '=' after variable")
        val start = expression()
        consume(BrsTokenType.TO, "Expected 'to'")
        val end = expression()

        val step = if (match(BrsTokenType.STEP)) {
            expression()
        } else null

        consumeNewlineOrColon()
        val body = parseBlock(BrsTokenType.END, BrsTokenType.NEXT)

        // Consume "end for" or "next"
        when {
            match(BrsTokenType.END) -> consume(BrsTokenType.FOR, "Expected 'for' after 'end'")
            match(BrsTokenType.NEXT) -> { /* optional variable name */ if (check(BrsTokenType.IDENTIFIER)) advance() }
            else -> throw error("Expected 'end for' or 'next'")
        }

        return BrsFor(variable, start, end, step, body)
    }

    private fun parseForEach(): BrsForEach {
        val variable = consume(BrsTokenType.IDENTIFIER, "Expected variable name").text
        consume(BrsTokenType.IN, "Expected 'in'")
        val iterable = expression()

        consumeNewlineOrColon()
        val body = parseBlock(BrsTokenType.END, BrsTokenType.NEXT)

        // Consume "end for" or "next"
        when {
            match(BrsTokenType.END) -> consume(BrsTokenType.FOR, "Expected 'for' after 'end'")
            match(BrsTokenType.NEXT) -> { /* optional variable name */ if (check(BrsTokenType.IDENTIFIER)) advance() }
            else -> throw error("Expected 'end for' or 'next'")
        }

        return BrsForEach(variable, iterable, body)
    }

    private fun parseReturn(): BrsReturn {
        consume(BrsTokenType.RETURN, "Expected 'return'")

        val value = if (!check(BrsTokenType.NEWLINE) && !check(BrsTokenType.COLON) && !isAtEnd()) {
            expression()
        } else null

        return BrsReturn(value)
    }

    private fun parsePrint(): BrsPrint {
        consume(BrsTokenType.PRINT, "Expected 'print'")

        val expressions = mutableListOf<BrsExpression>()

        if (!check(BrsTokenType.NEWLINE) && !check(BrsTokenType.COLON) && !isAtEnd()) {
            expressions.add(expression())
            while (match(BrsTokenType.SEMICOLON) || match(BrsTokenType.COMMA)) {
                if (!check(BrsTokenType.NEWLINE) && !check(BrsTokenType.COLON) && !isAtEnd()) {
                    expressions.add(expression())
                }
            }
        }

        return BrsPrint(expressions)
    }

    private fun parseTry(): BrsTry {
        consume(BrsTokenType.TRY, "Expected 'try'")
        consumeNewlineOrColon()

        val tryBlock = parseBlock(BrsTokenType.CATCH, BrsTokenType.END)

        var catchVariable: String? = null
        var catchBlock: BrsStatement? = null

        if (match(BrsTokenType.CATCH)) {
            if (check(BrsTokenType.IDENTIFIER)) {
                catchVariable = advance().text
            }
            consumeNewlineOrColon()
            catchBlock = parseBlock(BrsTokenType.END)
        }

        consume(BrsTokenType.END, "Expected 'end'")
        consume(BrsTokenType.TRY, "Expected 'try' after 'end'")

        return BrsTry(tryBlock, catchVariable, catchBlock)
    }

    private fun parseThrow(): BrsThrow {
        consume(BrsTokenType.THROW, "Expected 'throw'")
        val message = expression()
        return BrsThrow(message)
    }

    private fun parseExit(): BrsExit {
        consume(BrsTokenType.EXIT, "Expected 'exit'")
        val kind = when {
            match(BrsTokenType.FOR) -> BrsExitKind.FOR
            match(BrsTokenType.WHILE) -> BrsExitKind.WHILE
            else -> BrsExitKind.FOR // Default
        }
        return BrsExit(kind)
    }

    private fun parseContinue(): BrsContinue {
        consume(BrsTokenType.CONTINUE, "Expected 'continue'")
        val kind = when {
            match(BrsTokenType.FOR) -> BrsContinueKind.FOR
            match(BrsTokenType.WHILE) -> BrsContinueKind.WHILE
            else -> BrsContinueKind.FOR // Default
        }
        return BrsContinue(kind)
    }

    private fun parseStop(): BrsStop {
        consume(BrsTokenType.STOP, "Expected 'stop'")
        return BrsStop()
    }

    private fun parseComment(): BrsComment {
        val token = consume(BrsTokenType.COMMENT, "Expected comment")
        return BrsComment(token.value as? String ?: token.text)
    }

    private fun parseExpressionStatement(): BrsStatement {
        // Look ahead for simple variable assignment: IDENTIFIER = expr
        // We need to detect this BEFORE calling expression() because expression()
        // will consume the '=' as an equality comparison operator
        if (check(BrsTokenType.IDENTIFIER)) {
            val savedCurrent = current
            val nameToken = advance()

            when {
                check(BrsTokenType.EQ) -> {
                    // Simple assignment: x = value
                    advance()
                    val value = expression()
                    return BrsVariable(nameToken.text, null, value)
                }
                check(BrsTokenType.AS) -> {
                    // Variable declaration with type: x as Integer = 5
                    advance()
                    val type = parseType()
                    val value = if (match(BrsTokenType.EQ)) expression() else null
                    return BrsVariable(nameToken.text, type, value)
                }
                else -> {
                    // Not an assignment, backtrack and parse as expression
                    current = savedCurrent
                }
            }
        }

        val expr = expression()

        // Check for compound assignment (dot access or index access)
        return if (expr is BrsDotAccess && check(BrsTokenType.EQ)) {
            // Assignment to field: obj.field = value
            advance()
            val value = expression()
            BrsExpressionStatement(BrsBinaryOp(expr, BrsBinaryOperator.EQ, value))
        } else if (expr is BrsIndexAccess && check(BrsTokenType.EQ)) {
            // Assignment to index: arr[i] = value
            advance()
            val value = expression()
            BrsExpressionStatement(BrsBinaryOp(expr, BrsBinaryOperator.EQ, value))
        } else {
            BrsExpressionStatement(expr)
        }
    }

    private fun parseBlock(vararg terminators: BrsTokenType): BrsBlock {
        val statements = mutableListOf<BrsStatement>()
        skipNewlines()

        while (!isAtEnd() && terminators.none { check(it) }) {
            val stmt = parseStatement()
            if (stmt != null) {
                statements.add(stmt)
            }
            skipNewlines()
        }

        return BrsBlock(statements)
    }

    // ==================== Expressions ====================

    private fun expression(): BrsExpression = orExpr()

    private fun orExpr(): BrsExpression {
        var left = andExpr()

        while (match(BrsTokenType.OR)) {
            val right = andExpr()
            left = BrsBinaryOp(left, BrsBinaryOperator.OR, right)
        }

        return left
    }

    private fun andExpr(): BrsExpression {
        var left = notExpr()

        while (match(BrsTokenType.AND)) {
            val right = notExpr()
            left = BrsBinaryOp(left, BrsBinaryOperator.AND, right)
        }

        return left
    }

    private fun notExpr(): BrsExpression {
        if (match(BrsTokenType.NOT)) {
            val operand = notExpr()
            return BrsUnaryOp(BrsUnaryOperator.NOT, operand)
        }
        return comparison()
    }

    private fun comparison(): BrsExpression {
        var left = addition()

        while (true) {
            val op = when {
                match(BrsTokenType.LT) -> BrsBinaryOperator.LT
                match(BrsTokenType.GT) -> BrsBinaryOperator.GT
                match(BrsTokenType.LE) -> BrsBinaryOperator.LE
                match(BrsTokenType.GE) -> BrsBinaryOperator.GE
                match(BrsTokenType.EQ) -> BrsBinaryOperator.EQ
                match(BrsTokenType.NE) -> BrsBinaryOperator.NE
                else -> break
            }
            val right = addition()
            left = BrsBinaryOp(left, op, right)
        }

        return left
    }

    private fun addition(): BrsExpression {
        var left = multiplication()

        while (true) {
            val op = when {
                match(BrsTokenType.PLUS) -> BrsBinaryOperator.ADD
                match(BrsTokenType.MINUS) -> BrsBinaryOperator.SUB
                else -> break
            }
            val right = multiplication()
            left = BrsBinaryOp(left, op, right)
        }

        return left
    }

    private fun multiplication(): BrsExpression {
        var left = shift()

        while (true) {
            val op = when {
                match(BrsTokenType.STAR) -> BrsBinaryOperator.MUL
                match(BrsTokenType.SLASH) -> BrsBinaryOperator.DIV
                match(BrsTokenType.BACKSLASH) -> BrsBinaryOperator.INT_DIV
                match(BrsTokenType.MOD) -> BrsBinaryOperator.MOD
                else -> break
            }
            val right = shift()
            left = BrsBinaryOp(left, op, right)
        }

        return left
    }

    private fun shift(): BrsExpression {
        var left = exponent()

        while (true) {
            val op = when {
                match(BrsTokenType.LEFT_SHIFT) -> BrsBinaryOperator.LEFT_SHIFT
                match(BrsTokenType.RIGHT_SHIFT) -> BrsBinaryOperator.RIGHT_SHIFT
                else -> break
            }
            val right = exponent()
            left = BrsBinaryOp(left, op, right)
        }

        return left
    }

    private fun exponent(): BrsExpression {
        var left = unary()

        while (match(BrsTokenType.CARET)) {
            val right = unary()
            left = BrsBinaryOp(left, BrsBinaryOperator.POW, right)
        }

        return left
    }

    private fun unary(): BrsExpression {
        if (match(BrsTokenType.MINUS)) {
            val operand = unary()
            return BrsUnaryOp(BrsUnaryOperator.NEG, operand)
        }
        if (match(BrsTokenType.PLUS)) {
            return unary() // Unary plus is a no-op
        }
        return postfix()
    }

    private fun postfix(): BrsExpression {
        var expr = primary()

        while (true) {
            expr = when {
                match(BrsTokenType.DOT) -> {
                    val field = consume(BrsTokenType.IDENTIFIER, "Expected field name").text
                    if (check(BrsTokenType.LPAREN)) {
                        // Method call
                        advance()
                        val args = parseArguments()
                        consume(BrsTokenType.RPAREN, "Expected ')' after arguments")
                        BrsMethodCall(expr, field, args.toMutableList())
                    } else {
                        BrsDotAccess(expr, field)
                    }
                }
                match(BrsTokenType.LBRACKET) -> {
                    val index = expression()
                    consume(BrsTokenType.RBRACKET, "Expected ']'")
                    BrsIndexAccess(expr, index)
                }
                match(BrsTokenType.LPAREN) -> {
                    // Function call on expression
                    val args = parseArguments()
                    consume(BrsTokenType.RPAREN, "Expected ')' after arguments")
                    BrsFunctionCall(expr, args.toMutableList())
                }
                else -> break
            }
        }

        return expr
    }

    private fun primary(): BrsExpression {
        return when {
            match(BrsTokenType.INTEGER_LITERAL) -> BrsIntLiteral(previous().value as Int)
            match(BrsTokenType.LONG_INTEGER_LITERAL) -> BrsLongIntLiteral(previous().value as Long)
            match(BrsTokenType.FLOAT_LITERAL) -> BrsFloatLiteral(previous().value as Float)
            match(BrsTokenType.DOUBLE_LITERAL) -> BrsDoubleLiteral(previous().value as Double)
            match(BrsTokenType.STRING_LITERAL) -> BrsStringLiteral(previous().value as String)
            match(BrsTokenType.TRUE) -> BrsBooleanLiteral(true)
            match(BrsTokenType.FALSE) -> BrsBooleanLiteral(false)
            match(BrsTokenType.INVALID) -> BrsInvalidLiteral()

            match(BrsTokenType.LBRACKET) -> parseArrayLiteral()
            match(BrsTokenType.LBRACE) -> parseAALiteral()

            match(BrsTokenType.FUNCTION) -> parseAnonymousFunction()
            match(BrsTokenType.SUB) -> parseAnonymousSub()

            match(BrsTokenType.LPAREN) -> {
                val expr = expression()
                consume(BrsTokenType.RPAREN, "Expected ')' after expression")
                expr
            }

            match(BrsTokenType.IDENTIFIER) -> {
                val name = previous().text
                // Check for CreateObject special case
                if (name.equals("CreateObject", ignoreCase = true) && check(BrsTokenType.LPAREN)) {
                    parseCreateObject()
                } else if (name.equals("Type", ignoreCase = true) && check(BrsTokenType.LPAREN)) {
                    parseTypeOf()
                } else if (name == "m") {
                    BrsMRef()
                } else {
                    BrsIdentifier(name)
                }
            }

            else -> throw error("Expected expression, got ${peek().type}")
        }
    }

    private fun parseArrayLiteral(): BrsArrayLiteral {
        val elements = mutableListOf<BrsExpression>()

        skipNewlines()
        if (!check(BrsTokenType.RBRACKET)) {
            do {
                skipNewlines()
                if (check(BrsTokenType.RBRACKET)) break
                elements.add(expression())
                skipNewlines()
            } while (match(BrsTokenType.COMMA))
        }
        skipNewlines()
        consume(BrsTokenType.RBRACKET, "Expected ']'")

        return BrsArrayLiteral(elements)
    }

    private fun parseAALiteral(): BrsAALiteral {
        val entries = mutableListOf<BrsAAEntry>()

        skipNewlines()
        if (!check(BrsTokenType.RBRACE)) {
            do {
                skipNewlines()
                if (check(BrsTokenType.RBRACE)) break

                val key = when {
                    check(BrsTokenType.IDENTIFIER) -> advance().text
                    check(BrsTokenType.STRING_LITERAL) -> (advance().value as String)
                    else -> throw error("Expected key in associative array")
                }
                consume(BrsTokenType.COLON, "Expected ':' after key")
                skipNewlines()
                val value = expression()
                entries.add(BrsAAEntry(key, value))
                skipNewlines()
            } while (match(BrsTokenType.COMMA))
        }
        skipNewlines()
        consume(BrsTokenType.RBRACE, "Expected '}'")

        return BrsAALiteral(entries)
    }

    private fun parseAnonymousFunction(): BrsAnonymousFunction {
        consume(BrsTokenType.LPAREN, "Expected '(' after 'function'")
        val parameters = parseParameters()
        consume(BrsTokenType.RPAREN, "Expected ')' after parameters")

        val returnType = if (check(BrsTokenType.AS)) {
            advance()
            parseType()
        } else null

        consumeNewlineOrColon()
        val body = parseBlock(BrsTokenType.END)
        consume(BrsTokenType.END, "Expected 'end'")
        consume(BrsTokenType.FUNCTION, "Expected 'function' after 'end'")

        return BrsAnonymousFunction(parameters.toMutableList(), returnType, body)
    }

    private fun parseAnonymousSub(): BrsAnonymousFunction {
        consume(BrsTokenType.LPAREN, "Expected '(' after 'sub'")
        val parameters = parseParameters()
        consume(BrsTokenType.RPAREN, "Expected ')' after parameters")

        consumeNewlineOrColon()
        val body = parseBlock(BrsTokenType.END)
        consume(BrsTokenType.END, "Expected 'end'")
        consume(BrsTokenType.SUB, "Expected 'sub' after 'end'")

        return BrsAnonymousFunction(parameters.toMutableList(), BrsType.VOID, body)
    }

    private fun parseCreateObject(): BrsCreateObject {
        advance() // consume '('
        val typeArg = consume(BrsTokenType.STRING_LITERAL, "Expected object type string")
        val objectType = typeArg.value as String

        val args = mutableListOf<BrsExpression>()
        while (match(BrsTokenType.COMMA)) {
            args.add(expression())
        }
        consume(BrsTokenType.RPAREN, "Expected ')' after CreateObject arguments")

        return BrsCreateObject(objectType, args)
    }

    private fun parseTypeOf(): BrsTypeOf {
        advance() // consume '('
        val value = expression()
        consume(BrsTokenType.RPAREN, "Expected ')' after Type() argument")
        return BrsTypeOf(value)
    }

    private fun parseArguments(): List<BrsExpression> {
        val args = mutableListOf<BrsExpression>()

        if (!check(BrsTokenType.RPAREN)) {
            do {
                args.add(expression())
            } while (match(BrsTokenType.COMMA))
        }

        return args
    }

    // ==================== Utilities ====================

    private fun isAtEnd(): Boolean = peek().type == BrsTokenType.EOF

    private fun peek(): BrsToken = tokens[current]

    private fun previous(): BrsToken = tokens[current - 1]

    private fun advance(): BrsToken {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun check(type: BrsTokenType): Boolean = !isAtEnd() && peek().type == type

    private fun match(vararg types: BrsTokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }
        return false
    }

    private fun consume(type: BrsTokenType, message: String): BrsToken {
        if (check(type)) return advance()
        throw error(message)
    }

    private fun consumeNewlineOrColon() {
        if (!match(BrsTokenType.NEWLINE) && !match(BrsTokenType.COLON)) {
            // Allow empty for end of input
            if (!isAtEnd()) {
                throw error("Expected newline or ':'")
            }
        }
        skipNewlines()
    }

    private fun skipNewlines() {
        while (match(BrsTokenType.NEWLINE) || match(BrsTokenType.COMMENT)) {
            // Skip
        }
    }

    private fun error(message: String): ParseException {
        val token = peek()
        return ParseException("Line ${token.line}:${token.column}: $message (got '${token.text}')")
    }

    private fun synchronize() {
        advance()
        while (!isAtEnd()) {
            if (previous().type == BrsTokenType.NEWLINE) return

            when (peek().type) {
                BrsTokenType.FUNCTION,
                BrsTokenType.SUB,
                BrsTokenType.IF,
                BrsTokenType.WHILE,
                BrsTokenType.FOR,
                BrsTokenType.RETURN,
                BrsTokenType.PRINT,
                BrsTokenType.TRY,
                BrsTokenType.END -> return
                else -> advance()
            }
        }
    }
}

class ParseException(message: String) : RuntimeException(message)

/**
 * Result of parsing BrightScript code.
 */
data class BrsParseResult<T>(
    val result: T,
    val errors: List<String>
) {
    val hasErrors: Boolean get() = errors.isNotEmpty()
}

/**
 * Parse BrightScript source code into an AST.
 */
fun parseBrightScript(source: String): BrsParseResult<BrsProgram> {
    val lexResult = tokenizeBrightScript(source)
    if (lexResult.hasErrors) {
        return BrsParseResult(BrsProgram(mutableListOf(), mutableListOf()), lexResult.errors)
    }
    val parser = BrsParser(lexResult.tokens)
    return parser.parseProgram()
}

/**
 * Parse BrightScript statements (for inline code).
 */
fun parseBrightScriptStatements(source: String): BrsParseResult<List<BrsStatement>> {
    val lexResult = tokenizeBrightScript(source)
    if (lexResult.hasErrors) {
        return BrsParseResult(emptyList(), lexResult.errors)
    }
    val parser = BrsParser(lexResult.tokens)
    return parser.parseStatements()
}

/**
 * Parse a single BrightScript expression.
 */
fun parseBrightScriptExpression(source: String): BrsParseResult<BrsExpression> {
    val lexResult = tokenizeBrightScript(source)
    if (lexResult.hasErrors) {
        return BrsParseResult(BrsInvalidLiteral(), lexResult.errors)
    }
    val parser = BrsParser(lexResult.tokens)
    return parser.parseExpression()
}
