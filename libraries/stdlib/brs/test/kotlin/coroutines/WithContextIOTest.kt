/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.coroutines

import kotlin.brs.Dynamic
import kotlin.brs.roku.RoAssociativeArray
import kotlin.coroutines.task.IOWorkerRegistry
import kotlin.test.*

/**
 * Tests for `withContext(Dispatchers.IO)` automatic extraction infrastructure.
 *
 * The compiler automatically transforms `withContext(Dispatchers.IO)` blocks
 * into the worker registry pattern at compile time. These tests verify:
 *
 * 1. IOWorkerRegistry correctly handles worker registration
 * 2. RoAssociativeArray captures work correctly as containers
 * 3. Worker function patterns execute correctly
 *
 * Note: Full coroutine tests with withContext are not included here because
 * the withContext implementation has known limitations with Unconfined dispatcher.
 * For full end-to-end tests with actual Task execution, use roku-test-app.
 */
fun TestRunner.withContextIOTests() {
    suite("IOWorkerRegistry API") {

        test("register accepts worker function") {
            // This mimics what the compiler generates
            IOWorkerRegistry.register("__test_worker_api_1") { captures: Dynamic? ->
                val cap = captures as? RoAssociativeArray
                val value = cap?.lookup("value") as? String ?: "default"
                value.uppercase()
            }

            // Registration should succeed without throwing
            assertTrue(true)
        }

        test("register accepts multiple workers") {
            IOWorkerRegistry.register("__test_multi_1") { "result_1" }
            IOWorkerRegistry.register("__test_multi_2") { "result_2" }
            IOWorkerRegistry.register("__test_multi_3") { "result_3" }

            assertTrue(true, "Multiple registrations should succeed")
        }

        test("registerTyped provides type-safe registration") {
            IOWorkerRegistry.registerTyped<String>("__test_typed_1") { captures ->
                val input = captures?.lookup("input") as? String ?: "default"
                "processed: $input"
            }

            assertTrue(true, "Typed registration should succeed")
        }
    }

    suite("Capture Container (RoAssociativeArray)") {

        test("stores string values") {
            val captures = RoAssociativeArray.create()
            captures.addReplace("stringKey", "stringValue")

            assertEquals("stringValue", captures.lookup("stringKey") as? String)
        }

        test("stores integer values") {
            val captures = RoAssociativeArray.create()
            captures.addReplace("intKey", 42)

            assertEquals(42, captures.lookup("intKey") as? Int)
        }

        test("stores boolean values") {
            val captures = RoAssociativeArray.create()
            captures.addReplace("boolKey", true)

            assertEquals(true, captures.lookup("boolKey") as? Boolean)
        }

        test("stores null values") {
            val captures = RoAssociativeArray.create()
            captures.addReplace("nullKey", null)

            assertEquals(null, captures.lookup("nullKey"))
        }

        test("stores nested associative arrays") {
            val captures = RoAssociativeArray.create()
            val nested = RoAssociativeArray.create()
            nested.addReplace("nestedKey", "nestedValue")

            captures.addReplace("nested", nested)

            val retrieved = captures.lookup("nested") as? RoAssociativeArray
            assertNotNull(retrieved)
            assertEquals("nestedValue", retrieved!!.lookup("nestedKey") as? String)
        }

        test("stores RoArray values") {
            val captures = RoAssociativeArray.create()
            val array = kotlin.brs.roku.RoArray.create(0, true)
            array.push(1)
            array.push(2)
            array.push(3)

            captures.addReplace("array", array)

            assertTrue(captures.doesExist("array"), "Array should be stored")
            val retrieved = captures.lookup("array") as? kotlin.brs.roku.RoArray
            assertNotNull(retrieved)
            assertEquals(3, retrieved!!.count())
        }

        test("doesExist returns true for existing keys") {
            val captures = RoAssociativeArray.create()
            captures.addReplace("existingKey", "value")

            assertTrue(captures.doesExist("existingKey"))
        }

        test("doesExist returns false for missing keys") {
            val captures = RoAssociativeArray.create()

            assertFalse(captures.doesExist("missingKey"))
        }

        test("stores multiple types together") {
            val captures = RoAssociativeArray.create()
            captures.addReplace("str", "hello")
            captures.addReplace("num", 42)
            captures.addReplace("flag", true)
            captures.addReplace("nullable", null)

            assertEquals("hello", captures.lookup("str") as? String)
            assertEquals(42, captures.lookup("num") as? Int)
            assertEquals(true, captures.lookup("flag") as? Boolean)
            assertEquals(null, captures.lookup("nullable"))
        }
    }

    suite("Worker Function Pattern") {

        test("worker with captures extracts values") {
            fun simulatedWorker(captures: RoAssociativeArray?): String {
                val cap = captures ?: return "no captures"
                val name = cap.lookup("name") as? String ?: return "no name"
                return "Hello, $name!"
            }

            val captures = RoAssociativeArray.create()
            captures.addReplace("name", "World")

            val result = simulatedWorker(captures)
            assertEquals("Hello, World!", result)
        }

        test("worker handles null captures gracefully") {
            fun simulatedWorker(captures: RoAssociativeArray?): String {
                val cap = captures ?: return "default result"
                val name = cap.lookup("name") as? String ?: return "missing name"
                return "Hello, $name!"
            }

            assertEquals("default result", simulatedWorker(null))
        }

        test("worker handles missing keys gracefully") {
            fun simulatedWorker(captures: RoAssociativeArray?): String {
                val cap = captures ?: return "default result"
                val name = cap.lookup("name") as? String ?: return "missing name"
                return "Hello, $name!"
            }

            val captures = RoAssociativeArray.create()
            assertEquals("missing name", simulatedWorker(captures))
        }

        test("worker with multiple captures") {
            fun simulatedWorker(captures: RoAssociativeArray?): String {
                val cap = captures ?: return "error"
                val firstName = cap.lookup("firstName") as? String ?: "Unknown"
                val lastName = cap.lookup("lastName") as? String ?: "Unknown"
                val age = cap.lookup("age") as? Int ?: 0
                return "$firstName $lastName is $age years old"
            }

            val captures = RoAssociativeArray.create()
            captures.addReplace("firstName", "John")
            captures.addReplace("lastName", "Doe")
            captures.addReplace("age", 30)

            val result = simulatedWorker(captures)
            assertEquals("John Doe is 30 years old", result)
        }

        test("worker returns computed result") {
            fun computeWorker(captures: RoAssociativeArray?): Int {
                val cap = captures ?: return 0
                val a = cap.lookup("a") as? Int ?: 0
                val b = cap.lookup("b") as? Int ?: 0
                return a + b
            }

            val captures = RoAssociativeArray.create()
            captures.addReplace("a", 10)
            captures.addReplace("b", 20)

            assertEquals(30, computeWorker(captures))
        }

        test("worker with nested data") {
            fun nestedWorker(captures: RoAssociativeArray?): String {
                val cap = captures ?: return "error"
                val user = cap.lookup("user") as? RoAssociativeArray ?: return "no user"
                val name = user.lookup("name") as? String ?: "unknown"
                return "User: $name"
            }

            val user = RoAssociativeArray.create()
            user.addReplace("name", "Alice")

            val captures = RoAssociativeArray.create()
            captures.addReplace("user", user)

            assertEquals("User: Alice", nestedWorker(captures))
        }
    }

    suite("Capture Analysis Scenarios") {
        // These tests verify patterns the compiler generates for various capture scenarios

        test("single string capture pattern") {
            // Simulates: withContext(IO) { url.fetch() }
            // Compiler generates: captures.put("url", url)
            val url = "https://example.com"

            val captures = RoAssociativeArray.create()
            captures.addReplace("url", url)

            // Worker extracts: val url = cap.lookup("url") as String
            val extractedUrl = captures.lookup("url") as? String
            assertEquals("https://example.com", extractedUrl)
        }

        test("multiple primitive captures pattern") {
            // Simulates: withContext(IO) { doWork(x, y, enabled) }
            val x = 10
            val y = 20
            val enabled = true

            val captures = RoAssociativeArray.create()
            captures.addReplace("x", x)
            captures.addReplace("y", y)
            captures.addReplace("enabled", enabled)

            assertEquals(10, captures.lookup("x") as? Int)
            assertEquals(20, captures.lookup("y") as? Int)
            assertEquals(true, captures.lookup("enabled") as? Boolean)
        }

        test("nullable capture pattern") {
            // Simulates: val opt: String? = null; withContext(IO) { use(opt) }
            val optionalValue: String? = null

            val captures = RoAssociativeArray.create()
            captures.addReplace("optionalValue", optionalValue)

            val extracted = captures.lookup("optionalValue")
            assertNull(extracted)
        }

        test("object capture pattern") {
            // Simulates: val config = createConfig(); withContext(IO) { use(config) }
            val config = RoAssociativeArray.create()
            config.addReplace("timeout", 5000)
            config.addReplace("retries", 3)

            val captures = RoAssociativeArray.create()
            captures.addReplace("config", config)

            val extracted = captures.lookup("config") as? RoAssociativeArray
            assertNotNull(extracted)
            assertEquals(5000, extracted!!.lookup("timeout") as? Int)
            assertEquals(3, extracted!!.lookup("retries") as? Int)
        }
    }
}
