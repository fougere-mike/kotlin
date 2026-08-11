/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.coroutines.cancellation

/**
 * Thrown by cancellable suspending functions when the coroutine is cancelled
 * while suspended, and used as the quiet completion cause of cancelled jobs.
 * An uncaught CancellationException cancels the coroutine — it is never
 * treated as a failure (no parent propagation, no unhandled reporting).
 */
public open class CancellationException : IllegalStateException {
    public constructor() : super()
    public constructor(message: String?) : super(message)
    public constructor(message: String?, cause: Throwable?) : super(message, cause)
}
