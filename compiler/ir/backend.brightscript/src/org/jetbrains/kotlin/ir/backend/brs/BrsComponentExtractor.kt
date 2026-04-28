/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs

import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.isInterface
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

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
        val layout = extractLayout(irClass)

        // Merge interface fields from layout into the fields list
        val allFields = mergeInterfaceFields(fields, layout)

        return BrsComponentInfo(
            irClass = irClass,
            name = name,
            extendsComponent = extendsComponent,
            fields = allFields,
            exports = exports,
            layout = layout
        )
    }

    /**
     * Merge interface fields from layout DSL into the component's field list.
     *
     * Interface fields declared via `interfaceField()` in the layout become
     * BrsFieldInfo entries with the alias property set.
     */
    private fun mergeInterfaceFields(fields: List<BrsFieldInfo>, layout: BrsLayoutInfo?): List<BrsFieldInfo> {
        if (layout == null || layout.interfaceFields.isEmpty()) return fields

        val interfaceFieldInfos = layout.interfaceFields.map { fieldInfo ->
            BrsFieldInfo(
                name = fieldInfo.name,
                type = fieldInfo.type,
                alias = fieldInfo.alias,
                defaultValue = fieldInfo.value,
                onChange = fieldInfo.onChange,
                alwaysNotify = fieldInfo.alwaysNotify
            )
        }

        return fields + interfaceFieldInfos
    }

    /**
     * Check if a class is a SceneGraph component.
     *
     * A class is a component if it has @BrsComponent annotation OR extends
     * a class with @BrsSceneGraphComponent (e.g., GroupComponent, SceneComponent, TaskComponent).
     */
    fun isComponent(irClass: IrClass): Boolean {
        // Explicit @BrsComponent annotation
        if (findAnnotation(irClass, "BrsComponent") != null) return true

        // Classes extending GroupComponent/SceneComponent/TaskComponent/etc.
        return hasSceneGraphComponentInHierarchy(irClass)
    }

    /**
     * Check if a class inherits from a SceneGraph component base class.
     *
     * Note: We check the superclass hierarchy, but NOT the class itself.
     * The base classes (GroupComponent, SceneComponent, etc.) have @BrsSceneGraphComponent
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
     * Collect all declarations of type [T] from the BFS-traversed supertype hierarchy of [irClass].
     *
     * Starts from the direct supertypes (not the class itself) and walks up using a visited set
     * to handle diamond hierarchies. Mirrors the BFS shape of [hasSceneGraphComponentInHierarchy].
     */
    private inline fun <reified T : IrDeclaration> collectInheritedDeclarations(
        irClass: IrClass
    ): List<T> {
        val visited = mutableSetOf<IrClass>()
        val result = mutableListOf<T>()
        val queue = ArrayDeque<IrClass>()
        for (superType in irClass.superTypes) {
            val superClass = superType.classOrNull?.owner ?: continue
            queue.add(superClass)
        }
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            if (current in visited) continue
            visited.add(current)
            for (decl in current.declarations) if (decl is T) result.add(decl)
            for (superType in current.superTypes) {
                val superClass = superType.classOrNull?.owner ?: continue
                queue.add(superClass)
            }
        }
        return result
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

        // Inherited properties — subclass declaration wins (already in list), so skip if name present
        for (property in collectInheritedDeclarations<IrProperty>(irClass)) {
            if (fields.any { it.name == property.name.asString() }) continue
            val typeSafeField = extractTypeSafeField(property)
            if (typeSafeField != null) {
                fields.add(typeSafeField)
                continue
            }
            val fieldAnnotation = findAnnotation(property, "BrsField")
            if (fieldAnnotation != null) fields.add(extractFieldFromProperty(property, fieldAnnotation))
        }

        // Inherited fields (IrField) — same override semantics
        for (field in collectInheritedDeclarations<IrField>(irClass)) {
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
     * 1. Explicit @BrsOnChange("handlerName") annotation - looks up the function by Kotlin name,
     *    falling back to the inherited supertype hierarchy via BFS.
     * 2. Convention: on{PropertyName}Changed - auto-detected from class declarations,
     *    falling back to the inherited supertype hierarchy via BFS.
     *
     * @return The mangled BrightScript function name, or null if no handler is found.
     */
    private fun findOnChangeHandler(property: IrProperty): String? {
        val parentClass = property.parent as? IrClass ?: return null

        // Check @BrsOnChange annotation
        val onChangeAnnotation = findAnnotation(property, "BrsOnChange")
        if (onChangeAnnotation != null) {
            val handlerName = getAnnotationStringArg(onChangeAnnotation, 0) ?: return null
            // Look up the function by Kotlin name — local first, then inherited
            val handler = parentClass.declarations
                .filterIsInstance<IrSimpleFunction>()
                .find { it.name.asString() == handlerName }
                ?: collectInheritedDeclarations<IrSimpleFunction>(parentClass)
                    .find { it.name.asString() == handlerName }
            return handler?.let { context.getBrsName(it) }
        }

        // Auto-convention: on{PropertyName}Changed — local first, then inherited
        val expectedName = "on${property.name.asString().replaceFirstChar { it.uppercase() }}Changed"
        val handler = parentClass.declarations
            .filterIsInstance<IrSimpleFunction>()
            .find { it.name.asString() == expectedName }
            ?: collectInheritedDeclarations<IrSimpleFunction>(parentClass)
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

        // Inherited exports — subclass-declared export with same name wins (already in list)
        for (function in collectInheritedDeclarations<IrSimpleFunction>(irClass)) {
            val exportAnnotation = findAnnotation(function, "BrsExport")
            if (exportAnnotation != null) {
                val name = getAnnotationStringArg(exportAnnotation, "name") ?: function.name.asString()
                if (exports.none { it.name == name }) {
                    exports.add(BrsExportInfo(name = name, irFunction = function))
                }
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

    // ==================== Layout Extraction ====================

    /**
     * Extract layout information from a companion function annotated with @SGLayout.
     *
     * Pattern (companion function with marker annotation):
     * ```kotlin
     * companion object {
     *     @SGLayout
     *     fun defineLayout() = sceneLayout {
     *         interfaceField("buttonSelected", alias = "incrementButton.buttonSelected")
     *         button(id = "myButton")
     *         label(id = "myLabel")
     *     }
     * }
     * ```
     *
     * Node IDs are extracted from the DSL body, not from annotation parameters.
     *
     * @return BrsLayoutInfo if the class has an @SGLayout function, null otherwise.
     */
    private fun extractLayout(irClass: IrClass): BrsLayoutInfo? {
        // Find companion object
        val companion = irClass.declarations
            .filterIsInstance<IrClass>()
            .find { it.isCompanion }
            ?: return null

        // Find function with @SGLayout annotation in companion
        val layoutFunction = companion.declarations
            .filterIsInstance<IrSimpleFunction>()
            .find { findAnnotation(it, "SGLayout") != null }
            ?: return null

        // Extract the sceneLayout { } call from the function body
        val (nodes, interfaceFields) = extractLayoutNodesAndFieldsFromFunction(layoutFunction)
        if (nodes.isEmpty() && interfaceFields.isEmpty()) return null

        // Derive all node IDs from the nodes tree
        val allNodeIds = nodes.flatMap { it.allNodeIds() }

        return BrsLayoutInfo(
            propertyName = "layout",  // Standard name for the layout accessor
            nodes = nodes,
            allNodeIds = allNodeIds,
            interfaceFields = interfaceFields
        )
    }

    /**
     * Extract nodes from a layout function's body.
     */
    private fun extractLayoutNodesFromFunction(function: IrSimpleFunction): List<NodeEntryInfo> {
        val (nodes, _) = extractLayoutNodesAndFieldsFromFunction(function)
        return nodes
    }

    /**
     * Extract nodes and interface fields from a layout function's body.
     *
     * @return Pair of (nodes, interfaceFields)
     */
    private fun extractLayoutNodesAndFieldsFromFunction(function: IrSimpleFunction): Pair<List<NodeEntryInfo>, List<InterfaceFieldInfo>> {
        // Check for expression body (single expression function) first
        // e.g., fun defineLayout() = sceneLayout { ... }
        val expressionBody = function.body as? IrExpressionBody
        if (expressionBody != null) {
            val sceneLayoutCall = expressionBody.expression as? IrCall ?: return Pair(emptyList(), emptyList())
            val calleeName = sceneLayoutCall.symbol.owner.name.asString()
            if (calleeName != "sceneLayout") return Pair(emptyList(), emptyList())

            val lambdaArg = sceneLayoutCall.getValueArgument(sceneLayoutCall.valueArgumentsCount - 1)
            val lambda = extractLambdaBody(lambdaArg) ?: return Pair(emptyList(), emptyList())

            return extractNodesAndFieldsFromLambda(lambda)
        }

        // Check for block body with return statement
        // e.g., fun defineLayout(): SceneLayout { return sceneLayout { ... } }
        // Note: K2 compiles expression body functions to block body with return
        val blockBody = function.body as? IrBlockBody ?: return Pair(emptyList(), emptyList())

        // Find the return expression containing sceneLayout { } call
        for (statement in blockBody.statements) {
            val returnExpr = statement as? IrReturn ?: continue
            val sceneLayoutCall = returnExpr.value as? IrCall ?: continue
            val calleeName = sceneLayoutCall.symbol.owner.name.asString()
            if (calleeName != "sceneLayout") continue

            // The lambda is the last argument
            val lambdaArg = sceneLayoutCall.getValueArgument(sceneLayoutCall.valueArgumentsCount - 1)
            val lambda = extractLambdaBody(lambdaArg) ?: continue

            return extractNodesAndFieldsFromLambda(lambda)
        }

        return Pair(emptyList(), emptyList())
    }

    /**
     * Extract the lambda body from a function argument.
     */
    private fun extractLambdaBody(arg: IrExpression?): IrBlockBody? {
        return when (arg) {
            is IrFunctionExpression -> arg.function.body as? IrBlockBody
            is IrBlock -> {
                // Lambda may be wrapped in a block
                val functionRef = arg.statements.filterIsInstance<IrFunctionReference>().firstOrNull()
                val function = functionRef?.symbol?.owner ?: return null
                function.body as? IrBlockBody
            }
            else -> null
        }
    }

    /**
     * Extract node entries from a lambda body.
     */
    private fun extractNodesFromLambda(body: IrBlockBody): List<NodeEntryInfo> {
        val nodes = mutableListOf<NodeEntryInfo>()
        extractNodesFromStatements(body.statements, nodes)
        return nodes
    }

    /**
     * Extract nodes and interface fields from a lambda body.
     *
     * @return Pair of (nodes, interfaceFields)
     */
    private fun extractNodesAndFieldsFromLambda(body: IrBlockBody): Pair<List<NodeEntryInfo>, List<InterfaceFieldInfo>> {
        val nodes = mutableListOf<NodeEntryInfo>()
        val interfaceFields = mutableListOf<InterfaceFieldInfo>()
        extractNodesAndFieldsFromStatements(body.statements, nodes, interfaceFields)
        return Pair(nodes, interfaceFields)
    }

    /**
     * Recursively extract node entries and interface fields from statements.
     * Handles IrBlock wrappers that K2 generates around DSL calls.
     */
    private fun extractNodesAndFieldsFromStatements(
        statements: List<IrStatement>,
        nodes: MutableList<NodeEntryInfo>,
        interfaceFields: MutableList<InterfaceFieldInfo>
    ) {
        for (statement in statements) {
            // Handle IrBlock - the K2 compiler may wrap DSL calls in blocks
            if (statement is IrBlock) {
                extractNodesAndFieldsFromStatements(statement.statements, nodes, interfaceFields)
                continue
            }

            // Check if this is an interfaceField() call
            val call = statement as? IrCall
            if (call != null) {
                val functionName = call.symbol.owner.name.asString()
                if (functionName == "interfaceField") {
                    val fieldInfo = extractInterfaceFieldFromCall(call)
                    if (fieldInfo != null) {
                        interfaceFields.add(fieldInfo)
                    }
                    continue
                }
            }

            // Otherwise try to extract as a node
            val node = extractNodeFromStatement(statement)
            if (node != null) {
                nodes.add(node)
            }
        }
    }

    /**
     * Extract interface field info from an interfaceField() call.
     *
     * interfaceField(
     *     name: String,
     *     alias: String? = null,
     *     type: InterfaceFieldType = InterfaceFieldType.NODE,
     *     value: String? = null,
     *     onChange: String? = null,
     *     alwaysNotify: Boolean = false
     * )
     */
    private fun extractInterfaceFieldFromCall(call: IrCall): InterfaceFieldInfo? {
        val function = call.symbol.owner

        // Extract 'name' (first argument)
        val nameArg = call.getValueArgument(0) as? IrConst ?: return null
        val name = nameArg.value as? String ?: return null

        // Extract 'alias' (second argument, optional)
        var alias: String? = null
        val aliasArg = call.getValueArgument(1) as? IrConst
        if (aliasArg != null) {
            alias = aliasArg.value as? String
        }

        // Extract 'type' (third argument) - enum with brsType property
        var type = "node"
        val typeArg = call.getValueArgument(2)
        if (typeArg != null) {
            // Type argument is an IrGetEnumValue for InterfaceFieldType
            val enumValue = typeArg as? IrGetEnumValue
            if (enumValue != null) {
                // Map enum name to brsType string
                type = mapInterfaceFieldTypeEnumToBrsType(enumValue.symbol.owner.name.asString())
            }
        }

        // Extract 'value' (fourth argument, optional)
        var value: String? = null
        val valueArg = call.getValueArgument(3) as? IrConst
        if (valueArg != null) {
            value = valueArg.value as? String
        }

        // Extract 'onChange' (fifth argument, optional)
        var onChange: String? = null
        val onChangeArg = call.getValueArgument(4) as? IrConst
        if (onChangeArg != null) {
            onChange = onChangeArg.value as? String
        }

        // Extract 'alwaysNotify' (sixth argument, optional with default false)
        var alwaysNotify = false
        val alwaysNotifyArg = call.getValueArgument(5) as? IrConst
        if (alwaysNotifyArg != null) {
            alwaysNotify = alwaysNotifyArg.value as? Boolean ?: false
        }

        return InterfaceFieldInfo(
            name = name,
            alias = alias,
            type = type,
            value = value,
            onChange = onChange,
            alwaysNotify = alwaysNotify
        )
    }

    /**
     * Map InterfaceFieldType enum name to its BrightScript type string.
     */
    private fun mapInterfaceFieldTypeEnumToBrsType(enumName: String): String {
        return when (enumName) {
            // Scalar types
            "STRING" -> "string"
            "INTEGER" -> "integer"
            "LONG_INTEGER" -> "longinteger"
            "FLOAT" -> "float"
            "DOUBLE" -> "double"
            "BOOLEAN" -> "boolean"
            "COLOR" -> "color"
            "TIME" -> "time"
            "URI" -> "uri"
            "NODE" -> "node"
            "VECTOR_2D" -> "vector2d"
            "RECT_2D" -> "rect2D"
            "ASSOC_ARRAY" -> "assocarray"
            "ARRAY" -> "array"
            // Array types
            "INT_ARRAY" -> "intarray"
            "FLOAT_ARRAY" -> "floatarray"
            "BOOL_ARRAY" -> "boolarray"
            "STRING_ARRAY" -> "stringarray"
            "COLOR_ARRAY" -> "colorarray"
            "TIME_ARRAY" -> "timearray"
            "VECTOR_2D_ARRAY" -> "vector2darray"
            "RECT_2D_ARRAY" -> "rect2DArray"
            "NODE_ARRAY" -> "nodearray"
            else -> "node" // Default fallback
        }
    }

    /**
     * Recursively extract node entries from statements.
     * Handles IrBlock wrappers that K2 generates around DSL calls.
     */
    private fun extractNodesFromStatements(statements: List<IrStatement>, nodes: MutableList<NodeEntryInfo>) {
        for (statement in statements) {
            // Handle IrBlock - the K2 compiler may wrap DSL calls in blocks
            if (statement is IrBlock) {
                extractNodesFromStatements(statement.statements, nodes)
                continue
            }
            val node = extractNodeFromStatement(statement)
            if (node != null) {
                nodes.add(node)
            }
        }
    }

    /**
     * Extract a node entry from a statement (expected to be a builder method call).
     */
    private fun extractNodeFromStatement(statement: IrStatement): NodeEntryInfo? {
        val call = statement as? IrCall ?: return null
        val functionName = call.symbol.owner.name.asString()

        // Handle custom component() calls specially
        if (functionName == "component") {
            return extractCustomComponentNode(call)
        }

        // Map DSL method names to SceneGraph node types
        val nodeType = mapBuilderMethodToNodeType(functionName) ?: return null

        // Extract the ID argument (always the first argument)
        val id = extractIdArgument(call) ?: return null

        // Extract other attributes based on the node type
        val attributes = extractNodeAttributes(call, nodeType)

        // Extract children if this is a container node
        val children = extractChildNodes(call)

        return NodeEntryInfo(
            nodeType = nodeType,
            id = id,
            attributes = attributes,
            children = children
        )
    }

    /**
     * Extract a custom component node from a component() call.
     *
     * The component() method has signature:
     * component(componentType: String, id: String, ..., init: ComponentBuilder.() -> Unit)
     *
     * The ComponentBuilder lambda contains attr() calls for custom attributes
     * and optionally children {} blocks.
     */
    private fun extractCustomComponentNode(call: IrCall): NodeEntryInfo? {
        val function = call.symbol.owner

        // Extract componentType (first argument)
        val componentTypeArg = call.getValueArgument(0) as? IrConst ?: return null
        val componentType = componentTypeArg.value as? String ?: return null

        // Extract id (second argument)
        val idArg = call.getValueArgument(1) as? IrConst ?: return null
        val id = idArg.value as? String ?: return null

        // Extract standard node attributes (translation, rotation, etc.)
        val attributes = mutableMapOf<String, String>()
        for (i in 2 until call.valueArgumentsCount) {
            val param = function.valueParameters.getOrNull(i) ?: continue
            val paramName = param.name.asString()
            val arg = call.getValueArgument(i) ?: continue

            // Skip the 'init' lambda
            if (paramName == "init") continue

            val value = extractAttributeValue(arg, paramName)
            if (value != null) {
                val xmlAttrName = mapParamToXmlAttribute(paramName)
                attributes[xmlAttrName] = value
            }
        }

        // Extract custom attributes and children from the ComponentBuilder lambda
        val initParamIndex = function.valueParameters.indexOfFirst { it.name.asString() == "init" }
        val customAttributes = mutableMapOf<String, String>()
        var children = emptyList<NodeEntryInfo>()

        if (initParamIndex >= 0) {
            val lambdaArg = call.getValueArgument(initParamIndex)
            val lambdaBody = extractLambdaBody(lambdaArg)
            if (lambdaBody != null) {
                val (attrs, childNodes) = extractComponentBuilderCalls(lambdaBody)
                customAttributes.putAll(attrs)
                children = childNodes
            }
        }

        // Merge custom attributes into attributes map
        attributes.putAll(customAttributes)

        return NodeEntryInfo(
            nodeType = componentType,
            id = id,
            attributes = attributes,
            children = children
        )
    }

    /**
     * Extract attr() calls and children {} blocks from a ComponentBuilder lambda body.
     *
     * @return Pair of (custom attributes map, list of child nodes)
     */
    private fun extractComponentBuilderCalls(body: IrBlockBody): Pair<Map<String, String>, List<NodeEntryInfo>> {
        val attributes = mutableMapOf<String, String>()
        var children = emptyList<NodeEntryInfo>()

        for (statement in body.statements) {
            // Handle IrBlock wrappers
            if (statement is IrBlock) {
                val (blockAttrs, blockChildren) = extractComponentBuilderCallsFromStatements(statement.statements)
                attributes.putAll(blockAttrs)
                if (blockChildren.isNotEmpty()) {
                    children = blockChildren
                }
                continue
            }

            val call = statement as? IrCall ?: continue
            val functionName = call.symbol.owner.name.asString()

            when (functionName) {
                "attr" -> {
                    // attr(name: String, value: ...)
                    val nameArg = call.getValueArgument(0) as? IrConst ?: continue
                    val name = nameArg.value as? String ?: continue
                    val valueArg = call.getValueArgument(1) ?: continue
                    val value = extractAttrValue(valueArg)
                    if (value != null) {
                        attributes[name] = value
                    }
                }
                "children" -> {
                    // children { ... } - extract child nodes from the lambda
                    val childLambdaArg = call.getValueArgument(0)
                    val childLambdaBody = extractLambdaBody(childLambdaArg)
                    if (childLambdaBody != null) {
                        children = extractNodesFromLambda(childLambdaBody)
                    }
                }
            }
        }

        return Pair(attributes, children)
    }

    /**
     * Helper to extract from a list of statements (for handling IrBlock).
     */
    private fun extractComponentBuilderCallsFromStatements(statements: List<IrStatement>): Pair<Map<String, String>, List<NodeEntryInfo>> {
        val attributes = mutableMapOf<String, String>()
        var children = emptyList<NodeEntryInfo>()

        for (statement in statements) {
            if (statement is IrBlock) {
                val (blockAttrs, blockChildren) = extractComponentBuilderCallsFromStatements(statement.statements)
                attributes.putAll(blockAttrs)
                if (blockChildren.isNotEmpty()) {
                    children = blockChildren
                }
                continue
            }

            val call = statement as? IrCall ?: continue
            val functionName = call.symbol.owner.name.asString()

            when (functionName) {
                "attr" -> {
                    val nameArg = call.getValueArgument(0) as? IrConst ?: continue
                    val name = nameArg.value as? String ?: continue
                    val valueArg = call.getValueArgument(1) ?: continue
                    val value = extractAttrValue(valueArg)
                    if (value != null) {
                        attributes[name] = value
                    }
                }
                "children" -> {
                    val childLambdaArg = call.getValueArgument(0)
                    val childLambdaBody = extractLambdaBody(childLambdaArg)
                    if (childLambdaBody != null) {
                        children = extractNodesFromLambda(childLambdaBody)
                    }
                }
            }
        }

        return Pair(attributes, children)
    }

    /**
     * Extract value from an attr() call's value argument.
     * Handles String, Number, Boolean, Color, Vector2D, Vector4D.
     */
    private fun extractAttrValue(expr: IrExpression): String? {
        return when (expr) {
            is IrConst -> {
                val value = expr.value ?: return null
                when (value) {
                    is String -> if (value.isEmpty()) null else value
                    is Boolean -> if (value) "true" else "false"
                    is Number -> value.toString()
                    else -> value.toString()
                }
            }
            is IrConstructorCall -> extractConstructorValue(expr)
            is IrCall -> {
                // Handle property access like color.hex
                val functionName = expr.symbol.owner.name.asString()
                if (functionName.startsWith("get")) {
                    // Could be a getter - try to extract from the dispatch receiver
                    null
                } else {
                    extractCallValue(expr)
                }
            }
            is IrGetValue -> {
                val owner = expr.symbol.owner
                if (owner is IrVariable) {
                    val initializer = owner.initializer
                    if (initializer != null) {
                        extractAttrValue(initializer)
                    } else null
                } else null
            }
            else -> null
        }
    }

    /**
     * Map DSL builder method names to SceneGraph node types.
     * Returns null for methods that need special handling (like "component").
     */
    private fun mapBuilderMethodToNodeType(methodName: String): String? {
        return when (methodName) {
            "group" -> "Group"
            "layoutGroup" -> "LayoutGroup"
            "label" -> "Label"
            "poster" -> "Poster"
            "rectangle" -> "Rectangle"
            "button" -> "Button"
            "buttonGroup" -> "ButtonGroup"
            "textEditBox" -> "TextEditBox"
            "keyboard" -> "Keyboard"
            // "component" is handled specially - returns null here
            else -> null
        }
    }

    /**
     * Extract the ID argument from a builder method call.
     */
    private fun extractIdArgument(call: IrCall): String? {
        // ID is always the first value argument
        val idArg = call.getValueArgument(0) as? IrConst ?: return null
        return idArg.value as? String
    }

    /**
     * Extract XML attributes from a builder method call.
     */
    private fun extractNodeAttributes(call: IrCall, nodeType: String): Map<String, String> {
        val attributes = mutableMapOf<String, String>()
        val function = call.symbol.owner

        // Iterate through value parameters and arguments
        for (i in 0 until call.valueArgumentsCount) {
            val param = function.valueParameters.getOrNull(i) ?: continue
            val paramName = param.name.asString()
            val arg = call.getValueArgument(i) ?: continue

            // Skip 'id' (already handled) and 'init' (lambda for children)
            if (paramName == "id" || paramName == "init") continue

            val value = extractAttributeValue(arg, paramName)
            if (value != null) {
                val xmlAttrName = mapParamToXmlAttribute(paramName)
                attributes[xmlAttrName] = value
            }
        }

        return attributes
    }

    /**
     * Extract the value of an attribute from an IR expression.
     *
     * K2 (FIR) may introduce temporary variables for named arguments, wrapping
     * the actual values. This method handles both direct values and these
     * variable indirections by tracing through to the initializer.
     */
    private fun extractAttributeValue(expr: IrExpression, paramName: String): String? {
        return when (expr) {
            is IrConst -> formatConstValue(expr, paramName)
            is IrGetEnumValue -> expr.symbol.owner.name.asString()
            is IrConstructorCall -> extractConstructorValue(expr)
            is IrCall -> extractCallValue(expr)
            is IrVararg -> {
                // Handle vararg or list literals
                val elements = expr.elements.mapNotNull { element ->
                    when (element) {
                        is IrConst -> element.value?.toString()
                        else -> null
                    }
                }
                if (elements.isNotEmpty()) "[${elements.joinToString(", ")}]" else null
            }
            // Handle K2's temporary variable indirection for named arguments
            is IrGetValue -> {
                val owner = expr.symbol.owner
                if (owner is IrVariable) {
                    val initializer = owner.initializer
                    if (initializer != null) {
                        // Recursively extract from the initializer
                        extractAttributeValue(initializer, paramName)
                    } else null
                } else null
            }
            else -> null
        }
    }

    /**
     * Format a constant value for XML output.
     */
    private fun formatConstValue(const: IrConst, paramName: String): String? {
        val value = const.value ?: return null
        return when (value) {
            is String -> if (value.isEmpty()) null else value
            is Boolean -> if (value) "true" else "false"
            is Number -> value.toString()
            else -> value.toString()
        }
    }

    /**
     * Extract value from a constructor call (e.g., Vector2D(100, 200)).
     */
    private fun extractConstructorValue(call: IrConstructorCall): String? {
        val className = call.symbol.owner.parentAsClass.name.asString()

        return when (className) {
            "Vector2D" -> {
                val x = (call.getValueArgument(0) as? IrConst)?.value
                val y = (call.getValueArgument(1) as? IrConst)?.value
                if (x != null && y != null) "[$x, $y]" else null
            }
            "Vector4D" -> {
                val x = (call.getValueArgument(0) as? IrConst)?.value
                val y = (call.getValueArgument(1) as? IrConst)?.value
                val w = (call.getValueArgument(2) as? IrConst)?.value
                val h = (call.getValueArgument(3) as? IrConst)?.value
                if (x != null && y != null && w != null && h != null) "[$x, $y, $w, $h]" else null
            }
            "Color" -> {
                val hex = (call.getValueArgument(0) as? IrConst)?.value as? String
                hex
            }
            else -> null
        }
    }

    /**
     * Extract value from a function call (e.g., listOf(10, 20)).
     */
    private fun extractCallValue(call: IrCall): String? {
        val functionName = call.symbol.owner.name.asString()

        return when (functionName) {
            "listOf", "arrayOf", "intArrayOf" -> {
                val elements = mutableListOf<String>()
                for (i in 0 until call.valueArgumentsCount) {
                    val arg = call.getValueArgument(i)
                    when (arg) {
                        is IrConst -> arg.value?.toString()?.let { elements.add(it) }
                        is IrVararg -> {
                            for (element in arg.elements) {
                                (element as? IrConst)?.value?.toString()?.let { elements.add(it) }
                            }
                        }
                        else -> {}
                    }
                }
                if (elements.isNotEmpty()) "[${elements.joinToString(", ")}]" else null
            }
            else -> null
        }
    }

    /**
     * Map Kotlin parameter names to XML attribute names.
     */
    private fun mapParamToXmlAttribute(paramName: String): String {
        // Most parameter names map directly to XML attributes
        // Some need special handling
        return when (paramName) {
            "textFont" -> "textFont"
            "focusedTextColor" -> "focusedTextColor"
            "horizAlign" -> "horizAlign"
            "vertAlign" -> "vertAlign"
            else -> paramName
        }
    }

    /**
     * Extract child nodes from a container builder method call.
     */
    private fun extractChildNodes(call: IrCall): List<NodeEntryInfo> {
        val function = call.symbol.owner

        // Find the 'init' lambda parameter
        val initParamIndex = function.valueParameters.indexOfFirst { it.name.asString() == "init" }
        if (initParamIndex < 0) return emptyList()

        val lambdaArg = call.getValueArgument(initParamIndex) ?: return emptyList()
        val lambdaBody = extractLambdaBody(lambdaArg) ?: return emptyList()

        return extractNodesFromLambda(lambdaBody)
    }
}
