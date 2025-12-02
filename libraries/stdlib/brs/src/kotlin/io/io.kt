/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.io

import kotlin.brs.print

// Temporary for shared code, until we have an annotation like JvmSerializable
@Suppress("ACTUAL_WITHOUT_EXPECT")
internal actual interface Serializable

/**
 * Prints the given message and newline to the standard output stream.
 */
public fun println(message: Any?) {
    print(message)
    print("\n")
}

/**
 * Prints a newline to the standard output stream.
 */
public fun println() {
    print("\n")
}
