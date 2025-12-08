/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.test

/**
 * Current adapter providing assertion implementations.
 */
public val asserter: Asserter
    get() = _asserter ?: lookupAsserter()

/**
 * Used to override current asserter internally.
 */
internal var _asserter: Asserter? = null

/**
 * Interface for assertion implementations.
 */
public interface Asserter {
    /**
     * Asserts that the specified values are equal.
     */
    fun assertEquals(message: String?, expected: Any?, actual: Any?)

    /**
     * Asserts that the specified values are not equal.
     */
    fun assertNotEquals(message: String?, illegal: Any?, actual: Any?)

    /**
     * Asserts that the specified values refer to the same instance.
     */
    fun assertSame(message: String?, expected: Any?, actual: Any?)

    /**
     * Asserts that the specified values do not refer to the same instance.
     */
    fun assertNotSame(message: String?, illegal: Any?, actual: Any?)

    /**
     * Asserts that the specified value is true.
     */
    fun assertTrue(message: String?, actual: Boolean)

    /**
     * Asserts that the specified value is false.
     */
    fun assertFalse(message: String?, actual: Boolean)

    /**
     * Asserts that the specified value is not null.
     */
    fun assertNotNull(message: String?, actual: Any?)

    /**
     * Asserts that the specified value is null.
     */
    fun assertNull(message: String?, actual: Any?)

    /**
     * Fails the test with the specified message.
     */
    fun fail(message: String?): Nothing

    /**
     * Fails the test with the specified message and cause.
     */
    fun fail(message: String?, cause: Throwable?): Nothing {
        fail(message)
    }
}

/**
 * Helper to format message prefix.
 */
@PublishedApi
internal fun messagePrefix(message: String?): String = if (message == null) "" else "$message. "

// ============ Assertion Functions ============

/**
 * Asserts that the expression is true with an optional [message].
 */
public fun assertTrue(actual: Boolean, message: String? = null) {
    asserter.assertTrue(message ?: "Expected value to be true.", actual)
}

/**
 * Asserts that the given [block] returns true.
 */
public inline fun assertTrue(message: String? = null, block: () -> Boolean) {
    assertTrue(block(), message)
}

/**
 * Asserts that the expression is false with an optional [message].
 */
public fun assertFalse(actual: Boolean, message: String? = null) {
    asserter.assertFalse(message ?: "Expected value to be false.", actual)
}

/**
 * Asserts that the given [block] returns false.
 */
public inline fun assertFalse(message: String? = null, block: () -> Boolean) {
    assertFalse(block(), message)
}

/**
 * Asserts that [expected] is equal to [actual], with an optional [message].
 */
public fun <T> assertEquals(expected: T, actual: T, message: String? = null) {
    asserter.assertEquals(message, expected, actual)
}

/**
 * Asserts that [actual] is not equal to [illegal], with an optional [message].
 */
public fun <T> assertNotEquals(illegal: T, actual: T, message: String? = null) {
    asserter.assertNotEquals(message, illegal, actual)
}

/**
 * Asserts that [expected] is the same instance as [actual], with an optional [message].
 */
public fun <T> assertSame(expected: T, actual: T, message: String? = null) {
    asserter.assertSame(message, expected, actual)
}

/**
 * Asserts that [actual] is not the same instance as [illegal], with an optional [message].
 */
public fun <T> assertNotSame(illegal: T, actual: T, message: String? = null) {
    asserter.assertNotSame(message, illegal, actual)
}

/**
 * Asserts that [actual] is not null, with an optional [message].
 */
public fun <T : Any> assertNotNull(actual: T?, message: String? = null): T {
    asserter.assertNotNull(message, actual)
    return actual!!
}

/**
 * Asserts that [actual] is not null, with an optional [message], and executes [block] with the non-null value.
 */
public inline fun <T : Any, R> assertNotNull(actual: T?, message: String? = null, block: (T) -> R): R {
    asserter.assertNotNull(message, actual)
    return block(actual!!)
}

/**
 * Asserts that [actual] is null, with an optional [message].
 */
public fun assertNull(actual: Any?, message: String? = null) {
    asserter.assertNull(message, actual)
}

/**
 * Fails the current test with the given [message].
 */
public fun fail(message: String? = null): Nothing {
    asserter.fail(message)
}

/**
 * Fails the current test with the given [message] and [cause].
 */
public fun fail(message: String? = null, cause: Throwable?): Nothing {
    asserter.fail(message, cause)
}

// Note: assertFailsWith and assertFails are disabled for BrightScript target
// due to code generation issues with try-catch blocks.
// Uncomment when the compiler properly handles these constructs.

/*
public inline fun <reified T : Throwable> assertFailsWith(message: String? = null, block: () -> Unit): Throwable {
    try {
        block()
    } catch (e: Throwable) {
        return e
    }
    fail(messagePrefix(message) + "Expected an exception to be thrown, but was completed successfully.")
}

public inline fun assertFails(message: String? = null, block: () -> Unit): Throwable {
    try {
        block()
    } catch (e: Throwable) {
        return e
    }
    fail(messagePrefix(message) + "Expected an exception to be thrown, but was completed successfully.")
}
*/
