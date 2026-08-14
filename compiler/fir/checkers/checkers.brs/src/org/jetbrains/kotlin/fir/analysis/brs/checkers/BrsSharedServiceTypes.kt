/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.brs.checkers

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.resolve.fullyExpandedType
import org.jetbrains.kotlin.fir.resolve.lookupSuperTypes
import org.jetbrains.kotlin.fir.resolve.toRegularClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.types.ConeClassLikeType
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.typeContext
import org.jetbrains.kotlin.fir.types.withNullability
import org.jetbrains.kotlin.name.BrsStandardClassIds

/**
 * The SharedService hierarchy predicate shared by the BRS_SHARED_* checker family
 * (FirBrsSharedClassChecker, FirBrsSharedCopyChannelChecker).
 *
 * `kotlin.brs.SharedService` ([BrsStandardClassIds.Shared.sharedService]) is the SOLE
 * machinery root (design §4.5): no other type name is known to the compiler. Reaching
 * it is a FULL transitive walk over the superclass chain — `lookupSuperTypes(deep =
 * true)` expands type aliases at each hop (computePartialExpansion), so an aliased or
 * deep (abstract → abstract → concrete) hierarchy classifies the same as a direct
 * subclass. `lookupInterfaces = false` because SharedService is a class: it is only
 * reachable through the superclass chain.
 *
 * MIRROR NOTE (Phase 3): the emission switch and dispatch lowering in
 * compiler/ir/backend.brightscript need this same predicate over IR types. The IR
 * mirror lives at `BrsIntrinsics.isSharedServiceClass` / `sharedServiceClassOrNull`
 * (org.jetbrains.kotlin.ir.backend.brs — used by IrToBrsTransformer's
 * extension-shaped emission and BrsSharedDispatchLowering). checkers.brs (FIR/Cone
 * types) and backend.brightscript (IrClass/IrType) share no type-system source, so
 * the walk is MIRRORED there rather than imported — the shared root is the ClassId
 * in [BrsStandardClassIds.Shared]. If the classification here changes, change the
 * IR mirror in the same commit.
 */
internal object BrsSharedServiceTypes {

    /** Whether [symbol] is SharedService or reaches it through the transitive superclass chain. */
    fun isSharedServiceClass(symbol: FirClassSymbol<*>, session: FirSession): Boolean {
        if (symbol.classId == BrsStandardClassIds.Shared.sharedService) return true
        return lookupSuperTypes(symbol, lookupInterfaces = false, deep = true, useSiteSession = session)
            .any { it.lookupTag.classId == BrsStandardClassIds.Shared.sharedService }
    }

    /**
     * The SharedService-reaching class of [type] (alias-expanded, nullability-stripped),
     * or null when the type does not reach SharedService. Unclassifiable types (type
     * parameters, error types, non-class types) answer null — the checkers
     * under-approximate rather than false-positive (BrsScopeMarshallability precedent).
     */
    fun sharedClassSymbolOrNull(type: ConeKotlinType, session: FirSession): FirRegularClassSymbol? {
        val expanded = type.fullyExpandedType(session).withNullability(nullable = false, session.typeContext)
        val symbol = (expanded as? ConeClassLikeType)?.toRegularClassSymbol(session) ?: return null
        return symbol.takeIf { isSharedServiceClass(it, session) }
    }
}
