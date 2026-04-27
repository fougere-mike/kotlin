/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.diagnostics.brs

import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.diagnostics.rendering.CommonRenderers
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors.BRS_ADDFIELD_INVALID_TYPE
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors.BRS_BRSCONSTANT_VAR
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors.BRS_BRSNAME_REQUIRES_CALLABLE_REF
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors.BRS_CREATE_OBJECT_INVALID_TYPE
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors.BRS_INTRINSIC_LITERAL_REQUIRED
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors.BRS_INTRINSIC_USER_DEFINED
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors.BRS_IO_WORKER_NON_SERIALIZABLE_CAPTURE
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors.BRS_INHERITED_NAME_CASE_CLASH
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors.BRS_NAME_CASE_CLASH
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors.BRS_ONCHANGE_HANDLER_NOT_FOUND
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors.BRS_ONCHANGE_HANDLER_SIGNATURE
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors.BRS_SCENEGRAPH_FIELD_CONFLICT
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors.BRS_SCENEGRAPH_FIELD_TYPE
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors.BRS_STATIC_INVALID_TARGET
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors.BRS_STATIC_OVERLOAD

@Suppress("unused")
object FirBrsErrorsDefaultMessages : BaseDiagnosticRendererFactory() {
    override val MAP: KtDiagnosticFactoryToRendererMap by KtDiagnosticFactoryToRendererMap("FIR") { map ->
        map.put(BRS_INTRINSIC_LITERAL_REQUIRED, "An argument for the 'brs()' function must be a compile-time constant string.")
        map.put(
            BRS_INTRINSIC_USER_DEFINED,
            "@BrsIntrinsic is reserved for the BrightScript stdlib. Function ''{0}'' cannot be marked as an intrinsic.",
            CommonRenderers.STRING,
        )
        map.put(
            BRS_NAME_CASE_CLASH,
            "Name clashes with {0} at BrightScript runtime (BrightScript is case-insensitive).",
            CommonRenderers.STRING,
        )
        map.put(
            BRS_INHERITED_NAME_CASE_CLASH,
            "Inherited members ''{0}'' and ''{1}'' clash at BrightScript runtime (BrightScript is case-insensitive). Override one or suppress.",
            CommonRenderers.STRING, CommonRenderers.STRING,
        )
        map.put(
            BRS_SCENEGRAPH_FIELD_TYPE,
            "{0} requires {1} property type, got {2}.",
            CommonRenderers.STRING, CommonRenderers.STRING, CommonRenderers.STRING,
        )
        map.put(
            BRS_ONCHANGE_HANDLER_NOT_FOUND,
            "@BrsOnChange handler ''{0}'' not found in class ''{1}''.",
            CommonRenderers.STRING, CommonRenderers.STRING,
        )
        map.put(
            BRS_SCENEGRAPH_FIELD_CONFLICT,
            "Property has conflicting SceneGraph field annotations: {0}. Keep only one.",
            CommonRenderers.STRING,
        )
        map.put(
            BRS_ONCHANGE_HANDLER_SIGNATURE,
            "@BrsOnChange handler ''{0}'' on class ''{1}'' must take no arguments or a single RoSGNodeEvent; got signature {2}.",
            CommonRenderers.STRING, CommonRenderers.STRING, CommonRenderers.STRING,
        )
        map.put(
            BRS_ADDFIELD_INVALID_TYPE,
            "''{0}'' is not a valid SceneGraph field type for addField(). Valid types: {1}.",
            CommonRenderers.STRING, CommonRenderers.STRING,
        )
        map.put(
            BRS_CREATE_OBJECT_INVALID_TYPE,
            "''{0}'' is not a valid BrightScript object type for createObject(). Valid types: {1}.",
            CommonRenderers.STRING, CommonRenderers.STRING,
        )
        map.put(
            BRS_IO_WORKER_NON_SERIALIZABLE_CAPTURE,
            "Captured ''{0}'' of type ''{1}'' cannot cross the Dispatchers.IO task thread boundary. " +
                "Only primitives, String, RoArray, RoAssociativeArray, and Dynamic are serializable.",
            CommonRenderers.STRING, CommonRenderers.STRING,
        )
        map.put(
            BRS_STATIC_INVALID_TARGET,
            "@BrsStatic is only valid on top-level functions, object members, or companion object members. " +
                "Found on member of class ''{0}''.",
            CommonRenderers.STRING,
        )
        map.put(
            BRS_STATIC_OVERLOAD,
            "@BrsStatic function ''{0}'' cannot have overloads. Found {1} functions with the same name in the same scope.",
            CommonRenderers.STRING, CommonRenderers.STRING,
        )
        map.put(
            BRS_BRSNAME_REQUIRES_CALLABLE_REF,
            "brsName() requires a function reference (::functionName), got ''{0}''.",
            CommonRenderers.STRING,
        )
        map.put(
            BRS_BRSCONSTANT_VAR,
            "@BrsConstant objects cannot contain var properties. Use val instead: ''{0}''.",
            CommonRenderers.STRING,
        )
    }
}
