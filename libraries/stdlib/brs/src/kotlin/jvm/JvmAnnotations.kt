/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.jvm

/**
 * Specifies the name for the JVM method or field. For BrightScript, this is ignored.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.FILE)
@Retention(AnnotationRetention.SOURCE)
public annotation class JvmName(val name: String)

/**
 * Marks a field in a companion object as a static field. For BrightScript, this is ignored.
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
public annotation class JvmField

/**
 * Marks a function as a JVM synthetic method. For BrightScript, this is ignored.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.SOURCE)
public annotation class JvmSynthetic

/**
 * Instructs the Kotlin compiler to generate a multifile class. For BrightScript, this is ignored.
 */
@Target(AnnotationTarget.FILE)
@Retention(AnnotationRetention.SOURCE)
public annotation class JvmMultifileClass
