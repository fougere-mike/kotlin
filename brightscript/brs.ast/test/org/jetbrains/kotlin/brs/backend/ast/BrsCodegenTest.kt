/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.brs.backend.ast

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Tests for BrightScript code generation patterns.
 *
 * These tests verify that the BrightScript AST can represent
 * the code patterns needed for Kotlin-to-BrightScript compilation.
 */
class BrsCodegenTest {

    @Test
    fun testSimpleFunctionGeneration() {
        // Test: fun add(a: Int, b: Int): Int = a + b
        val function = BrsFunction(
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

        val rendered = function.render()
        assertTrue(rendered.contains("function add(a as Integer, b as Integer) as Integer"))
        assertTrue(rendered.contains("return a + b"))
        assertTrue(rendered.contains("end function"))
    }

    @Test
    fun testSubGeneration() {
        // Test: fun greet(name: String): Unit
        val sub = BrsSub(
            name = "greet",
            parameters = mutableListOf(
                BrsParameter("name", BrsType.STRING)
            ),
            body = BrsBlock(mutableListOf(
                BrsExpressionStatement(BrsFunctionCall(
                    BrsIdentifier("print"),
                    mutableListOf(BrsBinaryOp(
                        BrsStringLiteral("Hello, "),
                        BrsBinaryOperator.CONCAT,
                        BrsIdentifier("name")
                    ))
                ))
            ))
        )

        val rendered = sub.render()
        assertTrue(rendered.contains("sub greet(name as String)"))
        assertTrue(rendered.contains("print("))
        assertTrue(rendered.contains("end sub"))
    }

    @Test
    fun testClassConstructorPattern() {
        // Test: class Person(val name: String)
        // Should generate: function Person_create(name as String) as Object
        val constructor = BrsFunction(
            name = "Person_create",
            parameters = mutableListOf(
                BrsParameter("name", BrsType.STRING)
            ),
            returnType = BrsType.OBJECT,
            body = BrsBlock(mutableListOf(
                BrsVariable("this", BrsType.OBJECT, BrsAALiteral()),
                BrsExpressionStatement(BrsBinaryOp(
                    BrsDotAccess(BrsIdentifier("this"), "name"),
                    BrsBinaryOperator.EQ,
                    BrsIdentifier("name")
                )),
                BrsReturn(BrsIdentifier("this"))
            ))
        )

        val rendered = constructor.render()
        assertTrue(rendered.contains("function Person_create(name as String) as Object"))
        assertTrue(rendered.contains("this = {}"))
        assertTrue(rendered.contains("this.name = name"))
        assertTrue(rendered.contains("return this"))
    }

    @Test
    fun testIfElseGeneration() {
        // Test: if (x > 0) "positive" else "non-positive"
        val ifStatement = BrsIf(
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

        val rendered = ifStatement.render()
        assertTrue(rendered.contains("if x > 0 then"))
        assertTrue(rendered.contains("return \"positive\""))
        assertTrue(rendered.contains("else"))
        assertTrue(rendered.contains("return \"non-positive\""))
        assertTrue(rendered.contains("end if"))
    }

    @Test
    fun testWhileLoopGeneration() {
        // Test: while (i < 10) { print(i); i++ }
        val whileLoop = BrsWhile(
            condition = BrsBinaryOp(
                BrsIdentifier("i"),
                BrsBinaryOperator.LT,
                BrsIntLiteral(10)
            ),
            body = BrsBlock(mutableListOf(
                BrsExpressionStatement(BrsFunctionCall(
                    BrsIdentifier("print"),
                    mutableListOf(BrsIdentifier("i"))
                )),
                BrsExpressionStatement(BrsBinaryOp(
                    BrsIdentifier("i"),
                    BrsBinaryOperator.EQ,
                    BrsBinaryOp(BrsIdentifier("i"), BrsBinaryOperator.ADD, BrsIntLiteral(1))
                ))
            ))
        )

        val rendered = whileLoop.render()
        assertTrue(rendered.contains("while i < 10"))
        assertTrue(rendered.contains("print(i)"))
        assertTrue(rendered.contains("end while"))
    }

    @Test
    fun testForLoopGeneration() {
        // Test: for (i in 0..10)
        val forLoop = BrsFor(
            variable = "i",
            start = BrsIntLiteral(0),
            end = BrsIntLiteral(10),
            step = null,
            body = BrsBlock(mutableListOf(
                BrsExpressionStatement(BrsFunctionCall(
                    BrsIdentifier("print"),
                    mutableListOf(BrsIdentifier("i"))
                ))
            ))
        )

        val rendered = forLoop.render()
        assertTrue(rendered.contains("for i = 0 to 10"))
        assertTrue(rendered.contains("print(i)"))
        assertTrue(rendered.contains("end for"))
    }

    @Test
    fun testForEachGeneration() {
        // Test: for (item in items)
        val forEach = BrsForEach(
            variable = "item",
            iterable = BrsIdentifier("items"),
            body = BrsBlock(mutableListOf(
                BrsExpressionStatement(BrsFunctionCall(
                    BrsIdentifier("print"),
                    mutableListOf(BrsIdentifier("item"))
                ))
            ))
        )

        val rendered = forEach.render()
        assertTrue(rendered.contains("for each item in items"))
        assertTrue(rendered.contains("print(item)"))
        assertTrue(rendered.contains("end for"))
    }

    @Test
    fun testBinaryOperators() {
        val operators = listOf(
            BrsBinaryOperator.ADD to "+",
            BrsBinaryOperator.SUB to "-",
            BrsBinaryOperator.MUL to "*",
            BrsBinaryOperator.DIV to "/",
            BrsBinaryOperator.MOD to "mod",
            BrsBinaryOperator.LT to "<",
            BrsBinaryOperator.GT to ">",
            BrsBinaryOperator.LE to "<=",
            BrsBinaryOperator.GE to ">=",
            BrsBinaryOperator.EQ to "=",
            BrsBinaryOperator.NE to "<>",
            BrsBinaryOperator.AND to "and",
            BrsBinaryOperator.OR to "or"
        )

        for ((op, symbol) in operators) {
            val expr = BrsBinaryOp(BrsIdentifier("a"), op, BrsIdentifier("b"))
            assertEquals("a $symbol b", expr.render())
        }
    }

    @Test
    fun testUnaryOperators() {
        val neg = BrsUnaryOp(BrsUnaryOperator.NEG, BrsIntLiteral(5))
        assertEquals("-5", neg.render())

        val not = BrsUnaryOp(BrsUnaryOperator.NOT, BrsBooleanLiteral(true))
        assertEquals("not true", not.render())
    }

    @Test
    fun testArrayLiteral() {
        val array = BrsArrayLiteral(mutableListOf(
            BrsIntLiteral(1),
            BrsIntLiteral(2),
            BrsIntLiteral(3)
        ))
        assertEquals("[1, 2, 3]", array.render())
    }

    @Test
    fun testAssociativeArrayLiteral() {
        val aa = BrsAALiteral(mutableListOf(
            BrsAAEntry("name", BrsStringLiteral("test")),
            BrsAAEntry("value", BrsIntLiteral(42))
        ))
        val rendered = aa.render()
        assertTrue(rendered.contains("name: \"test\""), "Should contain name entry")
        assertTrue(rendered.contains("value: 42"), "Should contain value entry")
    }

    @Test
    fun testMethodCall() {
        // Test: obj.method(arg)
        val call = BrsMethodCall(
            BrsIdentifier("obj"),
            "method",
            mutableListOf(BrsIdentifier("arg"))
        )
        assertEquals("obj.method(arg)", call.render())
    }

    @Test
    fun testCreateObject() {
        // Test: CreateObject("roArray")
        val createObj = BrsCreateObject("roArray", mutableListOf())
        assertEquals("CreateObject(\"roArray\")", createObj.render())

        // Test with arguments: CreateObject("roUrlTransfer")
        val createObj2 = BrsCreateObject("roUrlTransfer", mutableListOf())
        assertEquals("CreateObject(\"roUrlTransfer\")", createObj2.render())
    }

    @Test
    fun testTryCatch() {
        // Test: try { riskyOperation() } catch e { handleError(e) }
        val tryCatch = BrsTry(
            tryBlock = BrsBlock(mutableListOf(
                BrsExpressionStatement(BrsFunctionCall(
                    BrsIdentifier("riskyOperation"),
                    mutableListOf()
                ))
            )),
            catchVariable = "e",
            catchBlock = BrsBlock(mutableListOf(
                BrsExpressionStatement(BrsFunctionCall(
                    BrsIdentifier("handleError"),
                    mutableListOf(BrsIdentifier("e"))
                ))
            ))
        )

        val rendered = tryCatch.render()
        assertTrue(rendered.contains("try"))
        assertTrue(rendered.contains("riskyOperation()"))
        assertTrue(rendered.contains("catch e"))
        assertTrue(rendered.contains("handleError(e)"))
        assertTrue(rendered.contains("end try"))
    }

    @Test
    fun testAnonymousFunction() {
        // Test: function(x) return x * 2 end function
        val lambda = BrsAnonymousFunction(
            parameters = mutableListOf(BrsParameter("x", BrsType.INTEGER)),
            returnType = BrsType.INTEGER,
            body = BrsBlock(mutableListOf(
                BrsReturn(BrsBinaryOp(
                    BrsIdentifier("x"),
                    BrsBinaryOperator.MUL,
                    BrsIntLiteral(2)
                ))
            ))
        )

        val rendered = lambda.render()
        assertTrue(rendered.contains("function(x as Integer) as Integer"))
        assertTrue(rendered.contains("return x * 2"))
        assertTrue(rendered.contains("end function"))
    }

    @Test
    fun testProgramGeneration() {
        // Test complete program with multiple functions
        val program = BrsProgram(
            declarations = mutableListOf(
                BrsFunction(
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
                ),
                BrsSub(
                    name = "main",
                    parameters = mutableListOf(),
                    body = BrsBlock(mutableListOf(
                        BrsVariable("result", BrsType.INTEGER,
                            BrsFunctionCall(BrsIdentifier("add"), mutableListOf(
                                BrsIntLiteral(1),
                                BrsIntLiteral(2)
                            ))
                        ),
                        BrsExpressionStatement(BrsFunctionCall(
                            BrsIdentifier("print"),
                            mutableListOf(BrsIdentifier("result"))
                        ))
                    ))
                )
            ),
            statements = mutableListOf()
        )

        val rendered = program.render()
        assertTrue(rendered.contains("function add(a as Integer, b as Integer) as Integer"))
        assertTrue(rendered.contains("sub main()"))
        assertTrue(rendered.contains("result = add(1, 2)"))
        assertTrue(rendered.contains("print(result)"))
    }

    // ==================== SceneGraph Component Patterns ====================

    @Test
    fun testComponentInitPattern() {
        // Test: SceneGraph init() sub with field observers
        // This is the pattern used for @BrsComponent classes
        val initSub = BrsSub(
            name = "init",
            parameters = mutableListOf(),
            body = BrsBlock(mutableListOf(
                // m.top.observeField("myField", "onMyFieldChanged")
                BrsExpressionStatement(
                    BrsMethodCall(
                        BrsDotAccess(BrsMRef(), "top"),
                        "observeField",
                        mutableListOf(
                            BrsStringLiteral("myField"),
                            BrsStringLiteral("onMyFieldChanged")
                        )
                    )
                ),
                // m.top.setField("status", "initialized")
                BrsExpressionStatement(
                    BrsMethodCall(
                        BrsDotAccess(BrsMRef(), "top"),
                        "setField",
                        mutableListOf(
                            BrsStringLiteral("status"),
                            BrsStringLiteral("initialized")
                        )
                    )
                )
            ))
        )

        val rendered = initSub.render()
        assertTrue(rendered.contains("sub init()"))
        assertTrue(rendered.contains("m.top.observeField(\"myField\", \"onMyFieldChanged\")"))
        assertTrue(rendered.contains("m.top.setField(\"status\", \"initialized\")"))
        assertTrue(rendered.contains("end sub"))
    }

    @Test
    fun testOnChangeHandlerPattern() {
        // Test: onChange handler function pattern
        // sub onMyFieldChanged(event as Object)
        val handler = BrsSub(
            name = "onMyFieldChanged",
            parameters = mutableListOf(
                BrsParameter("event", BrsType.OBJECT)
            ),
            body = BrsBlock(mutableListOf(
                // newValue = event.getData()
                BrsVariable(
                    name = "newValue",
                    initializer = BrsMethodCall(
                        BrsIdentifier("event"),
                        "getData",
                        mutableListOf()
                    )
                ),
                // print "Field changed to: "; newValue
                BrsExpressionStatement(
                    BrsFunctionCall(
                        BrsIdentifier("print"),
                        mutableListOf(
                            BrsStringLiteral("Field changed to: "),
                            BrsIdentifier("newValue")
                        )
                    )
                )
            ))
        )

        val rendered = handler.render()
        assertTrue(rendered.contains("sub onMyFieldChanged(event as Object)"))
        assertTrue(rendered.contains("newValue = event.getData()"))
        assertTrue(rendered.contains("end sub"))
    }

    @Test
    fun testCreateNodePattern() {
        // Test: CreateObject("roSGNode", "ComponentName")
        val createNode = BrsCreateObject(
            "roSGNode",
            mutableListOf(BrsStringLiteral("MyComponent"))
        )
        assertEquals("CreateObject(\"roSGNode\", \"MyComponent\")", createNode.render())
    }

    @Test
    fun testComponentFieldAccess() {
        // Test: m.top.fieldName
        val fieldAccess = BrsDotAccess(
            BrsDotAccess(BrsMRef(), "top"),
            "fieldName"
        )
        assertEquals("m.top.fieldName", fieldAccess.render())
    }

    @Test
    fun testTaskNodePattern() {
        // Test: Task node pattern with run function
        // sub runTask()
        //     result = doAsyncWork()
        //     m.top.result = result
        // end sub
        val taskRun = BrsSub(
            name = "runTask",
            parameters = mutableListOf(),
            body = BrsBlock(mutableListOf(
                BrsVariable(
                    name = "result",
                    initializer = BrsFunctionCall(
                        BrsIdentifier("doAsyncWork"),
                        mutableListOf()
                    )
                ),
                BrsExpressionStatement(
                    BrsBinaryOp(
                        BrsDotAccess(BrsDotAccess(BrsMRef(), "top"), "result"),
                        BrsBinaryOperator.EQ,
                        BrsIdentifier("result")
                    )
                )
            ))
        )

        val rendered = taskRun.render()
        assertTrue(rendered.contains("sub runTask()"))
        assertTrue(rendered.contains("result = doAsyncWork()"))
        assertTrue(rendered.contains("m.top.result = result"))
        assertTrue(rendered.contains("end sub"))
    }

    @Test
    fun testInheritanceChainPattern() {
        // Test: Class with inheritance pattern (__proto chain)
        // function Child_create(name as String) as Object
        //     this = Parent_create(name)
        //     this._super = {}
        //     this._super.greet = this.greet
        //     this.__proto = ["Child", this.__proto]
        //     this.__type = "Child"
        //     this.greet = Child_greet
        //     return this
        // end function
        val constructor = BrsFunction(
            name = "Child_create",
            parameters = mutableListOf(
                BrsParameter("name", BrsType.STRING)
            ),
            returnType = BrsType.OBJECT,
            body = BrsBlock(mutableListOf(
                // this = Parent_create(name)
                BrsVariable(
                    name = "this",
                    initializer = BrsFunctionCall(
                        BrsIdentifier("Parent_create"),
                        mutableListOf(BrsIdentifier("name"))
                    )
                ),
                // this._super = {}
                BrsExpressionStatement(
                    BrsBinaryOp(
                        BrsDotAccess(BrsIdentifier("this"), "_super"),
                        BrsBinaryOperator.EQ,
                        BrsAALiteral()
                    )
                ),
                // this._super.greet = this.greet
                BrsExpressionStatement(
                    BrsBinaryOp(
                        BrsDotAccess(BrsDotAccess(BrsIdentifier("this"), "_super"), "greet"),
                        BrsBinaryOperator.EQ,
                        BrsDotAccess(BrsIdentifier("this"), "greet")
                    )
                ),
                // this.__proto = ["Child", this.__proto]
                BrsExpressionStatement(
                    BrsBinaryOp(
                        BrsDotAccess(BrsIdentifier("this"), "__proto"),
                        BrsBinaryOperator.EQ,
                        BrsArrayLiteral(mutableListOf(
                            BrsStringLiteral("Child"),
                            BrsDotAccess(BrsIdentifier("this"), "__proto")
                        ))
                    )
                ),
                // this.__type = "Child"
                BrsExpressionStatement(
                    BrsBinaryOp(
                        BrsDotAccess(BrsIdentifier("this"), "__type"),
                        BrsBinaryOperator.EQ,
                        BrsStringLiteral("Child")
                    )
                ),
                // this.greet = Child_greet
                BrsExpressionStatement(
                    BrsBinaryOp(
                        BrsDotAccess(BrsIdentifier("this"), "greet"),
                        BrsBinaryOperator.EQ,
                        BrsIdentifier("Child_greet")
                    )
                ),
                // return this
                BrsReturn(BrsIdentifier("this"))
            ))
        )

        val rendered = constructor.render()
        assertTrue(rendered.contains("function Child_create(name as String) as Object"))
        assertTrue(rendered.contains("this = Parent_create(name)"))
        assertTrue(rendered.contains("this._super = {}"))
        assertTrue(rendered.contains("this._super.greet = this.greet"))
        assertTrue(rendered.contains("this.__type = \"Child\""))
        assertTrue(rendered.contains("this.greet = Child_greet"))
        assertTrue(rendered.contains("return this"))
    }
}
