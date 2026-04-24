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
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirFunctionCallChecker
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors
import org.jetbrains.kotlin.fir.declarations.FirAnonymousFunction
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.expressions.FirAnonymousFunctionExpression
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirPropertyAccessExpression
import org.jetbrains.kotlin.fir.expressions.FirResolvedQualifier
import org.jetbrains.kotlin.fir.expressions.FirThisReceiverExpression
import org.jetbrains.kotlin.fir.references.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.resolve.fullyExpandedType
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirReceiverParameterSymbol
import org.jetbrains.kotlin.fir.types.ConeClassLikeType
import org.jetbrains.kotlin.fir.types.ConeErrorType
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.isPrimitive
import org.jetbrains.kotlin.fir.types.isString
import org.jetbrains.kotlin.fir.types.resolvedType
import org.jetbrains.kotlin.fir.types.typeContext
import org.jetbrains.kotlin.fir.types.withNullability
import org.jetbrains.kotlin.fir.visitors.FirVisitorVoid
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * Flags captures inside `withContext(Dispatchers.IO) { ... }` that cannot cross the Task
 * thread boundary at runtime. The IR-backend lowering `BrsIOWorkerExtractionLowering`
 * applies the same rules later in compilation; this checker promotes the analysis to
 * FIR phase so it is visible at edit time and is `@Suppress`-able.
 *
 * Detection is deliberately limited to direct `Dispatchers.IO` / `IODispatcher`
 * references — context composition (`Dispatchers.IO + X`) or aliased dispatchers
 * (`val d = Dispatchers.IO; withContext(d) { ... }`) are NOT recognized. This matches
 * the IR lowering and avoids analysis complexity that would not change the runtime
 * failure mode.
 */
object FirBrsIOWorkerCaptureChecker : FirFunctionCallChecker(MppCheckerKind.Common) {

    private val BUILDERS_PACKAGE = FqName("kotlin.coroutines.builders")
    private val DISPATCHERS_PACKAGE = FqName("kotlin.coroutines.dispatchers")
    private val WITH_CONTEXT_CALLABLE = CallableId(BUILDERS_PACKAGE, Name.identifier("withContext"))
    private val DISPATCHERS_CLASS = ClassId(DISPATCHERS_PACKAGE, Name.identifier("Dispatchers"))
    private val IO_DISPATCHER_CLASS = ClassId(DISPATCHERS_PACKAGE, Name.identifier("IODispatcher"))
    private val IO_PROPERTY_CALLABLE = CallableId(DISPATCHERS_CLASS, Name.identifier("IO"))

    private val DYNAMIC_FQ_NAME = FqName("kotlin.brs.Dynamic")
    private val SERIALIZABLE_SHORT_NAMES = setOf(
        "Int", "Long", "Float", "Double", "Boolean", "String",
        "RoArray", "RoAssociativeArray", "Dynamic",
    )

    /** A finding collected during lambda-body walk, before reporting. */
    private data class Capture(val source: KtSourceElement?, val name: String, val typeName: String)

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(expression: FirFunctionCall) {
        if (expression.calleeReference.toResolvedCallableSymbol()?.callableId != WITH_CONTEXT_CALLABLE) return
        val args = expression.argumentList.arguments
        if (args.size < 2) return
        if (!isDispatchersIO(args[0])) return
        val lambda = (args[1] as? FirAnonymousFunctionExpression)?.anonymousFunction ?: return

        val findings = collectCaptures(lambda, context.session)
        for (finding in findings) {
            reporter.reportOn(
                finding.source,
                FirBrsErrors.BRS_IO_WORKER_NON_SERIALIZABLE_CAPTURE,
                finding.name,
                finding.typeName,
            )
        }
    }

    private fun isDispatchersIO(arg: FirExpression): Boolean {
        if (arg is FirPropertyAccessExpression &&
            arg.calleeReference.toResolvedCallableSymbol()?.callableId == IO_PROPERTY_CALLABLE
        ) return true
        if (arg is FirResolvedQualifier && arg.classId == IO_DISPATCHER_CLASS) return true
        return false
    }

    private fun collectCaptures(
        lambda: FirAnonymousFunction,
        session: FirSession,
    ): List<Capture> {
        val lambdaReceiverSymbol: FirReceiverParameterSymbol? = lambda.receiverParameter?.symbol
        val paramsAndLocals: MutableSet<FirBasedSymbol<*>> = mutableSetOf<FirBasedSymbol<*>>().apply {
            lambda.valueParameters.mapTo(this) { it.symbol }
            lambdaReceiverSymbol?.let(::add)
        }

        val findings = mutableListOf<Capture>()

        lambda.body?.accept(object : FirVisitorVoid() {
            override fun visitElement(element: FirElement) {
                element.acceptChildren(this)
            }

            override fun visitProperty(property: FirProperty) {
                paramsAndLocals += property.symbol
                super.visitProperty(property)
            }

            override fun visitAnonymousFunction(anonymousFunction: FirAnonymousFunction) {
                // Do not descend into nested lambdas — variables captured there stay within
                // that lambda's scope and do not cross this withContext(Dispatchers.IO) boundary.
            }

            override fun visitPropertyAccessExpression(propertyAccessExpression: FirPropertyAccessExpression) {
                val sym = propertyAccessExpression.calleeReference.toResolvedCallableSymbol()
                if (sym is FirPropertySymbol) {
                    if (sym.isLocal) {
                        // Local variable captured from outer scope (not a lambda param or tracked local)
                        if (sym !in paramsAndLocals) {
                            val type = propertyAccessExpression.resolvedType.fullyExpandedType(session)
                            if (!type.isSerializableForIOWorker(session)) {
                                findings += Capture(
                                    source = propertyAccessExpression.source ?: lambda.source,
                                    name = sym.name.asString(),
                                    typeName = type.renderTypeId(),
                                )
                            }
                        }
                    } else {
                        // Non-local (class member) accessed via implicit or explicit `this` of an
                        // outer class. The whole instance crosses the thread boundary — flag the access.
                        val dispatchReceiver = propertyAccessExpression.dispatchReceiver
                        if (dispatchReceiver is FirThisReceiverExpression) {
                            val thisRef = dispatchReceiver.calleeReference
                            if (thisRef.boundSymbol !== lambdaReceiverSymbol) {
                                val receiverType = dispatchReceiver.resolvedType.fullyExpandedType(session)
                                findings += Capture(
                                    source = propertyAccessExpression.source ?: lambda.source,
                                    name = sym.name.asString(),
                                    typeName = receiverType.renderTypeId(),
                                )
                            }
                        }
                    }
                }
                super.visitPropertyAccessExpression(propertyAccessExpression)
            }
        })

        return findings
    }

    private fun ConeKotlinType.isSerializableForIOWorker(session: FirSession): Boolean {
        if (this is ConeErrorType) return true
        val notNull = withNullability(nullable = false, session.typeContext)
        if (notNull.isPrimitive || notNull.isString) return true
        val classId = (notNull as? ConeClassLikeType)?.lookupTag?.classId ?: return false
        if (classId.shortClassName.asString() in SERIALIZABLE_SHORT_NAMES) return true
        if (classId.asSingleFqName() == DYNAMIC_FQ_NAME) return true
        return false
    }

    private fun ConeKotlinType.renderTypeId(): String {
        val expanded = if (this is ConeErrorType) return "unknown" else this
        return (expanded as? ConeClassLikeType)?.lookupTag?.classId?.asSingleFqName()?.asString()
            ?: expanded.toString()
    }
}
