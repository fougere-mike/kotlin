/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.lower.serialization.ir

import org.jetbrains.kotlin.backend.common.serialization.GlobalDeclarationTable
import org.jetbrains.kotlin.ir.IrBuiltIns

/**
 * Global declaration table for BrightScript IR serialization.
 *
 * Tracks declarations across the entire compilation for consistent serialization.
 */
class BrsGlobalDeclarationTable(builtIns: IrBuiltIns) : GlobalDeclarationTable(BrsManglerIr) {
    init {
        loadKnownBuiltins(builtIns)
    }
}
