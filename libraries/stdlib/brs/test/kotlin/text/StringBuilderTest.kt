/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.text

import kotlin.test.*

/**
 * Tests for StringBuilder.
 */
fun TestRunner.stringBuilderTests() {
    suite("StringBuilder") {
        test("create empty") {
            val sb = StringBuilder()
            assertEquals(0, sb.length)
            assertEquals("", sb.toString())
        }

        test("create with initial content") {
            val sb = StringBuilder("hello")
            assertEquals(5, sb.length)
            assertEquals("hello", sb.toString())
        }

        test("append string") {
            val sb = StringBuilder()
            sb.append("hello")
            sb.append(" ")
            sb.append("world")
            assertEquals("hello world", sb.toString())
        }

        test("append various types") {
            val sb = StringBuilder()
            sb.append(42)
            sb.append(true)
            sb.append('X')
            assertEquals("42trueX", sb.toString())
        }

        test("append chaining") {
            val result = StringBuilder()
                .append("a")
                .append("b")
                .append("c")
                .toString()
            assertEquals("abc", result)
        }

        test("insert") {
            val sb = StringBuilder("hello world")
            sb.insert(6, "beautiful ")
            assertEquals("hello beautiful world", sb.toString())
        }

        test("insert at beginning") {
            val sb = StringBuilder("world")
            sb.insert(0, "hello ")
            assertEquals("hello world", sb.toString())
        }

        test("deleteAt") {
            val sb = StringBuilder("hello")
            sb.deleteAt(2)
            assertEquals("helo", sb.toString())
        }

        test("charAt") {
            val sb = StringBuilder("hello")
            assertEquals('h', sb[0])
            assertEquals('e', sb[1])
            assertEquals('o', sb[4])
        }

        test("substring") {
            val sb = StringBuilder("hello world")
            assertEquals("world", sb.substring(6))
            assertEquals("ello", sb.substring(1, 5))
        }

        test("reverse") {
            val sb = StringBuilder("hello")
            sb.reverse()
            assertEquals("olleh", sb.toString())
        }

        test("clear") {
            val sb = StringBuilder("hello")
            sb.clear()
            assertEquals(0, sb.length)
            assertEquals("", sb.toString())
        }

        test("isEmpty") {
            val sb = StringBuilder()
            assertTrue(sb.isEmpty())
            sb.append("x")
            assertFalse(sb.isEmpty())
        }

        test("isNotEmpty") {
            val sb = StringBuilder()
            assertFalse(sb.isNotEmpty())
            sb.append("x")
            assertTrue(sb.isNotEmpty())
        }

        test("appendLine") {
            val sb = StringBuilder()
            sb.appendLine("hello")
            sb.appendLine("world")
            assertTrue(sb.toString().contains("hello"))
            assertTrue(sb.toString().contains("world"))
        }

    }
}
