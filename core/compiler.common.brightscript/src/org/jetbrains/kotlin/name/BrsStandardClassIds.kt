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
         * Base class for SceneGraph components that extend Group.
         */
        @JvmField
        val SceneComponent = "SceneComponent".brsId()

        /**
         * Base class for SceneGraph Task components.
         */
        @JvmField
        val TaskComponent = "TaskComponent".brsId()

        /**
         * Base class for SceneGraph Scene components.
         */
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
    }
}

private fun String.brsId() = ClassId(BrsStandardClassIds.BASE_BRS_PACKAGE, Name.identifier(this))

private fun String.brsInternalId() = ClassId(BrsStandardClassIds.BASE_BRS_INTERNAL_PACKAGE, Name.identifier(this))

private fun String.brsRokuId() = ClassId(BrsStandardClassIds.BASE_BRS_ROKU_PACKAGE, Name.identifier(this))

private fun String.callableId(packageName: FqName) = CallableId(packageName, Name.identifier(this))
