/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.brs

import org.jetbrains.kotlin.cli.brs.K2BrsCompiler
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.common.arguments.K2BrsCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.config.Services
import java.io.File
import kotlin.test.fail

/**
 * Base class for BrightScript FIR-phase diagnostic tests.
 *
 * Fixtures live under `compiler/testData/diagnostics/testsWithBrsStdLib/` and use
 * `<!DIAGNOSTIC_NAME!>...<!>` markers (same syntax as upstream JS/JVM diagnostic
 * tests). The runner strips markers from the source, compiles the clean source via
 * `K2BrsCompiler`, captures reported diagnostics through a `MessageCollector`, and
 * verifies that:
 *   - every marker is matched by a reported diagnostic whose severity is ERROR
 *     and whose name matches the marker;
 *   - no unexpected errors are reported beyond those declared by markers.
 *
 * Test data layout:
 *   compiler/testData/diagnostics/testsWithBrsStdLib/<subdir>/<name>.kt
 *
 * The harness is hand-rolled to mirror `AbstractBrsGoldenFileTest` and avoid
 * pulling the heavy upstream `TestConfigurationBuilder` machinery for a first
 * diagnostic. Converting both runners to upstream test infrastructure is a B7
 * task per the roadmap.
 */
abstract class AbstractBrsDiagnosticTest {

    companion object {
        private val TEST_DATA_ROOT = File("compiler/testData/diagnostics/testsWithBrsStdLib")
        private val STDLIB_KLIB = File("libraries/stdlib/brs-prebuilt/kotlin-stdlib-brs.klib")
        private val MARKER_REGEX = Regex("""<!([A-Z_]+)!>([\s\S]*?)<!>""")
    }

    protected data class Expected(val diagnostic: String, val strippedLine: Int, val strippedColumn: Int)

    protected fun runTest(testPath: String) {
        val inputFile = TEST_DATA_ROOT.resolve(testPath)
        if (!inputFile.exists()) {
            fail("Input file does not exist: ${inputFile.absolutePath}")
        }

        val rawSource = inputFile.readText()
        val (strippedSource, expected) = stripMarkers(rawSource)
        val segments = splitMultiFile(strippedSource)

        val tempFiles = segments.map { (name, content) ->
            File.createTempFile("brs-diag-${name.removeSuffix(".kt")}-", ".kt").apply {
                writeText(content)
                deleteOnExit()
            }
        }
        val actual = compileAndCollectDiagnostics(tempFiles)
        tempFiles.forEach { it.delete() }

        verify(testPath, expected, actual)
    }

    /**
     * Splits stripped test source by `// FILE: <name>.kt` markers.
     * If no marker is present, returns a single (synthetic) file using `default.kt`.
     * Marker format mirrors upstream Kotlin diagnostic-test convention.
     *
     * Returns a list of (filename, fileSource) pairs; `// FILE:` lines are stripped
     * from the segment they introduce.
     */
    private fun splitMultiFile(strippedSource: String): List<Pair<String, String>> {
        val markerRegex = Regex("""^// FILE:\s*(\S+)\s*$""", RegexOption.MULTILINE)
        val matches = markerRegex.findAll(strippedSource).toList()
        if (matches.isEmpty()) return listOf("default.kt" to strippedSource)
        val results = mutableListOf<Pair<String, String>>()
        for ((index, match) in matches.withIndex()) {
            val name = match.groupValues[1]
            val start = match.range.last + 1
            val end = if (index + 1 < matches.size) matches[index + 1].range.first else strippedSource.length
            results += name to strippedSource.substring(start, end)
        }
        return results
    }

    /**
     * Strip `<!DIAGNOSTIC_NAME!>...<!>` markers from the source and record, for each
     * marker, the diagnostic name and the (1-based) line+column where the marker
     * opened in the stripped text.
     *
     * Supports nested markers. A marker encloses a range, but for verification we only
     * track the start position of the inner content in the stripped source.
     */
    private fun stripMarkers(rawSource: String): Pair<String, List<Expected>> {
        val expected = mutableListOf<Expected>()
        val stripped = StringBuilder()
        var srcIndex = 0
        var strippedLine = 1
        var strippedColumn = 1

        while (srcIndex < rawSource.length) {
            // Closer "<!>" must be checked first — it is a prefix-conflict with the opener "<!".
            if (rawSource.startsWith("<!>", srcIndex)) {
                srcIndex += 3
                continue
            }
            if (rawSource.startsWith("<!", srcIndex)) {
                // Find opening marker end "!>"
                val markerEnd = rawSource.indexOf("!>", srcIndex + 2)
                if (markerEnd < 0) fail("Malformed marker (no !> close) at index $srcIndex")
                val diagName = rawSource.substring(srcIndex + 2, markerEnd)
                expected += Expected(diagName, strippedLine, strippedColumn)
                srcIndex = markerEnd + 2
                continue
            }
            val ch = rawSource[srcIndex]
            stripped.append(ch)
            if (ch == '\n') {
                strippedLine++
                strippedColumn = 1
            } else {
                strippedColumn++
            }
            srcIndex++
        }

        return stripped.toString() to expected
    }

