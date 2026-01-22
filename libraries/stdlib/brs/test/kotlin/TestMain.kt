/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test

import kotlin.test.runTests

// Collections tests
import test.collections.arrayListTests
import test.collections.hashMapTests
import test.collections.hashSetTests
import test.collections.linkedHashMapTests
import test.collections.linkedHashSetTests
import test.collections.collectionExtensionsTests
import test.collections.sortingTests

// Sequence tests
import test.sequences.sequenceTests

// Unsigned tests
import test.primitives.unsignedTests

// Math tests
import test.math.mathTests

// Text tests
import test.text.stringBuilderTests
import test.text.stringExtensionsTests

// Standard function tests
import test.standard.standardFunctionsTests

// Lazy delegate tests
import test.lazy.lazyDelegateTests

// Roku API tests
import test.roku.dateTimeTests
import test.roku.jsonTests
import test.roku.globalFunctionsTests

// Reflection tests
import test.reflect.kclassTests

// Coroutine tests (marked as ignored in CoroutineTest.kt - interface default method inheritance issue)
import test.coroutines.coroutineTests
import test.coroutines.suspendFunctionTests
import test.coroutines.dispatcherTests

/**
 * Main entry point for stdlib runtime tests.
 */
fun main() {
    runTests {
        // Collections
        arrayListTests()
        hashMapTests()
        hashSetTests()
        linkedHashMapTests()
        linkedHashSetTests()
        collectionExtensionsTests()
        sortingTests()

        // Sequences
        sequenceTests()

        // Unsigned types
        unsignedTests()

        // Math
        mathTests()

        // Text
        stringBuilderTests()
        stringExtensionsTests()

        // Standard functions
        standardFunctionsTests()

        // Lazy delegates
        lazyDelegateTests()

        // Roku APIs
        dateTimeTests()
        jsonTests()
        globalFunctionsTests()

        // Reflection
        kclassTests()

        // Coroutines (marked as ignored in CoroutineTest.kt - interface default method inheritance issue)
        coroutineTests()
        suspendFunctionTests()
        dispatcherTests()
    }
}
