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
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirRegularClassChecker
import org.jetbrains.kotlin.fir.analysis.collectors.AbstractDiagnosticCollector
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors
import org.jetbrains.kotlin.fir.declarations.DirectDeclarationsAccess
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.fir.declarations.getAnnotationByClassId
import org.jetbrains.kotlin.name.BrsStandardClassIds

/**
 * Reports BRS_BRSCONSTANT_VAR for every `var` property directly declared inside an
 * `object` annotated with @BrsConstant.
 *
 * @BrsConstant objects are evaluated at compile time and inlined at usage sites by
 * BrsConstantEvaluationLowering. A `var` cannot be inlined safely (its value can change
 * between reads), so the lowering currently emits a string error with no stable diagnostic
 * ID. This checker shifts the error left to FIR, gives it the stable ID
 * BRS_BRSCONSTANT_VAR, and makes it @Suppress-able by name.
 *
 * Class-kind filter: mirrors the IR lowering, which only processes ClassKind.OBJECT
 * carriers (BrsConstantEvaluationLowering.kt:64-65). @BrsConstant on a regular class is a
 * silent no-op at IR; we deliberately do not emit a FIR diagnostic for that case here.
 *
 * Defense-in-depth: the IR-phase walker still runs and re-emits a "[IR] @BrsConstant…"
 * error for callers that bypass FIR (klib boundaries) or when the FIR diagnostic is
 * @Suppress-ed at the property — same pattern as BRS_STATIC_* and
 * BRS_BRSNAME_REQUIRES_CALLABLE_REF.
 */
object FirBrsConstantVarChecker : FirRegularClassChecker(MppCheckerKind.Common) {

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirRegularClass) {
        if (declaration.classKind != ClassKind.OBJECT) return
        val session = context.session
        if (declaration.getAnnotationByClassId(BrsStandardClassIds.Annotations.BrsConstant, session) == null) return

        @OptIn(DirectDeclarationsAccess::class)
        for (member in declaration.declarations) {
            if (member !is FirProperty) continue
            if (!member.isVar) continue
            if (member.source?.kind is KtFakeSourceElementKind) continue
            if (member.isSuppressedByAnnotation("BRS_BRSCONSTANT_VAR")) continue
            reporter.reportOn(
                member.source,
                FirBrsErrors.BRS_BRSCONSTANT_VAR,
                member.name.asString(),
            )
        }
    }
}

private fun FirDeclaration.isSuppressedByAnnotation(diagnosticName: String): Boolean {
    val suppressed = AbstractDiagnosticCollector.getDiagnosticsSuppressedForContainer(this) ?: return false
    return diagnosticName in suppressed ||
        AbstractDiagnosticCollector.SUPPRESS_ALL_ERRORS in suppressed
}
