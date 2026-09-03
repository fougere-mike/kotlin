/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.name

import org.jetbrains.kotlin.name.StandardClassIds.BASE_KOTLIN_PACKAGE

/**
 * Standard class IDs for BrightScript-specific Kotlin types and annotations.
 */
object BrsStandardClassIds {
    val BASE_BRS_PACKAGE = BASE_KOTLIN_PACKAGE.child(Name.identifier("brs"))
    val BASE_BRS_INTERNAL_PACKAGE = BASE_BRS_PACKAGE.child(Name.identifier("internal"))
    val BASE_BRS_ROKU_PACKAGE = BASE_BRS_PACKAGE.child(Name.identifier("roku"))
    val BASE_BRS_COROUTINES_PACKAGE = BASE_BRS_PACKAGE.child(Name.identifier("coroutines"))
    val BASE_COROUTINES_TASK_PACKAGE = BASE_KOTLIN_PACKAGE.child(Name.identifier("coroutines")).child(Name.identifier("task"))
    val BASE_COROUTINES_FLOW_PACKAGE = BASE_KOTLIN_PACKAGE.child(Name.identifier("coroutines")).child(Name.identifier("flow"))

    /**
     * Built-in BrightScript object types.
     */
    object BuiltIns {
        @JvmField
        val roArray = "roArray".brsInternalId()

        @JvmField
        val roAssociativeArray = "roAssociativeArray".brsInternalId()

        @JvmField
        val roSGNode = "roSGNode".brsInternalId()

        @JvmField
        val roSGScreen = "roSGScreen".brsInternalId()

        @JvmField
        val roSGScreenEvent = "roSGScreenEvent".brsInternalId()

        @JvmField
        val roSGNodeEvent = "roSGNodeEvent".brsInternalId()

        @JvmField
        val roString = "roString".brsInternalId()

        @JvmField
        val roInt = "roInt".brsInternalId()

        @JvmField
        val roFloat = "roFloat".brsInternalId()

        @JvmField
        val roDouble = "roDouble".brsInternalId()

        @JvmField
        val roBoolean = "roBoolean".brsInternalId()

        @JvmField
        val roRegex = "roRegex".brsInternalId()

        @JvmField
        val roDateTime = "roDateTime".brsInternalId()

        @JvmField
        val roTimespan = "roTimespan".brsInternalId()

        @JvmField
        val roByteArray = "roByteArray".brsInternalId()

        @JvmField
        val roUrlTransfer = "roUrlTransfer".brsInternalId()

        @JvmField
        val roMessagePort = "roMessagePort".brsInternalId()

        @JvmField
        val roInput = "roInput".brsInternalId()

        @JvmField
        val roPath = "roPath".brsInternalId()

        @JvmField
        val roFileSystem = "roFileSystem".brsInternalId()

        @JvmField
        val roDeviceInfo = "roDeviceInfo".brsInternalId()

        @JvmField
        val roAppInfo = "roAppInfo".brsInternalId()

        @JvmField
        val roRegistry = "roRegistry".brsInternalId()

        @JvmField
        val roRegistrySection = "roRegistrySection".brsInternalId()

        @JvmField
        val roUtils = "roUtils".brsInternalId()

        // Native iteration marker types (kotlin.brs.roku package)
        @JvmField
        val nativeArrayIterator = "NativeArrayIterator".brsRokuId()

        @JvmField
        val nativeIterable = "NativeIterable".brsRokuId()

        // Native type interfaces (kotlin.brs.roku package)
        @JvmField
        val roArrayInterface = "RoArray".brsRokuId()

        @JvmField
        val roAssociativeArrayInterface = "RoAssociativeArray".brsRokuId()

        @JvmField
        val iEnumNative = "IEnumNative".brsRokuId()

        // ==================== Coroutine Types (kotlin.brs.coroutines package) ====================

        /** Base class for coroutine state machine implementations */
        @JvmField
        val coroutineImpl = "CoroutineImpl".brsCoroutinesId()

        /** Marker object for COROUTINE_SUSPENDED sentinel value */
        @JvmField
        val coroutineSingletons = "CoroutineSingletons".brsCoroutinesId()

        /** Safe continuation wrapper for double-resume protection */
        @JvmField
        val safeContinuation = "SafeContinuation".brsCoroutinesId()

        // SceneGraph type interfaces (kotlin.brs.roku package)
        @JvmField
        val roSGScreenInterface = "RoSGScreen".brsRokuId()

