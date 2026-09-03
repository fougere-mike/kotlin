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
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.declarations.FirReceiverParameter
import org.jetbrains.kotlin.fir.declarations.FirValueParameter
import org.jetbrains.kotlin.fir.declarations.utils.isSuspend
import org.jetbrains.kotlin.fir.expressions.FirAnonymousFunctionExpression
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirImplicitInvokeCall
import org.jetbrains.kotlin.fir.expressions.FirPropertyAccessExpression
import org.jetbrains.kotlin.fir.expressions.FirThisReceiverExpression
import org.jetbrains.kotlin.fir.expressions.FirVariableAssignment
import org.jetbrains.kotlin.fir.expressions.unwrapArgument
import org.jetbrains.kotlin.fir.expressions.unwrapLValue
import org.jetbrains.kotlin.fir.references.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.resolve.fullyExpandedType
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirValueParameterSymbol
import org.jetbrains.kotlin.fir.types.ConeClassLikeType
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.isSomeFunctionType
import org.jetbrains.kotlin.fir.types.resolvedType
import org.jetbrains.kotlin.fir.types.type
import org.jetbrains.kotlin.fir.visitors.FirVisitorVoid
import org.jetbrains.kotlin.name.BrsStandardClassIds
import org.jetbrains.kotlin.name.CallableId

/**
 * The lifted-region diagnostics family of the task lift (flow-program spec
 * §5/§7). At a `flowOn(Dispatchers.Task)` call site the compiler lifts the
 * ENTIRE upstream chain expression into a per-call-site synthesized
 * TaskComponent; at a `spawnTask { }` call site it lifts the literal block.
 * This checker rejects at FIR phase what that lift cannot express:
 *
 * - [FirBrsErrors.BRS_FLOW_UPSTREAM_NOT_LITERAL] (error): the lift needs the
 *   region's code literally visible at the call site. The flowOn receiver must
 *   be a literal flow chain — every hop a call whose CallableId is in
 *   [BrsStandardClassIds.Callables.flowLiftChainCallables] (the ONE whitelist
 *   the Task 7 lowering must also consume — keep-in-sync law KDoc'd there),
 *   every functional argument a literal lambda, bottoming out at
 *   flow/flowOf/asFlow (combine continues through its Flow-typed arguments
 *   instead of a receiver). The spawnTask argument must be a literal lambda.
 * - [FirBrsErrors.BRS_TASK_CAPTURE_UNMARSHALLABLE] (error): free values of the
 *   region cross the render→task boundary BY COPY as typed capture fields —
 *   the [BrsScopeMarshallability] oracle (shared with the ScopeHandle family;
 *   never forked) classifies what survives. Class instances — including the
 *   enclosing `this`, captured implicitly by any member access — and function
 *   values are the load-bearing rejections; the message names the
 *   hoist-to-local fix the spec mandates (a lazily-evaluated cold body plus
 *   auto-hoisting would silently change WHEN a property is read; the explicit
 *   hoist keeps evaluation time visible).
 * - [FirBrsErrors.BRS_TASK_CAPTURE_MUTATION_LOST] (warning): assignment to a
 *   captured var inside the region — the write lands on the task-side copy
 *   (mirror of BRS_SCOPE_CAPTURE_MUTATION_LOST).
 * - [FirBrsErrors.BRS_TASK_SUSPEND_IN_LIFTED] (error): the lifted upstream
 *   world is synchronous — the task thread has no coroutine pump, so a real
 *   park can never resume. Only `emit`/`emitAll`
 *   ([BrsStandardClassIds.Callables.flowLiftAllowedSuspendCallables]) suspend.
 *   spawnTask needs no instance of this rule: its block is a NON-suspend
 *   function type, so the ordinary frontend already rejects suspension inside.
 * - [FirBrsErrors.BRS_TASK_EMIT_NOT_MARSHALLABLE] (error): `emit`/`emitAll`
 *   argument types in the region, and the spawnTask call's result type, cross
 *   the task→render envelope hop by copy — same oracle.
 *
 * Under-approximation by design (BrsScopeMarshallability precedent), disclosed
 * holes (spec §7): values pre-erased to Dynamic (an external interface — the
 * marshallable set admits it; note kotlin.Any is a regular class, so an
 * Any-erased value IS flagged); suspend function REFERENCES passed to opaque
 * callees (no suspend CALL is statically visible); generic T-typed captures
 * and emissions (type parameters are unclassifiable). Also skipped rather than
 * guessed: emitAll arguments whose static type is a Flow SUBTYPE (the element
 * type is not directly readable), and smart-cast/safe-call receiver shapes,
 * which simply fail the literal-chain law. Escapes hit the guided-throw
 * runtime backstops (the un-lowered stubs; the shim's suspend guard).
 *
 * All errors suppressible; the region rules run only once the region is
 * literal (a non-literal chain has no well-defined region).
 */
object FirBrsFlowLiftChecker : FirFunctionCallChecker(MppCheckerKind.Common) {

