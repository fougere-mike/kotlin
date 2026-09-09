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

    @Test
    fun testNameCaseClashBrsNameOverrideCreatesClashRejected() {
        runTest("nameCaseClash/brsNameOverrideCreatesClashRejected.kt")
    }

    @Test
    fun testNameCaseClashBrsNameBlankOverrideUsesKotlinName() {
        runTest("nameCaseClash/brsNameBlankOverrideUsesKotlinName.kt")
    }

    @Test
    fun testNameCaseClashBrsNameOnClassMemberOk() {
        runTest("nameCaseClash/brsNameOnClassMemberOk.kt")
    }

    @Test
    fun testNameCaseClashCrossFileTopLevelRejected() {
        runTest("nameCaseClash/crossFileTopLevelClashRejected.kt")
    }

    @Test
    fun testNameCaseClashCrossFileBrsNameOverrideOk() {
        runTest("nameCaseClash/crossFileBrsNameOverrideOk.kt")
    }

    @Test
    fun testNameCaseClashCrossFileSamePackageDifferentLowercaseOk() {
        runTest("nameCaseClash/crossFileSamePackageDifferentLowercaseOk.kt")
    }

    @Test
    fun testNameCaseClashCrossFileDifferentPackageOk() {
        runTest("nameCaseClash/crossFileDifferentPackageOk.kt")
    }

    @Test
    fun testNameCaseClashCrossFileSuppressed() {
        runTest("nameCaseClash/crossFileSuppressedClash.kt")
    }

    // BRS_NAME_CASE_CLASH — Path B: own-declared member vs. inherited member case clash.

    @Test
    fun testNameCaseClashInheritedMemberClash() {
        runTest("nameCaseClash/inheritedMemberCaseClash.kt")
    }

    @Test
    fun testNameCaseClashInheritedFunctionClash() {
        runTest("nameCaseClash/inheritedFunctionCaseClash.kt")
    }

    @Test
    fun testNameCaseClashInheritedFromInterface() {
        runTest("nameCaseClash/inheritedFromInterface.kt")
    }

    @Test
    fun testNameCaseClashInheritedTransitive() {
        runTest("nameCaseClash/inheritedTransitive.kt")
    }

    @Test
    fun testNameCaseClashInheritedExactNameOverrideOk() {
        runTest("nameCaseClash/inheritedExactNameOverrideOk.kt")
    }

    @Test
    fun testNameCaseClashInheritedNoClashOk() {
        runTest("nameCaseClash/inheritedNoClashOk.kt")
    }

    @Test
    fun testNameCaseClashInheritedClashSuppressed() {
        runTest("nameCaseClash/inheritedClashSuppressed.kt")
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

    // BRS_IO_DISPATCHER_UNSUPPORTED — Dispatchers.IO silently aliases the render-thread
    // queue (TaskPool quarantined); background work goes through runTask<T>.

    @Test
    fun testIoDispatcherDirectReferenceRejected() {
        runTest("ioDispatcherUnsupported/directReferenceRejected.kt")
    }

    @Test
    fun testIoDispatcherWithContextIORejected() {
        runTest("ioDispatcherUnsupported/withContextIORejected.kt")
    }

    @Test
    fun testIoDispatcherSuppressedOk() {
        runTest("ioDispatcherUnsupported/suppressedOk.kt")
    }

    @Test
    fun testIoDispatcherOtherDispatchersOk() {
        runTest("ioDispatcherUnsupported/otherDispatchersOk.kt")
    }

    @Test
    fun testIoDispatcherAliasFlaggedAtSourceOnly() {
        runTest("ioDispatcherUnsupported/aliasFlaggedAtSourceOnly.kt")
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

    @Test
    fun testBrsStaticCrossFileTopLevelOverloadRejected() {
        runTest("brsStaticValidation/crossFileTopLevelOverloadRejected.kt")
    }

    @Test
    fun testBrsStaticCrossFileTripleOverloadRejected() {
        runTest("brsStaticValidation/crossFileTripleOverloadRejected.kt")
    }

    @Test
    fun testBrsStaticCrossFileMixedSameFileAndCrossFileRejected() {
        runTest("brsStaticValidation/crossFileMixedSameFileAndCrossFileRejected.kt")
    }

    @Test
    fun testBrsStaticCrossFileDifferentPackageOk() {
        runTest("brsStaticValidation/crossFileDifferentPackageOk.kt")
    }

    @Test
    fun testBrsStaticCrossFileOneAnnotatedOk() {
        runTest("brsStaticValidation/crossFileOneAnnotatedOk.kt")
    }

    @Test
    fun testBrsStaticCrossFileSuppressedOverload() {
        runTest("brsStaticValidation/crossFileSuppressedOverload.kt")
    }

    @Test
    fun testBrsStaticTopLevelExtensionRegularClassRejected() {
        runTest("brsStaticValidation/topLevelExtensionRegularClassRejected.kt")
    }

    @Test
    fun testBrsStaticTopLevelExtensionObjectRejected() {
        runTest("brsStaticValidation/topLevelExtensionObjectRejected.kt")
    }

    @Test
    fun testBrsStaticTopLevelExtensionInterfaceRejected() {
        runTest("brsStaticValidation/topLevelExtensionInterfaceRejected.kt")
    }

    @Test
    fun testBrsStaticTopLevelExtensionGenericRejected() {
        runTest("brsStaticValidation/topLevelExtensionGenericRejected.kt")
    }

    @Test
    fun testBrsStaticTopLevelExtensionNullableReceiverRejected() {
        runTest("brsStaticValidation/topLevelExtensionNullableReceiverRejected.kt")
    }

    @Test
    fun testBrsStaticTopLevelExtensionSuppressed() {
        runTest("brsStaticValidation/topLevelExtensionSuppressed.kt")
    }

    // BRS_BRSNAME_REQUIRES_CALLABLE_REF — brsName() requires a syntactic ::ref argument.
    // Passes for direct, qualified, bound, and constructor references.
    // Fires for lambdas, stored refs, function call results, and function-typed parameters.

    @Test
    fun testBrsNameTopLevelRefOk() {
        runTest("brsNameRequiresCallableRef/topLevelRefOk.kt")
    }

    @Test
    fun testBrsNameQualifiedRefOk() {
        runTest("brsNameRequiresCallableRef/qualifiedRefOk.kt")
    }

    @Test
    fun testBrsNameBoundRefOk() {
        runTest("brsNameRequiresCallableRef/boundRefOk.kt")
    }

    @Test
    fun testBrsNameConstructorRefOk() {
        runTest("brsNameRequiresCallableRef/constructorRefOk.kt")
    }

    @Test
    fun testBrsNameLambdaRejected() {
        runTest("brsNameRequiresCallableRef/lambdaRejected.kt")
    }

    @Test
    fun testBrsNameStoredRefRejected() {
        runTest("brsNameRequiresCallableRef/storedRefRejected.kt")
    }

    @Test
    fun testBrsNameFunctionCallResultRejected() {
        runTest("brsNameRequiresCallableRef/functionCallResultRejected.kt")
    }

    @Test
    fun testBrsNameParameterRefRejected() {
        runTest("brsNameRequiresCallableRef/parameterRefRejected.kt")
    }

    @Test
    fun testBrsNameSuppressedNonRef() {
        runTest("brsNameRequiresCallableRef/suppressedNonRef.kt")
    }

    // BRS_BRSNAME_INVALID_TARGET — brsName()'s target ::ref must point at a declaration
    // that emits a stable module-level BRS function. Local functions get hoisted with
    // synthesized names after BrsIntrinsicLowering captures the pre-hoist name; abstract
    // members emit no module-level function at all. Concrete instance methods of regular
    // (or abstract) classes, top-level functions, object/companion-object members, and
    // constructors all remain valid.

    // BRS_BRSNAME_BLANK — @BrsName("") and @BrsName("   ") are rejected at FIR phase.
    // Blank argument is never meaningful: FIR falls back to Kotlin name (silent no-op)
    // and IR emits an empty identifier. Checker covers all declaration targets.

    @Test
    fun testBrsBrsNameBlankOnFunctionRejected() {
        runTest("brsBrsNameBlank/blankOnFunctionRejected.kt")
    }

    @Test
    fun testBrsBrsNameBlankOnClassRejected() {
        runTest("brsBrsNameBlank/blankOnClassRejected.kt")
    }

    @Test
    fun testBrsBrsNameBlankOnPropertyRejected() {
        runTest("brsBrsNameBlank/blankOnPropertyRejected.kt")
    }

    @Test
    fun testBrsBrsNameBlankWhitespaceOnlyRejected() {
        runTest("brsBrsNameBlank/whitespaceOnlyRejected.kt")
    }

    @Test
    fun testBrsBrsNameBlankNonBlankOk() {
        runTest("brsBrsNameBlank/nonBlankOk.kt")
    }

    @Test
    fun testBrsBrsNameBlankSuppressed() {
        runTest("brsBrsNameBlank/blankSuppressed.kt")
    }

    @Test
    fun testBrsNameLocalFunctionRejected() {
        runTest("brsNameInvalidTarget/localFunctionRejected.kt")
    }

    @Test
    fun testBrsNameInterfaceMemberRejected() {
        runTest("brsNameInvalidTarget/interfaceMemberRejected.kt")
    }

    @Test
    fun testBrsNameAbstractMemberRejected() {
        runTest("brsNameInvalidTarget/abstractMemberRejected.kt")
    }

    @Test
    fun testBrsNameConcreteMethodOnAbstractClassOk() {
        runTest("brsNameInvalidTarget/concreteMethodOnAbstractClassOk.kt")
    }

    @Test
    fun testBrsNameInterfaceDefaultMethodOk() {
        runTest("brsNameInvalidTarget/interfaceDefaultMethodOk.kt")
    }

    @Test
    fun testBrsNameInvalidTargetTopLevelStillOk() {
        runTest("brsNameInvalidTarget/topLevelStillOk.kt")
    }

    @Test
    fun testBrsNameInvalidTargetSuppressedLocalFunction() {
        runTest("brsNameInvalidTarget/suppressedLocalFunction.kt")
    }

    // BRS_INTRINSIC_USER_DEFINED — @BrsIntrinsic is reserved for stdlib; user-defined functions
    // (both external and non-external) carrying @BrsIntrinsic are rejected. Origin-based filter
    // ensures stdlib's own ~23 @BrsIntrinsic sites are not affected at user-compile time.

    @Test
    fun testBrsIntrinsicUserDefinedExternalRejected() {
        runTest("brsIntrinsicUserDefined/userDefinedExternalRejected.kt")
    }

    @Test
    fun testBrsIntrinsicUserDefinedNonExternalRejected() {
        runTest("brsIntrinsicUserDefined/userDefinedNonExternalRejected.kt")
    }

    @Test
    fun testBrsIntrinsicUserDefinedCustomNameRejected() {
        runTest("brsIntrinsicUserDefined/userDefinedCustomNameRejected.kt")
    }

    @Test
    fun testBrsIntrinsicUserDefinedMultipleAllRejected() {
        runTest("brsIntrinsicUserDefined/multipleUserFunctionsAllRejected.kt")
    }

    @Test
    fun testBrsIntrinsicUserDefinedWithoutAnnotationOk() {
        runTest("brsIntrinsicUserDefined/userFunctionWithoutAnnotationOk.kt")
    }

    @Test
    fun testBrsIntrinsicUserDefinedSuppressed() {
        runTest("brsIntrinsicUserDefined/suppressedUserDefined.kt")
    }

    // BRS_BRSCONSTANT_VAR — `var` properties inside @BrsConstant objects are inlined
    // at compile time and cannot be mutable.

    @Test
    fun testBrsConstantVarPropertyRejected() {
        runTest("brsConstantVar/varPropertyRejected.kt")
    }

    @Test
    fun testBrsConstantMultipleVarsAllRejected() {
        runTest("brsConstantVar/multipleVarsAllRejected.kt")
    }

    @Test
    fun testBrsConstantMixedValVar() {
        runTest("brsConstantVar/mixedValVar.kt")
    }

    @Test
    fun testBrsConstantValOnlyOk() {
        runTest("brsConstantVar/valOnlyOk.kt")
    }

    @Test
    fun testBrsConstantNonAnnotatedObjectOk() {
        runTest("brsConstantVar/nonAnnotatedObjectOk.kt")
    }

    @Test
    fun testBrsConstantSuppressedVarProperty() {
        runTest("brsConstantVar/suppressedVarProperty.kt")
    }

    // BRS_BRSCONSTANT_NON_OBJECT — @BrsConstant on any class kind other than `object`
    // is silently ignored at IR lowering. The FIR diagnostic surfaces the misuse with a
    // stable, suppressible ID.

    @Test
    fun testBrsConstantNonObjectRegularClassRejected() {
        runTest("brsConstantNonObject/regularClassRejected.kt")
    }

    @Test
    fun testBrsConstantNonObjectAbstractClassRejected() {
        runTest("brsConstantNonObject/abstractClassRejected.kt")
    }

    @Test
    fun testBrsConstantNonObjectInterfaceRejected() {
        runTest("brsConstantNonObject/interfaceRejected.kt")
    }

    @Test
    fun testBrsConstantNonObjectEnumClassRejected() {
        runTest("brsConstantNonObject/enumClassRejected.kt")
    }

    @Test
    fun testBrsConstantNonObjectAnnotationClassRejected() {
        runTest("brsConstantNonObject/annotationClassRejected.kt")
    }

    @Test
    fun testBrsConstantNonObjectSealedClassRejected() {
        runTest("brsConstantNonObject/sealedClassRejected.kt")
    }

    @Test
    fun testBrsConstantNonObjectObjectStillOk() {
        runTest("brsConstantNonObject/objectStillOk.kt")
    }

    @Test
    fun testBrsConstantNonObjectNonAnnotatedClassOk() {
        runTest("brsConstantNonObject/nonAnnotatedClassOk.kt")
    }

    @Test
    fun testBrsConstantNonObjectSuppressed() {
        runTest("brsConstantNonObject/suppressedNonObjectClass.kt")
    }

    // BRS_INHERITED_NAME_CASE_CLASH — Path C: two inherited members (from base classes or interfaces)
    // whose effective BRS names clash in case, and the user class declares neither.
    // Reported on the user class source.

    @Test
    fun testInheritedNameCaseClashTwoBasesCaseClash() {
        runTest("inheritedNameCaseClash/twoBasesCaseClash.kt")
    }

    @Test
    fun testInheritedNameCaseClashTransitive() {
        runTest("inheritedNameCaseClash/transitiveInheritedClash.kt")
    }

    @Test
    fun testInheritedNameCaseClashUserOverrideResolvesOk() {
        runTest("inheritedNameCaseClash/userOverrideResolvesAmbiguityOk.kt")
    }

    @Test
    fun testInheritedNameCaseClashClassLevelSuppressed() {
        runTest("inheritedNameCaseClash/inheritedClashClassLevelSuppressed.kt")
    }

    // BRS_NAME_CASE_CLASH — Path D: local function declarations inside a function body whose
    // effective BrightScript names differ only in case. BrightScript is case-insensitive, so
    // two locals like `fun innerOne()` and `fun INNERONE()` silently collide at runtime.

    @Test
    fun testNameCaseClashLocalFunctionRejected() {
        runTest("nameCaseClash/localFunctionCaseClashRejected.kt")
    }

    @Test
    fun testNameCaseClashLocalFunctionNoClashOk() {
        runTest("nameCaseClash/localFunctionNoClashOk.kt")
    }

    @Test
    fun testNameCaseClashLocalFunctionNestedIsolation() {
        runTest("nameCaseClash/localFunctionNestedIsolation.kt")
    }

    @Test
    fun testNameCaseClashLocalPropertyAndFunctionClashRejected() {
        runTest("nameCaseClash/localPropertyAndFunctionClashRejected.kt")
    }

    @Test
    fun testNameCaseClashLocalFunctionSuppressed() {
        runTest("nameCaseClash/localFunctionSuppressed.kt")
    }

    // R33: a generated @SGComponentBuilder builder (`fun LayoutBuilder.badge`) beside its
    // component class (`class Badge`) is exempt in both directions; the un-annotated twin
    // in the same fixture still clashes (narrowness guard).

    @Test
    fun testNameCaseClashSGComponentBuilderBesideComponentClassOk() {
        runTest("nameCaseClash/sgComponentBuilderBesideComponentClassOk.kt")
    }

    // BRS_BRSCREATEOBJECT_INVALID_TYPE — @BrsCreateObject(typeName) annotation argument validation.
    // Annotation-site twin of BRS_CREATE_OBJECT_INVALID_TYPE; same 24-element valid-type set.

    @Test
    fun testBrsCreateObjectAnnotationCanonicalLowerOk() {
        runTest("brsBrsCreateObjectInvalidType/canonicalLowerOk.kt")
    }

    @Test
    fun testBrsCreateObjectAnnotationUnknownTypeRejected() {
        runTest("brsBrsCreateObjectInvalidType/unknownTypeRejected.kt")
    }

    @Test
    fun testBrsCreateObjectAnnotationCapitalisedRejected() {
        runTest("brsBrsCreateObjectInvalidType/capitalisedRejected.kt")
    }

    @Test
    fun testBrsCreateObjectAnnotationUppercasedRejected() {
        runTest("brsBrsCreateObjectInvalidType/uppercasedRejected.kt")
    }

    @Test
    fun testBrsCreateObjectAnnotationLowercasedRejected() {
        runTest("brsBrsCreateObjectInvalidType/lowercasedRejected.kt")
    }

    @Test
    fun testBrsCreateObjectAnnotationEmptyStringRejected() {
        runTest("brsBrsCreateObjectInvalidType/emptyStringRejected.kt")
    }

    @Test
    fun testBrsCreateObjectAnnotationWhitespaceOnlyRejected() {
        runTest("brsBrsCreateObjectInvalidType/whitespaceOnlyRejected.kt")
    }

    @Test
    fun testBrsCreateObjectAnnotationSuppressed() {
        runTest("brsBrsCreateObjectInvalidType/suppressedInvalidType.kt")
    }

    // BRS_TASK_STATE_NOT_FIELD — backing-field properties declared in concrete TaskComponent-derived
    // classes must carry @SG*Field/@BrsField. Un-annotated state compiles to plain m-state: the task
    // thread gets a clone-in copy (reads of init-set values work) but writes from run() are silently
    // lost (device-proven: spikes/task-node-spike FINDINGS m_writeback_isolation). v1 flags val too.

    @Test
    fun testTaskStateNotFieldPlainVarRejected() {
        runTest("taskStateNotField/plainVarRejected.kt")
    }

    @Test
    fun testTaskStateNotFieldPlainValRejected() {
        runTest("taskStateNotField/plainValRejected.kt")
    }

    @Test
    fun testTaskStateNotFieldAnnotatedFieldOk() {
        runTest("taskStateNotField/annotatedFieldOk.kt")
    }

    @Test
    fun testTaskStateNotFieldBrsFieldAnnotatedOk() {
        runTest("taskStateNotField/brsFieldAnnotatedOk.kt")
    }

    @Test
    fun testTaskStateNotFieldLocalInRunOk() {
        runTest("taskStateNotField/localInRunOk.kt")
    }

    @Test
    fun testTaskStateNotFieldGetterOnlyOk() {
        runTest("taskStateNotField/getterOnlyOk.kt")
    }

    @Test
    fun testTaskStateNotFieldAbstractBaseNotFlagged() {
        runTest("taskStateNotField/abstractBaseNotFlagged.kt")
    }

    @Test
    fun testTaskStateNotFieldInheritedPropertyNotFlagged() {
        runTest("taskStateNotField/inheritedPropertyNotFlagged.kt")
    }

    @Test
    fun testTaskStateNotFieldNonTaskComponentOk() {
        runTest("taskStateNotField/nonTaskComponentOk.kt")
    }

    @Test
    fun testTaskStateNotFieldSuppressed() {
        runTest("taskStateNotField/suppressedPlainVar.kt")
    }

    // BRS_CREATE_COMPONENT_INVALID_TYPE — createComponent<T>()'s reified type argument must be a
    // concrete component class (@BrsComponent on itself or a @BrsSceneGraphComponent ancestor).
    // Type-parameter arguments are skipped (checked at the outer reified call site).

    @Test
    fun testCreateComponentConcreteTaskOk() {
        runTest("createComponentInvalidType/concreteTaskOk.kt")
    }

    @Test
    fun testCreateComponentConcreteGroupOk() {
        runTest("createComponentInvalidType/concreteGroupOk.kt")
    }

    @Test
    fun testCreateComponentAnnotatedComponentOk() {
        runTest("createComponentInvalidType/annotatedComponentOk.kt")
    }

    @Test
    fun testCreateComponentAbstractRejected() {
        runTest("createComponentInvalidType/abstractRejected.kt")
    }

    @Test
    fun testCreateComponentBaseClassRejected() {
        runTest("createComponentInvalidType/baseClassRejected.kt")
    }

    @Test
    fun testCreateComponentNonComponentRejected() {
        runTest("createComponentInvalidType/nonComponentRejected.kt")
    }

    @Test
    fun testCreateComponentInterfaceRejected() {
        runTest("createComponentInvalidType/interfaceRejected.kt")
    }

    @Test
    fun testCreateComponentInferredAbstractRejected() {
        runTest("createComponentInvalidType/inferredAbstractRejected.kt")
    }

    @Test
    fun testCreateComponentTypeParameterSkippedOk() {
        runTest("createComponentInvalidType/typeParameterSkippedOk.kt")
    }

    @Test
    fun testCreateComponentSuppressedAbstract() {
        runTest("createComponentInvalidType/suppressedAbstract.kt")
    }

    @Test
    fun testCreateComponentRunTaskAbstractRejected() {
        runTest("createComponentInvalidType/runTaskAbstractRejected.kt")
    }

    // BRS_TRY_FINALLY_UNSUPPORTED — non-suspend visitTry silently drops the finally block
    // at BRS codegen. Stopgap FIR error on any try/finally whose nearest containing callable
    // is not suspend; suspend bodies (state-machine path) stay clean.

    @Test
    fun testTryFinallyNonSuspend() {
        runTest("tryFinallyNonSuspend.kt")
    }

    // BRS_SCOPE_BLOCK_NOT_LITERAL — ScopeHandle.run(block) requires a literal lambda at
    // the call site (the compiler lifts it into a named request). Stored function values
    // and function references cannot be lowered and would hit the runtime backstop ISE.

    @Test
    fun testScopeBlockNotLiteral() {
        runTest("scopeBlockNotLiteral.kt")
    }

    // The capture/result/arg family (ScopeHandle A.3): run{} captures and results cross
    // the component boundary BY COPY as plain data. BRS_SCOPE_CAPTURE_UNMARSHALLABLE
    // (error) rejects captures outside the marshallable set (incl. the component-`this`
    // hole); BRS_SCOPE_CAPTURE_MUTATION_LOST (warning) flags writes to captured vars;
    // BRS_SCOPE_RESULT_NOT_DATA (warning — severity verified via the
    // dataClassPropertyRead golden: property reads are direct member reads, so husk
    // results are usable as data) covers block results and declaration-site R;
    // BRS_SCOPE_ARG_NOT_MARSHALLABLE (error) covers A1/A2 at ScopeRequest1/2
    // declaration sites.

    @Test
    fun testScopeCaptureUnmarshallable() {
        runTest("scopeCaptureUnmarshallable.kt")
    }

    @Test
    fun testScopeCaptureMutationLost() {
        runTest("scopeCaptureMutationLost.kt")
    }

    @Test
    fun testScopeResultNotData() {
        runTest("scopeResultNotData.kt")
    }

    @Test
    fun testScopeArgNotMarshallable() {
        runTest("scopeArgNotMarshallable.kt")
    }

    // The SharedService family (spec §5): BRS_SHARED_CLASS_NOT_FINAL (error — open concrete
    // SharedService descendants break Phase 3's dispatcher enumeration of concrete leaves;
    // abstract bases with members/overrides/hooks are LEGAL), BRS_SHARED_FN_PROPERTY (error —
    // a stored callback is a function reference in the shared bag, the one shape static
    // dispatch cannot rescue), BRS_SHARED_THROUGH_COPYING_CHANNEL (error — shared instances
    // cross by reference only; setField/callFunc args and @SG*Field task-component
    // declarations copy, and copies are husks).

    @Test
    fun testSharedClassRules() {
        runTest("sharedClassRules.kt")
    }

    @Test
    fun testSharedCopyChannel() {
        runTest("sharedCopyChannel.kt")
    }

    // BRS_FLOW_ON_INVALID_DISPATCHER — Dispatchers.Task is a compile-time token
    // selecting the task lift (flow-program spec decisions 4/10): flowOn requires
    // the LITERAL token at the call site (the lift is a compile-time lowering, so
    // a runtime-chosen dispatcher cannot select it), and the token is legal
    // nowhere else (it is not a runtime dispatcher). One diagnostic, two
    // directions; suppressible escapes hit the TaskTokenDispatcher / flowOn-stub
    // guided-throw runtime backstops.

    @Test
    fun testFlowTaskDispatcher() {
        runTest("flowTaskDispatcher.kt")
    }

    // The lifted-region family (flow-program spec §5/§7): the task lift moves the
    // flowOn(Dispatchers.Task) upstream chain / the spawnTask block onto a Roku
    // Task thread as a per-call-site synthesized component.
    // BRS_FLOW_UPSTREAM_NOT_LITERAL (error) — only code literally visible at the
    // call site can be lifted: the flowOn receiver must be a literal flow chain
    // (whitelisted builder/operator hops, literal lambdas, flow/flowOf/asFlow
    // bottoms), the spawnTask argument a literal lambda.
    // BRS_TASK_CAPTURE_UNMARSHALLABLE (error) — region captures cross by copy;
    // class instances (incl. implicit `this` from an instance-property read),
    // function values, and other unmarshallables are rejected; the message names
    // the hoist-to-local fix. BRS_TASK_CAPTURE_MUTATION_LOST (warning) — writes
    // to captured vars land on the task-side copy.
    // BRS_TASK_EMIT_NOT_MARSHALLABLE (error) — emit/emitAll argument types in the
    // region and the spawnTask result type must be marshallable (the envelope hop
    // copies). BRS_TASK_SUSPEND_IN_LIFTED (error) — the lifted world is
    // synchronous; only emit/emitAll suspend (the task thread has no pump).
    // Disclosed holes stay disclosed (Dynamic-erased values, suspend fn
    // references into opaque callees, generic T emissions) — the fixtures pin
    // them as CLEAN.

    @Test
    fun testFlowLiftLiteral() {
        runTest("flowLiftLiteral.kt")
    }

    @Test
    fun testFlowLiftCaptures() {
        runTest("flowLiftCaptures.kt")
    }

    @Test
    fun testFlowLiftEmitTypes() {
        runTest("flowLiftEmitTypes.kt")
    }

    @Test
    fun testFlowLiftSuspend() {
        runTest("flowLiftSuspend.kt")
    }

    // Component constructor inputs (spec 2026-09-04-component-lifecycle §7, plan B Task 5).
    // BRS_COMPONENT_INPUT_READ_IN_INIT (error) — a direct read of a constructor-parameter
    // @SG property inside init {} or a sibling property initializer sees the declared
    // default (init() runs inside CreateObject before any field write). Reads in
    // lambdas, onStart, and methods are deferred/legal. BRS_CREATE_COMPONENT_HAS_INPUTS
    // (error) — createComponent<T>() on an input-bearing T; construct it with T(...)
    // instead (runTask<T> keeps its configure lambda and is exempt).
    // BRS_TASK_CONSTRUCTOR_INPUT (error) — an @SG constructor parameter on a
    // TaskComponent; task inputs go through runTask<T> { field = value }. The
    // fake-source skip in FirBrsTaskStateNotFieldChecker is gone: a PLAIN constructor
    // val on a task now fires BRS_TASK_STATE_NOT_FIELD.

    @Test
    fun testComponentInputsInitBlockRead() {
        runTest("componentInputs/initBlockRead.kt")
    }

    @Test
    fun testComponentInputsSiblingInitializerRead() {
        runTest("componentInputs/siblingInitializerRead.kt")
    }

    @Test
    fun testComponentInputsLambdaReadOk() {
        runTest("componentInputs/lambdaReadOk.kt")
    }

    @Test
    fun testComponentInputsOnStartReadOk() {
        runTest("componentInputs/onStartReadOk.kt")
    }

    @Test
    fun testComponentInputsMethodReadOk() {
        runTest("componentInputs/methodReadOk.kt")
    }

    @Test
    fun testComponentInputsInitReadSuppressed() {
        runTest("componentInputs/initReadSuppressed.kt")
    }

    @Test
    fun testComponentInputsInitBlockThisRead() {
        runTest("componentInputs/initBlockThisRead.kt")
    }

    @Test
    fun testComponentInputsSuperDelegateForwardOk() {
        runTest("componentInputs/superDelegateForwardOk.kt")
    }

    @Test
    fun testComponentInputsCreateComponentHasInputs() {
        runTest("componentInputs/createComponentHasInputs.kt")
    }

    @Test
    fun testComponentInputsCreateComponentHasInputsSuppressed() {
        runTest("componentInputs/createComponentHasInputsSuppressed.kt")
    }

    @Test
    fun testComponentInputsCreateComponentHasInputsAbstract() {
        runTest("componentInputs/createComponentHasInputsAbstract.kt")
    }

    @Test
    fun testComponentInputsTaskConstructorInput() {
        runTest("componentInputs/taskConstructorInput.kt")
    }

    @Test
    fun testComponentInputsTaskConstructorInputSuppressed() {
        runTest("componentInputs/taskConstructorInputSuppressed.kt")
    }

    @Test
    fun testComponentInputsTaskPlainConstructorVal() {
        runTest("componentInputs/taskPlainConstructorVal.kt")
    }
}
