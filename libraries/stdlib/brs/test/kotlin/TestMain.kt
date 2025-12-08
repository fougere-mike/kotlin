/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test

import kotlin.test.runTests
import kotlin.test.TestRunner

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

// Roku API tests
import test.roku.dateTimeTests
import test.roku.jsonTests

/**
 * Main entry point for stdlib runtime tests.
 *
 * Runs all test suites and outputs results in JSON format
 * between [KOTLINTEST_START] and [KOTLINTEST_END] markers.
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

        // Roku APIs
        dateTimeTests()
        jsonTests()
    }
}
