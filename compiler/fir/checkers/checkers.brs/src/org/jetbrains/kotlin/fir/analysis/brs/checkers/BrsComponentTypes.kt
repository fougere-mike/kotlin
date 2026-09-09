/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.brs.checkers

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.DirectDeclarationsAccess
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.declarations.toAnnotationClassId
import org.jetbrains.kotlin.fir.declarations.utils.fromPrimaryConstructor
import org.jetbrains.kotlin.fir.resolve.lookupSuperTypes
import org.jetbrains.kotlin.fir.resolve.toRegularClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.name.BrsStandardClassIds
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name

/**
 * FIR-side component predicates shared by the checkers.brs family (spec
 * 2026-09-04-component-lifecycle §7).
 *
 * MIRROR NOTE: [isComponentClass] mirrors the backend's `BrsComponentExtractor.isComponent`
 * (`@BrsComponent` on the class itself, or `@BrsSceneGraphComponent` on a strict supertype)
 * and `BrsIntrinsics.isSceneGraphComponent`; [constructorInputs] mirrors
 * `BrsIntrinsics.constructorInputs` (= `isConstructorParameterProperty` +
 * `BrsIntrinsics.hasInterfaceFieldAnnotation`), the ONE backend definition the extractor's
 * `requiredInputs` and the constructor-call lowering both consume (the class's OWN
 * primary-constructor `@SG*Field`/`@BrsField` properties — inherited inputs are not
 * enumerated on either side); [fieldAnnotationIds] mirrors
 * `BrsIntrinsics.interfaceFieldAnnotationIds`. The module boundary forbids importing the backend
 * predicates here (BrsSharedServiceTypes ↔ BrsIntrinsics.isSharedServiceClass precedent),
 * so a change to what counts as a component, or as a constructor input, lands in every
 * mirrored site in the same commit.
 *
 * CROSS-MODULE DISCLOSURE: `fromPrimaryConstructor` is set only by the raw FIR builders,
 * never by fir-deserialization, so an input-bearing component type from a DEPENDENCY klib is
 * never seen as having constructor inputs — BRS_CREATE_COMPONENT_HAS_INPUTS is silently clean
 * for it (the single-module closed-world convention, same as ScopeHandle `run {}` blocks,
 * SharedService dispatchers, and Flow lifts).
 */
object BrsComponentTypes {
    /**
     * `@SG*Field` + `@BrsField` — the annotations that make a property a node field.
     * MIRROR of the backend's `BrsIntrinsics.interfaceFieldAnnotationIds` (same expression).
     */
    val fieldAnnotationIds: Set<ClassId> =
        BrsStandardClassIds.Annotations.sgFieldAnnotationTypes.keys + BrsStandardClassIds.Annotations.BrsField

    /**
     * `@BrsComponent` on the class itself, or `@BrsSceneGraphComponent` on any strict
     * supertype (the base classes carry the annotation but are not themselves
     * instantiable components).
     */
    fun isComponentClass(classSymbol: FirRegularClassSymbol, session: FirSession): Boolean {
        if (classSymbol.hasAnnotation(BrsStandardClassIds.Annotations.BrsComponent, session)) return true
        return lookupSuperTypes(classSymbol, lookupInterfaces = false, deep = true, useSiteSession = session)
            .any { superType ->
                superType.toRegularClassSymbol(session)
                    ?.hasAnnotation(BrsStandardClassIds.Annotations.BrsSceneGraphComponent, session) == true
            }
    }

    /** A strict descendant of `kotlin.brs.TaskComponent` (abstract bases included). */
    fun isTaskComponentClass(classSymbol: FirRegularClassSymbol, session: FirSession): Boolean =
        lookupSuperTypes(classSymbol, lookupInterfaces = false, deep = true, useSiteSession = session)
            .any { it.lookupTag.classId == BrsStandardClassIds.Components.TaskComponent }

    /**
     * A constructor input: a property declared as a PRIMARY-constructor `val`/`var`
     * parameter that carries a field annotation (`class X(@SGStringField val a: String)`).
     * The `@SG*Field` annotations target PROPERTY only, so on a constructor parameter the
     * annotation lands on the generated property, never on the value parameter.
     */
    fun isConstructorInput(property: FirPropertySymbol, session: FirSession): Boolean =
        property.fromPrimaryConstructor &&
            property.resolvedAnnotationsWithClassIds.any { it.toAnnotationClassId(session) in fieldAnnotationIds }

    /** The class's own constructor-input properties, in declaration order. */
    @OptIn(DirectDeclarationsAccess::class)
    fun constructorInputs(classSymbol: FirRegularClassSymbol, session: FirSession): List<Name> =
        classSymbol.declarationSymbols.filterIsInstance<FirPropertySymbol>()
            .filter { isConstructorInput(it, session) }
            .map { it.name }
}
