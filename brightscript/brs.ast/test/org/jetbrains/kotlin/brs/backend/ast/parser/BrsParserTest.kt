/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.brs.backend.ast.parser

import org.jetbrains.kotlin.brs.backend.ast.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Tests for the BrightScript lexer and parser.
 */
class BrsParserTest {

    // ==================== Lexer Tests ====================

    @Test
    fun testLexerSimpleTokens() {
        val result = tokenizeBrightScript("+ - * / = <> < > <= >=")
        assertFalse(result.hasErrors, "Should have no errors: ${result.errors}")

        val types = result.tokens.map { it.type }
        assertTrue(BrsTokenType.PLUS in types)
        assertTrue(BrsTokenType.MINUS in types)
        assertTrue(BrsTokenType.STAR in types)
        assertTrue(BrsTokenType.SLASH in types)
        assertTrue(BrsTokenType.EQ in types)
        assertTrue(BrsTokenType.NE in types)
        assertTrue(BrsTokenType.LT in types)
        assertTrue(BrsTokenType.GT in types)
        assertTrue(BrsTokenType.LE in types)
        assertTrue(BrsTokenType.GE in types)
    }

    @Test
    fun testLexerKeywords() {
        val result = tokenizeBrightScript("function sub if then else for while return")
        assertFalse(result.hasErrors)

        val types = result.tokens.map { it.type }
        assertTrue(BrsTokenType.FUNCTION in types)
        assertTrue(BrsTokenType.SUB in types)
        assertTrue(BrsTokenType.IF in types)
        assertTrue(BrsTokenType.THEN in types)
        assertTrue(BrsTokenType.ELSE in types)
        assertTrue(BrsTokenType.FOR in types)
        assertTrue(BrsTokenType.WHILE in types)
        assertTrue(BrsTokenType.RETURN in types)
    }

    @Test
    fun testLexerLiterals() {
        val result = tokenizeBrightScript("""42 3.14 "hello" true false invalid""")
        assertFalse(result.hasErrors)

        val tokens = result.tokens.filter { it.type != BrsTokenType.EOF }
        assertEquals(6, tokens.size)
        assertEquals(BrsTokenType.INTEGER_LITERAL, tokens[0].type)
        assertEquals(42, tokens[0].value)
        assertEquals(BrsTokenType.FLOAT_LITERAL, tokens[1].type)
        assertEquals(BrsTokenType.STRING_LITERAL, tokens[2].type)
        assertEquals("hello", tokens[2].value)
        assertEquals(BrsTokenType.TRUE, tokens[3].type)
        assertEquals(BrsTokenType.FALSE, tokens[4].type)
        assertEquals(BrsTokenType.INVALID, tokens[5].type)
    }

    @Test
    fun testLexerNumericSuffixes() {
        val result = tokenizeBrightScript("100& 3.14! 2.718#")
        assertFalse(result.hasErrors)

        val tokens = result.tokens.filter { it.type != BrsTokenType.EOF && it.type != BrsTokenType.NEWLINE }
        assertEquals(BrsTokenType.LONG_INTEGER_LITERAL, tokens[0].type)
        assertEquals(100L, tokens[0].value)
        assertEquals(BrsTokenType.FLOAT_LITERAL, tokens[1].type)
        assertEquals(BrsTokenType.DOUBLE_LITERAL, tokens[2].type)
    }

    @Test
    fun testLexerComments() {
        val result = tokenizeBrightScript("""
            ' This is a comment
            x = 5 ' inline comment
            REM Another comment style
        """.trimIndent())
        assertFalse(result.hasErrors)

        val comments = result.tokens.filter { it.type == BrsTokenType.COMMENT }
        assertEquals(3, comments.size)
    }

    // ==================== Parser Expression Tests ====================

    @Test
    fun testParseSimpleExpression() {
        val result = parseBrightScriptExpression("1 + 2")
        assertFalse(result.hasErrors, "Errors: ${result.errors}")

        val expr = result.result
        assertTrue(expr is BrsBinaryOp)
        val binOp = expr as BrsBinaryOp
        assertEquals(BrsBinaryOperator.ADD, binOp.operator)
        assertTrue(binOp.left is BrsIntLiteral)
        assertTrue(binOp.right is BrsIntLiteral)
    }

    @Test
    fun testParseCompoundExpression() {
        val result = parseBrightScriptExpression("1 + 2 * 3")
        assertFalse(result.hasErrors)

        // Should be parsed as 1 + (2 * 3) due to precedence
        val expr = result.result as BrsBinaryOp
        assertEquals(BrsBinaryOperator.ADD, expr.operator)
        assertTrue(expr.left is BrsIntLiteral)
        assertTrue(expr.right is BrsBinaryOp)

        val rightOp = expr.right as BrsBinaryOp
        assertEquals(BrsBinaryOperator.MUL, rightOp.operator)
    }

