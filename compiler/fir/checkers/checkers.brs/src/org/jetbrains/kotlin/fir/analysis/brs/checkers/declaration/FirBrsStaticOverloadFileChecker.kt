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
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirFile
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.fir.declarations.getAnnotationByClassId
import org.jetbrains.kotlin.fir.packageFqName
import org.jetbrains.kotlin.fir.scopes.impl.FirPackageMemberScope
import org.jetbrains.kotlin.fir.scopes.impl.PACKAGE_MEMBER
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.name.BrsStandardClassIds
import org.jetbrains.kotlin.name.Name

/**
 * Reports `BRS_STATIC_OVERLOAD` when two or more top-level `@BrsStatic` functions
 * in the same package share a Kotlin name — whether they are in the same file or
 * different files. `@BrsStatic` compiles to a flat, unmangled BrightScript function
 * name, so overloads collide at runtime (BrightScript is case-insensitive and has
 * no module/file scope for top-level functions).
 *
 * Walks `FirPackageMemberScope` so cross-file same-package collisions are caught
 * (same pattern as `FirBrsNameClashFileTopLevelDeclarationsChecker`). Each file's
 * checker invocation reports only on its OWN declarations; peer files report on
 * theirs. This avoids double-reporting.
 *
 * Skips:
 *  - Peer functions without `@BrsStatic` (only annotated-vs-annotated collisions matter).
 *  - Library-origin peers (`!origin.fromSource`).
 *  - Fake-source declarations (compiler-synthesized).
 *
 * The class-scope counterpart lives in `FirBrsStaticClassChecker`; this checker
 * handles only file-level top-level functions.
 *
 * Defense-in-depth: the IR-phase walker in `BrsCompiler.validateBrsStaticAnnotations`
 * still runs for callers that bypass FIR (klib-deserialized dependencies).
 */
object FirBrsStaticOverloadFileChecker : FirFileChecker(MppCheckerKind.Common) {
    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirFile) {
        val session = context.session

        // --- Phase 1: collect this file's @BrsStatic functions ---
        val localByName = mutableMapOf<String, MutableList<FirSimpleFunction>>()
        val localDeclarations = mutableSetOf<FirDeclaration>()

        @OptIn(DirectDeclarationsAccess::class)
        for (member in declaration.declarations) {
            if (member !is FirSimpleFunction) continue
            if (member.source?.kind is KtFakeSourceElementKind) continue
            if (member.getAnnotationByClassId(BrsStandardClassIds.Annotations.BrsStatic, session) == null) continue
            localByName.getOrPut(member.name.asString()) { mutableListOf() }.add(member)
            localDeclarations += member
        }

        if (localByName.isEmpty()) return

        // --- Phase 2: probe package scope for cross-file @BrsStatic peers ---
        val packageScope: FirPackageMemberScope =
            context.sessionHolder.scopeSession.getOrBuild(declaration.packageFqName to session, PACKAGE_MEMBER) {
                FirPackageMemberScope(declaration.packageFqName, context.sessionHolder.session)
            }

        val peerCount = mutableMapOf<String, Int>()
        for (name in localByName.keys) {
            packageScope.processFunctionsByName(Name.identifier(name)) { peerSymbol ->
                @OptIn(SymbolInternals::class)
                val peerFir = peerSymbol.fir
                if (peerFir in localDeclarations) return@processFunctionsByName  // own decl, already counted
                if (!peerSymbol.origin.fromSource) return@processFunctionsByName  // skip library peers
                if (peerFir.getAnnotationByClassId(BrsStandardClassIds.Annotations.BrsStatic, session) == null)
                    return@processFunctionsByName  // unannotated peer doesn't count
                peerCount[name] = (peerCount[name] ?: 0) + 1
            }
        }

        // --- Phase 3: report on local functions whose total count >= 2 ---
        for ((name, localList) in localByName) {
            val total = localList.size + (peerCount[name] ?: 0)
            if (total < 2) continue
            val countRendered = total.toString()
            for (func in localList) {
                if (func.isSuppressedByAnnotation("BRS_STATIC_OVERLOAD")) continue
                reporter.reportOn(func.source, FirBrsErrors.BRS_STATIC_OVERLOAD, name, countRendered)
            }
        }
    }
}

private fun FirDeclaration.isSuppressedByAnnotation(diagnosticName: String): Boolean {
    val suppressed = AbstractDiagnosticCollector.getDiagnosticsSuppressedForContainer(this) ?: return false
    return diagnosticName in suppressed ||
        AbstractDiagnosticCollector.SUPPRESS_ALL_ERRORS in suppressed
}
