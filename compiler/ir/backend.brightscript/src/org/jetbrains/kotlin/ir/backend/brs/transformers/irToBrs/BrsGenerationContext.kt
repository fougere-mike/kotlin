/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.transformers.irToBrs

import org.jetbrains.kotlin.ir.backend.brs.BrsIrBackendContext
import org.jetbrains.kotlin.ir.symbols.IrValueSymbol

/**
 * Shared mutable state used by [IrToBrsTransformer] and its subordinate
 * [IrStatementToBrsTransformer] / [IrExpressionToBrsTransformer] transformers.
 *
 * Relocated from [IrToBrsTransformer] in B1b1 so subordinate transformers no
 * longer reach into sibling-class internals via `parent.*`.
 *
 * B1b1 keeps the bag mutable. A later phase (B1b2) may redesign this to match
 * JS's per-scope-immutable [org.jetbrains.kotlin.ir.backend.js.utils.JsGenerationContext]
 * shape, but that's a separate workstream and will rewrite the mutation model of the traversal.
 */
internal class BrsGenerationContext(
    private val backendContext: BrsIrBackendContext,
)

/**
 * Tracks captured variables when transforming a closure.
 * Maps IR variable symbols to their capture info (name and mutability).
 */
data class CapturedVariable(
    val symbol: IrValueSymbol,
    val name: String,
    val isMutable: Boolean,
)
