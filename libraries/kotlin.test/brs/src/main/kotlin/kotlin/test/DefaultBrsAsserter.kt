/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.test

/**
 * Default asserter implementation for BrightScript platform.
 */
internal class DefaultBrsAsserter : Asserter {

    override fun assertEquals(message: String?, expected: Any?, actual: Any?) {
        // Use structural equality via brsStructuralEquals which handles all types properly
        if (!brsStructuralEquals(expected, actual)) {
            // Explicitly convert to string for BrightScript compatibility
            val expectedStr = if (expected != null) expected.toString() else "null"
            val actualStr = if (actual != null) actual.toString() else "null"
            fail(message ?: "Expected <$expectedStr>, actual <$actualStr>.")
        }
    }

    override fun assertNotEquals(message: String?, illegal: Any?, actual: Any?) {
        // Use structural equality via brsStructuralEquals which handles all types properly
        if (brsStructuralEquals(illegal, actual)) {
            // Explicitly convert to string for BrightScript compatibility
            val actualStr = if (actual != null) actual.toString() else "null"
            fail(message ?: "Values should be different. Actual: <$actualStr>.")
        }
    }

    override fun assertSame(message: String?, expected: Any?, actual: Any?) {
        if (expected !== actual) {
            fail(message ?: "Expected and actual are not the same instance.")
        }
    }

    override fun assertNotSame(message: String?, illegal: Any?, actual: Any?) {
        if (illegal === actual) {
            fail(message ?: "Expected and actual should not be the same instance.")
        }
    }

    override fun assertTrue(message: String?, actual: Boolean) {
        if (!actual) {
            fail(message ?: "Expected value to be true.")
        }
    }

    override fun assertFalse(message: String?, actual: Boolean) {
        if (actual) {
            fail(message ?: "Expected value to be false.")
        }
    }

    override fun assertNotNull(message: String?, actual: Any?) {
        if (actual == null) {
            fail(message ?: "Expected value to be not null.")
        }
    }

    override fun assertNull(message: String?, actual: Any?) {
        if (actual != null) {
            fail(message ?: "Expected value to be null.")
        }
    }

    override fun fail(message: String?): Nothing {
        throw AssertionError(message)
    }
}
