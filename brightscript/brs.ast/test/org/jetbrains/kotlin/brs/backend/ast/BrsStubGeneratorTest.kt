/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.brs.backend.ast

import org.jetbrains.kotlin.ir.backend.brs.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Tests for BrightScript declaration extraction and Kotlin stub generation.
 */
class BrsStubGeneratorTest {

    private val extractor = BrsFileDeclarationExtractor()
    private val stubGenerator = BrsExternalStubGenerator()

    // ==================== Declaration Extraction Tests ====================

    @Test
    fun testExtractFunctionDeclaration() {
        val brsCode = """
            function calculateSum(a as Integer, b as Integer) as Integer
                return a + b
            end function
        """.trimIndent()

        val declarations = extractor.extractFromSource(brsCode, "math.brs")

        assertFalse(declarations.hasErrors)
        assertEquals(1, declarations.functions.size)

        val func = declarations.functions[0]
        assertEquals("calculateSum", func.name)
        assertEquals(2, func.parameters.size)
        assertEquals("a", func.parameters[0].name)
        assertEquals(BrsType.INTEGER, func.parameters[0].type)
        assertEquals(BrsType.INTEGER, func.returnType)
    }

    @Test
    fun testExtractSubDeclaration() {
        val brsCode = """
            sub printMessage(message as String)
                print message
            end sub
        """.trimIndent()

        val declarations = extractor.extractFromSource(brsCode, "utils.brs")

        assertFalse(declarations.hasErrors)
        assertEquals(1, declarations.subs.size)

        val sub = declarations.subs[0]
        assertEquals("printMessage", sub.name)
        assertEquals(1, sub.parameters.size)
        assertEquals("message", sub.parameters[0].name)
        assertEquals(BrsType.STRING, sub.parameters[0].type)
    }

    @Test
    fun testExtractMultipleDeclarations() {
        val brsCode = """
            function add(a as Integer, b as Integer) as Integer
                return a + b
            end function

            function multiply(a as Integer, b as Integer) as Integer
                return a * b
            end function

            sub logResult(result as Integer)
                print "Result: "; result
            end sub
        """.trimIndent()

        val declarations = extractor.extractFromSource(brsCode, "math.brs")

        assertFalse(declarations.hasErrors)
        assertEquals(2, declarations.functions.size)
        assertEquals(1, declarations.subs.size)
        assertEquals(3, declarations.callables.size)
    }

    @Test
    fun testExtractWithDefaultParameters() {
        val brsCode = """
            function formatNumber(value as Float, decimals as Integer = 2) as String
                return str(value)
            end function
        """.trimIndent()

        val declarations = extractor.extractFromSource(brsCode, "format.brs")

        assertFalse(declarations.hasErrors)
        val func = declarations.functions[0]
        assertEquals(2, func.parameters.size)
        assertFalse(func.parameters[0].hasDefault)
        assertTrue(func.parameters[1].hasDefault)
    }

    @Test
    fun testExtractWithDynamicType() {
        val brsCode = """
            function processData(data as Dynamic) as Object
                return data
            end function
        """.trimIndent()

        val declarations = extractor.extractFromSource(brsCode, "data.brs")

        assertFalse(declarations.hasErrors)
        val func = declarations.functions[0]
        assertEquals(BrsType.DYNAMIC, func.parameters[0].type)
        assertEquals(BrsType.OBJECT, func.returnType)
    }

    // ==================== Component XML Extraction Tests ====================

    @Test
    fun testExtractComponentFromXml() {
        val xml = """
            <?xml version="1.0" encoding="utf-8" ?>
            <component name="MyComponent" extends="Group">
                <interface>
                    <field id="title" type="string" />
                    <field id="count" type="integer" />
                    <field id="items" type="array" />
                    <function name="refresh" />
                    <function name="loadMore" />
                </interface>
                <script type="text/brightscript" uri="pkg:/components/MyComponent.brs" />
            </component>
        """.trimIndent()

        val component = extractor.extractComponentFromXml(xml, "MyComponent")

        assertNotNull(component)
        assertEquals("MyComponent", component!!.name)
        assertEquals("Group", component.extendsComponent)
        assertEquals(3, component.fields.size)
        assertEquals(2, component.functions.size)

        // Verify fields
        val titleField = component.fields.find { it.name == "title" }
        assertNotNull(titleField)
        assertEquals(BrsType.STRING, titleField!!.type)

        val countField = component.fields.find { it.name == "count" }
        assertNotNull(countField)
        assertEquals(BrsType.INTEGER, countField!!.type)
    }

