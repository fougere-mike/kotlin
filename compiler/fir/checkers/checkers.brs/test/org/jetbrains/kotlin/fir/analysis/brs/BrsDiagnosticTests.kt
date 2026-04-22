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
}
