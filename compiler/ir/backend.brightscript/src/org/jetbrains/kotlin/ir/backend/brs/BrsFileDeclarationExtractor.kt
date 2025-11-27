/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs

import org.jetbrains.kotlin.brs.backend.ast.*
import org.jetbrains.kotlin.brs.backend.ast.parser.*
import java.io.File

/**
 * Extracts declarations from BrightScript source files for mixed project support.
 *
 * This allows Kotlin code to reference functions and types defined in .brs files,
 * enabling seamless interop between Kotlin and existing BrightScript code.
 */
class BrsFileDeclarationExtractor {

    /**
     * Extract declarations from a single BrightScript file.
     */
    fun extractFromFile(file: File): BrsFileDeclarations {
        val source = file.readText()
        return extractFromSource(source, file.name)
    }

    /**
     * Extract declarations from BrightScript source code.
     */
    fun extractFromSource(source: String, fileName: String = "unknown.brs"): BrsFileDeclarations {
        val parseResult = parseBrightScript(source)

        if (parseResult.hasErrors) {
            return BrsFileDeclarations(
                fileName = fileName,
                functions = emptyList(),
                subs = emptyList(),
                components = emptyList(),
                errors = parseResult.errors
            )
        }

        val functions = mutableListOf<BrsFunctionDeclaration>()
        val subs = mutableListOf<BrsSubDeclaration>()
        val components = mutableListOf<BrsComponentDeclaration>()

        for (declaration in parseResult.result.declarations) {
            when (declaration) {
                is BrsFunction -> {
                    functions.add(extractFunction(declaration))
                }
                is BrsSub -> {
                    subs.add(extractSub(declaration))
                }
                else -> { /* Other declaration types */ }
            }
        }

        return BrsFileDeclarations(
            fileName = fileName,
            functions = functions,
            subs = subs,
            components = components,
            errors = emptyList()
        )
    }

    /**
     * Extract declarations from a SceneGraph component XML file.
     * Parses the XML to find interface fields and functions.
     */
    fun extractFromComponentXml(xmlFile: File): BrsComponentDeclaration? {
        val content = xmlFile.readText()
        return extractComponentFromXml(content, xmlFile.nameWithoutExtension)
    }

