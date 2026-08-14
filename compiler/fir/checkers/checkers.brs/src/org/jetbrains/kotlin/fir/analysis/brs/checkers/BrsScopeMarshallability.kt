/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.brs.checkers

import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.utils.isExternal
import org.jetbrains.kotlin.fir.resolve.fullyExpandedType
import org.jetbrains.kotlin.fir.resolve.toRegularClassSymbol
import org.jetbrains.kotlin.fir.types.ConeClassLikeType
import org.jetbrains.kotlin.fir.types.ConeErrorType
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.ConeTypeParameterType
import org.jetbrains.kotlin.fir.types.isNothing
import org.jetbrains.kotlin.fir.types.isPrimitive
import org.jetbrains.kotlin.fir.types.isString
import org.jetbrains.kotlin.fir.types.isUnit
import org.jetbrains.kotlin.fir.types.typeContext
import org.jetbrains.kotlin.fir.types.withNullability

/**
 * The ScopeHandle wire contract's marshallable set, shared by the capture,
 * result, and request-declaration checkers (the A.3 diagnostics family).
 *
 * Values cross the component boundary BY COPY over an @SG field write
 * (ComponentMailbox): AA-backed Kotlin objects survive as data husks — their
 * function-pointer slots (methods, equals/copy, invoke) do not survive the
 * copy. Marshallable therefore means "usable after a data-only copy":
 *
 * - primitives (Int/Long/Float/Double/Boolean & friends), String
 * - Unit / Nothing (result-position bookkeeping types; nothing crosses)
 * - external interfaces — native BrightScript objects: Dynamic, RoArray,
 *   RoAssociativeArray, RoSGNode and subtypes, and any other
 *   `external interface` (nodes cross by reference; the rest by native copy)
 *
 * NOT marshallable: function types, component types (the component-`this`
 * hole), and every non-external class type — kotlin collections, data
 * classes, plain classes (fields survive as data; behavior dies).
 *
 * Unclassifiable types (type parameters, error types, unresolvable classes)
 * are treated as marshallable — these checkers under-approximate rather than
 * false-positive.
 */
internal object BrsScopeMarshallability {

    fun isMarshallable(type: ConeKotlinType, session: FirSession): Boolean {
        if (type is ConeErrorType) return true
        val notNull = type.fullyExpandedType(session).withNullability(nullable = false, session.typeContext)
        if (notNull is ConeErrorType || notNull is ConeTypeParameterType) return true
        if (notNull.isPrimitive || notNull.isString || notNull.isUnit || notNull.isNothing) return true
        val classSymbol = (notNull as? ConeClassLikeType)?.toRegularClassSymbol(session) ?: return true
        return classSymbol.isExternal && classSymbol.classKind == ClassKind.INTERFACE
    }

    fun render(type: ConeKotlinType): String {
        if (type is ConeErrorType) return "unknown"
        return (type as? ConeClassLikeType)?.lookupTag?.classId?.asSingleFqName()?.asString() ?: type.toString()
    }
}
