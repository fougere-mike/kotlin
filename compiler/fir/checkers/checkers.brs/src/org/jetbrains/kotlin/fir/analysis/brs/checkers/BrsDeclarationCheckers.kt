/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.brs.checkers

import org.jetbrains.kotlin.fir.analysis.brs.checkers.declaration.FirBrsBlankAnnotationChecker
import org.jetbrains.kotlin.fir.analysis.brs.checkers.declaration.FirBrsCreateObjectAnnotationChecker
import org.jetbrains.kotlin.fir.analysis.brs.checkers.declaration.FirBrsConstantVarChecker
import org.jetbrains.kotlin.fir.analysis.brs.checkers.declaration.FirBrsIntrinsicUserDefinedChecker
import org.jetbrains.kotlin.fir.analysis.brs.checkers.declaration.FirBrsNameClashClassMembersChecker
import org.jetbrains.kotlin.fir.analysis.brs.checkers.declaration.FirBrsNameClashFileTopLevelDeclarationsChecker
import org.jetbrains.kotlin.fir.analysis.brs.checkers.declaration.FirBrsNameClashFunctionBodyChecker
import org.jetbrains.kotlin.fir.analysis.brs.checkers.declaration.FirBrsOnChangeHandlerChecker
import org.jetbrains.kotlin.fir.analysis.brs.checkers.declaration.FirBrsSceneGraphFieldTypeChecker
import org.jetbrains.kotlin.fir.analysis.brs.checkers.declaration.FirBrsStaticClassChecker
import org.jetbrains.kotlin.fir.analysis.brs.checkers.declaration.FirBrsStaticOverloadFileChecker
import org.jetbrains.kotlin.fir.analysis.brs.checkers.declaration.FirBrsStaticTopLevelExtensionChecker
import org.jetbrains.kotlin.fir.analysis.brs.checkers.declaration.FirBrsTaskStateNotFieldChecker
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.*

object BrsDeclarationCheckers : DeclarationCheckers() {
    override val basicDeclarationCheckers: Set<FirBasicDeclarationChecker>
        get() = setOf(
            FirBrsBlankAnnotationChecker,
            FirBrsCreateObjectAnnotationChecker,
        )

    override val fileCheckers: Set<FirFileChecker>
        get() = setOf(
            FirBrsNameClashFileTopLevelDeclarationsChecker,
            FirBrsStaticOverloadFileChecker,
        )

    override val classCheckers: Set<FirClassChecker>
        get() = setOf(
            FirBrsNameClashClassMembersChecker,
            FirBrsStaticClassChecker,
        )

    override val regularClassCheckers: Set<FirRegularClassChecker>
        get() = setOf(
            FirBrsConstantVarChecker,
        )

    override val propertyCheckers: Set<FirPropertyChecker>
        get() = setOf(
            FirBrsSceneGraphFieldTypeChecker,
            FirBrsOnChangeHandlerChecker,
            FirBrsTaskStateNotFieldChecker,
        )

    override val simpleFunctionCheckers: Set<FirSimpleFunctionChecker>
        get() = setOf(
            FirBrsIntrinsicUserDefinedChecker,
            FirBrsNameClashFunctionBodyChecker,
            FirBrsStaticTopLevelExtensionChecker,
        )
}
