/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin

/**
 * Actual implementations of exception classes for BrightScript target.
 *
 * In BrightScript, exceptions are handled using try-catch blocks with roException.
 * These classes provide Kotlin-idiomatic exception handling that the BRS backend
 * translates to appropriate BrightScript error handling patterns.
 */

// ============================================
// Core Exception Hierarchy
// ============================================

public open class Error : Throwable {
    public constructor() : super()
    public constructor(message: String?) : super(message)
    public constructor(message: String?, cause: Throwable?) : super(message, cause)
    public constructor(cause: Throwable?) : super(cause)
}

public open class Exception : Throwable {
    public constructor() : super()
    public constructor(message: String?) : super(message)
    public constructor(message: String?, cause: Throwable?) : super(message, cause)
    public constructor(cause: Throwable?) : super(cause)
}

public open class RuntimeException : Exception {
    public constructor() : super()
    public constructor(message: String?) : super(message)
    public constructor(message: String?, cause: Throwable?) : super(message, cause)
    public constructor(cause: Throwable?) : super(cause)
}

// ============================================
// Argument and State Exceptions
// ============================================

public open class IllegalArgumentException : RuntimeException {
    public constructor() : super()
    public constructor(message: String?) : super(message)
    public constructor(message: String?, cause: Throwable?) : super(message, cause)
    public constructor(cause: Throwable?) : super(cause)
}

public open class IllegalStateException : RuntimeException {
    public constructor() : super()
    public constructor(message: String?) : super(message)
    public constructor(message: String?, cause: Throwable?) : super(message, cause)
    public constructor(cause: Throwable?) : super(cause)
}

// ============================================
// Index and Bounds Exceptions
// ============================================

public open class IndexOutOfBoundsException : RuntimeException {
    public constructor() : super()
    public constructor(message: String?) : super(message)
}

// ============================================
// Collection-Related Exceptions
// ============================================

public open class ConcurrentModificationException : RuntimeException {
    public constructor() : super()
    public constructor(message: String?) : super(message)
    public constructor(message: String?, cause: Throwable?) : super(message, cause)
    public constructor(cause: Throwable?) : super(cause)
}

public open class UnsupportedOperationException : RuntimeException {
    public constructor() : super()
    public constructor(message: String?) : super(message)
    public constructor(message: String?, cause: Throwable?) : super(message, cause)
    public constructor(cause: Throwable?) : super(cause)
}

public open class NoSuchElementException : RuntimeException {
    public constructor() : super()
    public constructor(message: String?) : super(message)
}

// ============================================
// Type-Related Exceptions
// ============================================

public open class NumberFormatException : IllegalArgumentException {
    public constructor() : super()
    public constructor(message: String?) : super(message)
}

public open class NullPointerException : RuntimeException {
    public constructor() : super()
    public constructor(message: String?) : super(message)
}

public open class ClassCastException : RuntimeException {
    public constructor() : super()
    public constructor(message: String?) : super(message)
}

// ============================================
// Assertion and Logic Exceptions
// ============================================

public open class AssertionError : Error {
    public constructor() : super()
    public constructor(message: Any?) : super(message?.toString())

    @SinceKotlin("1.9")
    public constructor(message: String?, cause: Throwable?) : super(message, cause)
}

@SinceKotlin("1.3")
public open class ArithmeticException : RuntimeException {
    public constructor() : super()
    public constructor(message: String?) : super(message)
}

// ============================================
// Compiler-Generated Exceptions
// ============================================

@Deprecated("This exception type is not supposed to be thrown or caught in common code and will be removed from kotlin-stdlib-common soon.", level = DeprecationLevel.ERROR)
public open class NoWhenBranchMatchedException : RuntimeException {
    public constructor() : super()
    public constructor(message: String?) : super(message)
    public constructor(message: String?, cause: Throwable?) : super(message, cause)
    public constructor(cause: Throwable?) : super(cause)
}

@Deprecated("This exception type is not supposed to be thrown or caught in common code and will be removed from kotlin-stdlib-common soon.", level = DeprecationLevel.ERROR)
public class UninitializedPropertyAccessException : RuntimeException {
    public constructor() : super()
    public constructor(message: String?) : super(message)
    public constructor(message: String?, cause: Throwable?) : super(message, cause)
    public constructor(cause: Throwable?) : super(cause)
}

// ============================================
// Throwable Extension Functions
// ============================================

/**
 * Returns the detailed description of this throwable with its stack trace.
 * In BrightScript, stack trace information is limited compared to JVM.
 */
@SinceKotlin("1.4")
public fun Throwable.stackTraceToString(): String {
    val sb = StringBuilder()
    sb.append(this.toString())
    sb.append("\n")

    // Get stack trace if available
    val stack = (this as? Throwable)?.getStack()
    if (stack != null) {
        sb.append(stack)
    }

    // Add suppressed exceptions
    val suppressed = this.suppressedExceptions
    if (suppressed.size > 0) {
        var i = 0
        while (i < suppressed.size) {
            sb.append("\nSuppressed: ")
            sb.append(suppressed[i].stackTraceToString())
            i++
        }
    }

    // Add cause
    val cause = this.cause
    if (cause != null) {
        sb.append("\nCaused by: ")
        sb.append(cause.stackTraceToString())
    }

    return sb.toString()
}

/**
 * Prints the detailed description of this throwable to the console.
 * In BrightScript, this uses the print statement.
 */
@SinceKotlin("1.4")
public fun Throwable.printStackTrace() {
    println(this.stackTraceToString())
}

// In BrightScript, we use a simplified approach without full collection support.
// Suppressed exceptions are stored directly on the Throwable via a backing property.

/**
 * Adds the specified exception to the list of suppressed exceptions.
 * In BrightScript, this is a simplified no-op implementation.
 */
@SinceKotlin("1.4")
public fun Throwable.addSuppressed(exception: Throwable) {
    // Simplified implementation for BrightScript - suppressed exceptions not fully supported
}

/**
 * Returns a list of all suppressed exceptions.
 * In BrightScript, this always returns an empty array.
 */
@SinceKotlin("1.4")
public val Throwable.suppressedExceptions: Array<Throwable>
    get() = arrayOf()
