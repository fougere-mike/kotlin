/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.brs.lower.coroutines

import org.jetbrains.kotlin.backend.common.lower.coroutines.AbstractAddContinuationToFunctionCallsLowering
import org.jetbrains.kotlin.ir.backend.brs.BrsIrBackendContext
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.util.parentClassOrNull

/**
 * BrightScript-specific implementation of AddContinuationToFunctionCallsLowering.
 *
 * This lowering does two important things:
 * 1. Adds the continuation parameter to suspend function calls
 * 2. Replaces getContinuation() intrinsic calls with the actual continuation value
 *
 * The second point is critical for the suspendCoroutineUninterceptedOrReturn intrinsic to work.
 * When delay() calls suspendCoroutineUninterceptedOrReturn, that function is inline and contains
 * getContinuation() which must be replaced with the actual continuation in the calling context.
 *
 * Requires AddContinuationToLocalSuspendFunctionsLowering and
 * AddContinuationToNonLocalSuspendFunctionsLowering to transform function declarations first.
 */
class BrsAddContinuationToFunctionCallsLowering(
    override val context: BrsIrBackendContext
) : AbstractAddContinuationToFunctionCallsLowering() {

    override fun IrSimpleFunction.isContinuationItself(): Boolean {
        // Check if this function is a doResume override in a CoroutineImpl subclass.
        // In that case, `this` (the dispatch receiver) IS the continuation.
        val coroutineImplSymbol = context.brsSymbols.coroutineSymbols.coroutineImpl ?: return false
        return overriddenSymbols.any { overriddenSymbol ->
            val overriddenFn = overriddenSymbol.owner
            overriddenFn.name.asString() == "doResume" &&
            overriddenFn.parentClassOrNull?.symbol == coroutineImplSymbol
        }
    }
}
