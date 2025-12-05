/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin

// Note: UninitializedPropertyAccessException and NoWhenBranchMatchedException
// are defined in builtins/kotlin/Exceptions.kt - do not duplicate here.

/**
 * Exception thrown when a `Nothing` value is returned from a function.
 * This is used by the compiler for exhaustiveness checking.
 */
internal class KotlinNothingValueException : RuntimeException {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
}
