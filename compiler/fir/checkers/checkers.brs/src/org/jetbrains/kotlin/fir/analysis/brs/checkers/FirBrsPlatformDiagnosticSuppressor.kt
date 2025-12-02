/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.brs.checkers

import org.jetbrains.kotlin.fir.analysis.checkers.FirPlatformDiagnosticSuppressor
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.declarations.FirCallableDeclaration

/**
 * Platform-specific diagnostic suppressor for BrightScript target.
 *
 * Currently inherits default behavior. Can be extended to suppress
 * diagnostics for BrightScript-specific constructs as needed.
 */
class FirBrsPlatformDiagnosticSuppressor : FirPlatformDiagnosticSuppressor {
    override fun shouldReportNoBody(declaration: FirCallableDeclaration, context: CheckerContext): Boolean = true
}
