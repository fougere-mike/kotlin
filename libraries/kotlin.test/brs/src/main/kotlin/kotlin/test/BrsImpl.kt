/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.test

import kotlin.reflect.KClass

/**
 * Global asserter instance for BrightScript.
 */
private var _asserter: Asserter? = null

/**
 * Returns the current asserter instance.
 * If no asserter has been set, a DefaultBrsAsserter is created and returned.
 */
internal actual fun lookupAsserter(): Asserter {
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
public actual fun AssertionErrorWithCause(message: String?, cause: Throwable?): AssertionError {
    val error = AssertionError(message)
    // BRS doesn't support initCause, just return error with message
    return error
}

/**
 * Takes the given [block] of test code and _doesn't_ execute it.
 *
 * This keeps the code under test referenced, but doesn't actually test it until it is implemented.
 */
public actual fun todo(block: () -> Unit) {
    println("TODO at $block")
}

/**
 * Checks that the given block result is a failure with the specified exception type.
 */
@PublishedApi
internal actual fun <T : Throwable> checkResultIsFailure(
    exceptionClass: KClass<T>,
    message: String?,
    blockResult: Result<Unit>
): T {
    blockResult.fold(
        onSuccess = {
            asserter.fail(messagePrefix(message) + "Expected an exception of $exceptionClass to be thrown, but was completed successfully.")
        },
        onFailure = { e ->
            if (exceptionClass.isInstance(e)) {
                @Suppress("UNCHECKED_CAST")
                return e as T
            }
            asserter.fail(messagePrefix(message) + "Expected an exception of $exceptionClass to be thrown, but was $e", e)
        }
    )
}
