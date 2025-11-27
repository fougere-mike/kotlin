/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs

import org.jetbrains.kotlin.brs.backend.ast.BrsType

/**
 * Generates Kotlin external stub declarations from BrightScript sources.
 *
 * This enables Kotlin code to call BrightScript functions and use BrightScript
 * components with full type checking and IDE support. The generated stubs use
 * the @BrsExternal annotation to mark them as externally implemented.
 *
 * Example output:
 * ```kotlin
 * @file:Suppress("UNUSED_PARAMETER")
 * package brs.external
 *
 * import kotlin.brs.BrsExternal
 *
 * @BrsExternal
 * external fun myBrsFunction(param1: Int, param2: String): String
 * ```
 */
class BrsExternalStubGenerator {

    /**
     * Generate Kotlin stub source code for a project's BrightScript declarations.
     */
    fun generateStubs(
        projectDeclarations: BrsProjectDeclarations,
        packageName: String = "brs.external"
    ): String {
        val builder = StringBuilder()

        // File header
        builder.appendLine("/*")
        builder.appendLine(" * Auto-generated Kotlin stubs for BrightScript declarations.")
        builder.appendLine(" * DO NOT EDIT - regenerate using the BrightScript stub generator.")
        builder.appendLine(" */")
        builder.appendLine()
        builder.appendLine("@file:Suppress(\"UNUSED_PARAMETER\", \"RedundantVisibilityModifier\")")
        builder.appendLine()
        builder.appendLine("package $packageName")
        builder.appendLine()
        builder.appendLine("import kotlin.brs.*")
        builder.appendLine()

        // Generate function stubs
        for (func in projectDeclarations.allFunctions) {
            generateFunctionStub(func, builder)
            builder.appendLine()
        }

        // Generate sub stubs
        for (sub in projectDeclarations.allSubs) {
            generateSubStub(sub, builder)
            builder.appendLine()
        }

        // Generate component stubs
        for (component in projectDeclarations.components) {
            generateComponentStub(component, builder)
            builder.appendLine()
        }

        return builder.toString()
    }

    /**
     * Generate stub for a single BrightScript file's declarations.
     */
    fun generateStubsForFile(
        fileDeclarations: BrsFileDeclarations,
        packageName: String = "brs.external"
    ): String {
        val builder = StringBuilder()

        // File header
        builder.appendLine("/*")
        builder.appendLine(" * Auto-generated Kotlin stubs for ${fileDeclarations.fileName}")
        builder.appendLine(" */")
        builder.appendLine()
        builder.appendLine("@file:Suppress(\"UNUSED_PARAMETER\", \"RedundantVisibilityModifier\")")
        builder.appendLine()
        builder.appendLine("package $packageName")
        builder.appendLine()
        builder.appendLine("import kotlin.brs.*")
        builder.appendLine()

        // Generate function stubs
        for (func in fileDeclarations.functions) {
            generateFunctionStub(func, builder)
            builder.appendLine()
        }

        // Generate sub stubs
        for (sub in fileDeclarations.subs) {
            generateSubStub(sub, builder)
            builder.appendLine()
        }

        return builder.toString()
    }

    /**
     * Generate a Kotlin stub for a component.
     */
    fun generateComponentStub(
        component: BrsComponentDeclaration,
        packageName: String = "brs.external"
    ): String {
        val builder = StringBuilder()

        builder.appendLine("package $packageName")
        builder.appendLine()
        builder.appendLine("import kotlin.brs.*")
        builder.appendLine()

        generateComponentStub(component, builder)

        return builder.toString()
    }

    private fun generateFunctionStub(func: BrsFunctionDeclaration, builder: StringBuilder) {
        builder.append("@BrsExternal")
        builder.appendLine()
        builder.append("public external fun ${func.name}(")
        builder.append(func.parameters.joinToString(", ") { param ->
            val type = mapBrsTypeToKotlin(param.type)
            if (param.hasDefault) {
                "${param.name}: $type = definedExternally"
            } else {
                "${param.name}: $type"
            }
        })
        builder.append("): ")
        builder.append(mapBrsTypeToKotlin(func.returnType))
    }

