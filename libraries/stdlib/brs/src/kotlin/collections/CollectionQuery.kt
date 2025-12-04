/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.collections

/**
 * Returns `true` if the collection is not empty.
 */
@kotlin.internal.InlineOnly
public inline fun <T> Collection<T>.isNotEmpty(): Boolean = !isEmpty()

// Note: first(), firstOrNull(), last(), lastOrNull() are defined in CollectionExtensions.kt
// to avoid duplicate function definitions in the compiled BrightScript output.
