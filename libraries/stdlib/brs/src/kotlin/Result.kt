/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin

/**
 * A discriminated union that encapsulates a successful outcome with a value of type [T]
 * or a failure with an arbitrary [Throwable] exception.
 */
@SinceKotlin("1.3")
public inline class Result<out T> @PublishedApi internal constructor(
    @PublishedApi
    internal val value: Any?
) {
    /**
     * Returns `true` if this instance represents a successful outcome.
     */
    public val isSuccess: Boolean get() = value !is Failure

    /**
     * Returns `true` if this instance represents a failed outcome.
     */
    public val isFailure: Boolean get() = value is Failure

    /**
     * Returns the encapsulated value if this instance represents success or `null` if it is failure.
     */
    public fun getOrNull(): T? =
        when {
            isFailure -> null
            else -> value as T
        }

    /**
     * Returns the encapsulated [Throwable] exception if this instance represents failure or `null` if it is success.
     */
    public fun exceptionOrNull(): Throwable? =
        when (value) {
            is Failure -> value.exception
            else -> null
        }

    /**
     * Returns the encapsulated value if this instance represents success or throws the encapsulated [Throwable] exception if it is failure.
     */
    public fun getOrThrow(): T {
        throwOnFailure()
        return value as T
    }

    @PublishedApi
    internal fun throwOnFailure() {
        if (value is Failure) throw value.exception
    }

    public companion object {
        /**
         * Returns an instance that encapsulates the given [value] as successful value.
         */
        public fun <T> success(value: T): Result<T> =
            Result(value)

        /**
         * Returns an instance that encapsulates the given [Throwable] [exception] as failure.
         */
        public fun <T> failure(exception: Throwable): Result<T> =
            Result(Failure(exception))
    }

    internal class Failure(
        val exception: Throwable
    )

    public override fun toString(): String =
        when (value) {
            is Failure -> "Failure(${value.exception})"
            else -> "Success($value)"
        }
}

/**
 * Creates an instance of internal marker [Result.Failure] class to
 * make sure that this class is not exposed in ABI.
 */
@PublishedApi
@SinceKotlin("1.3")
internal fun createFailure(exception: Throwable): Any =
    Result.Failure(exception)
