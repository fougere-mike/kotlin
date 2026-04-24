/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.brs

import org.junit.jupiter.api.Test

class BrsDiagnosticTests : AbstractBrsDiagnosticTest() {

    // BRS_INTRINSIC_LITERAL_REQUIRED — FIR-phase argument validation for `brs("...")`.

    @Test
    fun testBrsIntrinsicLiteralOk() {
        runTest("brsIntrinsic/literalOk.kt")
    }

    @Test
    fun testBrsIntrinsicVariableRejected() {
        runTest("brsIntrinsic/variableRejected.kt")
    }

    @Test
    fun testBrsIntrinsicFunctionCallRejected() {
        runTest("brsIntrinsic/functionCallRejected.kt")
    }

    @Test
    fun testBrsIntrinsicConstValTemplateOk() {
        runTest("brsIntrinsic/constValTemplateOk.kt")
    }

    @Test
    fun testBrsIntrinsicMutableValTemplateRejected() {
        runTest("brsIntrinsic/mutableValTemplateRejected.kt")
    }

    // BRS_NAME_CASE_CLASH — same-scope declarations whose names only differ in case
    // silently collide at BrightScript runtime (which is case-insensitive).

    @Test
    fun testNameCaseClashTopLevel() {
        runTest("nameCaseClash/topLevelClashRejected.kt")
    }

    @Test
    fun testNameCaseClashClassBody() {
        runTest("nameCaseClash/classBodyClashRejected.kt")
    }

    @Test
    fun testNameCaseClashMixedKinds() {
        runTest("nameCaseClash/mixedKindsClashRejected.kt")
    }

    @Test
    fun testNameCaseClashOverloadsAllowed() {
        runTest("nameCaseClash/noFalsePositiveIdenticalNames.kt")
    }

    @Test
    fun testNameCaseClashDifferentScopes() {
        runTest("nameCaseClash/noFalsePositiveDifferentScopes.kt")
    }

    @Test
    fun testNameCaseClashSuppressed() {
        runTest("nameCaseClash/suppressedClash.kt")
    }

    @Test
    fun testNameCaseClashNestedClassWithSameLowercaseMethod() {
        runTest("nameCaseClash/nestedClassWithSameLowercaseMethodOk.kt")
    }

    @Test
    fun testNameCaseClashClassBodyMethodMatchingClassName() {
        runTest("nameCaseClash/classBodyMethodMatchingClassNameOk.kt")
    }

    // BRS_SCENEGRAPH_FIELD_TYPE — property type must be compatible with its SG*Field annotation.

    @Test
    fun testScenegraphFieldTypeStringFieldRejectsInt() {
        runTest("scenegraphFieldType/stringFieldRejectsInt.kt")
    }

    @Test
    fun testScenegraphFieldTypeIntegerFieldRejectsString() {
        runTest("scenegraphFieldType/integerFieldRejectsString.kt")
    }

    @Test
    fun testScenegraphFieldTypeLongIntegerFieldRejectsInt() {
        runTest("scenegraphFieldType/longIntegerFieldRejectsInt.kt")
    }

    @Test
    fun testScenegraphFieldTypeFloatFieldRejectsInt() {
        runTest("scenegraphFieldType/floatFieldRejectsInt.kt")
    }

    @Test
    fun testScenegraphFieldTypeArrayFieldRejectsIntArray() {
        runTest("scenegraphFieldType/arrayFieldRejectsIntArray.kt")
    }

    @Test
    fun testScenegraphFieldTypeAssocArrayFieldRejectsList() {
        runTest("scenegraphFieldType/assocArrayFieldRejectsList.kt")
    }

    @Test
    fun testScenegraphFieldTypeNodeFieldRejectsString() {
        runTest("scenegraphFieldType/nodeFieldRejectsString.kt")
    }

    @Test
    fun testScenegraphFieldTypeColorFieldRejectsInt() {
        runTest("scenegraphFieldType/colorFieldRejectsInt.kt")
    }

    @Test
    fun testScenegraphFieldTypeFunctionFieldRejectsString() {
        runTest("scenegraphFieldType/functionFieldRejectsString.kt")
    }

    @Test
    fun testScenegraphFieldTypeStringFieldAcceptsString() {
        runTest("scenegraphFieldType/stringFieldAcceptsString.kt")
    }

    @Test
    fun testScenegraphFieldTypeNullableStringAccepted() {
        runTest("scenegraphFieldType/nullableStringAccepted.kt")
    }

    @Test
    fun testScenegraphFieldTypeNodeFieldAcceptsSubtype() {
        runTest("scenegraphFieldType/nodeFieldAcceptsSubtype.kt")
    }

    @Test
    fun testScenegraphFieldTypeArrayFieldAcceptsList() {
        runTest("scenegraphFieldType/arrayFieldAcceptsList.kt")
    }

