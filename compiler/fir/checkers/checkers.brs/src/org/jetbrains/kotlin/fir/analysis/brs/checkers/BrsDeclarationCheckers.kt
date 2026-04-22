/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.brs.checkers

import org.jetbrains.kotlin.fir.analysis.brs.checkers.declaration.FirBrsNameClashClassMembersChecker
import org.jetbrains.kotlin.fir.analysis.brs.checkers.declaration.FirBrsNameClashFileTopLevelDeclarationsChecker
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.*

object BrsDeclarationCheckers : DeclarationCheckers() {
    override val fileCheckers: Set<FirFileChecker>
        get() = setOf(
            FirBrsNameClashFileTopLevelDeclarationsChecker,
        )

    override val classCheckers: Set<FirClassChecker>
        get() = setOf(
            FirBrsNameClashClassMembersChecker,
        )
}
