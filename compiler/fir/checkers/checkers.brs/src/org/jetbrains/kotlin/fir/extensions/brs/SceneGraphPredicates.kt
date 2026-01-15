/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.extensions.brs

import org.jetbrains.kotlin.fir.extensions.predicate.LookupPredicate
import org.jetbrains.kotlin.name.FqName

/**
 * Predicates for identifying SceneGraph DSL elements in FIR.
 */
object SceneGraphPredicates {
    private val SGLAYOUT_FQN = FqName("kotlin.brs.scenegraph.SGLayout")

    /**
     * Matches classes that have a companion object containing a function annotated with @SGLayout.
     * Uses `hasAnnotated` to match classes that HAVE a member (in companion) with the annotation.
     */
    val HAS_SGLAYOUT_FUNCTION: LookupPredicate = LookupPredicate.create {
        hasAnnotated(SGLAYOUT_FQN)
    }
}
