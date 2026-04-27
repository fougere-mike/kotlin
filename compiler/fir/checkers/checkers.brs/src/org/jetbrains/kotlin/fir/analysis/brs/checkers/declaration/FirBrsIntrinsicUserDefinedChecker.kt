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
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirSimpleFunctionChecker
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.fir.declarations.getAnnotationByClassId
import org.jetbrains.kotlin.name.BrsStandardClassIds

/**
 * Reports [FirBrsErrors.BRS_INTRINSIC_USER_DEFINED] when a user-defined function carries
 * `@BrsIntrinsic`. This annotation is reserved for stdlib-internal use: it maps a Kotlin
 * function to a BrightScript intrinsic implementation, and the IR backend only knows how
 * to dispatch the ~23 stdlib entries in `stdlibIntrinsicMapping`.
 *
 * Misuse shapes and their failure modes without this check:
 *   - `external` user function: IR routes the call into `stdlibIntrinsicMapping`; unknown
 *     key → no codegen / runtime failure on device.
 *   - Non-`external` user function: `isStdlibIntrinsic` short-circuits at `!isExternal`,
 *     the annotation is silently ignored, and the user's body compiles normally — the
 *     annotation is decorative and misleading.
 *
 * **Origin + package filter:** At user-compile time, stdlib loads as Library
 * (`origin.fromSource == false`), so the origin check alone would suffice. However,
 * when stdlib itself is compiled its own source is `origin.fromSource == true`.
 * Adding a package guard (`kotlin.brs.*`) lets the checker stay silent during the
 * stdlib build without requiring `@Suppress` on all ~23 stdlib intrinsic sites.
 * Both guards together ensure only non-`kotlin.brs.*` user source triggers the
 * diagnostic, matching the pattern in
 * `FirBrsNameClashFileTopLevelDeclarationsChecker` (line 118).
 *
 * `FirSimpleFunctionChecker` runs per-member with annotation context already pushed, so
 * `@Suppress("BRS_INTRINSIC_USER_DEFINED")` on the function works automatically — no
 * manual `isSuppressedByAnnotation` helper needed.
 */
object FirBrsIntrinsicUserDefinedChecker : FirSimpleFunctionChecker(MppCheckerKind.Common) {

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirSimpleFunction) {
        if (!declaration.origin.fromSource) return
        if (declaration.source?.kind is KtFakeSourceElementKind) return
        // Skip kotlin.brs.* packages — those are stdlib-internal sites (e.g., GlobalFunctions.kt).
        // This guard also protects stdlib's own compilation pass where origin.fromSource == true.
        val pkg = declaration.symbol.callableId.packageName.asString()
        if (pkg == "kotlin.brs" || pkg.startsWith("kotlin.brs.")) return

        declaration.getAnnotationByClassId(BrsStandardClassIds.Annotations.BrsIntrinsic, context.session)
            ?: return

        reporter.reportOn(
            declaration.source,
            FirBrsErrors.BRS_INTRINSIC_USER_DEFINED,
            declaration.name.asString(),
        )
    }
}