    private const val FLOW_ON_FINDING = "The flowOn receiver is not a literal flow chain"
    private const val FLOW_ON_FIX =
        "Declare the flow chain literally at the flowOn call site: each hop a direct " +
            "flow-builder/operator call with literal lambdas, bottoming out at flow, flowOf, or asFlow."
    private const val SPAWN_TASK_FINDING = "The spawnTask argument is not a literal lambda"
    private const val SPAWN_TASK_FIX =
        "Pass a literal lambda at the spawnTask call site — a stored function value has no body to lift."

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(expression: FirFunctionCall) {
        when (expression.calleeReference.toResolvedCallableSymbol()?.callableId) {
            BrsStandardClassIds.Callables.flowOn -> checkFlowOn(expression)
            BrsStandardClassIds.Callables.spawnTask -> checkSpawnTask(expression)
            else -> {}
        }
    }

    context(context: CheckerContext, reporter: DiagnosticReporter)
    private fun checkFlowOn(expression: FirFunctionCall) {
        val receiver = expression.explicitReceiver
        if (receiver == null) {
            reporter.reportOn(
                expression.source, FirBrsErrors.BRS_FLOW_UPSTREAM_NOT_LITERAL, FLOW_ON_FINDING, FLOW_ON_FIX,
            )
            return
        }
        if (!walkChain(receiver)) return
        reportRegionFindings(walkRegion(receiver, context.session, checkSuspendAndEmissions = true))
    }

    context(context: CheckerContext, reporter: DiagnosticReporter)
    private fun checkSpawnTask(expression: FirFunctionCall) {
        val argument = expression.argumentList.arguments.firstOrNull()?.unwrapArgument() ?: return
        val lambda = (argument as? FirAnonymousFunctionExpression)?.anonymousFunction
        if (lambda == null) {
            reporter.reportOn(
                argument.source ?: expression.source,
                FirBrsErrors.BRS_FLOW_UPSTREAM_NOT_LITERAL, SPAWN_TASK_FINDING, SPAWN_TASK_FIX,
            )
            return
        }

        val resultType = expression.resolvedType
        if (!BrsScopeMarshallability.isMarshallable(resultType, context.session)) {
            reporter.reportOn(
                expression.calleeReference.source ?: expression.source,
                FirBrsErrors.BRS_TASK_EMIT_NOT_MARSHALLABLE,
                "spawnTask result", BrsScopeMarshallability.render(resultType),
            )
        }

        // The block is a non-suspend function type: the ordinary frontend already
        // rejects suspend calls inside it, and emit cannot resolve there.
        reportRegionFindings(walkRegion(lambda, context.session, checkSuspendAndEmissions = false))
    }

    /**
     * Validates [expr] as a literal flow chain, reporting
     * BRS_FLOW_UPSTREAM_NOT_LITERAL on each offending node. Returns true iff
     * the whole chain is literal (the lifted region is well-defined).
     */
    context(context: CheckerContext, reporter: DiagnosticReporter)
    private fun walkChain(expr: FirExpression): Boolean {
        val call = expr as? FirFunctionCall ?: return reportNotLiteralChain(expr.source)
        val id = call.calleeReference.toResolvedCallableSymbol()?.callableId
            ?: return reportNotLiteralChain(call.source)
        if (id !in BrsStandardClassIds.Callables.flowLiftChainCallables) return reportNotLiteralChain(call.source)

        var literal = true
        for (rawArgument in call.argumentList.arguments) {
            val argument = rawArgument.unwrapArgument()
            when {
                argument is FirAnonymousFunctionExpression -> {}
                argument.isFlowTyped(context.session) -> if (!walkChain(argument)) literal = false
                argument.resolvedType.isSomeFunctionType(context.session) -> {
                    reportNotLiteralChain(argument.source)
                    literal = false
                }
                else -> {} // data argument — evaluated task-side; its free values are region captures
            }
        }

        when {
            id in BrsStandardClassIds.Callables.flowBuilderCallables -> {} // bottom of the chain
            id == BrsStandardClassIds.Callables.flowCombine -> {} // receiverless; chain continued via Flow args
            else -> {
                val receiver = call.explicitReceiver
                if (receiver == null) {
                    reportNotLiteralChain(call.source)
                    literal = false
                } else if (!walkChain(receiver)) {
                    literal = false
                }
            }
        }
        return literal
    }

    context(context: CheckerContext, reporter: DiagnosticReporter)
    private fun reportNotLiteralChain(source: KtSourceElement?): Boolean {
        reporter.reportOn(source, FirBrsErrors.BRS_FLOW_UPSTREAM_NOT_LITERAL, FLOW_ON_FINDING, FLOW_ON_FIX)
        return false
    }

    private fun FirExpression.isFlowTyped(session: FirSession): Boolean =
        (resolvedType.fullyExpandedType(session) as? ConeClassLikeType)?.lookupTag?.classId ==
            BrsStandardClassIds.Flow.flowInterface

