/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.brs.checkers.expression

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.brs.checkers.BrsComponentTypes
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirPropertyAccessExpressionChecker
import org.jetbrains.kotlin.fir.analysis.checkers.getContainingClassSymbol
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors
import org.jetbrains.kotlin.fir.expressions.FirPropertyAccessExpression
import org.jetbrains.kotlin.fir.expressions.FirThisReceiverExpression
import org.jetbrains.kotlin.fir.references.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirAnonymousInitializerSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol

/**
 * BRS_COMPONENT_INPUT_READ_IN_INIT (spec 2026-09-04-component-lifecycle §7): a DIRECT read
 * of a component's constructor-parameter `@SG` property, through `this` of that component,
 * inside one of ITS init blocks or a sibling property initializer. SceneGraph runs init()
 * inside CreateObject before any field write, so the read sees the declared default, never
 * the constructor argument.
 *
 * Resolution shape (Task 2 review note 2 — investigated, CAUGHT): in K2 an init block and a
 * property initializer see only the PURE primary-constructor parameter scope — parameters
 * that are NOT properties (`BodyResolveContext.withAnonymousInitializer` /
 * `forPropertyInitializer`, both `getPrimaryConstructorPureParametersScope`) — so a `val`
 * parameter's bare name (`"Airing " + airingId`) resolves to the PROPERTY with an implicit
 * `this` dispatch receiver, exactly like `this.airingId`; both spellings reach the same
 * clause (initBlockRead / siblingInitializerRead / initBlockThisRead fixtures). The VALUE
 * PARAMETER is the resolution only in constructor-header positions (the primary
 * constructor's delegated super call, `by`-delegation fields), which are parameter
 * forwards, not init reads — the checker ignores non-property callees deliberately
 * (superDelegateForwardOk fixture).
 *
 * Only the component's OWN read counts: `other.airingId` on a handle is an ordinary
 * node-field read.
 *
 * Context classification (FirUninitializedEnumChecker precedent): the nearest containing
 * init block / property / function on the checker's declaration stack. An anonymous
 * initializer of the owner, or a non-local sibling property of the owner (its initializer)
 * → REPORT. Any function on top — a lambda, a method, a constructor, a property ACCESSOR
 * body — → deferred/legal. DISCLOSED HOLE (under-approximation, the marshallability-checker
 * convention): an inline lambda invoked synchronously in init (`run { airingId }`)
 * classifies as deferred.
 */
object FirBrsComponentInputEarlyReadChecker : FirPropertyAccessExpressionChecker(MppCheckerKind.Common) {

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(expression: FirPropertyAccessExpression) {
        val session = context.session
        val (property, owner) = resolveInputRead(expression, session) ?: return

        val accessedContext = context.containingDeclarations.lastOrNull {
            it is FirAnonymousInitializerSymbol || it is FirPropertySymbol || it is FirFunctionSymbol<*>
        } ?: return

        val inInit = when (accessedContext) {
            is FirAnonymousInitializerSymbol -> accessedContext.getContainingClassSymbol() == owner
            // A sibling initializer. The input's OWN synthetic initializer (the fake-source read
            // that backs `val airingId`) never reaches here: its callee is the VALUE-PARAMETER
            // symbol (FirPropertyFromParameterResolvedNamedReference), filtered by the
            // `as? FirPropertySymbol` in resolveInputRead. The identity guard additionally
            // excludes the property's own initializer as defense-in-depth.
            is FirPropertySymbol ->
                !accessedContext.isLocal && accessedContext != property &&
                    accessedContext.getContainingClassSymbol() == owner
            else -> false
        }
        if (!inInit) return

        reporter.reportOn(
            expression.source,
            FirBrsErrors.BRS_COMPONENT_INPUT_READ_IN_INIT,
            property.name.asString(),
            owner.classId.shortClassName.asString(),
        )
    }

    /**
     * The (input property, owning component) pair behind [expression], or null when the
     * read is not a direct `this` read of a component's constructor input.
     */
    private fun resolveInputRead(
        expression: FirPropertyAccessExpression,
        session: FirSession,
    ): Pair<FirPropertySymbol, FirRegularClassSymbol>? {
        val property = expression.calleeReference.toResolvedCallableSymbol() as? FirPropertySymbol ?: return null
        if (!BrsComponentTypes.isConstructorInput(property, session)) return null
        val owner = property.getContainingClassSymbol() as? FirRegularClassSymbol ?: return null
        if (!BrsComponentTypes.isComponentClass(owner, session)) return null

        // Only the component's OWN read: `other.airingId` on a handle is a node field read.
        val receiver = expression.dispatchReceiver
        if (receiver != null && receiver !is FirThisReceiverExpression) return null
        if (receiver is FirThisReceiverExpression &&
            (receiver.calleeReference.boundSymbol as? FirRegularClassSymbol)?.classId != owner.classId
        ) return null
        return property to owner
    }
}
