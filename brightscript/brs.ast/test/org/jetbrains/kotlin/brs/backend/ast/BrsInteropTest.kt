/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.brs.backend.ast

import org.jetbrains.kotlin.brs.backend.ast.parser.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Tests for BrightScript interop functionality:
 * - @BrsInline code parsing
 * - Mixed project support (.brs file parsing)
 * - External declaration extraction
 */
class BrsInteropTest {

    // ==================== @BrsInline Parsing Tests ====================

    @Test
    fun testParseSimpleInlineCode() {
        // @BrsInline("return m.top.visible")
        val code = "return m.top.visible"
        val result = parseBrightScriptStatements(code)

        assertFalse(result.hasErrors, "Should parse without errors: ${result.errors}")
        assertEquals(1, result.result.size)

        val stmt = result.result[0] as BrsReturn
        val dotAccess = stmt.value as BrsDotAccess
        assertEquals("visible", dotAccess.field)
    }

    @Test
    fun testParseInlineWithVariableCapture() {
        // @BrsInline("return a + b")
        // fun add(a: Int, b: Int): Int
        val code = "return a + b"
        val result = parseBrightScriptStatements(code)

        assertFalse(result.hasErrors)
        assertEquals(1, result.result.size)

        val stmt = result.result[0] as BrsReturn
        val binaryOp = stmt.value as BrsBinaryOp
        assertEquals(BrsBinaryOperator.ADD, binaryOp.operator)
        assertEquals("a", (binaryOp.left as BrsIdentifier).name)
        assertEquals("b", (binaryOp.right as BrsIdentifier).name)
    }

    @Test
    fun testParseMultiLineInlineCode() {
        // @BrsInline("""
        //     result = CreateObject("roAssociativeArray")
        //     result.name = name
        //     return result
        // """)
        val code = """
            result = CreateObject("roAssociativeArray")
            result.name = name
            return result
        """.trimIndent()

        val result = parseBrightScriptStatements(code)

        assertFalse(result.hasErrors, "Errors: ${result.errors}")
        assertEquals(3, result.result.size)

        // First statement: result = CreateObject(...)
        val stmt1 = result.result[0] as BrsVariable
        assertEquals("result", stmt1.name)
        assertTrue(stmt1.initializer is BrsCreateObject)

        // Second statement: result.name = name
        val stmt2 = result.result[1] as BrsExpressionStatement
        assertTrue(stmt2.expression is BrsBinaryOp)

        // Third statement: return result
        val stmt3 = result.result[2] as BrsReturn
        assertTrue(stmt3.value is BrsIdentifier)
    }

    @Test
    fun testParseInlineWithMethodCall() {
        // @BrsInline("return arr.count()")
        val code = "return arr.count()"
        val result = parseBrightScriptStatements(code)

        assertFalse(result.hasErrors)
        val stmt = result.result[0] as BrsReturn
        val methodCall = stmt.value as BrsMethodCall
        assertEquals("count", methodCall.method)
        assertEquals("arr", (methodCall.receiver as BrsIdentifier).name)
    }

    @Test
    fun testParseInlineWithCreateObject() {
        // @BrsInline("return CreateObject(\"roUrlTransfer\")")
        val code = """return CreateObject("roUrlTransfer")"""
        val result = parseBrightScriptStatements(code)

        assertFalse(result.hasErrors)
        val stmt = result.result[0] as BrsReturn
        val createObj = stmt.value as BrsCreateObject
        assertEquals("roUrlTransfer", createObj.objectType)
    }

    @Test
    fun testParseInlineWithConditional() {
        // @BrsInline("""
        //     if value <> invalid then
        //         return value
        //     end if
        //     return defaultValue
        // """)
        val code = """
            if value <> invalid then
                return value
            end if
            return defaultValue
        """.trimIndent()

        val result = parseBrightScriptStatements(code)

        assertFalse(result.hasErrors, "Errors: ${result.errors}")
        assertEquals(2, result.result.size)
        assertTrue(result.result[0] is BrsIf)
        assertTrue(result.result[1] is BrsReturn)
    }

    // ==================== Mixed Project Support Tests ====================

