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
        if (!isComponent(irClass)) return null

        val componentAnnotation = findAnnotation(irClass, "BrsComponent")
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
     *
     * A class is a component if it has @BrsComponent annotation OR extends
     * a class with @BrsSceneGraphComponent (e.g., SceneComponent, TaskComponent).
     */
    fun isComponent(irClass: IrClass): Boolean {
        // Explicit @BrsComponent annotation
        if (findAnnotation(irClass, "BrsComponent") != null) return true

        // Classes extending SceneComponent/TaskComponent/SceneNodeComponent
        return hasSceneGraphComponentInHierarchy(irClass)
    }

    /**
     * Check if a class inherits from a SceneGraph component base class.
     *
     * Note: We check the superclass hierarchy, but NOT the class itself.
     * The base classes (SceneComponent, etc.) have @BrsSceneGraphComponent
     * but should not generate their own XML - they're abstract bases.
     */
    private fun hasSceneGraphComponentInHierarchy(irClass: IrClass): Boolean {
        val visited = mutableSetOf<IrClass>()
        val queue = ArrayDeque<IrClass>()

        // Start with supertypes, not the class itself
        for (superType in irClass.superTypes) {
            val superClass = superType.classOrNull?.owner ?: continue
            queue.add(superClass)
        }

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            if (current in visited) continue
            visited.add(current)

            if (findAnnotation(current, "BrsSceneGraphComponent") != null) {
                return true
            }

            for (superType in current.superTypes) {
                val superClass = superType.classOrNull?.owner ?: continue
                queue.add(superClass)
            }
        }
        return false
    }

    /**
     * Get the parent component to extend.
     */
    private fun getExtendsComponent(irClass: IrClass, componentAnnotation: IrConstructorCall?): String {
        // Check @BrsExtends annotation first
        val extendsAnnotation = findAnnotation(irClass, "BrsExtends")
        if (extendsAnnotation != null) {
            val value = getAnnotationStringArg(extendsAnnotation, 0)
            if (value != null) return value
        }

        // Check "extends" parameter on @BrsComponent (if present)
        if (componentAnnotation != null) {
            val extendsArg = getAnnotationStringArg(componentAnnotation, "extends")
            if (extendsArg != null) return extendsArg
        }

        // Check superclass for @BrsSceneGraphComponent
        val superClass = irClass.superTypes
            .mapNotNull { it.classOrNull?.owner }
            .firstOrNull { !it.isInterface && it.name.asString() != "Any" }

        if (superClass != null) {
            // If superclass has @BrsSceneGraphComponent, use its extends value
            val sgComponentAnnotation = findAnnotation(superClass, "BrsSceneGraphComponent")
            if (sgComponentAnnotation != null) {
                val extendsArg = getAnnotationStringArg(sgComponentAnnotation, "extends")
                if (extendsArg != null) return extendsArg
            }

            // Legacy: if superclass is a component, use its name
            if (isComponent(superClass)) {
                return context.getBrsName(superClass)
            }
        }

        // Default to Group
        return "Group"
    }

    /**
     * Extract field definitions from the class.
     */
    private fun extractFields(irClass: IrClass): List<BrsFieldInfo> {
        val fields = mutableListOf<BrsFieldInfo>()

        // Extract from properties - check type-safe annotations first, then legacy @BrsField
        for (property in irClass.declarations.filterIsInstance<IrProperty>()) {
            val typeSafeField = extractTypeSafeField(property)
            if (typeSafeField != null) {
                fields.add(typeSafeField)
                continue
            }

            val fieldAnnotation = findAnnotation(property, "BrsField")
            if (fieldAnnotation != null) {
                fields.add(extractFieldFromProperty(property, fieldAnnotation))
            }
        }

        // Extract from fields with @BrsField (legacy support)
        for (field in irClass.declarations.filterIsInstance<IrField>()) {
            val fieldAnnotation = findAnnotation(field, "BrsField")
            if (fieldAnnotation != null && fields.none { it.name == field.name.asString() }) {
                fields.add(extractFieldFromField(field, fieldAnnotation))
            }
        }

        return fields
    }

    /**
     * Map of type-safe annotation names to their BrightScript field types.
     */
    private val sgFieldAnnotationTypes = mapOf(
        "SGStringField" to BrsFieldTypes.STRING,
        "SGIntegerField" to BrsFieldTypes.INTEGER,
        "SGLongIntegerField" to BrsFieldTypes.LONG_INTEGER,
        "SGFloatField" to BrsFieldTypes.FLOAT,
        "SGDoubleField" to BrsFieldTypes.DOUBLE,
        "SGBooleanField" to BrsFieldTypes.BOOLEAN,
        "SGArrayField" to BrsFieldTypes.ARRAY,
        "SGAssocArrayField" to BrsFieldTypes.ASSOC_ARRAY,
        "SGNodeField" to BrsFieldTypes.NODE,
        "SGFunctionField" to BrsFieldTypes.FUNCTION,
        "SGUriField" to BrsFieldTypes.URI,
        "SGTimeField" to BrsFieldTypes.TIME,
        "SGVector2DField" to BrsFieldTypes.VECTOR_2D,
        "SGColorField" to BrsFieldTypes.COLOR
    )

    /**
     * Extract field info from type-safe SG*Field annotations.
     * Returns null if the property doesn't have a type-safe field annotation.
     */
    private fun extractTypeSafeField(property: IrProperty): BrsFieldInfo? {
        for ((annotationName, brsType) in sgFieldAnnotationTypes) {
            val annotation = findAnnotation(property, annotationName)
            if (annotation != null) {
                return extractFieldFromTypeSafeAnnotation(property, annotation, brsType)
            }
        }
        return null
    }

    /**
     * Extract field info from a type-safe annotation.
     */
    private fun extractFieldFromTypeSafeAnnotation(
        property: IrProperty,
        annotation: IrConstructorCall,
        brsType: String
    ): BrsFieldInfo {
        val name = property.name.asString()
        val alwaysNotify = getAnnotationBooleanArg(annotation, "alwaysNotify") ?: false
        val defaultValue = extractTypedDefaultValue(annotation, brsType)
        val onChange = findOnChangeHandler(property)

        return BrsFieldInfo(
            name = name,
            type = brsType,
            defaultValue = defaultValue,
            onChange = onChange,
            alwaysNotify = alwaysNotify,
            irProperty = property
        )
    }

    /**
     * Extract the default value from a type-safe annotation, formatting it appropriately.
     */
    private fun extractTypedDefaultValue(annotation: IrConstructorCall, brsType: String): String? {
        val constructor = annotation.symbol.owner
        val paramIndex = constructor.valueParameters.indexOfFirst { it.name.asString() == "defaultValue" }
        if (paramIndex < 0) return null

        val arg = annotation.getValueArgument(paramIndex) as? IrConst ?: return null
        val value = arg.value ?: return null

        return when (brsType) {
            BrsFieldTypes.STRING, BrsFieldTypes.URI, BrsFieldTypes.COLOR -> {
                val strValue = value as? String ?: return null
                if (strValue.isEmpty()) null else strValue
            }
            BrsFieldTypes.BOOLEAN -> {
                val boolValue = value as? Boolean ?: return null
                // Only emit if non-default (true)
                if (boolValue) "true" else null
            }
            BrsFieldTypes.INTEGER -> {
                val intValue = value as? Int ?: return null
                if (intValue != 0) intValue.toString() else null
            }
            BrsFieldTypes.LONG_INTEGER -> {
                val longValue = value as? Long ?: return null
                if (longValue != 0L) longValue.toString() else null
            }
            BrsFieldTypes.FLOAT -> {
                val floatValue = value as? Float ?: return null
                if (floatValue != 0.0f) floatValue.toString() else null
            }
            BrsFieldTypes.DOUBLE -> {
                val doubleValue = value as? Double ?: return null
                if (doubleValue != 0.0) doubleValue.toString() else null
            }
            else -> null
        }
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
     *
     * The handler name is resolved to its mangled BrightScript name. This works for:
     * 1. Explicit @BrsOnChange("handlerName") annotation - looks up the function by Kotlin name
     * 2. Convention: on{PropertyName}Changed - auto-detected from class declarations
     *
     * @return The mangled BrightScript function name, or null if no handler is specified.
     */
    private fun findOnChangeHandler(property: IrProperty): String? {
        val parentClass = property.parent as? IrClass ?: return null

        // Check @BrsOnChange annotation
        val onChangeAnnotation = findAnnotation(property, "BrsOnChange")
        if (onChangeAnnotation != null) {
            val handlerName = getAnnotationStringArg(onChangeAnnotation, 0) ?: return null
            // Look up the function by Kotlin name and resolve to mangled BrightScript name
            val handler = parentClass.declarations
                .filterIsInstance<IrSimpleFunction>()
                .find { it.name.asString() == handlerName }
            if (handler != null) {
                return context.getBrsName(handler)
            }
            // Handler not found - this will be caught by FIR checker, but return raw name as fallback
            return handlerName
        }

        // Auto-convention: on{PropertyName}Changed
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
