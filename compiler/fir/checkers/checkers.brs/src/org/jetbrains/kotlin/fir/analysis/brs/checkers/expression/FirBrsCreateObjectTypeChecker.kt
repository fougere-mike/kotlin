/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.brs.checkers.expression

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirFunctionCallChecker
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors
import org.jetbrains.kotlin.fir.declarations.utils.isConst
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirLiteralExpression
import org.jetbrains.kotlin.fir.expressions.FirPropertyAccessExpression
import org.jetbrains.kotlin.fir.expressions.canBeEvaluatedAtCompileTime
import org.jetbrains.kotlin.fir.expressions.impl.FirResolvedArgumentList
import org.jetbrains.kotlin.fir.references.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.types.isString
import org.jetbrains.kotlin.fir.types.resolvedType
import org.jetbrains.kotlin.name.BrsStandardClassIds

/**
 * FIR checker for `kotlin.brs.createObject<T>(type: String, vararg args: Any?): T`.
 *
 * Fires [FirBrsErrors.BRS_CREATE_OBJECT_INVALID_TYPE] when the `type` argument is a
 * compile-time-constant string that is not one of the 24 canonical BrightScript
 * built-in object type names (e.g. "roArray", "roSGNode", …).
 *
 * **Literal-required-narrow policy**: the diagnostic fires *only* when the `type`
 * argument can be evaluated at compile time. Dynamic (non-constant) strings are
 * silently skipped — no diagnostic, no error. This mirrors [FirBrsAddFieldTypeChecker]:
 * BrightScript's runtime `CreateObject()` accepts any string, so it is legitimate to
 * pass a runtime-determined type string; we only flag cases where we can prove at
 * compile time that the type name is wrong.
 *
 * **Case policy**: exact canonical match. `"roArray"` accepted; `"ROARRAY"`,
 * `"roarray"`, `"RoArray"` all rejected. The Roku SDK documents canonical camelCase
 * forms, and consistent casing is a cheap style nudge. Can be relaxed to
 * case-insensitive if false positives emerge in real codebases.
 */
object FirBrsCreateObjectTypeChecker : FirFunctionCallChecker(MppCheckerKind.Common) {

    private val createObjectCallable = BrsStandardClassIds.Callables.createObject
    private val validTypes: Set<String> = BrsStandardClassIds.brsCreateObjectValidTypes
    private val validTypesDisplay: String =
        validTypes.sorted().joinToString(", ") { "\"$it\"" }

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(expression: FirFunctionCall) {
        val callableId = expression.calleeReference.toResolvedCallableSymbol()?.callableId
        if (callableId != createObjectCallable) return

        val resolvedArgList = expression.argumentList as? FirResolvedArgumentList ?: return
        val typeArg = resolvedArgList.mapping.entries
            .firstOrNull { (_, param) -> param.name.identifier == "type" }
            ?.key
            ?: return

        if (!typeArg.resolvedType.isString) return

        if (!canBeEvaluatedAtCompileTime(typeArg, context.session, allowErrors = false, calledOnCheckerStage = true)) return

        val actualType = extractStringValue(typeArg) ?: return

        if (actualType in validTypes) return

        reporter.reportOn(
            typeArg.source ?: expression.source,
            FirBrsErrors.BRS_CREATE_OBJECT_INVALID_TYPE,
            actualType,
            validTypesDisplay,
        )
    }

    private fun extractStringValue(expression: FirExpression): String? {
        return when (expression) {
            is FirLiteralExpression -> expression.value as? String
            is FirPropertyAccessExpression -> {
                val sym = expression.calleeReference.toResolvedCallableSymbol() as? FirPropertySymbol
                    ?: return null
                if (!sym.isConst) return null
                val initializer = sym.resolvedInitializer as? FirLiteralExpression ?: return null
                initializer.value as? String
            }
            else -> null
        }
    }
}