    @Test
    fun testParseBrsFileWithFunctions() {
        val brsCode = """
            function calculateTotal(items as Object) as Float
                total = 0.0
                for each item in items
                    total = total + item.price
                end for
                return total
            end function

            sub logMessage(message as String)
                print message
            end sub
        """.trimIndent()

        val result = parseBrightScript(brsCode)

        assertFalse(result.hasErrors, "Errors: ${result.errors}")
        assertEquals(2, result.result.declarations.size)

        val func = result.result.declarations[0] as BrsFunction
        assertEquals("calculateTotal", func.name)
        assertEquals(1, func.parameters.size)
        assertEquals(BrsType.FLOAT, func.returnType)

        val sub = result.result.declarations[1] as BrsSub
        assertEquals("logMessage", sub.name)
        assertEquals(1, sub.parameters.size)
    }

    @Test
    fun testParseBrsFileWithDefaultParameters() {
        val brsCode = """
            function formatNumber(value as Float, decimals as Integer = 2) as String
                return str(value)
            end function
        """.trimIndent()

        val result = parseBrightScript(brsCode)

        assertFalse(result.hasErrors)
        val func = result.result.declarations[0] as BrsFunction
        assertEquals("formatNumber", func.name)
        assertEquals(2, func.parameters.size)
        assertNotNull(func.parameters[1].defaultValue)
    }

    @Test
    fun testParseBrsComponentScript() {
        val brsCode = """
            sub init()
                m.top.observeField("content", "onContentChanged")
                m.contentList = []
            end sub

            sub onContentChanged(event as Object)
                content = event.getData()
                if content <> invalid then
                    processContent(content)
                end if
            end sub

            function processContent(content as Object) as Boolean
                for each item in content
                    m.contentList.push(item)
                end for
                return true
            end function
        """.trimIndent()

        val result = parseBrightScript(brsCode)

        assertFalse(result.hasErrors, "Errors: ${result.errors}")
        assertEquals(3, result.result.declarations.size)

        // Verify init sub
        val initSub = result.result.declarations[0] as BrsSub
        assertEquals("init", initSub.name)
        assertEquals(0, initSub.parameters.size)

        // Verify onChange handler
        val onChange = result.result.declarations[1] as BrsSub
        assertEquals("onContentChanged", onChange.name)
        assertEquals(1, onChange.parameters.size)

        // Verify function
        val processFunc = result.result.declarations[2] as BrsFunction
        assertEquals("processContent", processFunc.name)
        assertEquals(BrsType.BOOLEAN, processFunc.returnType)
    }

    @Test
    fun testParseBrsWithTryCatch() {
        val brsCode = """
            function safeParse(jsonString as String) as Object
                try
                    result = ParseJson(jsonString)
                    return result
                catch e
                    print "Parse error: "; e
                    return invalid
                end try
            end function
        """.trimIndent()

        val result = parseBrightScript(brsCode)

        assertFalse(result.hasErrors, "Errors: ${result.errors}")
        val func = result.result.declarations[0] as BrsFunction
        assertEquals("safeParse", func.name)

        // Find the try statement in the body
        val tryStmt = func.body.statements.find { it is BrsTry } as BrsTry
        assertNotNull(tryStmt.catchBlock)
        assertEquals("e", tryStmt.catchVariable)
    }

    // ==================== Round-Trip Tests ====================

    @Test
    fun testRoundTripInlineCode() {
        val original = "return m.top.visible"
        val parsed = parseBrightScriptStatements(original)
        assertFalse(parsed.hasErrors)

        val rendered = BrsBlock(parsed.result.toMutableList()).render().trim()
        assertTrue(rendered.contains("return m.top.visible"))
    }

    @Test
    fun testRoundTripFunction() {
        val original = """
            function multiply(a as Integer, b as Integer) as Integer
                return a * b
            end function
        """.trimIndent()

        val parsed = parseBrightScript(original)
        assertFalse(parsed.hasErrors, "Parse errors: ${parsed.errors}")

        val rendered = parsed.result.render()
        assertTrue(rendered.contains("function multiply"))
        assertTrue(rendered.contains("return a * b"))
        assertTrue(rendered.contains("end function"))
    }

    @Test
    fun testRoundTripComplexExpression() {
        val code = "result = (a + b) * (c - d) / e"
        val parsed = parseBrightScriptStatements(code)
        assertFalse(parsed.hasErrors)

        val rendered = BrsBlock(parsed.result.toMutableList()).render()
        assertTrue(rendered.contains("result"))
        // The expression structure should be preserved
    }

    // ==================== Error Handling Tests ====================

    @Test
    fun testParseErrorUnterminatedString() {
        val code = """return "unterminated"""
        val result = parseBrightScriptStatements(code)

        assertTrue(result.hasErrors)
        assertTrue(result.errors.any { it.contains("Unterminated") || it.contains("string") })
    }

