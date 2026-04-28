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
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirBasicDeclarationChecker
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.getAnnotationByClassId
import org.jetbrains.kotlin.fir.declarations.getStringArgument
import org.jetbrains.kotlin.name.BrsStandardClassIds
import org.jetbrains.kotlin.name.Name

/**
 * Rejects `@BrsCreateObject("BadType")` annotations whose `typeName` argument is not in
 * the 24-element canonical BrightScript object type set.
 *
 * A wrong type name silently produces `CreateObject("BadType", …)` which Roku rejects
 * at runtime. Rejecting here at FIR gives a stable, suppressible diagnostic ID and IDE
 * squiggle, matching the call-site twin [FirBrsErrors.BRS_CREATE_OBJECT_INVALID_TYPE].
 *
 * Annotation args are always compile-time constants, so no `canBeEvaluatedAtCompileTime`
 * gate is needed — unlike the call-site checker.
 */
object FirBrsCreateObjectAnnotationChecker : FirBasicDeclarationChecker(MppCheckerKind.Common) {

    private val TYPE_NAME_ARG = Name.identifier("typeName")
    private val validTypes: Set<String> = BrsStandardClassIds.brsCreateObjectValidTypes
    private val validTypesDisplay: String =
        validTypes.sorted().joinToString(", ") { "\"$it\"" }

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirDeclaration) {
        if (declaration.source?.kind is KtFakeSourceElementKind) return

        val annotation = declaration.getAnnotationByClassId(
            BrsStandardClassIds.Annotations.BrsCreateObject,
            context.session,
        ) ?: return

        val arg = annotation.getStringArgument(TYPE_NAME_ARG, context.session) ?: return
        if (arg in validTypes) return

        reporter.reportOn(
            declaration.source,
            FirBrsErrors.BRS_BRSCREATEOBJECT_INVALID_TYPE,
            arg,
            validTypesDisplay,
        )
    }
}
