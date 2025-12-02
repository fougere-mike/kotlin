/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.text

/**
 * BrightScript Char extensions implementation.
 */

/**
 * Converts this character to lower case using Unicode mapping rules of the invariant locale.
 */
@Deprecated("Use lowercaseChar() instead.", ReplaceWith("lowercaseChar()"))
@DeprecatedSinceKotlin(warningSince = "1.5", errorSince = "2.1")
public inline fun Char.toLowerCase(): Char = lowercaseChar()

/**
 * Converts this character to lower case using Unicode mapping rules of the invariant locale.
 */
@SinceKotlin("1.5")
public inline fun Char.lowercaseChar(): Char = lowercase()[0]

/**
 * Converts this character to lower case using Unicode mapping rules of the invariant locale.
 */
@SinceKotlin("1.5")
public fun Char.lowercase(): String = brsIntrinsicLCase(this.toString())

/**
 * Converts this character to upper case using Unicode mapping rules of the invariant locale.
 */
@Deprecated("Use uppercaseChar() instead.", ReplaceWith("uppercaseChar()"))
@DeprecatedSinceKotlin(warningSince = "1.5", errorSince = "2.1")
public inline fun Char.toUpperCase(): Char = uppercaseChar()

/**
 * Converts this character to upper case using Unicode mapping rules of the invariant locale.
 */
@SinceKotlin("1.5")
public fun Char.uppercaseChar(): Char {
    val uppercase = uppercase()
    return if (uppercase.length > 1) this else uppercase[0]
}

/**
 * Converts this character to upper case using Unicode mapping rules of the invariant locale.
 */
@SinceKotlin("1.5")
public fun Char.uppercase(): String = brsIntrinsicUCase(this.toString())

/**
 * Converts this character to title case using Unicode mapping rules of the invariant locale.
 */
@SinceKotlin("1.5")
public fun Char.titlecaseChar(): Char = uppercaseChar()

/**
 * Returns the Unicode general category of this character.
 */
@SinceKotlin("1.5")
public val Char.category: CharCategory
    get() {
        // Simplified implementation for BrightScript - basic ASCII categories
        return when {
            this in 'A'..'Z' -> CharCategory.UPPERCASE_LETTER
            this in 'a'..'z' -> CharCategory.LOWERCASE_LETTER
            this in '0'..'9' -> CharCategory.DECIMAL_DIGIT_NUMBER
            this == ' ' || this == '\t' -> CharCategory.SPACE_SEPARATOR
            this in '\u0000'..'\u001F' || this in '\u007F'..'\u009F' -> CharCategory.CONTROL
            else -> CharCategory.OTHER_SYMBOL
        }
    }

/**
 * Returns `true` if this character is defined in Unicode.
 */
@SinceKotlin("1.5")
public fun Char.isDefined(): Boolean = true

/**
 * Returns `true` if this character is a letter.
 */
@SinceKotlin("1.5")
public fun Char.isLetter(): Boolean = this in 'a'..'z' || this in 'A'..'Z'

/**
 * Returns `true` if this character is a letter or digit.
 */
@SinceKotlin("1.5")
public fun Char.isLetterOrDigit(): Boolean = isLetter() || isDigit()

/**
 * Returns `true` if this character is a digit.
 */
@SinceKotlin("1.5")
public fun Char.isDigit(): Boolean = this in '0'..'9'

/**
 * Returns `true` if this character is upper case.
 */
@SinceKotlin("1.5")
public fun Char.isUpperCase(): Boolean = this in 'A'..'Z'

/**
 * Returns `true` if this character is lower case.
 */
@SinceKotlin("1.5")
public fun Char.isLowerCase(): Boolean = this in 'a'..'z'

/**
 * Returns `true` if this character is a title case letter.
 */
@SinceKotlin("1.5")
public fun Char.isTitleCase(): Boolean = false

/**
 * Returns `true` if this character is an ISO control character.
 */
@SinceKotlin("1.5")
public fun Char.isISOControl(): Boolean = this <= '\u001F' || this in '\u007F'..'\u009F'

/**
 * Determines whether a character is whitespace.
 */
public fun Char.isWhitespace(): Boolean = this == ' ' || this == '\t' || this == '\n' || this == '\r'

/**
 * Returns `true` if this character is a high surrogate.
 */
public fun Char.isHighSurrogate(): Boolean = this in '\uD800'..'\uDBFF'

/**
 * Returns `true` if this character is a low surrogate.
 */
public fun Char.isLowSurrogate(): Boolean = this in '\uDC00'..'\uDFFF'

/**
 * Returns `true` if this character is a surrogate code unit.
 */
public fun Char.isSurrogate(): Boolean = isHighSurrogate() || isLowSurrogate()

/**
 * Returns `true` if this character is equal to the [other] character, optionally ignoring case.
 */
public fun Char.equals(other: Char, ignoreCase: Boolean = false): Boolean {
    if (this == other) return true
    if (!ignoreCase) return false
    return this.lowercaseChar() == other.lowercaseChar()
}
