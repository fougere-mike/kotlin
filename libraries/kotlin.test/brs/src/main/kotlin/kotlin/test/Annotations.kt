/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.test

/**
 * Marks a function or class as a test.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
public actual annotation class Test

/**
 * Marks a test or test class as ignored.
 *
 * @param reason the reason why the test is ignored
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
public actual annotation class Ignore(actual val reason: String)

/**
 * Marks a function to be invoked before each test.
 */
@Target(AnnotationTarget.FUNCTION)
public actual annotation class BeforeTest

/**
 * Marks a function to be invoked after each test.
 */
@Target(AnnotationTarget.FUNCTION)
public actual annotation class AfterTest
