/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.brs.checkers.declaration

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.brs.checkers.BrsScopeMarshallability
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirRegularClassChecker
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.fir.resolve.fullyExpandedType
import org.jetbrains.kotlin.fir.types.ConeClassLikeType
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.type
import org.jetbrains.kotlin.name.BrsStandardClassIds

/**
 * The declaration-site half of the ScopeHandle A.3 diagnostics family:
 * validates the type arguments of `object X : ScopeRequest/1/2<...>` request
 * declarations against the wire contract's marshallable set
 * ([BrsScopeMarshallability]). Arguments and results cross the component
 * boundary BY COPY (ScopeHandle.run marshals args into an roArray; the result
 * comes back over an @SG field write), so behavior-carrying types die at the
 * boundary no matter what the handler does.
 *
 * - A1/A2 positions → [FirBrsErrors.BRS_SCOPE_ARG_NOT_MARSHALLABLE] (error).
 * - R position → [FirBrsErrors.BRS_SCOPE_RESULT_NOT_DATA] (warning — property
 *   reads compile to direct member reads on this backend, so a data-shaped
 *   result is usable as data; see FirBrsScopeCaptureChecker and the
 *   dataClassPropertyRead golden for the severity evidence).
 *
 * v1 scope: DIRECT ScopeRequest/1/2 supertypes only. A type argument that is
 * itself a type parameter (generic intermediate base) is skipped —
 * unclassifiable, and the concrete subclass of such a base names ScopeRequest
 * only indirectly, which this checker does not chase.
 */
object FirBrsScopeRequestDeclarationChecker : FirRegularClassChecker(MppCheckerKind.Common) {

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirRegularClass) {
        for (superTypeRef in declaration.superTypeRefs) {
            val superType = superTypeRef.coneType.fullyExpandedType() as? ConeClassLikeType ?: continue
            val resultIndex = when (superType.lookupTag.classId) {
                BrsStandardClassIds.Scope.scopeRequest -> 0
                BrsStandardClassIds.Scope.scopeRequest1 -> 1
                BrsStandardClassIds.Scope.scopeRequest2 -> 2
                else -> continue
            }
            val source = superTypeRef.source ?: declaration.source
            for (index in 0 until resultIndex) {
                val argType = superType.typeArguments.getOrNull(index)?.type ?: continue
                if (!BrsScopeMarshallability.isMarshallable(argType, context.session)) {
                    reporter.reportOn(source, FirBrsErrors.BRS_SCOPE_ARG_NOT_MARSHALLABLE, BrsScopeMarshallability.render(argType))
                }
            }
            val resultType = superType.typeArguments.getOrNull(resultIndex)?.type ?: continue
            if (!BrsScopeMarshallability.isMarshallable(resultType, context.session)) {
                reporter.reportOn(source, FirBrsErrors.BRS_SCOPE_RESULT_NOT_DATA, BrsScopeMarshallability.render(resultType))
            }
        }
    }
}