    /**
     * Extract component declaration from XML content.
     */
    fun extractComponentFromXml(xmlContent: String, componentName: String): BrsComponentDeclaration? {
        // Simple XML parsing for component declarations
        // In production, use proper XML parsing

        val extendsMatch = Regex("""extends\s*=\s*"([^"]+)"""").find(xmlContent)
        val extendsComponent = extendsMatch?.groupValues?.get(1) ?: "Group"

        val fields = mutableListOf<BrsFieldDeclaration>()
        val functions = mutableListOf<BrsFunctionDeclaration>()

        // Extract field declarations from <field> tags
        val fieldPattern = Regex(
            """<field\s+id\s*=\s*"([^"]+)"\s+type\s*=\s*"([^"]+)"[^>]*>""",
            RegexOption.IGNORE_CASE
        )
        for (match in fieldPattern.findAll(xmlContent)) {
            val fieldName = match.groupValues[1]
            val fieldType = match.groupValues[2]
            fields.add(BrsFieldDeclaration(
                name = fieldName,
                type = mapXmlTypeToBrsType(fieldType),
                hasOnChange = xmlContent.contains("""onChange\s*=\s*"[^"]*$fieldName""")
            ))
        }

        // Extract function declarations from <function> tags
        val funcPattern = Regex(
            """<function\s+name\s*=\s*"([^"]+)"[^>]*>""",
            RegexOption.IGNORE_CASE
        )
        for (match in funcPattern.findAll(xmlContent)) {
            val funcName = match.groupValues[1]
            // We don't know parameter types from XML alone
            functions.add(BrsFunctionDeclaration(
                name = funcName,
                parameters = emptyList(),
                returnType = BrsType.DYNAMIC
            ))
        }

        return BrsComponentDeclaration(
            name = componentName,
            extendsComponent = extendsComponent,
            fields = fields,
            functions = functions
        )
    }

    private fun extractFunction(func: BrsFunction): BrsFunctionDeclaration {
        return BrsFunctionDeclaration(
            name = func.name,
            parameters = func.parameters.map { param ->
                BrsParameterDeclaration(
                    name = param.name,
                    type = param.type ?: BrsType.DYNAMIC,
                    hasDefault = param.defaultValue != null
                )
            },
            returnType = func.returnType ?: BrsType.DYNAMIC
        )
    }

    private fun extractSub(sub: BrsSub): BrsSubDeclaration {
        return BrsSubDeclaration(
            name = sub.name,
            parameters = sub.parameters.map { param ->
                BrsParameterDeclaration(
                    name = param.name,
                    type = param.type ?: BrsType.DYNAMIC,
                    hasDefault = param.defaultValue != null
                )
            }
        )
    }

    private fun mapXmlTypeToBrsType(xmlType: String): BrsType {
        return when (xmlType.lowercase()) {
            "integer", "int" -> BrsType.INTEGER
            "longinteger", "long" -> BrsType.LONG_INTEGER
            "float" -> BrsType.FLOAT
            "double" -> BrsType.DOUBLE
            "string", "str" -> BrsType.STRING
            "boolean", "bool" -> BrsType.BOOLEAN
            "node" -> BrsType.OBJECT
            "array" -> BrsType.OBJECT
            "assocarray", "associativearray" -> BrsType.OBJECT
            "function" -> BrsType.FUNCTION
            else -> BrsType.DYNAMIC
        }
    }
}

/**
 * All declarations extracted from a BrightScript file.
 */
data class BrsFileDeclarations(
    val fileName: String,
    val functions: List<BrsFunctionDeclaration>,
    val subs: List<BrsSubDeclaration>,
    val components: List<BrsComponentDeclaration>,
    val errors: List<String>
) {
    val hasErrors: Boolean get() = errors.isNotEmpty()

    /**
     * Get all callable declarations (functions and subs).
     */
    val callables: List<BrsCallableDeclaration>
        get() = functions + subs
}

/**
 * Base interface for callable declarations.
 */
sealed interface BrsCallableDeclaration {
    val name: String
    val parameters: List<BrsParameterDeclaration>
}

/**
 * A function declaration extracted from BrightScript source.
 */
data class BrsFunctionDeclaration(
    override val name: String,
    override val parameters: List<BrsParameterDeclaration>,
    val returnType: BrsType
) : BrsCallableDeclaration

/**
 * A sub (procedure) declaration extracted from BrightScript source.
 */
data class BrsSubDeclaration(
    override val name: String,
    override val parameters: List<BrsParameterDeclaration>
) : BrsCallableDeclaration

/**
 * A parameter declaration.
 */
data class BrsParameterDeclaration(
    val name: String,
    val type: BrsType,
    val hasDefault: Boolean = false
)

/**
 * A SceneGraph component declaration.
 */
data class BrsComponentDeclaration(
    val name: String,
    val extendsComponent: String,
    val fields: List<BrsFieldDeclaration>,
    val functions: List<BrsFunctionDeclaration>
)

/**
 * A field declaration in a component interface.
 */
data class BrsFieldDeclaration(
    val name: String,
    val type: BrsType,
    val hasOnChange: Boolean = false
)

/**
 * Scans a project directory for BrightScript files and extracts declarations.
 */
class BrsProjectScanner {

    private val extractor = BrsFileDeclarationExtractor()

    /**
     * Scan a directory for .brs files and extract all declarations.
     */
    fun scanDirectory(directory: File): BrsProjectDeclarations {
        val allDeclarations = mutableListOf<BrsFileDeclarations>()
        val componentDeclarations = mutableListOf<BrsComponentDeclaration>()

        if (!directory.exists() || !directory.isDirectory) {
            return BrsProjectDeclarations(emptyList(), emptyList())
        }

        // Scan for .brs source files
        directory.walkTopDown()
            .filter { it.isFile && it.extension == "brs" }
            .forEach { file ->
                allDeclarations.add(extractor.extractFromFile(file))
            }

        // Scan for component XML files
        directory.walkTopDown()
            .filter { it.isFile && it.extension == "xml" && it.parentFile?.name == "components" }
            .forEach { file ->
                extractor.extractFromComponentXml(file)?.let {
                    componentDeclarations.add(it)
                }
            }

        return BrsProjectDeclarations(allDeclarations, componentDeclarations)
    }
}

/**
 * All declarations from a BrightScript project.
 */
data class BrsProjectDeclarations(
    val files: List<BrsFileDeclarations>,
    val components: List<BrsComponentDeclaration>
) {
    /**
     * Get all functions across all files.
     */
    val allFunctions: List<BrsFunctionDeclaration>
        get() = files.flatMap { it.functions }

    /**
     * Get all subs across all files.
     */
    val allSubs: List<BrsSubDeclaration>
        get() = files.flatMap { it.subs }

    /**
     * Find a function by name.
     */
    fun findFunction(name: String): BrsFunctionDeclaration? {
        return allFunctions.find { it.name.equals(name, ignoreCase = true) }
    }

    /**
     * Find a sub by name.
     */
    fun findSub(name: String): BrsSubDeclaration? {
        return allSubs.find { it.name.equals(name, ignoreCase = true) }
    }

    /**
     * Find a component by name.
     */
    fun findComponent(name: String): BrsComponentDeclaration? {
        return components.find { it.name.equals(name, ignoreCase = true) }
    }
}
