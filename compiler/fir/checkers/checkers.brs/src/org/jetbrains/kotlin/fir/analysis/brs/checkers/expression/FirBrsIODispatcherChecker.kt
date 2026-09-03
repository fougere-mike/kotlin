/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.brs.checkers.expression

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirPropertyAccessExpressionChecker
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors
import org.jetbrains.kotlin.fir.expressions.FirPropertyAccessExpression
import org.jetbrains.kotlin.fir.references.toResolvedCallableSymbol
import org.jetbrains.kotlin.name.BrsStandardClassIds
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name

/**
 * Rejects `Dispatchers.IO` in user code: on this platform IO dispatch is not
 * what the name promises — with TaskPool quarantined and never initialized,
 * `IODispatcher.dispatch` silently falls back to the current thread's
 * coroutine queue, i.e. `Dispatchers.IO` behaves exactly like
 * `Dispatchers.Main`. The sanctioned mechanism for background work is a typed
 * task (`runTask<T>`).
 *
 * The error is suppressible: stdlib internals and deliberate opt-ins into the
 * quarantined `withContext(IO)` pipeline carry
 * `@Suppress("BRS_IO_DISPATCHER_UNSUPPORTED")`, and the ioWorker capture
 * checker / extraction lowering continue to apply to such opted-in code.
 *
 * Detection matches [FirBrsIOWorkerCaptureChecker]'s identity-based approach:
 * only direct `Dispatchers.IO` property accesses are flagged — an alias
 * (`val d = Dispatchers.IO`) is flagged at the aliasing site only.
 */
object FirBrsIODispatcherChecker : FirPropertyAccessExpressionChecker(MppCheckerKind.Common) {

    // The Dispatchers ClassId is the shared BrsStandardClassIds.Callables.dispatchersClassId
    // (also consumed by FirBrsTaskDispatcherChecker) — keep both checkers keyed there.
    private val IO_PROPERTY_CALLABLE = CallableId(BrsStandardClassIds.Callables.dispatchersClassId, Name.identifier("IO"))

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(expression: FirPropertyAccessExpression) {
        if (expression.calleeReference.toResolvedCallableSymbol()?.callableId != IO_PROPERTY_CALLABLE) return
        reporter.reportOn(expression.source, FirBrsErrors.BRS_IO_DISPATCHER_UNSUPPORTED)
    }
}
