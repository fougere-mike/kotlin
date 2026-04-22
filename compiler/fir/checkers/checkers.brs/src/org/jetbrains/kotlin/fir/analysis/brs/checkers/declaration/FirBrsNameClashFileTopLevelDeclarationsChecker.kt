/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.brs.checkers.declaration

import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
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
import org.jetbrains.kotlin.name.Name

/**
 * Reports `BRS_NAME_CASE_CLASH` when two or more top-level declarations in the
 * same file have Kotlin names that differ in case but are equal after
 * `lowercase()` — these silently collide at BrightScript runtime because
 * BrightScript identifiers are case-insensitive.
 *
 * Skips:
 *  - Declarations with fake source kinds (compiler-synthesized: data-class
 *    members, expect/actual synthesis).
 *  - Groups where every Kotlin name is identical (those are handled by
 *    upstream `REDECLARATION` / `CONFLICTING_OVERLOADS`).
 *
 * Cross-file package-scope collisions are a known follow-up — they require
 * walking `FirPackageMemberScope`, which is deferred to a later B2 extension.
 */
object FirBrsNameClashFileTopLevelDeclarationsChecker : FirFileChecker(MppCheckerKind.Common) {
    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirFile) {
        val groupedByLowercase = mutableMapOf<String, MutableList<FirDeclaration>>()
        @OptIn(DirectDeclarationsAccess::class)
        for (member in declaration.declarations) {
            val name = member.caseClashName() ?: continue
            groupedByLowercase.getOrPut(name.asString().lowercase()) { mutableListOf() }.add(member)
        }
        for (group in groupedByLowercase.values) {
            reportCaseClashes(group)
        }
    }
}

/**
 * Extracts the Kotlin name used for case-clash grouping, or null if the
 * declaration should be skipped (no name, synthetic, or special).
 *
 * Constructors are skipped: FirConstructor symbols carry the enclosing
 * class's identifier as their name (not `<init>`), which would cause a
 * class's primary constructor to collide with a same-lowercase member inside
 * the class body (e.g., `class SubList` with `fun subList()`). Constructors
 * aren't independent name-holders at BrightScript runtime — they're invoked
 * through the class name itself.
 */
internal fun FirDeclaration.caseClashName(): Name? {
    if (source?.kind is KtFakeSourceElementKind) return null
    if (this is FirConstructor) return null
    val name = when (this) {
        is FirCallableDeclaration -> symbol.name
        is FirClassLikeDeclaration -> symbol.name
        else -> return null
    }
    return name.takeUnless { it.isSpecial }
}

/**
 * Reports `BRS_NAME_CASE_CLASH` on each member of `group` whose Kotlin name
 * has at least one peer with a *different* Kotlin name. Groups where every
 * Kotlin name is identical are left for upstream `REDECLARATION` /
 * `CONFLICTING_OVERLOADS` to handle.
 *
 * The visitor's `@Suppress` context is active at the enclosing file/class
 * level when a `FirFileChecker` / `FirClassChecker` runs — `@Suppress`
 * annotations on individual members have not yet been pushed. We check each
 * member's own annotations for `BRS_NAME_CASE_CLASH` explicitly.
 */
context(reporter: DiagnosticReporter, context: CheckerContext)
internal fun reportCaseClashes(group: List<FirDeclaration>) {
    if (group.size < 2) return
    val distinctNames = group.mapNotNull { it.caseClashName()?.asString() }.distinct()
    if (distinctNames.size < 2) return
    for (member in group) {
        val myName = member.caseClashName()?.asString() ?: continue
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
