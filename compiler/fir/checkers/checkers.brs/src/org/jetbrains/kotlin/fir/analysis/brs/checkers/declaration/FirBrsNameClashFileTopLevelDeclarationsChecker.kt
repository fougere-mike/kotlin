/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.brs.checkers.declaration

import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirFileChecker
import org.jetbrains.kotlin.fir.analysis.collectors.AbstractDiagnosticCollector
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors
import org.jetbrains.kotlin.fir.declarations.DirectDeclarationsAccess
import org.jetbrains.kotlin.fir.declarations.FirCallableDeclaration
import org.jetbrains.kotlin.fir.declarations.FirClassLikeDeclaration
import org.jetbrains.kotlin.fir.declarations.FirConstructor
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirFile
import org.jetbrains.kotlin.fir.declarations.getAnnotationByClassId
import org.jetbrains.kotlin.fir.declarations.getStringArgument
import org.jetbrains.kotlin.fir.packageFqName
import org.jetbrains.kotlin.fir.scopes.impl.FirPackageMemberScope
import org.jetbrains.kotlin.fir.scopes.impl.PACKAGE_MEMBER
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.name.BrsStandardClassIds
import org.jetbrains.kotlin.name.Name

/**
 * Reports `BRS_NAME_CASE_CLASH` when two or more top-level declarations in the
 * same package have effective BrightScript names that differ in case but are
 * equal after `lowercase()` — these silently collide at BrightScript runtime
 * because BrightScript identifiers are case-insensitive.
 *
 * Effective name = `@BrsName("x")` override if present and non-blank, else the
 * Kotlin identifier. This matches the IR backend's `getBrsName(...)` resolution.
 *
 * Walks `FirPackageMemberScope` so cross-file same-package collisions are caught
 * (precedent: `FirConflictsDeclarationChecker.checkFile`). Each file's checker
 * invocation reports only on its OWN declarations; the peer file reports on its
 * own when its checker runs. This avoids double-reporting.
 *
 * Skips:
 *  - Declarations with fake source kinds (compiler-synthesized).
 *  - Constructors (carry the enclosing class's name).
 *  - Groups where every effective name is identical (handled by upstream
 *    `REDECLARATION` / `CONFLICTING_OVERLOADS`).
 *  - Library-origin declarations (we only flag in-source clashes).
 */
object FirBrsNameClashFileTopLevelDeclarationsChecker : FirFileChecker(MppCheckerKind.Common) {
    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirFile) {
        val session = context.session
        val packageScope: FirPackageMemberScope =
            context.sessionHolder.scopeSession.getOrBuild(declaration.packageFqName to session, PACKAGE_MEMBER) {
                FirPackageMemberScope(declaration.packageFqName, context.sessionHolder.session)
            }

        // Collect this file's own declarations, grouped by lowercase effective name.
        val localByLowercase = mutableMapOf<String, MutableList<Pair<FirDeclaration, String>>>()
        val localDeclarations = mutableSetOf<FirDeclaration>()
        @OptIn(DirectDeclarationsAccess::class)
        for (member in declaration.declarations) {
            val name = member.effectiveBrsName(session) ?: continue
            localByLowercase.getOrPut(name.lowercase()) { mutableListOf() }.add(member to name)
            localDeclarations += member
        }

        // For each lowercase bucket, probe the package scope for peers with the same
        // lowercase effective name but a different effective name. We probe by the
        // exact Name objects of local members (both original casing and lowercase) to
        // pick up same-package peers declared with a different casing in another file.
        for ((lowercase, locals) in localByLowercase) {
            val combined = mutableListOf<Pair<FirDeclaration, String>>().apply { addAll(locals) }
            val seenSymbols = mutableSetOf<FirBasedSymbol<*>>()

            val probeNames = locals.flatMap { (_, name) ->
                listOf(
                    Name.identifier(name),
                    Name.identifier(name.lowercase()),
                    Name.identifier(name.replaceFirstChar { it.uppercaseChar() }),
                )
            }.toSet()

            for (probe in probeNames) {
                packageScope.processFunctionsByName(probe) { sym ->
                    addPeerIfNotLocal(combined, seenSymbols, sym, localDeclarations, session, lowercase)
                }
                packageScope.processPropertiesByName(probe) { sym ->
                    addPeerIfNotLocal(combined, seenSymbols, sym, localDeclarations, session, lowercase)
                }
                packageScope.processClassifiersByNameWithSubstitution(probe) { sym, _ ->
                    addPeerIfNotLocal(combined, seenSymbols, sym, localDeclarations, session, lowercase)
                }
            }

            reportCaseClashes(combined, localOnly = localDeclarations)
        }
    }

    @OptIn(SymbolInternals::class)
    private fun addPeerIfNotLocal(
        combined: MutableList<Pair<FirDeclaration, String>>,
        seen: MutableSet<FirBasedSymbol<*>>,
        peerSymbol: FirBasedSymbol<*>,
        localDeclarations: Set<FirDeclaration>,
        session: FirSession,
        expectedLowercase: String,
    ) {
        if (peerSymbol in seen) return
        seen += peerSymbol
        val peerDecl = peerSymbol.fir as? FirDeclaration ?: return
        if (peerDecl in localDeclarations) return
        if (!peerSymbol.origin.fromSource) return
        val peerName = peerDecl.effectiveBrsName(session) ?: return
        // Only add this peer if its effective name actually lowercase-collides with
        // the current bucket. A probe with Name("Foo") may return a declaration
        // whose @BrsName-renamed effective name has a different lowercase.
        if (peerName.lowercase() != expectedLowercase) return
        combined += peerDecl to peerName
    }
}

