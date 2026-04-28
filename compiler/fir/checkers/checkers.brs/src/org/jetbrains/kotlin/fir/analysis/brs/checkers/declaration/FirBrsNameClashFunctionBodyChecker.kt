/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.brs.checkers.declaration

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirSimpleFunctionChecker
import org.jetbrains.kotlin.fir.declarations.FirAnonymousFunction
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.fir.visitors.FirVisitorVoid

/**
 * Reports `BRS_NAME_CASE_CLASH` for local declarations (functions and properties) inside a
 * function body whose effective BrightScript names differ only in case. BrightScript identifiers
 * are case-insensitive, so `fun innerOne()` and `fun INNERONE()` silently collide at runtime.
 *
 * Covers Path D: local-scope clash (function-body level). The three existing paths are:
 * - Path A (`FirBrsNameClashClassMembersChecker`): directly-declared class members
 * - Path B (same checker): own-declared member vs. inherited member
 * - Path C (same checker): inherited-vs-inherited clash
 * - Top-level (`FirBrsNameClashFileTopLevelDeclarationsChecker`): same-package declarations
 *
 * **Scope isolation:** each local function body forms its own scope. The visitor descends into
 * `function.body` but stops at nested `FirSimpleFunction` declarations — those are handled when
 * the framework invokes this checker for each nested function in turn.
 *
 * **Suppression:** `FirSimpleFunctionChecker` runs with per-declaration context already pushed by
 * the framework, so `@Suppress("BRS_NAME_CASE_CLASH")` on the local function works automatically
 * (no manual `isSuppressedByAnnotation` helper needed). The framework will skip calling
 * `reportCaseClashes` for any declaration in the group that is suppressed.
 *
 * Note: `@Suppress` on a local function is syntactically valid in Kotlin and works here.
 * For local properties, `@Suppress` applies to the property declaration itself.
 *
 * **What is checked:** `FirSimpleFunction` and `FirProperty` declarations that appear directly
 * in the function's body statements — NOT declarations inside nested classes, objects, or lambdas
 * (those are separate scopes). Anonymous functions (lambdas) are also excluded from descent to
 * avoid false positives in callback-heavy code.
 */
object FirBrsNameClashFunctionBodyChecker : FirSimpleFunctionChecker(MppCheckerKind.Common) {

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirSimpleFunction) {
        val body = declaration.body ?: return
        val session = context.session

        // Collect direct local declarations in this function's body, grouped by lowercase
        // effective name. We stop descent at nested function bodies (handled independently)
        // and at anonymous functions (separate lambda scopes).
        val byLowercase = mutableMapOf<String, MutableList<Pair<FirDeclaration, String>>>()
        body.accept(object : FirVisitorVoid() {
            override fun visitElement(element: FirElement) {
                element.acceptChildren(this)
            }

            override fun visitSimpleFunction(simpleFunction: FirSimpleFunction) {
                // Record this local function in the current scope, then stop descent.
                // The framework will invoke FirBrsNameClashFunctionBodyChecker for the
                // nested function when it visits it — that checker handles its own body.
                val name = simpleFunction.effectiveBrsName(session) ?: return
                byLowercase.getOrPut(name.lowercase()) { mutableListOf() }
                    .add(simpleFunction to name)
            }

            override fun visitProperty(property: FirProperty) {
                // Record local property declarations and continue descent into their initializers
                // (do NOT stop here — a property's initializer may contain further local funs).
                val name = property.effectiveBrsName(session) ?: return
                byLowercase.getOrPut(name.lowercase()) { mutableListOf() }
                    .add(property to name)
                property.acceptChildren(this)
            }

            override fun visitAnonymousFunction(anonymousFunction: FirAnonymousFunction) {
                // Lambda bodies form their own separate scope — do not descend.
            }
        })

        for (group in byLowercase.values) {
            reportCaseClashes(group)
        }
    }
}
