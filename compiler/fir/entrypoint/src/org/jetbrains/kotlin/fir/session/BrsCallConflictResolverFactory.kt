/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.session

import org.jetbrains.kotlin.brs.resolve.BrsTypeSpecificityComparatorWithoutDelegate
import org.jetbrains.kotlin.fir.NoMutableState
import org.jetbrains.kotlin.fir.resolve.BodyResolveComponents
import org.jetbrains.kotlin.fir.resolve.calls.overloads.ConeCallConflictResolverFactory
import org.jetbrains.kotlin.fir.resolve.calls.overloads.ConeCompositeConflictResolver
import org.jetbrains.kotlin.fir.resolve.calls.overloads.ConeEquivalentCallConflictResolver
import org.jetbrains.kotlin.fir.resolve.calls.overloads.ConeIntegerOperatorConflictResolver
import org.jetbrains.kotlin.fir.resolve.calls.overloads.ConeOverloadConflictResolver
import org.jetbrains.kotlin.fir.resolve.inference.InferenceComponents
import org.jetbrains.kotlin.fir.types.typeContext
import org.jetbrains.kotlin.resolve.calls.results.TypeSpecificityComparator

/**
 * Call conflict resolver factory for BrightScript.
 *
 * Uses BrightScript-specific type specificity rules for overload resolution.
 * Since BrightScript doesn't have dynamic types like JavaScript, the
 * specificity comparator is simpler.
 */
@NoMutableState
object BrsCallConflictResolverFactory : ConeCallConflictResolverFactory() {
    override fun create(
        typeSpecificityComparator: TypeSpecificityComparator,
        components: InferenceComponents,
        transformerComponents: BodyResolveComponents
    ): ConeCompositeConflictResolver {
        val specificityComparator = BrsTypeSpecificityComparatorWithoutDelegate(components.session.typeContext)
        // NB: Adding new resolvers is strongly discouraged because the results are order-dependent.
        return ConeCompositeConflictResolver(
            ConeEquivalentCallConflictResolver(components.session),
            ConeIntegerOperatorConflictResolver,
            ConeOverloadConflictResolver(specificityComparator, components, transformerComponents),
        )
    }
}
