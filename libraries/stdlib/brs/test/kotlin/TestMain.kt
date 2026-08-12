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
import test.standard.shortCircuitTests
import test.standard.tryExpressionTests

// Lazy delegate tests
import test.lazy.lazyDelegateTests

// Roku API tests
import test.roku.dateTimeTests
import test.roku.jsonTests
import test.roku.globalFunctionsTests

// Reflection tests
import test.reflect.kclassTests

// Coroutine tests
import test.coroutines.coroutineTests
import test.coroutines.jobProtocolTests
import test.coroutines.jobHierarchyTests
import test.coroutines.suspendFunctionTests
import test.coroutines.dispatcherTests
import test.coroutines.delayTrackerTests
import test.coroutines.delayFunctionTests
import test.coroutines.yieldFunctionTests
import test.coroutines.coroutineQueueTests
import test.coroutines.runBlockingTests
import test.coroutines.taskFunctionRefTests
import test.coroutines.ioWorkerRegistryTests
import test.coroutines.taskPoolTests
import test.coroutines.withContextIOTests
import test.coroutines.awaitTests
import test.coroutines.delayCancellationTests
import test.coroutines.awaitAllTests
import test.coroutines.builderHierarchyTests
import test.coroutines.scopeFunctionTests
import test.coroutines.withTimeoutTests

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
        shortCircuitTests()
        tryExpressionTests()

        // Lazy delegates
        lazyDelegateTests()

        // Roku APIs
        dateTimeTests()
        jsonTests()
        globalFunctionsTests()

        // Reflection
        kclassTests()

        // Coroutines
        coroutineTests()
        jobProtocolTests()
        jobHierarchyTests()
        suspendFunctionTests()
        dispatcherTests()
        delayTrackerTests()
        delayFunctionTests()
        yieldFunctionTests()
        coroutineQueueTests()
        runBlockingTests()
        taskFunctionRefTests()
        ioWorkerRegistryTests()
        taskPoolTests()
        withContextIOTests()
        awaitTests()
        delayCancellationTests()
        awaitAllTests()
        builderHierarchyTests()
        scopeFunctionTests()
        withTimeoutTests()
    }
}
