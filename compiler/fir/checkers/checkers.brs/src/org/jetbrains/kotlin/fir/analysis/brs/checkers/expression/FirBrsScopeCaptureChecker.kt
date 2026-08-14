/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.brs.checkers.expression

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.brs.checkers.BrsScopeMarshallability
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirFunctionCallChecker
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors
import org.jetbrains.kotlin.fir.declarations.FirAnonymousFunction
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.declarations.FirReceiverParameter
import org.jetbrains.kotlin.fir.declarations.FirValueParameter
import org.jetbrains.kotlin.fir.expressions.FirAnonymousFunctionExpression
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirPropertyAccessExpression
import org.jetbrains.kotlin.fir.expressions.FirThisReceiverExpression
import org.jetbrains.kotlin.fir.expressions.FirVariableAssignment
import org.jetbrains.kotlin.fir.expressions.FirWrappedArgumentExpression
import org.jetbrains.kotlin.fir.expressions.unwrapLValue
import org.jetbrains.kotlin.fir.references.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirValueParameterSymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.isSuspendOrKSuspendFunctionType
import org.jetbrains.kotlin.fir.types.resolvedType
import org.jetbrains.kotlin.fir.visitors.FirVisitorVoid
import org.jetbrains.kotlin.name.BrsStandardClassIds

/**
 * The capture/result half of the ScopeHandle A.3 diagnostics family, fired on
 * the same call shape FirBrsScopeBlockChecker guards: `handle.run { block }`
 * with a literal lambda (the compiler-lowered surface). The lowering
 * (BrsScopeRunBlockLowering) marshals every free value of the block into an
 * roAssociativeArray that crosses the component boundary BY COPY, and the
 * block's result crosses back the same way — this checker rejects at FIR
 * phase what that copy destroys at runtime:
 *
 * - [FirBrsErrors.BRS_SCOPE_CAPTURE_UNMARSHALLABLE] (error): a captured value
 *   whose type is outside the wire contract's marshallable set
 *   ([BrsScopeMarshallability]) — function types, component/class instances
 *   (including the enclosing `this`, captured implicitly by any member
 *   access), kotlin collections, data classes. Fields survive the copy;
 *   methods and identity do not.
 * - [FirBrsErrors.BRS_SCOPE_CAPTURE_MUTATION_LOST] (warning): assignment to a
 *   captured local `var` inside the block — the write lands on the owner-side
 *   copy and never reaches the caller.
 * - [FirBrsErrors.BRS_SCOPE_RESULT_NOT_DATA] (warning): block result type
 *   outside the marshallable set. WARNING, not error: property reads compile
 *   to direct member reads on this backend (no getter calls), so a data-class
 *   result is usable AS DATA — only behavior (methods/equals/copy) dies. The
 *   severity is pinned by the dataClassPropertyRead golden.
 *
 * Free-value analysis mirrors the lowering's collectCaptures: every value
 * reference resolving OUTSIDE the block counts, including references made
 * from nested lambdas/local functions inside the block (the lowering lifts
 * those transitively). References to top-level declarations and singleton
 * objects are not captures — they resolve module-side wherever the lifted
 * block runs. One report per captured symbol (first reference in source
 * order); assignments report per write site.
 */
object FirBrsScopeCaptureChecker : FirFunctionCallChecker(MppCheckerKind.Common) {

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(expression: FirFunctionCall) {
        val callee = expression.calleeReference.toResolvedCallableSymbol() ?: return
        if (callee.callableId != BrsStandardClassIds.Callables.scopeHandleRun) return
        val functionSymbol = callee as? FirNamedFunctionSymbol ?: return
        val blockParameter = functionSymbol.valueParameterSymbols.singleOrNull() ?: return
        if (!blockParameter.resolvedReturnType.isSuspendOrKSuspendFunctionType(context.session)) return

        var argument = expression.argumentList.arguments.firstOrNull() ?: return
        if (argument is FirWrappedArgumentExpression) argument = argument.expression
        // Non-literal blocks are BRS_SCOPE_BLOCK_NOT_LITERAL's territory.
        val lambda = (argument as? FirAnonymousFunctionExpression)?.anonymousFunction ?: return

        val resultType = expression.resolvedType
        if (!BrsScopeMarshallability.isMarshallable(resultType, context.session)) {
            reporter.reportOn(
                expression.calleeReference.source ?: expression.source,
                FirBrsErrors.BRS_SCOPE_RESULT_NOT_DATA,
                BrsScopeMarshallability.render(resultType),
            )
        }

        for (finding in walkBlock(lambda, context.session)) {
            when (finding) {
                is Finding.Unmarshallable -> reporter.reportOn(
                    finding.source,
                    FirBrsErrors.BRS_SCOPE_CAPTURE_UNMARSHALLABLE,
                    finding.name,
                    finding.typeName,
                )
                is Finding.MutationLost -> reporter.reportOn(
                    finding.source,
                    FirBrsErrors.BRS_SCOPE_CAPTURE_MUTATION_LOST,
                    finding.name,
                )
            }
        }
    }

