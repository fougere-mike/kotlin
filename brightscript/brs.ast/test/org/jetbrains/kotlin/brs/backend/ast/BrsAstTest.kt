/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.brs.backend.ast

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Tests for the BrightScript AST and renderer.
 */
class BrsAstTest {

    @Test
    fun testIntLiteralRendering() {
        val literal = BrsIntLiteral(42)
        assertEquals("42", literal.render())
    }

    @Test
    fun testLongIntLiteralRendering() {
        val literal = BrsLongIntLiteral(9223372036854775807L)
        assertEquals("9223372036854775807&", literal.render())
    }

    @Test
    fun testFloatLiteralRendering() {
        val literal = BrsFloatLiteral(3.14f)
        assertTrue(literal.render().endsWith("!"))
    }

    @Test
    fun testDoubleLiteralRendering() {
        val literal = BrsDoubleLiteral(3.14159)
        assertTrue(literal.render().endsWith("#"))
    }

    @Test
    fun testStringLiteralRendering() {
        val literal = BrsStringLiteral("Hello, World!")
        assertEquals("\"Hello, World!\"", literal.render())
    }

    @Test
    fun testStringLiteralWithQuotesRendering() {
        val literal = BrsStringLiteral("Say \"Hello\"")
        assertEquals("\"Say \"\"Hello\"\"\"", literal.render())
    }

    @Test
    fun testBooleanLiteralRendering() {
        assertEquals("true", BrsBooleanLiteral(true).render())
        assertEquals("false", BrsBooleanLiteral(false).render())
    }

    @Test
    fun testInvalidLiteralRendering() {
        assertEquals("invalid", BrsInvalidLiteral().render())
    }

    @Test
    fun testIdentifierRendering() {
        val identifier = BrsIdentifier("myVariable")
        assertEquals("myVariable", identifier.render())
    }

    @Test
    fun testMRefRendering() {
        assertEquals("m", BrsMRef().render())
    }

    @Test
    fun testBinaryOperatorRendering() {
        val expr = BrsBinaryOp(
            BrsIntLiteral(1),
            BrsBinaryOperator.ADD,
            BrsIntLiteral(2)
        )
        assertEquals("1 + 2", expr.render())
    }

    @Test
    fun testUnaryOperatorRendering() {
        val negation = BrsUnaryOp(BrsUnaryOperator.NEG, BrsIntLiteral(5))
        assertEquals("-5", negation.render())

        val notOp = BrsUnaryOp(BrsUnaryOperator.NOT, BrsBooleanLiteral(true))
        assertEquals("not true", notOp.render())
    }

    @Test
    fun testFunctionCallRendering() {
        val call = BrsFunctionCall(
            BrsIdentifier("myFunc"),
            mutableListOf(BrsIntLiteral(1), BrsStringLiteral("hello"))
        )
        assertEquals("myFunc(1, \"hello\")", call.render())
    }

    @Test
    fun testMethodCallRendering() {
        val call = BrsMethodCall(
            BrsIdentifier("obj"),
            "doSomething",
            mutableListOf(BrsIntLiteral(42))
        )
        assertEquals("obj.doSomething(42)", call.render())
    }

    @Test
    fun testDotAccessRendering() {
        val access = BrsDotAccess(BrsIdentifier("obj"), "property")
        assertEquals("obj.property", access.render())
    }

    @Test
    fun testIndexAccessRendering() {
        val access = BrsIndexAccess(
            BrsIdentifier("arr"),
            BrsIntLiteral(0)
        )
        assertEquals("arr[0]", access.render())
    }

    @Test
    fun testArrayLiteralRendering() {
        val array = BrsArrayLiteral(
            mutableListOf(BrsIntLiteral(1), BrsIntLiteral(2), BrsIntLiteral(3))
        )
        assertEquals("[1, 2, 3]", array.render())
    }

    @Test
    fun testEmptyArrayLiteralRendering() {
        val array = BrsArrayLiteral()
        assertEquals("[]", array.render())
    }

    @Test
    fun testAALiteralRendering() {
        val aa = BrsAALiteral(
            mutableListOf(
                BrsAAEntry("name", BrsStringLiteral("John")),
                BrsAAEntry("age", BrsIntLiteral(30))
            )
        )
        assertEquals("{name: \"John\", age: 30}", aa.render())
    }

    @Test
    fun testEmptyAALiteralRendering() {
        val aa = BrsAALiteral()
        assertEquals("{}", aa.render())
    }

    @Test
    fun testCreateObjectRendering() {
        val createObj = BrsCreateObject(
            "roArray",
            mutableListOf(BrsIntLiteral(10), BrsBooleanLiteral(true))
        )
        assertEquals("CreateObject(\"roArray\", 10, true)", createObj.render())
    }

    @Test
    fun testTypeOfRendering() {
        val typeOf = BrsTypeOf(BrsIdentifier("myVar"))
        assertEquals("Type(myVar)", typeOf.render())
    }

