/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.diagnostics.brs

import org.jetbrains.kotlin.diagnostics.*
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactory0
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactory1
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactory2
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactory3
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

    // SceneGraph
    val BRS_SCENEGRAPH_FIELD_TYPE: KtDiagnosticFactory3<String, String, String> = KtDiagnosticFactory3("BRS_SCENEGRAPH_FIELD_TYPE", ERROR, SourceElementPositioningStrategies.DECLARATION_NAME, KtElement::class, getRendererFactory())
    val BRS_ONCHANGE_HANDLER_NOT_FOUND: KtDiagnosticFactory2<String, String> = KtDiagnosticFactory2("BRS_ONCHANGE_HANDLER_NOT_FOUND", ERROR, SourceElementPositioningStrategies.DECLARATION_NAME, KtElement::class, getRendererFactory())
    val BRS_SCENEGRAPH_FIELD_CONFLICT: KtDiagnosticFactory1<String> = KtDiagnosticFactory1("BRS_SCENEGRAPH_FIELD_CONFLICT", ERROR, SourceElementPositioningStrategies.DECLARATION_NAME, KtElement::class, getRendererFactory())
    val BRS_ONCHANGE_HANDLER_SIGNATURE: KtDiagnosticFactory3<String, String, String> = KtDiagnosticFactory3("BRS_ONCHANGE_HANDLER_SIGNATURE", ERROR, SourceElementPositioningStrategies.DECLARATION_NAME, KtElement::class, getRendererFactory())
    val BRS_ADDFIELD_INVALID_TYPE: KtDiagnosticFactory2<String, String> = KtDiagnosticFactory2("BRS_ADDFIELD_INVALID_TYPE", ERROR, SourceElementPositioningStrategies.DEFAULT, KtElement::class, getRendererFactory())
    val BRS_CREATE_OBJECT_INVALID_TYPE: KtDiagnosticFactory2<String, String> = KtDiagnosticFactory2("BRS_CREATE_OBJECT_INVALID_TYPE", ERROR, SourceElementPositioningStrategies.DEFAULT, KtElement::class, getRendererFactory())

    // IO worker extraction
    val BRS_IO_WORKER_NON_SERIALIZABLE_CAPTURE: KtDiagnosticFactory2<String, String> = KtDiagnosticFactory2("BRS_IO_WORKER_NON_SERIALIZABLE_CAPTURE", ERROR, SourceElementPositioningStrategies.DEFAULT, KtElement::class, getRendererFactory())

    override fun getRendererFactory(): BaseDiagnosticRendererFactory = FirBrsErrorsDefaultMessages
}
