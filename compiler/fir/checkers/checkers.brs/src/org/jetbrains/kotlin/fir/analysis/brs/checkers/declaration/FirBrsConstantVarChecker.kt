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
 * Reports BRS_BRSCONSTANT_NON_OBJECT for classes carrying @BrsConstant that are not
 * `object`s, and BRS_BRSCONSTANT_VAR for `var` properties inside `@BrsConstant object`s.
 *
 * @BrsConstant objects are evaluated at compile time and inlined at usage sites by
 * BrsConstantEvaluationLowering. A `var` cannot be inlined safely (its value can change
 * between reads), so the lowering currently emits a string error with no stable diagnostic
 * ID. This checker shifts the error left to FIR, gives it the stable ID
 * BRS_BRSCONSTANT_VAR, and makes it @Suppress-able by name.
 *
 * On non-object class kinds (`class`, `interface`, `enum class`, `annotation class`,
 * `sealed class`), the IR lowering silently ignores the annotation
 * (BrsConstantEvaluationLowering.kt:64-65). BRS_BRSCONSTANT_NON_OBJECT shifts that misuse
 * left to FIR with a stable, suppressible diagnostic ID. No IR-side defense-in-depth mirror
 * is added — non-object @BrsConstant is a runtime no-op, so the FIR diagnostic is the
 * only checkpoint.
 *
 * Defense-in-depth: the IR-phase walker still runs and re-emits a "[IR] @BrsConstant…"
 * error for callers that bypass FIR (klib boundaries) or when the FIR BRS_BRSCONSTANT_VAR
 * diagnostic is @Suppress-ed at the property — same pattern as BRS_STATIC_* and
 * BRS_BRSNAME_REQUIRES_CALLABLE_REF.
 */
object FirBrsConstantVarChecker : FirRegularClassChecker(MppCheckerKind.Common) {

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirRegularClass) {
        val session = context.session
        if (declaration.getAnnotationByClassId(BrsStandardClassIds.Annotations.BrsConstant, session) == null) return

        if (declaration.classKind != ClassKind.OBJECT) {
            if (declaration.isSuppressedByAnnotation("BRS_BRSCONSTANT_NON_OBJECT")) return
            reporter.reportOn(
                declaration.source,
                FirBrsErrors.BRS_BRSCONSTANT_NON_OBJECT,
                declaration.classKind.codeRepresentation ?: declaration.classKind.name.lowercase(),
            )
            return
        }

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
