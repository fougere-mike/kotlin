/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.test

import kotlin.test.adapters.JsonTestAdapter
import kotlin.test.device.TestPort
import kotlin.test.device.runPumping

/**
 * Test runner that executes test suites with JSON output for automated testing.
 *
 * Usage:
 * ```kotlin
 * fun main() {
 *     runTests {
 *         suite("MyTestSuite") {
 *             test("testFoo") {
 *                 assertEquals(2, 1 + 1)
 *             }
 *             test("testBar") {
 *                 assertTrue(true)
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * Or with test class instances:
 * ```kotlin
 * fun main() {
 *     runTests {
 *         testClass(MyTestClass())
 *         testClass(AnotherTestClass())
 *     }
 * }
 * ```
 *
 * The runner outputs structured JSON between [KOTLINTEST_START] and [KOTLINTEST_END]
 * markers that can be parsed by the Gradle test runner task.
 */
public fun runTests(block: TestRunner.() -> Unit) {
    val runner = TestRunner()
    runner.run(block)
}

/**
 * Test runner context that provides methods for registering and executing tests.
 */
public class TestRunner internal constructor() {
    private val adapter = JsonTestAdapter()
    private val testClasses = mutableListOf<Any>()
    private val directSuites = mutableListOf<Pair<String, () -> Unit>>()

    /**
     * Registers a test class instance to be executed.
     *
     * The test class should have methods annotated with @Test.
     * Test discovery uses the generated __testMethods__ property.
     *
     * @param instance The test class instance.
     */
    public fun testClass(instance: Any) {
        testClasses.add(instance)
    }

    /**
     * Declares a test suite directly.
     *
     * @param name The name of the test suite.
     * @param suiteFn Function containing the test definitions.
     */
    public fun suite(name: String, suiteFn: TestRunner.() -> Unit) {
        directSuites.add(Pair(name) { this.suiteFn() })
    }

    /**
     * Declares a test within the current suite.
     *
     * This is typically called within a suite block.
     *
     * @param name The name of the test.
     * @param testFn The test function to execute.
     */
    public fun test(name: String, testFn: () -> Unit) {
        adapter.test(name, false, testFn)
    }

    /**
     * Declares an ignored test within the current suite.
     *
     * @param name The name of the test.
     * @param reason Optional reason for ignoring.
     * @param testFn The test function (won't be executed).
     */
    public fun xtest(name: String, reason: String = "", testFn: () -> Unit) {
        adapter.test(name, true, testFn)
    }

    /**
     * Declares an async test that may await SceneGraph field changes.
     *
     * The body runs as a coroutine inside [runPumping], which pumps [TestPort]
     * on the current thread, so it can use kotlin.test.device.awaitField and
     * friends. If the body has not completed within [timeoutMs] the test fails
     * with an AssertionError describing the pending awaits; the run continues.
     *
     * @param name The name of the test.
     * @param timeoutMs Whole-test deadline in milliseconds.
     * @param testFn The suspending test body.
     */
    public fun testAsync(name: String, timeoutMs: Int = 10_000, testFn: suspend () -> Unit) {
        adapter.test(name, false) {
            runPumping(TestPort.port, timeoutMs) {
                testFn()
            }
        }
    }

    internal fun run(block: TestRunner.() -> Unit) {
        adapter.startRun()

        // Execute the registration block
        this.block()

        // Run direct suites
        for ((name, suiteFn) in directSuites) {
            adapter.suite(name, false, suiteFn)
        }

        // Run test classes
        for (instance in testClasses) {
            executeTestClass(instance)
        }

        adapter.endRun()
    }

    @Suppress("UNCHECKED_CAST")
    private fun executeTestClass(instance: Any) {
        val className = getClassName(instance)

        // The compiler generates a __testMethods__ property on test classes
        // that returns a list of test method info
        val testMethods = getTestMethods(instance)

        if (testMethods.isEmpty()) {
            // No generated test methods, try reflection-like approach
            // For now, just run the class as a suite if it has a runTests method
            adapter.suite(className, false) {
                tryRunAsTestClass(instance)
            }
            return
        }

        adapter.suite(className, false) {
            for (method in testMethods) {
                val methodName = method.first
                val methodFn = method.second
                adapter.test(methodName, false) {
                    methodFn()
                }
            }
        }
    }

    /**
     * Gets the class name from an instance.
     */
    private fun getClassName(instance: Any): String {
        return instance::class.simpleName ?: "UnknownClass"
    }

    /**
     * Gets test methods from an instance.
     *
     * The compiler generates a __testMethods__ property that returns
     * List<Pair<String, () -> Unit>> for classes with @Test methods.
     */
    @Suppress("UNCHECKED_CAST")
    private fun getTestMethods(instance: Any): List<Pair<String, () -> Unit>> {
        // Try to access the generated __testMethods__ property
        // This is generated by the compiler for test classes
        // Note: try-catch disabled due to compiler code generation issues.
        val getter = getTestMethodsProperty(instance)
        return if (getter != null) {
            getter() as List<Pair<String, () -> Unit>>
        } else {
            emptyList()
        }
    }

    /**
     * Attempts to get the __testMethods__ property getter from an instance.
     * Returns null if not available.
     */
    private fun getTestMethodsProperty(instance: Any): (() -> Any)? {
        // This will be lowered by the compiler to access the property
        // For now, return null - actual implementation requires compiler support
        return null
    }

    /**
     * Attempts to run a class as a test if it has a runTests method.
     */
    private fun tryRunAsTestClass(instance: Any) {
        // Fallback: if the class has a runTests() method, call it
        // This allows manual test registration
    }
}

/**
 * Represents metadata for a test method.
 */
public data class TestMethodInfo(
    val name: String,
    val ignored: Boolean = false,
    val ignoreReason: String = ""
)