private val BRS_NAME_ARG = Name.identifier("name")

/**
 * Returns the effective BrightScript name for case-clash grouping, or null if the
 * declaration should be skipped (no name, synthetic, or special).
 *
 * If `@BrsName("x")` is present and non-blank, returns `x` (the emitted BRS name).
 * Otherwise returns the Kotlin identifier. Constructors are skipped: they are
 * invoked through the class name and are not independent name-holders at BRS runtime.
 */
internal fun FirDeclaration.effectiveBrsName(session: FirSession): String? {
    if (source?.kind is KtFakeSourceElementKind) return null
    if (this is FirConstructor) return null
    val kotlinName = when (this) {
        is FirCallableDeclaration -> symbol.name
        is FirClassLikeDeclaration -> symbol.name
        else -> return null
    }
    if (kotlinName.isSpecial) return null
    val annotation = getAnnotationByClassId(BrsStandardClassIds.Annotations.BrsName, session)
    val override = annotation?.getStringArgument(BRS_NAME_ARG, session)
    if (!override.isNullOrBlank()) return override
    return kotlinName.asString()
}

/**
 * Reports `BRS_NAME_CASE_CLASH` on each member of `group` whose effective BRS name
 * has at least one peer with a *different* effective name. Groups where every
 * effective name is identical are left for upstream `REDECLARATION` /
 * `CONFLICTING_OVERLOADS` to handle.
 *
 * When `localOnly` is non-null, only reports on declarations in that set. This
 * prevents double-reporting in cross-file scenarios: each file's checker invocation
 * reports on its own declarations only; the peer file reports on its own.
 *
 * The visitor's `@Suppress` context is active at the enclosing file/class level
 * when a `FirFileChecker` / `FirClassChecker` runs — `@Suppress` annotations on
 * individual members have not yet been pushed. We check each member's own
 * annotations for `BRS_NAME_CASE_CLASH` explicitly.
 */
context(reporter: DiagnosticReporter, context: CheckerContext)
internal fun reportCaseClashes(
    group: List<Pair<FirDeclaration, String>>,
    localOnly: Set<FirDeclaration>? = null,
) {
    if (group.size < 2) return
    val distinctNames = group.map { it.second }.distinct()
    if (distinctNames.size < 2) return
    for ((member, myName) in group) {
        if (localOnly != null && member !in localOnly) continue
        val peers = distinctNames.filter { it != myName }
        if (peers.isEmpty()) continue
        if (member.isSuppressedByAnnotation("BRS_NAME_CASE_CLASH")) continue
        val peersRendered = peers.sorted().joinToString(", ") { "'$it'" }
        reporter.reportOn(member.source, FirBrsErrors.BRS_NAME_CASE_CLASH, peersRendered)
    }
}

private fun FirDeclaration.isSuppressedByAnnotation(diagnosticName: String): Boolean {
    val suppressed = AbstractDiagnosticCollector.getDiagnosticsSuppressedForContainer(this) ?: return false
    return diagnosticName in suppressed ||
        AbstractDiagnosticCollector.SUPPRESS_ALL_ERRORS in suppressed
}