    @Test
    fun testExtractTaskComponentFromXml() {
        val xml = """
            <?xml version="1.0" encoding="utf-8" ?>
            <component name="FetchTask" extends="Task">
                <interface>
                    <field id="url" type="string" />
                    <field id="result" type="assocarray" />
                    <field id="error" type="string" />
                </interface>
                <script type="text/brightscript" uri="pkg:/components/FetchTask.brs" />
            </component>
        """.trimIndent()

        val component = extractor.extractComponentFromXml(xml, "FetchTask")

        assertNotNull(component)
        assertEquals("Task", component!!.extendsComponent)
        assertEquals(3, component.fields.size)
    }

    // ==================== Stub Generation Tests ====================

    @Test
    fun testGenerateFunctionStub() {
        val brsCode = """
            function calculateTotal(items as Object) as Float
                total = 0.0
                for each item in items
                    total = total + item.price
                end for
                return total
            end function
        """.trimIndent()

        val declarations = extractor.extractFromSource(brsCode, "calc.brs")
        val stub = stubGenerator.generateStubsForFile(declarations, "com.example.brs")

        assertTrue(stub.contains("package com.example.brs"))
        assertTrue(stub.contains("@BrsExternal"))
        assertTrue(stub.contains("external fun calculateTotal"))
        assertTrue(stub.contains("items: Any"))
        assertTrue(stub.contains("): Float"))
    }

    @Test
    fun testGenerateSubStub() {
        val brsCode = """
            sub logEvent(eventName as String, data as Object)
                print eventName; ": "; data
            end sub
        """.trimIndent()

        val declarations = extractor.extractFromSource(brsCode, "logging.brs")
        val stub = stubGenerator.generateStubsForFile(declarations, "com.example.brs")

        assertTrue(stub.contains("@BrsExternal"))
        assertTrue(stub.contains("external fun logEvent"))
        assertTrue(stub.contains("eventName: String"))
        assertTrue(stub.contains("data: Any"))
        assertFalse(stub.contains("): "))  // Subs don't have return type
    }

    @Test
    fun testGenerateStubWithDefaultParameter() {
        val brsCode = """
            function formatPrice(value as Float, currency as String = "USD") as String
                return currency + str(value)
            end function
        """.trimIndent()

        val declarations = extractor.extractFromSource(brsCode, "format.brs")
        val stub = stubGenerator.generateStubsForFile(declarations, "com.example.brs")

        assertTrue(stub.contains("currency: String = definedExternally"))
    }

    @Test
    fun testGenerateComponentStub() {
        val component = BrsComponentDeclaration(
            name = "MyVideoPlayer",
            extendsComponent = "Video",
            fields = listOf(
                BrsFieldDeclaration("contentUrl", BrsType.STRING, hasOnChange = false),
                BrsFieldDeclaration("isPlaying", BrsType.BOOLEAN, hasOnChange = true)
            ),
            functions = listOf(
                BrsFunctionDeclaration("play", emptyList(), BrsType.VOID),
                BrsFunctionDeclaration("pause", emptyList(), BrsType.VOID)
            )
        )

        val stub = stubGenerator.generateComponentStub(component, "com.example.components")

        assertTrue(stub.contains("@BrsComponent"))
        assertTrue(stub.contains("external class MyVideoPlayer"))
        assertTrue(stub.contains(": Video"))
        assertTrue(stub.contains("var contentUrl: String"))
        assertTrue(stub.contains("var isPlaying: Boolean"))
        assertTrue(stub.contains("@BrsOnChange"))
        assertTrue(stub.contains("@BrsExport"))
        assertTrue(stub.contains("fun play()"))
        assertTrue(stub.contains("fun pause()"))
    }