    /**
     * Represents a single reported diagnostic captured from the MessageCollector.
     * We parse the text form of the message to extract the diagnostic name, since
     * K2BrsCompiler's MessageCollector bridge stringifies `KtDiagnostic` as
     * "NAME: message".
     */
    protected data class Reported(val severity: CompilerMessageSeverity, val message: String, val line: Int?, val column: Int?)

    private fun compileAndCollectDiagnostics(inputFiles: List<File>): List<Reported> {
        val tempOutputDir = createTempDir("brs-diag-test-")
        try {
            val compiler = K2BrsCompiler()
            val arguments = K2BrsCompilerArguments().apply {
                freeArgs = inputFiles.map { it.absolutePath }
                outputDir = tempOutputDir.absolutePath
                if (STDLIB_KLIB.exists()) libraries = STDLIB_KLIB.absolutePath
            }

            val reported = mutableListOf<Reported>()
            val collector = object : MessageCollector {
                override fun clear() { reported.clear() }
                override fun hasErrors(): Boolean = reported.any { it.severity == CompilerMessageSeverity.ERROR }
                override fun report(
                    severity: CompilerMessageSeverity,
                    message: String,
                    location: CompilerMessageSourceLocation?,
                ) {
                    reported += Reported(severity, message, location?.line, location?.column)
                }
            }
            // K2BrsCompiler.exec returns ExitCode; we don't assert on it, since
            // expected-error fixtures intentionally trigger COMPILATION_ERROR.
            @Suppress("UNUSED_VARIABLE")
            val exitCode: ExitCode = compiler.exec(collector, Services.EMPTY, arguments)
            return reported
        } finally {
            tempOutputDir.deleteRecursively()
        }
    }

