/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin

/**
 * Specifies that the corresponding member has internal visibility (visible only within the module).
 * For BrightScript, this is a marker annotation with no runtime effect.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
public annotation class InlineOnly

/**
 * When applied to a type parameter, requires the corresponding type argument to be only input to methods.
 * For BrightScript, this is a marker annotation with no runtime effect.
 */
@Target(AnnotationTarget.TYPE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
@SinceKotlin("1.9")
public annotation class OnlyInputTypes

/**
 * Marks a variable as thread-local. For BrightScript (single-threaded), this is a no-op marker.
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
public annotation class ThreadLocal

/**
 * Marks declarations that use unsigned types as experimental. For BrightScript, this is a marker annotation.
 */
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.TYPEALIAS
)
@Retention(AnnotationRetention.BINARY)
@SinceKotlin("1.3")
public annotation class ExperimentalUnsignedTypes
