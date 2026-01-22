/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.standard

import kotlin.test.*

/**
 * Tests for standard library scope functions.
 */
fun TestRunner.standardFunctionsTests() {
    suite("Standard Functions") {
        // let
        test("let returns lambda result") {
            val result = "hello".let { it.length }
            assertEquals(5, result)
        }

        test("let with null") {
            val str: String? = null
            val result = str?.let { it.length }
            assertNull(result)
        }

        test("let chain") {
            val result = 5
                .let { it * 2 }
                .let { it + 1 }
            assertEquals(11, result)
        }

        // run
        test("run returns lambda result") {
            val result = "hello".run { length }
            assertEquals(5, result)
        }

        test("run without receiver") {
            // Use kotlin.run to explicitly call the standalone version
            // Inside a lambda-with-receiver, unqualified `run` resolves to T.run
            val result = kotlin.run {
                val a = 1
                val b = 2
                a + b
            }
            assertEquals(3, result)
        }

        // also
        test("also returns receiver") {
            var sideEffect = 0
            val result = "hello".also { sideEffect = it.length }
            assertEquals("hello", result)
            assertEquals(5, sideEffect)
        }

        test("also chain") {
            val list = mutableListOf<Int>()
            val result = list
                .also { it.add(1) }
                .also { it.add(2) }
                .also { it.add(3) }
            assertEquals(listOf(1, 2, 3), result)
        }

        // apply
        test("apply returns receiver") {
            val sb = StringBuilder().apply {
                append("hello")
                append(" ")
                append("world")
            }
            assertEquals("hello world", sb.toString())
        }

        test("apply for configuration") {
            data class Config(var name: String = "", var value: Int = 0)

            val config = Config().apply {
                name = "test"
                value = 42
            }
            assertEquals("test", config.name)
            assertEquals(42, config.value)
        }

        // check and require
        test("check passes when true") {
            check(true)  // Should not throw
            assertTrue(true)
        }

        test("require passes when true") {
            require(true)  // Should not throw
            assertTrue(true)
        }

        // Pair
        test("to creates Pair") {
            val pair = "key" to 42
            assertEquals("key", pair.first)
            assertEquals(42, pair.second)
        }

        test("Pair destructuring") {
            val (a, b) = "hello" to "world"
            assertEquals("hello", a)
            assertEquals("world", b)
        }
    }
}
