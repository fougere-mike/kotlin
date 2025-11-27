/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.brs

import org.jetbrains.kotlin.name.BrsStandardClassIds
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName

/**
 * Predefined annotations for BrightScript code generation.
 */
enum class BrsPredefinedAnnotation(val classId: ClassId) {
    /**
     * Marks a declaration as externally implemented in BrightScript.
     */
    EXTERNAL(BrsStandardClassIds.Annotations.BrsExternal),

    /**
     * Specifies a custom BrightScript name.
     */
    NAME(BrsStandardClassIds.Annotations.BrsName),

    /**
     * Marks a function to be exported in the component interface.
     */
    EXPORT(BrsStandardClassIds.Annotations.BrsExport),

    /**
     * Marks a property as a component field.
     */
    FIELD(BrsStandardClassIds.Annotations.BrsField),

    /**
     * Specifies the onChange handler for a field.
     */
    ON_CHANGE(BrsStandardClassIds.Annotations.BrsOnChange),

    /**
     * Marks a class as a SceneGraph component.
     */
    COMPONENT(BrsStandardClassIds.Annotations.BrsComponent),

    /**
     * Specifies the parent component to extend.
     */
    EXTENDS(BrsStandardClassIds.Annotations.BrsExtends),

    /**
     * Marks code to be inlined as raw BrightScript.
     */
    INLINE(BrsStandardClassIds.Annotations.BrsInline);

    val fqName: FqName = classId.asSingleFqName()

    companion object {
        /**
         * Annotations that allow specifying a custom name.
         */
        val WITH_CUSTOM_NAME = setOf(NAME, EXTERNAL)

        /**
         * Annotations specific to SceneGraph components.
         */
        val COMPONENT_ANNOTATIONS = setOf(COMPONENT, EXTENDS, FIELD, ON_CHANGE, EXPORT)
    }
}
