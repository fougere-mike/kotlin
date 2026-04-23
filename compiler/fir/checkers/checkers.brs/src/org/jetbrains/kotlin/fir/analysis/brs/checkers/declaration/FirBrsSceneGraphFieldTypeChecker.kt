/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.brs.checkers.declaration

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirPropertyChecker
import org.jetbrains.kotlin.fir.analysis.diagnostics.brs.FirBrsErrors
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.declarations.toAnnotationClassId
import org.jetbrains.kotlin.fir.resolve.fullyExpandedType
import org.jetbrains.kotlin.fir.renderer.ConeIdShortRenderer
import org.jetbrains.kotlin.fir.renderer.ConeTypeRendererForReadability
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.name.BrsStandardClassIds
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.types.AbstractTypeChecker

private data class ClassIdWithArity(val classId: ClassId, val typeParamCount: Int)

/**
 * Reports `BRS_SCENEGRAPH_FIELD_TYPE` when a property annotated with a type-safe
 * SceneGraph field annotation (e.g. `@SGStringField`) has a Kotlin type that does
 * not match the BrightScript field type implied by that annotation.
 *
 * For example, `@SGStringField val count: Int` is an error because SGStringField
 * requires a `String` property type.
 */
object FirBrsSceneGraphFieldTypeChecker : FirPropertyChecker(MppCheckerKind.Common) {
    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirProperty) {
        val session = context.session
        val sgFieldClassIds = BrsStandardClassIds.Annotations.sgFieldAnnotationTypes.keys
        val sgFieldAnnotations = declaration.annotations.mapNotNull {
            it.toAnnotationClassId(session)?.takeIf { id -> id in sgFieldClassIds }
        }
        if (sgFieldAnnotations.size >= 2) {
            reporter.reportOn(
                declaration.source,
                FirBrsErrors.BRS_SCENEGRAPH_FIELD_CONFLICT,
                sgFieldAnnotations.joinToString(", ") { "@${it.shortClassName.asString()}" },
            )
            return
        }
        val propertyType = declaration.returnTypeRef.coneType.fullyExpandedType()
        for (annotation in declaration.annotations) {
            val annotationClassId = annotation.toAnnotationClassId(session) ?: continue
            val expectation = sgFieldTypeExpectations[annotationClassId] ?: continue
            if (expectation.matches(propertyType, session)) continue
            reporter.reportOn(
                declaration.source,
                FirBrsErrors.BRS_SCENEGRAPH_FIELD_TYPE,
                "@${annotationClassId.shortClassName.asString()}",
                expectation.expectedTypeLabel,
                propertyType.renderForDiagnostic(),
            )
        }
    }
}

private data class SgFieldExpectation(
    val expectedTypeLabel: String,
    val matches: (ConeKotlinType, FirSession) -> Boolean,
)

private val sgFieldTypeExpectations: Map<ClassId, SgFieldExpectation> = mapOf(
    BrsStandardClassIds.Annotations.SGStringField to SgFieldExpectation("String", exactType(StandardClassIds.String)),
    BrsStandardClassIds.Annotations.SGIntegerField to SgFieldExpectation("Int", exactType(StandardClassIds.Int)),
    BrsStandardClassIds.Annotations.SGLongIntegerField to SgFieldExpectation("Long", exactType(StandardClassIds.Long)),
    BrsStandardClassIds.Annotations.SGFloatField to SgFieldExpectation("Float", exactType(StandardClassIds.Float)),
    BrsStandardClassIds.Annotations.SGDoubleField to SgFieldExpectation("Double", exactType(StandardClassIds.Double)),
    BrsStandardClassIds.Annotations.SGBooleanField to SgFieldExpectation("Boolean", exactType(StandardClassIds.Boolean)),
    BrsStandardClassIds.Annotations.SGArrayField to SgFieldExpectation(
        "RoArray or List",
        subtypeOfAny(
            ClassIdWithArity(BrsStandardClassIds.BuiltIns.roArrayInterface, 0),
            ClassIdWithArity(StandardClassIds.List, 1),
        ),
    ),
    BrsStandardClassIds.Annotations.SGAssocArrayField to SgFieldExpectation(
        "RoAssociativeArray or Map",
        subtypeOfAny(
            ClassIdWithArity(BrsStandardClassIds.BuiltIns.roAssociativeArrayInterface, 0),
            ClassIdWithArity(StandardClassIds.Map, 2),
        ),
    ),
    BrsStandardClassIds.Annotations.SGNodeField to SgFieldExpectation(
        "RoSGNode or subtype",
        subtypeOfAny(ClassIdWithArity(BrsStandardClassIds.BuiltIns.roSGNodeInterface, 0)),
    ),
    BrsStandardClassIds.Annotations.SGFunctionField to SgFieldExpectation("function type", functionType()),
    BrsStandardClassIds.Annotations.SGUriField to SgFieldExpectation("String", exactType(StandardClassIds.String)),
    BrsStandardClassIds.Annotations.SGTimeField to SgFieldExpectation("Double", exactType(StandardClassIds.Double)),
    BrsStandardClassIds.Annotations.SGVector2DField to SgFieldExpectation(
        "FloatArray",
        exactType(StandardClassIds.primitiveArrayTypeByElementType[StandardClassIds.Float]!!),
    ),
    BrsStandardClassIds.Annotations.SGColorField to SgFieldExpectation("String", exactType(StandardClassIds.String)),
)

// ---------------------------------------------------------------------------
// Predicate factories (session is threaded through the lambda at call time)
// ---------------------------------------------------------------------------

private fun exactType(classId: ClassId): (ConeKotlinType, FirSession) -> Boolean = { type, session ->
    val unwrapped = type.withNullability(nullable = false, session.typeContext)
    (unwrapped as? ConeClassLikeType)?.lookupTag?.classId == classId
}

private fun subtypeOfAny(vararg candidates: ClassIdWithArity): (ConeKotlinType, FirSession) -> Boolean = { type, session ->
    val unwrapped = type.withNullability(nullable = false, session.typeContext)
    candidates.any { (classId, arity) ->
        val typeArgs = Array(arity) { ConeStarProjection }
        val target = classId.constructClassLikeType(typeArgs, isMarkedNullable = false)
        AbstractTypeChecker.isSubtypeOf(session.typeContext, unwrapped, target)
    }
}

private fun functionType(): (ConeKotlinType, FirSession) -> Boolean = { type, session ->
    val unwrapped = type.withNullability(nullable = false, session.typeContext)
    unwrapped.isSomeFunctionType(session)
}

// ---------------------------------------------------------------------------
// Rendering helper: uses ConeTypeRendererForReadability with ConeIdShortRenderer
// so that e.g. kotlin.String renders as "String" and kotlin.collections.List<T>
// renders as "List<T>".
// ---------------------------------------------------------------------------

private fun ConeKotlinType.renderForDiagnostic(): String =
    buildString { ConeTypeRendererForReadability(this) { ConeIdShortRenderer() }.render(this@renderForDiagnostic) }