    context(context: CheckerContext, reporter: DiagnosticReporter)
    private fun reportRegionFindings(findings: List<Finding>) {
        for (finding in findings) {
            when (finding) {
                is Finding.Unmarshallable -> reporter.reportOn(
                    finding.source, FirBrsErrors.BRS_TASK_CAPTURE_UNMARSHALLABLE, finding.name, finding.typeName,
                )
                is Finding.MutationLost -> reporter.reportOn(
                    finding.source, FirBrsErrors.BRS_TASK_CAPTURE_MUTATION_LOST, finding.name,
                )
                is Finding.SuspendCall -> reporter.reportOn(
                    finding.source, FirBrsErrors.BRS_TASK_SUSPEND_IN_LIFTED, finding.name,
                )
                is Finding.Emission -> reporter.reportOn(
                    finding.source, FirBrsErrors.BRS_TASK_EMIT_NOT_MARSHALLABLE, "Emitted", finding.typeName,
                )
            }
        }
    }

    private sealed class Finding {
        class Unmarshallable(val source: KtSourceElement?, val name: String, val typeName: String) : Finding()
        class MutationLost(val source: KtSourceElement?, val name: String) : Finding()
        class SuspendCall(val source: KtSourceElement?, val name: String) : Finding()
        class Emission(val source: KtSourceElement?, val typeName: String) : Finding()
    }

    /**
     * Free-value/suspend/emission analysis over the lifted region — the flowOn
     * receiver expression subtree (hops, data arguments, and every literal
     * lambda), or the spawnTask block. Mirrors FirBrsScopeCaptureChecker's
     * collectCaptures walk: every value reference resolving OUTSIDE the region
     * counts, including from nested lambdas/local functions (the lift marshals
     * those transitively); top-level declarations and singleton objects resolve
     * module-side wherever the lifted code runs and are not captures. One
     * report per captured symbol (first reference in source order); mutations
     * report per write; suspend calls and emissions per call site.
     */
    private fun walkRegion(root: FirElement, session: FirSession, checkSuspendAndEmissions: Boolean): List<Finding> {
        // Symbols DECLARED inside the region (locals, lambda parameters and
        // receivers): references to them never cross the boundary. Declarations
        // always precede their references in the FIR walk, so one pass suffices.
        val declared = mutableSetOf<FirBasedSymbol<*>>()
        val flagged = mutableSetOf<FirBasedSymbol<*>>()
        val findings = mutableListOf<Finding>()
        val fallbackSource = root.source

        fun recordCapture(symbol: FirBasedSymbol<*>, name: String, type: ConeKotlinType, source: KtSourceElement?) {
            if (symbol in declared || !flagged.add(symbol)) return
            if (BrsScopeMarshallability.isMarshallable(type, session)) return
            findings += Finding.Unmarshallable(source ?: fallbackSource, name, BrsScopeMarshallability.render(type))
        }

        fun handleCall(call: FirFunctionCall) {
            if (!checkSuspendAndEmissions) return
            val symbol = call.calleeReference.toResolvedCallableSymbol() as? FirNamedFunctionSymbol ?: return
            val id = symbol.callableId
            when {
                id in BrsStandardClassIds.Callables.flowLiftAllowedSuspendCallables -> {
                    val argument = call.argumentList.arguments.firstOrNull()?.unwrapArgument() ?: return
                    val emitted = emittedType(argument, id, session) ?: return
                    if (!BrsScopeMarshallability.isMarshallable(emitted, session)) {
                        findings += Finding.Emission(
                            argument.source ?: call.source ?: fallbackSource,
                            BrsScopeMarshallability.render(emitted),
                        )
                    }
                }
                symbol.isSuspend -> findings += Finding.SuspendCall(
                    call.source ?: fallbackSource, id.callableName.asString(),
                )
            }
        }

        root.accept(object : FirVisitorVoid() {
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

            override fun visitFunctionCall(functionCall: FirFunctionCall) {
                handleCall(functionCall)
                super.visitFunctionCall(functionCall)
            }

            // FirVisitorVoid delegates implicit-invoke to visitElement, NOT to
            // visitFunctionCall — without this override, `storedSuspendFn()` in
            // the region would escape the suspend rule.
            override fun visitImplicitInvokeCall(implicitInvokeCall: FirImplicitInvokeCall) {
                handleCall(implicitInvokeCall)
                super.visitImplicitInvokeCall(implicitInvokeCall)
            }

            override fun visitThisReceiverExpression(thisReceiverExpression: FirThisReceiverExpression) {
                // Any `this` bound outside the region — the enclosing class instance
                // (explicitly, or implicitly via a member access) or an enclosing
                // lambda/function receiver. The lift marshals it like any capture.
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

    /**
     * The type that crosses the envelope hop for an emission call: the argument
     * itself for `emit`; the Flow argument's element type for `emitAll` (null —
     * skipped, under-approximating — when the static type is a Flow subtype or
     * the element is a star projection).
     */
    private fun emittedType(argument: FirExpression, id: CallableId, session: FirSession): ConeKotlinType? {
        if (id != BrsStandardClassIds.Callables.flowEmitAll) return argument.resolvedType
        val flowType = argument.resolvedType.fullyExpandedType(session) as? ConeClassLikeType ?: return null
        if (flowType.lookupTag.classId != BrsStandardClassIds.Flow.flowInterface) return null
        return flowType.typeArguments.firstOrNull()?.type
    }
}
