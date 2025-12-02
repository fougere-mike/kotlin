/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.sequences

/**
 * A sequence that can be iterated only once.
 * Throws IllegalStateException on subsequent iteration attempts.
 *
 * This is used internally by sequence operations to ensure that
 * sequences created from iterators can only be consumed once.
 */
internal class ConstrainedOnceSequence<T>(
    private val sequence: Sequence<T>
) : Sequence<T> {
    private var consumed = false

    override fun iterator(): Iterator<T> {
        if (consumed) {
            throw IllegalStateException("This sequence can be consumed only once.")
        }
        consumed = true
        return sequence.iterator()
    }
}
