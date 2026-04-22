/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.brs.checkers.expression

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirFunctionCallChecker
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors
import org.jetbrains.kotlin.fir.declarations.utils.isConst
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.references.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.types.isString
import org.jetbrains.kotlin.fir.types.resolvedType
import org.jetbrains.kotlin.fir.visitors.FirVisitorVoid
import org.jetbrains.kotlin.name.BrsStandardClassIds

object FirBrsIntrinsicArgChecker : FirFunctionCallChecker(MppCheckerKind.Common) {
    private val brsCallableId = BrsStandardClassIds.Callables.brs

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(expression: FirFunctionCall) {
        if (expression.calleeReference.toResolvedCallableSymbol()?.callableId != brsCallableId) {
            return
        }

        val codeExpression = expression.arguments.firstOrNull()
        if (codeExpression == null || !codeExpression.resolvedType.isString) {
            reporter.reportOn(codeExpression?.source ?: expression.source, FirBrsErrors.BRS_INTRINSIC_LITERAL_REQUIRED)
            return
        }

        codeExpression.accept(object : FirVisitorVoid() {
            var lastReportedElement: FirElement? = null

            override fun visitElement(element: FirElement) {
                val lastReported = lastReportedElement
                element.acceptChildren(this)
                if (lastReported == lastReportedElement) {
                    if (!canBeEvaluatedAtCompileTime(element as? FirExpression, context.session, allowErrors = true, calledOnCheckerStage = true)) {
                        lastReportedElement = element
                        val source = element.source ?: codeExpression.source
                        reporter.reportOn(source, FirBrsErrors.BRS_INTRINSIC_LITERAL_REQUIRED)
                    }
                }
            }

            override fun visitPropertyAccessExpression(propertyAccessExpression: FirPropertyAccessExpression) {
                if (propertyAccessExpression.calleeReference.toResolvedCallableSymbol()?.isConst != true) {
                    super.visitPropertyAccessExpression(propertyAccessExpression)
                }
            }
        })
    }
}
