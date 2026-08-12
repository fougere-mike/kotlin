/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.brs.checkers

import org.jetbrains.kotlin.fir.analysis.brs.checkers.expression.FirBrsAddFieldTypeChecker
import org.jetbrains.kotlin.fir.analysis.brs.checkers.expression.FirBrsCreateComponentTypeChecker
import org.jetbrains.kotlin.fir.analysis.brs.checkers.expression.FirBrsCreateObjectTypeChecker
import org.jetbrains.kotlin.fir.analysis.brs.checkers.expression.FirBrsIODispatcherChecker
import org.jetbrains.kotlin.fir.analysis.brs.checkers.expression.FirBrsIOWorkerCaptureChecker
import org.jetbrains.kotlin.fir.analysis.brs.checkers.expression.FirBrsIntrinsicArgChecker
import org.jetbrains.kotlin.fir.analysis.brs.checkers.expression.FirBrsNameCallableRefChecker
import org.jetbrains.kotlin.fir.analysis.brs.checkers.expression.FirBrsTryFinallyChecker
import org.jetbrains.kotlin.fir.analysis.checkers.expression.*

object BrsExpressionCheckers : ExpressionCheckers() {
    override val functionCallCheckers: Set<FirFunctionCallChecker>
        get() = setOf(
            FirBrsIntrinsicArgChecker,
            FirBrsIOWorkerCaptureChecker,
            FirBrsAddFieldTypeChecker,
            FirBrsCreateObjectTypeChecker,
            FirBrsCreateComponentTypeChecker,
            FirBrsNameCallableRefChecker,
        )

    override val propertyAccessExpressionCheckers: Set<FirPropertyAccessExpressionChecker>
        get() = setOf(
            FirBrsIODispatcherChecker,
        )

    override val tryExpressionCheckers: Set<FirTryExpressionChecker>
        get() = setOf(
            FirBrsTryFinallyChecker,
        )
}