    private sealed class Finding {
        class Unmarshallable(val source: KtSourceElement?, val name: String, val typeName: String) : Finding()
        class MutationLost(val source: KtSourceElement?, val name: String) : Finding()
    }

    private fun walkBlock(lambda: FirAnonymousFunction, session: FirSession): List<Finding> {
        // Symbols DECLARED inside the block (locals, nested-function parameters and
        // receivers): references to them never cross the boundary. Declarations
        // always precede their references in the FIR walk, so one pass suffices.
        val declared = mutableSetOf<FirBasedSymbol<*>>()
        val flagged = mutableSetOf<FirBasedSymbol<*>>()
        val findings = mutableListOf<Finding>()
        val fallbackSource = lambda.source

        fun recordCapture(symbol: FirBasedSymbol<*>, name: String, type: ConeKotlinType, source: KtSourceElement?) {
            if (symbol in declared || !flagged.add(symbol)) return
            if (BrsScopeMarshallability.isMarshallable(type, session)) return
            findings += Finding.Unmarshallable(source ?: fallbackSource, name, BrsScopeMarshallability.render(type))
        }

        lambda.accept(object : FirVisitorVoid() {
            override fun visitElement(element: FirElement) {
                element.acceptChildren(this)
            }

            override fun visitProperty(property: FirProperty) {
                declared += property.symbol
                super.visitProperty(property)
            }

            override fun visitValueParameter(valueParameter: FirValueParameter) {
                declared += valueParameter.symbol
                super.visitValueParameter(valueParameter)
            }

            override fun visitReceiverParameter(receiverParameter: FirReceiverParameter) {
                declared += receiverParameter.symbol
                super.visitReceiverParameter(receiverParameter)
            }

            override fun visitThisReceiverExpression(thisReceiverExpression: FirThisReceiverExpression) {
                // Any `this` bound outside the block — the enclosing class instance
                // (explicitly, or implicitly via a member access) or an enclosing
                // lambda/function receiver. The lowering marshals it like any capture.
                val bound = thisReceiverExpression.calleeReference.boundSymbol as? FirBasedSymbol<*>
                if (bound != null) {
                    recordCapture(bound, "this", thisReceiverExpression.resolvedType, thisReceiverExpression.source)
                }
                super.visitThisReceiverExpression(thisReceiverExpression)
            }

            override fun visitPropertyAccessExpression(propertyAccessExpression: FirPropertyAccessExpression) {
                val symbol = propertyAccessExpression.calleeReference.toResolvedCallableSymbol()
                val isOuterValue = when (symbol) {
                    is FirValueParameterSymbol -> true
                    is FirPropertySymbol -> symbol.isLocal
                    else -> false
                }
                if (isOuterValue && symbol != null) {
                    recordCapture(
                        symbol, symbol.name.asString(),
                        propertyAccessExpression.resolvedType, propertyAccessExpression.source,
                    )
                }
                super.visitPropertyAccessExpression(propertyAccessExpression)
            }

            override fun visitVariableAssignment(variableAssignment: FirVariableAssignment) {
                val lValue = variableAssignment.unwrapLValue()
                val symbol = lValue?.calleeReference?.toResolvedCallableSymbol()
                if (symbol is FirPropertySymbol && symbol.isLocal && symbol !in declared) {
                    findings += Finding.MutationLost(
                        lValue.source ?: variableAssignment.source ?: fallbackSource,
                        symbol.name.asString(),
                    )
                }
                super.visitVariableAssignment(variableAssignment)
            }
        })

        return findings
    }
}
