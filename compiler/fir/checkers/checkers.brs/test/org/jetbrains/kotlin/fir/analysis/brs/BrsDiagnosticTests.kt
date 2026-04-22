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
}
