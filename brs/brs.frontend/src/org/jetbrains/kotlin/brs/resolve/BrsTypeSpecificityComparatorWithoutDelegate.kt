/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.brs.resolve

import org.jetbrains.kotlin.resolve.calls.results.TypeSpecificityComparator
import org.jetbrains.kotlin.types.model.KotlinTypeMarker
import org.jetbrains.kotlin.types.model.TypeSystemInferenceExtensionContext

/**
 * BrightScript type specificity comparator.
 *
 * Unlike JavaScript which has `dynamic` types that are always less specific than
 * concrete types, BrightScript's type system does not have this concept.
 * While BrightScript is dynamically typed at runtime, the Kotlin type system
 * used during compilation does not expose this as a special type that affects
 * overload resolution.
 *
 * Therefore, this comparator never considers one type definitively less specific
 * than another based on platform-specific rules.
 */
class BrsTypeSpecificityComparatorWithoutDelegate(
    val context: TypeSystemInferenceExtensionContext
) : TypeSpecificityComparator {

    override fun isDefinitelyLessSpecific(
        specific: KotlinTypeMarker,
        general: KotlinTypeMarker
    ): Boolean {
        // BrightScript has no dynamic types, so no platform-specific
        // type specificity rules are needed
        return false
    }
}