    /**
     * Inverse lookup: rendered-message → diagnostic name.
     *
     * K2BrsCompiler renders FIR diagnostics as plain messages (without a leading
     * "NAME: " prefix), so we can't reliably extract names from the rendered text.
     * Instead, every diagnostic we verify in fixtures must be listed here with its
     * exact rendered template.
     *
     * The templates come from [org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrorsDefaultMessages].
     * Keep in sync manually.
     *
     * NOTE: DefaultMessages uses MessageFormat `''` escaping (e.g. `''{0}''`) to produce a literal
     * single-quote character in the rendered output. Entries here must use the *rendered* form with
     * a single `'`, NOT the `''` escape, because `resolveDiagnosticName` matches against the already-
     * rendered compiler output, never through MessageFormat.
     */
    private val diagnosticRenderedMessages: Map<String, String> = mapOf(
        "BRS_INTRINSIC_LITERAL_REQUIRED" to "An argument for the 'brs()' function must be a compile-time constant string.",
        "BRS_NAME_CASE_CLASH" to "Name clashes with {0} at BrightScript runtime (BrightScript is case-insensitive).",
        "BRS_INHERITED_NAME_CASE_CLASH" to "Inherited members '{0}' and '{1}' clash at BrightScript runtime (BrightScript is case-insensitive). Override one or suppress.",
        "BRS_SCENEGRAPH_FIELD_TYPE" to "{0} requires {1} property type, got {2}.",
        "BRS_SCENEGRAPH_FIELD_CONFLICT" to "Property has conflicting SceneGraph field annotations: {0}. Keep only one.",
        "BRS_ONCHANGE_HANDLER_NOT_FOUND" to "@BrsOnChange handler '{0}' not found in class '{1}'.",
        "BRS_ONCHANGE_HANDLER_SIGNATURE" to
            "@BrsOnChange handler '{0}' on class '{1}' must take no arguments or a single RoSGNodeEvent; got signature {2}.",
        "BRS_IO_WORKER_NON_SERIALIZABLE_CAPTURE" to
            "Captured '{0}' of type '{1}' cannot cross the Dispatchers.IO task thread boundary. " +
                "Only primitives, String, RoArray, RoAssociativeArray, and Dynamic are serializable.",
        "BRS_IO_DISPATCHER_UNSUPPORTED" to
            "Dispatchers.IO is unsupported on this platform: IO dispatch silently falls back to the " +
                "render-thread queue, so it behaves exactly like Dispatchers.Main. " +
                "Use runTask<T> for background work.",
        "BRS_ADDFIELD_INVALID_TYPE" to "'{0}' is not a valid SceneGraph field type for addField(). Valid types: {1}.",
        "BRS_CREATE_OBJECT_INVALID_TYPE" to "'{0}' is not a valid BrightScript object type for createObject(). Valid types: {1}.",
        "BRS_STATIC_INVALID_TARGET" to
            "@BrsStatic is only valid on top-level functions, object members, or companion object members. " +
                "Found on member of class '{0}'.",
        "BRS_STATIC_OVERLOAD" to
            "@BrsStatic function '{0}' cannot have overloads. Found {1} functions with the same name in the same scope.",
        "BRS_BRSNAME_REQUIRES_CALLABLE_REF" to "brsName() requires a function reference (::functionName), got '{0}'.",
        "BRS_BRSNAME_INVALID_TARGET" to
            "brsName() target must be a top-level function, an object/companion-object member, an instance method of a regular class, or a constructor. '{1}' is a {0} and produces a name that does not match a runtime BrightScript function.",
        "BRS_BRSNAME_BLANK" to
            "@BrsName argument must be a non-blank BrightScript identifier on declaration '{0}'. Remove the annotation to fall back to the Kotlin name, or supply a valid identifier.",
        "BRS_INTRINSIC_USER_DEFINED" to
            "@BrsIntrinsic is reserved for the BrightScript stdlib. Function '{0}' cannot be marked as an intrinsic.",
        "BRS_BRSCONSTANT_VAR" to "@BrsConstant objects cannot contain var properties. Use val instead: '{0}'.",
        "BRS_BRSCONSTANT_NON_OBJECT" to "@BrsConstant is only valid on object declarations. Found on {0} declaration.",
        "BRS_BRSCREATEOBJECT_INVALID_TYPE" to "'{0}' is not a valid BrightScript object type for @BrsCreateObject. Valid types: {1}.",
        "BRS_TRY_FINALLY_UNSUPPORTED" to
            "try/finally is not supported outside suspend functions on the BrightScript backend: " +
                "the 'finally' block is silently dropped. Move this code into a suspend function, " +
                "restructure without 'finally', or @Suppress(\"BRS_TRY_FINALLY_UNSUPPORTED\") with a tracking comment.",
        "BRS_SCOPE_BLOCK_NOT_LITERAL" to
            "ScopeHandle.run { } requires a literal lambda at the call site (the compiler lifts it into a named request). " +
                "Passing a stored function value cannot be lowered — declare a ScopeRequest object and use run(request, args) instead.",
        "BRS_SCOPE_CAPTURE_UNMARSHALLABLE" to
            "Captured '{0}' of type '{1}' cannot cross the scope boundary: captures cross by copy as plain data, and this " +
                "type's methods do not survive the copy. Marshallable: primitives, String, Dynamic, and native BrightScript " +
                "types (RoArray, RoAssociativeArray, RoSGNode, and other external interfaces). Pass plain data (an " +
                "RoAssociativeArray or primitives) or move the value's construction inside the block.",
        "BRS_SCOPE_CAPTURE_MUTATION_LOST" to
            "Assignment to captured variable '{0}' inside a ScopeHandle.run block: captures cross by copy; " +
                "this write never reaches the caller — return a value from the block instead.",
        "BRS_SCOPE_RESULT_NOT_DATA" to
            "Scope request result type '{0}' is outside the marshallable set (primitives, String, Dynamic, and external " +
                "interfaces like RoArray/RoAssociativeArray/RoSGNode): results cross as data; methods/equals/copy will not " +
                "survive — share behavioral state via the VM.",
        "BRS_SCOPE_ARG_NOT_MARSHALLABLE" to
            "Scope request argument type '{0}' is outside the marshallable set (primitives, String, Dynamic, and external " +
                "interfaces like RoArray/RoAssociativeArray/RoSGNode): arguments cross by copy as plain data — " +
                "pass plain data or restructure the request.",
        "BRS_TASK_STATE_NOT_FIELD" to
            "Property '{0}' in task component '{1}' compiles to plain m-state: run() executes against a task-thread copy, " +
                "and writes from run() are silently lost. Annotate it with an @SG*Field annotation (or @BrsField), " +
                "or make it a local variable in run().",
        "BRS_CREATE_COMPONENT_INVALID_TYPE" to
            "'{1}' is not a valid component type for {0}(): {2}. The type argument must be a concrete component class " +
                "(extending GroupComponent, SceneComponent, TaskComponent, etc., or annotated with @BrsComponent).",
        // Upstream (non-BRS) diagnostic: the createComponentInvalidType/interfaceRejected fixture
        // expects it alongside ours, because an interface also violates the T : ComponentBase bound.
        // Template from FirErrorsDefaultMessages; {2} is an optional trailing sentence.
        "UPPER_BOUND_VIOLATED" to "Type argument is not within its bounds: must be subtype of '{0}'.{2}",
    )

