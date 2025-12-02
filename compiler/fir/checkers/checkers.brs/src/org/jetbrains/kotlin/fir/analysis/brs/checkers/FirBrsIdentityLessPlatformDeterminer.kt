/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.brs.checkers

import org.jetbrains.kotlin.fir.analysis.checkers.FirIdentityLessPlatformDeterminer
import org.jetbrains.kotlin.fir.analysis.checkers.TypeInfo

/**
 * Determines identity-less types for the BrightScript platform.
 *
 * BrightScript doesn't have identity-less primitive types in the same
 * way as JavaScript, so we return false for all types.
 */
object FirBrsIdentityLessPlatformDeterminer : FirIdentityLessPlatformDeterminer() {
    override fun isIdentityLess(typeInfo: TypeInfo): Boolean = false
}
