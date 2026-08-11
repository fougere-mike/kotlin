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

    @Test
    fun dataClassWithCompanion() = runTest("declarations/dataClassWithCompanion.kt")
}

// ==================== Expression Tests ====================

class BrsExpressionGoldenFileTests : AbstractBrsGoldenFileTest() {

    @Test
    fun binaryOperators() = runTest("expressions/binaryOperators.kt")

    @Test
    fun whenExpression() = runTest("expressions/whenExpression.kt")

    @Test
    fun nullability() = runTest("expressions/nullability.kt")

    @Test
    fun safeCast() = runTest("expressions/safeCast.kt")

    @Test
    fun externalBrsNameOverloads() = runTest("expressions/externalBrsNameOverloads.kt")

    @Test
    fun arrayFactories() = runTest("expressions/arrayFactories.kt")

    @Test
    fun shortCircuit() = runTest("expressions/shortCircuit.kt")
}

// ==================== Control Flow Tests ====================

class BrsControlFlowGoldenFileTests : AbstractBrsGoldenFileTest() {

    @Test
    fun loops() = runTest("controlFlow/loops.kt")

    @Test
    fun conditionals() = runTest("controlFlow/conditionals.kt")

    @Test
    fun doWhileConditionScope() = runTest("controlFlow/doWhileConditionScope.kt")

    @Test
    fun tryCatchExpression() = runTest("controlFlow/tryCatchExpression.kt")
}

// ==================== Class Tests ====================

class BrsClassGoldenFileTests : AbstractBrsGoldenFileTest() {

    @Test
    fun inheritance() = runTest("classes/inheritance.kt")

    @Test
    fun interfaces() = runTest("classes/interfaces.kt")

    @Test
    fun interfaceDefaultMethods() = runTest("classes/interfaceDefaultMethods.kt")

    @Test
    fun enumConstructorProperties() = runTest("classes/enumConstructorProperties.kt")
}

// ==================== Closure Tests ====================

class BrsClosureGoldenFileTests : AbstractBrsGoldenFileTest() {

    @Test
    fun lambdaCapture() = runTest("closures/lambdaCapture.kt")

    @Test
    fun capturedVarInAnonymousClass() = runTest("closures/capturedVarInAnonymousClass.kt")

    @Test
    fun nestedLambdaCaptureHoisting() = runTest("closures/nestedLambdaCaptureHoisting.kt")

    @Test
    fun nestedLambdaSuspendCaptureWrite() = runTest("closures/nestedLambdaSuspendCaptureWrite.kt")
}

// ==================== Coroutine Tests ====================

class BrsCoroutineGoldenFileTests : AbstractBrsGoldenFileTest() {

    @Test
    fun simpleSuspend() = runTest("coroutines/simpleSuspend.kt")

    @Test
    fun suspendResumeValue() = runTest("coroutines/suspendResumeValue.kt")

    @Test
    fun suspendStatementDiscard() = runTest("coroutines/suspendStatementDiscard.kt")

    @Test
    fun suspendInStringTemplate() = runTest("coroutines/suspendInStringTemplate.kt")

    @Test
    fun suspendInTryCatch() = runTest("coroutines/suspendInTryCatch.kt")
}

// ==================== Inline BrightScript Tests ====================

class BrsInlineGoldenFileTests : AbstractBrsGoldenFileTest() {

    @Test
    fun brsFunction() = runTest("inline/brsFunction.kt")

    @Test
    fun brsConstVal() = runTest("inline/brsConstVal.kt")
}

// ==================== SceneGraph Component Tests ====================

class BrsComponentGoldenFileTests : AbstractBrsGoldenFileTest() {

    @Test
    fun basicComponent() = runTest("components/basicComponent.kt")

    @Test
    fun sceneComponent() = runTest("components/sceneComponent.kt")

    @Test
    fun componentWithMAccess() = runTest("components/componentWithMAccess.kt")

    @Test
    fun inheritedField() = runTest("components/inheritedField.kt")

    @Test
    fun inheritedExport() = runTest("components/inheritedExport.kt")

    @Test
    fun inheritedOnChangeHandler() = runTest("components/inheritedOnChangeHandler.kt")

    @Test
    fun overriddenFieldKeepsBaseAnnotation() = runTest("components/overriddenFieldKeepsBaseAnnotation.kt")

    @Test
    fun overriddenFieldChangesAnnotation() = runTest("components/overriddenFieldChangesAnnotation.kt")

    @Test
    fun deepTransitiveDeps() = runTest("components/deepTransitiveDeps.kt")

    @Test
    fun taskComponentBasic() = runTest("components/taskComponentBasic.kt")

    @Test
    fun taskComponentCallSite() = runTest("components/taskComponentCallSite.kt")

    @Test
    fun taskComponentInheritedRun() = runTest("components/taskComponentInheritedRun.kt")

    @Test
    fun runTaskCallSite() = runTest("components/runTaskCallSite.kt")

    @Test
    fun componentExternalFieldAccess() = runTest("components/componentExternalFieldAccess.kt")

    @Test
    fun lambdaSelfWriteToField() = runTest("components/lambdaSelfWriteToField.kt")

    @Test
    fun brsFieldNamedArgs() = runTest("components/brsFieldNamedArgs.kt")

    @Test
    fun nodeFieldNodeType() = runTest("components/nodeFieldNodeType.kt")

    @Test
    fun layoutStubAccess() = runTest("components/layoutStubAccess.kt")

    @Test
    fun componentLaunchExtension() = runTest("components/componentLaunchExtension.kt")

    @Test
    fun componentPumpAttachMethodUse() = runTest("components/componentPumpAttachMethodUse.kt")

    @Test
    fun componentNoCoroutinesNoPumpAttach() = runTest("components/componentNoCoroutinesNoPumpAttach.kt")
}

// ==================== Intrinsics Tests ====================

class BrsIntrinsicsGoldenFileTests : AbstractBrsGoldenFileTest() {
    // brsName test disabled pending implementation of brsName() for callable references
    // @Test
    // fun brsName() = runTest("intrinsics/brsName.kt")
}

// ==================== @BrsConstant Tests ====================

class BrsConstantGoldenFileTests : AbstractBrsGoldenFileTest() {

    @Test
    fun brsConstantBasic() = runTest("constants/brsConstantBasic.kt")

    @Test
    fun brsConstantTransitive() = runTest("constants/brsConstantTransitive.kt")

    @Test
    fun brsConstantArithmetic() = runTest("constants/brsConstantArithmetic.kt")

    @Test
    fun brsConstantBoolean() = runTest("constants/brsConstantBoolean.kt")

    @Test
    fun brsConstantEnum() = runTest("constants/brsConstantEnum.kt")
}

// ==================== Reflection Tests ====================

class BrsReflectionGoldenFileTests : AbstractBrsGoldenFileTest() {

    @Test
    fun classReference() = runTest("reflection/classReference.kt")

    @Test
    fun getClass() = runTest("reflection/getClass.kt")

    @Test
    fun kclassEquality() = runTest("reflection/kclassEquality.kt")

    @Test
    fun nativeTypeClass() = runTest("reflection/nativeTypeClass.kt")
}
