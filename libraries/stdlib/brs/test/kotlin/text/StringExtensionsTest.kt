/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.text

import kotlin.test.*

/**
 * Tests for String extension functions.
 */
fun TestRunner.stringExtensionsTests() {
    suite("String Extensions") {
        // Basic properties
        test("length") {
            assertEquals(5, "hello".length)
            assertEquals(0, "".length)
        }

        test("isEmpty and isNotEmpty") {
            assertTrue("".isEmpty())
            assertFalse("hello".isEmpty())
            assertFalse("".isNotEmpty())
            assertTrue("hello".isNotEmpty())
        }

        test("isBlank and isNotBlank") {
            assertTrue("".isBlank())
            assertTrue("   ".isBlank())
            assertFalse("hello".isBlank())
            assertTrue("hello".isNotBlank())
        }

        // Case conversion
        test("uppercase") {
            assertEquals("HELLO", "hello".uppercase())
            assertEquals("HELLO WORLD", "Hello World".uppercase())
        }

        test("lowercase") {
            assertEquals("hello", "HELLO".lowercase())
            assertEquals("hello world", "Hello World".lowercase())
        }

        // Substring
        test("substring") {
            assertEquals("ello", "hello".substring(1))
            assertEquals("ell", "hello".substring(1, 4))
        }

        // Contains and startsWith/endsWith
        test("contains") {
            assertTrue("hello world".contains("world"))
            assertFalse("hello world".contains("xyz"))
            assertTrue("Hello World".contains("world", ignoreCase = true))
        }

        test("startsWith") {
            assertTrue("hello".startsWith("hel"))
            assertFalse("hello".startsWith("xyz"))
            assertTrue("Hello".startsWith("hel", ignoreCase = true))
        }

        test("endsWith") {
            assertTrue("hello".endsWith("llo"))
            assertFalse("hello".endsWith("xyz"))
            assertTrue("Hello".endsWith("LLO", ignoreCase = true))
        }

        // Finding
        test("indexOf") {
            assertEquals(6, "hello world".indexOf("world"))
            assertEquals(-1, "hello".indexOf("xyz"))
            assertEquals(0, "hello hello".indexOf("hello"))
        }

        test("lastIndexOf") {
            assertEquals(6, "hello hello".lastIndexOf("hello"))
            assertEquals(-1, "hello".lastIndexOf("xyz"))
        }

        // Padding
        test("padStart") {
            assertEquals("  abc", "abc".padStart(5))
            assertEquals("00abc", "abc".padStart(5, '0'))
        }

        test("padEnd") {
            assertEquals("abc  ", "abc".padEnd(5))
            assertEquals("abc00", "abc".padEnd(5, '0'))
        }

        // Char access
        test("get char") {
            assertEquals('h', "hello"[0])
            assertEquals('o', "hello"[4])
        }

        // Comparison
        test("equals") {
            assertTrue("hello".equals("hello"))
            assertFalse("hello".equals("HELLO"))
            assertTrue("hello".equals("HELLO", ignoreCase = true))
        }

        test("compareTo") {
            assertTrue("abc".compareTo("xyz") < 0)
            assertTrue("xyz".compareTo("abc") > 0)
            assertEquals(0, "abc".compareTo("abc"))
        }

        // Null handling
        test("isNullOrEmpty") {
            assertTrue((null as String?).isNullOrEmpty())
            assertTrue("".isNullOrEmpty())
            assertFalse("hello".isNullOrEmpty())
        }

        // Repeat
        test("repeat") {
            assertEquals("abcabcabc", "abc".repeat(3))
            assertEquals("", "abc".repeat(0))
        }

        // lastIndex property
        test("lastIndex") {
            assertEquals(4, "hello".lastIndex)
            assertEquals(-1, "".lastIndex)
        }
    }
}