    private fun generateSubStub(sub: BrsSubDeclaration, builder: StringBuilder) {
        builder.append("@BrsExternal")
        builder.appendLine()
        builder.append("public external fun ${sub.name}(")
        builder.append(sub.parameters.joinToString(", ") { param ->
            val type = mapBrsTypeToKotlin(param.type)
            if (param.hasDefault) {
                "${param.name}: $type = definedExternally"
            } else {
                "${param.name}: $type"
            }
        })
        builder.append(")")
    }

    private fun generateComponentStub(component: BrsComponentDeclaration, builder: StringBuilder) {
        val parentClass = mapComponentToKotlinType(component.extendsComponent)

        builder.appendLine("/**")
        builder.appendLine(" * SceneGraph component: ${component.name}")
        builder.appendLine(" * Extends: ${component.extendsComponent}")
        builder.appendLine(" */")
        builder.append("@BrsComponent(name = \"${component.name}\", extends = \"${component.extendsComponent}\")")
        builder.appendLine()
        builder.append("public external class ${component.name} : $parentClass {")
        builder.appendLine()

        // Generate fields
        for (field in component.fields) {
            builder.append("    ")
            if (field.hasOnChange) {
                builder.append("@BrsOnChange(\"on${field.name.replaceFirstChar { it.uppercase() }}Changed\") ")
            }
            builder.append("public var ${field.name}: ")
            builder.append(mapBrsTypeToKotlin(field.type))
            builder.appendLine()
        }

        if (component.fields.isNotEmpty() && component.functions.isNotEmpty()) {
            builder.appendLine()
        }

        // Generate interface functions
        for (func in component.functions) {
            builder.append("    @BrsExport")
            builder.appendLine()
            builder.append("    public fun ${func.name}(")
            builder.append(func.parameters.joinToString(", ") { param ->
                "${param.name}: ${mapBrsTypeToKotlin(param.type)}"
            })
            builder.append(")")
            if (func.returnType != BrsType.VOID) {
                builder.append(": ${mapBrsTypeToKotlin(func.returnType)}")
            }
            builder.appendLine()
        }

        builder.appendLine("}")
    }

    private fun mapBrsTypeToKotlin(brsType: BrsType): String {
        return when (brsType) {
            BrsType.INTEGER -> "Int"
            BrsType.LONG_INTEGER -> "Long"
            BrsType.FLOAT -> "Float"
            BrsType.DOUBLE -> "Double"
            BrsType.STRING -> "String"
            BrsType.BOOLEAN -> "Boolean"
            BrsType.VOID -> "Unit"
            BrsType.OBJECT -> "Any"
            BrsType.DYNAMIC -> "dynamic"
            BrsType.FUNCTION -> "Function<*>"
            else -> "Any"
        }
    }

    private fun mapComponentToKotlinType(componentType: String): String {
        return when (componentType.lowercase()) {
            "node" -> "Node"
            "group" -> "Group"
            "task" -> "Task"
            "contentnode" -> "ContentNode"
            "rectangle" -> "Rectangle"
            "label" -> "Label"
            "poster" -> "Poster"
            "video" -> "Video"
            "audio" -> "Audio"
            "timer" -> "Timer"
            "animation" -> "Animation"
            "scrollinggroup" -> "Group"
            "layoutgroup" -> "Group"
            "rowlist" -> "RowList"
            "markupgrid" -> "MarkupGrid"
            else -> componentType
        }
    }
}

/**
 * Represents a "definedExternally" placeholder for default values.
 * This is a compile-time constant used in stub generation.
 */
const val DEFINED_EXTERNALLY_PLACEHOLDER = "definedExternally"

/**
 * Extension function to generate stubs from a project scanner result.
 */
fun BrsProjectDeclarations.generateKotlinStubs(packageName: String = "brs.external"): String {
    return BrsExternalStubGenerator().generateStubs(this, packageName)
}

/**
 * Extension function to generate stubs for a single file's declarations.
 */
fun BrsFileDeclarations.generateKotlinStubs(packageName: String = "brs.external"): String {
    return BrsExternalStubGenerator().generateStubsForFile(this, packageName)
}
