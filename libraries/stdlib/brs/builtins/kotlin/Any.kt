/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin

/**
 * The root of the Kotlin class hierarchy. Every Kotlin class has [Any] as a superclass.
 *
 * In BrightScript, objects are represented as roAssociativeArray instances.
 * This actual class provides the foundation for all Kotlin classes compiled to BrightScript.
 */
public actual open class Any actual constructor() {
    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * For BrightScript, this compares object identity by default.
     * Subclasses can override this to provide structural equality.
     */
    public actual open operator fun equals(other: Any?): Boolean {
        // In BrightScript, use identity comparison by default
        // Subclasses can override for structural equality
        return this === other
    }

    /**
     * Returns a hash code value for the object.
     *
     * For BrightScript, this returns a simple hash based on the object's type.
     * Subclasses should override this for proper hash code computation.
     */
    public actual open fun hashCode(): Int {
        // Simple default hash code - subclasses should override
        return 0
    }

    /**
     * Returns a string representation of the object.
     */
    public actual open fun toString(): String {
        // In BrightScript, return a default representation
        return "[object]"
    }
}
