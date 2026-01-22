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
         * Placeholder for externally defined values.
         */
        @JvmField
        val definedExternally = "definedExternally".callableId(BASE_BRS_PACKAGE)

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
}

private fun String.brsId() = ClassId(BrsStandardClassIds.BASE_BRS_PACKAGE, Name.identifier(this))

private fun String.brsInternalId() = ClassId(BrsStandardClassIds.BASE_BRS_INTERNAL_PACKAGE, Name.identifier(this))

private fun String.brsRokuId() = ClassId(BrsStandardClassIds.BASE_BRS_ROKU_PACKAGE, Name.identifier(this))

private fun String.brsCoroutinesId() = ClassId(BrsStandardClassIds.BASE_BRS_COROUTINES_PACKAGE, Name.identifier(this))

private fun String.callableId(packageName: FqName) = CallableId(packageName, Name.identifier(this))
