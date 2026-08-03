/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.brs.checkers.expression

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirFunctionCallChecker
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.declarations.utils.isAbstract
import org.jetbrains.kotlin.fir.declarations.utils.isInterface
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.references.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.resolve.fullyExpandedType
import org.jetbrains.kotlin.fir.resolve.lookupSuperTypes
import org.jetbrains.kotlin.fir.resolve.toRegularClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.types.ConeTypeParameterType
import org.jetbrains.kotlin.fir.types.FirTypeProjectionWithVariance
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.name.BrsStandardClassIds

/**
 * FIR checker for `kotlin.brs.createComponent<T>()` (with its backend intrinsic
 * `brsCreateComponent<T>()`) and `kotlin.coroutines.task.runTask<T>{}`; the callable
 * set is [BrsStandardClassIds.Callables.componentFactoryCallables].
 *
 * Reports [FirBrsErrors.BRS_CREATE_COMPONENT_INVALID_TYPE] when the reified type
 * argument cannot be instantiated as a SceneGraph node:
 *  - it is an interface (checked before abstract: interfaces carry ABSTRACT modality);
 *  - it is an abstract class (including the component base classes themselves and
 *    sealed classes);
 *  - it is not a component class at all — mirroring `BrsComponentExtractor.isComponent`:
 *    a component either carries `@BrsComponent` itself or has a strict ancestor
 *    annotated `@BrsSceneGraphComponent` (GroupComponent, SceneComponent,
 *    TaskComponent, ...).
 *
 * A type-parameter argument is skipped: the concrete class is checked at the outer
 * reified call site instead. This also keeps the stdlib's own `createComponent`
 * wrapper body (which forwards `T` to `brsCreateComponent<T>()`) clean when the
 * stdlib is compiled with this checker active.
 */
object FirBrsCreateComponentTypeChecker : FirFunctionCallChecker(MppCheckerKind.Common) {

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(expression: FirFunctionCall) {
        val callableId = expression.calleeReference.toResolvedCallableSymbol()?.callableId ?: return
        if (callableId !in BrsStandardClassIds.Callables.componentFactoryCallables) return

        val session = context.session
        val typeProjection = expression.typeArguments.firstOrNull() as? FirTypeProjectionWithVariance ?: return
        val coneType = typeProjection.typeRef.coneType.fullyExpandedType()
        if (coneType is ConeTypeParameterType) return

        val classSymbol = coneType.toRegularClassSymbol(session) ?: return

        val reason = when {
            classSymbol.isInterface -> "it is an interface"
            classSymbol.isAbstract -> "it is an abstract class"
            !isComponentClass(classSymbol, session) ->
                "it has neither a @BrsComponent annotation nor a @BrsSceneGraphComponent base class"
            else -> return
        }

        reporter.reportOn(
            typeProjection.source ?: expression.source,
            FirBrsErrors.BRS_CREATE_COMPONENT_INVALID_TYPE,
            callableId.callableName.asString(),
            classSymbol.classId.shortClassName.asString(),
            reason,
        )
    }

    /**
     * FIR-level mirror of `BrsComponentExtractor.isComponent`: `@BrsComponent` on the
     * class itself, or `@BrsSceneGraphComponent` on any strict supertype (the base
     * classes carry the annotation but are not themselves instantiable components).
     */
    private fun isComponentClass(classSymbol: FirRegularClassSymbol, session: FirSession): Boolean {
        if (classSymbol.hasAnnotation(BrsStandardClassIds.Annotations.BrsComponent, session)) return true
        return lookupSuperTypes(classSymbol, lookupInterfaces = false, deep = true, useSiteSession = session)
            .any { superType ->
                superType.toRegularClassSymbol(session)
                    ?.hasAnnotation(BrsStandardClassIds.Annotations.BrsSceneGraphComponent, session) == true
            }
    }
}