    @Test
    fun testGenerateProjectStubs() {
        // Simulate multiple files
        val mathCode = """
            function add(a as Integer, b as Integer) as Integer
                return a + b
            end function
        """.trimIndent()

        val utilsCode = """
            sub log(message as String)
                print message
            end sub
        """.trimIndent()

        val mathDeclarations = extractor.extractFromSource(mathCode, "math.brs")
        val utilsDeclarations = extractor.extractFromSource(utilsCode, "utils.brs")

        val projectDeclarations = BrsProjectDeclarations(
            files = listOf(mathDeclarations, utilsDeclarations),
            components = emptyList()
        )

        val stub = stubGenerator.generateStubs(projectDeclarations, "com.example.external")

        assertTrue(stub.contains("package com.example.external"))
        assertTrue(stub.contains("external fun add"))
        assertTrue(stub.contains("external fun log"))
    }

    // ==================== Type Mapping Tests ====================

    @Test
    fun testTypeMapping() {
        val brsCode = """
            function testTypes(
                intVal as Integer,
                longVal as LongInteger,
                floatVal as Float,
                doubleVal as Double,
                strVal as String,
                boolVal as Boolean,
                objVal as Object,
                dynVal as Dynamic
            ) as Boolean
                return true
            end function
        """.trimIndent()

        val declarations = extractor.extractFromSource(brsCode, "types.brs")
        val stub = stubGenerator.generateStubsForFile(declarations, "test")

        assertTrue(stub.contains("intVal: Int"))
        assertTrue(stub.contains("longVal: Long"))
        assertTrue(stub.contains("floatVal: Float"))
        assertTrue(stub.contains("doubleVal: Double"))
        assertTrue(stub.contains("strVal: String"))
        assertTrue(stub.contains("boolVal: Boolean"))
        assertTrue(stub.contains("objVal: Any"))
        assertTrue(stub.contains("dynVal: dynamic"))
        assertTrue(stub.contains("): Boolean"))
    }

    // ==================== Project Scanner Tests ====================

    @Test
    fun testProjectDeclarationLookup() {
        val code1 = """
            function fetchData(url as String) as Object
                return invalid
            end function
        """.trimIndent()

        val code2 = """
            sub processData(data as Object)
                print data
            end sub
        """.trimIndent()

        val decl1 = extractor.extractFromSource(code1, "network.brs")
        val decl2 = extractor.extractFromSource(code2, "processor.brs")

        val project = BrsProjectDeclarations(
            files = listOf(decl1, decl2),
            components = listOf(
                BrsComponentDeclaration(
                    name = "MainScene",
                    extendsComponent = "Scene",
                    fields = emptyList(),
                    functions = emptyList()
                )
            )
        )

        // Test lookup functions
        assertNotNull(project.findFunction("fetchData"))
        assertNull(project.findFunction("nonExistent"))
        assertNotNull(project.findSub("processData"))
        assertNotNull(project.findComponent("MainScene"))
        assertEquals(1, project.allFunctions.size)
        assertEquals(1, project.allSubs.size)
    }

    // ==================== Edge Cases ====================

    @Test
    fun testExtractEmptyFile() {
        val declarations = extractor.extractFromSource("", "empty.brs")

        assertFalse(declarations.hasErrors)
        assertTrue(declarations.functions.isEmpty())
        assertTrue(declarations.subs.isEmpty())
    }

    @Test
    fun testExtractWithParseErrors() {
        val invalidCode = """
            function broken(
                ' Missing closing paren and body
        """.trimIndent()

        val declarations = extractor.extractFromSource(invalidCode, "broken.brs")

        assertTrue(declarations.hasErrors)
    }

    @Test
    fun testExtractFunctionWithNoParameters() {
        val brsCode = """
            function getTimestamp() as Integer
                return 0
            end function
        """.trimIndent()

        val declarations = extractor.extractFromSource(brsCode, "time.brs")

        assertFalse(declarations.hasErrors)
        val func = declarations.functions[0]
        assertEquals("getTimestamp", func.name)
        assertTrue(func.parameters.isEmpty())
    }

    @Test
    fun testExtractFunctionWithNoReturnType() {
        val brsCode = """
            function doSomething()
                print "doing something"
            end function
        """.trimIndent()

        val declarations = extractor.extractFromSource(brsCode, "action.brs")

        assertFalse(declarations.hasErrors)
        val func = declarations.functions[0]
        // When no return type specified, should default to Dynamic
        assertEquals(BrsType.DYNAMIC, func.returnType)
    }
}
