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
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirBasicDeclarationChecker
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors
import org.jetbrains.kotlin.fir.declarations.FirCallableDeclaration
import org.jetbrains.kotlin.fir.declarations.FirClassLikeDeclaration
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.getAnnotationByClassId
import org.jetbrains.kotlin.fir.declarations.getStringArgument
import org.jetbrains.kotlin.name.BrsStandardClassIds
import org.jetbrains.kotlin.name.Name

/**
 * Rejects `@BrsName("")` and `@BrsName("   ")` on any declaration target.
 *
 * A blank `@BrsName` argument is never meaningful:
 *  - FIR's `effectiveBrsName` helper treats blank as "fall back to Kotlin name" (silent no-op)
 *  - IR's `getBrsName()` returns the empty string verbatim, producing a broken BrightScript identifier
 *
 * This checker fires at FIR phase before IR so the error appears with a stable diagnostic ID
 * and can be suppressed with `@Suppress("BRS_BRSNAME_BLANK")`.
 */
object FirBrsBlankAnnotationChecker : FirBasicDeclarationChecker(MppCheckerKind.Common) {

    private val BRS_NAME_ARG = Name.identifier("name")

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirDeclaration) {
        if (declaration.source?.kind is KtFakeSourceElementKind) return

        val annotation = declaration.getAnnotationByClassId(
            BrsStandardClassIds.Annotations.BrsName,
            context.session,
        ) ?: return

        val arg = annotation.getStringArgument(BRS_NAME_ARG, context.session) ?: return
        if (arg.isNotBlank()) return

        val displayName = when (declaration) {
            is FirCallableDeclaration -> declaration.symbol.name.asString()
            is FirClassLikeDeclaration -> declaration.symbol.name.asString()
            else -> return
        }

        reporter.reportOn(
            declaration.source,
            FirBrsErrors.BRS_BRSNAME_BLANK,
            displayName,
        )
    }
}
