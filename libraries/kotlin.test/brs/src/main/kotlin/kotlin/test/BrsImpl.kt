/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.test

import kotlin.reflect.KClass

/**
 * Returns the current asserter instance.
 * If no asserter has been set, a DefaultBrsAsserter is created and returned.
 */
internal fun lookupAsserter(): Asserter {
    if (_asserter == null) {
        _asserter = DefaultBrsAsserter()
    }
    return _asserter!!
}

/**
 * Creates an AssertionError with the given message and cause.
 *
 * Note: BrightScript doesn't support Throwable.initCause(), so the cause is ignored.
 */
public fun AssertionErrorWithCause(message: String?, cause: Throwable?): AssertionError {
    val error = AssertionError(message)
    // BRS doesn't support initCause, just return error with message
    return error
}

/**
 * Takes the given [block] of test code and _doesn't_ execute it.
 *
 * This keeps the code under test referenced, but doesn't actually test it until it is implemented.
 */
public fun todo(block: () -> Unit) {
    println("TODO at $block")
}
