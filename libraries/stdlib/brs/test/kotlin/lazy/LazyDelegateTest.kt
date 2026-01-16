/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.lazy

import kotlin.test.*

/**
 * Tests for lazy property delegation support.
 */
fun TestRunner.lazyDelegateTests() {
    suite("Lazy Delegates") {

        test("lazy isInitialized check") {
            var initCount = 0
            val lazyInstance = lazy {
                initCount++
                "value"
            }

            assertFalse(lazyInstance.isInitialized())
            assertEquals(0, initCount)

            assertEquals("value", lazyInstance.value)
            assertTrue(lazyInstance.isInitialized())
            assertEquals(1, initCount)

            // Access again, still initialized, count unchanged
            assertEquals("value", lazyInstance.value)
            assertTrue(lazyInstance.isInitialized())
            assertEquals(1, initCount)
        }

        test("lazy toString before and after initialization") {
            val lazyInstance = lazy { "hello" }
            assertEquals("Lazy value not initialized yet.", lazyInstance.toString())
            lazyInstance.value // Force initialization
            assertEquals("hello", lazyInstance.toString())
        }
    }
}
