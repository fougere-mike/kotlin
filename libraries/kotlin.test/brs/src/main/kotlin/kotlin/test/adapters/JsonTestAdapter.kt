/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.test.adapters

import kotlin.brs.roku.RoDateTime
import kotlin.brs.roku.RoTimespan
import kotlin.brs.runtime.brsFormatJson
import kotlin.brs.runtime.mapToPlainAA
import kotlin.test.FrameworkAdapter

/**
 * JSON-output test adapter for automated E2E testing.
 *
 * This adapter outputs structured JSON that can be parsed by the test runner
 * via the Roku debug console (port 8085).
 *
 * JSON output is wrapped between [KOTLINTEST_START] and [KOTLINTEST_END] markers
 * for reliable parsing from the console stream.
 *
 * Output format:
 * ```
 * [KOTLINTEST_START]
 * {"type":"run_start","timestamp":1701234567890}
 * {"type":"suite_start","suite":"MyTest","timestamp":1701234567891}
 * {"type":"test_start","suite":"MyTest","test":"testFoo","timestamp":1701234567892}
 * {"type":"test_pass","suite":"MyTest","test":"testFoo","duration_ms":15}
 * {"type":"suite_end","suite":"MyTest","passed":1,"failed":0,"ignored":0,"duration_ms":20}
 * {"type":"run_complete","total_suites":1,"total_tests":1,"passed":1,"failed":0,"ignored":0,"duration_ms":25}
 * [KOTLINTEST_END]
 * ```
 */
public class JsonTestAdapter : FrameworkAdapter {
    private var currentSuite: String = ""
    private var suiteTimer = RoTimespan.create()
    private var testTimer = RoTimespan.create()
    private var runTimer = RoTimespan.create()

    // Suite-level counters
    private var suitePassed = 0
    private var suiteFailed = 0
    private var suiteIgnored = 0

    // Run-level counters
    private var totalSuites = 0
    private var totalPassed = 0
    private var totalFailed = 0
    private var totalIgnored = 0

    private var runStarted = false

    /**
     * Starts the test run and outputs the start marker.
     * Must be called before running any tests.
     *
     * This method implements a "flood + sentinel" strategy to handle stale logs
     * in the Roku debug console buffer:
     * 1. Print a unique sentinel with timestamp
     * 2. Flood with ~100 lines to push stale logs through the buffer
     * 3. Print sentinel again (test runner looks for the last occurrence)
     */
    public fun startRun() {
        runStarted = true
        runTimer.mark()

        // Generate unique sentinel using timestamp
        val runId = currentTimeMillis()
        val sentinel = "===KOTLINTEST_SENTINEL_${runId}==="

        // Print sentinel and flood to push out stale logs from device buffer
        // The Roku buffer can hold old logs from previous runs; flooding pushes them out
        println(sentinel)
        for (i in 0 until 100) {
            println("[KOTLINTEST_BUFFER_FLUSH:$runId:$i]")
        }
        println(sentinel)  // Print again after flush - test runner looks for the LAST occurrence

        // Now emit the actual test markers
        println("[KOTLINTEST_RUN_ID:$runId]")
        println("[KOTLINTEST_START]")
        emitJson(mapOf(
            "type" to "run_start",
            "timestamp" to currentTimeMillis()
        ))
    }

    /**
     * Ends the test run and outputs the summary.
     * Must be called after all tests have completed.
     */
    public fun endRun() {
        val duration = runTimer.totalMilliseconds()
        emitJson(mapOf(
            "type" to "run_complete",
            "total_suites" to totalSuites,
            "total_tests" to (totalPassed + totalFailed + totalIgnored),
            "passed" to totalPassed,
            "failed" to totalFailed,
            "ignored" to totalIgnored,
            "duration_ms" to duration
        ))
        println("[KOTLINTEST_END]")
    }

    override fun suite(name: String, ignored: Boolean, suiteFn: () -> Unit) {
        // Auto-start run if not already started
        if (!runStarted) {
            startRun()
        }

        if (ignored) {
            emitJson(mapOf(
                "type" to "suite_ignored",
                "suite" to name,
                "reason" to "Suite marked as ignored"
            ))
            return
        }

        currentSuite = name
        suiteTimer.mark()
        suitePassed = 0
        suiteFailed = 0
        suiteIgnored = 0
        totalSuites++

        emitJson(mapOf(
            "type" to "suite_start",
            "suite" to name,
            "timestamp" to currentTimeMillis()
        ))

        // Note: try-catch disabled due to compiler code generation issues.
        // Suite exceptions will propagate and crash the test run.
        suiteFn()

        val duration = suiteTimer.totalMilliseconds()
        emitJson(mapOf(
            "type" to "suite_end",
            "suite" to name,
            "passed" to suitePassed,
            "failed" to suiteFailed,
            "ignored" to suiteIgnored,
            "duration_ms" to duration
        ))
    }

    override fun test(name: String, ignored: Boolean, testFn: () -> Unit) {
        if (ignored) {
            suiteIgnored++
            totalIgnored++
            emitJson(mapOf(
                "type" to "test_ignored",
                "suite" to currentSuite,
                "test" to name,
                "reason" to "Test marked as ignored"
            ))
            return
        }

        testTimer.mark()

        emitJson(mapOf(
            "type" to "test_start",
            "suite" to currentSuite,
            "test" to name,
            "timestamp" to currentTimeMillis()
        ))

        // Note: try-catch disabled due to compiler code generation issues.
        // Test exceptions will propagate and crash the test run.
        // Failures will show which test crashed.
        testFn()
        val duration = testTimer.totalMilliseconds()
        suitePassed++
        totalPassed++
        emitJson(mapOf(
            "type" to "test_pass",
            "suite" to currentSuite,
            "test" to name,
            "duration_ms" to duration
        ))
    }

    /**
     * Emits a JSON object as a single line to the console.
     * Converts the Kotlin Map to a plain BrightScript AA to avoid
     * FormatJson trying to serialize method references.
     */
    private fun emitJson(data: Map<String, Any?>) {
        val plainAA = mapToPlainAA(data)
        val json = brsFormatJson(plainAA)
        println(json)
    }

    /**
     * Returns the current time in milliseconds since epoch.
     */
    private fun currentTimeMillis(): Long {
        val dt = RoDateTime.create()
        dt.mark()
        return dt.asSeconds() * 1000L + dt.getMilliseconds().toLong()
    }
}
