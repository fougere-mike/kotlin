/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.math

import kotlin.math.*
import kotlin.test.*

/**
 * Tests for math functions.
 */
fun TestRunner.mathTests() {
    suite("Math") {
        // Constants
        test("PI") {
            assertTrue(PI > 3.14)
            assertTrue(PI < 3.15)
        }

        test("E") {
            assertTrue(E > 2.71)
            assertTrue(E < 2.72)
        }

        // Absolute value
        test("abs Int") {
            assertEquals(5, abs(-5))
            assertEquals(5, abs(5))
            assertEquals(0, abs(0))
        }

        test("abs Double") {
            assertEquals(5.5, abs(-5.5))
            assertEquals(5.5, abs(5.5))
        }

        // Min and Max
        test("min") {
            assertEquals(1, min(1, 2))
            assertEquals(-5, min(-5, 3))
            assertEquals(1.5, min(1.5, 2.5))
        }

        test("max") {
            assertEquals(2, max(1, 2))
            assertEquals(3, max(-5, 3))
            assertEquals(2.5, max(1.5, 2.5))
        }

        // Rounding
        test("floor") {
            assertEquals(3.0, floor(3.7))
            assertEquals(-4.0, floor(-3.3))
        }

        test("ceil") {
            assertEquals(4.0, ceil(3.3))
            assertEquals(-3.0, ceil(-3.7))
        }

        test("round") {
            assertEquals(4.0, round(3.7))
            assertEquals(3.0, round(3.3))
            assertEquals(4.0, round(3.5))
        }

        test("truncate") {
            assertEquals(3.0, truncate(3.7))
            assertEquals(-3.0, truncate(-3.7))
        }

        test("roundToInt") {
            assertEquals(4, 3.7.roundToInt())
            assertEquals(3, 3.3.roundToInt())
        }

        test("roundToLong") {
            assertEquals(4L, 3.7.roundToLong())
            assertEquals(3L, 3.3.roundToLong())
        }

        // Power and roots
        test("sqrt") {
            assertEquals(3.0, sqrt(9.0))
            assertEquals(2.0, sqrt(4.0))
            assertTrue(sqrt(2.0) > 1.41 && sqrt(2.0) < 1.42)
        }

        test("pow") {
            assertEquals(8.0, 2.0.pow(3.0))
            assertEquals(1.0, 5.0.pow(0.0))
            assertEquals(0.25, 2.0.pow(-2.0))
        }

        test("pow Int") {
            assertEquals(8.0, 2.0.pow(3))
            assertEquals(1.0, 5.0.pow(0))
        }

        // Exponential and logarithmic
        test("exp") {
            assertEquals(1.0, exp(0.0))
            assertTrue(exp(1.0) > 2.71 && exp(1.0) < 2.72)
        }

        test("ln") {
            assertEquals(0.0, ln(1.0))
            assertTrue(abs(ln(E) - 1.0) < 0.0001, "ln(E) should be approximately 1.0")
        }

        test("log10") {
            assertEquals(0.0, log10(1.0))
            assertEquals(1.0, log10(10.0))
            assertEquals(2.0, log10(100.0))
        }

        test("log2") {
            assertEquals(0.0, log2(1.0))
            assertEquals(1.0, log2(2.0))
            assertEquals(3.0, log2(8.0))
        }

        test("log with base") {
            assertEquals(2.0, log(4.0, 2.0))
            assertEquals(3.0, log(8.0, 2.0))
        }

        // Trigonometric
        test("sin") {
            assertEquals(0.0, sin(0.0))
            assertTrue(abs(sin(PI / 2) - 1.0) < 0.0001)
        }

        test("cos") {
            assertEquals(1.0, cos(0.0))
            // cos(PI) should be approximately -1.0
            assertTrue(abs(cos(PI) + 1.0) < 0.0001)
        }

        test("tan") {
            assertEquals(0.0, tan(0.0))
            assertTrue(abs(tan(PI / 4) - 1.0) < 0.0001)
        }

        test("asin") {
            assertEquals(0.0, asin(0.0))
            assertTrue(abs(asin(1.0) - PI / 2) < 0.0001)
        }

        test("acos") {
            assertTrue(abs(acos(1.0)) < 0.0001)
            assertTrue(abs(acos(0.0) - PI / 2) < 0.0001)
        }

        test("atan") {
            assertEquals(0.0, atan(0.0))
            assertTrue(abs(atan(1.0) - PI / 4) < 0.0001)
        }

        test("atan2") {
            assertEquals(0.0, atan2(0.0, 1.0))
            assertTrue(abs(atan2(1.0, 1.0) - PI / 4) < 0.0001)
        }

        // Hyperbolic
        test("sinh") {
            assertEquals(0.0, sinh(0.0))
        }

        test("cosh") {
            assertEquals(1.0, cosh(0.0))
        }

        test("tanh") {
            assertEquals(0.0, tanh(0.0))
        }

        // Special functions
        test("hypot") {
            assertEquals(5.0, hypot(3.0, 4.0))
            assertEquals(13.0, hypot(5.0, 12.0))
        }

        test("sign") {
            assertEquals(1.0, sign(42.0))
            assertEquals(-1.0, sign(-42.0))
            assertEquals(0.0, sign(0.0))
        }

        test("sign Int") {
            assertEquals(1, 42.sign)
            assertEquals(-1, (-42).sign)
            assertEquals(0, 0.sign)
        }

        // Special values
        // Note: BrightScript throws runtime errors on division by zero instead of returning NaN,
        // and doesn't support Infinity constants the same way. These tests are skipped.
        // test("isNaN") { ... }
        // test("isInfinite") { ... }
        // test("isFinite") { ... }

    }
}