        @JvmField
        val roSGNodeInterface = "RoSGNode".brsRokuId()

        @JvmField
        val iSGNodeFieldInterface = "ISGNodeField".brsRokuId()

        @JvmField
        val iSGNodeDictInterface = "ISGNodeDict".brsRokuId()

        @JvmField
        val roSGScreenEventInterface = "RoSGScreenEvent".brsRokuId()

        @JvmField
        val roSGNodeEventInterface = "RoSGNodeEvent".brsRokuId()
    }

    /**
     * BrightScript-specific annotations for controlling code generation.
     */
    object Annotations {
        /**
         * Marks a declaration as external (implemented in BrightScript).
         */
        @JvmField
        val BrsExternal = "BrsExternal".brsId()

        /**
         * Marks an external object as a BrighterScript namespace.
         * Calls compile to Namespace_functionName format.
         */
        @JvmField
        val BrsNamespace = "BrsNamespace".brsId()

        /**
         * Specifies a custom BrightScript name for a declaration.
         */
        @JvmField
        val BrsName = "BrsName".brsId()

        /**
         * Marks a function to be exported in the component interface.
         */
        @JvmField
        val BrsExport = "BrsExport".brsId()

        /**
         * Marks a property as a component field.
         */
        @JvmField
        val BrsField = "BrsField".brsId()

        /**
         * Specifies the onChange handler for a field.
         */
        @JvmField
        val BrsOnChange = "BrsOnChange".brsId()

        /**
         * Marks a field to always notify on changes.
         */
        @JvmField
        val BrsAlwaysNotify = "BrsAlwaysNotify".brsId()

        /**
         * Marks a class as a SceneGraph component.
         */
        @JvmField
        val BrsComponent = "BrsComponent".brsId()

        /**
         * Specifies the parent component to extend.
         */
        @JvmField
        val BrsExtends = "BrsExtends".brsId()

        /**
         * Marks code to be inlined as raw BrightScript.
         */
        @JvmField
        val BrsInline = "BrsInline".brsId()

        /**
         * Marks a companion object function as a CreateObject factory.
         */
        @JvmField
        val BrsCreateObject = "BrsCreateObject".brsId()

        /**
         * Suppresses specific compiler warnings for BrightScript.
         */
        @JvmField
        val BrsSuppress = "BrsSuppress".brsId()

        /**
         * Specifies the minimum Roku OS version required.
         */
        @JvmField
        val BrsMinRokuOS = "BrsMinRokuOS".brsId()

        /**
         * Marks a declaration as internal to the generated code.
         */
        @JvmField
        val BrsInternal = "BrsInternal".brsInternalId()

        /**
         * Marks a class as a SceneGraph component base class.
         * Used on abstract classes like SceneComponent, TaskComponent, etc.
         */
        @JvmField
        val BrsSceneGraphComponent = "BrsSceneGraphComponent".brsId()

        /**
         * Marks a function to use its unmangled name for BrightScript interop.
         * Valid on top-level functions, object members, and companion object members.
         */
        @JvmField
        val BrsStatic = "BrsStatic".brsId()

        /**
         * Marks an object declaration as containing compile-time constants.
         * Properties are evaluated at compile time and inlined at usage sites.
         */
        @JvmField
        val BrsConstant = "BrsConstant".brsId()

        /**
         * Marks a function as a BrightScript intrinsic.
         * Reserved for stdlib use only — user-defined functions carrying this annotation
         * are reported as BRS_INTRINSIC_USER_DEFINED.
         */
        @JvmField
        val BrsIntrinsic = "BrsIntrinsic".brsId()

        // ==================== Type-Safe SceneGraph Field Annotations ====================

        /** Marks a property as a SceneGraph string field. */
        @JvmField
        val SGStringField = "SGStringField".brsId()

        /** Marks a property as a SceneGraph integer field. */
        @JvmField
        val SGIntegerField = "SGIntegerField".brsId()

        /** Marks a property as a SceneGraph long integer field. */
        @JvmField
        val SGLongIntegerField = "SGLongIntegerField".brsId()

        /** Marks a property as a SceneGraph float field. */
        @JvmField
        val SGFloatField = "SGFloatField".brsId()

        /** Marks a property as a SceneGraph double field. */
        @JvmField
        val SGDoubleField = "SGDoubleField".brsId()

        /** Marks a property as a SceneGraph boolean field. */
        @JvmField
        val SGBooleanField = "SGBooleanField".brsId()