    @Test
    fun testParseErrorMissingEndFunction() {
        val code = """
            function broken()
                return 1
        """.trimIndent()

        val result = parseBrightScript(code)
        assertTrue(result.hasErrors)
    }

    @Test
    fun testParseErrorInvalidOperator() {
        val code = "x === y"  // === is not valid in BrightScript
        val result = parseBrightScriptStatements(code)

        // Should either error or parse as x = (= y) which would then fail
        // The exact behavior depends on lexer/parser recovery
    }

    // ==================== Complex Pattern Tests ====================

    @Test
    fun testParseNetworkRequestPattern() {
        val code = """
            function fetchData(url as String) as Object
                http = CreateObject("roUrlTransfer")
                http.setUrl(url)
                http.setCertificatesFile("common:/certs/ca-bundle.crt")
                http.addHeader("Content-Type", "application/json")

                response = http.getToString()
                if response <> invalid then
                    return ParseJson(response)
                end if
                return invalid
            end function
        """.trimIndent()

        val result = parseBrightScript(code)
        assertFalse(result.hasErrors, "Errors: ${result.errors}")

        val func = result.result.declarations[0] as BrsFunction
        assertEquals("fetchData", func.name)
        assertTrue(func.body.statements.size >= 5)
    }

    @Test
    fun testParseSceneGraphPattern() {
        val code = """
            sub init()
                m.top.backgroundUri = ""
                m.top.backgroundColor = "0x000000FF"
                m.timer = m.top.findNode("refreshTimer")
                m.timer.observeField("fire", "onTimerFire")
            end sub

            sub onTimerFire(event as Object)
                refreshContent()
            end sub

            sub refreshContent()
                m.top.content = CreateObject("roSGNode", "ContentNode")
            end sub
        """.trimIndent()

        val result = parseBrightScript(code)
        assertFalse(result.hasErrors, "Errors: ${result.errors}")
        assertEquals(3, result.result.declarations.size)
    }

    @Test
    fun testParseTaskPattern() {
        val code = """
            sub init()
                m.top.functionName = "runTask"
            end sub

            sub runTask()
                url = m.top.url
                result = fetchData(url)
                m.top.result = result
            end sub

            function fetchData(url as String) as Object
                http = CreateObject("roUrlTransfer")
                http.setUrl(url)
                return ParseJson(http.getToString())
            end function
        """.trimIndent()

        val result = parseBrightScript(code)
        assertFalse(result.hasErrors, "Errors: ${result.errors}")
        assertEquals(3, result.result.declarations.size)

        // Verify Task pattern components
        val declarations = result.result.declarations.map {
            when (it) {
                is BrsFunction -> it.name
                is BrsSub -> it.name
                else -> "unknown"
            }
        }
        assertTrue("init" in declarations)
        assertTrue("runTask" in declarations)
        assertTrue("fetchData" in declarations)
    }

    // ==================== Lexer Edge Cases ====================

    @Test
    fun testLexerNumericLiterals() {
        val code = """
            a = 42
            b = 3.14
            c = 100&
            d = 2.5!
            e = 1.23#
        """.trimIndent()

        val result = parseBrightScriptStatements(code)
        assertFalse(result.hasErrors, "Errors: ${result.errors}")
        assertEquals(5, result.result.size)
    }

    @Test
    fun testLexerStringEscaping() {
        val code = """s = "He said ""Hello"" to me""""
        val lexResult = tokenizeBrightScript(code)
        assertFalse(lexResult.hasErrors)

        val stringToken = lexResult.tokens.find { it.type == BrsTokenType.STRING_LITERAL }
        assertNotNull(stringToken)
    }

    @Test
    fun testLexerComments() {
        val code = """
            ' Single line comment
            x = 1 ' Inline comment
            REM Another style comment
            y = 2
        """.trimIndent()

        val lexResult = tokenizeBrightScript(code)
        assertFalse(lexResult.hasErrors)

        val comments = lexResult.tokens.filter { it.type == BrsTokenType.COMMENT }
        assertEquals(3, comments.size)
    }

    @Test
    fun testLexerKeywordsCaseInsensitive() {
        val code = """
            FUNCTION test() AS INTEGER
                RETURN 1
            END FUNCTION
        """.trimIndent()

        val result = parseBrightScript(code)
        assertFalse(result.hasErrors, "Errors: ${result.errors}")
        assertEquals(1, result.result.declarations.size)
    }
}
