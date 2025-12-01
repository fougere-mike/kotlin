/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin

import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * A resource that can be closed or released.
 */
@SinceKotlin("2.0")
@WasExperimental(ExperimentalStdlibApi::class)
public actual interface AutoCloseable {
    /**
     * Closes this resource.
     */
    public actual fun close(): Unit
}

/**
 * Constructs an [AutoCloseable] instance with the given [closeAction] function.
 *
 * @param closeAction the action to be invoked upon calling [AutoCloseable.close].
 */
@SinceKotlin("2.0")
@kotlin.internal.InlineOnly
public actual inline fun AutoCloseable(crossinline closeAction: () -> Unit): AutoCloseable = object : AutoCloseable {
    override fun close() = closeAction()
}

/**
 * Executes the given [block] function on this resource and then closes it down correctly whether an exception
 * is thrown or not.
 *
 * @param block a function to process this [AutoCloseable] resource.
 * @return the result of [block] function invoked on this resource.
 */
@SinceKotlin("2.0")
@WasExperimental(ExperimentalStdlibApi::class)
@kotlin.internal.InlineOnly
public actual inline fun <T : AutoCloseable?, R> T.use(block: (T) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    var exception: Throwable? = null
    try {
        return block(this)
    } catch (e: Throwable) {
        exception = e
        throw e
    } finally {
        this.closeFinally(exception)
    }
}

@SinceKotlin("1.8")
@PublishedApi
internal fun AutoCloseable?.closeFinally(cause: Throwable?): Unit = when {
    this == null -> {}
    cause == null -> close()
    else ->
        try {
            close()
        } catch (closeException: Throwable) {
            cause.addSuppressed(closeException)
        }
}
