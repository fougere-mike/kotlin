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
 * Tests for [IOWorkerRegistry] and the worker registry pattern.
 *
 * These tests verify the registration API and helper functions work correctly.
 * Actual Task thread execution cannot be tested in the stdlib test framework
 * because custom Task components require XML definitions.
 *
 * For full integration testing with actual Task execution, use the roku-test-app.
 */
fun TestRunner.ioWorkerRegistryTests() {
    suite("IOWorkerRegistry") {

        test("register stores worker function without throwing") {
            // Register a simple worker - verifies the API doesn't throw
            IOWorkerRegistry.register("testWorker1") { captures ->
                "result from worker"
            }

            // The worker is stored in pendingRegistrations until syncToGlobal is called
            // We can't directly verify this without accessing private state,
            // but we can verify registration doesn't throw
            assertTrue(true, "Worker registration should not throw")
        }

        test("registerTyped provides type-safe registration") {
            // Register a typed worker
            IOWorkerRegistry.registerTyped<String>("testWorker2") { captures ->
                val input = captures?.lookup("input") as? String ?: "default"
                "processed: $input"
            }

            assertTrue(true, "Typed worker registration should not throw")
        }

        test("RoAssociativeArray can store string values") {
            val captures = RoAssociativeArray.create()
            captures.addReplace("stringKey", "stringValue")

            assertEquals("stringValue", captures.lookup("stringKey") as? String)
        }

        test("RoAssociativeArray can store integer values") {
            val captures = RoAssociativeArray.create()
            captures.addReplace("intKey", 42)

            assertEquals(42, captures.lookup("intKey") as? Int)
        }

        test("RoAssociativeArray can store boolean values") {
            val captures = RoAssociativeArray.create()
            captures.addReplace("boolKey", true)

            assertEquals(true, captures.lookup("boolKey") as? Boolean)
        }

        test("RoAssociativeArray can store null values") {
            val captures = RoAssociativeArray.create()
            captures.addReplace("nullKey", null)

            assertEquals(null, captures.lookup("nullKey"))
        }

        test("captures can store nested associative arrays") {
            val captures = RoAssociativeArray.create()
            val nested = RoAssociativeArray.create()
            nested.addReplace("nestedKey", "nestedValue")

            captures.addReplace("nested", nested)

            val retrieved = captures.lookup("nested") as? RoAssociativeArray
            assertNotNull(retrieved)
            assertEquals("nestedValue", retrieved!!.lookup("nestedKey") as? String)
        }

        test("captures can store RoArray values") {
            val captures = RoAssociativeArray.create()
            val array = kotlin.brs.roku.RoArray.create(0, true)
            array.push(1)
            array.push(2)
            array.push(3)

            captures.addReplace("array", array)

            // Verify the value was stored
            assertTrue(captures.doesExist("array"), "Array should be stored in captures")
            val retrieved = captures.lookup("array") as? kotlin.brs.roku.RoArray
            assertNotNull(retrieved)
            assertEquals(3, retrieved!!.count())
        }

        test("multiple workers can be registered with different names") {
            IOWorkerRegistry.register("worker_a") { "result_a" }
            IOWorkerRegistry.register("worker_b") { "result_b" }
            IOWorkerRegistry.register("worker_c") { "result_c" }

            // All registrations should succeed without throwing
            assertTrue(true, "Multiple worker registrations should not throw")
        }

        test("worker function receives Dynamic? parameter type") {
            // This test verifies the worker function signature accepts Dynamic?
            IOWorkerRegistry.register("capturesTestWorker") { captures: Dynamic? ->
                // Worker can check if captures is an RoAssociativeArray
                val aa = captures as? RoAssociativeArray
                aa?.lookup("key")
            }

            assertTrue(true, "Worker function should accept Dynamic? parameter")
        }

        test("doesExist returns true for existing keys") {
            val captures = RoAssociativeArray.create()
            captures.addReplace("existingKey", "value")

            assertTrue(captures.doesExist("existingKey"), "Key should exist")
        }

        test("doesExist returns false for non-existing keys") {
            val captures = RoAssociativeArray.create()

            assertFalse(captures.doesExist("missingKey"), "Key should not exist")
        }
    }
}
