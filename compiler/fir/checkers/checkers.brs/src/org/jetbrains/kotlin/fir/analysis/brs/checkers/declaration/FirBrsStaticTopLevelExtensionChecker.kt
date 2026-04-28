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
import org.jetbrains.kotlin.fir.analysis.checkers.isTopLevel
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors
import org.jetbrains.kotlin.fir.declarations.FirReceiverParameter
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.fir.declarations.getAnnotationByClassId
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.isMarkedNullable
import org.jetbrains.kotlin.name.BrsStandardClassIds

/**
 * Reports [FirBrsErrors.BRS_STATIC_INVALID_TARGET] when `@BrsStatic` is placed on a
 * top-level *extension* function.
 *
 * The class-body case is handled by [FirBrsStaticClassChecker]; the same-name overload
 * case by [FirBrsStaticOverloadFileChecker]. This checker fills the gap those two miss:
 * `BrsIrBackendContext.generateBrsFunctionName` strips both the `_k_` mangling and the
 * receiver signature suffix for any `@BrsStatic` function whose IR parent is a
 * `IrPackageFragment`, regardless of whether it has an extension receiver. The result is
 * a flat BrightScript function name keyed only on the Kotlin short name — `fun Foo.bar()`
 * and `fun Baz.bar()` both compile to BrightScript `bar` and silently collide. Even a
 * single occurrence is wrong, since the user gets neither a class prefix nor a receiver
 * disambiguator.
 *
 * Predicate: source-origin, non-fake top-level [FirSimpleFunction] carrying `@BrsStatic`
 * with a non-null `receiverParameter`.
 *
 * `FirSimpleFunctionChecker` runs per-function with the annotation context already pushed,
 * so `@Suppress("BRS_STATIC_INVALID_TARGET")` works automatically without a manual
 * `isSuppressedByAnnotation` helper (same pattern as [FirBrsIntrinsicUserDefinedChecker]).
 */
object FirBrsStaticTopLevelExtensionChecker : FirSimpleFunctionChecker(MppCheckerKind.Common) {

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirSimpleFunction) {
        if (!declaration.origin.fromSource) return
        if (declaration.source?.kind is KtFakeSourceElementKind) return
        if (!context.isTopLevel) return

        val receiver = declaration.receiverParameter ?: return

        declaration.getAnnotationByClassId(BrsStandardClassIds.Annotations.BrsStatic, context.session)
            ?: return

        reporter.reportOn(
            declaration.source,
            FirBrsErrors.BRS_STATIC_INVALID_TARGET,
            receiver.shortName(),
        )
    }

    private fun FirReceiverParameter.shortName(): String {
        val cone = typeRef.coneType
        val base = cone.classId?.shortClassName?.asString() ?: cone.toString()
        return if (cone.isMarkedNullable) "$base?" else base
    }
}
