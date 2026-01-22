/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.lower.inline

import org.jetbrains.kotlin.ir.backend.brs.BrsIrBackendContext
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.inline.InlineFunctionResolver
import org.jetbrains.kotlin.ir.inline.InlineMode
import org.jetbrains.kotlin.ir.overrides.isEffectivelyPrivate
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.util.*

/**
 * BrightScript-specific inline function resolver.
 *
 * This resolver handles inline function resolution for the BrightScript backend.
 * Unlike the JVM/JS/Native backends, we don't need to replace coroutine intrinsics
 * because BrightScript uses its own coroutine implementation that doesn't depend
 * on the standard coroutine intrinsics.
 *
 * Note: Currently, this resolver only inlines functions that have bodies available
 * in the current module. Functions from dependencies (like stdlib) are not inlined
 * because their bodies are not available at lowering time. This is a limitation
 * compared to other backends that have full klib inline function deserialization.
 */
internal class BrsInlineFunctionResolver(
    private val context: BrsIrBackendContext,
    private val inlineMode: InlineMode,
) : InlineFunctionResolver() {

    override fun getFunctionDeclaration(symbol: IrFunctionSymbol): IrFunction? {
        if (!symbol.isBound) return null
        val realOwner = symbol.owner.resolveFakeOverrideOrSelf()
        if (!realOwner.isInline) return null

        // For private inline functions mode, only inline private functions
        if (inlineMode == InlineMode.PRIVATE_INLINE_FUNCTIONS && !realOwner.isEffectivelyPrivate()) return null

        // Don't inline external functions (they have no body)
        if (realOwner.isExternal) return null

        // Don't inline if the function has no body (e.g., from dependencies)
        // TODO: Add klib inline function deserialization support
        if (realOwner.body == null) return null

        return realOwner
    }
}
