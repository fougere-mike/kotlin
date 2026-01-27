/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin

/**
 * The base class for all errors and exceptions. Only instances of this class can be thrown or caught.
 *
 * In BrightScript, exceptions are represented as roAssociativeArray with error information.
 * BrightScript requires exception objects to have specific fields: number, message, and optionally backtrace.
 * The `number` field is the BrightScript error code (ERR_USER = 0x28 for user-thrown exceptions).
 *
 * @param message the detail message string.
 * @param cause the cause of this throwable.
 */
public open class Throwable(
    public open val message: String?,
    public open val cause: Throwable?
) {
    public constructor(message: String?) : this(message, null)
    public constructor(cause: Throwable?) : this(cause?.toString(), cause)
    public constructor() : this(null, null)

    /**
     * BrightScript error number. Required for BrightScript throw compatibility.
     * ERR_USER (0x28 = 40) is used for user-thrown exceptions.
     */
    public open val number: Int = 0x28

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
