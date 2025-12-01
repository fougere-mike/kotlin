/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.collections

internal fun <T> Array<T>.resetAt(index: Int) {
    @Suppress("UNCHECKED_CAST")
    (this as Array<Any?>)[index] = null
}

internal fun <T> Array<T>.resetRange(fromIndex: Int, toIndex: Int) {
    for (i in fromIndex until toIndex) {
        @Suppress("UNCHECKED_CAST")
        (this as Array<Any?>)[i] = null
    }
}

internal fun <T> Array<T>.copyOfUninitializedElements(newSize: Int): Array<T> {
    @Suppress("UNCHECKED_CAST")
    return this.copyOf(newSize) as Array<T>
}

internal fun <T> arrayOfUninitializedElements(capacity: Int): Array<T> {
    require(capacity >= 0) { "capacity must be non-negative." }
    @Suppress("UNCHECKED_CAST")
    return arrayOfNulls<Any>(capacity) as Array<T>
}