        /** Marks a property as a SceneGraph array field. */
        @JvmField
        val SGArrayField = "SGArrayField".brsId()

        /** Marks a property as a SceneGraph associative array field. */
        @JvmField
        val SGAssocArrayField = "SGAssocArrayField".brsId()

        /** Marks a property as a SceneGraph node field. */
        @JvmField
        val SGNodeField = "SGNodeField".brsId()

        /** Marks a property as a SceneGraph function reference field. */
        @JvmField
        val SGFunctionField = "SGFunctionField".brsId()

        /** Marks a property as a SceneGraph URI field. */
        @JvmField
        val SGUriField = "SGUriField".brsId()

        /** Marks a property as a SceneGraph time field. */
        @JvmField
        val SGTimeField = "SGTimeField".brsId()

        /** Marks a property as a SceneGraph 2D vector field. */
        @JvmField
        val SGVector2DField = "SGVector2DField".brsId()

        /** Marks a property as a SceneGraph color field. */
        @JvmField
        val SGColorField = "SGColorField".brsId()

        /**
         * Map of type-safe field annotation ClassIds to their BrightScript type strings.
         */
        @JvmField
        val sgFieldAnnotationTypes = mapOf(
            SGStringField to "string",
            SGIntegerField to "integer",
            SGLongIntegerField to "longinteger",
            SGFloatField to "float",
            SGDoubleField to "double",
            SGBooleanField to "boolean",
            SGArrayField to "array",
            SGAssocArrayField to "assocarray",
            SGNodeField to "node",
            SGFunctionField to "function",
            SGUriField to "uri",
            SGTimeField to "time",
            SGVector2DField to "vector2d",
            SGColorField to "color"
        )

        /**
         * Annotations that must be on external declarations.
         */
        @JvmField
        val annotationsRequiringExternal = setOf(BrsExternal)
    }

    /**
     * SceneGraph component base classes.
     */
    object Components {
        /**
         * Abstract base class for all SceneGraph components.
         * Provides top, global, m properties and onKeyEvent.
         */
        @JvmField
        val ComponentBase = "ComponentBase".brsId()

        /**
         * Base class for SceneGraph components that extend Group.
         */
        @JvmField
        val GroupComponent = "GroupComponent".brsId()

        /**
         * Base class for SceneGraph components that extend LayoutGroup.
         * Use for components that need automatic layout of children.
         */
        @JvmField
        val LayoutComponent = "LayoutComponent".brsId()

        /**
         * Base class for SceneGraph Scene components.
         */
        @JvmField
        val SceneComponent = "SceneComponent".brsId()

        /**
         * Base class for SceneGraph Task components.
         */
        @JvmField
        val TaskComponent = "TaskComponent".brsId()

        /**
         * Base class for SceneGraph ContentNode components.
         * Use for data models in lists, grids, etc.
         */
        @JvmField
        val ContentNodeComponent = "ContentNodeComponent".brsId()

        /**
         * @deprecated Use [SceneComponent] instead.
         */
        @Deprecated("Use SceneComponent instead", ReplaceWith("SceneComponent"))
        @JvmField
        val SceneNodeComponent = "SceneNodeComponent".brsId()
    }

    /**
     * ScopeHandle (kotlin.brs) — cross-component scope borrowing.
     */
    object Scope {
        /** kotlin.brs.ScopeHandle — the borrowed-scope handle class. */
        @JvmField
        val scopeHandle = "ScopeHandle".brsId()

        /** kotlin.brs.ScopeRequest — 0-arg request declaration base (`<R>`). */
        @JvmField
        val scopeRequest = "ScopeRequest".brsId()

        /** kotlin.brs.ScopeRequest1 — 1-arg request declaration base (`<A1, R>`). */
        @JvmField
        val scopeRequest1 = "ScopeRequest1".brsId()

        /** kotlin.brs.ScopeRequest2 — 2-arg request declaration base (`<A1, A2, R>`). */
        @JvmField
        val scopeRequest2 = "ScopeRequest2".brsId()
    }

    /**
     * SharedService (kotlin.brs) — reference-shared classes across components.
     */
    object Shared {
        /**
         * kotlin.brs.SharedService — the SOLE machinery root (design §4.5): the
         * BRS_SHARED_* FIR rules, the Phase 3 dispatch lowering, and
         * publish/acquire all key on this one ClassId. No other type name is
         * known to the compiler.
         */
        @JvmField
        val sharedService = "SharedService".brsId()
    }

