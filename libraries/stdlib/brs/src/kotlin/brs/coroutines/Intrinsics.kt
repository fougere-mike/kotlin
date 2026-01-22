/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs.coroutines

import kotlin.coroutines.Continuation

/**
 * Returns the current continuation.
 *
 * This is a compiler intrinsic function. Calls to this function are transformed
 * by the compiler during coroutine lowering to access the continuation parameter
 * that is added to suspend functions.
 *
 * This function should not be called directly from user code.
 */
@PublishedApi
internal fun <T> getContinuation(): Continuation<T> {
    // This is a compiler intrinsic - the actual implementation is provided
    // by the compiler through continuation parameter insertion.
    throw UnsupportedOperationException("getContinuation is a compiler intrinsic")
}

/**
 * Returns the coroutine context of the current coroutine.
 *
 * This is a compiler intrinsic function. Calls to this function are transformed
 * by the compiler to access the context from the current continuation.
 */
@PublishedApi
internal fun getCoroutineContext(): kotlin.coroutines.CoroutineContext {
    // This is a compiler intrinsic
    throw UnsupportedOperationException("getCoroutineContext is a compiler intrinsic")
}
