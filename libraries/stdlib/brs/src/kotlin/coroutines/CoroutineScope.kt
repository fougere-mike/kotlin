/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.coroutines

/**
 * Defines a scope for new coroutines.
 *
 * Every coroutine builder (like [launch][kotlinx.coroutines.launch], [async][kotlinx.coroutines.async], etc.)
 * is an extension on [CoroutineScope] and inherits its [coroutineContext] to automatically
 * propagate all its elements and cancellation.
 *
 * The best ways to obtain a standalone instance of the scope are [CoroutineScope] and
 * [MainScope] factory functions.
 *
 * Additional context elements can be appended to the scope using the [plus][CoroutineScope.plus]
 * operator.
 */
@Suppress("BRS_NAME_CASE_CLASH")
public interface CoroutineScope {
    /**
     * The context of this scope.
     *
     * Context is encapsulated by the scope and used for implementation of coroutine builders
     * that are extensions on the scope. Accessing this property in general code is not recommended
     * for any purposes except accessing the [Job] instance for advanced usages.
     *
     * By convention, should contain an instance of a [Job] to enforce structured concurrency.
     */
    public val coroutineContext: CoroutineContext
}

/**
 * Creates a [CoroutineScope] with the given context.
 *
 * This function is a shortcut for `object : CoroutineScope { override val coroutineContext = context }`.
 */
@Suppress("BRS_NAME_CASE_CLASH")
public fun CoroutineScope(context: CoroutineContext): CoroutineScope =
    CoroutineScopeImpl(context)

/**
 * Internal implementation of [CoroutineScope].
 */
internal class CoroutineScopeImpl(
    override val coroutineContext: CoroutineContext
) : CoroutineScope
