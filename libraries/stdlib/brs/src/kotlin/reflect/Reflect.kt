/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.reflect

/**
 * Represents a type. This is a minimal stub for BrightScript.
 */
public interface KType {
    /**
     * The classifier used in the type (class, interface, type parameter).
     */
    public val classifier: KClassifier?
}

/**
 * Represents a classifier (class, interface, type parameter).
 */
public interface KClassifier

/**
 * Represents a class at runtime.
 *
 * Instances are created by:
 * - Class literal references: `Person::class`
 * - Getting class from instance: `person::class`
 *
 * The runtime implementation supports:
 * - [simpleName]: Returns the class name (e.g., "Person" or "roArray")
 * - [qualifiedName]: Always returns null (not supported in BrightScript)
 * - [isInstance]: Checks if a value is an instance of this class
 * - [equals]/[hashCode]: For use as map keys (e.g., `MutableMap<KClass<*>, Handler>`)
 */
public interface KClass<T : Any> : KClassifier {
    /**
     * The simple name of the class as declared in the source code,
     * or the BrightScript type name for native types (e.g., "roArray").
     */
    public val simpleName: String?

    /**
     * The fully qualified name of the class.
     * Always returns null in BrightScript as qualified names are not tracked.
     */
    public val qualifiedName: String? get() = null

    /**
     * Returns true if [value] is an instance of this class.
     *
     * For Kotlin classes, checks the __proto chain.
     * For native BrightScript types, compares against the Type() result.
     */
    public fun isInstance(value: Any?): Boolean

    /**
     * Checks equality with another KClass.
     * Two KClass instances are equal if they represent the same class.
     */
    override fun equals(other: Any?): Boolean

    /**
     * Returns a hash code for this KClass, consistent with equals.
     */
    override fun hashCode(): Int
}

/**
 * Returns a KType instance representing the type of the expression.
 * This is a stub implementation for BrightScript that returns a placeholder type.
 */
@Suppress("UNUSED_PARAMETER")
public inline fun <reified T> typeOf(): KType = object : KType {
    override val classifier: KClassifier? = null
}
