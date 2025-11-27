/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.brs.backend.ast

/**
 * Simple verification of BrightScript AST functionality.
 *
 * This can be run standalone to verify the AST rendering works correctly.
 * Run with: kotlinc -script BrsAstVerification.kt
 * Or simply verify compilation works.
 */
object BrsAstVerification {

    private var passCount = 0
    private var failCount = 0

    private fun check(name: String, expected: String, actual: String) {
        if (expected == actual) {
            println("✓ $name")
            passCount++
        } else {
            println("✗ $name")
            println("  Expected: $expected")
            println("  Actual:   $actual")
            failCount++
        }
    }

    private fun checkContains(name: String, text: String, vararg substrings: String) {
        val missing = substrings.filter { it !in text }
        if (missing.isEmpty()) {
            println("✓ $name")
            passCount++
        } else {
            println("✗ $name")
            println("  Missing: ${missing.joinToString(", ")}")
            println("  In text: $text")
            failCount++
        }
    }

    fun runAllTests() {
        println("=" .repeat(60))
        println("BrightScript AST Verification")
        println("=" .repeat(60))
        println()

        // Literal tests
        println("--- Literals ---")
        check("Int literal", "42", BrsIntLiteral(42).render())
        check("Long literal", "9223372036854775807&", BrsLongIntLiteral(9223372036854775807L).render())
        check("Boolean true", "true", BrsBooleanLiteral(true).render())
        check("Boolean false", "false", BrsBooleanLiteral(false).render())
        check("String literal", "\"Hello\"", BrsStringLiteral("Hello").render())
        check("String with quotes", "\"Say \"\"Hi\"\"\"", BrsStringLiteral("Say \"Hi\"").render())
        check("Invalid literal", "invalid", BrsInvalidLiteral().render())

        println()
        println("--- Identifiers & References ---")
        check("Identifier", "myVar", BrsIdentifier("myVar").render())
        check("M reference", "m", BrsMRef().render())

        println()
        println("--- Operators ---")
        check("Binary add", "1 + 2", BrsBinaryOp(
            BrsIntLiteral(1), BrsBinaryOperator.ADD, BrsIntLiteral(2)
        ).render())
        check("Binary compare", "x > 0", BrsBinaryOp(
            BrsIdentifier("x"), BrsBinaryOperator.GT, BrsIntLiteral(0)
        ).render())
        check("Unary negation", "-5", BrsUnaryOp(
            BrsUnaryOperator.NEG, BrsIntLiteral(5)
        ).render())
        check("Unary not", "not true", BrsUnaryOp(
            BrsUnaryOperator.NOT, BrsBooleanLiteral(true)
        ).render())

        println()
        println("--- Function Calls ---")
        check("Simple call", "print(\"hello\")", BrsFunctionCall(
            BrsIdentifier("print"),
            mutableListOf(BrsStringLiteral("hello"))
        ).render())
        check("Method call", "obj.doIt(42)", BrsMethodCall(
            BrsIdentifier("obj"), "doIt", mutableListOf(BrsIntLiteral(42))
        ).render())
        check("CreateObject", "CreateObject(\"roArray\", 10, true)", BrsCreateObject(
            "roArray", mutableListOf(BrsIntLiteral(10), BrsBooleanLiteral(true))
        ).render())

        println()
        println("--- Access Expressions ---")
        check("Dot access", "obj.field", BrsDotAccess(
            BrsIdentifier("obj"), "field"
        ).render())
        check("Index access", "arr[0]", BrsIndexAccess(
            BrsIdentifier("arr"), BrsIntLiteral(0)
        ).render())
        check("Complex access", "m.top.field[0]", BrsIndexAccess(
            BrsDotAccess(BrsDotAccess(BrsMRef(), "top"), "field"),
            BrsIntLiteral(0)
        ).render())

        println()
        println("--- Collections ---")
        check("Empty array", "[]", BrsArrayLiteral().render())
        check("Array", "[1, 2, 3]", BrsArrayLiteral(
            mutableListOf(BrsIntLiteral(1), BrsIntLiteral(2), BrsIntLiteral(3))
        ).render())
        check("Empty AA", "{}", BrsAALiteral().render())
        check("AA", "{name: \"John\", age: 30}", BrsAALiteral(
            mutableListOf(
                BrsAAEntry("name", BrsStringLiteral("John")),
                BrsAAEntry("age", BrsIntLiteral(30))
            )
        ).render())

        println()
        println("--- Statements ---")
        check("Return", "return 42", BrsReturn(BrsIntLiteral(42)).render().trim())
        check("Return void", "return", BrsReturn().render().trim())
        check("Exit for", "exit for", BrsExit(BrsExitKind.FOR).render().trim())
        check("Exit while", "exit while", BrsExit(BrsExitKind.WHILE).render().trim())
        check("Continue for", "continue for", BrsContinue(BrsContinueKind.FOR).render().trim())
        check("Throw", "throw \"Error\"", BrsThrow(BrsStringLiteral("Error")).render().trim())
        check("Comment", "' My comment", BrsComment("My comment").render().trim())
        check("REM comment", "REM My comment", BrsComment("My comment", isRem = true).render().trim())

        println()
        println("--- Functions ---")
        val simpleFunc = BrsFunction(
            "add",
            mutableListOf(
                BrsParameter("a", BrsType.INTEGER),
                BrsParameter("b", BrsType.INTEGER)
            ),
            BrsType.INTEGER,
            BrsBlock(mutableListOf(
                BrsReturn(BrsBinaryOp(
                    BrsIdentifier("a"),
                    BrsBinaryOperator.ADD,
                    BrsIdentifier("b")
                ))
            ))
        )
        checkContains("Function",
            simpleFunc.render(),
            "function add(a as Integer, b as Integer) as Integer",
            "return a + b",
            "end function"
        )

        val simpleSub = BrsSub(
            "greet",
            mutableListOf(BrsParameter("name", BrsType.STRING)),
            BrsBlock(mutableListOf(
                BrsExpressionStatement(BrsFunctionCall(
                    BrsIdentifier("print"), mutableListOf(BrsIdentifier("name"))
                ))
            ))
        )
        checkContains("Sub",
            simpleSub.render(),
            "sub greet(name as String)",
            "print(name)",
            "end sub"
        )

        println()
        println("--- Control Flow ---")
        val ifStmt = BrsIf(
            BrsBinaryOp(BrsIdentifier("x"), BrsBinaryOperator.GT, BrsIntLiteral(0)),
            BrsBlock(mutableListOf(BrsReturn(BrsStringLiteral("positive")))),
            BrsBlock(mutableListOf(BrsReturn(BrsStringLiteral("non-positive"))))
        )
        checkContains("If/else",
            ifStmt.render(),
            "if x > 0 then",
            "return \"positive\"",
            "else",
            "return \"non-positive\"",
            "end if"
        )

        val whileLoop = BrsWhile(
            BrsBinaryOp(BrsIdentifier("i"), BrsBinaryOperator.LT, BrsIntLiteral(10)),
            BrsBlock(mutableListOf(
                BrsExpressionStatement(BrsFunctionCall(
                    BrsIdentifier("print"), mutableListOf(BrsIdentifier("i"))
                ))
            ))
        )
        checkContains("While loop",
            whileLoop.render(),
            "while i < 10",
            "print(i)",
            "end while"
        )

        val forLoop = BrsFor(
            "i",
            BrsIntLiteral(0),
            BrsIntLiteral(10),
            BrsIntLiteral(2),
            BrsBlock()
        )
        checkContains("For loop",
            forLoop.render(),
            "for i = 0 to 10 step 2",
            "end for"
        )

        val forEach = BrsForEach(
            "item",
            BrsIdentifier("items"),
            BrsBlock()
        )
        checkContains("For each",
            forEach.render(),
            "for each item in items",
            "end for"
        )

        val tryCatch = BrsTry(
            BrsBlock(mutableListOf(
                BrsExpressionStatement(BrsFunctionCall(BrsIdentifier("risky"), mutableListOf()))
            )),
            "e",
            BrsBlock(mutableListOf(
                BrsExpressionStatement(BrsFunctionCall(
                    BrsIdentifier("print"), mutableListOf(BrsIdentifier("e"))
                ))
            ))
        )
        checkContains("Try/catch",
            tryCatch.render(),
            "try",
            "risky()",
            "catch e",
            "print(e)",
            "end try"
        )

        println()
        println("--- Full Program ---")
        val program = BrsProgram(
            declarations = mutableListOf(
                BrsFunction(
                    "main",
                    mutableListOf(),
                    BrsType.VOID,
                    BrsBlock(mutableListOf(
                        BrsExpressionStatement(BrsFunctionCall(
                            BrsIdentifier("print"),
                            mutableListOf(BrsStringLiteral("Hello, Roku!"))
                        ))
                    ))
                )
            )
        )
        checkContains("Program",
            program.render(),
            "function main()",
            "print(\"Hello, Roku!\")",
            "end function"
        )

        // Summary
        println()
        println("=" .repeat(60))
        println("Results: $passCount passed, $failCount failed")
        println("=" .repeat(60))

        if (failCount > 0) {
            throw AssertionError("$failCount test(s) failed")
        }
    }
}

// Allow running as a main function
fun main() {
    BrsAstVerification.runAllTests()
}
