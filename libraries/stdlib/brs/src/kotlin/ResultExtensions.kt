/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin

/**
 * Returns the result of [onSuccess] for the encapsulated value if this instance represents success
 * or the result of [onFailure] function for the encapsulated [Throwable] exception if it is failure.
 */
@kotlin.internal.InlineOnly
public inline fun <R, T> Result<T>.fold(
    onSuccess: (value: T) -> R,
    onFailure: (exception: Throwable) -> R
): R {
    return when (val exception = exceptionOrNull()) {
        null -> onSuccess(value as T)
        else -> onFailure(exception)
    }
}

/**
 * Calls the specified function [block] and returns its encapsulated result if invocation was successful,
 * catching any [Throwable] exception that was thrown from the [block] function execution and encapsulating it as a failure.
 */
@kotlin.internal.InlineOnly
@Suppress("UNCHECKED_CAST")
public inline fun <R> runCatching(block: () -> R): Result<R> {
    // Note: Rewritten to avoid try-catch as expression (not supported in BrightScript)
    // Initialize with a dummy value that will be overwritten
    var result: Result<R> = Result.success(null as R)
    try {
        result = Result.success(block())
    } catch (e: Throwable) {
        result = Result.failure(e)
    }
    return result
}

/**
 * Calls the specified function [block] with `this` value as its receiver and returns its encapsulated result if invocation was successful,
 * catching any [Throwable] exception that was thrown from the [block] function execution and encapsulating it as a failure.
 */
@kotlin.internal.InlineOnly
@Suppress("UNCHECKED_CAST")
public inline fun <T, R> T.runCatching(block: T.() -> R): Result<R> {
    // Note: Rewritten to avoid try-catch as expression (not supported in BrightScript)
    // Initialize with a dummy value that will be overwritten
    var result: Result<R> = Result.success(null as R)
    try {
        result = Result.success(block())
    } catch (e: Throwable) {
        result = Result.failure(e)
    }
    return result
}