    /**
     * BrightScript-specific callable IDs.
     */
    object Callables {
        /**
         * Inline BrightScript code function.
         */
        @JvmField
        val brs = "brs".callableId(BASE_BRS_PACKAGE)

        /**
         * CreateObject function for BrightScript object instantiation.
         */
        @JvmField
        val createObject = "createObject".callableId(BASE_BRS_PACKAGE)

        /**
         * Type function for runtime type checking.
         */
        @JvmField
        val typeOf = "typeOf".callableId(BASE_BRS_PACKAGE)

        /**
         * Print function for debugging output.
         */
        @JvmField
        val print = "print".callableId(BASE_BRS_PACKAGE)

        /**
         * brsName(::ref) — extracts mangled BrightScript function name.
         */
        @JvmField
        val brsName = "brsName".callableId(BASE_BRS_PACKAGE)

        /**
         * Placeholder for externally defined values.
         */
        @JvmField
        val definedExternally = "definedExternally".callableId(BASE_BRS_PACKAGE)

        // ==================== SceneGraph Callables ====================

        @JvmField
        val iSGNodeFieldAddField = CallableId(BuiltIns.iSGNodeFieldInterface, Name.identifier("addField"))

        @JvmField
        val roSGNodeAddField = CallableId(BuiltIns.roSGNodeInterface, Name.identifier("addField"))

        @JvmField
        val addFieldCallables: Set<CallableId> = setOf(iSGNodeFieldAddField, roSGNodeAddField)

        // ==================== Component Factory Callables ====================

        /**
         * createComponent<T>() — reified SceneGraph component factory (kotlin.brs.ComponentFactory).
         */
        @JvmField
        val createComponent = "createComponent".callableId(BASE_BRS_PACKAGE)

        /**
         * brsCreateComponent<T>() — backend intrinsic behind [createComponent].
         */
        @JvmField
        val brsCreateComponent = "brsCreateComponent".callableId(BASE_BRS_PACKAGE)

        /**
         * runTask<T>{} — reified typed-task launcher (kotlin.coroutines.task.TaskRunner).
         */
        @JvmField
        val runTask = "runTask".callableId(BASE_COROUTINES_TASK_PACKAGE)

        /**
         * Callables whose reified type argument must be a concrete SceneGraph component
         * class (checked by FirBrsCreateComponentTypeChecker).
         */
        @JvmField
        val componentFactoryCallables: Set<CallableId> = setOf(createComponent, brsCreateComponent, runTask)

        // ==================== ScopeHandle Callables ====================

        /**
         * ScopeHandle.run — member overloads. The suspend-block overload
         * (single `suspend () -> R` parameter) is rewritten by the compiler
         * to [scopeRunLowered]; FirBrsScopeBlockChecker requires its argument
         * to be a literal lambda so the rewrite can lift it.
         */
        @JvmField
        val scopeHandleRun = CallableId(Scope.scopeHandle, Name.identifier("run"))

        /**
         * kotlin.brs.runLowered — the lowered entry point targeted by the
         * `ScopeHandle.run { block }` rewrite (BrsScopeRunBlockLowering).
         */
        @JvmField
        val scopeRunLowered = "runLowered".callableId(BASE_BRS_PACKAGE)

        // ==================== Copying-Channel Callables ====================

        /**
         * ISGNodeField.setField / RoSGNode.setField — the field-write copying
         * channel (FirBrsSharedCopyChannelChecker flags SharedService-typed
         * value arguments). Both ids: RoSGNode overrides the declaration, and
         * subtype receivers resolve to the override (addFieldCallables
         * precedent).
         */
        @JvmField
        val iSGNodeFieldSetField = CallableId(BuiltIns.iSGNodeFieldInterface, Name.identifier("setField"))

        @JvmField
        val roSGNodeSetField = CallableId(BuiltIns.roSGNodeInterface, Name.identifier("setField"))

        @JvmField
        val setFieldCallables: Set<CallableId> = setOf(iSGNodeFieldSetField, roSGNodeSetField)

        /** ISGNodeDict.callFunc / RoSGNode.callFunc — the cross-component call copying channel. */
        @JvmField
        val iSGNodeDictCallFunc = CallableId(BuiltIns.iSGNodeDictInterface, Name.identifier("callFunc"))

        @JvmField
        val roSGNodeCallFunc = CallableId(BuiltIns.roSGNodeInterface, Name.identifier("callFunc"))

        @JvmField
        val callFuncCallables: Set<CallableId> = setOf(iSGNodeDictCallFunc, roSGNodeCallFunc)

