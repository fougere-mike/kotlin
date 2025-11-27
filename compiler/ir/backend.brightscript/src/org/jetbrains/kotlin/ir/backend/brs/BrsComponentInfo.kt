/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs

import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty

/**
 * Metadata about a SceneGraph component extracted from a Kotlin class.
 *
 * This captures all information needed to generate both the XML component
 * definition and the BrightScript implementation.
 */
data class BrsComponentInfo(
    /**
     * The IR class this component was extracted from.
     */
    val irClass: IrClass,

    /**
     * The BrightScript name for this component.
     */
    val name: String,

    /**
     * The parent component to extend (e.g., "Group", "Task", "Node").
     */
    val extendsComponent: String,

    /**
     * Fields exposed in the component interface.
     */
    val fields: List<BrsFieldInfo>,

    /**
     * Functions exported in the component interface.
     */
    val exports: List<BrsExportInfo>,

    /**
     * Additional script URIs to include.
     */
    val additionalScripts: List<String> = emptyList()
)

/**
 * Metadata about a field exposed in a SceneGraph component interface.
 */
data class BrsFieldInfo(
    /**
     * The field name as it appears in XML.
     */
    val name: String,

    /**
     * The BrightScript type for the field.
     */
    val type: String,

    /**
     * Default value for the field, if any.
     */
    val defaultValue: String? = null,

    /**
     * The onChange handler function name, if any.
     */
    val onChange: String? = null,

    /**
     * Whether to always notify on changes even if value is the same.
     */
    val alwaysNotify: Boolean = false,

    /**
     * Field alias (for observeField/setField from BrightScript).
     */
    val alias: String? = null,

    /**
     * The IR property this field was extracted from.
     */
    val irProperty: IrProperty? = null,

    /**
     * The IR field this was extracted from.
     */
    val irField: IrField? = null
)

/**
 * Metadata about a function exported in a SceneGraph component interface.
 */
data class BrsExportInfo(
    /**
     * The function name as it appears in XML.
     */
    val name: String,

    /**
     * The IR function this export was extracted from.
     */
    val irFunction: IrFunction
)

/**
 * BrightScript field types for SceneGraph.
 */
object BrsFieldTypes {
    const val STRING = "string"
    const val INTEGER = "integer"
    const val LONG_INTEGER = "longinteger"
    const val FLOAT = "float"
    const val DOUBLE = "double"
    const val BOOLEAN = "boolean"
    const val ARRAY = "array"
    const val ASSOC_ARRAY = "assocarray"
    const val NODE = "node"
    const val FUNCTION = "function"
    const val URI = "uri"
    const val TIME = "time"
    const val VECTOR_2D = "vector2d"
    const val COLOR = "color"

    /**
     * Map Kotlin types to BrightScript field types.
     */
    fun fromKotlinType(typeName: String): String {
        return when (typeName.lowercase()) {
            "int", "short", "byte" -> INTEGER
            "long" -> LONG_INTEGER
            "float" -> FLOAT
            "double" -> DOUBLE
            "boolean" -> BOOLEAN
            "string" -> STRING
            "list", "array", "arraylist" -> ARRAY
            "map", "hashmap", "linkedhashmap" -> ASSOC_ARRAY
            else -> ASSOC_ARRAY
        }
    }
}
