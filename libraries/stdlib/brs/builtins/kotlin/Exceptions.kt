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

public actual open class Error : Throwable {
    public actual constructor() : super()
    public actual constructor(message: String?) : super(message)
    public actual constructor(message: String?, cause: Throwable?) : super(message, cause)
    public actual constructor(cause: Throwable?) : super(cause)
}

public actual open class Exception : Throwable {
    public actual constructor() : super()
    public actual constructor(message: String?) : super(message)
    public actual constructor(message: String?, cause: Throwable?) : super(message, cause)
    public actual constructor(cause: Throwable?) : super(cause)
}

public actual open class RuntimeException : Exception {
    public actual constructor() : super()
    public actual constructor(message: String?) : super(message)
    public actual constructor(message: String?, cause: Throwable?) : super(message, cause)
    public actual constructor(cause: Throwable?) : super(cause)
}

// ============================================
// Argument and State Exceptions
// ============================================

public actual open class IllegalArgumentException : RuntimeException {
    public actual constructor() : super()
    public actual constructor(message: String?) : super(message)
    public actual constructor(message: String?, cause: Throwable?) : super(message, cause)
    public actual constructor(cause: Throwable?) : super(cause)
}

public actual open class IllegalStateException : RuntimeException {
    public actual constructor() : super()
    public actual constructor(message: String?) : super(message)
    public actual constructor(message: String?, cause: Throwable?) : super(message, cause)
    public actual constructor(cause: Throwable?) : super(cause)
}

// ============================================
// Index and Bounds Exceptions
// ============================================

public actual open class IndexOutOfBoundsException : RuntimeException {
    public actual constructor() : super()
    public actual constructor(message: String?) : super(message)
}

// ============================================
// Collection-Related Exceptions
// ============================================

public actual open class ConcurrentModificationException : RuntimeException {
    public actual constructor() : super()
    public actual constructor(message: String?) : super(message)
    public actual constructor(message: String?, cause: Throwable?) : super(message, cause)
    public actual constructor(cause: Throwable?) : super(cause)
}

public actual open class UnsupportedOperationException : RuntimeException {
    public actual constructor() : super()
    public actual constructor(message: String?) : super(message)
    public actual constructor(message: String?, cause: Throwable?) : super(message, cause)
    public actual constructor(cause: Throwable?) : super(cause)
}

public actual open class NoSuchElementException : RuntimeException {
    public actual constructor() : super()
    public actual constructor(message: String?) : super(message)
}

// ============================================
// Type-Related Exceptions
// ============================================

public actual open class NumberFormatException : IllegalArgumentException {
    public actual constructor() : super()
    public actual constructor(message: String?) : super(message)
}

public actual open class NullPointerException : RuntimeException {
    public actual constructor() : super()
    public actual constructor(message: String?) : super(message)
}

public actual open class ClassCastException : RuntimeException {
    public actual constructor() : super()
    public actual constructor(message: String?) : super(message)
}

// ============================================
// Assertion and Logic Exceptions
// ============================================

public actual open class AssertionError : Error {
    public actual constructor() : super()
    public actual constructor(message: Any?) : super(message?.toString())

    @SinceKotlin("1.9")
    public actual constructor(message: String?, cause: Throwable?) : super(message, cause)
}

@SinceKotlin("1.3")
public actual open class ArithmeticException : RuntimeException {
    public actual constructor() : super()
    public actual constructor(message: String?) : super(message)
}

// ============================================
// Compiler-Generated Exceptions
// ============================================

@Deprecated("This exception type is not supposed to be thrown or caught in common code and will be removed from kotlin-stdlib-common soon.", level = DeprecationLevel.ERROR)
public actual open class NoWhenBranchMatchedException : RuntimeException {
    public actual constructor() : super()
    public actual constructor(message: String?) : super(message)
    public actual constructor(message: String?, cause: Throwable?) : super(message, cause)
    public actual constructor(cause: Throwable?) : super(cause)
}

@Deprecated("This exception type is not supposed to be thrown or caught in common code and will be removed from kotlin-stdlib-common soon.", level = DeprecationLevel.ERROR)
public actual class UninitializedPropertyAccessException : RuntimeException {
    public actual constructor() : super()
    public actual constructor(message: String?) : super(message)
    public actual constructor(message: String?, cause: Throwable?) : super(message, cause)
    public actual constructor(cause: Throwable?) : super(cause)
}

// ============================================
// Throwable Extension Functions
// ============================================

/**
 * Returns the detailed description of this throwable with its stack trace.
 * In BrightScript, stack trace information is limited compared to JVM.
 */
@SinceKotlin("1.4")
public actual fun Throwable.stackTraceToString(): String {
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
    if (suppressed.isNotEmpty()) {
        for (e in suppressed) {
            sb.append("\nSuppressed: ")
            sb.append(e.stackTraceToString())
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
public actual fun Throwable.printStackTrace() {
    println(this.stackTraceToString())
}

// Suppressed exceptions storage - using a simple approach for BrightScript
private val suppressedExceptionsMap = mutableMapOf<Throwable, MutableList<Throwable>>()

/**
 * Adds the specified exception to the list of suppressed exceptions.
 */
@SinceKotlin("1.4")
public actual fun Throwable.addSuppressed(exception: Throwable) {
    val list = suppressedExceptionsMap.getOrPut(this) { mutableListOf() }
    list.add(exception)
}

/**
 * Returns a list of all suppressed exceptions.
 */
@SinceKotlin("1.4")
public actual val Throwable.suppressedExceptions: List<Throwable>
    get() = suppressedExceptionsMap[this] ?: emptyList()
