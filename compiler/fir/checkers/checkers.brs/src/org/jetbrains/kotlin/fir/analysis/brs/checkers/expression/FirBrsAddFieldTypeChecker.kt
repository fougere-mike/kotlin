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
 * FIR checker for `ISGNodeField.addField(fieldName, type, alwaysNotify)`.
 *
 * Fires [FirBrsErrors.BRS_ADDFIELD_INVALID_TYPE] when the `type` argument is a
 * compile-time-constant string that is not one of the 14 canonical BrightScript
 * SceneGraph field type names (all lowercase: "string", "integer", etc.).
 *
 * **Literal-required-narrow policy**: the diagnostic fires *only* when the `type`
 * argument can be evaluated at compile time. Dynamic (non-constant) strings are
 * silently skipped — no diagnostic, no error. This is asymmetric with
 * `BRS_INTRINSIC_LITERAL_REQUIRED`, which always requires a literal. The asymmetry
 * is intentional: BrightScript's runtime `addField()` accepts any string, so it
 * is legitimate to pass a runtime-determined type string; we only flag cases where
 * we can prove at compile time that the type name is wrong.
 */
object FirBrsAddFieldTypeChecker : FirFunctionCallChecker(MppCheckerKind.Common) {

    private val addFieldCallables = BrsStandardClassIds.Callables.addFieldCallables
    private val validTypes: Set<String> =
        BrsStandardClassIds.Annotations.sgFieldAnnotationTypes.values.toSet()
    private val validTypesDisplay: String =
        validTypes.sorted().joinToString(", ") { "\"$it\"" }

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(expression: FirFunctionCall) {
        val callableId = expression.calleeReference.toResolvedCallableSymbol()?.callableId
        if (callableId !in addFieldCallables) return

        // Extract the `type` parameter argument by name from the resolved argument list.
        val resolvedArgList = expression.argumentList as? FirResolvedArgumentList ?: return
        val typeArg = resolvedArgList.mapping.entries
            .firstOrNull { (_, param) -> param.name.identifier == "type" }
            ?.key
            ?: return

        // If the argument is not a String type (e.g. type error in user code), skip silently.
        if (!typeArg.resolvedType.isString) return

        // Literal-required-narrow: only flag compile-time-constant strings.
        if (!canBeEvaluatedAtCompileTime(typeArg, context.session, allowErrors = false, calledOnCheckerStage = true)) return

        // Extract the actual string value from the constant expression.
        val actualType = extractStringValue(typeArg) ?: return

        // Check against the valid set.
        if (actualType in validTypes) return

        reporter.reportOn(
            typeArg.source ?: expression.source,
            FirBrsErrors.BRS_ADDFIELD_INVALID_TYPE,
            actualType,
            validTypesDisplay,
        )
    }

    /**
     * Extracts a String value from a compile-time-constant expression.
     *
     * - Direct [FirLiteralExpression]: cast `value` to String.
     * - [FirPropertyAccessExpression] to a `const val`: look up the initializer as
     *   a [FirLiteralExpression] and cast its value.
     * - Otherwise: return null (silent skip — not enough information at compile time).
     */
    private fun extractStringValue(expression: FirExpression): String? {
        return when (expression) {
            is FirLiteralExpression -> expression.value as? String
            is FirPropertyAccessExpression -> {
                val sym = expression.calleeReference.toResolvedCallableSymbol() as? FirPropertySymbol
                    ?: return null
                if (!sym.isConst) return null
                val initializer = sym.fir.initializer as? FirLiteralExpression ?: return null
                initializer.value as? String
            }
            else -> null
        }
    }
}
