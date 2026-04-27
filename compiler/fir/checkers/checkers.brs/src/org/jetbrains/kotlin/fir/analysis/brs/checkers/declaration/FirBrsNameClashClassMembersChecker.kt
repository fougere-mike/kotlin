/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.brs.checkers.declaration

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirClassChecker
import org.jetbrains.kotlin.fir.analysis.checkers.getContainingClassSymbol
import org.jetbrains.kotlin.fir.analysis.checkers.unsubstitutedScope
import org.jetbrains.kotlin.fir.analysis.collectors.AbstractDiagnosticCollector
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors
import org.jetbrains.kotlin.fir.declarations.DirectDeclarationsAccess
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.originalForSubstitutionOverride
import org.jetbrains.kotlin.fir.scopes.collectAllFunctions
import org.jetbrains.kotlin.fir.scopes.collectAllProperties
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirIntersectionCallableSymbol

/**
 * Reports `BRS_NAME_CASE_CLASH` when two or more directly-declared members of
 * a class/interface/object have effective BrightScript names that differ in case
 * but are equal after `lowercase()` (Path A — existing), OR when a directly-declared
 * member case-clashes with an inherited member (Path B).
 *
 * Reports `BRS_INHERITED_NAME_CASE_CLASH` when two or more inherited members
 * case-clash and the user class declares no member in that bucket (Path C).
 *
 * Walks `unsubstitutedScope` to include inherited callables from all supertypes.
 * `FirIntersectionCallableSymbol` (multi-interface inheritance) is flattened via
 * its `intersections` property; substitution overrides are unwrapped via
 * `originalForSubstitutionOverride` so effective-name lookup resolves correctly.
 */
object FirBrsNameClashClassMembersChecker : FirClassChecker(MppCheckerKind.Common) {
    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirClass) {
        val session = context.session

        // Path A: directly-declared members — unchanged from original implementation.
        val ownByLowercase = mutableMapOf<String, MutableList<Pair<FirDeclaration, String>>>()
        @OptIn(DirectDeclarationsAccess::class)
        for (member in declaration.declarations) {
            val name = member.effectiveBrsName(session) ?: continue
            ownByLowercase.getOrPut(name.lowercase()) { mutableListOf() }.add(member to name)
        }
        for (group in ownByLowercase.values) {
            reportCaseClashes(group)
        }

        // Collect inherited callables (functions + properties) from the unsubstituted scope.
        // Exclude members whose containing class IS the current class — those are own-decls.
        val inheritedByLowercase = mutableMapOf<String, MutableList<String>>()
        val scope = declaration.symbol.unsubstitutedScope()
        val inheritedSymbols = buildList {
            addAll(flattenIntersections(scope.collectAllFunctions()))
            addAll(flattenIntersections(scope.collectAllProperties()))
        }
        for (sym in inheritedSymbols) {
            if (sym.getContainingClassSymbol() == declaration.symbol) continue
            @OptIn(SymbolInternals::class)
            val firDecl = sym.fir as? FirDeclaration ?: continue
            val name = firDecl.effectiveBrsName(session) ?: continue
            inheritedByLowercase.getOrPut(name.lowercase()) { mutableListOf() }.add(name)
        }

        // Path B: for each own-declared member, check against the inherited bucket.
        // Override exemption: if own member's effective name exactly matches an inherited name, skip.
        for ((lowercase, ownGroup) in ownByLowercase) {
            val inheritedNames = inheritedByLowercase[lowercase] ?: continue
            for ((member, ownName) in ownGroup) {
                if (inheritedNames.any { it == ownName }) continue
                if (member.isSuppressedByAnnotation("BRS_NAME_CASE_CLASH")) continue
                val peersRendered = inheritedNames.distinct().sorted().joinToString(", ") { "'$it'" }
                reporter.reportOn(member.source, FirBrsErrors.BRS_NAME_CASE_CLASH, peersRendered)
            }
        }

        // Path C: inherited-vs-inherited clashes where the user declared no member in the bucket.
        for ((lowercase, inheritedNames) in inheritedByLowercase) {
            val distinctNames = inheritedNames.distinct()
            if (distinctNames.size < 2) continue
            if (ownByLowercase.containsKey(lowercase)) continue
            if (declaration.isSuppressedByAnnotation("BRS_INHERITED_NAME_CASE_CLASH")) continue
            reporter.reportOn(
                declaration.source,
                FirBrsErrors.BRS_INHERITED_NAME_CASE_CLASH,
                distinctNames[0],
                distinctNames[1],
            )
        }
    }

    /**
     * Flattens intersection symbols from multi-interface inheritance.
     * For each `FirIntersectionCallableSymbol`, expands its `intersections` list.
     * Substitution overrides are unwrapped to their originals.
     */
    private fun flattenIntersections(
        symbols: Collection<FirCallableSymbol<*>>,
    ): List<FirCallableSymbol<*>> = buildList {
        for (sym in symbols) {
            if (sym is FirIntersectionCallableSymbol) {
                for (intersected in sym.intersections) {
                    add(intersected.originalForSubstitutionOverride ?: intersected)
                }
            } else {
                add(sym.originalForSubstitutionOverride ?: sym)
            }
        }
    }

    private fun FirDeclaration.isSuppressedByAnnotation(diagnosticName: String): Boolean {
        val suppressed = AbstractDiagnosticCollector.getDiagnosticsSuppressedForContainer(this) ?: return false
        return diagnosticName in suppressed ||
            AbstractDiagnosticCollector.SUPPRESS_ALL_ERRORS in suppressed
    }

    private fun FirClass.isSuppressedByAnnotation(diagnosticName: String): Boolean =
        (this as FirDeclaration).isSuppressedByAnnotation(diagnosticName)
}
