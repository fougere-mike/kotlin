/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.brs.checkers.declaration

import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirClassChecker
import org.jetbrains.kotlin.fir.analysis.collectors.AbstractDiagnosticCollector
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors
import org.jetbrains.kotlin.fir.declarations.DirectDeclarationsAccess
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.fir.declarations.getAnnotationByClassId
import org.jetbrains.kotlin.fir.declarations.utils.isCompanion
import org.jetbrains.kotlin.name.BrsStandardClassIds

/**
 * Reports two diagnostics for `@BrsStatic` functions inside a class body:
 *
 *   - `BRS_STATIC_INVALID_TARGET` when the enclosing class is anything other
 *     than a (non-companion) `object` or a companion object — that is, regular
 *     classes, abstract classes, sealed classes, enum classes, or interfaces.
 *
 *   - `BRS_STATIC_OVERLOAD` when two or more `@BrsStatic` functions in the same
 *     valid (object/companion) scope share a Kotlin name. Reports on every
 *     member of the duplicate group.
 *
 * The FIR diagnostic visitor invokes this checker once per `FirClass` (including
 * nested classes), so we do NOT recurse into nested member classes — each is
 * visited independently.
 *
 * Defense-in-depth: the IR-phase walker in `BrsCompiler.validateBrsStaticAnnotations`
 * still runs and re-emits these diagnostics for callers that bypass FIR (klib
 * boundaries).
 */
object FirBrsStaticClassChecker : FirClassChecker(MppCheckerKind.Common) {
    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirClass) {
        val session = context.session
        val isValidContext = declaration.classKind == ClassKind.OBJECT ||
            (declaration is FirRegularClass && declaration.isCompanion)
        val className = (declaration as? FirRegularClass)?.name?.asString()
            ?: declaration.classKind.name.lowercase()

        val staticFunctionsByName = mutableMapOf<String, MutableList<FirSimpleFunction>>()

        @OptIn(DirectDeclarationsAccess::class)
        for (member in declaration.declarations) {
            if (member !is FirSimpleFunction) continue
            if (member.source?.kind is KtFakeSourceElementKind) continue
            if (member.getAnnotationByClassId(BrsStandardClassIds.Annotations.BrsStatic, session) == null) continue
            if (!isValidContext) {
                if (!member.isSuppressedByAnnotation("BRS_STATIC_INVALID_TARGET")) {
                    reporter.reportOn(
                        member.source,
                        FirBrsErrors.BRS_STATIC_INVALID_TARGET,
                        className,
                    )
                }
            } else {
                staticFunctionsByName.getOrPut(member.name.asString()) { mutableListOf() }.add(member)
            }
        }

        if (!isValidContext) return

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
