/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin

/**
 * The base class for all errors and exceptions. Only instances of this class can be thrown or caught.
 *
 * In BrightScript, exceptions are represented as roAssociativeArray with error information.
 * The BRS backend generates appropriate error handling code.
 *
 * @param message the detail message string.
 * @param cause the cause of this throwable.
 */
public actual open class Throwable actual constructor(
    public actual open val message: String?,
    public actual open val cause: Throwable?
) {
    public actual constructor(message: String?) : this(message, null)
    public actual constructor(cause: Throwable?) : this(cause?.toString(), cause)
    public actual constructor() : this(null, null)

    private var _stack: String? = null

    /**
     * Captures the current stack trace.
     * In BrightScript, this is done via the backend using roException when available.
     */
    internal fun captureStack() {
        // The backend will lower this to appropriate BrightScript stack capture
        // For now, stack trace capture is a no-op until backend intrinsic is added
        _stack = null
    }

    internal fun getStack(): String? = _stack
    internal fun setStack(stack: String?) { _stack = stack }

    override fun toString(): String {
        val className = this::class.simpleName ?: "Throwable"
        return if (message != null) "$className: $message" else className
    }
}
