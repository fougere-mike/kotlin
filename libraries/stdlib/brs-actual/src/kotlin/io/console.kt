/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.io

/**
 * BrightScript console output implementation.
 * Maps to BrightScript's built-in print statement.
 */

/** Prints the line separator to the standard output stream. */
public fun println() {
    // BrightScript print adds newline automatically
    brsIntrinsicPrint("")
}

/** Prints the given [message] and the line separator to the standard output stream. */
public fun println(message: Any?) {
    brsIntrinsicPrint(message?.toString() ?: "null")
}

/** Prints the given [message] to the standard output stream. */
public fun print(message: Any?) {
    // BrightScript doesn't have a print without newline, but we can use semicolon
    // to suppress the newline in code generation
    brsIntrinsicPrintNoNewline(message?.toString() ?: "null")
}

@SinceKotlin("1.6")
public fun readln(): String =
    throw UnsupportedOperationException("readln is not supported in BrightScript")

@SinceKotlin("1.6")
public fun readlnOrNull(): String? =
    throw UnsupportedOperationException("readlnOrNull is not supported in BrightScript")

/**
 * Intrinsic function for BrightScript print statement.
 * This will be lowered to: print <message>
 */
@PublishedApi
internal external fun brsIntrinsicPrint(message: String)

/**
 * Intrinsic function for BrightScript print without newline.
 * This will be lowered to: print <message>;
 */
@PublishedApi
internal external fun brsIntrinsicPrintNoNewline(message: String)