    @Test
    fun testSimpleFunctionRendering() {
        val func = BrsFunction(
            name = "add",
            parameters = mutableListOf(
                BrsParameter("a", BrsType.INTEGER),
                BrsParameter("b", BrsType.INTEGER)
            ),
            returnType = BrsType.INTEGER,
            body = BrsBlock(mutableListOf(
                BrsReturn(BrsBinaryOp(
                    BrsIdentifier("a"),
                    BrsBinaryOperator.ADD,
                    BrsIdentifier("b")
                ))
            ))
        )
        val rendered = func.render()

        assertTrue(rendered.contains("function add(a as Integer, b as Integer) as Integer"))
        assertTrue(rendered.contains("return a + b"))
        assertTrue(rendered.contains("end function"))
    }

    @Test
    fun testSimpleSubRendering() {
        val sub = BrsSub(
            name = "greet",
            parameters = mutableListOf(
                BrsParameter("name", BrsType.STRING)
            ),
            body = BrsBlock(mutableListOf(
                BrsExpressionStatement(
                    BrsFunctionCall(
                        BrsIdentifier("print"),
                        mutableListOf(BrsIdentifier("name"))
                    )
                )
            ))
        )
        val rendered = sub.render()

        assertTrue(rendered.contains("sub greet(name as String)"))
        assertTrue(rendered.contains("print(name)"))
        assertTrue(rendered.contains("end sub"))
    }

    @Test
    fun testIfStatementRendering() {
        val ifStmt = BrsIf(
            condition = BrsBinaryOp(
                BrsIdentifier("x"),
                BrsBinaryOperator.GT,
                BrsIntLiteral(0)
            ),
            thenBranch = BrsBlock(mutableListOf(
                BrsReturn(BrsStringLiteral("positive"))
            )),
            elseBranch = BrsBlock(mutableListOf(
                BrsReturn(BrsStringLiteral("non-positive"))
            ))
        )
        val rendered = ifStmt.render()

        assertTrue(rendered.contains("if x > 0 then"))
        assertTrue(rendered.contains("return \"positive\""))
        assertTrue(rendered.contains("else"))
        assertTrue(rendered.contains("return \"non-positive\""))
        assertTrue(rendered.contains("end if"))
    }

    @Test
    fun testWhileLoopRendering() {
        val whileLoop = BrsWhile(
            condition = BrsBinaryOp(
                BrsIdentifier("i"),
                BrsBinaryOperator.LT,
                BrsIntLiteral(10)
            ),
            body = BrsBlock(mutableListOf(
                BrsExpressionStatement(
                    BrsBinaryOp(
                        BrsIdentifier("i"),
                        BrsBinaryOperator.EQ,
                        BrsBinaryOp(
                            BrsIdentifier("i"),
                            BrsBinaryOperator.ADD,
                            BrsIntLiteral(1)
                        )
                    )
                )
            ))
        )
        val rendered = whileLoop.render()

        assertTrue(rendered.contains("while i < 10"))
        assertTrue(rendered.contains("end while"))
    }

    @Test
    fun testForLoopRendering() {
        val forLoop = BrsFor(
            variable = "i",
            start = BrsIntLiteral(0),
            end = BrsIntLiteral(10),
            step = BrsIntLiteral(2),
            body = BrsBlock(mutableListOf(
                BrsExpressionStatement(
                    BrsFunctionCall(
                        BrsIdentifier("print"),
                        mutableListOf(BrsIdentifier("i"))
                    )
                )
            ))
        )
        val rendered = forLoop.render()

        assertTrue(rendered.contains("for i = 0 to 10 step 2"))
        assertTrue(rendered.contains("print(i)"))
        assertTrue(rendered.contains("end for"))
    }

    @Test
    fun testForEachRendering() {
        val forEach = BrsForEach(
            variable = "item",
            iterable = BrsIdentifier("collection"),
            body = BrsBlock(mutableListOf(
                BrsExpressionStatement(
                    BrsFunctionCall(
                        BrsIdentifier("process"),
                        mutableListOf(BrsIdentifier("item"))
                    )
                )
            ))
        )
        val rendered = forEach.render()

        assertTrue(rendered.contains("for each item in collection"))
        assertTrue(rendered.contains("end for"))
    }

    @Test
    fun testTryCatchRendering() {
        val tryCatch = BrsTry(
            tryBlock = BrsBlock(mutableListOf(
                BrsExpressionStatement(
                    BrsFunctionCall(
                        BrsIdentifier("riskyOperation"),
                        mutableListOf()
                    )
                )
            )),
            catchVariable = "e",
            catchBlock = BrsBlock(mutableListOf(
                BrsExpressionStatement(
                    BrsFunctionCall(
                        BrsIdentifier("print"),
                        mutableListOf(BrsIdentifier("e"))
                    )
                )
            ))
        )
        val rendered = tryCatch.render()

        assertTrue(rendered.contains("try"))
        assertTrue(rendered.contains("riskyOperation()"))
        assertTrue(rendered.contains("catch e"))
        assertTrue(rendered.contains("end try"))
    }

