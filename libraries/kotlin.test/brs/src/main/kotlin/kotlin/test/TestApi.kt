/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.test

import kotlin.test.adapters.BareAdapter

/**
 * Global framework adapter instance.
 */
private var _adapter: FrameworkAdapter? = null

/**
 * Returns the current framework adapter.
 * If no adapter has been set, a BareAdapter is created and returned.
 */
internal fun adapter(): FrameworkAdapter {
    if (_adapter == null) {
        _adapter = BareAdapter()
    }
    return _adapter!!
}

/**
 * Declares a test suite.
 *
 * @param name the name of the test suite
 * @param ignored whether the suite should be ignored (default: false)
 * @param suiteFn the function that contains the suite's tests
 */
public fun suite(name: String, ignored: Boolean = false, suiteFn: () -> Unit) {
    adapter().suite(name, ignored, suiteFn)
}

/**
 * Declares a test.
 *
 * @param name the name of the test
 * @param ignored whether the test should be ignored (default: false)
 * @param testFn the function that executes the test
 */
@Suppress("BRS_NAME_CASE_CLASH")
public fun test(name: String, ignored: Boolean = false, testFn: () -> Unit) {
    adapter().test(name, ignored, testFn)
}
