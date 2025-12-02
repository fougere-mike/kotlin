/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin

/**
 * Represents a value with lazy initialization.
 *
 * To create an instance of [Lazy] use the [lazy] function.
 */
public interface Lazy<out T> {
    /**
     * Gets the lazily initialized value of the current Lazy instance.
     * Once the value was initialized it must not change during the rest of lifetime of this Lazy instance.
     */
    public val value: T

    /**
     * Returns `true` if a value for this Lazy instance has been already initialized, and `false` otherwise.
     * Once this function has returned `true` it stays `true` for the rest of lifetime of this Lazy instance.
     */
    public fun isInitialized(): Boolean
}

/**
 * Specifies how a [Lazy] instance synchronizes initialization among multiple threads.
 *
 * In BrightScript, there's no multi-threading support, so all modes behave the same way.
 */
public enum class LazyThreadSafetyMode {
    /**
     * Locks are used to ensure that only a single thread can initialize the [Lazy] instance.
     */
    SYNCHRONIZED,

    /**
     * Initializer function can be called several times on concurrent access to uninitialized [Lazy] instance value,
     * but only the first returned value will be used as the value of [Lazy] instance.
     */
    PUBLICATION,

    /**
     * No locks are used to synchronize an access to the [Lazy] instance value; if the instance is accessed from multiple threads, its behavior is undefined.
     *
     * This mode should not be used unless the [Lazy] instance is guaranteed never to be initialized from more than one thread.
     */
    NONE
}
