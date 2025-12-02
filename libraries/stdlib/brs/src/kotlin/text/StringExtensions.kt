/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.text

/**
 * Returns `true` if this char sequence is not empty.
 */
@kotlin.internal.InlineOnly
public inline fun CharSequence.isNotEmpty(): Boolean = length > 0

/**
 * Returns `true` if this nullable char sequence is either `null` or empty.
 */
@kotlin.internal.InlineOnly
public inline fun CharSequence?.isNullOrEmpty(): Boolean = this == null || this.length == 0

/**
 * Returns `true` if this char sequence is empty (contains no characters).
 */
@kotlin.internal.InlineOnly
public inline fun CharSequence.isEmpty(): Boolean = length == 0

/**
 * Returns `true` if this string is not empty and contains some characters.
 */
@kotlin.internal.InlineOnly
public inline fun String.isNotEmpty(): Boolean = length > 0

/**
 * Returns `true` if this nullable string is either `null` or empty.
 */
@kotlin.internal.InlineOnly
public inline fun String?.isNullOrEmpty(): Boolean = this == null || this.length == 0