    @Test
    fun testScenegraphFieldTypeArrayFieldAcceptsRoArray() {
        runTest("scenegraphFieldType/arrayFieldAcceptsRoArray.kt")
    }

    @Test
    fun testScenegraphFieldTypeFunctionFieldAcceptsFunctionType() {
        runTest("scenegraphFieldType/functionFieldAcceptsFunctionType.kt")
    }

    @Test
    fun testScenegraphFieldTypeSuppressedMismatch() {
        runTest("scenegraphFieldType/suppressedMismatch.kt")
    }

    @Test
    fun testScenegraphFieldTypeConflictingAnnotationsBothMismatch() {
        runTest("scenegraphFieldType/conflictingAnnotationsBothMismatch.kt")
    }

    @Test
    fun testScenegraphFieldTypeConflictingAnnotationsBothMatch() {
        runTest("scenegraphFieldType/conflictingAnnotationsBothMatch.kt")
    }

    @Test
    fun testScenegraphFieldTypeConflictingAnnotationsThreeAnnotations() {
        runTest("scenegraphFieldType/conflictingAnnotationsThreeAnnotations.kt")
    }

    @Test
    fun testScenegraphFieldTypeConflictingAnnotationsSuppressed() {
        runTest("scenegraphFieldType/conflictingAnnotationsSuppressed.kt")
    }

    @Test
    fun testScenegraphFieldTypeVector2dFieldAcceptsFloatArray() {
        runTest("scenegraphFieldType/vector2dFieldAcceptsFloatArray.kt")
    }

    @Test
    fun testScenegraphFieldTypeVector2dFieldRejectsString() {
        runTest("scenegraphFieldType/vector2dFieldRejectsString.kt")
    }

    // BRS_ONCHANGE_HANDLER_NOT_FOUND — @BrsOnChange("name") must refer to an existing member
    // function on the enclosing class (or a supertype). Companion-object handlers are not
    // accepted; inherited handlers are. Empty-string arg and the convention path are not checked.

    @Test
    fun testBrsOnChangeHandlerExplicitMissingRejected() {
        runTest("brsOnChangeHandler/explicitHandlerMissingRejected.kt")
    }

    @Test
    fun testBrsOnChangeHandlerTypoRejected() {
        runTest("brsOnChangeHandler/typoHandlerRejected.kt")
    }

    @Test
    fun testBrsOnChangeHandlerCompanionRejected() {
        runTest("brsOnChangeHandler/companionHandlerRejected.kt")
    }

    @Test
    fun testBrsOnChangeHandlerWithoutSGAnnotationRejected() {
        runTest("brsOnChangeHandler/fieldWithoutSGAnnotationRejected.kt")
    }

    @Test
    fun testBrsOnChangeHandlerExplicitExistsOk() {
        runTest("brsOnChangeHandler/explicitHandlerExistsOk.kt")
    }

    @Test
    fun testBrsOnChangeHandlerConventionWithoutAnnotationOk() {
        runTest("brsOnChangeHandler/conventionWithoutAnnotationOk.kt")
    }

    @Test
    fun testBrsOnChangeHandlerInheritedOk() {
        runTest("brsOnChangeHandler/inheritedHandlerOk.kt")
    }

    @Test
    fun testBrsOnChangeHandlerMultiplePropertiesMixed() {
        runTest("brsOnChangeHandler/multipleOnChangePropertiesMixed.kt")
    }

    @Test
    fun testBrsOnChangeHandlerSuppressed() {
        runTest("brsOnChangeHandler/suppressedMissingHandler.kt")
    }

    @Test
    fun testBrsOnChangeHandlerEmptyArgOk() {
        runTest("brsOnChangeHandler/emptyHandlerArgumentOk.kt")
    }

    // BRS_IO_WORKER_NON_SERIALIZABLE_CAPTURE — withContext(Dispatchers.IO) captures must be serializable.

    @Test
    fun testIoWorkerThisCaptureRejected() {
        runTest("ioWorkerCapture/thisCaptureRejected.kt")
    }

    @Test
    fun testIoWorkerNonSerializableLocalRejected() {
        runTest("ioWorkerCapture/nonSerializableLocalRejected.kt")
    }

    @Test
    fun testIoWorkerSerializableLocalsAccepted() {
        runTest("ioWorkerCapture/serializableLocalsAccepted.kt")
    }

    @Test
    fun testIoWorkerFunctionTypeCaptureRejected() {
        runTest("ioWorkerCapture/functionTypeCaptureRejected.kt")
    }

    @Test
    fun testIoWorkerCollectionCaptureRejected() {
        runTest("ioWorkerCapture/collectionCaptureRejected.kt")
    }
}