        /**
         * kotlin.brs.asDynamic / unsafeCast — type-erasing casts the copy-channel
         * checker unwraps to classify the underlying value (a Dynamic-typed
         * parameter forces `value.asDynamic()` at the call site).
         */
        @JvmField
        val asDynamic = "asDynamic".callableId(BASE_BRS_PACKAGE)

        @JvmField
        val unsafeCast = "unsafeCast".callableId(BASE_BRS_PACKAGE)

        // ==================== SharedService Callables ====================

        /**
         * sharedFrom<T>(node[, key]) — reified shared-instance acquisition
         * (kotlin.brs.SharedService.kt). Call sites are rewritten to
         * `sharedAcquire(node, key, "<ClassName>", orNull)` by
         * BrsSharedFromCallLowering — klib inline functions are never inlined
         * at user call sites, so the reified type argument only exists on the
         * un-inlined IrCall.
         */
        @JvmField
        val sharedFrom = "sharedFrom".callableId(BASE_BRS_PACKAGE)

        /** sharedFromOrNull<T>(node[, key]) — the null-on-absence variant of [sharedFrom]. */
        @JvmField
        val sharedFromOrNull = "sharedFromOrNull".callableId(BASE_BRS_PACKAGE)

        // ==================== Flow Callables ====================

        /**
         * Flow<T>.flowOn(context) — the task-lift entry point
         * (kotlin.coroutines.flow, kotlin-flow-brs klib). The lift is a
         * compile-time lowering, so FirBrsTaskDispatcherChecker requires the
         * argument to be the literal Dispatchers.Task token (and rejects the
         * token everywhere else); the un-lowered stdlib body is a guided-throw
         * runtime backstop.
         */
        @JvmField
        val flowOn = "flowOn".callableId(BASE_COROUTINES_FLOW_PACKAGE)

        // ==================== Coroutine Callables ====================

        /**
         * Gets the COROUTINE_SUSPENDED sentinel value.
         */
        @JvmField
        val coroutineSuspendedGetter = "getCOROUTINE_SUSPENDED".callableId(BASE_BRS_COROUTINES_PACKAGE)

        /**
         * Gets the current continuation from suspend function context.
         */
        @JvmField
        val getContinuation = "getContinuation".callableId(BASE_BRS_COROUTINES_PACKAGE)

        /**
         * Gets the coroutine context from a continuation.
         */
        @JvmField
        val getCoroutineContext = "getCoroutineContext".callableId(BASE_BRS_COROUTINES_PACKAGE)
    }

    /**
     * Valid canonical type-name strings for [Callables.createObject].
     *
     * Derived from [BuiltIns] short class names. Adding a new [BuiltIns] entry
     * automatically extends the valid set.
     */
    @JvmField
    val brsCreateObjectValidTypes: Set<String> = setOf(
        BuiltIns.roArray, BuiltIns.roAssociativeArray, BuiltIns.roSGNode,
        BuiltIns.roSGScreen, BuiltIns.roSGScreenEvent, BuiltIns.roSGNodeEvent,
        BuiltIns.roString, BuiltIns.roInt, BuiltIns.roFloat, BuiltIns.roDouble,
        BuiltIns.roBoolean, BuiltIns.roRegex, BuiltIns.roDateTime, BuiltIns.roTimespan,
        BuiltIns.roByteArray, BuiltIns.roUrlTransfer, BuiltIns.roMessagePort,
        BuiltIns.roInput, BuiltIns.roPath, BuiltIns.roFileSystem,
        BuiltIns.roDeviceInfo, BuiltIns.roAppInfo, BuiltIns.roRegistry,
        BuiltIns.roRegistrySection, BuiltIns.roUtils,
    ).map { it.shortClassName.asString() }.toSet()
}

private fun String.brsId() = ClassId(BrsStandardClassIds.BASE_BRS_PACKAGE, Name.identifier(this))

private fun String.brsInternalId() = ClassId(BrsStandardClassIds.BASE_BRS_INTERNAL_PACKAGE, Name.identifier(this))

private fun String.brsRokuId() = ClassId(BrsStandardClassIds.BASE_BRS_ROKU_PACKAGE, Name.identifier(this))

private fun String.brsCoroutinesId() = ClassId(BrsStandardClassIds.BASE_BRS_COROUTINES_PACKAGE, Name.identifier(this))

private fun String.callableId(packageName: FqName) = CallableId(packageName, Name.identifier(this))
