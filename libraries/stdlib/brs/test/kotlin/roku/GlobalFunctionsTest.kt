/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.roku

import kotlin.brs.roku.*
import kotlin.test.*

/**
 * Tests for Roku global utility function bindings.
 */
fun TestRunner.globalFunctionsTests() {
    suite("Core Utility Functions") {
        test("sleep pauses execution") {
            val start = upTime()
            sleep(100)
            val elapsed = upTime() - start
            assertTrue(elapsed >= 0.09f, "Sleep should pause for at least 90ms, got $elapsed")
        }

        test("upTime returns positive value") {
            val time = upTime()
            assertTrue(time > 0f, "UpTime should return positive value")
        }
    }

    suite("JSON Functions") {
        test("parseJson parses valid JSON") {
            val result = parseJson("""{"name": "test", "value": 42}""")
            assertNotNull(result)
        }

        test("parseJson returns null for invalid JSON") {
            val result = parseJson("not valid json {")
            assertNull(result, "Invalid JSON should return null")
        }

        test("formatJson formats object") {
            val obj = RoAssociativeArray.create()
            obj.addReplace("key", "value")
            val json = formatJson(obj)
            assertTrue(json.contains("key"), "JSON should contain key")
            assertTrue(json.contains("value"), "JSON should contain value")
        }
    }

    suite("File System Functions") {
        test("listDir returns array") {
            val files = listDir("pkg:/")
            assertTrue(files.count() >= 0, "ListDir should return an array")
        }

        test("read and write ascii file") {
            val testPath = "tmp:/global_funcs_test.txt"
            val testContent = "Hello, Roku!"

            val written = writeAsciiFile(testPath, testContent)
            assertTrue(written, "Write should succeed")

            val read = readAsciiFile(testPath)
            assertEquals(testContent, read, "Content should match")

            val deleted = deleteFile(testPath)
            assertTrue(deleted, "Delete should succeed")
        }

        test("create and delete directory") {
            val testDir = "tmp:/global_funcs_test_dir"

            val created = createDirectory(testDir)
            assertTrue(created, "Create should succeed")

            val deleted = deleteDirectory(testDir)
            assertTrue(deleted, "Delete should succeed")
        }

        test("matchFiles searches for patterns") {
            val matches = matchFiles("pkg:/", "*.brs")
            // Should return an array (may or may not have matches)
            assertTrue(matches.count() >= 0)
        }
    }

    suite("String Conversion Functions") {
        test("strToI converts valid integer string") {
            assertEquals(42, strToI("42"))
        }

        test("strToI converts negative integer string") {
            assertEquals(-17, strToI("-17"))
        }

        test("strToI returns 0 for invalid string") {
            assertEquals(0, strToI("not a number"))
        }
    }

    suite("Localization Functions") {
        test("tr returns original string without translations") {
            // Without translations.xml, should return original string
            val result = tr("Hello")
            assertEquals("Hello", result)
        }
    }
}