    /**
     * Patterns for messages that appear alongside real diagnostics but are summary
     * noise we should ignore when matching expectations.
     */
    private val ignoredMessagePatterns: List<Regex> = listOf(
        Regex("^FIR analysis found errors.*"),
        Regex("^Backend codegen failed:.*"),
        // IR-backend errors for brs() are additive alongside FIR-phase ones (see
        // `IrToBrsTransformer.kt` brs branch). Fixtures that declare only the FIR
        // diagnostic expect IR-phase errors to be ignored at verification time.
        Regex("^brs\\(\\) argument must be a compile-time constant string.*"),
        Regex("^Error in brs\\(\\) code:.*"),
        Regex("^brs\\(\\) argument parsed to no statements.*"),
        Regex("^brs\\(\\) must contain exactly one expression or statement.*"),
        // IR-phase @BrsStatic validator fires when FIR suppresses the diagnostic (FIR suppression
        // prevents COMPILATION_ERROR so IR lowering runs). IR messages are prefixed with "[IR] "
        // to distinguish them from FIR-rendered messages.
        Regex("^\\[IR] @BrsStatic.*"),
        // IR-phase brsName() validator fires when FIR BRS_BRSNAME_REQUIRES_CALLABLE_REF is suppressed.
        Regex("^\\[IR] brsName\\(\\).*"),
        // IR-phase @BrsConstant validator fires when FIR BRS_BRSCONSTANT_VAR is suppressed
        // (FIR suppression prevents COMPILATION_ERROR so IR lowering still runs).
        Regex("^\\[IR] @BrsConstant.*"),
        // IR-phase createComponent validation (IrExpressionToBrsTransformer) fires when FIR
        // BRS_CREATE_COMPONENT_INVALID_TYPE is suppressed.
        Regex("^\\[IR] createComponent type argument.*"),
    )

    private fun verify(testPath: String, expected: List<Expected>, actual: List<Reported>) {
        val errors = actual.filter { it.severity == CompilerMessageSeverity.ERROR }
            .filterNot { err -> ignoredMessagePatterns.any { it.containsMatchIn(err.message) } }

        // WARNING-severity BRS diagnostics (e.g. BRS_SCOPE_CAPTURE_MUTATION_LOST) are
        // verified too — but only those resolving to a known template. Upstream
        // warnings (unused variable, name shadowing, ...) fire incidentally on many
        // fixtures and stay out of verification, exactly as before warnings existed
        // in the BRS diagnostic set.
        val warnings = actual
            .filter { it.severity == CompilerMessageSeverity.WARNING || it.severity == CompilerMessageSeverity.STRONG_WARNING }
            .filter { resolveDiagnosticName(it.message) != "<unknown>" }

        // Resolve each reported diagnostic to a name via the inverse message map.
        val actualNames = (errors + warnings).map { err -> resolveDiagnosticName(err.message) }.sorted()
        val expectedNames = expected.map { it.diagnostic }.sorted()

        if (expectedNames != actualNames) {
            val expectedStr = expectedNames.joinToString(", ").ifEmpty { "<none>" }
            val actualStr = actualNames.joinToString(", ").ifEmpty { "<none>" }
            val actualMessages = (errors + warnings).joinToString("\n  ") {
                "${resolveDiagnosticName(it.message)} @ L${it.line}:${it.column} — ${it.message}"
            }
            fail(
                """
                |Diagnostic mismatch for $testPath
                |  Expected: $expectedStr
                |  Actual:   $actualStr
                |
                |Reported errors:
                |  ${actualMessages.ifEmpty { "<none>" }}
                """.trimMargin()
            )
        }
    }

    private fun resolveDiagnosticName(message: String): String {
        // Exact template match.
        for ((name, template) in diagnosticRenderedMessages) {
            if (message == template) return name
        }
        // Parameterized templates use {0}/{1}; check by regex. Regex.escape wraps
        // the template in `\Q...\E` literal-quote markers, so substituting `{0}` with
        // `.*` would place the wildcard inside the quoted literal. Close the quote
        // around the placeholder so `.*` is interpreted as a regex wildcard.
        for ((name, template) in diagnosticRenderedMessages) {
            val regex = Regex("^" + Regex.escape(template).replace("""\{\d+}""".toRegex(), """\\E.*\\Q""") + "$")
            if (regex.matches(message)) return name
        }
        return "<unknown>"
    }

    private fun createTempDir(prefix: String): File =
        File.createTempFile(prefix, "").apply {
            delete()
            mkdirs()
        }
}
