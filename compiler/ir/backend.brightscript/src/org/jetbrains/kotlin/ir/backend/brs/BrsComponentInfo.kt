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
 * Task-thread entry point emitted for concrete TaskComponent subclasses.
 * Referenced by the generated init() (`m.top.functionName = ...`), the
 * component XML (`<function name=...>`), and the wrapper sub itself.
 */
const val KOTLIN_TASK_MAIN_FUNCTION_NAME = "__kotlinTaskMain"

/** Bare-named lifecycle entries emitted for every concrete render component (callFunc targets of retire/revive). */
const val KOTLIN_RETIRE_FUNCTION_NAME = "__kotlinRetire"
const val KOTLIN_REVIVE_FUNCTION_NAME = "__kotlinRevive"

/** The compiler-synthesized onStart driver member (BrsComponentLifecycleLowering). */
const val KOTLIN_START_DRIVER_NAME = "__kotlinStartDriver"

/**
 * TaskComponent completion-protocol interface fields (declared in the stdlib
 * kotlin.brs.TaskComponent and inherited into every user task's XML).
 * The wrapper writes the error AA before the state, and the state LAST.
 */
const val KOTLIN_TASK_STATE_FIELD = "kotlinTaskState"
const val KOTLIN_TASK_ERROR_FIELD = "kotlinTaskError"

/**
 * m-scope slot holding a scope-owner component's lowered run{}-block binding
 * table (request name → lifted function pointer), assigned in generated
 * init() and handed to the stdlib's per-component holder via
 * `__kotlinScopeBindingsInstall` (which is what owner dispatch reads).
 */
const val KOTLIN_SCOPE_BINDINGS_FIELD = "__kotlinScopeBindings"

/**
 * Information about a SceneGraph node entry extracted from the DSL.
 *
 * This represents a single node in the component's children hierarchy.
 */
data class NodeEntryInfo(
    /**
     * The SceneGraph node type (e.g., "Label", "LayoutGroup", "Button").
     */
    val nodeType: String,

    /**
     * The unique ID for this node within the component.
     */
    val id: String,

    /**
     * XML attributes for this node (key-value pairs).
     */
    val attributes: Map<String, String>,

    /**
     * Child nodes within this node.
     */
    val children: List<NodeEntryInfo>
) {
    /**
     * Recursively collects all node IDs from this node and its descendants.
     */
    fun allNodeIds(): List<String> = listOf(id) + children.flatMap { it.allNodeIds() }
}

/**
 * Information about an interface field extracted from interfaceField() calls.
 *
 * Fields can either alias a child node's field or be standalone fields.
 */
data class InterfaceFieldInfo(
    /**
     * The field name exposed on this component's interface.
     */
    val name: String,

    /**
     * The path to the child node's field (e.g., "buttonId.buttonSelected"),
     * or null for standalone fields.
     */
    val alias: String? = null,

    /**
     * The field type (default: "node").
     */
    val type: String = "node",

    /**
     * The default value for the field (optional).
     */
    val value: String? = null,

    /**
     * The callback function name to invoke when the field changes (optional).
     */
    val onChange: String? = null,

    /**
     * Whether to notify observers even when the value is unchanged (default: false).
     */
    val alwaysNotify: Boolean = false
)

/**
 * Information about a layout definition extracted from @SGLayout property.
 */
data class BrsLayoutInfo(
    /**
     * The property name (e.g., "layout").
     */
    val propertyName: String,

    /**
     * The top-level nodes declared in the layout DSL.
     */
    val nodes: List<NodeEntryInfo>,

    /**
     * Flattened list of all node IDs for accessor generation.
     */
    val allNodeIds: List<String>,

    /**
     * Interface field aliases declared via interfaceField() in the layout DSL.
     */
    val interfaceFields: List<InterfaceFieldInfo> = emptyList()
)

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
    val additionalScripts: List<String> = emptyList(),

    /**
     * Layout information extracted from @SGLayout property, if present.
     * Contains the DSL-declared node hierarchy for XML <children> generation.
     */
    val layout: BrsLayoutInfo? = null,

    /**
     * Constructor-parameter @SG properties — the component's REQUIRED INPUTS
     * (spec 2026-09-04-component-lifecycle §3), in declaration order. Non-empty
     * iff the type also declares the boolean ready-marker field
     * [LayoutInputValidation.READY_MARKER_ATTRIBUTE]; consumed by the
     * post-extraction static-layout validation in BrsCompiler (§5.9c).
     */
    val requiredInputs: List<String> = emptyList()
)

/** One static-layout finding: [ownerComponent]'s layout declares [childType] at [childId] without constant [missingInput]. */
data class LayoutInputFinding(val ownerComponent: String, val childId: String, val childType: String, val missingInput: String) {
    fun message(): String =
        "[BRS layout] component '$childType' (id=\"$childId\") declared in $ownerComponent's layout requires input " +
            "'$missingInput' as a compile-time constant — supply it in the builder call (or attr(\"$missingInput\", …)), " +
            "or construct $childType in code"
}

/**
 * Static-layout input validation (spec §5.9c). Pure over the extracted model so
 * it is unit-testable without IR: [requiredByType] maps a module component's
 * name to its constructor-input names; a child whose type is not in the map
 * (a SceneGraph built-in or a dependency-klib component) is never checked.
 */
object LayoutInputValidation {
    /**
     * The boolean interface field every input-bearing component type declares
     * (default false), AND the XML attribute a static layout sets to "true" on a
     * child whose required inputs are all present as constants — so the lifecycle
     * gate opens at creation for statically-declared children.
     */
    const val READY_MARKER_ATTRIBUTE = "__kotlinInputsReady"

    /** Every (child, required input) pair the layout under [owner] leaves without a constant value — one finding each. */
    fun validate(owner: String, nodes: List<NodeEntryInfo>, requiredByType: Map<String, List<String>>): List<LayoutInputFinding> {
        val findings = mutableListOf<LayoutInputFinding>()
        for (node in nodes) {
            val required = requiredByType[node.nodeType]
            if (required != null) {
                for (input in required) {
                    if (!node.attributes.containsKey(input)) {
                        findings.add(LayoutInputFinding(owner, node.id, node.nodeType, input))
                    }
                }
            }
            findings.addAll(validate(owner, node.children, requiredByType))
        }
        return findings
    }

    /** Adds `__kotlinInputsReady="true"` to every input-bearing child whose required inputs are all present. */
    fun withInputMarkers(nodes: List<NodeEntryInfo>, requiredByType: Map<String, List<String>>): List<NodeEntryInfo> =
        nodes.map { node ->
            val required = requiredByType[node.nodeType]
            val satisfied = required != null && required.isNotEmpty() && required.all { node.attributes.containsKey(it) }
            val attributes = if (satisfied) node.attributes + (READY_MARKER_ATTRIBUTE to "true") else node.attributes
            node.copy(attributes = attributes, children = withInputMarkers(node.children, requiredByType))
        }
}

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
     * For "node"-typed fields: the specific node type accepted by the field
     * (from @SGNodeField(nodeType = ...)), or null to accept any node.
     */
    val nodeType: String? = null,

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
