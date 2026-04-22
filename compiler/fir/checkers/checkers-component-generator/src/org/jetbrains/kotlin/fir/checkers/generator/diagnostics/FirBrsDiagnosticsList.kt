/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.checkers.generator.diagnostics

import org.jetbrains.kotlin.fir.checkers.generator.diagnostics.model.DiagnosticList
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.util.PrivateForInline

@Suppress("ClassName", "unused")
@OptIn(PrivateForInline::class)
object BRS_DIAGNOSTICS_LIST : DiagnosticList("FirBrsErrors") {
    val INTRINSICS by object : DiagnosticGroup("Intrinsics") {
        val BRS_INTRINSIC_LITERAL_REQUIRED by error<KtElement>()
    }
}
