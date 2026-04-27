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
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.expressions.impl.FirResolvedArgumentList
import org.jetbrains.kotlin.fir.references.toResolvedCallableSymbol
import org.jetbrains.kotlin.name.BrsStandardClassIds
import org.jetbrains.kotlin.name.Name

/**
 * FIR checker for `kotlin.brs.brsName(function)`.
 *
 * Fires [FirBrsErrors.BRS_BRSNAME_REQUIRES_CALLABLE_REF] when the argument is not a
 * syntactic function reference (`::ref`).
 *
 * `brsName` extracts the mangled BrightScript name of a function at compile time. The
 * IR-phase lowering (`BrsIntrinsicLowering`) can only resolve `IrFunctionReference`
 * nodes — anything stored in a variable, passed as a parameter, or expressed as a lambda
 * has already been lowered to an `IrGetValue` or `IrBlock` by the time the lowering runs.
 * This FIR check catches such usages early (IDE squiggle) with a clear message.
 *
 * **Passes:**
 * - `brsName(::topLevel)` — direct function reference
 * - `brsName(SomeClass::method)` — qualified reference
 * - `brsName(this::method)` — bound reference
 * - `brsName(::SomeClass)` — constructor reference (IR handles `IrConstructor` via
 *   `generateBrsFunctionName`)
 *
 * **Fires:**
 * - `brsName({ x -> x })` — lambda (kind: "lambda")
 * - `brsName(storedRef)` — stored callable reference variable (kind: "property reference")
 * - `brsName(funcReturningFun())` — function call result (kind: "function call result")
 * - `brsName(param)` — function-typed parameter (kind: "property reference")
 */
object FirBrsNameCallableRefChecker : FirFunctionCallChecker(MppCheckerKind.Common) {

    private val brsNameCallableId = BrsStandardClassIds.Callables.brsName
    private val FUNCTION_PARAM = Name.identifier("function")

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(expression: FirFunctionCall) {
        if (expression.calleeReference.toResolvedCallableSymbol()?.callableId != brsNameCallableId) return

        // Prefer named-parameter extraction (by "function") for robustness; fall back to positional.
        val arg = (expression.argumentList as? FirResolvedArgumentList)
            ?.mapping
            ?.entries
            ?.firstOrNull { (_, param) -> param.name == FUNCTION_PARAM }
            ?.key
            ?: expression.arguments.firstOrNull()
            ?: return

        // Any FirCallableReferenceAccess (top-level, qualified, bound, or constructor ref) is fine.
        if (arg is FirCallableReferenceAccess) return

        val actualKind = when (arg) {
            is FirAnonymousFunctionExpression -> "lambda"
            is FirPropertyAccessExpression -> "property reference"
            is FirFunctionCall -> "function call result"
            else -> "expression"
        }

        reporter.reportOn(
            arg.source ?: expression.source,
            FirBrsErrors.BRS_BRSNAME_REQUIRES_CALLABLE_REF,
            actualKind,
        )
    }
}
