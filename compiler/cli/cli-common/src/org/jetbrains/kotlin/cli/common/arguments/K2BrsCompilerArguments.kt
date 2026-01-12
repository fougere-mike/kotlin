/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.common.arguments

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.config.*

/**
 * Compiler arguments for the Kotlin to BrightScript compiler.
 */
class K2BrsCompilerArguments : CommonKlibBasedCompilerArguments() {
    companion object {
        @JvmStatic
        private val serialVersionUID = 0L
    }

    override val configurator: CommonCompilerArgumentsConfigurator = K2BrsCompilerArgumentsConfigurator()

    @Argument(
        value = "-output-dir",
        valueDescription = "<directory>",
        description = "Destination directory for generated .brs files."
    )
    var outputDir: String? = null
        set(value) {
            checkFrozen()
            field = if (value.isNullOrEmpty()) null else value
        }

    @GradleOption(
        value = DefaultValue.STRING_NULL_DEFAULT,
        gradleInputType = GradleInputTypes.INPUT,
        shouldGenerateDeprecatedKotlinOptions = true,
    )
    @Argument(
        value = "-module-name",
        description = "Name of the generated module."
    )
    var moduleName: String? = null
        set(value) {
            checkFrozen()
            field = if (value.isNullOrEmpty()) null else value
        }

    @Argument(
        value = "-libraries",
        valueDescription = "<path>",
        description = "Paths to Kotlin BrightScript libraries, separated by the system path separator."
    )
    var libraries: String? = null
        set(value) {
            checkFrozen()
            field = if (value.isNullOrEmpty()) null else value
        }

    @GradleOption(
        value = DefaultValue.STRING_NULL_DEFAULT,
        gradleInputType = GradleInputTypes.INPUT,
    )
    @Argument(
        value = "-min-roku-os",
        valueDescription = "{9.0|9.4|10.0|11.0|12.0|13.0}",
        description = "Minimum Roku OS version to target. Affects available language features."
    )
    var minRokuOS: String? = null
        set(value) {
            checkFrozen()
            field = if (value.isNullOrEmpty()) null else value
        }

    @GradleOption(
        value = DefaultValue.BOOLEAN_FALSE_DEFAULT,
        gradleInputType = GradleInputTypes.INPUT,
    )
    @Argument(
        value = "-debug",
        description = "Generate debug information (line numbers, variable names)."
    )
    var debugMode: Boolean = false
        set(value) {
            checkFrozen()
            field = value
        }

    @GradleOption(
        value = DefaultValue.BOOLEAN_FALSE_DEFAULT,
        gradleInputType = GradleInputTypes.INPUT,
    )
    @Argument(
        value = "-generate-xml",
        description = "Generate SceneGraph XML component files."
    )
    var generateXml: Boolean = true
        set(value) {
            checkFrozen()
            field = value
        }

    @GradleOption(
        value = DefaultValue.BOOLEAN_FALSE_DEFAULT,
        gradleInputType = GradleInputTypes.INPUT,
    )
    @Argument(
        value = "-minify",
        description = "Minify generated BrightScript code."
    )
    var minify: Boolean = false
        set(value) {
            checkFrozen()
            field = value
        }

    @Argument(
        value = "-main",
        valueDescription = "<function>",
        description = "Fully qualified name of the main function."
    )
    var mainFunction: String? = null
        set(value) {
            checkFrozen()
            field = if (value.isNullOrEmpty()) null else value
        }

    @Argument(
        value = "-component-extends",
        valueDescription = "<type>",
        description = "Default base type for SceneGraph components (e.g., Group, Task)."
    )
    var defaultComponentExtends: String? = null
        set(value) {
            checkFrozen()
            field = if (value.isNullOrEmpty()) null else value
        }

    @GradleOption(
        value = DefaultValue.BOOLEAN_FALSE_DEFAULT,
        gradleInputType = GradleInputTypes.INPUT,
    )
    @Argument(
        value = "-strict-mode",
        description = "Enable strict mode for additional compile-time checks."
    )
    var strictMode: Boolean = false
        set(value) {
            checkFrozen()
            field = value
        }

    @Argument(
        value = "-Xproduce",
        valueDescription = "{executable|library}",
        description = "Compilation output type: 'executable' for .brs files (default), 'library' for .klib"
    )
    var produce: String? = null
        set(value) {
            checkFrozen()
            field = if (value.isNullOrEmpty()) null else value
        }

    @Argument(
        value = "-output",
        valueDescription = "<path>",
        description = "Output file path for library mode (.klib file)"
    )
    var output: String? = null
        set(value) {
            checkFrozen()
            field = if (value.isNullOrEmpty()) null else value
        }

    override fun copyOf(): Freezable = copyK2BrsCompilerArguments(this, K2BrsCompilerArguments())
}

/**
 * Copies K2BrsCompilerArguments properties.
 */
fun copyK2BrsCompilerArguments(from: K2BrsCompilerArguments, to: K2BrsCompilerArguments): K2BrsCompilerArguments {
    copyCommonKlibBasedCompilerArguments(from, to)

    to.outputDir = from.outputDir
    to.moduleName = from.moduleName
    to.libraries = from.libraries
    to.minRokuOS = from.minRokuOS
    to.debugMode = from.debugMode
    to.generateXml = from.generateXml
    to.minify = from.minify
    to.mainFunction = from.mainFunction
    to.defaultComponentExtends = from.defaultComponentExtends
    to.strictMode = from.strictMode
    to.produce = from.produce
    to.output = from.output

    return to
}

/**
 * Constants for BrightScript compiler arguments.
 */
object K2BrsArgumentConstants {
    const val MIN_ROKU_OS_9_0 = "9.0"
    const val MIN_ROKU_OS_9_4 = "9.4"
    const val MIN_ROKU_OS_10_0 = "10.0"
    const val MIN_ROKU_OS_11_0 = "11.0"
    const val MIN_ROKU_OS_12_0 = "12.0"
    const val MIN_ROKU_OS_13_0 = "13.0"

    const val DEFAULT_MIN_ROKU_OS = MIN_ROKU_OS_9_4

    val SUPPORTED_ROKU_OS_VERSIONS = listOf(
        MIN_ROKU_OS_9_0,
        MIN_ROKU_OS_9_4,
        MIN_ROKU_OS_10_0,
        MIN_ROKU_OS_11_0,
        MIN_ROKU_OS_12_0,
        MIN_ROKU_OS_13_0
    )

    const val PRODUCE_EXECUTABLE = "executable"
    const val PRODUCE_LIBRARY = "library"
    const val DEFAULT_PRODUCE = PRODUCE_EXECUTABLE

    val SUPPORTED_PRODUCE_VALUES = listOf(
        PRODUCE_EXECUTABLE,
        PRODUCE_LIBRARY
    )
}
