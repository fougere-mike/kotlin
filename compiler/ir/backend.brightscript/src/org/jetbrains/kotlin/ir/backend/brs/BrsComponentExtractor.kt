/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs

import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.isInterface

/**
 * Extracts SceneGraph component metadata from Kotlin IR classes.
 *
 * This class processes classes annotated with @BrsComponent and extracts
 * field definitions, export declarations, and onChange handlers.
 */
class BrsComponentExtractor(
    private val context: BrsIrBackendContext
) {
    /**
     * Extract component information from an IR class, if it's a component.
     *
     * Returns null if the class is not a component.
     */
    fun extractComponent(irClass: IrClass): BrsComponentInfo? {
        val componentAnnotation = findAnnotation(irClass, "BrsComponent") ?: return null

        val name = context.getBrsName(irClass)
        val extendsComponent = getExtendsComponent(irClass, componentAnnotation)
        val fields = extractFields(irClass)
        val exports = extractExports(irClass)

        return BrsComponentInfo(
            irClass = irClass,
            name = name,
            extendsComponent = extendsComponent,
            fields = fields,
            exports = exports
        )
    }

    /**
     * Check if a class is a SceneGraph component.
     */
    fun isComponent(irClass: IrClass): Boolean {
        return findAnnotation(irClass, "BrsComponent") != null
    }

    /**
     * Get the parent component to extend.
     */
    private fun getExtendsComponent(irClass: IrClass, componentAnnotation: IrConstructorCall): String {
        // Check @BrsExtends annotation first
        val extendsAnnotation = findAnnotation(irClass, "BrsExtends")
        if (extendsAnnotation != null) {
            val value = getAnnotationStringArg(extendsAnnotation, 0)
            if (value != null) return value
        }

        // Check "extends" parameter on @BrsComponent
        val extendsArg = getAnnotationStringArg(componentAnnotation, "extends")
        if (extendsArg != null) return extendsArg

        // Check superclass
        val superClass = irClass.superTypes
            .mapNotNull { it.classOrNull?.owner }
            .firstOrNull { !it.isInterface && it.name.asString() != "Any" }

        if (superClass != null && isComponent(superClass)) {
            return context.getBrsName(superClass)
        }

        // Default to Group
        return "Group"
    }

    /**
     * Extract field definitions from the class.
     */
    private fun extractFields(irClass: IrClass): List<BrsFieldInfo> {
        val fields = mutableListOf<BrsFieldInfo>()

        // Extract from properties with @BrsField
        for (property in irClass.declarations.filterIsInstance<IrProperty>()) {
            val fieldAnnotation = findAnnotation(property, "BrsField")
            if (fieldAnnotation != null) {
                fields.add(extractFieldFromProperty(property, fieldAnnotation))
            }
        }

        // Extract from fields with @BrsField
        for (field in irClass.declarations.filterIsInstance<IrField>()) {
            val fieldAnnotation = findAnnotation(field, "BrsField")
            if (fieldAnnotation != null && fields.none { it.name == field.name.asString() }) {
                fields.add(extractFieldFromField(field, fieldAnnotation))
            }
        }

        return fields
    }

    /**
     * Extract field info from a property.
     */
    private fun extractFieldFromProperty(property: IrProperty, annotation: IrConstructorCall): BrsFieldInfo {
        val name = getAnnotationStringArg(annotation, "name")
            ?: property.name.asString()

        val type = getAnnotationStringArg(annotation, "type")
            ?: mapTypeToFieldType(property.getter?.returnType ?: property.backingField?.type)

        val defaultValue = getAnnotationStringArg(annotation, "defaultValue")
        val alias = getAnnotationStringArg(annotation, "alias")
        val alwaysNotify = getAnnotationBooleanArg(annotation, "alwaysNotify") ?: false

        // Check for @BrsOnChange annotation
        val onChange = findOnChangeHandler(property)

        return BrsFieldInfo(
            name = name,
            type = type,
            defaultValue = defaultValue,
            onChange = onChange,
            alwaysNotify = alwaysNotify,
            alias = alias,
            irProperty = property
        )
    }

    /**
     * Extract field info from a field declaration.
     */
    private fun extractFieldFromField(field: IrField, annotation: IrConstructorCall): BrsFieldInfo {
        val name = getAnnotationStringArg(annotation, "name")
            ?: field.name.asString()

        val type = getAnnotationStringArg(annotation, "type")
            ?: mapTypeToFieldType(field.type)

        val defaultValue = getAnnotationStringArg(annotation, "defaultValue")
        val alias = getAnnotationStringArg(annotation, "alias")
        val alwaysNotify = getAnnotationBooleanArg(annotation, "alwaysNotify") ?: false

        return BrsFieldInfo(
            name = name,
            type = type,
            defaultValue = defaultValue,
            alwaysNotify = alwaysNotify,
            alias = alias,
            irField = field
        )
    }

    /**
     * Find onChange handler for a property.
     */
    private fun findOnChangeHandler(property: IrProperty): String? {
        // Check @BrsOnChange on the property
        val onChangeAnnotation = findAnnotation(property, "BrsOnChange")
        if (onChangeAnnotation != null) {
            return getAnnotationStringArg(onChangeAnnotation, 0)
        }

        // Check for function named on{PropertyName}Changed in the class
        val parentClass = property.parent as? IrClass ?: return null
        val expectedName = "on${property.name.asString().replaceFirstChar { it.uppercase() }}Changed"

        val handler = parentClass.declarations
            .filterIsInstance<IrSimpleFunction>()
            .find { it.name.asString() == expectedName }

        return handler?.let { context.getBrsName(it) }
    }

    /**
     * Extract exported functions.
     */
    private fun extractExports(irClass: IrClass): List<BrsExportInfo> {
        val exports = mutableListOf<BrsExportInfo>()

        for (function in irClass.declarations.filterIsInstance<IrSimpleFunction>()) {
            val exportAnnotation = findAnnotation(function, "BrsExport")
            if (exportAnnotation != null) {
                val name = getAnnotationStringArg(exportAnnotation, "name")
                    ?: function.name.asString()

                exports.add(BrsExportInfo(
                    name = name,
                    irFunction = function
                ))
            }
        }

        return exports
    }

    /**
     * Map Kotlin IR type to BrightScript field type string.
     */
    private fun mapTypeToFieldType(type: IrType?): String {
        if (type == null) return BrsFieldTypes.ASSOC_ARRAY

        return when {
            type.isInt() || type.isShort() || type.isByte() -> BrsFieldTypes.INTEGER
            type.isLong() -> BrsFieldTypes.LONG_INTEGER
            type.isFloat() -> BrsFieldTypes.FLOAT
            type.isDouble() -> BrsFieldTypes.DOUBLE
            type.isBoolean() -> BrsFieldTypes.BOOLEAN
            type.isString() -> BrsFieldTypes.STRING
            type.isArray() -> BrsFieldTypes.ARRAY
            isNodeType(type) -> BrsFieldTypes.NODE
            else -> BrsFieldTypes.ASSOC_ARRAY
        }
    }

    /**
     * Check if a type represents a SceneGraph node.
     */
    private fun isNodeType(type: IrType): Boolean {
        val classifier = type.classOrNull?.owner ?: return false

        // Check if it's a component class
        if (isComponent(classifier)) return true

        // Check for Node/Group/etc. in the name
        val name = classifier.name.asString()
        return name in setOf("Node", "Group", "Task", "ContentNode") ||
                name.endsWith("Node")
    }

    // ==================== Annotation Helpers ====================

    /**
     * Find an annotation by name on a declaration.
     */
    private fun findAnnotation(declaration: IrDeclaration, annotationName: String): IrConstructorCall? {
        val annotations = when (declaration) {
            is IrClass -> declaration.annotations
            is IrProperty -> declaration.annotations
            is IrField -> declaration.annotations
            is IrFunction -> declaration.annotations
            else -> return null
        }

        return annotations.find { annotation ->
            val annotationClass = annotation.type.classifierOrNull?.owner as? IrClass
            annotationClass?.name?.asString() == annotationName
        }
    }

    /**
     * Get a string argument from an annotation by index.
     */
    private fun getAnnotationStringArg(annotation: IrConstructorCall, index: Int): String? {
        val arg = annotation.getValueArgument(index) as? IrConst ?: return null
        return arg.value as? String
    }

    /**
     * Get a string argument from an annotation by name.
     */
    private fun getAnnotationStringArg(annotation: IrConstructorCall, name: String): String? {
        val constructor = annotation.symbol.owner
        val paramIndex = constructor.valueParameters.indexOfFirst { it.name.asString() == name }
        if (paramIndex < 0) return null

        val arg = annotation.getValueArgument(paramIndex) as? IrConst ?: return null
        return arg.value as? String
    }

    /**
     * Get a boolean argument from an annotation by name.
     */
    private fun getAnnotationBooleanArg(annotation: IrConstructorCall, name: String): Boolean? {
        val constructor = annotation.symbol.owner
        val paramIndex = constructor.valueParameters.indexOfFirst { it.name.asString() == name }
        if (paramIndex < 0) return null

        val arg = annotation.getValueArgument(paramIndex) as? IrConst ?: return null
        return arg.value as? Boolean
    }
}
