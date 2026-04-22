/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.diagnostics.brs

import org.jetbrains.kotlin.diagnostics.*
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactory0
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactory1
import org.jetbrains.kotlin.diagnostics.KtDiagnosticsContainer
import org.jetbrains.kotlin.diagnostics.Severity.ERROR
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.fir.analysis.diagnostics.*
import org.jetbrains.kotlin.psi.KtElement

/**
 * Generated from: [org.jetbrains.kotlin.fir.checkers.generator.diagnostics.BRS_DIAGNOSTICS_LIST]
 */
@Suppress("IncorrectFormatting")
object FirBrsErrors : KtDiagnosticsContainer() {
    // Intrinsics
    val BRS_INTRINSIC_LITERAL_REQUIRED: KtDiagnosticFactory0 = KtDiagnosticFactory0("BRS_INTRINSIC_LITERAL_REQUIRED", ERROR, SourceElementPositioningStrategies.DEFAULT, KtElement::class, getRendererFactory())

    // Name case clashes
    val BRS_NAME_CASE_CLASH: KtDiagnosticFactory1<String> = KtDiagnosticFactory1("BRS_NAME_CASE_CLASH", ERROR, SourceElementPositioningStrategies.DECLARATION_NAME, KtElement::class, getRendererFactory())

    override fun getRendererFactory(): BaseDiagnosticRendererFactory = FirBrsErrorsDefaultMessages
}
