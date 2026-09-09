/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.brs.checkers.declaration

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.brs.checkers.BrsComponentTypes
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirPropertyChecker
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol

/**
 * BRS_TASK_CONSTRUCTOR_INPUT (spec 2026-09-04-component-lifecycle §7): a constructor-
 * parameter `@SG*Field`/`@BrsField` property on a TaskComponent (abstract task bases
 * included). Task inputs are configured through `runTask<T> { field = value }` on a fresh
 * node; constructor inputs are a render-component contract (v1). A PLAIN constructor val
 * on a task is the sibling rule's business (BRS_TASK_STATE_NOT_FIELD, m-state lost across
 * the task-thread clone).
 */
object FirBrsTaskConstructorInputChecker : FirPropertyChecker(MppCheckerKind.Common) {

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirProperty) {
        val session = context.session
        if (!BrsComponentTypes.isConstructorInput(declaration.symbol, session)) return
        // Member properties only: for a member the nearest containing declaration is the class.
        val containingClass = context.containingDeclarations.lastOrNull() as? FirRegularClassSymbol ?: return
        if (!BrsComponentTypes.isTaskComponentClass(containingClass, session)) return

        reporter.reportOn(
            declaration.source,
            FirBrsErrors.BRS_TASK_CONSTRUCTOR_INPUT,
            declaration.name.asString(),
            containingClass.classId.shortClassName.asString(),
        )
    }
}
