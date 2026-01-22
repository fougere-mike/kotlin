/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.coroutines.intrinsics

/**
 * BrightScript implementation of CoroutineSingletons.
 *
 * This enum provides singleton markers used internally by the coroutine machinery:
 * - COROUTINE_SUSPENDED: Signals that a coroutine has suspended
 * - UNDECIDED: Initial state before a continuation decides whether to suspend
 * - RESUMED: Indicates a continuation has already been resumed
 */
@SinceKotlin("1.3")
@PublishedApi
internal enum class CoroutineSingletons {
    COROUTINE_SUSPENDED,
    UNDECIDED,
    RESUMED
}

/**
 * The COROUTINE_SUSPENDED marker value.
 *
 * When a suspend function returns this value, it indicates that the function
 * has suspended and will be resumed later via its continuation.
 */
@SinceKotlin("1.3")
public val COROUTINE_SUSPENDED: Any get() = CoroutineSingletons.COROUTINE_SUSPENDED
