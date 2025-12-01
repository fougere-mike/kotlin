/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.io

/**
 * Prints the line separator to the standard output stream.
 *
 * In BrightScript, this outputs to the debug console (BrightScript debugger).
 */
public fun println() {
    brsIntrinsicPrint("")
}

/**
 * Prints the given [message] and the line separator to the standard output stream.
 *
 * In BrightScript, this outputs to the debug console (BrightScript debugger).
 */
public fun println(message: Any?) {
    brsIntrinsicPrint(message?.toString() ?: "null")
}

/**
 * Prints the given [message] to the standard output stream.
 *
 * In BrightScript, this outputs to the debug console without a line separator.
 * Note: BrightScript's print statement always adds a newline, so this may behave
 * differently than on other platforms when multiple print calls are made.
 */
public fun print(message: Any?) {
    // BrightScript doesn't have a true "print without newline" so we use a special intrinsic
    brsIntrinsicPrintNoNewline(message?.toString() ?: "null")
}

/**
 * Reads a line of input from the standard input stream.
 *
 * @throws UnsupportedOperationException as Roku/BrightScript does not support console input.
 */
@SinceKotlin("1.6")
public fun readln(): String = throw UnsupportedOperationException("readln is not supported in Kotlin/BRS")

/**
 * Reads a line of input from the standard input stream.
 *
 * @throws UnsupportedOperationException as Roku/BrightScript does not support console input.
 */
@SinceKotlin("1.6")
public fun readlnOrNull(): String? = throw UnsupportedOperationException("readlnOrNull is not supported in Kotlin/BRS")

/**
 * Intrinsic function for printing with a newline.
 * This will be replaced by the BrightScript backend with a print statement.
 */
@PublishedApi
internal external fun brsIntrinsicPrint(message: String)

/**
 * Intrinsic function for printing without a newline.
 * This will be replaced by the BrightScript backend with appropriate print handling.
 * Note: BrightScript's print always adds newline, so the backend may need to buffer.
 */
@PublishedApi
internal external fun brsIntrinsicPrintNoNewline(message: String)
