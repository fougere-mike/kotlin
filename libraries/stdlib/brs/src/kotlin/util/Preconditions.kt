/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.util

/**
 * Internal helper functions for bounds checking.
 * These mirror the internal functions in AbstractList but are accessible within the BRS stdlib.
 */

internal fun checkBoundsIndexes(startIndex: Int, endIndex: Int, size: Int) {
    if (startIndex < 0 || endIndex > size) {
        throw IndexOutOfBoundsException("startIndex: $startIndex, endIndex: $endIndex, size: $size")
    }
    if (startIndex > endIndex) {
        throw IllegalArgumentException("startIndex: $startIndex > endIndex: $endIndex")
    }
}

internal fun checkElementIndex(index: Int, size: Int) {
    if (index < 0 || index >= size) {
        throw IndexOutOfBoundsException("index: $index, size: $size")
    }
}

internal fun checkPositionIndex(index: Int, size: Int) {
    if (index < 0 || index > size) {
        throw IndexOutOfBoundsException("index: $index, size: $size")
    }
}

internal fun checkRangeIndexes(fromIndex: Int, toIndex: Int, size: Int) {
    if (fromIndex < 0 || toIndex > size) {
        throw IndexOutOfBoundsException("fromIndex: $fromIndex, toIndex: $toIndex, size: $size")
    }
    if (fromIndex > toIndex) {
        throw IllegalArgumentException("fromIndex: $fromIndex > toIndex: $toIndex")
    }
}

internal fun numberFormatError(input: String): Nothing {
    throw NumberFormatException("Invalid number format: '$input'")
}