    @Test
    fun testParseLogicalExpression() {
        val result = parseBrightScriptExpression("a and b or c")
        assertFalse(result.hasErrors)

        // Should be (a and b) or c
        val expr = result.result as BrsBinaryOp
        assertEquals(BrsBinaryOperator.OR, expr.operator)
        assertTrue(expr.left is BrsBinaryOp)
        assertTrue(expr.right is BrsIdentifier)
    }

    @Test
    fun testParseNotExpression() {
        val result = parseBrightScriptExpression("not x")
        assertFalse(result.hasErrors)

        val expr = result.result as BrsUnaryOp
        assertEquals(BrsUnaryOperator.NOT, expr.operator)
        assertTrue(expr.operand is BrsIdentifier)
    }

    @Test
    fun testParseFunctionCall() {
        val result = parseBrightScriptExpression("myFunc(1, 2, 3)")
        assertFalse(result.hasErrors)

        val expr = result.result as BrsFunctionCall
        assertTrue(expr.target is BrsIdentifier)
        assertEquals("myFunc", (expr.target as BrsIdentifier).name)
        assertEquals(3, expr.arguments.size)
    }

    @Test
    fun testParseMethodCall() {
        val result = parseBrightScriptExpression("obj.method(arg)")
        assertFalse(result.hasErrors)

        val expr = result.result as BrsMethodCall
        assertTrue(expr.receiver is BrsIdentifier)
        assertEquals("method", expr.method)
        assertEquals(1, expr.arguments.size)
    }

    @Test
    fun testParseDotAccess() {
        val result = parseBrightScriptExpression("obj.field")
        assertFalse(result.hasErrors)

        val expr = result.result as BrsDotAccess
        assertTrue(expr.target is BrsIdentifier)
        assertEquals("field", expr.field)
    }

    @Test
    fun testParseIndexAccess() {
        val result = parseBrightScriptExpression("arr[0]")
        assertFalse(result.hasErrors)

        val expr = result.result as BrsIndexAccess
        assertTrue(expr.target is BrsIdentifier)
        assertTrue(expr.index is BrsIntLiteral)
    }

    @Test
    fun testParseArrayLiteral() {
        val result = parseBrightScriptExpression("[1, 2, 3]")
        assertFalse(result.hasErrors)

        val expr = result.result as BrsArrayLiteral
        assertEquals(3, expr.elements.size)
    }

    @Test
    fun testParseAALiteral() {
        val result = parseBrightScriptExpression("""{ name: "test", value: 42 }""")
        assertFalse(result.hasErrors)

        val expr = result.result as BrsAALiteral
        assertEquals(2, expr.entries.size)
        assertEquals("name", expr.entries[0].key)
        assertEquals("value", expr.entries[1].key)
    }

    @Test
    fun testParseCreateObject() {
        val result = parseBrightScriptExpression("""CreateObject("roArray", 0, true)""")
        assertFalse(result.hasErrors)

        val expr = result.result as BrsCreateObject
        assertEquals("roArray", expr.objectType)
        assertEquals(2, expr.arguments.size)
    }

    @Test
    fun testParseMRef() {
        val result = parseBrightScriptExpression("m.top.visible")
        assertFalse(result.hasErrors)

        val expr = result.result as BrsDotAccess
        assertEquals("visible", expr.field)
        assertTrue(expr.target is BrsDotAccess)
        val innerAccess = expr.target as BrsDotAccess
        assertEquals("top", innerAccess.field)
        assertTrue(innerAccess.target is BrsMRef)
    }

    // ==================== Parser Statement Tests ====================

    @Test
    fun testParseVariableAssignment() {
        val result = parseBrightScriptStatements("x = 5")
        assertFalse(result.hasErrors, "Errors: ${result.errors}")

        assertEquals(1, result.result.size)
        val stmt = result.result[0] as BrsVariable
        assertEquals("x", stmt.name)
        assertTrue(stmt.initializer is BrsIntLiteral)
    }

    @Test
    fun testParseVariableWithType() {
        val result = parseBrightScriptStatements("x as Integer = 5")
        assertFalse(result.hasErrors)

        val stmt = result.result[0] as BrsVariable
        assertEquals("x", stmt.name)
        assertEquals(BrsType.INTEGER, stmt.type)
    }

    @Test
    fun testParseReturn() {
        val result = parseBrightScriptStatements("return 42")
        assertFalse(result.hasErrors)

        val stmt = result.result[0] as BrsReturn
        assertTrue(stmt.value is BrsIntLiteral)
    }

