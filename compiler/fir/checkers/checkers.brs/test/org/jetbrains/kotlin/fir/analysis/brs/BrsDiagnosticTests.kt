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

    @Test
    fun testNameCaseClashBrsNameOverrideResolvesClashOk() {
        runTest("nameCaseClash/brsNameOverrideResolvesClashOk.kt")
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

    @Test
    fun testIoWorkerTopLevelAccessAccepted() {
        runTest("ioWorkerCapture/topLevelAccessAccepted.kt")
    }

    @Test
    fun testIoWorkerDifferentDispatcherAccepted() {
        runTest("ioWorkerCapture/differentDispatcherAccepted.kt")
    }

    @Test
    fun testIoWorkerSuppressedCapture() {
        runTest("ioWorkerCapture/suppressedCapture.kt")
    }

    @Test
    fun testIoWorkerNestedLambdaCaptureNotDetected() {
        runTest("ioWorkerCapture/nestedLambdaCaptureNotDetected.kt")
    }

    @Test
    fun testIoWorkerContextCompositionNotDetected() {
        runTest("ioWorkerCapture/contextCompositionNotDetected.kt")
    }

    @Test
    fun testIoWorkerRoArrayCaptureAccepted() {
        runTest("ioWorkerCapture/roArrayCaptureAccepted.kt")
    }

    // BRS_ONCHANGE_HANDLER_SIGNATURE — handler function must be zero-arg or single RoSGNodeEvent.

    @Test
    fun testBrsOnChangeHandlerSignatureZeroArgOk() {
        runTest("brsOnChangeHandlerSignature/zeroArgHandlerOk.kt")
    }

    @Test
    fun testBrsOnChangeHandlerSignatureOneArgRoSGNodeEventOk() {
        runTest("brsOnChangeHandlerSignature/oneArgRoSGNodeEventOk.kt")
    }

    @Test
    fun testBrsOnChangeHandlerSignatureOneArgNullableRoSGNodeEventOk() {
        runTest("brsOnChangeHandlerSignature/oneArgNullableRoSGNodeEventOk.kt")
    }

    @Test
    fun testBrsOnChangeHandlerSignatureOneArgWrongTypeRejected() {
        runTest("brsOnChangeHandlerSignature/oneArgWrongTypeRejected.kt")
    }

    @Test
    fun testBrsOnChangeHandlerSignatureOneArgStringRejected() {
        runTest("brsOnChangeHandlerSignature/oneArgStringRejected.kt")
    }

    @Test
    fun testBrsOnChangeHandlerSignatureTwoArgsRejected() {
        runTest("brsOnChangeHandlerSignature/twoArgsRejected.kt")
    }

    @Test
    fun testBrsOnChangeHandlerSignatureInheritedBadRejected() {
        runTest("brsOnChangeHandlerSignature/inheritedHandlerWithBadSignatureRejected.kt")
    }

    @Test
    fun testBrsOnChangeHandlerSignatureInheritedGoodOk() {
        runTest("brsOnChangeHandlerSignature/inheritedHandlerWithGoodSignatureOk.kt")
    }

    @Test
    fun testBrsOnChangeHandlerSignatureOverloadedOneGoodOk() {
        runTest("brsOnChangeHandlerSignature/overloadedHandlerWithOneGoodOk.kt")
    }

    @Test
    fun testBrsOnChangeHandlerSignatureSuppressedOk() {
        runTest("brsOnChangeHandlerSignature/suppressedBadSignature.kt")
    }

    @Test
    fun testBrsOnChangeHandlerSignatureNotFoundTakesPrecedence() {
        runTest("brsOnChangeHandlerSignature/notFoundTakesPrecedence.kt")
    }

    @Test
    fun testBrsOnChangeHandlerSignatureConventionPathSkipped() {
        runTest("brsOnChangeHandlerSignature/conventionPathSkipped.kt")
    }

    // BRS_ADDFIELD_INVALID_TYPE — addField() type argument validation

    @Test
    fun testAddFieldCapitalisedStringRejected() {
        runTest("addFieldInvalidType/capitalisedStringRejected.kt")
    }

    @Test
    fun testAddFieldUnknownTypeRejected() {
        runTest("addFieldInvalidType/unknownTypeRejected.kt")
    }

    @Test
    fun testAddFieldPositionalArgsOk() {
        runTest("addFieldInvalidType/positionalArgsOk.kt")
    }

    @Test
    fun testAddFieldReorderedNamedArgsOk() {
        runTest("addFieldInvalidType/reorderedNamedArgsOk.kt")
    }

    @Test
    fun testAddFieldNonLiteralTypeOk() {
        runTest("addFieldInvalidType/nonLiteralTypeOk.kt")
    }

    @Test
    fun testAddFieldConstValOk() {
        runTest("addFieldInvalidType/constValOk.kt")
    }

    @Test
    fun testAddFieldConstValMismatch() {
        runTest("addFieldInvalidType/constValMismatch.kt")
    }

    @Test
    fun testAddFieldSubclassReceiverRejected() {
        runTest("addFieldInvalidType/subclassReceiverRejected.kt")
    }

    @Test
    fun testAddFieldSuppressedInvalidType() {
        runTest("addFieldInvalidType/suppressedInvalidType.kt")
    }

    // BRS_CREATE_OBJECT_INVALID_TYPE — FIR-phase validation for kotlin.brs.createObject(type, ...) call site.
    // Literal-required-narrow: only fires when type is a compile-time constant string.
    // Case policy: exact canonical match ("roArray" accepted; "RoArray", "roarray", "ROARRAY" rejected).

    @Test
    fun testCreateObjectLiteralOk() {
        runTest("createObjectInvalidType/literalOk.kt")
    }

    @Test
    fun testCreateObjectNamedArgOk() {
        runTest("createObjectInvalidType/namedArgOk.kt")
    }

    @Test
    fun testCreateObjectPositionalArgsOk() {
        runTest("createObjectInvalidType/positionalArgsOk.kt")
    }

    @Test
    fun testCreateObjectNonLiteralTypeOk() {
        runTest("createObjectInvalidType/nonLiteralTypeOk.kt")
    }

    @Test
    fun testCreateObjectConstValOk() {
        runTest("createObjectInvalidType/constValOk.kt")
    }

    @Test
    fun testCreateObjectUnknownTypeRejected() {
        runTest("createObjectInvalidType/unknownTypeRejected.kt")
    }

    @Test
    fun testCreateObjectCapitalisedRejected() {
        runTest("createObjectInvalidType/capitalisedRejected.kt")
    }

    @Test
    fun testCreateObjectUppercasedRejected() {
        runTest("createObjectInvalidType/uppercasedRejected.kt")
    }

    @Test
    fun testCreateObjectLowercasedRejected() {
        runTest("createObjectInvalidType/lowercasedRejected.kt")
    }

    @Test
    fun testCreateObjectConstValMismatch() {
        runTest("createObjectInvalidType/constValMismatch.kt")
    }

    @Test
    fun testCreateObjectSuppressedInvalidType() {
        runTest("createObjectInvalidType/suppressedInvalidType.kt")
    }

    // BRS_STATIC_INVALID_TARGET / BRS_STATIC_OVERLOAD — @BrsStatic annotation validation.
    // Two checkers: FirBrsStaticOverloadFileChecker (top-level scope) +
    // FirBrsStaticClassChecker (object/companion/regular-class scope).
    // Annotation-target filtering is upstream (@Target(FUNCTION)); these checkers
    // only see function targets.

    @Test
    fun testBrsStaticTopLevelOk() {
        runTest("brsStaticValidation/topLevelOk.kt")
    }

    @Test
    fun testBrsStaticObjectMemberOk() {
        runTest("brsStaticValidation/objectMemberOk.kt")
    }

    @Test
    fun testBrsStaticCompanionMemberOk() {
        runTest("brsStaticValidation/companionMemberOk.kt")
    }

    @Test
    fun testBrsStaticDifferentScopesOk() {
        runTest("brsStaticValidation/differentScopesOk.kt")
    }

    @Test
    fun testBrsStaticNonStaticOverloadOk() {
        runTest("brsStaticValidation/nonStaticOverloadOk.kt")
    }

    @Test
    fun testBrsStaticMixedAnnotatedAndNotOk() {
        runTest("brsStaticValidation/mixedAnnotatedAndNotOk.kt")
    }

    @Test
    fun testBrsStaticRegularClassMemberRejected() {
        runTest("brsStaticValidation/regularClassMemberRejected.kt")
    }

    @Test
    fun testBrsStaticAbstractClassMemberRejected() {
        runTest("brsStaticValidation/abstractClassMemberRejected.kt")
    }

    @Test
    fun testBrsStaticInterfaceMemberRejected() {
        runTest("brsStaticValidation/interfaceMemberRejected.kt")
    }

    @Test
    fun testBrsStaticNestedRegularClassRejected() {
        runTest("brsStaticValidation/nestedRegularClassRejected.kt")
    }

    @Test
    fun testBrsStaticTopLevelOverloadRejected() {
        runTest("brsStaticValidation/topLevelOverloadRejected.kt")
    }

    @Test
    fun testBrsStaticObjectOverloadRejected() {
        runTest("brsStaticValidation/objectOverloadRejected.kt")
    }

    @Test
    fun testBrsStaticCompanionOverloadRejected() {
        runTest("brsStaticValidation/companionOverloadRejected.kt")
    }

    @Test
    fun testBrsStaticTripleOverloadRejected() {
        runTest("brsStaticValidation/tripleOverloadRejected.kt")
    }

    @Test
    fun testBrsStaticSuppressedInvalidTarget() {
        runTest("brsStaticValidation/suppressedInvalidTarget.kt")
    }

    @Test
    fun testBrsStaticSuppressedOverload() {
        runTest("brsStaticValidation/suppressedOverload.kt")
    }

    @Test
    fun testBrsStaticSuppressedOneOfOverload() {
        runTest("brsStaticValidation/suppressedOneOfOverload.kt")
    }
}
