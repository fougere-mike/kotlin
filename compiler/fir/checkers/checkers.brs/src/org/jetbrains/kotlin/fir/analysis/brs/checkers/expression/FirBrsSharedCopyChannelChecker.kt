/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.brs.checkers.expression

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.brs.checkers.BrsSharedServiceTypes
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirPropertyChecker
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirFunctionCallChecker
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.declarations.toAnnotationClassId
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirWrappedArgumentExpression
import org.jetbrains.kotlin.fir.expressions.impl.FirResolvedArgumentList
import org.jetbrains.kotlin.fir.references.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.resolve.lookupSuperTypes
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.resolvedType
import org.jetbrains.kotlin.name.BrsStandardClassIds
import org.jetbrains.kotlin.name.ClassId

/**
 * The copying-channel halves of [FirBrsErrors.BRS_SHARED_THROUGH_COPYING_CHANNEL]
 * (error): a SharedService-typed value into an ordinary cross-component channel.
 * Every ordinary channel COPIES (device-pinned, scope-handle spike Q1d), and a
 * copy of a shared instance is a husk — data keys survive, every method slot is
 * stripped, and it is NOT the shared instance. Shared instances cross by
 * reference only: pass the stash node and acquire with `sharedFrom<T>()`.
 *
 * Two checkers, one rule (the three channels split by checkable shape):
 *
 * - [FirBrsSharedCopyChannelChecker] (call sites): `setField` value arguments
 *   and `callFunc` arguments whose STATIC type reaches SharedService. The
 *   type-erasing casts `asDynamic()`/`unsafeCast()` are unwrapped first — a
 *   Dynamic-typed parameter (callFunc's arg) forces `value.asDynamic()` at the
 *   call site, which would otherwise hide the shared type. Values already
 *   erased to Any/Dynamic elsewhere are invisible to this checker (static
 *   under-approximation, BrsScopeMarshallability precedent).
 *
 * - [FirBrsSharedTaskFieldChecker] (declarations): an `@SG*Field`/`@BrsField`
 *   property on a TaskComponent-derived class whose declared type reaches
 *   SharedService. The DECLARATION is the checkable shape: an @SG field write
 *   clones across the render/task thread boundary, and a SharedService-typed
 *   declaration is the only way such a write is statically visible (any
 *   assignment into it is already governed by the declared type). The
 *   SceneGraph field-type rule (BRS_SCENEGRAPH_FIELD_TYPE) also fires on the
 *   same declaration — deliberate co-firing; this rule names the right fix.
 *
 * ScopeHandle's paths need NO rule here: the existing CAPTURE/ARG/RESULT
 * marshallability checkers already classify SharedService types as non-external
 * classes (unmarshallable).
 */
object FirBrsSharedCopyChannelChecker : FirFunctionCallChecker(MppCheckerKind.Common) {

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(expression: FirFunctionCall) {
        val callableId = expression.calleeReference.toResolvedCallableSymbol()?.callableId ?: return
        val (parameterName, channel) = when (callableId) {
            in BrsStandardClassIds.Callables.setFieldCallables -> "value" to "a setField value argument"
            in BrsStandardClassIds.Callables.callFuncCallables -> "arg" to "a callFunc argument"
            else -> return
        }

        val resolvedArgList = expression.argumentList as? FirResolvedArgumentList ?: return
        val argument = resolvedArgList.mapping.entries
            .firstOrNull { (_, param) -> param.name.identifier == parameterName }
            ?.key
            ?: return

        val value = argument.unwrapErasingCasts()
        val shared = BrsSharedServiceTypes.sharedClassSymbolOrNull(value.resolvedType, context.session) ?: return

        reporter.reportOn(
            argument.source ?: expression.source,
            FirBrsErrors.BRS_SHARED_THROUGH_COPYING_CHANNEL,
            shared.classId.asSingleFqName().asString(),
            channel,
        )
    }

    /**
     * Unwraps named-argument wrappers and the kotlin.brs type-erasing casts
     * (`asDynamic()`, `unsafeCast<T>()`) down to the underlying value expression.
     */
    private fun FirExpression.unwrapErasingCasts(): FirExpression {
        var current: FirExpression = this
        while (true) {
            if (current is FirWrappedArgumentExpression) {
                current = current.expression
                continue
            }
            if (current is FirFunctionCall) {
                val calleeId = current.calleeReference.toResolvedCallableSymbol()?.callableId
                if (calleeId == BrsStandardClassIds.Callables.asDynamic ||
                    calleeId == BrsStandardClassIds.Callables.unsafeCast
                ) {
                    val receiver = current.explicitReceiver
                    if (receiver != null) {
                        current = receiver
                        continue
                    }
                }
            }
            return current
        }
    }
}

object FirBrsSharedTaskFieldChecker : FirPropertyChecker(MppCheckerKind.Common) {

    private val fieldAnnotationIds: Set<ClassId> =
        BrsStandardClassIds.Annotations.sgFieldAnnotationTypes.keys + BrsStandardClassIds.Annotations.BrsField

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirProperty) {
        val session = context.session
        if (declaration.annotations.none { it.toAnnotationClassId(session) in fieldAnnotationIds }) return

        val containingClass = context.containingDeclarations.lastOrNull() as? FirClassSymbol<*> ?: return
        // Abstract task bases are flagged too: the declaration types the copying
        // channel, and every concrete subclass inherits it.
        val isTaskDerived = lookupSuperTypes(containingClass, lookupInterfaces = false, deep = true, useSiteSession = session)
            .any { it.lookupTag.classId == BrsStandardClassIds.Components.TaskComponent }
        if (!isTaskDerived) return

        val shared = BrsSharedServiceTypes.sharedClassSymbolOrNull(declaration.returnTypeRef.coneType, session) ?: return

        reporter.reportOn(
            declaration.source,
            FirBrsErrors.BRS_SHARED_THROUGH_COPYING_CHANNEL,
            shared.classId.asSingleFqName().asString(),
            "an @SG field declaration on a task component",
        )
    }
}
