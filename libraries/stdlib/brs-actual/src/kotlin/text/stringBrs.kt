/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.text

/**
 * BrightScript String extensions implementation.
 * Maps to BrightScript's built-in string functions.
 */

/**
 * Returns a copy of this string converted to upper case using the rules of the default locale.
 */
@Deprecated("Use uppercase() instead.", ReplaceWith("uppercase()"))
@DeprecatedSinceKotlin(warningSince = "1.5", errorSince = "2.1")
public actual fun String.toUpperCase(): String = brsIntrinsicUCase(this)

/**
 * Returns a copy of this string converted to upper case using Unicode mapping rules of the invariant locale.
 */
@SinceKotlin("1.5")
public actual fun String.uppercase(): String = brsIntrinsicUCase(this)

/**
 * Returns a copy of this string converted to lower case using the rules of the default locale.
 */
@Deprecated("Use lowercase() instead.", ReplaceWith("lowercase()"))
@DeprecatedSinceKotlin(warningSince = "1.5", errorSince = "2.1")
public actual fun String.toLowerCase(): String = brsIntrinsicLCase(this)

/**
 * Returns a copy of this string converted to lower case using Unicode mapping rules of the invariant locale.
 */
@SinceKotlin("1.5")
public actual fun String.lowercase(): String = brsIntrinsicLCase(this)

/**
 * Returns a copy of this string having its first letter titlecased.
 */
@Deprecated("Use replaceFirstChar instead.", ReplaceWith("replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }"))
@DeprecatedSinceKotlin(warningSince = "1.5", errorSince = "2.1")
public actual fun String.capitalize(): String {
    return if (isNotEmpty() && this[0].isLowerCase())
        this[0].uppercase() + substring(1)
    else
        this
}

/**
 * Returns a copy of this string having its first letter lowercased.
 */
@Deprecated("Use replaceFirstChar instead.", ReplaceWith("replaceFirstChar { it.lowercase() }"))
@DeprecatedSinceKotlin(warningSince = "1.5", errorSince = "2.1")
public actual fun String.decapitalize(): String {
    return if (isNotEmpty() && this[0].isUpperCase())
        this[0].lowercase() + substring(1)
    else
        this
}

/**
 * Returns `true` if the contents of this char sequence are equal to the contents of the specified [other].
 */
@SinceKotlin("1.5")
public actual infix fun CharSequence?.contentEquals(other: CharSequence?): Boolean {
    if (this === other) return true
    if (this == null || other == null) return false
    if (this.length != other.length) return false
    for (i in 0 until this.length) {
        if (this[i] != other[i]) return false
    }
    return true
}

/**
 * Returns `true` if the contents of this char sequence are equal to the contents of the specified [other],
 * optionally ignoring case difference.
 */
@SinceKotlin("1.5")
public actual fun CharSequence?.contentEquals(other: CharSequence?, ignoreCase: Boolean): Boolean {
    if (this === other) return true
    if (this == null || other == null) return false
    if (this.length != other.length) return false

    if (!ignoreCase) {
        for (i in 0 until this.length) {
            if (this[i] != other[i]) return false
        }
    } else {
        for (i in 0 until this.length) {
            if (!this[i].equals(other[i], ignoreCase = true)) return false
        }
    }
    return true
}

@kotlin.internal.InlineOnly
internal actual inline fun String.nativeIndexOf(str: String, fromIndex: Int): Int = brsIntrinsicInstr(fromIndex, this, str)

@kotlin.internal.InlineOnly
internal actual inline fun String.nativeLastIndexOf(str: String, fromIndex: Int): Int {
    // BrightScript doesn't have lastIndexOf, implement manually
    var lastIndex = -1
    var searchIndex = 0
    while (searchIndex <= fromIndex) {
        val found = brsIntrinsicInstr(searchIndex, this, str)
        if (found < 0 || found > fromIndex) break
        lastIndex = found
        searchIndex = found + 1
    }
    return lastIndex
}

/**
 * Intrinsic function for BrightScript UCase().
 * This will be lowered to: UCase(<str>)
 */
@PublishedApi
internal external fun brsIntrinsicUCase(str: String): String

/**
 * Intrinsic function for BrightScript LCase().
 * This will be lowered to: LCase(<str>)
 */
@PublishedApi
internal external fun brsIntrinsicLCase(str: String): String

/**
 * Intrinsic function for BrightScript Instr().
 * This will be lowered to: Instr(<start>, <source>, <substring>)
 * Returns 0-based index or -1 if not found.
 */
@PublishedApi
internal external fun brsIntrinsicInstr(start: Int, source: String, substring: String): Int