    @Test
    fun testThrowRendering() {
        val throwStmt = BrsThrow(BrsStringLiteral("Error occurred"))
        assertEquals("throw \"Error occurred\"", throwStmt.render().trim())
    }

    @Test
    fun testExitForRendering() {
        val exit = BrsExit(BrsExitKind.FOR)
        assertEquals("exit for", exit.render().trim())
    }

    @Test
    fun testExitWhileRendering() {
        val exit = BrsExit(BrsExitKind.WHILE)
        assertEquals("exit while", exit.render().trim())
    }

    @Test
    fun testContinueForRendering() {
        val cont = BrsContinue(BrsContinueKind.FOR)
        assertEquals("continue for", cont.render().trim())
    }

    @Test
    fun testCommentRendering() {
        val comment = BrsComment("This is a comment")
        assertEquals("' This is a comment", comment.render().trim())

        val remComment = BrsComment("This is a REM comment", isRem = true)
        assertEquals("REM This is a REM comment", remComment.render().trim())
    }

    @Test
    fun testVariableDeclarationRendering() {
        val variable = BrsVariable(
            name = "count",
            type = BrsType.INTEGER,
            initializer = BrsIntLiteral(0)
        )
        assertEquals("count as Integer = 0", variable.render().trim())
    }

    @Test
    fun testAnonymousFunctionRendering() {
        val anonFunc = BrsAnonymousFunction(
            parameters = mutableListOf(
                BrsParameter("x"),
                BrsParameter("y")
            ),
            returnType = BrsType.INTEGER,
            body = BrsBlock(mutableListOf(
                BrsReturn(BrsBinaryOp(
                    BrsIdentifier("x"),
                    BrsBinaryOperator.ADD,
                    BrsIdentifier("y")
                ))
            ))
        )
        val rendered = anonFunc.render()

        assertTrue(rendered.contains("function(x, y) as Integer"))
        assertTrue(rendered.contains("return x + y"))
        assertTrue(rendered.contains("end function"))
    }

    @Test
    fun testDefaultParameterRendering() {
        val param = BrsParameter(
            name = "timeout",
            type = BrsType.INTEGER,
            defaultValue = BrsIntLiteral(1000)
        )
        val func = BrsFunction(
            name = "fetch",
            parameters = mutableListOf(param),
            returnType = BrsType.OBJECT,
            body = BrsBlock()
        )
        val rendered = func.render()

        assertTrue(rendered.contains("timeout as Integer = 1000"))
    }

    @Test
    fun testProgramRendering() {
        val program = BrsProgram(
            declarations = mutableListOf(
                BrsFunction(
                    name = "main",
                    parameters = mutableListOf(),
                    returnType = BrsType.VOID,
                    body = BrsBlock(mutableListOf(
                        BrsExpressionStatement(
                            BrsFunctionCall(
                                BrsIdentifier("print"),
                                mutableListOf(BrsStringLiteral("Hello, Roku!"))
                            )
                        )
                    ))
                )
            )
        )
        val rendered = program.render()

        assertTrue(rendered.contains("function main()"))
        assertTrue(rendered.contains("print(\"Hello, Roku!\")"))
        assertTrue(rendered.contains("end function"))
    }

    @Test
    fun testDeepCopy() {
        val original = BrsBinaryOp(
            BrsIntLiteral(1),
            BrsBinaryOperator.ADD,
            BrsIntLiteral(2)
        )
        val copy = original.deepCopy()

        // Modify original
        (original.left as BrsIntLiteral).value = 99

        // Copy should be unchanged
        assertEquals(1, (copy.left as BrsIntLiteral).value)
    }

    @Test
    fun testSourceLocation() {
        val literal = BrsIntLiteral(42)
        literal.source = BrsLocation("test.kt", 10, 5, 10, 7)

        assertNotNull(literal.source)
        assertEquals("test.kt", literal.source!!.file)
        assertEquals(10, literal.source!!.startLine)
        assertEquals(5, literal.source!!.startColumn)
    }

    @Test
    fun testComplexExpressionRendering() {
        // Test: m.top.field[0].method("arg")
        val expr = BrsMethodCall(
            receiver = BrsIndexAccess(
                target = BrsDotAccess(
                    target = BrsDotAccess(BrsMRef(), "top"),
                    field = "field"
                ),
                index = BrsIntLiteral(0)
            ),
            method = "method",
            arguments = mutableListOf(BrsStringLiteral("arg"))
        )
        assertEquals("m.top.field[0].method(\"arg\")", expr.render())
    }
}
