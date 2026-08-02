/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.test

import org.jetbrains.kotlin.cli.brs.K2BrsCompiler
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.common.arguments.K2BrsCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.config.Services
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.fail

/**
 * Base class for BrightScript golden file tests.
 *
 * Golden file tests compile Kotlin source files to BrightScript and compare
 * the output against expected reference files. This catches regressions in
 * the compiler's output.
 *
 * Test data is located in: compiler/testData/codegen/brs/
 *
 * Each test has:
 * - Input: <testname>.kt (Kotlin source)
 * - Expected: <testname>.brs.txt (Expected BrightScript output)
 *
 * To update golden files after intentional changes:
 * ./gradlew :compiler:ir:backend.brightscript:test --tests "*GoldenFile*" -PupdateGoldenFiles=true
 */
abstract class AbstractBrsGoldenFileTest {

    companion object {
        // Tests run from repo root via workingDir = rootDir in build.gradle.kts
        private val TEST_DATA_ROOT = File("compiler/testData/codegen/brs")
        private val STDLIB_KLIB = File("libraries/stdlib/brs-prebuilt/kotlin-stdlib-brs.klib")
        private val UPDATE_GOLDEN_FILES = System.getProperty("kotlin.test.update.golden.files")?.toBoolean() ?: false
    }

    /**
     * Run a golden file test for the given test path relative to TEST_DATA_ROOT.
     *
     * @param testPath Path to the .kt file (e.g., "declarations/simpleFunction.kt")
     */
    protected fun runTest(testPath: String) {
        val inputFile = TEST_DATA_ROOT.resolve(testPath)
        val expectedFile = TEST_DATA_ROOT.resolve(testPath.replace(".kt", ".brs.txt"))

        if (!inputFile.exists()) {
            fail("Input file does not exist: ${inputFile.absolutePath}")
        }

        // Compile Kotlin to BrightScript
        val actualOutput = compileKotlinToBrightScript(inputFile)

        if (UPDATE_GOLDEN_FILES) {
            // Update the golden file
            expectedFile.parentFile?.mkdirs()
            expectedFile.writeText(actualOutput)
            println("Updated golden file: ${expectedFile.absolutePath}")
            return
        }

        if (!expectedFile.exists()) {
            fail("""
                Expected file does not exist: ${expectedFile.absolutePath}

                Run with -PupdateGoldenFiles=true to generate it.

                Actual output:
                $actualOutput
            """.trimIndent())
        }

        val expected = expectedFile.readText().normalizeLineEndings()
        val actual = actualOutput.normalizeLineEndings()

        assertEquals(
            expected,
            actual,
            """
                Golden file mismatch for: $testPath

                Expected file: ${expectedFile.absolutePath}

                Run with -PupdateGoldenFiles=true to update the golden file if this change is intentional.
            """.trimIndent()
        )
    }

    /**
     * Compile a Kotlin file to BrightScript and return the output as a string.
     */
    private fun compileKotlinToBrightScript(inputFile: File): String {
        val tempOutputDir = createTempDir("brs-golden-test")
        try {
            val compiler = K2BrsCompiler()
            val arguments = K2BrsCompilerArguments().apply {
                freeArgs = listOf(inputFile.absolutePath)
                outputDir = tempOutputDir.absolutePath
                // Include stdlib so coroutine symbols and other builtins are available
                if (STDLIB_KLIB.exists()) {
                    libraries = STDLIB_KLIB.absolutePath
                }
            }

            // Capture compiler messages
            val messages = mutableListOf<String>()
            val messageCollector = object : MessageCollector {
                override fun clear() { messages.clear() }
                override fun hasErrors(): Boolean = messages.any { it.startsWith("ERROR") }
                override fun report(
                    severity: CompilerMessageSeverity,
                    message: String,
                    location: CompilerMessageSourceLocation?
                ) {
                    val loc = location?.let { "${it.path}:${it.line}:${it.column}: " } ?: ""
                    messages.add("$severity: $loc$message")
                }
            }

            val exitCode = compiler.exec(messageCollector, Services.EMPTY, arguments)

            if (exitCode != ExitCode.OK) {
                fail("""
                    Compilation failed for ${inputFile.name}: $exitCode

                    Messages:
                    ${messages.joinToString("\n")}
                """.trimIndent())
            }

            // Collect all .brs, .xml and .deps.json files from output directory.
            // The .deps.json capture locks the component dependency manifests (the
            // input to KGP <script> injection) into the goldens alongside the XML.
            val outputFiles = tempOutputDir.walkTopDown()
                .filter { it.extension in setOf("brs", "xml") || it.name.endsWith(".deps.json") }
                .sortedBy { it.name }
                .toList()

            return if (outputFiles.isEmpty()) {
                "// No BrightScript output generated"
            } else {
                outputFiles.joinToString("\n\n") { outputFile ->
                    "// --- File: ${outputFile.name} ---\n${outputFile.readText()}"
                }
            }
        } finally {
            tempOutputDir.deleteRecursively()
        }
    }

    /**
     * Create a temporary directory for test output.
     */
    private fun createTempDir(prefix: String): File {
        return File.createTempFile(prefix, "").apply {
            delete()
            mkdirs()
        }
    }

    /**
     * Normalize line endings for cross-platform comparison.
     */
    private fun String.normalizeLineEndings(): String {
        return this.replace("\r\n", "\n").replace("\r", "\n").trim()
    }
}
