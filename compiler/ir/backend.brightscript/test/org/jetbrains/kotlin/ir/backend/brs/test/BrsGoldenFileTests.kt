/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.test

import kotlin.test.Test

/**
 * Golden file tests for BrightScript code generation.
 *
 * These tests verify that Kotlin source compiles to expected BrightScript output.
 * Run with -PupdateGoldenFiles=true to regenerate expected output after intentional changes.
 */

// ==================== Declaration Tests ====================

class BrsDeclarationGoldenFileTests : AbstractBrsGoldenFileTest() {

    @Test
    fun simpleFunction() = runTest("declarations/simpleFunction.kt")

    @Test
    fun simpleClass() = runTest("declarations/simpleClass.kt")

    @Test
    fun dataClass() = runTest("declarations/dataClass.kt")
}

// ==================== Expression Tests ====================

class BrsExpressionGoldenFileTests : AbstractBrsGoldenFileTest() {

    @Test
    fun binaryOperators() = runTest("expressions/binaryOperators.kt")

    @Test
    fun whenExpression() = runTest("expressions/whenExpression.kt")

    @Test
    fun nullability() = runTest("expressions/nullability.kt")
}

// ==================== Control Flow Tests ====================

class BrsControlFlowGoldenFileTests : AbstractBrsGoldenFileTest() {

    @Test
    fun loops() = runTest("controlFlow/loops.kt")

    @Test
    fun conditionals() = runTest("controlFlow/conditionals.kt")
}

// ==================== Class Tests ====================

class BrsClassGoldenFileTests : AbstractBrsGoldenFileTest() {

    @Test
    fun inheritance() = runTest("classes/inheritance.kt")

    @Test
    fun interfaces() = runTest("classes/interfaces.kt")
}

// ==================== Closure Tests ====================

class BrsClosureGoldenFileTests : AbstractBrsGoldenFileTest() {

    @Test
    fun lambdaCapture() = runTest("closures/lambdaCapture.kt")
}

// ==================== Inline BrightScript Tests ====================

class BrsInlineGoldenFileTests : AbstractBrsGoldenFileTest() {

    @Test
    fun brsFunction() = runTest("inline/brsFunction.kt")
}

// ==================== SceneGraph Component Tests ====================

class BrsComponentGoldenFileTests : AbstractBrsGoldenFileTest() {

    @Test
    fun basicComponent() = runTest("components/basicComponent.kt")

    @Test
    fun sceneComponent() = runTest("components/sceneComponent.kt")

    @Test
    fun componentWithMAccess() = runTest("components/componentWithMAccess.kt")
}
