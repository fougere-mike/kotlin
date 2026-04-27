/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.brs.checkers.declaration

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirClassChecker
import org.jetbrains.kotlin.fir.declarations.DirectDeclarationsAccess
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.declarations.FirDeclaration

/**
 * Reports `BRS_NAME_CASE_CLASH` when two or more directly-declared members of
 * a class/interface/object have effective BrightScript names that differ in
 * case but are equal after `lowercase()`.
 *
 * Only walks directly-declared members. Inherited-member and fake-override
 * case-clash detection is a known follow-up (would require walking the
 * class's unsubstituted scope, analogous to `FirJsNameClashClassMembersChecker`
 * in JS).
 */
object FirBrsNameClashClassMembersChecker : FirClassChecker(MppCheckerKind.Common) {
    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirClass) {
        val session = context.session
        val groupedByLowercase = mutableMapOf<String, MutableList<Pair<FirDeclaration, String>>>()
        @OptIn(DirectDeclarationsAccess::class)
        for (member in declaration.declarations) {
            val name = member.effectiveBrsName(session) ?: continue
            groupedByLowercase.getOrPut(name.lowercase()) { mutableListOf() }.add(member to name)
        }
        for (group in groupedByLowercase.values) {
            reportCaseClashes(group)
        }
    }
}
