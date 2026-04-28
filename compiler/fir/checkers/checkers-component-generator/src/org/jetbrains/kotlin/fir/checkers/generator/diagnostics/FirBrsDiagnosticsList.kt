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
        val BRS_INTRINSIC_USER_DEFINED by error<KtElement>(PositioningStrategy.DECLARATION_NAME) {
            parameter<String>("functionName")
            isSuppressible = true
        }
    }

    val NAME_CASE_CLASH by object : DiagnosticGroup("Name case clashes") {
        val BRS_NAME_CASE_CLASH by error<KtElement>(PositioningStrategy.DECLARATION_NAME) {
            parameter<String>("peers")
            isSuppressible = true
        }
        val BRS_INHERITED_NAME_CASE_CLASH by error<KtElement>(PositioningStrategy.DECLARATION_NAME) {
            parameter<String>("firstName")
            parameter<String>("secondName")
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
        val BRS_ONCHANGE_HANDLER_SIGNATURE by error<KtElement>(PositioningStrategy.DECLARATION_NAME) {
            parameter<String>("handlerName")
            parameter<String>("className")
            parameter<String>("actualSignature")
            isSuppressible = true
        }
        val BRS_ADDFIELD_INVALID_TYPE by error<KtElement> {
            parameter<String>("actualType")
            parameter<String>("validTypes")
            isSuppressible = true
        }
        val BRS_CREATE_OBJECT_INVALID_TYPE by error<KtElement> {
            parameter<String>("actualType")
            parameter<String>("validTypes")
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

    val STATIC by object : DiagnosticGroup("@BrsStatic") {
        val BRS_STATIC_INVALID_TARGET by error<KtElement>(PositioningStrategy.DECLARATION_NAME) {
            parameter<String>("className")
            isSuppressible = true
        }
        val BRS_STATIC_OVERLOAD by error<KtElement>(PositioningStrategy.DECLARATION_NAME) {
            parameter<String>("name")
            parameter<String>("count")
            isSuppressible = true
        }
    }

    val BRSNAME by object : DiagnosticGroup("brsName") {
        val BRS_BRSNAME_REQUIRES_CALLABLE_REF by error<KtElement> {
            parameter<String>("actualKind")
            isSuppressible = true
        }
        val BRS_BRSNAME_INVALID_TARGET by error<KtElement> {
            parameter<String>("targetKind")
            parameter<String>("targetName")
            isSuppressible = true
        }
    }

    val BRSCONSTANT by object : DiagnosticGroup("@BrsConstant") {
        val BRS_BRSCONSTANT_VAR by error<KtElement>(PositioningStrategy.DECLARATION_NAME) {
            parameter<String>("propertyName")
            isSuppressible = true
        }
        val BRS_BRSCONSTANT_NON_OBJECT by error<KtElement>(PositioningStrategy.DECLARATION_NAME) {
            parameter<String>("classKind")
            isSuppressible = true
        }
    }
}
