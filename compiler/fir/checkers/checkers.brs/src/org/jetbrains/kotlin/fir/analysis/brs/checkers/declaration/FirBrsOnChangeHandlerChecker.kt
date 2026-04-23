/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.brs.checkers.declaration

import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirPropertyChecker
import org.jetbrains.kotlin.fir.analysis.checkers.unsubstitutedScope
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.declarations.getAnnotationByClassId
import org.jetbrains.kotlin.fir.declarations.getStringArgument
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.name.BrsStandardClassIds
import org.jetbrains.kotlin.name.Name

/**
 * Reports `BRS_ONCHANGE_HANDLER_NOT_FOUND` when a property annotated
 * `@BrsOnChange("foo")` refers to a handler name that does not resolve to any
 * member function of the enclosing class (or its supertypes).
 *
 * Makes good on the fallback comment at `BrsComponentExtractor.kt:369-370`
 * ("this will be caught by FIR checker").
 *
 * Scope:
 *  - Only fires when the annotation has a non-empty explicit string argument.
 *  - Skips the `on{PropName}Changed` convention path: absence there means
 *    "no handler configured", not a typo.
 *  - Accepts inherited handlers via `unsubstitutedScope` (walks supertypes).
 *  - Companion-object handlers are NOT accepted — matches the IR extractor's
 *    instance-scope-only resolution at `BrsComponentExtractor.kt:363-367`.
 */
object FirBrsOnChangeHandlerChecker : FirPropertyChecker(MppCheckerKind.Common) {
    private val HANDLER_ARG = Name.identifier("handler")

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirProperty) {
        if (declaration.source?.kind is KtFakeSourceElementKind) return

        val session = context.session
        val annotation = declaration.getAnnotationByClassId(
            BrsStandardClassIds.Annotations.BrsOnChange, session
        ) ?: return
        val handlerName = annotation.getStringArgument(HANDLER_ARG, session) ?: return
        if (handlerName.isEmpty()) return

        val containingClass = context.containingDeclarations.lastOrNull() as? FirClassSymbol<*> ?: return

        var found = false
        containingClass.unsubstitutedScope().processFunctionsByName(Name.identifier(handlerName)) { _ ->
            found = true
        }
        if (found) return

        reporter.reportOn(
            declaration.source,
            FirBrsErrors.BRS_ONCHANGE_HANDLER_NOT_FOUND,
            handlerName,
            containingClass.classId.shortClassName.asString(),
        )
    }
}
