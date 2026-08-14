/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.diagnostics.brs

import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.diagnostics.rendering.CommonRenderers
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors.BRS_ADDFIELD_INVALID_TYPE
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors.BRS_BRSCREATEOBJECT_INVALID_TYPE
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors.BRS_BRSCONSTANT_NON_OBJECT
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors.BRS_BRSCONSTANT_VAR
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors.BRS_BRSNAME_BLANK
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors.BRS_BRSNAME_INVALID_TARGET
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors.BRS_BRSNAME_REQUIRES_CALLABLE_REF
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors.BRS_CREATE_COMPONENT_INVALID_TYPE
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors.BRS_CREATE_OBJECT_INVALID_TYPE
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors.BRS_INTRINSIC_LITERAL_REQUIRED
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors.BRS_INTRINSIC_USER_DEFINED
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors.BRS_IO_DISPATCHER_UNSUPPORTED
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors.BRS_IO_WORKER_NON_SERIALIZABLE_CAPTURE
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors.BRS_INHERITED_NAME_CASE_CLASH
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors.BRS_NAME_CASE_CLASH
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors.BRS_ONCHANGE_HANDLER_NOT_FOUND
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors.BRS_ONCHANGE_HANDLER_SIGNATURE
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors.BRS_SCENEGRAPH_FIELD_CONFLICT
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors.BRS_SCENEGRAPH_FIELD_TYPE
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors.BRS_SCOPE_ARG_NOT_MARSHALLABLE
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors.BRS_SCOPE_BLOCK_NOT_LITERAL
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors.BRS_SCOPE_CAPTURE_MUTATION_LOST
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors.BRS_SCOPE_CAPTURE_UNMARSHALLABLE
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors.BRS_SCOPE_RESULT_NOT_DATA
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors.BRS_STATIC_INVALID_TARGET
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors.BRS_STATIC_OVERLOAD
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors.BRS_TASK_STATE_NOT_FIELD
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors.BRS_TRY_FINALLY_UNSUPPORTED

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
            BRS_IO_DISPATCHER_UNSUPPORTED,
            "Dispatchers.IO is unsupported on this platform: IO dispatch silently falls back to the " +
                "render-thread queue, so it behaves exactly like Dispatchers.Main. " +
                "Use runTask<T> for background work.",
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
            BRS_BRSNAME_INVALID_TARGET,
            "brsName() target must be a top-level function, an object/companion-object member, an instance method of a regular class, or a constructor. ''{1}'' is a {0} and produces a name that does not match a runtime BrightScript function.",
            CommonRenderers.STRING, CommonRenderers.STRING,
        )
        map.put(
            BRS_BRSNAME_BLANK,
            "@BrsName argument must be a non-blank BrightScript identifier on declaration ''{0}''. Remove the annotation to fall back to the Kotlin name, or supply a valid identifier.",
            CommonRenderers.STRING,
        )
        map.put(
            BRS_BRSCONSTANT_VAR,
            "@BrsConstant objects cannot contain var properties. Use val instead: ''{0}''.",
            CommonRenderers.STRING,
        )
        map.put(
            BRS_BRSCONSTANT_NON_OBJECT,
            "@BrsConstant is only valid on object declarations. Found on {0} declaration.",
            CommonRenderers.STRING,
        )
        map.put(
            BRS_BRSCREATEOBJECT_INVALID_TYPE,
            "''{0}'' is not a valid BrightScript object type for @BrsCreateObject. Valid types: {1}.",
            CommonRenderers.STRING, CommonRenderers.STRING,
        )
        map.put(
            BRS_TRY_FINALLY_UNSUPPORTED,
            "try/finally is not supported outside suspend functions on the BrightScript backend: " +
                "the 'finally' block is silently dropped. Move this code into a suspend function, " +
                "restructure without 'finally', or @Suppress(\"BRS_TRY_FINALLY_UNSUPPORTED\") with a tracking comment.",
        )
        map.put(
            BRS_SCOPE_BLOCK_NOT_LITERAL,
            "ScopeHandle.run { } requires a literal lambda at the call site (the compiler lifts it into a named request). " +
                "Passing a stored function value cannot be lowered — declare a ScopeRequest object and use run(request, args) instead.",
        )
        map.put(
            BRS_SCOPE_CAPTURE_UNMARSHALLABLE,
            "Captured ''{0}'' of type ''{1}'' cannot cross the scope boundary: captures cross by copy as plain data, and this " +
                "type''s methods do not survive the copy. Marshallable: primitives, String, Dynamic, and native BrightScript " +
                "types (RoArray, RoAssociativeArray, RoSGNode, and other external interfaces). Pass plain data (an " +
                "RoAssociativeArray or primitives) or move the value''s construction inside the block.",
            CommonRenderers.STRING, CommonRenderers.STRING,
        )
        map.put(
            BRS_SCOPE_CAPTURE_MUTATION_LOST,
            // No literal braces here: parameterized messages go through MessageFormat,
            // where a bare '{' is a parse error that poisons the whole renderer map.
            "Assignment to captured variable ''{0}'' inside a ScopeHandle.run block: captures cross by copy; " +
                "this write never reaches the caller — return a value from the block instead.",
            CommonRenderers.STRING,
        )
        map.put(
            BRS_SCOPE_RESULT_NOT_DATA,
            "Scope request result type ''{0}'' is outside the marshallable set (primitives, String, Dynamic, and external " +
                "interfaces like RoArray/RoAssociativeArray/RoSGNode): results cross as data; methods/equals/copy will not " +
                "survive — share behavioral state via the VM.",
            CommonRenderers.STRING,
        )
        map.put(
            BRS_SCOPE_ARG_NOT_MARSHALLABLE,
            "Scope request argument type ''{0}'' is outside the marshallable set (primitives, String, Dynamic, and external " +
                "interfaces like RoArray/RoAssociativeArray/RoSGNode): arguments cross by copy as plain data — " +
                "pass plain data or restructure the request.",
            CommonRenderers.STRING,
        )
        map.put(
            BRS_TASK_STATE_NOT_FIELD,
            "Property ''{0}'' in task component ''{1}'' compiles to plain m-state: run() executes against a task-thread copy, " +
                "and writes from run() are silently lost. Annotate it with an @SG*Field annotation (or @BrsField), " +
                "or make it a local variable in run().",
            CommonRenderers.STRING, CommonRenderers.STRING,
        )
        map.put(
            BRS_CREATE_COMPONENT_INVALID_TYPE,
            "''{1}'' is not a valid component type for {0}(): {2}. The type argument must be a concrete component class " +
                "(extending GroupComponent, SceneComponent, TaskComponent, etc., or annotated with @BrsComponent).",
            CommonRenderers.STRING, CommonRenderers.STRING, CommonRenderers.STRING,
        )
    }
}
