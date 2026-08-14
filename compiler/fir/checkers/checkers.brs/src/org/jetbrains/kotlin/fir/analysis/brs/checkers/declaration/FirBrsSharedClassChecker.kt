/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.brs.checkers.declaration

import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.brs.checkers.BrsSharedServiceTypes
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirPropertyChecker
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirRegularClassChecker
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.fir.declarations.utils.isOpen
import org.jetbrains.kotlin.fir.resolve.fullyExpandedType
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.isSomeFunctionType

/**
 * The declaration-shape half of the SharedService rule family (spec §5): purely
 * structural checks over hierarchies reaching `kotlin.brs.SharedService`
 * ([BrsSharedServiceTypes]). Abstract bases with state, concrete members,
 * overrides, and abstract hooks are LEGAL — there is deliberately no
 * member-free rule, no flat-hierarchy rule, and no name knowledge.
 *
 * [FirBrsSharedClassNotFinalChecker] — [FirBrsErrors.BRS_SHARED_CLASS_NOT_FINAL]
 * (error) on a CONCRETE shared class declared `open`. Phase 3's static dispatch
 * generates per-method dispatchers that enumerate the concrete leaves of a
 * shared hierarchy (closed world, single module); an open concrete class
 * invites leaves the enumeration contract cannot promise to see. Kotlin's
 * default (final) passes silently; abstract and sealed classes are not
 * concrete and pass.
 *
 * [FirBrsSharedFnPropertyChecker] — [FirBrsErrors.BRS_SHARED_FN_PROPERTY]
 * (error) on a function-typed property ANYWHERE in a shared hierarchy
 * (abstract base or concrete leaf, `val` or `var`, nullable or not, suspend
 * included). A stored callback is a function reference in the shared bag —
 * the one shape static dispatch cannot rescue: methods become extension-shaped
 * static calls, but a function VALUE in the instance's data dies with the
 * fn-slot machinery the design retires.
 */
object FirBrsSharedClassNotFinalChecker : FirRegularClassChecker(MppCheckerKind.Common) {

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirRegularClass) {
        if (declaration.classKind != ClassKind.CLASS) return
        // Modality OPEN only: final (the default) is the required shape; abstract and
        // sealed classes are not concrete leaves and are legal hierarchy interior.
        if (!declaration.isOpen) return
        if (!BrsSharedServiceTypes.isSharedServiceClass(declaration.symbol, context.session)) return

        reporter.reportOn(
            declaration.source,
            FirBrsErrors.BRS_SHARED_CLASS_NOT_FINAL,
            declaration.name.asString(),
        )
    }
}

object FirBrsSharedFnPropertyChecker : FirPropertyChecker(MppCheckerKind.Common) {

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirProperty) {
        val session = context.session
        // Member properties only: for locals the nearest containing declaration is a
        // function. Constructor-parameter properties are NOT skipped — `class Vm(val
        // onDone: () -> Unit)` is the likeliest real-world stored-callback shape.
        val containingClass = context.containingDeclarations.lastOrNull() as? FirClassSymbol<*> ?: return
        if (!BrsSharedServiceTypes.isSharedServiceClass(containingClass, session)) return

        val propertyType = declaration.returnTypeRef.coneType.fullyExpandedType()
        if (!propertyType.isSomeFunctionType(session)) return

        reporter.reportOn(
            declaration.source,
            FirBrsErrors.BRS_SHARED_FN_PROPERTY,
            declaration.name.asString(),
            containingClass.classId.shortClassName.asString(),
        )
    }
}
