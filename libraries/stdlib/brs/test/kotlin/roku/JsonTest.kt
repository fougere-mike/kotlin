/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.roku

import kotlin.brs.runtime.*
import kotlin.test.*

/**
 * Tests for Roku JSON APIs.
 */
fun TestRunner.jsonTests() {
    suite("JSON Formatting") {
        test("format simple map") {
            val map = mapOf("name" to "test", "value" to 42)
            val json = brsFormatJson(map)
            assertTrue(json.contains("name"))
            assertTrue(json.contains("test"))
            assertTrue(json.contains("value"))
            assertTrue(json.contains("42"))
        }

        test("format nested map") {
            val map = mapOf(
                "outer" to mapOf(
                    "inner" to "value"
                )
            )
            val json = brsFormatJson(map)
            assertTrue(json.contains("outer"))
            assertTrue(json.contains("inner"))
            assertTrue(json.contains("value"))
        }

        test("format list") {
            val list = listOf(1, 2, 3)
            val json = brsFormatJson(list)
            assertTrue(json.contains("1"))
            assertTrue(json.contains("2"))
            assertTrue(json.contains("3"))
        }

        test("format mixed types") {
            val map = mapOf(
                "string" to "hello",
                "number" to 123,
                "boolean" to true,
                "list" to listOf(1, 2, 3)
            )
            val json = brsFormatJson(map)
            assertTrue(json.contains("string"))
            assertTrue(json.contains("hello"))
            assertTrue(json.contains("number"))
            assertTrue(json.contains("123"))
            assertTrue(json.contains("boolean"))
            assertTrue(json.contains("true"))
            assertTrue(json.contains("list"))
        }

        test("format empty map") {
            val map = emptyMap<String, Any>()
            val json = brsFormatJson(map)
            assertTrue(json.contains("{"))
            assertTrue(json.contains("}"))
        }

        test("format empty list") {
            val list = emptyList<Int>()
            val json = brsFormatJson(list)
            assertTrue(json.contains("["))
            assertTrue(json.contains("]"))
        }

        test("format null") {
            val json = brsFormatJson(null)
            assertTrue(json.contains("null"))
        }
    }
}
