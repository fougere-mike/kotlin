/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.brs.checkers.expression

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirTryExpressionChecker
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors
import org.jetbrains.kotlin.fir.declarations.utils.isSuspend
import org.jetbrains.kotlin.fir.expressions.FirTryExpression
import org.jetbrains.kotlin.fir.symbols.impl.FirAnonymousFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.types.isSuspendOrKSuspendFunctionType

/**
 * Stopgap for the BRS backend's non-suspend `visitTry`, which silently DROPS the
 * `finally` block at codegen: any `try` with a `finally` whose nearest containing
 * callable is not suspend is rejected at FIR phase.
 *
 * Suspend function bodies are clean — the suspend state-machine lowering compiles
 * `finally` correctly. A non-suspend lambda INSIDE a suspend function still errors:
 * the lambda compiles as its own non-suspend BRS function, so the nearest callable
 * wins. `try`/`catch` without `finally` never fires.
 *
 * Suppressible: deliberate opt-ins carry `@Suppress("BRS_TRY_FINALLY_UNSUPPORTED")`
 * with a tracking comment. The error retires when codegen emits `finally` on the
 * non-suspend path.
 *
 * Suspend-lambda classification mirrors
 * [org.jetbrains.kotlin.fir.analysis.checkers.expression.FirSuspendCallChecker]'s
 * `findEnclosingSuspendFunction`.
 */
object FirBrsTryFinallyChecker : FirTryExpressionChecker(MppCheckerKind.Common) {

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(expression: FirTryExpression) {
        if (expression.finallyBlock == null) return
        val nearestCallable = context.containingDeclarations.lastOrNull { it is FirFunctionSymbol<*> }
        val isSuspend = when (nearestCallable) {
            is FirAnonymousFunctionSymbol ->
                if (nearestCallable.isLambda) nearestCallable.resolvedTypeRef.coneType.isSuspendOrKSuspendFunctionType(context.session)
                else nearestCallable.isSuspend
            is FirNamedFunctionSymbol -> nearestCallable.isSuspend
            else -> false
        }
        if (!isSuspend) {
            reporter.reportOn(expression.source, FirBrsErrors.BRS_TRY_FINALLY_UNSUPPORTED)
        }
    }
}
