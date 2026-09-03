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
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirPropertyAccessExpressionChecker
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirPropertyAccessExpression
import org.jetbrains.kotlin.fir.expressions.unwrapArgument
import org.jetbrains.kotlin.fir.references.toResolvedCallableSymbol
import org.jetbrains.kotlin.name.BrsStandardClassIds
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * `BRS_FLOW_ON_INVALID_DISPATCHER`, both directions (flow-program spec decisions
 * 4/10): `Dispatchers.Task` is a compile-time token selecting the task lift — the
 * lowering is chosen at compile time, so `flowOn` requires the LITERAL token at
 * the call site (a runtime-chosen dispatcher cannot select a lowering), and the
 * token is legal NOWHERE else (it is not a runtime dispatcher; its `dispatch()`
 * is a guided-throw backstop).
 *
 * - [FirBrsFlowOnDispatcherArgumentChecker] fires on a `flowOn` call whose
 *   argument is not literally a `Dispatchers.Task` property access.
 * - [FirBrsTaskTokenPositionChecker] fires on any `Dispatchers.Task` property
 *   access whose DIRECT enclosing statement is not a `flowOn` call taking it as
 *   the argument.
 *
 * Detection is identity-based like [FirBrsIODispatcherChecker]: an alias
 * (`val d = Dispatchers.Task`) is flagged at the aliasing site by the position
 * half, and the alias's USE in `flowOn(d)` is flagged by the argument half.
 *
 * Suppressible: `@Suppress("BRS_FLOW_ON_INVALID_DISPATCHER")` opts into the
 * runtime backstops (TaskTokenDispatcher.dispatch / the un-lowered flowOn stub),
 * both of which throw guided errors naming this rule's law.
 */
private object BrsTaskToken {
    private val DISPATCHERS_PACKAGE = FqName("kotlin.coroutines.dispatchers")
    private val DISPATCHERS_CLASS = ClassId(DISPATCHERS_PACKAGE, Name.identifier("Dispatchers"))
    val TASK_PROPERTY_CALLABLE = CallableId(DISPATCHERS_CLASS, Name.identifier("Task"))

    fun isLiteralTaskAccess(expression: FirExpression): Boolean {
        if (expression !is FirPropertyAccessExpression) return false
        return expression.calleeReference.toResolvedCallableSymbol()?.callableId == TASK_PROPERTY_CALLABLE
    }
}

/** The argument half: a `flowOn` call must take the literal `Dispatchers.Task`. */
object FirBrsFlowOnDispatcherArgumentChecker : FirFunctionCallChecker(MppCheckerKind.Common) {

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(expression: FirFunctionCall) {
        val callee = expression.calleeReference.toResolvedCallableSymbol() ?: return
        if (callee.callableId != BrsStandardClassIds.Callables.flowOn) return

        val argument = expression.argumentList.arguments.firstOrNull()?.unwrapArgument() ?: return
        if (BrsTaskToken.isLiteralTaskAccess(argument)) return

        reporter.reportOn(argument.source ?: expression.source, FirBrsErrors.BRS_FLOW_ON_INVALID_DISPATCHER)
    }
}

/** The position half: the literal `Dispatchers.Task` is legal only as a `flowOn` argument. */
object FirBrsTaskTokenPositionChecker : FirPropertyAccessExpressionChecker(MppCheckerKind.Common) {

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(expression: FirPropertyAccessExpression) {
        if (expression.calleeReference.toResolvedCallableSymbol()?.callableId != BrsTaskToken.TASK_PROPERTY_CALLABLE) return

        // Legal position: the DIRECT enclosing call (the innermost entry on the
        // checker context's call stack besides this access itself) is a flowOn
        // call taking this exact expression as its argument. Anything deeper —
        // flowOn(wrap(Dispatchers.Task)) — is not a literal flowOn argument.
        val enclosing = context.callsOrAssignments.lastOrNull { it !== expression }
        if (enclosing is FirFunctionCall &&
            enclosing.calleeReference.toResolvedCallableSymbol()?.callableId == BrsStandardClassIds.Callables.flowOn &&
            enclosing.argumentList.arguments.any { it.unwrapArgument() === expression }
        ) return

        reporter.reportOn(expression.source, FirBrsErrors.BRS_FLOW_ON_INVALID_DISPATCHER)
    }
}