    @Test
    fun testParseIfStatement() {
        val code = """
            if x > 0 then
                print "positive"
            else
                print "non-positive"
            end if
        """.trimIndent()

        val result = parseBrightScriptStatements(code)
        assertFalse(result.hasErrors, "Errors: ${result.errors}")

        val stmt = result.result[0] as BrsIf
        assertTrue(stmt.condition is BrsBinaryOp)
        assertNotNull(stmt.elseBranch)
    }

    @Test
    fun testParseWhileLoop() {
        val code = """
            while i < 10
                i = i + 1
            end while
        """.trimIndent()

        val result = parseBrightScriptStatements(code)
        assertFalse(result.hasErrors, "Errors: ${result.errors}")

        val stmt = result.result[0] as BrsWhile
        assertTrue(stmt.condition is BrsBinaryOp)
        assertTrue(stmt.body is BrsBlock)
    }

    @Test
    fun testParseForLoop() {
        val code = """
            for i = 0 to 10
                print i
            end for
        """.trimIndent()

        val result = parseBrightScriptStatements(code)
        assertFalse(result.hasErrors, "Errors: ${result.errors}")

        val stmt = result.result[0] as BrsFor
        assertEquals("i", stmt.variable)
        assertTrue(stmt.start is BrsIntLiteral)
        assertTrue(stmt.end is BrsIntLiteral)
    }

    @Test
    fun testParseForEachLoop() {
        val code = """
            for each item in items
                print item
            end for
        """.trimIndent()

        val result = parseBrightScriptStatements(code)
        assertFalse(result.hasErrors, "Errors: ${result.errors}")

        val stmt = result.result[0] as BrsForEach
        assertEquals("item", stmt.variable)
        assertTrue(stmt.iterable is BrsIdentifier)
    }

    @Test
    fun testParseTryCatch() {
        val code = """
            try
                riskyOperation()
            catch e
                handleError(e)
            end try
        """.trimIndent()

        val result = parseBrightScriptStatements(code)
        assertFalse(result.hasErrors, "Errors: ${result.errors}")

        val stmt = result.result[0] as BrsTry
        assertNotNull(stmt.catchBlock)
        assertEquals("e", stmt.catchVariable)
    }

    // ==================== Parser Declaration Tests ====================

    @Test
    fun testParseFunction() {
        val code = """
            function add(a as Integer, b as Integer) as Integer
                return a + b
            end function
        """.trimIndent()

        val result = parseBrightScript(code)
        assertFalse(result.hasErrors, "Errors: ${result.errors}")

        assertEquals(1, result.result.declarations.size)
        val func = result.result.declarations[0] as BrsFunction
        assertEquals("add", func.name)
        assertEquals(2, func.parameters.size)
        assertEquals(BrsType.INTEGER, func.returnType)
    }

    @Test
    fun testParseSub() {
        val code = """
            sub greet(name as String)
                print "Hello, "; name
            end sub
        """.trimIndent()

        val result = parseBrightScript(code)
        assertFalse(result.hasErrors, "Errors: ${result.errors}")

        assertEquals(1, result.result.declarations.size)
        val sub = result.result.declarations[0] as BrsSub
        assertEquals("greet", sub.name)
        assertEquals(1, sub.parameters.size)
    }

    @Test
    fun testParseAnonymousFunction() {
        val result = parseBrightScriptExpression("""
            function(x)
                return x * 2
            end function
        """.trimIndent())
        assertFalse(result.hasErrors, "Errors: ${result.errors}")

        val func = result.result as BrsAnonymousFunction
        assertEquals(1, func.parameters.size)
        assertEquals("x", func.parameters[0].name)
    }

    // ==================== Round-trip Tests ====================

    @Test
    fun testRoundTripSimpleFunction() {
        val code = """
            function add(a as Integer, b as Integer) as Integer
                return a + b
            end function
        """.trimIndent()

        val parseResult = parseBrightScript(code)
        assertFalse(parseResult.hasErrors)

        val rendered = parseResult.result.render()
        assertTrue(rendered.contains("function add"))
        assertTrue(rendered.contains("return"))
        assertTrue(rendered.contains("end function"))
    }

    @Test
    fun testRoundTripComponentCode() {
        val code = """
            sub init()
                m.top.observeField("myField", "onMyFieldChanged")
                m.top.setField("status", "initialized")
            end sub

            sub onMyFieldChanged(event as Object)
                newValue = event.getData()
                print "Field changed to: "; newValue
            end sub
        """.trimIndent()

        val parseResult = parseBrightScript(code)
        assertFalse(parseResult.hasErrors, "Errors: ${parseResult.errors}")

        assertEquals(2, parseResult.result.declarations.size)

        val rendered = parseResult.result.render()
        assertTrue(rendered.contains("sub init()"))
        assertTrue(rendered.contains("m.top.observeField"))
        assertTrue(rendered.contains("sub onMyFieldChanged"))
    }
}
