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
import org.jetbrains.kotlin.fir.expressions.FirAnonymousFunctionExpression
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirWrappedArgumentExpression
import org.jetbrains.kotlin.fir.references.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.types.isSuspendOrKSuspendFunctionType
import org.jetbrains.kotlin.name.BrsStandardClassIds

/**
 * Guards the compiler-lowered surface of `ScopeHandle.run { block }`: the block
 * overload is rewritten at IR phase (BrsScopeRunBlockLowering) into a named
 * request plus a lifted top-level function, and that lift is only possible when
 * the argument is a literal lambda at the call site. A stored function value
 * (or a function reference) has no body to lift — the un-rewritten call falls
 * through to the stdlib backstop, which throws IllegalStateException at runtime.
 * This checker turns that runtime failure into a FIR error.
 *
 * Fires only on the block overload — the single value parameter of suspend
 * function type. The `run(request, args...)` overloads take ScopeRequest
 * subclasses and are never lowered.
 *
 * Suppressible: `@Suppress("BRS_SCOPE_BLOCK_NOT_LITERAL")` opts into the
 * runtime backstop (deliberate escape hatch; the ISE message names this rule).
 */
object FirBrsScopeBlockChecker : FirFunctionCallChecker(MppCheckerKind.Common) {

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(expression: FirFunctionCall) {
        val callee = expression.calleeReference.toResolvedCallableSymbol() ?: return
        if (callee.callableId != BrsStandardClassIds.Callables.scopeHandleRun) return
        val functionSymbol = callee as? FirNamedFunctionSymbol ?: return
        val blockParameter = functionSymbol.valueParameterSymbols.singleOrNull() ?: return
        if (!blockParameter.resolvedReturnType.isSuspendOrKSuspendFunctionType(context.session)) return

        var argument = expression.argumentList.arguments.firstOrNull() ?: return
        if (argument is FirWrappedArgumentExpression) argument = argument.expression
        if (argument is FirAnonymousFunctionExpression) return

        reporter.reportOn(argument.source ?: expression.source, FirBrsErrors.BRS_SCOPE_BLOCK_NOT_LITERAL)
    }
}
