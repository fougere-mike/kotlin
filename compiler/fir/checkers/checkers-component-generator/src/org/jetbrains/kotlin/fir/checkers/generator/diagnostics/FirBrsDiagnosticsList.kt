/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.checkers.generator.diagnostics

import org.jetbrains.kotlin.fir.checkers.generator.diagnostics.model.DiagnosticList
import org.jetbrains.kotlin.fir.checkers.generator.diagnostics.model.PositioningStrategy
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.util.PrivateForInline

@Suppress("ClassName", "unused")
@OptIn(PrivateForInline::class)
object BRS_DIAGNOSTICS_LIST : DiagnosticList("FirBrsErrors") {
    val INTRINSICS by object : DiagnosticGroup("Intrinsics") {
        val BRS_INTRINSIC_LITERAL_REQUIRED by error<KtElement>()
    }

    val NAME_CASE_CLASH by object : DiagnosticGroup("Name case clashes") {
        val BRS_NAME_CASE_CLASH by error<KtElement>(PositioningStrategy.DECLARATION_NAME) {
            parameter<String>("peers")
            isSuppressible = true
        }
    }

    val SCENEGRAPH by object : DiagnosticGroup("SceneGraph") {
        val BRS_SCENEGRAPH_FIELD_TYPE by error<KtElement>(PositioningStrategy.DECLARATION_NAME) {
            parameter<String>("annotation")
            parameter<String>("expectedType")
            parameter<String>("actualType")
            isSuppressible = true
        }
        val BRS_ONCHANGE_HANDLER_NOT_FOUND by error<KtElement>(PositioningStrategy.DECLARATION_NAME) {
            parameter<String>("handlerName")
            parameter<String>("className")
            isSuppressible = true
        }
        val BRS_SCENEGRAPH_FIELD_CONFLICT by error<KtElement>(PositioningStrategy.DECLARATION_NAME) {
            parameter<String>("annotations")
            isSuppressible = true
        }
    }

    val IO_WORKER by object : DiagnosticGroup("IO worker extraction") {
        val BRS_IO_WORKER_NON_SERIALIZABLE_CAPTURE by error<KtElement> {
            parameter<String>("variableName")
            parameter<String>("variableType")
            isSuppressible = true
        }
    }
}
