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
 * Represents a class. This is a minimal stub for BrightScript.
 */
public interface KClass<T : Any> : KClassifier {
    /**
     * Simple name of the class.
     */
    public val simpleName: String?
}

/**
 * Returns a KType instance representing the type of the expression.
 * This is a stub implementation for BrightScript that returns a placeholder type.
 */
@Suppress("UNUSED_PARAMETER")
public inline fun <reified T> typeOf(): KType = object : KType {
    override val classifier: KClassifier? = null
}
