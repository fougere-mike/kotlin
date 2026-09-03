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
import org.jetbrains.kotlin.diagnostics.Severity.WARNING
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
    val BRS_INTRINSIC_USER_DEFINED: KtDiagnosticFactory1<String> = KtDiagnosticFactory1("BRS_INTRINSIC_USER_DEFINED", ERROR, SourceElementPositioningStrategies.DECLARATION_NAME, KtElement::class, getRendererFactory())

    // Name case clashes
    val BRS_NAME_CASE_CLASH: KtDiagnosticFactory1<String> = KtDiagnosticFactory1("BRS_NAME_CASE_CLASH", ERROR, SourceElementPositioningStrategies.DECLARATION_NAME, KtElement::class, getRendererFactory())
    val BRS_INHERITED_NAME_CASE_CLASH: KtDiagnosticFactory2<String, String> = KtDiagnosticFactory2("BRS_INHERITED_NAME_CASE_CLASH", ERROR, SourceElementPositioningStrategies.DECLARATION_NAME, KtElement::class, getRendererFactory())

    // SceneGraph
    val BRS_SCENEGRAPH_FIELD_TYPE: KtDiagnosticFactory3<String, String, String> = KtDiagnosticFactory3("BRS_SCENEGRAPH_FIELD_TYPE", ERROR, SourceElementPositioningStrategies.DECLARATION_NAME, KtElement::class, getRendererFactory())
    val BRS_ONCHANGE_HANDLER_NOT_FOUND: KtDiagnosticFactory2<String, String> = KtDiagnosticFactory2("BRS_ONCHANGE_HANDLER_NOT_FOUND", ERROR, SourceElementPositioningStrategies.DECLARATION_NAME, KtElement::class, getRendererFactory())
    val BRS_SCENEGRAPH_FIELD_CONFLICT: KtDiagnosticFactory1<String> = KtDiagnosticFactory1("BRS_SCENEGRAPH_FIELD_CONFLICT", ERROR, SourceElementPositioningStrategies.DECLARATION_NAME, KtElement::class, getRendererFactory())
    val BRS_ONCHANGE_HANDLER_SIGNATURE: KtDiagnosticFactory3<String, String, String> = KtDiagnosticFactory3("BRS_ONCHANGE_HANDLER_SIGNATURE", ERROR, SourceElementPositioningStrategies.DECLARATION_NAME, KtElement::class, getRendererFactory())
    val BRS_ADDFIELD_INVALID_TYPE: KtDiagnosticFactory2<String, String> = KtDiagnosticFactory2("BRS_ADDFIELD_INVALID_TYPE", ERROR, SourceElementPositioningStrategies.DEFAULT, KtElement::class, getRendererFactory())
    val BRS_CREATE_OBJECT_INVALID_TYPE: KtDiagnosticFactory2<String, String> = KtDiagnosticFactory2("BRS_CREATE_OBJECT_INVALID_TYPE", ERROR, SourceElementPositioningStrategies.DEFAULT, KtElement::class, getRendererFactory())

    // IO worker extraction
    val BRS_IO_WORKER_NON_SERIALIZABLE_CAPTURE: KtDiagnosticFactory2<String, String> = KtDiagnosticFactory2("BRS_IO_WORKER_NON_SERIALIZABLE_CAPTURE", ERROR, SourceElementPositioningStrategies.DEFAULT, KtElement::class, getRendererFactory())
    val BRS_IO_DISPATCHER_UNSUPPORTED: KtDiagnosticFactory0 = KtDiagnosticFactory0("BRS_IO_DISPATCHER_UNSUPPORTED", ERROR, SourceElementPositioningStrategies.DEFAULT, KtElement::class, getRendererFactory())

    // @BrsStatic
    val BRS_STATIC_INVALID_TARGET: KtDiagnosticFactory1<String> = KtDiagnosticFactory1("BRS_STATIC_INVALID_TARGET", ERROR, SourceElementPositioningStrategies.DECLARATION_NAME, KtElement::class, getRendererFactory())
    val BRS_STATIC_OVERLOAD: KtDiagnosticFactory2<String, String> = KtDiagnosticFactory2("BRS_STATIC_OVERLOAD", ERROR, SourceElementPositioningStrategies.DECLARATION_NAME, KtElement::class, getRendererFactory())

    // brsName
    val BRS_BRSNAME_REQUIRES_CALLABLE_REF: KtDiagnosticFactory1<String> = KtDiagnosticFactory1("BRS_BRSNAME_REQUIRES_CALLABLE_REF", ERROR, SourceElementPositioningStrategies.DEFAULT, KtElement::class, getRendererFactory())
    val BRS_BRSNAME_INVALID_TARGET: KtDiagnosticFactory2<String, String> = KtDiagnosticFactory2("BRS_BRSNAME_INVALID_TARGET", ERROR, SourceElementPositioningStrategies.DEFAULT, KtElement::class, getRendererFactory())
    val BRS_BRSNAME_BLANK: KtDiagnosticFactory1<String> = KtDiagnosticFactory1("BRS_BRSNAME_BLANK", ERROR, SourceElementPositioningStrategies.DECLARATION_NAME, KtElement::class, getRendererFactory())

    // @BrsConstant
    val BRS_BRSCONSTANT_VAR: KtDiagnosticFactory1<String> = KtDiagnosticFactory1("BRS_BRSCONSTANT_VAR", ERROR, SourceElementPositioningStrategies.DECLARATION_NAME, KtElement::class, getRendererFactory())
    val BRS_BRSCONSTANT_NON_OBJECT: KtDiagnosticFactory1<String> = KtDiagnosticFactory1("BRS_BRSCONSTANT_NON_OBJECT", ERROR, SourceElementPositioningStrategies.DECLARATION_NAME, KtElement::class, getRendererFactory())

    // @BrsCreateObject
    val BRS_BRSCREATEOBJECT_INVALID_TYPE: KtDiagnosticFactory2<String, String> = KtDiagnosticFactory2("BRS_BRSCREATEOBJECT_INVALID_TYPE", ERROR, SourceElementPositioningStrategies.DECLARATION_NAME, KtElement::class, getRendererFactory())

    // try/finally
    val BRS_TRY_FINALLY_UNSUPPORTED: KtDiagnosticFactory0 = KtDiagnosticFactory0("BRS_TRY_FINALLY_UNSUPPORTED", ERROR, SourceElementPositioningStrategies.DEFAULT, KtElement::class, getRendererFactory())

    // ScopeHandle
    val BRS_SCOPE_BLOCK_NOT_LITERAL: KtDiagnosticFactory0 = KtDiagnosticFactory0("BRS_SCOPE_BLOCK_NOT_LITERAL", ERROR, SourceElementPositioningStrategies.DEFAULT, KtElement::class, getRendererFactory())
    val BRS_SCOPE_CAPTURE_UNMARSHALLABLE: KtDiagnosticFactory2<String, String> = KtDiagnosticFactory2("BRS_SCOPE_CAPTURE_UNMARSHALLABLE", ERROR, SourceElementPositioningStrategies.DEFAULT, KtElement::class, getRendererFactory())
    val BRS_SCOPE_CAPTURE_MUTATION_LOST: KtDiagnosticFactory1<String> = KtDiagnosticFactory1("BRS_SCOPE_CAPTURE_MUTATION_LOST", WARNING, SourceElementPositioningStrategies.DEFAULT, KtElement::class, getRendererFactory())
    val BRS_SCOPE_RESULT_NOT_DATA: KtDiagnosticFactory1<String> = KtDiagnosticFactory1("BRS_SCOPE_RESULT_NOT_DATA", WARNING, SourceElementPositioningStrategies.DEFAULT, KtElement::class, getRendererFactory())
    val BRS_SCOPE_ARG_NOT_MARSHALLABLE: KtDiagnosticFactory1<String> = KtDiagnosticFactory1("BRS_SCOPE_ARG_NOT_MARSHALLABLE", ERROR, SourceElementPositioningStrategies.DEFAULT, KtElement::class, getRendererFactory())

    // SharedService
    val BRS_SHARED_CLASS_NOT_FINAL: KtDiagnosticFactory1<String> = KtDiagnosticFactory1("BRS_SHARED_CLASS_NOT_FINAL", ERROR, SourceElementPositioningStrategies.DECLARATION_NAME, KtElement::class, getRendererFactory())
    val BRS_SHARED_FN_PROPERTY: KtDiagnosticFactory2<String, String> = KtDiagnosticFactory2("BRS_SHARED_FN_PROPERTY", ERROR, SourceElementPositioningStrategies.DECLARATION_NAME, KtElement::class, getRendererFactory())
    val BRS_SHARED_THROUGH_COPYING_CHANNEL: KtDiagnosticFactory2<String, String> = KtDiagnosticFactory2("BRS_SHARED_THROUGH_COPYING_CHANNEL", ERROR, SourceElementPositioningStrategies.DEFAULT, KtElement::class, getRendererFactory())

    // Flow task lift
    val BRS_FLOW_ON_INVALID_DISPATCHER: KtDiagnosticFactory0 = KtDiagnosticFactory0("BRS_FLOW_ON_INVALID_DISPATCHER", ERROR, SourceElementPositioningStrategies.DEFAULT, KtElement::class, getRendererFactory())
    val BRS_FLOW_UPSTREAM_NOT_LITERAL: KtDiagnosticFactory2<String, String> = KtDiagnosticFactory2("BRS_FLOW_UPSTREAM_NOT_LITERAL", ERROR, SourceElementPositioningStrategies.DEFAULT, KtElement::class, getRendererFactory())
    val BRS_TASK_CAPTURE_UNMARSHALLABLE: KtDiagnosticFactory2<String, String> = KtDiagnosticFactory2("BRS_TASK_CAPTURE_UNMARSHALLABLE", ERROR, SourceElementPositioningStrategies.DEFAULT, KtElement::class, getRendererFactory())
    val BRS_TASK_EMIT_NOT_MARSHALLABLE: KtDiagnosticFactory2<String, String> = KtDiagnosticFactory2("BRS_TASK_EMIT_NOT_MARSHALLABLE", ERROR, SourceElementPositioningStrategies.DEFAULT, KtElement::class, getRendererFactory())
    val BRS_TASK_SUSPEND_IN_LIFTED: KtDiagnosticFactory1<String> = KtDiagnosticFactory1("BRS_TASK_SUSPEND_IN_LIFTED", ERROR, SourceElementPositioningStrategies.DEFAULT, KtElement::class, getRendererFactory())
    val BRS_TASK_CAPTURE_MUTATION_LOST: KtDiagnosticFactory1<String> = KtDiagnosticFactory1("BRS_TASK_CAPTURE_MUTATION_LOST", WARNING, SourceElementPositioningStrategies.DEFAULT, KtElement::class, getRendererFactory())

    // Typed task components
    val BRS_TASK_STATE_NOT_FIELD: KtDiagnosticFactory2<String, String> = KtDiagnosticFactory2("BRS_TASK_STATE_NOT_FIELD", ERROR, SourceElementPositioningStrategies.DECLARATION_NAME, KtElement::class, getRendererFactory())
    val BRS_CREATE_COMPONENT_INVALID_TYPE: KtDiagnosticFactory3<String, String, String> = KtDiagnosticFactory3("BRS_CREATE_COMPONENT_INVALID_TYPE", ERROR, SourceElementPositioningStrategies.DEFAULT, KtElement::class, getRendererFactory())

    override fun getRendererFactory(): BaseDiagnosticRendererFactory = FirBrsErrorsDefaultMessages
}
