/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.test

/**
 * Adapter interface for test frameworks.
 *
 * Implementations of this interface provide the bridge between kotlin.test API
 * and the underlying test execution mechanism.
 */
public interface FrameworkAdapter {
    /**
     * Declares a test suite.
     *
     * @param name the name of the test suite
     * @param ignored whether the suite is ignored
     * @param suiteFn the function that contains the suite's tests
     */
    public fun suite(name: String, ignored: Boolean, suiteFn: () -> Unit)

    /**
     * Declares a test.
     *
     * @param name the name of the test
     * @param ignored whether the test is ignored
     * @param testFn the function that executes the test
     */
    public fun test(name: String, ignored: Boolean, testFn: () -> Unit)
}
