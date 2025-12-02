/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin

/**
 * Exception helper functions used by the BrightScript backend.
 * These are called by generated code for null checks, type casts, etc.
 */

@PublishedApi
@Suppress("DEPRECATION_ERROR")
internal fun throwUninitializedPropertyAccessException(name: String): Nothing =
    throw UninitializedPropertyAccessException("lateinit property $name has not been initialized")

@PublishedApi
internal fun throwKotlinNothingValueException(): Nothing =
    throw KotlinNothingValueException()

@Suppress("DEPRECATION_ERROR")
internal fun noWhenBranchMatchedException(): Nothing =
    throw NoWhenBranchMatchedException()

internal fun THROW_ISE(): Nothing {
    throw IllegalStateException()
}

internal fun THROW_CCE(): Nothing {
    throw ClassCastException()
}

internal fun THROW_NPE(): Nothing {
    throw NullPointerException()
}

internal fun THROW_IAE(msg: String): Nothing {
    throw IllegalArgumentException(msg)
}

internal fun <T : Any> ensureNotNull(v: T?): T =
    if (v == null) THROW_NPE() else v
