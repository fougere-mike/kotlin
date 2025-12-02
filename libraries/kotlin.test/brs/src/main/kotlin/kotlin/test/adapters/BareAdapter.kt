/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.test.adapters

import kotlin.test.FrameworkAdapter

/**
 * Simple console-based test adapter for BrightScript.
 *
 * This adapter prints test results with parseable markers to the console,
 * which can be collected via telnet monitoring from the Roku device.
 */
internal class BareAdapter : FrameworkAdapter {
    private val results = mutableListOf<TestResult>()
    private var currentSuite: String = ""

    override fun suite(name: String, ignored: Boolean, suiteFn: () -> Unit) {
        if (ignored) {
            println("[SUITE IGNORED] $name")
            return
        }

        currentSuite = name
        println("[SUITE START] $name")

        try {
            suiteFn()
            println("[SUITE PASS] $name")
        } catch (e: Throwable) {
            println("[SUITE FAIL] $name: ${e.message}")
        }
    }

    override fun test(name: String, ignored: Boolean, testFn: () -> Unit) {
        val fullName = if (currentSuite.isNotEmpty()) "$currentSuite.$name" else name

        if (ignored) {
            println("[TEST IGNORED] $fullName")
            results.add(TestResult(currentSuite, name, "IGNORED", null))
            return
        }

        println("[TEST START] $fullName")

        try {
            testFn()
            println("[TEST PASS] $fullName")
            results.add(TestResult(currentSuite, name, "PASS", null))
        } catch (e: Throwable) {
            println("[TEST FAIL] $fullName: ${e.message}")
            results.add(TestResult(currentSuite, name, "FAIL", e.message))
        }
    }

    /**
     * Prints a summary of all test results.
     * This should be called at the end of test execution.
     */
    fun printSummary() {
        val total = results.size
        val passed = results.count { it.status == "PASS" }
        val failed = results.count { it.status == "FAIL" }
        val ignored = results.count { it.status == "IGNORED" }

        println("\n[TEST SUMMARY]")
        println("Total: $total")
        println("Passed: $passed")
        println("Failed: $failed")
        println("Ignored: $ignored")
        println("[TEST SUMMARY END]")
    }

    /**
     * Represents the result of a single test.
     */
    data class TestResult(
        val suite: String,
        val test: String,
        val status: String,
        val message: String?
    )
}
