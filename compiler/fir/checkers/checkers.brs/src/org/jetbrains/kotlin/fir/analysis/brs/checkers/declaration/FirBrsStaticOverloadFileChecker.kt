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
import org.jetbrains.kotlin.name.BrsStandardClassIds

/**
 * Reports `BRS_STATIC_OVERLOAD` when two or more top-level `@BrsStatic` functions
 * in the same file share a Kotlin name. `@BrsStatic` compiles to a flat,
 * unmangled BrightScript function name, so overloads collide at runtime.
 *
 * The class-scope counterpart lives in `FirBrsStaticClassChecker`; this checker
 * handles only file-level top-level functions.
 *
 * The annotation is `@Target(FUNCTION)`, so non-function targets are filtered
 * out upstream by `WRONG_ANNOTATION_TARGET`. Top-level functions are always
 * a valid `@BrsStatic` context, so there is no INVALID_TARGET case here.
 *
 * Defense-in-depth: the IR-phase walker in `BrsCompiler.validateBrsStaticAnnotations`
 * still runs for callers that bypass FIR (klib-deserialized dependencies).
 */
object FirBrsStaticOverloadFileChecker : FirFileChecker(MppCheckerKind.Common) {
    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirFile) {
        val session = context.session
        val staticFunctionsByName = mutableMapOf<String, MutableList<FirSimpleFunction>>()

        @OptIn(DirectDeclarationsAccess::class)
        for (member in declaration.declarations) {
            if (member !is FirSimpleFunction) continue
            if (member.source?.kind is KtFakeSourceElementKind) continue
            if (member.getAnnotationByClassId(BrsStandardClassIds.Annotations.BrsStatic, session) == null) continue
            staticFunctionsByName.getOrPut(member.name.asString()) { mutableListOf() }.add(member)
        }

        for ((name, overloads) in staticFunctionsByName) {
            if (overloads.size < 2) continue
            val countRendered = overloads.size.toString()
            for (func in overloads) {
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
